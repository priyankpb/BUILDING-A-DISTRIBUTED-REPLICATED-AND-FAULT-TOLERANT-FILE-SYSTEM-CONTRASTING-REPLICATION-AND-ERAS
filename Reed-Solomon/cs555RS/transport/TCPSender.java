package cs555RS.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender {

    Socket socket;
    DataOutputStream dout;

    public TCPSender(Socket socket) /*    */ {
        this.socket = socket;
        try {
            this.dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("--exception in output stream--");
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void sendData(byte[] data) throws IOException {
        int dataLength = data.length;
        try {
            this.dout.writeInt(dataLength);
            this.dout.write(data, 0, dataLength);
            this.dout.flush();
        } catch (IOException e) {
// System.out.println("--Exception in sending data--");
//            e.printStackTrace();
        }
    }
}
