/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Area;
import generated.dkf.Path;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that edits a single place of Interest. This editor allows the author to change the place's name
 * and location, and it also displays a list of all of the conditions that reference the place of interest.
 * 
 * @author nroberts
 */
public class PlaceOfInterestItemEditor extends ItemEditor<PlaceOfInterestWrapper> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static PlaceOfInterestItemEditorUiBinder uiBinder = GWT.create(PlaceOfInterestItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface PlaceOfInterestItemEditorUiBinder extends UiBinder<Widget, PlaceOfInterestItemEditor> {
    }
    
    /** Contains each of the place sub-editors */
    @UiField
    protected DeckPanel placeTypeDeck;
    
    /** The ribbon that allows the user to choose the place type */
    @UiField
    protected Ribbon placeTypeRibbon;

    /** The editor for points */
    @UiField
    protected PointEditor pointEditor;

    /** The editor for paths */
    @UiField
    protected PathEditor pathEditor;

    /** The editor for areas */
    @UiField
    protected AreaEditor areaEditor;

    /** The button to show the place of interest references */
    @UiField
    protected Button referencesButton;
    
    /** The panel containing the place of interest references */
    @UiField
    protected Collapse referencesCollapse;
    
    /** The list of references */
    @UiField(provided=true)
    protected PlaceOfInterestReferenceList referenceList = new PlaceOfInterestReferenceList();

    /** The ribbon item used to select the point type when creating a new place of interest */
    private Widget pointRibbonItem;

    /** The ribbon item used to select the path type when creating a new place of interest */
    private Widget pathRibbonItem;

    /** The ribbon item used to select the area type when creating a new place of interest */
    private Widget areaRibbonItem;
    
    /** disallowed types to be hidden in the coordinate editor.  Can be null or empty. */
    private CoordinateType[] disallowedCoordinateTypes = null;

    /**
     * Creates a new editor for editing a place of interest and initializes that editor's validation logic and UI elements
     */
    public PlaceOfInterestItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        initRibbon();
        
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
            referencesButton.setText("Hide References");
            referencesCollapse.show();
            
        } else {
            referencesButton.setIcon(IconType.CARET_RIGHT);
            referencesButton.setText("Show References");
            referencesCollapse.hide();
        }
    }
    
    /**
     * Sets the disallowed types to be hidden in the coordinate editor.
     * 
     * @param disallowedTypes the {@link CoordinateType types} to be hidden.  Can be empty/null
     * to hide nothing.
     */
    public void setDisallowedTypes(CoordinateType... disallowedTypes) {
        disallowedCoordinateTypes = disallowedTypes;
        pointEditor.setDisallowedTypes(disallowedTypes);
        pathEditor.setDisallowedTypes(disallowedTypes);
        areaEditor.setDisallowedTypes(disallowedTypes);
    }

    @Override
    protected void populateEditor(PlaceOfInterestWrapper wrapper) {
        
        Serializable obj = wrapper.getPlaceOfInterest();
        
        //populate the editor's fields
        if (obj == null) {
            showPlaceTypeRibbon();
        } else if (obj instanceof Point) {
            populateEditor((Point) obj);
        } else if (obj instanceof Path) {
            populateEditor((Path) obj);
        } else if (obj instanceof Area) {
            populateEditor((Area) obj);
        } else {
            final String unsupportedType = obj.getClass().getSimpleName();
            throw new IllegalArgumentException("An object of type '" + unsupportedType + "' is not editable.");
        }
        
        setReferencesVisible(false);
        
        //update the list of references
        List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(obj);
        referenceList.getListEditor().replaceItems(references); 
    }
    
    /**
     * Populates and shows the point editor.
     *
     * @param obj The {@link Point} object with which to
     *        populate the editor.
     */
    private void populateEditor(Point obj) {

        pointEditor.edit(obj);
        showPointEditor();
    }
    
    /**
     * Populates and shows the path editor.
     *
     * @param obj The {@link Path} object with which to
     *        populate the editor.
     */
    private void populateEditor(Path obj) {

        pathEditor.edit(obj);
        showPathEditor();
    }
    
    /**
     * Populates and shows the area editor.
     *
     * @param obj The {@link Area} object with which to
     *        populate the editor.
     */
    private void populateEditor(Area obj) {

        areaEditor.edit(obj);
        showAreaEditor();
    }

    @Override
    protected void applyEdits(PlaceOfInterestWrapper wrapper) {
        
        int deckIndex = placeTypeDeck.getVisibleWidget();
        
        if (deckIndex == placeTypeDeck.getWidgetIndex(placeTypeRibbon)) {
            wrapper.setPlaceOfInterest(null);
            
        } else if (deckIndex == placeTypeDeck.getWidgetIndex(pointEditor)) {
            
            Point point;
            
            if(wrapper.getPlaceOfInterest() instanceof Point) {
                point = (Point) wrapper.getPlaceOfInterest();
                
            } else {
                point = new Point();
                wrapper.setPlaceOfInterest(point);
            }
            
            wrapper.setPlaceOfInterest(point);          
            pointEditor.applyEdits(point);
            
        } else if (deckIndex == placeTypeDeck.getWidgetIndex(pathEditor)) {
            
            Path path;
            
            if(wrapper.getPlaceOfInterest() instanceof Path) {
                path = (Path) wrapper.getPlaceOfInterest();
                
            } else {
                path = new Path();
                wrapper.setPlaceOfInterest(path);
            }
            
            pathEditor.applyEdits(path);
            
        } else if (deckIndex == placeTypeDeck.getWidgetIndex(areaEditor)) {
            
            Area area = new Area();
            
            if(wrapper.getPlaceOfInterest() instanceof Area) {
                area = (Area) wrapper.getPlaceOfInterest();
                
            } else {
                area = new Area();
                wrapper.setPlaceOfInterest(area);
            }
            
            areaEditor.applyEdits(area);
            
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
        /* Nothing to validate */
    }

    @Override
    protected boolean validate(PlaceOfInterestWrapper poiWrapper) {
        final Serializable placeOfInterest = poiWrapper.getPlaceOfInterest();
        if (placeOfInterest instanceof Point || placeOfInterest instanceof Path || placeOfInterest instanceof Area) {
            String errorMsg = ScenarioValidatorUtility.validatePlaceOfInterest(placeOfInterest, disallowedCoordinateTypes);
            return StringUtils.isBlank(errorMsg);
        } else {
            return false;
        }
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
     * Initializes the ribbon used to select which type of place of interest to use
     */
    private void initRibbon() {
        
       Icon pointIcon = new Icon(IconType.MAP_MARKER);
       pointIcon.setWidth("24px");
       
       Icon mapIcon = new Icon(IconType.MAP);
       mapIcon.setWidth("24px");
       
       placeTypeRibbon.setTileHeight(100);
        
       pointRibbonItem = placeTypeRibbon.addRibbonItem(pointIcon, "Point", "Point of interest", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                populateEditor(new Point());
            }
       });
       
       pathRibbonItem = placeTypeRibbon.addRibbonItem(new Image("images/timeline.png"), "Path", "Path of interest", new ClickHandler() {

           @Override
           public void onClick(ClickEvent event) {
               populateEditor(new Path());
           }
       });
       
       areaRibbonItem = placeTypeRibbon.addRibbonItem(new Image("images/area.png"), "Area", "Area of interest", new ClickHandler() {

           @Override
           public void onClick(ClickEvent event) {
               populateEditor(new Area());
           }
       });
       
       if(WrapPanel.isCurrentTrainingAppSupported()) {
           Widget mapItem = placeTypeRibbon.addRibbonItem(mapIcon, "Map Location", "A location drawn on a map", new ClickHandler() {

               @Override
               public void onClick(ClickEvent event) {
                   WrapPanel.show();
               }
          });
                   
          mapItem.addStyleName("wrapRibbonButton");
       }
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
            setSaveButtonVisible(true);
            
            updateValidationForEditor(editor);
        }
    }
    
    /**
     * Shows the place type ribbon and hides all place editors.
     */
    private void showPlaceTypeRibbon() {

        showEditor(null);
        placeTypeDeck.showWidget(placeTypeDeck.getWidgetIndex(placeTypeRibbon));
        setSaveButtonVisible(false);
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
    
    /**
     * Sets the types of places of interest that this item editor should allow the author to pick from when
     * creating new places of interest
     * 
     * @param types the types of places of interest to allow
     */
    public void setAuthorablePlaceTypes(List<Class<?>> types) {
        
        if(types != null && !types.isEmpty()) {
            
            pointRibbonItem.setVisible(false);
            pathRibbonItem.setVisible(false);
            areaRibbonItem.setVisible(false);
            
            for(Class<?> type : types) {
                
                if(type.equals(Point.class)) {
                    pointRibbonItem.setVisible(true);
                    
                } else if(type.equals(Path.class)) {
                    pathRibbonItem.setVisible(true);
                    
                } else if(type.equals(Area.class)) {
                    areaRibbonItem.setVisible(true);
                }
            }
        } else {
            pointRibbonItem.setVisible(true);
            pathRibbonItem.setVisible(true);
            areaRibbonItem.setVisible(true);
        }
    }
}
