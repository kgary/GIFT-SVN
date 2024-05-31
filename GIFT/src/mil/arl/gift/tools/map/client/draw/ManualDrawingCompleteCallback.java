/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.draw;
 
/**
 * A callback used to notify listeners when the author has finished manually drawing a map shape
 * 
 * @author nroberts
 */
public interface ManualDrawingCompleteCallback {

    /**
     * Invokes logic when the author has finished manually drawing the given shape
     * 
     * @param shape the shape that the author drew
     */
    public void onDrawingComplete(AbstractMapShape<?> shape);
}
