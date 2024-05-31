/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

/**
 * An exception for survey questions answered in weird ways
 *
 * @author jleonard
 */
public class MalformedAnswerException extends Exception {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_MESSAGE = "Malformed answer";
    
    private final boolean isCritical;

    private final String why;

    /**
     * Constructor
     *
     * @param why Why the answer was malformed
     * @param isCritical whether the exception is deemed a critical exception or not  
     */
    public MalformedAnswerException(String why, boolean isCritical) {
        super(ERROR_MESSAGE + " - " + why);
        
        this.why = why;
        this.isCritical = isCritical;
    }
    
    public boolean getIsCritical() {
        
        return isCritical;
    }

    /**
     * Gets why the answer was malformed
     *
     * @return String The reason why the answer was malformed
     */
    public String getWhy() {
        return why;
    }
}
