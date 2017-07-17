/*    */ package cs555.transport;
/*    */ 
/*    */ import cs555.nodes.Node;
/*    */ import java.net.Socket;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TCPConnection
/*    */ {
/*    */   private Socket socket;
/*    */   private TCPSender sender;
/*    */   private TCPReceiver receiver;
/*    */   
/*    */   public TCPConnection(Socket socket, Node node)
/*    */   {
/* 19 */     this.socket = socket;
/* 20 */     this.sender = new TCPSender(socket);
/* 21 */     this.receiver = new TCPReceiver(socket, node);
/* 22 */     Thread receiverThread = new Thread(this.receiver);
/* 23 */     receiverThread.start();
/*    */   }
/*    */   
/*    */   public Socket getSocket() {
/* 27 */     return this.socket;
/*    */   }
/*    */   
/*    */   public void setSocket(Socket socket) {
/* 31 */     this.socket = socket;
/*    */   }
/*    */   
/*    */   public TCPSender getSender() {
/* 35 */     return this.sender;
/*    */   }
/*    */   
/*    */   public void setSender(TCPSender sender) {
/* 39 */     this.sender = sender;
/*    */   }
/*    */   
/*    */   public TCPReceiver getReceiver() {
/* 43 */     return this.receiver;
/*    */   }
/*    */   
/*    */   public void setReceiver(TCPReceiver receiver) {
/* 47 */     this.receiver = receiver;
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\transport\TCPConnection.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */