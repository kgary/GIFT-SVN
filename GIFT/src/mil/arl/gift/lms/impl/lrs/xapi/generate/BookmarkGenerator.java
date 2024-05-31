package mil.arl.gift.lms.impl.lrs.xapi.generate;

import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.SessionManager;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.BookmarkedTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.BookmarkStatement;

/**
 * Generator for bookmark xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class BookmarkGenerator extends AbstractStatementGenerator {
    /** User name of the evaluator parsed from top level Performance State*/
    private String actorSlug;
    /** Event time */
    private DateTime timestamp;
    /** Performance that contains the bookmark */
    private PerformanceState pState;
    /** Name of the associated Domain */
    private String courseName;
    /** Domain Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.Domain domainATC;
    /** Evaluator Observation Context Extension from xAPI Profile */
    private ItsContextExtensionConcepts.EvaluatorObservation evaluatorObservationCEC;
    
    /**
     * Parses performance state and attaches appenders
     * 
     * @param template - xAPI Statement Template describing generated xAPI Statement
     * @param session - Knowledge Session associated with Learner State
     * @param performance - Performance State from Learner State
     * @param domainSessionId - Identifier for Domain Session associated with Learner State
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    private BookmarkGenerator(StatementTemplate template, AbstractKnowledgeSession session, PerformanceState performance, Integer domainSessionId) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(performance == null) {
            throw new IllegalArgumentException("performance state can not be null!");
        }
        this.pState = performance;
        this.actorSlug = performance.getEvaluator();
        if(StringUtils.isBlank(actorSlug)) {
            throw new LmsXapiGeneratorException("Unable to generate bookmarked statement without evaluator!");
        }
        if(performance.getObservationStartedTime() != null) {
            this.timestamp = new DateTime(performance.getObservationStartedTime());
        } else {
            this.timestamp = new DateTime();
        }
        addStatementAppender(new DomainSessionAppender(domainSessionId));
        if(session != null) {
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(session.getSessionType()));
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("unable to add knowledge session type appender within bookmark statement generator!", e);
            }
            if(StringUtils.isNotBlank(session.getCourseRuntimeId())) {
                this.courseName = session.getCourseRuntimeId();
            } else {
                this.courseName = null;
            }
        }
        if(this.courseName == null) {
            // Course name from sessionManager
            this.courseName = SessionManager.getInstance().getDomainId(domainSessionId);
        }
        this.domainATC = ItsActivityTypeConcepts.Domain.getInstance();
        this.evaluatorObservationCEC = ItsContextExtensionConcepts.EvaluatorObservation.getInstance();
    }
    /**
     * Resolves xAPI Statement Template and sets state used within generateStatement
     * 
     * @param session - Knowledge Session associated with Learner State
     * @param performance - Performance State from Learner State
     * @param domainSessionId - Identifier for Domain Session associated with Learner State
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public BookmarkGenerator(AbstractKnowledgeSession session, PerformanceState performance, Integer domainSessionId) throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(BookmarkedTemplate.getInstance(), session, performance, domainSessionId);
    }

    @Override
    AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        try {
            actor = PersonaHelper.createMboxAgent(actorSlug);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiGeneratorException("unable to create mbox actor from actorSlug", e);
        }
        // Object
        DomainActivity object;
        try {
            object = domainATC.asActivity(courseName);
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("unable to create domain activity within bookmark statement generator!", e);
        }
        // statement
        BookmarkStatement stmt;
        try {
            stmt = new BookmarkStatement(actor, object, timestamp);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("unable to generate bookmark statement!", e);
        }
        // Context
        Context ctx = stmt.getContext();
        try {
            evaluatorObservationCEC.addToContext(ctx, pState);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiGeneratorException("unable to create evaluator observation context extension!", e);
        }
        stmt.setContext(ctx);
        return generateStatement(stmt, deriveId);
    }   
}
