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
 * Enumeration of the various Affective Feedback types.
 * These are usually associated with instructional interventions found in a DKF.
 * 
 * @author mhoffman
 *
 */
public class AffectiveFeedbackEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    /** complete set of enumerations of this class */
    private static List<AffectiveFeedbackEnum> enumList = new ArrayList<AffectiveFeedbackEnum>(2);
    private static int index = 0;

    public static final AffectiveFeedbackEnum MOTIVATION = new AffectiveFeedbackEnum("Motivation", "Motivation");
    public static final AffectiveFeedbackEnum POSITIVE = new AffectiveFeedbackEnum("Positive", "Positive");
    public static final AffectiveFeedbackEnum NEGATIVE = new AffectiveFeedbackEnum("Negative", "Negative");
    public static final AffectiveFeedbackEnum NONE = new AffectiveFeedbackEnum("None", "None");
 
    private static final AffectiveFeedbackEnum DEFAULT_VALUE = NONE;

    public static final AttributeValueEnumAccessor ACCESSOR = new AttributeValueEnumAccessor(enumList, enumList, DEFAULT_VALUE, null);

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public AffectiveFeedbackEnum() {
        super();
    }

    /**
     * Set attributes
     * 
     * @param name name - the unique name of this enumeration
     * @param displayName - the display name of this enumeration
     */
    private AffectiveFeedbackEnum(String name, String displayName){
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
    public static AffectiveFeedbackEnum valueOf(String name)
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
    public static AffectiveFeedbackEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<AffectiveFeedbackEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
