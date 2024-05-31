/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.AvailableLearnerActions;
import generated.dkf.Condition;
import generated.dkf.PaceCountCondition;
import generated.dkf.PlacesOfInterest;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import mil.arl.gift.common.util.StringUtils;

/**
 * Class to maintain a cache for the scenario objects' validity.
 *
 * @author sharrison
 */
public class ScenarioValidationCache {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioValidationCache.class.getName());

    /**
     * The cache for the scenario objects that contains their validity status and state of the cache
     * (dirty)
     */
    private final Map<Serializable, CacheStatus> validationCache = new HashMap<>();

    /** The validator for the scenario elements */
    private final ScenarioValidatorUtility validator = new ScenarioValidatorUtility(this);

    /** Constructor */
    public ScenarioValidationCache() {
    }

    /**
     * Sets the cache dirty for the provided validation object.
     *
     * @param validationObject the validation object to mark as dirty. Can't be null.
     */
    public void setCacheDirty(Serializable validationObject) {
        if (validationObject == null) {
            throw new IllegalArgumentException("The parameter 'validationObject' cannot be null.");
        }

        if (validationCache.containsKey(validationObject)) {
            validationCache.get(validationObject).setDirty();
        } else {
            validationCache.put(validationObject, new CacheStatus());
        }

        /* Some objects have additional special behavior when being set
         * dirty. */
        performSpecialSetDirtyBehavior(validationObject);
    }

    /**
     * Performs any additional special behavior that is required to make a
     * validation object dirty (e.g. marking dependent validation objects as
     * dirty).
     *
     * @param validationObject The object for which to perform additional dirty
     *        behavior.
     */
    private void performSpecialSetDirtyBehavior(Serializable validationObject) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("performSpecialSetDirtyBehavior(" + validationObject + ")");
        }

        if (validationObject instanceof AvailableLearnerActions) {
            performSpecialSetDirtyBehavior((AvailableLearnerActions) validationObject);
        } else if (validationObject instanceof Condition) {
            performSpecialSetDirtyBehavior((Condition) validationObject);
        } else if (validationObject instanceof Team) {
            performSpecialSetDirtyBehavior((Team) validationObject);
        } else if (validationObject instanceof TeamMember) {
            performSpecialSetDirtyBehavior((TeamMember) validationObject);
        } else if(validationObject instanceof PlacesOfInterest){
            // places of interest
            performSpecialSetDirtyBehavior((PlacesOfInterest)validationObject);
        }
    }
    
    /**
     * Marks all strategies that are dependent on a specific place of interest (e.g. generated.dkf.Point)
     * as dirty.
     * @param placesOfInterest the place of interest that has changed.  Should not be null.
     */
    private void performSpecialSetDirtyBehavior(PlacesOfInterest placesOfInterest){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("performSpecialSetDirtyBehavior(" + placesOfInterest + ")");
        }
        
        // check strategies
        for(Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()){
            
            boolean dirtyStrategy = false;
            for(Serializable activity : strategy.getStrategyActivities()){
             
                // looking for activities that can reference places of interest
                if(activity instanceof ScenarioAdaptation){
                    
                    ScenarioAdaptation sAdaptation = (ScenarioAdaptation)activity;
                    generated.dkf.EnvironmentAdaptation eAdaptation = sAdaptation.getEnvironmentAdaptation();
                    if(eAdaptation == null){
                        continue;
                    } else if(eAdaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs){
                        generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs breadcrumbs = 
                                (generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs)eAdaptation.getType();
                        
                        generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = breadcrumbs.getLocationInfo();
                        if(locationInfo != null){
                            dirtyStrategy = true;
                            break;
                        }
                        
                    }else if(eAdaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects){
                        generated.dkf.EnvironmentAdaptation.HighlightObjects highlight = 
                                (generated.dkf.EnvironmentAdaptation.HighlightObjects)eAdaptation.getType();
                        
                        if(highlight.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo){
                            
                            generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                                    (generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo)highlight.getType();
                            
                            if(locationInfo != null){
                                dirtyStrategy = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            if(dirtyStrategy){
                getStatus(strategy).setDirty();
            }
        }
    }

    /**
     * Marks all conditions that are dependent on a specific
     * {@link LearnerAction} as dirty.
     *
     * @param learnerActions The singleton {@link AvailableLearnerActions}.
     *        Should be non-null.
     */
    private void performSpecialSetDirtyBehavior(AvailableLearnerActions learnerActions) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("performSpecialSetDirtyBehavior(" + learnerActions + ")");
        }

        for (Condition condition : ScenarioClientUtility.getUnmodifiableConditionList()) {
            if (condition.getInput() != null && condition.getInput().getType() instanceof PaceCountCondition) {
                getStatus(condition).setDirty();
            }
        }
    }

    /**
     * Marks the {@link AvailableLearnerActions} within the current
     * {@link Scenario} as dirty if the given {@link Condition} consumes a
     * {@link PaceCountCondition} for its input. This is done because the
     * validation logic for {@link AvailableLearnerActions} specifies that
     * {@link LearnerActionType#START_PACE_COUNT} and
     * {@link LearnerActionType#START_PACE_COUNT} type {@link LearnerAction
     * LearnerActions} are required if there is a {@link PaceCountCondition}
     * within the {@link Scenario}.
     *
     * @param condition The {@link Condition} which was just marked dirty. Can't
     *        be null.
     */
    private void performSpecialSetDirtyBehavior(Condition condition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("performSpecialSetDirtyBehavior(" + condition + ")");
        }

        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        /* If a PaceCountCondition is dirty, then learner actions need to be
         * revalidated. */
        if (condition.getInput() != null && condition.getInput().getType() instanceof PaceCountCondition) {
            AvailableLearnerActions availableLearnerActions = ScenarioClientUtility.getAvailableLearnerActions();
            getStatus(availableLearnerActions).setDirty();
        }
    }

    /**
     * Marks all parents of the {@link TeamMember} as dirty.
     * 
     * @param teamMember the team member which was just marked dirty. Can't be
     *        null.
     */
    private void performSpecialSetDirtyBehavior(TeamMember teamMember) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("performSpecialSetDirtyBehavior(" + teamMember + ")");
        }

        if (teamMember == null) {
            throw new IllegalArgumentException("The parameter 'teamMember' cannot be null.");
        }

        Team parent = ScenarioClientUtility.findTeamParent(teamMember);
        while (parent != null) {
            getStatus(parent).setDirty();
            parent = ScenarioClientUtility.findTeamParent(parent);
        }
    }

    /**
     * Marks all parents of the {@link Team} as dirty.
     * 
     * @param team the team which was just marked dirty. Can't be null.
     */
    private void performSpecialSetDirtyBehavior(Team team) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("performSpecialSetDirtyBehavior(" + team + ")");
        }

        if (team == null) {
            throw new IllegalArgumentException("The parameter 'team' cannot be null.");
        }

        Team parent = ScenarioClientUtility.findTeamParent(team);
        while (parent != null) {
            getStatus(parent).setDirty();
            parent = ScenarioClientUtility.findTeamParent(parent);
        }
    }

    /**
     * Retrieves the validity of the provided validation object. If the cache is clean, the validity
     * value will be returned; otherwise, validation will be performed, the cache will be updated,
     * and the result returned.
     *
     * @param validationObject the validation object to retrieve the validity value. Can't be null.
     * @return the validity of the provided object.
     */
    public boolean isValid(Serializable validationObject) {
        return isValid(validationObject, false);
    }

    /**
     * Retrieves the validity of the provided validation object. If the cache is clean, the validity
     * value will be returned; otherwise, validation will be performed, the cache will be updated,
     * and the result returned.
     *
     * @param validationObject the validation object to retrieve the validity value. Can't be null.
     * @param ignoreCache always re-validates the object. Ignores the cached value.
     * @return the validity of the provided object.
     */
    public boolean isValid(Serializable validationObject, boolean ignoreCache) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(validationObject, ignoreCache);
            logger.fine("isValid(" + StringUtils.join(", ", params) + ")");
        }

        if (validationObject == null) {
            throw new IllegalArgumentException("The parameter 'validationObject' cannot be null.");
        }

        CacheStatus cache = getStatus(validationObject, ignoreCache);
        return cache.isValid();
    }

    /**
     * Retrieves the validity cache status of the provided validation object. If
     * the cache is clean, the validity value will be returned; otherwise,
     * validation will be performed, the cache will be updated, and the result
     * returned.
     *
     * @param validationObject the validation object to retrieve the validity
     *        value. Can't be null.
     * @param ignoreCache always re-validates the object. Ignores the cached
     *        value.
     * @return the validity cache status of the provided object.
     */
    public CacheStatus getStatus(Serializable validationObject) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getStatus(" + validationObject + ")");
        }

        return getStatus(validationObject, false);
    }

    /**
     * Retrieves the validity cache status of the provided validation object. If
     * the cache is clean, the validity value will be returned; otherwise,
     * validation will be performed, the cache will be updated, and the result
     * returned.
     *
     * @param validationObject the validation object to retrieve the validity
     *        value. Can't be null.
     * @param ignoreCache always re-validates the object. Ignores the cached
     *        value.
     * @return the validity cache status of the provided object.
     */
    public CacheStatus getStatus(Serializable validationObject, boolean ignoreCache) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(validationObject, ignoreCache);
            logger.fine("getStatus(" + StringUtils.join(", ", params) + ")");
        }

        /* Gets the existing status from the map or creates one and puts it in
         * the map. */
        CacheStatus cache = validationCache.get(validationObject);
        if (cache == null) {
            cache = new CacheStatus();
            validationCache.put(validationObject, cache);
        }

        /* If necessary, rerun the validation logic. */
        if (ignoreCache || cache.isDirty()) {
            String msg = validator.validateObject(validationObject);
            cache.setValidity(msg == null);
            if (logger.isLoggable(Level.FINE) && !cache.isValid()) {
                cache.setMessage(msg);
                logger.fine("Object is invalid because: " + msg);
            }
        }

        return cache;
    }

    /**
     * Removes the provided validation object from the cache.
     *
     * @param validationObject the validation object to remove from the cache. Can't be null.
     */
    public void dropFromCache(Serializable validationObject) {
        if (validationObject == null) {
            throw new IllegalArgumentException("The parameter 'validationObject' cannot be null.");
        }

        validationCache.remove(validationObject);
    }

    /**
     * Cache status containing its state (dirty flag) and validity value (valid/invalid).
     *
     * @author sharrison
     */
    public class CacheStatus {
        /** Validity flag */
        private boolean valid = true;

        /** Dirty state flag */
        private boolean dirty;

        /** The reason the status is invalid */
        private String message;

        /**
         * Constructor. Marks as dirty since no validity value was provided.
         */
        public CacheStatus() {
            setDirty();
        }

        /**
         * Returns this cache's validity value.
         *
         * @return true if the cache's value is valid; false otherwise.
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Updates the cache's validity value and marks it as clean (dirty = false).
         *
         * @param valid true to mark the validity value as valid; false to mark it as invalid.
         */
        public void setValidity(boolean valid) {
            this.valid = valid;
            setClean();
        }

        /**
         * Gets the reason that the status is invalid.
         *
         * @return The reason the status is invalid. If the status is valid,
         *         null will always be returned.
         */
        public String getMessage() {
            if (this.valid) {
                return null;
            }

            return message;
        }

        /**
         * Sets the reason that the status is invalid.
         *
         * @param message The reason that the cache status is invalid. Can't be
         *        null.
         */
        public void setMessage(String message) {
            if (message == null) {
                throw new IllegalArgumentException("The parameter 'message' cannot be null.");
            }

            this.message = message;
        }

        /**
         * Returns this cache's state.
         *
         * @return true if the cache is dirty (outdated) - this indicates that
         *         the validity value cannot be trusted; false otherwise.
         */
        public boolean isDirty() {
            return dirty;
        }

        /**
         * Marks the cache's state as dirty.
         */
        public void setDirty() {
            this.dirty = true;
        }

        /**
         * Marks the cache's state as clean.
         */
        private void setClean() {
            this.dirty = false;
        }
        
        @Override
        public String toString() {
            return new StringBuilder("[CacheStatus: ")
                    .append("dirty = ").append(dirty)
                    .append(", message = ").append(message)
                    .append(", valid = ").append(valid)
                    .toString();
    }
}
}
