/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.learner.clusterer.FuzzyARTClassifier;

/**
 * This class tests the Fuzzy ART classifier
 * 
 * @author mhoffman
 *
 */
public class FuzzyARTClassifierTest extends FuzzyARTClassifier {

	public FuzzyARTClassifierTest(int numFeatures){
		this.numFeatures = numFeatures;
	}
	
	public DataPoint buildDataPoint(List<Double> valueList){
		return new DataPoint(valueList);
	}
	
	public static void main(String args[]){
		
		System.out.println("Starting test");
		
		//create parameters
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(FuzzyARTClassifier.VIGILANCE, Double.valueOf(0.5).toString());
		properties.put(FuzzyARTClassifier.COMPLEMENT_CODE, Boolean.TRUE.toString());
//		properties.put(FuzzyARTClassifier.BIAS, new Double(0.000001).toString());
//		properties.put(FuzzyARTClassifier.LEARNING_RATE, new Double(1.0).toString());
					
		FuzzyARTClassifierTest fart = new FuzzyARTClassifierTest(2);
		try{
    		fart.configureByProperties(properties);
    		double[][] input = {{.1,.1},{.2,.2},{.3,.3},{.4,.4},{.5,.5},{.6,.6},{.7,.7},{.8,.8},{.9,.9},{1,1}};
    		runTest(fart, input);
    
    		System.out.println("Finished test");
		}catch(Throwable t){
		    t.printStackTrace();
		}
	}
	
	/**
	 * Run a test on the classifier with the given input
	 * 
	 * @param fart - the fuzzy ART classifier instances to use
	 * @param input - the collection of data pairs to test with as input to the classifier
	 */
	private static void runTest(FuzzyARTClassifierTest fart, double[][] input){
		
		List<DataPoint> inputBuild = new ArrayList<DataPoint>();

		for(double[] values : input){
			List<Double> valueList = new ArrayList<Double>();
			for(double value : values){
				valueList.add(value);
			}
			inputBuild.add(fart.buildDataPoint(valueList));
			
			List<Integer> patternPres = new ArrayList<Integer>();
			List<Integer> patternFin = new ArrayList<Integer>();
			
			for(DataPoint pattern : inputBuild){
				//present one pattern
				patternPres.add(fart.step(pattern));
			}
			
			for(DataPoint pattern : inputBuild){
				patternFin.add(fart.categorize(pattern));
			}
			
			//
			//print arrays
			//
			System.out.print("Presented:");
			for(Integer value : patternPres){
				System.out.print(" " + value + ",");
			}
			
			System.out.print("\nFind:");
			for(Integer value : patternFin){
				System.out.print(" " + value + ",");
			}
			
			//Categorize test
			List<Double> pair = new ArrayList<Double>();
			pair.add(0.2);
			pair.add(0.4);
			System.out.println("\nnet.categorize => "+fart.categorize(fart.buildDataPoint(pair)));
			System.out.println(fart.getModelVectorsDisplay());
		}
	}
}
