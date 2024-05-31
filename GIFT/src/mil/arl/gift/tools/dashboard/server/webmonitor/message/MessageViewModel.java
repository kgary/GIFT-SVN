/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

/**
 * The server-side representation of a message display panel on the client. This
 * acts as the web monitor's version of the desktop monitor's MessageViewPanel, though
 * since it is server-side, it only implements a small subset of MessageViewPanel's 
 * behavior while the client performs the rest.
 * 
 * @author nroberts
 */
public class MessageViewModel{

    /**
     * Sets the filter that this message view should use
     * 
     * @param messageViewFilter the filter to use. Cannot be null.
     */
    public void setFilterListModel(MessageViewFilter messageViewFilter) {
        messageViewFilter.acceptAll();
    }

}
