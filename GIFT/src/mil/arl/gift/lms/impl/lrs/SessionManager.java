package mil.arl.gift.lms.impl.lrs;

import java.util.LinkedHashMap;
import java.util.Map;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.lms.impl.common.LmsDomainSessionException;

/**
 * Management of sessions (Domain + Knowledge) used within xAPI instrumentation.
 * 
 * @author Yet Analytics
 *
 */
public class SessionManager {
    
    /**
     * Cache which stores n key value entries, once n is reached, the oldest entry is removed.
     * 
     * @author Yet Analytics
     *
     * @param <K> - Key within the cache
     * @param <V> - Value associated with Key
     */
    public class SimpleCache<K, V> extends LinkedHashMap<K, V>{
        
        private static final long serialVersionUID = 5160163048867230861L;
        private int cacheCapacity;
        
        public SimpleCache(int size) {
            // Insertion-order based removal of eldest entry
            super(16, (float) 0.75, false);
            this.cacheCapacity = size;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() >= cacheCapacity;
          }
    }
    
    /** Mapping of userSession to DomainSession */
    private final SimpleCache<UserSession, DomainSession> userSessionToCurrentDomainSession = new SimpleCache<UserSession, DomainSession>(100);
    
    /** Map of domain session id to domain session */
    private final SimpleCache<Integer, DomainSession> domainSessionIdToDomainSession = new SimpleCache<Integer, DomainSession>(100);
    
    /** Knowledge session used within xAPI statement generation */
    private AbstractKnowledgeSession currentKnowledgeSession;
    
    /** Instance of the SessionManager */
    private static SessionManager instance = null;
    
    /** Private no argument constructor used within getInstance() */
    private SessionManager() {}
    
    /**
     * @return possibly empty but not null mapping from UserSession(s) to DomainSession(s)
     */
    public SimpleCache<UserSession, DomainSession> getMapping(){
        return userSessionToCurrentDomainSession;
    }
    
    /**
     * @return instance of the SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if(instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Getter for current Knowledge Session
     * 
     * @return active IndividualKnowledgeSession, TeamKnowledgeSession or null if not set
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return currentKnowledgeSession;
    }
    
    /**
     * Has a knowledge session been set?
     * 
     * @return true if set, false otherwise
     */
    public boolean isKnowledgeSessionSet() {
        return getKnowledgeSession() != null;
    }
    
    /**
     * Setter for current Knowledge Session
     * 
     * @param session - IndividualKnowledgeSession or TeamKnowledgeSession from lrs.insertKnowledgeSessionDetails
     */
    public void setKnowledgeSession(AbstractKnowledgeSession session) {
        this.currentKnowledgeSession = session;
    }
    
    /**
     * Adds or overwrites existing mapping from user : domainSession within 'userSessionToCurrentDomainSession'
     * 
     * @param userSession - key
     * @param domainSession - value
     */
    public void mapUserToDomainSession(UserSession userSession, DomainSession domainSession) {
        userSessionToCurrentDomainSession.put(userSession, domainSession);
    }
    
    /**
     * Adds mapping domainSessionId : domainSession to 'domainSessionIdToDomainSession' but
     * will throw and not overwrite existing mappings on conflict
     * 
     * @param domainSessionId - key
     * @param domainSession - value
     * 
     * @throws LrsDomainSessionException - when there is an existing mapping between domainSessionId and domainSession
     */
    public void mapIdToDomainSession(Integer domainSessionId, DomainSession domainSession) throws LmsDomainSessionException {
        if(domainSessionIdToDomainSession.putIfAbsent(domainSessionId, domainSession) != null) {
            throw new LmsDomainSessionException("Attempting to overwrite mapping from domainSessionId to domainSession within domainSessionIdToDomainSession!");
        }
    }
    
    /**
     * Given a UserSession, return the currently associated DomainSession
     * found within 'userSessionToCurrentDomainSession', null otherwise
     * 
     * @param userSession - extract userId and use as key
     * 
     * @return associated DomainSession or null
     */
    public DomainSession getCurrentDomainSession(UserSession userSession) {
        return userSessionToCurrentDomainSession.get(userSession);
    }
    
    /**
     * Given a userId, return the currently associated DomainSession
     * 
     * @param userId - key
     * 
     * @return associated DomainSession or null
     */
    public DomainSession getCurrentDomainSession(Integer userId) {
        DomainSession session = null;
        for(Map.Entry<UserSession, DomainSession> kv : userSessionToCurrentDomainSession.entrySet()) {
            if(kv.getKey().getUserId() == userId) {
                session = kv.getValue();
                break;
            }
        }
        return session;
    }
    
