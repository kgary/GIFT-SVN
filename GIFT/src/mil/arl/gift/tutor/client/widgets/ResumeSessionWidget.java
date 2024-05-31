/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.TutorUserWebInterface;
import mil.arl.gift.tutor.client.WidgetFactory;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.SelectDomainWidgetProperties;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that displays the options for a currently active domain session
 * 
 * @author bzahid
 */
public class ResumeSessionWidget extends Composite {

	interface ResumeSessionWidgetUiBinder extends UiBinder<Widget, ResumeSessionWidget> {
	}
	
	private static ResumeSessionWidgetUiBinder uiBinder = GWT.create(ResumeSessionWidgetUiBinder.class);
	
	@UiField
	Label displayLabel;
	
	@UiField
	Button stopButton;
	
	@UiField
	Button resumeButton;
		
	@UiField
	Button logoutButton;
		
	/**
	 * Constructor
	 * 
	 * @param instance The web page instance of the select domain page.
	 */
	public ResumeSessionWidget(WidgetInstance instance) {
		
		History.newItem(TutorUserWebInterface.SELECT_DOMAIN_TAG, false);
		
		final String activeCourseName = SelectDomainWidgetProperties.getActiveDomainSessionName(instance.getWidgetProperties());
		final boolean returnToDomainSessionOnExit = instance.getWidgetProperties() != null
				? SelectDomainWidgetProperties.getReturnToDomainSessionOnExit(instance.getWidgetProperties())
				: false;
				
        
		initWidget(uiBinder.createAndBindUi(this));				
		
        resumeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BrowserSession.getInstance().resumeDomainSession();
            }
        });

        stopButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BrowserSession.getInstance().endDomainSession();
            }
        });
        
        displayLabel.setText("The course '" + activeCourseName + "' is already active.");
        
        if (!returnToDomainSessionOnExit) {
        	logoutButton.setVisible(true);
            logoutButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    
                    logoutButton.setEnabled(false);
                    if (BrowserSession.getInstance() != null) {
                        BrowserSession.getInstance().logout(new AsyncCallback<RpcResponse>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                logoutButton.setEnabled(true);
                            }

                            @Override
                            public void onSuccess(RpcResponse result) {
                                logoutButton.setEnabled(true);
                            }
                        });
                    } else {
                        Document.getInstance().displayError("Logging out", "The browser session is invalid");
                        try {
                            //change the login widget type to the type that was used to get to the login page
                            Document.getInstance().setArticleWidget(WidgetFactory.createWidgetType(BrowserSession.getLoginType()));
                        } catch (@SuppressWarnings("unused") Exception ex) {
                            Document.getInstance().displayError("Logging out", "Cannot create the login widget");
                        }
                    }
                }
            });
        }

        instance.getWidgetProperties().setIsFullscreen(true);
        
        // The load button should only be used for debugging and is not intended to be used
        // in release builds.  This button allows the user to manually trigger the load of any
        // save file that may exist.  
        /* $TODO$ nblomberg 
         * Look to make this configurable or only enabled in debug builds.  Currently we are commenting
         * this button out, and if needed it can be commented back in.  Eventually we will make this configurable
         * or remove altogether if no longer needed.
        Button loadButton = new Button("DEBUG - Load");
        saveButton.setWidth("100px");
        FlowPanel loadContainer = new FlowPanel();
        saveContainer.add(loadButton);
        saveContainer.setWidth("100px");
        saveContainer.addStyleName("smallMargin");
        saveContainer.addStyleName("right");
        add(loadContainer);
        
        loadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BrowserSession.getInstance().loadDomainSession();
            }
        });
        */

        
	}
}
