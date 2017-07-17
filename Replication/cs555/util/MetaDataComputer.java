/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs555.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author priyankb
 */
public class MetaDataComputer {

    private byte[] chunkData;

    private int version;
    private long timestamp;
    private String checksum;

    public int getVersion() {
        return version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getChecksum() {
        return checksum;
    }

//    private static int noSlices = Protocol.NO_OF_SLICES;
    private static int sliceSize = Protocol.SLICE_SIZE;

    public MetaDataComputer(byte[] chunkData) {
        this.chunkData = chunkData;
        this.timestamp = System.currentTimeMillis();
    }

    public byte[] computeMetadata() throws NoSuchAlgorithmException, IOException {
        version = 1;
//        timestamp = System.currentTimeMillis();
        checksum = getSHA1Checksum();
        String metaString = version + "\n" + timestamp + "\n" + checksum;
        byte[] metaByte = metaString.getBytes();
        return metaByte;
    }

    public String getSHA1Checksum() throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance("SHA1");
//        FileInputStream fis = new FileInputStream(datafile);
//        byte[] dataBytes = new byte[1024 * 8];
//        int nread = 0;
//        while ((nread = fis.read(dataBytes)) != -1) {
//            md.update(dataBytes, 0, nread);
//        }
        int noSlices = (int) Math.ceil(chunkData.length * 1.0 / sliceSize);
        StringBuffer sb = new StringBuffer("");
        
//        System.out.println("ChunkData: " + new String(chunkData));
        
        for (int i = 0; i < noSlices; i++) {
            if (i == noSlices - 1) {
                md.update(this.chunkData, i * sliceSize, chunkData.length % sliceSize);
            } else {
                md.update(this.chunkData, i * sliceSize, sliceSize);

            }

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            for (int j = 0; j < mdbytes.length; j++) {
                sb.append(Integer.toString((mdbytes[j] & 0xff) + 0x100, 16).substring(1));
            }
            sb.append("\n");
        }
        String check = sb.toString();
//        System.out.println("-checksum- \n" + check);
//        System.out.println("Digest(in hex format):: " + sb.toString());
        return check;
    }

}
