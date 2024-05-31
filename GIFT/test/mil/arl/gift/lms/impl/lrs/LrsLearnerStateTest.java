package mil.arl.gift.lms.impl.lrs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.KnowledgeSessionCourseInfo;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LowHighLevelEnum;
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
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AbstractGiftActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.processor.LearnerStateInvalidationProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.processor.LearnerStateProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts.extensionObjectKeys;
import mil.arl.gift.lms.impl.lrs.xapi.profile.tincan.TincanVerbConcepts;

public class LrsLearnerStateTest extends TestUtils {
    // lrs + statement collections
    private List<Statement> statements;
    private List<Statement> statementsUpdate;
    private List<String> usernames;
    // xAPI Components
    ItsActivityTypeConcepts.Lsa lsaATC;
    ItsResultExtensionConcepts.AttributeMeasure lsaREC;
    ItsActivityTypeConcepts.AssessmentNode.Task taskATC;
    ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate iConceptATC;
    ItsActivityTypeConcepts.AssessmentNode.Concept conceptATC;
    ItsResultExtensionConcepts.PerformanceMeasure performanceStateAttributeREC;
    ItsContextExtensionConcepts.PerformanceCharacteristics pcCEC;
    ItsContextExtensionConcepts.EvaluatorObservation eoCEC;
    ItsActivityTypeConcepts.Domain domainATC;
    ItsActivityTypeConcepts.DomainSession domainSessionATC;
    ItsVerbConcepts.Predicted predictedVC;
    ItsVerbConcepts.Demonstrated demonstratedVC;
    // xAPI - Common
    private Activity activityDomain;
    private Activity activityDomainSession;
    private UUID registration;
    private Activity affectiveActivity;
    private Activity cognitiveActivity;
    // Learner State - "original"
    private LearnerState learnerStateA;
    private AffectiveState affectiveStateA;
    private CognitiveState cognitiveStateA;
    private PerformanceState performanceStateA;
    // -> Affective Learner State Attribute
    private Activity arousalLsaActivityA;
    private ObjectNode arousalLsaExtA;
    // -> Cognitive Learner State Attributes
    // --> Engagement
    private Activity engagementLsaActivityA;
    private ObjectNode engagementLsaExtA;
    // --> Knowledge
    private Activity knowledgeLsaActivityA;
    // -> Learner State Attribute Collection Nodes + Assoicated Performance State Nodes
    TaskPerformanceState taskPerformanceStateA, taskPerformanceStateB;
    // --> Course Concepts
    private Activity courseConceptsActivityA;
    private ObjectNode courseConceptsPerformanceStateAttributeExtA, courseConceptsPerformanceStateAttributeExtB;
    private ObjectNode courseConceptsLearnerStateAttributeExtA;
    // --> All Concepts
    private Activity allConceptsActivityA;
    private ObjectNode allConceptsPerformanceStateAttributeExtA;
    private ObjectNode allConceptsLearnerStateAttributeExtA;
    // --> Some Concept A
    private Activity someConceptAActivityA;
    private ObjectNode someConceptAPerformanceStateAttributeExtA;
    private ObjectNode someConceptALearnerStateAttributeExtA;
    // --> Some Concept B
    private Activity someConceptBActivityA;
    private ObjectNode someConceptBPerformanceStateAttributeExtA;
    private ObjectNode someConceptBLearnerStateAttributeExtA;
    // --> Some Concept C
    private Activity someConceptCActivityA;
    private ObjectNode someConceptCPerformanceStateAttributeExtA;
    private ObjectNode someConceptCLearnerStateAttributeExtA;
    // Learner State - "updated"
    private LearnerState learnerStateB;
    private AffectiveState affectiveStateB;
    private CognitiveState cognitiveStateB;
    private PerformanceState performanceStateB;
    // xAPI - components (Deltas)
    // --> Course Concepts
    private Activity courseConceptsActivityB;
    // --> All Concepts
    private Activity allConceptsActivityB;
    // --> some Concept C updated
    private Activity someConceptCActivityB;
    private ObjectNode someConceptCPerformanceStateAttributeExtB;

    private void setPerformanceStateDefaults(PerformanceStateAttribute attr) {
        attr.setConfidence((float) 1.0);
        attr.setConfidenceHold(false);
        attr.setCompetence((float) 1.0);
        attr.setCompetenceHold(false);
        attr.setTrend((float) 1.0);
        attr.setTrendHold(false);
        attr.setPriorityHold(false);
        attr.setAssessmentHold(false);
        attr.setScenarioSupportNode(false);
        attr.setNodeStateEnum(PerformanceNodeStateEnum.UNACTIVATED);
    }
    
    private void prepXapi() throws Exception {
        statements = new ArrayList<Statement>();
        statementsUpdate = new ArrayList<Statement>();
        usernames = new ArrayList<String>();
        usernames.add(teamMemberUsername);
        usernames.add(studentUsername);
        lsaATC = ItsActivityTypeConcepts.Lsa.getInstance();
        lsaREC = ItsResultExtensionConcepts.AttributeMeasure.getInstance();
        pcCEC = ItsContextExtensionConcepts.PerformanceCharacteristics.getInstance();
        taskATC = ItsActivityTypeConcepts.AssessmentNode.Task.getInstance();
        iConceptATC = ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate.getInstance();
        conceptATC = ItsActivityTypeConcepts.AssessmentNode.Concept.getInstance();
        performanceStateAttributeREC = ItsResultExtensionConcepts.PerformanceMeasure.getInstance();
        affectiveActivity = ItsActivityConcepts.AffectiveStateActivity.getInstance().asActivity();
        cognitiveActivity = ItsActivityConcepts.CognitiveStateActivity.getInstance().asActivity();
        domainATC = ItsActivityTypeConcepts.Domain.getInstance();
        activityDomain = domainATC.asActivity(domainName);
        domainSessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
        activityDomainSession = domainSessionATC.asActivity(studentDomainSessionId);
        registration = UUIDHelper.createUUIDFromData(studentDomainSessionId.toString()); 
        predictedVC = ItsVerbConcepts.Predicted.getInstance();
        demonstratedVC = ItsVerbConcepts.Demonstrated.getInstance();
        eoCEC = ItsContextExtensionConcepts.EvaluatorObservation.getInstance();
    }
    
