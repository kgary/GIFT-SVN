package mil.arl.gift.lms.impl.lrs;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Group;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AbstractGiftActivity;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.UUIDCourseRecordRefIds;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.append.AffectiveAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ChainOfCustodyAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.CognitiveAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ConceptAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.CourseRecordAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.EvaluatorAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.IntermediateConceptAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ParentIntermediateConceptAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ParentTaskAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ProfileReferenceAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ReplacedStatementIdAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TaskAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TeamAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.PerformanceCharacteristicsAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.DemonstratedPerformanceTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.VoidedStatement;

public class AppendersTest extends TestUtils {
    // Appenders
    private AffectiveAppender affectiveAppender;
    private CognitiveAppender cognitiveAppender;
    private ConceptAppender conceptAppender;
    private CourseRecordAppender courseRecordAppender, uuidCourseRecordAppender;
    private DomainAppender domainAppender;
    private DomainSessionAppender domainSessionAppender;
    private EvaluatorAppender evaluatorAppender;
    private IntermediateConceptAppender icAppender;
    private KnowledgeSessionTypeAppender knowledgeSessionTypeAppender;
    private ParentIntermediateConceptAppender parentIcAppender;
    private ParentTaskAppender parentTaskAppender;
    private ProfileReferenceAppender profileRefAppender;
    private ReplacedStatementIdAppender replacementStmtIdAppender;
    private TaskAppender taskAppender;
    private ChainOfCustodyAppender chainOfCustodyAppender;
    private PerformanceCharacteristicsAppender pcAppender;
    // Team Appenders for Assessed Assessment, Demonstrated Performance and Demonstrated Performance with Assessed Team Org Entitites
    private TeamAppender teamAppenderAA, teamAppenderDP, teamAppenderATOE;
    // data
    private PerformanceStateAttribute psaOne, psaTwo, psaThree;
    private ConceptPerformanceState conceptPerfState;
    private IntermediateConceptPerformanceState icPerfState;
    private TaskPerformanceState taskPerfState;
    private Integer courseRecordId = 27;
    private UUID recordRefUUID, recordRefUUID2;
    private CourseRecordRef courseRecordRef, courseRecordRefUUID;
    private String compUUIDs;
    private UUID revision;
    // generic stmt, Assessed Assessment stmt, Demonstrated Performance stmt, Demonstrated Performance with Assessed Team Org Entities stmt
    private AbstractGiftStatement stmt, stmtAA, stmtDP, stmtATOE;
    private List<String> refUUIDs;
    // profile components
    private ItsActivityTypeConcepts.TeamEchelon echelonActivityType;
    private ItsActivityTypeConcepts.TeamRole teamRoleATC;
    private ItsContextExtensionConcepts.TeamStructure teamStructureCEC;
    private ItsContextExtensionConcepts.ChainOfCustody chainOfCustodyCEC;
    private ItsContextExtensionConcepts.PerformanceCharacteristics pcCEC;
    
