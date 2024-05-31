/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.lti.LtiConsumer;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;

/**
 * The DataCollectionLtiConsumerWidget was adapted from the LtiConsumerWidget class in the gat.  The 
 * differences are in the custom parameters required for data collection.
 * @author nblomberg
 *
 */
public class DataCollectionLtiConsumerWidget extends AbstractBsWidget {
    
    private static Logger logger = Logger.getLogger(DataCollectionLtiConsumerWidget.class.getName());

    private static DataCollectionLtiConsumerWidgetUiBinder uiBinder = GWT.create(DataCollectionLtiConsumerWidgetUiBinder.class);

    interface DataCollectionLtiConsumerWidgetUiBinder extends UiBinder<Widget, DataCollectionLtiConsumerWidget> {
    }

    @UiField
    protected Label nameLabel;
    
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
    
    /**
     * Creates a new widget for displaying LTI consumer data
     * 
     * @param consumer
     */
    public DataCollectionLtiConsumerWidget(LtiConsumer consumer) {
        
        initWidget(uiBinder.createAndBindUi(this));
        
        try{
            nameLabel.setText(consumer.getName());
            keyLabel.setText(consumer.getKey());
            sharedSecretLabel.setText(consumer.getSharedSecret());
            
        } catch(Exception e){
            
            //improperly formatted consumers could throw JavaScriptExceptions, so catch them just in case
            logger.severe(e.toString());
        }
        
        mainPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                mainCollapse.toggle();
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
    }

}
