package mil.arl.gift.lms.impl.lrs.xapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.rusticisoftware.tincan.Statement;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.AbstractLearnerState;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiGeneratorException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.AbstractVoidStatementGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.BookmarkGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.DemonstratedPerformanceGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.PredictedAffectiveLsaGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.PredictedCognitiveLsaGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.VoidDemonstratedPerformanceGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.VoidPredictedAffectiveLsaGenerator;
import mil.arl.gift.lms.impl.lrs.xapi.generate.VoidPredictedCognitiveLsaGenerator;

/**
 * Utility class for working with Learner State within xAPI instrumentation
 * 
 * @author Yet Analytics
 *
 */
public class LearnerStateHelper {
    
    /**
     * Return the last item from the list
     * 
     * @param history - Collection of Performance States
     * 
     * @return Performance State at last index
     */
    public static AbstractPerformanceState getLast(List<AbstractPerformanceState> history) {
        if(CollectionUtils.isEmpty(history)) {
            throw new IllegalArgumentException("history can not be null or empty!");
        }
        return history.get(history.size() - 1);
    }
    
    /**
     * Filters Learner State Attribute Collection from Learner State attributes.
     * 
     * @param learnerState - Learner State to parse Learner State Attribute Collections from
     * 
     * @return possibly empty non null map of Learner State Attribute Collections
     */
    public static Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> onlyLearnerStateAttributeCollections(AbstractLearnerState learnerState){
        if(learnerState == null) {
            throw new IllegalArgumentException("Abstract Learner State can not be null!");
        }
        // Filtered map to return
        Map<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection> filtered = new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttributeCollection>();
        // Iterate over source and fill filtered
        for(Map.Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> kv : learnerState.getAttributes().entrySet()) {
            if(kv.getValue() instanceof LearnerStateAttributeCollection) {
                filtered.put(kv.getKey(), (LearnerStateAttributeCollection) kv.getValue());
            }
        }
        return filtered;
    }
    
    /**
     * Uses list of Abstract Performance States, denoting the steps taken into Performance State, 
     * to navigate into a possibly nested Learner State Attribute Collection in order to parse out
     * a Learner State Attribute which references the final element of the list.
     * 
     * @param state - Learner State Attribute Collection to search
     * @param history - ordered collection of performance states, collection is unaltered
     * 
     * @return Learner State Attribute found at path or null
     */
    public static LearnerStateAttribute searchInLearnerStateAttributeColl(LearnerStateAttributeCollection state, List<AbstractPerformanceState> history) {
        LearnerStateAttribute atPath = null;
        List<AbstractPerformanceState> copy = new ArrayList<AbstractPerformanceState>(history);
        // Check for path[0]
        String first = copy.get(0).getState().getName();
        if(first == null) {
            return atPath;
        }
        // Possibly recursive search
        for(Map.Entry<String, LearnerStateAttribute> kv : state.getAttributes().entrySet()) {
            if(kv.getKey().equals(first)) {
                LearnerStateAttribute firstLevelAttribute = kv.getValue();
                if(copy.size() == 1) {
                    atPath = firstLevelAttribute;
                    break;
                } else if(firstLevelAttribute instanceof LearnerStateAttributeCollection) {
                    LearnerStateAttributeCollection nestedFirstLevelAttribute = (LearnerStateAttributeCollection) firstLevelAttribute;
                    copy.remove(0);
                    atPath = searchInLearnerStateAttributeColl(nestedFirstLevelAttribute, copy);
                }
            }
        }
        return atPath;
    }
    
    /**
     * Comparison between two Abstract Performance States based on their state and observed assessment only.
     * 
     * @param a - Abstract Performance State to compare
     * @param b - Abstract Performance State to compare
     * 
     * @return false if contained state are not equal or observed assessment are not equal
     */
    public static boolean samePerformanceStateShallow(AbstractPerformanceState a, AbstractPerformanceState b) {
        if(a.getState() == null) {
            if(b.getState() != null) {
                return false;
            }
        } else if(!a.getState().equals(b.getState())) {
            return false;
        }
        if(a.isContainsObservedAssessmentCondition() != b.isContainsObservedAssessmentCondition()) {
            return false;
        }
        return true;
    }
    
