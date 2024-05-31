package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Group;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import generated.dkf.Coordinate;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScore;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.LrsEnum;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ResultExtensionConcept;

/**
 * Result Extensions defined within the ITS xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ItsResultExtensionConcepts extends ResultExtensionConcept {
    
    private static Logger logger = LoggerFactory.getLogger(ItsResultExtensionConcepts.class);
    
    protected ItsResultExtensionConcepts(String id) throws LmsXapiProfileException {
        super(id, giftStatementExtensionSparqlQuery(id, true));
    }
    
    // Enum for Extension Keys
    public enum extensionObjectKeys implements LrsEnum {
        NAME("name"),
        UNITS("units"),
        VALUE("value"),
        ID("id"),
        STATE("state"),
        OBSERVED("hasObservedAssessment"),
        CONFIDENCE("confidence"),
        COMPETENCE("competence"),
        TREND("trend"),
        EXPLANATION("explanation"),
        PREDICTED("predicted"),
        SHORT_TERM("shortTerm"),
        LONG_TERM("longTerm"),
        ASSESSMENT("assessment"),
        TIMESTAMP("timestamp"),
        ASSESSED_TEAM_ENTITIES("assessedTeamOrgEntities"),
        SCENARIO_SUPPORT("scenarioSupport"),
        PLACE_OF_INTEREST("placeOfInterest"),
        COORDINATE("coordinate");
        private String value;
        extensionObjectKeys(String s) {
            this.value = s;
        }
        @Override
        public String getValue() {
            return value;
        }
    }
    // Subclass for assessment results
    public static class ConceptEvaluation extends ItsResultExtensionConcepts implements RawScoreNodeExtension {
        // Singleton
        private static ConceptEvaluation instance = null;
        // Constructor
        private ConceptEvaluation() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ResultExtension#assessment.node.evaluation");
        }
        // Access
        public static ConceptEvaluation getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new ConceptEvaluation();
            }
            return instance;
        }
        // Methods
        /**
         * Create Extension JSON from RawScoreNode
         * 
         * @param node - RawScoreNode to parse
         * 
         * @return extension JSON
         */
        public static ObjectNode createExtensionItem(RawScoreNode node) {
            if(node == null) {
                throw new IllegalArgumentException("node can not be null!");
            }
            RawScore rawScore = node.getRawScore();
            String assessment = node.getAssessment().getName();
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            item.put(extensionObjectKeys.NAME.getValue(), node.getName());
            item.put(extensionObjectKeys.UNITS.getValue(), rawScore.getUnitsLabel());
            item.put(extensionObjectKeys.VALUE.getValue(), rawScore.getValueAsString());
            item.put(extensionObjectKeys.ASSESSMENT.getValue(), assessment);
            return item;
        }
        @Override
        public Extensions asExtension(RawScoreNode node, Extensions ext) {
            return asExtension(node, ext, false);
        }
        @Override
        public Extensions asExtension(RawScoreNode node, Extensions ext, boolean forceOverwrite) {
            if(node == null || ext == null) {
                throw new IllegalArgumentException("ext and node can not be null!");
            }
            return asExtension(createExtensionItem(node), ext, forceOverwrite);
        }
        @Override
        public void addToResult(Result result, RawScoreNode node) {
            addToResult(result, node, false);
        }
        @Override
        public void addToResult(Result result, RawScoreNode node, boolean forceOverwrite) {
            if(result == null || node == null) {
                throw new IllegalArgumentException("result and node can not be null!");
            }
            addToResult(result, createExtensionItem(node), forceOverwrite);
        }
        @Override
        public void addToStatement(Statement statement, RawScoreNode node) {
            addToStatement(statement, node, false);
        }
        @Override
        public void addToStatement(Statement statement, RawScoreNode node, boolean forceOverwrite) {
            if(statement == null || node == null) {
                throw new IllegalArgumentException("statement and node can not be null!");
            }
            addToStatement(statement, createExtensionItem(node), forceOverwrite);
        }
        /**
         * Parse Extension JSON to RawScoreNode
         * 
         * @param extValItem - Extension JSON from statement
         * @param actorSlugs - relevant user names
         * 
         * @return RawScoreNode recreated from Extension JSON
         */
        public static RawScoreNode parseToRawScoreNode(ObjectNode extValItem, Set<String> actorSlugs) {
            if(extValItem == null) {
                throw new IllegalArgumentException("extValItem can not be null!");
            }
            String v = extValItem.get(extensionObjectKeys.VALUE.getValue()).asText();
            String u = extValItem.get(extensionObjectKeys.UNITS.getValue()).asText();
            String n = extValItem.get(extensionObjectKeys.NAME.getValue()).asText();
            String a = extValItem.get(extensionObjectKeys.ASSESSMENT.getValue()).asText();
            return new RawScoreNode(n, new DefaultRawScore(v, u), AssessmentLevelEnum.valueOf(a), actorSlugs);
        }
        /**
         * Parse ConceptEvaluation from Statement and create RawScoreNode
         * 
         * @param statement - xAPI Statement with ConceptEvaluation Result Extension
         * 
         * @return RawScoreNode recreated from Statement
         * 
         * @throws LmsXapiExtensionException when unable to parse user name(s) from xAPI Statement
         */
        public RawScoreNode parseToRawScoreNode(Statement statement) throws LmsXapiExtensionException {
            if(statement == null || statement.getActor() == null || statement.getResult() == null || statement.getResult().getExtensions() == null) {
                throw new IllegalArgumentException("Unable to parse RawScoreNode from incompatable statement!");
            }
            String n;
            try {
                n = PersonaHelper.getActorName(statement);
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiExtensionException("Unable to parse Actor name from Statement!", e);
            }
            Set<String> s = new HashSet<String>();
            if(statement.getActor() instanceof Group) {
                for(String m : StringUtils.split(n, ",")) {
                    s.add(m);
                }
            } else {
                s.add(n);
            }
            return parseToRawScoreNode((ObjectNode) parseFromExtensions(statement.getResult().getExtensions()), s);
        }
        /**
         * Parse RawScoreNode from xAPI Statement and add as child to passed in GradedScoreNode
         * 
         * @param parent - GradedScoreNode to add child to
         * @param statement - xAPI Statement to parse RawScoreNode from
         * 
         * @throws LmsXapiExtensionException when unable to parse user name(s) from xAPI Statement
         */
        public void parseToGradedScoreNode(GradedScoreNode parent, Statement statement) throws LmsXapiExtensionException {
            if(parent == null || statement == null) {
                throw new IllegalArgumentException("parent and statement can not be null!");
            }
            parent.addChild(parseToRawScoreNode(statement));
        }
    }
    // Performance State Attribute result extension
    public static class PerformanceMeasure extends ItsResultExtensionConcepts implements PerformanceStateExtension {
        // Team Role Activity Type
        ItsActivityTypeConcepts.TeamRole teamRoleATC;
        // Singleton
        private static PerformanceMeasure instance = null;
        // Constructor
        private PerformanceMeasure() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ResultExtension#assessment.node.performance.measure");
            teamRoleATC = ItsActivityTypeConcepts.TeamRole.getInstance();
        }
        // Access
        public static PerformanceMeasure getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new PerformanceMeasure();
            }
            return instance;
        }
        // Methods
        /**
         * Create Extension JSON from AbstractPerformanceState
         * 
         * @param performanceState - performanceState to parse to Extension JSON
         * 
         * @return Extension JSON for AbstractPerformanceState
         */
        public ObjectNode createExtensionItem(AbstractPerformanceState performanceState) {
            if(performanceState == null || performanceState.getState() == null) {
                throw new IllegalArgumentException("performanceState can not be null and must have non-null state!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            PerformanceStateAttribute attribute = performanceState.getState();
            item.put(extensionObjectKeys.ID.getValue(), attribute.getNodeId());
            item.put(extensionObjectKeys.STATE.getValue(), attribute.getNodeStateEnum().getDisplayName());
            item.put(extensionObjectKeys.OBSERVED.getValue(), performanceState.isContainsObservedAssessmentCondition());
            item.put(extensionObjectKeys.CONFIDENCE.getValue(), attribute.getConfidence());
            item.put(extensionObjectKeys.COMPETENCE.getValue(), attribute.getCompetence());
            item.put(extensionObjectKeys.TREND.getValue(), attribute.getTrend());
            item.put(extensionObjectKeys.SCENARIO_SUPPORT.getValue(), attribute.isScenarioSupportNode());
            // explanations
            ArrayNode explanationColl = new JsonNodeFactory(true).arrayNode();
            Set<String> allExpl = attribute.getAssessmentExplanation();
            if(allExpl != null) {
                for(String expl : allExpl) {
                    if(expl != null && StringUtils.isNotBlank(expl)) {
                        explanationColl.add(expl);
                    }
                }
            }
            item.set(extensionObjectKeys.EXPLANATION.getValue(), explanationColl);
            // Assessed Team Entities
            ObjectNode assessedTeamOrgEntities = new JsonNodeFactory(true).objectNode();
            // populate object node
            if(CollectionUtils.isNotEmpty(attribute.getAssessedTeamOrgEntities())){
                // String is TeamMember<?> name
                for(Map.Entry<String, AssessmentLevelEnum> kv : attribute.getAssessedTeamOrgEntities().entrySet()) {
                    String teamRoleName = kv.getKey();
                    MarkedTeamMember teamRole = new MarkedTeamMember(teamRoleName, teamRoleName);
                    // Activity Id for Team Role Positions
                    String teamRoleActivityId;
                    try {
                        teamRoleActivityId = teamRoleATC.asActivity(teamRole).getId().toString();
                    } catch (LmsXapiActivityException e) {
                        logger.error("Unable to create Team Role Activity from label!", e);
                        continue;
                    }
                    assessedTeamOrgEntities.put(teamRoleActivityId, kv.getValue().getDisplayName());
                }
            }
            item.set(extensionObjectKeys.ASSESSED_TEAM_ENTITIES.getValue(), assessedTeamOrgEntities);
            // predicted
            ObjectNode predicted = new JsonNodeFactory(true).objectNode();
            predicted.put(extensionObjectKeys.ASSESSMENT.getValue(), attribute.getPredicted().getDisplayName());
            predicted.put(extensionObjectKeys.TIMESTAMP.getValue(), attribute.getPredictedTimestamp());
            item.set(extensionObjectKeys.PREDICTED.getValue(), predicted);
            // shortTerm
            ObjectNode shortTerm = new JsonNodeFactory(true).objectNode();
            shortTerm.put(extensionObjectKeys.ASSESSMENT.getValue(), attribute.getShortTerm().getDisplayName());
            shortTerm.put(extensionObjectKeys.TIMESTAMP.getValue(), attribute.getShortTermTimestamp());
            item.set(extensionObjectKeys.SHORT_TERM.getValue(), shortTerm);
            // longTerm
            ObjectNode longTerm = new JsonNodeFactory(true).objectNode();
            longTerm.put(extensionObjectKeys.ASSESSMENT.getValue(), attribute.getLongTerm().getDisplayName());
            longTerm.put(extensionObjectKeys.TIMESTAMP.getValue(), attribute.getLongTermTimestamp());
            item.set(extensionObjectKeys.LONG_TERM.getValue(), longTerm);
            return item;
        }
        @Override
        public Extensions asExtension(AbstractPerformanceState performanceState, Extensions ext) {
            return asExtension(performanceState, ext, false);
        }
        @Override
        public Extensions asExtension(AbstractPerformanceState performanceState, Extensions ext, boolean forceOverwrite) {
            if(performanceState == null || ext == null) {
                throw new IllegalArgumentException("performanceState and ext can not be null!");
            }
            return asExtension(createExtensionItem(performanceState), ext, forceOverwrite);
        }
        @Override
        public void addToResult(Result result, AbstractPerformanceState performanceState) {
            addToResult(result, performanceState, false);
        }
        @Override
        public void addToResult(Result result, AbstractPerformanceState performanceState, boolean forceOverwrite) {
            if(result == null || performanceState == null) {
                throw new IllegalArgumentException("result and performanceState can not be null!");
            }
            addToResult(result, createExtensionItem(performanceState), forceOverwrite);
        }
        @Override
        public void addToStatement(Statement statement, AbstractPerformanceState performanceState) {
            addToStatement(statement, performanceState, false);
        }
        @Override
        public void addToStatement(Statement statement, AbstractPerformanceState performanceState, boolean forceOverwrite) {
            if(statement == null || performanceState == null) {
                throw new IllegalArgumentException("statement and performanceState can not be null!");
            }
            addToStatement(statement, createExtensionItem(performanceState), forceOverwrite);
        }
    }
    // Subclass for Learner State Attribute result extension
    public static class AttributeMeasure extends ItsResultExtensionConcepts implements LearnerStateAttributeExtension {
        // Singleton
        private static AttributeMeasure instance = null;
        // Constructor
        private AttributeMeasure() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ResultExtension#learner.state.attribute.measure");
        }
        // Access
        public static AttributeMeasure getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new AttributeMeasure();
            }
            return instance;
        }
        // Methods
        /**
         * Parse LearnerStateAttribute to JSON
         * 
         * @param attribute - LearnerStateAttribute to parse
         *  
         * @return parsed JSON
         */
        public ObjectNode createExtensionItem(LearnerStateAttribute attribute) {
            if(attribute == null) {
                throw new IllegalArgumentException("attribute can not be null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            // predicted
            ObjectNode predicted = new JsonNodeFactory(true).objectNode();
            predicted.put(extensionObjectKeys.ASSESSMENT.getValue(), attribute.getPredicted().getDisplayName());
            predicted.put(extensionObjectKeys.TIMESTAMP.getValue(), attribute.getPredictedTimestamp());
            item.set(extensionObjectKeys.PREDICTED.getValue(), predicted);
            // shortTerm
            ObjectNode shortTerm = new JsonNodeFactory(true).objectNode();
            shortTerm.put(extensionObjectKeys.ASSESSMENT.getValue(), attribute.getShortTerm().getDisplayName());
            shortTerm.put(extensionObjectKeys.TIMESTAMP.getValue(), attribute.getShortTermTimestamp());
            item.set(extensionObjectKeys.SHORT_TERM.getValue(), shortTerm);
            // longTerm
            ObjectNode longTerm = new JsonNodeFactory(true).objectNode();
            longTerm.put(extensionObjectKeys.ASSESSMENT.getValue(), attribute.getLongTerm().getDisplayName());
            longTerm.put(extensionObjectKeys.TIMESTAMP.getValue(), attribute.getLongTermTimestamp());
            item.set(extensionObjectKeys.LONG_TERM.getValue(), longTerm);
            return item;
        }
        @Override
        public Extensions asExtension(LearnerStateAttribute attribute, Extensions ext) {
            return asExtension(attribute, ext, false);
        }
        @Override
        public Extensions asExtension(LearnerStateAttribute attribute, Extensions ext, boolean forceOverwrite) {
            if(attribute == null || ext == null) {
                throw new IllegalArgumentException("attribute and ext can not be null!");
            }
            return asExtension(createExtensionItem(attribute), ext, forceOverwrite);
        }
        @Override
        public void addToResult(Result result, LearnerStateAttribute attribute) {
            addToResult(result, attribute, false);
        }
        @Override
        public void addToResult(Result result, LearnerStateAttribute attribute, boolean forceOverwrite) {
            if(result == null || attribute == null) {
                throw new IllegalArgumentException("result and attribute can not be null!");
            }
            addToResult(result, createExtensionItem(attribute), forceOverwrite);
        }
        @Override
        public void addToStatement(Statement statement, LearnerStateAttribute attribute) {
            addToStatement(statement, attribute, false);
        }
        @Override
        public void addToStatement(Statement statement, LearnerStateAttribute attribute, boolean forceOverwrite) {
            if(statement == null || attribute == null) {
                throw new IllegalArgumentException("statement and attribute can not be null!");
            }
            addToStatement(statement, createExtensionItem(attribute), forceOverwrite);
        }
    }
    // Subclass for Coordinate result extension
    public static class CoordinateResult extends ItsResultExtensionConcepts implements CoordinateResultExtension {
        // Singleton
        private static CoordinateResult instance = null;
        // Constructor
        private CoordinateResult() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ResultExtension#environment.adaptation.coordinate");
        }
        // Access
        public static CoordinateResult getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new CoordinateResult();
            }
            return instance;
        }
        @Override
        public Extensions asExtension(Coordinate coordinate, Extensions ext) {
            return asExtension(coordinate, ext, false);
        }
        @Override
        public Extensions asExtension(Coordinate coordinate, Extensions ext, boolean forceOverwrite) {
            if(coordinate == null || ext == null) {
                throw new IllegalArgumentException("coordinate and ext can not be null!");
            }
            List<Coordinate> coll = new ArrayList<Coordinate>(1);
            coll.add(coordinate);
            return asExtension(coll, ext, forceOverwrite);
        }
        @Override
        public Extensions asExtension(List<Coordinate> coordinates, Extensions ext) {
            return asExtension(coordinates, ext, false);
        }
        @Override
        public Extensions asExtension(List<Coordinate> coordinates, Extensions ext, boolean forceOverwrite) {
            if(coordinates == null || ext == null) {
                throw new IllegalArgumentException("coordinate and ext can not be null!");
            }
            return asExtension(CoordinateExtension.createExtensionColl(coordinates), ext, forceOverwrite);
        }
        @Override
        public void addToResult(Result result, Coordinate coordinate) {
            addToResult(result, coordinate, false);
        }
        @Override
        public void addToResult(Result result, Coordinate coordinate, boolean forceOverwrite) {
            if(result == null || coordinate == null) {
                throw new IllegalArgumentException("result and coordinate can not be null!");
            }
            List<Coordinate> coll = new ArrayList<Coordinate>(1);
            coll.add(coordinate);
            addToResult(result, coll, forceOverwrite);
        }
        @Override
        public void addToResult(Result result, List<Coordinate> coordinates) {
            addToResult(result, coordinates, false);
        }
        @Override
        public void addToResult(Result result, List<Coordinate> coordinates, boolean forceOverwrite) {
            if(result == null || coordinates == null) {
                throw new IllegalArgumentException("result and coordinates can not be null!");
            }
            addToResult(result, CoordinateExtension.createExtensionColl(coordinates), forceOverwrite);
        }
        @Override
        public void addToStatement(Statement statement, Coordinate coordinate) {
            addToStatement(statement, coordinate, false);
        }
        @Override
        public void addToStatement(Statement statement, Coordinate coordinate, boolean forceOverwrite) {
            if(statement == null || coordinate == null) {
                throw new IllegalArgumentException("statement and coordinate can not be null!");
            }
            List<Coordinate> coll = new ArrayList<Coordinate>(1);
            coll.add(coordinate);
            addToStatement(statement, coll, false);
        }
        @Override
        public void addToStatement(Statement statement, List<Coordinate> coordinates) {
            addToStatement(statement, coordinates, false);
        }
        @Override
        public void addToStatement(Statement statement, List<Coordinate> coordinates, boolean forceOverwrite) {
            if(statement == null || coordinates == null) {
                throw new IllegalArgumentException("statement and coordinate can not be null!");
            }
            addToStatement(statement, CoordinateExtension.createExtensionColl(coordinates), forceOverwrite);
        }
    }
    // Subclass for Highlight Objects Location Info result extension
    public static class LocationInfo extends ItsResultExtensionConcepts implements LocationInfoResultExtension {
        // Singleton
        private static LocationInfo instance = null;
        // Constructor
        private LocationInfo() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/ResultExtension#environment.adaptation.location");
        }
        // Access
        public static LocationInfo getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new LocationInfo();
            }
            return instance;
        }
        /**
         * Create Extension JSON from LocationInfo
         * 
         * @param coordinate - Coordinate from LocationInfo
         * @param placeOfInterestRef - string from LocationInfo
         *  
         * @return Extension JSON
         */
        private ObjectNode createExtensionItem(Coordinate coordinate, String placeOfInterestRef) {
            if(placeOfInterestRef == null) {
                throw new IllegalArgumentException("placeOfInterestRef can not be null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(coordinate != null) {
                ArrayNode coordinateNode = CoordinateExtension.createExtensionColl(coordinate);
                item.set(extensionObjectKeys.COORDINATE.getValue(), coordinateNode);
            }
            item.put(extensionObjectKeys.PLACE_OF_INTEREST.getValue(), placeOfInterestRef);
            return item;
        }
        /**
         * Create Extension JSON from LocationInfo
         * 
         * @param coordinate - Coordinate(s) from LocationInfo
         * @param placeOfInterestRef - string from LocationInfo
         * 
         * @return Extension JSON
         */
        private ObjectNode createExtensionItem(List<Coordinate> coordinate, String placeOfInterestRef) {
            if(placeOfInterestRef == null) {
                throw new IllegalArgumentException("placeOfInterestRef can not be null!");
            }
            ObjectNode item = new JsonNodeFactory(true).objectNode();
            if(coordinate != null) {
                ArrayNode coordinateNode = CoordinateExtension.createExtensionColl(coordinate);
                item.set(extensionObjectKeys.COORDINATE.getValue(), coordinateNode);
            }
            item.put(extensionObjectKeys.PLACE_OF_INTEREST.getValue(), placeOfInterestRef);
            return item;
        }
        /**
         * Create Extension JSON from LocationInfo
         * 
         * @param locationInfo - LocationInfo from Breadcrumbs EnvironmentAdaptation
         * 
         * @return Extension JSON
         */
        public ObjectNode createExtensionItem(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo) {
            if(locationInfo == null) {
                throw new IllegalArgumentException("locationInfo can not be null!");
            }
            return createExtensionItem(locationInfo.getCoordinate(), locationInfo.getPlaceOfInterestRef());
        }
        /**
         * Create Extension JSON from LocationInfo
         * 
         * @param locationInfo - LocationInfo from HighlightObjects EnvironmentAdaptation
         * 
         * @return Extension JSON
         */
        public ObjectNode createExtensionItem(EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo) {
            if(locationInfo == null) {
                throw new IllegalArgumentException("locationInfo can not be null!");
            }
            return createExtensionItem(locationInfo.getCoordinate(), locationInfo.getPlaceOfInterestRef());
        }
        @Override
        public Extensions asExtension(EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, Extensions ext) {
            return asExtension(locationInfo, ext, false);
        }
        @Override
        public Extensions asExtension(EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, Extensions ext, boolean forceOverwrite) {
            if(locationInfo == null || ext == null) {
                throw new IllegalArgumentException("locationInfo and ext can not be null!");
            }
            return asExtension(createExtensionItem(locationInfo), ext, forceOverwrite);
        }
        @Override
        public Extensions asExtension(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, Extensions ext) {
            return asExtension(locationInfo, ext, false);
        }
        @Override
        public Extensions asExtension(EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, Extensions ext, boolean forceOverwrite) {
            if(locationInfo == null || ext == null) {
                throw new IllegalArgumentException("locationInfo and ext can not be null!");
            }
            return asExtension(createExtensionItem(locationInfo), ext, forceOverwrite);
        }
        @Override
        public void addToResult(Result result, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo) {
            addToResult(result, locationInfo, false);
        }
        @Override
        public void addToResult(Result result, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, boolean forceOverwrite) {
            if(result == null || locationInfo == null) {
                throw new IllegalArgumentException("result and locationInfo can not be null!");
            }
            addToResult(result, createExtensionItem(locationInfo), forceOverwrite);
        }
        @Override
        public void addToResult(Result result, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo) {
            addToResult(result, locationInfo, false);
        }
        @Override
        public void addToResult(Result result, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, boolean forceOverwrite) {
            if(result == null || locationInfo == null) {
                throw new IllegalArgumentException("result and locationInfo can not be null!");
            }
            addToResult(result, createExtensionItem(locationInfo), forceOverwrite);
        }
        @Override
        public void addToStatement(Statement statement, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo) {
            addToStatement(statement, locationInfo, false);
        }
        @Override
        public void addToStatement(Statement statement, EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo, boolean forceOverwrite) {
            if(statement == null || locationInfo == null) {
                throw new IllegalArgumentException("statement and locationInfo can not be null!");
            }
            addToStatement(statement, createExtensionItem(locationInfo), forceOverwrite);
        }
        @Override
        public void addToStatement(Statement statement, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo) {
            addToStatement(statement, locationInfo, false);
        }
        @Override
        public void addToStatement(Statement statement, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo, boolean forceOverwrite) {
            if(statement == null || locationInfo == null) {
                throw new IllegalArgumentException("statement and locationInfo can not be null!");
            }
            addToStatement(statement, createExtensionItem(locationInfo), forceOverwrite);
        }
    }
}
