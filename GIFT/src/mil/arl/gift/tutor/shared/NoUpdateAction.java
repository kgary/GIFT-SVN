/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

/**
 * Represents that there is no update (i.e. new page) for the TUI to show.
 * This is useful so that a GWT RPC client request for the new page can be returned
 * before being timed out by an apache proxy.
 * 
 * @author mhoffman
 *
 */
public class NoUpdateAction extends AbstractAction {

    public NoUpdateAction(){
        super(ActionTypeEnum.NO_UPDATE);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[NoUpdateAction: ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
    
}
