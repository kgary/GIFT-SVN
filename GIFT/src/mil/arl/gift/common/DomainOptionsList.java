/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.List;

/**
 * Contains a list of domain options
 * 
 * @author mhoffman
 *
 */
public class DomainOptionsList{

    /** container of domain options */
    private List<DomainOption> list;

    /**
     * Class constructor
     *
     * @param list the domain options
     */
    public DomainOptionsList(List<DomainOption> list) {
        this.list = list;
    }

    /**
     * Return the list of domain options
     *
     * @return List<DomainOption> domain options
     */
    public List<DomainOption> getOptions() {
        return list;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[DomainOptionsList: ");

        sb.append(" domains = {");
        for (DomainOption domain : getOptions()) {
            sb.append(domain).append(", ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }
}
