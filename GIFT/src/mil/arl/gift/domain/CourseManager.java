/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.List.Concept;
import generated.course.LtiProvider;
import generated.course.LtiProviders;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore.ConceptOverallDetails;
import mil.arl.gift.common.survey.score.SurveyScorerManager;
import mil.arl.gift.domain.course.CourseObjectWrapper;
import mil.arl.gift.domain.course.DynamicContentHandler;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler;
import mil.arl.gift.domain.course.MerrillsBranchPointManager;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.ProxyIntermediateConceptAssessment;
import mil.arl.gift.domain.knowledge.common.ProxyPerformanceAssessment;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerRequestInterface;

/**
 * The course manager is responsible for keeping track of where the learner is,
 * at a high level, in a course.
 *
 * @author mhoffman
 */
public class CourseManager {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CourseManager.class);
    
    /** this is a fixed message that will be shown in place of an AAR course element when that AAR has no events to show/review */
    public static final String NO_AAR_MESSAGE = "The course author wanted to present a review of your performance at this time,<br>" +
            "however there is nothing to review.<br><br>If possible, after the course has completed, please make your instructor or GIFT administrator aware that this message appeared.<br><br>" +
            "Press continue to move forward in the course.";

    /** a guidance to be displayed while course content is loading and there is no other guidance or loading message to show. */
    public static final String DEFAULT_LOADING_MESSAGE_FILE =  "resources" + File.separator + "contentLoadingPage" + File.separator + "loadingGuidance.html";
    public static final String DEFAULT_LOADING_MESSAGE_FILE_URL = UriUtil.makeURICompliant(DomainModuleProperties.getInstance().getDomainContentServerAddress()  + Constants.FORWARD_SLASH + DEFAULT_LOADING_MESSAGE_FILE);
    
    /** a message to be displayed while a concept survey is created */
    public static final String DEFAULT_CONCEPT_SURVEY_LOADING_MESSAGE = "<html>Please wait while GIFT retrieves the course content.<br/><br/><b>Note:</b> The time it takes to start will depend on the size of the course content being presented and your network connection.  Sometimes this can take a minute or so.</html>";
    
    /** The default name to give the root concept in the hierarchy is no root concept is present */
    private static final String DEFAULT_ROOT_CONCEPT = "all concepts";
    
    /**
     * using 0 so that no progress will report as 0% which can happen if the course is ended before the first course object (e.g. on the course init page)
     */
    private static final Integer INIT_PROGRESS = 0;
    
    /** the course object populated with contents of input course xml file */
    private generated.course.Course course;

    /** list of course objects for this course */
    private List<CourseObjectWrapper> courseObjects;
    
    /**
     * Contains the concepts covered in this course
     * Note: can be null in cases where the author didn't define the concepts for this course.  This
     * would mean there are no branch point course elements either.
     * 
     * Types: generated.course.Concepts.List, generated.course.Concepts.Hierarchy
     */
    private Serializable courseConcepts;
    
    /** The hierarchy of concepts covered by the course */
    private Concepts.Hierarchy concepts;

    /** the current transitions index in the collection */
    private int transitionIndex = -1;
    
    /** 
     * The current progress.  This is a measure of the current transition based on the initial set of transitions for the course (excluding any 
     * dynamically inserted transitions that may come from adaptive courseflow).
     */
    private int currentProgress = INIT_PROGRESS;
    
    /** 
     * The maximum progress that can be made in the course.  This is a measure of the maximum transitions based on the initial set of
     * transitions in the course.
     * Note: using max value so that its obvious if the value doesn't match the number of course objects because
     * the % progress values will be really small (i.e. probably zero).
     */
    private int maxProgress = Integer.MAX_VALUE;
   
    /** (Optional) The progress reporter that is used to report progress to other parts of GIFT.  This is optional and can be empty. */
    private List<AbstractProgressReporter> progressReporters = new ArrayList<>();
    
    /** Stack used to keep track of any dynamically inserted course objects.  This is used to ignore incrementing progress if there are any values
     * in this stack.
     */
    private Stack<CourseObjectWrapper> insertedTransitions = new Stack<>();
    
    /** handler for ped request */
