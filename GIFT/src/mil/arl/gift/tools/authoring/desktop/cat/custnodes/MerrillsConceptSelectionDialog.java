/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat.custnodes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FToggleMutableNode;
import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This will present a dialog that allows the user to select a concept authored at the ancestor
 * Merrills branch point course transition.
 * 
 * @author mhoffman
 *
 */
public class MerrillsConceptSelectionDialog extends
        XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MerrillsConceptSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Merrill's Branch Course Transition Concept";
    
    private static final String LABEL = "Please select one of the course concepts.\n" +
            "Note: The set of Merrill's branch transition level concepts needs to be authored prior to using this selection dialog.";
    
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    public MerrillsConceptSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    @Override
    public Object[] getCustomValues() {
        
        List<String> items = new ArrayList<>();
        
        try{
            
            //
            // gather all concepts
            //              
            FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
            if(selectedNode != null){
                
                if(selectedNode.getLabelText().equals("name")){
                    //found name attribute, get 'Concepts' ancestor (name->ConceptQuestions->(array)->PresentSurvey->Recall->Quadrants sibling Concepts)
                    
                    TreeNode quadrantsNode = selectedNode.getParent().getParent().getParent().getParent().getParent();
                    TreeNode conceptsNode = quadrantsNode.getParent().getChildAt(1);
                    
                    @SuppressWarnings("unchecked")
                    Enumeration<? extends TreeNode> enumerator = conceptsNode.getChildAt(0).children();
                    while(enumerator.hasMoreElements()){
                        FToggleMutableNode childNode = (FToggleMutableNode)enumerator.nextElement();
                        
                        if(childNode.getValue() != null){
                            items.add((String) childNode.getValue());
                        }
                    }
                    
                    if(items.isEmpty()){
                        //show error message dialog
                        JOptionPane.showMessageDialog(this,
                                "There are no Merrill's branch transition level concepts to choose from.  You need to populate the Merrill's branch transition concepts before attempting to select one of them.",
                                "Error - No concepts to choose from",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            }
            
        }catch(Exception e){
            logger.error("Caught exception while gathering concept names", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather Merrill's branch concept names, check the CAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        // not supported
    }

}
