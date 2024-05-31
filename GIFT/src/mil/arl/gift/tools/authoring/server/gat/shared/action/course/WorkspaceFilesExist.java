/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.FilePath;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.WorkspaceFilesExistResult;
import net.customware.gwt.dispatch.shared.Action;

/**
 * 
 * Action that verifies multiple workspace files exists on the file system
 * 
 * @author cpadilla
 *
 */
public class WorkspaceFilesExist implements Action<WorkspaceFilesExistResult> {
    
    /** Username of the user accessing the file system */
    private String userName;
    
    /** The path of the base directory containing the files */
    private String baseCourseFolderPath;
    
    /** Collection of FilePaths containing a prefix for the base directory or http protocol for urls and the file name */
    private ArrayList<FilePath> filePathList;
    
    /**
     * Constructor
     */
    public WorkspaceFilesExist() {
        
    }

    /**
     * Constructor
     * 
     * @param userName - Username of the user accessing the file system
     * @param baseCourseFolderPath - The path of the base directory containing the files
     * @param mediaList - Collection of FilePaths containing a prefix for the base directory or http protocol for urls and the file name
     * @throws IllegalArgumentException - if any of the arguments are null or empty
     */
    public WorkspaceFilesExist(String userName, String baseCourseFolderPath, List<? extends Serializable> mediaList) {
        if(userName == null || userName.isEmpty() ) {
            throw new IllegalArgumentException("The username cannot be empty or null");
        }

        if(baseCourseFolderPath == null || baseCourseFolderPath.isEmpty() ) {
            throw new IllegalArgumentException("The baseCourseFolderPath cannot be empty or null");
        }
        
        if (mediaList == null) {
            throw new IllegalArgumentException("The mediaList cannot be empty or null");
        }
        
        ArrayList<FilePath> filePathList = new ArrayList<FilePath>();
        for (Serializable listItem : mediaList) {
            
            if(listItem instanceof generated.course.Media) {
                
                generated.course.Media media = (generated.course.Media) listItem;
            
                if(media.getUri() == null || media.getUri().isEmpty()) {
                    throw new IllegalArgumentException("The filePath cannot be empty or null");
                }
                
                filePathList.add(new FilePath(baseCourseFolderPath, media.getUri()));
            
            } else if(listItem instanceof generated.dkf.Media) {
                
                generated.dkf.Media media = (generated.dkf.Media) listItem;
            
                if(media.getUri() == null || media.getUri().isEmpty()) {
                    throw new IllegalArgumentException("The filePath cannot be empty or null");
                }
                
                filePathList.add(new FilePath(baseCourseFolderPath, media.getUri()));
            }
        }
        
        this.baseCourseFolderPath = baseCourseFolderPath;
        this.filePathList = filePathList;
        this.userName = userName;
    }
    
    /**
     * The getter for the baseCourseFolderPath
     * @return the value for the baseCourseFolderPath
     */
    public String getRelativePath(){
        return baseCourseFolderPath;
    }
    
    /**
     * The getter for the filePathList
     * @return the value of the filePathList (cannot be null or empty)
     */
    public List<FilePath> getFilePathList(){
        return filePathList;
    }
    
    /**
     * The getter for the username field
     * @return the value of username (cannot be null or empty)
     */
    public String getUsername() {
        return userName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[WorkspaceFilesExist: userName=");
        builder.append(userName);
        builder.append(", baseCourseFolderPath=");
        builder.append(baseCourseFolderPath);
        builder.append(", filePathList=");
        builder.append(filePathList);
        builder.append("]");
        return builder.toString();
    }
}

