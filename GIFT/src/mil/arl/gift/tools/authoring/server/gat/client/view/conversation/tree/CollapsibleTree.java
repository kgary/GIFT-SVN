/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.UnorderedList;

import com.github.gwtd3.api.Coords;
import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.Sort;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.behaviour.Zoom;
import com.github.gwtd3.api.behaviour.Zoom.ZoomEventType;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Transition;
import com.github.gwtd3.api.core.UpdateSelection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.functions.KeyFunction;
import com.github.gwtd3.api.layout.HierarchicalLayout.Node;
import com.github.gwtd3.api.layout.Link;
import com.github.gwtd3.api.layout.Tree;
import com.github.gwtd3.api.svg.Line;
import com.github.gwtd3.api.svg.Line.InterpolationMode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;

import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.assessments.AssessmentEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.assessments.AssessmentEditor.UpdateNodeCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.TextAreaDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.TreeNodeEnum;

/**
 * An interactive tree widget used to represent a conversation.
 * 
 * @author bzahid
 */
public class CollapsibleTree extends FlowPanel implements RequiresResize{
	
	/** UI constants of the tree widget. */
	private static final int DURATION = 750;
	private static final MyResources CSS = Bundle.INSTANCE.css();
	
	/** The amount of vertical space between parent nodes and their children */
	private static final int HEIGHT_BETWEEN_NODES = 100;
	
	/** The maximum width of a node */
	private static final int MAX_NODE_WIDTH = 300;
	
	/** The maximum number of rows of text that can be contained within a node*/
	private static final int MAX_NODE_ROWS = 3;
	
	/** The height of each row of text in a node */
	private static final int NODE_ROW_HEIGHT = 25;
	
	/** The width of node icons */
	private static final int NODE_ICON_WIDTH = 45;
	
	/** The width of node assessment icons */
	private static final int ASSESSMENT_ICON_WIDTH = 24;
	
	/** The default zoom level */
	protected static final double DEFAULT_ZOOM_LEVEL = 1;
	
	/** Tree node icons */
	private static final String END_ICON = "\uf04d"; 		//unicode for Font Awesome's 'stop' character
	private static final String CHOICE_ICON = "\uf007"; 	//unicode for Font Awesome's 'user' character
	private static final String MESSAGE_ICON = "\uf075";	//unicode for Font Awesome's 'comment' character
	private static final String QUESTION_ICON = "\uf059";	//unicode for Font Awesome's 'question-circle' character
	private static final String ADD_ICON = "\uf067";		//unicode for Font Awesome's 'plus' character
	private static final String ASSESSMENT_ICON = "images/clipboard_check.png";
	
	/** Validation Messages **/
	private static final String INVALID_CHOICE_LENGTH_MSG = "Text must be at least 1 character.";
	private static final String INVALID_NODE_LENGTH_MSG = "Text must be at least 3 characters.";
	
	/** D3 layout components */
	private Tree tree = null;
	private TreeNode root = null;
	private TreeNode selectedNode = null;
	private TreeLink selectedLink = null;
	private TreeNodeEnum newNodeType;
	private Line diagonal = null;
	private Selection svg = null;
	
	/** Whether or not the user is creating a link between two nodes. */
	private boolean linkNodes = false;
	
	/** Keeps track of tree ids. */
	private int treeLength = 0;	
	
	/** Context menus */
	private final PopupPanel contextMenu = new PopupPanel();
	private final PopupPanel linkMenu = new PopupPanel();
	private AssessmentEditor assessmentPanel;
	
	/** Context menu options */
	private final AnchorListItem selectItem = new AnchorListItem("Select Node");
	private final AnchorListItem addChoiceItem = new AnchorListItem("Add Choice");
	private final AnchorListItem addMessageItem = new AnchorListItem("Add Message");
	private final AnchorListItem addQuestionItem = new AnchorListItem("Add Question");
	private final AnchorListItem editTextItem = new AnchorListItem("Edit Message");
	private final AnchorListItem editAssessmentItem = new AnchorListItem("Edit Assessment");
	private final AnchorListItem moveLeftItem = new AnchorListItem("Move Left");
	private final AnchorListItem moveRightItem = new AnchorListItem("Move Right");
	private final AnchorListItem insertMessageBeforeItem = new AnchorListItem("Insert Message Before");
	private final AnchorListItem insertQuestionBeforeItem = new AnchorListItem("Insert Question Before");
	private final AnchorListItem insertMessageAfterItem = new AnchorListItem("Insert Message After");
	private final AnchorListItem insertQuestionAfterItem = new AnchorListItem("Insert Question After");
	private final AnchorListItem deleteItem = new AnchorListItem("Delete Path");
	private final AnchorListItem deleteNodeItem = new AnchorListItem("Delete Node");
	private final Divider upperDiv = new Divider();
	private final Divider lowerDiv = new Divider();
	
	/** Edit & Add tree node dialogs */
	private TextAreaDialog editTextDialog;
	private TextAreaDialog addNodeDialog;
	private TextAreaDialog insertNodeAfterDialog;
	private TextAreaDialog insertNodeBeforeDialog;
	
	/** The zoom object used to modify the tree's position and scale*/
	private Zoom zoom;
	
	/** The main SVG element containing the conversation tree */
	private Selection mainSvg;
	
	/** The vertical position that the mouse started at when the user is scrolling by dragging the mouse */
	private int verticalScrollingMouseStart = 0;
	
	/** The vertical position that scroll bar started at when the user is scrolling by dragging the mouse */
	private int verticalScrollingViewStart = 0;
	
	/** The horizontal position that the mouse started at when the user is scrolling by dragging the mouse */
	private int horizontalScrollingMouseStart = 0;
	
	/** The horizontal position that scroll bar started at when the user is scrolling by dragging the mouse */
	private int horizontalScrollingViewStart = 0;
	
	/** whether to allow question answer node concept assessments to be authored */
	private boolean allowAssessments = true;
		
	/** Instance of the logger */
	private static Logger logger = Logger.getLogger(CollapsibleTree.class.getName());
	
	/** Interface to allow CSS file access */
	public interface Bundle extends ClientBundle {
		public static final Bundle INSTANCE = GWT.create(Bundle.class);

		@Source("TreeStyles.css")
		public MyResources css();
	}
	
	/** Hidden HTML element for attaching objects to*/
	private HTML dom;

	/** Interface to allow CSS style name access */
	interface MyResources extends CssResource {

		String link();

		String multiLink();
		
		String node();
		
		String selectedNodeRect();
		
		String selectableNodeRect();

		String border();
		
		String icon();
		
		String text();
		
		String assessmentIcon();
	}
	
