package mil.arl.gift.lms.impl.lrs.xapi.profile;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of an Activity Profile Resource Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ActivityProfileResourceConcept extends AbstractDocumentResourceConcept {
    
    /**
     * Set Activity Profile Resource Concept fields from SPARQL query result
     * 
     * @param id - identifier for Activity Profile Resource
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public ActivityProfileResourceConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }
}
