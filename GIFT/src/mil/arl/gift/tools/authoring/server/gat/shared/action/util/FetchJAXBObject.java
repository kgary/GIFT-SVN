/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action telling the dispatch service to fetch the JAXB object corresponding to a file.
 * 
 * @author nroberts
 */
public class FetchJAXBObject implements Action<FetchJAXBObjectResult> {

	/** The Domain-relative path to the file from which to get the JAXB object.*/
    private String domainRelativePath;
	
	/** The user name. */
	private String userName;
	
	/** 
	 * whether to use parent folder in the relativePath as the course folder.  This is useful
     * if you can guarantee that the parent folder is the course folder (e.g. gift wrap authored dkf.xml).  In the past
     * the gift authoring tool would allow authors to place GIFT xml files in subfolders of the course folder.
     */
	private boolean useParentAsCourse;
    
    /** 
     * Default public constructor. 
     */
    public FetchJAXBObject() {
        super();
    }

	/**
	 * Gets the domain relative path.
	 *
	 * @return the domain relative path
	 */
	public String getRelativePath() {
		return domainRelativePath;
	}

	/**
	 * Sets the domain relative path.
	 *
	 * @param domainRelativePath the new domain relative path
	 */
	public void setRelativePath(String domainRelativePath) {
		this.domainRelativePath = domainRelativePath;
	}

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * whether to use parent folder in the relativePath as the course folder.  This is useful
     * if you can guarantee that the parent folder is the course folder (e.g. gift wrap authored dkf.xml).  In the past
     * the gift authoring tool would allow authors to place GIFT xml files in subfolders of the course folder.
     * 
	 * @return default is false
	 */
    public boolean isUseParentAsCourse() {
        return useParentAsCourse;
    }

    /**
     * Set whether to use parent folder in the relativePath as the course folder.  This is useful
     * if you can guarantee that the parent folder is the course folder (e.g. gift wrap authored dkf.xml).  In the past
     * the gift authoring tool would allow authors to place GIFT xml files in subfolders of the course folder.
     * @param useParentAsCourse the value to use
     */
    public void setUseParentAsCourse(boolean useParentAsCourse) {
        this.useParentAsCourse = useParentAsCourse;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchJAXBObject: ");
        sb.append("domainRelativePath = ").append(domainRelativePath);
        sb.append(", userName = ").append(userName);
        sb.append(", useParentAsCourse = ").append(isUseParentAsCourse());
        sb.append("]");

        return sb.toString();
    } 
}
