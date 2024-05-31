/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import mil.arl.gift.common.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A reference to a course records stored in an LMS/LRS/database that the LMS module has a connection to.
 * Useful for retrieving specific records for review during a course.
 * 
 * @author mhoffman
 *
 */
public class CourseRecordRef implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * contains zero or more references to course records
     */
    private AbstractCourseRecordRefId ref;

    /**
     * Set any course record reference(s) for this object.
     * @param ref can be null.
     */
    public void setRef(AbstractCourseRecordRefId ref){
        this.ref = ref;
    }
    
    /**
     * Return any course record references set in this object.
     * @return can be null.
     */
    public AbstractCourseRecordRefId getRef(){
        return ref;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ref == null) ? 0 : ref.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CourseRecordRef)) {
            return false;
        }
        CourseRecordRef other = (CourseRecordRef) obj;
        if (ref == null) {
            if (other.ref != null) {
                return false;
            }
        } else if (!ref.equals(other.ref)) {
            return false;
        }
        return true;
    }

    /**
     * Create a clone of the course record ref provided.
     * @param origRef contains the attributes to copy and create new instances for
     * @return a new course record ref instance
     */
    public static CourseRecordRef deepCopy(CourseRecordRef origRef){
        
        CourseRecordRef newRef = new CourseRecordRef();
        AbstractCourseRecordRefId origRefId = origRef.getRef();
        if(origRefId instanceof IntCourseRecordRefId){
            
            IntCourseRecordRefId origIntRefId = (IntCourseRecordRefId)origRefId;
            IntCourseRecordRefId newIntRefId = new IntCourseRecordRefId(origIntRefId.getRecordId());
            newRef.setRef(newIntRefId);
            
        }else if(origRefId instanceof UUIDCourseRecordRefIds){
            
            UUIDCourseRecordRefIds origUUIDRefIds = (UUIDCourseRecordRefIds)origRefId;
            UUIDCourseRecordRefIds newUUIDRefIds = new UUIDCourseRecordRefIds();
            for(String origId : origUUIDRefIds.getRecordUUIDs()){
                newUUIDRefIds.addRecordUUID(origId);
            }
            newRef.setRef(newUUIDRefIds);
        }
        
        return newRef;
    }
    
    /**
     * Create a new CourseRecordRef with the record id integer provided.  This is a
     * util method.
     * @param recordId an id of a course record
     * @return a new course record ref instance with that record id in it
     */
    public static CourseRecordRef buildCourseRecordRefFromInt(int recordId){
        
        CourseRecordRef ref = new CourseRecordRef();
        IntCourseRecordRefId intRef = new IntCourseRecordRefId(recordId);
        ref.setRef(intRef);
        
        return ref;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseRecordRef: ref = ");
        builder.append(ref);
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * Used to abstract the type of id used by the underlying course record storing system.
     * @author mhoffman
     *
     */
    @SuppressWarnings("serial")
    public static abstract class AbstractCourseRecordRefId implements Serializable{
        
        /**
         * Whether the record has a valid reference to a course record
         * @return true if there is at least one reference to a course record
         */
        public abstract boolean isEmpty();
    }
    
    /**
     * A single integer id mapped to a course record.
     * @author mhoffman
     *
     */
    @SuppressWarnings("serial")
    public static class IntCourseRecordRefId extends AbstractCourseRecordRefId{
        
        /** unique id of a GIFT course record in a particular system */
        private int recordId;
        
        /** Required for GWT serialization */
        @SuppressWarnings("unused")
        private IntCourseRecordRefId(){}
        
        public IntCourseRecordRefId(int recordId){
            this.recordId = recordId;
        }
        
        /**
         * Return the reference to the course record
         * 
         * @return id unique id of a course record that is stored in an external LMS connection.  Will be zero if not set, 
         * otherwise will be greater than zero.
         */
        public int getRecordId(){
            return recordId;
        }
        
        
        /**
         * Set the unique id of a GIFT course record that this object references
         * 
         * @param id unique id of a course record that is stored in an external LMS connection.  Must be greater than
         * zero.
         */
        public void setRecordId(int id){
            
            if(id <= 0){
                throw new IllegalArgumentException("The record id can't be less than or equal to zero");
            }
            recordId = id;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + recordId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof IntCourseRecordRefId)) {
                return false;
            }
            IntCourseRecordRefId other = (IntCourseRecordRefId) obj;
            if (recordId != other.recordId) {
                return false;
            }
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("[IntCourseRecordRefId: ");
            sb.append("id = ").append(recordId);
            sb.append("]");
            return sb.toString();
        }

    }
    
    /**
     * A collection of UUIDs for zero or more course records.
     * @author mhoffman
     *
     */
    @SuppressWarnings("serial")
    public static class UUIDCourseRecordRefIds extends AbstractCourseRecordRefId{
        
        /** collection of zero or more UUID(s) parsed from a Statement(s) corresponding to referenced record.
         * Can be null or empty. */
        private ArrayList<String> recordUUIDs;

        /**
         * No arg constructor
         */
        public UUIDCourseRecordRefIds(){            
        }
        
        /**
         * Return the UUID references for the course record 
         * @return collection of zero or more UUID(s) parsed from a Statement(s) corresponding to referenced record.
         * Can be null or empty.
         */
        public ArrayList<String> getRecordUUIDs() {
            return recordUUIDs;
        }
        
        
        /**
         * Null safe get UUID collection method.
         * @return existing recordUUIDs or empty ArrayList<String>
         */
        private ArrayList<String> existingRecordUUIDs() {
            ArrayList<String> coll = recordUUIDs != null ? recordUUIDs : new ArrayList<String>();
            return coll;
        }
        
        /**
         * Set the unique UUIDs of a GIFT course record that this object references
         * 
         * @param ids - Collection of zero or more UUIDs parsed from Statement(s) corresponding to referenced record
         */
        public void setRecordUUIDs(ArrayList<String> ids) {
            if(ids != null) {
                this.recordUUIDs = ids;
            }
        }
        
        /**
         * Adds id to recordUUIDs
         * @param id - UUID parsed from supporting xAPI Statement 
         */
        public void addRecordUUID(String id) {
            ArrayList<String> coll = existingRecordUUIDs();
            if(id != null) {
                coll.add(id);
            }
            setRecordUUIDs(coll);
        }        

        @Override
        public boolean isEmpty() {
            return CollectionUtils.isEmpty(recordUUIDs);
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((recordUUIDs == null) ? 0 : recordUUIDs.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof UUIDCourseRecordRefIds)) {
                return false;
            }
            UUIDCourseRecordRefIds other = (UUIDCourseRecordRefIds) obj;
            if (recordUUIDs == null) {
                if (other.recordUUIDs != null) {
                    return false;
                }
            } else if (!recordUUIDs.equals(other.recordUUIDs)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("[UUIDCourseRecordRefIds:");
            if(CollectionUtils.isNotEmpty(recordUUIDs)){
                sb.append(" ids = ").append(recordUUIDs);
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
