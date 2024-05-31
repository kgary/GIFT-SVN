/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * The weights for a matrix of choices question
 *
 * @author jleonard
 */
public class MatrixOfChoicesReplyWeights implements Serializable {
	
	@SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(MatrixOfChoicesReplyWeights.class.getName());

    private static final long serialVersionUID = 1L;

    /** 
     * the weights for the matrix of choices replies 
     * The first list are rows.
     **/
    private List<List<Double>> replyWeights;

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public MatrixOfChoicesReplyWeights() {
    }

    /**
     * Constructor
     *
     * @param replyWeights The weights of the matrix of choices question
     */
    public MatrixOfChoicesReplyWeights(List<List<Double>> replyWeights) {

        this.replyWeights = replyWeights;
    }

    /**
     * Gets the reply weights for the matrix of choices question
     *
     * @return the list of the weights for the matrix of choices replies 
     */
    public List<List<Double>> getReplyWeights() {

        return replyWeights;
    }

    /**
     * Gets the weight of the reply for an option
     *
     * @param row The index of the option's row
     * @param col The index of the option's column
     * @return double The weight of the reply for an option
     */
    public double getReplyWeight(int row, int col) {

        return replyWeights.get(row).get(col);
    }
    
    /**
     * Adds a null cell at the end of each list,
     * effectively adding a blank column to the 2D array
     */
    public void addColumn(){
    	for(List<Double> list : replyWeights){
    		list.add(0.0);
    	}
    }
    
    /**
     * Adds a new row to the reply weights the the desired
     * amount of elements, all defaulted to 0.0
     * 
     * @param size the number of columns in the row
     */
    public void addRow(int size) {
		List<Double> newRow = new ArrayList<Double>();
		for(int i = 0; i < size; i++){
			newRow.add(0.0);
		}
		replyWeights.add(newRow);
	}
    
    /**
     * Sets the weight at the given index
     *  
     * @param row the row
     * @param col the column
     * @param value the new value
     */
    public void setWeightValue(int row, int col, double value){
    	replyWeights.get(row).set(col, value);
    }

    /**
     * Removes a column from the 2D list
     * 
     * @param col the column to remove
     */
	public void removeColumn(int col){
	    int weightSize = replyWeights.size();
    	if(weightSize > 0 && replyWeights.get(0).size() > col){
//    		logger.info("Removing answer weights from column " + col);
	    	for(List<Double> list : replyWeights){
	    		list.remove(col);
	    	}
    	} else{
//    		logger.info("There is no weight at index " + col);
    	}
    }
	
	/**
	 * Removes a Row from the 2D list
	 * 
	 * @param row the row to be removed
	 */
	public void removeRow(int row){
		if(replyWeights.size() > row){
//			logger.info("Removing answer weights for row " + row);
			replyWeights.remove(row);
		} else {
//			logger.info("There is no answer weight at index " + row);
		}
	}
	
	/**
	 * Swaps 2 rows and all their weights
	 * 
	 * @param firstRow the first row to be swapped
	 * @param secondRow row to swap the first row with
	 */
	public void swapRows(int firstRow, int secondRow){
		if(replyWeights.size() > Math.max(firstRow, secondRow)){
//			logger.info("Swapping answer weights for rows " + firstRow + " and " + secondRow);
			Collections.swap(replyWeights, firstRow, secondRow);
		} else { 
//			logger.info("There are not weights for both rows " + firstRow + " and " + secondRow);
		}
	}
	
	/**
	 * Swaps 2 columns and all their weights
	 * 
	 * @param firstCol the first column to be swapped
	 * @param secondCol column to swap the first column with
	 */
	public void swapColumns(int firstCol, int secondCol){
		if(replyWeights.size() > Math.max(firstCol, secondCol)){
//			logger.info("Swapping answer weights for columns " + firstCol + " and " + secondCol);
			for(List<Double> list : replyWeights){
				Collections.swap(list, firstCol, secondCol);
			} 
		} else {
//			logger.info("There are not weights for both columns " + firstCol + " and " + secondCol);
		}
	}
	
	/**
	 * Return the maximum points that can be earned for a row in a matrix of choices question.
	 * Currently only a single choice can be made per row this so is the max weight in that row.
	 * 
	 * @param rowIndex a value between 0 (inclusive) and the number of replies (exclusive)
	 * @return the max weight of a choice in the row.  Will be zero if the max weight is negative
	 * or the row index provides is out of bounds.
	 */
	public double getMaxPointsForRow(int rowIndex){
	    
	    if(rowIndex >= 0 && rowIndex < replyWeights.size()){
	        List<Double> rowWeights = replyWeights.get(rowIndex);
	        double maxWeight = 0;
	        for(Double weight : rowWeights){
	            
	            if(weight != null && weight > maxWeight){
	                maxWeight = weight;
	            }
	        }
	        
	        return maxWeight;
	    }
	    
	    return 0;
	}
	
	/**
     * Return the minimum points that can be earned for a row in a matrix of choices question.
     * Currently only a single choice can be made per row this so is the min weight in that row.
     * 
     * @param rowIndex a value between 0 (inclusive) and the number of replies (exclusive)
     * @return the min weight of a choice in the row.  Will be zero if the row index provides is out of bounds.
     * Will be zero if there are no weights for the row.
     */
    public double getMinPointsForRow(int rowIndex){
        
        if(rowIndex >= 0 && rowIndex < replyWeights.size()){
            List<Double> rowWeights = replyWeights.get(rowIndex);
            
            if(!rowWeights.isEmpty()){
                double minWeight = Double.MAX_VALUE;
                for(Double weight : rowWeights){
                    
                    if(weight != null && weight < minWeight){
                        minWeight = weight;
                    }
                }
                
                //if still the max-value than return 0 as the javadoc for this method mentions
                return minWeight == Double.MAX_VALUE ? 0 : minWeight;
            }
        }
        
        return 0;
    }
}
