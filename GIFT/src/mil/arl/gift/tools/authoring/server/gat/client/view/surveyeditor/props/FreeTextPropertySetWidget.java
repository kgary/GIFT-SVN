/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyResponseTypeEnum;
import mil.arl.gift.common.gwt.client.survey.SurveyCssStyles;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;

/**
 * The ResponseFieldPropertySetWidget is responsible for displaying the controls that allow the
 * author to customize the properties for each free text response field.
 * 
 * @author sharrison
 *
 */
public class FreeTextPropertySetWidget extends AbstractPropertySetWidget {

    private static Logger logger = Logger.getLogger(FreeTextPropertySetWidget.class.getName());

    /** UI Binder */
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** UI Binder interface */
    interface WidgetUiBinder extends UiBinder<Widget, FreeTextPropertySetWidget> {
    }

    @UiField
    protected HorizontalPanel freeTextHorizontalPanel;
    
    @UiField
    protected Label typeLabel;
    
    @UiField
    protected CheckBox alignmentCheckbox;

    @UiField
    protected ValueListBox<String> typeDropdown;

    @UiField
    protected TextBox labelTextBox;

    private static final String LABEL_STR = "Response Field";

    /**
     * Constructor.
     * 
     * @param propertySet - The property set for the widget.
     * @param listener - The listener that will handle changes to the properties.
     * @param isInQuestionBank - True if the widget is contained within a question bank; false otherwise.
     * @param defaultValue - The default {@link SurveyResponseTypeEnum} to select in the type dropdown. Must be {@link SurveyResponseTypeEnum#NUMERIC} if this widget is in a question bank.
     * @param onChangeCommand - The command to be executed whenever the underlying metadata is changed. Can be null.
     */
    public FreeTextPropertySetWidget(AbstractPropertySet propertySet, PropertySetListener listener, boolean isInQuestionBank, SurveyResponseTypeEnum defaultValue, final Command onChangeCommand) {
        super(propertySet, listener);

        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("FreeTextPropertySetWidget(");
            List<Object> params = Arrays.<Object>asList(propertySet, listener, isInQuestionBank);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        /* Protect against creating an invalid type for question banks */
        if (isInQuestionBank && SurveyResponseTypeEnum.NUMERIC != defaultValue) {
            throw new IllegalArgumentException("The parameter 'type' must be NUMERIC since this is in a question bank.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        freeTextHorizontalPanel.addStyleName(SurveyCssStyles.SURVEY_FREE_RESPONSE_DROPDOWN_STYLE);
        typeLabel.setText(LABEL_STR);
        
        alignmentCheckbox.setText("Align Left");
        alignmentCheckbox.setHeight("21px");

        alignmentCheckbox.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (onChangeCommand != null) {
                    onChangeCommand.execute();
                }
            }
        });

        typeDropdown.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (onChangeCommand != null) {
                    onChangeCommand.execute();
                }
            }
        });

        labelTextBox.setPlaceholder("Enter Label");
        labelTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (event.getValue().contains(Constants.PIPE)) {
                    labelTextBox.setValue(event.getValue().replaceAll(Constants.BACKWARD_SLASH + Constants.PIPE, ""));
                }

                if (onChangeCommand != null) {
                    onChangeCommand.execute();
                }
            }
        });

        typeDropdown.setValue(defaultValue.getDisplayName());

        // default label to blank
        setLabel("");

        List<String> displayNames = new ArrayList<>();
        /* Question bank does not support Free Text */
        if (!isInQuestionBank) {
            displayNames.add(SurveyResponseTypeEnum.FREE_TEXT.getDisplayName());
        }
        displayNames.add(SurveyResponseTypeEnum.NUMERIC.getDisplayName());
        typeDropdown.setAcceptableValues(displayNames);
    }

    /**
     * Sets the name of the widget.
     * 
     * @param name the name of the widget.
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            typeLabel.setText(LABEL_STR + " " + name);
        }
    }

    /**
     * Gets the string representation of the type value.
     * 
     * @return the type value string.
     */
    public String getTypeValue() {
        if (StringUtils.isBlank(typeDropdown.getValue())) {
            throw new NullPointerException("Trying to retrieve a null value for the free text response dropdown");
        }

        return typeDropdown.getValue();
    }
    
    /**
     * Gets the boolean value for if the label is left aligned.
     * 
     * @return true if the checkbox is checked (meaning the label should be left aligned); false
     *         otherwise.
     */
    public Boolean isLeftAligned() {
        return alignmentCheckbox.getValue();
    }

    /**
     * Sets the alignment for the textbox label.
     * 
     * @param leftAligned true to left align the label; false to right align.
     */
    public void setLeftAligned(boolean leftAligned) {
        alignmentCheckbox.setValue(leftAligned);
    }

    /**
     * Sets the widget's label value.
     * 
     * @param label the label value.
     */
    public void setLabel(String label) {

        if (label == null) {
            label = "";
        }

        labelTextBox.setValue(label.trim());
    }

    /**
     * Gets the label value.
     * 
     * @return the label value.
     */
    public String getLabelValue() {
        return labelTextBox.getValue() == null ? "" : labelTextBox.getValue();
    }
}
