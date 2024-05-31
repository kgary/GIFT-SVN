package mil.arl.gift.lms.impl.lrs;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.UUIDCourseRecordRefIds;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.score.ScoreUtil;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.lms.impl.lrs.xapi.CourseRecordHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AbstractGiftActivity;
import mil.arl.gift.lms.impl.lrs.xapi.processor.CourseRecordInvalidationProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.processor.CourseRecordProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.VoidedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import generated.course.Concepts;
import generated.course.Concepts.Hierarchy;
import generated.course.ConceptNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementTarget;

public class LrsCourseRecordTest extends TestUtils {
    
    private MomActivityTypeConcepts.Assessment assessmentATC;
    private ItsActivityTypeConcepts.Domain domainATC;
    private ItsActivityTypeConcepts.CourseRecord courseRecordATC;
    private ItsActivityTypeConcepts.DomainSession domainSessionATC;
    private ItsActivityTypeConcepts.TrainingApplication trainingAppATC;
    private ItsContextExtensionConcepts.PerformanceCharacteristics pcCEC;
    private MomVerbConcepts.Assessed verbConcept;
    private ItsContextExtensionConcepts.NodeHierarchy nodeHierarchyCEC;
    private Integer courseRecordId = 1;
    // set up
    private List<Statement> statements, updatedStatements;
    private Date timestamp;
    private LMSCourseRecord courseRecord, updatedCourseRecord;
    private Concepts.Hierarchy dkfConcepts;
    private CourseRecordRef courseRecordRef;
    private String rootName, childNameA, childNameB, grandChildNameA, grandChildNameB, greatGrandChildNameA,
    childNameC, grandChildNameC, taskScoreNodeNameA, taskScoreNodeNameB, taskScoreNodeNameC, taskScoreNodeNameD;
    private static final String violations = "violations";
    private static final String count = "count";
    private AssessmentChainOfCustody chainOfCustody;
    // Statement components
    private Double difficultyA, difficultyB, difficultyC, difficultyD, 
    stressA, stressB, stressC, stressD, updatedDifficultyA, updatedStressA;
    private String difficultyReasonA, difficultyReasonB, stressReasonA, stressReasonB, updatedDifficultyReasonA, updatedStressReasonA;
    Set<String> userGroupA, userGroupB, userGroupC;
    private GradedScoreNode root, childA, grandChildA, greatGrandChildA, childB, grandChildB, childC, grandChildC;
    private TaskScoreNode taskScoreNodeA, updatedTaskScoreNodeA, taskScoreNodeB, taskScoreNodeC, taskScoreNodeD;
    private RawScoreNode childARawScore, childARawScoreUpdated, grandChildARawScore, grandChildBRawScore, greatGrandChildARawScore,
    greatGrandChildARawScoreUpdated, childCRawScore, grandChildCRawScore, taskScoreNodeARawScore, taskScoreNodeBRawScore, taskScoreNodeBRawScoreUpdated,
    taskScoreNodeCRawScore, taskScoreNodeCRawScoreUpdated, taskScoreNodeDRawScore, TaskScoreNodeBIndividualA, TaskScoreNodeBIndividualB,
    TaskScoreNodeBIndividualAUpdated, TaskScoreNodeBIndividualBUpdated, childARawScoreSecond, childARawScoreSecondUpdated;
    private Map<Integer, Activity> hierarchyChildA, hierarchyGrandChildA, hierarchyGrandChildB, hierarchyGreatGrandChildA,
    hierarchyChildC, hierarchyGrandChildC, hierarchyTaskScoreNodeA, hierarchyTaskScoreNodeB, hierarchyTaskScoreNodeC, hierarchyTaskScoreNodeD;
    private Activity activityRoot, activityChildA, activityGrandChildA, activityGrandChildB, activityGreatGrandChildA, activityDomain, activityDomainSession,
    activityCourseRecord, activityChildC, activityGrandChildC, activityTaskScoreNodeA, activityTaskScoreNodeB, activityTaskScoreNodeC, activityTaskScoreNodeD,
    activityTrainingApp;
    private UUID registration;
    private String responseChildA, responseGrandChildA, responseGrandChildB, responseGreatGrandChildA, responseChildC, responseGrandChildC,
    responseTaskA, responseTaskB, responseTaskC, updatedResponseTaskB, updatedResonseGreatGrandChildA, responseTaskD;
    private CourseRecordInvalidationProcessor invalidationProcessor;
    private CourseRecordProcessor processor;
    
    public void setupCommon() throws Exception {
        // Empty coll of statements and now as date
        statements = new ArrayList<Statement>();
        updatedStatements = new ArrayList<Statement>();
        timestamp = new Date(1614201099);
        userGroupA = new HashSet<String>();
        userGroupA.add(studentUsername);
        userGroupB = new HashSet<String>(userGroupA);
        userGroupB.add(teamMemberUsername);
        userGroupC = new HashSet<String>(1);
        userGroupC.add(teamMemberUsername);
        // xAPI
        assessmentATC = MomActivityTypeConcepts.Assessment.getInstance();
        domainATC = ItsActivityTypeConcepts.Domain.getInstance();
        courseRecordATC = ItsActivityTypeConcepts.CourseRecord.getInstance();
        domainSessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
        trainingAppATC = ItsActivityTypeConcepts.TrainingApplication.getInstance();
        verbConcept = MomVerbConcepts.Assessed.getInstance();
        nodeHierarchyCEC = ItsContextExtensionConcepts.NodeHierarchy.getInstance();
        pcCEC = ItsContextExtensionConcepts.PerformanceCharacteristics.getInstance();
        // Node names
        rootName = "root node";
        childNameA = "child a";
        childNameB = "child b";
        childNameC = "child c";
        grandChildNameA = "grand child a";
        grandChildNameB = "grand child b";
        grandChildNameC = "grand child c";
        greatGrandChildNameA = "great grand child a";
        taskScoreNodeNameA = "task node a";
        taskScoreNodeNameB = "task node b";
        taskScoreNodeNameC = "task node c";
        taskScoreNodeNameD = "task node d";
        chainOfCustody = new AssessmentChainOfCustody(studentId, studentDomainSessionId, "test-path", "test-dkf", "test-log-file-name");
        difficultyA = Double.valueOf(1.0);
        difficultyReasonA = "min value for difficulty";
        stressA = Double.valueOf(0.0);
        stressReasonA = "min value for stress";
        updatedDifficultyA = Double.valueOf(1.5);
        updatedDifficultyReasonA = "updated value for difficulty";
        updatedStressA = Double.valueOf(0.5);
        updatedStressReasonA = "updated value for stress";
        difficultyB = Double.valueOf(3.0);
        difficultyReasonB = "max value for difficulty";
        stressB = Double.valueOf(1.0);
        stressReasonB = "max value for stress";
        difficultyC = Double.valueOf(1.0);
        stressC = Double.valueOf(0.0);
        difficultyD = Double.valueOf(2.5);
        stressD = Double.valueOf(0.75);
    }
    
