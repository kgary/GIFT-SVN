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
import java.util.List;

import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.io.CourseListFilter.CourseSourceOption;
import mil.arl.gift.common.util.StringUtils;

/**
 * A domain option represents a possible domain a learner can load and tutor on.
 *
 * @author mhoffman
 */
public class DomainOption implements Comparable<DomainOption>, Serializable {

    private static final long serialVersionUID = 6142007344413978434L;

    /** the user this domain option is built for */
    private String username = null;
    
    /** the domain option's name */
    private String domainName = null;

    /** 
     * the domain option's id 
     * Currently this is the relative path from the root directory (i.e. 
     * the root of the course folders) to the course.xml file that is used to run the course.
     * e.g. "mhoffman/auto 02 import/auto 02 import.course.xml"
     * This can be the same as the sourceId, for legacy experiments which when created
     * made a copy of the source course, this could look different.
     */
    private String domainId = null;

    /**
     * The source id
     * Currently this is the relative path from the root directory (i.e. 
     * the root of the course folders) to the course.xml file for the source
     * course for an experiment.
     * e.g. "mhoffman/auto 02 import/auto 02 import.course.xml"
     */
    private String sourceId = null;
    
    /** (optional) The description of the domain */
    private String description;
    
    /** (optional) The id of the survey context */
    private Integer surveyContextId = null;
    
    /** (optional) recommendation to the user about the domain option */
    private DomainOptionRecommendation domainOptionRecommendation;
    
    /** (optional) permissions for each each that has access to the domain */
    private List<DomainOptionPermissions> domainOptionPermissionsList;
    
    /** (optional) concepts for the domain option */
    private generated.course.Concepts concepts = null;
    
    /** (optional) enumerated source type for this domain option (e.g. showcase course because its in the public workspace)*/
    private CourseSourceOption courseSourceOptionType = null;
    
    /** An enumeration to specify the sort order */
    private enum SortOrder { RECOMMENDED, NO_RECOMMENDATION, NOT_RECOMMENDED, WARNING, UNAVAILABLE }
    
    /** List of images to use on Public course tiles including default image (Paths will be changed in the future) */
    public static final String COURSE_DEFAULT_IMAGE = "images/course_tiles/course_default_books.jpg";
    public static final String COURSE_TYPE_RECOMMENDED = "images/Recommended.png";
    public static final String COURSE_TYPE_REFRESHER = "images/NotRecommended.png";
    public static final String COURSE_TYPE_INVALID = "images/invalid.png";
    public static final String COURSE_TYPE_WARNING = "images/Unavailable.png";
    public static final String FILE_NOT_FOUND_IMAGE = "images/course_tiles/image_not_found.png";
    
    /** ImageURL for course tile */
    private String imageURL = "";

    /**
     * The flag used by desktop mode to determine if the file is readable.
     * Server mode will determine if it's readable based on permissions.
     * Defaults to true.
     */
    private boolean isDomainIdReadable = true;

    /**
     * The flag used by desktop mode to determine if the file is writable. Server mode will
     * determine if it's writable based on permissions. Defaults to true.
     */
    private boolean isDomainIdWritable = true;
    
    /**
     * The flag used to indicate if this course is used by a publish course instance
     * that this user has access too. Defaults to false.
     */
    private boolean hasAccessiblePublishCourse = false;

    /**
     * Default Constructor (for gwt serialization)
     */
    private DomainOption() {
        
    }
    
    /**
     * Class constructor - set attributes
     *
     * @param name - name of the domain. Can't be null or empty.
     * @param id - id for the domain.  Currently this is the relative path from the root directory (i.e. 
     * the root of the course folders) to the course.xml file.  Can't be null or empty. e.g. "mhoffman/auto 02 import/auto 02 import.course.xml"
     * @param description - description of the domain.  Can be null.
     * @param username - name of the user requesting to build the domain option data.  Should only be null if the username
     * is not needed for permission checks, e.g. experiment course validation.
     */
    public DomainOption(String name, String id, String description, String username) {  
        this();
        setUsername(username);
        
        setDomainName(name);
        
        if(id == null || id.isEmpty()){
            throw new IllegalArgumentException("The domain id can't be null or empty.");
        }
        
        domainId = id;
        
        this.description = description;
    }

