package cs555RS.util;

public class Protocol {

    public static final byte CHUNKSERVER_REGISTRATION = 2;
    public static final byte CHUNKSERVER_MINOR_HEARTBEAT = 3;
    public static final byte CHUNKSERVER_MAJOR_HEARTBEAT = 4;
    public static final byte CHUNKSERVER_SEND_SHARD = 5;

    
    public static final byte CONTROLLER_REGISTRATION_STATUS = 50;
    public static final byte CONTROLLER_CLIENT_REGISTRATION_STATUS = 51;
    public static final byte CONTROLLER_SEND_CHUNK_TO_CHUNKSERVER = 52;
    public static final byte CONTROLLER_CLIENT_CHUNKSERVERS = 54;
    public static final byte CONTROLLER_READ_REPLY = 55;
    public static final byte CONTROLLER_HEAL_CHUNKSERVER = 56;
    

    public static final byte CLIENT_READ = 100;
    public static final byte CLIENT_WRITE = 101;
    public static final byte CLIENT_REGISTRATION = 102;
    public static final byte CLIENT_CHUNK_NAME = 103;
    public static final byte CLIENT_FETCH_SHARD = 104;

    public static final long MINOR_HEARTBEAT = 300L;
    public static final long MAJOR_HEARTBEAT = 3000L;

    public static final int CHUNK_SIZE = 1024 * 64; //64KB
    public static final int SLICE_SIZE = 1024 * 8; //8KB
    public static final int NO_OF_SLICES = CHUNK_SIZE / SLICE_SIZE;

    public static final String HOSTNAME = "129.82.46.230";
    public static final int PORT = 12520;
}
