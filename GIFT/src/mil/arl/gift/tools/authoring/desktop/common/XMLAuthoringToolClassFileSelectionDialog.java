/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common;

import java.util.List;

import mil.arl.gift.common.PackageUtil;

/**
 * This class enables a dialog for selecting a Java class file whose name will help populate
 * the appropriate XML element in the authoring tool.
 * 
 * @author mhoffman
 *
 */
public abstract class XMLAuthoringToolClassFileSelectionDialog extends
        XMLAuthoringToolFileSelectionDialog {
    
	private static final long serialVersionUID = 1L;

	/** java file extension */
    private static final String[] JAVA_EXTENSION = {"java"};
    
    /** search and replace strings for file classpath names*/
    private static final String CLASSPATH_PREFIX = "\\mil\\arl\\gift\\";
    private static final String SLASH = "\\";
    private static final String PERIOD = ".";

    /** directory location to start the java file browser */
    private static final String SOURCE_BROWSE_START_LOCATION = PackageUtil.getSource() + CLASSPATH_PREFIX;

    /**
     * Class constructor - create and show dialog
     * 
     * @param title - the text to display as the title of the dialog
     * @param label - the text to display for the information label on the dialog
     * @param defaultValues - the default values to populate the combo box with
     */
    public XMLAuthoringToolClassFileSelectionDialog(String title, String label,
            List<SelectionItem> defaultValues) {
        this(title, label, defaultValues, true);        
        
    }
    
    /**
     * Class constructor - create and show dialog
     * 
     * @param title - the text to display as the title of the dialog
     * @param label - the text to display for the information label on the dialog
     * @param defaultValues - the default values to populate the combo box with
     * @param allowUserEntries - whether to allow the user to manually type in their own values into the combobox
     */
    public XMLAuthoringToolClassFileSelectionDialog(String title, String label,
            List<SelectionItem> defaultValues, boolean allowUserEntries) {
        super(title, label, defaultValues, allowUserEntries, JAVA_EXTENSION, SOURCE_BROWSE_START_LOCATION);        

    }
    
    @Override
    protected String processFileName(String fileName) {
       
        String classPath = null;
            
        //strip everything before and including mil/arl/gift/
        int classPathBeginIndex = fileName.indexOf(CLASSPATH_PREFIX);
        String fileSubPath = fileName.substring(classPathBeginIndex + CLASSPATH_PREFIX.length());
        
        //strip file extension
        int extensionBeingIndex = fileSubPath.indexOf(JAVA_EXTENSION[0]);
        classPath = fileSubPath.substring(0, extensionBeingIndex-1);
        
        //replace slashes with periods to match format in classpath entries of authoring tool xml files
        classPath = classPath.replace(SLASH, PERIOD);

        return classPath;
    }
    
    /**
     * Return a class name without the GIFT package prefix (i.e. mil.arl.gift).
     * 
     * @param clazz - the class to return a formated class name for
     * @return String - the class name without the GIFT package prefix
     */
    public static String formatClassName(Class<?> clazz){
        return clazz.getName().replaceFirst("mil.arl.gift.", "");
    }

}
