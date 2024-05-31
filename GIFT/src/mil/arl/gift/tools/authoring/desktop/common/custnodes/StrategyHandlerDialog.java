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

import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerInterface;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolClassFileSelectionDialog;

/**
 * This is the XML Editor custom node dialog for the Strategy Handler implementation class element in the DKF schema file.
 * The dialog allows the user to specify which handler class to use in a DKF file being developed using the DAT.
 * 
 * @author mhoffman
 *
 */
public class StrategyHandlerDialog extends XMLAuthoringToolClassFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(StrategyHandlerDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Strategy Handler Implementation";
    
    /** user history keys needed */
    private static final String STRATEGY = "strategy";
    
    private static final String LABEL = "Please select an entry.\n" +
    		"To specify your own implementation of a Domain Module Strategy Handler class,\n" +
    		" please provide the class path from the mil.arl.gift package, followed by the class name.\n" +
    		"(e.g. domain.knowledge.strategy.DefaultStrategyHandler)\n" +
    		"Providing a custom class parameter allows a GIFT developer to create their own implementation class while still using authored 'input' configuration parameters.\n"+
    		"Note: the user history entries are stored in GIFT/config/tools/<toolname>/UserHistory.txt";
    
    /** last known list of user specified implementation class names */
    private List<String> history;

    private static List<SelectionItem> DEFAULT_ENTRIES = new ArrayList<>();
    static{
        
        try{
            String packageName = "mil.arl.gift.domain.knowledge.strategy";
            List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, StrategyHandlerInterface.class);
            for(Class<?> clazz : classes){
                
                //remove package prefix of "mil.arl.gift."   
                SelectionItem item = new SelectionItem(formatClassName(clazz), null);
                DEFAULT_ENTRIES.add(item);
            }
        
        }catch(Exception e){
            System.out.println("Unable to populate the default list of strategy handlers because of the following exception.");
            e.printStackTrace();
            logger.error("Caught exception while trying to populate the default list of strategy handlers.", e);
        }
        
        Collections.sort(DEFAULT_ENTRIES);
    }
    
    /**
     * Class constructor - create dialog
     */
    public StrategyHandlerDialog(){
        super(TITLE, LABEL, DEFAULT_ENTRIES);
    }

    @Override
    public String[] getCustomValues() {
        
        String[] userHistory = null;
        
        if(history == null){
            history = new ArrayList<String>();
        }
        
        try{
            
            //get user history for the attribute
            userHistory = UserHistory.getInstance().getPropertyArray(STRATEGY);
            
            if(userHistory != null){
                
                history.clear();                
                history.addAll(Arrays.asList(userHistory));
            }
            
        }catch(Exception e){
            System.out.println("There was an issue trying to read the user history, therefore the Strategy Handler dialog will not be correctly populated");
            e.printStackTrace();
        }
        
        return history.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        
        try {
            //add new user provided entry to the collection of user's historic entries
            history.add(value);            
            
            //write the new property value
            UserHistory.getInstance().setProperty(STRATEGY, history.toArray(new String[0]));
            
        } catch (Exception e) {
            System.out.println("Caught exception while trying to save the custom entry");
            e.printStackTrace();
        }
        
    }

}