	/**
	 * Creates a collapsible tree widget.
	 */
	public CollapsibleTree() {
		
		// Make sure the css style names are accessible before creating the tree.
		Bundle.INSTANCE.css().ensureInjected();
		
		// Setup the tree layout
		tree = D3.layout().tree()
				.separation(getSortFunction()) //assign a function to handle the spacing between nodes so nodes don't overlap
				.nodeSize(1, 1) //set the minimum size of each node (needed for separation to work properly)
		;
		
		// Setup path drawing
		diagonal = D3.svg().line()
				.interpolate(InterpolationMode.BASIS)
				.x(new DatumFunction<Double>() {
					
					@Override
					public Double apply(Element context, Value d, int index) {
						return d.getProperty("x").asDouble();
					}
				})
				.y(new DatumFunction<Double>() {
				
					@Override
					public Double apply(Element context, Value d, int index) {
						return d.getProperty("y").asDouble();
					}
				})
			;
		
		// Add a scroll panel to wrap the main SVG panel
		final ScrollPanel scroller = new ScrollPanel();
		scroller.setSize("100%", "100%");
		add(scroller);
		
		// Setup panning behavior
		zoom = D3.behavior().zoom().on(ZoomEventType.ZOOMSTART, new DatumFunction<Void>() {

			@Override
			public Void apply(Element context, Value d, int index) {
				
				verticalScrollingMouseStart = (int) D3.mouseY(scroller.getElement());
				verticalScrollingViewStart = scroller.getVerticalScrollPosition();
				
				horizontalScrollingMouseStart = (int) D3.mouseX(scroller.getElement());
				horizontalScrollingViewStart = scroller.getHorizontalScrollPosition();

				return null;
			}

		}).on(ZoomEventType.ZOOM, new DatumFunction<Void>() {

			@Override
			public Void apply(Element context, Value d, int index) {	
				
				final int relativeMouseX = (int) D3.mouseX(scroller.getElement());
				final int relativeMouseY = (int) D3.mouseY(scroller.getElement());				
					
				int xScroll = horizontalScrollingViewStart + horizontalScrollingMouseStart - relativeMouseX;
				
				if(0 < xScroll && xScroll < scroller.getElement().getScrollWidth()){						
					scroller.setHorizontalScrollPosition(xScroll);
				}
				
				int yScroll = verticalScrollingViewStart + verticalScrollingMouseStart - relativeMouseY;				
				
				if(0 < yScroll && yScroll < scroller.getElement().getScrollHeight()){
					scroller.setVerticalScrollPosition(yScroll);
				}

				return null;
			}

		});
		
		// Add the svg
		mainSvg = D3.select(scroller).append("svg")
				.style("min-width", "100%")
				.style("min-height", "100%")
				.style("display", "block")
				.call(zoom)
				.on("dblclick.zoom", null) //Disable zooming on double click, since we're adding our own double click handler
				.on("wheel.zoom", null) //Disable zooming via the mouse wheel in Chrome and Firefox
				.on("mousewheel.zoom", null) //Disable zooming via the mouse wheel in IE
		;
		
		//add the group that will contain the conversation tree
		svg = mainSvg.append("g");
		
		//Build the arrows that will be added to the links connecting nodes
		svg.append("svg:defs").selectAll("marker")
			.data(Array.fromJavaArray(new String[]{"end"}))
		  .enter().append("svg:marker")
		  .attr("id", "end")						//used to attach marker to paths
			.attr("viewBox", "0 -7.5 15 15") 		//defines the relative bounds within which the marker can be drawn
		    .attr("refX", 12) 						//x offset
		    .attr("refY", 0) 						//y offset
		    .attr("markerWidth", 9)					//scales the marker's width to the given size
		    .attr("markerHeight", 9)				//scales the marker's height to the given size
		    .attr("markerUnits", "userSpaceOnUse") 	//prevents scaling with line width
		    .attr("orient", "auto")					//makes the marker point in the direction of the line
		    .attr("fill", "black")
		  .append("svg:path")
		    .attr("d", "M0,-7.5L12,0L0,7.5") 		//actually draws the marker
		    .attr("stroke", "none")
		;
		
		// Setup the assessment editor
		assessmentPanel = new AssessmentEditor(new UpdateNodeCallback() {
			@Override
			public void update() {
				updateTree();
			}
		});
		
		initLinkMenu();
		initContextMenu();
		initCreateNodeDialogs();
		
		//Build the panel that will allow users to zoom in/out on the tree
		FlowPanel zoomPanel = new FlowPanel();
		zoomPanel.addStyleName("conversationTreeZoomPanel");
		
		ButtonGroup zoomGroup = new ButtonGroup();
		
		Button zoomInButton = new Button();
		zoomInButton.setIcon(IconType.SEARCH_PLUS);
		zoomInButton.setTitle("Zoom in");		
		zoomGroup.add(zoomInButton);
		
		zoomInButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				zoom.scale(zoom.scale() * 1.25);

				if(root != null){
					updateTree();
				}
			}
		});
		
		Button zoomOutButton = new Button();
		zoomOutButton.setIcon(IconType.SEARCH_MINUS);
		zoomOutButton.setTitle("Zoom out");
		zoomGroup.add(zoomOutButton);
		
		zoomOutButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				zoom.scale(zoom.scale()/1.25);

				if(root != null){
					updateTree();
				}
			}
		});
		
		Button resetZoomButton = new Button();
		resetZoomButton.setIcon(IconType.CROSSHAIRS);
		resetZoomButton.setTitle("Reset zoom");
		zoomGroup.add(resetZoomButton);
		
		resetZoomButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				zoom.scale(DEFAULT_ZOOM_LEVEL);

				if(root != null){
					updateTree();
				}
			}
		});
		
		zoomPanel.add(zoomGroup);
		
		add(zoomPanel);
		
		//Make this widget take up the size of its container
		setSize("100%", "100%");
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				
				if(root != null){
					updateTree();
				}
			}
		});
		
		//Apply the initial zoom level
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				
				zoom.scale(DEFAULT_ZOOM_LEVEL);
				
				if(root != null){
					updateTree();
				}
			}
		});		
		
		dom = new HTML();
		dom.getElement().getStyle().setVisibility(com.google.gwt.dom.client.Style.Visibility.HIDDEN);

	}

	/**
	 * Updates the tree widget.
	 */
	public void updateTree() {
		update(root);
	}
	
	/**
	 * Set whether to allow question answer node concept assessments to be authored
	 * @param allow true if assessment authoring should be allowed.  A reason for not allowing
	 * might be that the tree is presented during remediation or to deliver content.
	 */
	public void setAllowConceptAssessments(boolean allow){
	    this.allowAssessments = allow;
	}
	
	/**
	 * Creates a collapsible tree from a JSON string representation.
	 * 
	 * @param treeJSONStr The JSON string representation of a conversation tree.
	 */
	public void loadTree(String treeJSONStr) {
		root = (TreeNode) makeTree(treeJSONStr);
		update(root);
	}
	
	/**
	 * Creates a new tree and refreshes the view.
	 */
	public void newTree() {
		root = (TreeNode) makeTree();
		update(root);
	}
	
	/**
	 * Gets a JSON string representation of the conversation tree.
	 * 
	 * @return a JSON string representation of the conversation tree.
	 */
	public String getTreeJSONStr() {
		return stringify(root);
	}
	
	/**
	 * Gets the id of the first node in the conversation tree.
	 * 
	 * @return The id of the first node in the conversation tree.
	 */
	public int getStartNodeId() {
		return root.id();
	}
	
	/**
	 * Displays the appropriate context menu for the selected tree node.
	 */
	private void showContextMenu() {
		
		// Get the selected tree node type
		TreeNodeEnum nodeType = TreeNodeEnum.fromName(selectedNode.getType());
		
		// Initially enable all of the items
		selectItem.setEnabled(true);
		deleteItem.setEnabled(true);
		deleteNodeItem.setEnabled(true);
		addMessageItem.setEnabled(true);
		addQuestionItem.setEnabled(true);
		moveLeftItem.setEnabled(false);
		moveRightItem.setEnabled(false);
		
		// Set initial visibility
		selectItem.setVisible(true);
		deleteItem.setVisible(true);
		deleteNodeItem.setVisible(true);
		addChoiceItem.setVisible(true);
		addMessageItem.setVisible(true);
		addQuestionItem.setVisible(true);
		insertMessageAfterItem.setVisible(true);
		insertQuestionAfterItem.setVisible(true);
		insertMessageBeforeItem.setVisible(true);
		insertQuestionBeforeItem.setVisible(true);
		editTextItem.setVisible(true);
		editAssessmentItem.setVisible(false);
		moveLeftItem.setVisible(false);
		moveRightItem.setVisible(false);
		upperDiv.setVisible(true);
		lowerDiv.setVisible(true); 
		
		boolean hideContextMenu = false;
		
		//
		// Adjust item text and visibility based on node type
		//
		if(nodeType == TreeNodeEnum.QUESTION_NODE) {
			
			selectItem.setText("Select Choice");
			deleteNodeItem.setText("Delete Question");
			editTextItem.setText("Edit Question");
			
			addMessageItem.setVisible(false);
			addQuestionItem.setVisible(false);	
			insertMessageAfterItem.setVisible(false);
			insertQuestionAfterItem.setVisible(false);
			
			if(selectedNode.equals(root)) {
				deleteItem.setEnabled(false);
			}
			
		} else if(nodeType == TreeNodeEnum.CHOICE_NODE) {
			selectItem.setText("Select Node");
			editTextItem.setText("Edit Choice");			
			
			addChoiceItem.setVisible(false);
			editAssessmentItem.setVisible(allowAssessments);
			insertMessageBeforeItem.setVisible(false);
			insertQuestionBeforeItem.setVisible(false);
			deleteNodeItem.setVisible(false);
			
			moveLeftItem.setVisible(true);
			moveRightItem.setVisible(true);
			
			// Disable adding additional children
			boolean onlyHasEndNode = false;
			
			if(selectedNode.children() != null && selectedNode.children().length() > 0) {				
				
				if(selectedNode.children().length() == 1){
					
					TreeNode childNode = selectedNode.children().get(0).cast();
					TreeNodeEnum childNodeType = TreeNodeEnum.fromName(childNode.getType());
					
					if(TreeNodeEnum.ADD_NODE.equals(childNodeType)){
						onlyHasEndNode = true;
					}
				}
			}
			
			for(Value linkValue : root.getObjAttr("links").<Array<Value>>cast().asIterable()){
				
				TreeLink link = linkValue.as();
				
				if(selectedNode.id() == link.getSource()){
					onlyHasEndNode = false;
					break;
				}
			}
			
			if(!onlyHasEndNode){
				selectItem.setEnabled(false);
				addMessageItem.setEnabled(false);
				addQuestionItem.setEnabled(false);
			}
			
			if(selectedNode.parent().children() != null){
				
				int numChoices = 0;
				
				for(int i = 0; i < selectedNode.parent().children().length(); i++){
					
					TreeNode node = selectedNode.parent().children().get(i).cast();
					
					TreeNodeEnum type = TreeNodeEnum.fromName(node.getType());
					
					if(TreeNodeEnum.CHOICE_NODE.equals(type)){
						numChoices++;
					}
					
					if(node.equals(selectedNode)){
						
						if(i > 0){
							moveLeftItem.setEnabled(true);
						} 
						
						if(i < selectedNode.parent().children().length() - 2){
							moveRightItem.setEnabled(true);
						}
					}
				}
				
				if(numChoices < 3){
					
					//disable delete if only two choices remain
					deleteItem.setEnabled(false);					
					deleteItem.setTitle("Questions must have at least two choices");
				} else {
                    deleteItem.setTitle("");
				}
			}
			
		} else if(nodeType == TreeNodeEnum.END_NODE) {
			deleteItem.setVisible(false);
			deleteNodeItem.setVisible(false);
			selectItem.setVisible(false);
			editTextItem.setVisible(false);
			addChoiceItem.setVisible(false);
			addMessageItem.setVisible(false);
			addQuestionItem.setVisible(false);
			editAssessmentItem.setVisible(allowAssessments);
			upperDiv.setVisible(false);
			lowerDiv.setVisible(false);
			insertMessageAfterItem.setVisible(false);
			insertQuestionAfterItem.setVisible(false);
			insertMessageBeforeItem.setVisible(false);
			insertQuestionBeforeItem.setVisible(false);
			
			hideContextMenu = !allowAssessments;
			
		} else if(nodeType == TreeNodeEnum.ADD_NODE) {
			deleteItem.setVisible(false);
			deleteNodeItem.setVisible(false);
			selectItem.setVisible(false);
			editTextItem.setVisible(false);
			insertMessageBeforeItem.setVisible(false);
			insertQuestionBeforeItem.setVisible(false);
			insertMessageAfterItem.setVisible(false);
			insertQuestionAfterItem.setVisible(false);
			
			if(selectedNode.parent() != null){
			
				TreeNodeEnum parentType = TreeNodeEnum.fromName(selectedNode.parent().<TreeNode>cast().getType());
				
				if(TreeNodeEnum.QUESTION_NODE.equals(parentType)){
					addMessageItem.setVisible(false);
					addQuestionItem.setVisible(false);
				
				} else if(TreeNodeEnum.CHOICE_NODE.equals(parentType)){
					addChoiceItem.setVisible(false);
				
				} else if(TreeNodeEnum.MESSAGE_NODE.equals(parentType)){
					addChoiceItem.setVisible(false);
					
				} else {
					addMessageItem.setVisible(false);
					addQuestionItem.setVisible(false);
					addChoiceItem.setVisible(false);
				}
				
			}
			upperDiv.setVisible(false);
			lowerDiv.setVisible(false);
			
		} else {
			selectItem.setText("Select Node");
			editTextItem.setText("Edit Message");
			deleteNodeItem.setText("Delete Message");
			addChoiceItem.setVisible(false);
			
			// Disable adding additional children
			boolean onlyHasEndNode = false;
			
			if(selectedNode.children() != null && selectedNode.children().length() > 0) {
				
				if(selectedNode.children().length() == 1){
					
					TreeNode childNode = selectedNode.children().get(0).cast();
					TreeNodeEnum childNodeType = TreeNodeEnum.fromName(childNode.getType());
					
					if(TreeNodeEnum.ADD_NODE.equals(childNodeType)){
						onlyHasEndNode = true;
					}
				}
			}
			
			for(Value linkValue : root.getObjAttr("links").<Array<Value>>cast().asIterable()){
				
				TreeLink link = linkValue.as();
				
				if(selectedNode.id() == link.getSource()){
					onlyHasEndNode = false;
					break;
				}
			}
			
			if(!onlyHasEndNode){
			
				selectItem.setEnabled(false);
				addMessageItem.setEnabled(false);
				addQuestionItem.setEnabled(false);
			}
			
			// Disable delete if this is the root node
			if(selectedNode.equals(root)) {
				deleteItem.setEnabled(false);
			}
		}
		
		if(!hideContextMenu){		
		    // Position the context menu at the location of the mouse
		    showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), contextMenu);
		}
	}
	
	/**
	 * Initializes the TreeNode context menu
	 */
	private void initContextMenu() {
		
		// Handler for editing node text
		editTextItem.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				showEditNodeDialog();
				contextMenu.hide();
			}
			
		}, MouseDownEvent.getType());
		
		// Handler for selecting a child node
		selectItem.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(selectItem.isEnabled()){
					
					contextMenu.hide();
					linkNodes = true;
					updateTree();
				}
			}
			
		}, MouseDownEvent.getType());
		
		// Handler for adding a question node
		addQuestionItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if(addQuestionItem.isEnabled()) {
					contextMenu.hide();
					newNodeType = TreeNodeEnum.QUESTION_NODE;
					showAddNodeDialog();
				}
			}
			
		}, MouseDownEvent.getType());
				
		// Handler for adding a choice node
		addChoiceItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if(addChoiceItem.isEnabled()) {
					contextMenu.hide();
					newNodeType = TreeNodeEnum.CHOICE_NODE;
					showAddNodeDialog();
				}
			}
			
		}, MouseDownEvent.getType());
		
		// Handler for adding a message node
		addMessageItem.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				if(addMessageItem.isEnabled()) {
					contextMenu.hide();
					newNodeType = TreeNodeEnum.MESSAGE_NODE;
					showAddNodeDialog();
				}
			}

		}, MouseDownEvent.getType());
		
		// Handler for deleting a node path
		deleteItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if(deleteItem.isEnabled()) {
					contextMenu.hide();

					if(TreeNodeEnum.fromName(selectedNode.getType()) == TreeNodeEnum.END_NODE) {
						int index = selectedNode.parent().children().indexOf(selectedNode);
						selectedNode.parent().children().splice(index, 1);
						update((TreeNode) selectedNode.parent());
						return;
					}
					
					OkayCancelDialog.show("Delete Path", 
							"Are you sure you want to delete this " +  selectedNode.getType() + " and all of its children?", 
							"Delete Path", 
							new OkayCancelCallback() {

						@Override
						public void okay() {
							if(selectedNode.parent() != null && selectedNode.parent().children() != null) {

								try{
									// Remove associated links
									Array<TreeLink> treeLinks = root.getObjAttr("links").cast();
									for(int i = treeLinks.length() - 1; i >= 0; i--) {
										TreeLink link = treeLinks.get(i);
										if(link.getSource() == -1) {
											continue;
										}
										if(link.getSource() == selectedNode.id() || link.getTarget() == selectedNode.id()) {
											treeLinks.splice(i, 1);
										}
									}

									int index = selectedNode.parent().children().indexOf(selectedNode);
									selectedNode.parent().children().splice(index, 1);
									
									if(selectedNode.parent().children() == null 
											|| selectedNode.parent().children().length() == 0){
										
										//add an end node if the parent no longer has any children
										selectedNode.parent().<TreeNode>cast().setAttr("children", createEndNode());
									}
									
									//update the tree to apply the deletion
									updateTree();
									
									//append end nodes to any nodes that no longer have children after the deletion
									fixEndNodes();
									
								} catch(Exception e) {
									WarningDialog.error("Delete failed", "An error occurred while trying to delete associated links: " + e);
								}
								
							}
						}

						@Override
						public void cancel() {
							// nothing to do
						}

					});
				}
			}
			
		}, MouseDownEvent.getType());
		
		// Handler for deleting a node
		deleteNodeItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if(deleteNodeItem.isEnabled()) {
					contextMenu.hide();
					
					OkayCancelDialog.show("Delete " + selectedNode.getType(), 
							"Are you sure you want to delete this " +  selectedNode.getType() + "?", 
							"Delete " + selectedNode.getType(), 
							new OkayCancelCallback() {

						@Override
						public void okay() {
							
							try{
								// Remove associated links
								Array<TreeLink> treeLinks = root.getObjAttr("links").cast();
								for(int i = treeLinks.length() - 1; i >= 0; i--) {
									TreeLink link = treeLinks.get(i);
									if(link.getSource() == -1) {
										continue;
									}
									if(link.getSource() == selectedNode.id() || link.getTarget() == selectedNode.id()) {
										treeLinks.splice(i, 1);
									}
								}
								
							} catch(Exception e) {
								WarningDialog.error("Deletion failed", "An error occurred while trying to delete associated links: " + e);
								return;
							}
							
							TreeNode parent = selectedNode.parent().cast();
							TreeNode replacementChild = null;
							
							if(selectedNode.children() != null && selectedNode.children().length() > 0){
								
								if(selectedNode.children().length() == 1){
									
									TreeNode childNode = selectedNode.children().get(0).cast();
									
									TreeNodeEnum childNodeType = TreeNodeEnum.fromName(childNode.getType());
									
									if(!TreeNodeEnum.ADD_NODE.equals(childNodeType) 
											&& !TreeNodeEnum.END_NODE.equals(childNodeType)){
										
										replacementChild = childNode;
									}
									
								} else {
									
									for(int i = 0; i < selectedNode.children().length(); i++){
										
										TreeNode child = selectedNode.children().get(i).cast();
										
										if(child.children() != null && child.children().length() > 0){
											
											TreeNode grandchildNode = child.children().get(0).cast();
											
											TreeNodeEnum grandchildNodeType = TreeNodeEnum.fromName(grandchildNode.getType());
											
											if(!TreeNodeEnum.ADD_NODE.equals(grandchildNodeType) 
													&& !TreeNodeEnum.END_NODE.equals(grandchildNodeType)){
												
												if(replacementChild == null){
													replacementChild = grandchildNode;
													
												} else {
													
													WarningDialog.error("Unable to Delete Node",
															"This question could not be deleted because it contains choices "
															+ "leading to multiple conversation paths that could not be merged together."
															+ "<br/><br/>If you want to replace this question with one of its available "
															+ "paths, please delete all of the other paths and then retry the deletion."
															+ "<br/><br/>Alternatively, if you want to delete the question and <i>all</i> "
															+ "of its available paths, please use the \"Delete Path\" option instead.");
													return;
												}
											}
										}
									}
								}
							}
							
							if(selectedNode.equals(root)){
								
								if(replacementChild != null){
								
									replacementChild.setAttr("links", root.getObjAttr("links"));
									root = replacementChild;
								
								} else {
									
									WarningDialog.error("Unable to Delete Node", 
											"This node cannot be deleted because it is the only remaining non-terminating "
											+ "node in the conversation.<br/><br/>If you would like to replace this node, please add or "
											+ "insert a new node first and then delete this node.");
									return;
								}
								
							} else if(parent != null){
								
								int index = parent.children().indexOf(selectedNode);
								parent.children().splice(index, 1);
								
								if(parent.children() == null 
										|| parent.children().length() == 0){
									
									if(replacementChild != null){					
										
										Array<Node> newChildren = Array.create();
										newChildren.push(replacementChild);
										
										selectedNode.parent().<TreeNode>cast().setAttr("children", newChildren);
										
									} else {
										
										//add an end node if the parent no longer has any children
										selectedNode.parent().<TreeNode>cast().setAttr("children", createEndNode());
									}
								}
							
							}
							
							//update the tree to apply the deletion
							updateTree();
							
							//append end nodes to any nodes that no longer have children after the deletion
							fixEndNodes();
						}

						@Override
						public void cancel() {
							// nothing to do
						}

					});
				}
			}
			
		}, MouseDownEvent.getType());
		
		// Handler for editing a choice node assessment
		editAssessmentItem.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent arg0) {
				contextMenu.hide();
				assessmentPanel.setAssessmentNode(selectedNode);
				assessmentPanel.showEditor();
			}
			
		}, MouseDownEvent.getType());
		
		moveLeftItem.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent arg0) {
				contextMenu.hide();
				
				if(selectedNode.parent() != null && selectedNode.parent().children() != null){
					
					for(int i = 0; i < selectedNode.parent().children().length(); i++){
						
						TreeNode node = selectedNode.parent().children().get(i).cast();
						
						if(node.equals(selectedNode)){
							
							if(i > 0){		
								
								//move the node's position in its parent's child array one position to the left (i.e. subtract its index)
								selectedNode.parent().children().splice(
										i - 1,
										0, 
										selectedNode.parent().children().splice(i, 1).get(0)
								);
								
								update((TreeNode) selectedNode.parent());
							}
							
							break;
						}
					}
				}
			}
			
		}, MouseDownEvent.getType());
		
		moveRightItem.addDomHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent arg0) {
				contextMenu.hide();
				
				if(selectedNode.parent() != null && selectedNode.parent().children() != null){
					
					for(int i = 0; i < selectedNode.parent().children().length() - 1; i++){
						
						TreeNode node = selectedNode.parent().children().get(i).cast();
						
						if(node.equals(selectedNode)){
							
							if(i < selectedNode.parent().children().length() - 2){		
								
								//move the node's position in its parent's child array one position to the right (i.e. add its index)
								selectedNode.parent().children().splice(
										i + 1,
										0, 
										selectedNode.parent().children().splice(i, 1).get(0)
								);
								
								update((TreeNode) selectedNode.parent());
							}
							
							break;
						}
					}
				}
			}
			
		}, MouseDownEvent.getType());	
		
		insertMessageBeforeItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(insertMessageBeforeItem.isEnabled()){
					
					contextMenu.hide();
					newNodeType = TreeNodeEnum.MESSAGE_NODE;
					showInsertNodeBeforeDialog();
				}
			}
			
		}, MouseDownEvent.getType());
		
		insertQuestionBeforeItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(insertQuestionBeforeItem.isEnabled()){
					
					contextMenu.hide();
					newNodeType = TreeNodeEnum.QUESTION_NODE;
					showInsertNodeBeforeDialog();
				}
			}
			
		}, MouseDownEvent.getType());
		
		insertMessageAfterItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(selectedNode.children() == null 
						&& selectedNode.children().length() < 0
						&& addMessageItem.isEnabled()) {
					
					contextMenu.hide();
					newNodeType = TreeNodeEnum.MESSAGE_NODE;
					showAddNodeDialog();
				
				} else if(insertMessageAfterItem.isEnabled()){
					
					contextMenu.hide();
					newNodeType = TreeNodeEnum.MESSAGE_NODE;
					showInsertNodeAfterDialog();
				}
			}
			
		}, MouseDownEvent.getType());
		
		insertQuestionAfterItem.addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				
				if(selectedNode.children() == null 
						&& selectedNode.children().length() < 0
						&& addQuestionItem.isEnabled()) {
					
					contextMenu.hide();
					newNodeType = TreeNodeEnum.QUESTION_NODE;
					showAddNodeDialog();
				
				} else if(insertQuestionAfterItem.isEnabled()){
					
					contextMenu.hide();
					newNodeType = TreeNodeEnum.QUESTION_NODE;
					showInsertNodeAfterDialog();
				}
			}
			
		}, MouseDownEvent.getType());
		
		// Create and style the context menu widget
		UnorderedList choiceMenuList = new UnorderedList();
		choiceMenuList.setStyleName(Styles.DROPDOWN_MENU);
		choiceMenuList.getElement().getStyle().setProperty("display", "block");
		choiceMenuList.add(editTextItem);
		choiceMenuList.add(editAssessmentItem);
		choiceMenuList.add(moveLeftItem);
		choiceMenuList.add(moveRightItem);
		choiceMenuList.add(upperDiv);
		choiceMenuList.add(addChoiceItem);
		choiceMenuList.add(addMessageItem);
		choiceMenuList.add(addQuestionItem);		
		choiceMenuList.add(insertMessageBeforeItem);
		choiceMenuList.add(insertQuestionBeforeItem);
		choiceMenuList.add(insertMessageAfterItem);
		choiceMenuList.add(insertQuestionAfterItem);
		choiceMenuList.add(selectItem);
		choiceMenuList.add(lowerDiv);
		choiceMenuList.add(deleteNodeItem);
		choiceMenuList.add(deleteItem);
		//choiceMenuList.add(test);
		contextMenu.add(choiceMenuList);
		contextMenu.setAutoHideEnabled(true);
		contextMenu.getElement().getStyle().setProperty("border", "none");
		contextMenu.getElement().getStyle().setProperty("background", "none");
		
	}
		
	/**
	 * Initializes the TreeLink context menu.
	 */
	private void initLinkMenu() {
		AnchorListItem deleteLinkItem = new AnchorListItem("Delete Link");
		deleteLinkItem.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				linkMenu.hide();

				if(selectedLink == null) {
					WarningDialog.error("Cannot Delete Link", 
							"Deleting links is not yet supported. You may delete links manually " +
							"by opening the Conversation Tree File in a text editor and removing" +
							" child node id references.");
					return;
				}
				
				OkayCancelDialog.show("Delete Link", 
						"Are you sure you want to delete this link?", 
						"Delete", 
						new OkayCancelCallback() {

					@Override
					public void okay() {
						
						//find the source node
						int linkSourceId = selectedLink.getSource();
						TreeNode linkSource = null;
						
						for(Value nodeValue : tree.nodes(root).asIterable()){
							
							TreeNode node = nodeValue.as();
							
							if(node.id() == linkSourceId){
								linkSource = node;
							}
						}
						
						//remove the link
						Array<TreeLink> treeLinks = root.getObjAttr("links").cast();
						int index = treeLinks.indexOf(selectedLink);
						treeLinks.splice(index, 1);
						
						//append an end node to the source node if it no longer has any children
						if(linkSource != null && (linkSource.children() == null || linkSource.children().length() == 0)){
							linkSource.setAttr("children", createEndNode());
						}
						
						updateTree();
					}

					@Override
					public void cancel() {
						// nothing to do
					}

				});
			}
			
		});
		
		UnorderedList choiceMenuList = new UnorderedList();
		choiceMenuList.setStyleName(Styles.DROPDOWN_MENU);
		choiceMenuList.getElement().getStyle().setProperty("display", "block");
		choiceMenuList.add(deleteLinkItem);
		linkMenu.add(choiceMenuList);
		linkMenu.setAutoHideEnabled(true);
		linkMenu.getElement().getStyle().setProperty("border", "none");
		linkMenu.getElement().getStyle().setProperty("background", "none");
	}
	
	/**
	 * Creates the Edit and Add node dialog widgets.
	 */
	private void initCreateNodeDialogs() {

		//
		// Setup the Edit node dialog
		//
		editTextDialog  = new TextAreaDialog("Edit Text", "Enter the node text", "Ok");
		
		editTextDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
								
				TreeNodeEnum selectedNodeType = TreeNodeEnum.fromName(selectedNode.getType());
				
			    //Ensures that choice nodes meet their required length
			    if(selectedNodeType == TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 1) {
			        editTextDialog.setValidationMessage(INVALID_CHOICE_LENGTH_MSG);
                    editTextDialog.showValidationMessage(true);
                    return;
			    }
			    
			    //Ensures that non-choice nodes meet their required length
			    if(selectedNodeType != TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 3) {
					editTextDialog.setValidationMessage(INVALID_NODE_LENGTH_MSG);
				    editTextDialog.showValidationMessage(true);
					return;
				}
				
				editTextDialog.showValidationMessage(false);
				selectedNode.setText(event.getValue());
				editTextDialog.hide();
				
				updateTree(); //update the whole tree so the layout can be adjusted for any resized nodes
			}
			
		});
				
		//
		// Setup the Add node dialog
		// 
		addNodeDialog  = new TextAreaDialog("Add Node", "Enter the node text", "Ok");
				
		addNodeDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
			    //Ensures that choice nodes meet their required length
                if(newNodeType == TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 1) {
                    addNodeDialog.setValidationMessage(INVALID_CHOICE_LENGTH_MSG);
                    addNodeDialog.showValidationMessage(true);
                    return;
                }
                
                //Ensures that non-choice nodes meet their required length
                if(newNodeType != TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 3) {
                    addNodeDialog.setValidationMessage(INVALID_NODE_LENGTH_MSG);
                    addNodeDialog.showValidationMessage(true);
                    return;
                }
                
                TreeNode targetNode = selectedNode;
                
                if(TreeNodeEnum.ADD_NODE.equals(TreeNodeEnum.fromName(targetNode.getType()))){
                	
                	if(targetNode.parent() != null){
                		targetNode = targetNode.parent().cast();
                	}
                }
				
				if(newNodeType == TreeNodeEnum.QUESTION_NODE) {
					
					if(targetNode.children() == null) {
						
						//add the new node
						TreeNode newNode = createQuestionNode(false, event.getValue()).cast();												
						
						Array<TreeNode> children = Array.create();
						children.push(newNode);
						
						targetNode.setAttr("children", children);
						
						//attach a choice node as a child of the new node
						TreeNode choiceNodeA = createChoiceNode(false, "Choice A").cast();
						TreeNode choiceNodeB = createChoiceNode(false, "Choice B").cast();	
						
						Array<TreeNode> newChildren = Array.create();
						newChildren.push(choiceNodeA);
						newChildren.push(choiceNodeB);
						newChildren.push(createAddNode());
						
						newNode.setAttr("children", newChildren);
						
						//attach an end node as a child of the new node
						choiceNodeA.setAttr("children", createEndNode());
						choiceNodeB.setAttr("children", createEndNode());
						
					} else {
						
						//check to see if this node has an end node that needs to move
						TreeNode addNode = null;
						
						Iterator<Value> itr = targetNode.children().asIterable().iterator();
						while(itr.hasNext()){
							
							Value child = itr.next();
							
							TreeNode childNode = child.as();
							TreeNodeEnum childNodeType = TreeNodeEnum.fromName(childNode.getType());
							
							if(TreeNodeEnum.ADD_NODE.equals(childNodeType)){
								
								if(addNode == null){
									addNode = childNode;
								}
								
								itr.remove();
							}
						}
						
						//add the new node
						TreeNode newNode = createQuestionNode(false, event.getValue()).cast();
						targetNode.children().push(newNode);
						
						//attach a choice node as a child of the new node
						TreeNode choiceNodeA = createChoiceNode(false, "Choice A").cast();	
						TreeNode choiceNodeB = createChoiceNode(false, "Choice B").cast();	
						
						Array<TreeNode> newChildren = Array.create();
						newChildren.push(choiceNodeA);
						newChildren.push(choiceNodeB);
						newChildren.push(createAddNode());
						
						newNode.setAttr("children", newChildren);
						
						//attach an end node as a child of the new node
						choiceNodeA.setAttr("children", createEndNode());
						choiceNodeB.setAttr("children", createEndNode());
					}

				} else if(newNodeType == TreeNodeEnum.MESSAGE_NODE) {		
					
					if(targetNode.children() == null) {
						
						//add the new node
						TreeNode newNode = createMessageNode(false, event.getValue()).cast();												
						
						Array<TreeNode> children = Array.create();
						children.push(newNode);
						
						targetNode.setAttr("children", children);
						
						//attach an end node as a child of the new node
						newNode.setAttr("children", createEndNode());
						
					} else {
						
						//check to see if this node has an end node that needs to move
						TreeNode addNode = null;
						
						Iterator<Value> itr = targetNode.children().asIterable().iterator();
						while(itr.hasNext()){
							
							Value child = itr.next();
							
							TreeNode childNode = child.as();
							TreeNodeEnum childNodeType = TreeNodeEnum.fromName(childNode.getType());
							
							if(TreeNodeEnum.ADD_NODE.equals(childNodeType)){
								
								if(addNode == null){
									addNode = childNode;
								}
								
								itr.remove();
							}
						}
						
						//add the new node
						TreeNode newNode = createMessageNode(false, event.getValue()).cast();
						targetNode.children().push(newNode);
						
						if(addNode != null){
							
							//attach the end node as a child of the new node
							Array<TreeNode> children = Array.create();
							children.push(addNode);
							
							newNode.setAttr("children", children);
						}
					}

				} else if(newNodeType == TreeNodeEnum.CHOICE_NODE) {
					
					if(targetNode.children() == null) {
						
						//add the new node
						TreeNode newNode = createChoiceNode(false, event.getValue()).cast();												
						
						Array<TreeNode> children = Array.create();
						children.push(newNode);
						children.push(createAddNode());
						
						targetNode.setAttr("children", children);
						
						//attach an end node as a child of the new node
						newNode.setAttr("children", createEndNode());
						
					} else {
						
						//check to see if this node has an end node that needs to move
						Iterator<Value> itr = targetNode.children().asIterable().iterator();
						while(itr.hasNext()){
							
							Value child = itr.next();
							
							TreeNode childNode = child.as();
							TreeNodeEnum childNodeType = TreeNodeEnum.fromName(childNode.getType());
							
							if(TreeNodeEnum.ADD_NODE.equals(childNodeType)){								
								itr.remove();
							}
						}
						
						//add the new node
						TreeNode newNode = createChoiceNode(false, event.getValue()).cast();
						targetNode.children().push(newNode);
						targetNode.children().push(createAddNode());
							
						//attach an end node as a child of the new node
						newNode.setAttr("children", createEndNode());
					}
				}

				treeLength += 1;
				update(targetNode);
				addNodeDialog.hide();
				addNodeDialog.setValue("");
				addNodeDialog.showValidationMessage(false);
			}

		});
		
		insertNodeAfterDialog  = new TextAreaDialog("Insert Node", "Enter the node text", "Ok");
		
		insertNodeAfterDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
			    //Ensures that choice nodes meet their required length
                if(newNodeType == TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 1) {
                    insertNodeAfterDialog.setValidationMessage(INVALID_CHOICE_LENGTH_MSG);
                    insertNodeAfterDialog.showValidationMessage(true);
                    return;
                }
                
                //Ensures that non-choice nodes meet their required length
                if(newNodeType != TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 3) {
                    insertNodeAfterDialog.setValidationMessage(INVALID_NODE_LENGTH_MSG);
                    insertNodeAfterDialog.showValidationMessage(true);
                    return;
                }
                
                TreeNode targetNode = selectedNode;
                
                if(TreeNodeEnum.ADD_NODE.equals(TreeNodeEnum.fromName(targetNode.getType()))){
                	
                	if(targetNode.parent() != null){
                		targetNode = targetNode.parent().cast();
                	}
                }
				
				if(newNodeType == TreeNodeEnum.QUESTION_NODE) {

					Array<TreeNode> children = targetNode.children().slice(0).cast();
					targetNode.children().setLength(0);
					
					//add the new node
					TreeNode newNode = createQuestionNode(false, event.getValue()).cast();
					targetNode.children().push(newNode);
					
					//attach a choice node as a child of the new node
					TreeNode choiceNodeA = createChoiceNode(false, "Choice A").cast();	
					TreeNode choiceNodeB = createChoiceNode(false, "Choice B").cast();	
					
					Array<TreeNode> newChildren = Array.create();
					newChildren.push(choiceNodeA);
					newChildren.push(choiceNodeB);
					newChildren.push(createAddNode());
					
					newNode.setAttr("children", newChildren);
					
					//attach an end node as a child of the new node
					choiceNodeA.setAttr("children", children);
					choiceNodeB.setAttr("children", createEndNode());

				} else if(newNodeType == TreeNodeEnum.MESSAGE_NODE) {							
					
					Array<TreeNode> children = targetNode.children().slice(0).cast();
					targetNode.children().setLength(0);
					
					//add the new node
					TreeNode newNode = createMessageNode(false, event.getValue()).cast();
					targetNode.children().push(newNode);
						
					newNode.setAttr("children", children);				

				}

				treeLength += 1;
				update(targetNode);
				insertNodeAfterDialog.hide();
				insertNodeAfterDialog.setValue("");
				insertNodeAfterDialog.showValidationMessage(false);
			}

		});
		
		insertNodeBeforeDialog  = new TextAreaDialog("Insert Node", "Enter the node text", "Ok");
		
		insertNodeBeforeDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
			    //Ensures that choice nodes meet their required length
                if(newNodeType == TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 1) {
                    insertNodeAfterDialog.setValidationMessage(INVALID_CHOICE_LENGTH_MSG);
                    insertNodeAfterDialog.showValidationMessage(true);
                    return;
                }
                
                //Ensures that non-choice nodes meet their required length
                if(newNodeType != TreeNodeEnum.CHOICE_NODE && event.getValue() != null && trimText(event.getValue()).length() < 3) {
                    insertNodeAfterDialog.setValidationMessage(INVALID_NODE_LENGTH_MSG);
                    insertNodeAfterDialog.showValidationMessage(true);
                    return;
                }
                
                TreeNode targetNode = selectedNode;
                
