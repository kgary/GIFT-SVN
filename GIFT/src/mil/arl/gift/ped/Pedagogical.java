/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.ped.engine.TransitionsAgent;
import mil.arl.gift.ped.engine.LearnerCentricRecomPedagogicalModel;
import mil.arl.gift.ped.engine.ICAPAgent;

/**
 * This class is a container for the pedagogical model assigned to a unique user
 * executing on a specific domain session.
 * 
 * @author mhoffman
 *
 */
public class Pedagogical {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Pedagogical.class);
    
    /** the unique domain session for pedagogy instance */
    private DomainSession domainSession;

    /**
     * the state transition pedagogical model that uses the state transitions defined in an authored dkf
     */
    private final PedagogicalModel defaultModel = new TransitionsAgent(); 
    
    /**
     * the Interactive-Constructive-Active-Passive pedagogical model that determines the next activity
     * for an adaptive courseflow course object in a course.
     */
    private final PedagogicalModel icapAgent;
    
    /**
     * a pedagogical model used to determine if a course object should be skipped based on prior
     * learner state information
     */
    private final PedagogicalModel courseFlowModel = new LearnerCentricRecomPedagogicalModel();
    
    /** the list of models to use for this pedagogy */
    private final PedagogicalModel[] models;
    
    /**
     * Class constructor 
     * 
     * @param domainSession - information about the domain session (including the unique user id of the learner) 
     *                      this pedagogy instance is associated with
     * @throws DetailedException if there was a problem initializing any of the pedagogical models 
     */
    public Pedagogical(DomainSession domainSession) throws DetailedException {
    	
        this.domainSession = domainSession;
        
        // don't statically initialize this agent in case there are issues with the policy file,
        // this gives a better opportunity to present issues to the course taker
        try{
            icapAgent = new ICAPAgent();
        }catch(Exception e){
            throw new DetailedException("Failed to initialize the ICAP Agent due to an exception.", 
                    "The exception prevented the Pedagogical model from initializing for the domain session "+domainSession, e);
        }
        
        models = new PedagogicalModel[]{icapAgent, defaultModel, courseFlowModel};
    }
    
    /**
     * Return the unique domain session for this pedagogy.
     * 
     * @return the domain session for this pedagogy
     */
    public DomainSession getDomainSession(){
    	return domainSession;
    }    
    
    /**
     * Method to pass through the domain session initialization request to the pedagogical model.
     * 
     * @param initDomainSessionRequest request to initialize the domain session.
     */    
    public void initialize(InitializeDomainSessionRequest initDomainSessionRequest) {
            
        for(PedagogicalModel model : models){
            model.initialize(initDomainSessionRequest);
        }
    }  
    
    /**
     * Method to pass through the ped model initialization request to the pedagogical model.
     * 
     * @param initPedModelRequest Request to initialize a ped model.
     * @throws DetailedException - thrown if there was an issue when initializing the ped model
     */    
    public void initialize(InitializePedagogicalModelRequest initPedModelRequest) throws DetailedException {

        if(defaultModel == null){
            throw new DetailedException("The default pedagogical model failed to initialize successfully",
                    "There should be another exception detailing the reason why.  This exception was thrown because the Pedagogy is being initialized.",
                    null);
        }else if(icapAgent == null){
            throw new DetailedException("The eMAP failed to initialize successfully",
                    "There should be another exception detailing the reason why.  This exception was thrown because the Pedagogy is being initialized.",
                    null);
        }
        
        for(PedagogicalModel model : models){
            model.initialize(initPedModelRequest);
        }

    }
    
    /**
     * Notification that the lesson (DKF) has started in a GIFT course.  
     */
    public void handleLessonStarted() {
        
        for(PedagogicalModel model : models){
            
            if (model != null) {
                try {
                    model.handleLessonStarted();
                } catch (Exception e) {
                    throw new RuntimeException("Exception was caught trying to handle lesson started through the pedagogical model: " + model, e);
                }
            }
        }
    }
    
    /**
     * Handle a course state update.
     * 
     * @param state - update about the state of the course.
     * @return ped requests based on the new course state.  Can be empty but not null.
     */
    public PedagogicalRequest handleCourseStateUpdate(CourseState state){
        
        PedagogicalRequest request = new PedagogicalRequest();
        for(PedagogicalModel model : models){
            
            if (model != null) {

                try {
                    PedagogicalRequest modelRequest = model.handleCourseStateUpdate(state);
                    if (modelRequest != null && !modelRequest.getRequests().isEmpty()) {
                        // combine this model's requests with other model's requests into a single ped request 
                        // object for this method to return
                        
                        for(String reason : modelRequest.getRequests().keySet()){
                         
                            List<AbstractPedagogicalRequest> requests = request.getRequests().get(reason);
                            if(requests == null){
                                requests = new ArrayList<>();
                                request.getRequests().put(reason, requests);
                            }
                            
                            requests.addAll(modelRequest.getRequests().get(reason));
                        }

                    }
                } catch (Exception e) {
                    logger.error("Exception was caught trying to handle course state updates through the pedagogical model: " + model, e);
                }
            } else {
                logger.error("Unable to process update. Pedagogical model = " + model + " is null");
            }
        }
        
        return request;
    }
        
    /**
     * Handle the learner statue update sent by the learner module
     * 
     * @param state - a new learner state 
     */
    public void handleLearnerStateUpdate(LearnerState state){
    	
        PedagogicalRequest request = new PedagogicalRequest();
        for(PedagogicalModel model : models){
            
            if(model != null) {

                model.getPedagogicalActions(state, request);
                
                if(logger.isInfoEnabled()){
                    logger.info("After providing learner state to "+model+", the possible Ped Requests are " + request);  
                }
                
            }else {            
                logger.error("Unable to process update. Pedagogical model = "+model+" is null");
            }
        }
        
        if( !request.getRequests().isEmpty() ) {                                
            PedagogicalModule.getInstance().sendPedagogicalRequest(domainSession, request, null);              
        
        }else {
            
            if(logger.isInfoEnabled()){
                logger.info("No pedagogical requests from learner state update of: " + state);
            }
        } 

    }
}
