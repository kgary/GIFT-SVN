/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.experiment;

import static mil.arl.gift.common.util.StringUtils.isBlank;
import static mil.arl.gift.common.util.StringUtils.join;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.util.StringUtils;

/**
 * Represents a collection of experiments that are meant to be taken together.
 *
 * @author tflowers
 *
 */
public class CourseCollection implements Serializable {

    /** The version of the class used for serialization */
    private static final long serialVersionUID = 1L;

    /** The id for the {@link CourseCollection} */
    private String id;

    /** The name of the user who owns this course */
    private String owner;

    /** The name of the {@link CourseCollection} */
    private String name;

    /** The description of the {@link CourseCollection} */
    private String description;

    /** The links to the experiments that belong to this collection. */
    private List<DataCollectionItem> courses;

    /** The URL at which the course group can be viewed and executed */
    private String url;

    /**
     * The mapping of users to their permissions for the
     * {@link CourseCollection}
     */
    private Map<String, DataCollectionUserRole> userToPermission = new HashMap<>();


    /**
     * No argument constructor for GWT serialization purposes.
     */
    private CourseCollection() {

    }

    /**
     * Create a {@link CourseCollection} from a {@link Collection} of links.
     *
     * @param id The {@link String} value uniquely identifying this
     *        {@link CourseCollection}.
     * @param owner The name of the user who owns the course. Can't be blank.
     * @param name The name of the {@link CourseCollection}. Can't be null or
     *        empty.
     * @param description The description of the {@link CourseCollection}. Can't
     *        be null or empty.
     * @param courses The {@link List} of courses that belong to the collection.
     *        Can't be null or empty.
     * @param url The URL at which the course collection can be viewed or taken.
     *        Can't be null or empty.
     */
    public CourseCollection(String id, String owner, String name, String description, List<DataCollectionItem> courses, String url) {
        this();

        setId(id);
        setOwner(owner);
        setName(name);
        setDescription(description);
        setCourses(courses);
        setUrl(url);

        initPermissionMap();
    }

    /**
     * Populates the {@link #userToPermission} with each user's permissions for
     * the collection.
     */
    private void initPermissionMap() {
        userToPermission.clear();

        /* If there are no courses then there is no work that can be done */
        if (courses.isEmpty()) {
            return;
        }

        final DataCollectionItem firstCourse = courses.get(0);

        /* Seed the aggregate permissions with the permissions found on the
         * first course. */
        for (DataCollectionPermission perm : firstCourse.getPermissions()) {
            if(perm.getDataCollectionUserRole() != null) {
                userToPermission.put(perm.getUsername(), perm.getDataCollectionUserRole());
            }
        }

        /* Aggregate the permissions of the remaining courses into the map. The
         * final map should reflect a "least common denominator" permission. */
        for (int i = 1; i < courses.size(); i++) {
            final DataCollectionItem course = courses.get(i);
            Iterator<Map.Entry<String, DataCollectionUserRole>> userIter = userToPermission.entrySet().iterator();
            while (userIter.hasNext()) {
                Entry<String, DataCollectionUserRole> entry = userIter.next();
                String username = entry.getKey();
                final DataCollectionUserRole existingPermission = entry.getValue();

                /* If the user was missing a permission for this course, they
                 * have no permissions for the collection */
                final DataCollectionUserRole permission = course.getPermissionForUser(username);
                if (permission == null) {
                    userIter.remove();
                    continue;
                }

                /* If the permission on this course is lower than the current
                 * aggregated permission, overwrite it */
                if (existingPermission.compareTo(permission) < 0) {
                    entry.setValue(permission);
                }
            }
        }
    }

    /**
     * The getter for the id that uniquely identifies this
     * {@link CourseCollection}.
     *
     * @return The {@link String} value of {@link #id}.
     */
    public String getId() {
        return id;
    }

    /**
     * The setter for the id that uniquely identifies this
     * {@link CourseCollection}.
     *
     * @param id The new {@link String} value of {@link #id}.
     */
    private void setId(String id) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("The parameter 'id' cannot be blank.");
        }

        this.id = id;
    }

    /**
     * Getter for the user who owns the course.
     *
     * @return The {@link String} value of {@link #owner}. Can't be blank.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for the user who owns the course.
     *
     * @param owner The new {@link String} value of {@link #owner}. Can't be
     *        blank.
     */
    private void setOwner(String owner) {
        if (isBlank(owner)) {
            throw new IllegalArgumentException("The parameter 'owner' cannot be blank.");
        }

        this.owner = owner;
    }

    /**
     * The getter for the display name of the {@link CourseCollection}.
     *
     * @return The {@link String} value of {@link #name}.
     */
    public String getName() {
        return name;
    }

    /**
     * The setter for the display name of the {@link CourseCollection}.
     *
     * @param name The new {@link String} value of {@link #name}.
     */
    public void setName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("The parameter 'name' cannot be blank.");
        }

        this.name = name;
    }

    /**
     * The getter for the description that describes the
     * {@link CourseCollection} in more detail.
     *
     * @return The {@link String} value of {@link #description}.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The setter for the description that describes the
     * {@link CourseCollection} in more detail.
     *
     * @param description The new {@link String} value of {@link #description}.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The getter for the {@link List} of {@link DataCollectionItem} that are
     * contained within this {@link CourseCollection}.
     *
     * @return The {@link List} of {@link DataCollectionItem} value of
     *         {@link #courses}. Can't be null. Can be empty.
     */
    public List<DataCollectionItem> getCourses() {
        return courses;
    }

    /**
     * The setter for the {@link List} of {@link DataCollectionItem} that are
     * contained within this {@link CourseCollection}.
     *
     * @param courses The new {@link List} of {@link DataCollectionItem} value
     *        of {@link #courses}.
     */
    private void setCourses(List<DataCollectionItem> courses) {
        if (courses == null) {
            throw new IllegalArgumentException("The parameter 'courses' cannot be null.");
        }

        this.courses = courses;
    }

    /**
     * Getter for the URL at which the {@link CourseCollection} can be viewed or
     * taken.
     *
     * @return The {@link String} value of {@link #url}.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Setter for the URL at which the {@link CourseCollection} can be viewed or
     * taken.
     *
     * @param url The new {@link String} value of {@link #url}.
     */

    private void setUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("The parameter 'url' cannot be blank.");
        }

        this.url = url;
    }

    /**
     * Gets the {@link DataCollectionUserRole} permission on this collection for
     * a given user.
     *
     * @param username The name of the user for which to fetch the permissions.
     *        Can't be blank.
     * @return The {@link DataCollectionUserRole} for this user. Null indicates
     *         the user has no permissions for this collection. Always null for
     *         all users when the collection is empty.
     */
    public DataCollectionUserRole getCollectionPermissionForUser(String username) {
        return userToPermission.get(username);
    }

    /**
     * Removes all the {@link DataCollectionItem} from {@link #courses} that a
     * provided user does not have permission to view.
     *
     * @param username The name of the user for whom courses should be removed.
     *        Can't be null or empty.
     */
    public void removeCoursesByVisibility(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        Iterator<DataCollectionItem> courseIter = getCourses().iterator();
        while (courseIter.hasNext()) {
            DataCollectionItem item = courseIter.next();
            DataCollectionUserRole assignedPermission = item.getPermissionForUser(username);

            /* If there is no permission defined, remove the course from the
             * visible collection. */
            if (assignedPermission == null) {
                courseIter.remove();
            }
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("[CourseCollection: ")
                .append("id = ").append(id)
                .append(", name = ").append(name)
                .append(", description = ").append(description)
                .append(", courses = { ").append(join(", ", courses))
                .append(" }]").toString();
    }
}
