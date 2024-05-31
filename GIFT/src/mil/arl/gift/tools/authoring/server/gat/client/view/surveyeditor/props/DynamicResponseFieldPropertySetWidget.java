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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.SurveyResponseTypeEnum;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.util.StringUtils;

/**
 * The DynamicResponseFieldPropertySetWidget is responsible for displaying the controls that allow
 * the author to customize the layout of the dynamic response fields.
 * 
 * @author sharrison
 *
 */
public class DynamicResponseFieldPropertySetWidget extends AbstractPropertySetWidget {

    private static Logger logger = Logger.getLogger(DynamicResponseFieldPropertySetWidget.class.getName());

    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, DynamicResponseFieldPropertySetWidget> {
    }

    @UiField(provided = true)
    protected NumberSpinner numberOfResponses = new NumberSpinner(1, 1, 50);

    @UiField
    protected FlowPanel responsesPanel;

    @UiField(provided = true)
    protected NumberSpinner responsesPerLine = new NumberSpinner(1, 1, 10);

    /**
     * Flag indicating if the survey containing the widget is a question bank
     */
    private final boolean isInQuestionBank;

    /**
     * Constructor.
     * 
     * @param propertySet - The property set for the widget.
     * @param listener - The listener that will handle changes to the
     *        properties.
     */
    public DynamicResponseFieldPropertySetWidget(DynamicResponseFieldPropertySet propertySet,
            PropertySetListener listener) {
        this(propertySet, listener, false);
    }

