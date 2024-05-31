/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.learner.Input;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum.LEARNER_STATE_CATEGORY;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyScaleScore;
import mil.arl.gift.common.survey.score.SurveyScorerManager;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.learner.clusterer.AbstractClassifier;
import mil.arl.gift.learner.clusterer.ClassifierConfiguration;
import mil.arl.gift.learner.clusterer.GenericClassifier;
import mil.arl.gift.learner.clusterer.KnowledgeClassifier;
import mil.arl.gift.learner.clusterer.SkillClassifier;
import mil.arl.gift.learner.clusterer.TaskPerformanceStateClassifier;
import mil.arl.gift.learner.clusterer.data.AbstractSensorTranslator;
import mil.arl.gift.learner.predictor.AbstractBasePredictor;
import mil.arl.gift.learner.predictor.GenericPredictor;
import mil.arl.gift.learner.predictor.KnowledgePredictor;
import mil.arl.gift.learner.predictor.SkillPredictor;
import mil.arl.gift.learner.predictor.TaskPerformanceStatePredictor;

/**
 * This class represents a learner in the system.  It contains information
 * obtained on the learner both before a domain session and during a domain session. 
 * In addition, this class contains the necessary learner state managers used to classify
 * and predict the learner's state.
 * 
 * @author mhoffman
 *
 */
