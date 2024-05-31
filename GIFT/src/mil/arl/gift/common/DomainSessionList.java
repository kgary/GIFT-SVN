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
 * Contains a list of domain sessions.
 * 
 * @author mhoffman
 *
 */
public class DomainSessionList {

    /** container of domain sessions */
    private List<DomainSession> list;

    /**
     * Class constructor
     *
     * @param list the domain sessions
     */
    public DomainSessionList(List<DomainSession> list) {
        this.list = list;
    }
    
    /**
     * Class constructor - no domain sessions
     */
    public DomainSessionList(){
        list = new ArrayList<>(0);
    }

    /**
     * Return the list of domain sessions
     *
     * @return domain sessions
     */
    public List<DomainSession> getDomainSessions() {
        return list;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[DomainSessionList: ");

        sb.append(" domains = {");
        for (DomainSession domain : getDomainSessions()) {
            sb.append(domain).append(", ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }
}
