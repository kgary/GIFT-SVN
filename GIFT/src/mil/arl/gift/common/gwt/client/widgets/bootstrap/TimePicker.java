/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.HasReadOnly;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;

/**
 * A widget that allows users to enter a time in hh:mm format using multiple text boxes. This widget is only for hours and minutes,
 * see FormattedTimeBox if seconds should also be included. This widget internally calculates 
 * the total time based on the values that have been entered into all of its text boxes and provides access to it via the 
 * {@link HasValue} interface, though which the total time (in minutes) can be assigned or retrieved by callers.
 * This widget is meant to be used when retrieving local time from the user. The value of hours can't exceed 12,
 * the value of minutes can't exceed 59, and the widget can be switched between AM and PM.
 * 
 * @author mdubin
 */
public class TimePicker extends Composite implements HasValue<Integer>, HasEnabled, HasReadOnly{

    /** The UiBinder that combines the ui.xml with this java class */
    private static TimePickerUiBinder uiBinder = GWT.create(TimePickerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TimePickerUiBinder extends UiBinder<Widget, TimePicker> {
    }
    
    /** 60 minutes in an hour */
    private static final int MINUTES_PER_HOUR = 60;
    
    /** 60 seconds in a minute */
    private static final int SECONDS_PER_MINUTE = 60;
    
    /** Minimum allowed hours */
    private static final int MIN_HOURS = 1;
    
    /** Maximum allowed hours */
    private static final int MAX_HOURS = 12;
    
    /** Minimum allowed minutes */
    private static final int MIN_MINUTES = 0;
    
    /** Maximum allowed minutes */
    private static final int MAX_MINUTES = MINUTES_PER_HOUR - 1;
    
    /** The component used to enter the hours value */
    @UiField
    protected TextBox hoursBox;
    
    /** The component used to enter the minutes value */
    @UiField
    protected TextBox minutesBox;
    
    /** The component used to control AM/PM */
    @UiField
    protected Button dayNightSwitch;
    
    /** The component used to increment hours */
    @UiField
    protected Button hoursUpButton;
    
    /** The component used to decrement hours */
    @UiField
    protected Button hoursDownButton;
    
    /** The component used to increment minutes */
    @UiField
    protected Button minutesUpButton;
    
    /** The component used to decrement minutes */
    @UiField
    protected Button minutesDownButton;
    
    /** The value of this widget, a.k.a. the total time in seconds */
    private Integer value = null;
    
    /** The NumberFormat used to ensure both text boxes have the proper two-digit formatting */
    private static final NumberFormat twoDigitFormat = NumberFormat.getFormat("00");

    /**
     * Creates a new widget providing input elements to allow the user to enter a time in hh:mm:ss format
     */
    public TimePicker() {
        initWidget(uiBinder.createAndBindUi(this));

        //setup handlers for the minutes box
        
        minutesBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                minutesBox.selectAll();
            }
        });
        
        minutesBox.addKeyDownHandler(new KeyDownHandler() {
             
            @Override
            public void onKeyDown(KeyDownEvent event) {
                
                final String lastValue = minutesBox.getText();
                
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        
                        if(minutesBox.getText() != null && !minutesBox.getText().isEmpty()){
                        
                            try{
                                
                                //prevent the user from entering values that are invalid
                                
                                Integer minutes = Integer.valueOf(minutesBox.getValue());
                                
                                if(minutes < MIN_MINUTES || minutesBox.getValue().length() > 2){
                                    minutesBox.setText(lastValue);
                                    
                                } else if(minutes > MAX_MINUTES){
                                    minutesBox.setText(lastValue);
                                    
                                }
                                
                            } catch(@SuppressWarnings("unused") Exception e){
                                minutesBox.setText(lastValue);
                            }
                        }
                    }
                });
                
            }
        });
        
        minutesBox.addBlurHandler(new BlurHandler() {
            
            @Override
            public void onBlur(BlurEvent event) {           
                recalculateValue();
            }
        });
        
        
        //setup handlers for the hours box
        
        hoursBox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                hoursBox.selectAll();
            }
        });
        
        hoursBox.addKeyDownHandler(new KeyDownHandler() {
            
            @Override
            public void onKeyDown(KeyDownEvent event) {
                
                final String lastValue = hoursBox.getText();
                
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        
                        if(hoursBox.getText() != null && !hoursBox.getText().isEmpty()){
                        
                            try{
                                
                                //prevent the user from entering values that are invalid
                                
                                Integer hours = Integer.valueOf(hoursBox.getValue());
                                
                                if(hours < MIN_HOURS - 1 || hoursBox.getValue().length() > 2){
                                    hoursBox.setText(lastValue);
                                    
                                } else if(hours > MAX_HOURS){
                                    hoursBox.setText(lastValue);
                                    
                                }
                                
                            } catch(@SuppressWarnings("unused") Exception e){
                                hoursBox.setText(lastValue);
                            }
                        }
                    }
                });
                
            }
        });
        
        hoursBox.addBlurHandler(new BlurHandler() {
            
            @Override
            public void onBlur(BlurEvent event) {           
                recalculateValue();
            }
        });
        
        dayNightSwitch.setText("AM");
        dayNightSwitch.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                if(dayNightSwitch.getText().equals("AM")) {
                    dayNightSwitch.setText("PM");
                } else {
                    dayNightSwitch.setText("AM");
                }
            }
            
        });
        
        
        hoursUpButton.setText("\u25B2");
        hoursUpButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                int hours = Integer.valueOf(hoursBox.getValue());
                if(hours == MAX_HOURS) {
                    hoursBox.setText(Integer.toString(MIN_HOURS));
                    hoursBox.setValue(Integer.toString(MIN_HOURS));
                } else if(hours < MAX_HOURS) {
                    hoursBox.setText(Integer.toString(hours++));
                    hoursBox.setValue(Integer.toString(hours++));
                }
            }
        });
        
        minutesUpButton.setText("\u25B2");
        minutesUpButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                int minutes = Integer.valueOf(minutesBox.getValue());
                if(minutes == MAX_MINUTES) {
                    minutesBox.setText(Integer.toString(MIN_MINUTES));
                    minutesBox.setValue(Integer.toString(MIN_MINUTES));
                }
                if(minutes < MAX_MINUTES){
                   minutesBox.setText(Integer.toString(minutes++));
                   minutesBox.setValue(Integer.toString(minutes++));
               }
            }
        });
        
        hoursDownButton.setText("\u25BC");
        hoursDownButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                int hours = Integer.valueOf(hoursBox.getValue());
                if(hours == MIN_HOURS) {
                    hoursBox.setText(Integer.toString(MAX_HOURS));
                    hoursBox.setValue(Integer.toString(MAX_HOURS));
                } else if(hours > MIN_HOURS) {
                    hoursBox.setText(Integer.toString(hours--));
                    hoursBox.setValue(Integer.toString(hours--));
                }
            }
        });
        
        minutesDownButton.setText("\u25BC");
        minutesDownButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                int minutes = Integer.valueOf(minutesBox.getValue());
                if(minutes == MIN_MINUTES) {
                    minutesBox.setText(Integer.toString(MAX_MINUTES));
                    minutesBox.setValue(Integer.toString(MAX_MINUTES));
                }
                if(minutes > MIN_MINUTES){
                   minutesBox.setText(Integer.toString(minutes--));
                   minutesBox.setValue(Integer.toString(minutes--));
               }
            }
        });
    }
    
    /**
     * Reads the values currently entered into this widget's text boxes and calculates the total entered time (in seconds)
     */
    private void recalculateValue() {
        
        boolean hoursEmpty = hoursBox.getValue() == null || hoursBox.getValue().isEmpty();
        boolean minutesEmpty = minutesBox.getValue() == null || minutesBox.getValue().isEmpty();
        
        if(!hoursEmpty || !minutesEmpty){
            
            //if at least one box has a value, then assign a default value to the other boxes to make them valid
            
            if(hoursEmpty){
                hoursBox.setValue(Integer.toString(MIN_HOURS));
            }
            
            if(minutesEmpty){
                minutesBox.setValue(Integer.toString(MIN_MINUTES));
            }
                
            //calculate the total number of seconds based on the entered values
            Integer hourSeconds = convertHoursToSeconds(getHours());
            Integer minuteSeconds = convertMinutesToSeconds(getMinutes());
            
            Integer totalSeconds = hourSeconds + minuteSeconds;
            
            //update this widget's value
            setValue(totalSeconds, true);
            
        } else {
            
            //no values have been entered, so the total value is null
            setValue(null, true);
        }
    }

    /**
     * Updates this widget's text boxes to match its underlying value. This method will also apply the proper  number formatting 
     * to each of this widget's text boxes, ensuring that the minute and second boxes both show leading zeros (as is typical in
     * hh:mm:ss format)
     */
    private void updateDisplay(){
        
        if(value != null){
                                
            Integer hours = value / SECONDS_PER_MINUTE / MINUTES_PER_HOUR;
            Integer minutes = (value / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
            
            if(hours < MIN_HOURS) {
                hours = MIN_HOURS;
            }
            
            if(minutes < MIN_MINUTES) {
                minutes = MIN_MINUTES;
            }
            
            hoursBox.setValue(twoDigitFormat.format(hours));
            minutesBox.setValue(twoDigitFormat.format(minutes));
            
        } else {
            
            hoursBox.setValue(null);
            minutesBox.setValue(null);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Integer getValue() {
        return value;
    }
    
    /**
     * Provides the textbox that displays the hours
     * @return the textbox that displays the hours
     */
    public TextBox getHoursBox() {
        return hoursBox;
    }
    
    /**
     * Sets the value of the hours textbox
     * @param hours the value to set the hours textbox to
     */
    public void setHours(int hours) {
        hoursBox.setText(twoDigitFormat.format(hours));
        hoursBox.setValue(Integer.toString(hours));
    }
    
    /**
     * Provides the textbox that displays the minutes
     * @return the textbox that displays the minutes
     */
    public TextBox getMinutesBox() {
        return minutesBox;
    }
    
    /**
     * Sets the value of the minutes textbox
     * @param hours the value to set the minutes textbox to
     */
    public void setMinutes(int minutes) {
        minutesBox.setText(twoDigitFormat.format(minutes));
        minutesBox.setValue(Integer.toString(minutes));
    }
    
    /**
     * Provides the value of the {@link TimePicker} as a user-friendly display text in the
     * following form:<br>
     * <br>
     * HH:mm:ss
     * 
     * @return user-friendly display text
     */
    public String getValueAsText() {
        int hours = getHours();
        int minutes = getMinutes();
        String hourStr = hours < 10 ? "0" + hours : String.valueOf(hours);
        String minuteStr = minutes < 10 ? "0" + minutes : String.valueOf(minutes);

        // need to save in the format HH:mm
        StringBuilder formattedTime = new StringBuilder();
        formattedTime.append(hourStr);
        formattedTime.append(":");
        formattedTime.append(minuteStr);

        return formattedTime.toString() + dayNightSwitch.getText();
    }
    
    /**
     * Returns the time as a number of seconds from the {@link String} in form HH:mm:ss provided by
     * {@link #getValueAsText()}.
     * 
     * @param atTime The {@link String} that represents a span of time.
     * @return The time measured in seconds as an integer. Will use 0 for any value
     */
    public static int getTimeFromString(String atTime) {
        // return 0 if atTime is blank or not in the form HH:mm:ss
        if (StringUtils.isBlank(atTime) || atTime.length() != 8 || !atTime.matches("\\d\\d:\\d\\d:\\d\\d")) {
            return 0;
        }

        int atTimeHours, atTimeMinutes, atTimeSeconds;
        if (StringUtils.isNotBlank(atTime)) {
            String[] atTimeComponents = atTime.split(":");

            /* Try to parse the components, or default to zero if they're not parseable */
            try {
                atTimeHours = Integer.parseInt(atTimeComponents.length > 0 ? atTimeComponents[0] : "0");
                atTimeMinutes = Integer.parseInt(atTimeComponents.length > 1 ? atTimeComponents[1] : "0");
                atTimeSeconds = Integer.parseInt(atTimeComponents.length > 2 ? atTimeComponents[2] : "0");
            } catch (@SuppressWarnings("unused") NumberFormatException numbEx) {
                atTimeHours = 0;
                atTimeMinutes = 0;
                atTimeSeconds = 0;
            }
        } else {
            atTimeHours = atTimeMinutes = atTimeSeconds = 0;
        }

        return convertHoursToSeconds(atTimeHours) + convertMinutesToSeconds(atTimeMinutes) + atTimeSeconds;
    }
    
    /**
     * Converts the given time to user-friendly display text in the following forms depending on the
     * non zero values:<br>
     * <br>
     * Short Hand (true):<br>
     * Hh Mm Ss<br>
     * Hh Mm<br>
     * Hh Ss<br>
     * Hh<br>
     * Mm<br>
     * Ss<br>
     * <br>
     * Short Hand (false):<br>
     * H hour(s), M minute(s), and S second(s)<br>
     * H hour(s) and M minute(s)<br>
     * H hour(s) and S second(s)<br>
     * M minute(s) and S second(s)<br>
     * H hour(s)<br>
     * M minute(s)<br>
     * S second(s)
     * 
     * @param timeInSeconds the time in seconds
     * @param shortHand whether to use short hand notation
     * @return user-friendly display text,<br>
     * e.g. (short-hand) "12m 53s", "1h 12m 53s"<br>
     * e.g. "12 minutes and 53 seconds", "1 hour, 12 minutes and 53 seconds"
     */
    public static String getDisplayText(int timeInSeconds, boolean shortHand) {
        
        int hours = timeInSeconds / SECONDS_PER_MINUTE / MINUTES_PER_HOUR;
        int minutes = (timeInSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
        int seconds = timeInSeconds % SECONDS_PER_MINUTE;
        
        // create 'has' flags for each
        boolean hasHours = hours != 0;
        boolean hasMinutes = minutes != 0;
        boolean hasSeconds = seconds != 0;

        // build string
        StringBuilder sb = new StringBuilder();
        if (hasHours) {
            sb.append(hours);
            if(shortHand){
                sb.append("h");
            }else{
                sb.append(" hour");
                if (hours != 1) {
                    sb.append("s");
                }
            }

        }

        if (hasMinutes) {
            if (hasHours) {
                
                if(shortHand){
                    sb.append(hasSeconds ? Constants.SPACE : Constants.EMPTY);
                }else{
                    sb.append(hasSeconds ? ", " : " and ");
                }
            }

            sb.append(minutes);
            if(shortHand){
                sb.append("m");
            }else{
                sb.append(" minute");
                if (minutes != 1) {
                    sb.append("s");
                }
            }
            
        }

        if (hasSeconds) {
            if (hasHours && hasMinutes) {
                
                if(shortHand){
                    sb.append(Constants.SPACE);
                }else{
                    sb.append(", and ");
                }
            } else if (hasHours || hasMinutes) {
                if(shortHand){
                    sb.append(Constants.SPACE);
                }else{
                    sb.append(" and ");
                }
            }

            sb.append(seconds);
            if(shortHand){
                sb.append("s");
            }else{
                sb.append(" second");
                if (seconds != 1) {
                    sb.append("s");
                }
            }
        }

        if (!(hasHours || hasMinutes || hasSeconds)) {
            // If no hours, minutes, or seconds are set, just show 0 seconds
            sb.append(shortHand ? "0s" : "0 seconds");
        }

        return sb.toString();
    }

    /**
     * Converts the given time to user-friendly display text in the following forms depending on the
     * non zero values:<br>
     * <br>
     * H hour(s), M minute(s), and S second(s)<br>
     * H hour(s) and M minute(s)<br>
     * H hour(s) and S second(s)<br>
     * M minute(s) and S second(s)<br>
     * H hour(s)<br>
     * M minute(s)<br>
     * S second(s)
     * 
     * @param timeInSeconds the time in seconds
     * @return user-friendly display text, e.g. "12 minutes and 53 seconds", "1 hour, 12 minutes and 53 seconds"
     */
    public static String getDisplayText(int timeInSeconds) {
        return getDisplayText(timeInSeconds, false);
    }

    /**
     * Convert minutes to seconds.
     * 
     * @param minutes the number of minutes.
     * @return the number of seconds.
     */
    public static Integer convertMinutesToSeconds(long minutes) {
        return (int) (minutes * SECONDS_PER_MINUTE);
    }

    /**
     * Convert hours to seconds.
     * 
     * @param hours the number of hours.
     * @return the number of seconds.
     */
    public static Integer convertHoursToSeconds(long hours) {
        return convertMinutesToSeconds(hours * MINUTES_PER_HOUR);
    }

    /**
     * Gets the entered hours value. Returns 0 if the text isn't a valid integer.
     * 
     * @return the number of hours or 0 if the entered text isn't valid.
     */
    public int getHours() {
        try {
            return Integer.valueOf(hoursBox.getValue());
        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
            return 0;
        }
    }
    
    /**
     * Gets the button that controls the time of day.
     * 
     * @return the Button that controls AM/PM.
     */
    public Button getSwitch() {
        return dayNightSwitch;
    }
    
    /**
     * Determines whether the TimePicker is currently in AM or PM.
     * 
     * @return a boolean that represents what state the TimePicker is in. If true, AM. If false, PM.
     */
    public boolean isAM() {
        return dayNightSwitch.getText().equals("AM");
    }
    
    /**
     * Gets the button that increments the hours.
     * 
     * @return the Button that increments the hours.
     */
    public Button getHoursUpButton() {
        return hoursUpButton;
    }
    
    /**
     * Gets the button that decrements the hours.
     * 
     * @return the Button that decrements the hours.
     */
    public Button getHoursDownButton() {
        return hoursDownButton;
    }
    
    /**
     * Gets the button that increments the minutes.
     * 
     * @return the Button that increments the minutes.
     */
    public Button getMinutesUpButton() {
        return minutesUpButton;
    }
    
    /**
     * Gets the button that decrements the minutes.
     * 
     * @return the Button that decrements the minutes.
     */
    public Button getMinutesDownButton() {
        return minutesDownButton;
    }

    /**
     * Gets the entered minutes value. Returns 0 if the text isn't a valid integer.
     * 
     * @return the number of minutes or 0 if the entered text isn't valid.
     */
    public int getMinutes() {
        try {
            return Integer.valueOf(minutesBox.getValue());
        } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
            return 0;
        }
    }

    @Override
    public void setValue(Integer value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Integer value, boolean fireEvents) {
        
        Integer oldValue = this.value;
        
        this.value = value;
        
        updateDisplay();
        
        if(fireEvents){
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    @Override
    public boolean isEnabled() {
        return hoursBox.isEnabled() && minutesBox.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        hoursBox.setEnabled(enabled);
        minutesBox.setEnabled(enabled);
    }

    @Override
    public boolean isReadOnly() {
        return hoursBox.isReadOnly() && minutesBox.isReadOnly();
    }
    
    @Override
    public void setReadOnly(boolean readOnly) {
        hoursBox.setReadOnly(readOnly);
        minutesBox.setReadOnly(readOnly);
    }

}
