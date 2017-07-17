package cs555.nodes;

import cs555.transport.TCPConnection;
import cs555.util.MetaData;
import cs555.util.ProbeTimer;
import cs555.util.ProbeTimer2;
import cs555.util.Protocol;
import cs555.util.nodeSocket;
import cs555.wireformats.ChunkServerDataCorrupt;
import cs555.wireformats.ChunkServerMajorHeartbeat;
import cs555.wireformats.ChunkServerMinorHeartbeat;
import cs555.wireformats.ChunkServerProbe;
import cs555.wireformats.ChunkServerRegistration;
import cs555.wireformats.ClientChunkName;
import cs555.wireformats.ClientOverwrite;
import cs555.wireformats.ClientRead;
import cs555.wireformats.ClientRegistration;
import cs555.wireformats.ControllerClientChunkservers;
import cs555.wireformats.ControllerClientRegistrationStatus;
import cs555.wireformats.ControllerHealChunkServer;
import cs555.wireformats.ControllerOverwrite;
import cs555.wireformats.ControllerReadReply;
import cs555.wireformats.ControllerRegistrationStatus;
import cs555.wireformats.ControllerTransferChunkServer;
import cs555.wireformats.EventFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller
        extends Thread
        implements Node {

    private static Controller controller;
    public Map<String, TCPConnection> tempCache = new HashMap();
    public Map<Integer, TCPConnection> connectionCache = new HashMap();
    public Map<Integer, Long> timerCache = new HashMap();
    public Map<Integer, Long> timerCache2 = new HashMap();
    private AtomicInteger numNodes = new AtomicInteger(0);
    private static int previosNode = 0;
    public Map<Integer, Integer> listeningPortCache = new HashMap<>();
    List<SpaceCache> spaceCache = new ArrayList();

    private Map<String, Map<Integer, Map<Integer, MetaData>>> dataStructure = new HashMap<>();

    public static Controller getController() {
        return controller;
    }

    private Controller() throws IOException {

    }

    public static void main(String[] args)
            throws IOException {
        int port = Protocol.PORT;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        controller = new Controller();
        controller.startTimer();
        controller.startTimer2();
        controller.start2(port);
    }

    void start2(int port) throws IOException {
        ServerSocket controllerSocket = new ServerSocket(port);
        System.out.println("[INFO] Controller Started.");
        while (true) {
            Socket s = controllerSocket.accept();
            TCPConnection conn = new TCPConnection(s, this);
            addtoTempCache(conn);
        }
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
                long freeSpace = registration.getFreeSpace();
                int listeningPort = registration.getPort();
                System.out.println("[INFO] Chunk Server with id: " + nodeID + " connected");
                if (nodeID >= 0) {
                    addConnectiontoCache(nodeID, s);
                }
                ControllerRegistrationStatus crs = (ControllerRegistrationStatus) eventFactory.createEvent(Protocol.CONTROLLER_REGISTRATION_STATUS);
                crs.setNodeID(nodeID);
                crs.setInfo("Registration Request Succesfull");
                TCPConnection conn;
                synchronized (connectionCache) {
                    conn = (TCPConnection) connectionCache.get(nodeID);
//                    conn = new TCPConnection(s, this);
//                    InetAddress newIP = conn.getSocket().getInetAddress();
//                    Socket sc = new Socket(newIP, listeningPort);
//                    conn.getSocket().close();
//                    conn = new TCPConnection(sc, this);
                }
//                System.out.println("-conn " + conn);
                conn.getSender().sendData(crs.getByte());

                synchronized (listeningPortCache) {
                    listeningPortCache.put(nodeID, listeningPort);
                }
                synchronized (timerCache) {
                    timerCache.put(Integer.valueOf(nodeID), Long.valueOf(Protocol.PROBE_TIMEOUT));
                }
                synchronized (spaceCache) {
                    spaceCache.add(new SpaceCache(nodeID, freeSpace));
                }
                break;

            case Protocol.CHUNKSERVER_MINOR_HEARTBEAT:
                ChunkServerMinorHeartbeat minor = new ChunkServerMinorHeartbeat(data);
                int minorNodeID = minor.getNodeID();
//                System.out.println("[INFO] MINOR HEARTBEAT RECEIVED FROM " + minorNodeID);
                Map<String, MetaData> metaDataCache = minor.getMetaDataCache();
                updateDatastructure(metaDataCache, minorNodeID);
                resetTimer(minorNodeID);
                break;

            case Protocol.CHUNKSERVER_MAJOR_HEARTBEAT:
                ChunkServerMajorHeartbeat major = new ChunkServerMajorHeartbeat(data);
                int majorNodeID = major.getNodeID();
//                System.out.println("[INFO] MAJOR HEARTBEAT RECEIVED FROM " + majorNodeID);
                Map<String, MetaData> majormetaDataCache = major.getMetaDataCache();
                updateDatastructure(majormetaDataCache, majorNodeID);
                resetTimer(majorNodeID);
                break;

            case Protocol.CHUNKSERVER_PROBE:
                ChunkServerProbe probe = new ChunkServerProbe(data);
                int pNodeID = probe.getNodeID();
                resetTimer(pNodeID);
                resetTimer2(pNodeID);
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
                synchronized (connectionCache) {
                    cconn = (TCPConnection) this.connectionCache.get(cNodeID);
                }
                cconn.getSender().sendData(ccrs.getByte());
                break;

            case Protocol.CLIENT_OVERWRITE:
                System.out.println("[INFO] FILE REQUEST RECEIVED");
                ClientOverwrite overwrite = new ClientOverwrite(data);
                int cid = overwrite.getClientID();
                String fname = overwrite.getFileName();
                boolean present;
                synchronized (dataStructure) {
                    present = dataStructure.containsKey(fname);
                }
                ControllerOverwrite controllerOverwrite = (ControllerOverwrite) eventFactory.createEvent(Protocol.CONTROLLER_OVERWRITE);
                controllerOverwrite.setClientID(cid);
                controllerOverwrite.setFileName(fname);
                controllerOverwrite.setPresent(present);

                TCPConnection conno;
                synchronized (connectionCache) {
                    conno = (TCPConnection) this.connectionCache.get(cid);
                }
                 {
                    try {
                        conno.getSender().sendData(controllerOverwrite.getByte());
                    } catch (Exception ex) {
                        
                    }
                }

                break;

            case Protocol.CLIENT_CHUNK_NAME:
//                System.out.println("[INFO] Client store request recieved");     
                ClientChunkName ccn = new ClientChunkName(data);
                int clientNodeID = ccn.getNodeID();
                byte[] cn = ccn.getData();
                String ChunkName = new String(cn);
//                System.out.println("-chunkName-: "+ChunkName);

                TCPConnection connccc;
                synchronized (connectionCache) {
                    connccc = (TCPConnection) this.connectionCache.get(Integer.valueOf(clientNodeID));
                }

//                System.out.println("-nodes: " + nodes[0]+" "+nodes[1]+" "+nodes[2]);
                ControllerClientChunkservers ccc = (ControllerClientChunkservers) eventFactory.createEvent(Protocol.CONTROLLER_CLIENT_CHUNKSERVERS);
                ccc.setNodeID(clientNodeID);

                ccc.setChunkName(cn);
                int[] nodes = determineCS(ChunkName);
                ccc.setNode1(nodes[0]);
                ccc.setNode2(nodes[1]);
                ccc.setNode3(nodes[2]);

                nodeSocket[] ns = new nodeSocket[3];

                addToStructure(ChunkName, nodes);

                for (int i = 0; i < 3; i++) {
                    TCPConnection tconn;
                    synchronized (connectionCache) {
                        tconn = (TCPConnection) this.connectionCache.get(Integer.valueOf(nodes[i]));
                    }
                    byte[] ip = tconn.getSocket().getInetAddress().getHostName().getBytes();
                    int port;
                    synchronized (listeningPortCache) {
                        port = listeningPortCache.get(nodes[i]);
                    }
                    ns[i] = new nodeSocket(ip, port);
                }

//                System.out.println("-ip: " + new String(ns[0].getIp()) + ":" + ns[0].getPort());
                ccc.setIp1(ns[0].getIp());
                ccc.setPort1(ns[0].getPort());
                ccc.setIp2(ns[1].getIp());
                ccc.setPort2(ns[1].getPort());
                ccc.setIp3(ns[2].getIp());
                ccc.setPort3(ns[2].getPort());

//                System.out.println("[INFO] ChunkServer Info sent to Client");
                try {
                    connccc.getSender().sendData(ccc.getByte());
                } catch (Exception ex) {
                    System.err.println("[ERROR] Controller Client ChunkServers");
                }
                break;

            case Protocol.CLIENT_READ:
                ClientRead cread = new ClientRead(data);
                System.out.println("[INFO] Read Request Received");
                int clientread = cread.getClientID();
                String requestedFileName = new String(cread.getFileName());
                Map<Integer, String> chunkNode = new HashMap<>();
//                System.out.println("-filename-: " + requestedFileName);
//                System.out.println("-datastructure-: " + dataStructure);
                synchronized (dataStructure) {
                    if (dataStructure.get(requestedFileName).keySet() == null) {
                        break;
                    }
                    Set<Integer> chunks = dataStructure.get(requestedFileName).keySet();
//                    System.out.println("-datastructure-"+dataStructure);
                    for (Iterator<Integer> iterator = chunks.iterator(); iterator.hasNext();) {
                        int chunkNum = iterator.next();
                        Set<Integer> returnedNodes = dataStructure.get(requestedFileName).get(chunkNum).keySet();
                        int[] returnNode = new int[3];
                        int i = 0;
                        for (Iterator<Integer> nodeIterator = returnedNodes.iterator(); nodeIterator.hasNext();) {
                            int returnedNode = nodeIterator.next();
                            returnNode[i] = returnedNode;
                            i++;
                        }
                        String finalNode = returnNode[0] + ":" + returnNode[1] + ":" + returnNode[2];

                        chunkNode.put(chunkNum, finalNode);
                    }
                }
                Map<Integer, String> finalMapToSendToClient = new HashMap<>();
                for (Map.Entry<Integer, String> entrySet : chunkNode.entrySet()) {
                    Integer key = entrySet.getKey();
                    String nodestoSend = entrySet.getValue();

                    String[] nodetosend = nodestoSend.split(":");
                    String finalipport = "";
                    for (String nodetosend2 : nodetosend) {
                        int nodetosend1 = Integer.parseInt(nodetosend2);
                        TCPConnection tconn;
                        synchronized (connectionCache) {
                            tconn = (TCPConnection) this.connectionCache.get(nodetosend1);
                        }
                        String ipport = "";
                        if (tconn != null) {
                            byte[] ip = tconn.getSocket().getInetAddress().getHostAddress().getBytes();
                            int port;
                            synchronized (listeningPortCache) {
                                port = listeningPortCache.get(nodetosend1);
                            }
                            ipport = new String(ip) + ":" + port;
                        }
                        finalipport = finalipport + ipport + "#";
                    }
//                    System.out.println("-finalipport"+finalipport);
                    finalMapToSendToClient.put(key, finalipport);
                }

                ControllerReadReply crr = (ControllerReadReply) eventFactory.createEvent(Protocol.CONTROLLER_READ_REPLY);
                crr.setClientID(clientread);
                crr.setFileName(requestedFileName.getBytes());
                crr.setFinalMapToSendToClient(finalMapToSendToClient);
                TCPConnection conncrr;

                synchronized (connectionCache) {
                    conncrr = (TCPConnection) this.connectionCache.get(Integer.valueOf(clientread));
                }
                InetAddress clientIP = conncrr.getSocket().getInetAddress();
                int clientPort;
                synchronized (listeningPortCache) {
                    clientPort = listeningPortCache.get(clientread);
                }
                Socket socketread = new Socket(clientIP, clientPort);
                conncrr = new TCPConnection(socketread, this);
//                System.out.println("-conn " + conncrr.getSocket().getInetAddress() + ":" + conncrr.getSocket().getPort());

//                System.out.println("[INFO] ChunkServer Info sent to Client");
                try {
                    conncrr.getSender().sendData(crr.getByte());
                } catch (Exception ex) {
                    System.err.println("[ERROR] Controller Client ChunkServers");
                }

                break;

            case Protocol.CHUNKSERVER_DATA_CORRUPT:
                System.out.println("-data corrupt request-");
                ChunkServerDataCorrupt corrupt = new ChunkServerDataCorrupt(data);
                ControllerHealChunkServer heal = (ControllerHealChunkServer) eventFactory.createEvent(Protocol.CONTROLLER_HEAL_CHUNKSERVER);
                int clientw = corrupt.getClientID();
                boolean result = corrupt.isResult();
                heal.setResult(result);
                heal.setClientID(clientw);
                int corruptedNode = corrupt.getNodeID();
                heal.setCorruptedNodeID(corruptedNode);
//                System.out.print("-");
                TCPConnection thealc;
                synchronized (connectionCache) {
                    thealc = connectionCache.get(corruptedNode);
                }
                byte[] ipc = thealc.getSocket().getInetAddress().getHostAddress().getBytes();
                int portc;
//                System.out.print("@");
                synchronized (listeningPortCache) {
                    portc = listeningPortCache.get(corruptedNode);
                }
                heal.setCorruptedNodeIP(ipc);
                heal.setCorruptedNodePort(portc);

                byte[] corruptedChunk = corrupt.getChunkName();
                heal.setChunkName(corruptedChunk);
                heal.setCorruptSlices(corrupt.getCorruptSlices());
                TCPConnection theal;
                synchronized (connectionCache) {
                    theal = connectionCache.get(clientw);
                }
                byte[] ip = theal.getSocket().getInetAddress().getHostAddress().getBytes();
                int port;
//                System.out.print("$");
                synchronized (listeningPortCache) {
                    port = listeningPortCache.get(clientw);
                }
//                System.out.println("-ip-port-" + new String(ip) + ":" + port);
                heal.setClientIP(ip);
                heal.setClientPort(port);

                String corruptedChunknm = new String(corruptedChunk);
                String fileName = corruptedChunknm.substring(0, corruptedChunknm.lastIndexOf("_"));
                Map<Integer, Map<Integer, MetaData>> chunkInfo;
                synchronized (dataStructure) {
                    chunkInfo = dataStructure.get(fileName);
                }
//                System.out.println("-datastructure-" + dataStructure);
                int chunkNumber = getChunkNumber(corruptedChunknm);
                Set<Integer> cs = chunkInfo.keySet();
                Map<Integer, MetaData> tmp = chunkInfo.get(chunkNumber);
                Set<Integer> tmpHealerNodes = tmp.keySet();
//                System.out.println("-datastructure-" + dataStructure);
//                System.out.println("-set-" + tmpHealerNodes);
                int[] healerNodes = new int[3];
                int i = 0;
                int index = 0;
                for (Iterator<Integer> it = tmpHealerNodes.iterator(); it.hasNext();) {
                    int next = it.next();
                    healerNodes[i] = next;
                    if (healerNodes[i] == corruptedNode) {
                        if (result) {
                            index = (i + 1) % 3;
                        } else {
                            index = (i + 2) % 3;
                        }

                    }
                    i++;
                }
                int healer = healerNodes[index];
//                System.out.println("-healernodes-" + healerNodes[0] + " " + healerNodes[1] + " " + healerNodes[2]);
//                System.out.println("-healer" + healer);
                synchronized (connectionCache) {
                    theal = connectionCache.get(healer);
                }
//                System.out.print("%");
                String healerIP = theal.getSocket().getInetAddress().getHostAddress();
                synchronized (listeningPortCache) {
                    port = listeningPortCache.get(healer);
                }
//System.out.print("^");
//                System.out.println("-ip-port-"+healerIP+":"+po);
                Socket healerS = new Socket(healerIP, port);

                TCPConnection hconn = new TCPConnection(healerS, this);
//System.out.print("&");
                try {
                    hconn.getSender().sendData(heal.getByte());
                } catch (Exception ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;
        }

    }

    private void startTimer() {
//        System.out.println("-timer-");
        ProbeTimer probeTimer = new ProbeTimer(getController());
        Timer timer = new Timer();
        timer.schedule(probeTimer, 0, Protocol.PROBE_TIMER);
    }

    private void startTimer2() {
        ProbeTimer2 probeTimer2 = new ProbeTimer2(getController());
        Timer timer = new Timer();
        timer.schedule(probeTimer2, 0, Protocol.PROBE_TIMER);
    }

    private void resetTimer(int nodeID) {
        synchronized (timerCache) {
            this.timerCache.replace(nodeID, Protocol.PROBE_TIMEOUT);
        }
    }

    private void resetTimer2(int nodeID) {
        synchronized (timerCache2) {
            this.timerCache2.replace(nodeID, Protocol.PROBE_TIMEOUT2);
        }
    }

    private void addConnectiontoCache(int nodeId, Socket s) {
        TCPConnection tempObj;
        synchronized (tempCache) {
            int idbyPort = s.getPort();
            String idbyIP = s.getInetAddress().getHostAddress();
            String id = idbyIP + ":" + idbyPort;
//            System.out.println("-add- " + id);
            tempObj = (TCPConnection) this.tempCache.get(id);
//            System.out.println("-localPort-" + id + "-conn-" + tempObj);
        }
        if (tempObj != null) {
            synchronized (connectionCache) {
                this.connectionCache.put(nodeId, tempObj);
            }
//            this.tempConnectionCache.put(Integer.valueOf(id), tempObj);
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

    private int[] determineCS(String chunkName) {

        int[] nodes = new int[9];
        int numchnksrvrs;
        synchronized (spaceCache) {
            numchnksrvrs = spaceCache.size();

            Collections.sort(this.spaceCache, new Comparator<SpaceCache>() {
                public int compare(Controller.SpaceCache o1, Controller.SpaceCache o2) {
                    return (int) (o1.getSpace() - o2.getSpace());
                }
            });

            if (numchnksrvrs > 3) {
                int item1 = new Random().nextInt(numchnksrvrs);
                int item2 = item1 + 1;
                if (item2 >= numchnksrvrs) {
                    item2 = 0;
                }
                int item3 = item2 + 1;
                if (item3 >= numchnksrvrs) {
                    item3 = 0;
                }

                nodes[0] = spaceCache.get(item1).getID();
                nodes[1] = spaceCache.get(item2).getID();
                nodes[2] = spaceCache.get(item3).getID();

            }
        }
        return nodes;

//        int[] nodes = new int[3];
////        int[] previousNodes = new int[3];
////        for (int i = 0; i < 3; i++) {
////            previousNodes[i] = -1;
////        }
//        int limit;
//        int index = 0;
//        synchronized (this.spaceCache) {
//            limit = spaceCache.size();
//            Collections.sort(this.spaceCache, new Comparator<SpaceCache>() {
//                public int compare(Controller.SpaceCache o1, Controller.SpaceCache o2) {
//                    return (int) (o1.getSpace() - o2.getSpace());
//                }
//            });
//        }
//
////        System.out.println("-spacecache" + spaceCache);
//        for (int i = 0; i < 3; i++) {
//            index = i + previosNode;
//            if (index >= limit) {
//                index = 0;
//            }
//            SpaceCache space;
//            synchronized (spaceCache) {
//                space = (SpaceCache) this.spaceCache.get(index);
//            }
////            System.out.println("-id " + space.getID());
//            nodes[i] = space.getID();
//
//        }
//        previosNode = index;
////        System.out.println("-nodes: " + nodes[0] + nodes[1] + nodes[2]);
//
//        return nodes;
    }

    private int getChunkNumber(String s) {
        StringBuffer buff = new StringBuffer(s);
        String lastToken = buff.substring(buff.lastIndexOf("k") + 1);
        int number = Integer.parseInt(lastToken);
        return number;
    }

    private void addToStructure(String ChunkName, int[] nodes) {
        AddToDataStructure as = new AddToDataStructure(ChunkName, nodes);
//        Thread t = new Thread(as);
//        t.start();
        as.run();
    }

    private void updateDatastructure(Map<String, MetaData> metacache, int node) {
        UpdateDataStructure ud = new UpdateDataStructure(metacache, node);
//        Thread t = new Thread(ud);
//        t.start();
        ud.run();
    }

    public void removenode(int nodeID) {
        NodeRemover nr = new NodeRemover(nodeID);
//        Thread t = new Thread(nr);
//        t.start();
        nr.run();
    }

    private class SpaceCache {

        private int ID;
        private long space;

        public SpaceCache(int ID, long space) {
            this.ID = ID;
            this.space = space;
        }

        public int getID() {
            return this.ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public long getSpace() {
            return this.space;
        }

        public void setSpace(long space) {
            this.space = space;
        }
    }

    private class ServerReg implements Runnable {

        private byte[] data;
        private Socket s;
        EventFactory eventFactory = EventFactory.getInstance();

        private ServerReg(byte[] data, Socket s) {
            this.data = data;
            this.s = s;
        }

        @Override
        public void run() {
            try {
                ChunkServerRegistration registration = new ChunkServerRegistration(data);
//                int nodeID = addToDepository(registration.getIp(), registration.getPort(), s);
                int nodeID = numNodes.incrementAndGet();
                long freeSpace = registration.getFreeSpace();
                int listeningPort = registration.getPort();
                System.out.println("[INFO] Chunk Server with id: " + nodeID + " connected");
                if (nodeID >= 0) {

                    addConnectiontoCache(nodeID, s);

                }
                synchronized (listeningPortCache) {
                    listeningPortCache.put(nodeID, listeningPort);
                }
                ControllerRegistrationStatus crs = (ControllerRegistrationStatus) eventFactory.createEvent(Protocol.CONTROLLER_REGISTRATION_STATUS);
                crs.setNodeID(nodeID);
                crs.setInfo("Registration Request Succesfull");
                TCPConnection conn;
                synchronized (connectionCache) {
                    conn = (TCPConnection) connectionCache.get(nodeID);
                }
                conn.getSender().sendData(crs.getByte());
                synchronized (timerCache) {
                    timerCache.put(Integer.valueOf(nodeID), Long.valueOf(Protocol.PROBE_TIMEOUT));
                }
                synchronized (spaceCache) {
                    spaceCache.add(new SpaceCache(nodeID, freeSpace));
                }
            } catch (IOException ex) {
                System.err.println("[ERROR] Error while Registering ChunkServer");
            }
        }
    }

    private class AddToDataStructure implements Runnable {

        String fileName;
        int seqNum;
        int[] nodes = new int[3];
        MetaData meta;

        private AddToDataStructure(String ChunkName, int[] nodes) {
            this.fileName = ChunkName.substring(0, ChunkName.lastIndexOf("_"));
            this.seqNum = getChunkNumber(ChunkName);
            this.nodes = nodes;
        }

        @Override
        public void run() {
            synchronized (dataStructure) {
//                dataStructure.remove(fileName);

                Map<Integer, Map<Integer, MetaData>> chunks = dataStructure.get(fileName);
                if (chunks == null) {
                    chunks = new HashMap<>();
                }

                Map<Integer, MetaData> storedNodes = chunks.get(seqNum);
                if (storedNodes == null) {
                    storedNodes = new HashMap<>();
                }
                for (int i = 0; i < 3; i++) {
                    storedNodes.put(nodes[i], null);
                }
                chunks.put(seqNum, storedNodes);
                dataStructure.put(fileName, chunks);
//                System.out.println("[DATASTRUCTURE] " + dataStructure);
            }
        }

    }

    private class UpdateDataStructure implements Runnable {

//        String fileName;
        int nodeID;
        Map<String, MetaData> metacache;

        private UpdateDataStructure(Map<String, MetaData> metacache, int node) {

//            this.fileName = ChunkName.substring(0, ChunkName.lastIndexOf("_"));
            this.nodeID = node;
            this.metacache = metacache;
        }

        @Override
        public void run() {
            synchronized (dataStructure) {
                for (Map.Entry<String, MetaData> entrySet : metacache.entrySet()) {
                    String key = entrySet.getKey();
                    MetaData md = entrySet.getValue();

                    String fileName = key.substring(0, key.lastIndexOf("_"));
//                    System.out.println("-chunkname- " + key);
                    int chunkNum = getChunkNumber(key);

                    Map<Integer, Map<Integer, MetaData>> chunkstructure = dataStructure.get(fileName);
                    if (chunkstructure == null) {
                        chunkstructure = new HashMap<>();
                    }
                    Map<Integer, MetaData> nodestructure = chunkstructure.get(chunkNum);
                    if (nodestructure == null) {
                        nodestructure = new HashMap<>();
                    }
                    nodestructure.put(nodeID, md);
                    chunkstructure.put(chunkNum, nodestructure);
                    dataStructure.put(fileName, chunkstructure);
                }
            }
        }
    }

    private class NodeRemover implements Runnable {

        int nodeID;

        private NodeRemover(int nodeID) {
            this.nodeID = nodeID;
        }

        @Override
        public void run() {
            System.out.println("[INFO] CHUNK SERVER FAILURE DETECTED: " + nodeID);
            int[] otherNodes = new int[2];

//            int port = -1;
            TCPConnection conn;
//            String ip;
            Map<String, String> chunkstotransfer = new HashMap<>();
            synchronized (dataStructure) {
//                System.out.println("-datastructure-\n" + dataStructure);
                for (Map.Entry<String, Map<Integer, Map<Integer, MetaData>>> entrySet : dataStructure.entrySet()) {
                    int selectedID = -1;
                    String file = entrySet.getKey();
                    Map<Integer, Map<Integer, MetaData>> chunks = entrySet.getValue();
                    for (Map.Entry<Integer, Map<Integer, MetaData>> entrySet1 : chunks.entrySet()) {
                        int chunkNo = entrySet1.getKey();
                        Map<Integer, MetaData> node = entrySet1.getValue();

                        if (node.containsKey(nodeID)) {
                            node.remove(nodeID);

                            int i = 0;
                            for (Map.Entry<Integer, MetaData> entrySet2 : node.entrySet()) {
                                int othernode = entrySet2.getKey();
                                otherNodes[i] = othernode;
                                i++;
                            }

                            synchronized (spaceCache) {
                                boolean found = false;
                                int size = spaceCache.size();
                                while (!found) {
                                    int index = new Random().nextInt(size);
                                    SpaceCache prospective = spaceCache.get(index);
                                    int prospect = prospective.getID();
                                    if (otherNodes.length == 2) {
                                        if (prospect != nodeID && prospect != otherNodes[0] && prospect != otherNodes[1]) {
                                            selectedID = prospect;
                                            found = true;
                                        }
                                    } else if (otherNodes.length == 1) {
                                        if (prospect != nodeID && prospect != otherNodes[0]) {
                                            selectedID = prospect;
                                            found = true;
                                        }
                                    }

                                }
                            }
                            node.put(selectedID, null);
                            chunks.put(chunkNo, node);
                            dataStructure.put(file, chunks);

                            String chunkName = file + "_chunk" + chunkNo;
                            String newNodeOldNode = selectedID + ":" + otherNodes[0];
                            if (otherNodes.length > 1) {
                                newNodeOldNode = newNodeOldNode + ":" + otherNodes[1];
                            }
                            chunkstotransfer.put(chunkName, newNodeOldNode);

                        }
                    }
                }
            }

//            System.out.println("-chunktotransfer-\n" + chunkstotransfer);
            for (Map.Entry<String, String> entrySet : chunkstotransfer.entrySet()) {
                boolean present = false;
                String chunk = entrySet.getKey();
                String newNodeOldNode = entrySet.getValue();
                String[] split = newNodeOldNode.split(":");
                int newnodeID = Integer.parseInt(split[0]);
                int oldNodeID = Integer.parseInt(split[1]);
                int oldNodeID2 = -1;
                if (split.length > 2) {
                    oldNodeID2 = Integer.parseInt(split[2]);
                    present = true;
                }

                String newnodeIP;
                int newnodePort;
                String oldnodeIP;
                int oldnodePort;
                String oldnodeIP2 = null;
                int oldnodePort2 = 0;
                synchronized (connectionCache) {

                    conn = (TCPConnection) connectionCache.get(newnodeID);
//                    System.out.println("-conn-" + conn);
                    newnodeIP = conn.getSocket().getInetAddress().getHostAddress();
                    conn = (TCPConnection) connectionCache.get(oldNodeID);
                    oldnodeIP = conn.getSocket().getInetAddress().getHostAddress();
                    if (present) {
                        conn = (TCPConnection) connectionCache.get(oldNodeID2);
                        oldnodeIP2 = conn.getSocket().getInetAddress().getHostAddress();
                    }
                }
                synchronized (listeningPortCache) {
                    newnodePort = listeningPortCache.get(newnodeID);
                    oldnodePort = listeningPortCache.get(oldNodeID);
                    if (present) {
                        oldnodePort2 = listeningPortCache.get(oldNodeID2);
                    }
                }
                Socket soc = null;
                try {
                    System.out.println("[INFO] FILE TRANSFER FROM " + oldNodeID + " TO " + newnodeID);
//                    System.out.println("-ip-port- " +oldnodeIP + ":" + oldnodePort);
                    try {
                        soc = new Socket(oldnodeIP, oldnodePort);
                    } catch (Exception e) {
                        if (present) {
                            soc = new Socket(oldnodeIP2, oldnodePort2);
                        }
                    }
                    TCPConnection tconn = new TCPConnection(soc, controller);
                    EventFactory eventFactory = EventFactory.getInstance();
                    ControllerTransferChunkServer ctcs = (ControllerTransferChunkServer) eventFactory.createEvent(Protocol.CONTROLLER_TRANSFER_CHUNKSERVER);

                    ctcs.setNodeID(newnodeID);
                    ctcs.setChunkName(chunk.getBytes());
                    ctcs.setIp(newnodeIP.getBytes());
                    ctcs.setPort(newnodePort);

                    tconn.getSender().sendData(ctcs.getByte());

                } catch (Exception ex) {
//                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
//            synchronized (connectionCache) {
//                connectionCache.remove(nodeID);
//            }
//            synchronized (listeningPortCache) {
//                listeningPortCache.remove(nodeID);
//            }
            synchronized (timerCache) {
                timerCache.remove(nodeID);
            }

        }
    }
}
