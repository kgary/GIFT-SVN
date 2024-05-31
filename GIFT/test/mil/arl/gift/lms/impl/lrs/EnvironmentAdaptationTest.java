package mil.arl.gift.lms.impl.lrs;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementTarget;
import com.rusticisoftware.tincan.SubStatement;
import com.rusticisoftware.tincan.Verb;
import generated.dkf.AGL;
import generated.dkf.ActorTypeCategoryEnum;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.StrategyStressCategory;
import generated.dkf.Coordinate;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EnvironmentAdaptationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.processor.EnvironmentAdaptationProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.profile.adl.AdlVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.CoordinateExtension;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;

public class EnvironmentAdaptationTest extends TestUtils {

    // Adaptations
    private EnvironmentAdaptation.Overcast overcast = new EnvironmentAdaptation.Overcast();
    private EnvironmentAdaptation.Fog fog = new EnvironmentAdaptation.Fog();
    private EnvironmentAdaptation.Rain rain = new EnvironmentAdaptation.Rain();
    private EnvironmentAdaptation.TimeOfDay timeOfDay = new EnvironmentAdaptation.TimeOfDay();
    private EnvironmentAdaptation.CreateActors createActors = new EnvironmentAdaptation.CreateActors();
    private EnvironmentAdaptation.RemoveActors removeActors = new EnvironmentAdaptation.RemoveActors();
    private EnvironmentAdaptation.Teleport teleport = new EnvironmentAdaptation.Teleport();
    private EnvironmentAdaptation.FatigueRecovery fatigueRecovery = new EnvironmentAdaptation.FatigueRecovery();
    private EnvironmentAdaptation.Endurance endurance = new EnvironmentAdaptation.Endurance();
    private EnvironmentAdaptation.HighlightObjects highlightObjects = new EnvironmentAdaptation.HighlightObjects();
    private EnvironmentAdaptation.RemoveHighlightOnObjects removeHighlightOnObjects = new EnvironmentAdaptation.RemoveHighlightOnObjects();
    private EnvironmentAdaptation.CreateBreadcrumbs createBreadcrumbs = new EnvironmentAdaptation.CreateBreadcrumbs();
    private EnvironmentAdaptation.RemoveBreadcrumbs removeBreadcrumbs = new EnvironmentAdaptation.RemoveBreadcrumbs();
    private EnvironmentAdaptation.Script script = new EnvironmentAdaptation.Script();
    private EnvironmentAdaptation adaptation = new EnvironmentAdaptation();
    private EnvironmentControl envControl;
    // Statement generation
    private EnvironmentAdaptationProcessor processor;
    private List<Statement> statements = new ArrayList<Statement>(1);
    private Statement statement;
    // Extension parsing
    private JsonNode atExt;
    // Agents + Groups
    private Agent studentAgent, teamMemberAgent, npcAgent, studentTeamMemberGroup, studentNpcGroup, teamMemberNpcGroup;
    // Verb
    private Verb experiencedVerb;
    // Team Structure Activity Types
    private ItsActivityTypeConcepts.TeamRole teamRoleATC;
    private ItsActivityTypeConcepts.TeamEchelon echelonATC;
    // Adaptation Activities
    private EnvironmentAdaptationActivity.Overcast overcastActivity;
    private EnvironmentAdaptationActivity.Fog fogActivity;
    private EnvironmentAdaptationActivity.Rain rainActivity;
    private EnvironmentAdaptationActivity.Midnight midnightActivity;
    private EnvironmentAdaptationActivity.Dawn dawnActivity;
    private EnvironmentAdaptationActivity.Midday middayActivity;
    private EnvironmentAdaptationActivity.Dusk duskActivity;
    private EnvironmentAdaptationActivity.Script scriptActivity;
    private EnvironmentAdaptationActivity.CreateActors createActorsActivity;
    private EnvironmentAdaptationActivity.RemoveActors removeActorsActivity;
    private EnvironmentAdaptationActivity.Teleport teleportActivity;
    private EnvironmentAdaptationActivity.FatigueRecovery fatigueActivity;
    private EnvironmentAdaptationActivity.Endurance enduranceActivity;
    private EnvironmentAdaptationActivity.HighlightObjects highlightObjectsActivity;
    private EnvironmentAdaptationActivity.RemoveHighlight removeHighlightActivity;
    private EnvironmentAdaptationActivity.CreateBreadcrumbs createBreadcrumbsActivity;
    private EnvironmentAdaptationActivity.RemoveBreadcrumbs removeBreadcrumbsActivity;
    // Extensions
    private ItsContextExtensionConcepts.PerformanceCharacteristics pcCEC;
    private ItsContextExtensionConcepts.WeatherEnvironmentAdaptation weatherExt;
    private ItsContextExtensionConcepts.CreatedActor createActorExt;
    private ItsContextExtensionConcepts.Highlight highlightExt;
    private ItsContextExtensionConcepts.TeamStructure teamStructureExt;
    private ItsContextExtensionConcepts.CoordinateContext contextCoordinateExt;
    private ItsResultExtensionConcepts.CoordinateResult resultCoordinateExt;
    private ItsResultExtensionConcepts.LocationInfo locationInfoExt;
    
    
    @Before
    public void setup() throws Exception {
        // Agents
        studentAgent = PersonaHelper.createMboxAgent(studentUsername);
        teamMemberAgent = PersonaHelper.createMboxAgent(teamMemberUsername);
        npcAgent = PersonaHelper.createMboxAgent(npcMemberRole);
        // Groups
        List<Agent> groupMembers = new ArrayList<Agent>(2);
        groupMembers.add(studentAgent);
        groupMembers.add(teamMemberAgent);
        studentTeamMemberGroup = PersonaHelper.createGroup(groupMembers);
        groupMembers = new ArrayList<Agent>(2);
        groupMembers.add(studentAgent);
        groupMembers.add(npcAgent);
        studentNpcGroup = PersonaHelper.createGroup(groupMembers);
        groupMembers = new ArrayList<Agent>(2);
        groupMembers.add(teamMemberAgent);
        groupMembers.add(npcAgent);
        teamMemberNpcGroup = PersonaHelper.createGroup(groupMembers);
        // Verb
        experiencedVerb = AdlVerbConcepts.Experienced.getInstance().asVerb();
        // Activity Types
        teamRoleATC = ItsActivityTypeConcepts.TeamRole.getInstance();
        echelonATC = ItsActivityTypeConcepts.TeamEchelon.getInstance();
        // Activities
        overcastActivity = EnvironmentAdaptationActivity.Overcast.getInstance();
        fogActivity = EnvironmentAdaptationActivity.Fog.getInstance();
        rainActivity = EnvironmentAdaptationActivity.Rain.getInstance();
        midnightActivity = EnvironmentAdaptationActivity.Midnight.getInstance();
        dawnActivity = EnvironmentAdaptationActivity.Dawn.getInstance();
        middayActivity = EnvironmentAdaptationActivity.Midday.getInstance();
        duskActivity = EnvironmentAdaptationActivity.Dusk.getInstance();
        scriptActivity = EnvironmentAdaptationActivity.Script.getInstance();
        createActorsActivity = EnvironmentAdaptationActivity.CreateActors.getInstance();
        removeActorsActivity = EnvironmentAdaptationActivity.RemoveActors.getInstance();
        teleportActivity = EnvironmentAdaptationActivity.Teleport.getInstance();
        fatigueActivity = EnvironmentAdaptationActivity.FatigueRecovery.getInstance();
        enduranceActivity = EnvironmentAdaptationActivity.Endurance.getInstance();
        highlightObjectsActivity = EnvironmentAdaptationActivity.HighlightObjects.getInstance();
        removeHighlightActivity = EnvironmentAdaptationActivity.RemoveHighlight.getInstance();
        createBreadcrumbsActivity = EnvironmentAdaptationActivity.CreateBreadcrumbs.getInstance();
        removeBreadcrumbsActivity = EnvironmentAdaptationActivity.RemoveBreadcrumbs.getInstance();
        // Extensions
        teamStructureExt = ItsContextExtensionConcepts.TeamStructure.getInstance();
        weatherExt = ItsContextExtensionConcepts.WeatherEnvironmentAdaptation.getInstance();
        createActorExt = ItsContextExtensionConcepts.CreatedActor.getInstance();
        highlightExt = ItsContextExtensionConcepts.Highlight.getInstance();
        contextCoordinateExt = ItsContextExtensionConcepts.CoordinateContext.getInstance();
        resultCoordinateExt = ItsResultExtensionConcepts.CoordinateResult.getInstance();
        locationInfoExt = ItsResultExtensionConcepts.LocationInfo.getInstance();
        pcCEC = ItsContextExtensionConcepts.PerformanceCharacteristics.getInstance();
    }
    
