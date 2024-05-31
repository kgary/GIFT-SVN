/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.consumer;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.LtiProvider;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.LtiAuthenticationWidget;

/**
 * A widget used to display a course's LTI consumer properties to the user
 * 
 * @author sharrison
 */
public class LtiConsumerPropertiesPanel extends Composite {

    private static LtiConsumerPropertiesPanelUiBinder uiBinder = GWT.create(LtiConsumerPropertiesPanelUiBinder.class);

    interface LtiConsumerPropertiesPanelUiBinder extends UiBinder<Widget, LtiConsumerPropertiesPanel> {
    }

    @UiField
    protected Button editProvidersButton;

    @UiField
    protected FlowPanel trustedProvidersPanel;

    /**
     * Constructor.
     */
    public LtiConsumerPropertiesPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Initializes this widget's data so that it can display a course's LTI provider information to
     * the user
     * 
     * @param providerList the path to the course whose LTI information should be displayed
     */
    public void init(List<LtiProvider> providerList) {

        editProvidersButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        });

        refresh(providerList);
    }

    /**
     * Sets the click handler for the edit button.
     * 
     * @param clickHandler the click handler.
     */
    public void setEditProvidersButtonClickHandler(ClickHandler clickHandler) {
        editProvidersButton.addDomHandler(clickHandler, ClickEvent.getType());
    }
    
    /**
     * Refreshes the panel with the provider widgets.
     * 
     * @param providerList the new list of providers for this panel
     */
    public void refresh(List<LtiProvider> providerList) {
        trustedProvidersPanel.clear();

        if (providerList != null) {

            for (LtiProvider provider : providerList) {
                LtiAuthenticationWidget widget = new LtiAuthenticationWidget(provider);
                trustedProvidersPanel.add(widget);
            }
        }
    }
}
