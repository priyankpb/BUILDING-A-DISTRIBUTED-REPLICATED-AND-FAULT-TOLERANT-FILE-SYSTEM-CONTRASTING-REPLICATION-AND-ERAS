/*    */ package cs555.util;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class nodeSocket
/*    */ {
/*    */   private byte[] ip;
/*    */   
/*    */ 
/*    */ 
/*    */   private int port;
/*    */   
/*    */ 
/*    */ 
/*    */   public nodeSocket(byte[] ip, int port)
/*    */   {
/* 18 */     this.ip = ip;
/* 19 */     this.port = port;
/*    */   }
/*    */   
/*    */   public byte[] getIp() {
/* 23 */     return this.ip;
/*    */   }
/*    */   
/*    */   public void setIp(byte[] ip) {
/* 27 */     this.ip = ip;
/*    */   }
/*    */   
/*    */   public int getPort() {
/* 31 */     return this.port;
/*    */   }
/*    */   
/*    */   public void setPort(int port) {
/* 35 */     this.port = port;
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555.util\nodeSocket.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */