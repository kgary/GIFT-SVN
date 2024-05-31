/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

/**
 * This is the base score class for a survey element that has or contains, at some level, a correct answer.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractAnswerScore implements ScoreInterface {
    
    /** highest number of possible points */
    private double highestPossiblePoints;
    
    /** total points from all question scores */
    private double totalEarnedPoints;
    
    /**
     * Class constructor - empty
     */
    public AbstractAnswerScore(){
        
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param totalEarnedPoints total points from all question scores
     * @param highestPossiblePoints highest number of possible points
     */
    public AbstractAnswerScore(double totalEarnedPoints, double highestPossiblePoints){
        setTotalEarnedPoints(totalEarnedPoints);
        setHighestPossiblePoints(highestPossiblePoints);
    }
    
    /**
     * Set the total amount of earned points at the implemented survey item score class level
     * 
     * @param totalEarnedPoints total points from all scored questions
     */
    protected void setTotalEarnedPoints(double totalEarnedPoints){
        this.totalEarnedPoints = totalEarnedPoints;
    }
    
    /**
     * Return the total amount of earned points at the implemented survey item score class level
     * 
     * @return total points from all scored questions
     */
    public double getTotalEarnedPoints(){
        return totalEarnedPoints;
    }
    
    /**
     * Set the highest number of possible points at the implemented survey item score class level
     * 
     * @param highestPossiblePoints highest possible points that could be earned
     */
    protected void setHighestPossiblePoints(double highestPossiblePoints){
        
        if(highestPossiblePoints <= 0){
            throw new IllegalArgumentException("The highest possible points value must be greater than zero, value provided is "+highestPossiblePoints);
        }
        
        this.highestPossiblePoints = highestPossiblePoints;
    }
    
    /**
     * Return the highest number of possible points at the implemented survey item score class level
     * 
     * @return highest possible points that could be earned
     */
    public double getHighestPossiblePoints(){
        return highestPossiblePoints;
    }
    
    /**
     * Used to allow the score classes to summarize their score attributes.  An example could
     * be to calculate the total earned points and the highest possible points at various levels in the score hierarchy.
     */
    public abstract void collate();
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("totalEarnedPoints = ").append(getTotalEarnedPoints());
        sb.append(", highestPossiblePoints = ").append(getHighestPossiblePoints());
        
        return sb.toString();
    }
}