    private void configureEnvironmentControl(EnvironmentAdaptation environmentAdaptation) {
        envControl = new EnvironmentControl(environmentAdaptation);
    }
    
    private void configureEnvironmentControl(EnvironmentAdaptation environmentAdaptation, double stress) {
        configureEnvironmentControl(environmentAdaptation);
        envControl.setStress(Double.valueOf(stress));
    }
    
    private void configureProcessor(EnvironmentAdaptation environmentAdaptation) throws LmsXapiActivityException {
        configureEnvironmentControl(environmentAdaptation);
        processor = new EnvironmentAdaptationProcessor(envControl, studentUsername, studentDomainSessionId,
                SessionManager.getInstance().getKnowledgeSession());
    }
    
    private void configureProcessor(EnvironmentAdaptation environmentAdaptation, double stress) throws LmsXapiActivityException {
        configureEnvironmentControl(environmentAdaptation, stress);
        processor = new EnvironmentAdaptationProcessor(envControl, studentUsername, studentDomainSessionId,
                SessionManager.getInstance().getKnowledgeSession());
    }
    
    private void generateStatement() throws Exception {
        statements.clear();
        processor.process(statements);
        statement = statements.get(0);
    }
    
    private void testStatementActor() throws Exception {
        Assert.assertEquals(studentAgent, statement.getActor());
    }
    
    private void testStatementVerb() throws Exception {
        Assert.assertEquals(experiencedVerb, statement.getVerb());
    }
    
    private void testStatementObject(EnvironmentAdaptationActivity activity) throws Exception {
        StatementTarget stmtObj = statement.getObject();
        if(stmtObj instanceof Activity) {
            Assert.assertEquals(activity, statement.getObject());
        } else if(stmtObj instanceof SubStatement) {
            Assert.assertEquals(activity, ((SubStatement) stmtObj).getObject());
        }
    }
    
    private void testTeamRoleActivities(List<String> roleSlugs) throws Exception {
        // Expected set
        Set<String> expectedIds = new HashSet<String>(roleSlugs.size());
        for(String slug : roleSlugs) {
            MarkedTeamMember teamRole = new MarkedTeamMember(slug, slug);
            Activity teamRoleActivity = teamRoleATC.asActivity(teamRole);
            expectedIds.add(teamRoleActivity.getId().toString());
        }
        List<Activity> instances = teamRoleATC.findInstancesInCollection(statement.getContext().getContextActivities().getCategory());
        // Found set
        Set<String> foundIds = new HashSet<String>(instances.size());
        for(Activity roleActivity : instances) {
            String activityId = roleActivity.getId().toString();
            foundIds.add(activityId);
        }
        // equal sets
        Assert.assertEquals(expectedIds, foundIds);
    }
    
    private void testTeamRoleActivities(String roleSlug) throws Exception {
        List<String> roleSlugs = new ArrayList<String>(1);
        roleSlugs.add(roleSlug);
        testTeamRoleActivities(roleSlugs);
    }
    
