/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs555.util;

/**
 *
 * @author priyankb
 */
public class MetaData {

    private int version;
    private long timestamp;
    private String checksum;

    public MetaData(int v, long t, String c) {
        this.version = v;
        this.timestamp = t;
        this.checksum = c;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

}
