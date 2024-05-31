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
 * This class contains the various enumerated reasons for a domain option (i.e. course) not being available for selection
 * by a GIFT user.
 * 
 * @author mhoffman
 *
 */
public class DomainOptionUnavailableEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<DomainOptionUnavailableEnum> enumList = new ArrayList<DomainOptionUnavailableEnum>(2);
    private static int index = 0;

    public static final DomainOptionUnavailableEnum OTHER = new DomainOptionUnavailableEnum("Other", "Other");
    public static final DomainOptionUnavailableEnum SURVEY_VALIDATION = new DomainOptionUnavailableEnum("SurveyValidation", "Survey Validation");
    public static final DomainOptionUnavailableEnum RESTRICTED_TO_USER = new DomainOptionUnavailableEnum("RestrictedToUser", "Restricted to User");
    
    private DomainOptionUnavailableEnum(String name, String displayName){
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
    public static DomainOptionUnavailableEnum valueOf(String name)
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
    public static DomainOptionUnavailableEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<DomainOptionUnavailableEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
