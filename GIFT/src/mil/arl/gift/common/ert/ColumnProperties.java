/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;
import java.text.ParseException;

/**
 * An abstract class for representing the properties of a column
 *
 * @author jleonard
 */
public class ColumnProperties implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * No arg-constructor required by GWT for serialization
	 */
	/*
	 * Nick - 9/22/2015: Originally in the ERT package, this class was abstract and, consequently, did not need a constructor; however,
	 * upon moving this class to the common package, compile errors began occurring in the Dashboard whenever a class using 
	 * ColumnProperties was used in an RPC method. Eventually, while looking for a solution, I came across a thread 
	 * (https://code.google.com/p/google-web-toolkit/issues/detail?id=8588) mentioning that there were problems with passing abstract 
	 * classes through RPC calls, so we decided to use a protected constructor instead of an abstract class to circumvent the issue.
	 */
	protected ColumnProperties(){
		
	}

	/**
     * Parses a string representing a time in to milliseconds
     *
     * Examples of input this method will take is '1h', '30m20s', '2h30s',
     * '20s', '1m30ms'
     *
     * @param timeString The string representation of time
     * @return long The amount of milliseconds the string represents
     * @throws ParseException - thrown if there is an issue parsing the time string
     */
    public static long parseTime(String timeString) throws ParseException {

        long time = 0;

        boolean done = false;

        int index = 0;

        while (!done) {

            if (index >= timeString.length()) {

                done = true;

            } else {

                int startIndex = index;

                char number = timeString.charAt(index);

                if (number >= '0' && number <= '9') {

                    index += 1;

                    if (index >= timeString.length()) {

                        throw new ParseException("Reached end of string, expecting number or 'h', 'm', or 's'", index);
                    }

                    number = timeString.charAt(index);

                    while (number >= '0' && number <= '9') {

                        index += 1;

                        if (index >= timeString.length()) {

                            throw new ParseException("Reached end of string, expecting number or 'h', 'm', or 's'", index);
                        }

                        number = timeString.charAt(index);
                    }

                    long value = 0;

                    for (int i = 0; i < (index - startIndex); i += 1) {

                        value += (timeString.charAt(index - i - 1) - '0') * Math.pow(10, i);
                    }

                    if (index >= timeString.length()) {

                        throw new ParseException("Reached end of string, expecting 'h', 'm', or 's'", index);
                    }

                    char timeUnit = timeString.charAt(index);

                    if (timeUnit == 'H' || timeUnit == 'h') {

                        time += (value * 60 * 60) * 1000;

                    } else if (timeUnit == 'M' || timeUnit == 'm') {

                        boolean isMilliseconds = false;

                        if (index + 1 < timeString.length()) {

                            timeUnit = timeString.charAt(index + 1);

                            if (timeUnit == 'S' || timeUnit == 's') {

                                index += 1;

                                isMilliseconds = true;
                            }
                        }

                        if (isMilliseconds) {

                            time += value;

                        } else {

                            time += (value * 60) * 1000;
                        }

                    } else if (timeUnit == 'S' || timeUnit == 's') {

                        time += value * 1000;

                    } else {

                        throw new ParseException("Illegal character, expecting 'h', 'm', or 's': " + timeUnit, index);
                    }

                    index += 1;

                } else {

                    throw new ParseException("Illegal character, expecting number: " + number, index);
                }
            }
        }

        return time;
    }

    /**
     * Returns the string representation of a time, in milliseconds
     *
     * Examples of what is returned from this method is 360000 -> '1h', 6000 ->
     * '1m', 12200 > '2m2s', 362000 -> '1h20s'
     *
     * @param time The time, in milliseconds, to convert in to a string
     * @return String The string representation of the time
     */
    public static String convertTimeToString(long time) {

        long timeInSeconds = time / 1000;

        long hours = timeInSeconds / 3600;

        long remainder = timeInSeconds % 3600;

        long minutes = remainder / 60;

        long seconds = remainder % 60;

        long milliseconds = time % 1000;

        String timeString = "";

        if (hours > 0) {

            timeString += hours + "h";
        }

        if (minutes > 0) {

            timeString += minutes + "m";
        }

        if (seconds > 0) {

            timeString += seconds + "s";
        }

        if (milliseconds > 0) {

            timeString += milliseconds + "ms";
        }

        return timeString;
    }
}
