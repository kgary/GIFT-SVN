package mil.arl.gift.lms.impl.lrs.xapi.profile;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of an xAPI Statement Template defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class StatementTemplate extends AbstractStatementTemplate {
    
    /**
     * Set Statement Template fields from SPARQL query result
     * 
     * @param id - identifier for Statement Template
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public StatementTemplate(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
        for(String objectTemplate : src.getObjectStatementRefTemplate()) {
            SparqlResult objectStatementTemplate = statementTemplateSparqlQuery(objectTemplate, true);
            addObjectStatementRefTemplate(new StatementTemplate(objectTemplate, objectStatementTemplate));
            
        }
        for(String contextTemplate : src.getContextStatementRefTemplate()) {
            SparqlResult contextStatementTemplate = statementTemplateSparqlQuery(contextTemplate, true);
            addContextStatementRefTemplate(new StatementTemplate(contextTemplate, contextStatementTemplate));
        }
    }
}
