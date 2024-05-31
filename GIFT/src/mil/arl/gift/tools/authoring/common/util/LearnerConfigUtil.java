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
import mil.arl.gift.learner.clusterer.AbstractClassifier;
import mil.arl.gift.learner.clusterer.data.AbstractSensorTranslator;
import mil.arl.gift.learner.predictor.AbstractPredictor;

/**
 * This class contains utility methods for retrieving information for authoring Learner Configuration files.
 * 
 * @author mhoffman
 *
 */
public class LearnerConfigUtil {

    /**
     * Retrieve the list of Learner module classifier implementations available.
     * 
     * @return List<String> - the classifier class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<String> getClassiferImplementations() throws ClassNotFoundException, IOException{
        
        List<String> values = new ArrayList<>();
        
        String packageName = "mil.arl.gift.learner.clusterer";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractClassifier.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }
    
    /**
     * Retrieve the list of Learner module predictor implementations available.
     * 
     * @return List<String> - the predictor class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<String> getPredictorImplementations() throws ClassNotFoundException, IOException{
        
        List<String> values = new ArrayList<>();
        
        String packageName = "mil.arl.gift.learner.predictor";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractPredictor.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }
    
    /**
     * Retrieve the list of Learner module sensor data translator implementations available.
     * 
     * @return List<String> - the translator class names without the "mil.arl.gift" prefix.
     * @throws IOException if the source package was not found using the current threads class loader
     * @throws ClassNotFoundException if a potential matching class could not be found using the class loader
     */
    public static List<String> getTranslatorImplementations() throws ClassNotFoundException, IOException{
        
        List<String> values = new ArrayList<>();
        
        String packageName = "mil.arl.gift.learner.clusterer.data";
        List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractSensorTranslator.class);
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }
}
