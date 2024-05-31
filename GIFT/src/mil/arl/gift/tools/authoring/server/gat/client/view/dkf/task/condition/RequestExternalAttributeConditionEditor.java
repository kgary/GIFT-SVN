/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ExternalAttributeEnumType;
import generated.dkf.RequestExternalAttributeCondition;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;

/**
 * A {@link ConditionInputPanel} that is used to edit the
 * {@link RequestExternalAttributeCondition} input.
 *
 * @author tflowers
 *
 */
public class RequestExternalAttributeConditionEditor extends ConditionInputPanel<RequestExternalAttributeCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(RequestExternalAttributeConditionEditor.class.getName());

    /** The binder that binds this java class to a ui.xml */
    private static RequestExternalAttributeConditionEditorUiBinder uiBinder = GWT
            .create(RequestExternalAttributeConditionEditorUiBinder.class);

    /** The definition of the binder that binds a java class to a ui.xml */
    interface RequestExternalAttributeConditionEditorUiBinder extends UiBinder<Widget, RequestExternalAttributeConditionEditor> {
    }

    /** The picker that selects the team members to assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(true);
    
    /** the name label for the attribute name */
    @UiField
    protected Label attributeNameLabel;
    
    /** the name textbox for the attribute name */
    @UiField
    protected TextBox attributeNameTextbox;
    
    /** the picker for the type of the attribute */
    @UiField
    protected Select attributeTypeSelect;

    /** The validation object used to validate team member picker */
    private final WidgetValidationStatus teamMemberValidation = new WidgetValidationStatus(teamPicker.getTextBoxRef(),
            "At least one team member must be chosen.");
    
    /** The validation object used to validate attribute name textbox */
    private final WidgetValidationStatus attributeNameTextboxValidation;

    /**
     * Builds an unpopulated {@link RequestExternalAttributeCondition}.
     */
    public RequestExternalAttributeConditionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("RequestExternalAttributeConditionEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        attributeNameTextboxValidation = new WidgetValidationStatus(attributeNameTextbox,
                "The attribute name must be provided.");

        // populate select widget with enums
        for(ExternalAttributeEnumType type : ExternalAttributeEnumType.values()){
            Option option = new Option();
            option.setText(type.name());
            option.setValue(type.name());
            attributeTypeSelect.add(option);
        }
    }
    
    /**
     * Hide/Show widgets based on the currently selected attribute type
     * 
     * @param type the currently selected attribute type.  Can be null.
     */
    private void updateDisplayForSelectedAttributeType(ExternalAttributeEnumType type){
        
        boolean isVarNameNeeded = type == null || type != ExternalAttributeEnumType.WEAPON_STATE;
        attributeNameLabel.setVisible(isVarNameNeeded);
        attributeNameTextbox.setVisible(isVarNameNeeded);
    }

    /**
     * Handles when the value of the {@link #teamPicker} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #teamPicker}. Can't be null.
     */
    @UiHandler("teamPicker")
    protected void onTeamMembersChanged(ValueChangeEvent<List<String>> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onTeamMembersChanged(" + event.toDebugString() + ")");
        }

        final List<String> teamMemberRef = getInput().getTeamMemberRefs().getTeamMemberRef();
        teamMemberRef.clear();
        teamMemberRef.addAll(event.getValue());

        requestValidationAndFireDirtyEvent(getCondition(), teamMemberValidation);
    }
    
    @UiHandler("attributeNameTextbox")
    protected void onAttributeNameTextboxChanged(ValueChangeEvent<String> event){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAttributeNameTextboxChanged(" + event.toDebugString() + ")");
        }
        
        getInput().setAttributeName(event.getValue());
        
        // check to make sure the textbox isn't empty
        requestValidationAndFireDirtyEvent(getCondition(), attributeNameTextboxValidation);
    }
    
    @UiHandler("attributeTypeSelect")
    protected void onAttributeTypeSelectChanged(ValueChangeEvent<String> event){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAttributeTypeSelectChanged(" + event.toDebugString() + ")");
        }
        
        getInput().setAttributeType(ExternalAttributeEnumType.valueOf(event.getValue()));
        
        // show/hide the attribute name label/textbox depending of if it is needed based on the type selected
        updateDisplayForSelectedAttributeType(getInput().getAttributeType());
        
        // if the attribute name is needed check validation of its value
        requestValidationAndFireDirtyEvent(getCondition(), attributeNameTextboxValidation);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getValidationStatuses(" + validationStatuses + ")");
        }

        validationStatuses.add(teamMemberValidation);
        validationStatuses.add(attributeNameTextboxValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (teamMemberValidation.equals(validationStatus)) {
            final int teamRefCount = getInput().getTeamMemberRefs().getTeamMemberRef().size();
            validationStatus.setValidity(teamRefCount >= 1);
        }else if(attributeNameTextboxValidation.equals(validationStatus)){
            validationStatus.setValidity(!attributeNameTextbox.isVisible() || 
                    StringUtils.isNotBlank(getInput().getAttributeName()));
        }
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        /* Ensure that there is a value for the assessed teams. */
        TeamMemberRefs teamMemberRefs = getInput().getTeamMemberRefs();
        if (teamMemberRefs == null) {
            teamMemberRefs = new TeamMemberRefs();
            getInput().setTeamMemberRefs(teamMemberRefs);
        }

        teamPicker.setValue(teamMemberRefs.getTeamMemberRef());
        
        if(getInput().getAttributeType() != null){
            for (Option option : attributeTypeSelect.getItems()) {
                if (StringUtils.equals(getInput().getAttributeType().name(), option.getText())) {
                    option.setSelected(true);
                    break;
                }
            }
        }else{            
            // set default type since select is defaulted
            getInput().setAttributeType(ExternalAttributeEnumType.valueOf(attributeTypeSelect.getItems().get(0).getName()));
        }
        
        // show/hide the attribute name label/textbox depending of if it is needed based on the type selected
        updateDisplayForSelectedAttributeType(getInput().getAttributeType());
        
        if(getInput().getAttributeName() != null){
            attributeNameTextbox.setValue(getInput().getAttributeName());
        }        
        
        // if the attribute name is needed check validation of its value
        requestValidationAndFireDirtyEvent(getCondition(), attributeNameTextboxValidation);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        teamPicker.setReadonly(isReadonly);
        attributeNameTextbox.setReadOnly(isReadonly);
        attributeTypeSelect.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addValidationCompositeChildren(" + childValidationComposites + ")");
        }

        childValidationComposites.add(teamPicker);
    }
}
