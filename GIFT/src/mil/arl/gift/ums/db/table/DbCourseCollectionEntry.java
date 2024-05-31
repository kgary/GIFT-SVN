/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The Hibernate class that represents an entry in a course collection.
 *
 * @author tflowers
 *
 */
@Entity
@Table(name = "coursecollectionentry")
public class DbCourseCollectionEntry implements Serializable {

    /** The version used by the serialization logic */
    private static final long serialVersionUID = 1L;

    /** The unique id of the experiment collection */
    private String id;

    /** The position of this entry within the collection */
    private int position;

    /** The experiments which belong to the collection */
    private DbDataCollection experiment;

    /**
     * Getter for the unique id of this experiment collection.
     *
     * @return The value of the {@link #id}.
     */
    @Id
    @Column(name = "collectionId_FK")
    public String getId() {
        return id;
    }

    /**
     * Setter for the unique id of this experiment collection.
     *
     * @param id The new value of {@link #id}.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for the position of this entry within the course collection.
     *
     * @return The value of {@link #position}. Must be greater than or equal to
     *         0.
     */
    @Column(name = "collectionIndex", nullable = false)
    public int getPosition() {
        return position;
    }

    /**
     * Setter for the position of this entry within the course collection.
     *
     * @param position The new value of {@link #position}. Must be greater than
     *        or equal to 0.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Getter for the experiments that belong to this collection.
     *
     * @return The value of {@link #experiment}. Can be empty.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "courseId_FK")
    public DbDataCollection getCourse() {
        return experiment;
    }

    /**
     * Setter for the experiment that belong to this collection.
     *
     * @param experiment The new value of {@link #experiment}. Can be empty.
     */
    public void setCourse(DbDataCollection experiment) {
        this.experiment = experiment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (experiment == null ? 0 : experiment.hashCode());
        result = prime * result + (id == null ? 0 : id.hashCode());
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
        DbCourseCollectionEntry other = (DbCourseCollectionEntry) obj;
        if (experiment == null) {
            if (other.experiment != null)
                return false;
        } else if (!experiment.equals(other.experiment))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("[DbCourseCollectionEntry: ")
                .append("id = ").append(id)
                .append(", position = ").append(position)
                .append(", course = ").append(experiment)
                .append("]").toString();
    }
}