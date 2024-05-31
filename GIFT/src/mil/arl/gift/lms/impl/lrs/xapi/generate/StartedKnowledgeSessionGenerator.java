package mil.arl.gift.lms.impl.lrs.xapi.generate;

import org.joda.time.DateTime;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.SessionManager;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.KnowledgeSessionActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TeamAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TrainingApplicationAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.StartedKnowledgeSessionTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.StartedKnowledgeSessionStatement;

/**
 * Generator for Started Knowledge Session xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class StartedKnowledgeSessionGenerator extends AbstractStatementGenerator {
    /** Knowledge Session Activity Type (individual or team) */
    protected ItsActivityTypeConcepts knowledgeSessionATC;
    /** Mission Metadata Context Extension from xAPI Profile */
    protected ItsContextExtensionConcepts.MissionMetadata missionMetadataCE;
    /** studentId passed to Lrss' insertKnowledgeSessionDetails method */
    private String actorSlug;
    /** Knowledge Sessions' session start time */
    private DateTime timestamp;
    /** Knowledge Session which was started */
    private AbstractKnowledgeSession knowledgeSession;
    /**
     * Sets state used within generateStatement and attaches appenders
     * 
     * @param template - xAPI Statement Template that describes generated xAPI Statement
     * @param actorSlug - studentId passed to Lrss' insertKnowledgeSessionDetails method
     * @param timestamp - Knowledge Sessions' session start time
     * @param domainSessionId - Domain Session identifier for the actorSlug user
     * @param session - Knowledge Session that was started
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    protected StartedKnowledgeSessionGenerator(StatementTemplate template, String actorSlug, DateTime timestamp, Integer domainSessionId, AbstractKnowledgeSession session) 
            throws LmsXapiGeneratorException, LmsXapiProfileException {
        super(template, domainSessionId);
        if(timestamp == null) {
            throw new IllegalArgumentException("timestamp can not be null!");
        }
        this.timestamp = timestamp;
        if(StringUtils.isBlank(actorSlug)) {
            throw new IllegalArgumentException("actorSlug can not ben null or empty!");
        }
        this.actorSlug = actorSlug;
        if(session == null) {
            throw new IllegalArgumentException("knowledge session can not be null!");
        }
        this.knowledgeSession = session;
        if(knowledgeSession.getMission() != null) {
            this.missionMetadataCE = ItsContextExtensionConcepts.MissionMetadata.getInstance();
        }
        try {
            addStatementAppender(new KnowledgeSessionTypeAppender(knowledgeSession.getSessionType()));
        } catch (LmsXapiActivityException e) {
            throw new LmsXapiGeneratorException("Unable to append knowledge session type within started knowledge session generator!", e);
        }
        String courseName = StringUtils.isNotBlank(knowledgeSession.getCourseRuntimeId())
                ? knowledgeSession.getCourseRuntimeId()
                        : SessionManager.getInstance().getDomainId(domainSessionId);
        addStatementAppender(new DomainAppender(courseName));
        if(knowledgeSession instanceof IndividualKnowledgeSession) {
            this.knowledgeSessionATC = ItsActivityTypeConcepts.KnowledgeSessionIndividual.getInstance();
        } else if(knowledgeSession instanceof TeamKnowledgeSession) {
            this.knowledgeSessionATC = ItsActivityTypeConcepts.KnowledgeSessionTeam.getInstance();
        }
        if(knowledgeSession.getTeamStructure() != null) {
            try {
                addStatementAppender(new TeamAppender(knowledgeSession, true));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add team within started knowledge session generator!", e);
            }
        }
        TrainingApplicationEnum trainingApplication = knowledgeSession.getTrainingAppType();
        if(trainingApplication != null) {
            addStatementAppender(new TrainingApplicationAppender(trainingApplication));
        }
        addStatementAppender(new DomainSessionAppender(domainSessionId));
    }
    
    @Override
    AbstractGiftStatement generateStatement(Boolean deriveId) throws LmsXapiGeneratorException {
        // Actor
        Agent actor;
        try {
            actor = PersonaHelper.createMboxAgent(actorSlug);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiGeneratorException("Unable to create statement actor within started knowledge session generator!", e);
        }
        // Object
        KnowledgeSessionActivity object;
        if(knowledgeSession instanceof IndividualKnowledgeSession) {
            try {
                object = ((ItsActivityTypeConcepts.KnowledgeSessionIndividual) knowledgeSessionATC).asActivity((IndividualKnowledgeSession) knowledgeSession);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create individual knowledge session statement object within started knowledge session generator!", e);
            }
        } else if(knowledgeSession instanceof TeamKnowledgeSession) {
            try {
                object = ((ItsActivityTypeConcepts.KnowledgeSessionTeam) knowledgeSessionATC).asActivity((TeamKnowledgeSession) knowledgeSession);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiGeneratorException("Unable to create team knowledge session statement object within started knowledge session generator!", e);
            }
        } else {
            throw new LmsXapiGeneratorException("Unable to create Object Activity from unsupported Abstract Knowledge Session extension!");
        }
        // statement
        StartedKnowledgeSessionStatement stmt;
        // Context
        if(missionMetadataCE != null) {
            Context c = new Context();
            missionMetadataCE.addToContext(c, knowledgeSession.getMission());
            try {
                stmt = new StartedKnowledgeSessionStatement(actor, object, timestamp, c);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiGeneratorException("Unable to create started knowledge session statement with mission metadata!", e);
            }
        } else {
            try {
                stmt = new StartedKnowledgeSessionStatement(actor, object, timestamp);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiGeneratorException("Unable to create started knowledge session statement!", e);
            }
        }
        return generateStatement(stmt, deriveId);
    }
    
    // Subclass for Individual Knowledge Session Statement Object Template
    public static class Individual extends StartedKnowledgeSessionGenerator {
        /**
         * Started Individual Knowledge Session
         * 
         * @param actorSlug - studentId passed to Lrss' insertKnowledgeSessionDetails method
         * @param timestamp - Knowledge Sessions' session start time
         * @param domainSessionId - Domain Session identifier for the actorSlug user
         * @param session - Individual Knowledge Session that was started
         * 
         * @throws LmsXapiGeneratorException when unable to set appender
         * @throws LmsXapiProfileException when unable to parse xAPI Profile
         */
        public Individual(String actorSlug, DateTime timestamp, Integer domainSessionId, IndividualKnowledgeSession session) 
                throws LmsXapiGeneratorException, LmsXapiProfileException {
            super(StartedKnowledgeSessionTemplate.IndividualTemplate.getInstance(), actorSlug, timestamp, domainSessionId, session);
        }
    }
    
    // Subclass for Team Knowledge Session Statement Object Template
    public static class Team extends StartedKnowledgeSessionGenerator {
        /**
         * Started Team Knowledge Session
         * 
         * @param actorSlug - studentId passed to Lrss' insertKnowledgeSessionDetails method
         * @param timestamp - Knowledge Sessions' session start time
         * @param domainSessionId - Domain Session identifier for the actorSlug user
         * @param session - Team Knowledge Session that was started
         * 
         * @throws LmsXapiGeneratorException when unable to set appender
         * @throws LmsXapiProfileException when unable to parse xAPI Profile
         */
        public Team(String actorSlug, DateTime timestamp, Integer domainSessionId, TeamKnowledgeSession session) 
                throws LmsXapiGeneratorException, LmsXapiProfileException {
            super(StartedKnowledgeSessionTemplate.TeamTemplate.getInstance(), actorSlug, timestamp, domainSessionId, session);
        }
    }
}
