package mil.arl.gift.lms.impl.lrs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementRef;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.KnowledgeSessionCourseInfo;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.ObserverControls;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.SessionMember.IndividualMembership;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.StrategyUtil;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AbstractGiftActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EchelonActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EnvironmentAdaptationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.TeamRoleActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts.extensionObjectKeys;

public class TestUtils {
    // Lrs
    protected Lrs lrs;
    // Test student
    protected String studentUsername = "student";
    protected Integer studentId = 1;
    protected Integer studentDomainSessionId = 1;
    protected UserSession studentUserSession;
    protected DomainSession studentDomainSession;
    protected SessionMember studentSessionMember;
    protected String studentHostRole = "host_role";
    // Test Team Member
    protected String teamMemberUsername = "team_member";
    protected Integer teamMemberId = 2;
    protected Integer teamMemberDomainSessionId = 2;
    protected UserSession teamMemberUserSession;
    protected DomainSession teamMemberDomainSession;
    protected SessionMember teamMemberSessionMember;
    protected String teamMemberRole = "member_role";
    // Evaluator
    protected String evaluatorUsername = "evaluator";
    protected Integer evaluatorId = 3;
    protected Integer evaluatorDomainSessionId = 3;
    protected String evaluatorRole = "oct_role";
    protected String observerComment = "comment";
    protected String observerMedia = "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav";
    // Course
    protected String domainName = "test_course.xml";
    protected String courseSourceId = "test course source id";
    // Knowledge Session
    protected TeamKnowledgeSession teamKnowledgeSession;
    protected String knowledgeSessionName = "test session";
    protected String scenarioDesc = "test description";
    protected int totalPossibleTeamMembers = 2;
    protected Map<Integer, SessionMember> joinedMembers;
    protected List<String> roles;
    protected TrainingApplicationEnum trainingAppType = TrainingApplicationEnum.VBS;
    protected SessionType sessionType = SessionType.ACTIVE;
    protected long sessionStartTime = 1624300700000L;
    protected KnowledgeSessionCourseInfo courseInfo;
    // Team
    protected Team team;
    protected String teamName = "test team";
    protected EchelonEnum teamEchelon = EchelonEnum.FIRETEAM;
    protected List<AbstractTeamUnit> teamUnits;
    // -> Subteam
    protected Team subteam;
    protected String subTeamName = "subteam";
    protected List<AbstractTeamUnit> subTeamUnits;
    // Non playable Team Member
    protected String nonPlayableMemberRole = "non_playable";
    // Not Assigned Team Member
    protected String notAssignedMemberRole = "not_assigned";
    // -> Subsubteam
    protected Team subsubteam;
    protected String subsubteamName = "subsubteam";
    protected List<AbstractTeamUnit> subsubTeamUnits;
    // NPC team member
    protected String npcMemberRole = "npc";
    protected EchelonEnum subsubteamEchelon = EchelonEnum.SQUAD;
    // -> Subteam Branch
    protected Team branchSubTeam;
    protected String branchSubTeamName = "branch subteam";
    protected List<AbstractTeamUnit> branchSubTeamUnits;
    protected EchelonEnum branchSubTeamEchelon = EchelonEnum.FIRETEAM;
    // Group from Team Structure
    protected String anonGroupName = "test team|branch subteam";
    // -> node Id to Name Map
    protected Map<BigInteger, String> nodeIdToNameMap;
    protected BigInteger conceptNodeId = new BigInteger("10");
    protected String conceptName = "test concept";
    // Mission
    protected Mission mission;
    protected String missionSource = "mission source";
    protected String missionMet = "mission met";
    protected String missionTask = "mission task";
    protected String missionSituation = "mission situation";
    protected String missionGoals = "mission goals";
    protected String missionCondition = "mission condition";
    protected String missionRoe = "mission roe";
    protected String missionThreatWarning = "mission threat warning";
    protected String missionWeaponStatus = "mission weapon status";
    protected String missionWeaponPosture = "mission weapon posture";
    // Observer Controls
    protected ObserverControls observerControls;
    
    public TestUtils() {}
    
