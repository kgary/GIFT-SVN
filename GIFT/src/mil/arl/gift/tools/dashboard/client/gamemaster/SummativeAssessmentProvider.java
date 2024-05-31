/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.SummativeAssessmentProvider.SummativeAssessmentChangeHandler;

/**
 * Provider to listen to updates to a sessions's summative asessments
 * 
 * @author sharrison
 */
public class SummativeAssessmentProvider extends AbstractProvider<SummativeAssessmentChangeHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SummativeAssessmentProvider.class.getName());

    /** The instance of this class */
    private static SummativeAssessmentProvider instance = null;

    /** A mapping from each unique performance node name to its summative assessment score */
    private Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment = new HashMap<>();
    
    /** A mode that determines that types of assessments are shown in the timeline (i.e. formative vs summative) */
    private AssessmentDisplayMode assessmentDisplayMode = AssessmentDisplayMode.SUMMATIVE;

    /**
     * Singleton constructor
     */
    private SummativeAssessmentProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static SummativeAssessmentProvider getInstance() {
        if (instance == null) {
            instance = new SummativeAssessmentProvider();
        }

        return instance;
    }
    
    /**
     * Updates the stored summative assessments and notifies any listeners that they have changed
     * 
     * @param perfNodeNameToSummativeAssessment the summative assessments. Can be null.
     */
    public void setSummativeAssessments(final Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment) {
        this.perfNodeNameToSummativeAssessment = perfNodeNameToSummativeAssessment;

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<SummativeAssessmentChangeHandler>() {
            @Override
            public void execute(SummativeAssessmentChangeHandler handler) {
                handler.onSummativeAssessmentsChanged(perfNodeNameToSummativeAssessment);
            }
        });
    }

    /**
     * Gets the stored summative assessments for the session
     * 
     * @return the summative assessments. Can be null.
     */
    public Map<String, AssessmentLevelEnum> getSummativeAssessments() {
        return perfNodeNameToSummativeAssessment;
    }
    
    /**
     * Sets the display mode to use when showing assessments in the timeline and refreshes
     * the timeline to reflect the new display mode
     * 
     * @param mode the new display mode. Cannot be null.
     */
    public void setDisplayMode(final AssessmentDisplayMode mode) {
        
        if(mode == null) {
            throw new IllegalArgumentException("The display mode cannot be null");
        }
        
        boolean changed = this.assessmentDisplayMode == null 
                || !this.assessmentDisplayMode.equals(mode);
        this.assessmentDisplayMode = mode;

        if (changed) {
            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<SummativeAssessmentChangeHandler>() {
                @Override
                public void execute(SummativeAssessmentChangeHandler handler) {
                    handler.onDisplayModeChanged(mode);
                }
            });
        }
    }
    
    /**
     * Gets the display mode used to determine how the timeline should display assessment levels.
     * 
     * @return the assessment display mode. Cannot be null.
     */
    public AssessmentDisplayMode getDisplayMode() {
        return assessmentDisplayMode;
    }
    
    /**
     * The different modes that the timeline can use to display a session's assessments
     * 
     * @author nroberts
     */
    public enum AssessmentDisplayMode{
        
        /** 
         * Tells the timeline to display formative asessments that have been reported by learner
         * state message updates during the knowledge session
         */
        FORMATIVE("Formative"),
        
        /**
         * Tells the timeline to display summative assessments that have been gathered from the
         * knowledge session's published lesson scores
         */
        SUMMATIVE("Summative");
        
        /** The display name for this display mode */
        private String displayName;
        
        /**
         * Creates a new display mode with the given display name
         * 
         * @param displayName the display name of the mode. Cannot be null.
         */
        private AssessmentDisplayMode(String displayName) {
            if(StringUtils.isBlank(displayName)) {
                throw new IllegalArgumentException("The display name for a display mode cannot be null");
            }
            
            this.displayName = displayName;
        }

        /**
         * Gets the display name for this display mode
         * 
         * @return The display name. Will not be null.
         */
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * A handler that can be used to register to receive updates to the session's summative assessments
     * 
     * @author nroberts
     */
    public interface SummativeAssessmentChangeHandler {

        /**
         * Handles when the summative assessment scores have changed
         * 
         * @param perfNodeNameToSummativeAssessment a mapping from each performance node name to its
         * new summave score. Can be null.
         */
        void onSummativeAssessmentsChanged(Map<String, AssessmentLevelEnum> perfNodeNameToSummativeAssessment);
        
        /**
         * Handles when the assessment display mode has changed
         * 
         * @param displayMode the new assessment display mode
         */
        void onDisplayModeChanged(AssessmentDisplayMode displayMode);
    }
}
