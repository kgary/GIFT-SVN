/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.EnumerationNotFoundException;


/**
 * Enumeration of the various Training Applications GIFT can communicate with
 * during course execution.
 *
 * @author mhoffman
 *
 */
public class TrainingApplicationEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;

    private static List<TrainingApplicationEnum> enumList = new ArrayList<>(6);
    private static int index = 0;

    // *** Alphabetized ***
    public static final TrainingApplicationEnum ARES = new TrainingApplicationEnum("ARES", "Augmented REality Sandtable (ARES)");
    public static final TrainingApplicationEnum SIMPLE_EXAMPLE_TA = new TrainingApplicationEnum("SimpleExampleTrainingApp", "Demo Application");
    public static final TrainingApplicationEnum DE_TESTBED = new TrainingApplicationEnum("DE_Testbed", "Dynamic Environment Testbed");
    /* HAVEN is a legacy name for Synthetic Environment Sandbox. 
     * The display name has been updated, but the name has been left as "HAVEN" to keep compatibility with older logs. */
    public static final TrainingApplicationEnum HAVEN = new TrainingApplicationEnum("HAVEN", "Synthetic Environment Sandbox");
    public static final TrainingApplicationEnum MOBILE_DEVICE_EVENTS = new TrainingApplicationEnum("MobileDeviceEvents", "Mobile Device Events");
    public static final TrainingApplicationEnum POWERPOINT = new TrainingApplicationEnum("PowerPoint", "PowerPoint");
    public static final TrainingApplicationEnum RIDE = new TrainingApplicationEnum("RIDE", "Rapid Integration & Development Environment");
    public static final TrainingApplicationEnum SUDOKU = new TrainingApplicationEnum("Sudoku", "Sudoku");
    public static final TrainingApplicationEnum TC3 = new TrainingApplicationEnum("TC3", "Tactical Combat Casualty Care (TC3)");
    public static final TrainingApplicationEnum UNITY_EMBEDDED = new TrainingApplicationEnum("UnityEmbedded", "TUI Embedded Unity");
    public static final TrainingApplicationEnum UNITY_DESKTOP = new TrainingApplicationEnum("UnityDesktop", "Unity Desktop");
    public static final TrainingApplicationEnum VBS = new TrainingApplicationEnum("VBS", "Virtual Battle Space (VBS)");
    public static final TrainingApplicationEnum VR_ENGAGE = new TrainingApplicationEnum("VR-Engage", "VR-Engage");
    
    /** The string used to represent GDC coordinates for getValidCoordinateTypes. */
    public static final String GDC_COORDINATE_NAME = "GDC";
    
    /** The string used to represent GCC coordinates for getValidCoordinateTypes. */
    public static final String GCC_COORDINATE_NAME = "GCC";
    
    /** The string used to represent AGL coordinates for getValidCoordinateTypes. */
    public static final String AGL_COORDINATE_NAME = "AGL";

    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public because it is Serializable
     */
    public TrainingApplicationEnum() {
        super();
    }

    private TrainingApplicationEnum(String name, String displayName){
    	super(index++, name, displayName);
    	enumList.add(this);
    }

    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static TrainingApplicationEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static TrainingApplicationEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<TrainingApplicationEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }

    /**
     * Gets the icon for the given training application type
     *
     * @param type the type of training application for which an icon is needed
     * @return the icon corresponding to the training application type
     */
    public static String getTrainingAppTypeIcon(TrainingApplicationEnum type) {

        if (TrainingApplicationEnum.POWERPOINT.equals(type)) {
            return "images/PowerPoint.png";

        } else if (TrainingApplicationEnum.VBS.equals(type)) {
            return "images/VBS3.png";

        } else if (TrainingApplicationEnum.TC3.equals(type)) {
            return "images/vmedic_icon.png";

        } else if (TrainingApplicationEnum.ARES.equals(type)) {
            return "images/ARES.png";

        } else if (TrainingApplicationEnum.DE_TESTBED.equals(type)) {
            return "images/Excavator.png";

        } else if (TrainingApplicationEnum.UNITY_EMBEDDED.equals(type)
                || TrainingApplicationEnum.UNITY_DESKTOP.equals(type)) {
            return "images/Unity.png";

        } else if (TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.equals(type)) {
            return "images/mobile-app.png";

        } else if (TrainingApplicationEnum.VR_ENGAGE.equals(type)) {
            return "images/Engage.png";

        } else if (TrainingApplicationEnum.HAVEN.equals(type)) {
            return "images/seSandbox_Logo.png";
            
        } else if (TrainingApplicationEnum.RIDE.equals(type)) {
            return "images/RIDE_Logo.png";
            
        } else {
            return "images/transitions/ta.png";
        }
    }
    
    /**
     * Gets the valid coordinate types for the given training application type
     * @param trainingAppType the type of training application for which coordinates are needed
     * @return A set of strings representing the valid coordinate types
     * e.g. "GDC", "GCC", and "AGL"
     */
    public static Set<String> getValidCoordinateTypes(TrainingApplicationEnum trainingAppType) {
        
        Set<String> coordinateTypeSet = new HashSet<String>();
        
        // determine which coordinate types are allowed by training application type
        if(trainingAppType.equals(TrainingApplicationEnum.VBS)){
            coordinateTypeSet.add(GCC_COORDINATE_NAME);
            coordinateTypeSet.add(AGL_COORDINATE_NAME);
        } else if(trainingAppType.equals(TrainingApplicationEnum.VR_ENGAGE)) {
            coordinateTypeSet.add(GCC_COORDINATE_NAME);
            coordinateTypeSet.add(GDC_COORDINATE_NAME);
        } else if(trainingAppType.equals(TrainingApplicationEnum.HAVEN)) {
            //GCC only
            coordinateTypeSet.add(GCC_COORDINATE_NAME);
        } else if(trainingAppType.equals(TrainingApplicationEnum.RIDE)) {
            //GCC only
            coordinateTypeSet.add(GCC_COORDINATE_NAME);
        }else if(trainingAppType.equals(TrainingApplicationEnum.UNITY_EMBEDDED)){
            //AGL only
            coordinateTypeSet.add(AGL_COORDINATE_NAME);
        }else if(trainingAppType.equals(TrainingApplicationEnum.UNITY_DESKTOP)){
            //AGL only
            coordinateTypeSet.add(AGL_COORDINATE_NAME);
        }else if(trainingAppType.equals(TrainingApplicationEnum.MOBILE_DEVICE_EVENTS) ||
                trainingAppType.equals(TrainingApplicationEnum.ARES)){
            //GDC only
            coordinateTypeSet.add(GDC_COORDINATE_NAME);
        }else if(trainingAppType.equals(TrainingApplicationEnum.DE_TESTBED) ||
                trainingAppType.equals(TrainingApplicationEnum.TC3)){
            //GCC only
            coordinateTypeSet.add(GCC_COORDINATE_NAME);
        }
        
        return coordinateTypeSet;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        } else if (obj instanceof TrainingApplicationEnum) {

        	TrainingApplicationEnum enumObj = (TrainingApplicationEnum) obj;

            return enumObj.getValue() == getValue();
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 5;
        hash = hash * 31 + getValue();

        return hash;
    }

}
