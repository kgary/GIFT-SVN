package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.VerbConcept;

/**
 * Verbs defined within the ITS xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ItsVerbConcepts extends VerbConcept {
    
    protected ItsVerbConcepts(String id) throws LmsXapiProfileException {
        super(id, giftConceptRelationSparqlQuery(id, true));
    }
    // Subclass for Started Verb
    public static class Started extends ItsVerbConcepts {
        // Singleton
        private static Started instance = null;
        // Constructor for Started Verb
        private Started() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/Verb#started");
        }
        // Access
        public static Started getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Started();
            }
            return instance;
        }
    }
    // Subclass for Closed Verb
    public static class Closed extends ItsVerbConcepts {
        // Instance
        private static Closed instance = null;
        // Constructor for Closed Verb
        private Closed() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/Verb#closed");
        }
        // Access
        public static Closed getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Closed();
            }
            return instance;
        }
    }
    // Subclass for Predicted Verb
    public static class Predicted extends ItsVerbConcepts {
        // Singleton
        private static Predicted instance = null;
        // Constructor for Predicted Verb
        private Predicted() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/Verb#predicted");
        }
        // Access
        public static Predicted getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Predicted();
            }
            return instance;
        }
    }
    // Subclass for Demonstrated Verb
    public static class Demonstrated extends ItsVerbConcepts {
        // Singleton
        private static Demonstrated instance = null;
        // Constructor for Demonstrated Verb
        private Demonstrated() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/Verb#demonstrated");
        }
        // Access
        public static Demonstrated getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new Demonstrated();
            }
            return instance;
        }
    }
}