    private void prepAffectiveState() throws Exception {
        Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> affectiveAttrsA = new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttribute>();
        LearnerStateAttribute arousalA = new LearnerStateAttribute(LearnerStateAttributeNameEnum.AROUSAL,
                // shortTerm
                LowHighLevelEnum.LOW, Long.parseLong("1524771387000"),
                // longTerm
                LowHighLevelEnum.LOW, Long.parseLong("1524771387000"),
                // predicted
                LowHighLevelEnum.LOW, Long.parseLong("1524771387000"));
        affectiveAttrsA.put(LearnerStateAttributeNameEnum.AROUSAL, arousalA);
        // xAPI Correlates        
        arousalLsaActivityA = lsaATC.asActivity(arousalA);
        arousalLsaExtA = lsaREC.createExtensionItem(arousalA);
        affectiveStateA = new AffectiveState(affectiveAttrsA);
        affectiveStateB = affectiveStateA;
    }
    
    private void prepCognitiveState() throws Exception {
        Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> cognitiveAttrsA = new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttribute>();
        // --> Simple Learner State Attribute
        LearnerStateAttribute engagementA = new LearnerStateAttribute(LearnerStateAttributeNameEnum.ENGAGEMENT,
                // shortTerm
                LowHighLevelEnum.HIGH, Long.parseLong("1554380184000"),
                // longTerm
                LowHighLevelEnum.HIGH, Long.parseLong("1554380184000"),
                // predicted
                LowHighLevelEnum.HIGH, Long.parseLong("1554380184000"));
        cognitiveAttrsA.put(LearnerStateAttributeNameEnum.ENGAGEMENT, engagementA);
        // xAPI Correlates
        engagementLsaActivityA = lsaATC.asActivity(engagementA);
        engagementLsaExtA = lsaREC.createExtensionItem(engagementA);
        // --> Complex Learner State Attribute
        // ---> leaf Learner State Attributes
        // ----> some concept a
        LearnerStateAttribute someConceptA = new LearnerStateAttribute(LearnerStateAttributeNameEnum.KNOWLEDGE,
                // shortTerm
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"),
                // longTerm
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"),
                // predicted
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"));
        // xAPI correlate
        someConceptALearnerStateAttributeExtA = lsaREC.createExtensionItem(someConceptA);
        // ----> some concept b
        LearnerStateAttribute someConceptB = new LearnerStateAttribute(LearnerStateAttributeNameEnum.KNOWLEDGE,
                // shortTerm
                ExpertiseLevelEnum.EXPERT, Long.parseLong("1608153882037"),
                // longTerm
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"),
                // predicted
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"));
        // xAPI correlate
        someConceptBLearnerStateAttributeExtA = lsaREC.createExtensionItem(someConceptB);
        // ----> some concept c
        LearnerStateAttribute someConceptC = new LearnerStateAttribute(LearnerStateAttributeNameEnum.KNOWLEDGE,
                // shortTerm
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"),
                // longTerm
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"),
                // predicted
                ExpertiseLevelEnum.UNKNOWN, Long.parseLong("1608153882037"));
        // xAPI correlate
        someConceptCLearnerStateAttributeExtA = lsaREC.createExtensionItem(someConceptC);
        // ---> all concepts LearnerStateAttributeCollection
        Map<String, LearnerStateAttribute> allConceptsLabelToAttrsA = new HashMap<String, LearnerStateAttribute>();
        allConceptsLabelToAttrsA.put("some concept a", someConceptA);
        allConceptsLabelToAttrsA.put("some concept b", someConceptB);
        allConceptsLabelToAttrsA.put("some concept c", someConceptC);
        LearnerStateAttributeCollection allConceptsA = new LearnerStateAttributeCollection(LearnerStateAttributeNameEnum.KNOWLEDGE, allConceptsLabelToAttrsA);
        allConceptsLearnerStateAttributeExtA = lsaREC.createExtensionItem(allConceptsA);
        // ---> course concepts LearnerStateAttributeCollection
        Map<String, LearnerStateAttribute> courseConceptsLabelToAttrsA = new HashMap<String, LearnerStateAttribute>();
        courseConceptsLabelToAttrsA.put("all concepts", allConceptsA);
        LearnerStateAttributeCollection courseConceptsA = new LearnerStateAttributeCollection(LearnerStateAttributeNameEnum.KNOWLEDGE, courseConceptsLabelToAttrsA);
        courseConceptsLearnerStateAttributeExtA = lsaREC.createExtensionItem(courseConceptsA);
        // ---> top level Knowledge LearnerStateAttributeCollection
        Map<String, LearnerStateAttribute> knowledgeConceptsLabelToAttrsA = new HashMap<String, LearnerStateAttribute>();
        knowledgeConceptsLabelToAttrsA.put("course concepts", courseConceptsA);
        LearnerStateAttributeCollection knowledgeA = new LearnerStateAttributeCollection(LearnerStateAttributeNameEnum.KNOWLEDGE, knowledgeConceptsLabelToAttrsA);
        cognitiveAttrsA.put(LearnerStateAttributeNameEnum.KNOWLEDGE, knowledgeA);
        // xAPI correlates
        knowledgeLsaActivityA = lsaATC.asActivity(knowledgeA);
        cognitiveStateA = new CognitiveState(cognitiveAttrsA);
        cognitiveStateB = cognitiveStateA;
    }
    
    private void prepLearnerStateA() throws Exception { 
        // -> Performance State
        // --> Concept Performance State Leafs
        // ---> some concept a
        PerformanceStateAttribute performanceStateAttrASomeConceptA = new PerformanceStateAttribute("some concept a", 2, "65e58ad3-0b06-4df6-8bcd-4b51fe62c7b8",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(performanceStateAttrASomeConceptA);
        ConceptPerformanceState conceptPerformanceStateASomeConceptA = new ConceptPerformanceState(performanceStateAttrASomeConceptA);
        conceptPerformanceStateASomeConceptA.setContainsObservedAssessmentCondition(false);
        // xAPI correlates
        someConceptAActivityA = conceptATC.asActivity(conceptPerformanceStateASomeConceptA);
        someConceptAPerformanceStateAttributeExtA = performanceStateAttributeREC.createExtensionItem(conceptPerformanceStateASomeConceptA);
        // ---> some concept b
        PerformanceStateAttribute performanceStateAttrASomeConceptB = new PerformanceStateAttribute("some concept b", 3, "fa811aa4-f330-40de-a2eb-4b38dde619f7",
                // shortTerm
                AssessmentLevelEnum.ABOVE_EXPECTATION, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.ABOVE_EXPECTATION, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(performanceStateAttrASomeConceptB);
        ConceptPerformanceState conceptPerformanceStateASomeConceptB = new ConceptPerformanceState(performanceStateAttrASomeConceptB);
        conceptPerformanceStateASomeConceptB.setContainsObservedAssessmentCondition(false);
        // xAPI correlates
        someConceptBActivityA = conceptATC.asActivity(conceptPerformanceStateASomeConceptB);
        someConceptBPerformanceStateAttributeExtA = performanceStateAttributeREC.createExtensionItem(conceptPerformanceStateASomeConceptB);
        // ---> some concept c
        PerformanceStateAttribute performanceStateAttrASomeConceptC = new PerformanceStateAttribute("some concept c", 4, "4a3b5b87-fa96-45a4-87bf-cfa4d13e5899",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(performanceStateAttrASomeConceptC);
        ConceptPerformanceState conceptPerformanceStateASomeConceptC = new ConceptPerformanceState(performanceStateAttrASomeConceptC);
        conceptPerformanceStateASomeConceptC.setContainsObservedAssessmentCondition(false);
        // xAPI correlates
        someConceptCActivityA = conceptATC.asActivity(conceptPerformanceStateASomeConceptC);
        someConceptCPerformanceStateAttributeExtA = performanceStateAttributeREC.createExtensionItem(conceptPerformanceStateASomeConceptC);
        // --> Container IntermediateConceptPerformanceState nodes
        // ---> all concepts
        PerformanceStateAttribute allConceptsAPerformanceStateAttributeA = new PerformanceStateAttribute("all concepts", 5, "fc5a7511-7b82-4541-886a-686fdeebb5a2",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(allConceptsAPerformanceStateAttributeA);
        List<ConceptPerformanceState> allConceptsAConceptsA = new ArrayList<ConceptPerformanceState>();
        allConceptsAConceptsA.add(conceptPerformanceStateASomeConceptC);
        allConceptsAConceptsA.add(conceptPerformanceStateASomeConceptB);
        allConceptsAConceptsA.add(conceptPerformanceStateASomeConceptA);
        IntermediateConceptPerformanceState allConceptsAConceptPerformanceStateA = new IntermediateConceptPerformanceState(allConceptsAPerformanceStateAttributeA, allConceptsAConceptsA);
        // xAPI Correlate
        allConceptsActivityA = iConceptATC.asActivity(allConceptsAConceptPerformanceStateA);
        allConceptsPerformanceStateAttributeExtA = performanceStateAttributeREC.createExtensionItem(allConceptsAConceptPerformanceStateA);
        // --> Task State
        // ---> course concepts
        PerformanceStateAttribute courseConceptsAPerformanceStateAttributeA = new PerformanceStateAttribute("course concepts", 1, "9b61b194-6f39-4fb5-9c56-6adfd1247212",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882034"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882034"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882034"));
        setPerformanceStateDefaults(courseConceptsAPerformanceStateAttributeA);
        // Bookmark
        courseConceptsAPerformanceStateAttributeA.setEvaluator(evaluatorUsername);
        courseConceptsAPerformanceStateAttributeA.setObserverComment(observerComment);
        courseConceptsAPerformanceStateAttributeA.setObserverMedia(observerMedia);
        courseConceptsAPerformanceStateAttributeA.setObservationStartedTime(1625072878676L);
        List<ConceptPerformanceState> courseConceptsAConceptsA = new ArrayList<ConceptPerformanceState>();
        courseConceptsAConceptsA.add(allConceptsAConceptPerformanceStateA);
        taskPerformanceStateA = new TaskPerformanceState(courseConceptsAPerformanceStateAttributeA, courseConceptsAConceptsA);
        // Add Stress / Difficulty
        taskPerformanceStateA.setStress(Double.valueOf(0.5));
        taskPerformanceStateA.setStressReason("Stress Reason");
        taskPerformanceStateA.setDifficulty(Double.valueOf(1.5));
        taskPerformanceStateA.setDifficultyReason("Difficulty Reason");
        // xAPI correlate
        courseConceptsActivityA = taskATC.asActivity(taskPerformanceStateA);
        courseConceptsPerformanceStateAttributeExtA = performanceStateAttributeREC.createExtensionItem(taskPerformanceStateA);
        // --> Form Performance State A
        Map<Integer, TaskPerformanceState> tasksA = new HashMap<Integer, TaskPerformanceState>();
        tasksA.put(1, taskPerformanceStateA);
        performanceStateA = new PerformanceState(tasksA);
        performanceStateA.setEvaluator(evaluatorUsername);
        performanceStateA.setObserverComment(observerComment);
        performanceStateA.setObserverMedia(observerMedia);
        // -> Form Learner State A
        learnerStateA = new LearnerState(performanceStateA, cognitiveStateA, affectiveStateA);
    }
    
    private void prepLearnerStateB() throws Exception {
        // -> Performance State
        // --> Concept Performance State Leafs
        // ---> some concept a (unchanged)
        PerformanceStateAttribute performanceStateAttrBSomeConceptA = new PerformanceStateAttribute("some concept a", 2, "65e58ad3-0b06-4df6-8bcd-4b51fe62c7b8",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(performanceStateAttrBSomeConceptA);
        ConceptPerformanceState conceptPerformanceStateBSomeConceptA = new ConceptPerformanceState(performanceStateAttrBSomeConceptA);
        conceptPerformanceStateBSomeConceptA.setContainsObservedAssessmentCondition(false);     
        // ---> some concept b (unchanged)
        PerformanceStateAttribute performanceStateAttrBSomeConceptB = new PerformanceStateAttribute("some concept b", 3, "fa811aa4-f330-40de-a2eb-4b38dde619f7",
                // shortTerm
                AssessmentLevelEnum.ABOVE_EXPECTATION, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.ABOVE_EXPECTATION, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(performanceStateAttrBSomeConceptB);
        ConceptPerformanceState conceptPerformanceStateBSomeConceptB = new ConceptPerformanceState(performanceStateAttrBSomeConceptB);
        conceptPerformanceStateBSomeConceptB.setContainsObservedAssessmentCondition(false);        
        // ---> some concept c
        PerformanceStateAttribute performanceStateAttrBSomeConceptC = new PerformanceStateAttribute("some concept c", 4, "4a3b5b87-fa96-45a4-87bf-cfa4d13e5899",
                // shortTerm
                AssessmentLevelEnum.ABOVE_EXPECTATION, Long.parseLong("1613854885037"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(performanceStateAttrBSomeConceptC);
        performanceStateAttrBSomeConceptC.setEvaluator(evaluatorUsername);
        performanceStateAttrBSomeConceptC.setObserverComment(observerComment);
        ConceptPerformanceState conceptPerformanceStateBSomeConceptC = new ConceptPerformanceState(performanceStateAttrBSomeConceptC);
        conceptPerformanceStateBSomeConceptC.setContainsObservedAssessmentCondition(false);
        // xAPI correlates
        someConceptCActivityB = conceptATC.asActivity(conceptPerformanceStateBSomeConceptC);
        someConceptCPerformanceStateAttributeExtB = performanceStateAttributeREC.createExtensionItem(conceptPerformanceStateBSomeConceptC);        
        // --> Container IntermediateConceptPerformanceState nodes
        // ---> all concepts
        PerformanceStateAttribute allConceptsAPerformanceStateAttributeB = new PerformanceStateAttribute("all concepts", 5, "fc5a7511-7b82-4541-886a-686fdeebb5a2",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        setPerformanceStateDefaults(allConceptsAPerformanceStateAttributeB);
        List<ConceptPerformanceState> allConceptsAConceptsB = new ArrayList<ConceptPerformanceState>();
        allConceptsAConceptsB.add(conceptPerformanceStateBSomeConceptC);
        allConceptsAConceptsB.add(conceptPerformanceStateBSomeConceptB);
        allConceptsAConceptsB.add(conceptPerformanceStateBSomeConceptA);
        IntermediateConceptPerformanceState allConceptsAConceptPerformanceStateB = new IntermediateConceptPerformanceState(allConceptsAPerformanceStateAttributeB, allConceptsAConceptsB);
        // xAPI Correlate
        allConceptsActivityB = iConceptATC.asActivity(allConceptsAConceptPerformanceStateB);
        // --> Task State
        // ---> course concepts
        PerformanceStateAttribute courseConceptsAPerformanceStateAttributeB = new PerformanceStateAttribute("course concepts", 1, "9b61b194-6f39-4fb5-9c56-6adfd1247212",
                // shortTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882034"),
                // longTerm
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882034"),
                // predicted
                AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882034"));
        setPerformanceStateDefaults(courseConceptsAPerformanceStateAttributeB);
        courseConceptsAPerformanceStateAttributeB.setEvaluator(evaluatorUsername);
        List<ConceptPerformanceState> courseConceptsAConceptsB = new ArrayList<ConceptPerformanceState>();
        courseConceptsAConceptsB.add(allConceptsAConceptPerformanceStateB);
        taskPerformanceStateB = new TaskPerformanceState(courseConceptsAPerformanceStateAttributeB, courseConceptsAConceptsB);
        taskPerformanceStateB.setStress(Double.valueOf(0.75));
        taskPerformanceStateB.setStressReason("Updated Stress Reason");
        taskPerformanceStateB.setDifficulty(Double.valueOf(1.75));
        taskPerformanceStateB.setDifficultyReason("Updated Difficulty Reason");
        // xAPI correlate
        courseConceptsActivityB = taskATC.asActivity(taskPerformanceStateB);
        courseConceptsPerformanceStateAttributeExtB = performanceStateAttributeREC.createExtensionItem(taskPerformanceStateB);
        // --> Form Performance State A
        Map<Integer, TaskPerformanceState> tasksB = new HashMap<Integer, TaskPerformanceState>();
        tasksB.put(1, taskPerformanceStateB);
        performanceStateB = new PerformanceState(tasksB);
        learnerStateB = new LearnerState(performanceStateB, cognitiveStateB, affectiveStateB);
    }
    
    @Before
    public void setup() throws Exception {
        // xAPI Components
        prepXapi();
        // Shared Affective + Cognitive state
        // -> Affective State
        prepAffectiveState();
        // -> Cognitive State
        prepCognitiveState();
        // Create "original" Learner State
        prepLearnerStateA();
        // Create "updated" Learner State
        prepLearnerStateB();
    }
    
    private boolean validPredictedLearnerStateAttributeStatement(Statement stmt) {
        JsonNode stmtExt = lsaREC.parseFromExtensions(stmt.getResult().getExtensions());
        Activity stmtObject = (Activity) stmt.getObject();
        List<Activity> categoryActivities = ContextActivitiesHelper.getCategoryActivities(stmt.getContext());
        if(AbstractGiftActivity.isSameActivityId(stmtObject, arousalLsaActivityA)) {
            Assert.assertTrue("Unexpected Predicted value within extension",
                    isSameExtensionValue(arousalLsaExtA, stmtExt, extensionObjectKeys.PREDICTED));
            Assert.assertTrue("Unexpected Short Term value within extension",
                    isSameExtensionValue(arousalLsaExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
            Assert.assertTrue("Unexpected Long Term value within extension",
                    isSameExtensionValue(arousalLsaExtA, stmtExt, extensionObjectKeys.LONG_TERM));
            Assert.assertTrue("Statement did not contain affective activity",
                    isActivityInColl(categoryActivities, affectiveActivity));
            return true;
        }
        if(AbstractGiftActivity.isSameActivityId(stmtObject, engagementLsaActivityA)) {
            Assert.assertTrue("Unexpected Predicted value within extension",
                    isSameExtensionValue(engagementLsaExtA, stmtExt, extensionObjectKeys.PREDICTED));
            Assert.assertTrue("Unexpected Short Term value within extension",
                    isSameExtensionValue(engagementLsaExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
            Assert.assertTrue("Unexpected Long Term value within extension",
                    isSameExtensionValue(engagementLsaExtA, stmtExt, extensionObjectKeys.LONG_TERM));
            Assert.assertTrue("Statement did not contain cognitive activity",
                    isActivityInColl(categoryActivities, cognitiveActivity));
            return true;
        }
        Assert.assertTrue("Unexpected Predicted LSA Statement: "+stmt.toJSON(true), false);
        return false;
    }

    private boolean validPredictedAssociatedLearnerStateAttributeStatement(Statement stmt) throws Exception {
        JsonNode stmtExt = lsaREC.parseFromExtensions(stmt.getResult().getExtensions());
        Activity stmtObject = (Activity) stmt.getObject();
        List<Activity> otherActivities = ContextActivitiesHelper.getOtherActivities(stmt.getContext());
        comparePCTask(stmt, taskPerformanceStateA);
        if(AbstractGiftActivity.isSameActivityId(stmtObject, knowledgeLsaActivityA)) {
            Assert.assertTrue("Statement did not contain cognitive activity", 
                    isActivityInColl(ContextActivitiesHelper.getCategoryActivities(stmt.getContext()), cognitiveActivity));            
            if(isActivityInColl(otherActivities, courseConceptsActivityA)) {
                Assert.assertTrue("Unexpected Predicted value within extension",
                        isSameExtensionValue(courseConceptsLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.PREDICTED));
                Assert.assertTrue("Unexcpted Short Term value within extension", 
                        isSameExtensionValue(courseConceptsLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
                Assert.assertTrue("Unexpected Long Term value within extension", 
                        isSameExtensionValue(courseConceptsLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.LONG_TERM));
                return true;
            }
            if(isActivityInColl(otherActivities, allConceptsActivityA)) {
                Assert.assertTrue("Unexpected Predicted value within extension",
                        isSameExtensionValue(allConceptsLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.PREDICTED));
                Assert.assertTrue("Unexcpted Short Term value within extension", 
                        isSameExtensionValue(allConceptsLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
                Assert.assertTrue("Unexpected Long Term value within extension", 
                        isSameExtensionValue(allConceptsLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.LONG_TERM));
                return true;
            }
            if(isActivityInColl(otherActivities, someConceptCActivityA)) {
                Assert.assertTrue("Unexpected Predicted value within extension",
                        isSameExtensionValue(someConceptCLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.PREDICTED));
                Assert.assertTrue("Unexcpted Short Term value within extension", 
                        isSameExtensionValue(someConceptCLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
                Assert.assertTrue("Unexpected Long Term value within extension", 
                        isSameExtensionValue(someConceptCLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.LONG_TERM));
                return true;
            }
            if(isActivityInColl(otherActivities, someConceptBActivityA)) {
                Assert.assertTrue("Unexpected Predicted value within extension",
                        isSameExtensionValue(someConceptBLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.PREDICTED));
                Assert.assertTrue("Unexcpted Short Term value within extension", 
                        isSameExtensionValue(someConceptBLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
                Assert.assertTrue("Unexpected Long Term value within extension", 
                        isSameExtensionValue(someConceptBLearnerStateAttributeExtA, stmtExt, extensionObjectKeys.LONG_TERM));
                return true;
            }
            if(isActivityInColl(otherActivities, someConceptAActivityA)) {
                Assert.assertTrue("Unexpected Predicted value within extension",
                        isSameExtensionValue(someConceptALearnerStateAttributeExtA, stmtExt, extensionObjectKeys.PREDICTED));
                Assert.assertTrue("Unexcpted Short Term value within extension", 
                        isSameExtensionValue(someConceptALearnerStateAttributeExtA, stmtExt, extensionObjectKeys.SHORT_TERM));
                Assert.assertTrue("Unexpected Long Term value within extension", 
                        isSameExtensionValue(someConceptALearnerStateAttributeExtA, stmtExt, extensionObjectKeys.LONG_TERM));
                return true;
            }
        }
        Assert.assertTrue("Unexpected Predicted LSA with associated PSA Statement: "+stmt.toJSON(true), false);
        return false;
    }
    
    private boolean validPredictedStatement(Statement stmt) throws Exception {
        // differ based on inclusion of category contextActivity
        if(CollectionUtils.isNotEmpty(ContextActivitiesHelper.getOtherActivities(stmt.getContext()))) {
            return validPredictedAssociatedLearnerStateAttributeStatement(stmt);
        } else {
            return validPredictedLearnerStateAttributeStatement(stmt);
        }
    }
    
    private boolean isSameExtensionValue(JsonNode expected, JsonNode actual, extensionObjectKeys key) {
        JsonNode exp = expected.get(key.getValue());
        JsonNode act = actual.get(key.getValue());
        if(exp != null && act == null) {
            return false;
        } else if(exp == null && act != null) {
            return false;
        } else if(exp != null && act != null) {
            String expStr = exp.toString();
            String actStr = act.toString();
            return expStr.equals(actStr);
        } else {
            return true;
        }
    }
    
    private boolean isSameExtension(JsonNode expected, JsonNode actual) {
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.ID));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.STATE));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.OBSERVED));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.CONFIDENCE));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.COMPETENCE));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.TREND));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.EXPLANATION));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.PREDICTED));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.SHORT_TERM));
        Assert.assertTrue(isSameExtensionValue(expected, actual, extensionObjectKeys.LONG_TERM));
        return true;
    }
    
    private void comparePCTask(Statement statement, TaskPerformanceState state) throws Exception {
        JsonNode ext = pcCEC.parseFromExtensions(statement.getContext().getExtensions());
        JsonNode tasks = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.TASKS.getValue());
        JsonNode conditions = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.CONDITIONS.getValue());
        Assert.assertNull(conditions);
        Assert.assertEquals(1, tasks.size());
        JsonNode task = tasks.get(0);
        comparePcTask(task, state);
    }
    
    private boolean validDemonstratedStatement(Statement stmt) throws Exception {
        Activity stmtObject = (Activity) stmt.getObject();
        JsonNode stmtExt = performanceStateAttributeREC.parseFromExtensions(stmt.getResult().getExtensions());
        List<Activity> parentActivities = ContextActivitiesHelper.getParentActivities(stmt.getContext());
        comparePCTask(stmt, taskPerformanceStateA);
        if(AbstractGiftActivity.isSameActivityId(stmtObject, courseConceptsActivityA)) {
            Assert.assertTrue(isSameExtension(stmtExt, courseConceptsPerformanceStateAttributeExtA));
            return true;
        }
        if(AbstractGiftActivity.isSameActivityId(stmtObject, allConceptsActivityA)) {
            Assert.assertTrue(isSameExtension(stmtExt, allConceptsPerformanceStateAttributeExtA));
            Assert.assertTrue(isActivityInColl(parentActivities, courseConceptsActivityA));
            return true;
        }
        if(AbstractGiftActivity.isSameActivityId(stmtObject, someConceptCActivityA)) {
            Assert.assertTrue(isSameExtension(stmtExt, someConceptCPerformanceStateAttributeExtA));
            Assert.assertTrue(isActivityInColl(parentActivities, allConceptsActivityA));
            return true;
        }
        if(AbstractGiftActivity.isSameActivityId(stmtObject, someConceptBActivityA)) {
            Assert.assertTrue(isSameExtension(stmtExt, someConceptBPerformanceStateAttributeExtA));
            Assert.assertTrue(isActivityInColl(parentActivities, allConceptsActivityA));
            return true;
        }
        if(AbstractGiftActivity.isSameActivityId(stmtObject, someConceptAActivityA)) {
            Assert.assertTrue(isSameExtension(stmtExt, someConceptAPerformanceStateAttributeExtA));
            Assert.assertTrue(isActivityInColl(parentActivities, allConceptsActivityA));
            return true;
        }
        Assert.assertTrue("Unexpected Formative Assessment Statement: "+stmt.toJSON(true), false);
        return false;
    }
    
    private boolean validReplacementDemonstratedStatement(Statement stmt) throws Exception {
      Activity stmtObject = (Activity) stmt.getObject();
      JsonNode stmtExt = performanceStateAttributeREC.parseFromExtensions(stmt.getResult().getExtensions());
      // evaluator
      Assert.assertEquals(stmt.getContext().getInstructor().getName(), evaluatorUsername);
      comparePCTask(stmt, taskPerformanceStateB);
      if(AbstractGiftActivity.isSameActivityId(stmtObject, someConceptCActivityB)) {
          JsonNode ext = eoCEC.parseFromExtensions(stmt.getContext().getExtensions());
          Assert.assertEquals(observerComment, 
                  ext.get(ItsContextExtensionConcepts.extensionObjectKeys.COMMENT.getValue()).asText());
          Activity stmtParentActivity = iConceptATC.findInstancesInStatement(stmt).get(0);
          Assert.assertTrue(AbstractGiftActivity.isSameActivityId(stmtParentActivity, allConceptsActivityB));
          Assert.assertTrue(isSameExtension(stmtExt, someConceptCPerformanceStateAttributeExtB));
          return true;
      } else if(AbstractGiftActivity.isSameActivityId(stmtObject, courseConceptsActivityB)) {
          Assert.assertTrue(isSameExtension(stmtExt, courseConceptsPerformanceStateAttributeExtB));
          return true;
      }
      Assert.assertTrue("Unexpected replacement Formative Assessment Statement: "+stmt.toJSON(true), false);
      return false;
    }
    
    @Test
    public void testLearnerStateStatementPipeline() throws Exception {
        // populate statements with xAPI Statements generated from learnerStateA
        LearnerStateProcessor processor = new LearnerStateProcessor(studentUsername, learnerStateA, domainName, 
                studentDomainSessionId, SessionManager.getInstance().getKnowledgeSession());
        processor.process(statements);
        // Assertions for learnerStateA based xAPI Statements
        // -> Predicted Arousal
        // -> Predicted Engagement
        // -> Predicted Knowledge associated with Task Performance State (Course Concepts)
        // -> Predicted Knowledge associated with Intermediate Concept State (All Concepts)
        // -> Predicted Knowledge associated with Concept State (Concept A)
        // -> Predicted Knowledge associated with Concept State (Concept B)
        // -> Predicted KNowledge associated with Concept State (Concept C)
        // -> Demonstrated Task Performance State (Course Concepts)
        // -> Demonstrated Intermediate Concept State (All Concepts)
        // -> Demonstrated Concept State (Concept A)
        // -> Demonstrated Concept State (Concept B)
        // -> Demonstrated Concept State (Concept C)
        // -> Bookmark
        for(Statement stmt : statements) {
            if(stmt.getVerb().getId().equals(TincanVerbConcepts.Bookmarked.getInstance().asVerb().getId())) {
                // Actor
                Assert.assertEquals(PersonaHelper.createMboxAgent(evaluatorUsername), stmt.getActor());
                // Object
                Assert.assertTrue(AbstractGiftActivity.isSameActivityId(activityDomain, (Activity) stmt.getObject()));
                // Context
                JsonNode ext = eoCEC.parseFromExtensions(stmt.getContext().getExtensions());
                Assert.assertEquals(observerComment, 
                        ext.get(ItsContextExtensionConcepts.extensionObjectKeys.COMMENT.getValue()).asText());
            } else {
                // Parent domain activity
                Activity parsedDomainActivity = ((DomainActivity) activityDomain).parseFromStatement(stmt);
                Assert.assertTrue(AbstractGiftActivity.isSameActivityId(activityDomain, parsedDomainActivity));
                // Grouping domain session activity
                Activity parsedDomainSessionActivity = ((DomainSessionActivity) activityDomainSession).parseFromStatement(stmt);
                Assert.assertTrue(AbstractGiftActivity.isSameActivityId(activityDomainSession, parsedDomainSessionActivity));
                // Context.registration
                Assert.assertEquals(registration, stmt.getContext().getRegistration());
                // Branch based on verb
                if(predictedVC.asVerb().getId().equals(stmt.getVerb().getId())) {
                    Assert.assertTrue("Statement was not a valid predicted learner state attribute statement from LearnerStateA!", 
                            validPredictedStatement(stmt));
                    Assert.assertEquals(PersonaHelper.createMboxAgent(studentUsername), stmt.getActor());
                    
                } else if(demonstratedVC.asVerb().getId().equals(stmt.getVerb().getId())) {
                    Assert.assertTrue("Statement was not a valid demonstrated performance state statement from LearnerStateA!", 
                            validDemonstratedStatement(stmt));
                    JsonNode ext = performanceStateAttributeREC.parseFromExtensions(stmt.getResult().getExtensions());
                    JsonNode assessedTeamOrgEntities = ext.get(ItsResultExtensionConcepts.extensionObjectKeys.ASSESSED_TEAM_ENTITIES.getValue());
                    if(assessedTeamOrgEntities.size() == 0) {
                        Assert.assertEquals(PersonaHelper.createActor(usernames), stmt.getActor());
                    } else {
                        List<String> selectUserNames = new ArrayList<String>();
                        Iterator<String> itr = assessedTeamOrgEntities.fieldNames();
                        while(itr.hasNext()) {
                            selectUserNames.add(assessedTeamOrgEntities.get(itr.next()).asText());
                        }
                        Assert.assertEquals(PersonaHelper.createActor(selectUserNames), stmt.getActor());
                    }
                }
            }
        }
        Assert.assertEquals(13, statements.size());
        // Run the Update / Invalidation / Replacement
        // -> Create Knowledge Session with evaluator as host
        Map<Integer, SessionMember> updatedJoinedMembers = new HashMap<Integer, SessionMember>();
        UserSession teamMemUserSession = createUserSession(teamMemberId, teamMemberUsername);
        SessionMember teamMemSessionMember = createSessionMember(teamMemberRole, teamMemUserSession, teamMemberDomainSessionId);
        updatedJoinedMembers.put(teamMemberDomainSessionId, teamMemSessionMember);
        UserSession studentMemUserSession = createUserSession(studentId, studentUsername);
        SessionMember studentMemSessionMember = createSessionMember(studentHostRole, studentMemUserSession, studentDomainSessionId);
        updatedJoinedMembers.put(studentDomainSessionId, studentMemSessionMember);
        UserSession evaluatorUserSession = createUserSession(evaluatorId, evaluatorUsername);
        SessionMember evaluatorSessionMember = createSessionMember(evaluatorRole, evaluatorUserSession, evaluatorDomainSessionId);
        TeamKnowledgeSession editSession = new TeamKnowledgeSession(knowledgeSessionName, scenarioDesc, 
                new KnowledgeSessionCourseInfo(domainName, domainName, courseSourceId, null), 3, evaluatorSessionMember,
                updatedJoinedMembers, roles, team, nodeIdToNameMap, trainingAppType, sessionType, sessionStartTime,
                mission, observerControls);
        editSession.setInPastSessionMode(true);
        // -> configure processor
        LearnerStateInvalidationProcessor updateProcessor = 
                new LearnerStateInvalidationProcessor(learnerStateB, learnerStateA, evaluatorUsername, domainName, 
                        studentDomainSessionId, editSession);
        updateProcessor.process(statementsUpdate);
        // form test collection of statements
        List<Statement> invalidColl = new ArrayList<Statement>();
        // -> invalidate Demonstrated Task Performance State (Course Concepts)
        // -> invalidate Demonstrated Concept State (Concept C)
        // filter out void statement from statementsUpdate
        List<Statement> updateNoVoids = new ArrayList<Statement>();
        // -> replacement Demonstrated Task Performance State (Course Concepts)
        // -> replacement Demonstrated Concept State (Concept C)
        for(Statement stmt : statementsUpdate) {
            if(stmt.getObject().getObjectType().equalsIgnoreCase("statementref")) {
                Assert.assertEquals(PersonaHelper.createMboxAgent(evaluatorUsername), stmt.getActor());
                // Printing to out
                // -> Target Invalid Statement
//                findAndPrintVoidTarget(statements, stmt);
                // -> Replacement Statement
//                findAndPrintReplacement(statementsUpdate, stmt);
                // remove "invalid" stmt from statements and put into invalidColl
                mimicStatementVoid(statements, stmt, invalidColl);
            } else {
                updateNoVoids.add(stmt);
            }
        }
        // Assert Size of Updated after removal of void statements
        Assert.assertEquals(2, updateNoVoids.size());
        // Assert Size of dlq
        Assert.assertEquals(2, invalidColl.size());
        // Assert Size of statements after removal of invalid statements
        Assert.assertEquals(11, statements.size());
        // Check Statements in statementsUpdate after voids removed
        for(Statement stmt : updateNoVoids) {
            // Context
            // -> Context Activities
            // --> Domain Activity
            Activity parsedDomainActivity = ((DomainActivity) activityDomain).parseFromStatement(stmt);
            Assert.assertTrue(AbstractGiftActivity.isSameActivityId(activityDomain, parsedDomainActivity));
            // --> Domain Session Activity
            Activity parsedDomainSessionActivity = ((DomainSessionActivity) activityDomainSession).parseFromStatement(stmt);
            Assert.assertTrue(AbstractGiftActivity.isSameActivityId(activityDomainSession, parsedDomainSessionActivity));
            // --> Registration
            Assert.assertEquals(registration, stmt.getContext().getRegistration());
            if(demonstratedVC.asVerb().getId().equals(stmt.getVerb().getId())) {
                if(stmt.getContext().getRevision() != null) {
                    Assert.assertTrue("Statement was not a valid replacement demonstrated performance state statement from LearnerStateB!", 
                            validReplacementDemonstratedStatement(stmt));
                }
                // Actor
                JsonNode ext = performanceStateAttributeREC.parseFromExtensions(stmt.getResult().getExtensions());
                JsonNode assessedTeamOrgEntities = ext.get(ItsResultExtensionConcepts.extensionObjectKeys.ASSESSED_TEAM_ENTITIES.getValue());
                if(assessedTeamOrgEntities.size() == 0) {
                    List<String> unames = new ArrayList<String>();
                    unames.add(studentUsername);
                    unames.add(teamMemberUsername);
                    Assert.assertEquals(PersonaHelper.createActor(unames), stmt.getActor());
                } else {
                    List<String> selectUserNames = new ArrayList<String>();
                    Iterator<String> itr = assessedTeamOrgEntities.fieldNames();
                    while(itr.hasNext()) {
                        selectUserNames.add(assessedTeamOrgEntities.get(itr.next()).asText());
                    }
                    Assert.assertEquals(PersonaHelper.createActor(selectUserNames), stmt.getActor());
                }
            }
        }
    }
}
