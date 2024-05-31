/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets;

import generated.learner.Input;

import java.util.HashMap;
import java.util.List;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.io.LearnerStateIconMap;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Tree that displays Learner State Interpreters (generated.learner.Input) as 
 * nodes underneath their corresponding Learner State parent nodes.
 * 
 * @author elafave
 *
 */
public class LearnerStateInterpreterTree extends Tree{
	
	private HashMap<LearnerStateAttributeNameEnum, TreeItem> learnerStateToLearnerStateInterpreterTreeItemsMap = new HashMap<LearnerStateAttributeNameEnum, TreeItem>();

	private TreeItem lastSelectedTreeItem = null;
	
	private ScheduledCommand removeInterpreterCmd = null;
	
	public LearnerStateInterpreterTree() {
		
		//prefetch the learner state icons
		for(LearnerStateAttributeNameEnum state : LearnerStateAttributeNameEnum.VALUES()){
			
			String iconPath = null;
			
			try{
				iconPath = LearnerStateIconMap.getEnumerationIconPath(state);
				
			} catch(@SuppressWarnings("unused") Exception e){
				//if no icon could be found matching the learner state, don't prefetch the icon
			}
			
			if(iconPath != null){
				
				iconPath = iconPath.substring(iconPath.indexOf("images"), iconPath.length());
				
				Image.prefetch(iconPath);
			}
		}
		
		//handle styling for selecting Input elements styling does NOT apply to learner state attribute names so they don't appear selectable.
		this.addSelectionHandler(new SelectionHandler<TreeItem>() {
			
			@Override
			public void onSelection(SelectionEvent<TreeItem>  event) {
				
				boolean wasLastSelectedItemNotAnAttributeName = lastSelectedTreeItem == null 
						|| (lastSelectedTreeItem.getUserObject() != null && lastSelectedTreeItem.getUserObject() instanceof Input);
				
				boolean isCurrentSelectedItemAnInterpreter = event.getSelectedItem() != null
						&& event.getSelectedItem().getUserObject() instanceof Input;
				
				if(wasLastSelectedItemNotAnAttributeName && isCurrentSelectedItemAnInterpreter){
					
					if(lastSelectedTreeItem != null){
					
						lastSelectedTreeItem.getElement().getStyle().clearProperty("backgroundColor");
						lastSelectedTreeItem.getElement().getStyle().clearProperty("color");
						lastSelectedTreeItem.getElement().getStyle().clearProperty("boxShadow");
						lastSelectedTreeItem.getElement().getStyle().clearProperty("backgroundImage");
						lastSelectedTreeItem.getElement().getStyle().clearProperty("borderTopLeftRadius");
						lastSelectedTreeItem.getElement().getStyle().clearProperty("borderBottomLeftRadius");
					}
					
					lastSelectedTreeItem = event.getSelectedItem();
					
					lastSelectedTreeItem.getElement().getStyle().setProperty("backgroundColor", "rgb(98, 140, 213)");
					lastSelectedTreeItem.getElement().getStyle().setProperty("color", "white");
					lastSelectedTreeItem.getElement().getStyle().setProperty("boxShadow", "3px 3px 5px rgba(0,0,0,0.2)");
					lastSelectedTreeItem.getElement().getStyle().setProperty("backgroundImage", "linear-gradient(rgb(98, 140, 213), rgb(0, 40, 113))");
					lastSelectedTreeItem.getElement().getStyle().setProperty("borderTopLeftRadius", "10px");
					lastSelectedTreeItem.getElement().getStyle().setProperty("borderBottomLeftRadius", "10px");
				}
			}
		});
	}
	
