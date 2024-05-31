/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.github.gwtd3.api.Coords;
import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.arrays.ForEachCallback;
import com.github.gwtd3.api.behaviour.Drag;
import com.github.gwtd3.api.behaviour.Drag.DragEventType;
import com.github.gwtd3.api.behaviour.Zoom;
import com.github.gwtd3.api.behaviour.Zoom.ZoomEventType;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Transform;
import com.github.gwtd3.api.core.UpdateSelection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.functions.KeyFunction;
import com.github.gwtd3.api.layout.HierarchicalLayout.Node;
import com.github.gwtd3.api.layout.Link;
import com.github.gwtd3.api.layout.Tree;
import com.github.gwtd3.api.svg.Line;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.Course;
import generated.course.DkfRef;
import generated.course.LessonMaterial;
import generated.course.PresentSurvey;
import generated.course.SlideShowProperties;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.CourseValidationResults.CourseObjectValidationResults;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;

/**
 * An interactive tree widget used to represent a course
 * 
 * @author nroberts
 */
public class CourseTree extends FlowPanel implements HasSelectionHandlers<TreeNode>, RequiresResize{
    
    /**
     * Interface for the event binder for this class.
     * 
     * @author sharrison
     */
    interface MyEventBinder extends EventBinder<CourseTree> {
    }

    /** Binder for handling events. */
    private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
	
	/** UI constants of the tree widget. */
	private static final int WIDTH = 960;
	private static final int HEIGHT = 600;
	
	/** The number of milliseconds it should take for animations to finish in the tree */
//	private static final int TRANSITION_ANIMATION_DURATION = 250;
	
	private static final MyResources CSS = Bundle.INSTANCE.css();
	
	/** The number of free-space pixels to place to the left of each node in the tree */
	protected static final double HORIZONTAL_MARGIN_BETWEEN_NODES = 60;
	
	/** The number of free-space pixels to place to the top of each node in the tree */
	protected static final int VERTICAL_MARGIN_BETWEEN_NODES = 200;
	
	/** The x translation that should be applied to the first node in the tree */
	private static final int FIRST_NODE_X_TRANSLATION = 45;
	
	/** The y translation that should be applied to the first node in the tree */
	private static final int FIRST_NODE_Y_TRANSLATION = 65;
	
	/** The pixel width of each node in the tree */
	private static final double TREE_NODE_WIDTH = 210;
	
	/** The pixel height of each node in the tree */
	private static final int TREE_NODE_HEIGHT = 100;
	
	/** The default zoom level */
	protected static final double DEFAULT_ZOOM_LEVEL = 1;
	
	/**
	 * The number of pixels away from the top/bottom of the course tree's scroll panel at which
	 * the tree should start scrolling during a drag operation
	 */
	private static final int DRAG_SCROLL_THRESHOLD = 50;
	
	/** D3 layout components */
	private Tree tree = null;
	private TreeNode root = null;
	private TreeNode selectedNode = null;
	private TreeLink selectedLink = null;
	private TreeNodeEnum newNodeType;
	private Line diagonal = null;
	private Selection svg = null;
	private Selection deleteSvg = null;
	private Selection trashIconSvg = null;
	private Selection trashToolTip = null;
	private boolean readOnly = false;
    private Map<String,CourseObjectValidationResults> courseValidationResultsMap = new HashMap<String, CourseObjectValidationResults>();
	
	/** Whether or not the user is creating a link between two nodes. */
	private boolean linkNodes = false;
	
	/** Keeps track of tree ids. */
	private int treeLength = 0;	

	/** Instance of the logger */
	private static Logger logger = Logger.getLogger(CourseTree.class.getName());
	
	/** Interface to allow CSS file access */
	public interface Bundle extends ClientBundle {
		public static final Bundle INSTANCE = GWT.create(Bundle.class);

		@Source("TreeStyles.css")
		public MyResources css();
	}

	/** Interface to allow CSS style name access */
	interface MyResources extends CssResource {

		String link();

		String multiLink();
		
		String node();

		String border();
		
		String icon();
		
		String failedValidationIcon();
		
		String failedValidationIcon_hidden();
		
		String text();
		
		String startNodeHeader();
		
		String endNodeHeader();

		String header();
		
		String separator();
		
		String endNodeSeparator();
		
		String nodeRect();
		
		String startNodeRect();
		
		String endNodeRect();
		
		String selectedNodeRect();
		
		String selectedDisabledNodeRect();
		
		String disabledNodeRect();
		
		String selectedDeleteRect();
		
		String dropArea();
	}
	
	/**
	 * An interface representing classes capable of returning functions for calculating attribute values for D3 SVG selectors.
	 * This interface was introduced to cut down on code repetition in the update(TreeNode) method, since it allows new nodes
	 * and updating nodes to retrieve the same functions for calculating their attributes
	 */
	private static interface AttributeUpdator{
		
		public DatumFunction<?> getAttributeValue(String attributeName);
	}
	
	/**
	 * A helper class used to retrieve functions for calculating attributes for the rectangles used by tree nodes
	 * 
	 * @author nroberts
	 */
	private class NodeRectangleUpdator implements AttributeUpdator{
		
		/** The set of attribute functions provided by this updator */
		private Map<String, DatumFunction<?>> functions = new HashMap<String, DatumFunction<?>>();
		
