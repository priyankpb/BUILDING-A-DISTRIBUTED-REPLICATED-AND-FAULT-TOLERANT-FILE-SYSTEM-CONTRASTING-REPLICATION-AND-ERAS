package cs555.util;

import cs555.nodes.ChunkServer;
import cs555.transport.TCPConnection;
import cs555.wireformats.ChunkServerMajorHeartbeat;
import cs555.wireformats.ChunkServerMinorHeartbeat;
import cs555.wireformats.EventFactory;
import java.util.TimerTask;

public class Heartbeat
        extends TimerTask {

    ChunkServer cs;
    int time = 0;
    int nodeID;
    TCPConnection conn;
    EventFactory eventFactory = EventFactory.getInstance();

    public Heartbeat(ChunkServer cs) {
        this.cs = cs;
        this.nodeID = cs.getNodeID();
        this.conn = cs.getConn();
    }

    public void run() {
        this.time += 1;
        long interval = Protocol.MAJOR_HEARTBEAT / Protocol.MINOR_HEARTBEAT;

        if (this.time == interval) {
            this.time = 0;
            try {
                ChunkServerMajorHeartbeat major = (ChunkServerMajorHeartbeat) this.eventFactory.createEvent(Protocol.CHUNKSERVER_MAJOR_HEARTBEAT);
                major.setNodeID(this.nodeID);
                major.setMetaDataCache(cs.getMetaDataCache());

                this.conn.getSender().sendData(major.getByte());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                ChunkServerMinorHeartbeat minor = (ChunkServerMinorHeartbeat) this.eventFactory.createEvent(Protocol.CHUNKSERVER_MINOR_HEARTBEAT);
                minor.setMetaDataCache(cs.getNewMetaDataCache());
                minor.setNodeID(this.nodeID);

                this.conn.getSender().sendData(minor.getByte());
                cs.resetNewMetaDataCache();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
