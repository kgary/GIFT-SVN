/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.Map;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Result containing non-xml files from the server and their url on the file system
 * 
 * @author bzahid
 */
public class FetchMediaFilesResult extends GatServiceResult {

	/** A map of media file names mapped to a url of their location on the file system */
	private Map<FileTreeModel, String> fileMap;
	
	/**
	 * Class constructor. Needed for serialization.
	 */
	public FetchMediaFilesResult() {
		
	}
	
	/**
	 * Sets the media file map
	 * 
	 * @param fileList The map of media file names
	 */
	public void setFileMap(Map<FileTreeModel, String> fileMap) {
		this.fileMap = fileMap;
	}
	
	/**
	 * Gets the map of media files
	 * 
	 * @return a map of media file names
	 */
	public Map<FileTreeModel, String> getFileMap() {
		return fileMap;
	}
	
}
