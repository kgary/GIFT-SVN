/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An action that is called with the course needs to be ended.
 * Currently this is ONLY used by the TUI in embedded mode to respond properly
 * to when the course transition is completed and the course needs to end.
 *
 * @author nblomberg
 */
public class EndCourseAction extends AbstractAction implements IsSerializable {

    
    /** (optional) message to indicate why the course is ending */
    private String message = "";
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    private EndCourseAction() {
        super(ActionTypeEnum.END_COURSE);
    }

    /**
     * Constructor
     *
     * @param msg - (optional) A reason why the course is ending.
     */
    public EndCourseAction(String msg) {
        this();
        
        message = msg;
    }
    
    /**
     * Accessor to return the reason why the course is ending. May be null or empty string.
     * @return String - reason why the domain sesssion is ending (may be null or empty string).
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[EndCourseAction: message=");
        builder.append(message);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
    
}
