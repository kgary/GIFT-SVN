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
 * An action that is called when the course is ready to be started.
 * Currently this is ONLY used by the TUI in embedded mode to respond properly
 * to when the course is started.
 *
 * @author nblomberg
 *
 */
public class StartCourseAction extends AbstractAction implements IsSerializable {

    
    /** (optional) message to indicate why the domain session is starting */
    private String message = "";
    
    /**
     * Default Constructor
     *
     */
    public StartCourseAction() {
        super(ActionTypeEnum.START_COURSE);
    }


    /**
     * Constructor which takes in an optional message to indicate why the course is started.
     * @param msg - the optional message to indicate why the course is started.  Can be null or empty string.
     */
    public StartCourseAction(String msg) {
        super(ActionTypeEnum.START_COURSE);
        
        message = msg;
    }
    
    /**
     * Accessor to retrieve the optional message.  The message can be used to indicate
     * why the course is started.  It can be empty or null string.
     * @return String - the optional message to indicate why the course is started.  Can be null or empty string.
     */
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[StartDomainSessionAction: ");
        sb.append(super.toString());
        sb.append(", message = ").append(message);
        sb.append("]");
        return sb.toString();
    }
}
