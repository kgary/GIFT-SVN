package mil.arl.gift.lms.impl.lrs.xapi.processor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Statement;
import generated.dkf.ActorTypeCategoryEnum;
import generated.dkf.Coordinate;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProcessorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.TeamHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.EnvironmentAdaptationActivity;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.EnvironmentAdaptationGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.templates.EnvironmentAdaptationTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.statements.EnvironmentAdaptationStatement;
import mil.arl.gift.lms.impl.lrs.xapi.statements.EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement;

/**
 * Processes Environment Adaptation in order to determine xAPI Statement Template, 
 * Environment Adaptation Activity, and appropriate xAPI Statement Generator.
 *  
 * @author Yet Analytics
 *
 */
public class EnvironmentAdaptationProcessor extends AbstractProcessor {
    /** Container for Environment Adaptation and associated stress value */
    private EnvironmentControl envControl;
    /** The Environment Adaptation to process into xAPI Statement */    
    private EnvironmentAdaptation adaptation;
    /** Knowledge Session for the session in which the Environment Adaptation happens */
    private AbstractKnowledgeSession session;
    /** xAPI Statement Template describing the generated xAPI Statement */
    private EnvironmentAdaptationTemplate template;
    /** The xAPI Activity corresponding to the Environment Adaptation */
    private EnvironmentAdaptationActivity activity;
    /** xAPI Statement Generator corresponding to the Environment Adaptation and configured based on contained properties */
    private AbstractStatementGenerator generator;
    /** xAPI Agent or Group used as the Actor within a sub statement */
    private Agent subStatementActor;
    /** boolean indicating if a TeamMemberRef points to a member of the knowledge session */
    private boolean isSessionMember;
    /** collection of knowledge session user names relevant to the adaptation */
    private List<String> otherUsernames;
    /** collection of references to NPC(s) within the virtual environment related to the adaptation */
    private Set<MarkedTeamMember> npcRefs;
    /** are any of the TeamMemberRefs pointing to session members */
    private boolean sessionMemberRefs;
    /** are any of the TeamMemberRefs pointing to non session members */
    private boolean npcMemberRefs;
    /**
     * Initialize global state used within processor methods
     * 
     * @param environmentAdaptation - environment adaptation to process into xAPI Statement
     * @param actorSlug - user name from environment adaptation message
     * @param domainSessionId - numeric id for the domain session
     * @param knowledgeSession - knowledge session in which adaptation occurred
     */
    private EnvironmentAdaptationProcessor(EnvironmentAdaptation adapt, String actorSlug,
            Integer domainSessionId, AbstractKnowledgeSession knowledgeSession) {
        super(actorSlug, domainSessionId);
        if(adapt == null) {
            throw new IllegalArgumentException("Environment Control can not be null!");
        }
        this.adaptation = adapt;
        this.session = knowledgeSession;
    }
    /**
     * Initialize global state used within processor methods
     * 
     * @param eControl - environment adaptation with associated stress value
     * @param actorSlug - user name from environment adaptation message
     * @param domainSessionId - numeric id for the domain session
     * @param knowledgeSession - knowledge session in which adaptation occurred
     */
    public EnvironmentAdaptationProcessor(EnvironmentControl eControl, String actorSlug,
            Integer domainSessionId, AbstractKnowledgeSession knowledgeSession) throws LmsXapiActivityException {
        this(eControl.getEnvironmentStatusType(), actorSlug, domainSessionId, knowledgeSession);
        this.envControl = eControl;
        this.activity = EnvironmentAdaptationActivity.dispatch(adaptation);
    }
    
    /**
     * Creates agent from user name corresponding to team member reference or from the
     * team member reference identifier
     * 
     * @param refTeamMember - MarkedTeamMember created from Team Member Reference within environment adaptation
     * 
     * @return xAPI Agent to include within sub statement
     * 
     * @throws LmsXapiAgentException when unable to create xAPI Agent
     */
    private Agent subStatementAgent(MarkedTeamMember refTeamMember) throws LmsXapiAgentException {
        if(refTeamMember == null) {
            throw new IllegalArgumentException("refTeamMember can not be null!");
        }
        if(isSessionMember) {
            String refUsername = TeamHelper.refUsername(session, refTeamMember);
            if(refUsername == null) {
                throw new LmsXapiAgentException("Unable to derive username for Team Member Ref!");
            }
            return PersonaHelper.createMboxAgent(refUsername);
        } else {
            return PersonaHelper.createMboxAgent(refTeamMember.getIdentifier());
        }
    }
    
