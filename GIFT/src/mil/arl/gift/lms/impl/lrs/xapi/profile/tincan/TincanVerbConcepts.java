package mil.arl.gift.lms.impl.lrs.xapi.profile.tincan;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.VerbConcept;

/**
 * Verb Concept defined in the TinCan xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class TincanVerbConcepts extends VerbConcept {

    protected TincanVerbConcepts(String id) throws LmsXapiProfileException {
        super(id, tincanConceptRelationSparqlQuery(id, true));
    }
    // Subclass for bookmarked
    public static class Bookmarked extends TincanVerbConcepts {
        // Singleton
        private static Bookmarked instance = null;
        // Constructor
        private Bookmarked() throws LmsXapiProfileException {
            super("http://id.tincanapi.com/verb/bookmarked");
        }
        // Access
        public static Bookmarked getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Bookmarked();
            }
            return instance;
        }
    }
}