public class Learner {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Learner.class);

    /** the default learner configuration file name to parse */
    private static final String DEFAULT_LEARNER_CONFIG_FILENAME =  LearnerModuleProperties.getInstance().getLearnerConfigurationFilename();
    private static final File DEFAULT_LEARNER_CONFIG_FILE = new File(DEFAULT_LEARNER_CONFIG_FILENAME);

    /** information about the user session this learner instance is associated with */
    private UserSession userSession;
    
    /** the current domain session this learner is executing.  Can be null. */
    private DomainSession currentDomainSession = null;
    
    private LMSCourseRecords lmsRecords = new LMSCourseRecords();
    
    /**
     * time at which the last LMS records where received by this Learner instance
     */
    private Date lastLMSQuery = null;            

    /** mapping of statemanagers to each <input> tag defined in the learnerconfiguration.xml. */
    private Map<String, LearnerStateAttributeManager> inputNameToStateManager = new HashMap<String, LearnerStateAttributeManager>();
    
    /** mapping of unique task/concept name to learner state manager instance which is responsible for determining learner state */
    private Map<String, LearnerStateAttributeManager> perfCategoryNameToStateManager = new HashMap<String, LearnerStateAttributeManager>();
    
    private Map<LearnerStateAttributeNameEnum, List<LearnerStateAttributeManager>> attributeToManagers = new HashMap<>();
    
    /** the current learner state states */
    private PerformanceState currentPerformance = new PerformanceState();
	private CognitiveState currentCognitive = new CognitiveState();
    private AffectiveState currentAffective = new AffectiveState();
    
    /** Maintain a list of misconfigured inputs */
    private List<String> misconfiguredInputs = new ArrayList<>();
    
    /** Used to set and retrieve learner configuration information */
    private LearnerConfigFileHandler fileHandler = null;
    
    /** the current knowledge session (DKF) this learner is involved in (can be null) */
    private AbstractKnowledgeSession currKnowledgeSession;

    /**
     * Class constructor 
     * 
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      this learner instance is associated with
     * @param configXmlContent - the xml learner configuration to use for this session. Can be null.
     * @throws DetailedException if there is an error parsing the xml content.
     */
    public Learner(UserSession userSession, String configXmlContent) throws DetailedException{
    	this.userSession = userSession;
    	
	    	if(configXmlContent != null) {	 
                fileHandler = new LearnerConfigFileHandler(configXmlContent, null, true);
	    	} else {
                try {
                    fileHandler = new LearnerConfigFileHandler(new FileProxy(DEFAULT_LEARNER_CONFIG_FILE), true);
                } catch (FileNotFoundException e) {
                    throw new DetailedException("Failed to find the default learner configuration file",
                            "The default learner configuration file of "+DEFAULT_LEARNER_CONFIG_FILENAME+" doesn't exist.",
                            e);
                }
	    	}

    }
    
    /**
     * Init method. Sends the learner state to the pedagogical module.
     */
    public void init() {
        sendLearnerState();
    }
    
    /**
     * Set the current domain session this learner is execution.
     * 
     * @param domainSession can be null if this learner isn't in a domain session
     */
    public void setCurrentDomainSession(DomainSession domainSession){
        this.currentDomainSession = domainSession;
        
        // NOTE: need to clear out previous performance state that can be carried over from a previous
        // domain session.  Not doing this prevents new course concepts from being properly tracked, thereby
        // preventing after recall remediation, etc.
        currentPerformance.getTasks().clear();

        if(logger.isInfoEnabled()) {
            logger.info("Cleared learner state performance state data for start of domain session for "+userSession+".");
        }
        
        for(List<LearnerStateAttributeManager> mgrs : attributeToManagers.values()){
            
            for(LearnerStateAttributeManager mgr : mgrs){
                mgr.domainSessionStarted();
            }
        }
    }
    
    /**
     * Set the current knowledge session (DKF) this learner is involved in.
     * @param knowledgeSession contains information about the current knowledge session.  Can be null.
     */
    public void setCurrentKnowledgeSession(AbstractKnowledgeSession knowledgeSession){        
        this.currKnowledgeSession = knowledgeSession;
    }
    
    /**
     * Return the current knowledge session this learner is involved in.
     * @return contains information about the current knowledge session.  Can be null.
     */
    public AbstractKnowledgeSession getCurrentKnowledgeSession(){
        return this.currKnowledgeSession;
    }
    
    /**
     * Return the unique user information for this learner instance
     * 
     * @return user information for the user of this Learner instance
     */
    public UserSession getUserSessionInfo(){
    	return userSession;
    }
    
    /**
     * Add more records to the existing records
     * 
     * @param lmsCourseRecords - LMS history
     */
    public void addLMSCourseRecords(LMSCourseRecords lmsCourseRecords){
        
        for(LMSCourseRecord record : lmsCourseRecords.getRecords()){
            addLMSCourseRecord(record, false);
        }    
    }
    
    /**
     * Add a single LMS course record to the existing records.  Set the 'updateLearnerState'
     * to true if the record should update the learner state at this time (if the statue is updated
     * a learner state message will be sent).
     * 
     * @param lmsCourseRecord the record to add
     * @param updateLearnerState whether or not to use the course record to update the learner state
     */
    public void addLMSCourseRecord(LMSCourseRecord lmsCourseRecord, boolean updateLearnerState){
        
        // remove nodes that have nothing to do with this learner (e.g. the learner may have been in a team
        // session that was scored and other learners could have scoring info in this object)
        // Note: make a copy of the grade structure in case it is delivered to other Learner instances as part of a team session
        LMSCourseRecord learnerSpecificRecord = LMSCourseRecord.deepCopy(lmsCourseRecord);
        learnerSpecificRecord.getRoot().removeUnrelatedScores(userSession.getUsername());
        
        //add to records known about this learner
        this.lmsRecords.addRecord(learnerSpecificRecord);
        
        if(updateLearnerState){

            if(updateLearnerStateFromLMSRecords()){
                
                if(logger.isInfoEnabled()){
                    logger.info("Learner State Manager has determined the learner state has changed because of performance assessment, creating learner state message");
                }
    
                sendLearnerState();
            }
        }        

    }
    
    /**
     * Analyze the known course records for this learner and use them to update the learner
     * state. 
     * 
     * @return boolean whether or not the learner state was updated 
     */
    private boolean updateLearnerStateFromLMSRecords(){
        
        boolean stateChanged = false;        
        
        //
        //update the learner state based on the records known
        //
        
        //first sort the records so the newest records are at the end
        this.lmsRecords.sort();
        
        //for now just apply the last record in the sorted list
        LMSCourseRecord record = this.lmsRecords.getRecords().get(this.lmsRecords.getRecords().size()-1);
                   
        //
        // important - update the skill (cognitive) state based on the course record
        //
        List<LearnerStateAttributeManager> skillMgrs = getSkillManagers();
        
        for(LearnerStateAttributeManager mgr : skillMgrs){
            
            try{
                stateChanged |= mgr.updateState(record);
            }catch(Throwable t){
                logger.error("Caught exception from mis-behaving learner state attribute manager of "+mgr+" while processing LMS records for "+userSession, t);
            }
        }
        ///////////////

        if(stateChanged){
            //the performance assessment change, therefore create learner state update            
            for(LearnerStateAttributeManager mgr : skillMgrs){
                updateLearnerState((LearnerStateAttribute)mgr.getState());
            }
        }

        return stateChanged;
    }
    
    /**
     * Return the collection of attribute managers for {@link LearnerStateAttributeNameEnum.KNOWLEDGE}.  If none
     * are assigned, create the pipeline for a new manager.
     * @return the collection of attribute managers for knowledge.
     */
    private List<LearnerStateAttributeManager> getKnowledgeManagers(){
        
        List<LearnerStateAttributeManager> knowledgeMgrs = attributeToManagers.get(LearnerStateAttributeNameEnum.KNOWLEDGE);
        if(knowledgeMgrs == null){
            
            knowledgeMgrs = new ArrayList<>();
            AbstractClassifier classifier = new KnowledgeClassifier();
            LearnerStateAttributeManager knowledgeMgr = new LearnerStateAttributeManager(LearnerStateAttributeNameEnum.KNOWLEDGE, classifier, new KnowledgePredictor(classifier));
            knowledgeMgrs.add(knowledgeMgr);
            
            attributeToManagers.put(LearnerStateAttributeNameEnum.KNOWLEDGE, knowledgeMgrs);
        }
        
        return knowledgeMgrs;
    }
    
    /**
     * Return the collection of attribute managers for {@link LearnerStateAttributeNameEnum.SKILL}.  If none
     * are assigned, create the pipeline for a new manager.
     * @return the collection of attribute managers for skill.
     */
    private List<LearnerStateAttributeManager> getSkillManagers(){
        
        List<LearnerStateAttributeManager> skillMgrs = attributeToManagers.get(LearnerStateAttributeNameEnum.SKILL);
        if(skillMgrs == null){
            
            skillMgrs = new ArrayList<>();
            AbstractClassifier classifier = new SkillClassifier();
            LearnerStateAttributeManager skillMgr = new LearnerStateAttributeManager(LearnerStateAttributeNameEnum.SKILL, classifier, new SkillPredictor(classifier));
            skillMgrs.add(skillMgr);
            
            attributeToManagers.put(LearnerStateAttributeNameEnum.SKILL, skillMgrs);
        }
        
        return skillMgrs;
    }
    
    /**
     * Return the LMS course records for this learner
     * 
     * @return LMSCourseRecords
     */
    public LMSCourseRecords getLMSCourseRecords(){
    	return lmsRecords;
    }
    
    /**
     * Send a Learner State message to the pedagogical module
     */
    private synchronized void sendLearnerState(){

        try{
            //send learner state update
            LearnerState state = new LearnerState(currentPerformance, currentCognitive, currentAffective);
            LearnerModule.getInstance().sendLearnerState(getUserSessionInfo(), currentDomainSession, state);
        }catch(Throwable t){
            logger.error("Caught exception while trying to send a new learner state for "+getUserSessionInfo()+".  This is a critical error as it prevents the "+
                    "Ped from knowing the current learner state which will influence the instructional strategy decisions made, therefore requesting that the domain session end.", t);
            LearnerModule.getInstance().sendCloseDomainSessionRequest(getUserSessionInfo(), currentDomainSession,
                    "There was a critical error sending a learner state update to the Pedagogical module, therefore the Learner module is requesting the domain session be ended.");
        }
    }
    
    /**
     * Notification that a lesson (i.e. dkf assessment) is being started.  For the learner model
     * the previous performance assessment needs to be cleared so the new DKF hierarchy doesn't collide
     * with the previous DKF hierarchy (i.e. node id collisions)
     */
    public void lessonStarted(){
        
        currentPerformance.getTasks().clear();
        
        for(List<LearnerStateAttributeManager> mgrs : attributeToManagers.values()){
            
            for(LearnerStateAttributeManager mgr : mgrs){
                mgr.knowledgeSessionStarted();
            }
        }

        if(logger.isInfoEnabled()) {
            logger.info("Cleared learner state performance state data for start of lesson for "+userSession+".");
        }
    }
    
    /**
     * Notification that a lesson (i.e. dkf assessment) has completed.  For the learner model
     * the previous performance assessment needs to be cleared so any future DKF hierarchy doesn't collide
     * with the previous DKF hierarchy (i.e. node id collisions)
     */
    public void lessonCompleted(){
        
        currentPerformance.getTasks().clear();
        
        for(List<LearnerStateAttributeManager> mgrs : attributeToManagers.values()){
            
            for(LearnerStateAttributeManager mgr : mgrs){
                mgr.knowledgeSessionCompleted();
            }
        }

        if(logger.isInfoEnabled()) {
            logger.info("Cleared learner state performance state data for completion of lesson for "+userSession+".");
        }
    }
    
    /**
     * Update a performance attribute's (i.e. task, concept) state.
     * 
     * @param tStateManager - the performance attribute's state manager
     * @param assessment - the current assessment for that task/concept
     * @return boolean - whether the state changed for this performance attribute
     */
    private boolean updatePerformanceCategory(LearnerStateAttributeManager tStateManager, TaskAssessment assessment){
        
        if(tStateManager == null){
            //create a new state manager for this category
            
//            AbstractClassifier classifier = new UnderstandingClassifier();
//            tStateManager = new LearnerStateAttributeManager(classifier, new UnderstandingPredictor(classifier));
            AbstractClassifier classifier = new TaskPerformanceStateClassifier();
            tStateManager = new LearnerStateAttributeManager(null, classifier, new TaskPerformanceStatePredictor(classifier));
            perfCategoryNameToStateManager.put(assessment.getName().toLowerCase(), tStateManager);
            
            if(logger.isInfoEnabled()) {
                logger.info("Created learner state manager for task name = "+assessment.getName());                
            }
        }
        
        boolean stateChanged = tStateManager.updateState(assessment);
        
        /////////////////////////////////////
        //
        // important - update the knowledge (cognitive) state based on the performance assessment as long as the task assessment is for course concepts (a key task name)
        //             The performance assessments for course concepts are created by Knowledge Assessment Question bank rules authored in a course object.
        //             This will not update knowledge state for concepts as part of a real-time assessment (dkf) task/concept hierarchy performance assessment.
        //
        List<LearnerStateAttributeManager> knowledgeMgrs = getKnowledgeManagers();
        
        if(assessment.getName().equals(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME)){
            for(LearnerStateAttributeManager mgr : knowledgeMgrs){
                stateChanged |= mgr.updateState(assessment);
            }
        }
        //////////////////////////////////////////////
        
        /////////////////////////////////////
        //
        // important - update the skill (cognitive) state based on the performance assessment as long as the task assessment descendant concepts are course concepts
        //             This will update skill state for concepts as part of a real-time assessment (dkf) task/concept hierarchy performance assessment.
        //
        List<LearnerStateAttributeManager> skillMgrs = getSkillManagers();
        
        for(LearnerStateAttributeManager mgr : skillMgrs){
            stateChanged |= mgr.updateState(assessment);
        }
        //////////////////////////////////////////////
        
        
        if(stateChanged){
            //the performance assessment change, therefore create learner state update            
            updateLearnerState(tStateManager);
        }
        
        return stateChanged;
    }
    
    
    /**
     * Performance Assessment data has been received for this learner instance, 
     * provide the data to the learner state manager(s).
     * 
     * @param perfAssessment - incoming performance assessment data
     */
    public synchronized void addPerformanceAssessment(PerformanceAssessment perfAssessment){
    	
        boolean stateChanged = false;
        
        //update the evaluator
        if (!Objects.equals(this.currentPerformance.getEvaluator(), perfAssessment.getEvaluator())) {
            stateChanged = true;
            this.currentPerformance.setEvaluator(perfAssessment.getEvaluator());
        }
        
        //update the observer comment
        //NOTE: As a special case, empty observer comments should be treated as new bookmarks even if the last bookmark had an empty observer comment.
        if (!Objects.equals(this.currentPerformance.getObserverComment(), perfAssessment.getObserverComment()) ||
                StringUtils.isBlank(this.currentPerformance.getObserverComment())) {
            stateChanged = true;
            this.currentPerformance.setObserverComment(perfAssessment.getObserverComment());
        }
        
        //update the observer media
        if (!Objects.equals(this.currentPerformance.getObserverMedia(), perfAssessment.getObserverMedia())) {
            stateChanged = true;
            this.currentPerformance.setObserverMedia(perfAssessment.getObserverMedia());
        }
        
        //update each task's state manager
        for(TaskAssessment tAss : perfAssessment.getTasks()){
            
        	LearnerStateAttributeManager tStateManager = perfCategoryNameToStateManager.get(tAss.getName().toLowerCase());
            
        	stateChanged |= updatePerformanceCategory(tStateManager, tAss);
        }
        
        if(stateChanged){
            
            if(logger.isInfoEnabled()){
                logger.info("Learner State Manager has determined the learner state has changed because of performance assessment, creating learner state message");
            }
            
            sendLearnerState();
        }
    }
    
    /**
     * GradedScoreNode data has been received for this learner instance, provide the data to
     * the learner state manager(s). Will always update the learner state.
     * 
     * @param gradedScoreNode incoming graded score node data
     * @param courseConcepts the course concepts
     */
    public synchronized void addSkillScore(GradedScoreNode gradedScoreNode, List<String> courseConcepts) {

        boolean stateChanged = false;

        //
        // important - update the skill (cognitive) state based on the course record
        //
        List<LearnerStateAttributeManager> skillMgrs = attributeToManagers.get(LearnerStateAttributeNameEnum.SKILL);
        if (skillMgrs == null) {

            skillMgrs = new ArrayList<>();
            SkillClassifier classifier = new SkillClassifier();
            classifier.initCourseConcepts(courseConcepts, gradedScoreNode);
            
            LearnerStateAttributeManager skillMgr = new LearnerStateAttributeManager(LearnerStateAttributeNameEnum.SKILL, classifier, new SkillPredictor(classifier));
            skillMgrs.add(skillMgr);

            attributeToManagers.put(LearnerStateAttributeNameEnum.SKILL, skillMgrs);
        }
        
        // remove nodes that have nothing to do with this learner (e.g. the learner may have been in a team
        // session that was scored and other learners could have scoring info in this object)
        // Note: make a copy of the grade structure in case it is delivered to other Learner instances as part of a team session
        GradedScoreNode learnerScores = GradedScoreNode.deepCopy(gradedScoreNode);
        learnerScores.removeUnrelatedScores(userSession.getUsername());
        
        for (LearnerStateAttributeManager mgr : skillMgrs) {

            try {
                stateChanged |= mgr.updateState(learnerScores);
            } catch (Throwable t) {
                logger.error("Caught exception from mis-behaving learner state attribute manager of " + mgr + " while processing GradedScoreNode for "
                        + userSession, t);
            }
        }
        ///////////////

        if (stateChanged) {
            // the state changed, therefore create learner state update
            for (LearnerStateAttributeManager mgr : skillMgrs) {
                updateLearnerState((LearnerStateAttribute) mgr.getState());
            }
            sendLearnerState();
        }
    }
    
    /**
     * Processed sensor data has been received for this learner instance,
     * provide the data to the learner state manager.
     * 
     * @param domainSessionId - unique domain session id associated with the learner model
     * @param sensorData - filtered sensor data to use to determine the latest learner state
     */
    public void addFilteredSensorData(int domainSessionId, FilteredSensorData sensorData){
    	
        // Iterate through the inputs.
        // Does the input listen for this type of message?
        // Is the statemanager for this input instantiated?
        //   If not - instantiate the state manager & add it to the list.
        // Update the statemanager with the message.
        
        for (int x=0; x < fileHandler.getInputs().size(); x++) {
            
            
            Input input = fileHandler.getInputs().get(x);
            
            // If this is a misconfigured input, then ignore it and move to the next input.
            if (misconfiguredInputs.contains(input.getName())) {
                continue;
            }
            
            // If this input listens for this message, then try to handle the message.
            if (inputListensforFilteredSensorData(input, sensorData)) {
                
                // If the state manager hasn't been instantiated yet for this input, create it now.
                if (!inputNameToStateManager.containsKey(input.getName())) {
                    
                    // Instantiate the statemanager             
                    LearnerStateAttributeManager stateManager = createStateManager(input);
                    
                    if (stateManager != null) {
                        inputNameToStateManager.put(input.getName(), stateManager);
                    }
                }
             
                // Update the statemanager with the message.
                updateStateManager(input.getName(), sensorData);
            }
        }
    	
    }
    
    /**
     * Helper function to update the statemanager (based on InputName) with the stateData object.
     * The stateData is expected to be a FilteredSensorData or TrainingAppState object type, otherwise an error is logged.
     * If the object is valid, then the statemanager updateState method is called with the latest state.  If the state is
     * actually updated, then a learner state message is sent to broadcast the new state.
     * 
     * 
     * @param inputName - The name of the input (from the learnerconfiguration.xml file).  Cannot be null.
     * @param stateData - A FilteredSensorData or TrainingAppState object.  Cannot be null.
     */
    private void updateStateManager(String inputName, Object stateData) {
        
        if (stateData instanceof FilteredSensorData ||
            stateData instanceof TrainingAppState) {
            LearnerStateAttributeManager stateManager = inputNameToStateManager.get(inputName);
            
            if (stateManager != null) {
                
                try{
                    if(stateManager.updateState(stateData)){
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Learner State Manager has determined the learner state has changed. Creating learner state message.");
                        }
                        
                        //create learner state update
                        updateLearnerState(stateManager);
                        sendLearnerState();
                    }
                }catch(Throwable t){
                    logger.error("Caught exception from mis-behaving learner state attribute manager of "+stateManager+" while processing data "+stateData+" for "+userSession, t);
                }
            } 
            
          
        } else {
            logger.error("Attempting to update state manager for an unsupported state type. Received stateData of class: " 
                         + stateData.getClass().getName() + ", but only FilteredSensorData and TrainingAppState is expected.");
            
        }
        
    }
    
    
    /**
     * Determines if an input is configured to listen for specified filteredsensor data.
     * 
     * @param input - The input (as defined in the leanerconfiguration.xml file).  Cannot be null.
     * @param data - filtered sensor data to check the input against.  Cannot be null.
     * @return boolean - True if the input is configured to listen for the sensor messages.  False otherwise.
     */
    private boolean inputListensforFilteredSensorData(Input input, FilteredSensorData data) {
        boolean success = false;
        
        // Iterate over each producer for the input.
        for (Serializable producer : input.getProducers().getProducerType()) {
            
            // Only check against sensor types.
            if(producer instanceof generated.learner.Sensor){
                generated.learner.Sensor sensorProducer = (generated.learner.Sensor)producer;
                SensorTypeEnum sensorType = SensorTypeEnum.valueOf(sensorProducer.getType());
                
                // If sensor type configured in the leaner file matches the filteredsensordata message, then return true.
                if (sensorType == data.getSensorType()) {
                    success = true;
                    break;
                }
            }
                
        }
        
        return success;
    }
    
    /**
     * Determines if an input is configured to listen for a specified trainingappstate message.
     * 
     * @param input - The input (as defined in the leanerconfiguration.xml file).  Cannot be null.
     * @param msgType - The trainingAppState message to check the input against.  Cannot be null.
     * @return boolean - True if the input is configured to listen for message type.  False otherwise.
     */
    private boolean inputListensForTrainingAppState(Input input, MessageTypeEnum msgType) {
        boolean success = false;
        
        // Iterate over each producer for the input.
        for (Serializable producer : input.getProducers().getProducerType()) {

            // Only check for trainingappstate producers.
            if(producer instanceof generated.learner.TrainingAppState){
                generated.learner.TrainingAppState taStateProducer = (generated.learner.TrainingAppState)producer;
                
                // If the messageType configured in the learnerconfiguration.xml file matches the msgType, then return true.
                MessageTypeEnum messageType = MessageTypeEnum.valueOf(taStateProducer.getType());
                if (msgType == messageType) {
                    success = true;
                    break;
                }
            }
        }
        return success;
    }
    
    /**
     * Updates the attributeToManagers map which tracks which managers are handling each attribute.
     * 
     * @param stateManager - The statemanager to update.  Cannot be null.
     */
    private void updateAttributeManagerList(LearnerStateAttributeManager stateManager) {
        
        if (stateManager != null) {
            //map state manager to attribute it managers for quick lookup later
            LearnerStateAttributeNameEnum attribute = stateManager.getAttribute();
            if(attribute != null){
                List<LearnerStateAttributeManager> mgrs = attributeToManagers.get(attribute);
                
                if(mgrs == null){
                    mgrs = new ArrayList<>();
                    attributeToManagers.put(attribute, mgrs);
                }
                
                // Add this stateManager to our list of managers for this attribute.
                mgrs.add(stateManager);                 
            }
        }
    }
    
    /**
     * Creates a LearnerStateAttributeManager based on an input (as defined in the learnerconfiguration.xml file).
     * If it cannot be created, then null can be returned.  An exception will be thrown as well if there is a configuration issue.
     * 
     * @param input - The input (as defined in the leanerconfiguration.xml file).  Cannot be null.
     * @return LearnerStateAttributeManager - True if the input is configured to listen for message type.  False otherwise.
     */
    private LearnerStateAttributeManager createStateManager(Input input) {
        
        LearnerStateAttributeManager stateManager = null;                
        
        try {
            
            // 
            // Translator
            // 
            Class<?> translatorClass = fileHandler.getTranslator(input.getName());
            
            if (translatorClass != null) {
                AbstractSensorTranslator translator = (AbstractSensorTranslator)(translatorClass.getDeclaredConstructor().newInstance());
                ClassifierConfiguration classifierConfig = fileHandler.getClassifier(input.getName());
                
                if(classifierConfig == null){
                    throw new RuntimeException("There is no classifier configured for input named "+input.getName()+".  Make sure the LearnerConfiguration.xml file is setup correctly.");
                }
                
                //
                // Classifier
                //
                
                AbstractClassifier classifier = (AbstractClassifier)classifierConfig.getImplementationClass().getDeclaredConstructor().newInstance();
                classifier.configureByProperties(classifierConfig.getProperties());
                classifier.setTranslator(translator);               
                
                //
                // Predictor
                //
                Class<?> predictorClass = fileHandler.getPredictor(input.getName());
                if(predictorClass == null){
                    throw new RuntimeException("There is no predictor configured for input type of "+input.getName()+".  Make sure the LearnerConfiguration.xml file is setup correctly.");
                }                
                
                Constructor<?> constructor = predictorClass.getConstructor(AbstractClassifier.class);
                AbstractBasePredictor predictor = (AbstractBasePredictor)constructor.newInstance(classifier);
                
                // Create the new learner state attribute manager class.
                stateManager = new LearnerStateAttributeManager(classifier.getAttribute(), classifier, predictor);
                
                
                // Update the attribute manager list with the new statemanager.
                updateAttributeManagerList(stateManager);
                                
                if(logger.isInfoEnabled()) {
                    logger.info("Successfully created statemanager for input named " + input.getName());
                }
                
            } else {
                
                throw new RuntimeException("There is no translator configured for input type of "+input.getName()+".  Make sure the LearnerConfiguration.xml file is setup correctly.");
                
            }
        } catch (Throwable e) {
            
            logger.error("Error trying to create the learnerstateattributemanager for input of " + input.getName(), e);
            
            // Add this to the list of misconfigured inputs.
            misconfiguredInputs.add(input.getName());
        }
            
        return stateManager;
    }
    
    /**
     * Process the Training Application state and update the learner state accordingly
     * 
     * @param type the type of training app state message to process
     * @param state - the training application state to process
     */
    public void addTrainingAppState(MessageTypeEnum type, TrainingAppState state){
        
        
        // Iterate through the inputs.
        // Does the input listen for this type of message?
        // Is the statemanager for this input instantiated?
        //   If not - instantiate the state manager & add it to the list.
        // Update the statemanager with the message.

        for (int x=0; x < fileHandler.getInputs().size(); x++) {
            
            
            Input input = fileHandler.getInputs().get(x);
            
            // If this is a misconfigured input, then ignore it and move to the next input.
            if (misconfiguredInputs.contains(input.getName())) {
                continue;
            }
            
            // If this input listens for this message, then try to handle the message.
            if (inputListensForTrainingAppState(input, type)) {
                
                // If the state manager hasn't been instantiated yet for this input, create it now.
                if (!inputNameToStateManager.containsKey(input.getName())) {
                    
                    // Instantiate the statemanager             
                    LearnerStateAttributeManager stateManager = createStateManager(input);
                    
                    if (stateManager != null) {
                        inputNameToStateManager.put(input.getName(), stateManager);
                    }
                }
             
                // Update the statemanager with the message.
                updateStateManager(input.getName(), state);
                
            }
        }
    }
    
    /**
     * Process the survey response and update the learner state accordingly to any scoring information
     * available for the survey and it's questions.
     * 
     * @param surveyResults - response(s) to a survey
     */
    public void addSurveyResponse(SubmitSurveyResults surveyResults){
     
        //
        // Retrieve the survey score scales and apply to the learner state
        //
        List<ScoreInterface> scores = SurveyScorerManager.getScores(surveyResults.getSurveyResponse());
        boolean stateUpdated = false;
        for(ScoreInterface score : scores){
            
            if(score instanceof SurveyScaleScore){
                
                SurveyScaleScore scaleScore = (SurveyScaleScore)score;
                
                for(AbstractScale aScale : scaleScore.getScales()){
                    
                    LearnerStateAttributeNameEnum attribute = aScale.getAttribute();
                    AbstractEnum value = aScale.getValue();
                    
                    if(value != null){
                        //get manager(s) for the attribute
                        
                        List<LearnerStateAttributeManager> mgrs = attributeToManagers.get(attribute);
                            
                        if(mgrs == null){
                            /* a manager doesn't exist for this attribute,
                             * therefore create one. If the type is knowledge,
                             * use it's specific classifier; otherwise use a
                             * generic classifier. */

                            if (attribute == LearnerStateAttributeNameEnum.KNOWLEDGE) {
                                mgrs = getKnowledgeManagers();
                            } else {
                                AbstractClassifier classifier = new GenericClassifier(attribute);
                                AbstractBasePredictor predictor = new GenericPredictor(classifier);
                                LearnerStateAttributeManager stateManager = new LearnerStateAttributeManager(attribute, classifier, predictor);

                                mgrs = new ArrayList<>();                            
                                mgrs.add(stateManager); 
                            }
                                                         
                            attributeToManagers.put(attribute, mgrs);
                        }               

                    
                        for(LearnerStateAttributeManager mgr : mgrs){
                        
                            try{
                                if(mgr.updateState(aScale)){
                                    stateUpdated = true;
                                    updateLearnerState(mgr);
                                }
                            }catch(Throwable t){
                                logger.error("Caught exception from mis-behaving learner state attribute manager of "+mgr+" while processing survey response score "+aScale+" for "+userSession, t);
                            }
                        }
                    }
                }
                
            }
        }
        
        if(stateUpdated){
            
            if(logger.isInfoEnabled()) {
                logger.info("Learner State Manager has determined the learner state has changed because of survey result data, creating learner state message");
            }
            
            //create learner state update
            sendLearnerState();
        }
    }
    
    /**
     * Return the time at which the last LMS records where received by this Learner instance
     * @return can be null if {@link #updateLearnerStatesFromLMS(List)} was never called.
     */
    public Date getLastLMSQuery(){
        return lastLMSQuery;
    }
    
    /**
     * Set the course concepts used in the current course.
     * @param courseConcepts can be null and can contain no course concepts.
     */
    public void setCourseConcepts(generated.course.Concepts courseConcepts) {
        
        if(courseConcepts == null) {
            return;
        }
        
        List<LearnerStateAttributeManager> mgrs = getKnowledgeManagers();
        for(LearnerStateAttributeManager mgr : mgrs) {
            try {
                mgr.setCourseConcepts(courseConcepts);
            }catch(Exception e) {
                throw new RuntimeException("An error happened while trying to set the course concepts on the knowledge manager of "+mgr, e);
            }
        }
    }
    
    /**
     * Process the learner state list and update the learner state accordingly
     * 
     * @param learnerStateAttributes the states for each learner state attribute.  If null or empty this method
     * doesn't update learner state.
     */
    public void updateLearnerStatesFromLMS(List<AbstractScale> learnerStateAttributes){
        
        lastLMSQuery = new Date();

        if (learnerStateAttributes != null && !learnerStateAttributes.isEmpty()) {
            for (AbstractScale learnerScale : learnerStateAttributes) {

                LearnerStateAttributeNameEnum attribute = learnerScale.getAttribute();
                AbstractEnum value = learnerScale.getValue();

                if (value != null) {
                    // get manager(s) for the attribute
                    List<LearnerStateAttributeManager> mgrs = attributeToManagers.get(attribute);

                    if (mgrs == null) {
                        
                        if(attribute.equals(LearnerStateAttributeNameEnum.KNOWLEDGE)){
                            mgrs = getKnowledgeManagers();
                        }else if(attribute.equals(LearnerStateAttributeNameEnum.SKILL)){
                            mgrs = getSkillManagers();
                        }else{
                            // a manager doesn't exist for this attribute, therefore create a generic one
                            AbstractClassifier classifier = new GenericClassifier(attribute);
                            GenericPredictor predictor = new GenericPredictor(classifier);
    
                            LearnerStateAttributeManager stateManager = new LearnerStateAttributeManager(attribute, classifier, predictor);
                            mgrs = new ArrayList<>();
                            mgrs.add(stateManager);
                        }
                        
                        attributeToManagers.put(attribute, mgrs);
                    }

                    for (LearnerStateAttributeManager mgr : mgrs) {

                        try {
                            if (mgr.updateState(learnerScale)) {
                                updateLearnerState(mgr);
                            }
                        } catch (Throwable t) {
                            logger.error("Caught exception from mis-behaving learner state attribute manager of " + mgr
                                    + " while processing LMS learner state " + learnerScale + " for " + userSession, t);
                        }
                    }
                }
            }

        }
    }
    
    /**
     * Update the Learner State Manager's learner state attribute information in the map
     * 
     * @param stateManager - a learner state manager instance to update
     */
    private void updateLearnerState(LearnerStateAttributeManager stateManager){
    
        Object state = stateManager.getState();
    	
    	if(state == null){
    		logger.error("The learner state attribute for "+stateManager+" is null, therefore unable to process any state attribute changes");
    		
    	}else{    	    
            //handle the various types of learner state states (e.g. Task Performance state, Affective state)
    	    
    	    //TODO: applying task performance to knowledge makes since for a performance assessment message as a result
    	    //      of survey response but not for real time assessing during a training application
            if(state instanceof TaskPerformanceState){
                TaskPerformanceState tState = (TaskPerformanceState)state;
                
                synchronized(currentPerformance){
                    currentPerformance.getTasks().put(tState.getState().getNodeId(), tState);
                }
                
                //update Knowledge (cognitive) state
                List<LearnerStateAttributeManager> knowledgeMgrs = attributeToManagers.get(LearnerStateAttributeNameEnum.KNOWLEDGE);
                for(LearnerStateAttributeManager mgr : knowledgeMgrs){
                    updateLearnerState((LearnerStateAttribute)mgr.getState());
                }
                
            }else{
                LearnerStateAttribute stateAttr = (LearnerStateAttribute)state;
                updateLearnerState(stateAttr);
            }   		
    		
    	}    	

    }
    
    /**
     * Update the learner state affective and cognitive attributes based on the state attribute provided.
     * 
     * @param stateAttr contains the lastest attribute information to place in a learner state object.  If null nothing is updated.
     */
    private void updateLearnerState(LearnerStateAttribute stateAttr){
        
        if(stateAttr == null){
            return;
        }
        
        LearnerStateAttributeNameEnum attrName = stateAttr.getName();
        if(attrName.getLearnerStateCategory().equals(LEARNER_STATE_CATEGORY.AFFECTIVE)){
            //affective states
            currentAffective.getAttributes().put(attrName, stateAttr);
        }else if(attrName.getLearnerStateCategory().equals(LEARNER_STATE_CATEGORY.COGNITIVE)){
            //cognitive states
            updateCurrentCognitiveAttributes(stateAttr);
        }
    }

    /**
     * Updates the current cognitive attribute for the specific state attribute.
     * If none was previously existing, add it to the attribute map. If the
     * existing entry is a {@link LearnerStateAttributeCollection collection},
     * only a {@link LearnerStateAttributeCollection collection} can replace it.
     * If the existing entry is a {@link LearnerStateAttribute single attribute}
     * then any non-null value can replace it.
     * 
     * @param stateAttrToUpdate the state attribute to update the current
     *        cognitive attribute map.
     */
    private void updateCurrentCognitiveAttributes(LearnerStateAttribute stateAttrToUpdate) {
        /* Do not update if null */
        if (stateAttrToUpdate == null) {
            return;
        }

        LearnerStateAttributeNameEnum attrToUpdateName = stateAttrToUpdate.getName();

        Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> currentAttributeMap = currentCognitive
                .getAttributes();
        LearnerStateAttribute currentAttribute = currentAttributeMap.get(attrToUpdateName);
        /* Check if the current attribute is a collection type */
        if (currentAttribute instanceof LearnerStateAttributeCollection) {
            /* Only a collection can replace a collection, otherwise drop the
             * provided state attribute on the floor */
            if (stateAttrToUpdate instanceof LearnerStateAttributeCollection) {
                currentAttributeMap.put(attrToUpdateName, stateAttrToUpdate);
            }
        } else {
            /* If the current attribute is not a collection, anything can
             * replace it */
            currentAttributeMap.put(attrToUpdateName, stateAttrToUpdate);
        }
    }

    /**
     * Cleanup this learner model because it is no longer needed.
     */
    public void cleanup(){
        
    }

}
