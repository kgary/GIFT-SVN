/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/
package mil.arl.gift.tools.services.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.dkf.Actions;
import generated.dkf.Area;
import generated.dkf.Assessment;
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
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.EnvironmentAdaptation.Script;
import generated.dkf.Feedback;
import generated.dkf.InTutor;
import generated.dkf.Input;
import generated.dkf.InstructionalIntervention;
import generated.dkf.LearnerId;
import generated.dkf.Message;
import generated.dkf.Objects;
import generated.dkf.ObservedAssessmentCondition;
import generated.dkf.Path;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import generated.dkf.Resources;
import generated.dkf.Scenario;
import generated.dkf.ScenarioStarted;
import generated.dkf.Segment;
import generated.dkf.Segment.End;
import generated.dkf.Segment.Start;
import generated.dkf.StartTriggers;
import generated.dkf.StartTriggers.Trigger;
import generated.dkf.StartTriggers.Trigger.TriggerMessage;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.StrategyHandler;
import generated.dkf.StrategyStressCategory;
import generated.dkf.Task;
import generated.dkf.Task.DifficultyMetric;
import generated.dkf.Task.StressMetric;
import generated.dkf.TaskEnded;
import generated.dkf.Tasks;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamMemberRefs;
import generated.dkf.TeamOrganization;
import generated.json.Activity__1.ActivityType;
import generated.json.Measure;
import generated.json.Strategy.StrategySource;
import generated.json.StressLevel.StressType;import generated.json.Target;
import generated.json.TriggerObject;
import generated.json.XEvent;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.enums.MessageFeedbackDisplayModeEnum;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.rest.RESTClient;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;

/**
 * Contains logic used to import XTSP JSON files into DKFs. This allows DKFs to
 * be created with some data already defined.
 * 
 * @author mcambata
 *
 */
public class XTSPImporter {
	
	private static final String BELOW_EXPECTATION_DKF_REF = "BelowExpectation";
	private static final String AT_EXPECTATION_DKF_REF = "AtExpectation";
	private static final String ABOVE_EXPECTATION_DKF_REF = "AboveExpectation";
	/**
	 * Constants used to read values from the xTSP file, when importing into a DKF.
	 */
	private static final String TEAM_SKILL_MEASURES = "teamSkillMeasures";
	private static final String DIMENSION_ID = "teamSkillId";
	private static final String TEAM_DIMENSIONS = "TeamSkills";
	private static final String DIMENSION_TITLE = "teamSkillTitle";
	private static final String TEAM_WORK = "teamwork";
	private static final String DKF_DESCRIPTION = "This DKF was generated by importing data from an xTSP file.";
	private static final String ROLE_ID = "roleId";
    private static final String FIRETEAM = "Fireteam";
	private static final String TEAM = "Team";
	private static final String SUB_TEAMS = "subTeams";
	private static final String REAL_PLAYER = "realPlayer";
	private static final String TEAM_ROLES = "teamRoles";
	private static final String TEAM_ECHELON = "teamEchelon";
	private static final String TEAM_DESIGNATION = "teamDesignation";
	private static final String TEAMS = "teams";
	private static final String ORG_NAME = "orgName";
	private static final String ORGANIZATIONS = "organizations";
	private static final String AFFILIATION = "affiliation";
	private static final String SIDE_NAME = "sideName";
	private static final String POLYGON = "polygon";
	private static final String LINE = "lineString";
	private static final int ALTITUDE_INDEX = 2;
	private static final int LATITUDE_INDEX = 0;
	private static final int LONGITUDE_INDEX = 1;
	private static final String POINT = "point";
	private static final String ANCHOR = "anchor";
	private static final String POINTS = "points";
	private static final String SHAPE = "shape";
	private static final String DIFFICULTY_LEVEL = "difficultyLevel";
	private static final String ACTIVITIES = "activities";
	private static final String ACTOR_NAME = "actorName";
	private static final String ROLE_ACTOR = "roleActor";
	private static final String ACTOR = "actor";
	private static final String ROLE_NAME = "roleName";
	private static final String ROLE = "role";
	private static final String TARGET_TYPE = "targetType";
	private static final String SHOW_FILL = "showFill";
	private static final String SHOW_LINE = "showLine";
	private static final String LINE_ALPHA = "lineAlpha";
	private static final String LINE_COLOR = "lineColor";
	private static final String LINE_SIZE = "lineSize";
	private static final String LINE_STYLE = "lineStyle";
	private static final String STRATEGY_NAME = "strategyName";
	private static final String SYMBOL_CODE = "symbolCode";
	private static final String OVERLAY_NAME = "overlayName";
	private static final String OVERLAY_USE = "overlayUse";
	private static final String TEXT_COLOR = "textColor";
	private static final String TEXT_FONT = "textFont";
	private static final String TEXT_MESSAGE = "textMessage";
	private static final String TARGET_ID = "targetId";
	private static final String X_EVENT_ID = "xEventId";
	private static final String TRIGGER_TARGETS = "targets";
	private static final String ACTIVATE_STRATEGIES = "activateStrategies";
	private static final String TASK_TRIGGER_STRATEGY = "TaskTriggerStrategy";
	private static final String TRIGGER_ACTIVITIES = "triggerActivities";
	private static final String TRIGGER_ACTION = "triggerAction";
	private static final String TRIGGER_DELAY_TIME = "triggerDelayTime";
	private static final String END_TRIGGERS = "endTriggers";
	private static final String START_TRIGGERS = "startTriggers";
	private static final String SUB_MEASURES = "subMeasures";
	private static final String CRITERION_ID = "criterionId";
	private static final String CRITERION = "criterion";
	private static final String LEVEL_ID = "levelID";
	private static final String LEVEL_CRITERIA = "level-criteria";
	private static final String AUTO_EVAL_CLASS = "autoEvalClass";
	private static final String XTSP_EVALUATION = "evaluation";
	private static final String MSR_ID = "msrId";
	private static final String MSR_TITLE = "msrTitle";
	private static final String TASK_MEASURES = "taskMeasures";
	private static final String TGT_PERFORMERS = "tgtPerformers";
	private static final String TASK_TITLE = "taskTitle";
	private static final String TASK_WORK = "taskwork";
	private static final String HIGH_STRESS = "High";
	private static final String MEDIUM_STRESS = "Medium";
	private static final String LOW_STRESS = "Low";
	private static final String STRESS_LEVEL = "stressLevel";
	private static final String HARD_DIFFICULTY = "Hard";
	private static final String MODERATE_DIFFICULTY = "Moderate";
	private static final String EASY_DIFFICULTY = "Easy";
	private static final String INIT_DIFFICULTY = "initDifficulty";
	private static final String X_EVENT_NAME = "xEventName";
	private static final String BELOW_EXPECTATION_ID_NUMBER = "3";
	private static final String AT_EXPECTATION_ID_NUMBER = "2";
	private static final String ABOVE_EXPECTATION_ID_NUMBER = "1";
	private static final String UNKNOWN_ID_NAME = "Unknown";
	private static final String BELOW_EXPECTATION_ID_NAME = "Below Expectation";
	private static final String AT_EXPECTATION_ID_NAME = "At Expectation";
	private static final String ABOVE_EXPECTATION_ID_NAME = "Above Expectation";
	private static final String LEVEL_ID_JSON = "levelId";
	private static final String X_EVENTS_JSON = "XEvents";
	private static final String ACTIVITIES_JSON = "Activities";
	private static final String STRATEGIES_JSON = "Strategies";
	private static final String TRIGGER_ID_JSON = "triggerId";
	private static final String TRIGGERS_JSON = "Triggers";
	private static final String TASK_ID_JSON = "taskId";
	private static final String TASKS_JSON = "Tasks";
	private static final String LEVELS_JSON = "Levels";
	private static final String OVERLAY_ID_JSON = "overlayId";
	private static final String OVERLAYS_JSON = "Overlays";
	private static final String SIDE_ID_JSON = "sideId";
	private static final String FORCE_SIDES_JSON = "ForceSides";
	private static final String TEAM_ID_JSON = "teamId";
	private static final String TEAMS_JSON = "Teams";
	private static final String ROLE_ID_JSON = ROLE_ID;
	private static final String ROLES_JSON = "Roles";
	private static final String ACTOR_ID_JSON = "actorId";
	private static final String ACTORS_JSON = "Actors";
	private static final String ROOT_TEAM_NAME = "Everyone";
	private static final String XTSP_TITLE = "xtspTitle";
	private static final String IDENTIFICATION = "Identification";

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(XTSPImporter.class);
    
    /** The default class for the strategy handler */
    private static final String DEFAULT_STRATEGY_HANDLER_CLASS = "domain.knowledge.strategy.DefaultStrategyHandler";

	private static final String XTSP_TASK_TYPE = "XTSP_TASK";
	private static final String XTSP_MEASURE_TYPE = "XTSP_MEASURE";
	private static final BigDecimal STRATEGY_STRESS_MAX = new BigDecimal(1.0);
	private static final BigDecimal STRATEGY_STRESS_MIN = new BigDecimal(-1.0);
	private static final Long STRATEGY_DIFFICULTY_MAX = 1L;
	private static final Long STRATEGY_DIFFICULTY_MIN = -1L;

	ObjectMapper exportObjectMapper;
	
	/**
	 * Used to track node IDs during the import process, to ensure that they are all
	 * unique.
	 */
    private BigInteger mostRecentNodeId;

    /** This map associates imported tasks with the JSONObjects they come from. */
    private Map<JSONObject, Task> taskJSONMap;
    
	/**
	 * This map associates imported concepts with the JSONObjects they come from.
	 */
    private Map<JSONObject, Concept> conceptJSONMap;
    
    /** This map associates imported concepts with the concepts' IDs. */
    private Map<String, Concept> conceptIdMap;
    
	/**
	 * This map associates imported places of interest with the JSONObjects they
	 * come from.
	 */
    private Map<JSONObject, Serializable> placeOfInterestJSONMap;
    
    /** This map associates imported teams with the JSONObjects they come from. */
    private Map<JSONObject, Team> teamJSONMap;
    
	/**
	 * This map associates imported team members with the JSONObjects they come
	 * from.
	 */
    private Map<JSONObject, TeamMember> teamMemberJSONMap;
    
	/**
	 * This map associates imported Strategies with the JSONObjects they come from.
	 */
    private Map<JSONObject, Strategy> strategiesJSONMap;
    
    /** Used to track the number of each Strategy name, to prevent duplicates. */
    private Map<String, Integer> strategyNameMap;
    
    /** This map associates assessment levels with level IDs from the xTSP file. */
    private Map<String, AssessmentLevelEnum> assessmentLevelIdMap;
    
	/**
	 * This map associates assessment levels with criterion IDs from the xTSP file.
	 */
    private Map<String, AssessmentLevelEnum> assessmentLevelCriterionIdMap;
    
	/**
	 * This map associates GIFT Concepts with criterion IDs from the xTSP file. Each
	 * criterion ID has one Concept that it is part of.
	 */
    private Map<String, Concept> conceptCriterionIdMap;
    
	/**
	 * This is a list of imported GIFT Concepts which should have
	 * correspondingly-named Course Concepts after the import process is completed.
	 */
    private List<Concept> courseConceptList;
    
    /** This contains the JSONArray of actors read from the xTSP file. */
    private JSONArray xtspActors;
    
	/**
	 * This contains the JSONObjects from xtspActors, each mapped to their actorId
	 * value.
	 */
    private Map<String, JSONObject> xtspActorIdMap;
    
    /** This contains the JSONArray of roles read from the xTSP file. */
    private JSONArray xtspRoles;
    
	/**
	 * This contains the JSONObjects from xtspRoles, each mapped to their roleId
	 * value.
	 */
    private Map<String, JSONObject> xtspRoleIdMap;
    
    /** This contains the JSONArray of teams read from the xTSP file. */
    private JSONArray xtspTeams;
    
	/**
	 * This contains the JSONObjects from xtsTeams, each mapped to their teamId
	 * value.
	 */
    private Map<String, JSONObject> xtspTeamIdMap;
    
    /** This contains the JSONArray of forceSides read from the xTSP file. */
    private JSONArray xtspForceSides;
    
	/**
	 * This contains the JSONObjects from xtspForceSides, each mapped to their
	 * sideId value.
	 */
    private Map<String, JSONObject> xtspForceSideIdMap;
    
    /** This contains the JSONArray of overlays read from the xTSP file. */
    private JSONArray xtspOverlays;
    
	/**
	 * This contains the JSONObjects from xtspOverlays, each mapped to their
	 * overlayId value.
	 */
    private Map<String, JSONObject> xtspOverlayIdMap;
    
    /** This contains the JSONArray of levels read from the xTSP file. */
    private JSONArray xtspLevels;
    
    /** This contains the JSONArray of tasks read from the xTSP file. */
    private JSONArray xtspTasks;
        
	/**
	 * This contains the JSONObjects from xtspTasks, each mapped to their taskId
	 * value.
	 */
    private Map<String, JSONObject> xtspTaskIdMap;

    /** This contains the JSONArray of team dimensions read from the xTSP file. */
    private JSONArray xtspDimensions;
    
	/**
	 * This contains the JSONObjects from xtspDimensions, each mapped to their
	 * dimensionId value.
	 */
    private Map<String, JSONObject> xtspDimensionIdMap;
    
    /** This contains the JSONArray of triggers read from the xTSP file. */
    private JSONArray xtspTriggers;
    
	/**
	 * This contains the JSONObjects from xtspTriggers, each mapped to their
	 * triggerId value.
	 */
    private Map<String, JSONObject> xtspTriggerIdMap;
    
	/** This contains the JSONArray of strategies read from the xTSP file. */
	private JSONArray xtspStrategies;

	/** This contains the JSONArray of scripts read from the xTSP file. */
	private JSONArray xtspActivities;

    /** This contains the JSONArray of xEvents read from the xTSP file. */
    private JSONArray xtspXEvents;
    
	/* This is a list of the warnings and errors logged during import/export processes. */
	private List<DetailedException> errorLogList = new ArrayList<DetailedException>();

