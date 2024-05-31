/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;

import org.xml.sax.SAXException;

/**
 * This class provides logic to migrate GIFT v2.0 XML files to GIFT v3.0 XML files using the appropriate schemas and generated
 * classes.  
 * 
 * Note: the only changes in XML schemas between these two version is at the DKF, Course, learner action and lesson material levels.
 * 
 * @author mhoffman
 *
 */
public class ConversionWizardUtil_v2_3 extends AbstractConversionWizardUtil {

    /** course schema info */
    public static final File PREV_COURSE_SCHEMA_FILE = new File("data"+File.separator+"conversionWizard"+File.separator+"v2"+File.separator+"domain"+File.separator+"course"+File.separator+"course.xsd");
    public static final Class<?> COURSE_ROOT = generated.v2.course.Course.class;
    
    /** dkf schema info */
    public static final File PREV_DKF_SCHEMA_FILE = new File("data"+File.separator+"conversionWizard"+File.separator+"v2"+File.separator+"domain"+File.separator+"dkf"+File.separator+"dkf.xsd");
    public static final Class<?> DKF_ROOT = generated.v2.dkf.Scenario.class;
          
    /**
     * Auto-generate a GIFT v2.0 course object with every element/attribute instantiated.
     *  
     * @return generated.v2.course.Course - new 2.0 course object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v2.course.Course createCourse() throws Exception{
        Node rootNode = new Node();
        Object obj = createFullInstance(COURSE_ROOT, rootNode);
        return (generated.v2.course.Course)obj;
    }
    
    /**
     * Auto-generate a GIFT v2.0 dkf object with every element/attribute instantiated.
     *  
     * @return generated.v2.dkf.Scenario - new 2.0 dkf object, fully populated
     * @throws Exception - thrown if there is a severe error during the conversion process
     */
    public static generated.v2.dkf.Scenario createScenario() throws Exception{
        Node rootNode = new Node();
        Object obj = createFullInstance(DKF_ROOT, rootNode);
        return (generated.v2.dkf.Scenario)obj;
    }

    @Override
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
    	
