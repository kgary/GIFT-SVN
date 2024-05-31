/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;

/**
 * Class for containing the properties for a time column
 *
 * @author jleonard
 */
public class TimeProperties extends ColumnProperties implements MinMaxProperty, Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Any times before this time will be filtered out of any reports. Can be null.*/
	private Long filterBeforeTime = null;

	/** Any times after this time will be filtered out of any reports. Can be null.*/
    private Long filterAfterTime = null;

    /**
     * Default Constructor
     *
     * No filtering is done
     */
    public TimeProperties() {
    }

    /**
     * Constructor
     *
     * @param filterBeforeTime The time, in milliseconds, to filter out all events
     * before this time
     * @param filterAfterTime The time, in milliseconds, to filter out all events
     * after this time
     */
    public TimeProperties(Long filterBeforeTime, Long filterAfterTime) {

        this.filterBeforeTime = filterBeforeTime;

        this.filterAfterTime = filterAfterTime;
    }

    /**
     * Gets the time to filter out all events before this time
     *
     * Returns null if there is no filter before time
     *
     * @return Long The time, in milliseconds, to filter out all events before this
     * time
     */
    public Long getFilterBeforeTime() {

        return filterBeforeTime;
    }

    /**
     * Sets the time to filter out all events before
     *
     * Set null if there is no filter before time
     *
     * @param filterBeforeTime The time, in milliseconds, to filter out all events
     * before this time
     */
    public void setFilterBeforeTime(Long filterBeforeTime) {

        this.filterBeforeTime = filterBeforeTime;
    }

    /**
     * Gets the time to filter out all events after this time
     *
     * Returns null if there is no filter after time
     *
     * @return Long The time, in milliseconds, to filter out all events before this
     * time
     */
    public Long getFilterAfterTime() {

        return filterAfterTime;
    }

    /**
     * Sets the time to filter out all events after
     *
     * Set null if there is no filter after time
     *
     * @param filterAfterTime The time, in milliseconds, to filter out all events
     * before this time
     */
    public void setFilterAfterTime(Long filterAfterTime) {

        this.filterAfterTime = filterAfterTime;
    }
    
    @Override
    public Long getMinimum() {

        return filterBeforeTime;
    }

    @Override
    public Long getMaximum() {

        return filterAfterTime;
    }
    
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder();
        
        builder.append("[TimeProperties: ");
        
        builder.append("FilterBeforeTime: ").append(filterBeforeTime);
        
        builder.append(", FilterAfterTime: ").append(filterAfterTime);

        builder.append("]");

        return builder.toString();
    }
}
