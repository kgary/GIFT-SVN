/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * This class contains information about text that needs to be displayed by the
 * Tutor
 *
 * @author jleonard
 */
public class DisplayTextGuidanceTutorRequest extends AbstractDisplayGuidanceTutorRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text = null;

    /** This request is used by the tutor module to clear the current transition. 
     * Used to present the continue button in an autotutor session that occurs after a training application. */
    public static final DisplayTextGuidanceTutorRequest EMPTY_REQUEST = new DisplayTextGuidanceTutorRequest();
        
    /**
     * Default constructor. Should not be called outside of this class.
     */
    public DisplayTextGuidanceTutorRequest() {
        super();
    }

    /**
     * Class constructor
     * 
     * @param text The text to be displayed
     */
    public DisplayTextGuidanceTutorRequest(String text) {
        super();

        this.text = text;
    }

    /**
     *
     * @param text The text to be displayed
     * @param fullscreen Whether the guidance should be displayed in full screen on the tutor
     */
    public DisplayTextGuidanceTutorRequest(String text, boolean fullscreen) {
        super(fullscreen);

        this.text = text;
    }

    /**
     * Class constructor
     *
     * @param text The text to be displayed
     * @param fullscreen Whether the guidance should be displayed in full screen on the tutor
     * @param displayDuration The amount of time the text should be displayed
     * before being removed (milliseconds)
     */
    public DisplayTextGuidanceTutorRequest(String text, boolean fullscreen, int displayDuration) {
        super(fullscreen, displayDuration);

        this.text = text;
    }

    /**
     * Returns the text to be displayed
     *
     * @return String The text to be displayed
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayTextGuidanceTutorRequest: ");
        sb.append(" text = ").append(getText());
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
