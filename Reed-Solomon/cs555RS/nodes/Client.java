package cs555RS.nodes;

import cs555RS.transport.TCPConnection;
import cs555RS.transport.TCPSender;
import cs555RS.util.FileSplit;
import cs555RS.util.InteractiveCommandParser;
import cs555RS.util.Protocol;
import cs555RS.wireformats.ChunkServerSendShard;
import cs555RS.wireformats.ClientChunkName;
import cs555RS.wireformats.ClientFetchShard;
import cs555RS.wireformats.ClientRead;
import cs555RS.wireformats.ClientRegistration;
import cs555RS.wireformats.ClientWrite;
import cs555RS.wireformats.ControllerClientChunkservers;
import cs555RS.wireformats.ControllerClientRegistrationStatus;
import cs555RS.wireformats.ControllerReadReply;
import cs555RS.wireformats.EventFactory;
import erasure.ReedSolomon;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client
        implements Node {

    public static final int DATA_SHARDS = 6;
    public static final int PARITY_SHARDS = 3;
    public static final int TOTAL_SHARDS = 9;

    public static final int BYTES_IN_INT = 4;

    private int nodeID = -1;
    public Socket s;
    public ServerSocket clientServerSocket;
    private TCPSender senderController;
    private static Client client;
    public String myIP;
//    InetAddress serverIP;
//    int serverport;

    public static Client getClient() {
        return client;
    }

    public Map<Integer, byte[]> shardCache = new HashMap<>();
    public Map<Integer, byte[]> receivedChunks = new TreeMap<>();

    public static void main(String[] args)
            throws UnknownHostException, IOException, Exception {
        String hostName = Protocol.HOSTNAME;
        int port = Protocol.PORT;
        if (args.length == 2) {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }
        InetAddress ip = InetAddress.getByName(hostName);

        client = new Client(ip, port);
        client.start();
    }

    private Client(InetAddress ip, int port) throws IOException {
//        this.serverIP = ip;
//        this.serverport = port;
        this.s = new Socket(ip, port);
        senderController = new TCPSender(s);
        this.clientServerSocket = new ServerSocket(0);
        this.myIP = s.getLocalAddress().getHostAddress();
        String myName = this.myIP = s.getLocalAddress().getHostName();
        System.out.println("[INFO] " + myName + ":" + this.clientServerSocket.getLocalPort());
    }

    public void onEvent(byte[] data, Socket s) throws IOException {
        EventFactory eventFactory = EventFactory.getInstance();
        switch (data[0]) {

            case Protocol.CONTROLLER_CLIENT_REGISTRATION_STATUS:
                ControllerClientRegistrationStatus ccrs = new ControllerClientRegistrationStatus(data);
                this.nodeID = ccrs.getNodeID();
                if (this.nodeID >= 0) {
                    System.out.println("[INFO] Node ID: " + this.nodeID);
                    System.out.println("[INFO] " + ccrs.getInfo());
                } else {
                    System.err.println("[ERROR] Registration UNSUCCESSFULL.");
                }
                break;
        }
    }

    public void start() throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ClientRegistration csr = (ClientRegistration) eventFactory.createEvent(Protocol.CLIENT_REGISTRATION);
        csr.setIp(InetAddress.getLocalHost().getHostAddress().getBytes());
        csr.setPort(this.clientServerSocket.getLocalPort());
        this.senderController.sendData(csr.getByte());
        System.out.println("");
        receiveMessage(s);

        InteractiveCommandParser interactiveCommandParser = new InteractiveCommandParser(this);
        Thread commandThread = new Thread(interactiveCommandParser);
        commandThread.start();
    }

    private void receiveMessage(Socket socket) {
        DataInputStream dIn = null;
        int messageLen = 0;
        try {
            dIn = new DataInputStream(socket.getInputStream());
            messageLen = dIn.readInt();
            byte[] messagedata = new byte[messageLen];
            dIn.readFully(messagedata, 0, messageLen);
            this.onEvent(messagedata, socket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void storeFile(FileSplit fs) throws IOException, Exception {
//        Socket serverS = new Socket(serverIP, nodeID);
//        TCPSender serverSender = new TCPSender(serverS);
        EventFactory eventFactory = EventFactory.getInstance();
        List<byte[]> data = fs.getData();
        int numOfChunks = fs.getNumberOfChunks();

        String prefix = fs.getPrefix();
        List<String> tempchunkName = fs.getChunkNames();
        for (int i = 0; i < numOfChunks; i++) {
            String chunkNames = prefix + tempchunkName.get(i);

            //Reed-Solomon
            byte[] chunkData = data.get(i);
            int storedsize = chunkData.length + BYTES_IN_INT;
            int shardsize = (storedsize + DATA_SHARDS - 1) / DATA_SHARDS;
            int buffersize = shardsize * DATA_SHARDS;
            byte[] allBytes = new byte[buffersize];
            ByteBuffer.wrap(allBytes).putInt(chunkData.length);
            System.arraycopy(chunkData, 0, allBytes, BYTES_IN_INT, chunkData.length);

            int paddingLen = buffersize - (storedsize);
            byte[] paddedZeros = new byte[paddingLen];
            for (int j = 0; j < paddingLen; j++) {
                paddedZeros[j] = 0;
            }
            if (paddingLen != 0) {
                System.arraycopy(paddedZeros, 0, allBytes, storedsize, paddingLen);
            }
            byte[][] shards = new byte[TOTAL_SHARDS][shardsize];

            for (int j = 0; j < DATA_SHARDS; j++) {
                System.arraycopy(allBytes, j * shardsize, shards[j], 0, shardsize);
            }

            ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
            reedSolomon.encodeParity(shards, 0, shardsize);

            for (int j = 0; j < TOTAL_SHARDS; j++) {
                synchronized (this.shardCache) {
                    this.shardCache.putIfAbsent(j, shards[j]);
                }
            }
            ClientChunkName ccn = (ClientChunkName) eventFactory.createEvent(Protocol.CLIENT_CHUNK_NAME);
            ccn.setNodeID(this.nodeID);
            byte[] chunkNameBytes = chunkNames.getBytes();
            ccn.setData(chunkNameBytes);
//            System.out.println("-ccn " + ccn.getByte());
            senderController.sendData(ccn.getByte());

//            receiveMessage(s);
            DataInputStream dIn = null;
            int messageLen = 0;

            dIn = new DataInputStream(senderController.getSocket().getInputStream());
            messageLen = dIn.readInt();
//            System.out.println("-message len-" + messageLen);
            byte[] messagedata = new byte[messageLen];
            dIn.readFully(messagedata, 0, messageLen);

            ControllerClientChunkservers ccc = new ControllerClientChunkservers(messagedata);
//                System.out.print("1");
            int clientNodeID = ccc.getNodeID();
            if (clientNodeID == this.nodeID) {
                String chnkName = new String(ccc.getChunkName());
                Map<Integer, Map<Integer, String>> chunkServers = ccc.getChunkServers();

                for (Map.Entry<Integer, Map<Integer, String>> entrySet : chunkServers.entrySet()) {
                    ClientWrite clientWrite = (ClientWrite) eventFactory.createEvent(Protocol.CLIENT_WRITE);
                    int shardNum = entrySet.getKey();
                    Map<Integer, String> nodeaddr = entrySet.getValue();

                    int nodeID = 0;
                    String tmpipport = "";
                    for (Map.Entry<Integer, String> entrySet1 : nodeaddr.entrySet()) {
                        int tmpnodeID = entrySet1.getKey();
                        String tempipport = entrySet1.getValue();
                        nodeID = tmpnodeID;
                        tmpipport = tempipport;
                    }
                    String shardName = chnkName + "_shard" + shardNum;
                    clientWrite.setClientID(clientNodeID);
                    clientWrite.getShardName(shardName.getBytes());
                    clientWrite.setData(shards[shardNum - 1]);
                    String[] ipport = tmpipport.split(":");
                    int port = Integer.parseInt(ipport[1]);
                    String ip = ipport[0];
                    Socket soc = new Socket(ip, port);
                    TCPSender send = new TCPSender(soc);

                    try {
                        send.sendData(clientWrite.getByte());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
            System.out.print("-");
        }
        System.out.println("\n[INFO] FILE STORED!");
//        serverSender.getSocket().close();
    }

    private int getChunkNumber(String s) {
        StringBuffer buff = new StringBuffer(s);
        String lastToken = buff.substring(buff.lastIndexOf("k") + 1);
        int number = Integer.parseInt(lastToken);
        return number;
    }

    private TCPConnection establishConnection(String ip, int port) throws IOException {
//        System.out.print("-ip "+ip.getHostAddress());
        Socket sc = new Socket(ip, port);
        TCPConnection tconn = new TCPConnection(sc, this);
        return tconn;
    }

    public void sendReadRequest(String fileName) throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ClientRead cr = (ClientRead) eventFactory.createEvent(Protocol.CLIENT_READ);
        cr.setClientID(nodeID);
        cr.setFileName(fileName.getBytes());
        senderController.sendData(cr.getByte());
//        receiveMessage(s);
        DataInputStream dIn = null;
        int messageLen = 0;

        dIn = new DataInputStream(senderController.getSocket().getInputStream());
        messageLen = dIn.readInt();
//            System.out.println("-message len-" + messageLen);
        byte[] messagedata = new byte[messageLen];
        dIn.readFully(messagedata, 0, messageLen);
//            System.out.println("-message-" + new String(messagedata));
//            this.onEvent(messagedata, socket);

        ControllerReadReply crr = new ControllerReadReply(messagedata);
        Map<Integer, Map<Integer, String>> chunkMap = crr.getFinalMapToSendToClient();
        String fName = new String(crr.getFileName());

        String path = "/tmp/cs555_priyankb" + fName.substring(0, fName.lastIndexOf("/") + 1);
//                System.out.println("-path- " + path);
        File newFile = new File(path);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
//                System.out.println("-creating file-");
        String actualReceivedFileName = "/tmp/cs555_priyankb" + fName;
//                System.out.println("-name- " + actualReceivedFileName);
        File actualFile = new File(actualReceivedFileName);
        if (!actualFile.exists()) {
            actualFile.createNewFile();
        }
        FileOutputStream fout = new FileOutputStream(actualFile);
//                System.out.println("");
        int size = chunkMap.size();
//                Map<String, String> chunksToFetch = new String[size];
//                System.out.println("-chunkmap-"+ chunkMap);
//        synchronized (chunkMap) {

        for (Map.Entry<Integer, Map<Integer, String>> entrySet : chunkMap.entrySet()) {
            int chunkNo = entrySet.getKey();
            Map<Integer, String> shardLoc = entrySet.getValue();

            byte[][] shards = new byte[TOTAL_SHARDS][];
            int shardNum = 0;
            int shardCount = 0;
            int shardSize = 0;
            boolean[] shardPresent = new boolean[TOTAL_SHARDS];
            for (Map.Entry<Integer, String> entrySet1 : shardLoc.entrySet()) {
                int shardNo = entrySet1.getKey();
                String ipport = entrySet1.getValue();
                String shardName = fName + "_chunk" + chunkNo + "_shard" + shardNo;
                String[] split = ipport.split(":");
                InetAddress ip = InetAddress.getByName(split[0]);
                int port = Integer.parseInt(split[1]);

                ClientFetchShard cfc = (ClientFetchShard) eventFactory.createEvent(Protocol.CLIENT_FETCH_SHARD);
                cfc.setClientID(this.nodeID);
                cfc.setShardName(shardName.getBytes());
                cfc.setPort(this.clientServerSocket.getLocalPort());
                cfc.setIp((this.myIP.getBytes()));
                Socket chs = new Socket(ip, port);

                TCPSender sender = new TCPSender(chs);
                try {
                    sender.sendData(cfc.getByte());
                } catch (Exception ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

//                            receiveMessage(chs);
                DataInputStream dIn1 = null;
                int messageLen1 = 0;
                Socket soc = sender.getSocket();
                dIn1 = new DataInputStream(soc.getInputStream());
                messageLen1 = dIn1.readInt();
                byte[] messagedata1 = new byte[messageLen1];
                dIn1.readFully(messagedata1, 0, messageLen1);
                if (messagedata1[0] == Protocol.CHUNKSERVER_SEND_SHARD) {
//                                System.out.println("[INFO] Shard Received");

                    ChunkServerSendShard cssc = new ChunkServerSendShard(messagedata1);
                    boolean corrupted = cssc.isCorrupted();
                    if (!corrupted) {
                        byte[] receivedShardData = cssc.getData();
                        shards[shardNum] = receivedShardData;
                        shardPresent[shardNum] = true;
                        shardCount++;
                        shardSize = receivedShardData.length;
                    }
                }
                shardNum++;
            }
            if (shardCount < DATA_SHARDS) {
                System.err.println("[ERROR] NOT ENOUGH SHARDS PRESENT");
            }
            for (int i = 0; i < TOTAL_SHARDS; i++) {
                if (!shardPresent[i]) {
                    shards[i] = new byte[shardSize];
                }
            }

            //Reed-Solomon
            ReedSolomon reedSolomon = new ReedSolomon(DATA_SHARDS, PARITY_SHARDS);
            reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

            byte[] allbytes = new byte[shardSize * DATA_SHARDS];
            for (int i = 0; i < DATA_SHARDS; i++) {
                System.arraycopy(shards[i], 0, allbytes, shardSize * i, shardSize);
            }

            int chunkSize = ByteBuffer.wrap(allbytes).getInt();
//                System.out.println("-chunksize" + chunkSize);
//                System.out.println("-shardsize" + shardSize);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(allbytes, BYTES_IN_INT, chunk, 0, chunkSize);
            fout.write(chunk);
            System.out.print("-");
        }
        fout.close();
        System.out.println("[INFO] FILE RECEIVED!");
    }

//    }
}
