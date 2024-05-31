/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.File;


//TODO: create toPathName method which takes in relative path from root and returns absolute path
//TODO: create toPackageAbstraction method which takes in absolute path and return relative path from root
/**
 * This class provides various package locations within the project.
 *
 * @author mhoffman
 *
 */
public class PackageUtil {

	/** the package prefix to gift */
	private static final String ROOT_PACKAGE = "mil.arl.gift";

	private static final String SRC_DIR = "src";

	/** the output directory */
	private static final String OUTPUT_DIR 	= "output";

	/** the configuration directory */
	private static final String CONFIG_DIR	= "config";

	/** the data directory */
	private static final String DATA_DIR = "data";

	/** the output logger directory */
	private static final String LOG_DIR = OUTPUT_DIR + File.separator + "logger";

	/** the output sensor directory */
	private static final String SENSOR_OUTPUT_DIR = OUTPUT_DIR + File.separator + "sensor";

	/** the output logger message directory */
	private static final String MESSAGE_LOG_DIR = LOG_DIR + File.separator + "message";

	/** the output logger tools directory */
    private static final String TOOLS_LOG_DIR = LOG_DIR + File.separator + "tools";

	/** the output logger module directory */
	private static final String MODULE_LOG_DIR = LOG_DIR + File.separator + "module";

	/** the test data directory */
	private static final String TEST_DATA = DATA_DIR + File.separator + "tests";
	
	/** the directory where output files associated with domain sessions (i.e. message logs, bookmarks, sensor data, etc.) are written */
    private static final String DOMAIN_SESSION_DIR = OUTPUT_DIR + File.separator + "domainSessions";

    /** the Wrap resources directory */
    private static final String TRAINING_APPS_WRAP_DIR = "WrapResources";

    /** the maps directory within Training.Apps */
    private static final String TRAINING_APPS_MAPS_DIR = "maps";

    /** the Land Nav scenario directory within Training.Apps/maps */
    private static final String LAND_NAV_SCENARIO_DIR = TRAINING_APPS_WRAP_DIR 
            + File.separator + TRAINING_APPS_MAPS_DIR
            + File.separator + "Public"
            + File.separator + "LandNav_Standalone_HD";

	private PackageUtil(){}

	/**
	 * Return the test data directory path.
	 *
	 * @return String
	 */
	public static String getTestData(){
	    return TEST_DATA;
	}
	
	/**
     * Return the domain sessions directory path (output files associated with message logs, bookmarks, sensor data, etc.).
     *
     * @return {@value #DOMAIN_SESSION_DIR}
     */
    public static String getDomainSessions(){
        return DOMAIN_SESSION_DIR;
    }

	/**
	 * Return the output directory path
	 *
	 * @return String
	 */
	public static String getOutput(){
		return OUTPUT_DIR;
	}

	/**
	 * Return the configuration directory path
	 *
	 * @return String
	 */
	public static String getConfiguration(){
		return CONFIG_DIR;
	}

	/**
	 * Return the data directory path
	 *
	 * @return String
	 */
	public static String getData(){
	    return DATA_DIR;
	}

	/**
	 * Return the 'gift' source package prefix
	 *
	 * @return "mil.arl.gift"
	 */
	public static String getRoot(){
		return ROOT_PACKAGE;
	}

	/**
	 * Return the message log directory path
	 *
	 * @return String
	 */
	public static String getMessageLog(){
		return MESSAGE_LOG_DIR;
	}

	/**
     * Return the tools log directory path
     *
     * @return String
     */
    public static String getToolLog(){
        return TOOLS_LOG_DIR;
    }

	/**
	 * Return the module log directory path
	 *
	 * @return String
	 */
	public static String getModuleLog(){
		return MODULE_LOG_DIR;
	}

	/**
	 * Return the src directory path
	 *
	 * @return String
	 */
	public static String getSource(){
	    return SRC_DIR;
	}

	/**
	 * Return the output sensor directory
	 *
	 * @return String
	 */
	public static String getSensorOutput(){
	    return SENSOR_OUTPUT_DIR;
	}

    /**
     * Return the directory containing Wrap images and embedded applications
     *
     * @return String
     */
    public static String getWrapResourcesDir() {
        return TRAINING_APPS_WRAP_DIR;
    }

    /**
     * Return the Training.Apps maps directory
     *
     * @return String
     */
    public static String getTrainingAppsMaps(){
        return TRAINING_APPS_MAPS_DIR;
    }

    /**
     * Return the Land Nav scenario directory
     *
     * @return String
     */
    public static String getLandNavScenario() {
        return LAND_NAV_SCENARIO_DIR;
    }
}
