/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat.custnodes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import mil.arl.gift.tools.authoring.desktop.cat.CAT;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.AbstractStrategyReferenceDialog;

/**
 * This class is a custom dialog that populate a list of authored strategy names for selection in the DAT.
 * 
 * @author mhoffman
 *
 */
public class StrategyReferenceDialog extends AbstractStrategyReferenceDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(StrategyReferenceDialog.class);
    
    /**
     * Class constructor - create dialog
     */
    public StrategyReferenceDialog(){
        super();
        
    }

    @Override
    public Object[] getCustomValues() {
        
        List<String> items = new ArrayList<>();        
        
        try{
            //
            // gather all strategy names
            //  
            Element rootNode = CAT.getInstance().getCATForm().getRootNode(); 
            
            populateStrategies(rootNode, items);           
        
        }catch(Throwable e){
            logger.error("Caught exception while gathering strategy names", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather strategy names, check the DAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return items.toArray();
    }

}
