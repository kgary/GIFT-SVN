package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.UUID;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Result;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.LearnerStateHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.TimestampHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.AssessmentActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.EvaluatorAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ParentIntermediateConceptAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ParentTaskAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.PerformanceCharacteristicsAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ReplacedStatementIdAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TeamAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.DemonstratedPerformanceTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.DemonstratedPerformanceStatement;

/**
 * Generator for Demonstrated Performance State Attribute (formative assessment) xAPI Statement.
 * 
 * @author Yet Analytics
 *
 */
public class DemonstratedPerformanceGenerator extends AbstractStatementGenerator {
    /** User name(s) associated with the performance state */
    protected List<String> actorNames;
    /** Performance state used within statement generation */
    protected AbstractPerformanceState performance;
    /** Knowledge Session for the course session in which this statement is generated */
    protected AbstractKnowledgeSession knowledgeSession;
    /** Activity Type from xAPI Profile for task */
    protected ItsActivityTypeConcepts.AssessmentNode.Task taskATC;
    /** Activity Type from xAPI Profile for intermediate concept */
    protected ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate iConceptATC;
    /** Activity Type from xAPI Profile for concept */
    protected ItsActivityTypeConcepts.AssessmentNode.Concept conceptATC;
    /** Performance Measure Result Extension from xAPI Profile */
    protected ItsResultExtensionConcepts.PerformanceMeasure performanceStateAttributeREC;
    /** Performance Characteristics Context Extension */
    protected PerformanceCharacteristicsAppender pcAppender;
    /** Evaluator Observation Context Extension from xAPI Profile */
    private ItsContextExtensionConcepts.EvaluatorObservation evaluatorObservationCEC;
    
