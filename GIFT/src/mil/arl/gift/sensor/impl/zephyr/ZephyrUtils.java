/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl.zephyr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the Zephyr Technologies library. Used when interfacing with
 * the Zephyr BioHarness sensor.
 */
public class ZephyrUtils {
    
    /**
     * Reference to the Logger object.
     */
    private static Logger logger = LoggerFactory.getLogger(ZephyrUtils.class);

    /** 8 Bit CRC polynomial */
    public final static int CRC_8 = 0x8C;
    
    /** Start of text */
    public final static byte STX = 0x02;

    /** End of text */
    public final static byte ETX = 0x03;

    /**
     * Do a CRC check on supplied packet
     * 
     * @param packet the packet on which to perform CRC
     * @return true if error free, otherwise return false
     */
    public static boolean checkCRC(byte[] packet) {

        boolean result = false;

        if (packet != null && packet.length >= 5) {
            
            if (packet[0] == STX) {
                
                // Index 0 : STX, Index 1 : Msg Type, Index 2 : Size
                int dataSize = byteAsInteger(packet[2]);
                int crcIndex = dataSize + 3;
                int startIndex = 3;

                if (packet.length > crcIndex) {

                    int crc = 0;
                    for (int i = startIndex; i < crcIndex; i++) {
                        crc = crcPushByte(crc, byteAsInteger(packet[i]));
                    }

                    /** Then compare to the packet CRC */
                    if (crc == byteAsInteger(packet[crcIndex])) {
                        result = true;
                    } else {
                        logger.error("Calculated: " + crc + ", read: " + packet[crcIndex]);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates the CRC value for the provided packet.
     * 
     * @param packet the packet for which the CRC should be generated
     * @return the generated CRC value
     */
    public static int generateCRC(byte[] packet) {

        int crc = 0;
        
        if (packet != null && packet.length >= 5) {

            if (packet[0] == STX) {

                // Index 0 : STX, Index 1 : Msg Type, Index 2 : Size
                int dataSize = byteAsInteger(packet[2]);
                int crcIndex = dataSize + 3;
                int startIndex = 3;

                if (packet.length > crcIndex) {
                    for (int i = startIndex; i < crcIndex; i++) {
                        crc = crcPushByte(crc, byteAsInteger(packet[i]));
                    }
                }
            }
        }

        return crc;
    }

    /**
     * CRC check taken from Zephyr PDF's.
     * 
     * Updates an existing CRC with a new byte.
     * 
     * @param crc the CRC value before considering the new byte
     * @param newByte the new byte value to be applied to the CRC
     * @return the new CRC value.
     */
    private static int crcPushByte(int crc, int newByte) {

        int newCRC = crc ^ newByte;

        for (int bit = 0; bit < 8; bit++) {
            if ((newCRC & 1) == 1) {
                newCRC = ((newCRC >> 1) ^ CRC_8);
            } else {
                newCRC = (newCRC >> 1);
            }
        }

        return newCRC;
    }

    /**
     * Returns the value of the byte as an integer (interprets the byte as an unsigned value).
     * 
     * @param b the byte to convert
     * @return an integer representation of the byte
     */
    public static int byteAsInteger(byte b) {
        return (b & 0xff);
    }

    /**
     * Merge two bytes into a signed 2's complement integer
     * 
     * @param low the least significant byte
     * @param high the most significant byte
     * @return a signed int value 
     */
    public static int merge(byte low, byte high) {
        int b = 0;

        b += (high << 8) + low;
        if ((high & 0x80) != 0) {
            b = -(0xffffffff - b);
        }
        return b;
    }

    /**
     * Convert a byte to a hex string.
     * 
     * @param data the byte to convert
     * @return the hex string for the byte
     */
    public static String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Convert a byte array to a hex string.
     * 
     * @param data the byte array to convert
     * @return the byte array as a hex string
     */
    public static String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
        }
        return buf.toString();
    }
    
    
    /**
     * Computes a hex string representation for a segment of a byte array
     * 
     * @param data the byte array to process
     * @param startIndex the starting index for processing
     * @param count the number of bytes in the array to process
     * @return the hex string representation for the specified bytes
     */
    public static String bytesToHex(byte[] data, int startIndex, int count) {
        StringBuffer buf = new StringBuffer();
        if (data.length >= (startIndex + count)) {
            for (int i = startIndex; i < count + startIndex; i++) {
                buf.append(byteToHex(data[i]));
            }
        }
        return buf.toString();
    }

    /**
     * Convert an int to a hex char.
     * 
     * @param i the integer to convert
     * @return the hex char for the provided integer
     */
    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9))
            return (char) ('0' + i);
        else
            return (char) ('a' + (i - 10));
    }
}
