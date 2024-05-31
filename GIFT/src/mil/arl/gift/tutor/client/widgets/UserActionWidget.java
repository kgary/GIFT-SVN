/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;

import generated.dkf.LearnerActionEnumType;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.TutorTest;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.UserAction;
import mil.arl.gift.tutor.shared.UserActionIconEnum;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.UserActionWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A widget for displaying a list of actions the user can take.
 *
 * @author jleonard
 */
public class UserActionWidget extends Composite {

    private final List<UserAction> userActions = new ArrayList<UserAction>();
    
    /** A panel for Tutor Me actions. */
    private FlowPanel tutorMePanel = new FlowPanel();
    
    /**
     * Constructor
     *
     * @param instance The instance of the after action review widget
     */
    public UserActionWidget(final WidgetInstance instance) {
        
        FlowPanel containerPanel = new FlowPanel();
        containerPanel.addStyleName("center");
        containerPanel.setWidth("350px");
        initWidget(containerPanel);

        userActions.add(null);
        WidgetProperties properties = instance.getWidgetProperties();
        if (properties != null) {
            List<UserAction> actions = UserActionWidgetProperties.getUserActions(properties);
            userActions.addAll(actions);
        }
        
        final ModalDialogBox infoDialog = new ModalDialogBox();
        final HTML infoMessage = new HTML("");
        infoDialog.add(infoMessage);
        infoDialog.setCloseable(true);
        infoDialog.setGlassEnabled(true);
        infoMessage.addStyleName("conversationDetails");
        infoDialog.addStyleName("detailsDialog");
        
        for (final UserAction action : userActions) {
            
            if (action != null) {
                
            	boolean isTutorAction = false;
            	FocusPanel focusPanel = new FocusPanel();
                HorizontalPanel rowPanel = new HorizontalPanel();
                String iconImage = "images/questionmark.png";
                
                if(action.getIcon() != null) {
                    if (action.getIcon().equals(UserActionIconEnum.REPORT_ICON)) {
                        iconImage = "images/report.png";
                    } else if (action.getIcon().equals(UserActionIconEnum.RADIO_ICON)) {
                        iconImage = "images/radio.png";
                    } else if (action.getIcon().equals(UserActionIconEnum.PACE_COUNT_START_ICON)) {
                        iconImage = "images/pace_start.png";
                    } else if (action.getIcon().equals(UserActionIconEnum.PACE_COUNT_END_ICON)) {
                        iconImage = "images/pace_end.png";
                    } else if (action.getIcon().equals(UserActionIconEnum.TUTOR_ME_ICON)) {
                        iconImage = "images/tutorMe.png";
                        isTutorAction = true;
                    } else if(action.getIcon().equals(UserActionIconEnum.APPLY_STRATEGY_ICON)){
                        iconImage = "images/strategy.png";
                    }
                }
                
                Image icon = new Image(iconImage);
                icon.setSize("48px", "48px");
                icon.addStyleName("middle");
                rowPanel.addStyleName("tutorMeButton");
                rowPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                
                if(isTutorAction) {
                	
                	FlowPanel infoPanel = new FlowPanel();
                	FlowPanel wrapper = new FlowPanel();
                	Button infoButton = new Button();
                	
                	infoPanel.add(new InlineLabel(action.getDisplayString()));
                	wrapper.add(icon);
                	wrapper.add(infoPanel);
                	focusPanel.add(wrapper);
                	rowPanel.add(focusPanel);
                	rowPanel.add(infoButton);
                	
                	wrapper.setWidth("100%");
                	infoButton.setIcon(IconType.INFO);
                	infoButton.addStyleName("infoButton");
                	infoPanel.getElement().getStyle().setProperty("paddingLeft", "15px");
                	infoPanel.getElement().getStyle().setProperty("fontSize", "16px");
                	infoPanel.getElement().getStyle().setProperty("display", "inline");
                	icon.getElement().getStyle().setProperty("borderRadius", "12px");
                	icon.setSize("48px", "42px");
                	 
                	infoButton.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							infoDialog.setText(action.getDisplayString());
							infoMessage.setHTML(action.getDescription());
							infoDialog.center();
						}
                		
                	});
                } else {
                	rowPanel.add(icon);
                }
                
                focusPanel.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        
                        if(instance.getWidgetProperties().isTutorTest()) { 
                            // This is a test action
                            
                            TutorTest.getInstance().handleUserAction(action);
                            
                        } else {
                            ArrayList<UserAction> actionsTaken = new ArrayList<UserAction>();
                            actionsTaken.add(action);
                            WidgetProperties properties = new WidgetProperties();
                            UserActionWidgetProperties.setUserActionsTaken(properties, actionsTaken);
                            SubmitAction submitAction = new SubmitAction(instance.getWidgetId(), properties);
                            BrowserSession.getInstance().sendActionToServer(submitAction, new AsyncCallback<RpcResponse>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    // Nothing to do
                                }

                                @Override
                                public void onSuccess(RpcResponse result) {
                                    // Nothing to do
                                }
                            });
                            
                            if(LearnerActionEnumType.TUTOR_ME.value().equals(action.getValue())){
                                TutorActionsWidget.getInstance().selectConversation(action.getDisplayString());
                            }
                        }
                        
                    }
                });
                
                focusPanel.addTouchStartHandler(new TouchStartHandler() {
                    
                    @Override
                    public void onTouchStart(TouchStartEvent event) {
                        
                        /*
                         * Nothing to do. This handler only exists to allow the proper CSS styling to be applied
                         * when the learner touches the button on a mobile device, since the :active pseudo-style
                         * isn't applied unless a touch handler is added.
                         */
                    }
                });
                
                if(isTutorAction) {
                	tutorMePanel.add(rowPanel);
                } else {
                	InlineLabel actionLabel = new InlineLabel(action.getDisplayString());
                	actionLabel.getElement().getStyle().setProperty("fontSize", "16px");
                    rowPanel.add(actionLabel);
                    focusPanel.add(rowPanel);
                	containerPanel.add(focusPanel);
                }
                
            }
        }

    }
    
    /**
     * Gets the panel containing Tutor Me learner actions.
     * 
     * @return a panel containing Tutor Me learner actions.
     */
    public FlowPanel getTutorMeActions() {
    	return tutorMePanel;
    }
}
