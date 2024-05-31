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
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This is the XML Editor custom node dialog for the SIMILE Configuration file element in the DKF schema file.
 * The dialog allows the user to specify which SIMILE configuration file (contains SIMILE rules) to use in a DKF file being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class SIMILEConfigurationFileDialog extends
        XMLAuthoringToolFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** the file extension to filter on */
    private static final String[] FILE_EXTENSION = {"ixs"};
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SIMILEConfigurationFileDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "SIMILE Configuration File";
    
    private static final String LABEL = "Please select the SIMILE Configuration file with assessment knowledge for this condition.\n\n"
            + "The file must reside in the course folder in order to be used.";
    
    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();

    /**
     * Class constructor - configure dialog
     */
    public SIMILEConfigurationFileDialog() {
        super(TITLE, LABEL, DEFAULT_VALUES, false, FILE_EXTENSION, CommonProperties.getInstance().getWorkspaceDirectory());

    }

    @Override
    public Object[] getCustomValues() {
        
        List<String> values = new ArrayList<>();
        
        try{
            File courseFolder = DAT.getInstance().getDATForm().updateCourseFolder();
            
            if(courseFolder != null){
                //search for existing SIMILE config files in the course folder
                
                List<String> files = CommonUtil.getSIMILEConfigFiles(courseFolder); 
                for(String file : files){
                    values.add(file);
                }
                Collections.sort(values);
            }
        }catch(Throwable e){
            logger.error("There was a problem trying to find all the course folder SIMILE configuration files.", e);
            e.printStackTrace();
        }
        
        return values.toArray(new String[0]);
    }
    
    @Override
    protected String processFileName(String fileName) throws DetailedException{
        
        if(fileName != null){
            File courseFolder = DAT.getInstance().getDATForm().updateCourseFolder();
            if(courseFolder != null){
                return FileFinderUtil.getRelativePath(courseFolder, new File(fileName));
            }
        }
        
        return null;
    }

    @Override
    public void addUserEntry(String value) {
        //nothing to do
    }

}
