/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3d;

import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.AssessMyLocationTutorAction;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.domain.knowledge.common.Area;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.net.api.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.AreaRef;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.PointRef;

/**
 * This condition checks whether an entity is avoiding one or more locations.
 *
 * @author mhoffman
 *
 */
public class AvoidLocationCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AvoidLocationCondition.class);

    /** the default assessment for this condition when no location based assessment has taken place yet */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;

    /** Used to evaluate the condition based on current coordinates (used only for GeoLocation messages). */
    private Point3d cachedPoint = new Point3d();

    /**
     * contains the types of GIFT messages this condition needs in order to
     * provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;

    static {
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
        simulationInterests.add(MessageTypeEnum.GEOLOCATION);
        simulationInterests.add(MessageTypeEnum.LEARNER_TUTOR_ACTION);
    }

    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.ViolationTime.class);
    }

    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION =
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "AvoidLocation.GIFT Domain condition description.html"), "Avoid/Reach location");

    /** the authored parameters for this condition */
    private generated.dkf.AvoidLocationCondition avoidLocationInput = null;

    /** Used to test incoming area coordinates. */
    private Point3d areaPointI = new Point3d();
    private Point3d areaPointJ = new Point3d();

    /**
     * mapping of the team members being assessed by this condition to the last known point received by this condition.
     * This will only be used if this condition is configured to require a learner action to be used when the learner
     * feels they have reached the proper location.
     * Note: the key can be null when Geo Location is being received, since Geo Location doesn't have entity identifiers to
     *       map to team members
     */
    private Map<TeamMember<?>, Point3d> lastKnownPtMap = new HashMap<>(0);

    /** the learner actions needed to be shown to the learner for this condition to assess the learner */
    private static final Set<generated.dkf.LearnerActionEnumType> LEARNER_ACTIONS = new HashSet<>(2);
    static{
        LEARNER_ACTIONS.add(generated.dkf.LearnerActionEnumType.ASSESS_MY_LOCATION);
    }

    /**
     * Default constructor - required for authoring logic
     */
    public AvoidLocationCondition(){ }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param avoidLocation - dkf content for this condition
     */
    public AvoidLocationCondition(generated.dkf.AvoidLocationCondition avoidLocation) {

        this.avoidLocationInput = avoidLocation;

        if(avoidLocationInput.getPointRef().isEmpty() && avoidLocationInput.getAreaRef().isEmpty()){
            throw new IllegalArgumentException("There are no locations to avoid");
        }

        //save any authored real time assessment rules
        if(avoidLocationInput.getRealTimeAssessmentRules() != null){
            addRealTimeAssessmentRules(avoidLocationInput.getRealTimeAssessmentRules());
        }

        if(avoidLocationInput.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(avoidLocationInput.getTeamMemberRefs());
        }

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //set the initial assessment to the authored real time assessment value
            updateAssessment(authoredLevel);
        }else{
            updateAssessment(DEFAULT_ASSESSMENT);
        }
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        boolean reachedLocations = false;
        final int prevViolatorCount = getViolatorSize();
        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {

            EntityState entityState = (EntityState) message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if(teamMember == null){
                return false;
            }

            if(avoidLocationInput.isRequireLearnerAction() == Boolean.TRUE){
                //save location for this team member but don't assess until the learner action is pressed
                lastKnownPtMap.put(teamMember, entityState.getLocation());
                return false;
            }

            AssessmentLevelEnum level = null;

            if (evaluateCondition(entityState.getLocation())) {
                //found to be within the specified distance of the location to avoid
                
                if(logger.isTraceEnabled()){
                    logger.trace(teamMember.getName()+" is violating avoid location");
                }

                reachedLocations = true;
                addViolator(teamMember, entityState.getEntityID());
                level = handleViolation(teamMember);

            } else{
                //make sure if this entity was in violation that it is no longer in the violation collection
                removeViolator(entityState.getEntityID());
                level = handleSuccess(teamMember);

                if(getViolatorSize() == 0){
                    // the current entity for this entity state message is not violating this condition, nor
                    // is any other entity at the moment
                    // need to notify the assessment(s) tracking the group of team members 
                    // being assessed by this condition
                    handleSuccess();
                }
            }

            if (level != null || prevViolatorCount != getViolatorSize()) {
                setAssessmentExplanation(reachedLocations);
                updateAssessment(level);
                return true;
            }

        } else if (message.getMessageType() == MessageTypeEnum.GEOLOCATION) {

            Geolocation geolocation = (Geolocation) message.getPayload();

            AssessmentLevelEnum level = null;

            CoordinateUtil.getInstance().convertIntoPoint(geolocation.getCoordinates(), cachedPoint);

            if(avoidLocationInput.isRequireLearnerAction() == Boolean.TRUE){
                //save location for this team member but don't assess until the learner action is pressed
                lastKnownPtMap.put(null, cachedPoint);
                return false;
            }

            if (evaluateCondition(cachedPoint)) {
                //found to be within the specified distance of the location to avoid

                addViolator(null, null);
                reachedLocations = true;
                level = handleViolation(null);

            } else{
                removeViolator(null);
                level = handleSuccess();
            }

            if (level != null || prevViolatorCount != getViolatorSize()) {
                setAssessmentExplanation(reachedLocations);
                updateAssessment(level);
                return true;
            }
        } else if(message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION){

            LearnerTutorAction action = (LearnerTutorAction)message.getPayload();
            AbstractLearnerTutorAction actionData = action.getAction();

            if(actionData instanceof AssessMyLocationTutorAction){
                //time to perform the assessment on all known team members

                AssessmentLevelEnum level = null;

                for(TeamMember<?> teamMember : lastKnownPtMap.keySet()){

                    Point3d point = lastKnownPtMap.get(teamMember);
                    if (evaluateCondition(point)) {
                        // found to be within the specified distance of the location to avoid

                        reachedLocations = true;
                        level = handleViolation(teamMember);
                        break;  //currently if anyone is in violation than stop checking, in the future this could be more sophisticated

                    } else{
                        level = handleSuccess(teamMember);
                    }
                }

                if (level != null || prevViolatorCount != getViolatorSize()) {
                    setAssessmentExplanation(reachedLocations);
                    updateAssessment(level);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators){

        if(removedViolators != null && !removedViolators.isEmpty()){
            
            // update scoring for each team member
            handleSuccess(removedViolators.toArray(new TeamMember<?>[removedViolators.size()]));
            
            if(getViolatorSize() == 0){
                //no more violators - update real time assessment
                
                AssessmentLevelEnum level = handleSuccess();
                if(level != null){
                    setAssessmentExplanation(false);
                    updateAssessment(level);
                    if(conditionActionInterface != null){
                        conditionActionInterface.conditionAssessmentCreated(this);
                    }
                }
            }


        }
    }

    /**
     * Handle the case where the learner is avoiding the location(s) at this moment.
     *
     * @param teamMembersNotViolating The {@link TeamMember<?>}s who are
     * not violating this condition. Use no value to update the group assessment
     * of team members being assessed by this condition instance.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleSuccess(TeamMember<?>... teamMembersNotViolating){

        AssessmentLevelEnum level = null;

        //its ok to call this repeatedly w/o starting an event
        scoringEventEnded(teamMembersNotViolating);

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //one of the authored assessment rules has been satisfied

            if(getAssessment() != authoredLevel){
                //this is a new assessment, don't want to keep sending old, non-changed assessment
                level = authoredLevel;
            }
        }else if(getViolatorSize() == 0){
            //no authored assessment rules AND no current violators

            if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                //found to be far enough away from the location AND was not previously, therefore the level has changed

                level = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }

        return level;
    }

    /**
     * Handle the case where the learner is NOT avoiding the location(s) at this moment.
     *
     * @param teamMember the team member violating this condition.  Can be null for single learner situations
     * like mobile device geo locations.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleViolation(TeamMember<?> teamMember){

        AssessmentLevelEnum level = null;

        if(isScoringEventActive(teamMember)){
            //make sure the level hasn't changed due to an ongoing time of violation

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null && getAssessment() != authoredLevel){
                //only set the level if the assessment is different than the current assessment to indicate
                //a new assessment has taken place and needs to be communicated throughout gift
                level = authoredLevel;
            }
        }else{
            //this is a new event, not a continuation of an ongoing event

            scoringEventStarted(teamMember);

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null){
                //one of the authored assessment rules has been satisfied
                level = authoredLevel;
            }else{
                if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
                    //not currently violating this condition, therefore treat this as a new violation
                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                }
            }
        }

        return level;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {

        if(avoidLocationInput != null && avoidLocationInput.isRequireLearnerAction() == Boolean.TRUE){
            //this avoid location condition instance needs assess my location learner action
            return LEARNER_ACTIONS;
        }else{
            return null;
        }
    }

    @Override
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        super.setPlacesOfInterestManager(placesOfInterestManager);

        // Avoid location calculations for areas uses 2D checks, therefore the coordinates specified
        // in an area place of interest must not be GCC type (i.e. the 3rd coordinate must be elevation)
        // We are converting the dkf places of interest that may be GCC type into a GDC type.
        for(AreaRef areaRef : avoidLocationInput.getAreaRef()){

            PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(areaRef.getValue());
            if(poi == null){
                continue;
            }else if(!(poi instanceof Area)){
                continue;
            }

            Area area = (Area)poi;
            for(int x=0; x < area.getPoints().size(); x++) {

                AbstractCoordinate coordinate = area.getPoints().get(x);
                if(coordinate == null){
                    continue;
                }

                if (coordinate instanceof GCC) {

                    GCC coordGCC = (GCC)coordinate;

                    // Convert the GCC coordinate to GDC.
                    GDC coordGDC = CoordinateUtil.getInstance().convertToGDC(coordGCC);

                    // Replace the area point location with the new GDC coordinate
                    // This needs to preserve  the order of the points within the Area.
                    area.getPoints().set(x,  coordGDC);
                }

            }
        }
    }

    /**
     * Return whether the current learner location provided is within any of the locations
     * for this condition.
     *
     * @param currentLocation the current learner location to compare against the condition's locations.
     * If the condition is configured with area places of interest than it is assumed that the currentLocation
     * Z value is elevation because the within area check only uses X and Y.
     * @return true if the learner location is near any of the condition's locations to avoid.
     */
    private boolean evaluateCondition(Point3d currentLocation){

        if(currentLocation == null){
            return false;
        }else if(placesOfInterestManager == null){
            return false;
        }

        if(avoidLocationInput != null){

            // Check points
            for(PointRef pointRef : avoidLocationInput.getPointRef()){

                if(pointRef == null){
                    continue;
                }

                PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(pointRef.getValue());
                if(poi == null){
                    continue;
                }else if(!(poi instanceof Point)){
                    continue;
                }

                Point point = (Point)poi;

                BigDecimal bigDecimal = pointRef.getDistance();
                double distanceThreshold = 0.0d;
                if(bigDecimal != null && bigDecimal.doubleValue() >= 0.0){
                    distanceThreshold = bigDecimal.doubleValue();
                }

                if(reachedLocation(point, distanceThreshold, currentLocation)){
                    //within this point threshold, no need continue checking
                    return true;
                }
            }

            // Check areas
            for(AreaRef areaRef : avoidLocationInput.getAreaRef()){

                if(areaRef == null){
                    continue;
                }

                PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(areaRef.getValue());
                if(poi == null){
                    continue;
                }else if(!(poi instanceof Area)){
                    continue;
                }

                Area area = (Area)poi;
                if(isWithinArea(currentLocation.x, currentLocation.y, area)){
                    //within this area, no need to continue checking
                    return true;
                }

            }
        }

        return false;

    }

    /**
     * Return whether the entity location has reached the point specified.
     *
     * @param candidatePt the authored point to check the entity state against. Can't be null.
     * Note: when the third value of this point is 0 a 2D check is performed.
     * @param pointProximity how close does the entity state location need to be to the point.  Should be greater than 0.
     * @param currentEntityStateLocation the current entity location to use for this check. Can't be null.
     * @return true if the entity location has reached the point specified.
     */
    private boolean reachedLocation(Point3d candidatePt, double pointProximity, Point3d currentEntityStateLocation){

        boolean reached = false;
        if(candidatePt.getZ() == 0){
            //perform 2D calculation - this is here to handle the situation where the location authored was
            //                         created using gift wrap for Unity training application.  In this case
            //                         the third value (elevation) in the Unity AGL coordinate will always be zero.
            //                         However when running the Unity scenario, the entity state messages can
            //                         contain non-zero elevation values.
            double distance2D = Math.sqrt(Math.pow(candidatePt.getX() - currentEntityStateLocation.getX(), 2) + Math.pow(candidatePt.getY() - currentEntityStateLocation.getY(), 2));
            if(distance2D <= pointProximity){
                reached = true;
            }
        }else if(currentEntityStateLocation.distance(candidatePt) <= pointProximity){
            //perform 3D calculation
            reached = true;
        }

        return reached;
    }

    /**
     * Returns whether the specified (x,y) coordinate is within the Area specified.
     * See: https://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon
     * The method looks at a "ray" that starts at the tested spot and extends to infinity to the right side of the X axis.
     * For each polygon segment, it checks if the ray crosses it. If the total number of segment crossing is odd then the
     * tested point is considered inside the polygon, otherwise - it is outside.
     *
     * @param x - The x-axis coordinate to check
     * @param y - The y-axis coordinate to check
     * @param area - The targeted area to check whether the 2D coordinate is within
     * @return true if the position is located inside the area
     */
    private boolean isWithinArea(double x, double y, Area area) {
        boolean result = false;  // 0 points checked, 0 is even, therefore outside the polygon
        List<AbstractCoordinate> points = area.getPoints();
        int i;
        int j;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {

            AbstractCoordinate coordI = points.get(i);
            AbstractCoordinate coordJ = points.get(j);


            // Area check is a 2D check (ignores Z/elevation).
            CoordinateUtil.getInstance().convertIntoPoint(coordI, areaPointI);
            CoordinateUtil.getInstance().convertIntoPoint(coordJ, areaPointJ);
            double point1X = areaPointI.x;
            double point1Y = areaPointI.y;
            double point2X = areaPointJ.x;
            double point2Y = areaPointJ.y;
            if ((point1Y > y) != (point2Y > y) &&
                    (x < (point2X - point1X) * (y - point1Y) / (point2Y - point1Y) + point1X)) {
                result = !result;
            }
        }
        return result;
    }

    /**
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @param reachedLocations whether the explanation represents that one or more of the assessed learners is at
     * one or more of the specified locations.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(boolean reachedLocations){

        //update assessment explanation
        Set<TeamMember<?>> violators = buildViolatorsInfo();
        boolean changed = false;
        if(violators.isEmpty()){
            changed = assessmentExplanation != null;
            assessmentExplanation = null;
        }else{
            StringBuilder assessmentExplanationBuilder = new StringBuilder();
            Iterator<TeamMember<?>> itr = violators.iterator();
            assessmentExplanationBuilder.append("{");
            boolean mobileUser = false;
            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                if(violator == null){
                    assessmentExplanationBuilder.setLength(0);  //clear the { character
                    mobileUser = true;
                    break;
                }else{
                    assessmentExplanationBuilder.append(violator.getName());
                    if(itr.hasNext()){
                        assessmentExplanationBuilder.append(", ");
                    }
                }
            }

            if(mobileUser){

                if(reachedLocations){
                    assessmentExplanationBuilder.append("Reached the specified location(s)");
                }else{
                    assessmentExplanationBuilder.append("Not near the specified location(s)");
                }
            }else{
                if(violators.size() > 1){
                    assessmentExplanationBuilder.append("} have reached the specified location(s).");
                }else{
                    assessmentExplanationBuilder.append("} has reached the specified location(s).");
                }
            }

            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }

        return changed;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[AvoidLocationCondition: ");
        sb.append(super.toString());

        sb.append(", locations = {");
        if(avoidLocationInput != null){

            for(AreaRef areaRef : avoidLocationInput.getAreaRef()){

                if(areaRef == null){
                    continue;
                }

                sb.append("\n [").append(areaRef.getValue()).append(" (area)]");
            }

            for(PointRef pointRef : avoidLocationInput.getPointRef()){

                if(pointRef == null){
                    continue;
                }

                if(placesOfInterestManager == null){
                    sb.append("\n [").append(pointRef.getValue());
                    sb.append(", distance = ").append(pointRef.getDistance()).append("]");
                }else{
                    PlaceOfInterestInterface poi = placesOfInterestManager.getPlacesOfInterest(pointRef.getValue());
                    if(poi == null){
                        continue;
                    }else if(!(poi instanceof Point)){
                        continue;
                    }

                    Point point = (Point)poi;
                    sb.append("\n [").append(pointRef.getValue());
                    sb.append(", ").append(point);
                    sb.append(", distance = ").append(pointRef.getDistance()).append("]");
                }

            }
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }

    @Override
    public boolean canComplete() {
        return false;
    }
}
