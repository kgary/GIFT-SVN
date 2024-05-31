/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OrderBy;

/**
 * The Hibernate class that represents a collection of courses.
 *
 * @author tflowers
 *
 */
@Entity
@Table(name = "coursecollection")
public class DbCourseCollection {

    /** The unique identifier of the collection */
    private String id;

    /** The display name of the collection */
    private String name;

    /** The name of the user who owns this collection */
    private String owner;

    /** The description of the collection */
    private String description;

    /** The {@link List} of course entries in the collection */
    private List<DbCourseCollectionEntry> entries;

    /**
     * Getter for the unique identifier of the {@link DbCourseCollection}.
     *
     * @return The value of {@link #id}.
     */
    @Id
    @Column(name = "collectionId_PK")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    public String getId() {
        return id;
    }

    /**
     * Setter for the unique identifier of the {@link DbCourseCollection}.
     *
     * @param id The new value of {@link #id}.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for the display name of the {@link DbCourseCollection}.
     *
     * @return The value of {@link #name}.
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Setter for the display name of the {@link DbCourseCollection}.
     *
     * @param name The new value of {@link #name}.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Getter for the description of the {@link DbCourseCollection}.
     *
     * @return The value of {@link #description}.
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * Setter for the description of the {@link DbCourseCollection}.
     *
     * @param description The new value of {@link #description}.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the entries within the {@link DbCourseCollection}.
     *
     * @return The value of {@link #entries}.
     */
    @OneToMany(targetEntity = DbCourseCollectionEntry.class, mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy(clause = "position")
    public List<DbCourseCollectionEntry> getEntries() {
        return entries;
    }

    /**
     * Setter for the entries within the {@link DbCourseCollection}.
     *
     * @param entries The new value of {@link #entries}.
     */
    public void setEntries(List<DbCourseCollectionEntry> entries) {
        this.entries = entries;
    }
}
