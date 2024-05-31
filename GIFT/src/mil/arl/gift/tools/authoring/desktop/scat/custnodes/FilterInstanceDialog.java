/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.scat.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.scat.SCAT;

/**
 * This is the XML Editor custom node dialog for the Filter instance id reference element in the sensor configuration schema.
 * The dialog allows the user to specify which filter instance Id from the list of filters to use in a sensor configuration file being developed using the SCAT.
 * 
 * @author mhoffman
 *
 */
public class FilterInstanceDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.getLogger(FilterInstanceDialog.class);

    /** title of the dialog */
    private static final String TITLE = "Filter Instance Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing Filter instance entry from the filters list.";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static final String FILTERS = "Filters";
    private static final String FILTER  = "Filter";
    private static final String ID      = "id";
    private static final String NAME    = "name";
    
    //the following block checks to make sure the class structure of Filter from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{       
          generated.sensor.Filter filter = new generated.sensor.Filter();
          filter.getId();
          filter.getName();
          generated.sensor.Filters filters = new generated.sensor.Filters();
          filters.getFilter();
    }
    
    /**
     * Class constructor - create dialog
     */
    public FilterInstanceDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof FilterItem){
            int perfNodeId = ((FilterItem)selectedValue).getId();
            return perfNodeId;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<FilterItem> items = new ArrayList<>();        
            
        try{
            //
            // gather all filters from list
            //
            Element rootNode = SCAT.getInstance().getSCATForm().getRootNode();
            
            NodeList nl = rootNode.getElementsByTagName(FILTERS);
            if(nl != null && nl.getLength() == 1){
                //found filters element, the root of the filters list
                
                Element filters = (Element)nl.item(0);
                
                nl = filters.getElementsByTagName(FILTER);
                if(nl != null){
                    
                    //get info for each Filter
                    for(int filterIndex = 0; filterIndex < nl.getLength(); filterIndex++){
                        
                        Element filter = (Element)nl.item(filterIndex);
                        String idStr = filter.getAttribute(ID);
                        if(idStr == null || idStr.length() == 0){
                            logger.warn("While searching for filter ids, skipping a filter because it's id is not set yet");
                            continue;
                        }
                        int id = Integer.valueOf(idStr);
    
                        String filterName;
                        NodeList nameNL = filter.getElementsByTagName(NAME);
                        if(nameNL != null && nameNL.getLength() == 1){
                            Element nameElement = (Element)nameNL.item(0);
                            filterName = nameElement.getFirstChild().getNodeValue();
                        }else{
                            logger.error("Unable to get filter name, therefore skipping filter with id = "+id);
                            continue;
                        }                    
                        
                        items.add(new FilterItem(id, filterName));
    
                    }//end for
                }
                
            }
        }catch(Throwable e){
            logger.error("Caught exception while gathering filter ids", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather filter ids, check the SCAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        Collections.sort(items);
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        //no custom entries allowed for user history for this dialog implementation
    }
    
    /**
     * Wrapper class for Filter information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class FilterItem implements Comparable<FilterItem>{
        
        int id;
        String name;
        
        public FilterItem(int id, String name){
            this.id = id;
            this.name = name;
        }
        
        public int getId(){
            return id;
        }
        
        @Override
        public String toString(){
            return getId() + " " + name;
        }
        
        @Override
        public int compareTo(FilterItem otherFilterItem) {
            
            if(otherFilterItem == null){
                return -1;
            }else{
                
                if(getId() < otherFilterItem.getId()){
                    return -1;
                }else if(getId() > otherFilterItem.getId()){
                    return 1;
                }
            }
            
            return 0;
        }
    }    
}