    /**
     * Constructor.
     * 
     * @param propertySet - The property set for the widget.
     * @param listener - The listener that will handle changes to the properties.
     * @param isInQuestionBank - true if the widget is contained within a question bank; false otherwise.
     */
    public DynamicResponseFieldPropertySetWidget(DynamicResponseFieldPropertySet propertySet, PropertySetListener listener, boolean isInQuestionBank) {
        super(propertySet, listener);
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("DynamicResponseFieldPropertySetWidget(");
            List<Object> params = Arrays.<Object>asList(propertySet, listener, isInQuestionBank);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.isInQuestionBank = isInQuestionBank;

        numberOfResponses.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {

                updateResponseFieldList(event.getValue());

                propListener.onPropertySetChange(propSet);
            }
        });

        responsesPerLine.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {

                DynamicResponseFieldPropertySet props = (DynamicResponseFieldPropertySet) propSet;
                props.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE, event.getValue());

                propListener.onPropertySetChange(propSet);
            }
        });

        if (propertySet != null) {

            Integer perLine = propertySet.getIntegerPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELDS_PER_LINE);
            if (responsesPerLine != null) {
                responsesPerLine.setValue(perLine, false);
            }

            // make sure the responses panel is empty
            while (responsesPanel.getWidgetCount() != 0) {
                responsesPanel.remove(0);
            }

            // retrieve types list from properties
            List<String> types = SurveyItemProperties
                    .decodeListString((String) propertySet.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES), Constants.COMMA);

            // retrieve labels list from properties
            List<String> labels = SurveyItemProperties
                    .decodeListString((String) propertySet.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS), Constants.PIPE);

            // retrieve labels list from properties
            List<String> alignment = SurveyItemProperties
                    .decodeListString((String) propertySet.getPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED), Constants.COMMA);

            /* Must have at least 1 response */
            boolean repopulateProperties = types.isEmpty();
            if (repopulateProperties) {
                types.add((isInQuestionBank ? SurveyResponseTypeEnum.NUMERIC : SurveyResponseTypeEnum.FREE_TEXT)
                        .getDisplayName());
                labels.add("");
                alignment.add(Boolean.FALSE.toString());
            }

            numberOfResponses.setValue(types.size(), false);

            // populate responses panel
            for (int i = 0; i < types.size(); i++) {
                String type = types.get(i);

                String label;
                if (i + 1 > labels.size()) {
                    // no label was specified... set to null
                    label = null;
                } else {
                    label = labels.get(i);
                }
                
                boolean leftAlignment;
                if (i + 1 > alignment.size()) {
                    // no alignment was specified... set to false
                    leftAlignment = false;
                } else {
                    leftAlignment = Boolean.parseBoolean(alignment.get(i));
                }

                responsesPanel.add(createFreeTextPropertySetWidget(type, label, Integer.toString(i + 1), leftAlignment));
            }

            /* Push default response to the properties */
            if (repopulateProperties) {
                repopulatePropertyLists();
            }

            propListener.onPropertySetChange(propSet);
        }
    }

    /**
     * Updates the response field list. The given number of response fields specifies what the total
     * number of response fields should be. If there are currently less than that number, then more
     * are added; if there are more than that number, then the difference is removed.
     * 
     * @param numResponseFields the total number of response fields that should exist.
     */
    private void updateResponseFieldList(Integer numResponseFields) {
        if (numResponseFields == null) {
            numResponseFields = 0;
        }

        int widgetCount = responsesPanel.getWidgetCount();

        // add more fields
        if (numResponseFields > widgetCount) {
            for (int i = widgetCount; i < numResponseFields; i++) {
                FreeTextPropertySetWidget widget = createFreeTextPropertySetWidget(null, null, Integer.toString(i + 1), false);

                responsesPanel.add(widget);
            }

            repopulatePropertyLists();
        }
        // remove fields starting from bottom
        else if (numResponseFields < widgetCount) {
            for (int i = widgetCount; i > numResponseFields; i--) {
                responsesPanel.remove(i - 1);
            }

            repopulatePropertyLists();
        }
    }

    /**
     * Creates a new {@link FreeTextPropertySetWidget}.
     * 
     * @param type the type value.
     * @param label the label value.
     * @param name the widget name identifier.
     * @param labelLeftAligned true if the label is left aligned.
     * @return {@link FreeTextPropertySetWidget}
     */
    private FreeTextPropertySetWidget createFreeTextPropertySetWidget(String type, String label, String name, boolean labelLeftAligned) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("createFreeTextPropertySetWidget(");
            List<Object> params = Arrays.<Object>asList(type, label, name, labelLeftAligned);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        SurveyResponseTypeEnum responseType;
        try {
            responseType = SurveyResponseTypeEnum.valueOf(type);
        } catch (@SuppressWarnings("unused") EnumerationNotFoundException ex) {
            // default the response type to free text
            responseType = isInQuestionBank ? SurveyResponseTypeEnum.NUMERIC : SurveyResponseTypeEnum.FREE_TEXT;
        }

        FreeTextPropertySetWidget widget = new FreeTextPropertySetWidget(propSet, propListener, isInQuestionBank, responseType,
                new Command() {
                    @Override
                    public void execute() {
                        repopulatePropertyLists();
                        propListener.onPropertySetChange(propSet);
                    }
                });
        widget.setName(name);
        widget.setLeftAligned(labelLeftAligned);

        if (label != null && !label.trim().isEmpty()) {
            widget.setLabel(label);
        }

        return widget;
    }

    /**
     * Re-populate the property values for the widgets' types and labels.
     */
    private void repopulatePropertyLists() {

        List<String> typeList = new ArrayList<String>();
        List<String> labelList = new ArrayList<String>();
        List<String> alignmentList = new ArrayList<String>();
        for (int i = 0; i < responsesPanel.getWidgetCount(); i++) {
            Widget respWidget = responsesPanel.getWidget(i);
            if (respWidget instanceof FreeTextPropertySetWidget) {
                FreeTextPropertySetWidget propWidget = (FreeTextPropertySetWidget) respWidget;
                typeList.add(propWidget.getTypeValue());
                labelList.add(propWidget.getLabelValue());
                alignmentList.add(propWidget.isLeftAligned().toString());
            }
        }

        DynamicResponseFieldPropertySet props = (DynamicResponseFieldPropertySet) propSet;
        props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_TYPES,
                SurveyItemProperties.encodeListString(typeList, Constants.COMMA));
        props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LABELS,
                SurveyItemProperties.encodeListString(labelList, Constants.PIPE));
        props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.RESPONSE_FIELD_LEFT_ALIGNED,
                SurveyItemProperties.encodeListString(alignmentList, Constants.COMMA));
    }
}
