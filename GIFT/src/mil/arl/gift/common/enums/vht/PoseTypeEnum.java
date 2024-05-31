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
 * An enumeration of the pose states in VHT
 *
 * @author jleonard
 */
public class PoseTypeEnum extends AbstractEnum {
    
    private static final long serialVersionUID = 1L;

    private static List<PoseTypeEnum> enumList = new ArrayList<PoseTypeEnum>(2);

    private static int index = 0;

    public static final PoseTypeEnum MOVING = new PoseTypeEnum("moving", "moving");

    public static final PoseTypeEnum NOT_MOVING = new PoseTypeEnum("not_moving", "not_moving");

    private PoseTypeEnum(String name, String displayName) {
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
    public static PoseTypeEnum valueOf(String name)
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
    public static PoseTypeEnum valueOf(int value)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static final List<PoseTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
