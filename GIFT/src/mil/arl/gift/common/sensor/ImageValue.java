/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import mil.arl.gift.common.ImageData;
import mil.arl.gift.common.enums.ImageFormatEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import org.apache.commons.codec.binary.Base64;

/**
 * Represents an image sensor attribute value.
 *
 * @author jleonard
 */
public class ImageValue extends AbstractSensorAttributeValue {

    /**
     * Class constructor - set attributes.
     *
     * @param name - the name of the sensor attribute
     * @param value - the value of the sensor attribute
     */
    public ImageValue(SensorAttributeNameEnum name, ImageData value) {
        super(name, value);
    }

    /**
     * Gets the binary image data
     *
     * @return byte[] The binary image data
     */
    public byte[] getData() {

        return getImageData().getData();
    }

    /**
     * Gets the width of the image
     *
     * @return int The width of the image, in pixels
     */
    public int getWidth() {

        return getImageData().getWidth();
    }

    /**
     * Gets the height of the image
     *
     * @return int The height of the image, in pixels
     */
    public int getHeight() {

        return getImageData().getHeight();
    }

    /**
     * Gets the format of the image
     *
     * @return ImageFormatEnum The format of the image
     */
    public ImageFormatEnum getFormat() {
        return getImageData().getFormat();
    }

    /**
     * Gets the image data for this value
     *
     * @return ImageData The image data for this value
     */
    public ImageData getImageData() {

        return (ImageData) value;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Data Length = ").append(getData().length);
        sb.append(", Width = ").append(getWidth());
        sb.append(", Height = ").append(getHeight());
        sb.append(", Format = ").append(getFormat().getName());
        return sb.toString();
    }

    /**
     * Overriding this method because the data needs to be base 64 encoded to be
     * represented as a String
     *
     * @return The string representation of the data
     */
    @Override
    public String toDataString() {

        String dataBase64 = Base64.encodeBase64String(getData());
        
        StringBuilder sb = new StringBuilder();
        sb.append(dataBase64);
        sb.append(", ").append(getWidth());
        sb.append(", ").append(getHeight());
        sb.append(", ").append(getFormat().getName());
        return sb.toString();
    }
}
