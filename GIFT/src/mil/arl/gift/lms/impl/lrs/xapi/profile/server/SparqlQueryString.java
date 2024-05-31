package mil.arl.gift.lms.impl.lrs.xapi.profile.server;

/**
 * Creation of xAPI Profile SPARQL query strings.
 * 
 * @author Yet Analytics
 *
 */
public class SparqlQueryString {
    
    // Prefixes used across SPARQL queries
    public static final String prefixes =
            "PREFIX prov: <http://www.w3.org/ns/prov#>\n"
            + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX xapi: <https://w3id.org/xapi/ontology#>\n"
            + "PREFIX profile: <https://w3id.org/xapi/profiles/ontology#>\n"
            + "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
            + "PREFIX schemaorg: <http://schema.org/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
    // Variable names used within SPARQL queries
    public static String asVar(SparqlConstants s) {
        return SparqlConstants.VARIABLE_PREFIX.getValue() + s.getValue();
    }
    // -> common
    public static final String INSCHEME_VAR = asVar(SparqlConstants.INSCHEME);
    public static final String PREFLABEL_VAR = asVar(SparqlConstants.PREFLABEL);
    public static final String DEFINITION_VAR = asVar(SparqlConstants.DEFINITION);
    public static final String DEPRECATED_VAR = asVar(SparqlConstants.DEPRECATED);
    public static final String TYPE_VAR = asVar(SparqlConstants.TYPE);
    // -> Verb, Activity Type, Attachment Usage Type
    public static final String BROADER_VAR = asVar(SparqlConstants.BROADER);
    public static final String BROAD_MATCH_VAR = asVar(SparqlConstants.BROAD_MATCH);
    public static final String NARROWER_VAR = asVar(SparqlConstants.NARROWER);
    public static final String NARROW_MATCH_VAR = asVar(SparqlConstants.NARROW_MATCH);
    public static final String RELATED_VAR = asVar(SparqlConstants.RELATED);
    public static final String RELATED_MATCH_VAR = asVar(SparqlConstants.RELATED_MATCH);
    public static final String EXACT_MATCH_VAR = asVar(SparqlConstants.EXACT_MATCH);
    // -> Activity
    public static final String ACTIVITY_DEFINITION_VAR = asVar(SparqlConstants.ACTIVITY_DEFINITION);
    public static final String ACTIVITY_DESCRIPTION_VAR = asVar(SparqlConstants.ACTIVITY_DESCRIPTION);
    public static final String ACTIVITY_NAME_VAR = asVar(SparqlConstants.ACTIVITY_NAME);
    public static final String ACTIVITY_TYPE_VAR = asVar(SparqlConstants.ACTIVITY_TYPE);
    public static final String ACTIVITY_MORE_INFO_VAR = asVar(SparqlConstants.ACTIVITY_MORE_INFO);
    public static final String ACTIVITY_EXTENSIONS_VAR = asVar(SparqlConstants.ACTIVITY_EXTENSIONS);
    public static final String ACTIVITY_INTERACTION_TYPE_VAR = asVar(SparqlConstants.ACTIVITY_INTERACTION_TYPE);
    public static final String ACTIVITY_CORRECT_RESPONSE_PATTERN_VAR = asVar(SparqlConstants.ACTIVITY_CORRECT_RESPONSE_PATTERN);
    public static final String ACTIVITY_CHOICES_VAR = asVar(SparqlConstants.ACTIVITY_CHOICES);
    public static final String ACTIVITY_SCALE_VAR = asVar(SparqlConstants.ACTIVITY_SCALE);
    public static final String ACTIVITY_SOURCE_VAR = asVar(SparqlConstants.ACTIVITY_SOURCE);
    public static final String ACTIVITY_TARGET_VAR = asVar(SparqlConstants.ACTIVITY_TARGET);
    public static final String ACTIVITY_STEPS_VAR = asVar(SparqlConstants.ACTIVITY_STEPS);
    // -> Extensions
    public static final String RECOMMENDED_ACTIVITY_TYPES_VAR = asVar(SparqlConstants.RECOMMENDED_ACTIVITY_TYPES);
    public static final String RECOMMENDED_VERBS_VAR = asVar(SparqlConstants.RECOMMENDED_VERBS);
    public static final String CONTEXT_VAR = asVar(SparqlConstants.CONTEXT);
    public static final String SCHEMA_VAR = asVar(SparqlConstants.SCHEMA);
    public static final String INLINE_SCHEMA_VAR = asVar(SparqlConstants.INLINE_SCHEMA);
    // -> Document Resource
    public static final String CONTENT_TYPE_VAR = asVar(SparqlConstants.CONTENT_TYPE);
    // -> Statement Template
    public static final String VERB_VAR = asVar(SparqlConstants.VERB);
    public static final String OBJECT_ACTIVITY_TYPE_VAR = asVar(SparqlConstants.OBJECT_ACTIVITY_TYPE);
    public static final String CONTEXT_GROUPING_ACTIVITY_TYPE_VAR = asVar(SparqlConstants.CONTEXT_GROUPING_ACTIVITY_TYPE);
    public static final String CONTEXT_PARENT_ACTIVITY_TYPE_VAR = asVar(SparqlConstants.CONTEXT_PARENT_ACTIVITY_TYPE);
    public static final String CONTEXT_OTHER_ACTIVITY_TYPE_VAR = asVar(SparqlConstants.CONTEXT_OTHER_ACTIVITY_TYPE);
    public static final String CONTEXT_CATEGORY_ACTIVITY_TYPE_VAR = asVar(SparqlConstants.CONTEXT_CATEGORY_ACTIVITY_TYPE);
    public static final String ATTACHMENT_USAGE_TYPE_VAR = asVar(SparqlConstants.ATTACHMENT_USAGE_TYPE);
    public static final String OBJECT_STATEMENT_REF_TEMPLATE_VAR = asVar(SparqlConstants.OBJECT_STATEMENT_REF_TEMPLATE);
    public static final String CONTEXT_STATEMENT_REF_TEMPLATE_VAR = asVar(SparqlConstants.CONTEXT_STATEMENT_REF_TEMPLATE);
    // -> Patterns
    public static final String PRIMARY_VAR = asVar(SparqlConstants.PRIMARY);
    public static final String ALTERNATES_VAR = asVar(SparqlConstants.ALTERNATES);
    public static final String OPTIONAL_VAR = asVar(SparqlConstants.OPTIONAL);
    public static final String ONE_OR_MORE_VAR = asVar(SparqlConstants.ONE_OR_MORE);
    public static final String SEQUENCE_VAR = asVar(SparqlConstants.SEQUENCE);
    public static final String ZERO_OR_MORE_VAR = asVar(SparqlConstants.ZERO_OR_MORE);
    
