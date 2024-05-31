package mil.arl.gift.lms.impl.lrs.xapi.profile.adl;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.VerbConcept;

/**
 * Verbs defined in the ADL xAPI Profile that are used within the xAPI instrumentation.
 * 
 * @author Yet Analytics
 *
 */
public class AdlVerbConcepts extends VerbConcept {
    // Constructor
    protected AdlVerbConcepts(String id) throws LmsXapiProfileException {
        super(id, adlConceptRelationSparqlQuery(id, true));
    }
    // Subclass for Voided
    public static class Voided extends AdlVerbConcepts {
        // Singleton
        private static Voided instance = null;
        // Constructor
        private Voided() throws LmsXapiProfileException {
            super("http://adlnet.gov/expapi/verbs/voided");
        }
        // Access
        public static Voided getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Voided();
            }
            return instance;
        }
    }
    // Subclass for experienced
    public static class Experienced extends AdlVerbConcepts {
        // Singleton
        private static Experienced instance = null;
        // Constructor
        private Experienced() throws LmsXapiProfileException {
            super("http://adlnet.gov/expapi/verbs/experienced");
        }
        // Access
        public static Experienced getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Experienced();
            }
            return instance;
        }
    }
}
