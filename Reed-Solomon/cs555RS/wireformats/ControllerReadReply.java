/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author priyankb
 */
public class ControllerReadReply implements Event {

    byte type = Protocol.CONTROLLER_READ_REPLY;
    int clientID;
    byte[] fileName;
    Map<Integer, Map<Integer, String>> finalMapToSendToClient = new TreeMap<>();

    ControllerReadReply() {

    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public byte[] getFileName() {
        return fileName;
    }

    public void setFileName(byte[] chunkName) {
        this.fileName = chunkName;
    }

    public Map<Integer, Map<Integer, String>> getFinalMapToSendToClient() {
        return finalMapToSendToClient;
    }

    public void setFinalMapToSendToClient(Map<Integer, Map<Integer, String>> finalMapToSendToClient) {
        this.finalMapToSendToClient = finalMapToSendToClient;
    }

    public ControllerReadReply(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.clientID = din.readInt();

        int namelength = din.readInt();
        this.fileName = new byte[namelength];
        din.readFully(this.fileName, 0, namelength);
        
        int length = din.readInt();
        for (int i = 0; i < length; i++) {
            int chunkNo = din.readInt();
            Map<Integer, String> shardLoc = new TreeMap<>();
            int size = din.readInt();
            for (int j = 0; j < size; j++) {
                int shardNo = din.readInt();

                int len = din.readInt();
                byte[] locB = new byte[len];
                din.readFully(locB, 0, len);
                String loc = new String(locB);
                shardLoc.put(shardNo, loc);
            }
            finalMapToSendToClient.put(chunkNo, shardLoc);
        }

        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeInt(this.clientID);

        byte[] nameBytes = this.fileName;
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        int length = finalMapToSendToClient.size();
        dout.writeInt(length);

        for (Map.Entry<Integer, Map<Integer, String>> entrySet : finalMapToSendToClient.entrySet()) {
            int chunkNo = entrySet.getKey();
            Map<Integer, String> shardLoc = entrySet.getValue();

            dout.writeInt(chunkNo);
            int size = shardLoc.size();
            dout.writeInt(size);
            for (Map.Entry<Integer, String> entrySet1 : shardLoc.entrySet()) {
                int shardNo = entrySet1.getKey();
                String loc = entrySet1.getValue();

                dout.writeInt(shardNo);
                byte[] locB = loc.getBytes();
                dout.writeInt(locB.length);
                dout.write(locB);

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
