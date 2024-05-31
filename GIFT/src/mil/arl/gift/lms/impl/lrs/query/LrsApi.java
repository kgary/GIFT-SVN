package mil.arl.gift.lms.impl.lrs.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rusticisoftware.tincan.lrsresponses.AboutLRSResponse;
import com.rusticisoftware.tincan.lrsresponses.AgentProfileLRSResponse;
import com.rusticisoftware.tincan.lrsresponses.LRSResponse;
import com.rusticisoftware.tincan.lrsresponses.StatementsResultLRSResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.QueryResultFormat;
import com.rusticisoftware.tincan.RemoteLRS;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.StatementsResult;
import com.rusticisoftware.tincan.TCAPIVersion;
import com.rusticisoftware.tincan.Verb;
import com.rusticisoftware.tincan.documents.AgentProfileDocument;
import com.rusticisoftware.tincan.v10x.StatementsQuery;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiQueryException;
import mil.arl.gift.lms.impl.lrs.CommonLrsEnum;
import mil.arl.gift.lms.impl.lrs.LanguageTagEnum;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsAgentProfileConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;

/**
 * Class which provides methods for interacting with an LRS's API.
 * 
 * @author Yet Analytics
 *
 */
public class LrsApi extends RemoteLRS {
    /** is there an active connection to the LRS API */
    private boolean isConnected = false;
    
    /** class used to periodically check connection to LRS */
    private LrsPoll poll;
    
    /** class used to store statements prior to send off and records what statement ids have been sent to the LRS */
    private StatementCache stmtCache;
    
    /** class used to store Actor : LSA mappings within LRS */
    private LsaCache lsaCache;
    