	/**
	 * Removes the selected Learner State Interpreter and auto-selects the next
	 * Learner State Interpreter.
	 * @return True if there was a selected Learner State Interpreter to
	 * remove, False otherwise.
	 */
	public boolean removeSelectedInterpreter() {
		//If there isn't a selected interpreter then we're done.
    	Input removedInterpreter = getSelectedInterpreter();
    	if(removedInterpreter == null) {
    		return false;
    	}
    	
    	//Gather all of the data we'll need to perform this task.
		TreeItem childToRemove = getSelectedItem();
		TreeItem parentOfChildToRemove = childToRemove.getParentItem();
		TreeItem parentOfChildToSelect = parentOfChildToRemove; //We might update this later.
		boolean useNewParentsFirstNode = true;
		
		//Remove the child but remember its index.
		int childToRemoveIndex = parentOfChildToRemove.getChildIndex(childToRemove);
		childToRemove.remove();
		
		//If the parent doesn't have any more kids then it needs to be removed
		//and we need to pick out a new parent.
		if(parentOfChildToRemove.getChildCount() == 0) {
			//Remove the parent but remember its index.
			int parentOfChildToRemoveIndex = getTopLevelIndex(parentOfChildToRemove);
			LearnerStateAttributeNameEnum learnerState = LearnerStateAttributeNameEnum.valueOf(parentOfChildToRemove.getText());
			learnerStateToLearnerStateInterpreterTreeItemsMap.remove(learnerState);
			removeItem(parentOfChildToRemove);
			
			if(learnerStateToLearnerStateInterpreterTreeItemsMap.isEmpty()){
				return true;
			}
			
			//Identify the parent whose child will be the next selected. If
			//there was a subsequent sibling node, use that. Otherwise use the
			//previous sibling node.
			if(parentOfChildToRemoveIndex < learnerStateToLearnerStateInterpreterTreeItemsMap.size()) {
				parentOfChildToSelect = getItem(parentOfChildToRemoveIndex);
			} else {
				parentOfChildToSelect = getItem(parentOfChildToRemoveIndex - 1);
				useNewParentsFirstNode = false;
			}
			
			//Make sure that parent is open.
			parentOfChildToSelect.setState(true);
		}
		
		//Identify the child we'll be selecting.
		TreeItem childToSelect = null;
		if(parentOfChildToRemove == parentOfChildToSelect) {
			if(childToRemoveIndex < parentOfChildToSelect.getChildCount()) {
				childToSelect = parentOfChildToSelect.getChild(childToRemoveIndex);
			} else {
				childToSelect = parentOfChildToSelect.getChild(childToRemoveIndex - 1);
			}
		} else {
			if(useNewParentsFirstNode) {
				childToSelect = parentOfChildToSelect.getChild(0);
			} else {
				childToSelect = parentOfChildToSelect.getChild(parentOfChildToSelect.getChildCount() - 1);
			}
		}
		
		//Finally make sure that child is selected!
		setSelectedItem(childToSelect, true);
		
		return true;
	}
	
	/**
	 * Determines the index of the Tree Item that represents a Learner State
	 * parent node. I would have thought that the GWT API would provide this
	 * functionality but it seems I had to create it myself.
	 * @param learnerStateTreeItem Tree Item that represents a Learner State
	 * parent node.
	 * @return The index of the supplied learnerStateTreeItem.
	 */
	private int getTopLevelIndex(TreeItem learnerStateTreeItem) {
		int count = learnerStateToLearnerStateInterpreterTreeItemsMap.size();
		for(int x = 0; x < count; x++) {
			TreeItem theTreeItem = getItem(x);
			if(theTreeItem == learnerStateTreeItem) {
				return x;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @return The selected Learner State Interpreter, if one isn't selected
	 * then NULL is returned.
	 */
	public Input getSelectedInterpreter() {
		TreeItem selectedItem = getSelectedItem();
		
		if(selectedItem == null) {
			return null;
		}
		
		Object userObject = selectedItem.getUserObject();
		if(userObject == null) {
			return null;
		}
		
		Input selectedInterpreter = (Input)userObject;
		return selectedInterpreter;
	}
	
	/**
	 * Clears the tree and builds a new one to contain the supplied
	 * interpreters.
	 * @param interpreters Learner State Interpreters to display in the tree.
	 */
	public void setInterpreters(List<Input> interpreters) {
		removeItems();
		learnerStateToLearnerStateInterpreterTreeItemsMap.clear();
		
		for(int x = 0; x < interpreters.size(); x++) {
			Input interpreter = interpreters.get(x);
			
			if(x == 0) {
				addInterpreter(interpreter, true);
			} else {
				addInterpreter(interpreter, false);
			}
		}
	}
	
	/**
	 * Adds a Learner State Interpreter to the tree and auto-selects it.
	 * @param interpreter Learner State Interpreter to add to the tree.
	 */
	public void addInterpreter(Input interpreter) {
		addInterpreter(interpreter, true);
	}
	
	/**
	 * Adds a Learner State Interpreter to the tree and auto-selects it
	 * depending on the supplied selectAndFireEvents parameter.
	 * @param interpreter Learner State Interpreter to add to the tree.
	 * @param selectAndFireEvents True if the newly added tree item should be
	 * auto-selected and fire a selection event, False otherwise.
	 */
	private void addInterpreter(final Input interpreter, boolean selectAndFireEvents) {
		
		final TreeItem item = new TreeItem();
		
		final DeckPanel deckPanel = new DeckPanel();
		deckPanel.getElement().getStyle().setProperty("paddingRight", "15px");
		
		final Label label = new Label();
		label.setText(interpreter.getName());
		label.getElement().getStyle().setProperty("display", "inline");
		
		final TextBox textBox = new TextBox();
		textBox.setText(interpreter.getName());
		
		Image closeImage = new Image(GatClientBundle.INSTANCE.cancel_image());
		closeImage.getElement().getStyle().setProperty("display", "inline");
		closeImage.getElement().getStyle().setProperty("marginRight", "5px");
		
		final FlowPanel labelWrapper = new FlowPanel();
		labelWrapper.add(closeImage);
		labelWrapper.add(label);
				
		closeImage.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(removeInterpreterCmd != null) {
					removeInterpreterCmd.execute();
				}
			}
			
		});
		
		//selection handler that enables clicking the label to edit the name
		this.addSelectionHandler(new SelectionHandler<TreeItem>() {
			
			ClickHandler labelClickHandler = null;
			
			HandlerRegistration clickHandlerRegistration = null;

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				
				if(event.getSelectedItem() != null 
						&& event.getSelectedItem().equals(item)){
									
					//if this item is selected, allow its name to be edited by clicking its label
					
					labelClickHandler = new ClickHandler() {
						
						boolean isInitialClick = true;
						
						@Override
						public void onClick(ClickEvent event) {
							
							if(isInitialClick){
								
								//do nothing on the first click so that users can select an item without immediately editing it
								isInitialClick = false;
								
							} else {
							
								deckPanel.showWidget(deckPanel.getWidgetIndex(textBox));
								
								//need to use a scheduled command here, or else the text box will immediately lose focus and revert to a label
								Scheduler.get().scheduleDeferred(new ScheduledCommand() {
									
									@Override
									public void execute() {
										textBox.setFocus(true);
									}
								});		
							}
						}
					};
										
					clickHandlerRegistration = label.addClickHandler(labelClickHandler);
				
				} else if(clickHandlerRegistration != null){
					
					// otherwise, disallow editing the name
					clickHandlerRegistration.removeHandler();
				}
			}
		});
		