    /**
     * Comparison between top level TaskPerformanceState properties 
     * 
     * @param stateA - TaskPerformanceState to compare
     * @param stateB - TaskPerformanceState to compare
     * 
     * @return false if TaskPerformanceStates have different stress / difficulty values
     */
    public static boolean compareTaskPerformanceStates(TaskPerformanceState stateA, TaskPerformanceState stateB) {
        if(!samePerformanceStateShallow(stateA, stateB)) {
            return false;
        }
        if(!Objects.equals(stateA.getStress(), stateB.getStress())) {
            return false;
        }
        if(!Objects.equals(stateA.getStressReason(), stateB.getStressReason())) {
            return false;
        }
        if(!Objects.equals(stateA.getDifficulty(), stateB.getDifficulty())) {
            return false;
        }
        if(!Objects.equals(stateA.getDifficultyReason(), stateB.getDifficultyReason())) {
            return false;
        }
        return true;
    }
    
    /**
     * Derive list of user name(s) for formative assessment statement from assessedTeamOrgEntities.
     * 
     * When assessedTeamOrgEntities is empty, the user name(s) with assigned
     * Team Roles (from session) are set as the user name(s).
     * 
     * When assessedTeamOrgEntities is not empty and none of the knowledgeSession memberships are being assessed,
     * the assessed Team Roles are set as the user name(s).
     * 
     * When assessedTeamOrgEntites is empty and none of the knowledgeSession memberships are assigned to a Team Role,
     * all session member user names are set as the user name(s).
     * 
     * Otherwise, the Host Session user name is the only user name.
     * 
     * @param performanceState - performance state used to create xAPI Statement Object, can't be null
     * @param session - knowledge session corresponding to the course session in which the xAPI statement is generated, can't be null
     * 
     * @return non-null List of 0 or more strings
     */
    public static List<String> deriveStatementActorNames(AbstractPerformanceState performanceState, AbstractKnowledgeSession session) {
        if(performanceState == null || performanceState.getState() == null || session == null) {
            throw new IllegalArgumentException("both performanceState and session must be non null!");
        }
        // Parse team roles from assessed team org entities
        PerformanceStateAttribute state = performanceState.getState();
        Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = state.getAssessedTeamOrgEntities();
        List<TeamMember<?>> teamMembers = new ArrayList<TeamMember<?>>();
        for(Map.Entry<String, AssessmentLevelEnum> kv : assessedTeamOrgEntities.entrySet()) {
            String teamRoleName = kv.getKey();
            MarkedTeamMember member = new MarkedTeamMember(teamRoleName, teamRoleName);
            teamMembers.add(member);
        }
        // Parse Session Memberships from session
        List<SessionMembership> memberships = TeamHelper.getSessionMemberships(TeamHelper.getSessionMembers(session));
        // select members via assessed team org entities or team roles assigned to session members
        if(teamMembers.isEmpty()) {
            teamMembers = TeamHelper.relevantTeamRoles(memberships);
        }
        // Derive agent names from teamMembers
        List<String> agentNames = new ArrayList<String>();
        for(TeamMember<?> tMember : teamMembers) {
            SessionMembership membership = TeamHelper.findMatchingSessionMembership(tMember, memberships);
            if(membership != null && membership.getUsername() != null) {
                agentNames.add(membership.getUsername());
            }
        }
        // Handle Performance State with assessedTeamOrgEntities not mapped to Session Memberships 
        if(CollectionUtils.isEmpty(agentNames) && CollectionUtils.isNotEmpty(assessedTeamOrgEntities)) {
            // attempt fallback to assessed team org entities team roles
            for(Map.Entry<String, AssessmentLevelEnum> kv : assessedTeamOrgEntities.entrySet()) {
                agentNames.add(kv.getKey());
            }
        } else if(CollectionUtils.isEmpty(agentNames) && CollectionUtils.isEmpty(assessedTeamOrgEntities) && CollectionUtils.isNotEmpty(memberships)) {
            // attempt fallback to usernames from session memberships
            for(SessionMembership membership : memberships) {
                if(membership != null && membership.getUsername() != null) {
                    agentNames.add(membership.getUsername());
                }
            }
        } else if(CollectionUtils.isEmpty(agentNames) && CollectionUtils.isEmpty(assessedTeamOrgEntities) && CollectionUtils.isEmpty(memberships)) {
            // attempt fallback to host session
            String hostSlug = session.getHostSessionMember().getUserSession().getUsername() != null ?
                    session.getHostSessionMember().getUserSession().getUsername() :
                        session.getHostSessionMember().getSessionMembership().getUsername();
            if(hostSlug != null) {
                agentNames.add(hostSlug);
            }
        }
        return agentNames;
    }
    
