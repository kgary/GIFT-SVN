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
 * Enumeration of the various survey question scorer types
 * 
 * @author jleonard
 *
 */
public class SurveyScorerTypeEnum extends AbstractEnum implements Serializable {

    private static List<SurveyScorerTypeEnum> enumList = new ArrayList<SurveyScorerTypeEnum>(6);
    private static int index = 0;

    // Total - add up all of the individual weights
    public static final SurveyScorerTypeEnum TOTAL = new SurveyScorerTypeEnum("Total", "Total");
    
    // Scale - apply attribute(s) (and possibly value(s)) from survey response (e.g. NASA-TLX "Mental Demand")
    public static final SurveyScorerTypeEnum SCALE = new SurveyScorerTypeEnum("Scale", "Scale");
    
    // Rubric - in general this is where you take the earned weights and divide by highest total possible weight to get a 
    //          normalized type of value.
    public static final SurveyScorerTypeEnum RUBRIC = new SurveyScorerTypeEnum("Rubric", "Rubric");
    
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public because it is Serializable
     */
    public SurveyScorerTypeEnum() {
        super();
    }
    
    private SurveyScorerTypeEnum(String name, String displayName){
    	super(index++, name, displayName);
        enumList.add(this);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        } else if (obj instanceof SurveyScorerTypeEnum) {

            SurveyScorerTypeEnum enumObj = (SurveyScorerTypeEnum) obj;

            return enumObj.getValue() == getValue();
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 5;
        hash = hash * 31 + getValue();

        return hash;
    }

    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static SurveyScorerTypeEnum valueOf(String name)
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
    public static SurveyScorerTypeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<SurveyScorerTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
