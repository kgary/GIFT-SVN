/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileChooserHelper;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.LearnerActionsFileFilter;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolClassFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the XML Editor custom node dialog for the Learner Action file element in the DKF schema file under the available learner actions element.
 * The dialog allows the user to specify which Learner Actions file to use for gathering references to learner actions when developing a DKF using the DAT.
 * 
 * @author mhoffman
 *
 */
public class LearnerActionSelectionDialog extends XMLAuthoringToolClassFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerActionSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Learner Actions File";
    
    private static final String LABEL = "Please select the Learner Actions file with references to appropriate learner actions for the domain.\n\n"
            + "The Learner Actions file must reside in the course folder in order to be used.";

    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Class constructor - create dialog
     */
    public LearnerActionSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);

    }
    
    /**
     * The browse button was pressed.  Display a file browser window so the user can select the learner action files whose filepath
     * will populate the combobox.
     * 
     * @return String - the learner actions file selected by the user
     * @throws DetailedException if there was a problem with the dkf schema file
     */
    @Override
    protected String browseForItem() throws DetailedException {
       
        String fileName = null;
        File courseFolder = DAT.getInstance().getDATForm().updateCourseFolder();
        File file = FileChooserHelper.showSelectFileDialog(courseFolder.getAbsolutePath(), LearnerActionsFileFilter.FILE_EXTENSION);
        
        if(file != null){
            //get classpath and populate combobox
            
            fileName = FileFinderUtil.getRelativePath(courseFolder, file);
        }
        
        return fileName;
    }

    @Override
    public String[] getCustomValues() {
        
        List<String> values = new ArrayList<>();
        
        try{
            File courseFolder = DAT.getInstance().getDATForm().updateCourseFolder();
            
            if(courseFolder != null){
                //search for existing DKFs in the course folder
                
                List<String> files = CommonUtil.getLearnerActionsFiles(courseFolder); 
                for(String file : files){
                    values.add(file);
                }
                Collections.sort(values);
            }
        }catch(Throwable e){
            logger.error("There was a problem trying to find all the course folder learner action files.", e);
            e.printStackTrace();
        }
        
        return values.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        //nothing to do
    }

}