    /**
     * Configure xAPI Statement generator components for Overcast Environment Adaptation and set generator
     * 
     * @param adapt - Overcast environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.Overcast adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.Weather.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Weather xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to create Overcast xAPI Statement Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for Fog Environment Adaptation and set generator
     * 
     * @param adapt - Fog environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.Fog adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.Weather.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Weather xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to create Fog xAPI Statement Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for Rain Environment Adaptation and set generator
     * 
     * @param adapt - Rain environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.Rain adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.Weather.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Weather xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to create Rain xAPI Statement Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for Script Environment Adaptation and set generator
     * 
     * @param adapt - Script environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.Script adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.Script.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Script xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to create Script xAPI Statement Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for TimeOfDay Environment Adaptation and set generator
     * 
     * @param adapt - TimeOfDay environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.TimeOfDay adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.TimeOfDay.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve TimeOfDay xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to create TimeOfDay xAPI Statement Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for CreateActors Environment Adaptation and set generator
     * 
     * @param adapt - CreateActors environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.CreateActors adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.CreateActors.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Create Actors xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to create Create Actors xAPI Statement Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for RemoveActors Environment Adaptation and set generator
     * 
     * @param adapt - RemoveActors environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.RemoveActors adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.RemoveActors.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Remove Actors xAPI Statement Template from xAPI Profile", e);
        }
        Serializable target = adapt.getType();
        List<Coordinate> cordTargets = new ArrayList<Coordinate>(1);
        List<String> actorNameTargets = new ArrayList<String>(1);
        ActorTypeCategoryEnum actorTypeCategory = adapt.getTypeCategory();
        if(target instanceof EnvironmentAdaptation.RemoveActors.Location) {
            cordTargets.add(((EnvironmentAdaptation.RemoveActors.Location) target).getCoordinate());
        } else if(target instanceof String) {
            // NOTE: actorNames assumed to point to session members (not NPCs).
            //       Implications on TeamAppender; NPC require TeamMemberRef to resolve their TeamRole activity and inclusion
            //       within Team Structure context extension
            actorNameTargets.add((String) target);
        } else {
            throw new LmsXapiProcessorException("unable to create Remove Actors xAPI Statement from unexpected type!");
        }
        try {
            generator = new EnvironmentAdaptationGenerator(actorNameTargets, cordTargets, actorTypeCategory, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to initialize Remove Actors Environment Adaptation Generator!", e);
        }
    }
    
    /**
     * Handles common state for generators with agent actor sub statements
     * 
     * @param refTeamMember - reference to team member that may be a session member
     * 
     * @throws LmsXapiProcessorException when unable to to create Agent from reference
     */
    private void configureSubstatementActor(MarkedTeamMember refTeamMember) throws LmsXapiProcessorException {
        if(refTeamMember == null) {
            throw new IllegalArgumentException("refTeamMember can not be null!");
        }
        // Does the TeamMember reference point to a knowledge session member
        isSessionMember = TeamHelper.isRefSessionMember(session, refTeamMember);
        try {
            subStatementActor = subStatementAgent(refTeamMember);
        } catch (LmsXapiAgentException e) {
            throw new LmsXapiProcessorException("Unable to create Agent from Team Member Ref!", e);
        }
        if(isSessionMember) {
            // User names to include Team Role Activities for (generator handles addition of actor slug)
            List<String> usernames = new ArrayList<String>();
            // User name for TeamMemberRef
            String refUsername = subStatementActor.getName();
            usernames.add(refUsername);
            otherUsernames = usernames;
        }
    }
    
