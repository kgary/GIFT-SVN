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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.AttributeValueEnumAccessor;
import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * Enumeration of the various Learner State Attribute names
 * 
 * @author mhoffman
 */
public class LearnerStateAttributeNameEnum extends AbstractEnum implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * the enumerated learner state bin this learner state attribute can appear in
     * @author mhoffman
     *
     */
    public enum LEARNER_STATE_CATEGORY{
        PERFORMANCE,    // current performance (e.g. DKF)
        COGNITIVE,      // thoughts/thinking, reasoning, remembering, beliefs - (more long term)
        AFFECTIVE       // moods, feelings, attitudes - (more short term)
    }
    
    /**
     * mapping of enumerated learner state category to the learner state attributes tagged with that category.
     */
    private static Map<LEARNER_STATE_CATEGORY, Set<LearnerStateAttributeNameEnum>> categoryMap = new HashMap<>();

    /**
     * sorted list by display name of all enumerations
     */
    private static List<LearnerStateAttributeNameEnum> enumList = new ArrayList<LearnerStateAttributeNameEnum>(2);
    
    /**
     * whether the {@link #enumList} has been sorted yet
     */
    private static boolean sorted = false;
    
    private static int index = 0;
    private static final AttributeValueEnumAccessor DEFAULT_ACCESSOR = LowMediumHighLevelEnum.ACCESSOR;

    /* Enum Name MUST NOT contain spaces or special characters, because the LRS 'extensions' JSON object requires the keys 
     * (which are the LearnerStateAttributeNameEnum names) be URIs which do not allow for spaces or special characters.*/
    
    public static final LearnerStateAttributeNameEnum ENGAGEMENT = new LearnerStateAttributeNameEnum("Engagement", "Engagement", EngagementLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum UNDERSTANDING = new LearnerStateAttributeNameEnum("Understanding", "Understanding", UnderstandingLevelEnum.ACCESSOR, true, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum AROUSAL = new LearnerStateAttributeNameEnum("Arousal", "Arousal", ArousalLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum LT_EXCITEMENT = new LearnerStateAttributeNameEnum("LongTermExcitement", "Long Term Excitement", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum ST_EXCITEMENT = new LearnerStateAttributeNameEnum("ShortTermExcitement", "Short Term Excitement", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum MEDITATION = new LearnerStateAttributeNameEnum("Meditation", "Meditation", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum FRUSTRATION = new LearnerStateAttributeNameEnum("Frustration", "Frustration", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    
    // Learner states needed for rapid miner integration task.
    public static final LearnerStateAttributeNameEnum ANXIOUS = new LearnerStateAttributeNameEnum("Anxious", "Anxious", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum BORED = new LearnerStateAttributeNameEnum("Bored", "Bored", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE); 
    public static final LearnerStateAttributeNameEnum CONFUSED = new LearnerStateAttributeNameEnum("Confused", "Confused", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum ENG_CONCENTRATION = new LearnerStateAttributeNameEnum("EngConcentration", "Eng. Concentration", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum OFFTASK = new LearnerStateAttributeNameEnum("OffTask", "OffTask", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    public static final LearnerStateAttributeNameEnum SURPRISED = new LearnerStateAttributeNameEnum("Surprised", "Surprised", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.AFFECTIVE);
    
    
    //Note: driven by the eMAP (Pedagogical Model)
    public static final LearnerStateAttributeNameEnum MOTIVATION = new LearnerStateAttributeNameEnum("Motivation", "Motivation", LowHighLevelEnum.ACCESSOR, true, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum PRIOR_KNOWLEDGE = new LearnerStateAttributeNameEnum("PriorKnowledge", "Prior Knowledge", ExpertiseLevelEnum.ACCESSOR, true, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum KNOWLEDGE = new LearnerStateAttributeNameEnum("Knowledge", "Knowledge", ExpertiseLevelEnum.ACCESSOR, true, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum SKILL = new LearnerStateAttributeNameEnum("Skill", "Skill", ExpertiseLevelEnum.ACCESSOR, true, LEARNER_STATE_CATEGORY.COGNITIVE);
    // Note: not really Cognitive, don't have a good learner state bin for this one
    public static final LearnerStateAttributeNameEnum SOCIO_ECONOMIC_STATUS = new LearnerStateAttributeNameEnum("SocioEconomicStatus", "Socio-Economic Status", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum LOCUS_OF_CONTROL = new LearnerStateAttributeNameEnum("LocusOfControl", "Locus of Control", LocusOfControlEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum LEARNER_ABILITY = new LearnerStateAttributeNameEnum("LearnerAbility", "Learner Ability", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum GENERAL_INTELLIGENCE = new LearnerStateAttributeNameEnum("GeneralIntelligence", "General Intelligence", DEFAULT_ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum LEARNING_STYLE = new LearnerStateAttributeNameEnum("LearningStyle", "Learning Style", LearningStyleEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum SELF_REGULATORY_ABILITY = new LearnerStateAttributeNameEnum("SelfRegulatoryAbility", "Self-Regulatory Ability", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum GRIT = new LearnerStateAttributeNameEnum("Grit", "Grit", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum SELF_EFFICACY = new LearnerStateAttributeNameEnum("SelfEfficacy", "Self-Efficacy", LearningStyleEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum GOAL_ORIENTATION = new LearnerStateAttributeNameEnum("GoalOrientation", "Goal Orientation", GoalOrientationEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    
    // Motivational Assessment Tool (MAT) from IST research
    public static final LearnerStateAttributeNameEnum MOTIVATION_LEARNER_DRIVEN = new LearnerStateAttributeNameEnum("MotivationLearnerDriven", "Motivation Learner Driven", LowMediumHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_CHALLENGE = new LearnerStateAttributeNameEnum("MotivationChallenge", "Motivation Challenge", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_LOSS_OF_EFFORT = new LearnerStateAttributeNameEnum("MotivationLossOfEffort", "Motivation Loss of Effort", LowMediumHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_FREQUENCY = new LearnerStateAttributeNameEnum("MotivationFrequency", "Motivation Frequency", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_FEAR_FREEZE_FLIGHT = new LearnerStateAttributeNameEnum("MotivationFearFreezeFlight", "Motivation Fear, Freeze, and Flight", LowMediumHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_COMPETITION = new LearnerStateAttributeNameEnum("MotivationCompetition", "Motivation Competition", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_RELATEDNESS = new LearnerStateAttributeNameEnum("MotivationRelatedness", "Motivation Relatedness", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_SOCIAL_LINK = new LearnerStateAttributeNameEnum("MotivationSocialLink", "Motivation Social Link", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_PUNISHMENT = new LearnerStateAttributeNameEnum("MotivationPunishment", "Motivation Punishment", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_BREAKS = new LearnerStateAttributeNameEnum("MotivationBreaks", "Motivation Breaks", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_FEEDBACK = new LearnerStateAttributeNameEnum("MotivationFeedback", "Motivation Feedback", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_TIME_DURING_LEARNING = new LearnerStateAttributeNameEnum("MotivationTimeDuringLearning", "Motivation Time During Learning", LowMediumHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_ENERGIZER = new LearnerStateAttributeNameEnum("MotivationEnergizer", "Motivation Energizer", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_SENSOR = new LearnerStateAttributeNameEnum("MotivationSensor", "Motivation Sensor", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_LOGICAL_CONSEQUENCE = new LearnerStateAttributeNameEnum("MotivationLogicalConsequence", "Motivation Logical Consequence", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_ACK_DIGITAL = new LearnerStateAttributeNameEnum("MotivationAcknowledgementDigital", "Motivation Acknowledgement / Digital", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_LOW_AND_HIGH_VALUE = new LearnerStateAttributeNameEnum("MotivationLowAndHighValue", "Motivation Low and High Value", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_TIME_AFTER_LEARNING = new LearnerStateAttributeNameEnum("MotivationTimeAfterLearning", "Motivation Time After Learning", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_HOBBY = new LearnerStateAttributeNameEnum("MotivationHobby", "Motivation Hobby", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_ACTIVITY = new LearnerStateAttributeNameEnum("MotivationActivity", "Motivation Activity", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);
    public static final LearnerStateAttributeNameEnum MOTIVATION_GOAL_ORIENTATION = new LearnerStateAttributeNameEnum("MotivationGoalOrientation", "Motivation Goal Orientation", LowHighLevelEnum.ACCESSOR, false, LEARNER_STATE_CATEGORY.COGNITIVE);

    // If any new learner state attributes are added here, you can give them icons in the addImageReferences() method 
    // of the mil.arl.gift.common.io.LearnerStateIconMap class.
    // New icons should be added to the mil.arl.gift.common.images.icons folder.
    
    /** the class containing the enumerated values for this attribute */
    private final AttributeValueEnumAccessor attributeAccessor;
    
    /**
     * This boolean flag indicates if the {@link LearnerStateAttributeNameEnum} is used exclusively
     * in relation to course concepts. This relation can cause course behaviors to act differently
     * (e.g. skipping surveys).
     */
    private final boolean exclusiveToConcepts;
    
    /**
     * the enumerated learner state bin this learner state attribute can appear in.  Can't be null.
     */
    private final LEARNER_STATE_CATEGORY learnerStateCategory;

    /**
     * Default Constructor
     *
     * Required to exist for GWT compatability
     */
    private LearnerStateAttributeNameEnum() {
        super();
        
        attributeAccessor = DEFAULT_ACCESSOR;
        learnerStateCategory = LEARNER_STATE_CATEGORY.PERFORMANCE;
        exclusiveToConcepts = false;
    }
    
    /**
     * Constructor.
     * 
     * @param name the key name for the enum. Must not contain spaces or special characters, because
     *            the LRS 'extensions' JSON object requires the keys (which are the
     *            LearnerStateAttributeNameEnum names) be URIs which do not allow for spaces or
     *            special characters.
     * @param displayName the display name for the enum.
     * @param attributeAccessor the accessor for the attribute value enums.
     * @param exclusiveToConcepts This boolean flag indicates if the enum is used exclusively in
     *            relation to course concepts.
     * @param learnerStateCategory the enumerated learner state bin this learner state attribute can appear in. Can't be null.
     */
    private LearnerStateAttributeNameEnum(String name, String displayName, AttributeValueEnumAccessor attributeAccessor,
            boolean exclusiveToConcepts, LEARNER_STATE_CATEGORY learnerStateCategory) {
        super(index++, name, displayName);

        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException("name can only contain letter characters but the name '" + name + "' was supplied");
            }
        }
        
        if(learnerStateCategory == null) {
            throw new IllegalArgumentException("The learner state category is null");
        }

        enumList.add(this);

        this.attributeAccessor = attributeAccessor == null ? DEFAULT_ACCESSOR : attributeAccessor;
        this.exclusiveToConcepts = exclusiveToConcepts;
        this.learnerStateCategory = learnerStateCategory;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        } else if (obj instanceof LearnerStateAttributeNameEnum) {

            LearnerStateAttributeNameEnum enumObj = (LearnerStateAttributeNameEnum) obj;

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
     * Gets the default value for a set of attribute values
     * 
     * @return AbstractEnum The default value of a set of attribute values
     */
    public AbstractEnum getAttributeDefaultValue() {        
        return attributeAccessor.DEFAULT_VALUE();
    }
    
    /**
     * Return the 'unknown' value which represents an enum for when a value
     * can't be calculated.  This is something that authoring tools could hide from authors.
     * 
     * @return can be null if an 'unknown' enum is not part of the values collection
     */
    public AbstractEnum getAttributeUnknownValue(){
        return attributeAccessor.getUnknownValue();
    }
    
    /**
     * Return the class containing the possible attribute values of this attribute.
     * 
     * @return wont be null or empty
     */
    public List<? extends AbstractEnum> getAttributeValues(){
        return attributeAccessor.VALUES();
    }
    
    /**
     * Return the class containing the possible attribute authorable values of this attribute.
     * 
     * @return wont be null, will be a subset of getAttributeValues()
     */
    public List<? extends AbstractEnum> getAttributeAuthoredValues(){
        return attributeAccessor.AUTHORABLE_VALUES();
    }
    
    /**
     * Return the attribute value instance referred to by the name provided for this learner state attribute.
     * 
     * @param valueName - a value of this learner state attribute
     * @return AbstractEnum - can be null if no instance was found.
     */
    public AbstractEnum getAttributeValue(String valueName){
        
        try{
            return AbstractEnum.valueOf(valueName, attributeAccessor.VALUES());
        }catch(EnumerationNotFoundException e){
            throw new EnumerationNotFoundException("Failed to find "+getClass().getName()+" object for '"+valueName+"'.", valueName, e);
        }        
    }

    /**
     * Returns the flag that indicates if the {@link LearnerStateAttributeNameEnum} is used
     * exclusively in relation to course concepts.
     * 
     * @return the exclusiveToConcepts
     */
    public boolean isExclusiveToConcepts() {
        return exclusiveToConcepts;
    }

    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static LearnerStateAttributeNameEnum valueOf(String name)
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
    public static LearnerStateAttributeNameEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<LearnerStateAttributeNameEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
    
    /**
     * Returns a list of sorted enumerations (sorted by display name alphabetically in ascending order).
     * 
     * @return sorted enumerations (sorted by display name alphabetically in ascending order).
     */
    public static final List<LearnerStateAttributeNameEnum> SORTED_VALUES() {

        if(!sorted) {
            Collections.sort(enumList, new Comparator<LearnerStateAttributeNameEnum>() {
    
                @Override
                public int compare(LearnerStateAttributeNameEnum attrib1, LearnerStateAttributeNameEnum attrib2) {
                    
                    String dn1 = attrib1.getDisplayName();
                    String dn2 = attrib2.getDisplayName();
                    
                    return dn1.compareTo(dn2);
                }
                
            });
        }
        
        sorted = true;
        
        return enumList;
    }
    
    /**
     * Return the enumerated learner state bin this learner state attribute can appear in. Can't be null.
     * @return won't be null
     */
    public LEARNER_STATE_CATEGORY getLearnerStateCategory() {
        return learnerStateCategory;
    }
    
    /**
     * Return the learner state attributes mapped to the provided category
     * @param category the enumerated learner state bin to get the set of learner state attributes that could go in that bin
     * @return the set of learner state attributes for that learner state category.  Returns null of the category is null. 
     * Can return an empty collection.
     */
    public static Set<LearnerStateAttributeNameEnum> getLearnerStateAttributesForCategory(LEARNER_STATE_CATEGORY category){
        
        if(category == null) {
            throw new IllegalArgumentException("The category is null");
        }
        
        Set<LearnerStateAttributeNameEnum> attrs = categoryMap.get(category);
        if(attrs == null) {
            // the static category map hasn't been populated yet
            
            // populate category map
            for(LearnerStateAttributeNameEnum attr : VALUES()) {
                
                LEARNER_STATE_CATEGORY categoryCandidate = attr.getLearnerStateCategory();
                Set<LearnerStateAttributeNameEnum> attrsCandidate = categoryMap.get(categoryCandidate);
                if(attrsCandidate == null) {
                    attrsCandidate = new HashSet<>();
                    categoryMap.put(categoryCandidate, attrsCandidate);
                }
                
                attrsCandidate.add(attr);
            }
            
            attrs = categoryMap.get(category);
        }
        
        return attrs;
    }
}
