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

import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;

/**
 * This trigger is used to determine if a specific concept has finished.
 * 
 * @author mhoffman
 *
 */
public class ConceptEndedTrigger extends AbstractTrigger {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ConceptEndedTrigger.class);
    
    /** the concept looking for a finish state */
    private Concept concept;
    
    /**
     * Class constructor - set attributes
     * 
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param concept the concept looking for a finish state
     */
    public ConceptEndedTrigger(String triggerName, Concept concept) throws RuntimeException{
        super(triggerName);
        
        if(concept == null){
            throw new IllegalArgumentException("The concept can't be null");
        }
        
        this.concept = concept;
        
        validate();
    }
    
    /**
     * Check whether the concept's conditions can all complete by calling 
     * {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * 
     * @throws RuntimeException when at least one of the concept's condition doesn't complete.
     */
    private void validate() throws RuntimeException{
        
        if(!hasOnlyConditionsThatCanComplete(concept)){
            throw new RuntimeException("The concept end trigger will never activate because the '"+concept.getName()+
                    "' contains at least one condition that will never completed.");
        }
    }
    
    /**
     * Return whether all conditions in the provided parameter can call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * @param conditionsOrConcepts either a {@link generated.dkf.Concepts} or {@link generated.dkf.Conditions} object
     * to check all conditions implementations against the server's list of conditions that can complete.
     * @return true if all conditions found in the given parameters can complete.  Also returns false if the 
     * conditionsOrConcepts is null.
     */
    public static boolean hasOnlyConditionsThatCanComplete(Concept concept){
        
        if(concept == null){
            return false;
        }
        
        if(concept instanceof IntermediateConcept){
            
            IntermediateConcept iConcept = (IntermediateConcept)concept;
            for(Concept subConcept : iConcept.getConcepts()){
                
                if(!hasOnlyConditionsThatCanComplete(subConcept)){
                    return false;
                }
            }
            
        }else{
            
            for(AbstractCondition condition : concept.getConditions()){
                
                if(!condition.canComplete()){
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public boolean shouldActivate(Concept concept){
        
        boolean activate = false;
        if(this.concept == concept && concept.isFinished()){
            activate = true;
            if(logger.isDebugEnabled()){
                logger.debug("Activating "+this+" because concept finished");
            }
        }
        
        return activate;
    }    

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ConceptEndedTrigger: ");
        sb.append(super.toString());  
        sb.append(", concept = ").append(concept);
        sb.append("]");
        
        return sb.toString();
    }

}
