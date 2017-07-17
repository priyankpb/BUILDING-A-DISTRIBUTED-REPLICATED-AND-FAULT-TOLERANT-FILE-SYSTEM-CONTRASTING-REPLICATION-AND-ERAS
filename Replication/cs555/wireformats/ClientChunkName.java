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
/*    */ public class ClientChunkName
/*    */   implements Event
/*    */ {
/* 23 */   private byte type = Protocol.CLIENT_CHUNK_NAME;
/*    */   private int nodeID;
/*    */   private byte[] data;
/*    */   
/*    */   public int getNodeID()
/*    */   {
/* 29 */     return this.nodeID;
/*    */   }
/*    */   
/*    */   public void setNodeID(int nodeID) {
/* 33 */     this.nodeID = nodeID;
/*    */   }
/*    */   
/*    */   public byte[] getData() {
/* 37 */     return this.data;
/*    */   }
/*    */   
/*    */   public void setData(byte[] data) {
/* 41 */     this.data = data;
/*    */   }

/*    */   public ClientChunkName() {
/* 59 */     
/*    */   }
/*    */   
/*    */   public ClientChunkName(byte[] data) throws IOException
/*    */   {
/* 47 */     ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
/* 48 */     DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
/* 49 */     this.type = din.readByte();
/* 50 */     this.nodeID = din.readInt();
/* 51 */     int length = din.readInt();
/* 52 */     this.data = new byte[length];
/* 53 */     din.readFully(this.data, 0, length);
/* 54 */     baInputStream.close();
/* 55 */     din.close();
/*    */   }
/*    */   
/*    */   
/*    */   public byte[] getByte() throws Exception
/*    */   {
/* 64 */     byte[] marshalledBytes = null;
/* 65 */     ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
/* 66 */     DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
/* 67 */     dout.write(getType());
/* 68 */     dout.writeInt(this.nodeID);
/* 69 */     byte[] data = this.data;
/* 70 */     dout.writeInt(data.length);
/* 71 */     dout.write(data);
/* 72 */     dout.flush();
/* 73 */     marshalledBytes = baOutputStream.toByteArray();
/* 74 */     baOutputStream.close();
/* 75 */     dout.close();
/* 76 */     return marshalledBytes;
/*    */   }
/*    */   
/*    */   public byte getType()
/*    */   {
/* 81 */     return this.type;
/*    */   }
/*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ClientChunkName.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */