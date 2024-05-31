/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import generated.dkf.Area;
import generated.dkf.BooleanEnum;
import generated.dkf.Concept;
import generated.dkf.ConceptAssessment;
import generated.dkf.ConceptEnded;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Coordinate;
import generated.dkf.EndTriggers;
import generated.dkf.EntityLocation;
import generated.dkf.EntityLocation.EntityId.TeamMemberRef;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.GDC;
import generated.dkf.InTutor;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Message;
import generated.dkf.ObservedAssessmentCondition;
import generated.dkf.Path;
import generated.dkf.Point;
import generated.dkf.PointRef;
import generated.dkf.Scenario;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.ScenarioStarted;
import generated.dkf.Segment;
import generated.dkf.StartTriggers;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.StrategyHandler;
import generated.dkf.Task;
import generated.dkf.TaskEnded;
import generated.dkf.Tasks;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamOrganization;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.course.dkf.BasicDKFHandler;
import mil.arl.gift.common.enums.MessageFeedbackDisplayModeEnum;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.tools.services.file.XTSPImporter;

/**
 * A jUnit test for the XTSP Import process.
 * This does not edit any existing files or create any new files. It tests that an XTSP file can be parsed,
 * and that the resulting Scenario object is 
 * 
 * @author mcambata
 *
 */
public class XTSPImportTest {
    /** The path of the base DKF file to read in when creating a scenario */
    private static final String DKF_PATH = "data" + File.separator + "tests" + File.separator + "squadDesertExercise.dkf.xml";
    
    /** The path of the XTSP file to read and parse in order to modify the scenario from the DKF */
    private static final String XTSP_PATH = "data" + File.separator + "tests" + File.separator + "xtsp-draft-v0.9.7.2.1-unit-test.json";
    
    /*
     * The following static final Strings are used to verify the reference XTSP file is being read correctly. If it is changed,
     * they may need to be updated.
     */
    /** BLUFOR-1 team information in the XTSP file that will be validated in the unit test. */
    private static final String BLUFOR_1_TEAM_NAME = "West (BLUFOR)";
    
    private static final String[] BLUFOR_1_TEAM_NAME_LIST = {"BLUFOR Team 1", "BLUFOR Team 1-A", "BLUFOR Team 1-A-1-1", 
            "BLUFOR Team 1-A-1-1-A", "BLUFOR Team 1-A-1-1-B"};

    private static final String[] BLUFOR_1_ECHELON_LIST = {"", "Platoon", "Squad", 
            "Fireteam", "Brigade"};
    
    private static final String[] BLUFOR_1_TEAM_MEMBER_NAME_LIST = {"BLUFOR Role A", "BLUFOR Role B", "BLUFOR Role C", 
            "BLUFOR Role D", "BLUFOR Role E", "BLUFOR Role F", "BLUFOR Role G", "BLUFOR Role H", "BLUFOR Role I"};
    
    private static final String[] BLUFOR_1_LEARNER_ID_LIST = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    
    private static final boolean[] BLUFOR_1_PLAYABLE_LIST = {true, true, true, true, true, false, false, false, false};
    
    /** OPFOR-1 team information in the XTSP file that will be validated in the unit test. */
    private static final String OPFOR_1_TEAM_NAME = "East (OPFOR)";
    
    private static final String[] OPFOR_1_TEAM_NAME_LIST = {"OPFOR Team 1", "OPFOR Team 1-1-1-1", "OPFOR Team 1-1-1-1-1"};

    private static final String[] OPFOR_1_ECHELON_LIST = {"", "Division", "Company"};
    
    private static final String[] OPFOR_1_TEAM_MEMBER_NAME_LIST = {"OPFOR Role A", "OPFOR Role B", "OPFOR Role C", 
            "OPFOR Role D"};
    
    private static final String[] OPFOR_1_LEARNER_ID_LIST = {"10", "11", "12", "13"};
    
    private static final boolean[] OPFOR_1_PLAYABLE_LIST = {false, false, false, false};
     
    
    /** Admin-1 (Game Master) team information in the XTSP file that will be validated in the unit test. */
    private static final String ADMIN_1_TEAM_NAME = "Game-Master (ADMIN)";
    
    private static final String[] ADMIN_1_TEAM_NAME_LIST = {"BLUFOR Team 1-A-1-1 (1)", "BLUFOR Team 1-A-1-1-A (1)", "BLUFOR Team 1-A-1-1-B (1)"};

    private static final String[] ADMIN_1_ECHELON_LIST = {"Squad", "Fireteam", "Brigade"};
    
    private static final String[] ADMIN_1_LEARNER_ID_LIST = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    
    private static final boolean[] ADMIN_1_PLAYABLE_LIST = {true, true, true, true, true, false, false, false, false};
    
    /** The string associated with the point of interest's name in this scenario. */
    private static final String POINT_OF_INTEREST_NAME = "Test Point";
    /** The latitude value of the point of interest */
    private static final String POINT_OF_INTEREST_LATITUDE = "40.122576";
    /** The longitude value of the point of interest */
    private static final String POINT_OF_INTEREST_LONGITUDE = "-150.457684";
    /** The string associated with the path of interest's name in this scenario. */
    private static final String PATH_OF_INTEREST_NAME = "Test Path";
    /** The latitude values of the path of interest points */
    private static final String[][] PATH_OF_INTEREST_LAT_LONGS = {{"40.548575", "-150.128675"}, {"40.786594", "-150.575849"}, {"40.550987", "-150.765786"}, 
            {"40.800987", "-150.874324"}};
    /** The string associated with the area of interest's name in this scenario. */
    private static final String AREA_OF_INTEREST_NAME = "Test Area";
    /** The latitude values of the path of interest points */
    private static final String[][] AREA_OF_INTEREST_LAT_LONGS = {{"40.125423", "-150.453786"}, {"40.356876", "-150.569807"}, {"40.568498", "-150.689754"}, 
            {"40.689056", "-150.895764"}};
    
    /** Task names that will be validated by the unit test. */
    private static final String[] TASK_NAMES = {"Task 1", "Task 2", "Task 3 (No Concepts)"};
    
    /** Task stress values that will be validated by the unit test. */
    private static final double[] TASK_STRESS = {1.0, 0.5, 0.1};
    
    /** Task difficulty values that will be validated by the unit test. */
    private static final long[] TASK_DIFFICULTY = {3L, 2L, 1L};
    
    /** Nested concepts that appear in the Tasks that will be validated by the unit test. */
    private static final String[] CONCEPT_NAMES = {"Task Work", "Task 1, Concept 1", "Task 1, Concept 1-A", "Task 1, Concept 1-B", "Task 1, Concept 1-C", 
            "Task 1, Concept 2", "Task 1, Concept 2-A", "Task 1, Concept 2-B", "Task 1, Concept 2-C", "Task 1, Concept 2-D", "Task Work (1)", 
            "Task 2, Concept 1", "Task 2, Concept 1-A", "Task 2, Concept 1-B", "Task 2, Concept 1-C", "Task 2, Concept 1-D", "Task 2, Concept 2", "Task 2, Concept 2-A",
            "Task 2, Concept 2-B", "Task 2, Concept 2-C", "Task 2, Concept 2-D", "Task 2, Concept 2-E", "Task 2, Concept 3", "Task 2, Concept 3-A", "Task 2, Concept 3-B",
            "Task 2, Concept 3-C", "Task 2, Concept 3-D"};
    
    /** End Triggers information inside the xTSP that will be validated by the unit test. */
    private static final String[] END_TRIGGERS_ASSESSMENTS = {"AboveExpectation", "AtExpectation", "BelowExpectation", "AboveExpectation", "AtExpectation", "BelowExpectation"};
    
    private static final String END_TRIGGERS_TEAM_MEMBER_REFS = "BLUFOR Role A";
    
    private static final String END_TRIGGERS_POINT_REF = "Test Point";
    
    private static final double END_TRIGGERS_DISTANCE = 7.0;
    
    private static int CONCEPT_NODE_ID = 1;
    
    private static final int[] NODE_IDS_ARRAY = {11, 29, 30};
    
    private static final String CONDITION_IMPL_NAME = "domain.knowledge.condition.ObservedAssessmentCondition";
    
    private static final String STRATEGY_IMPL_NAME = "domain.knowledge.strategy.DefaultStrategyHandler";

    private static final BooleanEnum[] BOOLEAN_ENUM_ARRAY = {BooleanEnum.FALSE, BooleanEnum.FALSE, BooleanEnum.FALSE, BooleanEnum.TRUE, BooleanEnum.FALSE, BooleanEnum.FALSE, BooleanEnum.FALSE};
    
    private static final String[] TEXT_ENHANCEMENT_ENUM_ARRAY = {TextFeedbackDisplayEnum.NO_EFFECT.getName(), TextFeedbackDisplayEnum.NO_EFFECT.getName(), TextFeedbackDisplayEnum.NO_EFFECT.getName(), 
            TextFeedbackDisplayEnum.NO_EFFECT.getName(), TextFeedbackDisplayEnum.NO_EFFECT.getName(), TextFeedbackDisplayEnum.FLASH_ONLY.getName(), TextFeedbackDisplayEnum.FLASH_ONLY.getName()};

    private static final String SCRIPT_COMMAND = "createVehicle LE1";
    
    private static final int TASK_ENDED_NODE_ID = 11;
    
    private static final int CONCEPT_ENDED_NODE_ID = 18;
    
    private static final int CONCEPT_ASSESSMENT_VALUE = 24;
    
    /** The scale of the BigDecimal used to check the places of interest values. */
    private static int PLACE_OF_INTEREST_SCALE = 6;
    
    /** Indexes that are used to iterate through the array of information provided from the xTSP. */
    private static int currentTeamIndex = 0;
    
    private static int currentTeamMemberIndex = 0;
    
    private static int conceptsIndex = 0;
    
    private static int tasksIndex = 0;
    
    private static int assessmentsIndex = 0;
    
    private static int strategyIndex = 0;

    /** A string that will provide a detailed reason as to why the unit test failed. */
    private static String reason = null;
    
    @Ignore
    @Test
    public void testImportXTSP() throws IllegalArgumentException, Exception {
        File dkfFile = new File(DKF_PATH);
        BasicDKFHandler schemaHandler = new BasicDKFHandler(new FileProxy(dkfFile));
        
        Scenario xTSPScenario = (Scenario) schemaHandler.parseAndValidate(Scenario.class, dkfFile, true).getUnmarshalled();
        Set<String> allowedCoordinateTypes = new HashSet<String>();
        allowedCoordinateTypes.add("GDC");
        
        /* Use the XTSPImporter to read in the XTSP file, and modify the xTSPScenario with its data. */
        XTSPImporter importer = new XTSPImporter();
        importer.importXtspIntoDkf("default", 
                xTSPScenario, null, new FileProxy(new File(XTSP_PATH)), allowedCoordinateTypes);
        
        /* 
         * Check that the modified xTSPScenario has expected data as a result of being modified by the DKF.
         */
        TeamOrganization baseTeamOrg = xTSPScenario.getTeamOrganization();
        
        List<Serializable> baseTeamOrTeamMemberList = baseTeamOrg.getTeam().getTeamOrTeamMember();
        
        /* Check the root teams: BLUFOR-1, OPFOR-1, and ADMIN-1. */
        boolean isBlufor1TeamPresent = false;
        boolean isOpfor1TeamPresent = false;
        boolean isAdmin1TeamPresent = false;
        
        List<Serializable> blufor1TeamOrTeamMemberList = null;
        List<Serializable> opfor1TeamOrTeamMemberList = null;
        List<Serializable> admin1TeamOrTeamMemberList = null;
        
        for (Serializable teamOrMemberToCheck : baseTeamOrTeamMemberList) {
            if (teamOrMemberToCheck instanceof Team) {
                Team teamToCheck = (Team) teamOrMemberToCheck;
                if (teamToCheck.getName().equals(BLUFOR_1_TEAM_NAME)) {
                    isBlufor1TeamPresent = true;
                    blufor1TeamOrTeamMemberList = ((Team) teamOrMemberToCheck).getTeamOrTeamMember();
                } else if (teamToCheck.getName().equals(OPFOR_1_TEAM_NAME)) {
                    isOpfor1TeamPresent = true;
                    opfor1TeamOrTeamMemberList = ((Team) teamOrMemberToCheck).getTeamOrTeamMember();
                } else if (teamToCheck.getName().equals(ADMIN_1_TEAM_NAME)) {
                    isAdmin1TeamPresent = true;
                    admin1TeamOrTeamMemberList = ((Team) teamOrMemberToCheck).getTeamOrTeamMember();
                }
            }
        }
        
        /* Validate that root teams were present within the xTSP file. */
        Assert.assertTrue("Root team BLUFOR-1 not imported correctly from XTSP file.", isBlufor1TeamPresent);
        Assert.assertTrue("Root team OPFOR-1 not imported correctly from XTSP file.", isOpfor1TeamPresent);
        Assert.assertTrue("Root team ADMIN-1 not imported correctly from XTSP file", isAdmin1TeamPresent);

        /* Check if the child BLUFOR team is present within the root team. */
        boolean isBlufor1_1TeamPresent = checkTeamOrTeamMember(blufor1TeamOrTeamMemberList, new HashSet<>(), BLUFOR_1_TEAM_NAME_LIST, BLUFOR_1_ECHELON_LIST, 
                BLUFOR_1_TEAM_MEMBER_NAME_LIST, BLUFOR_1_LEARNER_ID_LIST, BLUFOR_1_PLAYABLE_LIST);
        currentTeamIndex = currentTeamMemberIndex = 0;
        
        /* Check if the child OPFOR team is present within the root team. */
        boolean isOpfor1_1TeamPresent = checkTeamOrTeamMember(opfor1TeamOrTeamMemberList, new HashSet<>(), OPFOR_1_TEAM_NAME_LIST, OPFOR_1_ECHELON_LIST, 
                OPFOR_1_TEAM_MEMBER_NAME_LIST, OPFOR_1_LEARNER_ID_LIST, OPFOR_1_PLAYABLE_LIST);
        currentTeamIndex = currentTeamMemberIndex = 0;
        
        /* Check if the child ADMIN team is present within the root team. */
        boolean isAdmin1_1TeamPresent = checkTeamOrTeamMember(admin1TeamOrTeamMemberList, new HashSet<>(), ADMIN_1_TEAM_NAME_LIST, ADMIN_1_ECHELON_LIST, 
                null, ADMIN_1_LEARNER_ID_LIST, ADMIN_1_PLAYABLE_LIST);
        currentTeamIndex = currentTeamMemberIndex = 0;
        
        /* Validate that child teams were present within the xTSP file. */
        Assert.assertTrue("Child team(s) BLUFOR-1-1 not imported correctly from XTSP file", isBlufor1_1TeamPresent);
        Assert.assertTrue("Child team(s) OPFOR-1-1 not imported correctly from XTSP file", isOpfor1_1TeamPresent);
        Assert.assertTrue("Child team(s) ADMIN-1-1 not imported correctly from XTSP file", isAdmin1_1TeamPresent);
    
        boolean isPointPresent = false;
        boolean isPathPresent = false;
        boolean isAreaPresent = false;
        
        List<Serializable> placesOfInterest = xTSPScenario.getAssessment().getObjects().getPlacesOfInterest().getPointOrPathOrArea();
        
        for (Serializable placeToCheck : placesOfInterest) {
            if (placeToCheck instanceof Point) {
                isPointPresent = checkPoint((Point) placeToCheck);
            } else if (placeToCheck instanceof Path) {
                isPathPresent = checkPath((Path) placeToCheck);
            } else if (placeToCheck instanceof Area) {
                isAreaPresent = checkArea((Area) placeToCheck);
            } else {
                /* The object to check does not contain either of the supported values to check. */
                reason = "The object " + placeToCheck + " imported from the xTSP does not contain a Point, Path, or Area object to check.";
            }
        }
        
        Assert.assertTrue("Point of Interest not imported correctly from XTSP file. Reason: " + reason, isPointPresent);
        Assert.assertTrue("Path of Interest not imported correctly from XTSP file. Reason: " + reason, isPathPresent);
        Assert.assertTrue("Area of Interest not imported correctly from XTSP file. Reason: " + reason, isAreaPresent);
        
        Tasks tasks = xTSPScenario.getAssessment().getTasks();
     
        boolean isValidTasksConcepts = checkTasks(tasks);
        Assert.assertTrue("Tasks and/or Concepts not imported correctly from XTSP file. Reason: " + reason, isValidTasksConcepts);
    }
    
