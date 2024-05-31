package mil.arl.gift.lms.impl.lrs.xapi.profile.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.rusticisoftware.tincan.InteractionComponent;

/**
 * Parses SPARQL JSON node(s) to corresponding xAPI Profile component field. Allows for
 * instantiation of xAPI Profile component representation classes from (live or local) Profile Server clients.
 * 
 * @author Yet Analytics
 *
 */
public class SparqlResult {
    // Fields across xAPI Profile Components
    private String inScheme;
    private String prefLabel;
    private String prefLabelLang;
    private String definition;
    private String definitionLang;
    private String deprecated;
    private String type;
    private String activityType;
    private String activityName;
    private String activityNameLang;
    private String activityDescription;
    private String activityDescriptionLang;
    private String activityMoreInfo;
    private String activityInteractionType;
    private String context;
    private String schema;
    private String inlineSchema;
    private String contentType;
    private String verb;
    private String objectActivityType;
    private String primary;
    private String optional;
    private String oneOrMore;
    private String zeroOrMore;
    private Map<String, String> activityExtensions;
    private Set<String> activityCorrectResponsePattern;
    private Set<InteractionComponent> activityChoices;
    private Set<InteractionComponent> activityScale;
    private Set<InteractionComponent> activitySource;
    private Set<InteractionComponent> activityTarget;
    private Set<InteractionComponent> activitySteps;
    private Set<String> broader;
    private Set<String> broadMatch;
    private Set<String> narrower;
    private Set<String> narrowMatch;
    private Set<String> related;
    private Set<String> relatedMatch;
    private Set<String> exactMatch;
    private Set<String> recommendedActivityTypes;
    private Set<String> recommendedVerbs;
    private Set<String> contextGroupingActivityType;
    private Set<String> contextParentActivityType;
    private Set<String> contextOtherActivityType;
    private Set<String> contextCategoryActivityType;
    private Set<String> attachmentUsageType;
    private Set<String> objectStatementRefTemplate;
    private Set<String> contextStatementRefTemplate;
    private Set<String> alternates;
    private Set<String> sequence;
    
    // getters + setters
    public String getInScheme() {
        return inScheme;
    }
    
    public String getPrefLabel() {
        return prefLabel;
    }
    
    public String getPrefLabelLang() {
        return prefLabelLang;
    }
    
    public String getDefinition() {
        return definition;
    }
    
    public String getDefinitionLang() {
        return definitionLang;
    }
    
    public String getDeprecated() {
        return deprecated;
    }
    
    public String getType() {
        return type;
    }
    
