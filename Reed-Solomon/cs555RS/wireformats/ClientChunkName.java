package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientChunkName implements Event {

    private byte type = Protocol.CLIENT_CHUNK_NAME;
    private int nodeID;
    private byte[] data;

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

    public ClientChunkName() {
    }

    public ClientChunkName(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.nodeID = din.readInt();
        int length = din.readInt();
        this.data = new byte[length];
        din.readFully(this.data, 0, length);
        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeInt(this.nodeID);
        byte[] data = this.data;
        dout.writeInt(data.length);
        dout.write(data);
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
