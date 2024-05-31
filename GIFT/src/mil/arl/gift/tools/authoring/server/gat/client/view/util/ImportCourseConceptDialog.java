/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import generated.course.ConceptNode;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ConceptNodeTreeItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ConceptNodeTreeItem.PickMode;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility.ConceptNodeRef;

public class ImportCourseConceptDialog extends ModalDialogBox {

    private static ImportCourseConceptDialogUiBinder uiBinder = GWT.create(ImportCourseConceptDialogUiBinder.class);

    interface ImportCourseConceptDialogUiBinder extends UiBinder<Widget, ImportCourseConceptDialog> {
    }
    
    private static ImportCourseConceptDialog instance = new ImportCourseConceptDialog();
    
    /** Button used to collapse all the concepts in the tree */
    @UiField
    protected Button collapseAllButton;
    
    /** Button used to expand all the concepts in the tree */
    @UiField
    protected Button expandAllButton;
    
    /** A text box used to search for concepts names */
    @UiField
    protected TextBox searchBox;
    
    private Button importButton = new Button("Import");
    
    private ImportHandler importHandler;
    
    /** The tree of concepts */
    @UiField(provided=true)
    protected Tree tree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };
    
    /** The list-filtering command that is scheduled to be executed after keystrokes in the selection box*/
    private ScheduledCommand scheduledFilter = null;

    private ImportCourseConceptDialog() {
        super();
        
        setWidget(uiBinder.createAndBindUi(this));
        setGlassEnabled(true);
        setText("Import Course Concept");
        setCloseable(true);
        getCloseButton().setText("Cancel");
        
        importButton.setEnabled(false);
        importButton.setType(ButtonType.PRIMARY);
        setFooterWidget(importButton);
        
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
                            reload();
                            
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
        
        importButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(importHandler != null) {
                    importHandler.onImport(getConceptNodeToImport());
                }
                
                hide();
            }
        });
    }
    
    public static void display(ImportHandler handler) {
        instance.displayInstance(handler);
    }
    
    private void displayInstance(ImportHandler handler) {
        
        importButton.setEnabled(false);
        
        importHandler = handler;
        
        reload();
        center();
    }
    
    private void reload() {
        
        tree.clear();
        
        ConceptNode rootConcept = CourseConceptUtility.getRootConcept();
        if(rootConcept != null) {
            
            ConceptNodeTreeItem rootItem = new ConceptNodeTreeItem(rootConcept);
            rootItem.setPickMode(PickMode.MULTIPLE);
            rootItem.setOnPickStateChangeCommand(new Command() {
                
                @Override
                public void execute() {
                    
                    importButton.setEnabled(getConceptNodeToImport() != null);
                }
            });
            
            tree.addItem(rootItem);
            
            if(StringUtils.isNotBlank(searchBox.getText())) {
                
                //sort the concept tree's items by the given search text, if any search text has been entered
                rootItem.sortByText(searchBox.getText().toLowerCase());
            }  
        }
    }
    
    public ConceptNodeRef getConceptToImport(ConceptNodeTreeItem item) {
           
        List<ConceptNodeRef> nodes = null;
        int childCount = item.getChildCount();
        if(childCount > 0) {
            for(int i = 0; i < childCount; i++) {
                
                ConceptNodeRef childRef = getConceptToImport((ConceptNodeTreeItem) item.getChild(i));
                if(childRef != null) {
                    
                    if(nodes == null) {
                        nodes = new ArrayList<>();
                    }
                    
                    nodes.add(childRef);
                }
            }
            
            if(nodes == null) {
                return null;
            }
            
        } else if(!item.isPicked()){
            return null;
        }
        
        return new ConceptNodeRef(item.getName(), nodes);
    }
    
    private ConceptNodeRef getConceptNodeToImport() {
        
        ConceptNodeRef nodeToImport = null;
        int childCount = tree.getItemCount();
        for(int i = 0; i < childCount; i++) {
            
            nodeToImport = getConceptToImport((ConceptNodeTreeItem) tree.getItem(i));
            if(nodeToImport != null) {
                break;
            }
        }
        
        return nodeToImport;
    }
    
    public static interface ImportHandler{
        
        public void onImport(ConceptNodeRef nodeToImport);
    }
}
