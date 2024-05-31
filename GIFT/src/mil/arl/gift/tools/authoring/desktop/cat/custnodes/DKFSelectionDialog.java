/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat.custnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.desktop.cat.CAT;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFileSelectionDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the XML Editor custom node dialog for the DKF file element in the course schema file.
 * The dialog allows the user to specify which DKF to use for assessing a training application when developing a course using the CAT.
 * 
 * @author mhoffman
 */
public class DKFSelectionDialog extends XMLAuthoringToolFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DKFSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Domain Knowledge File";
    
    private static final String LABEL = "Please select the DKF with assessment knowledge for the lesson given by this\ntraining application instance.\n\n"
            + "The DKF must reside in the course folder in order to be used.";
    
    private static final String[] FILE_EXTENSION = {AbstractSchemaHandler.DKF_FILE_EXTENSION};

    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Class constructor - create dialog
     */
    public DKFSelectionDialog(){
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
        
        List<String> courseDKFs = new ArrayList<>();
        
        try{
            File courseFolder = CAT.getInstance().getCATForm().updateCourseFolder();
            
            if(courseFolder != null){
                //search for existing DKFs in the course folder
                
                List<String> files = CommonUtil.getDKFs(courseFolder); 
                for(String file : files){
                    courseDKFs.add(file);
                }
                Collections.sort(courseDKFs);
            }
        }catch(Throwable e){
            logger.error("There was a problem trying to find all the course folder DKFs.", e);
            e.printStackTrace();
        }
        
        return courseDKFs.toArray(new String[0]);

    }

    @Override
    public void addUserEntry(String value) {
        //nothing to do
    }

}