    /**
     * Handles common state for generators with group actor sub statements
     * 
     * @param memberColl - collection of references to team members that may be also session members
     * 
     * @throws LmsXapiProcessorException when unable to to create Agent from reference
     */
    private void configureSubstatementActor(List<MarkedTeamMember> memberColl) throws LmsXapiProcessorException {
        if(memberColl == null) {
            throw new IllegalArgumentException("memberColl can not be null!");
        }
        // Split refColl into session members and NPCs coll for team appender
        List<String> usernames = new ArrayList<String>();
        Set<MarkedTeamMember> npcs = new HashSet<MarkedTeamMember>();
        // sub statement group actor member 
        List<Agent> groupMembers = new ArrayList<Agent>();
        for(MarkedTeamMember refTeamMember : memberColl) {
            // Session Membership determination
            isSessionMember = TeamHelper.isRefSessionMember(session, refTeamMember);
            // Agent for group member
            Agent refTeamMemberAgent;
            try {
                refTeamMemberAgent = subStatementAgent(refTeamMember);
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiProcessorException("Unable to create xAPI Agent from referenced team member! "+refTeamMember, e);
            }
            groupMembers.add(refTeamMemberAgent);
            if(isSessionMember) {
                String refUsername = refTeamMemberAgent.getName();
                usernames.add(refUsername);
            } else {
                npcs.add(refTeamMember);
            }
        }
        // sub statement group actor
        otherUsernames = usernames;
        npcRefs = npcs;
        subStatementActor = PersonaHelper.createGroup(groupMembers);
        sessionMemberRefs = CollectionUtils.isNotEmpty(usernames);
        npcMemberRefs = CollectionUtils.isNotEmpty(npcs);
    }
    
