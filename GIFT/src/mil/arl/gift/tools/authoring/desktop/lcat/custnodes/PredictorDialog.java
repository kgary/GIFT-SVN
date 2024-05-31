/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.lcat.custnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.tools.authoring.common.util.LearnerConfigUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolClassFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.UserHistory;

/**
 * This is the XML Editor custom node dialog for the Predictor implementation class element in the learner config schema file.
 * The dialog allows the user to specify which predictor class to use in a learner configuration file being developed using the LCAT.
 * 
 * @author mhoffman
 *
 */
public class PredictorDialog extends XMLAuthoringToolClassFileSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PredictorDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Predictor Implementation";

    private static final String LABEL = "Please select an entry.\n" +
    		"To specify your own implementation of a Learner Module predictor class,\n" +
    		" please provide the class path from the mil.arl.gift package, followed by the class name.\n" +
    		"(e.g. learner.predictor.EngagementTwoStatePredictor)\n" +
    		"Note: the user history entries are stored in GIFT/config/tools/<toolname>/UserHistory.txt";
    
    /** last known list of user specified predictor implementation class names */
    private List<String> predictorHistory;

    private static List<SelectionItem> DEFAULT_ENTRIES = new ArrayList<>();   
    static{
        
        try{
            List<String> predictors = LearnerConfigUtil.getPredictorImplementations();
            for(String clazz : predictors){
                
                //remove package prefix of "mil.arl.gift." 
                SelectionItem item = new SelectionItem(clazz, null);
                DEFAULT_ENTRIES.add(item);
            }
        
        }catch(Exception e){
            System.out.println("Unable to populate the default list of predictors because of the following exception.");
            e.printStackTrace();
            logger.error("Caught exception while trying to populate the default list of predictors.", e);
        }
        
        Collections.sort(DEFAULT_ENTRIES);
    }
    
    /**
     * Class constructor - create dialog
     */
    public PredictorDialog(){
        super(TITLE, LABEL, DEFAULT_ENTRIES);

    }

    @Override
    public String[] getCustomValues() {
        
        String[] userHistory = null;
        
        if(predictorHistory == null){
            predictorHistory = new ArrayList<String>();
        }
        
        try{
            
            //get user history for the attribute
            userHistory = UserHistory.getInstance().getPropertyArray(UserHistory.PREDICTOR);
            
            if(userHistory != null){
                
                predictorHistory.clear();                
                predictorHistory.addAll(Arrays.asList(userHistory));
            }
            
        }catch(Exception e){
            System.out.println("There was an issue trying to read the user history, therefore the Predictor dialog will not be correctly populated");
            e.printStackTrace();
        }
        
        return predictorHistory.toArray(new String[0]);
    }

    @Override
    public void addUserEntry(String value) {
        
        try {
            //add new user provided entry to the collection of user's historic entries
            predictorHistory.add(value);            
            
            //write the new property value
            UserHistory.getInstance().setProperty(UserHistory.PREDICTOR, predictorHistory.toArray(new String[0]));
            
        } catch (Exception e) {
            System.out.println("Caught exception while trying to save the custom entry");
            e.printStackTrace();
        }
        
    }

}
