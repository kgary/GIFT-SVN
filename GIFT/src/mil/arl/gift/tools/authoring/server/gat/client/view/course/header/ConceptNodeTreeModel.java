/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import generated.course.ConceptNode;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

/**
 * Used in conjunction with a CellTree to display a ConceptNode (and its
 * children) and allow the user to edit the names inline.
 * 
 * @author elafave
 *
 */
public class ConceptNodeTreeModel implements TreeViewModel {

	/**
	 * The same cell object is used to display all of the ConceptNodes. The
	 * cell we use is actually a TextInputCell but the only way I could find to
	 * use the TextInputCell within a TreeViewModel was to wrap it inside of
	 * a CompositeCell. It is rather odd that we're using a CompositeCell to
	 * wrap around a single Cell but it works.
	 */
	private CompositeCell<ConceptNode> conceptNodeCell;
	
	/**
	 * Maps a ConceptNode to the ListDataProvider that is displayed beneath it.
	 * The null key points to a ListDataProvider that contains the single root
	 * ConceptNode. This is populated all at once when the setRoot method is
	 * called.
	 */
	private HashMap<ConceptNode, ListDataProvider<ConceptNode>> dataProviders = new HashMap<ConceptNode, ListDataProvider<ConceptNode>>();

	/**
	 * Maps a ConceptNode to the NodeInfo that is linked to it. The null key
	 * points to a NodeInfo associated with the single root ConceptNode. This
	 * is lazily populated as the GWT API calls the getNodeInfo method.
	 */
	private HashMap<ConceptNode, DefaultNodeInfo<ConceptNode>> nodeInfos = new HashMap<ConceptNode, DefaultNodeInfo<ConceptNode>>();
	
	/**
	 * An action used to edit concepts
	 */
	private Delegate<ConceptNode> editConceptAction = null;
	
	/**
	 * An action used to add child concepts
	 */
	private Delegate<ConceptNode> addChildConceptAction = null;
	
	/**
	 * An action used to remove concepts
	 */
	private Delegate<ConceptNode> removeConceptAction = null;

	/**
	 * Initializes the model, cells, field updaters, etc.
	 */
	public ConceptNodeTreeModel(boolean showOptions) {		
		super();
		
		//Create the cell that'll be used to display the concepts
		HasCell<ConceptNode, String> hasCell = new HasCell<ConceptNode, String>(){
			
			private TextCell cell = new TextCell();
			
			@Override
			public Cell<String> getCell() {
				return cell;
			}

			@Override
			public FieldUpdater<ConceptNode, String> getFieldUpdater() {
				return null;
			}

			@Override
			public String getValue(ConceptNode conceptNode) {
				return conceptNode.getName();
			}
			
		};
		ArrayList<HasCell<ConceptNode, ?>> hasCells = new ArrayList<HasCell<ConceptNode, ?>>();
		hasCells.add(hasCell);
		
		if(showOptions) {
			hasCells.add(new ConceptNodeRemoveHasCell("Remove", new Delegate<ConceptNode>() {
	
				@Override
				public void execute(ConceptNode key) {
					
					if(removeConceptAction != null){
						removeConceptAction.execute(key);
					}
				}
			}));
			hasCells.add(new ConceptNodeEditHasCell("Edit", new Delegate<ConceptNode>() {
	
				@Override
				public void execute(ConceptNode key) {
					
					if(editConceptAction != null){
						editConceptAction.execute(key);
					}
				}
			}));
			hasCells.add(new ConceptNodeAddChildHasCell("Add Child", new Delegate<ConceptNode>() {
	
				@Override
				public void execute(ConceptNode key) {
					
					if(addChildConceptAction != null){
						addChildConceptAction.execute(key);
					}
				}
			}));	
	    }
		
		conceptNodeCell = new CompositeCell<ConceptNode>(hasCells);
		
		//This is the data provider that'll be used right under the title and
		//for now at least it will have nothing in it.
		dataProviders.put(null, new ListDataProvider<ConceptNode>());
	}
	
	

