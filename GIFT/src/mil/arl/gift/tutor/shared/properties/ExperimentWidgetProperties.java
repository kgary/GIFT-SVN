/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;


/**
 * Properties for widgets that are used for experiments.
 * 
 * @author nroberts
 */
public class ExperimentWidgetProperties {
	
	/** Property used to get and set the experiment ID */
	private static final String EXPERIMENT_ID = "EXPERIMENT_ID";
	
	/** Property used to get and set the experiment folder path */
    private static final String EXPERIMENT_FOLDER = "EXPERIMENT_FOLDER";

	/** Property used to get and set whether or not the back button was pressed */
	private static final String BACK_BUTTON_PRESSED = "BACK_BUTTON_PRESSED";
	
	/**
	 * Gets the experiment ID from the specified widget properties
	 * 
	 * @param properties the widget properties from which to get the experiment Id
	 * @return the experiment ID
	 */
	public static String getExperimentId(WidgetProperties properties){
		return (String) properties.getPropertyValue(EXPERIMENT_ID);
	}
	
	/**
	 * Sets the experiment Id for the specified widget properties.
	 * 
	 * @param properties the widget properties to which the experiment ID should be assigned
	 * @param request the experiment ID
	 */
	public static void setExperimentId(WidgetProperties properties, String experimentId){
		properties.setPropertyValue(EXPERIMENT_ID, experimentId);
	}
	
	/**
     * Gets the experiment folder path from the specified widget properties
     * 
     * @param properties the widget properties from which to get the experiment folder path
     * @return the experiment folder path
     */
    public static String getExperimentFolder(WidgetProperties properties){
        return (String) properties.getPropertyValue(EXPERIMENT_FOLDER);
    }

    /**
     * Sets the experiment folder path for the specified widget properties.
     * 
     * @param properties the widget properties to which the experiment folder
     *        path should be assigned
     * @param experimentFolderPath the path to the experiment folder. This
     *        should be relative to the runtime\experiments folder.
     * @param request the experiment ID
     */
    public static void setExperimentFolder(WidgetProperties properties, String experimentFolderPath) {
        properties.setPropertyValue(EXPERIMENT_FOLDER, experimentFolderPath);
    }

	/**
	 * Gets whether or not the back button was pressed from the specified widget properties
	 * 
	 * @param properties the widget properties from which to get whether or not the back button was pressed
	 * @return whether or not the back button was pressed
	 */
	public static boolean getBackButtonPressed(WidgetProperties properties){
		return Boolean.TRUE.equals(properties.getPropertyValue(BACK_BUTTON_PRESSED));
	}
	
	/**
	 * Sets whether or not the back button was pressed for the specified widget properties.
	 * 
	 * @param properties the widget properties assign the property to
	 * @param request whether or not the back button was pressed
	 */
	public static void setBackButtonPressed(WidgetProperties properties, boolean backButtonPressed){
		properties.setPropertyValue(BACK_BUTTON_PRESSED, backButtonPressed);
	}
}
