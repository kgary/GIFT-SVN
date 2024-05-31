/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.paradata;

/**
 * The data model for a line of a GIFT paradata file.
 * 
 * @author mhoffman
 *
 */
public class ParadataBean {
    
    /** id generated used in the default no arg constructor */
    private static int nextId = 1;
    
    /** unique id of this paradata row in a paradata file */
    private int id;

    /** epoch time at which the paradata entry was captured */
    private long date;
    
    /** adaptive courseflow phase when the content was delivered */
    private PhaseEnum phase;
    
    /** enumerated types of adaptive courseflow phases where content is delivered */
    public enum PhaseEnum{
        K,  // knowledge deliver - Rules/Example phases
        KR, // knowledge remediation - after a failed recall
        P,  // Practice
        PR  // Practice remediation - after a failed practice
    }
    
    /** amount of time in seconds the content was shown to the learner */
    private int duration;
    
    /** enumeration for whether the assessment was failed or passed for this content */
    private PassedEnum passed;
    
    /** enumerations for pass or fail */
    public enum PassedEnum{
        y,
        n
    }
    
    /** learner state information at the time the content was selected for delivery */
    private String state;
    
    /**
     * Default constructor - used by SuperCSV parser
     */
    public ParadataBean(){
        id = ++nextId;
    }

    /**
     * Set attributes 
     * @param id unique id of this paradata row in a paradata file
     * @param date epoch time at which the paradata entry was captured 
     * @param phase adaptive courseflow phase when the content was delivered 
     * @param duration amount of time in seconds the content was shown to the learner
     * @param passed enumeration for whether the assessment was failed or passed for this content
     * @param state learner state information at the time the content was selected for delivery
     */
    public ParadataBean(int id, long date, PhaseEnum phase, int duration, PassedEnum passed, String state) {
        super();
        setId(id);
        setDate(date);
        setPhase(phase);
        setDuration(duration);
        setPassed(passed);
        setState(state);
    }    
    
    /**
     * Return the unique id of this paradata row in a paradata file
     * @return unique id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the unique id of this paradata row in a paradata file
     * @param id unique id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Return the epoch time at which the paradata entry was captured 
     * @return epoch time
     */
    public long getDate() {
        return date;
    }

    /**
     * Set the epoch time at which the paradata entry was captured 
     * @param date milliseconds
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * Return the adaptive courseflow phase when the content was delivered 
     * @return enumerated phase
     */
    public PhaseEnum getPhase() {
        return phase;
    }

    /**
     * Set the adaptive courseflow phase when the content was delivered 
     * @param phase enumerated phase
     */
    public void setPhase(PhaseEnum phase) {
        this.phase = phase;
    }

    /**
     * Return the amount of time in seconds the content was shown to the learner 
     * @return time in seconds
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Set the amount of time in seconds the content was shown to the learner 
     * @param duration time in seconds
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Return the enumeration for whether the assessment was failed or passed for this content
     * @return passed enumeration
     */
    public PassedEnum getPassed() {
        return passed;
    }

    /**
     * Set the enumeration for whether the assessment was failed or passed for this content
     * @param passed passed enumeration
     */
    public void setPassed(PassedEnum passed) {
        this.passed = passed;
    }

    /**
     * Return the learner state information at the time the content was selected for delivery
     * @return learner state information
     */
    public String getState() {
        return state;
    }

    /**
     * Set the learner state information at the time the content was selected for delivery
     * @param state learner state information
     */
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + id >>> 32;

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ParadataBean)) {
            return false;
        }
        ParadataBean other = (ParadataBean) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ParadataBean: ");
        builder.append("id = ");
        builder.append(id);
        builder.append(", date = ");
        builder.append(date);
        builder.append(", phase = ");
        builder.append(phase);
        builder.append(", duration = ");
        builder.append(duration);
        builder.append(", passed = ");
        builder.append(passed);
        builder.append(", state = ");
        builder.append(state);
        builder.append("]");
        return builder.toString();
    }

}