		public NodeRectangleUpdator(){
			
			functions.put("class", 
					new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					StringBuilder sb = new StringBuilder(CSS.nodeRect());
					
					TreeNode node = d.<TreeNode> as();
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					if(type != null){
						
						if(TreeNodeEnum.COURSE_END.equals(type) ){							
							sb.append(" ").append(CSS.endNodeRect());		
						
						}
					}
										
					boolean selectedNodeFlag = node.equals(selectedNode);
					boolean disabledFlag = CourseElementUtil.isTransitionDisabled(nodeToCourseElement.get(node));

                    if (selectedNodeFlag && disabledFlag) {
                        sb.append(" ").append(CSS.selectedDisabledNodeRect());
                    }
                    else if (disabledFlag) {
                        sb.append(" ").append(CSS.disabledNodeRect());
                    }
                    else if(selectedNodeFlag){
						sb.append(" ").append(CSS.selectedNodeRect());	
					}
					
					return sb.toString();
				}
			});
			
			
			functions.put("width", 
					new DatumFunction<String>() {				
				@Override
				public String apply(Element context, Value d, int index) {
					
					Double width = d.<TreeNode> as().getWidth();
					return (width.intValue() + 20) + "px";
				}
			});
			
			
			functions.put("x", 
					new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					Double width = d.<TreeNode> as().getWidth();
					return (width.intValue() / -2) + "px";
				}
			});
			
			
			functions.put("rx", 
					new DatumFunction<String>(){

				@Override
				public String apply(Element context, Value d, int index) {
					
					return "5px";
				}
				
			});
		}
		

		@Override
		public DatumFunction<?> getAttributeValue(String attributeName) {
			
			return functions.get(attributeName);
		}
	}
	
	/**
	 * A helper class used to retrieve functions for calculating attributes for the images used by tree nodes
	 * 
	 * @author nroberts
	 */
	private class NodeImageUpdator implements AttributeUpdator{
		
		/** The set of attribute functions provided by this updator */
		private Map<String, DatumFunction<?>> functions = new HashMap<String, DatumFunction<?>>();
		
		public NodeImageUpdator(){
			
			functions.put("xlink:href", 
					new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode>as();
					
					String url = "";
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					if(type != null){
						
						if(TreeNodeEnum.TRANSITION.equals(type)){
							
							Serializable transition = nodeToCourseElement.get(node);
							
							// Nick: Originally, I was using ImageResources from GatClientBundle here and calling the getDataUri()
							// method to get their Base64 encoded URLs. For some reason, though, dragging and dropping nodes in 
							// the tree would cause the getDataUri() method to return garbage, corrupting the image in the process.
							// I have no idea why this happens, but using a regular image URL instead of a Base64 encoded one seems 
							// to fix the issue.
							String icon = CourseElementUtil.getTypeIcon(transition);
							
							if(icon != null){
								return icon;
							}							
						}					
					}					
					
					return url;
				}
			});
            
            functions.put("xlink:href-validation-css", new DatumFunction<String>() {
                @Override
                public String apply(Element context, Value d, int index) {

                    TreeNode node = d.<TreeNode>as();

                    String visible = CSS.failedValidationIcon_hidden();

                    TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());

                    if (type != null) {

                        if (TreeNodeEnum.TRANSITION.equals(type)) {

                            Serializable transition = nodeToCourseElement.get(node);

                            if (courseObjectFailedValidation(transition)) {
                                visible = CSS.failedValidationIcon();
                            }
                        }
                    }

                    return visible;
                }
            });
		}
		

		@Override
		public DatumFunction<?> getAttributeValue(String attributeName) {
			
			return functions.get(attributeName);
		}
	}
	
	/** A mapping from each tree node to it's associated course element, if it has one */
	private Map<TreeNode, Serializable> nodeToCourseElement = new HashMap<TreeNode, Serializable>();
	
	/** A function to be invoked whenever a tree node is double clicked */
	private DatumFunction<Void> nodeClickFunction;
	
	/** A function to be invoked whenever a tree node's context menu is shown */
	private DatumFunction<Void> nodeContextFunction;
	
	/** A function to be invoked whenever a tree node is dragged */
	private DatumFunction<Void> nodeDragStartFunction;
	
	/** A function to be invoked whenever a tree node has a DOM element dragged into it */
	private DatumFunction<Void> nodeDragEnterFunction;
	
	/** A function to be invoked whenever a tree node has a DOM element dragged over it */
	private DatumFunction<Void> nodeDragOverFunction;
	
	/** A function to be invoked whenever a tree node has a DOM element dropped on it */
	private DatumFunction<Void> nodeDropFunction;
	
	/** A function to be invoked whenever the SVG container has a DOM element dragged over it */
	private DatumFunction<Void> svgDragOverFunction;
	
	/** A function to be invoked whenever the SVG container has a DOM element dropped on it */
	private DatumFunction<Void> svgDropFunction;
	
	/** A function to be invoked whenever a tree node has been deleted. */
	private DatumFunction<Void> deleteNodeFunction;
	
	/** The updater used to get attributes for tree node rectagles */
	private NodeRectangleUpdator nodeRectUpdator = new NodeRectangleUpdator();
	
	/** The updater used to get attributes for tree node rectagles */
	private NodeImageUpdator nodeImageUpdator = new NodeImageUpdator();
	
	/** A bounding rectangle around the main SVG element used to show a border when a course object is dragged over it */
	private Selection svgBorder;
	
	/** A D3 behavior used to allow nodes in the course tree to be dragged on top of one another*/
	private Drag dragBehavior;
	
	/** The tree node currently being dragged (not used when dragging from the "Course Objects" panel) */
	private TreeNode nodeBeingDragged = null;
	
	private boolean deleteNode = false;
	
	/** An object used to handle zooming on the tree */
	private Zoom zoom;
	
	/** The main SVG element containing the course tree */
	private Selection mainSvg;
	
	/** The vertical position that the mouse started at when the user is scrolling by dragging the mouse */
	private int verticalScrollingMouseStart = 0;
	
	/** The vertical position that scroll bar started at when the user is scrolling by dragging the mouse */
	private int verticalScrollingViewStart = 0;
	
	/** 
	 * The X translation applied to the course tree based on the current zoom level. This is used 
	 * to ensure that zooming in at high zoom levels doesn't push the course tree out of the view.
	 * Basically, the padding between the left side of the course tree panel and the course tree 
	 * itself would normally increase with an increasing zoom level, so this value is used to 
	 * counteract that effect so that the course tree's left side remains at roughly the same 
	 * position.
	 */
	protected double zoomedXTranslation;
	
	/** 
	 * The Y translation applied to the course tree based on the current zoom level. This is used
	 * to ensure that the topmost part of the course tree never gets stuck under the toolbar.
	 */
	protected double zoomedYTranslation;
	
	/** The scroll panel used to let users scroll through the course tree */
	private final ScrollPanel scroller = new ScrollPanel();
	
	/** 
	 * A timer used to handle the scrolling animation that occurs when the user drags a course node
	 * near the top or bottom of the course tree's scroll panel.
	 */
	private Timer dragScrollAnimationTimer;
	
	/** The Y-coordinate of the mouse during the current drag operation*/
	private Integer mouseDragY;
	
	/**
	 * Creates a collapsible tree widget.
	 */
	public CourseTree() {
	    
	    eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
		
		// Make sure the css style names are accessible before creating the tree.
		Bundle.INSTANCE.css().ensureInjected();
		
		// Setup the tree layout
		tree = D3.layout().tree().size(WIDTH, HEIGHT);
		
		// Setup path drawing
		diagonal = D3.svg().line()
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
		
		// Setup dragging behavior
		dragBehavior = D3.behavior().drag().on(DragEventType.DRAG, new DatumFunction<Void>() {
			
			double dragX;
			double dragY;
			
			@Override
			public Void apply(final Element context, Value d, int index) {
				
				if(GatClientUtility.isReadOnly()){
				    return null;
				}
				
			    TreeNode node = d.<TreeNode>as();
				
				TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
				
				final Selection selection = D3.select(context);
				
				if(type != null){
					
					if(TreeNodeEnum.TRANSITION.equals(type)){
						
						if(nodeBeingDragged == null && 
								(Math.abs(D3.dragEvent().dx()) > 2 		//x drag threshold
								|| Math.abs(D3.dragEvent().dy()) > 2) 	//y drag threshold
							){
							
							//start dragging a transition node once the mouse has moved past a certain threshold
							//(this prevents drag events from overlapping with click events)
							nodeBeingDragged = node;
							
							if(nodeBeingDragged.equals(selectedNode)){
								
								//deselect this node while it is being dragged
								setSelectedNode(null, true);
							}
							
							if(nodeDragStartFunction != null){
								nodeDragStartFunction.apply(context, d, index);
							}
							
							//move the node to the end of its parent container so it has the highest z-index
							context.getParentElement().appendChild(context);	
							
							//create a timer to update the scroller whenever the node is dragged near the top
							//or bottom of the scrollable area
							dragScrollAnimationTimer = new Timer() {
								
								@Override
								public void run() {
									
									if(mouseDragY != null){
									
										int scrollPos = scroller.getVerticalScrollPosition();
										
										if(mouseDragY < DRAG_SCROLL_THRESHOLD
												&& scrollPos > scroller.getMinimumVerticalScrollPosition()){
											
											int dY = mouseDragY - DRAG_SCROLL_THRESHOLD;
											
											//update the node's position or else it won't move with the scroll
											Transform transform = Transform.parse(selection.attr("transform"));
											
											selection.attr("transform", transform.translate(
													transform.translate().getInt(0),
													(int) (transform.translate().getInt(1) + dY/zoom.scale())
											).toString());
											
											//scroll up when the top threshold has been reached
											scroller.setVerticalScrollPosition(Math.max(
													scroller.getMinimumVerticalScrollPosition(), 
													scrollPos + dY
											));											
										
										} else if(mouseDragY > scroller.getOffsetHeight() - DRAG_SCROLL_THRESHOLD
												&& scrollPos < scroller.getMaximumVerticalScrollPosition()){
											
											int dY = mouseDragY - (scroller.getOffsetHeight() - DRAG_SCROLL_THRESHOLD);
											
											//update the node's position or else it won't move with the scroll
											Transform transform = Transform.parse(selection.attr("transform"));
											
											selection.attr("transform", transform.translate(
													transform.translate().getInt(0),
													(int) (transform.translate().getInt(1) + dY/zoom.scale())
											).toString());
											
											//scroll down when the bottom threshold has been reached
											scroller.setVerticalScrollPosition(Math.min(
													scroller.getMaximumVerticalScrollPosition(), 
													scrollPos + dY
											));									
										}
									}
								}							
							};
							
							dragScrollAnimationTimer.scheduleRepeating(50);
						} 
						
						if(nodeBeingDragged != null){
						
							//if a transition node is being dragged, move it with the mouse
							dragX = D3.eventAsCoords().x();
							dragY =  D3.eventAsCoords().y();
							
							D3.select(context).attr("transform", "translate(" + dragX + "," + dragY + ")");
							
							//disable pointer events on the dragged node, otherwise it will consume mouse events rather than nodes underneath it
							D3.select(context).attr("pointer-events", "none");
							
							mouseDragY = (int) Math.round(D3.mouseAsCoords(scroller.getElement()).y());
						}
					}
				}
				
				return null;
			}
		}).on(DragEventType.DRAGEND, new DatumFunction<Void>() {
			
			@Override
			public Void apply(final Element context, Value d, int index) {
				
				TreeNode node = d.<TreeNode>as();
				
				TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
				
				if(type != null){
					
					if(TreeNodeEnum.TRANSITION.equals(type)){
						
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							
							@Override
							public void execute() {
							
								if(deleteNode) {
									
									deleteNode = false;
									deleteNode(nodeBeingDragged, context);
									
								} else if(nodeBeingDragged != null){
								
									//reset the node to its original location when dragging is complete
									double originalX = nodeBeingDragged.x();
									double originalY = nodeBeingDragged.y();
									nodeBeingDragged = null;
									
									D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");
									
									//re-enable mouse events on the dragged node
									D3.select(context).attr("pointer-events", (String) null);
								}
							}
						});			
					}
				}
				
				if(dragScrollAnimationTimer != null){
					
					//cancel and reset the scrolling animation timer since we're no longer dragging
					dragScrollAnimationTimer.cancel();
					dragScrollAnimationTimer = null;
				}
				
				mouseDragY = null;
				
				return null;
			}
		});
		
		// Add a scroll panel to wrap the main SVG panel
		scroller.setSize("100%", "100%");
		add(scroller);
		
		// Setup panning behavior
		zoom = D3.behavior().zoom().on(ZoomEventType.ZOOMSTART, new DatumFunction<Void>() {

			@Override
			public Void apply(Element context, Value d, int index) {
				
				verticalScrollingMouseStart = (int) D3.mouseY(scroller.getElement());
				verticalScrollingViewStart = scroller.getVerticalScrollPosition();

				return null;
			}

		}).on(ZoomEventType.ZOOM, new DatumFunction<Void>() {

			@Override
			public Void apply(Element context, Value d, int index) {	
				
				int yOffset = verticalScrollingMouseStart - ((int) D3.mouseY(scroller.getElement()));
				
				scroller.setVerticalScrollPosition(verticalScrollingViewStart + yOffset);

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
				.on("dragexit", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						svgBorder.attr("class", "");
						
						return null;
					}})
				.on("dragleave", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						svgBorder.attr("class", "");
						
						return null;
					}})
				.on("dragend", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						svgBorder.attr("class", "");
						
						return null;
					}})
				.on("dragover", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						svgBorder.attr("class", CSS.dropArea());
						
						if(selectedNode != null){
							setSelectedNode(null, true);
						}
						
						if(svgDragOverFunction != null){
							svgDragOverFunction.apply(context, d, index);
						}
						
						mouseDragY = (int) Math.round(D3.mouseAsCoords(scroller.getElement()).y());
						
						return null;
					}})
				.on("drop", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						svgBorder.attr("class", "");
						
						if(svgDropFunction != null){
							svgDropFunction.apply(context, d, index);
						}
						
						return null;
					}
				});
		
		//add the group that will contain the course tree
		svg = mainSvg.append("g");
		
		//add a border to the SVG for when the user drags something over it
		svgBorder = mainSvg.append("rect")
			.attr("x", 0)
			.attr("y", 0)
			.attr("height", "100%")
			.attr("width", "100%")
			.attr("fill", "none");
		
		//add the delete image
		deleteSvg = D3.select(this).append("svg")
			.attr("style", "overflow: visible; position: absolute; width: 1px; height: 1px; right: 17px; bottom: 0px;");
		
		trashIconSvg = deleteSvg.append("svg:image")
			.attr("y", -100)
			.attr("x", -90)
			.attr("width", "78px")
			.attr("height", "78px")
			.attr("opacity", "0.9")
			.attr("style", "-ms-interpolation-mode: bicubic;")
			.attr("xlink:href", GatClientBundle.INSTANCE.trashcan().getSafeUri().asString());
		 
		 final Selection deleteRect = deleteSvg.append("rect")
		     .attr("width", "98px")
		     .attr("height", "98px")
		     .attr("x", -100)
		     .attr("y", -110)
		     .attr("rx", "5px")
		     .attr("fill", "transparent");
		     
		 trashToolTip = deleteRect.append("title").text("Drag a course object here to permanently delete.");
		 deleteRect.on("mouseleave", new DatumFunction<Void>() {

			  @Override
			  public Void apply(Element context, Value d, int index) {
				  deleteRect.attr("class", "");
				  deleteNode = false;
				  return null;
			  }
			  
		  })
		  .on("mouseover", new DatumFunction<Void>() {

			  @Override
			  public Void apply(Element context, Value d, int index) {
				  if(nodeBeingDragged != null && !readOnly) {
					  deleteRect.attr("class", CSS.selectedDeleteRect());
					  deleteNode = true;
				  }
				  return null;
			  }
		  });
		  			
		//Build the arrows that will be added to the links connecting nodes
		svg.append("svg:defs").selectAll("marker")
			.data(Array.fromJavaArray(new String[]{"end"}))
		  .enter().append("svg:marker")
			.attr("id", "end")
			.attr("viewBox", "0 -5 10 10")
		    .attr("refX", 12)
		    .attr("refY", 0)
		    .attr("markerWidth", 6)
		    .attr("markerHeight", 6)
		    .attr("orient", "auto")
		    .attr("fill", "black")
		  .append("svg:path")
		    .attr("d", "M0,-5L10,0L0,5")
		;
		
		//Build the panel that will allow users to zoom in/out on the tree
		FlowPanel zoomPanel = new FlowPanel();
		zoomPanel.addStyleName("courseTreeZoomPanel");
		
		ButtonGroup zoomGroup = new ButtonGroup();
		
		Button zoomInButton = new Button("", IconType.SEARCH_PLUS, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				zoom.scale(zoom.scale() * 1.25);

				applyZoom();
			}
		});
		ManagedTooltip zoomInTooltip = new ManagedTooltip(zoomInButton, "Zoom in");
		zoomGroup.add(zoomInTooltip);
		
		Button zoomOutButton = new Button("", IconType.SEARCH_MINUS, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				zoom.scale(zoom.scale()/1.25);

				applyZoom();
			}
		});
	    ManagedTooltip zoomOutTooltip = new ManagedTooltip(zoomOutButton, "Zoom out");
		zoomGroup.add(zoomOutTooltip);
		
		Button resetZoomButton = new Button("", IconType.CROSSHAIRS, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				zoom.scale(DEFAULT_ZOOM_LEVEL);

				applyZoom();
			}
		});
		ManagedTooltip zoomResetTooltip = new ManagedTooltip(resetZoomButton, "Reset zoom");
		zoomGroup.add(zoomResetTooltip);
		
		zoomPanel.add(zoomGroup);
		
		add(zoomPanel);
		
		//Make this widget take up the size of its container
		setSize("100%", "100%");
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				
				if(root != null){
					
					//need to update the tree so it can wrap its nodes
					updateTree();
					
					scrollToSelectedNode();
				}
			}
		});
		
		//Apply the initial zoom level
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				
				zoom.scale(DEFAULT_ZOOM_LEVEL);
				
				applyZoom();
			}
		});		
	}
	
    /**
     * On course object renamed.
     *
     * @param event the event that contains the course object that was renamed
     */
    @EventHandler
    protected void onCourseObjectRenamedEvent(CourseObjectRenamedEvent event) {
        
        if (event != null) {
            
            logger.info("Received course object renamed event:\n"+event);
            
            //update the validation map
           CourseObjectValidationResults validationResults = courseValidationResultsMap.remove(event.getPreviousName());
           if(validationResults != null){
               
               String newName = CourseElementUtil.getTransitionName(event.getCourseObject());
               if(newName != null){
                   logger.info("Updated course validation results map for old course object named '"+event.getPreviousName()+"' to new name '"+newName+"'.");
                   courseValidationResultsMap.put(newName, validationResults);
               }
           }
        }
    }
	
	/**
	 * Updates the view of the course tree to match the current zoom level. This method translates and scales the
	 * course tree based on the current zoom level and then updates the tree so it can reposition its nodes.
	 */
	protected void applyZoom() {
		
		zoomedXTranslation = FIRST_NODE_X_TRANSLATION + (TREE_NODE_WIDTH/2)*zoom.scale();
		zoomedYTranslation = FIRST_NODE_Y_TRANSLATION + (TREE_NODE_HEIGHT/2)*zoom.scale();
		
		svg.attr("transform", 
				"translate(" + 
						zoomedXTranslation + "," + 
						zoomedYTranslation + 
				") " + 
				"scale(" + zoom.scale() + ")");
		
		if(root != null){
			
			//need to update the tree so it can wrap its nodes
			updateTree();
		}
	}

	/**
	 * Updates the tree widget.
	 */
	public void updateTree() {
		update(root);
	}
	
	/**
	 * Creates a collapsible tree from a JSON string representation.
	 * 
	 * @param treeJSONStr The JSON string representation of a conversation tree.
	 */
	public void loadTree(Course course) {

		//preserve the selected object if it gets reloaded
		Serializable selectedObject = null;
		
		if(selectedNode != null){
			
			for(TreeNode node : nodeToCourseElement.keySet()){
				
				if(selectedNode.equals(node)){
					selectedObject = nodeToCourseElement.get(node);
					break;
				}
			}
		}
				
		//create a new tree based on the given course
		root = makeTree(course);
		update(root);
		
		if(selectedObject != null){
			
			//reselect the preserved object, if the loaded tree still has it
			selectCourseObject(selectedObject);
		}
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
	 * Presents the user with a confirmation dialog to delete the TreeNode.
	 * 
	 * @param node The TreeNode to delete
	 * @param context The context from a drag event. Can be null if there is no associated drag event.
	 */
	public void deleteNode(final TreeNode node, final Element context) {
		
		String nodeName = getNodeBody(node);
		String nodeHeader = getNodeHeader(node);
		
		Serializable courseObject  = getCourseObject(node);
		if(courseObject instanceof LessonMaterial) {
			final LessonMaterial lm = (LessonMaterial) courseObject;
			if(lm.getLessonMaterialList() != null 
					&& lm.getLessonMaterialList().getMedia() != null 
					&& !lm.getLessonMaterialList().getMedia().isEmpty()) {
				
				Serializable props = lm.getLessonMaterialList().getMedia().get(0).getMediaTypeProperties();
				if(props != null && props instanceof SlideShowProperties) {
				    //Custom delete logic for gift slide shows
				    
					SlideShowProperties properties = (SlideShowProperties) props;
					if(properties.getSlideRelativePath() != null && !properties.getSlideRelativePath().isEmpty()) {
						
						OkayCancelDialog.show("Delete " + nodeHeader + " course object?", 
								"Are you sure you want to <b>permanently</b> delete \"" + nodeName +"\"?"
										+ "<br/></br><b>Note:</b> The images in this slide show will also be deleted.", 
										"Delete",
										new OkayCancelCallback() {

							@Override
							public void okay() {
								
								// Reset node position so it doesn't hang by the trash can while the delete is in progress
								if(nodeBeingDragged != null) {
									double originalX = nodeBeingDragged.x();
									double originalY = nodeBeingDragged.y();
									D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");
								}
								
								BsLoadingDialogBox.display("Deleting Slide Show Course Object", "Please wait...");
								String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
								List<String> filesToDelete = new ArrayList<String>();
                                final String filePath = GatClientUtility.getBaseCourseFolderPath() + "/Slide Shows/" + lm.getLessonMaterialList().getMedia().get(0).getName();
                                filesToDelete.add(filePath);
                                
                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                    @Override
                                    public void onFailure(Throwable error) {
                                    	BsLoadingDialogBox.remove();
                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                "Failed to delete the file.", 
                                                error.getMessage(), 
                                                DetailedException.getFullStackTrace(error));
                                        dialog.setDialogTitle("Deletion Failed");
                                        dialog.center();
                                    }

                                    @Override
                                    public void onSuccess(GatServiceResult result) {
                                    	BsLoadingDialogBox.remove();
                                        if(result.isSuccess()){
                                        	permanentlyDeleteNode(node, context);
                                        	
                                        } else {
                                        	resetNode(context);
                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                                            dialog.setTitle("Error");
                                            dialog.center();
                                        }
                                    }
                                    
                                });
							}

							@Override
							public void cancel() {
								resetNode(context);
							}										
						});
						
						return;
					}
				}
			}
		}else if(courseObject instanceof PresentSurvey){
		    PresentSurvey presentSurvey = (PresentSurvey)courseObject;
		    
		    if(presentSurvey.getSurveyChoice() instanceof generated.course.Conversation){
		        
		        generated.course.Conversation conversation = (generated.course.Conversation)presentSurvey.getSurveyChoice();
		        if(conversation != null){
		            
		            if(conversation.getType() instanceof generated.course.ConversationTreeFile){		        
		                //custom delete logic for Conversation tree
		            
		                final generated.course.ConversationTreeFile convTreeFile = (generated.course.ConversationTreeFile)conversation.getType();
	                    if(convTreeFile.getName() != null && !convTreeFile.getName().isEmpty()){
	                        
	                        OkayCancelDialog.show("Delete " + nodeHeader + " course object?", 
	                                "Are you sure you want to <b>permanently</b> delete \"" + nodeName +"\"?"
	                                        + "<br/></br><b>Note:</b> The conversation tree contents will also be deleted.</br></br>"+
	                                        "If you are using the underyling conversation tree somewhere else in the course consider removing the reference</br>"+
	                                        "to that conversation tree from this course object.  To remove the reference:</br>"+
	                                        "<ol><li>Click the conversation tree course object to open the editor</li>"+
	                                        "<li>Click the 'Remove' button next to 'Real-Time Assessment'</li>" +
	                                        "<li>Follow the instructions on the remove dialog to remove the reference but keep the conversation tree content.", 
	                                        "Delete",
	                                        new OkayCancelCallback() {

	                            @Override
	                            public void okay() {
	                                
	                                // Reset node position so it doesn't hang by the trash can while the delete is in progress
	                                if(nodeBeingDragged != null) {
	                                    double originalX = nodeBeingDragged.x();
	                                    double originalY = nodeBeingDragged.y();
	                                    D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");
	                                }
	                                
	                                BsLoadingDialogBox.display("Deleting Conversation Tree Course Object", "Please wait...");
	                                String username = GatClientUtility.getUserName();
	                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
	                                List<String> filesToDelete = new ArrayList<String>();
	                                final String filePath = GatClientUtility.getBaseCourseFolderPath() + "/" + convTreeFile.getName();
	                                filesToDelete.add(filePath);
	                                
	                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
	                                SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

	                                    @Override
	                                    public void onFailure(Throwable error) {
	                                        BsLoadingDialogBox.remove();
	                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
	                                                "Failed to delete the file.", 
	                                                error.getMessage(), 
	                                                DetailedException.getFullStackTrace(error));
	                                        dialog.setDialogTitle("Deletion Failed");
	                                        dialog.center();
	                                    }

	                                    @Override
	                                    public void onSuccess(GatServiceResult result) {
	                                        BsLoadingDialogBox.remove();
	                                        if(result.isSuccess()){
	                                            permanentlyDeleteNode(node, context);
	                                            
	                                        } else {
	                                            resetNode(context);
	                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
	                                            dialog.setTitle("Error");
	                                            dialog.center();
	                                        }
	                                    }
	                                    
	                                });
	                            }

	                            @Override
	                            public void cancel() {
	                                resetNode(context);
	                            }                                       
	                        });
	                        
	                        return;
	                    }
		            }else if(conversation.getType() instanceof generated.course.AutoTutorSession){
		                
		                generated.course.AutoTutorSession atSession = (generated.course.AutoTutorSession)conversation.getType();
		                if(atSession.getAutoTutorConfiguration() instanceof generated.course.AutoTutorSKO){
		                    
		                    generated.course.AutoTutorSKO atSKO = (generated.course.AutoTutorSKO)atSession.getAutoTutorConfiguration();
		                    if(atSKO.getScript() instanceof generated.course.LocalSKO){
		                        //custom delete logic for an AutoTutor SKO that is in the course folder
		                        
		                        final generated.course.LocalSKO localSKO = (generated.course.LocalSKO)atSKO.getScript();
		                        if(localSKO.getFile() != null && !localSKO.getFile().isEmpty()){
		                            
		                            OkayCancelDialog.show("Delete " + nodeHeader + " course object?", 
		                                    "Are you sure you want to <b>permanently</b> delete \"" + nodeName +"\"?"
		                                            + "<br/></br><b>Note:</b> The AutoTutor conversation script will also be deleted.</br></br>"+
		                                            "If you are using the underyling AutoTutor conversation script somewhere else in the course consider removing the reference</br>"+
		                                            "to that AutoTutor conversation script from this course object.  To remove the reference:</br>"+
		                                            "<ol><li>Click the AutoTutor conversation course object to open the editor</li>"+
		                                            "<li>Click the 'Remove' button next to 'Real-Time Assessment'</li>" +
		                                            "<li>Follow the instructions on the remove dialog to remove the reference but keep the AutoTutor conversation script.", 
		                                            "Delete",
		                                            new OkayCancelCallback() {

		                                @Override
		                                public void okay() {
		                                    
		                                    // Reset node position so it doesn't hang by the trash can while the delete is in progress
		                                    if(nodeBeingDragged != null) {
		                                        double originalX = nodeBeingDragged.x();
		                                        double originalY = nodeBeingDragged.y();
		                                        D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");
		                                    }
		                                    
		                                    BsLoadingDialogBox.display("Deleting AutoTutor Conversation Course Object", "Please wait...");
		                                    String username = GatClientUtility.getUserName();
		                                    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
		                                    List<String> filesToDelete = new ArrayList<String>();
		                                    final String filePath = GatClientUtility.getBaseCourseFolderPath() + "/" + localSKO.getFile();
		                                    filesToDelete.add(filePath);
		                                    
		                                    DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
		                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

		                                        @Override
		                                        public void onFailure(Throwable error) {
		                                            BsLoadingDialogBox.remove();
		                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
		                                                    "Failed to delete the file.", 
		                                                    error.getMessage(), 
		                                                    DetailedException.getFullStackTrace(error));
		                                            dialog.setDialogTitle("Deletion Failed");
		                                            dialog.center();
		                                        }

		                                        @Override
		                                        public void onSuccess(GatServiceResult result) {
		                                            BsLoadingDialogBox.remove();
		                                            if(result.isSuccess()){
		                                                permanentlyDeleteNode(node, context);
		                                                
		                                            } else {
		                                                resetNode(context);
		                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
		                                                dialog.setTitle("Error");
		                                                dialog.center();
		                                            }
		                                        }
		                                        
		                                    });
		                                }

		                                @Override
		                                public void cancel() {
		                                    resetNode(context);
		                                }                                       
		                            });
		                            
		                            return;
		                        }
		                        
		                    }
		                }else if(atSession.getAutoTutorConfiguration() instanceof generated.course.DkfRef){
		                    
		                    final DkfRef dkfRef = (generated.course.DkfRef)atSession.getAutoTutorConfiguration();
		                    if(dkfRef.getFile() != null && !dkfRef.getFile().isEmpty()){
		                        //custom delete logic for autotutor dkf
		                        
		                        OkayCancelDialog.show("Delete " + nodeHeader + " course object?", 
		                                "Are you sure you want to <b>permanently</b> delete \"" + nodeName +"\"?"
		                                        + "<br/></br><b>Note:</b> The real time assessment will also be deleted.</br></br>"+
		                                        "If you are using the underyling real time assessment somewhere else in the course consider removing the reference</br>"+
		                                        "to that real time assessment from this course object.  To remove the reference:</br>"+
		                                        "<ol><li>Click the course object to open the editor</li>"+
		                                        "<li>Click the 'Remove' button next to 'Real-Time Assessment'</li>" +
		                                        "<li>Follow the instructions on the remove dialog to remove the reference but keep the real time assessment content.", 
		                                        "Delete",
		                                        new OkayCancelCallback() {

		                            @Override
		                            public void okay() {
		                                
		                                // Reset node position so it doesn't hang by the trash can while the delete is in progress
		                                if(nodeBeingDragged != null) {
		                                    double originalX = nodeBeingDragged.x();
		                                    double originalY = nodeBeingDragged.y();
		                                    D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");
		                                }
		                                
		                                BsLoadingDialogBox.display("Deleting Course Object", "Please wait...");
		                                String username = GatClientUtility.getUserName();
		                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
		                                List<String> filesToDelete = new ArrayList<String>();
		                                final String filePath = GatClientUtility.getBaseCourseFolderPath() + "/" + dkfRef.getFile();
		                                filesToDelete.add(filePath);
		                                
		                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
		                                SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

		                                    @Override
		                                    public void onFailure(Throwable error) {
		                                        BsLoadingDialogBox.remove();
		                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
		                                                "Failed to delete the file.", 
		                                                error.getMessage(), 
		                                                DetailedException.getFullStackTrace(error));
		                                        dialog.setDialogTitle("Deletion Failed");
		                                        dialog.center();
		                                    }

		                                    @Override
		                                    public void onSuccess(GatServiceResult result) {
		                                        BsLoadingDialogBox.remove();
		                                        if(result.isSuccess()){
		                                            permanentlyDeleteNode(node, context);
		                                            
		                                        } else {
		                                            resetNode(context);
		                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
		                                            dialog.setTitle("Error");
		                                            dialog.center();
		                                        }
		                                    }
		                                    
		                                });
		                            }

		                            @Override
		                            public void cancel() {
		                                resetNode(context);
		                            }                                       
		                        });
		                        
		                        return;
		                    }
		                }
		            }
		            
		        }
		    }
		    
		}else if(courseObject instanceof generated.course.TrainingApplication){
		    
		    generated.course.TrainingApplication trainingApp = (generated.course.TrainingApplication)courseObject;
		    if(trainingApp.getDkfRef() != null){
		        //custom delete logic for training app dkf
		        
		        final DkfRef dkfRef = trainingApp.getDkfRef();
		        if(dkfRef.getFile() != null && !dkfRef.getFile().isEmpty()){
		            
                    OkayCancelDialog.show("Delete " + nodeHeader + " course object?", 
                            "Are you sure you want to <b>permanently</b> delete \"" + nodeName +"\"?"
                                    + "<br/></br><b>Note:</b> The real time assessment will also be deleted.</br></br>"+
                                    "If you are using the underyling real time assessment somewhere else in the course consider removing the reference</br>"+
                                    "to that real time assessment from this course object.  To remove the reference:</br>"+
                                    "<ol><li>Click the course object to open the editor</li>"+
                                    "<li>Click the 'Remove' button next to 'Real-Time Assessment'</li>" +
                                    "<li>Follow the instructions on the remove dialog to remove the reference but keep the real time assessment content.", 
                                    "Delete",
                                    new OkayCancelCallback() {

                        @Override
                        public void okay() {
                            
                            // Reset node position so it doesn't hang by the trash can while the delete is in progress
                            if(nodeBeingDragged != null) {
                                double originalX = nodeBeingDragged.x();
                                double originalY = nodeBeingDragged.y();
                                D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");
                            }
                            
                            BsLoadingDialogBox.display("Deleting Course Object", "Please wait...");
                            String username = GatClientUtility.getUserName();
                            String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                            List<String> filesToDelete = new ArrayList<String>();
                            final String filePath = GatClientUtility.getBaseCourseFolderPath() + "/" + dkfRef.getFile();
                            filesToDelete.add(filePath);
                            
                            DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                @Override
                                public void onFailure(Throwable error) {
                                    BsLoadingDialogBox.remove();
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                            "Failed to delete the file.", 
                                            error.getMessage(), 
                                            DetailedException.getFullStackTrace(error));
                                    dialog.setDialogTitle("Deletion Failed");
                                    dialog.center();
                                }

                                @Override
                                public void onSuccess(GatServiceResult result) {
                                    BsLoadingDialogBox.remove();
                                    if(result.isSuccess()){
                                        permanentlyDeleteNode(node, context);
                                        
                                    } else {
                                        resetNode(context);
                                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                                        dialog.setTitle("Error");
                                        dialog.center();
                                    }
                                }
                                
                            });
                        }

                        @Override
                        public void cancel() {
                            resetNode(context);
                        }                                       
                    });
                    
                    return;
		        }
		    }
		}
		
		//default delete course object logic that just deletes the course object no underlying references/files/content 
		OkayCancelDialog.show("Delete " + nodeHeader + " course object?", 
				"Are you sure you want to <b>permanently</b> delete \"" + nodeName +"\"?"+
                        "<br/></br><b>Note:</b> This will not delete any underlying media/content/files referenced by this course object.</br></br>", 
						"Delete",
						new OkayCancelCallback() {

			@Override
			public void okay() {
				permanentlyDeleteNode(node, context);
			}

			@Override
			public void cancel() {
				resetNode(context);
			}										
		});
	}
	
	/**
	 * Permanently deletes a node from the course tree
	 * 
	 * @param node The node to delete
	 * @param context The context from a drag event. Can be null if there is no associated drag event.
	 */
	private void permanentlyDeleteNode(TreeNode node, Element context) {

		if(node.parent() != null){
			
			//if this node has a parent, link that parent to this node's children
			node.parent().children().splice(0, 1);
			((TreeNode) node.parent()).setChildren(node.getChildren());
		}
		
		//remove any validation information for this node being deleted
		String nodeName = getNodeBody(node);
		courseValidationResultsMap.remove(nodeName);
		
		update((TreeNode) node.parent());
		deleteNodeFunction.apply(context, null, -1);									
		nodeBeingDragged = null;

	}
	
	/**
	 * Resets the node to its original location in the course tree.
	 * 
	 * @param context The context from a drag event.
	 */
	private void resetNode(Element context) {
		if(nodeBeingDragged != null) {
			//reset the node to its original location when dragging is complete
			double originalX = nodeBeingDragged.x();
			double originalY = nodeBeingDragged.y();
			nodeBeingDragged = null;

			D3.select(context).attr("transform", "translate(" + originalX + "," + originalY + ")");

			//re-enable mouse events on the dragged node
			D3.select(context).attr("pointer-events", (String) null);
		}
	}
	
	/**
	 * Updates the tree graphics.
	 * 
	 * @param source The tree node to update
	 */
	private void update(final TreeNode source) {
	    
	    if(root == null){
	        logger.fine("Skipped updating nodes for empty course tree.");
	        return;
	    }
	    
		logger.fine("Beginning update()");
		
		Array<Node> nodes = tree.nodes(root).reverse();
		Array<TreeLink> treeLinks = root.getLinks();
		Array<Link> links = tree.links(nodes);
		final HashMap<Integer, TreeNode> nodeLinks = new HashMap<Integer, TreeNode>();
		
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
		
		
		//calculate how long a row of nodes should be. This will be used to determine nodes' vertical positioning.
		double nodeRowWidth = 0;		
		boolean isWithinViewableArea = true;
		
		while(isWithinViewableArea){
			
			double nextWidth = nodeRowWidth + TREE_NODE_WIDTH + HORIZONTAL_MARGIN_BETWEEN_NODES;
			
			if(nextWidth < (CourseTree.this.getOffsetWidth()/zoom.scale())){
				nodeRowWidth = nextWidth;
				
			} else {
				isWithinViewableArea = false;
			}
		}
		
		final double NODE_ROW_WIDTH = nodeRowWidth;
		
		// Normalize for fixed depth
		logger.fine("3: Setting tree node depth & width properties.");
		nodes.forEach(new ForEachCallback<Void>() {
			@Override
			public Void forEach(Object thisArg, Value element, int index,
					Array<?> array) {
				
				TreeNode treeNode = element.<TreeNode> as();	
				
				//set the node's width
				treeNode.setWidth(TREE_NODE_WIDTH);
				
				//get this node's potential x position
				double xPosition = treeNode.depth() * treeNode.getWidth() + treeNode.depth() * HORIZONTAL_MARGIN_BETWEEN_NODES;
				
				//if the potential x position exceeds the viewable space, we need to wrap it to the next line
				int numHorizontalWraps = 0;
				
				if(NODE_ROW_WIDTH > 0){
					
					while(xPosition > NODE_ROW_WIDTH - zoomedXTranslation){	
						
						double nextX = xPosition - NODE_ROW_WIDTH;
						
						if(nextX >= 0){			
							
							xPosition = nextX;
							numHorizontalWraps++;
						
						} else {
							
							//don't let the x position go below 0, since it could go beyond the left bound of the viewable area
							break; 
						}
					}
					
				} else {
					
					//if there isn't enough room for even one node per row, don't even bother changing the x position, aside from the
					//initial offset
					xPosition = 0;
				}
				
				//apply the x position to the node
				treeNode.setAttr("x", xPosition);
				
				//get this node's potential y position
				double yPosition;
				
				if(NODE_ROW_WIDTH > 0){					
					yPosition = VERTICAL_MARGIN_BETWEEN_NODES * numHorizontalWraps;
					
				} else {
					
					//if there isn't enough room for even one node per row, then just put each node on its own row
					yPosition = VERTICAL_MARGIN_BETWEEN_NODES * treeNode.depth();
				}
				
				//apply the y position to the node
				treeNode.setAttr("y", yPosition);
					
				// Add visible tree nodes
				nodeLinks.put(treeNode.id(), treeNode);
				
				if(treeNode.depth() == array.length() - 1){
					
					//after the last node has been added, calculate the height of the tree and set the SVG container to
					//that height (plus padding) so that it can be scrolled properly 
					mainSvg.attr("height", ((int) ((yPosition + TREE_NODE_HEIGHT/2) * zoom.scale() + zoomedYTranslation + 50)) + "px");
				}
				
				return null;
			}
		});
		
		// Enter any new nodes at the parent's previous position
		logger.fine("4: Translating new node positions.");
		Selection nodeEnter = node
				.enter()
				.append("g")
				.attr("class", CSS.node())
				.call(dragBehavior) // enable dragging
				.on("click", new TreeNodeClicked())
				.on("mousedown", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						//need to prevent mouse down events from bubbling so we don't dragBehavior the whole tree when each node is dragged
						D3.event().stopPropagation();
						
						return null;
					}
				}).on("mouseover", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						TreeNode node = d.<TreeNode>as();
						
						if(nodeBeingDragged != null){
							
							if(nodeBeingDragged.equals(node)){		
								
								//nodes shouldn't handle mouseover events while being dragged
								D3.event().preventDefault();
								
							} else {
								
								if(!node.equals(selectedNode)){
									setSelectedNode(node, true);
								}
							}
						}
						
						return null;
					}
				})
				.on("mouseleave", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						TreeNode node = d.<TreeNode>as();
						
						if(nodeBeingDragged != null){
							
							if(nodeBeingDragged.equals(node)){		
								
								//nodes shouldn't handle mousleave events while being dragged
								D3.event().preventDefault();
								
							} else {
								
								if(node.equals(selectedNode)){
									setSelectedNode(null, true);
								}
							}
						}
						
						return null;
					}
				})
				.on("mouseup", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						TreeNode node = d.<TreeNode>as();
						
						if(nodeBeingDragged != null){
							
							if(nodeBeingDragged.equals(node)){		
								
								//nodes shouldn't handle mouseup events while being dragged
								D3.event().preventDefault();
								
							} else {
								
								//if this node isn't a course start node, drop the dragged node in its place
								nodeDropFunction.apply(context, d, index);
							}
						}
						
						return null;
					}
				}).on("dragenter", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						if(nodeDragEnterFunction != null){
							nodeDragEnterFunction.apply(context, d, index);
						}
						
						return null;
					}
				}).on("dragover", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						D3.event().stopPropagation();
						
						svgBorder.attr("class", "");
						
						if(nodeDragOverFunction != null){
							nodeDragOverFunction.apply(context, d, index);
						}
						
						return null;
					}
				}).on("drop", new DatumFunction<Void>() {
					
					@Override
					public Void apply(Element context, Value d, int index) {
						
						if(nodeDropFunction != null){
							nodeDropFunction.apply(context, d, index);
						}
						
						return null;
					}
				});
		
		// Create a rectangle wrapper for new nodes
		logger.fine("5: Creating rectangle wrappers for new nodes.");
		nodeEnter.append("rect")
			.attr("class", nodeRectUpdator.getAttributeValue("class"))
			.attr("width", nodeRectUpdator.getAttributeValue("width"))
			.attr("height", TREE_NODE_HEIGHT)
			.attr("rx", nodeRectUpdator.getAttributeValue("rx")) //border radius
			.attr("x", nodeRectUpdator.getAttributeValue("x"))
			.attr("y", -(TREE_NODE_HEIGHT/2))
			.append("title").text(new DatumFunction<String>() {
				
				@Override
				public String apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					Serializable courseTransition = nodeToCourseElement.get(node);
					boolean disabled = CourseElementUtil.isTransitionDisabled(courseTransition);
					
					if(type != null){
					    StringBuilder strBuilder = new StringBuilder();
						if(TreeNodeEnum.TRANSITION.equals(type)){
                            strBuilder.append(disabled ? "<DISABLED> " : "");
						    strBuilder.append(GatClientUtility.isReadOnly() ? "Click to view." : "Click to edit. Drag to move.");
							return strBuilder.toString();
							
						} else if(TreeNodeEnum.COURSE_END.equals(type)){
							return GatClientUtility.isReadOnly() ? "" : "Drag course objects here to move them to the end of the course.";
						}
					}
					
					return null;
				}
				
			});
		
		// Create the new node's header text
		logger.fine("6: Creating text graphics for new nodes.");
		
		Selection headerText = nodeEnter.append("svg:text")
			.attr("class", new DatumFunction<String>() {

				@Override
				public String apply(Element context, Value d, int index) {
					
					StringBuilder sb = new StringBuilder(CSS.header());
					
					TreeNode node = d.<TreeNode> as();
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					if(type != null){
						
						if(TreeNodeEnum.COURSE_END.equals(type) ){
							
							sb.append(" ").append(CSS.endNodeHeader());		
						}
					}
					
					return sb.toString();
				}
			})
			.attr("width", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getWidth();
					return width.intValue() - 45;
				}
			})
			.attr("y", new DatumFunction<Integer>() {

				@Override
				public Integer apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					if(type != null){
						
						if(TreeNodeEnum.COURSE_END.equals(type) ){								
							return -TREE_NODE_HEIGHT/2 + 85;
						}	
					}
					
					return -TREE_NODE_HEIGHT/2 + 25;
				}
			})
			.attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					
					TreeNode node = d.<TreeNode> as();
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					if(type != null){
					    
					    //Checks to see if the given node is an end node, who's text's x placement is different than all other nodes
					    if(TreeNodeEnum.COURSE_END.equals(type) ){								
							return -30;
						}	
					}
					
					Double width = d.<TreeNode> as().getWidth();
					return 35 - (width.intValue() / 2);
				}
			})
			.attr("pointer-events", "none") //only the node itself should interact with the mouse, not the elements inside it
	        .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						return getNodeHeader(d.<TreeNode>as());
					}
		        })
		;
		
		formatText(headerText, 200, 1);

		// Create the new node's text
		logger.fine("6: Creating text graphics for new nodes.");
		
		Selection text = nodeEnter.append("svg:text")
			.attr("class", CSS.text())
			.attr("width", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getWidth();
					return width.intValue() - 45;
				}
			})
			.attr("y", -TREE_NODE_HEIGHT/2 + 60)
			.attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getWidth();
					return 10 - (width.intValue() / 2);
				}
			})
			.attr("pointer-events", "none") //only the node itself should interact with the mouse, not the elements inside it
	        .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						return getNodeBody(d.<TreeNode>as());
					}
		        })
		;
		
		formatText(text, 200, 2);
		
		// Create the new node's icon
		logger.fine("7a: Creating icons for new nodes.");
		nodeEnter.append("svg:image")
			.attr("class", CSS.icon())
		    .attr("y", -TREE_NODE_HEIGHT/2 + 5)
		    .attr("x", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getWidth();
					return 10 - (width.intValue() / 2);
				}
			})
			.attr("width", 24)
			.attr("height", 24)
			.attr("pointer-events", "none") //only the node itself should interact with the mouse, not the elements inside it
		    .attr("xlink:href" , nodeImageUpdator.getAttributeValue("xlink:href"));
		
		// Create the new node's validation failed icon
        logger.fine("7b: Creating validation failed icons for new nodes.");
        nodeEnter.append("svg:image")
            .attr("class", nodeImageUpdator.getAttributeValue("xlink:href-validation-css"))
            .attr("y", -TREE_NODE_HEIGHT/2 + 5)
            .attr("x", new DatumFunction<Integer>() {
                @Override
                public Integer apply(Element context, Value d, int index) {
                    Double width = d.<TreeNode> as().getWidth();
                    return (width.intValue() / 2) - 10;
                }
            })
            .attr("width", 24)
            .attr("height", 24)
            .attr("pointer-events", "visibleFill") //this element should only interact with the mouse when it is visible
            .attr("xlink:href", "images/Alert-32.png")
            .on("click", new ValidationFailedButtonClicked())
            .on("mousedown", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    //need to prevent mouse down events from bubbling so we don't perform click events for the whole tree
                    D3.event().stopPropagation();
                    
                    return null;
                }
            })
            .append("title").text("Click to display the course validation errors.");
		
		// Create the new node's separator line		
		nodeEnter.append("svg:line")
			.attr("class", new DatumFunction<String>() {

				@Override
				public String apply(Element context, Value d, int index) {
					
					StringBuilder sb = new StringBuilder(CSS.separator());
					
					TreeNode node = d.<TreeNode> as();
					
					TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
					
					if(type != null){
						
						if(TreeNodeEnum.COURSE_END.equals(type)){							
							sb.append(" ").append(CSS.endNodeSeparator());		
						}
					}
					
					return sb.toString();
				}
			})			
			.attr("y1", -TREE_NODE_HEIGHT/2 + 35)
			.attr("y2", -TREE_NODE_HEIGHT/2 + 35)
			.attr("x1", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getWidth();
					return 10 - (width.intValue() / 2);
				}
			})
			.attr("x2", new DatumFunction<Integer>() {
				@Override
				public Integer apply(Element context, Value d, int index) {
					Double width = d.<TreeNode> as().getWidth();
					return 15 + (width.intValue() / 2);
				}
			})
			.attr("pointer-events", "none") //only the node itself should interact with the mouse, not the elements inside it
		;
		
		// Transition nodes to their new position
		logger.fine("8: Translating existing node positions.");
		Selection nodeUpdate = node
