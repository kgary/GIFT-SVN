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
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
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
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.domain.knowledge.VariablesHandler.AbstractAssessmentActor;
import mil.arl.gift.domain.knowledge.VariablesHandler.ActorVariables;
import mil.arl.gift.domain.knowledge.VariablesHandler.TeamMemberActor;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.domain.knowledge.common.metric.grade.DefaultGradeMetric;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assesses whether one or more team members are properly engaging targets. Weapon
 * cone is used as a parameter for the assessment algorithm.
 * @author mhoffman
 *
 */
public class EngageTargetsCondition extends AbstractCondition {
    
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(EngageTargetsCondition.class);

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
            Paths.get("docs", "conditions", "EngageTargets.GIFT Domain condition description.html").toFile(),
            "Engage Targets");
    
    /** name of the thread used to prevent thrashing of assessments from a violator */
    private static final String TIMER_NAME = "Engage Target violation timer";
    
    /**
     * the maximum amount of time it can take for an object to be detected once
     * in the field of view to receive an Above Expectation assessment.
     */
    private double maxAboveAssessmentTimeMs = 2.5;
    
    /**
     * the maximum amount of time it can take for an object to be detected once
     * in the field of view to receive an At Expectation assessment.
     */
    private double maxAtAssessmentTimeMs = 5.0;
    
    /**
     * The maximum distance to consider a target/non-target to be within
     * range of being assessed for engagement.  i.e. ignore objects that
     * are far away.
     */
    private int weaponConeDistance = 300;
    
    /**
     * The maximum angle between an assessed members orientation vector
     * and an engage target/non-target to be considered in the weapon cone
     * of the assessed member.
     * Using 30 degrees as a default weapon cone.
     */
    private double weaponConeHalfAngle = 30 / 2.0;
    
    /**
     * Names of targets to engage (point names and team org member names)
     * Populated from the condition's input.  Won't be empty as a single target is required.
     */
    private Set<String> memberTargetsToEngage = new HashSet<>();
    
    /** 
     * mapping of unique team member name to the information being tracked for assessment for this condition
     * Populated once an Entity State message is received for a target.
     */
    private Map<String, AssessedEntityMetadata> memberTargetsToEngageAssessmentMap = new HashMap<>();
    
    /**
     * mapping of assessed team member to the current targets (team member or point name) to engage in view.  The current collection in view
     * can be null or empty.
     */
    private Map<TeamMember<?>, Map<String, ViewedTargetWrapper>> assessedMemberToEngageTargetsInWeaponCone = new HashMap<>();
    
    /**
     * mapping of place of interest name to the Point object which contains the GCC/GDC coordinate information.
     * Used to check if the assessed member is looking at these points that should be fired upon.
     * Can be empty if only team member targets are specified.
     */
    private Map<String, Point> targetPointRefs = new HashMap<>();
    
    /**
     * the timer used when the engage targets is actively being violated, need to know if the violation
     * has been sustained for a certain amount of time as provided by the author
     */
    private SchedulableTimer violationTimer = null;
    
    /**
     * the configuration for this condition instance
     */
    private generated.dkf.EngageTargetsCondition input;
        
    /**
     * Empty constructor required for authoring logic to work.
     */
    public EngageTargetsCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("EngageTargetsCondition()");
        }
        
    }
    
    /**
     * Constructs a {@link EngageTargetsCondition} that is configured with a
     * {@link generated.dkf.EngageTargetsCondition}.
     *
     * @param input The {@link generated.dkf.EngageTargetsCondition} used to
     *        configure the assessment performed by the
     *        {@link EngageTargetsCondition}.
     */
    public EngageTargetsCondition(generated.dkf.EngageTargetsCondition input) {
        super(AssessmentLevelEnum.UNKNOWN);
        if (logger.isInfoEnabled()) {
            logger.info("EngageTargetsCondition(" + input + ")");
        }
        
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }
        
        this.input = input;
        
        setTeamMembersBeingAssessed(input.getTeamMemberRefs());
        
        // weapon cone distance - optional parameter
        if(input.getWeaponConeMaxDistance() != null){
            weaponConeDistance = input.getWeaponConeMaxDistance().intValue();
        }
        
        // weapon cone angle
        weaponConeHalfAngle = input.getWeaponConeAngle() / 2;
        
        // maximum time to get a Above Expectation (0 to this value)
        if(input.getAboveExpectationUpperBound() != null){
            maxAboveAssessmentTimeMs = input.getAboveExpectationUpperBound().doubleValue() * 1000.0;
        }
        
        // maximum time to get an At Expectation (Above Max to this value)
        if(input.getAtExpectationUpperBound() != null){
            maxAtAssessmentTimeMs = input.getAtExpectationUpperBound().doubleValue() * 1000.0;
        }
        
        for(Serializable targetObj : input.getTargetsToEngage().getTeamMemberRefOrPointRef()){
            
            if(targetObj instanceof String){
                // this is a team member name reference
                
                String targetMemberName = (String)targetObj;                
                memberTargetsToEngage.add(targetMemberName);
            }
        }
    }
    
    @Override
    public void stop(){
        super.stop();
        
        // finish the repeating timer task
        if(violationTimer != null){
            violationTimer.cancel();
        }
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        if (logger.isTraceEnabled()) {
            logger.trace("handleTrainingAppGameState(" + message + ")");
        }

        final MessageTypeEnum msgType = message.getMessageType();

        if (MessageTypeEnum.ENTITY_STATE.equals(msgType)) {
            EntityState es = (EntityState) message.getPayload();
            
            TeamMember<?> teamMember = getTeamMemberFromTeamOrg(es);
            if(teamMember == null){
                // this entity is not in the team org
                return false;
            }

            // capture last location of target and non-target entities for next comparison to assigned members
            if(memberTargetsToEngage.contains(teamMember.getName())){
                // found a target

                final AssessedEntityMetadata assessedMetadata = memberTargetsToEngageAssessmentMap
                        .computeIfAbsent(teamMember.getName(), key -> new AssessedEntityMetadata());
                assessedMetadata.updateMetadata(es);
                
                // for now don't bother checking all of the assessed members, wait for the assessed
                // member's next entity state instead.  If this approach results in inadequate assessment
                // fidelity, than remove the return statement and use this target's location update for assessment now.
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
            // Determine if any engage or non-engage targets on in the weapon cone in order to start timing.
            //
            
            synchronized(assessedMemberToEngageTargetsInWeaponCone){

                Map<String, ViewedTargetWrapper> engageTargetsInWeaponCone = assessedMemberToEngageTargetsInWeaponCone.get(teamMember);
                if(engageTargetsInWeaponCone == null){
                    engageTargetsInWeaponCone = new HashMap<>();
                    assessedMemberToEngageTargetsInWeaponCone.put(teamMember, engageTargetsInWeaponCone);
                }
                
                // check engage points 
                for(String pointName : targetPointRefs.keySet()){
                    
                    Point point = targetPointRefs.get(pointName);
                    GDC gdcPt = point.toGDC();
                    
                    ViewedTargetWrapper targetWrapper = engageTargetsInWeaponCone.get(pointName);
                    if(targetWrapper != null && targetWrapper.getAssessment() != null){
                        // the target point has already been assessed for this assessed member, currently
                        // not assessing the same point differently for subsequent actions made by the assessed member
                        continue;
                    }
                    
                    // check that the engage target point is within the weapon cone distance to be considered for this assessment
                    double distanceBetweenLocations = es.getLocation().distance(point);
                    if(distanceBetweenLocations > weaponConeDistance){
                        // the target point could have moved out of the weapon cone
                        // don't check if its in the weapon cone
                        continue;
                    }
                    
                    /* Test the angle to determine whether or not this target point is
                     * in the field of view of the assessed team member. */
                    final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, gdcPt, assessedHeading);
                    
                    /* If the theta value is
                     * - under the threshold the target point is in the weapon cone
                     * - over the threshold the target point is not in the weapon cone
                     */
                    if (theta <= weaponConeHalfAngle) {
                        // make sure target is being tracked as in Weapon Cone
                        
                        if(targetWrapper == null){
                            
                            if(logger.isDebugEnabled()){
                                logger.debug(teamMember.getName()+" has target point '"+pointName+"' in weapon cone");
                            }
                            targetWrapper = new ViewedTargetWrapper(pointName);
                            engageTargetsInWeaponCone.put(pointName, targetWrapper);
                        }
                    }else{
                        // remove target from being tracked as in Weapon Cone
                        engageTargetsInWeaponCone.remove(pointName);
                    }
                    
                }
                
                // check engage team members
                for(String targetMemberName : memberTargetsToEngageAssessmentMap.keySet()){
                    
                    TeamMember<?> targetMember = getTeamMember(targetMemberName);
                    AssessedEntityMetadata targetMemberMetadata = memberTargetsToEngageAssessmentMap.get(targetMemberName);
                    
                    ViewedTargetWrapper targetWrapper = engageTargetsInWeaponCone.get(targetMemberName);
                    if(targetWrapper != null && targetWrapper.getAssessment() != null){
                        // the target member has already been assessed for this assessed member, currently
                        // not assessing the same point differently for subsequent actions made by the assessed member
                        continue;
                    }
                    
                    // if target is dead, it is no longer a target that should be considered
                    if(targetMemberMetadata.getDamage() == DamageEnum.DESTROYED){
                        engageTargetsInWeaponCone.remove(targetMemberName);
                        continue;
                    }
                    
                    // check that the engage target is within the weapon cone distance to be considered for this assessment
                    double distanceBetweenLocations = es.getLocation().distance(targetMemberMetadata.getLastGccLocation());
                    if(distanceBetweenLocations > weaponConeDistance){
                        // TODO: may want to not remove here because if the object entered the Weapon Cone w/in max distance and
                        //       then moved beyond max distance w/o first entering the inner cone, isn't this a violation of not engaging?
                        engageTargetsInWeaponCone.remove(targetMemberName); // the target could have moved out of the weapon cone
                        continue;
                    }
                    
                    /* Test the angle to determine whether or not this target team member is
                     * in the field of view of the assessed team member. */
                    final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, targetMemberMetadata.getLastGdcLocation(), assessedHeading);
                    
                    /* If the theta value is
                     * - under the threshold the target team member is in the weapon cone
                     * - over the threshold the target team member is not in the weapon cone
                     */
                    if (theta <= weaponConeHalfAngle) {
                        // make sure target is being tracked as in Weapon Cone
                        
                        if(targetWrapper == null){
                            if(logger.isDebugEnabled()){
                                logger.debug(teamMember.getName()+" has target team member '"+targetMember.getName()+"' in weapon cone");
                            }
                            targetWrapper = new ViewedTargetWrapper(targetMember);
                            engageTargetsInWeaponCone.put(targetMember.getName(), targetWrapper);
                        }
                    }else{
                        // remove target from being tracked as in Weapon Cone
                        engageTargetsInWeaponCone.remove(targetMemberName);
                    }
                }
            }
            
            if(violationTimer == null){
                // the timer is not running and the timer is needed in order to determine if the learner has violated the assigned
                // sector for a long enough time

                violationTimer = new SchedulableTimer(TIMER_NAME);

                // run assessment reporting logic twice as often as the time an assessment must be maintained
                violationTimer.scheduleAtFixedRate(new ViolationTimeTask(), (long)(maxAboveAssessmentTimeMs) / 2, (long)(maxAboveAssessmentTimeMs) / 2);

                if(logger.isDebugEnabled()) {
                    logger.debug("Started the scheduler for assessing whether targets are being engaged in a timely manner.");
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
                // if engage targets in weapon cone view > 0 => (SUCCESS)
                // else - no assessment
                
                // For all targets in the assessed member's weapon cone, update the last check timestamp to prevent
                // an Omission violation from occurring since the assessed member has fired their weapon.
                synchronized(assessedMemberToEngageTargetsInWeaponCone){
                    Map<String, ViewedTargetWrapper> engageTargetsInWeaponCone = assessedMemberToEngageTargetsInWeaponCone.get(assessedTeamMember);
                    for(ViewedTargetWrapper targetWrapper : engageTargetsInWeaponCone.values()){
                        
                    	// track the time at which the shooter begins engaging the target
                    	AbstractAssessmentActor<?> targetActor = null;
                    	if(targetWrapper.getTeamMemberOrPointName() instanceof TeamMember<?>) {
                    		targetActor = new TeamMemberActor(((TeamMember<?>) targetWrapper.getTeamMemberOrPointName()).getName());
                    	} else {
                    		//TODO: Need to track open time for points as well
                    	}
                    	
                    	if(targetActor != null) {
	                    	if(varsHandler.getVariable(targetActor, ActorVariables.OPEN_TIME) == null) {
		                    	varsHandler.setVariable(targetActor, ActorVariables.OPEN_TIME, System.currentTimeMillis());
                    		}
                    	}
                    	
                    	// update the last time this entity was fired upon while in Weapon Cone
                        targetWrapper.updateLastWeaponFireWhileInWeaponCone();
                    }
                }
                
                AssessedEntityMetadata firingEntityMetadata = entityMetadataLookup.get(firingEntityId);
                if(firingEntityMetadata == null){
                    // an entity state hasn't been received for the assessed member
                    return false;
                }
                
                final double firingEntityHeading = CoordinateUtil.getInstance().getHeading(firingEntityMetadata.getLastOrientation(), 
                        firingEntityMetadata.getLastGdcLocation());
                                                
                // [CHECK] Correctly engage target(s)
                // Determine if engage target(s) are in the weapon cone of the assessed member AND not beyond the weapon cone distance.
                synchronized(assessedMemberToEngageTargetsInWeaponCone){

                    Map<String, ViewedTargetWrapper> engageTargetsInWeaponCone = assessedMemberToEngageTargetsInWeaponCone.get(assessedTeamMember);
                    if(engageTargetsInWeaponCone != null){
                    	
                    	ViewedTargetWrapper engagedTarget = null;
                        for(ViewedTargetWrapper engageTarget : engageTargetsInWeaponCone.values()){
                            
                            if(engageTarget.getAssessment() != null){
                                // can't assess the same target for the same assessed member more than once during being in the
                                // assessed member's weapon cone w/o leaving the weapon cone
                                continue;
                            }
                            
                            if(engageTarget.getTeamMemberOrPointName() instanceof TeamMember<?>){
                                // shooting at target team member
                                
                                TeamMember<?> engageTargetMember = (TeamMember<?>)engageTarget.getTeamMemberOrPointName();
                                AssessedEntityMetadata targetMemberMetadata = memberTargetsToEngageAssessmentMap.get(engageTargetMember.getName());
                                // if target is dead, it is no longer a target that should be considered
                                if(targetMemberMetadata == null || targetMemberMetadata.getDamage() == DamageEnum.DESTROYED){
                                    continue;
                                }
        
                                // check that the engage target is within the weapon cone distance to be considered for this assessment
                                // Note: might not be necessary since the target should be in assessedMemberToEngageTargetsInWeaponCone unless within the weapon cone distance.
                                double distanceBetweenEntities = firingEntityMetadata.getLastGccLocation().distance(targetMemberMetadata.getLastGccLocation());
                                if(distanceBetweenEntities > weaponConeDistance){
                                    continue;
                                }
                                
                                /* Test the angle to determine whether or not the assessed entity is
                                 * aiming the weapon cone at this target. */
                                final double theta = MuzzleFlaggingCondition.getTheta(firingEntityMetadata.getLastGdcLocation(), 
                                        targetMemberMetadata.getLastGdcLocation(), firingEntityHeading);
                                
                                /* If the theta value is
                                 * - under the threshold the target team member is in the weapon cone
                                 * - over the threshold the target team member is not in the weapon cone
                                 */
                                if (theta > weaponConeHalfAngle) {
                                    // not a violation of this condition or its set parameters - could be firing at another target, or incorrectly firing their weapon
                                    continue;
                                }else{
                                    // Correctly engage a target
                                    
                                    if(logger.isDebugEnabled()){
                                        logger.debug(assessedTeamMember.getName()+" correctly engaged target member of "+engageTargetMember.getName());
                                    }
                                    
                                    long detectDuration = engageTarget.getLastWeaponFireWhileInWeaponCone() - engageTarget.getEnteredWeaponCone();
                                    if(detectDuration < maxAboveAssessmentTimeMs){
                                        // this assessed member receives above expectation for this object
                                        engagedTarget = engageTarget;
                                        engageTarget.setAssessment(AssessmentLevelEnum.ABOVE_EXPECTATION);

                                    }else if(detectDuration < maxAtAssessmentTimeMs){
                                        // this assessed member receives at expectation for this object
                                        engagedTarget = engageTarget;
                                        engageTarget.setAssessment(AssessmentLevelEnum.AT_EXPECTATION);
                                    }
                                }
                            }else if(engageTarget.getTeamMemberOrPointName() instanceof String){
                                
                                String pointName = (String)engageTarget.getTeamMemberOrPointName();
                                Point point = targetPointRefs.get(pointName);
                                
                                // check that the engage target is within the weapon cone distance to be considered for this assessment
                                // Note: might not be necessary since the target should be in assessedMemberToEngageTargetsInWeaponCone unless within the weapon cone distance.
                                double distanceBetweenEntities = firingEntityMetadata.getLastGccLocation().distance(point);
                                if(distanceBetweenEntities > weaponConeDistance){
                                    continue;
                                }
                                
                                /* Test the angle to determine whether or not the assessed entity is
                                 * aiming the weapon cone at this target. */
                                final double theta = MuzzleFlaggingCondition.getTheta(firingEntityMetadata.getLastGdcLocation(), 
                                        point.toGDC(), firingEntityHeading);
                                
                                /* If the theta value is
                                 * - under the threshold the target location is in the weapon cone
                                 * - over the threshold the target location is not in the weapon cone
                                 */
                                if (theta > weaponConeHalfAngle) {
                                    // not a violation of this condition or its set parameters - could be firing at another target, or incorrectly firing their weapon
                                    continue;
                                }else{
                                    // Correctly engage a target
                                    
                                    if(logger.isDebugEnabled()){
                                        logger.debug(assessedTeamMember.getName()+" correctly engaged target point of "+point.getName());
                                    }
                                    
                                    long detectDuration = engageTarget.getLastWeaponFireWhileInWeaponCone() - engageTarget.getEnteredWeaponCone();
                                    if(detectDuration < maxAboveAssessmentTimeMs){
                                        // this assessed member receives above expectation for this object
                                        engagedTarget = engageTarget;
                                        engageTarget.setAssessment(AssessmentLevelEnum.ABOVE_EXPECTATION);

                                    }else if(detectDuration < maxAtAssessmentTimeMs){
                                        // this assessed member receives at expectation for this object
                                        engagedTarget = engageTarget;
                                        engageTarget.setAssessment(AssessmentLevelEnum.AT_EXPECTATION);
                                    }
                                }
                            }
                        } // end for on targets
                        
                        if(engagedTarget != null){
                            // there was at least 1 correctly engaged target
                            updateAssessment();
                            updateExplanation();
                            
                            String engageName = null;
                            if(engagedTarget.getTeamMemberOrPointName() instanceof TeamMember<?>){
                                TeamMember<?> engageMember = (TeamMember<?>) engagedTarget.getTeamMemberOrPointName();
                                engageName = engageMember.getName();
                            
                            } else {
                                engageName = (String) engagedTarget.getTeamMemberOrPointName();
                            }
                            
                            for(String member : memberTargetsToEngageAssessmentMap.keySet()) {
                            	
                            	if(member.equals(engageName)) {
                            		/* Skip calculating close time for the target that was engaged*/
                            	    continue;
                            	}
                            	
                            	TeamMemberActor targetActor = new TeamMemberActor(member);
                            	
                            	/* For all other targets, first see if fire was opened on them AND the target was NOT eliminated.
                            	 * If so, then the close fire time needs to be tracked*/
                            	if(varsHandler.getVariable(targetActor, ActorVariables.OPEN_TIME) != null
                            			&& varsHandler.getVariable(targetActor, ActorVariables.TARGET_FIRST_STRIKE) == null
        	                    		&& varsHandler.getVariable(targetActor, ActorVariables.TARGET_CLOSE_TIME) == null) {
                            	    
                            		varsHandler.setVariable(targetActor, ActorVariables.TARGET_CLOSE_TIME, System.currentTimeMillis());
                            	}
                            }
                            
                            return true;
                        }
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
     * Check the latest assessments of the assessed members and update the condition's assessment if
     * necessary.
     * @return true if the condition's assessment has changed.
     */
    private boolean updateAssessment(){
        if(logger.isDebugEnabled()){
            logger.debug("Calculating new assessment value");
        }
        
        synchronized(assessedMemberToEngageTargetsInWeaponCone){
            
            int collectiveScoreTotal = 0, assessedMembers = 0;
            Iterator<TeamMember<?>> assessedMemberItr = assessedMemberToEngageTargetsInWeaponCone.keySet().iterator();
            while(assessedMemberItr.hasNext()){
                
                TeamMember<?> assessedMember = assessedMemberItr.next();
                Map<String, ViewedTargetWrapper> targetWrappers = assessedMemberToEngageTargetsInWeaponCone.get(assessedMember);
                if(CollectionUtils.isEmpty(targetWrappers)){
                    continue;
                }
                
                int individualScoreTotal = 0, targetsToInclude = 0;
                Iterator<ViewedTargetWrapper> targetWrapperItr = targetWrappers.values().iterator();
                while(targetWrapperItr.hasNext()){
                    
                    // assume this target is going to be assessed for this team member
                    targetsToInclude++; 
                    
                    ViewedTargetWrapper targetWrapper = targetWrapperItr.next();
                    AssessmentLevelEnum assessment = targetWrapper.getAssessment();
                    if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                        individualScoreTotal += DefaultGradeMetric.BELOW_EXPECTATION_SCORE;                                
                    }else if(assessment == AssessmentLevelEnum.AT_EXPECTATION){
                        individualScoreTotal += DefaultGradeMetric.AT_EXPECTATION_SCORE;
                    }else if(assessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
                        individualScoreTotal += DefaultGradeMetric.ABOVE_EXPECTATION_SCORE;
                    }else{
                        // this target has not been assessed yet for this team member
                        targetsToInclude--;
                    }
                }
                
                // get average score for this assessed member
                double individualScoreAvg;
                if(targetsToInclude == 0){
                    // no targets where assessed for this team member
                    continue;
                }else{
                    assessedMembers++; // this member was assessed against at least 1 target
                    individualScoreAvg = individualScoreTotal / targetsToInclude;
                }
                if(individualScoreAvg < DefaultGradeMetric.BELOW_EXPECTATION_UPPER_THRESHOLD){
                    collectiveScoreTotal += DefaultGradeMetric.BELOW_EXPECTATION_SCORE;                            
                }else if(individualScoreAvg < DefaultGradeMetric.AT_EXPECTATION_UPPER_THRESHOLD){
                    collectiveScoreTotal += DefaultGradeMetric.AT_EXPECTATION_SCORE;
                }else{
                    collectiveScoreTotal += DefaultGradeMetric.ABOVE_EXPECTATION_SCORE;
                }
                
            }
            
            // calculate single assessment for this condition
            AssessmentLevelEnum newAssessment = null;
            if(assessedMembers > 0){
                double collectiveScoreAvg = collectiveScoreTotal/assessedMembers;
                if(collectiveScoreAvg < DefaultGradeMetric.BELOW_EXPECTATION_UPPER_THRESHOLD){
                    newAssessment = AssessmentLevelEnum.BELOW_EXPECTATION;
                }else if(collectiveScoreAvg < DefaultGradeMetric.AT_EXPECTATION_UPPER_THRESHOLD){
                    newAssessment = AssessmentLevelEnum.AT_EXPECTATION;
                }else{
                    newAssessment = AssessmentLevelEnum.ABOVE_EXPECTATION; 
                }
            }
            
            if(newAssessment != null && !getAssessment().equals(newAssessment)){
                // the calculated assessment is different than the current set assessment for this condition instance 
                updateAssessment(newAssessment);
                
                if(newAssessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                    // count number of At/Above to Below, and duration of condition in Below state
                    scoringEventStarted();
                }else{
                    // stop timer for Below state
                    scoringEventEnded();
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Updates the value of {@link AbstractCondition#assessmentExplanation}
     * based on the current value of {@link #assessedMemberToEngageTargetsInWeaponCone()}.
     * @return whether the assessment explanation was changed from the previous value
     */
    private boolean updateExplanation() {
        
        final StringBuilder sb = new StringBuilder();
        synchronized(assessedMemberToEngageTargetsInWeaponCone){
            
            Iterator<TeamMember<?>> membersBeingAssessedItr = assessedMemberToEngageTargetsInWeaponCone.keySet().iterator();
            while(membersBeingAssessedItr.hasNext()){
                
                TeamMember<?> memberBeingAssessed = membersBeingAssessedItr.next();
                Iterator<ViewedTargetWrapper> viewedObjectsItr = assessedMemberToEngageTargetsInWeaponCone.get(memberBeingAssessed).values().iterator();

                if(!viewedObjectsItr.hasNext()){
                    // this member is in the larger map but somehow has no object information
                    continue;
                }
                
                while(viewedObjectsItr.hasNext()){
                    
                    ViewedTargetWrapper viewedObjectWrapper = viewedObjectsItr.next();
                    AssessmentLevelEnum assessment = viewedObjectWrapper.getAssessment();
                    if(assessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' engaged '").append(viewedObjectWrapper.getNameOfObject()).append("' above standard.\n");
                    }else if(assessment == AssessmentLevelEnum.AT_EXPECTATION){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' engaged '").append(viewedObjectWrapper.getNameOfObject()).append("' at standard.\n");
                    }else if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' engaged '").append(viewedObjectWrapper.getNameOfObject()).append("' below standard.\n");
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
    
    /**
     * Notify the parent concept to this condition that the condition has a new assessment
     * outside the handle training app game state method call (i.e. because the violation timer task fired)
     */
    private void sendAsynchAssessmentNotification(){
        sendAssessmentEvent();
    }
    
    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager) {
        super.setPlacesOfInterestManager(placesOfInterestManager);
        
        // update and convert engage point references to GDC once for quicker assessment usage as game state is received
        for(Serializable target : input.getTargetsToEngage().getTeamMemberRefOrPointRef()){
            
            if(target instanceof generated.dkf.PointRef){
                generated.dkf.PointRef ptRef = (generated.dkf.PointRef)target;
                PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(ptRef.getValue());
                if(poi instanceof Point){
                    Point pt = (Point)poi;
                    GDC gdc = pt.toGDC();
                    if(gdc != null){
                        targetPointRefs.put(ptRef.getValue(), pt); 
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
                .append(", membersToEngage = ").append(memberTargetsToEngage)
                .append(", # points to engage = ").append(targetPointRefs.size())
                .append(", maxAboveTimeMs = ").append(maxAboveAssessmentTimeMs)
                .append(", maxAtTimeMs = ").append(maxAtAssessmentTimeMs)
                .append(", ").append(super.toString())
                .append(']').toString();
    }
    
    /**
     * Used to keep information about viewed targets, i.e. targets that come within the field of view cone,
     * and when the last time the assessed member fired on that target.
     * 
     * @author mhoffman
     *
     */
    private class ViewedTargetWrapper implements Serializable{

        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
        /** the target team member or point in view */
        private Serializable teamMemberOrPointName;

        /** 
         * the epoch time when the assessed member fired their
         * weapon while this target was in the weapon cone
         */
        private Long lastWeaponFireWhileInWeaponCone = null;
        
        /**
         * the epoch time when the target entered the weapon cone
         */
        private long enteredWeaponConeTime = System.currentTimeMillis();
        
        /**
         * the latest assessment for this target object against the assessed member
         */
        private AssessmentLevelEnum assessment = null;
        
        /**
         * Set attribute
         * @param teamMember the target team member to engage
         */
        public ViewedTargetWrapper(TeamMember<?> teamMember){
            setTeamMemberOrPointName(teamMember);
        }
        
        /**
         * Set attribute
         * @param pointName the target point to engage
         */
        public ViewedTargetWrapper(String pointName){
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
         * Set the time at which the assessed member fired their weapon while this target was
         * in the weapon cone to the current epoch time.
         */
        public void updateLastWeaponFireWhileInWeaponCone(){
            lastWeaponFireWhileInWeaponCone = System.currentTimeMillis();
        }

        /**
         * Return the epoch time when the assessed member fired their
         * weapon while this target was in the weapon cone
         * @return epoch time, can be null if not fired upon yet.
         */
        public Long getLastWeaponFireWhileInWeaponCone(){
            return lastWeaponFireWhileInWeaponCone;
        }
        
        /**
         * Return the epoch time when the target entered
         * the assessed member's weapon cone.
         * @return epoch time
         */
        public long getEnteredWeaponCone(){
            return enteredWeaponConeTime;
        }

        /**
         * Return the latest assessment for this target object against the assessed member
         * @return can be null if this object has not been assessed
         */
        public AssessmentLevelEnum getAssessment() {
            return assessment;
        }

        /**
         * Set the latest assessment for this target object against the assessed member
         * @param assessment for this condition the value is AT (correctly engaged) or 
         * Below (omission), currently.
         */
        public void setAssessment(AssessmentLevelEnum assessment) {
            this.assessment = assessment;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ViewedTargetWrapper: teamMemberOrPointName = ");
            builder.append(teamMemberOrPointName);
            builder.append(", lastWeaponFireWhileInWeaponCone = ");
            builder.append(lastWeaponFireWhileInWeaponCone);
            builder.append(", enteredWeaponConeTime = ");
            builder.append(enteredWeaponConeTime);
            builder.append(", lastAssessment = ").append(assessment);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
    
    /**
     * This class is the timer task which runs at the appropriately scheduled date.  It will
     * cause a change in assessment when an assessed entity has not engaged a target after a long enough duration
     * as set by the author.
     *
     * @author mhoffman
     *
     */
    private class ViolationTimeTask extends TimerTask{

        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("engage targets violation timer task fired.");
            }
            
            boolean assessmentChanged = false;
            
            try{

                boolean needExplanationUpdate = false;
                synchronized(assessedMemberToEngageTargetsInWeaponCone){
                    long now = System.currentTimeMillis();
                    Iterator<TeamMember<?>> assessedMemberItr = assessedMemberToEngageTargetsInWeaponCone.keySet().iterator();
                    while(assessedMemberItr.hasNext()){
                        
                        TeamMember<?> assessedMember = assessedMemberItr.next();
                        Map<String, ViewedTargetWrapper> targetWrappers = assessedMemberToEngageTargetsInWeaponCone.get(assessedMember);
                        if(CollectionUtils.isEmpty(targetWrappers)){
                            continue;
                        }
                        
                        Iterator<ViewedTargetWrapper> targetWrapperItr = targetWrappers.values().iterator();
                        while(targetWrapperItr.hasNext()){
                            ViewedTargetWrapper targetWrapper = targetWrapperItr.next();
                            
                            if(targetWrapper.getAssessment() != null){
                                // this target has been assessed for this assessed member, don't assess again
                                continue;
                            }
                            
                            long engageDuration = now - targetWrapper.getEnteredWeaponCone();
                            if(engageDuration > maxAtAssessmentTimeMs){
                                
                                // Omission (VIOLATION)
                                // for now call this for each target not being fired upon for this assessed member,
                                // maybe in the future only call this once per team member or once if the team member
                                // was not violating this condition when the last check was performed
                                if(logger.isDebugEnabled()){
                                    logger.debug(assessedMember.getName()+" performed Omission violation on target "+targetWrapper.getTeamMemberOrPointName());
                                }
                                targetWrapper.setAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
                                assessmentChanged |= updateAssessment();
                                needExplanationUpdate = true;
                            }
                        }
                    }
                }
                
                if(needExplanationUpdate){
                    updateExplanation();
                    sendAsynchAssessmentNotification();
                }else if(assessmentChanged){
                    sendAsynchAssessmentNotification();
                }
            }catch(Throwable t){
                logger.error("An error happened while checking for omission violation", t);
            }

        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("[EngageTargetsCondition-ViolationTimerTask: ");
            sb.append("]");

            return sb.toString();
        }
    }

}
