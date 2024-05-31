/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;


/**
 * This is the base class for a custom dialog that populate a list of authored strategy names for selection.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractStrategyReferenceDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;

    /** dialog components */
    private static final String TITLE = "Strategy Reference";
    private static final String INFO = "Please select an exsting strategy to use as a choice.\n\nNote: A strategy is referenced by it's unique name, therefore if a strategy has not been given a name\nit will not show up in the list to select from.";
    
    private static List<SelectionItem> DEFAULT_VALUES = new ArrayList<>();
    
    /** xml schema elements */
    private static final String STRATEGY = "strategy";
    private static final String NAME = "name";
    
    /**
     * Class constructor - create dialog
     */
    public AbstractStrategyReferenceDialog(){
        super(TITLE, INFO, DEFAULT_VALUES, false);
        
    }
    
    /**
     * Populate the list with instructional strategy references.
     * 
     * @param rootNode the root node of the xml document
     * @param items the collection to add strategy names to
     */
    protected void populateStrategies(Element rootNode, List<String> items){
        
        //
        // gather all strategy names
        //  
        
        NodeList nl = rootNode.getElementsByTagName(STRATEGY);
        if(nl != null && nl.getLength() >= 1){
            //found list of strategies, add strategy names to list
            
            for(int i = 0; i < nl.getLength(); i++){
                
                Element strategy = (Element)nl.item(i);
                String name = strategy.getAttribute(NAME);
                if(name != null && name.length() > 0){
                    //must have a value for the name
                    
                    items.add(name);
                }
            }//end for
            
        }else{
            JOptionPane.showMessageDialog(this,
                    "There are no authored strategies yet, therefore the list of strategy names will be empty.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void addUserEntry(String value) {
        // does not support user history of values        
    }
}
