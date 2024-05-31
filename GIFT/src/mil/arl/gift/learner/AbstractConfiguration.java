/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner;

import java.util.Map;

/**
 * This is the base class for learner module configuration classes
 * 
 * @author mhoffman
 *
 */
public class AbstractConfiguration {

	/** the class which implements the particular type of learner module class being configured */
	protected Class<?> clazz;
	
	/** contains properties to apply to the learner module class being configured */
	protected Map<String, String> properties;
	
	/**
	 * Class constructor - set class attributes
	 * 
	 * @param clazz - the class which implements the particular type of learner module class being configured
	 * @param properties - contains properties to apply to the learner module class being configured
	 */
	public AbstractConfiguration(Class<?> clazz, Map<String, String> properties){
		this.clazz = clazz;
		this.properties = properties;
	}
	
	/**
	 * Return the implementation class
	 * 
	 * @return Class<?>
	 */
	public Class<?> getImplementationClass(){
		return clazz;
	}
	
	/**
	 * Return the properties for this configuration
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getProperties(){
		return properties;
	}
}
