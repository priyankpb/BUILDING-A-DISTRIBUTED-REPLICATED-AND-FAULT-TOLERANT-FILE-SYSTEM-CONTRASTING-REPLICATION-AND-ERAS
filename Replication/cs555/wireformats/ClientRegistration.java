/*    */ package cs555.wireformats;
/*    */ 
import cs555.util.Protocol;
/*    */ import java.io.BufferedInputStream;
/*    */ import java.io.BufferedOutputStream;
/*    */ import java.io.ByteArrayInputStream;
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.io.DataInputStream;
/*    */ import java.io.DataOutputStream;
/*    */ import java.io.IOException;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class ClientRegistration
/*    */   implements Event
/*    */ {
/* 23 */   private byte type = Protocol.CLIENT_REGISTRATION;
/*    */   
/*    */   private byte[] ip;
/*    */   private int port;
/*    */   private byte[] data;
/*    */   
/*    */   ClientRegistration() {}
/*    */   
/*    */   public byte[] getIp()
/*    */   {
/* 33 */     return this.ip;
/*    */   }
/*    */   
/*    */   public void setIp(byte[] ip) {
/* 37 */     this.ip = ip;
/*    */   }
/*    */   
/*    */   public int getPort() {
/* 41 */     return this.port;
/*    */   }
/*    */   
/*    */   public void setPort(int port) {
/* 45 */     this.port = port;
/*    */   }
/*    */   
/*    */   public void setType(byte type) {
/* 49 */     this.type = type;
/*    */   }
/*    */   
/*    */   public void setData(byte[] incomingData) {
/* 53 */     this.data = incomingData;
/*    */   }
/*    */   
/*    */   public ClientRegistration(byte[] data) throws IOException {
/* 57 */     ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
/* 58 */     DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
/* 59 */     this.type = din.readByte();
/* 60 */     int iplength = din.readByte();
/* 61 */     this.ip = new byte[iplength];
/* 62 */     din.readFully(this.ip, 0, iplength);
/* 63 */     this.port = din.readInt();
/* 64 */     baInputStream.close();
/* 65 */     din.close();
/*    */   }
/*    */   
/*    */ 
/*    */   public byte[] getByte()
/*    */     throws Exception
/*    */   {
/* 72 */     byte[] marshalledBytes = null;
/* 73 */     ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
/* 74 */     DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
/* 75 */     dout.write(getType());
/* 76 */     byte[] ipbytes = this.ip;
/* 77 */     dout.write(ipbytes.length);
/* 78 */     dout.write(ipbytes);
/* 79 */     int port = this.port;
/* 80 */     dout.writeInt(port);
/* 81 */     dout.flush();
/* 82 */     marshalledBytes = baOutputStream.toByteArray();
/* 83 */     baOutputStream.close();
/* 84 */     dout.close();
/* 85 */     return marshalledBytes;
/*    */   }
/*    */   
/*    */   public byte getType()
/*    */   {
/* 90 */     return this.type;
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ClientRegistration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */