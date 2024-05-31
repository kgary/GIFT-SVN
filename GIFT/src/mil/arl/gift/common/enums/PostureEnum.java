/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This class contains the various enumerated posture types
 * 
 * @author mhoffman
 *
 */
public class PostureEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    private static List<PostureEnum> enumList = new ArrayList<>(2);
    private static int index = 0;

    public static final PostureEnum UNUSED = new PostureEnum("Unused", "Unused");
    public static final PostureEnum STANDING = new PostureEnum("Standing", "Standing");
    public static final PostureEnum WALKING = new PostureEnum("Walking", "Walking");
    public static final PostureEnum RUNNING = new PostureEnum("Running", "Running");
    public static final PostureEnum KNEELING = new PostureEnum("Kneeling", "Kneeling");
    public static final PostureEnum PRONE = new PostureEnum("Prone", "Prone");
    public static final PostureEnum CRAWLING = new PostureEnum("Crawling", "Crawling");
    public static final PostureEnum SWIMMING = new PostureEnum("Swimming", "Swimming");
    public static final PostureEnum PARACHUTING = new PostureEnum("Parachuting", "Parachuting");
    public static final PostureEnum JUMPING = new PostureEnum("Jumping", "Jumping");
    public static final PostureEnum SITTING = new PostureEnum("Sitting", "Sitting");
    public static final PostureEnum SQUATTING = new PostureEnum("Squatting", "Squatting");
    public static final PostureEnum CROUCHING = new PostureEnum("Crouching", "Crouching");
    public static final PostureEnum WADING = new PostureEnum("Wading", "Wading");
    public static final PostureEnum SURRENDER = new PostureEnum("Surrender", "Surrender");
    public static final PostureEnum DETAINED = new PostureEnum("Detained", "Detained");
    
    /**
     * default - required for GWT serialization
     */
    private PostureEnum() {
    }
    
    private PostureEnum(String name, String displayName){
        super(index++, name, displayName);
        enumList.add(this);
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static PostureEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static PostureEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<PostureEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
