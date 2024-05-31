/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ItemListEditorEditEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.draggableFlexTable.DraggableFlexTable;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.draggableFlexTable.DraggableFlexTable.MoveCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor.ValidationCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A generic class that is used to edit a {@link List} and its elements inline.
 *
 * @author tflowers
 *
 * @param <T> The type of elements contained within the list which is being
 *        edited.
 */
public class ItemListEditor<T> extends ScenarioValidationComposite {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ItemListEditor.class.getName());

    /** The ui binder that combines this java class with the ui.xml */
    private static ItemListEditorUiBinder uiBinder = GWT.create(ItemListEditorUiBinder.class);

    /** Defines the ui binder that combines the java class with the ui.xml */
    interface ItemListEditorUiBinder extends UiBinder<Widget, ItemListEditor<?>> {
    }

    /**
     * Interface used for catching {@link ListChangedEvent list changed events}.
     *
     * @author sharrison
     * @param <T> The type of elements contained within the list which is being
     *        edited.
     */
    public interface ListChangedCallback<T> {

        /**
         * Callback used when list within the {@link ItemListEditor} changes.
         *
         * @param event the event containing the list changed action and
         *        affected items.
         */
        void listChanged(ListChangedEvent<T> event);
    }

    /**
     * Interface used for handling when edits are cancelled.
     *
     * @author cpadilla
     */
    public interface EditCancelledCallback {

        /**
         * Callback used when an edit to the list within the {@link ItemListEditor}
         * is cancelled.
         */
        void editCancelled();
    }

    /**
     * The action that was delayed due to the item editor being in use.
     *
     * @author sharrison
     */
    public enum DelayedAction {
        /** Redraw the table */
        REDRAW,
        /** Refresh the table */
        REFRESH
    }

    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The css for the rows containing the inline editor in the
         * {@link #grid}
         *
         * @return the css value
         */
        String inlineEditorStyle();
    }

    /** The css Styles declared in the ui.xml */
    @UiField
    protected Style style;

    /** HTML text above the {@link #grid} for descriptive or informative text */
    @UiField
    protected HTML tableLabel;

    /**
     * The HTML containing the placeholder shown when {@link #items} is empty
     */
    @UiField
    protected HTML placeholderEntry;

    /** The absolute panel used as a drag boundary for the {@link #grid table}. */
    @UiField(provided = true)
    protected final AbsolutePanel gridAbsolutePanel = new AbsolutePanel();

    /** The grid that contains the widgets to edit elements within the list. */
    @UiField(provided = true)
    protected final DraggableFlexTable grid = new DraggableFlexTable(gridAbsolutePanel);

    /** Background container for the {@link #topButtonsPanel} */
    @UiField
    protected HTMLPanel topButtonsContainer;

    /** The panel that contains the buttons below the entries */
    @UiField
    protected Panel topButtonsPanel;

    /** The predicate for testing for deletability. Defaults to always being deletable. */
    private DeletePredicate<T> deletePredicate = new DeletePredicate<T>() {
        @Override
        public boolean canDelete(T item) {
            return true;
        }
    };

    /** The default delete {@link ItemAction} */
    private final ItemAction<T> DELETE_ACTION = new ItemAction<T>() {

        @Override
        public boolean isEnabled(T item) {
            return !isReadonly() && !isEditing() && deletePredicate.canDelete(item);
        }

        @Override
        public void execute(final T item) {
            OkayCancelDialog.show(removeItemDialogTitle, "Are you sure you want to delete "
                    + (removeItemStringifier != null ? removeItemStringifier.stringify(item) : "this item") + "?",
                    "Delete", new OkayCancelCallback() {

                        @Override
                        public void okay() {

                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("User confirmed delete");
                            }

                            remove(item);

                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Finished deleting item");
                            }
                        }

                        @Override
                        public void cancel() {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("User cancelled delete");
                            }
                        }
                    });
        }

        @Override
        public String getTooltip(T item) {
            return "Delete";
        }

        @Override
        public IconType getIconType(T item) {
            return IconType.TRASH;
        }
    };

    /** The collection of {@link ItemAction item actions} for each element. */
    private final List<ItemAction<T>> actions = new ArrayList<>();

    /** The wrapper for the {@link ItemEditor} */
    private final ItemEditorWrapper<T> editorWrapper;

    /** The element that is currently being edited. */
    private T editElement;

    /** The row index of the edited item in the grid table */
    private int editedGridRowIndex = -1;

    /** The collection of {@link ItemField item fields} for each element */
    private final List<ItemField<T>> fields = new ArrayList<>();

    /** The flag that indicates whether any fields have edit widgets */
    private boolean inlineEditorReplacesRow = false;

    /** Flag that specifies whether readonly mode is active */
    private boolean isReadonly = false;

    /**
     * Value that specifies if there is a delayed action due to a conflict with a row using the item
     * editor to edit an object.
     */
    private DelayedAction delayedAction;

    /**
     * The callbacks that will execute when the list is changed.
     */
    private Set<ListChangedCallback<T>> listChangedCallbacks = new HashSet<>();

    /**
     * The callbacks that will execute when an edit to the list is cancelled.
     */
    private Set<EditCancelledCallback> editCancelledCallbacks = new HashSet<>();

    /** The list of elements that the widget is editing. */
    private List<T> items;

    /** Flag that indicates if the rows in {@link #grid} are draggable or not. Default is false. */
    private boolean draggable = false;

    /** The placeholder text to display to the user when the item table is empty */
    private static final String DEFAULT_PLACEHOLDER = "There are no entries in this table.";

    /**
     * Maps the add button panels to their respective row element. This is necessary because the
     * flow panel is returned to this {@link ItemListEditor editor's} parent which it can use to
     * enable or disable the add button row at a later time.
     */
    private Map<FlowPanel, TableRowElement> addButtonPanelToRow = new HashMap<>();

    /**
     * The table rows that hold the table's add buttons mapped to their editable states. This state
     * indicates if the add button is editable or not. All states are editable (true) unless
     * specifically marked as not editable (false) by the {@link ItemListEditor editor's} parent.
     */
    private Map<TableRowElement, Boolean> addButtonEditableMap = new HashMap<>();

    /** The title that is shown within the delete confirmation dialog */
    private String removeItemDialogTitle = "Delete Item?";

    /** A stringifier for the Delete Item dialog to use */
    private Stringifier<T> removeItemStringifier;

    /**
     * This is a 'workaround' to marking rows as valid and invalid. The invalid rows go into this
     * validation status.
     */
    private final WidgetValidationStatus rowValidation = new WidgetValidationStatus(
            "A row in your list is invalid. Edit the invalid row to get more specific information.");

    /** A formatter that may be used to modify the appearance of this list editor's rows based on their data */
    private ItemFormatter<T> itemFormatter;

    /**
     * Constructs an {@link ItemListEditor} with no editor, and no starting elements.
     */
    public ItemListEditor() {
        this(null, new ArrayList<T>());
    }

    /**
     * Constructs an {@link ItemListEditor} with a provided {@link ItemEditor}
     * and no starting elements.
     *
     * @param editor The {@link ItemListEditor} to show inline when an element
     *        is selected. If null, then no inline editor will be shown.
     */
    public ItemListEditor(ItemEditor<T> editor) {
        this(editor, new ArrayList<T>());
    }

    /**
     * Constructs an {@link ItemListEditor} that edits a provided list of
     * elements.
     *
     * @param startingElements The {@link List} that the {@link ItemListEditor}
     *        will edit.
     */
    public ItemListEditor(List<T> startingElements) {
        this(null, startingElements);
    }

    /**
     * Constructs an {@link ItemListEditor} with a provided {@link ItemEditor}
     * and a list of elements to edit.
     *
     * @param editor The {@link ItemListEditor} to show inline when an element
     *        is selected. If null, then no inline editor will be shown.
     * @param startingElements The {@link List} that the {@link ItemListEditor}
     *        will edit.
     */
    public ItemListEditor(final ItemEditor<T> editor, List<T> startingElements) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(editor != null ? editor.getClass().getSimpleName() : null,
                    startingElements);
            logger.fine("ItemListEditor(" + StringUtils.join(", ", params) + ")");
        }

        if (startingElements == null) {
            throw new IllegalArgumentException("The parameter 'startingElements' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        /* Save references to parameters */
        if (editor != null) {
            editor.setActive(false);
        }

        setItems(startingElements);

        /* Initialize default actions */
        setActions(null);
        placeholderEntry.setText(DEFAULT_PLACEHOLDER);

        /* Subscribe to events on the editor wrapper */
        editorWrapper = new ItemEditorWrapper<>(editor, this);
        editorWrapper.addSaveHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("editorWrapper.saveButton.onMouseDown()");
                }

                /* Allows all value change handlers to fire before saving */
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        boolean allValid = true;

                        // perform item editor validation
                        if (editor != null) {
                            allValid &= editor.validateAll();
                        }

                        // perform edit field validations
                        Set<ValidationStatus> editFieldValidations = new HashSet<ValidationStatus>();
                        for (ItemField<T> field : fields) {
                            if (field instanceof EditableItemField) {
                                EditableItemField<T> editField = (EditableItemField<T>) field;
                                editFieldValidations.clear();
                                editField.getValidationStatuses(editFieldValidations);
                                for (ValidationStatus status : editFieldValidations) {
                                    editField.validate(status);
                                    allValid &= status.isValid();
                                    updateValidationStatus(status);
                                }
                            }
                        }

                        if (allValid) {
                            if (editor != null) {
                                /* sometimes the item editor needs to perform remote calls for
                                 * validation checks (e.g. name conflict) before the save can
                                 * happen. */
                                editor.performRemotePreSaveValidation(new ValidationCallback() {
                                    @Override
                                    public void validationPassed() {
                                        performSave();
                                    }
                                    
                                    @Override
                                    public void validationFailed() {
                                        /* do nothing, validation failed and it needs to be resolved
                                         * before we perform the save. */
                                    }
                                });
                            } else {
                                performSave();
                            }
                        }
                    }
                });

            }
        });

        editorWrapper.addCancelHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                cancelEditing();
            }
        });

        // hide button containers by default
        topButtonsContainer.setVisible(false);

        // default the grid table to not be draggable
        setDraggable(draggable);
        grid.addMoveCallback(new MoveCallback() {
            @Override
            public void onMoveOut(FlexTable table, int rowIndex) {
                /* This method is called when a row is dragged from this table
                 * to another table. This currently isn't supported */
            }

            @Override
            public void onMoveIn(FlexTable table, int rowIndex) {
                /* This method is called when a row is dragged to this table
                 * from another table. This currently isn't supported. */
            }

            @Override
            public void onMoveWithinTable(FlexTable table, int startIndex, int endIndex) {
                // make sure it's this table that we moved within
                if (table == grid) {
                    // moving up, -1; moving down, 1
                    int moveIncrement = (startIndex > endIndex) ? -1 : 1;
                    while (startIndex != endIndex) {
                        Collections.swap(items, startIndex, startIndex + moveIncrement);
                        startIndex += moveIncrement;
                    }

                    ListChangedEvent<T> changeEvent = new ListChangedEvent<T>(ListAction.REORDER, items);
                    fireDirtyOrExecuteListChangedCallbacks(changeEvent);
                }
            }
        });

        grid.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Cell clickedCell = grid.getCellForEvent(event);
                
                /* Check if this list editor has a dedicated item editor. If so, clicking it should begin editing. */
                boolean allowsEditing = editorWrapper.getEditor() != null;
                
                if(!allowsEditing) {
                    
                    /* Check if there are any inline editable fields */
                    for(ItemField<T> field : fields) {
                        if(field instanceof EditableItemField<?>) {
                            
                            /* An editable field was found, so allow it to be edited by clicking */
                            allowsEditing = true;
                            break;
                        }
                    }
                }


                // start editing an element whenever its row is clicked on
                if (clickedCell != null && allowsEditing && !isEditing()) {
                    editExisting(clickedCell.getRowIndex());
                }
            }
        });
    }

    /**
     * Perform the 'save' logic for the editor wrapper
     */
    private void performSave() {
        /* remove the element from the invalid list because it just passed validation (remove
         * doesn't care if the item is in the list or not) */
        rowValidation.removeElementByKey(editElement);
        requestValidation(rowValidation);

        final boolean isEditingNewElement = !items.contains(editElement);
        final ListAction actionPerformed = isEditingNewElement ? ListAction.ADD : ListAction.EDIT;
        final ListChangedEvent<T> changeEvent = new ListChangedEvent<T>(actionPerformed, editElement);

        if (isEditingNewElement) {
            items.add(editElement);
        }

        for (ItemField<T> field : fields) {
            if (field instanceof EditableItemField) {
                EditableItemField<T> editField = (EditableItemField<T>) field;
                editField.applyEdits(editElement);
            }
        }

        Command proceedCommand = new Command() {
            @Override
            public void execute() {
                /* if we have a delayed action, then stop editing will revalidate all items so we do
                 * not need to perform the validation again afterwards */
                final boolean revalidateInvalid = delayedAction == null;
                stopEditing(isEditingNewElement, true);

                if (revalidateInvalid) {
                    revalidateInvalidItems();
                }

                fireDirtyOrExecuteListChangedCallbacks(changeEvent);
            }
        };

        ItemEditor<T> editor = editorWrapper.getEditor();
        if (editor != null) {
            editor.applyEdits(editElement, proceedCommand);
        } else {
            proceedCommand.execute();
        }
    }

    /**
     * Adds a provided element to the {@link #items} and adds a new row to the
     * {@link #grid}.
     *
     * @param element The element to add to the {@link #items} and to the
     *        {@link #grid}.
     */
    public void add(T element) {

        // already in the list
        if (items.contains(element)) {
            return;
        }

        placeholderEntry.setVisible(false);

        /* Add the element to the backing list data model. */
        items.add(element);

        /* Determine the row */
        int rowListIndex = items.size() - 1;

        /* Add each field to the new row. */
        int currColumn = 0;
        for (ItemField<T> field : fields) {
            grid.setWidget(rowListIndex, currColumn++, field.getViewWidget(element));
        }

        /* Add each action to the new row. */
        for (ItemAction<T> action : actions) {
            grid.setWidget(rowListIndex, currColumn++, createIcon(element, action));
        }
        
        applyItemFormatting(element, rowListIndex);

        revalidateAllItems();

        /* Update the row tooltips */
        updateRowTooltip(rowListIndex);

        ListChangedEvent<T> changeEvent = new ListChangedEvent<T>(ListAction.ADD, element);
        fireDirtyOrExecuteListChangedCallbacks(changeEvent);
    }

    /**
     * Adds each element within a provided {@link Iterable} to the list of items
     * being edited.
     *
     * @param elements The {@link Iterable} of elements to be added to the
     *        internal {@link List}. Can't be null.
     */
    public void addAll(Iterable<? extends T> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("The parameter 'elements' cannot be null.");
        }

        for (T element : elements) {
            add(element);
        }
    }

    /**
     * Adds a button to the UI used to add a new item to the list.
     *
     * @param label The text to display on the buttons.
     * @param createAction The action that describes how to create the item to
     *        add to the underlying {@link List}.
     * @return The reference to the button that was created. Can't be null.
     */
    public Widget addCreateListAction(String label, CreateListAction<? extends T> createAction) {
        return addCreateListAction(label, IconType.PLUS_CIRCLE, createAction);
    }

    /**
     * Adds a button to the UI used to add a new item to the list.
     *
     * @param label The text to display on the buttons.
     * @param iconType The icon to display within the button.
     * @param createAction The action that describes how to create the item to
     *        add to the underlying {@link List}.
     * @return The reference to the button that was created. Can't be null.
     */
    public Widget addCreateListAction(String label, IconType iconType,
            final CreateListAction<? extends T> createAction) {

        /* create a row for the table header that will start editing a new element when it is
         * clicked on */
        final TableRowElement addRow = TableRowElement.as(DOM.createTR());
        addRow.setClassName("itemListAddButton");
        DOM.sinkEvents(addRow, Event.ONMOUSEDOWN);
        DOM.setEventListener(addRow, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                if (BrowserEvents.MOUSEDOWN.equals(event.getType())) {

                    /* if we are editing or the add button is not editable, return and do not
                     * perform 'add' action. */
                    if (isEditing() || Boolean.FALSE.equals(addButtonEditableMap.get(addRow))) {
                        return;
                    }

                    T item = createAction.createDefaultItem();
                    if (item != null) {
                        editNewElement(item);
                    }
                }
            }
        });

        TableCellElement addColumn = addRow.insertCell(-1);

        // add the icon and add button label
        Icon icon = new Icon(IconType.PLUS_CIRCLE);
        icon.setSize(IconSize.TIMES2);
        icon.setColor("rgb(0,200,0)");
        icon.setMarginRight(10);
        icon.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);

        InlineLabel labelText = new InlineLabel(label);
        labelText.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);

        ItemListEditorAddRowFlowPanel addPanel = new ItemListEditorAddRowFlowPanel();
        addPanel.add(icon);
        addPanel.add(labelText);

        addColumn.appendChild(addPanel.getElement());

        // set panel to row mapping
        addButtonPanelToRow.put(addPanel, addRow);
        // row defaults as editable
        addButtonEditableMap.put(addRow, Boolean.TRUE);

        return addPanel;
    }

    /**
     * Creates the icon from the element and action.
     *
     * @param element the element to perform the action upon.
     * @param action the action to perform when the icon is clicked.
     * @return the icon.
     */
    private Icon createIcon(final T element, final ItemAction<T> action) {
        Icon icon = new Icon(action.getIconType(element));
        icon.setTitle(action.getTooltip(element));
        icon.setSize(IconSize.LARGE);

        final boolean isEnabled = action.isEnabled(element);
        icon.getElement().getStyle().setOpacity(isEnabled ? 1.0f : 0.5f);
        icon.getElement().getStyle().setCursor(isEnabled ? Cursor.POINTER : Cursor.DEFAULT);

        // Add padding to make the icon easier to click
        icon.getElement().getStyle().setPadding(10, Unit.PX);

        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                // prevent propagation to the element's row so that we don't start editing
                event.stopPropagation();

                if (isEnabled) {
                    action.execute(element);
                }
            }
        });

        return icon;
    }

    /**
     * If there are no {@link ListChangedCallback list changed callbacks}, then
     * fire a dirty event. Otherwise, execute the callbacks.
     *
     * @param changeEvent the event to execute if there are any callbacks.
     */
    private void fireDirtyOrExecuteListChangedCallbacks(ListChangedEvent<T> changeEvent) {
        if (listChangedCallbacks.isEmpty() || changeEvent.getAffectedItems().isEmpty()) {
            ScenarioEventUtility.fireDirtyEditorEvent();
        } else {
            for (ListChangedCallback<T> callback : listChangedCallbacks) {
                callback.listChanged(changeEvent);
            }
        }
    }

    /**
     * Execute the edit cancelled callbacks.
     */
    private void fireEditCancelledCallbacks() {
        for (EditCancelledCallback callback : editCancelledCallbacks) {
            callback.editCancelled();
        }
    }

    /**
     * Begins editing an element that is not yet in the {@link #items} list.
     *
     * @param element The element to edit. Can't be null.
     */
    public void editNewElement(T element) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editNewElement(" + element + ")");
        }

        /* Validate that the element passed meets the constraints */
        if (editElement != null) {
            String msg = "The editor is already editing an element '" + editElement
                    + "' and therefore cannot edit another one.";
            throw new UnsupportedOperationException(msg);
        } else if (element == null) {
            throw new IllegalArgumentException("A null 'element' can't be edited");
        }

        placeholderEntry.setVisible(false);

        /* Save a reference to the element and the index at which it is being
         * edited. */
        this.editElement = element;
        this.editedGridRowIndex = items.size();

        refreshItemActions();

        grid.insertRow(items.size());
        if (!inlineEditorReplacesRow) {
            grid.insertRow(items.size());
        }

        showEditInterface();
    }

    /**
     * Presents the editing interface for the element at the provided index.
     *
     * @param index The index of the element within the underlying {@link List} to begin editing.
     *        The index must be within the bounds of the underlying {@link List}.
     */
    public void editExisting(int index) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editExisting(" + index + ")");
        }

        /* Validate that the arguments passed meet the constraints */
        if (index >= items.size() || index < 0) {
            String msg = "The element at index '" + index + "' cannot be edited because the list only has '"
                    + items.size() + "' elements";
            throw new UnsupportedOperationException(msg);
        } else if (editElement != null) {
            String msg = "The editor is already editing an element '" + editElement
                    + "' and therefore cannot edit another one.";
            throw new UnsupportedOperationException(msg);
        } else if (items.get(index) == null) {
            throw new IllegalArgumentException("A null 'editElement' can't be edited");
        }

        /* Save a reference to the element and the index at which it is being
         * edited. */
        this.editElement = items.get(index);
        this.editedGridRowIndex = index;
        
        ItemListEditorEditEvent<T> editEvent = new ItemListEditorEditEvent<T>(editElement);
        SharedResources.getInstance().getEventBus().fireEvent(editEvent);

        refreshItemActions();

        /* If there are edit fields, add an additional row for the save and
         * cancel buttons. If there are no edit fields, empty the row in which
         * the inline editor will be placed. */
        if (!inlineEditorReplacesRow) {
            grid.insertRow(index + 1);
        } else {
            grid.removeCells(index, 1, fields.size() + actions.size() - 1);
        }

        showEditInterface();
    }
    
    /**
     * Presents the editing interface for the given element, assuming it is in the list.
     *
     * @param item The element within the underlying {@link List} to begin editing. 
     * If the item is not found within the underlying list, this method will do nothing.
     */
    public void editExisting(T item) {
        
        int index = items.indexOf(item);
        
        if(index != -1) {
            editExisting(index);
        }
    }

    /**
     * Presents the editing interface for the {@link #editElement} at the
     * {@link #editedGridRowIndex}.
     */
    private void showEditInterface() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showEditInterface()");
        }

        /* Remove the red box around the edited row */
        rowValidation.removeElementByKey(editElement);

        /* Disable dragging */
        if (draggable) {
            grid.setDraggable(false);
        }

        /* Disables all buttons that cause edits */
        updateRowTooltips();
        setAddButtonsEnabled(false);

        Widget scrollToWidget = null;

        if (!inlineEditorReplacesRow) {
            /* There are some edit widgets therefore remove the view widgets and replace with edit
             * widgets where available. */
            Set<ValidationStatus> editFieldValidations = new HashSet<ValidationStatus>();
            for (int i = 0; i < fields.size(); i++) {
                ItemField<T> field = fields.get(i);
                if (field instanceof EditableItemField) {
                    EditableItemField<T> editField = (EditableItemField<T>) field;
                    Widget editWidget = editField.getEditWidget(editElement);
                    if (editWidget != null) {
                        grid.setWidget(editedGridRowIndex, i, editWidget);
                        scrollToWidget = editWidget;
                    }

                    /* perform validation on edit field */
                    editFieldValidations.clear();
                    editField.getValidationStatuses(editFieldValidations);
                    for (ValidationStatus status : editFieldValidations) {
                        editField.validate(status);
                        updateValidationStatus(status);
                    }
                }
            }

            /* If the element being edited is a new element, ensure the newly
             * created row has item actions as well. */
            if (!items.contains(editElement)) {
                for (int i = 0; i < actions.size(); i++) {
                    ItemAction<T> action = actions.get(0);
                    Icon icon = createIcon(editElement, action);
                    grid.setWidget(editedGridRowIndex, i + fields.size(), icon);
                }
            }
        }

        /* Format the row that contains the inline editor and the save/cancel
         * buttons.
         *
         * IMPORTANT: this block which attaches the editor and wrapper to the
         * DOM come before the following block which populates the editor so
         * that if the populateEditor method requires widgets to already be
         * attached to the DOM, they are. */
        final int editorGridRowIndex = inlineEditorReplacesRow ? editedGridRowIndex : editedGridRowIndex + 1;
        grid.getRowFormatter().addStyleName(editorGridRowIndex, style.inlineEditorStyle());
        grid.getFlexCellFormatter().setColSpan(editorGridRowIndex, 0, getColumnCount());
        grid.setWidget(editorGridRowIndex, 0, editorWrapper);

        /* Instantiate the editor's state */
        ItemEditor<T> editor = editorWrapper.getEditor();
        if (editor != null) {
            editor.populateEditor(editElement);
            editor.setReadonly(isReadonly());
            editor.setActive(true);
            editor.validateAll();
            scrollToWidget = editor;
        }

        /* Attempt to scroll any part of the editing interface into view */
        if (scrollToWidget != null) {
            scrollIntoView(scrollToWidget.getElement());
        }
    }

    /**
     * Sets the text for the save button. Nothing will change if the {@link #editorWrapper} is null
     * or if the provided text is blank.
     * 
     * @param saveText the text to use for the save button.
     */
    public void setSaveButtonText(String saveText) {
        if (editorWrapper != null && StringUtils.isNotBlank(saveText)) {
            editorWrapper.getSaveButton().setText(saveText);
        }
    }

    /**
     * Sets the text for the cancel button. Nothing will change if the {@link #editorWrapper} is
     * null or if the provided text is blank.
     * 
     * @param cancelText the text to use for the cancel button.
     */
    public void setCancelButtonText(String cancelText) {
        if (editorWrapper != null && StringUtils.isNotBlank(cancelText)) {
            editorWrapper.getCancelButton().setText(cancelText);
        }
    }

    /**
     * Cancel editing. If not editing, nothing will happen.
     */
    public void cancelEditing() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("cancelEditing()");
        }

        if (!isEditing()) {
            return;
        }

        clearValidations();
        delayedAction = DelayedAction.REFRESH;

        ItemEditor<T> editor = editorWrapper.getEditor();
        if (editor != null) {
            editor.onCancel();
        }

        boolean isEditingNewElement = !items.contains(editElement);
        stopEditing(isEditingNewElement, false);
        fireEditCancelledCallbacks();
    }

    /**
     * Removes the provided element from the list data model and from the user
     * interface.
     *
     * @param element The element to remove from the {@link ItemListEditor} and
     *        from the backing {@link List}. Can't be null.
     */
    public void remove(T element) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("remove(" + element + ") from\n"+items);
        }

        if (element == null) {
            throw new IllegalArgumentException("The parameter 'element' cannot be null.");
        }

        removeAt(items.indexOf(element));
    }

    /**
     * Removes the element at the specified index from the backing {@link List}
     * and from the user interface.
     *
     * @param listIndex The index at which to remove the element. Must be a
     *        non-negative number less than the size of the backing
     *        {@link List}.
     */
    public void removeAt(int listIndex) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeAt(" + listIndex + ")");
        }

        if (listIndex >= items.size() || listIndex < 0) {
            String msg = "The element at index '" + listIndex + "' cannot be removed because the list only has '"
                    + items.size() + "' elements";
            throw new UnsupportedOperationException(msg);
        }

        grid.removeRow(listIndex);
        T element = items.remove(listIndex);

        /* remove element from validation (will only be there if it is currently invalid). */
        rowValidation.removeElementByKey(element);
        requestValidation(rowValidation);

        placeholderEntry.setVisible(items.isEmpty());

        revalidateAllItems();

        ListChangedEvent<T> changeEvent = new ListChangedEvent<T>(ListAction.REMOVE, element);
        fireDirtyOrExecuteListChangedCallbacks(changeEvent);
    }

    /**
     * Redraws every row within the {@link ItemListEditor} to accurately reflect
     * the state of the underlying elements. This method is potentially
     * expensive. If a known element or set of elements has changed use the
     * {@link #refresh(int)} or {@link #refresh(Object)} methods.
     *
     * @param forceRedraw true to redraw the entire table even if there is
     *        something currently being edited (which will close the editor);
     *        false will delay the redraw action iff the editor is currently
     *        open.
     */
    public void redrawListEditor(boolean forceRedraw) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("redrawListEditor(" + forceRedraw + ")");
        }

        if (!forceRedraw && isEditing()) {
            // delay redrawing the table until the editor is closed.
            delayedAction = DelayedAction.REDRAW;
            return;
        }

        // will do nothing if not editing
        cancelEditing();

        // clear row validations
        if (!rowValidation.getElements().isEmpty()) {
            rowValidation.getElementIdentifierMap().clear();
            requestValidation(rowValidation);
        }

        grid.removeAllRows();
        updateHeaders();

        placeholderEntry.setVisible(items.isEmpty());

        for (int i = 0; i < items.size(); i++) {
            refresh(i);
        }

        /* Update the row tooltips */
        updateRowTooltips();
    }

    /**
     * Refreshes the row of the specified element to accurately reflect the
     * state of the given element.
     *
     * @param element The element to refresh. The element must be contained
     *        within the underlying list.
     */
    public void refresh(T element) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refresh(" + element + ")");
        }

        int rowIndex = items.indexOf(element);

        /* if we are trying to refresh the row being edited, skip refresh. Will update when switched
         * back to 'view mode' */
        if (getEditElementListIndex() != rowIndex) {
            refresh(rowIndex);
        }
    }

    /**
     * Refreshes the row of the element at the specified index to accurately
     * reflect the state of the element within the underlying {@link List}.
     *
     * @param listIndex The index of the item who's row should be refreshed.
     *        Will do nothing if the provided index is out of bounds of the
     *        underlying {@link List}.
     */
    private void refresh(int listIndex) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refresh(" + listIndex + ")");
        }

        if (listIndex >= items.size() || listIndex < 0) {
            return;
        }

        /* The actual element whose row needs to be refreshed. */
        final T element = items.get(listIndex);

        /* The index of the column to be populated next. */
        int currColIndex = 0;

        /* Rebuild the fields for the row */
        for (ItemField<T> field : fields) {
            Widget widgetToSet;
            if (listIndex == getEditElementListIndex() && field instanceof EditableItemField) {
                EditableItemField<T> editField = (EditableItemField<T>) field;
                widgetToSet = editField.getEditWidget(element);
            } else {
                widgetToSet = field.getViewWidget(element);
            }
            grid.setWidget(listIndex, currColIndex++, widgetToSet);
        }

        /* Rebuild the actions for the row */
        for (ItemAction<T> action : actions) {
            grid.setWidget(listIndex, currColIndex++, createIcon(element, action));
        }
        
        applyItemFormatting(element, listIndex);

        /* need to revalidate because we remove and refresh the row, removing the validation
         * styles */
        validateItem(element);
    }

    /**
     * Refreshes a specific cell for a given list item.
     *
     * @param listIndex The index of the element for which a cell is being
     *        refreshed.
     * @param columnIndex The index of the column that should be refreshed for
     *        the specified element.
     */
    private void refresh(int listIndex, int columnIndex) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(listIndex, columnIndex);
            logger.fine("refresh(" + StringUtils.join(", ", params) + ")");
        }

        if (columnIndex < 0 || columnIndex >= getColumnCount()) {
            throw new IllegalArgumentException(
                    "The columnIndex'" + columnIndex + "' must be between 0 and " + (getColumnCount() - 1));
        } else if (listIndex < 0 || listIndex >= items.size()) {
            throw new IllegalArgumentException(
                    "The listIndex '" + listIndex + "' must be between 0 and " + (items.size() - 1));
        }

        final T element = items.get(listIndex);

        /* Rebuild an ItemField or an ItemAction depending on the column index */
        Widget cellWidget;
        if (columnIndex < fields.size()) {
            ItemField<T> field = fields.get(columnIndex);
            if (listIndex == getEditElementListIndex() && field instanceof EditableItemField) {
                EditableItemField<T> editField = (EditableItemField<T>) field;
                cellWidget = editField.getEditWidget(element);
            } else {
                cellWidget = field.getViewWidget(element);
            }
        } else {
            int actionIndex = columnIndex - fields.size();
            ItemAction<T> action = actions.get(actionIndex);
            cellWidget = createIcon(element, action);
        }

        grid.setWidget(listIndex, columnIndex, cellWidget);
        
        applyItemFormatting(element, listIndex);
    }

    /**
     * Updates the current state of all {@link ItemAction item actions} within the
     * {@link ItemListEditor}.
     */
    private void refreshItemActions() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refreshItemActions()");
        }

        for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
            int columnIndex = actionIndex + fields.size();
            for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {
                refresh(rowIndex, columnIndex);
            }
        }
    }

    /**
     * Shows/Hides the tooltip for the table rows.
     */
    private void updateRowTooltips() {
        final NodeList<TableRowElement> rows = TableElement.as(grid.getElement()).getTBodies().getItem(0).getRows();
        for (int rowIndex = 0; rowIndex < rows.getLength(); rowIndex++) {
            updateRowTooltip(rowIndex);
        }
    }

    /**
     * Shows/Hides the tooltip for the row with the provided index.
     *
     * @param rowIndex the index of the row.
     */
    private void updateRowTooltip(int rowIndex) {
        final NodeList<TableRowElement> rows = TableElement.as(grid.getElement()).getTBodies().getItem(0).getRows();

        TableRowElement row = rows.getItem(rowIndex);
        if (row == null) {
            return;
        }

        row.setTitle((editorWrapper.getEditor() == null || isEditing()) ? null : "Click to edit");
    }

    /**
     * Sets the title that is displayed within the dialog that prompts the user
     * for confirmation before deleting an item from the underlying
     * {@link List}.
     *
     * @param removeItemDialogTitle The new title to use. Can't be null.
     */
    public void setRemoveItemDialogTitle(String removeItemDialogTitle) {
        if (StringUtils.isBlank(removeItemDialogTitle)) {
            throw new IllegalArgumentException("The parameter 'removeItemDialogTitle' cannot be empty or null.");
        }

        this.removeItemDialogTitle = removeItemDialogTitle;
    }

    /**
     * Sets the optional stringifier for an item used when displaying the Delete
     * Item confirmation dialog. If not set, the Delete Item dialog will refer
     * to it as "this item". If set, the Delete Item dialog will ask "Are you
     * sure you want to delete &lt;stringify(item)&gt;?"
     *
     * @param stringify - the stringifier for the item of type T used when
     *        displaying the Delete Item dialog
     */
    public void setRemoveItemStringifier(Stringifier<T> stringify) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setRemoveItemStringifier(" + stringify + ")");
        }

        this.removeItemStringifier = stringify;
    }

    /**
     * Hides the editing interface for an element if it is being shown.
     *
     * @param isNew Indicates whether the item being edited is pre-existing or
     *        new.
     * @param isSave Indicates whether the changes to the item being edited were
     *        saved or canceled.
     */
    private void stopEditing(boolean isNew, boolean isSave) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("stopEditing(" + isNew + ", " + isSave + ")");
        }

        if (!isEditing()) {
            return;
        }

        // mark as not editing
        editElement = null;

        placeholderEntry.setVisible(items.isEmpty());

        /* Determine the index of the inline editor row */
        final int inlineEditorIndex = inlineEditorReplacesRow ? editedGridRowIndex : editedGridRowIndex + 1;

        /* Remove the inline editor styling and sizing */
        grid.getRowFormatter().removeStyleName(inlineEditorIndex, style.inlineEditorStyle());
        grid.getFlexCellFormatter().setColSpan(inlineEditorIndex, 0, 1);

        /* Remove the inline editor row */
        grid.removeRow(inlineEditorIndex);

        /* Make sure the final number of rows matches the number of rows in the list and they are
         * all displaying the 'view' widgets. */
        if (inlineEditorReplacesRow) {
            /* If the element will be in the list after editing and the inline editor replaced the
             * element's row, we need to recreate the element row and refresh it to make sure that
             * its display is up to date. */
            if (isSave || !isNew) {
                grid.insertRow(editedGridRowIndex);
                refresh(editedGridRowIndex);
            }
        } else if (isNew) {
            /* If the user is saving a new element that wasn't replaced by an inline editor, we need
             * to refresh the row to make sure that the appropriate widgets are shown. Otherwise if
             * the user is canceling an add, then we need to totally remove the row from the
             * table. */
            if (isSave) {
                refresh(editedGridRowIndex);
            } else {
                grid.removeRow(editedGridRowIndex);
            }
        } else {
            refresh(editedGridRowIndex);
        }

        /* Disable the editor since we are no longer editing. */
        ItemEditor<T> editor = editorWrapper.getEditor();
        if (editor != null) {
            editor.setActive(false);
        }

        /* Update the edit actions to be enabled */
        refreshItemActions();
        updateRowTooltips();
        setAddButtonsEnabled(true);
        if (draggable && !isReadonly()) {
            grid.setDraggable(draggable);
        }

        // perform delayed actions now that the editor is closed
        if (delayedAction == DelayedAction.REDRAW) {
            redrawListEditor(true);
        } else if (delayedAction == DelayedAction.REFRESH) {
            revalidateAllItems();
        }

        // reset values
        delayedAction = null;
        editedGridRowIndex = -1;
    }

    /**
     * Updates the {@link #grid} to display the headers defined within {@link #fields}
     */
    private void updateHeaders() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateHeaders()");
        }

        HashMap<Integer, String> fieldIndexToHeader = new HashMap<>();

        // get the header text for each column, if applicable
        int currCol = 0;
        for (ItemField<T> field : fields) {

            if (StringUtils.isNotBlank(field.getHeader())) {
                fieldIndexToHeader.put(currCol, field.getHeader());
            }

            currCol++;
        }

        if (!fieldIndexToHeader.isEmpty() || !addButtonEditableMap.isEmpty()) {

            /* if there is header content to display, update the header to include the new header
             * content */
            TableSectionElement tableHeadElement = TableElement.as(grid.getElement()).createTHead();
            tableHeadElement.removeAllChildren();

            if (!fieldIndexToHeader.isEmpty()) {

                TableRowElement row = tableHeadElement.insertRow(-1);

                currCol = 0;
                while (currCol < getColumnCount()) {

                    String headerText = fieldIndexToHeader.get(currCol);

                    TableCellElement headerCell = row.insertCell(currCol);

                    if (headerText != null) {

                        // define the header text for each column of the table
                        Heading header = new Heading(HeadingSize.H5, headerText);
                        header.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                        header.getElement().getStyle().setMargin(0, Unit.PX);

                        headerCell.appendChild(header.getElement());

                    } else {

                        // just add an empty header for columns with no header text
                        row.insertCell(currCol);
                    }

                    currCol++;
                }
            }

            if (!isReadonly()) {

                for (TableRowElement addButton : addButtonEditableMap.keySet()) {

                    // make sure the add button spans across all of the table's columns
                    addButton.getCells().getItem(0).setColSpan(Math.max(1, fields.size() + actions.size()));

                    setAddButtonEnabled(addButton, !isEditing());

                    // place any add buttons in the header
                    tableHeadElement.appendChild(addButton);
                }
            }

        } else {

            // if there's no header content to display, remove the header
            TableElement.as(grid.getElement()).deleteTHead();
        }
    }
    
    /**
     * Sets the available {@link ItemAction item actions} for each item within
     * the {@link List} which is being edited.
     *
     * @param actions The {@link Iterable} of {@link ItemAction} to set as the
     *        available actions for the user. Null is treated as an
     *        {@link Iterable} without any elements.
     */
    public void setActions(Iterable<ItemAction<T>> actions) {
        setActions(actions, true);
    }

    /**
     * Sets the available {@link ItemAction item actions} for each item within
     * the {@link List} which is being edited.
     *
     * @param actions The {@link Iterable} of {@link ItemAction} to set as the
     *        available actions for the user. Null is treated as an
     *        {@link Iterable} without any elements.
     * @param allowDelete whether the default delete action should be provided.
     * This defaults to true.
     */
    public void setActions(Iterable<ItemAction<T>> actions, boolean allowDelete) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setActions(" + actions + ")");
        }

        this.actions.clear();

        if (actions != null) {
            for (ItemAction<T> action : actions) {
                this.actions.add(action);
            }
        }

        if(allowDelete) {
            /* Add the default actions */
            this.actions.add(DELETE_ACTION);
        }

        redrawListEditor(true);
    }

    /**
     * Updates whether or not the add buttons above the elements in the
     * {@link ItemListEditor} are clickable.
     *
     * @param enabled True if the buttons are clickable, false if they are not
     *        clickable.
     */
    private void setAddButtonsEnabled(boolean enabled) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setAddButtonsEnabled(" + enabled + ")");
        }

        for (TableRowElement addButton : addButtonEditableMap.keySet()) {
            setAddButtonEnabled(addButton, enabled);
        }
    }

    /**
     * Updates whether or not the provided add button is clickable.
     *
     * @param addButton the add button to enable or disable.
     * @param enabled True if the button is clickable, false if it is not
     *        clickable.
     */
    private void setAddButtonEnabled(TableRowElement addButton, boolean enabled) {
        if (addButton == null || !addButtonEditableMap.containsKey(addButton)) {
            return;
        }

        boolean editable = Boolean.TRUE.equals(addButtonEditableMap.get(addButton));
        if (editable && enabled) {
            addButton.removeClassName("itemListAddButtonDisabled");
        } else {
            addButton.addClassName("itemListAddButtonDisabled");
        }
    }

    /**
     * Updates whether or not the provided add button is clickable.
     *
     * @param addButton the add button to enable or disable.
     * @param enabled True if the button is clickable, false if it is not
     *        clickable.
     */
    public void setAddButtonEnabled(Widget addButton, boolean enabled) {
        if (addButton == null || !addButtonPanelToRow.containsKey(addButton)) {
            return;
        }

        TableRowElement rowElement = addButtonPanelToRow.get(addButton);
        addButtonEditableMap.put(rowElement, Boolean.valueOf(enabled));
        setAddButtonEnabled(rowElement, enabled);
    }

    /**
     * Sets the buttons to be displayed within the panel located below the
     * entries being displayed.
     *
     * @param topButtons The {@link Iterable} of {@link EnforcedButton buttons}. Null is
     *        treated as an {@link Iterable} with no elements.
     */
    public void setTopButtons(Iterable<EnforcedButton> topButtons) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setTopButtons(" + topButtons + ")");
        }

        topButtonsPanel.clear();
        if (topButtons == null) {
            topButtonsContainer.setVisible(false);
            return;
        }

        for (EnforcedButton button : topButtons) {
            button.setType(ButtonType.SUCCESS);
            button.setMarginLeft(2.5);
            button.setMarginRight(2.5);
            button.setMarginBottom(1);
            button.setMarginTop(1);
            topButtonsPanel.add(button);
        }

        topButtonsContainer.setVisible(topButtonsPanel.getWidgetCount() != 0);
    }

    /**
     * Calculates the number of columns that the {@link #grid} should have. The
     * number of columns is a summation of thenumber of {@link #fields} and
     * {@link #actions}.
     *
     * @return The number of columns. Will be a non-negative number.
     */
    private int getColumnCount() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getColumnCount()");
        }

        return fields.size() + actions.size();
    }

    /**
     * Returns true if the {@link ItemListEditor} is currently editing one of
     * its elements.
     *
     * @return True if the {@link ItemListEditor} is currently editing an
     *         element, otherwise false.
     */
    public boolean isEditing() {
        final boolean editing = editElement != null;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isEditing() - " + editing);
        }

        return editing;
    }

    /**
     * Sets the {@link ItemField item fields} that allow for displaying and
     * potentially editing the elements contained within the
     * {@link ItemListEditor}.
     *
     * @param fields The {@link Iterable} that contains each {@link ItemField}
     *        to be used by the {@link ItemListEditor}. Null is treated as an
     *        {@link Iterable} with no elements.
     */
    public void setFields(Iterable<? extends ItemField<T>> fields) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setFields(" + fields + ")");
        }

        this.fields.clear();

        if (fields != null) {
            for (ItemField<T> field : fields) {
                this.fields.add(field);
            }
        }

        /* Set each column to the specified width */
        inlineEditorReplacesRow = true;
        for (int i = 0; i < this.fields.size(); i++) {
            ItemField<T> field = this.fields.get(i);
            inlineEditorReplacesRow &= !(field instanceof EditableItemField);
            grid.getColumnFormatter().getElement(i).getStyle().setProperty("width", field.getWidth());
        }

        redrawListEditor(true);
    }

    /**
     * Gets a copy of the underlying {@link List} of elements.
     *
     * @return A copy of the list currently being edited. Changes made to the
     *         value returned by this method will not change the underlying list
     *         nor will they be reflected in the UI.
     */
    public List<T> getItems() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getItems()");
        }

        return Collections.unmodifiableList(items);
    }

    /**
     * Sets the underlying {@link List} which is being edited.
     *
     * @param items The {@link List} that should be edited. This will
     *        automatically call {@link #redrawListEditor(boolean)} in order to
     *        make the UI reflect the new list. All changes performed through
     *        the UI on the list will automatically propogate to the list that
     *        is being passed in. The list being passed in can't be null.
     */
    public void setItems(List<T> items) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setItems(" + items + ")");
        }

        if (items == null) {
            throw new IllegalArgumentException("The parameter 'items' cannot be null.");
        }

        /* Must reassign as opposed to addAll() because we want a direct reference to the list
         * provided. */
        this.items = items;

        redrawListEditor(true);

        /* Show the appropriate elements based on the state of the list */
        placeholderEntry.setVisible(items.isEmpty());
    }

    /**
     * Gets the current number of elements contained within the
     * {@link ItemListEditor}. Results in the same value as calling
     * {@link List#size()} on the result of {@link #getItems()} but avoids the
     * unnecessary defensive copy associated with that method.
     *
     * @return The current number of elements in the {@link ItemListEditor}.
     */
    public int size() {
        return items.size();
    }

    /**
     * Sets the text to be displayed if the table is empty.
     *
     * @param text the text to be shown. Null or empty will set the placeholder back to its default.
     */
    public void setPlaceholder(String text) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setPlaceholder(text='" + text + "')");
        }

        setPlaceholder(SafeHtmlUtils.fromString(StringUtils.isBlank(text) ? DEFAULT_PLACEHOLDER : text));
    }

    /**
     * Sets the html to be displayed if the table is empty.
     *
     * @param html the html to be shown. Null or empty will set the placeholder back to its default.
     */
    public void setPlaceholder(SafeHtml html) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setPlaceholder(html='" + html + "')");
        }

        if (html == null || StringUtils.isBlank(html.asString())) {
            placeholderEntry.setText(DEFAULT_PLACEHOLDER);
        } else {
            placeholderEntry.setHTML(html);
        }
    }

    /**
     * Sets the {@link #grid table} rows to be draggable or not. Default is not
     * draggable.
     *
     * @param draggable true to set the rows to be draggable; false otherwise.
     */
    public void setDraggable(boolean draggable) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setDraggable(" + draggable + ")");
        }

        this.draggable = draggable;
        grid.setDraggable(draggable);
    }

    /**
     * Sets the {@link DeletePredicate} for the item list editor. This
     * determines if an item is able to be deleted or not.
     *
     * @param deletePredicate the {@link DeletePredicate}. Can't be null.
     */
    public void setDeletePredicate(DeletePredicate<T> deletePredicate) {
        if (deletePredicate == null) {
            throw new IllegalArgumentException("The parameter 'deletePredicate' cannot be null.");
        }

        this.deletePredicate = deletePredicate;
    }

    /**
     * Sets the HTML to be displayed above the item table.
     *
     * @param html the String value
     */
    public void setTableLabel(String html) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setTableLabel(" + html + ")");
        }

        if (StringUtils.isNotBlank(html)) {
            tableLabel.setVisible(true);
            setTableLabel(SafeHtmlUtils.fromTrustedString(html));
        } else {
            tableLabel.setVisible(false);
        }
    }

    /**
     * Sets the HTML to be displayed above the item table.
     *
     * @param html the SafeHtml value
     */
    public void setTableLabel(SafeHtml html) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setTableLabel(" + html + ")");
        }

        this.tableLabel.setHTML(html);

        // hide the label entirely if it has no text
        this.tableLabel.setVisible(html != null);
    }

    /**
     * Sets the callback to be executed whenever the list is modified. It is
     * assumed that the callback will handle firing any necessary dirty events.
     *
     * @param listChangedCallback the callback to execute when the list is
     *        changed. Can't be null.
     */
    public void addListChangedCallback(ListChangedCallback<T> listChangedCallback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addListChangedCallback(" + listChangedCallback + ")");
        }

        if (listChangedCallback == null) {
            throw new IllegalArgumentException("The parameter 'listChangedCallback' cannot be null.");
        }

        listChangedCallbacks.add(listChangedCallback);
    }

    /**
     * Sets the callback to be executed whenever an edit to the list is cancelled.
     *
     * @param editCancelledCallback the callback to execute when an edit to an item
     *        in the the list is cancelled. Can't be null.
     */
    public void addEditCancelledCallback(EditCancelledCallback editCancelledCallback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addEditCancelledCallback(" + editCancelledCallback + ")");
        }

        if (editCancelledCallback == null) {
            throw new IllegalArgumentException("The parameter 'listChangedCallback' cannot be null.");
        }

        editCancelledCallbacks.add(editCancelledCallback);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(rowValidation);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        ItemEditor<T> editor = editorWrapper.getEditor();
        if (editor != null) {
            childValidationComposites.add(editor);
        }
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (rowValidation.equals(validationStatus)) {
            /* Here only the validity of the status is set. The validateItem
             * method is responsible for determining which elements need to be
             * highlighted. */
            rowValidation.setValidity(rowValidation.getElements().isEmpty());
        }
    }

    /**
     * Check the invalid elements to see if they are still invalid. This method should be called
     * when a row is changed or added.
     */
    @SuppressWarnings("unchecked")
    public void revalidateInvalidItems() {
        for (Object identifier : rowValidation.getElementIdentifierMap().keySet()) {
            /* We know this can only be T, but there is no way to check, so I had to add a suppress
             * warnings tag */
            validateItem((T) identifier);
        }
    }

    /**
     * Validate all the items in the list. This should be called when a row is removed or manually
     * added.
     */
    public void revalidateAllItems() {
        for (T item : items) {
            validateItem(item);
        }
    }

    /**
     * Checks the validity of the element.
     *
     * @param element the element to validate. Can't be null.
     */
    private void validateItem(T element) {
        // don't validate if a validation widget was never set.
        if (!isValidationWidgetSet()) {
            return;
        }

        if (element == null) {
            throw new IllegalArgumentException("The parameter 'element' cannot be null.");
        }

        int locationInList = items.indexOf(element);
        if (locationInList == -1) {
            throw new IllegalArgumentException("The parameter 'element' is not contained within the list items.");
        }

        TableElement table = TableElement.as(grid.getElement());
        TableRowElement row = table.getTBodies().getItem(0).getRows().getItem(locationInList);
        if (row == null) {
            throw new IllegalArgumentException("There is no row element at index " + locationInList
                    + ". The ItemListEditor is out of sync with the underlying list.");
        }

        // check editor
        ItemEditor<T> editor = editorWrapper.getEditor();
        if (editor != null) {
            /* Delay validating until editing is complete */
            if (isEditing()) {
                /* redraw is a larger operation so we don't want to downscale
                 * the delayed action to refresh */
                if (delayedAction != DelayedAction.REDRAW) {
                    delayedAction = DelayedAction.REFRESH;
                }
                return;
            }

            /* Perform validation on the element */
            boolean valid = editor.validate(element);
            if (!valid) {
                rowValidation.addElement(row, element, this);
                requestValidation(rowValidation);
                return;
            }
        }

        // check edit fields
        Set<ValidationStatus> editFieldValidations = new HashSet<ValidationStatus>();
        for (ItemField<T> field : fields) {
            if (field instanceof EditableItemField) {
                EditableItemField<T> editField = (EditableItemField<T>) field;
                editFieldValidations.clear();
                editField.getValidationStatuses(editFieldValidations);

                editField.getEditWidget(element);
                for (ValidationStatus status : editFieldValidations) {
                    editField.validate(status);
                    if (!status.isValid()) {
                        rowValidation.addElement(row, element, this);
                        requestValidation(rowValidation);
                        return;
                    }
                }
            }
        }

        // valid
        rowValidation.removeElementByKey(element);
        requestValidation(rowValidation);
    }

    /**
     * Removes all the validation errors caused by the {@link ItemListEditor} or {@link ItemEditor}.
     */
    @Override
    public void clearValidations() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("clearValidationErrors()");
        }

        super.clearValidations();

        Set<ValidationStatus> widgetValidationStatuses = new HashSet<ValidationStatus>();
        for (ItemField<T> field : fields) {
            if (field instanceof EditableItemField) {
                EditableItemField<T> editField = (EditableItemField<T>) field;
                editField.getValidationStatuses(widgetValidationStatuses);
            }
        }

        // mark all validation statuses as valid and pass to panel
        for (ValidationStatus validationStatus : widgetValidationStatuses) {
            validationStatus.setValid();
            updateValidationStatus(validationStatus);
        }
    }

    /**
     * Retrieves the list index of the edited element.
     *
     * @return the list index of the edited element; -1 if the element is null
     *         or not in the list.
     */
    private int getEditElementListIndex() {
        return editElement != null ? items.indexOf(editElement) : -1;
    }

    /**
     * Determines whether or not the {@link ItemListEditor} is in readonly mode.
     * When in readonly mode, none of the rows are editable, nor can any rows be
     * added or deleted.
     *
     * @return True if the widget is in readonly mode. False if it is not in
     *         readonly mode.
     */
    public boolean isReadonly() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isReadonly()");
        }

        return isReadonly;
    }

    /**
     * Sets the {@link ItemListEditor} to read only mode which prevents users
     * from making changes to the underlying {@link List} or elements within.
     *
     * @param isReadonly True if the {@link ItemListEditor} should prevent
     *        editing, false if the {@link ItemListEditor} should allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        /* If the value of the readonly flag is not changing, there is no need
         * to execute the rest of this method */
        if (this.isReadonly == isReadonly) {
            return;
        }

        this.isReadonly = isReadonly;
        
        if (draggable && isReadonly) {
            grid.setDraggable(false);
        }

        for (int i = 0; i < topButtonsPanel.getWidgetCount(); i++) {
            Widget widget = topButtonsPanel.getWidget(i);
            if (widget instanceof HasEnabled) {
                ((HasEnabled) widget).setEnabled(!isReadonly);
            }
        }
        
        if (editorWrapper.getEditor() != null) {
            editorWrapper.getEditor().setReadonly(isReadonly);
            editorWrapper.getEditor().setSaveButtonVisible(!isReadonly);
        }

        /// show/hide add buttons in the table's header as appropriate
        updateHeaders();
        redrawListEditor(true);
    }

    /**
     * Scrolls the element into view. Will perform the bare minimum amount of
     * scrolling (e.g. will appear at bottom).
     *
     * @param element the element to scroll to.
     */
    private native void scrollIntoView(Element element) /*-{
		element.scrollIntoView(false);
    }-*/;
    
    /**
     * Sets the item formatter that this list editor should use to format the rows representing its items. By default,
     * no item formatting will be applied, so all items will be visually formatted the same way.
     * 
     * @param formatter the formatter to use
     */
    public void setItemFormatter(ItemFormatter<T> formatter) {
        this.itemFormatter = formatter;
    }
    
    /**
     * Applies the appropriate formatting to the given row using data from the given item based on the formatter
     * specified by {@link #setItemFormatter(ItemFormatter)}. If no item formatter has been specified, this method will
     * do nothing.
     * 
     * @param item the item whose data should be used to format the given row
     * @param row the row to apply formatting to
     */
    private void applyItemFormatting(T item, int row) {
        
        if(itemFormatter != null && row < grid.getRowCount()) {
            itemFormatter.format(item, TableRowElement.as(grid.getRowFormatter().getElement(row)));
        }
    }
    
    /**
     * Class to override a flow panel's getParent class to point to this {@link ItemListEditor}.
     * This is needed because the add rows (which are flow panels) in the {@link ItemListEditor}
     * don't have a parent, so validation's 'jump to' functionality fails. Using this class instead
     * of the standard flow panel fixes that because we are forcing the getParent to return this
     * {@link ItemListEditor}.
     * 
     * @author sharrison
     */
    private class ItemListEditorAddRowFlowPanel extends FlowPanel {
        @Override
        public Widget getParent() {
            return ItemListEditor.this;
        }
    }
    
}