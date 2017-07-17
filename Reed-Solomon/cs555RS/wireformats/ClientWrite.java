package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientWrite
        implements Event {

    private byte type = Protocol.CLIENT_WRITE;
    private int clientID;
    private byte[] shardName;
    private byte[] data;

    public int getClientID() {
        return this.clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public byte[] getShardName() {
        return this.shardName;
    }

    public void getShardName(byte[] chunkName) {
        this.shardName = chunkName;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ClientWrite() {
    }

    public ClientWrite(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.clientID = din.readInt();

        int namelength = din.readInt();
        this.shardName = new byte[namelength];
        din.readFully(this.shardName, 0, namelength);

        int datalength = din.readInt();
        this.data = new byte[datalength];
        din.readFully(this.data, 0, datalength);

        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeInt(this.clientID);

        byte[] nameBytes = this.shardName;
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

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
