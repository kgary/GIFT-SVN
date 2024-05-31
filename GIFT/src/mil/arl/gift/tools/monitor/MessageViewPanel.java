/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.tools.monitor.PanelManager.ModuleListModel;

/**
 * Custom cell renderer for the MessageFilter JList.
 *
 * @author cragusa
 */
class FilterListCellRenderer extends JCheckBox implements ListCellRenderer<MessageTypeEnum> {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(
            JList<? extends MessageTypeEnum> list,
            MessageTypeEnum choice,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        this.setSelected(isSelected);
        this.setText(choice.getDisplayName());

        return this;
    }
}

/**
 * Custom cell renderer for the UserSessionMessage JList.
 *
 * @author cragusa
 */
class MessageListCellRenderer extends JLabel implements ListCellRenderer<Message> {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Message> list,
            Message choice,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        this.setText(choice.getMessageType().getDisplayName());
        this.setBackground(Color.LIGHT_GRAY);
        this.setOpaque(isSelected);

        return this;
    }
}

/**
 * Top-level swing container that holds multiple views and controllers (filters)
 * for Messages.
 *
 * @author cragusa
 */
public class MessageViewPanel extends JPanel implements FilterChangeListener {

    private static final long serialVersionUID = 1L;

    /** View of the messages in list form. Messages are selectable */
    protected final JList<Message> messageListView = new JList<>();

    /** GUI presentation of the message type filter */
    protected final JList<MessageTypeEnum> filterListView = new JList<>();
    
    /** label for the entity marking input  */
    protected static final JLabel entityMarkingLabel = new JLabel("Entity Marking");
    
    /** label with instructions on how to apply the entity marking filter */
    protected static final JLabel entityMarkingInstructionsLabel = new JLabel("(press Enter to apply)");
    
    /** where the user provides the entity marking text to filter on */
    protected final JTextField entityMarkingTextfield = new JTextField();

    /** TextArea used to show message details when a specific message is selected */
    protected final JTextArea messageDetailsTextArea = new JTextArea();

    /** Graphical panel that animates messages flowing between activeMQ endoints */
    protected final MessageAnimationPanel messageAnimationPanel = new MessageAnimationPanel();

    /** The control panel used to turn listening of messages on and off */
    protected final MessageViewControlPanel controlPanel;
    
    /** help icon for the help button */
    private javax.swing.ImageIcon helpIcon;
    
    /** help button that shows info on entity marking filtering */
    private javax.swing.JButton helpButton;
    
    /** The currently selected message in the view of the messages in list form.*/
    private Message selectedMessage;
    
    /**
     * Constructor
     * 
     * @param domainSessionId
     */
    MessageViewPanel(int domainSessionId) {
        this.setLayout(new BorderLayout());

        controlPanel = (domainSessionId > 0) ? new MessageViewControlPanel(domainSessionId, messageAnimationPanel) : new MessageViewControlPanel(messageAnimationPanel);
        messageAnimationPanel.setToolTipText("<html>Shows messages being passed between the various GIFT modules and the message bus.<br>As a sender or receiver of a message is identified, it will be added to the diagram dynamically.</html>");     
        
        //TODO: messageDetailsTextArea needs change it's view if the previously selected value is no longer there 
        //      e.g. if the message is made invisible by a change in the filter
        //      consider making it listen to the filter. Then if the filter changes it can see if its current view is still valid?
        messageDetailsTextArea.setLineWrap(true);
        messageDetailsTextArea.setWrapStyleWord(true);
        messageDetailsTextArea.setEditable(false);
        messageDetailsTextArea.setToolTipText("The selected message's details (e.g. message header, payload) will appear here.");

        filterListView.setToolTipText("<html>This is a list of message types received by this monitor so far.<br>If a message type is unchecked, then messages of that type will not be displayed in the list of recent messages.<br>To select multiple message types hold down the 'CTRL' key while clicking.</html>");
        
        final JScrollPane messageDetailsScrollPane = new JScrollPane(messageDetailsTextArea);

        final int MESSAGE_DETAIL_AREA_HEIGHT = 300;

        messageDetailsScrollPane.setPreferredSize(new Dimension(0, MESSAGE_DETAIL_AREA_HEIGHT));

        messageListView.setCellRenderer(new MessageListCellRenderer());
        messageListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        messageListView.setToolTipText("<html>This is a list of the most recent messages received by this monitor so far.<br>Note: the maximum number of messages displayed is "+MonitorModuleProperties.getInstance().getMessageDisplayBufferSize()+".</html>");

        final ListSelectionListener listSelectionListener = new ListSelectionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void valueChanged(ListSelectionEvent evt) {          

                JList<Message> source = (JList<Message>) evt.getSource();

                int selectedIndex = source.getSelectedIndex();
                
                if (selectedIndex < source.getModel().getSize()) {

                    selectedMessage = source.getSelectedValue();

                    if (selectedMessage != null) {

                        messageDetailsTextArea.setText(constructMessageString(selectedMessage));
                        messageDetailsTextArea.setCaretPosition(0);
                    } else {

                        //TODO: can this happen? If so what does it mean?
                        messageDetailsTextArea.setText("");
                    }
                } 
            }
        };        

