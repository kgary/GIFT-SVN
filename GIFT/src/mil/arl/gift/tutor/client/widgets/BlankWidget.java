/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Constructs a blank widget used display nothing on the page
 *
 * @author cpadilla
 */
public class BlankWidget extends Composite {

    /**
     * Constructor
     */
    public BlankWidget() {
        VerticalPanel container = new VerticalPanel();
        initWidget(container);
    }
}
