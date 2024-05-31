/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy;

import generated.ped.Attribute;
import generated.ped.Attributes;
import generated.ped.EMAP;
import generated.ped.Example;
import generated.ped.MetadataAttribute;
import generated.ped.MetadataAttributes;
import generated.ped.Practice;
import generated.ped.Recall;
import generated.ped.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.GoalOrientationEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LearningStyleEnum;
import mil.arl.gift.common.enums.LocusOfControlEnum;
import mil.arl.gift.common.enums.LowHighLevelEnum;
import mil.arl.gift.common.enums.LowMediumHighLevelEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;

/**
 * Provides some static utility methods that didn't have a natural home elsewhere.
 * @author elafave
 *
 */
public class PedagogyConfigurationEditorUtility {

    /**
     * 
     * @return An EMAP with a default Attribute for each quadrant.
     */
    static public EMAP createDefaultPedagogyConfiguration() {
    	//Create the rule.
    	Rule rule = new Rule();
    	rule.setAttributes(createDefaultAttributes());

    	//Create the example
    	Example example = new Example();
    	example.setAttributes(createDefaultAttributes());
    	
    	//Create the recall
    	Recall recall = new Recall();
    	recall.setAttributes(createDefaultAttributes());
    	
    	//Create the practice
    	Practice practice = new Practice();
    	practice.setAttributes(createDefaultAttributes());
    	
    	//Compile those objects into an EMAP
    	EMAP pedagogyConfiguration = new EMAP();
    	pedagogyConfiguration.setRule(rule);
    	pedagogyConfiguration.setExample(example);
    	pedagogyConfiguration.setRecall(recall);
    	pedagogyConfiguration.setPractice(practice);
    	return pedagogyConfiguration;
    }
    
    /**
     * 
     * @return An Attributes object with a single attribute that possesses a
     * single MetadataAttribute.
     */
    static private Attributes createDefaultAttributes() {
    	MetadataAttribute metadataAttribute = new MetadataAttribute();
    	metadataAttribute.setValue(MetadataAttributeEnum.ANIMATION.getName());
    	
    	MetadataAttributes metadataAttributes = new MetadataAttributes();
    	metadataAttributes.getMetadataAttribute().add(metadataAttribute);
    	
    	Attribute attribute = new Attribute();
    	attribute.setType(LearnerStateAttributeNameEnum.ANXIOUS.getName());
    	attribute.setValue(LowMediumHighLevelEnum.MEDIUM.getName());
    	attribute.setMetadataAttributes(metadataAttributes);
    	
    	Attributes attributes = new Attributes();
    	attributes.getAttribute().add(attribute);
    	
    	return attributes;
    }
    
    /**
     * Each Learner State maps to an enum that can describe/quantify the
     * Learner State. This method encapsulates that mapping and returns the
     * class of the quantifying enum for the supplied learnerState
     * @param learnerState Learner State whose quantifying enum is desired.
     * @return Class of an enum that described/quantifies the learnerState.
     */
	static public Class<? extends AbstractEnum> getLearnerStateDescriptiveEnum(LearnerStateAttributeNameEnum learnerState) {
		if(learnerState == LearnerStateAttributeNameEnum.ANXIOUS ||
				learnerState == LearnerStateAttributeNameEnum.AROUSAL ||
				learnerState == LearnerStateAttributeNameEnum.BORED ||
				learnerState == LearnerStateAttributeNameEnum.CONFUSED ||
				learnerState == LearnerStateAttributeNameEnum.ENG_CONCENTRATION ||
				learnerState == LearnerStateAttributeNameEnum.ENGAGEMENT ||
				learnerState == LearnerStateAttributeNameEnum.FRUSTRATION ||
				learnerState == LearnerStateAttributeNameEnum.GENERAL_INTELLIGENCE ||
				learnerState == LearnerStateAttributeNameEnum.LEARNER_ABILITY ||
				learnerState == LearnerStateAttributeNameEnum.LT_EXCITEMENT ||
				learnerState == LearnerStateAttributeNameEnum.MEDITATION ||
				learnerState == LearnerStateAttributeNameEnum.OFFTASK ||
				learnerState == LearnerStateAttributeNameEnum.ST_EXCITEMENT ||
				learnerState == LearnerStateAttributeNameEnum.SOCIO_ECONOMIC_STATUS ||
				learnerState == LearnerStateAttributeNameEnum.SURPRISED ||
				learnerState == LearnerStateAttributeNameEnum.UNDERSTANDING) {
			return LowMediumHighLevelEnum.class;
		} else if(learnerState == LearnerStateAttributeNameEnum.GOAL_ORIENTATION) {
			return GoalOrientationEnum.class;
		} else if(learnerState == LearnerStateAttributeNameEnum.GRIT ||
				learnerState == LearnerStateAttributeNameEnum.MOTIVATION ||
				learnerState == LearnerStateAttributeNameEnum.SELF_REGULATORY_ABILITY) {
			return LowHighLevelEnum.class;
		} else if(learnerState == LearnerStateAttributeNameEnum.KNOWLEDGE ||
				learnerState == LearnerStateAttributeNameEnum.PRIOR_KNOWLEDGE ||
				learnerState == LearnerStateAttributeNameEnum.SKILL) {
			return ExpertiseLevelEnum.class;
		} else if(learnerState == LearnerStateAttributeNameEnum.LEARNING_STYLE ||
				learnerState == LearnerStateAttributeNameEnum.SELF_EFFICACY) {
			return LearningStyleEnum.class;
		} else if(learnerState == LearnerStateAttributeNameEnum.LOCUS_OF_CONTROL) {
			return LocusOfControlEnum.class;
		}
		
		return null;
	}
    
