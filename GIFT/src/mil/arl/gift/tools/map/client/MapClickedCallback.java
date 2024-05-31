/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client;

import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A callback used to notify listeners when a map has been clicked on
 * 
 * @author nroberts
 */
public interface MapClickedCallback {

    /**
     * Invokes logic when a map has been clicked at a particular position
     * 
     * @param position the position on the map where it was clicked
     */
    public void onClick(AbstractMapCoordinate position);
}
