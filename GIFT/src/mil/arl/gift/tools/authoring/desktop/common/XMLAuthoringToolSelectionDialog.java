/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.common;

import com.fg.ftreenodes.FAbstractToggleNode;
import com.fg.ftreenodes.FTextLabelNode;
import com.fg.ftreenodes.FToggleSwitchNode;
import com.fg.ftreenodes.ICellControl;
import com.fg.ftreenodes.Params;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This dialog is used by the Authoring Tools that use XML Editor and have the appropriate annotation in 
 * the XML schemas used by the tools.  For example the learnerConfig.xsd specifies a Translator editor-class which
 * extends this class.  The dialog created contains a label, for information/hints/guidance and a combobox with the possible
 * choices.
 * 
 * @author mhoffman
 *
 */
public abstract class XMLAuthoringToolSelectionDialog extends JDialog implements ICellControl {
    
    /** serial version uid */
	private static final long serialVersionUID = 1L;
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(XMLAuthoringToolSelectionDialog.class);
    
    /** GUI components */
    private javax.swing.JButton cancelButton;
    protected javax.swing.JComboBox<Object> valuesComboBox;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea informationTextArea;
    private javax.swing.JButton okButton;
    protected javax.swing.JButton browseButton;
    
    /** the last selected item in the combobox */
    protected Object selectedValue;
    
    /** the initial value selected in the combobox when the dialog is updated (can be null)*/
    protected Object initialValue;
    
    /** default values to add to the combobox */
    private List<SelectionItem> defaultValues;
    
    /** tooltip information to show when mousing over an item in the combobox */
    private List<String> tooltips = new ArrayList<>();
    
    /**
     * Class constructor - create and show dialog
     * 
     * @param title - the text to display as the title of the dialog
     * @param label - the text to display as helpful information on the dialog
     * @param defaultValues - the default values to populate the combo box with
     * @param allowUserEntries - whether to allow the user to manually type in their own values into the combobox
     */
    public XMLAuthoringToolSelectionDialog(String title, String label, List<SelectionItem> defaultValues, boolean allowUserEntries){
        
        initComponents(title, label);
        
        setDefaultValues(defaultValues);
        
        populateWithEntries(); 
        
        valuesComboBox.setEditable(allowUserEntries);
    }
    
    /**
     * Set the default values to show in the combobox for every instance of the dialog.
     * 
     * @param values information about the items to place in the combobox
     */
    protected void setDefaultValues(List<SelectionItem> values){
        this.defaultValues = values;
    }
    
    /**
     * Return the default values provided for the combobox in the dialog.
     * 
     * @return List<SelectionItem> information about the items to place in the combobox
     */
    protected List<SelectionItem> getDefaultValues(){
        return defaultValues;
    }
    
    /**
     * Return additional values for the dialog specific to the implementation class.  For example, the additional values
     * could be based on other values in the XML file (such as ids) and/or from a user history entry (such as custom implementation class entries).  
     * These values are added to the default values in the combobox.
     * 
     * @return Object[]  - can be empty but not null.
     */
    public abstract Object[] getCustomValues();
    
    /**
     * The user has provided a new entry in the combobox and it should be saved to the appropriate
     * user history location for this particular attribute.
     * 
     * @param value - the custom user provided value for the attribute represented by the combobox in this dialog
     */
    public abstract void addUserEntry(String value);
    
    /**
     * Refresh/Re-populate the combobox with entries from the default collection and user provided history of entries.
     */
    private void populateWithEntries(){
        
        valuesComboBox.removeAllItems();
        tooltips.clear();
        
        Object[] userHistory = null;
        try{
            userHistory = getCustomValues();
        }catch(Exception e){
            logger.error("Caught exception from mis-behaving selection dialog implementation class, therefore the user history will not be used", e);
        }
        
        if(userHistory != null){
            for(int i = 0; i < userHistory.length; i++){
                
                Object item = userHistory[i];
                if(item instanceof SelectionItem){
                    //handle if the custom value is a complex selection item
                    
                    valuesComboBox.addItem(((SelectionItem)item).getValue());
                    tooltips.add(((SelectionItem)item).getFormattedDescription());
                }else{
                    valuesComboBox.insertItemAt(userHistory[i], i);
                }
            }
        }else{
            logger.error("The custom values for the dialog "+this+" returned null.  An empty list is allowed but not null.");
            JOptionPane.showMessageDialog(this, "There was a problem populating all the values for this dialog. There maybe more information in the appropriate authoring tool log.",
                    "Unable to fully populate dialog", JOptionPane.ERROR_MESSAGE);
        }
        
        for(SelectionItem item : defaultValues){
            valuesComboBox.addItem(item.getValue());
            tooltips.add(item.getFormattedDescription());
        } 
        
    }
    
