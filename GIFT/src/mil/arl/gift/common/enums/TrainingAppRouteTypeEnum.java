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
 * A enumeration identifying the different destinations that training application messages can be routed to. These enumerations are used
 * to preserve training application load arguments as the correct type after serialization, since the types cannot be inferred after
 * serialization.
 * 
 * @author nroberts
 */
public class TrainingAppRouteTypeEnum extends AbstractEnum {

	private static final long serialVersionUID = 1L;
	
	private static List<TrainingAppRouteTypeEnum> enumList = new ArrayList<TrainingAppRouteTypeEnum>(2);
	private static int index = 0;
	
	/** A type representing embedded web applications */
	public static final TrainingAppRouteTypeEnum EMBEDDED = new TrainingAppRouteTypeEnum("Embedded", "Embedded");
	
	/** A type representing desktop training applications */
	public static final TrainingAppRouteTypeEnum INTEROP = new TrainingAppRouteTypeEnum("Interop", "Interop");
	
	/**
	 * Creates a new enumeration with the given name and display name
	 * 
	 * @param name the name to use for the enumeration
	 * @param displayName the display name to use for the enumeration
	 */
	private TrainingAppRouteTypeEnum(String name, String displayName) {
		super(index++, name, displayName);
		enumList.add(this);
	}
	
	/**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
	public static TrainingAppRouteTypeEnum valueOf(int index) throws EnumerationNotFoundException {
		return AbstractEnum.valueOf(index, VALUES());
	}
	
	/**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
	public static TrainingAppRouteTypeEnum valueOf(String name) throws EnumerationNotFoundException {
		return AbstractEnum.valueOf(name, VALUES());
	}

	 /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
	public static List<TrainingAppRouteTypeEnum> VALUES() {
		return Collections.unmodifiableList(enumList);
	}
	
}
