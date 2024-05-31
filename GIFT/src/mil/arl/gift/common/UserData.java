/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.enums.GenderEnum;

/**
 * This class contains information about a user such as biographical information
 *  
 * @author mhoffman
 *
 */
public class UserData{

    /** unique user id among all users of this GIFT instance */
    private Integer userId = null;
    
    /** (optional) a unique experiment id if this user is a subject in an experiment */
    private String experimentId = null;
    
    /** the user's gender */
    private GenderEnum gender = null;
    
    /** 
     * a user name for the LMS database(s) being used by GIFT.  By default GIFT uses the local LMS created by GIFT which
     * simply stores scoring information authored in a DKF.  However the LMS has the ability to connect to multiple LMS/LRS databases
     * and therefore we could see this attribute turn into a list of user names based on the various connected accounts on those databases. 
     */
    private String lmsUserName = null;
    
    /** 
     * a user name that can be authenticated when accompanied with a password and is therefore unique.
     * This is an optional field in order to support experiments which require anonymity.
     */
    private String username;
    
    /**
     * Class constructor - set attributes
     * 
     * @param userId - unique user id
     * @param lmsUserName - user's LMS user name
     * @param gender - gender of the user
     */
    public UserData(Integer userId, String lmsUserName, GenderEnum gender){
        setUserId(userId);
        this.lmsUserName = lmsUserName;
        setGender(gender);
    }
    
    /**
     * Class constructor - set attributes of a new user with unknown user id at this point
     * 
     * @param lmsUserName - user's LMS user name
     * @param gender - gender of the user
     */
    public UserData(String lmsUserName, GenderEnum gender){
        this.lmsUserName = lmsUserName;
        setGender(gender);
    }
    
    /**
     * Set the user id 
     * 
     * @param userId - unique user id
     */
    public void setUserId(Integer userId){
        
        if(userId == null){
            throw new IllegalArgumentException("The user id can't be null");
        }
        
        this.userId = userId;
    }
    
    /**
     * Return the user's id
     * 
     * @return int - unique user id
     */
    public Integer getUserId(){
        return userId;
    }
    
    /**
     * Set the gender
     * 
     * @param gender - gender of the user
     */
    private void setGender(GenderEnum gender){
        
        if(gender == null){
            throw new IllegalArgumentException("The gender can't be null");
        }
        
        this.gender = gender;
    }

    /**
     * Return the user's gender
     *
     * @return GenderEnum - gender of the user
     */
    public GenderEnum getGender() {
        return gender;
    }

    /**
     * Return the User's LMS Username
     *
     * @return String
     */
    public String getLMSUserName() {
        return this.lmsUserName;
    }
    
    /**
     * Return the username for this user
     * 
     * @return String the username for this user.  Can be null.
     */
    public String getUsername(){
        return username;
    }
    
    /**
     * Set the user name for this user
     * (For more information refer to the class attribute's javadoc)
     * 
     * @param username the user name for this user.  Can be null.
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * Return the unique experiment id if this user is a subject in an experiment
     * 
     * @return can be null if the user is not in an experiment
     */
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * Set the unique experiment id if this user is a subject in an experiment
     * 
     * @param experimentId can be null if the user is not in an experiment
     */
    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[UserData: ");
        sb.append("userId = ").append(getUserId());
        sb.append(", experimentId = ").append(getExperimentId());
        sb.append(", username = ").append(getUsername());
        sb.append(", gender = ").append(getGender());
        sb.append(", LMS-username = ").append(getLMSUserName());
        sb.append("]");

        return sb.toString();
    }
}
