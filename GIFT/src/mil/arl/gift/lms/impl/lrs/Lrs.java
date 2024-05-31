/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.lrs;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Extensions;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Score;
import com.rusticisoftware.tincan.Statement;
import com.rusticisoftware.tincan.Verb;
import generated.course.Concepts;
import generated.lms.Parameters;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.AbstractCourseRecordRefId;
import mil.arl.gift.common.course.CourseRecordRef.UUIDCourseRecordRefIds;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.ConceptStateRecord;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.SurveyScale;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.AbstractLms;
import mil.arl.gift.lms.impl.common.Assessment;
import mil.arl.gift.lms.impl.common.LmsDomainSessionException;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStatementException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.common.LmsXapiQueryException;
import mil.arl.gift.lms.impl.common.LmsXapiUUIDException;
import mil.arl.gift.lms.impl.lrs.query.LrsApi;
import mil.arl.gift.lms.impl.lrs.query.StatementQueryPostProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.CourseRecordHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.TimestampHelper;
import mil.arl.gift.lms.impl.lrs.xapi.UUIDHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.CourseRecordActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.LearnerStateAttributeActivity;
import mil.arl.gift.lms.impl.lrs.xapi.activity.SurveyActivity;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.ClosedDomainSessionGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.LessonCompletedGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.StartedDomainSessionGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.StartedKnowledgeSessionGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.processor.CourseRecordInvalidationProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.processor.CourseRecordProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.processor.EnvironmentAdaptationProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.processor.LearnerStateInvalidationProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.processor.LearnerStateProcessor;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts.extensionObjectKeys;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomVerbConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.server.Client;
import mil.arl.gift.net.api.message.DomainSessionMessage;

/**
 * This class provides methods for accessing an LRS and conversion of GIFT data structures to xAPI Statements.
 * 
 * @author cnucci, sharrison, Yet Analytics
 */
