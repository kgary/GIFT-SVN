package mil.arl.gift.lms.impl.lrs.xapi.generate;

import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Result;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.SessionManager;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.DomainActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.LessonCompletedTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.LessonCompletedStatement;

/**
 * Generator for Lesson Completed xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class LessonCompletedGenerator extends AbstractStatementGenerator {
    /** Student name passed to Lrss' insertLessonCompleted method */
    private String actorSlug;
    /** Domain identifier for the associated course */
    private String courseName;
    /** Domain Activity Type from xAPI Profile */
    private ItsActivityTypeConcepts.Domain domainATC;
    /** reason string corresponding to LessonCompletedStatusType enumeration */
    private String reason;
    /**
     * Sets appenders, derives reason string from completed status and derives course name
     * 
     * @param template - xAPI Statement Template which describes generated xAPI Statement
     * @param session - knowledge session corresponding to the complete lesson
     * @param domainSessionId - identifier for the domain session associated with the course
     * @param actorSlug - student id passed to Lrss' insertLessonCompleted
     * @param lessonCompleted - contains the reason for the completion
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    private LessonCompletedGenerator(StatementTemplate template, AbstractKnowledgeSession session, Integer domainSessionId, String actorSlug, LessonCompleted lessonCompleted) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(StringUtils.isBlank(actorSlug)) {
            throw new IllegalArgumentException("actorSlug can not be null!");
        }
        this.actorSlug = actorSlug;
        addStatementAppender(new DomainSessionAppender(domainSessionId));
        if(session != null) {
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(session.getSessionType()));
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("unable to add knowledge session type appender within bookmark statement generator!", e);
            }
            // Course name from knowledgeSession
            if(StringUtils.isNotBlank(session.getCourseRuntimeId())) {
                this.courseName = session.getCourseRuntimeId();
            } else {
                this.courseName = null;
            }
        }
        if(this.courseName == null) {
            this.courseName = SessionManager.getInstance().getDomainId(domainSessionId);
        }
        this.domainATC = ItsActivityTypeConcepts.Domain.getInstance();
        if(lessonCompleted == null) {
            throw new IllegalArgumentException("lessonCompleted can not be null!");
        }
        if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.LEGACY_NOT_SPECIFIED)) {
            reason = "Unknown completion status: this message is the default for legacy instances that don't have this field (pre July 2021)";
        } else if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.ERROR)) {
            reason = "An error occurred in GIFT and the lesson can no longer continue";
        } else if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.LESSON_RULE)) {
            reason = "A rule in the lesson (DKF) caused the lesson to end gracefully";
        } else if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.LEARNER_ENDED_OBJECT)) {
            reason = "The learner ended the course prematurely";
        } else if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.LEARNER_ENDED_COURSE)) {
            reason = "The learner ended the course object";
        } else if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.CONTROLLER_ENDED_LESSON)) {
            reason = "Some external controller outside of the modules ended the lesson (e.g. game master, RTA application)";
        } else if(lessonCompleted.getStatusType().equals(LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST)) {
            reason = "some instructional design ended the course prematurely, e.g. too many failed attempts, need to start lesson over";
        } else {
            throw new LmsXapiGeneratorException("Unable to set reason from Lesson Completed Status Type!");
        }
    }
    /**
     * Parses xAPI Statement Template from xAPI Profile and sets up for statement generation
     * 
     * @param session - knowledge session corresponding to the complete lesson
     * @param domainSessionId - identifier for the domain session associated with the course
     * @param actorSlug - student id passed to Lrss' insertLessonCompleted
     * @param lessonCompleted - contains the reason for the completion
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public LessonCompletedGenerator(AbstractKnowledgeSession session, Integer domainSessionId, String actorSlug, LessonCompleted lessonCompleted) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        this(LessonCompletedTemplate.getInstance(), session, domainSessionId, actorSlug, lessonCompleted);
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
        // Statement
        LessonCompletedStatement stmt;
        try {
            stmt = new LessonCompletedStatement(actor, object, new DateTime());
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("unable to generate lesson completed statement!", e);
        }
        // Result
        if(reason != null && StringUtils.isNotBlank(reason)) {
            Result r = new Result();
            r.setResponse(reason);
            stmt.setResult(r);
        }
        return generateStatement(stmt, deriveId);
    }
}
