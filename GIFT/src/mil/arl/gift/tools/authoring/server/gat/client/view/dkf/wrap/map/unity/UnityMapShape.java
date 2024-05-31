/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.map.unity;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Provides a common interface for translating {@link UnityMapPanel} shapes.
 *
 * @author tflowers
 *
 */
public interface UnityMapShape {
    /**
     * Sets how much the shape is translated from its default location. This
     * method should be called when the map image moves underneath it. This
     * keeps the shape above the location that it actually represents. Always
     * translate this by the same amount that the map image has been translated.
     *
     * @param x The horizontal translation of the shape measured in pixels. A
     *        negative value translates the shape to the top and a positive
     *        value translates the shape to the bottom. A 0 value will reset the
     *        horizontal position to its default location.
     * @param y The vertical translation of the shape measured in pixels. A
     *        negative value translates the shape to the left and a positive
     *        value translates the shape to the right. A 0 value will reset the
     *        vertical position to its default location.
     */
    public void setTranslation(int x, int y);

    /**
     * Used to get the widget to which the shape is rendered.
     *
     * @return The {@link IsWidget} that is used to render the shape. Can't be
     *         null.
     */
    public IsWidget getShapeContainer();

    /**
     * Used to draw this {@link UnityMapShape} to a provided {@link Canvas}.
     *
     * @param canvas The {@link Canvas} on which to draw the
     *        {@link UnityMapShape}
     * @param canvasIsTranslated A flag indicating whether the canvas has
     *        already been translated to reflect the position of this
     *        {@link UnityMapShape}.
     * @param applyEdits A flag indicating whether or not the rendered state of
     *        this {@link UnityMapShape} should be pushed to its backing data
     *        model as well.
     */
    public void render(Canvas canvas, boolean canvasIsTranslated, boolean applyEdits);
}
