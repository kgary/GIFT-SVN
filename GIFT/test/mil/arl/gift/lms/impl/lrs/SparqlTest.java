package mil.arl.gift.lms.impl.lrs;

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQueryString;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlQuery;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.LocalClient;

public class SparqlTest {

    private static final String EXAMPLE_URI = "https://example.com/uri";
    private static final String SPARQL_RESPONSE = 
            "[{\"inScheme\":{\"token\":\"uri\",\"value\":\"https://w3id.org/xapi/acrossx/v/2\"},"
            + "\"prefLabel\":{\"token\":\"literal\",\"value\":\"video\",\"lang\":\"en\"},"
            + "\"definition\":{\"token\":\"literal\",\"value\":\"A recording of audible and visual content intended to be shown on a video display.\",\"lang\":\"en\"},"
            + "\"deprecated\":null,"
            + "\"type\":{\"token\":\"uri\",\"value\":\"https://w3id.org/xapi/ontology#ActivityType\"},"
            + "\"broader\":null,"
            + "\"broadMatch\":null,"
            + "\"narrower\":null,"
            + "\"narrowMatch\":null,"
            + "\"related\":null,"
            + "\"relatedMatch\":null,"
            + "\"exactMatch\":{\"token\":\"uri\",\"value\":\"https://w3id.org/xapi/video/activity-type/video\"}},"
            + "{\"inScheme\":{\"token\":\"uri\",\"value\":\"https://w3id.org/xapi/acrossx/v/2\"},"
            + "\"prefLabel\":{\"token\":\"literal\",\"value\":\"video\",\"lang\":\"en\"},"
            + "\"definition\":{\"token\":\"literal\",\"value\":\"A recording of audible and visual content intended to be shown on a video display.\",\"lang\":\"en\"},"
            + "\"deprecated\":null,"
            + "\"type\":{\"token\":\"uri\",\"value\":\"https://w3id.org/xapi/ontology#ActivityType\"},"
            + "\"broader\":null,"
            + "\"broadMatch\":null,"
            + "\"narrower\":null,"
            + "\"narrowMatch\":null,"
            + "\"related\":null,"
            + "\"relatedMatch\":null,"
            + "\"exactMatch\":{\"token\":\"uri\",\"value\":\"http://activitystrea.ms/schema/1.0/video\"}}]";
    
