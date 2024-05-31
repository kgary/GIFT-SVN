/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Area;
import generated.dkf.Path;
import generated.dkf.Point;

/**
 * A panel used to filter a list of places of interest to display only certain types
 * 
 * @author nroberts
 */
public class PlaceOfInterestFilterPanel extends Composite implements HasValue<List<Class<?>>>{

    private static PlaceOfInterestFilterPanelUiBinder uiBinder = GWT.create(PlaceOfInterestFilterPanelUiBinder.class);

    interface PlaceOfInterestFilterPanelUiBinder extends UiBinder<Widget, PlaceOfInterestFilterPanel> {
    }
    
    @UiField
    protected CheckBoxButton pointButton;
    
    @UiField
    protected Tooltip pointTooltip;
    
    @UiField
    protected CheckBoxButton pathButton;
    
    @UiField
    protected Tooltip pathTooltip;
    
    @UiField
    protected CheckBoxButton areaButton;
    
    @UiField
    protected Tooltip areaTooltip;

    /**
     * Creates a new panel for filtering a list of points of interest and initializes its handlers
     */
    public PlaceOfInterestFilterPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        Icon pointIcon = new Icon(IconType.MAP_MARKER);
        pointIcon.setSize(IconSize.LARGE);
        
        Image pathImage = new Image("images/timeline.png");
        Image areaImage = new Image("images/area.png");
        
        pointButton.setHTML(pointIcon.getElement().getString() + " Point");
        pathButton.setHTML(pathImage.getElement().getString() + " Path");
        areaButton.setHTML(areaImage.getElement().getString() + " Area");
        
        pointButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(getValue().size() == 1 && pointButton.isActive()) {
                    
                    //don't allow the author to deselect the last filter button
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
        pointButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        updatePointTooltip();
                        ValueChangeEvent.fire(PlaceOfInterestFilterPanel.this, getValue());
                    }
                });
            }
        });
        
        pathButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(getValue().size() == 1 && pathButton.isActive()) {
                    
                    //don't allow the author to deselect the last filter button
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
        pathButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        updatePathTooltip();
                        ValueChangeEvent.fire(PlaceOfInterestFilterPanel.this, getValue());
                    }
                });
            }
        });

        areaButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(getValue().size() == 1 && areaButton.isActive()) {
                    
                    //don't allow the author to deselect the last filter button
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
        areaButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        updateAreaTooltip();
                        ValueChangeEvent.fire(PlaceOfInterestFilterPanel.this, getValue());
                    }
                });
            }
        });
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Class<?>>> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public List<Class<?>> getValue() {
        
        //get the selected types based on which buttons are selected
        List<Class<?>> selectedTypes = new ArrayList<Class<?>>();
        
        if(!pointButton.isVisible() && !pathButton.isVisible() && !areaButton.isVisible()) {
        
            //if the author can't select a type to filter, don't filter any types
            selectedTypes.add(Point.class);
            selectedTypes.add(Path.class);
            selectedTypes.add(Area.class);
            
        } else {
            
            if(pointButton.isVisible() && pointButton.isActive()) {
                selectedTypes.add(Point.class);
            }
            
            if(pathButton.isVisible() && pathButton.isActive()) {
                selectedTypes.add(Path.class);
            }
            
            if(areaButton.isVisible() && areaButton.isActive()) {
                selectedTypes.add(Area.class);
            }
        }
        
        return selectedTypes;
    }

    @Override
    public void setValue(List<Class<?>> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<Class<?>> value, boolean fireEvents) {
        
        //select the appropriate buttons based on which types are passed in
        boolean hasPoint = false;
        boolean hasPath = false;
        boolean hasArea = false;
        
        if(value != null) {
            
            for(Class<?> type : value) {
                
                if(!hasPoint && type.equals(Point.class)) {
                    hasPoint = true;
                
                } else if(!hasPath && type.equals(Path.class)) {
                    hasPath = true;
                
                } else if(!hasArea && type.equals(Area.class)) {
                    hasArea = true;
                }
            }
        }
        
        pointButton.setValue(hasPoint, fireEvents);
        pointButton.setActive(hasPoint);
        updatePointTooltip();
        
        pathButton.setValue(hasPath, fireEvents);
        pathButton.setActive(hasPath);
        updatePathTooltip();
        
        areaButton.setValue(hasArea, fireEvents);
        areaButton.setActive(hasArea);
        updateAreaTooltip();
    }

    /**
     * Sets which types this widget should allow authors to select (i.e. which types should be visible to pick from)
     * 
     * @param acceptedTypes the types that the author should be able to select. If null or empty, all types will be shown.
     */
    public void setAcceptedTypes(Class<?>[] acceptedTypes) {
        
        if(acceptedTypes != null && acceptedTypes.length > 0) {
            
            //hide types that are not accepted
            boolean hasPoint = false;
            boolean hasPath = false;
            boolean hasArea = false;
            
            for(Class<?> type : acceptedTypes) {
                
                if(!hasPoint && type.equals(Point.class)) {
                    hasPoint = true;
                
                } else if(!hasPath && type.equals(Path.class)) {
                    hasPath = true;
                
                } else if(!hasArea && type.equals(Area.class)) {
                    hasArea = true;
                }
            }
            
            pointButton.setVisible(hasPoint);
            pathButton.setVisible(hasPath);
            areaButton.setVisible(hasArea);
            
            setVisible(acceptedTypes.length > 1);
            
        } else {
            
            //show all types, since all types are accepted
            pointButton.setVisible(true);
            pathButton.setVisible(true);
            areaButton.setVisible(true);
            
            setVisible(true);
        }
    }
    
    /**
     * Updates the tooltip for the button used to show and hide points so that its text matches the state of the button
     */
    private void updatePointTooltip() {
        pointTooltip.setTitle(pointButton.isActive() ? "Hide Points" : "Show Points");
    }
    
    /**
     * Updates the tooltip for the button used to show and hide paths so that its text matches the state of the button
     */
    private void updatePathTooltip() {
        pathTooltip.setTitle(pathButton.isActive() ? "Hide Paths" : "Show Paths");
    }
    
    /**
     * Updates the tooltip for the button used to show and hide areas so that its text matches the state of the button
     */
    private void updateAreaTooltip() {
        areaTooltip.setTitle(areaButton.isActive() ? "Hide Areas" : "Show Areas");
    }
}
