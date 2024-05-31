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
 * Enumeration of the various module types.
 * 
 * @author mhoffman
 *
 */
public class ModuleTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<ModuleTypeEnum> enumList = new ArrayList<ModuleTypeEnum>(9);
    private static int index = 0;

    public static final ModuleTypeEnum UMS_MODULE		= new ModuleTypeEnum("UMS_Module", "UMS Module");
    public static final ModuleTypeEnum LMS_MODULE		= new ModuleTypeEnum("LMS_Module", "LMS Module");
    public static final ModuleTypeEnum TUTOR_MODULE		= new ModuleTypeEnum("Tutor_Module", "Tutor Module");
    public static final ModuleTypeEnum LEARNER_MODULE	= new ModuleTypeEnum("Learner_Module", "Learner Module");
    public static final ModuleTypeEnum PEDAGOGICAL_MODULE = new ModuleTypeEnum("Pedagogical_Module", "Pedagogical Module");
    public static final ModuleTypeEnum SENSOR_MODULE	= new ModuleTypeEnum("Sensor_Module", "Sensor Module");
    public static final ModuleTypeEnum GATEWAY_MODULE   = new ModuleTypeEnum("Gateway_Module", "Gateway Module");
    public static final ModuleTypeEnum DOMAIN_MODULE    = new ModuleTypeEnum("Domain_Module", "Domain Module");
    public static final ModuleTypeEnum MONITOR_MODULE   = new ModuleTypeEnum("Monitor_Module", "Monitor Module");

    /**
     * Default constructor required for serialization
     */
    private ModuleTypeEnum() {
        
    }
    
    private ModuleTypeEnum(String name, String displayName){
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
    public static ModuleTypeEnum valueOf(String name)
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
    public static ModuleTypeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<ModuleTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }

}
