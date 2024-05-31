/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.draw;

import com.google.gwt.user.client.Command;

import mil.arl.gift.tools.map.client.AbstractMapPanel;

/**
 * An abstract representation of a shape that can be drawn to a map
 *
 * @author nroberts
 * @param <T> The type of panel that the shape exists within
 */
public abstract class AbstractMapShape<T extends AbstractMapPanel> {

    /** The default color to use when creating a new shape*/
    public static String DEFAULT_SHAPE_COLOR = "red";

    /** The color that should be used when rendering this shape */
    private String color = DEFAULT_SHAPE_COLOR;

    /** The display name associated with this shape */
    private String name = null;

    /** The map implementation used to render this shape */
    protected T mapImpl;

    /** A command to be invoked when this shape is edited */
    private Command onEditCommand;

    /** A command to be invoked when this shape is clicked on */
    private Command onClickCommand;

    /** A callback to be invoked when this shape is drawn */
    private ShapeDrawnCallback drawCallback;

    /**
     * Creates a shape that renders to the given map implementation
     *
     * @param mapImpl the map implementation that should be used to render this shape
     */
    protected AbstractMapShape(T mapImpl) {
        this.mapImpl = mapImpl;
    }

    /**
     * Gets the color that should be used when rendering this shape
     *
     * @return the color of this shape
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the the color that should be used when rendering this shape. Note that this color will not be reflected visually
     * on the map until this shape is redrawn.
     *
     * @param color the color this shape should use
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Gets the display name associated with this shape
     *
     * @return the shape's display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name to associate with this shape
     *
     * @param name the shape's display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Draws this shape to the appropriate map. If this shape has already been drawn, the existing drawing
     * will be updated to match this shape's current state.
     */
    public abstract void draw();

    /**
     * Erases this shape from the appropriate map. If this shape has not been drawn, this method will do nothing.
     */
    public abstract void erase();

    /**
     * Allows this shape to be edited by the author (i.e. drag, move/add/remove vertices, etc.) and invokes the given
     * command whenever the author makes an edit.
     *
     * @param onEdit the command to invoke whenever the author makes an edit
     */
    public void enableEditing(Command onEdit) {
        this.onEditCommand = onEdit;
        setEditable(true);
    }

    /**
     * Prevents this shape from being edited by the author. Editing is disabled on all shapes by default, so this method will
     * have no effect unless {@link #enableEditing(Command)} was called beforehand.
     */
    public void disableEditing() {
        this.onEditCommand = null;
        setEditable(false);
    }

    /**
     * Sets whether or not this shape should be editable by the author
     *
     * @param editable whether this shape should be editable
     */
    protected abstract void setEditable(boolean editable);

    /**
     * Notifies the appropriate listener that this shape was edited using the command specified by {@link #enableEditing(Command)}
     */
    protected void onEdit() {

        if(this.onEditCommand != null) {
            this.onEditCommand.execute();
        }
    }

    /**
     * If a command is provided, makes this shape clickable and invokes the given command when it is clicked. If no command is
     * provided, then this shape will be made unclickable.
     *
     * @param onClick the command to execute when this shape is clicked on. If null, the shape will be unclickable and do
     * nothing when clicked on
     */
    public void setClickCommand(Command onClick) {

        this.onClickCommand = onClick;
        setClickable(onClick != null);
    }

    /**
     * Sets whether or not this shape should be clickable
     *
     * @param clickable whether this shape should be clickable
     */
    protected abstract void setClickable(boolean clickable);

    /**
     * If this shape is clickable, executes the command specified by {@link #setClickCommand(Command)}
     */
    protected void onClick() {

        if(this.onClickCommand != null) {
            this.onClickCommand.execute();
        }
    }
    
    /**
     * Gets whether or not this shape is currently drawn to the appropriate map.
     * 
     * @return whether this shape is drawn
     */
    public abstract boolean isDrawn();
    
    /**
     * Assigns a callback to this shape that will be invoked whenever the shape is redrawn (i.e. whenever its rendered appearance
     * is changed)
     * 
     * @param callback the callback to invoke. If null, no logic will be performed when this shape is redrawn.
     */
    public void setDrawCallback(ShapeDrawnCallback callback) {
        this.drawCallback = callback;
    }
    
    /**
     * Invokes the callback supplied by {@link #setDrawCallback(ShapeDrawnCallback)}, if such a callback has been defined
     */
    protected void onDraw() {
        
        if(this.drawCallback != null) {
            this.drawCallback.onShapeDrawn(this);
        }
    }
}