    /**
     * Sets state used by generateStatement and attaches common appenders
     * 
     * @param template - xAPI Statement Template describing generated xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be object of xAPI statement
     * @param session - Knowledge Session associated with the learner state
     * @param domainId - Identifier for course associated with learner state
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    protected DemonstratedPerformanceGenerator(StatementTemplate template, List<AbstractPerformanceState> history,
            AbstractKnowledgeSession session, String domainId, Integer domainSessionId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(CollectionUtils.isEmpty(history)) {
            throw new IllegalArgumentException("History of Performance State(s) can not be null or empty!");
        }
        // target Performance State is last item within history
        performance = LearnerStateHelper.getLast(history);
        if(performance == null) {
            throw new IllegalArgumentException("Performance State can not be null!");
        }
        if(session == null) {
            this.actorNames = new ArrayList<String>(1);
            this.actorNames.add(actorSlug);
        } else {
            this.actorNames = LearnerStateHelper.deriveStatementActorNames(performance, session);
        }
        if(CollectionUtils.isEmpty(actorNames)) {
            throw new LmsXapiGeneratorException("Unable to derive Statement Actor(s) from AbstractPerformanceState and AbstractKnowledgeSession!");
        }
        this.performanceStateAttributeREC = ItsResultExtensionConcepts.PerformanceMeasure.getInstance();
        this.taskATC = ItsActivityTypeConcepts.AssessmentNode.Task.getInstance();
        this.iConceptATC = ItsActivityTypeConcepts.AssessmentNode.ConceptIntermediate.getInstance();
        this.conceptATC = ItsActivityTypeConcepts.AssessmentNode.Concept.getInstance();
        this.evaluatorObservationCEC = ItsContextExtensionConcepts.EvaluatorObservation.getInstance();
        this.knowledgeSession = session;
        if(session != null) {
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(session.getSessionType()));
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to add knowledge session type activity within demonstrated performance generator!", e);
            }
            if(session.getTeamStructure() != null) {
                try {
                    addStatementAppender(new TeamAppender(session, false, actorNames, performance));
                } catch (LmsXapiAppenderException e) {
                    throw new LmsXapiGeneratorException("Unable to add team within demonstrated performance generator!", e);
                }
            }
        }
        if(domainId != null) {
            addStatementAppender(new DomainAppender(domainId));
        }
        if(domainSessionId != null) {
            addStatementAppender(new DomainSessionAppender(domainSessionId));
        }
        this.pcAppender = new PerformanceCharacteristicsAppender();
        if(history.size() == 1 && performance instanceof TaskPerformanceState) {
            pcAppender.addTaskPerformanceState((TaskPerformanceState) performance);
        } else if(CollectionUtils.isNotEmpty(history)) {
            // Task expected to be first item within history
            AbstractPerformanceState task = history.get(0);
            // Parent is the second to last item within history
            AbstractPerformanceState parent = history.get(history.size() - 2);
            if(task instanceof TaskPerformanceState) { 
                pcAppender.addTaskPerformanceState((TaskPerformanceState) task);
            }
            if(parent instanceof TaskPerformanceState) {
                addStatementAppender(new ParentTaskAppender((TaskPerformanceState) parent));
            } else if(parent instanceof IntermediateConceptPerformanceState) {
                addStatementAppender(new ParentIntermediateConceptAppender((IntermediateConceptPerformanceState) parent));
            }
        }
        addStatementAppender(pcAppender);
    }
    /**
     * Generator for formative assessment xAPI statements
     * 
     * @param history - Ordered collection of performance states - last entry expected to be object of xAPI statement
     * @param session - Knowledge Session associated with the learner state
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param domainId - Identifier for course associated with learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI profile
     */
    public DemonstratedPerformanceGenerator(List<AbstractPerformanceState> history, AbstractKnowledgeSession session, 
            Integer domainSessionId, String domainId, String actorSlug) throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(DemonstratedPerformanceTemplate.getInstance(), history, session, domainId, domainSessionId, actorSlug);
    }
    
    /**
     * Generator for replacement formative assessment xAPI Statements
     * 
     * @param history - Ordered collection of performance states - last entry expected to be object of xAPI statement
     * @param invalidStmtId - Statement Id of the xAPI Statement being replaced
     * @param session - Knowledge Session associated with the learner state
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param domainId - Identifier for course associated with learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI profile
     */
    public DemonstratedPerformanceGenerator(List<AbstractPerformanceState> history, UUID invalidStmtId,
            AbstractKnowledgeSession session, Integer domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(DemonstratedPerformanceTemplate.ReplacementTemplate.getInstance(), history, session, domainId, domainSessionId, actorSlug);
        if(invalidStmtId == null) {
            throw new IllegalArgumentException("invalidated xAPI Statement Id can not be null!");
        }
        addStatementAppender(new ReplacedStatementIdAppender(invalidStmtId));
        // Evaluator as Context.instructor
        if(performance.getState() != null && performance.getState().getEvaluator() != null) {
            try {
                addStatementAppender(new EvaluatorAppender(performance.getState().getEvaluator()));
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiGeneratorException("Unable to add evaluator within demonstrated performance generator!", e);
            }
        }
    }
    
    @Override
    AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        try {
            actor = PersonaHelper.createActor(actorNames);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiGeneratorException("Unable to create statement actor within demonstrated performance generator!", e);
        }
        // Object
        AssessmentActivity activity;
        if(performance instanceof TaskPerformanceState) {
            try {
                activity = taskATC.asActivity((TaskPerformanceState) performance);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create statement task object within demonstrated performance generator!", e);
            }
        } else if(performance instanceof IntermediateConceptPerformanceState) {
            try {
                activity = iConceptATC.asActivity((IntermediateConceptPerformanceState) performance);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create statement intermediate concept object within demonstrated performance generator!", e);
            }
        } else if(performance instanceof ConceptPerformanceState) {
            try {
                activity = conceptATC.asActivity((ConceptPerformanceState) performance);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create statement concept object within demonstrated performance generator!", e);
            }
        } else {
            throw new LmsXapiGeneratorException("Unsupported Performance State! Unable to create statement object activity!");
        }
        // Event Time
        DateTime timestamp = null;
        if(performance.getState() != null) {
            long assessmentTime = performance.getState().getPerformanceAssessmentTime();
            if(assessmentTime == 0) {
                Entry<String, Long> predicted = TimestampHelper.mostRecentPrediction(performance.getState());
                timestamp = TimestampHelper.fromEpoch(predicted.getValue());
            } else {
                timestamp = TimestampHelper.fromEpoch(assessmentTime);
            }
        }
        // Statement
        DemonstratedPerformanceStatement stmt;
        try {
            stmt = new DemonstratedPerformanceStatement(actor, activity, timestamp);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to create demonstrated performance statement!", e);
        }
        // Evaluator Observation extension (comment and/or media)
        if(StringUtils.isNotBlank(performance.getState().getObserverComment()) || StringUtils.isNotBlank(performance.getState().getObserverMedia())) {
            try {
                evaluatorObservationCEC.addToContext(stmt.getContext(), performance);
            } catch (LmsXapiExtensionException e) {
                throw new LmsXapiGeneratorException("unable to create evaluator observation context extension!", e);
            }
        }
        // Result
        Result r = new Result();
        performanceStateAttributeREC.addToResult(r, performance);
        stmt.setResult(r);
        return generateStatement(stmt, deriveId);
    }
}
