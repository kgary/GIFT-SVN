/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import mil.arl.gift.common.gwt.server.AbstractWebSessionData;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;

/**
 * Stores the screen state of the browser session.  It holds an enum to indicate
 * which screen the browser was on.
 * 
 * @author nblomberg
 *
 */
public class BrowserScreenState extends AbstractWebSessionData {


    /** The current screen enum  */
    private ScreenEnum screenState = ScreenEnum.INVALID;
    
    /** 
     * Constructor - default
     */
    public BrowserScreenState(ScreenEnum screenState) {
       this.screenState = screenState;
    }

    /**
     * Get the current state / enum of the screen
     * @return The enum of the screen state. 
     */
    public ScreenEnum getScreenState() {
        return screenState;
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("[BrowserScreenState: ");
        sb.append("screenState = ").append(getScreenState());
        sb.append("]");
        return sb.toString();
    }

      
}
