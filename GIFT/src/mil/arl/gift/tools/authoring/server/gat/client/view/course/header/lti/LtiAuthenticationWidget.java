/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.course.BooleanEnum;
import generated.course.LtiProvider;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.lti.LtiConsumer;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

/**
 * A widget used to represent trusted LTI consumers and display their information to users so that
 * it can be copied
 * 
 * @author nroberts
 */
public class LtiAuthenticationWidget extends Composite {

    private static Logger logger = Logger.getLogger(LtiAuthenticationWidget.class.getName());

    private static LtiAuthenticationWidgetUiBinder uiBinder = GWT.create(LtiAuthenticationWidgetUiBinder.class);

    interface LtiAuthenticationWidgetUiBinder extends UiBinder<Widget, LtiAuthenticationWidget> {
    }

    @UiField
    protected Label identifierLabel;
    
    @UiField
    protected Tooltip identifierTooltip;

    @UiField
    protected Label keyLabel;

    @UiField
    protected Button copyKeyButton;

    @UiField
    protected FocusPanel collapseKeyButton;

    @UiField
    protected Collapse keyCollapse;

    @UiField
    protected Label sharedSecretLabel;

    @UiField
    protected Button copySecretButton;

    @UiField
    protected FocusPanel collapseSecretButton;

    @UiField
    protected Collapse secretCollapse;

    @UiField
    protected FocusPanel mainPanel;

    @UiField
    protected Collapse mainCollapse;
    
    @UiField
    protected Icon arrowIcon;
    
    @UiField
    protected Tooltip keyTooltip;
    
    @UiField
    protected HTMLPanel keyTextPanel;
    
    @UiField
    protected Tooltip sharedSecretTooltip;
    
    @UiField
    protected HTMLPanel sharedSecretTextPanel;
    
    /**
     * Toggle switch to tell if the panel is open or not.
     */
    private boolean open = false;
    
    /** String value shown to the user when the LTI data needs to be hidden */
    private final static String PROTECTED_LTI_DATA = "**protected**";

    /**
     * Creates a new widget for displaying LTI consumer data
     * 
     * @param consumer
     */
    public LtiAuthenticationWidget(LtiConsumer consumer) {
        init(consumer.getName(), consumer.getKey(), consumer.getSharedSecret(), null);
    }

    /**
     * Creates a new widget for displaying LTI provider data
     * 
     * @param provider
     */
    public LtiAuthenticationWidget(LtiProvider provider) {
        init(provider.getIdentifier(), provider.getKey(), provider.getSharedSecret(), provider.getProtectClientData());
    }

    /**
     * Creates a new widget for displaying LTI data
     * 
     * @param name the identifier name.
     * @param key the client key value.
     * @param sharedSecret the shared client secret value.
     * @param protectLtiData true to protect/hide the sensitive LTI data.
     */
    private void init(String name, String key, String sharedSecret, BooleanEnum protectLtiData) {
        initWidget(uiBinder.createAndBindUi(this));
        
        try {
            identifierLabel.setText(name);
            if (GatClientUtility.isReadOnly() && BooleanEnum.TRUE.equals(protectLtiData)) {
                keyLabel.setText(PROTECTED_LTI_DATA);
                sharedSecretLabel.setText(PROTECTED_LTI_DATA);
            } else {
                keyLabel.setText(key);
                sharedSecretLabel.setText(sharedSecret);
            }
        } catch (Exception e) {

            // improperly formatted consumers could throw JavaScriptExceptions, so catch them just
            // in case
            logger.log(Level.SEVERE, "Wasn't able to set the labels for the LTI Authentication widget: " + e.getMessage(), e);
        }

        mainPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mainCollapse.toggle();
                
                if (open) {
                    arrowIcon.setType(IconType.CHEVRON_RIGHT);
                }else {
                    arrowIcon.setType(IconType.CHEVRON_DOWN);
                }
                
                open = !open;
            }
        });

        copyKeyButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                JsniUtility.copyTextToClipboard(keyLabel.getElement());

                event.stopPropagation();
            }
        });

        collapseKeyButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                keyCollapse.toggle();
            }
        });

        copySecretButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                JsniUtility.copyTextToClipboard(sharedSecretLabel.getElement());

                event.stopPropagation();
            }
        });

        collapseSecretButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                secretCollapse.toggle();
            }
        });
        
        copyKeyButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
        });
        
        copyKeyButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
        });
        
        keyTextPanel.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                keyTooltip.show();
                
            }
        }, MouseOverEvent.getType());
        
        keyTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                keyTooltip.hide();
                
            }
        }, MouseOutEvent.getType());
        
        copySecretButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
        });
        
        copySecretButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
        });
        
        sharedSecretTextPanel.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                sharedSecretTooltip.show();
                
            }
        }, MouseOverEvent.getType());
        
        sharedSecretTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                sharedSecretTooltip.hide();
                
            }
        }, MouseOutEvent.getType());
    }
}
