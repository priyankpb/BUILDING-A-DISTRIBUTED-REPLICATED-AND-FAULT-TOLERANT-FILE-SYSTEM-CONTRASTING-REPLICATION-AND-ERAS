package cs555RS.transport;

import cs555RS.nodes.Node;
import java.net.Socket;

public class TCPConnection {

    private Socket socket;
    private TCPSender sender;
    private TCPReceiver receiver;

    public TCPConnection(Socket socket, Node node) {
        this.socket = socket;
        this.sender = new TCPSender(socket);
        this.receiver = new TCPReceiver(socket, node);
        Thread receiverThread = new Thread(this.receiver);
        receiverThread.start();
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public TCPSender getSender() {
        return this.sender;
    }

    public void setSender(TCPSender sender) {
        this.sender = sender;
    }

    public TCPReceiver getReceiver() {
        return this.receiver;
    }

    public void setReceiver(TCPReceiver receiver) {
        this.receiver = receiver;
    }
}
