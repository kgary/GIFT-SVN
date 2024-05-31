/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * This class contains the state of the a power point application being used for a lesson.
 * 
 * @author mhoffman
 *
 */
public class PowerPointState implements TrainingAppState {
    
    public static final int UNKNOWN_SLIDE_INDEX = -1;
    public static final int UNKNOWN_SLIDE_CNT = -1;

    /** current slide show slide index (first slide is index = 1)*/
    private int slideIndex = UNKNOWN_SLIDE_INDEX;
    
    /** total number of slides */
    private int slideCount = UNKNOWN_SLIDE_CNT;
    
    /** an error message describing an error with the current power point state */
    private String errorMsg = null;
    
    /**
     * Create a PowerPoint state that describes an error.
     * 
     * @param errorMsg information about the error with PowerPoint
     */
    public PowerPointState(String errorMsg){
        setErrorMessage(errorMsg);
    }
    
    /**
     * Class constructor
     * 
     * @param currentSlideIndex - the current slide index in the active PowerPoint show
     * @param slideCount total number of slides in the current presentation
     */
    public PowerPointState(int currentSlideIndex, int slideCount){
        setSlideIndex(currentSlideIndex);
        setSlideCount(slideCount);
    }
    
    private void setErrorMessage(String errorMsg){
        
        if(errorMsg == null || errorMsg.isEmpty()){
            throw new IllegalArgumentException("The error message can't be null or empty.");
        }
        
        this.errorMsg = errorMsg;
    }
    
    private void setSlideIndex(int slideIndex){
        
        if(slideIndex <= 0){
            throw new IllegalArgumentException("The slide index must be greater than zero");
        }
        
        this.slideIndex = slideIndex;
    }
    
    private void setSlideCount(int slideCount){
        
        if(slideCount <= 0){
            throw new IllegalArgumentException("The slide count must be greater than zero");
        }
        
        this.slideCount = slideCount;
    }
    
    /**
     * Return the error message associated with this state.
     * 
     * @return String information about an error.  The value will be null if there is
     * no error.
     */
    public String getErrorMessage(){
        return errorMsg;
    }
    
    /**
     * Return the current slide index
     * 
     * @return int the value will be $UNKNOWN_SLIDE_INDEX if this state describes an error
     */
    public int getSlideIndex(){
        return slideIndex;
    }
    
    /**
     * Return the total number of slides
     * 
     * @return int the value will be $UNKNOWN_SLIDE_CNT if this state describes an error
     */
    public int getSlideCount(){
        return slideCount;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PowerPointState: ");
        sb.append("slideIndex = ").append(getSlideIndex());
        sb.append(", slideCount = ").append(getSlideCount());
        sb.append(", errorMsg = ").append(getErrorMessage());
        sb.append("]");
        
        return sb.toString();
    }
}
