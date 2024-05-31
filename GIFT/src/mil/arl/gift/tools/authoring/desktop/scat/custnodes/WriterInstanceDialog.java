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
 * This is the XML Editor custom node dialog for the Writer instance id reference element in the sensor configuration schema.
 * The dialog allows the user to specify which writer instance Id from the list of writers to use in a sensor configuration file being developed using the SCAT.
 * 
 * @author mhoffman
 *
 */
public class WriterInstanceDialog extends XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.getLogger(WriterInstanceDialog.class);

    /** title of the dialog */
    private static final String TITLE = "Writer Instance Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing Writer instance entry from the writers list.";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    private static final String WRITERS = "Writers";
    private static final String WRITER  = "Writer";
    private static final String ID      = "id";
    private static final String NAME    = "name";
    
    //the following block checks to make sure the class structure of Writer from the xsd hasn't changed
    //as this class uses that structure to to find node elements in the tree.
    //If changes are made to this structure or the names of class fields, then the code that walks/searches nodes in this class
    //will need to be changed accordingly.
    static{       
          generated.sensor.Writer writer = new generated.sensor.Writer();
          writer.getId();
          writer.getName();
          generated.sensor.Writers writers = new generated.sensor.Writers();
          writers.getWriter();
    }
    
    /**
     * Class constructor - create dialog
     */
    public WriterInstanceDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object getData(){
        
        if(selectedValue != null && selectedValue instanceof WriterItem){
            int perfNodeId = ((WriterItem)selectedValue).getId();
            return perfNodeId;
        }
        
        return selectedValue;
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<WriterItem> items = new ArrayList<>();        
          
        try{
            //
            // gather all writers from list
            //
            Element rootNode = SCAT.getInstance().getSCATForm().getRootNode();
            
            NodeList nl = rootNode.getElementsByTagName(WRITERS);
            if(nl != null && nl.getLength() == 1){
                //found writers element, the root of the writers list
                
                Element writers = (Element)nl.item(0);
                
                nl = writers.getElementsByTagName(WRITER);
                if(nl != null){
                    
                    //get info for each Writer
                    for(int writerIndex = 0; writerIndex < nl.getLength(); writerIndex++){
                        
                        Element writer = (Element)nl.item(writerIndex);
                        String idStr = writer.getAttribute(ID);
                        if(idStr == null || idStr.length() == 0){
                            logger.warn("While searching for writer ids, skipping a writer because it's id is not set yet");
                            continue;
                        }
                        int id = Integer.valueOf(idStr);
    
                        String name;
                        NodeList nameNL = writer.getElementsByTagName(NAME);
                        if(nameNL != null && nameNL.getLength() == 1){
                            Element nameElement = (Element)nameNL.item(0);
                            name = nameElement.getFirstChild().getNodeValue();
                        }else{
                            logger.error("Unable to get writer name, therefore skipping writer with id = "+id);
                            continue;
                        }                    
                        
                        items.add(new WriterItem(id, name));
    
                    }//end for
                }
                
            }
        }catch(Throwable e){
            logger.error("Caught exception while gathering writer ids", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather writer ids, check the SCAT log for more details",
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
     * Wrapper class for Writer information.  This class is responsible for 
     * handling the toString implementation used to display a string representing an instance of this class on the dialog.
     * 
     * @author mhoffman
     *
     */
    public class WriterItem implements Comparable<WriterItem>{
        
        int id;
        String name;
        
        public WriterItem(int id, String name){
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
        public int compareTo(WriterItem otherWriterItem) {
            
            if(otherWriterItem == null){
                return -1;
            }else{
                
                if(getId() < otherWriterItem.getId()){
                    return -1;
                }else if(getId() > otherWriterItem.getId()){
                    return 1;
                }
            }
            
            return 0;
        }
    }    
}
