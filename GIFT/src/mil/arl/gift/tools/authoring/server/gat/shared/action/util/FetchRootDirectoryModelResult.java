/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.Set;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

// TODO: Auto-generated Javadoc
/**
 * The Class GetDomainDirectoryModelResult.
 */
public class FetchRootDirectoryModelResult extends GatServiceResult {
	
	/** The domain directory model. */
	private FileTreeModel domainDirectoryModel;
	
	private Set<String> lockedFiles;
	
    /**
     * Default public constructor.
     */
    public FetchRootDirectoryModelResult() {
        super();
    }
    
    /**
     * Instantiates a new gets the domain directory model result.
     *
     * @param domainDirectoryModel the domain directory model
     */
    public FetchRootDirectoryModelResult(FileTreeModel domainDirectoryModel) {
    	this.domainDirectoryModel = domainDirectoryModel;
	}
    
	/**
	 * Gets the domain directory model.
	 *
	 * @return the domainDirectoryModel
	 */
	public FileTreeModel getDomainDirectoryModel() {
		return domainDirectoryModel;
	}
	
	/**
	 * Sets the domain directory model.
	 *
	 * @param domainDirectoryModel the domainDirectoryModel to set
	 */
	public void setDirectoryModel(FileTreeModel domainDirectoryModel) {
		this.domainDirectoryModel = domainDirectoryModel;
	}

	public Set<String> getLockedFiles() {
		return lockedFiles;
	}

	public void setLockedFiles(Set<String> lockedFiles) {
		this.lockedFiles = lockedFiles;
	}
}
