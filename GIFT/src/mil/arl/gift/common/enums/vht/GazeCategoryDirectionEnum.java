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
 * An enumeration of the gaze category direction in VHT
 *
 * @author jleonard
 */
public class GazeCategoryDirectionEnum extends AbstractEnum {
    
    private static final long serialVersionUID = 1L;

    private static List<GazeCategoryDirectionEnum> enumList = new ArrayList<GazeCategoryDirectionEnum>(2);

    private static int index = 0;

    public static final GazeCategoryDirectionEnum TOWARDS = new GazeCategoryDirectionEnum("towards", "Towards");

    public static final GazeCategoryDirectionEnum AWAY = new GazeCategoryDirectionEnum("away", "Away");

    private GazeCategoryDirectionEnum(String name, String displayName) {
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
    public static GazeCategoryDirectionEnum valueOf(String name)
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
    public static GazeCategoryDirectionEnum valueOf(int value)
            throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static final List<GazeCategoryDirectionEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
