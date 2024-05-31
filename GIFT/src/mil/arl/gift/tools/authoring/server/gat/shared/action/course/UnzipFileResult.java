/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link UnzipFile} action. Contains a file tree model representing the folder containing the extracted files.
 * The root of this model is the course subfolder where the extracted files are located.
 * 
 * @author nroberts
 */
public class UnzipFileResult extends GatServiceResult {
	
	/** 
	 * A model representing the folder containing the files that were extracted. The root of this model should 
	 * be the course subfolder where the extracted files are located.
	 */
	private FileTreeModel unzippedFolderModel;

	/**
	 * Gets the model representing the folder containing the files that were extracted. The root of this model should
	 * be the course subfolder where the extracted files are located.
	 * 
	 * @return the model of the unzipped folder
	 */
	public FileTreeModel getUnzippedFolderModel() {
		return unzippedFolderModel;
	}

	/**
	 * Sets the model representing the folder containing the files that were extracted. The root of this model should
	 * be the course subfolder where the extracted files are located.
	 * 
	 * @param unzippedFolderModel the model of the unzipped folder
	 */
	public void setUnzippedFolderModel(FileTreeModel unzippedFolderModel) {
		
		if(unzippedFolderModel == null){
			throw new IllegalArgumentException("The model of the folder containing the extracted files cannot be null.");
		}
		
		this.unzippedFolderModel = unzippedFolderModel;
	}

}
