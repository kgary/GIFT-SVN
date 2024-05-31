/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Wraps a bootstrap3 CheckBox so that we can set an indeterminate state in
 * addition to the regular true/false states.
 * 
 * @author sharrison
 */
public class ThreeStateCheckbox extends org.gwtbootstrap3.client.ui.CheckBox {

    /** The property value used to set the state as Indeterminate */
    private static final String INDETERMINATE = "indeterminate";

    /**
     * Creates a check box with the specified text label.
     * 
     * @param label the check box's label
     */
    public ThreeStateCheckbox(SafeHtml label) {
        super(label);
    }

    /**
     * Creates a check box with the specified text label.
     * 
     * @param label the check box's label
     * @param dir the text's direction. Note that {@code DEFAULT} means
     *        direction should be inherited from the widget's parent element.
     */
    public ThreeStateCheckbox(SafeHtml label, Direction dir) {
        super(label, dir);
    }

    /**
     * Creates a label with the specified text and a default direction
     * estimator.
     * 
     * @param label the check box's label
     * @param directionEstimator A DirectionEstimator object used for automatic
     *        direction adjustment. For convenience,
     *        {@link #DEFAULT_DIRECTION_ESTIMATOR} can be used.
     */
    public ThreeStateCheckbox(SafeHtml label, DirectionEstimator directionEstimator) {
        super(label, directionEstimator);
    }

    /**
     * Creates a check box with the specified text label.
     * 
     * @param label the check box's label
     */
    public ThreeStateCheckbox(String label) {
        super(label);
    }

    /**
     * Creates a check box with the specified text label.
     * 
     * @param label the check box's label
     * @param dir the text's direction. Note that {@code DEFAULT} means
     *        direction should be inherited from the widget's parent element.
     */
    public ThreeStateCheckbox(String label, Direction dir) {
        super(label, dir);
    }

    /**
     * Creates a check box with the specified text label and a default direction
     * estimator.
     * 
     * @param label the check box's label
     * @param directionEstimator A DirectionEstimator object used for automatic
     *        direction adjustment. For convenience,
     *        {@link #DEFAULT_DIRECTION_ESTIMATOR} can be used.
     */
    public ThreeStateCheckbox(String label, DirectionEstimator directionEstimator) {
        super(label, directionEstimator);
    }

    /**
     * Creates a check box with the specified text label.
     * 
     * @param label the check box's label
     * @param asHTML <code>true</code> to treat the specified label as html
     */
    public ThreeStateCheckbox(String label, boolean asHTML) {
        super(label, asHTML);
    }

    /**
     * Creates a check box
     */
    public ThreeStateCheckbox() {
        super();
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        /* Setting true or false value; remove indeterminate property */
        if (value != null) {
            inputElem.setPropertyBoolean(INDETERMINATE, false);
            super.setValue(value, fireEvents);
            return;
        }

        /* A null value is considered as indeterminate */
        Boolean oldValue = getValue();
        inputElem.setPropertyBoolean(INDETERMINATE, true);

        if (oldValue != null && fireEvents) {
            ValueChangeEvent.fire(this, null);
        }
    }

    @Override
    public Boolean getValue() {
        if (inputElem.getPropertyBoolean(INDETERMINATE) == Boolean.TRUE) {
            return null;
        } else {
            return super.getValue() == Boolean.TRUE;
        }
    }
}
