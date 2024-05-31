/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.provider;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.lti.LtiClientUtility;
import mil.arl.gift.common.gwt.client.lti.LtiConsumer;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.lti.LtiAuthenticationWidget;

/**
 * A widget used to display a course's LTI provider properties to the user
 * 
 * @author nroberts
 */
public class LtiProviderPropertiesPanel extends Composite {
    
    private static Logger logger = Logger.getLogger(LtiProviderPropertiesPanel.class.getName());

    private static LtiProviderPropertiesPanelUiBinder uiBinder = GWT.create(LtiProviderPropertiesPanelUiBinder.class);

    interface LtiProviderPropertiesPanelUiBinder extends UiBinder<Widget, LtiProviderPropertiesPanel> {
    }

    @UiField
    protected Label launchUrlLabel;
    
    @UiField
    protected Button copyUrlButton;
    
    @UiField
    protected FocusPanel collapseUrlButton;
    
    @UiField
    protected Collapse urlCollapse;
    
    @UiField
    protected Label courseIdLabel;
    
    @UiField
    protected Button copyIdButton;
    
    @UiField
    protected FocusPanel collapseIdButton;
    
    @UiField
    protected Collapse idCollapse;
    
    @UiField
    protected FlowPanel trustedConsumersPanel;
    
    @UiField
    protected Button ltiHelpButton;
    
    @UiField
    protected FlowPanel noAccessPanel;
    
    @UiField
    protected FlowPanel accessPanel;
    
    @UiField
    protected HTMLPanel launchUrlTextPanel;
    
    @UiField
    protected Tooltip launchUrlTooltip;
    
    @UiField
    protected HTMLPanel customParametersTextPanel;
    
    @UiField
    protected Tooltip customParametersTooltip;
    
    /** This is the tag that is expected to be added as the custom parameter defining the path to the course. 
     * This is used in the LtiToolProviderServlet class.  */
    private static final String COURSE_ID_TAG="course_id";
    private static final String EQUALS_TAG = "=";
    
    /** URL to the LTI integration wiki help page. This is retrieved from the server. */
    private String ltiHelpPageUrl = "";
    
    /**
     * Constructor.
     */
    public LtiProviderPropertiesPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        copyIdButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                JsniUtility.copyTextToClipboard(courseIdLabel.getElement());
                
                event.stopPropagation();
            }
        });
        
        copyUrlButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                JsniUtility.copyTextToClipboard(launchUrlLabel.getElement());
                
                event.stopPropagation();
            }
        });
        
        collapseUrlButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                urlCollapse.toggle();
            }
        });
        
        collapseIdButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                idCollapse.toggle();
            }
        });
        
        ltiHelpButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                Window.open(ltiHelpPageUrl, "_blank", "");
            }
        });
        
        customParametersTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                customParametersTooltip.hide();
                
            }
        }, MouseOutEvent.getType());
        
        customParametersTextPanel.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                customParametersTooltip.show();
                
            }
        }, MouseOverEvent.getType());
        
        launchUrlTextPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //hide the tooltip when the non-button part of the panel is moused out of
                launchUrlTooltip.hide();
                
            }
        }, MouseOutEvent.getType());
        
        launchUrlTextPanel.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //show the tooltip when the non-button part of the panel is hovered over
                launchUrlTooltip.show();
                
            }
        }, MouseOverEvent.getType());
        
        copyIdButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
            
        });
        
        copyUrlButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
            
        });
        
        copyIdButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
            
        });
        
        copyUrlButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //prevent mouse events from registering up the hierarchy
                event.stopPropagation();
                
            }
            
        });
    }
    
    /**
     * Initializes this widget's data so that it can display a course's LTI information to the user
     * 
     * @param coursePath the path to the course whose LTI information should be displayed
     */
    public void init(String courseId){    
        
        
        
        // If the course id is null or empty, it means the user does not have
        // permissions to view the lti properties.  In this case, the 'no access' panel should
        // be shown informing the user why the properties are not available.
        if (courseId == null || courseId.isEmpty()) {
            noAccessPanel.setVisible(true);
            accessPanel.setVisible(false);
        } else {
            
            // If the course id is valid, then display the lti properties.
            noAccessPanel.setVisible(false);
            accessPanel.setVisible(true);
            
            ServerProperties properties = GatClientUtility.getServerProperties();
            if(properties != null){
                
                try{
                    
                    courseIdLabel.setText("[\"" + COURSE_ID_TAG + EQUALS_TAG + courseId + "\"]");
                    
                    String consumersJson = properties.getPropertyValue(ServerProperties.TRUSTED_LTI_CONSUMERS);
                    
                    if(consumersJson != null){
                        
                        trustedConsumersPanel.clear();

                        ArrayList<LtiConsumer> consumerList = LtiClientUtility.getLtiConsumerList(consumersJson);
                        for(LtiConsumer consumer : consumerList){
                            trustedConsumersPanel.add(new LtiAuthenticationWidget(consumer));
                        }
                    }
                    
                    ltiHelpPageUrl = properties.getPropertyValue(ServerProperties.LTI_HELP_PAGE_URL);
                    
                } catch(Exception e){
                    
                    //improperly formatted consumers could throw JavaScriptExceptions, so catch them just in case
                    logger.severe(e.toString());
                }
                
                launchUrlLabel.setText(properties.getLtiUrl());
            }
        }
        
    }
}
