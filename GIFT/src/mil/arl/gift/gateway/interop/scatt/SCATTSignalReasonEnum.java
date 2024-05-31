/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.scatt;

/**
 * Enumeration of signal reason types sent by the SCATT marksmanship training
 * application.
 * 
 * @author ohasan
 */
public enum SCATTSignalReasonEnum {

    START_AIMING(1), START_INTERVAL(2), STOP_INTERVAL(3), STOP_AIMING(4), STOP_SESSION(
            5);

    private int value;

    private SCATTSignalReasonEnum(int value) {

        this.value = value;
    }

    /**
     * Returns the SCATTSignalReasonEnum object associated with the specified
     * value.
     * 
     * @param ordinal the value associated with the SCATTSignalReasonEnum to
     *            find.
     * @return the SCATTSignalReasonEnum object associated with the specified
     *         value.
     */
    public static SCATTSignalReasonEnum getByValue(int ordinal) {

        if (ordinal < 0 || ordinal > SCATTSignalReasonEnum.values().length) {

            throw new IndexOutOfBoundsException("Invalid ordinal");
        }

        return SCATTSignalReasonEnum.values()[ordinal];
    }

    /**
     * Returns the value associated with this SCATTSignalReasonEnum object.
     * 
     * @return the value associated with this SCATTSignalReasonEnum object
     */
    public int getValue() {

        return value;
    }
}
