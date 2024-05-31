/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Breadcrumbs;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths.Path;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDoneEditingEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.authoredbranch.BranchSelectedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.AuthoredBranchTree;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseTree;

/**
 * A widget containing the main course tree that also creates and manages trees for viewing and interacting with authored branches.
 * 
 * @author nroberts
 */
public class TreeManager extends SimpleLayoutPanel {
	
	private static TreeManagerUiBinder uiBinder = GWT.create(TreeManagerUiBinder.class);

	interface TreeManagerUiBinder extends UiBinder<Widget, TreeManager> {
	}
	
	@UiField
	protected DockLayoutPanel mainDock;
	
	@UiField
	protected Widget breadcrumbsPanel;
	
	@UiField
	protected DeckLayoutPanel treeDeck;
	
	@UiField
	protected CourseTree courseTree;
	
	@UiField
	protected Breadcrumbs treeBreadcrumbs;
		
	@UiField
	protected AnchorListItem courseBreadcrumb;
	
	@UiField
	protected ManagedTooltip breadcrumbTooltip;
	
	/**	Used with showBreadcrumbTooltip and toggled to true when already shown once within same session */
	private boolean hasTooltipBeenShown = false;
	
	/**	Timer for the delay of showing the tooltip after the screen shifting animation to the authored branch tree */
	private static final int tooltipTimer = 500;
	
	/** The current authored branch that is being edited. Null if no branches are being edited. */
	private AuthoredBranch currentBranch;
	
	/** A map from each authored branch to its associated tree */
	private Map<AuthoredBranch, AuthoredBranchTree> branchToTree = new HashMap<>();
	
	/** A map from each authored branch to the breadcrumb used to access it's tree*/
	private Map<AuthoredBranch, AnchorListItem> branchToBreadcrumb  = new HashMap<>();
	
