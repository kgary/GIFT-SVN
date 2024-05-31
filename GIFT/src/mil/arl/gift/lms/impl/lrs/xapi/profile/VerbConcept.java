package mil.arl.gift.lms.impl.lrs.xapi.profile;

import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.SparqlResult;

/**
 * Representation of Verb Concept defined in an xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class VerbConcept extends AbstractConceptRelation {
    
    /**
     * Set Verb Concept fields from SPARQL query result
     * 
     * @param id - identifier for State Resource
     * @param src - SPARQL query result
     * 
     * @throws LmsXapiProfileException when unable to set properties
     */
    public VerbConcept(String id, SparqlResult src) throws LmsXapiProfileException {
        super(id, src);
    }
    
    /**
     * Convert from Verb Concept to Verb
     * 
     * @return verb representation of the xAPI Profile Verb Concept
     */
    public Verb asVerb() {
        Verb v = new Verb(getId());
        if(getPrefLabel() != null) {
            v.setDisplay(getPrefLabel());
        }
        return v;
    }
}
