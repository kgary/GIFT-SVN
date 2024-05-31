/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.GenericConditionInput;
import generated.dkf.Nvpair;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.EditableItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * The condition impl for generic condition inputs.
 */
public class GenericConditionInputEditorImpl extends ConditionInputPanel<GenericConditionInput> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(GenericConditionInputEditorImpl.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static GenericConditionInputEditorUiBinder uiBinder = GWT.create(GenericConditionInputEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface GenericConditionInputEditorUiBinder extends UiBinder<Widget, GenericConditionInputEditorImpl> {
    }

    /** The item list editor table for the condition parameters */
    @UiField
    protected ItemListEditor<Nvpair> conditionParameterListEditor;
    
    /** read only flag */
    private boolean isReadOnly = false;

    /** Validation container for when there is no condition nvpair */
    private final WidgetValidationStatus conditionSelectedValidation;
    
    /**
     * Constructor
     */
    public GenericConditionInputEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        conditionParameterListEditor.setFields(buildConditionParameterItemFields());
        conditionParameterListEditor.setDraggable(true);
        conditionParameterListEditor.addListChangedCallback(new ListChangedCallback<Nvpair>() {
            @Override
            public void listChanged(ListChangedEvent<Nvpair> event) {
                requestValidationAndFireDirtyEvent(getCondition(), conditionSelectedValidation);
            }
        });
        conditionParameterListEditor.setRemoveItemStringifier(new Stringifier<Nvpair>() {
            @Override
            public String stringify(Nvpair obj) {
                return "the parameter with name '" + bold(obj.getName()).asString() + "' and value '"
                        + bold(obj.getValue()).asString() + "'";
            }
        });
        Widget createButton = conditionParameterListEditor.addCreateListAction("Click here to add a new parameter",
                buildConditionParameterCreateAction());
        
        conditionSelectedValidation = new WidgetValidationStatus(createButton, "There are no key-value pairs to match. Add a key-value pair that should be matched.");
    }

    /**
     * Builds the item fields for the condition parameter table.
     * 
     * @return the {@link ItemField item field columns} for the condition parameter table.
     */
    private List<EditableItemField<Nvpair>> buildConditionParameterItemFields() {

        EditableItemField<Nvpair> nameField = new EditableItemField<Nvpair>("Name", "50%") {
            private TextBox textbox = new TextBox();
            {
                textbox.addValueChangeHandler(new ValueChangeHandler<String>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        validate(nameValidationStatus);
                        updateValidationStatus(nameValidationStatus);
                    }
                });
            }
            
            private final static String BLANK_NAME_ERR_MSG = "The condition parameter name cannot be empty.";

            private WidgetValidationStatus nameValidationStatus = new WidgetValidationStatus(textbox,
                    BLANK_NAME_ERR_MSG);

            private Nvpair itemInField;
            
            @Override
            public Widget getViewWidget(Nvpair item) {
                itemInField = item;
                return new HTML(item.getName());
            }

            @Override
            public Widget getEditWidget(Nvpair item) {
                itemInField = item;
                
                textbox.setValue(item.getName());
                textbox.setPlaceholder("Enter a name");

                // check for the read only flag
                textbox.setEnabled(!isReadOnly);

                return textbox;
            }

            @Override
            public void applyEdits(Nvpair obj) {
                obj.setName(textbox.getValue());
            }

            @Override
            public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
               validationStatuses.add(nameValidationStatus);
            }

            @Override
            public void validate(ValidationStatus validationStatus) {
                if (nameValidationStatus.equals(validationStatus)) {
                    if (StringUtils.isBlank(textbox.getValue())) {
                        nameValidationStatus.setErrorMessage(BLANK_NAME_ERR_MSG);
                        nameValidationStatus.setInvalid();
                    } else {
                        boolean foundDuplicate = false;
                        for (Nvpair pair : conditionParameterListEditor.getItems()) {
                            // skip current pair
                            if (pair.equals(itemInField)) {
                                continue;
                            }

                            if (StringUtils.equalsIgnoreCase(pair.getName(), textbox.getValue())) {
                                foundDuplicate = true;
                                nameValidationStatus.setErrorMessage(
                                        "The condition parameter name '" + textbox.getValue() + "' is a duplicate.");
                                break;
                            }
                        }
                        nameValidationStatus.setValidity(!foundDuplicate);
                    }
                }
            }
        };

        EditableItemField<Nvpair> valueField = new EditableItemField<Nvpair>("Value", "50%") {
            private TextBox textbox = new TextBox();
            {
                textbox.addValueChangeHandler(new ValueChangeHandler<String>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        validate(nameValidationStatus);
                        updateValidationStatus(nameValidationStatus);
                    }
                });
            }

            private WidgetValidationStatus nameValidationStatus = new WidgetValidationStatus(textbox,
                    "The condition parameter value cannot be empty.");

            @Override
            public Widget getViewWidget(Nvpair item) {
                return new HTML(item.getValue());
            }

            @Override
            public Widget getEditWidget(Nvpair item) {
                textbox.setValue(item.getValue());
                textbox.setPlaceholder("Enter a value");

                // check for the read only flag
                textbox.setEnabled(!isReadOnly);

                return textbox;
            }

            @Override
            public void applyEdits(Nvpair obj) {
                obj.setValue(textbox.getValue());
            }
            
            @Override
            public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
               validationStatuses.add(nameValidationStatus);
            }

            @Override
            public void validate(ValidationStatus validationStatus) {
                if(nameValidationStatus.equals(validationStatus)) {
                    nameValidationStatus.setValidity(StringUtils.isNotBlank(textbox.getValue()));                   
                }
            }
        };

        return Arrays.asList(nameField, valueField);
    }

    /**
     * Builds the create action for the condition parameter table.
     * 
     * @return the {@link CreateListAction} for the condition parameter table.
     */
    private CreateListAction<Nvpair> buildConditionParameterCreateAction() {
        return new CreateListAction<Nvpair>() {

            @Override
            public Nvpair createDefaultItem() {
                return new Nvpair();
            }
        };
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit(" + getInput().getNvpair() + ")");
        }
        
        // set list into the condition parameter editor
        conditionParameterListEditor.setItems(getInput().getNvpair());
    }
    

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(conditionSelectedValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int conditionParamSize;
        if (conditionSelectedValidation.equals(validationStatus)) {
            conditionParamSize = conditionParameterListEditor.size();
            conditionSelectedValidation.setValidity(conditionParamSize > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(conditionParameterListEditor);
    }
    @Override
    protected void setReadonly(boolean isReadonly) {
        this.isReadOnly = isReadonly;
        conditionParameterListEditor.setReadonly(isReadonly);
    }
}