/*    */ package cs555RS.wireformats;
/*    */

import cs555RS.util.Protocol;
/*    */ import java.io.BufferedInputStream;
/*    */ import java.io.BufferedOutputStream;
/*    */ import java.io.ByteArrayInputStream;
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.io.DataInputStream;
/*    */ import java.io.DataOutputStream;
/*    */ import java.io.IOException;
/*    */
/*    */
/*    */ public class ControllerRegistrationStatus
        /*    */ implements Event /*    */ {
    /* 15 */ private byte type = Protocol.CONTROLLER_REGISTRATION_STATUS;
    /*    */    private int nodeID;
    /*    */    private String info;
    /*    */    private byte[] data;
    /*    */
    /*    */ public ControllerRegistrationStatus(byte[] data) throws IOException {
        /* 21 */ ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
        /* 22 */ DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        /* 23 */ this.type = din.readByte();
        /* 24 */ this.nodeID = din.readInt();
        /* 25 */ int infoLength = din.readByte();
        /* 26 */ byte[] temp = new byte[infoLength];
        /* 27 */ din.readFully(temp, 0, infoLength);
        /* 28 */ this.info = new String(temp);
        /* 29 */ baInputStream.close();
        /* 30 */ din.close();
        /*    */    }
    /*    */
    /*    */
    /*    */ public ControllerRegistrationStatus() {
    }
    /*    */
    /*    */
    /*    */ public byte[] getByte()
            /*    */ throws IOException /*    */ {
        /* 40 */ byte[] marshalledBytes = null;
        /* 41 */ ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        /* 42 */ DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        /* 43 */ dout.write(getType());
        /* 44 */ dout.writeInt(this.nodeID);
        /* 45 */ dout.write(this.info.length());
        /* 46 */ dout.write(this.info.getBytes());
        /* 47 */ dout.flush();
        /* 48 */ marshalledBytes = baOutputStream.toByteArray();
        /* 49 */ baOutputStream.close();
        /* 50 */ dout.close();
        /* 51 */ return marshalledBytes;
        /*    */    }
    /*    */
    /*    */
    /*    */ public byte getType() /*    */ {
        /* 57 */ return this.type;
        /*    */    }
    /*    */
    /*    */ public int getNodeID() {
        /* 61 */ return this.nodeID;
        /*    */    }
    /*    */
    /*    */ public void setNodeID(int nodeID) {
        /* 65 */ this.nodeID = nodeID;
        /*    */    }
    /*    */
    /*    */ public String getInfo() /*    */ {
        /* 70 */ return this.info;
        /*    */    }
    /*    */
    /*    */ public void setInfo(String info) {
        /* 74 */ this.info = info;
        /*    */    }
    /*    */ }


/* Location:              C:\Users\YANK\Dropbox\CSU\cs555a1.jar!\cs555\wireformats\ControllerRegistrationStatus.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */
