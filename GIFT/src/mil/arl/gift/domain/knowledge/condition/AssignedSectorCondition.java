/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

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
import generated.dkf.RealTimeAssessmentRules;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assesses whether one or more team members are looking in their
 * assigned sector.
 * 
 * @author mhoffman
 *
 */
public class AssignedSectorCondition extends AbstractCondition {
    
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(AssignedSectorCondition.class);

    /** The message types that this condition is interested in. */
    private static final List<MessageTypeEnum> simulationInterests = Arrays.asList(MessageTypeEnum.ENTITY_STATE);
    
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
            Paths.get("docs", "conditions", "AssignedSector.GIFT Domain condition description.html").toFile(),
            "Assigned Sector");
    
    /** name of the thread used to prevent thrashing of assessments from a violator */
    private static final String TIMER_NAME = "Assigned Sector violation timer";
    
    /**
     * the unique name of the point that represents the center of the sector
     */
    private String pointRef = null;
    
    /**
     * the coordinate of the point that represents the center of the sector.
     * Will be null if not retrieved from places of interest manager or if the coordinate is not a supported type.
     */
    private GDC sectorMiddlePoint = null;
    
    /**
     * The maximum angle between an entity's orientation and relative location
     * vector of the assigned sector middle point 
     */
    private double maxAngleFromCenter;
    
    /**
     * (optional) time in milliseconds that a member is allowed to violate the max angle from
     * the sector middle point
     */
    private Double freeLookDurationMS = null;
    
    /**
     * the timer used when the assigned sector is actively being violated, need to know if the violation
     * has been sustained for a certain amount of time as provided by the author
     */
    private SchedulableTimer violationTimer = null;
    
    /**
     * mapping of assessed team members that are not currently looking in their assigned
     * sector to the information about that team member.  Used to allow free look actions
     * for a small window of time without tagging the member as violating.
     * Can be null if free look is not allowed.
     */
    private Map<TeamMember<?>, AssessmentWrapper> violatorCandidates = null;
    
    /**
     * Empty constructor required for authoring logic to work.
     */
    public AssignedSectorCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("AssignedSectorCondition()");
        }
        
    }
    
    /**
     * Constructs a {@link AssignedSectorCondition} that is configured with a
     * {@link generated.dkf.AssignedSectorCondition}.
     *
     * @param input The {@link generated.dkf.AssignedSectorCondition} used to
     *        configure the assessment performed by the
     *        {@link AssignedSectorCondition}.
     */
    public AssignedSectorCondition(generated.dkf.AssignedSectorCondition input) {
        super(AssessmentLevelEnum.UNKNOWN);
        if (logger.isInfoEnabled()) {
            logger.info("AssignedSectorCondition(" + input + ")");
        }
        
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }
        
        final RealTimeAssessmentRules rtaRules = input.getRealTimeAssessmentRules();
        if (rtaRules != null) {
            addRealTimeAssessmentRules(rtaRules);
        }
        
        setTeamMembersBeingAssessed(input.getTeamMemberRefs());
        
        pointRef = input.getPointRef().getValue();
        maxAngleFromCenter = input.getMaxAngleFromCenter().doubleValue();
        freeLookDurationMS = input.getFreeLookDuration() != null ? input.getFreeLookDuration().doubleValue() * 1000 : null;
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

        if (logger.isDebugEnabled()) {
            logger.debug("handleTrainingAppGameState(" + message + ")");
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
            
            final GDC memberLocation = CoordinateUtil.getInstance().convertFromGCCToGDC(es.getLocation());
            
            if(sectorMiddlePoint == null){
                // only perform this conversion from point to GDC once per instance of this condition
                PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(pointRef);
                if(poi instanceof Point){
                    Point point = (Point)poi;
                    sectorMiddlePoint = point.toGDC();
                }
            }
            
            if(sectorMiddlePoint != null){                
                
                /* Test the angle to determine whether or not this entity is
                 * violating the assigned sector condition. */
                final double assessedHeading = CoordinateUtil.getInstance().getHeading(es.getOrientation(), memberLocation);
                final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, sectorMiddlePoint, assessedHeading);
                
                /* If the theta value is over the threshold, update the
                 * assessment to below expectation because this means the angle between
                 * the two vectors is larger than the threshold - member isn't looking at their sector . */
                if (theta > maxAngleFromCenter) {
                    
                    // check duration field
                    if(freeLookDurationMS != null && freeLookDurationMS > 0){
                        
                        if(violationTimer == null){
                            // the timer is not running and the timer is needed in order to determine if the learner has violated the assigned
                            // sector for a long enough time

                            violationTimer = new SchedulableTimer(TIMER_NAME);
                            violatorCandidates = new HashMap<>();

                            // run assessment reporting logic twice as often as the time an assessment must be maintained
                            violationTimer.scheduleAtFixedRate(new ViolationTimeTask(), (long)(freeLookDurationMS / 2.0), (long)(freeLookDurationMS / 2.0));

                            if(logger.isDebugEnabled()) {
                                logger.debug("Started the scheduler for determining whether a learner is looking outside of the assigned sector for to long of a period.");
                            }
                        }
                        
                        synchronized(violatorCandidates){
                            if(!violatorCandidates.containsKey(assessedMember)){
                                AssessmentWrapper assessmentWrapper = new AssessmentWrapper(assessedMember); 
                                assessmentWrapper.updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION); // just use something so the last assessment time is set and can be checked later 
                                violatorCandidates.put(assessedMember, assessmentWrapper);
                            }
                        }
                        
                    }else{
                        // the member is violating and the author didn't specify a free look duration
                        return handleViolation(assessedMember);
                    }
                }else{
                    /* The assessed entity is looking in the assigned sector */
                    
                    if(violatorCandidates != null){
                        synchronized(violatorCandidates){
                            // remove just in case the member is in the candidate violator collection
                            violatorCandidates.remove(assessedMember);
                        }
                    }
                    
                    return handleSuccess(assessedMember);
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
     * based on the current value of {@link #getViolatorTeamOrgEntries()}.
     */
    private void updateExplanation() {
        final Set<String> violatorTeamOrgEntries = getViolatorTeamOrgEntries();
        if (CollectionUtils.isNotEmpty(violatorTeamOrgEntries)) {
            final StringBuilder sb = new StringBuilder("{ ");
            StringUtils.join(", ", violatorTeamOrgEntries, StringUtils.DEFAULT, sb);
            sb.append(" } ").append(violatorTeamOrgEntries.size() == 1 ? "is" : "are")
                    .append(" not viewing their assigned sectors.");
            assessmentExplanation = sb.toString();
        } else {
            assessmentExplanation = "";
        }
    }
    
    /**
     * Notify the parent concept to this condition that the condition has a new assessment
     * outside the handle training app game state method call (i.e. because the violation timer task fired)
     */
    private void sendAsynchAssessmentNotification(){
        sendAssessmentEvent();
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[AssignedSectorCondition: ")
                .append(", point = ").append(pointRef)
                .append(", maxAngleFromCenter = ").append(maxAngleFromCenter)
                .append(", freeLookDuration = ").append(freeLookDurationMS)
                .append(", ").append(super.toString())
                .append(']').toString();
    }
    
    /**
     * This class is the timer task which runs at the appropriately scheduled date.  It will
     * cause a change in assessment when the assigned sector has been violated long enough as set by the author.
     *
     * @author mhoffman
     *
     */
    private class ViolationTimeTask extends TimerTask{

        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("assigned sector violation timer task fired.");
            }

            synchronized(violatorCandidates){

                boolean assessmentChanged = false;
                long now = System.currentTimeMillis();

                // check the assessment map for the lowest expectation result
                Iterator<AssessmentWrapper> itr = violatorCandidates.values().iterator();
                while(itr.hasNext()){
                    AssessmentWrapper assessmentWrapper = itr.next();

                    Long assessmentTime = assessmentWrapper.getLastAssessmentTime();
                    if(now - assessmentTime >= freeLookDurationMS){
                        // the violation has been going on long enough,
                        // move to actual violation and remove from violation candidates
                        
                        assessmentChanged |= handleViolation(assessmentWrapper.getTeamMember());
                        itr.remove();

                    }
                }
                
                if(assessmentChanged){
                    sendAsynchAssessmentNotification();
                }
            }

        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("[AssignedSectorCondition-ViolationTimerTask: ");
            sb.append("]");

            return sb.toString();
        }
    }

}
