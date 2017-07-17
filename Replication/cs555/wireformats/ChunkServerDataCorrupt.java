package cs555.wireformats;

import cs555.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkServerDataCorrupt implements Event {

    private byte type = Protocol.CHUNKSERVER_DATA_CORRUPT;
    private int nodeID;
    private int clientID;
    private byte[] chunkName;
    private byte[] corruptSlices;
    boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public byte[] getCorruptSlices() {
        return corruptSlices;
    }

    public void setCorruptSlices(byte[] corruptSlices) {
        this.corruptSlices = corruptSlices;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getNodeID() {
        return this.nodeID;
    }

    public void setNodeID(int clientID) {
        this.nodeID = clientID;
    }

    public byte[] getChunkName() {
        return this.chunkName;
    }

    public void setChunkName(byte[] chunkName) {
        this.chunkName = chunkName;
    }

    public ChunkServerDataCorrupt() {
    }

    public ChunkServerDataCorrupt(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.result = din.readBoolean();
        this.nodeID = din.readInt();
        this.clientID = din.readInt();
        int namelength = din.readInt();
        this.chunkName = new byte[namelength];
        din.readFully(this.chunkName, 0, namelength);

        int listSize = din.readInt();
        this.corruptSlices = new byte[listSize];
        din.readFully(this.corruptSlices, 0, listSize);

        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeBoolean(result);
        dout.writeInt(this.nodeID);
        dout.writeInt(this.clientID);

        byte[] nameBytes = this.chunkName;
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        dout.writeInt(corruptSlices.length);
        dout.write(corruptSlices);

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