    /**
     * Check if the team imported from the xTSP file contains nested layers of sub-teams or team members. This is 
     * a recursive method that makes use of the Team tree-like structure to visit team nodes in a depth-first manner. 
     * 
     * @param teamOrTeamMemberList the list of teams or team members that will be visited for checking.
     * 
     * @param checked a set that keeps track of the visited sub-teams/team members to ensure that no team is visited
     *                more than once.
     *                
     * @param teamNameArray an array of team names, in order, that correspond to the sub-teams/team members to check 
     * 
     * @param teamEchelonArray an array of team echelons, in order, that correspond to the sub-teams/team members to check
     * 
     * @param teamMemberNameArray an array of team member names, in order, that correspond to the sub-teams/team members to check
     * 
     * @param teamMemberLearnerIdArray an array of team member learner ids, in order, that correspond to the sub-teams/team members 
     *        to check
     * 
     * @param teamMemberPlayableArray an array of team member playable values, in order, that correspond to the sub-teams/team members 
     *        to check
     * 
     * @return a value indicating whether the sub-teams were imported properly with all information present.
     */
    private boolean checkTeamOrTeamMember(List<Serializable> teamOrTeamMemberList, Set<Serializable> checked, String[] teamNameArray, String[] teamEchelonArray, 
            String[] teamMemberNameArray, String[] teamMemberLearnerIdArray, boolean[] teamMemberPlayableArray) {
        /* Base case - we've descended to the end of the tree. */
        if (CollectionUtils.isEmpty(teamOrTeamMemberList)) {
            return true;
        }
        
        /* For now, assume that the xTSP import process successfully preserved all information. */
        boolean isSuccess = true;
        
        for (Serializable teamOrMemberToCheck : teamOrTeamMemberList) {       
            /* If we visited this team or team member already, continue past it. */
            if (checked.contains(teamOrMemberToCheck)) {
                continue;
            }
            
            /* Add the current team or team member that's being checked to the set of running teams/team members. */
            checked.add(teamOrMemberToCheck);
            
            if (teamOrMemberToCheck instanceof Team) {
                Team teamToCheck = (Team) teamOrMemberToCheck;
                String teamToCheckName = teamToCheck.getName();
                String teamToCheckEchelon = teamToCheck.getEchelon() == null ? "" : teamToCheck.getEchelon();
                
                /* 
                 * Check if the current team/team member name and echelon are equal to the array of names and 
                 * echelons at the current index. If at any point the checks fail, then the team wasn't imported 
                 * correctly, returning false.
                 * 
                 *  Ex: true & true & true & false --> false
                 */
                isSuccess &= teamToCheckName.equals(teamNameArray[currentTeamIndex]) &&
                        teamToCheckEchelon.equals(teamEchelonArray[currentTeamIndex++]) && 
                        checkTeamOrTeamMember(((Team) teamOrMemberToCheck).getTeamOrTeamMember(), checked, teamNameArray, teamEchelonArray, teamMemberNameArray,
                                teamMemberLearnerIdArray, teamMemberPlayableArray);
            } else if (teamOrMemberToCheck instanceof TeamMember) {
                TeamMember memberToCheck = (TeamMember) teamOrMemberToCheck;
                String memberToCheckName = memberToCheck.getName();
                String memberToCheckLearnerId = (String) memberToCheck.getLearnerId().getType();
                boolean isPlayable = memberToCheck.isPlayable();
                
                /* 
                 * A valid decoding of a team member means that the team member name, learner id, and playable values 
                 * are equal to the corresponding arrays.
                 */
                isSuccess &= memberToCheckName.equals(teamMemberNameArray[currentTeamMemberIndex]) && 
                        memberToCheckLearnerId.equals(teamMemberLearnerIdArray[currentTeamMemberIndex]) &&
                        isPlayable == teamMemberPlayableArray[currentTeamMemberIndex++];
            } else {
                /* The team doesn't contain a team or team member, which renders the xTSP team import invalid. */
                reason = "The team " + teamOrMemberToCheck + " does not contain a valid team or team member to check.";
                return false;
            }
        }
        
        /* Return the value after checking all nested sub-teams and/or team members. */
        return isSuccess;    
    }
    