    /**
     * Configure xAPI Statement generator components for Teleport Environment Adaptation and set generator
     * 
     * @param adapt - Teleport environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.Teleport adapt) throws LmsXapiProcessorException {
        // parse adaptation
        Coordinate coordinate = adapt.getCoordinate();
        Integer heading = null;
        if(adapt.getHeading() != null) {
            heading = adapt.getHeading().getValue();
        }
        // NOTE: only MarkedTeamMember is supported
        MarkedTeamMember refTeamMember = null;
        EnvironmentAdaptation.Teleport.TeamMemberRef ref = adapt.getTeamMemberRef();
        if(ref != null) {
            refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
        }
        // Activity used as Statement Object or SubStatement Object
        if(refTeamMember == null || TeamHelper.isActorTeamMemberRef(actorSlug, session, refTeamMember)) {
            // Template for flat Statement
            try {
                template = EnvironmentAdaptationTemplate.Teleport.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve flat Teleport xAPI Statement Template from xAPI Profile", e);
            }
            try {
                generator = new EnvironmentAdaptationGenerator(coordinate, heading, template, session, domainSessionId, actorSlug, activity);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("Unable to initialize flat Teleport Environment Adaptation Generator!", e);
            }
        } else {
            // Template for nested Statement
            try {
                template = EnvironmentAdaptationTemplate.TeleportNested.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve nested Teleport xAPI Statement Template from xAPI Profile", e);
            }
            // SubStatement Actor creation + session membership determination + otherUsernames creation + sets isSessionMember
            configureSubstatementActor(refTeamMember);
            // Sub Statement containing activity as object + heading as result.response
            EnvironmentAdaptationSubStatement subStatement;
            try {
                subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity, heading);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to initialize Fatigue Recovery Activity!", e);
            }
            // Generator determination
            if(isSessionMember) {
                try {
                    generator = new EnvironmentAdaptationGenerator(otherUsernames, coordinate, subStatement, template, session, domainSessionId, actorSlug);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize nested Teleport Environment Adaptation Generator!", e);
                }
            } else {
                try {
                    generator = new EnvironmentAdaptationGenerator(refTeamMember, coordinate, subStatement, template, session, domainSessionId, actorSlug);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize nested Teleport Environment Adaptation Generator!", e);
                }
            }
        }
    }
    
    /**
     * Configure xAPI Statement generator components for Fatigue Recovery Environment Adaptation and set generator
     * 
     * @param adapt - FatigueRecovery environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.FatigueRecovery adapt) throws LmsXapiProcessorException {
        // parse adaptation
        EnvironmentAdaptation.FatigueRecovery.TeamMemberRef ref = adapt.getTeamMemberRef();
        MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
        BigDecimal rate = adapt.getRate();
        // Activity used as Statement Object or SubStatement Object
        if(TeamHelper.isActorTeamMemberRef(actorSlug, session, refTeamMember)) {
            // Template for flat Statement
            try {
                template = EnvironmentAdaptationTemplate.PlayerStatus.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve flat Fatigue Recovery xAPI Statement Template from xAPI Profile", e);
            }
            // xAPI Statement where object is activity
            try {
                generator = new EnvironmentAdaptationGenerator(rate, template, session, domainSessionId, actorSlug, activity);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("Unable to initialize flat Fatigue Recovery Environment Adaptation Generator!", e);
            }
        } else {
            // Template for nested Statement
            try {
                template = EnvironmentAdaptationTemplate.PlayerStatusNested.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to initialize nested Fatigue Recovery Environment Adaptation Generator!", e);
            }
            // SubStatement Actor creation + session membership determination + otherUsernames creation + sets isSessionMember
            configureSubstatementActor(refTeamMember);
            // Sub Statement
            EnvironmentAdaptationSubStatement subStatement;
            try {
                subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity, rate);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to initialize Fatigue Recovery Activity!", e);
            }
            if(isSessionMember) {
                try {
                    generator = new EnvironmentAdaptationGenerator(otherUsernames, session, domainSessionId, actorSlug, template, subStatement);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize nested Fatigue Recovery Environment Adaptation Generator!", e);
                }
            } else {
                try {
                    generator = new EnvironmentAdaptationGenerator(refTeamMember, session, domainSessionId, actorSlug, template, subStatement);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize nested Fatigue Recovery Environment Adaptation Generator!", e);
                }
            }
        }
    }
    
    /**
     * Configure xAPI Statement generator components for Endurance Environment Adaptation and set generator
     * 
     * @param adapt - Endurance environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.Endurance adapt) throws LmsXapiProcessorException {
        // parse adaptation
        EnvironmentAdaptation.Endurance.TeamMemberRef ref = adapt.getTeamMemberRef();
        MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
        BigDecimal rate = adapt.getValue();
        if(TeamHelper.isActorTeamMemberRef(actorSlug, session, refTeamMember)) {
            // Template for flat Statement
            try {
                template = EnvironmentAdaptationTemplate.PlayerStatus.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve flat Endurance xAPI Statement Template from xAPI Profile", e);
            }
            // xAPI Statement where object is activity
            try {
                generator = new EnvironmentAdaptationGenerator(rate, template, session, domainSessionId, actorSlug, activity);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("Unable to initialize flat Endurance Environment Adaptation Generator!", e);
            }
        } else {
            // Template for nested Statement
            try {
                template = EnvironmentAdaptationTemplate.PlayerStatusNested.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve nested Endurance xAPI Statement Template from xAPI Profile", e);
            }
            // SubStatement Actor creation + session membership determination + otherUsernames creation + sets isSessionMember
            configureSubstatementActor(refTeamMember);
            // Sub Statement
            EnvironmentAdaptationSubStatement subStatement;
            try {
                subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity, rate);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to initialize Fatigue Recovery Activity!", e);
            }
            if(isSessionMember) {
                try {
                    generator = new EnvironmentAdaptationGenerator(otherUsernames, session, domainSessionId, actorSlug, template, subStatement);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize nested Endurance Environment Adaptation Generator!", e);
                }
            } else {
                try {
                    generator = new EnvironmentAdaptationGenerator(refTeamMember, session, domainSessionId, actorSlug, template, subStatement);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize nested Endurance Environment Adaptation Generator!", e);
                }
            }
        }
    }
    
    /**
     * Configure xAPI Statement generator components for HighlightObjects Environment Adaptation and set generator
     * 
     * @param adapt - HighlightObjects environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.HighlightObjects adapt) throws LmsXapiProcessorException {
        Serializable kind = adapt.getType();
        // Location or Team Member Reference
        if(kind instanceof EnvironmentAdaptation.HighlightObjects.LocationInfo) {
            EnvironmentAdaptation.HighlightObjects.LocationInfo locInfo = (EnvironmentAdaptation.HighlightObjects.LocationInfo) kind;
            // Template for location highlight Statement
            try {
                template = EnvironmentAdaptationTemplate.HighlightObjectsLocation.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve highlight location xAPI Statement Template!", e);
            }
            // xAPI Statement where object is activity and includes location info result extension
            try {
                generator = new EnvironmentAdaptationGenerator(locInfo, adapt, session, domainSessionId, actorSlug, template, activity);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("Unable to initialize Highlight Location Environment Adaptation Generator!", e);
            }
        } else if(kind instanceof EnvironmentAdaptation.HighlightObjects.TeamMemberRef) {
            EnvironmentAdaptation.HighlightObjects.TeamMemberRef ref = (EnvironmentAdaptation.HighlightObjects.TeamMemberRef) kind;
            MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
            if(TeamHelper.isActorTeamMemberRef(actorSlug, session, refTeamMember)) {
                // Template for flat Statement
                try {
                    template = EnvironmentAdaptationTemplate.HighlightObjectsActor.getInstance();
                } catch (LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("unable to resolve highlight actor xAPI Statement Template from xAPI Profile", e);
                }
                // xAPI Statement where object is activity
                try {
                    generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("Unable to initialize Highlight Actor Environment Adaptation Generator!", e);
                }
            } else {
                String name = adapt.getName();
                // Template for nested Statement
                try {
                    template = EnvironmentAdaptationTemplate.HighlightObjectsAgent.getInstance();
                } catch (LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("unable to resolve highlight agent xAPI Statement Template from xAPI Profile", e);
                }
                // SubStatement Actor creation + session membership determination + otherUsernames creation + sets isSessionMember
                configureSubstatementActor(refTeamMember);
                // Sub Statement
                EnvironmentAdaptationSubStatement subStatement;
                try {
                    subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity, name);
                } catch (LmsXapiProfileException e) {
                    throw new LmsXapiProcessorException("Unable to initialize Fatigue Recovery Activity!", e);
                }
                if(isSessionMember) {
                    try {
                        generator = new EnvironmentAdaptationGenerator(otherUsernames, adapt, subStatement, template, session, domainSessionId, actorSlug);
                    } catch (LmsXapiGeneratorException e) {
                        throw new LmsXapiProcessorException("unable to resolve highlight agent Environment Adaptation Generator!", e);
                    }
                } else {
                    try {
                        generator = new EnvironmentAdaptationGenerator(refTeamMember, adapt, subStatement, template, session, domainSessionId, actorSlug);
                    } catch (LmsXapiGeneratorException e) {
                        throw new LmsXapiProcessorException("unable to resolve highlight npc Environment Adaptation Generator!", e);
                    }
                }
            }
        } else {
            throw new LmsXapiProcessorException("Unsupported Highlight Objects type! "+adapt);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for RemoveHighlightOnObjects Environment Adaptation and set generator
     * 
     * @param adapt - RemoveHighlightOnObjects environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.RemoveHighlightOnObjects adapt) throws LmsXapiProcessorException {
        try {
            template = EnvironmentAdaptationTemplate.RemoveHighlight.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve Remove Highlight xAPI Statement Template from xAPI Profile", e);
        }
        try {
            generator = new EnvironmentAdaptationGenerator(adapt, session, domainSessionId, actorSlug, template, activity);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("unable to initialize Remove Highlight Environment Adaptation Generator!", e);
        }
    }
    
    /**
     * Configure xAPI Statement generator components for CreateBreadcrumbs Environment Adaptation and set generator when the 
     * adaptation contains a single Team Member Reference
     * 
     * @param ref - single Team Member Reference from list of references
     * @param locInfo - location info from the adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void handleSingleMemberRef(EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef ref, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo) throws LmsXapiProcessorException {
        MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
        if(TeamHelper.isActorTeamMemberRef(actorSlug, session, refTeamMember)) {
            // Template for flat Statement
            try {
                template = EnvironmentAdaptationTemplate.CreateBreadcrumbsActor.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve actor breadcrumbs xAPI Statement Template from xAPI Profile", e);
            }
            // xAPI Statement where object is activity
            try {
                generator = new EnvironmentAdaptationGenerator(activity, locInfo, template, session, domainSessionId, actorSlug);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("Unable to initialize Actor Breadcrumbs Environment Adaptation Generator!", e);
            }
        } else {
            // Template for nested Statement
            try {
                template = EnvironmentAdaptationTemplate.CreateBreadcrumbsAgent.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve agent breadcrumbs xAPI Statement Template from xAPI Profile", e);
            }
            // SubStatement Actor creation + session membership determination + otherUsernames creation + set isSessionMember
            configureSubstatementActor(refTeamMember);
            // Sub Statement
            EnvironmentAdaptationSubStatement subStatement;
            try {
                subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to create agent bread crumbs substatement!", e);
            }
            if(isSessionMember) {
                try {
                    generator = new EnvironmentAdaptationGenerator(otherUsernames, locInfo, subStatement, template, session, domainSessionId, actorSlug);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("unable to resolve session member breadcrumbs Environment Adaptation Generator!", e);
                }
            } else {
                try {
                    generator = new EnvironmentAdaptationGenerator(refTeamMember, locInfo, subStatement, template, session, domainSessionId, actorSlug);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("unable to resolve npc breadcrumbs Environment Adaptation Generator!", e);
                }
            }
        }
    }
    
    /**
     * Configure xAPI Statement generator components for RemoveBreadcrumbs Environment Adaptation and set generator when the 
     * adaptation contains a single Team Member Reference
     * 
     * @param ref - single Team Member Reference from list of references
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void handleSingleMemberRef(EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef ref) throws LmsXapiProcessorException {
        MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
        if(TeamHelper.isActorTeamMemberRef(actorSlug, session, refTeamMember)) {
            // Template for flat Statement
            try {
                template = EnvironmentAdaptationTemplate.RemoveBreadcrumbsActor.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve remove actor breadcrumbs xAPI Statement Template from xAPI Profile", e);
            }
            try {
                generator = new EnvironmentAdaptationGenerator(session, domainSessionId, actorSlug, template, activity);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("Unable to initialize remove Actor Breadcrumbs Environment Adaptation Generator!", e);
            }
        } else {
            // Template for nested Statement
            try {
                template = EnvironmentAdaptationTemplate.RemoveBreadcrumbsAgent.getInstance();
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("unable to resolve remove agent breadcrumbs xAPI Statement Template from xAPI Profile", e);
            }
            // SubStatement Actor creation + session membership determination + otherUsernames creation + set isSessionMember
            configureSubstatementActor(refTeamMember);
            // Sub Statement
            EnvironmentAdaptationSubStatement subStatement;
            try {
                subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity);
            } catch (LmsXapiProfileException e) {
                throw new LmsXapiProcessorException("Unable to create agent bread crumbs substatement!", e);
            }
            if(isSessionMember) {
                try {
                    generator = new EnvironmentAdaptationGenerator(otherUsernames, session, domainSessionId, actorSlug, template, subStatement);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("unable to resolve remove session member breadcrumbs Environment Adaptation Generator!", e);
                }
            } else {
                try {
                    generator = new EnvironmentAdaptationGenerator(refTeamMember, session, domainSessionId, actorSlug, template, subStatement);
                } catch (LmsXapiGeneratorException e) {
                    throw new LmsXapiProcessorException("unable to resolve remove npc breadcrumbs Environment Adaptation Generator!", e);
                }
            }
        }
    }
    
    /**
     * Configure xAPI Statement generator components for CreateBreadcrumbs Environment Adaptation and set generator when the
     * adaptation contains more than one Team Member Reference
     * 
     * @param memberColl - collection of Marked Team Members created from list of references
     * @param locInfo - location info from the adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void handleManyMemberRef(List<MarkedTeamMember> memberColl, EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo) throws LmsXapiProcessorException {
        // Template for nested statement with group as sub statement actor
        try {
            template = EnvironmentAdaptationTemplate.CreateBreadcrumbsGroup.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve agent breadcrumbs xAPI Statement Template from xAPI Profile", e);
        }
        // Handle subStatement Group Actor + NPC / Session Member split + boolean setting
        configureSubstatementActor(memberColl);
        // sub statement
        EnvironmentAdaptationSubStatement subStatement;
        try {
            subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to create group bread crumbs substatement!", e);
        }
        if(sessionMemberRefs && npcMemberRefs) {
            try {
                generator = new EnvironmentAdaptationGenerator(otherUsernames, npcRefs, locInfo, subStatement, template, session, domainSessionId, actorSlug);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("unable to resolve session members + NPCs group breadcrumbs Environment Adaptation Generator!", e);
            }
        } else if(sessionMemberRefs && !npcMemberRefs) {
            try {
                generator = new EnvironmentAdaptationGenerator(otherUsernames, locInfo, subStatement, template, session, domainSessionId, actorSlug);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("unable to resolve session members group breadcrumbs Environment Adaptation Generator!", e);
            }
        } else if(!sessionMemberRefs && npcMemberRefs) {
            try {
                generator = new EnvironmentAdaptationGenerator(npcRefs, locInfo, subStatement, template, session, domainSessionId, actorSlug);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("unable to resolve NPCs group breadcrumbs Environment Adaptation Generator!", e);
            }
        } else {
            throw new LmsXapiProcessorException("Team Member references were unable to be split into session members vs NPCs!");
        }
    }
    
    /**
     * Configure xAPI Statement generator components for RemoveBreadcrumbs Environment Adaptation and set generator when the
     * adaptation contains more than one Team Member Reference
     * 
     * @param memberColl - collection of Marked Team Members created from list of references
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void handleManyMemberRef(List<MarkedTeamMember> memberColl) throws LmsXapiProcessorException {
        // Template for nested statement with group as sub statement actor
        try {
            template = EnvironmentAdaptationTemplate.RemoveBreadcrumbsGroup.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("unable to resolve agent breadcrumbs xAPI Statement Template from xAPI Profile", e);
        }
        // Handle subStatement Group Actor + NPC / Session Member split + sets booleans
        configureSubstatementActor(memberColl);
        // sub statement
        EnvironmentAdaptationSubStatement subStatement;
        try {
            subStatement = new EnvironmentAdaptationStatement.EnvironmentAdaptationSubStatement(subStatementActor, activity);
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiProcessorException("Unable to create group bread crumbs substatement!", e);
        }
        if(sessionMemberRefs && npcMemberRefs) {
            try {
                generator = new EnvironmentAdaptationGenerator(otherUsernames, npcRefs, session, domainSessionId, actorSlug, template, subStatement);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("unable to resolve remove session members + NPCs group breadcrumbs Environment Adaptation Generator!", e);
            }
        } else if(sessionMemberRefs && !npcMemberRefs) {
            try {
                generator = new EnvironmentAdaptationGenerator(otherUsernames, session, domainSessionId, actorSlug, template, subStatement);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("unable to resolve remove session members group breadcrumbs Environment Adaptation Generator!", e);
            }
        } else if(!sessionMemberRefs && npcMemberRefs) {
            try {
                generator = new EnvironmentAdaptationGenerator(npcRefs, session, domainSessionId, actorSlug, template, subStatement);
            } catch (LmsXapiGeneratorException e) {
                throw new LmsXapiProcessorException("unable to resolve remove NPCs group breadcrumbs Environment Adaptation Generator!", e);
            }
        } else {
            throw new LmsXapiProcessorException("Team Member references were unable to be split into session members vs NPCs!");
        }
    }
    
    /**
     * Configure xAPI Statement generator components for CreateBreadcrumbs Environment Adaptation and set generator
     * 
     * @param adapt - CreateBreadcrumbs environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.CreateBreadcrumbs adapt) throws LmsXapiProcessorException {
        // Location info used across generators
        EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locInfo = adapt.getLocationInfo();
        // handle one or many TeamMemberRef
        List<EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef> refColl = adapt.getTeamMemberRef();
        switch(refColl.size()) {
        case 0:
            throw new LmsXapiProcessorException("Unable to create xAPI statement from empty list of Team Member References!");
        case 1:
            handleSingleMemberRef(refColl.get(0), locInfo);
            break;
        default:
            List<MarkedTeamMember> teamMemberColl = new ArrayList<MarkedTeamMember>();
            for(EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef ref : refColl) {
                MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
                teamMemberColl.add(refTeamMember);
            }     
            handleManyMemberRef(teamMemberColl, locInfo);
            break;
        }
    }
    
    /**
     * Configure xAPI Statement generator components for RemoveBreadcrumbs Environment Adaptation and set generator
     * 
     * @param adapt - RemoveBreadcrumbs environment adaptation
     * 
     * @throws LmsXapiProcessorException when unable to configure generator
     */
    private void configureGenerator(EnvironmentAdaptation.RemoveBreadcrumbs adapt) throws LmsXapiProcessorException {
        // handle one or many TeamMemberRef
        List<EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef> refColl = adapt.getTeamMemberRef();
        switch(refColl.size()) {
        case 0:
            throw new LmsXapiProcessorException("Unable to create xAPI statement from empty list of Team Member References!");
        case 1:
            handleSingleMemberRef(refColl.get(0));
            break;
        default:
            List<MarkedTeamMember> teamMemberColl = new ArrayList<MarkedTeamMember>();
            for(EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef ref : refColl) {
                MarkedTeamMember refTeamMember = new MarkedTeamMember(ref.getValue(), ref.getEntityMarking());
                teamMemberColl.add(refTeamMember);
            }
            handleManyMemberRef(teamMemberColl);
            break;
        }
    }
    