public class Lrs extends AbstractLms {
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Lrs.class);
    
    /** LRS API */
    private LrsApi api;
    
    /** METRICS */
    private long globalStart;
    
    /** Concept name to ignore within Learner State Attribute query */
    private static final String allConcepts = "all concepts";
    
    /**
     * Default constructor.
     */
    public Lrs() {}
    
    @Override
    public void connect(Parameters parameters) throws LmsIoException {
        this.parameters = parameters;
        // profile server initialization
        try {
            Client.getClient(parameters);
        } catch (LmsXapiProfileException e) {
            // this doesn't prevent any logic from work as the local profile is used instead
            logger.warn("Unable to initialize ADL profile server connection from parameters!", e);
        }
        try {
            this.api = prepLrsApi();
        } catch (LmsXapiQueryException e) {
            throw new LmsIoException("Unable to connect to LRS", e);
        }
        // Poll LRS About resource for connection check
        api.startPoll();
    }

    @Override
    public void disconnect() throws LmsIoException {
        this.api.cleanup();
    }

    @Override
    public void createUser(String userId) throws LmsInvalidStudentIdException, LmsIoException {
    }

    @Override
    public boolean isUserSessionSupported(UserSession userSession) {
        /* Session is only supported if it is NOT an experiment */
        return StringUtils.isBlank(userSession.getExperimentId());
    }

    @Override
    public LMSConnectionInfo getConnectionInfo() {
        if(connectionInfo == null) {
            connectionInfo = new LMSConnectionInfo(getName());
        }
        return connectionInfo;
    }
    
    /**
     * Uses parameters to initialize LRS API
     * 
     * @return LrsApi
     * @throws LmsIoException when unable to connect to LRS
     */
    private LrsApi prepLrsApi() throws LmsXapiQueryException {
        String endpoint = parameters.getNetworkAddress();
        String lrsUser = parameters.getUsername();
        String lrsPass = parameters.getPassword();
        if(endpoint == null || lrsUser == null || lrsPass == null) {
            throw new IllegalArgumentException("LRS API requires endpoint, username and password!");
        }
        LrsApi api;
        try {
            api = new LrsApi(endpoint, lrsUser, lrsPass);
        } catch (LmsException e) {
            throw new LmsXapiQueryException("unable to initialize LRS API!", e);
        }
        return api;
    }
    
    /**
     * Gets a list of all assessments from the LRS for the given user by user
     * name.
     * 
     * @param username
     *            The user name of the user whose assessments to retrieve
     * @throws LmsIoException
     *             Thrown if an exception occurs while connecting to the LRS
     */
    @Override
    public List<Assessment> getAssessments(String username) throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {

        List<Assessment> assessments = new ArrayList<Assessment>();

        //TODO: not implemented

        return assessments;
    }

    /**
     * Creates a formatted extension key with the supplied elements
     * @param keyPathValues the elements to combine into an extension key
     * @return the combined extension key
     */
    private String createExtensionKey(String... keyPathValues) {
        
        if(keyPathValues == null) {
            throw new IllegalArgumentException("keyPathValues must not be null");
        }
        
        String format = getExtensionFormat(keyPathValues.length);
        return String.format(format, (Object[]) keyPathValues);
    }
    
    /**
     * Creates the format string of a specific length used to produce an extension key
     * @param pathLength the number of elements in the format string's path, 
     * must be greater than 0
     * @return the format string of specified length
     */
    private String getExtensionFormat(int pathLength) {
        if(pathLength <= 0) {
            throw new IllegalArgumentException("pathLength must be a positive value");
        }
        
        StringBuilder sb = new StringBuilder("%s");
        for (int i = 0; i < pathLength - 1; i++) {
            sb.append("/%s");
        }
        
        return sb.toString();
    }
    
    @Override
    protected void insertSurveyResult(String studentId, int userId, int domainSessionID, double score, double maxScore,
            Date endTime, String surveyName, String courseName, Map<LearnerStateAttributeNameEnum, String> learnerStates, SubmitSurveyResults surveyResults) throws LmsIoException {
        // Attempt Agent creation
        Agent actor;
        try {
            actor = PersonaHelper.createMboxAgent(studentId);
        } catch (LmsXapiAgentException e) {
            throw new LmsIoException("Unable to create Actor Agent from studentId!", e);
        }
        Verb verb;
        try {
            verb = new Verb("http://adlnet.gov/expapi/verbs/responded");
        } catch (URISyntaxException e) {
            throw new LmsIoException("Unable to create Responded Verb!", e);
        }
        // Attempt Object Activity creation
        Activity activity;
        try {
            activity = new SurveyActivity(surveyName);
        } catch (LmsXapiActivityException e) {
            throw new LmsIoException("Unable to create Survey Activity from survey name!", e);
        }
        // Handle Result with possible extensions
        Result result = new Result();
        Score numericScore = new Score();
        numericScore.setMax(maxScore);
        // TODO: this will cause an error if the total score is negative. We need a 'calculate least total possible function' 
        numericScore.setMin(0.0);
        numericScore.setRaw(score);
        result.setScore(numericScore);
        result.setCompletion(true);
        int numExtensionsInserted = 0;
        if(learnerStates != null) {
            result.setExtensions(new Extensions());
            for(Entry<LearnerStateAttributeNameEnum, String> learnerStateEntry : learnerStates.entrySet()) {
                // We will never want to store any attributes if they are exclusive to concepts.
                // We cannot use these as universal records since one concept's attribute cannot be
                // used to evaluate another concept and the LRS cannot differentiate at this time.
                if(!learnerStateEntry.getKey().isExclusiveToConcepts()) {
                    String extensionKey = createExtensionKey(actor.getMbox(), learnerStateEntry.getKey().getName());
                    try {
                        result.getExtensions().put(extensionKey, learnerStateEntry.getValue());
                        numExtensionsInserted++;
                    } catch (URISyntaxException e) {
                        logger.error(extensionKey + " could not be parsed into a URI.", e);
                    }
                }
            }
        }
        // if there are no extensions, then we don't want to insert a blank result. Exit without
        // pushing to the LRS.
        if (numExtensionsInserted == 0) {
            return;
        }
        // Create the context with the platform of GIFT
        Context context = new Context();
        context.setPlatform("GIFT");
        // Attempt Parent Activity ID formation
        Activity parentActivity;
        try {
            parentActivity = new DomainActivity(courseName);
        } catch (LmsXapiActivityException e) {
            throw new LmsIoException("Unable to create Domain Activity from course name!", e);
        }
        ContextActivitiesHelper.addParentActivity(parentActivity, context);
        Statement stmt = new Statement(actor, verb, activity, result, context);
        stmt.setTimestamp(new DateTime(endTime.getTime()));
        stmt.setId(UUID.randomUUID());
        List<Statement> statementListForSaveStatements = new ArrayList<Statement>();
        statementListForSaveStatements.add(stmt);
        try {
            api.saveStatements(statementListForSaveStatements, false);
        } catch (Exception e) {
            String reason = e.getMessage();
            if(StringUtils.isBlank(reason)){
                reason = "an exception was thrown on the server.";
            }
            throw new LmsIoException("Unable to save the LRS statement because "+reason+".", e);
        }
    }
    
    @Override
    public List<AbstractScale> getLearnerStateAttributes(String username, Set<String> courseConcepts, Date sinceWhen) 
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {
        List<AbstractScale> learnerStatesAttributes = new ArrayList<AbstractScale>();
        // Keep track of LSAs collected thus far so these LSAs can be ignored in some queries.
        Set<LearnerStateAttributeNameEnum> collectedAttrs = new HashSet<>();
        
        // METRICS - log timing of queries + post-processing
        long conceptsTimeMs, nonconceptsTimeMs, conceptProcessingStart, conceptProcessingEnd, conceptQueryStart, conceptQueryEnd;
        long start = System.currentTimeMillis();
        
        // Agent query parameter
        Agent user;
        try {
            user = PersonaHelper.createMboxAgent(username);
        } catch (LmsXapiAgentException e) {
            throw new LmsInvalidStudentIdException("Unable to create Agent from username!", e);
        }
        // Since query parameter
        DateTime since = null;
        if(sinceWhen != null) {
            since = new DateTime(sinceWhen);
        }
        // Verb query parameter
        Verb verb;
        try {
            verb = ItsVerbConcepts.Predicted.getInstance().asVerb();
        } catch (LmsXapiProfileException e) {
            throw new LmsIoException("Unable to initialize Verb used within LRS Query!", e);
        }
        // Result Extension concept
        ItsResultExtensionConcepts.AttributeMeasure extConcept;
        try {
            extConcept = ItsResultExtensionConcepts.AttributeMeasure.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsIoException("Unable to initialize Attribute Measure Result Extension Concept!", e);
        }
        
        // METRICS
        start = System.currentTimeMillis();
        
        // Handle LSA associated with course concepts
        if(courseConcepts != null){
            // Iterate over courseConcepts
            for(String concept : courseConcepts) {
                // NOTE: the "all concepts" concept is ignored
                if(concept.equals(allConcepts)) {
                    continue;
                }
                // Accumulator
                List<Statement> statements;
                // Activity query parameter
                Activity activity;
                try {
                    activity = new AssessmentActivity(concept);
                } catch (LmsXapiActivityException e) {
                    throw new LmsInvalidCourseRecordException("Unable to create Activity id from courseConcept!", e);
                }
                // METRICS
                conceptQueryStart = System.currentTimeMillis();
                // Execute
                try {
                    statements = api.query(user, null, activity, true, verb, null, since, null, null, null, null);
                } catch (LmsXapiQueryException e) {
                    throw new LmsIoException("Unable to configure and execute LRS Statements query!", e);
                }
                // METRICS
                conceptQueryEnd = System.currentTimeMillis() - conceptQueryStart;
                conceptProcessingStart = System.currentTimeMillis();
                
                // Post Process when results
                if(CollectionUtils.isNotEmpty(statements)) {
                    // Group by LSA
                    Map<LearnerStateAttributeNameEnum, List<Statement>> byLsa = new HashMap<LearnerStateAttributeNameEnum, List<Statement>>();
                    for(Statement stmt : statements) {
                        Activity lsa = (Activity) stmt.getObject();
                        LearnerStateAttributeNameEnum lsaEnum =
                                LearnerStateAttributeNameEnum.valueOf(lsa.getDefinition().getName().get(LanguageTagEnum.EN_US.getValue()));
                        List<Statement> coll = byLsa.get(lsaEnum) != null ? byLsa.get(lsaEnum) : new ArrayList<Statement>();
                        coll.add(stmt);
                        byLsa.put(lsaEnum, coll);   
                    }
                    // Use statement data to create ConceptStateRecord(s)
                    for(Entry<LearnerStateAttributeNameEnum, List<Statement>> kv : byLsa.entrySet()) {
                        List<Statement> lsaStatements = kv.getValue();
                        StatementQueryPostProcessor.sortByTimestamp(lsaStatements);
                        for(Statement lsaStmt : lsaStatements) {
                            Extensions resultExt = lsaStmt.getResult().getExtensions();
                            if(resultExt == null) {
                                // Attempt with next statement
                                continue;
                            }
                            ObjectNode ext = (ObjectNode) extConcept.parseFromExtensions(resultExt);
                            if(ext == null) {
                                // Attempt with next statement
                                continue;
                            }
                            String assessment = ext.get(extensionObjectKeys.SHORT_TERM.getValue()).get(extensionObjectKeys.ASSESSMENT.getValue()).asText();
                            AbstractEnum assessmentEnum;
                            try {
                                assessmentEnum = kv.getKey().getAttributeValue(assessment);
                            } catch (@SuppressWarnings("unused") EnumerationNotFoundException e) {
                                // Attempt with next statement
                                continue;
                            }
                            // Handle non authored values
                            if(!kv.getKey().getAttributeAuthoredValues().contains(assessmentEnum)) {
                                assessmentEnum = kv.getKey().getAttributeAuthoredValues().get(0);
                            }
                            // Create item and add to return collection
                            ConceptStateRecord record = new ConceptStateRecord(concept, kv.getKey(), assessmentEnum);
                            record.setTimeStamp(lsaStmt.getTimestamp().toDate());
                            learnerStatesAttributes.add(record);
                            collectedAttrs.add(kv.getKey());
                            // Move on to next Entry
                            break;
                        }
                    }
                }
                
                // METRICS
                conceptProcessingEnd = System.currentTimeMillis() - conceptProcessingStart;
                if(logger.isTraceEnabled()){
                    logger.trace("METRICS: getLearnerStateAttributes - course CONCEPT "+concept+" Query: "+conceptQueryEnd+" ms");
                    logger.trace("METRICS: getLearnerStateAttributes - course CONCEPT "+concept+ " processing: "+conceptProcessingEnd+" ms");
                }
            }
        }
        
        // METRICS
        conceptsTimeMs = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        
        // Lookup LSA user has statements for
        Set<LearnerStateAttributeNameEnum> expectedLsa;
        try {
            expectedLsa = api.getAgentProfileData(user);
        } catch (LmsXapiQueryException e) {
            logger.warn("Unable to fetch previous Learner State Attributes for user!", e);
            // Unable to derive list of LSAs, initialize empty collection
            expectedLsa = new HashSet<LearnerStateAttributeNameEnum>();
        }
        List<LearnerStateAttributeNameEnum> source;
        // When no LSAs have been saved for the User
        if(CollectionUtils.isEmpty(expectedLsa)) {
            // Handle cold start w/ historical data
            source = LearnerStateAttributeNameEnum.VALUES();
        } else {
            source = new ArrayList<LearnerStateAttributeNameEnum>(expectedLsa);
        }
        // Handle LSA which are not associated with course concepts
        for(LearnerStateAttributeNameEnum lsaEnum : source) {
            // Has the LSA already been found with an associated course concept?
            if(collectedAttrs.contains(lsaEnum)){
                continue;
            }
            // Accumulator
            List<Statement> lsaOnlyStatements;
            // Activity
            Activity activity;
            try {
                activity = new LearnerStateAttributeActivity(lsaEnum.getDisplayName());
            } catch (LmsXapiActivityException e) {
                throw new LmsIoException("Unable to create LSA Activity and set ID within LRS Query!", e);
            }
            // Execute
            try {
                lsaOnlyStatements = api.query(user, null, activity, null, verb, null, since, null, null, null, null);
            } catch (LmsXapiQueryException e) {
                throw new LmsIoException("Unable to configure and execute LRS Statements query!", e);
            }
            
            // Find most recent valid statement
            if(CollectionUtils.isNotEmpty(lsaOnlyStatements)) {
                StatementQueryPostProcessor.sortByTimestamp(lsaOnlyStatements);
                for(Statement lsaOnlyStmt : lsaOnlyStatements) {
                    // Only process statements with associated concept
                    if(CollectionUtils.isEmpty(ContextActivitiesHelper.getOtherActivities(lsaOnlyStmt.getContext()))) {
                        // Parse statement
                        Extensions resultExt = lsaOnlyStmt.getResult().getExtensions();
                        if(resultExt == null) {
                            // Attempt with next statement
                            continue;
                        }
                        ObjectNode ext = (ObjectNode) extConcept.parseFromExtensions(resultExt);
                        if(ext == null) {
                            // Attempt with next statement
                            continue;
                        }
                        String assessment = ext.get(extensionObjectKeys.SHORT_TERM.getValue()).get(extensionObjectKeys.ASSESSMENT.getValue()).asText();
                        AbstractEnum assessmentEnum;
                        try {
                            assessmentEnum = lsaEnum.getAttributeValue(assessment);
                        } catch (@SuppressWarnings("unused") EnumerationNotFoundException e) {
                            // Attempt with next statement
                            continue;
                        }
                        // Handle non authored values
                        if(!lsaEnum.getAttributeAuthoredValues().contains(assessmentEnum)) {
                            assessmentEnum = lsaEnum.getAttributeAuthoredValues().get(0);
                        }
                        // Create item and add to return collection
                        SurveyScale scale = new SurveyScale(lsaEnum, assessmentEnum, 0.0);
                        scale.setTimeStamp(lsaOnlyStmt.getTimestamp().toDate());
                        learnerStatesAttributes.add(scale);
                        // Move on to next Entry
                        break;
                    }
                }
            }
        }
        
        // METRICS
        nonconceptsTimeMs = System.currentTimeMillis() - start;
        if(logger.isTraceEnabled()){
            logger.trace("METRICS: getLearnerStateAttributes - course concepts query+processing: "+conceptsTimeMs+" ms, other LSA query+processing: "+nonconceptsTimeMs+" ms");
        }
        
        return learnerStatesAttributes;
    }

    /**
     * When there is no User registered to a Domain Session within SessionManager, create the mapping, otherwise no-op
     * 
     * @param domainSessionId - domain session id to lookup user for
     * @param userId - fallback user id
     * @param domainId - name of the domain
     * 
     * @throws LmsDomainSessionException when there is already a domain session with the domain session id but its not associated with a user
     */
    private void handlePlaybackUserMapping(Integer domainSessionId, Integer userId, String domainId) throws LmsDomainSessionException {
        SessionManager sessionManager = SessionManager.getInstance();
        if(sessionManager.getUserIdForDomainSessionId(domainSessionId) == null) {
            // Create User Session from Id
            UserSession us = new UserSession(userId);
            // Check for presence of Domain Session
            DomainSession ds = sessionManager.getDomainSession(domainSessionId);
            if(ds == null) {
                // Create Domain Session
                ds = new DomainSession(domainSessionId, userId, domainId, domainId);
                // Mapping
                sessionManager.mapUserToDomainSession(us, ds);
                sessionManager.mapIdToDomainSession(domainSessionId, ds);
            } else {
                // Map to stored Domain Session
                sessionManager.mapUserToDomainSession(us, ds);
            }
        }
    }
    
    @Override
    protected CourseRecordRef insertCourseRecord(String studentId, int userId, int domainSessionId,
            LMSCourseRecord record, Concepts.Hierarchy concepts) throws LmsXapiProcessorException, LmsXapiQueryException {
        CourseRecordRef recordRef = record.getCourseRecordRef() != null ? record.getCourseRecordRef() : new CourseRecordRef();
        recordRef.setRef(new UUIDCourseRecordRefIds());
        List<Statement> statements = new ArrayList<Statement>();
        SessionManager sessionManager = SessionManager.getInstance();
        String domainId = record.getDomainName();
        // Handle Playback User
        try {
            handlePlaybackUserMapping(domainSessionId, userId, domainId);
        } catch (LmsDomainSessionException e) {
            throw new LmsXapiProcessorException("Playback User's Domain Session was already registered but not to any user!", e);
        }
        CourseRecordProcessor processor = new CourseRecordProcessor(record, concepts, domainId, domainSessionId, sessionManager.getKnowledgeSession());
        
        // METRICS
        long start, processingMs;
        start = System.currentTimeMillis();
        
        // xAPI Statements generation
        processor.process(statements);
        
        // METRICS
        processingMs = System.currentTimeMillis() - start;
        
        // Attempt Statement save
        if(CollectionUtils.isNotEmpty(statements)) {
            for(Statement stmt : statements) {
                if(stmt == null || stmt.getId() == null) {
                    logger.warn("Non-empty statement results contained null statement OR statement without id!");
                } else if(api.isNovelStatementId(stmt)) {
                    // Add each statement id to recordRef
                    ((UUIDCourseRecordRefIds)recordRef.getRef()).addRecordUUID(stmt.getId().toString());
                }
            }
            // METRICS
            if(logger.isTraceEnabled()) {
                logger.trace("METRICS: insertCourseRecord Processing took: "+processingMs+" ms");
            }
            // Send to LRS for storage - force save of statements + those already in statementAccum
            api.saveStatements(statements, true);
        }
        return recordRef;
    }

    @Override
    public LMSCourseRecord getCourseRecord(String studentId, int userId, CourseRecordRef recordRef)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {
        List<Statement> statements = new ArrayList<Statement>();
        // Single statement query or parameterized
        AbstractCourseRecordRefId ref = recordRef.getRef();
        if(ref instanceof UUIDCourseRecordRefIds) {
            List<String> uuids = ((UUIDCourseRecordRefIds) ref).getRecordUUIDs();
            if(uuids != null && CollectionUtils.isNotEmpty(uuids)) {
                for(String stmtId : uuids) {
                    if(api.getIsConnected()) {
                        Statement stmt = api.retrieveStatement(stmtId).getContent();
                        if(stmt != null) {
                            statements.add(stmt);
                        }
                    }
                }
            }
        } else {
            // Agent query parameter
            Agent actor;
            try {
                actor = PersonaHelper.createMboxAgent(studentId);
            } catch (LmsXapiAgentException e) {
                throw new LmsInvalidStudentIdException("unable to create Actor Agent from studentId!", e);
            }
            // Verb
            Verb v;
            try {
                v = MomVerbConcepts.Assessed.getInstance().asVerb();
            } catch (LmsXapiProfileException e) {
                throw new LmsIoException("unable to initialize Assessed Verb from the MOM xAPI Profile", e);
            }
            // Execute
            try {
                statements = api.query(actor, null, null, null, v, null, null, null, null, null, null);
            } catch (LmsXapiQueryException e) {
                throw new LmsIoException("unable to create scored skill assessment statements query!", e);
            }
            // Filter down to statements containing course record reference activity
            try {
                statements = StatementQueryPostProcessor.filterByCourseRecord(statements, recordRef);
            } catch (LmsXapiActivityException | LmsXapiProfileException e) {
                throw new LmsIoException("Unable to filter course records given the recordRef!", e);
            }
        }
        // Sort by statement event time
        StatementQueryPostProcessor.sortByTimestamp(statements);
        LMSCourseRecord record;
        try {
            record = CourseRecordHelper.deriveCourseRecord(statements, recordRef, getConnectionInfo());
        } catch (LmsXapiProcessorException e) {
            throw new LmsIoException("Unable to derive Course Record from statements!", e);
        }
        return record;
    }

    @Override
    public List<LMSCourseRecord> getCourseRecords(String studentId, int userId, List<CourseRecordRef> recordRefs)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {
        List<LMSCourseRecord> courseRecords = new ArrayList<LMSCourseRecord>();
        if(recordRefs != null && !recordRefs.isEmpty()) {
            for(CourseRecordRef recordRef : recordRefs) {
                LMSCourseRecord record = getCourseRecord(studentId, userId, recordRef);
                if(record != null) {
                    courseRecords.add(record);
                }
            }
        }
        return courseRecords;
    }

    @Override
    public List<LMSCourseRecord> getCourseRecords(String studentId, int userId, int pageStart, int pageSize,
            boolean sortDescending, Set<String> domainIds, Set<Integer> domainSessionIds) throws LmsIoException, LmsInvalidStudentIdException {
        // Accumulator
        List<LMSCourseRecord> courseRecords = new ArrayList<LMSCourseRecord>();
        // Common LRS query parameters
        Agent actor;
        try {
            actor = PersonaHelper.createMboxAgent(studentId);
        } catch (LmsXapiAgentException e) {
            throw new LmsIoException("unable to create Actor Agent from studentId!", e);
        }
        // Common xAPI Profile components
        Verb v;
        try {
            v = MomVerbConcepts.Assessed.getInstance().asVerb();
        } catch (LmsXapiProfileException e) {
            throw new LmsIoException("unable to initialize Assessed Verb from the MOM xAPI Profile", e);
        }
        ItsActivityTypeConcepts.CourseRecord courseRecordATC;
        try {
            courseRecordATC = ItsActivityTypeConcepts.CourseRecord.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsIoException("unable to initalize Course Record Activity Type Concept!", e);
        }
        // Statements accumulator
        List<Statement> statements;
        // Populate accumulator
        if(domainSessionIds == null) {
            return courseRecords;
        }
        for(Integer dsId : domainSessionIds) {
            try {
                statements = api.query(actor, null, null, null, v, 
                        UUIDHelper.createUUIDFromData(dsId.toString()),
                        null, null, pageSize, !sortDescending, null);
            } catch (LmsXapiQueryException | LmsXapiUUIDException e) {
                throw new LmsIoException("error while executing LRS query!", e);
            }
            // Post process statements
            Map<String, List<Statement>> recordToStmts = new HashMap<String, List<Statement>>();
            // Sort based on time stamp opposed to stored time
            if(sortDescending) {
                StatementQueryPostProcessor.sortByTimestamp(statements);
            }
            // Group by course record
            for(Statement stmt : statements) {
                List<Activity> courseRecordActivities;
                try {
                    courseRecordActivities = courseRecordATC.findInstancesInStatement(stmt);
                } catch (LmsInvalidStatementException e) {
                    throw new LmsIoException("Statement expected to contain course record activity was invalid!", e);
                }
                if(CollectionUtils.isNotEmpty(courseRecordActivities)) {
                    String activityId = courseRecordActivities.get(0).getId().toString();
                    List<Statement> batch = recordToStmts.get(activityId);
                    if(batch != null) {
                        batch.add(stmt);
                        recordToStmts.put(activityId, batch);
                    } else {
                        List<Statement> freshBatch = new ArrayList<Statement>();
                        freshBatch.add(stmt);
                        recordToStmts.put(activityId, freshBatch);
                    }
                }
            }
            // Create courseRecord per batch
            for(Map.Entry<String, List<Statement>> recordBatch : recordToStmts.entrySet()) {
                LMSCourseRecord record;
                List<Statement> stmts = recordBatch.getValue();
                List<Activity> courseRecordActivities;
                try {
                    courseRecordActivities = courseRecordATC.findInstancesInStatement(stmts.get(0));
                } catch (LmsInvalidStatementException e) {
                    throw new LmsIoException("Statement expected to contain course record activity was invalid!", e);
                }
                CourseRecordActivity courseRecordActivity = (CourseRecordActivity) courseRecordActivities.get(0);
                // Parse Course Record Reference components from activity
                Integer intId;
                try {
                    intId = Integer.parseInt(courseRecordActivity.parseActivityName());
                } catch (@SuppressWarnings("unused") NumberFormatException e) {
                    intId = null;
                }
                CourseRecordRef courseRecordRef;
                if(intId != null) {
                    courseRecordRef = CourseRecordRef.buildCourseRecordRefFromInt(intId);
                } else {
                    ArrayList<String> uuids = new ArrayList<String>();
                    String courseRecordDescription = courseRecordActivity.parseActivityDescription();
                    for(String uuid : courseRecordDescription.split(CommonLrsEnum.SEPERATOR_COMMA.getValue())) {
                        uuids.add(uuid);
                    }
                    courseRecordRef = new CourseRecordRef();
                    UUIDCourseRecordRefIds uuidRef = new UUIDCourseRecordRefIds();
                    uuidRef.setRecordUUIDs(uuids);
                    courseRecordRef.setRef(uuidRef);
                }
                // LMS course record
                try {
                    record = CourseRecordHelper.deriveCourseRecord(recordBatch.getValue(), courseRecordRef, getConnectionInfo());
                } catch (LmsXapiProcessorException e) {
                    throw new LmsIoException("error while reconstructing course record from xAPI Statements!", e);
                }
                if(record != null) {
                    courseRecords.add(record);
                }
            }
        }
        return courseRecords;
    }

    @Override
    public List<LMSCourseRecord> getLatestRootCourseRecordsPerDomain(String studentId, int userId, List<Integer> domainSessionIds)
            throws LmsIoException, LmsInvalidStudentIdException {
        return getCourseRecords(studentId, userId, 0, 500, true, new HashSet<String>(), new HashSet<Integer>(domainSessionIds));
    }
    
    @Override
    protected void insertLearnerState(String userName, int userId, int domainSessionId, LearnerState learnerState) {
        List<Statement> statements = new ArrayList<Statement>();
        SessionManager sessionManager = SessionManager.getInstance();
        String dId = sessionManager.getDomainId(domainSessionId);
        String storedUserName = sessionManager.getUserNameForDomainSessionId(domainSessionId);
        String actorSlug = storedUserName != null ? storedUserName : userName;
        // Handle Playback User
        try {
            handlePlaybackUserMapping(domainSessionId, userId, dId);
        } catch (LmsDomainSessionException e) {
            logger.error("Playback User's Domain Session was already registered but not to any user!", e);
        }
        LearnerStateProcessor processor = new LearnerStateProcessor(actorSlug, learnerState, dId, domainSessionId, sessionManager.getKnowledgeSession());
        // xAPI Statements generation
        try{
            processor.process(statements);
        } catch (LmsXapiProcessorException e) {
            logger.error("unable to process Learner State into xAPI Statements!", e);
        }
        
        // METRICS
        long start, processing;
        start = System.currentTimeMillis();
        
        // METRICS
        processing = System.currentTimeMillis() - start;
        
        // Attempt Statement save
        if(CollectionUtils.isNotEmpty(statements)) {
            api.cacheAgentProfileData(statements, actorSlug);
            api.saveStatements(statements, false);
        }
        
        // METRICS
        if(logger.isTraceEnabled()) {
            logger.trace("METRICS: insertLearnerState processing took: "+processing+" ms ");
        }
    }
    
    @Override
    public void pastSessionLearnerStateUpdated(AbstractKnowledgeSession knowledgeSession, DomainSession domainSession,
            LearnerState newLearnerState, LearnerState oldLearnerState) throws LmsIoException {
        List<Statement> statements = new ArrayList<Statement>();
        String dId = domainSession.getDomainRuntimeId();
        String octSlug =  knowledgeSession.getHostSessionMember().getUserSession().getUsername() != null ?
                knowledgeSession.getHostSessionMember().getUserSession().getUsername() :
                    knowledgeSession.getHostSessionMember().getSessionMembership().getUsername();
        // Handle Playback User within chain of custody extension
        try {
            handlePlaybackUserMapping(domainSession.getDomainSessionId(), domainSession.getUserId(), dId);
        } catch (LmsDomainSessionException e) {
            throw new LmsIoException("Playback User's Domain Session was already registered but not to any user!", e);
        }
        LearnerStateInvalidationProcessor processor =
                new LearnerStateInvalidationProcessor(newLearnerState, oldLearnerState, octSlug, dId,
                        domainSession.getDomainSessionId(), knowledgeSession);
        
        // METRICS
        long start, processing;
        start = System.currentTimeMillis();
        
        // xAPI Statements generation
        try {
            processor.process(statements);
        } catch (LmsXapiProcessorException e) {
            throw new LmsIoException("Unable to process updated learner state!", e);
        }
        
        // METRICS
        processing = System.currentTimeMillis() - start;
        if(logger.isTraceEnabled()) {
            logger.trace("METRICS: pastSessionLearnerStateUpdated processing took: "+processing+" ms");
        }
        
        // Attempt Statement save
        if(CollectionUtils.isNotEmpty(statements)) {
            api.clearStmtIdCache();
            api.saveStatements(statements, true);
        }
    }

    @Override
    public void pastSessionCourseRecordUpdated(AbstractKnowledgeSession knowledgeSession, AssessmentChainOfCustody chainOfCustody,
            LMSCourseRecord newCourseRecord, LMSCourseRecord oldCourseRecord, Concepts.Hierarchy concepts) throws LmsIoException {
        if(!newCourseRecord.getDomainName().equals(oldCourseRecord.getDomainName())) {
            throw new IllegalArgumentException("The Course Records are from two different courses!");
        }
        List<Statement> statements = new ArrayList<Statement>();
        String domainId = oldCourseRecord.getDomainName();
        // Handle Playback User
        try {
            handlePlaybackUserMapping(chainOfCustody.getDomainsessionid(), chainOfCustody.getUserid(), domainId);
        } catch (LmsDomainSessionException e) {
            throw new LmsIoException("Playback User's Domain Session was already registered but not to any user!", e);
        }
        CourseRecordInvalidationProcessor processor = 
                new CourseRecordInvalidationProcessor(oldCourseRecord, newCourseRecord, concepts, domainId,
                        chainOfCustody, knowledgeSession);
        // METRICS
        long start, processingMs;
        start = System.currentTimeMillis();
        
        // xAPI Statements generation
        try {
            processor.process(statements);
        } catch (LmsXapiProcessorException e) {
            throw new LmsIoException("Unable to process updated LMS Course Record!", e);
        }
        
        // METRICS
        processingMs = System.currentTimeMillis() - start;
        if(logger.isTraceEnabled()) {
            logger.trace("METRICS: pastSessionCourseRecordUpdated Processing took: "+processingMs+" ms");
        }
        
        if(CollectionUtils.isNotEmpty(statements)) {
            api.clearStmtIdCache();
            api.saveStatements(statements, true);
        }
        
        // handle course record reference update
        CourseRecordRef oldRef = oldCourseRecord.getCourseRecordRef() != null ? oldCourseRecord.getCourseRecordRef() : new CourseRecordRef();
        CourseRecordRef newRef = newCourseRecord.getCourseRecordRef() != null ? newCourseRecord.getCourseRecordRef() : new CourseRecordRef();
        CourseRecordHelper.updateCourseRecordRef(oldRef, newRef, statements);
    }

    @Override
    protected void insertPedagogicalRequest(String studentId, int userId, int domainSessionId,
            PedagogicalRequest pedagogicalRequest) {
        // Reason + list of AbstractPedagogicalRequests associated w/ the reason
    }

    @Override
    protected void insertDomainSessionInit(DomainSessionMessage message) throws LmsDomainSessionException {
        // METRICS
        globalStart = System.currentTimeMillis();
        
        // Record current domain session
        SessionManager sessionManager = SessionManager.getInstance();
        Integer domainSessionId = message.getDomainSessionId();
        UserSession userSession = message.getUserSession();
        InitializeDomainSessionRequest payload = (InitializeDomainSessionRequest) message.getPayload();
        String domainId = payload.getDomainCourseFileName();
        DomainSession ds = new DomainSession(domainSessionId, userSession.getUserId(), domainId, domainId);
        ds.copyFromUserSession(userSession);
        sessionManager.mapUserToDomainSession(userSession, ds);
        sessionManager.mapIdToDomainSession(domainSessionId, ds);
    }

    @Override
    protected void insertDomainSessionStarted(DomainSessionMessage message) throws LmsDomainSessionException {
        // From message
        UserSession userSession = message.getUserSession();
        Integer domainSessionId = message.getDomainSessionId();
        Integer userId = userSession.getUserId();
        // Currently stored within mapping
        SessionManager sessionManager = SessionManager.getInstance();
        Integer storedDomainSessionId = sessionManager.getCurrentDomainSessionId(userSession);
        Integer storedUserId = sessionManager.getUserIdForDomainSessionId(storedDomainSessionId);
        if(!domainSessionId.equals(storedDomainSessionId) || !userId.equals(storedUserId)) {
            throw new LmsDomainSessionException(
                    "Domain Session set during Domain Session initialization doesn't match"
                            + " the Domain Session from this Started Domain Session Message! \n"
                            + "domainSessionId: "+domainSessionId+"\n"
                            + "storedDomainSessionId: "+storedDomainSessionId+"\n"
                            + "userId: "+userId+"\n"
                            + "storedUserId: "+storedUserId+"\n");
        }
        // Needed by statement generator
        String actorSlug = sessionManager.getUserNameForDomainSessionId(domainSessionId) != null ? 
                sessionManager.getUserNameForDomainSessionId(domainSessionId) : userSession.getUsername();
        String domainId = sessionManager.getDomainId(domainSessionId);
        if(domainId == null) {
            throw new LmsDomainSessionException("Unable to resolve Domain Id for Domain Session!");
        }
        List<Statement> statements = new ArrayList<Statement>(1);
        AbstractStatementGenerator gen;
        try {
            gen = new StartedDomainSessionGenerator(actorSlug, TimestampHelper.fromEpoch(message.getTimeStamp()),
                    domainSessionId, domainId);
        } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
            throw new LmsDomainSessionException("Unable to initialize started domain session generator!", e);
        }
        try {
            gen.generateAndAdd(statements);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsDomainSessionException("Unable to generate started domain session statement!", e);
        }
        // Attempt Statement save
        if(CollectionUtils.isNotEmpty(statements)) {
            api.saveStatements(statements, false);
        }
    }
    
    @Override
    protected void insertDomainSessionClose(DomainSessionMessage message) throws LmsDomainSessionException {
        // From message
        UserSession userSession = message.getUserSession();
        Integer domainSessionId = message.getDomainSessionId();
        Integer userId = userSession.getUserId();
        SessionManager sessionManager = SessionManager.getInstance();
        // Currently stored within mapping
        Integer storedDomainSessionId = sessionManager.getCurrentDomainSessionId(userSession);
        Integer storedUserId = sessionManager.getUserIdForDomainSessionId(storedDomainSessionId);
        if(!domainSessionId.equals(storedDomainSessionId) || !userId.equals(storedUserId)) {
            throw new LmsDomainSessionException(
                    "Domain Session set during Domain Session initialization doesn't match "
                            + "the Domain Session from this Closed Domain Session Message! \n"
                            + "domainSessionId: "+domainSessionId+"\n"
                            + "storedDomainSessionId: "+storedDomainSessionId+"\n"
                            + "userId: "+userId+"\n"
                            + "storedUserId: "+storedUserId+"\n");
        }
        // Needed by statement generator
        String actorSlug = sessionManager.getUserNameForDomainSessionId(domainSessionId) != null ? 
                sessionManager.getUserNameForDomainSessionId(domainSessionId) : userSession.getUsername();
        String domainId = sessionManager.getDomainId(domainSessionId);
        if(domainId == null) {
            throw new LmsDomainSessionException("Unable to create Closed Domain Session Statement Generator!");
        }
        List<Statement> statements = new ArrayList<Statement>(1);
        AbstractStatementGenerator gen;
        try {
            gen = new ClosedDomainSessionGenerator(actorSlug, TimestampHelper.fromEpoch(message.getTimeStamp()),
                    domainSessionId, domainId);
        } catch (LmsXapiProfileException | LmsXapiGeneratorException e) {
            throw new LmsDomainSessionException("Unable to create Closed Domain Session Statement Generator!", e);
        }
        try {
            gen.generateAndAdd(statements);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsDomainSessionException("Unable to generate Closed Domain Session xAPI Statement!", e);
        }
        // Attempt Statement save
        if(CollectionUtils.isNotEmpty(statements)) {
            api.saveStatements(statements, true);
        }
        
        // METRICS
        long duration = System.currentTimeMillis() - globalStart;
        if(logger.isTraceEnabled()) {
            logger.trace("METRICS: Init to Close took: "+duration+" ms.");
        }
    }
    
    @Override
    protected void insertKnowledgeSessionDetails(String studentId, int userId, int domainSessionId, KnowledgeSessionCreated knowledgeSessionCreated) {
        // METRICS
        long start, processing;
        start = System.currentTimeMillis();
        
        // Set current knowledge session within sessionManager
        AbstractKnowledgeSession session = knowledgeSessionCreated.getKnowledgeSession();
        SessionManager.getInstance().setKnowledgeSession(session);
        // Started Knowledge Session xAPI Statement
        List<Statement> statements = new ArrayList<Statement>(1);
        // Session host as actor unless userName is blank, fallback to studentId
        String hostUsername = session.getHostSessionMember().getUserSession().getUsername();
        String actorSlug = StringUtils.isNotBlank(hostUsername) ? hostUsername : studentId;
        DateTime timestamp = TimestampHelper.fromEpoch(session.getSessionStartTime());
        // Handle Playback User
        try {
            handlePlaybackUserMapping(domainSessionId, userId, session.getCourseName());
        } catch (LmsDomainSessionException e) {
            logger.error("Playback User's Domain Session was already registered but not to any user!", e);
        }
        AbstractStatementGenerator gen = null;
        if(session instanceof IndividualKnowledgeSession) {
            try {
                gen = new StartedKnowledgeSessionGenerator.Individual(actorSlug, timestamp, domainSessionId, (IndividualKnowledgeSession) session);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                logger.error("Unable to initialize Individual Knowledge Session Started Statement Generator!", e);
            }
        } else if(session instanceof TeamKnowledgeSession) {
            try {
                gen = new StartedKnowledgeSessionGenerator.Team(actorSlug, timestamp, domainSessionId, (TeamKnowledgeSession) session);
            } catch (LmsXapiGeneratorException | LmsXapiProfileException e) {
                logger.error("Unable to initialize Team Knowledge Session Started Statement Generator!", e);
            }
        } else {
            logger.error("Unspported Knowledge Session! "+session);
        }
        if(gen != null) {
            try {
                gen.generateAndAdd(statements);
            } catch (LmsXapiGeneratorException e) {
                logger.error("Unable to generate started knowledge session statement!", e);
            }
        }
        if(CollectionUtils.isNotEmpty(statements)) {
            api.saveStatements(statements, false);
        }
        // METRICS
        processing = System.currentTimeMillis() - start;
        if(logger.isTraceEnabled()) {
            logger.trace("METRICS: insertKnowledgeSessionDetails processing took: "+processing+" ms");
        }
    }

    @Override
    protected void insertLessonCompleted(String studentId, int userId, int domainSessionId, LessonCompleted lessonCompleted) {
        List<Statement> statements = new ArrayList<Statement>(1);
        // Handle Playback User
        SessionManager sessionManager = SessionManager.getInstance();
        String domainId = sessionManager.getKnowledgeSession().getCourseName() != null ? 
                sessionManager.getKnowledgeSession().getCourseName() : 
                    sessionManager.getDomainId(domainSessionId);
        domainId = domainId != null ? domainId : "Unknown Course!";
        try {
            handlePlaybackUserMapping(domainSessionId, userId, domainId);
        } catch (LmsDomainSessionException e) {
            logger.error("Playback User's Domain Session was already registered but not to any user!", e);
        }
        AbstractStatementGenerator gen = null;
        try {
            gen = new LessonCompletedGenerator(SessionManager.getInstance().getKnowledgeSession(), domainSessionId, studentId, lessonCompleted);
        } catch(LmsXapiGeneratorException | LmsXapiProfileException e) {
            logger.error("Unable to initialize Lesson Completed xAPI Statement generator!", e);
        }
        if(gen != null) {
            try {
                gen.generateAndAdd(statements);
            } catch (LmsXapiGeneratorException e) {
                logger.error("Unable to generate Completed Lesson statement!", e);
            }
        }
        // Save Statements + force send off of any statements in accumulator
        api.saveStatements(statements, true);
    }

    @Override
    protected void insertEnvironmentAdaptation(String studentId, int userId, int domainSessionId, EnvironmentControl eControl) {
        List<Statement> statements = new ArrayList<Statement>(1);
        // Handle Playback User
        SessionManager sessionManager = SessionManager.getInstance();
        String domainId = sessionManager.getKnowledgeSession().getCourseName() != null ? 
                sessionManager.getKnowledgeSession().getCourseName() : 
                    sessionManager.getDomainId(domainSessionId);
        domainId = domainId != null ? domainId : "Unknown Course!";
        try {
            handlePlaybackUserMapping(domainSessionId, userId, domainId);
        } catch (LmsDomainSessionException e) {
            logger.error("Playback User's Domain Session was already registered but not to any user!", e);
        }
        EnvironmentAdaptationProcessor processor = null;
        try {
            processor = new EnvironmentAdaptationProcessor(eControl, studentId, domainSessionId,
                        SessionManager.getInstance().getKnowledgeSession());
        } catch (LmsXapiActivityException e) {
            logger.error("Unable to init Environment Adaptation Processor!", e);
        }
        // xAPI Statement generation
        if(processor != null) {
        try {
            processor.process(statements);
        } catch (LmsXapiProcessorException e) {
            logger.error("Unable to process Environment Adaptation!", e);
        }
        }
        if(CollectionUtils.isNotEmpty(statements)) {
            api.saveStatements(statements, true);
        }
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("[Lrs: ");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }

}
