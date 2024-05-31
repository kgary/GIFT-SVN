/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed information about any assessments that have been performed by a knowledge session,
 * such as variable information from condition classes 
 * 
 * @author nroberts
 */
public class KnowledgeAssessmentDetails {
    
    /** Condition variables associated with the knowledge session's assessments  */
    private List<KnowledgeSessionVariable> variables = new ArrayList<>();

    /**
     * Creates a new empty set of assessment details
     */
    public KnowledgeAssessmentDetails() {}
    
    /**
     * Creates a set of assessment details with the given variables
     * 
     * @param variables variables based on a knowledge session's condition assessments.
     * Cannot be null, but can be empty.
     */
    public KnowledgeAssessmentDetails(List<KnowledgeSessionVariable> variables) {
        this();
        
        if(variables == null) {
            throw new IllegalArgumentException("The list of assessment variables cannot be null");
        }
        
        this.variables = variables;
    }
    
    /**
     * Gets the variables from a knowledge session's condition assessments
     * 
     * @return the variables. Will not be null.
     */
    public List<KnowledgeSessionVariable> getVariables() {
        return variables;
    }
    
    /**
     * Adds a variable that's associated with a knowledge session's condition assessments
     * 
     * @param variable the variable to add. Cannot be null.
     */
    public void addVariable(KnowledgeSessionVariable variable) {
        
        if(variable == null) {
            throw new IllegalArgumentException(
                    "Only non-null varaibles can be added to a set of assessment details");
        }
        
        variables.add(variable);
    }

    /**
     * A representation of a variable from a knowledge session
     * 
     * @author nroberts
     */
    public static class KnowledgeSessionVariable {
        
        /** The name of the variable */
        private String name;
        
        /** The variable's value */
        private String value;
        
        /** The units used to measure the variable's value */
        private String units;
        
        /** The actor targeted by the variable */
        private String actor;

        /**
         * Creates a new knowledge session variable
         * 
         * @param name the name of the variable. Can be null.
         * @param value the variable's value. Can be null.
         * @param units the units used to measure the variable's value. Can be null.
         * @param actor the actor targeted by the variable. Can be null.
         */
        public KnowledgeSessionVariable(String name, String value, String units, String actor) {
            this.name = name;
            this.value = value;
            this.units = units;
            this.actor = actor;
        }

        /**
         * Gets the name of the variable
         * 
         * @return the name. Can be null.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the variable's value
         * 
         * @return the value. Can be null.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the units used to measure the variable's value
         * 
         * @return the units. Can be null.
         */
        public String getUnits() {
            return units;
        }

        /**
         * Gets the actor targeted by the variable
         * 
         * @return the actor. Can be null.
         */
        public String getActor() {
            return actor;
        }
        
        
    }
}
