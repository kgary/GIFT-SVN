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

/**
 * Enums for the Echelon level of a team unit
 *
 * @author cpadilla
 *
 */
public class EchelonEnum extends AbstractEnum {

    /** The serial version id */
    private static final long serialVersionUID = 1L;

    /** List of enumeration values */
    private static List<EchelonEnum> enumList = new ArrayList<>();

    /** Index of the enum in this class'es list of enumerations */
    private static int index = 0;

    /**
     * Division - consists of 3 or more Brigades
     */
    public static final EchelonEnum DIVISION = new EchelonEnum("Division", "Division", MilitaryBranch.ARMY, 7,
            "3 or more Brigades", CommanderRank.MAJOR_GENERAL);

    /**
     * Brigade - consists of 2-5 Battalions
     */
    public static final EchelonEnum BRIGADE = new EchelonEnum("Brigade", "Brigade", MilitaryBranch.ARMY, 6,
            "2-5 Battalions", CommanderRank.COLONEL);

    /**
     * Battalion - consists of 4-6 Companies
     */
    public static final EchelonEnum BATTALION = new EchelonEnum("Battalion", "Battalion", MilitaryBranch.ARMY, 5,
            "4-6 Companies", CommanderRank.LT_COLONEL);

    /**
     * Company - consists of 100-200 Soldiers in 3-5 Platoons
     */
    public static final EchelonEnum COMPANY = new EchelonEnum("Company", "Company", MilitaryBranch.ARMY, 4,
            "100-200 Soldiers in 3-5 Platoons", CommanderRank.CAPTAIN);

    /**
     * Platoon - consists of 16-40 Soldiers in 2 or more Squads
     */
    public static final EchelonEnum PLATOON = new EchelonEnum("Platoon", "Platoon", MilitaryBranch.ARMY, 3,
            "16-40 Soldiers in 2 or more Squads", CommanderRank.LIEUTENANT);

    /**
     * Squad - consists of 4-10 Soldiers
     */
    public static final EchelonEnum SQUAD = new EchelonEnum("Squad", "Squad", MilitaryBranch.ARMY, 2,
            "4-10 Soldiers", CommanderRank.STAFF_SGT, CommanderRank.SGT);

    /**
     * Fireteam - consists of 2-4 Soldiers
     */
    public static final EchelonEnum FIRETEAM = new EchelonEnum("Fireteam", "Fireteam", MilitaryBranch.ARMY, 1,
            "4 Soldiers", CommanderRank.STAFF_SGT);

    /** A description of the components that make up the echelon */
    private String components;

    /** A list of Commander's Ranks that can command the echelon */
    private CommanderRank[] commandersRank;

    /** The branch of the military this echelon belongs to */
    private MilitaryBranch branch;

    /** The level of the echelon, with the smallest unit starting at 1 */
    private int echelonLevel;

    /**
     * Constructor
     */
    private EchelonEnum() {
        super();
    }

    /**
     * Constructor
     *
     * @param name the name of the echelon
     * @param displayName the display name of the echelon
     * @param branch the branch of the military this echelon belongs to
     * @param echelonLevel the level of the echelon, with the smallest unit starting at 1
     * @param components a description of the components that make up the echelon
     * @param commandersRank the list of commander's ranks that can command this echelon
     */
    private EchelonEnum(String name, String displayName, MilitaryBranch branch, int echelonLevel, String components, CommanderRank... commandersRank) {
        super(index++, name, displayName);
    	enumList.add(this);
    	this.components = components;
    	this.commandersRank = commandersRank;
    	this.branch = branch;
    	this.echelonLevel = echelonLevel;
    }

    /**
     * Gets the Echelon level with the given ordinal value within this
     * enumeration
     *
     * @param enumValue the ordinal value of the Echelon level to get
     * @return the Echelon value corresponding to the provided ordinal value.
     *         Can be null if the provided value is not a valid enum value.
     */
    public static EchelonEnum valueOf(int enumValue) {
        if (enumValue < 0 || enumValue >= enumList.size()) {
            return null;
        }

        return AbstractEnum.valueOf(enumValue, enumList);
    }

    /**
     * Gets the components of this echelon
     *
     * @return the components
     */
    public String getComponents() {
        return components;
    }

    /**
     * Gets the commander's ranks that can command this echelon
     *
     * @return the commandersRank
     */
    public CommanderRank[] getCommandersRank() {
        return commandersRank;
    }

    /**
     * The branch of the military this echelon belongs to
     *
     * @return the branch
     */
    public MilitaryBranch getBranch() {
        return branch;
    }

    /**
     * The level of the echelon, with the smallest unit starting at 1
     *
     * @return the echelonLevel
     */
    public int getEchelonLevel() {
        return echelonLevel;
    }

    /**
     * The list of values of this enum type
     *
     * @return the enumList
     */
    public static List<EchelonEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }

}
