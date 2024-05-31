/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;

/**
 * Parses the build.dependencies file and makes the attributes easily accessible
 * @author cdettmering
 */
public class BuildDependencies {
	
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(BuildDependencies.class);
	
	/** Delimiters that differentiate each module*/
	private static final String BUILD_PROPERTIES_FILE = PackageUtil.getConfiguration() + File.separator + "build.dependencies";
	private static final String DELIM_COMMON = "@common@";
	private static final String DELIM_DOMAIN = "@domain@";
	private static final String DELIM_GATEWAY = "@gateway@";
	private static final String DELIM_LMS = "@lms@";
	private static final String DELIM_UMS = "@ums@";
	private static final String DELIM_PED = "@ped@";
	private static final String DELIM_LEARNER = "@learner@";
	private static final String DELIM_SENSOR = "@sensor@";
	private static final String DELIM_TUTOR = "@tutor@";
	
	/** Generated list of dependencies for each module */
	private List<String> commonDependencies;
	private List<String> domainDependencies = new ArrayList<>();
	private List<String> gatewayDependencies = new ArrayList<>();
	private List<String> lmsDependencies = new ArrayList<>();
	private List<String> umsDependencies = new ArrayList<>();
	private List<String> pedDependencies = new ArrayList<>();
	private List<String> learnerDependencies = new ArrayList<>();
	private List<String> sensorDependencies = new ArrayList<>();
	private List<String> tutorDependencies = new ArrayList<>();
	
	
	/**
	 * Creates a new BuildDependencies which parses the dependency file.
	 */
	public BuildDependencies() {
		parse();
	}
	
	/**
	 * Gets the list of dependencies for the Common module.
	 * @return List of dependency paths for the Common module.
	 */
	public List<String> getCommonDependencies() {
		return commonDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the Domain module.
	 * @return List of dependency paths for the Domain module.
	 */
	public List<String> getDomainDependencies() {
		return domainDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the Gateway module.
	 * @return List of dependency paths for the Gateway module.
	 */
	public List<String> getGatewayDependencies() {
		return gatewayDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the LMS module.
	 * @return List of dependency paths for the LMS module.
	 */
	public List<String> getLmsDependencies() {
		return lmsDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the UMS module.
	 * @return List of dependency paths for the UMS module.
	 */
	public List<String> getUmsDependencies() {
		return umsDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the Ped module.
	 * @return List of dependency paths for the Ped module.
	 */
	public List<String> getPedDependencies() {
		return pedDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the Learner module.
	 * @return List of dependency paths for the Learner module.
	 */
	public List<String> getLearnerDependencies() {
		return learnerDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the Sensor module.
	 * @return List of dependency paths for the Sensor module.
	 */
	public List<String> getSensorDependencies() {
		return sensorDependencies;
	}
	
	/**
	 * Gets the list of dependencies for the Tutor module.
	 * @return List of dependency paths for the Tutor module.
	 */
	public List<String> getTutorDependencies() {
		return tutorDependencies;
	}
	
	/**
	 * Parses line using delim to generate the dependency list.
	 * 
	 * @param delim The delimiter that marks the line
	 * @param line The line as read from the file
	 * @return List of dependencies. Returns a fixed-size list backed by the specified array, i.e. can't be modified.
	 */
	private List<String> generateDependencies(String delim, String line) {
	    if(logger.isInfoEnabled()) {
	        logger.info("Generating dependency list for " + delim);
	    }
		line = line.replace(delim, "");
		String[] split = line.split(",");
		for(int i = 0; i < split.length; i++) {

		    if(logger.isInfoEnabled()) {
		        logger.info("Generated dependency - " + split[i]);
		    }
		}
		if(logger.isInfoEnabled()) {
		    logger.info("Dependency generation complete");
		}
		return new ArrayList<String>(Arrays.asList(split));
	}
	
	/**
	 * Parses the build.dependencies file.
	 */
	private void parse() {

		try {
		    List<String> lines = Files.readAllLines(Paths.get(BUILD_PROPERTIES_FILE));
			for(String line : lines){

			    if(line != null) {
					if(line.startsWith(DELIM_COMMON)) {
						commonDependencies = generateDependencies(DELIM_COMMON, line);
					} else if(line.startsWith(DELIM_DOMAIN)) {
						domainDependencies.addAll(generateDependencies(DELIM_DOMAIN, line));
					} else if(line.startsWith(DELIM_GATEWAY)) {
						gatewayDependencies.addAll(generateDependencies(DELIM_GATEWAY, line));
					} else if(line.startsWith(DELIM_LMS)) {
						lmsDependencies.addAll(generateDependencies(DELIM_LMS, line));
					} else if(line.startsWith(DELIM_UMS)) {
						umsDependencies.addAll(generateDependencies(DELIM_UMS, line));
					} else if(line.startsWith(DELIM_PED)) {
						pedDependencies.addAll(generateDependencies(DELIM_PED, line));
					} else if(line.startsWith(DELIM_LEARNER)) {
						learnerDependencies.addAll(generateDependencies(DELIM_LEARNER, line));
					} else if(line.startsWith(DELIM_SENSOR)) {
						sensorDependencies.addAll(generateDependencies(DELIM_SENSOR, line));
					} else if(line.startsWith(DELIM_TUTOR)) {
						tutorDependencies.addAll(generateDependencies(DELIM_TUTOR, line));
					}
				}
			}
			
			domainDependencies.addAll(commonDependencies);
			gatewayDependencies.addAll(commonDependencies);
			lmsDependencies.addAll(commonDependencies);
			umsDependencies.addAll(commonDependencies);
			pedDependencies.addAll(commonDependencies);
			learnerDependencies.addAll(commonDependencies);
			sensorDependencies.addAll(commonDependencies);
			tutorDependencies.addAll(commonDependencies);

		} catch(Exception e) {
			logger.error("Caught exception while parsing " + BUILD_PROPERTIES_FILE, e);
		}
	}
}
