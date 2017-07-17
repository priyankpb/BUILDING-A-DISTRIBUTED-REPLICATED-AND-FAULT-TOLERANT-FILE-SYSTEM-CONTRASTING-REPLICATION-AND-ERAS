package cs555.util;

public class Protocol {

    public static final byte CHUNKSERVER_REGISTRATION = 2;
    public static final byte CHUNKSERVER_DATA_CORRUPT = 3;
    public static final byte CHUNKSERVER_MINOR_HEARTBEAT = 4;
    public static final byte CHUNKSERVER_MAJOR_HEARTBEAT = 5;
    public static final byte CHUNKSERVER_PROBE = 6;
    public static final byte CHUNKSERVER_CHUNKSERVER1 = 7;
    public static final byte CHUNKSERVER_CHUNKSERVER2 = 8;
    public static final byte CHUNKSERVER_SENDCHUNK = 9;
    public static final byte CHUNKSERVER_HEAL_CHUNKSERVER = 10;
    public static final byte CHUNKSERVER_CHUNKSERVER_TRANSFER = 11;

    public static final byte CONTROLLER_PROBE = 50;
    public static final byte CONTROLLER_SEND_CHUNK_TO_CHUNKSERVER = 51;
    public static final byte CONTROLLER_REGISTRATION_STATUS = 52;
    public static final byte CONTROLLER_CLIENT_REGISTRATION_STATUS = 53;
    public static final byte CONTROLLER_CLIENT_CHUNKSERVERS = 54;
    public static final byte CONTROLLER_READ_REPLY = 55;
    public static final byte CONTROLLER_HEAL_CHUNKSERVER = 56;
    public static final byte CONTROLLER_TRANSFER_CHUNKSERVER = 57;
    public static final byte CONTROLLER_OVERWRITE = 58;

    public static final byte CLIENT_READ = 100;
    public static final byte CLIENT_WRITE = 101;
    public static final byte CLIENT_REGISTRATION = 102;
    public static final byte CLIENT_CHUNK_NAME = 103;
    public static final byte CLIENT_FETCH_CHUNK = 104;
    public static final byte CLIENT_OVERWRITE = 105;

    public static final long MINOR_HEARTBEAT = 30000;
    public static final long MAJOR_HEARTBEAT = 300000;
    public static final long PROBE_TIMEOUT = 40000;
    public static final long PROBE_TIMEOUT2 = 50000;
    public static final long PROBE_TIMER = 1000;

//    public static final long MINOR_HEARTBEAT = 15000;
//    public static final long MAJOR_HEARTBEAT = 150000;
//    public static final long PROBE_TIMEOUT = 20000;
//    public static final long PROBE_TIMEOUT2 = 25000;
//    public static final long PROBE_TIMER = 1000;
    
    public static final int CHUNK_SIZE = 1024 * 64; //64KB
    public static final int SLICE_SIZE = 1024 * 8; //8KB
    public static final int NO_OF_SLICES = CHUNK_SIZE / SLICE_SIZE;

    public static final String HOSTNAME = "129.82.46.229";
    public static final int PORT = 11520;
}
