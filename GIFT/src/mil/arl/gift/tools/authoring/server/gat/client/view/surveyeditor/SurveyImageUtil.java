/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.ListBox;

import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.shared.GwtSurveySystemProperties;


/**
 * Utility class to for common logic in dealing with survey images and list of images
 * since there are multiple property sets that contain image lists.
 * 
 * This class is meant to be a class of static methods only.  It is not meant to be 
 * instantiated.
 * 
 * @author nblomberg
 *
 */
public class SurveyImageUtil {

    private static Logger logger = Logger.getLogger(SurveyImageUtil.class.getName());
    
    /** The maximum number of characters to display in an image name before truncation */
    private static int MAX_IMAGE_NAME_LENGTH = 47;
    
    /** The string to substitute truncated file names */
    private static String ELLIPSIS = "...";
    
    /**
     * Constructor (private)
     */
    private SurveyImageUtil() {
        
    }
    
    /**
     * Standardizes the image paths to have forward slashes.
     * 
     * @param imagePath - path of the image
     * @return String - The path of the image with forward slashes (null can be returned if the imagePath was null).
     */
    static private String standarizeImagePaths(String imagePath) {
        if (imagePath != null) {
            return imagePath.replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH);
        }
        
        return null;
    }
    
    /**
     * Sets the selected image path in the list of images.  If the image cannot be found in the list, then it is
     * added at the top of the list.
     *
     * @param imageList - The list of images that are in the list box.
     * @param imageListBox - The list box control that contains the list of images.
     * @param imagePath - The path of the image to set the list box value to.
     */
    static private void setOrAddSurveyImageInList(List<String> imageList, ListBox imageListBox, String imagePath) {
        
        String finalImageName = standarizeImagePaths(imagePath);
        if(logger.isLoggable(Level.INFO)){
            logger.info("setOrAddSurveyItemInList() called.");
        }
        if (imageList.contains(finalImageName)) {
            if(logger.isLoggable(Level.INFO)){
                logger.info("Setting index to: " + imageList.indexOf(finalImageName));
            }
            imageListBox.setSelectedIndex(imageList.indexOf(finalImageName));
        } else {
            
            if(logger.isLoggable(Level.INFO)){
                logger.info("Image (" + finalImageName + ") not found on the server, adding it to the list and setting image index to 1.");
            }
            // Insert the value into the box if it doesn't exist (this most likely would mean that the 
            // item references an invalid image that no longer exists on the server.
            String displayName = getImageDisplayName(finalImageName);
            imageListBox.insertItem(displayName != null ? displayName : finalImageName,  1);
            imageListBox.setValue(1, finalImageName);
            imageList.add(1, finalImageName);
            
            imageListBox.setSelectedIndex(1);
            
        }
    }
    
    /**
     * Populates a list box with the list of images and sets the value to the selectedImage.
     * 
     * @param inList - The input list of images to populate the list box with.
     * @param outList - The returned list of images that are populated in the list box.
     * @param imageListBox - The list box control that will be populated.
     * @param selectedImage - The value that should be selected in the list box.  If the value cannot be found in the list, then it is
     *                        added to the list.
     */
    static public void populateSurveyImagesList(List<String> inList, List<String> outList, ListBox imageListBox,  String selectedImage) {
        if(logger.isLoggable(Level.INFO)){
            logger.info("populateSurveyImagesList() called: " + selectedImage);
        }

        outList.clear();
        imageListBox.clear();
        
        // Add an empty item into our list
        imageListBox.addItem("");
        outList.add("");
        int index = 1;
        for (String imageName : inList) {
            String fullImageName = GwtSurveySystemProperties.SURVEY_IMAGE_UPLOAD_URL + imageName;
            
            String displayName = getImageDisplayName(imageName);
            imageListBox.addItem(displayName != null ? displayName : imageName);
            imageListBox.setValue(index++, fullImageName);
            outList.add(fullImageName);
        }

        String replacedImage = SurveyImageUtil.standarizeImagePaths(selectedImage);
        if (replacedImage != null && !replacedImage.isEmpty()) {
            SurveyImageUtil.setOrAddSurveyImageInList(outList, imageListBox, replacedImage);
        } else {
            imageListBox.setSelectedIndex(0);
        }
        
    }
    
    /**
     * Get the display name for an image path, truncating it to the file name and
     * inserting ellipsis if longer than the maximum allowed image name length.
     * ie. If the path is /my/directory/AVeryLongImagePathName.jpg and the max
     * length allowed is 10, the image display name would be: AV...e.jpg
     * 
     * @param imagePath the path to the image
     * @return the truncated file name or null if the imagePath is blank
     */
    private static String getImageDisplayName(String imagePath) {
        
        if (StringUtils.isBlank(imagePath)) {
            return null;
        }
        
        // Get the file name from the string
        String imageName = FileTreeModel.createFromRawPath(imagePath).getFileOrDirectoryName();

        // Limit name length to the last few characters
        int length = imageName.length();
        if (length > MAX_IMAGE_NAME_LENGTH) {
            final String namePrefix = imageName.substring(0, MAX_IMAGE_NAME_LENGTH/2 - ELLIPSIS.length());
            final String namePostfix = imageName.substring(length - (MAX_IMAGE_NAME_LENGTH/2), length);
            imageName = namePrefix + ELLIPSIS + namePostfix;
        }

        return imageName;
    }
    
    
    
}
