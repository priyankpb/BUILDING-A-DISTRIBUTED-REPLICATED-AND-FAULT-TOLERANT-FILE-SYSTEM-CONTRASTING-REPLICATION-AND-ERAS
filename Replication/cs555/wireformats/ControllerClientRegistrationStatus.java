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
/*    */ public class ControllerClientRegistrationStatus
/*    */   implements Event
/*    */ {
/* 22 */   private byte type = 53;
/*    */   private int nodeID;
/*    */   private String info;
/*    */   
/*    */   public ControllerClientRegistrationStatus(byte[] data) throws IOException {
/* 27 */     ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
/* 28 */     DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
/* 29 */     this.type = din.readByte();
/* 30 */     this.nodeID = din.readInt();
/* 31 */     int infoLength = din.readByte();
/* 32 */     byte[] temp = new byte[infoLength];
/* 33 */     din.readFully(temp, 0, infoLength);
/* 34 */     this.info = new String(temp);
/* 35 */     baInputStream.close();
/* 36 */     din.close();
/*    */   }
/*    */   
/*    */ 
/*    */   public ControllerClientRegistrationStatus() {}
/*    */   
/*    */ 
/*    */   public byte[] getByte()
/*    */     throws IOException
/*    */   {
/* 46 */     byte[] marshalledBytes = null;
/* 47 */     ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
/* 48 */     DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
/* 49 */     dout.write(getType());
/* 50 */     dout.writeInt(this.nodeID);
/* 51 */     dout.write(this.info.length());
/* 52 */     dout.write(this.info.getBytes());
/* 53 */     dout.flush();
/* 54 */     marshalledBytes = baOutputStream.toByteArray();
/* 55 */     baOutputStream.close();
/* 56 */     dout.close();
/* 57 */     return marshalledBytes;
/*    */   }
/*    */   
/*    */ 
/*    */   public byte getType()
/*    */   {
/* 63 */     return this.type;
/*    */   }
/*    */   
/*    */   public int getNodeID() {
/* 67 */     return this.nodeID;
/*    */   }
/*    */   
/*    */   public void setNodeID(int nodeID) {
/* 71 */     this.nodeID = nodeID;
/*    */   }
/*    */   
/*    */   public String getInfo()
/*    */   {
/* 76 */     return this.info;
/*    */   }
/*    */   
/*    */   public void setInfo(String info) {
/* 80 */     this.info = info;
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ControllerClientRegistrationStatus.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */