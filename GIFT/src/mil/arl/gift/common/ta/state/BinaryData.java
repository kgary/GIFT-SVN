/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.util.Base64;

/**
 * This class wraps a generic binary data (blob).  The binary data is expected to be encoded in Big Endian form. 
* This is critical for libraries such as JDIS which assumes that the blob data is Big Endian.  
 * 
 * When writing blob data into this class, care must be taken to ensure that the writer writes the
 * data into Big Endian form.
 * 
 * @author nblomberg
 *
 */
public class BinaryData implements TrainingAppState {


    /** The binary data (byte array) */
    /** NOTE that this binary data is EXPECTED to be encoded in Big Endian format */
    private byte[] binaryData;
    
    /** The binary data type */
    private BinaryDataType dataType;
    
    /** Enum containing the types of supported binary data */
    public enum BinaryDataType {
        DisPdu;  // Binary Data is a DIS PDU (can be decoded using a library such as JDIS)
    }
    
    /** 
     * Constructor
     * 
     * @param dataType The type of binary data that is being stored.  Cannot be null.
     * @param binaryData The actual binary data (blob) to store.  Cannot be null.
     *   The blob MUST be encoded in Big Endian format.
     */
    public BinaryData(BinaryDataType dataType, byte[] binaryData){
        
        if (dataType == null) {
            throw new IllegalArgumentException("The dataType can't be null.");
        }
        
        if(binaryData == null){
            throw new IllegalArgumentException("The binaryData can't be null.");
        }
         
        this.binaryData = binaryData;
        this.dataType = dataType;
    }
    

    /**
     * Gets the binary data (blob) as a byte array.
     * The blob MUST be stored as Big Endian.
     * 
     * @return The binary data as a byte array.
     */
    public byte[] getData(){
        return binaryData;
    }

    /**
     * Encodes the binary string data (using base64 decoding).
     * 
     * @return
     */
    public String encodeAsString() {
        
        String binaryDataStr = "";
        if (binaryData != null) {
            binaryDataStr = Base64.getEncoder().encodeToString(binaryData);
        }
        
        return binaryDataStr;
    }
    
    /**
     * Returns the data type of the binary data.
     * 
     * @return The type of the binary data being stored.
     */
    public BinaryDataType getDataType(){
        return dataType;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BinaryData: ");
        sb.append("dataType = ").append(getDataType());
        sb.append("binaryData = ").append(encodeAsString());
        sb.append("]");
        return sb.toString();
    }
}
