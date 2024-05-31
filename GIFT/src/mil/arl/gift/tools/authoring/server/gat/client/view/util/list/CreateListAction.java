/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

/**
 * Describes how create a new element in the {@link List} for an
 * {@link ItemListEditor}.
 * 
 * @author tflowers
 *
 * @param <T> The type of element that the {@link CreateListAction} makes.
 */
public interface CreateListAction<T> {
    /**
     * The method that creates the new element to add to the {@link List}.
     * 
     * @return The new element to add to the {@link List}. If null, the value is
     *         not added to the list.
     */
    T createDefaultItem();
}
