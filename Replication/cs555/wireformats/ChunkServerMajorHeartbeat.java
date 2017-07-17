package cs555.wireformats;

import cs555.util.MetaData;
import cs555.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChunkServerMajorHeartbeat implements Event {

    private byte type = Protocol.CHUNKSERVER_MAJOR_HEARTBEAT;

    private int nodeID;
    private byte[] data;
    private Map<String, MetaData> metaDataCache = new HashMap<>();

    ChunkServerMajorHeartbeat() {
    }

    public int getNodeID() {
        return this.nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<String, MetaData> getMetaDataCache() {
        return metaDataCache;
    }

    public void setMetaDataCache(Map<String, MetaData> metaDataCache) {
        this.metaDataCache = metaDataCache;
    }

    public ChunkServerMajorHeartbeat(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.nodeID = din.readInt();

        int length = din.readInt();
        for (int i = 0; i < length; i++) {

            int keyLen = din.readInt();
            byte[] keyB = new byte[keyLen];
            din.readFully(keyB, 0, keyLen);
            String key = new String(keyB);
            
            int ver = din.readInt();
            long tstamp = din.readLong();
            int checkLen = din.readInt();
            byte[] check = new byte[checkLen];
            din.readFully(check, 0, checkLen);
            String checksum = new String(check);
            
            MetaData value = new MetaData(ver, tstamp, checksum);
            metaDataCache.put(key, value);

        }

        baInputStream.close();
        din.close();
    }

    public byte[] getByte()
            throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeInt(getNodeID());

        int length = metaDataCache.size();
        dout.writeInt(length);

        for (Map.Entry<String, MetaData> entrySet : metaDataCache.entrySet()) {
            String key = entrySet.getKey();
            MetaData value = entrySet.getValue();

            byte[] name = key.getBytes();
            dout.writeInt(name.length);
            dout.write(name);

            dout.writeInt(value.getVersion());
            dout.writeLong(value.getTimestamp());
            byte[] check = value.getChecksum().getBytes();
            dout.writeInt(check.length);
            dout.write(check);

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