    public String getContext() {
        return context;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public String getInlineSchema() {
        return inlineSchema;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public String getVerb() {
        return verb;
    }
    
    public String getObjectActivityType() {
        return objectActivityType;
    }
    
    public String getActivityType() {
        return activityType;
    }
    
    public String getActivityName() {
        return activityName;
    }
    
    public String getActivityNameLang() {
        return activityNameLang;
    }
    
    public String getActivityDescription() {
        return activityDescription;
    }
    
    public String getActivityDescriptionLang() {
        return activityDescriptionLang;
    }
    
    public String getActivityMoreInfo() {
        return activityMoreInfo;
    }
    
    public String getActivityInteractionType() {
        return activityInteractionType;
    }
    
    public String getPrimary() {
        return primary;
    }
    
    public String getOptional() {
        return optional;
    }
    
    public String getOneOrMore() {
        return oneOrMore;
    }
    
    public String getZeroOrMore() {
        return zeroOrMore;
    }
    
    public Set<String> getAlternates() {
        return alternates != null ? alternates : new HashSet<String>();
    }
    
    public void setAlternates(Set<String> coll) {
        this.alternates = coll;
    }
    
    public Set<String> getSequence() {
        return sequence != null ? sequence : new HashSet<String>();
    }
    
    public void setSequence(Set<String> coll) {
        this.sequence = coll;
    }
    
    public Map<String, String> getActivityExtensions() {
        return activityExtensions != null ? activityExtensions : new HashMap<String, String>();
    }
    
    public void setActivityExtensions(Map<String, String> coll) {
        this.activityExtensions = coll;
    }
    
    public Set<String> getActivityCorrectResponsePattern() {
        return activityCorrectResponsePattern != null ? activityCorrectResponsePattern : new HashSet<String>();
    }
    
    public void setActivityCorrectResponsePattern(Set<String> coll) {
        this.activityCorrectResponsePattern = coll;
    }
    
    public Set<InteractionComponent> getActivityChoices() {
        return activityChoices != null ? activityChoices : new HashSet<InteractionComponent>();
    }
    
    public void setActivityChoices(Set<InteractionComponent> coll) {
        this.activityChoices = coll;
    }
    
    public Set<InteractionComponent> getActivityScale() {
        return activityScale != null ? activityScale : new HashSet<InteractionComponent>();
    }
    
    public void setActivityScale(Set<InteractionComponent> coll) {
        this.activityScale = coll;
    }
    
    public Set<InteractionComponent> getActivitySource() {
        return activitySource != null ? activitySource : new HashSet<InteractionComponent>();
    }
    
    public void setActivitySource(Set<InteractionComponent> coll) {
        this.activitySource = coll;
    }
    
    public Set<InteractionComponent> getActivityTarget() {
        return activityTarget != null ? activityTarget : new HashSet<InteractionComponent>();
    }
    
    public void setActivityTarget(Set<InteractionComponent> coll) {
        this.activityTarget = coll;
    }
    
    public Set<InteractionComponent> getActivitySteps() {
        return activitySteps != null ? activitySteps : new HashSet<InteractionComponent>();
    }
    
    public void setActivitySteps(Set<InteractionComponent> coll) {
        this.activitySteps = coll;
    }
    
    public Set<String> getBroader() {
        return broader != null ? broader : new HashSet<String>();
    }
    
    public void setBroader(Set<String> coll) {
        this.broader = coll;
    }
    
    public Set<String> getBroadMatch() {
        return broadMatch != null ? broadMatch : new HashSet<String>();
    }
    
    public void setBroadMatch(Set<String> coll) {
        this.broadMatch = coll;
    }
    
    public Set<String> getNarrower() {
        return narrower != null ? narrower : new HashSet<String>();
    }
    
    public void setNarrower(Set<String> coll) {
        this.narrower = coll;
    }
    
    public Set<String> getNarrowMatch() {
        return narrowMatch != null ? narrowMatch : new HashSet<String>();
    }
    
    public void setNarrowMatch(Set<String> coll) {
        this.narrowMatch = coll;
    }
    
    public Set<String> getRelated() {
        return related != null ? related : new HashSet<String>();
    }
    
    public void setRelated(Set<String> coll) {
        this.related = coll;
    }
    
    public Set<String> getRelatedMatch() {
        return relatedMatch != null ? relatedMatch : new HashSet<String>();
    }
    
    public void setRelatedMatch(Set<String> coll) {
        this.relatedMatch = coll;
    }
    
    public Set<String> getExactMatch() {
        return exactMatch != null ? exactMatch : new HashSet<String>();
    }
    
    public void setExactMatch(Set<String> coll) {
        this.exactMatch = coll;
    }
    
    public Set<String> getRecommendedActivityTypes() {
        return recommendedActivityTypes != null ? recommendedActivityTypes : new HashSet<String>();
    }
    
    public void setRecommendedActivityTypes(Set<String> coll) {
        this.recommendedActivityTypes = coll;
    }
    
    public Set<String> getRecommendedVerbs() {
        return recommendedVerbs != null ? recommendedVerbs : new HashSet<String>();
    }
    
    public void setRecommendedVerbs(Set<String> coll) {
        this.recommendedVerbs = coll;
    } 
    
    public Set<String> getContextGroupingActivityType() {
        return contextGroupingActivityType != null ? contextGroupingActivityType : new HashSet<String>();
    }
    
    public void setContextGroupingActivityType(Set<String> coll) {
        this.contextGroupingActivityType = coll;
    }
    
    public Set<String> getContextParentActivityType() {
        return contextParentActivityType != null ? contextParentActivityType : new HashSet<String>();
    }
    
    public void setContextParentActivityType(Set<String> coll) {
        this.contextParentActivityType = coll;
    }
    
    public Set<String> getContextOtherActivityType() {
        return contextOtherActivityType != null ? contextOtherActivityType : new HashSet<String>();
    }
    
    public void setContextOtherActivityType(Set<String> coll) {
        this.contextOtherActivityType = coll;
    }
    
    public Set<String> getContextCategoryActivityType() {
        return contextCategoryActivityType != null ? contextCategoryActivityType : new HashSet<String>();
    }
    
    public void setContextCategoryActivityType(Set<String> coll) {
        this.contextCategoryActivityType = coll;
    }
    
    public Set<String> getAttachmentUsageType() {
        return attachmentUsageType != null ? attachmentUsageType : new HashSet<String>();
    }
    
    public void setAttachmentUsageType(Set<String> coll) {
        this.attachmentUsageType = coll;
    }
    
    public Set<String> getObjectStatementRefTemplate() {
        return objectStatementRefTemplate != null ? objectStatementRefTemplate : new HashSet<String>();
    }
    
    public void setObjectStatementRefTemplate(Set<String> coll) {
        this.objectStatementRefTemplate = coll;
    }
    
    public Set<String> getContextStatementRefTemplate() {
        return contextStatementRefTemplate != null ? contextStatementRefTemplate : new HashSet<String>();
    }
    
    public void setContextStatementRefTemplate(Set<String> coll) {
        this.contextStatementRefTemplate = coll;
    }
    
    // Helper fns based on expected JSON from SPARQL query or Local Client
    private static String parseValue(JsonNode node) {
        if(node == null || node.isNull()) {
            return null;
        } else if(node.get("value") != null) {
            return node.get("value").asText();
        } else if(node.isObject()){
            Iterator<String> iter = node.fieldNames();
            return node.get(iter.next()).asText();
        } else {
            return node.asText();
        }
    }
    
    private static String parseLang(JsonNode node) {
        if(node == null || node.isNull()) {
            return null;
        } else if(node.get("lang") != null) {
            return node.get("lang").asText();
        } else if(node.isObject()){
            Iterator<String> iter = node.fieldNames();
            // Language map key
            return iter.next();
        } else {
            return null;
        }
    }
    
    private static JsonNode handleVariableKeys(JsonNode src, String keyA, String keyB) {
        if(src == null || src.isNull()) {
            return null;
        }
        JsonNode atKeyA = src.get(keyA);
        if(atKeyA != null && !atKeyA.isNull()) {
            return atKeyA;
        }
        JsonNode atKeyB = src.get(keyB);
        if(atKeyB != null && !atKeyB.isNull()) {
            return atKeyB;
        }
        return null;
    }
    
    private static JsonNode handleVariableKeys(JsonNode src, SparqlConstants keyA, SparqlConstants keyB) {
        return handleVariableKeys(src, keyA.getValue(), keyB.getValue());
    }
    
    private static Set<String> handleSetFields(Set<String> coll, JsonNode node){
        if(node == null) {
            return coll;
        } else if(node.isArray()) {
            for(JsonNode innerNode : node) {
                coll.add(parseValue(innerNode));
            }
        } else if(node != null && !node.isNull()) {
            coll.add(parseValue(node));
        }
        return coll;
    }
    
    private static Set<InteractionComponent> handleInteractionComponents(Set<InteractionComponent> coll, JsonNode node) {
        if(node == null || node.isNull()) {
            return coll;
        } else if(node.isArray()) {
            for(JsonNode innerNode : node) {
                coll.add(new InteractionComponent(innerNode));
            }
        }
        return coll;
    }
    
    private static JsonNode findValue(JsonNode data, SparqlConstants key) {
        if(data == null || data.isNull()) {
            return null;
        }
        return data.get(key.getValue());
    }
    
    // Constructor
    public SparqlResult(JsonNode data) {
        // inScheme
        this.inScheme = parseValue(findValue(data, SparqlConstants.INSCHEME));
        // prefLabel
        JsonNode prefLabelNode = findValue(data, SparqlConstants.PREFLABEL);
        this.prefLabel = parseValue(prefLabelNode);
        this.prefLabelLang = parseLang(prefLabelNode);
        // definition
        JsonNode definitionNode = findValue(data, SparqlConstants.DEFINITION);
        this.definition = parseValue(definitionNode);
        this.definitionLang = parseLang(definitionNode);
        // deprecated
        this.deprecated = parseValue(findValue(data, SparqlConstants.DEPRECATED));
        // type
        this.type = parseValue(findValue(data, SparqlConstants.TYPE));
        // context
        this.context = parseValue(findValue(data, SparqlConstants.CONTEXT));
        // schema
        this.schema = parseValue(findValue(data, SparqlConstants.SCHEMA));
        // inlineSchema
        this.inlineSchema = parseValue(findValue(data, SparqlConstants.INLINE_SCHEMA));
        // contentType
        this.contentType = parseValue(findValue(data, SparqlConstants.CONTENT_TYPE));
        // verb
        this.verb = parseValue(findValue(data, SparqlConstants.VERB));
        // objectActivityType
        this.objectActivityType = parseValue(findValue(data, SparqlConstants.OBJECT_ACTIVITY_TYPE));
        // Activity Definition
        JsonNode activityDefinitionNode = findValue(data, SparqlConstants.ACTIVITY_DEFINITION);
        if(activityDefinitionNode != null && !activityDefinitionNode.isNull()) {
            // -> Activity Type
            this.activityType = parseValue(handleVariableKeys(activityDefinitionNode,
                    SparqlConstants.TYPE,
                    SparqlConstants.ACTIVITY_TYPE));
            // -> Activity Name + Lang
            JsonNode activityNameNode = handleVariableKeys(activityDefinitionNode,
                    SparqlConstants.NAME,
                    SparqlConstants.ACTIVITY_NAME);
            this.activityName = parseValue(activityNameNode);
            this.activityNameLang = parseLang(activityNameNode);
            // -> Activity Description + lang
            JsonNode activityDescriptionNode = handleVariableKeys(activityDefinitionNode,
                    SparqlConstants.DESCRIPTION,
                    SparqlConstants.ACTIVITY_DESCRIPTION);
            this.activityDescription = parseValue(activityDescriptionNode);
            this.activityDescriptionLang = parseLang(activityDescriptionNode);
            // -> Activity moreInfo
            this.activityMoreInfo = parseValue(handleVariableKeys(activityDefinitionNode,
                    SparqlConstants.MORE_INFO,
                    SparqlConstants.ACTIVITY_MORE_INFO));
            // -> Activity Interaction Type
            this.activityInteractionType = parseValue(handleVariableKeys(activityDefinitionNode,
                    SparqlConstants.INTERACTION_TYPE,
                    SparqlConstants.ACTIVITY_INTERACTION_TYPE));
            // -> Activity Extensions
            JsonNode activityExtensionsNode = handleVariableKeys(activityDefinitionNode,
                    SparqlConstants.EXTENSIONS,
                    SparqlConstants.ACTIVITY_EXTENSIONS);
            Map<String, String> existingActivityExtensions = getActivityExtensions();
            if(activityExtensionsNode != null && !activityExtensionsNode.isNull()) {
                Iterator<String> iter = activityExtensionsNode.fieldNames();
                while(iter.hasNext()) {
                    String key = iter.next();
                    existingActivityExtensions.put(key, activityExtensionsNode.get(key).asText());
                }
            }
            // -> Activity Correct Response Pattern
            setActivityCorrectResponsePattern(handleSetFields(
                    getActivityCorrectResponsePattern(), 
                    handleVariableKeys(activityDefinitionNode,
                            SparqlConstants.CORRECT_RESPONSE_PATTERN, 
                            SparqlConstants.ACTIVITY_CORRECT_RESPONSE_PATTERN)));
            // -> Activity Choices
            setActivityChoices(handleInteractionComponents(
                    getActivityChoices(), 
                    handleVariableKeys(activityDefinitionNode,
                            SparqlConstants.CHOICES,
                            SparqlConstants.ACTIVITY_CHOICES)));
            // -> Activity Scale
            setActivityScale(handleInteractionComponents(
                    getActivityScale(),
                    handleVariableKeys(activityDefinitionNode,
                            SparqlConstants.SCALE,
                            SparqlConstants.ACTIVITY_SCALE)));
            // -> Activity Source
            setActivitySource(handleInteractionComponents(
                    getActivitySource(), 
                    handleVariableKeys(activityDefinitionNode,
                            SparqlConstants.SOURCE,
                            SparqlConstants.ACTIVITY_SOURCE)));
            // -> Activity Target
            setActivityTarget(handleInteractionComponents(
                    getActivityTarget(), 
                    handleVariableKeys(activityDefinitionNode,
                            SparqlConstants.TARGET,
                            SparqlConstants.ACTIVITY_TARGET)));
            // -> Activity Steps
            setActivitySteps(handleInteractionComponents(
                    getActivitySteps(), 
                    handleVariableKeys(activityDefinitionNode,
                            SparqlConstants.STEPS,
                            SparqlConstants.ACTIVITY_STEPS)));
        }
        // broader
        setBroader(handleSetFields(getBroader(), findValue(data, SparqlConstants.BROADER)));
        // broadMatch
        setBroadMatch(handleSetFields(getBroadMatch(), findValue(data, SparqlConstants.BROAD_MATCH)));
        // narrower
        setNarrower(handleSetFields(getNarrower(), findValue(data, SparqlConstants.NARROWER)));
        // narrowMatch
        setNarrowMatch(handleSetFields(getNarrowMatch(), findValue(data, SparqlConstants.NARROW_MATCH)));
        // related
        setRelated(handleSetFields(getRelated(), findValue(data, SparqlConstants.RELATED)));
        // relatedMatch
        setRelatedMatch(handleSetFields(getRelatedMatch(), findValue(data, SparqlConstants.RELATED_MATCH)));
        // exactMatch
        setExactMatch(handleSetFields(getExactMatch(), findValue(data, SparqlConstants.EXACT_MATCH)));
        // recommendedActivityTypes
        setRecommendedActivityTypes(handleSetFields(getRecommendedActivityTypes(), findValue(data, SparqlConstants.RECOMMENDED_ACTIVITY_TYPES)));
        // recommendedVerbs
        setRecommendedVerbs(handleSetFields(getRecommendedVerbs(), findValue(data, SparqlConstants.RECOMMENDED_VERBS)));
        // contextGroupingActivityType
        setContextGroupingActivityType(handleSetFields(getContextGroupingActivityType(), findValue(data, SparqlConstants.CONTEXT_GROUPING_ACTIVITY_TYPE)));
        // contextParentActivityType
        setContextParentActivityType(handleSetFields(getContextParentActivityType(), findValue(data, SparqlConstants.CONTEXT_PARENT_ACTIVITY_TYPE)));
        // contextOtherActivityType
        setContextOtherActivityType(handleSetFields(getContextOtherActivityType(), findValue(data, SparqlConstants.CONTEXT_OTHER_ACTIVITY_TYPE)));
        // contextCategoryActivityType
        setContextCategoryActivityType(handleSetFields(getContextCategoryActivityType(), findValue(data, SparqlConstants.CONTEXT_CATEGORY_ACTIVITY_TYPE)));
        // attachmentUsageType
        setAttachmentUsageType(handleSetFields(getAttachmentUsageType(), findValue(data, SparqlConstants.ATTACHMENT_USAGE_TYPE)));
        // objectStatementRefTemplate
        setObjectStatementRefTemplate(handleSetFields(getObjectStatementRefTemplate(), findValue(data, SparqlConstants.OBJECT_STATEMENT_REF_TEMPLATE)));
        // contextStatementRefTemplate
        setContextStatementRefTemplate(handleSetFields(getContextStatementRefTemplate(), findValue(data, SparqlConstants.CONTEXT_STATEMENT_REF_TEMPLATE)));
        // primary
        this.primary = parseValue(findValue(data, SparqlConstants.PRIMARY));
        // alternates
        setAlternates(handleSetFields(getAlternates(), findValue(data, SparqlConstants.ALTERNATES)));
        // optional
        this.optional = parseValue(findValue(data, SparqlConstants.OPTIONAL));
        // oneOrMore
        this.oneOrMore = parseValue(findValue(data, SparqlConstants.ONE_OR_MORE));
        // sequence
        setSequence(handleSetFields(getSequence(), findValue(data, SparqlConstants.SEQUENCE)));
        // zeroOrMore
        this.zeroOrMore = parseValue(findValue(data, SparqlConstants.ZERO_OR_MORE));
    }
    
    // Merging of SparqlResult objects
    
    public static <T> Set<T> combine(Set<T> existing, Set<T> novel) {
        existing = existing != null ? existing : new HashSet<T>();
        if(novel == null) {
            return existing;
        }
        for(T item : novel) {
            existing.add(item);
        }
        return existing;
    }
    
    public static Map<String, String> combine(Map<String, String> existing, Map<String, String> novel) {
        existing = existing != null ? existing : new HashMap<String, String>();
        if(novel == null) {
            return existing;
        }
        existing.putAll(novel);
        return existing;
    }
    
    public static boolean canMerge(SparqlResult a, SparqlResult b) {
        // SparqlResults fields that must match
        if(a.getInScheme() != null && b.getInScheme() != null && !a.getInScheme().equals(b.getInScheme())) {
            return false;
        }
        if(a.getPrefLabel() != null && b.getPrefLabel() != null && !a.getPrefLabel().equals(b.getPrefLabel())) {
            return false;
        }
        if(a.getDefinition() != null && b.getDefinition() != null && !a.getDefinition().equals(b.getDefinition())) {
            return false;
        }
        if(a.getDeprecated() != null && b.getDeprecated() != null && !a.getDeprecated().equals(b.getDeprecated())) {
            return false;
        }
        if(a.getType() != null && b.getType() != null && !a.getType().equals(b.getType())) {
            return false;
        }
        if(a.getContext() != null && b.getContext() != null && !a.getContext().equals(b.getContext())) {
            return false;
        }
        if(a.getSchema() != null && b.getSchema() != null && !a.getSchema().equals(b.getSchema())) {
            return false;
        }
        if(a.getInlineSchema() != null && b.getInlineSchema() != null && !a.getInlineSchema().equals(b.getInlineSchema())) {
            return false;
        }
        if(a.getContentType() != null && b.getContentType() != null && !a.getContentType().equals(b.getContentType())) {
            return false;
        }
        if(a.getVerb() != null && b.getVerb() != null && !a.getVerb().equals(b.getVerb())) {
            return false;
        }
        if(a.getObjectActivityType() != null && b.getObjectActivityType() != null && !a.getObjectActivityType().equals(b.getObjectActivityType())) {
            return false;
        }
        if(a.getActivityType() != null && b.getActivityType() != null && !a.getActivityType().equals(b.getActivityType())) {
            return false;
        }
        if(a.getActivityName() != null && b.getActivityName() != null && !a.getActivityName().equals(b.getActivityName())) {
            return false;
        }
        if(a.getActivityNameLang() != null && b.getActivityNameLang() != null && !a.getActivityNameLang().equals(b.getActivityNameLang())) {
            return false;
        }
        if(a.getActivityDescription() != null && b.getActivityDescription() != null && !a.getActivityDescription().equals(b.getActivityDescription())) {
            return false;
        }
        if(a.getActivityDescriptionLang() != null && b.getActivityDescriptionLang() != null && !a.getActivityDescriptionLang().equals(b.getActivityDescriptionLang())) {
            return false;
        }
        if(a.getActivityMoreInfo() != null && b.getActivityMoreInfo() != null && !a.getActivityMoreInfo().equals(b.getActivityMoreInfo())) {
            return false;
        }
        if(a.getActivityInteractionType() != null && b.getActivityInteractionType() != null && !a.getActivityInteractionType().equals(b.getActivityInteractionType())) {
            return false;
        }
        if(a.getPrimary() != null && b.getPrimary() != null && !a.getPrimary().equals(b.getPrimary())) {
            return false;
        }
        if(a.getOptional() != null && b.getOptional() != null && !a.getOptional().equals(b.getOptional())) {
            return false;
        }
        if(a.getOneOrMore() != null && b.getOneOrMore() != null && !a.getOneOrMore().equals(b.getOneOrMore())) {
            return false;
        }
        if(a.getZeroOrMore() != null && b.getZeroOrMore() != null && !a.getZeroOrMore().equals(b.getZeroOrMore())) {
            return false;
        }
        return true;
    }
    
    public static SparqlResult merge(SparqlResult a, SparqlResult b) {
        if(canMerge(a, b)) {
            // Mergeable fields
            // -> broader
            a.setBroader(combine(a.getBroader(), b.getBroader()));
            // -> broadMatch
            a.setBroadMatch(combine(a.getBroadMatch(), b.getBroadMatch()));
            // -> narrower
            a.setNarrower(combine(a.getNarrower(), b.getNarrower()));
            // -> narrowMatch
            a.setNarrowMatch(combine(a.getNarrowMatch(), b.getNarrowMatch()));
            // -> related
            a.setRelated(combine(a.getRelated(), b.getRelated()));
            // -> relatedMatch
            a.setRelatedMatch(combine(a.getRelatedMatch(), b.getRelatedMatch()));
            // -> exactMatch
            a.setExactMatch(combine(a.getExactMatch(), b.getExactMatch()));
            // -> recommendedActivityTypes
            a.setRecommendedActivityTypes(combine(a.getRecommendedActivityTypes(), b.getRecommendedActivityTypes()));
            // -> recommendedVerbs
            a.setRecommendedVerbs(combine(a.getRecommendedVerbs(), b.getRecommendedVerbs()));
            // -> contextGroupingActivityType
            a.setContextGroupingActivityType(combine(a.getContextGroupingActivityType(), b.getContextGroupingActivityType()));
            // -> contextParentActivityType
            a.setContextParentActivityType(combine(a.getContextParentActivityType(), b.getContextParentActivityType()));
            // -> contextOtherActivityType
            a.setContextOtherActivityType(combine(a.getContextOtherActivityType(), b.getContextOtherActivityType()));
            // -> contextCategoryActivityType
            a.setContextCategoryActivityType(combine(a.getContextCategoryActivityType(), b.getContextCategoryActivityType()));
            // -> attachmentUsageType
            a.setAttachmentUsageType(combine(a.getAttachmentUsageType(), b.getAttachmentUsageType()));
            // -> objectStatementRefTemplate
            a.setObjectStatementRefTemplate(combine(a.getObjectStatementRefTemplate(), b.getObjectStatementRefTemplate()));
            // -> contextStatementRefTemplate
            a.setContextStatementRefTemplate(combine(a.getContextStatementRefTemplate(), b.getContextStatementRefTemplate()));
            // -> activityExtensions
            a.setActivityExtensions(combine(a.getActivityExtensions(), b.getActivityExtensions()));
            // -> activityCorrectResponsePattern
            a.setActivityCorrectResponsePattern(combine(a.getActivityCorrectResponsePattern(), b.getActivityCorrectResponsePattern()));
            // -> activityChoices
            a.setActivityChoices(combine(a.getActivityChoices(), b.getActivityChoices()));
            // -> activityScale
            a.setActivityScale(combine(a.getActivityScale(), b.getActivityScale()));
            // -> activitySource
            a.setActivitySource(combine(a.getActivitySource(), b.getActivitySource()));
            // -> activityTarget
            a.setActivityTarget(combine(a.getActivityTarget(), b.getActivityTarget()));
            // -> activitySteps
            a.setActivitySteps(combine(a.getActivitySteps(), b.getActivitySteps()));
            // -> alternates
            a.setAlternates(combine(a.getAlternates(), b.getAlternates()));
            // -> sequence
            a.setSequence(combine(a.getSequence(), b.getSequence()));
        }
        return a;
    }
    
    public void merge(SparqlResult toMerge) {
        merge(this, toMerge);
    }
    
}
