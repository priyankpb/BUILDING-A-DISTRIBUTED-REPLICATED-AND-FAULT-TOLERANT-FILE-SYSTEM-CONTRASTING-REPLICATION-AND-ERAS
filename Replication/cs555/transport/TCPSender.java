/*    */ package cs555.transport;
/*    */ 
/*    */ import java.io.DataOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.PrintStream;
/*    */ import java.net.Socket;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TCPSender
/*    */ {
/*    */   Socket socket;
/*    */   DataOutputStream dout;
/*    */   
/*    */   public TCPSender(Socket socket)
/*    */   {
/* 19 */     this.socket = socket;
/*    */     try {
/* 21 */       this.dout = new DataOutputStream(socket.getOutputStream());
/*    */     } catch (IOException ex) {
/* 23 */       System.out.println("--exception in output stream--");
/*    */     }
/*    */   }
/*    */   
/*    */   public void sendData(byte[] data) throws IOException {
/* 28 */     int dataLength = data.length;
/*    */     try {
/* 30 */       this.dout.writeInt(dataLength);
/* 31 */       this.dout.write(data, 0, dataLength);
/* 32 */       this.dout.flush();
/*    */     } catch (IOException e) {
/* 34 */       System.out.println("--Exception in sending data--");
/*    */     }
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\transport\TCPSender.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */