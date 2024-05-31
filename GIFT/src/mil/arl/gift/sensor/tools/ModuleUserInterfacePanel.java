/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.tools;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the user interfaces for this module.
 * 
 * @author mhoffman
 *
 */
public class ModuleUserInterfacePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(ModuleUserInterfacePanel.class);
    
    /** the root pane that contains the tabs, one for each user interface tied to a specific uniquely named source (e.g. "Q Sensor")*/
    private JTabbedPane tabbedPane;

    /**
     * Create the panel.
     */
    public ModuleUserInterfacePanel() {
        setLayout(new BorderLayout(0, 0));
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setPreferredSize(new Dimension(450, 300));
        add(tabbedPane);
    }
    
    /**
     * Return the current number of tabs on the panel.
     * 
     * @return int the number of tabs
     */
    public int getNumberOfTabs(){
        return tabbedPane.getTabCount();
    }
    
    /**
     * Create a new generic message panel that consists of a text area and text field.
     * 
     * @param tabLabel the unique name of the panel and label to place on the panel's tab
     * @return GenericMessagePanel the panel created
     */ 
    public GenericMessagePanel addGenericMessagePanel(String tabLabel){
        
        GenericMessagePanel newPanel = new GenericMessagePanel(tabLabel);
        return newPanel;
    }
    
    /**
     * Remove a generic message panel from the tabbed pane.
     * 
     * @param panelToRemove instace to remove
     */
    public void removeGenericMessagePanel(GenericMessagePanel panelToRemove){
        
        int index = tabbedPane.indexOfTab(panelToRemove.getTabTitle());
        if(index != -1){
            tabbedPane.remove(index);
        }else{
            throw new IllegalArgumentException("Unable to find the tab with title of "+panelToRemove.getTabTitle()+".");
        }
    }
    
    /**
     * This inner class contains logic to build and interact with a 'generic message panel' that consists
     * of a text area and text field.
     * 
     * @author mhoffman
     *
     */
    public class GenericMessagePanel{
        
        /** the current event listener instance to notify of user response events (can be null) */
        private ModuleUserInterfaceEventListener eventListener;
        
        /** the unique title of the tab this panel is on */
        private String tabTitle;
        
        /** used to show messages to the user */
        private JTextArea textArea;
        
        /** used to allow the user to provide input */
        private JTextField textField;
              
        /**
         * Class constructor - build the panel
         * 
         * @param tabLabel the unique title of the tab this panel is on
         */
        public GenericMessagePanel(String tabLabel){
            
            this.tabTitle = tabLabel;
            
            buildPanel(tabLabel);
        }
        
        /**
         * Return the unique title of the tab this panel is on
         * 
         * @return String
         */
        public String getTabTitle(){
            return tabTitle;
        }
        
        /**
         * Set the current event listener
         * 
         * @param eventListener listener to use for notification of user response(s)
         */
        public void setEventListener(ModuleUserInterfaceEventListener eventListener){
            this.eventListener = eventListener;
        }
        
        /**
         * Create the panel's components
         * 
         * @param tabLabel the unique title of the tab this panel is on
         */
        private void buildPanel(String tabLabel){
            
            if(tabLabel == null || tabLabel.isEmpty()){
                throw new IllegalArgumentException("The label can't be null or empty.");
            }
            
            JPanel panel = new JPanel();
            tabbedPane.addTab(tabLabel, null, panel, null);
            panel.setLayout(null);
            
            textArea = new JTextArea(50, 100);
            textArea.setEditable(false);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            DefaultCaret caret = (DefaultCaret)textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            JScrollPane scrollPane = new JScrollPane(textArea); 
            scrollPane.setPreferredSize(new Dimension(425, 300));
            scrollPane.setBounds(10, 11, 425, 220);
            panel.add(scrollPane);
            
            textField = new JTextField();
            textField.setToolTipText("Enter text here (when applicable)");
            textField.setBounds(10, 241, 425, 20);
            textField.addKeyListener(new KeyListener() {
                
                @Override
                public void keyTyped(KeyEvent e) {
                    
                }
                
                @Override
                public void keyReleased(KeyEvent evt) {
                   
                    if(evt.getKeyCode() == KeyEvent.VK_ENTER){
                        //user pressed enter
                        
                        //capture text in textfield
                        String text = textField.getText();
                        
                        //clear text in textfield
                        textField.setText("");
                        
                        //send text to event listener
                        if(eventListener != null){
                            logger.info("Calling event listener with text of "+text+".");
                            eventListener.textEntered(text);
                        }
                        
                        try{
                            addTextToGenericMessagePanel(text);
                        }catch(Exception e){
                            logger.error("Unable to add text of "+text+" to textfield of tab nammed "+tabTitle+".", e);
                        }
                    }
                    
                }
                
                @Override
                public void keyPressed(KeyEvent e) {
                    
                }
            });
            panel.add(textField);
            textField.setColumns(10);
        }
        
        /**
         * Append text to the end of the message panel's text area.
         * 
         * @param text the message to append
         * @throws Exception if there was a problem appending the text
         */
        public void addTextToGenericMessagePanel(String text) throws Exception{       
            String newLineText = text + "\n";
            textArea.append(newLineText);
        }
        
    }//end innner class
}
