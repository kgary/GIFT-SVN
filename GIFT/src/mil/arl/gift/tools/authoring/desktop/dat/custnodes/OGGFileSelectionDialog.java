/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.AbstractConfigurableFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adamb.vorbis.VorbisIO;

/**
 * This custom dialog is responsible for allowing the user to specify an OGG audio file.  This class
 * also makes sure the OGG file provided has the correct codec of Vorbis.
 * 
 * @author mhoffman
 *
 */
public class OGGFileSelectionDialog extends AbstractConfigurableFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(OGGFileSelectionDialog.class);

    /**
     * Class constructor
     */
    public OGGFileSelectionDialog(){
        super();
    }
    
    @Override
    public Object[] getCustomValues() {
        
        if(this.getFileExtension() != null){
        
            //get default items from generic file selection by extension and then validate them
            List<Object> items = Arrays.asList(super.getCustomValues()); 
            
            File courseFolder;
            try {
                courseFolder = getCourseFolder();
            } catch (DetailedException e1) {
                logger.error("Unable to get the course folder.", e1);
                return new Object[0];
            }
            
            List<Object> removeThese = new ArrayList<>();
            for(Object item : items){
                
                String fileName = (String) item;
                
                try {
                    
                    if(courseFolder != null){
                        checkVorbis(courseFolder.getCanonicalPath() + File.separator + fileName);
                    }
                 } catch (@SuppressWarnings("unused") IOException e) {
                     //add to list of values to remove
                     removeThese.add(item);
                 }
            }
            
            items.removeAll(removeThese);
            
            return items.toArray(new Object[0]);
        }else{
            return new Object[0];
        }
    }
    
    @Override
    protected String processFileName(String fileName) throws DetailedException{
        
        //first make sure the OGG file has the appropriate codec - Vorbis
        try{
            checkVorbis(fileName);
                
        }catch (FileNotFoundException fnf){
            logger.error("Caught exception while trying to verify the OGG codec", fnf);
            
            JOptionPane.showMessageDialog(this,
                    "Unable to find the OGG file named "+fileName+", check the DAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
            return null;
            
        }catch (IOException ioe){
            logger.error("The OGG file "+fileName+" has the incorrect codec.  Looking for Vorbis.  Please fix the OGG file before trying to use it in GIFT.", ioe);
            
            JOptionPane.showMessageDialog(this,
                    "The OGG file "+fileName+" has the incorrect codec.  \nLooking for Vorbis.  Please fix the OGG file before trying to use it in GIFT.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            
            return null;
        }
        
        return super.processFileName(fileName);
    }
    
    /**
     * Check whether the file name provided for an OGG audio file has the Vorbis codec.
     * 
     * @param fileName - the filename of an OGG file
     * @throws IOException - if the OGG file is not found or doesn't have the Vorbis codec.
     */
    private static void checkVorbis(String fileName) throws IOException{
        
        File oggFile = new File(fileName);
        VorbisIO.readComments(oggFile);
    }

    @Override
    public File getCourseFolder() throws DetailedException {
        return DAT.getInstance().getDATForm().updateCourseFolder();
    }
}
