package mil.arl.gift.lms.impl.lrs.xapi.profile.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generated.lms.Parameters;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;

/**
 * Container for live and local Profile Server client. Live client communicates with ADL Profile Server API.
 * Local client parses xAPI Components from xAPI Profiles found within LMS configuration directory.
 * 
 * @author Yet Analytics
 *
 */
public final class Client {
    
    private static Client instance = null;
    private LocalClient local;
    private SparqlQuery live;
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    
    private Client() {
        this.live = null;
        try {
            this.local = new LocalClient();
        } catch (LmsXapiProfileException e) {
            logger.error("Unable to Initalize Local Profile Server!", e);
            this.local = null;
        }
    }
    
    private Client(Parameters parameters) throws LmsXapiProfileException {
        this();
        try {
            this.live = connect(parameters);
        } catch (LmsXapiProfileException e) {
            this.live = null;
            throw new LmsXapiProfileException("Unable to connect to ADL Profile Server!", e);
        }
    }
    
    private static SparqlQuery connect(Parameters parameters) throws LmsXapiProfileException {
        SparqlQuery live;
        try {
            live = new SparqlQuery(parameters);
        } catch (LmsXapiSparqlException e) {
            throw new LmsXapiProfileException("Unable to connect to ADL Profile Server!", e);
        }
        
        String attemptSparql = SparqlQueryString.createConceptRelationQuery("http://adlnet.gov/expapi/verbs/attempted");
        try {
            live.executeQuery(attemptSparql);
        } catch (LmsXapiSparqlException e) {
            throw new LmsXapiProfileException("ADL Profile Server connection attempt failed!", e);
        }
        return live;
    }
    
    public static synchronized Client getClient() {
        if(instance == null) {
            instance = new Client();
        }
        return instance;
    }
    
    public static synchronized Client getClient(Parameters parameters) throws LmsXapiProfileException {
        if(instance == null) {
            instance = new Client(parameters);
        }
        return instance;
    }
    
    public LocalClient getLocalClient() {
        return local;
    }
    
    public SparqlQuery getLiveClient() {
        return live;
    }   
}
