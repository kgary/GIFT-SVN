/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This class contains multiple course records for a single user and provides methods for filtering
 * or organizing the records.
 * 
 * @author mhoffman
 *
 */
public class LMSCourseRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    /** container of LMS course records */
	private List<LMSCourseRecord> records;
	
	/** dictionary of knowledge, skills, and abilities to assessment level */
	private Map<String, AssessmentLevelEnum> assessments;
	
	/** flag used to determine if the latest grades map should be rebuilt, usually because a new grade was added */
	private Boolean rebuildLatestGrades = true;
    
	/**
	 * contains the latest grades for each unique domain id
	 * key: unique domain id (e.g. mhoffman/Remediation Course/Remediation Course.course.xml)
	 * value: latest grade for that domain (by date)
	 */
    Map<String, Set<LMSCourseRecord>> recordsByDomain = new HashMap<>();
	
	/**
	 * Class constructor
	 */
	public LMSCourseRecords(){
		records = new ArrayList<LMSCourseRecord>();
	}
	
	/**
	 * Class constructor - populate with LMS records
	 * 
	 * @param records - LMS records
	 */
	public LMSCourseRecords(List<LMSCourseRecord> records){
	    
        if(records == null){
            throw new IllegalArgumentException("The records can't be null.");
        }
        
		this.records = records;
	}
	
	/**
	 * Return the list of LMS records
	 * 
	 * @return Won't be null.
	 */
	public List<LMSCourseRecord> getRecords(){
		return records;
	}
	
	/**
	 * This will sort the course records by ascending date 
	 * (i.e. early records are in the beginning of the collection)
	 */
	public void sort(){
	    Collections.sort(this.records);
	}

	/**
	 * Return the records for each unique domain id.  The map will be built
	 * if not already built or a record has been added.
     * 
     * 
	 * @return can be empty but not null
	 * key: unique domain id (e.g. mhoffman/Remediation Course/Remediation Course.course.xml)
     * value: latest grade for that domain (by date)
	 */
	public Map<String, Set<LMSCourseRecord>> getRecordsByDomain(){
	    
	    synchronized (rebuildLatestGrades) {
            
	        if(rebuildLatestGrades){
	            buildRecordMap();           
	        }
        }

        
        return recordsByDomain;
	}
	
	/**
	 * Search the records known to this class instance and build a map of the
	 * records for each domain.
	 */
	private void buildRecordMap(){
	    
        for(LMSCourseRecord record : getRecords()) {
            
            String key = record.getDomainName();
            // Get the latest known record for the domain
            Set<LMSCourseRecord> domainRecords = recordsByDomain.get(key);
            
            if(domainRecords == null){
                domainRecords = new HashSet<>();
                recordsByDomain.put(key, domainRecords);
            }
            
            domainRecords.add(record);
        }
        
        rebuildLatestGrades = false;
	}
	
	/**
	 * Add the LMS course record to the list
	 * 
	 * @param record a course record to add
	 */
	public void addRecord(LMSCourseRecord record){
	    
	    if(record == null){
	        throw new IllegalArgumentException("The record can't be null.");
	    }
	    
		records.add(record);
		
		synchronized (rebuildLatestGrades) {
		    rebuildLatestGrades = true;
		}
	}
	
	/**
	 * Add the LMS course records to the list
	 * 
	 * @param records a collection of course records to add
	 */
	public void addRecords(List<LMSCourseRecord> records){
	    
        if(records == null){
            throw new IllegalArgumentException("The records can't be null.");
        }
        
	    this.records.addAll(records);
	    
        synchronized (rebuildLatestGrades) {
            rebuildLatestGrades = true;
        }
	}
	
	/**
	 * Return the map of assessments
	 * 
	 * @return map of assessments found in the records.  Will be null if no assessment where found.
	 */
	public Map<String, AssessmentLevelEnum> getAssessments() {
		return assessments;
	}
	
	/**
	 * Returns the assessment for the specified knowledge, skill, or ability
	 * @param name The name of the knowledge, skill, or ability
	 * @return AssessmentLevelEnum - The assessment found or UNKNOWN if not found
	 */
	public AssessmentLevelEnum getAssessment(String name) {
	    
		AssessmentLevelEnum assessment = AssessmentLevelEnum.UNKNOWN;
		if(assessments != null) {
			assessment = assessments.get(name.toUpperCase());
			if(assessment == null){
				assessment = AssessmentLevelEnum.UNKNOWN;
			}
		}
		return assessment;
	}

	/**
	 * Add an assessment to the map
	 * @param name The name of the knowledge, skill, or ability
	 * @param assessmentLevel The level of assessment
	 */
	public void addAssessment(String name, AssessmentLevelEnum assessmentLevel) {

	    if(assessments == null){
	        assessments = new HashMap<String, AssessmentLevelEnum>();
	    }
		assessments.put(name.toUpperCase(), assessmentLevel);
	}
	
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[LMSCourseRecords: ");
        sb.append(" records = {\n");
        for(LMSCourseRecord record : getRecords()){
            sb.append(record).append(",\n");
        }
        sb.append("}"); 
        sb.append("]");

        return sb.toString();
    }
}
