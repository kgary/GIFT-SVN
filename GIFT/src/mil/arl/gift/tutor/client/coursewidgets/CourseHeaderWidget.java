/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.coursewidgets;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.HideEvent;
import org.gwtbootstrap3.client.shared.event.HideHandler;
import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.shared.event.ShowEvent;
import org.gwtbootstrap3.client.shared.event.ShowHandler;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Popover;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that displays the course object title, message, navigation buttons, and continue button
 * 
 * @author bzahid
 */
public abstract class CourseHeaderWidget extends Composite {
    
    private static CourseHeaderWidgetUiBinder uiBinder = GWT.create(CourseHeaderWidgetUiBinder.class);
    
    interface CourseHeaderWidgetUiBinder extends UiBinder<Widget, CourseHeaderWidget>{
    }
    
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CourseHeaderWidget.class.getName());
    
    @UiField
    protected Label nameLabel;
    
    @UiField
    protected ButtonGroup dropDownWidget;
    
    @UiField
    protected DropDownMenu dropDownMenu;
    
    @UiField
    protected Button dropDownLabel;
    
    @UiField
    protected DeckPanel titleDeckPanel;
    
    @UiField
    protected Button infoButton;
    
    @UiField
    protected Button continueButton;
    
    @UiField
    protected Button nextButton;
    
    @UiField
    protected Tooltip nextButtonTooltip;
        
    @UiField
    protected Button previousButton;
    
    @UiField
    protected Tooltip previousButtonTooltip;
    
    @UiField
    protected Popover continuePopup;
    
    @UiField
    protected Icon loadingIcon;
    
    @UiField
    protected Icon warningIcon;
        
    protected CourseMessageDialog dialog;
    
    private HandlerRegistration infoHandler;
    
    private HandlerRegistration nextHandler;
    
    private HandlerRegistration previousHandler;
    
    protected String pageId = null;
            
    protected ContinueHandler continueCallback = null;
    
    private static boolean showContinue = true;
    
    private boolean iframeAdded = false;
    
    private boolean popupVisible = false;
    
    private static CourseHeaderWidget instance = null;
    
    /**
     * Constructor
     */
    public CourseHeaderWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        showContinue = true;
        instance = this;
        
        warningIcon.setVisible(false);
        titleDeckPanel.showWidget(titleDeckPanel.getWidgetIndex(nameLabel));
        
        dropDownLabel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dropDownMenu.setVisible(true);
                
                if(!iframeAdded) {
                    // This is needed because for some reason, the iframe is not actually added to 
                    // the dom when this logic is placed in the ui.xml file or elsewhere in the constructor
                    HTML html = new HTML();
                    html.setHTML("<iframe style=\"top: 0px; width: 101%; height: 101%; position: absolute; z-index: -100; border: none;\"></iframe>");
                    dropDownMenu.add(html);
                    iframeAdded = true;
                }
            }
            
        });
        
        dialog = new CourseMessageDialog();
        dialog.addCloseHandler(new ModalHideHandler() {

            @Override
            public void onHide(ModalHideEvent event) {
                Animate.animate(infoButton, Animation.BOUNCE_IN);
            }
            
        });
        
        continuePopup.addShowHandler(new ShowHandler() {

            @Override
            public void onShow(ShowEvent event) {
                popupVisible = true;
            }
        });
        
        continuePopup.addHideHandler(new HideHandler() {
            
            @Override
            public void onHide(HideEvent event) {
                popupVisible = false;
            }
        });
        
        continueButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(continueCallback != null) {
                    
                    continueCallback.onClick();
                    
                    if(continueCallback.getShouldStop()){
                    	
                    	//the normal continue logic needs to be bypassed, so return prematurely
                        return;
                    }
                }
                
                handleContinue();
            }
        });
        
        previousButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                //hide the previous button's tooltip after the learner taps it on a mobile device
                previousButtonTooltip.hide();
            }
        });
           
        nextButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                //hide the next button's tooltip after the learner taps it on a mobile device
                nextButtonTooltip.hide();
            }
        });
    }
    
    /**
     * Gets the instance of the current widget
     */
    public static CourseHeaderWidget getInstance() {
        return instance;
    }
    
    public abstract void setHeaderTitle(String name);
    
    public abstract void handleContinue();
        
    public void setName(String name) {
        if(name != null && name.length() > 50) {
            name = name.substring(0, 50) + "...";
        }
        nameLabel.setText(name);
        dropDownLabel.setText(name);
    }
    
    /**
     * Adds a link to the drop down menu. Enables the drop down 
     * button if necessary.
     * 
     * @param item The drop down link to add.
     */
    public void addDropDownLink(AnchorListItem item) {
        dropDownMenu.add(item);
        if(dropDownMenu.getWidgetCount() > 1) {
            setDropDownEnabled(true);
        }
    }
    
    /**
     * Clears the drop down menu and sets it to active or inactive.
     * 
     * @param enabled true to show the drop down menu, false to display a title only
     */
    public void setDropDownEnabled(boolean enabled) {
        if(enabled) {
            titleDeckPanel.showWidget(titleDeckPanel.getWidgetIndex(dropDownWidget));
        } else {
            titleDeckPanel.showWidget(titleDeckPanel.getWidgetIndex(nameLabel));
            dropDownMenu.clear();
        }
    }
    
    /**
     * Sets the course object information message
     * 
     * @param message The message to display
     * @param show True to show the dialog immediately, false to allow the dialog to be shown manually.
     */
    public void setInfoMessage(final String message, boolean show) {
        HTML messageHtml = new HTML(message);
        if(messageHtml.getText() != null && !messageHtml.getText().isEmpty()) {
        
            infoButton.setVisible(true);
            if(infoHandler != null) {
                infoHandler.removeHandler();
            }
            
            infoHandler = infoButton.addClickHandler(new ClickHandler() {
    
                @Override
                public void onClick(ClickEvent event) {
                    dialog.showMessage(message);
                }
            });
            
            if(show) {
                infoButton.click();
            }
        }
    }
    
    public void clearInfoMessage() {
        if(infoHandler != null) {
            infoHandler.removeHandler();
        }
        
        infoButton.setVisible(false);
        
        if(dialog.isVisible()) {
            dialog.hide();
        }
    }
    
    public void showPreviousButton(ClickHandler clickHandler, String tooltip) {
        if(previousHandler != null) {
            previousHandler.removeHandler();
        }
        previousHandler = previousButton.addClickHandler(clickHandler);
        previousButton.setVisible(true);
        
        previousButtonTooltip.setTitle(tooltip);
    }
    
    public void showNextButton(ClickHandler clickHandler, String tooltip) {
        if(nextHandler != null) {
            nextHandler.removeHandler();
        }
        nextHandler = nextButton.addClickHandler(clickHandler);
        nextButton.setVisible(true);
        nextButton.setEnabled(true);
        
        nextButtonTooltip.setTitle(tooltip);
    }
    
    public void setPreviousButtonEnabled(boolean enabled) {
        previousButton.setEnabled(enabled);
        
        //if disabling the button make sure to hide any currently shown tooltip, otherwise it won't animate away on its own
        if(!enabled){
            previousButtonTooltip.hide();
        }
    }
    
    public void setNextButtonEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        
        //if disabling the button make sure to hide any currently shown tooltip, otherwise it won't animate away on its own
        if(!enabled){
            nextButtonTooltip.hide();
        }
    }
    
    public void hidePreviousButton() {
        if(previousHandler != null) {
            previousHandler.removeHandler();
        }
        previousButton.setEnabled(false);
        previousButton.setVisible(false);
    }
    
    public void hideNextButton() {
        if(nextHandler != null) {
            nextHandler.removeHandler();
        }
        nextButton.setVisible(false);
    }
        
    /**
     * Hides the header widget and additional buttons.
     */
    public void hide() {
        hideNextButton();
        hidePreviousButton();
        clearInfoMessage();
        setDropDownEnabled(false);
        setVisible(false);
    }
    
    /**
     * Shows the header widget. If the header widget hasn't been shown before,
     * a tooltip appears next to the continue button.
     */
    public void show() {
        setVisible(true);
        
        if(showContinue) {
            
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    continuePopup.toggle();
                    
                    Timer timer = new Timer() {
                        @Override 
                        public void run() {
                            if(popupVisible) {
                                continuePopup.hide();
                            }
                        }
                    };
                    
                    timer.schedule(4000);
                    showContinue = false;
                }
            });
        }
    }
    
    /**
     * Enables or disables the continue button
     * 
     * @param enabled True to enable the continue button, false otherwise.
     */
    public void setContinueButtonEnabled(boolean enabled) {
        continueButton.setEnabled(enabled);
    }
    
    /**
     * Sets the pageId of the current widget being displayed. This is used when the
     * continue button is clicked and the widget is closed.
     * 
     * @param pageId The current widget id 
     */
    public void setContinuePageId(String pageId) {
        this.pageId = pageId;
    }
    
    /**
     * Sets a callback to execute when the continue button is clicked
     * 
     * @param callback The callback to execute
     */
    public void setContinueButtonCallback(ContinueHandler callback) {
        continueCallback = callback;
    }
    
    /**
     * Shows or hides a loading indicator near the header widget
     * @param loading True to show the loading indicator, false otherwise
     */
    public void showLoading(boolean loading) {
        loadingIcon.setPulse(loading);
        loadingIcon.setVisible(loading);
    }
    
    /**
     * Displays a warning icon in front of the course object name in the header widget.
     * This should be used if there is a problem with the course object being previewed.
     * 
     * @param show True to show a warning icon, false otherwise.
     */
    public void showWarning(boolean show) {
        warningIcon.setVisible(show);
    }
    
    /**
     * A handler that handles when the continue button is clicked
     * 
     * @author nroberts
     */
    public static abstract class ContinueHandler {
        
        /** Whether or not the continue button logic should terminate after this handler is invoked */
        private boolean shouldStop = false;
        
        public abstract void onFailure();
        
        public abstract void onClick();

        /**
         * Gets whether or not the continue button logic should terminate after this handler is invoked
         * 
         * @return whether or not the continue button logic should terminate
         */
        public boolean getShouldStop() {
            return shouldStop;
        }

        /**
         * Sets whether or not the continue button logic should terminate after this handler is invoked
         * 
         * @param shouldContinue whether or not the continue button logic should terminate
         */
        public void setShouldStop(boolean shouldStop) {
            this.shouldStop = shouldStop;
        }
    }
}
