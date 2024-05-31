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
 * An enum representing the different levels that GIFT's modules can use when presenting training material
 * while a learner is in a lesson. This is mainly used to determine when GIFT should leverage its own
 * Tutor module to present training material or whether an external application should be used instead.
 * 
 * @author nroberts
 */
public class LessonLevelEnum extends AbstractEnum {

    /** default UID */
	private static final long serialVersionUID = 1L;

	/** list of enums of this type, used to keep track of all possible LessonLevelEnum objects  */
	private static List<LessonLevelEnum> enumList = new ArrayList<LessonLevelEnum>(3);
	
	/** starting index of these enums in the list, used when referencing an enum by integer */
	private static int index = 0;
	
	/** A lesson level used to present training material in a browser using the Tutor module */
	public static final LessonLevelEnum COURSE = new LessonLevelEnum("Course", "Course");
	
	/** A lesson level used to present training material using an external application */
	public static final LessonLevelEnum RTA = new LessonLevelEnum("RTA", "Real-Time Assessment");
	
	/**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatibility
     */
	public LessonLevelEnum() {
		super();
	}
		
	private LessonLevelEnum(String name, String displayName) {
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
	public static LessonLevelEnum valueOf(String name) 
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
    public static LessonLevelEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }	
	
	/**
	 * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
	 */
	public static final List<LessonLevelEnum> VALUES() {
		return Collections.unmodifiableList(enumList);
	}
}
