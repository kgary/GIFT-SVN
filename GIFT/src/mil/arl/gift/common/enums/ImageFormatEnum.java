/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This class contains the various formats of images
 * 
 * @author jleonard
 */
public class ImageFormatEnum extends AbstractEnum {

    private static List<ImageFormatEnum> enumList = new ArrayList<ImageFormatEnum>(2);
    private static int index = 0;

    public static final ImageFormatEnum KINECT_DEPTH = new ImageFormatEnum("Kinect_Depth", "Kinect Depth");
    public static final ImageFormatEnum KINECT_INFRARED = new ImageFormatEnum("Kinect_Infrared", "Kinect Infrared");
    public static final ImageFormatEnum BAYER_GRGB = new ImageFormatEnum("Bayer_GRGB", "Bayer GRGB");
    public static final ImageFormatEnum RGB_8_BGRA = new ImageFormatEnum("RGB_8_BGRA", "RGB 8 BGRA");
    public static final ImageFormatEnum YUV_422_UYVY = new ImageFormatEnum("YUV_422_UYVY", "YUV 422 UYVY");
    
    private static final long serialVersionUID = 1L;
    
    private ImageFormatEnum(String name, String displayName){
        super(index++, name, displayName);
        enumList.add(this);
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static ImageFormatEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static ImageFormatEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<ImageFormatEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
