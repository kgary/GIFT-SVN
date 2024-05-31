/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

import java.text.ParseException;

import mil.arl.gift.common.ert.ColumnProperties;
import mil.arl.gift.common.ert.TimeProperties;

/**
 * A widget for editing the properties of a time column
 *
 * @author jleonard
 */
public class TimePropertiesWidget extends Composite {

    private final TimeProperties properties;
    
    private final Label warning = new Label();

    /**
     * Constructor
     *
     * @param columnName The name of the column
     * @param properties The time column properties
     */
    public TimePropertiesWidget(String columnName, final TimeProperties properties) {

        this.properties = properties;

        FlowPanel container = new FlowPanel();

        Label header = new Label(columnName + " Properties");
        
        header.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

        container.add(header);

        Label example = new Label("Example Input: \"1h\", \"1m30s\", \"45s\", \"1h60ms\"");

        container.add(example);
        
        Grid filterTimeGrid = new Grid(2, 2);

        InlineLabel filterBeforeLabel = new InlineLabel("Filter Out Events Before Time");

        filterTimeGrid.setWidget(0, 0, filterBeforeLabel);

        final TextBox filterBeforeTextBox = new TextBox();

        filterBeforeTextBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {

                if (!filterBeforeTextBox.getValue().isEmpty()) {

                    try {

                        long value = ColumnProperties.parseTime(filterBeforeTextBox.getValue());

                        properties.setFilterBeforeTime(value);
                        
                        validate();

                    } catch (ParseException e) {

                        CommonResources.displayErrorDialog("User Error", "There is a problem with the filter before time.", e);
                    }

                } else {
                    
                    properties.setFilterBeforeTime(null);
                }
            }
        });
        
        if (properties.getFilterBeforeTime() != null) {

            filterBeforeTextBox.setValue(ColumnProperties.convertTimeToString(properties.getFilterBeforeTime()));
        }
        
        filterTimeGrid.setWidget(0, 1, filterBeforeTextBox);

        InlineLabel filterAfterLabel = new InlineLabel("Filter Out Events After Time");

        filterTimeGrid.setWidget(1, 0, filterAfterLabel);

        final TextBox filterAfterTextBox = new TextBox();

        filterAfterTextBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {

                if (!filterAfterTextBox.getValue().isEmpty()) {

                    try {

                        long value = ColumnProperties.parseTime(filterAfterTextBox.getValue());

                        properties.setFilterAfterTime(value);
                        
                        validate();

                    } catch (ParseException e) {

                        CommonResources.displayErrorDialog("User Error", "There is a problem with the filter after time.", e);
                    }

                } else {

                    properties.setFilterAfterTime(null);
                }
            }
        });

        if (properties.getFilterAfterTime() != null) {

            filterAfterTextBox.setValue(ColumnProperties.convertTimeToString(properties.getFilterAfterTime()));
        }
        
        filterTimeGrid.setWidget(1, 1, filterAfterTextBox);
        
        filterTimeGrid.setBorderWidth(0);
        
        filterTimeGrid.setCellPadding(3);

        container.add(filterTimeGrid);
        
        warning.getElement().getStyle().setColor("red");
        
        container.add(warning);

        this.initWidget(container);
    }
    
    private void validate() {

        if (properties.getFilterBeforeTime() != null && properties.getFilterAfterTime() != null && properties.getFilterAfterTime() < properties.getFilterBeforeTime()) {

            warning.setText("Warning: Your times overlap each other, nothing will be written in the report.");

            warning.setVisible(true);

        } else {

            warning.setVisible(false);

            warning.setText("");
        }
    }
}
