/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * This is the base class for performance assessment classes
 * 
 * This class overrides the original Abstract Assessment class at
 * mil/arl/gift/common/AbstractAssessment.java so that client-side code
 * can use its performance node state enum. Any changes made to this class
 * must be also be made to that class as well.
 * 
 * @author nroberts
 *
 */
public class AbstractAssessment {
    
    /**
     * Used to enumerate the different states of tasks/concepts
     * 
     * @author mhoffman
     *
     */
    public enum PerformanceNodeStateEnum{
        
        UNACTIVATED ("UNACTIVATED", "unactivated"),  //means the task/concept was never active
        ACTIVE ("ACTIVE", "active"),                 //means the task/concept is currently being assessed
        DEACTIVATED ("DEACTIVATED", "deactivated"),  //means the concept's conditions never finished but the concept is NOT currently being assessed
                                                     //because the ancestor task is no longer active
        FINISHED ("FINISHED", "finished");           //means the task/concept gracefully finished and is NOT currently being assessed
        
        /** the unique name value for the enum */
        private String name;
        
        /** the display string for the enum */
        private String displayName;
        
        /**
         * Set attributes.
         * 
         * @param name the unique name value for the enum. Can't be null or empty.
         * @param displayName the display string for the enum.  Can't be null or empty.
         */
        private PerformanceNodeStateEnum(String name, String displayName){
            setName(name);
            setDisplayName(displayName);
        }

        /**
         * the unique name value for the enum.
         * 
         * @return won't be null or empty.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the unique name value for the enum
         * 
         * @param name  can't be null or empty.
         */
        private void setName(String name) {
            
            if(StringUtils.isBlank(name)){
                throw new IllegalArgumentException("The name can't be null or empty.");
            }
            
            this.name = name;
        }

        /**
         * the display string for the enum.
         * 
         * @return won't be null or empty.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Set the display string for the enum.
         * 
         * @param displayName can't be null or empty.
         */
        private void setDisplayName(String displayName) {
            
            if(StringUtils.isBlank(displayName)){
                throw new IllegalArgumentException("The display name can't be null or empty.");
            }
            
            this.displayName = displayName;        
        }
    }
}
