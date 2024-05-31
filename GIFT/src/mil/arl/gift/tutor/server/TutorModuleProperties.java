/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the Tutor module property values.
 *
 * @author jleonard
 */
public class TutorModuleProperties extends AbstractModuleProperties {

    /** the properties file name */
    private static final String PROPERTIES_FILE = "tutor.properties";

    /** The name of the property used by {@link #getLandingPageMessage()} */
    public static final String LANDINGPAGE_MESSAGE = "LandingPageMessage";

    /** The name of the property used by {@link #getCharacterDetectionTimeout()} */
    public static final String CHARACTER_DETECTION_TIMEOUT = "CharacterDetectionTimeout";
    
    /** the name of the property used by {@link #getOldFeedbackDifferentStyle()} */
    public static final String OLD_FEEDBACK_DIFF_STYLE = "OldFeedbackDifferentStyle";

    /**
     * The default value to be returned by {@link #getCharacterDetectionTimeout()}
     * if a value wasn't defined in the properties file
     */
    public static final int DEFAULT_CHARACTER_DETECTION_TIMEOUT = 120000;

    /** singleton instance of this class */
    private static TutorModuleProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return TutorModuleProperties
     */
    public static synchronized TutorModuleProperties getInstance() {

        if (instance == null) {
            instance = new TutorModuleProperties();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private TutorModuleProperties() {
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return whether older feedback shown in the TUI webpage should be styled differently
     * than the latest feedback.
     * @return true if the older feedback should be styled differently than the latest feedback.
     * Default is false.
     */
    public boolean getOldFeedbackDifferentStyle(){
        return getPropertyBooleanValue(OLD_FEEDBACK_DIFF_STYLE, false);
    }

    /**
     * Return the landing page message to use in the tutor client.
     *
     * @return String Can be null or empty string
     */
    public String getLandingPageMessage() {
        return getPropertyValue(LANDINGPAGE_MESSAGE);
    }

    /**
     * Return the timeout duration for how long to wait for a response back from
     * the avatar before assuming it is not functioning correctly.
     *
     * @return The number of milliseconds to wait before the timeout occurs. If
     *         the property wasn't defined,
     *         {@value #DEFAULT_CHARACTER_DETECTION_TIMEOUT} is returned.
     */
    public int getCharacterDetectionTimeout() {
        return getPropertyIntValue(CHARACTER_DETECTION_TIMEOUT, DEFAULT_CHARACTER_DETECTION_TIMEOUT);
    }
}
