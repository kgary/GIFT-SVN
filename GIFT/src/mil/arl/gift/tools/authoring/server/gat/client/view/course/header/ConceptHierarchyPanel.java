/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

import generated.course.AuthoritativeResource;
import generated.course.ConceptNode;
import generated.course.Concepts;
import generated.course.Concepts.List.Concept;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ConceptNodeTreeItem;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.AuthoritativeResourceRecord;

/**
 * The widget that is responsible for editing a {@link Concepts} object
 * 
 * @author nroberts
 *
 */
public class ConceptHierarchyPanel extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ConceptHierarchyPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ConceptHierarchyPanelUiBinder uiBinder = GWT.create(ConceptHierarchyPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ConceptHierarchyPanelUiBinder extends UiBinder<Widget, ConceptHierarchyPanel> {
    }
    
    /**
     * The Interface ConceptHierarchyPanelEventBinder
     */
    interface ConceptHierarchyPanelEventBinder extends EventBinder<ConceptHierarchyPanel> {
    }

    /** The Constant eventBinder. */
    private static final ConceptHierarchyPanelEventBinder eventBinder = GWT.create(ConceptHierarchyPanelEventBinder.class);
    
	/** Interface to allow CSS style name access */
	interface Style extends CssResource {
		String treeDisabled();
	}
    
	/** Style for the concept hierarchy Panel */
    @UiField
	protected Style style;
    
    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);
    
    /** The tree of concepts */
    @UiField(provided=true)
    protected Tree tree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };
    
    /** Button used to collapse all the concepts in the tree */
    @UiField
    protected Button collapseAllButton;
    
    /** Button used to expand all the concepts in the tree */
    @UiField
    protected Button expandAllButton;
    
    /** A text box used to search for concepts names */
    @UiField
    protected TextBox searchBox;
    
    /** A button used to browse for authoritative resources */
    @UiField
    protected Button browseButton;
    
    /** The split panel used to show/hide the authoritative resource browser*/
    @UiField
    protected SplitLayoutPanel browserSplitter;
    
    /** A browser that displays resources in an authoritative system */
    @UiField(provided = true)
    protected AuthoritativeResourceBrowser resourceBrowser = new AuthoritativeResourceBrowser(new ImportHandler() {
        
        @Override
        public void onImport(List<AuthoritativeResourceRecord> resources) {
            
            if(resources == null) {
                throw new IllegalArgumentException("The resource to import cannot be null");
            }
            
            TreeItem item = tree.getSelectedItem();
            if(item instanceof ConceptNodeTreeItem) {
                
                for(AuthoritativeResourceRecord resource : resources) {
                
                    //create a new concept from the resource that is being imported
                    ConceptNode node = new ConceptNode();
                    node.setName(CourseConceptUtility.generateNewConceptName(resource.getName()));
                    node.setAuthoritativeResource(new AuthoritativeResource());
                    node.getAuthoritativeResource().setId(resource.getId());
                    
                    ((ConceptNodeTreeItem) item).getObject().getConceptNode().add(node);
                    
                    //create a new tree item for said concept
                    EditableConceptTreeItem importedItem = new EditableConceptTreeItem(node);
                    item.addItem(importedItem);
                }
                
                //show the item that was added
                item.setState(true);
            }
            
        }
    });
    
    /** Validation that warns the author when there are no concepts in the entire concept hierarchy */
    private WidgetValidationStatus emptyTreeValidation;

    /** Validation that warns the author when there is an error with one of the concept hierarchy's children */
    private WidgetValidationStatus treeValidation;
    
    /** The list-filtering command that is scheduled to be executed after keystrokes in the selection box*/
    private ScheduledCommand scheduledFilter = null;

    /**
     * Instantiates a panel to modify the course's global {@link Concepts}
     */
    public ConceptHierarchyPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
        
        
        emptyTreeValidation = new WidgetValidationStatus(tree, 
                "No learner roles are available. Please add at least one learner role "
                + "corresponding to a learner in the training application.");
        
        treeValidation = new WidgetValidationStatus(tree, "An item in the tree is invalid.");
        
        collapseAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                for(int i = 0; i < tree.getItemCount(); i++) {
                    collapseAll(tree.getItem(i));
                }
            }

            /**
             * Collapses the given tree item and its children
             * 
             * @param item the tree item to collapse
             */
            private void collapseAll(TreeItem item) {
                item.setState(false);
                
                for(int i = 0; i < item.getChildCount(); i++) {
                    collapseAll(item.getChild(i));
                }
            }
        });
        
        expandAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                for(int i = 0; i < tree.getItemCount(); i++) {
                    expandAll(tree.getItem(i));
                }
            }

            /**
             * Expands the given tree item and its children
             * 
             * @param item the tree item to expand
             */
            private void expandAll(TreeItem item) {
                item.setState(true);
                
                for(int i = 0; i < item.getChildCount(); i++) {
                    expandAll(item.getChild(i));
                }
            }
        });
        
        searchBox.setPlaceholder("Search concepts");
        searchBox.addDomHandler(new InputHandler() {

            @Override
            public void onInput(InputEvent event) {
                
                if(scheduledFilter == null) {
                    
                    // Schedule a filter operation for the list. We don't want to perform the filter operation immediately because
                    // it can cause some slight input lag if the user presses several keys in quick succession or holds a key down.
                    scheduledFilter = new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            // update the filter for the list
                            edit(CourseConceptUtility.getConcepts());
                            
                            //allow the filter operation to be applied again, since it is finished
                            scheduledFilter = null;
                        }
                    };

                    Scheduler.get().scheduleDeferred(scheduledFilter);
                }
            }

        }, InputEvent.getType());
        searchBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {

                /* select all of the search box's text when it gains focus so that it's easier for
                 * the author to clear out */
                searchBox.selectAll();
            }
        });
        
        browseButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //toggle whether or not the authoritative resource browser is shown
                if(browserSplitter.getWidgetSize(resourceBrowser) == 0) {
                
                    browserSplitter.setWidgetHidden(resourceBrowser, false);
                    browserSplitter.setWidgetSize(resourceBrowser, getOffsetWidth()/2);
                    
                    resourceBrowser.refresh();
                    
                } else {
                    browserSplitter.setWidgetSize(resourceBrowser, 0);
                    browserSplitter.setWidgetHidden(resourceBrowser, true);
                }
            }
        });
        
        browserSplitter.setWidgetHidden(resourceBrowser, true);
        
        // needs to be called last
        initValidationComposite(validations);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(emptyTreeValidation);
        validationStatuses.add(treeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        
        if(emptyTreeValidation.equals(validationStatus)) {
            emptyTreeValidation.setValidity(CourseConceptUtility.getRootConcept() != null);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //no children to validate
    }

    /**
     * Populates the panel using the data within the given {@link Concepts}.
     * 
     * @param courseObject the data object that will be used to populate the panel.
     */
    public void edit(Concepts concepts) {
        
        if (GatClientUtility.isReadOnly()) {
            setReadonly(true);
        }
        
        tree.clear();
        
        if(concepts == null) {
            throw new IllegalArgumentException("Concepts cannot be null");
        }
        
        Concepts.Hierarchy hierarchy = null;
        if(concepts.getListOrHierarchy() == null) {
            concepts.setListOrHierarchy(new Concepts.Hierarchy());
        }
        
        Serializable structure = concepts.getListOrHierarchy();
        if(structure instanceof Concepts.List) {
            
            hierarchy = new Concepts.Hierarchy();
            
            ConceptNode root = new ConceptNode();
            root.setName("All Concepts"); 
            hierarchy.setConceptNode(root);
            
            for(Concept concept : ((Concepts.List) structure).getConcept()) {
                
                ConceptNode toAdd = new ConceptNode();
                toAdd.setName(concept.getName());
                root.getConceptNode().add(toAdd);
            }
            
            concepts.setListOrHierarchy(hierarchy);
            
        } else {
            hierarchy = (Concepts.Hierarchy) structure;
        }
        
        if(hierarchy.getConceptNode() != null) {
            
            EditableConceptTreeItem rootItem = new EditableConceptTreeItem(hierarchy.getConceptNode());
            /* Manually update the tree validation with the root tree item's
             * validity */
            treeValidation.setValidity(rootItem.isValid());
            tree.addItem(rootItem);
            
            //select the root item of the tree by default so that authoritative resources can be imported immediately
            tree.setSelectedItem(rootItem);
            
            if(StringUtils.isNotBlank(searchBox.getText())) {
                
                //sort the concept tree's items by the given search text, if any search text has been entered
                rootItem.sortByText(searchBox.getText().toLowerCase());
            }  
        }
        
        validateAll();
    }
    
    /**
     * Find the TreeItem that corresponds to the Serializable item it represents
     * 
     * @param item The Serializable item to look for. Cannot be null.
     * @return the tree item that corresponds to the Serializable item. Returns null if
     *         not found.
     */
    private ConceptNodeTreeItem findTreeItem(Serializable item) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("findTreeItem(" + item + ")");
        }

        if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        ConceptNodeTreeItem node = null;

        for (int i = 0; i < tree.getItemCount(); i++) {
            node = findTreeItem((ConceptNodeTreeItem) tree.getItem(i), item);
            if (node != null) {
                return node;
            }
        }

        return node;
    }
    
    /**
     * Recursive method to search through the tree for the Serializable item.
     * 
     * @param node the root node containing the tree items we want to search through.
     *        Cannot be null.
     * @param item the Serializable item we want to find. Cannot be null.
     * @return the tree item that corresponds to the Serializable item. Returns null if
     *         not found.
     */
    private ConceptNodeTreeItem findTreeItem(ConceptNodeTreeItem node, Serializable item) {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'node' cannot be null.");
        } else if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        if (node.getObject() == item) {
            return node;
        }

        ConceptNodeTreeItem currNode = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i) instanceof ConceptNodeTreeItem) {
                currNode = findTreeItem((ConceptNodeTreeItem) node.getChild(i), item);
                if (currNode != null) {
                    return currNode;
                }
            }
        }

        return currNode;
    }
    
    /**
     * Walks the tree (from the bottom up) and performs validation for each item found.
     * 
     * @param treeItem the bottom node of the tree from which to start walking. If null, no validation occurs.
     */
    private void walkTreeAndValidate(ConceptNodeTreeItem treeItem) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("walkTreeAndValidate(" + treeItem + ")");
        }

        if (treeItem == null) {
            return;
        }

        boolean isTreeValid = true;
        while (treeItem != null) {

            ScenarioClientUtility.getValidationCache().getStatus(treeItem.getObject(), true);
            
            // update icon to reflect validity of the object
            treeItem.updateIcon();
            isTreeValid &= treeItem.isValid();

            // move up the tree
            treeItem = treeItem.getParentItem();

            if (treeItem == null) {
                // update validation message for tree
            }
        }

        treeValidation.setValidity(isTreeValid);
    }
    
    /**
     * Handles when a dirty event has been fired. Revalidates any necessary tree items.
     * 
     * @param event The dirty event containing details about the source scenario object.
     */
    public void handleDirtyEvent(ScenarioEditorDirtyEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleDirtyEvent(" + event.getSourceScenarioObject() + ")");
        }

        Serializable object = event.getSourceScenarioObject();
        if (object == null || !(object instanceof ConceptNode)) {
            return;
        }

        walkTreeAndValidate(findTreeItem(object));
        
        validateAll();
    }
    
    /**
     * Updates the style of the concept hierarchy panel to read only mode
     * 
     * @param isReadonly whether to set the panel as read only
     */
    private void setReadonly(boolean isReadonly) {
        if (isReadonly) {
            tree.addStyleName(style.treeDisabled()); 
        } else {
            tree.removeStyleName(style.treeDisabled());
        }
    }

    /**
     * A handler used to notify this panel when the user has decided to import this authoritative resource
     * 
     * @author nroberts
     */
    public static interface ImportHandler {
        
        /**
         * Notifies the listener when the user has decided to import the given resources
         * 
         * @param resources the resources being imported. Cannot be null.
         */
        public void onImport(List<AuthoritativeResourceRecord> resources);
    }
}