    /**
     * Given a userName, return the currently associated DomainSession or null
     * 
     * @param userName - key
     * 
     * @return associated DomainSession or null
     */
    public DomainSession getCurrentDomainSession(String userName) {
        DomainSession session = null;
        for(Map.Entry<UserSession, DomainSession> kv : userSessionToCurrentDomainSession.entrySet()) {
            if(kv.getKey().getUsername().equals(userName)) {
                session = kv.getValue();
                break;
            }
        }
        return session;
    }
    
    /**
     * Given a domainSessionId, return the corresponding DomainSession
     * found within 'domainSessionIdToDomainSession', null otherwise
     * 
     * @param domainSessionId - key
     * 
     * @return associated DomainSession or null
     */
    public DomainSession getDomainSession(Integer domainSessionId) {
        return domainSessionIdToDomainSession.get(domainSessionId);
    }
    
    /**
     * Given a UserSession, return the domainId from the currently associated DomainSession
     * found within 'userSessionToCurrentDomainSession', null otherwise
     * 
     * @param userSession - key
     * 
     * @return name of the course associated with the domain session or null
     */
    public String getCurrentDomainId(UserSession userSession) {
        return getCurrentDomainId(userSession.getUserId());
    }
    
    /**
     * Given a userId, return the domainId from the currently associated DomainSession
     * found within 'userSessionToCurrentDomainSession', null otherwise
     * 
     * @param userId - key
     * 
     * @return name of the course associated with the domain session or null
     */
    public String getCurrentDomainId(Integer userId) {
        DomainSession ds = getCurrentDomainSession(userId);
        if(ds == null) {
            return null;
        }
        return ds.getDomainRuntimeId();
    }
    
    /**
     * Given a domainSessionId, return the associated domainId or null if not found
     * 
     * @param domainSessionId - key
     * 
     * @return domainId of associated DomainSession or null
     */
    public String getDomainId(Integer domainSessionId) {
        DomainSession ds = getDomainSession(domainSessionId);
        if(ds == null) {
            return null;
        }
        return ds.getDomainRuntimeId();
    }
    
    /**
     * Given a UserSession, return the domainSessionId from the currently associated DomainSession
     * found within 'userSessionToCurrentDomainSession', null otherwise
     * 
     * @param userSession - key
     * 
     * @return domain session id or null
     */
    public Integer getCurrentDomainSessionId(UserSession userSession) {
        return getCurrentDomainSessionId(userSession.getUserId());
    }
    
    /**
     * Given a userId, return the domainSessionId from the currently associated DomainSession
     * found within 'userSessionToCurrentDomainSession', null otherwise
     * 
     * @param userId - key
     * 
     * @return domain session id or null
     */
    public Integer getCurrentDomainSessionId(Integer userId) {
        DomainSession ds = getCurrentDomainSession(userId);
        if(ds == null) {
            return null;
        }
        return ds.getDomainSessionId();
    }
    
    /**
     * Given a userName, return the domainSessionId from the currently associated DomainSession
     * found within 'userSessionToCurrentDomainSession', null otherwise
     * 
     * @param userName - key
     * 
     * @return domain session id or null
     */
    public Integer getCurrentDomainSessionId(String userName) {
        DomainSession ds = getCurrentDomainSession(userName);
        if(ds == null) {
            return null;
        }
        return ds.getDomainSessionId();
    }
    
    /**
     * Given a domainSessionId, return the associated userId or null if not found
     * 
     * @param domainSessionId - key
     * 
     * @return userId of the associated Domain Session or null if not found
     */
    public Integer getUserIdForDomainSessionId(Integer domainSessionId) {
        DomainSession ds = getDomainSession(domainSessionId);
        if(ds == null) {
            return null;
        }
        return ds.getUserId();
    }
    
    /**
     * Given a domainSessionId, return the associated userName or null
     * 
     * @param domainSessionId - key
     * 
     * @return userName within associated Domain Session when set, null otherwise
     */
    public String getUserNameForDomainSessionId(Integer domainSessionId) {
        DomainSession ds = getDomainSession(domainSessionId);
        if(ds == null) {
            return null;
        }
        return ds.getUsername();
    }
}
