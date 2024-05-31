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
 * This class represents an enumeration of an articulation parameter's type designator field.
 *
 * @author dscrane
 *
 */
public class ArticulationParameterTypeDesignatorEnum extends AbstractEnum  {

    private static final List<ArticulationParameterTypeDesignatorEnum> enumList = new ArrayList<ArticulationParameterTypeDesignatorEnum>(2);
    private static int index = 0;

    public static final ArticulationParameterTypeDesignatorEnum ARTICULATED_PART = new ArticulationParameterTypeDesignatorEnum("ArticulatedPart", "ArticulatedPart");
    public static final ArticulationParameterTypeDesignatorEnum ATTACHED_PART = new ArticulationParameterTypeDesignatorEnum("AttachedPart", "AttachedPart");
    
    private static final long serialVersionUID = 1L;

    private ArticulationParameterTypeDesignatorEnum(String name, String displayName) {
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
    public static ArticulationParameterTypeDesignatorEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return ArticulationParameterTypeDesignatorEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static ArticulationParameterTypeDesignatorEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return ArticulationParameterTypeDesignatorEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static List<ArticulationParameterTypeDesignatorEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
