/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

import mil.arl.gift.tutor.client.BrowserSession;

/**
 * Constructs the GIFT logo widget
 *
 * @author jleonard
 */
public class LogoWidget extends Composite {

    /**
     * Constructor
     */
    public LogoWidget() {
        VerticalPanel container = new VerticalPanel();
        container.setSize("250px", "146px");
        container.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        container.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Image logo = new Image(BrowserSession.getInstance().getLogoUrl());
        logo.setSize("250px", "146px");
        logo.addStyleName("mediumMargin");
        container.add(logo);
        initWidget(container);
    }
}
