package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ControllerClientChunkservers implements Event {

    private byte type = Protocol.CONTROLLER_CLIENT_CHUNKSERVERS;
    private int nodeID;
    private byte[] chunkName;
    private Map<Integer, Map<Integer, String>> chunkServers = new HashMap<>();

    ControllerClientChunkservers() {
    }

    public Map<Integer, Map<Integer, String>> getChunkServers() {
        return chunkServers;
    }

    public void setChunkServers(Map<Integer, Map<Integer, String>> chunkServers) {
        this.chunkServers = chunkServers;
    }

    public int getNodeID() {
        return this.nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public byte[] getChunkName() {
        return this.chunkName;
    }

    public void setChunkName(byte[] chunkName) {
        this.chunkName = chunkName;
    }

    public ControllerClientChunkservers(byte[] data) throws IOException {

        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.nodeID = din.readInt();
        int nameLength = din.readInt();
        this.chunkName = new byte[nameLength];
        din.readFully(this.chunkName, 0, nameLength);

        int size = din.readInt();
//        System.out.println("-SIZE- " + size);
        for (int i = 0; i < size; i++) {
            int shardNum = din.readInt();

            int len = din.readInt();
            Map<Integer, String> nodemap = new HashMap<>();
            for (int j = 0; j < len; j++) {
                int nodeID = din.readInt();
                
                int ipportL = din.readInt();
                byte[] ipportB = new byte[ipportL];
                din.readFully(ipportB);
                String ipport = new String(ipportB);
                nodemap.put(nodeID, ipport);
            }

            chunkServers.put(shardNum, nodemap);
        }

        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(this.type);
        dout.writeInt(this.nodeID);

        byte[] tempChunkName = this.chunkName;
        dout.writeInt(tempChunkName.length);
        dout.write(tempChunkName);

        int size = chunkServers.size();
//        System.out.println("-size" + size);
        dout.writeInt(size);
        for (Map.Entry<Integer, Map<Integer, String>> entrySet : chunkServers.entrySet()) {
            Integer shardNum = entrySet.getKey();
            Map<Integer, String> nodemap = entrySet.getValue();

            dout.writeInt(shardNum);

            dout.writeInt(nodemap.size());
            for (Map.Entry<Integer, String> entrySet1 : nodemap.entrySet()) {
                int nodeID = entrySet1.getKey();
                dout.writeInt(nodeID);

                String ipport = entrySet1.getValue();
                byte[] ipportB = ipport.getBytes();
                dout.writeInt(ipportB.length);
                dout.write(ipportB);
            }

        }

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    public byte getType() {
        return this.type;
    }
}
