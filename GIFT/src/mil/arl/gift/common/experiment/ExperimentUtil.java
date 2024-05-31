/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.experiment;

import mil.arl.gift.common.enums.SharedPublishedCoursePermissionsEnum;

/**
 * This class contains common logic for Experiments.
 *
 * @author mhoffman
 *
 */
public class ExperimentUtil {

    /**
     * The various states of an experiment.
     *
     * @author mhoffman
     *
     */
    public enum ExperimentStatus{
        RUNNING,
        PAUSED,
        ENDED,
        INVALID_COURSE
    }

    /**
     * The types of data collection data sets. 
     *
     * @author nblomberg
     *
     */
    public enum DataSetType {
        EXPERIMENT,  // allows for a Course URL, anonymous users, no dashboard login
        LTI,         // allows a course to be used in an LTI consumer (e.g. EdX)
        COURSE_DATA, // manages data behind courses taken through the dashboard
        COLLECTION  // contains one or more of the above types        
    }

    /**
     * The roles for sharing data collection items (i.e. published courses)
     *
     * @author mhoffman
     *
     */
    public enum DataCollectionUserRole {

        // NOTE: order matters for sorting by enum (e.g. DbDataCollectionPermission.compareTo)
        OWNER ("OWNER", "Owner"),
        MANAGER ("MANAGER", "Manager"),
        RESEARCHER ("RESEARCHER", "Researcher");

        /** a unique name for the enum */
        private String name;

        /** a display name for the enum */
        private String displayName;

        /**
         * Set attributes
         *
         * @param name the unique name for an enum
         * @param displayName the display name for the enum
         */
        private DataCollectionUserRole(String name, String displayName){
            this.name = name;
            this.displayName = displayName;
        }

        /**
         * Return the unique name for the enum
         *
         * @return the name for the enum
         */
        public String getName(){
            return name;
        }

        /**
         * Return the display name for the enum
         *
         * @return the display name for the enum
         */
        public String getDisplayName(){
            return displayName;
        }

        @Override
        public String toString(){
            return getName();
        }

    }

    /**
     * Convert a GIFT SharedPublishedCoursePermissionsEnum (used on GWT client side) to a DataCollectionUserRole enum (used by UMS db).
     *
     * @param sharedPublishedCoursePermissionsEnum the GIFT enum to convert
     * @return the equivalent enum value.  Null is returned if null is provided.
     */
    public static DataCollectionUserRole getDataCollectionUserRole(SharedPublishedCoursePermissionsEnum sharedPublishedCoursePermissionsEnum){

        if(sharedPublishedCoursePermissionsEnum == null){
            return null;
        }else if(sharedPublishedCoursePermissionsEnum == SharedPublishedCoursePermissionsEnum.OWNER){
            return DataCollectionUserRole.OWNER;
        }else if(sharedPublishedCoursePermissionsEnum == SharedPublishedCoursePermissionsEnum.MANAGER){
            return DataCollectionUserRole.MANAGER;
        }else if(sharedPublishedCoursePermissionsEnum == SharedPublishedCoursePermissionsEnum.RESEARCHER){
            return DataCollectionUserRole.RESEARCHER;
        }else{
            throw new UnsupportedOperationException("The user role "+sharedPublishedCoursePermissionsEnum+" is not handled");
        }
    }

    /**
     * Convert a DataCollectionUserRole enum (used by UMS db) to a GIFT SharedPublishedCoursePermissionsEnum (used on GWT client side).
     *
     * @param dataCollectionUserRole the enum to convert
     * @return the equivalent GIFT enum value.  Null is returned if null is provided.
     * @throws UnsupportedOperationException when the role is not mapped to an equivalent enum
     */
    public static SharedPublishedCoursePermissionsEnum getSharedPublishedCoursePermissionsEnum(DataCollectionUserRole dataCollectionUserRole) throws UnsupportedOperationException{

        if(dataCollectionUserRole == null){
            return null;
        }

        switch(dataCollectionUserRole){

        case OWNER:
            return SharedPublishedCoursePermissionsEnum.OWNER;
        case MANAGER:
            return SharedPublishedCoursePermissionsEnum.MANAGER;
        case RESEARCHER:
            return SharedPublishedCoursePermissionsEnum.RESEARCHER;
        default:
            throw new UnsupportedOperationException("The user role "+dataCollectionUserRole+" is not handled");
        }

    }
}