    /**
	 * Constructor. Sets up the initial state for the XTSPImporter. Each instance
	 * should only be used to run the import process once. To import a new xTSP
	 * file, create a new instance with this constructor. This class is not
	 * thread-safe.
     */
    public XTSPImporter() {
        mostRecentNodeId = BigInteger.ZERO;
        taskJSONMap = new HashMap<JSONObject, Task>();
        conceptJSONMap = new HashMap<JSONObject, Concept>();
        conceptIdMap = new HashMap<String, Concept>();
        placeOfInterestJSONMap = new HashMap<JSONObject, Serializable>();
        teamJSONMap = new HashMap<JSONObject, Team>();
        teamMemberJSONMap = new HashMap<JSONObject, TeamMember>();
        strategiesJSONMap = new HashMap<JSONObject, Strategy>();
        strategyNameMap = new HashMap<String, Integer>();
        assessmentLevelIdMap = new HashMap<String, AssessmentLevelEnum>();
        assessmentLevelCriterionIdMap = new HashMap<String, AssessmentLevelEnum>();
        conceptCriterionIdMap = new HashMap<String, Concept>();
        courseConceptList = new ArrayList<Concept>();
        xtspActorIdMap = new HashMap<String, JSONObject>();
        xtspRoleIdMap = new HashMap<String, JSONObject>();
        xtspTeamIdMap = new HashMap<String, JSONObject>();
        xtspForceSideIdMap = new HashMap<String, JSONObject>();
        xtspTaskIdMap = new HashMap<String, JSONObject>();
        xtspDimensionIdMap = new HashMap<String, JSONObject>();
        xtspTriggerIdMap = new HashMap<String, JSONObject>();
        xtspOverlayIdMap = new HashMap<String, JSONObject>();
		
		exportObjectMapper = new ObjectMapper();
		exportObjectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
		exportObjectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		exportObjectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		
		/* Ignore elements that aren't part of the schema used to compile the XTSP classes. If this
		 * isn't set, then extra properties we don't care about can cause UnrecognizedPropertyExceptions*/
		exportObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
	 * Reads the details of a DKF scenario and outputs specific sections of that DKF into an existing xTSP file.
	 * This does not export the entire DKF into the xTSP, but rather a specific set of changes that GIFT is allowed to make
	 * to the xTSP.
	 * 
	 * @param username The username of the user who is exporting this xTSP file. Used when marshalling the resulting xTSP file. Cannot be empty or null.
	 * @param dkfScenario The DKF Scenario object that is loaded with the xTSP's data. Should be a default DKF object by default. Cannot be null.
	 * @param dkfPath The path of the DKF file. Cannot be null or empty.
	 * @param xtspFileProxy A FileProxy containing the xTSP file to be modified. Cannot be null.
	 * @param folderProxy An AbstractFolderProxy pointing to the course folder containing the DKF and xTSP file. Used to write to the xTSP file. Cannot be null.
	 * @throws DetailedException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public void exportDkfIntoXtsp(String username, Scenario dkfScenario, String dkfPath, FileProxy xtspFileProxy,
			AbstractFolderProxy folderProxy)
			throws DetailedException, IllegalArgumentException, IOException {
		
		//ObjectMapper objectMapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
		exportObjectMapper = new ObjectMapper();
		exportObjectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
		exportObjectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		exportObjectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		
		// Load the XTSP file's data into a Jackson JsonNode.
        
		Object parsedObject = null;
		try (InputStreamReader isr = new InputStreamReader(xtspFileProxy.getInputStream());
				BufferedReader fileReader = new BufferedReader(isr)) {
			final JSONParser jsonParser = new JSONParser();
			parsedObject = jsonParser.parse(fileReader);
		} catch (Exception e) {
			String details = "";
			if (StringUtils.isBlank(xtspFileProxy.getFileId())) {
				details = "An exception occurred while parsing the XTSP file. The file proxy does not contain a valid file ID. Was it created properly?";
				logger.warn(details);
			} else {
				details = "An exception occurred while parsing the XTSP file at " + xtspFileProxy.getFileId() + "."
						+ " Is the XTSP file formatted correctly? Does it contain valid JSON data?";
				logger.warn(details);
			}
			errorLogList.add(new DetailedException(details, "Unable to parse an XTSP file.", e));
			throw new DetailedException("Unable to parse an XTSP file.", details, e);
		}

		if (parsedObject instanceof JSONObject) {
			JSONObject parsedJSONObject = (JSONObject) parsedObject;

				JsonNode xtspJson = exportObjectMapper.readValue(parsedJSONObject.toJSONString(), JsonNode.class);

			// Export assessment levels
			// exportAssessmentLevelsToXtsp();

			// Export team organization
				// exportTeamOrganizationToXtsp(xtspJson, dkfScenario);

				// Export strategies
				 exportStrategiesToXtsp(xtspJson, dkfScenario);
				
				// Export state transitions
				// exportStateTransitionsToXtsp();
				
			// Export places of interest
				// exportPlacesOfInterestToXtsp(xtspJson, dkfScenario);

				// Export elements of task/concept hierarchy
				exportTasksAndConceptsToXtsp(xtspJson, dkfScenario);

			// Save the xTSP data to a file

			// Truncate DKF path to point to its folder instead.
			String fileFolderPath = String.copyValueOf(dkfPath.toCharArray());
			
			// If the last character is a /, then remove that first.
			if (fileFolderPath.lastIndexOf("/") == fileFolderPath.length()-1) {
				fileFolderPath = fileFolderPath.substring(0, fileFolderPath.lastIndexOf("/"));
			}
			
				if(fileFolderPath.lastIndexOf("/") != -1) {
					fileFolderPath = fileFolderPath.substring(0, fileFolderPath.lastIndexOf("/"));
				}
	
			String xtspFileName = fileFolderPath + "/" + folderProxy.getRelativeFileName(xtspFileProxy);
			ServicesManager.getInstance().getFileServices().marshalToFile(username, xtspJson.toPrettyString(), xtspFileName, null);
			
			/* Determine if there is a REST endpoint that the XTSP should be exported to*/
			String restEndpoint = ServicesProperties.getInstance().getPropertyValue(ServicesProperties.XTSP_REST_ENDPOINT_ADDRESS);
			String restFunction = ServicesProperties.getInstance().getPropertyValue(ServicesProperties.EXPORT_XTSP_REST_FUNCTION);
			
    		if(StringUtils.isNotBlank(restEndpoint) && StringUtils.isNotBlank(restFunction)) {
    			
    		    /* Export the XTSP*/
    			RESTClient restClient = new RESTClient();
    			String restUrl =  restEndpoint + restFunction;
    			
    			byte[] response = restClient.post(new URL(restUrl), xtspJson.toPrettyString().getBytes());
    			if(response == null) {
    				String details = "No response was received after saving XTSP to REST endpoint. Is the endpoint reachable?";
    			    logger.error(details);
    			    errorLogList.add(new DetailedException(details, "Unable to reach REST endpoint to send XTSP to. Is the endpoint reachable?", null));
    			    throw new IOException("Unable to reach REST endpoint to send XTSP to. Is the endpoint reachable?");
    			} else {
    			    if(logger.isInfoEnabled()) {
    			        logger.info("Received response from REST endpoint upon saving XTSP:" + new String(response));
    			    }
    			}
			}
		}

	}


	/**
	 * Extracts the xTSP ID value from the external source ID.
	 * 
	 * @param typeValue the task or measure type.
	 * @param idValue the task or concept ID.
	 * 
	 * @return the external source ID.
	 */
	private String generateXtspExternalSourceId(String typeValue, String idValue) {
		return typeValue + ";" + idValue;
	}
	
	/**
	 * Extracts the xTSP type value from the external source ID.
	 * 
	 * @param the external source ID.
	 * 
	 * @return the external source Id value.
	 */
	private String getExternalSourceIdValue(String externalSourceId) {
		if (externalSourceId == null) {
			return null;
		} else {
		return externalSourceId.substring(externalSourceId.indexOf(';') + 1);
	}
	}
	
	/**
	 * Retrieve the external source type value.
	 * 
	 * @param the external source id.
	 * 
	 * @return the external source type value.
	 */
	private String getExternalSourceTypeValue(String externalSourceId) {
		if (externalSourceId == null) {
			return null;
		} else {
		return externalSourceId.substring(0, externalSourceId.indexOf(';'));
	}
	}
	
	/**
	 * Modifies the strategies of the xtspJson to match any changes that have been made to the DKF's strategies.
	 * 
	 * @param xtspJson The JsonNode containing the xTSP's data. Cannot be null.
	 * @param dkfScenario A reference to the DKF scenario data. Cannot be null.
	 */
	private void exportStrategiesToXtsp(JsonNode xtspJson, Scenario dkfScenario) {
		
		List<Strategy> dkfStrategies = dkfScenario.getActions().getInstructionalStrategies().getStrategy();

		ArrayNode xtspStrategies = (ArrayNode) xtspJson.get("Strategies");
		xtspStrategies.removeAll();
		
		ArrayNode activityNode = (ArrayNode) xtspJson.get("Activities");
		activityNode.removeAll();
		
		int strategyId = 0;
		
		for (Strategy currentStrategy : dkfStrategies) {
			
				generated.json.Strategy xtspStrategy = new generated.json.Strategy();
				
				List<generated.json.Activity__1> activity_1_List = exportActivitiesFromDkfToXtsp(currentStrategy, xtspJson);
				
				xtspStrategy.setStrategyName(currentStrategy.getName());
				xtspStrategy.setStrategySource(StrategySource.ITS);
				
				if (currentStrategy.getDifficulty() != null) {
					xtspStrategy.setDifficultyLevel(currentStrategy.getDifficulty().intValue());
				}
				
				if (currentStrategy.getStress() != null) {
					xtspStrategy.setStressLevel(currentStrategy.getStress().doubleValue());
				}
				
				/* Instantiate list of type Activity to populate with data from type Activity__1*/
				List<generated.json.Activity> activityList = new ArrayList<generated.json.Activity>();
				
				/* Iterate through Activity_1 list and create Activity objects from them to add to Activity list. */
				for (generated.json.Activity__1 activity__1 : activity_1_List) {
					generated.json.Activity activity = new generated.json.Activity();
					activity.setActivityId((Integer) activity__1.getActivityId());
					activity.setActivityName(activity__1.getActivityName());
					activityList.add(activity);
	}
	
				xtspStrategy.setActivities(activityList);
				xtspStrategy.setStrategyId(++strategyId);
				xtspStrategy.setStrategyUuid("default");
		
				JsonNode xtspStrategyNode = exportObjectMapper.valueToTree(xtspStrategy);
				xtspStrategies.add(xtspStrategyNode);
		}
	}
	
	/**
	 * Generates a list of xTSP Activity data based on the contents of the specified DKF strategy.
	 * Note: The xTSP schema has two different objects called "activity". The resulting generated classes in GIFT are 
	 * called "Activity" and "Activity__1".
	 * 
	 * @param currentDkfStrategy The DKF strategy being analyzed
	 * @param xtspJson The JsonNode, used to access the Activities.
	 * @return A list of xTSP Activities generated from those contained within the current DKF strategy.
	 */
	private List<generated.json.Activity__1> exportActivitiesFromDkfToXtsp(Strategy currentDkfStrategy, JsonNode xtspJson) {
		
		List<generated.json.Activity__1> activity_1_List = new ArrayList<generated.json.Activity__1>();
			if(currentDkfStrategy.getStrategyActivities() != null) {
				for (Serializable strategyActivity : currentDkfStrategy.getStrategyActivities()) {
		
				if(strategyActivity instanceof generated.dkf.ScenarioAdaptation) {
					generated.dkf.ScenarioAdaptation scenarioAdaptation = (generated.dkf.ScenarioAdaptation) strategyActivity;
					EnvironmentAdaptation environmentAdaptation = scenarioAdaptation.getEnvironmentAdaptation();
					
					if (environmentAdaptation.getType() instanceof Script) {
						EnvironmentAdaptation.Script script = (Script) environmentAdaptation.getType();
		generated.json.Activity__1 xtspActivity = new generated.json.Activity__1();
						xtspActivity.setScriptCommand(script.getValue());
						if (scenarioAdaptation.getDescription() != null) {
							xtspActivity.setActivityName(scenarioAdaptation.getDescription());
						}
		
						List<generated.json.StressLevel> stressCategories = new ArrayList<generated.json.StressLevel>();
						generated.json.StressLevel stressLevel = new generated.json.StressLevel();
		
						if (script.getStressCategory() != null) {
							switch(script.getStressCategory().value()) {
		
							case "Environmental":
								stressLevel.setStressType(StressType.ENVIRONMENTAL);
								break;
			
							case "Cognitive":
								stressLevel.setStressType(StressType.COGNITIVE);
								break;
			
							case "Physiological":
								stressLevel.setStressType(StressType.PHYSICAL);
								break;
			
							default:
								stressLevel.setStressType(null);
							}
						}
			
						
						stressCategories.add(stressLevel);	
						xtspActivity.setStressLevel(stressCategories);
				
						ArrayNode xtspActivities = (ArrayNode) xtspJson.get("Activities");
						xtspActivity.setActivityId(xtspActivities.size() + 1);
						xtspActivity.setActivityUuid("default");
						xtspActivity.setActivityName(scenarioAdaptation.getDescription());
						xtspActivity.setActivityType(ActivityType.CUSTOM_SCRIPT);
						xtspActivity.setScriptCommand(script.getValue());
						xtspActivity.setScriptHandler(scenarioAdaptation.getStrategyHandler().toString());
						
			JsonNode xtspActivityNode = exportObjectMapper.valueToTree(xtspActivity);
						xtspActivities.add(xtspActivityNode);
						
						activity_1_List.add(xtspActivity);
						
					}
			
					} else if (strategyActivity instanceof generated.dkf.InstructionalIntervention) {
						
						generated.dkf.InstructionalIntervention instructionalIntervention = (generated.dkf.InstructionalIntervention) strategyActivity;
						generated.json.Activity__1 xtspActivity = new generated.json.Activity__1();
			
						xtspActivity.setScriptCommand(instructionalIntervention.getFeedback().toString());
						
						List<generated.json.StressLevel> stressCategories = new ArrayList<generated.json.StressLevel>();
						generated.json.StressLevel stressLevel = new generated.json.StressLevel();
						
						if (instructionalIntervention.getStressCategory() != null) {
							switch(instructionalIntervention.getStressCategory().value()) {
							
							case "Environmental":
								stressLevel.setStressType(StressType.ENVIRONMENTAL);
								break;
								
							case "Cognitive":
								stressLevel.setStressType(StressType.COGNITIVE);
								break;
								
							case "Physiological":
								stressLevel.setStressType(StressType.PHYSICAL);
								
							default:
								stressLevel.setStressType(null);
							}
						}
										
						stressCategories.add(stressLevel);	
						xtspActivity.setStressLevel(stressCategories);
				
						ArrayNode xtspActivities = (ArrayNode) xtspJson.get("Activities");
						xtspActivity.setActivityId(xtspActivities.size() + 1);
						xtspActivity.setActivityUuid("default");
						xtspActivity.setActivityType(ActivityType.ACTOR_INTERVENTION);
			
						if (instructionalIntervention.getFeedback().getFeedbackPresentation() instanceof generated.dkf.Message) {
							generated.dkf.Message message = (generated.dkf.Message) instructionalIntervention.getFeedback().getFeedbackPresentation();
							xtspActivity.setScriptCommand("js showText " + "(" + message.getContent() + ")");
							xtspActivity.setActivityName("Feedback Message: " + message.getContent());
					}
						
						xtspActivity.setDifficultyLevel(0.0);
						xtspActivity.setScriptHandler(instructionalIntervention.getStrategyHandler().toString());
						
						JsonNode xtspActivityNode = exportObjectMapper.valueToTree(xtspActivity);
						xtspActivities.add(xtspActivityNode);
						activity_1_List.add(xtspActivity);
					}			
				}
				
			}
			return activity_1_List;
			
		}


	/**
	 * Exports any allowable changes to the DKF scenario's Tasks and Concepts to the xTSP JsonNode.
	 * This includes start and end triggers, conditions, and condition inputs.
	 * 
	 * @param xtspJson The JsonNode containing the xTSP data. Will be modified by this method. Cannot be null.
	 * @param dkfScenario The Scenario containing the DKF data. Cannot be null.
	 */
	private void exportTasksAndConceptsToXtsp(JsonNode xtspJson, Scenario dkfScenario) {

		List<Task> taskList = dkfScenario.getAssessment().getTasks().getTask();

		// Create a map of the xTSP xEvents, which is used for identifying xEvents that are tied to triggers later on.
		Map<String, generated.json.XEvent> xEventIdMap = new HashMap<String, generated.json.XEvent>();
        
		ArrayNode xEventNode = (ArrayNode) xtspJson.get("XEvents");

		Iterator<JsonNode> xEventIterator = xEventNode.iterator();
		while (xEventIterator.hasNext()) {
			JsonNode currentXEventNode = xEventIterator.next();
			try {
				generated.json.XEvent currentXEvent = exportObjectMapper.readValue(currentXEventNode.toString(), generated.json.XEvent.class);
				xEventIdMap.put(currentXEvent.getxEventId().toString(), currentXEvent);
			} catch (JsonProcessingException e) {
				
				String details = "Could not map " + currentXEventNode + " to xEventIdMap";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
			}
		}
		
		/*
		 * Create a map of the xTSP tasks, which is used for identifying tasks that are tied to triggers later on.
		 * During this, also create a map of the xTSP task measures, which is used for identifying measures that are tied to triggers later on.
		 */
		Map<String, generated.json.Task> xtspTaskIdMap = new HashMap<String, generated.json.Task>();
		Map<String, generated.json.TeamSkill> xtspTeamSkillIdMap = new HashMap<String, generated.json.TeamSkill>();
		Map<String, generated.json.Measure> xtspMeasureIdMap = new HashMap<String, generated.json.Measure>();
		
		/* 
		 * The following map is a map of all Tasks mapped to the names of themselves and the measures they contain.
		 * It is used for cases in which a Task needs to be referenced by it or its measures.
		 */
		Map<String, generated.json.Task> xtspTaskAndMeasureIdMap = new HashMap<String, generated.json.Task>();
		
		ArrayNode xtspTaskNode = (ArrayNode) xtspJson.get("Tasks");
		ArrayNode xtspTeamSkillNode = (ArrayNode) xtspJson.get("TeamSkills");
		
		try {
			Iterator<JsonNode> teamSkillIterator = xtspTeamSkillNode.iterator();
			while (teamSkillIterator.hasNext()) {
				JsonNode currentTeamSkillNode = teamSkillIterator.next();
				generated.json.TeamSkill currentTeamSkill = exportObjectMapper.readValue(currentTeamSkillNode.toString(), generated.json.TeamSkill.class);

				xtspTeamSkillIdMap.put(currentTeamSkill.getTeamSkillId().toString(), currentTeamSkill);
				// Now search for any team skill measures.
				List<generated.json.Measure> currentTeamSkillMeasures = currentTeamSkill.getTeamSkillMeasures();
				for (generated.json.Measure currentMeasure : currentTeamSkillMeasures) {
					xtspMeasureIdMap.put(currentMeasure.getMsrId().toString(), currentMeasure);
					List<Measure> subMeasuresToAdd = getSubMeasuresFromMeasure(currentMeasure);
					for (generated.json.Measure currentSubMeasure : subMeasuresToAdd) {
						xtspMeasureIdMap.put(currentSubMeasure.getMsrId().toString(), currentSubMeasure);
				}
			}
			}
			
		Iterator<JsonNode> taskIterator = xtspTaskNode.iterator();
		while (taskIterator.hasNext()) {
			JsonNode currentTaskNode = taskIterator.next();
			try {
				generated.json.Task currentTask = exportObjectMapper.readValue(currentTaskNode.toString(), generated.json.Task.class);
					xtspTaskIdMap.put(currentTask.getTaskId().toString(), currentTask);
					xtspTaskAndMeasureIdMap.put(currentTask.getTaskId().toString(), currentTask);
					
				// Now search for any task measures.
				List<generated.json.Measure> currentTaskMeasures = currentTask.getTaskMeasures();
				for (generated.json.Measure currentMeasure : currentTaskMeasures) {
						xtspMeasureIdMap.put(currentMeasure.getMsrId().toString(), currentMeasure);
						xtspTaskAndMeasureIdMap.put(currentMeasure.getMsrId().toString(), currentTask);
						List<Measure> subMeasuresToAdd = getSubMeasuresFromMeasure(currentMeasure);
						for (generated.json.Measure currentSubMeasure : subMeasuresToAdd) {
							xtspMeasureIdMap.put(currentSubMeasure.getMsrId().toString(), currentSubMeasure);
							xtspTaskAndMeasureIdMap.put(currentSubMeasure.getMsrId().toString(), currentTask);
						}
					}
				
			} catch (JsonProcessingException e) {
					String details = "Could not create map of task measures from the current task node.";
					errorLogList.add(new DetailedException(details, e.toString(), e));
					logger.warn(details, e);
			}
		}
		} catch (JsonProcessingException e) {
			String details = "Could not create maps of measures from Team Skills and Tasks";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
		}
				
		// Clear any existing Triggers from the xTSP JSON, as they will all be re-written based on the triggers in the current DKF.
		ArrayNode triggerNode = (ArrayNode) xtspJson.get("Triggers");
		triggerNode.removeAll();
        
		for (Task currentTask : taskList) {			
			// Export Start Triggers and End Triggers of each Task to xTSP xEvents
			exportTriggersFromDkfTaskToXtsp(currentTask, xEventIdMap, xtspTaskIdMap, xtspMeasureIdMap, xtspTaskAndMeasureIdMap, xtspJson, dkfScenario);

			// Iterate through Concepts to find each Condition, then
			// export Condition input changes to xTSP file
        	}
        
		List<Task> dkfTasks = dkfScenario.getAssessment().getTasks().getTask();

		/*
		 * Check if conditions or concepts are concepts. Method will call itself if it
		 * is a concept.
		 */
		
		for (Task dkfTask : dkfTasks) {
			Concepts concepts = dkfTask.getConcepts();
			List<Concept> dkfConcepts = concepts.getConcept();

			for (Concept dkfConcept : dkfConcepts) {
				traverseConcept(xtspJson, dkfScenario, dkfConcept, dkfTask, xtspTaskIdMap, xtspMeasureIdMap);
			}
		}

		// Write all Tasks back to the xTSP JSON node after modifications have been made.
        xtspTaskNode.removeAll();
        for (generated.json.Task taskToWrite : xtspTaskIdMap.values()) {
            JsonNode taskExport = exportObjectMapper.valueToTree(taskToWrite);
            xtspTaskNode.add(taskExport);
			}
        
        // Write all TeamSkills back to the xTSP JSON node after modifications have been made.
        xtspTeamSkillNode.removeAll();
        for (generated.json.TeamSkill teamSkillToWrite : xtspTeamSkillIdMap.values()) {
        	JsonNode teamSkillExport = exportObjectMapper.valueToTree(teamSkillToWrite);
        	xtspTeamSkillNode.add(teamSkillExport);
        }
		
		// Write all xEvents back to the xTSP JSON node after modifications have been made.
		xEventNode.removeAll();
		for (XEvent xEventToWrite : xEventIdMap.values()) {
			JsonNode xEventExport = exportObjectMapper.valueToTree(xEventToWrite);
			xEventNode.add(xEventExport);
			}
		}
	    
	/**
	 * Recursively traverses any sub-measures of this measure and its sub-measures, 
	 * and assembles a List containing all of them.
	 * @param currentMeasure The measure being checked for subMeasures.
	 * @return a List of all subMeasures contained within this measure and its subMeasures.
	 */
	private List<Measure> getSubMeasuresFromMeasure(Measure currentMeasure) {
		List<Measure> measuresToReturn = new ArrayList<Measure>();
		
		if (currentMeasure == null) {
			return measuresToReturn;
		}
		
		List<Measure> subMeasures = currentMeasure.getSubMeasures();
		
		for (Measure measureToTraverse : subMeasures) {
			measuresToReturn.addAll(getSubMeasuresFromMeasure(measureToTraverse));
		}
		
		measuresToReturn.addAll(subMeasures);
		
		return measuresToReturn;
	}
	
	/**
	 * Recursively traverses the specified Concept and its sub-Concepts. If any Conditions are found, modifies the data
	 * in xtspJson to include them, replacing previous Conditions with any new modifications.
	 * @param xtspJson The JsonNode containing the xTSP data. Will be modified by this method. Cannot be null.
	 * @param dkfScenario The Scenario containing the DKF data. Cannot be null.
	 * @param concept The DKF Concept being read. Cannot be null.
	 * @param task The DKF Task which contains the Concept being read. Cannot be null.
	 * @param xtspTaskIdMap A Map containing the xTSP Tasks, indexed by their taskId values. Cannot be null.
	 * @param xtspMeasureIdMap A Map containing the xTSP Measures (from both Task Work and Team Work), indexed by their msrId values. Cannot be null.
	 */
	private void traverseConcept(JsonNode xtspJson, Scenario dkfScenario, Concept concept, Task task, Map<String, generated.json.Task> xtspTaskIdMap, Map<String, Measure> xtspMeasureIdMap) {
	
		Serializable conditionsOrConcepts = concept.getConditionsOrConcepts();
		
		generated.json.Task xtspTask = xtspTaskIdMap.get(task.getName());

		if (conditionsOrConcepts instanceof Concepts) {
			Concepts dkfConcepts = (Concepts) conditionsOrConcepts;
			List<Concept> dkfConceptList = dkfConcepts.getConcept();

			for (Concept dkfConcept : dkfConceptList) {

				traverseConcept(xtspJson, dkfScenario, dkfConcept, task, xtspTaskIdMap, xtspMeasureIdMap);
					}
				}
				
		if (conditionsOrConcepts instanceof Conditions) {
			Conditions dkfConditions = (Conditions) conditionsOrConcepts;
			List<Condition> dkfConditionsList = dkfConditions.getCondition();
			
			generated.json.Measure matchingXtspMeasure = xtspMeasureIdMap.get(getExternalSourceIdValue(concept.getExternalSourceId()));

				if (matchingXtspMeasure != null) {
					
				// This should be populated based on data from the dkfConcept's conditions
				List<String> newMsrConditions = new ArrayList<String>();
				
				// This should be populated based on data from the dkfConcept's conditions
				List<String> newMethodInputs = new ArrayList<String>();
				
				for (Condition dkfCondition : dkfConditionsList) {
					newMsrConditions.add(dkfCondition.getConditionImpl());
					
					try {
						Input conditionInput = dkfCondition.getInput();
						String xmlString = AbstractSchemaHandler.getAsXMLString(conditionInput, Input.class,
								AbstractSchemaHandler.DKF_SCHEMA_FILE);
						newMethodInputs.add(xmlString);
					} catch (Exception e) {
						String details = "Unable to save condition inputs to XTSP: " + dkfCondition;
						errorLogList.add(new DetailedException(details, e.toString(), e));
						logger.error(details, e);
					}
					
				}
				
				matchingXtspMeasure.setMsrConditions(newMsrConditions);
				matchingXtspMeasure.setMethodInputs(newMethodInputs);

					if (xtspTask != null) {
					xtspTask.getTaskMeasures().add(matchingXtspMeasure);

						JsonNode xtspTaskNode = exportObjectMapper.valueToTree(xtspTask);
					}
				}
			
			}
		}
	
	/**
	 * Add conditions to the specified Concept.
	 * 
	 * @param concept the DKF concept.
	 * @param allConditions the list of DKF conditions.
	 */
	private void addConditions(generated.dkf.Concept concept, List<generated.dkf.Condition> allConditions) {
	    
	    if(concept == null) {
	        return;
	    }
	    
	    if(concept.getConditionsOrConcepts() instanceof generated.dkf.Concepts) {
	       for(generated.dkf.Concept subconcept : ((generated.dkf.Concepts) concept.getConditionsOrConcepts()).getConcept()){
	            addConditions(subconcept, allConditions);
	       }
	        
	    } else {
	        allConditions.addAll(((generated.dkf.Conditions)concept.getConditionsOrConcepts()).getCondition());
	    }
	}

	/**
	 * Modifies the Triggers of a task within the xtspJson to match any changes that have been made to the DKF's strategies.
	 * 
	 * @param currentTask The DKF Task being read in order to export its triggers. Cannot be null.
	 * @param xEventIdMap A map of xTSP xEvents, indexed by their ID values. Cannot be null.
	 * @param xtspTaskIdMap A map of xTSP Tasks, indexed by their ID values. Cannot be null.
	 * @param xtspMeasureIdMap A map of xTSP measures, indexed by their ID values. Cannot be null.
	 * @param xtspTaskAndMeasureIdMap A map of xTSP Tasks, index by their ID values AND the ID values of any xTSP measures they contain. Cannot be null.
	 * @param xtspJson The JsonNode containing the xTSP's data. Cannot be null.
	 * @param dkfScenario A reference to the DKF scenario data. Cannot be null.
	 */
	private void exportTriggersFromDkfTaskToXtsp(Task currentTask, Map<String, generated.json.XEvent> xEventIdMap, Map<String, generated.json.Task> xtspTaskIdMap, Map<String, generated.json.Measure> xtspMeasureIdMap, Map<String, generated.json.Task> xtspTaskAndMeasureIdMap, JsonNode xtspJson, Scenario dkfScenario) {
    	
		generated.json.XEvent currentXEvent = xEventIdMap.get(getExternalSourceIdValue(currentTask.getExternalSourceId()));
    	
		currentXEvent.getStartTriggers().clear();
		currentXEvent.getEndTriggers().clear();
    		
		if (currentXEvent != null) {
    				
			List<generated.dkf.StartTriggers.Trigger> startTriggerList = new ArrayList<generated.dkf.StartTriggers.Trigger>();
			if (currentTask.getStartTriggers() != null) {
				if (currentTask.getStartTriggers().getTrigger() != null) {
					startTriggerList.addAll(currentTask.getStartTriggers().getTrigger());
    		}
    	}
    	
			List<generated.dkf.EndTriggers.Trigger> endTriggerList = new ArrayList<generated.dkf.EndTriggers.Trigger>();
			if (currentTask.getEndTriggers() != null) {
				if (currentTask.getEndTriggers().getTrigger() != null) {
					endTriggerList.addAll(currentTask.getEndTriggers().getTrigger());
				}
    	}
			
			for (generated.dkf.StartTriggers.Trigger currentStartTrigger : startTriggerList) {
				exportStartTriggerToXtsp(currentStartTrigger, currentTask, currentXEvent, xEventIdMap, xtspTaskIdMap, xtspMeasureIdMap, xtspTaskAndMeasureIdMap, xtspJson, dkfScenario);
    }
			for (generated.dkf.EndTriggers.Trigger currentEndTrigger : endTriggerList) {
				exportEndTriggerToXtsp(currentEndTrigger, currentTask, currentXEvent, xEventIdMap, xtspTaskIdMap, xtspMeasureIdMap, xtspTaskAndMeasureIdMap, xtspJson, dkfScenario);
		      }
	        }   	
    }
    
	/**
	 * Modifies the end trigger of a task within the xtspJson to match any changes that have been made to the DKF's strategies.
	 * 
	 * @param currentEndTrigger The DKF end trigger to be exported to the xTSP file. Cannot be null.
	 * @param currentTask The DKF task containing currentEndTrigger. Cannot be null.
	 * @param currentXEvent The xTSP xEvent that corresponds to currentTask. Cannot be null.
	 * @param xEventIdMap A map containing all the xTSP's xEvents, indexed by their ID values. Cannot be null.
	 * @param xtspTaskIdMap A map containing all the xTSP's Tasks (distinct from DKF Tasks; there is a terminology difference between the formats), indexed by their ID values. Cannot be null.
	 * @param xtspMeasureIdMap A map containing all the xTSP's measures from both Tasks and Team Skills, indexed by their ID values. Cannot be null.
	 * @param xtspTaskAndMeasureIdMap A map containing all the xTSP's Tasks (distinct from DKF Tasks), indexed by their ID values AND by the ID values of any measures they contain. Cannot be null.
	 * @param xtspJson The JsonNode containing the xTSP's data. Cannot be null.
	 * @param dkfScenario A reference to the DKF scenario data. Cannot be null.
	 */
	private void exportEndTriggerToXtsp(generated.dkf.EndTriggers.Trigger currentEndTrigger, Task currentTask,
			generated.json.XEvent currentXEvent, Map<String, XEvent> xEventIdMap, Map<String, generated.json.Task> xtspTaskIdMap, Map<String, generated.json.Measure> xtspMeasureIdMap, Map<String, generated.json.Task> xtspTaskAndMeasureIdMap, JsonNode xtspJson, Scenario dkfScenario) {
		generated.json.Trigger xtspTrigger = new generated.json.Trigger();
    	
		ArrayNode triggersNode = (ArrayNode) xtspJson.get("Triggers");
    	
		xtspTrigger.setTriggerId(triggersNode.size() + 1);
		xtspTrigger.setTriggerName(currentEndTrigger.getTriggerType().toString());
		xtspTrigger.setTriggerActive(true);
		xtspTrigger.setRepeat(false);
    		
		if (currentEndTrigger.getTriggerType() instanceof generated.dkf.ConceptAssessment) {
			generated.dkf.ConceptAssessment conceptAssessmentTrigger = (generated.dkf.ConceptAssessment) currentEndTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.ASSESS);
			Tasks dkfTasks = dkfScenario.getAssessment().getTasks();
			xtspTrigger.setTargets(exportAssessTriggerTargetsFromDkfToXtsp(conceptAssessmentTrigger, dkfTasks, xtspTaskIdMap, xtspJson));
			xtspTrigger.setTriggerObjects(exportAssessTriggerObjectsFromDkfToXtsp(conceptAssessmentTrigger, dkfTasks, xtspTaskIdMap, xtspMeasureIdMap, xtspJson));
		} else if (currentEndTrigger.getTriggerType() instanceof generated.dkf.ConceptEnded) {
			generated.dkf.ConceptEnded conceptEndedTrigger = (generated.dkf.ConceptEnded) currentEndTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.COMPLETE);
			Tasks dkfTasks = dkfScenario.getAssessment().getTasks();
			xtspTrigger.setTargets(exportConceptCompleteTriggerTargetsFromDkfToXtsp(conceptEndedTrigger, dkfTasks, xEventIdMap, xtspJson));
			xtspTrigger.setTriggerObjects(exportConceptCompleteTriggerObjectsFromDkfToXtsp(conceptEndedTrigger, dkfTasks, xtspTaskIdMap, xtspMeasureIdMap, xtspJson));
		} else if (currentEndTrigger.getTriggerType() instanceof generated.dkf.TaskEnded) {
			generated.dkf.TaskEnded taskEndedTrigger = (generated.dkf.TaskEnded) currentEndTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.COMPLETE);
			Tasks dkfTasks = dkfScenario.getAssessment().getTasks();
			xtspTrigger.setTargets(exportTaskCompleteTriggerTargetsFromDkfToXtsp(taskEndedTrigger, dkfTasks, xEventIdMap, xtspJson));
			xtspTrigger.setTriggerObjects(exportTaskCompleteTriggerObjectsFromDkfToXtsp(taskEndedTrigger, dkfTasks, xEventIdMap, xtspJson));
		} else if (currentEndTrigger.getTriggerType() instanceof generated.dkf.EntityLocation) {
			generated.dkf.EntityLocation entityLocationTrigger = (generated.dkf.EntityLocation) currentEndTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.ENTER);
			xtspTrigger.setTargets(exportEntityLocationTriggerTargetsFromDkfToXtsp(entityLocationTrigger, xtspJson, dkfScenario));
			xtspTrigger.setTriggerObjects(exportEntityLocationTriggerObjectsFromDkfToXtsp(entityLocationTrigger, xtspJson));
		} else if (currentEndTrigger.getTriggerType() instanceof generated.dkf.StrategyApplied) {
			generated.dkf.StrategyApplied strategyAppliedTrigger = (generated.dkf.StrategyApplied) currentEndTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.ACTIVATE);
			xtspTrigger.setTargets(exportStrategyAppliedTriggerTargetsFromDkfToXtsp(strategyAppliedTrigger, xtspJson));
		    	}
		    	
		if (currentEndTrigger.getTriggerDelay() != null) {
			long remainingSeconds = currentEndTrigger.getTriggerDelay().intValue();
			
			long hoursValue = TimeUnit.SECONDS.toHours(remainingSeconds);
			remainingSeconds -= TimeUnit.HOURS.toSeconds(hoursValue);
			long minutesValue = TimeUnit.SECONDS.toMinutes(remainingSeconds);
			remainingSeconds -= TimeUnit.MINUTES.toSeconds(minutesValue);
		
			String hoursText;
			if (hoursValue >= 10) {
				hoursText = String.valueOf(hoursValue);
			} else if (hoursValue > 0) {
				hoursText = "0" + String.valueOf(hoursValue);
			} else {
				hoursText = "00";
			}
			
			String minutesText;
			if (minutesValue >= 10) {
				minutesText = String.valueOf(minutesValue);
			} else if (minutesValue > 0) {
				minutesText = "0" + String.valueOf(minutesValue);
			} else {
				minutesText = "00";
			}
			
			String secondsText;
			if (remainingSeconds >= 10) {
				secondsText = String.valueOf(remainingSeconds);
			} else if (remainingSeconds > 0) {
				secondsText = "0" + String.valueOf(remainingSeconds);
			} else {
				secondsText = "00";
			}
			
			String delayTimeString = hoursText + ":" + minutesText + ":" + secondsText;
			
			xtspTrigger.setTriggerDelayTime(delayTimeString);
		}
		
		JsonNode xtspEndTriggerNode = exportObjectMapper.valueToTree(xtspTrigger);
		    	
		triggersNode.add(xtspEndTriggerNode);
		
				JsonNodeFactory factory = new JsonNodeFactory(false);
		    			
		ObjectNode triggerReference = new ObjectNode(factory);
		    			
		triggerReference.put("triggerId", xtspTrigger.getTriggerId().toString());
		
		currentXEvent.getEndTriggers().add(triggerReference);
		    			}
	
	/**
	 * Modifies the end trigger of a task within the xtspJson to match any changes that have been made to the DKF's strategies.
	 * 
	 * @param currentStartTrigger The DKF start trigger to be exported to the xTSP file. Cannot be null.
	 * @param currentTask The DKF task containing currentEndTrigger. Cannot be null.
	 * @param currentXEvent The xTSP xEvent that corresponds to currentTask. Cannot be null.
	 * @param xEventIdMap A map containing all the xTSP's xEvents, indexed by their ID values. Cannot be null.
	 * @param xtspTaskIdMap A map containing all the xTSP's Tasks (distinct from DKF Tasks; there is a terminology difference between the formats), indexed by their ID values. Cannot be null.
	 * @param xtspMeasureIdMap A map containing all the xTSP's measures from both Tasks and Team Skills, indexed by their ID values. Cannot be null.
	 * @param xtspTaskAndMeasureIdMap A map containing all the xTSP's Tasks (distinct from DKF Tasks), indexed by their ID values AND by the ID values of any measures they contain. Cannot be null.
	 * @param xtspJson The JsonNode containing the xTSP's data. Cannot be null.
	 * @param dkfScenario A reference to the DKF scenario data. Cannot be null.
	 */
	private void exportStartTriggerToXtsp(Trigger currentStartTrigger, Task currentTask, 
			generated.json.XEvent currentXEvent, Map<String, XEvent> xEventIdMap, Map<String, generated.json.Task> xtspTaskIdMap, Map<String, generated.json.Measure> xtspMeasureIdMap, Map<String, generated.json.Task> xtspTaskAndMeasureIdMap, JsonNode xtspJson, Scenario dkfScenario) {
		generated.json.Trigger xtspTrigger = new generated.json.Trigger();
		
		ArrayNode triggersNode = (ArrayNode) xtspJson.get("Triggers");
		
		xtspTrigger.setTriggerId(triggersNode.size() + 1);
		xtspTrigger.setTriggerName(currentStartTrigger.getTriggerType().toString());
		xtspTrigger.setTriggerActive(true);
		xtspTrigger.setRepeat(false);
		
		if (currentStartTrigger.getTriggerType() instanceof generated.dkf.ConceptAssessment) {
			generated.dkf.ConceptAssessment conceptAssessmentTrigger = (generated.dkf.ConceptAssessment) currentStartTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.ASSESS);
			Tasks dkfTasks = dkfScenario.getAssessment().getTasks();
			xtspTrigger.setTargets(exportAssessTriggerTargetsFromDkfToXtsp(conceptAssessmentTrigger, dkfTasks, xtspTaskAndMeasureIdMap, xtspJson));
			xtspTrigger.setTriggerObjects(exportAssessTriggerObjectsFromDkfToXtsp(conceptAssessmentTrigger, dkfTasks, xtspTaskIdMap, xtspMeasureIdMap, xtspJson));
		} else if (currentStartTrigger.getTriggerType() instanceof generated.dkf.ConceptEnded) {
			generated.dkf.ConceptEnded conceptEndedTrigger = (generated.dkf.ConceptEnded) currentStartTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.COMPLETE);
			Tasks dkfTasks = dkfScenario.getAssessment().getTasks();
			xtspTrigger.setTargets(exportConceptCompleteTriggerTargetsFromDkfToXtsp(conceptEndedTrigger, dkfTasks, xEventIdMap, xtspJson));
			xtspTrigger.setTriggerObjects(exportConceptCompleteTriggerObjectsFromDkfToXtsp(conceptEndedTrigger, dkfTasks, xtspTaskIdMap, xtspMeasureIdMap, xtspJson));
		} else if (currentStartTrigger.getTriggerType() instanceof generated.dkf.TaskEnded) {
			generated.dkf.TaskEnded taskEndedTrigger = (generated.dkf.TaskEnded) currentStartTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.COMPLETE);
			Tasks dkfTasks = dkfScenario.getAssessment().getTasks();
			xtspTrigger.setTargets(exportTaskCompleteTriggerTargetsFromDkfToXtsp(taskEndedTrigger, dkfTasks, xEventIdMap, xtspJson));
			xtspTrigger.setTriggerObjects(exportTaskCompleteTriggerObjectsFromDkfToXtsp(taskEndedTrigger, dkfTasks, xEventIdMap, xtspJson));
		} else if (currentStartTrigger.getTriggerType() instanceof generated.dkf.EntityLocation) {
			generated.dkf.EntityLocation entityLocationTrigger = (generated.dkf.EntityLocation) currentStartTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.ENTER);
			xtspTrigger.setTargets(exportEntityLocationTriggerTargetsFromDkfToXtsp(entityLocationTrigger, xtspJson, dkfScenario));
			xtspTrigger.setTriggerObjects(exportEntityLocationTriggerObjectsFromDkfToXtsp(entityLocationTrigger, xtspJson));
		} else if (currentStartTrigger.getTriggerType() instanceof generated.dkf.StrategyApplied) {
			generated.dkf.StrategyApplied strategyAppliedTrigger = (generated.dkf.StrategyApplied) currentStartTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.ACTIVATE);
			xtspTrigger.setTargets(exportStrategyAppliedTriggerTargetsFromDkfToXtsp(strategyAppliedTrigger, xtspJson));
		} else if (currentStartTrigger.getTriggerType() instanceof generated.dkf.ScenarioStarted) {
			generated.dkf.ScenarioStarted scenarioStartedTrigger = (generated.dkf.ScenarioStarted) currentStartTrigger.getTriggerType();
			xtspTrigger.setTriggerAction(generated.json.Trigger.TriggerAction.START);
			xtspTrigger.setTargets(new ArrayList<generated.json.Target>());
		}
		 
		if (currentStartTrigger.getTriggerDelay() != null) {
			xtspTrigger.setTriggerDelayTime(currentStartTrigger.getTriggerDelay().intValue());
		}
				
		if (currentStartTrigger.getTriggerDelay() != null) {
			long remainingSeconds = currentStartTrigger.getTriggerDelay().intValue();
			
			long hoursValue = TimeUnit.SECONDS.toHours(remainingSeconds);
			remainingSeconds -= TimeUnit.HOURS.toSeconds(hoursValue);
			long minutesValue = TimeUnit.SECONDS.toMinutes(remainingSeconds);
			remainingSeconds -= TimeUnit.MINUTES.toSeconds(minutesValue);
			
			String hoursText;
			if (hoursValue >= 10) {
				hoursText = String.valueOf(hoursValue);
			} else if (hoursValue > 0) {
				hoursText = "0" + String.valueOf(hoursValue);
			} else {
				hoursText = "00";
			}
			
			String minutesText;
			if (minutesValue >= 10) {
				minutesText = String.valueOf(minutesValue);
			} else if (minutesValue > 0) {
				minutesText = "0" + String.valueOf(minutesValue);
			} else {
				minutesText = "00";
			}
			
			String secondsText;
			if (remainingSeconds >= 10) {
				secondsText = String.valueOf(remainingSeconds);
			} else if (remainingSeconds > 0) {
				secondsText = "0" + String.valueOf(remainingSeconds);
			} else {
				secondsText = "00";
			}
			
			String delayTimeString = hoursText + ":" + minutesText + ":" + secondsText;
			
			xtspTrigger.setTriggerDelayTime(delayTimeString);
		}
		
		List<Trigger> triggerList = currentTask.getStartTriggers().getTrigger();
		
		/* Instantiate list of type ActivityItem to populate with data from type Activity__1*/
		List<generated.json.ActivityItem> activityItems = new ArrayList<generated.json.ActivityItem>();
		
		if(currentStartTrigger.getTriggerMessage() != null) {
			generated.dkf.Strategy strategy = currentStartTrigger.getTriggerMessage().getStrategy();
			List<generated.json.Activity__1> activity_1_List = exportActivitiesFromDkfToXtsp(strategy, xtspJson);

			/*
			 * Iterate through Activity_1 list and create ActivityItem objects from them to
			 * add to ActivityItems.
			 */
			for (generated.json.Activity__1 activity__1 : activity_1_List) {
				generated.json.ActivityItem activityItem = new generated.json.ActivityItem();
				activityItem.setActivityId(activity__1.getActivityId());

				Map<String, Object> activityInputsMap = new HashMap<String, Object>();
				activityInputsMap.put("activityUuid", activity__1.getActivityUuid());
				activityInputsMap.put("activityName", activity__1.getActivityName());
				activityInputsMap.put("difficultyLevel", activity__1.getDifficultyLevel());
				activityInputsMap.put("stressLevel", activity__1.getStressLevel());
				activityInputsMap.put("activityType", activity__1.getActivityType());
				activityInputsMap.put("scriptHandler", activity__1.getScriptHandler());
				activityInputsMap.put("scriptCommand", activity__1.getScriptCommand());

				activityItem.setActivityInputs("");
				
				activityItems.add(activityItem);
			}
		}
		
		xtspTrigger.setTriggerActivities(activityItems);
		
		JsonNode xtspEndTriggerNode = exportObjectMapper.valueToTree(xtspTrigger);
		
		triggersNode.add(xtspEndTriggerNode);
		
		JsonNodeFactory factory = new JsonNodeFactory(false);
		
		ObjectNode triggerReference = new ObjectNode(factory);
		
		triggerReference.put("triggerId", xtspTrigger.getTriggerId().toString());
		
		currentXEvent.getStartTriggers().add(triggerReference);
	
		
	}

	/**
	 * Generates a list of xTSP trigger targets from an xEvent's "task work" property.
	 * 
	 * @param xEventToRead The xEvent being used as a reference for the task work. Cannot be null.
	 * @return A List of generated.json.Target representing the target performers in the task work.
	 */
	private List<Target> getTriggerTargetsFromTaskwork(generated.json.XEvent xEventToRead) {
		List<Target> targetList = new ArrayList<Target>();
		List<generated.json.TaskItem> taskWork = xEventToRead.getTaskwork();
		if (taskWork != null) {
			for (generated.json.TaskItem currentTaskItem : taskWork) {
				List<generated.json.Performer> tgtPerformers = currentTaskItem.getTgtPerformers();
				targetList = getTriggerTargetsFromTgtPerformers(tgtPerformers);
			}
		}
		
		return targetList;
	}
	
	/**
	 * Generates a list of xTSP trigger targets from a list of xTSP "Performers".
	 * 
	 * @param tgtPerformers The list of Performers used to generate trigger targets.
	 * @return A List of generated.json.Target representing the target performers.
	 */
	private List<Target> getTriggerTargetsFromTgtPerformers(List<generated.json.Performer> tgtPerformers) {
		List<Target> targetList = new ArrayList<Target>();
				if (tgtPerformers != null) {
					for (generated.json.Performer currentPerformer : tgtPerformers) {
						Object roleId = currentPerformer.getRoleId();
						Object teamId = currentPerformer.getTeamId();
						
						if (roleId != null && roleId instanceof Integer) {
							Target roleTarget = new Target();
							roleTarget.setTargetType(Target.TargetType.ROLE);
							roleTarget.setTargetId((Integer) roleId);
							targetList.add(roleTarget);
				}
						
						if (teamId != null && teamId instanceof Integer) {
							Target teamTarget = new Target();
							teamTarget.setTargetType(Target.TargetType.TEAM);
							teamTarget.setTargetId((Integer) teamId);
							targetList.add(teamTarget);
				}
					}
				}
	    	
		return targetList;
	}
		    	
	/**
	 * Generates targets from a StrategyApplied trigger so that it can be used for xTSP export.
	 * 
	 * @param strategyAppliedTrigger The StrategyApplied trigger in the DKF. Cannot be null.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return a List of generated.json.Targets to be used in the exported xTSP trigger.
	 */
	private List<Target> exportStrategyAppliedTriggerTargetsFromDkfToXtsp(generated.dkf.StrategyApplied strategyAppliedTrigger, JsonNode xtspJson) {
		    	
		String strategyName = strategyAppliedTrigger.getStrategyName();
		String strategyId = null;
		List<generated.json.Target> targetList =  new ArrayList<generated.json.Target>();
		
		ArrayNode strategyNode = (ArrayNode) xtspJson.get("Strategies");
		Iterator<JsonNode> strategyIterator = strategyNode.iterator();
		
		while (strategyIterator.hasNext()) {
			JsonNode currentStrategyNode = strategyIterator.next();
			try {
				generated.json.Strategy currentStrategy = exportObjectMapper.readValue(currentStrategyNode.toString(), generated.json.Strategy.class);
				if (currentStrategy.getStrategyName().equals(strategyName)) {
					strategyId = currentStrategy.getStrategyId().toString();
					break;
				}
			} catch (JsonProcessingException e) {
				String details = "Exception occurred while searching for " + strategyName + " in the xTSP.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
			}
			
    	}
    	
		if (strategyId != null) {
			generated.json.Target outputTriggerTarget = new generated.json.Target();
			outputTriggerTarget.setTargetType(generated.json.Target.TargetType.STRATEGY);
			outputTriggerTarget.setTargetId(Integer.parseInt(strategyId));
			
			targetList.add(outputTriggerTarget);
    }
    
		return targetList;
	}

	/**
	 * Generates trigger objects from an EntityLocation trigger so that they can be used for xTSP export.
	 * @param entityLocationTrigger The EntityLocation trigger in the DKF. Cannot be null.
	 * @param xtspJson  A JsonNode containing the xTSP data. Cannot be null.
	 * @return A list of TriggerObjects generated from the EntityLocation trigger and xTSP data.
	 */
	private List<TriggerObject> exportEntityLocationTriggerObjectsFromDkfToXtsp(generated.dkf.EntityLocation entityLocationTrigger, JsonNode xtspJson) {
    	
		List<TriggerObject> objectList = new ArrayList<generated.json.TriggerObject>();
    	
		// TODO: Make sure to check this is valid. There are multiple ways to create an EntityLocation trigger in GIFT, and we only use ones that reference points of interest.
		String locationName = entityLocationTrigger.getTriggerLocation().getPointRef().getValue();
		String overlayId = null;
    	
		ArrayNode overlayNode = (ArrayNode) xtspJson.get("Overlays");
		Iterator<JsonNode> overlayIterator = overlayNode.iterator();
    	
		while (overlayIterator.hasNext()) {
			JsonNode currentOverlayNode = overlayIterator.next();
			try {
				generated.json.Overlay currentOverlay = exportObjectMapper.readValue(currentOverlayNode.toString(), generated.json.Overlay.class);
				if (currentOverlay.getOverlayName().equals(locationName)) {
					overlayId = currentOverlay.getOverlayId().toString();
					break;
    			}
			} catch (JsonProcessingException e) {
				String details = "Exception occurred while searching for " + locationName + " in the xTSP";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
    		}
    	}
    	
		if (overlayId != null) {
			TriggerObject outputTriggerObject = new generated.json.TriggerObject();
			outputTriggerObject.setObjectType(generated.json.TriggerObject.ObjectType.OVERLAY);
			outputTriggerObject.setObjectName(locationName);
			outputTriggerObject.setObjectId(overlayId);
			
			objectList.add(outputTriggerObject);
    	}
    	
		return objectList;
	}

	/**
	 * Generates targets from a EntityLocation trigger so that it can be used for xTSP export.
	 * 
	 * @param entityLocationTrigger The EntityLocation trigger in the DKF. Cannot be null.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @param dkfScenario The Scenario data of the DKF containing the trigger. Cannot be null.
	 * @return a List of generated.json.Targets to be used in the exported xTSP trigger.
	 */
	private List<Target> exportEntityLocationTriggerTargetsFromDkfToXtsp(generated.dkf.EntityLocation entityLocationTrigger, JsonNode xtspJson, Scenario dkfScenario) {
		
		List<Target> targetList = new ArrayList<Target>();
		
		generated.dkf.EntityLocation.EntityId entityId = entityLocationTrigger.getEntityId();
		
		if (entityId != null) {
			if (entityId.getTeamMemberRefOrLearnerId() instanceof LearnerId) {

				String details = "An EntityLocation trigger was created with a LearnerId target. The xTSP only currently supports EntityLocation triggers "
						+ "with TeamMemberRef targets.";
				logger.warn(details);
				
			} else if (entityId.getTeamMemberRefOrLearnerId() instanceof generated.dkf.EntityLocation.EntityId.TeamMemberRef) {
				
				generated.dkf.EntityLocation.EntityId.TeamMemberRef teamMemberRef = (generated.dkf.EntityLocation.EntityId.TeamMemberRef) entityId.getTeamMemberRefOrLearnerId();
				String teamMemberName = teamMemberRef.getValue();
				TeamMember matchingTeamMember = getMatchingTeamMemberFromTeamOrg(teamMemberName, dkfScenario.getTeamOrganization().getTeam());
				try {
					if (matchingTeamMember.getLearnerId().getType() instanceof String) {
						int teamMemberId = Integer.parseInt((String) matchingTeamMember.getLearnerId().getType());
						Target newTarget = new Target();
						newTarget.setTargetType(generated.json.Target.TargetType.ACTOR);
						newTarget.setTargetId(teamMemberId);
						targetList.add(newTarget);
					}
				} catch (Exception e) {
					String details = "An error has occurred while getting the targets for an Entity Location trigger.";
					errorLogList.add(new DetailedException(details, e.toString(), e));
					logger.error(details, e.getStackTrace());

				}
			}
		}
		
		return targetList;
	}

	/**
	 * Given a DKF team and the name of a Team Member, gets the appropriate Team Member object.
	 * 
	 * @param teamMemberName A String name for the Team Member to find. Cannot be null or empty.
	 * @param teamToCheck The Team to check for the specified Team Member. Cannot be null.
	 * @return The TeamMember with the matching name under the teamToCheck.
	 */
	private TeamMember getMatchingTeamMemberFromTeamOrg(String teamMemberName, generated.dkf.Team teamToCheck) {
		TeamMember teamMemberToReturn = null;
		
		List<Serializable> teamContents = teamToCheck.getTeamOrTeamMember();
		
		for (Serializable elementToCheck : teamContents) {
			if (elementToCheck instanceof TeamMember) {
				TeamMember teamMemberToCheck = (TeamMember) elementToCheck;
				if (teamMemberToCheck.getName().equals(teamMemberName)) {
					return teamMemberToCheck;
				}
			} else if (elementToCheck instanceof generated.dkf.Team) {
				Team currentTeam = (generated.dkf.Team) elementToCheck;
				teamMemberToReturn = getMatchingTeamMemberFromTeamOrg(teamMemberName, currentTeam);
				if (teamMemberToReturn != null) {
					return teamMemberToReturn;
				}
			}
		}
		
		return teamMemberToReturn;
	}

	/**
	 * Generates trigger objects from a TaskEnded trigger, to be used for export to the xTSP file.
	 * 
	 * @param taskEndedTrigger The TaskEnded trigger in the DKF. Cannot be null.
	 * @param dkfTasks A list of Tasks contained in the DKF scenario. Cannot be null.
	 * @param xEventIdMap A map of the xEvents from the xTSP file, indexed by their ID value.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return A list of TriggerObjects generated for use with the xTSP file, from the TaskEnded trigger.
	 */
	private List<TriggerObject> exportTaskCompleteTriggerObjectsFromDkfToXtsp(TaskEnded taskEndedTrigger, generated.dkf.Tasks dkfTasks, Map<String, XEvent> xEventIdMap, JsonNode xtspJson) {
		List<TriggerObject> objectList = new ArrayList<TriggerObject>();
		
		BigInteger triggerNodeId = taskEndedTrigger.getNodeId();
		List<generated.dkf.Task> taskList = dkfTasks.getTask();
		
		for (generated.dkf.Task currentTask : taskList) {
			if (currentTask.getNodeId().equals(triggerNodeId)) {
				TriggerObject objectToAdd = new TriggerObject();
				generated.json.XEvent matchingXEvent = xEventIdMap.get(getExternalSourceIdValue(currentTask.getExternalSourceId()));
				if (matchingXEvent != null) {
					objectToAdd.setObjectType(TriggerObject.ObjectType.XEVENT);
					objectToAdd.setObjectName(matchingXEvent.getxEventName());
					objectToAdd.setObjectId(matchingXEvent.getxEventId());
					objectList.add(objectToAdd);
					break;
	}
			}
		}

		return objectList;
	}
		
	/**
	 * Generates generated.json.Targets from a TaskEnded trigger, to be used for export to the xTSP file.
	 * 
	 * @param taskEndedTrigger The TaskEnded trigger in the DKF. Cannot be null.
	 * @param dkfTasks A list of Tasks contained in the DKF scenario. Cannot be null.
	 * @param xEventIdMap A map of the xEvents from the xTSP file, indexed by their ID value.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return A list of TriggerObjects generated for use with the xTSP file, from the TaskEnded trigger.
	 */
	private List<Target> exportTaskCompleteTriggerTargetsFromDkfToXtsp(TaskEnded taskEndedTrigger, generated.dkf.Tasks dkfTasks, Map<String, XEvent> xEventIdMap, JsonNode xtspJson) {
		List<Target> targetList = new ArrayList<Target>();
    	
		BigInteger triggerNodeId = taskEndedTrigger.getNodeId();
		List<generated.dkf.Task> taskList = dkfTasks.getTask();
		
		for (generated.dkf.Task currentTask : taskList) {
			if (currentTask.getNodeId().equals(triggerNodeId)) {
				generated.json.XEvent matchingXEvent = xEventIdMap.get(getExternalSourceIdValue(currentTask.getExternalSourceId()));
				if (matchingXEvent != null) {
					// Get all target performers from taskwork entries.
					targetList.addAll(getTriggerTargetsFromTaskwork(matchingXEvent));
				}
			}
		}
		
		return targetList;
	}
		
	/**
	 * Generates TriggerObjects from a ConceptEnded trigger, to be used for export to the xTSP file.
	 * 
	 * @param conceptEndedTrigger The ConceptEnded trigger in the DKF. Cannot be null.
	 * @param dkfTasks A list of Tasks contained in the DKF scenario. Cannot be null.
	 * @param xtspTaskIdMap A map of xTSP Tasks from the xTSP file, indexed by their ID value.
	 * @param xtspMeasureIdMap A map of xTSP tmeasures from the xTSP file, indexed by their ID value.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return A list of TriggerObjects generated for use with the xTSP file, from the ConceptEnded trigger.
	 */
	private List<generated.json.TriggerObject> exportConceptCompleteTriggerObjectsFromDkfToXtsp(ConceptEnded conceptEndedTrigger, Tasks dkfTasks, Map<String, generated.json.Task> xtspTaskIdMap, Map<String, generated.json.Measure> xtspMeasureIdMap, JsonNode xtspJson) {
		List<generated.json.TriggerObject> objectList = new ArrayList<generated.json.TriggerObject>();
		
		BigInteger triggerNodeId = conceptEndedTrigger.getNodeId();
		List<generated.dkf.Task> taskList = dkfTasks.getTask();
		boolean conceptFound = false;
		
		for (generated.dkf.Task currentTask : taskList) {
			if (conceptFound) {
				break;
			}
		
			for (generated.dkf.Concept currentConcept : currentTask.getConcepts().getConcept()) {
				generated.dkf.Concept objectConcept = getConceptWithNodeId(triggerNodeId, currentConcept);
				// Add the xTSP Task OR Measure as a trigger object.
				if (objectConcept != null) {
					generated.json.Task objectTask = null;
					objectTask = xtspTaskIdMap.get(getExternalSourceIdValue(objectConcept.getExternalSourceId()));
					if (objectTask != null &&
							getExternalSourceTypeValue(objectConcept.getExternalSourceId()).equals(XTSP_TASK_TYPE)) {
						generated.json.TriggerObject newTriggerObject = new generated.json.TriggerObject();
						newTriggerObject.setObjectType(generated.json.TriggerObject.ObjectType.TASK);
						newTriggerObject.setObjectId(objectTask.getTaskId());
						newTriggerObject.setObjectName(objectTask.getTaskTitle());
						objectList.add(newTriggerObject);
						conceptFound = true;
						break;
					} else {
						generated.json.Measure objectMeasure = null;
						objectMeasure = xtspMeasureIdMap.get(getExternalSourceIdValue(objectConcept.getExternalSourceId()));
						if (objectMeasure != null &&
								getExternalSourceTypeValue(objectConcept.getExternalSourceId()).equals(XTSP_MEASURE_TYPE)) {
							generated.json.TriggerObject newTriggerObject = new generated.json.TriggerObject();
							newTriggerObject.setObjectType(generated.json.TriggerObject.ObjectType.MEASURE);
							newTriggerObject.setObjectId(objectMeasure.getMsrId());
							newTriggerObject.setObjectName(objectConcept.getName());
							objectList.add(newTriggerObject);
							conceptFound = true;
							break;
	}
					}
				}
			}
		}

		return objectList;
	}
    	
	/**
	 * Generates generated.json.Targets from a ConceptEnded trigger, to be used for export to the xTSP file.
	 * 
	 * @param conceptEndedTrigger The ConceptEnded trigger in the DKF. Cannot be null.
	 * @param dkfTasks A list of Tasks contained in the DKF scenario. Cannot be null.
	 * @param xEventIdMap A map of xEvents from the xTSP file, indexed by their ID values. Cannot be null.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return A List of generated.json.Targets for use with the xTSP file, from the ConceptEnded trigger
	 */
	private List<Target> exportConceptCompleteTriggerTargetsFromDkfToXtsp(ConceptEnded conceptEndedTrigger, generated.dkf.Tasks dkfTasks, Map<String, XEvent> xEventIdMap, JsonNode xtspJson) {
		List<Target> targetList = new ArrayList<Target>();
    	
		BigInteger triggerNodeId = conceptEndedTrigger.getNodeId();
		List<generated.dkf.Task> taskList = dkfTasks.getTask();
    	
		for (generated.dkf.Task currentTask : taskList) {
			if (currentTask.getNodeId().equals(triggerNodeId)) {
				generated.json.XEvent matchingXEvent = xEventIdMap.get(currentTask.getExternalSourceId());
				if (matchingXEvent != null) {
					// Get all target performers from taskwork entries.
					targetList.addAll(getTriggerTargetsFromTaskwork(matchingXEvent));
				}
			}
		}
    	
		return targetList;
	}
    	
	/**
	 * Generates TriggerObjects from a ConceptAssessment trigger, to be used for export to the xTSP file.
	 * 
	 * @param conceptAssessedTrigger The ConceptAssessment trigger in the DKF. Cannot be null.
	 * @param dkfTasks A list of Tasks contained in the DKF scenario. Cannot be null.
	 * @param xtspTaskIdMap A map of xTSP tasks from the xTSP file, indexed by their ID values. Cannot be null.
	 * @param xtspMeasureIdMap A map of measures from the xTSP file, indexed by their ID values. Cannot be null.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return A list of TriggerObjects for use with the xTSP file, from the ConceptAssessment trigger
	 */
	private List<generated.json.TriggerObject> exportAssessTriggerObjectsFromDkfToXtsp(ConceptAssessment conceptAssessedTrigger, generated.dkf.Tasks dkfTasks, Map<String, generated.json.Task> xtspTaskIdMap, Map<String, Measure> xtspMeasureIdMap, JsonNode xtspJson) {
		List<generated.json.TriggerObject> objectList = new ArrayList<generated.json.TriggerObject>();
    	
		// TODO: Investigate this. The triggerTargets aren't the issue. This is. It's getting parent Tasks instead of the Measures it should (I need to test this with Team Work measures, as well).
		// TODO: That said, I think that the targets for this trigger won't work for Team Work as of now, so I need to update that, too.
		BigInteger triggerNodeId = conceptAssessedTrigger.getConcept();
		List<generated.dkf.Task> taskList = dkfTasks.getTask();
		boolean conceptFound = false;
    	
		for (generated.dkf.Task currentTask : taskList) {
			if (conceptFound) {
				break;
			}
    			
			for (generated.dkf.Concept currentConcept : currentTask.getConcepts().getConcept()) {
				generated.dkf.Concept objectConcept = getConceptWithNodeId(triggerNodeId, currentConcept);
				// Add the xTSP Task OR Measure as a trigger object.
				if (objectConcept != null) {
					generated.json.Task objectTask = null;
					objectTask = xtspTaskIdMap.get(getExternalSourceIdValue(objectConcept.getExternalSourceId()));
					if (objectTask != null && getExternalSourceTypeValue(objectConcept.getExternalSourceId()).equals(XTSP_TASK_TYPE)) {
						generated.json.TriggerObject newTriggerObject = new generated.json.TriggerObject();
						newTriggerObject.setObjectType(generated.json.TriggerObject.ObjectType.TASK);
						newTriggerObject.setObjectId(objectTask.getTaskId());
						newTriggerObject.setObjectName(objectTask.getTaskTitle());
						objectList.add(newTriggerObject);
						conceptFound = true;
						break;
					} else {
						generated.json.Measure objectMeasure = null;
						objectMeasure = xtspMeasureIdMap.get(getExternalSourceIdValue(objectConcept.getExternalSourceId()));
						if (objectMeasure != null && getExternalSourceTypeValue(objectConcept.getExternalSourceId()).equals(XTSP_MEASURE_TYPE)) {
							generated.json.TriggerObject newTriggerObject = new generated.json.TriggerObject();
							newTriggerObject.setObjectType(generated.json.TriggerObject.ObjectType.MEASURE);
							newTriggerObject.setObjectId(objectMeasure.getMsrId());
							newTriggerObject.setObjectName(objectConcept.getName());
							objectList.add(newTriggerObject);
							conceptFound = true;
							break;
    			}
    		}
    	}
			}
		}
    	
			// Add the expected Assessment level as a trigger object.
			ArrayNode xtspLevels = (ArrayNode) xtspJson.get("Levels");
			Iterator<JsonNode> levelIterator = xtspLevels.iterator();
			
			String levelString = UNKNOWN_ID_NAME;
			
			if (conceptAssessedTrigger.getResult().equalsIgnoreCase(ABOVE_EXPECTATION_DKF_REF)) {
				levelString = ABOVE_EXPECTATION_ID_NAME;
			} else if (conceptAssessedTrigger.getResult().equalsIgnoreCase(AT_EXPECTATION_DKF_REF)) {
				levelString = AT_EXPECTATION_ID_NAME;
			} else if (conceptAssessedTrigger.getResult().equalsIgnoreCase(BELOW_EXPECTATION_DKF_REF)) {
				levelString = BELOW_EXPECTATION_ID_NAME;
			}
			
			while (levelIterator.hasNext()) {
				JsonNode currentLevelNode = levelIterator.next();
				try {
					generated.json.Level currentLevel = exportObjectMapper.readValue(currentLevelNode.toString(), generated.json.Level.class);
				
					if (conceptAssessedTrigger != null && currentLevel != null) {
						if (levelString.equalsIgnoreCase(currentLevel.getLevelTitle())) {
							generated.json.TriggerObject levelTriggerObject = new generated.json.TriggerObject();
							levelTriggerObject.setObjectType(generated.json.TriggerObject.ObjectType.LEVEL);
							levelTriggerObject.setObjectName(currentLevel.getLevelTitle());
							objectList.add(levelTriggerObject);
					}
					}
				} catch (JsonProcessingException e) {
				String details = "Could not set and add level trigger object " + levelString;
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
				}
			}
    
		return objectList;
	}
	
	/**
	 * Recursively searches for the DKF Concept with the specified nodeId. 
	 * Used primarily when exporting triggers to the xTSP file.
	 * 
	 * @param triggerNodeId The nodeID as specified in
	 * @param currentDkfConcept The DKF concept currently being traversed.
	 * @return The Concept that has a nodeId matching triggerNodeId, if one exists. If not, returns null.
	 */
	private Concept getConceptWithNodeId(BigInteger triggerNodeId, generated.dkf.Concept currentDkfConcept) {
		if (currentDkfConcept.getNodeId().equals(triggerNodeId)) {
			return currentDkfConcept;
		} else {
			if (currentDkfConcept.getConditionsOrConcepts() instanceof Concepts) {
				Concepts subConcepts = (Concepts) currentDkfConcept.getConditionsOrConcepts();
				List<Concept> subConceptList = subConcepts.getConcept();
				for (Concept currentSubConcept : subConceptList) {
					Concept returnConcept = getConceptWithNodeId(triggerNodeId, currentSubConcept);
					if (returnConcept != null) {
						return returnConcept;
					}
				}
			}
		}
    	
		// If no matching concept was found, return null.
		return null;
	}
    	
	/**
	 * Generates generated.json.Targets from a ConceptAssessment trigger, to be used for export to the xTSP file.
	 * 
	 * @param conceptAssessedTrigger The ConceptAssessment trigger in the DKF. Cannot be null.
	 * @param dkfTasks A list of Tasks contained in the DKF scenario. Cannot be null.
	 * @param xtspTaskAndMeasureIdMap A map of xTSP Tasks, indexed by their ID values and the ID values of the measures they contain. Cannot be null.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return A list of TriggerObjects for use with the xTSP file, from the ConceptAssessment trigger
	 */
	private List<generated.json.Target> exportAssessTriggerTargetsFromDkfToXtsp(ConceptAssessment conceptAssessedTrigger, generated.dkf.Tasks dkfTasks, Map<String, generated.json.Task> xtspTaskAndMeasureIdMap, JsonNode xtspJson) {
		List<Target> targetList = new ArrayList<Target>();
		
		BigInteger triggerNodeId = conceptAssessedTrigger.getConcept();
		List<generated.dkf.Task> taskList = dkfTasks.getTask();
		generated.json.TaskItem matchingTaskItem = null;
    			
		// Find the xTSP Task Item that contains the specified task or measure name.
		for (generated.dkf.Task currentTask : taskList) {
			if (currentTask.getConcepts() != null) {
				for (generated.dkf.Concept currentConcept : currentTask.getConcepts().getConcept()) {
					// TODO: The below matchingConcept used to call getConceptWithMatchingNodeId(). This should still work, but test to make sure.
					generated.dkf.Concept matchingConcept = getConceptWithNodeId(triggerNodeId, currentConcept);
					if (matchingConcept != null) { 
						matchingTaskItem = getTaskItemWithSpecifiedTaskOrMeasureName(getExternalSourceIdValue(matchingConcept.getExternalSourceId()), xtspTaskAndMeasureIdMap, xtspJson);
						break;
					}
				}
			}
		}
    	
		// Get tgtPerformers from the xTSP TaskItem, and return them as the targetList.
		if (matchingTaskItem != null) {
			List<generated.json.Performer> tgtPerformers = matchingTaskItem.getTgtPerformers();
			targetList = getTriggerTargetsFromTgtPerformers(tgtPerformers);
		}
						
		return targetList;
	}

	/**
	 * TODO: I don't think this is guaranteed to work now that I've changed from using names to externalSourceID. Need to check and potentially fix.
	 * 
	 * Gets the xTSP TaskItem that has the specified externalSourceID.
	 * @param dkfConceptExternalSourceId The exernalSourceID (in xTSP: Task ID or measure ID) 
	 * @param xtspTaskAndMeasureIdMap A map of xTSP Tasks, indexed by the ID values of the tasks and the measures they contain.
	 * @param xtspJson A JsonNode containing the xTSP data. Cannot be null.
	 * @return An xTSP TaskItem with an ID matching the dkfConceptExternalSourceId.
	 */
	private generated.json.TaskItem getTaskItemWithSpecifiedTaskOrMeasureName(String dkfConceptExternalSourceId, Map<String, generated.json.Task> xtspTaskAndMeasureIdMap, JsonNode xtspJson) {
		
		ArrayNode xEventNode = (ArrayNode) xtspJson.get(X_EVENTS_JSON);
		
		// Get the taskId of the specified xtsp task or measure.
		generated.json.Task taskWithName = xtspTaskAndMeasureIdMap.get(dkfConceptExternalSourceId);
		if (taskWithName != null) {
			String targetTaskId = taskWithName.getTaskId().toString();
			
			// Now check all the xEvents until a TaskItem with a matching taskId is found, and return that.
			Iterator<JsonNode> xEventIterator = xEventNode.iterator();
			while (xEventIterator.hasNext()) {
				JsonNode currentXEventJson = xEventIterator.next();
				try {
					generated.json.XEvent currentXEvent = exportObjectMapper.readValue(currentXEventJson.toString(), generated.json.XEvent.class);
					List<generated.json.TaskItem> taskWork = currentXEvent.getTaskwork();
					if (taskWork != null) {
						for (generated.json.TaskItem currentTaskItem : taskWork) {
							if (currentTaskItem.getTaskId() != null) {
								String currentTaskId = currentTaskItem.getTaskId().toString();
								if (targetTaskId.equals(currentTaskId)) {
									return currentTaskItem;
								}
							}
						}
					}
				} catch (JsonProcessingException e) {
					String details = "Could not get current task item.";
					errorLogList.add(new DetailedException(details, e.toString(), e));
					logger.warn(details, e);
				}
			}
		}

		// If no appropriate TaskItem was found, return null.
		return null;
	}

	/**
	 * Modifies the Overlays of the xtspJson to match any changes that have been made to the DKF's Places of Interest.
	 * 
	 * @param xtspJson The JsonNode containing the xTSP's data. Cannot be null.
	 * @param dkfScenario A reference to the DKF scenario data. Cannot be null.
	*/
	public void exportPlacesOfInterestToXtsp(JsonNode xtspJson, Scenario dkfScenario) {
		List<Serializable> dkfPlacesOfInterest = dkfScenario.getAssessment().getObjects().getPlacesOfInterest()
				.getPointOrPathOrArea();

		// Create a map of the places of interest so they can be more quickly searched
		// while iterating over xTSP data.
		Map<String, Serializable> placeNameMap = new HashMap<String, Serializable>();

		for (Serializable placeOfInterest : dkfPlacesOfInterest) {
			if (placeOfInterest instanceof generated.dkf.Point) {
				Point pointOfInterest = (Point) placeOfInterest;
				placeNameMap.put(pointOfInterest.getName(), pointOfInterest);
			} else if (placeOfInterest instanceof generated.dkf.Area) {
				Area areaOfInterest = (Area) placeOfInterest;
				placeNameMap.put(areaOfInterest.getName(), areaOfInterest);
			} else if (placeOfInterest instanceof generated.dkf.Path) {
				Path pathOfInterest = (Path) placeOfInterest;
				placeNameMap.put(pathOfInterest.getName(), pathOfInterest);
			}
		}

		List<String> placeNamesWithMatchingOverlays = new ArrayList<String>();

		// Iterate over the xTSP overlays. If a name matches a place of interest name,
		// copy that place of interest's data into the xTSP JsonNode.
		// But, if an xTSP overlay has no matching Place of Interest name, remove it from the xTSP as it has been deleted
		// (or renamed) in the DKF.
		ArrayNode xtspOverlays = (ArrayNode) xtspJson.get("Overlays");
		java.util.Iterator<JsonNode> overlayIterator = xtspOverlays.elements();
		while (overlayIterator.hasNext()) {
			JsonNode nodeToCheck = overlayIterator.next();
			
			if (!(nodeToCheck instanceof NullNode)) {
			String overlayName = nodeToCheck.get("overlayName").asText();

			Serializable matchingPlaceOfInterest = placeNameMap.get(overlayName);

			if (matchingPlaceOfInterest != null) {
				updateNodeToMatchPlaceOfInterest((ObjectNode) nodeToCheck, matchingPlaceOfInterest);
				placeNamesWithMatchingOverlays.add(overlayName);
			} else
			{
					overlayIterator.remove();
			}
		}
		}

		// Once all the existing nodes have been iterated through, check if any places
		// of interest exist that don't have matching JsonNodes.
		// If any do, create new nodes to contain that data in the xTSP JSON.
		// TODO: I think we want a more efficient process for this. Double-check.
		for (String placeName : placeNameMap.keySet()) {
			if (!placeNamesWithMatchingOverlays.contains(placeName)) {
				JsonNode newNode = createNodeWithPlaceOfInterest(xtspOverlays, placeNameMap.get(placeName));
				if (newNode != null) {
					xtspOverlays.add(newNode);
				}
			}
		}
		}
		
	/**
	 * Creates a JsonNode to contain the data from the specified Place of Interest
	 * @param xtspOverlays The "Overlays" node of the xTSP file, as a JsonNode
	 * @param placeName A Place of Interest, either as a Point, Area, or Path.
	 * @return The specified Place of Interest, as a JsonNode containing overlay data.
	 */
	private JsonNode createNodeWithPlaceOfInterest(JsonNode xtspOverlays, Serializable placeName) {

		JsonNodeFactory factory = new JsonNodeFactory(false);	
		ObjectNode createdNode = new ObjectNode(factory);
		ArrayNode pointsArray = new ArrayNode(factory);
		
		java.util.Iterator<JsonNode> overlayIterator = xtspOverlays.elements();
		
		java.util.List<Integer> overlayIdList = new ArrayList<>();
		
		while (overlayIterator.hasNext()) {
			
			JsonNode currentNode = overlayIterator.next();
			int overlayId = currentNode.get("overlayId").asInt();
			overlayIdList.add(overlayId);
		}
		
		int lastOverlayId = overlayIdList.get(overlayIdList.size() - 1);
		
		
		if (placeName instanceof Point) {
			Point pointOfInterest = (Point) placeName;
			
			createdNode.put(TEXT_MESSAGE, pointOfInterest.getName());
			createdNode.put(SHAPE, "Point");
			createdNode.put(OVERLAY_NAME, pointOfInterest.getName());
			createdNode.put(SHOW_FILL, false);
			createdNode.put(LINE_COLOR, "default");
			createdNode.put(OVERLAY_USE, "default");
			createdNode.put(LINE_ALPHA, "FF");
			createdNode.put(LINE_SIZE, "1px");
			createdNode.put(TEXT_COLOR, "default");
			
			ArrayNode point = convertDkfCoordToXtspPoint(pointOfInterest.getCoordinate());
			pointsArray.add(point);
			
			createdNode.set(POINTS, pointsArray);
			createdNode.put(SYMBOL_CODE, "String");
			createdNode.put(LINE_STYLE, "solid");
			createdNode.put(SHOW_LINE, true);
			createdNode.put(TEXT_FONT, "NewTimesRoman");
			createdNode.put(OVERLAY_ID_JSON, ++lastOverlayId);			

		} else if (placeName instanceof Path) {
			Path pathOfInterest = (Path) placeName;
			
			createdNode.put(TEXT_MESSAGE, pathOfInterest.getName());
			createdNode.put(SHAPE, "LineString");
			createdNode.put(OVERLAY_NAME, pathOfInterest.getName());
			createdNode.put(SHOW_FILL, false);
			createdNode.put(LINE_COLOR, "default");
			createdNode.put(OVERLAY_USE, "default");
			createdNode.put(LINE_ALPHA, "FF");
			createdNode.put(LINE_SIZE, "1px");
			createdNode.put(TEXT_COLOR, "default");	
			
			List<Segment> segmentList = pathOfInterest.getSegment();
			for (Segment currentSegment : segmentList) {
				Coordinate coordStart = currentSegment.getStart().getCoordinate();
				Coordinate coordEnd = currentSegment.getEnd().getCoordinate();

				ArrayNode startPoint = convertDkfCoordToXtspPoint(coordStart);
				ArrayNode endPoint = convertDkfCoordToXtspPoint(coordEnd);
				
				pointsArray.add(startPoint);
				pointsArray.add(endPoint);
				
				createdNode.set(POINTS, pointsArray);
				createdNode.put(SYMBOL_CODE, "String");
				createdNode.put(LINE_STYLE, "solid");
				createdNode.put(SHOW_LINE, true);
				createdNode.put(TEXT_FONT, "NewTimesRoman");
				createdNode.put(OVERLAY_ID_JSON, ++lastOverlayId);
			}

		} else if (placeName instanceof Area) {
			Area areaOfInterest = (Area) placeName;
			
			createdNode.put(TEXT_MESSAGE, areaOfInterest.getName());
			createdNode.put(SHAPE, "Polygon");
			createdNode.put(OVERLAY_NAME, areaOfInterest.getName());
			createdNode.put(SHOW_FILL, false);
			createdNode.put(LINE_COLOR, "default");
			createdNode.put(OVERLAY_USE, "default");
			createdNode.put(LINE_ALPHA, "FF");
			createdNode.put(LINE_SIZE, "1px");
			createdNode.put(TEXT_COLOR, "default");
			
			List<Coordinate> areaCoords = areaOfInterest.getCoordinate();
			for (Coordinate currentCoord : areaCoords) {
				ArrayNode coord = convertDkfCoordToXtspPoint(currentCoord);
				pointsArray.add(coord);
			}
			
			createdNode.set(POINTS, pointsArray);
			createdNode.put(SYMBOL_CODE, "String");
			createdNode.put(LINE_STYLE, "solid");
			createdNode.put(SHOW_FILL, true);
			createdNode.put(TEXT_FONT, "NewTimesRoman");
			createdNode.put(OVERLAY_ID_JSON, ++lastOverlayId);
		}

		return createdNode;
	}

	/**
	 * Updates an existing JsonNode from the xTSP file so that its overlay data matches the data of a DKF place of interest.
	 * @param nodeToCheck a JsonNode containing overlay data.
	 * @param matchingPlaceOfInterest A Point, Area, or Path with data matching the nodeToCheck, from the DKF.
	 * @return A modified version of nodeToCheck with data pulled from matchingPlaceOfInterest.
	 */
	private JsonNode updateNodeToMatchPlaceOfInterest(ObjectNode nodeToCheck, Serializable matchingPlaceOfInterest) {
		if (matchingPlaceOfInterest instanceof Point) {
			Point pointOfInterest = (Point) matchingPlaceOfInterest;
			nodeToCheck.put("shape", "Point");
			ArrayNode pointsArray = (ArrayNode) nodeToCheck.get("points");
			pointsArray.removeAll();
			ArrayNode point = convertDkfCoordToXtspPoint(pointOfInterest.getCoordinate());
			pointsArray.add(point);
		} else if (matchingPlaceOfInterest instanceof Path) {
			Path pathOfInterest = (Path) matchingPlaceOfInterest;
			nodeToCheck.put("shape", "LineString");
			ArrayNode pointsArray = (ArrayNode) nodeToCheck.get("points");
			pointsArray.removeAll();
			List<Segment> segmentList = pathOfInterest.getSegment();
			for (Segment currentSegment : segmentList) {
				Coordinate coordStart = currentSegment.getStart().getCoordinate();
				Coordinate coordEnd = currentSegment.getEnd().getCoordinate();

				ArrayNode latestPoint = (ArrayNode) pointsArray.get(pointsArray.size() - 1);

				ArrayNode startPoint = convertDkfCoordToXtspPoint(coordStart);
				ArrayNode endPoint = convertDkfCoordToXtspPoint(coordEnd);

				boolean addStartPoint = false;
				if (!pointsArray.isEmpty()) {
					if (!pointValuesAreEqual(latestPoint, startPoint)) {
						addStartPoint = true;
					}
				} else {
					addStartPoint = true;
				}

				if (addStartPoint) {
					pointsArray.add(startPoint);
				}

				if (!pointValuesAreEqual(startPoint, endPoint)) {
					pointsArray.add(endPoint);
				}

			}
		} else if (matchingPlaceOfInterest instanceof Area) {
			Area areaOfInterest = (Area) matchingPlaceOfInterest;
			nodeToCheck.put("shape", "Polygon");
			ArrayNode pointsArray = (ArrayNode) nodeToCheck.get("points");
			pointsArray.removeAll(); 
			List<Coordinate> areaCoords = areaOfInterest.getCoordinate();
			for (Coordinate currentCoord : areaCoords) {
				pointsArray.add(convertDkfCoordToXtspPoint(currentCoord));
			}
		}

		return nodeToCheck;
	}

	/**
	 * Determines whether or not two ArrayNodes containing point coordinates have equal values.
	 * @param firstPoint The first set of coordinates, as an ArrayNode.
	 * @param secondPoint The second set of coordinates, as an ArrayNode.
	 * @return True if the point values are equal. False otherwise.
	 */
	private boolean pointValuesAreEqual(ArrayNode firstPoint, ArrayNode secondPoint) {
		boolean arePointsEqual = true;
		if (firstPoint.size() == secondPoint.size()) {
			for (int i = 0; i < firstPoint.size(); i++) {
				if (Double.compare(firstPoint.get(i).asDouble(), secondPoint.get(i).asDouble()) != 0) {
					arePointsEqual = false;
					break;
				}
			}
		} else {
			arePointsEqual = false;
		}
		return arePointsEqual;
	}

	/**
	 * Converts a DKF coordinate to the format used in the xTSP file.
	 * Because the xTSP file uses GDC coordinates, this also involves checking if a DKF coordinate is in GDC,
	 * and convering it to GDC if not.
	 * 
	 * @param coordinate The coordinate to convert.
	 * @return The resulting point, as an ArrayNode of coordinates.
	 */
	private ArrayNode convertDkfCoordToXtspPoint(Coordinate coordinate) {
		JsonNodeFactory factory = new JsonNodeFactory(false);
		ArrayNode resultPoint = new ArrayNode(factory);

		double latitude = 0;
		double longitude = 0;
		double altitude = 0;

		// The xTSP uses GDC points. If the coordinate is GCC, convert to GDC first.
		if (coordinate.getType() instanceof generated.dkf.GCC) {
			generated.dkf.GCC gccCoordinate = (generated.dkf.GCC) coordinate.getType();
			
			try {
				generated.dkf.GDC gdcCoordinate = CoordinateUtil.getInstance().convertToDkfGDC(gccCoordinate);
				latitude = gdcCoordinate.getLatitude().doubleValue();
				longitude = gdcCoordinate.getLongitude().doubleValue();
				altitude = gdcCoordinate.getElevation().doubleValue();
			} catch (Exception e) {
				// If the GCC coordinate cannot be properly converted to GDC, then 
				// reset to and use the default latitude/longitude/altitude instead.
				latitude = 0;
				longitude = 0;
				altitude = 0;
			}
		} else if (coordinate.getType() instanceof generated.dkf.GDC) {
			generated.dkf.GDC gdcCoordinate = (generated.dkf.GDC) coordinate.getType();
			latitude = gdcCoordinate.getLatitude().doubleValue();
			longitude = gdcCoordinate.getLongitude().doubleValue();
			altitude = gdcCoordinate.getElevation().doubleValue();
		}

		// Set latitude (index 0)
		resultPoint.add(latitude);

		// Set longitude (index 1)
		resultPoint.add(longitude);

		// Set altitude (index 2)
		resultPoint.add(altitude);

		return resultPoint;
	}

    /**
     * Reads an xTSP file, converts its data into a DKF object, and then marshalls the DKF to a file so it can be loaded by GIFT.
     * 
     * @param username The username of the user who is importing this xTSP file. Used when marshalling the resulting DKF file. Cannot be empty or null.
     * @param dkfScenario The DKF Scenario object that is loaded with the xTSP's data. Should be a default DKF object by default. Cannot be null.
     * @param xtspInput An input stream containing the raw XTSP JSON. Cannot be null.
     * @param allowedCoordinateTypes A set of strings representing allowed coordinate types for the current training application.
     * e.g. "GCC" or "GDC". Cannot be null.
     * @throws DetailedException if an error occurs which prevents the xTSP file from being properly parsed.
     * @throws IllegalArgumentException if dkfScenario, xtspFileProxy, or allowedCoordinateTypes are null, or if username or dkfPath are blank (empty or null) Strings.
     */
    public void importXtspIntoDkf(String username, Scenario dkfScenario, InputStream xtspInput, Set<String> allowedCoordinateTypes) throws DetailedException, IllegalArgumentException {
        
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        } else if (dkfScenario == null) {
            throw new IllegalArgumentException("The parameter 'dkfScenario' cannot be null.");
        } else if (xtspInput == null) {
            throw new IllegalArgumentException("The parameter 'xtspFileProxy' cannot be null.");
        } else if (allowedCoordinateTypes == null) {
        	throw new IllegalArgumentException("The parameter 'allowedCoordinateTypes' cannot be null.");
        }
		
		if (dkfScenario.getTeamOrganization() == null) {
			generated.dkf.TeamOrganization teamOrg = new generated.dkf.TeamOrganization();
			generated.dkf.Team rootTeam = new generated.dkf.Team();
			rootTeam.setName(ROOT_TEAM_NAME);
			teamOrg.setTeam(rootTeam);
			dkfScenario.setTeamOrganization(teamOrg);
		}


        // Parse the XTSP file
        Object parsedObject = null;
        try (InputStreamReader isr = new InputStreamReader(xtspInput);
        		BufferedReader fileReader = new BufferedReader(isr)) {
            final JSONParser jsonParser = new JSONParser();
            parsedObject = jsonParser.parse(fileReader);
        } catch (Exception e) {
            String details = "An exception occurred while parsing the XTSP file." 
                        + " Is the XTSP file formatted correctly? Does it contain valid JSON data?";
            errorLogList.add(new DetailedException(details, e.toString(), e));
            throw new DetailedException("Unable to parse an XTSP file.", details, e);
        }
        
        if (parsedObject instanceof JSONObject) {
            JSONObject parsedJSONObject = (JSONObject) parsedObject;
            
			Object identification = parsedJSONObject.get(IDENTIFICATION);
			if(identification instanceof JSONObject) {
			    
			    Object title = ((JSONObject) identification).get(XTSP_TITLE);
	            if(title instanceof String) {
	                dkfScenario.setName((String) title);
	            }
			}

            Object actors = parsedJSONObject.get(ACTORS_JSON);
            if (actors instanceof JSONArray) {
            	xtspActors = (JSONArray) actors;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + ACTORS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object actorToMap : xtspActors) {
            	JSONObject actorJSON = (JSONObject) actorToMap;
            	String actorId = readJSONAsString(actorJSON.get(ACTOR_ID_JSON));
            	xtspActorIdMap.put(actorId, actorJSON);
            }
            
            Object roles = parsedJSONObject.get(ROLES_JSON);
            if (roles instanceof JSONArray) {
            	xtspRoles = (JSONArray) roles;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + ROLES_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object roleToMap : xtspRoles) {
            	JSONObject roleJSON = (JSONObject) roleToMap;
            	String roleId = readJSONAsString(roleJSON.get(ROLE_ID_JSON));
            	xtspRoleIdMap.put(roleId, roleJSON);
            }
            
            Object teams = parsedJSONObject.get(TEAMS_JSON);
            if (teams instanceof JSONArray) {
            	xtspTeams = (JSONArray) teams;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + TEAMS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object teamToMap : xtspTeams) {
            	JSONObject teamJSON = (JSONObject) teamToMap;
            	String teamId = readJSONAsString(teamJSON.get(TEAM_ID_JSON));
            	xtspTeamIdMap.put(teamId, teamJSON);
            }
            
            Object forceSides = parsedJSONObject.get(FORCE_SIDES_JSON);
            if (forceSides instanceof JSONArray) {
            	xtspForceSides = (JSONArray) forceSides;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + FORCE_SIDES_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object forceSideToMap : xtspForceSides) {
            	JSONObject forceSideJSON = (JSONObject) forceSideToMap;
            	String forceSideId = readJSONAsString(forceSideJSON.get(SIDE_ID_JSON));
            	xtspForceSideIdMap.put(forceSideId, forceSideJSON);
            }
            
            Object overlays = parsedJSONObject.get(OVERLAYS_JSON);
            if (overlays instanceof JSONArray) {
            	xtspOverlays = (JSONArray) overlays;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + OVERLAYS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object overlayToMap : xtspOverlays) {
            	JSONObject overlayJSON = (JSONObject) overlayToMap;
            	String overlayId = readJSONAsString(overlayJSON.get(OVERLAY_ID_JSON));
            	xtspOverlayIdMap.put(overlayId, overlayJSON);
            }
            
            Object levels = parsedJSONObject.get(LEVELS_JSON);
            if (levels instanceof JSONArray) {
            	xtspLevels = (JSONArray) levels;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + LEVELS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            Object tasks = parsedJSONObject.get(TASKS_JSON);
            if (tasks instanceof JSONArray) {
            	xtspTasks = (JSONArray) tasks;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + TASKS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object taskToMap : xtspTasks) {
            	JSONObject taskJSON = (JSONObject) taskToMap;
            	String taskId = readJSONAsString(taskJSON.get(TASK_ID_JSON));
            	xtspTaskIdMap.put(taskId, taskJSON);
            }
            
            Object teamDimensions = parsedJSONObject.get(TEAM_DIMENSIONS);
            if (teamDimensions instanceof JSONArray) {
            	xtspDimensions = (JSONArray) teamDimensions;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + TEAM_DIMENSIONS + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object dimensionToMap : xtspDimensions) {
            	JSONObject dimensionJSON = (JSONObject) dimensionToMap;
            	String dimensionId = readJSONAsString(dimensionJSON.get(DIMENSION_ID));
            	xtspDimensionIdMap.put(dimensionId, dimensionJSON);
            }
            
            Object triggers = parsedJSONObject.get(TRIGGERS_JSON);
            if (triggers instanceof JSONArray) {
            	xtspTriggers = (JSONArray) triggers;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + TRIGGERS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            for (Object triggerToMap : xtspTriggers) {
            	JSONObject triggerJSON = (JSONObject) triggerToMap;
            	String triggerId = readJSONAsString(triggerJSON.get(TRIGGER_ID_JSON));
            	xtspTriggerIdMap.put(triggerId, triggerJSON);
            }
            
            Object strategies = parsedJSONObject.get(STRATEGIES_JSON);
            if (strategies instanceof JSONArray) {
            	xtspStrategies = (JSONArray) strategies;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + STRATEGIES_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
			Object activities = parsedJSONObject.get(ACTIVITIES_JSON);
			if (activities instanceof JSONArray) {
				xtspActivities = (JSONArray) activities;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + ACTIVITIES_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
            Object xEvents = parsedJSONObject.get(X_EVENTS_JSON);
            if (xEvents instanceof JSONArray) {
            	xtspXEvents = (JSONArray) xEvents;
			} else {
				throw new DetailedException("The xTSP file did not contain the " + X_EVENTS_JSON + " element. The import process cannot continue.", "Invalid xTSP file", new Exception());
            }
            
			// Get "levels," which correspond to assessment levels, and may be referenced by
			// other parts of the import process.
            try {
                for (Object levelToAdd : xtspLevels) {
                    if (levelToAdd instanceof JSONObject) {
                        JSONObject levelJSON = (JSONObject) levelToAdd;
                        String levelId = readJSONAsString(levelJSON.get(LEVEL_ID_JSON));

                        if (levelId == null) {
							String details = "The \"levels\" element of the xTSP contains a levelId with no specified value. This level cannot be imported, and will be skipped."
									+ " Attempting to continue the import process.";
							errorLogList.add(new DetailedException("Unable to import level Id: " + levelId, details, null));
							logger.warn(details);
                        	continue;
                        }
                        
                        if (levelId.equals(ABOVE_EXPECTATION_ID_NAME) || levelId.equals(ABOVE_EXPECTATION_ID_NUMBER)) {
                            assessmentLevelIdMap.put(levelId, AssessmentLevelEnum.ABOVE_EXPECTATION);
                        } else if (levelId.equals(AT_EXPECTATION_ID_NAME) || levelId.equals(AT_EXPECTATION_ID_NUMBER)) {
                            assessmentLevelIdMap.put(levelId, AssessmentLevelEnum.AT_EXPECTATION);
						} else if (levelId.equals(BELOW_EXPECTATION_ID_NAME)
								|| levelId.equals(BELOW_EXPECTATION_ID_NUMBER)) {
                            assessmentLevelIdMap.put(levelId, AssessmentLevelEnum.BELOW_EXPECTATION);
                        } else if (levelId.equals(UNKNOWN_ID_NAME)) {
                            assessmentLevelIdMap.put(levelId, AssessmentLevelEnum.UNKNOWN);
                        }
                    }
                }
            } catch (Exception e) {
				String details = "The \"levels\" element of the xTSP cannot be properly read. This will prevent references to assessment"
						+ " levels from being populated during the xTSP import process. It should not impact other aspects of the import process.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
            }
            
            try {
                // Create a team organization from the XTSP file
                generated.dkf.TeamOrganization xtspTeamOrg = dkfScenario.getTeamOrganization();
                
                xtspTeamOrg = generateTeamOrgFromXTSP(xtspTeamOrg);
                
                // Set the scenario's team organization to match the data from the XTSP file.
                dkfScenario.setTeamOrganization(xtspTeamOrg);
                    
            } catch (Exception e) {
				String details = "Team organization data could not be parsed from the XTSP file. "
                        + "Attempting to continue the process of importing from XTSP.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
                logger.warn(details, e);
                
            }
            
            // Create Places of Interest from the XTSP file
            generated.dkf.PlacesOfInterest xtspPlacesOfInterest = new generated.dkf.PlacesOfInterest();
            xtspPlacesOfInterest = generatePlacesOfInterestFromXTSP(xtspPlacesOfInterest, allowedCoordinateTypes);

            if(dkfScenario.getAssessment() == null) {
                dkfScenario.setAssessment(new Assessment());
            }
            
            if(dkfScenario.getAssessment().getObjects() == null) {
                dkfScenario.getAssessment().setObjects(new Objects());
            }
            
            dkfScenario.getAssessment().getObjects().setPlacesOfInterest(xtspPlacesOfInterest);
            
			// Create Strategies from the XTSP file (probably bring in deleted Strategy import code)
			generated.dkf.Actions.InstructionalStrategies instructionalStrategies = new generated.dkf.Actions.InstructionalStrategies();
			instructionalStrategies = generateInstructionalStrategiesFromXTSP();
			
			if(dkfScenario.getActions() == null) {
			    dkfScenario.setActions(new Actions());
			}
			
			dkfScenario.getActions().setInstructionalStrategies(instructionalStrategies);
			
            // Create Tasks and Concepts from the xTSP file
            Tasks xtspTasks = generateTasksFromXTSP(parsedJSONObject, dkfScenario.getTeamOrganization());
            dkfScenario.getAssessment().setTasks(xtspTasks);
            
            // Set the DKF's description data.
            dkfScenario.setDescription(DKF_DESCRIPTION);
        }
    }
            
    /**
     * Reads an xTSP file, converts its data into a DKF object, and then marshalls the DKF to a file so it can be loaded by GIFT.
     * 
     * @param username The username of the user who is importing this xTSP file. Used when marshalling the resulting DKF file. Cannot be empty or null.
     * @param dkfScenario The DKF Scenario object that is loaded with the xTSP's data. Should be a default DKF object by default. Cannot be null.
     * @param dkfPath The path where the DKF file will be saved. Cannot be empty or null.
     * @param xtspFileProxy The FileProxy that is used to access the xTSP file. Cannoy be null.
     * @param allowedCoordinateTypes A set of strings representing allowed coordinate types for the current training application.
     * e.g. "GCC" or "GDC". Cannot be null.
     * @throws DetailedException if an error occurs which prevents the xTSP file from being properly parsed.
     * @throws IllegalArgumentException if dkfScenario, xtspFileProxy, or allowedCoordinateTypes are null, or if username or dkfPath are blank (empty or null) Strings.
     */
    public void importXtspIntoDkf(String username, Scenario dkfScenario, String dkfPath, FileProxy xtspFileProxy, Set<String> allowedCoordinateTypes) throws DetailedException, IllegalArgumentException {
        if (StringUtils.isBlank(dkfPath)) {
            throw new IllegalArgumentException("The parameter 'dkfPath' cannot be blank.");
        } else if (dkfScenario == null) {
            throw new IllegalArgumentException("The parameter 'dkfScenario' cannot be null.");
        }
        
        try {
            
            if(dkfScenario.getResources() == null) {
                dkfScenario.setResources(new Resources());
            }
            
        	dkfScenario.getResources().setSourcePath(xtspFileProxy.getName());
            importXtspIntoDkf(username, dkfScenario, xtspFileProxy.getInputStream(), allowedCoordinateTypes);
        
        } catch (IOException e) {
             String details = "";
                if (StringUtils.isBlank(xtspFileProxy.getFileId())) {
                    details = "An exception occurred while parsing the XTSP file. The file proxy does not contain a valid file ID. Was it created properly?";
                    errorLogList.add(new DetailedException(details, e.toString(), e));
                } else {
                    details = "An exception occurred while parsing the XTSP file at " + xtspFileProxy.getFileId() + "." 
                            + " Is the XTSP file formatted correctly? Does it contain valid JSON data?";
                }
                errorLogList.add(new DetailedException(details, e.toString(), e));
                throw new DetailedException("Unable to parse an XTSP file.", details, e);
        }
        
            /* 
             * If a dkfPath was provided, marshal the now-updated scenario to save it back into the scenario file.
             * If a dkfPath is not provided, the dkfScenario object is still modified, but not written to a file.
             * This can be useful in cases where XTSP-parsing should be performed but the results do not need to be saved
             * for later use, such as during a unit test.
             */
            try {
                if (!StringUtils.isBlank(dkfPath)) {
                    ServicesManager.getInstance().getFileServices().marshalToFile(
                            username, 
                            dkfScenario, 
                            dkfPath, 
                            null,
                            false);
                }
            } catch (Exception e) {
				String details = "Failed to marshal to the DKF at " + dkfPath + " with user name " + username + ".";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.error(details, e);
            }
        }
        
    /**
	 * Gets a valid value for a node ID in the task and concept hierarchy. Takes the
	 * most recently used node ID, increments by 1, and then returns that number.
	 * Because mostRecentNodeId is modified as part of this method, multiple calls
	 * will produce new values and prevent duplicate node IDs.
	 * 
     * @return a BigInteger representing the next unused node ID that can be used.
     */
    private BigInteger getNodeIdValue() {
        mostRecentNodeId = mostRecentNodeId.add(BigInteger.ONE);
        return mostRecentNodeId;
    }

    /**
	 * Parses the JSON containing Task data, and writes what can be read to the Task
	 * and Concept hierarchy in the DKF.
     * 
     * @param xtspJSON the JSON data parsed from the xTSP file. Cannot be null.
	 * @param teamOrg  a TeamOrganization object used to reference learners for
	 *                 tasks and child objects. Can be null, but if so, references
	 *                 to learners will not be populated in the resulting DKF.
	 * @return a generated.dkf.Tasks which contains the Tasks data that could be
	 *         read.
     * @throws IllegalArgumentException if xtspJSON is null.
     */
    private Tasks generateTasksFromXTSP(JSONObject xtspJSON, TeamOrganization teamOrg) {

        Tasks tasksToReturn = new Tasks();
        
        try {
            if (xtspJSON == null) {
                throw new IllegalArgumentException("The parameter 'xtspJSON' cannot be null.");
            }
    
            Map<String, Integer> nameMap = new HashMap<String, Integer>();
            
            /*
             * The experienceEvents in the xTSP file correspond to top-level Tasks in GIFT.
             */
            for (Object xEventToAdd : xtspXEvents) {
                if (xEventToAdd instanceof JSONObject) {
                
                    JSONObject xEventJSON = (JSONObject) xEventToAdd;
                    
                    Task taskToAdd = generateTask(xEventJSON, nameMap, teamOrg);
                    
                    if (taskToAdd != null) {
                        tasksToReturn.getTask().add(taskToAdd);
                        taskJSONMap.put(xEventJSON, taskToAdd);
                    }
                }
            }
            
            /*
			 * StartTriggers and EndTriggers must be added after the first pass, because
			 * otherwise they could reference tasks that had not yet been imported.
             */
            for (JSONObject xEventJSON : taskJSONMap.keySet()) {
               
                taskJSONMap.get(xEventJSON).setStartTriggers(generateStartTriggers(xEventJSON));
                taskJSONMap.get(xEventJSON).setEndTriggers(generateEndTriggers(xEventJSON));
            }
                
        } catch (Exception e) {
			String details = "An exception occurred while attempting to import Tasks from the xTSP file. Cannot import Tasks. "
					+ "Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
        }
        
        return tasksToReturn;
    }

    /**
	 * Parses the JSON containing a single Task from an xTSP file, and converts it
	 * to a GIFT DKF Task.
     * 
     * @param taskJSON the JSON data containing a Task. Cannot be null.
	 * @param nameMap. A map with a String key of names of Tasks and Concepts, and
	 *                 an Integer value. Used to count duplicate names in the xTSP
	 *                 file, so they can be adjusted to prevent duplicate names in
	 *                 GIFT's DKF. Cannot be null.
	 * @param teamOrg  a TeamOrganization object used to reference learners for
	 *                 tasks and child objects. Can be null, but if so, references
	 *                 to learners will not be populated in the resulting DKF.
	 * @return a generated.dkf.Task with the data that could be read from the xTSP
	 *         file.
     * @throws IllegalArgumentException if xtspJSON is null.
     */
    private Task generateTask(JSONObject taskJSON, Map<String, Integer> nameMap, TeamOrganization teamOrg) {
        if (taskJSON == null) {
            throw new IllegalArgumentException("The parameter 'taskJSON' cannot be null.");
        } else if (nameMap == null) {
            throw new IllegalArgumentException("The parameter 'nameMap' cannot be null.");
        }
        
        Task taskToReturn = new Task();
        String taskName = "";
        
        try {
            taskName = readJSONAsString(taskJSON.get(X_EVENT_NAME));
        } catch (Exception e) {
			String details = "Could not read the name of a Task in the xTSP file. This Task cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
        
        if (taskName == null) {
			String details = "A Task in the xTSP file has a null value for a name. This Task will not be imported. Attempting to continue the import process.";
			errorLogList.add(new DetailedException("A task could not be imported.", details, null));
			logger.warn(details);
            return null;
        }
        
        try {
            taskName = getNameAfterCheckingForDuplicates(taskName, nameMap);
			String taskId = readJSONAsString(taskJSON.get(X_EVENT_ID));
            
            try {
                Object initDifficulty = taskJSON.get(INIT_DIFFICULTY);
                /*
				 * initDifficulty is optional. If it isn't present as a JSONObject in the xTSP
				 * file, skip this. But if it is present and has invalid data, then display the
				 * exception in a warning.
                 */
                if (initDifficulty instanceof String) {
                    String difficultyString = readJSONAsString(initDifficulty);
                    Long difficultyLevel = 0L;
                    if (difficultyString.equals(EASY_DIFFICULTY)) {
                    	difficultyLevel = 1L;
                    } else if (difficultyString.equals(MODERATE_DIFFICULTY)) {
                    	difficultyLevel = 2L;
                    } else if (difficultyString.equals(HARD_DIFFICULTY)) {
                    	difficultyLevel = 3L;
                    }
					else {
						String details = "The difficulty level for a Task named " + taskName + " is invalid. "
								+ "Only values of " + EASY_DIFFICULTY + ", " + MODERATE_DIFFICULTY + ", or " + HARD_DIFFICULTY + " are valid,"
								+ "but the value provided was " + difficultyString + ".";
						errorLogList.add(new DetailedException("Difficulty Level Invalid.", details, null));
						logger.warn(details);
					}
					
					if (difficultyLevel != 0L) {
                    DifficultyMetric taskDifficulty = new DifficultyMetric();
                    taskDifficulty.setValue(new BigDecimal(difficultyLevel));
                    taskToReturn.setDifficultyMetric(taskDifficulty);
                }
				} else if (initDifficulty instanceof Long){
					
					Long difficultyLevel = (Long) initDifficulty;

					if (difficultyLevel > 0L && difficultyLevel <= 3L) {
						if (difficultyLevel != 0L) {
							DifficultyMetric taskDifficulty = new DifficultyMetric();
							taskDifficulty.setValue(new BigDecimal(difficultyLevel));
							taskToReturn.setDifficultyMetric(taskDifficulty);
						}

					}

					else {
						String details = "The difficulty level for a Task named " + taskName + " is invalid. "
								+ "Only values 0-3 are valid, but the value provided was " + difficultyLevel + ".";
						errorLogList.add(new DetailedException("Difficulty Level Invalid", details, null));
						logger.warn(details);
					}

				} else {
					String details = "The difficulty level for a Task named " + taskName + " is invalid. Only Strings and Longs are valid, and the provided difficulty level was not a String or a Long.";
					errorLogList.add(new DetailedException("Difficulty Level Invalid", details, null));
					logger.warn(details);
				}
            } catch (Exception e) {
				String details = "An exception occurred while reading the initial difficulty rating of a Task named "
						+ taskName + ". " + "This task will not be assigned a difficulty rating on import.";
				errorLogList.add(new DetailedException(details, e.toString(), null));
				logger.warn(details, e);
            }
            
            try {
                Object stressLevel = taskJSON.get(STRESS_LEVEL);
                /*
				 * stressLevel is optional. If it isn't present as a JSONObject in the xTSP
				 * file, skip this. But if it is present and has invalid data, then display the
				 * exception in a warning.
                 */
                if (stressLevel instanceof String) {
                    String stressLevelString =  readJSONAsString(stressLevel);
                    BigDecimal stressValueBigDecimal = null;
                    
                    /*
                     * If the stressValue is written without a decimal mark, the parser interprets
					 * it as a Double. Otherwise, it's interpreted as a Long. So check for both and
					 * convert to the eventual target type of BigDecimal.
                     */
                    if (stressLevelString.equals(LOW_STRESS)) {
                    	stressValueBigDecimal = new BigDecimal(0.1);
                    } else if (stressLevelString.equals(MEDIUM_STRESS)) {
                    	stressValueBigDecimal = new BigDecimal(0.5);
                    } else if (stressLevelString.equals(HIGH_STRESS)) {
                    	stressValueBigDecimal = new BigDecimal(1.0);
                    }
                    
                    if (stressValueBigDecimal == null) {
						String details = "Could not read the stress level of a Task named " + taskName + ". "
								+ "This task will use the default stress level on import.";
						errorLogList.add(new DetailedException("Stress Level", details, null));
						logger.warn(details);
                    } else {
                        StressMetric stressMetric = new StressMetric();
                        stressMetric.setValue(stressValueBigDecimal);
                        taskToReturn.setStressMetric(stressMetric);
                    }
                }
            } catch (Exception e) {
				String details = "An exception occurred while reading the stress level of a Task named " + taskName + ". "
						+ "This task will use the default stress level on import.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
            }
            
            StartTriggers startTriggers = new StartTriggers();
            EndTriggers endTriggers = new EndTriggers();
                    
            Concepts concepts = generateConceptsList(taskJSON, taskName, nameMap, teamOrg);
                    
            taskToReturn.setNodeId(getNodeIdValue());
            taskToReturn.setName(taskName);
			taskToReturn.setExternalSourceId(generateXtspExternalSourceId(XTSP_TASK_TYPE, taskId));
            taskToReturn.setStartTriggers(startTriggers);
            taskToReturn.setEndTriggers(endTriggers);
            taskToReturn.setConcepts(concepts);
                        
            return taskToReturn;
        } catch (Exception e) {
			String details = "An exception occurred while importing the Task named " + taskName + " from the xTSP file. "
					+ "This Task cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
    }
    
    /**
	 * Generates a list of concepts which are children of the Task represented by in
	 * xEventJSON.
     * 
	 * @param xEventJSON the JSON data containing a Task (called xEvent in the xTSP
	 *             )      file). Cannot be null.
	 * @param parentName the name of the Task which this Concepts list is part of.
	 *                   Cannot be null or empty.
	 * @param nameMap.   A map with a String key of names of Tasks and Concepts, and
	 *                   an Integer value. Used to count duplicate names in the xTSP
	 *                   file, so they can be adjusted to prevent duplicate names in
	 *                   GIFT's DKF. Cannot be null.
	 * @param teamOrg    a TeamOrganization object used to reference learners for
	 *                   tasks and child objects. Can be null, but if so, references
	 *                   to learners will not be populated in the resulting DKF.
	 * @return a generated.dkf.Concepts with the data that could be read from the
	 *         xTSP file.
     * @throws IllegalArgumentException if xtspJSON is null.
     */
	private Concepts generateConceptsList(JSONObject xEventJSON, String parentName, Map<String, Integer> nameMap,
			TeamOrganization teamOrg) {
        if (xEventJSON == null) {
            throw new IllegalArgumentException("The parameter 'xEventJSON' cannot be null.");
        } else if (StringUtils.isBlank(parentName)) {
            throw new IllegalArgumentException("The parameter 'parentName' cannot be blank.");
        } else if (nameMap == null) {
            throw new IllegalArgumentException("The parameter 'nameMap' cannot be null.");
        }
        
        Concepts conceptsToReturn = new Concepts();
        int taskMeasureSize;
        try {
            List<Concept> conceptList = conceptsToReturn.getConcept();
            
            Object teamWork = xEventJSON.get(TEAM_WORK);
            
            JSONArray teamWorkArray = null;
            
            if (teamWork instanceof JSONArray) {
            	teamWorkArray = (JSONArray) teamWork;
            }
            
            if (teamWorkArray != null) {
                if (teamWorkArray.size() > 0) {
                    try {                    
                        Concept teamWorkConcept = new Concept();
                        teamWorkConcept.setNodeId(getNodeIdValue());
        
                        teamWorkConcept.setName(getNameAfterCheckingForDuplicates("Team Work", nameMap));
                        conceptList.add(teamWorkConcept);
                        
                        Concepts teamWorkSubConcepts = new Concepts();
                        
                        teamWorkConcept.setConditionsOrConcepts(teamWorkSubConcepts);
                        
                        for (Object xtspDimensionRef : teamWorkArray) {                                                    
                            /*
                             *  xTSP's "dimensions" are their own type of object in the xTSP file, but GIFT
                             *  interprets them, along with measures, as distinct layers of Concepts.
                             */
                        	JSONObject dimensionRefJSON = (JSONObject) xtspDimensionRef;
                        	String xtspDimensionId = readJSONAsString(dimensionRefJSON.get(DIMENSION_ID));
                        	JSONObject currentDimensionJSON = xtspDimensionIdMap.get(xtspDimensionId);
                            Concept currentDimensionConcept = new Concept();
                            currentDimensionConcept.setNodeId(getNodeIdValue());
                            
							String currentDimensionConceptName = readJSONAsString(
									currentDimensionJSON.get(DIMENSION_TITLE));
							currentDimensionConceptName = getNameAfterCheckingForDuplicates(currentDimensionConceptName,
									nameMap);
                            
                            currentDimensionConcept.setName(currentDimensionConceptName);
                            
                            Concepts currentDimensionSubConcepts = new Concepts();
                            currentDimensionConcept.setConditionsOrConcepts(currentDimensionSubConcepts);
                            
                            teamWorkSubConcepts.getConcept().add(currentDimensionConcept);
                            
                            conceptJSONMap.put(currentDimensionJSON, currentDimensionConcept);
                            conceptIdMap.put(xtspDimensionId, currentDimensionConcept);
                            courseConceptList.add(currentDimensionConcept);
                            
                            /*
							 * Get a list of target performers. For all Conditions under this Concept, all
							 * these targets are being assessed.
                             */
                            List<String> learnerNamesToAssess = new LinkedList<String>();
                        	JSONArray targetRefs = (JSONArray) dimensionRefJSON.get(TGT_PERFORMERS);
                        	for (Object currentRef : targetRefs) {
                        		JSONObject targetRefJSON = (JSONObject) currentRef;
                        		String roleId = readJSONAsString(targetRefJSON.get(ROLE_ID_JSON));
                        		if (roleId != null) {
	                        		JSONObject roleToAddJSON = xtspRoleIdMap.get(roleId);
	                        		
	                        		/*
									 * Use the role JSON to find the imported learner, and get their name. This is
									 * necessary to avoid a situation where a role's name may have changed from xTSP
									 * to GIFT (such as two learners having the same name)
	                        		 */
	                        		TeamMember teamMemberToAssess = teamMemberJSONMap.get(roleToAddJSON);
	                        		learnerNamesToAssess.add(teamMemberToAssess.getName());
	                        		continue;
                        		}
                        		
                        		/*
                        		 * If a roleId wasn't found, check for a teamId instead.
                        		 */
                        		String teamId = readJSONAsString(targetRefJSON.get(TEAM_ID_JSON));
                        		if (teamId != null) {
                        			JSONObject teamJSON = xtspTeamIdMap.get(teamId);
                        			if (teamJSON != null) {
                        				learnerNamesToAssess.addAll(getTeamMembersFromTeamJSON(teamJSON));
                        				continue;
                        			}
                        		}
                        		
                        		/*
                        		 * Check for an actorId if other Ids weren't found.
                        		 */
                        		String actorId = readJSONAsString(targetRefJSON.get(ACTOR_ID_JSON));
                        		if (actorId != null) {
                        			for (Object role : xtspRoles) {
                        				if (role instanceof JSONObject) {
	                        				JSONObject roleJSON = (JSONObject) role;
	                        				JSONObject roleActor = (JSONObject) roleJSON.get(ROLE_ACTOR);
	                        				String roleActorId = readJSONAsString(roleActor.get(ACTOR_ID_JSON));
	                        				if (roleActorId.equals(actorId)) {
	                        					TeamMember teamMemberToAssess = teamMemberJSONMap.get(roleJSON);
	        	                        		learnerNamesToAssess.add(teamMemberToAssess.getName());
	                        					break;
	                        				}
                        				}
                        			}
                        			continue;
                        		}
                        		
                        		/*
                        		 * If nothing caused a break by this point, the current entry is not supported.
                        		 */
								String details = "Attempted to read a tgtPerformers entry which was not an actorID, roleId, or teamId. Only these types of tgtPerformers are currently supported by GIFT."
										+ "Skipping this tgtPerformer and attempting to continue the import process.";
								errorLogList.add(new DetailedException("A TgtPerformer Could Not Be Read", details, null));
								logger.warn(details);
                        	}
                            
                            JSONArray teamSkillMeasures = (JSONArray) currentDimensionJSON.get(TEAM_SKILL_MEASURES);
                            if (teamSkillMeasures.size() > 0) {
                                for (Object conceptToProcess : teamSkillMeasures) {
                                    
                                    JSONObject conceptJSON = (JSONObject) conceptToProcess;
                                
									Concept conceptToAdd = generateConcept(conceptJSON, parentName, nameMap,
											learnerNamesToAssess);
                                    if (conceptToAdd != null) {
                                        currentDimensionSubConcepts.getConcept().add(conceptToAdd);
                                    }
                                }
                            } else {
                                Conditions defaultConditions = generateDefaultConditions(learnerNamesToAssess);
                                currentDimensionConcept.setConditionsOrConcepts(defaultConditions);
                            }
                        }
                    } catch (Exception e) {
						String details = "An exception occurred while reading the teamTasks under the xEvent named "
								+ parentName + " in the xTSP file. " + "Skipping any unread teamTasks of " + parentName
								+ ". Attempting to continue the import process.";
						errorLogList.add(new DetailedException(details, e.toString(), e));
						logger.warn(details, e);
                    
                    }
                }
            }
            
            Object taskWork = xEventJSON.get(TASK_WORK);
              
            /*
			 * taskwork is optional, but if it is present, then each present list should be
			 * a Concept which contains its respective sub-Concepts.
             */
            JSONArray taskWorkArray = null;
            
            if (taskWork instanceof JSONArray) {
            	taskWorkArray = (JSONArray) taskWork;
            }
            
            if (taskWorkArray != null) {
                if (taskWorkArray.size() > 0) {
                    try {                    
                        Concept taskWorkConcept = new Concept();
                        taskWorkConcept.setNodeId(getNodeIdValue());
        
                        taskWorkConcept.setName(getNameAfterCheckingForDuplicates("Task Work", nameMap));
                        conceptList.add(taskWorkConcept);
                        
                        Concepts taskWorkSubConcepts = new Concepts();
                        
                        taskWorkConcept.setConditionsOrConcepts(taskWorkSubConcepts);
                        
                        for (Object xtspTaskRef : taskWorkArray) {                                                    
							try {
                            /*
                             *  xTSP's "tasks" are their own type of object in the xTSP file, but GIFT
                             *  interprets them, along with measures, as distinct layers of Concepts.
                             */
                        	JSONObject taskRefJSON = (JSONObject) xtspTaskRef;
                        	String xtspTaskId = readJSONAsString(taskRefJSON.get(TASK_ID_JSON));
                        	JSONObject currentTaskJSON = xtspTaskIdMap.get(xtspTaskId);
								if (currentTaskJSON == null) {
									String details = "The xTSP references a task with ID " + xtspTaskId + ", but a task with that ID cannot be found in the xTSP file.";
									errorLogList.add(new DetailedException("A Task could not be found.", details, null));
									logger.warn(details);
									continue;
								}
                            Concept currentTaskConcept = new Concept();
                            currentTaskConcept.setNodeId(getNodeIdValue());
							currentTaskConcept.setExternalSourceId(generateXtspExternalSourceId(XTSP_TASK_TYPE, xtspTaskId));
                            
                            String currentTaskConceptName = readJSONAsString(currentTaskJSON.get(TASK_TITLE));
                            currentTaskConceptName = getNameAfterCheckingForDuplicates(currentTaskConceptName, nameMap);
                            
                            currentTaskConcept.setName(currentTaskConceptName);
                            
                            Concepts currentTaskSubConcepts = new Concepts();
                            currentTaskConcept.setConditionsOrConcepts(currentTaskSubConcepts);
                            
                            taskWorkSubConcepts.getConcept().add(currentTaskConcept);
                            
                            conceptJSONMap.put(currentTaskJSON, currentTaskConcept);
                            conceptIdMap.put(xtspTaskId, currentTaskConcept);
                            courseConceptList.add(currentTaskConcept);
                            
                            /*
							 * Get a list of target performers. For all Conditions under this Concept, all
							 * these targets are being assessed.
                             */
                            List<String> learnerNamesToAssess = new LinkedList<String>();
                        	JSONArray targetRefs = (JSONArray) taskRefJSON.get(TGT_PERFORMERS);
                        	for (Object currentRef : targetRefs) {
                        		JSONObject targetRefJSON = (JSONObject) currentRef;
                        		String roleId = readJSONAsString(targetRefJSON.get(ROLE_ID_JSON));
                        		if (roleId != null) {
										JSONObject roleToAddJSON = xtspRoleIdMap.get(roleId);
	
										/*
									 * Use the role JSON to find the imported learner, and get their name. This is
									 * necessary to avoid a situation where a role's name may have changed from xTSP
									 * to GIFT (such as two learners having the same name)
										 */
										TeamMember teamMemberToAssess = teamMemberJSONMap.get(roleToAddJSON);
										learnerNamesToAssess.add(teamMemberToAssess.getName());
	                        		continue;
									}
	
                        		/*
                        		 * If a roleId wasn't found, check for a teamId instead.
                        		 */
                        		String teamId = readJSONAsString(targetRefJSON.get(TEAM_ID_JSON));
                        		if (teamId != null) {
                        			JSONObject teamJSON = xtspTeamIdMap.get(teamId);
                        			if (teamJSON != null) {
                        				learnerNamesToAssess.addAll(getTeamMembersFromTeamJSON(teamJSON));
                        				continue;
                        			}
                        		}
                        		
                        		/*
                        		 * Check for an actorId if other Ids weren't found.
                        		 */
                        		String actorId = readJSONAsString(targetRefJSON.get(ACTOR_ID_JSON));
                        		if (actorId != null) {
                        			for (Object role : xtspRoles) {
                        				if (role instanceof JSONObject) {
	                        				JSONObject roleJSON = (JSONObject) role;
	                        				JSONObject roleActor = (JSONObject) roleJSON.get(ROLE_ACTOR);
	                        				String roleActorId = readJSONAsString(roleActor.get(ACTOR_ID_JSON));
	                        				if (roleActorId.equals(actorId)) {
	                        					TeamMember teamMemberToAssess = teamMemberJSONMap.get(roleJSON);
	        	                        		learnerNamesToAssess.add(teamMemberToAssess.getName());
	                        					break;
	                        				}
                        				}
                        			}
                        			continue;
                        		}
                        		
                        		/*
                        		 * If nothing caused a break by this point, the current entry is not supported.
                        		 */
									String details = "Attempted to read a tgtPerformers entry which was not an actorID, roleId, or teamId. Only these types of tgtPerformers are currently supported by GIFT."
											+ "Skipping this tgtPerformer and attempting to continue the import process.";
									errorLogList.add(new DetailedException("TgtPerformer Skipped", details, null));
									logger.warn(details);
                        	}
                            
                            JSONArray taskMeasures = (JSONArray) currentTaskJSON.get(TASK_MEASURES);
                            taskMeasureSize = taskMeasures.size();
                            if (taskMeasureSize > 0) {
                                for (Object conceptToProcess : taskMeasures) {
                                    
                                    JSONObject conceptJSON = (JSONObject) conceptToProcess;
                                
									Concept conceptToAdd = generateConcept(conceptJSON, parentName, nameMap,
											learnerNamesToAssess);
                                    if (conceptToAdd != null) {
                                        currentTaskSubConcepts.getConcept().add(conceptToAdd);
                                    }
                                }
                            } else {
                                Conditions defaultConditions = generateDefaultConditions(learnerNamesToAssess);
                                currentTaskConcept.setConditionsOrConcepts(defaultConditions);
                            }
                        }
							catch (Exception e) {
								String details = "An exception occurred while reading the a teamTask under the xEvent named "
										+ parentName + " in the xTSP file. " + "Skipping the teamTask with the error"
										+ ". Attempting to continue the import process.";
								errorLogList.add(new DetailedException(details, e.toString(), e));
								logger.warn(details, e);
							}
						}
                    } catch (Exception e) {
						String details = "An exception occurred while reading the teamTasks under the xEvent named "
								+ parentName + " in the xTSP file. " + "Skipping any unread teamTasks of " + parentName
								+ ". Attempting to continue the import process.";
						errorLogList.add(new DetailedException(details, e.toString(), e));
						logger.warn(details, e);
                    }
                }
            }
             
                    } catch (Exception e) {
			String details = "An exception occurred while reading the Concepts under the xEvent named " + parentName
					+ " in the xTSP file. " + "Skipping any unread Concepts of " + parentName
					+ ". Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
        }
        
        return conceptsToReturn;
    }
    
    /**
	 * Reads a team's JSON data, and returns the names of any teamRoles that are
	 * part of that team, or any subteams, traversed recursively.
	 * 
     * @param teamJSON The JSONObject containing a team's data. Cannot be null.
	 * @return A List containing names from every teamRole in the specified team or
	 *         its subteams, traversed recursively.
     */
    private List<String> getTeamMembersFromTeamJSON(JSONObject teamJSON) {
    	if (teamJSON == null) {
            throw new IllegalArgumentException("The parameter 'teamJSON' cannot be null.");
        }
    	
		List<String> namesToReturn = new ArrayList<String>();
		
    	Object teamRoles = teamJSON.get("teamRoles");
		if (teamRoles instanceof JSONArray) {
			JSONArray teamRolesJSON = (JSONArray) teamRoles;
			for (Object teamRole : teamRolesJSON) {
				if (teamRole instanceof JSONObject) {
					JSONObject teamRoleJSON = (JSONObject) teamRole;
					String roleId = readJSONAsString(teamRoleJSON.get(ROLE_ID));
					JSONObject roleToAdd = xtspRoleIdMap.get(roleId);
					TeamMember teamMemberToAssess = teamMemberJSONMap.get(roleToAdd);
					namesToReturn.add(teamMemberToAssess.getName());
				}
			}
		}
		
		Object subTeams = teamJSON.get(SUB_TEAMS);
		if (subTeams instanceof JSONArray) {
			JSONArray subTeamsJSON = (JSONArray) subTeams;
			for (Object subTeamRef : subTeamsJSON) {
				if (subTeamRef instanceof JSONObject) {
					JSONObject subTeamRefJSON = (JSONObject) subTeamRef;
					JSONObject subTeamFullData = xtspTeamIdMap.get(readJSONAsString(subTeamRefJSON.get(TEAM_ID_JSON)));
					getTeamMembersFromTeamJSON(subTeamFullData);
				}
			}
		}
		return namesToReturn;
	}

	/**
	 * Generates an individual Concept from the xTSP JSON data contained within the
	 * xtspConceptData object.
     * 
	 * @param xtspConceptData      the JSON data containing this Concept's data.
	 *                             Cannot be null.
	 * @param parentName           the name of the Task which this Concepts list is
	 *                             part of. Cannot be null or empty.
	 * @param nameMap.             A map with a String key of names of Tasks and
	 *                             Concepts, and an Integer value. Used to count
	 *                             duplicate names in the xTSP file, so they can be
	 *                             adjusted to prevent duplicate names in GIFT's
	 *                             DKF. Cannot be null.
	 * @param learnerNamesToAssess A list of Strings containing names of learners
	 *                             that will be assessed by conditions that inherit
	 *                             from this concept. Cannot be null.
	 * @return a generated.dkf.Concept with the data that could be read from the
	 *         xTSP file.
     * @throws IllegalArgumentException if xtspJSON is null.
     */
	private Concept generateConcept(JSONObject xtspConceptData, String parentName, Map<String, Integer> nameMap,
			List<String> learnerNamesToAssess) {
        if (xtspConceptData == null) {
            throw new IllegalArgumentException("The parameter 'xtspConceptData' cannot be null.");
        } else if (StringUtils.isBlank(parentName)) {
            throw new IllegalArgumentException("The parameter 'parentName' cannot be blank.");
        } else if (nameMap == null) {
            throw new IllegalArgumentException("The parameter 'nameMap' cannot be null.");
        } else if (learnerNamesToAssess == null) {
            throw new IllegalArgumentException("The parameter 'learnerNamesToAssess' cannot be null.");
        }
        
        Concept conceptToReturn = new Concept();
        
        String conceptName; 
        String conceptId;
        int xtspSubConceptSize;
        
        try {
        	
            conceptName = readJSONAsString(xtspConceptData.get(MSR_TITLE));
            conceptId = readJSONAsString(xtspConceptData.get(MSR_ID));
        } catch (Exception e) {
			String details = "Could not read the name and ID of a Concept with a parent object called " + parentName
					+ ". This Concept cannot be read. " + "Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
        
        if (conceptName == null) {
			String details = "A Concept in the xTSP file with a parent object called " + parentName
					+ " has a null value for a name. This Concept will not be imported. "
					+ "Attempting to continue the import process.";
			errorLogList.add(new DetailedException("A Concept Failed to Import", details, null));
			logger.warn(details);
            return null;
        }
        
        conceptName = getNameAfterCheckingForDuplicates(conceptName, nameMap);
        
        try {
            conceptToReturn.setNodeId(getNodeIdValue());
            conceptToReturn.setName(conceptName);
			conceptToReturn.setExternalSourceId(generateXtspExternalSourceId(XTSP_MEASURE_TYPE, conceptId));
            
            try {
                /* 
				 * Map any criterion ID from this concept to any assessment level that has been
				 * defined, and to its respective Concept. Note that this will only work for
				 * Concepts derived from the xTSP's "taskMeasures" rather than "tasks" (both of
                 * which map to Concepts in GIFT). That is expected.
                 */
                Object evaluation = xtspConceptData.get(XTSP_EVALUATION);
				// xTSP "tasks" will return false in the statement below, and exit this clause
				// without logging a warning.
                if (evaluation instanceof JSONObject) {
                    JSONObject evaluationJSON = (JSONObject) evaluation;
                    Object autoEvalClass = evaluationJSON.get(AUTO_EVAL_CLASS);
					// autoEvalClass may not be a part of a criterion. If it is not, this section is
					// skipped.
                    if (autoEvalClass instanceof JSONObject) {
		                    JSONObject autoEvalClassJSON = (JSONObject) evaluationJSON.get(AUTO_EVAL_CLASS);
		                    JSONArray criteriaJSON = (JSONArray) autoEvalClassJSON.get(LEVEL_CRITERIA);
	                    for (Object criterionEntry : criteriaJSON) {
	                        if (criterionEntry instanceof JSONObject) {
	                            JSONObject criterionEntryJSON = (JSONObject) criterionEntry;
		                            
		                            String levelKey = readJSONAsString(criterionEntryJSON.get(LEVEL_ID));
	                            Object criterionArray = criterionEntryJSON.get(CRITERION);
	                            if (criterionArray instanceof JSONArray) {
	                                JSONArray criterionArrayJSON = (JSONArray) criterionArray;
	                                for (Object assessmentCriterion : criterionArrayJSON) {
	                                    if (assessmentCriterion instanceof JSONObject) {
	                                        JSONObject assessmentCriterionJSON = (JSONObject) assessmentCriterion;
											String criterionId = readJSONAsString(
													assessmentCriterionJSON.get(CRITERION_ID));
		                                        
	                                        AssessmentLevelEnum assessmentLevel = assessmentLevelIdMap.get(levelKey);
	                                        if (assessmentLevel != null) {
	                                            assessmentLevelCriterionIdMap.put(criterionId, assessmentLevel);
	                                            conceptCriterionIdMap.put(criterionId, conceptToReturn);
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
                    }
                }
            } catch (Exception e) {
				String details = "An exception occurred while reading the criteria from the concept called " + conceptName
						+ ". This Concept's criteria will not be imported."
						+ " Attempting to continue the import process.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
            }
            
            JSONArray xtspSubConcepts = (JSONArray) xtspConceptData.get(SUB_MEASURES);
            // This is used to determine if a Concept has more Concepts as children
            boolean doSubConceptsExist = false;
            
            if (xtspSubConcepts != null) {
                xtspSubConceptSize = xtspSubConcepts.size();
                if (xtspSubConceptSize > 0) {
                    doSubConceptsExist = true;
                    Concepts subConcepts = new Concepts();
                    List<Concept> subConceptList = subConcepts.getConcept();
                    conceptToReturn.setConditionsOrConcepts(subConcepts);
                    for (Object subConceptToParse : xtspSubConcepts) {
                        if (subConceptToParse instanceof JSONObject) {
                            JSONObject subConceptJSON = (JSONObject) subConceptToParse;
							Concept conceptToAdd = generateConcept(subConceptJSON, conceptName, nameMap,
									learnerNamesToAssess);
                            if (conceptToAdd != null) {
                                subConceptList.add(conceptToAdd);
                            }
                        }
                    }
                }
            }
            
            /* 
			 * For any Concepts which do not have sub-Concepts, create an Observed
			 * Assessment Condition to allow the Concepts to validate correctly. This will
			 * be replaced with a more complete system for importing Conditions in the
			 * future.
             */
            if (!doSubConceptsExist) {
				generated.json.Measure parentMeasure = exportObjectMapper.readValue(xtspConceptData.toString(), generated.json.Measure.class);
				Conditions conditions = generateTaskConditions(parentMeasure);
				if (conditions == null || conditions.getCondition() == null || conditions.getCondition().size() == 0) {
					conditions = generateDefaultConditions(learnerNamesToAssess);
				}
                conceptToReturn.setConditionsOrConcepts(conditions);
            }
                    
            conceptJSONMap.put(xtspConceptData, conceptToReturn);
            conceptIdMap.put(conceptId, conceptToReturn);
            return conceptToReturn;
        } catch (Exception e) {
			String details = 
					"An exception occurred while importing the Concept named " + conceptName + " from the xTSP file. "
							+ "This Concept cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
    }
    
    /**
	 * Generates DKF task conditions from an XTSP measure.
     * 
	 * @param measureToGetConditionsFrom the xTSP measure that will be used to generate conditions.
	 * 
	 * @return the task conditions.
	 */

	private Conditions generateTaskConditions(generated.json.Measure measureToGetConditionsFrom) {
		Conditions taskConditions = new Conditions();
		List<Condition> conditionList = taskConditions.getCondition();
		
		List<String> msrConditions = measureToGetConditionsFrom.getMsrConditions();
		List<String> methodInputs = measureToGetConditionsFrom.getMethodInputs();
		
		if (methodInputs.size() == msrConditions.size()) {
			for (int i=0; i < methodInputs.size(); i++) {
		        UnmarshalledFile unmarshalledInput;
				try {
					unmarshalledInput = AbstractSchemaHandler.getFromXMLString(methodInputs.get(i), Input.class, null, true);
				
			        if(unmarshalledInput.getUnmarshalled() instanceof Input) {
			            
			            Input importedInput = (Input) unmarshalledInput.getUnmarshalled();
			            Condition generatedCondition = new Condition();
			            generatedCondition.setInput(importedInput);
			            generatedCondition.setConditionImpl(msrConditions.get(i));
			            conditionList.add(generatedCondition);
			        }
				} catch (UnsupportedEncodingException | FileNotFoundException | JAXBException | SAXException e) {
					String details = "Conditions and condition inputs could not be exported to the XTSP.";
					errorLogList.add(new DetailedException(details, e.toString(), e));
					logger.warn(details, e);
				}
			}
		}
		
		return taskConditions;
	}
	
    /**
	 * Creates a generated.dkf.Conditions object containing an Observed Assessment
	 * condition, applying to the learners in learnerNamesToBeAssessed.
     * 
	 * @param learnerNamesToAssess A list of learner names to be assessed by the
	 *                             Observed Assessment condition.
     * @return the Conditions object that contains an Observed Assessment condition
     * @throws IllegalArgumentException if learnerNamestoAssess is null.
     */
    private Conditions generateDefaultConditions(List<String> learnerNamesToAssess) {
        if (learnerNamesToAssess == null) {
            throw new IllegalArgumentException("The parameter 'learnerNamesToAssess' cannot be null.");
        }
        
        Condition defaultCondition = new Condition();
        Input defaultConditionInput = new Input();
        ObservedAssessmentCondition observedAssessmentInput = new ObservedAssessmentCondition();
        TeamMemberRefs observedAssessmentTeamMemberRefs = new TeamMemberRefs();
        observedAssessmentTeamMemberRefs.getTeamMemberRef().addAll(learnerNamesToAssess); 
        observedAssessmentInput.setTeamMemberRefs(observedAssessmentTeamMemberRefs);
                        
        defaultConditionInput.setType(observedAssessmentInput);
        
		String conditionImplName = mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition.class
				.getCanonicalName().substring(DomainKnowledgeUtil.PACKAGE_PATH.length());
        
        defaultCondition.setConditionImpl(conditionImplName);
        defaultCondition.setInput(defaultConditionInput);
        
        Conditions conditions = new Conditions();
        List<Condition> conditionList = conditions.getCondition();
        conditionList.add(defaultCondition);
        
        return conditions;
    }
    
    /**
	 * Generates a list of StartTriggers from the xTSP JSON data contained within
	 * the taskJSON object
     * 
     * @param taskJSON the JSON data containing a Task's data. Cannot be null.
     * @return a StartTriggers object for the task represented by taskJSON.
     * @throws IllegalArgumentException if an argument is null.
     */
    private StartTriggers generateStartTriggers(JSONObject taskJSON) {
        if (taskJSON == null) {
            throw new IllegalArgumentException("The parameter 'taskJSON' cannot be null.");
        }
        
        StartTriggers startTriggers = new StartTriggers();
        
        try {
            List<StartTriggers.Trigger> triggerList = startTriggers.getTrigger();
            
            JSONArray startTriggersJSON = (JSONArray) taskJSON.get(START_TRIGGERS);
            String taskName = readJSONAsString(taskJSON.get(X_EVENT_NAME));
			Object xEventActivateScripts = taskJSON.get(TRIGGER_ACTIVITIES);
            JSONArray xEventActivities = null;
            if (xEventActivateScripts instanceof JSONArray) {
            	xEventActivities = (JSONArray) xEventActivateScripts;
            }
            
            for (Object triggerRef : startTriggersJSON) {
            	if (triggerRef instanceof JSONObject) {
            		JSONObject triggerRefJSON = (JSONObject) triggerRef;
            		String triggerIndex = readJSONAsString(triggerRefJSON.get(TRIGGER_ID_JSON));
	                JSONObject triggerToImport = xtspTriggerIdMap.get(triggerIndex);
	                    
					StartTriggers.Trigger generatedTrigger = generateStartTrigger(triggerToImport, taskName,
							triggerIndex, xEventActivities);
                    
                    /* 
					 * If generatedTrigger is null, then it cannot be properly read and will be
					 * ignored. A warning is logged in generateStartTrigger(), and so is not
					 * redundantly logged here.
                     */
                    if (generatedTrigger != null) {
                        triggerList.add(generatedTrigger);
                    }
            	}
            }
        } catch (Exception e) {
			String details = "An exception occurred while importing a start trigger list from the xTSP file. "
					+ "A list of start triggers cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
        }
        
        return startTriggers;
    }
    
    /**
	 * Generates a list of EndTriggers from the xTSP JSON data contained within the
	 * taskJSON object
     * 
     * @param taskJSON the JSON data containing a Task's data. Cannot be null.
     * @return an EndTriggers object for the task represented by taskJSON.
     * @throws IllegalArgumentException if an argument is null.
     */
    private EndTriggers generateEndTriggers(JSONObject taskJSON) {
        if (taskJSON == null) {
            throw new IllegalArgumentException("The parameter 'taskJSON' cannot be null.");
        }
        
        EndTriggers endTriggers = new EndTriggers();
        
        try {
            List<EndTriggers.Trigger> triggerList = endTriggers.getTrigger();
            
            JSONArray endTriggersJSON = (JSONArray) taskJSON.get(END_TRIGGERS);
            for (Object triggerRef : endTriggersJSON) {
                if (triggerRef instanceof JSONObject) {
                    JSONObject triggerRefJSON = (JSONObject) triggerRef;
                    String triggerIndex = readJSONAsString(triggerRefJSON.get(TRIGGER_ID_JSON));
	                JSONObject triggerToImport = xtspTriggerIdMap.get(triggerIndex);
                    
                    EndTriggers.Trigger generatedTrigger = generateEndTrigger(triggerToImport);
                    
                    /* 
					 * If generatedTrigger is null, then it cannot be properly read and will be
					 * ignored. A warning is logged in generateStartTrigger(), and so is not
					 * redundantly logged here.
                     */
                    if (generatedTrigger != null) {
                        triggerList.add(generatedTrigger);
                    }
                }
            }
        } catch (Exception e) {
			String details = "An exception occurred while importing an end trigger list from the xTSP file. "
					+ "A list of end triggers cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
        }
        
        return endTriggers;
    }

    /**
	 * Generates a StartTriggers.Trigger from the xTSP JSON data contained within
	 * the triggerJSON object
     * 
	 * @param triggerJSON      the JSON data containing a start trigger's data.
	 *                         Cannot be null.
     * @param taskName the name of the task which this trigger is part of.
     * @param triggerIndex the index of this trigger in its parent task.
	 * @param xEventActivities a JSONArray containing any activities that are
	 *                         specified at the xEvent level rather than the trigger
	 *                         level. If an activity is specified at the xEvent
	 *                         level, it should be included in all of the xEvent's
	 *                         triggers.
	 * @return a StartTriggers.Trigger represented by the data in triggerJSON.
	 *         Returns null in the case that the trigger cannot be properly read.
     * @throws IllegalArgumentException if an argument is null.
     */
	private StartTriggers.Trigger generateStartTrigger(JSONObject triggerJSON, String taskName, String triggerIndex,
			JSONArray xEventActivities) {
        int strategiesJSONSize;
        
        if (triggerJSON == null) {
            throw new IllegalArgumentException("The parameter 'triggerJSON' cannot be null.");
        } else if (StringUtils.isBlank(taskName)) {
            throw new IllegalArgumentException("The parameter 'taskName' cannot be empty or null.");
        }
        
        try {            
            StartTriggers.Trigger generatedTrigger = new StartTriggers.Trigger();
            
            Object triggerDelay = triggerJSON.get(TRIGGER_DELAY_TIME);
            String triggerAction = readJSONAsString(triggerJSON.get(TRIGGER_ACTION));
            
            if (triggerDelay != null) {
                try {
                    String triggerDelayString = readJSONAsString(triggerDelay);
                        
                    /* 
					 * Split the time string into hours, minutes, and seconds by removing any
					 * non-numeric characters.
                     */
                    String[] triggerTimeSegments = triggerDelayString.split("[^0-9]");
                    int hours = Integer.parseInt(triggerTimeSegments[0]);
                    int minutes = Integer.parseInt(triggerTimeSegments[1]);
                    int seconds = Integer.parseInt(triggerTimeSegments[2]);
                    
                    Long totalSeconds = seconds + (minutes * 60l) + (hours * 3600l);
                    generatedTrigger.setTriggerDelay(new BigDecimal(totalSeconds));
                } catch (Exception e) {
					
					String details = "An xTSP trigger with triggerAction " + triggerAction
							+ " has a triggerDelay value that could not be imported."
                            + " Is the triggerDelay value in the format of \"HH:MM:SS\"? This trigger's delay value will not be automatically populated."
							+ " Attempting to continue the import process.";
					errorLogList.add(new DetailedException(details, e.toString(), e));
					logger.warn(details, e);
                }
            }
            
            Serializable triggerTypeData = getTriggerTypeData(triggerJSON);
            
            try {
            	if (xEventActivities != null) {
            		addActivitiesFromJSONToStartTrigger(xEventActivities, generatedTrigger, triggerIndex, taskName);
            	}
            	
				Object activateScripts = triggerJSON.get(TRIGGER_ACTIVITIES);
                if (activateScripts instanceof JSONArray) {
                    JSONArray scriptsJSON = (JSONArray) activateScripts;
                                        
                    addActivitiesFromJSONToStartTrigger(scriptsJSON, generatedTrigger, triggerIndex, taskName);
                } else {
                    /* 
					 * If activateScripts is not being used, check for "activateStrategies". The two
					 * options should not be used in the same trigger, but the xTSP can use either.
                     */
                    Object activateStrategies = triggerJSON.get(ACTIVATE_STRATEGIES);
                    if (activateStrategies instanceof JSONArray) {
                        JSONArray strategiesJSON = (JSONArray) activateStrategies;
                        strategiesJSONSize = strategiesJSON.size();
                        if (strategiesJSONSize > 0) {
                            Object strategyToCheck = strategiesJSON.get(0);
                            if (strategyToCheck instanceof JSONObject) {
                                JSONObject strategyJSON = (JSONObject) strategyToCheck;
                                Strategy triggerStrategy = generateStrategyFromXTSP(strategyJSON);
                                if (triggerStrategy != null) {
                                    TriggerMessage triggerMessage = new TriggerMessage();
                                    triggerMessage.setStrategy(triggerStrategy);
                                    generatedTrigger.setTriggerMessage(triggerMessage);
                                }
                            }
                            
                            if (strategiesJSON.size() > 1) {
                                if (logger.isDebugEnabled()) {
									logger.debug("An xTSP trigger with triggerAction " + triggerAction
											+ "contains multiple activateStrategies. "
                                            + "GIFT only supports import of one activateStrategy per trigger. Only this trigger's first activateStrategy is being used.");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
				String details = "An exception occurred while importing the Activities of an xTSP trigger with triggerAction  "
								+ triggerAction + ". "
						+ "This trigger's Activities cannot be read and will be skipped. Attempting to continue the import process.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
            }

            if (triggerTypeData != null) {
                generatedTrigger.setTriggerType(getTriggerTypeData(triggerJSON));
            } else {
                
                /* 
				 * Any warning message that accompanies this return should be handled in
				 * getTriggerTypeData(), when it returns a null value.
                 */
                return null;
            }
                    
            return generatedTrigger;
        } catch (Exception e) {
			String details = "An exception occurred while importing a start trigger from the xTSP file. "
					+ "This start trigger cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
    }
    
    /**
     * Adds activities from a JSONArray of activities to a DKF Start Trigger.
     * 
	 * @param scriptsJSON      The activities to add (in xTSP, these are referred to
	 *                         as "scripts". Cannot be null.
     * @param generatedTrigger The trigger to add activities to. Cannot be null.
	 * @param triggerIndex     The index of the trigger in the xTSP file. Cannot be
	 *                         blank.
	 * @param taskName         The name of the Task that this Start Trigger is part
	 *                         of. Cannot be blank.
     */
	private void addActivitiesFromJSONToStartTrigger(JSONArray scriptsJSON, Trigger generatedTrigger,
			String triggerIndex, String taskName) {
    	if (scriptsJSON == null) {
            throw new IllegalArgumentException("The parameter 'scriptsJSON' cannot be null.");
        } else if (generatedTrigger == null) {
        	throw new IllegalArgumentException("The parameter 'generatedTrigger' canot be null.");
        } else if (StringUtils.isBlank(triggerIndex)) {
            throw new IllegalArgumentException("The parameter 'triggerIndex' cannot be empty or null.");
        } else if (StringUtils.isBlank(taskName)) {
            throw new IllegalArgumentException("The parameter 'taskName' cannot be empty or null.");
        }
    	
    	if (scriptsJSON.size() > 0) {
            Strategy triggerStrategy = new Strategy();
            triggerStrategy.setName(TASK_TRIGGER_STRATEGY);
            for (int i=0; i < scriptsJSON.size(); i++) {
                JSONObject scriptToAdd = (JSONObject) scriptsJSON.get(i);
                String triggerName = "Trigger " + triggerIndex + " of Task " + taskName;
				String activityId = readJSONAsString(scriptToAdd.get("activityId"));
				Serializable activityToAdd = generateActivityFromXTSP(activityId, triggerName, i);
                if (activityToAdd != null) {
                    triggerStrategy.getStrategyActivities().add(activityToAdd);
                }
                TriggerMessage triggerMessage = new TriggerMessage();
                triggerMessage.setStrategy(triggerStrategy);
                generatedTrigger.setTriggerMessage(triggerMessage);
            }
        }
	}

	/**
	 * Generates an EndTriggers.Trigger from the xTSP JSON data contained within the
	 * triggerJSON object
     * 
	 * @param triggerJSON the JSON data containing an end trigger's data. Cannot be
	 *                    null.
	 * @return an EndTriggers.Trigger represented by the data in triggerJSON.
	 *         Returns null in the case that the trigger cannot be properly read.
     * @throws IllegalArgumentException if an argument is null.
     */
    private EndTriggers.Trigger generateEndTrigger(JSONObject triggerJSON) {
        if (triggerJSON == null) {
            throw new IllegalArgumentException("The parameter 'triggerJSON' cannot be null.");
        }
        
        try {            
            EndTriggers.Trigger generatedTrigger = new EndTriggers.Trigger();
            
            Object triggerDelay = triggerJSON.get(TRIGGER_DELAY_TIME);
            String triggerAction = readJSONAsString(triggerJSON.get(TRIGGER_ACTION));
            
            if (triggerDelay != null) {
                try {
                    String triggerDelayString = readJSONAsString(triggerDelay);
                        
                    /* 
					 * Split the time string into hours, minutes, and seconds by removing any
					 * non-numeric characters.
                     */
                    String[] triggerTimeSegments = triggerDelayString.split("[^0-9]");
                    int hours = Integer.parseInt(triggerTimeSegments[0]);
                    int minutes = Integer.parseInt(triggerTimeSegments[1]);
                    int seconds = Integer.parseInt(triggerTimeSegments[2]);
                    
                    Long totalSeconds = seconds + (minutes * 60l) + (hours * 3600l);
                    generatedTrigger.setTriggerDelay(new BigDecimal(totalSeconds));
                } catch (Exception e) {
					String details = "An xTSP trigger with triggerAction " + triggerAction
							+ " has a triggerDelay value that could not be imported."
                            + " Is the triggerDelay value in the format of \"HH:MM:SS\"? This trigger's delay value will not be automatically populated."
							+ " Attempting to continue the import process.";
					errorLogList.add(new DetailedException(details, e.toString(), e));
					logger.warn(details, e);
                }
            }
            
            Serializable triggerTypeData = getTriggerTypeData(triggerJSON);
            
            if (triggerTypeData != null) {
                if (triggerTypeData instanceof ScenarioStarted) {
					String details = "An xTSP endTrigger with triggerAction " + triggerAction
							+ " was resolved into a Scenario Started trigger."
                        + " This is not a valid End Trigger in GIFT. The trigger which contains this action will be skipped."
							+ " Attempting to continue the import process.";
					errorLogList.add(new DetailedException("Invalid End Trigger", details, null));
					logger.warn(details);
                    return null;
                }
                generatedTrigger.setTriggerType(getTriggerTypeData(triggerJSON));
            } else {
                
                /* 
				 * Any warning message that accompanies this return should be handled in
				 * getTriggerTypeData(), when it returns a null value.
                 */
                return null;
            }
                    
            return generatedTrigger;
        } catch (Exception e) {
			String details = "An exception occurred while importing an end trigger from the xTSP file. "
					+ "This end trigger cannot be read. Attempting to continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
    }
    
    /**
	 * Generates the data which contains the details of the trigger. These are
	 * referred to in the generated.dkf Trigger objects as "type." This data is
	 * determined by reading the xTSP JSON data of a start or end trigger.
     * 
	 * @param triggerJSON the JSON data containing an end trigger's data. Cannot be
	 *                    null.
	 * @return the trigger's type, if the xTSP data is valid. Otherwise, logs a
	 *         warning and returns null.
     */
    private Serializable getTriggerTypeData(JSONObject triggerJSON) {
        if (triggerJSON == null) {
            throw new IllegalArgumentException("The parameter 'triggerJSON' cannot be null.");
        }
        
        Serializable triggerTypeToReturn = null;
        
		try {
			generated.json.Trigger xtspTrigger = exportObjectMapper.readValue(triggerJSON.toString(), generated.json.Trigger.class);
			if (xtspTrigger.getTriggerAction() == generated.json.Trigger.TriggerAction.START) {
            ScenarioStarted scenarioStartedType = new ScenarioStarted();
            
            triggerTypeToReturn = scenarioStartedType;
			} else if (xtspTrigger.getTriggerAction() == generated.json.Trigger.TriggerAction.ASSESS) {
				ConceptAssessment conceptAssessment = new ConceptAssessment();
                
				/*
				 * Read the triggerObjects. There should be two of them: a task or measure, and a level.
				 */
				List<generated.json.TriggerObject> objectsList = xtspTrigger.getTriggerObjects();
                    
				for (generated.json.TriggerObject currentTriggerObject : objectsList) {
					if (currentTriggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.TASK || 
							currentTriggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.MEASURE) {
                    /* 
				 * Check the conceptJSONList for a concept with the specified name and ID, and
				 * then get the node ID from the corresponding Concept.
                     */
						for (JSONObject conceptJSON : conceptJSONMap.keySet()) {
                    String currentId = readJSONAsString(conceptJSON.get(TASK_ID_JSON));
							String currentName = null;
							generated.json.TriggerObject.ObjectType triggerObjectType = null;
                    if (currentId != null) {
                        // This is a "task" in the xTSP file
                        currentName = readJSONAsString(conceptJSON.get(TASK_TITLE));
								triggerObjectType = generated.json.TriggerObject.ObjectType.TASK;
							} else if (readJSONAsString(conceptJSON.get(MSR_ID)) != null) {
                        // This is a "taskMeasure" in the xTSP file
                        currentId = readJSONAsString(conceptJSON.get(MSR_ID));
                        currentName = readJSONAsString(conceptJSON.get(MSR_TITLE));
								triggerObjectType = generated.json.TriggerObject.ObjectType.MEASURE;
                    }
                    
							if (currentId != null && triggerObjectType != null) {
								if (currentId.equals(currentTriggerObject.getObjectId().toString()) &&
										triggerObjectType == currentTriggerObject.getObjectType()) {
                        Concept conceptToAssign = conceptJSONMap.get(conceptJSON);
                        if (conceptToAssign != null && conceptToAssign.getNodeId() != null) {
										conceptAssessment.setConcept(conceptToAssign.getNodeId());
                        }
                    }
                }
						}
					} else if (currentTriggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.LEVEL) {
						
						if (currentTriggerObject.getObjectName().equalsIgnoreCase(ABOVE_EXPECTATION_ID_NAME)) {
							conceptAssessment.setResult(ABOVE_EXPECTATION_DKF_REF);
						} else if (currentTriggerObject.getObjectName().equalsIgnoreCase(AT_EXPECTATION_ID_NAME)) {
							conceptAssessment.setResult(AT_EXPECTATION_DKF_REF);
						} else if (currentTriggerObject.getObjectName().equalsIgnoreCase(BELOW_EXPECTATION_ID_NAME)) {
							conceptAssessment.setResult(BELOW_EXPECTATION_DKF_REF);
						} else if (currentTriggerObject.getObjectName().equalsIgnoreCase(UNKNOWN_ID_NAME)) {
							conceptAssessment.setResult(UNKNOWN_ID_NAME);
						}						
					}
					
					if (conceptAssessment.getConcept() == null || conceptAssessment.getResult() == null) {
						triggerTypeToReturn = null;
                } else {
						triggerTypeToReturn = conceptAssessment;
                }
				}
                
			} else if (xtspTrigger.getTriggerAction() == generated.json.Trigger.TriggerAction.COMPLETE) {
                
                
				/*
				 * Read the triggerObjects. There should be one of them: a task or measure, and a level.
				 */
				List<generated.json.TriggerObject> objectsList = xtspTrigger.getTriggerObjects();
                
				if (objectsList.size() > 1) {
					logger.warn("A trigger with the triggerAction 'complete' could not be imported because it has more than one triggerObject, which is invalid."
							+ "This trigger will be skipped. Attempting to continue the import process.");
					triggerTypeToReturn = null;
				} else {
					generated.json.TriggerObject triggerObject = objectsList.get(0);
                	    
					TaskEnded taskEnded = new TaskEnded();
	                	
					if (triggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.XEVENT) {
						for (JSONObject taskJSON : taskJSONMap.keySet()) {
							String currentName = readJSONAsString(taskJSON.get(X_EVENT_NAME));
							String currentId = readJSONAsString(taskJSON.get(X_EVENT_ID));
							
							if (currentId.equals(triggerObject.getObjectId().toString()) && currentName.equals(triggerObject.getObjectName())) {
								Task taskToAssign = taskJSONMap.get(taskJSON);
								if (taskToAssign != null && taskToAssign.getNodeId() != null) {
									taskEnded.setNodeId(taskToAssign.getNodeId());
								}
							}
						}
						
						if (taskEnded.getNodeId() != null) {
							triggerTypeToReturn = taskEnded;
						}
					} else if (triggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.TASK ||
						triggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.MEASURE) {
						
						ConceptEnded conceptEnded = new ConceptEnded();
						
	                        /* 
						 * Check the conceptJSONList for a concept with the specified name and ID, and
						 * then get the node ID from the corresponding Concept.
	                         */
	                    for (JSONObject conceptJSON : conceptJSONMap.keySet()) {
	                        String currentId = readJSONAsString(conceptJSON.get(TASK_ID_JSON));
							String currentName = null;
							generated.json.TriggerObject.ObjectType triggerObjectType = null;
	                        if (currentId != null) {
	                            // This is a "task" in the xTSP file
	                            currentName = readJSONAsString(conceptJSON.get(TASK_TITLE));
								triggerObjectType = generated.json.TriggerObject.ObjectType.TASK;
							} else if (readJSONAsString(conceptJSON.get(MSR_ID)) != null) {
	                            // This is a "taskMeasure" in the xTSP file
	                            currentId = readJSONAsString(conceptJSON.get(MSR_ID));
	                            currentName = readJSONAsString(conceptJSON.get(MSR_TITLE));
								triggerObjectType = generated.json.TriggerObject.ObjectType.MEASURE;
	                        }
	                        
							if (currentName != null && currentId != null && triggerObjectType != null) {
								if (currentId.equals(triggerObject.getObjectId().toString()) &&
										(triggerObjectType == triggerObject.getObjectType())) {
	                            Concept conceptToAssign = conceptJSONMap.get(conceptJSON);
									if (conceptToAssign != null && conceptToAssign.getNodeId() != null) {
										conceptEnded.setNodeId(conceptToAssign.getNodeId());
	                            }
	                        }
	                    }
                	}
						
						if (conceptEnded.getNodeId() != null) {
							triggerTypeToReturn = conceptEnded;
                }
					}
				}
                
			} else if (xtspTrigger.getTriggerAction() == generated.json.Trigger.TriggerAction.ENTER) {
                EntityLocation entityLocation = new EntityLocation();
				// Get the actor with the matching ID
				if (xtspTrigger.getTargets().size() == 1) {
					generated.json.Target targetToRead = xtspTrigger.getTargets().get(0);
					if (targetToRead.getTargetType() == generated.json.Target.TargetType.ACTOR)
					{
						JSONArray targetsJSON = (JSONArray) triggerJSON.get(TRIGGER_TARGETS);
						JSONObject targetToReadJSON = (JSONObject) targetsJSON.get(0);
						generated.dkf.EntityLocation.EntityId.TeamMemberRef teamMemberRef = new generated.dkf.EntityLocation.EntityId.TeamMemberRef(); 
						generated.dkf.EntityLocation.EntityId triggerEntityId = new generated.dkf.EntityLocation.EntityId();
						teamMemberRef.setValue(getLearnerFromTriggerTarget(targetToReadJSON));
						triggerEntityId.setTeamMemberRefOrLearnerId(teamMemberRef);
						entityLocation.setEntityId(triggerEntityId);
					}
				}
                
				// Get the overlay with the matching name and ID
				if (xtspTrigger.getTriggerObjects().size() == 1) {
					generated.json.TriggerObject triggerObject = xtspTrigger.getTriggerObjects().get(0);
					if (triggerObject.getObjectName() != null && triggerObject.getObjectId() != null && 
							triggerObject.getObjectType() == generated.json.TriggerObject.ObjectType.OVERLAY) {
						String overlayName = null;
						String overlayId = triggerObject.getObjectId().toString();
                
						for (JSONObject overlayJSON : placeOfInterestJSONMap.keySet()) {
							generated.json.Overlay overlayToCheck = exportObjectMapper.readValue(overlayJSON.toString(), generated.json.Overlay.class);
                
							if (overlayToCheck.getOverlayId().toString().equals(overlayId)) {
								Serializable placeOfInterestToUse = placeOfInterestJSONMap.get(overlayJSON);
								generated.dkf.TriggerLocation triggerLocation = new generated.dkf.TriggerLocation();
                
								if (placeOfInterestToUse instanceof generated.dkf.Point) {
									generated.dkf.Point pointToUse = (generated.dkf.Point) placeOfInterestToUse;
									overlayName = pointToUse.getName();
									generated.dkf.PointRef pointRef = new generated.dkf.PointRef();
									pointRef.setValue(overlayName);
									
									if (overlayToCheck.getRadius() != null) {
										pointRef.setDistance(new BigDecimal(overlayToCheck.getRadius()));
									}
									
                        triggerLocation.setPointRef(pointRef);
                        entityLocation.setTriggerLocation(triggerLocation);
                            }                          
                        }
                    }
                }
				}

				if (entityLocation.getEntityId() != null && entityLocation.getTriggerLocation() != null) {
                    triggerTypeToReturn = entityLocation;
				}
                    
			} else if (xtspTrigger.getTriggerAction() == generated.json.Trigger.TriggerAction.ACTIVATE) {
                StrategyApplied strategyApplied = new StrategyApplied();
                
				if (xtspTrigger.getTargets().size() == 1) {
					generated.json.Target triggerTarget = xtspTrigger.getTargets().get(0);
					if (triggerTarget.getTargetType() == generated.json.Target.TargetType.STRATEGY &&
							triggerTarget.getTargetId() != null) {
						String strategyId = triggerTarget.getTargetId().toString();
                
						for (JSONObject strategyJSON : strategiesJSONMap.keySet()) {
							generated.json.Strategy strategyToCheck = exportObjectMapper.readValue(strategyJSON.toString(), generated.json.Strategy.class);
                        
							if (strategyToCheck.getStrategyId().toString().equals(strategyId)) {
                            strategyApplied.setStrategyName(strategiesJSONMap.get(strategyJSON).getName());
								
								if (strategyApplied.getStrategyName() != null) {
									triggerTypeToReturn = strategyApplied;
                        }
                    }
                }
					}
				}
                
                    
                }
		} catch (JsonProcessingException e) {
			String details = "Failed to get trigger type data";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            }
        
        return triggerTypeToReturn;
    }
        
    /**
	 * Gets a learner name from a target JSONObject in the xTSP file. A target can
	 * point to a number of different possible structures in the xTSP file. This
	 * determines what learner names fall within the specified target in the team
	 * hierarchy.
     * 
     * @param triggerTarget The JSONObject containing the target data.
     * @return A string containing the learner name that corresponds to the target.
     */
    private String getLearnerFromTriggerTarget(JSONObject triggerTarget) {
        
        String targetType = readJSONAsString(triggerTarget.get(TARGET_TYPE));
        String targetId = readJSONAsString(triggerTarget.get(TARGET_ID));
        
        if (targetType.equals(ROLE)) {
            for (JSONObject teamMemberToCheck : teamMemberJSONMap.keySet()) {
                String currentId = readJSONAsString(teamMemberToCheck.get(ROLE_ID_JSON));
                
				if (currentId.equals(targetId)) {
                    String newLearnerName = teamMemberJSONMap.get(teamMemberToCheck).getName();
                    // Avoid adding duplicate names to the list.
                        return newLearnerName;
                }
            }
        } else if (targetType.equals(ACTOR)) {
            for (JSONObject teamMemberToCheck : teamMemberJSONMap.keySet()) {
                JSONObject actorJSON = (JSONObject) teamMemberToCheck.get(ROLE_ACTOR);
                String currentId = readJSONAsString(actorJSON.get(ACTOR_ID_JSON));
                
				if (currentId.equals(targetId)) {
                    String newLearnerName = teamMemberJSONMap.get(teamMemberToCheck).getName();
                    // Avoid adding duplicate names to the list.
                        return newLearnerName;
                }
            }
        }
        
        // If no learner name can be found, return null.
        return null;
    }

    /**
	 * Takes a name, checks whether there are any instances of that name in the
	 * nameMap, and returns a modified name if there have been. The modified name is
	 * the original name with a parenthetical number after it for the number of
	 * duplicates that exist (so if a concept called "Example" is used three times,
	 * you get "Example", "Example (1)", and "Example (2)". Updates the nameMap if a
	 * duplicate is found.
     * 
     * @param nameToCheck The name being checked. Cannot be null.
	 * @param nameMap     A map of all names to compare against, associated with the
	 *                    number of times they have been used so far in the xTSP
	 *                    import. This map is updated if a new duplicate name is
	 *                    found; the number associated with that key is incremented.
	 *                    Cannot be null.
	 * @return The name to use in the DKF. This is the original name if no duplicate
	 *         was found, or a modified name if duplicates exist.
     * @throws IllegalArgumentException if a parameter is null.
     */
    private String getNameAfterCheckingForDuplicates(String nameToCheck, Map<String, Integer> nameMap) {
        if (nameToCheck == null) {
            throw new IllegalArgumentException("The parameter 'nameToCheck' cannot be null.");
        } else if (nameMap == null) {
            throw new IllegalArgumentException("The parameter 'nameMap' cannot be null.");
        }
        
        String nameToReturn;
        
        if (nameMap.containsKey(nameToCheck)) {
            /*
			 * If the name already exists, increment its counter and modify the nameToReturn
			 * with the count, to prevent duplicate entries.
             */
            nameMap.put(nameToCheck, nameMap.get(nameToCheck) + 1);
            nameToReturn = new String(nameToCheck + " (" + nameMap.get(nameToCheck) + ")");
        } else {
            /*
			 * If the name does not exist, add it to the name map so that later entries can
			 * be checked to prevent duplicates. Return the original name.
             */
            nameMap.put(nameToCheck, 0);
            nameToReturn = new String(nameToCheck);
        }
        
        return nameToReturn;
    }
    
    /**
	 * Generate DKF Instructional Strategies from the XTSP.
	 * 
	 * @return strategiesResult the generated DKF strategies.
	 */
	generated.dkf.Actions.InstructionalStrategies generateInstructionalStrategiesFromXTSP() {
		generated.dkf.Actions.InstructionalStrategies strategiesResult = new generated.dkf.Actions.InstructionalStrategies();
		
		for (Object xtspStrategyObj : xtspStrategies) {
			if (xtspStrategyObj instanceof JSONObject) {
				JSONObject strategyJSON = (JSONObject) xtspStrategyObj;
				Strategy generatedStrategy = generateStrategyFromXTSP(strategyJSON);
				strategiesJSONMap.put(strategyJSON, generatedStrategy);
				strategiesResult.getStrategy().add(generatedStrategy);
			}
		}
		
		return strategiesResult;
	}

    /**
	 * Parses the JSON containing Strategy data, and generates a
	 * generated.dkf.Strategy from that, if possible.
	 * 
     * @param scriptJSON The JSON object containing Strategy data to be parsed.
     * 
	 * @return the generated Strategy, or null, if a Strategy could not be properly
	 *         read.
     * @throws IllegalArgumentException if scriptJSON is null.
     */
    private Strategy generateStrategyFromXTSP(JSONObject strategyJSON) {
        if (strategyJSON == null) {
			throw new IllegalArgumentException("The parameter 'strategyJSON' cannot be null.");
        }

        Strategy generatedStrategy = new Strategy();

        try {
            String strategyName = readJSONAsString(strategyJSON.get(STRATEGY_NAME));
            
            if (StringUtils.isBlank(strategyName)) {
                strategyName = "New Strategy";
            }
            
            strategyName = getNameAfterCheckingForDuplicates(strategyName, strategyNameMap);
            generatedStrategy.setName(strategyName);
            try {
				JSONArray scriptArray = (JSONArray) strategyJSON.get(ACTIVITIES);
                for (int i=0; i < scriptArray.size(); i++) {
                    Object currentScript = scriptArray.get(i);
                    if (currentScript instanceof JSONObject) {
                        JSONObject currentScriptJSON = (JSONObject) currentScript;
						String activityId = readJSONAsString(currentScriptJSON.get("activityId"));
						Serializable activityToAdd = generateActivityFromXTSP(activityId, strategyName, i);
                        if (activityToAdd != null) {
                            generatedStrategy.getStrategyActivities().add(activityToAdd);
                        }
                    }
                }
                Object strategyStressLevel = strategyJSON.get(STRESS_LEVEL);
                Object strategyDifficulty = strategyJSON.get(DIFFICULTY_LEVEL);
                
                /*
				 * stressLevel and difficultyLevel are optional. If one is absent, it will be
				 * null, resulting in instanceof evaluating to false.
                 */
				BigDecimal bigDecimalStressValue = null;
                if (strategyStressLevel instanceof String) {
                    /* 
					 * NOTE: String is not officially supported in the xTSP for this value, but is
					 * used in some of the existing xTSP files. GIFT supports this to avoid
					 * compatibility issues with older versions.
                     */
                    String stressLevelValue = readJSONAsString(strategyStressLevel);
					bigDecimalStressValue = new BigDecimal(stressLevelValue);
                } else if (strategyStressLevel instanceof Long) {
                    Long stressLevelValue = (Long) strategyStressLevel;
					bigDecimalStressValue = new BigDecimal(stressLevelValue);
                } else if (strategyStressLevel instanceof Double) {
                    Double stressLevelValue = (Double) strategyStressLevel;
					bigDecimalStressValue = new BigDecimal(stressLevelValue);
                }
                
				if (bigDecimalStressValue != null) {
					if (bigDecimalStressValue.compareTo(STRATEGY_STRESS_MAX) > 0) {
						logger.warn("A strategy named " + strategyName + " has a stress of " + bigDecimalStressValue.doubleValue() + ", which is too high for the DKF schema. It is being set to " + STRATEGY_STRESS_MAX + " upon import.");
						generatedStrategy.setStress(STRATEGY_STRESS_MAX);
					} else if (bigDecimalStressValue.compareTo(STRATEGY_STRESS_MIN) < 0) {
						logger.warn("A strategy named " + strategyName + " has a stress of " + bigDecimalStressValue.doubleValue() + ", which is too low for the DKF schema. It is being set to " + STRATEGY_STRESS_MIN + " upon import.");
						generatedStrategy.setStress(STRATEGY_STRESS_MIN);
					} else {
						generatedStrategy.setStress(bigDecimalStressValue);
					}
				}
	
                if (strategyDifficulty instanceof Long) {
                    Long difficultyValue = (Long) strategyDifficulty;
					if (difficultyValue > STRATEGY_DIFFICULTY_MAX) {
						logger.warn("A strategy named " + strategyName + " has a difficulty of " + difficultyValue + ", which is too high for the DKF schema. It is being set to " + STRATEGY_DIFFICULTY_MAX + " upon import.");
						generatedStrategy.setDifficulty(new BigDecimal(STRATEGY_DIFFICULTY_MAX));
					}
					else if (difficultyValue < STRATEGY_DIFFICULTY_MIN) {
						logger.warn("A strategy named " + strategyName + " has a difficulty of " + difficultyValue + ", which is too low for the DKF schema. It is being set to " + STRATEGY_DIFFICULTY_MIN + " upon import.");
						generatedStrategy.setDifficulty(new BigDecimal(STRATEGY_DIFFICULTY_MIN));
					}
					else {
                    generatedStrategy.setDifficulty(new BigDecimal(difficultyValue));
					}
                } else if (strategyDifficulty instanceof Double) {
                    Double difficultyValue = (Double) strategyDifficulty;
					if (difficultyValue > STRATEGY_DIFFICULTY_MAX) {
						logger.warn("A strategy named " + strategyName + " has a difficulty of " + difficultyValue + ", which is too high for the DKF schema. It is being set to " + STRATEGY_DIFFICULTY_MAX + " upon import.");
						generatedStrategy.setDifficulty(new BigDecimal(STRATEGY_DIFFICULTY_MAX));
					}
					else if (difficultyValue < STRATEGY_DIFFICULTY_MIN) {
						logger.warn("A strategy named " + strategyName + " has a difficulty of " + difficultyValue + ", which is too low for the DKF schema. It is being set to " + STRATEGY_DIFFICULTY_MIN + " upon import.");
						generatedStrategy.setDifficulty(new BigDecimal(STRATEGY_DIFFICULTY_MIN));
					}
					else {
                    generatedStrategy.setDifficulty(new BigDecimal(difficultyValue));
                }
				}
                
            } catch (Exception e) {
				String details = "A strategy named " + strategyName + " could not be imported from the xTSP file. "
						+ "Attempting to skip this strategy and continue the import process.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
                generatedStrategy = null;
            }
        } catch (Exception e) {
			String details = "A strategy could not be imported from the xTSP file. "
					+ "Attempting to skip this strategy and continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            generatedStrategy = null;
        }
        
        return generatedStrategy;
    }
    
    /**
	 * Parses the JSON containing Strategy data, and generates a Serializable from
	 * that which can be added to a Strategy, if possible.
	 * 
	 * @param activityId         The ID of the activity to be parsed.
	 * @param parentStrategyName The name of the parent object (i.e. strategy or
	 *                           task) which this is part of. Used to identify this
	 *                           Activity in logs, if an exception occurs.
	 * @param scriptIndex        The index of this script in a larger array. Used to
	 *                           identify this Activity in logs, if an exception
	 *                           occurs.
	 * @return a Serializable object representing a Strategy's activity. Returns
	 *         null if an Activity cannot be properly read.
     */
	private Serializable generateActivityFromXTSP(String activityId, String parentName, int scriptIndex) {
		if (StringUtils.isBlank(activityId)) {
			throw new IllegalArgumentException("The parameter 'scriptJSON' cannot be empty or null.");
        } else if (StringUtils.isBlank(parentName)) {
            throw new IllegalArgumentException("The parameter 'parentName' cannot be empty or null.");
        }
        
		Serializable generatedDkfActivity = null;

        try {
			for (Object currentActivityObj : xtspActivities) {
				if (currentActivityObj instanceof JSONObject) {
					JSONObject currentActivityJSON = (JSONObject) currentActivityObj;
					generated.json.Activity__1 generatedXtspActivity = exportObjectMapper.readValue(currentActivityJSON.toString(), generated.json.Activity__1.class);
            
					if (generatedXtspActivity.getActivityId().toString().equals(activityId)) {
            
						StrategyHandler strategyHandler = new StrategyHandler();
						strategyHandler.setImpl(DEFAULT_STRATEGY_HANDLER_CLASS);
            	
						Object stressItemArray = currentActivityJSON.get(STRESS_LEVEL);
						String stressType = null;
            	
						if (stressItemArray instanceof JSONArray) {
            
							JSONArray stressItemsJSON = (JSONArray) stressItemArray;
							for (Object stressItemJSON : stressItemsJSON) {
								if (stressItemJSON instanceof JSONObject) {
									generated.json.StressLevel stressLevel = exportObjectMapper.readValue(stressItemJSON.toString(), generated.json.StressLevel.class);
									if (stressLevel.getStressType() != null) {
										stressType = stressLevel.getStressType().toString();
            }
								}
							}				
						}
            
						
						if (generatedXtspActivity.getActivityType() == generated.json.Activity__1.ActivityType.CUSTOM_SCRIPT) {
							generated.dkf.ScenarioAdaptation newScenarioAdaptation = new generated.dkf.ScenarioAdaptation();
							generated.dkf.EnvironmentAdaptation.Script newScript = new generated.dkf.EnvironmentAdaptation.Script();
							generated.dkf.EnvironmentAdaptation newEnvironmentAdaptation = new generated.dkf.EnvironmentAdaptation();
							newScript.setValue(generatedXtspActivity.getScriptCommand());
							newEnvironmentAdaptation.setType(newScript);
							newScenarioAdaptation.setEnvironmentAdaptation(newEnvironmentAdaptation);
							newScenarioAdaptation.setDescription(generatedXtspActivity.getActivityName());
							newScenarioAdaptation.setStrategyHandler(strategyHandler);
							
                if (stressType != null) {
                    StrategyStressCategory stressCategory = getStressCategoryFromString(stressType);
                    if (stressCategory != null) {
									newScript.setStressCategory(stressCategory);
                    }
                }
							generatedDkfActivity = newScenarioAdaptation;
                
						} else if (generatedXtspActivity.getActivityType() == generated.json.Activity__1.ActivityType.ACTOR_INTERVENTION) {
                                
                InstructionalIntervention messageIntervention = new InstructionalIntervention();
                Feedback feedbackValue = new Feedback();
                Message feedbackMessage = new Message();
							
                if (stressType != null) {
                    StrategyStressCategory stressCategory = getStressCategoryFromString(stressType);
                    if (stressCategory != null) {
                        messageIntervention.setStressCategory(stressCategory);
                    }
                }
							
                Message.Delivery messageDelivery = new Message.Delivery();
                Message.Delivery.InTrainingApplication messageInTrainingApp = new Message.Delivery.InTrainingApplication();
                InTutor messageInTutor = new InTutor();
                
				// Set default values of parameters, then update the parameters to match any
				// values specified in the xTSP file.
                boolean doesBeep = false;
                boolean doesFlash = false;
                messageInTrainingApp.setEnabled(BooleanEnum.FALSE);
                messageInTutor.setMessagePresentation(MessageFeedbackDisplayModeEnum.TEXT_ONLY.getName());
                
							messageDelivery.setInTrainingApplication(messageInTrainingApp);
							messageDelivery.setInTutor(messageInTutor);
							feedbackMessage.setDelivery(messageDelivery);
							feedbackMessage.setContent(getTextInsideDelimiters(generatedXtspActivity.getScriptCommand(), "showText (", ")"));
							feedbackValue.setFeedbackPresentation(feedbackMessage);
							messageIntervention.setFeedback(feedbackValue);
							messageIntervention.setStrategyHandler(strategyHandler);
                
                                if (messageInTutor != null) {
                                    if (doesFlash && doesBeep) {
                                        messageInTutor.setTextEnhancement(TextFeedbackDisplayEnum.BEEP_AND_FLASH.getName());
                                    } else if (doesFlash) {
                                        messageInTutor.setTextEnhancement(TextFeedbackDisplayEnum.FLASH_ONLY.getName());
                                    } else if (doesBeep) {
                                        messageInTutor.setTextEnhancement(TextFeedbackDisplayEnum.BEEP_ONLY.getName());
                                    } else {
                                        messageInTutor.setTextEnhancement(TextFeedbackDisplayEnum.NO_EFFECT.getName());
                                    }
                                }
							
							generatedDkfActivity = messageIntervention;
	
                            }
                        }
                    }
                }
                
                        } catch (Exception e) {
			String details = "Script number " + scriptIndex + " of the object called " + parentName
					+ " could not be imported from the xTSP file. "
					+ "Attempting to skip this script and continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
                            return null;
                        }
        
		return generatedDkfActivity;
                    }
                
	/** 
	 * Modifies a string by getting the contents between two substrings.
	 * 
	 * @param scriptCommand the script command value of the generated XTSP activity.
	 * @param startDelimiter the starting substring
	 * @param endDelimiter the ending substring
	 * 
	 * @return textToReturn the modified String.
	 */
	private String getTextInsideDelimiters(String scriptCommand, String startDelimiter, String endDelimiter) {
		String textToReturn = scriptCommand;
		int indexOfFirstDelimiter = scriptCommand.indexOf(startDelimiter);
		int indexOfEndDelimiter = scriptCommand.lastIndexOf(endDelimiter);
                    
		/*
		 * Only modify the text if both start and end delimiters are found. Otherwise, return the original text in its entirety.
		 */
		if (indexOfFirstDelimiter >= 0 && indexOfEndDelimiter >= 0) {
			textToReturn = textToReturn.substring(indexOfFirstDelimiter+startDelimiter.length(), indexOfEndDelimiter);
                }
        
		return textToReturn;
    }
    
    /**
	 * Gets a StrategyStressCategory based on a string in an xTSP file. Ignores case
	 * when reading strings.
     * 
	 * @param stressTypeString The string representing a stress category from the
	 *                         xTSP file.
	 * @return the corresponding StrategyStressCategory if a match exists. Returns
	 *         null if the stressTypeString doesn't match a StrategyStressCategory
	 *         or if stressTypeString's value is null.
     */
    private StrategyStressCategory getStressCategoryFromString(String stressTypeString) {
        StrategyStressCategory categoryToReturn = null;
        
        if (stressTypeString != null) {
            if (stressTypeString.equalsIgnoreCase(StrategyStressCategory.COGNITIVE.value())) {
                categoryToReturn = StrategyStressCategory.COGNITIVE;
            } else if (stressTypeString.equalsIgnoreCase(StrategyStressCategory.ENVIRONMENTAL.value())) {
                categoryToReturn = StrategyStressCategory.ENVIRONMENTAL;
            } else if (stressTypeString.equalsIgnoreCase(StrategyStressCategory.PHYSIOLOGICAL.value())) {
                categoryToReturn = StrategyStressCategory.PHYSIOLOGICAL;
            }
        }
        
        return categoryToReturn;
    }

    /**
	 * Parses the JSON data containing XTSP overlay features, and generates GIFT
	 * Places of Interest as a result.
	 * 
     * @param xtspPlacesOfInterest a PlacesOfInterest object to modify
	 * @param allowedCoordinateTypes A set of strings representing allowed
	 *                               coordinate types for the current training
	 *                               application. e.g. "GCC" or "GDC"
     * @return GIFT places of interest based on the XTSP data
     * @throws IllegalArgumentException if an argument is null.
     */
	private PlacesOfInterest generatePlacesOfInterestFromXTSP(PlacesOfInterest xtspPlacesOfInterest,
			Set<String> allowedCoordinateTypes) {
       
        if (xtspPlacesOfInterest == null) {
            throw new IllegalArgumentException("The parameter 'xtspPlacesOfInterest' cannot be null.");
        } else if (allowedCoordinateTypes == null) {
            throw new IllegalArgumentException("The parameter 'allowedCoordinateTypes' cannot be null.");
        }
        
		if (!allowedCoordinateTypes.contains(TrainingApplicationEnum.GDC_COORDINATE_NAME)
				&& !allowedCoordinateTypes.contains(TrainingApplicationEnum.GCC_COORDINATE_NAME)) {
			String details = 
					"The current training application does not support GDC or GCC coordinates, so Places of Interest cannot be imported from the XTSP.";
			errorLogList.add(new DetailedException("Places of Interest Cannot Be Imported", details, null));
			logger.warn(details);
            return xtspPlacesOfInterest;
        }
        
        /*
		 * Loop through all the overlays, and convert those to places of interest that
		 * GIFT can use.
         */
        List<Serializable> placesList = xtspPlacesOfInterest.getPointOrPathOrArea();
		Set<String> foundIds = new HashSet<>();
        
        for (Object overlayToImport : xtspOverlays) {
            try {
            	JSONObject overlayJSON = (JSONObject) overlayToImport;
            	
                String featureName = readJSONAsString(overlayJSON.get(OVERLAY_NAME));
				String featureId = readJSONAsString(overlayJSON.get(OVERLAY_ID_JSON));
                if(foundIds.contains(featureId)) {
                    
                    /* XTSP had an overlay with a duplicate ID, which is invalid. Ignore the duplicate */
                	String details = "Ignoring duplicate overlay ID of " + featureId + " for overlay named " + featureName;
                	errorLogList.add(new DetailedException("Duplicate Overlay ID", details, null));
                	logger.warn(details);
                    continue;
                    
                } else {
                    foundIds.add(featureId);
                }
                String geometryType = readJSONAsString(overlayJSON.get(SHAPE));
                
                JSONArray coordinatesJSONArray = (JSONArray) overlayJSON.get(POINTS);
                JSONArray anchorJSONArray = (JSONArray) overlayJSON.get(ANCHOR);

                List<JSONArray> coordinatesList = new ArrayList<JSONArray>();

                if (anchorJSONArray != null) {
	                for (Object currentCoordinate : anchorJSONArray) {
	                	if (currentCoordinate instanceof JSONArray) {
	                		coordinatesList.add((JSONArray) currentCoordinate);
	                	}
	                }
                }
                
                if (coordinatesJSONArray != null) {
	                for (Object currentCoordinate : coordinatesJSONArray) {
	                	if (currentCoordinate instanceof JSONArray) {
	                		coordinatesList.add((JSONArray) currentCoordinate);
	                	}
	                }
                }
                
                if (geometryType.toUpperCase().equals(POINT.toUpperCase())) {
                    try {
                        Point pointToAdd = new Point();
                        pointToAdd.setName(featureName);
                        
                        if (coordinatesList.size() >= 1) {
                        	
							// Display a warning if there is more than one coordinate, but continue to
							// import and just use the first coordinate.
                        	if (coordinatesList.size() > 1) {
								String details = "The geoCoordinates for the XTSP overlay feature named " + featureName
										+ " represent a point, but contains more than one point coordinates."
                                        + " A point shape should have exactly one coordinate."
										+ " Attempting to read the first coordinate in this overlay feature and use that as the point's coordinate.";
								errorLogList.add(new DetailedException("Overlay Contains More Than One Coordinate", details, null));
								logger.warn(details);
                        	}
                        	
                            JSONArray pointCoordinateJSON = coordinatesList.get(0);
                            generated.dkf.GDC pointGdcValue = new generated.dkf.GDC();
							pointGdcValue
									.setLongitude(new BigDecimal((Double) pointCoordinateJSON.get(LONGITUDE_INDEX)));
                            pointGdcValue.setLatitude(new BigDecimal((Double) pointCoordinateJSON.get(LATITUDE_INDEX)));
                            Object altitudeValue = pointCoordinateJSON.get(ALTITUDE_INDEX);
                            if (altitudeValue instanceof Double) {
								pointGdcValue
										.setElevation(new BigDecimal((Double) pointCoordinateJSON.get(ALTITUDE_INDEX)));
                            } else if (altitudeValue instanceof Long) {
								pointGdcValue
										.setElevation(new BigDecimal((Long) pointCoordinateJSON.get(ALTITUDE_INDEX)));
                            }
                            
							Coordinate convertedCoordinate = convertToAllowedCoordinate(pointGdcValue,
									allowedCoordinateTypes);
                            
                            if (convertedCoordinate != null) {
                                pointToAdd.setCoordinate(convertedCoordinate);
                                pointToAdd.setName(featureName);
                                
                                placesList.add(pointToAdd);
                                placeOfInterestJSONMap.put(overlayJSON, pointToAdd);
                            } else {
								String details = "A coordinate with a null value cannot be added to the feature "
										+ featureName + ".";
								errorLogList.add(new DetailedException("Null Coordinate Value", details, null));
								logger.warn(details);
                            }
                        } else {
							String details = "The geoCoordinates for the XTSP overlay feature named " + featureName
									+ " represent a point, but do not contain any point values."
                                    + " A point shape must have at least one coordinate."
									+ " Attempting to skip this feature and continue the import process.";
							errorLogList.add(new DetailedException("Null GeoCoordinate Values", details, null));
							logger.warn(details);
                        }
                    } catch (Exception e) {
						String details = "Failed to convert an XTSP overlay feature named '" + featureName
								+ "' into a GIFT Point. Attempting to skip this Point and continue the import process.";
						errorLogList.add(new DetailedException(details, e.toString(), e));
						logger.warn(details, e);
                    }
                    
                } else if (geometryType.toUpperCase().equals(LINE.toUpperCase())) {
                    try {
                        Path pathToAdd = new Path();
                        pathToAdd.setName(featureName);
                        
                        List<Coordinate> pathCoordinatesList = new LinkedList<Coordinate>();
                        
                        /*
                         * Get all the coordinates used to create the path.
                         */
                        for (JSONArray currentCoordinate : coordinatesList) {                              
                                                        
                            generated.dkf.GDC pointGdcValue = new generated.dkf.GDC();
                            pointGdcValue.setLongitude(new BigDecimal((Double) currentCoordinate.get(LONGITUDE_INDEX))); 
                            pointGdcValue.setLatitude(new BigDecimal((Double) currentCoordinate.get(LATITUDE_INDEX)));
                            Object altitudeValue = currentCoordinate.get(ALTITUDE_INDEX);
                            if (altitudeValue instanceof Double) {
								pointGdcValue
										.setElevation(new BigDecimal((Double) currentCoordinate.get(ALTITUDE_INDEX)));
                            } else if (altitudeValue instanceof Long) {
								pointGdcValue
										.setElevation(new BigDecimal((Long) currentCoordinate.get(ALTITUDE_INDEX)));
                            }
                            
							Coordinate convertedCoordinate = convertToAllowedCoordinate(pointGdcValue,
									allowedCoordinateTypes);
                            
                            if (convertedCoordinate != null) {
                                pathCoordinatesList.add(convertedCoordinate);
                            } else {
								String details = "A coordinate with a null value cannot be added to the feature "
										+ featureName + ".";
								errorLogList.add(new DetailedException("Null Coordinate Value", details, null));
								logger.warn(details);
                            }
                        }
                        
                       /*
                        * Assemble segments from the coordinates used to create the path.
                        */
                        List<Segment> segmentList = pathToAdd.getSegment(); 
                        
                        Segment segmentToModify = new Segment();
                        
                        for (Coordinate currentSegmentCoordinate : pathCoordinatesList) {
                            if (segmentToModify.getStart() == null) {
                                /*
								 * First, if the start of a segment has not yet been defined, create a new one
								 * with the current coordinate (this should only occur on the first segment).
                                 */
                                segmentToModify.setStart(new Start());
                                segmentToModify.getStart().setCoordinate(currentSegmentCoordinate);
                            } else if (segmentToModify.getEnd() == null) {
                                /*
								 * If the start of a segment has been defined but not the end, end the segment
								 * with the current coordinate and then create a new segment that also starts
								 * with the current coordinate. Segments (other than the first) will start with
								 * the end of the previous segment.
                                 */
                                segmentToModify.setEnd(new End());
                                segmentToModify.getEnd().setCoordinate(currentSegmentCoordinate);
                                segmentToModify.setName("Segment " + (segmentList.size() + 1));
                                segmentToModify.setWidth(new BigDecimal(1.0));
                                segmentList.add(segmentToModify);
                                
                                /*
								 * Do not create a new segment if this is the last coordinate in the list. The
								 * segment would be created and then left unused, otherwise.
                                 */
								if (pathCoordinatesList
										.get(pathCoordinatesList.size() - 1) != currentSegmentCoordinate) {
                                    segmentToModify = new Segment();
                                    segmentToModify.setStart(new Start());
                                    segmentToModify.getStart().setCoordinate(currentSegmentCoordinate);
                                }
                            }
                        }
                        
                        placesList.add(pathToAdd);
                        placeOfInterestJSONMap.put(overlayJSON, pathToAdd);
                    } catch (Exception e) {
						String details = "Failed to convert an XTSP overlay feature named '" + featureName
								+ "' into a GIFT Path. Attempting to skip this Path and continue the import process.";
						errorLogList.add(new DetailedException(details, e.toString(), e));
						logger.warn(details, e);
                    }
                } else if (geometryType.toUpperCase().equals(POLYGON.toUpperCase())) {
                    try {
                        Area areaToAdd = new Area();
                        areaToAdd.setName(featureName);
                        
                        List<Coordinate> areaCoordinatesList = areaToAdd.getCoordinate();
                                                    
                        /*
						 * Get all the coordinates used to create the area. The xTSP file stores
						 * coordinates as three arrays: an array of latitudes, an array of longitudes,
						 * and an array of altitudes. This import process relies on all three arrays
						 * having the same amount of entries, which is checked earlier in this method.
                         */
                        for (JSONArray currentCoordinate : coordinatesList) {
                            
                            generated.dkf.GDC pointGdcValue = new generated.dkf.GDC();
                            pointGdcValue.setLongitude(new BigDecimal((Double) currentCoordinate.get(LONGITUDE_INDEX))); 
                            pointGdcValue.setLatitude(new BigDecimal((Double) currentCoordinate.get(LATITUDE_INDEX)));
                            Object altitudeValue = currentCoordinate.get(ALTITUDE_INDEX);
                            if (altitudeValue instanceof Double) {
								pointGdcValue
										.setElevation(new BigDecimal((Double) currentCoordinate.get(ALTITUDE_INDEX)));
                            } else if (altitudeValue instanceof Long) {
								pointGdcValue
										.setElevation(new BigDecimal((Long) currentCoordinate.get(ALTITUDE_INDEX)));
                            }
                            
							Coordinate convertedCoordinate = convertToAllowedCoordinate(pointGdcValue,
									allowedCoordinateTypes);
                            
                            if (convertedCoordinate != null) {
                                areaCoordinatesList.add(convertedCoordinate);
                            } else {
								String details = "A coordinate with a null value cannot be added to the feature "
										+ featureName + ".";
								errorLogList.add(new DetailedException("Null Coordinate Value", details, null));
								logger.warn(details);
                            }
                        }
                        
                        placesList.add(areaToAdd);
                        placeOfInterestJSONMap.put(overlayJSON, areaToAdd);
                    } catch (Exception e) {
						String details = "Failed to convert an XTSP overlay feature named '" + featureName
								+ "' into a GIFT Area. Attempting to skip this Area and continue the import process.";
						errorLogList.add(new DetailedException(details, e.toString(), e));
						logger.warn(details, e);
                    }
                }
            } catch (Exception e) {
				String details = "Failed to convert an XTSP overlay feature into a GIFT place of interest."
						+ " This is feature number " + (xtspOverlays.indexOf(overlayToImport) + 1) + " out of "
						+ xtspOverlays.size() + " that were successfully parsed."
						+ " Attempting to skip this feature and continue the import process.";
				errorLogList.add(new DetailedException(details, e.toString(), e));
				logger.warn(details, e);
            }
        }
                
        return xtspPlacesOfInterest;
    }
    
    /**
	 * Converts imported XTSP data from the default GDC format to GCC, if GDC is not
	 * supported by the current training app. If neither format is allowed, returns
	 * a null value.
	 * 
     * @param gdcCoordinate The GDC coordinate to convert from
	 * @param allowedCoordinateTypes A set of strings representing the coordinate
	 *                               types that are allowed e.g. "GDC" or "GCC"
     * @return A coordinate of the proper type if possible, or null otherwise.
     * @throws IllegalArgumentException if either argument is null.
     */
    public Coordinate convertToAllowedCoordinate(generated.dkf.GDC gdcCoordinate, Set<String> allowedCoordinateTypes) {
        
        if (gdcCoordinate == null) {
            throw new IllegalArgumentException("The parameter 'gdcCoordinate' cannot be null.");
        } else if (allowedCoordinateTypes == null) {
            throw new IllegalArgumentException("The parameter 'allowedCoordinateTypes' cannot be null.");
        }
        
        Coordinate outputCoordinate = new Coordinate();
        
        if (allowedCoordinateTypes.contains(TrainingApplicationEnum.GDC_COORDINATE_NAME)) {
            outputCoordinate.setType(gdcCoordinate);
        } else if (allowedCoordinateTypes.contains(TrainingApplicationEnum.GCC_COORDINATE_NAME)) {
            generated.dkf.GCC gccCoordinate = CoordinateUtil.getInstance().convertToDkfGCC(gdcCoordinate);
            outputCoordinate.setType(gccCoordinate);
        } else {
			String details = "Neither GDC nor GCC coordinates are allowed, so a coordinate cannot be converted to a useable format. Coordinate set to a null value.";
			errorLogList.add(new DetailedException("Coordinate Can't Be Converted", details, null));
			logger.warn(details);
            outputCoordinate = null;
            
        }
        
        return outputCoordinate;
    }
    
    /**
	 * Parses the JSONArray containing teams and the JSONArray containing actors
	 * from the XTSP file, and creates a TeamOrganization based on the results.
     * 
	 * @param teamOrg a TeamOrganization to be modified. This will be cleared and
	 *                replaced with the XTSP team organization data.
     * @return The generated TeamOrganization with data from the xTSP file.
     * @throws IllegalArgumentException if teamOrg or xtspJSON are null.
     */
    private TeamOrganization generateTeamOrgFromXTSP(generated.dkf.TeamOrganization teamOrg) {
        
        if (teamOrg == null) {
            throw new IllegalArgumentException("The parameter 'teamOrg' cannot be null.");
        }
        
        Map<String, Integer> teamOrgNameMap = new HashMap<String, Integer>();
        
        try {
            // Set the scenario's team organization to match the data from the XTSP file.
            generated.dkf.Team rootTeam = teamOrg.getTeam();
            
            List<Serializable> rootTeamList = rootTeam.getTeamOrTeamMember();
            
            // Clear the existing teams and members from the existing DKF
            rootTeamList.clear();
                        
            for (Object sideToRead : xtspForceSides) {
                if (sideToRead instanceof JSONObject) {
                    JSONObject sideToReadJSON = (JSONObject) sideToRead;
                    
                    String sideName = readJSONAsString(sideToReadJSON.get(SIDE_NAME));
                    String sideAffiliation = readJSONAsString(sideToReadJSON.get(AFFILIATION));
					String sideTeamName = getNameAfterCheckingForDuplicates(sideName + " (" + sideAffiliation + ")",
							teamOrgNameMap);
                    
                    Team newSideTeam = new Team();
                    newSideTeam.setName(sideTeamName);
                    newSideTeam.setEchelon(null);
                    
                    rootTeamList.add(newSideTeam);
                    
                    JSONArray sideOrganizations = (JSONArray) sideToReadJSON.get(ORGANIZATIONS);
                    if (sideOrganizations != null) {
	                    for (Object organizationToRead : sideOrganizations) {
	                    	JSONObject organizationToReadJSON = (JSONObject) organizationToRead;
	                    	
	                    	String orgName = readJSONAsString(organizationToReadJSON.get(ORG_NAME));
	                    	
	                    	Team newOrgTeam = new Team();
	                    	newOrgTeam.setName(getNameAfterCheckingForDuplicates(orgName, teamOrgNameMap));
	                    	newOrgTeam.setEchelon(null);
	                    	
	                    	newSideTeam.getTeamOrTeamMember().add(newOrgTeam);
	                    	
		                    JSONArray orgSubTeams = (JSONArray) organizationToReadJSON.get(TEAMS);
		                    
		                    for (Object teamRef : orgSubTeams) {
		                        if (teamRef instanceof JSONObject) {
		                            JSONObject teamRefJSON = (JSONObject) teamRef;
		                            
		                            String teamId = readJSONAsString(teamRefJSON.get(TEAM_ID_JSON));
		                            
		                            JSONObject teamToReadJSON = xtspTeamIdMap.get(teamId);
		                            
		                            Team subTeam = new Team();
		                            
		                            newOrgTeam.getTeamOrTeamMember().add(subTeam);
		                            
									// Recursively add the teams and team members under this side to the team
									// organization.
		                            addTeamElements(teamToReadJSON, subTeam, teamOrgNameMap);
		                        }
		                    }
		                }
                    }
                    JSONArray sideTeams = (JSONArray) sideToReadJSON.get(TEAMS);
                    if (sideTeams != null) {
                    	for (Object teamRef : sideTeams) {
	                        if (teamRef instanceof JSONObject) {
	                            JSONObject teamRefJSON = (JSONObject) teamRef;
	                            
	                            String teamId = readJSONAsString(teamRefJSON.get(TEAM_ID_JSON));
	                            
	                            JSONObject teamToReadJSON = xtspTeamIdMap.get(teamId);
	                            
	                            Team subTeam = new Team();
	                            
	                            newSideTeam.getTeamOrTeamMember().add(subTeam);
	                            
								// Recursively add the teams and team members under this side to the team
								// organization.
	                            addTeamElements(teamToReadJSON, subTeam, teamOrgNameMap);
	                        }
	                    }
                    }
                }
            }
        } catch (Exception e) {
			String details = "Failed to read Team Organization data from the imported xTSP file. Attempting to skip the Team Organization and continue the import process.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
        }
        
        return teamOrg;
        
    }
    
    /**
	 * A recursive method which adds all team members and sub-teams to a given team
	 * read from the XTSP file, and then is called again on every sub-team to add
	 * members and Team data from those as well.
     * 
	 * @param teamToReadJSON The JSONObject that contains the current team's data
	 * @param teamToModify   The generated.dkf.Team object to be modified
	 * @param teamOrgNameMap The map of names and number of times they've been used
	 *                       in the TeamOrganization. Used to prevent duplicate
	 *                       names.
     */
    private void addTeamElements(JSONObject teamToReadJSON, Team teamToModify, Map<String, Integer> teamOrgNameMap) {
        
    	if (teamToReadJSON == null) {
            throw new IllegalArgumentException("The parameter 'teamToReadJSON' cannot be null.");
        } else if (teamToModify == null) {
        	throw new IllegalArgumentException("The parameter 'teamToModify' cannot be null.");
        } else if (teamOrgNameMap == null) {
        	throw new IllegalArgumentException("The parameter 'teamOrgNameMap' cannot be null.");
        }
    	
        try {
            teamJSONMap.put(teamToReadJSON, teamToModify);
            
            // Set the team's name and echelon.
            String teamName = readJSONAsString(teamToReadJSON.get(TEAM_DESIGNATION));
            String teamEchelon = readJSONAsString(teamToReadJSON.get(TEAM_ECHELON));
            
            teamToModify.setName(getNameAfterCheckingForDuplicates(teamName, teamOrgNameMap));
            teamToModify.setEchelon(processTeamEchelon(teamEchelon));
            
            if (StringUtils.isBlank(teamName)) {
                // Team name cannot be empty or null
				String details = "A team from an xTSP file did not have a valid name, and so cannot be read into the team organization. Team name cannot be empty or null.";
				errorLogList.add(new DetailedException("Invalid Team Name", details, null));
				logger.warn(details);
                return;
            } else if (teamEchelon == null) {
                // Team echelon cannot be null
				String details = "The team called " + teamName
						+ " from xTSP file did not have a valid echelon value, and so cannot be read into the team organization. Team echelon cannot be null.";
				errorLogList.add(new DetailedException("Invalid Echelon Value", details, null));
				logger.warn(details);
                return;
            }
            
            // Add members to the team
            List<Serializable> teamToModifyContents = teamToModify.getTeamOrTeamMember();
            
            JSONArray teamRoles = (JSONArray) teamToReadJSON.get(TEAM_ROLES);
            
			// It is valid for a team to not have any teamRoles, so check before trying to
			// iterate through them.
            if (teamRoles != null) {
	            for (Object roleRef : teamRoles) {
	                if (roleRef instanceof JSONObject) {
	                    JSONObject roleRefJSON = (JSONObject) roleRef;
	                    String roleId = readJSONAsString(roleRefJSON.get(ROLE_ID_JSON));
	                    
	                    JSONObject roleToAddJSON = xtspRoleIdMap.get(roleId);
	                    
	                    if (roleToAddJSON == null) {
							String details = "The team named " + teamName + "refers to a role ID " + roleId
									+ " that does not have an associated role in the xTSP file. As a result, this role cannot be added to the team organization. Attempting to continue the import process.";
							errorLogList.add(new DetailedException("Unfound Role ID", details, null));
							logger.warn(details);
	                    	continue;
	                    }
	                    
	                    String roleName = readJSONAsString((roleToAddJSON.get(ROLE_NAME)));
	                    roleName = getNameAfterCheckingForDuplicates(roleName, teamOrgNameMap);
	
	                    JSONObject roleActorRef = (JSONObject) roleToAddJSON.get(ROLE_ACTOR);
	                    String roleActorId = readJSONAsString(roleActorRef.get(ACTOR_ID_JSON));
	                    
	                    if (roleActorId == null) {
	                        // Learner ID cannot be empty or null
							String details = "An actor's 'actorId' value from an xTSP file is null, and so cannot be read into the team organization. actorId cannot be empty or null. The affected actor's role name is "
											+ roleName + ".";
							errorLogList.add(new DetailedException("Null Actor Id Value", details, null));
							logger.warn(details);
	                        continue;
	                    }
	                    
	                    JSONObject roleActor = xtspActorIdMap.get(roleActorId);
	                    
	                    if (roleActor == null) {
							String details = "The role with ID " + roleId + "refers to an actor ID " + roleActorId
									+ " that does not have an associated actor in the xTSP file. As a result, this actor cannot be added to the team organization. Attempting to continue the import process.";
							errorLogList.add(new DetailedException("Actor ID Not Found", details, null));
							logger.warn(details);
	                    	continue;
	                    }
	                                        
	                    Boolean isPlayableValue = (Boolean) (roleActor.get(REAL_PLAYER));
	                    
	                    if (StringUtils.isBlank(roleName)) {
	                        // Role name cannot be empty or null
							String details = 
									"A role from an xTSP file did not have a valid name, and so cannot be read into the team organization. Role name cannot be empty or null.";
							errorLogList.add(new DetailedException("Invalid Role Name", details, null));
							logger.warn(details);
	                        continue;
	                    } else if (isPlayableValue == null) {
	                        // Is Playable cannot be empty or null
							String details = 
									"An actor's 'realPlayer' value from an xTSP file is null, and so will default to false. The affected actor's role name is "
											+ roleName + ".";
							errorLogList.add(new DetailedException("Null RealPlayer Value", details, null));
							logger.warn(details);
	                        isPlayableValue = false;
	                    } 

	                    if (!teamMemberJSONMap.containsKey(roleToAddJSON)) {
	                    TeamMember newMember = new TeamMember();
	                    newMember.setName(roleName);
	                    newMember.setPlayable(isPlayableValue);
	                    generated.dkf.LearnerId newLearnerId = new LearnerId();
	                    newLearnerId.setType(roleActorId.toString());
	                    newMember.setLearnerId(newLearnerId);
	                    
	                    teamToModifyContents.add(newMember);
	                    teamMemberJSONMap.put(roleToAddJSON, newMember);
	                    } else {
							String details = "The xTSP file attempted to add the TeamMember " + roleName
									+ " multiple times. There may be duplicate references to this team member or their team in the xTSP's force structure."
									+ "Attempting to skip this team member and continue the import process.";
							errorLogList.add(new DetailedException("Couldn't Add Role Name", details, null));
							logger.warn(details);
	                }
	            }
            }
            }
            
            // Add any sub-teams and their contents
            JSONArray subTeams = (JSONArray) teamToReadJSON.get(SUB_TEAMS);
            
			// It is valid for a team to not have any sub-teams, so check before trying to
			// iterate through them.
            if (subTeams != null) {
                for (Object subTeamRef : subTeams) {
                    if (subTeamRef instanceof JSONObject) {
                        JSONObject subTeamRefJSON = (JSONObject) subTeamRef;
                        
                        String subTeamId = readJSONAsString(subTeamRefJSON.get(TEAM_ID_JSON));
                                                
                        JSONObject subTeamToAddJSON = xtspTeamIdMap.get(subTeamId);
                        Team subTeam = new Team();
                        
                        teamToModifyContents.add(subTeam);
                        
                        addTeamElements(subTeamToAddJSON, subTeam, teamOrgNameMap);
                    }
                }
            }
        } catch (Exception e) {
            String warnMessage = "Failed to import Team data from the xTSP JSON file to GIFT.";
            String teamName = teamToModify.getName();
            if (!StringUtils.isBlank(teamName)) {
                warnMessage = warnMessage + " The name of this Team is " + teamName + ".";
            }
            
			errorLogList.add(new DetailedException(warnMessage, e.toString(), e));
            logger.warn(warnMessage, e);
        }
    }
    
    /**
	 * Reads a string representing an echelon, and returns a string that will be
	 * properly parsed within a DKF. Corrects the fact that XTSP calls an echelon
	 * "Team" that GIFT calls "Fireteam", and changes invalid entries to an empty
	 * string (which will result in no echelon being specified, as opposed to an
	 * invalid value).
     * 
	 * @param echelonInput The string read from the XTSP file to represent an
	 *                     echelon
	 * @return A string that GIFT can use in DKF files to represent an echelon (or
	 *         null for no echelon). A value of null will result in a team with an
	 *         echelon of 'None'.
     */
    private String processTeamEchelon(String echelonInput) {
        
        String echelonInputToCheck = echelonInput;
        
        List<EchelonEnum> echelonsList = EchelonEnum.VALUES(EchelonEnum.class);
                
        if (echelonInputToCheck.equals(TEAM)) {
            echelonInputToCheck = FIRETEAM;
        }
        
        try {
            EchelonEnum echelonResult = EchelonEnum.valueOf(echelonInputToCheck, echelonsList);
            return echelonResult.getName();
        } catch (EnumerationNotFoundException e) {
			String details = "An EchelonEnum value was not found that matches " + echelonInputToCheck
					+ ". Defaulting to an echelon of 'None'.";
			errorLogList.add(new DetailedException(details, e.toString(), e));
			logger.warn(details, e);
            return null;
        }
    }
    
    /**
	 * Reads the specified object as a String. This is designed for parsing JSON
	 * from the xTSP file. The JSON Simple parser used by this class reads values in
	 * as Objects. This method is designed to allow the conversion to a string value
	 * to happen simply, without repeating code.
	 * 
     * @param jsonToRead
     * @return The JSON's value as a string.
     */
    private String readJSONAsString(Object jsonToRead) {
    	String resultString;
    	
    	if (jsonToRead == null) {
    		resultString = null;
    	} else if (jsonToRead instanceof String) {
    		resultString = (String) jsonToRead;
    	} else if (jsonToRead instanceof Long) {
    		resultString = String.valueOf(jsonToRead);
    	} else {
    		resultString = jsonToRead.toString();
    	}
    	
    	return resultString;
    }

    /**
	 * Returns a list of strings representing the names of course concepts from this
	 * DKF. Course concepts are assembled into a list during the import process, so
	 * this method can reference them.
     * 
	 * @return a list of strings representing the names of course concepts from this
	 *         DKF. If none exist, an empty list is returned.
     */
    public List<String> getCourseConceptNames() {
    	List<String> courseConceptNames = new LinkedList<String>();
    	
    	if (courseConceptList != null) {
	    	for (generated.dkf.Concept conceptToCopy : courseConceptList) {
				String conceptToAdd = conceptToCopy.getName();
				
				courseConceptNames.add(conceptToAdd);
			}
    	}
    	
    	return courseConceptNames;
    }
	
	/**
	 * Gets the list of errors and warnings that were collected during the importing
	 * or exporting process.
	 * 
	 * @return a list of errors and warnings. Will not be null, but can be empty.
	 */
	public List<DetailedException> getErrorLogList(){
		return errorLogList;
}
}