        messageListView.addListSelectionListener(listSelectionListener);

        //show user-selected messages in the animation panel
        messageListView.addListSelectionListener(messageAnimationPanel);

        JScrollPane messageListViewScrollPane = new JScrollPane(messageListView);
        messageListViewScrollPane.setMinimumSize(new Dimension(200, 1));
        messageListViewScrollPane.setPreferredSize(new Dimension(200, 1));

        //Add the entity marking message filter for attached domain sessions only (i.e. not the system messages)
        if(domainSessionId > 0){
            entityMarkingTextfield.setPreferredSize(new Dimension(200,20));
            
            // update the filter when the user presses enter on the keyboard
            entityMarkingTextfield.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {

                    for(ListSelectionListener listener : filterListView.getListSelectionListeners()){
                        
                        if(listener instanceof MessageViewFilter){
                            // update the entity marking text filter
                            MessageViewFilter filter = (MessageViewFilter)listener;
                            filter.setEntityStateURNMarkingFilter(entityMarkingTextfield.getText());
                        }
                    }
                }
            });
            
            helpIcon = createImageIcon("images/help.png", "Help");
            helpButton = new JButton(helpIcon);
            
            helpButton.setPreferredSize(new Dimension(helpIcon.getIconWidth(), helpIcon.getIconHeight()));
            helpButton.setOpaque(false);
            helpButton.setContentAreaFilled(false);
            helpButton.setBorderPainted(false);
            helpButton.setToolTipText("<html>When using DIS interop protocol, entity marking can't exceed 11 characters.<br />So only use up to the first 11 characters here.</html>");
            
            JPanel entityMarkingPanel = new JPanel();
            entityMarkingPanel.add(entityMarkingLabel);
            entityMarkingPanel.add(entityMarkingTextfield);
            entityMarkingPanel.add(entityMarkingInstructionsLabel);
            entityMarkingPanel.add(helpButton);
            controlPanel.add(entityMarkingPanel, BorderLayout.SOUTH);
        }
        
        this.add(messageListViewScrollPane, BorderLayout.WEST);
        this.add(new JScrollPane(messageAnimationPanel), BorderLayout.CENTER);
        this.add(new JScrollPane(filterListView), BorderLayout.EAST);
        this.add(new JScrollPane(messageDetailsScrollPane), BorderLayout.SOUTH);
        this.add(controlPanel, BorderLayout.NORTH);
    }
    
    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path The absolute path of the image to create an icon from
     * @param description The description of the image
     * @return ImageIcon The image converted into an icon
     */
    protected ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        }

        System.err.println("Couldn't find file: " + path);
        return null;
    }

    /**
     * Formats a Message string for presentation in the message detail text
     * area.
     *
     * @param message The message to format
     * @return String The string formatted message
     */
    static String constructMessageString(final Message message) {

        StringBuilder builder = new StringBuilder();
        if (message != null) {
            /* In order to avoid tab spacing inconsistencies, define a string format that left-aligns the text, 
             * prepends 20 spaces between the left edge and the first space observed, and outputs the next string 
             * tab-spaced. */
            String format = "%-20s\t%s";
            String lineSep = System.getProperty("line.separator");
            
            /* Use the defined string format to output the message header and contents. */
            builder.append(String.format(format, "Source:", message.getSenderModuleName())).append(lineSep);
            builder.append(String.format(format, "Source Address:", message.getSenderAddress())).append(lineSep);
            builder.append(String.format(format, "Source Module Type:", message.getSenderModuleType().getDisplayName())).append(lineSep);
            builder.append(String.format(format, "Destination Address:", message.getDestinationQueueName())).append(lineSep);
            builder.append(String.format(format, "Message Type:",message.getMessageType().getDisplayName())).append(lineSep);
            builder.append(String.format(format, "Sequence #:", message.getSequenceNumber())).append(lineSep);
            builder.append(String.format(format, "Source Event Id:", message.getSourceEventId())).append(lineSep);
            
            if (message.isReplyMessage()) {
                builder.append(String.format(format, "Reply to Sequence #:", message.getReplyToSequenceNumber())).append(lineSep);
            }
            
            if(message instanceof UserSessionMessage){
                builder.append(String.format(format, "User Id:", ((UserSessionMessage)message).getUserSession().getUserId())).append(lineSep);
                builder.append(String.format(format, "Username:", ((UserSessionMessage)message).getUserSession().getUsername())).append(lineSep);
                
                if(message instanceof DomainSessionMessage){
                    
                    if(((DomainSessionMessage)message).getExperimentId() != null){
                        builder.append(String.format(format, "Experiment id:", ((DomainSessionMessage)message).getExperimentId())).append(lineSep);
                    }
                }
            }
            
            builder.append(String.format(format, "Needs ACK:", message.needsHandlingResponse())).append(lineSep);
            builder.append(String.format(format, "Timestamp (raw):", message.getTimeStamp())).append(lineSep);
            builder.append(String.format(format, "Timestamp:", TimeUtil.timeFirstFormat.format(message.getTimeStamp()))).append(lineSep);

            Object payload = message.getPayload();

            if (payload != null) {
                builder.append(String.format(format, "Message Details:", payload.toString()));
            }

        } else {
            builder.append("Message is null");
        }

        return builder.toString();
    }

    /**
     * Sets the (Message)ListModel for this panel and appropriate sub-panels.
     *
     * @param messageListModel the new list model to use
     */
    public void setMessageListModel(MessageListModel messageListModel) {
        messageListView.setModel(messageListModel);
        messageAnimationPanel.setUserSessionMessageListModel(messageListModel);        
        controlPanel.setMessageListModel(messageListModel);
    }

    /**
     * Sets the (Filter)ListModel for this panel and appropriate sub-panels.
     *
     * @param filterListModel
     */
    void setFilterListModel(MessageViewFilter filterListModel) {
        filterListView.setModel(filterListModel);
        filterListView.setCellRenderer(new FilterListCellRenderer());
        filterListView.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //default to all selected		
        final int COUNT = filterListView.getModel().getSize();
        int[] selectedIndicies = new int[COUNT];
        for (int i = 0; i < COUNT; i++) {
            selectedIndicies[i] = i;
        }
        filterListView.setSelectedIndices(selectedIndicies);
        
        //list for changes to the filter's list model containing the message types, if any items are added, auto-select them
        filterListModel.addListDataListener(new ListDataListener() {
            
          @Override
          public void intervalRemoved(ListDataEvent e) {
              //nothing to do
              
          }
          
          @Override
          public void intervalAdded(ListDataEvent e) {

              //get the range of the indexes in the list for the new entries
              final int startIndex = e.getIndex0();
              final int endIndex = e.getIndex1();
                 
              //add the new entries to the current list of selected items
              //NOTE: if this is not executed later, an index out of bounds exception is thrown
              Runnable runnable = new Runnable() {

                  @Override
                  public void run() {
                      filterListView.addSelectionInterval(startIndex, endIndex);
                      
                  }
              };
              
              SwingUtilities.invokeLater(runnable);
              
          }
          
          @Override
          public void contentsChanged(ListDataEvent e) {
              //nothing to do
              System.out.println("contents changed");
          }
        });

        //TODO: implement custom?
        //filterList.setSelectionModel(selectionModel);
    }

    /**
     * Sets the (Module)ListModel for this panel and appropriate sub-panels.
     *
     * @param moduleListModel
     */
    void setModuleListModel(ModuleListModel moduleListModel) {
        messageAnimationPanel.setModuleListModel(moduleListModel);
    }

    /**
     * Sets the (Module)ListSelectionListener for this panel and appropriate
     * sub-panels.
     *
     * @param listener
     */
    void setListSelectionListener(final ListSelectionListener listener) {
        filterListView.addListSelectionListener(listener);
    }

    @Override
    public void filterChanged(FilterChangeEvent event) {
        
    	if(selectedMessage == null || !filterListView.getSelectedValuesList().contains(selectedMessage.getMessageType())){
    		
    		// if the filter no longer contains the message type currently selected then clear the message details.
    		messageDetailsTextArea.setText("");
    		messageListView.clearSelection();
    		
    	} else{
    		
    		// otherwise, make sure to update the selection index based on the new list of messages
    		messageListView.setSelectedIndex(((MessageListModel) messageListView.getModel()).getDisplayList().indexOf(selectedMessage));
    	}
       

    }
}
