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
 * The types of media that GIFT supports
 *
 * @author jleonard
 */
public class MediaTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<MediaTypeEnum> enumList = new ArrayList<MediaTypeEnum>(8);
    private static int index = 0;
    
    public static final MediaTypeEnum WEBPAGE		= new MediaTypeEnum("Webpage", "Web Resource");
    public static final MediaTypeEnum IMAGE			= new MediaTypeEnum("Image", "Image");
    public static final MediaTypeEnum VIDEO			= new MediaTypeEnum("Video", "Video");
    public static final MediaTypeEnum PDF			= new MediaTypeEnum("PDF", "PDF");
    public static final MediaTypeEnum YOUTUBE_VIDEO	= new MediaTypeEnum("YoutubeVideo", "Youtube Video");
    public static final MediaTypeEnum SLIDE_SHOW	= new MediaTypeEnum("SlideShow", "Slide Show");

    private MediaTypeEnum(String name, String displayName) {
        super(index++, name, displayName);
        enumList.add(this);
    }

    /**
     * Return the enumeration object that has the matching name.
     *
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     * name is not found.
     */
    public static MediaTypeEnum valueOf(String name)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     *
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     * value is not found.
     */
    public static MediaTypeEnum valueOf(int value)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static final List<MediaTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
