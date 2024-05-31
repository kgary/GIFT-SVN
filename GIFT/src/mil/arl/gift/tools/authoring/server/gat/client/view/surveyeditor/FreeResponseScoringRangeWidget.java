/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.DoubleBox;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyResponseTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;

/**
 * The FreeResponseScoringRangeWidget allows the author to specify the score and a numerical range
 * for each authored condition within a FreeResponseScoringWidget. If no numerical range is visible,
 * a catch-all label will be displayed.
 * 
 * @author sharrison
 *
 */
public class FreeResponseScoringRangeWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(FreeResponseScoringRangeWidget.class.getName());

    /** UI Binder */
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** UI Binder interface */
    interface WidgetUiBinder extends UiBinder<Widget, FreeResponseScoringRangeWidget> {
    }

    @UiField
    protected DoubleBox pointBox;

    @UiField
    protected FlowPanel entireRangePanel;

    @UiField
    protected TextBox minRange;

    @UiField
    protected FlowPanel maxRangePanel;

    @UiField
    protected TextBox maxRange;

    @UiField
    protected CheckBox rangeCheckBox;

    @UiField(provided = true)
    protected Image removeButton = new Image(GatClientBundle.INSTANCE.cancel_image());

    @UiField
    protected Label catchAllLabel;
    
    private final SurveyResponseTypeEnum responseType;
    
    /** The color to indicate that the scoring condition has a conflict */
    private static final String CONFLICT_COLOR = "pink";

    /**
     * Constructor. The remove button will be missing a Click Handler using this constructor. Please
     * add one after initialization using getRemoveButton().addClickHandler().
     * 
     * @param responseType the {@link SurveyResponseTypeEnum} of this widget. Can't be null.
     * @param onChangeCommand the command that will be executed when the point box or range values
     *            are changed. Can't be null.
     */
    public FreeResponseScoringRangeWidget(SurveyResponseTypeEnum responseType, final Command onChangeCommand) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("FreeResponseScoringRangeWidget(");
            List<Object> params = Arrays.<Object>asList(responseType, onChangeCommand);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (responseType == null) {
            throw new IllegalArgumentException("Response type cannot be null.");
        } else if (onChangeCommand == null) {
            throw new IllegalArgumentException("The parameter 'onChangeCommand' cannot be null.");
        }
        
        initWidget(uiBinder.createAndBindUi(this));
        
        this.responseType = responseType;
        
        // default to zero
        pointBox.setValue(Double.valueOf(0), false);
        pointBox.addValueChangeHandler(new ValueChangeHandler<Double>() {

            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if (pointBox.getValue() == null) {
                    pointBox.setValue(0.0);
                }

                onChangeCommand.execute();
            }
        });

        minRange.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onChangeCommand.execute();
            }
        });

        maxRange.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onChangeCommand.execute();
            }
        });

        addNumericRegex(minRange);
        addNumericRegex(maxRange);

        rangeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (event.getValue()) {
                    maxRangePanel.setVisible(true);
                } else {
                    // hide the panel containing the maxRange
                    maxRange.setValue(null);
                    maxRangePanel.setVisible(false);
                }

                onChangeCommand.execute();
            }
        });

        // by default, hide the catch all label and the max range panel
        catchAllLabel.setVisible(false);
        maxRangePanel.setVisible(false);
        setMinInvalid(true);
    }

    /**
     * Checks if the text box is blank.
     * 
     * @param textBox the text box to check
     * @return true if the field is blank.
     */
    private boolean isTextBoxBlank(TextBox textBox) {
        return textBox.getValue() == null || textBox.getValue().trim().isEmpty();
    }

    /**
     * Adds a keydownhandler to the given textBox. This prevents the user from typing alphabetic
     * characters.
     * 
     * @param textBox the text field to add the handler to.
     */
    private void addNumericRegex(final TextBox textBox) {
        textBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {

                        if (!isTextBoxBlank(textBox)) {

                            try {

                                if (textBox.getValue().equals("-")) {
                                    // do nothing if they start with a negative sign
                                } else if (textBox.getValue().endsWith(".")
                                        && textBox.getValue().indexOf(".") == textBox.getValue().lastIndexOf(".")) {
                                    // do nothing if they press the decimal point ONLY if there are
                                    // no other decimal points
                                } else if (textBox.getValue().contains(" ")) {
                                    // remove spaces
                                    textBox.setValue(textBox.getValue().replaceAll(" ", ""));
                                } else {
                                    // prevent the user from entering values that are invalid
                                    Double value = Double.valueOf(textBox.getValue());

                                    // eliminate leading 0's as the user is typing
                                    if (textBox.getValue().startsWith("0")) {
                                        textBox.setValue(Double.toString(value));
                                    }
                                }
                            } catch (@SuppressWarnings("unused") Exception e) {
                                textBox.setValue(textBox.getValue(), false);
                            }
                        }
                    }
                });

            }
        });
    }

    /**
     * Toggles the visual elements between the numerical ranges or the catch-all label.
     * 
     * @param show true to show the numerical range options; false to show the catch-all label.
     */
    public void showNumberRanges(boolean show) {
        entireRangePanel.setVisible(show);
        catchAllLabel.setVisible(!show);
    }

    /**
     * Enables or disables the point box input.
     * 
     * @param enabled true to enable the point box; false to disable it.
     */
    public void setPointBoxEnabled(boolean enabled) {
        pointBox.setEnabled(enabled);
    }

    /**
     * Sets the text used for the catch-all label widget.
     * 
     * @param text the text to be displayed. No change will occur if the text is null or blank.
     */
    public void setCatchAllLabelText(String text) {
        showNumberRanges(false);
        if (text != null && !text.trim().isEmpty()) {
            catchAllLabel.setText(text);
        }
    }

    /**
     * Sets the value within the point box input field.
     * 
     * @param points the points to set into the input field. No change will occur if the points are
     *            null.
     */
    public void setPointBoxValue(Double points) {
        if (points != null) {
            pointBox.setValue(points, false);
        }
    }
    
    /**
     * Retrieves the minimum range value. If the minimum range textbox is blank, it will use the
     * value from the maximum range textbox if possible. If no values exist, this will return null.
     * 
     * @return value from minimum range textbox if populated; value from maximum range textbox if
     *         populated and minimum range textbox is not populated; null if both textboxes are not
     *         populated.
     */
    public Double getMinRange() {
        Double min = null;

        // set min range if it exists
        if (!isTextBoxBlank(minRange)) {
            min = Double.valueOf(minRange.getValue());
        }

        return min;
    }
    
    /**
     * Sets the minimum range value.
     * 
     * @param min the value to set into the minimum range. No change will occur if the value is
     *            null.
     */
    public void setMinRange(Double min) {
        if (min != null) {
            minRange.setValue(Double.toString(min));
            // if we are setting the min range, then the catch all is not visible
            catchAllLabel.setVisible(false);
        }
    }

    /**
     * Retrieves the maximum range value. If the maximum range textbox is blank, this will return null.
     * 
     * @return value from maximum range textbox if populated; null if not populated.
     */
    public Double getMaxRange() {
        Double max = null;
        
        // set max range if it exists
        if (isRangeEnabled() && !isTextBoxBlank(maxRange)) {
            max = Double.valueOf(maxRange.getValue());
        }

        return max;
    }
    
    /**
     * Sets the maximum range value.
     * 
     * @param max the value to set into the maximum range. If null, the maximum range value will
     *            become null and the range checkbox will be unchecked.
     */
    public void setMaxRange(Double max) {
        if (max == null) {
            maxRange.setValue(null);
            rangeCheckBox.setValue(false, false);
            maxRangePanel.setVisible(false);
        } else {
            maxRange.setValue(Double.toString(max));
            // if we are setting the max range, then range is checked and the maxRangePanel is
            // visible
            rangeCheckBox.setValue(true, false);
            maxRangePanel.setVisible(true);
            // if we are setting the max range, then the catch all is not visible
            catchAllLabel.setVisible(false);
        }
    }
    
    /**
     * Returns the value of the range checkbox.
     * 
     * @return true if the range is enabled, false otherwise.
     */
    public boolean isRangeEnabled() {
        return rangeCheckBox.getValue();
    }
    
    /**
     * Changes the appearance of the minimum range textbox based on if it is invalid or not. If
     * invalid, the background will change color and a tooltip will be attached.
     * 
     * @param invalid true if there is a conflict with another scoring range widget; false if there
     *            is no conflict.
     */
    public void setMinInvalid(boolean invalid) {
        if (invalid) {
            String error;
            
            Double minRangeVal = getMinRange();
            Double maxRangeVal = getMaxRange();
            if (minRangeVal == null) {
                error = "The minimum range of the condition is empty. Result on save: the entire condition will be removed.";
            } else if (maxRangeVal != null && minRangeVal > maxRangeVal) {
                error = "The minimum range value is greater than the maximum range value. Result on save: the entire condition will be removed.";
            } else {
                error = "There is a conflict with this condition and another; please resolve the conflict. Result during runtime: scoring will use the first valid conditon it finds. This can result in unreliable scores.";
            }
            
            minRange.getElement().getStyle().setBackgroundColor(CONFLICT_COLOR);
            minRange.setTitle(error);
        } else {
            minRange.getElement().getStyle().clearBackgroundColor();
            minRange.setTitle(null);
        }
    }

    /**
     * Changes the appearance of the maximum range textbox based on if it is invalid or not. If
     * invalid, the background will change color and a tooltip will be attached.
     * 
     * @param invalid true if there is a conflict with another scoring range widget; false if there
     *            is no conflict.
     */
    public void setMaxInvalid(boolean invalid) {
        if (invalid) {
            maxRange.getElement().getStyle().setBackgroundColor(CONFLICT_COLOR);
            
            Double minRangeVal = getMinRange();
            Double maxRangeVal = getMaxRange();
            if (maxRangeVal == null) {
                maxRange.setTitle("The maximum range is empty. Result on save: The maximum range will be removed and only the minimum range value will be used for scoring.");
            } else if (minRangeVal != null && minRangeVal > maxRangeVal) {
                maxRange.setTitle("The maximum range value is less than the minimum range value. Result on save: the entire condition will be removed.");
            } else {
                maxRange.setTitle("There is a conflict with this condition and another. Please resolve the conflict. Result during runtime: scoring will use the first valid conditon it finds. This can result in unreliable scores.");
            }
        } else {
            maxRange.getElement().getStyle().clearBackgroundColor();
            maxRange.setTitle(null);
        }
    }

    /**
     * Returns the list of values that will be stored in the properties. The list will always
     * contain the point box value first. Next will come the optional minimum range value and the
     * optional maximum range value, sequentially. Can return null.
     * 
     * @return the list of values from the condition. [points, (opt.) minRange, (opt.) maxRange].
     *         Can be null if there is a range without any values or if the minimum is greater than the maximum.
     */
    public List<Double> getWeightedScores() {
        
        List<Double> scoreList = new ArrayList<Double>();
        
        // free text never has any scoring, return an empty list.
        if (SurveyResponseTypeEnum.FREE_TEXT.equals(responseType)) {
            return scoreList;
        }
        
        scoreList.add(pointBox.getValue() == null ? 0 : pointBox.getValue());

        if (!catchAllLabel.isVisible()) {

            Double min = getMinRange();
            if (min == null) {
                // No range values, return null to indicate this.
                return null;
            }

            Double max = getMaxRange();
            if (max != null && min > max) {
                // ranges can't have the minimum greater than the maximum
                return null;
            }

            // add min range
            scoreList.add(min);

            // add max range if it exists
            if (max != null) {
                scoreList.add(max);
            }
        }

        return scoreList;
    }
    
    /**
     * Checks if there is a conflict between this scoring range widget and the given scoring range
     * widget.
     * 
     * @param otherWidget the widget to compare scoring values against.
     * @return true if this widget and the given widget have conflicting scoring conditions; false
     *         otherwise.
     */
    public boolean isConflict(FreeResponseScoringRangeWidget otherWidget) {
        boolean conflict = false;
        
        // check min value
        Double min = otherWidget.getMinRange();
        if (min != null) {
            conflict = isConflict(min);
        }

        if (!conflict) {
            Double max = otherWidget.getMaxRange();
            if (max != null) {
                conflict = isConflict(max);
            }
        }

        return conflict;
    }
    
    /**
     * Checks if there is a conflict between this scoring range widget and the given score.
     * 
     * @param value the value to compare this widget's scores against.
     * @return true if this widget's scoring conditions conflict with the given value; false
     *         otherwise.
     */
    private boolean isConflict(Double value) {
        boolean conflict = false;

        if (value != null) {
            Double min = getMinRange();
            Double max = getMaxRange();
            
            if (min != null && max != null) {
                conflict = (min <= value) && (value <= max);
            } else if (min != null && max == null) {
                conflict = min.equals(value);
            } else if (min == null && max != null) {
                conflict = max.equals(value);
            }
        }
        
        return conflict;
    }
    
    /**
     * Retrieves the widget's remove button.
     * 
     * @return the remove button
     */
    public Image getRemoveButton() {
        return removeButton;
    }
}
