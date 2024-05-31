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
import org.w3c.dom.NodeList;

import mil.arl.gift.tools.authoring.desktop.cat.CAT;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This custom dialog will present the user with a list of concept names authored
 * at the course level.
 * 
 * @author mhoffman
 *
 */
public class CourseConceptSelectionDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CourseConceptSelectionDialog.class);
    
    /** title of the dialog */
    private static final String TITLE = "Course Concept";
    
    private static final String LABEL = "Please select one of the course concepts.\n" +
            "Note: The set of course level concepts needs to be authored prior to using this selection dialog.";
    
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    /**
     * Default constructor - create dialog
     */
    public CourseConceptSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    @Override
    public Object[] getCustomValues() {
        
        List<String> items = new ArrayList<>();
        
        try{
            //
            // gather all concepts
            //  
            Element rootNode = CAT.getInstance().getCATForm().getRootNode(); 
            
            NodeList nl = rootNode.getChildNodes();
            for(int i = 0; i < nl.getLength(); i++){
                
                if(nl instanceof Element){
                    
                    Element element = (Element)nl.item(i);
                    if(element.getNodeName().equals("concepts")){
                        //get concepts implementation (list or hierarchy)
                        
                        nl = element.getChildNodes();
                        if(nl != null && nl.getLength() == 1){
                            
                            element = (Element)nl.item(0);
                            if(element.getNodeName().equals("list")){
                                //gather list of concepts
                                
                                nl = element.getChildNodes();
                                for(int j = 0; j < nl.getLength(); j++){
                                    
                                    element = (Element)nl.item(j);
                                    
                                    String conceptValue = element.getAttribute("name");
                                    if(conceptValue != null && !conceptValue.isEmpty()){
                                        items.add(conceptValue);
                                    }
                                }
                                
                                
                            }else{
                                //must be concept hierarchy, walk the 'tree' in depth first fashion
                                
                                nl = ((Element) nl).getElementsByTagName("conceptNode");  //the depth first logic
                                for(int j = 0; j < nl.getLength(); j++){
                                    
                                    element = (Element)nl.item(j);
                                    String conceptValue = element.getAttribute("name");
                                    if(conceptValue != null && !conceptValue.isEmpty()){
                                        items.add(conceptValue);
                                    }
                                }
                            }

                            break;
                        }
                        
                        break;
                    }
                }
            }
            
        
        }catch(Throwable e){
            logger.error("Caught exception while gathering concept names", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather course concept names, check the CAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        if(items.isEmpty()){
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There are no course level concepts to choose from.  You need to populate the course concepts before attempting to select one of them.",
                    "Error - No course concepts to choose from",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        // not supported
    }

}