    /**
     * Check if the Point object imported from the xTSP file contains the expected information inside the 
     * DKF object. 
     * 
     * @param point the Point to check
     * @return a value indicating whether the Point is valid or not
     */
    private boolean checkPoint(Point point) {
        if (point == null) {
            reason = "The point is null.";
            return false;
        }

        String pointName = point.getName();
        
        /* If the point name in the DKF is equal to the one from the xTSP, check the latitude and longitude 
         * values of the point. 
         */
        if (pointName.equals(POINT_OF_INTEREST_NAME)) {
            GDC gdcCoord = (GDC) point.getCoordinate().getType();

            BigDecimal latitudeValue = gdcCoord.getLatitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
            BigDecimal longitudeValue = gdcCoord.getLongitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
            
            boolean isLatitudeValuesEq = latitudeValue.compareTo(new BigDecimal(POINT_OF_INTEREST_LATITUDE).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) == 0;
            boolean isLongitudeValuesEq = longitudeValue.compareTo(new BigDecimal(POINT_OF_INTEREST_LONGITUDE).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) == 0;
            
            /* The point is valid only if the latitude and longitude values are equal. */
            return isLatitudeValuesEq && isLongitudeValuesEq;
        } else {
            reason = "The point name imported from the xTSP " + pointName + " is not equal to the expected name value " + POINT_OF_INTEREST_NAME + ".";
            return false;
        }
    }
    
    /**
     * Check if the Path object imported from the xTSP file contains the expected information inside the 
     * DKF object. 
     * 
     * @param path the Path object to check
     * @return a value indicating whether the path is valid or not
     */
    private boolean checkPath(Path path) {
        if (path == null) {
            reason = "The path is null.";
            return false;
        }
        
        String pathName = path.getName();
        
        if (pathName.equals(PATH_OF_INTEREST_NAME)) {
            List<Segment> segmentList = path.getSegment();
            
            /* A path is constructed of a segment of points that represent start and end values to check. Verify
             * that the start points and end points are equal by checking the latitude and longitude of each. 
             */
            for (int i = 0; i + 1 <= segmentList.size(); i++) {
                Segment segment = segmentList.get(i);
                
                GDC startGdcCoord = (GDC) segment.getStart().getCoordinate().getType();
                GDC endGdcCoord = (GDC) segment.getEnd().getCoordinate().getType();
                
                BigDecimal startLatitude = startGdcCoord.getLatitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
                BigDecimal startLongitude = startGdcCoord.getLongitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
                BigDecimal endLatitude = endGdcCoord.getLatitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
                BigDecimal endLongitude = endGdcCoord.getLongitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
                
                boolean isStartLatitudeValuesEq = startLatitude.compareTo(new BigDecimal(PATH_OF_INTEREST_LAT_LONGS[i][0]).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) == 0;
                boolean isStartLongitudeValuesEq = startLongitude.compareTo(new BigDecimal(PATH_OF_INTEREST_LAT_LONGS[i][1]).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) == 0;
                boolean isEndLatitudeValuesEq = endLatitude.compareTo(new BigDecimal(PATH_OF_INTEREST_LAT_LONGS[i + 1][0]).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) == 0;
                boolean isEndLongitudeValuesEq = endLongitude.compareTo(new BigDecimal(PATH_OF_INTEREST_LAT_LONGS[i + 1][1]).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) == 0;
                
                /* The path is valid only if the latitude and longitude values of all start and end are equal. */
                if (!isStartLatitudeValuesEq || !isStartLongitudeValuesEq || !isEndLatitudeValuesEq || !isEndLongitudeValuesEq) {
                    return false;
                }
            }
        } else {
            /* Somewhere down the line, the import process failed to produce a valid path. Fail the test. */
            reason = "The path name imported from the xTSP " + pathName + " is not equal to the expected name value " + PATH_OF_INTEREST_NAME + ".";
            return false;
        }

        return true;
    }
    
