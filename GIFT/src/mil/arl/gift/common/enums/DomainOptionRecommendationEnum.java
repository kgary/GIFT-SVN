/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This class contains the various enumerated recommendations to a user about a domain option (i.e. course)
 * by a GIFT user.
 * 
 * @author mhoffman
 *
 */
public class DomainOptionRecommendationEnum extends AbstractEnum implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static List<DomainOptionRecommendationEnum> enumList = new ArrayList<DomainOptionRecommendationEnum>(2);
    private static int index = 0;

    public static final DomainOptionRecommendationEnum UNAVAILABLE_OTHER = new DomainOptionRecommendationEnum("UnavailableOther", "Unavailable - Other");
    public static final DomainOptionRecommendationEnum UNAVAILABLE_SURVEY_VALIDATION = new DomainOptionRecommendationEnum("UnavailableSurveyValidation", "Unavailable - Survey Validation");
    public static final DomainOptionRecommendationEnum UNAVAILABLE_LOGIC_VALIDATION = new DomainOptionRecommendationEnum("UnavailableLogicValidation", "Unavailable - Logic Validation");
    public static final DomainOptionRecommendationEnum UNAVAILABLE_RESTRICTED_TO_USER = new DomainOptionRecommendationEnum("UnavailableRestrictedToUser", "Unavailable - Restricted to User");
    public static final DomainOptionRecommendationEnum AVAILABLE_WITH_WARNING = new DomainOptionRecommendationEnum("AvailableWithWarning", "Available with warning", false);
    public static final DomainOptionRecommendationEnum RECOMMENDED = new DomainOptionRecommendationEnum("Recommended", "Recommended", false);
    public static final DomainOptionRecommendationEnum NOT_RECOMMENDED = new DomainOptionRecommendationEnum("NotRecommended", "Not Recommended", false);
    
    /** flag used to indicate if this enum is one of the unavailable types or not */
    private boolean isUnavailableType = true;
    
    /*
     * Default constructor (needed for gwt serialization)
     */
    public DomainOptionRecommendationEnum() {
        
    }
    
    private DomainOptionRecommendationEnum(String name, String displayName){
        super(index++, name, displayName);
        enumList.add(this);
    }
    
    private DomainOptionRecommendationEnum(String name, String displayName, boolean isUnavailableType){
        this(name, displayName);
        
        this.isUnavailableType = isUnavailableType;
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static DomainOptionRecommendationEnum valueOf(String name)
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
    public static DomainOptionRecommendationEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<DomainOptionRecommendationEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
    
    /**
     * Return whether this recommendation enumeration is an unavailable type, meaning the enumeration
     * represents one of the reasons the domain option would be unavailable possibly due to an issue.
     * 
     * @return boolean - whether this recommendation enumeration is an unavailable type
     */
    public boolean isUnavailableType(){
        return isUnavailableType;
    }
    
    @Override
    public boolean equals(Object other){
        
        if(other instanceof DomainOptionRecommendationEnum){
            
            DomainOptionRecommendationEnum otherEnum = (DomainOptionRecommendationEnum)other;
            
            return this.getName().equals(otherEnum.getName()) &&
                    this.getDisplayName().equals(otherEnum.getDisplayName());
        }
        
        return false;
    }
    
    @Override
    public int hashCode(){        
        return this.getValue();
    }
}