    protected UserSession createUserSession(int userId, String userName) {
        UserSession session = new UserSession(userId);
        session.setUsername(userName);
        return session;
    }
    
    private DomainSession createDomainSession(int userId, int domainSessionId, UserSession userSession) {
        DomainSession domainSession = new DomainSession(domainSessionId, userId, domainName, domainName);
        domainSession.copyFromUserSession(userSession);
        return domainSession;
    }
    
    private void updateSessionManager(UserSession userSession, DomainSession domainSession) throws Exception {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.mapIdToDomainSession(domainSession.getDomainSessionId(), domainSession);
        sessionManager.mapUserToDomainSession(userSession, domainSession);
    }
    
    protected SessionMember createSessionMember(String role, UserSession userSession, int domainSessionId) throws Exception {
        IndividualMembership member = new IndividualMembership(userSession.getUsername());
        TeamMember<?> teamMember = new MarkedTeamMember(role, role);
        member.setTeamMember(teamMember);
        return new SessionMember(member, userSession, domainSessionId);
    }
    
    private Team prepTeam() {
        // Top level team
        teamUnits = new ArrayList<AbstractTeamUnit>();
        teamUnits.add(studentSessionMember.getSessionMembership().getTeamMember());
        // Sub sub team
        subsubTeamUnits = new ArrayList<AbstractTeamUnit>();
        TeamMember<?> npcRole = new MarkedTeamMember(npcMemberRole, npcMemberRole);
        npcRole.setPlayable(false);
        subsubTeamUnits.add(npcRole);
        subsubteam = new Team(subsubteamName, subsubteamEchelon, subsubTeamUnits);
        // Sub team
        subTeamUnits = new ArrayList<AbstractTeamUnit>();
        subTeamUnits.add(subsubteam);
        TeamMember<?> nonPlayableRole = new MarkedTeamMember(nonPlayableMemberRole, nonPlayableMemberRole);
        nonPlayableRole.setPlayable(false);
        subTeamUnits.add(nonPlayableRole);
        TeamMember<?> notAssignedRole = new MarkedTeamMember(notAssignedMemberRole, notAssignedMemberRole);
        subTeamUnits.add(notAssignedRole);
        subteam = new Team(subTeamName, null, subTeamUnits);
        teamUnits.add(subteam);
        // Branch Subteam
        branchSubTeamUnits = new ArrayList<AbstractTeamUnit>();
        branchSubTeamUnits.add(teamMemberSessionMember.getSessionMembership().getTeamMember());
        branchSubTeam = new Team(branchSubTeamName, branchSubTeamEchelon, branchSubTeamUnits);
        teamUnits.add(branchSubTeam);
        return new Team(teamName, teamEchelon, teamUnits);
    }
    
    private void prepKnowledgeSession() throws Exception {
        courseInfo = new KnowledgeSessionCourseInfo(domainName, domainName, courseSourceId, null);
        joinedMembers = new HashMap<Integer, SessionMember>();
        joinedMembers.put(teamMemberDomainSessionId, teamMemberSessionMember);
        team = prepTeam();
        roles = new ArrayList<String>();
        roles.add(studentHostRole);
        roles.add(teamMemberRole);
        nodeIdToNameMap = new HashMap<BigInteger, String>();
        nodeIdToNameMap.put(conceptNodeId, conceptName);
        mission = new Mission(missionSource, missionMet, missionTask, missionSituation, missionGoals,
                missionCondition, missionRoe, missionThreatWarning, missionWeaponStatus, missionWeaponPosture);
        observerControls = new ObserverControls();
        teamKnowledgeSession = new TeamKnowledgeSession(
                knowledgeSessionName, scenarioDesc, courseInfo, totalPossibleTeamMembers, studentSessionMember,
                joinedMembers, roles, team, nodeIdToNameMap, trainingAppType, sessionType, sessionStartTime,
                mission, observerControls);
        SessionManager.getInstance().setKnowledgeSession(teamKnowledgeSession);
    }
    