    // Predicates used within SPARQL queries
    public static final String PROFILE_DEPRECATED = SparqlConstants.PROFILE_DEPRECATED.getValue();
    public static final String PROFILE_TYPE = SparqlConstants.PROFILE_TYPE.getValue();
    public static final String PROFILE_CONTEXT = SparqlConstants.PROFILE_CONTEXT.getValue();
    public static final String PROFILE_RECOMMENDED_ACTIVITY_TYPES = SparqlConstants.PROFILE_RECOMMENDED_ACTIVITY_TYPES.getValue();
    public static final String PROFILE_RECOMMENDED_VERBS = SparqlConstants.PROFILE_RECOMMENDED_VERBS.getValue();
    public static final String PROFILE_SCHEMA = SparqlConstants.PROFILE_SCHEMA.getValue();
    public static final String PROFILE_INLINE_SCHEMA = SparqlConstants.PROFILE_INLINE_SCHEMA.getValue();
    public static final String PROFILE_CONTENT_TYPE = SparqlConstants.PROFILE_CONTENT_TYPE.getValue();
    public static final String PROFILE_VERB = SparqlConstants.PROFILE_VERB.getValue();
    public static final String PROFILE_OBJECT_ACTIVITY_TYPE = SparqlConstants.PROFILE_OBJECT_ACTIVITY_TYPE.getValue();
    public static final String PROFILE_CONTEXT_GROUPING_ACTIVITY_TYPE = SparqlConstants.PROFILE_CONTEXT_GROUPING_ACTIVITY_TYPE.getValue();
    public static final String PROFILE_CONTEXT_PARENT_ACTIVITY_TYPE = SparqlConstants.PROFILE_CONTEXT_PARENT_ACTIVITY_TYPE.getValue();
    public static final String PROFILE_CONTEXT_OTHER_ACTIVITY_TYPE = SparqlConstants.PROFILE_CONTEXT_OTHER_ACTIVITY_TYPE.getValue();
    public static final String PROFILE_CONTEXT_CATEGORY_ACTIVITY_TYPE = SparqlConstants.PROFILE_CONTEXT_CATEGORY_ACTIVITY_TYPE.getValue();
    public static final String PROFILE_ATTACHMENT_USAGE_TYPE = SparqlConstants.PROFILE_ATTACHMENT_USAGE_TYPE.getValue();
    public static final String PROFILE_OBJECT_STATEMENT_REF_TEMPLATE = SparqlConstants.PROFILE_OBJECT_STATEMENT_REF_TEMPLATE.getValue();
    public static final String PROFILE_CONTEXT_STATEMENT_REF_TEMPLATE = SparqlConstants.PROFILE_CONTEXT_STATEMENT_REF_TEMPLATE.getValue();
    public static final String PROFILE_ACTIVITY_DEFINITION = SparqlConstants.PROFILE_ACTIVITY_DEFINITION.getValue();
    public static final String PROFILE_PRIMARY = SparqlConstants.PROFILE_PRIMARY.getValue();
    public static final String PROFILE_ALTERNATES = SparqlConstants.PROFILE_ALTERNATES.getValue();
    public static final String PROFILE_OPTIONAL = SparqlConstants.PROFILE_OPTIONAL.getValue();
    public static final String PROFILE_ONE_OR_MORE = SparqlConstants.PROFILE_ONE_OR_MORE.getValue();
    public static final String PROFILE_SEQUENCE = SparqlConstants.PROFILE_SEQUENCE.getValue();
    public static final String PROFILE_ZERO_OR_MORE = SparqlConstants.PROFILE_ZERO_OR_MORE.getValue();
    public static final String XAPI_ACTIVITY_DESCRIPTION = SparqlConstants.XAPI_ACTIVITY_DESCRIPTION.getValue();
    public static final String XAPI_ACTIVITY_NAME = SparqlConstants.XAPI_ACTIVITY_NAME.getValue();
    public static final String XAPI_ACTIVITY_TYPE = SparqlConstants.XAPI_ACTIVITY_TYPE.getValue();
    public static final String XAPI_ACTIVITY_MORE_INFO = SparqlConstants.XAPI_ACTIVITY_MORE_INFO.getValue();
    public static final String XAPI_ACTIVITY_EXTENSIONS = SparqlConstants.XAPI_ACTIVITY_EXTENSIONS.getValue();
    public static final String XAPI_ACTIVITY_INTERACTION_TYPE = SparqlConstants.XAPI_ACTIVITY_INTERACTION_TYPE.getValue();
    public static final String XAPI_ACTIVITY_CORRECT_RESPONSE_PATTERN = SparqlConstants.XAPI_ACTIVITY_CORRECT_RESPONSE_PATTERN.getValue();
    public static final String XAPI_ACTIVITY_CHOICES = SparqlConstants.XAPI_ACTIVITY_CHOICES.getValue();
    public static final String XAPI_ACTIVITY_SCALE = SparqlConstants.XAPI_ACTIVITY_SCALE.getValue();
    public static final String XAPI_ACTIVITY_SOURCE = SparqlConstants.XAPI_ACTIVITY_SOURCE.getValue();
    public static final String XAPI_ACTIVITY_TARGET = SparqlConstants.XAPI_ACTIVITY_TARGET.getValue();
    public static final String XAPI_ACTIVITY_STEPS = SparqlConstants.XAPI_ACTIVITY_STEPS.getValue();
    public static final String SKOS_PREFLABEL = SparqlConstants.SKOS_PREFLABEL.getValue();
    public static final String SKOS_DEFINITION = SparqlConstants.SKOS_DEFINITION.getValue();
    public static final String SKOS_INSCHEME = SparqlConstants.SKOS_INSCHEME.getValue();
    public static final String SKOS_BROADER = SparqlConstants.SKOS_BROADER.getValue();
    public static final String SKOS_BROAD_MATCH = SparqlConstants.SKOS_BROAD_MATCH.getValue();
    public static final String SKOS_NARROWER = SparqlConstants.SKOS_NARROWER.getValue();
    public static final String SKOS_NARROW_MATCH = SparqlConstants.SKOS_NARROW_MATCH.getValue();
    public static final String SKOS_RELATED = SparqlConstants.SKOS_RELATED.getValue();
    public static final String SKOS_RELATED_MATCH = SparqlConstants.SKOS_RELATED_MATCH.getValue();
    public static final String SKOS_EXACT_MATCH = SparqlConstants.SKOS_EXACT_MATCH.getValue();
    
