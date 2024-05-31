/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import generated.dkf.RealTimeAssessmentRules;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VariableInfo;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr.ConditionEntityState;
import mil.arl.gift.net.api.message.Message;

/**
 * A condition that is used whether to assess if specified entities are pointing
 * their weapons at other specified entities.
 *
 * @author tflowers
 *
 */
public class MuzzleFlaggingCondition extends AbstractCondition {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(MuzzleFlaggingCondition.class);

    /** The message types that this condition is interested in. */
    private static final List<MessageTypeEnum> simulationInterests = Arrays.asList(MessageTypeEnum.ENTITY_STATE, MessageTypeEnum.VARIABLE_STATE_RESULT);

    /** A northward constant */
    private static final Vector2d NORTH = new Vector2d(1.0, 0.0);
    
    /** how long until a weapon state is stale and updated information is needed */
    private static final long STALE_WEAPON_STATE_MS = 1000;

    /**
     * The type of overall assessment scorers this condition can populate for an
     * AAR.
     */
    private static final Set<Class<?>> overallAssessmentTypes = new HashSet<>();
    static {
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.ViolationTime.class);
    }

    /** Information about the purpose of this condition */
    private static final ConditionDescription description = new FileDescription(
            Paths.get("docs", "conditions", "MuzzleFlagging.GIFT Domain condition description.html").toFile(),
            "Muzzle Flagging");

    /**
     * A collection that is used to pass the violator's target entities to
     * {@link AbstractCondition}'s
     * {@link AbstractCondition#addViolatorWithDependencies(TeamMember, EntityIdentifier, Collection)}
     * to minimize allocations.
     */
    private final Collection<EntityIdentifier> VIOLATOR_TARGET_COLLECTION = new ArrayList<>();

    /**
     * The maximum distance between two entities at which this condition should
     * still be assessed. The maximum distance is stored as a squared value to
     * facilitate better performance. For instance if the desired maximum
     * distance is 5, this variable's value will be 25.
     */
    private final double maxDistanceSquared;

    /**
     * The maximum angle between an entity's orientation and relative location
     * vector of a target for which the assessed entity is considered to be
     * flagging the target.
     */
    private final double maxThetaThreshold;
    
    /** contains the entity markings of the team members that are being assessed in this condition */
    private Set<String> assessedMarkings = new HashSet<>();
    
    /**
     * mapping of assessed team member to the list of targets the assessed member is currently orienting
     * their actor towards.  This is the first part of the flagging assessment.  The next part is to use
     * weapon status information like weapon safety value.  The list of targets can be empty and an assessed
     * team member might not be in the map.
     */
    private Map<TeamMember<?>, Set<TeamMember<?>>> pendingViolationsMap = new HashMap<>();
    
    /** flag used to indicate if the weapon safety value should be used as part of the assessment */
    private boolean useWeaponSafety = true;

    /**
     * Empty constructor required for authoring logic to work.
     */
    public MuzzleFlaggingCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("MuzzleFlaggingCondition()");
        }

        this.maxDistanceSquared = 0.0f;
        this.maxThetaThreshold = 0.0f;
    }

    /**
     * Constructs a {@link MuzzleFlaggingCondition} that is configured with a
     * {@link generated.dkf.MuzzleFlaggingCondition}.
     *
     * @param input The {@link generated.dkf.MuzzleFlaggingCondition} used to
     *        configure the assessment performed by the
     *        {@link MuzzleFlaggingCondition}.
     */
    public MuzzleFlaggingCondition(generated.dkf.MuzzleFlaggingCondition input) {
        super(AssessmentLevelEnum.AT_EXPECTATION);
        if (logger.isInfoEnabled()) {
            logger.info("MuzzleFlaggingCondition(" + input + ")");
        }

        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }

        final RealTimeAssessmentRules rtaRules = input.getRealTimeAssessmentRules();
        if (rtaRules != null) {
            addRealTimeAssessmentRules(rtaRules);
        }

        setTeamMembersBeingAssessed(input.getTeamMemberRefs());

        final BigDecimal maxDistance = input.getMaxDistance();
        maxDistanceSquared = maxDistance != null ? Math.pow(maxDistance.doubleValue(), 2) : 0;

        maxThetaThreshold = input.getMaxAngle().doubleValue();
        
        Boolean shouldUseWeaponSafety = input.isUseWeaponSafety();
        if(shouldUseWeaponSafety != null){
            useWeaponSafety = shouldUseWeaponSafety;
        }
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleTrainingAppGameState(" + message + ")");
        }
        
        if(blackboard == null){
            setBlackboard(message);
        }

        final MessageTypeEnum msgType = message.getMessageType();

        if (MessageTypeEnum.ENTITY_STATE.equals(msgType)) {
            EntityState es = (EntityState) message.getPayload();

            /* Try to get the TeamMember using the entity id. */
            final TeamMember<?> assessedMember = isConditionAssessedTeamMember(es.getEntityID());

            /* If the entity is not being assessed by this condition, there will
             * be no change in the assessment. */
            if (assessedMember == null) {
                return false;
            }
            
            // gather entity markings of assessed members
            if(assessedMember.getIdentifier() instanceof String){
                assessedMarkings.add((String) assessedMember.getIdentifier());
            }

            /* Get the spatial information for the assessed entity and cache
             * it. */
            final AssessedEntityMetadata assessedMetadata = entityMetadataLookup
                    .computeIfAbsent(assessedMember.getEntityIdentifier(), key -> new AssessedEntityMetadata());
            assessedMetadata.updateMetadata(es);
            final Point3d assessedGcc = assessedMetadata.getLastGccLocation();
            final GDC assessedGdc = assessedMetadata.getLastGdcLocation();

            /* Check this entity against all other assessed entities. */
            boolean violatedOrientation = false, violatedFlagging = false;
            for (String targetMemberName : getTeamOrgRefs().getTeamMemberRef()) {

                /* Don't assess the entity against itself. */
                if (targetMemberName.equals(assessedMember.getName())) {
                    continue;
                }

                /* Fetch the target location */
                final TeamMember<?> targetMember = getTeamMember(targetMemberName);

                /* If the metadata for the target is not yet known, skip the
                 * assessment for now. */
                final AssessedEntityMetadata targetMetadata;
                synchronized (entityMetadataLookup) {
                    targetMetadata = entityMetadataLookup.get(targetMember.getEntityIdentifier());
                }

                if (targetMetadata == null) {
                    continue;
                }

                final Point3d targetGcc = targetMetadata.getLastGccLocation();
                final GDC targetGdc = targetMetadata.getLastGdcLocation();

                /* If the distance is greater than the max distance threshold,
                 * flagging is impossible. */
                if (maxDistanceSquared != 0){
                    final double distanceSquared = assessedGcc.distanceSquared(targetGcc);
                    if(distanceSquared > maxDistanceSquared) {
                        continue;
                    }
                }

                /* Test the angle to determine whether or not this entity is
                 * violating the muzzle flagging condition. */
                final double assessedHeading = CoordinateUtil.getInstance().getHeading(es.getOrientation(), assessedGdc);
                final double theta = getTheta(assessedGdc, targetGdc, assessedHeading);

                /* If the theta value is under the threshold, update the
                 * assessment to below expectation. It is safe to return now
                 * since we know the entity that is being assessed (the one to
                 * which the incoming entity state belongs), is pointing its
                 * weapon towards at least one other assessed entity. */
                if (theta <= maxThetaThreshold) {
                    
                    violatedOrientation = true;
                    
                    if(useWeaponSafety){
                        if(checkWeaponStateForViolation(assessedMember, targetMember)){
                            violatedFlagging |= handleViolation(assessedMember, targetMember);
                        }
                    }else{
                        violatedFlagging |= handleViolation(assessedMember, targetMember);
                    }
                }
            }// end for
            
            if(violatedOrientation){
                // the orientation check failed on at least one target, return whether
                // the additional checks (e.g. weapon safety) also resulted in flagging violation
                return violatedFlagging;
            }else{
                /* The assessed entity was not flagging any targets */
                return handleSuccess(assessedMember);
            }
        }else if(message.getMessageType() == MessageTypeEnum.VARIABLE_STATE_RESULT){
            
            // if weapon safety isn't important to this flagging instance than don't process
            // the variable state information
            if(!useWeaponSafety){
                return false;
            }
            
            VariablesStateResult result = (VariablesStateResult)message.getPayload();    
            Map<String, VariableState> weaponStateMap = result.getVariablesState().getVariableMapForType(VARIABLE_TYPE.WEAPON_STATE);

            // check each assessed team member's weapon state if that team member is currently
            // oriented toward one or more target team members
            // 
            boolean stateChanged = false;
            for(TeamMember<?> assessedMember : pendingViolationsMap.keySet()){
                
                Set<TeamMember<?>> targets = pendingViolationsMap.get(assessedMember);
                if(targets == null || targets.isEmpty()){
                    continue;
                }
                
                WeaponState wState = (WeaponState) weaponStateMap.get(assessedMember.getIdentifier());
                if(wState == null){
                    // the weapon state update didn't contain this assessed member
                    continue;
                }
                
                if(checkWeaponStateForViolation(wState)){
                    // the assessed member has now been determined to:
                    // 1. orienting toward the target
                    // 2. equipped weapon (e.g. in hands)
                    // 3. weapon safety is off
                    // ... so update the violation information for this team member
                    for(TeamMember<?> targetMember : targets){
                        stateChanged |= handleViolation(assessedMember, targetMember);
                    }
                }                
                
                // reset to populate again in the future for this assessed member
                targets.clear();
            }
            
            return stateChanged;
        }

        return false;
    }
    
    /**
     * Handles the case when the provided assessed team member has oriented their actor toward
     * the provided target member.  If weapon state information is available and not stale on the
     * assessed member it will be used to determine if a full flagging violation has occurred.  If
     * weapon state information is not available it will be requested.
     * @param assessedMember
     * @param targetMember
     * @return true under the following situations:
     * 1. the blackboard is still null (shouldn't be the case)
     * 2. the up to date weapon state shows the assessed member has a weapon enabled and the weapon safety is off
     * 3. the training application (Gateway) is not available to be queried, probably because running a log playback
     * Other wise false will be returned:
     * 1. the target member is already a known member being oriented toward and a request has been made for weapon status
     *    on the assessed member.
     * 2. a request has been made on the assessed member
     */
    private boolean checkWeaponStateForViolation(TeamMember<?> assessedMember, TeamMember<?> targetMember){
        
        if(blackboard == null){
            return true;
        }else if(!blackboard.isTrainingAppAvailable()){
            return true;
        }
        
        ConditionEntityState state = blackboard.getConditionEntityState(assessedMember.getName());
        if(System.currentTimeMillis() - state.getWeaponStateUpdatedEpoch() > STALE_WEAPON_STATE_MS){
            // need to query for the last weapon state and wait for response
            
            Set<TeamMember<?>> targets = pendingViolationsMap.get(assessedMember);
            if(targets == null){
                targets = new HashSet<>();
                pendingViolationsMap.put(assessedMember, targets);
            }else if(targets.contains(targetMember)){
                // the target is already in the set of members being flagged by the assessed member and
                // this condition is waiting for a response to the weapon state request message
                return false;
            }            
            
            targets.add(targetMember);
            if(targets.size() > 1){
                // a request for the assessed member's weapon status was already sent when the previous target member
                // was added
                return false;
            }
            
            //TODO: optimize this logic by 
            // 1. using timer task, using a gathering list of entities that need weapon state in a central location (e.g. Scenario.java?)
            // 2. only requesting violating members, not all members in this condition
            //
            // check if the members being assessed in this condition are already awaiting weapon state result from
            // a previous request
            if(blackboard.addPendingWeaponStateResultEntities(assessedMarkings)){
                // there are one or more members that don't have a pending weapon state request
                VariablesStateRequest request = new VariablesStateRequest(conditionInstanceID.toString());
                VariableInfo varInfo = new VariableInfo(assessedMarkings);
                request.setTypeVariable(VARIABLE_TYPE.WEAPON_STATE, varInfo);
                conditionActionInterface.trainingApplicationRequest(request);
            }
            
            return false;
            
        }else{
            // use the last weapon state to check for violation
            // Violation after orienting your actor toward another entity happens if:
            // 1. the weapon is equipped (i.e. in your hands)
            // 2. the safety is off            
            return checkWeaponStateForViolation(state.getWeaponState());
        }
    }
    
    /**
     * Checks the provided weapon state to see if the weapon is equipped and the safety is off.
     * @param wState the weapon state info to check.  Can be null.
     * @return true if the weapon is equipped and the safety is off, false otherwise. False if
     * the weapon state is null.
     */
    private boolean checkWeaponStateForViolation(WeaponState wState){
        
        if(wState == null){
            return false;
        }
        boolean hasWeapon = wState.getHasWeapon() == null || wState.getHasWeapon();
        boolean isSafe = wState.getWeaponSafetyStatus() == null || wState.getWeaponSafetyStatus();
        return hasWeapon && !isSafe;
    }

    /**
     * A method that handles the case when a provided team member flags another
     * team member.
     *
     * @param violatingMember The member who violated the condition by flagging
     *        another team member. Can't be null.
     * @param targetMember The member the violator is aiming at. Can't be null.
     * @return True if this violation resulted in a change in the assessment
     *         value, otherwise false.
     */
    private boolean handleViolation(TeamMember<?> violatingMember, TeamMember<?> targetMember) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleViolation(" + violatingMember + ")");
        }

        final EntityIdentifier memberEntityId = violatingMember.getEntityIdentifier();
        final AssessmentLevelEnum prevAssessment = getAssessment();
        final int prevViolatorCount = getViolatorSize();

        VIOLATOR_TARGET_COLLECTION.add(targetMember.getEntityIdentifier());
        addViolatorWithDependencies(violatingMember, memberEntityId, VIOLATOR_TARGET_COLLECTION);
        VIOLATOR_TARGET_COLLECTION.clear();

        final boolean violatorListChanged = prevViolatorCount != getViolatorSize();
        updateExplanation();

        final AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();

        /* If a scoring event is ongoing, reassess using the custom real-time
         * assessment in the event that the assessment value is dependent on
         * violation time. */
        if (isScoringEventActive(violatingMember)) {

            /* If the custom real-time assessment has not resulted in an
             * evaluation, no further action is required. */
            if (authoredLevel == null) {
                return violatorListChanged;
            }

            /* If the custom assessment is unchanged, no further action is
             * needed. */
            if (getAssessment() == authoredLevel) {
                return violatorListChanged;
            }

            /* Update the assessment value. */
            updateAssessment(authoredLevel);
            return true;
        }

        scoringEventStarted(violatingMember);
        if (authoredLevel != null) {
            final boolean assessmentChanged = authoredLevel != prevAssessment;
            if (assessmentChanged) {
                updateAssessment(authoredLevel);
            }

            return violatorListChanged || assessmentChanged;
        }

        /* If the previous assessment is already below expectation, no
         * assessment change is possible. */
        if (prevAssessment == AssessmentLevelEnum.BELOW_EXPECTATION) {
            return violatorListChanged;
        }

        updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
        return true;
    }

    /**
     * A method that handles the case when a provided team member is not
     * flagging any other team members.
     *
     * @param teamMemberNotViolating The {@link TeamMember<?>} of the member who is
     *        not flagging any other team member. Can't be null.
     * @return True if this results in a change in the assessment
     *         value.
     */
    private boolean handleSuccess(TeamMember<?> teamMemberNotViolating) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleSuccess(" + teamMemberNotViolating + ")");
        }

        final AssessmentLevelEnum prevAssessment = getAssessment();
        final int prevViolatorCount = getViolatorSize();

        removeViolator(teamMemberNotViolating.getEntityIdentifier());
        final boolean violatorListChanged = prevViolatorCount != getViolatorSize();
        updateExplanation();

        scoringEventEnded(teamMemberNotViolating);
        if (getViolatorSize() == 0) {
            scoringEventEnded();
        }

        /* If the authored custom assessment rules have produced a value, use
         * that */
        final AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if (authoredLevel != null) {
            final boolean assessmentChanged = authoredLevel != prevAssessment;
            if (assessmentChanged) {
                updateAssessment(authoredLevel);
            }

            return violatorListChanged || assessmentChanged;
        }

        /* If the previous assessment is already at expectation, no further
         * action is needed. */
        if (prevAssessment == AssessmentLevelEnum.AT_EXPECTATION) {
            return violatorListChanged;
        }

        /* If there are still violators, the assessment level can't change
         * yet. */
        if (getViolatorSize() != 0) {
            return violatorListChanged;
        }

        updateAssessment(AssessmentLevelEnum.AT_EXPECTATION);
        return true;
    }

    /**
     * Updates the value of {@link AbstractCondition#assessmentExplanation}
     * based on the current value of {@link #getViolatorTeamOrgEntries()}.
     */
    private void updateExplanation() {
        final Set<String> violatorTeamOrgEntries = getViolatorTeamOrgEntries();
        if (CollectionUtils.isNotEmpty(violatorTeamOrgEntries)) {
            final StringBuilder sb = new StringBuilder("{ ");
            StringUtils.join(", ", violatorTeamOrgEntries, StringUtils.DEFAULT, sb);
            sb.append(" } ").append(violatorTeamOrgEntries.size() == 1 ? "is" : "are")
                    .append(" flagging other assessed team members.");
            assessmentExplanation = sb.toString();
        } else {
            assessmentExplanation = "";
        }
    }

    @Override
    public ConditionDescription getDescription() {
        return description;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    @Override
    public boolean canComplete() {
        return false;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }

    @Override
    public String toString() {
        return new StringBuilder("[MuzzleFlaggingCondition: ")
                .append("entityMetadataLookup = ").append(entityMetadataLookup)
                .append(", maxDistanceSquared = ").append(maxDistanceSquared)
                .append(", maxThetaThreshold = ").append(maxThetaThreshold)                
                .append(", ").append(super.toString())
                .append(']').toString();
    }

    /**
     * <h1>Visualization</h1>
     *
     * <code><pre>
     *                            target entity
     *                           /
     * =========================O================
     *               \          |
     *                \         |
     *  orientation -> \        |
     *                  \       |
     *                   \      |
     *                    \     |
     *                     \  _ |
     *                      \/ \|
     *                       \  |
     *               theta -> \ |
     *                         \|
     *                          O - source entity
     * </pre></code>
     *
     * @param sourceGdc The location of the entity being assessed. Can't be
     *        null.
     * @param targetGdc The location of the entity which may or may not be
     *        flagged by the source entity.
     * @param heading The direction the ray cast will travel.
     * @return The angle between the orientation of the sourceEntity and the
     *         vector between the source entity and the target entity.
     */
    protected static double getTheta(GDC sourceGdc, GDC targetGdc, double heading) {

        /* Determines the location of the target relative to the source */
        final double sourceLat = sourceGdc.getLatitude();
        final double sourceLong = sourceGdc.getLongitude();
        final double targetLat = targetGdc.getLatitude();
        final double targetLong = targetGdc.getLongitude();
        final Vector2d displacement = new Vector2d(targetLat - sourceLat, targetLong - sourceLong);

        /* Determines the angle of the target's relative location to the
         * direction of north */
        final double displacementVsNorthAngle = Math.toDegrees(displacement.angle(NORTH));

        /* Calculates the angle between the direction the assessed entity is
         * facing and the direction of the target entity. */
        double theta;
        if (heading >= 0 && displacement.getY() >= 0) {
            theta = Math.abs(displacementVsNorthAngle - heading);
        } else if (heading < 0 && displacement.getY() < 0) {
            theta = Math.abs(displacementVsNorthAngle + heading);
        } else {
            theta = displacementVsNorthAngle + Math.abs(heading);
        }

        return theta > 180 ? 360 - theta : theta;
    }

    @Override
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators) {

        if(removedViolators != null && !removedViolators.isEmpty() && getViolatorSize() == 0){
            //no more violators

            for(TeamMember<?> teamMember : removedViolators) {
                handleSuccess(teamMember);
                if(conditionActionInterface != null){
                    conditionActionInterface.conditionAssessmentCreated(this);
                }
            }
        }
    }
}
