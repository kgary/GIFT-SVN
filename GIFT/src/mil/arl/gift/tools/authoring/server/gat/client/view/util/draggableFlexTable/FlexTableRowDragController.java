/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.draggableFlexTable;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows table rows to dragged by their handle.
 */
public class FlexTableRowDragController extends PickupDragController {

    /** The draggable {@link FlexTable} proxy */
    private FlexTable draggableTable;

    /** The index of the row being dragged */
    private int dragRow;

    /**
     * Constructor.
     * 
     * @param boundaryPanel the {@link AbsolutePanel} that is used as a drag boundary.
     */
    public FlexTableRowDragController(AbsolutePanel boundaryPanel) {
        super(boundaryPanel, false);
        setBehaviorDragProxy(true);
        setBehaviorMultipleSelection(false);
    }

    @Override
    public void dragEnd() {
        super.dragEnd();

        // cleanup
        draggableTable = null;
    }

    @Override
    protected Widget newDragProxy(DragContext context) {
        FlexTable proxy = new FlexTable();
        draggableTable = (FlexTable) context.draggable.getParent();
        dragRow = getWidgetRowIndex(draggableTable, context.draggable);
        FlexTableUtil.copyRow(draggableTable, proxy, dragRow, 0);
        return proxy;
    }

    /**
     * Retrieves the draggable {@link FlexTable}.
     * 
     * @return the {@link FlexTable} that contains the row that is being dragged.
     */
    FlexTable getDraggableTable() {
        return draggableTable;
    }

    /**
     * Retrieves the index of the row being dragged.
     * 
     * @return the index of the row being dragged.
     */
    int getDragRowIndex() {
        return dragRow;
    }

    /**
     * Finds the row that contains the widget being dragged.
     * 
     * @param table the {@link FlexTable} that contains the provided widget.
     * @param widget the widget to search for.
     * @return the index of the {@link FlexTable} row that contains the provided widget.
     */
    private int getWidgetRowIndex(FlexTable table, Widget widget) {
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 0; col < table.getCellCount(row); col++) {
                if (table.getWidget(row, col) == widget) {
                    return row;
                }
            }
        }
        throw new RuntimeException("Unable to determine widget row");
    }

    /**
     * Checks if the provided widget is draggable or not.
     * @param widget the widget to check.
     * @return true if the widget is draggable; false otherwise.
     */
    boolean isWidgetDraggable(Widget widget) {
        if (widget == null) {
            return false;
        }

        return widget.getElement().hasClassName(DragClientBundle.INSTANCE.css().draggable());
    }
}