    /**
     * Check if the Area object imported from the xTSP file contains the expected information inside the 
     * DKF object. 
     * 
     * @param area the Area object to check
     * @return a value indicating whether the area is valid or not
     */
    private boolean checkArea(Area area) {
        if (area == null) {
            reason = "The area is null.";
            return false;
        }
        
        String areaName = area.getName();
        
        if (areaName.equals(AREA_OF_INTEREST_NAME)) {
            List<Coordinate> coordinates = area.getCoordinate();

            for (int i = 0; i < coordinates.size(); i++) {
                GDC currentGdcCoord = (GDC) coordinates.get(i).getType();
                
                BigDecimal currentLatitude = currentGdcCoord.getLatitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
                BigDecimal currentLongitude = currentGdcCoord.getLongitude().setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP);
                
                /* If any of the coordinates are not equal to the expected values, we've found a mismatch. */
                if (currentLatitude.compareTo(new BigDecimal(AREA_OF_INTEREST_LAT_LONGS[i][0]).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) != 0 
                        || currentLongitude.compareTo(new BigDecimal(AREA_OF_INTEREST_LAT_LONGS[i][1]).setScale(PLACE_OF_INTEREST_SCALE, RoundingMode.HALF_UP)) != 0) {
                    reason = "The coordinates within the Area oject are not equal to the expected values.";
                    return false;
                }
            }
        }

