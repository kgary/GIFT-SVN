/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.draggableFlexTable;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.AbstractPositioningDropController;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.LocationWidgetComparator;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.view.util.draggableFlexTable.DraggableFlexTable.MoveCallback;

/**
 * Allows one or more table rows to be dropped into an existing table.
 */
public class FlexTableRowDropController extends AbstractPositioningDropController {

    /** The css string for the positioner (indicator of where a row will be dropped) */
    private static final String CSS_TABLE_POSITIONER = "table-positioner";

    /** The draggable {@link FlexTable} */
    private DraggableFlexTable flexTable;

    /** The list of {@link MoveCallback callbacks} to trigger when a row is moved */
    private List<MoveCallback> moveCallbacks = new ArrayList<MoveCallback>();

    /** An indexed panel used to retrieve the {@link #flexTable flex table's} widgets */
    private InsertPanel flexTableRowsAsIndexPanel = new InsertPanel() {
        @Override
        public void add(Widget w) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Widget getWidget(int index) {
            return flexTable.getWidget(index, 0);
        }

        @Override
        public int getWidgetCount() {
            return flexTable.getRowCount();
        }

        @Override
        public int getWidgetIndex(Widget child) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(Widget w, int beforeIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(int index) {
            throw new UnsupportedOperationException();
        }
    };

    /** An indicator of where a row will be dropped */
    private Widget positioner = null;

    /** The index of the row where the drop event occurred */
    private int targetRowIndex;

    /**
     * Constructor.
     * 
     * @param flexTable The {@link DraggableFlexTable}
     */
    public FlexTableRowDropController(DraggableFlexTable flexTable) {
        super(flexTable);
        this.flexTable = flexTable;
    }

    @Override
    public void onDrop(DragContext context) {
        FlexTableRowDragController trDragController = (FlexTableRowDragController) context.dragController;
        FlexTableUtil.moveRow(trDragController.getDraggableTable(), flexTable, trDragController.getDragRowIndex(),
                targetRowIndex + 1);
        super.onDrop(context);

        // fire callbacks
        if (!moveCallbacks.isEmpty()) {
            int draggedRow = trDragController.getDragRowIndex();
            boolean sameTable = trDragController.getDraggableTable() == flexTable;
            if (sameTable) {
                int droppedRow = (draggedRow > targetRowIndex) ? targetRowIndex + 1 : targetRowIndex;
                if (draggedRow != droppedRow) {
                    for (MoveCallback callback : moveCallbacks) {
                        callback.onMoveWithinTable(flexTable, draggedRow, droppedRow);
                    }
                }
            } else {
                for (MoveCallback callback : moveCallbacks) {
                    callback.onMoveOut(trDragController.getDraggableTable(), draggedRow);
                    callback.onMoveIn(flexTable, targetRowIndex + 1);
                }
            }
        }
    }

    @Override
    public void onEnter(DragContext context) {
        super.onEnter(context);
        positioner = newPositioner();
    }

    @Override
    public void onLeave(DragContext context) {
        positioner.removeFromParent();
        positioner = null;
        super.onLeave(context);
    }

    @Override
    public void onMove(DragContext context) {
        super.onMove(context);

        // update positioner location
        if (flexTable.getRowCount() > 0) {
            Location tableLocation = new WidgetLocation(flexTable, context.boundaryPanel);

            targetRowIndex = DOMUtil.findIntersect(flexTableRowsAsIndexPanel,
                    new CoordinateLocation(context.mouseX, context.mouseY),
                    LocationWidgetComparator.BOTTOM_HALF_COMPARATOR) - 1;

            int topLocation;
            TableSectionElement tbody = TableElement.as(flexTable.getElement()).getTBodies().getItem(0);
            if (targetRowIndex < 0) {
                topLocation = tbody.getRows().getItem(0).getOffsetTop() - (flexTable.getVerticalBorderSpacing() / 2);
            } else {
                TableRowElement tr = tbody.getRows().getItem(targetRowIndex);
                topLocation = tr.getOffsetTop() + tr.getOffsetHeight() + (flexTable.getVerticalBorderSpacing() / 2);
            }

            context.boundaryPanel.add(positioner, tableLocation.getLeft(), topLocation);
        }
    }

    /**
     * Create the positioner.
     * 
     * @return the new positioner
     */
    private Widget newPositioner() {
        Widget p = new SimplePanel();
        p.addStyleName(CSS_TABLE_POSITIONER);
        p.setPixelSize(flexTable.getOffsetWidth(), 1);
        return p;
    }

    /**
     * Add a callback listener for when a row is moved.
     * 
     * @param moveCallback the {@link MoveCallback} listener.
     */
    public void addMoveCallback(MoveCallback moveCallback) {
        this.moveCallbacks.add(moveCallback);
    }
}