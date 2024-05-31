/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Shortcuts for displaying swing dialogs to the user
 *
 * @author jleonard
 */
public class FileChooserHelper {
    
    /**
     * Display a file selection dialog that is configured to allow only directories to be selected.
     * 
     * @param startingDirectory the directory to start the dialog in.  Null will not be used.
     * @return File the directory selected, null if none was chosen
     */
    public static File showSelectDirectoryDialog(String startingDirectory){
        
        JFileChooser fc = new JFileChooser();
        
        if(startingDirectory != null){
            fc.setCurrentDirectory(new File(startingDirectory));
        }
        
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int ret = fc.showOpenDialog(fc);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        
        return null;
    }

    /**
     * Displays a file selection dialog for any file on the system
     *
     * @return File The file that was selected, null if none was chosen
     */
    public static File showSelectFileDialog() {
        JFileChooser fc = new JFileChooser();

        int ret = fc.showOpenDialog(fc);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        
        return null;
    }

    /**
     * Displays a file selection dialog for any file on the system, starting in
     * some defined directory
     *
     * @param directory The directory to start the dialog in
     * @return File The file that was selected, null if none was chosen
     */
    public static File showSelectFileDialog(String directory) {
        JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File(directory));

        int ret = fc.showOpenDialog(fc);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        
        return null;
    }

    /**
     * Displays a file selection dialog for files with a specific extension,
     * starting in some defined directory
     *
     * @param directory The directory to start the dialog in
     * @param extensions a list of extensions to filter on.  A null extension means no filtering on file extension 
     *          will take place, i.e. like using "All Files".
     * @return File The file that was selected, null if none was chosen
     */
    public static File showSelectFileDialog(String directory, final String... extensions) {
        JFileChooser fc = new JFileChooser();

        fc.setCurrentDirectory(new File(directory));
        
        if(extensions != null){

            fc.setFileFilter(new FileFilter() {
    
                @Override
                public boolean accept(File file) {
                    
                    if (file.isDirectory()) {
                        return true;
                        
                    }  else if(extensions != null) {
                        
                        for(String extension : extensions){

                            if(file.getName().endsWith(extension)) {
                                return true;
                            }
                        }
                    }
                    
                    return false;
                }
    
                @Override
                public String getDescription() {
                    
                    String description = "*";
                    if(extensions != null){
                        for(int i = 0; i < extensions.length; i++){
                            description += "." + extensions[i];
                            
                            if(i + 1 < extensions.length){
                                description += "|";
                            }
                        }
                    }
                    
                    return description;
                }
            });
            fc.setAcceptAllFileFilterUsed(false);
        }

        int ret = fc.showOpenDialog(fc);
        if (ret == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        
        return null;
    }
}
