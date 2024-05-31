/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.course.ConceptNode;
import generated.course.Concepts;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;

/**
 * The Publish Lesson score contains the records of a single lesson for a learner.
 * 
 * @author mhoffman
 *
 */
public class PublishLessonScore{

    /** learner's unique LMS username */
    private String lmsUsername;

    /** a single course's lms data */
    private LMSCourseRecord record;
    
    /** the hierarchy of concepts covered by a course */
    private Concepts.Hierarchy concepts;

    /**
     * Class constructor 
     * 
     * @param lmsUsername - LMS user for this score
     * @param record - the score information
     * @param concepts - the hierarchy of concepts covered by a course. Can be null.
     */
    public PublishLessonScore(String lmsUsername, LMSCourseRecord record, Concepts.Hierarchy concepts) {

        this.lmsUsername = lmsUsername;
        this.record = record;
        this.concepts = concepts;
    }

    public String getLmsUsername() {
        return lmsUsername;
    }

    public LMSCourseRecord getCourseData() {
        return record;
    }

    /**
     * Gets the hierarchy of concepts covered by a course
     * 
     * @return the concepts. Can be null.
     */
    public Concepts.Hierarchy getConcepts() {
        return concepts;
    }
    
    /**
     * Convert the generated class concepts object into a string representation of the XML contents.
     * 
     * @param concepts the object to convert to an XML string
     * @return String the XML string created from the object.  Can be null if the object is null.
     * @throws Exception if there was a problem marshalling the object.
     */
    public static String getConceptsAsXMLString(ConceptNode concepts) throws Exception{
        
        if(concepts == null){
            return null;
        }
        
        String xmlString = 
                AbstractSchemaHandler.getAsXMLString(concepts, ConceptNode.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE);
        return xmlString;
    }
    
    /**
     * Convert the XML string concepts into a generated class instance.
     * 
     * @param xmlString the XML string to convert into a generated class object.
     * @return the new instance object created from the XML string.
     * @throws Exception if there was a problem unmarshalling the XML string
     */
    public static ConceptNode getConceptsFromXMLString(String xmlString) throws Exception{
        
        UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(xmlString, ConceptNode.class, null, true);
        return (ConceptNode) uFile.getUnmarshalled();
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[PublishLessonScore: ");
        sb.append(" lms-username = ").append(getLmsUsername());
        sb.append(", courseData = ").append(getCourseData());
        sb.append(", rootConcept = ").append(getConcepts());
        sb.append("]");

        return sb.toString();
    }
}
