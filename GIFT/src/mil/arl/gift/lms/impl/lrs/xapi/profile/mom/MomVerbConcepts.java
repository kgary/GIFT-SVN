package mil.arl.gift.lms.impl.lrs.xapi.profile.mom;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.VerbConcept;

/**
 * Verb Concept defined in the Master Object Model (MOM) xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class MomVerbConcepts extends VerbConcept {
    
    protected MomVerbConcepts(String id) throws LmsXapiProfileException {
        super(id, tlaConceptRelationSparqlQuery(id, true));
    }
    
    // Subclass for Assessed
    public static class Assessed extends MomVerbConcepts {
        // Singleton
        private static Assessed instance = null;
        // Constructor for Assessed Verb
        private Assessed() throws LmsXapiProfileException {
            super("https://w3id.org/xapi/tla/verbs/assessed");
        }
        // Access
        public static Assessed getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Assessed();
            }
            return instance;
        }
    }
}
