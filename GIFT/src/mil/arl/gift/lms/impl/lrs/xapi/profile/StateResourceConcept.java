package mil.arl.gift.lms.impl.lrs.xapi.profile;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of a State Resource Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class StateResourceConcept extends AbstractDocumentResourceConcept {
    
    /**
     * Set State Resource Concept fields from SPARQL query result
     * 
     * @param id - identifier for State Resource
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public StateResourceConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }
}
