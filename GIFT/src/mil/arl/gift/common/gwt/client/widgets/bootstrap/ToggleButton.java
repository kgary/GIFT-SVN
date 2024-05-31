/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A modern looking toggle button.
 * 
 * @author sharrison
 */
public class ToggleButton extends Composite implements HasValue<Boolean> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ToggleButton.class.getName());

    /** The binder which combines this java file with the ui.xml file */
    private static ToggleButtonUiBinder uiBinder = GWT.create(ToggleButtonUiBinder.class);

    /** Defines the binder that combines the java file with the ui.xml file */
    interface ToggleButtonUiBinder extends UiBinder<Widget, ToggleButton> {
    }

    /** Interface to allow CSS style name access */
    public interface ToggleButtonStyle extends CssResource {
        /**
         * Styles the widget to indicate that the toggle button is 'off'.
         * 
         * @return the style name for the 'off' position.
         */
        String offBackground();

        /**
         * Styles the widget to indicate that the toggle button is 'on'.
         * 
         * @return the style name for the 'on' position.
         */
        String onBackground();
    }

    /** The style */
    @UiField
    protected ToggleButtonStyle style;

    /** The container panel for the toggle button */
    @UiField
    protected FlowPanel mainPanel;

    /** The tooltip for the {@link #preLabel} */
    @UiField
    protected ManagedTooltip preLabelTooltip;

    /** The label that is before the {@link #toggleButton} */
    @UiField
    protected InlineHTML preLabel;

    /** The toggle button */
    @UiField
    protected ToggleSwitch toggleButton;

    /** The tooltip for the {@link #postLabel} */
    @UiField
    protected ManagedTooltip postLabelTooltip;

    /** The label that is after the {@link #toggleButton} */
    @UiField
    protected InlineHTML postLabel;

    /**
     * Constructor.
     */
    public ToggleButton() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        toggleButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateLookAndFeel();
            }
        });
    }

    /**
     * Updates the UI style for the {@link #toggleButton} based on its value.
     */
    private void updateLookAndFeel() {
        if (Boolean.TRUE.equals(toggleButton.getValue())) {
            /* Set style to 'on' */
            mainPanel.removeStyleName(style.offBackground());
            mainPanel.addStyleName(style.onBackground());
        } else {
            /* Set style to 'off' */
            mainPanel.removeStyleName(style.onBackground());
            mainPanel.addStyleName(style.offBackground());
        }
    }

    /**
     * Set the title for the tooltip on the label appearing before the
     * {@link #toggleButton}.
     * 
     * @param title the text to display for the label tooltip.
     */
    public void setPreLabelTooltip(String title) {
        preLabelTooltip.setTitle(title);
        preLabelTooltip.recreate();
    }

    /**
     * Set the text for the label appearing before the {@link #toggleButton}.
     * 
     * @param text the text to display for the label. If null or empty, the
     *        label will be hidden.
     */
    public void setPreLabel(String text) {
        preLabel.setText(text);
        preLabel.setVisible(StringUtils.isNotBlank(text));
    }

    /**
     * Set the text for the label appearing before the {@link #toggleButton}.
     * 
     * @param html the text to display for the label. If null or empty, the
     *        label will be hidden.
     */
    public void setPreLabel(SafeHtml html) {
        preLabel.setHTML(html);
        preLabel.setVisible(html != null && StringUtils.isNotBlank(html.asString()));
    }

    /**
     * Set the title for the tooltip on the label appearing after the
     * {@link #toggleButton}.
     * 
     * @param title the text to display for the label tooltip.
     */
    public void setPostLabelTooltip(String title) {
        postLabelTooltip.setTitle(title);
        postLabelTooltip.recreate();
    }

    /**
     * Set the text for the label appearing after the {@link #toggleButton}.
     * 
     * @param text the text to display for the label. If null or empty, the
     *        label will be hidden.
     */
    public void setPostLabel(String text) {
        postLabel.setText(text);
        postLabel.setVisible(StringUtils.isNotBlank(text));
    }

    /**
     * Set the text for the label appearing after the {@link #toggleButton}.
     * 
     * @param html the text to display for the label. If null or empty, the
     *        label will be hidden.
     */
    public void setPostLabel(SafeHtml html) {
        postLabel.setHTML(html);
        postLabel.setVisible(html != null && StringUtils.isNotBlank(html.asString()));
    }

    /**
     * Set the label color.
     * 
     * @param color the color to set.
     */
    public void setLabelColor(String color) {
        preLabel.getElement().getStyle().setColor(color);
        postLabel.getElement().getStyle().setColor(color);
    }

    /**
     * Get the {@link Style} for the label appearing before the
     * {@link #toggleButton}.
     * 
     * @return the label's style.
     */
    public Style getPreLabelStyle() {
        return preLabel.getElement().getStyle();
    }

    /**
     * Get the {@link Style} for the label appearing after the
     * {@link #toggleButton}.
     * 
     * @return the label's style.
     */
    public Style getPostLabelStyle() {
        return postLabel.getElement().getStyle();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return toggleButton.addValueChangeHandler(handler);
    }

    @Override
    public Boolean getValue() {
        return toggleButton.getValue();
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        /* Prevent indeterminate state */
        boolean isTrue = Boolean.TRUE.equals(value);
        toggleButton.setValue(isTrue, fireEvents);
        updateLookAndFeel();
    }
    
    /**
     * Sets whether or not this button should be enabled (i.e. whether the user can interact with it)
     * 
     * @param enabled whether the button should be enabled
     */
    public void setEnabled(boolean enabled) {
        toggleButton.setEnabled(enabled);
    }
}