        UnmarshalledFile uFile = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(), failOnFirstSchemaError);
        generated.v2.dkf.Scenario v2Scenario = (generated.v2.dkf.Scenario)uFile.getUnmarshalled();

        // Convert the version 2 scenario to the newest version and return it
        return convertScenario(v2Scenario, showCompletionDialog);       
    }
    
    /**
     * Convert the previous scenario schema object to a newer version of the scenario schema.
     * 
     * @param v2Scenario - the scenario schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new scenario
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertScenario(generated.v2.dkf.Scenario v2Scenario, boolean showCompletionDialog) throws IllegalArgumentException {
    	 
    	generated.v3.dkf.Scenario newScenario = new generated.v3.dkf.Scenario();
    	
    	//
        // copy over contents from old object to new object
        //
    	newScenario.setDescription(v2Scenario.getDescription());
        newScenario.setName(v2Scenario.getName());
         
        //
        //Learner Id
        //
        if(v2Scenario.getLearnerId() != null){
            generated.v3.dkf.LearnerId newLearnerId = new generated.v3.dkf.LearnerId();
            generated.v3.dkf.StartLocation newStartLocation = new generated.v3.dkf.StartLocation();
            newStartLocation.setCoordinate(convertCoordinate(v2Scenario.getLearnerId().getType().getCoordinate()));
            newLearnerId.setType(newStartLocation);
            newScenario.setLearnerId(newLearnerId);
        }
        
        //
        //Resources
        //
        generated.v3.dkf.Resources newResources = new generated.v3.dkf.Resources();
        newResources.setSurveyContext(v2Scenario.getResources().getSurveyContext());
        
        generated.v3.dkf.AvailableLearnerActions newALA = new generated.v3.dkf.AvailableLearnerActions();
        
        if(v2Scenario.getResources().getAvailableLearnerActions() != null){
            
            generated.v2.dkf.AvailableLearnerActions ala = v2Scenario.getResources().getAvailableLearnerActions();
            if(ala.getLearnerActionsFiles() != null){
                generated.v3.dkf.LearnerActionsFiles newLAF = new generated.v3.dkf.LearnerActionsFiles();
                for(String filename : ala.getLearnerActionsFiles().getFile()){
                    newLAF.getFile().add(filename);
                }
                
                newALA.setLearnerActionsFiles(newLAF);
            }
            
            if(ala.getLearnerActionsList() != null){
                
                generated.v3.dkf.LearnerActionsList newLAL = new generated.v3.dkf.LearnerActionsList();
                for(generated.v2.dkf.LearnerAction action : ala.getLearnerActionsList().getLearnerAction()){
                    
                    generated.v3.dkf.LearnerAction newAction = new generated.v3.dkf.LearnerAction();
                    newAction.setDisplayName(action.getDisplayName());
                    newAction.setType(generated.v3.dkf.LearnerActionEnumType.fromValue(action.getType().value()));
                    newLAL.getLearnerAction().add(newAction);
                }
                newALA.setLearnerActionsList(newLAL);
            }
        
            newResources.setAvailableLearnerActions(newALA);
        }        
        
        newScenario.setResources(newResources);
        
        //
        //Assessment
        //
        generated.v3.dkf.Assessment newAssessment = new generated.v3.dkf.Assessment();
        if(v2Scenario.getAssessment() != null){
            
            generated.v2.dkf.Assessment assessment = v2Scenario.getAssessment();
            
            //
            // Objects
            //
            generated.v3.dkf.Objects newObjects = new generated.v3.dkf.Objects();
            if(assessment.getObjects() != null){
                
                if(assessment.getObjects().getWaypoints() != null){
                    
                    generated.v3.dkf.Waypoints newWaypoints = new generated.v3.dkf.Waypoints();
                    
                    generated.v2.dkf.Waypoints waypoints = assessment.getObjects().getWaypoints();
                    for(generated.v2.dkf.Waypoint waypoint : waypoints.getWaypoint()){
                        
                        generated.v3.dkf.Waypoint newWaypoint = new generated.v3.dkf.Waypoint();
                        newWaypoint.setName(waypoint.getName());
                        newWaypoint.setCoordinate(convertCoordinate(waypoint.getCoordinate()));
                        
                        newWaypoints.getWaypoint().add(newWaypoint);
                    }
                    
                    newObjects.setWaypoints(newWaypoints);
                }
            }
            newAssessment.setObjects(newObjects);
            
            //
            // Tasks
            //            
            generated.v3.dkf.Tasks newTasks = new generated.v3.dkf.Tasks();
            if(assessment.getTasks() != null){
                
                for(generated.v2.dkf.Task task : assessment.getTasks().getTask()){
                    
                    generated.v3.dkf.Task newTask = new generated.v3.dkf.Task();
                    newTask.setName(task.getName());
                    newTask.setNodeId(task.getNodeId());
                    
                    // start triggers
                    if(task.getStartTriggers() != null){
                        generated.v3.dkf.StartTriggers newStartTriggers = new generated.v3.dkf.StartTriggers();
                        newStartTriggers.getTriggers().addAll(convertTriggers( task.getStartTriggers().getTriggers()));
                        newTask.setStartTriggers(newStartTriggers);
                    }
                    
                    // end triggers
                    if(task.getEndTriggers() != null){
                        generated.v3.dkf.EndTriggers newEndTriggers = new generated.v3.dkf.EndTriggers();
                        newEndTriggers.getTriggers().addAll(convertTriggers( task.getEndTriggers().getTriggers()));
                        newTask.setEndTriggers(newEndTriggers);
                    }
                    
                    // Concepts
                    if(task.getConcepts() != null){                        
                        newTask.setConcepts(convertConcepts(task.getConcepts()));                        
                    }
                    
                    // Assessments
                    if(task.getAssessments() != null){
                        newTask.setAssessments(convertAssessments(task.getAssessments()));
                    }                    
                    
                    newTasks.getTask().add(newTask);
                }
                
            }//end task if
            
            newAssessment.setTasks(newTasks);
            
        } //end assessment if
         
        newScenario.setAssessment(newAssessment);
        
        //
        //Actions
        //
        if(v2Scenario.getActions() != null){
            
            generated.v2.dkf.Actions actions = v2Scenario.getActions();
            generated.v3.dkf.Actions newActions = new generated.v3.dkf.Actions();
            
            //instructional strategies
            if(actions.getInstructionalStrategies() != null){
                
                generated.v2.dkf.InstructionalStrategies iStrategies = actions.getInstructionalStrategies();
                generated.v3.dkf.Actions.InstructionalStrategies newIStrategies = new generated.v3.dkf.Actions.InstructionalStrategies();
                
                for(generated.v2.dkf.Strategy strategy : iStrategies.getStrategy()){
                    
                    generated.v3.dkf.Strategy newStrategy = new generated.v3.dkf.Strategy();
                    newStrategy.setName(strategy.getName());
                    
                    Object strategyType = strategy.getValueAttribute();
                    if(strategyType instanceof generated.v2.dkf.PerformanceAssessment){
                        
                        generated.v2.dkf.PerformanceAssessment perfAss = (generated.v2.dkf.PerformanceAssessment)strategyType;
                        
                        generated.v3.dkf.PerformanceAssessment newPerfAss = new generated.v3.dkf.PerformanceAssessment();
                        newPerfAss.setNodeId(perfAss.getNodeId());
                        newPerfAss.setStrategyHandler(convertStrategyHandler(perfAss.getStrategyHandler()));
                        
                        newStrategy.setStrategyType(newPerfAss);
                        
                    }else if(strategyType instanceof generated.v2.dkf.InstructionalIntervention){
                        
                        generated.v2.dkf.InstructionalIntervention iIntervention = (generated.v2.dkf.InstructionalIntervention)strategyType;
                        
                        generated.v3.dkf.InstructionalIntervention newIIntervention = new generated.v3.dkf.InstructionalIntervention();
                        newIIntervention.setStrategyHandler(convertStrategyHandler(iIntervention.getStrategyHandler()));
                        
                        //only have a feedback choice in this version 
                        for(generated.v2.dkf.Feedback feedback : iIntervention.getInterventionTypes()){
                            
                            generated.v3.dkf.Feedback newFeedback = new generated.v3.dkf.Feedback();
                            
                            //only have a string message in this version
                            generated.v3.dkf.Feedback.Message message = new generated.v3.dkf.Feedback.Message();
                            message.setContent(feedback.getMessage());
                            
                            newFeedback.setFeedbackPresentation(message);
                            
                            newIIntervention.getInterventionTypes().add(newFeedback);
                        }
                        
                        newStrategy.setStrategyType(newIIntervention);
                        
                    }else if(strategyType instanceof generated.v2.dkf.ScenarioAdaptation){
                        
                        generated.v2.dkf.ScenarioAdaptation adaptation = (generated.v2.dkf.ScenarioAdaptation)strategyType;
                        
                        generated.v3.dkf.ScenarioAdaptation newAdaptation = new generated.v3.dkf.ScenarioAdaptation();
                        newAdaptation.setStrategyHandler(convertStrategyHandler(adaptation.getStrategyHandler()));
                        
                        //only have environment adaptation in this version
                        for(generated.v2.dkf.EnvironmentAdaptation eAdapt : adaptation.getAdaptationTypes()){
                            
                            generated.v3.dkf.EnvironmentAdaptation newEAdapt = new generated.v3.dkf.EnvironmentAdaptation();
                            
                            generated.v3.dkf.EnvironmentAdaptation.Pair newPair = new generated.v3.dkf.EnvironmentAdaptation.Pair();
                            newPair.setType(eAdapt.getPair().getType());
                            newPair.setValue(eAdapt.getPair().getValue());
                            newEAdapt.setPair(newPair);
                            
                            newAdaptation.getAdaptationTypes().add(newEAdapt);
                        }
                        
                        newStrategy.setStrategyType(newAdaptation);
                        
                    }else{
                        throw new IllegalArgumentException("Found unhandled strategy type of "+strategyType);
                    }
                    
                    
                    newIStrategies.getStrategy().add(newStrategy);
                }
                
                newActions.setInstructionalStrategies(newIStrategies);
            }
            
            //State transitions
            if(actions.getStateTransitions() != null){
               
                generated.v2.dkf.StateTransitions sTransitions = actions.getStateTransitions();
                generated.v3.dkf.Actions.StateTransitions newSTransitions = new generated.v3.dkf.Actions.StateTransitions();
                
                for(generated.v2.dkf.StateTransition sTransition : sTransitions.getStateTransition()){
                    
                    generated.v3.dkf.Actions.StateTransitions.StateTransition newSTransition = new generated.v3.dkf.Actions.StateTransitions.StateTransition();
                    
                    generated.v3.dkf.Actions.StateTransitions.StateTransition.LogicalExpression newLogicalExpression = new generated.v3.dkf.Actions.StateTransitions.StateTransition.LogicalExpression();
                    
                    //State type
                    Object stateType = sTransition.getStateType();
                    if(stateType instanceof generated.v2.dkf.Enum){
                        
                        generated.v2.dkf.Enum stateEnum = (generated.v2.dkf.Enum)stateType;
                        
                        generated.v3.dkf.LearnerStateTransitionEnum learnerStateTrans = new generated.v3.dkf.LearnerStateTransitionEnum();
                        learnerStateTrans.setAttribute(stateEnum.getAttribute());
                        learnerStateTrans.setCurrent(stateEnum.getCurrent());
                        learnerStateTrans.setPrevious(stateEnum.getPrevious());
                        
                        newLogicalExpression.getStateType().add(learnerStateTrans);
                        
                    }else if(stateType instanceof generated.v2.dkf.PerformanceNode){
                        
                        generated.v2.dkf.PerformanceNode perfNode = (generated.v2.dkf.PerformanceNode)stateType;
                        
                        generated.v3.dkf.PerformanceNode newPerfNode = new generated.v3.dkf.PerformanceNode();
                        newPerfNode.setName(perfNode.getName());
                        newPerfNode.setNodeId(perfNode.getNodeId());
                        newPerfNode.setCurrent(generated.v3.dkf.AssessmentLevelEnumType.fromValue(perfNode.getCurrent().value()));
                        newPerfNode.setPrevious(generated.v3.dkf.AssessmentLevelEnumType.fromValue(perfNode.getPrevious().value()));
                        
                        newLogicalExpression.getStateType().add(newPerfNode);
                        
                    }else{
                        throw new IllegalArgumentException("Found unhandled action's state transition state type of "+stateType);
                    }
                    
                    newSTransition.setLogicalExpression(newLogicalExpression);
                    
                    //Strategy Choices
                    generated.v3.dkf.Actions.StateTransitions.StateTransition.StrategyChoices newStrategyChoices = new generated.v3.dkf.Actions.StateTransitions.StateTransition.StrategyChoices();
                    for(generated.v2.dkf.StrategyRef strategyRef : sTransition.getStrategyChoices().getStrategyRef()){
                        
                        generated.v3.dkf.StrategyRef newStrategyRef = new generated.v3.dkf.StrategyRef();
                        newStrategyRef.setName(strategyRef.getName());
                        
                        newStrategyChoices.getStrategyRef().add(newStrategyRef);
                    }
                    newSTransition.setStrategyChoices(newStrategyChoices);
                    
                    newSTransitions.getStateTransition().add(newSTransition);
                }
                
                newActions.setStateTransitions(newSTransitions);
            }
            
            newScenario.setActions(newActions);
        }
              
        // Continue the conversion with the next Util
        ConversionWizardUtil_v3_4 util = new ConversionWizardUtil_v3_4();
        util.setConversionIssueList(conversionIssueList);
        return util.convertScenario(newScenario, showCompletionDialog);
    }   
    
    /**
     * Convert a strategy handler object to a new version of the strategy handler object.
     * 
     * @param handler - the object to convert
     * @return generated.v3.dkf.StrategyHandler - the new object
     */
    private static generated.v3.dkf.StrategyHandler convertStrategyHandler(generated.v2.dkf.StrategyHandler handler){
        
        generated.v3.dkf.StrategyHandler newHandler = new generated.v3.dkf.StrategyHandler();
        newHandler.setImpl(handler.getImpl());
        
        return newHandler;
    }
    
    /**
     * Convert a concepts object to a new version of the concepts object.
     * 
     * @param concepts - the object to convert
     * @return generated.v3.dkf.Concepts - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v3.dkf.Concepts convertConcepts(generated.v2.dkf.Concepts concepts) throws IllegalArgumentException{
        
        generated.v3.dkf.Concepts newConcepts = new generated.v3.dkf.Concepts();
        for(generated.v2.dkf.Concept concept : concepts.getConcept()){
            
            generated.v3.dkf.Concept newConcept = new generated.v3.dkf.Concept();
            newConcept.setName(concept.getName());
            newConcept.setNodeId(concept.getNodeId());
            
            newConcept.setAssessments(convertAssessments(concept.getAssessments()));
            
            Object metricsOrConcepts = concept.getMetricsOrConcepts();
            if(metricsOrConcepts instanceof generated.v2.dkf.Concepts){
                //nested concepts
                newConcept.setConditionsOrConcepts(convertConcepts((generated.v2.dkf.Concepts)metricsOrConcepts));
                
            }else if(metricsOrConcepts instanceof generated.v2.dkf.Metrics){
                //no longer support metrics nodes - bypass metrics and get to conditions
                
                generated.v3.dkf.Conditions newConditions = new generated.v3.dkf.Conditions();
                
                generated.v2.dkf.Metrics metrics = (generated.v2.dkf.Metrics)metricsOrConcepts;
                
                for(generated.v2.dkf.Metric metric : metrics.getMetric()){
                    
                    for(generated.v2.dkf.Condition condition : metric.getConditions().getCondition()){
                        
                        generated.v3.dkf.Condition newCondition = new generated.v3.dkf.Condition();
                        newCondition.setConditionImpl(condition.getConditionImpl());                        
                        
                        if(condition.getDefault() != null){
                            generated.v3.dkf.Default newDefault = new generated.v3.dkf.Default();
                            newDefault.setAssessment(generated.v3.dkf.AssessmentLevelEnumType.fromValue(condition.getDefault().getAssessment().value()));
                            newCondition.setDefault(newDefault);
                        }                            
                        
                        //Input
                        generated.v3.dkf.Input newInput = new generated.v3.dkf.Input();
                        if(condition.getInput() != null){
                            
                            Object inputType = condition.getInput().getType();
                            if(inputType instanceof generated.v2.dkf.ApplicationCompletedCondition){
                                
                                @SuppressWarnings("unused")
                                generated.v2.dkf.ApplicationCompletedCondition conditionInput = (generated.v2.dkf.ApplicationCompletedCondition)inputType;
                                
                                generated.v3.dkf.ApplicationCompletedCondition newConditionInput = new generated.v3.dkf.ApplicationCompletedCondition();
                                                                
                                newInput.setType(newConditionInput);
                                
                            }else if(inputType instanceof generated.v2.dkf.AvoidLocationCondition){
                                
                                generated.v2.dkf.AvoidLocationCondition conditionInput = (generated.v2.dkf.AvoidLocationCondition)inputType;
                                
                                generated.v3.dkf.AvoidLocationCondition newConditionInput = new generated.v3.dkf.AvoidLocationCondition();
                                
                                if(conditionInput.getWaypointRef() != null){                                    
                                    newConditionInput.setWaypointRef(convertWaypointRef(conditionInput.getWaypointRef()));
                                }
                                                                
                                newInput.setType(newConditionInput);
                                
                            }else if(inputType instanceof generated.v2.dkf.CheckpointPaceCondition){

                                generated.v2.dkf.CheckpointPaceCondition conditionInput = (generated.v2.dkf.CheckpointPaceCondition)inputType;
                                
                                generated.v3.dkf.CheckpointPaceCondition newConditionInput = new generated.v3.dkf.CheckpointPaceCondition();
                                for(generated.v2.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                    
                                    newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                                }
                                                                
                                newInput.setType(newConditionInput);
                                                                
                            }else if(inputType instanceof generated.v2.dkf.CheckpointProgressCondition){

                                generated.v2.dkf.CheckpointProgressCondition conditionInput = (generated.v2.dkf.CheckpointProgressCondition)inputType;
                                
                                generated.v3.dkf.CheckpointProgressCondition newConditionInput = new generated.v3.dkf.CheckpointProgressCondition();
                                for(generated.v2.dkf.Checkpoint checkpoint : conditionInput.getCheckpoint()){
                                    
                                    newConditionInput.getCheckpoint().add(convertCheckpoint(checkpoint));
                                }
                                                                
                                newInput.setType(newConditionInput);                                
                                
                            }else if(inputType instanceof generated.v2.dkf.CorridorBoundaryCondition){
                                
                                generated.v2.dkf.CorridorBoundaryCondition conditionInput = (generated.v2.dkf.CorridorBoundaryCondition)inputType;
                                
                                generated.v3.dkf.CorridorBoundaryCondition newConditionInput = new generated.v3.dkf.CorridorBoundaryCondition();
                                newConditionInput.setBufferWidthPercent(conditionInput.getBufferWidthPercent());
                                newConditionInput.setPath(convertPath(conditionInput.getPath()));                                
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.CorridorPostureCondition){

                                generated.v2.dkf.CorridorPostureCondition conditionInput = (generated.v2.dkf.CorridorPostureCondition)inputType;
                                
                                generated.v3.dkf.CorridorPostureCondition newConditionInput = new generated.v3.dkf.CorridorPostureCondition();
                                newConditionInput.setPath(convertPath(conditionInput.getPath()));                                

                                generated.v3.dkf.Postures postures = new generated.v3.dkf.Postures();
                                for(generated.v2.dkf.PostureEnumType posture : conditionInput.getPostures().getPosture()){
                                    postures.getPosture().add(generated.v3.dkf.PostureEnumType.fromValue(posture.value()));
                                }
                                newConditionInput.setPostures(postures);
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.EliminateHostilesCondition){

                                generated.v2.dkf.EliminateHostilesCondition conditionInput = (generated.v2.dkf.EliminateHostilesCondition)inputType;
                                
                                generated.v3.dkf.EliminateHostilesCondition newConditionInput = new generated.v3.dkf.EliminateHostilesCondition();
                                 
                                if(conditionInput.getEntities() != null){                                    
                                    newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                                }
                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.EnterAreaCondition){

                                generated.v2.dkf.EnterAreaCondition conditionInput = (generated.v2.dkf.EnterAreaCondition)inputType;
                                
                                generated.v3.dkf.EnterAreaCondition newConditionInput = new generated.v3.dkf.EnterAreaCondition();
                                
                                for(generated.v2.dkf.Entrance entrance : conditionInput.getEntrance()){
                                    
                                    generated.v3.dkf.Entrance newEntrance = new generated.v3.dkf.Entrance();
                                    
                                    newEntrance.setAssessment(generated.v3.dkf.AssessmentLevelEnumType.fromValue(entrance.getAssessment().value()));
                                    newEntrance.setName(entrance.getName());
                                    
                                    generated.v3.dkf.Inside newInside = new generated.v3.dkf.Inside();
                                    newInside.setProximity(entrance.getInside().getProximity());
                                    newInside.setWaypoint(entrance.getInside().getWaypoint());
                                    newEntrance.setInside(newInside);
                                    
                                    generated.v3.dkf.Outside newOutside = new generated.v3.dkf.Outside();
                                    newOutside.setProximity(entrance.getOutside().getProximity());
                                    newOutside.setWaypoint(entrance.getOutside().getWaypoint());
                                    newEntrance.setOutside(newOutside);
                                    
                                    newConditionInput.getEntrance().add(newEntrance);
                                }
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.ExplosiveHazardSpotReportCondition){

                                @SuppressWarnings("unused")
                                generated.v2.dkf.ExplosiveHazardSpotReportCondition conditionInput = (generated.v2.dkf.ExplosiveHazardSpotReportCondition)inputType;
                                
                                generated.v3.dkf.ExplosiveHazardSpotReportCondition newConditionInput = new generated.v3.dkf.ExplosiveHazardSpotReportCondition();
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.IdentifyPOIsCondition){

                                generated.v2.dkf.IdentifyPOIsCondition conditionInput = (generated.v2.dkf.IdentifyPOIsCondition)inputType;
                                
                                generated.v3.dkf.IdentifyPOIsCondition newConditionInput = new generated.v3.dkf.IdentifyPOIsCondition();
                                
                                if(conditionInput.getPois() != null){
                                    
                                    generated.v3.dkf.Pois pois = new generated.v3.dkf.Pois();
                                    for(generated.v2.dkf.WaypointRef waypointRef : conditionInput.getPois().getWaypointRef()){
                                        pois.getWaypointRef().add(convertWaypointRef(waypointRef));
                                    }
                                    
                                    newConditionInput.setPois(pois);
                                }
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.LifeformTargetAccuracyCondition){

                                generated.v2.dkf.LifeformTargetAccuracyCondition conditionInput = (generated.v2.dkf.LifeformTargetAccuracyCondition)inputType;
                                
                                generated.v3.dkf.LifeformTargetAccuracyCondition newConditionInput = new generated.v3.dkf.LifeformTargetAccuracyCondition();
                                
                                if(conditionInput.getEntities() != null){                                    
                                    newConditionInput.setEntities(convertEntities(conditionInput.getEntities()));
                                }
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.NineLineReportCondition){

                                @SuppressWarnings("unused")
                                generated.v2.dkf.NineLineReportCondition conditionInput = (generated.v2.dkf.NineLineReportCondition)inputType;
                                
                                generated.v3.dkf.NineLineReportCondition newConditionInput = new generated.v3.dkf.NineLineReportCondition();
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.PowerPointDwellCondition){

                                generated.v2.dkf.PowerPointDwellCondition conditionInput = (generated.v2.dkf.PowerPointDwellCondition)inputType;
                                
                                generated.v3.dkf.PowerPointDwellCondition newConditionInput = new generated.v3.dkf.PowerPointDwellCondition();
                                
                                generated.v3.dkf.PowerPointDwellCondition.Default newPPTDefault = new generated.v3.dkf.PowerPointDwellCondition.Default();
                                newPPTDefault.setTimeInSeconds(conditionInput.getDefault().getTimeInSeconds());
                                newConditionInput.setDefault(newPPTDefault);
                                
                                generated.v3.dkf.PowerPointDwellCondition.Slides slides = new generated.v3.dkf.PowerPointDwellCondition.Slides();
                                for(generated.v2.dkf.PowerPointDwellCondition.Slides.Slide slide : conditionInput.getSlides().getSlide()){
                                    
                                    generated.v3.dkf.PowerPointDwellCondition.Slides.Slide newSlide = new generated.v3.dkf.PowerPointDwellCondition.Slides.Slide();
                                    newSlide.setIndex(slide.getIndex());
                                    newSlide.setTimeInSeconds(slide.getTimeInSeconds());
                                    
                                    slides.getSlide().add(newSlide);
                                }
                                newConditionInput.setSlides(slides);
                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.RulesOfEngagementCondition){

                                generated.v2.dkf.RulesOfEngagementCondition conditionInput = (generated.v2.dkf.RulesOfEngagementCondition)inputType;
                                
                                generated.v3.dkf.RulesOfEngagementCondition newConditionInput = new generated.v3.dkf.RulesOfEngagementCondition();
                                generated.v3.dkf.Wcs newWCS = new generated.v3.dkf.Wcs();
                                newWCS.setValue(conditionInput.getWcs().getValue());
                                newConditionInput.setWcs(newWCS);
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.SpotReportCondition){

                                @SuppressWarnings("unused")
                                generated.v2.dkf.SpotReportCondition conditionInput = (generated.v2.dkf.SpotReportCondition)inputType;
                                
                                generated.v3.dkf.SpotReportCondition newConditionInput = new generated.v3.dkf.SpotReportCondition();
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else if(inputType instanceof generated.v2.dkf.UseRadioCondition){
                                
                                @SuppressWarnings("unused")
                                generated.v2.dkf.UseRadioCondition conditionInput = (generated.v2.dkf.UseRadioCondition)inputType;
                                
                                generated.v3.dkf.UseRadioCondition newConditionInput = new generated.v3.dkf.UseRadioCondition();
                                                                
                                newInput.setType(newConditionInput);  
                                
                            }else{
                                throw new IllegalArgumentException("Found unhandled condition input type of "+inputType);
                            }

                        }
                        newCondition.setInput(newInput);
                        
                        //Scoring
                        generated.v3.dkf.Scoring newScoring = new generated.v3.dkf.Scoring();
                        if(condition.getScoring() != null){
                            
                            for(Object scoringType : condition.getScoring().getType()){
                                
                                if(scoringType instanceof generated.v2.dkf.Count){
                                    
                                    generated.v2.dkf.Count count = (generated.v2.dkf.Count)scoringType;
                                    
                                    generated.v3.dkf.Count newCount = new generated.v3.dkf.Count();                                    
                                    newCount.setName(count.getName());
                                    newCount.setUnits(generated.v3.dkf.UnitsEnumType.fromValue(count.getUnits().value()));
                                    
                                    if(count.getEvaluators() != null){                                        
                                        newCount.setEvaluators(convertEvaluators(count.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newCount);
                                    
                                }else if(scoringType instanceof generated.v2.dkf.CompletionTime){
                                    
                                    generated.v2.dkf.CompletionTime complTime = (generated.v2.dkf.CompletionTime)scoringType;
                                    
                                    generated.v3.dkf.CompletionTime newComplTime = new generated.v3.dkf.CompletionTime();
                                    newComplTime.setName(complTime.getName());
                                    newComplTime.setUnits(generated.v3.dkf.UnitsEnumType.fromValue(complTime.getUnits().value()));

                                    if(complTime.getEvaluators() != null){                                        
                                        newComplTime.setEvaluators(convertEvaluators(complTime.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newComplTime);
                                    
                                }else if(scoringType instanceof generated.v2.dkf.ViolationTime){
                                    
                                    generated.v2.dkf.ViolationTime violationTime = (generated.v2.dkf.ViolationTime)scoringType;
                                    
                                    generated.v3.dkf.ViolationTime newViolationTime = new generated.v3.dkf.ViolationTime();
                                    newViolationTime.setName(violationTime.getName());
                                    newViolationTime.setUnits(violationTime.getUnits());
                                    
                                    if(violationTime.getEvaluators() != null){                                        
                                        newViolationTime.setEvaluators(convertEvaluators(violationTime.getEvaluators()));
                                    }
                                    
                                    newScoring.getType().add(newViolationTime);
                                    
                                }else{
                                    throw new IllegalArgumentException("Found unhandled scoring type of "+scoringType);
                                }
                            }
                        }
                        newCondition.setScoring(newScoring);
                        
                        newConditions.getCondition().add(newCondition);
                    }
                }
                
                newConcept.setConditionsOrConcepts(newConditions);
                
            }else{
                throw new IllegalArgumentException("Found unhandled subconcept node type of "+metricsOrConcepts);
            }
            
            newConcepts.getConcept().add(newConcept);
            
        }
        
        return newConcepts;
    }
    
    /**
     * Convert a waypointref object to a new waypointref object.
     * 
     * @param waypointRef - the object to convert
     * @return generated.v3.dkf.WaypointRef - the new object
     */
    private static generated.v3.dkf.WaypointRef convertWaypointRef(generated.v2.dkf.WaypointRef waypointRef){
        
        generated.v3.dkf.WaypointRef newWaypoint = new generated.v3.dkf.WaypointRef();
        newWaypoint.setValue(waypointRef.getValue());
        newWaypoint.setDistance(waypointRef.getDistance());
        
        return newWaypoint;
    }
    
    /**
     * Convert an entities object to a new entities object.
     * 
     * @param entities - the object to convert
     * @return generated.v3.dkf.Entities - the new object
     * @throws IllegalArgumentException
     */
    private static generated.v3.dkf.Entities convertEntities(generated.v2.dkf.Entities entities) throws IllegalArgumentException{
        
        generated.v3.dkf.Entities newEntities = new generated.v3.dkf.Entities();
        for(generated.v2.dkf.StartLocation location : entities.getStartLocation()){
            
            generated.v3.dkf.StartLocation newLocation = new generated.v3.dkf.StartLocation();
            newLocation.setCoordinate(convertCoordinate(location.getCoordinate()));
            newEntities.getStartLocation().add(newLocation);
        }
        
        return newEntities;
    }
    
    /**
     * Convert a path object into a new path object.
     * 
     * @param path - the object to convert
     * @return generated.v3.dkf.Path - the new object
     */
    private static generated.v3.dkf.Path convertPath(generated.v2.dkf.Path path){
        
        generated.v3.dkf.Path newPath = new generated.v3.dkf.Path();
        for(generated.v2.dkf.Segment segment : path.getSegment()){
            
            generated.v3.dkf.Segment newSegment = new generated.v3.dkf.Segment();
            newSegment.setBufferWidthPercent(segment.getBufferWidthPercent());
            newSegment.setName(segment.getName());
            newSegment.setWidth(segment.getWidth());
            
            generated.v3.dkf.Start start = new generated.v3.dkf.Start();
            start.setWaypoint(segment.getStart().getWaypoint());
            newSegment.setStart(start);
            
            generated.v3.dkf.End end = new generated.v3.dkf.End();
            end.setWaypoint(segment.getEnd().getWaypoint());
            newSegment.setEnd(end);
            
            newPath.getSegment().add(newSegment);
        }
        
        return newPath;
    }
    
    /**
     * Convert a checkpoint object into a new checkpoint object.
     * 
     * @param checkpoint - the object to convert
     * @return generated.v3.dkf.Checkpoint - the new object
     */
    private static generated.v3.dkf.Checkpoint convertCheckpoint(generated.v2.dkf.Checkpoint checkpoint){
        
        generated.v3.dkf.Checkpoint newCheckpoint = new generated.v3.dkf.Checkpoint();
        newCheckpoint.setAtTime(checkpoint.getAtTime());
        newCheckpoint.setWaypoint(checkpoint.getWaypoint());
        newCheckpoint.setWindowOfTime(checkpoint.getWindowOfTime());
        
        return newCheckpoint;
    }
    
    /**
     * Convert an evaluators object into a new evaluators object.
     * 
     * @param evaluators - the object to convert
     * @return generated.v3.dkf.Evaluators - the new object
     */
    private static generated.v3.dkf.Evaluators convertEvaluators(generated.v2.dkf.Evaluators evaluators){
        
        generated.v3.dkf.Evaluators newEvaluators = new generated.v3.dkf.Evaluators();
        for(generated.v2.dkf.Evaluator evaluator : evaluators.getEvaluator()){
            
            generated.v3.dkf.Evaluator newEvaluator = new generated.v3.dkf.Evaluator();
            newEvaluator.setAssessment(generated.v3.dkf.AssessmentLevelEnumType.fromValue(evaluator.getAssessment().value()));
            newEvaluator.setValue(evaluator.getValue());                                            
            newEvaluator.setOperator(generated.v3.dkf.OperatorEnumType.fromValue(evaluator.getOperator().value()));
            
            newEvaluators.getEvaluator().add(newEvaluator);
        }
        
        return newEvaluators;
    }
    
    /**
     * Convert an assessment object into a new assessment object.
     * 
     * @param assessments - the assessment object to convert
     * @return generated.v3.dkf.Assessments - the new assessment object
     */
    private static generated.v3.dkf.Assessments convertAssessments(generated.v2.dkf.Assessments assessments){
        
        generated.v3.dkf.Assessments newAssessments = new generated.v3.dkf.Assessments();
        for(generated.v2.dkf.Survey survey : assessments.getSurvey()){
            
            generated.v3.dkf.Assessments.Survey newSurvey = new generated.v3.dkf.Assessments.Survey();
            newSurvey.setGIFTSurveyKey(survey.getGIFTSurveyKey());
            
            generated.v3.dkf.Questions newQuestions = new generated.v3.dkf.Questions();
            for(generated.v2.dkf.Question question : survey.getQuestions().getQuestion()){
                
                generated.v3.dkf.Question newQuestion = new generated.v3.dkf.Question();
                newQuestion.setKey(question.getKey());
                
                for(generated.v2.dkf.Reply reply : question.getReply()){
                    
                    generated.v3.dkf.Reply newReply = new generated.v3.dkf.Reply();
                    newReply.setKey(reply.getKey());
                    newReply.setResult(generated.v3.dkf.AssessmentLevelEnumType.fromValue(reply.getResult().value()));
                    
                    newQuestion.getReply().add(newReply);
                }
                
                newQuestions.getQuestion().add(newQuestion);
            }
            
            newSurvey.setQuestions(newQuestions);
            
            newAssessments.getAssessmentTypes().add(newSurvey);
        }
        
        
        return newAssessments;
    }
    
    /**
     * Convert a collection of trigger objects (start or end triggers) into the new schema version.
     * 
     * @param triggerObjects - collection of trigger objects to convert
     * @return List<Object> - converted trigger objects (same size as triggerObjects collection)
     * @throws IllegalArgumentException
     */
    private static List<Object> convertTriggers(List<Object> triggerObjects) throws IllegalArgumentException{
        
        List<Object> newTriggerObjects = new ArrayList<>();
        for(Object triggerObj : triggerObjects){
            
            if(triggerObj instanceof generated.v2.dkf.EntityLocation){
                
                generated.v2.dkf.EntityLocation entityLocation = (generated.v2.dkf.EntityLocation)triggerObj;
                generated.v3.dkf.EntityLocation newEntityLocation = new generated.v3.dkf.EntityLocation();
                
                generated.v3.dkf.StartLocation startLocation = new generated.v3.dkf.StartLocation();
                startLocation.setCoordinate(convertCoordinate(entityLocation.getStartLocation().getCoordinate()));
                newEntityLocation.setStartLocation(startLocation);
                
                generated.v3.dkf.TriggerLocation triggerLocation = new generated.v3.dkf.TriggerLocation();
                triggerLocation.setCoordinate(convertCoordinate(entityLocation.getTriggerLocation().getCoordinate()));
                newEntityLocation.setTriggerLocation(triggerLocation);
                
                newTriggerObjects.add(newEntityLocation);
                
            }else if(triggerObj instanceof generated.v2.dkf.LearnerLocation){
                
                generated.v2.dkf.LearnerLocation learnerLocation = (generated.v2.dkf.LearnerLocation)triggerObj;
                generated.v3.dkf.LearnerLocation newLearnerLocation = new generated.v3.dkf.LearnerLocation();
                
                newLearnerLocation.setCoordinate(convertCoordinate(learnerLocation.getCoordinate()));
                
                newTriggerObjects.add(newLearnerLocation);
                
            }else if(triggerObj instanceof generated.v2.dkf.ConceptEnded){
                
                generated.v2.dkf.ConceptEnded conceptEnded = (generated.v2.dkf.ConceptEnded)triggerObj;
                generated.v3.dkf.ConceptEnded newConceptEnded = new generated.v3.dkf.ConceptEnded();
                
                newConceptEnded.setNodeId(conceptEnded.getNodeId());
                
                newTriggerObjects.add(newConceptEnded);
                
            }else{
                throw new IllegalArgumentException("Found unhandled trigger type of "+triggerObj);
            }
        }
        
        return newTriggerObjects;
    }
       
    /**
     * Convert a coordinate object into the latest schema version.
     * 
     * @param coordinate - v2.0 coordinate object to convert
     * @return generated.v3.dkf.Coordinate - the new coordinate object
     * @throws IllegalArgumentException
     */
    private static generated.v3.dkf.Coordinate convertCoordinate(generated.v2.dkf.Coordinate coordinate) throws IllegalArgumentException{
        
        generated.v3.dkf.Coordinate newCoord = new generated.v3.dkf.Coordinate();
        
        Object coordType = coordinate.getType();
        if(coordType instanceof generated.v2.dkf.GCC){
            
            generated.v2.dkf.GCC gcc = (generated.v2.dkf.GCC)coordType;
            generated.v3.dkf.GCC newGCC = new generated.v3.dkf.GCC();
            
            newGCC.setX(gcc.getX());
            newGCC.setY(gcc.getY());
            newGCC.setZ(gcc.getZ());
            
            newCoord.setType(newGCC);
            
        }else if(coordType instanceof generated.v2.dkf.GDC){
            
            generated.v2.dkf.GDC gdc = (generated.v2.dkf.GDC)coordType;
            generated.v3.dkf.GDC newGDC = new generated.v3.dkf.GDC();
            
            newGDC.setLatitude(gdc.getLatitude());
            newGDC.setLongitude(gdc.getLongitude());
            newGDC.setElevation(gdc.getElevation());
            
            newCoord.setType(newGDC);
            
        }else if(coordType instanceof generated.v2.dkf.VBS2AGL){
            
            generated.v2.dkf.VBS2AGL agl = (generated.v2.dkf.VBS2AGL)coordType;
            generated.v3.dkf.VBS2AGL newAGL = new generated.v3.dkf.VBS2AGL();
            
            newAGL.setX(agl.getX());
            newAGL.setY(agl.getY());
            newAGL.setZ(agl.getZ());
            
            newCoord.setType(newAGL);
            
        }else{
            throw new IllegalArgumentException("Found unhandled coordinate type of "+coordType);
        }
        
        return newCoord;
    }
    
    @Override
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
    	UnmarshalledFile uFile = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
    	generated.v2.course.Course v2Course = (generated.v2.course.Course)uFile.getUnmarshalled();
    	
    	// Convert the version 2 course to the newest version and return it
    	return convertCourse(v2Course, showCompletionDialog);
    }
    
    
    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param v2Course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @return Object - the new course
     * @throws IllegalArgumentException - thrown if there is a severe error during the conversion process
     */
    public UnmarshalledFile convertCourse(generated.v2.course.Course v2Course, boolean showCompletionDialog) throws IllegalArgumentException{
        
        generated.v3.course.Course newCourse = new generated.v3.course.Course();
        
        //
        // copy over contents from old object to new object
        //
        newCourse.setDescription(v2Course.getDescription());
        newCourse.setName(v2Course.getName());
        newCourse.setSurveyContext(v2Course.getSurveyContext());
        
        generated.v3.course.Transitions newTransitions = new generated.v3.course.Transitions();
        newCourse.setTransitions(newTransitions);
        
        //TRANSITIONS
        for(Object transitionObj : v2Course.getTransitions().getTransitionType()){
            
            if(transitionObj instanceof generated.v2.course.Guidance){
                
                generated.v2.course.Guidance guidance = (generated.v2.course.Guidance)transitionObj;
                generated.v3.course.Guidance newGuidance = convertGuidance(guidance);
                
                newCourse.getTransitions().getTransitionType().add(newGuidance);
                
            }else if(transitionObj instanceof generated.v2.course.PresentSurvey){
                
                generated.v2.course.PresentSurvey presentSurvey = (generated.v2.course.PresentSurvey)transitionObj;
                generated.v3.course.PresentSurvey newPresentSurvey = new generated.v3.course.PresentSurvey();
                
                newPresentSurvey.setSurveyChoice(presentSurvey.getGIFTSurveyKey());
                
                newCourse.getTransitions().getTransitionType().add(newPresentSurvey);
                
            }else if(transitionObj instanceof generated.v2.course.AAR){
                
                newCourse.getTransitions().getTransitionType().add(new generated.v3.course.AAR());
                
            }else if(transitionObj instanceof generated.v2.course.TrainingApplication){
                
                generated.v2.course.TrainingApplication trainApp = (generated.v2.course.TrainingApplication)transitionObj;
                generated.v3.course.TrainingApplication newTrainApp = new generated.v3.course.TrainingApplication();
                
                generated.v3.course.DkfRef dkfRef = new generated.v3.course.DkfRef();
                dkfRef.setFile(trainApp.getDkfRef().getFile());
                newTrainApp.setDkfRef(dkfRef);
                
                newTrainApp.setFinishedWhen(generated.v3.course.TrainingApplicationStateEnumType.fromValue(trainApp.getFinishedWhen().value()));
                
                if(trainApp.getGuidance() != null){
                    
                    generated.v3.course.Guidance newGuidance = convertGuidance(trainApp.getGuidance());
                    newTrainApp.setGuidance(newGuidance);
                }
                
                
                generated.v3.course.Interops newInterops = new generated.v3.course.Interops();
                newTrainApp.setInterops(newInterops);
                
                for(generated.v2.course.Interop interop : trainApp.getInterops().getInterop()){
                    
                    generated.v3.course.Interop newInterop = new generated.v3.course.Interop();
                    newInterop.setInteropImpl(interop.getInteropImpl());
                    
                    newInterop.setInteropInputs(new generated.v3.course.InteropInputs());
                    
                    Object interopObj = interop.getInteropInputs().getInteropInput();
                    if(interopObj instanceof generated.v2.course.VBS2InteropInputs){
                        
                        generated.v2.course.VBS2InteropInputs vbs2 = (generated.v2.course.VBS2InteropInputs)interopObj;
                        generated.v3.course.VBS2InteropInputs newVbs2 = new generated.v3.course.VBS2InteropInputs();
                        
                        generated.v3.course.VBS2InteropInputs.LoadArgs loadArgs = new generated.v3.course.VBS2InteropInputs.LoadArgs();
                        loadArgs.setScenarioName(vbs2.getLoadArgs().getScenarioName());
                        newVbs2.setLoadArgs(loadArgs);
                        
                        newInterop.getInteropInputs().setInteropInput(newVbs2);
                        
                    }else if(interopObj instanceof generated.v2.course.DISInteropInputs){
                        
                        generated.v3.course.DISInteropInputs newDIS = new generated.v3.course.DISInteropInputs();
                        newDIS.setLoadArgs(new generated.v3.course.DISInteropInputs.LoadArgs());
                        newInterop.getInteropInputs().setInteropInput(newDIS);
                        
                    }else if(interopObj instanceof generated.v2.course.PowerPointInteropInputs){
                        
                        generated.v2.course.PowerPointInteropInputs ppt = (generated.v2.course.PowerPointInteropInputs)interopObj;
                        generated.v3.course.PowerPointInteropInputs newPPT = new generated.v3.course.PowerPointInteropInputs();
                        
                        newPPT.setLoadArgs(new generated.v3.course.PowerPointInteropInputs.LoadArgs());
                        
                        newPPT.getLoadArgs().setShowFile(ppt.getLoadArgs().getShowFile());
                        newInterop.getInteropInputs().setInteropInput(newPPT);
                        
                    }else if(interopObj instanceof generated.v2.course.CustomInteropInputs){
                        
                        generated.v2.course.CustomInteropInputs custom = (generated.v2.course.CustomInteropInputs)interopObj;
                        generated.v3.course.CustomInteropInputs newCustom = new generated.v3.course.CustomInteropInputs();
                        
                        newCustom.setLoadArgs(new generated.v3.course.CustomInteropInputs.LoadArgs());
                        
                        for(generated.v2.course.Nvpair pair : custom.getLoadArgs().getNvpair()){
                            generated.v3.course.Nvpair newPair = new generated.v3.course.Nvpair();
                            newPair.setName(pair.getName());
                            newPair.setValue(pair.getValue());
                            newCustom.getLoadArgs().getNvpair().add(newPair);
                        }
                        
                        newInterop.getInteropInputs().setInteropInput(newCustom);
                        
                    }else{
                        throw new IllegalArgumentException("Found unhandled interop input type of "+interopObj);
                    }
                    
                    newTrainApp.getInterops().getInterop().add(newInterop);
                }
                
                newCourse.getTransitions().getTransitionType().add(newTrainApp);
                
            }else if(transitionObj instanceof generated.v2.course.LessonMaterial){
                
                generated.v2.course.LessonMaterial lessonMaterial = (generated.v2.course.LessonMaterial)transitionObj;
                generated.v3.course.LessonMaterial newLessonMaterial = new generated.v3.course.LessonMaterial();
                
                if(lessonMaterial.getLessonMaterialList() != null){
                    generated.v3.course.LessonMaterialList newLessonMaterialList = new generated.v3.course.LessonMaterialList();

                    for(generated.v2.course.Media media : lessonMaterial.getLessonMaterialList().getMedia()){
                        
                        generated.v3.course.Media newMedia = new generated.v3.course.Media();
                        newMedia.setName(media.getName());
                        newMedia.setUri(media.getUri());
                        
                        Object mediaType = media.getMediaTypeProperties();
                        if(mediaType instanceof generated.v2.course.PDFProperties){
                            newMedia.setMediaTypeProperties(new generated.v3.course.PDFProperties());
                            
                        }else if(mediaType instanceof generated.v2.course.WebpageProperties){
                            newMedia.setMediaTypeProperties(new generated.v3.course.WebpageProperties());
                            
                        }else if(mediaType instanceof generated.v2.course.YoutubeVideoProperties){
                            
                            generated.v2.course.YoutubeVideoProperties uTubeProp = (generated.v2.course.YoutubeVideoProperties)mediaType;
                            generated.v3.course.YoutubeVideoProperties newUTubeProp = new generated.v3.course.YoutubeVideoProperties();
                            
                            if(uTubeProp.isAllowFullScreen() != null){
                                newUTubeProp.setAllowFullScreen(uTubeProp.isAllowFullScreen());
                            }
                            
                            if(uTubeProp.getSize() != null){
                                generated.v3.course.Size size = new generated.v3.course.Size();
                                size.setHeight(uTubeProp.getSize().getHeight());
                                size.setWidth(uTubeProp.getSize().getWidth());
                                newUTubeProp.setSize(size);
                            }
                            
                            newMedia.setMediaTypeProperties(newUTubeProp);
                            
                        }else if(mediaType instanceof generated.v2.course.ImageProperties){
                            newMedia.setMediaTypeProperties(new generated.v3.course.ImageProperties());
                            
                        }else{
                            throw new IllegalArgumentException("Found unhandled media type of "+mediaType);
                        }
                        
                        newLessonMaterialList.getMedia().add(newMedia);
                    }
                    
                    newLessonMaterial.setLessonMaterialList(newLessonMaterialList);
                }

                if(lessonMaterial.getLessonMaterialFiles() != null){
                    
                    generated.v3.course.LessonMaterialFiles newFiles = new generated.v3.course.LessonMaterialFiles();
                    for(String file : lessonMaterial.getLessonMaterialFiles().getFile()){
                        newFiles.getFile().add(file);
                    }                    
                    
                    newLessonMaterial.setLessonMaterialFiles(newFiles);
                    
                }
                
                newCourse.getTransitions().getTransitionType().add(newLessonMaterial);
                
            }else{
                throw new IllegalArgumentException("Found unhandled transition type of "+transitionObj);
            }
        }
        
        // Continue the conversion with the next util.
        ConversionWizardUtil_v3_4 util = new ConversionWizardUtil_v3_4();
        util.setConversionIssueList(conversionIssueList);
        return util.convertCourse(newCourse, showCompletionDialog);
    }
    
    /**
     * Convert a Guidance course element.
     * 
     * @param guidance - the guidance element to migrate to a newer version
     * @return generated.v3.course.Guidance - the converted guidance element
     */
    private static generated.v3.course.Guidance convertGuidance(generated.v2.course.Guidance guidance){
        
        generated.v3.course.Guidance newGuidance = new generated.v3.course.Guidance();
        
        newGuidance.setDisplayTime(guidance.getDisplayTime());
        
        generated.v3.course.Guidance.Message message = new generated.v3.course.Guidance.Message();
        message.setContent(guidance.getMessage());
        newGuidance.setGuidanceChoice(message);
        
        return newGuidance;
    }
    
    /**
     * Used to test logic in this class.
     * 
     * @param args - unused
     */
    public static void main(String[] args){
        
        try {
            @SuppressWarnings("unused")
            generated.v2.course.Course course = createCourse();
            
            System.out.println("Course generated successfully.");
            
            @SuppressWarnings("unused")
            generated.v2.dkf.Scenario scenario = createScenario();
            
            System.out.println("Scenario generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getPreviousCourseSchemaFile() {
        return PREV_COURSE_SCHEMA_FILE;
    }

    @Override
    public Class<?> getPreviousCourseSchemaRoot() {
        return COURSE_ROOT;
    }

    @Override
    public File getPreviousDKFSchemaFile() {
        return PREV_DKF_SCHEMA_FILE;
    }

    @Override
    public Class<?> getPreviousDKFSchemaRoot() {
        return DKF_ROOT;
    }
}
