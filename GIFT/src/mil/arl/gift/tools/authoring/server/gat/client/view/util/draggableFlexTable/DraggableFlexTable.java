/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.draggableFlexTable;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * An extension of {@link FlexTable} that permits the rows to be draggable within the
 * {@link FlexTable} itself or to an external {@link FlexTable} that is within the same
 * {@link AbsolutePanel}.
 * 
 * @author sharrison
 */
public class DraggableFlexTable extends FlexTable {
    
    /**
     * A helper panel used to wrap each widget added to the flex table. The drag-and-drop behavior requires that the widget
     * possessing the drag handle implements HasMouseDownHandlers, so this widget exists to ensure that widgets that do
     * not implement this interface can still be dragged in the table. It also automatically fills all of the remaining width
     * in a table's cell so that the user can still drag an item using the whitespace around it.
     * 
     * @author nroberts
     */
    private class DraggableWrapperPanel extends FocusPanel{
        
        /**
         * Creates a new draggable wrapper that wraps the given wiget so that it can be dragged in the table
         * 
         * @param widget the widget to wrap
         */
        public DraggableWrapperPanel(Widget widget) {
            super(widget);
            
            setWidth("100%");
            getElement().getStyle().setProperty("outline", "none"); //remove the outline normally shown when focusing
        }
    }

    /** The callback that fires when a row move occurs. */
    public interface MoveCallback {
        /**
         * Executes some logic when a row is moved out of the provided {@link FlexTable}.
         * 
         * @param table the table that the row was removed from.
         * @param rowIndex the index of the row that was removed.
         */
        public void onMoveOut(FlexTable table, int rowIndex);

        /**
         * Executes some logic when a row is moved into the the provided {@link FlexTable}.
         * 
         * @param table the table that the row was inserted into.
         * @param rowIndex the index of the row that was inserted.
         */
        public void onMoveIn(FlexTable table, int rowIndex);

        /**
         * Executes some logic when a row changes position within the provided {@link FlexTable}.
         * 
         * @param table the table that contains the moved row.
         * @param startIndex the index of the starting location of the row.
         * @param endIndex the index of the ending location of the row.
         */
        public void onMoveWithinTable(FlexTable table, int startIndex, int endIndex);
    }

    /** The css string for the {@link FlexTable} */
    private static final String CSS_DRAGGABLE_FLEX_TABLE = "draggableFlexTable";

    /**
     * The vertical border spacing that is declared in the {@link #CSS_DRAGGABLE_FLEX_TABLE css} (in
     * pixels).
     */
    private static final int VERTICAL_BORDER_SPACING = 10;

    /** The drag controller for the {@link FlexTable} rows */
    private FlexTableRowDragController dragController;

    /** The drop controller for the {@link FlexTable} rows */
    private FlexTableRowDropController dropController;

    /**
     * Flag that indicates if this {@link DraggableFlexTable} is draggable or not. Default is true.
     */
    private boolean draggable = true;

    /**
     * Constructor.
     * 
     * @param absolutePanel the absolute panel used as a drag boundary.
     */
    public DraggableFlexTable(AbsolutePanel absolutePanel) {
        super();
        if (absolutePanel == null) {
            throw new IllegalArgumentException("The parameter 'absolutePanel' cannot be null.");
        }

        setStyleName(CSS_DRAGGABLE_FLEX_TABLE);
        dragController = new FlexTableRowDragController(absolutePanel);
        dragController.setBehaviorDragProxy(true);
        dragController.setBehaviorMultipleSelection(false);
        dragController.setConstrainWidgetToBoundaryPanel(true);
        dragController.setBehaviorConstrainedToBoundaryPanel(true);
        dragController.setBehaviorScrollIntoView(false);
        
        //require the user to move their mouse a few pixels before dragging so mouse down events can still be handled separately
        dragController.setBehaviorDragStartSensitivity(5);

        dropController = new FlexTableRowDropController(this);
        addDropController(dropController);
    }

