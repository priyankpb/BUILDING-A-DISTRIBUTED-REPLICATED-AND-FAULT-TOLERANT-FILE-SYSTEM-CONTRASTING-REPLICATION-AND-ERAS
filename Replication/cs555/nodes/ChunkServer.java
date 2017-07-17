package cs555.nodes;

import cs555.transport.TCPConnection;
import cs555.transport.TCPSender;
import cs555.util.Heartbeat;
import cs555.util.MetaData;
import cs555.util.MetaDataComputer;
import cs555.util.Protocol;
import cs555.wireformats.ChunkServerChunkServer1;
import cs555.wireformats.ChunkServerChunkServer2;
import cs555.wireformats.ChunkServerChunkServerTransfer;
import cs555.wireformats.ChunkServerDataCorrupt;
import cs555.wireformats.ChunkServerHealChunkServer;
import cs555.wireformats.ChunkServerProbe;
import cs555.wireformats.ChunkServerRegistration;
import cs555.wireformats.ChunkServerSendChunk;
import cs555.wireformats.ClientFetchChunk;
import cs555.wireformats.ClientWrite;
import cs555.wireformats.ControllerHealChunkServer;
import cs555.wireformats.ControllerProbe;
import cs555.wireformats.ControllerRegistrationStatus;
import cs555.wireformats.ControllerTransferChunkServer;
import cs555.wireformats.EventFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkServer implements Node {

    public Socket s;
    public ServerSocket csServerSocket;
    private TCPConnection conn;
    private int nodeID = -1;
    private static ChunkServer chunkServer;

    private Map<String, MetaData> metaDataCache = new HashMap<>();
    private Map<String, MetaData> newMetaDataCache = new HashMap<>();

    public Map<String, MetaData> getMetaDataCache() {
        return metaDataCache;
    }

    public Map<String, MetaData> getNewMetaDataCache() {
        return newMetaDataCache;
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

    private ChunkServer(InetAddress ip, int port, int myport) {
        try {
            this.s = new Socket(ip, port);
        } catch (IOException ex) {
            System.out.println("[ERROR] CHUNKSERVER CANNOT CONNECT TO CONTROLLER");
        }
        this.conn = new TCPConnection(this.s, this);
        try {
            this.csServerSocket = new ServerSocket(myport);
        } catch (IOException ex) {
            System.out.println("[ERROR] CHUNKSERVER CANNOT START ON PORT " + myport);
        }
        System.out.println("[INFO] " + s.getLocalAddress().getHostName() + ":" + this.csServerSocket.getLocalPort());
    }

    public static void main(String[] args)
            throws UnknownHostException, Exception {

        String hostName = Protocol.HOSTNAME;
        int port = Protocol.PORT;
        int myport = 5000;
        if (args.length == 3) {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
            myport = Integer.parseInt(args[2]);
        }
        InetAddress ip = InetAddress.getByName(hostName);
        chunkServer = new ChunkServer(ip, port, myport);
        chunkServer.start();
    }

    private void start() throws IOException, Exception {
        EventFactory eventFactory = EventFactory.getInstance();
        ChunkServerRegistration csr = (ChunkServerRegistration) eventFactory.createEvent(Protocol.CHUNKSERVER_REGISTRATION);
        long freespace = new File("/tmp").getFreeSpace();
        csr.setFreeSpace(freespace);
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
                        Heartbeat heartbeat = new Heartbeat(getChunkServer());
                        Timer timer = new Timer();
                        timer.schedule(heartbeat, Protocol.MINOR_HEARTBEAT, Protocol.MINOR_HEARTBEAT);
                        System.out.println("[INFO] heartbeat started");
                    } else {
                        System.err.println("[ERROR] Registration UNSUCCESSFULL.");
                    }
                    break;

                case Protocol.CONTROLLER_PROBE:
                    ControllerProbe cp = new ControllerProbe(data);
                    int pNodeID = cp.getNodeID();
                    if (this.nodeID == pNodeID) {
                        ChunkServerProbe chunkServerProbe = (ChunkServerProbe) eventFactory.createEvent(Protocol.CHUNKSERVER_PROBE);
                        chunkServerProbe.setNodeID(this.nodeID);
                        this.conn.getSender().sendData(chunkServerProbe.getByte());
                    }
                    break;

                case Protocol.CLIENT_WRITE:
                    ClientWrite clientWrite = new ClientWrite(data);
                    int clientID = clientWrite.getClientID();
                    String ChunkName = new String(clientWrite.getChunkName());
                    byte[] chunkData = clientWrite.getData();

//                      System.out.println("[INFO] received data from client: " + clientID);
//                    System.out.print("1");
                    System.out.println("-chunk-" + ChunkName + " -copy- 1");
                    int n4 = getChunkNumber(ChunkName);

                    saveFile(ChunkName, chunkData);

                    ChunkServerChunkServer1 cscs = (ChunkServerChunkServer1) eventFactory.createEvent(Protocol.CHUNKSERVER_CHUNKSERVER1);
                    cscs.setClientID(clientID);
                    cscs.setChunkName(ChunkName.getBytes());
                    cscs.setData(chunkData);
                    cscs.setNode1(clientWrite.getNode2());
                    cscs.setIp1(clientWrite.getIp2());
                    cscs.setPort1(clientWrite.getPort2());
//                      System.out.println("-chunk" + new String(chunkData));
                    int port = clientWrite.getPort1();
                    String hostAddr = new String(clientWrite.getIp1());
                    InetAddress ip = InetAddress.getByName(hostAddr);
                    TCPConnection conn1 = establishConnection(ip, port);

                    try {
                        conn1.getSender().sendData(cscs.getByte());
                    } catch (Exception ex) {
                        System.err.println("[ERROR] while writing from cs1_to_cs2");
                    }

                    break;

                case Protocol.CHUNKSERVER_CHUNKSERVER1:
                    ChunkServerChunkServer1 cscs1 = new ChunkServerChunkServer1(data);
                    int clientID1 = cscs1.getClientID();
                    String ChunkName1 = new String(cscs1.getChunkName());
                    byte[] chunkData1 = cscs1.getData();

//                    System.out.print("2");
                    System.out.println("-chunk-" + ChunkName1 + " -copy- 2");
                    int n2 = getChunkNumber(ChunkName1);
                    saveFile(ChunkName1, chunkData1);

                    ChunkServerChunkServer2 cscs2 = (ChunkServerChunkServer2) eventFactory.createEvent(Protocol.CHUNKSERVER_CHUNKSERVER2);
                    cscs2.setClientID(clientID1);
                    cscs2.setChunkName(ChunkName1.getBytes());
                    cscs2.setData(chunkData1);

                    int port1 = cscs1.getPort1();
                    String hostAddr1 = new String(cscs1.getIp1());
                    InetAddress ip1 = InetAddress.getByName(hostAddr1);
                    TCPConnection conn2 = establishConnection(ip1, port1);

                    try {
                        conn2.getSender().sendData(cscs2.getByte());
                    } catch (Exception ex) {
                        System.err.println("[ERROR] while writing from cs2_to_cs3");
                    }

                    break;

                case Protocol.CHUNKSERVER_CHUNKSERVER2:
                    ChunkServerChunkServer2 cscs3 = new ChunkServerChunkServer2(data);
                    int clientID2 = cscs3.getClientID();
                    String ChunkName2 = new String(cscs3.getChunkName());
                    byte[] chunkData2 = cscs3.getData();

//                    System.out.print("3");
                    System.out.println("-chunk-" + ChunkName2 + " -copy- 3");
                    int n1 = getChunkNumber(ChunkName2);
                    saveFile(ChunkName2, chunkData2);

                    break;

                case Protocol.CLIENT_FETCH_CHUNK:
                    System.out.println("[INFO] Fetch request received");
                    ClientFetchChunk cfc = new ClientFetchChunk(data);
                    byte[] clientIPaddr = cfc.getIp();
                    int clientPort = cfc.getPort();
                    int clientreq = cfc.getClientID();
                    byte[] cname = cfc.getChunkName();

                    sendToClientORReportError(clientIPaddr, clientPort, clientreq, cname);

                    break;

                case Protocol.CONTROLLER_HEAL_CHUNKSERVER:
                    System.out.println("-data healing request from controller-");
                    ControllerHealChunkServer healServer = new ControllerHealChunkServer(data);
//                    System.out.println("-");
                    int clientIDH = healServer.getClientID();
                    byte[] clientIPH = healServer.getClientIP();
                    int clientPortH = healServer.getClientPort();
                    byte[] corruptedChunkName = healServer.getChunkName();
                    byte[] tmpSlc = healServer.getCorruptSlices();
                    String[] slices = new String(tmpSlc).split(",");
                    System.out.println("slices " + new String(tmpSlc));
                    int corruptedNode = healServer.getCorruptedNodeID();
//                    System.out.println("&");
                    byte[] corruptedNodeIP = healServer.getCorruptedNodeIP();
//                    System.out.println("-corrupt ip" + new String(corruptedNodeIP));
                    int corruptedNodePort = healServer.getCorruptedNodePort();
//                    System.out.println("-client: " + clientIPH + ":" + clientPortH);
                    if (healServer.isResult()) {
                        sendToClientORReportError(clientIPH, clientPortH, clientIDH, corruptedChunkName);
                    }
                    System.out.println("-healing-");
                    //healing
                    boolean corrupt;
                    int[] slice = new int[slices.length];
                    for (int i = 0; i < slices.length; i++) {
                        slice[i] = Integer.parseInt(slices[i]);
                    }
                    String chunkToSend = "/tmp/cs555_priyankb" + new String(corruptedChunkName);
//                    System.out.println("-chunk to send-" + chunkToSend);
                    File cts = new File(chunkToSend);
                    if (!cts.exists()) {
                        System.out.println("-file not found-");
                        ChunkServerDataCorrupt corrupt2 = (ChunkServerDataCorrupt) eventFactory.createEvent(Protocol.CHUNKSERVER_DATA_CORRUPT);
                        corrupt2.setNodeID(corruptedNode);
                        corrupt2.setResult(false);
                        corrupt2.setClientID(clientIDH);
                        corrupt2.setChunkName(corruptedChunkName);
//                        System.out.println("-corrupted slices " + corruptedSlices);
                        corrupt2.setCorruptSlices(tmpSlc);
                        try {
//                            System.out.println("@@@");
                            this.conn.getSender().sendData(corrupt2.getByte());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    int filelen = (int) cts.length();

                    Map<Integer, byte[]> chunkSlices = new HashMap<>();
                    RandomAccessFile fin = new RandomAccessFile(cts, "r");

                    byte[] content = new byte[(int) cts.length()];
                    fin.read(content);

                    MetaData md;
                    synchronized (metaDataCache) {
                        md = metaDataCache.get(new String(corruptedChunkName));
                    }
                    String storedShecksum = md.getChecksum();
//                    System.out.println("-checksum-" + storedShecksum);
                    MetaDataComputer mdc = new MetaDataComputer(content);

                    try {
                        mdc.computeMetadata();
                    } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    String computedChecksum = mdc.getChecksum();
                    if (computedChecksum.equals(storedShecksum)) {

                        System.out.println("-file length " + filelen);
                        int maxSliceSize = Protocol.SLICE_SIZE;
                        System.out.println("-slice len " + slice.length);
                        System.out.println("-slices len " + slices.length);
                        System.out.println("loop" + (Math.ceil(filelen *1.0 / maxSliceSize)));
                        int numSlices = (int) ((slice.length == 8) ? Math.ceil(filelen * 1.0/ maxSliceSize) : slice.length);
                        System.out.println("-num slices " + numSlices);
                        for (int i = 0; i < numSlices; i++) {
                            int sliceSize = ((int) (filelen) > ((i + 1) * maxSliceSize) ? maxSliceSize : (((int) filelen) - (i * maxSliceSize)));
                            System.out.println("-slice size " + sliceSize);
//                            if (sliceSize <= 0) {
////                                sliceSize = filelen;
//                            }
                            byte[] sliceData = new byte[sliceSize];
                            fin.seek((slice[i]) * maxSliceSize);
                            fin.readFully(sliceData, 0, sliceSize);
//                            fin.re
//                        System.out.println("-slicedata "+ new String(sliceData));
                            System.out.println("-i- " + i);
                            synchronized (chunkSlices) {
                                chunkSlices.put(Integer.parseInt(slices[i]), sliceData);
//                                System.out.println("--");
                            }

                        }
//                    System.out.println("-chunkslices\n" + chunkSlices);
//                    System.out.println("-corrupted node- " + corruptedNodeIP + ":" + corruptedNodePort);
                        Socket clientSocket = new Socket(new String(corruptedNodeIP), corruptedNodePort);
                        TCPSender cnconn = new TCPSender(clientSocket);

                        ChunkServerHealChunkServer healChunkServer = (ChunkServerHealChunkServer) eventFactory.createEvent(Protocol.CHUNKSERVER_HEAL_CHUNKSERVER);
                        healChunkServer.setChunkName(corruptedChunkName);
                        healChunkServer.setSliceCache(chunkSlices);

                        try {
                            cnconn.sendData(healChunkServer.getByte());
                        } catch (Exception ex) {
                            Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        ChunkServerDataCorrupt corrupt2 = (ChunkServerDataCorrupt) eventFactory.createEvent(Protocol.CHUNKSERVER_DATA_CORRUPT);
                        corrupt2.setNodeID(corruptedNode);
                        corrupt2.setResult(false);
                        corrupt2.setClientID(clientIDH);
                        corrupt2.setChunkName(corruptedChunkName);
//                        System.out.println("-corrupted slices " + corruptedSlices);
                        corrupt2.setCorruptSlices(tmpSlc);
                        try {
//                            System.out.println("@@@");
                            this.conn.getSender().sendData(corrupt2.getByte());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;

                case Protocol.CHUNKSERVER_HEAL_CHUNKSERVER:
                    System.out.println("-data heal request from chunk server-");
                    ChunkServerHealChunkServer cshcs = new ChunkServerHealChunkServer(data);
                    String chunkName = "/tmp/cs555_priyankb" + new String(cshcs.getChunkName());
                    Map<Integer, byte[]> sliceCache = cshcs.getSliceCache();
//cshcs.get
                    String path = chunkName.substring(0, chunkName.lastIndexOf("/") + 1);

                    File newFile = new File(path);
                    if (!newFile.exists()) {
                        newFile.mkdirs();
                    }

                    File healFile = new File(chunkName);
                    RandomAccessFile hfin = new RandomAccessFile(healFile, "rw");
                    int maxSliceSize1 = Protocol.SLICE_SIZE;
                    synchronized (sliceCache) {
                        int size = sliceCache.size();
//                    for (int i = 0; i < size; i++) {
////                        int sliceSize = ((int) (cts.length()) > ((i + 1) * maxSliceSize) ? maxSliceSize : (((int) cts.length()) - (i * maxSliceSize)));
//                        int sliceSize = maxSliceSize1;
//                        if(i==size-1){
//                            
//                        }
//                        byte[] sliceData = new byte[sliceSize];
//                        fin.read(sliceData, (i * maxSliceSize), sliceSize);
//                        synchronized (chunkSlices) {
//                            chunkSlices.put(i, sliceData);
//                        }
//                    }

                        for (Map.Entry<Integer, byte[]> entrySet : sliceCache.entrySet()) {
                            Integer sliceNum = entrySet.getKey();
                            byte[] sliceData = entrySet.getValue();
                            hfin.seek(sliceNum * maxSliceSize1);
                            System.out.println("-slicedata " + new String(sliceData));
                            hfin.write(sliceData);

                        }
                    }

                    break;

                case Protocol.CONTROLLER_TRANSFER_CHUNKSERVER:
                    System.out.println("[INFO] NEW FILE CHUNK");
                    ControllerTransferChunkServer ctcs = new ControllerTransferChunkServer(data);
                    String ipT = new String(ctcs.getIp());
                    int portT = ctcs.getPort();
                    Socket socT = new Socket(ipT, portT);
                    TCPConnection connT = new TCPConnection(socT, this);

                    byte[] chunkNameT = ctcs.getChunkName();
                    int nodeIDT = ctcs.getNodeID();

                    ChunkServerChunkServerTransfer cscst = (ChunkServerChunkServerTransfer) eventFactory.createEvent(Protocol.CHUNKSERVER_CHUNKSERVER_TRANSFER);
                    cscst.setChunkName(chunkNameT);
                    cscst.setNodeID(nodeIDT);

                    String chunkToTransfer = "/tmp/cs555_priyankb" + new String(chunkNameT);
                    File ctt = new File(chunkToTransfer);
                    if (!ctt.exists()) {
                        break;
                    }
                    byte[] chunkT = new byte[(int) ctt.length()];
                    RandomAccessFile finT = new RandomAccessFile(ctt, "r");
                    finT.read(chunkT);
                    finT.close();
                    cscst.setData(chunkT);
                    connT.getSender().sendData(cscst.getByte());

                    break;

                case Protocol.CHUNKSERVER_CHUNKSERVER_TRANSFER:
                    System.out.println("[INFO] Transfer request received from another chunkserver");
                    ChunkServerChunkServerTransfer transfer = new ChunkServerChunkServerTransfer(data);
                    int myID = transfer.getNodeID();
                    String chunkNametr = new String(transfer.getChunkName());
                    byte[] chunkDatatr = transfer.getData();
                    int n = getChunkNumber(chunkNametr);
                    saveFile(chunkNametr, chunkDatatr);
                    break;

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void saveFile(String chunkName, byte[] chunkData) {
        FileSaver fs = new FileSaver(chunkName, chunkData);
        Thread fileSaverThread = new Thread(fs);
        fileSaverThread.start();
    }

    public void resetNewMetaDataCache() {
        synchronized (newMetaDataCache) {
            newMetaDataCache.clear();
        }
    }

    private void sendToClientORReportError(byte[] clientIPaddr, int clientPort, int clientreq, byte[] cname) {
        try {
            EventFactory eventFactory = EventFactory.getInstance();
            Socket clientSocket = new Socket(new String(clientIPaddr), clientPort);
//            TCPConnection clientConn = new TCPConnection(clientSocket, this);
            TCPSender sender = new TCPSender(clientSocket);

            String chunkToSend = "/tmp/cs555_priyankb" + new String(cname);
//            System.out.println("-chunkname- " + chunkToSend);
            File cts = new File(chunkToSend);
            if (cts.exists()) {
//                FileInputStream fin = new FileInputStream(cts);
                RandomAccessFile fin = new RandomAccessFile(cts, "rw");
                byte[] content = new byte[(int) cts.length()];
                fin.read(content);

                MetaData md;
                synchronized (metaDataCache) {
                    md = metaDataCache.get(new String(cname));
                }
                String storedShecksum = md.getChecksum();
//                    System.out.println("-checksum-" + storedShecksum);
                MetaDataComputer mdc = new MetaDataComputer(content);

                try {
                    mdc.computeMetadata();
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
                }

                String computedChecksum = mdc.getChecksum();
//                    System.out.print("+");
                if (computedChecksum.equals(storedShecksum)) {
                    System.out.println("-sending-");
                    ChunkServerSendChunk cssc = (ChunkServerSendChunk) eventFactory.createEvent(Protocol.CHUNKSERVER_SENDCHUNK);
                    cssc.setChunkName(chunkToSend.getBytes());
                    cssc.setData(content);
                    cssc.setClientID(clientreq);
                    try {
                        sender.sendData(cssc.getByte());
//                        clientConn.getSender().sendData(cssc.getByte());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("-tampering detected-");
                    //Healing
                    String[] splittedComputedChecksum = computedChecksum.split("\n");
                    String[] splittedStoredChecksum = storedShecksum.split("\n");

                    int comLen = splittedComputedChecksum.length;
                    int stoLen = splittedStoredChecksum.length;

                    System.out.println("-storedlength " + stoLen);
                    System.out.println("-computedlength " + comLen);

                    String corruptedSlices = "";
                    if (stoLen >= comLen) {
//                        System.out.print("@");
                        int i;
                        for (i = 0; i < comLen; i++) {
                            if (!splittedStoredChecksum[i].equals(splittedComputedChecksum[i])) {
                                corruptedSlices += i;
                                corruptedSlices += ",";
                            }
                        }
                        for (int j = i; j < stoLen; j++) {
                            corruptedSlices += i;
                            corruptedSlices += ",";
                        }

                    } else if (stoLen < comLen) {
//                        System.out.print("#");
                        for (int i = 0; i < stoLen; i++) {
                            if (!splittedStoredChecksum[i].equals(splittedComputedChecksum[i])) {
                                corruptedSlices += i;
                                corruptedSlices += ",";
                            }
                        }
                        //delete extra slice
//                        System.out.print("*");
                        fin.setLength(0);
                        fin.write(content, 0, stoLen * Protocol.SLICE_SIZE);

                    }
//                    System.out.print("-");
                    if (corruptedSlices.equals("")) {
                        //send chunkto client
//                        ChunkServerSendChunk cssc = (ChunkServerSendChunk) eventFactory.createEvent(Protocol.CHUNKSERVER_SENDCHUNK);
//                        cssc.setChunkName(chunkToSend.getBytes());
//                        cssc.setData(content);
//                        cssc.setClientID(clientreq);
//                        try {
//                            sender.sendData(cssc.getByte());
////                            clientConn.getSender().sendData(cssc.getByte());
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
                    } else {
                        //send corrupt to controller
                        ChunkServerDataCorrupt corrupt = (ChunkServerDataCorrupt) eventFactory.createEvent(Protocol.CHUNKSERVER_DATA_CORRUPT);
                        corrupt.setNodeID(this.nodeID);
                        corrupt.setResult(true);
                        corrupt.setClientID(clientreq);
                        corrupt.setChunkName(cname);
//                        System.out.println("-corrupted slices " + corruptedSlices);
                        corrupt.setCorruptSlices(corruptedSlices.getBytes());
                        try {
//                            System.out.println("@@@");
                            this.conn.getSender().sendData(corrupt.getByte());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                }

                //deleted chunk
            } else {
                ChunkServerDataCorrupt corrupt = (ChunkServerDataCorrupt) eventFactory.createEvent(Protocol.CHUNKSERVER_DATA_CORRUPT);
                corrupt.setNodeID(this.nodeID);
                corrupt.setResult(true);
                corrupt.setClientID(clientreq);
                corrupt.setChunkName(cname);
                int nslices = Protocol.CHUNK_SIZE / Protocol.SLICE_SIZE;
                String corruptedSlices = "";
                for (int i = 0; i < nslices; i++) {
                    corruptedSlices += i;
                    corruptedSlices += ",";
                }
                corrupt.setCorruptSlices(corruptedSlices.getBytes());
                try {
                    this.conn.getSender().sendData(corrupt.getByte());
                } catch (Exception ex) {
                    Logger.getLogger(ChunkServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            System.out.println("-came out of loop-");
        } catch (IOException ex) {
            System.err.println("ERROR : ");
            ex.printStackTrace();
        }
    }

    private int getChunkNumber(String s) {
        StringBuffer buff = new StringBuffer(s);
        String lastToken = buff.substring(buff.lastIndexOf("k") + 1);
        int number = Integer.parseInt(lastToken);
        return number;
    }

    class FileSaver implements Runnable {

        private String chunkName;
        private byte[] chunkData;

        private FileSaver(String chunkName, byte[] chunkData) {
            this.chunkName = chunkName;
            this.chunkData = chunkData;
        }

        @Override
        public void run() {
            try {
                String fn = "/tmp/cs555_priyankb" + chunkName;
                String path = fn.substring(0, fn.lastIndexOf("/") + 1);
                String actualfile = fn.replaceFirst("/tmp/cs555_priyankb", "");

                File newFile = new File(path);
                if (!newFile.exists()) {
                    newFile.mkdirs();
                }

                File actualFile = new File(fn);

                FileOutputStream fout = new FileOutputStream(actualFile);
//                fout.write(chunkData, 0, Protocol.CHUNK_SIZE);
                fout.write(chunkData, 0, chunkData.length);

//                int n = getChunkNumber(fn);
                MetaDataComputer md = new MetaDataComputer(chunkData);
                byte[] computedMetaData = md.computeMetadata();
                String fn_m = fn + ".metadata";

                MetaData m = new MetaData(md.getVersion(), md.getTimestamp(), md.getChecksum());
                synchronized (metaDataCache) {
                    metaDataCache.put(actualfile, m);
//                    System.out.println("-metadata-" + metaDataCache);
                }
                synchronized (newMetaDataCache) {
                    newMetaDataCache.put(actualfile, m);
                }

                File metaFile = new File(fn_m);
                FileOutputStream fmout = new FileOutputStream(metaFile);
                fmout.write(computedMetaData, 0, computedMetaData.length);

            } catch (IOException ex) {
//                System.err.println("[ERROR] FILE SAVE");
            } catch (NoSuchAlgorithmException ex) {
//                System.err.println("[ERROR] Can not find Hash Algorithm");
            }
        }

    }

    private TCPConnection establishConnection(InetAddress ip, int port) throws IOException {
//        System.out.println("[INFO] Trying to connect to "+ip+":"+port);
        Socket sc = new Socket(ip.getHostAddress(), port);
        TCPConnection tconn = new TCPConnection(sc, this);
        return tconn;
    }
}
