/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.ConceptNode;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.AuthoritativeResourceEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;

/**
 * A widget that can display course concept data associated with a specific scenario object
 * 
 * @author nroberts
 */
public class CourseConceptDisplayWidget extends Composite {

    private static CourseConceptDisplayWidgetUiBinder uiBinder = GWT.create(CourseConceptDisplayWidgetUiBinder.class);

    interface CourseConceptDisplayWidgetUiBinder extends UiBinder<Widget, CourseConceptDisplayWidget> {
    }
    
    /** Interface for handling events. */
    interface MyEventBinder extends EventBinder<CourseConceptDisplayWidget> {
    }
    
    /** Create the instance of the event binder (binds the widget for events. */
    private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
    
    /** The header/title for the control */
    @UiField
    protected PanelHeader panelHeader;

    /** The collapse that contains the body of the widget */
    @UiField
    protected Collapse collapse;
    
    /** The panel used to display the authoritative resource editor */
    @UiField
    protected Widget resourcePanel;
    
    /** An editor used to display authoritative resource data for a concept*/
    @UiField
    protected AuthoritativeResourceEditor resourceEditor;
    
    /** The button used to view this course's concepts in the concepts editor*/
    @UiField
    protected Button viewButton;
    
    /** The scenario object whose course concept data is being displayed */
    private Serializable object;

    /**
     * Creates a new widget with no course concept data. By default, this widget will be invisible until
     * a scenario object with associated course concept data is loaded.
     */
    public CourseConceptDisplayWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
        
        resourceEditor.setHeaderVisible(true);
        
        viewButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                CourseConceptUtility.showCourseConceptsEditor();
            }
        });
        
        // Populate the collapsible sections with a randomly generate id
        String id = Document.get().createUniqueId();
        panelHeader.setDataTarget("#" + id);
        collapse.setId(id);
    }
    
    /**
     * Loads the given scenario object and, if possible, attempts to display its associated course concept data.
     * If the scenario object's name does not match any course concept, then no data will be loaded and this widget
     * will be hidden.
     * 
     * @param object the object to load. Can be null, which will hide this widget.
     */
    public void load(Serializable object) {
        
        this.object = object;
        
        if(object == null) {
            setVisible(false);
            return;
        }
        
        String name = ScenarioElementUtil.getObjectName(object);
        
        ConceptNode courseConcept = CourseConceptUtility.getConceptWithName(name);
        if(courseConcept != null) {
            
            resourceEditor.edit(courseConcept);
            resourceEditor.refreshDisplayedData();
            
            resourcePanel.setVisible(courseConcept.getAuthoritativeResource() != null);
            
            setVisible(true);
            
        } else {
            setVisible(false);
        }
    }
    
    @EventHandler
    protected void onRenameScenarioObjectEvent(RenameScenarioObjectEvent renameEvent) {
        
        /*
         * if the loaded scenario object is renamed, we need to reload its course concept data, since
         * course concepts are associated by name
         */
        if(object != null && object.equals(renameEvent.getScenarioObject())) {
            load(object);
        }
    }
    
    @EventHandler
    protected void onCourseConceptsChanged(CourseConceptsChangedEvent event) {
        
        // if the course concepts are changed, we need to reload the course concept data
        if(object != null) {
            load(object);
        }
    }

}