    // common
    public static String wrapId(String id) {
        return "<"+id+">";
    }
    
    // String Builder methods
    public static StringBuilder withPrefixes() {
        return new StringBuilder(prefixes);
    }
    
    public static StringBuilder withSelectVars(StringBuilder builder, String...variables) {
        builder.append("SELECT ");
        for(String var : variables) {
            builder.append(var).append(" ");
        }
        return builder.append("\n");
    }
    
    public static StringBuilder withWhereClause(StringBuilder builder) {
        return builder.append("WHERE { \n");
    }
    
    public static StringBuilder appendOptionalStatement(StringBuilder builder, String subject, String predicate, String object) {
        String statement = "OPTIONAL { "+subject+" "+predicate+" "+object+" } . \n";
        return builder.append(statement);
    }
    
    public static StringBuilder appendStatement(StringBuilder builder, String subject, String predicate, String object) {
        String statement = subject+" "+predicate+" "+object+" . \n";
        return builder.append(statement);
    }
    
    public static StringBuilder appendInscheme(StringBuilder builder, String id) {
        return appendStatement(builder, wrapId(id), SKOS_INSCHEME, INSCHEME_VAR);
    }
    
    public static StringBuilder appendPreflabel(StringBuilder builder, String id) {
        return appendStatement(builder, wrapId(id), SKOS_PREFLABEL, PREFLABEL_VAR);
    }
    
