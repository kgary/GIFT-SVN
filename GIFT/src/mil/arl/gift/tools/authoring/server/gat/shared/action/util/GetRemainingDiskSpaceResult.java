/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result containing the remaining disk space for a user workspace.
 * 
 * @author bzahid
 */
public class GetRemainingDiskSpaceResult extends GatServiceResult {
	
	/**
	 * The remaining disk space in megabytes
	 */
	private String diskSpace;
	
	/**
	 * Default constructor
	 */
	public GetRemainingDiskSpaceResult() {
		super();
	}
	
	/**
	 * Gets the remaining disk space.
	 * 
	 * @return the remaining disk space for a user workspace. Can be null.
	 */
	public String getDiskSpace() {
		return diskSpace;
	}

	/**
	 * Sets the remaining disk space.
	 * 
	 * @param diskSpace the remaining disk space for a user workspace. Can be null.
	 */
	public void setDiskSpace(String diskSpace) {
		this.diskSpace = diskSpace;
	}

}
