/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

/**
 * A wrapper around a schema object that defines a set of survey resources (i.e. survey context, question bank, concepts, etc.)
 * 
 * @author nroberts
 */
public abstract class AbstractSurveyResources {

    /**
     * Gets the survey context ID that surveys should use
     * 
     * @return the survey context ID
     */
    abstract public int getSurveyContextId();
    
    /**
     * Gets whether or not multiple schema objects share the same question bank
     * 
     * @return whether or not multiple schema objects share the same question bank
     */
    abstract public boolean hasSharedQuestionBank();
    
    /**
     * Gets the base course's list of concepts
     * 
     * @return the list of course concepts
     */
    public List<String> getConcepts(){
        return GatClientUtility.getBaseCourseConcepts();
    }
    
    /**
     * Gets whether or not the base course has any concepts
     * 
     * @return  whether or not there are any course concepts
     */
    public boolean hasConcepts() {
        
        List<String> concepts = getConcepts();
        
        return concepts != null && !concepts.isEmpty();
    }
}
