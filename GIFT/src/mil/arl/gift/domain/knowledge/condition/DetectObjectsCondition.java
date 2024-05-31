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

import javax.vecmath.Vector3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.ArticulationParameterTypeDesignatorEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.domain.knowledge.VariablesHandler.ActorVariables;
import mil.arl.gift.domain.knowledge.VariablesHandler.TeamMemberActor;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.domain.knowledge.common.metric.grade.DefaultGradeMetric;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assess whether objects are being detected in a timely manner.
 * Currently detection is determined when the assessed team member's orient towards
 * the object.
 * @author mhoffman
 *
 */
public class DetectObjectsCondition extends AbstractCondition {
    
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(DetectObjectsCondition.class);

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
            Paths.get("docs", "conditions", "DetectObjects.GIFT Domain condition description.html").toFile(),
            "Detect Objects");
    
    /** name of the thread used to prevent thrashing of assessments from a violator */
    private static final String TIMER_NAME = "Detect Objects violation timer";
    
    /**
     * the timer used when the assigned sector is actively being violated, need to know if the violation
     * has been sustained for a certain amount of time as provided by the author
     */
    private SchedulableTimer violationTimer = null;
    
    /**
     * the configuration for this condition instance
     */
    private generated.dkf.DetectObjectsCondition input;
    
    
    /**
     * The maximum angle between an assessed members orientation vector
     * and an engage target/non-target to be considered in the field of view
     * of the assessed member.
     * Using 170 degrees as human field of view.
     */
    private double fovHalfAngle = 170 / 2.0;
    
    /**
     * The maximum distance to consider an object to detect to be within
     * range of being assessed for detection.  i.e. ignore objects that
     * are far away.
     */
    private int viewMaxDistance = 300;
    
    /**
     * The maximum angle between an assessed members orientation vector
     * and an object to detect to be considered in the direct view
     * of the assessed member.
     * Using 30 degrees as a default cone.
     */
    private double orientConeHalfAngle = 30 / 2.0;
    
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
     * Names of targets to detect (point names and team org member names)
     * Populated from the condition's input.  Won't be empty as a single object is required.
     */
    private Set<String> originalMemberObjectsToDetect = new HashSet<>();
    
    /**
     * contains the remaining members to detect.  Will be empty if there were never members
     * to detect or if all members provided were detected.
     */
    private Set<String> remainingMemberObjectsToDetect = new HashSet<>();
    
    /**
     * mapping of place of interest name to the Point object which contains the GCC/GDC coordinate information.
     * Used to check if the assessed member is looking at these points that should be detected.
     * Can be empty if only team member objects are specified.
     */
    private Map<String, Point> originalObjectPointRefs = new HashMap<>();
    
    /**
     * contains the remaining points to detect.  Will be empty if there were never points
     * to detect or if all points provided were detected.
     */
    private Map<String, Point> remainingObjectPointRefs = new HashMap<>();
    
    /** 
     * mapping of unique team member name to the information being tracked for assessment for this condition
     * Populated once an Entity State message is received for an object to detect.
     */
    private Map<String, AssessedEntityMetadata> memberObjectsToDetectAssessmentMap = new HashMap<>();
    
    /**
     * mapping of assessed team member to the current objects to detect (team member or point name) in the field of view.  The current collection in view
     * can be null or empty.
     */
    private Map<TeamMember<?>, Map<String, ViewedObjectWrapper>> assessedMemberToObjectsInFOV = new HashMap<>();

    /** 
     * The timer task that is currently counting down to when the assessment should be updated. Used to 
     * skip the remaining time until an assessment if an object is detected early.
     */
	private ViolationTimeTask currentViolationTimerTask;
    
    /**
     * Empty constructor required for authoring logic to work.
     */
    public DetectObjectsCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("DetectObjectsCondition()");
        }
        
    }
    
    /**
     * Constructs a {@link DetectObjectsCondition} that is configured with a
     * {@link generated.dkf.DetectObjectsCondition}.
     *
     * @param input The {@link generated.dkf.DetectObjectsCondition} used to
     *        configure the assessment performed by the
     *        {@link DetectObjectsCondition}.
     */
    public DetectObjectsCondition(generated.dkf.DetectObjectsCondition input) {
        super(AssessmentLevelEnum.UNKNOWN);
        if (logger.isInfoEnabled()) {
            logger.info("DetectObjectsCondition(" + input + ")");
        }
        
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }
        
        this.input = input;
        
        setTeamMembersBeingAssessed(input.getTeamMemberRefs());
        
        // field of view - optional parameter
        if(input.getFieldOfView() != null){
            fovHalfAngle = input.getFieldOfView() / 2;
        }
        
        // weapon cone distance - optional parameter
        if(input.getViewMaxDistance() != null){
            viewMaxDistance = input.getViewMaxDistance().intValue();
        }
        
        // orient view cone angle - optional parameter
        if(input.getOrientAngle() != null){
            orientConeHalfAngle = input.getOrientAngle() / 2;
        }
        
        // VALIDATION - field of view must be greater than or equal to orient cone half angle
        //              in order for the logic of first finding an object in the outer cone to then
        //              determine that the assessed member oriented to that object and that object
        //              entered the smaller, inner, cone.
        if(fovHalfAngle < orientConeHalfAngle){
            throw new IllegalArgumentException("The field of view angle must be greater than the orientation angle");
        }
        
        // maximum time to get a Above Expectation (0 to this value)
        if(input.getAboveExpectationUpperBound() != null){
            maxAboveAssessmentTimeMs = input.getAboveExpectationUpperBound().doubleValue() * 1000.0;
        }
        
        // maximum time to get an At Expectation (Above Max to this value)
        if(input.getAtExpectationUpperBound() != null){
            maxAtAssessmentTimeMs = input.getAtExpectationUpperBound().doubleValue() * 1000.0;
        }
        
        for(Serializable targetObj : input.getObjectsToDetect().getTeamMemberRefOrPointRef()){
            
            if(targetObj instanceof String){
                // this is a team member name reference
                
                String targetMemberName = (String)targetObj;                
                originalMemberObjectsToDetect.add(targetMemberName);
                remainingMemberObjectsToDetect.add(targetMemberName);
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

            // capture last location of objects to detect entities for next comparison to assigned members
            if(remainingMemberObjectsToDetect.contains(teamMember.getName())){
                // found an object to detect

                final AssessedEntityMetadata assessedMetadata = memberObjectsToDetectAssessmentMap
                        .computeIfAbsent(teamMember.getName(), key -> new AssessedEntityMetadata());
                assessedMetadata.updateMetadata(es);
                
                // for now don't bother checking all of the assessed members, wait for the assessed
                // member's next entity state instead.  If this approach results in inadequate assessment
                // fidelity, than remove the return statement and use this target's location update for assessment now.
                return false;
                
            }else if(!isConditionAssessedTeamMember(teamMember)){
                // not a team member being assessed by this condition
                return false;
            }
            
            
            boolean isPlatform = es.getEntityType().getEntityKind() == 1;
            Vector3d articulatedOrientation = null;
            
            /* If the entity is a platform type (vehicle), calculate the angles required for assessment. */
            if (isPlatform) {
            	double psiAngle = 0;
                double thetaAngle = 0;
                double phiAngle = 0;
            	
                List<ArticulationParameter> artParams = es.getArticulationParameters();
                for (ArticulationParameter artParam : artParams) {
                    if (artParam.getParameterTypeDesignator() == ArticulationParameterTypeDesignatorEnum.ARTICULATED_PART) {                  
                        int parameterTypeLowBits = artParam.decodeParameterTypeLowBits();
                        
                        if (parameterTypeLowBits == 11) { // Azimuth
                            float angle = artParam.decodeParameterValue();
                            psiAngle = Double.valueOf(angle).doubleValue();
                        } else if(parameterTypeLowBits == 13) { // Elevation
                            float angle = artParam.decodeParameterValue();
                            thetaAngle = Double.valueOf(angle).doubleValue();
                        } else if (parameterTypeLowBits == 15) { 
                            float angle = artParam.decodeParameterValue();
                            phiAngle = Double.valueOf(angle).doubleValue();
                        }
                    }
                }
                
                articulatedOrientation = new Vector3d(psiAngle, thetaAngle, phiAngle);
            }

            
            final GDC memberLocation = CoordinateUtil.getInstance().convertFromGCCToGDC(es.getLocation());
            double assessedHeading = CoordinateUtil.getInstance().getHeading(es.getOrientation(), memberLocation);
            
            if(isPlatform && articulatedOrientation != null) {
            	
            	/* If the entity that needs to detect the targets is a vehicle with a turret, need to
            	 * add the horizontal angle of the turret to the heading */
            	assessedHeading = assessedHeading + Math.toDegrees(articulatedOrientation.getX());
            }
            
            //
            // Determine if any objects to detect are in the field of view.
            //
            
            synchronized(assessedMemberToObjectsInFOV){

                Map<String, ViewedObjectWrapper> detectObjectsInFOV = assessedMemberToObjectsInFOV.get(teamMember);
                if(detectObjectsInFOV == null){
                    detectObjectsInFOV = new HashMap<>();
                    assessedMemberToObjectsInFOV.put(teamMember, detectObjectsInFOV);
                }
                
                // check detect points 
                for(String pointName : remainingObjectPointRefs.keySet()){
                    
                    Point point = remainingObjectPointRefs.get(pointName);
                    GDC gdcPt = point.toGDC();
                    
                    // check that the detect point is within the max distance to be considered for this assessment
                    double distanceBetweenLocations = es.getLocation().distance(point);
                    if(distanceBetweenLocations > viewMaxDistance){
                        // not allowed to detect something that is out of distance
                        continue;
                    }
                    
                    /* Test the angle to determine whether or not this detect point is
                     * in the field of view of the assessed team member. */
                    final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, gdcPt, assessedHeading);
                    
                    /* If the theta value is
                     * - under the threshold the detect point is in the FOV
                     * - over the threshold the detect point is not in the FOV
                     */
                    if (theta <= fovHalfAngle) {
                        // make sure point is being tracked as in FOV for this assessed member
                        
                        if(!detectObjectsInFOV.containsKey(pointName)){
                            
                            if(logger.isDebugEnabled()){
                                logger.debug(teamMember.getName()+" has detect point '"+pointName+"' in FOV");
                            }
                            ViewedObjectWrapper targetWrapper = new ViewedObjectWrapper(pointName);
                            detectObjectsInFOV.put(pointName, targetWrapper);
                        }                        
                        
                        /* If the theta value is
                         * - under the orient cone threshold the object is in the inner cone
                         * - over the orient cone threshold the object is not in the inner cone
                         */
                        if (detectObjectsInFOV.get(pointName).getTimeOfDetection() == null &&
                                theta <= orientConeHalfAngle) {
                            // point has been oriented to and is in inner cone (for the first time), 
                            // set detection time for assessment coming up.  
                            // This is being used to determine detect of the object right now.
                            detectObjectsInFOV.get(pointName).detected();
                        }
                    }
                    
                }
                
                // check detect team members
                for(String targetMemberName : memberObjectsToDetectAssessmentMap.keySet()){
                    
                    TeamMember<?> memberToDetect = getTeamMember(targetMemberName);
                    AssessedEntityMetadata targetMemberMetadata = memberObjectsToDetectAssessmentMap.get(targetMemberName);
                    
                    // if object is dead, it is no longer an object that should be considered for detection
                    if(targetMemberMetadata.getDamage() == DamageEnum.DESTROYED){
                        
                        if(detectObjectsInFOV.containsKey(memberToDetect.getName())){
                        	
                            // this member to detect had previously entered this assessed member's FOV and now that
                            // member to detect is dead.  The assessed member doesn't have to detect that object anymore so
                            // remove it so it isn't counted toward an assessment calculation for this condition.
                        	detectObjectsInFOV.remove(memberToDetect.getName());
                        }
                        continue;
                    }
                    
                    // check that the detect target is within the view max distance to be considered for this assessment
                    double distanceBetweenLocations = es.getLocation().distance(targetMemberMetadata.getLastGccLocation());
                    if(distanceBetweenLocations > viewMaxDistance){
                        // not allowed to detect something that is out of distance
                        continue;
                    }
                    
                    /* Test the angle to determine whether or not this team member is
                     * in the field of view of the assessed team member. */
                    final double theta = MuzzleFlaggingCondition.getTheta(memberLocation, targetMemberMetadata.getLastGdcLocation(), assessedHeading);
                    
                    /* If the theta value is
                     * - under the threshold the team member is in the FOV
                     * - over the threshold the team member is not in the FOV
                     */
                    if (theta <= fovHalfAngle) {
                        // make sure target is being tracked as in FOV
                        
                        if(!detectObjectsInFOV.containsKey(memberToDetect.getName())){
                            if(logger.isDebugEnabled()){
                                logger.debug(teamMember.getName()+" has team member '"+memberToDetect.getName()+"' in FOV");
                            }
                            ViewedObjectWrapper targetWrapper = new ViewedObjectWrapper(memberToDetect);
                            detectObjectsInFOV.put(memberToDetect.getName(), targetWrapper);
                            
                            // track when each target is detected
                            varsHandler.setVariable(new TeamMemberActor(memberToDetect.getName()), 
                            		ActorVariables.TARGET_DETECTION_TIME, 
                            		System.currentTimeMillis() - startAtTime.getTime());
                        }
                        
                        /* If the theta value is
                         * - under the orient cone threshold the object is in the inner cone
                         * - over the orient cone threshold the object is not in the inner cone
                         */
                        if (detectObjectsInFOV.get(memberToDetect.getName()).getTimeOfDetection() == null &&
                                theta <= orientConeHalfAngle) {
                            // object has been oriented to and is in inner cone (for the first time), 
                            // set detection time for assessment coming up
                            // This is being used to determine detect of the object right now.
                            detectObjectsInFOV.get(memberToDetect.getName()).detected();
                            
                            // track when each target is oriented to
                            varsHandler.setVariable(new TeamMemberActor(memberToDetect.getName()), 
                            		ActorVariables.TARGET_ORIENTATION_TIME, 
                            		System.currentTimeMillis() - startAtTime.getTime());
                            
                            /* Immediately check to see if an assessment should be produced. This is
                             * needed in case the object is detected before the above expectation
                             * maximum time limit */
                            currentViolationTimerTask.run();
                        }
                    }
                }
            }
            
            if(violationTimer == null){
                // the timer is not running and the timer is needed in order to determine if the learner has violated the assigned
                // detection for a long enough time

                violationTimer = new SchedulableTimer(TIMER_NAME);
                
                currentViolationTimerTask = new ViolationTimeTask();

                // run assessment reporting logic twice as often as the time an assessment must be maintained
                violationTimer.scheduleAtFixedRate(currentViolationTimerTask, (long)(maxAboveAssessmentTimeMs) / 2, (long)(maxAboveAssessmentTimeMs) / 2);

                if(logger.isDebugEnabled()) {
                    logger.debug("Started the scheduler for assessing whether targets are being engaged in a timely manner.");
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
        return true;
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
     * Notify the parent concept to this condition that the condition has a new assessment
     * outside the handle training app game state method call (i.e. because the violation timer task fired)
     */
    private void sendAsynchAssessmentNotification(){
        sendAssessmentEvent();
    }
    
    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager) {
        super.setPlacesOfInterestManager(placesOfInterestManager);
        
        // update and convert point references to GDC once for quicker assessment usage as game state is received
        for(Serializable target : input.getObjectsToDetect().getTeamMemberRefOrPointRef()){
            
            if(target instanceof generated.dkf.PointRef){
                generated.dkf.PointRef ptRef = (generated.dkf.PointRef)target;
                PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(ptRef.getValue());
                if(poi instanceof Point){
                    Point pt = (Point)poi;
                    GDC gdc = pt.toGDC();
                    if(gdc != null){
                        originalObjectPointRefs.put(ptRef.getValue(), pt); 
                        remainingObjectPointRefs.put(ptRef.getValue(), pt);
                    }
                }
            }
        }

    }
    
    /**
     * Updates the value of {@link AbstractCondition#assessmentExplanation}
     * based on the current value of the assessments in {@link #assessedMemberToObjectsInFOV}.
     */
    private void updateExplanation() {
        
        synchronized(assessedMemberToObjectsInFOV){
            
            final StringBuilder sb = new StringBuilder();
            Iterator<TeamMember<?>> membersBeingAssessedItr = assessedMemberToObjectsInFOV.keySet().iterator();
            while(membersBeingAssessedItr.hasNext()){
                
                TeamMember<?> memberBeingAssessed = membersBeingAssessedItr.next();
                Iterator<ViewedObjectWrapper> viewedObjectsItr = assessedMemberToObjectsInFOV.get(memberBeingAssessed).values().iterator();

                if(!viewedObjectsItr.hasNext()){
                    // this member is in the larger map but somehow has no object information
                    continue;
                }
                
                while(viewedObjectsItr.hasNext()){
                    
                    ViewedObjectWrapper viewedObjectWrapper = viewedObjectsItr.next();
                    AssessmentLevelEnum assessment = viewedObjectWrapper.getAssessment();
                    if(assessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' detected '").append(viewedObjectWrapper.getNameOfObject()).append("' above standard.\n");
                    }else if(assessment == AssessmentLevelEnum.AT_EXPECTATION){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' detected '").append(viewedObjectWrapper.getNameOfObject()).append("' at standard.\n");
                    }else if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                        sb.append("'").append(memberBeingAssessed.getName()).append("' detected '").append(viewedObjectWrapper.getNameOfObject()).append("' below standard.\n");
                    }
                }

            }
            
            assessmentExplanation = sb.toString();
        }

    }
    
    @Override
    public void start(){
    	super.start();
    	
    	/* Track the time that detection is started for each target */
    	for(String memberName : originalMemberObjectsToDetect) {
    		varsHandler.setVariable(new TeamMemberActor(memberName), ActorVariables.TARGET_UP, System.currentTimeMillis());
    	}
    }

    @Override
    public String toString() {
        return new StringBuilder("[DetectObjectsCondition: ")
                .append(", fovHalfAngle = ").append(fovHalfAngle)
                .append(", orientConeHalfAngle = ").append(orientConeHalfAngle)
                .append(", maxDistance = ").append(viewMaxDistance)
                .append(", maxAboveTimeMs = ").append(maxAboveAssessmentTimeMs)
                .append(", maxAtTimeMs = ").append(maxAtAssessmentTimeMs)
                .append(", originalMembersToDetect = ").append(originalMemberObjectsToDetect)
                .append(", remainingMembersToDetect = ").append(remainingMemberObjectsToDetect)
                .append(", # of original points to detect = ").append(originalObjectPointRefs.size())
                .append(", # of remaining points to detect = ").append(remainingObjectPointRefs.size())
                .append(", ").append(super.toString())
                .append(']').toString();
    }
    
    /**
     * Data model used to track an assessed member's detection of an object
     * that has entered the FOV.
     * 
     * @author mhoffman
     *
     */
    private class ViewedObjectWrapper implements Serializable{

        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
        /** the team member or point to detect in an assessed member's field of view */
        private Serializable teamMemberOrPointName;

        /** 
         * the epoch time when this object entered an assessed
         * members field of view.
         */
        private Long enteredFOVTime = System.currentTimeMillis(); 
        
        /**
         * the epoch time at which the object was detected by the assessed member
         * Will be null if detection hasn't happened yet.
         */
        private Long detectTime = null;
        
        /**
         * the calculated assessment of detected this object by a single assessed member
         */
        private AssessmentLevelEnum calculatedAssessment = null;
        
        /**
         * Set team member as being viewed.
         * 
         * @param teamMember the member being viewed.  Can't be null.
         */
        public ViewedObjectWrapper(TeamMember<?> teamMember){
            setTeamMemberOrPointName(teamMember);
        }
        
        /**
         * Set name of point being viewed.
         * 
         * @param pointName the unique name of a point being viewed.  Can't be null.
         */
        public ViewedObjectWrapper(String pointName){
            setTeamMemberOrPointName(pointName);
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
         * Return the epoch time when this object entered an assessed
         * members field of view.
         * @return epoch time
         */
        public Long getEnteretedFOVTime(){
            return enteredFOVTime;
        }
        
        /**
         * Set the first detect time to the current time.  i.e. used to notify
         * this data model that the object was first detected by an assessed member.
         * Note: this method should only be called once, subsequent calls will be ignored.
         */
        public void detected(){
            if(detectTime != null){
                return;
            }
            detectTime = System.currentTimeMillis();
        }
        
        /**
         * Return the epoch time at which the object was first detected by an assessed member.
         * @return can be null if the object was never detected.
         */
        public Long getTimeOfDetection(){
            return detectTime;
        }
        
        /**
         * Set the assessment level of detecting this object by an assessed member.
         * 
         * @param assessmentLevel can be null
         */
        public void setAssessment(AssessmentLevelEnum assessmentLevel){
            this.calculatedAssessment = assessmentLevel;
        }
        
        /**
         * Return the assessment level of detecting this object by an assessed member.
         * 
         * @return the assessment of detecting this object, can be null.
         */
        public AssessmentLevelEnum getAssessment(){
            return calculatedAssessment;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ViewedObjectWrapper: teamMemberOrPointName = ");
            builder.append(teamMemberOrPointName);
            builder.append(", enteredFOVTime = ");
            builder.append(enteredFOVTime);
            builder.append(", detectTime = ");
            builder.append(detectTime);
            builder.append(", calculatedAssessment = ");
            builder.append(calculatedAssessment);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
    
    /**
     * This class is the timer task which runs at the appropriately scheduled date.  It will
     * cause a change in assessment when an assessed entity has not detected an object after a long enough duration
     * as set by the author.
     *
     * @author mhoffman
     *
     */
    private class ViolationTimeTask extends TimerTask{

        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("detect objects violation timer task fired.");
            }
            
            if(hasCompleted()){
                return;
            }
            
            boolean assessmentChanged = false, explanationNeedsChanging = false;
            
            try{

                StringBuilder debugDesc = new StringBuilder();

                synchronized(assessedMemberToObjectsInFOV){
                    long now = System.currentTimeMillis();
                    int collectiveScoreTotal = 0;  // total assessment points earned across all team members
                    int totalMembersScored = 0;    // number of team members that have non-unknown assessments, don't want to bring down the collective score with unknowns
                    boolean hasAssessment = false; // whether there is at least 1 team member with an assessment
                    
                    Iterator<TeamMember<?>> assessedMemberItr = assessedMemberToObjectsInFOV.keySet().iterator();
                    if(assessedMemberToObjectsInFOV.isEmpty()){
                        return;
                    }
                    
                    while(assessedMemberItr.hasNext()){
                        
                        TeamMember<?> assessedMember = assessedMemberItr.next();
                        Map<String, ViewedObjectWrapper> detectObjectWrappers = assessedMemberToObjectsInFOV.get(assessedMember);
                        if(CollectionUtils.isEmpty(detectObjectWrappers)){
                            // assessment member has no objects in their field of view
                            // Also prevents divide by 0 later
                            if(logger.isDebugEnabled()){
                                debugDesc.append(assessedMember.getName()).append(" has no objects in FOV\n");
                            }
                            continue;
                        }                        
                        
                        int individualScoreTotal = 0;  // the total assessment score for this team member
                        boolean individualHasAssessment = false;  // whether there is at least 1 assessment for this team member
                        
                        Iterator<ViewedObjectWrapper> viewedObjectWrapperItr = detectObjectWrappers.values().iterator();
                        while(viewedObjectWrapperItr.hasNext()){
                            ViewedObjectWrapper viewedObjectWrapper = viewedObjectWrapperItr.next();
                            
                            AssessmentLevelEnum assessment = null;
                            if(viewedObjectWrapper.getAssessment() != null){
                                // already has an assessment on this object for this assessed member
                                assessment = viewedObjectWrapper.getAssessment();
                                
                                if(logger.isDebugEnabled()){
                                    debugDesc.append(assessedMember.getName()).append(" has existing assessment of ").append(assessment).append(" for ").append(viewedObjectWrapper.getNameOfObject()).append("\n");
                                }
                                
                            }else if(viewedObjectWrapper.getTimeOfDetection() != null){
                                // the object was detected but the assessed member has not been assessed yet
                                
                                long detectDuration = now - viewedObjectWrapper.getTimeOfDetection();
                                if(detectDuration < maxAboveAssessmentTimeMs){
                                    // this assessed member receives above expectation for this object
                                    assessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
                                }else if(detectDuration < maxAtAssessmentTimeMs){
                                    // this assessed member receives at expectation for this object
                                    assessment = AssessmentLevelEnum.AT_EXPECTATION;
                                }else{
                                    // this assessed member receives below expectation for this object
                                    assessment = AssessmentLevelEnum.BELOW_EXPECTATION;                                    
                                }
                                
                                if(logger.isDebugEnabled()){
                                    debugDesc.append(assessedMember.getName()).append(" has time of detection assessment of ").append(assessment).append(" for ").append(viewedObjectWrapper.getNameOfObject()).append("\n");
                                }
                                
                                explanationNeedsChanging = true;
                            }else{
                                // the object is in the FOV but has not been detected yet
                                
                                long inFOVDuration = now - viewedObjectWrapper.getEnteretedFOVTime();
                                if(inFOVDuration > maxAtAssessmentTimeMs){
                                    // this assessed member receives below expectation for this object
                                    assessment = AssessmentLevelEnum.BELOW_EXPECTATION;  
                                    
                                    explanationNeedsChanging = true;
                                    
                                    if(logger.isDebugEnabled()){
                                        debugDesc.append(assessedMember.getName()).append(" has failed to detect, using assessment of ").append(assessment).append(" for ").append(viewedObjectWrapper.getNameOfObject()).append("\n");
                                    }
                                }
                            }
                            
                            if(assessment != null){
                                hasAssessment = true;
                                individualHasAssessment = true;
                                
                                // optimize - no longer check if an assessed member has detected this object
                                remainingObjectPointRefs.remove(viewedObjectWrapper.getNameOfObject());
                                remainingMemberObjectsToDetect.remove(viewedObjectWrapper.getNameOfObject());
                            }
                            
                            // update in case it has changed, really should only be null->null,  null->not null, X assessment -> X assessment
                            // based on above logic
                            viewedObjectWrapper.setAssessment(assessment);
                            
                            if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                                individualScoreTotal += DefaultGradeMetric.BELOW_EXPECTATION_SCORE;                                
                            }else if(assessment == AssessmentLevelEnum.AT_EXPECTATION){
                                individualScoreTotal += DefaultGradeMetric.AT_EXPECTATION_SCORE;
                            }else if(assessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
                                individualScoreTotal += DefaultGradeMetric.ABOVE_EXPECTATION_SCORE;
                            }

                        } // end while on individual assessed member's viewed objects
                        
                        if(individualHasAssessment){
                            if(logger.isDebugEnabled()){
                                debugDesc.append(assessedMember.getName()).append(" will be included in count for number of assessed members\n");
                            }
                            totalMembersScored++;
                        }
                        
                        // get average score for this assessed member
                        double individualScoreAvg = individualScoreTotal / detectObjectWrappers.size();
                        if(individualScoreAvg < DefaultGradeMetric.BELOW_EXPECTATION_UPPER_THRESHOLD){
                            collectiveScoreTotal += DefaultGradeMetric.BELOW_EXPECTATION_SCORE;                            
                        }else if(individualScoreAvg < DefaultGradeMetric.AT_EXPECTATION_UPPER_THRESHOLD){
                            collectiveScoreTotal += DefaultGradeMetric.AT_EXPECTATION_SCORE;
                        }else{
                            collectiveScoreTotal += DefaultGradeMetric.ABOVE_EXPECTATION_SCORE;
                        }
                        
                        if(logger.isDebugEnabled()){
                            debugDesc.append("Calculated collective score now = ").append(collectiveScoreTotal).append(", just used individual score = ").append(individualScoreAvg).append(" from ").append(individualScoreTotal).append(" / ").append(detectObjectWrappers.size()).append("\n");
                        }
                    } // end while on collective assessed members
                    
                    AssessmentLevelEnum newAssessment = null;
                    if(hasAssessment){
                        // calculate single assessment for this condition
                        double collectiveScoreAvg = collectiveScoreTotal/(double)totalMembersScored;
                        if(collectiveScoreAvg < DefaultGradeMetric.BELOW_EXPECTATION_UPPER_THRESHOLD){
                            newAssessment = AssessmentLevelEnum.BELOW_EXPECTATION;
                        }else if(collectiveScoreAvg < DefaultGradeMetric.AT_EXPECTATION_UPPER_THRESHOLD){
                            newAssessment = AssessmentLevelEnum.AT_EXPECTATION;
                        }else{
                            newAssessment = AssessmentLevelEnum.ABOVE_EXPECTATION; 
                        }
                        
                        if(logger.isDebugEnabled()){
                            debugDesc.append("Calculated assessment is ").append(newAssessment).append(" with avg score = ").append(collectiveScoreAvg).append(" from ").append(collectiveScoreTotal).append(" / ").append(totalMembersScored).append("\n");
                        }
                    }
                    
                    if(newAssessment != null && newAssessment != getAssessment()){
                        if(logger.isDebugEnabled()){
                            debugDesc.append("Setting new assessment to ").append(newAssessment).append("\n");
                        }
                        assessmentChanged = true;
                        updateAssessment(newAssessment);
                        
                        if(newAssessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                            // count number of At/Above to Below, and duration of condition in Below state
                            scoringEventStarted();
                        }else{
                            // stop timer for Below state
                            scoringEventEnded();
                        }
                    }
                }
                
                if(explanationNeedsChanging){
                    updateExplanation();  //update assessment explanation
                    sendAsynchAssessmentNotification();  //notify parent concept that something changed with this condition
                }else if(assessmentChanged){
                    sendAsynchAssessmentNotification(); //notify parent concept that something changed with this condition
                }
                
                if(remainingObjectPointRefs.isEmpty() && remainingMemberObjectsToDetect.isEmpty()){
                    // there is nothing more to detect
                    if(logger.isDebugEnabled()){
                        debugDesc.append("All objects detect, condition completed.");
                    }
                    conditionCompleted();
                }
                
                if(logger.isDebugEnabled()){
                    logger.debug(debugDesc.toString());
                }
            }catch(Throwable t){
                logger.error("An error happened while checking for failed to detect objects violation", t);
            }

        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("[DetectObjectsCondition-ViolationTimerTask: ");
            sb.append("]");

            return sb.toString();
        }
    }
}