		textBox.addBlurHandler(new BlurHandler() {
			
			@Override
			public void onBlur(BlurEvent event) {			
				deckPanel.showWidget(deckPanel.getWidgetIndex(labelWrapper));
			}
		});
		
		textBox.addValueChangeHandler(new ValueChangeHandler<String>(){

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					interpreter.setName(event.getValue());
					label.setText(event.getValue());
					
				} else {
					
					WarningDialog.warning("Invalid value", "The name for this interpreter must not be empty.");
					
					label.setText(interpreter.getName());
					textBox.setValue(interpreter.getName());
				}
			}
			
		});
		
		textBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				
				 boolean enterPressed = KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode();
				 
                 if (enterPressed){
                	 
                	 textBox.setFocus(false);
                 }
			}
		});
		
		deckPanel.add(labelWrapper);
		deckPanel.add(textBox);
		
		deckPanel.showWidget(deckPanel.getWidgetIndex(labelWrapper));
		
		item.setWidget(deckPanel);
		item.setUserObject(interpreter);
		
		item.setUserObject(interpreter);
		
		TreeItem parentItem = getLearnerStateItem(interpreter);
		parentItem.addItem(item);
		parentItem.setState(true);
		if(selectAndFireEvents) {
			setSelectedItem(item, true);
		}
	}
	
	public void setRemoveInterpreterCommand(ScheduledCommand cmd) {
		removeInterpreterCmd = cmd;
	}
	
	private TreeItem getLearnerStateItem(Input interpreter) {
		//TODO What should we do if learner state is NULL because our mappings don't work with the interpreter?
		LearnerStateAttributeNameEnum learnerState = LearnerConfigurationMaps.getInstance().getLearnerState(interpreter);
		TreeItem item = learnerStateToLearnerStateInterpreterTreeItemsMap.get(learnerState);
		if(item == null) {
			item = new TreeItem();
			
			FlowPanel flowPanel = new FlowPanel();
			flowPanel.getElement().getStyle().setProperty("paddingTop", "5px");
			
			String iconPath = LearnerStateIconMap.getEnumerationIconPath(learnerState);
			
			Image icon = null;
			
			if(iconPath != null && iconPath.indexOf("images") != -1){
				
				iconPath = iconPath.substring(iconPath.indexOf("images"), iconPath.length());
				
				icon = new Image();
				icon.setUrl(iconPath);
				icon.setAltText("An icon for a Learner State Attribute");
				icon.getElement().getStyle().setProperty("float", "left");
				icon.getElement().getStyle().setProperty("marginRight", "5px");
				icon.setWidth("32px");
				icon.setHeight("32px");
			}
			
			HTML itemHTML = new HTML();
			itemHTML.setWidth("100%");
			itemHTML.setHTML(""
					+ 	"<div style='padding: 5px; width: 100%; font-weight: bold; text-decoration: underline; font-size: 16px; color: #4b4a4a;"
					+ 			"text-shadow: 3px 3px 2px rgba(100,100,100,0.2); cursor: default;'"
					+ 	">"
					+ 		learnerState.getDisplayName()
					+ 	"</div>"
			);		
			
			if(icon != null){
				flowPanel.add(icon);				
			}
			
			flowPanel.add(itemHTML);
			
			item.setWidget(flowPanel);

			learnerStateToLearnerStateInterpreterTreeItemsMap.put(learnerState, item);
			
			addItem(item);
		}
		return item;
	}
}
