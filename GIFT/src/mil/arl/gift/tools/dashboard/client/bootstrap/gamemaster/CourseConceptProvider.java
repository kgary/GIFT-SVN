/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generated.course.ConceptNode;
import generated.course.Concepts;

/**
 * A provider that provides information about the course concepts involved in a knowledge session
 * 
 * @author nroberts
 */
public class CourseConceptProvider {
    
    /** The singleton instance of this class */
    private static CourseConceptProvider instance;

    /** The current set of known course concept names*/
    private Set<String> courseConcepts = new HashSet<>();
    
    /**
     * Creates a new provider to provide course concepts
     */
    private CourseConceptProvider() {
        
    }
    
    /**
     * Parses the given course concept hierarchy for any course concepts and adds them to the 
     * stored course concept names
     * 
     * @param concepts the course concept hierarhcy. If null, no course concepts will be gathered.
     */
    public void updateCourseConcepts(Concepts.Hierarchy concepts) {
        
        courseConcepts.clear();
        
        if(concepts != null) {
            updateCourseConcepts(concepts.getConceptNode());
        }
    }
    
    /**
     * Parses the given concept node for any course concepts and adds them to the 
     * stored course concept names
     * 
     * @param concept the concept node to parse. If null, no course concepts will be gathered.
     */
    private void updateCourseConcepts(ConceptNode concept) {
        
        if(concept != null
                && concept.getName() != null) {
            
            courseConcepts.add(concept.getName().toLowerCase());
            
            if(!concept.getConceptNode().isEmpty()) {
                for(ConceptNode child : concept.getConceptNode()) {
                    updateCourseConcepts(child);
                }
            }
        }
    }
    
    /**
     * Gets whether the given concept name corresponds to a course concept
     * 
     * @param conceptName the concept name to check. If null, false will be returned.
     * @return whether the concept name matches a course concept
     */
    public boolean isCourseConcept(String conceptName) {
        
        if(conceptName == null) {
            return false;
        }
        
        return courseConcepts.contains(conceptName.toLowerCase());
    }
    
    /**
     * Gets a list of all the course concepts
     * 
     * @return the course concepts list. Will not be null.
     */
    public List<String> getCourseConceptList(){
        return new ArrayList<>(courseConcepts);
    }
    
    /**
     * Gets the singleton instance of this class;
     * 
     * @return the instance of this class. Will not be null.
     */
    public static CourseConceptProvider get() {
        
        if(instance == null) {
            instance = new CourseConceptProvider();
        }
        
        return instance;
    }
}
