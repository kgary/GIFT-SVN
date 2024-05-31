/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums.vht;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AbstractEnum;

/**
 * An enumeration of the posture types in VHT
 *
 * @author jleonard
 */
public class PostureTypeEnum extends AbstractEnum {
    
    private static final long serialVersionUID = 1L;

    private static List<PostureTypeEnum> enumList = new ArrayList<PostureTypeEnum>(5);

    private static int index = 0;

    public static final PostureTypeEnum LEANING_FORWARD = new PostureTypeEnum("leaning_forward", "leaning_forward");

    public static final PostureTypeEnum LEANING_BACKWARD = new PostureTypeEnum("leaning_backward", "leaning_backward");

    public static final PostureTypeEnum UPRIGHT = new PostureTypeEnum("upright", "upright");
    
    public static final PostureTypeEnum TILTED = new PostureTypeEnum("tilted", "tilted");
    
    public static final PostureTypeEnum ROCKING = new PostureTypeEnum("rocking", "rocking");

    private PostureTypeEnum(String name, String displayName) {
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
    public static PostureTypeEnum valueOf(String name)
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
    public static PostureTypeEnum valueOf(int value)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static final List<PostureTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
