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
import mil.arl.gift.common.AttributeValueEnumAccessor;
import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This enum class encapsulates the supported css resolutions (units) that are supported for the video properties
 * in GIFT.  
 * 
 * @author nblomberg
 *
 */
public class VideoCssUnitsEnum extends AbstractEnum {

    /** complete set of enumerations of this class */
    private static final List<VideoCssUnitsEnum> enumList = new ArrayList<VideoCssUnitsEnum>(4);
    private static int index = 0;

    public static final VideoCssUnitsEnum PIXELS = new VideoCssUnitsEnum("px", "Pixels (px)");
    public static final VideoCssUnitsEnum PERCENT = new VideoCssUnitsEnum("%", "Percent (%)");
    public static final VideoCssUnitsEnum VIEWPORT_WIDTH = new VideoCssUnitsEnum("vw", "% Vpt Width (vw)");
    public static final VideoCssUnitsEnum VIEWPORT_HEIGHT = new VideoCssUnitsEnum("vh", "% Vpt Height (vh)");

    private static final VideoCssUnitsEnum DEFAULT_VALUE = PIXELS;

    public static final AttributeValueEnumAccessor ACCESSOR = new AttributeValueEnumAccessor(enumList, enumList, DEFAULT_VALUE, null);
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public VideoCssUnitsEnum() {
        super();
    }
    
    /**
     * Constructor 
     * @param name Name of the enum object.
     * @param displayName The display name of the enum object.
     */
    private VideoCssUnitsEnum(String name, String displayName){
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
    public static VideoCssUnitsEnum valueOf(String name)
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
    public static VideoCssUnitsEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<VideoCssUnitsEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
