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
 * Enum for the ranks of commanders in the U.S. Army
 * 
 * @author cpadilla
 *
 */
public class CommanderRank extends AbstractEnum {

    /** The serial version id */
    private static final long serialVersionUID = 1L;
    
    /** List of enumeration values */
    private static List<CommanderRank> enumList = new ArrayList<CommanderRank>();
    
    /** Index of the enum in this class'es list of enumerations */
    private static int index = 0;

    /** Staff Sergeant */
    public static final CommanderRank STAFF_SGT = new CommanderRank("StaffSgt", "Staff Sgt");
    
    /** Sergeant */
    public static final CommanderRank SGT = new CommanderRank("Sgt", "Sgt");
    
    /** Lieutenant */
    public static final CommanderRank LIEUTENANT = new CommanderRank("Lieutenant", "Lieutenant");
    
    /** Captain */
    public static final CommanderRank CAPTAIN = new CommanderRank("Captain", "Captain");
    
    /** Lieutenant Colonel */
    public static final CommanderRank LT_COLONEL = new CommanderRank("LtColonel", "Lt. Colonel");

    /** Colonel */
    public static final CommanderRank COLONEL = new CommanderRank("Colonel", "Colonel");

    /** Major General */
    public static final CommanderRank MAJOR_GENERAL = new CommanderRank("MajorGeneral", "Major General");

    /** Lieutenant General */
    public static final CommanderRank LT_GENERAL = new CommanderRank("LtGeneral", "Lt. General");

    /** General */
    public static final CommanderRank GENERAL = new CommanderRank("General", "General");

    /**
     * Constructor
     */
    public CommanderRank() {
        super();
    }

    /**
     * Constructor
     * 
     * @param name the name of the enum
     * @param displayName the name to display
     */
    public CommanderRank(String name, String displayName) {
    	super(index++, name, displayName);
    	enumList.add(this);
    }

}
