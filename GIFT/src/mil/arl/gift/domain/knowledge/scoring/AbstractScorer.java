/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.scoring;

import generated.dkf.UnitsEnumType;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This class is the base class for scoring classes which maintain and assess scoring for
 * a performance node.<br/>
 * {@link IntegrationScorer}, {@link CountScorer}, {@link CompletionTimeScorer}
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractScorer {
    
    /** default raw score value - used if actual raw score can't be calculated */
    protected static final String DEFAULT_RAW_SCORE = "unknown";
    
    /** 
     * date formatter used for timestamps
     * NOTE: set the time zone to GMT since getTime uses that time zone
     */
    protected static FastDateFormat fdf = FastDateFormat.getInstance("HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.getDefault());
        
    /** timestamp units for time based scorers */
    protected static final String TIME_UNITS = "hh:mm:ss";

    /** the name of the scoring type */
    private final String name;
    
    /** the units of the raw score */
    private final UnitsEnumType units;
    
    /** whether the scorer is being used to track internal information and is not something the authored requested,
     * nor should it be reported in a long term learner record. */
    private boolean internalUseOnly = false;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the display name of this scorer
     * @param units - the units of measurement the score produces by this scorer
     */
    public AbstractScorer(String name, UnitsEnumType units){
       this.name = name; 
       this.units = units;
    }

    /**
     * Return the name of the scoring type
     * 
     * @return String
     */
    public String getName(){
        return name;
    }
    
    /**
     * Return whether the scorer is being used to track internal information and is not something the authored requested,
     * nor should it be reported in a long term learner record.
     * @return false is the default.
     */
    public boolean isInternalUseOnly() {
        return internalUseOnly;
    }

    /**
     * Set whether the scorer is being used to track internal information and is not something the authored requested,
     * nor should it be reported in a long term learner record.
     * @param internalUseOnly the value to use
     */
    public void setInternalUseOnly(boolean internalUseOnly) {
        this.internalUseOnly = internalUseOnly;
    }

    /**
     * Return the units of the raw score
     * 
     * @return UnitsEnumType
     */
    public UnitsEnumType getUnits(){
        return units;
    }
    
    /**
     * Return the scoring assessment
     * 
     * @return AssessmentLevelEnum
     */
    public abstract AssessmentLevelEnum getAssessment();
    
    /**
     * Return the raw score
     * 
     * @return String
     */
    public abstract String getRawScore();
    
    /**
     * Notification that the scorer can stop assessing and calculate it's score
     */
    public void cleanup(){
        //empty
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("name = ").append(getName());      
        sb.append(", units = ").append(getUnits());
        sb.append(", internalUseOnly = ").append(isInternalUseOnly());
        
        return sb.toString();
    }
}
