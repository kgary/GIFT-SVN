/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A descriptor widget for the conditions. Displays the condition name and description. Contains a
 * hidden panel that can be toggled to show the full description of the condition.
 * 
 * @author sharrison
 */
public class ConditionDescriptor extends Composite {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConditionDescriptor.class.getName());

    /** The ui binder. */
    private static ConditionDescriptorUiBinder uiBinder = GWT.create(ConditionDescriptorUiBinder.class);

    /** The Interface ConditionDescriptorUiBinder */
    interface ConditionDescriptorUiBinder extends UiBinder<Widget, ConditionDescriptor> {
    }

    /** The name of the condition */
    @UiField
    protected InlineHTML conditionName;

    /** The collapse containing the entire condition description */
    @UiField
    protected PanelHeader collapseHeader;

    /** The collapse containing the entire condition description */
    @UiField
    protected Collapse collapse;
    
    /** The condition description displayed in its entirety */
    @UiField
    protected HTML fullConditionDescription;

    /** A brief description of the condition */
    @UiField
    protected HTML shortConditionDescription;
    
    /**
     * The button the user uses to navigate back to the
     * {@link ConditionSelectionPanel}
     */
    @UiField
    protected org.gwtbootstrap3.client.ui.Button backButton;

    /**
     * Constructor.
     */
    public ConditionDescriptor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        collapseHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (collapse.isShown()) {
                    collapse.hide();
                } else {
                    collapse.show();
                }
            }
        }, ClickEvent.getType());

        // set defaults
        shortConditionDescription.setVisible(false);
        conditionName.setHTML("Loading...");
        
        backButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                collapse.hide();

                // stop the click from propagating to the collpase panel
                event.stopPropagation();
            }
        });
    }

    /**
     * Sets the condition name
     * 
     * @param conditionName the name to set
     */
    public void setConditionName(String conditionName) {
        if (StringUtils.isBlank(conditionName)) {
            return;
        }

        this.conditionName.setHTML(conditionName);
    }

    /**
     * Sets the full condition description that the user will be able to view within the 'help'
     * page.
     * 
     * @param safeDescription the condition description in its entirety.
     */
    public void setFullConditionDescription(SafeHtml safeDescription) {
        if (safeDescription == null) {
            return;
        }

        fullConditionDescription.setHTML(safeDescription);
    }

    /**
     * Sets an abbreviated description to be displayed to the user.
     * 
     * @param safeDescription the shortened condition description.
     */
    public void setShortConditionDescription(SafeHtml safeDescription) {
        if (safeDescription == null) {
            return;
        }

        shortConditionDescription.setHTML(safeDescription);
        shortConditionDescription.setVisible(true);
    }

    /**
     * Attaches a ClickHandler to the back button
     * 
     * @param backButtonClickHandler - the click handler to attach to the back button
     * @return the handler registration used to remove the click handler from the back button.
     */
    public HandlerRegistration addBackButtonClickHandler(ClickHandler backButtonClickHandler) {
        return backButton.addClickHandler(backButtonClickHandler);
    }
    
    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        backButton.setEnabled(!isReadonly);
    }
}
