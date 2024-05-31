/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * Defines a generic list widget that allows for:
 * <ul>
 * <li>Actions to be called on each of its children</li>
 * <li>Dynamic buttons above the list</li>
 * <li>Dynamic buttons below the list</li>
 * </ul>
 * 
 * @author tflowers
 *
 * @param <T> The type of the list elements
 */
public class GenericListEditor<T> extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(GenericListEditor.class.getName());

    /** The ui binder that combines this java class with the ui.xml */
    private static GenericListEditorUiBinder uiBinder = GWT.create(GenericListEditorUiBinder.class);

    /** Defines the ui binder that combines the java class with the ui.xml */
    interface GenericListEditorUiBinder extends UiBinder<Widget, GenericListEditor<?>> {
    }

    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The css for the rows in the {@link #entryTable}
         * 
         * @return the css value
         */
        String entryTableRow();
    }

    /** The css Styles declared in the ui.xml */
    @UiField
    protected Style style;

    /** Background container for the {@link #topButtons} panel */
    @UiField
    protected HTMLPanel topButtonsContainer;

    /** Contains each of the buttons that are above the {@link #entryTable} */
    @UiField
    protected Panel topButtons;

    /** Background container for the {@link #bottomButtons} panel */
    @UiField
    protected HTMLPanel bottomButtonsContainer;

    /** Contains each of the buttons that are below the {@link #entryTable} */
    @UiField
    protected Panel bottomButtons;

    /** Placeholder panel for when there are no entries in the {@link #entryTable} */
    @UiField
    protected HTML placeholderEntry;

    /** HTML text above the {@link #entryTable} for descriptive or informative text */
    @UiField
    protected HTML tableLabel;

    /** Renders each of the entries within {@link GenericListEditor} */
    @UiField
    protected CellTable<T> entryTable;

    /**
     * Contains each of the {@link GenericListEditor}'s entries to be rendered by
     * {@link #entryTable}
     */
    private ListDataProvider<T> listDataProvider = new ListDataProvider<>();

    /** The object that is used to get the string representation of each of the list items. */
    private Stringifier<T> entryTableStringifier;

    /** The handler registration for the row action */
    private HandlerRegistration rowActionHandler;

    /** The placeholder text to display to the user when the item table is empty */
    private static final String DEFAULT_PLACEHOLDER = "There are no entries in this table.";

    /**
     * The default constructor that initializes the widget
     * 
     * @param stringifier The object that is used to get the string representation of each of the
     *        list items.
     */
    public GenericListEditor(Stringifier<T> stringifier) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        if (stringifier == null) {
            throw new IllegalArgumentException("The parameter stringifier can't be null.");
        }

        entryTableStringifier = stringifier;

        placeholderEntry.setText(DEFAULT_PLACEHOLDER);
        placeholderEntry.setVisible(true);

        // hide button container by default
        topButtonsContainer.setVisible(false);
        bottomButtonsContainer.setVisible(false);

        // populate the columns
        setColumnActions(null);

        /* the cell table should be 'read-only' so remove the selection model and keyboard
         * selections */
        entryTable.setSelectionModel(new NoSelectionModel<T>());
        entryTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

        entryTable.setRowStyles(new RowStyles<T>() {
            @Override
            public String getStyleNames(T arg0, int arg1) {
                return style.entryTableRow();
            }
        });

        // Bind the list to reflect the items in the list data provider
        listDataProvider.addDataDisplay(entryTable);
    }

    /**
     * Builds the columns for the entry table. Columns will contain the stringified entry and one
     * for each action.
     * 
     * @param actions The actions that can be performed on each element within the list. Null is
     *        treated as an empty collection.
     */
    public void setColumnActions(Collection<ItemAction<T>> actions) {

        // ensure no NPE
        if (actions == null) {
            actions = new ArrayList<ItemAction<T>>();
        }

        // clear columns
        while (entryTable.getColumnCount() > 0) {
            entryTable.removeColumn(0);
        }

        // build columns...

        // Add the column that identifies the entry in the list
        entryTable.addColumn(new Column<T, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(T item) {
                // stringifier can't be null here because it is required in the constructor.
                return SafeHtmlUtils.fromTrustedString(entryTableStringifier.stringify(item));
            }

        });

        entryTable.setColumnWidth(0, "100%");

        // Create a column for each of the actions, if any
        for (final ItemAction<T> action : actions) {
            ButtonCell buttonCell = new ButtonCell() {
                @Override
                public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {

                    SafeHtml html = SafeHtmlUtils.fromTrustedString(value);
                    sb.append(html);
                }
            };

            Column<T, String> column = new Column<T, String>(buttonCell) {
                @Override
                public String getValue(T item) {
                    Icon icon = new Icon(action.getIconType(item));
                    icon.setSize(IconSize.LARGE);
                    icon.setTitle(action.getTooltip(item));
                    icon.getElement().getStyle().setCursor(action.isEnabled(item) ? Cursor.POINTER : Cursor.DEFAULT);

                    // set opacity to indicate enabled vs disabled
                    icon.getElement().getStyle().setOpacity(action.isEnabled(item) ? 1.0f : .5f);

                    // Add padding to make the icon easier to click
                    icon.getElement().getStyle().setPadding(10, Unit.PX);

                    return icon.toString();
                }
            };

            column.setFieldUpdater(new FieldUpdater<T, String>() {

                @Override
                public void update(int index, T item, String value) {
                    if (action.isEnabled(item)) {
                        action.execute(item);
                    }
                }
            });

            entryTable.addColumn(column);
        }
    }

    /**
     * Sets the buttons that are displayed above the list of items in the table.
     * 
     * @param buttons the buttons to display. This will replace any existing buttons. Null is
     *        treated as an empty collection.
     */
    public void setTopButtons(Iterable<EnforcedButton> buttons) {
        topButtonsContainer.clear();
        topButtons.clear();
        if (buttons == null) {
            topButtonsContainer.setVisible(false);
            return;
        }

        for (EnforcedButton button : buttons) {
            button.setType(ButtonType.SUCCESS);
            button.setMarginLeft(2.5);
            button.setMarginRight(2.5);
            button.setMarginBottom(1);
            button.setMarginTop(1);
            topButtons.add(button);
        }

        if (topButtons.getWidgetCount() == 0) {
            topButtonsContainer.setVisible(false);
        } else {
            topButtonsContainer.setVisible(true);
            topButtonsContainer.add(topButtons);
        }
    }

    /**
     * Sets the buttons that are displayed below the list of items in the table.
     * 
     * @param buttons the buttons to display. This will replace any existing buttons. Null is
     *        treated as an empty collection.
     */
    public void setBottomButtons(Iterable<EnforcedButton> buttons) {
        bottomButtonsContainer.clear();
        bottomButtons.clear();
        if (buttons == null) {
            bottomButtons.setVisible(false);
            return;
        }

        for (EnforcedButton button : buttons) {
            button.setType(ButtonType.INFO);
            button.setMarginLeft(2.5);
            button.setMarginRight(2.5);
            button.setMarginBottom(1);
            button.setMarginTop(1);
            bottomButtons.add(button);
        }

        if (bottomButtons.getWidgetCount() == 0) {
            bottomButtonsContainer.setVisible(false);
        } else {
            bottomButtonsContainer.setVisible(true);
            bottomButtonsContainer.add(bottomButtons);
        }
    }

    /**
     * Replaces the collection of items contained within the {@link GenericListEditor}. Clears the
     * old items before adding the new items.
     * 
     * @param items The new items to display. Null is treated as empty.
     */
    public void replaceItems(Collection<T> items) {
        listDataProvider.getList().clear();

        if (items != null && !items.isEmpty()) {
            listDataProvider.getList().addAll(items);
        }

        int size = size();
        placeholderEntry.setVisible(size == 0);

        refresh();
    }

    /**
     * Adds the item to the list of items contained within the {@link GenericListEditor}.
     * 
     * @param item The new item to display. If the item is null or is already in the list, then it
     *        will not be added.
     */
    public void addItem(T item) {
        // null or already in the list
        if (item == null || listDataProvider.getList().contains(item)) {
            return;
        }

        listDataProvider.getList().add(item);

        if (placeholderEntry.isVisible()) {
            placeholderEntry.setVisible(false);
        }

        refresh();
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation).
     * Shifts the element currently at that position (if any) and any subsequent elements to the
     * right (adds one to their indices).
     * 
     * @param index index at which the specified element is to be inserted. Negative values will
     *        assume insert at the beginning. Values greater than the size of the list will assume
     *        insert at the end.
     * @param item The new item to display. If the item is null or is already in the list, then it
     *        will not be added.
     */
    public void addItem(int index, T item) {
        // null or already in the list
        if (item == null || listDataProvider.getList().contains(item)) {
            return;
        }

        if (index < 0) {
            index = 0;
        } else if (index >= listDataProvider.getList().size()) {
            addItem(item);
            return;
        }

        listDataProvider.getList().add(index, item);
        placeholderEntry.setVisible(false);
        refresh();
    }

    /**
     * Replaces the item at the specified index in the list of items contained within the
     * {@link GenericListEditor}.
     * 
     * @param index the index of the item in the table list that will be replaced with the given
     *        item.
     * @param item The new item to display. If the item is null or is already in the list, then it
     *        will not be added.
     * @return true if the index is valid and the given item is not null e.g. the replace occurred
     *         successfully; false if the original table list was not modified.
     */
    public boolean replaceItem(int index, T item) {
        // null or already in the list, or if the provided index is out of bounds
        if (item == null || listDataProvider.getList().contains(item) || index < 0 || index >= size()) {
            return false;
        }

        listDataProvider.getList().set(index, item);
        refresh();

        return true;
    }

    /**
     * Removes the item contained within the {@link GenericListEditor}.
     * 
     * @param item the item to remove. Null is ignored.
     */
    public void removeItem(T item) {
        int size = size();
        if (item != null) {
            listDataProvider.getList().remove(item);

            if (size == 0) {
                placeholderEntry.setVisible(true);
            }
            
            refresh();
        }
    }

    /**
     * Refreshes the UI to reflect any mutations to the objects within the underlying collection
     */
    public void refresh() {
        listDataProvider.refresh();
        listDataProvider.flush();
    }

    /**
     * Sets the HTML to be displayed above the item table.
     * 
     * @param html the String value
     */
    public void setTableLabel(String html) {
        setTableLabel(SafeHtmlUtils.fromTrustedString(html));
    }

    /**
     * Sets the HTML to be displayed above the item table.
     * 
     * @param html the SafeHtml value
     */
    public void setTableLabel(SafeHtml html) {
        this.tableLabel.setHTML(html);
        this.tableLabel.setVisible(StringUtils.isNotBlank(html.asString()));
    }

    /**
     * Sets the row action for the table.
     * 
     * @param rowAction the row action. If null, any existing row action will be removed.
     */
    public void setRowAction(final ItemAction<T> rowAction) {
        if (rowActionHandler != null) {
            rowActionHandler.removeHandler();
        }

        if (rowAction == null) {
            entryTable.getElement().getStyle().setCursor(Cursor.DEFAULT);
            return;
        }

        entryTable.getElement().getStyle().setCursor(Cursor.POINTER);

        // make row clickable
        rowActionHandler = entryTable.addCellPreviewHandler(new Handler<T>() {
            @Override
            public void onCellPreview(CellPreviewEvent<T> event) {
                switch(event.getNativeEvent().getType()) {
                case "click":
                    // if the user clicked on the cell row
                    rowAction.execute(event.getValue());
                    break;
                case "mouseover":
                    // if the user is hovering
                    entryTable.getRowElement(event.getIndex()).getCells().getItem(event.getColumn()).setTitle(rowAction.getTooltip(event.getValue()));
                    break;
                }
            }
        });
    }

    /**
     * Returns the number of elements in the item table.
     * 
     * @return the table item count.
     */
    public int size() {
        return listDataProvider.getList().size();
    }

    /**
     * Returns the index of the first occurrence of the specified element in the table list, or -1
     * if this list does not contain the element.
     * 
     * @param item the item to find
     * @return the index of the item from the table list. Will return -1 if the item is null or is
     *         not found in the list.
     */
    public int indexOf(T item) {
        if (item == null) {
            return -1;
        }

        return listDataProvider.getList().indexOf(item);
    }

    /**
     * Sets the text to be displayed if the table is empty.
     * 
     * @param text the text to be shown. Null or empty will set the placeholder back to its default.
     */
    public void setPlaceholder(String text) {
        placeholderEntry.setText(StringUtils.isBlank(text) ? DEFAULT_PLACEHOLDER : text);
    }

    /**
     * Sorts the table using the given comparator.
     * 
     * @param sortComparator the comparator used to sort the data table entries. If null, no sort
     *        will be performed.
     */
    public void applySort(Comparator<T> sortComparator) {
        if (sortComparator == null) {
            return;
        }

        Collections.sort(listDataProvider.getList(), sortComparator);
        refresh();
    }

    /**
     * Returns the items in the editor. BE CAREFUL! The items are being used by reference in the
     * list, so modifying the items will impact this editor.
     * 
     * @return the list of elements in the editor.
     */
    public Iterable<T> getItems() {
        // not returning the data provider list so that they can't add/remove from the list directly
        return new ArrayList<T>(listDataProvider.getList());
    }
    
    /**
     * Redraws the row in the table that contains the provided item.
     * 
     * @param item the item that is being used within one of the table rows.
     */
    public void redrawItem(T item) {

        int index = listDataProvider.getList().indexOf(item);
        
        if(index > -1) {
            entryTable.redrawRow(index);
        }
    }
}