    private void testPcCondition() throws Exception {
        JsonNode ext = pcCEC.parseFromExtensions(statement.getContext().getExtensions());
        JsonNode tasks = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.TASKS.getValue());
        JsonNode conditions = ext.get(ItsContextExtensionConcepts.extensionObjectKeys.CONDITIONS.getValue());
        Assert.assertNull(tasks);
        Assert.assertEquals(1, conditions.size());
        comparePcCondition(conditions.get(0), envControl);
    }
    
    private void testStatementBasic(EnvironmentAdaptationActivity activity) throws Exception {
        // process + generate statement
        configureProcessor(adaptation);
        generateStatement();
        testStatementActor();
        testStatementVerb();
        // Actor team role expected to be in all statements
        testTeamRoleActivities(studentHostRole);
        testStatementObject(activity);
    }
    
    private void testStatementBasic(EnvironmentAdaptationActivity activity, String otherRoleSlug) throws Exception {
        // process + generate statement
        configureProcessor(adaptation);
        generateStatement();
        testStatementActor();
        testStatementVerb();
        List<String> roleSlugs = new ArrayList<String>(2);
        roleSlugs.add(studentHostRole);
        roleSlugs.add(otherRoleSlug);
        testTeamRoleActivities(roleSlugs);
        testStatementObject(activity);
    }
    
    private void testStatementBasic(EnvironmentAdaptationActivity activity, List<String> roleSlugs) throws Exception {
        // process + generate statement
        configureProcessor(adaptation);
        generateStatement();
        testStatementActor();
        testStatementVerb();
        roleSlugs.add(studentHostRole);
        testTeamRoleActivities(roleSlugs);
        testStatementObject(activity);
    }
    
    private void testSubStatementActor(Agent subActor) throws Exception {
        Assert.assertEquals(subActor, ((SubStatement) statement.getObject()).getActor());
    }
    
    private void testResultResponse(String expected) throws Exception {
        if(statement.getObject() instanceof Activity) {
            Assert.assertEquals(expected, statement.getResult().getResponse());
        } else if(statement.getObject() instanceof SubStatement) {
            Assert.assertEquals(expected, ((SubStatement) statement.getObject()).getResult().getResponse());
        }
    }
    
    private void parseExtension(ItsContextExtensionConcepts extension) throws Exception {
        atExt = extension.parseFromExtensions(statement.getContext().getExtensions());
    }
    
    private void parseExtension(ItsResultExtensionConcepts extension) throws Exception {
        atExt = extension.parseFromExtensions(statement.getResult().getExtensions());
    }
    
    private void testTeamStructure(boolean npcRelevant, boolean sessionMemberRelevant) throws Exception {
        // NOTE: parent and branch sub team both fire team echelon
        List<Activity> instances = echelonATC.findInstancesInCollection(statement.getContext().getContextActivities().getGrouping());
        Set<String> foundIds = new HashSet<String>(instances.size());
        for(Activity echelonActivity : instances) {
            String activityId = echelonActivity.getId().toString();
            foundIds.add(activityId);
        }
        Set<String> expectedIds;
        parseExtension(teamStructureExt);
        if(npcRelevant && sessionMemberRelevant) {
            Assert.assertEquals(atExt.size(), 3);
            validateParentTeam(atExt.get(0));
            validateSubSubTeam(atExt.get(1));
            validateBranchSubTeam(atExt.get(2));
            // Echelon (fire team + squad)
            expectedIds = new HashSet<String>(2);
            expectedIds.add(echelonATC.asActivity(teamEchelon).getId().toString());
            expectedIds.add(echelonATC.asActivity(subsubteamEchelon).getId().toString());
            Assert.assertEquals(foundIds, expectedIds);
        } else if(npcRelevant) {
            // session member not relevant to statement
            Assert.assertEquals(atExt.size(), 2);
            validateParentTeam(atExt.get(0));
            validateSubSubTeam(atExt.get(1));
            // Echelon (fire team + squad)
            expectedIds = new HashSet<String>(2);
            expectedIds.add(echelonATC.asActivity(teamEchelon).getId().toString());
            expectedIds.add(echelonATC.asActivity(subsubteamEchelon).getId().toString());
            Assert.assertEquals(foundIds, expectedIds);
        } else {
            // by assumption + default, session member is relevant
            Assert.assertEquals(atExt.size(), 2);
            validateParentTeam(atExt.get(0));
            validateBranchSubTeam(atExt.get(1));
            // Echelon (fire team)
            expectedIds = new HashSet<String>(1);
            expectedIds.add(echelonATC.asActivity(teamEchelon).getId().toString());
            Assert.assertEquals(foundIds, expectedIds);
        }
    }
    
    private void testDurationExtensionKey(double expected) {
        Assert.assertTrue(getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.DURATION).asDouble() == expected);
    }
    
    private void testValueExtensionKey(double expected) {
        Assert.assertTrue(getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.VALUE).doubleValue() == expected);
    }
    
    private void testExtension(EnvironmentAdaptation.Overcast adaptation) throws Exception {
        parseExtension(weatherExt);
        // Duration
        testDurationExtensionKey(adaptation.getScenarioAdaptationDuration().doubleValue());
        // Value
        testValueExtensionKey(adaptation.getValue().doubleValue());
    }
    
    private void testExtension(EnvironmentAdaptation.Rain adaptation) throws Exception {
        parseExtension(weatherExt);
        // Duration
        testDurationExtensionKey(adaptation.getScenarioAdaptationDuration().doubleValue());
        // Value
        testValueExtensionKey(adaptation.getValue().doubleValue());
    }
    
    private void testExtension(EnvironmentAdaptation.Fog adaptation) throws Exception {
        parseExtension(weatherExt);
        // Duration
        testDurationExtensionKey(adaptation.getScenarioAdaptationDuration().doubleValue());
        // Density
        Assert.assertTrue(getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.DENSITY).asDouble() == adaptation.getDensity().doubleValue());
        // Color
        JsonNode atColor = getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.COLOR);
        // -> red 
        Assert.assertTrue(getNodeField(atColor, ItsContextExtensionConcepts.extensionObjectKeys.RED).intValue() == adaptation.getColor().getRed());
        // -> blue
        Assert.assertTrue(getNodeField(atColor, ItsContextExtensionConcepts.extensionObjectKeys.BLUE).intValue() == adaptation.getColor().getBlue());
        // -> green
        Assert.assertTrue(getNodeField(atColor, ItsContextExtensionConcepts.extensionObjectKeys.GREEN).intValue() == adaptation.getColor().getGreen());
    }
    
    private void testCoordinateExtension(JsonNode node, List<Coordinate> coordinates) {
        for(int i = 0; i<=coordinates.size() - 1; i++) {
            Coordinate coordinate = coordinates.get(i);
            JsonNode coordNode = node.get(i);
            Serializable coordKind = coordinate.getType();
            if(coordKind instanceof GCC) {
                GCC gcc = (GCC) coordKind;
                JsonNode atGCC = getNodeField(coordNode, CoordinateExtension.extensionObjectKeys.GCC);
                Assert.assertTrue(getNodeField(atGCC, CoordinateExtension.extensionObjectKeys.X).doubleValue() == gcc.getX().doubleValue());
                Assert.assertTrue(getNodeField(atGCC, CoordinateExtension.extensionObjectKeys.Y).doubleValue() == gcc.getY().doubleValue());
                Assert.assertTrue(getNodeField(atGCC, CoordinateExtension.extensionObjectKeys.Z).doubleValue() == gcc.getZ().doubleValue());
            } else if(coordKind instanceof GDC) {
                GDC gdc = (GDC) coordKind;
                JsonNode atGDC = getNodeField(coordNode, CoordinateExtension.extensionObjectKeys.GDC);
                Assert.assertTrue(getNodeField(atGDC, CoordinateExtension.extensionObjectKeys.LATITUDE).doubleValue() == gdc.getLatitude().doubleValue());
                Assert.assertTrue(getNodeField(atGDC, CoordinateExtension.extensionObjectKeys.LONGITUDE).doubleValue() == gdc.getLongitude().doubleValue());
                Assert.assertTrue(getNodeField(atGDC, CoordinateExtension.extensionObjectKeys.ELEVATION).doubleValue() == gdc.getElevation().doubleValue());
            } else if(coordKind instanceof AGL) {
                AGL agl = (AGL) coordKind;
                JsonNode atAGL = getNodeField(coordNode, CoordinateExtension.extensionObjectKeys.AGL);
                Assert.assertTrue(getNodeField(atAGL, CoordinateExtension.extensionObjectKeys.X).doubleValue() == agl.getX().doubleValue());
                Assert.assertTrue(getNodeField(atAGL, CoordinateExtension.extensionObjectKeys.Y).doubleValue() == agl.getY().doubleValue());
                Assert.assertTrue(getNodeField(atAGL, CoordinateExtension.extensionObjectKeys.ELEVATION).doubleValue() == agl.getElevation().doubleValue());
            }
        }
    }
    
    private void testCoordinateExtension(JsonNode node, Coordinate coordinate) {
        List<Coordinate> coordinates = new ArrayList<Coordinate>(1);
        coordinates.add(coordinate);
        testCoordinateExtension(node, coordinates);
    }
    
    private void testCoordinateExtension(List<Coordinate> coordinates) throws Exception {
        testCoordinateExtension(atExt, coordinates);
    }
    
    private void testContextCoordinateExtension(List<Coordinate> coordinates) throws Exception {
        parseExtension(contextCoordinateExt);
        testCoordinateExtension(coordinates);
    }
    
    private void testResultCoordinateExtension(List<Coordinate> coordinates) throws Exception {
        parseExtension(resultCoordinateExt);
        testCoordinateExtension(coordinates);
    }
    
    private void testContextCoordinateExtension(Coordinate coordinate) throws Exception {
        List<Coordinate> coordinates = new ArrayList<Coordinate>(1);
        coordinates.add(coordinate);
        testContextCoordinateExtension(coordinates);
    }
    
    private void testResultCoordinateExtension(Coordinate coordinate) throws Exception {
        List<Coordinate> coordinates = new ArrayList<Coordinate>(1);
        coordinates.add(coordinate);
        testResultCoordinateExtension(coordinates);
    }
    
    private void testExtension(EnvironmentAdaptation.CreateActors adaptation) throws Exception {
        parseExtension(createActorExt);
        // type
        Assert.assertEquals(getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.TYPE).textValue(), adaptation.getType());
        // side
        String atSide = getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.SIDE).textValue();
        Serializable sideKind = adaptation.getSide().getType();
        if(sideKind instanceof EnvironmentAdaptation.CreateActors.Side.Civilian) {
            Assert.assertEquals(atSide, ItsContextExtensionConcepts.extensionObjectKeys.CIVILIAN.getValue());
        } else if(sideKind instanceof EnvironmentAdaptation.CreateActors.Side.Blufor) {
            Assert.assertEquals(atSide, ItsContextExtensionConcepts.extensionObjectKeys.BLUFOR.getValue());
        } else if(sideKind instanceof EnvironmentAdaptation.CreateActors.Side.Opfor) {
            Assert.assertEquals(atSide, ItsContextExtensionConcepts.extensionObjectKeys.OPFOR.getValue());
        }
        // Coordinate
        testContextCoordinateExtension(adaptation.getCoordinate());
    }
    
    @Test
    public void testOvercast() throws Exception {
        // adaptation
        overcast.setScenarioAdaptationDuration(new BigInteger("10"));
        overcast.setValue(new BigDecimal("1.0"));
        adaptation.setType(overcast);
        // process + generate statement
        testStatementBasic(overcastActivity);
        testExtension(overcast);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.5);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testFog() throws Exception {
        // adaptation
        EnvironmentAdaptation.Fog.Color color = new EnvironmentAdaptation.Fog.Color();
        color.setRed(1);
        color.setBlue(2);
        color.setGreen(3);
        fog.setColor(color);
        fog.setScenarioAdaptationDuration(new BigInteger("10"));
        fog.setDensity(new BigDecimal("1.0"));
        adaptation.setType(fog);
        // process + generate statement
        testStatementBasic(fogActivity);
        testExtension(fog);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.0);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testRain() throws Exception {
        // adaptation
        rain.setScenarioAdaptationDuration(new BigInteger("10"));
        rain.setValue(new BigDecimal("1.0"));
        adaptation.setType(rain);
        // process + generate statement
        testStatementBasic(rainActivity);
        testExtension(rain);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.5);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testTimeOfDay() throws Exception {
        // Midnight
        EnvironmentAdaptation.TimeOfDay.Midnight midnight = new EnvironmentAdaptation.TimeOfDay.Midnight();
        timeOfDay.setType(midnight);
        adaptation.setType(timeOfDay);
        testStatementBasic(midnightActivity);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 1.0);
        generateStatement();
        testPcCondition();
        // Dawn
        EnvironmentAdaptation.TimeOfDay.Dawn dawn = new EnvironmentAdaptation.TimeOfDay.Dawn();
        timeOfDay.setType(dawn);
        adaptation.setType(timeOfDay);
        testStatementBasic(dawnActivity);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.75);
        generateStatement();
        testPcCondition();
        // Midday
        EnvironmentAdaptation.TimeOfDay.Midday midday = new EnvironmentAdaptation.TimeOfDay.Midday();
        timeOfDay.setType(midday);
        adaptation.setType(timeOfDay);
        testStatementBasic(middayActivity);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.5);
        generateStatement();
        testPcCondition();
        // Dusk
        EnvironmentAdaptation.TimeOfDay.Dusk dusk = new EnvironmentAdaptation.TimeOfDay.Dusk();
        timeOfDay.setType(dusk);
        adaptation.setType(timeOfDay);
        testStatementBasic(duskActivity);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.25);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testScript() throws Exception {
        // adaptation
        script.setValue("test script");
        script.setStressCategory(StrategyStressCategory.COGNITIVE);
        adaptation.setType(script);
        // process + generate statement
        testStatementBasic(scriptActivity);
        testResultResponse(script.getValue());
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.0);
        generateStatement();
        testPcCondition();
    }
    
    private Coordinate createGCC() {
        Coordinate coordinate = new Coordinate();
        GCC gcc = new GCC();
        gcc.setX(new BigDecimal("1.0"));
        gcc.setY(new BigDecimal("2.0"));
        gcc.setZ(new BigDecimal("3.0"));
        coordinate.setType(gcc);
        return coordinate;
    }
    
    private Coordinate createGDC() {
        Coordinate coordinate = new Coordinate();
        GDC gdc = new GDC();
        gdc.setLatitude(new BigDecimal("1.0"));
        gdc.setLongitude(new BigDecimal("2.0"));
        gdc.setElevation(new BigDecimal("3.0"));
        coordinate.setType(gdc);
        return coordinate;
    }
    
    private Coordinate createAGL() {
        Coordinate coordinate = new Coordinate();
        AGL agl = new AGL();
        agl.setX(new BigDecimal("1.0"));
        agl.setY(new BigDecimal("2.0"));
        agl.setElevation(new BigDecimal("3.0"));
        coordinate.setType(agl);
        return coordinate;
    }
    
    @Test
    public void testCreateActors() throws Exception {
        // adaptation
        createActors.setType("test actor type");
        EnvironmentAdaptation.CreateActors.Side side = new EnvironmentAdaptation.CreateActors.Side();
        // civ + GCC
        side.setType(new EnvironmentAdaptation.CreateActors.Side.Civilian());
        createActors.setCoordinate(createGCC());
        createActors.setSide(side);
        adaptation.setType(createActors);
        // process + generate statement
        testStatementBasic(createActorsActivity);
        testExtension(createActors);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.25);
        generateStatement();
        testPcCondition();
        // bluefor + GDC
        side.setType(new EnvironmentAdaptation.CreateActors.Side.Blufor());
        createActors.setCoordinate(createGDC());
        createActors.setSide(side);
        adaptation.setType(createActors);
        // process + generate statement
        testStatementBasic(createActorsActivity);
        testExtension(createActors);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.5);
        generateStatement();
        testPcCondition();
        // opfor + AGL
        side.setType(new EnvironmentAdaptation.CreateActors.Side.Opfor());
        createActors.setCoordinate(createAGL());
        createActors.setSide(side);
        adaptation.setType(createActors);
        // process + generate statement
        testStatementBasic(createActorsActivity);
        testExtension(createActors);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.75);
        generateStatement();
        testPcCondition();
    }
    
    private void testRemovedActorsRepresentation() throws Exception {
        Set<Agent> memberSet = new HashSet<Agent>(((Group) statement.getContext().getInstructor()).getMembers());
        Set<Agent> expectedAgents = new HashSet<Agent>();
        Serializable target = removeActors.getType();
        if(target instanceof String) {
            expectedAgents.add(PersonaHelper.createMboxAgent((String) target));
        }
        Assert.assertEquals(expectedAgents, memberSet); 
    }
    
    @Test
    public void testRemoveActors() throws Exception {
        // NOTE: NPC only relevant when TeamMemberRef points to NPC opposed to string name
        // adaptation
        // -> Actor name without category
        removeActors.setType(teamMemberUsername);
        adaptation.setType(removeActors);
        // process + generate statement
        testStatementBasic(removeActorsActivity, teamMemberRole);
        testRemovedActorsRepresentation();
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -1.0);
        generateStatement();
        testPcCondition();
        // -> Single Actor name
        // --> Actor name points to session member
        removeActors.setType(teamMemberUsername);
        removeActors.setTypeCategory(ActorTypeCategoryEnum.PERSON);
        adaptation.setType(removeActors);
        // process + generate statement
        testStatementBasic(removeActorsActivity, teamMemberRole);
        testRemovedActorsRepresentation();
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -1.0);
        generateStatement();
        testPcCondition();
        // --> Actor name doesn't point to session member
        removeActors.setType(npcMemberRole);
        removeActors.setTypeCategory(ActorTypeCategoryEnum.OTHER);
        adaptation.setType(removeActors);
        // process + generate statement
        testStatementBasic(removeActorsActivity);
        testRemovedActorsRepresentation();
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.75);
        generateStatement();
        testPcCondition();
        // -> Single Location GCC
        EnvironmentAdaptation.RemoveActors.Location location = new EnvironmentAdaptation.RemoveActors.Location();
        Coordinate coordinate = createGCC();
        location.setCoordinate(coordinate);
        removeActors.setType(location);
        removeActors.setTypeCategory(ActorTypeCategoryEnum.VEHICLE);
        adaptation.setType(removeActors);
        // process + generate statement
        testStatementBasic(removeActorsActivity);
        testContextCoordinateExtension(coordinate);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.25);
        generateStatement();
        testPcCondition();
        // -> Single GDC Location without category
        EnvironmentAdaptation.RemoveActors.Location secondLocation = new EnvironmentAdaptation.RemoveActors.Location();
        Coordinate secondCoordinate = createGDC();
        secondLocation.setCoordinate(secondCoordinate);
        removeActors.setType(secondLocation);
        adaptation.setType(removeActors);
        List<Coordinate> coordinates = new ArrayList<Coordinate>(1);
        coordinates.add(secondCoordinate);
        // process + generate statement
        testStatementBasic(removeActorsActivity);
        testContextCoordinateExtension(coordinates);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.0);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testTeleport() throws Exception {
        // adaptation
        Integer headingValue = 1;
        EnvironmentAdaptation.Teleport.Heading heading = new EnvironmentAdaptation.Teleport.Heading();
        heading.setValue(headingValue);
        Coordinate coordinate = createAGL();
        // Actor as team member ref
        EnvironmentAdaptation.Teleport.TeamMemberRef teamMemberRef = new EnvironmentAdaptation.Teleport.TeamMemberRef();
        teamMemberRef.setEntityMarking(studentHostRole);
        teamMemberRef.setValue(studentHostRole);
        teleport.setCoordinate(coordinate);
        teleport.setHeading(heading);
        teleport.setTeamMemberRef(teamMemberRef);
        teleport.setStressCategory(StrategyStressCategory.COGNITIVE);
        adaptation.setType(teleport);
        
        // process + generate statement
        testStatementBasic(teleportActivity);
        testResultResponse(headingValue.toString());
        testResultCoordinateExtension(coordinate);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.75);
        generateStatement();
        testPcCondition();
        // Session Member as team member ref
        headingValue = 2;
        heading.setValue(headingValue);
        teamMemberRef.setEntityMarking(teamMemberRole);
        teamMemberRef.setValue(teamMemberRole);
        coordinate = createGDC();
        teleport.setTeamMemberRef(teamMemberRef);
        teleport.setHeading(heading);
        teleport.setCoordinate(coordinate);
        adaptation.setType(teleport);
        // process + generate statement
        testStatementBasic(teleportActivity, teamMemberRole);
        testSubStatementActor(teamMemberAgent);
        testResultResponse(headingValue.toString());
        testResultCoordinateExtension(coordinate);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 1.0);
        generateStatement();
        testPcCondition();
        // NPC as team member ref
        headingValue = 3;
        heading.setValue(headingValue);
        teamMemberRef.setEntityMarking(npcMemberRole);
        teamMemberRef.setValue(npcMemberRole);
        coordinate = createGCC();
        teleport.setTeamMemberRef(teamMemberRef);
        teleport.setHeading(heading);
        teleport.setCoordinate(coordinate);
        adaptation.setType(teleport);
        // process + generate statement
        testStatementBasic(teleportActivity, npcMemberRole);
        testSubStatementActor(npcAgent);
        testResultResponse(headingValue.toString());
        testResultCoordinateExtension(coordinate);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, 0.75);
        generateStatement();
        testPcCondition();
    }
    
    private void testRawScore(BigDecimal value) throws Exception {
        if(statement.getObject() instanceof Activity) {
            Assert.assertTrue(value.doubleValue() == statement.getResult().getScore().getRaw().doubleValue());
        } else if(statement.getObject() instanceof SubStatement) {
            Assert.assertTrue(value.doubleValue() == ((SubStatement) statement.getObject()).getResult().getScore().getRaw().doubleValue());
        }
    }
    
    @Test
    public void testFatigue() throws Exception {
        // adaptation
        BigDecimal rate = new BigDecimal("1.0");
        fatigueRecovery.setRate(rate);
        // Actor as team member ref
        EnvironmentAdaptation.FatigueRecovery.TeamMemberRef teamMemberRef = new EnvironmentAdaptation.FatigueRecovery.TeamMemberRef();
        teamMemberRef.setEntityMarking(studentHostRole);
        teamMemberRef.setValue(studentHostRole);
        fatigueRecovery.setTeamMemberRef(teamMemberRef);
        fatigueRecovery.setStressCategory(StrategyStressCategory.PHYSIOLOGICAL);
        adaptation.setType(fatigueRecovery);
        // process + generate statement
        testStatementBasic(fatigueActivity);
        testRawScore(rate);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.5);
        generateStatement();
        testPcCondition();
        // Session member as team member ref
        teamMemberRef.setEntityMarking(teamMemberRole);
        teamMemberRef.setValue(teamMemberRole);
        fatigueRecovery.setTeamMemberRef(teamMemberRef);
        adaptation.setType(fatigueRecovery);
        testStatementBasic(fatigueActivity, teamMemberRole);
        testRawScore(rate);
        testSubStatementActor(teamMemberAgent);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.25);
        generateStatement();
        testPcCondition();
        // NPC as team member ref
        teamMemberRef.setEntityMarking(npcMemberRole);
        teamMemberRef.setValue(npcMemberRole);
        fatigueRecovery.setTeamMemberRef(teamMemberRef);
        adaptation.setType(fatigueRecovery);
        testStatementBasic(fatigueActivity, npcMemberRole);
        testSubStatementActor(npcAgent);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, 0.0);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testEndurance() throws Exception {
        // adaptation
        BigDecimal rate = new BigDecimal("1.0");
        endurance.setValue(rate);
        // Actor as Team member ref
        EnvironmentAdaptation.Endurance.TeamMemberRef teamMemberRef = new EnvironmentAdaptation.Endurance.TeamMemberRef();
        teamMemberRef.setEntityMarking(studentHostRole);
        teamMemberRef.setValue(studentHostRole);
        endurance.setTeamMemberRef(teamMemberRef);
        endurance.setStressCategory(StrategyStressCategory.PHYSIOLOGICAL);
        adaptation.setType(endurance);
        // process + generate statement
        testStatementBasic(enduranceActivity);
        testRawScore(rate);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.25);
        generateStatement();
        testPcCondition();
        // Session member as team member ref
        teamMemberRef.setEntityMarking(teamMemberRole);
        teamMemberRef.setValue(teamMemberRole);
        endurance.setTeamMemberRef(teamMemberRef);
        adaptation.setType(endurance);
        // process + generate statement
        testStatementBasic(enduranceActivity, teamMemberRole);
        testRawScore(rate);
        testSubStatementActor(teamMemberAgent);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.5);
        generateStatement();
        testPcCondition();
        // NPC as team member ref
        teamMemberRef.setEntityMarking(npcMemberRole);
        teamMemberRef.setValue(npcMemberRole);
        endurance.setTeamMemberRef(teamMemberRef);
        adaptation.setType(endurance);
        // process + generate statement
        testStatementBasic(enduranceActivity, npcMemberRole);
        testRawScore(rate);
        testSubStatementActor(npcAgent);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, -0.75);
        generateStatement();
        testPcCondition();
    }
    
    private void testExtension(EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo) throws Exception {
        parseExtension(locationInfoExt);
        Assert.assertEquals(locationInfo.getPlaceOfInterestRef(), getNodeField(atExt, ItsResultExtensionConcepts.extensionObjectKeys.PLACE_OF_INTEREST).asText());
        if(locationInfo.getCoordinate() != null) {
            testCoordinateExtension(getNodeField(atExt, ItsResultExtensionConcepts.extensionObjectKeys.COORDINATE), locationInfo.getCoordinate());
        }
    }
    
    private void testExtension(EnvironmentAdaptation.HighlightObjects highlightObjects) throws Exception {
        parseExtension(highlightExt);
        // Offset
        EnvironmentAdaptation.HighlightObjects.Offset offset = highlightObjects.getOffset();
        JsonNode atOffset = getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.OFFSET);
        if(atOffset != null && offset != null) {
            Assert.assertTrue(getNodeField(atOffset, ItsContextExtensionConcepts.extensionObjectKeys.FRONT).asDouble() == offset.getFront().doubleValue());
            Assert.assertTrue(getNodeField(atOffset, ItsContextExtensionConcepts.extensionObjectKeys.RIGHT).asDouble() == offset.getRight().doubleValue());
            Assert.assertTrue(getNodeField(atOffset, ItsContextExtensionConcepts.extensionObjectKeys.UP).asDouble() == offset.getUp().doubleValue());
        }
        // Color
        EnvironmentAdaptation.HighlightObjects.Color color = highlightObjects.getColor();
        JsonNode atColor = getNodeField(atExt, ItsContextExtensionConcepts.extensionObjectKeys.COLOR);
        if(atColor != null && color != null) {
            Serializable colorKind = color.getType();
            if(colorKind instanceof EnvironmentAdaptation.HighlightObjects.Color.Red) {
                Assert.assertEquals(ItsContextExtensionConcepts.extensionObjectKeys.RED.getValue(), atColor.asText());
            } else if(colorKind instanceof EnvironmentAdaptation.HighlightObjects.Color.Green) {
                Assert.assertEquals(ItsContextExtensionConcepts.extensionObjectKeys.GREEN.getValue(), atColor.asText());
            } else if(colorKind instanceof EnvironmentAdaptation.HighlightObjects.Color.Blue) {
                Assert.assertEquals(ItsContextExtensionConcepts.extensionObjectKeys.BLUE.getValue(), atColor.asText());
            }
        }
        // Duration
        BigInteger duration = highlightObjects.getDuration();
        if(duration != null) {
            testDurationExtensionKey(duration.doubleValue());
        }
        Serializable kind = highlightObjects.getType();
        // Location Info
        if(kind instanceof EnvironmentAdaptation.HighlightObjects.LocationInfo) {
            testExtension((EnvironmentAdaptation.HighlightObjects.LocationInfo) kind);
        }
    }
        
    @Test
    public void testHighlight() throws Exception {
        // adaptation
        highlightObjects.setDuration(new BigInteger("1"));
        highlightObjects.setName("highlight name");
        // color
        EnvironmentAdaptation.HighlightObjects.Color color = new EnvironmentAdaptation.HighlightObjects.Color();
        EnvironmentAdaptation.HighlightObjects.Color.Red redColor = new EnvironmentAdaptation.HighlightObjects.Color.Red();
        EnvironmentAdaptation.HighlightObjects.Color.Green greenColor = new EnvironmentAdaptation.HighlightObjects.Color.Green();
        EnvironmentAdaptation.HighlightObjects.Color.Blue blueColor = new EnvironmentAdaptation.HighlightObjects.Color.Blue();
        // offset
        EnvironmentAdaptation.HighlightObjects.Offset offset = new EnvironmentAdaptation.HighlightObjects.Offset();
        offset.setRight(new BigDecimal("1.0"));
        offset.setFront(new BigDecimal("2.0"));
        offset.setUp(new BigDecimal("3.0"));
        highlightObjects.setOffset(offset);
        // location info
        EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = new EnvironmentAdaptation.HighlightObjects.LocationInfo();
        locationInfo.setPlaceOfInterestRef("point of interest ref");
        Coordinate coordinate = createGCC();
        locationInfo.setCoordinate(coordinate);
        highlightObjects.setType(locationInfo);
        color.setType(redColor);
        highlightObjects.setColor(color);
        adaptation.setType(highlightObjects);
        testStatementBasic(highlightObjectsActivity);
        testResultResponse(highlightObjects.getName());
        testTeamStructure(false, true);
        // location result extension + highlight context extension
        testExtension(highlightObjects);
        // with stress
        configureProcessor(adaptation, -1.0);
        generateStatement();
        testPcCondition();
        // teamMemberRef
        EnvironmentAdaptation.HighlightObjects.TeamMemberRef teamMemberRef = new EnvironmentAdaptation.HighlightObjects.TeamMemberRef();
        // -> Actor as ref
        teamMemberRef.setEntityMarking(studentHostRole);
        teamMemberRef.setValue(studentHostRole);
        highlightObjects.setType(teamMemberRef);
        color.setType(greenColor);
        highlightObjects.setColor(color);
        adaptation.setType(highlightObjects);
        testStatementBasic(highlightObjectsActivity);
        testResultResponse(highlightObjects.getName());
        testTeamStructure(false, true);
        // highlight context extension
        testExtension(highlightObjects);
        // with stress
        configureProcessor(adaptation, -0.75);
        generateStatement();
        testPcCondition();
        // -> Session member as ref
        teamMemberRef.setEntityMarking(teamMemberRole);
        teamMemberRef.setValue(teamMemberRole);
        highlightObjects.setType(teamMemberRef);
        color.setType(blueColor);
        highlightObjects.setColor(color);
        adaptation.setType(highlightObjects);
        testStatementBasic(highlightObjectsActivity, teamMemberRole);
        testSubStatementActor(teamMemberAgent);
        testResultResponse(highlightObjects.getName());
        testTeamStructure(false, true);
        // highlight context extension
        testExtension(highlightObjects);
        // with stress
        configureProcessor(adaptation, -0.5);
        generateStatement();
        testPcCondition();
        // -> NPC as ref
        teamMemberRef.setEntityMarking(npcMemberRole);
        teamMemberRef.setValue(npcMemberRole);
        highlightObjects.setType(teamMemberRef);
        adaptation.setType(highlightObjects);
        testStatementBasic(highlightObjectsActivity, npcMemberRole);
        testSubStatementActor(npcAgent);
        testResultResponse(highlightObjects.getName());
        testTeamStructure(true, false);
        // highlight context extension
        testExtension(highlightObjects);
        // with stress
        configureProcessor(adaptation, -0.25);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testRemoveHighlight() throws Exception {
        // adaptation
        removeHighlightOnObjects.setHighlightName("highlight name");
        adaptation.setType(removeHighlightOnObjects);
        testStatementBasic(removeHighlightActivity);
        testResultResponse(removeHighlightOnObjects.getHighlightName());
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.0);
        generateStatement();
        testPcCondition();
    }
    
    private void testExtension(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo) throws Exception {
        parseExtension(locationInfoExt);
        Assert.assertEquals(locationInfo.getPlaceOfInterestRef(), getNodeField(atExt, ItsResultExtensionConcepts.extensionObjectKeys.PLACE_OF_INTEREST).asText());
        if(locationInfo.getCoordinate() != null) {
            testCoordinateExtension(getNodeField(atExt, ItsResultExtensionConcepts.extensionObjectKeys.COORDINATE), locationInfo.getCoordinate());
        }
    }
    
    @Test
    public void testBreadcrumbs() throws Exception {
        // adaptation
        // location info
        EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = new EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo();
        // -> list of coordinates
        List<Coordinate> coordinates = locationInfo.getCoordinate();
        Coordinate coordinateOne = createGCC();
        Coordinate coordinateTwo = createGDC();
        Coordinate coordinateThree = createAGL();
        // -> place of interest
        locationInfo.setPlaceOfInterestRef("point of interest ref");
        // list of team member refs
        List<EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef> teamMemberRefs = createBreadcrumbs.getTeamMemberRef();
        EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef actorRef = new EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef();
        actorRef.setEntityMarking(studentHostRole);
        actorRef.setValue(studentHostRole);
        EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef sessionMemberRef = new EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef();
        sessionMemberRef.setEntityMarking(teamMemberRole);
        sessionMemberRef.setValue(teamMemberRole);
        EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef npcRef = new EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef();
        npcRef.setEntityMarking(npcMemberRole);
        npcRef.setValue(npcMemberRole);
        // permutations
        // -> Single ref points to Actor
        teamMemberRefs.add(actorRef);
        coordinates.add(coordinateOne);
        createBreadcrumbs.setLocationInfo(locationInfo);
        createBreadcrumbs.setStressCategory(StrategyStressCategory.COGNITIVE);
        adaptation.setType(createBreadcrumbs);
        testStatementBasic(createBreadcrumbsActivity);
        testExtension(locationInfo);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.25);
        generateStatement();
        testPcCondition();
        // -> Single ref points to session member
        teamMemberRefs.clear();
        coordinates.clear();
        teamMemberRefs.add(sessionMemberRef);
        coordinates.add(coordinateTwo);
        createBreadcrumbs.setLocationInfo(locationInfo);
        adaptation.setType(createBreadcrumbs);
        testStatementBasic(createBreadcrumbsActivity, teamMemberRole);
        testExtension(locationInfo);
        testSubStatementActor(teamMemberAgent);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.5);
        generateStatement();
        testPcCondition();
        // -> Single ref points to NPC
        teamMemberRefs.clear();
        coordinates.clear();
        teamMemberRefs.add(npcRef);
        coordinates.add(coordinateThree);
        createBreadcrumbs.setLocationInfo(locationInfo);
        adaptation.setType(createBreadcrumbs);
        testStatementBasic(createBreadcrumbsActivity, npcMemberRole);
        testExtension(locationInfo);
        testSubStatementActor(npcAgent);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, 0.75);
        generateStatement();
        testPcCondition();
        // -> Many ref, Actor + Session Member
        teamMemberRefs.clear();
        coordinates.clear();
        teamMemberRefs.add(actorRef);
        teamMemberRefs.add(sessionMemberRef);
        coordinates.add(coordinateOne);
        coordinates.add(coordinateTwo);
        createBreadcrumbs.setLocationInfo(locationInfo);
        adaptation.setType(createBreadcrumbs);
        testStatementBasic(createBreadcrumbsActivity, teamMemberRole);
        testExtension(locationInfo);
        testSubStatementActor(studentTeamMemberGroup);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 1.0);
        generateStatement();
        testPcCondition();
        // -> Many ref, Actor + NPC
        teamMemberRefs.clear();
        coordinates.clear();
        teamMemberRefs.add(actorRef);
        teamMemberRefs.add(npcRef);
        coordinates.add(coordinateOne);
        coordinates.add(coordinateThree);
        createBreadcrumbs.setLocationInfo(locationInfo);
        adaptation.setType(createBreadcrumbs);
        testStatementBasic(createBreadcrumbsActivity, npcMemberRole);
        testExtension(locationInfo);
        testSubStatementActor(studentNpcGroup);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, 0.75);
        generateStatement();
        testPcCondition();
        // -> Many ref, Session Member + NPC
        teamMemberRefs.clear();
        coordinates.clear();
        teamMemberRefs.add(sessionMemberRef);
        teamMemberRefs.add(npcRef);
        coordinates.add(coordinateTwo);
        coordinates.add(coordinateThree);
        createBreadcrumbs.setLocationInfo(locationInfo);
        adaptation.setType(createBreadcrumbs);
        List<String> roleNames = new ArrayList<String>(2);
        roleNames.add(npcMemberRole);
        roleNames.add(teamMemberRole);
        testStatementBasic(createBreadcrumbsActivity, roleNames);
        testExtension(locationInfo);
        testSubStatementActor(teamMemberNpcGroup);
        testTeamStructure(true, true);
        // with stress
        configureProcessor(adaptation, 0.5);
        generateStatement();
        testPcCondition();
    }
    
    @Test
    public void testRemoveBreadcrumbs() throws Exception {
        // adaptation
        // list of team member refs
        List<EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef> teamMemberRefs = removeBreadcrumbs.getTeamMemberRef();
        EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef actorRef = new EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef();
        actorRef.setEntityMarking(studentHostRole);
        actorRef.setValue(studentHostRole);
        EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef sessionMemberRef = new EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef();
        sessionMemberRef.setEntityMarking(teamMemberRole);
        sessionMemberRef.setValue(teamMemberRole);
        EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef npcRef = new EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef();
        npcRef.setEntityMarking(npcMemberRole);
        npcRef.setValue(npcMemberRole);
        // permutations
        // -> Single ref points to Actor
        teamMemberRefs.add(actorRef);
        adaptation.setType(removeBreadcrumbs);
        testStatementBasic(removeBreadcrumbsActivity);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.25);
        generateStatement();
        testPcCondition();
        // -> Single ref points to session member
        teamMemberRefs.clear();
        teamMemberRefs.add(sessionMemberRef);
        adaptation.setType(removeBreadcrumbs);
        testStatementBasic(removeBreadcrumbsActivity, teamMemberRole);
        testSubStatementActor(teamMemberAgent);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, 0.0);
        generateStatement();
        testPcCondition();
        // -> Single ref points to NPC
        teamMemberRefs.clear();
        teamMemberRefs.add(npcRef);
        adaptation.setType(removeBreadcrumbs);
        testStatementBasic(removeBreadcrumbsActivity, npcMemberRole);
        testSubStatementActor(npcAgent);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, -0.25);
        generateStatement();
        testPcCondition();
        // -> Many ref, Actor + Session Member
        teamMemberRefs.clear();
        teamMemberRefs.add(actorRef);
        teamMemberRefs.add(sessionMemberRef);
        adaptation.setType(removeBreadcrumbs);
        testStatementBasic(removeBreadcrumbsActivity, teamMemberRole);
        testSubStatementActor(studentTeamMemberGroup);
        testTeamStructure(false, true);
        // with stress
        configureProcessor(adaptation, -0.5);
        generateStatement();
        testPcCondition();
        // -> Many ref, Actor + NPC
        teamMemberRefs.clear();
        teamMemberRefs.add(actorRef);
        teamMemberRefs.add(npcRef);
        adaptation.setType(removeBreadcrumbs);
        testStatementBasic(removeBreadcrumbsActivity, npcMemberRole);
        testSubStatementActor(studentNpcGroup);
        testTeamStructure(true, false);
        // with stress
        configureProcessor(adaptation, -0.75);
        generateStatement();
        testPcCondition();
        // -> Many ref, Session Member + NPC
        teamMemberRefs.clear();
        teamMemberRefs.add(sessionMemberRef);
        teamMemberRefs.add(npcRef);
        adaptation.setType(removeBreadcrumbs);
        List<String> roleNames = new ArrayList<String>(2);
        roleNames.add(npcMemberRole);
        roleNames.add(teamMemberRole);
        testStatementBasic(removeBreadcrumbsActivity, roleNames);
        testSubStatementActor(teamMemberNpcGroup);
        testTeamStructure(true, true);
        // with stress
        configureProcessor(adaptation, -1.0);
        generateStatement();
        testPcCondition();
    }
}
