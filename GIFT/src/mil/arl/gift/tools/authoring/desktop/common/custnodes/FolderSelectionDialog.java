/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.io.File;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.FileChooserHelper;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

import com.fg.ftreenodes.ICellControl;
import com.fg.ftreenodes.Params;

/**
 * This class presents a file browse dialog to allow the user to select a directory to use as a value
 * for an XML element.  The dialog can be configured to start at a specific directory, as well as return relative
 * paths instead of absolute paths for the directory selected.
 * 
 * @author mhoffman
 *
 */
public class FolderSelectionDialog extends JDialog implements ICellControl {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FolderSelectionDialog.class);
    
    /** 
     * parameter key in the schema 
     */
    
    /** the starting directory (relative to the GIFT directory) for the folder browse dialog*/
    private static final String STARTING_DIRECTORY = "StartingDirectory";
    
    /** the relative directory (value is relative to the GIFT directory) to use when returning the value for the folder selected,
     * i.e. return the relative path from the selected folder to the directory listed for this param (which is relative to the GIFT folder) */
    private static final String RELATIVE_TO_DIRECTORY = "RelativeToDirectory";
    
    private static File GIFT_ROOT_DIR = new File(".");
    
    /** by default folder's will be referenced from the root of GIFT (i.e. the GIFT folder) */
    private static File DEFAULT_RELATIVE_TO_DIRECTORY = GIFT_ROOT_DIR;
    private static File DEFAULT_STARTING_DIRECTORY = GIFT_ROOT_DIR;
    
    /** which directory to start the file browse dialog at */
    private File startingDirectory = DEFAULT_STARTING_DIRECTORY;
    
    /**used to change the absolute selected folder path to a relative path, relative to this directory */
    private File relativeToDirectory = DEFAULT_RELATIVE_TO_DIRECTORY;
    
    /** the selected directory relative path (relative to the relativeToDirectory value) */
    private String selectedFolderRelativePath = null;
    
    /**
     * Default constructor
     */
    public FolderSelectionDialog(){
        
        //make sure the actual dialog is never visible, the actual dialog will just be a Java file browse dialog shown later...
        this.setSize(0, 0);
    }

    @Override
    public Object getData() {
        return selectedFolderRelativePath;
    }

    @Override
    public void initCellControl(boolean arg0) {

    }
    
    /**
     * Dispose of the dialog (which is never visible in the first place).
     * This method causes the "getData" method to be called by XML editor library.  So far this 
     * is the easiest way to cause the id value to be used and actually provided as a non-null, non-empty data
     * value when "updateCellControl" is called upon.
     */
    private void close(){
        this.dispose();
    }

    @Override
    public void updateCellControl(boolean isEditor, boolean enabled,
            boolean editable, Object data, Params params) {
        
        if(params != null){
            useParameters(XMLAuthoringToolSelectionDialog.stipBadPrefix(params));
        }

        //change the absolute file path into a relative one, relative to the 'relativeToDirectory' value
        File selectedFolderFile = FileChooserHelper.showSelectDirectoryDialog(startingDirectory.getAbsolutePath());
        selectedFolderRelativePath = FileFinderUtil.getRelativePath(relativeToDirectory, selectedFolderFile);
        
        //force the invisible dialog to close, causing XML editor library to save the current id value as the node's value
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                close();
                
            }
        });
    }
    
    /**
     * Use any of the parameters from the schema for this dialog.
     * 
     * @param params contains the parameters from the schema for the element using this custom dialog
     */
    private void useParameters(Params params){
        
        if(params.getMap() != null){
            //use parameters to customize dialog
            
            Map<?,?> paramsMap = params.getMap();
            
            if(paramsMap.containsKey(STARTING_DIRECTORY)){
                String startingDirectoryName = (String) paramsMap.get(STARTING_DIRECTORY);
                
                //check value
                File startingDirectoryTemp = new File(startingDirectoryName);
                if(startingDirectoryTemp.exists()){
                    startingDirectory = startingDirectoryTemp;
                }else{
                    logger.error("Unalbe to use the "+STARTING_DIRECTORY+" parameter value of "+startingDirectoryName+" because the full path of "+
                            startingDirectoryTemp.getAbsolutePath()+" doesn't exist.  Please make sure the value is relative to the GIFT named folder.");
                }
            }
            
            if(paramsMap.containsKey(RELATIVE_TO_DIRECTORY)){
                String relativeToDirectoryName = (String) paramsMap.get(RELATIVE_TO_DIRECTORY);
                
                //check value
                File relativeToDirectoryTemp = new File(relativeToDirectoryName);
                if(relativeToDirectoryTemp.exists()){
                    relativeToDirectory = relativeToDirectoryTemp;
                }else{
                    logger.error("Unalbe to use the "+RELATIVE_TO_DIRECTORY+" parameter value of "+relativeToDirectoryName+" because the full path of "+
                            relativeToDirectoryTemp.getAbsolutePath()+" doesn't exist.  Please make sure the value is relative to the GIFT named folder.");
                }
            }
        }
        
    }

}
