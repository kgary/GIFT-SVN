/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

/**
 * This is the interface for performance assessment nodes that can handle concept lesson assessments.
 * 
 * @author mhoffman
 *
 */
public interface ConceptLessonAssessmentHandlerInterface {

    /**
     * This method is used to inform the parent performance assessment node would like additional assessing (if available) 
     * and that the concept should execute further or different assessments in regards to the 
     * knowledge/interface it has at it's disposal.  For instance, a concept may want to perform additional assessment
     * logic such as re-evaluating its previous assessments or using a new algorithm to potentially provide a different 
     * assessment value for the parent performance assessment node.
     */
    public void assessConcepts();
}
