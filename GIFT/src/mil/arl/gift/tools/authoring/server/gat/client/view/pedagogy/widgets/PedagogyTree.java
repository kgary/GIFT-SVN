/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import generated.ped.Attribute;

import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.io.LearnerStateIconMap;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * The PedagogyConfiguration (generated.ped.EMAP) is a data structure nested
 * two levels deep (Merrill's quadrant -> Attribute). Each attribute has a 
 * "type" (Learner State) and "value" (Enum quantifying the state).
 * 
 * The PedagogyTree displays these attributes in a tree that is nested three
 * levels deep (Merrill's quadrant -> Learner State -> quantifying enum). The
 * 3rd level of the tree (quantifying enum) is used to represent an Attribute.
 * 
 * @author elafave
 *
 */
public class PedagogyTree extends Tree {
	
	private TreeItem lastSelectedTreeItem = null;
	
	private ScheduledCommand removeAttributeCmd = null;

	public PedagogyTree() {
		List<MerrillQuadrantEnum> quadrants = MerrillQuadrantEnum.VALUES();
		for(MerrillQuadrantEnum quadrant : quadrants) {
		    if(quadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_PRACTICE ||
		            quadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){
		        // these phases are just for tracking where the learner came from (e.g. recall) 
		        // and are not meant to have metadata attributes authored for these phases because
		        // the metadata comes from Example/Rule phases for passive activity requests.
		        continue;
		    }
			TreeItem quadrantTreeItem = new TreeItem();
			
			HTML itemHtml = new HTML(""
					+ 	"<div style='padding: 5px; text-align: center; color: white; font-size: 16px; font-weight: bold; border-radius: 2px;"
					+ 			"margin-top: 5px; background-color: black; cursor: default; text-shadow: 3px 3px 5px rgba(0,0,0,0.5);"
					+ 			"background-image: linear-gradient(rgb(100, 100, 100), black); box-shadow: 3px 3px 5px rgba(0,0,0,0.5);'>"
					+ 		quadrant.getDisplayName()
					+ 	"</div>");
			
			quadrantTreeItem.setWidget(itemHtml);
			quadrantTreeItem.setUserObject(quadrant);
			
			addItem(quadrantTreeItem);
		}
		
		//prefetch the learner state icons
		for(LearnerStateAttributeNameEnum state : LearnerStateAttributeNameEnum.VALUES()){
			
			String iconPath = LearnerStateIconMap.getEnumerationIconPath(state);
			
			if(iconPath != null){
				
				iconPath = iconPath.substring(iconPath.indexOf("images"), iconPath.length());
				
				Image.prefetch(iconPath);
			}
		}
		
		//handle styling for selecting Input elements styling does NOT apply to learner state attribute names so they don't appear selectable.
		this.addSelectionHandler(new SelectionHandler<TreeItem>() {
			
			@Override
			public void onSelection(SelectionEvent<TreeItem>  event) {
				
				boolean wasLastSelectedItemAnAttribute = lastSelectedTreeItem == null 
						|| (lastSelectedTreeItem.getUserObject() != null && lastSelectedTreeItem.getUserObject() instanceof Attribute);
				
				boolean isCurrentSelectedItemAnAnAttribute = event.getSelectedItem() != null
						&& event.getSelectedItem().getUserObject() instanceof Attribute;
				
				if(wasLastSelectedItemAnAttribute && isCurrentSelectedItemAnAnAttribute){
					
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
	 * If an Attribute is selected then it is removed and the next attribute is
	 * automatically selected.
	 * 
	 * @return True if the currently selected Attribute is removed, False
	 * otherwise.
	 */
	public boolean removeSelectedAttribute() {
		if(getSelectedAttribute() == null) {
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
			TreeItem grandparent = parentOfChildToRemove.getParentItem();
			int parentOfChildToRemoveIndex = grandparent.getChildIndex(parentOfChildToRemove);
			parentOfChildToRemove.remove();
			
			//Identify the parent whose child will be the next selected. If
			//there was a subsequent sibling node, use that. Otherwise use the
			//previous sibling node.
			if(parentOfChildToRemoveIndex < grandparent.getChildCount()) {
				parentOfChildToSelect = grandparent.getChild(parentOfChildToRemoveIndex);
			} else {
				parentOfChildToSelect = grandparent.getChild(parentOfChildToRemoveIndex - 1);
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
	 * Determines which Quadrant the currently selected attribute is within.
	 * @return Quadrant of the currently selected attribute or null of an
	 * attribute isn't currently selected.
	 */
	public MerrillQuadrantEnum getSelectedAttributesQuadrant() {
		if(getSelectedAttribute() == null) {
			return null;
		}
		
		TreeItem selectedItem = getSelectedItem();
		TreeItem quadrantItem = selectedItem.getParentItem().getParentItem();
		MerrillQuadrantEnum quadrant = (MerrillQuadrantEnum)quadrantItem.getUserObject();
		
		return quadrant;
	}
	
	/**
	 * 
	 * @return Currently selected attribute, null if no attribute is selected.
	 */
	public Attribute getSelectedAttribute() {
		TreeItem selectedItem = getSelectedItem();
		
		if(selectedItem == null) {
			return null;
		}
		
		Object userObject = selectedItem.getUserObject();
		if(!(userObject instanceof Attribute)) {
			return null;
		}
		
		Attribute selectedAttribute = (Attribute)userObject;
		return selectedAttribute;
	}
	
	/**
	 * Replaces the existing attributes with the supplied attributes. The first
	 * attribute is automatically selected.
	 * 
	 * @param ruleAttributes Attributes in the rule quadrant.
	 * @param exampleAttributes Attributes in the example quadrant.
	 * @param recallAttributes Attributes in the recall quadrant.
	 * @param practiceAttributes Attributes in the practice quadrant.
	 */
	public void setAttributes(
			List<Attribute> ruleAttributes,
			List<Attribute> exampleAttributes,
			List<Attribute> recallAttributes,
			List<Attribute> practiceAttributes) {
		clearAttributes();
		
		boolean firstSelection = true;
		for(Attribute attribute : ruleAttributes) {
			addAttribute(MerrillQuadrantEnum.RULE, attribute, firstSelection);
			if(firstSelection) {
				firstSelection = false;
			}
		}
		for(Attribute attribute : exampleAttributes) {
			addAttribute(MerrillQuadrantEnum.EXAMPLE, attribute, firstSelection);
			if(firstSelection) {
				firstSelection = false;
			}
		}
		for(Attribute attribute : recallAttributes) {
			addAttribute(MerrillQuadrantEnum.RECALL, attribute, firstSelection);
			if(firstSelection) {
				firstSelection = false;
			}
		}
		for(Attribute attribute : practiceAttributes) {
			addAttribute(MerrillQuadrantEnum.PRACTICE, attribute, firstSelection);
			if(firstSelection) {
				firstSelection = false;
			}
		}
	}
	
	/**
	 * Adds a new attribute to the tree.
	 * 
	 * @param quadrant Quadrant the attribute belongs to.
	 * @param attribute Attribute to add to the tree.
	 * @param selectAndFireEvents True if you want to auto-select the attribute.
	 */
	public void addAttribute(MerrillQuadrantEnum quadrant, Attribute attribute, boolean selectAndFireEvents) {

		Image deleteButton = new Image(GatClientBundle.INSTANCE.cancel_image());
		deleteButton.getElement().getStyle().setProperty("display", "inline");
		deleteButton.getElement().getStyle().setProperty("marginRight", "5px");
		
		Label nameLabel = new Label(attribute.getValue());
		nameLabel.getElement().getStyle().setProperty("display", "inline");
		
		FlowPanel itemWrapper = new FlowPanel();
		itemWrapper.add(deleteButton);
		itemWrapper.add(nameLabel);
		
		//Create the attribute's TreeItem
		TreeItem attributeTreeItem = new TreeItem();
		attributeTreeItem.setWidget(itemWrapper);
		attributeTreeItem.setUserObject(attribute);
		
		//Add the TreeItem to its parent.
		String attributeType = attribute.getType();
		LearnerStateAttributeNameEnum learnerState = LearnerStateAttributeNameEnum.valueOf(attributeType);
		TreeItem parentTreeItem = getLearnerStateTreeItem(quadrant, learnerState);
		parentTreeItem.addItem(attributeTreeItem);
		
		//Make sure the parent and grandparent are open.
		parentTreeItem.setState(true);
		parentTreeItem.getParentItem().setState(true);
		
		//Select the new item if appropriate.
		if(selectAndFireEvents) {
			setSelectedItem(attributeTreeItem, true);
		}		
		
		deleteButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent arg0) {
				if(removeAttributeCmd != null) {
					removeAttributeCmd.execute();
				}
			}
			
		});
	}
	
	public void setRemoveAttributeCommand(ScheduledCommand cmd) {
		removeAttributeCmd = cmd;
	}
	
	private TreeItem getLearnerStateTreeItem(MerrillQuadrantEnum quadrant, LearnerStateAttributeNameEnum learnerState) {
		//Check the existing tree structure to see if the tree item already exists.
		TreeItem quadrantTreeItem = getQuadrantTreeItem(quadrant);
		TreeItem learnerStateTreeItem = null;
		for(int x = 0; x < quadrantTreeItem.getChildCount(); x++) {
			TreeItem childTreeItem = quadrantTreeItem.getChild(x);
			LearnerStateAttributeNameEnum childLearnerState = (LearnerStateAttributeNameEnum)childTreeItem.getUserObject();
			if(childLearnerState == learnerState) {
				learnerStateTreeItem = childTreeItem;
				break;
			}
		}
		
		//If it doesn't already exists, then add it.
		if(learnerStateTreeItem == null) {
			learnerStateTreeItem = new TreeItem();
			
			FlowPanel flowPanel = new FlowPanel();
			flowPanel.getElement().getStyle().setProperty("paddingTop", "5px");
			
			String iconPath = null;
			
			try{
				iconPath = LearnerStateIconMap.getEnumerationIconPath(learnerState);
				
			} catch(@SuppressWarnings("unused") EnumerationNotFoundException e){
				//if no icon could be found matching the learner state, don't add an icon
			}
			
			Image icon = null;
			
			if(iconPath != null && iconPath.indexOf("images") != -1){
				
				iconPath = iconPath.substring(iconPath.indexOf("images"), iconPath.length());
				
				icon = new Image();
				icon.setUrl(iconPath);
				icon.setAltText("An icon for a Learner State Attribute");
				icon.getElement().getStyle().setProperty("float", "left");
				icon.getElement().getStyle().setProperty("marginRight", "5px");
				icon.getElement().getStyle().setProperty("cursor", "default");
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
			
			learnerStateTreeItem.setWidget(flowPanel);

			learnerStateTreeItem.setUserObject(learnerState);
			
			quadrantTreeItem.addItem(learnerStateTreeItem);
			
			/* 
			 * Nick: Bugfix to set full width in tree item parent. See https://code.google.com/p/google-web-toolkit/issues/detail?id=5119. 
			 * 
			 * It's possible GWT may change the structure of tree items in the future, so I've added null checks in case elements are removed.
			 *  
			 * This code will not work until the tree item has at least one child, hence why it is used after a child is added. 
			 */
			Element div = quadrantTreeItem.getElement();			
			if(div != null){
				
				Element table = DOM.getFirstChild(div);
				if(table != null){
					
					Element tbody = DOM.getFirstChild(table);
					if(tbody != null){
						
						Element tr = DOM.getFirstChild(tbody);
						if(tr != null){
							
							Element td = DOM.getChild(tr, 1);
							if(td != null){
								
								td.getStyle().setWidth(100, Unit.PCT);
							}
						}
					}
				}
			}
		}
		
		return learnerStateTreeItem;
	}
	
	private TreeItem getQuadrantTreeItem(MerrillQuadrantEnum quadrant) {
		int size = getItemCount();
		for(int x = 0; x < size; x++) {
			TreeItem treeItem = getItem(x);
			MerrillQuadrantEnum treeItemsQuadrant = (MerrillQuadrantEnum)treeItem.getUserObject();
			if(quadrant == treeItemsQuadrant) {
				return treeItem;
			}
		}
		return null;
	}
	
	private void clearAttributes() {
		int size = getItemCount();
		for(int x = 0; x < size; x++) {
			TreeItem treeItem = getItem(x);
			treeItem.removeItems();
		}
	}
}
