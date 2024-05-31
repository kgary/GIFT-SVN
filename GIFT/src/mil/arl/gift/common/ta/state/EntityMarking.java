/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * The entity marking object for an Entity State message.
 *
 * @author acase
 *
 */
public class EntityMarking implements TrainingAppState {

    /** Specifies the ASCII character encoding */
    public static final String ASCII_CHARACTER_SET = "ASCII(1)";

    /**
     * The entity Character Set (e.g. {@link EntityMarking#ASCII_CHARACTER_SET})
     */
    private String entityCharacterSet;

    /** The Entity Marking */
    private String entityMarking;

    /** The user-friendly name to use for the entity within GIFT. */
    private String giftDisplayName;

    /**
     * Class constructor - set attributes
     *
     * @param entityCharacterSet The character encoding to use for the value of
     *        the entity marking (e.g.
     *        {@link EntityMarking#ASCII_CHARACTER_SET}).
     * @param entityMarking The {@link String} value of the entity marking.
     */
    public EntityMarking(String entityCharacterSet, String entityMarking) {
        this.entityCharacterSet = entityCharacterSet;
        this.entityMarking = entityMarking;
    }

    /**
     * Class constructor - set attributes
     *
     * @param entityCharacterSet The {@link String} value representing the
     *        character encoding to use for the value of the entity marking
     *        (e.g. {@link EntityMarking#ASCII_CHARACTER_SET}).
     * @param entityMarking The {@link String} value of the entity marking.
     * @param giftDisplayName The {@link String} value that represents the
     *        user-friendly text that represents the marked entity.
     */
    public EntityMarking(String entityCharacterSet, String entityMarking, String giftDisplayName) {
        this(entityCharacterSet, entityMarking);
        setGiftDisplayName(giftDisplayName);
    }

    /**
     * Creates a shallow copy of this {@link EntityMarking}.
     *
     * @return The copy of this {@link EntityMarking}.
     */
    private EntityMarking shallowCopy() {
        return new EntityMarking(entityCharacterSet, entityMarking);
    }

    /**
     * Getter for the text of the entity marking.
     *
     * @return The value of {@link #entityMarking}.
     */
    public String getEntityMarking() {
        return entityMarking;
    }

    /**
     * Creates a copy of this {@link EntityMarking} with a different value for
     * the {@link #entityMarking}.
     *
     * @param entityMarking The {@link String} value to use for
     *        {@link #entityMarking}.
     * @return The newly created {@link EntityMarking}. Can't be null.
     */
    public EntityMarking replaceEntityMarking(String entityMarking) {
        EntityMarking toRet = shallowCopy();
        toRet.entityMarking = entityMarking;
        return toRet;
    }

    /**
     * Getter for the user-friendly name of the entity.
     *
     * @return The value of {@link #giftDisplayName}. Can be null.
     */
    public String getGiftDisplayName() {
        return giftDisplayName;
    }

    /**
     * Setter for the user-friendly name of the entity.
     *
     * @param giftDisplayName The new value of {@link #giftDisplayName}. Can be
     *        null.
     */
    private void setGiftDisplayName(String giftDisplayName) {
        this.giftDisplayName = giftDisplayName;
    }

    /**
     * Creates a copy of this {@link EntityMarking} with a different value for
     * {@link #giftDisplayName}.
     *
     * @param giftDisplayName The new {@link String} value to use for
     *        {@link #giftDisplayName}.
     * @return The newly created {@link EntityMarking}. Can't be null.
     */
    public EntityMarking replaceGiftDisplayName(String giftDisplayName) {
        EntityMarking toRet = shallowCopy();
        toRet.setGiftDisplayName(giftDisplayName);
        return toRet;
    }

    /**
     * Getter for the encoding used for the entity marking value.
     *
     * @return The value of the {@link #entityCharacterSet}.
     */
    public String getEntityCharacterSet() {
        return entityCharacterSet;
    }

    /**
     * Getter for the numeric value of {@link #entityCharacterSet}.
     *
     * @return The numeric value of specified {@link #entityCharacterSet} or 0
     *         if the encoding is not set.
     */
    public int getEntityCharacterSetConstant() {
        switch (this.entityCharacterSet) {
        case ASCII_CHARACTER_SET:
            return 1;
        default:
            return 0;
        }
    }

    @Override
    public boolean equals(Object otherEntityMarkingObj) {
        if(otherEntityMarkingObj instanceof EntityMarking) {
            return this.entityMarking == ((EntityMarking)otherEntityMarkingObj).getEntityMarking();
        }
        return false;
    }

    @Override
    public int hashCode() {

        int hashCode = 0;

        hashCode |= getEntityCharacterSet().hashCode();
        hashCode |= getEntityMarking().hashCode();

        return hashCode;
    }


    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EntityMarking: ");
        sb.append("Entity Marking = ").append(getEntityMarking());
        sb.append(", Entity Character Set = ").append(getEntityCharacterSet());
        sb.append("]");

        return sb.toString();
    }

}
