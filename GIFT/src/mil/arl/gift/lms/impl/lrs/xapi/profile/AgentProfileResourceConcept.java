package mil.arl.gift.lms.impl.lrs.xapi.profile;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of an Agent Profile Resource Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class AgentProfileResourceConcept extends AbstractDocumentResourceConcept {
    
    /**
     * Set Agent Profile Resource Concept fields from SPARQL query result
     * 
     * @param id - identifier for Agent Profile Resource
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public AgentProfileResourceConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }
}
