/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat.custnodes;

import generated.course.Media;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.LessonMaterialFileFilter;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.desktop.cat.CAT;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFileSelectionDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the XML Editor custom node dialog for the Lesson Material file element in the course schema file under the lesson material element.
 * The dialog allows the user to specify which Lesson Material file to use for gathering references to lesson material when developing a course using the CAT.
 * 
 * @author mhoffman
 */
public class LessonMaterialSelectionDialog extends XMLAuthoringToolFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LessonMaterialSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Lesson Material File";
    
    private static final String LABEL = "Please select the Lesson Material file with references to appropriate lesson material for the \n"+
            "lesson given by this training application instance.\n\n"
            + "The Lesson Material XML file must reside in the course folder in order to be used.";
    
    private static final String[] FILE_EXTENSION = {LessonMaterialFileFilter.FILE_EXTENSION};

    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Class constructor - create dialog
     */
    public LessonMaterialSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false, FILE_EXTENSION, CommonProperties.getInstance().getWorkspaceDirectory());

    }
    
    @Override
    protected String processFileName(String fileName) throws DetailedException{
        
        if(fileName != null){
            File courseFolder = CAT.getInstance().getCATForm().updateCourseFolder();
            if(courseFolder != null){
                return FileFinderUtil.getRelativePath(courseFolder, new File(fileName));
            }
        }
        
        return null;
    }

    @Override
    public String[] getCustomValues() {

        List<String> values = new ArrayList<>();
        
        try{
            File courseFolder = CAT.getInstance().getCATForm().updateCourseFolder();
            
            if(courseFolder != null){
                //search for existing lesson material files in the course folder
                
                List<String> files = CommonUtil.getLessonMaterialFiles(courseFolder); 
                for(String file : files){
                    values.add(file);
                }
                Collections.sort(values);
            }
        }catch(Throwable e){
            logger.error("There was a problem trying to find all the course folder lesson material files.", e);
            e.printStackTrace();
        }
        
        return values.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        //nothing to do
    }
    
    @Override
    protected void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	
    	super.okButtonActionPerformed(evt);
    	
        //save selected/provided entry
        Object tempSelectedValue = valuesComboBox.getSelectedItem();
        
        if(tempSelectedValue != null && tempSelectedValue.toString().length() > 0){
            
        	//a value has been provided/selected, validate it        	
        	validateLessonMaterialFile(tempSelectedValue.toString());        	
        }    
    }
    
    /**
     * Validates the contents of the selected lesson material file.
     * 
     * @param fileName The name of the lesson material file to be validated
     */
    private void validateLessonMaterialFile(final String fileName){
        
        //used for dialogs created in this method
        final Component parentComponent = this;
                    
        //Release the calling event dispatch thread to allow the following dialogs to be shown correctly
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {              
                
                // show custom 'please wait' dialog
                final JDialog dialog = new JDialog();
                JLabel label = new JLabel("<html><br>Please wait while the media within this lesson material file is validated.<br><br></html>");
                dialog.setLocationRelativeTo(parentComponent);
                dialog.setTitle("Please Wait...");
                dialog.add(label);
                dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                dialog.setIconImage(ImageUtil.getInstance().getSystemIcon());
                dialog.pack();
                dialog.setVisible(true);                

                //Release the event dispatch thread to allow any dialog created below to be shown correctly
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                    	
                    	try{
                        
	                        // get the list of media used by the lesson material file
                    	    File courseFolderFile = CAT.getInstance().getCATForm().updateCourseFolder();
                    	    DesktopFolderProxy courseFolder = new DesktopFolderProxy(courseFolderFile);
	                        LessonMaterialFileHandler fileHandler = new LessonMaterialFileHandler(fileName, courseFolder, InternetConnectionStatusEnum.UNKNOWN);
	                      	fileHandler.parse(true);
	                        List<Media> mediaList = fileHandler.getLessonMaterial().getMedia();
	                        
	                        // create string builders to store messages related to URIs that are invalid or were corrected during validation
	                        StringBuilder invalidUris = new StringBuilder();
	                        StringBuilder correctedUris = new StringBuilder();
	                                                
	                        // validate the media
	                        for(Media m : mediaList){
	                        	
	                        	String uriString = m.getUri();
	                            
	                        	try{
		                           
	                        		String correctedUri = LessonMaterialFileHandler.validateMedia(m, null, courseFolder, InternetConnectionStatusEnum.UNKNOWN);		                            
		                            if(correctedUri != null){
		                            	 try{                    
		                                        // correct any media URIs that need correction
		                                        FileUtil.updateUriFields(uriString, correctedUri, courseFolderFile + "/" + fileName);
		                                        
		                                        // notify author of corrected URI
		                                        correctedUris.append("   Old:    ").append(uriString).append("\n   New:  ").append(correctedUri).append("\n\n");
		                                        
		                                  } catch(Exception ex){
		                                        
		                                        // if an error occurs, notify author that the URI needs correction
		                                        invalidUris.append("\nURI:          ").append(uriString).append("\n   Reason:  The specified URI should be replaced with \"").append(correctedUri).append("\"\n\n");
		                                        logger.error("Caught exception while trying update references to URI " + uriString + " in file " + courseFolderFile + "/" + fileName + ".", ex);
		                                  }
		                            }
	                            
	                         
	                                
	                            } catch (Exception e){
	                                
	                                if(e.getCause() instanceof java.net.MalformedURLException) {
	                                    
	                                    // notify author of malformed URL
	                                    invalidUris.append("   URI:          ").append(uriString).append("\n   Reason:  The specified URI is malformed.\n\n");
	                                
	                                } else if(e instanceof java.net.ConnectException) {
	                                    
	                                    // notify author of invalid HTTP status response
	                                    invalidUris.append("   URI:          ").append(uriString).append("\n   Reason:  Received an invalid HTTP response code while connecting to the specified URI.\n\n");
	                                    
	                                } else {
	                                    
	                                    // notify author of general media validation failure
	                                    invalidUris.append("   URI:          ").append(uriString).append("\n   Reason:  The specified URI could not be verified as a reference to a valid local file or web resource.\n\n");
	                                }
	                            } 
	                        }
	                        
	                        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	                        dialog.dispose();
	                                
	                        if(!invalidUris.toString().isEmpty() || !correctedUris.toString().isEmpty()){
	                            
	                            // if there are any corrections or invalid URIs that need attention, present them to the author
	                            
	                            String invalidUriMessage = "";
	                            String warningMessage = "";
	                            if(!invalidUris.toString().isEmpty()){
	                                invalidUriMessage = "The following URIs within this lesson material file are invalid:\n\n";
	                                invalidUriMessage += invalidUris.toString() + "\n";
	                                warningMessage += "Saving this course with invalid URIs could cause problems while checking the course.\n"
	                                        + "Please fix any invalid URIs before trying to run the course with GIFT.";
	                            }
	                            
	                            String correctedUriMessage = "";
	                            if(!correctedUris.toString().isEmpty()){
	                                correctedUriMessage = "The following corrections were made within this lesson material file:\n\n";
	                                correctedUriMessage += correctedUris.toString() + "\n";
	                            }
	                        
	                            JOptionPane.showMessageDialog(parentComponent,
	                                "GIFT detected issues while validating the lesson material file \n\"" + fileName + "\"\n\n" 
	                                    + invalidUriMessage 
	                                    + correctedUriMessage
	                                    + warningMessage,
	                                "Lesson Material File Validation",
	                                JOptionPane.INFORMATION_MESSAGE);
	
	                        } 
	                        
	                        return null;
	                        
                    	}catch(Throwable e){
                        	
                    		// if an exception occurs while validating, make sure to close the 'please wait' dialog
                    		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 	                        dialog.dispose();
 	                        
 	                       JOptionPane.showMessageDialog(parentComponent,
                                "GIFT encountered an error while validating the lesson material file \n\"" + fileName + "\"\n\n"
                                	+ "Saving this course with an invalid lesson material file could cause problems while checking the course.\n"
                                	+ "Please check the specified lesson material file for errors before trying to run the course with GIFT.",                             
                                "Lesson Material File Validation",
                                JOptionPane.ERROR_MESSAGE);
 	                        
                        	throw e;
                        }
                        
                    }//end doInBackground()
                };
                
                //start the worker task (which will also dispose of the 'please wait' dialog)
                worker.execute();                
                
            }//end run()
        });   	

    }
}
