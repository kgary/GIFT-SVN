package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import generated.dkf.EnvironmentAdaptation.CreateActors;
import generated.dkf.EnvironmentAdaptation.Fog.Color;
import generated.dkf.ActorTypeCategoryEnum;
import generated.dkf.Coordinate;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.StrategyStressCategory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StrategyUtil;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.LrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.TeamHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EchelonActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EnvironmentAdaptationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.TeamRoleActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ContextExtensionConcept;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomActivityTypeConcepts;
import mil.arl.gift.net.util.Util;

/**
 * Context Extensions defined within the ITS xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ItsContextExtensionConcepts extends ContextExtensionConcept {
    
    protected ItsContextExtensionConcepts(String id) throws LmsXapiProfileException {
        super(id, giftStatementExtensionSparqlQuery(id, true));
    }
    // Enum for Extension Keys
    public enum extensionObjectKeys implements LrsEnum {
        SOURCE("source"),
        MET("met"),
        TASK("task"),
        TASKS("tasks"),
        SITUATION("situation"),
        GOALS("goals"),
        CONDITION("condition"),
        CONDITIONS("conditions"),
        ROE("roe"),
        THREAT_WARNING("threatWarning"),
        WEAPON_STATUS("weaponStatus"),
        WEAPON_POSTURE("weaponPosture"),
        PLAYABLE("playable"),
        IDENTIFIER("identifier"),
        ASSIGNED("assigned"),
        NAME("name"),
        ECHELON("echelon"),
        DEPTH("depth"),
        PARENT("parent"),
        MEMBERS("members"),
        DURATION("duration"),
        VALUE("value"),
        COLOR("color"),
        RED("red"),
        GREEN("green"),
        BLUE("blue"),
        DENSITY("density"),
        TYPE("type"),
        SIDE("side"),
        CIVILIAN("civilian"),
        BLUFOR("blufor"),
        OPFOR("opfor"),
        OFFSET("offset"),
        RIGHT("right"),
        FRONT("front"),
        UP("up"),
        RELEASE_DATE("releaseDate"),
        VERSION_NAME("versionName"),
        BUILD_DATE("buildDate"),
        IP("ip"),
        LOG_FILE_PATH("logFilePath"),
        DKF_FILE_NAMES("dfkFileNames"),
        LOG_FILE_NAMES("logFileNames"),
        STRESS("stress"),
        STRESS_REASON("stressReason"),
        STRESS_CATEGORY("stressCategory"),
        DIFFICULTY("difficulty"),
        DIFFICULTY_REASON("difficultyReason"),
        COMMENT("comment"),
        MEDIA("media"),
        ACTOR_CATEGORY("actorTypeCategory"),
        HEADING("heading");
        private String value;
        extensionObjectKeys(String s) {
            this.value = s;
        }
        @Override
        public String getValue() {
            return value;
        }
    }
    
    public static class EvaluatorObservation extends ItsContextExtensionConcepts {
        // Singleton
        private static EvaluatorObservation instance = null;
        // Constructor
        private EvaluatorObservation() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#evaluator.observation");
        }
        // Access
        public static EvaluatorObservation getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new EvaluatorObservation();
            }
            return instance;
        }
        
        /**
         * Parse Observer Comment and Media from scoreNode and set as fields within
         * ObjectNode when not blank.
         * 
         * @param scoreNode - AbstractScoreNode to parse observations from
         * 
         * @return ObjectNode with possibly COMMENT and MEDIA fields
         */
        private static ObjectNode createExtensionItem(AbstractScoreNode scoreNode) {
            if(scoreNode == null) {
                throw new IllegalArgumentException("AbstractScoreNode must be non-null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(StringUtils.isNotBlank(scoreNode.getObserverComment())) {
                item.put(extensionObjectKeys.COMMENT.getValue(), scoreNode.getObserverComment());
            }
            if(StringUtils.isNotBlank(scoreNode.getObserverMedia())) {
                item.put(extensionObjectKeys.MEDIA.getValue(), scoreNode.getObserverMedia());
            }
            return item;
        }
        
        /**
         * Parse Observer Comment and Media from performance and set as fields within
         * ObjectNode when not blank.
         * 
         * @param performance - PerformanceState to parse observations from
         * 
         * @return ObjectNode with possibly COMMENT and MEDIA fields
         */
        private static ObjectNode createExtensionItem(PerformanceState performance) {
            if(performance == null) {
                throw new IllegalArgumentException("PerformanceState must be non-null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(StringUtils.isNotBlank(performance.getObserverComment())) {
                item.put(extensionObjectKeys.COMMENT.getValue(), performance.getObserverComment());
            }
            if(StringUtils.isNotBlank(performance.getObserverMedia())) {
                item.put(extensionObjectKeys.MEDIA.getValue(), performance.getObserverMedia());
            }
            return item;
        }
        
        /**
         * Parse Observer Comment and Media from Abstract Performance State and set as fields within
         * ObjectNode when not blank.
         * 
         * @param psa - PerformanceState with PerformanceStateAttribute to parse observations from
         * 
         * @return ObjectNode with possibly COMMENT and MEDIA fields
         */
        private static ObjectNode createExtensionItem(AbstractPerformanceState psa) {
            if(psa == null) {
                throw new IllegalArgumentException("PerformanceStateAttribute must be non-null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(StringUtils.isNotBlank(psa.getState().getObserverComment())) {
                item.put(extensionObjectKeys.COMMENT.getValue(), psa.getState().getObserverComment());
            }
            if(StringUtils.isNotBlank(psa.getState().getObserverMedia())) {
                item.put(extensionObjectKeys.MEDIA.getValue(), psa.getState().getObserverMedia());
            }
            return item;
        }
        
        /**
         * Create Extension from scoreNode and add to Context when the resulting ObjectNode
         * is not empty.
         * 
         * @param context - Context to add Extension to, can't be null
         * @param scoreNode - AbstractScoreNode used to create ObjectNode, can't be null
         * 
         * @throws LmsXapiExtensionException when unable to parse Context Extension from xAPI Profile
         */
        public void addToContext(Context context, AbstractScoreNode scoreNode) throws LmsXapiExtensionException {
            addToContext(context, scoreNode, false);
        }
        
        /**
         * Create Extension from scoreNode and add to Context when the resulting ObjectNode
         * is not empty.
         * 
         * @param context - Context to add Extension to, can't be null
         * @param scoreNode - AbstractScoreNode used to create ObjectNode, can't be null
         * @param forceOverwrite - if true, overwrite any existing values with extension created from arguments
         * 
         * @throws LmsXapiExtensionException when unable to parse Context Extension from xAPI Profile
         */
        public void addToContext(Context context, AbstractScoreNode scoreNode, boolean forceOverwrite) throws LmsXapiExtensionException {
            ObjectNode ext = createExtensionItem(scoreNode);
            if(ext.size() != 0) {
                addToContext(context, ext, forceOverwrite);
            }
        }
        
        /**
         * Create Extension from performance and add to Context when the resulting ObjectNode
         * is not empty.
         * 
         * @param context - Context to add Extension to, can't be null
         * @param performance - PerformanceState used to create ObjectNode, can't be null
         * 
         * @throws LmsXapiExtensionException when unable to parse Context Extension from xAPI Profile
         */
        public void addToContext(Context context, PerformanceState performance) throws LmsXapiExtensionException {
            addToContext(context, performance, false);
        }
        
        /**
         * Create Extension from performance and add to Context when the resulting ObjectNode
         * is not empty.
         * 
         * @param context - Context to add Extension to, can't be null
         * @param performance - PerformanceState used to create ObjectNode, can't be null
         * @param forceOverwrite - if true, overwrite any existing values with extension created from arguments
         * 
         * @throws LmsXapiExtensionException when unable to parse Context Extension from xAPI Profile
         */
        public void addToContext(Context context, PerformanceState performance, boolean forceOverwrite) throws LmsXapiExtensionException {
            ObjectNode ext = createExtensionItem(performance);
            if(ext.size() != 0) {
                addToContext(context, ext, forceOverwrite);
            }
        }
        
        /**
         * Create Extension from Performance State Attribute and add to Context when the resulting ObjectNode
         * is not empty.
         * 
         * @param context - Context to add Extension to, can't be null
         * @param psa - AbstractPerformanceState with PerformanceStateAttribute used to create ObjectNode, can't be null
         * 
         * @throws LmsXapiExtensionException when unable to parse Context Extension from xAPI Profile
         */
        public void addToContext(Context context, AbstractPerformanceState psa) throws LmsXapiExtensionException {
            addToContext(context, psa, false);
        }
        
        /**
         * Create Extension from Performance State Attribute and add to Context when the resulting ObjectNode
         * is not empty.
         * 
         * @param context - Context to add Extension to, can't be null
         * @param psa - AbstractPerformanceState with PerformanceStateAttribute used to create ObjectNode, can't be null
         * @param forceOverwrite - if true, overwrite any existing values with extension created from arguments
         * 
         * @throws LmsXapiExtensionException when unable to parse Context Extension from xAPI Profile
         */
        public void addToContext(Context context, AbstractPerformanceState psa, boolean forceOverwrite) throws LmsXapiExtensionException {
            ObjectNode ext = createExtensionItem(psa);
            if(ext.size() != 0) {
                addToContext(context, ext, forceOverwrite);
            }
        }
    }
    
    public static class PerformanceCharacteristics extends ItsContextExtensionConcepts {
        // Singleton
        private static PerformanceCharacteristics instance = null;
        // Constructor
        private PerformanceCharacteristics() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#performance.characteristics");
        }
        // Access
        public static PerformanceCharacteristics getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new PerformanceCharacteristics();
            }
            return instance;
        }
        
        /**
         * Create JSON Object with at least the task field. Stress, stressReason, Difficulty, difficultyReason
         * are added to the JSON Object when set within the TaskScoreNode.
         * 
         * @param node - TaskScoreNode used to create JSON Object
         * 
         * @return JSON Object
         * 
         * @throws LmsXapiExtensionException when unable to create activity id from TaskScoreNode name
         */
        private static ObjectNode createExtensionItem(TaskScoreNode node) throws LmsXapiExtensionException {
            if(node == null) {
                throw new IllegalArgumentException("TaskScoreNode must be non-null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(node.getStress() != null) {
                item.put(extensionObjectKeys.STRESS.getValue(), node.getStress());
            }
            if(StringUtils.isNotBlank(node.getStressReason())) {
                item.put(extensionObjectKeys.STRESS_REASON.getValue(), node.getStressReason());
            }
            if(node.getDifficulty() != null) {
                item.put(extensionObjectKeys.DIFFICULTY.getValue(), node.getDifficulty());
            }
            if(StringUtils.isNotBlank(node.getDifficultyReason())) {
                item.put(extensionObjectKeys.DIFFICULTY_REASON.getValue(), node.getDifficultyReason());
            }
            String activityId;
            try {
                activityId = AssessmentActivity.createAssessmentId(node.getName());
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiExtensionException("Unable to create assessment activity id!", e);
            }
            item.put(extensionObjectKeys.TASK.getValue(), activityId);
            return item;
        }
        
        /**
         * Create JSON Object with at least the task field. Stress, stressReason, Difficulty, difficultyReason
         * are added to the JSON Object when set within the TaskPerformanceState
         * 
         * @param state - TaskPerformanceState used to create JSON Object
         * 
         * @return JSON object
         * 
         * @throws LmsXapiExtensionException when unable to create activity id from TaskPerformanceState name
         */
        private static ObjectNode createExtensionItem(TaskPerformanceState state) throws LmsXapiExtensionException {
            if(state == null) {
                throw new IllegalArgumentException("TaskPerformanceState must be non-null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(state.getStress() != null) {
                item.put(extensionObjectKeys.STRESS.getValue(), state.getStress());
            }
            if(StringUtils.isNotBlank(state.getStressReason())) {
                item.put(extensionObjectKeys.STRESS_REASON.getValue(), state.getStressReason());
            }
            if(state.getDifficulty() != null) {
                item.put(extensionObjectKeys.DIFFICULTY.getValue(), state.getDifficulty());
            }
            if(StringUtils.isNotBlank(state.getDifficultyReason())) {
                item.put(extensionObjectKeys.DIFFICULTY_REASON.getValue(), state.getDifficultyReason());
            }
            String activityId;
            try {
                activityId = AssessmentActivity.createAssessmentId(state.getState().getName());
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiExtensionException("Unable to create assessment activity id!", e);
            }
            item.put(extensionObjectKeys.TASK.getValue(), activityId);
            return item;
        }
        
        /**
         * Create JSON Object with at least the condition field. Stress is added to the JSON Object when
         * set within the EnvironmentControl.
         * 
         * @param envControl - EnvironmentControl used to create JSON Object
         * 
         * @return JSON Object
         * 
         * @throws LmsXapiExtensionException when unable to initialize Environment Adaptation Activity or
         * when the Environment Adaptation type is not supported.
         */
        private static ObjectNode createExtensionItem(EnvironmentControl envControl) throws LmsXapiExtensionException {
            if(envControl == null) {
                throw new IllegalArgumentException("EnvironmentControl must be non-null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(envControl.getStress() != null) {
                item.put(extensionObjectKeys.STRESS.getValue(), envControl.getStress());
            }
            if(envControl.getEnvironmentStatusType() != null) {
                generated.dkf.EnvironmentAdaptation envAdapt = envControl.getEnvironmentStatusType();
                EnvironmentAdaptationActivity envAdaptActivity;
                try {
                    envAdaptActivity = EnvironmentAdaptationActivity.dispatch(envAdapt);
                } catch (LmsXapiActivityException e) {
                    throw new LmsXapiExtensionException("Unable to init Environment Adaptation Activity for: "+envAdapt.getType(), e);
                }
                String activityId = envAdaptActivity.getId().toString();
                item.put(extensionObjectKeys.CONDITION.getValue(), activityId);
                // Stress Category
                StrategyStressCategory stressCategory = StrategyUtil.getStrategyStressCategory(envAdapt);
                if(stressCategory != null) {
                    item.put(extensionObjectKeys.STRESS_CATEGORY.getValue(), stressCategory.value());
                }
            }
            return item;
        }
        
        /**
         * Given a collection of Objects (expected to be either TaskScoreNode, EnvironmentControl or TaskPerformanceState)
         * create the corresponding extension item and add to corresponding collection of items.
         * 
         * @param items - collection of TaskScoreNode(s) and/or EnvironmentControl(s)
         * 
         * @return Extension JSON Object
         * 
         * @throws LmsXapiExtensionException when unable to create an item or items contains unexpected type
         */
        private static ObjectNode createExtensionColl(List<Object> items) throws LmsXapiExtensionException {
            if(items == null) {
                throw new IllegalArgumentException("items must be non-null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            ArrayNode tasks = new JsonNodeFactory(true).arrayNode();
            ArrayNode conditions = new JsonNodeFactory(true).arrayNode();
            for(Object item : items) {
                if(item instanceof TaskScoreNode) {
                    ObjectNode task = createExtensionItem((TaskScoreNode) item);
                    tasks.add(task);
                } else if(item instanceof EnvironmentControl) {
                    ObjectNode condition = createExtensionItem((EnvironmentControl) item);
                    conditions.add(condition);
                } else if(item instanceof TaskPerformanceState) {
                    ObjectNode task = createExtensionItem((TaskPerformanceState) item);
                    tasks.add(task);
                } else {
                    throw new LmsXapiExtensionException("Items contained unsupported object! "+item);
                }
            }
            if(tasks.size() != 0) {
                ext.set(extensionObjectKeys.TASKS.getValue(), tasks);
            }
            if(conditions.size() != 0) {
                ext.set(extensionObjectKeys.CONDITIONS.getValue(), conditions);
            }
            return ext;
        }
        
        /**
         * Calls createExtensionColl to create Extension JSON object containing a task JSON object per TaskScoreNode || TaskPerformanceState
         * and a condition JSON object per EnvironmentControl within items. The created extension is added to context when non-empty
         * 
         * @param context - Context to add Extension to, can't be null
         * @param items - Collection of TaskScoreNode and/or EnvironmentControl and/or TaskPerformanceState
         * 
         * @throws LmsXapiExtensionException when unable to create activity id,
         * when unable to initialize an Environment Adaptation Activity, when an Environment Adaptation type is not supported,
         * when items contains a Object that's not a TaskScoreNode, TaskPerformanceState or EnvironmentControl.
         */
        public void addToContext(Context context, List<Object> items) throws LmsXapiExtensionException {
            addToContext(context, items, false);
        }
        
        /**
         * Calls createExtensionColl to create Extension JSON object containing a task JSON object per TaskScoreNode || TaskPerformanceState
         * and a condition JSON object per EnvironmentControl within items. The created extension is added to context when non-empty
         * 
         * @param context - Context to add Extension to, can't be null
         * @param items - Collection of TaskScoreNode and/or EnvironmentControl
         * @param forceOverwrite - if true, overwrite any existing values with extension created from arguments
         * 
         * @throws LmsXapiExtensionException when unable to create activity id,
         * when unable to initialize an Environment Adaptation Activity, when an Environment Adaptation type is not supported,
         * when items contains a Object that's not a TaskScoreNode, TaskPerformanceState or EnvironmentControl.
         */
        public void addToContext(Context context, List<Object> items, boolean forceOverwrite) throws LmsXapiExtensionException {
            ObjectNode ext = createExtensionColl(items);
            if(ext.size() != 0) {
                addToContext(context, ext, forceOverwrite);
            }
        }
    }
    
    // Subclass for node activity id to node id
    public static class NodeIdMapping extends ItsContextExtensionConcepts {
        private MomActivityTypeConcepts.Assessment assessmentATC;
        // Singleton
        private static NodeIdMapping instance = null;
        // Constructor
        private NodeIdMapping() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#assessment.node.to.id");
            this.assessmentATC = MomActivityTypeConcepts.Assessment.getInstance();
        }
        // Access
        public static NodeIdMapping getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new NodeIdMapping();
            }
            return instance;
        }
        // Methods
        public void createExtensionItem(ObjectNode ext, GradedScoreNode node) throws LmsXapiExtensionException {
            if(ext == null || node == null) {
                throw new IllegalArgumentException("ext and node can not be null!");
            }
            String activityId;
            try {
                activityId = assessmentATC.asActivity(node).getId().toString();
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiExtensionException("Unable to create Activity id from GradedScoreNode!", e);
            }
            Integer nodeId = node.getPerformanceNodeId();
            if(nodeId != null) {
                ext.put(activityId, nodeId);
            }   
        }
        public ObjectNode createExtensionColl(List<GradedScoreNode> nodes) throws LmsXapiExtensionException {
            if(nodes == null) {
                throw new IllegalArgumentException("nodes can not be null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            for(GradedScoreNode node : nodes) {
                createExtensionItem(ext, node);
            }
            return ext;
        }
        public void addToContext(Context context, List<GradedScoreNode> nodes, boolean forceOverwrite) throws LmsXapiExtensionException {
            if(context == null || nodes == null) {
                throw new IllegalArgumentException("context can not be null and nodes can not be null!");
            }
            ObjectNode ext = createExtensionColl(nodes);
            if(ext.size() != 0) {
                addToContext(context, ext, forceOverwrite);
            }
        }
        public void addToContext(Context context, List<GradedScoreNode> nodes) throws LmsXapiExtensionException {
            addToContext(context, nodes, false);
        }
    }
    // Subclass for node hierarchy
    public static class NodeHierarchy extends ItsContextExtensionConcepts implements NodeHierarchyExtension {
        private MomActivityTypeConcepts.Assessment assessmentATC;
        // Singleton
        private static NodeHierarchy instance = null;
        // Constructor
        private NodeHierarchy() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#assessment.node.hierarchy");
            this.assessmentATC = MomActivityTypeConcepts.Assessment.getInstance();
        }
        // Access
        public static NodeHierarchy getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new NodeHierarchy();
            }
            return instance;
        }
        // methods
        /**
         * Create Assessment Activity from GradedScoreNode and return JSON representation
         * 
         * @param node - GradedScoreNode to create Activity from
         * 
         * @return JSON representation of Assessment Activity
         * 
         * @throws LmsXapiActivityException when unable to create Assessment Activity from GradedScoreNode
         */
        public ObjectNode createExtensionItem(GradedScoreNode node) throws LmsXapiActivityException {
            return assessmentATC.asActivity(node).toJSONNode();
        }
        /**
         * Create Extension JSON from ordered collection of GradedScoreNodes
         * 
         * @param history - ordered collection of GradedScoreNodes
         * 
         * @return Extension JSON
         * 
         * @throws LmsXapiExtensionException when unable to create Assessment Activity from GradedScoreNode
         */
        public ObjectNode createExtensionColl(List<GradedScoreNode> history) throws LmsXapiExtensionException {
            if(history == null) {
                throw new IllegalArgumentException("history can not be null!");
            }
            Integer counter = 0;
            ObjectNode hierarchy = new JsonNodeFactory(true).objectNode();
            for(GradedScoreNode ancestor : history) {
                ObjectNode node;
                try {
                    node = createExtensionItem(ancestor);
                } catch(LmsXapiActivityException e) {
                    throw new LmsXapiExtensionException("Unable to create Extension item from GradedScoreNode within history!", e);
                }
                hierarchy.set(counter.toString(), node);
                ++counter;
            }
            return hierarchy;
        }
        @Override
        public Extensions asExtension(List<GradedScoreNode> history, Extensions ext) throws LmsXapiExtensionException {
            return asExtension(history, ext, false);
        }
        @Override
        public Extensions asExtension(List<GradedScoreNode> history, Extensions ext, boolean forceOverwrite) throws LmsXapiExtensionException {
            if(ext == null || history == null) {
                throw new IllegalArgumentException("history and ext can not be null!");
            }
            ObjectNode extColl = createExtensionColl(history);
            if(extColl.size() != 0) {
                return asExtension(extColl, ext, forceOverwrite);
            } else {
                return ext;
            }
        }
        @Override
        public void addToContext(Context context, List<GradedScoreNode> history) throws LmsXapiExtensionException {
            addToContext(context, history, false);
        }
        @Override
        public void addToContext(Context context, List<GradedScoreNode> history, boolean forceOverwrite) throws LmsXapiExtensionException {
            if(context == null || history == null) {
                throw new IllegalArgumentException("context and history can not be null!");
            }
            ObjectNode extColl = createExtensionColl(history);
            if(extColl.size() != 0) {
                addToContext(context, extColl, forceOverwrite);
            }
        }
        @Override
        public void addToStatement(Statement statement, List<GradedScoreNode> history) throws LmsXapiExtensionException {
            addToStatement(statement, history, false);
        }
        @Override
        public void addToStatement(Statement statement, List<GradedScoreNode> history, boolean forceOverwrite) throws LmsXapiExtensionException {
            if(statement == null || history == null) {
                throw new IllegalArgumentException("statement and history can not be null!");
            }
            ObjectNode extColl = createExtensionColl(history);
            if(extColl.size() != 0) {
                addToStatement(statement, extColl, forceOverwrite);
            }
        }
        /**
         * 
         * @param statement
         * @return
         * @throws LmsXapiExtensionException
         */
        public Map<Integer, Activity> parseToMap(Statement statement) throws LmsXapiExtensionException {
            if(statement.getContext() == null || statement.getContext().getExtensions() == null) {
                throw new IllegalArgumentException("Unable to parse Context Extensions from Statement!");
            }
            JsonNode hierarchy = parseFromExtensions(statement.getContext().getExtensions());
            Map<Integer, Activity> mapping = new HashMap<Integer, Activity>();
            if(hierarchy == null) {
                return mapping;
            }
            Iterator<String> idxs = hierarchy.fieldNames();
            while(idxs.hasNext()) {
                String sIdx = idxs.next();
                JsonNode node = hierarchy.get(sIdx);
                if(node == null) {
                    throw new LmsXapiExtensionException("Unable to find node for index within Node Hierarchy Context Extension!");
                }
                Activity a;
                try {
                    a = new Activity(node);
                } catch (URISyntaxException e) {
                    throw new LmsXapiExtensionException("Unable to create Activity from JsonNode found within Node Hierarchy Context Extension!", e);
                }
                mapping.put(Integer.parseInt(sIdx), a);
            }
            return mapping;
        }
    }
    // Subclass for Team Structure
    public static class TeamStructure extends ItsContextExtensionConcepts {
        // Singleton
        private static TeamStructure instance = null;
        // Constructor
        private TeamStructure() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#team.structure");
        }
        // Access
        public static TeamStructure getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new TeamStructure();
            }
            return instance;
        }
        // Methods
        /**
         * Creates team member extension item
         * 
         * @param playable - is the team member playable?
         * @param identifier - team role activity id created from TeamMember
         * @param assigned - agent IFI of user who is assigned to the team role, can be null
         * 
         * @return ObjectNode with playable, identifier and optionally assigned keys
         */
        private static ObjectNode createTeamMember(boolean playable, String identifier, String assigned) {
            if(identifier == null) {
                throw new IllegalArgumentException("identifier can not be null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            item.put(extensionObjectKeys.PLAYABLE.getValue(), playable);
            item.put(extensionObjectKeys.IDENTIFIER.getValue(), identifier);
            if(assigned != null) {
                item.put(extensionObjectKeys.ASSIGNED.getValue(), assigned);
            }
            return item;
        }
        /**
         * Creates team member extension item from SessionMembership
         * 
         * @param member - a SessionMembership for a user who joined a KnowledgeSession
         * 
         * @return ObjectNode with playable, identifier and assigned keys
         * 
         * @throws LmsXapiExtensionException when unable to create team role identifier or assigned identifier
         */
        private static ObjectNode createTeamMember(SessionMembership member) throws LmsXapiExtensionException {
            if(member == null) {
                throw new IllegalArgumentException("member can not be null!");
            }
            TeamMember<?> teamRole = member.getTeamMember();
            String username = member.getUsername();
            String teamRoleActivityId, agentIfi;
            if(teamRole == null) {
                throw new IllegalArgumentException("Unable to create team member extension item from null TeamMember!");
            }
            try {
                teamRoleActivityId = (new TeamRoleActivity(teamRole)).getId().toString();
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiExtensionException("Unable to create Team Role Activity id!", e);
            }
            if(username != null) {
                try {
                    agentIfi = PersonaHelper.createMboxIFI(member.getUsername());
                } catch (LmsXapiAgentException e) {
                    throw new LmsXapiExtensionException("Unable to create mbox ifi from session member username!", e);
                }
            } else {
                agentIfi = username;
            }
            return createTeamMember(teamRole.isPlayable(), teamRoleActivityId, agentIfi);
        }
        /**
         * Creates team member extension item from TeamMember assumed to be unassigned
         * 
         * @param teamRole - A TeamMember within a team structure that's not assigned to a user
         * 
         * @return ObjectNode with playable and identifier
         * 
         * @throws LmsXapiExtensionException when unable to create identifier
         */
        private static ObjectNode createTeamMember(TeamMember<?> teamRole) throws LmsXapiExtensionException {
            if(teamRole == null) {
                throw new IllegalArgumentException("Unable to create team member extension item from null TeamMember!");
            }
            String teamRoleActivityId;
            try {
                teamRoleActivityId = (new TeamRoleActivity(teamRole)).getId().toString();
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiExtensionException("Unable to create Team Role Activity id!", e);
            }
            return createTeamMember(teamRole.isPlayable(), teamRoleActivityId, null);
        }
        /**
         * Creates team unit extension item from team info + associated session members and unassigned roles
         * 
         * @param teamName - name of the team, can't be null
         * @param teamEchelonActivityId - activity id created from team's echelon, can be null
         * @param teamDepth - level of nesting of the team unit within the root team, can't be null
         * @param teamParent - name of the parent team unit, can be null
         * @param sessionMembers - container for relevant session members, can be empty but not null
         * @param unassignedUnits - container for unassigned team roles, can be empty but not null
         * 
         * @return team unit with at least name and depth
         * 
         * @throws LmsXapiExtensionException when any of teamName, sessionMembers, unassignedUnits are null or unable to create member item
         */
        private static ObjectNode createTeamUnit(String teamName, EchelonEnum teamEchelon, int teamDepth, String teamParent,
                List<SessionMembership> sessionMembers, List<TeamMember<?>> unassignedUnits) throws LmsXapiExtensionException {
            if(teamName == null || sessionMembers == null || unassignedUnits == null) {
                throw new IllegalArgumentException("Team Unit extension item creation requires non-null teamName, members and unassignedUnits!");
            }
            ObjectNode unit = new JsonNodeFactory(true).objectNode();
            unit.put(extensionObjectKeys.NAME.getValue(), teamName);
            if(teamEchelon != null) {
                String teamEchelonActivityId;
                try {
                    teamEchelonActivityId = (new EchelonActivity(teamEchelon)).getId().toString();
                } catch (LmsXapiActivityException e) {
                    throw new LmsXapiExtensionException("Unable to create Echelon Activity from teamEchelon!", e);
                }
                unit.put(extensionObjectKeys.ECHELON.getValue(), teamEchelonActivityId);
            }
            unit.put(extensionObjectKeys.DEPTH.getValue(), teamDepth);
            if(teamParent != null) {
                unit.put(extensionObjectKeys.PARENT.getValue(), teamParent);
            }
            ArrayNode members = new JsonNodeFactory(true).arrayNode();
            for(SessionMembership member : sessionMembers) {
                ObjectNode teamRole = createTeamMember(member);
                members.add(teamRole);
            }
            for(TeamMember<?> unassigned : unassignedUnits) {
                ObjectNode teamRole = createTeamMember(unassigned);
                members.add(teamRole);
            }
            if(members.size() != 0) {
                unit.set(extensionObjectKeys.MEMBERS.getValue(), members);
            }
            return unit;
        }
        /**
         * create first team unit within team structure extension
         * 
         * @param rootTeam - top level Team within a team structure
         * @param sessionMembers - all members of a KnowledgeSession
         * 
         * @return xAPI representation of the top level team within a team structure
         * 
         * @throws LmsXapiExtensionException when unable to create team unit item
         */
        private static ObjectNode createTeamUnit(Team rootTeam, List<SessionMembership> sessionMembers) throws LmsXapiExtensionException {
            if(rootTeam == null || sessionMembers == null) {
                throw new IllegalArgumentException("rootTeam and sessionMembers can not be null!");
            }
            return createTeamUnit(rootTeam.getName(), rootTeam.getEchelon(), rootTeam.getTeamDepth(), null,
                    TeamHelper.relevantSessionMembers(rootTeam, sessionMembers),
                    TeamHelper.deriveUnassignedRoles(rootTeam, sessionMembers));
        }
        /**
         * create team unit extension item at non-zero depth within team structure
         * 
         * @param team - Team Unit within arbitrary depth within team structure
         * @param sessionMembers - all members of a KnowledgeSession
         * @param path - ordered list of parent team names for the current team
         * 
         * @return xAPI representation of the non-top level team within a team structure
         * 
         * @throws LmsXapiExtensionException when unable to create team unit item
         */
        private static ObjectNode createTeamUnit(Team team, List<SessionMembership> sessionMembers, List<String> path) throws LmsXapiExtensionException {
            if(team == null || sessionMembers == null || path == null) {
                throw new IllegalArgumentException("team, sessionMembers and path can not be null!");
            }
            Team parent = team.getParentTeam();
            String parentName, teamParents;
            if(parent != null) {
                parentName = parent.getName();
                path.add(parentName);
            }
            teamParents = StringUtils.join(path, "|");
            return createTeamUnit(team.getName(), team.getEchelon(), team.getTeamDepth(), teamParents,
                    TeamHelper.relevantSessionMembers(team, sessionMembers),
                    TeamHelper.deriveUnassignedRoles(team, sessionMembers));
        }
        /**
         * Recursive navigation into team structure, creating team unit extension items per Team found within units
         * 
         * @param extVal - Collection of team units to update
         * @param units - Team units to search for teams within
         * @param sessionMembers - all Knowledge Session members
         * @param path - history of parent team names
         * 
         * @throws LmsXapiExtensionException when unable to create a team unit or team member item
         */
        private static void iterateOverTeams(ArrayNode extVal, List<AbstractTeamUnit> units, List<SessionMembership> sessionMembers, List<String> path) throws LmsXapiExtensionException {
            if(extVal == null || units == null || sessionMembers == null || path == null) {
                throw new IllegalArgumentException("extVal, units, sessionMembers and path can not be null!");
            }
            for(AbstractTeamUnit unit : units) {
                if(unit instanceof Team) {
                    List<String> unitPath = new ArrayList<String>(path);
                    Team team = (Team) unit;
                    ObjectNode teamUnit = createTeamUnit(team, sessionMembers, unitPath);
                    extVal.add(teamUnit);
                    iterateOverTeams(extVal, team.getUnits(), sessionMembers, unitPath);
                }
            }
        }
        /**
         * Create team structure extension array node from top level team and sessionMembers with a role in the team
         * 
         * @param rootTeam - Top level team which can contain sub teams
         * @param sessionMembers - session members from a knowledge session
         * 
         * @return collection of team units derived from the team and session members
         */
        public ArrayNode createExtensionColl(Team rootTeam, List<SessionMembership> sessionMembers) throws LmsXapiExtensionException {
            if(rootTeam == null || sessionMembers == null) {
                throw new IllegalArgumentException("rootTeam and sessionMembers can not be null!");
            }
            ArrayNode teamStructure = new JsonNodeFactory(true).arrayNode();
            List<String> parentNames = new ArrayList<String>();
            // Top level
            ObjectNode topLevelTeam = createTeamUnit(rootTeam, sessionMembers);
            teamStructure.add(topLevelTeam);
            iterateOverTeams(teamStructure, rootTeam.getUnits(), sessionMembers, parentNames);
            return teamStructure;
        }
        public void addToContext(Context context, Team rootTeam, List<SessionMembership> sessionMembers) throws LmsXapiExtensionException {
            addToContext(context, rootTeam, sessionMembers, false);
        }
        public void addToContext(Context context, Team rootTeam, List<SessionMembership> sessionMembers, boolean forceOverwrite) throws LmsXapiExtensionException {
            if(context == null || rootTeam == null || sessionMembers == null) {
                throw new IllegalArgumentException("context, rootTeam and sessionMembers can not be null!");
            }
            addToContext(context, createExtensionColl(rootTeam, sessionMembers), forceOverwrite);
        }
    }
    // Subclass for Mission Metadata
    public static class MissionMetadata extends ItsContextExtensionConcepts {
        // Singleton
        private static MissionMetadata instance = null;
        // Constructor
        private MissionMetadata() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#mission.metadata");
        }
        // Access
        public static MissionMetadata getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new MissionMetadata();
            }
            return instance;
        }
        // Methods
        public ObjectNode createExtensionColl(Mission mission) {
            if(mission == null) {
                throw new IllegalArgumentException("mission can not be null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            ext.put(extensionObjectKeys.SOURCE.getValue(), mission.getSource());
            ext.put(extensionObjectKeys.MET.getValue(), mission.getMET());
            ext.put(extensionObjectKeys.TASK.getValue(), mission.getTask());
            ext.put(extensionObjectKeys.SITUATION.getValue(), mission.getSituation());
            ext.put(extensionObjectKeys.GOALS.getValue(), mission.getGoals());
            ext.put(extensionObjectKeys.CONDITION.getValue(), mission.getCondition());
            ext.put(extensionObjectKeys.ROE.getValue(), mission.getROE());
            ext.put(extensionObjectKeys.THREAT_WARNING.getValue(), mission.getThreatWarning());
            ext.put(extensionObjectKeys.WEAPON_STATUS.getValue(), mission.getWeaponStatus());
            ext.put(extensionObjectKeys.WEAPON_POSTURE.getValue(), mission.getWeaponPosture());
            return ext;
        }
        public void addToContext(Context context, Mission mission, boolean forceOverwrite) {
            if(context == null || mission == null) {
                throw new IllegalArgumentException("context and mission can not be null!");
            }
            addToContext(context, createExtensionColl(mission), forceOverwrite);
        }
        public void addToContext(Context context, Mission mission) {
            addToContext(context, mission, false);
        }
    }
    // Subclass for Weather Environment Adaptation
    public static class WeatherEnvironmentAdaptation extends ItsContextExtensionConcepts {
        // Singleton
        private static WeatherEnvironmentAdaptation instance = null;
        // Constructor
        private WeatherEnvironmentAdaptation() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#environment.adaptation.weather");
        }
        // Access
        public static WeatherEnvironmentAdaptation getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new WeatherEnvironmentAdaptation();
            }
            return instance;
        }
        // Methods
        public ObjectNode createExtensionItem(EnvironmentAdaptation.Overcast overcast) {
            if(overcast == null) {
                throw new IllegalArgumentException("overcast can not be null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            if(overcast.getScenarioAdaptationDuration() != null) {
                ext.put(extensionObjectKeys.DURATION.getValue(), overcast.getScenarioAdaptationDuration().doubleValue());
            }
            ext.put(extensionObjectKeys.VALUE.getValue(), overcast.getValue().doubleValue());
            return ext;
        }
        
        public ObjectNode createExtensionItem(EnvironmentAdaptation.Fog fog) {
            if(fog == null) {
                throw new IllegalArgumentException("fog can not be null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            ext.put(extensionObjectKeys.DENSITY.getValue(), fog.getDensity().doubleValue());
            if(fog.getScenarioAdaptationDuration() != null) {
                ext.put(extensionObjectKeys.DURATION.getValue(), fog.getScenarioAdaptationDuration().doubleValue());
            }
            Color color = fog.getColor();
            if(color != null) {
                ObjectNode colorObj = new JsonNodeFactory(true).objectNode();
                colorObj.put(extensionObjectKeys.RED.getValue(), color.getRed());
                colorObj.put(extensionObjectKeys.BLUE.getValue(), color.getBlue());
                colorObj.put(extensionObjectKeys.GREEN.getValue(), color.getGreen());
                ext.set(extensionObjectKeys.COLOR.getValue(), colorObj);
            }
            return ext;
        }
        
        public ObjectNode createExtensionItem(EnvironmentAdaptation.Rain rain) {
            if(rain == null) {
                throw new IllegalArgumentException("rain can not be null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            if(rain.getScenarioAdaptationDuration() != null) {
                ext.put(extensionObjectKeys.DURATION.getValue(), rain.getScenarioAdaptationDuration().doubleValue());
            }
            ext.put(extensionObjectKeys.VALUE.getValue(), rain.getValue().doubleValue());
            return ext;
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.Overcast adaptation, boolean forceOverwrite) {
            if(context == null || adaptation == null) {
                throw new IllegalArgumentException("context and adaptation can not be null!");
            }
            addToContext(context, createExtensionItem(adaptation), forceOverwrite);
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.Overcast adaptation) {
            addToContext(context, adaptation, false);
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.Fog adaptation, boolean forceOverwrite) {
            if(context == null || adaptation == null) {
                throw new IllegalArgumentException("context and adaptation can not be null!");
            }
            addToContext(context, createExtensionItem(adaptation), forceOverwrite);
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.Fog adaptation) {
            addToContext(context, adaptation, false);
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.Rain adaptation, boolean forceOverwrite) {
            if(context == null || adaptation == null) {
                throw new IllegalArgumentException("context and adaptation can not be null!");
            }
            addToContext(context, createExtensionItem(adaptation), forceOverwrite);
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.Rain adaptation) {
            addToContext(context, adaptation, false);
        }
    }
    // Subclass for Coordinate context extension
    public static class CoordinateContext extends ItsContextExtensionConcepts {
        // Singleton
        private static CoordinateContext instance = null;
        // Constructor
        private CoordinateContext() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#environment.adaptation.coordinate");
        }
        // Access
        public static CoordinateContext getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CoordinateContext();
            }
            return instance;
        }
        
        public void addToContext(Context context, Coordinate coordinate, boolean forceOverwrite) {
            if(context == null || coordinate == null) {
                throw new IllegalArgumentException("context and coordinate can not be null!");
            }
            List<Coordinate> coll = new ArrayList<Coordinate>(1);
            coll.add(coordinate);
            addToContext(context, coll, forceOverwrite);
        }
        
        public void addToContext(Context context, Coordinate coordinate) {
            addToContext(context, coordinate, false);
        }
        
        public void addToContext(Context context, List<Coordinate> coordinates, boolean forceOverwrite) {
            if(context == null || coordinates == null) {
                throw new IllegalArgumentException("context and coordinates can not be null!");
            }
            addToContext(context, CoordinateExtension.createExtensionColl(coordinates), forceOverwrite);
        }
        
        public void addToContext(Context context, List<Coordinate> coordinates) {
            addToContext(context, coordinates, false);
        }
        
    }
    // Subclass for Created Actor Environment Adaptation
    public static class CreatedActor extends ItsContextExtensionConcepts {
        // Singleton
        private static CreatedActor instance = null;
        // Constructor
        private CreatedActor() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#environment.adaptation.create.actor");
        }
        // Access
        public static CreatedActor getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CreatedActor();
            }
            return instance;
        }
        
        public ObjectNode createExtensionItem(CreateActors adaptation) {
            if(adaptation == null) {
                throw new IllegalArgumentException("adaptation can not be null!");
            }
            ObjectNode ext = new JsonNodeFactory(true).objectNode();
            ext.put(extensionObjectKeys.TYPE.getValue(), adaptation.getType());
            Serializable side = adaptation.getSide().getType();
            String sideVal = null;
            if(side instanceof EnvironmentAdaptation.CreateActors.Side.Civilian) {
                sideVal = extensionObjectKeys.CIVILIAN.getValue();
            } else if(side instanceof EnvironmentAdaptation.CreateActors.Side.Blufor) {
                sideVal = extensionObjectKeys.BLUFOR.getValue();
            } else if(side instanceof EnvironmentAdaptation.CreateActors.Side.Opfor) {
                sideVal = extensionObjectKeys.OPFOR.getValue();
            }
            if(sideVal != null) {
                ext.put(extensionObjectKeys.SIDE.getValue(), sideVal);
            }
            ActorTypeCategoryEnum actorType = adaptation.getTypeCategory();
            if(actorType != null) {
                ext.put(extensionObjectKeys.ACTOR_CATEGORY.getValue(), actorType.value());
            }
            if(adaptation.getHeading() != null) {
                ext.put(extensionObjectKeys.HEADING.getValue(), adaptation.getHeading().getValue());
            }
            
            return ext;
        }
        
        public void addToContext(Context context, CreateActors adaptation, boolean forceOverwrite) {
            if(context == null || adaptation == null) {
                throw new IllegalArgumentException("context and adaptation can not be null!");
            }
            addToContext(context, createExtensionItem(adaptation), forceOverwrite);
        }
        
        public void addToContext(Context context, CreateActors adaptation) {
            addToContext(context, adaptation, false);
        }
    }
    // Subclass for Highlight Objects Environment Adaptation
    public static class Highlight extends ItsContextExtensionConcepts {
        // Singleton
        private static Highlight instance = null;
        // Constructor
        private Highlight() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#environment.adaptation.highlight");
        }
        // Access
        public static Highlight getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Highlight();
            }
            return instance;
        }
        
        public ObjectNode createExtensionItem(EnvironmentAdaptation.HighlightObjects.Offset offset, 
                EnvironmentAdaptation.HighlightObjects.Color color, BigInteger duration) {
            ObjectNode item, offsetNode;
            item = new JsonNodeFactory(true).objectNode();
            if(offset != null) {
                offsetNode = new JsonNodeFactory(true).objectNode();
                if(offset.getFront() != null) {
                    offsetNode.put(extensionObjectKeys.FRONT.getValue(), offset.getFront());
                }
                if(offset.getRight() != null) {
                    offsetNode.put(extensionObjectKeys.RIGHT.getValue(), offset.getRight());
                }
                if(offset.getUp() != null) {
                    offsetNode.put(extensionObjectKeys.UP.getValue(), offset.getUp());
                }
                if(offsetNode.size() != 0) {
                    item.set(extensionObjectKeys.OFFSET.getValue(), offsetNode);
                }
            }
            if(color != null) {
                Serializable kind = color.getType();
                if(kind instanceof EnvironmentAdaptation.HighlightObjects.Color.Red) {
                    item.put(extensionObjectKeys.COLOR.getValue(), extensionObjectKeys.RED.getValue());
                } else if(kind instanceof EnvironmentAdaptation.HighlightObjects.Color.Green) {
                    item.put(extensionObjectKeys.COLOR.getValue(), extensionObjectKeys.GREEN.getValue());
                } else if(kind instanceof EnvironmentAdaptation.HighlightObjects.Color.Blue) {
                    item.put(extensionObjectKeys.COLOR.getValue(), extensionObjectKeys.BLUE.getValue());
                }
            }
            if(duration != null) {
                item.put(extensionObjectKeys.DURATION.getValue(), duration.doubleValue());
            }
            return item;
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.HighlightObjects.Offset offset, 
                EnvironmentAdaptation.HighlightObjects.Color color, BigInteger duration, boolean forceOverwrite) {
            if(context == null) {
                throw new IllegalArgumentException("context can not be null!");
            }
            ObjectNode ext = createExtensionItem(offset, color, duration);
            if(ext.size() != 0) {
                addToContext(context, ext, forceOverwrite);
            }
        }
        
        public void addToContext(Context context, EnvironmentAdaptation.HighlightObjects.Offset offset, 
                EnvironmentAdaptation.HighlightObjects.Color color, BigInteger duration) {
            addToContext(context, offset, color, duration, false);
        }
    }
    // Subclass for Chain of Custody context extension
    public static class ChainOfCustody extends ItsContextExtensionConcepts {
        // Singleton
        private static ChainOfCustody instance = null;
        // Constructor
        private ChainOfCustody() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ContextExtension#chain.of.custody");
        }
        // Access
        public static ChainOfCustody getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new ChainOfCustody();
            }
            return instance;
        }
        private static void addCollToItem(ObjectNode item, extensionObjectKeys key, ArrayNode coll, Set<String> src) {
            if(item == null || key == null || coll == null || src == null) {
                throw new IllegalArgumentException("item, key, coll and src can not be null!");
            }
            for(String s : src) {
                if(s != null && StringUtils.isNotBlank(s)) {
                    coll.add(s);
                }
            }
            if(coll.size() != 0) {
                item.set(key.getValue(), coll);
            }
        }
        public ObjectNode createExtensionItem(String logFilePath, Set<String> dkfFileNames, Set<String> logFileNames) {
            if(logFilePath == null) {
                throw new IllegalArgumentException("logFilePath can not be null!");
            }
            ObjectNode item;
            item = new JsonNodeFactory(true).objectNode();
            // Version info
            Version version = Version.getInstance();
            item.put(extensionObjectKeys.RELEASE_DATE.getValue(), version.getReleaseDate());
            item.put(extensionObjectKeys.VERSION_NAME.getValue(), version.getName());
            item.put(extensionObjectKeys.BUILD_DATE.getValue(), version.getBuildDate());
            // IP
            item.put(extensionObjectKeys.IP.getValue(), Util.getLocalHostAddress().getHostAddress());
            // Log file folder
            item.put(extensionObjectKeys.LOG_FILE_PATH.getValue(), logFilePath);
            // Log file names
            if(CollectionUtils.isNotEmpty(logFileNames)) {
                ArrayNode logFileColl = new JsonNodeFactory(true).arrayNode();
                addCollToItem(item, extensionObjectKeys.LOG_FILE_NAMES, logFileColl, logFileNames);
            }
            // DKF file names
            if(CollectionUtils.isNotEmpty(dkfFileNames)) {
                ArrayNode dkfFileColl = new JsonNodeFactory(true).arrayNode();
                addCollToItem(item, extensionObjectKeys.DKF_FILE_NAMES, dkfFileColl, dkfFileNames);
            }
            return item;
        }
        public void addToContext(Context context, String logFilePath, Set<String> dkfFileNames, Set<String> logFileNames, boolean forceOverwrite) {
            if(context == null || logFilePath == null) {
                throw new IllegalArgumentException("context and logFilePath can not be null!");
            }
            addToContext(context, createExtensionItem(logFilePath, dkfFileNames, logFileNames), forceOverwrite);
        }
        public void addToContext(Context context, String logFilePath, Set<String> dkfFileNames, Set<String> logFileNames) {
            addToContext(context, logFilePath, dkfFileNames, logFileNames, false);
        }
    }
}
