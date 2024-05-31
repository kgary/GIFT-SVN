/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import com.google.gwt.user.client.ui.Widget;

/**
 * Populates a single column within the {@link ItemListEditor}. Contains the information necessary
 * to format the column and display a header. This class supports a view-only column.
 * 
 * @author tflowers
 *
 * @param <T> The type of the item populating the fields
 */
public abstract class ItemField<T> {

    /** The header of the column */
    private String header;

    /** The width string value (e.g. 75% or 45px) */
    private String width;

    /**
     * Constructor. Defaults to no header and 'auto' width.
     */
    public ItemField() {
    }

    /**
     * Constructor.
     * 
     * @param header the header of the column. Null displays no header.
     * @param width The width string value (e.g. 75% or 45px). Null defaults to auto.
     */
    public ItemField(String header, String width) {
        setHeader(header);
        setWidth(width);
    }

    /**
     * Retrieves the header value from the column.
     * 
     * @return the header value. Can be null.
     */
    public String getHeader() {
        return header;
    }

    /**
     * Sets the header value for the column.
     * 
     * @param header the header of the column. Null displays no header.
     */
    private void setHeader(String header) {
        this.header = header;
    }

    /**
     * Retrieves the width value from the column.
     * 
     * @return the width string value (e.g. 75% or 45px).
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the width value for the column.
     * 
     * @param width the width string value (e.g. 75% or 45px). Null defaults to auto.
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * Retrieves the widget to use when in view-only mode.
     * 
     * @param item the item to populate the view widget before it is displayed.
     * @return the populated view widget.
     */
    public abstract Widget getViewWidget(T item);
}
