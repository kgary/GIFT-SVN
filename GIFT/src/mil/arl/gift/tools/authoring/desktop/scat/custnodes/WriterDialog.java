/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.scat.custnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.tools.authoring.common.util.SensorConfigUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolClassFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.UserHistory;

/**
 * This is the XML Editor custom node dialog for the Writer implementation class element in the sensor config schema file.
 * The dialog allows the user to specify which writer class to use in a sensor configuration file being developed using the SCAT.
 * 
 * @author mhoffman
 *
 */
public class WriterDialog extends XMLAuthoringToolClassFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(WriterDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Writer Implementation";

    private static final String LABEL = "Please select an entry.\n" +
    		"To specify your own implementation of a Sensor Module writer class,\n" +
    		" please provide the class path from the mil.arl.gift package, followed by the class name.\n" +
    		"(e.g. sensor.writer.GenericSensorDelimitedWriter)\n" +
            "Providing a custom class parameter allows a GIFT developer to create their own implementation class while still using authored 'input' configuration parameters.\n"+
    		"Note: the user history entries are stored in GIFT/config/tools/<toolname>/UserHistory.txt";
    
    /** last known list of user specified writer implementation class names */
    private List<String> writerHistory;

    private static List<SelectionItem> DEFAULT_ENTRIES = new ArrayList<>();
    static{
        
        try{
            List<String> writers = SensorConfigUtil.getWriterImplementations();
            for(String clazz : writers){
                
                //remove package prefix of "mil.arl.gift."      
                SelectionItem item = new SelectionItem(clazz, null);
                DEFAULT_ENTRIES.add(item);
            }
        
        }catch(Exception e){
            System.out.println("Unable to populate the default list of sensor writers because of the following exception.");
            e.printStackTrace();
            logger.error("Caught exception while trying to populate the default list of sensor writers.", e);
        }
        
        Collections.sort(DEFAULT_ENTRIES);
    }
    
    /**
     * Class constructor - create dialog
     */
    public WriterDialog(){
        super(TITLE, LABEL, DEFAULT_ENTRIES);

    }

    @Override
    public String[] getCustomValues() {
        
        String[] userHistory = null;
        
        if(writerHistory == null){
            writerHistory = new ArrayList<String>();
        }
        
        try{
            
            //get user history for the attribute
            userHistory = UserHistory.getInstance().getPropertyArray(UserHistory.WRITER);
            
            if(userHistory != null){
                
                writerHistory.clear();                
                writerHistory.addAll(Arrays.asList(userHistory));
            }
            
        }catch(Exception e){
            System.out.println("There was an issue trying to read the user history, therefore the Writer dialog will not be correctly populated");
            e.printStackTrace();
        }
        
        return writerHistory.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        
        try {
            //add new user provided entry to the collection of user's historic entries
            writerHistory.add(value);            
            
            //write the new property value
            UserHistory.getInstance().setProperty(UserHistory.WRITER, writerHistory.toArray(new String[0]));
            
        } catch (Exception e) {
            System.out.println("Caught exception while trying to save the custom entry");
            e.printStackTrace();
        }
        
    }

}
