/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Class to control the view of GIFT messages.
 *
 * @author cragusa
 */
public class MessageViewControlPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * MessageAnimationPanel to be controlled by this control panel.
     */
    private MessageAnimationPanel mypanel;
    
    /**
     * MessageListModel controlled by this control panel.
     */
    private MessageListModel messageListModel;
    
    /**
     * Constructor used to create MessageViewControlPanel for message views not associated with a domain session.
     * 
     * @param panel the MessageAnimationPanel to be controlled.
     */
    MessageViewControlPanel(MessageAnimationPanel panel) {
    	        
    	this.mypanel = panel;    	
        
    	setLayout(new BorderLayout());
    	
    	addListenCheckbox(this);
    }
    
    /**
     * Add the listen label and checkbox to the provided panel.
     * 
     * @param panel the panel to add the components too
     */
    private void addListenCheckbox(JPanel panel){        
        
        final JCheckBox listenCheckbox = new JCheckBox("Listen");
        listenCheckbox.setToolTipText("Whether or not to update this message panel with incoming messages.");
        
        listenCheckbox.setSelected(true);

        panel.add(listenCheckbox, BorderLayout.CENTER);

        listenCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                if (event.getSource().equals(listenCheckbox)) {

                    if(messageListModel != null) {                      
                        messageListModel.setListening(listenCheckbox.isSelected());
                    }
                    mypanel.setManualMode(!listenCheckbox.isSelected());
                }
            }
        });   
    }

    
    /**
     * Constructor used to create MessageViewControlPanel for message views that are associated with a particular domain session.
     * 
     * @param domainSessionId The ID of the domain session to display
     * @param panel the MessageAnimationPanel to be controlled.
     */
    MessageViewControlPanel(final int domainSessionId, MessageAnimationPanel panel) {
        
        this.mypanel = panel;       
        
        setLayout(new BorderLayout());
        
        JPanel controlsPanel = new JPanel();
        addListenCheckbox(controlsPanel);
        
        final JButton closeButton = new JButton("Close");
        closeButton.setToolTipText("Closes this tabbed panel.");
        
        controlsPanel.add(closeButton);

        closeButton.addActionListener(new ActionListener() {
        	
            @Override
            public void actionPerformed(ActionEvent event) {
                MonitorModule.getInstance().ignoreDomainSession(domainSessionId);
            }
        });
        
        add(controlsPanel, BorderLayout.CENTER);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 60);
    }


    /**
     * Sets the MessageListModel to be controlled by this control panel.
     *  
     * @param messageListModel the new list model to use
     */
	public void setMessageListModel(MessageListModel messageListModel) {
		
		this.messageListModel = messageListModel;
	}
}
