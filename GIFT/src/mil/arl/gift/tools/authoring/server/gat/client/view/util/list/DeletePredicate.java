/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

/**
 * Predicate interface for testing an item for deletability.
 * 
 * @author sharrison
 * @param <T> The type of elements that can be tested for deletability.
 */
public interface DeletePredicate<T> {
    /**
     * Determines if the item can be deleted or not.
     * 
     * @param item the item to determine whether it is deletable.
     * @return true if the item is able to be deleted; false otherwise.
     */
    public boolean canDelete(T item);
}
