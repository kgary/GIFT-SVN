/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a list of user sessions.
 * 
 * @author mhoffman
 *
 */
public class UserSessionList {

    /** container of user sessions */
    private List<UserSession> list;

    /**
     * Class constructor
     *
     * @param list the user sessions
     */
    public UserSessionList(List<UserSession> list) {
        this.list = list;
    }
    
    /**
     * Class constructor - no user sessions
     */
    public UserSessionList(){
        list = new ArrayList<>(0);
    }

    /**
     * Return the list of user sessions
     *
     * @return List<UserSession>
     */
    public List<UserSession> getUserSessions() {
        return list;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[UserSessionList: ");

        sb.append(" users = {");
        for (UserSession domain : getUserSessions()) {
            sb.append(domain).append(", ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }
}
