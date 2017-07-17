package cs555.wireformats;

import cs555.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ControllerHealChunkServer implements Event {

    private byte type = Protocol.CONTROLLER_HEAL_CHUNKSERVER;
    boolean result;
    private int corruptedNodeID;
    private byte[] corruptedNodeIP;
    private int corruptedNodePort;

    private int clientID;
    private byte[] clientIP;
    private int clientPort;
    private byte[] chunkName;
    private byte[] corruptSlices;

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

    public int getCorruptedNodeID() {
        return this.corruptedNodeID;
    }

    public void setCorruptedNodeID(int clientID) {
        this.corruptedNodeID = clientID;
    }

    public byte[] getCorruptedNodeIP() {
        return corruptedNodeIP;
    }

    public void setCorruptedNodeIP(byte[] corruptedNodeIP) {
        this.corruptedNodeIP = corruptedNodeIP;
    }

    public int getCorruptedNodePort() {
        return corruptedNodePort;
    }

    public void setCorruptedNodePort(int corruptedNodePort) {
        this.corruptedNodePort = corruptedNodePort;
    }

    public byte[] getChunkName() {
        return this.chunkName;
    }

    public void setChunkName(byte[] chunkName) {
        this.chunkName = chunkName;
    }

    public byte[] getClientIP() {
        return clientIP;
    }

    public void setClientIP(byte[] clientIP) {
        this.clientIP = clientIP;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public ControllerHealChunkServer() {
    }

    public ControllerHealChunkServer(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();

        this.result = din.readBoolean();
        this.corruptedNodeID = din.readInt();
        int nipLength = din.readInt();
        this.corruptedNodeIP = new byte[nipLength];
        din.readFully(this.corruptedNodeIP, 0, nipLength);
        this.corruptedNodePort = din.readInt();

        this.clientID = din.readInt();
        int ipLength = din.readInt();
        this.clientIP = new byte[ipLength];
        din.readFully(this.clientIP, 0, ipLength);
        this.clientPort = din.readInt();

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
        dout.writeInt(this.corruptedNodeID);

        byte[] cipBytes = this.corruptedNodeIP;
        dout.writeInt(cipBytes.length);
        dout.write(cipBytes);
        dout.writeInt(this.corruptedNodePort);

        dout.writeInt(this.clientID);
        byte[] ipBytes = this.clientIP;
        dout.writeInt(ipBytes.length);
        dout.write(ipBytes);
        dout.writeInt(this.clientPort);

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