    // Statement Generation - Bookmark
    
    /**
     * Configure, generate and add bookmark xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param performance - PerformanceState with global bookmark
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateBookmarkStatement(List<Statement> statements, PerformanceState performance, 
            AbstractKnowledgeSession knowledgeSession, int domainSessionId)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        if(performance.getEvaluator() != null) {
            AbstractStatementGenerator gen = new BookmarkGenerator(knowledgeSession, performance, domainSessionId);
            gen.generateAndAdd(statements);
        }
    }
    
    // Statement Generation - Affective Lsa
    
    /**
     * Configure, generate and add predicted LSA xAPI Statement to statement accumulator
     *  
     * @param statements - xAPI Statement accumulator
     * @param lsa - Affective Learner State Attribute used as object of xAPI Statement
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateAffectiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, 
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen = 
                new PredictedAffectiveLsaGenerator(knowledgeSession, actorSlug, lsa, null, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add replacement predicted LSA xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Affective Learner State Attribute used as object of xAPI Statement
     * @param invalidStatementId - Id of xAPI Statement being replaced
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateAffectiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, UUID invalidStatementId,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen =
                new PredictedAffectiveLsaGenerator(knowledgeSession, actorSlug, lsa, null, invalidStatementId, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add predicted LSA with associated PSA xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Affective Learner State Attribute Collection used as object of xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateAffectiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, List<AbstractPerformanceState> history,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen =
                new PredictedAffectiveLsaGenerator(knowledgeSession, actorSlug, lsa, history, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add replacement predicted LSA with associated PSA xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Affective Learner State Attribute Collection used as object of xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA
     * @param invalidStatementId - Id of xAPI Statement being replaced
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateAffectiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, List<AbstractPerformanceState> history, 
            UUID invalidStatementId, AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen =
                new PredictedAffectiveLsaGenerator(knowledgeSession, actorSlug, lsa, history, invalidStatementId, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add voiding xAPI Statement to statement accumulator. Voiding xAPI Statement invalidates
     * predicted Affective LSA xAPI Statement.
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Affective Learner State Attribute used as object of invalidated xAPI Statement
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @return id of the xAPI Statement being invalidated
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static UUID voidAffectiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractVoidStatementGenerator gen = 
                new VoidPredictedAffectiveLsaGenerator(knowledgeSession, null, actorSlug, lsa, null, domainSessionId, domainId);
        return gen.generateAndReturnTargetId(statements);
    }
    
    /**
     * Configure, generate and add voiding xAPI Statement to statement accumulator. Voiding xAPI Statement invalidates
     * predicted Affective LSA with associated PSA xAPI Statement.
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Affective Learner State Attribute Collection used as object of invalidated xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @return id of the xAPI Statement being invalidated
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static UUID voidAffectiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, List<AbstractPerformanceState> history, 
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractPerformanceState state = getLast(history);
        String octSlug = state.getState().getEvaluator() != null ? state.getState().getEvaluator() : actorSlug;
        AbstractVoidStatementGenerator gen =
                new VoidPredictedAffectiveLsaGenerator(knowledgeSession, octSlug, actorSlug, lsa, history, domainSessionId, domainId);
        return gen.generateAndReturnTargetId(statements);
    }
    
    // Statement Generation - Cognitive Lsa
    
    /**
     * Configure, generate and add predicted LSA xAPI Statement to statement accumulator
     *  
     * @param statements - xAPI Statement accumulator
     * @param lsa - Cognitive Learner State Attribute used as object of xAPI Statement
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateCognitiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen = 
                new PredictedCognitiveLsaGenerator(knowledgeSession, actorSlug, lsa, null, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add replacement predicted LSA xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Cognitive Learner State Attribute used as object of xAPI Statement
     * @param invalidStatementId - Id of xAPI Statement being replaced
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateCognitiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, UUID invalidStatementId,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen =
                new PredictedCognitiveLsaGenerator(knowledgeSession, actorSlug, lsa, null, invalidStatementId, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add predicted LSA with associated PSA xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Cognitive Learner State Attribute Collection used as object of xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateCognitiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, List<AbstractPerformanceState> history,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen = 
                new PredictedCognitiveLsaGenerator(knowledgeSession, actorSlug, lsa, history, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add replacement predicted LSA with associated PSA xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Cognitive Learner State Attribute Collection used as object of xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA
     * @param invalidStatementId - Id of xAPI Statement being replaced
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateCognitiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, List<AbstractPerformanceState> history,
            UUID invalidStatementId, AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen =
                new PredictedCognitiveLsaGenerator(knowledgeSession, actorSlug, lsa, history, invalidStatementId, domainSessionId, domainId);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add voiding xAPI Statement to statement accumulator. Voiding xAPI Statement invalidates
     * predicted Cognitive LSA xAPI Statement.
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Cognitive Learner State Attribute used as object of invalidated xAPI Statement
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @return id of the xAPI Statement being invalidated
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static UUID voidCognitiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) 
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractVoidStatementGenerator gen =
                new VoidPredictedCognitiveLsaGenerator(knowledgeSession, null, actorSlug, lsa, null, domainSessionId, domainId);
        return gen.generateAndReturnTargetId(statements);
    }
    
    /**
     * Configure, generate and add voiding xAPI Statement to statement accumulator. Voiding xAPI Statement invalidates
     * predicted Cognitive LSA with associated PSA xAPI Statement.
     * 
     * @param statements - xAPI Statement accumulator
     * @param lsa - Cognitive Learner State Attribute Collection used as object of invalidated xAPI Statement
     * @param history - Ordered collection of performance states - last entry expected to be PSA associated with LSA
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * @param actorSlug - Learner the LSA is associated with
     * 
     * @return id of the xAPI Statement being invalidated
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static UUID voidCognitiveLsaStatement(List<Statement> statements, LearnerStateAttribute lsa, List<AbstractPerformanceState> history,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractPerformanceState state = getLast(history);
        String octSlug = state.getState().getEvaluator() != null ? state.getState().getEvaluator() : actorSlug;
        AbstractVoidStatementGenerator gen =
                new VoidPredictedAffectiveLsaGenerator(knowledgeSession, octSlug, actorSlug, lsa, history, domainSessionId, domainId);
        return gen.generateAndReturnTargetId(statements);
    }
    
    
    // Statement Generation - Formative Assessment
    
    /**
     * Configure, generate and add formative assessment xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param history - Ordered collection of performance states - last entry used as object of xAPI statement
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateFormativeAssessmentStatement(List<Statement> statements, List<AbstractPerformanceState> history,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen = 
                new DemonstratedPerformanceGenerator(history, knowledgeSession, domainSessionId, domainId, actorSlug);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add replacement formative assessment xAPI Statement to statement accumulator
     * 
     * @param statements - xAPI Statement accumulator
     * @param history - Ordered collection of performance states - last entry used as object of xAPI statement
     * @param invalidStatementId - Id of xAPI Statement being replaced
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static void generateFormativeAssessmentStatement(List<Statement> statements, List<AbstractPerformanceState> history, UUID invalidStatementId,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String actorSlug) throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractStatementGenerator gen =
                new DemonstratedPerformanceGenerator(history, invalidStatementId, knowledgeSession, domainSessionId, domainId, actorSlug);
        gen.generateAndAdd(statements);
    }
    
    /**
     * Configure, generate and add voiding xAPI Statement to statement accumulator. Voiding xAPI Statement invalidates
     * formative assessment xAPI Statement.
     * 
     * @param statements - xAPI Statement accumulator
     * @param history - Ordered collection of performance states - last entry used as object within invalidated xAPI statement
     * @param knowledgeSession - Knowledge Session associated with Learner State
     * @param domainSessionId - Identifier for domain session associated with Learner State
     * @param domainId - Identifier for course associated with learner state
     * 
     * @return id of the xAPI Statement being invalidated
     * 
     * @throws LmsXapiGeneratorException when unable to attach appender
     * @throws LmsXapiProfileException when unable to parse xAPI Profile
     */
    public static UUID voidFormativeAssessmentStatement(List<Statement> statements, List<AbstractPerformanceState> history,
            AbstractKnowledgeSession knowledgeSession, int domainSessionId, String domainId, String octSlug, String actorSlug)
                    throws LmsXapiGeneratorException, LmsXapiProfileException {
        AbstractPerformanceState state = getLast(history);
        String oct = state.getState().getEvaluator() != null ? state.getState().getEvaluator() : octSlug;
        AbstractVoidStatementGenerator gen = 
                new VoidDemonstratedPerformanceGenerator(history, knowledgeSession, oct, domainSessionId, domainId, actorSlug);
        return gen.generateAndReturnTargetId(statements);
    }
}
