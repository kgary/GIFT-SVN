/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import generated.course.AuthoritativeResource;
import generated.course.ConceptNode;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.shared.AuthoritativeResourceRecord;

/**
 * An editor used to modify concepts' references to authoritative resources
 * 
 * @author nroberts
 */
public class AuthoritativeResourceEditor extends Composite {
    
    /** Placeholder text for labels whose data could not be retrieved */
    private static final String UNKNOWN_PLACEHOLDER = "<i style='color: red;'>Not Found</i>";
    
    /** The default type name to show for resources without a specific type */
    private static final String DEFAULT_TYPE = "Resource";

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(AuthoritativeResourceEditor.class.getName());
   
    private static AuthoritativeResourceEditorUiBinder uiBinder = GWT.create(AuthoritativeResourceEditorUiBinder.class);
    
    interface AuthoritativeResourceEditorUiBinder extends UiBinder<Widget, AuthoritativeResourceEditor> {
    }
    
    /** The panel containing the header text */
    @UiField
    protected Widget headerPanel;
    
    /** The header text */
    @UiField
    protected Label header;
    
    /** The input element used to change the unique ID of the authoritative resource*/
    @UiField
    protected TextBox idBox;
    
    /** The button used to show/hide the resource's details */
    @UiField
    protected Button detailsButton;
    
    /** The panel containing the resource's details */
    @UiField
    protected FlowPanel detailsPanel;
    
    /** The label used to show the resource's name */
    @UiField
    protected HTML nameLabel;
    
    /** The label used to show the resource's description */
    @UiField
    protected HTML descriptionLabel;
    
    /** The tooltip on the details button that says "Expand" or "Collapse" when hovered over */
    @UiField
    protected Tooltip expandCollapseTooltip;
    
    /** The concept currently being edited by this widget */
    private ConceptNode concept;
    
    /** A command to invoke whenever the resource is modified in some way */
    private Command onResourceChangeCommand = null;

    /**
     * Creates a new instance of this editor and initializes its event handlers
     */
    public AuthoritativeResourceEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        idBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(concept.getAuthoritativeResource() == null) {
                    AuthoritativeResource newResource = new AuthoritativeResource();
                    newResource.setId("");  // need to set the id to something in order to have the required id element appear in the course.xml
                                            // if not then PublishLessonScore.getConceptsAsXMLString(publishLessonScore.getConcepts().getConceptNode())); fails
                    concept.setAuthoritativeResource(newResource);
                }
                
                concept.getAuthoritativeResource().setId(event.getValue());
                
                onResourceChanged();
            }
        });
           
           detailsButton.setIcon(IconType.CHEVRON_RIGHT);
           
           detailsButton.addClickHandler(new ClickHandler() {
     			@Override
     			public void onClick(ClickEvent arg0) {
     				detailsButton.setIcon(detailsPanel.isVisible() ? IconType.CHEVRON_RIGHT : IconType.CHEVRON_DOWN);
     				//toggle the Details button
     				if(detailsPanel.isVisible()) {
     				   detailsPanel.setVisible(false);
     				   
     				   expandCollapseTooltip.setTitle("Click to expand");
     				   
     				}
     				
     				else {
     				  detailsPanel.setVisible(true);
     				  expandCollapseTooltip.setTitle("Click to collapse");
     				}
     			 }
             });
         }
    
    /**
     * Loads the given concept's authoritative resource into this editor so that the author can modify it using the provided user
     * interface controls
     * 
     * @param concept the concept whose authoritative resource should be modified
     */
    public void edit(final ConceptNode concept) {
        
        this.concept = concept;
        
        idBox.setValue(concept.getAuthoritativeResource() != null ? concept.getAuthoritativeResource().getId() : null);
    }
    
    /**
     * Updates the components in the editor based on the provided read-only flag.
     * 
     * @param readOnly true to set the components as read-only.
     */
    public void setReadOnly(boolean isReadonly) {
        idBox.setEnabled(!isReadonly);
    }
    
    /**
     * Notifies the appropriate listener whenever the resource is changed
     */
    private void onResourceChanged() {
        
        refreshDisplayedData();
        
        if(onResourceChangeCommand != null) {
            onResourceChangeCommand.execute();
        }
    }
    
    /**
     * Sets the command that should be invoked to notify a listener whenever the resource is changed
     * 
     * @param command the command to invoke on resource change
     */
    public void setResourceChangedCommand(Command command) {
        this.onResourceChangeCommand = command;
    }

    /**
     * Refreshes the data displayed for this resource to match what is currently on the server
     */
    public void refreshDisplayedData() {
        
        if(concept != null && concept.getAuthoritativeResource() != null) {
            SharedResources.getInstance().getRpcService().getAuthoritiativeResource(concept.getAuthoritativeResource().getId(), new AsyncCallback<GenericRpcResponse<AuthoritativeResourceRecord>>() {

                @Override
                public void onFailure(Throwable caught) {
                    logger.severe("Authoritative resource not found. " + caught);
                    header.setText(DEFAULT_TYPE);
                    nameLabel.setHTML(UNKNOWN_PLACEHOLDER);
                    descriptionLabel.setHTML(UNKNOWN_PLACEHOLDER);
                }

                @Override
                public void onSuccess(GenericRpcResponse<AuthoritativeResourceRecord> response) {
                    
                        if(response.getContent() == null) {
                            
                            header.setText(DEFAULT_TYPE);
                            nameLabel.setHTML(UNKNOWN_PLACEHOLDER);
                            descriptionLabel.setHTML(UNKNOWN_PLACEHOLDER);
                            return; 
                        }
                        
                        String type = response.getContent().getType();
                        
                        header.setText(StringUtils.isNotBlank(type) ? type : DEFAULT_TYPE);
                        nameLabel.setText(response.getContent().getName());
                        descriptionLabel.setText(response.getContent().getDescription());
                    }
                });
            }
    }
    
    /**
     * Sets whether or not the header text should be visible. Defaults to false to save space.
     * 
     * @param visible whethe the header should be visible
     */
    public void setHeaderVisible(boolean visible) {
        headerPanel.setVisible(visible);
    }
}
