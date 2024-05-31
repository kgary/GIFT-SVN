package mil.arl.gift.lms.impl.lrs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.Statement;

import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.generate.StartedKnowledgeSessionGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts.extensionObjectKeys;

public class KnowledgeSessionTest extends TestUtils {
    private StartedKnowledgeSessionGenerator.Team stmtGen;
    private ItsActivityTypeConcepts.KnowledgeSessionTeam knowledgeSessionATC;
    private ItsActivityTypeConcepts.Domain domainATC;
    private ItsActivityTypeConcepts.TeamEchelon echelonActivityType;
    private ItsActivityTypeConcepts.DomainSession domainSessionATC;
    private ItsActivityTypeConcepts.KnowledgeSessionType knowledgeSessionTypeATC;
    private ItsActivityTypeConcepts.TeamRole teamRoleATC;
    private ItsContextExtensionConcepts.MissionMetadata missionMetadataCE;
    private ItsContextExtensionConcepts.TeamStructure teamStructureCEC;
    
    @Before
    public void setup() throws Exception {
        stmtGen = new StartedKnowledgeSessionGenerator.Team(studentUsername, new DateTime(sessionStartTime),
                studentDomainSessionId, (TeamKnowledgeSession) SessionManager.getInstance().getKnowledgeSession());
        knowledgeSessionATC = ItsActivityTypeConcepts.KnowledgeSessionTeam.getInstance();
        domainATC = ItsActivityTypeConcepts.Domain.getInstance();
        echelonActivityType = ItsActivityTypeConcepts.TeamEchelon.getInstance();
        domainSessionATC = ItsActivityTypeConcepts.DomainSession.getInstance();
        knowledgeSessionTypeATC = ItsActivityTypeConcepts.KnowledgeSessionType.getInstance();
        teamRoleATC = ItsActivityTypeConcepts.TeamRole.getInstance();
        missionMetadataCE = ItsContextExtensionConcepts.MissionMetadata.getInstance();
        teamStructureCEC = ItsContextExtensionConcepts.TeamStructure.getInstance();
    }
    
    @Test
    public void testTeamKnowledgeSessionStatements() throws Exception {
        Statement stmt = stmtGen.generateStatement();
        // Id
        Assert.assertEquals("05bff6db-e431-35a8-80e4-145ef6d22836", stmt.getId().toString());
        // Actor
        Assert.assertEquals(PersonaHelper.createMboxAgent(studentUsername), stmt.getActor());
        // Verb
        Assert.assertEquals(ItsVerbConcepts.Started.getInstance().asVerb().getId(), stmt.getVerb().getId());
        // Object
        Activity obj = (Activity) stmt.getObject();
        Assert.assertEquals(obj.getId().toString(), "activityId:uri/its/knowledge.session.team/test+session");
        Assert.assertEquals(obj.getDefinition().getType(), knowledgeSessionATC.asActivityType());
        // Context
        Context ctx = stmt.getContext();
        // -> contextActivities
        // --> Parent
        List<Activity> parent = ContextActivitiesHelper.getParentActivities(ctx);
        Assert.assertEquals(1, parent.size());
        Activity domain = parent.get(0);
        Assert.assertEquals(domain.getId().toString(), "activityId:uri/its/test_course.xml");
        Assert.assertEquals(domain.getDefinition().getType(), domainATC.asActivityType());
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
        Assert.assertTrue(groupingIds.contains("activityId:uri/its/echelon/army/squad"));
        Assert.assertTrue(groupingATs.contains(domainSessionATC.asActivityType()));
        Assert.assertTrue(groupingIds.contains("activityId:uri/its/domain.session/1"));
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
        Assert.assertTrue(categoryIds.contains("https://xapinet.org/xapi/stetmt/its/v0.0.1"));
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/knowledge.session.playback/active"));
        Assert.assertTrue(categoryATs.contains(knowledgeSessionTypeATC.asActivityType()));
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/member_role"));
        Assert.assertTrue(categoryIds.contains("activityId:uri/its/team.role/host_role"));
        Assert.assertTrue(categoryATs.contains(teamRoleATC.asActivityType()));
        // -> Team
        Group team = (Group) ctx.getTeam();
        Assert.assertEquals(team.getName(), anonGroupName);
        List<Agent> members = team.getMembers();
        List<Agent> expectedMembers = new ArrayList<Agent>();
        expectedMembers.add(PersonaHelper.createMboxAgent(studentUsername));
        expectedMembers.add(PersonaHelper.createMboxAgent(teamMemberUsername));
        Assert.assertTrue(members.containsAll(expectedMembers));
        // -> Extensions
        // --> Team Structure
        JsonNode teamStructureExt = teamStructureCEC.parseFromExtensions(ctx.getExtensions());
        for(int i = 0; i < teamStructureExt.size(); i++) {
            JsonNode node = teamStructureExt.get(i);
            if(i == 0) {
                validateParentTeam(node);
            }
            if(i == 1) {
                validateSubTeam(node);
            }
            if(i == 2) {
                validateSubSubTeam(node);
            }
            if(i == 3) {
                validateBranchSubTeam(node);
            }   
        }
        // --> Mission metadata
        JsonNode missionExt = missionMetadataCE.parseFromExtensions(ctx.getExtensions());
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.SOURCE).asText(), missionSource);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.MET).asText(), missionMet);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.TASK).asText(), missionTask);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.SITUATION).asText(), missionSituation);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.GOALS).asText(), missionGoals);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.CONDITION).asText(), missionCondition);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.ROE).asText(), missionRoe);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.THREAT_WARNING).asText(), missionThreatWarning);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.WEAPON_STATUS).asText(), missionWeaponStatus);
        Assert.assertEquals(getNodeField(missionExt, extensionObjectKeys.WEAPON_POSTURE).asText(), missionWeaponPosture);
    }
}
