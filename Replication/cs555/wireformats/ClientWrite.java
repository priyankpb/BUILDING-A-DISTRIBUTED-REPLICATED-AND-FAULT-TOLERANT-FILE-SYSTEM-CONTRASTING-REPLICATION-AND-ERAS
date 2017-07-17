package cs555.wireformats;

import cs555.util.Protocol;
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
    private byte[] chunkName;
    private byte[] data;
    private int node1;
    private int node2;

    private byte[] ip1;
    private byte[] ip2;
    private int port1;
    private int port2;

    public int getClientID() {
        return this.clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public byte[] getChunkName() {
        return this.chunkName;
    }

    public void setChunkName(byte[] chunkName) {
        this.chunkName = chunkName;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getNode1() {
        return node1;
    }

    public void setNode1(int node1) {
        this.node1 = node1;
    }

    public int getNode2() {
        return node2;
    }

    public void setNode2(int node2) {
        this.node2 = node2;
    }

    public byte[] getIp1() {
        return ip1;
    }

    public void setIp1(byte[] ip1) {
        this.ip1 = ip1;
    }

    public byte[] getIp2() {
        return ip2;
    }

    public void setIp2(byte[] ip2) {
        this.ip2 = ip2;
    }

    public int getPort1() {
        return port1;
    }

    public void setPort1(int port1) {
        this.port1 = port1;
    }

    public int getPort2() {
        return port2;
    }

    public void setPort2(int port2) {
        this.port2 = port2;
    }

    public ClientWrite() {
    }

    public ClientWrite(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.clientID = din.readInt();

        int namelength = din.readInt();
        this.chunkName = new byte[namelength];
        din.readFully(this.chunkName, 0, namelength);

        int datalength = din.readInt();
        this.data = new byte[datalength];
        din.readFully(this.data, 0, datalength);

        this.node1 = din.readInt();
        this.node2 = din.readInt();

        int ip1Length = din.readInt();
        this.ip1 = new byte[ip1Length];
        din.readFully(this.ip1, 0, ip1Length);

        int ip2Length = din.readInt();
        this.ip2 = new byte[ip2Length];
        din.readFully(this.ip2, 0, ip2Length);

        this.port1 = din.readInt();
        this.port2 = din.readInt();

        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeInt(this.clientID);

        byte[] nameBytes = this.chunkName;
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        byte[] data = this.data;
        dout.writeInt(data.length);
        dout.write(data);

        dout.writeInt(this.node1);
        dout.writeInt(this.node2);

        byte[] TempIP1 = this.ip1;
        dout.writeInt(TempIP1.length);
        dout.write(TempIP1);

        byte[] TempIP2 = this.ip2;
        dout.writeInt(TempIP2.length);
        dout.write(TempIP2);

        dout.writeInt(this.port1);
        dout.writeInt(this.port2);

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
