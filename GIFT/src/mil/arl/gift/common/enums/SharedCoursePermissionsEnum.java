/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This class contains the various enumerated shared permissions for a user about a domain option
 * (i.e. course) by a GIFT user.
 * 
 * @author sharrison
 *
 */
public class SharedCoursePermissionsEnum extends AbstractEnum implements Serializable {

    private static final long serialVersionUID = 1L;

    private static List<SharedCoursePermissionsEnum> enumList = new ArrayList<SharedCoursePermissionsEnum>(2);
    private static int index = 0;

    public static final SharedCoursePermissionsEnum TAKE_COURSE = new SharedCoursePermissionsEnum("TakeCourse", "Can Take");
    public static final SharedCoursePermissionsEnum VIEW_COURSE = new SharedCoursePermissionsEnum("ViewCourse", "Can View, Copy, Export, and Take");
    public static final SharedCoursePermissionsEnum EDIT_COURSE = new SharedCoursePermissionsEnum("EditCourse", "Can Edit, Copy, Export, and Take");
    
    // FUTURE IDEAS - not to be lost
    // public static final SharedCoursePermissionsEnum SHARE_COURSE = new SharedCoursePermissionsEnum("ShareCourse", "Share");
    // public static final SharedCoursePermissionsEnum EDIT_SURVEYS = new SharedCoursePermissionsEnum("EditSurveys", "Edit Surveys");

    /*
     * Default constructor (needed for gwt serialization)
     */
    private SharedCoursePermissionsEnum() {
    }

    /**
     * Constructor.
     * 
     * @param name the enum value.
     * @param displayName the enum display name.
     */
    private SharedCoursePermissionsEnum(String name, String displayName) {
        super(index++, name, displayName);
        enumList.add(this);
    }

    /**
     * Return the enumeration object that has the matching name.
     * 
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching name is not found.
     */
    public static SharedCoursePermissionsEnum valueOf(String name) throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * 
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching value is not found.
     */
    public static SharedCoursePermissionsEnum valueOf(int value) throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * 
     * @return a List of the currently defined enumerations.
     */
    public static final List<SharedCoursePermissionsEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof SharedCoursePermissionsEnum) {
            SharedCoursePermissionsEnum otherEnum = (SharedCoursePermissionsEnum) other;
            return this.getName().equals(otherEnum.getName()) && this.getDisplayName().equals(otherEnum.getDisplayName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getValue();
    }
}
