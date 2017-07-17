/*    */ package cs555.wireformats;
/*    */ 
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
/*    */ public class ChunkServerRegistration
/*    */   implements Event
/*    */ {
/* 23 */   private byte type = 2;
/*    */   
/*    */   private byte[] ip;
/*    */   private int port;
/*    */   private long freeSpace;
/*    */   
/*    */   ChunkServerRegistration() {}
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
/*    */   public long getFreeSpace() {
/* 49 */     return this.freeSpace;
/*    */   }
/*    */   
/*    */   public void setFreeSpace(long freeSpace) {
/* 53 */     this.freeSpace = freeSpace;
/*    */   }
/*    */   
/*    */   public ChunkServerRegistration(byte[] data)
/*    */     throws IOException
/*    */   {
/* 59 */     ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
/* 60 */     DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
/* 61 */     this.type = din.readByte();
/* 62 */     int iplength = din.readByte();
/* 63 */     this.ip = new byte[iplength];
/* 64 */     din.readFully(this.ip, 0, iplength);
/* 65 */     this.port = din.readInt();
/* 66 */     this.freeSpace = din.readLong();
/* 67 */     baInputStream.close();
/* 68 */     din.close();
/*    */   }
/*    */   
/*    */ 
/*    */   public byte[] getByte()
/*    */     throws Exception
/*    */   {
/* 75 */     byte[] marshalledBytes = null;
/* 76 */     ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
/* 77 */     DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
/* 78 */     dout.write(getType());
/* 79 */     byte[] ipbytes = this.ip;
/* 80 */     dout.write(ipbytes.length);
/* 81 */     dout.write(ipbytes);
/* 82 */     int port = this.port;
/* 83 */     dout.writeInt(port);
/* 84 */     dout.writeLong(this.freeSpace);
/* 85 */     dout.flush();
/* 86 */     marshalledBytes = baOutputStream.toByteArray();
/* 87 */     baOutputStream.close();
/* 88 */     dout.close();
/* 89 */     return marshalledBytes;
/*    */   }
/*    */   
/*    */   public byte getType()
/*    */   {
/* 94 */     return this.type;
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ChunkServerRegistration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */