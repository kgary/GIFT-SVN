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
 * This class contains the various enumerated shared permissions for a user about a published course by a GIFT user.
 * 
 * @author sharrison
 *
 */
public class SharedPublishedCoursePermissionsEnum extends AbstractEnum implements Serializable {

    private static final long serialVersionUID = 1L;

    private static List<SharedPublishedCoursePermissionsEnum> enumList = new ArrayList<SharedPublishedCoursePermissionsEnum>(2);
    private static int index = 0;

    public static final SharedPublishedCoursePermissionsEnum OWNER = new SharedPublishedCoursePermissionsEnum("Ownder", "Owner");
    public static final SharedPublishedCoursePermissionsEnum MANAGER = new SharedPublishedCoursePermissionsEnum("Manager", "Manager");
    public static final SharedPublishedCoursePermissionsEnum RESEARCHER = new SharedPublishedCoursePermissionsEnum("Researcher", "Researcher");

    /*
     * Default constructor (needed for gwt serialization)
     */
    private SharedPublishedCoursePermissionsEnum() {
    }

    /**
     * Constructor.
     * 
     * @param name the enum value.
     * @param displayName the enum display name.
     */
    private SharedPublishedCoursePermissionsEnum(String name, String displayName) {
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
    public static SharedPublishedCoursePermissionsEnum valueOf(String name) throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * 
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching value is not found.
     */
    public static SharedPublishedCoursePermissionsEnum valueOf(int value) throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * 
     * @return a List of the currently defined enumerations.
     */
    public static final List<SharedPublishedCoursePermissionsEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof SharedPublishedCoursePermissionsEnum) {
            SharedPublishedCoursePermissionsEnum otherEnum = (SharedPublishedCoursePermissionsEnum) other;
            return this.getName().equals(otherEnum.getName()) && this.getDisplayName().equals(otherEnum.getDisplayName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getValue();
    }
}