    public static StringBuilder appendDefinition(StringBuilder builder, String id) {
        return appendStatement(builder, wrapId(id), SKOS_DEFINITION, DEFINITION_VAR);
    }
    
    public static StringBuilder appendType(StringBuilder builder, String id) {
        return appendStatement(builder, wrapId(id), PROFILE_TYPE, TYPE_VAR);
    }
    
    public static StringBuilder appendContentType(StringBuilder builder, String id) {
        return appendStatement(builder, wrapId(id), PROFILE_CONTENT_TYPE, CONTENT_TYPE_VAR);
    }
    
    public static StringBuilder appendActivityDefinition(StringBuilder builder, String id) {
        appendStatement(builder, wrapId(id), PROFILE_ACTIVITY_DEFINITION, ACTIVITY_DEFINITION_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_DESCRIPTION, ACTIVITY_DESCRIPTION_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_NAME, ACTIVITY_NAME_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_TYPE, ACTIVITY_TYPE_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_MORE_INFO, ACTIVITY_MORE_INFO_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_EXTENSIONS, ACTIVITY_EXTENSIONS_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_INTERACTION_TYPE, ACTIVITY_INTERACTION_TYPE_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_CORRECT_RESPONSE_PATTERN, ACTIVITY_CORRECT_RESPONSE_PATTERN_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_CHOICES, ACTIVITY_CHOICES_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_SCALE, ACTIVITY_SCALE_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_SOURCE, ACTIVITY_SOURCE_VAR);
        appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_TARGET, ACTIVITY_TARGET_VAR);
        return appendOptionalStatement(builder, ACTIVITY_DEFINITION_VAR, XAPI_ACTIVITY_STEPS, ACTIVITY_STEPS_VAR);
    }
    
    public static StringBuilder appendOptionalDeprecated(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_DEPRECATED, DEPRECATED_VAR);
    }
    
    public static StringBuilder appendOptionalBroader(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_BROADER, BROADER_VAR);
    }
    
    public static StringBuilder appendOptionalBroadMatch(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_BROAD_MATCH, BROAD_MATCH_VAR);
    }
    
    public static StringBuilder appendOptionalNarrower(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_NARROWER, NARROWER_VAR);
    }
    
    public static StringBuilder appendOptionalNarrowMatch(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_NARROW_MATCH, NARROW_MATCH_VAR);
    }
    
    public static StringBuilder appendOptionalRelated(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_RELATED, RELATED_VAR);
    }
    
    public static StringBuilder appendOptionalRelatedMatch(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_RELATED_MATCH, RELATED_MATCH_VAR);
    }
    
    public static StringBuilder appendOptionalExactMatch(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_EXACT_MATCH, EXACT_MATCH_VAR);
    }
    
    public static StringBuilder appendOptionalRecommendedActivityTypes(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_RECOMMENDED_ACTIVITY_TYPES, RECOMMENDED_ACTIVITY_TYPES_VAR);
    }
    
    public static StringBuilder appendOptionalRecommendedVerbs(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_RECOMMENDED_VERBS, RECOMMENDED_VERBS_VAR);
    }
    
    public static StringBuilder appendOptionalContext(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_CONTEXT, CONTEXT_VAR);
    }
    
    public static StringBuilder appendOptionalSchema(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_SCHEMA, SCHEMA_VAR);
    }
    
    public static StringBuilder appendOptionalInlineSchema(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_INLINE_SCHEMA, INLINE_SCHEMA_VAR);
    }
    
    public static StringBuilder appendOptionalVerb(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_VERB, VERB_VAR);
    }
    
    public static StringBuilder appendOptionalObjectActivityType(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_OBJECT_ACTIVITY_TYPE, OBJECT_ACTIVITY_TYPE_VAR);
    }
    
    public static StringBuilder appendOptionalContextGroupingActivityType(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_CONTEXT_GROUPING_ACTIVITY_TYPE, CONTEXT_GROUPING_ACTIVITY_TYPE_VAR);
    }
    
    public static StringBuilder appendOptionalContextParentActivityType(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_CONTEXT_PARENT_ACTIVITY_TYPE, CONTEXT_PARENT_ACTIVITY_TYPE_VAR);
    }
    
    public static StringBuilder appendOptionalContextOtherActivityType(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_CONTEXT_OTHER_ACTIVITY_TYPE, CONTEXT_OTHER_ACTIVITY_TYPE_VAR);
    }
    
    public static StringBuilder appendOptionalContextCategoryActivityType(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_CONTEXT_CATEGORY_ACTIVITY_TYPE, CONTEXT_CATEGORY_ACTIVITY_TYPE_VAR);
    }
    
    public static StringBuilder appendOptionalAttachmentUsageType(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_ATTACHMENT_USAGE_TYPE, ATTACHMENT_USAGE_TYPE_VAR);
    }
    
    public static StringBuilder appendOptionalObjectStatementRefTemplate(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_OBJECT_STATEMENT_REF_TEMPLATE, OBJECT_STATEMENT_REF_TEMPLATE_VAR);
    }
    
    public static StringBuilder appendOptionalContextStatementRefTemplate(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_CONTEXT_STATEMENT_REF_TEMPLATE, CONTEXT_STATEMENT_REF_TEMPLATE_VAR);
    }
    
    public static StringBuilder appendOptionalInScheme(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_INSCHEME, INSCHEME_VAR);
    }
    
    public static StringBuilder appendOptionalPrefLabel(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_PREFLABEL, PREFLABEL_VAR);
    }
    
    public static StringBuilder appendOptionalDefinition(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), SKOS_DEFINITION, DEFINITION_VAR);
    }
    
    public static StringBuilder appendOptionalPrimaryPattern(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_PRIMARY, PRIMARY_VAR);
    }
    
    public static StringBuilder appendOptionalPatternAlternates(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_ALTERNATES, ALTERNATES_VAR);
    }
    
    public static StringBuilder appendOptionalPatternOptional(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_OPTIONAL, OPTIONAL_VAR);
    }
    
    public static StringBuilder appendOptionalPatternOneOrMore(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_ONE_OR_MORE, ONE_OR_MORE_VAR);
    }
    
    public static StringBuilder appendOptionalPatternSequence(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_SEQUENCE, SEQUENCE_VAR);
    }
    
    public static StringBuilder appendOptionalPatternZeroOrMore(StringBuilder builder, String id) {
        return appendOptionalStatement(builder, wrapId(id), PROFILE_ZERO_OR_MORE, ZERO_OR_MORE_VAR);
    }
    
    public static String createQuery(StringBuilder builder) {
        return builder.append("}").toString();
    }
    
    /**
     * Creates SPARQL String querying for properties of a Verb, Activity Type or Attachment Usage Type.
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#71-verbs-activity-types-and-attachment-usage-types
     * 
     * @param id - Id of the Verb, Activity Type or Attachment Usage Type this query targets
     * 
     * @return SPARQL query string
     */
    public static String createConceptRelationQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, INSCHEME_VAR, PREFLABEL_VAR, DEFINITION_VAR, DEPRECATED_VAR, TYPE_VAR,
                BROADER_VAR, BROAD_MATCH_VAR, NARROWER_VAR, NARROW_MATCH_VAR, RELATED_VAR, RELATED_MATCH_VAR, EXACT_MATCH_VAR);
        withWhereClause(builder);
        appendInscheme(builder, id);
        appendPreflabel(builder, id);
        appendDefinition(builder, id);
        appendType(builder, id);
        appendOptionalDeprecated(builder, id);
        appendOptionalBroader(builder, id);
        appendOptionalBroadMatch(builder, id);
        appendOptionalNarrower(builder, id);
        appendOptionalNarrowMatch(builder, id);
        appendOptionalRelated(builder, id);
        appendOptionalRelatedMatch(builder, id);
        appendOptionalExactMatch(builder, id);
        return createQuery(builder);
    }
    
    /**
     * Creates SPARQL String querying for properties of an Activity defined within an xAPI Profile
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#activities
     * 
     * @param id - Id of the Activity
     * 
     * @return SPARQL query string
     */
    public static String createActivityConceptQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, INSCHEME_VAR, TYPE_VAR, DEPRECATED_VAR, 
                ACTIVITY_DESCRIPTION_VAR, ACTIVITY_NAME_VAR, ACTIVITY_TYPE_VAR, ACTIVITY_MORE_INFO_VAR,
                ACTIVITY_EXTENSIONS_VAR, ACTIVITY_INTERACTION_TYPE_VAR, ACTIVITY_CORRECT_RESPONSE_PATTERN_VAR,
                ACTIVITY_CHOICES_VAR, ACTIVITY_SCALE_VAR, ACTIVITY_SOURCE_VAR, ACTIVITY_TARGET_VAR, ACTIVITY_STEPS_VAR);
        withWhereClause(builder);
        appendInscheme(builder, id);
        appendType(builder, id);
        appendOptionalDeprecated(builder, id);
        appendActivityDefinition(builder, id);
        return createQuery(builder);
    }
    
    /**
     * Creates SPARQL String querying for properties of an Activity Extension.
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#72-extensions
     * 
     * @param id - Id of the Activity Extension this query targets
     * 
     * @return SPARQL query string
     */
    public static String createActivityExtensionQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, INSCHEME_VAR, PREFLABEL_VAR, DEFINITION_VAR, 
                DEPRECATED_VAR, TYPE_VAR, RECOMMENDED_ACTIVITY_TYPES_VAR, CONTEXT_VAR, SCHEMA_VAR, INLINE_SCHEMA_VAR);
        withWhereClause(builder);
        appendInscheme(builder, id);
        appendPreflabel(builder, id);
        appendDefinition(builder, id);
        appendType(builder, id);
        appendOptionalDeprecated(builder, id);
        appendOptionalRecommendedActivityTypes(builder, id);
        appendOptionalContext(builder, id);
        appendOptionalSchema(builder, id);
        appendOptionalInlineSchema(builder, id);
        return createQuery(builder);
    }
    
    /**
     * Creates SPARQL String querying for properties of a Context or Result Extension.
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#72-extensions
     * 
     * @param id - Id of the Context or Result Extension this query targets
     * 
     * @return SPARQL query string
     */
    public static String createStatementExtensionQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, INSCHEME_VAR, PREFLABEL_VAR, DEFINITION_VAR, DEPRECATED_VAR, TYPE_VAR, RECOMMENDED_VERBS_VAR, CONTEXT_VAR, SCHEMA_VAR, INLINE_SCHEMA_VAR);
        withWhereClause(builder);
        appendInscheme(builder, id);
        appendPreflabel(builder, id);
        appendDefinition(builder, id);
        appendType(builder, id);
        appendOptionalDeprecated(builder, id);
        appendOptionalRecommendedVerbs(builder, id);
        appendOptionalContext(builder, id);
        appendOptionalSchema(builder, id);
        appendOptionalInlineSchema(builder, id);
        return createQuery(builder);
    }
    
    /**
     * Creates SPARQL String querying for properties of a Document Resource
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#73-document-resources
     * 
     * @param id - Id of the Document Resource this query targets
     * 
     * @return SPARQL query string
     */
    public static String createDocumentResourceQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, INSCHEME_VAR, PREFLABEL_VAR, DEFINITION_VAR, TYPE_VAR, CONTENT_TYPE_VAR, DEPRECATED_VAR, CONTEXT_VAR, SCHEMA_VAR, INLINE_SCHEMA_VAR);
        withWhereClause(builder);
        appendInscheme(builder, id);
        appendPreflabel(builder, id);
        appendDefinition(builder, id);
        appendType(builder, id);
        appendContentType(builder, id);
        appendOptionalDeprecated(builder, id);
        appendOptionalContext(builder, id);
        appendOptionalSchema(builder, id);
        appendOptionalInlineSchema(builder, id);
        return createQuery(builder);
    }
    
    /**
     * Creates SPARQL String querying for properties of a Statement Template
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#80-statement-templates
     * 
     * @param id - Id of the Statement Template this query targets
     * 
     * @return SPARQL query string
     */
    public static String createStatementTemplateQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, INSCHEME_VAR, PREFLABEL_VAR, DEFINITION_VAR, TYPE_VAR, DEPRECATED_VAR, VERB_VAR, OBJECT_ACTIVITY_TYPE_VAR, 
                CONTEXT_GROUPING_ACTIVITY_TYPE_VAR, CONTEXT_PARENT_ACTIVITY_TYPE_VAR, CONTEXT_OTHER_ACTIVITY_TYPE_VAR, CONTEXT_CATEGORY_ACTIVITY_TYPE_VAR,
                ATTACHMENT_USAGE_TYPE_VAR, OBJECT_STATEMENT_REF_TEMPLATE_VAR, CONTEXT_STATEMENT_REF_TEMPLATE_VAR);
        withWhereClause(builder);
        appendInscheme(builder, id);
        appendPreflabel(builder, id);
        appendDefinition(builder, id);
        appendType(builder, id);
        appendOptionalDeprecated(builder, id);
        appendOptionalVerb(builder, id);
        appendOptionalObjectActivityType(builder, id);
        appendOptionalContextGroupingActivityType(builder, id);
        appendOptionalContextParentActivityType(builder, id);
        appendOptionalContextOtherActivityType(builder, id);
        appendOptionalContextCategoryActivityType(builder, id);
        appendOptionalAttachmentUsageType(builder, id);
        appendOptionalObjectStatementRefTemplate(builder, id);
        appendOptionalContextStatementRefTemplate(builder, id);
        return createQuery(builder);
    }
    
    /**
     * Creates SPARQL String querying for properties of a Pattern
     * 
     * see https://github.com/adlnet/xapi-profiles/blob/master/xapi-profiles-structure.md#90-patterns
     * 
     * @param id - Id of the Pattern this query targets
     * 
     * @return SPARQL query string
     */
    public static String createPatternQuery(String id) {
        StringBuilder builder = withPrefixes();
        withSelectVars(builder, TYPE_VAR, PRIMARY_VAR, INSCHEME_VAR, PREFLABEL_VAR, DEFINITION_VAR, DEPRECATED_VAR, 
                ALTERNATES_VAR, OPTIONAL_VAR, ONE_OR_MORE_VAR, SEQUENCE_VAR, ZERO_OR_MORE_VAR);
        withWhereClause(builder);
        appendType(builder, id);
        appendOptionalPrimaryPattern(builder, id);
        appendOptionalInScheme(builder, id);
        appendOptionalPrefLabel(builder, id);
        appendOptionalDefinition(builder, id);
        appendOptionalDeprecated(builder, id);
        appendOptionalPatternAlternates(builder, id);
        appendOptionalPatternOptional(builder, id);
        appendOptionalPatternOneOrMore(builder, id);
        appendOptionalPatternSequence(builder, id);
        appendOptionalPatternZeroOrMore(builder, id);
        return createQuery(builder);
    }
}
