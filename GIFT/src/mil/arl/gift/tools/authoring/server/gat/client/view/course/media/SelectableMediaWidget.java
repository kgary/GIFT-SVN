/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.media;

import java.util.Arrays;

import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

/**
 * A widget that represents a non-xml file within a course folder
 * 
 * @author bzahid
 */
public class SelectableMediaWidget {
	
	private String filePath;
	
	private String fileName;
	
	private String type;
	
	private boolean isSelected = false;
	
	protected Icon icon = new Icon();
	
	/**
	 * Creates a new media file widget from a FileTreeModel
	 * 
	 * @param ftModel The filetree model the widget will represent (cannot be null)
	 * @throws IllegalArgumentException if the ftModel is null
	 */
	public SelectableMediaWidget(FileTreeModel ftModel) throws IllegalArgumentException {
		
	    //Validates the arguments
	    if(ftModel == null) {
	        throw new IllegalArgumentException("The value of the FileTreeModel used to create a SelectableMediaWidget cannot be null");
	    }
	    
	    //Initializes the fields
	    this.filePath = GatClientUtility.getBaseCourseFolderPath() + Constants.FORWARD_SLASH + ftModel.getRelativePathFromRoot(true);
	    this.fileName = ftModel.getFileOrDirectoryName();
	    initIcon();	
		
	}
	
	/**
	 * Creates a new media file widget from a String
	 * 
	 * @param fileName The name of the file the widget will represent (cannot be null or empty)
	 * @throws IllegalArgumentException if the fileName is null or empty
	 */
	public SelectableMediaWidget(String fileName) throws IllegalArgumentException {
	    
	    //Validates the arguments
	    if(fileName == null) {
	        throw new IllegalArgumentException("The value of the String used to create a SelectableMediaWidget cannot be null");
	    }
        if(fileName.isEmpty()) {
	        throw new IllegalArgumentException("The value of the String used to create a SelectableMediaWidget cannot be empty");
	    }
	    
	    //Initializes the fields
	    this.filePath = GatClientUtility.getBaseCourseFolderPath() + fileName;
        this.fileName = fileName;
        initIcon();
	}
	
	/**
	 * Determines the icon value based off of the extension of the filename
	 */
	private void initIcon() {
	    type = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        
        if(type.equals(".zip")) {
            icon.setType(IconType.FILE_ARCHIVE_O);
            
        } else if (type.equals(".pdf")) {
            icon.setType(IconType.FILE_PDF_O);
                        
        } else if (StringUtils.endsWith(type, Constants.image_supported_types)) {
            icon.setType(IconType.FILE_IMAGE_O);
            
        } else if (Constants.AUDIO.contains(type)) {
            icon.setType(IconType.FILE_AUDIO_O);
            
        } else if (StringUtils.endsWith(type, Constants.ppt_show_supported_types)) {
            icon.setType(IconType.FILE_POWERPOINT_O);
            
        } else if (Arrays.asList(Constants.html_supported_types).contains(type) || type.equals(".js")) {
            icon.setType(IconType.FILE_CODE_O);
            
        } else {
            icon.setType(IconType.FILE);
        }       
	}
	
	/**
	 * Gets the name of the media file.
	 * 
	 * @return The name of the media file.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Getter for the filePath field
	 * @return The value of the file path relative to the workspace folder. Cannot be null
	 */
	public String getFilePath() {
	    return this.filePath;
	}
	
	/**
	 * Gets the file type/extension of the media
	 * 
	 * @return The file extension as a string (e.g. ".pdf")
	 */
	
	public String getType(){
	    return type;
	}
	
	/**
	 * Gets the media file type icon.
	 * 
	 * @return The media file type icon.
	 */
	public Icon getIcon() {
		return icon;
	}
	
	/**
	 * Sets whether or not the selectable media widget is selected
	 * 
	 * @param isSelected True if the selectable media widget is selected, false otherwise
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	/**
	 * Gets whether or not the selectable media widget is selected.
	 * 
	 * @return True if the widget is selelected, false otherwise.
	 */
	public boolean isSelected() {
		return isSelected;
	}
}
