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
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;


/**
 * Enumeration of the various Sensor Types
 * 
 * @author mhoffman
 *
 */
public class SensorTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<SensorTypeEnum> enumList = new ArrayList<SensorTypeEnum>(4);
    private static int index = 0;

    //public static final SensorTypeEnum TEMP_HUMIDITY = new SensorTypeEnum("TEMP_HUMIDITY", "Temp/Humidity");
    public static final SensorTypeEnum GSR = new SensorTypeEnum("GSR", "GSR");
    public static final SensorTypeEnum SINE_WAVE = new SensorTypeEnum("SINEWAVE", "Sine Wave");
    public static final SensorTypeEnum SELF_ASSESSMENT = new SensorTypeEnum("SELF_ASSESSMENT", "Self-Assessment");
    
    public static final SensorTypeEnum MOUSE_TH_SURROGATE = new SensorTypeEnum("MOUSE_TEMP_HUMIDITY_SURROGATE", "Mouse Temperature Humidity Surrogate");
    public static final SensorTypeEnum MOUSE_TH = new SensorTypeEnum("MOUSE_TEMP_HUMIDITY", "Mouse Temperature Humidity");
    //public static final SensorTypeEnum MOUSE_EVENT = new SensorTypeEnum("MOUSE_EVENT", "Mouse Event");
    
    public static final SensorTypeEnum EMO_COMPOSER = new SensorTypeEnum("EMOTIV_COMPOSER", "EmoComposer");
    public static final SensorTypeEnum EMOTIV = new SensorTypeEnum("EMOTIV", "Emotiv");
    
    public static final SensorTypeEnum Q = new SensorTypeEnum("Q", "Q");
    
    public static final SensorTypeEnum VHT_MULTISENSE = new SensorTypeEnum("VHT_Multisense", "VHT Multisense");

    public static final SensorTypeEnum KINECT = new SensorTypeEnum("KINECT", "Kinect");

    public static final SensorTypeEnum OS3D = new SensorTypeEnum("OS3D", "OS3D");
    public static final SensorTypeEnum BIOHARNESS = new SensorTypeEnum("BIOHARNESS", "BioHarness");
    
    public static final SensorTypeEnum MOTIVATION_SURROGATE = new SensorTypeEnum("MOTIVATION_SURROGATE", "Motivation Surrogate");
    public static final SensorTypeEnum EXPERTISE_SURROGATE = new SensorTypeEnum("EXPERTISE_SURROGATE", "Expertise Surrogate");
    
    public static final SensorTypeEnum MICROSOFT_BAND_2 = new SensorTypeEnum("MicrosoftBand2", "Microsoft Band 2");

    private SensorTypeEnum(String name, String displayName){
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
    public static SensorTypeEnum valueOf(String name)
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
    public static SensorTypeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<SensorTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
