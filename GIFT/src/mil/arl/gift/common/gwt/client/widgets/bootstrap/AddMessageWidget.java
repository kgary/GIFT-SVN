/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.io.Serializable; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.constants.IconSize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.BooleanEnum;
import generated.dkf.InTutor;
import generated.dkf.Message;
import generated.dkf.Message.Delivery;
import generated.dkf.Message.Delivery.InTrainingApplication;
import generated.dkf.Message.Delivery.InTrainingApplication.MobileOption;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.MessageFeedbackDisplayModeEnum;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;

/**
 * Edits message items for an item list.
 *
 * @author nroberts
 */
public class AddMessageWidget extends ValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AddMessageWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static AddFeedbackWidgetUiBinder uiBinder = GWT.create(AddFeedbackWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AddFeedbackWidgetUiBinder extends UiBinder<Widget, AddMessageWidget> {
    }

    /** Message content box */
    @UiField
    protected TextArea messageContentBox;

    /** Training Application option */
    @UiField
    protected CheckBoxButton onlyTARadio;

    /** Vibrate mobile option */
    @UiField
    protected CheckBoxButton vibrateCheckBox;

    /** Text option */
    @UiField
    protected CheckBoxButton onlyTextRadio;

    /** Avatar option */
    @UiField
    protected CheckBoxButton onlyAvatarRadio;

    /** The button that toggles whether or not to use a delay */
    @UiField
    protected DisclosureButton delayToggleButton;

    /** The time box used to specify how long the delay should be */
    @UiField
    protected FormattedTimeBox delayTimeBox;

    /** Beep sound option */
    @UiField
    protected CheckBoxButton beepCheckBox;

    /** Flash option */
    @UiField
    protected CheckBoxButton flashCheckBox;

    /** Validation used to defining the text content of a message */
    private final WidgetValidationStatus messageContentValidation;

    /** Validation used to ensure there is a valid delay value specified */
    private final WidgetValidationStatus delayValueValidation;

    /** The current training appliation type */
    private final TrainingApplicationEnum trainingAppType;

	/** Interface to allow CSS style name access */
	interface Style extends CssResource {
		String textOnlyButton();
		String taOnlyButton();
	}

    @UiField
	protected Style style;
    
    /**
     * Creates a new editor for modifying feedback items
     * 
     * @param trainingAppType the current training application type
     */
    public AddMessageWidget(final TrainingApplicationEnum trainingAppType) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Constructing message widget");
        }

        initWidget(uiBinder.createAndBindUi(this));

        messageContentValidation = new WidgetValidationStatus(messageContentBox,
                "At least 2 characters must be entered for the message to present. Enter some text for this message.");

        delayValueValidation = new WidgetValidationStatus(delayTimeBox, "A non zero delay must be provided for an audio feedback activity.");
        
        this.trainingAppType = trainingAppType;

        //create icons for the buttons in the message panel
        setIconSize(IconSize.TIMES2);

        vibrateCheckBox.setVisible(trainingAppType == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS);

        onlyTARadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                //need to defer reading the value, since it takes a moment for the value to bubble up
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        onlyTARadio.setActive(onlyTARadio.getValue());
                        vibrateCheckBox.setVisible(trainingAppType == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS && onlyTARadio.getValue());
                    }
                });
            }
        });

        vibrateCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                //need to defer reading the value, since it takes a moment for the value to bubble up
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        vibrateCheckBox.setActive(vibrateCheckBox.getValue());
                        // always enable the onlyTARadio button if the vibrate option is checked
                        if (vibrateCheckBox.getValue() && !onlyTARadio.getValue()) {
                            onlyTARadio.setValue(true);
                        }
                    }
                });
            }
            
        });

        onlyTextRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                //need to defer reading the value, since it takes a moment for the value to bubble up
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        onlyTextRadio.setActive(onlyTextRadio.getValue());
                        beepCheckBox.setVisible(onlyTextRadio.getValue());
                        flashCheckBox.setVisible(onlyTextRadio.getValue());
                    }
                });
            }
        });

        onlyAvatarRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                // need to defer reading the value, since it takes a moment for the value to bubble up
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        onlyAvatarRadio.setActive(onlyAvatarRadio.getValue());
                    }
                });
            }
        });

        beepCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                // need to defer reading the value, since it takes a moment for the value to bubble up
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        beepCheckBox.setActive(beepCheckBox.getValue());
                    }
                });
            }
        });

        flashCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                // need to defer reading the value, since it takes a moment for the value to bubble up
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        flashCheckBox.setActive(flashCheckBox.getValue());
                    }
                });
            }
        });

        messageContentBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(messageContentValidation);
            }
        });

        manageClickEvents(onlyTARadio);
        manageClickEvents(vibrateCheckBox);
        manageClickEvents(onlyTextRadio);
        manageClickEvents(onlyAvatarRadio);
        manageClickEvents(beepCheckBox);
        manageClickEvents(flashCheckBox);
    }
    
    /**
     * Disable click events on the button when the button is disabled.
     * This is to work around the issue with GWT Bootstrap CheckBoxButton class setEnabled() 
     * method which doesn't actually do what it's supposed to. It visually disables the element, 
     * but it doesn't actually stop it from receiving click events. 
     * 
     * @param button the button to apply custom click handling to when the button is disabled
     */
    private void manageClickEvents(final CheckBoxButton button){
        
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(!button.isEnabled()){
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
    }

    public void populateEditor(Message message) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Populating feedback item");
        }

        //clear fields that might have been changed by previously edited objects
        messageContentBox.setValue(null);
        onlyAvatarRadio.setValue(true);
        onlyTextRadio.setValue(true);
        beepCheckBox.setVisible(true);
        beepCheckBox.setValue(false);
        flashCheckBox.setValue(false);
        flashCheckBox.setVisible(true);
        delayToggleButton.setValue(false);
        delayTimeBox.setValue(null);

        if (trainingAppType == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS) {
            onlyTARadio.removeStyleName(style.taOnlyButton());
            onlyTARadio.addStyleName(style.textOnlyButton());
            onlyTARadio.setValue(true);
            vibrateCheckBox.setValue(true);
            vibrateCheckBox.setVisible(true);
        } else {
            onlyTARadio.addStyleName(style.taOnlyButton());
            onlyTARadio.removeStyleName(style.textOnlyButton());
            onlyTARadio.setValue(false);
            vibrateCheckBox.setValue(false);
            vibrateCheckBox.setVisible(false);
        }

        //populate editor fields based on the object being edited
        if(message != null) {

            messageContentBox.setValue(message.getContent());

            boolean hasInTutor = message.getDelivery() != null && message.getDelivery().getInTutor() != null;

            onlyTARadio.setValue(message.getDelivery() != null
                    && message.getDelivery().getInTrainingApplication() != null
                    && message.getDelivery().getInTrainingApplication().getEnabled() != null
                    && message.getDelivery().getInTrainingApplication().getEnabled().equals(BooleanEnum.TRUE));

            if (trainingAppType == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS) {
                vibrateCheckBox.setValue(message.getDelivery() != null
                        && message.getDelivery().getInTrainingApplication() != null
                        && message.getDelivery().getInTrainingApplication().getMobileOption() != null
                        && message.getDelivery().getInTrainingApplication().getMobileOption().isVibrate());
                vibrateCheckBox.setVisible(onlyTARadio.getValue());
            }

            if(hasInTutor){

                InTutor inTutor = message.getDelivery().getInTutor();

                try{
                    MessageFeedbackDisplayModeEnum displayMode = MessageFeedbackDisplayModeEnum.valueOf(inTutor.getMessagePresentation());

                    if(displayMode.equals(MessageFeedbackDisplayModeEnum.AVATAR_AND_TEXT)){

                        onlyTextRadio.setValue(true);
                        onlyAvatarRadio.setValue(true);

                    } else if(displayMode.equals(MessageFeedbackDisplayModeEnum.TEXT_ONLY)){

                        onlyTextRadio.setValue(true);
                        onlyAvatarRadio.setValue(false);

                    } else if(displayMode.equals(MessageFeedbackDisplayModeEnum.AVATAR_ONLY)){

                        onlyAvatarRadio.setValue(true);
                        onlyTextRadio.setValue(false);

                    } else {
                        onlyAvatarRadio.setValue(false);
                        onlyTextRadio.setValue(false);
                    }

                } catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
                    //this shouldn't happen, but catch just in case so the UI can't get stuck

                }

                boolean hasNoEnhancement = inTutor.getTextEnhancement() == null;

                if(!hasNoEnhancement){

                    try{
                        TextFeedbackDisplayEnum feedbackDisplay = TextFeedbackDisplayEnum.valueOf(inTutor.getTextEnhancement());

                        if(feedbackDisplay.equals(TextFeedbackDisplayEnum.BEEP_AND_FLASH)){

                            beepCheckBox.setValue(true);
                            flashCheckBox.setValue(true);

                        } else if(feedbackDisplay.equals(TextFeedbackDisplayEnum.BEEP_ONLY)){

                            beepCheckBox.setValue(true);
                            flashCheckBox.setValue(false);

                        } else if(feedbackDisplay.equals(TextFeedbackDisplayEnum.FLASH_ONLY)){

                            beepCheckBox.setValue(false);
                            flashCheckBox.setValue(true);
                        }

                    } catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
                        //this shouldn't happen, but catch just in case so the UI can't get stuck
                    }
                }
            }else{
                // no in tutor option, de-select default selected related buttons
                onlyTextRadio.setValue(false);
                onlyAvatarRadio.setValue(false);
            }
        }
        
        if (!onlyTextRadio.getValue() && !onlyAvatarRadio.getValue() && !onlyTARadio.getValue()) {
            // no message presentation is the same as having in tutor as text and have the character
            // say the feedback
            // during course execution. Therefore select those icons by default.

            // Note: matches logic in StrategyActivityUtil.getMessagePresentationDisplay.            
            onlyTextRadio.setValue(true);
            onlyAvatarRadio.setValue(true);
        }

        beepCheckBox.setVisible(onlyTextRadio.getValue());
        flashCheckBox.setVisible(onlyTextRadio.getValue());

        //update the styling of the message panel's display buttons, since setValue doesn't do it for them automatically
        onlyTARadio.setActive(onlyTARadio.getValue());
        vibrateCheckBox.setActive(vibrateCheckBox.getValue());
        onlyTextRadio.setActive(onlyTextRadio.getValue());
        onlyAvatarRadio.setActive(onlyAvatarRadio.getValue());
        beepCheckBox.setActive(beepCheckBox.getValue());
        flashCheckBox.setActive(flashCheckBox.getValue());

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished populating feedback item");
        }
    }

    /**
     * The handler that is fired when the user toggles the
     * {@link #delayToggleButton}.
     *
     * @param event The event that specifies whether the button is now toggled
     *        on or off.
     */
    @UiHandler("delayToggleButton")
    protected void onDelayButtonToggled(ValueChangeEvent<Boolean> event) {
        requestValidation(delayValueValidation);
    }

    /**
     * The handler that is fired when the user changes the time in the
     * {@link #delayTimeBox}.
     *
     * @param event The event that contains the new delay time value in seconds.
     */
    @UiHandler("delayTimeBox")
    protected void onDelayTimeChanged(ValueChangeEvent<Integer> event) {
        requestValidation(delayValueValidation);
    }

    public void applyEdits(Message message) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Applying edits to feedback item");
        }

        message.setContent(messageContentBox.getValue());

        Delivery delivery = new Delivery();

        if(onlyTextRadio.getValue() || onlyAvatarRadio.getValue() || beepCheckBox.getValue() || flashCheckBox.getValue()) {

            InTutor inTutor = new InTutor();

            String messagePresentation = null;

            if(onlyTextRadio.getValue() && onlyAvatarRadio.getValue()) {
                messagePresentation = MessageFeedbackDisplayModeEnum.AVATAR_AND_TEXT.getName();

            } else if(onlyTextRadio.getValue()) {
                messagePresentation = MessageFeedbackDisplayModeEnum.TEXT_ONLY.getName();

            } else if(onlyAvatarRadio.getValue()) {
                messagePresentation = MessageFeedbackDisplayModeEnum.AVATAR_ONLY.getName();
            }

            inTutor.setMessagePresentation(messagePresentation);

            String textEnhancement = null;

            boolean beepSelected = beepCheckBox.isVisible() && beepCheckBox.getValue();
            boolean flashSelected = flashCheckBox.isVisible() && flashCheckBox.getValue();

            if (beepSelected && flashSelected) {
                textEnhancement = TextFeedbackDisplayEnum.BEEP_AND_FLASH.getName();

            } else if(beepSelected) {
                textEnhancement = TextFeedbackDisplayEnum.BEEP_ONLY.getName();

            } else if(flashSelected) {
                textEnhancement = TextFeedbackDisplayEnum.FLASH_ONLY.getName();

            } else {
                textEnhancement = TextFeedbackDisplayEnum.NO_EFFECT.getName();
            }

            inTutor.setTextEnhancement(textEnhancement);

            delivery.setInTutor(inTutor);
        }

        if(onlyTARadio.getValue()) {

            InTrainingApplication inTA = new InTrainingApplication();
            inTA.setEnabled(BooleanEnum.TRUE);

            if (trainingAppType == TrainingApplicationEnum.MOBILE_DEVICE_EVENTS
                    && vibrateCheckBox.getValue()) {
                MobileOption options = new MobileOption();
                options.setVibrate(vibrateCheckBox.getValue());
                inTA.setMobileOption(options);
            }

            delivery.setInTrainingApplication(inTA);
        }

        message.setDelivery(delivery);
    }

    /**
     * Gets the authored delay to wait before moving on to the next activity.
     *
     * @return The amount of time to wait in seconds.
     */
    public Integer getDelay() {
        return delayToggleButton.getValue() ? delayTimeBox.getValue() : null;
    }

    /**
     * Sets the authored delay to wait before moving on to the next activity.
     *
     * @param delayInSeconds The amount of time to wait in seconds.
     */
    public void setDelay(Integer delayInSeconds) {
        delayToggleButton.setValue(delayInSeconds != null);
        delayTimeBox.setValue(delayInSeconds);
    }
    
    /**
     * Set the visibility of the 'in training application' feedback message option.
     * @param visible true if the button should be visible, false if hidden.
     */
    public void setInTrainingAppFeedbackVisibility(boolean visible){
        onlyTARadio.setVisible(visible);
    }
    
    /**
     * Set the visibility of the 'in tutor' feedback message option.
     * @param visible true if the button should be visible, false if hidden.
     */
    public void setInTutorFeedbackVisibility(boolean visible){
        onlyAvatarRadio.setVisible(visible);
    }
    
    /**
     * Sets the icon size for the buttons
     * 
     * @param iconSize the icon size to set
     */
    public void setIconSize(IconSize iconSize) {
        if (iconSize == null) {
            iconSize = IconSize.NONE;
        }

        final String iconCssName = iconSize.getCssName();
        onlyTextRadio.setHTML("<i class='fa fa-file-text " + iconCssName + "'/>");
        onlyAvatarRadio.setHTML("<i class='fa fa-user-o " + iconCssName + "'/>");
        beepCheckBox.setHTML("<i class='fa fa-bell " + iconCssName + "'/>");
        flashCheckBox.setHTML("<i class='fa fa-sun-o " + iconCssName + "'/>");
        delayToggleButton.setIconSize(iconSize);

        /* Do best to convert to image size */
        String imgSize;
        switch (iconSize) {
        case LARGE:
            imgSize = "1.333em";
            break;
        case TIMES2:
            imgSize = "2em";
            break;
        case TIMES3:
            imgSize = "3em";
            break;
        case TIMES4:
            imgSize = "4em";
            break;
        case TIMES5:
            imgSize = "5em";
            break;
        case NONE:
            /* Intentional fall-through */
        default:
            imgSize = "1em";
        }

        onlyTARadio.setHTML(SafeHtmlUtils.fromTrustedString("<img style='width: " + imgSize + ";' src='"
                + TrainingApplicationEnum.getTrainingAppTypeIcon(trainingAppType) + "'/>"));

        vibrateCheckBox.setHTML(SafeHtmlUtils
                .fromTrustedString("<img style='width: " + imgSize + ";' src='" + "images/vibrate-phone.png" + "'/>"));
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(messageContentValidation);
        validationStatuses.add(delayValueValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if(messageContentValidation.equals(validationStatus)) {
            String message = messageContentBox.getValue();
            boolean messageNotBlank = StringUtils.isNotBlank(message);
            messageContentValidation.setValidity(messageNotBlank && message.trim().length() >= 2);
        } else if (delayValueValidation.equals(validationStatus)) {
            Integer delayTime = delayTimeBox.getValue();
            delayValueValidation.setValidity(!delayToggleButton.getValue() || delayTime != null && delayTime > 0);
        }
    }

    @Override
    protected void fireDirtyEvent(Serializable sourceObject) {
        /* Do nothing by default. This method should be overridden if you want
         * to perform a specific action when it becomes dirty. */
    }

    /**
     * Enables or disables the check box buttons. Setting the 'enabled' flag on the item itself
     * wasn't working so a workaround is to disable all pointer events.
     *
     * @param enable true to make the buttons clickable, false otherwise.
     */
    private void enableCheckBoxButtons(boolean enable) {
        onlyTARadio.setEnabled(enable);
        onlyTextRadio.setEnabled(enable);
        onlyAvatarRadio.setEnabled(enable);
        beepCheckBox.setEnabled(enable);
        flashCheckBox.setEnabled(enable);
        delayToggleButton.setReadOnly(!enable);
        delayTimeBox.setEnabled(enable);
        vibrateCheckBox.setEnabled(enable);
    }

    public void setReadonly(boolean isReadonly) {
        messageContentBox.setEnabled(!isReadonly);
        enableCheckBoxButtons(!isReadonly);
    }

    /**
     * Sets the message to a string that the user cannot modify
     * 
     * @param string the string to set the message to. If null, the message
     * box will be re-enabled so that the user can change the message again
     */
    public void setImmutableMessage(String string) {
        
        if(string != null) {
            messageContentBox.setValue(string, true);
            messageContentBox.setEnabled(false);
            messageContentBox.setTitle("The external strategy provider will provide this text");
            
        } else {
            messageContentBox.setEnabled(true);
            messageContentBox.setTitle(null);
        }
    }
}