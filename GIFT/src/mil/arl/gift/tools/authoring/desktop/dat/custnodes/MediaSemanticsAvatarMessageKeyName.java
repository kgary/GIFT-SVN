/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fg.ftreenodes.FToggleNode;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolFormManager;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

import org.json.JSONException;

/**
 * This custom dialog is responsible for parsing the Media Semantics java script (.js) file that was created via the
 * Character builder application.  The js file contains the key name value that references logic to play an authored audio file.
 * 
 * @author mhoffman
 *
 */
public class MediaSemanticsAvatarMessageKeyName extends
        XMLAuthoringToolSelectionDialog {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MediaSemanticsAvatarMessageKeyName.class);

    /** custom title for the dialog */
    private static final String TITLE = "Select Avatar Message Key Name";
    
    /** helpful information displayed on the dialog */
    private static final String INFO = "Please select a message key that corresponds to the specific message the avatar will render.\n" +
    		"The values are directly from the selected Avatar .js file.";
    
    /** DKF schema node element labels */
    private static final String KEY_NAME = "key-name";
    private static final String AVATAR = "Avatar";
    private static final String MEDIA_SEMANTICS = "mediaSemantics";
    
    /** javascript and json related strings to parse for/on */
    private static final String HTML    = ".html";
    private static final String JS      = ".js";
    private static final String JS_STARTJSON = "{";
    private static final String JS_ENDJSON = "}";
    private static final String NAME    = "name";
    private static final String CHARACTER = "character";
    private static final String TYPE    = "type";
    private static final String ONFRAME = "onframe";
    
    private static final String FILES   = "_Files";
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    /**
     * Class constructor - show dialog
     */
    public MediaSemanticsAvatarMessageKeyName(){
        super(TITLE, INFO, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object[] getCustomValues() {
        
        List<String> items = new ArrayList<>();   
       
        try{

            FToggleNode selectedNode = XMLAuthoringToolFormManager.getInstance().getSelectedNode();
            if(selectedNode != null){
                
                if(selectedNode.getLabelText().equals(KEY_NAME)){
                    //found key-name element, now find the value of the sibling element name Avatar
                    
                    FToggleNode mediaSemanticsNode = (FToggleNode)selectedNode.getParent();
                    if(mediaSemanticsNode != null && mediaSemanticsNode.getLabelText().equals(MEDIA_SEMANTICS)){
                        //found media semantics node, get child Avatar node
                        
                        File courseFolder = DAT.getInstance().getDATForm().updateCourseFolder();
                        if(courseFolder != null){
                        
                            //iterate over all child elements 
                            for(int i = 0; i < mediaSemanticsNode.getChildCount(); i++){
                                
                                FToggleNode childNode = (FToggleNode) mediaSemanticsNode.getChildAt(i);
                                if(childNode.getLabelText().equals(AVATAR)){
                                    //found avatar element
                                    
                                    String avatarValue = (String)childNode.getValue();
                                    if(avatarValue != null && avatarValue.length() > 0){
                                        //have some value for the avatar file input element
                                        
                                        File avatarFile = new File(courseFolder + File.separator + avatarValue);
                                        if(avatarFile.exists()){
                                            //the avatar file exists, now find the js file of the same name
                                            
                                            String filename = avatarFile.getName();
                                            String jsFilename = filename.replace(HTML, JS);
                                            String path = avatarFile.getAbsolutePath().substring(0,avatarFile.getAbsolutePath().lastIndexOf(File.separator));
                                            File jsFile = new File(path + File.separator + filename.replace(HTML, FILES) + File.separator + jsFilename);
                                            if(jsFile.exists()){
                                                
                                                //get contents of entire file into string
                                                String jsStr = FileUtils.readFileToString(jsFile);
                                                
                                                //strip unwanted text
                                                int jsonStartIndex = jsStr.indexOf(JS_STARTJSON);
                                                int jsonEndIndex = jsStr.lastIndexOf(JS_ENDJSON);
                                                JSONObject jsonObject = new JSONObject(jsStr.substring(jsonStartIndex, jsonEndIndex+1));
    
                                                getKeyNames(jsonObject, items);
                                                
                                            }else{
                                                JOptionPane.showMessageDialog(this, "<html>Unable to find the related Avatar file of<br>"+jsFile.getAbsolutePath()+"<br>based on the provided avatar <br>file of<br>"+avatarValue+".<br><br>Therefore the list of key name values will not be provided.</html>",
                                                        "Media Semantics Avatar File Required", JOptionPane.ERROR_MESSAGE);
                                            }
                                            
                                        }else{
                                            JOptionPane.showMessageDialog(this, "<html>Unable to find the Avatar file of <br>"+avatarFile+".<br>Therefore the list of key name values will not be provided.</html>",
                                                    "Media Semantics Avatar File Required", JOptionPane.ERROR_MESSAGE);
                                        }
                                        
                                    }else{
                                        JOptionPane.showMessageDialog(this, "Please populate the Media Semantics Avatar value before attempting to select from it's key name values.",
                                                "Media Semantics Avatar File Required", JOptionPane.ERROR_MESSAGE);
                                    }
                                    
                                    //done with for loop
                                    break;
                                }
                                
                            }//end for loop        
                        }
                        
                    }else{
                        logger.error("Found the wrong selected node label - wanted "+MEDIA_SEMANTICS+", found "+selectedNode.getLabelText());
                        return null;
                    }
                    
                }//end if on found key-name element
                
            }else{
                logger.error("The selected node is null, therefore can't determine which Media Semantics Avatar message key name node to find values for");
                return null;
            }
            

        
        }catch(Throwable e){
            logger.error("Caught exception while gathering MSC Avatar message key names", e);
            
            //show error message dialog
            JOptionPane.showMessageDialog(this,
                    "There was an error caught while trying to gather MSC Avatar message key names, check the DAT log for more details",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        Collections.sort(items);
        return items.toArray();
    }
    
    /**
     * Recursively walk the JSON object to find the key name values that references authored characters.
     * 
     * @param object - the json object to parse
     * @param names - current list of key names from the json object
     */
    public void getKeyNames(JSONObject object, List<String> names){
        
        if(object == null){
            return;
        }

        @SuppressWarnings("unchecked")
        Iterator<String> iterator = object.keys();
        String type = null, name = null, character = null;
        while(iterator.hasNext()){

            String key = iterator.next();
            
            Object value;
            
            try {
                value = object.get(key);
            } catch (JSONException e) {
                logger.error("Caught exception while retrieving value for key of "+key+" from JSON object.  Skipping this key:value pair.", e);
                continue;
            }
            
            if(key.equals(TYPE)){
                //found a type
                type = (String) value;
                
            }else if(key.equals(NAME)){
                // found name
                name = (String) value;
                
            }else if(key.equals(CHARACTER)){
                // found character
                character = (String) value;
                
            }else if(!key.equals(ONFRAME)){
                //ignore onframe json object which contains a lot of un-needed info here
                
                if(value instanceof JSONObject){
                    
                    try{
                        JSONObject jsonObj = (JSONObject) value;
                        if(jsonObj.keys() != null){
                            getKeyNames(jsonObj, names); 
                        }
                    }catch(@SuppressWarnings("unused") Exception e){}
                }
            }
            
            if(type != null && name != null && character != null){
                //found key name pairing, add name value to list
                names.add(name);
                
                //don't add name again when there are still more objects to iterator over
                name = null;
                type = null;
                character = null;
            }

        }
    }

    @Override
    public void addUserEntry(String value) {
        // currently don't support user history for this dialog, therefore nothing to do 
    }

}
