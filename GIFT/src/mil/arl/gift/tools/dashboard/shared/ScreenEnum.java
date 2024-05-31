/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * The screen enum class defines the various 'screens' that a user can be on within the dashboard
 * This enum can be used to help maintain the state of the user on the server and restore the state on the client.
 *
 * @author nblomberg
 *
 */
public enum ScreenEnum implements IsSerializable {

    LOGIN,
    MYCOURSES,
    LEARNER_PROFILE,
    COURSE_CREATOR,
    MY_RESEARCH,
    COURSE_RUNTIME,
    PAGELOAD_ERROR,
    LTI_CONSUMER_START_PAGE,
    LTI_CONSUMER_END_PAGE,
    DASHBOARD_ERROR_PAGE,
    LTI_COURSE_RUNTIME,
    GAME_MASTER_ACTIVE,
    GAME_MASTER_PAST,
    WEB_MONITOR_STATUS,
    WEB_MONITOR_MESSAGE,
    INVALID,
    COURSE_COLLECTION,
    EXPERIMENT_WELCOME_PAGE,
    EXPERIMENT_RUNTIME
}