//				.duration(TRANSITION_ANIMATION_DURATION)
				.attr("transform", new DatumFunction<String>() {
					@Override
					public String apply(Element context, Value d, int index) {
						TreeNode data = d.<TreeNode> as();
						
						if(nodeBeingDragged != null){
									
							if(data.equals(nodeBeingDragged)){
								
								// If a node is being dragged, don't update its position here, otherwise, 
								// the node will flicker whenever it is dragged into another node
								return D3.select(context).attr("transform");
							}
						}
						
						return "translate(" + data.x() + "," + data.y() + ")";
					}
				});

		// Transition exiting nodes to the parent's new position
		logger.fine("9: Translating exiting node positions.");
		node.exit()
				.attr("transform", new DatumFunction<String>() {
					@Override
					public String apply(Element context, Value d, int index) {
						return "translate(" + source.x() + "," + source.y()
								+ ")";
					}
				}).remove();
		
		// Update the rectangle wrappers for existing nodes
		logger.fine("10: Updating existing rectangle graphics.");

		nodeUpdate.select("rect")
			.attr("class", nodeRectUpdator.getAttributeValue("class"));
		
		// Update the node text
		logger.fine("11: Updating existing node text.");
		
		Selection headerUpdate = node.select("text." + CSS.header())
	        .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						return getNodeHeader(d.<TreeNode>as());
					}
		        })
		;	
		
		formatText(headerUpdate, 200, 1);
		
		// Update the node text
		logger.fine("11: Updating existing node text.");
		
		Selection textUpdate = node.select("text." + CSS.text())
	        .text(new DatumFunction<String>() {
				@Override
				public String apply(Element context, Value d, int index) {
						return getNodeBody(d.<TreeNode>as());
					}
		        })
		;
		
		formatText(textUpdate, 200, 2);
		
        nodeUpdate.select("image." + CSS.icon()).attr("xlink:href", nodeImageUpdator.getAttributeValue("xlink:href"));

        nodeUpdate.select("image." + CSS.failedValidationIcon()).attr("class",
                nodeImageUpdator.getAttributeValue("xlink:href-validation-css"));

        nodeUpdate.select("image." + CSS.failedValidationIcon_hidden()).attr("class",
                nodeImageUpdator.getAttributeValue("xlink:href-validation-css"));

		// Remove any previously drawn links
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
					com.github.gwtd3.api.layout.Node sourceNode = link.source();
					com.github.gwtd3.api.layout.Node targetNode = link.target();

					if(sourceNode != null && targetNode != null) {	
												Array<Coords> points = Array.create();
						
						
						if(targetNode.x() > sourceNode.x() && targetNode.y() == sourceNode.y()){
							
							double sourceXOffset = TREE_NODE_WIDTH/2;
							double targetXOffset = -TREE_NODE_WIDTH/2;
							
							points.push(Coords.create(
									sourceNode.x() + sourceXOffset, 
									sourceNode.y()
							));
							
							points.push(Coords.create(
									targetNode.x() + targetXOffset, 
									targetNode.y()
							));
							
						} else if(targetNode.y() > sourceNode.y()){
							
							points.push(Coords.create(
									sourceNode.x(), 
									sourceNode.y() + TREE_NODE_HEIGHT/2
							));
							
							points.push(Coords.create(
									sourceNode.x(), 
									sourceNode.y() + VERTICAL_MARGIN_BETWEEN_NODES/2
							));
							
							points.push(Coords.create(
									targetNode.x(), 
									targetNode.y() + -VERTICAL_MARGIN_BETWEEN_NODES/2
							));
							
							points.push(Coords.create(
									targetNode.x(), 
									targetNode.y() + -TREE_NODE_HEIGHT/2
							));
						}

						return diagonal.generate(points);
						
					} else {			
						
//						logger.severe("Unfinished link: [" + link.getSource() + ", "+ link.getTarget() +"]");
						
						if(sourceNode == null){
							logger.severe("Source is null");
						}
						
						if(targetNode == null){
							logger.severe("Target is null");
						}
						
						return "";
					}
				}
			});
		
		// Update the node positions
		logger.fine("16: Updating node positions");
		nodes.forEach(new ForEachCallback<Void>() {
			@Override
			public Void forEach(Object thisArg, Value element, int index,
					Array<?> array) {
				TreeNode data = element.<TreeNode> as();
				data.x0(data.x());
				data.y0(data.y());
				return null;
			}
		});
		
		// Apply context menu handler to any new nodes
		logger.fine("17: Applying context menu handler to new nodes.");
		svg.selectAll("g." + CSS.node() + ":not([contextmenu=nodeOptions])").on("contextmenu", new DatumFunction<Void>() {

			@Override
			public Void apply(Element context, Value d, int index) {
				
				D3.event().preventDefault();
				
				if(nodeContextFunction != null){
					nodeContextFunction.apply(context, d, index);
				}
				
				return null;
			}

		});

		logger.fine("Completed update()");
	}
	
	/**
	 * Creates a course tree.
	 * 
	 * @param course A course object from which to creat the tree
	 * @return the TreeNode at the root of the course tree.
	 */
	private TreeNode makeTree(Course course){		

		nodeToCourseElement.clear();
		
		//Nick - assigning IDs here might be useful for handling animations on specific transitions later, but isn't neccesary right now
//		int idIncrementor = 0;
		
		TreeNode root = null;
		TreeNode lastNode = null;
		
		for(Serializable transition : course.getTransitions().getTransitionType()){
			
			TreeNode transNode = TreeNode.createObject().cast();		
			transNode.setType(TreeNodeEnum.TRANSITION.toString());

			nodeToCourseElement.put(transNode, transition);
			
			Array<TreeLink> transLinks = Array.create();		
			transNode.setLinks(transLinks);
			
			if(root == null){
				root = transNode;
			}
			
			if(lastNode != null){
			
				if(lastNode.children() == null){
					
					Array<TreeNode> children = Array.create();
					lastNode.setChildren(children);
				}
	
				lastNode.children().push(transNode);
			}
			
			lastNode = transNode;
		}
		
		if (!GatClientUtility.isRtaLessonLevel()) {
            
            //if GIFT's lesson level is not set to RTA, add an end node so the author can drag course objects to the end of the tree
    		TreeNode end = TreeNode.createObject().cast();
    		end.setType(TreeNodeEnum.COURSE_END.name());
    		
    		if(root == null){
    			root = end;
    		}
    		
    		Array<TreeLink> endLinks = Array.create();		
    		root.setLinks(endLinks);
    		
    		if(lastNode != null){
    		
    			if(lastNode.children() == null){
    				
    				Array<TreeNode> children = Array.create();
    				lastNode.setChildren(children);
    			}
    	
    			lastNode.children().push(end);
    		}
        
        } else {
            
            //if GIFT's lesson level is set to RTA, hide the trash icon and the SVG around it, since course objects shouldn't be deleted
            deleteSvg.attr("display", "none");
        }
        
		return root;
	}
		
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
	 * Automatically splits an SVG text element into tspan elements displayed on new lines.
	 * 
	 * @param selection The text selection to format.
	 * @param maxWidth The width at which to wrap lines
	 */
	private native void formatText(Selection selection, double maxWidth) /*-{
	
		function wrap(text) {
			text.each(function() {
				
				//get the SVG text element where the text for the selected data set is
	    		var text = $wnd.d3.select(this),
	    		
	    		//split the text into words based on whitespace
	        	words = text.text().split(/\s+/).reverse(),
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
		      				      		
		      		if (tspan.node().getComputedTextLength() > maxWidth) {	 
		      			
		      			//if the current line exceeds the maximum width for this text element, we need to move it to the next line				      				      			
		        		line.pop();
		        		
		        		//move the current word to the next line
		        		tspan.text(line.join(" "));
		        		line = [word + " "];
		        		tspan = text.append("tspan").attr("x", x).attr("y", y).attr("dy", ++lineNumber * lineHeight +  "em").text(word);
	      			}
	    		}
	    		
	    	});
  		};
  		
  		selection.call(wrap);
  		
	}-*/;
	
	/**
	 * Automatically splits an SVG text element into tspan elements displayed on new lines and appends
	 * an ellipsis if the text exceeds the given height
	 * 
	 * @param selection The text selection to format.
	 * @param maxWidth The width at which to wrap lines
	 * @param maxHeight The height at which to add an ellipsis
	 */
	private native void formatText(Selection selection, double maxWidth, double maxLines) /*-{
	
		function wrap(text) {
			text.each(function() {
				
				//get the SVG text element where the text for the selected data set is
	    		var text = $wnd.d3.select(this),
	    		
	    		//split the text into words based on whitespace
	        	words = text.text().split(/\s+/).reverse(),
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
		      		
		      		if (tspan.node().getComputedTextLength() > maxWidth) {
		      				   			
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
	    		
	    	});
  		};
  		
  		selection.call(wrap);
  		
	}-*/;
		
	/**
	 * A DatumFunction to manipulate TreeNodes when they are clicked.
	 */
	private class TreeNodeClicked implements DatumFunction<Void> {
		@Override
		public Void apply(Element context, Value d, int index) {
			
			TreeNode node = d.<TreeNode> as();
			
			TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
			
			if(type != null){
				
				if(TreeNodeEnum.TRANSITION.equals(type)){	
					
					setSelectedNode(node, true);
					
					if(nodeClickFunction != null){
						nodeClickFunction.apply(context, d, index);
					}
					
					scrollToSelectedNode();
				}
			
			} else {
				return null;
			}
			
			return null;
		}
	}
	
	/**
     * A DatumFunction to manipulate the TreeNode's failed validation button when they are clicked.
     */
    private class ValidationFailedButtonClicked implements DatumFunction<Void> {
        @Override
        public Void apply(Element context, Value d, int index) {
            
            TreeNode node = d.<TreeNode> as();
            
            TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
            
            if(type != null){
                
                if(TreeNodeEnum.TRANSITION.equals(type)){   

                    if (courseValidationResultsMap == null) {
                        logger.severe("Validation results are null when trying to display them to the user. If the user can click on the failed validation icon to get to this point and they are null, then something went wrong.");
                        return null;
                    }
                    
                    // find course object's validation errors
                    Serializable transition = nodeToCourseElement.get(node);
                    String transitionName = CourseElementUtil.getTransitionName(transition);
                    CourseObjectValidationResults thisCourseResults = courseValidationResultsMap.get(transitionName);

                    if (thisCourseResults != null) {                
                        CourseValidationResults courseResults = new CourseValidationResults();
                        courseResults.getCourseObjectResults().add(thisCourseResults);
                        FileValidationDialog dialog = new FileValidationDialog(null, "There are validation issues with this course object.", courseResults, true);
                        dialog.setText("Validation Errors for " + transitionName);
                        dialog.center();
                    }
                }
            
            } else {
                return null;
            }
            
            return null;
        }
    }
	
	/**
	 * Gets the header text for the given node
	 * 
	 * @param node the node to get the header text for
	 * @return the header text
	 */
	private String getNodeHeader(TreeNode node){
		
		TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
		
		if(type != null){
			
			if(TreeNodeEnum.TRANSITION.equals(type)){
				
				Serializable transition = nodeToCourseElement.get(node);
				
				return CourseElementUtil.getTypeDisplayName(transition);
				
			} else {
				return type.getDisplayName();
			}
		
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the body text for the given node
	 * 
	 * @param node the node to get the body text for
	 * @return the body text
	 */
	private String getNodeBody(TreeNode node){
		
		TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
		
		if(type != null){
			
			if(TreeNodeEnum.TRANSITION.equals(type)){
				
				Serializable transition = nodeToCourseElement.get(node);
				
				//if there is white space at the beginning of the transition name the 
				//node will not show the name at all.
				try {
					String name = CourseElementUtil.getTransitionName(transition);
					return name == null ? null : name.trim();
					
				} catch (IllegalArgumentException e) {
					logger.log(Level.SEVERE, "There was a problem getting the body text for the course object.", e);
					return null;
				}
				
			} else {
				return null;
			}
		
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.logical.shared.HasSelectionHandlers#addSelectionHandler(com.google.gwt.event.logical.shared.SelectionHandler)
	 */
	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<TreeNode> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}
	
	/**
	 * Gets the tree node that has been selected, if such a tree node exists
	 * 
	 * @return the selected tree node, or null, if no tree nodes have been selected
	 */
	public TreeNode getSelectedNode(){
		return selectedNode;
	}
	
	/**
	 * Selects the given tree node and, if specified, fires an event to indicate its selection
	 * 
	 * @param node the node to select
	 * @param fireEvents whether or not to fire an event indicating the selection
	 */
	public void setSelectedNode(TreeNode node, boolean fireEvents){
		
		//select the node that was clicked
		selectedNode = node;
		
		//update the styling on all existing nodes to indicate which node was selected
		svg.selectAll("g." + CSS.node()).selectAll("rect").attr("class", nodeRectUpdator.getAttributeValue("class"));
		
		if(fireEvents){
			
			//notify listeners when the selection is updated
			SelectionEvent.fire(CourseTree.this, selectedNode);
		}
	}
	
	/**
	 * Scrolls the course tree so that the selected node is visible
	 */
	public void scrollToSelectedNode(){
		
		if(selectedNode != null){
			
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				
				@Override
				public void execute() {
					svg.selectAll("g." + CSS.node()).selectAll("rect").each(new DatumFunction<Void>() {
						
						@Override
						public Void apply(Element context, Value d, int index) {
							
							TreeNode node = d.as();
							
							if(node != null && node.equals(selectedNode)){
								
								int scrollPos = scroller.getVerticalScrollPosition();
								int nodeTop = getSVGNodeTop(context);
								int nodeBottom = getSVGNodeBottom(context);
								
								if(nodeTop < scroller.getElement().getAbsoluteTop()){
									
									scrollPos = scrollPos - 50 - (scroller.getElement().getAbsoluteTop() - nodeTop);
																	
									scrollPos = Math.max(
											scroller.getMinimumVerticalScrollPosition(), 
											scrollPos
									);
									
									scroller.setVerticalScrollPosition(scrollPos);
									
								} else if(nodeBottom > scroller.getElement().getAbsoluteBottom()){
									
									scrollPos = scrollPos + 50 + (nodeBottom - scroller.getElement().getAbsoluteBottom());
									
									scrollPos = Math.min(
											scroller.getMaximumVerticalScrollPosition(), 
											scrollPos
									);
									
									scroller.setVerticalScrollPosition(scrollPos);
								}
							}
							
							return null;
						}
	
					});
				}
			});
		}
	}
	
	/**
	 * Gets the pixel position of the given SVG element's topmost bound with respect to the current viewport.
	 * 
	 * @param element the target SVG element
	 * @return the position of the element's topmost bound
	 */
	private native  int getSVGNodeTop(Element element)/*-{
		return element.getBoundingClientRect().top;
	}-*/;
	
	/**
	 * Gets the pixel position of the given SVG element's bottom-most bound with respect to the current viewport.
	 * 
	 * @param element the target SVG element
	 * @return the position of the element's bottom-most bound
	 */
	private native  int getSVGNodeBottom(Element element)/*-{
		return element.getBoundingClientRect().bottom;
	}-*/;
	
	/**
	 * Set whether or not the course is read-only
	 * 
	 * @param readOnly True if the course is in read-only mode, false otherwise.
	 */
	public void setReadOnly(boolean readOnly) {
	    logger.info("setReadonly = " + readOnly);
		this.readOnly = readOnly;
		if(readOnly) {
			trashToolTip.text("Unavailable in Read-Only mode.");
			trashIconSvg.attr("xlink:href", GatClientBundle.INSTANCE.trashcan_disabled().getSafeUri().asString());
		} else {
		    trashToolTip.text("Drag a course object here to permanently delete.");
            trashIconSvg.attr("xlink:href", GatClientBundle.INSTANCE.trashcan().getSafeUri().asString());
		}
	}
	
	/**
	 * Gets the course object associated with the given node, assuming the node has one
	 * 
	 * @param node the node for which to get the course object
	 * @return the course object associated with the node, or null, if the node is not associated with a course object
	 */
	public Serializable getCourseObject(TreeNode node){
		return nodeToCourseElement.get(node);
	}
	
	/**
	 * Sets the function used to handle when a tree node is clicked
	 * 
	 * @param function the function to use
	 */
	public void setNodeClickFunction(DatumFunction<Void> function){
		this.nodeClickFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node's context menu is displayed
	 * 
	 * @param function the function to use
	 */
	public void setNodeContextFunction(DatumFunction<Void> function){
		this.nodeContextFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node is dragged
	 * 
	 * @param function the function to use
	 */
	public void setNodeDragStartFunction(DatumFunction<Void> function){
		this.nodeDragStartFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node has a DOM element dragged into it
	 * 
	 * @param function the function to use
	 */
	public void setNodeDragEnterFunction(DatumFunction<Void> function){
		this.nodeDragEnterFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node has a DOM element dragged into it
	 * 
	 * @param function the function to use
	 */
	public void setNodeDragOverFunction(DatumFunction<Void> function){
		this.nodeDragOverFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node has a DOM element dropped on it
	 * 
	 * @param function the function to use
	 */
	public void setNodeDropFunction(DatumFunction<Void> function){
		this.nodeDropFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node has a DOM element dragged into it
	 * 
	 * @param function the function to use
	 */
	public void setSVGDragOverFunction(DatumFunction<Void> function){
		this.svgDragOverFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node has a DOM element dropped on it
	 * 
	 * @param function the function to use
	 */
	public void setSVGDropFunction(DatumFunction<Void> function){
		this.svgDropFunction = function;
	}
	
	/**
	 * Sets the function used to handle when a tree node has been deleted.
	 * 
	 * @param function the function to use
	 */
	public void setDeleteNodeFunction(DatumFunction<Void> function){
		this.deleteNodeFunction = function;
	}

	/**
	 * Sets the validation results used to determine which course objects have validation failures.
	 * 
	 * @param validationResults the validation results
	 */
    public void setValidationResults(ValidateFileResult validationResults) {
        // initialize map
        if (courseValidationResultsMap == null) {
            courseValidationResultsMap = new HashMap<String, CourseObjectValidationResults>();
        } else {
            courseValidationResultsMap.clear();
        }
        
        // no results to process, exit method
        if (validationResults == null || validationResults.getCourseValidationResults() == null || validationResults.getCourseValidationResults().getCourseObjectResults() == null) {
            return;
        }
        
        // find course object's validation errors
        for (CourseObjectValidationResults courseResults : validationResults.getCourseValidationResults().getCourseObjectResults()) {
            if (courseResults.getCourseObjectIcon() == null) {
                Serializable courseTransition = findCourseTransitionByName(courseResults.getCourseObjectName());
                String icon = courseTransition == null ? null : CourseElementUtil.getTypeIcon(courseTransition);
                courseResults.setCourseObjectIcon(icon);
            }
            courseValidationResultsMap.put(courseResults.getCourseObjectName(), courseResults);
        }
    }
    
    /**
     * Finds the course transition with the transition name
     * 
     * @param transitionName
     *            the name to search
     * @return the course transition element
     */
    private Serializable findCourseTransitionByName(String transitionName) {
        Serializable transition = null;
        
        for (Serializable courseElement : nodeToCourseElement.values()) {
            if (transitionName.equals(CourseElementUtil.getTransitionName(courseElement))){
                transition = courseElement;
                break;
            }
        }
        
        return transition;
    }

    /**
	 * Notifies this widget that a course object outside the SVG context is being dragged so
	 * that this widget can automatically adjust its scroll position based on the position
	 * of the object being dragged.
	 */
	public void startDragScrolling(){
		
		//create a timer to update the scroller whenever the node is dragged near the top
		//or bottom of the scrollable area
		dragScrollAnimationTimer = new Timer() {
			
			@Override
			public void run() {
				
				if(mouseDragY != null){
				
					int scrollPos = scroller.getVerticalScrollPosition();
					
					if(mouseDragY < DRAG_SCROLL_THRESHOLD
							&& scrollPos > scroller.getMinimumVerticalScrollPosition()){
						
						int dY = mouseDragY - DRAG_SCROLL_THRESHOLD;
						
						//scroll up when the top threshold has been reached
						scroller.setVerticalScrollPosition(Math.max(
								scroller.getMinimumVerticalScrollPosition(), 
								scrollPos + dY
						));											
					
					} else if(mouseDragY > scroller.getOffsetHeight() - DRAG_SCROLL_THRESHOLD
							&& scrollPos < scroller.getMaximumVerticalScrollPosition()){
						
						int dY = mouseDragY - (scroller.getOffsetHeight() - DRAG_SCROLL_THRESHOLD);
						
						//scroll down when the bottom threshold has been reached
						scroller.setVerticalScrollPosition(Math.min(
								scroller.getMaximumVerticalScrollPosition(), 
								scrollPos + dY
						));									
					}
				}
			}							
		};
		
		dragScrollAnimationTimer.scheduleRepeating(50);
	}
	
	/**
	 * Notifies this widget that a course object outside the SVG context is no longer
	 * being dragged, so that this widget no longer adjusts its scroll position based 
	 * on the position of the object being dragged.
	 */
	public void endDragScrolling(){
		
		if(dragScrollAnimationTimer != null){
			
			//cancel and reset the scrolling animation timer since we're no longer dragging
			dragScrollAnimationTimer.cancel();
			dragScrollAnimationTimer = null;
		}
		
		mouseDragY = null;
	}

	@Override
	public void onResize() {
		
		if(root != null){
			
			//need to update the tree so it can wrap its nodes
			updateTree();
			
			scrollToSelectedNode();
		}
	}
	
	/**
	 * Selects the node in the course tree representing the given course object, if such a node exists.
	 * 
	 * @param courseObject the course object whose node should be selected
	 */
	public void selectCourseObject(Serializable courseObject){
		
		if(courseObject != null){
		
			for(TreeNode node : nodeToCourseElement.keySet()){
				
				if(courseObject.equals(nodeToCourseElement.get(node))){
					
					setSelectedNode(node, true);
					
					scrollToSelectedNode();
				}
			}
		}
	}
	
    /**
     * Checks if the course object has validation erorrs.
     * 
     * @param courseObject
     *            the course object
     * @return true if the course object failed validation
     */
    private boolean courseObjectFailedValidation(Serializable courseObject) {
        boolean validationFailed = false;
        if (courseObject != null && courseValidationResultsMap != null) {
            String name = CourseElementUtil.getTransitionName(courseObject);
            CourseObjectValidationResults courseResults = courseValidationResultsMap.get(name);
            if (courseResults != null && courseResults.getValidationResults() != null && courseResults.getValidationResults().getFirstError() != null) {
                validationFailed = true;
            }
        }

        return validationFailed;
    }
}