package mil.arl.gift.lms.impl.lrs.xapi.profile.its;

import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.AgentProfileResourceConcept;

/**
 * Agent Profile Concept defined within the ITS xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class ItsAgentProfileConcepts extends AgentProfileResourceConcept {

    protected ItsAgentProfileConcepts(String id) throws LmsXapiProfileException {
        super(id, giftDocumentResourceSparqlQuery(id, true));
    }
    
    public static class LsaCache extends ItsAgentProfileConcepts {
        // Singleton
        private static LsaCache instance = null;
        // Constructor
        private LsaCache() throws LmsXapiProfileException {
            super("https://xapinet.org/xapi/stetmt/its/AgentProfileResource#lsa.cache");
        }
        // Access
        public static LsaCache getInstance() throws LmsXapiProfileException {
            if(instance == null) {
                instance = new LsaCache();
            }
            return instance;
        }
    }
}
