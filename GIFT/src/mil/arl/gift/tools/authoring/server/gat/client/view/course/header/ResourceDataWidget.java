/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.shared.AuthoritativeResourceRecord;

/**
 * A widget used to display and interact with an authoritative resource
 * 
 * @author nroberts
 */
public class ResourceDataWidget extends Composite {

    private static ResourceDataWidgetUiBinder uiBinder = GWT.create(ResourceDataWidgetUiBinder.class);

    interface ResourceDataWidgetUiBinder extends UiBinder<Widget, ResourceDataWidget> {
    }
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {

        /**
         * Gets the style name used to indicate that the resource selected by this widget is selected
         * 
         * @return the style
         */
        String resourceSelected();
        
        /**
         * Gets the style name used to show ellipsis when a text element overflows
         * 
         * @return the style name
         */
        String ellipsis();
    }
    
    private static String MAIN_TOOLTIP_TEXT = "Click to view other resources within this ";
    private static String IMPORT_TOOLTIP_TEXT = "Click to create a concept associated with this ";
    private static String DEFAULT_TYPE = "Resource";
    
    /** An accessor for this widget's CSS styling rules */
    @UiField
    protected Style style;
    
    /** A label used to show the name of the authoritative resource being displayed */
    @UiField
    protected Label nameLabel;
    
    /** A label used to show the description of the authoritative resource being displayed */
    @UiField
    protected Label descriptionLabel;
    
    /** A label used to show the ID of the authoritative resource being displayed */
    @UiField
    protected Anchor idLabel;
    
    /** A button used to import the authoritative resource being displayed */
    @UiField
    protected Button importButton;
    
    /** The tooltip shown whenever the user hovers their mouse over this widget */
    @UiField
    protected Tooltip mainTooltip;
    
    /** The tooltip for the import button */
    @UiField
    protected Tooltip importTooltip;
    
    /** A check box used to decide whether this resource is selected for importing */
    @UiField
    protected CheckBox selectedBox;
    
    /** The panel containing the selection check box*/
    @UiField
    protected Widget selectedPanel;
    
    /** A button used to show/hide the resource's details */
    @UiField
    protected Button detailsButton;
    
    /** The resource that this widget represents */
    private final AuthoritativeResourceRecord resource;
    
    /** Whether the resource's details are currently visible */
    boolean detailsVisible = false;

    /**
     * Creates a new widget displaying the given authoritative resource
     * 
     * @param resource the authoritative resource to represent. Cannot be null.
     * @param importer the handle to use when the user tries to import the resource. Cannot be null.
     */
    public ResourceDataWidget(final AuthoritativeResourceRecord resource, final Command importer, final Command expand) {
        
        if(resource == null) {
            throw new IllegalArgumentException("The resource to display data for cannot be null");
        }
        
        if(importer == null) {
            throw new IllegalArgumentException("The handler used to import this resource cannot be null");
        }
        
        
        this.resource = resource;
        
        initWidget(uiBinder.createAndBindUi(this));

       
        nameLabel.setText(resource.getName());
        
        descriptionLabel.setText(resource.getDescription());
        idLabel.setText(resource.getId());
        idLabel.setHref(UriUtils.fromString(resource.getId()));
        
        importButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent click from bubbling to parent, since that could trigger navigation
                event.stopPropagation();
                
                setSelected(true);
                
                //notify any listeners when a user tries to import the resource
                importer.execute();
            }
        });
            
        selectedPanel.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent click from bubbling to parent, since that could trigger navigation
                event.stopPropagation();
                
                //select or deselect this resource for importing
                setSelected(!selectedBox.getValue());
            }
            
        }, ClickEvent.getType());
        
        detailsButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent click from bubbling to parent
                event.stopPropagation();
                
                expand.execute();
                
            }
        });
        
       nameLabel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //prevent click from bubbling to parent
                event.stopPropagation();
                
                expand.execute();
                
            }
        });
        
        
        //modify the displayed tooltip based on what type of resource this is
        String type = resource.getType();
        if(type == null) {
            type = DEFAULT_TYPE;
        }
        
        mainTooltip.setTitle(MAIN_TOOLTIP_TEXT + type);
        importTooltip.setTitle(IMPORT_TOOLTIP_TEXT + type);
        
        if(resource.getChildrenIDs() == null) {
            mainTooltip.setTrigger(Trigger.MANUAL);
            mainTooltip.hide();
        }
        
        
          addDomHandler(new ClickHandler() {

     			@Override
     			public void onClick(ClickEvent arg0) {
     				
     				 //toggle whether the resource's details are shown
                    detailsVisible = !detailsVisible;
                    idLabel.setVisible(detailsVisible);
                    
                    if(detailsVisible) {
                        descriptionLabel.removeStyleName(style.ellipsis());
                    } else {
                        descriptionLabel.addStyleName(style.ellipsis());
                    }
     			}
             }, ClickEvent.getType());
     }
    
    @Override
    protected void onDetach() {
        super.onDetach();
        
        mainTooltip.hide();
    }
    
    /**
     * Gets the resource that this widget represents
     * 
     * @return the resource. Will not be null.
     */
    public AuthoritativeResourceRecord getResource() {
        return resource;
    }
    
    /**
     * Gets whether this widget's resource is selected for importing
     * 
     * @return whether the resource is selected
     */
    public boolean isSelected() {
        return selectedBox.getValue();
    }
    
    /**
     * Sets whether this widget's resource is selected for importing
     * 
     * @param selected whether the resource is selected
     */
    public void setSelected(boolean selected) {
        selectedBox.setValue(selected);
        
        if(selected) {
            addStyleName(style.resourceSelected());
            
        } else {
            removeStyleName(style.resourceSelected());
        }
    }
}
