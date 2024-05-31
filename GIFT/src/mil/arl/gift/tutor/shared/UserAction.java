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
 * An action that can be made by the user
 *
 * @author jleonard
 */
public class UserAction implements IsSerializable {

    private UserActionIconEnum icon;
    
    private generated.dkf.LearnerAction learnerAction;
    
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public UserAction() {
    }

    /**
     * Constructor
     *
     * @param learnerAction The authored learner action details. Cannot be null.
     * If the icon is TUTOR_ME_ICON, the learnerAction.description cannot be null or empty.
     * @param icon The icon to display with the user action. Can be null.
     */
    public UserAction(generated.dkf.LearnerAction learnerAction, UserActionIconEnum icon) {

        if(learnerAction == null) {
            throw new IllegalArgumentException("The learner action value can't be null.");
        }else if(learnerAction.getType() == null){
            throw new IllegalArgumentException("The learner action type can't be null.");
        }
        
        this.learnerAction = learnerAction;
        this.icon = icon;        
    }
    
    /**
     * Return the authored learner action information for this user action.
     * 
     * @return won't be null.
     */
    public generated.dkf.LearnerAction getLearnerAction(){
        return learnerAction;
    }

    /**
     * Gets the key of the user action
     *
     * @return String The key of the user action
     */
    public String getValue() {

        return learnerAction.getType().value();
    }

    /**
     * Gets the icon to display with the user action
     *
     * @return UserActionIconEnum The icon to display with the user action. Can be null.
     */
    public UserActionIconEnum getIcon() {

        return icon;
    }

    /**
     * Gets the display string for the user action
     *
     * @return String The display string for the user action
     */
    public String getDisplayString() {
        return learnerAction.getDisplayName() != null ? learnerAction.getDisplayName() : learnerAction.getType().value();
    }
    
    /**
     * Gets the description for the user action.
     * 
     * @return description the description for the user action. Can be null.
     */
    public String getDescription() {
    	return learnerAction.getDescription();
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[UserAction: ");
        sb.append("value = ").append(getValue());
        sb.append(", displayString = ").append(getDisplayString());
        sb.append(", description = ").append(getDescription());
        sb.append(", icon = ").append(getIcon());
        sb.append("]");
        return sb.toString();
    }
}