//                if(TreeNodeEnum.ADD_NODE.equals(TreeNodeEnum.fromName(targetNode.getType()))){
//                	
//                	if(targetNode.parent() != null){
//                		targetNode = targetNode.parent().cast();
//                	}
//                }
				
				if(newNodeType == TreeNodeEnum.QUESTION_NODE) {
					
					TreeNode parent = targetNode.parent().cast();
					
					//add the new node
					TreeNode newNode = createQuestionNode(false, event.getValue()).cast();
					
					//attach a choice node as a child of the new node
					TreeNode choiceNodeA = createChoiceNode(false, "Choice A").cast();	
					TreeNode choiceNodeB = createChoiceNode(false, "Choice B").cast();	
					
					Array<TreeNode> newChildren = Array.create();
					newChildren.push(choiceNodeA);
					newChildren.push(choiceNodeB);
					newChildren.push(createAddNode());
					
					newNode.setAttr("children", newChildren);
					
					if(parent != null && parent.children() != null){
						
						//replace the parent's reference to the target node with the new node instead
						parent.children().set(parent.children().indexOf(targetNode), newNode);
					
					} else if(targetNode.equals(root)){
						
						//a new node is being inserted before the current root, so we need to update the root
						newNode.setAttr("links", root.getObjAttr("links"));
						root = newNode;
					}
					
					//assign the target node as a child of the first choice beneath the new question
					Array<TreeNode> children = Array.create();
					children.push(targetNode);
					
					choiceNodeA.setAttr("children", children);
					choiceNodeB.setAttr("children", createEndNode());

				} else if(newNodeType == TreeNodeEnum.MESSAGE_NODE) {
					
					TreeNode parent = targetNode.parent().cast();
					
					//add the new node
					TreeNode newNode = createMessageNode(false, event.getValue()).cast();	
					
					if(parent != null && parent.children() != null){
						
						//replace the parent's reference to the target node with the new node instead
						parent.children().set(parent.children().indexOf(targetNode), newNode);
					
					} else if(targetNode.equals(root)){
						
						//a new node is being inserted before the current root, so we need to update the root
						newNode.setAttr("links", root.getObjAttr("links"));
						root = newNode;
					}
					
					//assign the target node as a child of the new node
					Array<TreeNode> children = Array.create();
					children.push(targetNode);
					
					newNode.setAttr("children", children);

				}

				treeLength += 1;
				updateTree();
				insertNodeBeforeDialog.hide();
				insertNodeBeforeDialog.setValue("");
				insertNodeBeforeDialog.showValidationMessage(false);
			}

		});	
	}
	
	/**
	 * Displays the Add node dialog
	 */
	private void showAddNodeDialog() {
		// Update the text according to the new node type
		addNodeDialog.setText("Add " + newNodeType.getDisplayName());
		addNodeDialog.setInstructions("Enter the " + newNodeType.getName() + " text: ");
		addNodeDialog.center();
	}
	
	/**
	 * Displays the Edit node dialog
	 */
	private void showEditNodeDialog() {
		
		TreeNodeEnum nodeType = TreeNodeEnum.fromName(selectedNode.getType());
		
		editTextDialog.setValue(selectedNode.getText());
		editTextDialog.setText("Edit " + nodeType.getDisplayName());
		editTextDialog.setInstructions("Enter the " + nodeType.getName() + " text:");
		editTextDialog.showValidationMessage(false);
		editTextDialog.center();
	}
	
	/**
	 * Displays the dialog for inserting a node after an existing node
	 */
	private void showInsertNodeAfterDialog() {
		// Update the text according to the new node type
		insertNodeAfterDialog.setText("Insert " + newNodeType.getDisplayName());
		insertNodeAfterDialog.setInstructions("Enter the " + newNodeType.getName() + " text: ");
		insertNodeAfterDialog.center();
	}
	
	/**
	 * Displays the dialog for inserting a node before an existing node
	 */
	private void showInsertNodeBeforeDialog() {
		// Update the text according to the new node type
		insertNodeBeforeDialog.setText("Insert " + newNodeType.getDisplayName());
		insertNodeBeforeDialog.setInstructions("Enter the " + newNodeType.getName() + " text: ");
		insertNodeBeforeDialog.center();
	}
	
	/**
	 * Updates the tree graphics.
	 * 
	 * @param source The tree node to update
	 */
	private void update(final TreeNode source) {
		logger.fine("Beginning update()");
		
		if(source.getObjAttr("x0") == null){
			
			//if the source node does not have a starting X position, start it at 0
			source.setAttr("x0", 0);
		}
		
		if(source.getObjAttr("y0") == null){
			
			//if the source node does not have a starting Y position, start it at 0
			source.setAttr("y0", 0);
		}
		
		logger.fine("0: Resizing tree nodes.");		
		//Nick: This needs to be called before the nodes are loaded into the SVG element, otherwise
		//the separation function used to keep nodes from overlapping won't work properly.
		resizeNodes();
		
		Array<Node> nodes = tree.nodes(root).reverse();
		Array<Link> links = tree.links(nodes);
		Array<TreeLink> treeLinks = root.getObjAttr("links").cast();		
		final HashMap<Integer, Node> nodeLinks = new HashMap<Integer, Node>();
		
		if(treeLength <= nodes.length()) {
			treeLength = nodes.length() + 1;
		}
		
		// Assign the node ids
		logger.fine("1: Mapping tree node ids.");
		UpdateSelection node = svg.selectAll("g." + CSS.node()).data(nodes,
				new KeyFunction<Integer>() {
			@Override
			public Integer map(Element context, Array<?> newDataArray,
					Value datum, int index) {
				
				TreeNode d = datum.<TreeNode> as();
				if(treeLength <= d.id()) {
					treeLength = d.id() + 1;
				}
				if(d.id() == -1) {
					d.id(treeLength);
					treeLength += 1;
				}	
				// Add all tree nodes
				nodeLinks.put(d.id(), d);
				return d.id();
			}
		});
		
		// Remove any obsolete links
		logger.fine("2: Checking for obsolete links");
		for(int i = treeLinks.length() - 1; i >= 0; i--) {
			TreeLink link = treeLinks.get(i);
			if(link.getSource() == -1) {
				continue;
			}
			if(!nodeLinks.containsKey(link.getSource()) || !nodeLinks.containsKey(link.getTarget())) {
				treeLinks.splice(i, 1);
			}
		}
		
		// Reset the links array
		nodeLinks.clear();
		
		// Normalize for fixed depth
		logger.fine("3: Setting tree node depth & width properties.");
		//Nick: This needs to be called after the nodes are loaded into the SVG element, otherwise
		//the loading logic will override the positions assigned by this method call.
		layoutNodes(nodeLinks);
		
		// Enter any new nodes at the parent's previous position
		logger.fine("4: Translating new node positions.");
		Selection nodeEnter = node
				.enter()
				.append("g")
				.attr("class", CSS.node())
				.attr("transform",
						"translate(" + source.getNumAttr("x0") + ","
								+ source.getNumAttr("y0") + ")")
				.on("click", new TreeNodeClicked());
		

		// Create a rectangle wrapper for new nodes
		logger.fine("5: Creating rectangle wrappers for new nodes.");
		nodeEnter.append("rect")
			.attr("class", new DatumFunction<String>(){

				@Override
				public String apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					if(node.equals(selectedNode)){
						return CSS.selectedNodeRect();
						
					} else if(linkNodes){
						
						//highlight all of the available nodes that can be linked from the selected node
						TreeNodeEnum nodeType = TreeNodeEnum.fromName(node.getType());
						TreeNodeEnum selectedNodeType = TreeNodeEnum.fromName(selectedNode.getType());
						
						if(TreeNodeEnum.QUESTION_NODE.equals(selectedNodeType)){
							
							if(TreeNodeEnum.CHOICE_NODE.equals(nodeType)){
								
								//if the user is trying to link from a question node, highlight any choice nodes
								return CSS.selectableNodeRect();
							}
							
						} else if(TreeNodeEnum.MESSAGE_NODE.equals(selectedNodeType)
								|| TreeNodeEnum.CHOICE_NODE.equals(selectedNodeType)){
							
							if(TreeNodeEnum.MESSAGE_NODE.equals(nodeType) 
									|| TreeNodeEnum.QUESTION_NODE.equals(nodeType)
									|| TreeNodeEnum.END_NODE.equals(nodeType)){
								
								//if the user is trying to link from a message or choice node, 
								//highlight choice, question, or message nodes
								return CSS.selectableNodeRect();
							}
						}
					}
					
					return null;
				}
				
			})
			.attr("width", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return width.intValue() + 20;
				}
			})
			.attr("stroke", new DatumFunction<String>() {

				@Override
				public String apply(Element context, Value d, int index) {
					 
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.QUESTION_NODE) {
						return "black";
						
					} else if(nodeType == TreeNodeEnum.END_NODE) {
						return "white";
						
					} else {						
						return "gray";
					}
				}
				
			})
			.attr("stroke-width", new DatumFunction<Double>() {
				@Override
				public Double apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.END_NODE) {
						return 3.0;
					}
					
					return 1.5;
				}
			})
			.attr("stroke-dasharray", new DatumFunction<String>(){

				@Override
				public String apply(Element context, Value d, int index) {
					
					TreeNodeEnum type = TreeNodeEnum.fromName(d.<TreeNode>as().getType());
					
					if(TreeNodeEnum.ADD_NODE.equals(type)){
						return "5 5";
					}
					
					return "none";
				}
				
			})
			.attr("height", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double height = d.<TreeNode> as().getNumAttr("height");
					return height.intValue() + 15;
				}
			})
			.attr("rx", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.CHOICE_NODE) {
						return 0;
					}
					
					return 15;
				}
			})
			.attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return (width.intValue() / -2) - 5;
				}
			})
			.attr("y", -15)
			.style("fill", new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.CHOICE_NODE) {
						return "lightyellow";
						
					} else if(nodeType == TreeNodeEnum.END_NODE) {
						return "rgb(210,0,0)";
					
					} else if(nodeType == TreeNodeEnum.ADD_NODE) {
						return "rgba(0,0,0,0.01)";
						
					} else {
						return "white";
					}
				}
			})
			.append("title").text(new DatumFunction<String>() {

				@Override
				public String apply(Element context, Value d, int index) {
					if(d.<TreeNode> as().hasAssessments()) {
						return "This node has assessments available";
					}
					
					return "";
				}
				
			});
		

		// Create the new node's text
		logger.fine("6: Creating text graphics for new nodes.");
		Selection text = nodeEnter.append("svg:text")
			.attr("class", CSS.text())
			.attr("width", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					if(node.hasAssessments()){
						Double width = node.getNumAttr("width");
						return width.intValue() - NODE_ICON_WIDTH + ASSESSMENT_ICON_WIDTH;
						
					} else {
						Double width = node.getNumAttr("width");
						return width.intValue() - NODE_ICON_WIDTH;
					}
				}
			})
			.attr("y", 5)
			.attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					if(node.hasAssessments()){
						Double width = node.getNumAttr("width");
						return (int) (-(width)/2 + NODE_ICON_WIDTH + ASSESSMENT_ICON_WIDTH) - 7;
					
					} else {
						Double width = node.getNumAttr("width");
						return (int) (-(width)/2 + NODE_ICON_WIDTH) - 7;
					}
				}
			})
			.style("fill", new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.END_NODE) {
						return "white";
						
					} else if(nodeType == TreeNodeEnum.ADD_NODE) {
						return "gray";
						
					} else {
						return "black";
					}
				}
			})
	        .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					String text = d.<TreeNode> as().getText();
					return getFormattedString(text);
				}
	        });
		
		formatText(text, MAX_NODE_ROWS);
		
		// Create the new node's icon
		logger.fine("7: Creating icons for new nodes.");
		nodeEnter.append("svg:text")
			.attr("class", CSS.icon())
		    .attr("text-anchor", "middle")
		    .attr("y", 5)
		    .attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return 20 - (width.intValue() / 2);
				}
			})
		    .style("fill", new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.ADD_NODE) {
						return "gray";
						
					} else {
						return "black";
					}
				}
			})
		    .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						String type = d.<TreeNode> as().getType();
						TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
						
						if(nodeType == TreeNodeEnum.QUESTION_NODE) {
							return QUESTION_ICON;
						} else if(nodeType == TreeNodeEnum.CHOICE_NODE) {
							return CHOICE_ICON;
						} else if(nodeType == TreeNodeEnum.END_NODE) {
							return END_ICON;
							
						} else if(nodeType == TreeNodeEnum.ADD_NODE) {
							return ADD_ICON;
							
						} else {
							return MESSAGE_ICON;
						}
					}
		        });
		
		// Create the new node's icon
		logger.fine("Creating assessment icons for new nodes.");
		nodeEnter.append("svg:image")
			.attr("class", CSS.assessmentIcon())
		    .attr("text-anchor", "middle")
		    .attr("y", -17)
		    .attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return 30 - (width.intValue() / 2);
				}
			})
		    .attr("width", 21)
			.attr("height", 21)
		    .attr("xlink:href" , new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						
					if(d.<TreeNode> as().hasAssessments()){
						return ASSESSMENT_ICON;
						
					} else {
						return "";
					}
				}
	        });
		
		// Transition nodes to their new position
		logger.fine("8: Translating existing node positions.");
		Transition nodeUpdate = node.transition().duration(DURATION)
				.attr("transform", new DatumFunction<String>() {
					@Override
					public String apply(Element context, Value d, int index) {
						TreeNode data = d.<TreeNode> as();
						return "translate(" + data.x() + "," + data.y() + ")";
					}
				});

		// Transition exiting nodes to the parent's new position
		logger.fine("9: Translating exiting node positions.");
		node.exit().remove();
		
		// Update the rectangle wrappers for existing nodes
		logger.fine("10: Updating existing rectangle graphics.");
		nodeUpdate.select("rect")
			.attr("class", new DatumFunction<String>(){
	
				@Override
				public String apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					if(node.equals(selectedNode)){
						return CSS.selectedNodeRect();
						
					} else if(linkNodes){
						
						//highlight all of the available nodes that can be linked from the selected node
						TreeNodeEnum nodeType = TreeNodeEnum.fromName(node.getType());
						TreeNodeEnum selectedNodeType = TreeNodeEnum.fromName(selectedNode.getType());
						
						if(TreeNodeEnum.QUESTION_NODE.equals(selectedNodeType)){
							
							if(TreeNodeEnum.CHOICE_NODE.equals(nodeType)){
								
								//if the user is trying to link from a question node, highlight any choice nodes that aren't
								//the question's immediate children
								return CSS.selectableNodeRect();
							}
							
						} else if(TreeNodeEnum.MESSAGE_NODE.equals(selectedNodeType)
								|| TreeNodeEnum.CHOICE_NODE.equals(selectedNodeType)){
							
							if(TreeNodeEnum.MESSAGE_NODE.equals(nodeType) 
									|| TreeNodeEnum.QUESTION_NODE.equals(nodeType)
									|| TreeNodeEnum.END_NODE.equals(nodeType)){
								
								//if the user is trying to link from a message or choice node, 
								//highlight choice, question, or message nodes
								return CSS.selectableNodeRect();
							}
						}
					}
					
					return null;
				}
				
			})
			.attr("width", new DatumFunction<Integer>() {
				
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return width.intValue() + 10;
				}
			})
			.attr("stroke", new DatumFunction<String>() {

				@Override
				public String apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.QUESTION_NODE) {
						return "black";
						
					} else if(nodeType == TreeNodeEnum.END_NODE) {
						return "white";
						
					} else {						
						return "gray";
					}
				}
				
			})
			.attr("stroke-width", new DatumFunction<Double>() {
				@Override
				public Double apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.END_NODE) {
						return 3.0;
					}
					
					return 1.5;
				}
			})
			.attr("stroke-dasharray", new DatumFunction<String>(){

				@Override
				public String apply(Element context, Value d, int index) {
					
					TreeNodeEnum type = TreeNodeEnum.fromName(d.<TreeNode>as().getType());
					
					if(TreeNodeEnum.ADD_NODE.equals(type)){
						return "5 5";
					}
					
					return "none";
				}
				
			})
			.attr("height", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double height = d.<TreeNode> as().getNumAttr("height");
					return height.intValue() + 15;
				}
			})
			.attr("rx", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.CHOICE_NODE) {
						return 0;
					}
					
					return 15;
				}
			})
			.attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return (width.intValue() / -2) - 5;
				}
			})
			.attr("y", -25)
			.style("fill", new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.CHOICE_NODE) {
						return "lightyellow";
						
					} else if(nodeType == TreeNodeEnum.END_NODE) {
						return "rgb(210,0,0)";
					
					} else if(nodeType == TreeNodeEnum.ADD_NODE) {
						return "rgba(0,0,0,0.01)";
						
					} else {
						return "white";
					}
				}
			});
		
		nodeUpdate.select("rect.title").text(new DatumFunction<String>() {

			@Override
			public String apply(Element context, Value d, int index) {
				if(d.<TreeNode> as().hasAssessments()) {
					return "This node has assessments available";
				}
				
				return "";
			}
			
		});
		
		// Update the node text
		logger.fine("11: Updating existing node text.");
		Selection textUpdate = node.select("text." + CSS.text())	        
				.attr("width", new DatumFunction<Integer>() {
					@Override
					public Integer apply(Element context, Value d, int index) {
						
						TreeNode node = d.<TreeNode> as();
						
						if(node.hasAssessments()){
							Double width = node.getNumAttr("width");
							return width.intValue() - NODE_ICON_WIDTH + ASSESSMENT_ICON_WIDTH;
							
						} else {
							Double width = node.getNumAttr("width");
							return width.intValue() - NODE_ICON_WIDTH;
						}
					}
				})
				.attr("x", new DatumFunction<Integer>() {
					@Override
					public Integer apply(Element context, Value d, int index) {
						
						TreeNode node = d.<TreeNode> as();
						
						if(node.hasAssessments()){
							Double width = node.getNumAttr("width");
							return (int) (-(width)/2 + NODE_ICON_WIDTH + ASSESSMENT_ICON_WIDTH) - 7;
						
						} else {
							Double width = node.getNumAttr("width");
							return (int) (-(width)/2 + NODE_ICON_WIDTH) - 7;
						}
					}
				})
		        .style("fill", new DatumFunction<String>() {
					@Override
					public String apply(Element context, Value d, int index) {
						
						String type = d.<TreeNode> as().getType();
						TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
						
						if(nodeType == TreeNodeEnum.END_NODE) {
							return "white";
							
						} else if(nodeType == TreeNodeEnum.ADD_NODE) {
							return "gray";
							
						} else {
							return "black";
						}
					}
				})
				.text(new DatumFunction<String>() {
					@Override
					public String apply(Element context, Value d, int index) {
						String text = d.<TreeNode> as().getText();
						return getFormattedString(text);
					}
		        });
		
		formatText(textUpdate, MAX_NODE_ROWS);
		
		// Update existing node icon positions
		logger.fine("12: Updating existing node icon positions.");
		nodeUpdate.select("text." + CSS.icon())
		    .attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return 20 - (width.intValue() / 2);
				}
			})
		    .style("fill", new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					String type = d.<TreeNode> as().getType();
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
					
					if(nodeType == TreeNodeEnum.ADD_NODE) {
						return "gray";
						
					} else {
						return "black";
					}
				}
			})
		    .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						String type = d.<TreeNode> as().getType();
						TreeNodeEnum nodeType = TreeNodeEnum.fromName(type);
						
						if(nodeType == TreeNodeEnum.QUESTION_NODE) {
							return QUESTION_ICON;
						} else if(nodeType == TreeNodeEnum.CHOICE_NODE) {
							return CHOICE_ICON;
						} else if(nodeType == TreeNodeEnum.END_NODE) {
							return END_ICON;
						} else if(nodeType == TreeNodeEnum.ADD_NODE) {
							return ADD_ICON;
						} else {
							return MESSAGE_ICON;
						}
					}
		        });
		
		// Update existing node assessment icon positions
		logger.fine("Updating existing node assessment icon positions.");
		nodeUpdate.select("image." + CSS.assessmentIcon())
		    .attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getNumAttr("width");
					return 30 - (width.intValue() / 2);
				}
			})
		    .attr("xlink:href" , new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						
					if(d.<TreeNode> as().hasAssessments()){
						return ASSESSMENT_ICON;
						
					} else {
						return "";
					}
				}
	        });
		
		// Remove any previously drawn links
		//(Nick: This is needed to avoid a bug in IE where updating paths that have markers causes the paths to disappear)
		svg.selectAll("path." + CSS.link()).remove();

		// Update the links
		logger.fine("13: Mapping links.");
		UpdateSelection link = svg.selectAll("path." + CSS.link()).data(links,
			new KeyFunction<Integer>() {
				@Override
				public Integer map(Element context, Array<?> newDataArray, Value datum, int index) {
					return datum.<Link> as().target().<TreeNode> cast().id();
				}
			});
		
		// Enter any new links at the parent's previous position
		logger.fine("14: Setting link positions.");
		link.enter().insert("svg:path", "g")
			.attr("class", CSS.link())
			.attr("marker-end", "url(#end)")
			.attr("d", new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					Link link = d.<Link> as();
					TreeNode sourceNode = link.source().cast();
					TreeNode targetNode = link.target().cast();

					return generatePathDescription(sourceNode, targetNode, false);
				}
			});
		
		// Transition exiting links to the parent's new position
		link.exit().remove();
		
		// Update the node positions and calculate the dimensions of the tree		
		double leftmostX = 0;
		double rightmostX = 0;
		double topmostY = 0;
		double bottommostY = 0;
		
		logger.fine("16: Updating node positions.");
		for(int i = 0; i < nodes.length(); i++){
			
			TreeNode data = nodes.get(i).cast();
			data.setAttr("x0", data.x());
			data.setAttr("y0", data.y());
			
			// Nick: The calculations here assume that the root node is always positioned at (0,0).
			// If the root node is moved in the future, these calculations will need to be adjusted
			// accordingly.
			if(data.x() - data.getNumAttr("width")/2 < leftmostX){
				leftmostX = data.x() - data.getNumAttr("width")/2;
			}
			
			if(data.x() + data.getNumAttr("width")/2 > rightmostX){
				rightmostX = data.x() + data.getNumAttr("width")/2;
			}
			
			if(data.y() - NODE_ROW_HEIGHT/2 < topmostY){
				topmostY = data.y() - NODE_ROW_HEIGHT/2;
			}
			
			if(data.y() + data.getNumAttr("height") > bottommostY){
				bottommostY = data.y() + data.getNumAttr("height");
			}
		}
		
		// Apply context menu handler to any new nodes
		logger.fine("17: Applying context menu handler to new nodes.");
		svg.selectAll("g." + CSS.node() + ":not([contextmenu=nodeOptions])").on("contextmenu", new DatumFunction<Void>() {

			@Override
			public Void apply(Element context, Value d, int index) {
				// Set the selected node to the node that was right-clicked
				selectedNode = d.<TreeNode> as();
				selectedNode.setStrAttr("contextmenu", "nodeOptons");
				showContextMenu();
				
				updateTree();
				
				D3.event().preventDefault();
				
				return null;
			}

		});
		
		// Remove any previously drawn multiLinks
		svg.selectAll("path." + CSS.multiLink()).remove();
		
		// Draw visible links
		logger.fine("18: Drawing additional links.");
		UpdateSelection multilink = svg.selectAll("path." + CSS.multiLink()).data(treeLinks,
			new KeyFunction<String>() {
				@Override
				public String map(Element context, Array<?> newDataArray, Value datum, int index) {
				    // the return value must be unique for each source pointing to that same target
				    // see #3483 for more information
					return datum.<TreeLink> as().getSource() + "_to_" + datum.<TreeLink> as().getTarget();
				}
			});
			
			multilink.enter().insert("svg:path", "g")
				.attr("class", CSS.multiLink())
				.attr("marker-end", "url(#end)")
				.attr("d", new DatumFunction<String>() {
					@Override
					public String apply(Element context, Value d, int index) {
										
						TreeLink link = d.<TreeLink> as();
						TreeNode sourceNode = nodeLinks.get(link.getSource()).cast();
						TreeNode targetNode = nodeLinks.get(link.getTarget()).cast();

						return generatePathDescription(sourceNode, targetNode, true);
					}
				});
		
		// Apply context menu handler to any new links
		logger.fine("19: Applying context menu handler to links.");
		svg.selectAll("path." + CSS.link() + ":not([contextmenu=linkOptions])")
			.on("contextmenu", new DatumFunction<Void>() {
	
				@Override
				public Void apply(Element context, Value d, int index) {
					// Set the selected link to null since link actions are not available yet.
					d.<TreeLink> as().setAttr("contextmenu", "linkOptons");
					selectedLink = null;
					showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), linkMenu);
					D3.event().preventDefault();
					return null;
				}
	
			})
			.on("click", new DatumFunction<Void>() {
	
				@Override
				public Void apply(Element context, Value d, int index) {
					// Set the selected link to null since link actions are not available yet.
					d.<TreeLink> as().setAttr("contextmenu", "linkOptons");
					selectedLink = null;
					showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), linkMenu);
					D3.event().preventDefault();
					return null;
				}
	
			});
		
		// Apply context menu handler to any new mutliLinks
		logger.fine("20: Applying context menu handler to additional links.");
		svg.selectAll("path." + CSS.multiLink() + ":not([contextmenu=linkOptions])")
			.on("contextmenu", new DatumFunction<Void>() {

				@Override
				public Void apply(Element context, Value d, int index) {				
					
					selectedLink = d.<TreeLink> as();
					selectedLink.setAttr("contextmenu", "linkOptons");				
					showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), linkMenu);
					D3.event().preventDefault();
					return null;
				}
	
			})
			.on("click", new DatumFunction<Void>() {

				@Override
				public Void apply(Element context, Value d, int index) {				
					
					selectedLink = d.<TreeLink> as();
					selectedLink.setAttr("contextmenu", "linkOptons");				
					showWithinViewport(D3.event().getClientX(), D3.event().getClientY(), linkMenu);
					D3.event().preventDefault();
					return null;
				}
	
			});
		
		//after the update has finished, calculate the height and width of the tree and set the SVG container to
		//that height and width (plus padding) so that it can be scrolled properly 
		logger.fine("Finalizing tree dimensions.");	
		
		/* Figure out which of the leftmost and rightmost x coordinates is larger and double that to 
		 * make sure the tree is wide enough. Simply subtracting the left x from the right x is not sufficient
		 * because the tree could be heavily weighted to one side.*/
		double treeWidth = Math.max(Math.abs(rightmostX), Math.abs(leftmostX)) * 2; 
		double treeHeight = bottommostY - topmostY; 
		
		int scaledTreeWidth = ((int) (treeWidth * zoom.scale() + 100 * zoom.scale()));
		int scaledTreeHeight = ((int) (treeHeight * zoom.scale() + 100 * zoom.scale()));
			
		mainSvg.attr("height", scaledTreeHeight + "px");
		mainSvg.attr("width",  scaledTreeWidth + "px");
		
		double xTranslation = scaledTreeWidth < getOffsetWidth() 
				? getOffsetWidth()/2  //center the tree horizontally if it is smaller than the viewport width
				: Math.abs(leftmostX * zoom.scale()) + 50 * zoom.scale();
				
		double yTranslation = scaledTreeHeight < getOffsetHeight() 
				? getOffsetHeight()/2 - (treeHeight * zoom.scale())/2 //center the tree vertically if it is smaller than the viewport height
				: 50 * zoom.scale();
				
		svg.attr("transform", 
				"translate(" + xTranslation + ", " + yTranslation + ") " + 
				"scale(" + zoom.scale() + ")"
		);
		
	}
	
	/**
	 * Gets only the text from a HTML string without any mark-up
	 * 
	 * @param text the HTML string
	 * @return the text from the html
	 */
	private String getFormattedString(String html) {
		String formattedText = "";
		if (html != null && html != "") {
			dom.setHTML(html);
			formattedText = trimText(dom.getText());
		}
		
		return formattedText;
	}
	
	/**
	 * Generates a value for the "d" (description) attribute belonging to a path linking two nodes.
	 * 
	 * @param sourceNode the node that the path begins at
	 * @param targetNode the node that the path ends at
	 * @param isMultiLink whether or not the path represents a manual multilink
	 * @return a description of the path linking the source and target nodes
	 */
	private String generatePathDescription(TreeNode sourceNode, TreeNode targetNode, boolean isMultiLink) {
		
		if(sourceNode != null && targetNode != null) {
			
			double sourceXOffset = 0;
			double targetXOffset = 0;
			double sourceYOffset = 0;
			double targetYOffset = 0;
			
			//the array of points that will make up the line between the two nodes
			Array<Coords> points = Array.create();
			
			if(targetNode.y() <= sourceNode.y() && sourceNode.y() <= targetNode.y() + targetNode.getNumAttr("height")){
				
				if(targetNode.x() > sourceNode.x()){
				
					sourceXOffset = sourceNode.getNumAttr("width")/2 + 5;
					targetXOffset = -targetNode.getNumAttr("width")/2 - 5;
					
					if(isMultiLink){
						
						//offset multilinks slightly so they don't overlap too much with regular links
						sourceYOffset = sourceYOffset + 5;
						targetYOffset = targetYOffset + 5;
					}
					
				} else {
					
					sourceXOffset = -sourceNode.getNumAttr("width")/2 - 5;
					targetXOffset = targetNode.getNumAttr("width")/2 + 5;
					
					if(isMultiLink){
						
						//offset multilinks slightly so they don't overlap too much with regular links
						sourceYOffset = sourceYOffset - 5;
						targetYOffset = targetYOffset - 5;
					}
				}	
				
				Coords source = Coords.create(
						sourceNode.x() + sourceXOffset, 
						sourceNode.y() + sourceYOffset
				);
				
				Coords target = Coords.create(
						targetNode.x() + targetXOffset, 
						targetNode.y() + targetYOffset
				);	
				
				points.push(source);
				points.push(target);
				
			} else {
				
				if(targetNode.y() > sourceNode.y()){
									
					sourceYOffset = sourceNode.getNumAttr("height") - NODE_ROW_HEIGHT/2 + 2;
					targetYOffset = -(NODE_ROW_HEIGHT);
					
					if(isMultiLink){
						
						//offset multilinks slightly so they don't overlap too much with regular links
						sourceXOffset = sourceXOffset + 10;
						targetXOffset = targetXOffset + 10;
					}
					
				} else {
					
					sourceYOffset = -(NODE_ROW_HEIGHT);
					targetYOffset = targetNode.getNumAttr("height") - NODE_ROW_HEIGHT/2 + 2;
					
					if(isMultiLink){
						
						//offset multilinks slightly so they don't overlap too much with regular links
						sourceXOffset = sourceXOffset - 10;
						targetXOffset = targetXOffset - 10;
					}
				}				
				
				Coords source = Coords.create(
						sourceNode.x() + sourceXOffset, 
						sourceNode.y() + sourceYOffset
				);
				
				Coords target = Coords.create(
						targetNode.x() + targetXOffset, 
						targetNode.y() + targetYOffset
				);	
				
				points.push(source);
				points.push(Coords.create(
						source.x(), 
						source.y() + (target.y() - source.y()) * 0.15
				));
				points.push(Coords.create(
						source.x(), 
						source.y() + (target.y() - source.y())/2
				));
				points.push(Coords.create(
						target.x(), 
						source.y() + (target.y() - source.y())/2
				));
				points.push(Coords.create(
						target.x(), 
						source.y() + (target.y() - source.y()) * 0.85
				));
				points.push(target);
			}	

			return diagonal.generate(points);
			
		} else {			
			
//			logger.severe("Unfinished link: [" + link.getSource() + ", "+ link.getTarget() +"]");
			
			if(sourceNode == null){
				logger.severe("Source is null");
			}
			
			if(targetNode == null){
				logger.severe("Target is null");
			}
			
			return "";
		}
	}

	/**
	 * Sets the positions of all of the given node's children
	 * 
	 * @param node the node whose children should be laid out
	 * @param nodeLinks a mapping of node IDs to be populated by this method
	 */
	private void layoutChildren(TreeNode node, HashMap<Integer, Node> nodeLinks){
		
		if(node != null && node.children() != null){
			
			double parentY = node.getNumAttr("y");
			double parentHeight = node.getNumAttr("height");
			
			for(int i = 0; i < node.children().length(); i++){
				
				TreeNode treeNode = node.children().get(i).cast();
				treeNode.setAttr("y", parentY + parentHeight + HEIGHT_BETWEEN_NODES);
				
				nodeLinks.put(treeNode.id(), treeNode);
				
				layoutChildren(treeNode, nodeLinks);
			}
		}
	}
	
	/**
	 * Sets the positions of all the nodes in the tree
	 * 
	 * @param nodeLinks a mapping of node IDs to be populated by this method
	 */
	private void layoutNodes(HashMap<Integer, Node> nodeLinks){
		
		TreeNode treeNode = root;
		treeNode.setAttr("y", 0);
		
		nodeLinks.put(treeNode.id(), treeNode);
		
		layoutChildren(treeNode, nodeLinks);
	}
	
	/**
	 * Sets the sizes of all of the given node's children
	 * 
	 * @param node the node whose children should be resized
	 */
	private void resizeChildren(TreeNode node) {
		
		if(node != null && node.children() != null){
			
			for(int i = 0; i < node.children().length(); i++){
				
				TreeNode treeNode = node.children().get(i).cast();
				
				double minWidth = treeNode.hasAssessments() ? NODE_ICON_WIDTH + ASSESSMENT_ICON_WIDTH : NODE_ICON_WIDTH;
				
				double width = minWidth;
				double height = NODE_ROW_HEIGHT;
				
				if(treeNode.getText() != null) {
					String text = getFormattedString(treeNode.getText());
					
					double textLength = (text.length() * 10);
					
					if(textLength > MAX_NODE_WIDTH - minWidth){
									
						//calculate how many rows will be needed to fit all of the node's text
						int numRows = (int) Math.ceil(textLength/(MAX_NODE_WIDTH - minWidth));
								
						width =  MAX_NODE_WIDTH;				
						height = NODE_ROW_HEIGHT * Math.min(
								MAX_NODE_ROWS, 
								numRows
						);
						
					} else {				
						width = minWidth + textLength;
					}
				}
				
				treeNode.setAttr("width", width);
				treeNode.setAttr("height", height);
				
				resizeChildren(treeNode);
			}
		}
	}
	
	/**
	 * Sets the sizes of all the nodes in the tree
	 */
	private void resizeNodes(){
		
		TreeNode treeNode = root;
		
		double minWidth = treeNode.hasAssessments() ? NODE_ICON_WIDTH + ASSESSMENT_ICON_WIDTH : NODE_ICON_WIDTH;
		
		double width = minWidth;
		double height = NODE_ROW_HEIGHT;
		
		if(treeNode.getText() != null) {
			
			String text = getFormattedString(treeNode.getText());
			
			double textLength = (text.length() * 10);
			
			if(textLength > MAX_NODE_WIDTH - minWidth){
							
				//calculate how many rows will be needed to fit all of the node's text
				int numRows = (int) Math.ceil(textLength/(MAX_NODE_WIDTH - minWidth));
						
				width =  MAX_NODE_WIDTH;				
				height = NODE_ROW_HEIGHT * Math.min(
						MAX_NODE_ROWS, 
						numRows
				);
				
			} else {				
				width = minWidth + textLength;
			}
		}
		
		treeNode.setAttr("width", width);
		treeNode.setAttr("height", height);
		
		resizeChildren(treeNode);
	}

	/**
	 * Creates a conversation tree.
	 * 
	 * @return the TreeNode at the root of the conversation tree.
	 */
	private native JavaScriptObject makeTree() /*-{
		var data = [{
			"name": "I see that you are creating a conversation tree. Click this node to get started.",
			"type": "message",
			"id": "1",
           	"links": [],
           	"children" : [{
				"type" : "add",
				"children" : [{
	           		"name" : "End",
					"type" : "end"		
	           	}]
           	}]
        }];
           
		return data[0];
		
	}-*/;
	
	/**
	 * Trim the leading and trailing whitespace from text.
	 * 
	 * @param text A string.
	 * @return the trimmed text.
	 */
	private native String trimText(String text) /*-{	
		return text.trim();
	}-*/;
	
	/**
	 * Creates a conversation tree.
	 * 
	 * @param treeJSONStr A JSON string of the TreeNode to create.
	 * @return the TreeNode at the root of the conversation tree.
	 */
	private native JavaScriptObject makeTree(String treeJSONStr) /*-{	
		return JSON.parse(treeJSONStr);		
	}-*/;
	
	/**
	 * Creates a question TreeNode
	 * 
	 * @param newArray Whether or not a new array should be returned. True to add children to 
	 * a parent which as no children. False to add a node to an array of children.
	 * @param text The text of the question.
	 * @return A new TreeNode array if newArray was true. Otherwise, returns a single TreeNode.
	 */
	private native JavaScriptObject createQuestionNode(boolean newArray, String text) /*-{
		var data = [
			{	
				"name": text,
				"type": "question"
			}
		];
		
		return newArray ? data : data[0];
		
	}-*/;
	
	/**
	 * Creates a message TreeNode
	 * 
	 * @param newArray Whether or not a new array should be returned. True to add children to 
	 * a parent which as no children. False to add a node to an array of children.
	 * @param text The text of the question.
	 * @return A new TreeNode array if newArray was true. Otherwise, returns a single TreeNode.
	 */
	private native JavaScriptObject createMessageNode(boolean newArray, String text) /*-{
		var data = [
			{	
				"name": text,
				"type": "message"
			}
		];
		
		return newArray ? data : data[0];
		
	}-*/;
	
	/**
	 * Creates a choice TreeNode
	 * 
	 * @param newArray Whether or not a new array should be returned. True to add children to 
	 * a parent which as no children. False to add a node to an array of children.
	 * @param text The text of the question.
	 * @return A new TreeNode array if newArray was true. Otherwise, returns a single TreeNode.
	 */
	private native JavaScriptObject createChoiceNode(boolean newArray, String text)  /*-{
		var data = [
			{	
				"name": text,
				"type": "choice"
			}
		];
		
		return newArray ? data : data[0];
		
	}-*/;
	
	/**
	 * Creates an ending TreeNode
	 */
	private native JavaScriptObject createEndNode()  /*-{
		var data = [
			{	
				"type" : "add",
				"children" : [{
					"name" : "End",
				"type" : "end",
				}]
			}
		];
		
		return data;
		
	}-*/;
	
	/**
	 * Creates an adding TreeNode
	 */
	private native JavaScriptObject createAddNode()  /*-{
		var data = {	
			"type" : "add"
		};
		
		return data;
		
	}-*/;
		
	/**
	 * Creates a link between two nodes.
	 * 
	 * @param root The root of the tree
	 * @param sourceId The id of the source node.
	 * @param targetId The id of the target node.
	 * @return The new link object.
	 */
	private native void createLink(TreeNode root, int sourceId, int targetId) /*-{
		var link = {
			source : sourceId,
			target : targetId
		}
		
		root.links.push(link);
		
	}-*/;
	
	/**
	 * Removes a link between two nodes.
	 * 
	 * @param root The root of the tree.
	 * @param index The index of the link to delete.
	 */
	private native void removeLink(TreeNode root, int index)/*-{
		if(root.links != null && root.links.length < index) {
			root.links.splice(index, 1);
		}
	}-*/;
	
	/**
	 * Calls JSON.stringify on an object. Attributes such as children, attributes, and links 
	 * will be converted. However, other circular structures will be replaced with the text "[circular]". 
	 * 
	 * @param object The object to stringify
	 * @return A JSON string representation of the object.
	 */
	private native String stringify(JavaScriptObject object) /*-{
		
		function stringify(object) {
			var simpleObject = {};
			var children = '';
			var links = '';
			var assessments = '';
		
		    for (var prop in object){
		        
		        if (!object.hasOwnProperty(prop)){
		            simpleObject[prop] = "[circular]";
		            
		        } else if (typeof(object[prop]) == 'object' && object[prop] != null){
		            if(prop == 'children') {		            
		            	var nodes = object.children;		            	
		            	nodes.forEach(function(d, i) {
		            		children += stringify(d);
		            		if(i != nodes.length - 1) {
		            			children += ',';
		            		}
		            	});
		            } else if(prop == '_children') {            
		            	var nodes = object._children;		            	
		            	nodes.forEach(function(d, i) {
		            		children += stringify(d);
		            		if(i != nodes.length - 1) {
		            			children += ',';
		            		}
		            	});
		            } else if(prop == 'links') {
		            	var nodes = object.links;		            	
		            	nodes.forEach(function(d, i) {
		            		links += stringify(d);
		            		if(i != nodes.length - 1) {
		            			links += ',';
		            		}
		            	});
		            } else if(prop == 'assessments') {
		            	var nodes = object.assessments;		            	
		            	nodes.forEach(function(d, i) {
		            		assessments += stringify(d);
		            		if(i != nodes.length - 1) {
		            			assessments += ',';
		            		}
		            	});
		            } else {
		            	simpleObject[prop] = "[circular]";
		            }
		            
		        } else  if (typeof(object[prop]) == 'function'){
		            simpleObject[prop] = "[circular]";
		            
		        } else if (prop == 'confidence') {
		        	
		        	var value = object[prop];
		        	if(typeof value == "number") {
		        		if(value % 1 === 0) {
		        			value += ".0";
		        		} else {
		        			value = value.toString();
		        		}
		        	}
		        
		        	simpleObject[prop] = value;
		        	
		        } else {
		        	simpleObject[prop] = object[prop];
		        }
			}
			
			if(children.length > 1) {
				children = ',"children":[' + children + ']'
			}
			
			if(links.length > 1) {
				links = ',"links":[' + links + ']'
			}
			
			if(assessments.length > 1) {
				assessments = ',"assessments":[' + assessments + ']'
			}
			
		    return JSON.stringify(simpleObject).slice(0, -1) + children + assessments + links + '}';
		}
	    
	    return stringify(object);
	}-*/;
	
	/**
	 * Automatically splits an SVG text element into tspan elements displayed on new lines and appends
	 * an ellipsis if the text exceeds the given height
	 * 
	 * @param selection The text selection to format.
	 * @param maxWidth The width at which to wrap lines
	 * @param maxHeight The height at which to add an ellipsis
	 */
	private native void formatText(Selection selection, double maxLines) /*-{
	
		function wrap(text) {
			text.each(function(d, i) {
				
				//get the SVG text element where the text for the selected data set is
	    		var text = $wnd.d3.select(this);
	    		
	    		if(text.text() != null && text.text().length > 0){
	    		
	    			var trimmedText = text.text().trim();
	    			
	    			var maxWidth = @mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.CollapsibleTree::MAX_NODE_WIDTH 
	    					- @mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.CollapsibleTree::NODE_ICON_WIDTH;
	    			
	    			if(d.assessments != null && d.assessments.length > 0){
	    				
	    				//if a node has assessments, it's text width should be decreased to make room for the assessment icon
	    				maxWidth -= @mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree.CollapsibleTree::ASSESSMENT_ICON_WIDTH;
	    			}
	    		
		    		//split the text into words based on whitespace
		        	var words = trimmedText.split(/\s+/).reverse(),
		        	word,	    
		        	
		        	//initialize the lines that the text will be wrapped into
		        	line = [],
		        	lineNumber = 0,
		        	lineHeight = 1.1, // ems
		        	
		        	//keep track of the positioning of the text element so that new lines are placed in the current position
			        y = text.attr("y"),
			        x = text.attr("x"),
			        
			        //create a tspan element to contain the next line
			        tspan = text.text(null).append("tspan").attr("x", x).attr("y", y);
			    
			    	//iterate through the array of words
			    	while (word = words.pop()) {
			    		
			    		//add each word to the current line
			      		line.push(word);
			      		
			      		tspan.text(line.join(" "));		      		
			      		var lineLength = 0;
			      		
			      		try{
			      			lineLength = tspan.node().getComputedTextLength();
			      			
			      		} catch(e){
			      			
			      			//Apparently, IE throws an error if getComputedTextLength() is called before a text node becomes visible. 
			      			//Since the tree gets redrawn whenever it is attached and shown in the document, there really
			      			//isn't anything to do here, as the problem will resolve itself automatically.
			      		}
			      		
			      		if (lineLength > maxWidth) {		      			
			      				   			
			      			//if the current line exceeds the maximum width for this text element, we need to either move it to
			      			//the next line or break it onto the next line
			        		line.pop();
			        		
			        		//calculate the length of the word that exceeded the max width
			        		var currentText = tspan.text();
			        		tspan.text(word);
			        		
			        		var wordLength = tspan.node().getComputedTextLength();
			        		
			        		tspan.text(currentText);
			        		
			        		if(wordLength > maxWidth){
			        			
			        			//if the word is longer than the line it will be placed on, we need to break it
			        			if(line.length != 0){
			        				
			        				//if the current line contains other words, move the current word to the next line and break it there
			        				if(lineNumber + 1 >= maxLines){
		      				
					      				//the next line would exceed the maximum height, so just cut off the 
					      				//rest of the text with an ellipsis
					      				line.push("...");
					      				tspan.text(line.join(" "));
					      				
					      				break;	
					      									      				
					      			} else {
			        				
			        					//the next line won't exceed the maximum height, so go ahead and move the word to the next line
				        				tspan.text(line.join(" "));
						        		line = [];
						        		tspan = text.append("tspan").attr("x", x).attr("y", y).attr("dy", ++lineNumber * lineHeight +  "em").text("");
						        		
						        		words.push(word);
						        		
						        		continue;
			        				}
			        			}
			        					        			
			        			var substring = word.substring(0, 0);
			        			tspan.text(substring);
			        			
			        			var exceededHeight = false;
			        			
			        			//determine where the word should be broken (i.e. where it exceeds the max width)
			        			for(var charIndex = 1; charIndex < word.length; charIndex++){
			        				
			        				substring = word.substring(0, charIndex);
			        				
			        				tspan.text(substring);
			        				
			        				if(tspan.node().getComputedTextLength() > maxWidth){
			        					
			        					//insert a hyphen at the break point
			        					var unwrappedChars = word.substring(0, charIndex - 1) + "-";
			        					
			        					//fill the remainder of the current line with the characters before the break point
			        					line.push(unwrappedChars);
			        					
			        					tspan.text(line.join(" "));
			        					
			        					if(lineNumber + 1 >= maxLines){
		      				
						      				//the next line would exceed the maximum height, so just cut off the 
						      				//rest of the text with an ellipsis
						      				line.push("...");
						      				tspan.text(line.join(" "));
						      				
						      				exceededHeight = true;
						      				
						      				break;						      				
						      			}
			        					
			        					//move the remaining characters to the next line
			        					var wrappedChars = word.substring(charIndex - 1, word.length)
			        					
			        					line = [];			          					
				        				tspan = text.append("tspan").attr("x", x).attr("y", y).attr("dy", ++lineNumber * lineHeight +  "em").text("");
				        				
				        				words.push(wrappedChars);
				        				
			        					break;
			        				}
			        			}
	
								if(exceededHeight){
									
									//we've already exceeded the max height, so we don't need to display any more words
									break;
								}
			        			
			        		} else {
			        			
			        			//the current word is smaller than the max width, so we can put it on the next line without breaking it
			        			if(lineNumber + 1 >= maxLines){
		      				
				      				//the next line would exceed the maximum height, so just cut off the 
				      				//rest of the text with an ellipsis
				      				line.push("...");
				      				tspan.text(line.join(" "));
				      				
				      				break;
				      				
				      			} else {
			        		
			        				//the next line won't exceed the maximum height, so move the current word to the next line
					        		tspan.text(line.join(" "));
					        		line = [word + " "];
					        		tspan = text.append("tspan").attr("x", x).attr("y", y).attr("dy", ++lineNumber * lineHeight +  "em").text(word);
			        			}
			        		}
			      			
		      			}
		    		}
	    		}
	    		
	    	});
  		};
  		
  		selection.call(wrap);
  		
	}-*/;
	
	/**
	 * Gets the sort function used to separate this tree's nodes
	 * 
	 * @return the sort function
	 */
	private native Sort getSortFunction()/*-{
		
		return function(a,b){
			
			if(a.width && b.width){
			
				//calculate the total horizontal space taken up by nodes a and b
				var totalWidth = (a.width + 20) + (b.width + 20);
				
				//divide the total space in half and add some padding so that the nodes can't overlap
	  			return (totalWidth / 2) + 10;
	  			
			} else {
				return 10;
			}
  			
		};
		
	}-*/;
	
	/**
	 * Appends end nodes to all of the leaf nodes in this tree that do not already have them.
	 */
	private void fixEndNodes(){
		
		fixEndNodes(root);
		
		updateTree();
	}
	
	/**
	 * Appends end nodes to all of the leaf nodes in the given tree that do not already have them.
	 * 
	 * @param node the root node of the tree
	 */
	private void fixEndNodes(TreeNode node){
		
		if(node.children() != null && node.children().length() > 0){
			
			for(Value childNodeValue : node.children().asIterable()){				
				fixEndNodes(childNodeValue.<TreeNode>as());				
			}
			
		} else {
			
			boolean hasLinks = false;
			
			for(Value linkValue : root.getObjAttr("links").<Array<Value>>cast().asIterable()){
				
				TreeLink link = linkValue.as();
				
				if(node.id() == link.getSource()){
					
					//there's at least one link in the tree using this node as its source, 
					//so this node has at least one child
					hasLinks = true;
					
					break;
					
				} 		
			}
			
			if(!hasLinks){
				
				TreeNodeEnum nodeType = TreeNodeEnum.fromName(node.getType());
				
				if(!nodeType.equals(TreeNodeEnum.ADD_NODE) && !nodeType.equals(TreeNodeEnum.END_NODE)){
					
					//if this node has no children and is not an Add or End node, we need to append an End node to it.
					node.setAttr("children", createEndNode());
				}
				
			}
		}	
	}

	@Override
	public void onAttach(){
		
		super.onAttach();
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				
				if(root != null){
					updateTree();
				}
			}
		});		
	}
	
	@Override
	public void onResize() {
		
		if(root != null){
			
			//need to update the tree so it can wrap its nodes
			updateTree();
		}
	}

	/**
	 * Shows the given popup panel at the given coordinates and repositions it if necessary to keep it
	 * from going outside the client's viewable area.
	 * 
	 * @param x the preferred x position that the popup panel should be shown at
	 * @param y the preferred y position that the popup panel should be shown at
	 * @param popup the popup panel to show
	 */
	private void showWithinViewport(int x, int y, PopupPanel popup){
	 		
		popup.setPopupPosition(x, y);
		popup.show();
		
		// We need to try to keep the context menu from going off the page; otherwise, scrollbars can appear next to the map.
		// We don't want this to happen because it interfere's with the map's zooming controls.
		int top = popup.getPopupTop();
		int left = popup.getPopupLeft();
		
		int maxHeight = Window.getClientHeight() - top - 1;
		int maxWidth = Window.getClientWidth() - left;		
		
		int verticalDisplacement = 0;
		int horizontalDisplacement = 0 ;
		
		if(popup.getOffsetHeight()+ 5 + popup.getWidget().getOffsetHeight() > maxHeight){
			
			verticalDisplacement = maxHeight - (popup.getOffsetHeight() + 5 + popup.getWidget().getOffsetHeight());			
		}
		
		if(popup.getWidget().getOffsetWidth() > maxWidth){
			
			horizontalDisplacement = maxWidth - (popup.getWidget().getOffsetWidth());
		}
		
		popup.setPopupPosition(left + horizontalDisplacement, top + verticalDisplacement);
	}		
		
	/**
	 * A JavaScriptObject that allows access to the link properties of a TreeNode
	 */
	private static class TreeLink extends Link {
		
		/**
		 * Class constructor
		 */
		protected TreeLink() {
			super();
		}
		
		/**
		 * Gets the source node id.
		 * 
		 * @return The id of the source node.
		 */
		public final native int getSource() /*-{
			return this.source || -1;
		}-*/;

		/**
		 * Sets the source node id.
		 * 
		 * @param sourceId The id of the source node.
		 */
		public final native void setSource(int sourceId) /*-{
			this.source = sourceId;
		}-*/;
		
		/**
		 * Gets the target node id.
		 * 
		 * @return The id of the target node.
		 */
		public final native int getTarget() /*-{
			return this.target || -1;
		}-*/;

		/**
		 * Sets the target node id.
		 * 
		 * @param targetId The id of the target node.
		 */
		public final native void setTarget(int targetId) /*-{
			this.target = targetId;
		}-*/;
		
		/**
		 * Adds an attribute to the TreeLink
		 * @param name The name of the attribute
		 * @param value The value of the attribute
		 */
		public final native void setAttr(String name, String value) /*-{
			this[name] = value;
		}-*/;
		
	}
	
	/**
	 * A DatumFunction to manipulate TreeNodes when they are clicked.
	 */
	private class TreeNodeClicked implements DatumFunction<Void> {
		@Override
		public Void apply(Element context, Value d, int index) {
			TreeNode node = d.<TreeNode> as();
			
			if(linkNodes) {
				
				//make that the clicked node is a valid choice for a new link
				boolean linkAllowed = false;
				
				if(!selectedNode.equals(node)){
				
					TreeNodeEnum nodeType = TreeNodeEnum.fromName(node.getType());
					TreeNodeEnum selectedNodeType = TreeNodeEnum.fromName(selectedNode.getType());
					
					if(TreeNodeEnum.QUESTION_NODE.equals(selectedNodeType)){
						
						if(TreeNodeEnum.CHOICE_NODE.equals(nodeType) && !selectedNode.equals(node.parent())){						
							linkAllowed = true;
						}
						
					} else if(TreeNodeEnum.MESSAGE_NODE.equals(selectedNodeType)
							|| TreeNodeEnum.CHOICE_NODE.equals(selectedNodeType)){
						
						if(TreeNodeEnum.MESSAGE_NODE.equals(nodeType) 
								|| TreeNodeEnum.QUESTION_NODE.equals(nodeType)){
							
							linkAllowed = true;
							
							if(selectedNode.children() != null){
								selectedNode.setAttr("children", null);
							}
							
						} else if(TreeNodeEnum.END_NODE.equals(nodeType)){
							
							//create a link to the target node unless it is an end node immediately after the selected node
							if(node.parent() == null 
									|| selectedNode.children() == null
									|| selectedNode.children().length() != 1
									|| !node.parent().equals(selectedNode.children().get(0))){
								
								linkAllowed = true;
								
								if(selectedNode.children() != null){
									selectedNode.setAttr("children", null);
								}
							}
						}
					}
					
				} else {
					linkAllowed = false;
				}
				
				if(linkAllowed){
				
					// If the user is attempting to select a node, create a 
					// link between the right-clicked node and this one.
					createLink(root, selectedNode.id(), node.id());
					update(root);
					
					linkNodes = false;
					
					updateTree();
					
				} else {
					
					linkNodes = false;
					
					updateTree();
				}
			
			} else {
				selectedNode = node;
				selectedNode.setStrAttr("contextmenu", "nodeOptons");
				showContextMenu();
				
				updateTree();
			}			
			
			return null;
		}
	}
}