/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.util.Set;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.course.Concepts.List.Concept;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A {@link ItemEditor} that edits concepts in the list of course concepts
 * 
 * @author nroberts
 */
public class ConceptItemEditor extends ItemEditor<Concept> {
    /** The UiBinder that combines the ui.xml with this java class */
    private static ConceptItemEditorUiBinder uiBinder = GWT.create(ConceptItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ConceptItemEditorUiBinder extends UiBinder<Widget, ConceptItemEditor> {
    }

    /** The text box containing the name of the concept item */
    @UiField
    protected TextBox nameBox;

    /** The name of the waypoint being edited when it was originally loaded */
    private String originalName = null;
    
    /** Validation that displays an error when the author has not entered a unique name for this concept */
    private WidgetValidationStatus nameValidation;

    /**
     * Creates a new editor for editing concepts
     */
    public ConceptItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        nameValidation = new WidgetValidationStatus(nameBox, "Please enter a unique name for this concept.");
        
        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(nameValidation);
            }
        });
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        
        if(nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(isEnteredNameUnique());
        }
    }

    @Override
    protected boolean validate(Concept concept) {
        if (StringUtils.isBlank(concept.getName())) {
            return false;
        }

        for (Concept editorItem : getParentItemListEditor().getItems()) {
            /* Skip provided concept */
            if (editorItem == concept) {
                continue;
            }

            /* Check if the concept name (case insensitive) conflicts with
             * another */
            if (StringUtils.equalsIgnoreCase(concept.getName(), editorItem.getName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether or not the name that has been entered into the text box is valid and does not match any other existing
     * concept's name.
     * 
     * @return whether or not the entered name is valid and unique
     */
    private boolean isEnteredNameUnique() {
        
        if(StringUtils.isBlank(nameBox.getValue())) {
            
            //entered concept name is not valid
            return false;
        }
        
        String enteredName = nameBox.getValue().trim();
        
        if(GatClientUtility.getBaseCourseConcepts() != null) {
            
            for(String conceptName : GatClientUtility.getBaseCourseConcepts()) {
                /* Skip the item being edited */
                if (StringUtils.equalsIgnoreCase(conceptName, originalName)) {
                    continue;
                } else if (StringUtils.equalsIgnoreCase(enteredName, conceptName)) {
                    // entered concept name is not unique
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    protected void populateEditor(Concept obj) {
        
        originalName = obj.getName();
        
        nameBox.setValue(originalName);
    }

    @Override
    protected void applyEdits(Concept obj) {
        obj.setName(nameBox.getValue().toLowerCase().trim());
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        nameBox.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

}
