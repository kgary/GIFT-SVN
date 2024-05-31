/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This class contains helper functions for formatting time stamps
 * 
 * @author cdettmering
 */
public class TimeUtil {

    /** instance of the logger */
	//currently not used.
    //private static Logger logger = LoggerFactory.getLogger(TimeUtil.class);
    
    /** Date formatter used for log file names. NOTE: null Locale and Timezone arguments make the formatter use the system defaults. */
    private static final FastDateFormat logFilenameFormat = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss", null, null);
    
    /** Date formatter used for the system log that holds all of the Message events. 
     * NOTE: null Locale and Timezone arguments make the formatter use the system defaults. */
    private static final FastDateFormat systemLogFormat = FastDateFormat.getInstance("yyyy:MM:dd HH:mm:ss:SSS z", null, null);    
    
    /** Date formatter used for showing time followed by day. 
     * NOTE: null Locale and Timezone arguments make the formatter use the system defaults. */
    public static final FastDateFormat timeFirstFormat = FastDateFormat.getInstance("HH:mm:ss:SSS z MM/dd/yyyy", null, null);
    
    /** Date formatter used for showing the date of a learner record */
    public static final FastDateFormat LEARNER_RECORD_DATE_FORMAT = FastDateFormat.getInstance("MM/dd/yyyy HH:mm z", null, null); 
    
    /** Decimal formatter used for formatting relative time. Relative time is formatted as
     * 5 digits to the left of the decimal and 3 digits to the right, with leading and
     * trailing zeros to keep the column a fixed width.
     */
    private static final DecimalFormat relativeTimeFormat = new DecimalFormat("00000.000");
    
    /**
     * Formats epoch time into the date format used in log file names.
     * The format is as follows: yyyy-MM-dd_HH-mm-ss
     * 
     * Example: 1999-01-05_03-57-28
     * 
     * The example reads as: January 5th, 1999 at 3:57:28
     * 
     * 
     * @param epoch Epoch time given by System.currentTimeMillis
     * @return Formatted date string.
     */
    public static String formatTimeLogFilename(long epoch) {
    	return logFilenameFormat.format(epoch);
    }
    
    /**
     * Formats epoch time into the date format used in log file names.
     * The format is as follows: yyyy-MM-dd_HH-mm-ss
     * 
     * Example: 1999-01-05_03-57-28
     * 
     * The example reads as: January 5th, 1999 at 3:57:28
     * 
     * 
     * @param time The time given as a java.util.Date object.
     * @return Formatted date string.
     */
    public static String formatTimeLogFilename(Date time) {
    	return logFilenameFormat.format(time);
    }
    
    /**
     * Formats epoch time into the date format used in the system log.
     * The format is as follows: yyyy:MM:dd HH:mm:ss:SSS z
     * 
     * Example: 1999:01:05 03:57:28:321 EDT
     * 
     * The example reads as: January 5th, 1999 at 3:57:28:321 Eastern Time
     * 
     * 
     * @param epoch Epoch time given by System.currentTimeMillis
     * @return Formatted date string.
     */
    public static String formatTimeSystemLog(long epoch) {
    	return systemLogFormat.format(epoch);
    }
    
    /**
     * Formats epoch time into the date format used in the system log.
     * The format is as follows: yyyy:MM:dd HH:mm:ss:SSS z
     * 
     * Example: 1999:01:05 03:57:28:321 EDT
     * 
     * The example reads as: January 5th, 1999 at 3:57:28:321 Eastern Time
     * 
     * 
     * @param time The time given as a java.util.Date object.
     * @return Formatted date string.
     */
    public static String formatTimeSystemLog(Date time) {
    	return systemLogFormat.format(time);
    }
    
    /**
     * Formats a relative time stamp given a start and end time.
     * The format is given in seconds, with the decimals on the right being milliseconds.
     * The format is as follows: 00000.000
     * 
     * Examples:
     *
     * 00032.543
     * 12396.123
     * 00005.100
     * 
     * @param start The start of the time session, in epoch time.
     * @param end The end of the time session, in epoch time.
     * @return Formatted relative time stamp based on the elapsed time from start to end.
     */
    public static String formatTimeRelative(long start, long end) {
    	double relative = ((double) end - start) / 1000.0;
        return relativeTimeFormat.format(relative);
    }

    /**
     * Formats a relative time stamp given a relative time. The format is given
     * in seconds, with the decimals on the right being milliseconds. The format
     * is as follows: 00000.000
     * 
     * Examples:
     *
     * 00032.543 12396.123 00005.100
     * 
     * @param relativeTime The relative session time.
     * @return Formatted relative time stamp.
     */
    public static String formatTimeRelative(double relativeTime) {
        return relativeTimeFormat.format(relativeTime);
    }

    /**
     * Gets the default relative time stamp, if the relative time cannot be 
     * computed yet. (Time stamps to use when messages come in before a session
     * starts.)
     * 
     * @return "00000.000"
     */
    public static String getDefaultRelativeTime() {
    	return "00000.000";
    }
    
    /**
     * Formats the current date/time.
     * 
     * The format is as follows: yyyy-MM-dd_HH-mm-ss
     * 
     * Example: 1999-01-05_03-57-28
     * 
     * The example reads as: January 5th, 1999 at 3:57:28
     * 
     * @return Formatted date string.
     */
    public static String formatCurrentTime(){
        Date now = new Date();
        return logFilenameFormat.format(now);
    }

    /**
     * Private constructor
     */
    private TimeUtil() {
    }
    
    /**
     * This inner class offers thread safe access to SimpleDateFormat.
     *  
     * @author mhoffman
     *
     */
    public static class ConcurrentDateFormatAccess {
        /** The date format thread */
        private ThreadLocal<DateFormat> df;
        
        /**
         * Create a thread safe SimpleDateFormat instance.
         * 
         * @param format the string format to use in the SimpleDateFormat constructor.
         * @param timeZone the time zone to use for the SimpleDateFormat instance.
         */
        public ConcurrentDateFormatAccess(final String format, final TimeZone timeZone){
            
            df = new ThreadLocal<DateFormat> () {

                @Override
                public DateFormat get() {
                    return super.get();
                }

                @Override
                protected DateFormat initialValue() {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                    simpleDateFormat.setTimeZone(timeZone);
                    return simpleDateFormat;
                }

                @Override
                public void remove() {
                    super.remove();
                }

                @Override
                public void set(DateFormat value) {
                    super.set(value);
                }

            };
        }

        /**
         * Converts the timestamp string to a Date object.
         * 
         * @param dateString the string that should adhere to the format specified
         * to this class instance.
         * @return the corresponding date object
         * @throws ParseException if there was a problem parsing the string according to the
         * format provided.
         */
        public Date convertStringToDate(String dateString) throws ParseException {
            return df.get().parse(dateString);
        }

    }  
    
}