	/**
	 * Each attribute encapsulates a Learner State and an enum describing that
	 * state. This method returns every valid pairing of Learner State and
	 * the enum values it can be paired with that are NOT already contained
	 * within the supplied Attributes.
	 * @param attributes List of attributes where each attribute represents
	 * a pairing between a Learner State and an enum value describing that state.
	 * @return The remaining Learner State and enum value pairings that can
	 * be added to the supplied attributes.
	 */
    static public HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> getRemainingAttributePermutations(List<Attribute> attributes) {
    	HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> remaining = getAttributePermutations();
    	
    	//Deconstruct each attribute into a LearnerState and AbstractEnum and
    	//remove that pairing from the remaining attribute permutations.
    	for(Attribute attribute : attributes) {
    		String type = attribute.getType();
    		LearnerStateAttributeNameEnum learnerState = LearnerStateAttributeNameEnum.valueOf(type);
    		
    		String value = attribute.getValue();
    		AbstractEnum abstractEnum = getAbstractEnum(learnerState, value);
    		
    		ArrayList<AbstractEnum> enums = remaining.get(learnerState);
    		enums.remove(abstractEnum);
    	}
    	
    	//If the the map contains an entry that doesn't have any values left
    	//then go ahead and remove the entry.
    	Iterator<Map.Entry<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>>> i;
    	for(i = remaining.entrySet().iterator(); i.hasNext(); ) {
    		Map.Entry<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> entry = i.next();
    		if(entry.getValue().isEmpty()) {
    			i.remove();
    		}
    	}
    	
    	return remaining;
    }
    
    /**
     * 
     * @return A HashMap that represents all of the valid pairings between the
     * Learner State and the corresponding enum describing that state.
     */
    static private HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> getAttributePermutations() {
    	HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>> permutations = new HashMap<LearnerStateAttributeNameEnum, ArrayList<AbstractEnum>>();
    	
    	List<LearnerStateAttributeNameEnum> learnerStates = LearnerStateAttributeNameEnum.VALUES();
    	for(LearnerStateAttributeNameEnum learnerState : learnerStates) {
    		Class<? extends AbstractEnum> descriptiveEnumClass = PedagogyConfigurationEditorUtility.getLearnerStateDescriptiveEnum(learnerState);
        	
    		//TODO We can probably do this with reflection.
        	if(descriptiveEnumClass == LowMediumHighLevelEnum.class) {
        		permutations.put(learnerState, new ArrayList<AbstractEnum>(LowMediumHighLevelEnum.VALUES()));
        	} else if(descriptiveEnumClass == GoalOrientationEnum.class) {
        		permutations.put(learnerState, new ArrayList<AbstractEnum>(GoalOrientationEnum.VALUES()));
        	} else if(descriptiveEnumClass == LowHighLevelEnum.class) {
        		permutations.put(learnerState, new ArrayList<AbstractEnum>(LowHighLevelEnum.VALUES()));
        	} else if(descriptiveEnumClass == ExpertiseLevelEnum.class) {
        		permutations.put(learnerState, new ArrayList<AbstractEnum>(ExpertiseLevelEnum.VALUES()));
        	} else if(descriptiveEnumClass == LearningStyleEnum.class) {
        		permutations.put(learnerState, new ArrayList<AbstractEnum>(LearningStyleEnum.VALUES()));
        	} else if(descriptiveEnumClass == LocusOfControlEnum.class) {
        		permutations.put(learnerState, new ArrayList<AbstractEnum>(LocusOfControlEnum.VALUES()));
        	}
    	}
    	
    	return permutations;
    }
    
    /**
     * Each Learner State can be described by a particular subclass of
     * AbstractEnum. Given the Learner State and the String representation of
     * the descriptive enum this method is able to return the enumeration.
     * @param learnerState Learner State the supplied enumName describes.
     * @param enumName String representation of an enum that describes the
     * supplied learnerState.
     * @return The AbstractEnum that describes the supplied learnerState and
     * whose String representation is defined by the supplied enumName.
     */
    static private AbstractEnum getAbstractEnum(LearnerStateAttributeNameEnum learnerState, String enumName) {
    	Class<? extends AbstractEnum> descriptiveEnumClass = PedagogyConfigurationEditorUtility.getLearnerStateDescriptiveEnum(learnerState);
    	
    	//TODO We can probably do this with reflection.
    	if(descriptiveEnumClass == LowMediumHighLevelEnum.class) {
    		return LowMediumHighLevelEnum.valueOf(enumName);
    	} else if(descriptiveEnumClass == GoalOrientationEnum.class) {
			return GoalOrientationEnum.valueOf(enumName);
    	} else if(descriptiveEnumClass == LowHighLevelEnum.class) {
			return LowHighLevelEnum.valueOf(enumName);
    	} else if(descriptiveEnumClass == ExpertiseLevelEnum.class) {
			return ExpertiseLevelEnum.valueOf(enumName);
    	} else if(descriptiveEnumClass == LearningStyleEnum.class) {
			return LearningStyleEnum.valueOf(enumName);
    	} else if(descriptiveEnumClass == LocusOfControlEnum.class) {
			return LocusOfControlEnum.valueOf(enumName);
    	}
    	
    	return null;
    }
}