    @Before
    public void setup() throws Exception {
        // Canonical Activity Appenders
        affectiveAppender = new AffectiveAppender();
        cognitiveAppender = new CognitiveAppender();
        // Performance State
        psaOne = 
                new PerformanceStateAttribute("psa 1", 1, "65e58ad3-0b06-4df6-8bcd-4b51fe62c7b8",
                        // shortTerm
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                        // longTerm
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                        // predicted
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        conceptPerfState = new ConceptPerformanceState(psaOne);
        conceptAppender = new ConceptAppender(conceptPerfState);
        
        psaTwo = 
                new PerformanceStateAttribute("psa 2", 2, "65e58ad3-0b06-4df6-8bcd-4b51fe62c7b6",
                        // shortTerm
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                        // longTerm
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                        // predicted
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        List<ConceptPerformanceState> concepts = new ArrayList<ConceptPerformanceState>();
        concepts.add(conceptPerfState);
        icPerfState = new IntermediateConceptPerformanceState(psaTwo, concepts);
        icAppender = new IntermediateConceptAppender(icPerfState);
        parentIcAppender = new ParentIntermediateConceptAppender(icPerfState);
        
        psaThree = 
                new PerformanceStateAttribute("psa 3", 3, "65e58ad3-0b06-4df6-8bcd-4b51fe62c7b2",
                        // shortTerm
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                        // longTerm
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"),
                        // predicted
                        AssessmentLevelEnum.UNKNOWN, Long.parseLong("1608153882035"));
        // Appenders
        taskPerfState = new TaskPerformanceState(psaThree, concepts);
        parentTaskAppender = new ParentTaskAppender(taskPerfState);
        taskAppender = new TaskAppender(taskPerfState);
        // record ref appenders
        // -> int
        courseRecordRef = CourseRecordRef.buildCourseRecordRefFromInt(courseRecordId);
        courseRecordAppender = new CourseRecordAppender(courseRecordRef);
        // -> uuids
        recordRefUUID = UUID.fromString("edd1c7c6-231c-486f-ac71-e2d084f5d2b5");
        recordRefUUID2 = UUID.fromString("7adc613f-f3f1-422f-9ad5-f40bce223560");
        courseRecordRefUUID = new CourseRecordRef();
        courseRecordRefUUID.setRef(new UUIDCourseRecordRefIds());
        UUIDCourseRecordRefIds ref = ((UUIDCourseRecordRefIds) courseRecordRefUUID.getRef());
        ref.addRecordUUID(recordRefUUID.toString());
        ref.addRecordUUID(recordRefUUID2.toString());
        refUUIDs = ref.getRecordUUIDs();
        String [] coll = new String[refUUIDs.size()];
        coll = refUUIDs.toArray(coll);
        compUUIDs = StringUtils.join(coll, CommonLrsEnum.SEPERATOR_COMMA.getValue());
        uuidCourseRecordAppender = new CourseRecordAppender(courseRecordRefUUID);
        // simple appenders
        domainAppender = new DomainAppender(domainName);
        domainSessionAppender = new DomainSessionAppender(studentDomainSessionId);
        evaluatorAppender = new EvaluatorAppender(evaluatorUsername);
        knowledgeSessionTypeAppender = new KnowledgeSessionTypeAppender(SessionType.ACTIVE_PLAYBACK);
        profileRefAppender = new ProfileReferenceAppender(DemonstratedPerformanceTemplate.VoidingTemplate.getInstance());
        revision = UUID.fromString("65e58ad3-0b06-4df6-8bcd-4b51fe62c7b8");
        replacementStmtIdAppender = new ReplacedStatementIdAppender(revision);
        Map<Integer, TaskPerformanceState> tasks = new HashMap<Integer, TaskPerformanceState>();
        tasks.put(1, taskPerfState);
        PerformanceState performanceState = new PerformanceState(tasks);
        performanceState.setObserverMedia(observerMedia);
        chainOfCustodyAppender = new ChainOfCustodyAppender(studentDomainSessionId, studentId);
        // Simple statement with nothing appended
        stmt = new VoidedStatement(UUID.randomUUID());
        // Appenders appending
        affectiveAppender.appendToStatement(stmt);
        cognitiveAppender.appendToStatement(stmt);
        conceptAppender.appendToStatement(stmt);
        courseRecordAppender.appendToStatement(stmt);
        domainAppender.appendToStatement(stmt);
        domainSessionAppender.appendToStatement(stmt);
        evaluatorAppender.appendToStatement(stmt);
        icAppender.appendToStatement(stmt);
        knowledgeSessionTypeAppender.appendToStatement(stmt);
        parentIcAppender.appendToStatement(stmt);
        parentTaskAppender.appendToStatement(stmt);
        profileRefAppender.appendToStatement(stmt);
        replacementStmtIdAppender.appendToStatement(stmt);
        taskAppender.appendToStatement(stmt);
        uuidCourseRecordAppender.appendToStatement(stmt);
        chainOfCustodyAppender.appendToStatement(stmt);
        // Profile Components
        echelonActivityType = ItsActivityTypeConcepts.TeamEchelon.getInstance();
        teamRoleATC = ItsActivityTypeConcepts.TeamRole.getInstance();
        teamStructureCEC = ItsContextExtensionConcepts.TeamStructure.getInstance();
        chainOfCustodyCEC = ItsContextExtensionConcepts.ChainOfCustody.getInstance();
        pcCEC = ItsContextExtensionConcepts.PerformanceCharacteristics.getInstance();
    }
    
    @Test
    public void testAppenders() throws Exception {
        Context ctx = stmt.getContext();
        // Registration
        Assert.assertEquals(UUIDHelper.createUUIDFromData(studentDomainSessionId.toString()), ctx.getRegistration());
        // Instructor
        Assert.assertEquals(PersonaHelper.createMboxIFI(evaluatorUsername), ctx.getInstructor().getMbox());
        // Revision
        Assert.assertEquals(ctx.getRevision(), revision.toString());
        // Parent Context Activities
        List<Activity> parent = ContextActivitiesHelper.getParentActivities(ctx);
        List<String> parentIdColl = new ArrayList<String>();
        for(Activity a : parent) {
            parentIdColl.add(a.getId().toString());
        }
        Assert.assertTrue(parentIdColl.contains("activityId:uri/its/test_course.xml"));
        Assert.assertTrue(parentIdColl.contains("activityId:uri/its/course.concept/psa+2"));
        Assert.assertTrue(parentIdColl.contains("activityId:uri/its/course.concept/psa+3"));
        // Grouping Context Activities
        List<Activity> grouping = ContextActivitiesHelper.getGroupingActivities(ctx);
        List<String> groupingIdColl = new ArrayList<String>();
        
        for(Activity a : grouping) {
            if(a.getId().toString() == "activityId:uri/its/course.record/b47412ee-2b87-30a7-8da1-cd07b94231cf") {
                Assert.assertEquals(AbstractGiftActivity.parseActivityName(a), "b47412ee-2b87-30a7-8da1-cd07b94231cf");
                Assert.assertEquals(AbstractGiftActivity.parseActivityDescription(a), compUUIDs);
            }
            groupingIdColl.add(a.getId().toString());
        }
        Assert.assertTrue(groupingIdColl.contains("activityId:uri/its/course.record/27"));
        Assert.assertTrue(groupingIdColl.contains("activityId:uri/its/domain.session/1"));
        // Category Context Activities
        List<Activity> category = ContextActivitiesHelper.getCategoryActivities(ctx);
        List<String> categoryIdColl = new ArrayList<String>();
        for(Activity a : category) {
            categoryIdColl.add(a.getId().toString());
        }
        Assert.assertTrue(categoryIdColl.contains("activityId:uri/its/learner.state.affective"));
        Assert.assertTrue(categoryIdColl.contains("activityId:uri/its/learner.state.cognitive"));
        Assert.assertTrue(categoryIdColl.contains("activityId:uri/its/knowledge.session.playback/active.playback"));
        Assert.assertTrue(categoryIdColl.contains("https://xapinet.org/xapi/stetmt/its/v0.0.1"));
        // Other Context Activities
        List<Activity> other = ContextActivitiesHelper.getOtherActivities(ctx);
        List<String> otherIdColl = new ArrayList<String>();
        for(Activity a : other) {
            otherIdColl.add(a.getId().toString());
        }
        Assert.assertTrue(otherIdColl.contains("activityId:uri/its/course.concept/psa+1")); 
        Assert.assertTrue(otherIdColl.contains("activityId:uri/its/course.concept/psa+2"));
        Assert.assertTrue(otherIdColl.contains("activityId:uri/its/course.concept/psa+3"));
    }
    
    @Test
    public void testAssessedAssessmentTeamAppender() throws Exception {
        Set<String> relevantUsernames = new HashSet<String>();
        relevantUsernames.add(studentUsername);
        relevantUsernames.add(teamMemberUsername);
        teamAppenderAA = new TeamAppender(SessionManager.getInstance().getKnowledgeSession(), false, relevantUsernames);
        stmtAA = new VoidedStatement(UUID.randomUUID());
        teamAppenderAA.appendToStatement(stmtAA);
        // Context
        Context ctx = stmtAA.getContext();
        // -> Team
        Group team = (Group) ctx.getTeam();
        Assert.assertEquals(team.getName(), anonGroupName);
        List<Agent> members = team.getMembers();
        List<Agent> expectedMembers = new ArrayList<Agent>();
        expectedMembers.add(PersonaHelper.createMboxAgent(studentUsername));
        expectedMembers.add(PersonaHelper.createMboxAgent(teamMemberUsername));
        Assert.assertTrue(members.containsAll(expectedMembers));
        // -> contextActivities
        // --> Grouping
        List<Activity> grouping = ContextActivitiesHelper.getGroupingActivities(ctx);
        List<URI> groupingATs = new ArrayList<URI>();
        List<String> groupingIds = new ArrayList<String>();
        for(Activity g : grouping) {
            groupingIds.add(g.getId().toString());
            groupingATs.add(g.getDefinition().getType());
        }
        Assert.assertTrue(groupingATs.contains(echelonActivityType.asActivityType()));
        Assert.assertTrue(groupingIds.contains("activityId:uri/its/echelon/army/fireteam"));
        // --> Category
        List<Activity> category = ContextActivitiesHelper.getCategoryActivities(ctx);
        List<URI> categoryATs = new ArrayList<URI>();
        List<String> categoryIds = new ArrayList<String>();
        for(Activity c : category) {
            categoryIds.add(c.getId().toString());
            if(c.getDefinition() != null) {
                categoryATs.add(c.getDefinition().getType());
            }
        }
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/member_role"));
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/host_role"));
        Assert.assertTrue(categoryATs.contains(teamRoleATC.asActivityType()));
        // -> Extensions
        JsonNode teamStructureExt = teamStructureCEC.parseFromExtensions(ctx.getExtensions());
        for(int i = 0; i < teamStructureExt.size(); i++) {
            JsonNode node = teamStructureExt.get(i);
            if(i == 0) {
                validateParentTeam(node);
            }
            if(i == 1) {
                validateBranchSubTeam(node);
            }
        }
    }
    
    @Test
    public void testDemonstratedPerfomanceTeamAppender() throws Exception {
        Set<String> relevantUsernames = new HashSet<String>(1);
        relevantUsernames.add(studentUsername);
        teamAppenderDP = new TeamAppender(SessionManager.getInstance().getKnowledgeSession(), false, relevantUsernames);
        stmtDP = new VoidedStatement(UUID.randomUUID());
        teamAppenderDP.appendToStatement(stmtDP);
        // Context
        Context ctx = stmtDP.getContext();
        // -> contextActivities
        // --> Grouping
        List<Activity> grouping = ContextActivitiesHelper.getGroupingActivities(ctx);
        List<URI> groupingATs = new ArrayList<URI>();
        List<String> groupingIds = new ArrayList<String>();
        for(Activity g : grouping) {
            groupingIds.add(g.getId().toString());
            groupingATs.add(g.getDefinition().getType());
        }
        Assert.assertTrue(groupingIds.contains("activityId:uri/its/echelon/army/fireteam"));
        Assert.assertTrue(groupingATs.contains(echelonActivityType.asActivityType()));
        // --> Category
        List<Activity> category = ContextActivitiesHelper.getCategoryActivities(ctx);
        List<URI> categoryATs = new ArrayList<URI>();
        List<String> categoryIds = new ArrayList<String>();
        for(Activity c : category) {
            categoryIds.add(c.getId().toString());
            if(c.getDefinition() != null) {
                categoryATs.add(c.getDefinition().getType());
            }
        }
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/host_role"));
        Assert.assertTrue(categoryATs.contains(teamRoleATC.asActivityType()));
        // -> Extensions
        // --> Team Structure
        JsonNode teamStructureExt = teamStructureCEC.parseFromExtensions(ctx.getExtensions());
        for(int i = 0; i < teamStructureExt.size(); i++) {
            JsonNode node = teamStructureExt.get(i);
            if(i == 0) {
                validateParentTeam(node);
            }
            if(i == 1) {
                validateBranchSubTeam(node);
            }
        }
    }
    
    @Test
    public void testAssessedTeamOrgEntitiesTeamAppender() throws Exception {
        Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = new HashMap<String, AssessmentLevelEnum>();
        assessedTeamOrgEntities.put(teamMemberRole, AssessmentLevelEnum.AT_EXPECTATION);
        conceptPerfState.getState().setAssessedTeamOrgEntities(assessedTeamOrgEntities);
        List<String> usernames = new ArrayList<String>(1);
        usernames.add(studentUsername);
        teamAppenderATOE = new TeamAppender(SessionManager.getInstance().getKnowledgeSession(), false, usernames, conceptPerfState);
        stmtATOE = new VoidedStatement(UUID.randomUUID());
        teamAppenderATOE.appendToStatement(stmtATOE);
        // Context
        Context ctx = stmtATOE.getContext();
        // -> contextActivities
        // --> Grouping
        List<Activity> grouping = ContextActivitiesHelper.getGroupingActivities(ctx);
        List<URI> groupingATs = new ArrayList<URI>();
        List<String> groupingIds = new ArrayList<String>();
        for(Activity g : grouping) {
            groupingIds.add(g.getId().toString());
            groupingATs.add(g.getDefinition().getType());
        }
        Assert.assertTrue(groupingIds.contains("activityId:uri/its/echelon/army/fireteam"));
        Assert.assertTrue(groupingATs.contains(echelonActivityType.asActivityType()));
        // --> Category
        List<Activity> category = ContextActivitiesHelper.getCategoryActivities(ctx);
        List<URI> categoryATs = new ArrayList<URI>();
        List<String> categoryIds = new ArrayList<String>();
        for(Activity c : category) {
            categoryIds.add(c.getId().toString());
            if(c.getDefinition() != null) {
                categoryATs.add(c.getDefinition().getType());
            }
        }
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/host_role"));
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/member_role"));
        Assert.assertTrue(categoryATs.contains(teamRoleATC.asActivityType()));
        // -> Extensions
        // --> Team Structure
        JsonNode teamStructureExt = teamStructureCEC.parseFromExtensions(ctx.getExtensions());
        for(int i = 0; i < teamStructureExt.size(); i++) {
            JsonNode node = teamStructureExt.get(i);
            if(i == 0) {
                validateBranchSubTeam(node);
            }
        }
    }
    
    @Test
    public void testChainOfCustodyAppender() throws Exception {
        // Actual
        JsonNode atExt = chainOfCustodyCEC.parseFromExtensions(stmt.getContext().getExtensions());
        // expected
        String domainSessionLogsPath = chainOfCustodyAppender.createDomainSessionsPath();
        List<FileProxy> found = chainOfCustodyAppender.locateFiles(domainSessionLogsPath);
        Set<String> dkfFileNames = new HashSet<String>();
        Set<String> logFileNames = new HashSet<String>();
        chainOfCustodyAppender.populateFileColls(found, dkfFileNames, logFileNames);
        JsonNode expected = chainOfCustodyCEC.createExtensionItem(domainSessionLogsPath, dkfFileNames, logFileNames);
        Assert.assertEquals(expected, atExt);
    }
    
    @SuppressWarnings("null")
    private void comparePC(AbstractGiftStatement statement, List<Object> items) throws Exception {
        JsonNode ext = pcCEC.parseFromExtensions(statement.getContext().getExtensions());
        JsonNode tasks = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.TASKS.getValue());
        JsonNode conditions = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.CONDITIONS.getValue());
        int expExtSize, actSize, taskSize, conditionSize, taskCursor, conditionCusor;
        actSize = 0;
        taskSize = 0;
        if(tasks != null) {
            taskSize = tasks.size();
        }
        conditionSize = 0;
        if(conditions != null) {
            conditionSize = conditions.size();
        }
        expExtSize = taskSize + conditionSize;
        taskCursor = 0;
        conditionCusor = 0;
        for(int i = 0; i < items.size(); i++) {
           Object item = items.get(i);
           if(item instanceof EnvironmentControl) {
               if(((EnvironmentControl) item).getStress() != null) {
                   comparePcCondition(conditions.get(conditionCusor), (EnvironmentControl) item);
                   conditionCusor++;
               }
           } else if(item instanceof TaskScoreNode) {
               comparePcTask(tasks.get(taskCursor), (TaskScoreNode) item);
               taskCursor++;
           } else if(item instanceof TaskPerformanceState) {
               comparePcTask(tasks.get(taskCursor), (TaskPerformanceState) item);
               taskCursor++;
           }  
        }
        actSize = taskCursor + conditionCusor;
        Assert.assertEquals(actSize, expExtSize);
        Assert.assertEquals(conditionCusor, conditionSize);
        Assert.assertEquals(taskCursor, taskSize);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskScoreNode node) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(node);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, EnvironmentControl envControl) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(envControl);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskPerformanceState taskState) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(taskState);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskScoreNode node, EnvironmentControl envControl) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(node);
        items.add(envControl);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskScoreNode node, TaskPerformanceState taskState) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(node);
        items.add(taskState);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskPerformanceState taskState, EnvironmentControl envControl) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(taskState);
        items.add(envControl);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskScoreNode node, TaskPerformanceState taskState, EnvironmentControl envControl) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(node);
        items.add(taskState);
        items.add(envControl);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, TaskScoreNode node, List<EnvironmentControl> envControls) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(node);
        items.addAll(envControls);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, List<TaskScoreNode> nodes, EnvironmentControl envControl) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.add(envControl);
        items.addAll(nodes);
        comparePC(statement, items);
    }
    
    private void comparePC(AbstractGiftStatement statement, List<TaskScoreNode> nodes, List<EnvironmentControl> envControls) throws Exception {
        List<Object> items = new ArrayList<Object>();
        items.addAll(envControls);
        items.addAll(nodes);
        comparePC(statement, items);
    }
    
    @Test
    public void testPerformanceCharacteristicsAppender() throws Exception {
        TaskScoreNode taskNodeA, taskNodeB, taskNodeC;
        TaskPerformanceState taskStateA, taskStateB;
        PerformanceStateAttribute stateA, stateB;
        List<ConceptPerformanceState> conceptStates = new ArrayList<ConceptPerformanceState>();
        EnvironmentControl envControlA, envControlB;
        String nodeNameA, nodeNameB, nodeNameC, stressReasonA, difficultyReasonA;
        Double stressA, difficultyA, stressB, difficultyB;
        nodeNameA = "node a";
        nodeNameB = "node b";
        nodeNameC = "node c";
        stressReasonA = "stress reason a";
        difficultyReasonA = "difficulty reason a";
        stressA = Double.valueOf(0.5);
        difficultyA = Double.valueOf(2.0);
        stressB = Double.valueOf(1.0);
        difficultyB = Double.valueOf(3.0);
        taskNodeA = new TaskScoreNode(nodeNameA);
        taskNodeA.setStress(stressA);
        taskNodeA.setStressReason(stressReasonA);
        taskNodeA.setDifficulty(difficultyA);
        taskNodeA.setDifficultyReason(difficultyReasonA);
        taskNodeB = new TaskScoreNode(nodeNameB);
        taskNodeB.setStress(stressB);
        taskNodeB.setDifficulty(difficultyB);
        taskNodeC = new TaskScoreNode(nodeNameC);
        EnvironmentAdaptation.Overcast overcast = new EnvironmentAdaptation.Overcast();
        EnvironmentAdaptation adaptationA = new EnvironmentAdaptation();
        adaptationA.setType(overcast);
        EnvironmentAdaptation adaptationB = new EnvironmentAdaptation();
        adaptationB.setType(overcast);
        envControlA = new EnvironmentControl(adaptationA);
        envControlA.setStress(stressA);
        envControlB = new EnvironmentControl(adaptationB);
        // constructor permutations
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(taskNodeA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskNodeA);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addEnvironmentControl(envControlA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, envControlA);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(taskNodeA);
        pcAppender.addEnvironmentControl(envControlA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskNodeA, envControlA);
        List<EnvironmentControl> envControls = new ArrayList<EnvironmentControl>();
        envControls.add(envControlA);
        envControls.add(envControlB);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(taskNodeA);
        pcAppender.addEnvironmentControl(envControls);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskNodeA, envControls);
        stmt = new VoidedStatement(UUID.randomUUID());
        List<TaskScoreNode> nodes = new ArrayList<TaskScoreNode>();
        nodes.add(taskNodeA);
        nodes.add(taskNodeB);
        nodes.add(taskNodeC);
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(nodes);
        pcAppender.addEnvironmentControl(envControlB);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, nodes, envControlB);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(nodes);
        pcAppender.addEnvironmentControl(envControls);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, nodes, envControls);
        stateA = new PerformanceStateAttribute("stateA", 1, nodeNameA, 
                AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION);
        taskStateA = new TaskPerformanceState(stateA, conceptStates);
        taskStateA.setStress(stressA);
        taskStateA.setDifficulty(difficultyA);
        taskStateA.setStressReason(stressReasonA);
        taskStateA.setDifficultyReason(difficultyReasonA);
        stateB = new PerformanceStateAttribute("stateB", 2, nodeNameB,
                AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION, AssessmentLevelEnum.AT_EXPECTATION);
        taskStateB = new TaskPerformanceState(stateB, conceptStates);
        taskStateB.setStress(stressB);
        taskStateB.setDifficulty(difficultyB);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskPerformanceState(taskStateA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskStateA);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(taskNodeA);
        pcAppender.addTaskPerformanceState(taskStateA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskNodeA, taskStateA);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskPerformanceState(taskStateA);
        pcAppender.addEnvironmentControl(envControlA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskStateA, envControlA);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(taskNodeA);
        pcAppender.addTaskPerformanceState(taskStateA);
        pcAppender.addEnvironmentControl(envControlA);
        pcAppender.appendToStatement(stmt);
        comparePC(stmt, taskNodeA, taskStateA, envControlA);
        List<TaskPerformanceState> states = new ArrayList<TaskPerformanceState>();
        states.add(taskStateA);
        states.add(taskStateB);
        stmt = new VoidedStatement(UUID.randomUUID());
        pcAppender = new PerformanceCharacteristicsAppender();
        pcAppender.addTaskScoreNode(nodes);
        pcAppender.addTaskPerformanceState(states);
        pcAppender.addEnvironmentControl(envControls);
        pcAppender.appendToStatement(stmt);
        List<Object> items = new ArrayList<Object>();
        items.addAll(nodes);
        items.addAll(states);
        items.addAll(envControls);
        comparePC(stmt, items);
    }
}