    /**
     * Refresh the entries in the combobox
     */
    protected void refreshEntries(){
        populateWithEntries();
    }
    
    /**
     * Set the text to display as helpful information on the dialog
     * 
     * @param content text to place in the information component for the user to see
     */
    protected void setHelpfulInformation(String content){
        
        if(informationTextArea != null){
            informationTextArea.setText(content);
        }
    }
    
    /**
     * Initialize/Build the dialog components
     * 
     * @param title - the text to display as the title of the dialog
     * @param label - the text to display for the information label on the dialog
     */
    protected void initComponents(String title, String label) {

        valuesComboBox = new javax.swing.JComboBox<Object>();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        informationTextArea = new javax.swing.JTextArea();
        browseButton = new javax.swing.JButton();
        
        browseButton.setText("Browse");
        //disable, hide browse button because its not needed by all types of AT selection dialogs but its convenient for now
        //to add it to the panel layout
        browseButton.setVisible(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        valuesComboBox.setModel(new javax.swing.DefaultComboBoxModel<Object>());
        
        //to present tooltips for each item in the combobox
        ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
        valuesComboBox.setRenderer(renderer);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.setMaximumSize(new java.awt.Dimension(65, 23));
        okButton.setMinimumSize(new java.awt.Dimension(65, 23));
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        
        informationTextArea.setColumns(20);
        informationTextArea.setRows(5);
        DefaultCaret caret = (DefaultCaret)informationTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        informationTextArea.setText(label);
        informationTextArea.setEditable(false);
        informationTextArea.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        scrollPane.setViewportView(informationTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 393, Short.MAX_VALUE)
                            .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(valuesComboBox, 0, 488, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(browseButton)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(valuesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(browseButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
            );
        
        setTitle(title);

        pack();
    }
    
    /**
     * The ok button was pressed.  If the user provided a new custom entry, save it.
     * 
     * @param evt the button action event
     */
    protected void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        
        //save selected/provided entry
        Object tempSelectedValue = valuesComboBox.getSelectedItem();
        
        if(tempSelectedValue != null && tempSelectedValue.toString().length() > 0){
            //a value has been provided/selected
            
            selectedValue = tempSelectedValue;

            //if not in combobox already then add entry to user history
            if(valuesComboBox.getSelectedIndex() == -1){
                
                addUserEntry(selectedValue.toString());
            }
            
            this.dispose();
        }else{
            //show error message 
            
            JOptionPane.showMessageDialog(this,
                    "The value selected is invalid",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        
    }

    /**
     * The cancel button was pressed.  Close the window.
     * 
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        
        //set the selected value to the initial value when the dialog was updated
        selectedValue = initialValue;
        
        this.dispose();
    }
    
    /**
     * Parse and use the input parameters to this tool
     * Note: override this method to provide specific handling of parameters.
     * 
     * @param params contains mapping of parameters for the dialog
     */
    protected void useParameters(Params params){
        //nothing to do here...
    }
    
    @Override
    public Object getData() {
        return selectedValue;
    }

    @Override
    public void initCellControl(boolean arg0) {
        //nothing to do
        
    }

    @Override
    public void updateCellControl(boolean isEditor, boolean enabled,
            boolean editable, Object data, Params params) {
        
        if(params != null){
            useParameters(stipBadPrefix(params));
        }
        
        populateWithEntries();
        
        initialValue = data;
        
        if(data != null && (data instanceof String && !((String)data).isEmpty())){
            valuesComboBox.setSelectedItem(data);
        
            if(valuesComboBox.getSelectedIndex() == -1){        
                //loaded new custom entry, add it to list and select it
                String entryStr = data.toString();
                if(entryStr.length() > 0){
                    addUserEntry(data.toString());
                }
                valuesComboBox.insertItemAt(data, 0);
                valuesComboBox.setSelectedIndex(0);
            }
        
            selectedValue = valuesComboBox.getSelectedItem();
        }

        if(valuesComboBox.getItemCount() == 0 && !valuesComboBox.isEditable()){
            valuesComboBox.setEnabled(false);
        }else{        
            valuesComboBox.setEnabled(true);
        }
    }
    
    /**
     * For some reason the Params map values have "null\n" prefixed to any value specified in the schema.
     * This method removes that string from the values in the map (if any).
     * 
     * @param params XMLEditor parameters to clean up
     * @return Params - original Params object with modified map values (if any).
     */
    public static Params stipBadPrefix(Params params){
        
        if(params != null && params.getMap() != null){
            
            for(Object key : params.getMap().keySet()){
                Object value = params.getMap().get(key);
                
                if(value instanceof String){
                    value = ((String) value).replace("null\n", "");
                    params.getMap().put(key, value);
                }
            }
        }
        
        return params;
    }
    
    /**
     * Find a child node in the authoring tool tree under the given parent with the given name.
     * 
     * @param parent The parent to check it's children
     * @param name - the label of an xml node to find as a child to the parent node.
     * @return FAbstractToggleNode - the first child node found to have the name specified.  Can be null if no node was found.
     */
    protected static FAbstractToggleNode findChildNodeByName(FAbstractToggleNode parent, String name){
        
        if(parent == null){
            return null;
        }else if(name == null){
            return null;
        }
        
        int childCnt = parent.getChildCount();
        for(int i = 0; i < childCnt; i++){
            
            TreeNode node = parent.getChildAt(i);
            if(node instanceof FAbstractToggleNode && ((FAbstractToggleNode)node).getLabelText().equals(name)){
                return (FAbstractToggleNode) node;
            }
        }
        
        return null;
    }
    
    /**
     * Return the index of the desired choice item in the choice node's list of choices.
     * 
     * @param choice the choice element that contains the list of items to choose from
     * @param desiredChoiceLabel a label of the item to choose from the choice list
     * @return int the index of the desired choice in the list of choices.  Can be -1 if an item was not found.
     */
    protected static int getChoiceIndex(FToggleSwitchNode choice, String desiredChoiceLabel){
        
        if(choice == null){
            throw new IllegalArgumentException("The choice node can't be null");
        }else if(desiredChoiceLabel == null || desiredChoiceLabel.length() == 0){
            throw new IllegalArgumentException("The desired choice label of "+desiredChoiceLabel+" must contain at least one character.");
        }
        
        int elementIndex = -1;
        
        //Note: can't call getChildCount here because it may return zero
        Enumeration<?> enumeration = choice.children();
        while(enumeration.hasMoreElements()){
            
            elementIndex++;
            if(((FTextLabelNode)enumeration.nextElement()).getLabelText().equalsIgnoreCase(desiredChoiceLabel)){
                break;
            }
        }
        
        return elementIndex;
    }
    
    /**
     * Used to set tooltip information for each cell in the combobox.
     * 
     * @author mhoffman
     *
     */
    public class ComboboxToolTipRenderer extends DefaultListCellRenderer {
        
        /** serial version uid */
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                            int index, boolean isSelected, boolean cellHasFocus) {
            
            //index = 0 is reserved for the combobox's selected item, i.e. the cell that is shown then the combobox is not selected.
            //therefore the items index start at 1.
          	
            if(index >= 0 && value != null && tooltips.size() > index){            	
                	setToolTipText(tooltips.get(index));
                
            } else {
                setToolTipText("");
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }
    
    /**
     * This inner class is used to contain additional information for each item in the dialog's
     * combobox.
     *
     * @author mhoffman
     *
     */
    public static class SelectionItem implements Comparable<SelectionItem>{
        
        /** the text to show as the selectable item in the combobox */
        private String value;
        
        /** information about the item */
        private String description;
        
        /** html formatted information about the item */
        private String formattedDescription;
        
        /**
         * Class constructor - set class attribute values.
         * 
         * @param value the text to show for the item in the combobox.  Can't be null.
         * @param description information about the item.  Can be null.
         */
        public SelectionItem(String value, String description){
            
            if(value == null){
                throw new IllegalArgumentException("The value can't be null.");
            }
            
            this.value = value;
            this.description = description;
            
            //Format description
            if(description != null && description.length() > 100){
                
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                
                //Note: j=i+1 is to skip the space character found so a space doesn't start at the beginning of the newline
                for(int i = 100, j = 0; i < description.length(); j=i+1, i = i+100){
                    i = description.indexOf(" ", i);
                    
                    if(i == -1){
                        //no space found after the position, therefore just allow the remaining characters
                        sb.append(description.substring(j));
                        break;
                    }else{
                        sb.append(description.substring(j, i));
                        sb.append("<br>");
                    }
                    
                    //make sure to grab last part of string
                    if((i+100) > description.length()){
                        int val = i + 1;
                        sb.append(description.substring(val));
                    }
                }
                
                sb.append("</html>");
                
                this.formattedDescription = sb.toString();
            }else{
                this.formattedDescription = description;
            }
        }
        
        /**
         * Return the text shown in the combobox item
         * 
         * @return String
         */
        public String getValue(){
            return value;
        }
        
        /**
         * Return information about the item.
         * 
         * @return String
         */
        public String getDescription(){
            return description;
        }
        
        /**
         * Return the HTML formatted version of the description
         * 
         * @return String
         */
        public String getFormattedDescription(){
            return formattedDescription;
        }

        @Override
        public int compareTo(SelectionItem otherItem) {
            
            if(otherItem != null){
                return this.getValue().compareTo(otherItem.getValue());
            }
            
            return -1;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[SelectionItem: ");
            sb.append("value = ").append(getValue());
            sb.append(", description = ").append(getDescription());
            sb.append("]");
            
            return sb.toString();
            
        }
    }

}
