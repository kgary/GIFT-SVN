/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor;

/**
 * Each enum represents a "type" of Filter that can be used in a
 * SensorsConfiguration. This is particularly useful for end users because it
 * allows them to identify a Filter using the enum's displayName rather than
 * the FilterImpl that it maps to (see SensorsConfigurationMaps).
 * @author elafave
 *
 */
public enum FilterTypeEnum {

	BIOHARNESS_FILTER("Bioharness Filter"),
	EMOTIV_SENSOR_FILTER("Emotiv Sensor Filter"),
	GSR_DETECTION_FILTER("GSR Detection Filter"),
	GENERIC_SENSOR_FILTER("Generic Sensor Filter"),
	KINECT_SENSOR_FILTER("Kinect Sensor Filter"),
	QRS_FROM_ECG_FILTER("QRS From ECG Filter"),
	SINE_WAVE_SENSOR_FILTER("Sine Wave Sensor Filter");

    private final String displayName;
	
    private FilterTypeEnum(String displayName){
        this.displayName = displayName;
    }

    /**
     * 
     * @return A human-friendly string that identifies the FilterTypeEnum.
     */
    public String getDisplayName() {
    	return displayName;
    }
    
    /**
     * Identifies the FilterTypEnum with the given displayName.
     * @param displayName displayName of a FilterTypeEnum
     * @return FilterTypeEnum with the given displayName, null if none of the
     * FilterTypeEnums have the given displayName.
     */
    static public FilterTypeEnum fromDisplayName(String displayName) {
    	FilterTypeEnum [] filterTypeEnums = FilterTypeEnum.values();
        for(FilterTypeEnum filterTypeEnum : filterTypeEnums) {
        	if(filterTypeEnum.getDisplayName().equals(displayName)) {
        		return filterTypeEnum;
        	}
        }
        return null;
    }

}
