/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;

/**
 * This class contains utility methods for retrieving information for authoring GIFT courses.
 * 
 * @author mhoffman
 *
 */
public class CourseUtil {

    /**
     * Retrieve the list of Gateway module interop implementations (i.e. plugins) available.
     * 
     * @return List<String> - the interop class names without the "mil.arl.gift" prefix.
     * @throws DetailedException most common causes are:
     * 1. if the source package was not found using the current threads class loader (IOException)
     * 2. if a potential matching class could not be found using the class loader (NoClassDefFoundError/ClassNotFoundException)
     */
    public static ArrayList<String> getInteropImplementations() throws DetailedException{
        
        ArrayList<String> values = new ArrayList<String>();
        
        String packageName = "mil.arl.gift.gateway.interop";
        List<Class<?>> classes;
        try {
            classes = ClassFinderUtil.getSubClassesOf(packageName, AbstractInteropInterface.class);
        } catch (Throwable e) {
            throw new DetailedException("Failed to retrieve the Gateway interop plugin classes.", 
                    "An exception was caught when retrieving the subclasses of "+AbstractInteropInterface.class.getCanonicalName()+
                    " in this GIFT instance.  Please check the runtime classpath as this is most likely new logic that has not been fully implemented and tested yet.", e);
        }
        
        for(Class<?> clazz : classes){
            //remove package prefix of "mil.arl.gift."                    
            values.add(clazz.getName().replaceFirst("mil.arl.gift.", ""));
        }
        
        return values;
    }
}
