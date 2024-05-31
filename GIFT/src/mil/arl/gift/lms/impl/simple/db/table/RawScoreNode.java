/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple.db.table;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@Entity
@Table(name="rawscorenode")
public class RawScoreNode extends AbstractScoreNode {

	private String assessment;
	private String value;
	private String units;
	
	/**
	 * A comma separated list of usernames of the users who received this score
	 * Can be null or empty.
	 */
	private String usernames;
	
	/**
	 * Default Constructor
	 */
	public RawScoreNode(){
	    
	}
	
	/**
	 * Class constructor - set attributes
	 * 
	 * @param name the name of this node 
	 * @param value the value for the raw score
	 * @param units the units of measurement for the value
	 * @param assessment the assessment for this score node
	 * @param parent the parent of this node.  Can't be null.
	 * @param usernames A comma separated list of usernames of the users who received this score
     * Can be null or empty.
	 */
	public RawScoreNode(String name, String value, String units, String assessment, GradedScoreNode parent, String usernames) {
	    super(name, parent);
		this.value = value;
		this.units = units;
		this.assessment = assessment;
		this.setUsernames(usernames);
	}

    //can't be null
    @Column(nullable=false)
    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    //can't be null
    @Column(nullable=false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Sets the units label. Null values are illegal.
     * 
     * @param units the units of measurement for the value
     */
    public void setUnits(String units) {        
        this.units = units;
    }

    //can't be null
    @Column(nullable=false)
    public String getUnits() {        
        return units;
    }

	/**
	 * Return a comma separated list of usernames of the users who received this score
     * 
	 * @return Can be null or empty.
	 */
    public String getUsernames() {
        return usernames;
    }

    /**
     * Set the comma separated list of usernames of the users who received this score
     * @param usernames Can be null or empty.
     */
    public void setUsernames(String usernames) {
        this.usernames = usernames;
    }

    @Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[RawScoreNode:");
	    sb.append(super.toString());
	    sb.append(", value = ").append(getValue());
        sb.append(", units = ").append(getUnits());
	    sb.append(", assessment = ").append(getAssessment());
	    sb.append(", usernames = ").append(getUsernames());
	    sb.append("]");
		
		return sb.toString();
	}
}