//    private CoursePedagogicalRequestHandler pedRequestHandler;
    
    /** 
     * An artifical Task Performance Assessment to contain course concept assessments
     * It will be used (and re-used) when assessing against course concepts
     * defined by the author.  This doesn't contain DKF task/concepts during a training app course
     * object in the course.
     */
    private ProxyTaskAssessment courseConceptsProxyTaskAssessment;
    
    /**
     * An artifical Task performance assessment to contain conversation related concept assessments.
     * It will be used (and re-used) for conversations that don't have a DKF involved.
     * For example AutoTutor course object can populate this with Expectation, Hint and Prompt concept assessments
     * during an AutoTutor conversation and for each learner response.
     */
    private ProxyTaskAssessment conversationConceptsProxyTaskAssessment;
    
    /**
     * contains IDs of all the artifical tasks performance assessments created to manage
     * different assessments that span across course objects (e.g. authored defined course concepts) or
     * are not managed by a DKF (e.g. AutoTutor conversation course object)
     */
    private ProxyPerformanceAssessment proxyPerformanceAssessment;
    
    /**
     * maintains the performance assessment values across a course and is used here to
     * update assessments calculated from survey responses.
     */
    private AssessmentProxy assessmentProxy;
    
    /**
     * mapping of course concept name (lower case) to the unique runtime id for a performance assessment node/object
     * that will keep track of the course concept performance assessment over the course execution
     */
    private Map<String, UUID> courseConceptToNodeId = new HashMap<>();    
    
    /** 
     * for managing the course transition created during the execution of a 
     * merrill's branch point course transition 
     */
    private MerrillsBranchPointManager merrillsBranchPointManager;
    
    /**
     * for handling training application course objects that have optional
     * remediation enabled.  Will be null if not in a training application course object
     * that leverages remediation or in a series of course objects resulting from
     * remediation.
     */
    private DynamicContentHandler trainingAppRemediationHandler;

    /** where the runtime version of the course resides, it is the course directory the 
     * course XML file is in and which contains all course related files */
    private DesktopFolderProxy courseRuntimeDirectory;
    
    /**
     * where the session writes its output too.  E.g. domain session log, video files, sensor files.
     * Won't be null and will exists.
     */
    private DesktopFolderProxy sessionOutputDirectory;
    
    /** where the authored course resides, useful for updating persistent files like paradata */
    private AbstractFolderProxy courseAuthoredDirectory;
    
    /** contains any custom module configuration file references.  Can be null since it is optional course.xml element */
    private generated.course.Course.Configurations customConfigurations = null;
    
    /** used to manage different types of conversations that can happen during a course */
    private ConversationManager conversationManager = new ConversationManager();
    
    /**
     * Class constructor - uses the provided course file handler instead of creating it's own instance.  This could
     * be useful in situations where the caller wants to control what logic is executed when parsing the course file (e.g. validation).
     *
     * @param runtimeCourseFile The course file that contains course execution information to manage
     * @param courseAuthoredDirectory where the authored course resides, useful for updating persistent files like paradata. Can't be null.
     * @param assessmentProxy maintains the performance assessment values across a course and is used here to
     * update assessments calculated from survey responses.
     * @param strategyHandlerRequestInterface - the handler for strategy requests
     * @param domainCourseFileHandler the handler to use that parsed the course file
     * @param runtimeParams (Optional - can be null).  Additional parameters that may be used to configure the domain session.
     * @param username the username of the user running the course.
     * @param sessionOutputFolder where the session is writing output too.  This is a folder in the output folder of GIFT.  E.g. domainSession697_uId1
     * @throws DetailedException if there was a problem retrieving the course folder or the course folder could not be found
     * @throws FileNotFoundException if there was a problem retrieving the runtime course folder
     * @throws IOException if there was a problem retrieving the course files
     */
    public CourseManager(FileProxy runtimeCourseFile, AbstractFolderProxy courseAuthoredDirectory, AssessmentProxy assessmentProxy, 
            StrategyHandlerRequestInterface strategyHandlerRequestInterface, DomainCourseFileHandler domainCourseFileHandler, 
            AbstractRuntimeParameters runtimeParams, String username, String sessionOutputFolder) throws FileNotFoundException, DetailedException, IOException {
        
        generated.course.Course course = domainCourseFileHandler.getCourse();
        customConfigurations = domainCourseFileHandler.getCustomConfigurations();
        
        if(assessmentProxy == null){
            throw new IllegalArgumentException("The assessment proxy can't be null.");
        }
        
        this.assessmentProxy = assessmentProxy;
        
        if(course == null){
            throw new IllegalArgumentException("The course can't be null.");
        }
        
        if (courseAuthoredDirectory == null) {
            throw new IllegalArgumentException("The parameter 'courseAuthoredDirectory' cannot be null.");
        }
        this.courseAuthoredDirectory = courseAuthoredDirectory;

        // the course manager will only be used in the GIFT runtime.  The GIFT runtime has the course folder
        // on local disk.  The course file proxy's id is the absolute path to the file.  Therefore to get the
        // course folder, just get the parent to the course file.
        this.courseRuntimeDirectory = new DesktopFolderProxy(new File(runtimeCourseFile.getFileId()).getParentFile());
        
        this.sessionOutputDirectory = new DesktopFolderProxy(new File(PackageUtil.getDomainSessions() + File.separator + sessionOutputFolder));

        // Create the progress reporter class that will be used by the course manager.
        // This can return null, and is optional.  If it is null, then no progress is reported.
        addProgressReporter(AbstractProgressReporter.createProgressReporterByRuntimeParams(runtimeParams));

        init(course);   

        //TODO: future work to allow course level action knowledge
//        CourseActionKnowledge actionKnowledge = new CourseActionKnowledge(dcfh.getCourse().getActions());
//        
//        pedRequestHandler = new CoursePedagogicalRequestHandler(actionKnowledge, strategyHandlerRequestInterface);
    }
    
    /**
     * Add a handler of course progress to this manager.  The handler will be called upon when 
     * a course progress is created.
     * 
     * @param progressReporter a handler of course progress information, e.g. sends a publish score message
     * to the LMS module.
     */
    public void addProgressReporter(AbstractProgressReporter progressReporter){
        
        if(progressReporter == null){
            return;
        }
        
        progressReporters.add(progressReporter);
    }

    
    /**
     * Returns the maximum progress that can be reached in the course.  This is based on the initial set of transitions
     * for the course.  It does not count/measure any dynamically inserted transitions that may get added 
     * after the course has started.
     * 
     * @return The maximum progress that can be reached in the course.
     */
    public int getMaxProgress() {
        return maxProgress;
    }
    
    /**
     * The current progress that the learner is in the course.  The is based on the initial set of transitions
     * for the course.  It does not count/measure any dynamically inserted transitions that may get added
     * after the course has started.
     * 
     * @return
     */
    public int getCurrentProgress() {
        return currentProgress;
    }
    
    /**
     * Initialize the course manager with a course
     *
     * @param course The course to be managed
     * @throws IOException - if there was a problem retrieving a file reference using the authored course folder
     */
    private void init(generated.course.Course course) throws IOException {

        this.course = course;

        int numEnabledTransitions = 0;
        List<Serializable> transitions = course.getTransitions().getTransitionType();
        courseObjects = new ArrayList<>();
        for (Serializable transition : transitions) {
            if (!DomainCourseFileHandler.isTransitionDisabled(transition)) {
                numEnabledTransitions++;
            }
            
            CourseObjectWrapper wrapper = CourseObjectWrapper.generateCourseObjectWrapper(transition, getCourseAuthoredDirectory());
            courseObjects.add(wrapper);
        }
        
        if (numEnabledTransitions == 0) {

            throw new IllegalArgumentException("There must be at least one enabled transition");
        }
        
        // Maximum progress is represented by the initial set of enabled transitions in the course (not added transitions by adaptive courseflow).
        maxProgress = numEnabledTransitions;
        currentProgress = INIT_PROGRESS;
        
        if(course.getConcepts() != null){
            
            courseConcepts = course.getConcepts().getListOrHierarchy();
            
            //construct a hierarchical representation of this course's concepts to share with other modules
            if(courseConcepts instanceof Concepts.Hierarchy){
                concepts = (Concepts.Hierarchy) courseConcepts;
                
            } else if(courseConcepts instanceof Concepts.List) {
                
                ConceptNode root = new ConceptNode();
                root.setName(DEFAULT_ROOT_CONCEPT);
                
                for(Concept concept : ((Concepts.List) courseConcepts).getConcept()){
                    ConceptNode child = new ConceptNode();
                    child.setName(concept.getName());
                    root.getConceptNode().add(child);
                }
                
                concepts = new Concepts.Hierarchy();
                concepts.setConceptNode(root);
            }
            
            buildCourseConceptAssessment();
        }else{

            //create placeholder proxy for the performance assessment being built to deal with
            //any performances assessments made outside of a lesson (dkf) 
            List<UUID> taskNodeCourseIds = new ArrayList<>(1);            
            proxyPerformanceAssessment = new ProxyPerformanceAssessment(taskNodeCourseIds);
        }
        
        merrillsBranchPointManager = new MerrillsBranchPointManager(courseRuntimeDirectory, courseAuthoredDirectory);

    }
    
    /**
     * Return the merrills branch point manager used for a course instance execution to
     * help manage all adaptive courseflow course objects in the course.  This will exist
     * even if the course has not adaptive courseflows.
     * 
     * @return the manager used to help adaptive courseflow logic
     */
    public MerrillsBranchPointManager getMerrillsBranchPointManager(){
        return merrillsBranchPointManager;
    }
    
    /**
     * Return the handler used for managing remediation logic after a practice application.
     * 
     * @return Will be null if not in a training application course object
     * that leverages remediation or in a series of course objects resulting from
     * remediation.
     */
    public DynamicContentHandler getTrainingAppRemediationHandler(){
        return trainingAppRemediationHandler;
    }
    
    /**
     * Set the handler used for managing remediation logic after a practice application.
     * 
     * @param trainingAppRemediationHandler the handler associated with a practice application
     * course object that has remediation enabled.  Use null when the training application 
     * that was leveraging remediation logic has completed and the next authored course object
     * will be presented next.
     */
    public void setTrainingAppRemediationHandler(DynamicContentHandler trainingAppRemediationHandler){
        this.trainingAppRemediationHandler = trainingAppRemediationHandler;
    }
    
    /**
     * Return the course folder that the course is executed from and contains all course relevant files
     * 
     * @return proxy to the runtime course folder
     */
    public DesktopFolderProxy getCourseRuntimeDirectory(){
        return courseRuntimeDirectory;
    }
    
    /**
     * Return where the authored course resides, useful for updating persistent files like paradata
     * @return proxy to the authored course folder. Won't be null.
     */
    public AbstractFolderProxy getCourseAuthoredDirectory(){
        return courseAuthoredDirectory;
    }
    
    /**
     * Return where where the session writes its output too.  E.g. domain session log, video files, sensor files.
     * 
     * @return Won't be null and will exists.
     */
    public DesktopFolderProxy getSessionOutputDirectory(){
        return sessionOutputDirectory;
    }
    
    /**
     * Return the conversation manager instance for this course manager.
     * 
     * @return the conversation manager for this domain session
     */
    public ConversationManager getConversationManager(){        
        return conversationManager;
    }
    
    /**
     * Construct a new performance assessment proxy that will contain the necessary course level
     * concepts.  This performance assessment proxy will be updated after each concept based survey
     * is assessed. 
     */
    private void buildCourseConceptAssessment(){
              
        int nextNodeId = 1;  //Id 1 will be for a root task performance assessment node,
                                 //the remaining Ids will be for the concept nodes
        
        //contains the task ids (will be only 1 as all course concepts are inherently 
        // placed under an auto generated task) for the performance assessment
        List<UUID> taskNodeCourseIds = new ArrayList<>(1);
        
        //contains the child concepts ids for the 1 and only task
        List<UUID> childConceptNodeCourseIds = new ArrayList<>();
        
        //a proxy for the performance assessment being built 
        //- this will be used to build each survey response performance assessment
        proxyPerformanceAssessment = new ProxyPerformanceAssessment(taskNodeCourseIds);   
        
        if(concepts != null) {
            
            courseConceptsProxyTaskAssessment =  
                    new ProxyTaskAssessment(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME, AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), childConceptNodeCourseIds, nextNodeId, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
            taskNodeCourseIds.add(courseConceptsProxyTaskAssessment.getCourseNodeId());
            assessmentProxy.fireAssessmentUpdate(courseConceptsProxyTaskAssessment);
            courseConceptToNodeId.put(courseConceptsProxyTaskAssessment.getName().toLowerCase(), courseConceptsProxyTaskAssessment.getCourseNodeId());
            
            //get the root node's (i.e. task) child concepts and update the roots list of child Ids 
            buildHierarchyCourseConceptAssessment(nextNodeId + 1, concepts.getConceptNode(), childConceptNodeCourseIds);
            
        } else{
             throw new ConfigurationException("Found unhandled course concept structure of "+courseConcepts+".",
                     "Unable to build the performance assessment structure for "+courseConcepts+".",
                     null);
    }
    
    }
    
    /**
     * This method can be used recursively to construct a performance assessment concept tree based on the specified
     * concept node structure.  It can create sub-concept [proxy] or (leaf) concept assessment objects for each child
     * node of the concept node structure.
     * 
     * @param nextNodeId the next node id to use for a new performance assessment node.  This id is unique to this performance assessment
     * node structure and not the entire course.
     * @param conceptNode the parent concept node to the structure being built in this method
     * @param childConceptNodeCourseIds the collection to add nodes being created as children to the parent concept node specified.
     * @return the next node id to use for the next node created
     */
    private int buildHierarchyCourseConceptAssessment(int nextNodeId, ConceptNode conceptNode, List<UUID> childConceptNodeCourseIds){
      
      if(conceptNode == null) {
          return nextNodeId;
      }
      
      if(conceptNode.getConceptNode().isEmpty()) {
          //found a leaf concept node 
          ConceptAssessment conceptAssessment = 
              new ConceptAssessment(conceptNode.getName(), AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), nextNodeId, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
          
          if(conceptNode.getAuthoritativeResource() != null) {
              conceptAssessment.setAuthoritativeResource(conceptNode.getAuthoritativeResource().getId());
          }
          
          assessmentProxy.fireAssessmentUpdate(conceptAssessment);
          
          childConceptNodeCourseIds.add(conceptAssessment.getCourseNodeId());
          courseConceptToNodeId.put(conceptAssessment.getName().toLowerCase(), conceptAssessment.getCourseNodeId());
      } else {
          //found a concept node that has at least 1 child, i.e. an intermediate or subconcept node
          List<UUID> grandChildConceptNodeCourseIds = new ArrayList<>();
          for(ConceptNode childNode : conceptNode.getConceptNode()){
              
              //build the children nodes first so they can be added to this node
              nextNodeId = buildHierarchyCourseConceptAssessment(nextNodeId, childNode, grandChildConceptNodeCourseIds);              
          }
          
          ProxyIntermediateConceptAssessment proxyInterConceptAssessment = 
                  new ProxyIntermediateConceptAssessment(conceptNode.getName(), AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), nextNodeId, grandChildConceptNodeCourseIds, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
          
          if(conceptNode.getAuthoritativeResource() != null) {
              proxyInterConceptAssessment.setAuthoritativeResource(conceptNode.getAuthoritativeResource().getId());
          }
          
          assessmentProxy.fireAssessmentUpdate(proxyInterConceptAssessment);  
          courseConceptToNodeId.put(proxyInterConceptAssessment.getName().toLowerCase(), proxyInterConceptAssessment.getCourseNodeId());             
          childConceptNodeCourseIds.add(proxyInterConceptAssessment.getCourseNodeId());
      }
      
      //used the current next node id in this method call, increment and return the next node id
      //to be used the next time a node is created
      return nextNodeId + 1;
  }
    
    /**
     * A pedagogical request was received and need to be handled by the domain knowledge
     * 
     * @param request - the request to handle. Can't be null.
     */
    public void handlePedagogicalRequest(PedagogicalRequest request){
        
        //TODO: allow dkf style actions/strategy definitions to be used at the course level
//        pedRequestHandler.handlePedagogicalRequest(request);

        merrillsBranchPointManager.handlePedagogicalRequest(request);
    }
    
    /**
     * Gets the hierarchy of concepts covered by the course
     * 
     * @return the concept hierarchy. Can be null if the course covers no concepts.
     */
    public Concepts.Hierarchy getConcepts(){
        return concepts;
    }
    
    /**
     * Return the concepts covered in this course
     * 
     * @return Serializable - generated.course.Concepts.List or generated.course.Concepts.Hierarchy.  Can be null
     * because this is an optional element in the course schema.
     */
    public List<String> getCourseConceptsFlatList() {
        return CourseConceptsUtil.getConceptNameList(course.getConcepts());
    }
    
    /**
     * Return the LTI providers covered in this course
     * 
     * @return the list of LTI providers covered in this course. Can be null because this is an
     *         optional element in the course schema.
     */
    public List<LtiProvider> getCourseLtiProviders() {
        if (course.getLtiProviders() == null) {
            course.setLtiProviders(new LtiProviders());
        }

        // ensure no duplicates by using a map with the keys as the LTI provider identifiers
        Map<String, LtiProvider> idToProviderMap = new HashMap<String, LtiProvider>();

        // from authored course
        for (LtiProvider provider : course.getLtiProviders().getLtiProvider()) {
            idToProviderMap.put(provider.getIdentifier(), provider);
        }

        // from property file
        HashMap<String, LtiProvider> propertyProviders = DomainModuleProperties.getInstance().getTrustedLtiProviders();
        if (propertyProviders != null) {
            // from authored course
            for (LtiProvider provider : propertyProviders.values()) {
                idToProviderMap.put(provider.getIdentifier(), provider);
            }
        }

        return new ArrayList<LtiProvider>(idToProviderMap.values());
    }
    
    /**
     * Return the actions portion of the course.
     * 
     * @return generated.course.Actions
     */
    public generated.course.Actions getActions(){
        //currently not supported
//        return course.getActions();
        return null;
    }

    /**
     * Return the survey context id for the course
     *
     * @return int The survey context id for the course
     */
    public int getSurveyContextId() {
        return course.getSurveyContext().intValue();
    }

    /**
     * Get the authored name of the course
     *
     * @return String The authored name of the course
     */
    public String getCourseName() {
        return course.getName();
    }
    
    /**
     * Return the custom pedagogical configuration file contents authored in this course as a string.
     * 
     * @return the contents of the pedagogical configuration file referenced by the configurations object. 
     * Will be null if either the configurations object is null or the pedagogical configuration parameter is null.
     * @throws Exception if there was any type of critical exception trying to retrieve the pedagogical configuration file.
     */
    public String getCustomPedConfiguration() throws Exception{
        return DomainCourseFileHandler.getCustomPedConfiguration(courseRuntimeDirectory, customConfigurations);    	
    }
    
    /**
     * Return the custom sensor configuration file contents authored in this course as a string.
     * 
     * @return the contents of the sensor configuration file referenced by the configurations object. 
     * Will be null if either the configurations object is null or the sensor configuration parameter is null.
     * @throws Exception if there was any type of critical exception trying to retrieve the sensor configuration file.
     */
    public String getCustomSensorConfiguration() throws Exception{
        return DomainCourseFileHandler.getCustomSensorConfiguration(courseRuntimeDirectory, customConfigurations);        
    }
    
    /**
     * Return the custom learner configuration file contents authored in this course as a string.
     * 
     * @return the contents of the learner configuration file referenced by the configurations object. 
     * Will be null if either the configurations object is null or the learner configuration parameter is null.
     * @throws Exception if there was any type of critical exception trying to retrieve the learner configuration file.
     */
    public String getCustomLearnerConfiguration() throws Exception{
        return DomainCourseFileHandler.getCustomLearnerConfiguration(courseRuntimeDirectory, customConfigurations);        
    }  
        
    /**
     * Get the next transition for the course. This will increment the current
     * transition. Note: null will be returned if there is no next transition.
     *
     * @return the next transition in the course
     */
    public CourseObjectWrapper getNextTransition() {

        CourseObjectWrapper transitionData = null;

        if (hasMoreTransitions()) {
            transitionIndex++;
            transitionData = courseObjects.get(transitionIndex);
            if(logger.isInfoEnabled()){
                logger.info("Current transition index is "+transitionIndex);
            }
            
            boolean disabled = DomainCourseFileHandler.isTransitionDisabled(transitionData.getCourseObject());
            updateProgress(!disabled);
        }        

        
        return transitionData;
    }
    
    
    /**
     * Updates the learner progress in the course.  Progress is based on the initial set of
     * transitions in the course.  Any transitions that are dynamically inserted into the course
     * once the course starts (such as for adaptive courseflow), these transitions are ignored and not 
     * included when measuring progress.  This means that once a user hits an adaptive courseflow transition,
     * the progress is not updated until the user has completed the adaptive courseflow transition completely.
     * 
     * @param incrementProgress true to increment the progress counter; false to skip incrementing.
     */
    private void updateProgress(boolean incrementProgress) {
        // If there are no inserted transitions, then simply increment the current progress.
        if (insertedTransitions.isEmpty()) {
            if (incrementProgress) {
                currentProgress++;
            }
        } else {
            // If there are inserted transitions, simply 'pop' them off the stack until the stack is empty
            // so that the 'progress' remains the same while there are inserted transitions that the learner
            // is progressing through.
            insertedTransitions.pop();
        }
    }
    
    /**
     * Insert the list of transitions after the current transition.
     * 
     * @param courseObjects - course objects to insert
     */
    public void insertTransitions(List<CourseObjectWrapper> courseObjects){
        
        if(logger.isInfoEnabled()){
            logger.info("Inserting transitions after index of "+transitionIndex);
        }
        this.courseObjects.addAll(transitionIndex+1, courseObjects);
        
        // This keeps track of the inserted transitions so that they can be measured for 'progress'.  This stack
        // is simply used to measure once the user has finished the dynamically added transitions.
        this.insertedTransitions.addAll(courseObjects);
    }
    
    /**
     * Insert a single course object after the current course object.
     * 
     * @param courseObject the course object to insert.  Can't be null.
     */
    public void insertTransition(CourseObjectWrapper courseObject){
        
        if(courseObjects == null){
            throw new IllegalArgumentException("The course object to insert is null.");
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Inserting course objects after index of "+transitionIndex);
        }
        this.courseObjects.add(transitionIndex+1, courseObject);
        
        // This keeps track of the inserted course objects so that they can be measured for 'progress'.  This stack
        // is simply used to measure once the user has finished the dynamically added course objects.
        this.insertedTransitions.add(courseObject);
    }

    /**
     * Return the current course object of the course.
     *
     * @return the current course object in the course
     */
    public CourseObjectWrapper getCurrentTransition() {
        return courseObjects.get(transitionIndex);
    }

    /**
     * Return whether or not there is another transition after the current
     * transition.
     *
     * @return boolean If there is another transition after the current
     * transition.
     */
    public boolean hasMoreTransitions() {
        return (transitionIndex + 1) < courseObjects.size();
    }
    
    /**
     * Sets the next transition index to the size of the transition collection.
     * This is useful for skipping all transitions and ending the course.
     */
    public void setNextTransitionToEnd(){
        transitionIndex = courseObjects.size();
        
        setProgressToEnd();
    }
    
    /**
     * Sets the progress values to the 'end' of the course.  
     */
    public void setProgressToEnd() {
        insertedTransitions.clear();
        currentProgress = maxProgress;
        
    }
    
    /**
     * Create a performance assessment from the conversation assessments provided which are based on rules authored 
     * for the conversation and the learner's input.  Assessments will only be applied if the confidence
     * level is high enough to warrant a change in reported performance for a concept.
     * 
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     * Can't be null or empty.
     * @return a new performance assessment based on the conversation assessments. Won't be null.
     */
    public PerformanceAssessment assessPerformanceFromConversation(List<ConversationAssessment> assessments){
        
        for(ConversationAssessment conversationAssessment : assessments){
            
            if(conversationAssessment.getConfidence() >= AbstractPerformanceAssessmentNode.MIN_AFFECT_CONFIDENCE){
                
                String conceptName = conversationAssessment.getConcept().toLowerCase();
                
                // get the course concept UUID (if it is a course concept)
                UUID conceptUUID = courseConceptToNodeId.get(conceptName);
                if(conceptUUID == null){
                    //not a course concept, create new concept assessment instance and add it to the conversation task assessment
                                        
                    Integer nextNodeId = assessmentProxy.getNextNodeId(proxyPerformanceAssessment);
                    
                    ConceptAssessment conceptAssessment = 
                            new ConceptAssessment(conceptName, conversationAssessment.getAssessmentLevel(), System.currentTimeMillis(), nextNodeId, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
                    conceptAssessment.updateConfidence(conversationAssessment.getConfidence());
                    assessmentProxy.fireAssessmentUpdate(conceptAssessment);
                    courseConceptToNodeId.put(conceptName, conceptAssessment.getCourseNodeId());                    
                    
                    if(conversationConceptsProxyTaskAssessment == null){  
                        //the artifical task assessment hasn't been created yet, create it so the conversations assessed concepts
                        //can reside as children and then be properly sent as a performance assessment message later.
                        
                        conversationConceptsProxyTaskAssessment =  
                                new ProxyTaskAssessment(ConversationManager.CONVERSATION_TASK_NAME, AssessmentLevelEnum.UNKNOWN, System.currentTimeMillis(), new ArrayList<>(0), nextNodeId++, DomainKnowledgeManager.getNewPerformanceAssessmentNodeUUID());
                        proxyPerformanceAssessment.getTasks().add(conversationConceptsProxyTaskAssessment.getCourseNodeId());
                        assessmentProxy.fireAssessmentUpdate(conversationConceptsProxyTaskAssessment); 
                    }
                    
                    conversationConceptsProxyTaskAssessment.getConcepts().add(conceptAssessment.getCourseNodeId());
                    
                }else{
                    //is a course concept, update existing concept's assessment
                    AbstractAssessment assessment = assessmentProxy.get(conceptUUID);
                    assessment.updateAssessment(conversationAssessment.getAssessmentLevel());
                    assessment.updatePriority(assessment.getPriority());
                    assessment.updateConfidence(conversationAssessment.getConfidence());
                    assessmentProxy.fireAssessmentUpdate(assessment);
                }
            }
        }
        
        //Create the performance assessment object (that should be sent as a message to the learner module)
        PerformanceAssessment performanceAssessment = assessmentProxy.generatePerformanceAssessment(proxyPerformanceAssessment);
        
        if(logger.isDebugEnabled()){
            logger.debug("Created Performance Assessment from conversation assessment of\n"+assessments);
        }
        
        return performanceAssessment;
    }

    /**
     * Generate the performance assessment using the performance assessment
     * level for the provided concepts.
     * 
     * @param concepts the LTI concepts that should be updated with the provided
     *        assessment level. The concepts should be contained within the list
     *        of course concepts (any that aren't will be skipped). Can't be null.
     * @param assessmentLevel the assessment level with which to update the
     *        provided concepts. Can't be null.
     * @return the updated performance assessment. Can be null if no assessment was updated.
     */
    public PerformanceAssessment assessPerformanceFromLTI(Set<String> concepts, AssessmentLevelEnum assessmentLevel) {
        if (concepts == null) {
            throw new IllegalArgumentException("The parameter 'concepts' cannot be null.");
        } else if (assessmentLevel == null) {
            throw new IllegalArgumentException("The parameter 'assessmentLevel' cannot be null.");
        }

        boolean assessmentUpdated = false;
        for (String concept : concepts) {
            /* Skip if no concept exists in the course */
            if (!courseConceptToNodeId.containsKey(concept.toLowerCase())) {
                continue;
            }

            UUID conceptUUID = courseConceptToNodeId.get(concept.toLowerCase());
            AbstractAssessment assessment = assessmentProxy.get(conceptUUID);
            if (assessment instanceof ConceptAssessment) {
                /* found concept assessment object based on the concept name for
                 * the current concept */
                assessment.updateAssessment(assessmentLevel);
                assessmentProxy.fireAssessmentUpdate(assessment);
                assessmentUpdated = true;
            }
        }

        if (assessmentUpdated) {
            /* Create the performance assessment object (that should be sent as
             * a message to the learner module) */
            PerformanceAssessment performanceAssessment = assessmentProxy
                    .generatePerformanceAssessment(proxyPerformanceAssessment);
            return performanceAssessment;
        }

        return null;
    }

    /**
     * Create a performance assessment from the survey response and the survey rules authored for the course.
     * 
     * @param conceptSurveyTransition contains transition information for the concept survey including the criteria to assess
     * the concepts via rules to apply to the survey response.
     * @param surveyResponse contains the user's response to a concept survey  
     * @return a new performance assessment based on the survey response and the assessment rules. Won't be null.
     */
    public PerformanceAssessment assessPerformanceFromSurvey(generated.course.PresentSurvey.ConceptSurvey conceptSurveyTransition, 
            SurveyResponse surveyResponse){

        return assessPerformanceFromSurvey(null, conceptSurveyTransition.getConceptQuestions(), surveyResponse);
    }
    
    /**
     * Create a performance assessment from the current assessment information collected.  When called
     * before any scored surveys, assessed conversations or overall assessments from dkf it will contain just
     * the course concepts.
     * 
     * @return a new performance assessment from the current assessment information collected.
     */
    public PerformanceAssessment getCurrentPerformanceAssessment(){
        
        //Create the performance assessment object (that should be sent as a message to the learner module)
        PerformanceAssessment performanceAssessment = assessmentProxy.generatePerformanceAssessment(proxyPerformanceAssessment);
        
        if(logger.isDebugEnabled()){
            logger.debug("Created Performance Assessment from Recall survey of "+performanceAssessment);
        }
        
        return performanceAssessment;
    }
    
    /**
     * Create a performance assessment from the survey response and the survey rules authored for the course.
     * 
     * @param handler the Merrill's branch point handler for when the survey is given as part of a Merrill's branch point course transition.
     *          Note: can be null if the survey is a 'prior knowledge' type test.
     * @param conceptQuestionsCollection the course authored rules for scoring the concept based survey
     * @param surveyResponse contains the user's response to a concept survey  
     * @return a new performance assessment based on the survey response and the assessment rules. Won't be null.
     */
    public PerformanceAssessment assessPerformanceFromSurvey(MerrillsBranchPointHandler handler, List<generated.course.ConceptQuestions> conceptQuestionsCollection, SurveyResponse surveyResponse){
        
        if(logger.isInfoEnabled()){
            logger.info("Assessing performance from Concept Survey results.");
        }
        
        //
        // Get the concept based score from the survey response
        //
        SurveyConceptAssessmentScore surveyConceptAssessmentScore = null;
        
        List<ScoreInterface> scores = SurveyScorerManager.getScores(surveyResponse);
        for(ScoreInterface score : scores){
            
            if(score instanceof SurveyConceptAssessmentScore){
                surveyConceptAssessmentScore = (SurveyConceptAssessmentScore) score;
                break;
            }
        }
        
        if(surveyConceptAssessmentScore == null){
            logger.error("Unable to calculate the concept based survey assessment from the survey response of "+surveyResponse+".  Does at least 1 question have the necessary question properties (i.e. concept name, correct answer) to score the responses based on concepts?");
            throw new IllegalArgumentException("Unable to calculate survey concept assessment score from the survey response from survey named "+surveyResponse.getSurveyName()+"."); 
        }
        
        //
        // Update the concept performance assessment nodes 
        //
        for(generated.course.ConceptQuestions conceptQuestions : conceptQuestionsCollection){
            
            //the concept being taught/assessed in the current Merrill's branch point transition
            String concept = conceptQuestions.getName();
            
            //the recall survey rules for this concept
            generated.course.ConceptQuestions.AssessmentRules assessmentRules = conceptQuestions.getAssessmentRules();
            
            //get the survey scoring details for this concept
            ConceptOverallDetails details = surveyConceptAssessmentScore.getConceptDetails().get(concept);            

            //update the concept question priority for this branch point to use in future recall survey requests
            Set<Integer> correctQuestions = details.getCorrectQuestions();
            Set<Integer> incorrectQuestions = details.getIncorrectQuestions();
            if(handler != null){
                handler.updateLastTimeAnswered(correctQuestions, incorrectQuestions);
            }
            
            //Apply the recall rule for this concept
            int numberCorrect = correctQuestions.size();
            
            //Note: an unknown value is handled as an above expectation later on so don't use unknown here
            AssessmentLevelEnum assessmentLevel = AssessmentLevelEnum.BELOW_EXPECTATION;
            int aboveThreshold = assessmentRules.getAboveExpectation().getNumberCorrect().intValue();
            int atThreshold = assessmentRules.getAtExpectation().getNumberCorrect().intValue();
            int belowThreshold = assessmentRules.getBelowExpectation().getNumberCorrect().intValue();
            if(aboveThreshold <= numberCorrect){
                assessmentLevel = AssessmentLevelEnum.ABOVE_EXPECTATION;
            }else if(atThreshold <= numberCorrect && atThreshold != 0){
                //This block is never executed if the at expectation level is never used (i.e. atNumber == 0)
                assessmentLevel = AssessmentLevelEnum.AT_EXPECTATION;
            }else if(belowThreshold <= numberCorrect){
                //Note: as of 1/2017 the below expectation value is always zero as set by the GAT, therefore
                //      this is essentially an else statement, i.e. the fall through case.
                assessmentLevel = AssessmentLevelEnum.BELOW_EXPECTATION;
            }           
                
            //get the concept performance assessment to update by unique concept name
            for(String courseConceptName : courseConceptToNodeId.keySet()){
                
                AbstractAssessment assessment = assessmentProxy.get(courseConceptToNodeId.get(courseConceptName));
                if(assessment instanceof ConceptAssessment && courseConceptName.equalsIgnoreCase(concept)){
                    //found concept assessment object based on the concept name for the current concept
                    
                    assessment.updateAssessment(assessmentLevel);
                    assessment.updatePriority(assessment.getPriority());
                    assessmentProxy.fireAssessmentUpdate(assessment);
                    break;
                }
            }

        }
        
        //Create the performance assessment object (that should be sent as a message to the learner module)
        PerformanceAssessment performanceAssessment = assessmentProxy.generatePerformanceAssessment(proxyPerformanceAssessment);
        
        if(logger.isDebugEnabled()){
            logger.debug("Created Performance Assessment from Recall survey of "+performanceAssessment);
        }
        
        return performanceAssessment;
        
    }
    
    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes. 
     */
    public void close(){
        getConversationManager().stopAllConversations();
        progressReporters.clear();
        merrillsBranchPointManager.cleanup();
        courseConceptsProxyTaskAssessment = null;
    }


    /**
     * Used to report the progress (if the progress reporter is valid) of the learner in the course.
     * The progress is measured based on the initial set of course transitions.  Transitions that are added
     * dynamically once the course is started are ignored.  The current progress tracks which transition index the learner
     * has completed versus the maximum progress which tracks the final transition index of the course.
     * 
     * @param isFinalProgressReport whether this is the final notification of progress because the course is ending.</br>
     * Note: when true this will prevent ALL subsequent progress reporting calls because there is only one final progress report.
     */
    public synchronized void reportProgress(boolean isFinalProgressReport) {
           
        for(AbstractProgressReporter progressReporter : progressReporters){
            progressReporter.reportProgress(getCurrentProgress(), getMaxProgress(), isFinalProgressReport);
        }
        
        //safe guard for sending final progress multiple times
        if(isFinalProgressReport){
            progressReporters.clear();
        }
    }
}