        return true;
    }
    
    /**
     * This method checks that the Tasks imported into the DKF contain all the necessary information from the 
     * xTSP file.
     * 
     * @param tasks the DKF Tasks object containing the information from the xTSP
     * @return a value indicating whether the tasks are valid or not 
     */
    private boolean checkTasks(Tasks tasks) {
        if (tasks == null) {
            reason = "The tasks are null.";
            return false;
        }
        
        boolean isValidTasks = true;
        List<Task> taskList = tasks.getTask();
        
        /* For each task inside the list of tasks, check that the task name, node ID, start triggers, end triggers, 
         * conditions, and concepts are valid according to the xTSP.
         */
        for (Task task : taskList) {
            String taskName = task.getName();
            int nodeId = task.getNodeId().intValue();
            double stress = task.getStressMetric().getValue().doubleValue();
            long difficulty = task.getDifficultyMetric().getValue().longValue();
            
            isValidTasks &= taskName.equals(TASK_NAMES[tasksIndex]) && nodeId == NODE_IDS_ARRAY[tasksIndex] && 
                  stress == TASK_STRESS[tasksIndex] && difficulty == TASK_DIFFICULTY[tasksIndex++] && 
                  checkStartTriggers(task.getStartTriggers()) && checkEndTriggers(task.getEndTriggers()) &&
                  checkConditionsOrConcepts(task.getConcepts());
            CONCEPT_NODE_ID++;
        }
        
        /* Return the value after checking. */
        return isValidTasks;
    }
    
    /**
     * Check that the StartTriggers imported into the DKF contains all the necessary information from the xTSP file.
     * 
     * @param startTriggers the DKF StartTriggers object containing the information from the xTSP
     * @return a value indicating whether the start triggers are valid or not
     */
    private boolean checkStartTriggers(StartTriggers startTriggers) {
        if (startTriggers == null) {
            reason = "The start triggers are null.";
            return false;
        }
        
        boolean isStartTriggers = true;
        List<StartTriggers.Trigger> startTriggersList = startTriggers.getTrigger();
        
        for (StartTriggers.Trigger startTrigger : startTriggersList) {    
            StartTriggers.Trigger.TriggerMessage message = startTrigger.getTriggerMessage();
            
            if (message != null) {
                isStartTriggers &= checkStrategy(message.getStrategy()) && checkTriggerType(startTrigger.getTriggerType());
            } else {
                isStartTriggers &= checkTriggerType(startTrigger.getTriggerType());
            } 
        }
        
        return isStartTriggers;
    }
    
    /**
     * Check that the EndTriggers imported into the DKF contains all the necessary information from the xTSP file.
     * 
     * @param endTriggers the DKF EndTriggers object containing the information from the xTSP
     * @return a value indicating whether the end triggers are valid or not
     */
    private boolean checkEndTriggers(EndTriggers endTriggers) {
        if (endTriggers == null) {
            reason = "The end triggers are null.";
            return false;
        }
        
        boolean isEndTriggers = true;
        List<EndTriggers.Trigger> endTriggersList = endTriggers.getTrigger();
        
        for (EndTriggers.Trigger endTrigger : endTriggersList) {  
            EndTriggers.Trigger.Message message = endTrigger.getMessage();
            
            if (message != null) {
                isEndTriggers &= checkStrategy(message.getStrategy()) && checkTriggerType(endTrigger.getTriggerType());
            } else {
                isEndTriggers &= checkTriggerType(endTrigger.getTriggerType());
            }  
        }
        
        return isEndTriggers;
    }
    
    /**
     * Check that the set of strategies imported into the DKF are correct according to the xTSP file. 
     * 
     * @param strategy the DKF Strategy object to check 
     * @return a value indicating whether the strategy is valid
     */
    private boolean checkStrategy(Strategy strategy) {
        if (strategy == null) {
            reason = "The strategy is null.";
            return false;
        }
        
        boolean isStrategy = true;
        List<Serializable> activities = strategy.getStrategyActivities();
        
        for (Serializable activity : activities) {
            if (activity instanceof ScenarioAdaptation) {
                ScenarioAdaptation scenarioAdaptation = (ScenarioAdaptation) activity;
                StrategyHandler handler = scenarioAdaptation.getStrategyHandler();
                
                Serializable type = scenarioAdaptation.getEnvironmentAdaptation().getType();
                
                if (type instanceof EnvironmentAdaptation.TimeOfDay) {
                    EnvironmentAdaptation.TimeOfDay timeOfDay = (EnvironmentAdaptation.TimeOfDay) type;
                    Serializable timeOfDayType = timeOfDay.getType();
                    isStrategy &= (timeOfDayType instanceof EnvironmentAdaptation.TimeOfDay.Dawn || 
                            timeOfDayType instanceof EnvironmentAdaptation.TimeOfDay.Dusk || 
                            timeOfDayType instanceof EnvironmentAdaptation.TimeOfDay.Midday ||
                            timeOfDayType instanceof EnvironmentAdaptation.TimeOfDay.Midnight);
                } else if (type instanceof EnvironmentAdaptation.Script) {
                    isStrategy &= ((EnvironmentAdaptation.Script) type).getValue().equals(SCRIPT_COMMAND);
                } else {
                    reason = "The type " + type + " is currently not supported by the xTSP and therefore cannot be validated.";
                    return false;
                }
                
                isStrategy &= handler.getImpl().equals(STRATEGY_IMPL_NAME);
            } else if (activity instanceof InstructionalIntervention) {
                InstructionalIntervention instructionalIntervention = (InstructionalIntervention) activity;
                StrategyHandler instructionalStrategyHandler = instructionalIntervention.getStrategyHandler();

                Message message = ((Message) instructionalIntervention.getFeedback().getFeedbackPresentation());
                Message.Delivery.InTrainingApplication inTrainingApp = message.getDelivery().getInTrainingApplication();
                InTutor inTutor = message.getDelivery().getInTutor();
                
                if (inTutor != null) {
                    isStrategy &= instructionalStrategyHandler.getImpl().equals(STRATEGY_IMPL_NAME) && inTrainingApp.getEnabled().equals(BOOLEAN_ENUM_ARRAY[strategyIndex]) 
                            && inTutor.getMessagePresentation().equals(MessageFeedbackDisplayModeEnum.TEXT_ONLY.getName()) && inTutor.getTextEnhancement().equals(TEXT_ENHANCEMENT_ENUM_ARRAY[strategyIndex++]);
                } else {
                    isStrategy &= instructionalStrategyHandler.getImpl().equals(STRATEGY_IMPL_NAME) && inTrainingApp.getEnabled().equals(BOOLEAN_ENUM_ARRAY[strategyIndex]);
                    strategyIndex++;
                }
            } else {
                reason = "The activity " + activity + " is currently not supported by the xTSP and therefore cannot be validated.";
                return false;
            }
        }

        return isStrategy; 
    }
    
    /**
     * Method that checks the individual trigger types for each start/end trigger that was imported into the DKF. This was 
     * separated so both start and end trigger validation can make use of this logic. 
     * 
     * @param triggerType the supported trigger types of the Start or End Trigger
     * @return a value indicating whether, for the specified trigger, the information imported into the DKF is valid or not 
     */
    private boolean checkTriggerType(Serializable triggerType) {
        if (triggerType == null) {
            reason = "The trigger type is null.";
            return false;
        }
        
        if (triggerType instanceof EntityLocation) {
            EntityLocation entityLocation = (EntityLocation) triggerType;
            Serializable teamMemberRefOrLearnerId = entityLocation.getEntityId().getTeamMemberRefOrLearnerId();
            
            /* If the entity identifier's type is a TeamMemberRef, validate according to the defined test 
             * xTSP. Note that LearnerId is a type that currently is not created by the XTSPImporter according to 
             * XTSPImporter.getTriggerTypeData(). Once valid, the check for LearnerId would be added here. 
             */
            if (teamMemberRefOrLearnerId instanceof TeamMemberRef) {
                String teamMemberRef = ((TeamMemberRef) teamMemberRefOrLearnerId).getValue();
                
                if (!teamMemberRef.equals(END_TRIGGERS_TEAM_MEMBER_REFS)) {
                    reason = "The team member reference " + teamMemberRef + " is not equal to the expected value " + END_TRIGGERS_TEAM_MEMBER_REFS + ".";
                    return false;
                }
            }  else {
                reason = "The team member reference or learner id " + teamMemberRefOrLearnerId + " is not a valid type that can"
                        + " be checked by the unit test.";
                return false;
            }
            
            PointRef pointRef = entityLocation.getTriggerLocation().getPointRef();
            String pointRefValue = pointRef.getValue();
            double distance = pointRef.getDistance().doubleValue();
            
            /* Check that the entity location's point reference and distance are equal to the values from the xTSP. */
            if (!pointRefValue.equals(END_TRIGGERS_POINT_REF) && distance != END_TRIGGERS_DISTANCE) {
                reason = "The point reference value " + pointRefValue + " and distance " + distance + "is not equal to the expected value(s)" + END_TRIGGERS_POINT_REF + 
                        " and " + END_TRIGGERS_DISTANCE + ".";
                return false;
            }
        } else if (triggerType instanceof ConceptEnded) {
            ConceptEnded conceptEnded = (ConceptEnded) triggerType;
            int nodeId = conceptEnded.getNodeId().intValue();

            if (nodeId != CONCEPT_ENDED_NODE_ID) {
                reason = "The node id " + nodeId + " is not equal to the expected value 0.";
                return false;
            }
        } else if (triggerType instanceof ConceptAssessment) {
            ConceptAssessment conceptAssessment = (ConceptAssessment) triggerType;
            
            int concept = conceptAssessment.getConcept().intValue();
            String result = conceptAssessment.getResult();

            if (concept != CONCEPT_ASSESSMENT_VALUE && !result.equals(END_TRIGGERS_ASSESSMENTS[assessmentsIndex++])) {
                reason = "The concept value " + concept + " and assessment result " + result + "is not equal to the expected value(s) 1 and " + END_TRIGGERS_ASSESSMENTS[assessmentsIndex - 1] + ".";
                return false;
            }
        } else if (triggerType instanceof StrategyApplied) {
            /* Currently, strategies are not supported in the xTSP. Because of this, the StrategyApplied trigger will not 
             * work at the moment. Nonetheless, return true since this trigger is technically valid in the xTSP.
             */
            return true;
        } else if (triggerType instanceof TaskEnded) {
            TaskEnded taskEnded = (TaskEnded) triggerType;
            int nodeId = taskEnded.getNodeId().intValue();
            
            if (nodeId != TASK_ENDED_NODE_ID) {
                reason = "The node id " + nodeId + " is not equal to the expected value 0.";
                return false;
            }
        } else if (triggerType instanceof ScenarioStarted) {
            /* There's nothing to check here. */
            return true;
        } else {
            reason = "The trigger type " + triggerType + " is currently not supported by GIFT.";
            return false;
        }
        
        return true;
    }
    
    /**
     * Check that the nested conditions or concepts within the Task imported into the DKF are valid according to the xTSP 
     * test file. 
     * 
     * @param conditionsOrConcepts the DKF Concepts/Conditions object that will be checked
     * @return a value indicating whether the conditions or concepts are valid 
     */
    private boolean checkConditionsOrConcepts(Serializable conditionsOrConcepts) {
        if (conditionsOrConcepts == null) {
            reason = "The conditions or concepts are null.";
            return false;
        }
        
        boolean isValidConditionsOrConcepts = true;
        
        if (conditionsOrConcepts instanceof Concepts) {
            Concepts concepts = (Concepts) conditionsOrConcepts;
            List<Concept> conceptList = concepts.getConcept();

            /* For each root concept, check the child conditions/concepts that exist within the node. */
            for (Concept concept : conceptList) {
                isValidConditionsOrConcepts &= concept.getName().equals(CONCEPT_NAMES[conceptsIndex++]) && concept.getNodeId().intValue() == CONCEPT_NODE_ID++
                        && checkConditionsOrConcepts(concept.getConditionsOrConcepts());
            }
        } else if (conditionsOrConcepts instanceof Conditions) {
            Conditions conditions = (Conditions) conditionsOrConcepts;
            List<Condition> conditionList = conditions.getCondition();
            
            /* For each condition, check that the condition class and type are equal to the expected values from the DKF. */
            for (Condition condition : conditionList) {
                String conditionImpl = condition.getConditionImpl();
                Serializable conditionType = condition.getInput().getType();
                isValidConditionsOrConcepts &= conditionImpl.equals(CONDITION_IMPL_NAME) && conditionType instanceof ObservedAssessmentCondition;
            }
            
        } else {
            reason = "The conditions or concepts object " + conditionsOrConcepts + " does not contain a type that can be validated by "
                    + "the unit test.";
            return false;
        }
        
        return isValidConditionsOrConcepts;
    }
    
    /**
     * Runs a REST endpoint that can be used to test exporting an XTSP to a rest service
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public void testRunRestEndpoint() throws Exception{
        
        /* Create the server hosting the endpoint */
        Server server = new Server(3210);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/rest");
        server.setHandler(context);
        
        /* Create a servlet to define the REST function that handles the received XTSP */
        HttpServlet testServlet = new HttpServlet() {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                super.doPost(req, resp);
                
                /* Print the received XTSP */
                System.out.println("Received saved XTSP from GIFT: ");
                
                String requestContent = new String(req.getInputStream().readAllBytes());
                System.out.println(requestContent);
                
                resp.setContentType("text/html");
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        };
        
        context.addServlet(new ServletHolder(testServlet), "/saveXtsp");
        
        server.start();
        
        while(true) {
            //wait until unit test ends to close server
        }
    }
}

