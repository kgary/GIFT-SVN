/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.engine;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.ped.ActionEnum;
import generated.ped.Attribute;
import generated.ped.EMAP;
import generated.ped.MetadataAttribute;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.CourseState.ExpandableCourseObjectStateEnum;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ActiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ConstructiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.InteractiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo.AdvancementConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.MetadataAttributeItem;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.PassiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ProgressionInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.ped.PedagogicalModel;

/**
 * This class is the implementation of the ICAP (interactive constructive active passive) pedagogical model.
 * The Passive component is backed by the effort done for the "engine for management of Adaptive Pedagogy" (eMAP) which follows a
 * pre-configured layout of quadrant and learner characteristics to find metadata attributes of interest (back by research) that
 * can drive the presentation of domain content.  An instance of this class is created for each domain session.
 *  
 * @author mhoffman
 *
 */
public class ICAPAgent implements PedagogicalModel{
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ICAPAgent.class);

    /** the reason for requesting branch adaptation request of progression */
    private static final String CONTINUE_REASON = "Continue Adaptive courseflow";
    
    /** the (prefix) reason for requesting branch adaptation to skip rule/example/recall phases */
    private static final String EXPERT_KNOWLEDGE_REASON = "Learner has shown expert knowledge on ";
    
    /** the (prefix) reason for requesting branch adaptation to remediation on one or more concepts [after recall] */
    private static final String REMEDIATION_REASON = "Learner needs remediation on ";
    
    /** the (prefix) reason for requesting branch adaptation to remediation on one or more concepts [after practice] */
    private static final String AFTER_PRACTICE_REMEDATION_REASON = "Learner needs skill based remediation on ";
    
    /** the (prefix) reason for requesting branch adaptation to skip practice phase */
    private static final String EXPERT_SKILL_REASON = "Learner has shown expert skill on ";
    
    /** the reason for finishing an adaptive courseflow */
    private static final String FINISH_REASON = "Adaptive courseflow has been completed";
    
    /** the policy to use for populating remediation of the various types of activities in ICAP */
    private ICAPPolicy icapPolicy;
     
    /** 
     * used to randomly determine how many activity types (i.e. from the list of interactive, constructive, active, passive) 
     * are needed (1, 2, 3 or 4) 
     */
    private Random numOfActivitiesRandomizer = new Random();
    
    /** 
     * used to randomly determine the priority of the activity types that
     * were also randomly chosen
     */
    private Random activityWeightRandomizer = new Random();

    /** 
     * the current quadrant the user is at (or is the next transition) in the course.  
     * Null if not entering a quadrant. 
     * Examples:
     * i. Rule then Guidance the value would be null.
     * ii. Rule, Guidance then Example the value would be Example
     */
    private MerrillQuadrantEnum currentQuadrant = null;
    
    /** 
     * the previous quadrant the user was at in the course. 
     * Null if the last current quadrant was null.  
     * Examples: 
     * i. Rule then Guidance the value would be Rule.
     * ii. Rule, Guidance then Example the value would be null (because of Guidance). 
     */
    private MerrillQuadrantEnum previousQuadrant = null;
    
    /** 
     * the last quadrant the user was at in the course. 
     * Will only be null if no quadrant was ever entered. Only changes when currentQuadrant value changes from NOT null to something else. 
     * Examples:
     * i. Rule then Guidance the value would be Rule.
     * ii. Rule, Guidance then Example the value would be Rule
     */
    private MerrillQuadrantEnum lastQuadrant = null;
    
    /** the pedagogy configuration file content */
    private EMAP eMAP;
    
    /** the last learner state received from the learner module, can be null if a learner state has yet to be calculated */
    private LearnerState previousLearnerState = null;
    
    /** stores the {@link #previousLearnerState} at the time an expandable course object begins (e.g. adaptive course flow, training app w/ remediation) */
    private LearnerState preExpandableCourseObjectLearnerState = null;
    
    /** 
     * contains list of course concepts that are retrieved from a 
     * performance learner state with a unique task course concept name 
     */
    private Set<String> courseConcepts = null;
    
    /**
     * number of consecutive remediation attempts on the current course object
     */
    private int remediationCnt = 0;
    
    /**
     * holds the last course state value in order to compare changing values
     * on the next course state.  When not ExpandableCourseObjectStateEnum.NONE it means the current course object the learner is in
     * is part of an expandable course object (e.g. adaptive course flow, training app w/ remediation)
     */
    private ExpandableCourseObjectStateEnum prevExpandableCourseObjectState = ExpandableCourseObjectStateEnum.NONE;
    
    /**
     * Constructor - load the policy
     * @throws PedagogyFileValidationException if there was a problem configuration the pedagogical model using the configuration file.
     * @throws FileNotFoundException if the ICAP Policy file specified in the ped.properties could not be found.
     * @throws IllegalArgumentException if the ICAP policy file specified in the ped.properties is null or is a directory
     */
    public ICAPAgent() throws PedagogyFileValidationException, IllegalArgumentException, FileNotFoundException{
        icapPolicy = new ICAPFilePolicy();
    }
    
    /**
     * Set the current quadrant the user is at (or is the next transition) in the course.
     * 
     * @param currentQuadrant - can be null if not in a quadrant
     * @param ignoreAdvancement - whether the learner has chosen to take the adaptive courseflow knowledge 
     * phases as remediation instead of skipping those phases.  True means to not recommending advancing/skipping adaptive
     * courseflow phases.
     * @return ped requests for that quadrant.  Can be empty but not null.
     */
    public PedagogicalRequest setCurrentQuadrant(MerrillQuadrantEnum currentQuadrant, boolean ignoreAdvancement){  
        
        //
        // update the quadrant transition information
        //
        
        if(this.currentQuadrant != null && this.currentQuadrant != currentQuadrant){
            this.lastQuadrant = this.currentQuadrant;
        }
        
        previousQuadrant = this.currentQuadrant;
        this.currentQuadrant = currentQuadrant;
        
        //if there is a change in quadrants being requested, determine the pedagogical action to request
        //to the domain module
        if(previousQuadrant != currentQuadrant){ 
            if(logger.isDebugEnabled()){
                logger.debug("The quadrant changed from "+previousQuadrant+" to "+currentQuadrant+".");
            }
            PedagogicalRequest request = new PedagogicalRequest();
            getPedagogicalActions(previousLearnerState, request, ignoreAdvancement);
            buildCourseLevelPedRequest(request, ignoreAdvancement);
            return request;
        }else{
            return new PedagogicalRequest();
        }
    }
    
    /**
     * Check if the performance learner state has the course concepts.
     * If the course concepts are already known to this class this method will simply return.
     * 
     * @param performanceState contains a performance learner state snapshot of data
     */
    private void populateCourseConcepts(PerformanceState performanceState){
        
        if(courseConcepts != null){
            return;
        }
        
        for(TaskPerformanceState taskState : performanceState.getTasks().values()){
            
            PerformanceStateAttribute taskStateAttribute = taskState.getState();
            if(taskStateAttribute.getName().equals(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME)){ 
                //found the course concepts by the unique task name
                
                courseConcepts = new HashSet<>();
                
                for(ConceptPerformanceState conceptState : taskState.getConcepts()){  
                    populateCourseConcepts(conceptState);
                }
                
                break;
            }
        }
    }
    
    /**
     * Gather the course concepts from a learner state's concept performance provided.
     *  
     * @param conceptState contains a concept and possible subconcepts to gather the name of concepts from.
     */
    private void populateCourseConcepts(ConceptPerformanceState conceptState){
        
        if(conceptState == null){
            return;
        }else{
            
            PerformanceStateAttribute conceptStateAttribute = conceptState.getState();
            courseConcepts.add(conceptStateAttribute.getName().toLowerCase());
            
            if(conceptState instanceof IntermediateConceptPerformanceState){        
            
                for(ConceptPerformanceState subconceptState : ((IntermediateConceptPerformanceState)conceptState).getConcepts()){  
                    populateCourseConcepts(subconceptState);
                }
            }

        }
        
    }
    
    /**
     * Analyze the performance assessment to determine what, if any, type of remediation is warranted.
     * 
     * @param performanceState the performance state to analyze.  Can't be null.
     * @param phaseMetadataAttributes contains CDT phase mapped list of ideal metadata attributes retrieved
     * based on the current learner cognitive and affective state
     * @return remediation information on a per concept basis (i.e. at most one entry in the list per concept).  Can be null or empty.
     */
    private List<AbstractRemediationConcept> handlePerformanceState(PerformanceState performanceState, 
            Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes){        
        
        //not instantiation the list until it is determined that remediation is needed 
        List<AbstractRemediationConcept> remediation = null;
        
        //remediation request only come after the Recall quadrants (previous = Recall) and 
        //after the generated AAR (after Practice) quadrant (previous = null - cause it was AAR, last = Practice)
        if(previousQuadrant == MerrillQuadrantEnum.RECALL || (previousQuadrant == null && lastQuadrant == MerrillQuadrantEnum.PRACTICE)){
            
            //
            // Check concepts for the following to populate remediation info:
            //
            //    1) Below Expectation => Rule remediation
            //    2) At Expectation => Example remediation
            //    3) Above Expectation => no remediation
            //    4) Unknown => no remediation
            //
            
            for(TaskPerformanceState taskState : performanceState.getTasks().values()){
                
                //only check the 'course concepts' uniquely named task of the performance learner state as the other task nodes
                //will contain real-time assessment values which can cause unwarranted remediation requests
                if(!taskState.getState().getName().equals(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME)){
                    continue;
                }
                
                //instantiate the list if not already done so
                if(remediation == null){
                    remediation = new ArrayList<>(2);
                }                
                
                for(ConceptPerformanceState conceptState : taskState.getConcepts()){  
                
                    handleConceptPerformanceState(conceptState, phaseMetadataAttributes, remediation);
                }
            }
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("After analyzing the current learner state performance state course concepts the following course concepts need remediation:\n"+remediation);
        }
        
        return remediation;
    }
    
    /**
     * Determine which concepts need remediation.
     * 
     * @param conceptState contains the performance learner state information for a concept.  If the concept is
     * a subconcept than it also contains the descendant concepts information.
     * @param phaseMetadataAttributes contains CDT phase mapped list of ideal metadata attributes retrieved
     * based on the current learner cognitive and affective state
     * @param remediation where to place information about concepts that need remediation based on the concept performance provided.
     * Can't be null, can be empty.
     */
    private void handleConceptPerformanceState(ConceptPerformanceState conceptState, Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes, 
            List<AbstractRemediationConcept> remediation){
        
        if(conceptState == null){
            return;
        }else if(remediation == null){
            throw new IllegalArgumentException("The remediation list can't be null.");
        }

        AbstractRemediationConcept remediationConcept = null;                
        PerformanceStateAttribute stateAttribute = conceptState.getState();
        String conceptName = stateAttribute.getName();
        
        //check if this is a course concept, can only remediate on course concepts
        if(!courseConcepts.contains(conceptName.toLowerCase())){
            logger.warn("Provided course concept '"+conceptName.toLowerCase()+"' that is not being tracked by this ped model.  Current known course concepts are:"+courseConcepts);
            return;
        }

        //TODO: this logic could be expanded (beyond just short term value checks) to look at the 
        //      various levels of temporal assessments w/in the stateAttribute
        if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(stateAttribute.getShortTerm())){
            
            List<MetadataAttributeItem> metadataItems = phaseMetadataAttributes.get(MerrillQuadrantEnum.RULE);
            if(metadataItems == null){
                //the current learner state found no metadata attributes
                metadataItems = new ArrayList<>(0);
            }
            
            remediationConcept = new PassiveRemediationConcept(stateAttribute.getName(), metadataItems, MerrillQuadrantEnum.RULE);
            if(logger.isDebugEnabled()){
                logger.debug("The concept "+stateAttribute.getName()+" needs "+MerrillQuadrantEnum.RULE+" remediation.");
            }
            
            currentQuadrant = MerrillQuadrantEnum.RULE;   
            if(logger.isDebugEnabled()){
                logger.debug("Setting the next quadrant to "+currentQuadrant+" because the concept "+conceptName+" needs remediation in that regard.");
            }
            
        }else if(AssessmentLevelEnum.AT_EXPECTATION.equals(stateAttribute.getShortTerm())){
            
            List<MetadataAttributeItem> metadataItems = phaseMetadataAttributes.get(MerrillQuadrantEnum.RULE);
            if(metadataItems == null){
                //the current learner state found no metadata attributes
                metadataItems = new ArrayList<>(0);
            }
            
            remediationConcept = new PassiveRemediationConcept(stateAttribute.getName(), metadataItems, MerrillQuadrantEnum.EXAMPLE);
            
            if(logger.isDebugEnabled()){
                logger.debug("The concept "+conceptName+" needs "+MerrillQuadrantEnum.EXAMPLE+" remediation.");   
            }
            
            //don't update the next quadrant to transition too if the quadrant comes before the 
            //Example quadrant (i.e. the rule quadrant)
            if(currentQuadrant != MerrillQuadrantEnum.RULE){
                currentQuadrant = MerrillQuadrantEnum.EXAMPLE;
                
                if(logger.isDebugEnabled()){
                    logger.debug("Setting the next quadrant to "+currentQuadrant+" because the concept "+conceptName+" "
                            + "needs remediation in that regard (and no other concept needs more remediation thus far).");
                }
            }
        }
        
        //add remediation info to the list
        if(remediationConcept != null){                
            remediation.add(remediationConcept);
        }
        
        if(conceptState instanceof IntermediateConceptPerformanceState){
            //handle descendants
            
            for(ConceptPerformanceState subConceptState : ((IntermediateConceptPerformanceState)conceptState).getConcepts()){
                handleConceptPerformanceState(subConceptState, phaseMetadataAttributes, remediation);
            }
        }
    }
    
    /**
     * Handle the learner cognitive state by determining the appropriate ped actions.
     * 
     * @param cognitive - the latest learner cognitive state
     * @param phaseMetadataAttributes contains CDT phase mapped list of ideal metadata attributes to populate
     * based on the current learner cognitive state
     * @param request - the pedagogical request being built which contains pedagogical actions to use for a branch adaptation request
     * @param ignoreAdvancement - whether the learner has chosen to take the adaptive courseflow knowledge 
     * phases as remediation instead of skipping those phases.  True means to not recommending advancing/skipping adaptive
     * courseflow phases.
     */
    private void handleCognitiveState(CognitiveState cognitive, 
            Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes, PedagogicalRequest request, boolean ignoreAdvancement){
        
        if( cognitive != null ) {
            //get the cognitive state attributes to use in eMAP tree lookups
            getAttributes(cognitive.getAttributes(), currentQuadrant, phaseMetadataAttributes);
            
            if(ignoreAdvancement){
                // no further learner state cognitive attribute processing needed
                return;
            }
            
            //determine if a branch adaptation request for Advancement is warranted --
            //check the cognitive state for 'knowledge' attribute(s) that will warrant
            //branch adaptation requests (e.g. request skip Rules/Examples/Recall for
            //concepts the user is deemed an expert on)
            //*** If the previous cognitive knowledge state for these identified attributes is
            //    the same, then a request is not needed because a request should have been sent out when the
            //    previous learner state was received.                    
            LearnerStateAttribute knowledgeState = getAttribute(cognitive, LearnerStateAttributeNameEnum.KNOWLEDGE);
            
            if(knowledgeState != null && knowledgeState instanceof LearnerStateAttributeCollection){
                //if knowledge is broken down into a collection of attribute(s) we can get the 
                //concepts the learner is an expert on      
                List<AdvancementConcept> advancementConceptList = new ArrayList<>();
                determineAdvancementInfoFromExpertise(knowledgeState, advancementConceptList);
                if (!advancementConceptList.isEmpty()) {
                    //only author this strategy if there is advancement information to send                     

                    AdvancementInfo advancementInfo = new AdvancementInfo(advancementConceptList);                       
                    BranchAdaptationStrategy strategy = new BranchAdaptationStrategy(advancementInfo);

                    StringBuilder sb = new StringBuilder(EXPERT_KNOWLEDGE_REASON);
                    for(AdvancementConcept aConcept : advancementConceptList){
                        sb.append(Constants.SPACE).append(aConcept.getConcept()).append(Constants.COMMA);
                    }
                    RequestBranchAdaptation branchAdaptationRequest = new RequestBranchAdaptation(strategy);
                    branchAdaptationRequest.setIsMacroRequest(true);
                    request.addRequest(sb.toString(), branchAdaptationRequest);
                }
                    
            }
            
            //determine if a branch adaptation request for Remediation is warranted --
            //check the cognitive state for 'skill' attribute(s) that will warrant
            //branch adaptation requests (e.g. request remediation -Example only- on
            //concepts the user is deemed a novice on)
            //*** If the previous cognitive knowledge state for these identified attributes is
            //    the same, then a request is not needed because a request should have been sent out when the
            //    previous learner state was received.                    
            LearnerStateAttribute skillState = getAttribute(cognitive, LearnerStateAttributeNameEnum.SKILL);
            
            if(skillState != null && skillState instanceof LearnerStateAttributeCollection){
                //if skill is broken down into a collection of attribute(s) we can get the 
                //concepts the learner is an novice on      
                
                List<MetadataAttributeItem> exampleItems = new ArrayList<>();
                getAttributes(previousLearnerState, MerrillQuadrantEnum.EXAMPLE, exampleItems);
                
                List<AbstractRemediationConcept> remediationConceptList = new ArrayList<>();
                determineRemediationInfoFromNovice(skillState, exampleItems, remediationConceptList);
                
                List<AdvancementConcept> advancementConceptList = new ArrayList<AdvancementConcept>();
                determineAdvancementInfoFromExpertise(skillState, advancementConceptList);
                
                if(!remediationConceptList.isEmpty()){
                    //only author this strategy if there is remediation information to send                     
                    
                    RemediationInfo remediationInfo = new RemediationInfo();
                        StringBuilder sb = new StringBuilder(AFTER_PRACTICE_REMEDATION_REASON);

                    for(AbstractRemediationConcept remediationConcept : remediationConceptList){
                        
                        icapPolicy.getRemediation(remediationConcept, remediationInfo);
                        sb.append(Constants.SPACE).append(remediationConcept.getConcept()).append(Constants.COMMA);
                    }
                    
                    remediationInfo.setAfterPractice(true);
                    BranchAdaptationStrategy strategy = new BranchAdaptationStrategy(remediationInfo);
                    
                    RequestBranchAdaptation branchAdaptationRequest = new RequestBranchAdaptation(strategy);
                    branchAdaptationRequest.setIsMacroRequest(true);
                    request.addRequest(sb.toString(), branchAdaptationRequest);                    
                }
                
                if(!advancementConceptList.isEmpty()) {
                    //the learner is an expert for skill on one or more course concepts,
                    //need to let the domain module know so practice on these concepts can be skipped
                    
                        AdvancementInfo advancementInfo = new AdvancementInfo(advancementConceptList, true);
                        BranchAdaptationStrategy strategy = new BranchAdaptationStrategy(advancementInfo);
                        
                        StringBuilder sb = new StringBuilder(EXPERT_SKILL_REASON);
                        for(AdvancementConcept aConcept : advancementConceptList){
                            sb.append(Constants.SPACE).append(aConcept.getConcept()).append(Constants.COMMA);
                        }
                        
                        RequestBranchAdaptation branchAdaptationRequest = new RequestBranchAdaptation(strategy);
                        branchAdaptationRequest.setIsMacroRequest(true);
                        request.addRequest(sb.toString(), branchAdaptationRequest);
                }
            }
        }
    }
    
    /**
     * Return the Cognitive learner state attribute based on the type provided (can be an attribute collection) for
     * the specified cognitive state.
     * 
     * @param cognitiveState a learner state cognitive state object which may contain the attribute type.
     * @param attributeType the attribute type to find in the cognitive state
     * @return LearnerStateAttribute can be null if the attribute was not found.
     */
    private LearnerStateAttribute getAttribute(CognitiveState cognitiveState, LearnerStateAttributeNameEnum attributeType){
        
        for(LearnerStateAttributeNameEnum anAttributeType : cognitiveState.getAttributes().keySet()){
            
            if(anAttributeType == attributeType){
                //found the attribute
                
                LearnerStateAttribute attribute = cognitiveState.getAttributes().get(anAttributeType);
                return attribute;
            }
        }
        
        return null;
    }
    
    /**
     * Populate the advancement information list based on the cognitive knowledge state attribute provided.
     * If the attribute is not a collection than it will not be possible to retrieve labeled attribute values
     * (e.g. knowledge collection contains {'concept a' : expert, novice, novice, 'concept b' : novice, novice, journeyman} )
     * 
     * @param cognitiveStateAttribute the cognitive knowledge state attribute to analyze
     * @param advancementConceptList the collection to add new advancement information too.  May not add any items.
     */
    private void determineAdvancementInfoFromExpertise(LearnerStateAttribute cognitiveStateAttribute, List<AdvancementConcept> advancementConceptList){
        
        if(cognitiveStateAttribute instanceof LearnerStateAttributeCollection){
            //the attribute is most likely a task or intermediate concept cognitive state attribute, check its children
         
            for(String label : ((LearnerStateAttributeCollection)cognitiveStateAttribute).getAttributes().keySet()){
                
                LearnerStateAttribute childStateAttribute = ((LearnerStateAttributeCollection)cognitiveStateAttribute).getAttributes().get(label);
                if(childStateAttribute.getShortTerm() == ExpertiseLevelEnum.EXPERT){
                    //the learner is an expert at this labeled thing (e.g. task, concept), therefore
                    //added information that this thing should be skipped in the course
                    
                    AdvancementConcept concept = new AdvancementConcept(label);
                    if(!advancementConceptList.contains(concept)){
                        advancementConceptList.add(concept);
                    }
                }
                
                //are there more children to this state attribute that may contain other labeled things
                if(childStateAttribute instanceof LearnerStateAttributeCollection){
                    determineAdvancementInfoFromExpertise(childStateAttribute, advancementConceptList);
                }
            }
        }
    }
    
    /**
     * Populate the remediation information list based on the cognitive state attribute provided.
     * If the attribute is not a collection than it will not be possible to retrieve labeled attribute values
     * (e.g. collection contains {'concept a' : expert, novice, novice, 'concept b' : novice, novice, journeyman} )
     * 
     * @param cognitiveStateAttribute the cognitive state attribute to analyze
     * @param metadataItems the ideal metadata attributes for the current learner state
     * @param remediationConceptList the collection to add new remediation information too.  May not add any items.
     */
    private void determineRemediationInfoFromNovice(LearnerStateAttribute cognitiveStateAttribute, 
            List<MetadataAttributeItem> metadataItems, List<AbstractRemediationConcept> remediationConceptList){
        
        if(cognitiveStateAttribute instanceof LearnerStateAttributeCollection){
            //the attribute is most likely a task or intermediate concept cognitive state attribute, check its children
         
            for(String label : ((LearnerStateAttributeCollection)cognitiveStateAttribute).getAttributes().keySet()){
                
                //check if this is a course concept, can only remediate on course concepts
                if(courseConcepts == null || !courseConcepts.contains(label.toLowerCase())){
                    continue;
                }
                
                LearnerStateAttribute childStateAttribute = ((LearnerStateAttributeCollection)cognitiveStateAttribute).getAttributes().get(label);
                if(childStateAttribute.getShortTerm() == ExpertiseLevelEnum.NOVICE){
                    // the learner is an novice at this labeled thing (e.g. task, concept), therefore
                    // added information that this thing should be remediated in the course
                    
                    AbstractRemediationConcept concept = new PassiveRemediationConcept(label, metadataItems, MerrillQuadrantEnum.EXAMPLE);
                    remediationConceptList.add(concept);
                }
                
                // are there more children to this state attribute that may contain other labeled things
                if(childStateAttribute instanceof LearnerStateAttributeCollection){
                    determineRemediationInfoFromNovice(childStateAttribute, metadataItems, remediationConceptList);
                }
            }
        }
    }
    
    /**
     * Handle the learner affective assessment by determining the appropriate ped actions.
     * 
     * @param affective - the latest learner affective state
     * @param phaseMetadataAttributes contains CDT phase mapped list of ideal metadata attributes to populate
     * based on the current learner affective state
     */
    private void handleAffectiveState(AffectiveState affective, Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes){

        if( affective != null) {
            getAttributes(affective.getAttributes(), currentQuadrant, phaseMetadataAttributes);
        }   
    }
    
    /**
     * Populate the list of metadata attributes with attributes found by traversing the eMAP tree based on the current
     * learner state attributes.
     * 
     * @param learnerState - the learner state whose attributes can be used to find EMAP metadata attributes
     * @param quadrant - the quadrant to start at in the eMAP tree.  If null, this method will not add to the items collection.
     * @param items - list of metadata items to add too based on the eMAP traversal.
     */
    private void getAttributes(LearnerState learnerState, 
            MerrillQuadrantEnum quadrant, List<MetadataAttributeItem> items){
        
        List<Attribute> attributes = getAttributesForQuadrant(quadrant);
        if(attributes == null){
            return;
        }
        
        if(learnerState != null){
            getAttributes(attributes, learnerState.getCognitive().getAttributes(), items);
            getAttributes(attributes, learnerState.getAffective().getAttributes(), items);
        }
    }
    
    /**
     * Populate the list of metadata attributes with attributes found by traversing the eMAP tree based on the current
     * learner state attributes.
     * 
     * @param learnerAttributes - collection of current learner state attributes
     * @param quadrant - the quadrant to start at in the eMAP tree.  If null, this method will not add to the items collection.
     * @param phaseMetadataAttributes contains CDT phase mapped list of ideal metadata attributes to populate
     * based on the current learner state attributes
     */
    private void getAttributes(Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> learnerAttributes, 
            MerrillQuadrantEnum quadrant, Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes){
        
        if(quadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){
            //after recall remediation can require either rule or example phases therefore need both attributes
            
            getAttributes(learnerAttributes, MerrillQuadrantEnum.RULE, phaseMetadataAttributes);
            getAttributes(learnerAttributes, MerrillQuadrantEnum.EXAMPLE, phaseMetadataAttributes);
            
        }else{
            //get attributes for specific EMAP encoded CDT phase
            List<Attribute> attributes = getAttributesForQuadrant(quadrant);
            if(attributes == null){
                return;
            }
            
            List<MetadataAttributeItem> items = phaseMetadataAttributes.get(quadrant);
            if(items == null){
                items = new ArrayList<>();
                phaseMetadataAttributes.put(quadrant, items);
            }
            
            getAttributes(attributes, learnerAttributes, items);

        }
        
    }
    
    /**
     * Return the collection of EMAP metadata attributes for the phase provided.
     * 
     * @param quadrant the adaptive course flow phase the attributes are needed for.
     * @return the collection of metadata attributes configured for that phase.
     */
    private List<Attribute> getAttributesForQuadrant(MerrillQuadrantEnum quadrant){
     
        List<Attribute> attributes;
        if(quadrant == MerrillQuadrantEnum.RULE){            
            attributes = eMAP.getRule().getAttributes().getAttribute();
        }else if(quadrant == MerrillQuadrantEnum.EXAMPLE){
            attributes = eMAP.getExample().getAttributes().getAttribute();
        }else if(quadrant == MerrillQuadrantEnum.RECALL){
            attributes = eMAP.getRecall().getAttributes().getAttribute();
        }else if(quadrant == MerrillQuadrantEnum.PRACTICE){
            attributes = eMAP.getPractice().getAttributes().getAttribute();
        }else{
            //the quadrant is null when the course state is not in a
            //merrill's branch point which happens here when a pre-test
            //is given.
            return null;
        }
        
        return attributes;
    }
    
    /**
     * Populate the list of metadata attributes with attributes found by traversing the eMAP tree based on the current
     * learner state attributes.
     * 
     * @param attributes - eMAP tree attributes to search through
     * @param learnerAttributes - collection of current learner state attributes
     * @param items - list of metadata items to add too based on the eMAP traversal.
     */
    public void getAttributes(List<Attribute> attributes, Map<LearnerStateAttributeNameEnum, 
            LearnerStateAttribute> learnerAttributes, List<MetadataAttributeItem> items){
        
        //search the eMAP attributes for the learner state attributes known right now
        for(Attribute eMAPAttribute : attributes){
            
            //an eMAP attribute type
            String typeStr = eMAPAttribute.getType();
            
            //the eMAP attribute type enumeration
            LearnerStateAttributeNameEnum type = LearnerStateAttributeNameEnum.valueOf(typeStr);
            
            //the current learner state attribute value for this eMAP attribute type
            LearnerStateAttribute stateAttribute = learnerAttributes.get(type);
            
            if(stateAttribute != null){
                //found an attribute in the eMAP that is in the learner state
                
                //the eMAP attribute type's value 
                String valueStr = eMAPAttribute.getValue();
                
                //the eMAP attribute type's value enumeration (use to compare against the learner state attribute value enumeration)
                AbstractEnum eMAPValue = type.getAttributeValue(valueStr);
                
                addMetadata(eMAPAttribute, eMAPValue, stateAttribute, null, items);
            }
            
        }//end for
    }
    
    /**
     * Add to the metadata items collection based on the learner state attribute provided and matching attribute
     * value found in the eMAP tree.  When the attribute provided is a collection this method will be called 
     * recursively and the label parameter will be used in order to distinguish attributes of the same type
     * but with different values.  This way a state type like knowledge can have metadata attributes sets for
     * 'concept A' versus 'concept B'.
     * 
     * @param eMAPAttribute the eMAP attribute that matches the learner state attribute type (but not the value necessarily)
     * @param eMAPValue the eMAP value for the attribute that will be compared to the learner state attribute type's value.
     *                  If they match, the metadata attributes will be added to the items collection.
     * @param stateAttribute the learner state attribute to analyze and compare to the eMAP value
     * @param label a label (e.g. 'concept B') that can be paired with the metadata attributes being added to the collection. 
     *              Can be null if a label is not needed for these metadata attributes.
     * @param items the collection to add metadata attributes too.  This list will be used by the Domain module to search for the
     *              appropriate content to present.
     */
    private void addMetadata(Attribute eMAPAttribute, AbstractEnum eMAPValue, LearnerStateAttribute stateAttribute, String label, List<MetadataAttributeItem> items){
        
        if(stateAttribute instanceof LearnerStateAttributeCollection){
            
            LearnerStateAttributeCollection collection = (LearnerStateAttributeCollection)stateAttribute;
            for(String childLabel : collection.getAttributes().keySet()){
                
                stateAttribute = collection.getAttributes().get(childLabel);
                addMetadata(eMAPAttribute, eMAPValue, stateAttribute, childLabel, items);
            }
            
        }else{
            AbstractEnum stateValue = stateAttribute.getShortTerm();  
            
            //the attribute value in the eMAP tree must match the value in this  learner state attribute
            if(stateValue == eMAPValue){
            
                //
                //add metadata attributes to list
                //
                for(MetadataAttribute mAttr : eMAPAttribute.getMetadataAttributes().getMetadataAttribute()){
                    
                    generated.metadata.Attribute metadataAttribute = new generated.metadata.Attribute();
                    metadataAttribute.setValue(mAttr.getValue());
                    
                    MetadataAttributeItem item = new MetadataAttributeItem(metadataAttribute);
                    item.setLabel(label);
        
                    int index = items.indexOf(item);
                    if(index != -1){
                        //metadata attribute already exists in list, increase its priority
                        
                        items.get(index).increasePriority();
                        
                    }else{
                        //this is a new metadata attribute, add it to the list
                        
                        items.add(item);
                    }
        
                }
            }
        }
    }
    
    @SuppressWarnings("unused")
    private AbstractEnum getLowestExpertiseLevel(LearnerStateAttribute stateAttribute){
        
        AbstractEnum lowestExpertiseLevel = stateAttribute.getShortTerm();
        if(stateAttribute instanceof LearnerStateAttributeCollection){
            
            for(LearnerStateAttribute childStateAttribute : ((LearnerStateAttributeCollection)stateAttribute).getAttributes().values()){
                
                AbstractEnum tempLowestExpertiseLevel = getLowestExpertiseLevel(childStateAttribute);
                if(lowestExpertiseLevel == ExpertiseLevelEnum.NOVICE){
                    //reached the lowest level, all done
                    break;
                }else if(tempLowestExpertiseLevel == ExpertiseLevelEnum.NOVICE){
                    //the newly found lowest is the lowest level, all done
                    lowestExpertiseLevel = tempLowestExpertiseLevel;
                    break;
                }else if(lowestExpertiseLevel == ExpertiseLevelEnum.EXPERT && tempLowestExpertiseLevel == ExpertiseLevelEnum.JOURNEYMAN){
                    //the newly found lowest is lower than the current
                    lowestExpertiseLevel = tempLowestExpertiseLevel;
                }else if(lowestExpertiseLevel == ExpertiseLevelEnum.UNKNOWN && tempLowestExpertiseLevel != ExpertiseLevelEnum.UNKNOWN){
                    //the newly found lowest is better than unknown
                    lowestExpertiseLevel = tempLowestExpertiseLevel;
                }
            }
            
        }
        
        return lowestExpertiseLevel;
    }
    
    /**
     * Create pedagogical request(s) specific to the next quadrant to be entered based on
     * the current state of the learner. 
     * 
     * @param request the pedagogical request being built which contains pedagogical actions
     * @param ignoreAdvancement - whether the learner has chosen to take the adaptive courseflow knowledge 
     * phases as remediation instead of skipping those phases.  True means to not recommending advancing/skipping adaptive
     * courseflow phases.
     */ 
    private void buildCourseLevelPedRequest(PedagogicalRequest request, boolean ignoreAdvancement){
        
        if(previousLearnerState != null){
            //need a learner state for gathering EMAP metadata attributes

            synchronized(previousLearnerState){  
                //don't want the state to change while we are searching for metadata attributes
                
                //A branch point course transition contains quadrants of which either Recall or Practice
                //is the last quadrant.  Therefore if recall is the last quadrant the following or 'current quadrant'
                //will be null when the domain module is requesting to exit the branch point transition after 
                //the recall quadrant has been completed and there is no practice next.
                //If the previous quadrant is null and the current quadrant is Example, then this is a request for 
                //after Practice remediation.
                //If the previous quadrant is Practice and the current quadrant is null, then check if after Practice
                //remediation is needed before existing the adaptive course flow object
                if(previousQuadrant == MerrillQuadrantEnum.RECALL || currentQuadrant != null){ 

                    Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes = new HashMap<>();
                    
                    //Metadata attributes are only important during the Rule/Example/Practice quadrants of the branch point
                    //transition (i.e. not Recall).
                    if(currentQuadrant != MerrillQuadrantEnum.RECALL){
                        
                        handleCognitiveState(previousLearnerState.getCognitive(), phaseMetadataAttributes, request, ignoreAdvancement);
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("After analyzing cognitive state, the phase based metadata attributes map looks like:\n"+phaseMetadataAttributes);
                        }
                
                        handleAffectiveState(previousLearnerState.getAffective(), phaseMetadataAttributes);
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("After analyzing affective state, the phase based metadata attributes map looks like:\n"+phaseMetadataAttributes);
                        }
                    }

                    //check for remediation information
                    //Note: this can change the next scheduled quadrant
                    List<AbstractRemediationConcept> concepts = handlePerformanceState(previousLearnerState.getPerformance(), phaseMetadataAttributes);
                    
                    //set the next schedule quadrant
                    MerrillQuadrantEnum nextQuadrant = currentQuadrant;

                    BranchAdaptationStrategy strategy;
                    String reason;
                    if(nextQuadrant != null){
                        //identifies that the branch point is not over and the specified quadrant should be executed next.
                        
                        if(CollectionUtils.isNotEmpty(concepts)){
                            RemediationInfo remediationInfo = new RemediationInfo();
                            StringBuilder sb = new StringBuilder(REMEDIATION_REASON);
                            for(AbstractRemediationConcept remediationConcept : concepts){
                                
                                icapPolicy.getRemediation(remediationConcept, remediationInfo);
                                sb.append(Constants.SPACE).append(remediationConcept.getConcept()).append(Constants.COMMA);
                            }
                            
                            strategy = new BranchAdaptationStrategy(remediationInfo);
                            reason = sb.toString();
                            
                        }else{
                            
                            List<MetadataAttributeItem> metadataItems = phaseMetadataAttributes.get(nextQuadrant);
                            if(metadataItems == null){
                                metadataItems = new ArrayList<>(0);
                            }
                            
                            ProgressionInfo progression = new ProgressionInfo(nextQuadrant, metadataItems);
                            strategy = new BranchAdaptationStrategy(progression);
                            reason = CONTINUE_REASON;

                        }
                    }else{
                        //the branch point is over, therefore no quadrant information, nor metadata is provided
                        strategy = new BranchAdaptationStrategy(new ProgressionInfo());
                        reason = FINISH_REASON;
                    }
                    
                    RequestBranchAdaptation branchAdaptationRequest = new RequestBranchAdaptation(strategy);
                    branchAdaptationRequest.setIsMacroRequest(true);
                    request.addRequest(reason, branchAdaptationRequest);                    
                }
                
            }//end synchronize

        }

    }
    
    /**
     * Given a learner state object, computes and returns the appropriate pedagogical action(s).
     * 
     * @param state state of the learner
     * @param request the pedagogical request contains pedagogical actions to take
     * @param ignoreAdvancement - whether the learner has chosen to take the adaptive courseflow knowledge 
     * phases as remediation instead of skipping those phases.  True means to not recommending advancing/skipping adaptive
     * courseflow phases.
     */
    public void getPedagogicalActions(
            LearnerState state, PedagogicalRequest request, boolean ignoreAdvancement) {
        
        if(state != null){ 
            
            populateCourseConcepts(state.getPerformance());
            
            // After failed Recall:
            // A. with practice next, current = recall, previous = recall
            // B. with no practice next, current = ?, previous = ?
            // After successful Recall:
            // A. with practice next, current = recall, previous = recall ... 
            //         followed by current = remediation after recall, previous = recall ... 
            //         followed by current = null, previous = remediation after recall [then structured review is shown]
            // After structured review after successful recall:
            // A. with practice next, current = Practice After Recall, previous = null...
            //         followed by current = null, previous = Practice ...
            //         followed by current = null, previous = Practice (again).. and a third time [then practice activity is shown]
            // After failed Practice:
            //    current = null, previous = Practice [GETS THE METADATA ATTRIBUTES!!!]     
            if(currentQuadrant == null && previousQuadrant != MerrillQuadrantEnum.PRACTICE){
                //this learner state arrived while not in a branch point course transition, 
                //and not leaving a practice -
                //check the cognitive state for 'knowledge' attribute(s) that will warrant
                //branch adaptation requests (e.g. request skip Rules/Examples/Recall for
                //concepts the user is deemed an expert on)
                if(logger.isInfoEnabled()){
                    logger.info("Gathering actions (not metadata) based on cognitive state.");
                }
                handleCognitiveState(state.getCognitive(), null, request, ignoreAdvancement);
                
            }else if(currentQuadrant == null && previousQuadrant == MerrillQuadrantEnum.PRACTICE){
                
                if(logger.isInfoEnabled()){
                    logger.info("Gathering metadata attributes based on cognitive state.");
                }
                Map<MerrillQuadrantEnum, List<MetadataAttributeItem>> phaseMetadataAttributes = new HashMap<>();
                handleCognitiveState(state.getCognitive(), phaseMetadataAttributes, request, ignoreAdvancement);
                
//            }else if(currentQuadrant == MerrillQuadrantEnum.EXAMPLE && previousQuadrant == null){
//                
//                logger.info("Gathering metadata attributes based on cognitive state.");
//                List<MetadataAttributeItem> items = new ArrayList<>();
//                handleCognitiveState(state.getCognitive(), items, actions);
                
            }else{
                if(logger.isDebugEnabled()){
                    logger.debug("Not creating pedagogical actions because the currentQuadrant = "+currentQuadrant+
                            ", previousQuadrant = "+previousQuadrant+".");
                }
            }
            
            //update state
            previousLearnerState = state;          

        }else if(currentQuadrant != null){
            //there is no learner state yet but the learner has entered a branch point course element quadrant (e.g. Rule)
            //therefore need to provide progression branch point request so the course continues
            
            ProgressionInfo progression = new ProgressionInfo(currentQuadrant, new ArrayList<MetadataAttributeItem>());
            BranchAdaptationStrategy strategy = new BranchAdaptationStrategy(progression);
            RequestBranchAdaptation branchAdaptationRequest = new RequestBranchAdaptation(strategy);
            request.addRequest(CONTINUE_REASON, branchAdaptationRequest);
            
            if(logger.isInfoEnabled()){
                logger.info("Providing a pedagogical request of "+strategy+" because there hasn't been a learner state yet but the learner has entered a branch point course element.");
            }
        }
    }

    @Override
    public void getPedagogicalActions(
            LearnerState state, PedagogicalRequest request) {
                
        getPedagogicalActions(state, request, false);

    }

    @Override
    public void initialize(
            InitializeDomainSessionRequest initDomainSessionRequest) {
        //Note: the reason this isn't static is because eventually the configuration file used here maybe driven by other means (e.g. course, learner, etc. specific)

    }

    @Override
    public void initialize(InitializePedagogicalModelRequest initModelRequest)
            throws DetailedException {
        
        EMAPConfigFileHandler handler = new EMAPConfigFileHandler(initModelRequest.getPedModelConfig(), null, true);
        eMAP = handler.getEMAP();       
    }
    
    @Override
    public PedagogicalRequest handleCourseStateUpdate(CourseState state) {
        
        boolean ignoreAdvancement = false;
        if(hasJustFailedRecall(state) || hasJustFailedPractice(state)){
            // increase number of current remediation attempts for policy checks later on
            remediationCnt++;
            
            if(logger.isDebugEnabled()){
                logger.debug("Incremented remediation count to "+remediationCnt);
            }
            
        }else if(isEnteringExpandableCourseObject(state)){
            // entering adaptive course flow OR training application w/ remediation course object, need to set variables used to
            // determine appropriate remediation activities in this course object
            
            if(state.getExpandableCourseObjectState() == ExpandableCourseObjectStateEnum.TRAINING_APPLICATION){
                // 06.29.2020 - before this if statement the remediation count was being incremented before being
                //              used elsewhere in this class for after failed practice during a training application course object.
                //              This is due to the differences in sequence of course state messages versus after practice
                //              in adaptive courseflow practice.  Therefore not applying the first request to increment in this 
                //              specific case.
                remediationCnt = -1;
            }else{
                remediationCnt = 0;
            }
            preExpandableCourseObjectLearnerState = previousLearnerState;
            
            if(logger.isDebugEnabled()){
                logger.debug("Entering expandable course object.  Reset remediation count and saved current learner state");
            }
            
        }else if(isExitingExpandableCourseObject(state)){
            // exiting adaptive course flow OR training application w/ remediation course object, 
            // need to set variables used so that they aren't used while out of the course object
            remediationCnt = 0;
            preExpandableCourseObjectLearnerState = null;
            
            if(logger.isDebugEnabled()){
                logger.debug("Exiting expandable course object.  Reset remediation count and set current learner state to null");
            }

        }
        
        prevExpandableCourseObjectState = state.getExpandableCourseObjectState();
        
        Serializable currAdaptiveCourseflowShelfLife = state.getLearnerStateShelfLife().get(LearnerStateAttributeNameEnum.KNOWLEDGE);
        if(currAdaptiveCourseflowShelfLife != null && currAdaptiveCourseflowShelfLife instanceof Long && ((Long)currAdaptiveCourseflowShelfLife).equals(0l)){
            // the learner has chosen to take the adaptive courseflow knowledge phases as remediation instead of skipping those phases
            ignoreAdvancement = true;
        }
        
        return setCurrentQuadrant(state.getNextQuadrant(), ignoreAdvancement);
    }
    
    /**
     * Return true if the incoming course state specifies that a new expandable course object
     * is being entered.
     * 
     * @param incomingCourseState the new course state notification from the Domain module
     * @return true if the previous value of {@link #prevExpandableCourseObjectState} was ExpandableCourseObjectStateEnum.NONE
     * and the incoming value is NOT ExpandableCourseObjectStateEnum.NONE.
     */
    private boolean isEnteringExpandableCourseObject(CourseState incomingCourseState){
        
        // true when the last course state said the learner wasn't in an expandable course object
        // and now the incoming state says the learner is in an expandable course object.
        boolean entered = prevExpandableCourseObjectState == ExpandableCourseObjectStateEnum.NONE && 
                incomingCourseState.getExpandableCourseObjectState() != ExpandableCourseObjectStateEnum.NONE;
        
        return entered;
    }
    
    /**
     * Return true if the incoming course state specifies that the current expandable course object
     * is being exited.
     * 
     * @param incomingCourseState the new course state notification from the Domain module
     * @return true if the previous value of {@link #prevExpandableCourseObjectState} was not ExpandableCourseObjectStateEnum.NONE
     * and the incoming value is ExpandableCourseObjectStateEnum.NONE.
     */
    private boolean isExitingExpandableCourseObject(CourseState incomingCourseState){
        
        // true when the last course state said the learner WAS in an expandable course object
        // and now the incoming state says the learner is NOT in an expandable course object.
        boolean exited = prevExpandableCourseObjectState != ExpandableCourseObjectStateEnum.NONE && 
                incomingCourseState.getExpandableCourseObjectState() == ExpandableCourseObjectStateEnum.NONE;
        
        return exited;
    }

     
    /**
     * Return whether the incoming course state update from the Domain module as an indication
     * of the next course object/activity about to be shown is indicative of having just failed
     * the check on learning (recall) phase of an adaptive course flow course object.
     * @param incomingCourseState the new course state notification from the Domain module
     * @return true if the course state is for entering no next phase of an adaptive course flow
     * course object (because currently entering mandatory structured review) and the current phase
     * is either practice OR example) and the last phase was Recall.
     */
    private boolean hasJustFailedRecall(CourseState incomingCourseState){
        return incomingCourseState.getNextQuadrant() == null && 
                (currentQuadrant == MerrillQuadrantEnum.RULE || currentQuadrant == MerrillQuadrantEnum.EXAMPLE) &&
                previousQuadrant == MerrillQuadrantEnum.RECALL &&
                lastQuadrant == MerrillQuadrantEnum.RECALL;
    }
    
    /**
     * Return whether the incoming course state update from the Domain module as an indication
     * of the next course object/activity about to be shown is indicative of having just failed
     * practice phase of an adaptive course flow course object.
     * @param incomingCourseState the new course state notification from the Domain module
     * @return true if the course state is for entering practice the first time:<br/>
     * remediation after recall (used for after practice as well)
     * and the current phase is null (because showing failure structured review) and the last phase
     * was practice;
     * or subsequent times:<br/>
     * null is the next quadrant and current, previous and last phases are all Practice.
     */
    private boolean hasJustFailedPractice(CourseState incomingCourseState){
        
        // the first time practice is entered has a slightly different signature than subsequent
        // times in the same adaptive course flow course object
        boolean failedFirstPractice = 
                incomingCourseState.getNextQuadrant() == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL && 
                currentQuadrant == null &&
                previousQuadrant == null &&
                lastQuadrant == MerrillQuadrantEnum.PRACTICE;
        
        boolean failedAfterFirstPractice =
                incomingCourseState.getNextQuadrant() == null && 
                currentQuadrant == MerrillQuadrantEnum.PRACTICE &&
                previousQuadrant == MerrillQuadrantEnum.PRACTICE &&
                lastQuadrant == MerrillQuadrantEnum.PRACTICE;
        
        // this is true when:
        // 1. adaptive courseflow with practice only - i.e. rule/example/recall were skipped
        // 2. training app course object w/ remediation, first attempt (lastQuadrant = null)
        // 3. training app course object w/ remediation, after first attempt (lastQuadrant = Practice)
        boolean failedPracticeOnlytExpandableCourseObject = 
                incomingCourseState.getNextQuadrant() == null && 
                currentQuadrant == MerrillQuadrantEnum.PRACTICE &&
                previousQuadrant == null &&
                (lastQuadrant == null || lastQuadrant == MerrillQuadrantEnum.PRACTICE);
        
        return failedFirstPractice || failedAfterFirstPractice || failedPracticeOnlytExpandableCourseObject;
    }    

    @Override
    public void handleLessonStarted() {
        // nothing        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ICAPAgent: ");
        sb.append("currentQuadrant = ").append(currentQuadrant);
        sb.append(", previousQuadrant = ").append(previousQuadrant);
        sb.append(", lastQuadrant = ").append(lastQuadrant);
        sb.append(", remediationCnt = ").append(remediationCnt);
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * The interface for all ICAP Policy classes.
     * 
     * @author mhoffman
     *
     */
    private interface ICAPPolicy{
        
        /**
         * Populates remediationInfo with one or more randomly selected ICAP activities for the concept
         * needing remediation.
         *  
         * @param remediationConcept the concept needing remediation.  This will most likely be a Passive remediation type
         * containing the ideal EMAP metadata attributes.
         * @param remediationInfo the object containing all remediation information for all concepts needing remediation.
         * This is where any randomly selected remediation activities will be placed, mapped to the concept needing remediation.
         */
        public void getRemediation(AbstractRemediationConcept remediationConcept, RemediationInfo remediationInfo);
    }
    
    /**
     * Uses a policy file to make decisions on which remediation activity to choose based on the specified action
     * for a set of state attribute (feature) values.
     * 
     * @author mhoffman
     *
     */
    private class ICAPFilePolicy implements ICAPPolicy{
        
        /**
         * 
         * @throws PedagogyFileValidationException if there was a problem configuration the pedagogical model using the configuration file.
         * @throws FileNotFoundException if the ICAP Policy file specified in the ped.properties could not be found.
         * @throws IllegalArgumentException if the ICAP policy file specified in the ped.properties is null or is a directory
         */
        public ICAPFilePolicy() throws PedagogyFileValidationException, IllegalArgumentException, FileNotFoundException {
            // make sure the ICAP policy file can be read in and is valid
            ICAPPolicyHandler.getInstance();
        }
        
        @Override
        public void getRemediation(AbstractRemediationConcept remediationConcept, RemediationInfo remediationInfo){
            
            String concept = remediationConcept.getConcept();
            
            generated.ped.ICAPPolicy icapPolicy;
            ICAPPolicyHandler icapPolicyHandler;
            try {
                icapPolicyHandler = ICAPPolicyHandler.getInstance();
                icapPolicy = icapPolicyHandler.getICAPPolicy();
            } catch (Exception e) {
                logger.error("Unable to check for the next remediation activity to request from the ICAP Policy due to an exception.", e);
                return;
            }
            
            generated.ped.StateAttributes stateAttributes = icapPolicy.getStateAttributes();

            List<String> currFeatureValues = new ArrayList<>(icapPolicyHandler.getStateAttributeSize());
                   
            // NOTE: ConceptPretestAboveNovice logic must be the 1st state attribute because its defined 1st in the schema
            if(stateAttributes.getConceptPretestAboveNovice() != null){
                // determine current (adaptive course course object concept) pretest score and whether it is 
                // above novice expertise level
                
                generated.ped.StateAttributes.ConceptPretestAboveNovice conceptPretestAboveNovice = stateAttributes.getConceptPretestAboveNovice();
                
                boolean conditionEvaluation = false;
                
                // explore the knowledge cognitive learner state attribute for the concept
                if(preExpandableCourseObjectLearnerState != null){
                    LearnerStateAttribute knowledgeState = getAttribute(preExpandableCourseObjectLearnerState.getCognitive(), LearnerStateAttributeNameEnum.KNOWLEDGE);
                    if(knowledgeState instanceof LearnerStateAttributeCollection){
                        LearnerStateAttribute expertiseStateAttribute = ((LearnerStateAttributeCollection)knowledgeState).getConceptExpertiseLevel(concept);
                        if(expertiseStateAttribute != null && 
                                (expertiseStateAttribute.getShortTerm() == ExpertiseLevelEnum.EXPERT || expertiseStateAttribute.getShortTerm() == ExpertiseLevelEnum.JOURNEYMAN)){
                            conditionEvaluation = true;
                        }
                    }
                }
                
                if(conditionEvaluation){
                    currFeatureValues.add(conceptPretestAboveNovice.getTrueValue());
                }else{
                    currFeatureValues.add(conceptPretestAboveNovice.getFalseValue());
                }
            }
            
            // NOTE: ConceptRemediationCount logic must be the 2nd state attribute because its defined 1st in the schema
            if(stateAttributes.getConceptRemediationCount() != null){
                // determine current (adaptive courseflow course object) remediation value according to remediation count state attribute
                // (if the remediation count state attribute is defined in the policy)
                
                generated.ped.StateAttributes.ConceptRemediationCount conceptRemediationCount = stateAttributes.getConceptRemediationCount();
                String operator = conceptRemediationCount.getTrueCondition().getOperator();
                int conditionValue = conceptRemediationCount.getTrueCondition().getValue().intValue();
                boolean conditionEvaluation = false;
                if(OperatorEnum.GT.getName().equals(operator)){
                    conditionEvaluation = remediationCnt > conditionValue;
                }else if(OperatorEnum.LT.getName().equals(operator)){
                    conditionEvaluation = remediationCnt < conditionValue;
                }else if(OperatorEnum.EQUALS.getName().equals(operator)){
                    conditionEvaluation = remediationCnt == conditionValue;
                }else if(OperatorEnum.GTE.getName().equals(operator)){
                    conditionEvaluation = remediationCnt >= conditionValue;
                }else if(OperatorEnum.LTE.getName().equals(operator)){
                    conditionEvaluation = remediationCnt <= conditionValue;
                }
                
                if(conditionEvaluation){
                    currFeatureValues.add(conceptRemediationCount.getTrueValue());
                }else{
                    currFeatureValues.add(conceptRemediationCount.getFalseValue());
                }
            }
            
            //
            // get the Action from the policy
            //
            
            List<AbstractRemediationConcept>remediations = new ArrayList<>();
            ActionEnum actionEnum = icapPolicyHandler.getActionForStateAttributeValues(currFeatureValues);
            boolean includedPassive = false;
            if(actionEnum == null){
                actionEnum = icapPolicy.getPolicies().getDefaultAction().getActionChoice();
            }
                
            if(ActionEnum.INTERACTIVE == actionEnum){
                //add an interactive activity request
                
                InteractiveRemediationConcept interactiveRemediation = new InteractiveRemediationConcept(concept);
                remediations.add(interactiveRemediation);
                
            }else if(ActionEnum.CONSTRUCTIVE == actionEnum){
                //add a constructive activity request
                
                ConstructiveRemediationConcept constructiveRemediation = new ConstructiveRemediationConcept(concept);
                remediations.add(constructiveRemediation);
                
            }else if(ActionEnum.ACTIVE == actionEnum){
                //add an active activity request
                
                ActiveRemediationConcept activeRemediation = new ActiveRemediationConcept(concept);
                remediations.add(activeRemediation);
                
            }else if(ActionEnum.PASSIVE == actionEnum){
                //add a passive activity request (use the passive request for this concept)
                
                remediations.add(remediationConcept);
                includedPassive = true;
            }
            
            if(!includedPassive && remediationConcept instanceof PassiveRemediationConcept){
                // make sure to include the passive remediation information as a fall back in case there
                // are no Interactive, constructive or active activities authored.  This contains the EMAP
                // metadata attributes to look for.
                remediations.add(remediationConcept);
            }
            
            remediationInfo.setConceptRemediation(remediationConcept.getConcept(), remediations);

        }

    }
    
    /**
     * Randomize any remediation branch adaptation requests found in the passive requests provided on a per
     * concept basis.  The randomize remediation request for a concept will contain, at most, one of each constructive, 
     * active and passive remediation activity details. The randomly selected request activity priorities are also random per concept.
     * 
     * Example:
     * Remediation is requested on 'concept a'.  The resulting randomized remediation might be
     * prioritized as 1. active, 2. constructive.  In this example the original passive remediation request created by
     * EMAP logic is not used.
     * 
     * @author mhoffman
     *
     */
    @SuppressWarnings("unused")
    private class ICAPRandomPolicy implements ICAPPolicy{
        
        @Override
        public void getRemediation(AbstractRemediationConcept remediationConcept, RemediationInfo remediationInfo){

            String concept = remediationConcept.getConcept();
            List<AbstractRemediationConcept> randomizeRemediations = new ArrayList<>();
            
            //
            // First - randomize how many activity types (interactive/constructive/active/passive) will be prioritized (value = 1,2,3 or 4)
            //
            int numOfActivities = numOfActivitiesRandomizer.nextInt(4) + 1;
            
            //
            // Second - randomize the weight for each of the three activity types
            //
            int interactiveWeight = activityWeightRandomizer.nextInt();
            int constructiveWeight = activityWeightRandomizer.nextInt();
            int activeWeight = activityWeightRandomizer.nextInt();
            int passiveWeight = activityWeightRandomizer.nextInt();
            List<Integer> weights = new ArrayList<>(4);
            weights.add(interactiveWeight);
            weights.add(constructiveWeight);
            weights.add(activeWeight);
            weights.add(passiveWeight);
            Collections.sort(weights);      //give ascending order
            Collections.reverse(weights);   //give descending order
            
            if(logger.isDebugEnabled()){
                logger.debug("ICAP Pedagogical request - concept = '"+concept+"', weights = {constructive "+constructiveWeight+", active "+activeWeight+", passive "+passiveWeight+"}");
            }
            
            //
            // Third - build the 'numOfActivities' number of requests
            //
            boolean includedPassive = false;
            for(int index = 0; index < weights.size() && index < numOfActivities; index++){
                
                int weight = weights.get(index);
                if(weight == interactiveWeight){
                    //add an interactive activity request
                    
                    InteractiveRemediationConcept interactiveRemediation = new InteractiveRemediationConcept(concept);
                    randomizeRemediations.add(interactiveRemediation);
                    
                }else if(weight == constructiveWeight){
                    //add a constructive activity request
                    
                    ConstructiveRemediationConcept constructiveRemediation = new ConstructiveRemediationConcept(concept);
                    randomizeRemediations.add(constructiveRemediation);
                    
                }else if(weight == activeWeight){
                    //add an active activity request
                    
                    ActiveRemediationConcept activeRemediation = new ActiveRemediationConcept(concept);
                    randomizeRemediations.add(activeRemediation);
                    
                }else if(weight == passiveWeight){
                    //add a passive activity request (use the passive request for this concept)
                    
                    randomizeRemediations.add(remediationConcept);
                    includedPassive = true;
                }
            }//end for
            
            if(!includedPassive && remediationConcept instanceof PassiveRemediationConcept){
                // make sure to include the passive remediation information as a fall back in case there
                // are no Interactive, constructive or active activities authored.  This contains the EMAP
                // metadata attributes to look for.
                randomizeRemediations.add(remediationConcept);
            }
            remediationInfo.setConceptRemediation(remediationConcept.getConcept(), randomizeRemediations);
        }
    }
    
// MH: the following was captured in June 2020 when first entering the handleCourseStateUpdate method when running
//     the Simple ICAP public course.  It severed as the basis for building the hasJust* named methods and could be
//     useful for determining other sequences of events.
//
//    after failed recall - incoming coursestate next quadrant = RAR
//    curr = recall, prev = example, last = example
//
//    showing AAR - incoming coursestate next quadrant = null
//    curr = rule, prev = recall, last = recall
//
//    after AAR - incoming coursestate next quadrant = RAR
//    curr = null, prev = rule, last = rule
//
//    showing remediation - incoming coursestate next quadrant = RAR
//    curr = RAR, prev = null, last = rule
//
//    finished remediation - course state next quad = recall
//    curr = RAR, prev = RAR, last = rule
//
//    after passed recall - course state next quad = RAR
//    curr = recall, prev = RAR, last = RAR
//
//    showing AAR - coursestate next quad = null
//    curr = RAR, prev = recall, last = recall
//
//    after AAR - next quad = P
//    curr = null, prev = RAR, last = RAR
//
//    starting practice - next quad = P
//    curr = P, prev = null, last = RAR
//
//    A.1 after good practice (when recall came before) - next quad = null
//    curr = P, prev = P, last = RAR
//
//    before AAR - next quad = null
//    curr = null, prev = P, last = P
//
//    course object after Adaptive courseflow - next quad = null
//    curr = null, prev = null, last = P
//    
//    A.2 after good practice (when recall was skipped), 1 of 2 states - next quad = null
//    curr = P, prev = P, last = null
//    2 of 2 states - next quad = null  [looks like 'A.1 before AAR' above]
//    curr = null, prev = P, last = P
//
//    B. after bad practice - next quad = null
//    curr = P, prev = P, last = RAR
//
//    showing AAR - next quad = null
//    curr = null, prev = P, last = P
//
//    starting remediation - next quad = RAR
//    curr = null, prev = null, last = P
//
//    ...
//    after good practice - next quad = null
//    curr = P, prev = P, last = RAR
//
//    -----
//    A+C
//    after passed recall - course state next quad = RAR
//    curr = recall, previous = example, last = example (same as failed recall)
//
//    showing AAR - incoming coursestate next quadrant = null
//    curr = RAR, previous = recall, last = recall
//
//    ------------------
//    Training app course object w/ remediation
//    exiting scenario - next quad = null
//    curr = P, prev = null, last = null
//    
//    showing AAR - next quad = null
//    curr = null, prev = P, last = P
//    
//    entering remediation - 
//
//    ------------------
//    RULES THAT WERE DETEREMINED FROM THE ABOVE:
//    FAILED-RECALL =
//    showing AAR - incoming coursestate next quadrant = null
//    curr = rule, prev = recall, last = recall
//
//    showing AAR - incoming coursestate next quadrant = null
//    curr = example, prev = recall, last = recall
//
//    PASSED-RECALL =
//    showing AAR - incoming coursestate next quadrant = null
//    curr = RAR, previous = recall, last = recall
//
//    FAILED-PRACTICE =
//    (first time)
//    starting remediation - next quad = RAR
//    curr = null, prev = null, last = P
//    (next times)
//    starting remediation - next quad = null
//    curr = P, prev = P, last = P
//
//    FAILED-PRACTICE (training app course object)
//    exiting scenario - next quad = null
//    curr = P, prev = null, last = null
//
//
//    PASSED-PRACTICE =
//    after good practice - next quad = null
//    curr = P, prev = P, last = RAR
//
//    -------------------
//    entering rule - next quad = rule
//    curr = null, prev = null, last = null
//
//    entering rule (when last thing was leaving successful practice) - next quad = rule
//    curr = null, prev = null, last = P
//
//    entering practice (after pre-test passed) - next quad = P
//    curr = null, prev = null, last = null
//

}
