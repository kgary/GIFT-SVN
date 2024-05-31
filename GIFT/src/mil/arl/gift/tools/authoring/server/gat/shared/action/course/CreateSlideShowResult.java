/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.ArrayList;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Result of a CreateSlideShow action.
 * 
 * @author bzahid
 */
public class CreateSlideShowResult extends GatServiceResult {

    /** list of image paths that are relative to the course folder */
	private ArrayList<String> relativeSlidePaths;
	
	/**
	 * the folder, including the ancestor folder information, where the slide 
     * show images were placed. E.g. [Desktop] will be pointing to the <course object name> for the slide show 
     * course object with ancestor parent file tree model like 'workspace/mhoffman/test/Slide Shows/' where 'test'
     * is the course folder name and 'Slide Shows' is the fixed folder name that GIFT creates when creating slide shows images.
	 */
	private FileTreeModel slidesFolderModel;
	
	/** whether or not a file name conflict occurred */
	private boolean hasNameConflict = false;
	
	/** the file name that caused the conflict, can be null if {@link #getHasNameConflict()} is false. */
	private String nameConflict;
	
	/** 
	 * Class constructor. Don't change, required for serialization
	 */
	public CreateSlideShowResult() {
		
	}
	
	/**
	 * Sets the list of image paths that are relative to the course folder
	 * 
	 * @param relativeSlidePaths A list of the image paths.
	 */
	public void setRelativeSlidePath(ArrayList<String> relativeSlidePaths) {
		this.relativeSlidePaths = relativeSlidePaths;
	}
	
	/**
	 * The list of image paths that are relative to the course folder
	 * 
	 * @return a list of image paths
	 */
	public ArrayList<String> getRelativeSlidePaths() {
		return relativeSlidePaths;
	}
	
	/**
	 * Set the file tree model for the folder where the slide show images were placed.
	 * @param slidesFolderModel the folder, including the ancestor folder information, where the slide 
	 * show images were placed. E.g. [Desktop] will be pointing to the <course object name> for the slide show 
	 * course object with ancestor parent file tree model like 'workspace/mhoffman/test/Slide Shows/' where 'test'
	 * is the course folder name and 'Slide Shows' is the fixed folder name that GIFT creates when creating slide shows images.
	 */
	public void setSlidesFolderModel(FileTreeModel slidesFolderModel) {
		this.slidesFolderModel = slidesFolderModel;
	}
	
	/**
	 * Return the file tree model for the folder where the slide show images were placed.
     * @return the folder, including the ancestor folder information, where the slide 
     * show images were placed. E.g. [Desktop] will be pointing to the <course object name> for the slide show 
     * course object with ancestor parent file tree model like 'workspace/mhoffman/test/Slide Shows/' where 'test'
     * is the course folder name and 'Slide Shows' is the fixed folder name that GIFT creates when creating slide shows images.
     * Should not be null but can be if the creator didn't call {@link #setSlidesFolderModel(FileTreeModel)} with non-null.
	 */
	public FileTreeModel getSlidesFolderModel() {
		return slidesFolderModel;
	}
	
	/**
	 * Sets whether or not a file name conflict occurred
	 * 
	 * @param hasNameConflict true if there was a file name conflict, false otherwise
	 */
	public void setHasNameConflict(boolean hasNameConflict) {
		this.hasNameConflict = hasNameConflict;
	}

	/**
	 * Gets whether or not a file name conflict occurred
	 * 
	 * @return true if there was a file name conflict, false otherwise.  Default is false.
	 */
	public boolean getHasNameConflict() {
		return hasNameConflict;
	}
	
	/**
	 * Sets the file name that caused the conflict
	 * 
	 * @param conflict The file name that caused the conflict. Can be null if there was no name conflict.
	 */
	public void setNameConflict(String conflict) {
		this.nameConflict = conflict;
	}
	
	/**
	 * Gets the file name that caused the conflict
	 * 
	 * @return The file name that caused the conflict.  Can be null if there was no name conflict, {@link #getHasNameConflict()} is false.
	 */
	public String getNameConflict() {
		return nameConflict;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CreateSlideShowResult: relativeSlidePaths = ");
        builder.append(relativeSlidePaths);
        builder.append(", slidesFolderModel = ");
        builder.append(slidesFolderModel);
        builder.append(", hasNameConflict = ");
        builder.append(hasNameConflict);
        builder.append(", nameConflict = ");
        builder.append(nameConflict);
        builder.append("]");
        return builder.toString();
    }
	
	
}
