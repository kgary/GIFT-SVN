/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Coordinate;
import generated.dkf.LearnerId;
import generated.dkf.StartLocation;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * An editor that provides the author with an interface used to modify learner IDs
 * 
 * @author nroberts
 */
public class LearnerIdEditor extends Composite {

    private static LearnerIdEditorUiBinder uiBinder = GWT.create(LearnerIdEditorUiBinder.class);

    interface LearnerIdEditorUiBinder extends UiBinder<Widget, LearnerIdEditor> {
    }
    
    /**
     * A callback used to handle when this editor's optional cancel button is pressed
     * 
     * @author nroberts
     */
    public static interface CancelCallback{
        
        /**
         * Handles when the user has cancelled editing for this editor
         * 
         * @param backupCopy the backup copy of the learner ID being edited that this editor will revert to
         */
        public void onEditingCancelled(LearnerId backupCopy);
    }
    
    /** The deck panel used to switch between the sub-editors for each learner ID type*/
    @UiField
    protected DeckPanel mainDeck;
    
    /** A ribbon used to select the learner ID type */
    @UiField
    protected Ribbon typeRibbon;
    
    /** The editor used to modify the start location type */
    @UiField(provided = true)
    protected ScenarioCoordinateEditor startLocationEditor = new ScenarioCoordinateEditor(true);
    
    /** The editor used to modify the entity marker type*/
    @UiField
    protected FlowPanel entityMarkerEditor;
    
    /** The text box used to change an entity marker's name */
    @UiField
    protected TextBox markerNameBox;
    
    /** A button used to change the learner ID type after a type has been selected */
    @UiField
    protected Button changeTypeButton;
    
    /** The label for the entity marker editor*/
    @UiField
    protected HTML entityMarkerLabel;
    
    /** A button used to cancel editing a learner ID and revert to its original value */
    @UiField
    protected Button cancelButton;
    
    /** The learner ID currently being edited by this widget */
    private LearnerId learnerId;
    
    /** A command to invoke whenever the learner ID is modified in some way */
    private Command onLearnerIdChangeCommand = null;
    
    /** A backup of the learner ID's original value that is used when editing is cancelled */
    private LearnerId backupLearnerId;

    /** The callback that will handle when this editor's cancel button is pressed, if assigned */
    private CancelCallback cancelCallback;
    
    /**
     * Creates a new editor that allows the author to modify learner IDs and sets up its event handlers
     */
    public LearnerIdEditor() {
        this(null);
    }

