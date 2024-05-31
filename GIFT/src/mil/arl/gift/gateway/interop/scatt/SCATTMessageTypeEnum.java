/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.scatt;

/**
 * Enumeration of message types sent by the SCATT marksmanship training
 * application.
 * 
 * @author ohasan
 */
public enum SCATTMessageTypeEnum {

    REGISTER(0), STATE(1), POINT(2), SHOT(3);

    private int value;

    private SCATTMessageTypeEnum(int value) {

        this.value = value;
    }

    /**
     * Returns the SCATTMessageTypeEnum object associated with the specified
     * value.
     * 
     * @param ordinal the value associated with the SCATTMessageTypeEnum to
     *            find.
     * @return the SCATTMessageTypeEnum object associated with the specified
     *         value.
     */
    public static SCATTMessageTypeEnum getByValue(int ordinal) {

        if (ordinal < 0 || ordinal > SCATTMessageTypeEnum.values().length) {

            throw new IndexOutOfBoundsException("Invalid ordinal");
        }

        return SCATTMessageTypeEnum.values()[ordinal];
    }

    /**
     * Returns the value associated with this SCATTMessaegTypeEnum object.
     * 
     * @return the value associated with this SCATTMessaegTypeEnum object
     */
    public int getValue() {

        return value;
    }
}