    /**
     * Class constructor - set attributes
     *
     * @param name - name of the domain. Can't be null or empty.
     * @param id - id for the domain.  Currently this is the relative path from the root directory (i.e. 
     * the root of the course folders) to the course.xml file.  Can't be null or empty.
     * @param sourceId - the source id of the course. This is used for experiments to identify the source course directory. Can be null.
     * @param description - description of the domain.  Can be null.
     * @param username - name of the user requesting to build the domain option data.  Should only be null if the username
     * is not needed for permission checks, e.g. experiment course validation.
     */
    public DomainOption(String name, String id, String sourceId, String description, String username) {        
        this(name, id, description, username);

        setSourceId(sourceId);
    }

    /**
     * Gets the name of the user requesting to build the domain option data.
     *
     * @return name of the user requesting to build the domain option data. Will only be null if the username
     * is not needed for permission checks, e.g. experiment course validation.
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the name of the user requesting to build the domain option data.
     * 
     * @param name the name of the user requesting to build the domain option data. Should only be null if the username
     * is not needed for permission checks, e.g. experiment course validation.
     */
    private void setUsername(String name){       
        username = name;
    }
    
    /**
     * Gets the domain's name
     *
     * @return String The domain's name.  Won't be null or empty.
     */
    public String getDomainName() {

        return domainName;
    }
    
    /**
     * Sets the Domain's name
     * 
     * @param name the name used to set. Can't be null or empty.
     */
    public void setDomainName(String name){
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The domain name can't be null or empty.");
        }
        
