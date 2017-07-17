package cs555RS.wireformats;

import cs555RS.util.Protocol;
import java.io.IOException;

public class EventFactory {

    private static EventFactory eventFactory = new EventFactory();

    public static EventFactory getInstance() {
        return eventFactory;
    }

    public Event createEvent(byte eventType) throws IOException {
        switch (eventType) {

            case Protocol.CHUNKSERVER_REGISTRATION:
                return new ChunkServerRegistration();

            case Protocol.CHUNKSERVER_MINOR_HEARTBEAT:
                return new ChunkServerMinorHeartbeat();

            case Protocol.CHUNKSERVER_MAJOR_HEARTBEAT:
                return new ChunkServerMajorHeartbeat();


            case Protocol.CHUNKSERVER_SEND_SHARD:
                return new ChunkServerSendShard();

            case Protocol.CONTROLLER_REGISTRATION_STATUS:
                return new ControllerRegistrationStatus();

            case Protocol.CONTROLLER_CLIENT_REGISTRATION_STATUS:
                return new ControllerClientRegistrationStatus();

            case Protocol.CONTROLLER_CLIENT_CHUNKSERVERS:
                return new ControllerClientChunkservers();

            case Protocol.CONTROLLER_READ_REPLY:
                return new ControllerReadReply();

            case Protocol.CLIENT_REGISTRATION:
                return new ClientRegistration();

            case Protocol.CLIENT_CHUNK_NAME:
                return new ClientChunkName();

            case Protocol.CLIENT_READ:
                return new ClientRead();

            case Protocol.CLIENT_WRITE:
                return new ClientWrite();

            case Protocol.CLIENT_FETCH_SHARD:
                return new ClientFetchShard();
        }

        return null;
    }

    
}
