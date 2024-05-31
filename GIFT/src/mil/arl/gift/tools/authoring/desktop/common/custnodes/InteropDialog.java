/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.tools.authoring.common.util.CourseUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolClassFileSelectionDialog;

/**
 * This is the XML Editor custom node dialog for the Interop implementation class element in the course schema file.
 * The dialog allows the user to specify which gateway module interop interface class to use in a course file being developed using the CAT.
 * 
 * @author mhoffman
 *
 */
public class InteropDialog extends XMLAuthoringToolClassFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(InteropDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Interop Interface Implementation";
    
    private static final String LABEL = "Please select an entry.\n" +
    		"To specify your own implementation of a Gateway Module interop interface class,\n" +
    		" please provide the class path from the mil.arl.gift package, followed by the class name.\n" +
    		"(e.g. gateway.interop.condition.DISInterface)\n" +
    		"Providing a custom class parameter allows a GIFT developer to create their own implementation class while still using authored 'input' configuration parameters.\n"+
    		"Note: the user history entries are stored in GIFT/config/tools/<toolname>/UserHistory.txt";
    
    /** last known list of user specified interop interface implementation class names */
    private List<String> interopHistory;

    private static List<SelectionItem> DEFAULT_ENTRIES = new ArrayList<>();
    static{
        
        try{
            List<String> values = CourseUtil.getInteropImplementations();
            for(String value : values){
                SelectionItem item = new SelectionItem(value, null);
                DEFAULT_ENTRIES.add(item);
            }
        
        }catch(Exception e){
            System.out.println("Unable to populate the default list of interops because of the following exception.");
            e.printStackTrace();
            logger.error("Caught exception while trying to populate the default list of interops.", e);
        }
        
        Collections.sort(DEFAULT_ENTRIES);
    }
    

    
    /**
     * Class constructor - create dialog
     */
    public InteropDialog(){
        super(TITLE, LABEL, DEFAULT_ENTRIES);

    }

    @Override
    public String[] getCustomValues() {
        
        String[] userHistory = null;
        
        if(interopHistory == null){
            interopHistory = new ArrayList<>();
        }
        
        try{
            
            //get user history for the attribute
            userHistory = UserHistory.getInstance().getPropertyArray(UserHistory.INTEROP);
            
            if(userHistory != null){
                
                interopHistory.clear();                
                interopHistory.addAll(Arrays.asList(userHistory));
            }
            
        }catch(Exception e){
            System.out.println("There was an issue trying to read the user history, therefore the Interop dialog will not be correctly populated");
            e.printStackTrace();
        }
        
        return interopHistory.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        
        try {
            //add new user provided entry to the collection of user's historic entries
            interopHistory.add(value);            
            
            //write the new property value
            UserHistory.getInstance().setProperty(UserHistory.INTEROP, interopHistory.toArray(new String[0]));
            
        } catch (Exception e) {
            System.out.println("Caught exception while trying to save the custom entry");
            e.printStackTrace();
        }
        
    }

}
