package cs555.nodes;

import cs555.transport.TCPConnection;
import cs555.transport.TCPSender;
import cs555.util.FileSplit;
import cs555.util.InteractiveCommandParser;
import cs555.util.Protocol;
import cs555.wireformats.ChunkServerSendChunk;
import cs555.wireformats.ClientChunkName;
import cs555.wireformats.ClientFetchChunk;
import cs555.wireformats.ClientOverwrite;
import cs555.wireformats.ClientRead;
import cs555.wireformats.ClientRegistration;
import cs555.wireformats.ClientWrite;
import cs555.wireformats.ControllerClientChunkservers;
import cs555.wireformats.ControllerClientRegistrationStatus;
import cs555.wireformats.ControllerOverwrite;
import cs555.wireformats.ControllerReadReply;
import cs555.wireformats.EventFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client
        implements Node {

    private int nodeID = -1;
    public Socket s;
    public ServerSocket clientServerSocket;
    private TCPConnection conn;
    private static Client client;
    public String myIP;

    public static Client getClient() {
        return client;
    }

    public Map<String, byte[]> chunkCache = new HashMap<>();
    public Map<String, Integer> incompleteCounter = new HashMap<>();
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
        this.s = new Socket(ip, port);
        this.conn = new TCPConnection(this.s, this);
        this.clientServerSocket = new ServerSocket(0);
        this.myIP = s.getLocalAddress().getHostAddress();
        System.out.println("[INFO] " + myIP + ":" + this.clientServerSocket.getLocalPort());
    }

    public void onEvent(byte[] data, Socket s) {
        EventFactory eventFactory = EventFactory.getInstance();
        switch (data[0]) {
            case Protocol.CONTROLLER_CLIENT_REGISTRATION_STATUS:
                ControllerClientRegistrationStatus ccrs;
                try {
                    ccrs = new ControllerClientRegistrationStatus(data);

                    this.nodeID = ccrs.getNodeID();
                    if (this.nodeID >= 0) {
                        System.out.println("[INFO] Node ID: " + this.nodeID);
                        System.out.println("[INFO] " + ccrs.getInfo());
                    } else {
                        System.err.println("[ERROR] Registration UNSUCCESSFULL.");
                    }
                } catch (IOException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case Protocol.CONTROLLER_OVERWRITE: {
                try {
                    System.out.println("[INFO] FILE REPLY RECEIVED");
                    ControllerOverwrite overwrite = new ControllerOverwrite(data);
                    if (overwrite.getClientID() == nodeID) {
                        if (!overwrite.isPresent()) {
                            String fileName = overwrite.getFileName();
                            FileSplit fs = new FileSplit(new File(fileName));
                            storeFile(fs);
                        } else {
                            System.out.println("[INFO] File already stored");
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            break;

            case Protocol.CONTROLLER_CLIENT_CHUNKSERVERS:
//                System.out.println("[INFO] ChunkServer Info received");
                ControllerClientChunkservers ccc;
                try {
                    ccc = new ControllerClientChunkservers(data);

//                System.out.print("1");
                    int clientNodeID = ccc.getNodeID();
                    if (clientNodeID == this.nodeID) {
                        String chnkName = new String(ccc.getChunkName());
                        if (this.chunkCache.containsKey(chnkName)) {
                            ClientWrite clientWrite = (ClientWrite) eventFactory.createEvent(Protocol.CLIENT_WRITE);
                            synchronized (this.chunkCache) {
                                clientWrite.setClientID(clientNodeID);
                                clientWrite.setChunkName(chnkName.getBytes());
                                clientWrite.setData((byte[]) this.chunkCache.get(chnkName));
//                            System.out.println("-chunk" + new String(this.chunkCache.get(chnkName)));
                                chunkCache.remove(chnkName);
                            }

                            clientWrite.setNode1(ccc.getNode2());
                            clientWrite.setIp1(ccc.getIp2());
                            clientWrite.setPort1(ccc.getPort2());

                            clientWrite.setNode2(ccc.getNode3());
                            clientWrite.setIp2(ccc.getIp3());
                            clientWrite.setPort2(ccc.getPort3());

                            int port = ccc.getPort1();
                            String hostAddr = new String(ccc.getIp1());
                            InetAddress ip = InetAddress.getByName(hostAddr);
//                            TCPConnection conn1 = establishConnection(ip, port);
                            Socket soc1 = new Socket(ip, port);
                            TCPSender sen1 = new TCPSender(soc1);
                            try {
//                            System.out.println("[INFO] Writing to ChunkServer: " + ccc.getNode1());
                                sen1.sendData(clientWrite.getByte());
                            } catch (Exception ex) {
                                System.err.println("[ERROR] while writing from client");
                            }
                        }

                    }
                } catch (IOException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case Protocol.CONTROLLER_READ_REPLY:
                System.out.println("[INFO] REPLY RECEIVED FROM CONTROLLER");
                ControllerReadReply crr;
                try {
                    crr = new ControllerReadReply(data);

                    Map<Integer, String> chunkMap = crr.getFinalMapToSendToClient();
                    String fName = new String(crr.getFileName());
                    int size = chunkMap.size();
//                Map<String, String> chunksToFetch = new String[size];
                    synchronized (incompleteCounter) {
                        incompleteCounter.put(fName, size);
                        System.out.println("[INFO] CHUNKS: " + size);
                    }
//                System.out.println("-chunkmap-"+ chunkMap);
//                    synchronized (chunkMap) {
                    int i = 1;
                    for (Map.Entry<Integer, String> entrySet : chunkMap.entrySet()) {
                        ClientFetchChunk cfc = (ClientFetchChunk) eventFactory.createEvent(Protocol.CLIENT_FETCH_CHUNK);
                        cfc.setClientID(this.nodeID);

                        cfc.setPort(this.clientServerSocket.getLocalPort());
                        cfc.setIp((this.myIP.getBytes()));
                        int chunkNumber = entrySet.getKey();
                        String ipport = entrySet.getValue();
                        String chunkName = fName + "_chunk" + chunkNumber;
                        cfc.setChunkName(chunkName.getBytes());
                        String[] nodesl = ipport.split("#");
                        int attempt = 0;
                        int att = 0;
//                        TCPConnection[] conn1 = new TCPConnection[nodesl.length];
                        TCPSender conn1;
                        Socket socket = null;
                        InetAddress[] ip = new InetAddress[nodesl.length];
                        int[] port = new int[3];
                        int num = 0;
                        for (String split1 : nodesl) {
                            String[] split = split1.split(":");
                            ip[num] = InetAddress.getByName(split[0]);
                            port[num] = Integer.parseInt(split[1]);
                            num++;
                        }

                        for (int j = 0; j < nodesl.length; j++) {
                            try {
                                socket = new Socket(ip[j], port[j]);
                                break;
                            } catch (Exception ex) {
                                System.out.println("chunk server-" + ip[j] + "- is not listening");
                            }
                        }
                        conn1 = new TCPSender(socket);
                        conn1.sendData(cfc.getByte());
//                     
                    }
                } catch (IOException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;

            case Protocol.CHUNKSERVER_SENDCHUNK:
                System.out.println("[INFO] Chunk Received");
                ChunkServerSendChunk cssc;
                try {
                    cssc = new ChunkServerSendChunk(data);

                    String receivedChunkName = new String(cssc.getChunkName());
                    receivedChunkName = receivedChunkName.replaceFirst("/tmp/cs555_priyankb", "");
                    String receivedFileName = receivedChunkName.substring(0, receivedChunkName.lastIndexOf("_"));
                    byte[] receivedChunkData = cssc.getData();
                    int counter;
//                    System.out.println("-counter-");
                    synchronized (incompleteCounter) {
                        counter = incompleteCounter.get(receivedFileName);
                        counter--;
                        incompleteCounter.put(receivedFileName, counter);
                    }

                    synchronized (receivedChunks) {
                        int number = getChunkNumber(receivedChunkName);
                        receivedChunks.put(number, receivedChunkData);
                    }

                    if (counter == 0) {
                        String path = "/tmp/cs555_priyankb" + receivedFileName.substring(0, receivedFileName.lastIndexOf("/") + 1);
//                    System.out.println("-path-" + path);
                        File newFile = new File(path);
                        if (!newFile.exists()) {
                            newFile.mkdirs();
                        }
//                    System.out.println("-creating file-");
                        String actualReceivedFileName = "/tmp/cs555_priyankb" + receivedFileName;
//                    System.out.println("-name-" + actualReceivedFileName);
                        File actualFile = new File(actualReceivedFileName);
                        if (!actualFile.exists()) {
                            actualFile.createNewFile();
                        }
                        FileOutputStream fout = new FileOutputStream(actualFile);

                        synchronized (incompleteCounter) {
                            incompleteCounter.remove(receivedFileName);
                        }
//                    byte[] finalData = new byte[counter];

                        synchronized (receivedChunks) {

                            for (Map.Entry<Integer, byte[]> entrySet : receivedChunks.entrySet()) {
                                int key = entrySet.getKey();
                                byte[] value = entrySet.getValue();

//                            if (key.substring(0, key.lastIndexOf("_")).equals(receivedFileName)) {
//                                System.out.println("-value-" + value);
                                fout.write(value);
//                                write(value, 0, value.length);
//                                receivedChunks.remove(key);
//                            }

                            }
                            receivedChunks.clear();
                            System.out.println("[INFO] File Received");
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }

    public void start() throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ClientRegistration csr = (ClientRegistration) eventFactory.createEvent(Protocol.CLIENT_REGISTRATION);
        csr.setIp(InetAddress.getLocalHost().getHostAddress().getBytes());
        csr.setPort(this.clientServerSocket.getLocalPort());
        this.conn.getSender().sendData(csr.getByte());

        InteractiveCommandParser interactiveCommandParser = new InteractiveCommandParser(this);
        Thread commandThread = new Thread(interactiveCommandParser);
        commandThread.start();
        while (true) {
            Socket csClient = this.clientServerSocket.accept();
            TCPConnection localTCPConnection = new TCPConnection(csClient, this);
        }
    }

    public void storeFile(FileSplit fs) throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        //byte[][] data = fs.getData();
//        System.out.println("-data-" + new String());
        List<byte[]> data = fs.getData();
        int numOfChunks = fs.getNumberOfChunks();

        String prefix = fs.getPrefix();
        List<String> tempchunkName = fs.getChunkNames();
//        System.out.println("-size: " + numOfChunks);
        for (int i = 0; i < numOfChunks; i++) {
            String chunkNames = prefix + tempchunkName.get(i);
//            System.out.println("-prefix- " + prefix);
//            System.out.println("-chunkname- " + tempchunkName.get(i));
            ClientChunkName ccn = (ClientChunkName) eventFactory.createEvent(Protocol.CLIENT_CHUNK_NAME);
            ccn.setNodeID(this.nodeID);
            byte[] chunkNameBytes = chunkNames.getBytes();
            ccn.setData(chunkNameBytes);
            this.conn.getSender().sendData(ccn.getByte());
            synchronized (this.chunkCache) {
                this.chunkCache.putIfAbsent(chunkNames, data.get(i));
//                System.out.println("-chunk: " + new String(data.get(i)));
            }
        }
    }

    private int getChunkNumber(String s) {
        StringBuffer buff = new StringBuffer(s);
        String lastToken = buff.substring(buff.lastIndexOf("k") + 1);
        int number = Integer.parseInt(lastToken);
        return number;
    }

    private TCPSender establishConnection(InetAddress ip, int port) throws IOException {
//        System.out.print("-ip "+ip.getHostAddress());
        Socket sc = new Socket(ip.getHostAddress(), port);
        TCPSender sen = new TCPSender(sc);
        return sen;
    }

    public void sendReadRequest(String fileName) throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ClientRead cr = (ClientRead) eventFactory.createEvent(Protocol.CLIENT_READ);
        cr.setClientID(nodeID);
        cr.setFileName(fileName.getBytes());
        this.conn.getSender().sendData(cr.getByte());
    }

    public void sendPresentReq(String fileName) throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ClientOverwrite overwrite = (ClientOverwrite) eventFactory.createEvent(Protocol.CLIENT_OVERWRITE);
        overwrite.setClientID(nodeID);
        overwrite.setFileName(fileName);
        this.conn.getSender().sendData(overwrite.getByte());
    }
}
