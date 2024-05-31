/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.Params;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFileSelectionDialog;

/**
 * This is the XML Editor custom node dialog for a file selection element in the course file.
 * The dialog allows the user to select a file whose name will appear as the value of an element.
 * Furthermore this dialog has customizable elements such as the dialog's title, information text and file extension type to filter on.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractConfigurableFileSelectionDialog extends XMLAuthoringToolFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractConfigurableFileSelectionDialog.class);
    
    /** default title of the dialog if a custom one is not provided */
    private static final String DEFAULT_TITLE = "File Selection";
    
    /** default information text if custom content is not provided */
    private static final String DEFAULT_LABEL = "Please select the appropriate file.\n";
    
    /** parameter keys in the schema */
    private static final String TITLE = "title";
    private static final String EXTENSION = "extension";
    private static final String INFO = "information";
    
    /** a delimeter to use for multiple extensions in the extension parameter */
    private static final String EXTENSION_DELIM = "\\|";

    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /**
     * Class constructor - create dialog
     */
    public AbstractConfigurableFileSelectionDialog(){
        super(DEFAULT_TITLE, DEFAULT_LABEL, DEFAULT_VALUES, false, null, CommonProperties.getInstance().getWorkspaceDirectory());

    }
    
    /**
     * Get the course folder from the desktop authoring tool to filter the files
     * shown to the user to those in the course folder for this dialog instance.
     * 
     * @return the course folder.  Can be null if the course folder wasn't found or the user 
     * decided not to save the XML file being authored.
     * @throws DetailedException if there was a problem retrieving the course folder from the XML authoring tool
     */
    public abstract File getCourseFolder() throws DetailedException;
    
    @Override
    protected String processFileName(String fileName) throws DetailedException{
        
        if(fileName != null){
            File courseFolder = getCourseFolder();
            if(courseFolder != null){
                return FileFinderUtil.getRelativePath(courseFolder, new File(fileName));
            }
        }
        
        return null;
    }
    
    @Override
    protected void useParameters(Params params){
        
        if(params.getMap() != null){
            //use parameters to customize dialog
            
            Map<?,?> paramsMap = params.getMap();
            
            if(paramsMap.containsKey(TITLE)){
                this.setTitle((String)paramsMap.get(TITLE));
            }
            
            if(paramsMap.containsKey(EXTENSION)){
                this.setFileExtension(((String)paramsMap.get(EXTENSION)).split(EXTENSION_DELIM));
            }else{
                this.setFileExtension(null);
            }
            
            if(paramsMap.containsKey(INFO)){
                this.setHelpfulInformation((String)paramsMap.get(INFO));
            }
        }
    }

    @Override
    public Object[] getCustomValues() {
        
        List<String> values = new ArrayList<>();
        try{
            File courseFolder = getCourseFolder();
            if(courseFolder != null){
                values = CommonUtil.getRelativeFileNamesByExtensions(courseFolder, getFileExtension());
            }
        }catch(Throwable e){
            logger.error("There was a problem trying to find all the course folder files with the extensions of "+getFileExtension(), e);
            e.printStackTrace();
        }
        
        return values.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        // currently don't support user history for this dialog, therefore nothing to do        
    }

}
