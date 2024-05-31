package mil.arl.gift.lms.impl.lrs.xapi.profile;

import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of Result Extension Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ResultExtensionConcept extends AbstractStatementExtensionConcept {
    
    /**
     * Set Result Extension Concept fields from SPARQL query result
     * 
     * @param id - identifier for Result Extension
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public ResultExtensionConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }
    
    /**
     * Add resultExtensions to Result
     * 
     * @param result - Result to update
     * @param resultExtensions - Extensions to add
     */
    private void addToResult(Result result, Extensions resultExtensions) {
        if(result == null) {
            throw new IllegalArgumentException("result can not be null!");
        }
        if(resultExtensions != null) {
            result.setExtensions(resultExtensions);
        }
    }
    
    /**
     * Adds Extension to Result where the key is the id of this ResultExtensionConcept
     * and the value is the passed in 'value', attempts to update instead of overwrite on conflict
     * 
     * @param result - Result to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ResultExtensionConcept
     */
    public void addToResult(Result result, Object value) {
        addToResult(result, value, false);
    }
    
    /**
     * Adds Extension to Result where the key is the id of this ResultExtensionConcept
     * and the value is the passed in 'value', on conflict behavior mediated by 'forceOverwrite'
     * 
     * @param result - Result to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ResultExtensionConcept
     * @param forceOverwrite - if true, overwrite any existing values for this ResultExtensionConcept with 'value' 
     */
    public void addToResult(Result result, Object value, boolean forceOverwrite) {
        if(result == null) {
            throw new IllegalArgumentException("result can not be null!");
        }
        Extensions existing = result.getExtensions();
        if(existing == null) {
            addToResult(result, asExtension(value));
        } else {
            addToResult(result, asExtension(value, existing, forceOverwrite));
        }
    }
    
    /**
     * Adds Extension to Result when the statement's verb is
     * one of the recommendedVerbs.
     * 
     * If added, the extension key is the id of this ResultExtensionConcept
     * and the value is the passed in 'value'. Merge attempted on conflict
     * 
     * @param statement - Statement to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ResultExtensionConcept
     */
    @Override
    public void addToStatement(Statement statement, Object value) {
        addToStatement(statement, value, false);
    }
    
    /**
     * Adds Extension to Result when the statement's verb is
     * one of the recommendedVerbs.
     * 
     * If added, the extension key is the id of this ResultExtensionConcept
     * and the value is the passed in 'value'. Behavior on conflict mediated by 'forceOverwrite'
     * 
     * @param statement - Statement to update
     * @param value - object that conforms to the 'schema' or 'inLineSchema' (JSON Schema) of this ResultExtensionConcept
     * @param forceOverwrite - if true, overwrite any existing values for this ResultExtensionConcept with 'value'
     */
    @Override
    public void addToStatement(Statement statement, Object value, boolean forceOverwrite) {
        if(statement == null) {
            throw new IllegalArgumentException("statement can not be null!");
        }
        if(isRecommendedVerb(statement.getVerb())) {
            Result r = statement.getResult() != null ? statement.getResult() : new Result();
            addToResult(r, value, forceOverwrite);
            statement.setResult(r);
        }
    }
}
