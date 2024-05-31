/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.shared;

import java.io.Serializable;

import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.ta.util.TeamEchelonUtility;

/**
 * Represents a SIDC and provides methods for modifying it safely and correctly.
 *
 * @author tflowers
 *
 */
public class SIDC implements Serializable {

    /** Default serial version ID */
    private static final long serialVersionUID = 1L;

    /** The coding scheme of the {@link SIDC} */
    private char codingScheme;

    /** The standard identity of the {@link SIDC} */
    private char standardIdentity;

    /** The battle dimension of the {@link SIDC} */
    private char battleDimension;

    /** The status of the {@link SIDC} */
    private char status;

    /** The function ID of the {@link SIDC} */
    private String functionID;

    /** The modifier of the {@link SIDC} */
    private String modifier;

    /** The country of the {@link SIDC} */
    private String country;

    /** The order of battle of the {@link SIDC} */
    private char orderOfBattle;

    /** No-arg constructor required for GWT serialization */
    private SIDC() {
    }

    /**
     * Constructs a new {@link SIDC} from an existing value.
     *
     * @param initialValue The value to initialize the new {@link SIDC} with.
     *        Can't be null and must be 15 characters long.
     */
    public SIDC(String initialValue) {
        this();
        if (initialValue == null) {
            throw new IllegalArgumentException("The parameter 'initialValue' cannot be null.");
        } else if (initialValue.length() != 15) {
            throw new IllegalArgumentException("The paramater 'initialValue' must have a length of 15.");
        }

        setCodingScheme(initialValue.charAt(0));
        setStandardIdentity(initialValue.charAt(1));
        setBattleDimension(initialValue.charAt(2));
        setStatus(initialValue.charAt(3));
        setFunctionID(initialValue.substring(4, 10));
        setModifier(initialValue.substring(10, 12));
        setCountry(initialValue.substring(12, 14));
        setOrderOfBattle(initialValue.charAt(14));
    }

    /**
     * Getter for the coding scheme field of the {@link SIDC}.
     *
     * @return The value of {@link #codingScheme}.
     */
    public char getCodingScheme() {
        return codingScheme;
    }

    /**
     * Setter for the coding scheme of the {@link SIDC}.
     *
     * @param codingScheme The new value of {@link #codingScheme}
     */
    public void setCodingScheme(char codingScheme) {
        this.codingScheme = Character.toUpperCase(codingScheme);
    }

    /**
     * Getter for the standard identity of the {@link SIDC}.
     *
     * @return The value of {@link #standardIdentity}.
     */
    public char getStandardIdentity() {
        return standardIdentity;
    }

    /**
     * Setter for the standard identity of the {@link SIDC}.
     *
     * @param standardIdentity The new value of {@link #standardIdentity}
     */
    public void setStandardIdentity(char standardIdentity) {
        this.standardIdentity = Character.toUpperCase(standardIdentity);
    }

    /**
     * Getter for the battle dimension of the {@link SIDC}.
     *
     * @return The value of {@link #battleDimension}.
     */
    public char getBattleDimension() {
        return battleDimension;
    }

    /**
     * Setter for the battle dimension of the {@link SIDC}.
     *
     * @param battleDimension The new value of {@link #battleDimension}
     */
    public void setBattleDimension(char battleDimension) {
        this.battleDimension = Character.toUpperCase(battleDimension);
    }

    /**
     * Getter for the status of the {@link SIDC}.
     *
     * @return The value of {@link #status}.
     */
    public char getStatus() {
        return status;
    }

    /**
     * Setter for the status of the {@link SIDC}.
     *
     * @param status The new value of {@link #status}
     */
    public void setStatus(char status) {
        this.status = Character.toUpperCase(status);
    }

    /**
     * Getter for the function ID of the {@link SIDC}.
     *
     * @return The value of {@link #functionID}. Can't be null. Will always have
     *         length of 6.
     */
    public String getFunctionID() {
        return functionID;
    }

    /**
     * Setter for the function ID of the {@link SIDC}.
     *
     * @param functionID The new value of {@link #functionID}. Can't be null.
     *        Will always have length of 6.
     */
    public void setFunctionID(String functionID) {
        if (functionID == null) {
            throw new IllegalArgumentException("The parameter 'functionID' cannot be null.");
        } else if (functionID.length() != 6) {
            throw new IllegalArgumentException("The parameter 'functionID' must be 6 characters long");
        }

        this.functionID = functionID.toUpperCase();
    }

    /**
     * Getter for the modifier of the {@link SIDC}.
     *
     * @return The value of {@link #modifier}. Can't be null. Will always have
     *         length of 2.
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * Setter for the modifier of the {@link SIDC}.
     *
     * @param modifier The new value of {@link #modifier}. Can't be null. Will
     *        always have length of 2.
     */
    public void setModifier(String modifier) {
        if (modifier == null) {
            throw new IllegalArgumentException("The parameter 'modifier' cannot be null.");
        } else if (modifier.length() != 2) {
            throw new IllegalArgumentException("The parameter 'modifier' must be 2 characters long");
        }

        this.modifier = modifier.toUpperCase();
    }

    /**
     * Getter for the country of the {@link SIDC}.
     *
     * @return The value of {@link #country}. Can't be null. Will always have
     *         length of 2.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Setter for the country of the {@link SIDC}.
     *
     * @param country The new value of {@link #country}. Can't be null. Will
     *        always have length of 2.
     */
    public void setCountry(String country) {
        if (country == null) {
            throw new IllegalArgumentException("The parameter 'country' cannot be null.");
        } else if (country.length() != 2) {
            throw new IllegalArgumentException("The parameter 'country' must be 2 characters long");
        }
        this.country = country.toUpperCase();
    }

    /**
     * Getter for the order of battle of the {@link SIDC}.
     *
     * @return The value of {@link #orderOfBattle}.
     */
    public char getOrderOfBattle() {
        return orderOfBattle;
    }

    /**
     * Setter for the order of battle of the {@link SIDC}.
     *
     * @param orderOfBattle The new value of {@link #orderOfBattle}
     */
    public void setOrderOfBattle(char orderOfBattle) {
        this.orderOfBattle = Character.toUpperCase(orderOfBattle);
    }

    @Override
    public String toString() {
        return new StringBuilder(12)
                .append(codingScheme)
                .append(standardIdentity)
                .append(battleDimension)
                .append(status)
                .append(functionID)
                .append(modifier)
                .append(country)
                .append(orderOfBattle)
                .toString();
    }

    /**
     * Sets the modifier to the value that corresponds to the echelon
     * 
     * @param echelon the echelon to set the modifier to. No-op if null.
     */
    public void setEchelon(EchelonEnum echelon) {
        if (echelon != null) {

            String newModifier = TeamEchelonUtility.setSIDCModifierForEchelon(getModifier(), echelon);
            setModifier(newModifier);
        }
    }
}
