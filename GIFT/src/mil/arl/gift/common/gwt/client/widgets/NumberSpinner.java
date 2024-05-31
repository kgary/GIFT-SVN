/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that presents a textbox containing a number and two buttons for 
 * incrementing and decrementing that number.
 * 
 * @author tflowers
 *
 */
public class NumberSpinner extends Composite implements HasValue<Integer>, HasEnabled {
    
    private static NumberSpinnerUiBinder uiBinder = GWT.create(NumberSpinnerUiBinder.class);
    
    interface NumberSpinnerUiBinder extends UiBinder<Widget, NumberSpinner> {
        
    }
    
    @UiField
    TextBox inputBox;
    
    @UiField
    Button incrementButton;
    
    @UiField
    Button decrementButton;
    
    private final String emptyString = "";
    private final String hyphenString = "-";
    private final char hyphenChar = '-';
    
    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;
    private int stepSize = 1;
    private int defaultValue = 1;
    private boolean isEnabled = true;
    private boolean resizeToFitContent = true;
    
    
    /**
     * Constructor
     */
    public NumberSpinner() {
        initWidget(uiBinder.createAndBindUi(this));
        
        //Add the event handlers
        inputBox.addKeyPressHandler(new KeyPressHandler(){

            @Override
            public void onKeyPress(KeyPressEvent arg0) {
                char keyPressed = arg0.getCharCode();
                // Prevent any non-numerical values from being entered
                if (!(Character.isDigit(keyPressed) || keyPressed == hyphenChar)) {
                    arg0.preventDefault();
                }
            }
        });
        
        inputBox.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent arg0) {
                int keyCode = arg0.getNativeKeyCode();
                if(keyCode == KeyCodes.KEY_UP) {
                    incrementButton.click();
                } else if(keyCode == KeyCodes.KEY_DOWN) {
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
        
        incrementButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent arg0) {
                int newVal = getValue() + stepSize;
                if(newVal > maxValue) {
                    setValue(maxValue);
                } else {
                    setValue(newVal);
                }
            }
            
        });
        
        decrementButton.addClickHandler(new ClickHandler(){
            
            @Override
            public void onClick(ClickEvent arg0) {
                int newVal = getValue() - stepSize;
                if(newVal < minValue) {
                    setValue(minValue);
                } else {
                    setValue(newVal);
                }
            }
            
        });

        setValue(defaultValue);
    }
    
    /**
     * Constructor
     * 
     * @param defaultValue the default value the number spinner is set to on initial load or when reset
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     */
    public NumberSpinner(int defaultValue, int min, int max) {
        this();
        
        if(max < min) {
            throw new IllegalArgumentException("The max value cannot be less than the minimum value");
        }

        if (defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("The default value must be within the specified min/max values");
        }
        
        this.defaultValue = defaultValue;

        minValue = min;
        maxValue = max;
        
        setValue(defaultValue);
    }
    
    /**
     * Constructor
     * 
     * @param defaultValue the default value the number spinner is set to on initial load or when reset
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @param stepSize the number to increment or decrement the value by when using the respective buttons
     */
    public NumberSpinner(int defaultValue, int min, int max, int stepSize) {
        this(defaultValue, min, max);
        this.stepSize = stepSize;
    }
    
    /**
     * Gets the maximum allowed value
     * 
     * @return the max value
     */
    public int getMaxValue() {
        return maxValue;
    }
    
    /**
     * Updates the maximum value that the number spinner can be set to.
     * If the new maximum value causes the number spinner's current value 
     * to be out of range, then the current value is set to the new maximum 
     * value.
     * 
     * @param newValue the new maximum value, must be greater than or equal to current
     * minimum value
     */
    public void setMaxValue(int newValue) {
        if(newValue < minValue) {
            throw new IllegalArgumentException("The maximum value cannot be less than the minimum value");
        }
        
        maxValue = newValue;
        if(getValue() > maxValue) {
            setValue(maxValue);
        }
    }
    
    /**
     * Gets the minimum allowed value
     * 
     * @return the min value
     */
    public int getMinValue() {
        return minValue;
    }
    
    /**
     * Updates the minimum value that the number spinner can be set to.
     * If the new minimum value causes the number spinner's current value 
     * to be out of range, then the current value is set to the new minimum
     * value.
     * 
     * @param newValue the new minimum value, must be less than or equal to current
     * maximum value
     */
    public void setMinValue(int newValue) {
        if(newValue > maxValue) {
            throw new IllegalArgumentException("The minimum value cannot be greater than the maximum value");
        }
        
        minValue = newValue;
        if(getValue() < minValue) {
            setValue(minValue);
        }
    }

    /**
     * Gets the size of the step of each increment/decrement 
     * 
     * @return the step size
     */
    public int getStepSize() {
        return stepSize;
    }

    /**
     * Sets the size of the step of each increment/decrement 
     * 
     * @param newValue the step size
     */
    public void setStepSize(int newValue) {
        stepSize = newValue;
    }
    
    /**
     * Checks if the inputBox contains a hyphen and formats it as a negative number,
     * checks that the value is within the min/max range and handles empty string values
     * as the default value
     */
    private void formatNumber() {
        String text = inputBox.getText();

        if (inputBox.getText().contains(hyphenString)) {
            
            // if it starts with '-', remove all '-' and add one to the start
            // to remove any duplicates
            if (text.startsWith(hyphenString)) {
                text = text.replaceAll(hyphenString, emptyString);
                text = hyphenString.concat(text);
            } else { // otherwise, remove any '-' since it would be an invalid format
                text = text.replaceAll(hyphenString, emptyString);
            }
            
        }

        try {

            int numValue = Integer.parseInt(text);
            
            if(numValue > maxValue) {
                numValue = maxValue;
            } else if (numValue < minValue) {
                numValue = minValue;
            }
            
            setValue(numValue);

        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            if (text == hyphenString) { // handle the case where the inputBox contains only '-'
                inputBox.setText(hyphenString);
                ValueChangeEvent.fire(this, defaultValue);
            } else if (text.trim().length() > 0) { // reset the input box to this widget's last valid value
                setValue(getValue());
            } else { // allow an empty value
                inputBox.setText(emptyString);
                ValueChangeEvent.fire(this, defaultValue);
            }
        }
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> newHandler) {
        return addHandler(newHandler, ValueChangeEvent.getType());
    }

    @Override
    public Integer getValue() {
        try {
            return Integer.parseInt(inputBox.getText());
        } catch(@SuppressWarnings("unused") NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public void setValue(Integer arg0) {
        setValue(arg0, true);
    }

    /**
     * Set's the value of the number spinner to the specified value. Does nothing if the value is not within valid range
     * 
     * @param newValue the new value of the number spinner, null is treated 
     * as the current minValue
     * @param fireEvents indicates whether or not the value change event 
     * should be fired
     */
    @Override
    public void setValue(Integer newValue, boolean fireEvents) {
        if(newValue == null) {
            newValue = minValue;
        }
        
        if(newValue >= minValue && newValue <= maxValue) {
            inputBox.setText(newValue.toString());
            
            if (resizeToFitContent) {
                resizeToFitContent();
            }
            
            if(fireEvents) {
                ValueChangeEvent.fire(this, newValue);
            }
        }
    }

    
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    
    @Override
    public void setEnabled(boolean newVal) {
        isEnabled = newVal;
        inputBox.setEnabled(newVal);
        incrementButton.setEnabled(newVal);
        decrementButton.setEnabled(newVal);
    }
    
    /**
     * Set the flag for resizing the textbox to fit the content.
     * 
     * @param newVal true (default value) will resize the textbox when the value is changed; false
     *            will keep the size it was originally constructed as when the value changes.
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
        if(getValue() == 0){
            // Note: Math.log10 returns negative infinity for 0
            numberOfDigits = 1;
        }else{
            // Note: Math.log10 returns NaN for negative values
            numberOfDigits = (int) Math.floor(Math.log10(Math.abs(getValue())));
            
            if(getValue() < 0){
                // add a digit for the negative sign
                numberOfDigits++;
            }
        }
        int newWidth = baseWidth + digitWidth * numberOfDigits + buttonWidth;
        setWidth(Math.max(0, newWidth) + "px");
    }
}
