/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;



/**
 * The ChildIFrameConsts class represents a list of constant
 * strings to specify which iframe the dashboard will need to communicate with.
 * 
 * @author nblomberg
 *
 */
public class ChildIFrameConsts {

    // The origin key of the TUI (used to signal which 'child frame' we want to communicate with).
    public static final String TUI_IFRAME_KEY = "TUI";
    
    // The origin key of the GAT.
    public static final String GAT_IFRAME_KEY = "GAT";

    
    /*
     * Make private so the class cannot be instantiated.
     */
    private ChildIFrameConsts() {
        
    }
}
