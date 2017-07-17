package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientFetchShard implements Event {

    private byte type = Protocol.CLIENT_FETCH_SHARD;
    private int clientID;
    private byte[] shardName;

    private byte[] ip;
    private int port;

    public byte[] getIp() {
        return ip;
    }

    public void setIp(byte[] ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getClientID() {
        return this.clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public byte[] getShardName() {
        return this.shardName;
    }

    public void setShardName(byte[] chunkName) {
        this.shardName = chunkName;
    }

    public ClientFetchShard() {
    }

    public ClientFetchShard(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.clientID = din.readInt();

        int namelength = din.readInt();
        this.shardName = new byte[namelength];
        din.readFully(this.shardName, 0, namelength);

        int ipLength = din.readInt();
        this.ip = new byte[ipLength];
        din.readFully(this.ip, 0, ipLength);

        this.port = din.readInt();

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

        byte[] TempIP = this.ip;
        dout.writeInt(TempIP.length);
        dout.write(TempIP);

        dout.writeInt(this.port);

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
