/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Enums for the branches of the military
 * 
 * @author cpadilla
 *
 */
public class MilitaryBranch extends AbstractEnum {

    /** The serial version id */
    private static final long serialVersionUID = 1L;
    
    /** List of enumeration values */
    private static List<MilitaryBranch> enumList = new ArrayList<MilitaryBranch>();
    
    /** Index of the enum in this class'es list of enumerations */
    private static int index = 0;

    /** The Army */
    public static final MilitaryBranch ARMY = new MilitaryBranch("Army", "Army");

    /**
     * Constructor
     */
    public MilitaryBranch() {
        super();
    }

    /**
     * Constructor
     * 
     * @param name the name of the branch of the military
     * @param displayName the display name of the branch of the military
     */
    public MilitaryBranch(String name, String displayName) {
        super(index++, name, displayName);
    	enumList.add(this);
    }

}
