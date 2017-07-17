package cs555RS.nodes;

import cs555RS.transport.TCPConnection;
import cs555RS.transport.TCPSender;
import cs555RS.util.Protocol;
import cs555RS.wireformats.ChunkServerMajorHeartbeat;
import cs555RS.wireformats.ChunkServerMinorHeartbeat;
import cs555RS.wireformats.ChunkServerRegistration;
import cs555RS.wireformats.ClientChunkName;
import cs555RS.wireformats.ClientRead;
import cs555RS.wireformats.ClientRegistration;
import cs555RS.wireformats.ControllerClientChunkservers;
import cs555RS.wireformats.ControllerClientRegistrationStatus;
import cs555RS.wireformats.ControllerReadReply;
import cs555RS.wireformats.ControllerRegistrationStatus;
import cs555RS.wireformats.EventFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller extends Thread implements Node {

    private static Controller controller;

    public Map<String, TCPConnection> tempCache = new HashMap();
    public Map<Integer, TCPConnection> connectionCache = new HashMap();
    private AtomicInteger numNodes = new AtomicInteger(0);
    public Map<Integer, Integer> listeningPortCache = new HashMap<>();
    private AtomicInteger numChunkSrvrs = new AtomicInteger(0);
    Set<Integer> chunkServers = new HashSet<>();

    Map<String, Map<Integer, Map<Integer, Integer>>> dataStructure = new HashMap<>();

    public static Controller getController() {
        return controller;
    }

    private Controller(int port) throws IOException {
        ServerSocket controllerSocket = new ServerSocket(port);
        System.out.println("[INFO] Controller Started.");
        while (true) {
            Socket s = controllerSocket.accept();
            TCPConnection conn = new TCPConnection(s, this);
            addtoTempCache(conn);
        }
    }

    public static void main(String[] args)
            throws IOException {
        int port = Protocol.PORT;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        controller = new Controller(port);
    }

    public void onEvent(byte[] data, Socket s) throws IOException {
        EventFactory eventFactory = EventFactory.getInstance();
        switch (data[0]) {

            case Protocol.CHUNKSERVER_REGISTRATION:
//                ServerReg reg = new ServerReg(data, s);
//                Thread serverRegThread = new Thread(reg);
//                serverRegThread.start();

                ChunkServerRegistration registration = new ChunkServerRegistration(data);
//                int nodeID = addToDepository(registration.getIp(), registration.getPort(), s);
                int nodeID = numNodes.incrementAndGet();
                numChunkSrvrs.incrementAndGet();
                int listeningPort = registration.getPort();
                synchronized (chunkServers) {
                    chunkServers.add(nodeID);
                }
                System.out.println("[INFO] Chunk Server with id: " + nodeID + " connected");
                addConnectiontoCache(nodeID, s);
                ControllerRegistrationStatus crs = (ControllerRegistrationStatus) eventFactory.createEvent(Protocol.CONTROLLER_REGISTRATION_STATUS);
                crs.setNodeID(nodeID);
                crs.setInfo("Registration Request Succesfull");
                TCPConnection conn;
                synchronized (connectionCache) {
                    conn = (TCPConnection) connectionCache.get(nodeID);

                }
//                System.out.println("-conn " + conn);
                conn.getSender().sendData(crs.getByte());

                synchronized (listeningPortCache) {
                    listeningPortCache.put(nodeID, listeningPort);
                }

                break;

            case Protocol.CHUNKSERVER_MINOR_HEARTBEAT:
                ChunkServerMinorHeartbeat minor = new ChunkServerMinorHeartbeat(data);
                int minorNodeID = minor.getNodeID();
//                Map<String, MetaData> metaDataCache = minor.getMetaDataCache();

                break;

            case Protocol.CHUNKSERVER_MAJOR_HEARTBEAT:
                ChunkServerMajorHeartbeat major = new ChunkServerMajorHeartbeat(data);
                int majorNodeID = major.getNodeID();

                break;

            case Protocol.CLIENT_REGISTRATION:
                ClientRegistration cr = new ClientRegistration(data);
//                int cNodeID = addToDepository(cr.getIp(), cr.getPort(), s);
                int cNodeID = numNodes.incrementAndGet();
                int cListeningPort = cr.getPort();
                System.out.println("[INFO] Client with id: " + cNodeID + " connected");
                if (cNodeID >= 0) {
                    addConnectiontoCache(cNodeID, s);
                }
                synchronized (listeningPortCache) {
                    listeningPortCache.put(cNodeID, cListeningPort);
                }
                ControllerClientRegistrationStatus ccrs = (ControllerClientRegistrationStatus) eventFactory.createEvent(Protocol.CONTROLLER_CLIENT_REGISTRATION_STATUS);
                ccrs.setNodeID(cNodeID);
                ccrs.setInfo("Registration Request Succesfull");
                TCPConnection cconn;

//                cconn = getfromTempCache(s);
                TCPSender sender = new TCPSender(s);
                sender.sendData(ccrs.getByte());
                break;

            case Protocol.CLIENT_CHUNK_NAME:
                System.out.println("[INFO] Client store request recieved");
                ClientChunkName ccn = new ClientChunkName(data);
                int clientNodeID = ccn.getNodeID();
                byte[] cn = ccn.getData();
                String ChunkName = new String(cn);
//                System.out.println("-chunkName-: "+ChunkName);
                int[] nodes = determineCS();
//                System.out.println("-nodes- " + nodes[0] + " "+ nodes[1] + " "+ nodes[2] + " "+ nodes[3] + " "+ nodes[4] + " "+ nodes[5] + " "+ nodes[6] + " "+ nodes[7] + " "+ nodes[8] + " ");
                addToStructure(ChunkName, nodes);
//                System.out.println("-nodes: " + nodes[0]+" "+nodes[1]+" "+nodes[2]);
                ControllerClientChunkservers ccc = (ControllerClientChunkservers) eventFactory.createEvent(Protocol.CONTROLLER_CLIENT_CHUNKSERVERS);
                ccc.setNodeID(clientNodeID);
                ccc.setChunkName(cn);

                TCPConnection tconn;
                Map<Integer, Map<Integer, String>> nodeAddr = new HashMap<>();
                for (int i = 0; i < 9; i++) {
                    Map<Integer, String> nodeMap = new HashMap<>();
                    synchronized (connectionCache) {
                        tconn = (TCPConnection) this.connectionCache.get(nodes[i]);
                    }
                    String ip = tconn.getSocket().getInetAddress().getHostAddress();
                    int port;
                    synchronized (listeningPortCache) {
                        port = listeningPortCache.get(nodes[i]);
                    }
                    String ipport = ip + ":" + port;
                    nodeMap.put(nodes[i], ipport);
                    nodeAddr.put(i + 1, nodeMap);
                }
                ccc.setChunkServers(nodeAddr);
//                System.out.println("-nodeAddr-" + nodeAddr);
//                System.out.println("-ip: " + new String(ns[0].getIp()) + ":" + ns[0].getPort());
                TCPSender sender1 = new TCPSender(s);

                try {

                    sender1.sendData(ccc.getByte());
                } catch (Exception ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;

            case Protocol.CLIENT_READ:
                ClientRead cread = new ClientRead(data);
                System.out.println("[INFO] Read Request Received");
//                System.out.println("-datastructure- \n" + dataStructure);
                int clientread = cread.getClientID();
                String requestedFileName = new String(cread.getFileName());
                Map<Integer, Map<Integer, String>> finalMapToSendToClient = new HashMap<>();

                synchronized (dataStructure) {
//                    System.out.println("-datastructure-");
                    Set<Integer> chunks = dataStructure.get(requestedFileName).keySet();
                    Map<Integer, Map<Integer, Integer>> chunkCache = dataStructure.get(requestedFileName);
                    for (Integer chunkNo : chunks) {
                        Map<Integer, Integer> shardCache = chunkCache.get(chunkNo);
                        Map<Integer, String> shardLoc = new HashMap<>();
                        for (Map.Entry<Integer, Integer> entrySet : shardCache.entrySet()) {
                            int shardNo = entrySet.getKey();
                            int destID = entrySet.getValue();
                            TCPConnection tconn1;
                            synchronized (connectionCache) {
                                tconn1 = (TCPConnection) this.connectionCache.get(destID);
                            }
                            byte[] ip1 = tconn1.getSocket().getInetAddress().getHostName().getBytes();
                            int port1;
                            synchronized (listeningPortCache) {
                                port1 = listeningPortCache.get(destID);
                            }
                            String ipport = new String(ip1) + ":" + port1;
                            shardLoc.put(shardNo, ipport);
                        }
                        finalMapToSendToClient.put(chunkNo, shardLoc);
                    }
                }

                ControllerReadReply crr = (ControllerReadReply) eventFactory.createEvent(Protocol.CONTROLLER_READ_REPLY);
                crr.setClientID(clientread);
                crr.setFileName(requestedFileName.getBytes());
                crr.setFinalMapToSendToClient(finalMapToSendToClient);
                TCPSender senderr = new TCPSender(s);

                try {
                    senderr.sendData(crr.getByte());
                } catch (Exception ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;

        }

    }

    private void addConnectiontoCache(int nodeId, Socket s) {
        TCPConnection tempObj = getfromTempCache(s);
        if (tempObj != null) {
            synchronized (connectionCache) {
                this.connectionCache.put(nodeId, tempObj);
            }
        }
    }

    private void addtoTempCache(TCPConnection connection) {
        int idbyPort = connection.getSocket().getPort();
        String idbyIP = connection.getSocket().getInetAddress().getHostAddress();
        String id = idbyIP + ":" + idbyPort;
//        System.out.println("-ret- " + id);
        synchronized (tempCache) {
            this.tempCache.put(id, connection);
//            System.out.println("-temp cache-" + tempCache);
        }
    }

    private TCPConnection getfromTempCache(Socket s) {
        TCPConnection tempObj;
        synchronized (tempCache) {
            int idbyPort = s.getPort();
            String idbyIP = s.getInetAddress().getHostAddress();
            String id = idbyIP + ":" + idbyPort;
            tempObj = (TCPConnection) this.tempCache.get(id);
        }
        return tempObj;
    }

    private int[] determineCS() {
        int[] nodes = new int[9];
        int numchnksrvrs;
        synchronized (numChunkSrvrs) {
            numchnksrvrs = numChunkSrvrs.get();
        }
        if (numchnksrvrs > 9) {
            for (int i = 0; i < 9; i++) {
                nodes[i] = randomNodeID(numchnksrvrs);
            }
        } else {
            synchronized (chunkServers) {
                List<Integer> temp = new ArrayList<>(chunkServers);
                int[] tmp = new int[temp.size()];
                for (int j = 0; j < temp.size(); j++) {
                    tmp[j] = temp.get(j);
                }
                int i = 0;
                int size = tmp.length;
                for (int j = 0; j < 9; j++) {
                    nodes[j] = tmp[i];
                    i++;
                    if (i >= size) {
                        i = 0;
                    }
                }
            }
        }
        return nodes;
    }

    private int getChunkNumber(String s) {
        StringBuffer buff = new StringBuffer(s);
        String lastToken = buff.substring(buff.lastIndexOf("k") + 1);
        int number = Integer.parseInt(lastToken);
        return number;
    }

    private void addToStructure(String shardName, int[] node) {
        AddToDataStructure as;
        as = new AddToDataStructure(shardName, node);
        Thread t = new Thread(as);
        t.start();
    }

    private int randomNodeID(int numchnksrvrs) {
        int item = new Random().nextInt(numchnksrvrs);
        int j = 0;
        synchronized (chunkServers) {
            for (Integer nodeID : chunkServers) {
                if (j == item) {
                    return nodeID;
                }
                j = j + 1;
            }

        }
        return -1;
    }

    private class AddToDataStructure implements Runnable {

        String fileName;
        int chunkNum;
        int[] nodes;

        private AddToDataStructure(String shardName, int[] nodes) {
            this.fileName = shardName.substring(0, shardName.lastIndexOf("_"));
            this.chunkNum = getChunkNumber(shardName);
            this.nodes = nodes;
        }

        @Override
        public void run() {
            synchronized (dataStructure) {
                if (!dataStructure.containsKey(fileName)) {
                    dataStructure.put(fileName, new HashMap<>());
                }
                Map<Integer, Map<Integer, Integer>> t1 = dataStructure.get(fileName);
                if (t1 == null) {
                    t1 = new HashMap<>();
                }
                t1.put(chunkNum, new HashMap<>());

                Map<Integer, Integer> t2 = t1.get(chunkNum);
                if (t2 == null) {
                    t2 = new HashMap<>();
                }
                for (int i = 0; i < 9; i++) {
                    t2.put(i + 1, nodes[i]);
                }
            }
        }

    }
}
