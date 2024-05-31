package mil.arl.gift.lms.impl.lrs.xapi.profile.scorm;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.VerbConcept;

/**
 * Verb Concept defined in the SCORM xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ScormVerbConcepts extends VerbConcept {

    protected ScormVerbConcepts(String id) throws LmsXapiProfileException {
        super(id, scormConceptRelationSparqlQuery(id, true));
    }
    
    public static class Completed extends ScormVerbConcepts {
        // Singleton
        private static Completed instance = null;
        // Constructor
        private Completed() throws LmsXapiProfileException {
            super("http://adlnet.gov/expapi/verbs/completed");
        }
        // Access
        public static Completed getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Completed();
            }
            return instance;
        }
    }
}
