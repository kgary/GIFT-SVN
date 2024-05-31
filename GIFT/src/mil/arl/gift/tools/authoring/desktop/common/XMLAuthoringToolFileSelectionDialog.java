/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileChooserHelper;

/**
 * This dialog is used by the Authoring Tools that use XML Editor and have the appropriate annotation in 
 * the XML schemas used by the tools.  For example the learnerConfig.xsd specifies a Translator editor-class which
 * extends this class.  The dialog created contains a label, for information/hints/guidance and a combo box with the possible
 * choices.  In addition this class enables the file browse button and supports selecting a file whose name will help populate
 * the appropriate XML element in the authoring tool.
 * 
 * @author mhoffman
 *
 */
public abstract class XMLAuthoringToolFileSelectionDialog extends
        XMLAuthoringToolSelectionDialog {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(XMLAuthoringToolFileSelectionDialog.class);
    
	private static final long serialVersionUID = 1L;
    
    private String[] fileExtensions;
    
    private String browseStartLocation;
    
    /**
     * Class constructor - create and show dialog
     * 
     * @param title - the text to display as the title of the dialog
     * @param label - the text to display for the information label on the dialog
     * @param defaultValues - the default values to populate the combo box with
     * @param allowUserEntries - whether to allow the user to manually type in their own values into the combobox
     * @param fileExtensions - list of file extensions to filter on in a file browser with a file filter. Can be null for no filtering.
     * @param browseStartLocation - the starting location (folder) for the file browser
     */
    public XMLAuthoringToolFileSelectionDialog(String title, String label,
            List<SelectionItem> defaultValues, boolean allowUserEntries, String[] fileExtensions, String browseStartLocation) {
        super(title, label, defaultValues, allowUserEntries);        
        
        if(browseStartLocation == null){
            this.browseStartLocation = PackageUtil.getRoot();
        }else{
            this.browseStartLocation = browseStartLocation;
        }
        
        this.fileExtensions = fileExtensions;
    }
    
    protected void setFileExtension(String[] fileExtensions){
        this.fileExtensions = fileExtensions;
    }
    
    protected String[] getFileExtension(){
        return fileExtensions;
    }
    
    @Override
    protected void initComponents(String title, String label) {
        
        super.initComponents(title, label);
        
        browseButton.setVisible(true);

        browseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                try{
                    browseButtonActionPerformed(evt);
                }catch(DetailedException e){
                    logger.error("Failed to process the selected file.", e);
                }
            }
        });
        
    }
    
    /**
     * The browse button was pressed.  Present a file browser to the user for them to select an entry
     * to populate the combo box with.
     * 
     * @param evt the button action event
     * @throws DetailedException if there was a problem processing the selected file
     */
    protected void browseButtonActionPerformed(java.awt.event.ActionEvent evt) throws DetailedException{
        
        String userBrowsedItem = browseForItem();
        if(userBrowsedItem != null){
            
            if( ((javax.swing.DefaultComboBoxModel<Object>)valuesComboBox.getModel()).getIndexOf(userBrowsedItem) == -1 ){
                valuesComboBox.insertItemAt(userBrowsedItem, 0);
            }
            
            valuesComboBox.setSelectedItem(userBrowsedItem);
        }
    }
    
    /**
     * Process the selected file name before adding it to the values combobox.
     * 
     * @param fileName name of the file to process
     * @return the processed file name
     * @throws DetailedException if there was a problem processing the file name
     */
    protected String processFileName(String fileName) throws DetailedException{
        //default implementation
        return fileName;
    }
    
    /**
     * Display a file browser window so the user can select the Java file whose classpath
     * will populate the combo box.
     * 
     * @return String - the classpath of a java file selected by the user
     * @throws DetailedException if there was a problem processing the file name that was selected
     */
    protected String browseForItem() throws DetailedException {
       
        String processedFileName = null;
        File file = FileChooserHelper.showSelectFileDialog(browseStartLocation, fileExtensions);
        
        if(file != null){
            //get classpath and populate combobox
            
            String fullFileName = file.getAbsolutePath();
            
            processedFileName = processFileName(fullFileName);
            
            if(processedFileName != null){
                valuesComboBox.setSelectedItem(processedFileName);
                valuesComboBox.setEnabled(true);
            }
        }
        
        return processedFileName;
    }

}