    /** class used to configure and execute LRS query */
    private LrsQuery stmtQuery;
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LrsApi.class);
    
    /**
     * Creates TCAPIVersion.V100 RemoteLRS
     * 
     * @param endpoint - Target LRS statements resource
     * @param apiKey - Target LRS basic authorization user name
     * @param apiKeySecret - Target LRS basic authorization password
     * 
     * @throws LmsXapiQueryException - when unable to initialize or connect to LRS API
     */
    public LrsApi(String endpoint, String apiKey, String apiKeySecret) throws LmsXapiQueryException {
        super(TCAPIVersion.V100);
        try {
            setEndpoint(endpoint);
        } catch (MalformedURLException e) {
            throw new LmsXapiQueryException("Unable to set Lrs API endpoint!", e);
        }
        setUsername(apiKey);
        setPassword(apiKeySecret);
        setIsConnected(isConnected());
        if(!isConnected) {
            throw new LmsXapiQueryException("Unable to connect to LRS!");
        }
        this.poll = new LrsPoll();
        this.stmtCache = new StatementCache();
        this.lsaCache = new LsaCache();
        this.stmtQuery = new LrsQuery();
    }
    
    /**
     * Makes request to LRS's about resource to determine if connection is active
     * 
     * @return true if request was successful, false otherwise
     */
    public boolean isConnected() {
        AboutLRSResponse aboutResp = this.about();
        return aboutResp.getSuccess();
    }
    
    /**
     * setter for isConnected
     * 
     * @param bool - is there an active connection to LRS
     */
    private void setIsConnected(boolean bool) {
        this.isConnected = bool;
    }
    
    /**
     * @return is there an active connection to the LRS
     */
    public boolean getIsConnected() {
        return isConnected;
    }
    
    /**
     * clear stored state and shut down LRS connection status checking
     */
    public void cleanup() {
        poll.cleanup();
        stmtCache.cleanup();
        lsaCache.cleanup();
    }
    
    /**
     * Initialize periodic checking status of LRS connection
     */
    public void startPoll() {
        poll.init();
    }
    
    /**
     * Has this xAPI Statement already been added to Statement Accumulator
     * 
     * @param statement - xAPI Statement to check
     * 
     * @return true when Statement's id not found within the Statement Id Accumulator
     */
    public boolean isNovelStatementId(Statement statement) {
        if(statement == null) {
            throw new IllegalArgumentException("Statement can not be null!");
        }
        return stmtCache.novelStatementId(statement.getId());
    }
    
    /**
     * Remove all Statement Id(s) from Statement Id Accumulator
     */
    public void clearStmtIdCache() {
        stmtCache.clearStatementIdCache();
    }
    
    /**
     * Determines if still connected to LRS based off of the response.
     *  
     * @param resp - response from TinCanJava
     */
    private void handleResponseStatus(LRSResponse resp) {
        if(resp == null) {
            throw new IllegalArgumentException("LRS Response can not be null!");
        }
        String errorMsg = resp.getErrMsg();
        boolean success = resp.getSuccess();
        String statusReason = resp.getResponse().getStatusMsg();
        int status = resp.getResponse().getStatus();
        if(errorMsg != null) {
            logger.error("Error msg from LRS response: "+errorMsg);
        }
        if(logger.isDebugEnabled() && !success) {
            logger.debug("Success: "+success);
            logger.debug("Status: "+status);
            logger.debug("Status Reason: "+statusReason);
        }
        if(!success && status == 400 && StringUtils.contains(statusReason, "Exception in RemoteLRS.makeSyncRequest():")) {
            setIsConnected(false);
            logger.warn("No longer connected to LRS!");
        } else if(!success && status == 500) {
            setIsConnected(false);
            logger.warn("No longer connected to LRS!");
        }
    }
    
    /**
     * Attempt to save single xAPI Statement to LRS
     * 
     * @param stmt - xAPI Statement to save
     * 
     * @return was the save successful?
     */
    private boolean saveSingleStatement(Statement stmt) {
        if(stmt == null) {
            throw new IllegalArgumentException("Statement can not be null!");
        }
        boolean success = false;
        if(isConnected) {
            LRSResponse resp = saveStatement(stmt);
            handleResponseStatus(resp);
            success = resp.getSuccess();
        }
        return success;
    }
    
    /**
     * Attempts to save xAPI Statements within Statement Accumulator and populate
     * LRS with cached Agent Profile Document data.
     * 
     * When not connected to LRS, results in no op.
     * 
     * When connected to LRS and Statement Conflict encountered, attempts
     * to retry statement by statement. During this process, if a Statement
     * is rejected by the LRS its added to the Statement DLQ
     */
    private void saveStatements() {
        List<Statement> accumulator = stmtCache.getAccumulator();
        // METRICS
        int stmtCount = accumulator.size();
        long start, end;
        start = System.currentTimeMillis();
        // Logging
//        for(Statement stmt : accumulator) {
//            if(logger.isDebugEnabled()) {
//                logger.debug("Statement being sent to LRS: "+stmt.toJSON(true));
//            }
//        }
        if(isConnected) {
            LRSResponse resp = saveStatements(accumulator);
            // determine if response indicates disconnection from LRS
            handleResponseStatus(resp);
            int status = resp.getResponse().getStatus();
            if(resp.getSuccess()) {
                stmtCache.clearStatementAccumulator();
            } else if(isConnected && (status == 400 || status == 409)) {
                // failure while still connected to LRS due to statement conflict
                // retry statement by statement
                for(int i = 0; i < stmtCount; i++) {
                    // Get last statement and attempt save
                    Statement stmt = accumulator.get(accumulator.size() - 1);
                    boolean singleStmtSuccess = saveSingleStatement(stmt);
                    if(!singleStmtSuccess && isConnected) {
                        // Unable to save xAPI Statement to LRS
                        logger.warn("Unable to save xAPI Statement to LRS! Adding to DLQ");
                        if(logger.isDebugEnabled()) {
                            logger.debug("Statement Added to DLQ: "+stmt.toJSON(true));
                        }
                        stmtCache.getDLQ().add(stmt);
                        accumulator.remove(accumulator.size() - 1);
                    } else if(singleStmtSuccess) {
                        accumulator.remove(accumulator.size() - 1);
                    }
                }
            }
            // Attempt to save cached Actor : LSA data
            lsaCache.saveCachedData();
        }
        // METRICS
        end = System.currentTimeMillis() - start;
        if(logger.isTraceEnabled()) {
            logger.trace("METRICS: saveStatements took: "+end+" ms for "+stmtCount+" statements");
        }
    }
    
    /**
     * Adds xAPI Statements to Statement Accumulator and saves to LRS when
     * forceSave is true or the Statement Accumulator has reached capacity
     * 
     * @param statements - xAPI Statements to save to LRS
     * @param forceSave - when true, save xAPI Statements regardless of Statement Accumulator size
     */
    public void saveStatements(List<Statement> statements, boolean forceSave) {
        if(statements == null) {
            throw new IllegalArgumentException("Statements can not be null!");
        }
        stmtCache.recordStatements(statements);
        if(forceSave) {
            saveStatements();
        } else {
            if(stmtCache.isAccumulatorFull()) {
                saveStatements();   
            }
        }
    }
    
    /**
     * Update map of Agent : LeanrerStateAttributeNameEnum stored within LsaCache
     * 
     * @param statements - Collection of xAPI Statements
     * @param actorSlug - user name used to create Agent
     * 
     * @throws LmsXapiAgentException when unable to create Agent from actorSlug
     */
    public void cacheAgentProfileData(List<Statement> statements, String actorSlug) {
        if(statements == null) {
            throw new IllegalArgumentException("Statements can not be null!");
        }
        if(actorSlug == null) {
            throw new IllegalArgumentException("actorSlug can not be null!");
        }
        Agent actor = null;
        try {
            actor = PersonaHelper.createMboxAgent(actorSlug);
        } catch (LmsXapiAgentException e) {
            logger.error("Unable to create Agent from: "+actorSlug, e);
        }
        lsaCache.populateAgentProfileAccum(statements, actor);
    }
    
    /**
     * parses AgentProfileData from LRS and converts to LeanrerStateAttributeNameEnum(s)
     * 
     * @param actorSlug - user name for Agent to fetch data for
     * 
     * @return never null, possibly empty set of LearnerStateAttributeNameEnum
     * 
     * @throws LmsXapiQueryException when unable to create Agent or get data from LRS
     */
    public Set<LearnerStateAttributeNameEnum> getAgentProfileData(Agent actor) throws LmsXapiQueryException {
        if(actor == null) {
            throw new IllegalArgumentException("actor can not be null!");
        }
        Set<LearnerStateAttributeNameEnum> lsaNames = new HashSet<>();
        Set<String> set = lsaCache.getAgentLearnerStateAttributes(actor);
        if(CollectionUtils.isNotEmpty(set)) {
            for(String lsaName : set) {
                LearnerStateAttributeNameEnum lsa = LearnerStateAttributeNameEnum.valueOf(lsaName);
                lsaNames.add(lsa);
            }
        }
        return lsaNames;
    }
    
    /**
     * Creates xAPI Statement resource query from supplied parameters and executes.
     * 
     * @param agent - return Statements for which the specified Agent or Group is the Actor or Object of the Statement
     * @param expandAgents - Apply the Agent filter broadly. Include Statements for which the Actor, Object, Authority, Instructor,
     *                       Team or any of these properties in a contained SubStatement match the Agent parameter
     * @param activity - Filter, only return Statements for which the Object of the Statement is an Activity with the specified id
     * @param expandActivities - Apply the Activity filter broadly. Include Statements for which the Object, any of the context Activities,
     *                           or any of those properties in a contained SubStatement match the Activity parameter
     * @param verb - Filter, only return Statements matching the specified Verb id
     * @param registration - Filter, only return Statements matching the specified registration id
     * @param since - Only Statements stored since the specified timestamp (exclusive) are returned
     * @param until - Only Statements stored at or before the specified timestamp are returned
     * @param limit - Maximum number of Statements to return. 0 indicates return the maximum the server will allow
     * @param isAscending - if true, return results in ascending order of stored time
     * @param format - QueryResultFormat corresponding to 'ids', 'exact' or 'canonical'
     * 
     * @return Statements Query configured with non-null parameters
     * 
     * @throws LmsXapiQueryException when unable to set a non-null parameter
     */
    public List<Statement> query(Agent agent, Boolean expandAgents, Activity activity, Boolean expandActivities, Verb verb,
            UUID registration, DateTime since, DateTime until, Integer limit, Boolean isAscending, QueryResultFormat format) throws LmsXapiQueryException {
        StatementsQuery q = stmtQuery.configureStatementQuery(agent, expandAgents, activity, expandActivities, verb, registration, since, until, limit, isAscending, format);
        return stmtQuery.query(q);
    }
    
    /**
     * Creates xAPI Statement resource query from supplied parameters and executes.
     * 
     * @param statements - collection of xAPI Statements to add query results to
     * @param agent - return Statements for which the specified Agent or Group is the Actor or Object of the Statement
     * @param expandAgents - Apply the Agent filter broadly. Include Statements for which the Actor, Object, Authority, Instructor,
     *                       Team or any of these properties in a contained SubStatement match the Agent parameter
     * @param activity - Filter, only return Statements for which the Object of the Statement is an Activity with the specified id
     * @param expandActivities - Apply the Activity filter broadly. Include Statements for which the Object, any of the context Activities,
     *                           or any of those properties in a contained SubStatement match the Activity parameter
     * @param verb - Filter, only return Statements matching the specified Verb id
     * @param registration - Filter, only return Statements matching the specified registration id
     * @param since - Only Statements stored since the specified timestamp (exclusive) are returned
     * @param until - Only Statements stored at or before the specified timestamp are returned
     * @param limit - Maximum number of Statements to return. 0 indicates return the maximum the server will allow
     * @param isAscending - if true, return results in ascending order of stored time
     * @param format - QueryResultFormat corresponding to 'ids', 'exact' or 'canonical'
     * 
     * @return Statements Query configured with non-null parameters
     * 
     * @throws LmsXapiQueryException when unable to set a non-null parameter
     */
    public List<Statement> query(List<Statement> statements, Agent agent, Boolean expandAgents, Activity activity, Boolean expandActivities, Verb verb,
            UUID registration, DateTime since, DateTime until, Integer limit, Boolean isAscending, QueryResultFormat format) throws LmsXapiQueryException {
        StatementsQuery q = stmtQuery.configureStatementQuery(agent, expandAgents, activity, expandActivities, verb, registration, since, until, limit, isAscending, format);
        return stmtQuery.query(q, statements);
    }
    
    /**
     * Class which handles periodic checking of LRS connection status
     * 
     * @author Yet Analytics
     *
     */
    private class LrsPoll {
        /** number of successful calls to about resource */
        private int successCount = 0;
        
        /** number of minutes to wait before next call to about resource */
        private int frequency = 1;
        
        /** execute scheduled checks to about resource */
        private ScheduledExecutorService scheduler;
        
        /** async */
        private ScheduledFuture<?> poller;
        
        /**
         * Initialize scheduler
         */
        private LrsPoll() {
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        
        /**
         * cancel ongoing asyn tasks and shut down scheduler
         */
        private void cleanup() {
            if(logger.isDebugEnabled()) {
                logger.debug("Shutting down LrsPoll!");
            }
            scheduler.shutdown();
            try {
                if(!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    scheduler.shutdownNow();
                } 
            } catch (InterruptedException e) {
                logger.error("Error while shutting down LRS API poll!", e);
                scheduler.shutdownNow();
            }
        }
        
        /**
         * Initialize periodic polling of LRS to ensure connection status
         */
        private void init() {
            // cancel previous future if exists
            if(poller != null && !poller.isCancelled()) {
                poller.cancel(true);
            }
            // determine period
            int period;
            if(successCount == 0) {
                period = frequency;
            } else {
                period = frequency * successCount;
            }
            if(logger.isDebugEnabled()) {
                logger.debug("LRS Poll next run in "+period+" minutes");
            }
            Runnable checkConn = new Runnable() {
                @Override
                public void run() {
                    if(isConnected()) {
                        setIsConnected(true);
                        successCount += 1;
                        if(logger.isDebugEnabled()) {
                            logger.debug("connected? "+true);
                            logger.debug("success Count: "+successCount);
                        }
                    } else {
                        setIsConnected(false);
                        successCount = 0;
                        logger.warn("LRS is not connected!");
                    }
                    // cycle after completion of previous scheduled run
                    if(poller != null) {
                        init();
                    }
                }
            };
            this.poller = scheduler.schedule(checkConn, period, TimeUnit.MINUTES);
        }
    }
    
    /**
     * Class which handles caching of xAPI Statements prior to send off to LRS and
     * caching of xAPI Statement id(s) which have been cached / send off to LRS
     * 
     * @author Yet Analytics
     *
     */
    private class StatementCache {
        /** Max number of Statements identifiers to store within previousStatementIds prior to eviction */
        private int statementIdCapacity;
        
        /** Statement identifiers sent to LRS */
        private Deque<UUID> previousStatementIds;
        
        /** Number of statements to accumulate before sending to LRS */
        private int statementAccumMaxSize;
        
        /** xAPI Statement accumulator */
        private List<Statement> statementAccum;
        
        /** xAPI Statement DLQ */
        private List<Statement> statementDLQ;
        
        /**
         * Initialize with default values
         */
        private StatementCache() {
            this(10000, 200);
        }
        
        /**
         * Initialize with provided values
         * 
         * @param stmtIdCap - number of ids to store before eviction happens
         * @param stmtAccumSize - number of statements to store before saving to LRS
         */
        private StatementCache(int stmtIdCap, int stmtAccumSize) {
            this.statementIdCapacity = stmtIdCap; 
            this.statementAccumMaxSize = stmtAccumSize;
            this.previousStatementIds =  new LinkedBlockingDeque<UUID>(statementIdCapacity);
            this.statementAccum = new ArrayList<Statement>();
            this.statementDLQ = new ArrayList<Statement>();
        }
        
        /**
         * Clear the previousStatementIds cache
         */
        private void clearStatementIdCache() {
            previousStatementIds.clear();
        }
        
        /**
         * Clear the statementAccum list
         */
        private void clearStatementAccumulator() {
            statementAccum.clear();
        }
        
        /**
         * Clear the rejected xAPI Statement list
         */
        private void clearStatementDLQ() {
            statementDLQ.clear();
        }
        
        /**
         * remove cached data
         */
        private void cleanup() {
            clearStatementIdCache();
            clearStatementAccumulator();
            if(logger.isDebugEnabled()) {
                logger.debug("discarding "+statementDLQ.size()+" statements from DLQ!");
            }
            clearStatementDLQ();
        }
        
        /**
         * Checks for presence of stmdId within previousStatementIds
         * 
         * @param stmtId - statement id to check
         * 
         * @return true if novel statement id, false otherwise
         */
        private boolean novelStatementId(UUID stmtId) {
            if(stmtId == null) {
                throw new IllegalArgumentException("statement id can not be null!");
            }
            return !previousStatementIds.contains(stmtId);
        }
        
        /**
         * Adds statements to accumulator and record id when valid to do so
         * 
         * @param statements - Collection of xAPI Statements
         */
        private void recordStatements(List<Statement> statements) {
            if(statements == null) {
                throw new IllegalArgumentException("statements can not be null!");
            }
            for(Statement stmt : statements) {
                if(stmt == null) {
                    logger.error("Unable to record null xAPI Statement!");
                } else if(stmt.getId() == null) {
                    logger.error("Unable to record the following xAPI Statement due to missing id! \n"+stmt.toJSON(true));
                } else if(novelStatementId(stmt.getId())) {
                    // add to accumulator
                    statementAccum.add(stmt);
                    // Statement Id capacity check
                    if(previousStatementIds.size() == statementIdCapacity) {
                        // Remove from head
                        previousStatementIds.pollFirst();
                    }
                    // Add to tail
                    previousStatementIds.offer(stmt.getId());
                }
            }
        }
        
        /**
         * Has the Statement Accumulator reached capacity
         * 
         * @return true if at or above capacity
         */
        private boolean isAccumulatorFull() {
            return statementAccum.size() >= statementAccumMaxSize;
        }
        
        /**
         * @return List of cached xAPI Statement(s)
         */
        private List<Statement> getAccumulator() {
            return statementAccum;
        }
        
        /**
         * @return List of rejected xAPI Statement(s)
         */
        private List<Statement> getDLQ(){
            return statementDLQ;
        }
    }
    
    /**
     * Class which handles interactions with LRS's agent profile resource in order to store
     * Agent : LSA mappings derived from xAPI Statements generated from Learner State messages 
     * 
     * @author Yet Analytics
     *
     */
    private class LsaCache {
        // TODO: the course in which the LSA : Agent pairing happened is not considered.
        // i.e., Learner State about LSA within Course 1 not distinguished from Learner State about LSA within Course 2
        
        /** Actor : Learner State Attributes - Agent Profile Resource */
        private Map<Agent, Set<LearnerStateAttributeNameEnum>> actorLsaMap;
        
        /** key within JSON Agent Profile at which data is stored */
        private static final String ATTRIBUTES = "attributes";
        
        /** LSA Cache Agent Profile */
        private ItsAgentProfileConcepts.LsaCache cacheConcept;
        
        /** Verb within xAPI Statements that contain an Actor : LSA mapping */
        private ItsVerbConcepts.Predicted verb;
        
        /** Activity Type for LSA Activities */
        private ItsActivityTypeConcepts.Lsa lsaAt;
        
        /**
         * Initialize cache and xAPI Profile Concepts
         * 
         * @throws LmsXapiQueryException when unable to parse xAPI Profile concept from xAPI Profile
         */
        private LsaCache() throws LmsXapiQueryException {
            this.actorLsaMap = new HashMap<Agent, Set<LearnerStateAttributeNameEnum>>();
            try {
                this.cacheConcept = ItsAgentProfileConcepts.LsaCache.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiQueryException("Unable to initialize LsaCache Agent Profile Concept!", e);
            }
            try {
                this.verb = ItsVerbConcepts.Predicted.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiQueryException("Unable to initialize Prediced Verb Concept!", e);
            }
            try {
                this.lsaAt = ItsActivityTypeConcepts.Lsa.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiQueryException("Unable to initialize Learner State Attribute Activity Type Concept!", e);
            }
        }
        
        /**
         * Clears cache of Agent : LSA(s)
         */
        private void cleanup() {
            actorLsaMap.clear();
        }
        
        /**
         * Iterate over key value pairs within actorLsaMap and create or update AgentProfileDocument
         * within LRS. Removes key value pair from actorLsaMap upon successful updating of LRS
         */
        private void saveCachedData() {
            if(!actorLsaMap.isEmpty()) {
                for(Map.Entry<Agent, Set<LearnerStateAttributeNameEnum>> kv : actorLsaMap.entrySet()) {
                    boolean success = saveAgentProfileDocument(kv.getKey(), kv.getValue());
                    if(success) {
                        actorLsaMap.remove(kv.getKey(), kv.getValue());
                    }
                }
            }
        }
        
        /**
         * Parses learner state attribute from statements and updates actorLsaMap
         * 
         * @param statements - collection of statements to parse learner state attributes from
         * @param actor - Agent used to filter xAPI Statements and used as Key within Agent : LSA cache
         */
        private void populateAgentProfileAccum(List<Statement> statements, Agent actor) {
            if(statements == null) {
                throw new IllegalArgumentException("statements can not be null!");
            }
            if(actor != null) {
                Set<LearnerStateAttributeNameEnum> lsaNames = new HashSet<LearnerStateAttributeNameEnum>();
                // Only care about Raw LSA statements
                String targetVerbId = verb.getId().toString();
                String targetObjectType = lsaAt.getId().toString();
                // Determine LSAs to add to the Agent Profile
                for(Statement stmt : statements) {
                    // Filter down to relevant statements
                    if(CollectionUtils.isEmpty(ContextActivitiesHelper.getOtherActivities(stmt.getContext())) &&
                            stmt.getVerb().getId().toString().equals(targetVerbId) &&
                            ((Activity) stmt.getObject()).getDefinition().getType().toString().equals(targetObjectType) &&
                            stmt.getActor().equals(actor)) {
                        // Parse out LSA from statement object
                        Activity lsa = (Activity) stmt.getObject();
                        LearnerStateAttributeNameEnum lsaEnum =
                                LearnerStateAttributeNameEnum.valueOf(lsa.getDefinition().getName().get(LanguageTagEnum.EN_US.getValue()));
                        lsaNames.add(lsaEnum);
                    }
                }
                // Populate Agent Profile accumulator
                Set<LearnerStateAttributeNameEnum> existing = actorLsaMap.get(actor);
                if(CollectionUtils.isEmpty(existing)) {
                    actorLsaMap.put(actor, lsaNames);
                } else {
                    existing.addAll(lsaNames);
                    actorLsaMap.put(actor, existing);
                }
            }
        }
        
        /**
         * Query for LSA Agent Profile for passed in Agent.
         *  
         * @param agent - Agent to search for
         * 
         * @return Possibly empty but never null set of strings
         * 
         * @throws LmsXapiQueryException when unable to parse Agent Profile
         */
        private Set<String> getAgentLearnerStateAttributes(Agent agent) throws LmsXapiQueryException {
            if(agent == null) {
                throw new IllegalArgumentException("agent can not be null!");
            }
            Set<String> coll;
            if(isConnected) {
                AgentProfileLRSResponse resp = retrieveAgentProfile(cacheConcept.getId().toString(), agent);
                handleResponseStatus(resp);
                AgentProfileDocument doc = resp.getContent();
                coll = parseAgentProfileDocument(doc);
            } else {
                coll = new HashSet<String>();
            }
            return coll;
        }
        
        /**
         * Creates or updates an existing Agent Profile. 
         * 
         * Agent Profile tracks the Learner State Attributes the Agent has xAPI Statements for
         * 
         * @param agent - Actor Agent from Learner State xAPI Statement(s)
         * @param data - LearnerStateAttributeNameEnum(s) parsed from Learner State xAPI Statement(s)
         * 
         * @throws LmsXapiQueryException when unable to parse / write JSON data stored by the Agent Profile
         */
        private boolean saveAgentProfileDocument(Agent agent, Set<LearnerStateAttributeNameEnum> data) {
            if(agent == null) {
                throw new IllegalArgumentException("agent can not be null!");
            }
            if(data == null) {
                throw new IllegalArgumentException("data can not be null!");
            }
            boolean success = false;
            if(isConnected) {
                // Check for existing Agent Profile Data
                AgentProfileLRSResponse resp = retrieveAgentProfile(cacheConcept.getId().toString(), agent);
                handleResponseStatus(resp);
                if(!resp.getSuccess()) {
                    return success;
                }
                AgentProfileDocument doc = resp.getContent();
                // xAPI Concurrency Controls
                // see the following for more information
                // -> 'https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#concurrency'
                // -> 'https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#26-agent-profile-resource'
                String eTag;
                if(doc == null || doc.getContent() == null) {
                    eTag = resp.getResponse().getEtag();
                    AgentProfileDocument document;
                    try {
                        document = createAgentProfileDocument(agent, data);
                    } catch (LmsXapiQueryException e) {
                        logger.error("Unable to create Agent Profile Document!", e);
                        return success;
                    }
                    document.setEtag(eTag);
                    if(isConnected) {
                        LRSResponse updateresp = updateAgentProfile(document);
                        handleResponseStatus(resp);
                        success = updateresp.getSuccess();
                    }
                } else {
                    eTag = doc.getEtag();
                    try {
                        updateAgentProfileDocument(doc, data);
                    } catch (LmsXapiQueryException e) {
                        logger.error("Unable to update Agent Profile Document!", e);
                        return success;
                    }
                    doc.setEtag(eTag);
                    if(isConnected) {
                        LRSResponse updateresp = updateAgentProfile(doc);
                        handleResponseStatus(resp);
                        success = updateresp.getSuccess();
                    }
                }
            }
            return success;
        }
        
        /**
         * Convert Set of Strings into JSON object stored within Agent Profile
         * 
         * @param data - Set of Strings to convert into JSON object
         * 
         * @return JSON object with key ATTRIBUTES and value of data as an array
         */
        private ObjectNode createAgentProfileJson(Set<String> data) {
            if(data == null) {
                throw new IllegalArgumentException("data can not be null!");
            }
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode coll = mapper.createArrayNode();
            ObjectNode jdata = mapper.createObjectNode();
            for(String item : data) {
                coll.add(item);
            }
            jdata.set(ATTRIBUTES, coll);
            return jdata;
        }
        
        /**
         * Create and configure AgentProfileDocument
         * 
         * @param agent - Agent associated with the document
         * @param data - Set of LearnerStateAttributeNameEnum to write to LRS
         * 
         * @return AgentProfileDocument to write to LRS
         * 
         * @throws LmsXapiQueryException when unable to convert data into JSON
         */
        private AgentProfileDocument createAgentProfileDocument(Agent agent, Set<LearnerStateAttributeNameEnum> data) throws LmsXapiQueryException {
            if(agent == null) {
                throw new IllegalArgumentException("agent can not be null!");
            }
            if(data == null) {
                throw new IllegalArgumentException("data can not be null!");
            }
            AgentProfileDocument document = new AgentProfileDocument();
            document.setAgent(agent);
            document.setId(cacheConcept.getId().toString());
            Set<String> set = new HashSet<String>();
            for(LearnerStateAttributeNameEnum lsa : data) {
                set.add(lsa.toString());
            }
            ObjectNode jdata = createAgentProfileJson(set);
            try {
                document.setContent(jdata.toString().getBytes(CommonLrsEnum.ENCODING.getValue()));
            } catch (UnsupportedEncodingException e) {
                throw new LmsXapiQueryException("Unable to encode json data as document content!", e);
            }
            document.setContentType(cacheConcept.getContentType());
            return document;
        }
        
        /**
         * Given an Agent Profile, return the set of Learner State Attribute Name Strings
         * 
         * @param profile - AgentProfileDocument
         * 
         * @return set of Learner State Attribute Names stored in the Agent Profile Document
         * 
         * @throws LmsXapiQueryException when unable to parse Agent Profile data
         */
        private Set<String> parseAgentProfileDocument(AgentProfileDocument profile) throws LmsXapiQueryException {
            Set<String> set;
            if(profile != null && profile.getContent() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode existing;
                try {
                    existing = mapper.readTree(new String (profile.getContent()));
                } catch (IOException e) {
                    throw new LmsXapiQueryException("Uanble to parse Agent Profile data!", e);
                }
                ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {});
                try {
                    set = new HashSet<String>(reader.readValue(existing.findValue(ATTRIBUTES)));
                } catch (IOException e) {
                    throw new LmsXapiQueryException("Unable to parse Agent Profile attributes into set!", e);
                }
            } else {
                set = new HashSet<String>();
            }
            return set;
        }
        
        /**
         * Update and configure existing AgentProfileDocument
         * 
         * @param document - AgentProfileDocument to update
         * @param data - Set of LearnerStateAttributeNameEnum to merge with Agent Profile Document data
         * 
         * @throws LmsXapiQueryException when unable to parse existing JSON or create updated JSON
         */
        private void updateAgentProfileDocument(AgentProfileDocument document, Set<LearnerStateAttributeNameEnum> data) throws LmsXapiQueryException {
            if(document == null) {
                throw new IllegalArgumentException("document can not be null!");
            }
            if(data == null) {
                throw new IllegalArgumentException("data can not be null!");
            }
            Set<String> set = parseAgentProfileDocument(document);
            // Compare to passed in data
            for(LearnerStateAttributeNameEnum lsaName : data) {
                set.add(lsaName.toString());
            }
            ObjectNode jdata = createAgentProfileJson(set);
            try {
                document.setContent(jdata.toString().getBytes(CommonLrsEnum.ENCODING.getValue()));
            } catch (UnsupportedEncodingException e) {
                throw new LmsXapiQueryException("Unable to set data as document content!", e);
            }
            document.setContentType(cacheConcept.getContentType());
        }
    }
    
    /**
     * Class which handles LRS Statement Query configuration and execution
     * 
     * @author Yet Analytics
     *
     */
    private class LrsQuery {
        
        /** Max number of statements to return from LRS query */
        private int statementQueryBatchMax;
        
        /**
         * Initialize with default values
         */
        private LrsQuery() {
            this(100);
        }
        
        /**
         * Initialize with provided values
         * 
         * @param maxBatchSize - default max number of xAPI Statements to return in Statements Query
         */
        private LrsQuery(int maxBatchSize) {
            this.statementQueryBatchMax = maxBatchSize;
        }
        
        /**
         * Processes the statements result and request further statements if pagination is
         * required.
         * 
         * @param result - the result of a statement query.
         * @param statements - the list of statements to populate with the results.
         *
         * @throws LmsXapiQueryException - when unable to fetch additional statements
         */
        private void processStatementsResult(StatementsResult result, List<Statement> statements) {
            if(result != null && result.getStatements() != null && statements != null) {
                statements.addAll(result.getStatements());
                // Check for more
                if(result.getMoreURL() != null && !result.getMoreURL().isEmpty() && isConnected) {
                    // Recursively request the next chunk of statements
                    StatementsResultLRSResponse resp = moreStatements(result.getMoreURL());
                    StatementsResult moreResult = resp.getContent();
                    handleResponseStatus(resp);
                    processStatementsResult(moreResult, statements);                    
                }
            }
        }
        
        /**
         * Executes provided StatementQuery - following all more links - and returns statements
         * 
         * @param q - StatementsQuery to execute against this
         * 
         * @return xAPI Statements from the Target LRS (this) that match the StatementsQuery
         * 
         * @throws LmsXapiQueryException - when unable to fetch statements
         */
        private List<Statement> query(StatementsQuery q) throws LmsXapiQueryException {
            if(q == null) {
                throw new IllegalArgumentException("Statement Query can not be null!");
            }
            List<Statement> statements = new ArrayList<Statement>();
            if(isConnected) {
                return query(q, statements);
            } else {
                return statements;
            }   
        }
        
        /**
         * Executes provided StatementQuery - following all more links - and returns statements
         * 
         * @param q - StatementsQuery to execute against this
         * @param statements - Possibly empty collection of Statements
         * 
         * @return xAPI Statements from the Target LRS (this) that match the StatementsQuery
         * 
         * @throws LmsXapiQueryException - when unable to fetch statements or passed in statements is null
         */
        private List<Statement> query(StatementsQuery q, List<Statement> statements) {
            if(statements == null) {
                throw new IllegalArgumentException("Statements must not be null!");
            }
            if(q == null) {
                throw new IllegalArgumentException("Statement Query can not be null!");
            }
            if(isConnected) {
                StatementsResultLRSResponse result = queryStatements(q);
                handleResponseStatus(result);
                processStatementsResult(result.getContent(), statements);
            }
            return statements;
        }
        
        /**
         * Creates xAPI Statement resource query from supplied parameters
         * 
         * @param agent - return Statements for which the specified Agent or Group is the Actor or Object of the Statement
         * @param expandAgents - Apply the Agent filter broadly. Include Statements for which the Actor, Object, Authority, Instructor,
         *                       Team or any of these properties in a contained SubStatement match the Agent parameter
         * @param activity - Filter, only return Statements for which the Object of the Statement is an Activity with the specified id
         * @param expandActivities - Apply the Activity filter broadly. Include Statements for which the Object, any of the context Activities,
         *                           or any of those properties in a contained SubStatement match the Activity parameter
         * @param verb - Filter, only return Statements matching the specified Verb id
         * @param registration - Filter, only return Statements matching the specified registration id
         * @param since - Only Statements stored since the specified timestamp (exclusive) are returned
         * @param until - Only Statements stored at or before the specified timestamp are returned
         * @param limit - Maximum number of Statements to return. 0 indicates return the maximum the server will allow
         * @param isAscending - if true, return results in ascending order of stored time
         * @param format - QueryResultFormat corresponding to 'ids', 'exact' or 'canonical'
         * 
         * @return Statements Query configured with non-null parameters
         * 
         * @throws LmsXapiQueryException when unable to set a non-null parameter
         */
        private StatementsQuery configureStatementQuery(Agent agent, Boolean expandAgents, Activity activity, Boolean expandActivities, Verb verb,
                UUID registration, DateTime since, DateTime until, Integer limit, Boolean isAscending, QueryResultFormat format) throws LmsXapiQueryException {
            StatementsQuery q = new StatementsQuery();
            if(agent != null) {
                q.setAgent(agent);
            }
            if(expandAgents != null) {
                q.setRelatedAgents(expandAgents);
            }
            if(activity != null && activity.getId() != null) {
                q.setActivityID(activity.getId());
            }
            if(expandActivities != null) {
                q.setRelatedActivities(expandActivities);
            }
            if(verb != null) {
                try {
                    q.setVerbID(verb);
                } catch (URISyntaxException e) {
                    throw new LmsXapiQueryException("unable to set verb id!", e);
                }
            }
            if(registration != null) {
                q.setRegistration(registration);
            }
            if(since != null) {
                q.setSince(since);
            }
            if(until != null) {
                q.setUntil(until);
            }
            if(limit != null) {
                q.setLimit(limit);
            } else {
                q.setLimit(statementQueryBatchMax);
            }
            if(isAscending != null) {
                q.setAscending(isAscending);
            }
            if(format != null) {
                q.setFormat(format);
            }
            return q;
        }
    }
}
