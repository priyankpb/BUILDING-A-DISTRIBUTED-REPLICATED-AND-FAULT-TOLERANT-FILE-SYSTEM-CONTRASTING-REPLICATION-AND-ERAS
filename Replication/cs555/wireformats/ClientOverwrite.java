/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs555.wireformats;

import cs555.util.Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author priyankb
 */
public class ClientOverwrite implements Event {

    private byte type = Protocol.CLIENT_OVERWRITE;
    private int clientID;
    private String fileName;

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public ClientOverwrite() {
    }

    public ClientOverwrite(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();
        this.clientID = din.readInt();

        int len = din.readInt();
        byte[] fn = new byte[len];
        din.readFully(fn, 0, len);
        this.fileName = new String(fn);
        
        baInputStream.close();
        din.close();
    }

    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());
        dout.writeInt(this.clientID);

        byte[] fn = fileName.getBytes();
        dout.writeInt(fn.length);
        dout.write(fn);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public byte getType() {
        return this.type;
    }

}
