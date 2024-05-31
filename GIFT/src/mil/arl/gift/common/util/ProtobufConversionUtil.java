/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.Date;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

/**
 * Helper util class for commonly used conversions
 * 
 * @author sharrison
 */
public class ProtobufConversionUtil {
    /**
     * Converts the time in milliseconds to the protobuf Timestamp object.
     *
     * @param timeInMillis the time in milliseconds.
     * @return the protobuf Timestamp object.
     */
    public static Timestamp convertMillisToTimestamp(long timeInMillis) {
        return Timestamps.fromMillis(timeInMillis);
    }

    /**
     * Converts the time to the protobuf Timestamp object.
     *
     * @param date the time.
     * @return the protobuf Timestamp object.
     */
    public static Timestamp convertDateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }

        return convertMillisToTimestamp(date.getTime());
    }

    /**
     * Converts the protobuf Timestamp object to time in milliseconds.
     *
     * @param timestamp the protobuf Timestamp object. Can't be null.
     * @return timeInMillis the time in milliseconds.
     */
    public static Long convertTimestampToMillis(Timestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("The parameter 'timestamp' cannot be null.");
        }

        return timestamp.getSeconds() * 1000 + timestamp.getNanos() / 1000000;
    }
}
