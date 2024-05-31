/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.lti.LtiClientUtility;
import mil.arl.gift.common.gwt.client.lti.LtiConsumer;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.dashboard.client.Dashboard;

/**
 * The DataCollectionLtiPropertiesPanel was adapted from the Gat LtiPropertiesPanel class but is 
 * slightly different visually and has additional custom parameters used for data collection.
 * 
 * @author nblomberg
 *
 */
public class DataCollectionLtiPropertiesPanel extends AbstractBsWidget {
    
    private static Logger logger = Logger.getLogger(DataCollectionLtiPropertiesPanel.class.getName());

    private static LtiPropertiesPanelUiBinder uiBinder = GWT.create(LtiPropertiesPanelUiBinder.class);

    interface LtiPropertiesPanelUiBinder extends UiBinder<Widget, DataCollectionLtiPropertiesPanel> {
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
    protected Label customParamsLabel;
    
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

    /** This is the tag that is expected to be added as the custom parameter defining the path to the course. 
     * This is used in the LtiToolProviderServlet class.  */
    private static final String COURSE_ID_TAG="course_id";
    private static final String DATA_SET_ID_TAG = "data_set_id";
    private static final String EQUALS_TAG = "=";
    
    /** URL to the LTI integration wiki help page. This is retrieved from the server. */
    private String ltiHelpPageUrl = "";
    
    /**
     * Constructor
     */
    public DataCollectionLtiPropertiesPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        copyIdButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                JsniUtility.copyTextToClipboard(customParamsLabel.getElement());
                
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
    }
    
    /**
     * Initializes this widget's data so that it can display a course's LTI information to the user
     * 
     * @param courseId the courseId that refers to the course that will be run.
     * @param dataSetId the id of the published course used for tracking and collating the data for data collection.
     */
    public void init(String courseId, String dataSetId){    
        
        ServerProperties properties = Dashboard.getInstance().getServerProperties();
        

        if(properties != null){
            
            try{
                
                String customParameters = buildCustomParameters(courseId, dataSetId);
                customParamsLabel.setText(customParameters);
                
                String consumersJson = properties.getPropertyValue(ServerProperties.TRUSTED_LTI_CONSUMERS);
                
                if(consumersJson != null){
                    
                    trustedConsumersPanel.clear();
                    
                    ArrayList<LtiConsumer> consumerList = LtiClientUtility.getLtiConsumerList(consumersJson);
                    for(LtiConsumer consumer : consumerList){
                        trustedConsumersPanel.add(new DataCollectionLtiConsumerWidget(consumer));
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
    
    /**
     * Builds the custom parameter string that will be used by the Lti Tool Consumer.
     * 
     * @param courseId the courseId that refers to the course that will be run.
     * @param dataSetId the id of the published course used for tracking and collating the data for data collection.
     * @return the formatted custom parameter string used by the Lti Tool Consumer.
     */
    private String buildCustomParameters(String courseId, String dataSetId) {
       
        // edX uses quotes around each parameter in comma delimited format.
        // http://edx.readthedocs.io/projects/open-edx-building-and-running-a-course/en/latest/exercises_tools/lti_component.html
        String customParams = "[\"" + COURSE_ID_TAG + EQUALS_TAG + courseId + "\",\"" + 
                              DATA_SET_ID_TAG + EQUALS_TAG + dataSetId + "\"]";
                             
        
        return customParams;
    }
}
