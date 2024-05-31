package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Result;
import generated.course.ConceptNode;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.CourseRecordHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.CourseRecordAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.EvaluatorAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.PerformanceCharacteristicsAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ReplacedStatementIdAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TeamAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TrainingApplicationAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.mom.MomActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.SkillAssessmentStatement;

/**
 * Generator for Assessed Assessment (summative) xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class SkillAssessmentGenerator extends AbstractStatementGenerator {
    /** Assessment Activity Type from MOM xAPI Profile */
    protected MomActivityTypeConcepts.Assessment assessmentATC;
    /** Node Hierarchy Context Extension from xAPI Profile */
    protected ItsContextExtensionConcepts.NodeHierarchy nodeHierarchyCEC;
    /** Node Id Mapping Context Extension from xAPI Profile */
    protected ItsContextExtensionConcepts.NodeIdMapping nodeIdMappingCEC;
    /** Evaluator Observation Context Extension from xAPI Profile */
    private ItsContextExtensionConcepts.EvaluatorObservation evaluatorObservationCEC;
    /** Concept Evaluation from xAPI Profile */
    protected ItsResultExtensionConcepts.ConceptEvaluation conceptEvaluationREC;
    /** xAPI Statement event time */
    protected DateTime eventTime;
    /** Graded Score Node used as xAPI Statement Object expected to contain Raw Score Node */
    protected GradedScoreNode conceptGSN;
    /** Raw Score Node parsed from Graded Score Node */
    protected RawScoreNode conceptRSN;
    /** Order list of Graded Score Node parent(s) */
    protected List<GradedScoreNode> historyColl;
    /** Concept Node from DKF corresponding to the Graded Score Node; may contain Authoritative Resource */
    protected ConceptNode conceptDKF;
    /**
     * Sets properties and Statement Appenders used within summative xAPI Statement generation
     * 
     * @param template - xAPI Statement Template describing generated xAPI Statement
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param domainSessionId - Identifier for the Domain Session passed to LRSs' insertCourseRecord method
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * 
     * @throws LmsXapiGeneratorException when unable to set Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     */
    private SkillAssessmentGenerator(StatementTemplate template, AbstractKnowledgeSession knowledgeSession,
            DateTime timestamp, GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history, 
            ConceptNode conceptDkf, String domainId, Integer domainSessionId, CourseRecordRef courseRecordRef) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(timestamp == null) {
            throw new IllegalArgumentException("timestamp can not be null!");
        }
        this.eventTime = timestamp;
        if(conceptGsn == null) {
            throw new IllegalArgumentException("graded score node can not be null!");
        }
        this.conceptGSN = conceptGsn;
        if(conceptRsn == null) {
            throw new IllegalArgumentException("raw score node can not be null!");
        }
        this.conceptRSN = conceptRsn;
        if(history == null) {
            throw new IllegalArgumentException("collection of graded score node parents can not be null!");
        }
        this.historyColl = history;
        this.conceptDKF = conceptDkf;
        this.assessmentATC = MomActivityTypeConcepts.Assessment.getInstance();
        this.nodeHierarchyCEC = ItsContextExtensionConcepts.NodeHierarchy.getInstance();
        this.nodeIdMappingCEC = ItsContextExtensionConcepts.NodeIdMapping.getInstance();
        this.evaluatorObservationCEC = ItsContextExtensionConcepts.EvaluatorObservation.getInstance();
        this.conceptEvaluationREC = ItsResultExtensionConcepts.ConceptEvaluation.getInstance();
        if(knowledgeSession != null) {
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(knowledgeSession.getSessionType()));
            } catch (LmsXapiActivityException | LmsXapiProfileException e) {
                throw new LmsXapiGeneratorException("Unable to add knowledge session type within generator!", e);
            }
            if(knowledgeSession.getTeamStructure() != null) {
                try {
                    addStatementAppender(new TeamAppender(knowledgeSession, false, conceptRSN.getUsernames()));
                } catch (LmsXapiAppenderException e) {
                    throw new LmsXapiGeneratorException("Unable to add team within generator!", e);
                }
            }
            if(knowledgeSession.getTrainingAppType() != null) {
                addStatementAppender(new TrainingApplicationAppender(knowledgeSession.getTrainingAppType()));
            }
        }
        if(domainId != null) {
            addStatementAppender(new DomainAppender(domainId));
        }
        if(domainSessionId != null) {
            addStatementAppender(new DomainSessionAppender(domainSessionId));
        }
        if(courseRecordRef != null) {
            addStatementAppender(new CourseRecordAppender(courseRecordRef));
        }
        PerformanceCharacteristicsAppender pcAppender = new PerformanceCharacteristicsAppender();
        List<TaskScoreNode> tScoreNodes = CourseRecordHelper.filterForTaskNodes(historyColl);
        if(conceptGsn instanceof TaskScoreNode) {
            tScoreNodes.add((TaskScoreNode) conceptGsn);
        }
        pcAppender.addTaskScoreNode(tScoreNodes);
        addStatementAppender(pcAppender);
    }
    /**
     * Determine Team vs Individual xAPI Statement Template
     * 
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param domainSessionId - Identifier for the Domain Session passed to LRSs' insertCourseRecord method
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * 
     * @throws LmsXapiGeneratorException when unable to attach Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     * @throws LmsXapiAgentException when unable to create actor from rsn
     */
    public SkillAssessmentGenerator(AbstractKnowledgeSession knowledgeSession, DateTime timestamp,
            GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history,
            ConceptNode conceptDkf, String domainId, Integer domainSessionId, CourseRecordRef courseRecordRef) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException, LmsXapiAgentException {
        this(CourseRecordHelper.determineTemplate(conceptRsn, null), knowledgeSession, timestamp, conceptGsn, conceptRsn,
                history, conceptDkf, domainId, domainSessionId, courseRecordRef);
    }
    /**
     * Determine Team vs Individual replacement xAPI Statement Template and attach ReplacedStatementIdAppender
     * 
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param domainSessionId - Identifier for the Domain Session passed to LRSs' insertCourseRecord method
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * @param invalidStmtId - Statement Id of an out-dated xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to attach Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     * @throws LmsXapiAgentException when unable to create actor from rsn
     */
    public SkillAssessmentGenerator(AbstractKnowledgeSession knowledgeSession, DateTime timestamp,
            GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history, ConceptNode conceptDkf,
            String domainId, Integer domainSessionId, CourseRecordRef courseRecordRef, UUID invalidStmtId, String voiderSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException, LmsXapiAgentException {
        this(CourseRecordHelper.determineTemplate(conceptRsn, invalidStmtId), knowledgeSession, timestamp, conceptGsn, conceptRsn,
                history, conceptDkf, domainId, domainSessionId, courseRecordRef);
        if(invalidStmtId == null) {
            throw new IllegalArgumentException("Invalid Statement Id can not be null!");
        }
        addStatementAppender(new ReplacedStatementIdAppender(invalidStmtId));
        if(StringUtils.isNotBlank(voiderSlug)) {
            addStatementAppender(new EvaluatorAppender(voiderSlug));
        }
    }
    /**
     * Sets properties and Statement Appenders used within summative xAPI Statement generation
     * 
     * @param template - xAPI Statement Template describing generated xAPI Statement
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param chainInfo - Chain of custody info
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * 
     * @throws LmsXapiGeneratorException when unable to set Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     */
    private SkillAssessmentGenerator(StatementTemplate template, AbstractKnowledgeSession knowledgeSession,
            DateTime timestamp, GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history, 
            ConceptNode conceptDkf, String domainId, AssessmentChainOfCustody chainInfo, CourseRecordRef courseRecordRef) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, chainInfo);
        if(timestamp == null) {
            throw new IllegalArgumentException("timestamp can not be null!");
        }
        this.eventTime = timestamp;
        if(conceptGsn == null) {
            throw new IllegalArgumentException("graded score node can not be null!");
        }
        this.conceptGSN = conceptGsn;
        if(conceptRsn == null) {
            throw new IllegalArgumentException("raw score node can not be null!");
        }
        this.conceptRSN = conceptRsn;
        if(history == null) {
            throw new IllegalArgumentException("collection of graded score node parents can not be null!");
        }
        this.historyColl = history;
        this.conceptDKF = conceptDkf;
        this.assessmentATC = MomActivityTypeConcepts.Assessment.getInstance();
        this.nodeHierarchyCEC = ItsContextExtensionConcepts.NodeHierarchy.getInstance();
        this.nodeIdMappingCEC = ItsContextExtensionConcepts.NodeIdMapping.getInstance();
        this.evaluatorObservationCEC = ItsContextExtensionConcepts.EvaluatorObservation.getInstance();
        this.conceptEvaluationREC = ItsResultExtensionConcepts.ConceptEvaluation.getInstance();
        if(knowledgeSession != null) {
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(knowledgeSession.getSessionType()));
            } catch (LmsXapiActivityException | LmsXapiProfileException e) {
                throw new LmsXapiGeneratorException("Unable to add knowledge session type within generator!", e);
            }
            if(knowledgeSession.getTeamStructure() != null) {
                try {
                    addStatementAppender(new TeamAppender(knowledgeSession, false, conceptRSN.getUsernames()));
                } catch (LmsXapiAppenderException e) {
                    throw new LmsXapiGeneratorException("Unable to add team within generator!", e);
                }
            }
            if(knowledgeSession.getTrainingAppType() != null) {
                addStatementAppender(new TrainingApplicationAppender(knowledgeSession.getTrainingAppType()));
            }
        }
        if(domainId != null) {
            addStatementAppender(new DomainAppender(domainId));
        }
        if(domainSessionId != null) {
            addStatementAppender(new DomainSessionAppender(domainSessionId));
        }
        if(courseRecordRef != null) {
            addStatementAppender(new CourseRecordAppender(courseRecordRef));
        }
        PerformanceCharacteristicsAppender pcAppender = new PerformanceCharacteristicsAppender();
        List<TaskScoreNode> tScoreNodes = CourseRecordHelper.filterForTaskNodes(historyColl);
        if(conceptGsn instanceof TaskScoreNode) {
            tScoreNodes.add((TaskScoreNode) conceptGsn);
        }
        pcAppender.addTaskScoreNode(tScoreNodes);
        addStatementAppender(pcAppender);
    }
    /**
     * Determine Team vs Individual xAPI Statement Template
     * 
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param chainInfo - Chain of custody info
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * 
     * @throws LmsXapiGeneratorException when unable to attach Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     * @throws LmsXapiAgentException when unable to create actor from rsn
     */
    public SkillAssessmentGenerator(AbstractKnowledgeSession knowledgeSession, DateTime timestamp,
            GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history,
            ConceptNode conceptDkf, String domainId, AssessmentChainOfCustody chainInfo, CourseRecordRef courseRecordRef) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException, LmsXapiAgentException {
        this(CourseRecordHelper.determineTemplate(conceptRsn, null), knowledgeSession, timestamp, conceptGsn, conceptRsn,
                history, conceptDkf, domainId, chainInfo, courseRecordRef);
    }
    
    /**
     * Generate Novel Summative Assessment Statement based on evaluation provided by Observer Controller
     * 
     * @param knowledgeSession - Knowledge Session corresponding to session being evaluated
     * @param timestamp - Event time of the assessment creation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param chainInfo - Chain of custody info
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * @param evaluatorSlug - Observer Controller who created the novel assessment
     * 
     * @throws LmsXapiGeneratorException when unable to attach Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     * @throws LmsXapiAgentException when unable to create actor from rsn or evaluatorSlug
     */
    public SkillAssessmentGenerator(AbstractKnowledgeSession knowledgeSession, DateTime timestamp,
            GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history, ConceptNode conceptDkf,
            String domainId, AssessmentChainOfCustody chainInfo, CourseRecordRef courseRecordRef, String evaluatorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException, LmsXapiAgentException {
        this(knowledgeSession, timestamp, conceptGsn, conceptRsn, history, conceptDkf, domainId, chainInfo, courseRecordRef);
        if(StringUtils.isNotBlank(evaluatorSlug)) {
            addStatementAppender(new EvaluatorAppender(evaluatorSlug));
        }
    }
    
    /**
     * Determine Team vs Individual replacement xAPI Statement Template and attach ReplacedStatementIdAppender
     * 
     * @param knowledgeSession - Knowledge Session in which summative assessment was derived
     * @param timestamp - Event time for the assessment derivation
     * @param conceptGsn - Graded Score Node corresponding to the assessed Course Concept
     * @param conceptRsn - Raw Score Node corresponding to the cumulative assessment results for the Course Concept
     * @param history - Ordered list of Parents for the conceptGsn
     * @param conceptDkf - Course Concept from the DKF corresponding to conceptGsn
     * @param domainId - Identifier for the corresponding Course
     * @param chainInfo - Chain of custody info
     * @param courseRecordRef - Course Record Reference from the corresponding Course Record
     * @param invalidStmtId - Statement Id of an out-dated xAPI Statement
     * @param voiderSlug - Observer Controller who updated the assessment
     * 
     * @throws LmsXapiGeneratorException when unable to attach Statement Appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile component from xAPI Profile(s)
     * @throws LmsXapiAgentException when unable to create actor from rsn
     */
    public SkillAssessmentGenerator(AbstractKnowledgeSession knowledgeSession, DateTime timestamp,
            GradedScoreNode conceptGsn, RawScoreNode conceptRsn, List<GradedScoreNode> history, ConceptNode conceptDkf,
            String domainId, AssessmentChainOfCustody chainInfo, CourseRecordRef courseRecordRef, UUID invalidStmtId, String voiderSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException, LmsXapiAgentException {
        this(CourseRecordHelper.determineTemplate(conceptRsn, invalidStmtId), knowledgeSession, timestamp, conceptGsn, conceptRsn,
                history, conceptDkf, domainId, chainInfo, courseRecordRef);
        if(invalidStmtId == null) {
            throw new IllegalArgumentException("Invalid Statement Id can not be null!");
        }
        addStatementAppender(new ReplacedStatementIdAppender(invalidStmtId));
        if(StringUtils.isNotBlank(voiderSlug)) {
            addStatementAppender(new EvaluatorAppender(voiderSlug));
        }
    }
    
    @Override
    public AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        try {
            actor = CourseRecordHelper.createActorFromRawScoreNode(conceptRSN);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiGeneratorException("unable to create Actor from RawScoreNode!", e);
        }
        // Object
        AssessmentActivity object;
        if(conceptDKF != null && conceptDKF.getAuthoritativeResource() != null && conceptDKF.getAuthoritativeResource().getId() != null) {
            try {
                object = assessmentATC.asActivity(conceptGSN, conceptDKF);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create Activity Object from GradedScoreNode: "+conceptGSN+" and DKF Concept: "+conceptDKF, e);
            }
        } else {
            try {
                object = assessmentATC.asActivity(conceptGSN);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create Activity Object from GradedScoreNode: "+conceptGSN, e);
            }
        }
        // Context
        Context context = new Context();
        // -> Graded Score Node hierarchy context extension from history
        try {
            nodeHierarchyCEC.addToContext(context, historyColl);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiGeneratorException("Unable to create Node Hierarchy Context Extension from GradedScoreNode family tree!", e);
        }
        List<GradedScoreNode> allGradedScoreNodes = new ArrayList<GradedScoreNode>(historyColl);
        allGradedScoreNodes.add(conceptGSN);
        try {
            nodeIdMappingCEC.addToContext(context, allGradedScoreNodes);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiGeneratorException("Unable to create Node Id mapping Context Extension from all Graded Score Nodes!", e);
        }
        // Evaluator Observation extension (comment and/or media)
        if(StringUtils.isNotBlank(conceptGSN.getObserverComment()) || StringUtils.isNotBlank(conceptGSN.getObserverMedia())) {
            try {
                evaluatorObservationCEC.addToContext(context, conceptGSN);
            } catch (LmsXapiExtensionException e) {
                throw new LmsXapiGeneratorException("unable to create evaluator observation context extension!", e);
            }
        } else if(StringUtils.isNotBlank(conceptRSN.getObserverComment()) || StringUtils.isNotBlank(conceptRSN.getObserverMedia())) {
            try {
                evaluatorObservationCEC.addToContext(context, conceptRSN);
            } catch (LmsXapiExtensionException e) {
                throw new LmsXapiGeneratorException("unable to create evaluator observation context extension!", e);
            }
        }
        // Result
        Result result = new Result();
        // -> Response
        result.setResponse(conceptGSN.getGradeAsString());
        // -> Concept Evaluation Result Extension
        conceptEvaluationREC.addToResult(result, conceptRSN);
        // Statement
        SkillAssessmentStatement stmt;
        try {
            stmt = new SkillAssessmentStatement(actor, object, eventTime, context, result);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to generate scored skill assessment statement!", e);
        }
        return generateStatement(stmt, deriveId);
    }
}