    /**
     * Creates a new editor that allows the author to modify learner IDs, sets up its event handlers, and optionally
     * allows the user to cancel editing
     * 
     * @param cancelCallback the callback that will handle when this editor's cancel button is pressed, or null, if the
     * user shouldn't be able to cancel editing
     */
    public LearnerIdEditor(final CancelCallback cancelCallback) {
        initWidget(uiBinder.createAndBindUi(this));
        
        this.cancelCallback = cancelCallback;
        
        typeRibbon.addRibbonItem(
                IconType.BULLSEYE, 
                "Start Location", 
                "Identify the entity associated with this learner in training application based on its start location", 
                new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        StartLocation location = new StartLocation();
                        
                        learnerId.setType(location);
                        populateStartLocation(location);
                        
                        onLearnerIdChanged();
                    }
                });
        
        typeRibbon.addRibbonItem(
                IconType.TAG, 
                "Entity Marker", 
                "Identify the entity associated with this learner in training application using a unique marker assigned to it", 
                new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        String markerName = "New Entity Marker";
                        
                        learnerId.setType(markerName);
                        populateMarkerName(markerName);
                        
                        onLearnerIdChanged();
                    }
                });
        
        changeTypeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                learnerId.setType(null);
                mainDeck.showWidget(mainDeck.getWidgetIndex(typeRibbon));
                changeTypeButton.setVisible(false);
                
                onLearnerIdChanged();
            }
        });
        
        startLocationEditor.addStyleName("teamMemberCoordinate");
        
        if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
            // for VBS, AGL is not allowed for learner location
            startLocationEditor.setDisallowedTypes(CoordinateType.AGL);
        }
        
        startLocationEditor.addSaveHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //re-validate when the start location is changed
                onLearnerIdChanged();
            }
        });
        
        markerNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(StringUtils.isNotBlank(event.getValue())) {
                    
                    //update the marker name
                    learnerId.setType(event.getValue());
                    onLearnerIdChanged();
                    
                } else {
                    
                    //don't allow the author to leave the marker name empty
                    Notify.notify("The marker name must contain at least one visible character.", NotifyType.DANGER);
                    
                    if(learnerId.getType() instanceof String) {
                        markerNameBox.setText((String)learnerId.getType());
                    }
                }
            }
        });
        
        if(cancelCallback != null) {
            
            //if the appropriate callback is provided, allow the author to cancel editing
            cancelButton.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    event.stopPropagation();
                    
                    edit(backupLearnerId);
                    backupLearnerId = getLearnerIdCopy();
                    
                    cancelCallback.onEditingCancelled(backupLearnerId);
                }
            });
            
            cancelButton.setVisible(true);
            
        } else {
            cancelButton.setVisible(false);
        }
        
        //show the type ribbon by default
        mainDeck.showWidget(mainDeck.getWidgetIndex(typeRibbon));
        changeTypeButton.setVisible(false);
    }
    
    /**
     * Loads the given learner ID into this editor so that the author can modify it using the provided user
     * interface controls
     * 
     * @param learnerId the learner ID to be edited
     */
    public void edit(LearnerId learnerId) {
        
        this.learnerId = learnerId;
        
        if(cancelCallback != null) {
            backupLearnerId = getLearnerIdCopy();
        }
        
        if(learnerId.getType() instanceof StartLocation){
            populateStartLocation((StartLocation) learnerId.getType());
            
        } else if(learnerId.getType() instanceof String){  
            populateMarkerName((String) learnerId.getType());
                
        } else {
            
            mainDeck.showWidget(mainDeck.getWidgetIndex(typeRibbon));
            changeTypeButton.setVisible(false);
        }
    }
    
    /**
     * Loads the given start location into the editor and shows the start location editor to the author
     * 
     * @param location the start location to load
     */
    private void populateStartLocation(StartLocation location) {
        
        if(location.getCoordinate() == null) {
            location.setCoordinate(new Coordinate());
        }
        
        startLocationEditor.setCoordinate(location.getCoordinate());
        
        if(startLocationEditor.isTypeSelected()) {
            startLocationEditor.updateCoordinate();
        }
        
        mainDeck.showWidget(mainDeck.getWidgetIndex(startLocationEditor));
        changeTypeButton.setVisible(true);
    }
    
    /**
     * Loads the given entity marker name into the editor and shows the entity marker editor to the author
     * 
     * @param name the entity marker name to load
     */
    private void populateMarkerName(String name) {
        
        markerNameBox.setValue(name);
        
        mainDeck.showWidget(mainDeck.getWidgetIndex(entityMarkerEditor));
        changeTypeButton.setVisible(true);
    }
    
    /**
     * Notifies the appropriate listener whenever the learner ID is changed
     */
    private void onLearnerIdChanged() {
        
        if(onLearnerIdChangeCommand != null) {
            onLearnerIdChangeCommand.execute();
        }
    }
    
    /**
     * Sets the command that should be invoked to notify a listener whenever the learner ID is changed
     * 
     * @param command the command to invoke on learner ID change
     */
    public void setLearnerIdChangedCommand(Command command) {
        this.onLearnerIdChangeCommand = command;
    }

    /**
     * Updates the components in the editor based on the provided read-only flag.
     * 
     * @param readOnly true to set the components as read-only.
     */
    public void setReadOnly(boolean isReadonly) {
        typeRibbon.setReadonly(isReadonly);
        startLocationEditor.setReadOnly(isReadonly);
        markerNameBox.setEnabled(!isReadonly);
        changeTypeButton.setEnabled(!isReadonly);
    }

    /**
     * Sets the text of the label for the entity marker editor to the given string
     * 
     * @param text the string to use as the text for the label.  HTML supported.
     */
    public void setEntityMarkerLabel(String text) {
        entityMarkerLabel.setHTML(text);
    }
    
    /**
     * Set the max length for entity marking string.
     * 
     * @param maxChars if less than one this method does nothing.
     */
    public void setEntityMarkerCharacterLimit(int maxChars){
        
        if(maxChars < 1){
            return;
        }
        
        markerNameBox.setMaxLength(maxChars);
    }
    
    /**
     * Gets a deep copy of the learner ID being modified by this editor
     * 
     * @return the deep copy of the learner ID
     */
    public LearnerId getLearnerIdCopy() {
        
        LearnerId copyLearnerId = new LearnerId();
        
        if(learnerId.getType() instanceof String) {
            copyLearnerId.setType(learnerId.getType());
            
        } else if(learnerId.getType() instanceof StartLocation){
            
            StartLocation startLocation = (StartLocation) learnerId.getType();
            StartLocation newStartLocation = new StartLocation();
            
            newStartLocation.setCoordinate(startLocationEditor.getCoordinateCopy());
            
            copyLearnerId.setType(startLocation);
        }
        
        return copyLearnerId;
    }
}
