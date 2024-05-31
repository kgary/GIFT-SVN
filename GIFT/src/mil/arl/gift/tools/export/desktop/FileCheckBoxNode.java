/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.File;

import mil.arl.gift.common.io.FileFinderUtil;

/**
 * This class extends CheckBox node in order to provide special handling of the File object
 * associated with the CheckBox.
 * 
 * @author mhoffman
 *
 */
public class FileCheckBoxNode extends CheckBoxNode {
    
    /** Generated serial */
    private static final long serialVersionUID = 1L;
    
    private File relativeToAncestorDirectory;

    /**
     * Class constructor - set attribute(s)
     * 
     * @param file - the file object being associated with the checkbox
     * @param relativeToAncestorDirectory - an optional ancestor directory to use to trim the 
     * prefix of the file name to show
     */
    public FileCheckBoxNode(File file, File relativeToAncestorDirectory){
        
        if(file == null){
            throw new IllegalArgumentException("The file can't be null.");
        }else if(!file.exists()){
            throw new IllegalArgumentException("The file of "+file+" must exist.");
        }else if(relativeToAncestorDirectory != null && !relativeToAncestorDirectory.exists() && !relativeToAncestorDirectory.isDirectory()){
            throw new IllegalArgumentException("The directory "+relativeToAncestorDirectory+" must exist and be a directory.");
        }
        
        setUserObject(file);        
        this.relativeToAncestorDirectory = relativeToAncestorDirectory;
    }
    
    @Override
    public boolean equals(Object otherObj){
        return otherObj instanceof FileCheckBoxNode && ((FileCheckBoxNode)otherObj).getUserObject().equals(getUserObject());
    }
    
    @Override
    public int hashCode(){
        return getUserObject().hashCode();
    }
    
    @Override
    public String toString(){
        
        if(relativeToAncestorDirectory != null){
            return FileFinderUtil.getRelativePath(relativeToAncestorDirectory, (File)getUserObject());
        }else{
            return ((File)getUserObject()).getName();
        }
    }
}
