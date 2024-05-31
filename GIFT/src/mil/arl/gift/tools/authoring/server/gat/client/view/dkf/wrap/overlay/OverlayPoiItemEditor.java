/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.PlacesOfInterest;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReferenceList;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that edits a single place of interest for the places of interest overlay. This editor allows the author 
 * to change the place's name, location and color, and it also displays a list of all of the conditions that reference 
 * the place of interest.
 * 
 * @author nroberts
 */
public class OverlayPoiItemEditor extends ItemEditor<AbstractPlaceOfInterestShape<?, ?>> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static OverlayPoiItemEditorUiBinder uiBinder = GWT.create(OverlayPoiItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface OverlayPoiItemEditorUiBinder extends UiBinder<Widget, OverlayPoiItemEditor> {
    }
    
    /** Contains each of the place sub-editors */
    @UiField
    protected DeckPanel placeTypeDeck;
    
    @UiField
    protected OverlayPointEditor pointEditor;
    
    @UiField
    protected OverlayPathEditor pathEditor;
    
    @UiField
    protected OverlayAreaEditor areaEditor;
    
    /** The button to show the place of interest references */
    @UiField
    protected Button referencesButton;
    
    /** The panel containing the place of interest references */
    @UiField
    protected Collapse referencesCollapse;
    
    /** The list of references */
    @UiField(provided=true)
    protected PlaceOfInterestReferenceList referenceList = new PlaceOfInterestReferenceList();
    
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);

    /**
     * Creates a new editor for editing a place of interest and initializes that editor's validation logic and UI elements
     */
    public OverlayPoiItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        referencesButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //change the appearance of the button used to display the reference list whenever it is clicked
                setReferencesVisible(IconType.CARET_RIGHT.equals(referencesButton.getIcon()));
            }
        });
    }

    /**
     * Shows or hides the list of conditions referencing a place of interest
     * 
     * @param visible whether or not to show the reference list
     */
    private void setReferencesVisible(boolean visible) {
        
        if(visible) {
            referencesButton.setIcon(IconType.CARET_DOWN);
            referencesCollapse.show();
            
        } else {
            referencesButton.setIcon(IconType.CARET_RIGHT);
            referencesCollapse.hide();
        }
    }

    @Override
    protected void populateEditor(AbstractPlaceOfInterestShape<?, ?> wrapper) {
        populateEditor(wrapper, true);
    }

    /**
     * Populates the editor with the data contained within the provided place of interest wrapper and, if specified,
     * resets the state of the editor. Resetting the editor's state will revert any changes the author has made that do
     * not affect the values of the object being edited, such as showing a list of coordinates.
     * 
     * @param wrapper the wrapper whose data should populate the editor
     * @param resetState whether to reset the state of the editor   
     */
    public void populateEditor(AbstractPlaceOfInterestShape<?, ?> wrapper, boolean resetState) {
        
        //display validation errors in the validation widget inside this editor
        initValidationComposite(validations);
        
        if(resetState) {
            
            //hide coordinates until the author decides to see them
            pointEditor.setCoordinatesVisible(false);
            pathEditor.setCoordinatesVisible(false);
            areaEditor.setCoordinatesVisible(false);
        }
        
        if (wrapper instanceof PointOfInterestShape) {
            
            pointEditor.edit((PointOfInterestShape) wrapper);
            showPointEditor();
            
        } else if (wrapper instanceof PathOfInterestShape) {
            
            pathEditor.edit((PathOfInterestShape) wrapper);
            showPathEditor();
            
        } else if (wrapper instanceof AreaOfInterestShape) {
            
            areaEditor.edit((AreaOfInterestShape) wrapper);
            showAreaEditor();
            
        } else {
            final String unsupportedType = wrapper.getClass().getSimpleName();
            throw new IllegalArgumentException("An object of type '" + unsupportedType + "' is not editable.");
        }
        
        setReferencesVisible(false);
        
        //update the list of references
        List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(wrapper.getPlaceOfInterest());
        referenceList.getListEditor().replaceItems(references);
    }

    @Override
    protected void applyEdits(AbstractPlaceOfInterestShape<?, ?> wrapper) {
        
        int deckIndex = placeTypeDeck.getVisibleWidget();
        
        if (deckIndex == placeTypeDeck.getWidgetIndex(pointEditor) && wrapper instanceof PointOfInterestShape) {
            pointEditor.applyEdits((PointOfInterestShape) wrapper);
            
        } else if (deckIndex == placeTypeDeck.getWidgetIndex(pathEditor) && wrapper instanceof PathOfInterestShape) {
            pathEditor.applyEdits((PathOfInterestShape) wrapper);
            
        } else if (deckIndex == placeTypeDeck.getWidgetIndex(areaEditor) && wrapper instanceof AreaOfInterestShape) {
            areaEditor.applyEdits((AreaOfInterestShape) wrapper);
            
        } else {
            throw new UnsupportedOperationException("An index of '" + deckIndex + "' was unhandled");
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        pointEditor.setReadonly(isReadonly);
        pathEditor.setReadonly(isReadonly);
        areaEditor.setReadonly(isReadonly);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //Nothing to validate
    }

    @Override
    protected boolean validate(AbstractPlaceOfInterestShape<?, ?> wrapper) {
        PlacesOfInterest placesOfInterest = new PlacesOfInterest();
        if (wrapper instanceof PointOfInterestShape) {
            PointOfInterestShape pointShape = (PointOfInterestShape) wrapper;
            placesOfInterest.getPointOrPathOrArea().add(pointShape.getPlaceOfInterest());
        } else if (wrapper instanceof PathOfInterestShape) {
            PathOfInterestShape pathShape = (PathOfInterestShape) wrapper;
            placesOfInterest.getPointOrPathOrArea().add(pathShape.getPlaceOfInterest());
        } else if (wrapper instanceof AreaOfInterestShape) {
            AreaOfInterestShape areaShape = (AreaOfInterestShape) wrapper;
            placesOfInterest.getPointOrPathOrArea().add(areaShape.getPlaceOfInterest());
        } else {
            return false;
        }

        String errorMsg = ScenarioValidatorUtility.validatePlacesOfInterest(placesOfInterest);
        return StringUtils.isBlank(errorMsg);
    }

    /**
     * Checks to see if the author has entered a unique name for the place of interest being edited. To be considered unique,
     * the entered name must not be blank and must not match the name of any other place of interest.
     * 
     * @param originalName the name this place of interest had when it was loaded
     * @param name the currently edited name of this place of interest
     * 
     * @return whether or not the entered name is unique
     */
    static boolean isEnteredNameUnique(String originalName, String name) {
        
        String enteredName = name;
        
        if(StringUtils.isBlank(enteredName)) {
            return false;
        }
        
        for (Serializable poi : ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea()) {
            if (!StringUtils.equals(originalName, ScenarioClientUtility.getPlaceOfInterestName(poi)) 
                    && StringUtils.equals(enteredName, ScenarioClientUtility.getPlaceOfInterestName(poi))) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //No statuses to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(pointEditor);
        childValidationComposites.add(pathEditor);
        childValidationComposites.add(areaEditor);
    }

    /**
     * Shows the editor used to edit points of interest
     */
    protected void showPointEditor() {
        showEditor(pointEditor);
    }
    
    /**
     * Shows the editor used to edit paths of interest
     */
    protected void showPathEditor() {
        showEditor(pathEditor);
    }
    
    /**
     * Shows the editor used to edit areas of interest
     */
    protected void showAreaEditor() {
        showEditor(areaEditor);
    }
    
    /**
     * Called to show a given editor.
     *
     * @param editor The editor to show. Null will not show an editor.
     */
    private void showEditor(ScenarioValidationComposite editor) {

        for (ValidationComposite child : getChildren()) {
            child.clearValidations();
            child.setActive(false);
        }

        if (editor != null) {
            
            editor.setActive(true);
            placeTypeDeck.showWidget(placeTypeDeck.getWidgetIndex(editor));
            
            updateValidationForEditor(editor);
        }
    }
    
    /**
     * Enable validations for the provided editor while clearing and disabling
     * validations from the other children.
     *
     * @param editor the editor to allow validations
     */
    private void updateValidationForEditor(ScenarioValidationComposite editor) {
        for (ValidationComposite validationChild : getChildren()) {
            if (validationChild.equals(editor)) {
                // mark active before validating
                validationChild.setActive(true);
                validationChild.validateAll();
            } else {
                // clear validations before marking as deactivated
                validationChild.clearValidations();
                validationChild.setActive(false);
            }
        }
    }
}
