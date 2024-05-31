/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.draw;
 
/**
 * A callback used to notify listeners when an {@link AbstractMapShape}'s rendered appearance (i.e. color, location, etc.) is changed
 * 
 * @author nroberts
 *
 */
public interface ShapeDrawnCallback {

    /**
     * Performs logic when a shape's rendered appearance is changed
     * 
     * @param shape the shape that was changed
     */
    public void onShapeDrawn(AbstractMapShape<?> shape);
}
