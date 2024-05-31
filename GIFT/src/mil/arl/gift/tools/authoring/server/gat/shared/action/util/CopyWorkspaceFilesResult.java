/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/** 
 * The class containing the results of the {@link CopyWorkspaceFiles CopyWorkspaceFiles} action.  
 * The result contains a list of all files that were copied (if the copy was successful).
 *   
 * @author nblomberg
 *
 */
public class CopyWorkspaceFilesResult extends GatServiceResult {
	
	
   /** A list of files that were successfully copied */
	private ArrayList<FileTreeModel> copiedFiles = new ArrayList<FileTreeModel>();
	
	/** A mapping of files that were not copied to the locations where they were supposed to be copied to */
	private Map<String, String> copyFailures = new HashMap<String, String>();

    /**
     * Constructor
     */
    public CopyWorkspaceFilesResult() {
        super();
    }

	
	/**
	 * Adds a file to the list of files that are successfully copied.
	 * 
	 * @param file - A {@link FileTreeModel FileTreeModel} containing the details of the file that was copied.  Cannot be null.  
	 */
	public void addCopiedFile(FileTreeModel file) {
	    copiedFiles.add(file);
	}
	
	
	/**
	 * Return the list of files that were copied successfully.
	 * 
	 * @return ArrayList<{@link FileTreeModel FileTreeModel}> - list of files that were copied successfully.  Cannot be null.
	 */
	public ArrayList<FileTreeModel> getCopiedFiles() {
	    return copiedFiles;
	}

	/**
	 * Adds an indication that the specified source file could not be copied to its destination because a file with the same name 
	 * already exists
	 * 
	 * @param sourceFilePath the path to the source file
	 * @param targetFilePath the path to the destination
	 */
	public void addCopyFailure(String sourceFilePath, String targetFilePath){
		copyFailures.put(sourceFilePath, targetFilePath);
	}

	/**
	 * Gets the mapping of files that were not moved because of name conflicts to the locations where they were supposed to be copied to
	 * 
	 * @return the mapping of files that were not moved to the locations where they were supposed to be copied to
	 */
	public Map<String, String> getCopyFailures(){
		return copyFailures;
	}


	/**
	 * Returns the first copied file name in the list of copied files or null if the list is empty.
	 * 
	 * @return the first copied file name in the list of copied files. Can be null.
	 */
	public String getCopiedFilename() {
		if(copiedFiles != null && !copiedFiles.isEmpty()) {
			return copiedFiles.get(0).getFileOrDirectoryName();
		}
		return null;
	}
	
	@Override
    public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[CopyWorkspaceFileResult: ");
	    
	    sb.append("copiedFiles = {");
	    for(FileTreeModel copiedFile : copiedFiles){
	        sb.append(copiedFile).append(", ");
	    }
	    sb.append("}");
	    
        sb.append(", copyFailures = {");
        for(String copyFailure : copyFailures.keySet()){
            sb.append(copyFailure).append(", ");
        }
        sb.append("}");
	            
	    sb.append(", ").append(super.toString());
	    sb.append("]");
	    return sb.toString();
	}
}