    @Before
    public void prepSessionManager() throws Exception {
        // Student
        if(SessionManager.getInstance().getCurrentDomainSession(studentUsername) == null) {
            studentUserSession = createUserSession(studentId, studentUsername);
            studentDomainSession = createDomainSession(studentId, studentDomainSessionId, studentUserSession);
            updateSessionManager(studentUserSession, studentDomainSession);
            studentSessionMember = createSessionMember(studentHostRole, studentUserSession, studentDomainSessionId);
            // Team Member
            teamMemberUserSession = createUserSession(teamMemberId, teamMemberUsername);
            teamMemberDomainSession = createDomainSession(teamMemberId, teamMemberDomainSessionId, teamMemberUserSession);
            updateSessionManager(teamMemberUserSession, teamMemberDomainSession);
            teamMemberSessionMember = createSessionMember(teamMemberRole, teamMemberUserSession, teamMemberDomainSessionId);
            // Knowledge Session
            prepKnowledgeSession();
        }
    }
    
    @Before
    public void prepLrs() throws Exception {
        lrs = new Lrs();
        lrs.setName("test LRS");
    }
    
    @Test
    public void passingTest() throws Exception {
        Assert.assertTrue(true);
    }
    
    protected JsonNode getNodeField(JsonNode node, LrsEnum key) {
        return node.get(key.getValue());
    }
    
    protected void checkNodeName(JsonNode node, String name) {
        Assert.assertEquals(name,
                getNodeField(node, extensionObjectKeys.NAME).asText());
    }
    
    protected void checkNodeEchelon(JsonNode node, EchelonEnum echelon) throws Exception {
        JsonNode val = getNodeField(node, extensionObjectKeys.ECHELON);
        if(echelon == null) {
            Assert.assertNull(val);
        } else {
            Assert.assertEquals((new EchelonActivity(echelon)).getId().toString(), 
                    val.asText());
        }
    }
    
    protected void checkNodeDepth(JsonNode node, int depth) {
        Assert.assertEquals(depth, 
                getNodeField(node, extensionObjectKeys.DEPTH).asInt());
    }
    
    protected void checkNodeParent(JsonNode node, String parent) {
        JsonNode parentNode = getNodeField(node, extensionObjectKeys.PARENT);
        if(parent == null) {
            Assert.assertNull(parentNode);
        } else {
            Assert.assertEquals(parent, 
                    parentNode.asText());
        }
    }
    
    protected void checkNodeMember(JsonNode node, boolean playable, String identifier, String assigned) throws Exception {
        Assert.assertEquals(playable, 
                getNodeField(node, extensionObjectKeys.PLAYABLE).asBoolean());
        
        Assert.assertEquals((new TeamRoleActivity(identifier)).getId().toString(), 
                getNodeField(node, extensionObjectKeys.IDENTIFIER).asText());
        
        JsonNode assignedNode = getNodeField(node, extensionObjectKeys.ASSIGNED);
        if(assigned == null) {
            Assert.assertNull(assignedNode);
        } else {
            Assert.assertEquals(PersonaHelper.createMboxIFI(assigned),
                    assignedNode.asText());
        }
    }
    
    protected void validateParentTeam(JsonNode node) throws Exception {
        checkNodeName(node, teamName);
        checkNodeEchelon(node, teamEchelon);
        checkNodeDepth(node, 0);
        checkNodeParent(node, null);
        ArrayNode nodeMembers = (ArrayNode) getNodeField(node, extensionObjectKeys.MEMBERS);
        for(int j = 0; j < nodeMembers.size(); j++) {
            JsonNode memberNode = nodeMembers.get(j);
            if(j == 0) {
                checkNodeMember(memberNode, true, studentHostRole, studentUsername);
            }
        }
    }
    
    protected void validateSubTeam(JsonNode node) throws Exception {
        checkNodeName(node, subTeamName);
        checkNodeEchelon(node, null);
        checkNodeDepth(node, 1);
        checkNodeParent(node, teamName);
        ArrayNode nodeMembers = (ArrayNode) getNodeField(node, extensionObjectKeys.MEMBERS);
        for(int j = 0; j < nodeMembers.size(); j++) {
            JsonNode memberNode = nodeMembers.get(j);
            if(j == 0) {
                checkNodeMember(memberNode, false, nonPlayableMemberRole, null);
            }
            if(j == 1) {
                checkNodeMember(memberNode, true, notAssignedMemberRole, null);
            }
        }
    }
    
