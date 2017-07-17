/*    */ package cs555RS.transport;
/*    */ 
/*    */ import cs555RS.nodes.Node;
/*    */ import java.io.DataInputStream;
/*    */ import java.io.IOException;
/*    */ import java.net.Socket;
/*    */ 
/*    */ 
/*    */ public class TCPReceiver
/*    */   implements Runnable
/*    */ {
/*    */   Socket socket;
/*    */   DataInputStream din;
/*    */   Node node;
/*    */   
/*    */   public TCPReceiver(Socket socket, Node node)
/*    */   {
/* 19 */     this.socket = socket;
/* 20 */     this.node = node;
/*    */     try {
/* 22 */       this.din = new DataInputStream(socket.getInputStream());
/*    */     } catch (IOException ex) {
///* 24 */       System.out.println("--exception in output stream--");
/*    */     }
/*    */   }
/*    */   
/*    */   public void run()
/*    */   {
        boolean result = true;
/*    */     while(result) {
/* 31 */       int dataLength = 0;
/* 32 */       while (this.socket != null) {
/*    */         try
/*    */         {
/* 35 */           dataLength = this.din.readInt();
/* 36 */           byte[] data = new byte[dataLength];
/* 37 */           this.din.readFully(data, 0, dataLength);
/* 38 */           this.node.onEvent(data, this.socket);
/*    */         } catch (IOException se) {
///* 40 */           System.out.println("--Error in receiving data--");
///* 41 */           se.printStackTrace();
                    result = false;
/*    */         }
/*    */       }
/*    */     }
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\transport\TCPReceiver.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */