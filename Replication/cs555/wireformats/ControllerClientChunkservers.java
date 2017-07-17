/*     */ package cs555.wireformats;
/*     */ 
import cs555.util.Protocol;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ControllerClientChunkservers
/*     */   implements Event
/*     */ {
/*  23 */   private byte type = Protocol.CONTROLLER_CLIENT_CHUNKSERVERS;
/*     */   
/*     */   private int nodeID;
/*     */   
/*     */   private byte[] chunkName;
/*     */   private int node1;
/*     */   private int node2;
/*     */   private int node3;
/*     */   private byte[] ip1;
/*     */   private byte[] ip2;
/*     */   private byte[] ip3;
/*     */   private int port1;
/*     */   private int port2;
/*     */   private int port3;

/*     */   
/*     */   ControllerClientChunkservers() {}
/*     */   

/*     */   public int getNodeID()
/*     */   {
/*  42 */     return this.nodeID;
/*     */   }
/*     */   
/*     */   public void setNodeID(int nodeID) {
/*  46 */     this.nodeID = nodeID;
/*     */   }
/*     */   
/*     */   public byte[] getChunkName() {
/*  50 */     return this.chunkName;
/*     */   }
/*     */   
/*     */   public void setChunkName(byte[] chunkName) {
/*  54 */     this.chunkName = chunkName;
/*     */   }
/*     */   
/*     */   public int getNode1() {
/*  58 */     return this.node1;
/*     */   }
/*     */   
/*     */   public void setNode1(int node1) {
/*  62 */     this.node1 = node1;
/*     */   }
/*     */   
/*     */   public int getNode2() {
/*  66 */     return this.node2;
/*     */   }
/*     */   
/*     */   public void setNode2(int node2) {
/*  70 */     this.node2 = node2;
/*     */   }
/*     */   
/*     */   public int getNode3() {
/*  74 */     return this.node3;
/*     */   }
/*     */   
/*     */   public void setNode3(int node3) {
/*  78 */     this.node3 = node3;
/*     */   }
/*     */   
/*     */   public byte[] getIp1() {
/*  82 */     return this.ip1;
/*     */   }
/*     */   
/*     */   public void setIp1(byte[] ip1) {
/*  86 */     this.ip1 = ip1;
/*     */   }
/*     */   
/*     */   public byte[] getIp2() {
/*  90 */     return this.ip2;
/*     */   }
/*     */   
/*     */   public void setIp2(byte[] ip2) {
/*  94 */     this.ip2 = ip2;
/*     */   }
/*     */   
/*     */   public byte[] getIp3() {
/*  98 */     return this.ip3;
/*     */   }
/*     */   
/*     */   public void setIp3(byte[] ip3) {
/* 102 */     this.ip3 = ip3;
/*     */   }
/*     */   
/*     */   public int getPort1() {
/* 106 */     return this.port1;
/*     */   }
/*     */   
/*     */   public void setPort1(int port1) {
/* 110 */     this.port1 = port1;
/*     */   }
/*     */   
/*     */   public int getPort2() {
/* 114 */     return this.port2;
/*     */   }
/*     */   
/*     */   public void setPort2(int port2) {
/* 118 */     this.port2 = port2;
/*     */   }
/*     */   
/*     */   public int getPort3() {
/* 122 */     return this.port3;
/*     */   }
/*     */   
/*     */   public void setPort3(int port3) {
/* 126 */     this.port3 = port3;
/*     */   }
/*     */   
/*     */   public ControllerClientChunkservers(byte[] data) throws IOException {
/* 130 */     ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
/* 131 */     DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
/* 132 */     this.type = din.readByte();
                this.nodeID = din.readInt();
                
/* 133 */     int nameLength = din.readInt();
/* 134 */     this.chunkName = new byte[nameLength];
/* 135 */     din.readFully(this.chunkName, 0, nameLength);

/* 136 */     this.node1 = din.readInt();
/* 137 */     this.node2 = din.readInt();
/* 138 */     this.node3 = din.readInt();
/*     */     
/* 140 */     int ip1Length = din.readInt();
/* 141 */     this.ip1 = new byte[ip1Length];
/* 142 */     din.readFully(this.ip1, 0, ip1Length);

/* 143 */     int ip2Length = din.readInt();
/* 144 */     this.ip2 = new byte[ip2Length];
/* 145 */     din.readFully(this.ip2, 0, ip2Length);

/* 146 */     int ip3Length = din.readInt();
/* 147 */     this.ip3 = new byte[ip3Length];
/* 148 */     din.readFully(this.ip3, 0, ip3Length);
/*     */     
/* 150 */     this.port1 = din.readInt();
/* 151 */     this.port2 = din.readInt();
/* 152 */     this.port3 = din.readInt();
/*     */     
/* 154 */     baInputStream.close();
/* 155 */     din.close();
/*     */   }
/*     */   
/*     */   public byte[] getByte() throws Exception
/*     */   {
/* 160 */     byte[] marshalledBytes = null;
/* 161 */     ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
/* 162 */     DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
/* 163 */     dout.write(getType());
                dout.writeInt(this.getNodeID());

/* 164 */     byte[] tempChunkName = this.chunkName;
/* 165 */     dout.writeInt(tempChunkName.length);
/* 166 */     dout.write(tempChunkName);

/* 167 */     dout.writeInt(this.node1);
/* 168 */     dout.writeInt(this.node2);
/* 169 */     dout.writeInt(this.node3);
/*     */     
/* 171 */     byte[] TempIP1 = this.ip1;
/* 172 */     dout.writeInt(TempIP1.length);
/* 173 */     dout.write(TempIP1);

/* 174 */     byte[] TempIP2 = this.ip2;
/* 175 */     dout.writeInt(TempIP2.length);
/* 176 */     dout.write(TempIP2);

/* 177 */     byte[] TempIP3 = this.ip3;
/* 178 */     dout.writeInt(TempIP3.length);
/* 179 */     dout.write(TempIP3);
/*     */     
/* 181 */     dout.writeInt(this.port1);
/* 182 */     dout.writeInt(this.port2);
/* 183 */     dout.writeInt(this.port3);
/*     */     
/* 185 */     dout.flush();
/* 186 */     marshalledBytes = baOutputStream.toByteArray();
/* 187 */     baOutputStream.close();
/* 188 */     dout.close();
/* 189 */     return marshalledBytes;
/*     */   }
/*     */   
/*     */   public byte getType()
/*     */   {
/* 194 */     return this.type;
/*     */   }
/*     */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ControllerClientChunkservers.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */