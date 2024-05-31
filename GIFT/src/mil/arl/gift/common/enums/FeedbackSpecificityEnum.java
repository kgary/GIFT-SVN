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
 * Enumeration of the various Feedback Specificity types.
 * These are usually associated with instructional interventions found in a DKF.
 * 
 * @author mhoffman
 *
 */
public class FeedbackSpecificityEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    /** complete set of enumerations of this class */
    private static List<FeedbackSpecificityEnum> enumList = new ArrayList<FeedbackSpecificityEnum>(2);
    private static int index = 0;

    public static final FeedbackSpecificityEnum HINT = new FeedbackSpecificityEnum("Hint", "Hint");
    public static final FeedbackSpecificityEnum PROMPT = new FeedbackSpecificityEnum("Prompt", "Prompt");
    public static final FeedbackSpecificityEnum PUMP = new FeedbackSpecificityEnum("Pump", "Pump");
    public static final FeedbackSpecificityEnum ASSERT = new FeedbackSpecificityEnum("Assert", "Assert");
    public static final FeedbackSpecificityEnum REFLECT = new FeedbackSpecificityEnum("Reflect", "Reflect");
 
    private static final FeedbackSpecificityEnum DEFAULT_VALUE = HINT;

    public static final AttributeValueEnumAccessor ACCESSOR = new AttributeValueEnumAccessor(enumList, enumList, DEFAULT_VALUE, null);

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public FeedbackSpecificityEnum() {
        super();
    }

    /**
     * Set attributes
     * 
     * @param name name - the unique name of this enumeration
     * @param displayName - the display name of this enumeration
     */
    private FeedbackSpecificityEnum(String name, String displayName){
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
    public static FeedbackSpecificityEnum valueOf(String name)
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
    public static FeedbackSpecificityEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<FeedbackSpecificityEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
