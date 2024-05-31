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
 * The different scales a survey can be associated with
 *
 * @author jleonard
 */
public class SurveyItemScaleEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<SurveyItemScaleEnum> enumList = new ArrayList<>(6);

    private static int index = 0;

    /**
     * Agent Persona Instrument (API) attributes
     */
    public static final SurveyItemScaleEnum CREDIBLE = new SurveyItemScaleEnum("Credible", "Credible", "API");

    public static final SurveyItemScaleEnum FACILITATE_LEARNING = new SurveyItemScaleEnum("FacilitateLearning", "Facilitate Learning", "API");

    public static final SurveyItemScaleEnum MENTOR_LIKE = new SurveyItemScaleEnum("MentorLike", "Mentor Like", "API");

    public static final SurveyItemScaleEnum ENGAGING = new SurveyItemScaleEnum("Engaging", "Engaging", "API");

    public static final SurveyItemScaleEnum HUMAN_LIKE = new SurveyItemScaleEnum("HumanLike", "Human Like", "API");
    
    /**
     * ITC SOPI attributes
     */
    public static final SurveyItemScaleEnum PHYS_SPACE = new SurveyItemScaleEnum("PhysicalSpace", "Sensor of Physical Space", "ITC SOPI");

    public static final SurveyItemScaleEnum ENGAGEMENT = new SurveyItemScaleEnum("Engagement", "Engagement", "ITC SOPI");

    public static final SurveyItemScaleEnum ECO_VALIDITY = new SurveyItemScaleEnum("EcologicalValidity", "Ecological Validity", "ITC SOPI");

    public static final SurveyItemScaleEnum NEGATIVE_EFFECTS = new SurveyItemScaleEnum("NegativeEffects", "Negative Effects", "ITC SOPI");

    /**
     * NASA TLX attributes
     */
    public static final SurveyItemScaleEnum MENTAL = new SurveyItemScaleEnum("Mental", "Mental", "NASA TLX");

    public static final SurveyItemScaleEnum PHYSICAL = new SurveyItemScaleEnum("Physical", "Physical", "NASA TLX");

    public static final SurveyItemScaleEnum TEMPORAL = new SurveyItemScaleEnum("Temporal", "Temporal", "NASA TLX");

    public static final SurveyItemScaleEnum PERFORMANCE = new SurveyItemScaleEnum("Performance", "Performance", "NASA TLX");

    public static final SurveyItemScaleEnum EFFORT = new SurveyItemScaleEnum("Effort", "Effort", "NASA TLX");

    public static final SurveyItemScaleEnum FRUSTRATION = new SurveyItemScaleEnum("Frustration", "Frustration", "NASA TLX");

    private String groupName;

    private SurveyItemScaleEnum(String name, String displayName, String groupName) {
        super(index++, name, displayName);

        this.groupName = groupName;

        enumList.add(this);
    }

    /**
     * Gets the name of the group this scale is in
     *
     * @return String The name of the group this scale is in
     */
    public String getGroupName() {

        return groupName;
    }

    /**
     * Return the enumeration object that has the matching name.
     *
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     * name is not found.
     */
    public static SurveyItemScaleEnum valueOf(String name)
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
    public static SurveyItemScaleEnum valueOf(int value)
            throws EnumerationNotFoundException {

        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     *
     * @return a List of the currently defined enumerations.
     */
    public static final List<SurveyItemScaleEnum> VALUES() {

        return Collections.unmodifiableList(enumList);
    }
}
