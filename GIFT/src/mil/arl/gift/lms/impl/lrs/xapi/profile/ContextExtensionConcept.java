package mil.arl.gift.lms.impl.lrs.xapi.profile;

import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of Context Extension Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ContextExtensionConcept extends AbstractStatementExtensionConcept {
    
    /**
     * Set Context Extension Concept fields from SPARQL query result
     * 
     * @param id - identifier for Context Extension
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public ContextExtensionConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }

    /**
     * Add contextExtensions to Context
     * 
     * @param context - Context to update
     * @param contextExtensions - Extensions to add
     */
    private void addToContext(Context context, Extensions contextExtensions) {
        if(context == null) {
            throw new IllegalArgumentException("context can not be null!");
        }
        if(contextExtensions != null) {
            context.setExtensions(contextExtensions);
        }
    }

    /**
     * Adds Extension to Context where the key is the id of this ContextExtensionConcept
     * and the value is the passed in 'value'. attempts to update instead of overwrite on conflict
     * 
     * @param context - Context to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ContextExtensionConcept
     */
    public void addToContext(Context context, Object value) {
        addToContext(context, value, false);
    }

    /**
     * Adds Extension to Context where the key is the id of this ContextExtensionConcept
     * and the value is the passed in 'value', on conflict behavior mediated by 'forceOverwrite'
     * 
     * @param context - Context to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ContextExtensionConcept
     * @param forceOverwrite - if true, overwrite any existing values for this ContextExtensionConcept with 'value' 
     */
    public void addToContext(Context context, Object value, boolean forceOverwrite) {
        if(context == null) {
            throw new IllegalArgumentException("context can not be null!");
        }
        Extensions existing = context.getExtensions();
        if(existing == null) {
            addToContext(context, asExtension(value));
        } else {
            addToContext(context, asExtension(value, existing, forceOverwrite));
        }
    }

    /**
     * Adds Extension to Context when the statement's verb is
     * one of the recommendedVerbs.
     * 
     * If added, the extension key is the id of this ContextExtensionConcept
     * and the value is the passed in 'value'. Merge attempted on conflict
     * 
     * @param statement - Statement to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ContextExtensionConcept
     */
    @Override
    public void addToStatement(Statement statement, Object value) {
        addToStatement(statement, value, false);
    }

    /**
     * Adds Extension to Context when the statement's verb is
     * one of the recommendedVerbs.
     * 
     * If added, the extension key is the id of this ContextExtensionConcept
     * and the value is the passed in 'value'. Behavior on conflict mediated by 'forceOverwrite'
     * 
     * @param statement - Statement to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ContextExtensionConcept
     * @param forceOverwrite - if true, overwrite any existing values for this ContextExtensionConcept with 'value'
     */
    @Override
    public void addToStatement(Statement statement, Object value, boolean forceOverwrite) {
        if(statement == null) {
            throw new IllegalArgumentException("statement can not be null!");
        }
        if(isRecommendedVerb(statement.getVerb())) {
            Context c = statement.getContext() != null ? statement.getContext() : new Context();
            addToContext(c, value, forceOverwrite);
            statement.setContext(c);
        }
    }
}