    /**
     * <b>Note:</b> This subclass's implementation of this method differs slightly from that of {@link FlexTable}'s in 
     * order to support the drag-and-drop. Any widgets passed to this method will be wrapped in a helper panel that implements
     * the necessary mouse handlers and takes up the full cell width so that it the widget can be 
     * properly dragged. This can affect the results of calling {@link #getWidget(int, int)} later on since the widget 
     * returned likely will not be the same one that was passed in.
     * <br/><br/>
     * Sets the widget within the specified cell. 
     * <br/><br/>
     * Inherited implementations may either throw IndexOutOfBounds exception if the cell does not exist, or 
     * allocate a new cell to store the content. 
     * <br/><br/>
     * FlexTable will automatically allocate the cell at the correct location and then set the widget. Grid 
     * will set the widget if and only if the cell is within the Grid's bounding box. 
     * 
     * @param row the cell's row
     * @param column the cell's column
     * @param widget The widget to be added, or null to clear the cell
     */
    @Override
    public void setWidget(int row, int column, Widget widget) {
        
        Widget target = widget;
        
        if(!(widget instanceof DraggableWrapperPanel)) {
        
            // wrap the widget in a panel that implements the proper interfaces (namely HasMouseDownHandlers) and fills
            // the available cell width
            target = new DraggableWrapperPanel(widget);
        }
        
        super.setWidget(row, column, target);

        // mark new widget as draggable or not draggable
        setWidgetDraggable(target, draggable);
    }

    /**
     * Add a callback listener for when a row is moved.
     * 
     * @param moveCallback the {@link MoveCallback} listener.
     */
    public void addMoveCallback(MoveCallback moveCallback) {
        dropController.addMoveCallback(moveCallback);
    }

    /**
     * Allows for an external table within the same absolute panel to have it's row dragged onto
     * this table.
     * 
     * @param dropController the drop controller for the external table.
     */
    public void addDropController(FlexTableRowDropController dropController) {
        if (dropController == null) {
            throw new IllegalArgumentException("The parameter 'dropController' cannot be null.");
        } else if (dragController == null) {
            throw new UnsupportedOperationException(
                    "The init method for the DraggableFlexTable needs to be called before any other operation.");
        }

        dragController.registerDropController(dropController);
    }

    /**
     * Retrieves the drag controller for this table.
     * 
     * @return the {@link FlexTableRowDragController} instantiated by this table.
     */
    public FlexTableRowDragController getRowDragController() {
        return dragController;
    }

    /**
     * Retrieves the drop controller for this table.
     * 
     * @return the {@link FlexTableRowDropController} instantiated by this table.
     */
    public FlexTableRowDropController getRowDropController() {
        return dropController;
    }

    /**
     * Retrieves the vertical border spacing size (in pixels).
     * 
     * @return the {@link #VERTICAL_BORDER_SPACING}.
     */
    public int getVerticalBorderSpacing() {
        return VERTICAL_BORDER_SPACING;
    }

    /**
     * Set the {@link DraggableFlexTable} draggable or not draggable. This will update any existing
     * widgets within the table.
     * 
     * @param draggable true to make the table draggable; false otherwise.
     */
    public void setDraggable(boolean draggable) {
        // no change
        if (this.draggable == draggable) {
            return;
        }

        this.draggable = draggable;

        for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < getCellCount(rowIndex); columnIndex++) {
                // mark new widget as draggable or not draggable
                setWidgetDraggable(getWidget(rowIndex, columnIndex), draggable);
            }
        }
    }

    /**
     * Sets the widget to be draggable or not draggable.
     * 
     * @param widget the widget to update.
     * @param draggable true to make the widget draggable; false otherwise.
     */
    private void setWidgetDraggable(Widget widget, boolean draggable) {
        if (widget != null) {
            if (draggable) {
                dragController.makeDraggable(widget);
            } else if (dragController.isWidgetDraggable(widget)) {
                dragController.makeNotDraggable(widget);
            }
        }
    }
}
