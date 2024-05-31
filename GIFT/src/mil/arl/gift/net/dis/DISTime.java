/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.dis;

/**
 * This class contains helper methods for converting time into DIS PDUs related fields
 * for time (e.g. hours, time past the hour fields).
 * 
 * NOTE: the DIS timestamp is represented by a 32-bit unsigned integer
 * representing time past the hour, with the least significant bit used as an
 * absolute/relative flag. The remaining 31 bits are used to represent one hour,
 * so the DIS timestamp rolls over at the end of each hour. There is no
 * representation of anything but "time past the hour" in the DIS timestamp.
 * 
 * NOTE: Each unit of DIS Time is approximately 1.676 microseconds.
 * 
 * @author mhoffman
 *
 */
public class DISTime {
    
    /**
     * Constant indicating the number of milliseconds in an hour.
     */
    final static long MILLISECONDS_PER_HOUR = 3600000;
    
    /**
     * Multiplier used to convert a 31 bit representation of milliseconds to a
     * 32 bit value.
     */
    private final static double DIS_TIME_UNIT_SCALE;

    /**
     * Constant reprenting the maximum value represented by 31 bits. This value
     * is equal to (2^31 - 1) and represents one hour of time based on the DIS
     * scaling.
     */
    private final static long MAX_31_BIT_VALUE = 0x7fffffff;
    
    static {

        DIS_TIME_UNIT_SCALE =
            ((double) MAX_31_BIT_VALUE / (double) MILLISECONDS_PER_HOUR);
        
    }

    /**
     * Returns the number of hours since the epoch for the specified UTC time.
     * 
     * @param time UTC time in milliseconds
     * @return the number of hours since the epoch for the specified UTC time
     */
    public static int getHoursSinceEpochFromUTC(long time) {

        return ((int) (time / MILLISECONDS_PER_HOUR));
    }
    
    /**
     * Returns the number of milliseconds since the last hour for the specified
     * UTC time.
     * 
     * @param time UTC time in milliseconds
     * @return the number of milliseconds since the last hour for the specified
     *         UTC time
     */
    public static int getMillisSinceLastHourFromUTC(long time) {

        return ((int) (time % MILLISECONDS_PER_HOUR));
    }
    
    /**
     * Converts the specified UTC time (long = milliseconds since midnight Jan
     * 1, 1970) into a DIS Timestamp. Since DIS is only interested in time < 1
     * hour, any time >= 1 hour is lost.
     * 
     * @param utcTime Description of the Parameter
     * @return the DIS timestamp value corresponding to the specified UTC
     */
    public static long convertUTCTimeToDISTimestamp(long utcTime) {

        long timestamp;

        // convert the UTC time to DIS time units (31 bits)
        long timeUnitsSinceLastHour = convertUTCTimeToDISTimeUnits(utcTime);

        // shift the DIS time value left by one place and set the least
        // significant bit corresponding to the realtive/absolute flag
//        if (useRelativeTimestamp) {
//
//            timestamp = timeUnitsSinceLastHour << 1;
//            // Least significant bit = 0
//        } else {
            timestamp = (timeUnitsSinceLastHour << 1) + 1;
            // Least significant bit = 1
//        }

        return timestamp;
    }
    
    /**
     * Converts a UTC time (long = milliseconds since midnight Jan 1, 1970) into
     * DIS Time Units. Since DIS is only interested in time < 1 hour, any time >
     * = 1 hour is stripped off.
     * 
     * @param timeInMillis time in UTC format
     * @return a long value of time since the last hour tick in DIS time units
     */
    public static long convertUTCTimeToDISTimeUnits(long timeInMillis) {

        long millisSinceLastHour;
        double timeUnitsSinceLastHour;

        // strip out time > 1 hour
        millisSinceLastHour = (timeInMillis % MILLISECONDS_PER_HOUR);

        // convert the UTC time to DIS time units
        timeUnitsSinceLastHour = millisSinceLastHour * DIS_TIME_UNIT_SCALE;

        return StrictMath.round(timeUnitsSinceLastHour);
    }

    private DISTime() {
    }
}
