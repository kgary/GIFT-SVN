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
import mil.arl.gift.common.util.StringUtils;


/**
 * Enumeration of the various levels of assessment
 * 
 * @author mhoffman
 *
 */
public class AssessmentLevelEnum extends AbstractEnum implements Serializable {

    private static List<AssessmentLevelEnum> enumList = new ArrayList<AssessmentLevelEnum>(2);
    private static int index = 0;

    /* ORDER MATTERS. The index order is used to determine priority. */

    public static final AssessmentLevelEnum BELOW_EXPECTATION = new AssessmentLevelEnum("BelowExpectation", "Below Expectation");
    public static final AssessmentLevelEnum AT_EXPECTATION = new AssessmentLevelEnum("AtExpectation", "At Expectation");
    public static final AssessmentLevelEnum ABOVE_EXPECTATION = new AssessmentLevelEnum("AboveExpectation", "Above Expectation");
    public static final AssessmentLevelEnum UNKNOWN = new AssessmentLevelEnum("Unknown", "Unknown");
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    public AssessmentLevelEnum() {
        
    }
    
    private AssessmentLevelEnum(String name, String displayName){
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
    public static AssessmentLevelEnum valueOf(String name)
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
    public static AssessmentLevelEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<AssessmentLevelEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
    
    /**
     * Whether this enumeration is considered poor performance among the enumerations here.
     * @return true if this enumeration object is considered poor performance.
     */
    public boolean isPoorPerforming(){
        return this.equals(BELOW_EXPECTATION);
    }
    
    /**
     * Return whether this enumeration means the assessment is at or above standards/expectation.
     * @return does this enumeration is {@link #AT_EXPECTATION} or {@link #ABOVE_EXPECTATION}
     */
    public boolean hasReachedStandards() {
        return this.equals(AT_EXPECTATION) || this.equals(ABOVE_EXPECTATION);
    }
    
    /**
     * Return whether the 'other' assessment is better than this assessment enumeration.
     * @param other the assessment to compare this enumeration too
     * @return true if the other assessment is better than this assessment
     * e.g. would return true if this enum value is {@link #AT_EXPECTATION} and other is {@link #ABOVE_EXPECTATION}
     * If they are the same, false is returned.
     */
    public boolean isBetterAssessment(AssessmentLevelEnum other) {
        
        if(this.equals(ABOVE_EXPECTATION)) {
            // can't get better than this
            return false;
        }else if(other != null) {
            if(other.equals(ABOVE_EXPECTATION)) {
                // other beats this (which is not Above)
                return true;
            }else if(this.equals(AT_EXPECTATION)) {
                // this is equal to or better than other (which is not Above)
                return false;
            }else if(other.equals(AT_EXPECTATION)) {
                // other beats this (which is not Above or At)
                return true;
            }else if(this.equals(BELOW_EXPECTATION)) {
                // this is equal to or better than other (which is not above or at)
                return false;
            }else if(other.equals(BELOW_EXPECTATION)) {
                // other beats this which is Unknown
                return true;
            }else {
                // this and other are unknown
                return false;
            }
        }else {
            return true;
        }

    }
    
    /**
     * Returns an enum value from this class for the PassFailEnum unique name value provided.
     * This was created when PassFailEnum was removed. (#5197)
     * @param passFailEnumName values supported are "PASS", "FAIL", "UNKNOWN", "INCOMPLETE". 
     * @return if null/empty is provided {@link #UNKNOWN} is returned.  "PASS" results in {@link #AT_EXPECTATION}.
     * "FAIL" results in {@link #BELOW_EXPECTATION}. All other values {@link #UNKNOWN}.
     */
    public static AssessmentLevelEnum fromPassFailEnum(String passFailEnumName) {
        
        if(StringUtils.isBlank(passFailEnumName)) {
            return UNKNOWN;
        }else if(passFailEnumName.equals("PASS")) {
            return AT_EXPECTATION;
        }else if(passFailEnumName.equals("FAIL")) {
            return BELOW_EXPECTATION;
        }else {
            return UNKNOWN;
        }
    }
}