    public void setupDKF() throws Exception {
        // Build DKF Course Concepts
        dkfConcepts = new Hierarchy();
        // -> root
        ConceptNode courseRoot = new ConceptNode();
        courseRoot.setName(rootName);
        // -> valid child
        ConceptNode courseChildA = new ConceptNode();
        courseChildA.setName(childNameA);
        // -> valid child only found in original course record
        ConceptNode courseChildC = new ConceptNode();
        courseChildC.setName(childNameC);
        // -> TaskScoreNode A child
        ConceptNode courseTaskNodeA = new ConceptNode();
        courseTaskNodeA.setName(taskScoreNodeNameA);
        // -> TaskScoreNode B child
        ConceptNode courseTaskNodeB = new ConceptNode();
        courseTaskNodeB.setName(taskScoreNodeNameB);
        // -> TaskScoreNode C child
        ConceptNode courseTaskNodeC = new ConceptNode();
        courseTaskNodeC.setName(taskScoreNodeNameC);
        // -> TaskScoreNode D child
        ConceptNode courseTaskNodeD = new ConceptNode();
        courseTaskNodeD.setName(taskScoreNodeNameD);
        // -> valid grand child
        ConceptNode courseGrandChildA = new ConceptNode();
        courseGrandChildA.setName(grandChildNameA);
        // -> valid grand child only found in updated course record
        ConceptNode courseGrandChildB = new ConceptNode();
        courseGrandChildB.setName(grandChildNameB);
        // -> third grand child only found in original course record
        ConceptNode courseGrandChildC = new ConceptNode();
        courseGrandChildC.setName(grandChildNameC);
        // -> valid great grand child
        ConceptNode courseGreatGrandChildA = new ConceptNode();
        courseGreatGrandChildA.setName(greatGrandChildNameA);
        // -> Great Grand Child A as child of Grand Child A
        courseGrandChildA.getConceptNode().add(courseGreatGrandChildA);
        // -> Grand Child C child of Child C
        courseChildC.getConceptNode().add(courseGrandChildC);
        // -> Grand Child A + B + TaskNodeA  + TaskNodeB as child of Child A
        courseChildA.getConceptNode().add(courseGrandChildA);
        courseChildA.getConceptNode().add(courseGrandChildB);
        courseChildA.getConceptNode().add(courseTaskNodeA);
        courseChildA.getConceptNode().add(courseTaskNodeB);
        courseChildA.getConceptNode().add(courseTaskNodeC);
        courseChildA.getConceptNode().add(courseTaskNodeD);
        // -> A as child of Root
        courseRoot.getConceptNode().add(courseChildA);
        // -> C as child of Root
        courseRoot.getConceptNode().add(courseChildC);
        dkfConcepts.setConceptNode(courseRoot);
    }
    