    /**
     * Handles xAPI Statement generator configuration based on the Environment Adaptation type and adds corresponding
     * xAPI Statement to passed in collection of xAPI Statements
     * 
     * @param statements - collection of xAPI Statements
     * 
     * @throws LmsXapiProcessorException when unable to configure generator or issue generating xAPI Statement
     */
    private void processEnvironmentAdaptation(List<Statement> statements) throws LmsXapiProcessorException {
        // Generator determination
        Serializable kind = adaptation.getType();
        if(kind instanceof EnvironmentAdaptation.Overcast) {
            configureGenerator((EnvironmentAdaptation.Overcast) kind);
        } else if(kind instanceof EnvironmentAdaptation.Fog) {
            configureGenerator((EnvironmentAdaptation.Fog) kind);
        } else if(kind instanceof EnvironmentAdaptation.Rain) {
            configureGenerator((EnvironmentAdaptation.Rain) kind);
        } else if(kind instanceof EnvironmentAdaptation.Script) {
            configureGenerator((EnvironmentAdaptation.Script) kind);
        } else if(kind instanceof EnvironmentAdaptation.TimeOfDay) {
            configureGenerator((EnvironmentAdaptation.TimeOfDay) kind);
        } else if(kind instanceof EnvironmentAdaptation.CreateActors) {
            configureGenerator((EnvironmentAdaptation.CreateActors) kind);
        } else if(kind instanceof EnvironmentAdaptation.RemoveActors) {
            configureGenerator((EnvironmentAdaptation.RemoveActors) kind);
        } else if(kind instanceof EnvironmentAdaptation.Teleport) {
            configureGenerator((EnvironmentAdaptation.Teleport) kind);
        } else if(kind instanceof EnvironmentAdaptation.FatigueRecovery) {
            configureGenerator((EnvironmentAdaptation.FatigueRecovery) kind);
        } else if(kind instanceof EnvironmentAdaptation.Endurance) {
            configureGenerator((EnvironmentAdaptation.Endurance) kind);
        } else if(kind instanceof EnvironmentAdaptation.HighlightObjects) {
            configureGenerator((EnvironmentAdaptation.HighlightObjects) kind);
        } else if(kind instanceof EnvironmentAdaptation.RemoveHighlightOnObjects) {
            configureGenerator((EnvironmentAdaptation.RemoveHighlightOnObjects) kind);
        } else if(kind instanceof EnvironmentAdaptation.CreateBreadcrumbs) {
            configureGenerator((EnvironmentAdaptation.CreateBreadcrumbs) kind);
        } else if(kind instanceof EnvironmentAdaptation.RemoveBreadcrumbs) {
            configureGenerator((EnvironmentAdaptation.RemoveBreadcrumbs) kind);
        } else {
            throw new LmsXapiProcessorException("Unsupported Environment Adaptation! "+adaptation);
        }
        // Utilize environment control
        try {
            ((EnvironmentAdaptationGenerator) generator).addPcAppender(envControl);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("Unable to configure generator with environment control!", e);
        }
        // Generate statement
        try {
            generator.generateAndAdd(statements);
        } catch (LmsXapiGeneratorException e) {
            throw new LmsXapiProcessorException("Unable to generate Environment Adaptation statement!", e);
        }
    }

    @Override
    public void process(List<Statement> statements) throws LmsXapiProcessorException {
        processEnvironmentAdaptation(statements);
    }
}
