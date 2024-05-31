/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assesses whether one or more team members are properly avoiding firing
 * at specified objects.  Weapon cone is used as a parameter for the assessment algorithm.
 * @author mhoffman
 *
 */
public class NegligentDischargeCondition extends AbstractCondition {
    
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(NegligentDischargeCondition.class);

    /** The message types that this condition is interested in. */
    private static final List<MessageTypeEnum> simulationInterests = Arrays.asList(MessageTypeEnum.ENTITY_STATE,
            MessageTypeEnum.WEAPON_FIRE);
    
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
            Paths.get("docs", "conditions", "NegligentDischarge.GIFT Domain condition description.html").toFile(),
            "Negligent Discharge");
    
    /**
     * The maximum distance to consider a non-target to be within
     * range of being assessed for engagement.  i.e. ignore objects that
     * are far away.
     */
    private int weaponConeDistance = 300;
    
    /**
     * The maximum angle between an assessed members orientation vector
     * and an engage non-target to be considered in the weapon cone
     * of the assessed member.
     * Using 30 degrees as a default weapon cone.
     */
    private double weaponConeHalfAngle = 30 / 2.0;
    
    /**
     * Names of team org member names to NOT engage.
     * Can be empty as non-targets are an optional input to this condition.
     */
    private Set<String> memberTargetsToNotEngage = new HashSet<>();
    
    /**
     * mapping of assessed team member to the current targets (team member or point name) to NOT engage in view.  The current collection
     * can be null or empty.
     */
    private Map<TeamMember<?>, Map<String, ViewedObjectWrapper>> assessedMemberToNonEngageTargetsInWeaponCone = new HashMap<>();
    
    /** 
     * mapping of unique team member name to the information being tracked for assessment for this condition
     * Populated once an Entity State message is received for a non-target.
     */
    private Map<String, AssessedEntityMetadata> memberTargetsToNotEngageAssessmentMap = new HashMap<>();
    
    /**
     * mapping of place of interest name to the Point object which contains the GCC/GDC coordinate information.
     * Used to check if the assessed member is looking at these points that should NOT be fired upon.
     * Can be empty.
     */
    private Map<String, Point> nonTargetPointRefs = new HashMap<>();
    
    /**
     * the configuration for this condition instance
     */
    private generated.dkf.NegligentDischargeCondition input;
        
    /**
     * Empty constructor required for authoring logic to work.
     */
    public NegligentDischargeCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("NegligentDischargeCondition()");
        }
        
    }
    
    /**
     * Constructs a {@link NegligentDischargeCondition} that is configured with a
     * {@link generated.dkf.NegligentDischargeCondition}.
     *
     * @param input The {@link generated.dkf.NegligentDischargeCondition} used to
     *        configure the assessment performed by the
     *        {@link NegligentDischargeCondition}.
     */
    public NegligentDischargeCondition(generated.dkf.NegligentDischargeCondition input) {
        super(AssessmentLevelEnum.AT_EXPECTATION);
        if (logger.isInfoEnabled()) {
            logger.info("NegligentDischargeCondition(" + input + ")");
        }
        
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }
        
        this.input = input;
        
        final RealTimeAssessmentRules rtaRules = input.getRealTimeAssessmentRules();
        if (rtaRules != null) {
            addRealTimeAssessmentRules(rtaRules);
        }
        
        setTeamMembersBeingAssessed(input.getTeamMemberRefs());
        
        // weapon cone distance - optional parameter
        if(input.getWeaponConeMaxDistance() != null){
            weaponConeDistance = input.getWeaponConeMaxDistance().intValue();
        }
        
        // weapon cone angle
        weaponConeHalfAngle = input.getWeaponConeAngle() / 2;        
           
        for(Serializable nonTargetObj : input.getTargetsToAvoid().getTeamMemberRefOrPointRef()){
            
            if(nonTargetObj instanceof String){
                // this is a team member name reference
                
                String nonTargetMemberName = (String)nonTargetObj;                    
                memberTargetsToNotEngage.add(nonTargetMemberName);
            }
        }

    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        if (logger.isDebugEnabled()) {
            logger.debug("handleTrainingAppGameState(" + message + ")");
        }

        final MessageTypeEnum msgType = message.getMessageType();

        if (MessageTypeEnum.ENTITY_STATE.equals(msgType)) {
            EntityState es = (EntityState) message.getPayload();
            
            TeamMember<?> teamMember = getTeamMemberFromTeamOrg(es);
            if(teamMember == null){
                // this entity is not in the team org
                return false;
            }

            // capture last location of non-target entities for next comparison to assigned members
            if(memberTargetsToNotEngage.contains(teamMember.getName())){
                // found a non-target
                
                final AssessedEntityMetadata assessedMetadata = memberTargetsToNotEngageAssessmentMap
                        .computeIfAbsent(teamMember.getName(), key -> new AssessedEntityMetadata());
                assessedMetadata.updateMetadata(es);
                
                // for now don't bother checking all of the assessed members, wait for the assessed
                // member's next entity state instead.  If this approach results in inadequate assessment
                // fidelity, than remove the return statement and use this non-target's location update for assessment now.
                return false;
            }else if(isConditionAssessedTeamMember(teamMember)){
                // found a member being assessed
                
                // save assessed member's location to use for muzzle cone calculations when firing the weapon
                final AssessedEntityMetadata assessedMetadata = entityMetadataLookup
                        .computeIfAbsent(teamMember.getEntityIdentifier(), key -> new AssessedEntityMetadata());
                assessedMetadata.updateMetadata(es);
                
            }else if(!isConditionAssessedTeamMember(teamMember)){
                // not a team member being assessed by this condition
                return false;
            }
            
            final GDC memberLocation = CoordinateUtil.getInstance().convertFromGCCToGDC(es.getLocation());
            final double assessedHeading = CoordinateUtil.getInstance().getHeading(es.getOrientation(), memberLocation);
            
            //
            // Determine if any non-engage targets on in the weapon cone
            //
            Map<String, ViewedObjectWrapper> objectsToAvoid = assessedMemberToNonEngageTargetsInWeaponCone.get(teamMember);
            if(objectsToAvoid == null){
                objectsToAvoid = new HashMap<>();
                assessedMemberToNonEngageTargetsInWeaponCone.put(teamMember, objectsToAvoid);
            }
            
            // check non-engage team members
            for(String nonTargetMemberName : memberTargetsToNotEngageAssessmentMap.keySet()){
                
                TeamMember<?> nonTargetMember = getTeamMember(nonTargetMemberName);
                AssessedEntityMetadata nonTargetMemberMetadata = memberTargetsToNotEngageAssessmentMap.get(nonTargetMemberName);
                
                // if non-target is dead, it is no longer a target that should be considered
                if(nonTargetMemberMetadata.getDamage() == DamageEnum.DESTROYED){
                    
                    ViewedObjectWrapper targetWrapper = objectsToAvoid.get(nonTargetMember.getName());
                    if(targetWrapper != null){
                        // the object to avoid is now dead and this assessed member never performed negligent discharge
                        // therefore stop tracking this object (until it gets revived)
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+" is not considering '"+nonTargetMember.getName()+"' for negligent discharge because that entity is not healthy.");
                        }
                        objectsToAvoid.remove(nonTargetMember.getName());
                    }
                    continue;
                }
                
                // check that the non-engage target is within the weapon cone distance to be considered for this assessment
                double distanceBetweenLocations = es.getLocation().distance(nonTargetMemberMetadata.getLastGccLocation());
                if(distanceBetweenLocations > weaponConeDistance){
                    
                    ViewedObjectWrapper targetWrapper = objectsToAvoid.get(nonTargetMember.getName());
                    if(targetWrapper != null){
                        // the object to avoid is now out of the weapon cone based on distance and this assessed member never performed negligent discharge
                        // therefore stop tracking this object (until it gets revived)
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+" has enough distance from '"+nonTargetMember.getName()+"' to remove it from the list.");
                        }
                        objectsToAvoid.remove(nonTargetMember.getName());
                    }
                    continue;
                }
                
                /* Test the angle to determine whether or not this non-target team member is
                 * in the assessed team member's weapon cone. */
                final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, nonTargetMemberMetadata.getLastGdcLocation(), assessedHeading);
                
                /* If the theta value is
                 * - under the threshold the non-target team member is in the weapon cone
                 * - over the threshold the non-target team member is not in the weapon cone
                 */
                if (theta <= weaponConeHalfAngle) {
                    // make sure non-target is being tracked as in weapon cone
                    
                    if(!objectsToAvoid.containsKey(nonTargetMember.getName())){
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+ " has non-target team member '"+nonTargetMember.getName()+"' in weapon cone");
                        }
                        ViewedObjectWrapper nonTargetWrapper = new ViewedObjectWrapper(nonTargetMember);
                        objectsToAvoid.put(nonTargetMember.getName(), nonTargetWrapper);
                    }
                }else{
                    ViewedObjectWrapper targetWrapper = objectsToAvoid.get(nonTargetMember.getName());
                    if(targetWrapper != null){
                        // the object to avoid is now not in the weapon cone and this assessed member never performed negligent discharge
                        // therefore stop tracking this object
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+" is no longer looking at '"+nonTargetMember.getName()+"'.");
                        }
                        objectsToAvoid.remove(nonTargetMember.getName());
                    }

                }
            }
            
            // check non-engage points 
            for(String pointName : nonTargetPointRefs.keySet()){
                
                Point point = nonTargetPointRefs.get(pointName);
                GDC gdcPt = point.toGDC();
                
                // check that the non-engage target point is within the weapon cone distance to be considered for this assessment
                double distanceBetweenLocations = es.getLocation().distance(point);
                if(distanceBetweenLocations > weaponConeDistance){
                    
                    ViewedObjectWrapper targetWrapper = objectsToAvoid.get(pointName);
                    if(targetWrapper != null){
                        // the object to avoid is now out of the weapon cone and this assessed member never performed negligent discharge
                        // therefore stop tracking this object (until it gets revived)
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+" has enough distance from '"+pointName+"' to remove it from the list.");
                        }
                        objectsToAvoid.remove(pointName);
                    }
                    continue;
                }
                
                /* Test the angle to determine whether or not this point is
                 * within the weapon cone of this assessed team member. */
                final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, gdcPt, assessedHeading);
                
                /* If the theta value is
                 * - under the threshold the target point is in the weapon cone
                 * - over the threshold the target point is not in the weapon cone
                 */
                if (theta <= weaponConeHalfAngle) {
                    // make sure target is being tracked as in weapon cone
                    
                    if(!objectsToAvoid.containsKey(pointName)){
                        
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+" has non-target point '"+pointName+"' in weapon cone");
                        }
                        ViewedObjectWrapper targetWrapper = new ViewedObjectWrapper(pointName);
                        objectsToAvoid.put(pointName, targetWrapper);
                    }
                }else{
                    ViewedObjectWrapper targetWrapper = objectsToAvoid.get(pointName);
                    if(targetWrapper != null){
                        // the object to avoid is now not in the weapon cone and this assessed member never performed negligent discharge
                        // therefore stop tracking this object (until it gets revived)
                        if(logger.isDebugEnabled()){
                            logger.debug(teamMember.getName()+" is no longer aiming at '"+pointName+"'.");
                        }
                        objectsToAvoid.remove(pointName);
                    }
                }
                
            }
            
        } else if(MessageTypeEnum.WEAPON_FIRE.equals(msgType)){
            
            WeaponFire weaponFire = (WeaponFire)message.getPayload();
            
            EntityIdentifier firingEntityId = weaponFire.getFiringEntityID();
            TeamMember<?> assessedTeamMember = isConditionAssessedTeamMember(firingEntityId);
            if(assessedTeamMember != null){
                // an assessed team member is firing 
                
                // Assessment logic
                // if non-engage targets in weapon cone view > 0 => Negligent Discharge (VIOLATION)
                // else - no assessment
                
                AssessedEntityMetadata firingEntityMetadata = entityMetadataLookup.get(firingEntityId);
                if(firingEntityMetadata == null){
                    // an entity state hasn't been received for the assessed member
                    return false;
                }
                
                final double firingEntityHeading = CoordinateUtil.getInstance().getHeading(firingEntityMetadata.getLastOrientation(), 
                        firingEntityMetadata.getLastGdcLocation());
                
                // [CHECK] Negligent Discharge Violation -
                // Determine if non-engage targets are in the weapon cone of the assessed member AND not beyond the weapon cone distance.
                Map<String, ViewedObjectWrapper> nonEngageTargetsInWeaponCone = assessedMemberToNonEngageTargetsInWeaponCone.get(assessedTeamMember);
                if(nonEngageTargetsInWeaponCone != null){
                    boolean violationDetected = false;  // Note: must mark all objects that were discharged upon for a complete assessment explanation 
                    for(ViewedObjectWrapper nonEngageTarget : nonEngageTargetsInWeaponCone.values()){
                        
                        Serializable teamMemberOrPointName = nonEngageTarget.getTeamMemberOrPointName();
                        if(teamMemberOrPointName instanceof TeamMember<?>){
                            TeamMember<?> nonEngageTargetMember = (TeamMember<?>)teamMemberOrPointName;
                            AssessedEntityMetadata nonTargetMemberMetadata = memberTargetsToNotEngageAssessmentMap.get(nonEngageTargetMember.getName());
                            
                            // if target is dead, it is no longer a target that should be considered
                            if(nonTargetMemberMetadata == null || nonTargetMemberMetadata.getDamage() == DamageEnum.DESTROYED){
                                continue;
                            }
    
                            // check that the non-engage target is within the weapon cone distance to be considered for this assessment
                            // Note: might not be necessary since the non-target should be in assessedMemberToNonEngageTargetsInWeaponCone unless within the weapon cone distance.
                            double distanceBetweenEntities = firingEntityMetadata.getLastGccLocation().distance(nonTargetMemberMetadata.getLastGccLocation());
                            if(distanceBetweenEntities > weaponConeDistance){
                                continue;
                            }
                            
                            /* Test the angle to determine whether or not the assessed entity is
                             * aiming the weapon cone at this non target team member. */
                            final double theta = MuzzleFlaggingCondition.getTheta(firingEntityMetadata.getLastGdcLocation(), 
                                    nonTargetMemberMetadata.getLastGdcLocation(), firingEntityHeading);
                            
                            /* If the theta value is
                             * - under the threshold the non-target team member is in the weapon cone
                             * - over the threshold the non-target team member is not in the weapon cone
                             */
                            if (theta > weaponConeHalfAngle) {
                                // not a violation
                                continue;
                            }else{
                                // Negligent Discharge Violation
                                if(logger.isDebugEnabled()){
                                    logger.debug(assessedTeamMember.getName()+" performed Negligent Discharge violation on "+nonEngageTargetMember.getName()+" team org. member");
                                }
                                nonEngageTarget.setViolatedNegligentDischarge(true);
                                violationDetected = true;
                            }
                        }else if(teamMemberOrPointName instanceof String){
                            // check this point that is in the Weapon Cone
                            
                            Point point = nonTargetPointRefs.get(teamMemberOrPointName);
                            
                            // check that the non-engage target is within the weapon cone distance to be considered for this assessment
                            // Note: might not be necessary since the non-target should be in assessedMemberToNonEngageTargetsInWeaponCone unless within the weapon cone distance.
                            double distanceBetweenObjects = firingEntityMetadata.getLastGccLocation().distance(point);
                            if(distanceBetweenObjects > weaponConeDistance){
                                continue;
                            }
                            
                            /* Test the angle to determine whether or not the assessed entity is
                             * aiming the weapon cone at this non target location. */
                            final double theta = MuzzleFlaggingCondition.getTheta(firingEntityMetadata.getLastGdcLocation(), 
                                    point.toGDC(), firingEntityHeading);
                            
                            /* If the theta value is
                             * - under the threshold the non-target team member is in the weapon cone
                             * - over the threshold the non-target team member is not in the weapon cone
                             */
                            if (theta > weaponConeHalfAngle) {
                                // not a violation
                                continue;
                            }else{
                                // Negligent Discharge Violation
                                if(logger.isDebugEnabled()){
                                    logger.debug(assessedTeamMember.getName()+" performed Negligent Discharge violation on point named "+teamMemberOrPointName);
                                }
                                nonEngageTarget.setViolatedNegligentDischarge(true);
                                violationDetected = true;
                            }
                        }

                    }// end for loop on objects in weapon cone for firing assessed team member                    
                    
                    if(violationDetected){
                        return handleViolation(assessedTeamMember);
                    }
                }
                
            }
        }
        
        return false;
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
    
    /**
     * A method that handles the case when a provided team member is not viewing the assigned sector.
     *
     * @param violatingMember The member who violated the condition by flagging
     *        another team member. Can't be null.
     * @param targetMember The member the violator is aiming at. Can't be null.
     * @return True if this violation resulted in a change in the assessment
     *         value, otherwise false.
     */
    private boolean handleViolation(TeamMember<?> violatingMember) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleViolation(" + violatingMember + ")");
        }

        final AssessmentLevelEnum prevAssessment = getAssessment();
        final int prevViolatorCount = getViolatorSize();

        final boolean violatorListChanged = prevViolatorCount != getViolatorSize();
        final boolean explanationUpdated = updateExplanation();

        final AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();

        /* If a scoring event is ongoing, reassess using the custom real-time
         * assessment in the event that the assessment value is dependent on
         * violation time. */
        if (isScoringEventActive(violatingMember)) {

            /* If the custom real-time assessment has not resulted in an
             * evaluation, no further action is required. */
            if (authoredLevel == null) {
                return violatorListChanged || explanationUpdated;
            }

            /* If the custom assessment is unchanged, no further action is
             * needed. */
            if (getAssessment() == authoredLevel) {
                return violatorListChanged || explanationUpdated;
            }

            /* Update the assessment value. */
            updateAssessment(authoredLevel);
            return true;
        }else{
            // only want to count a violation if this is a new violation of this condition
            // since this condition calls this method every time a target is engage incorrectly
            scoringEventStarted(violatingMember);
        }

        if (authoredLevel != null) {
            final boolean assessmentChanged = authoredLevel != prevAssessment;
            if (assessmentChanged) {
                updateAssessment(authoredLevel);
            }

            return violatorListChanged || assessmentChanged || explanationUpdated;
        }

        /* If the previous assessment is already below expectation, no
         * assessment change is possible. */
        if (prevAssessment == AssessmentLevelEnum.BELOW_EXPECTATION) {
            return violatorListChanged || explanationUpdated;
        }

        updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
        return true;
    }

    /**
     * A method that handles the case when a provided team member is 
     * viewing the assigned sector.
     *
     * @param teamMemberNotViolating The {@link TeamMember<?>} of the member who is
     *        viewing the assigned sector. Can't be null.
     * @return True if this abidance results in a change in the assessment
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
        final boolean explanationUpdated = updateExplanation();

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

            return violatorListChanged || assessmentChanged || explanationUpdated;
        }

        /* If the previous assessment is already at expectation, no further
         * action is needed. */
        if (prevAssessment == AssessmentLevelEnum.AT_EXPECTATION) {
            return violatorListChanged || explanationUpdated;
        }

        /* If there are still violators, the assessment level can't change
         * yet. */
        if (getViolatorSize() != 0) {
            return violatorListChanged || explanationUpdated;
        }

        updateAssessment(AssessmentLevelEnum.AT_EXPECTATION);
        return true;
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
    
    /**
     * Updates the value of {@link AbstractCondition#assessmentExplanation}
     * based on the current value of {@link #buildViolatorsInfo()}.
     * 
     * @return whether the assessment explanation was changed from the previous value
     */
    private boolean updateExplanation() {
        
        final StringBuilder sb = new StringBuilder();
        synchronized(assessedMemberToNonEngageTargetsInWeaponCone){            
            
            Iterator<TeamMember<?>> membersBeingAssessedItr = assessedMemberToNonEngageTargetsInWeaponCone.keySet().iterator();
            while(membersBeingAssessedItr.hasNext()){
                
                TeamMember<?> memberBeingAssessed = membersBeingAssessedItr.next();
                Iterator<ViewedObjectWrapper> viewedObjectsItr = assessedMemberToNonEngageTargetsInWeaponCone.get(memberBeingAssessed).values().iterator();

                if(!viewedObjectsItr.hasNext()){
                    // this member is in the larger map but somehow has no object information
                    continue;
                }
                
                while(viewedObjectsItr.hasNext()){
                    
                    ViewedObjectWrapper viewedObjectWrapper = viewedObjectsItr.next();
                    if(viewedObjectWrapper.hasViolatedNegligentDischarge()){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' fired on '").append(viewedObjectWrapper.getNameOfObject()).append("'.\n");
                    }
                }

            }
            
        }
        
        String newAssessmentExplanation = sb.toString();
        if(assessmentExplanation == null || !newAssessmentExplanation.equals(assessmentExplanation)){
            assessmentExplanation = newAssessmentExplanation;            
            if(logger.isDebugEnabled()){
                logger.debug("updatedExplanation() -\n"+assessmentExplanation);
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager) {
        super.setPlacesOfInterestManager(placesOfInterestManager);
        
        // update and convert detect point references to GDC once for quicker assessment usage as game state is received
        for(Serializable nonTarget : input.getTargetsToAvoid().getTeamMemberRefOrPointRef()){
            
            if(nonTarget instanceof generated.dkf.PointRef){
                generated.dkf.PointRef ptRef = (generated.dkf.PointRef)nonTarget;
                PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(ptRef.getValue());
                if(poi instanceof Point){
                    Point pt = (Point)poi;
                    GDC gdc = pt.toGDC();
                    if(gdc != null){
                        nonTargetPointRefs.put(ptRef.getValue(), pt); 
                    }
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[EngageTargetsCondition: ")
                .append(", weaponConeHalfAngle = ").append(weaponConeHalfAngle)
                .append(", weaponConeDistance = ").append(weaponConeDistance)
                .append(", membersToNotEngage = ").append(memberTargetsToNotEngage)
                .append(", # points to not engage = ").append(nonTargetPointRefs.size())
                .append(", ").append(super.toString())
                .append(']').toString();
    }
    
    /**
     * Used to keep information about viewed objects, i.e. objects that come within the field of view cone,
     * and whether the assessed member fired on that target (negligent discharge).
     * 
     * @author mhoffman
     *
     */
    private class ViewedObjectWrapper implements Serializable{

        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
        /** the target team member or point in view */
        private Serializable teamMemberOrPointName;
        
        /** whether the assessed member fired upon this viewed object */
        private boolean didNegligentDischarge = false;
        
        /**
         * Set attribute
         * @param teamMember the target team member to engage
         */
        public ViewedObjectWrapper(TeamMember<?> teamMember){
            setTeamMemberOrPointName(teamMember);
        }
        
        /**
         * Set attribute
         * @param pointName the target point to engage
         */
        public ViewedObjectWrapper(String pointName){
            setTeamMemberOrPointName(pointName);
        }
        
        /**
         * Return the name of the object being detected in this instance.
         * @return the name of the object.  Won't be null or empty.
         */
        private String getNameOfObject(){
            
            if(teamMemberOrPointName instanceof String){
                return (String)teamMemberOrPointName;
            }else if(teamMemberOrPointName instanceof TeamMember<?>){
                return ((TeamMember<?>)teamMemberOrPointName).getName();
            }else{
                return "<unknown name>";
            }
        }
        
        /**
         * Set target that is in view
         * @param teamMemberOrPointName the target team member or point in view
         */
        private void setTeamMemberOrPointName(Serializable teamMemberOrPointName){
            
            if(teamMemberOrPointName == null){
                throw new IllegalArgumentException("The teamMemberOrPointName is null.");
            }

            this.teamMemberOrPointName = teamMemberOrPointName;
        }
        
        @Override
        public int hashCode(){
            
            final int prime = 31;
            int result = 1;
            result = prime * result + getTeamMemberOrPointName().hashCode();
            return result;
        }
        
        /**
         * Return the target team member or point in view
         * @return either a {@link TeamMember} or String. won't be null.
         */
        public Serializable getTeamMemberOrPointName(){
            return teamMemberOrPointName;
        }
        
        /**
         * Notification that the assessed member either just fired upon this object or fired
         * but not while this object was in weapon cone view.
         */
        public void setViolatedNegligentDischarge(boolean justViolatedNegligentDischarge){
            didNegligentDischarge = justViolatedNegligentDischarge;
        }
        
        /**
         * Return whether the assessed member fired upon this viewed object
         * @return default is false
         */
        public boolean hasViolatedNegligentDischarge(){
            return didNegligentDischarge;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ViewedTargetWrapper: teamMemberOrPointName = ");
            builder.append(getNameOfObject());
            builder.append(", didNegligentDischarge = ").append(didNegligentDischarge);
            builder.append("]");
            return builder.toString();
        }        
        
    }
}