    public void setupOriginalCourseRecord() throws Exception {
        // Build GradedScoreNode to be processed
        root = new GradedScoreNode(rootName);
        // first level of children
        // -> valid course concept line
        childA = new GradedScoreNode(childNameA);
        DefaultRawScore childAScore = new DefaultRawScore("2", violations);
        childARawScore = new RawScoreNode("First Level Assessment", childAScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupA);
        childA.addChild(childARawScore);
        // second indiviudal assessed for childA
        childARawScoreSecond = new RawScoreNode("First Level Assessment", childAScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupC);
        childA.addChild(childARawScoreSecond);
        // -> invalid course concept line
        childB = new GradedScoreNode(childNameB);
        DefaultRawScore childBScore = new DefaultRawScore("100", "percentage");
        RawScoreNode childBRawScore = new RawScoreNode("Course Completion", childBScore, AssessmentLevelEnum.ABOVE_EXPECTATION, userGroupA);
        childB.addChild(childBRawScore);
        // -> valid course concept not found within updated course record
        childC = new GradedScoreNode(childNameC);
        DefaultRawScore childCScore = new DefaultRawScore("7", violations);
        childCRawScore = new RawScoreNode("Incorrect Assessment", childCScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupB);
        childC.addChild(childCRawScore);
        // -> TaskScoreNode A
        taskScoreNodeA = new TaskScoreNode(taskScoreNodeNameA);
        taskScoreNodeA.setDifficulty(difficultyA);
        taskScoreNodeA.setStress(stressA);
        taskScoreNodeA.setDifficultyReason(difficultyReasonA);
        taskScoreNodeA.setStressReason(stressReasonA);
        DefaultRawScore taskScoreNodeAScore = new DefaultRawScore("0", violations);
        taskScoreNodeARawScore = new RawScoreNode("Task Node A Assessment", taskScoreNodeAScore, AssessmentLevelEnum.ABOVE_EXPECTATION, userGroupA);
        taskScoreNodeA.addChild(taskScoreNodeARawScore);
        // -> Descendant course concept not found within updated course record
        grandChildC = new GradedScoreNode(grandChildNameC);
        DefaultRawScore grandChildCScore = new DefaultRawScore("0", violations);
        grandChildCRawScore = new RawScoreNode("Descendant Incorrect Assessment", grandChildCScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        grandChildC.addChild(grandChildCRawScore);
        // second level of children
        grandChildA = new GradedScoreNode(grandChildNameA);
        DefaultRawScore grandChildAScore = new DefaultRawScore("0", violations);
        grandChildARawScore = new RawScoreNode("Second Level Assessment", grandChildAScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        grandChildA.addChild(grandChildARawScore);
        // third level of children
        greatGrandChildA = new GradedScoreNode(greatGrandChildNameA);
        DefaultRawScore greatGrandChildAScore = new DefaultRawScore("3", count);
        greatGrandChildARawScore = new RawScoreNode("Third Level Assessment", greatGrandChildAScore, AssessmentLevelEnum.ABOVE_EXPECTATION, userGroupA);
        greatGrandChildA.addChild(greatGrandChildARawScore);
        // fourth level of children
        taskScoreNodeB = new TaskScoreNode(taskScoreNodeNameB);
        taskScoreNodeB.setDifficulty(difficultyB);
        taskScoreNodeB.setStress(stressB);
        taskScoreNodeB.setDifficultyReason(difficultyReasonB);
        taskScoreNodeB.setStressReason(stressReasonB);
        DefaultRawScore taskScoreNodeBScore = new DefaultRawScore("1", violations);
        // Group Assessment
        taskScoreNodeBRawScore = new RawScoreNode("Task Node B Assessment", taskScoreNodeBScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupB);
        taskScoreNodeB.addChild(taskScoreNodeBRawScore);
        // Individuals within Group
        TaskScoreNodeBIndividualA = new RawScoreNode("Task Node B Assessment", taskScoreNodeBScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        TaskScoreNodeBIndividualB = new RawScoreNode("Task Node B Assessment", taskScoreNodeBScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupC);
        taskScoreNodeB.addChild(TaskScoreNodeBIndividualA);
        taskScoreNodeB.addChild(TaskScoreNodeBIndividualB);
        // fifth level of children
        taskScoreNodeC = new TaskScoreNode(taskScoreNodeNameC);
        taskScoreNodeC.setDifficulty(difficultyC);
        taskScoreNodeC.setStress(stressC);
        DefaultRawScore taskScoreNodeCScore = new DefaultRawScore("2", violations);
        taskScoreNodeCRawScore = new RawScoreNode("Task Node C Assessment", taskScoreNodeCScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupB);
        taskScoreNodeC.addChild(taskScoreNodeCRawScore);
        // -> Build Root GradedScoreNode
        taskScoreNodeB.addChild(taskScoreNodeC);
        greatGrandChildA.addChild(taskScoreNodeB);
        grandChildA.addChild(greatGrandChildA);
        childA.addChild(grandChildA);
        childC.addChild(grandChildC);
        root.addChild(childA);
        root.addChild(childB);
        root.addChild(childC);
        root.addChild(taskScoreNodeA);
        // -> Perform roll up
        ScoreUtil.performAssessmentRollup(root, true);
        // LMS Course Record
        courseRecordRef = CourseRecordRef.buildCourseRecordRefFromInt(courseRecordId);
        courseRecord = new LMSCourseRecord(courseRecordRef, domainName, root, timestamp);
        courseRecord.setGiftEventId(studentDomainSessionId);
        courseRecord.setLMSConnectionInfo(lrs.getConnectionInfo());
        // Course Record Processor
        processor = new CourseRecordProcessor(courseRecord, dkfConcepts, domainName, studentDomainSessionId, 
                SessionManager.getInstance().getKnowledgeSession());
        processor.process(statements);
    }
    
    public void setupUpdatedCourseRecord() throws Exception {
        // rebuild GradedScoreNode tree w/ updates
        root = new GradedScoreNode(rootName);
        // first level of children (leave out childB but no-op bc non course concept)
        // -> updated assessed users
        childA = new GradedScoreNode(childNameA);
        DefaultRawScore childAScore = new DefaultRawScore("2", violations);
        childARawScoreUpdated = new RawScoreNode("First Level Assessment", childAScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupB);
        childA.addChild(childARawScoreUpdated);
        // Update score for second individual
        childARawScoreSecondUpdated = new RawScoreNode("First Level Assessment", childAScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupC);
        childA.addChild(childARawScoreSecondUpdated);
        // -> TaskScoreNode A
        updatedTaskScoreNodeA = new TaskScoreNode(taskScoreNodeNameA);
        updatedTaskScoreNodeA.setDifficulty(updatedDifficultyA);
        updatedTaskScoreNodeA.setStress(updatedStressA);
        updatedTaskScoreNodeA.setDifficultyReason(updatedDifficultyReasonA);
        updatedTaskScoreNodeA.setStressReason(updatedStressReasonA);
        DefaultRawScore taskScoreNodeAScore = new DefaultRawScore("0", violations);
        taskScoreNodeARawScore = new RawScoreNode("Task Node A Assessment", taskScoreNodeAScore, AssessmentLevelEnum.ABOVE_EXPECTATION, userGroupA);
        updatedTaskScoreNodeA.addChild(taskScoreNodeARawScore);
        // -> TaskScoreNode D (novel)
        taskScoreNodeD = new TaskScoreNode(taskScoreNodeNameD);
        taskScoreNodeD.setDifficulty(difficultyD);
        taskScoreNodeD.setStress(stressD);
        DefaultRawScore taskScoreNodeDScore = new DefaultRawScore("1", violations);
        taskScoreNodeDRawScore = new RawScoreNode("Task Node D Assessment", taskScoreNodeDScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        taskScoreNodeD.addChild(taskScoreNodeDRawScore);
        // second level of children
        // -> grand child a stays the same
        grandChildA = new GradedScoreNode(grandChildNameA);
        DefaultRawScore grandChildAScore = new DefaultRawScore("0", violations);
        grandChildARawScore = new RawScoreNode("Second Level Assessment", grandChildAScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        grandChildA.addChild(grandChildARawScore);
        // -> add grand child b
        grandChildB = new GradedScoreNode(grandChildNameB);
        DefaultRawScore grandChildBScore = new DefaultRawScore("1", violations);
        grandChildBRawScore = new RawScoreNode("Second Level Assessment B", grandChildBScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupB);
        grandChildB.addChild(grandChildBRawScore);
        // third level of children
        // -> update assessment
        greatGrandChildA = new GradedScoreNode(greatGrandChildNameA);
        DefaultRawScore greatGrandChildAScore = new DefaultRawScore("3", count);
        greatGrandChildARawScoreUpdated = new RawScoreNode("Third Level Assessment", greatGrandChildAScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        greatGrandChildA.addChild(greatGrandChildARawScoreUpdated);
        // fourth level of children
        // -> updated Task Node B
        taskScoreNodeB = new TaskScoreNode(taskScoreNodeNameB);
        taskScoreNodeB.setDifficulty(difficultyB);
        taskScoreNodeB.setStress(stressB);
        taskScoreNodeB.setDifficultyReason(difficultyReasonB);
        taskScoreNodeB.setStressReason(stressReasonB);
        DefaultRawScore taskScoreNodeBScore = new DefaultRawScore("3", violations);
        // Group Assessment
        taskScoreNodeBRawScoreUpdated = new RawScoreNode("Task Node B Assessment", taskScoreNodeBScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupB);
        taskScoreNodeB.addChild(taskScoreNodeBRawScoreUpdated);
        // Individual Assessments
        TaskScoreNodeBIndividualAUpdated = new RawScoreNode("Task Node B Assessment", taskScoreNodeBScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupA);
        TaskScoreNodeBIndividualBUpdated = new RawScoreNode("Task Node B Assessment", taskScoreNodeBScore, AssessmentLevelEnum.BELOW_EXPECTATION, userGroupC);
        taskScoreNodeB.addChild(TaskScoreNodeBIndividualAUpdated);
        taskScoreNodeB.addChild(TaskScoreNodeBIndividualBUpdated);
        // fifth level of children
        // -> updated Task Node C user group
        taskScoreNodeC = new TaskScoreNode(taskScoreNodeNameC);
        taskScoreNodeC.setDifficulty(difficultyC);
        taskScoreNodeC.setStress(stressC);
        DefaultRawScore taskScoreNodeCScore = new DefaultRawScore("2", violations);
        taskScoreNodeCRawScoreUpdated = new RawScoreNode("Task Node C Assessment", taskScoreNodeCScore, AssessmentLevelEnum.AT_EXPECTATION, userGroupA);
        taskScoreNodeC.addChild(taskScoreNodeCRawScoreUpdated);
        // -> Build Root GradedScoreNode
        taskScoreNodeB.addChild(taskScoreNodeC);
        greatGrandChildA.addChild(taskScoreNodeB);
        grandChildA.addChild(greatGrandChildA);
        childA.addChild(grandChildA);
        childA.addChild(grandChildB);
        root.addChild(childA);
        root.addChild(updatedTaskScoreNodeA);
        root.addChild(taskScoreNodeD);
        // -> Perform roll up
        ScoreUtil.performAssessmentRollup(root, true);
        // LMS Course Record
        courseRecordRef = CourseRecordRef.buildCourseRecordRefFromInt(courseRecordId);
        updatedCourseRecord = new LMSCourseRecord(courseRecordRef, domainName, root, timestamp);
        updatedCourseRecord.setGiftEventId(studentDomainSessionId);
        // Course Record Update Processor
        invalidationProcessor = new CourseRecordInvalidationProcessor(courseRecord, updatedCourseRecord, dkfConcepts,
                domainName, chainOfCustody, SessionManager.getInstance().getKnowledgeSession());
        invalidationProcessor.process(updatedStatements);
    }
    
    public void setupXapiExpectations() throws Exception {
        // pre-compute expected activities for graded score nodes
        activityRoot = assessmentATC.asActivity(root);
        activityChildA = assessmentATC.asActivity(childA);
        activityGrandChildA = assessmentATC.asActivity(grandChildA);
        activityGreatGrandChildA = assessmentATC.asActivity(greatGrandChildA);
        activityChildC = assessmentATC.asActivity(childC);
        activityGrandChildC = assessmentATC.asActivity(grandChildC);
        activityTaskScoreNodeA = assessmentATC.asActivity(taskScoreNodeA);
        activityTaskScoreNodeB = assessmentATC.asActivity(taskScoreNodeB);
        activityTaskScoreNodeC = assessmentATC.asActivity(taskScoreNodeC);
        // Expected family trees
        hierarchyChildA = new HashMap<Integer, Activity>();
        hierarchyChildA.put(0, activityRoot);
        hierarchyTaskScoreNodeA = new HashMap<Integer, Activity>(hierarchyChildA);
        hierarchyChildC = new HashMap<Integer, Activity>(hierarchyChildA);
        hierarchyGrandChildC = new HashMap<Integer, Activity>(hierarchyChildC);
        hierarchyGrandChildC.put(1, activityChildC);
        hierarchyGrandChildA = new HashMap<Integer, Activity>(hierarchyChildA);
        hierarchyGrandChildA.put(1, activityChildA);
        hierarchyGreatGrandChildA = new HashMap<Integer, Activity>(hierarchyGrandChildA);
        hierarchyGreatGrandChildA.put(2, activityGrandChildA);
        hierarchyTaskScoreNodeB = new HashMap<Integer, Activity>(hierarchyGreatGrandChildA);
        hierarchyTaskScoreNodeB.put(3, activityGreatGrandChildA);
        hierarchyTaskScoreNodeC = new HashMap<Integer, Activity>(hierarchyTaskScoreNodeB);
        hierarchyTaskScoreNodeC.put(4, activityTaskScoreNodeB);
        // Expected Context Activities
        activityDomain = domainATC.asActivity(domainName);
        activityDomainSession = domainSessionATC.asActivity(studentDomainSessionId);
        activityCourseRecord = courseRecordATC.asActivity(courseRecordRef);
        activityTrainingApp = trainingAppATC.asActivity(trainingAppType);
        // Registration
        registration = UUIDHelper.createUUIDFromData(studentDomainSessionId.toString());
        // Result Response
        responseChildA = childA.getGradeAsString();
        responseGrandChildA = grandChildA.getGradeAsString();
        responseGreatGrandChildA = greatGrandChildA.getGradeAsString();
        responseChildC = childC.getGradeAsString();
        responseGrandChildC = grandChildC.getGradeAsString();
        responseTaskA = taskScoreNodeA.getGradeAsString();
        responseTaskB = taskScoreNodeB.getGradeAsString();
        responseTaskC = taskScoreNodeC.getGradeAsString();
    }
    
    public void setupUpdatedXapiExpectations() throws Exception {
        activityGrandChildB = assessmentATC.asActivity(grandChildB);
        hierarchyGrandChildB = new HashMap<Integer, Activity>(hierarchyChildA);
        hierarchyGrandChildB.put(1, activityChildA);
        responseGrandChildB = grandChildB.getGradeAsString();
        updatedResponseTaskB = taskScoreNodeB.getGradeAsString();
        // Failure is fixed via distinct var instead of overwrite but note still relevant
        // Grade for greatGrandChildA -> fail bc TaskNodeB RawScoreNode is Below Expectation
        // NOTE: failure happens at child node but assessment at current node isn't failure
        //       should statement be updated to reflect this? currently the "due to child node"
        //       part is lost in translation
        updatedResonseGreatGrandChildA = greatGrandChildA.getGradeAsString();
        activityTaskScoreNodeD = assessmentATC.asActivity(taskScoreNodeD);
        hierarchyTaskScoreNodeD = new HashMap<Integer, Activity>(hierarchyChildA);
        responseTaskD = taskScoreNodeD.getGradeAsString();
    }
    
    @Before
    public void setup() throws Exception {
        setupCommon();
        setupDKF();
        setupOriginalCourseRecord();
        setupXapiExpectations();
        setupUpdatedCourseRecord();
        setupUpdatedXapiExpectations();
    }
    
    private Boolean areHierarchiesSame(Map<Integer, Activity> a, Map<Integer, Activity> b) {
        for(Map.Entry<Integer, Activity> src : a.entrySet()) {
            // look for corresponding thing in b
            Activity activityB = b.get(src.getKey());
            if(activityB == null) {
                return false;
            }
            String activityIdA = src.getValue().getId().toString();
            if(!activityIdA.equals(activityB.getId().toString())) {
                return false;
            }
        }
        for(Map.Entry<Integer, Activity> target : b.entrySet()) {
            // look for corresponding thing in a
            Activity activityA = a.get(target.getKey());
            if(activityA == null) {
                return false;
            }
        }
        return true;
    }
    
    private Boolean compareLMSCourseRecords(LMSCourseRecord a, LMSCourseRecord b) {
        if(!a.getDate().equals(b.getDate())) {
            return false;
        }
        if(!a.getDomainName().equalsIgnoreCase(b.getDomainName())) {
            return false;
        }
        if(!a.getCourseRecordRef().equals(b.getCourseRecordRef())) {
            return false;
        }
        if(!a.getGIFTEventId().equals(b.getGIFTEventId())) {
            return false;
        }
        if(!CourseRecordHelper.isSameNode(a.getRoot(), b.getRoot())) {
            return false;
        }
        return true;
    }
    
    private void comparePcTask(Statement statement, List<TaskScoreNode> nodes) throws Exception {
        JsonNode ext = pcCEC.parseFromExtensions(statement.getContext().getExtensions());
        if(nodes.isEmpty()) {
            Assert.assertNull(ext);
        } else {
            JsonNode tasks = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.TASKS.getValue());
            JsonNode conditions = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.CONDITIONS.getValue());
            Assert.assertNull(conditions);
            Assert.assertEquals(nodes.size(), tasks.size());
            for(int i = 0; i < tasks.size(); i++) {
                comparePcTask(tasks.get(i), nodes.get(i));
            }
        }
    }
    
    @Test
    public void testCourseRecordStatementPipeline() throws Exception {
        // 11 statements expected given setup
        Assert.assertEquals(11, statements.size());
        // Actor used in majority of statements
        Agent agentActor = PersonaHelper.createMboxAgent(studentUsername);
        // Ensure all 11 are valid
        for(Statement stmt : statements) {
            // Verb
            Assert.assertEquals(verbConcept.asVerb().getId(), stmt.getVerb().getId());
            // Object
            Activity stmtObject = (Activity) stmt.getObject();
            // Context
            // -> Extensions
            Map<Integer, Activity> hierarchy = nodeHierarchyCEC.parseToMap(stmt);
            // Result
            // -> Response
            String resultResponse = stmt.getResult().getResponse();
            // -> Extensions
            RawScoreNode stmtRSN = CourseRecordHelper.parseRsnFromStatement(stmt);
            // Statement expectations based on Statement object
            if(AbstractGiftActivity.isSameActivityId(stmtObject, activityChildA)) {
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyChildA));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseChildA));
                if(CourseRecordHelper.isSameNode(stmtRSN, childARawScore)) {
                    Assert.assertEquals(agentActor, stmt.getActor());
                } else if(CourseRecordHelper.isSameNode(stmtRSN, childARawScoreSecond)) {
                    Agent agent = PersonaHelper.createMboxAgent(teamMemberUsername);
                    Assert.assertEquals(agent, stmt.getActor());
                } else {
                    Assert.assertTrue("Unexpected RawScoreNode for Statement with activityChildA object!", false);
                }
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityGrandChildA)) {
                Assert.assertEquals(agentActor, stmt.getActor());
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyGrandChildA));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseGrandChildA));
                Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                        CourseRecordHelper.isSameNode(stmtRSN, grandChildARawScore));
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityGreatGrandChildA)) {
                Assert.assertEquals(agentActor, stmt.getActor());
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyGreatGrandChildA));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseGreatGrandChildA));
                Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                        CourseRecordHelper.isSameNode(stmtRSN, greatGrandChildARawScore));
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityChildC)) {
                Group actor = (Group) stmt.getActor();
                Assert.assertEquals(2, actor.getMembers().size());
                Assert.assertTrue("Statement's Actor was not the expected Group!",
                        CourseRecordHelper.createActorFromRawScoreNode(stmtRSN).equals(actor));
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyChildC));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseChildC));
                Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                        CourseRecordHelper.isSameNode(stmtRSN, childCRawScore));
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityGrandChildC)) {
                Assert.assertEquals(agentActor, stmt.getActor());
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyGrandChildC));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseGrandChildC));
                Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                        CourseRecordHelper.isSameNode(stmtRSN, grandChildCRawScore));
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityTaskScoreNodeA)) {
                Assert.assertEquals(agentActor, stmt.getActor());
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeA));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseTaskA));
                Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                        CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeARawScore));
                List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                nodes.add(taskScoreNodeA);
                comparePcTask(stmt, nodes);
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityTaskScoreNodeB)) {
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeB));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseTaskB));
                List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                nodes.add(taskScoreNodeB);
                comparePcTask(stmt, nodes);
                if(CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeBRawScore)) {
                    Group actor = (Group) stmt.getActor();
                    Assert.assertEquals(2, actor.getMembers().size());
                    Assert.assertTrue("Statement's Actor was not the expected Group!",
                            CourseRecordHelper.createActorFromRawScoreNode(stmtRSN).equals(actor));
                } else if(CourseRecordHelper.isSameNode(stmtRSN, TaskScoreNodeBIndividualA)) {
                    Assert.assertEquals(agentActor, stmt.getActor());
                } else if(CourseRecordHelper.isSameNode(stmtRSN, TaskScoreNodeBIndividualB)) {
                    Agent agent = PersonaHelper.createMboxAgent(teamMemberUsername);
                    Assert.assertEquals(agent, stmt.getActor());
                } else {
                    Assert.assertTrue("Unexpected RawScoreNode for Statement with TaskScoreNodeB object!", false); 
                } 
            } else if(AbstractGiftActivity.isSameActivityId(stmtObject, activityTaskScoreNodeC)) {
                Group actor = (Group) stmt.getActor();
                Assert.assertEquals(2, actor.getMembers().size());
                Assert.assertTrue("Statement's Actor was not the expected Group!",
                        CourseRecordHelper.createActorFromRawScoreNode(stmtRSN).equals(actor));
                Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                        areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeC));
                Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                        resultResponse.equals(responseTaskC));
                Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                        CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeCRawScore));
                List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                nodes.add(taskScoreNodeB);
                nodes.add(taskScoreNodeC);
                comparePcTask(stmt, nodes);
            } else {
                Assert.assertTrue("Statement Object was not one of the expected Graded Score Node Activities!",
                        false);
            }
            // Common Context
            // -> Context Activities
            // --> Domain Activity
            Activity domainActivity = domainATC.findInstancesInStatement(stmt).get(0);
            Assert.assertEquals(activityDomain.getId().toString(), domainActivity.getId().toString());
            // --> Domain Session Activity
            Activity domainSessionActivity = domainSessionATC.findInstancesInStatement(stmt).get(0);
            Assert.assertEquals(activityDomainSession.getId().toString(), domainSessionActivity.getId().toString());
            // --> Course Record Activity
            Activity courseRecordActivity = courseRecordATC.findInstancesInStatement(stmt).get(0);
            Assert.assertEquals(activityCourseRecord.getId().toString(), courseRecordActivity.getId().toString());
            // --> Training App Activity
            Activity trainingAppActivity = trainingAppATC.findInstancesInStatement(stmt).get(0);
            Assert.assertEquals(activityTrainingApp.getId().toString(), trainingAppActivity.getId().toString());
            // --> Registration
            UUID reg = stmt.getContext().getRegistration();
            Assert.assertEquals(registration, reg);
        }
        // Reconstruction of LMSCourseRecord from statements
        LMSCourseRecord recreated = CourseRecordHelper.deriveCourseRecord(statements, courseRecordRef, lrs.getConnectionInfo());
        // add non course concept not encoded within statement
        GradedScoreNode recreatedRoot = recreated.getRoot();
        recreatedRoot.addChild(childB);
        Assert.assertTrue("Recreated LMSCourseRecord doesn't match source LMSCourseRecord!", 
                compareLMSCourseRecords(courseRecord, recreated));
    }
    
    @Test
    public void testCourseRecordInvalidationAndReplacement() throws Exception {
        List<Statement> invalidColl = new ArrayList<Statement>();
        List<Statement> updateNoVoids = new ArrayList<Statement>();
        Agent agentActor = PersonaHelper.createMboxAgent(studentUsername);
        for(Statement stmt : updatedStatements) {
            StatementTarget target = stmt.getObject();
            if(target.getObjectType().equalsIgnoreCase("statementref")) { 
                mimicStatementVoid(statements, stmt, invalidColl);
            } else {
                updateNoVoids.add(stmt);
                Map<Integer, Activity> hierarchy = nodeHierarchyCEC.parseToMap(stmt);
                String resultResponse = stmt.getResult().getResponse();
                RawScoreNode stmtRSN = CourseRecordHelper.parseRsnFromStatement(stmt);
                Activity obj = (Activity) target;
                if(AbstractGiftActivity.isSameActivityId(obj, activityChildA)) {
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyChildA));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(responseChildA));
                    if(CourseRecordHelper.isSameNode(stmtRSN, childARawScoreUpdated)) {
                        Group actor = (Group) stmt.getActor();
                        Assert.assertEquals(2, actor.getMembers().size());
                        Assert.assertTrue("Statement's Actor was not the expected Group!",
                                CourseRecordHelper.createActorFromRawScoreNode(stmtRSN).equals(actor));
                    } else if(CourseRecordHelper.isSameNode(stmtRSN, childARawScoreSecondUpdated)) {
                        Agent agent = PersonaHelper.createMboxAgent(teamMemberUsername);
                        Assert.assertEquals(agent, stmt.getActor());
                    } else {
                        Assert.assertTrue("Unexpected RawScoreNode for Statement with activityChildA object!", false);
                    }
                } else if(AbstractGiftActivity.isSameActivityId(obj, activityGreatGrandChildA)) {
                    Assert.assertEquals(agentActor, stmt.getActor());
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyGreatGrandChildA));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(updatedResonseGreatGrandChildA));
                    // Above Expectation -> At Expectation
                    Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                            CourseRecordHelper.isSameNode(stmtRSN, greatGrandChildARawScoreUpdated));
                } else if(AbstractGiftActivity.isSameActivityId(obj, activityGrandChildB)) {
                    // novel GradedScoreNode w/ novel RawScoreNode
                    Group actor = (Group) stmt.getActor();
                    Assert.assertEquals(2, actor.getMembers().size());
                    Assert.assertTrue("Statement's Actor was not the expected Group!",
                            CourseRecordHelper.createActorFromRawScoreNode(stmtRSN).equals(actor));
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyGrandChildB));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(responseGrandChildB));
                    Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                            CourseRecordHelper.isSameNode(stmtRSN, grandChildBRawScore));
                } else if(AbstractGiftActivity.isSameActivityId(obj, activityTaskScoreNodeA)) {
                    Assert.assertEquals(agentActor, stmt.getActor());
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeA));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(responseTaskA));
                    Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                            CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeARawScore));
                    List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                    nodes.add(updatedTaskScoreNodeA);
                    comparePcTask(stmt, nodes);
                } else if(AbstractGiftActivity.isSameActivityId(obj, activityTaskScoreNodeB)) {
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeB));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(updatedResponseTaskB));
                    List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                    nodes.add(taskScoreNodeB);
                    comparePcTask(stmt, nodes);
                    // At -> Below Expectation for Group and Individuals
                    if(CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeBRawScoreUpdated)) {
                        Group actor = (Group) stmt.getActor();
                        Assert.assertEquals(2, actor.getMembers().size());
                        Assert.assertTrue("Statement's Actor was not the expected Group!",
                                CourseRecordHelper.createActorFromRawScoreNode(stmtRSN).equals(actor));
                    } else if(CourseRecordHelper.isSameNode(stmtRSN, TaskScoreNodeBIndividualAUpdated)) {
                        Assert.assertEquals(agentActor, stmt.getActor());
                    } else if(CourseRecordHelper.isSameNode(stmtRSN, TaskScoreNodeBIndividualBUpdated)) {
                        Agent agent = PersonaHelper.createMboxAgent(teamMemberUsername);
                        Assert.assertEquals(agent, stmt.getActor());
                    } else {
                        Assert.assertTrue("Unexpected RawScoreNode for Statement with TaskScoreNodeB object!", false); 
                    }
                } else if(AbstractGiftActivity.isSameActivityId(obj, activityTaskScoreNodeC)) {
                    Assert.assertEquals(agentActor, stmt.getActor());
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeC));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(responseTaskC));
                    Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                            CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeCRawScoreUpdated));
                    List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                    nodes.add(taskScoreNodeB);
                    nodes.add(taskScoreNodeC);
                    comparePcTask(stmt, nodes);
                } else if (AbstractGiftActivity.isSameActivityId(obj, activityTaskScoreNodeD)) {
                    Assert.assertEquals(agentActor, stmt.getActor());
                    Assert.assertTrue("Statement's Node Hierarchy Context Extension was not the expected hierarchy!", 
                            areHierarchiesSame(hierarchy, hierarchyTaskScoreNodeD));
                    Assert.assertTrue("Statement's Result Response was not the expected responses!", 
                            resultResponse.equals(responseTaskD));
                    Assert.assertTrue("Statement's Result Formative Competency Set was not the expected Raw Score Node!", 
                            CourseRecordHelper.isSameNode(stmtRSN, taskScoreNodeDRawScore));
                    List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
                    nodes.add(taskScoreNodeD);
                    comparePcTask(stmt, nodes);
                } else {
                    Assert.assertTrue("Statement Object was not one of the expected Graded Score Node Activities!",
                            false);
                }
                // Common Context
                // -> Context Activities
                // --> Domain Activity
                Activity domainActivity = domainATC.findInstancesInStatement(stmt).get(0);
                Assert.assertEquals(activityDomain.getId().toString(), domainActivity.getId().toString());
                // --> Domain Session Activity
                Activity domainSessionActivity = domainSessionATC.findInstancesInStatement(stmt).get(0);
                Assert.assertEquals(activityDomainSession.getId().toString(), domainSessionActivity.getId().toString());
                // --> Course Record Activity
                Activity courseRecordActivity = courseRecordATC.findInstancesInStatement(stmt).get(0);
                Assert.assertEquals(activityCourseRecord.getId().toString(), courseRecordActivity.getId().toString());
                // --> Training App Activity
                Activity trainingAppActivity = trainingAppATC.findInstancesInStatement(stmt).get(0);
                Assert.assertEquals(activityTrainingApp.getId().toString(), trainingAppActivity.getId().toString());
                // --> Registration
                UUID reg = stmt.getContext().getRegistration();
                Assert.assertEquals(registration, reg);
            }
        }
        // Assert Size of Updated after removal of void statements
        Assert.assertEquals(10, updateNoVoids.size());
        // Assert Size of dlq
        Assert.assertEquals(10, invalidColl.size());
        // Assert Size of statements after removal of out-dated statements
        Assert.assertEquals(1, statements.size());
        // Ensure expected statements make it into DLQ
        for(Statement stmt : invalidColl) {
            Activity stmtObject = (Activity) stmt.getObject();
            boolean expectedId =
                    // Associated users changes
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityChildA) ||
                    // Assessment Level changes
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityGreatGrandChildA) ||
                    // Not found within updated course record
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityChildC) ||
                    // Not found within updated course record
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityGrandChildC) ||
                    // Stress / Difficulty value changes
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityTaskScoreNodeA) ||
                    // Assessment Level + value changes (Group + Individuals)
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityTaskScoreNodeB) ||
                    // Assessment group changes
                    AbstractGiftActivity.isSameActivityId(stmtObject, activityTaskScoreNodeC);
            Assert.assertTrue("Object id was not one of the expected out-dated object ids!", expectedId);
        }
        // Reconstruction of LMSCourseRecord from relevantStatements
        List<Statement> relevantStatements = new ArrayList<Statement>(statements);
        relevantStatements.addAll(updateNoVoids);
        LMSCourseRecord recreated = CourseRecordHelper.deriveCourseRecord(relevantStatements, courseRecordRef, lrs.getConnectionInfo());
        Assert.assertTrue("Recreated LMSCourseRecord doesn't match source LMSCourseRecord!", 
                compareLMSCourseRecords(updatedCourseRecord, recreated));
    }
    
    @Test
    public void testCourseRecordReferenceUpdate() throws Exception {
        // setup
        CourseRecordRef originalRef = new CourseRecordRef();
        UUIDCourseRecordRefIds originalRefIds = new UUIDCourseRecordRefIds();
        CourseRecordRef updatedRef = new CourseRecordRef();
        UUIDCourseRecordRefIds updatedRefIds = new UUIDCourseRecordRefIds();
        String nonOutdatedIdA, nonOutdatedIdB, outdatedIdA, replacementIdA, novelIdA;
        // statement ids
        nonOutdatedIdA = "43c8e708-ddcb-4f27-94ea-127c2863bd8f";
        originalRefIds.addRecordUUID(nonOutdatedIdA);
        nonOutdatedIdB = "ce1bb8f2-7032-48a1-ab23-41c094360807";
        originalRefIds.addRecordUUID(nonOutdatedIdB);
        outdatedIdA = "3a2ca90e-b9e1-4b4a-a0e0-de3c76d84345";
        originalRefIds.addRecordUUID(outdatedIdA);
        replacementIdA = "6bc6258f-f5c5-433b-96ed-7cef9830a0bf";
        novelIdA = "01bdbaa0-4aee-41da-9dad-d17684c2ff10";
        // update refs
        originalRef.setRef(originalRefIds);
        updatedRef.setRef(updatedRefIds);
        // moc statements
        List<Statement> statements = new ArrayList<Statement>();
        VoidedStatement voidStmt = new VoidedStatement(UUID.fromString(outdatedIdA));
        statements.add(voidStmt);
        Statement replacementStmt = new Statement();
        replacementStmt.setId(UUID.fromString(replacementIdA));
        statements.add(replacementStmt);
        Statement novelStmt = new Statement();
        novelStmt.setId(UUID.fromString(novelIdA));
        statements.add(novelStmt);
        // perform update of updatedRef + test contained statement identifiers
        CourseRecordHelper.updateCourseRecordRef(originalRef, updatedRef, statements);
        ArrayList<String> updatedIdColl = ((UUIDCourseRecordRefIds) updatedRef.getRef()).getRecordUUIDs();
        Assert.assertTrue(updatedIdColl.contains(nonOutdatedIdA));
        Assert.assertTrue(updatedIdColl.contains(nonOutdatedIdB));
        Assert.assertFalse(updatedIdColl.contains(outdatedIdA));
        Assert.assertTrue(updatedIdColl.contains(replacementIdA));
        Assert.assertTrue(updatedIdColl.contains(novelIdA));
        ArrayList<String> originalIdColl = ((UUIDCourseRecordRefIds) originalRef.getRef()).getRecordUUIDs();
        Assert.assertTrue(originalIdColl.contains(nonOutdatedIdA));
        Assert.assertTrue(originalIdColl.contains(nonOutdatedIdB));
        Assert.assertTrue(originalIdColl.contains(outdatedIdA));
        Assert.assertFalse(originalIdColl.contains(replacementIdA));
        Assert.assertFalse(originalIdColl.contains(novelIdA));
    }
}
