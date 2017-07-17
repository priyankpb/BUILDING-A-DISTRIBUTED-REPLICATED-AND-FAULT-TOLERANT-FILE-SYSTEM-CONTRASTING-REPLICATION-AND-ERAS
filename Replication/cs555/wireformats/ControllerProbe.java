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
/*    */ public class ControllerProbe
        /*    */ implements Event /*    */ {
    /* 23 */ private byte type = Protocol.CONTROLLER_PROBE;
    /*    */    private int nodeID;
    /*    */
    /*    */ public ControllerProbe(byte[] data) throws IOException {
        /* 27 */ ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        /* 28 */ DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        /* 29 */ this.type = din.readByte();
        /* 30 */ this.nodeID = din.readInt();
        /* 31 */ baInputStream.close();
        /* 32 */ din.close();
        /*    */    }
    /*    */
    /*    */
    /*    */ public ControllerProbe() {
    }
    /*    */
    /*    */
    /*    */ public byte[] getByte()
            /*    */ throws IOException /*    */ {
        /* 42 */ byte[] marshalledBytes = null;
        /* 43 */ ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        /* 44 */ DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        /* 45 */ dout.write(getType());
        /* 46 */ dout.writeInt(this.nodeID);
        /* 47 */ dout.flush();
        /* 48 */ marshalledBytes = baOutputStream.toByteArray();
        /* 49 */ baOutputStream.close();
        /* 50 */ dout.close();
        /* 51 */ return marshalledBytes;
        /*    */    }
    /*    */
    /*    */ public byte getType() /*    */ {
        /* 56 */ return this.type;
        /*    */    }
    /*    */
    /*    */ public int getNodeID() {
        /* 60 */ return this.nodeID;
        /*    */    }
    /*    */
    /*    */ public void setNodeID(int nodeID) {
        /* 64 */ this.nodeID = nodeID;
        /*    */    }
    /*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ControllerProbe.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */
