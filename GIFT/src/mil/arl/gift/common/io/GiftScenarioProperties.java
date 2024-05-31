/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.math.BigDecimal;

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * The property values from a GIFT scenario property file. These property files are typically
 * located within "Training.Apps/maps/[Public | user]". They are used to provide additional
 * information for pre-packaged embedded applications (e.g. Unity). <br/>
 * <br/>
 * An example of the contents of the property file is as follows:
 *
 * <pre>
 * Name=Land Navigation Training
 * Author=GIFT 
 * Description=This environment is used for land navigation training. You can author assessments based on the learner's movement through the environment. 
 * TrainingAppType=UnityEmbedded 
 * ScenarioEntryPoint=ALPHA_UnityBuildOutput_2019-02-11_14-26-00/UnityBuildOutput_2019-02-11_14-26-00/index.html\
 * LearnerStartLocationAGL=1267.732177734375,3033.732177734375,282.8634033203125
 * </pre>
 * 
 * @author sharrison
 */
public class GiftScenarioProperties implements Serializable {

    /** default serial version number */
    private static final long serialVersionUID = 1L;

    /** The name of the GIFT scenario property file */
    public static final String FILENAME = "GiftScenario.properties";

    /** Name property key */
    public static final String NAME = "Name";
    /** Author property key */
    public static final String AUTHOR = "Author";
    /** Description property key */
    public static final String DESCRIPTION = "Description";
    /** Training Application Type property key */
    public static final String TYPE = "TrainingAppType";
    /** Scenario entry point property key */
    public static final String ENTRY_POINT = "ScenarioEntryPoint";
	/**	User's start location point property key */
	public static final String START_LOCATION = "LearnerStartLocationAGL";

    /** The name of the scenario */
    private String name;

    /** The author of the scenario */
    private String author;

    /** The scenario description */
    private String description;

    /** The scenario training application type */
    private TrainingApplicationEnum trainingApplicationType;

    /** The path to the scenario entry point */
    private String scenarioEntryPointPath;
    
    /** The user's starting point location */
    private SimulationCoordinate userStartLocation;

    /**
     * The parent folder of the property file that populated this object. The root of the path is a
     * subfolder of Training.Apps/maps (e.g. Public/folderName).
     */
    private String propertyParentPath;

    /**
     * Flag indicating if the entry point location was found.
     */
    private Boolean entryPointFound = null;

    /**
     * No arg constructor used to make the class GWT serializable.
     */
    private GiftScenarioProperties() {
    }

    /**
     * Constructor.
     *
     * @param name the name of the scenario. Can't be blank.
     * @param author the author of the scenario. Can't be blank.
     * @param description the scenario description. Can't be blank.
     * @param trainingApplicationType the scenario training application type. Can't be null.
     * @param propertyParentPath the parent folder of the property file that populated this object.
     *        The root of the path should be a subfolder of Training.Apps/maps (e.g.
     *        Public/folderName). Can't be blank.
     * @param scenarioEntryPointPath the path to the scenario entry point. The root of the path
     *        should be a subfolder of the provided property parent path. Can't be blank.
     * @param userStartLocation the coordinates of the user's start location. Can't be blank.    
     */
    public GiftScenarioProperties(String name, String author, String description,
            TrainingApplicationEnum trainingApplicationType, String propertyParentPath, String scenarioEntryPointPath, String userStartLocation) {
        this();

        setName(name);
        setAuthor(author);
        setDescription(description);
        setTrainingApplicationType(trainingApplicationType);
        setPropertyParentPath(propertyParentPath);
        setScenarioEntryPointPath(scenarioEntryPointPath);
        setUserStartLocation(userStartLocation);
    }

