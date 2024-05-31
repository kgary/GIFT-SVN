/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.domain.knowledge.Concept;

/**
 * This trigger is used to determine if the concept's assessment matches the authored assessment
 * 
 * @author bzahid
 *
 */
public class ConceptAssessmentTrigger extends AbstractTrigger {
	
	private static Logger logger = LoggerFactory.getLogger(ConceptAssessmentTrigger.class);

	/** the concept looking for a finish state */
	private Concept concept;
	
	/** the goal assessment level for this concept to be in*/
	private AssessmentLevelEnum goalAssessment;
	
	/**
	 * Class constructor - set attributes
	 * 
	 * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
	 * @param concept - the concept looking for a finish state
	 * @param goalAssessment the goal assessment level for this concept to be in
	 */
	public ConceptAssessmentTrigger(String triggerName, Concept concept, AssessmentLevelEnum goalAssessment) {
	    super(triggerName);
		
		if(concept == null){
			throw new IllegalArgumentException("The concept can't be null");
		}
		
		this.concept = concept;
		
		if(goalAssessment == null){
	          throw new IllegalArgumentException("The concept goal assessment can't be null");
		}
		
		this.goalAssessment = goalAssessment;
	}

	/**
	 * Checks if this trigger should activate
	 * 
	 * @param concept - the concept looking for a finish state
	 * 
	 * @return true if this trigger should activate, false if it should not
	 */
	@Override
	public boolean shouldActivate(Concept concept) {
		boolean activate = false;
		
		/* Get authored assessment */
		ConceptAssessment cAssessment = concept.getAssessment();
		
		/* If concept's assessment matches authored assessment, then activate */
		if(this.concept.getNodeId() == concept.getNodeId() && goalAssessment == cAssessment.getAssessmentLevel()) {
			activate = true;
			logger.debug("Activating " + this + " because the concept reached its");
		}
		
		return activate;
	}
	
	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		sb.append("[ConceptAssessmentTrigger: ");
		sb.append(super.toString());
		sb.append(", concept = ").append(concept);
		sb.append(", goalAssessment = ").append(goalAssessment);
		sb.append("]");
		
		return sb.toString();
	}
}
