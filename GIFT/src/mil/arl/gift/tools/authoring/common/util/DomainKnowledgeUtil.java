/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.course.InteropsInfo.InteropInfo;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.InlineDescription;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.common.metric.assessment.PerformanceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.competence.CompetenceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.confidence.ConfidenceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.priority.PriorityMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.trend.TrendMetricInterface;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.InteropConfigFileHandler;
import mil.arl.gift.tools.authoring.common.CommonProperties;

/**
 * This class contains common util methods needed for DKF authoring tools (DAT and Web-DAT).
 *
 * @author mhoffman
 *
 */
public class DomainKnowledgeUtil {

    /** SIMILE - configuration file line items */
    private static final String BEGIN_BLOCK_COMMENT = "/*";
    private static final String CONT_BLOCK_COMMENT = "*";
    private static final String LINE_COMMENT ="//";
    private static final String QUOTATION = "\"";
    private static final String CONCEPT_LINE_KEY = "Concept( KeyName = ";

    public static final String GENERATED_PATH = "generated.dkf.";
    public static final String PACKAGE_PATH = "mil.arl.gift.";

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainKnowledgeUtil.class);

    /**
     * Contains a list of condition input generated classes (e.g. generated.dkf.GenericConditionInput)
     * which can be used to filter the list of possible inputs to a
     * condition class (e.g. mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.java)
     */
    private static List<Class<?>> inputClasses;

    /** Retrieves a list of condition input classes by annotations */
    static {

    	Field field;
    	Class<?> inputClass;
    	PropertyDescriptor p;

    	try {

	    	inputClass = Class.forName("generated.dkf.Input");

			p = Introspector.getBeanInfo(
					inputClass, Object.class).getPropertyDescriptors()[0];

			field = inputClass.getDeclaredField(p.getName());

			inputClasses = ClassFinderUtil.getChoiceTypes(field);

    	} catch(Exception e) {

    		logger.error("Error retrieving Condition Input classes: ", e);
    	}
    }

    /**
     * the list of all condition classes in the mil.arl.gift.domain.knowledge.condition package.
     * A condition classes extends AbstractCondition.class.
     */
    private static List<Class<?>> conditionClasses;

    /** Retrieves a list of condition classes by annotations */
    static {

        String packageName = "mil.arl.gift.domain.knowledge.condition";
        try {
            Set<String> excludePackages = new HashSet<>();
            if (CommonProperties.getInstance().isJRE64Bit()) {
                /* SIMILE does not support 64 bit JREs */
                excludePackages.add("mil.arl.gift.domain.knowledge.condition.simile");
            }

            if (logger.isInfoEnabled() && !excludePackages.isEmpty()) {
                logger.info("Excluding packages: [" + StringUtils.join(", ", excludePackages) + "]");
            }

            conditionClasses = ClassFinderUtil.getSubClassesOf(packageName, AbstractCondition.class, excludePackages);
        } catch (Throwable e) {
            logger.error("Error retrieving Condition classes: ", e);
        }

    }

    /**
     * the list of condition classes that call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * A condition classes extends AbstractCondition.class.
     */
    private static Set<String> conditionsThatCanComplete;

    /** retrieves a list of condition classes that can complete */
    static{
        conditionsThatCanComplete = new HashSet<>();

        // must be called after conditionClasses has been populated statically
        List<Class<?>> conditionClasses = getConditionClasses();
        for(Class<?> conditionClass : conditionClasses){

            try{
                Constructor<?> constructor = conditionClass.getConstructor();
                AbstractCondition abstractCondition = (AbstractCondition) constructor.newInstance();
                if(abstractCondition.canComplete()){
                    String conditionClassSuffix = conditionClass.getCanonicalName().substring(PACKAGE_PATH.length());
                    conditionsThatCanComplete.add(conditionClassSuffix);
                }

            }catch(@SuppressWarnings("unused") InvocationTargetException e){
                //ignore
            }catch(Exception e){
                logger.warn("Failed to determine if the condition class "+conditionClass+" calls conditionCompleted method to indicate the condition is completed.", e);
            }

        }
    }

    /**
     * Contains information about Gateway module interop plugin interface classes and the Domain module condition
     * classes that could analyze training application messages that the interop classes could produce.
     */
    private static final InteropsInfo INTEROPS_INFO;

    /**
     * Contains map of condition class impl names (e.g. "domain.knowledge.condition.ApplicationCompletedCondition") to
     * the condition info for that condition.  Note: the class names don't have the "mil.arl.gift" prefix.
     */
    private static final Map<String, ConditionInfo> conditionImplToConditionInfo;

    static{

        INTEROPS_INFO = new InteropsInfo();
        conditionImplToConditionInfo = new HashMap<>();

        //
        // First - map message types to condition classes
        //
        Map<MessageTypeEnum, List<ConditionInfo>> messageTypesToConditions = new HashMap<>();
        List<Class<?>> conditionClasses = getConditionClasses();
        for(Class<?> conditionClass : conditionClasses){

            try{
                Constructor<?> constructor = conditionClass.getConstructor();
                AbstractCondition abstractCondition = (AbstractCondition) constructor.newInstance();

                List<MessageTypeEnum> consumedMessages = abstractCondition.getSimulationInterests();
                for(MessageTypeEnum consumedMessage : consumedMessages){

                    List<ConditionInfo> conditions = messageTypesToConditions.get(consumedMessage);
                    if(conditions == null){
                        conditions = new ArrayList<>();
                        messageTypesToConditions.put(consumedMessage, conditions);
                    }

                    String condDesc, displayName;
                    if(abstractCondition.getDescription() instanceof FileDescription){

                        FileDescription fileDescription = (FileDescription)abstractCondition.getDescription();
                        byte[] encoded = Files.readAllBytes(Paths.get(fileDescription.getFile().toURI()));
                        condDesc = new String(encoded, StandardCharsets.UTF_8);

                        displayName = fileDescription.getDisplayName();

                    }else if(abstractCondition.getDescription() instanceof InlineDescription){
                        InlineDescription inlineDescription = (InlineDescription)abstractCondition.getDescription();
                        condDesc = inlineDescription.getDescription();

                        displayName = inlineDescription.getDisplayName();
                    }else{
                        condDesc = "NEED TO CREATE A CONDITION DESCRIPTION!!!";
                        displayName = "NEED TO CREATE A CONDITION DESCRIPTION!!!";
                    }

                    ConditionInfo conditionInfo =
                            new ConditionInfo(conditionClass, displayName,
                                    condDesc, abstractCondition.getSimulationInterests(), abstractCondition.getLearnerActionsNeeded());
                    conditions.add(conditionInfo);
                    conditionImplToConditionInfo.put(conditionInfo.getConditionClass(), conditionInfo);
                }
            }catch(@SuppressWarnings("unused") InvocationTargetException e){
                //ignore
            }catch(Exception e){
                logger.warn("Failed to map the condition class "+conditionClass+" to training application interop plugins (for INTEROPS_INFO).", e);
            }
        } //end for

        //Second - build interop info object by going through all interop classes
        List<String> interopClassNames = CourseUtil.getInteropImplementations();
        for(String interopClassName : interopClassNames){

            try {
                AbstractInteropInterface abstractInteropInterface = InteropConfigFileHandler.getInteropInstantiation(interopClassName, interopClassName);

                List<MessageTypeEnum> interopMessageTypes = abstractInteropInterface.getProducedMessageTypes();
                Set<ConditionInfo> conditionInfoSet = new HashSet<>();
                for(MessageTypeEnum interopMessageType : interopMessageTypes){

                    List<ConditionInfo> conditions = messageTypesToConditions.get(interopMessageType);
                    if(conditions == null){
                        continue;
                    }

                    // filter duplicate conditions that consume multiple of the messages types being looped on here
                    conditionInfoSet.addAll(conditions);
                }

                InteropInfo interopInfo = new InteropInfo(interopClassName, interopMessageTypes, conditionInfoSet);
                INTEROPS_INFO.addInteropInfo(interopInfo);

            } catch (Exception e) {
                logger.warn("Failed to map the gateway module interop plugin class "+interopClassName+" to condition classes.", e);
            }

        } // end for
    }

    /**
     * contains mapping of enumerated learner actions to the domain condition class that can be used to
     * assess using the learner action.
     * Note: not all learner actions have condition classes (e.g. tutor me)
     */
    private static final Map<generated.dkf.LearnerActionEnumType, Class<?>> learnerActionTypeToConditionClassMap;

    /**
     * contains mapping of enumerated learner actions to the condition information for the domain condition
     * class that can be used to assess using the learner action.
     * Note: not all learner actions have condition classes (e.g. tutor me)
     */
    private static final Map<generated.dkf.LearnerActionEnumType, ConditionInfo> learnerActionTypeToConditionInfoMap;

    /**
     * the list of condition information for all conditions that can be used to assess using learner actions.
     */
    private static final List<ConditionInfo> learnerActionConditionInfos;

    /**
     * the set of overall assessment types to the condition classes can populate it its life cycle.
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime} class canonical name value
     */
    private static final Map<String, Set<String>> overallAssessmentTypesConditionsMap;

    /**
     * contains mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports.
     */
    private static final Map<TrainingApplicationEnum, Set<Class<?>>> trainingAppScenarioAdaptationsMap;

    /**
     * contains mapping of training applications integrated with GIFT to the condition classes that
     * are designed to assess the learner's actions in that application.
     */
    private static final Map<TrainingApplicationEnum, Class<?>[]> trainingAppConditionClassesMap;

    /**
     * contains mapping of training applications integrated with GIFT to information about the condition
     * classes that are designed to assess the learner's actions in that application.
     */
    private static final Map<TrainingApplicationEnum, Set<ConditionInfo>> trainingAppConditionInfosMap;
    static{

        trainingAppConditionClassesMap = new HashMap<>();
        trainingAppConditionInfosMap = new HashMap<>();
        learnerActionTypeToConditionClassMap = new HashMap<>();
        learnerActionTypeToConditionInfoMap = new HashMap<>();
        learnerActionConditionInfos = new ArrayList<>();
        trainingAppScenarioAdaptationsMap = new HashMap<>();
        overallAssessmentTypesConditionsMap = new HashMap<>();

        // ARES
        Class<?>[] ARES_CONDITIONS = {
                mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.ARES, ARES_CONDITIONS);

        // DE Testbed (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] DETESTBED_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.HasCollidedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HasMovedExcavatorComponentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.DE_TESTBED, DETESTBED_CONDITIONS);

        // PowerPoint (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] POWERPOINT_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.PowerPointUnderDwellCondition.class,
                    mil.arl.gift.domain.knowledge.condition.PowerPointOverDwellCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.POWERPOINT, POWERPOINT_CONDITIONS);

        // Simple Example Training App (aka Demo Application)
        Class<?>[] DEMO_APP_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.StringMatchingExampleCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA, DEMO_APP_CONDITIONS);

        // Sudoku
        Class<?>[] SUDOKU_CONDITIONS =
                    {mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                            mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                            mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                            mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.SUDOKU, SUDOKU_CONDITIONS);

        // TC3 (aka vMedic)
        List<Class<?>> tc3ConditionList = new ArrayList<>();
        if (!CommonProperties.getInstance().isJRE64Bit()) {
            /* SIMILE does not support 64 bit JREs */
            tc3ConditionList.add(mil.arl.gift.domain.knowledge.condition.simile.SIMILEInterfaceCondition.class);
        }
        tc3ConditionList.add(mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class);
        tc3ConditionList.add(mil.arl.gift.domain.knowledge.condition.TimerCondition.class);
        tc3ConditionList.add(mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class);
        tc3ConditionList.add(mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class);

        Class<?>[] TC3_CONDITIONS = new Class<?>[tc3ConditionList.size()];
        tc3ConditionList.toArray(TC3_CONDITIONS);
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.TC3, TC3_CONDITIONS);

        // Unity Embedded
        Class<?>[] UNITY_EMBEDDED_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.StringMatchingExampleCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.UNITY_EMBEDDED, UNITY_EMBEDDED_CONDITIONS);

        // VBS (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] VBS_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CorridorPostureCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.RulesOfEngagementCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.IdentifyPOIsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.LifeformTargetAccuracyCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EliminateHostilesCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SpacingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HealthCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HaltCondition.class,
                    mil.arl.gift.domain.knowledge.condition.MuzzleFlaggingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AssignedSectorCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EngageTargetsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.DetectObjectsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.NegligentDischargeCondition.class,
                    mil.arl.gift.domain.knowledge.condition.FireTeamRateOfFireCondition.class,
                    mil.arl.gift.domain.knowledge.condition.RequestExternalAttributeCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.VBS, VBS_CONDITIONS);

        Set<Class<?>> VBS_ADAPTATIONS = new HashSet<>();
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.CreateActors.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Endurance.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.FatigueRecovery.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.RemoveActors.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Teleport.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Fog.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Overcast.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Rain.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.TimeOfDay.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Script.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.HighlightObjects.class);
        VBS_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects.class);
        trainingAppScenarioAdaptationsMap.put(TrainingApplicationEnum.VBS, VBS_ADAPTATIONS);        
        
        Set<Class<?>> DE_TESTBED_ADAPTATIONS = new HashSet<>();
        DE_TESTBED_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Fog.class);
        trainingAppScenarioAdaptationsMap.put(TrainingApplicationEnum.DE_TESTBED, DE_TESTBED_ADAPTATIONS);
        
        // HAVEN (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] HAVEN_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.RulesOfEngagementCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.LifeformTargetAccuracyCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EliminateHostilesCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SpacingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HealthCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HaltCondition.class,
                    mil.arl.gift.domain.knowledge.condition.MuzzleFlaggingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AssignedSectorCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EngageTargetsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.DetectObjectsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.NegligentDischargeCondition.class,
                    mil.arl.gift.domain.knowledge.condition.FireTeamRateOfFireCondition.class,
                    };
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.HAVEN, HAVEN_CONDITIONS);

        Set<Class<?>> HAVEN_ADAPTATIONS = new HashSet<>();
        HAVEN_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.TimeOfDay.class);
        HAVEN_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Script.class);
        trainingAppScenarioAdaptationsMap.put(TrainingApplicationEnum.HAVEN, HAVEN_ADAPTATIONS);
        
        // RIDE (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] RIDE_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.RulesOfEngagementCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.LifeformTargetAccuracyCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EliminateHostilesCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SpacingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HealthCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HaltCondition.class,
                    mil.arl.gift.domain.knowledge.condition.MuzzleFlaggingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AssignedSectorCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EngageTargetsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.DetectObjectsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.NegligentDischargeCondition.class,
                    mil.arl.gift.domain.knowledge.condition.FireTeamRateOfFireCondition.class,
                    };
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.RIDE, RIDE_CONDITIONS);

        Set<Class<?>> RIDE_ADAPTATIONS = new HashSet<>();
        RIDE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.TimeOfDay.class);
        RIDE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Script.class);
        trainingAppScenarioAdaptationsMap.put(TrainingApplicationEnum.RIDE, RIDE_ADAPTATIONS);

        Class<?>[] MOBILE_DEVICE_CONDITIONS =
            {
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class,

                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class
            };
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS, MOBILE_DEVICE_CONDITIONS);

        // VR-Engage (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] VR_ENGAGE_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CorridorPostureCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.RulesOfEngagementCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.IdentifyPOIsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.LifeformTargetAccuracyCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EliminateHostilesCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SpacingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.HaltCondition.class,
                    mil.arl.gift.domain.knowledge.condition.MuzzleFlaggingCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AssignedSectorCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EngageTargetsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.DetectObjectsCondition.class,
                    mil.arl.gift.domain.knowledge.condition.NegligentDischargeCondition.class,
                    mil.arl.gift.domain.knowledge.condition.FireTeamRateOfFireCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.VR_ENGAGE, VR_ENGAGE_CONDITIONS);

        Set<Class<?>> VR_ENGAGE_ADAPTATIONS = new HashSet<>();
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.CreateActors.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.RemoveActors.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Teleport.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Fog.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Overcast.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Rain.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.TimeOfDay.class);
        VR_ENGAGE_ADAPTATIONS.add(generated.dkf.EnvironmentAdaptation.Script.class);
        trainingAppScenarioAdaptationsMap.put(TrainingApplicationEnum.VR_ENGAGE, VR_ENGAGE_ADAPTATIONS);
        
        // VR-Engage (if adding/removing conditions, please update DomainKnowledgeUtilTest.getConditionInfosForTrainingAppTest())
        Class<?>[] UNITY_STANDALONE_CONDITIONS =
            {mil.arl.gift.domain.knowledge.condition.StringMatchingExampleCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition.class,
                    mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition.class,
                    mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition.class,
                    mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.class,
                    mil.arl.gift.domain.knowledge.condition.TimerCondition.class,
                    mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition.class,
                    mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class};
        trainingAppConditionClassesMap.put(TrainingApplicationEnum.UNITY_DESKTOP, UNITY_STANDALONE_CONDITIONS);

        // LEARNER ACTIONS
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.EXPLOSIVE_HAZARD_SPOT_REPORT, mil.arl.gift.domain.knowledge.condition.ExplosiveHazardSpotReportCondition.class);
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.NINE_LINE_REPORT, mil.arl.gift.domain.knowledge.condition.NineLineReportCondition.class);
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.RADIO, mil.arl.gift.domain.knowledge.condition.UseRadioCondition.class);
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.SPOT_REPORT, mil.arl.gift.domain.knowledge.condition.SpotReportCondition.class);
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.START_PACE_COUNT, mil.arl.gift.domain.knowledge.condition.PaceCountCondition.class);
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.END_PACE_COUNT, mil.arl.gift.domain.knowledge.condition.PaceCountCondition.class);
        learnerActionTypeToConditionClassMap.put(generated.dkf.LearnerActionEnumType.ASSESS_MY_LOCATION, mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition.class);
        //
        // Conditions - build condition info objects for all known conditions, then map those to training applications
        //
        List<Class<?>> conditionClasses = getConditionClasses();
        for(Class<?> conditionClass : conditionClasses){

            try{
                Constructor<?> constructor = conditionClass.getConstructor();
                AbstractCondition abstractCondition;
                try{
                    abstractCondition = (AbstractCondition) constructor.newInstance();
                }catch(@SuppressWarnings("unused") UnsupportedOperationException | InvocationTargetException e){
                    //ignore deprecated condition(s) (e.g. AutoTutorWebServiceInterfaceCondition)
                    continue;
                }

                String condDesc, displayName;
                if(abstractCondition.getDescription() instanceof FileDescription){

                    FileDescription fileDescription = (FileDescription)abstractCondition.getDescription();
                    byte[] encoded = Files.readAllBytes(Paths.get(fileDescription.getFile().toURI()));
                    condDesc = new String(encoded, StandardCharsets.UTF_8);

                    displayName = fileDescription.getDisplayName();

                }else if(abstractCondition.getDescription() instanceof InlineDescription){
                    InlineDescription inlineDescription = (InlineDescription)abstractCondition.getDescription();
                    condDesc = inlineDescription.getDescription();

                    displayName = inlineDescription.getDisplayName();
                }else{
                    condDesc = "NEED TO CREATE A CONDITION DESCRIPTION!!!";
                    displayName = "NEED TO CREATE A CONDITION DESCRIPTION!!!";
                }

                // populate overall assessment type condition map
                Set<Class<?>> overallAssessmentTypes = abstractCondition.getOverallAssessmenTypes();
                if(overallAssessmentTypes != null){

                    String conditionClassSuffix = conditionClass.getCanonicalName().substring(PACKAGE_PATH.length());

                    Set<String> conditionsAssessmentTypes = overallAssessmentTypesConditionsMap.get(conditionClassSuffix);
                    if(conditionsAssessmentTypes == null){
                        conditionsAssessmentTypes = new HashSet<>();
                        overallAssessmentTypesConditionsMap.put(conditionClassSuffix, conditionsAssessmentTypes);
                    }

                    for(Class<?> overallAssessmentType : overallAssessmentTypes){

                        if(overallAssessmentType.equals(generated.dkf.Count.class) ||
                                overallAssessmentType.equals(generated.dkf.ViolationTime.class) ||
                                overallAssessmentType.equals(generated.dkf.CompletionTime.class)){
                            // found a valid overall assessment type

                            conditionsAssessmentTypes.add(overallAssessmentType.getCanonicalName());
                        }
                    }
                }

                final ConditionInfo conditionInfo =
                        new ConditionInfo(conditionClass, displayName,
                                condDesc, abstractCondition.getSimulationInterests(), abstractCondition.getLearnerActionsNeeded());

                // map to the appropriate training applications
                for(TrainingApplicationEnum trainingAppEnum : trainingAppConditionClassesMap.keySet()){

                    for(Class<?> trainingAppConditionClass : trainingAppConditionClassesMap.get(trainingAppEnum)){

                        if(conditionClass.equals(trainingAppConditionClass)){
                            //the condition is for this training application

                            Set<ConditionInfo> conditionInfos = trainingAppConditionInfosMap.get(trainingAppEnum);
                            if(conditionInfos == null){
                                conditionInfos = new HashSet<>();
                                trainingAppConditionInfosMap.put(trainingAppEnum, conditionInfos);
                            }

                            conditionInfos.add(conditionInfo);
                        }
                    }
                }

                // map to the appropriate learner action type
                for(generated.dkf.LearnerActionEnumType learnerActionType : learnerActionTypeToConditionClassMap.keySet()){

                    Class<?> learnerActionTypeClass = learnerActionTypeToConditionClassMap.get(learnerActionType);
                    if(conditionClass.equals(learnerActionTypeClass)){
                        //the condition is for this learner action

                        learnerActionTypeToConditionInfoMap.put(learnerActionType, conditionInfo);

                        if(!learnerActionConditionInfos.contains(conditionInfo)){
                            learnerActionConditionInfos.add(conditionInfo);
                        }
                    }
                }
            }catch(Exception e){
                logger.warn("Failed to map the condition class "+conditionClass+" to training application interop plugins.", e);
            }
        }
    }

    /**
     * Return the list of all condition input generated classes.
     * A condition input generated classes (e.g. generated.dkf.GenericConditionInput)
     * is an argument of a condition class (e.g. mil.arl.gift.domain.knowledge.condition.EnterAreaCondition.java)
     * constructor.
     *
     * @return the superset list of all possible condition input generated classes
     * from the dkf.xsd
     */
    public static List<Class<?>> getConditionInputClasses() {
    	return inputClasses;
    }

    /**
     * Return the list of all condition classes in the mil.arl.gift.domain.knowledge.condition package.
     * A condition classes extends AbstractCondition.class.
     *
     * @return the list of all condition classes found.
     */
    public static List<Class<?>> getConditionClasses(){
        return conditionClasses;
    }

    /**
     * Return the list of condition classes that call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * @return won't be null. The condition classes without the "mil.arl.gift" prefix.
     * that can call {@link #AbstractConditionconditionCompleted()}.
     */
    public static Set<String> getConditionsThatCanComplete(){
        return conditionsThatCanComplete;
    }

    /**
     * Retrieve the list of Domain module performance assessment metric implementations available.
     *
     * @return List<String> - the performance assessment metric class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static ArrayList<String> getPerformanceMetricImplementations() throws ClassNotFoundException, IOException{

        ArrayList<String> values = new ArrayList<>();

        String packageName = "mil.arl.gift.domain.knowledge.common.metric.assessment";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, PerformanceMetricInterface.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }

        return values;
    }

    /**
     * Retrieve the list of Domain module confidence metric implementations available.
     *
     * @return List<String> - the confidence metric class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static ArrayList<String> getConfidenceMetricImplementations() throws ClassNotFoundException, IOException{

        ArrayList<String> values = new ArrayList<>();

        String packageName = "mil.arl.gift.domain.knowledge.common.metric.confidence";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, ConfidenceMetricInterface.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }

        return values;
    }

    /**
     * Retrieve the list of Domain module competence metric implementations available.
     *
     * @return List<String> - the competence metric class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static ArrayList<String> getCompetenceMetricImplementations() throws ClassNotFoundException, IOException{

        ArrayList<String> values = new ArrayList<>();

        String packageName = "mil.arl.gift.domain.knowledge.common.metric.competence";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, CompetenceMetricInterface.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }

        return values;
    }

    /**
     * Retrieve the list of Domain module trend metric implementations available.
     *
     * @return List<String> - the trend metric class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static ArrayList<String> getTrendMetricImplementations() throws ClassNotFoundException, IOException{

        ArrayList<String> values = new ArrayList<>();

        String packageName = "mil.arl.gift.domain.knowledge.common.metric.trend";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, TrendMetricInterface.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }

        return values;
    }

    /**
     * Retrieve the list of Domain module priority metric implementations available.
     *
     * @return List<String> - the priority metric class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static ArrayList<String> getPriorityMetricImplementations() throws ClassNotFoundException, IOException{

        ArrayList<String> values = new ArrayList<>();

        String packageName = "mil.arl.gift.domain.knowledge.common.metric.priority";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, PriorityMetricInterface.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }

        return values;
    }

    /**
     * Return the list of SIMILE concept names found in the SIMILE configuration file provided.
     *
     * @param simileConfigFilename the SIMILE configuration file name used for error messages about the input stream
     * @param inputStream to read and retrieve concept names from
     * @return List<String> the list of SIMILE concept names from that file
     * @throws FileNotFoundException if the file could not be found
     * @throws IllegalArgumentException if there was a problem with the contents of the file
     * @throws IOException if there was a severe problem reading the configuration file
     */
    public static List<String> getSIMILEConcepts(String simileConfigFilename, InputStream inputStream)
            throws FileNotFoundException, IllegalArgumentException, IOException{

        if(inputStream == null){
            throw new IllegalArgumentException("The input stream can't be null.");
        }

        List<String> concepts = new ArrayList<>();

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line, conceptName;
        String[] tokens;
        while((line = in.readLine()) != null){

            line = line.trim();

            //ignore empty lines, or lines that start with comments
            if(line.length() == 0 || line.startsWith(CONT_BLOCK_COMMENT) || line.startsWith(BEGIN_BLOCK_COMMENT) || line.startsWith(LINE_COMMENT)){
                continue;
            }else if(line.contains(CONCEPT_LINE_KEY)){
                //found a concept line, retrieve the concept name value

                //Concept line format:
                //  Concept( Name = "concept_name" Transition = "at_expectation" Default = "unknown" )
                //  >>> The concept_name value is desired
                tokens = line.split(Constants.SPACE);
                if(tokens.length == 11){

                    conceptName = tokens[3];
                    conceptName = conceptName.replace(QUOTATION, Constants.EMPTY);  //remove quotations

                    //don't allow duplicate concept names in this list because the user won't know the difference between
                    //each of the concepts with the same name presented in the GIFT authoring tool.
                    if(!concepts.contains(conceptName)){
                        concepts.add(conceptName);
                    }

                }else{
                    //ERROR
                    logger.error("The concept line of '"+line+"' is not formatted correctly.  There should be 11 tokens around spaces but there are "+tokens.length+" instead.");
                    in.close();
                    throw new IllegalArgumentException("The SIMILE Configuration file at "+simileConfigFilename+" has a concept line that is not properly formatted.\n" +
                                    "Check the DAT log for more details.");
                }
            }


        }//end while

        in.close();

        return concepts;

    }

    /**
     * Returns a list of condition input class names that can be used as configuration input to the specified
     * condition implementation class.  This is useful for filtering the list of condition input choices for a
     * selected condition implementation class.
     *
     * For example 'generated.dkf.ApplicationCompletedCondition' class returns 'ApplicationCompletedCondition'
     * @param conditionImplClass the condition implementation class to check condition inputs against
     * @return the collection of valid condition implementation condition input class names (the simple class name).  Can be empty but not null.
     */
    public static List<String> getValidConditionInputParams(String conditionImplClass){

        List<String> validCondInputParams = new ArrayList<>();
        for(Class<?> condInputClass : getConditionInputClasses()){

            if(DomainKnowledgeUtil.isValidConditionInputParam(PACKAGE_PATH + conditionImplClass, GENERATED_PATH + condInputClass.getSimpleName())){
                validCondInputParams.add(condInputClass.getSimpleName());
            }
        }

        return validCondInputParams;
    }

    /**
     * Returns the parameter class if it is a valid parameter for a Condition
     * class constructor, or null otherwise.
     *
     * @param conditionClass - the Condition class to compare
     * @param parameterClass - the generated Condition Input class to compare
     * @return boolean - true if the parameter class is a valid parameter for
     * 	the Condition class constructor, or false otherwise.
     */
    public static boolean isValidConditionInputParam(String conditionClass, String parameterClass) {

    	Class<?> parameter = null;
		Class<?> condition = null;
		Constructor<?> conditionConstructor = null;

    	try {

    		/* Get the parameter class name */
			parameter = Class.forName(parameterClass);

			/* Get the condition class name */
			condition = Class.forName(conditionClass);

			/* Get the condition constructor */
			conditionConstructor = condition.getConstructor(parameter);

            /* Constructors that have been deprecated are kept for legacy support but should not
               be considered valid. If the constructor exists and is not deprecated, the parameter
               is correct; return it. */
			if(conditionConstructor != null) {
                Deprecated[] annotations = conditionConstructor.getAnnotationsByType(Deprecated.class);
				return annotations.length == 0;
			}

		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {

			if(parameter == null) {
				logger.error("Invalid parameter class specified: " + parameterClass, e);
			}

			else if(condition == null) {
				logger.error("Invalid condition class specified: " + conditionClass, e);
			}

			else {
				logger.debug("Searching for matching method. Mismatch met: "
						+ condition.getSimpleName() + "(" + parameter.getSimpleName() + ")");
			}
		}

		return false;
    }

    /**
     * Return information about the Gateway module interop plugin interface classes including which conditions
     * classes might analyze the gift messages the interop plugin class could produced.
     *
     * @return information about all interop plugins.  Won't be null.  If there was an error thrown in the static
     * block of this class than it could be empty of information.
     */
    public static InteropsInfo getInteropsInfo(){
        return INTEROPS_INFO;
    }

    /**
     * Return the collection of domain modules condition classes meant to be used with the
     * specified training application.
     *
     * @param trainingApplicationEnum the training application to get the collection of conditions for
     * @return the collection of conditions to be used with the training application.  Will be null if:</br>
     * 1. the provided training application enum is null
     * 2. the provided training application enum is not in the map
     * 3. the provided training application enum has no condition classes used to assess it
     */
    public static Set<ConditionInfo> getConditionInfosForTrainingApp(TrainingApplicationEnum trainingApplicationEnum){
        return trainingAppConditionInfosMap.get(trainingApplicationEnum);
    }

    /**
     * Return the collection of scenario adaptations available for the training application type specified.
     *
     * @param trainingApplicationEnum the training application to get the collection of scenario adaptations for
     * @return the collection of scenario adaptation classes to be used with the training application.  (e.g. generated.dkf.EnvironmentAdaptation.TeleportLearner.class)
     * </br>Will be null if:</br>
     * 1. the provided training application enum is null
     * 2. the provided training application enum is not in the map
     * 3. the provided training application enum has no scenario adaptations supported
     */
    public static Set<Class<?>> getScenarioAdaptationsForTrainingApp(TrainingApplicationEnum trainingApplicationEnum){
        return trainingAppScenarioAdaptationsMap.get(trainingApplicationEnum);
    }
    
    /**
     * Return the mapping of training application type to supported scenario adaptations.
     * @return won't be null.  Values are generated classes getCanonicalName (e.g. generated.dkf.EnvironmentAdaptation.Fog).
     */
    public static Map<TrainingApplicationEnum, Set<Class<?>>>  getTrainingAppScenarioAdaptationsMap(){
        return trainingAppScenarioAdaptationsMap;
    }

    /**
     * Return the set of overall assessment types to the conditions that can populate it its life cycle.
     *
     * @return the overall assessment type map.
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime} class canonical name value.  Can be empty or null.
     */
    public static Map<String, Set<String>> getOverallAssessmentTypesConditionsMap(){
        return overallAssessmentTypesConditionsMap;
    }

    /**
     * Return the condition information for the provided condition class impl name.
     *
     * @param conditionImpl the class name of a condition class (e.g. "domain.knowledge.condition.ApplicationCompletedCondition"),
     * minus the package prefix of "mil.arl.gift".
     * @return the condition information for that condition impl, can be null if not found.
     */
    public static ConditionInfo getConditionInfoForConditionImpl(String conditionImpl){
        return conditionImplToConditionInfo.get(conditionImpl);
    }

    /**
     * Return the condition information that can assess using the specified learner action type.
     *
     * @param learnerAction the enumerated learner action to get condition information for.
     * @return can return null if the learner action provided is null or the learner action has no condition
     * to assess it.
     */
    public static ConditionInfo getConditionInfoForLearnerAction(generated.dkf.LearnerActionEnumType learnerAction){
        return learnerActionTypeToConditionInfoMap.get(learnerAction);
    }

    /**
     * Return all of the conditions used to assess the learner action types.
     *
     * @return the collection of condition information for conditions that can be used to assess
     * learner actions.
     */
    public static List<ConditionInfo> getConditionInfosForAllLearnerActions(){
        return learnerActionConditionInfos;
    }
}
