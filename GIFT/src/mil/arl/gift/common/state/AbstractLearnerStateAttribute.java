/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This class is the base class for enumerated learner state attributes (e.g. Arousal attribute with values of S.T="low", L.T.="low", Predicted="medium")
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractLearnerStateAttribute implements TemporalStateAttribute  {

    /** the enumerated name of the learner state attribute */
    private LearnerStateAttributeNameEnum name;
    
    /** 
     * the enumerated value of the learner state attribute over various temporal windows 
     * and the time at which that value was set.
     */
    private AbstractEnum shortTerm;
    private long shortTermTimestamp;
    private AbstractEnum longTerm;
    private long longTermTimestamp;
    private AbstractEnum predicted;
    private long predictedTimestamp;
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    protected AbstractLearnerStateAttribute() {}
    
    /**
     * Class constructor - set attributes
     * 
     * @param name the enumerated name of the learner state attribute. Can't be null.
     * @param shortTerm the enumerated value of the learner state attribute for short term. Can't be null.
     * @param shortTermTimestamp the time in milliseconds at which the short term state value was set. Must be greater than 0.
     * @param longTerm the enumerated value of the learner state attribute for long term. Can't be null.
     * @param longTermTimestamp the time in milliseconds at which the long term state value was set. Must be greater than 0.
     * @param predicted the enumerated value of the learner state attribute for predicted. Can't be null.
     * @param predictedTimestamp the time in milliseconds at which the predicted state value was set. Must be greater than 0.
     */
    public AbstractLearnerStateAttribute(LearnerStateAttributeNameEnum name, 
            AbstractEnum shortTerm, long shortTermTimestamp,
            AbstractEnum longTerm, long longTermTimestamp,
            AbstractEnum predicted, long predictedTimestamp){
        
        this();
        
        if(name == null){
            throw new IllegalArgumentException("The name can't be null.");
        }
        
        if(shortTerm == null){
            throw new IllegalArgumentException("The short term assessment can't be null.");
        }
        
        if(longTerm == null){
            throw new IllegalArgumentException("The long term assessment can't be null.");
        }
        
        if(predicted == null){
            throw new IllegalArgumentException("The predicted assessment can't be null.");
        }
        
        if(shortTermTimestamp < 1){
            throw new IllegalArgumentException("The short term timestamp value of "+shortTermTimestamp+" must be a positive value.");
        }
        
        if(longTermTimestamp < 1){
            throw new IllegalArgumentException("The long term timestamp value of "+longTermTimestamp+" must be a positive value.");
        }
        
        if(predictedTimestamp < 1){
            throw new IllegalArgumentException("The predicted timestamp value of "+predictedTimestamp+" must be a positive value.");
        }
        
        this.name = name;
        this.shortTerm = shortTerm;
        this.longTerm = longTerm;
        this.predicted = predicted;
        this.shortTermTimestamp = shortTermTimestamp;
        this.longTermTimestamp = longTermTimestamp;
        this.predictedTimestamp = predictedTimestamp;
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param name the enumerated name of the learner state attribute. Can't be null.
     * @param shortTerm the enumerated value of the learner state attribute for short term. Can't be null.
     * @param longTerm the enumerated value of the learner state attribute for long term. Can't be null.
     * @param predicted the enumerated value of the learner state attribute for predicted. Can't be null.
     */
    public AbstractLearnerStateAttribute(LearnerStateAttributeNameEnum name, AbstractEnum shortTerm, AbstractEnum longTerm, AbstractEnum predicted){
        this(name, shortTerm, System.currentTimeMillis(), longTerm, System.currentTimeMillis(), predicted, System.currentTimeMillis());

    }

    /**
     * Return the enumerated name of the learner state attribute
     * @return won't be null.
     */
    public LearnerStateAttributeNameEnum getName() {
        return name;
    }

    @Override
    public AbstractEnum getShortTerm() {
        return shortTerm;
    }    
    
    @Override
    public long getShortTermTimestamp(){
        return shortTermTimestamp;
    }

    @Override
    public AbstractEnum getLongTerm() {
        return longTerm;
    }
    
    @Override
    public long getLongTermTimestamp(){
        return longTermTimestamp;
    }

    @Override
    public AbstractEnum getPredicted() {
        return predicted;
    }
    
    @Override
    public long getPredictedTimestamp(){
        return predictedTimestamp;
    }
    
    public void setShortTerm(AbstractEnum value){
        shortTerm = value;
        shortTermTimestamp = System.currentTimeMillis();
    }
    
    public void setLongTerm(AbstractEnum value){
        longTerm = value;
        longTermTimestamp = System.currentTimeMillis();
    }
    
    public void setPredicted(AbstractEnum value){
        predicted = value;
        predictedTimestamp = System.currentTimeMillis();
    }
    
    @Override
    public boolean equals(Object otherAbstractLearnerStateAttribute){
        
        if(otherAbstractLearnerStateAttribute instanceof AbstractLearnerStateAttribute){
            
            AbstractLearnerStateAttribute other = ((AbstractLearnerStateAttribute)otherAbstractLearnerStateAttribute);
            return this.getName() == other.getName() && this.getShortTerm() == other.getShortTerm() && this.getLongTerm() == other.getLongTerm() &&
                    this.getPredicted() == other.getPredicted();
        }
        
        return false;
    }
    
    @Override
    public int hashCode(){
    	
    	// Start with prime number
    	int hash = 13;
    	int mult = 167;
    	
    	// Take another prime as multiplier, add members used in equals
    	hash = mult * hash + this.getName().hashCode();
    	hash = mult * hash + this.getLongTerm().hashCode();
    	hash = mult * hash + this.getShortTerm().hashCode();
    	hash = mult * hash + this.getPredicted().hashCode();
    	
    	return hash;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("name = ").append(getName());
        sb.append(", short-term = ").append(getShortTerm()); 
        sb.append(", short-term-timestamp = ").append(getShortTermTimestamp());
        sb.append(", long-term = ").append(getLongTerm()); 
        sb.append(", long-term-timestamp = ").append(getLongTermTimestamp());
        sb.append(", predicted = ").append(getPredicted()); 
        sb.append(", predicted-timestamp = ").append(getPredictedTimestamp());
        
        return sb.toString();
    }
}
