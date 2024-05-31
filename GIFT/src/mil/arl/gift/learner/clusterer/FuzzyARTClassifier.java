/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;

/**
 * This classifier implements the Fuzzy Adaptive Resonance Theory (ART) logic found in artmap.py
 * Python file provided by Keith @ RDECOM.
 * 
 * @author mhoffman
 *
 */
public class FuzzyARTClassifier extends AbstractClassifier {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FuzzyARTClassifier.class);
    private static boolean isDebugEnabled = logger.isDebugEnabled();
    
    /** classifier properties */
    public static final String MAX_NUM_CATEGORIES = "maxNumCategories";
    public static final String VIGILANCE = "vigilance";
    public static final String BIAS = "bias";
    public static final String LEARNING_RATE = "learningRate";
    public static final String COMPLEMENT_CODE = "complementCode";
    
    /** default property values */
    private static final int DEFAULT_MAX_CATEGORIES = Integer.MAX_VALUE;
    private static final double DEFAULT_VIGILANCE = 0.75;
    private static final double DEFAULT_BIAS = 0.000001;
    private static final double DEFAULT_LEARNING_RATE = 1.0;
    private static final boolean DEFAULT_COMPLEMENT = false;
    
    private static final int NONE_CATEGORY = -1;
    
    /** the maximum number of categories that can be created */
    protected int maxNumCategories = DEFAULT_MAX_CATEGORIES;
    
    /** 
     * standard threshold
     *  	0 means it rarely says "I do not know"
     *      (if it does not know during learning, it will make a new category)
     */
    protected double vigilance = DEFAULT_VIGILANCE;
    
    /** avoid dividing by zero */
    protected double bias = DEFAULT_BIAS;
    
    /** 1.0 is "fast learning" */
    protected double learningRate = DEFAULT_LEARNING_RATE;
    
    /** often needed for binary codes */
    protected boolean complement = DEFAULT_COMPLEMENT;
    
    /** the current number of categories */
    private int numCategories = 0;
    
    /** size of data patterns and weights */
    protected int numFeatures;

    private List<List<Double>> weight = new ArrayList<List<Double>>();
    
    /** the learner's engagement state */
	protected LearnerStateAttribute state;
    
	/**
	 * Default constructor
	 */
    public FuzzyARTClassifier(){
    	
    }
    
	/**
	 * Return the learner engagement state
	 * 
	 * @return LearnerState
	 */
    @Override
	public LearnerStateAttribute getState(){
		return state;
	}
	
	@Override
	public Object getCurrentData(){
		return -1;
	}
    
	@Override
    public void configureByProperties(Map<String, String> properties) throws ConfigurationException{
        
        super.configureByProperties(properties);
        
        String value = properties.get(MAX_NUM_CATEGORIES);
        if(value != null){
        	maxNumCategories = Integer.valueOf(value);
        }
        
        value = properties.get(VIGILANCE);
        if(value != null){
        	vigilance = Double.parseDouble(value);
        }
        
        value = properties.get(BIAS);
        if(value != null){
        	bias = Double.parseDouble(value);
        }
        
        value = properties.get(LEARNING_RATE);
        if(value != null){
        	learningRate = Double.parseDouble(value);
        }
        
        value = properties.get(COMPLEMENT_CODE);
        if(value != null){
        	complement = Boolean.parseBoolean(value);
        	
        	if(complement){
        		numFeatures *= 2;
        	}
        }
        
        if(isDebugEnabled){
        	logger.debug("configured classifier -> "+this);
        }
    }
    
    /**
     * Train all patterns
     * 
     * @param dataPoints the data to train with
     * @return boolean always returns true
     */
    protected boolean train(List<DataPoint> dataPoints){
    	   	
    	logger.debug("training started");
    	
    	for(DataPoint dPt : dataPoints){
    		step(dPt);
    	}
    	
    	logger.debug("training finished");
    	
    	return true;
    }
    
    /**
     * Train one pattern
     * 
     * @param dataPoint contains values for dimensions
     * @return int - the category number
     */
    protected int step(DataPoint dataPoint){
    	
    	if(complement){
    		dataPoint = complementCode(dataPoint);
    	}
    	
    	if(isDebugEnabled){
    		logger.debug("training input: "+dataPoint);
    	}
    	
    	List<Double> categories = activateCategories(dataPoint);
    	List<ActivationCategory> sorted = new ArrayList<ActivationCategory>(categories.size());
    	for(int i = 0; i < categories.size(); i++){
    		ActivationCategory cat = new ActivationCategory(categories.get(i), i);
    		sorted.add(cat);
    	}
    	Collections.sort(sorted);
    	Collections.reverse(sorted);
    	
    	int currentSortedIndex = 0;
    	while(true){
    		if(numCategories == 0){
    			//no categories yet (first time): add a category
    			List<Double> category = new ArrayList<Double>(numFeatures);
    			for(int i = 0; i < numFeatures; i++){
    				category.add(1.0);
    			}
    			weight.add(category);
    			updateWeights(dataPoint, weight.get(0));
    			numCategories++;
    			
    			if(isDebugEnabled){
    				logger.debug("created category 0, current weight = "+weight.get(0));
    			}
    			
    			//winning category
    			return 0;
    		}
    		
    		//at least one category exists
    		int currentCategory = sorted.get(currentSortedIndex).getCategory();
    		List<Double> currentWeightVector = weight.get(currentCategory);
    		double match = calculateMatch(dataPoint, currentWeightVector);
    		
    		//if match is close enough
    		if(match > vigilance){
    			//update the weights
    			updateWeights(dataPoint, weight.get(currentCategory));
    			
    			if(isDebugEnabled){
    				logger.debug("matched "+dataPoint+" to category "+currentCategory+", weight of that category is "+weight.get(currentCategory));
    			}
    			
    			return currentCategory;
    			
    		}
    		
    		if(currentSortedIndex == (numCategories - 1)){
    				
    				if(maxNumCategories != Integer.MAX_VALUE && numCategories == maxNumCategories){
    					//no winner
    					
    	    			if(isDebugEnabled){
    	    				logger.debug("unable to match to category");
    	    			}
    	    			
    					return NONE_CATEGORY;
    					
    				}
    				
    				List<Double> category = new ArrayList<Double>(numFeatures);
                    for(int i = 0; i < numFeatures; i++){
                        category.add(1.0);
                    }
                    weight.add(category);
                    updateWeights(dataPoint, weight.get(weight.size()-1));
                    numCategories++;
                    
                    if(isDebugEnabled){
                        logger.debug("created category "+(numCategories-1)+" for "+dataPoint+", current weight = "+weight.get(weight.size()-1));
                    }
                    
                    return numCategories - 1;    				
            }
    		
    		currentSortedIndex++;
    	}
    }
    
    /**
     * Propagate the activation to the category layer.
     * 
     * @param dataPoint
     * @return List<Double> - activations
     */
    private List<Double> activateCategories(DataPoint dataPoint){
    	
    	List<Double> categories = new ArrayList<Double>();
    	List<Double> dimensions = dataPoint.getDimensions();
    	
    	double match, weight;
    	for(int j = 0; j < numCategories; j++){
    		match = 0;
    		weight = 0;
    		for(int i = 0; i < numFeatures; i++){
    			//TODO: j & numcategories, i & numfeatures and this.weight need closer connection
    			match += Math.min(dimensions.get(i), this.weight.get(j).get(i));
    			weight += this.weight.get(j).get(i);
    		}
    		categories.add(match / (bias + weight));
    	}
    	
    	return categories;
    }
    
    /**
     * Update the weights
     * 
     * @param dataPoint
     * @param category
     * @return boolean - true if the weights change
     */
    private boolean updateWeights(DataPoint dataPoint, List<Double> category){
    	
    	boolean change = false;
    	for(int i = 0; i < dataPoint.getDimensions().size(); i++){
    		if(dataPoint.getDimensions().get(i) < category.get(i)){
    			category.set(i, (learningRate * dataPoint.getDimensions().get(i)) + ((1-learningRate) * category.get(i)));
    			change = true;
    		}
    	}
    	
    	return change;
    }
    
    /**
     * 
     * @param dataPoint
     * @param b
     * @return double
     */
    private static double calculateMatch(DataPoint dataPoint, List<Double> b){
    	return fuzzyAnd(dataPoint, b);
    }
    
    /**
     * Find the closest model vector (category)
     * 
     * @param dataPoint - a data point to categorize
     * @return int - the category closest to the data point
     */
    protected int categorize(DataPoint dataPoint){
    	
    	if(complement){
    		dataPoint = complementCode(dataPoint);
    	}
    	
    	if(isDebugEnabled){
    		logger.debug("categorize input: "+dataPoint);
    	}
    	
    	List<Double> categories = activateCategories(dataPoint);
    	List<ActivationCategory> sorted = new ArrayList<ActivationCategory>(categories.size());
    	for(int i = 0; i < categories.size(); i++){
    		ActivationCategory cat = new ActivationCategory(categories.get(i), i);
    		sorted.add(cat);
    	}
    	Collections.sort(sorted);
    	Collections.reverse(sorted);
    	
    	double match = 0;
    	int currentSortedIndex = 0, currentCategory;
    	while(true){
    		
    		currentCategory = sorted.get(currentSortedIndex).getCategory();
        	List<Double> currentWeightVector = weight.get(currentCategory);
        	match = calculateMatch(dataPoint, currentWeightVector);
        	
        	if(match > vigilance){
        		
            	if(isDebugEnabled){
            		logger.debug("found match to category "+currentCategory);
            	}
            	
        		return currentCategory;
        	}
        	
            if(currentSortedIndex < numCategories){
                currentSortedIndex++;
            }else{
                //end of the line
                
                if(isDebugEnabled){
                    logger.debug("unable to find match to a category");
                }
                
                return NONE_CATEGORY;
            }
    	}

    }
    
    /**
     * Constructs complement code pairs [n, 1-n, ...] for n in data point
     * 
     * @param dataPoint - contains values for dimensions
     * @return DataPoint - the complement data point object
     */
    private DataPoint complementCode(DataPoint dataPoint){
    	
    	DataPoint dPt = new DataPoint();
    	List<Double> dPtDimensions = dPt.getDimensions();
    	
    	for(Double value : dataPoint.getDimensions()){
    		dPtDimensions.add(value);
    		dPtDimensions.add(1 - value);
    	}
    	
    	return dPt;
    }
    
    /**
     * Fuzzy AND uses min, scale by total 1's
     * 
     * @param dataPoint
     * @param b
     * @return double
     */
    private static double fuzzyAnd(DataPoint dataPoint, List<Double> b){
    	
    	double match = 0, total = 0;
    	for(int i = 0; i < dataPoint.getDimensions().size(); i++){
    		match += Math.min(dataPoint.getDimensions().get(i), b.get(i));
    		total += dataPoint.getDimensions().get(i);
    	}
    	
    	if(total == 0){
    		return 0;
    	}
    	
    	return match/total;
    }
    
    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return null;
    }
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[FuzzyARTClassifier:");
	    sb.append(" state = ").append(getState());
	    sb.append(", value = ").append(getCurrentData());
	    sb.append(", # features = ").append(numFeatures);
	    sb.append(", vigilance = ").append(vigilance);
	    sb.append(", bias = ").append(bias);
	    sb.append(", learning rate = ").append(learningRate);
	    sb.append(", complement = ").append(complement);
	    sb.append(", # categories = ").append(numCategories);
	    sb.append(", max # categories = ").append(maxNumCategories);
	    sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Return a printable (i.e. easily human readable) string representation of the ART network
	 * 
	 * @return String
	 */
	public String getModelVectorsDisplay(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("Model Vectors:\n");
		for(int i = 0; i < weight.size(); i++){
		    sb.append(i).append("[");
			for(double value : weight.get(i)){
			    sb.append(" ").append(value).append(",");
			}
			sb.append("]\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * This class contains values for each dimension which represents a data point that can be categorized
	 * 
	 * @author mhoffman
	 *
	 */
	protected class DataPoint{
		
		private List<Double> dimensions;
		
		/**
		 * Class constructor - populate data point with values
		 * 
		 * @param dimensions - data points to categorize
		 */
		public DataPoint(List<Double> dimensions){
			this.dimensions = dimensions;
		}
		
		/**
		 * Default Constructor
		 */
		public DataPoint(){
			dimensions = new ArrayList<Double>();
		}
		
		/**
		 * Return the values for this data point
		 * 
		 * @return List<Double>
		 */
		public List<Double> getDimensions(){
			return dimensions;
		}
		
		@Override
		public String toString(){
			
		    StringBuffer sb = new StringBuffer();
		    sb.append("[DataPoint:");
		    sb.append(" dimensions = {");
			for(Double value : dimensions){
			    sb.append(value).append(", ");
			}
			
			sb.append("}");
			sb.append("]");
			
			return sb.toString();
		}
	}
	
	/**
	 * This class contains the activation and category values for an activation category
	 * 
	 * @author mhoffman
	 *
	 */
	protected class ActivationCategory implements Comparable<ActivationCategory>{
		
		private double activation;
		private int category;
		
		/**
		 * Class constructor - populate attributes
		 * 
		 * @param currentActivation the activation value  
		 * @param currentCategory the index of the category
		 */
		public ActivationCategory(double currentActivation, int currentCategory){
			this.activation = currentActivation;
			this.category = currentCategory;
		}
		
		public double getActivation(){
			return activation;
		}
		
		public int getCategory(){
			return category;
		}
		
		@Override
		public int compareTo(ActivationCategory thatCategory){
			return (int)(this.getActivation() - thatCategory.getActivation());
		}
		
		@Override
		public String toString(){
			
		    StringBuffer sb = new StringBuffer();
		    sb.append("[FuzzyARTClassifier");
		    sb.append(" activiation = ").append(activation);
		    sb.append(", category = ").append(category);
		    sb.append("]");
			
			return sb.toString();
		}
	}
}
