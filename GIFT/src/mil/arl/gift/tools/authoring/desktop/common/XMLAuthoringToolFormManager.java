/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common;

import java.io.File;

import com.fg.ftreenodes.FToggleNode;

/**
 * This class allows for XML authoring tools a central location to share information.
 * 
 * @author mhoffman
 *
 */
public class XMLAuthoringToolFormManager{
    
    /** the current XML file being authored.  Can be null if the file hasn't been saved yet. */
    private File currentFile = null;    
    
    /** 
     * the course folder of this file being authored.  Can be null if the file hasn't been saved yet or the course
     * folder couldn't be determined
     */
    private File courseFolder = null;
    
    /** the currently selected node in the authoring tool */
    private FToggleNode selectedNode = null;
    
    /** singleton instance of this class */
    private static XMLAuthoringToolFormManager instance;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return XMLAuthoringToolFormManager
     */
    public static XMLAuthoringToolFormManager getInstance(){
        
        if(instance == null){
            instance = new XMLAuthoringToolFormManager();
        }
        
        return instance;
    }
    
    private XMLAuthoringToolFormManager() {}
    
    /**
     * Return the currently selected node in the tree.
     * Note: can be null if there is no selected node.
     * 
     * @return FToggleNode
     */
    public FToggleNode getSelectedNode(){
        return selectedNode;
    }
    
    /**
     * Set the currently selected node in the tree.
     * Note: can be null if there is no selected node.
     * 
     * @param selectedNode - the node selected
     */
    public void setSelectedNode(FToggleNode selectedNode){
        this.selectedNode = selectedNode;
    }
    
    /**
     * Set the current XML file being authored.  
     * 
     * @param currentFile Can be null if the file hasn't been saved yet.
     */
    public void setCurrentFile(File currentFile){
        this.currentFile = currentFile;
    }
    
    /**
     * Return the current XML file being authored.  
     * 
     * @return File Can be null if the file hasn't been saved yet.
     */
    public File getCurrentFile(){
        return currentFile;
    }
    
    /**
     * Set the current course folder for the XML file being authored.  
     * 
     * @param courseFolder Can be null if the file hasn't been saved yet.
     */
    public void setCourseFolder(File courseFolder){
        this.courseFolder = courseFolder;
    }
    
    /**
     * Return the current course folder for the XML file being authored.  
     * 
     * @return File Can be null if the file hasn't been saved yet or the course folder couldn't be found.
     */
    public File getCourseFolder(){
        return courseFolder;
    }
}
