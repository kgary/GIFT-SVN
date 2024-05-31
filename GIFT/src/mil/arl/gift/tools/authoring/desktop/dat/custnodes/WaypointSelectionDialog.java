/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This custom dialog provides the list of DKF waypoints already authored.
 * 
 * @author mhoffman
 *
 */
public class WaypointSelectionDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(WaypointSelectionDialog.class);

    /** title of the dialog */
    private static final String TITLE = "Waypoint Selection";
    
    private static final String LABEL = "Please select from an existing waypoint from the list of authored waypoints in this DKF.";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static final String WAYPOINT = "waypoint";
    private static final String NAME = "name";
    
    /**
     * Class constructor
     */
    public WaypointSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }

    @Override
    public Object[] getCustomValues() {
        
        List<String> items = new ArrayList<>();        
        
        try{
            //
            // gather all concept performance node Ids that are a child to the selected Task
            //            
            Element scenarioNode = DAT.getInstance().getDATForm().getRootNode(); 
            
            NodeList nl = scenarioNode.getElementsByTagName(WAYPOINT);
            for(int i = 0; i < nl.getLength(); i++){
                
                Element waypoint = (Element)nl.item(i);
                String name = waypoint.getAttribute(NAME);
                items.add(name);
            }
            
            Collections.sort(items);
            
        }catch(Throwable e){
            logger.error("Caught exception while gathering waypoints", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather waypoints, check the DAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);            
        }
        
        return items.toArray();
    }

    @Override
    public void addUserEntry(String value) {
        // user history not supported for this dialog
    }

}
