/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import generated.course.Course;
import generated.course.MerrillsBranchPoint;
import generated.course.PresentSurvey;
import generated.course.Recall;
import generated.course.PresentSurvey.ConceptSurvey;

import java.io.Serializable;


/**
 * This class contains utility methods for the gat client that interact with course object data.
 * 
 * @author nblomberg
 *
 */
public class GatClientCourseUtil {

    
    
    /**
     * Utility method to determine if a course has multiple transitions that contain question bank surveys.
     * 
     * @param course - The course object to check (cannot be null).
     * @return True if the course has multiple transitions that share a common question bank, false otherwise.
     */
    public static boolean hasSharedQuestionBank(Course course) {
        
        boolean hasSharedQuestionBank = false;
        
        if (course == null) {
            return hasSharedQuestionBank;
        }
        
        int possibleQuestionBankCount = 0;
        
        
        for(Serializable transition : course.getTransitions().getTransitionType()){
            
            if(transition instanceof PresentSurvey){
                PresentSurvey surveyTransition = (PresentSurvey)transition;
                if (surveyTransition.getSurveyChoice() instanceof ConceptSurvey) {
                    possibleQuestionBankCount++;
                } 
            } else if (transition instanceof MerrillsBranchPoint) {
                MerrillsBranchPoint bpTransition = (MerrillsBranchPoint)transition;
                
                for (Serializable quadrant : bpTransition.getQuadrants().getContent()) {
                    
                    if (quadrant instanceof Recall) {
                        Recall recallQuadrant = (Recall) quadrant;
                        
                        if (recallQuadrant != null && recallQuadrant.getPresentSurvey() != null && 
                                recallQuadrant.getPresentSurvey().getSurveyChoice() != null) {
                                possibleQuestionBankCount++;
                        }
                    }
                }
                
            }
        
            if (possibleQuestionBankCount > 1) {
                hasSharedQuestionBank = true;
                break;
            }
                
        }
        
        return hasSharedQuestionBank;
    }
    
    
}
