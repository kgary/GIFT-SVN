/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

/**
 * An event that notifies listeners that an item in an item list editor has been edited
 * 
 * @author cpadilla
 */
public class ItemListEditorEditEvent<T> extends ScenarioEditorEvent {
    
    /** The item that was edited */
    private T item;
    
    /**
     * Creates a new event indicating that the given place of interest was edited
     * 
     * @param place the place of interest that was edited
     */
    public ItemListEditorEditEvent(T itemEdited) {
        this.item = itemEdited;
    }

    /**
     * Gets the place of interest that was edited
     * 
     * @return the place of interest that was edited
     */
    public T getItemEdited() {
        return item;
    }

    @Override
    public String toString() {
        return new StringBuilder("[ItemListEditorEditEvent: item ='")
                .append(item)
                .append("']")
                .toString();
    }
}
