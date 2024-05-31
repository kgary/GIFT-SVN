/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.Map;

import mil.arl.gift.learner.AbstractConfiguration;

/**
 * This class contains the configuration for a classifier class
 * 
 * @author mhoffman
 *
 */
public class ClassifierConfiguration extends AbstractConfiguration {

	/**
	 * Class constructor 
	 * 
     * @param clazz - the class which implements the particular type of learner module class being configured
     * @param properties - contains properties to apply to the learner module class being configured
	 */
	public ClassifierConfiguration(Class<?> clazz, Map<String, String> properties){
		super(clazz, properties);
		
	}
	
}