    protected void validateSubSubTeam(JsonNode node) throws Exception {
        checkNodeName(node, subsubteamName);
        checkNodeEchelon(node, subsubteamEchelon);
        checkNodeDepth(node, 2);
        List<String> parentNames = new ArrayList<String>();
        parentNames.add(teamName);
        parentNames.add(subTeamName);
        checkNodeParent(node, StringUtils.join(parentNames, "|"));
        ArrayNode nodeMembers = (ArrayNode) getNodeField(node, extensionObjectKeys.MEMBERS);
        for(int j = 0; j < nodeMembers.size(); j++) {
            JsonNode memberNode = nodeMembers.get(j);
            if(j == 0) {
                checkNodeMember(memberNode, false, npcMemberRole, null);
            }
        }
    }
    
    protected void validateBranchSubTeam(JsonNode node) throws Exception {
        checkNodeName(node, branchSubTeamName);
        checkNodeEchelon(node, branchSubTeamEchelon);
        checkNodeDepth(node, 1);
        checkNodeParent(node, teamName);
        ArrayNode nodeMembers = (ArrayNode) getNodeField(node, extensionObjectKeys.MEMBERS);
        for(int j = 0; j < nodeMembers.size(); j++) {
            JsonNode memberNode = nodeMembers.get(j);
            if(j == 0) {
                checkNodeMember(memberNode, true, teamMemberRole, teamMemberUsername);
            }
        }
    }
    
    protected void mimicStatementVoid(List<Statement> stmts, Statement voidingStatement, List<Statement> dlq) {
        if(voidingStatement.getObject() instanceof StatementRef) {
            UUID targetId = ((StatementRef) voidingStatement.getObject()).getId();
            for(Statement stmt : stmts) {
                if(stmt.getId().equals(targetId)) {
                    dlq.add(stmt);
                    stmts.remove(stmt);
                    break;
                }
            }
        }
    }
    
    protected boolean isActivityInColl(List<Activity> coll, Activity target) {
        for(Activity a : coll) {
            if(AbstractGiftActivity.isSameActivityId(a, target)) {
                return true;
            }
        }
        return false;
    }
    
    protected void findAndPrintVoidTarget(List<Statement> stmts, Statement voidingStatement) throws Exception {
        if(voidingStatement.getObject().getObjectType().equalsIgnoreCase("statementref")) {
            String targetId = ((StatementRef) voidingStatement.getObject()).getId().toString();
            for(Statement stmt : stmts) {
                if(stmt.getId().toString().equals(targetId)) {
                    System.out.print("Targed Statement! \n");
                    System.out.print(stmt.toJSON(true));
                    System.out.print("\n");
                    break;
                }
            }
        }
    }
    
    protected void findAndPrintReplacement(List<Statement> stmts, Statement voidingStatement) throws Exception {
        if(voidingStatement.getObject().getObjectType().equalsIgnoreCase("statementref")) {
            String targetId = ((StatementRef) voidingStatement.getObject()).getId().toString();
            for(Statement stmt : stmts) {
                String revision = stmt.getContext().getRevision();
                if(revision != null && revision.equals(targetId)) {
                    System.out.print("Replacement Statement! \n");
                    System.out.print(stmt.toJSON(true));
                    System.out.print("\n");
                    break;
                }
            }
        }
    }
    
