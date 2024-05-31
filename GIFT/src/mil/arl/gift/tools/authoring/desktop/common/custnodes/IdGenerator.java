/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common.custnodes;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.fg.ftreenodes.ICellControl;
import com.fg.ftreenodes.Params;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This is the XML editor custom dialog for unique Id's that are incremented with values starting at 1.
 * 
 * @author mhoffman
 *
 */
public class IdGenerator extends JDialog implements ICellControl {
    
    private static final long serialVersionUID = 1L;
    
    /** the current global id value */
    private static Integer SHARED_ID = 0;
    
    /** map of id key to the current id value - this allows common nodes to use the same sequence of ids */
    private static Map<String, Integer> idKeyToCurrentValue = new HashMap<>();
    
    /** parameter keys in the schema */
    private static final String KEY = "idKey";
    
    /** 
     * Set a new value for the min global id value.  This is useful for when an XML file is loaded
     * which already contains id's and the next node to need an id should increment off the highest id value
     * in that file.
     * 
     * @param value a new min global id value
     */
    public static void setGlobalId(int value){
        synchronized(SHARED_ID){
            SHARED_ID = value;
        }
    }
    
    public static void setGlobalId(int value, String key){
        synchronized(idKeyToCurrentValue){
            idKeyToCurrentValue.put(key, value);
        }
    }
    
    /** the id of the current rendered node */
    private Integer id = 0;
    
    /**
     * Class constructor 
     */
    public IdGenerator(){
        
        //make sure the actual dialog is never visible
        this.setSize(0, 0);
    }
    
    /**
     * Set the id to use next by using the next value mapped to a unique key in the parameters map.
     * If no value is found for the key, then return false.  
     * 
     * @param paramsMap - contains xml parameters for the element using this custom dialog.
     * @return boolean - true iff the next unique id was updated from the parameter map information.
     */
    private boolean useKeyParameter(Map<?,?> paramsMap){
                    
        if(paramsMap != null && paramsMap.containsKey(KEY)){
            Integer currIdValue = idKeyToCurrentValue.get(paramsMap.get(KEY));
            
            //first time use of this key, initialize the object that will contain the id
            if(currIdValue == null){
                currIdValue = Integer.valueOf(0);                    
            }
            
            //use the next id
            id = ++currIdValue;
            
            idKeyToCurrentValue.put((String)paramsMap.get(KEY), currIdValue);
            
            return true;
        }
        
        return false;
    }

    @Override
    public Object getData() {
        return id.toString();
    }

    @Override
    public void initCellControl(boolean arg0) {

    }
    
    /**
     * Dispose of the dialog (which is never visible in the first place).
     * This method causes the "getData" method to be called by XML editor library.  So far this 
     * is the easiest way to cause the id value to be used and actually provided as a non-null, non-empty data
     * value when "updateCellControl" is called upon.
     */
    private void close(){
        this.dispose();
    }

    @Override
    public void updateCellControl(boolean isEditor, boolean enabled,
            boolean editable, Object data, Params params) {  

        //if the cell's data value is not populated yet, increment the global id and set it to the current id for this node
        if(data == null || data == ""){
            
            Map<?,?> paramsMap = XMLAuthoringToolSelectionDialog.stipBadPrefix(params).getMap();
            if(!useKeyParameter(paramsMap)){           
                synchronized(SHARED_ID){
                    id = ++SHARED_ID;
                }
            }
            
//            System.out.println("updateCellControl w/ "+id);            
        }else{
            //the cell already has a value, no need to increment it
            if(data instanceof String){
                id = Integer.valueOf((String)data);
            }else{
                id = (Integer) data;
            }
        }
        
        //force the invisible dialog to close, causing XML editor library to save the current id value as the node's value
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                close();
                
            }
        });
    }
}