    @Test
    public void testQueryCreation() throws Exception {
        Assert.assertEquals(SparqlQueryString.prefixes, SparqlQueryString.withPrefixes().toString());
        Assert.assertEquals("SELECT ?foo ?baz \n", SparqlQueryString.withSelectVars(new StringBuilder(), "?foo", "?baz").toString());
        Assert.assertEquals("WHERE { \n", SparqlQueryString.withWhereClause(new StringBuilder()).toString());
        Assert.assertEquals("OPTIONAL { ?subject ?predicate ?object } . \n", 
                SparqlQueryString.appendOptionalStatement(new StringBuilder(), "?subject", "?predicate", "?object").toString());
        Assert.assertEquals("?subject ?predicate ?object . \n",
                SparqlQueryString.appendStatement(new StringBuilder(), "?subject", "?predicate", "?object").toString());
        Assert.assertEquals("<https://example.com/uri> skos:inScheme ?inScheme . \n",
                SparqlQueryString.appendInscheme(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("<https://example.com/uri> skos:prefLabel ?prefLabel . \n",
                SparqlQueryString.appendPreflabel(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("<https://example.com/uri> skos:definition ?definition . \n", 
                SparqlQueryString.appendDefinition(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("<https://example.com/uri> a ?type . \n",
                SparqlQueryString.appendType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("<https://example.com/uri> profile:contentType ?contentType . \n", 
                SparqlQueryString.appendContentType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n",
                SparqlQueryString.appendOptionalDeprecated(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:broader ?broader } . \n",
                SparqlQueryString.appendOptionalBroader(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:broadMatch ?broadMatch } . \n",
                SparqlQueryString.appendOptionalBroadMatch(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:narrower ?narrower } . \n",
                SparqlQueryString.appendOptionalNarrower(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:narrowMatch ?narrowMatch } . \n",
                SparqlQueryString.appendOptionalNarrowMatch(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:related ?related } . \n",
                SparqlQueryString.appendOptionalRelated(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:relatedMatch ?relatedMatch } . \n",
                SparqlQueryString.appendOptionalRelatedMatch(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:exactMatch ?exactMatch } . \n",
                SparqlQueryString.appendOptionalExactMatch(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:recommendedActivityTypes ?recommendedActivityTypes } . \n",
                SparqlQueryString.appendOptionalRecommendedActivityTypes(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:recommendedVerbs ?recommendedVerbs } . \n",
                SparqlQueryString.appendOptionalRecommendedVerbs(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:context ?context } . \n",
                SparqlQueryString.appendOptionalContext(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:schema ?schema } . \n",
                SparqlQueryString.appendOptionalSchema(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:inlineSchema ?inlineSchema } . \n",
                SparqlQueryString.appendOptionalInlineSchema(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:verb ?verb } . \n",
                SparqlQueryString.appendOptionalVerb(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:objectActivityType ?objectActivityType } . \n",
                SparqlQueryString.appendOptionalObjectActivityType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:contextGroupingActivityType ?contextGroupingActivityType } . \n",
                SparqlQueryString.appendOptionalContextGroupingActivityType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:contextParentActivityType ?contextParentActivityType } . \n",
                SparqlQueryString.appendOptionalContextParentActivityType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:contextOtherActivityType ?contextOtherActivityType } . \n",
                SparqlQueryString.appendOptionalContextOtherActivityType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:contextCategoryActivityType ?contextCategoryActivityType } . \n",
                SparqlQueryString.appendOptionalContextCategoryActivityType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:attachmentUsageType ?attachmentUsageType } . \n",
                SparqlQueryString.appendOptionalAttachmentUsageType(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:objectStatementRefTemplate ?objectStatementRefTemplate } . \n",
                SparqlQueryString.appendOptionalObjectStatementRefTemplate(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:contextStatementRefTemplate ?contextStatementRefTemplate } . \n",
                SparqlQueryString.appendOptionalContextStatementRefTemplate(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("<https://example.com/uri> profile:activityDefinition ?activityDefinition . \n"
                + "OPTIONAL { ?activityDefinition xapi:description ?activityDescription } . \n"
                + "OPTIONAL { ?activityDefinition xapi:name ?activityName } . \n"
                + "OPTIONAL { ?activityDefinition xapi:type ?activityType } . \n"
                + "OPTIONAL { ?activityDefinition xapi:moreInfo ?activityMoreInfo } . \n"
                + "OPTIONAL { ?activityDefinition xapi:extensions ?activityExtensions } . \n"
                + "OPTIONAL { ?activityDefinition xapi:interactionType ?activityInteractionType } . \n"
                + "OPTIONAL { ?activityDefinition xapi:correctResponsesPattern ?activityCorrectResponsePattern } . \n"
                + "OPTIONAL { ?activityDefinition xapi:choices ?activityChoices } . \n"
                + "OPTIONAL { ?activityDefinition xapi:scale ?activityScale } . \n"
                + "OPTIONAL { ?activityDefinition xapi:source ?activitySource } . \n"
                + "OPTIONAL { ?activityDefinition xapi:target ?activityTarget } . \n"
                + "OPTIONAL { ?activityDefinition xapi:steps ?activitySteps } . \n",
                SparqlQueryString.appendActivityDefinition(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:inScheme ?inScheme } . \n", 
                SparqlQueryString.appendOptionalInScheme(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:prefLabel ?prefLabel } . \n", 
                SparqlQueryString.appendOptionalPrefLabel(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> skos:definition ?definition } . \n", 
                SparqlQueryString.appendOptionalDefinition(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:primary ?primary } . \n", 
                SparqlQueryString.appendOptionalPrimaryPattern(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:alternates ?alternates } . \n", 
                SparqlQueryString.appendOptionalPatternAlternates(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:optional ?optional } . \n", 
                SparqlQueryString.appendOptionalPatternOptional(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:oneOrMore ?oneOrMore } . \n", 
                SparqlQueryString.appendOptionalPatternOneOrMore(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:sequence ?sequence } . \n", 
                SparqlQueryString.appendOptionalPatternSequence(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("OPTIONAL { <https://example.com/uri> profile:zeroOrMore ?zeroOrMore } . \n",
                SparqlQueryString.appendOptionalPatternZeroOrMore(new StringBuilder(), EXAMPLE_URI).toString());
        Assert.assertEquals("}", SparqlQueryString.createQuery(new StringBuilder()));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?inScheme ?prefLabel ?definition ?deprecated ?type ?broader ?broadMatch ?narrower ?narrowMatch ?related ?relatedMatch ?exactMatch \n"
                + "WHERE { \n"
                + "<https://example.com/uri> skos:inScheme ?inScheme . \n"
                + "<https://example.com/uri> skos:prefLabel ?prefLabel . \n"
                + "<https://example.com/uri> skos:definition ?definition . \n"
                + "<https://example.com/uri> a ?type . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:broader ?broader } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:broadMatch ?broadMatch } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:narrower ?narrower } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:narrowMatch ?narrowMatch } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:related ?related } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:relatedMatch ?relatedMatch } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:exactMatch ?exactMatch } . \n"
                + "}", 
                SparqlQueryString.createConceptRelationQuery(EXAMPLE_URI));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?inScheme ?prefLabel ?definition ?deprecated ?type ?recommendedActivityTypes ?context ?schema ?inlineSchema \n"
                + "WHERE { \n"
                + "<https://example.com/uri> skos:inScheme ?inScheme . \n"
                + "<https://example.com/uri> skos:prefLabel ?prefLabel . \n"
                + "<https://example.com/uri> skos:definition ?definition . \n"
                + "<https://example.com/uri> a ?type . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:recommendedActivityTypes ?recommendedActivityTypes } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:context ?context } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:schema ?schema } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:inlineSchema ?inlineSchema } . \n"
                + "}",
                SparqlQueryString.createActivityExtensionQuery(EXAMPLE_URI));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?inScheme ?prefLabel ?definition ?deprecated ?type ?recommendedVerbs ?context ?schema ?inlineSchema \n"
                + "WHERE { \n"
                + "<https://example.com/uri> skos:inScheme ?inScheme . \n"
                + "<https://example.com/uri> skos:prefLabel ?prefLabel . \n"
                + "<https://example.com/uri> skos:definition ?definition . \n"
                + "<https://example.com/uri> a ?type . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:recommendedVerbs ?recommendedVerbs } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:context ?context } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:schema ?schema } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:inlineSchema ?inlineSchema } . \n"
                + "}",
                SparqlQueryString.createStatementExtensionQuery(EXAMPLE_URI));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?inScheme ?prefLabel ?definition ?type ?contentType ?deprecated ?context ?schema ?inlineSchema \n"
                + "WHERE { \n"
                + "<https://example.com/uri> skos:inScheme ?inScheme . \n"
                + "<https://example.com/uri> skos:prefLabel ?prefLabel . \n"
                + "<https://example.com/uri> skos:definition ?definition . \n"
                + "<https://example.com/uri> a ?type . \n"
                + "<https://example.com/uri> profile:contentType ?contentType . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:context ?context } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:schema ?schema } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:inlineSchema ?inlineSchema } . \n"
                + "}",
                SparqlQueryString.createDocumentResourceQuery(EXAMPLE_URI));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?inScheme ?prefLabel ?definition ?type ?deprecated ?verb ?objectActivityType "
                + "?contextGroupingActivityType ?contextParentActivityType ?contextOtherActivityType "
                + "?contextCategoryActivityType ?attachmentUsageType ?objectStatementRefTemplate ?contextStatementRefTemplate \n"
                + "WHERE { \n"
                + "<https://example.com/uri> skos:inScheme ?inScheme . \n"
                + "<https://example.com/uri> skos:prefLabel ?prefLabel . \n"
                + "<https://example.com/uri> skos:definition ?definition . \n"
                + "<https://example.com/uri> a ?type . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:verb ?verb } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:objectActivityType ?objectActivityType } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:contextGroupingActivityType ?contextGroupingActivityType } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:contextParentActivityType ?contextParentActivityType } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:contextOtherActivityType ?contextOtherActivityType } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:contextCategoryActivityType ?contextCategoryActivityType } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:attachmentUsageType ?attachmentUsageType } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:objectStatementRefTemplate ?objectStatementRefTemplate } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:contextStatementRefTemplate ?contextStatementRefTemplate } . \n"
                + "}",
                SparqlQueryString.createStatementTemplateQuery(EXAMPLE_URI));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?inScheme ?type ?deprecated ?activityDescription ?activityName ?activityType ?activityMoreInfo "
                + "?activityExtensions ?activityInteractionType ?activityCorrectResponsePattern ?activityChoices "
                + "?activityScale ?activitySource ?activityTarget ?activitySteps \n"
                + "WHERE { \n"
                + "<https://example.com/uri> skos:inScheme ?inScheme . \n"
                + "<https://example.com/uri> a ?type . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "<https://example.com/uri> profile:activityDefinition ?activityDefinition . \n"
                + "OPTIONAL { ?activityDefinition xapi:description ?activityDescription } . \n"
                + "OPTIONAL { ?activityDefinition xapi:name ?activityName } . \n"
                + "OPTIONAL { ?activityDefinition xapi:type ?activityType } . \n"
                + "OPTIONAL { ?activityDefinition xapi:moreInfo ?activityMoreInfo } . \n"
                + "OPTIONAL { ?activityDefinition xapi:extensions ?activityExtensions } . \n"
                + "OPTIONAL { ?activityDefinition xapi:interactionType ?activityInteractionType } . \n"
                + "OPTIONAL { ?activityDefinition xapi:correctResponsesPattern ?activityCorrectResponsePattern } . \n"
                + "OPTIONAL { ?activityDefinition xapi:choices ?activityChoices } . \n"
                + "OPTIONAL { ?activityDefinition xapi:scale ?activityScale } . \n"
                + "OPTIONAL { ?activityDefinition xapi:source ?activitySource } . \n"
                + "OPTIONAL { ?activityDefinition xapi:target ?activityTarget } . \n"
                + "OPTIONAL { ?activityDefinition xapi:steps ?activitySteps } . \n"
                + "}", 
                SparqlQueryString.createActivityConceptQuery(EXAMPLE_URI));
        Assert.assertEquals(
                SparqlQueryString.prefixes+
                "SELECT ?type ?primary ?inScheme ?prefLabel ?definition ?deprecated ?alternates ?optional ?oneOrMore ?sequence ?zeroOrMore \n"
                + "WHERE { \n"
                + "<https://example.com/uri> a ?type . \n"
                + "OPTIONAL { <https://example.com/uri> profile:primary ?primary } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:inScheme ?inScheme } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:prefLabel ?prefLabel } . \n"
                + "OPTIONAL { <https://example.com/uri> skos:definition ?definition } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:deprecated ?deprecated } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:alternates ?alternates } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:optional ?optional } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:oneOrMore ?oneOrMore } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:sequence ?sequence } . \n"
                + "OPTIONAL { <https://example.com/uri> profile:zeroOrMore ?zeroOrMore } . \n"
                + "}",
                SparqlQueryString.createPatternQuery(EXAMPLE_URI));
    }
    