	@Override
	public <T> NodeInfo<?> getNodeInfo(T node) {
		if(node == null) {
			DefaultNodeInfo<ConceptNode> nodeInfo = getNodeInfo(null);
			return nodeInfo;
		} else if(node instanceof String) {
			DefaultNodeInfo<ConceptNode> nodeInfo = getNodeInfo(null);
			return nodeInfo;
		} else if(node instanceof ConceptNode) {
			ConceptNode conceptNode = (ConceptNode)node;
			DefaultNodeInfo<ConceptNode> nodeInfo = getNodeInfo(conceptNode);
			return nodeInfo;
		}
		
		return null;
	}

	@Override
	public boolean isLeaf(Object node) {
		
		if(node instanceof ConceptNode) {
			ConceptNode conceptNode = (ConceptNode)node;
			if(conceptNode.getConceptNode().isEmpty()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Clears tree and all of the backend data structures we use to maintain it.
	 */
	public void clear() {
		
		//Remove the node under the "title" node. I believe this will
		//implicitly get rid or all of the other nodes as well.
		if(dataProviders.get(null) != null && dataProviders.get(null).getList() != null){
			dataProviders.get(null).getList().clear();
			dataProviders.get(null).flush();
			dataProviders.get(null).refresh();
		}
		
		//Get rid of the old DataProviders we stored.
		
		if(dataProviders.keySet() != null){
			HashSet<ConceptNode> keys = new HashSet<ConceptNode>(dataProviders.keySet());
			for(ConceptNode key : keys) {
				//Don't get rid of the empty top level data provider.
				if(key != null) {
					dataProviders.remove(key);
				}
			}
		}
		
		nodeInfos.clear();
	}
	
	/**
	 * Gets the DataProviders that are used to display the nodes in the tree.
	 * 
	 * @return a Map from each ConceptNode in the tree to the ListDataProvider
	 * that holds its children. The null key points to a ListDataProvider that
	 * contains the root node. You can use these ListDataProviders to add/remove
	 * nodes.
	 */
	public HashMap<ConceptNode, ListDataProvider<ConceptNode>> getDataProviders() {
		return dataProviders;
	}
	
	/**
	 * Clears the tree and all of the backend data structures used to maintain
	 * and then repopulates it based off the new ConceptNode.
	 * @param node ConceptNode to display.
	 */
	public void setRoot(ConceptNode node) {
		clear();
		
		//Create the data providers for the child of the root ConceptNode.
		populateDataProviders(node);
		
		//Add the root ConceptNode in the highest level Data Provider.
		
		if(dataProviders.get(null) == null){
			dataProviders.put(null, new ListDataProvider<ConceptNode>());
		}
		
		dataProviders.get(null).getList().add(node);
	}
	
	private DefaultNodeInfo<ConceptNode> getNodeInfo(ConceptNode conceptNode) {
		
		DefaultNodeInfo<ConceptNode> nodeInfo = nodeInfos.get(conceptNode);
		
		if(nodeInfo == null) {
			ListDataProvider<ConceptNode> dataProvider = dataProviders.get(conceptNode);
			nodeInfo = new DefaultNodeInfo<ConceptNode>(dataProvider, conceptNodeCell, null, null);
			nodeInfos.put(conceptNode, nodeInfo);
		}
		
		return nodeInfo;
	}
	
	private void populateDataProviders(ConceptNode conceptNode) {
		ListDataProvider<ConceptNode> dataProvider = createDataProvider(conceptNode);
		dataProviders.put(conceptNode, dataProvider);
		
		List<ConceptNode> children = conceptNode.getConceptNode();
		for(ConceptNode child : children) {
			populateDataProviders(child);
		}
	}
	
	private ListDataProvider<ConceptNode> createDataProvider(ConceptNode conceptNode) {
		List<ConceptNode> childConceptNodes = conceptNode.getConceptNode();
		
		ListDataProvider<ConceptNode> dataProvider = new ListDataProvider<ConceptNode>();
		dataProvider.getList().addAll(childConceptNodes);
		
		return dataProvider;
	}

	/**
	 * Sets the action to execute when a concept node's edit button is pressed
	 * 
	 * @param editConceptAction the action to execute
	 */
	public void setEditConceptAction(Delegate<ConceptNode> editConceptAction) {
		this.editConceptAction = editConceptAction;
	}
	
	/**
	 * Sets the action to execute when a concept node's add child button is pressed
	 * 
	 * @param addChildConceptAction the action to execute
	 */
	public void setAddChildConceptAction(Delegate<ConceptNode> addChildConceptAction) {
		this.addChildConceptAction = addChildConceptAction;
	}
	
	/**
	 * Sets the action to execute when a concept node's remove button is pressed
	 * 
	 * @param editConceptAction the action to execute
	 */
	public void setRemoveConceptAction(Delegate<ConceptNode> removeConceptAction) {
		this.removeConceptAction = removeConceptAction;
	}

	/**
	 * An object used to render the edit button for each row and handle its actions
	 * 
	 * @author nroberts
	 */
	private class ConceptNodeEditHasCell implements HasCell<ConceptNode, ConceptNode>{

		private ActionCell<ConceptNode> cell;
		
		public ConceptNodeEditHasCell(String text, Delegate<ConceptNode> delegate){
			cell = new ActionCell<ConceptNode>(text, delegate){
				
				@Override
			    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            ConceptNode value, SafeHtmlBuilder sb) {
								
					sb.appendHtmlConstant("<span title='Edit this concept' style='padding-left: 20px; float: right;'>");
					
					Image buttonImage = new Image(GatClientBundle.INSTANCE.edit_image().getSafeUri().asString());
					
			        SafeHtml html = SafeHtmlUtils.fromTrustedString(buttonImage.toString());
			        sb.append(html);
			        
			        sb.appendHtmlConstant("</span>");
				}
			};
		}
		
		@Override
		public Cell<ConceptNode> getCell() {			
			return cell;
		}
		
		@Override
		public FieldUpdater<ConceptNode, ConceptNode> getFieldUpdater() {
			return null;
		}

		@Override
		public ConceptNode getValue(ConceptNode key) {
			return key;
		}
	}
	
	/**
	 * An object used to render the add child button for each row and handle its actions
	 * 
	 * @author nroberts
	 */
	private class ConceptNodeAddChildHasCell implements HasCell<ConceptNode, ConceptNode>{

		private ActionCell<ConceptNode> cell;
		
		public ConceptNodeAddChildHasCell(String text, Delegate<ConceptNode> delegate){
			cell = new ActionCell<ConceptNode>(text, delegate){
				
				@Override
			    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            ConceptNode value, SafeHtmlBuilder sb) {
								
					sb.appendHtmlConstant("<span title='Add a child to this concept' style='padding-left: 20px; float: right;'>");
					
					Image buttonImage = new Image(GatClientBundle.INSTANCE.add_child_image().getSafeUri().asString());
					
			        SafeHtml html = SafeHtmlUtils.fromTrustedString(buttonImage.toString());
			        sb.append(html);
			        
			        sb.appendHtmlConstant("</span>");
				}
			};
		}	
		
		@Override
		public Cell<ConceptNode> getCell() {			
			return cell;
		}
		
		@Override
		public FieldUpdater<ConceptNode, ConceptNode> getFieldUpdater() {
			return null;
		}

		@Override
		public ConceptNode getValue(ConceptNode key) {
			return key;
		}
	}
	
	
	/**
	 * An object used to render the remove button for each row and handle its actions
	 * 
	 * @author nroberts
	 */
	private class ConceptNodeRemoveHasCell implements HasCell<ConceptNode, ConceptNode>{

		private ActionCell<ConceptNode> cell;
		
		public ConceptNodeRemoveHasCell(String text, Delegate<ConceptNode> delegate){
			cell = new ActionCell<ConceptNode>(text, delegate){
				
				@Override
			    public void render(com.google.gwt.cell.client.Cell.Context context, 
		            ConceptNode value, SafeHtmlBuilder sb) {
								
					sb.appendHtmlConstant("<span title='Remove this concept' style='padding-left: 20px; padding-right: 20px; float: right;'>");
					
					Image buttonImage = new Image(GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString());
					
			        SafeHtml html = SafeHtmlUtils.fromTrustedString(buttonImage.toString());
			        sb.append(html);
			        
			        sb.appendHtmlConstant("</span>");
				}
			};
		}
		

		@Override
		public Cell<ConceptNode> getCell() {			
			return cell;
		}
		
		@Override
		public FieldUpdater<ConceptNode, ConceptNode> getFieldUpdater() {
			return null;
		}

		@Override
		public ConceptNode getValue(ConceptNode key) {
			return key;
		}
	}
}
