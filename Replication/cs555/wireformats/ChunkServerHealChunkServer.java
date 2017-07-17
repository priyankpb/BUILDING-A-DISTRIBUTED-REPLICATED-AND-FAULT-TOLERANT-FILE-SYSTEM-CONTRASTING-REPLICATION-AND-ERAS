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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author priyankb
 */
public class ChunkServerHealChunkServer implements Event {

    private byte type = Protocol.CHUNKSERVER_HEAL_CHUNKSERVER;
    byte[] chunkName;
    Map<Integer, byte[]> sliceCache = new HashMap<>();

    public ChunkServerHealChunkServer() {
    }

    public byte[] getChunkName() {
        return chunkName;
    }

    public void setChunkName(byte[] chunkName) {
        this.chunkName = chunkName;
    }

    public Map<Integer, byte[]> getSliceCache() {
        return sliceCache;
    }

    public void setSliceCache(Map<Integer, byte[]> sliceCache) {
        this.sliceCache = sliceCache;
    }

    public ChunkServerHealChunkServer(byte[] data) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readByte();

        int namelength = din.readInt();
        this.chunkName = new byte[namelength];
        din.readFully(this.chunkName, 0, namelength);

        int length = din.readInt();

        for (int i = 0; i < length; i++) {

            int key = din.readInt();

            int dataLen = din.readInt();
            byte[] databyte = new byte[dataLen];
            din.readFully(databyte, 0, dataLen);

            sliceCache.put(key, databyte);

        }

        baInputStream.close();
        din.close();
    }

    @Override
    public byte[] getByte() throws Exception {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.write(getType());

        byte[] nameBytes = this.chunkName;
        dout.writeInt(nameBytes.length);
        dout.write(nameBytes);

        int length = this.sliceCache.size();
        dout.writeInt(length);

        for (Map.Entry<Integer, byte[]> entrySet : sliceCache.entrySet()) {
            int key = entrySet.getKey();
            byte[] data = entrySet.getValue();

            dout.writeInt(key);

            dout.writeInt(data.length);
            dout.write(data);

        }

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
