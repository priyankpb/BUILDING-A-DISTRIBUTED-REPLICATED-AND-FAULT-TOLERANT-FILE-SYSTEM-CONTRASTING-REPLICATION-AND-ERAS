package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChunkServerSendShard implements Event {

    private byte type = Protocol.CHUNKSERVER_SEND_SHARD;
    private int clientID;
    private byte[] shard;
    private byte[] data;
    private boolean corrupted;

    public byte[] getShard() {
        return shard;
    }

    public void setShard(byte[] shard) {
        this.shard = shard;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    public int getClientID() {
        return this.clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public byte[] getShardName() {
        return this.shard;
    }

    public void setShardName(byte[] shardName) {
        this.shard = shardName;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ChunkServerSendShard() {
    }

    public ChunkServerSendShard(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.clientID = din.readInt();

        int namelength = din.readInt();
        this.shard = new byte[namelength];
        din.readFully(this.shard, 0, namelength);

        this.corrupted = din.readBoolean();

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

        byte[] nameBytes = this.shard;
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        dout.writeBoolean(corrupted);
        
        byte[] sharddata = this.data;
//        System.out.println("-data-"+ this.data);
        dout.writeInt(sharddata.length);
        dout.write(sharddata);

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
