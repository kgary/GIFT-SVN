/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.survey.SliderRange;


/**
 * The SurveyScorerUtil class allows for a central class that conrols how the scoring of various survey items
 * are scored.  This currently can be accessed by the runtime (domain session) logic as well as the authoring tools
 * such as the GAT.
 *
 * @author nblomberg
 *
 */
public class SurveyScorerUtil {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SurveyScorerUtil.class.getName());

    /**
     * Constructor default
     */
    SurveyScorerUtil() {

    }


    /**
     * Gets the highest score possible for a multiple choice question.
     *
     * @param weights The weights of the multiple choice possible answers.  If null or empty 0 will be returned.
     * @param minSelectionsAllowed the minimum number of selections allowed.  Must be greater than zero.  Can be equal to max selections allowed.
     * @param maxSelectionsAllowed The maximum number of selections allowed.  Must be greater than zero.  Can be equal but less than max selections allowed.  A value
     * greater than the size of weights provided will instead use the size value.
     * @return Highest score possible adhering to the min and max selections allowed.
     */
    public static double getHighestScoreMultipleChoice(List<Double> weights, int minSelectionsAllowed, int maxSelectionsAllowed) {

        if (minSelectionsAllowed > maxSelectionsAllowed) {
            throw new IllegalArgumentException("The min selections allowed value of "+minSelectionsAllowed+" can't be greater than the max selection allowed value of "+maxSelectionsAllowed);
        } else if (minSelectionsAllowed < 0) {
            throw new IllegalArgumentException("The min selections allowed value of " + minSelectionsAllowed + " must be greater than zero.");
        } else if (maxSelectionsAllowed < 0) {
            throw new IllegalArgumentException("The max selections allowed value of " + maxSelectionsAllowed + " must be greater than zero.");
        } else if (weights == null || weights.isEmpty()) {
            logger.info("There are no weights, returning 0.0");
            return 0.0;
        }

        // deep copy values for manipulation in the collection
        List<Double> sortedWeights = new ArrayList<Double>(weights);

        double highestPossiblePoints = 0;

        if (maxSelectionsAllowed > 0) {

            if (maxSelectionsAllowed > sortedWeights.size()) {
                maxSelectionsAllowed = sortedWeights.size();
            }

            // sort weights with higher values first (hence the reverse)
            Collections.sort(sortedWeights, Collections.reverseOrder());

            // grab weights from highest value first, until min selection is satisfied,
            // than capture additional positive weights until max selections is reached
            for (int index = 0; index < maxSelectionsAllowed; index++) {

                Double weight = sortedWeights.get(index);
                if (minSelectionsAllowed > index) {
                    // still have to satisfy the min selections allowed
                    highestPossiblePoints += weight;

                } else if (weight > 0) {
                    // found positive weight but have already passed the min selections allowed
                    // adding this weight as working toward the max selections allowed
                    highestPossiblePoints += weight;

                } else {
                    // found non-positive weight and have already passed the min selections allowed
                    //therefore there are no more weights to add to improve the highest possible score
                    break;
                }

            }
        }

        return highestPossiblePoints;
    }

    /**
     * Gets the highest score possible for a rating scale question.
     *
     * @param weights The weights of the rating scale choices.
     * @return Highest score possible. If weights is an empty list, 0.0
     * will be returned.
     */
    public static double getHighestScoreRatingScale(List<Double> weights) {

        // If an empty list is supplied return 0
        if (weights.isEmpty()) {
            return 0.0;
        }

        List<Double> sortedWeights = new ArrayList<Double>(weights);
        int maxSelections = 1;
        double highestPossiblePoints = 0;


        if (maxSelections > 0) {

            if (maxSelections > sortedWeights.size()) {
                throw new IllegalArgumentException("The number of maximum selections (" + maxSelections + ") can't be greater than the number of points available (" + sortedWeights.size() + ")");
            }

            Collections.sort(sortedWeights);
            for (int index = sortedWeights.size() - 1; index >= (sortedWeights.size() - maxSelections); index--) {

                Double weight = sortedWeights.get(index);
                if (weight > 0) {
                    highestPossiblePoints += sortedWeights.get(index);
                }
            }
        }

        return highestPossiblePoints;
    }

    /**
     * Gets the highest score possible for a matrix of choice question.
     *
     * @param replyWeights The weights of the matrix of choice possible answers.
     * @return Highest score possible.
     */
    public static double getHighestScoreMatrixOfChoice(List<List<Double>> replyWeights) {
        return getHighestScoreMatrixOfChoice(replyWeights, replyWeights.size(), Integer.MAX_VALUE);
    }

    /**
     * Gets the highest score possible for a matrix of choice question.
     *
     * @param replyWeights The weights of the matrix of choice possible answers.
     * @param rows The number of answer rows visible
     * @param columns The number of answer columns visible
     * @return Highest score possible.
     */
    public static double getHighestScoreMatrixOfChoice(List<List<Double>> replyWeights, int rows, int columns) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getHighestScoreMatrixOfChoice(" + replyWeights + ", " + rows + ", " + columns + ")");
        }

        double highestPossiblePoints = 0;

        for (int i = 0; i<replyWeights.size() && i<rows; i++) {
            List<Double> weights = replyWeights.get(i).subList(0, Math.min(columns, replyWeights.get(i).size()));

            List<Double> sortedWeights = new ArrayList<Double>(weights);

            // Only one option can be selected at a time
            int maxSelections = 1;

            if (maxSelections > 0) {

                if (maxSelections > sortedWeights.size()) {
                    throw new IllegalArgumentException("The number of maximum selections (" + maxSelections + ") can't be greater than the number of points available (" + sortedWeights.size() + ")");
                }

                Collections.sort(sortedWeights);
                for (int index = sortedWeights.size() - 1; index >= (sortedWeights.size() - maxSelections); index--) {

                    Double weight = sortedWeights.get(index);
                    if (weight > 0) {
                        highestPossiblePoints += sortedWeights.get(index);
                    }
                }
            }
        }

        return highestPossiblePoints;
    }

    /**
     * Gets the highest score possible for a free response question.
     *
     * @param replyWeights The weights of the free response possible answers.
     * @return Highest score possible.
     */
    public static double getHighestScoreFreeResponse(List<List<List<Double>>> replyWeights) {

        double highestPossiblePoints = 0;

        if (replyWeights != null) {
            for (List<List<Double>> responseFields : replyWeights) {
                if (responseFields == null || responseFields.isEmpty()) {
                    continue;
                }

                double maxWeight = 0;
                for (List<Double> weights : responseFields) {
                    if (weights == null || weights.isEmpty()) {
                        continue;
                    } else if (weights.get(0) > maxWeight) {
                        maxWeight = weights.get(0);
                    }
                }

                highestPossiblePoints += maxWeight;
            }
        }

        return highestPossiblePoints;
    }

    /**
     * Gets the highest score possible for a slider question.
     *
     * @param range The range of the slider (min and max).
     * @return Highest score possible.
     */
    public static double getHighestScoreSlider(SliderRange range) {
        if (range != null) {

            return range.getMaxValue();

        } else {

            return 100;
        }
    }


}
