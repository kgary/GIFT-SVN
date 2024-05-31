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
 * This class contains the various enumerated detonation result types
 * 
 * @author mhoffman
 *
 */
public class DetonationResultEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<DetonationResultEnum> enumList = new ArrayList<DetonationResultEnum>(2);
    private static int index = 0;

    public static final DetonationResultEnum OTHER = new DetonationResultEnum("Other", "Other");
    public static final DetonationResultEnum ENTITY_IMPACT = new DetonationResultEnum("EntityImpact", "Entity Impact");
    public static final DetonationResultEnum NONE = new DetonationResultEnum("None", "None");
    
    /**
     * default - required for GWT serialization
     */
    private DetonationResultEnum(){}
    
    private DetonationResultEnum(String name, String displayName){
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
    public static DetonationResultEnum valueOf(String name)
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
    public static DetonationResultEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<DetonationResultEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
