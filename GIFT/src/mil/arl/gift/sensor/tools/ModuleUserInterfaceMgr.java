/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.sensor.tools.ModuleUserInterfacePanel.GenericMessagePanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This manager handles communication (e.g. displaying messages that would normally be shown via 'System.out' calls)
 * between the user and GIFT through the appropriate user interface depending on how the module was started.  If the
 * module was started in Learner mode the interface will be a custom window, however if Power User mode is being used
 * the interface will be the standard command prompt window.  
 * 
 * @author mhoffman
 *
 */
public class ModuleUserInterfaceMgr {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(ModuleUserInterfaceMgr.class);
    
    /** the root panel created by this manager */
    private ModuleUserInterfacePanel panel;
    
    /** the frame containing the panel, used to control when/if the window is shown to the user */
    private JFrame frame;
    
    /** 
     * map of panels containing the interface between a specific piece of GIFT (e.g. Q Sensor implementation class) and the user 
     * 
     * key: unique title of the panel (e.g. tab title)
     * value: the panel associated with that tab
     */
    private Map<String, GenericMessagePanel> panelIdToPanel = new HashMap<>();
    
    /** collection of threads created to notify listeners of user input in a console (command prompt) window [ONLY]*/
    private List<Thread> consoleEventThreads = new ArrayList<>();
    
    /** flag used to indicate this manager is being asked to cleanup */
    private boolean cleaningUp = false;

    /** the singleton instance of this class */
    private static ModuleUserInterfaceMgr instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return ModuleUserInterfaceMgr the singleton instance
     */
    public static ModuleUserInterfaceMgr getInstance(){
        
        if(instance == null){
            instance = new ModuleUserInterfaceMgr();
        }
        
        return instance;
    }
    
    /**
     * Default (private) constructor
     */
    private ModuleUserInterfaceMgr(){
        
        //only create the panel if the console is not available
        if(System.console() == null){
            panel = new ModuleUserInterfacePanel();
        }
    }
    
    /**
     * Display the provided message to the user via the appropriate user interface based on the way the module
     * was started.
     * 
     * @param source a unique identifier of the source of the message (e.g. "Q Sensor").  This will be used to group
     *          messages from the same source together.
     * @param message the information to display to the user.  Can be empty but NOT null.
     * @param eventListener used for notification of a user response to the message.  Can be null if the caller doesn't want to be notified.
     * @throws Exception if there was a sever problem displaying the message.
     */
    public void displayMessage(String source, String message, final ModuleUserInterfaceEventListener eventListener) throws Exception{
        
        if(System.console() == null){
            //message will be displayed on a dialog instead of console window
            
            GenericMessagePanel panel = panelIdToPanel.get(source);
            if(panel == null){
                //a panel doesn't exist for the corresponding source, create one for it...
                panel = addGenericMessagePanel(source);
            }
            
            panel.setEventListener(eventListener);
            
            panel.addTextToGenericMessagePanel(message);
            
            logger.info("Displaying message on panel: "+message);
            
        }else{
            //display message on console
            
            logger.info("Displaying message on console: "+message);
            
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(source).append("): ").append(message);
            System.out.println(sb.toString());
            
            if(eventListener != null){
                
                //spawn a thread to wait and read for a console window response, thereby releasing the calling thread.
                Thread t = new Thread("UserInterfaceResponse"){
                    
                    @Override
                    public void run(){
                        
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                        String input = null;
                        
                        try {
                            input = inputReader.readLine();
                            eventListener.textEntered(input);
                        } catch (IOException e) {
                            
                            if(!cleaningUp){
                                logger.error("Caught exception while trying to read your response from the console.", e);
                                eventListener.errorOccurred("Failed to read the user's response from the command prompt.");
                            }
                        }

                    }
                };
                
                consoleEventThreads.add(t);
                t.start();                

            }
        }
    }
    
    /**
     * Create a generic message panel which consists of a text area and a text field.
     * 
     * @param tabLabel a unique label for the tab that will contain the panel.
     * @return GenericMessagePanel the panel created
     */
    private GenericMessagePanel addGenericMessagePanel(String tabLabel){
        
        if(panelIdToPanel.containsKey(tabLabel)){
            throw new IllegalArgumentException("There is already a tab with the label of "+tabLabel+".");
        }
        
        GenericMessagePanel newPanel = panel.addGenericMessagePanel(tabLabel);
        panelIdToPanel.put(tabLabel, newPanel);
        
        logger.info("Added generic message panel labeled "+tabLabel+".");
        
        //make sure the window containing this new panel is shown to the user
        checkShowCustomIntefrace();
        
        return newPanel;
    }
    
    /**
     * Cleanup this class by making sure any threads created are destroyed and any custom user interface window 
     * created is closed.
     */
    public void cleanup(){
        
        cleaningUp = true;
        
        if(frame != null){
            
            for(Thread t : consoleEventThreads){
                try{
                    t.interrupt();
                }catch(@SuppressWarnings("unused") Exception e){}
            }
            
            consoleEventThreads.clear();
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.dispose();
            
            
        }
    }

    /**
     * Determine if the custom interface for this module should be shown to the user.
     */
    private void checkShowCustomIntefrace(){
        
        logger.info("Number of tabs is "+panel.getNumberOfTabs()+".");
        
        if(frame == null){
            //build the frame/window
            
            frame = new JFrame("Module User Interface");
            frame.setIconImage(ImageUtil.getInstance().getSystemIcon());    
            //don't allow the user to close the sensor controller dialog manually (i.e. by selecting the "x" on the window)
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.add(panel);
            frame.pack();
        }
        
        frame.setVisible(panel.getNumberOfTabs() > 0);
    }
}
