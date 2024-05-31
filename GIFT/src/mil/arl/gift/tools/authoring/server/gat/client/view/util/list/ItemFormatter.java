/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import com.google.gwt.dom.client.TableRowElement;

/**
 * An interface that can be used to modify the visual format of items displayed by an item list editor
 * 
 * @author nroberts
 *
 * @param <T>
 */
public interface ItemFormatter<T> {

    /**
     * Formats the given row in an item list editor based on the given item's data
     * 
     * @param item the item whose data should be used to format the row
     * @param rowElement the row to format
     */
    public void format(T item, TableRowElement rowElement);
}
