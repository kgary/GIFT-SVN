/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.List;

/**
 * The scoring weights for a free response question
 *
 * @author sharrison
 */
public class FreeResponseReplyWeights implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * the weights for the matrix of choices replies The first list are rows.
     **/
    private List<List<List<Double>>> replyWeights;

    /**
     * Default Constructor
     *
     * Required to exist for GWT compatibility
     */
    @SuppressWarnings("unused")
    private FreeResponseReplyWeights() {
    }

    /**
     * Constructor
     *
     * @param replyWeights The weights of the free response conditions. There are a list of response
     *            fields; each response field has a list of conditions; each condition contains
     *            scored point value, (optional) min range, (optional) max range, respectively. Can
     *            be empty. Cannot be null.
     */
    public FreeResponseReplyWeights(List<List<List<Double>>> replyWeights) {

        if (replyWeights == null) {
            throw new IllegalArgumentException("reply weights cannot be null for FreeResponseReplyWeights");
        }

        this.replyWeights = replyWeights;
    }

    /**
     * Gets the reply weights for the free response question
     *
     * @return The weights of the free response conditions. There are a list of response fields;
     *         each response field has a list of conditions; each condition contains scored point
     *         value, (optional) min range, (optional) max range, respectively. Can be empty.
     */
    public List<List<List<Double>>> getReplyWeights() {
        return replyWeights;
    }

    /**
     * Sets the scoring weights at the given index
     * 
     * @param row the row index to set. This matches the order that the response field is at (0 base
     *            index). Must be >= 0.
     * @param values the list of conditions and their scores and ranges. Each condition contains
     *            scored point value, (optional) min range, (optional) max range, respectively. Can
     *            be empty. Cannot be null.
     */
    public void setWeightValue(int row, List<List<Double>> values) {
        
        if (row < 0) {
            throw new IllegalArgumentException("response field row index must be 0 or a positive integer.");
        } else if (values == null) {
            throw new IllegalArgumentException("can only set a non-null value for reply weights.");
        }
        
        replyWeights.set(row, values);
    }

    /**
     * Adds a new entry in the reply weights list.
     * 
     * @param values the list of conditions and their scores and ranges. Each condition contains
     *            scored point value, (optional) min range, (optional) max range, respectively. Can
     *            be empty.
     */
    public void addNew(List<List<Double>> values) {
        replyWeights.add(values);
    }

    /**
     * Return the maximum points that can be earned for a response field in a free response
     * question.
     *
     * @param responseFieldIndex a value between 0 (inclusive) and the number of replies (exclusive)
     * @return the maximum weight of a free response field. Will be zero if the index provided is
     *         out of bounds.
     */
    public double getMaxPointsForResponseField(int responseFieldIndex) {

        Double maxWeight = null;

        if (responseFieldIndex >= 0 && responseFieldIndex < replyWeights.size()) {

            // each condition option
            for (List<Double> row : replyWeights.get(responseFieldIndex)) {
                if (row != null && !row.isEmpty()) {
                    // if the condition point value is greater than the max, then we have a new max
                    if (maxWeight == null || row.get(0) > maxWeight) {
                        maxWeight = row.get(0);
                    }
                }
            }
        }

        return maxWeight == null ? 0 : maxWeight;
    }

    /**
     * Return the minimum points that can be earned for a response field in a free response
     * question.
     *
     * @param responseFieldIndex a value between 0 (inclusive) and the number of replies (exclusive)
     * @return the minimum weight of a free response field. Will be zero if the index provided is
     *         out of bounds.
     */
    public double getMinPointsForResponseField(int responseFieldIndex) {

        Double minWeight = null;

        if (responseFieldIndex >= 0 && responseFieldIndex < replyWeights.size()) {

            // each condition option
            for (List<Double> row : replyWeights.get(responseFieldIndex)) {
                if (row != null && !row.isEmpty()) {
                    // if the condition point value is less than the min, then we have a new min
                    if (minWeight == null || row.get(0) < minWeight) {
                        minWeight = row.get(0);
                    }
                }
            }
        }

        return minWeight == null ? 0 : minWeight;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[FreeResponseReplyWeights: ");
        sb.append("reply weights = ").append(getReplyWeights());
        sb.append("]");

        return sb.toString();
    }
}
