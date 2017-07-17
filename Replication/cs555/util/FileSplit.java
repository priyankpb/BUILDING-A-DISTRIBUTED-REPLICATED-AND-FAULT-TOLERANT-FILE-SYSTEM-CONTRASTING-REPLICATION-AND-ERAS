package cs555.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSplit {

    // private static byte[][] data;

    private static int numberOfChunks;

    private static String prefix;

    private List<String> chunkNames = new ArrayList<>();
    private List<byte[]> data = new ArrayList<>();

    public List<byte[]> getData() {
        return data;
    }

    public List<String> getChunkNames() {
        return chunkNames;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
    }

    public String getPrefix() {
        return prefix;
    }

    public FileSplit(File f) throws IOException {
        int partCounter = 1;
        prefix = f.getParent() + "/";
        int chunkSize = Protocol.CHUNK_SIZE;
        byte[] buffer = new byte[chunkSize];

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

        String name = f.getName();
        int tmp = 0;
        int i = 0;
        while ((tmp = bis.read(buffer)) > 0) {
//        data[i] = new byte[chunkSize];
            data.add(Arrays.copyOfRange(buffer, 0, tmp));
            String tmp1 = name + "_chunk" + partCounter++;
            chunkNames.add(tmp1);
            i++; 
//            buffer = new byte[chunkSize];
        }
        numberOfChunks = i;
    }
}
