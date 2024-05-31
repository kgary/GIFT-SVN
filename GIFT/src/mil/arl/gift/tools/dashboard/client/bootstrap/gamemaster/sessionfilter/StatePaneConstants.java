/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * The constant variables used in the {@link StatePane}. This is here to reduce
 * the size of the {@link StatePane} file.
 * 
 * @author sharrison
 */
public class StatePaneConstants {
    /** The session details: Scenario name */
    public static final String SCENARIO_NAME = "Name: ";

    /** Mission data: source */
    public static final String SOURCE = "Source: ";

    /** Mission data: source */
    public static final String MET = "MET: ";

    /** Mission data: source */
    public static final String TASK = "Task: ";

    /** Mission data: source */
    public static final String SITUATION = "Situation: ";

    /** Mission data: source */
    public static final String GOALS = "Goals: ";

    /** Mission data: source */
    public static final String CONDITION = "Condition: ";

    /** Mission data: source */
    public static final String ROE = "ROE: ";

    /** Mission data: source */
    public static final String THREAT_WARNING = "Threat Warning: ";

    /** Mission data: source */
    public static final String WEAPON_STATUS = "Weapon Status: ";

    /** Mission data: source */
    public static final String WEAPON_POSTURE = "Weapon Posture: ";

    /** The session details: Host username */
    public static final String HOST_USERNAME = "Host: ";

    /** The session details: Course ID parameter */
    public static final String SESSION_COURSE_ID = "Course ID: ";

    /** The session details: Source Course ID parameter */
    public static final String SESSION_SOURCE_COURSE_ID = "Source Course ID: ";

    /** The session details: Team participants parameter */
    public static final String SESSION_PARTICIPANTS = "Team Roster: ";

    /** The session details: Host session ID parameter */
    public static final String SESSION_HOST_SESSION_ID = "Host session ID: ";

    /**
     * The session details: time info for session (start and end - if provided)
     */
    public static final String TIME = "Time: ";

    /**
     * The session details: duration of the session (if end time is provided)
     */
    public static final String DURATION = "Duration: ";

    /**
     * The session details: domain session log (optional - only for past session
     * playback)
     */
    public static final String SESSION_LOG_FILE = "Log: ";

    /** The session details: description */
    public static final String SESSION_DESCRIPTION = "Description: ";

    /**
     * Date format used to show the full date (i.e. [HOUR]:[MINUTE] [MONTH]
     * [DAY], [YEAR])
     */
    public static final DateTimeFormat FULL_DATE_FORMAT = DateTimeFormat.getFormat("HH:mm MMMM d, yyyy");

    /** The base "EM" indent for the first tier in the list */
    public static final double BASE_INDENT = 1.2;
}