        domainName = name;
    }

    /**
     * Gets the domain's ID
     * Currently this is the relative path from the root directory (i.e. 
     * the root of the course folders) to the course.xml file.
     *
     * @return the domain's ID. Won't be null or empty. e.g. "mhoffman/auto 02 import/auto 02 import.course.xml"
     */
    public String getDomainId() {
        return domainId;
    }
    
    /**
     * Get the domain's course folder path.
     * Currently this is the relative path from the root directory (i.e. the root of the course folders)
     * to the course folder.
     * @return the course folder path, Won't be null or empty.  e.g. "mhoffman/auto 02 import"
     */
    public String getCourseFolderPath(){
        String courseFolderPath = domainId.substring(0, domainId.lastIndexOf("/"));
        return courseFolderPath;
    }

    /**
     * Gets the source Id
     * @return the sourceId.  Can be null.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Sets the source Id
     * @param sourceId the sourceId to set
     */
    private void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Gets the domain's description
     *
     * @return String The domain's description.  Can be null.
     */
    public String getDescription() {

        return description;
    }
    
    /** 
     * Sets the survey context id for this course. If null, the survey context will not be
     * deleted if the user deletes the course.
     * 
     * @param surveyContextId The survey context id. Can be null
     */
    public void setSurveyContextId(Integer surveyContextId) {
    	this.surveyContextId = surveyContextId;
    }
    
    /** 
     * Gets the survey context id for this course
     * 
     * @return The survey context id. Can be null.
     */
    public Integer getSurveyContextId() {
    	return surveyContextId;
    }
    
    /**
     * Set the recommendation to the user about the domain option.
     * 
     * @param domainOptionRecommendation - information about recommendation.  Can be null for no recommendation.
     */
    public void setDomainOptionRecommendation(DomainOptionRecommendation domainOptionRecommendation){
        this.domainOptionRecommendation = domainOptionRecommendation;
    }
    
    /**
     * Return the recommendation to the user about the domain option.
     * 
     * @return DomainOptionRecommendation - information about recommendation.  Can be null.
     */
    public DomainOptionRecommendation getDomainOptionRecommendation(){
        return domainOptionRecommendation;
    }

    /**
     * Retrieves the permission information for all users that have access to this domain.
     * 
     * @return the information containing the user permissions. Can be null if in desktop mode.
     */
    public List<DomainOptionPermissions> getDomainOptionPermissions() {
        return domainOptionPermissionsList;
    }
    
    /**
     * Adds the specified user's permission information to the domain permission list.
     * 
     * @param domainOptionPermissions information about user permissions. Will not add if
     *            permissions is null.
     */
    public void addDomainOptionPermissions(DomainOptionPermissions domainOptionPermissions) {
        if (domainOptionPermissions != null) {
            if (getDomainOptionPermissions() == null) {
                domainOptionPermissionsList = new ArrayList<DomainOptionPermissions>();
                domainOptionPermissionsList.add(domainOptionPermissions);
            } else {

                // check for username conflict
                for (DomainOptionPermissions existingUser : getDomainOptionPermissions()) {
                    if (existingUser.getUser().equalsIgnoreCase(domainOptionPermissions.getUser())) {
                        existingUser.setPermission(domainOptionPermissions.getPermission());
                        return;
                    }
                }

                getDomainOptionPermissions().add(domainOptionPermissions);
            }
        }
    }
    
    /**
     * Adds the specified permissions information to the domain permission list.
     * 
     * @param domainOptionPermissionsList information about user permissions. Will not add if
     *            permissions is null.
     */
    public void setDomainOptionPermissions(List<DomainOptionPermissions> domainOptionPermissionsList) {
        if (getDomainOptionPermissions() != null) {
            getDomainOptionPermissions().clear();
        }

        if (domainOptionPermissionsList != null) {
            for (DomainOptionPermissions option : domainOptionPermissionsList) {
                addDomainOptionPermissions(option);
            }
        }
    }
    
    /**
     * Checks if the user that requested this domain option is the owner.
     * 
     * @return true if the user is the owner; false otherwise.
     */
    public boolean isOwner() {
        String user = getUsername();
        return isOwner(user);
    }
    
    /**
     * Checks if the username provided is the owner of this domain option.
     * 
     * @param usernameToCheck the username to check permissions on this course for ownership.
     * If null or empty, false is returned
     * @return true if the user is the owner, false othewise.
     */
    public boolean isOwner(String usernameToCheck){
        
        boolean owner = false;

        if (getDomainOptionPermissions() != null && StringUtils.isNotBlank(usernameToCheck)) {
            for (DomainOptionPermissions permissions : getDomainOptionPermissions()) {
                if (permissions != null && (permissions.getUser().equalsIgnoreCase(usernameToCheck) 
                        || permissions.getUser().equalsIgnoreCase(DomainOptionPermissions.ALL_USERS))) {
                    owner = permissions.isOwner();
                    break;
                }
            }
        }

        return owner;
    }

    /**
     * This version of compareTo will sort domain options by recommendation 
     * type.
     * Priority: seeDomainOption.SortOrder enum
     * 
     * @param otherOption the other option to compare against this option
     * @return
     */
    @SuppressWarnings("unused")
    private int compareToByRecommendation(DomainOption otherOption){
        
        if (otherOption != null) {
            
            SortOrder thisValue;
            DomainOptionRecommendation rec = getDomainOptionRecommendation();
            if(rec != null){

                DomainOptionRecommendationEnum recEnum = rec.getDomainOptionRecommendationEnum();
                if(recEnum == DomainOptionRecommendationEnum.RECOMMENDED){
                    thisValue = SortOrder.RECOMMENDED;
                }else if(recEnum == DomainOptionRecommendationEnum.NOT_RECOMMENDED){
                    thisValue = SortOrder.NOT_RECOMMENDED;
                }else if(recEnum == DomainOptionRecommendationEnum.AVAILABLE_WITH_WARNING){
                    thisValue = SortOrder.WARNING;
                }else{
                    thisValue = SortOrder.UNAVAILABLE;
                }
                    
            }else{
                thisValue = SortOrder.NO_RECOMMENDATION;
            }
            
            SortOrder otherValue;
            rec = otherOption.getDomainOptionRecommendation();
            if(rec != null){

                DomainOptionRecommendationEnum recEnum = rec.getDomainOptionRecommendationEnum();
                if(recEnum == DomainOptionRecommendationEnum.RECOMMENDED){
                    otherValue = SortOrder.RECOMMENDED;
                }else if(recEnum == DomainOptionRecommendationEnum.NOT_RECOMMENDED){
                    otherValue = SortOrder.NOT_RECOMMENDED;
                }else{
                    otherValue = SortOrder.UNAVAILABLE;
                }
                    
            }else{
                otherValue = SortOrder.NO_RECOMMENDATION;
            }

            // If recommendations are the same, compare by name; Otherwise, compare by recommendation
            if(thisValue == otherValue){
                return this.getDomainName().compareToIgnoreCase(otherOption.getDomainName());
            }else{
                return Integer.compare(thisValue.ordinal(), otherValue.ordinal());
            }

        } else {

            return -1;
        }
    }
    
    /**
     * This version of compareTo will sort domain options by name.
     * 
     * @param otherOption the other option to compare against this option
     * @return
     */
    private int compareToByName(DomainOption otherOption){
        
        if (otherOption != null) {
            
            return this.getDomainName().toLowerCase().compareTo(otherOption.getDomainName().toLowerCase());
        } else {
            return -1;
        }
    }

    @Override
    public int compareTo(DomainOption otherOption) {
        return compareToByName(otherOption);
    }
    
    @Override
    public boolean equals(Object obj){
        
        if(obj instanceof DomainOption){
            
            DomainOption otherDomainOption = (DomainOption)obj;
            
            //check required, non null fields:
            // id and name
            if(!(this.getDomainId().equalsIgnoreCase(otherDomainOption.getDomainId()) && 
                    this.getDomainName().equals(otherDomainOption.getDomainName()))){
                return false;                
            }
            
            //check description
            //if this has a description it must match other
            //if this doesn't have a description, other must not have one either
            if(!(this.getDescription() != null && this.getDescription().equals(otherDomainOption.getDescription()) ||
                    (this.getDescription() == null && otherDomainOption.getDescription() == null))){
                   return false;     
            }
            
            //check domain option recommendation
            //if this has a recommendation it must match other
            //if this doesn't have a recommendation, other must not have one either
            if(!(this.getDomainOptionRecommendation() != null && this.getDomainOptionRecommendation().equals(otherDomainOption.getDomainOptionRecommendation()) ||
                    (this.getDomainOptionRecommendation() == null && otherDomainOption.getDomainOptionRecommendation() == null))){
                return false;
            }
                    
            //check image url
            if(!(this.getImageURL() != null && this.getImageURL().equals(otherDomainOption.getImageURL()) ||
                    (this.getImageURL() == null && otherDomainOption.getImageURL() == null))){
                return false;
            }
            
            return true;            

        }
        
        return false;
    }
    
    @Override
    public int hashCode(){
        
        int result = 7;
        result = 37 * result + this.getDomainId().hashCode();
        return result;
    }
    
    /**
     * Return the concepts associated with this domain option.
     * 
     * @return generated.course.Concepts - The concepts associated with this course. Can be null.
     */
    public generated.course.Concepts getConcepts() {
        return concepts;
    }
    
    /**
     * Set the concepts for this domain option.
     * 
     * @param concepts - The course concepts. Can be null.
     */
    public void setConcepts(generated.course.Concepts concepts) {
        this.concepts = concepts;
    }
    
    public void setImageURL(String URL){
    	imageURL = URL;
    }
    
    /**
     * The URL of the course image.
     * 
     * @return can be null or empty string
     */
    public String getImageURL(){
    	return imageURL;
    }

    /**
     * Returns if the domain is readable, meaning can be opened in course creator read only mode.<br/>
     * For local desktop mode this should always be true (unless specifically set to false), but for 
     * server (Nuxeo) mode, this may vary based on the permissions.<br/>
     * Also see @link {@link SharedCoursePermissionsEnum}.
     * 
     * @return true if the domain is readable; false otherwise.
     */
    public boolean isDomainIdReadable() {
        /* If writable, then readable is true */
        boolean writable = isDomainIdWritable();
        if (writable) {
            return true;
        }

        // desktop mode, return flag
        if (getDomainOptionPermissions() == null) {
            return isDomainIdReadable;
        }

        // server mode, check permissions

        boolean readable = false;

        String user = getUsername();
        if (getDomainOptionPermissions() != null && StringUtils.isNotBlank(user)) {
            for (DomainOptionPermissions permissions : getDomainOptionPermissions()) {
                if (user.equalsIgnoreCase(permissions.getUser())
                        || permissions.getUser().equalsIgnoreCase(DomainOptionPermissions.ALL_USERS)) {
                    readable = SharedCoursePermissionsEnum.VIEW_COURSE.equals(permissions.getPermission());
                    break;
                }
            }
        }

        return readable;
    }

    /**
     * Set whether this course is readable by the user wanting to view it.
     * @param isDomainIdReadable the value to set.  
     * @throws IllegalArgumentException if the course is write-able and the value provided is false,
     * i.e. can't make it not readable if it is write-able.
     */
    public void setDomainIdReadable(boolean isDomainIdReadable) throws IllegalArgumentException {
        if (!isDomainIdReadable && isDomainIdWritable()) {
            throw new IllegalArgumentException("The domain id is writable, so its readability cannot be false.");
        }
        this.isDomainIdReadable = isDomainIdReadable;
    }

    /**
     * Returns if the domain is writable. For local desktop mode this should always be true (unless
     * specifically set to false), but for server (Nuxeo) mode, this may vary based on the
     * permissions.
     * 
     * @return true if the domain is writable; false otherwise.
     */
    public boolean isDomainIdWritable() {
        // desktop mode, return flag
        if (getDomainOptionPermissions() == null) {
            return isDomainIdWritable;
        }
        
        // server mode, check permissions
        
        boolean writable = false;

        String user = getUsername();
        if (getDomainOptionPermissions() != null && StringUtils.isNotBlank(user)) {
            for (DomainOptionPermissions permissions : getDomainOptionPermissions()) {
                if (permissions.getUser().equalsIgnoreCase(user)
                        || permissions.getUser().equalsIgnoreCase(DomainOptionPermissions.ALL_USERS)) {
                    writable = SharedCoursePermissionsEnum.EDIT_COURSE.equals(permissions.getPermission());
                    break;
                }
            }
        }

        return writable;
    }
    
    /**
     * Set whether this course is write-able or not.
     * @param isDomainIdWritable the value to set
     */
    public void setDomainIdWritable(boolean isDomainIdWritable) {
        this.isDomainIdWritable = isDomainIdWritable;
    }
    
    /**
     * Return whether this user has access to a published course instance that references
     * this course.  The published course can be any type.
     * @return The flag used to indicate if this course is used by a publish course instance
     * that this user has access too. Default is false.
     */
    public boolean hasAccessiblePublishCourse(){
        return hasAccessiblePublishCourse;
    }
    
    /**
     * Set whether this user has access to a published course instance that references
     * this course.  The published course can be any type.
     * @param value flag used to indicate if this course is used by a publish course instance
     * that this user has access too
     */
    public void setHasAccessiblePublishCourse(boolean value){
        this.hasAccessiblePublishCourse = value;
    }

    /**
     * Return the concepts associate with this domain option as a list.
     * 
     * @return Can be empty but not null.
     */
    public List<String> getConceptsAsList(){
        
        List<String> conceptList = new ArrayList<>();
        if(concepts == null){
            return conceptList;
        }else if(concepts.getListOrHierarchy() instanceof generated.course.Concepts.List){
            
            for(generated.course.Concepts.List.Concept concept : ((generated.course.Concepts.List)concepts.getListOrHierarchy()).getConcept()){
                conceptList.add(concept.getName());
            }
            
        }else if(concepts.getListOrHierarchy() instanceof generated.course.Concepts.Hierarchy){
            
            generated.course.ConceptNode rootNode = ((generated.course.Concepts.Hierarchy)concepts.getListOrHierarchy()).getConceptNode();
            flattenConceptHierarcy(rootNode, conceptList);
        }
        
        return conceptList;
    }   
    
    /**
     * Recursively translate the concept map hierarchy into a flattened list.
     * 
     * @param node a concept hierarchy node to retrieve it's name as well as its child nodes
     * @param conceptList the list of concept names
     */
    private void flattenConceptHierarcy(generated.course.ConceptNode node, List<String> conceptList){
        
        conceptList.add(node.getName());
        for(generated.course.ConceptNode childNode : node.getConceptNode()){
            flattenConceptHierarcy(childNode, conceptList);
        }
    }

    /**
     * Return the enumerated type of source for this domain option 
     * @return enumerated source type for this domain option (e.g. showcase course because its in the public workspace).
     * Can be null.
     */
    public CourseSourceOption getCourseSourceOptionType() {
        return courseSourceOptionType;
    }

    /**
     * Set the source option type for this domain option.
     *  
     * @param courseSourceOptionType enumerated source type for this domain option 
     * (e.g. showcase course because its in the public workspace), can be null.
     */
    public void setCourseSourceOptionType(CourseSourceOption courseSourceOptionType) {
        this.courseSourceOptionType = courseSourceOptionType;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DomainOption: ");
        sb.append(" domain name = ").append(getDomainName());
        sb.append(", id = ").append(getDomainId());
        sb.append(", description = ").append(getDescription());
        sb.append(", source type = ").append(getCourseSourceOptionType());
        sb.append(", survey context id = ").append(getSurveyContextId());
        sb.append(", writable = ").append(isDomainIdWritable());
        sb.append(", readable = ").append(isDomainIdReadable());
        sb.append(", accessiblePublishedCourse = ").append(hasAccessiblePublishCourse());
        if(getDomainOptionRecommendation() != null){
            sb.append(", domainOptionRecommendation = ").append(getDomainOptionRecommendation());
        }
        if (getDomainOptionPermissions() != null) {
            sb.append(", permissions = {");
            StringUtils.join(", ", getDomainOptionPermissions(), sb);
            sb.append("}");
        }
        
        if(getConcepts() != null){
            
            sb.append(", concepts = {");
        	sb.append(getConcepts());
            sb.append("}");
        }
        sb.append("]");

        return sb.toString();
    }

	/**
	 * This inner class contains recommendation information for this domain option.
	 * 
	 * @author mhoffman
	 *
	 */
	public static class DomainOptionRecommendation implements Serializable {

        private static final long serialVersionUID = -7466478933181399156L;

        /** enumerated recommendation to the user about the domain option */
        private DomainOptionRecommendationEnum domainOptionRecommendationEnum;
        
        /** (optional) describes the reason for the recommendation to the user */
        private String reason;
        
        /** (optional) describes additional details about the reason for the recommendation to the user */
        private String details;
        
        /** (optional) the validation results of this domain option. */
        private CourseValidationResults courseValidationResults;
        
        /**
         * Constructor (default) - needed for gwt serialization
         */
        @SuppressWarnings("unused")
        private DomainOptionRecommendation() {
            
        }
        
        /**
         * Class constructor - set attribute(s)
         * 
         * @param domainOptionRecommendationEnum enumerated recommendation to the user about the domain option
         */
        public DomainOptionRecommendation(DomainOptionRecommendationEnum domainOptionRecommendationEnum){
            
            if(domainOptionRecommendationEnum == null){
                throw new IllegalArgumentException("The domain option recommendation enum can't be null.");
            }
            
            this.domainOptionRecommendationEnum = domainOptionRecommendationEnum;
        }
        
        /**
         * Set the reason for the recommendation to the user.
         * 
         * @param reason - the reason for the recommendation.  Can be null or empty.
         */
        public void setReason(String reason){
            this.reason = reason;            
        }
        
        /**
         * Return the reason for the recommendation to the user.
         * 
         * @return String - the recommendation description.  Can be null or empty.
         */
        public String getReason(){
            return reason;
        }
        
        /**
         * Set the details of the reason for the recommendation to the user.
         * 
         * @param details - the details of the recommendation.  Can be null or empty.
         */
        public void setDetails(String details){
            this.details = details;            
        }
        
        /**
         * Return the details of the reason for the recommendation to the user.
         * 
         * @return String - the details of the recommendation.  Can be null or empty.
         */
        public String getDetails(){
            return details;
        }
        
        /**
         * Return the enumerated recommendation to the user about the domain option.
         * 
         * @return the enumerated reason
         */
        public DomainOptionRecommendationEnum getDomainOptionRecommendationEnum(){
            return domainOptionRecommendationEnum;
        }
        
        public CourseValidationResults getCourseValidationResulst() {
            return courseValidationResults;
        }

        public void setCourseValidationResulst(CourseValidationResults courseValidationResulst) {
            this.courseValidationResults = courseValidationResulst;
        }

        @Override
        public boolean equals(Object obj){
         
            if(obj instanceof DomainOptionRecommendation){
                
                DomainOptionRecommendation other = (DomainOptionRecommendation)obj;
                
                return this.getDomainOptionRecommendationEnum() == other.getDomainOptionRecommendationEnum() &&
                        ((this.getDetails() != null && this.getDetails().equals(other.getDetails())) ||
                                (this.getDetails() == null && other.getDetails() == null)) &&
                        ((this.getReason() != null && this.getReason().equals(other.getReason())) ||
                                (this.getReason() == null && other.getReason() == null));
            }
            
            return false;
        }
        
        @Override
        public int hashCode(){
            
            int result = 7;
            if(getDetails() != null){
                result += 37 * result + getDetails().hashCode();
            }
            
            if(getReason() != null){
                result += 37 * result + getReason().hashCode();
            }
            
            result += getDomainOptionRecommendationEnum().getValue();
            
            return result;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[DomainOptionRecommendation: ");
            if(getDomainOptionRecommendationEnum() != null){
            	sb.append("type = ").append(getDomainOptionRecommendationEnum());
            }
            
            if(getReason() != null){
            	sb.append(", reason = ").append(getReason());
            }
            
            if(getDetails() != null){
                sb.append(", details = ").append(getDetails());
            }
            
            sb.append("]");
            return sb.toString();
        }
    }
	
    /**
     * Contains permission information for this domain option.
     * 
     * @author sharrison
     */
    public static class DomainOptionPermissions implements Serializable, Comparable<DomainOptionPermissions> {

        private static final long serialVersionUID = 1L;
        
        /** 
         * the string used to represent that all usernames get this permission value
         * DESKTOP MODE: this is used instead of usernames so that anyone who has access to that GIFT instance can access all courses.
         */
        public static final String ALL_USERS = "*";

        /** the user */
        private String user;

        /** enumerated permission for the user */
        private SharedCoursePermissionsEnum permission;

        /** indicates if the user is the owner of the domain option */
        private boolean isOwner;

        /**
         * Constructor (default) - needed for gwt serialization
         */
        private DomainOptionPermissions() {
        }

        /**
         * Constructor.
         * 
         * @param user the username. Can't be null.
         * @param permission the enumerated permission for the user.
         * @param isOwner indicates if the user is the owner of the domain option.
         */
        public DomainOptionPermissions(String user, SharedCoursePermissionsEnum permission, boolean isOwner) {
            this();

            if (StringUtils.isBlank(user)) {
                throw new IllegalArgumentException("The user can't be null.");
            }

            /* Always make the user lowercase so it can be used safely with the
             * database and nuxeo */
            this.user = user.toLowerCase().trim();
            this.permission = permission;
            this.isOwner = isOwner;
        }

        /**
         * Returns the username. Will always be lower case.
         * 
         * @return the username. Can't be null.
         */
        public String getUser() {
            return user;
        }

        /**
         * Returns the permission for the user.
         * 
         * @return the enumerated permission for the user. Can be null.
         */
        public SharedCoursePermissionsEnum getPermission() {
            return permission;
        }

        /**
         * Updates the user's permission information with the given permission enum.
         * 
         * @param permission the shared permission enum
         */
        public void setPermission(SharedCoursePermissionsEnum permission) {
            this.permission = permission;
        }

        /**
         * Indicates if the user is the owner of the domain option.
         * 
         * @return true if the user is the owner; false otherwise.
         */
        public boolean isOwner() {
            return isOwner;
        }

        @Override
        public int compareTo(DomainOptionPermissions other) {
            // little hack to make sure the owner always shows up first (in ascending)
            if (this.isOwner()) {
                return -1;
            } else if (other.isOwner()) {
                return 1;
            }

            return this.getUser().compareTo(other.getUser());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isOwner ? 1231 : 1237);
            result = prime * result + ((permission == null) ? 0 : permission.hashCode());
            result = prime * result + ((user == null) ? 0 : user.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DomainOptionPermissions other = (DomainOptionPermissions) obj;
            if (isOwner != other.isOwner)
                return false;
            if (permission == null) {
                if (other.permission != null)
                    return false;
            } else if (!permission.equals(other.permission))
                return false;
            if (user == null) {
                if (other.user != null)
                    return false;
            } else if (!user.equals(other.user))
                return false;
            return true;
        }

        @Override
        public String toString() {

            StringBuffer sb = new StringBuffer();
            sb.append("[DomainOptionPermission: ");
            sb.append("user = ").append(getUser());
            sb.append(", permission = ").append(getPermission());
            sb.append(", isOwner = ").append(isOwner());
            sb.append("]");
            return sb.toString();
        }
    }
}
