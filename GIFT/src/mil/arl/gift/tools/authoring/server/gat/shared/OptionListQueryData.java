/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The parameters of the query for option lists
 *
 * @author jleonard
 */
public class OptionListQueryData implements IsSerializable {
    
    /** Flag indicating if the option lists queried should be shared */
    private boolean showSharedOnly = false;
    
    /** The user name */
    private String userName;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public OptionListQueryData() {
    }
    
    /**
     * Constructor.
     * 
     * @param showSharedOnly true to retrieve only the shared option lists; false to retrieve option
     *            lists regardless of being shared.
     * @param userName filter the queried option lists based on if the list has visible permissions
     *            for this username. Will also return any lists that have visible permissions for
     *            all users.
     */
    public OptionListQueryData(boolean showSharedOnly, String userName) {
        this.userName = userName;
        this.showSharedOnly = showSharedOnly;
    }

    /**
     * Flag indicating if the option lists queried should only be shared.
     * @return true if the the query will only return shared option lists; false otherwise.
     */
    public boolean isShowSharedOnly() {
        
        return showSharedOnly;
    }
    
    /**
     * Sets the flag indicating if the option lists queried should only be shared.
     * @param showSharedOnly true to only retrieve shared option lists; false otherwise.
     */
    public void setShowSharedOnly(boolean showSharedOnly) {
        
        this.showSharedOnly = showSharedOnly;
    }

    /**
     * Retrieves the username used to filter the results down to only the lists that have granted
     * the username visible permissions.
     * 
     * @return the username used to filter.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username used to filter the results down to only the lists that have granted the
     * username visible permissions.
     * 
     * @param userName the username used to filter.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[OptionListQueryData:");
        sb.append(" showSharedOnly = ").append(isShowSharedOnly());
        sb.append(", userName = ").append(getUserName());
        sb.append("]");

        return sb.toString();
    }
}