    /**
     * Retrieves the GIFT scenario name
     *
     * @return the name. Guaranteed to be non-null and non-empty.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the GIFT scenario name
     *
     * @param name the name to set. Can't be blank.
     */
    private void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The parameter 'name' cannot be blank.");
        }

        this.name = name;
    }

    /**
     * Retrieves the GIFT scenario author
     *
     * @return the author. Guaranteed to be non-null and non-empty.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the GIFT scenario author
     *
     * @param author the author to set. Can't be blank.
     */
    private void setAuthor(String author) {
        if (StringUtils.isBlank(author)) {
            throw new IllegalArgumentException("The parameter 'author' cannot be blank.");
        }

        this.author = author;
    }

    /**
     * Retrieves the GIFT scenario description
     *
     * @return the description. Guaranteed to be non-null and non-empty.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the GIFT scenario description
     *
     * @param description the description to set. Can't be blank.
     */
    private void setDescription(String description) {
        if (StringUtils.isBlank(description)) {
            throw new IllegalArgumentException("The parameter 'description' cannot be blank.");
        }

        this.description = description;
    }

    /**
     * Retrieves the GIFT scenario training application type
     *
     * @return the training application type. Guaranteed to be non-null.
     */
    public TrainingApplicationEnum getTrainingApplicationType() {
        return trainingApplicationType;
    }

    /**
     * Sets the GIFT scenario training application type
     *
     * @param trainingApplicationType the training application type to set. Can't be null.
     */
    private void setTrainingApplicationType(TrainingApplicationEnum trainingApplicationType) {
        if (trainingApplicationType == null) {
            throw new IllegalArgumentException("The parameter 'trainingApplicationType' cannot be null.");
        }

        this.trainingApplicationType = trainingApplicationType;
    }

    /**
     * Retrieves the path to the GIFT scenario entry point. The root of the path is a subfolder of
     * {@link #propertyParentPath}.
     *
     * @return the scenario entry point. Guaranteed to be non-null and non-empty.
     */
    public String getScenarioEntryPointPath() {
        return scenarioEntryPointPath;
    }

    /**
     * Retrieves the path to the GIFT scenario entry point including the property file parent path.
     * The root of the path is a subfolder of Training.Apps/maps/WrapResources (e.g. Public/folderName).
     * 
     * @return the scenario entry point. Guaranteed to be non-null and non-empty.
     */
    public String getScenarioEntryPointPathWithParentPath() {
        String parentPath = getPropertyParentPath();
        if (!parentPath.endsWith("/")) {
            parentPath += "/";
        }

        return parentPath + getScenarioEntryPointPath();
    }

    /**
     * Sets the path to the GIFT scenario entry point. The root of the path is a subfolder of
     * {@link #propertyParentPath}.
     *
     * @param scenarioEntryPointPath the scenario entry point. Can't be blank.
     */
    private void setScenarioEntryPointPath(String scenarioEntryPointPath) {
        if (StringUtils.isBlank(scenarioEntryPointPath)) {
            throw new IllegalArgumentException("The parameter 'scenarioEntryPointPath' cannot be blank.");
        }

        this.scenarioEntryPointPath = scenarioEntryPointPath;
    }

    /**
     * Retrieves the parent folder of the property file that populated this object. The root of the
     * path is a subfolder of Training.Apps/maps (e.g. Public/folderName).
     *
     * @return the property path. Guaranteed to be non-null and non-empty.
     */
    public String getPropertyParentPath() {
        return propertyParentPath;
    }

    /**
     * Sets the parent folder of the property file that populated this object. The root of the path
     * is a subfolder of Training.Apps/maps (e.g. Public/folderName).
     *
     * @param propertyParentPath the property path to set. Can't be blank.
     */
    private void setPropertyParentPath(String propertyParentPath) {
        if (StringUtils.isBlank(propertyParentPath)) {
            throw new IllegalArgumentException("The parameter 'propertyParentPath' cannot be blank.");
        }

        this.propertyParentPath = propertyParentPath;
    }

    /**
     * Retrieves the flag indicating if the entry point at {@link #scenarioEntryPointPath} was
     * found.
     * 
     * @return true if the entry point was found at the specified location; false if it was searched
     *         for but not found. Can return null if no check was performed on whether it exists or
     *         not.
     */
    public Boolean isEntryPointFound() {
        return entryPointFound;
    }

    /**
     * Sets the flag indicating if the entry point at {@link #scenarioEntryPointPath} was found.
     * 
     * @param entryPointFound true if the entry point was found at the specified location; false if
     *        it was searched for but not found.
     */
    public void setEntryPointFound(boolean entryPointFound) {
        this.entryPointFound = Boolean.valueOf(entryPointFound);
    }

    /**	
     * Retrieves the user's start location simulation coordinate which is an object
     * 	that contains the X, Y, and Z (A.K.A elevation) coordinate values.
     * 
     * @return userStartLocation  is the coordinates that the user starts at on the map given
     * 			from the GiftScenario.propeties file (e.g. "1267.732177734375,3033.732177734375,282.8634033203125").
     * 			The value is checked for and can't be empty.
     */
    public SimulationCoordinate getUserStartLocation() {
		return userStartLocation;
	}
    
    
	/**
	 * Sets the user's start location coordinates by creating a new simulation coordinate
	 * object and then split and assign's the X, Y and Z (A.K.A Elevation) values given from
	 * the GiftScenario.properties file.
	 * 
	 * @param userStartLocation is the coordinates that the user starts at on the map given
	 * 		  from the GiftScenario.propeties file (e.g. "1267.732177734375,3033.732177734375,282.8634033203125").
	 *		  The value is checked for and can't be empty.
	 */
	private void setUserStartLocation(String userStartLocation) {
        if (userStartLocation.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'description' cannot be blank.");
        }

        this.userStartLocation = new SimulationCoordinate(userStartLocation);
	}
	
    /**	
     * 
     * @author mweinert
     */
    public static class SimulationCoordinate implements Serializable {
    	/**
         * default
         */
        private static final long serialVersionUID = 1L;

        /** x coordinate for the simulation coordinate object */
    	private BigDecimal x;
    	
    	/** y coordinate for the simulation coordinate object */
    	private BigDecimal y;
    	
    	/** z coordinate for the simulation coordinate object */
    	private BigDecimal z;
    	
    	/** No arg constructor */
    	@SuppressWarnings("unused")
		private SimulationCoordinate() {
    	}
    	
    	/**	
    	 * 
    	 * @param coordinateStr
    	 */
    	public SimulationCoordinate(String coordinateStr) {
    		
          String[] split = coordinateStr.split(Constants.COMMA);
    		
      	  /* Convert X value */
      	  try {
      		  setX(new BigDecimal(split[0].trim()));
      	  } catch (NumberFormatException nfe) {
      	      throw new IllegalArgumentException(
      	              "The property X coordinate value '" + split[0] + "' is not a valid decimal.", nfe);
      	  }

      	  /* Convert Y value */
      	  try {
      	      setY(new BigDecimal(split[1].trim()));
      	  } catch (NumberFormatException nfe) {
      	      throw new IllegalArgumentException(
      	              "The property Elevation coordinate value '" + split[1] + "' is not a valid decimal.", nfe);
      	  }
      	  
      	  /* Convert Elevation value */
      	  try {
      	      setZ(new BigDecimal(split[2].trim()));
      	  } catch (NumberFormatException nfe) {
      	      throw new IllegalArgumentException(
      	              "The property Elevation coordinate value '" + split[2] + "' is not a valid decimal.", nfe);
      	  }
    		
    	}

		/** Retrieves the simulation coordinate's X value
		 * @return x-coordinate
		 */
		public BigDecimal getX() {
			return x;
		}

		/**	Sets the X value to for the simulation coordinate object.
		 * 
		 * @param x value to set the x-coordinate to
		 */
		private void setX(BigDecimal x) {
			this.x = x;
		}

		/**	Retrieves the simulation coordinate's Y value
		 * 
		 * @return y coordinate
		 */
		public BigDecimal getY() {
			return y;
		}

		/**	Sets the Y value to for the simulation coordinate object.
		 * 
		 * @param y value to set the y-coordinate to
		 */
		private void setY(BigDecimal y) {
			this.y = y;
		}

		/** Retrieves the simulation coordinate's Z value
		 * 
		 * @return z (A.K.A. elevation) coordinate
		 */
		public BigDecimal getZ() {
			return z;
		}

		/**	Sets the Z (A.K.A Elevation) value to for the simulation coordinate object.
		 * 
		 * @param z value to set the z-coordinate to
		 */
		private void setZ(BigDecimal z) {
			this.z = z;
		}
		
    	@Override
    	public String toString() {
        	StringBuffer sb = new StringBuffer();
			sb.append(x).append(",").append(y).append(",").append(z);
			return sb.toString();

    	}
	}

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[GiftScenarioProperties: ");
        sb.append(NAME).append(" = ").append(getName());
        sb.append(", ").append(AUTHOR).append(" = ").append(getAuthor());
        sb.append(", ").append(DESCRIPTION).append(" = ").append(getDescription());
        sb.append(", ").append(TYPE).append(" = ").append(getTrainingApplicationType());
        sb.append(", ").append(ENTRY_POINT).append(" = ").append(getScenarioEntryPointPath());
        sb.append(", ").append(START_LOCATION).append(" = ").append(getUserStartLocation().toString());
        sb.append(", propertyParentPath = ").append(getPropertyParentPath());
        sb.append(", entryPointFound = ").append(isEntryPointFound());
        sb.append("]");

        return sb.toString();
    }
}
