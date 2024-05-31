package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Result;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.LearnerStateHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.TimestampHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.LearnerStateAttributeActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.ConceptAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.IntermediateConceptAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.PerformanceCharacteristicsAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ReplacedStatementIdAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TaskAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.EvaluatorAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsResultExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.PredictedLearnerStateAttributeTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.PredictedLearnerStateAttributeStatement;

/**
 * Generator for Predicted (Affective | Cognitive) Learner State Attribute xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class PredictedLearnerStateAttributeGenerator extends AbstractStatementGenerator {
    /** Activity Type from xAPI Profile for Learner State Attribute */
    protected ItsActivityTypeConcepts.Lsa lsaATC;
    /** User name associated with the learner state */
    protected String actorSlug;
    /** Learner State Attribute */
    protected LearnerStateAttribute lsa;
    /** Attribute Measure Result Extension from xAPI Profile */
    protected ItsResultExtensionConcepts.AttributeMeasure lsaREC;
    /**
     * Sets state used within generate statement, parses xAPI Profile and attaches common appenders
     * 
     * @param template - xAPI Statement Template describing generated xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA - can be null
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param actorSlug - User name associated with the learner state
     * @param attribute - Learner State Attribute to generate statement about
     * @param session - Knowledge Session associated with the learner state
     * @param domainId - Identifier for course associated with learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    protected PredictedLearnerStateAttributeGenerator(StatementTemplate template, List<AbstractPerformanceState> history, Integer domainSessionId, 
            String actorSlug, LearnerStateAttribute attribute, AbstractKnowledgeSession session, String domainId) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(StringUtils.isBlank(actorSlug)) {
            throw new IllegalArgumentException("actorSlug can not be null or empty!");
        }
        this.actorSlug = actorSlug;
        if(attribute == null) {
            throw new IllegalArgumentException("Learner State Attribute can not be null!");
        }
        this.lsa = attribute;
        this.lsaATC = ItsActivityTypeConcepts.Lsa.getInstance();
        this.lsaREC = ItsResultExtensionConcepts.AttributeMeasure.getInstance();
        addStatementAppender(new DomainSessionAppender(domainSessionId));
        if(domainId != null) {
            addStatementAppender(new DomainAppender(domainId));
        }
        if(session != null) {
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(session.getSessionType()));
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to append knowledge session type within predicted learner state attribute generator!", e);
            }
        }
        if(CollectionUtils.isNotEmpty(history)) {
            PerformanceCharacteristicsAppender pcAppender = new PerformanceCharacteristicsAppender();
            AbstractPerformanceState state = LearnerStateHelper.getLast(history);
            if(history.size() == 1 && state instanceof TaskPerformanceState) {
                pcAppender.addTaskPerformanceState((TaskPerformanceState) state);
                addStatementAppender(new TaskAppender((TaskPerformanceState) state));
            } else {
                AbstractPerformanceState task = history.get(0);
                if(task instanceof TaskPerformanceState) { 
                    pcAppender.addTaskPerformanceState((TaskPerformanceState) task);
                }
                if(state instanceof IntermediateConceptPerformanceState) {
                    addStatementAppender(new IntermediateConceptAppender((IntermediateConceptPerformanceState) state));
                } else if(state instanceof ConceptPerformanceState) {
                    addStatementAppender(new ConceptAppender((ConceptPerformanceState) state));
                }
            }
            addStatementAppender(pcAppender);
        }
    }
    /**
     * Generator that supports Learner State Attribute Collections with associated Performance State
     * 
     * @param session - Knowledge Session associated with the learner state
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA - can be null
     * @param actorSlug - User name associated with the learner state
     * @param attribute - Learner State Attribute to generate statement about
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param domainId - Identifier for the Domain associated with the learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public PredictedLearnerStateAttributeGenerator(AbstractKnowledgeSession session, List<AbstractPerformanceState> history, 
            String actorSlug, LearnerStateAttribute attribute, Integer domainSessionId, String domainId) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(PredictedLearnerStateAttributeTemplate.getInstance(), history, domainSessionId, actorSlug, attribute, session, domainId);
    }
    /**
     * Generator for replacement xAPI Statements, supports Learner State Attribute Collections with associated Performance State
     * 
     * @param session - Knowledge Session associated with the learner state
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA - can be null
     * @param actorSlug - User name associated with the learner state
     * @param attribute - Learner State Attribute to generate statement about
     * @param invalidStmtId - Identifier of out-dated xAPI Statement being replaced
     * @param domainSessionId - Identifier for domain session associated with the learner state
     * @param domainId - Identifier for the Domain associated with the learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public PredictedLearnerStateAttributeGenerator(AbstractKnowledgeSession session, List<AbstractPerformanceState> history,
            String actorSlug, LearnerStateAttribute attribute, UUID invalidStmtId, Integer domainSessionId, String domainId) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(PredictedLearnerStateAttributeTemplate.ReplacementTemplate.getInstance(), history, domainSessionId, actorSlug, attribute, session, domainId);
        if(invalidStmtId != null) {
            addStatementAppender(new ReplacedStatementIdAppender(invalidStmtId));
        }
        if(CollectionUtils.isNotEmpty(history)) {
            AbstractPerformanceState state = LearnerStateHelper.getLast(history);
            // Evaluator as Context.instructor
            if(state.getState() != null && state.getState().getEvaluator() != null) {
                try {
                    addStatementAppender(new EvaluatorAppender(state.getState().getEvaluator()));
                } catch (LmsXapiAgentException e) {
                    throw new LmsXapiGeneratorException("Unable to append evaluator within predicted learner state attribute generator!", e);
                }
            }
        }
    }
    
    @Override
    public AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        try {
            actor = PersonaHelper.createMboxAgent(actorSlug);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiGeneratorException("Unable to create statement actor within predicted learner state attribute generator!", e);
        }
        // Object
        LearnerStateAttributeActivity activity;
        try {
            activity = lsaATC.asActivity(lsa);
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("Unable to create statement object within predicted learner state attribute generator!", e);
        }
        // Timestamp
        Entry<String, Long> predicted = TimestampHelper.mostRecentPrediction(lsa);
        // Statement
        PredictedLearnerStateAttributeStatement stmt;
        try {
            stmt = new PredictedLearnerStateAttributeStatement(actor, activity, TimestampHelper.fromEpoch(predicted.getValue()));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to create predicted learner state attribute statement!", e);
        }
        // Result
        Result r = new Result();
        lsaREC.addToResult(r, lsa);
        stmt.setResult(r);
        return generateStatement(stmt, deriveId);
    }
}
