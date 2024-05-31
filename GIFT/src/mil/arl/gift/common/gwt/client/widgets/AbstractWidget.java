/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.user.client.ui.Composite;

/**
 * This is the abstract widget class that all gift gwt widgets should extend from
 * Note that if using bootstrap components, extend off of the AbstractBsWidget class.
 *   
 * @author nblomberg
 *
 */
public abstract class AbstractWidget extends Composite {
    
    
    /**
     * Allow a widget to perform any cleanup prior to being detached from the dom.
     * In one example, we use this to allow the widget to cancel any outstanding rpcs.
     * 
     */
    public void onPreDetach() {
        // Do Nothing.
    }
}
