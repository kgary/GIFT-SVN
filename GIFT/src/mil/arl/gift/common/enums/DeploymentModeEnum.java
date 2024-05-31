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

public class DeploymentModeEnum extends AbstractEnum {

	private static final long serialVersionUID = 1L;

	private static List<DeploymentModeEnum> enumList = new ArrayList<DeploymentModeEnum>(3);
	
	private static int index = 0;
	
	public static final DeploymentModeEnum SERVER = new DeploymentModeEnum("Server", "Server");
	public static final DeploymentModeEnum DESKTOP = new DeploymentModeEnum("Desktop", "Desktop");
	public static final DeploymentModeEnum SIMPLE = new DeploymentModeEnum("Simple", "Simple - No Login Authentication");
	
	/**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatibility
     */
	public DeploymentModeEnum() {
		super();
	}
		
	private DeploymentModeEnum(String name, String displayName) {
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
	public static DeploymentModeEnum valueOf(String name) 
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
    public static DeploymentModeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }	
	
	/**
	 * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
	 */
	public static final List<DeploymentModeEnum> VALUES() {
		return Collections.unmodifiableList(enumList);
	}
}
