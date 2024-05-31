/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.AutoTutorConditionInput;
import generated.dkf.AutoTutorSKO;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.BooleanEnum;
import generated.dkf.Checkpoint;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.CorridorPostureCondition;
import generated.dkf.DetectObjectsCondition;
import generated.dkf.EliminateHostilesCondition;
import generated.dkf.EnterAreaCondition;
import generated.dkf.Entities;
import generated.dkf.Entrance;
import generated.dkf.ExcavatorComponentEnum;
import generated.dkf.ExternalAttributeEnumType;
import generated.dkf.GenericConditionInput;
import generated.dkf.HasMovedExcavatorComponentInput;
import generated.dkf.HasMovedExcavatorComponentInput.Component;
import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.LocalSKO;
import generated.dkf.Point;
import generated.dkf.PointRef;
import generated.dkf.Pois;
import generated.dkf.Postures;
import generated.dkf.PowerPointDwellCondition;
import generated.dkf.RequestExternalAttributeCondition;
import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.TimerConditionInput;
import generated.dkf.Wcs;
import generated.dkf.WeaponControlStatusEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A cache class that is used to keep singleton instances for each of the
 * condition input classes.
 *
 * @author tflowers
 *
 */
public class ConditionInputCache {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConditionInputCache.class.getName());

    /**
     * An enumeration of each of the input condition types.
     *
     * @author tflowers
     *
     */
    public enum ConditionType {
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.ApplicationCompletedCondition ApplicationCompletedCondition} */
        APPLICATION_COMPLETED_CONDITION("domain.knowledge.condition.ApplicationCompletedCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.AssignedSectorCondition AssignedSectorCondition} */
        ASSIGNED_SECTOR_CONDITION("domain.knowledge.condition.AssignedSectorCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterfaceCondition AutoTutorWebServiceInterfaceCondition} */
        AUTO_TUTOR_CONDITION("domain.knowledge.condition.autotutor.AutoTutorWebServiceInterfaceCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.AvoidLocationCondition AvoidLocationCondition} */
        AVOID_LOCATION_CONDITION("domain.knowledge.condition.AvoidLocationCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.CheckpointPaceCondition CheckpointPaceCondition} */
        CHECKPOINT_PACE_CONDITION("domain.knowledge.condition.CheckpointPaceCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.CheckpointProgressCondition CheckpointProgressCondition} */
        CHECKPOINT_PROGRESS_CONDITION("domain.knowledge.condition.CheckpointProgressCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition CorridorBoundaryCondition} */
        CORRIDOR_BOUNDARY_CONDITION("domain.knowledge.condition.CorridorBoundaryCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.CorridorPostureCondition CorridorPostureCondition} */
        CORRIDOR_POSTURE_CONDITION("domain.knowledge.condition.CorridorPostureCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.DetectObjectsCondition DetectObjectsCondition} */
        DETECT_OBJECTS_CONDITION("domain.knowledge.condition.DetectObjectsCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.EliminateHostilesCondition EliminateHostilesCondition} */
        ELIMINATE_HOSTILES_CONDITION("domain.knowledge.condition.EliminateHostilesCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.EngageTargetsCondition EngageTargetsCondition} */
        ENGAGE_TARGETS_CONDITION("domain.knowledge.condition.EngageTargetsCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.EnterAreaCondition EnterAreaCondition} */
        ENTER_AREA_CONDITION("domain.knowledge.condition.EnterAreaCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.ExplosiveHazardSpotReportCondition ExplosiveHazardSpotReportCondition} */
        EXPLOSIVE_HAZARD_SPOT_REPORT_CONDITION("domain.knowledge.condition.ExplosiveHazardSpotReportCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.FireTeamRateOfFireCondition FireTeamRateOfFireCondition} */
        FIRE_TEAM_RATE_OF_FIRE_CONDITION("domain.knowledge.condition.FireTeamRateOfFireCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.HaltCondition HaltCondition} */
        HALT_CONDITION("domain.knowledge.condition.HaltCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.HealthCondition HealthCondition} */
        HEALTH_CONDITION("domain.knowledge.condition.HealthCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.HasCollidedCondition HasCollidedCondition} */
        HAS_COLLIDED_CONDITION("domain.knowledge.condition.HasCollidedCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.HasMovedExcavatorComponentCondition HasMovedExcavatorComponentCondition} */
        HAS_MOVED_EXCAVATOR_COMPONENT_CONDITION("domain.knowledge.condition.HasMovedExcavatorComponentCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.IdentifyPOIsCondition IdentifyPOIsCondition} */
        IDENTIFY_POIS_CONDITION("domain.knowledge.condition.IdentifyPOIsCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.LifeformTargetAccuracyCondition LifeformTargetAccuracyCondition} */
        LIFEFORM_TARGET_ACCURACY_CONDITION("domain.knowledge.condition.LifeformTargetAccuracyCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.MarksmanshipPrecisionCondition MarksmanshipPrecisionCondition} */
        MARKSMANSHIP_PRECISION_CONDITION("domain.knowledge.condition.MarksmanshipPrecisionCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.MarksmanshipSessionCompleteCondition MarksmanshipSessionCompleteCondition} */
        MARKSMANSHIP_SESSION_COMPLETE_CONDITION("domain.knowledge.condition.MarksmanshipSessionCompleteCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.MuzzleFlaggingCondition MuzzleFlaggingCondition} */
        MUZZLE_FLAGGING_CONDITION("domain.knowledge.condition.MuzzleFlaggingCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.NegligentDischargeCondition NegligentDischargeCondition} */
        NEGLIGENT_DISCHARGE_CONDITION("domain.knowledge.condition.NegligentDischargeCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.NineLineReportCondition NineLineReportCondition} */
        NINE_LINE_REPORT_CONDITION("domain.knowledge.condition.NineLineReportCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.NumberOfShotsFiredCondition NumberOfShotsFiredCondition} */
        NUMBER_OF_SHOTS_FIRED_CONDITION("domain.knowledge.condition.NumberOfShotsFiredCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.ObservedAssessmentCondition ObservedAssessmentCondition} */
        OBSERVED_ASSESSMENT_CONDITION("domain.knowledge.condition.ObservedAssessmentCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.PaceCountCondition PaceCountCondition} */
        PACE_COUNT_CONDITION("domain.knowledge.condition.PaceCountCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.PowerPointOverDwellCondition PowerPointOverDwellCondition} */
        PPT_OVER_DWELL_CONDITION("domain.knowledge.condition.PowerPointOverDwellCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.PowerPointUnderDwellCondition PowerPointUnderDwellCondition} */
        PPT_UNDER_DWELL_CONDITION("domain.knowledge.condition.PowerPointUnderDwellCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.RequestExternalAttributeCondition RequestExternalAttributeCondition} */
        REQUEST_EXTERNAL_ATTRIBUTE_CONDITION("domain.knowledge.condition.RequestExternalAttributeCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.RulesOfEngagementCondition RulesOfEngagementCondition} */
        RULES_OF_ENGAGEMENT_CONDITION("domain.knowledge.condition.RulesOfEngagementCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.simile.SIMILEInterfaceCondition SIMILEInterfaceCondition} */
        SIMILE_CONDITION("domain.knowledge.condition.simile.SIMILEInterfaceCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.SimpleSurveyAssessmentCondition SimpleSurveyAssessmentCondition} */
        SIMPLE_SURVEY_ASSESSMENT_CONDITION("domain.knowledge.condition.SimpleSurveyAssessmentCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.SpacingCondition SpacingCondition} */
        SPACING_CONDITION("domain.knowledge.condition.SpacingCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition SpeedLimitCondition} */
        SPEED_LIMIT_CONDITION("domain.knowledge.condition.SpeedLimitCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.SpotReportCondition SpotReportCondition} */
        SPOT_REPORT_CONDITION("domain.knowledge.condition.SpotReportCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.UseRadioCondition UseRadioCondition} */
        USE_RADIO_CONDITION("domain.knowledge.condition.UseRadioCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.StringMatchingExampleCondition StringMatchingExampleCondition} */
        STRING_MATCHING_EXAMPLE_CONDITION("domain.knowledge.condition.StringMatchingExampleCondition"),
        /** The {@link ConditionType} for {@link mil.arl.gift.domain.knowledge.condition.TimerCondition TimerCondition} */
        TIMER_CONDITION("domain.knowledge.condition.TimerCondition");

        /** The name of the condition type */
        private String name;

        /**
         * Constructs the {@link ConditionType} with the given name.
         *
         * @param name The name of the {@link ConditionType} to construct.
         */
        private ConditionType(String name) {
            if (name == null) {
                throw new IllegalArgumentException("The parameter 'name' cannot be null.");
            }

            this.name = name;
        }

        /**
         * Gets the {@link ConditionType} for a corresponding name.
         *
         * @param name The name of the {@link ConditionType} to return. If no
         *        {@link ConditionType} is found a
         *        {@link UnsupportedOperationException} is thrown.
         * @return The {@link ConditionType} with the given name.
         * @throws UnsupportedOperationException if the condition type is unknown
         */
        public static ConditionType getTypeFromString(String name) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("ConditionType.getTypeFromString(" + name + ")");
            }

            for (ConditionType cType : ConditionType.values()) {
                if (StringUtils.equals(cType.name, name)) {
                    return cType;
                }
            }

            throw new UnsupportedOperationException(
                    "The string '" + name + "' could not be converted to a ConditionType");
        }

        /**
         * Returns the name of the given {@link ConditionType}.
         *
         * @param type The {@link ConditionType} to return a name for.
         * @return The name of the provided {@link ConditionType}.
         */
        public static String getNameFromType(ConditionType type) {
            return type.name;
        }

        /**
         * Creates an initialized instance of a XML condition input of the provided
         * {@link ConditionType}.
         *
         * @param cType The {@link ConditionType} of the condition input to create.
         * @return The initialized condition of the provided {@link ConditionType}.
         * @throws UnsupportedOperationException if the condition type is unknown
         */
        public static Serializable createInstance(ConditionType cType) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("ConditionType.createInstance(" + cType + ")");
            }

            if (cType == null) {
                throw new IllegalArgumentException("The parameter 'cType' cannot be null.");
            }

            switch (cType) {
            case APPLICATION_COMPLETED_CONDITION:
                return new generated.dkf.ApplicationCompletedCondition();
            case ASSIGNED_SECTOR_CONDITION:
                return new generated.dkf.AssignedSectorCondition();
            case AUTO_TUTOR_CONDITION:
                AutoTutorConditionInput autoTutorConditionInput = new AutoTutorConditionInput();
                AutoTutorSKO sko = new AutoTutorSKO();
                sko.setScript(new LocalSKO());
                autoTutorConditionInput.setAutoTutorSKO(sko);
                return autoTutorConditionInput;
            case AVOID_LOCATION_CONDITION:
                List<Serializable> availablePlacesOfInterest = ScenarioClientUtility.getUnmodifiablePlacesOfInterestList();

                PointRef waypointRef = new PointRef();
                waypointRef.setDistance(BigDecimal.ZERO);
                if (!availablePlacesOfInterest.isEmpty()) {

                    for(Serializable placeOfInterest : availablePlacesOfInterest){

                        if(placeOfInterest instanceof Point){

                            Point pointStart = (Point) placeOfInterest;
                            waypointRef.setValue(pointStart.getName());

                            break;
                        }
                    }
                }

                AvoidLocationCondition avoidLocationCondition = new AvoidLocationCondition();
                avoidLocationCondition.getPointRef().add(waypointRef);
                return avoidLocationCondition;
            case CHECKPOINT_PACE_CONDITION:
                return new generated.dkf.CheckpointPaceCondition();
            case CHECKPOINT_PROGRESS_CONDITION:
                return new generated.dkf.CheckpointProgressCondition();
            case CORRIDOR_BOUNDARY_CONDITION:
                CorridorBoundaryCondition corridorBoundaryCondition = new CorridorBoundaryCondition();
                corridorBoundaryCondition.setBufferWidthPercent(BigDecimal.ZERO);
                return corridorBoundaryCondition;
            case CORRIDOR_POSTURE_CONDITION:
                CorridorPostureCondition corridorPostureCondition = new CorridorPostureCondition();
                corridorPostureCondition.setPostures(new Postures());
                return corridorPostureCondition;
            case DETECT_OBJECTS_CONDITION:
                DetectObjectsCondition detectObjectsCondition = new DetectObjectsCondition();
                return detectObjectsCondition;
            case ELIMINATE_HOSTILES_CONDITION:
                EliminateHostilesCondition eliminateHostilesCondition = new EliminateHostilesCondition();
                eliminateHostilesCondition.setEntities(new Entities());
                return eliminateHostilesCondition;
            case ENGAGE_TARGETS_CONDITION:
                return new generated.dkf.EngageTargetsCondition();
            case ENTER_AREA_CONDITION:
                return new generated.dkf.EnterAreaCondition();
            case EXPLOSIVE_HAZARD_SPOT_REPORT_CONDITION:
                return new generated.dkf.ExplosiveHazardSpotReportCondition();
            case FIRE_TEAM_RATE_OF_FIRE_CONDITION:
                return new generated.dkf.FireTeamRateOfFireCondition();
            case HALT_CONDITION:
                return new generated.dkf.HaltConditionInput();
            case HEALTH_CONDITION:
                return new generated.dkf.HealthConditionInput();
            case HAS_COLLIDED_CONDITION:
                return new generated.dkf.NoConditionInput();
            case HAS_MOVED_EXCAVATOR_COMPONENT_CONDITION:
                HasMovedExcavatorComponentInput excavatorCondition = new HasMovedExcavatorComponentInput();
                Component component = new Component();
                component.setComponentType(ExcavatorComponentEnum.BUCKET);
                excavatorCondition.getComponent().add(component);
                return excavatorCondition;
            case IDENTIFY_POIS_CONDITION:
                IdentifyPOIsCondition identifyPOIsCondition = new IdentifyPOIsCondition();
                identifyPOIsCondition.setPois(new Pois());
                return identifyPOIsCondition;
            case LIFEFORM_TARGET_ACCURACY_CONDITION:
                LifeformTargetAccuracyCondition lifeformTargetAccuracyCondition = new LifeformTargetAccuracyCondition();
                lifeformTargetAccuracyCondition.setEntities(new Entities());
                return lifeformTargetAccuracyCondition;
            case MARKSMANSHIP_PRECISION_CONDITION:
                return new generated.dkf.MarksmanshipPrecisionCondition();
            case MARKSMANSHIP_SESSION_COMPLETE_CONDITION:
                return new generated.dkf.MarksmanshipSessionCompleteCondition();
            case MUZZLE_FLAGGING_CONDITION:
                return new generated.dkf.MuzzleFlaggingCondition();
            case NEGLIGENT_DISCHARGE_CONDITION:
                return new generated.dkf.NegligentDischargeCondition();
            case NINE_LINE_REPORT_CONDITION:
                return new generated.dkf.NineLineReportCondition();
            case NUMBER_OF_SHOTS_FIRED_CONDITION:
                return new generated.dkf.NumberOfShotsFiredCondition();
            case OBSERVED_ASSESSMENT_CONDITION:
                return new generated.dkf.ObservedAssessmentCondition();
            case PACE_COUNT_CONDITION:
                ScenarioClientUtility.ensurePaceCountLearnerActionsExist();
                return new generated.dkf.PaceCountCondition();
            case PPT_OVER_DWELL_CONDITION:
                PowerPointDwellCondition overDwellInput = new generated.dkf.PowerPointDwellCondition();
                overDwellInput.setDefault(new PowerPointDwellCondition.Default());
                overDwellInput.setSlides(new PowerPointDwellCondition.Slides());
                return overDwellInput;
            case PPT_UNDER_DWELL_CONDITION:
                PowerPointDwellCondition underDwellInput = new generated.dkf.PowerPointDwellCondition();
                underDwellInput.setDefault(new PowerPointDwellCondition.Default());
                underDwellInput.setSlides(new PowerPointDwellCondition.Slides());
                return underDwellInput;
            case REQUEST_EXTERNAL_ATTRIBUTE_CONDITION:
                RequestExternalAttributeCondition requestInput = new generated.dkf.RequestExternalAttributeCondition();
                requestInput.setAttributeType(ExternalAttributeEnumType.ANIMATION_PHASE);  // set a default since the select widget will show a choice by default
                return requestInput;
            case RULES_OF_ENGAGEMENT_CONDITION:
                Wcs wcs = new Wcs();
                wcs.setValue(WeaponControlStatusEnum.FREE);
                RulesOfEngagementCondition rulesOfEngagementCondition = new RulesOfEngagementCondition();
                rulesOfEngagementCondition.setWcs(wcs);
                return rulesOfEngagementCondition;
            case SIMILE_CONDITION:
                return new generated.dkf.SIMILEConditionInput();
            case SIMPLE_SURVEY_ASSESSMENT_CONDITION:
                return new generated.dkf.NoConditionInput();
            case SPACING_CONDITION:
                return new generated.dkf.SpacingCondition();
            case SPEED_LIMIT_CONDITION:
                return new generated.dkf.SpeedLimitCondition();
            case SPOT_REPORT_CONDITION:
                return new generated.dkf.SpotReportCondition();
            case STRING_MATCHING_EXAMPLE_CONDITION:
                return new GenericConditionInput();
            case USE_RADIO_CONDITION:
                return new generated.dkf.UseRadioCondition();
            case TIMER_CONDITION:
                TimerConditionInput timerCondition = new TimerConditionInput();
                timerCondition.setInterval(BigDecimal.ONE);
                timerCondition.setRepeatable(BooleanEnum.FALSE);
                return timerCondition;
            }

            /* Throw outside the switch statement so that eclipse will warn if
             * the switch statement is missing a case. */
            throw new UnsupportedOperationException(
                    "The ConditionType '" + cType + "' does not have a defined way of creating an instance.");
        }
    }

    /**
     * The object that is used to index condition input instances within a
     * {@link ConditionInputCache}.
     *
     * @author tflowers
     *
     */
    private class CacheKey {
        /** The name of the condition input type. */
        private final String inputTypeName;

        /** The type of the condition. */
        private final ConditionType conditionType;

        /**
         * Creates a {@link CacheKey} with the given {@link ConditionType} and
         * {@link String condition input name}
         *
         * @param type The {@link ConditionType} the condition input is for. Can't be null.
         * @param inputTypeName The name of the condition input to create. Can't be null or empty.
         */
        public CacheKey(ConditionType type, String inputTypeName) {
            if (type == null) {
                throw new IllegalArgumentException("The parameter 'type' cannot be null.");
            }

            if (StringUtils.isBlank(inputTypeName)) {
                throw new IllegalArgumentException("The parameter 'inputTypeName' cannot be null or empty.");
            }

            this.inputTypeName = inputTypeName;
            this.conditionType = type;
        }

        /**
         * Gets the name of the condition input type.
         *
         * @return The name of the condition input type. Can't be null.
         */
        public String getInputTypeName() {
            return inputTypeName;
        }

        /**
         * Gets the type of the condition.
         *
         * @return The type of the condition. Can't be null.
         */
        public ConditionType getConditionType() {
            return conditionType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + getConditionType().hashCode();
            result = prime * result + getInputTypeName().hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (getConditionType() != other.getConditionType())
                return false;
            if (inputTypeName == null) {
                if (other.inputTypeName != null)
                    return false;
            } else if (!getInputTypeName().equals(other.getInputTypeName()))
                return false;
            return true;
        }

        /**
         * Gets the instance of the {@link ConditionInputCache} that created
         * this {@link CacheKey}.
         *
         * @return The instance of the outer {@link ConditionInputCache}. Can't
         *         be null.
         */
        private ConditionInputCache getOuterType() {
            return ConditionInputCache.this;
        }

        @Override
        public String toString() {
            return new StringBuilder("[CacheKey: ")
                    .append("conditionType = ").append(getConditionType())
                    .append(", inputTypeName = ").append(getInputTypeName())
                    .append("]").toString();
        }
    }

    /** Stores each of the condition inputs indexed by type */
    private final Map<CacheKey, Serializable> lookup = new HashMap<>();

    /**
     * Gets the instance of the provided {@link ConditionType}. If one exists it
     * will be returned. If one does not exist, a new one will be created and
     * returned.
     *
     * @param conditionType The {@link ConditionType} of the instance to return.
     * @param conditionInputTypeName The name of the condition input type
     * @return The instance of the provided {@link ConditionType}.
     */
    public Serializable get(ConditionType conditionType, String conditionInputTypeName) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("get(" + conditionType + ", " + conditionInputTypeName + ")");
        }

        if (conditionType == null) {
            throw new IllegalArgumentException("The parameter 'conditionType' cannot be null.");
        }

        CacheKey key = new CacheKey(conditionType, conditionInputTypeName);

        /* If the instance has already been created (and is valid) return */
        Serializable toRet = lookup.get(key);
        if (conditionType == ConditionType.AVOID_LOCATION_CONDITION && isCachedAvoidLocationConditionValid()
                || conditionType == ConditionType.CHECKPOINT_PACE_CONDITION && isCachedCheckpointPaceConditionValid()
                || conditionType == ConditionType.CHECKPOINT_PROGRESS_CONDITION && isCachedCheckpointProgressConditionValid()
                || conditionType == ConditionType.CORRIDOR_BOUNDARY_CONDITION && isCachedCorridorBoundaryConditionValid()
                || conditionType == ConditionType.CORRIDOR_POSTURE_CONDITION && isCachedCorridorPostureConditionValid()
                || conditionType == ConditionType.ENTER_AREA_CONDITION && isCachedEnterAreaConditionValid()
                || conditionType == ConditionType.IDENTIFY_POIS_CONDITION && isCachedIdentifyPoisConditionValid()
                || toRet != null) {
            return toRet;
        }

        /* Create the new instance if it didn't exist */
        if (logger.isLoggable(Level.INFO)) {
            logger.info("No instanceof of '" + conditionType + "' found, creating new instance.");
        }

        toRet = ConditionType.createInstance(conditionType);
        put(conditionType, toRet);
        return toRet;
    }

    /**
     * Determines whether the current instance of the
     * {@link AvoidLocationCondition} is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedAvoidLocationConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedAvoidLocationConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.AVOID_LOCATION_CONDITION,
                AvoidLocationCondition.class.getSimpleName());
        AvoidLocationCondition avoidLocationCondition = (AvoidLocationCondition) lookup.get(key);
        if (avoidLocationCondition == null) {
            return false;
        }

        List<PointRef> pointRefs = avoidLocationCondition.getPointRef();
        for(PointRef pointRef : pointRefs){
            String pointName = pointRef.getValue();
            List<Serializable> availablePlacesOfInterest = getAvailablePlacesOfInterest();
            boolean containsPoint = containsPlaceOfInterest(pointName, availablePlacesOfInterest);
            if (!containsPoint) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether the current instance of the
     * {@link CheckpointPaceCondition} is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedCheckpointPaceConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedCheckpointPaceConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.CHECKPOINT_PACE_CONDITION,
                CheckpointPaceCondition.class.getSimpleName());
        CheckpointPaceCondition checkpointPaceCondition = (CheckpointPaceCondition) lookup.get(key);
        if (checkpointPaceCondition == null) {
            return false;
        }

        List<Serializable> availablePlacesOfInterest = getAvailablePlacesOfInterest();
        List<Checkpoint> checkPoints = checkpointPaceCondition.getCheckpoint();
        for (Checkpoint checkPoint : checkPoints) {
            if (!containsPlaceOfInterest(checkPoint.getPoint(), availablePlacesOfInterest)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether the current instance of the
     * {@link IdentifyPOIsCondition} is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedIdentifyPoisConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedIdentifyPoisConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.IDENTIFY_POIS_CONDITION, IdentifyPOIsCondition.class.getSimpleName());
        IdentifyPOIsCondition identifyPoisCondition = (IdentifyPOIsCondition) lookup.get(key);
        if (identifyPoisCondition == null) {
            return false;
        }

        List<Serializable> availablePlacesOfInterest = getAvailablePlacesOfInterest();
        List<PointRef> waypointRefs = identifyPoisCondition.getPois().getPointRef();
        for (PointRef waypointRef : waypointRefs) {
            if (!containsPlaceOfInterest(waypointRef.getValue(), availablePlacesOfInterest)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether the current instance of the {@link EnterAreaCondition}
     * is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedEnterAreaConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedEnterAreaConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.ENTER_AREA_CONDITION, EnterAreaCondition.class.getSimpleName());
        EnterAreaCondition enterAreaConditionCondition = (EnterAreaCondition) lookup.get(key);
        if (enterAreaConditionCondition == null) {
            return false;
        }

        List<Serializable> availablePlacesOfInterest = getAvailablePlacesOfInterest();
        List<Entrance> entrances = enterAreaConditionCondition.getEntrance();
        for (Entrance entrance : entrances) {
            if (!containsPlaceOfInterest(entrance.getOutside().getPoint(), availablePlacesOfInterest) ||
                    !containsPlaceOfInterest(entrance.getInside().getPoint(), availablePlacesOfInterest)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether the current instance of the
     * {@link CorridorPostureCondition} is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedCorridorPostureConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedCorridorPostureConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.CORRIDOR_POSTURE_CONDITION,
                CorridorPostureCondition.class.getSimpleName());
        CorridorPostureCondition corridorPostureCondition = (CorridorPostureCondition) lookup.get(key);
        if (corridorPostureCondition == null) {
            return false;
        }

        if(corridorPostureCondition.getPathRef() == null || StringUtils.isBlank(corridorPostureCondition.getPathRef().getValue())) {
            return false;
        }

        return true;
    }

    /**
     * Determines whether the current instance of the
     * {@link CorridorBoundaryCondition} is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedCorridorBoundaryConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedCorridorBoundaryConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.CORRIDOR_BOUNDARY_CONDITION,
                CorridorBoundaryCondition.class.getSimpleName());
        CorridorBoundaryCondition corridorBoundaryCondition = (CorridorBoundaryCondition) lookup.get(key);
        if (corridorBoundaryCondition == null) {
            return false;
        }

        if(corridorBoundaryCondition.getPathRef() == null || StringUtils.isBlank(corridorBoundaryCondition.getPathRef().getValue())) {
            return false;
        }

        return true;
    }

    /**
     * Determines whether the current instance of the
     * {@link CheckpointProgressCondition} is valid.
     *
     * @return True if the cached instance is valid, false otherwise.
     */
    private boolean isCachedCheckpointProgressConditionValid() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCachedCheckpointProgressConditionValid()");
        }

        CacheKey key = new CacheKey(ConditionType.CHECKPOINT_PROGRESS_CONDITION,
                CheckpointProgressCondition.class.getSimpleName());
        CheckpointProgressCondition checkpointProgressCondition = (CheckpointProgressCondition) lookup.get(key);
        if (checkpointProgressCondition == null) {
            return false;
        }

        List<Serializable> availableWaypoints = getAvailablePlacesOfInterest();
        List<Checkpoint> checkpoints = checkpointProgressCondition.getCheckpoint();
        for(Checkpoint checkpoint : checkpoints){

            if (!containsPlaceOfInterest(checkpoint.getPoint(), availableWaypoints)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Updates the cached instance of the provided {@link ConditionType}.
     *
     * @param conditionType The {@link ConditionType} of the instance to update.
     * @param inputType The new value of the cached instance.
     */
    public void put(ConditionType conditionType, Serializable inputType) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(conditionType, inputType);
            logger.fine("put(" + StringUtils.join(", ", params) + ")");
        }

        if (conditionType == null) {
            throw new IllegalArgumentException("The parameter 'conditionType' cannot be null.");
        } else if (inputType == null) {
            throw new IllegalArgumentException("The parameter 'inputType' cannot be null.");
        }

        String conditionInputTypeName = inputType.getClass().getSimpleName();
        CacheKey key = new CacheKey(conditionType, conditionInputTypeName);
        lookup.put(key, inputType);
    }

    /**
     * Tests whether a provided list of places of interest contains a place of
     * interest (e.g. Point) with a provided name.
     *
     * @param name The name of the place of interest for which to search.
     * @param placesOfInterest The collection of places of interest to search.
     * @return True if the collection contains a place of interest with the
     *         given name, false otherwise.
     */
    private boolean containsPlaceOfInterest(String name, List<Serializable> placesOfInterest) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(name, placesOfInterest);
            logger.fine("containsPlaceOfInterest(" + StringUtils.join(", ", params) + ")");
        }

        if (name == null || name.length() == 0) {
            return false;
        }

        return ScenarioClientUtility.containsPlaceOfInterest(name, placesOfInterest);
    }

    /**
     * Returns a copy of the list of places of interest contained within the
     * current {@link Scenario}.
     *
     * @return A copy of the {@link Scenario scenario's} places of interest.
     */
    private ArrayList<Serializable> getAvailablePlacesOfInterest() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getAvailableWaypoints()");
        }

        return new ArrayList<>(ScenarioClientUtility.getUnmodifiablePlacesOfInterestList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ConditionInputCache: ");
        StringUtils.join(", ", lookup.entrySet(), new Stringifier<Map.Entry<CacheKey, Serializable>>() {

            @Override
            public String stringify(Entry<CacheKey, Serializable> obj) {
                return obj.getKey().toString() + "=" + obj.getValue();
            }
        }, sb);
        return sb.append("]").toString();
    }
}