    private void comparePcExtItem(JsonNode extNode, String key, String activityId,
            Double stress, String stressReason, String stressCategory, 
            Double difficulty, String difficultyReason) throws Exception {
        Assert.assertEquals(activityId, extNode.get(key).asText());
        JsonNode actStress = extNode.get(ItsContextExtensionConcepts.extensionObjectKeys.STRESS.getValue());
        if(actStress == null && stress != null) {
            Assert.assertTrue("expected a stress value but not found within extension!", false);
        } else if(actStress != null && stress == null) {
            Assert.assertTrue("didn't expect a stress value but one was found within extension!", false);
        } else if(actStress != null && stress != null) {
            Assert.assertEquals(stress, (Double) actStress.asDouble());
        }
        JsonNode actDifficulty = extNode.get(ItsContextExtensionConcepts.extensionObjectKeys.DIFFICULTY.getValue());
        if(actDifficulty == null && difficulty != null) {
            Assert.assertTrue("expected a difficulty value but not found within extension!", false);
        } else if(actDifficulty != null && difficulty == null) {
            Assert.assertTrue("didn't expect a stress value but one was found within extension!", false);
        } else if(actDifficulty != null && difficulty != null) {
            Assert.assertEquals(difficulty, (Double) actDifficulty.asDouble());
        }
        JsonNode actStressReason = extNode.get(ItsContextExtensionConcepts.extensionObjectKeys.STRESS_REASON.getValue());
        if(actStressReason == null && stressReason != null) {
            Assert.assertTrue("expected a stress reason value but not found within extension!", false);
        } else if(actStressReason != null && stressReason == null) {
            Assert.assertTrue("didn't expect a stress reason value but one was found within extension!", false);
        } else if(actStressReason != null && stressReason != null) {
            Assert.assertEquals(stressReason, actStressReason.asText());
        }
        JsonNode actDifficultyReason = extNode.get(ItsContextExtensionConcepts.extensionObjectKeys.DIFFICULTY_REASON.getValue());
        if(actDifficultyReason == null && difficultyReason != null) {
            Assert.assertTrue("expected a difficulty reason value but not found within extension!", false);
        } else if(actDifficultyReason != null && difficultyReason == null) {
            Assert.assertTrue("didn't expect a difficulty reason value but one was found within extension!", false);
        } else if(actDifficultyReason != null && difficultyReason != null) {
            Assert.assertEquals(difficultyReason, actDifficultyReason.asText());
        }
        JsonNode actStressCategory = extNode.get(ItsContextExtensionConcepts.extensionObjectKeys.STRESS_CATEGORY.getValue());
        if(actStressCategory == null && stressCategory != null) {
            Assert.assertTrue("expected a stress category value but not found within extension!", false);
        } else if(actStressCategory != null && stressCategory == null) {
            Assert.assertTrue("didn't expect a stress category value but one was found within extension!", false);
        } else if(actStressCategory != null && stressCategory != null) {
            Assert.assertEquals(stressCategory, actStressCategory.asText());
        }
    }
    
    protected void comparePcTask(JsonNode task, TaskScoreNode node) throws Exception {
        comparePcExtItem(task,
                ItsContextExtensionConcepts.extensionObjectKeys.TASK.getValue(),
                AssessmentActivity.createAssessmentId(node.getName()),
                node.getStress(),
                node.getStressReason(),
                null,
                node.getDifficulty(),
                node.getDifficultyReason());
    }
    
    protected void comparePcTask(JsonNode task, TaskPerformanceState state) throws Exception {
        comparePcExtItem(task,
                ItsContextExtensionConcepts.extensionObjectKeys.TASK.getValue(),
                AssessmentActivity.createAssessmentId(state.getState().getName()),
                state.getStress(),
                state.getStressReason(),
                null,
                state.getDifficulty(),
                state.getDifficultyReason());
    }
    
    protected void comparePcCondition(JsonNode condition, EnvironmentControl envControl) throws Exception {
        generated.dkf.EnvironmentAdaptation envAdapt = envControl.getEnvironmentStatusType();
        String expActivityId = EnvironmentAdaptationActivity.dispatch(envAdapt).getId().toString();
        generated.dkf.StrategyStressCategory stressCategory = StrategyUtil.getStrategyStressCategory(envAdapt);
        String sCategory = null;
        if(stressCategory != null) {
            sCategory = stressCategory.value();
        }
        comparePcExtItem(condition,
                ItsContextExtensionConcepts.extensionObjectKeys.CONDITION.getValue(),
                expActivityId,
                envControl.getStress(),
                null,
                sCategory,
                null,
                null);
    }
}
