/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.sensor.filter.AbstractSensorFilter;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.writer.AbstractWriter;

/**
 * This class contains utility methods for retrieving information for authoring Sensor Configuration files.
 * 
 * @author mhoffman
 *
 */
public class SensorConfigUtil {
    
    /**
     * Retrieve the list of Sensor module writer implementations available.
     * 
     * @return List<String> - the writer class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<String> getWriterImplementations() throws ClassNotFoundException, IOException{
        
        List<String> values = new ArrayList<>();
        
        String packageName = "mil.arl.gift.sensor.writer";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractWriter.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }
    
    /**
     * Retrieve the list of Sensor module sensor implementations available.
     * 
     * @return List<String> - the sensor class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<String> getSensorImplementations() throws ClassNotFoundException, IOException{
        
        List<String> values = new ArrayList<>();
        
        String packageName = "mil.arl.gift.sensor.impl";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractSensor.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }

    /**
     * Retrieve the list of Sensor module sensor filter implementations available.
     * 
     * @return List<String> - the sensor filter class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<String> getSensorFiterImplementations() throws ClassNotFoundException, IOException{
        
        List<String> values = new ArrayList<>();
        
        String packageName = "mil.arl.gift.sensor.filter";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractSensorFilter.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }
}
