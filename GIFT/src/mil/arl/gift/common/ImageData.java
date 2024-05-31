/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.enums.ImageFormatEnum;

/**
 * The binary representation of an image
 *
 * @author jleonard
 */
public class ImageData {

    private byte[] data;

    private int width;

    private int height;

    private ImageFormatEnum format;
    
    /**
     * Constructor
     * 
     * @param data The binary image data
     * @param width The width of the image, in pixels
     * @param height The height of the image, in pixels
     * @param format The format of the image, in pixels 
     */
    public ImageData(byte[] data, int width, int height, ImageFormatEnum format) {

        if (data == null) {

            throw new IllegalArgumentException("The data cannot be null");
        }

        this.data = data;

        if (width <= 0) {

            throw new IllegalArgumentException("The width needs to be a positive value");
        }

        this.width = width;

        if (height <= 0) {

            throw new IllegalArgumentException("The height needs to be a positive value");
        }

        this.height = height;

        if (format == null) {

            throw new IllegalArgumentException("The image format cannot be null");
        }

        this.format = format;
    }

    /**
     * Gets the binary image data
     * 
     * @return byte[] The binary image data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the width of the image
     * 
     * @return int The width of the image, in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the image
     * 
     * @return int The height of the image, in pixels
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gets the format of the image
     * 
     * @return ImageFormatEnum The format of the image
     */
    public ImageFormatEnum getFormat() {
        return format;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[ImageData: ");
        sb.append(" data length = ").append(getData().length);
        sb.append(", width = ").append(getWidth());
        sb.append(", height = ").append(getHeight());
        sb.append(", format = ").append(getFormat());
        sb.append("]");

        return sb.toString();
    }
}
