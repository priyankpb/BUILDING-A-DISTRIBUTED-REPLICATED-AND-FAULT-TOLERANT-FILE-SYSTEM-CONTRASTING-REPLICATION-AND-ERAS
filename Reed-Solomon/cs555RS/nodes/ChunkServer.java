package cs555RS.nodes;

import cs555RS.transport.TCPConnection;
import cs555RS.transport.TCPSender;
import cs555RS.util.Protocol;
import cs555RS.wireformats.ChunkServerRegistration;
import cs555RS.wireformats.ChunkServerSendShard;
import cs555RS.wireformats.ClientFetchShard;
import cs555RS.wireformats.ClientWrite;
import cs555RS.wireformats.ControllerRegistrationStatus;
import cs555RS.wireformats.EventFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkServer implements Node {

    public Socket s;
    public ServerSocket csServerSocket;
    private TCPConnection conn;
    private int nodeID = -1;
    private static ChunkServer chunkServer;

    private Map<String, String> localShardsCache = new HashMap<>();
    private Map<String, String> newLocalShardsCache = new HashMap<>();

    public Map<String, String> getLocalShardsCache() {
        return localShardsCache;
    }

    public void setLocalShardsCache(Map<String, String> localShardsCache) {
        this.localShardsCache = localShardsCache;
    }

    public Map<String, String> getNewLocalShardsCache() {
        return newLocalShardsCache;
    }

    public void setNewLocalShardsCache(Map<String, String> newLocalShardsCache) {
        this.newLocalShardsCache = newLocalShardsCache;
    }

    public TCPConnection getConn() {
        return this.conn;
    }

    public int getNodeID() {
        return this.nodeID;
    }

    public ChunkServer() {
    }

    public ChunkServer getChunkServer() {
        return chunkServer;
    }

    private ChunkServer(InetAddress ip, int port) throws IOException, Exception {

        this.s = new Socket(ip, port);
        this.conn = new TCPConnection(this.s, this);
        this.csServerSocket = new ServerSocket(0);
        System.out.println("[INFO] " + s.getLocalAddress().getHostName() + ":" + this.csServerSocket.getLocalPort());
    }

    public static void main(String[] args) throws UnknownHostException, Exception {
        String hostName = Protocol.HOSTNAME;
        int port = Protocol.PORT;
        if (args.length == 2) {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }
        InetAddress ip = InetAddress.getByName(hostName);
//        System.out.println("-ip-port-" + hostName + ":" + port);
        chunkServer = new ChunkServer(ip, port);
        chunkServer.start();
    }

    private void start() throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ChunkServerRegistration csr = (ChunkServerRegistration) eventFactory.createEvent(Protocol.CHUNKSERVER_REGISTRATION);
        csr.setIp(InetAddress.getLocalHost().getHostAddress().getBytes());
        csr.setPort(this.csServerSocket.getLocalPort());
        this.conn.getSender().sendData(csr.getByte());
        while (true) {
            Socket csClient = this.csServerSocket.accept();
            TCPConnection localTCPConnection = new TCPConnection(csClient, this);
        }
    }

    public void onEvent(byte[] data, Socket s) {

        try {
            EventFactory eventFactory = EventFactory.getInstance();
            switch (data[0]) {

                case Protocol.CONTROLLER_REGISTRATION_STATUS:
                    ControllerRegistrationStatus crs = new ControllerRegistrationStatus(data);
                    this.nodeID = crs.getNodeID();
                    if (this.nodeID >= 0) {
                        System.out.println("[INFO] Node ID: " + this.nodeID);
                        System.out.println("[INFO] " + crs.getInfo());
                    } else {
                        System.err.println("[ERROR] Registration UNSUCCESSFULL.");
                    }
                    break;

                case Protocol.CLIENT_WRITE:
                    System.out.println("[INFO] Client Write request received");
                    ClientWrite clientWrite = new ClientWrite(data);
                    int clientID = clientWrite.getClientID();
                    String shardName = new String(clientWrite.getShardName());
                    byte[] shardData = clientWrite.getData();

//                System.out.println("[INFO] received data from client: " + clientID);
                    saveFile(shardName, shardData);

                    break;

                case Protocol.CLIENT_FETCH_SHARD:
                    System.out.println("[INFO] Fetch request received");
                    ClientFetchShard cfc = new ClientFetchShard(data);
                    byte[] clientIPaddr = cfc.getIp();
                    int clientPort = cfc.getPort();
                    int clientreq = cfc.getClientID();
                    byte[] sname = cfc.getShardName();

                    sendToClient(clientIPaddr, clientPort, clientreq, sname, s);

                    break;

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void saveFile(String chunkName, byte[] chunkData) {
        FileSaver fs = new FileSaver(chunkName, chunkData);
        Thread fileSaverThread = new Thread(fs);
        fileSaverThread.start();
    }

    public void resetNewLocalShardsCache() {
        synchronized (newLocalShardsCache) {
//            newLocalShardsCache.clear();
        }
    }

    private void sendToClient(byte[] clientIPaddr, int clientPort, int clientreq, byte[] cname, Socket clientsocket) throws NoSuchAlgorithmException {
        try {
            EventFactory eventFactory = EventFactory.getInstance();

            String shardToSend = "/tmp/cs555_priyankb" + new String(cname);
//            System.out.println("-chunkname- " + shardToSend);
            File cts = new File(shardToSend);
            boolean corrupted = false;
            byte[] content = new byte[(int) cts.length()];

            ChunkServerSendShard cssc = (ChunkServerSendShard) eventFactory.createEvent(Protocol.CHUNKSERVER_SEND_SHARD);
            cssc.setShardName(shardToSend.getBytes());
            if (cts.exists()) {
                RandomAccessFile fin = new RandomAccessFile(cts, "rw");

                fin.read(content);

                String storedShecksum;
                synchronized (localShardsCache) {
                    storedShecksum = localShardsCache.get(shardToSend);
                }

                String computedChecksum = computeSHA1(content);
//                System.out.println("-computed-" + computedChecksum);
//                System.out.println("-stored-" + storedShecksum);
                if (!computedChecksum.equals(storedShecksum)) {
                    content = new byte[(int) cts.length()];
                    corrupted = true;
                } else {
//                    System.out.println("-sharddata-" + content);
                    cssc.setData(content);
                }

            } else {
                content = new byte[(int) cts.length()];
                corrupted = true;
            }

            cssc.setCorrupted(corrupted);

            cssc.setClientID(clientreq);
            try {
//                clientConn.getSender().sendData(cssc.getByte());
                TCPSender sender = new TCPSender(clientsocket);
                sender.sendData(cssc.getByte());
            } catch (Exception ex) {
                Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            System.err.println("ERROR : ");
            ex.printStackTrace();
        }
    }

    class FileSaver implements Runnable {

        private String shardName;
        private byte[] shardData;

        private FileSaver(String shardName, byte[] shardData) {
            this.shardName = shardName;
            this.shardData = shardData;
        }

        @Override
        public void run() {
            try {
                String fn = "/tmp/cs555_priyankb" + shardName;
                String path = fn.substring(0, fn.lastIndexOf("/") + 1);
//                System.out.println("-store shard-" + fn);
                File newFile = new File(path);
                if (!newFile.exists()) {
                    newFile.mkdirs();
                }

                File actualFile = new File(fn);

                FileOutputStream fout = new FileOutputStream(actualFile);
//                fout.write(chunkData, 0, Protocol.CHUNK_SIZE);
                fout.write(shardData, 0, shardData.length);

                String checksum = computeSHA1(shardData);

                synchronized (localShardsCache) {
                    localShardsCache.put(fn, checksum);
                }
                synchronized (newLocalShardsCache) {
                    newLocalShardsCache.put(fn, checksum);
                }

            } catch (IOException ex) {
//                System.err.println("[ERROR] FILE SAVE");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private TCPConnection establishConnection(InetAddress ip, int port) throws IOException {
//        System.out.println("[INFO] Trying to connect to "+ip+":"+port);
        Socket sc = new Socket(ip.getHostAddress(), port);
        TCPConnection tconn = new TCPConnection(sc, this);
        return tconn;
    }

    private String computeSHA1(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");

        StringBuffer sb = new StringBuffer("");

        md.update(data);
        byte[] mdbytes = md.digest();

        //convert the byte to hex format
        for (int j = 0; j < mdbytes.length; j++) {
            sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
        }
        sb.append("\n");

        String check = sb.toString();
        return check;
    }
}
