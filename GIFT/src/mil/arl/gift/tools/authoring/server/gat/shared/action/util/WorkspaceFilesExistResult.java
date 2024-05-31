/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.tools.authoring.server.gat.shared.FilePath;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

public class WorkspaceFilesExistResult extends GatServiceResult {

    /**
     * A mapping of files and whether or not they exist in the file system
     * FilePath - a string containing the address or uri to the file
     * GatServiceResult - a Result object containing info on if the file exists on the file system or as a uri
     */
    private Map<FilePath, GatServiceResult> filesExist = new HashMap<FilePath, GatServiceResult>();
    
    /**
     * Constructor
     * 
     */
    public WorkspaceFilesExistResult() {
        super();
    }
    
    /**
     * Add whether a new file exists to the results list
     * 
     * @param filePath - Path of the file, cannot be null
     * @param exists - True if the file exists, false if media could not be found
     * @throws IllegalArgumentException - if the filePath is null
     */
    public void addFileResult(FilePath filePath, Boolean exists) {
        if (filePath == null) {
            throw new IllegalArgumentException("The filePath cannot be null.");
        }
        
        GatServiceResult result = new GatServiceResult();
        result.setSuccess(exists);
        filesExist.put(filePath, result);
    }
    
    /**
     * Add a new file to the result containing error details
     * @param filePath - Path of the file
     * @param exists - True if the file exists, false if media could not be found
     * @param errorMessage - The error message of the exception that occurred when trying to locate the file
     * @throws IllegalArgumentException - if the filePath or errorMessage is null
     */
    public void addFileResult(FilePath filePath, Boolean exists, String errorMessage) {
        if (filePath == null) {
            throw new IllegalArgumentException("The filePath cannot be null.");
        }
        if (errorMessage == null) {
            throw new IllegalArgumentException("The errorMessage cannot be null.");
        }

        GatServiceResult result = new GatServiceResult();
        result.setSuccess(exists);
        result.setErrorMsg(errorMessage);
        filesExist.put(filePath, result);
    }
    
    /**
     * Gets the filesExist map
     * 
     * @return filesExist - A mapping of files and whether or not they exist in the file system or as a uri
     */
    public Map<FilePath, GatServiceResult> getFilesExistResults() {
        return filesExist;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[WorkspaceFilesExistResult: filesExist=");
        builder.append(filesExist);
        builder.append("]");
        return builder.toString();
    }
}

