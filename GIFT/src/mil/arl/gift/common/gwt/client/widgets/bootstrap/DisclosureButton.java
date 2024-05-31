/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.util.Iterator;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * A toggleable button that reveals widgets.
 *
 * @author tflowers
 *
 */
public class DisclosureButton extends Composite implements HasWidgets, HasValue<Boolean> {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(DisclosureButton.class.getName());

    /** The binder which combines this java file with the ui.xml file */
    private static DisclosureButtonUiBinder uiBinder = GWT.create(DisclosureButtonUiBinder.class);

    /** Defines the binder that combines the java file with the ui.xml file */
    interface DisclosureButtonUiBinder extends UiBinder<Widget, DisclosureButton> {
    }
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * disabled style
         * 
         * @return the style
         */
        String disabled();
    }

    /** The style */
    @UiField
    protected Style style;

    /** The tooltip that is attached to the button */
    @UiField
    protected Tooltip tooltip;

    /** The button that when toggled, shows and hides {@link #childWidgets} */
    @UiField
    protected Button toggleButton;

    /** The panel that contains the widgets to be shown and hidden */
    @UiField
    protected FlowPanel childWidgets;

    /**
     * The bootstrap style name to be applied to the button when it has been
     * toggled on
     */
    private static final String TOGGLE_ON_STYLE = "active";

    /**
     * Constructs a new Disclosure panel
     */
    public DisclosureButton() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * The event handler that shows and hides the {@link #childWidgets} panel.
     *
     * @param event the click event
     */
    @UiHandler("toggleButton")
    protected void onCheckButtonClicked(ClickEvent event) {
        event.stopPropagation();
        setValue(!childWidgets.isVisible(), true);
    }

    @Override
    public void add(Widget child) {
        if (child == null) {
            throw new IllegalArgumentException("The parameter 'child' cannot be null.");
        }

        childWidgets.add(child);
    }

    @Override
    public boolean remove(Widget child) {
        return true;
    }

    @Override
    public void clear() {

    }

    @Override
    public Iterator<Widget> iterator() {
        return childWidgets.iterator();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Boolean getValue() {
        return childWidgets.isVisible();
    }
    
    /** Removes focus from the disclosure button */
    public void blur() {
        toggleButton.getElement().blur();
    }

    @Override
    public void setValue(Boolean newValue) {
        setValue(newValue, false);
    }

    @Override
    public void setValue(Boolean newValue, boolean fireEvents) {
        if (childWidgets.isVisible() != newValue) {
            
            logger.info("Changing the child widget visiblity to "+newValue);
            /* Update the visibility */
            childWidgets.setVisible(newValue);

            /* Fire events if requested. */
            if (fireEvents) {
                ValueChangeEvent.fire(this, newValue);
            }
        }

        /* Ensure the visual elements are in the correct state for the given
         * value. */
        if (newValue) {
            toggleButton.addStyleName(TOGGLE_ON_STYLE);

            int buttonHeight = toggleButton.getOffsetHeight();
            childWidgets.setHeight(buttonHeight + "px");
        } else {
            toggleButton.removeStyleName(TOGGLE_ON_STYLE);
        }
    }

    /**
     * Sets the icon that is shown within the toggle button.
     *
     * @param iconType The {@link IconType} to set the button's icon to.
     */
    public void setIcon(IconType iconType) {
        toggleButton.setIcon(iconType);
    }

    /**
     * Sets the size of the icon that is shown within the toggle button.
     *
     * @param iconSize The {@link IconSize} to set the button's icon size to.
     */
    public void setIconSize(IconSize iconSize) {
        toggleButton.setIconSize(iconSize);
    }

    /**
     * Updates the tooltip of the {@link #toggleButton}.
     *
     * @param tooltipText the new text to be used for the
     *        {@link #toggleButton}'s tooltip. A null value will hide the
     *        tooltip.
     */
    public void setTooltip(String tooltipText) {
        if (tooltipText != null) {
            tooltip.setTrigger(Trigger.HOVER);
            tooltip.setTitle(tooltipText);
        } else {
            tooltip.setTrigger(Trigger.MANUAL);
            tooltip.hide();
        }
    }
    
    /**
     * Set the components managed by this class to read only mode.
     * 
     * @param readOnly true if changing to read only mode
     */
    public void setReadOnly(boolean readOnly){
        logger.info("changing to read only = "+readOnly);
        toggleButton.setEnabled(!readOnly);
        if (readOnly) {
            this.addStyleName(style.disabled());
        } else {
            this.removeStyleName(style.disabled());
        }
    }
}