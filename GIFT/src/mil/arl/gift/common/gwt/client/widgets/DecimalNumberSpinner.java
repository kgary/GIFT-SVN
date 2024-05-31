/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import java.math.BigDecimal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that presents a textbox containing a number and two buttons for incrementing and
 * decrementing that number.
 * 
 * @author tflowers
 */
public class DecimalNumberSpinner extends Composite implements HasValue<BigDecimal>, HasEnabled {

    /** The UiBinder that combines the ui.xml with this java class */
    private static DecimalNumberSpinnerUiBinder uiBinder = GWT.create(DecimalNumberSpinnerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface DecimalNumberSpinnerUiBinder extends UiBinder<Widget, DecimalNumberSpinner> {
    }

    /** Input box for the float value */
    @UiField
    TextBox inputBox;

    /** Button to increment the float value */
    @UiField
    Button incrementButton;

    /** Button to decrement the float value */
    @UiField
    Button decrementButton;

    private final String emptyString = "";
    private final String hyphenString = "-";
    private final char hyphenChar = '-';
    private final String decimalString = ".";
    private final char decimalChar = '.';

    private BigDecimal minValue = BigDecimal.valueOf(Float.MAX_VALUE * -1);
    private BigDecimal maxValue = BigDecimal.valueOf(Float.MAX_VALUE);
    private BigDecimal stepSize = BigDecimal.ONE;
    private BigDecimal defaultValue = BigDecimal.ONE;
    private boolean isEnabled = true;
    private boolean resizeToFitContent = true;

    /**
     * Constructor
     */
    public DecimalNumberSpinner() {
        initWidget(uiBinder.createAndBindUi(this));

        // Add the event handlers
        inputBox.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent arg0) {
                char keyPressed = arg0.getCharCode();
                // Prevent any non-numerical values from being entered
                if (!(Character.isDigit(keyPressed) || keyPressed == hyphenChar || keyPressed == decimalChar)) {
                    arg0.preventDefault();
                }
            }
        });

        inputBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent arg0) {
                int keyCode = arg0.getNativeKeyCode();
                if (keyCode == KeyCodes.KEY_UP) {
                    incrementButton.click();
                } else if (keyCode == KeyCodes.KEY_DOWN) {
                    decrementButton.click();
                } else {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @Override
                        public void execute() {
                            formatNumber();
                        }

                    });
                }
            }
        });

        incrementButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                BigDecimal newVal = getValue().add(stepSize);
                if (newVal.compareTo(maxValue) == 1) {
                    setValue(maxValue, true);
                } else {
                    setValue(newVal, true);
                }
            }

        });

        decrementButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                BigDecimal newVal = getValue().subtract(stepSize);
                if (newVal.compareTo(minValue) == -1) {
                    setValue(minValue, true);
                } else {
                    setValue(newVal, true);
                }
            }

        });

        setValue(defaultValue);
    }

    /**
     * Constructor
     * 
     * @param defaultValue the default value the number spinner is set to on initial load or when
     *        reset. If null, the default value will not change.
     */
    public DecimalNumberSpinner(BigDecimal defaultValue) {
        this(defaultValue, null, null);
    }

    /**
     * Constructor
     * 
     * @param defaultValue the default value the number spinner is set to on initial load or when
     *        reset. If null, the default value will not change.
     * @param min the minimum allowed value. If null, the minimum value will not change.
     * Default min value is -Float.MAX_VALUE
     * @param max the maximum allowed value. If null, the maximum value will not change.
     * Default max value is -Float.MAX_VALUE
     */
    public DecimalNumberSpinner(BigDecimal defaultValue, BigDecimal min, BigDecimal max) {
        this(defaultValue, min, max, null);
    }

    /**
     * Constructor
     * 
     * @param defaultValue the default value the number spinner is set to on initial load or when
     *        reset. If null, the default value will not change. 
     * @param min the minimum allowed value. If null, the minimum value will not change. 
     * Default min value is -Float.MAX_VALUE
     * @param max the maximum allowed value. If null, the maximum value will not change.
     * Default max value is -Float.MAX_VALUE
     * @param stepSize the number to increment or decrement the value by when using the respective
     *        buttons. If null, the step size value will not change.
     */
    public DecimalNumberSpinner(BigDecimal defaultValue, BigDecimal min, BigDecimal max, BigDecimal stepSize) {
        this();

        if (defaultValue != null) {
            this.defaultValue = defaultValue;
        }

        if (min != null) {
            minValue = min;
            setValue(min);
        }

        if (max != null) {
            maxValue = max;
        }

        if (stepSize != null) {
            this.stepSize = stepSize;
        }

        if (maxValue.compareTo(minValue) == -1) {
            throw new IllegalArgumentException("The max value cannot be less than the minimum value");
        }

        if (this.defaultValue.compareTo(minValue) == -1 || this.defaultValue.compareTo(maxValue) == 1) {
            throw new IllegalArgumentException("The default value must be within the specified min/max values");
        }
    }

    /**
     * Gets the maximum allowed value
     * 
     * @return the max value
     */
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    /**
     * Updates the maximum value that the number spinner can be set to. If the new maximum value
     * causes the number spinner's current value to be out of range, then the current value is set
     * to the new maximum value.
     * 
     * @param newValue the new maximum value, must be greater than or equal to current minimum value
     * @throws IllegalArgumentException if the new max value is less than the current min value
     */
    public void setMaxValue(BigDecimal newValue) throws IllegalArgumentException {
        if (newValue.compareTo(minValue) == -1) {
            throw new IllegalArgumentException("The maximum value cannot be less than the minimum value");
        }

        maxValue = newValue;
        if (getValue().compareTo(maxValue) == 1) {
            setValue(maxValue, true);
        }
    }

    /**
     * Gets the minimum allowed value
     * 
     * @return the min value
     */
    public BigDecimal getMinValue() {
        return minValue;
    }

    /**
     * Updates the minimum value that the number spinner can be set to. If the new minimum value
     * causes the number spinner's current value to be out of range, then the current value is set
     * to the new minimum value.
     * 
     * @param newValue the new minimum value, must be less than or equal to current maximum value
     * @throws IllegalArgumentException if the new min value is greater than the current max value
     */
    public void setMinValue(BigDecimal newValue) {
        if (maxValue.compareTo(newValue) == -1) {
            throw new IllegalArgumentException("The minimum value cannot be greater than the maximum value");
        }

        minValue = newValue;
        if (getValue().compareTo(minValue) == -1) {
            setValue(minValue, true);
        }
    }

    /**
     * Gets the size of the step of each increment/decrement
     * 
     * @return the step size
     */
    public BigDecimal getStepSize() {
        return stepSize;
    }

    /**
     * Sets the size of the step of each increment/decrement
     * 
     * @param newValue the step size
     */
    public void setStepSize(BigDecimal newValue) {
        stepSize = newValue;
    }

    /**
     * Checks if the inputBox contains a hyphen and formats it as a negative number, checks that the
     * value is within the min/max range and handles empty string values as the default value
     */
    private void formatNumber() {
        String text = inputBox.getText();

        if (text.contains(hyphenString)) {

            // if it starts with '-', remove all '-' and add one to the start
            // to remove any duplicates
            if (text.startsWith(hyphenString)) {
                text = text.replaceAll(hyphenString, emptyString);
                text = hyphenString.concat(text);
            } else { // otherwise, remove any '-' since it would be an invalid format
                text = text.replaceAll(hyphenString, emptyString);
            }

        }

        // if it contains a '.', remove any subsequent '.'
        if (text.contains(decimalString)) {
            if (text.indexOf(decimalString) != text.lastIndexOf(decimalString)) {
                int firstIndex = text.indexOf(decimalString);
                String textIncludingFirstDecimal = text.substring(0, firstIndex + 1);
                String textAfterFirstDecimal = text.substring(firstIndex + 1).replaceAll(RegExp.quote(decimalString),
                        emptyString);
                text = textIncludingFirstDecimal.concat(textAfterFirstDecimal);
            }
        }

        try {

            BigDecimal numValue = new BigDecimal(text);

            boolean resetValue = false;
            if (numValue.compareTo(maxValue) == 1) {
                numValue = maxValue;
                resetValue = true;
            } else if (numValue.compareTo(minValue) == -1) {
                numValue = minValue;
                resetValue = true;
            }

            if (resetValue || !text.endsWith(decimalString)) {
                setValue(numValue, true);
            }

        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            if (text == hyphenString) { // handle the case where the inputBox contains only '-'
                inputBox.setText(hyphenString);
                ValueChangeEvent.fire(this, defaultValue);
            } else if (text.trim().length() > 0) { // reset the input box to this widget's last
                                                   // valid value
                setValue(getValue());
            } else { // allow an empty value
                inputBox.setText(emptyString);
                ValueChangeEvent.fire(this, defaultValue);
            }
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<BigDecimal> newHandler) {
        return addHandler(newHandler, ValueChangeEvent.getType());
    }

    @Override
    public BigDecimal getValue() {
        try {
            return new BigDecimal(inputBox.getText());
        } catch (@SuppressWarnings("unused") NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public void setValue(BigDecimal arg0) {
        setValue(arg0, false);
    }

    /**
     * Set's the value of the number spinner to the specified value. Does nothing if the value is
     * not within valid range
     * 
     * @param newValue the new value of the number spinner, null is treated as the current minValue
     * @param fireEvents indicates whether or not the value change event should be fired
     */
    @Override
    public void setValue(BigDecimal newValue, boolean fireEvents) {
        if (newValue == null) {
            newValue = minValue;
        }

        if (newValue.compareTo(minValue) >= 0 && newValue.compareTo(maxValue) <= 0) {
            inputBox.setText(newValue.toString());

            if (resizeToFitContent) {
                resizeToFitContent();
            }

            if (fireEvents) {
                ValueChangeEvent.fire(this, newValue);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        inputBox.setEnabled(enabled);
        incrementButton.setEnabled(enabled);
        decrementButton.setEnabled(enabled);
    }

    /**
     * Set the flag for resizing the textbox to fit the content.
     * 
     * @param newVal true (default value) will resize the textbox when the value is changed; false
     *        will keep the size it was originally constructed as when the value changes.
     */
    public void setResizeToFitContent(boolean newVal) {
        resizeToFitContent = newVal;
    }

    /**
     * Resizes the width of the input box to fit the length of the text value
     */
    public void resizeToFitContent() {
        final int baseWidth = 30;
        final int digitWidth = 4;
        final int buttonWidth = 30;

        int numberOfDigits;
        if(getValue().floatValue() == 0){
            // Note: Math.log10 returns negative infinity for 0
            numberOfDigits = 1;
        }else{
            // Note: Math.log10 returns NaN for negative values
            numberOfDigits = (int) Math.floor(Math.log10(Math.abs(getValue().floatValue())));
            
            if(getValue().floatValue() < 0){
                // add a digit for the negative sign
                numberOfDigits++;
            }
        }
        int newWidth = baseWidth + digitWidth * numberOfDigits + buttonWidth;
        setWidth(Math.max(0, newWidth) + "px");
    }
}