	/**
	 * Creates a new tree manager and initializes its UI elements
	 */
	public TreeManager() {
		setWidget(uiBinder.createAndBindUi(this));
		
		courseBreadcrumb.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
			    // fire the branch exit event for CoursePresenter to handle updating the editor tabs and tree UI
			    SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent(null));
			}
		});
		
		//always show the base course tree first
		treeDeck.showWidget(courseTree);
		
		//always hide the breacrumbs by default
		mainDock.setWidgetHidden(breadcrumbsPanel, true);
		
	}
	
	/**
	 * Gets the tree representing the base course flow
	 * 
	 * @return the base course tree
	 */
	public CourseTree getBaseCourseTree(){
		return courseTree;
	}
	
	/**
	 * Creates and adds a new tree to represent the given branch and sets up the UI components needed to interact with it.
	 * 
	 * @param branch the branch to create a tree for
	 */
	public void addBranchTree(final AuthoredBranch branch){
		
		final AuthoredBranchTree tree = new AuthoredBranchTree();
		
		//need to attach tree to DOM before loading it or else Edge throws an error
		treeDeck.add(tree);
		
		tree.loadTree(branch);
        //update the cursor to indicate that course objects can be dragged into course tree nodes
		tree.setSVGDragOverFunction(new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                //by default, the browser prevents dropping elements into other elements, so we need to disable this
                D3.event().preventDefault();
                
                return null;
            }
        });
		
		branchToTree.put(branch, tree);
		
		AnchorListItem breadcrumb = new AnchorListItem(branch.getTransitionName());
		breadcrumb.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
			    // fire the branch exit event for CoursePresenter to handle updating the editor tabs and tree UI
                SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent(branch));
			}
		});
						
		treeBreadcrumbs.add(breadcrumb);
				
		branchToBreadcrumb.put(branch, breadcrumb);		
	}
	
	/**
	 * Shows the current branch tree
	 */
	public void showCurrentBranchTree() {
	    if (currentBranch != null) {
	        showBranchTree(currentBranch);
	    }
	}
	
	/**
	 * Shows the tree corresponding to the given branch. If the branch does not yet have a tree associated with it, then a
	 * new one is created and added to this widget.
	 * 
	 * @param branch the branch to show a tree for.  Null is used to show the parent tree and deselect the current authored branch course object
	 * in the parent tree.
	 */
	public void showBranchTree(AuthoredBranch branch){
		
		if (branch != null) {
		    currentBranch = branch;
			
			if (!branchToTree.containsKey(branch)) {
				
				addBranchTree(branch);
				
				if (treeBreadcrumbs.getWidgetCount() == 2) {
					
					//show the breadcrumb panel
					mainDock.setWidgetHidden(breadcrumbsPanel, false);
					mainDock.animate(tooltipTimer);
					showBreadcrumbTooltip();
				}
				
			} else {
				branchToTree.get(branch).loadTree(branch);
			}
		
			treeDeck.showWidget(branchToTree.get(branch));
			
			if (branchToBreadcrumb.containsKey(branch)) {
		
				//remove all of the branches that are children of the branch being shown
				int removeBreadcrumbIndex = treeBreadcrumbs.getWidgetIndex(branchToBreadcrumb.get(branch)) + 1;
			
				while (removeBreadcrumbIndex < treeBreadcrumbs.getWidgetCount()) {
					
					Widget widget = treeBreadcrumbs.getWidget(removeBreadcrumbIndex);
					
					if (widget instanceof AnchorListItem) {
						
						AnchorListItem breadcrumb = (AnchorListItem) widget;
                        AuthoredBranch childBranchToRemove = null;

						for (AuthoredBranch childBranch : branchToBreadcrumb.keySet()){
							
							if (breadcrumb.equals(branchToBreadcrumb.get(childBranch))){
								
								if(branchToTree.containsKey(childBranch)){
									treeDeck.remove(branchToTree.get(childBranch));
								}
								
								branchToTree.remove(childBranch);
                                childBranchToRemove = childBranch;
								
                                break;
							}
						}
						
						branchToBreadcrumb.remove(childBranchToRemove);
					}
						
					treeBreadcrumbs.remove(widget);
				}
			}
		} else { // show the course tree
		    currentBranch = null;
		    
            //show the course tree when the main course breadcrumb is clicked
            treeDeck.showWidget(courseTree);
            
            // notify the course presenter when a course object is done being
            // edited so it can deselect it in the course tree - the authored branch course object will no longer be selected in the tree.
            SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDoneEditingEvent());
            
            //hide the breadcrumb panel
            mainDock.setWidgetHidden(breadcrumbsPanel, true);
            mainDock.animate(500);
                            
            //remove all the branches from this widget
            int removeBreadcrumbIndex = treeBreadcrumbs.getWidgetIndex(courseBreadcrumb) + 1;
            
            while(removeBreadcrumbIndex < treeBreadcrumbs.getWidgetCount()){
                
                Widget widget = treeBreadcrumbs.getWidget(removeBreadcrumbIndex);
                
                if(widget instanceof AnchorListItem){
                    
                    AnchorListItem breadcrumb = (AnchorListItem) widget;
                    
                    for(AuthoredBranch subBranch : branchToBreadcrumb.keySet()){
                        
                        if(breadcrumb.equals(branchToBreadcrumb.get(subBranch))){
                            
                            if(branchToTree.containsKey(subBranch)){
                                treeDeck.remove(branchToTree.get(subBranch));
                            }
                            
                            branchToTree.clear();
                            branchToBreadcrumb.clear();
                            
                        }
                        
                        break;
                    }
                }
                    
                treeBreadcrumbs.remove(widget);
            }
		}
	}
	
	/**
	 * Gets the current branch
	 * 
	 * @return the current branch
	 */
	public AuthoredBranch getCurrentBranch() {
	    return currentBranch;
	}
	
	/**
	 * Sets the current branch
	 * 
	 * @param currentBranch the current branch
	 */
	public void setCurrentBranch(AuthoredBranch currentBranch) {
	    this.currentBranch = currentBranch;
	}
	
	/**
	 * Get the associated tree of a branch
	 * 
	 * @param branch the branch to get the associated tree of
	 * @return the tree associated with the branch
	 */
	public AuthoredBranchTree getBranchTree(AuthoredBranch branch) {
	    return branchToTree.get(branch);
	}

	/**
	 * Gets the element associated with the course tree node
	 * 
	 * @param node the course tree node
	 * @return the element associated with the tree node. Null if no such element exists
	 */
    public Serializable getCourseObject(mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node) {
        if (currentBranch == null) {
            return courseTree.getCourseObject(node);
        }
        return null;
    }

	/**
	 * Gets the element associated with the branch tree node
	 * 
	 * @param node the branch tree node
	 * @return the element associated with the tree node. Null if no such element exists
	 */
    public Serializable getCourseObject(mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node) {
        if (currentBranch != null) {
            return branchToTree.get(currentBranch).getCourseObject(node);
        }
        return null;
    }
    
    /**
     * Sets the selected branch tree node of the branch
     * 
     * @param node the branch tree node
     * @param fireEvents whether to fire events for the select event
     */
    public void setSelectedNode(mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node, boolean fireEvents) {
        if (currentBranch != null) {
            branchToTree.get(currentBranch).setSelectedNode(node, fireEvents);
        }
    }
    
    /**
     * Sets the selected course tree node of the course
     * 
     * @param node the course tree node
     * @param fireEvents whether to fire events for the select event
     */
    public void setSelectedNode(mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node, boolean fireEvents) {
        courseTree.setSelectedNode(node, fireEvents);
    }
    
    /**
     * Gets the selected authored branch tree node of the current branch. Returns null if no branch is selected
     * 
     * @return the currently selected node in an authored branch. Null if no branch is selected
     */
    public mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode getSelectedNode() {
        if (currentBranch != null) {
            return branchToTree.get(currentBranch).getSelectedNode();
        }
        return null;
    }

    /**
     * Adds the dragged element to the specified path
     * 
     * @param pathStart the branch tree node associated with the path to add the dragged tree node to
     * @param dragged the dragged tree node
     */
    public void addToPathEnd(TreeNode pathStart, Serializable dragged) {
        if (currentBranch != null) {
            AuthoredBranch.Paths.Path path = (AuthoredBranch.Paths.Path) branchToTree.get(currentBranch).getCourseObject(pathStart);
            if (path != null) {
                int endIndex = path.getCourseobjects().getAAROrAuthoredBranchOrEnd().size();
                branchToTree.get(currentBranch).addToPath(pathStart, dragged, endIndex);

                showBranchTree(currentBranch);
            }
        }
    }

    /**
     * Get the path that the element belongs to
     * 
     * @param dropTarget the element to retrieve the path for
     * @return the Path of the element that contains the element. Null if no path
     * contains that element
     */
    public Path getPathOfElement(Serializable dropTarget) {
		
        for (Path path : currentBranch.getPaths().getPath()) {
            for (Serializable obj : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()) {
               if (obj == dropTarget) {
                   return path;
               }
            }
        }

        return null;
    }
    
    
    /**
     * Shows the breadcrumb tooltip when for the user's first navigation to edit the authored branch
     * and hides it for the rest of the edit course session
     */
    public void showBreadcrumbTooltip() {
    	if(!hasTooltipBeenShown) {
    		
    		//delay until the animation is complete
    		Timer delayTimer = new Timer() {
				@Override
				public void run() {
		    		breadcrumbTooltip.show();
		    		hasTooltipBeenShown = true;
		    		
		    		//shows the tooltip for a duration
		    		Timer showTimer = new Timer() {
		    			@Override
		    			public void run() {			
		    				if(hasTooltipBeenShown) {
		    					breadcrumbTooltip.hide();
		    				}
		    			}
		    		};
		    		showTimer.schedule(4000);
				}
    		};
    		delayTimer.schedule(2 * tooltipTimer);
    	}
    }
}
