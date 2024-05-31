/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer.data;

/**
 * This class contains range values for an attribute.
 * 
 * @author mhoffman
 *
 */
public class AttributeRange {

	private double min;
	private double max;
	
	/**
	 * Class constructor - set attributes.
	 * 
	 * @param min - the minimum value for this range
	 * @param max - the maximum value for this range
	 */
	public AttributeRange(double min, double max){
	    
	    if(min >= max){
	        throw new IllegalArgumentException("("+min+" >= "+max+" == TRUE).  The min value must be less than the max value");
	    }
	    
		this.min = min;
		this.max = max;
	}
	
	public double getMin(){
		return min;
	}
	
	public double getMax(){
		return max;
	}
	
	/**
	 * Return the difference between the min and max value, i.e. the range between the two values.
	 * 
	 * @return double
	 */
	public double getRange(){
		return max - min;
	}
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[AttributeRange:");
	    sb.append(" min = ").append(getMin());
	    sb.append(", max = ").append(getMax());
	    sb.append(", range = ").append(getRange());
	    sb.append("]");
		
		return sb.toString();
	}
}
