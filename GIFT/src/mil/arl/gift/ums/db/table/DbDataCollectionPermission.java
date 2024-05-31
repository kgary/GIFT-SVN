/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.util.StringUtils;

/**
 * Contains permission information for a single data collection (e.g. Experiment) row.
 * 
 * @author mhoffman
 *
 */
@Entity
@Table(name="datacollectionpermission")
public class DbDataCollectionPermission implements Serializable, Comparable<DbDataCollectionPermission>{

    /**
     * default - auto generated
     */
    private static final long serialVersionUID = 1L;

    /** a unique id of a data collection (e.g. Experiment) among all data collection types (e.g. Experiment, LTI) */
    private String dataCollectionId;
    
    /** the gift username the permission value is for */
    private String username;
    
    /** the type of role assigned to this gift user for the data collection row */
    private DataCollectionUserRole role;
    
    /**
     * Set a unique id of a data collection (e.g. Experiment) among all data collection types (e.g. Experiment, LTI)
     * @param dataCollectionId the unique id
     */
    public void setDataCollectionId(String dataCollectionId){
        this.dataCollectionId = dataCollectionId;
    }
    
    /**
     * Return a unique id of a data collection (e.g. Experiment) among all data collection types (e.g. Experiment, LTI)
     * 
     * @return the unique id
     */
    @Id
    @Column(name="dataCollectionId_FK", nullable = false, unique = false)
    public String getDataCollectionId(){
        return dataCollectionId;
    }
    
    /**
     * Return the gift username the permission value is for
     * @return the gift username, won't be null
     */
    @Id
    @Column(nullable = false, unique = false)
    public String getUsername() {
        return username;
    }

    /**
     * Set the gift username the permission value is for
     * @param username won't be null
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Return the type of role assigned to this gift user for the data collection row
     * @return the DataCollectionUserRole for this instance
     */
    @Column(name="dataCollectionUserRole") 
    @Enumerated(EnumType.STRING) 
    public DataCollectionUserRole getDataCollectionUserRole() {
        return role;
    }

    /**
     * Set the type of role assigned to this gift user for the data collection row
     * @param role the DataCollectionUserRole to set
     */
    public void setDataCollectionUserRole(DataCollectionUserRole role) {
        this.role = role;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.dataCollectionId);
        hash = 53 * hash + Objects.hashCode(this.username);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {

            return false;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }

        final DbDataCollectionPermission other = (DbDataCollectionPermission) obj;

        if (!StringUtils.equals(this.dataCollectionId, other.getDataCollectionId())) {

            return false;
        }

        if (!StringUtils.equals(this.username, other.getUsername())) {

            return false;
        }

        return true;
    }
    

    @Override
    public int compareTo(DbDataCollectionPermission other) {
        
        /* Overriding compareTo to group DbDataCollectionPermissions by their user roles, and then sorting by username within each group.
        * The user role groupings will be sorted based on the order they were declared in DataCollectionUserRole. */
        DataCollectionUserRole thisUserRole = this.getDataCollectionUserRole();
        DataCollectionUserRole otherUserRole = other.getDataCollectionUserRole();
        if (thisUserRole != null && otherUserRole != null && thisUserRole != otherUserRole){
            // enum's compareTo method uses the order the objects are declared as their natural order.
            return thisUserRole.compareTo(otherUserRole);
        }

        return this.getUsername().compareTo(other.getUsername());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[DbDataCollectionPermission: dataCollectionId = ");
        builder.append(dataCollectionId);
        builder.append(", username = ");
        builder.append(username);
        builder.append(", role = ");
        builder.append(role);
        builder.append("]");
        return builder.toString();
    }
    
    
}
