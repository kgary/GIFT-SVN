package mil.arl.gift.lms.impl.lrs.xapi.generate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Result;
import com.rusticisoftware.tincan.Score;
import com.rusticisoftware.tincan.StatementTarget;

import generated.dkf.ActorTypeCategoryEnum;
import generated.dkf.Coordinate;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.SessionManager;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EnvironmentAdaptationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.append.AbstractStatementAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ContextCoordinateAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.CreateActorAdaptationAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.DomainSessionAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.HighlightAdaptationAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.KnowledgeSessionTypeAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.LocationInfoAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.PerformanceCharacteristicsAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.RemoveActorsAdaptationInstructorAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.ResultCoordinateAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.TeamAppender;
import mil.arl.gift.lms.impl.lrs.xapi.append.WeatherAdaptationAppender;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.EnvironmentAdaptationTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.EnvironmentAdaptationStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement;

/**
 * Generator for Environment Adaptation xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public class EnvironmentAdaptationGenerator extends AbstractStatementGenerator {
    /** User name of Statement Actor converted to MBOX IFI - must be passed down from processor */
    private String actorSlug;
    /** Name of the domain in which the adaptation happened - derived */
    private String courseName;
    /** Value of the script that determined the effect of the adaptation */
    private String script;
    /** Rate of FatigueRecovery | Endurance */
    private BigDecimal rate;
    /** Heading property of teleport adaptation */
    private Integer heading;
    /** Name of the Highlight Object */
    private String highlightName;
    /** Actor type for Remove Actor adapt */
    private ActorTypeCategoryEnum actorType;
    /** Knowledge Session parsed from SessionManager used within Team processing */
    private AbstractKnowledgeSession session;
    /** Environment Adaptation activity when actor experiences the adaptation or SubStatement when actor experiences another agent or group experience the adaptation */
    private StatementTarget stmtObj;
    /**
     * Sets common global state and common appenders
     * 
     * @param template - xAPI Statement Template describing produced statement
     * @param obj - xAPI Statement object (Activity or SubStatement)
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException - when unable to set appender
     */
    private EnvironmentAdaptationGenerator(StatementTemplate template, StatementTarget obj, AbstractKnowledgeSession knowledgeSession,
            Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        super(template, domainSessionId);
        if(StringUtils.isBlank(actorSlug)) {
            throw new IllegalArgumentException("actor slug can not be null!");
        }
        this.actorSlug = actorSlug;
        if(obj == null) {
            throw new IllegalArgumentException("statement target can not be null!");
        }
        this.stmtObj = obj;
        // Domain Session + Registration
        try {
            addStatementAppender(new DomainSessionAppender(domainSessionId));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add create new Domain Session Appender!", e);
        }
        // Knowledge Session Kind + Course Name derivation
        this.session = knowledgeSession;
        if(session != null) {
            // Knowledge Session Type Activity
            try {
                addStatementAppender(new KnowledgeSessionTypeAppender(session.getSessionType()));
            } catch (LmsXapiActivityException | LmsXapiProfileException e) {
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
        // Domain
        try {
            addStatementAppender(new DomainAppender(courseName));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to create new Domain Appender!", e);
        }
    }
    /**
     * sets TeamAppender when the actor is the only relevant session user
     * 
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param obj - xAPI Statement object (Activity or SubStatement)
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug, 
            EnvironmentAdaptationTemplate template, StatementTarget obj) throws LmsXapiGeneratorException {
        // Calls 'common global state' constructor
        this(template, obj, knowledgeSession, domainSessionId, actorSlug);
        // User names to include Team Role Activities for
        Set<String> relevantUsernames = new HashSet<String>();
        relevantUsernames.add(actorSlug);
        // Team Extension
        if(knowledgeSession != null && knowledgeSession.getTeamStructure() != null) {
            try {
                addStatementAppender(new TeamAppender(knowledgeSession, false, relevantUsernames));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add Team Appender!", e);
            }
        }
    }
    /**
     * Sets TeamAppender when actor is not the only relevant session member
     * 
     * @param otherUsernames - other session member user name(s)
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param obj - xAPI Statement object (Activity or SubStatement)
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> otherUsernames, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, StatementTarget obj) throws LmsXapiGeneratorException {
        // Calls 'common global state' constructor
        this(template, obj, knowledgeSession, domainSessionId, actorSlug);
        // User names to include Team Role Activities for
        if(otherUsernames == null) {
            throw new IllegalArgumentException("otherUsernames can not be null!");
        }
        otherUsernames.add(actorSlug);
        Set<String> relevantUsernames = new HashSet<String>();
        relevantUsernames.addAll(otherUsernames);
        // Team Extension
        if(knowledgeSession != null && knowledgeSession.getTeamStructure() != null) {
            try {
                addStatementAppender(new TeamAppender(knowledgeSession, false, relevantUsernames));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add Team Appender!", e);
            }
        }
    }
    /**
     * Sets TeamAppender when reference team member is not a session member
     * 
     * @param ref - Team Member reference from the adaptation which doesn't target the actorSlug and is not a session member
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param obj - xAPI Statement object (Activity or SubStatement)
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(MarkedTeamMember ref, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, StatementTarget obj) throws LmsXapiGeneratorException {
        // Calls 'common global state' constructor
        this(template, obj, knowledgeSession, domainSessionId, actorSlug);
        // Team Member ref is not a session member
        // -> prevent corresponding team unit from being pruned from team structure extension
        // -> ensure corresponding team unit echelon activity found within statement
        // -> ensure corresponding team role activity found within statement
        if(knowledgeSession != null && knowledgeSession.getTeamStructure() != null) {
            try {
                addStatementAppender(new TeamAppender(knowledgeSession, false, actorSlug, ref));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add Team Appender!", e);
            }
        }
    }
    /**
     * Sets TeamAppender when all reference team members are NPCs
     * 
     * @param refColl - collection of NPC team members references
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param obj - xAPI Statement object (Activity or SubStatement)
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(Set<MarkedTeamMember> refColl, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, StatementTarget obj) throws LmsXapiGeneratorException {
        // Calls 'common global state' constructor
        this(template, obj, knowledgeSession, domainSessionId, actorSlug);
        if(knowledgeSession != null && knowledgeSession.getTeamStructure() != null) {
            try {
                addStatementAppender(new TeamAppender(knowledgeSession, false, actorSlug, refColl));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add Team Appender!", e);
            }
        }
    }
    /**
     * Sets TeamAppender when referenced team members are a mix of NPCs and session members
     * 
     * @param otherUsernames - other session member user name(s)
     * @param refColl - collection of NPC team members references
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param obj - xAPI Statement object (Activity or SubStatement)
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> otherUsernames, Set<MarkedTeamMember> refColl, AbstractKnowledgeSession knowledgeSession,
            Integer domainSessionId, String actorSlug, EnvironmentAdaptationTemplate template, StatementTarget obj) throws LmsXapiGeneratorException {
        // Calls 'common global state' constructor
        this(template, obj, knowledgeSession, domainSessionId, actorSlug);
        if(otherUsernames == null) {
            throw new IllegalArgumentException("otherUsernames can not be null!");
        }
        otherUsernames.add(actorSlug);
        Set<String> relevantUsernames = new HashSet<String>();
        relevantUsernames.addAll(otherUsernames);
        if(knowledgeSession != null && knowledgeSession.getTeamStructure() != null) {
            try {
                addStatementAppender(new TeamAppender(knowledgeSession, false, relevantUsernames, refColl));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add Team Appender!", e);
            }
        }   
    }
    /**
     * Generator for Overcast environment adaptation xAPI Statement
     * 
     * @param adaptation - Overcast environment adaptation used to form corresponding extension
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.Overcast adaptation, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        try {
            addStatementAppender(new WeatherAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Weather Adaptation Appender!", e);
        }
    }
    /**
     * Generator for Fog environment adaptation xAPI Statement
     * 
     * @param adaptation - Fog environment adaptation used to form corresponding extension
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.Fog adaptation, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        try {
            addStatementAppender(new WeatherAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Weather Adaptation Appender!", e);
        }
    }
    /**
     * Generator for Rain environment adaptation xAPI Statement
     * 
     * @param adaptation - Rain environment adaptation used to form corresponding extension
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.Rain adaptation, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        try {
            addStatementAppender(new WeatherAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Weather Adaptation Appender!", e);
        }
    }
    /**
     * Generator for Script environment adaptation xAPI Statement
     * 
     * @param adaptation - Script environment adaptation used to set result.response
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.Script adaptation, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        if(StringUtils.isBlank(adaptation.getValue())) {
            throw new IllegalArgumentException("Script's value can not be null / empty!");
        }
        this.script = adaptation.getValue();
    }
    /**
     * Generator for CreateActors environment adaptation xAPI Statement
     * 
     * @param adaptation - CreateActor environment adaptation used to set corresponding and coordinate extensions
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.CreateActors adaptation, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        try {
            addStatementAppender(new ContextCoordinateAppender(adaptation.getCoordinate()));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Context Coordinate Appender!", e);
        }
        try {
            addStatementAppender(new CreateActorAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Create Actor Adaptation Appender!", e);
        }
    }
    /**
     * Generator for RemoveActors environment adaptation xAPI Statement
     * 
     * @param actorNameTargets - collection of target actor names
     * @param cordTargets - collection of target coordinates
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> actorNameTargets, List<Coordinate> cordTargets, ActorTypeCategoryEnum actorCategoryType,
            AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug, EnvironmentAdaptationTemplate template, 
            EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is NOT the only relevant session user' constructor
        this(actorNameTargets, knowledgeSession, domainSessionId, actorSlug, template, activity);
        // Set actor Category type in top level state for use within statement generation
        actorType = actorCategoryType;
        if(CollectionUtils.isNotEmpty(cordTargets)) {
            try {
                addStatementAppender(new ContextCoordinateAppender(cordTargets));
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiGeneratorException("Unable to add Context Coordiante Appender!", e);
            }
        }
        // EDGE CASE: Constructor adds actorSlug to collection for Team Appender Handling
        //            Remove the last entry from the list to ensure accurate instructor members
        List<String> onlyTargets = new ArrayList<String>(actorNameTargets);
        onlyTargets.remove(actorNameTargets.size() - 1);
        if(CollectionUtils.isNotEmpty(onlyTargets)) {
            try {
                addStatementAppender(new RemoveActorsAdaptationInstructorAppender(onlyTargets));
            } catch (LmsXapiAppenderException e) {
                throw new LmsXapiGeneratorException("Unable to add Remove Actor Adaptation Instructor Appender!", e);
            }
        }
    }
    /**
     * Generator for FatigueRecovery and Endurance environment adaptation xAPI Statement when the actor is experiencing the adaptation
     * 
     * @param rate - value from environment adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(BigDecimal rate, EnvironmentAdaptationTemplate template, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        if(rate == null) {
            throw new IllegalArgumentException("rate can not be null!");
        }
        this.rate = rate;
    }
    /**
     * Generator for Teleport environment adaptation xAPI Statement
     * 
     * @param coordinate - location within virtual environment used to set corresponding extension
     * @param heading - value from environment adaptation message used to set result.response
     * @param template - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(Coordinate coordinate, Integer heading, EnvironmentAdaptationTemplate template, AbstractKnowledgeSession knowledgeSession,
            Integer domainSessionId, String actorSlug, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        this.heading = heading;
        try {
            addStatementAppender(new ResultCoordinateAppender(coordinate));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Result Coordinate Appender!", e);
        }
    }
    /**
     * Generator for Teleport environment adaptation xAPI Statement where actor is experiencing another session member experience the adaptation
     * 
     * @param otherUsernames - collection of user name(s) relevant to the experience
     * @param coordinate - location within virtual environment used to set corresponding extension
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> otherUsernames, Coordinate coordinate, EnvironmentAdaptationSubStatement subStatement,
            EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'actor is NOT the only relevant session user' constructor
        this(otherUsernames, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new ResultCoordinateAppender(coordinate));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Result Coordinate Appender!", e);
        }
    }
    /**
     * Generator for Teleport environment adaptation xAPI Statement where actor is experiencing a NPC experience the adaptation
     * 
     * @param ref - TeamMember reference that points to NPC
     * @param coordinate - location within virtual environment used to set corresponding extension
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(MarkedTeamMember ref, Coordinate coordinate, EnvironmentAdaptationSubStatement subStatement, 
            EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'reference team member is NOT session user' constructor
        this(ref, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new ResultCoordinateAppender(coordinate));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Result Coordinate Appender!", e);
        }
    }
    /**
     * Generator for Highlight Object environment adaptation xAPI Statement where actor is experiencing the highlight
     * 
     * @param adaptation - Highlight environment adaptation
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.HighlightObjects adaptation, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId,
            String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        if(StringUtils.isBlank(adaptation.getName())) {
            throw new IllegalArgumentException("highlight name can not be null!");
        }
        this.highlightName = adaptation.getName();
        try {
            addStatementAppender(new HighlightAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Highlight Adaptation Appender!", e);
        }
    }
    /**
     * Generator for Highlight Object environment adaptation xAPI Statement where actor is experiencing the highlight of a location
     * 
     * @param locInfo - Location Info for the highlight
     * @param adaptation - Highlight environment adaptation
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.HighlightObjects.LocationInfo locInfo, EnvironmentAdaptation.HighlightObjects adaptation, 
            AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug,
            EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor + 'Highlight Object' constructor
        this(adaptation, knowledgeSession, domainSessionId, actorSlug, template, activity);
        try {
            addStatementAppender(new LocationInfoAppender(locInfo));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Location Info Appender!", e);
        }
    }
    /**
     * Generator for Highlight Object environment adaptation xAPI Statement where actor is experiencing the highlight of another player
     * 
     * @param otherUsernames - collection of user name(s) relevant to the experience
     * @param adaptation - Highlight environment adaptation
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> otherUsernames, EnvironmentAdaptation.HighlightObjects adaptation, EnvironmentAdaptationSubStatement subStatement,
            EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'actor is NOT the only relevant session user' constructor
        this(otherUsernames, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new HighlightAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Highlight Adaptation Appender!", e);
        }
    }
    /**
     * Generator for Highlight Object environment adaptation xAPI Statement where actor is experiencing the highlight of an NPC
     * 
     * @param ref - TeamMember reference that points to NPC
     * @param adaptation - Highlight environment adaptation
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(MarkedTeamMember ref, EnvironmentAdaptation.HighlightObjects adaptation, EnvironmentAdaptationSubStatement subStatement,
            EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'reference team member is NOT session user' constructor
        this(ref, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new HighlightAdaptationAppender(adaptation));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Highlight Adaptation Appender!", e);
        }
    }
    /**
     * Generator for Remove Highlight environment adaptation xAPI Statement
     * 
     * @param adaptation - Remove Highlight environment adaptation
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * @param template - xAPI Statement Template describing produced statement
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptation.RemoveHighlightOnObjects adaptation, AbstractKnowledgeSession knowledgeSession,
            Integer domainSessionId, String actorSlug, EnvironmentAdaptationTemplate template, EnvironmentAdaptationActivity activity) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        if(adaptation == null || adaptation.getHighlightName() == null) {
            throw new IllegalArgumentException("adaptation and its name can not be null!");
        }
        this.highlightName = adaptation.getHighlightName();
    }
    /**
     * Generator for Create bread crumbs environment adaptation xAPI Statement where the actor is experiencing the adaptation
     * 
     * @param activity - Environment Adaptation Activity used as Object of xAPI Statement
     * @param locInfo - Location info from the adaptation
     * @param template - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(EnvironmentAdaptationActivity activity, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo,
            EnvironmentAdaptationTemplate template, AbstractKnowledgeSession knowledgeSession, Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'actor is the only relevant session user' constructor
        this(knowledgeSession, domainSessionId, actorSlug, template, activity);
        try {
            addStatementAppender(new LocationInfoAppender(locInfo));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Location Info Appender!", e);
        }    
    }
    /**
     * Generator for Create bread crumbs environment adaptation xAPI Statement where the actor is experiencing the application of adaptation to an NPC
     * 
     * @param ref - reference to NPC
     * @param locInfo - Location info from the adaptation
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(MarkedTeamMember ref, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo,
            EnvironmentAdaptationSubStatement subStatement, EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession, 
            Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'reference team member is NOT session user' constructor
        this(ref, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new LocationInfoAppender(locInfo));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Location Info Appender!", e);
        }
    }
    /**
     * Generator for Create bread crumbs environment adaptation xAPI Statement where the actor is experiencing the application of adaptation to session members
     * 
     * @param otherUsernames - collection of user name(s) relevant to the experience
     * @param locInfo - Location info from the adaptation
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> otherUsernames, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo,
            EnvironmentAdaptationSubStatement subStatement, EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession, 
            Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'actor is NOT the only relevant session user' constructor
        this(otherUsernames, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new LocationInfoAppender(locInfo));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Location Info Appender!", e);
        }
    }
    /**
     * Generator for Create bread crumbs environment adaptation xAPI Statement where the actor is experiencing the application of adaptation to NPCs
     * 
     * @param refColl - reference to NPCs
     * @param locInfo - Location info from the adaptation
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(Set<MarkedTeamMember> refColl, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo, 
            EnvironmentAdaptationSubStatement subStatement, EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession,
            Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'all NPCs' constructor
        this(refColl, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new LocationInfoAppender(locInfo));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Location Info Appender!", e);
        }
    }
    /**
     * Generator for Create bread crumbs environment adaptation xAPI Statement where the actor is experiencing the application of adaptation to NPCs and
     * session members
     * 
     * @param otherUsernames - collection of user name(s) relevant to the experience
     * @param refColl - reference to NPCs
     * @param locInfo - Location info from the adaptation
     * @param subStatement - statement describing the adaptation experience
     * @param nestedTemplate - xAPI Statement Template describing produced statement
     * @param knowledgeSession - knowledge session in which adaptation occurred
     * @param domainSessionId - numeric id for the domain session
     * @param actorSlug - user name from the Environment Adaptation message
     * 
     * @throws LmsXapiGeneratorException when unable to set appender
     */
    public EnvironmentAdaptationGenerator(List<String> otherUsernames, Set<MarkedTeamMember> refColl, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo,
            EnvironmentAdaptationSubStatement subStatement, EnvironmentAdaptationTemplate nestedTemplate, AbstractKnowledgeSession knowledgeSession,
            Integer domainSessionId, String actorSlug) throws LmsXapiGeneratorException {
        // Calls 'NPCs + session members' constructor
        this(otherUsernames, refColl, knowledgeSession, domainSessionId, actorSlug, nestedTemplate, subStatement);
        try {
            addStatementAppender(new LocationInfoAppender(locInfo));
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to add Location Info Appender!", e);
        }
    }
    
    /**
     * Add PerformanceCharacteristicsAppender to collection of statement appenders
     * 
     * @param envControl - Environment Control to represent within extension
     * 
     * @throws LmsXapiGeneratorException when unable to set appender or the set of appenders already
     * includes a Performance Characteristics Appender
     */
    public void addPcAppender(EnvironmentControl envControl) throws LmsXapiGeneratorException {
        for(AbstractStatementAppender appender : getStatementAppenders()) {
            if(appender instanceof PerformanceCharacteristicsAppender) {
                throw new LmsXapiGeneratorException("There is already a Performance Characteristics Appender attached to this generator!");
            }
        }
        PerformanceCharacteristicsAppender appender;
        try {
            appender = new PerformanceCharacteristicsAppender();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to init Performance Characteristics Appender!", e);
        }
        appender.addEnvironmentControl(envControl);
        addStatementAppender(appender);
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
        // Statement
        EnvironmentAdaptationStatement statement;
        try {
            statement = new EnvironmentAdaptationStatement(actor, stmtObj);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiGeneratorException("Unable to generate Environmental Adaptation statement!", e);
        }
        // Statement Result
        Result result = statement.getResult();
        // Script as result response
        if(stmtObj instanceof EnvironmentAdaptationActivity.Script) {
            result.setResponse(script);
            statement.setResult(result);
        }
        // Highlight name as result response
        if(stmtObj instanceof EnvironmentAdaptationActivity.HighlightObjects || stmtObj instanceof EnvironmentAdaptationActivity.RemoveHighlight) {
            result.setResponse(highlightName);
            statement.setResult(result);
        }
        // Fatigue Recovery + Endurance rate as raw score within flat statement
        if(stmtObj instanceof EnvironmentAdaptationActivity.FatigueRecovery || stmtObj instanceof EnvironmentAdaptationActivity.Endurance) {
            Score score = result.getScore() != null ? result.getScore() : new Score();
            score.setRaw(rate.doubleValue());
            result.setScore(score);
            statement.setResult(result);
        }
        if(stmtObj instanceof EnvironmentAdaptationActivity.Teleport && heading != null) {
            result.setResponse(heading.toString());
            statement.setResult(result);
        }
        if(stmtObj instanceof EnvironmentAdaptationActivity.RemoveActors && actorType != null) {
            result.setResponse(actorType.value());
            statement.setResult(result);
        }
        // Statement Id generation and return
        return generateStatement(statement, deriveId);
    }
}
