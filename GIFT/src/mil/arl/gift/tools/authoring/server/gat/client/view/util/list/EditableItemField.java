/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.validation.HasValidation;

/**
 * Populates a single column within the {@link ItemListEditor}. Contains the information necessary
 * to format the column and display a header. This class supports a viewable and editable column.
 * 
 * @author sharrison
 *
 * @param <T> The type of the item populating the fields
 */
public abstract class EditableItemField<T> extends ItemField<T> implements HasValidation {

    /**
     * Constructor. Defaults to no header and 'auto' width.
     */
    public EditableItemField() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param header the header of the column. Null displays no header.
     * @param width The width string value (e.g. 75% or 45px). Null defaults to auto.
     */
    public EditableItemField(String header, String width) {
        super(header, width);
    }

    /**
     * Retrieves the widget to use when in edit mode.
     * 
     * @param item the item to populate the editable widget before it is displayed.
     * @return the populated editable widget.
     */
    public abstract Widget getEditWidget(T item);

    /**
     * Saves the edited fields from the {@link ItemField} to the provided object. None of the
     * changes in the {@link ItemField} will persist until this method is called.
     * 
     * @param obj the object to persist the changes from the {@link ItemField} to.
     */
    public abstract void applyEdits(T obj);
}
