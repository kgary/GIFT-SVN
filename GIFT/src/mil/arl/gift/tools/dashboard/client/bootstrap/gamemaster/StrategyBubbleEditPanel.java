/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.DelayAfterStrategy;
import generated.dkf.Feedback;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Message;
import generated.dkf.Strategy;
import generated.dkf.Team;
import generated.dkf.TeamRef;
import mil.arl.gift.common.SaveCancelCallback;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationStatusChangedCallback;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AddMessageWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamPicker;
import mil.arl.gift.common.util.StringUtils;

/**
 * A display bubble to allow the user to select the strategy and its activities.
 *
 * @author sharrison
 */
public class StrategyBubbleEditPanel extends ValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyBubbleEditPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StrategyBubbleEditPanelUiBinder uiBinder = GWT.create(StrategyBubbleEditPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StrategyBubbleEditPanelUiBinder extends UiBinder<Widget, StrategyBubbleEditPanel> {
    }

    /** Validation widget */
    private final ValidationWidget validations = new ValidationWidget(this);

    /** The main edit panel */
    @UiField
    protected FlowPanel editPanel;

    /** The panel containing the widgets to edit the strategy name */
    @UiField
    protected FlowPanel strategyNamePanel;

    /** The text box used to edit the strategy name */
    @UiField
    protected TextBox strategyNameTextBox;

    /** The widget used to add feedback messages */
    @UiField(provided = true)
    protected AddMessageWidget messageWidget;

    /** The widget for picking team members */
    protected TeamPicker teamPicker;
    
	/** The panel containing the widget for picking team members */
    @UiField(provided = true)
    protected FlowPanel teamPickerPanel;

    /** The error message to show the user if there is a validation error */
    @UiField
    protected HTML errorDetailMsg;

    /** The panel containing the action buttons */
    @UiField
    protected FlowPanel buttonPanel;

    /** Button to save the values on the {@link #editPanel} */
    @UiField
    protected Button saveBtn;

    /** Button to cancel any changes */
    @UiField
    protected Button cancelBtn;

    /** The callback to execute if save or cancel is clicked */
    private final SaveCancelCallback saveCancelCallback;

    /** The parent strategy of the {@link #iiActivity} */
    private final Strategy strategy;

    /** The strategy activity to edit */
    private final InstructionalIntervention iiActivity;

    /** Validate against the strategy name text box */
    private final WidgetValidationStatus strategyNameValidation;

    /**
     * Constructor.
     * 
     * @param parentStrategy the parent {@link Strategy}. Can't be null.
     * @param iiActivity the {@link InstructionalIntervention} activity to edit
     *        from the {@link #strategy}. Can't be null. Must contain a
     *        {@link Message} feedback presentation.
     * @param trainingAppType the training application type of the application
     *        in use when this session was created. Can't be null.
     * @param team the structure of the team for the session.
     * @param editStrategyName determines if the edit panel should include a
     *        field for the user to edit the strategy name. True to edit; false
     *        otherwise.
     * @param callback the callback to be executed when the user saves or
     *        cancels the edit panel
     */
    public StrategyBubbleEditPanel(Strategy parentStrategy, InstructionalIntervention iiActivity,
            TrainingApplicationEnum trainingAppType, Team team, boolean editStrategyName, SaveCancelCallback callback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        if (parentStrategy == null) {
            throw new IllegalArgumentException("The parameter 'parentStrategy' cannot be null.");
        } else if (iiActivity == null) {
            throw new IllegalArgumentException("The parameter 'iiActivity' cannot be null.");
        } else if (iiActivity.getFeedback() == null
                || !(iiActivity.getFeedback().getFeedbackPresentation() instanceof Message)) {
            throw new IllegalArgumentException("The parameter 'iiActivity' must have a Message feedback");
        } else if (trainingAppType == null) {
            throw new IllegalArgumentException("The parameter 'trainingAppType' cannot be null.");
        }

        messageWidget = new AddMessageWidget(trainingAppType);
        teamPickerPanel = new FlowPanel();
        if(team != null) {
            teamPicker = new TeamPicker(team);
            teamPicker.setLabel("Learners that this feedback should be presented to (Optional):");
            teamPickerPanel.add(teamPicker);
        } 

        this.strategy = parentStrategy;
        this.iiActivity = iiActivity;
        this.saveCancelCallback = callback;

        initWidget(uiBinder.createAndBindUi(this));
        strategyNameValidation = new WidgetValidationStatus(strategyNameTextBox, "The strategy name can't be blank");

        this.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        strategyNameTextBox.setValue(strategy.getName());
        if (editStrategyName) {
            strategyNameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    requestValidation(strategyNameValidation);
                }
            });
        } else {
            strategyNamePanel.removeFromParent();
        }

        final Feedback feedback = iiActivity.getFeedback();
        final Message feedbackMessage = (Message) feedback.getFeedbackPresentation();
        messageWidget.populateEditor(feedbackMessage);

        /* The delay is populated separately */
        DelayAfterStrategy delayAfterStrategy = iiActivity.getDelayAfterStrategy();
        if (delayAfterStrategy != null && delayAfterStrategy.getDuration() != null) {
            messageWidget.setDelay(delayAfterStrategy.getDuration().intValue());
        }

        List<String> teamNames = new ArrayList<>();
        for (TeamRef ref : feedback.getTeamRef()) {
            teamNames.add(ref.getValue());
        }
        if(teamPicker != null) {
        	teamPicker.setValue(teamNames);
        }

        saveBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                performSave();
            }
        });

        cancelBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();

                /* Execute the cancel callback */
                if (saveCancelCallback != null) {
                    saveCancelCallback.cancel();
                }
            }
        });

        addValidationStatusChangedCallback(new ValidationStatusChangedCallback() {
            @Override
            public void changedValidity(boolean isValid, boolean fireEvents) {
                for (int i = 0; i < buttonPanel.getWidgetCount(); i++) {
                    Button button = (Button) buttonPanel.getWidget(i);
                    if (button == cancelBtn) {
                        continue;
                    }

                    button.setEnabled(isValid);
                }

                errorDetailMsg.setVisible(!isValid);
            }
        });

        /* Add validation widget last */
        initValidationComposite(validations);
        validateAll();
    }

    /**
     * Saves the values on this panel to their backing data models.
     */
    private void performSave() {
        strategy.setName(strategyNameTextBox.getValue());

        Feedback feedback = iiActivity.getFeedback();
        Message feedbackMessage = (Message) feedback.getFeedbackPresentation();

        /* If a message has been defined, save it to the feedback message */
        messageWidget.applyEdits(feedbackMessage);

        /* If a delay has been defined, save it to the instructional
         * intervention */
        Integer delayAmount = messageWidget.getDelay();
        if (delayAmount != null) {
            DelayAfterStrategy delay = new DelayAfterStrategy();
            delay.setDuration(new BigDecimal(delayAmount));
            iiActivity.setDelayAfterStrategy(delay);
        } else {
            iiActivity.setDelayAfterStrategy(null);
        }

        /* If team members have been defined, save it to the feedback */
        feedback.getTeamRef().clear();
        if(teamPicker != null) {
	        final List<String> teamMembers = teamPicker.getValue();
	        if (teamMembers != null) {
	            for (String teamName : teamMembers) {
	                TeamRef ref = new TeamRef();
	                ref.setValue(teamName);
	                feedback.getTeamRef().add(ref);
	            }
	        }
        }

        /* Execute the save callback */
        if (saveCancelCallback != null) {
            saveCancelCallback.save();
        }
    }

    /**
     * Show the 'save and apply' button. This button performs a save then
     * executes the provided apply command.
     * 
     * @param buttonText the text to display in the button
     * @param makeButtonFirst true to have the button inserted before any
     *        others; false to have the button show up after any others.
     * @param applyCommand the command to execute after save is complete.
     */
    public void addSaveAndApplyButton(String buttonText, boolean makeButtonFirst, final Command applyCommand) {
        Button saveAndApplyBtn = new Button(buttonText, IconType.CHECK_SQUARE, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                performSave();

                /* 'Apply' the strategy */
                if (applyCommand != null) {
                    applyCommand.execute();
                }
            }
        });
        saveAndApplyBtn.setType(ButtonType.PRIMARY);
        saveAndApplyBtn.setEnabled(isValid());
        saveAndApplyBtn.getElement().getStyle().setMargin(4, Unit.PX);

        if (makeButtonFirst) {
            buttonPanel.insert(saveAndApplyBtn, 0);
        } else {
            buttonPanel.add(saveAndApplyBtn);
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(strategyNameValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (strategyNameValidation.equals(validationStatus)) {
            strategyNameValidation.setValidity(
                    !strategyNameTextBox.isAttached() || StringUtils.isNotBlank(strategyNameTextBox.getValue()));
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(messageWidget);
    }

    @Override
    protected void fireDirtyEvent(Serializable sourceObject) {
        // do nothing
    }
}