    @Test
    public void testQueryParsing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(SPARQL_RESPONSE);
        SparqlResult parsed = SparqlQuery.parseQuery(data);
        Set<String> expectedExactMatch = new HashSet<String>();
        expectedExactMatch.add("http://activitystrea.ms/schema/1.0/video");
        expectedExactMatch.add("https://w3id.org/xapi/video/activity-type/video");
        Assert.assertEquals(expectedExactMatch, parsed.getExactMatch());
        Assert.assertEquals("https://w3id.org/xapi/acrossx/v/2", parsed.getInScheme());
        Assert.assertEquals("video", parsed.getPrefLabel());
        Assert.assertEquals("en", parsed.getPrefLabelLang());
        Assert.assertEquals("https://w3id.org/xapi/ontology#ActivityType", parsed.getType());
        Assert.assertNull(parsed.getDeprecated());
        Set<String> emptySet = new HashSet<String>();
        Assert.assertEquals(emptySet, parsed.getBroader());
        Assert.assertEquals(emptySet, parsed.getBroadMatch());
        Assert.assertEquals(emptySet, parsed.getNarrower());
        Assert.assertEquals(emptySet, parsed.getNarrowMatch());
        Assert.assertEquals(emptySet, parsed.getRelated());
        Assert.assertEquals(emptySet, parsed.getRelatedMatch());
    }
    
    @Test
    public void testLocalClient() throws Exception {
        LocalClient client = new LocalClient();
        Assert.assertNotNull(client.getConcepts());
        Assert.assertNotNull(client.getTemplates());
        Assert.assertNotNull(client.getPatterns());
        // Verb
        SparqlResult localVerbParsed = client.searchConcepts("http://adlnet.gov/expapi/verbs/attempted");
        Assert.assertEquals("Verb", localVerbParsed.getType());
        Assert.assertEquals("https://w3id.org/xapi/adl/v1.0", localVerbParsed.getInScheme());
        Assert.assertEquals("attempted", localVerbParsed.getPrefLabel());
        Assert.assertEquals("en", localVerbParsed.getPrefLabelLang());
        Assert.assertEquals("Indicates the actor made an effort to access the object. An attempt statement without additional activities could be considered incomplete in some cases.",
                localVerbParsed.getDefinition());
        Assert.assertEquals("en", localVerbParsed.getDefinitionLang());
        // Extensions
        SparqlResult localExtensionParsed = client.searchConcepts("https://w3id.org/xapi/tla/extensions/unit_identification_code");
        Assert.assertEquals("ContextExtension", localExtensionParsed.getType());
        Assert.assertEquals("https://w3id.org/xapi/tla/v/4", localExtensionParsed.getInScheme());
        Assert.assertEquals("unit identification code", localExtensionParsed.getPrefLabel());
        Assert.assertEquals("en", localExtensionParsed.getPrefLabelLang());
        Assert.assertEquals("A unique code for the learner's unit", localExtensionParsed.getDefinition());
        Assert.assertEquals("en", localExtensionParsed.getDefinitionLang());
        Set<String> expectedRecVerbs = new HashSet<String>();
        expectedRecVerbs.add("https://w3id.org/xapi/tla/verbs/detailed");
        expectedRecVerbs.add("https://w3id.org/xapi/tla/verbs/mobilized");
        expectedRecVerbs.add("https://w3id.org/xapi/tla/verbs/employed");
        expectedRecVerbs.add("https://w3id.org/xapi/tla/verbs/schooled");
        Assert.assertEquals(expectedRecVerbs, localExtensionParsed.getRecommendedVerbs());
        // Activity
        SparqlResult localActivityParsed = client.searchConcepts("activityId:uri/its/learner.state.affective");
        Assert.assertEquals("Activity", localActivityParsed.getType());
        Assert.assertEquals("https://xapinet.org/xapi/stetmt/its/v0.0.1", localActivityParsed.getInScheme());
        Assert.assertEquals("https://xapinet.org/xapi/stetmt/its/ActivityType#learner.state.affective", localActivityParsed.getActivityType());
        Assert.assertEquals("Affective Learner State", localActivityParsed.getActivityName());
        Assert.assertEquals("en", localActivityParsed.getActivityNameLang());
        
        // TODO: fixture with Interaction Components
        
        
        // Activity Type
        SparqlResult localActivityTypeParsed = client.searchConcepts("https://xapinet.org/xapi/stetmt/its/ActivityType#course.record");
        Assert.assertEquals("ActivityType", localActivityTypeParsed.getType());
        Assert.assertEquals("https://xapinet.org/xapi/stetmt/its/v0.0.1", localActivityTypeParsed.getInScheme());
        Assert.assertEquals("Course Record", localActivityTypeParsed.getPrefLabel());
        Assert.assertEquals("en", localActivityTypeParsed.getPrefLabelLang());
        Assert.assertEquals("Activity represents a grouping of assessment results calculated at the end of a DKF instance based on the Actors performance during the instance.",
                localActivityTypeParsed.getDefinition());
        Assert.assertEquals("en", localActivityTypeParsed.getDefinitionLang());
        // Statement Template
        SparqlResult localTemplateParsed = client.searchTemplates("https://w3id.org/xapi/tla#launched");
        Assert.assertEquals("StatementTemplate", localTemplateParsed.getType());
        Assert.assertEquals("https://w3id.org/xapi/tla/v/4", localTemplateParsed.getInScheme());
        Assert.assertEquals("launched", localTemplateParsed.getPrefLabel());
        Assert.assertEquals("en", localTemplateParsed.getPrefLabelLang());
        Assert.assertEquals("Indicates the actor attempted to start up an activity", localTemplateParsed.getDefinition());
        Assert.assertEquals("en", localTemplateParsed.getDefinitionLang());
        Assert.assertEquals("true", localTemplateParsed.getDeprecated());
        Assert.assertEquals("https://adlnet.gov/expapi/verbs/launched", localTemplateParsed.getVerb());
        SparqlResult voidTemplateParsed = client.searchTemplates("https://xapinet.org/xapi/stetmt/its/StatementTemplate#voided.summative.assessment");
        Assert.assertEquals("StatementTemplate", voidTemplateParsed.getType());
        Assert.assertEquals("https://xapinet.org/xapi/stetmt/its/v0.0.1", voidTemplateParsed.getInScheme());
        Assert.assertEquals("Void Summative Assessment", voidTemplateParsed.getPrefLabel());
        Assert.assertEquals("en", voidTemplateParsed.getPrefLabelLang());
        Assert.assertEquals("Matches against voiding statements that target an Individual or Team Summative Assessment statement", 
                voidTemplateParsed.getDefinition());
        Assert.assertEquals("en", voidTemplateParsed.getDefinitionLang());
        Assert.assertEquals("http://adlnet.gov/expapi/verbs/voided", voidTemplateParsed.getVerb());
        Set<String> expectedObjRefTemplates = new HashSet<String>();
        expectedObjRefTemplates.add("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment");
        expectedObjRefTemplates.add("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.team");
        expectedObjRefTemplates.add("https://xapinet.org/xapi/stetmt/its/StatementTemplate#summative.assessment.individual");
        Assert.assertEquals(expectedObjRefTemplates, voidTemplateParsed.getObjectStatementRefTemplate());
        // Pattern
        SparqlResult localPatternParsed = client.searchPatterns("https://w3id.org/xapi/tla#completed_session");
        Assert.assertEquals("Pattern", localPatternParsed.getType());
        Assert.assertEquals("true", localPatternParsed.getPrimary());
        Assert.assertEquals("https://w3id.org/xapi/tla/v/4", localPatternParsed.getInScheme());
        Assert.assertEquals("completed session", localPatternParsed.getPrefLabel());
        Assert.assertEquals("en", localPatternParsed.getPrefLabelLang());
        Assert.assertEquals("This pattern describes the sequence of statements sent over the an entire course registration.",
                localPatternParsed.getDefinition());
        Assert.assertEquals("en", localPatternParsed.getDefinitionLang());
        Set<String> expectedSeq = new HashSet<String>();
        expectedSeq.add("https://w3id.org/xapi/tla#launched");
        expectedSeq.add("https://w3id.org/xapi/tla#initialized");
        expectedSeq.add("https://w3id.org/xapi/tla#completed");
        expectedSeq.add("https://w3id.org/xapi/tla#terminated");
        Assert.assertEquals(expectedSeq, localPatternParsed.getSequence());
    }
}
