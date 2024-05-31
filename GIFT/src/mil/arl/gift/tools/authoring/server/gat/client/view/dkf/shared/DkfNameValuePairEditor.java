/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextButtonCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import generated.dkf.Nvpair;
import mil.arl.gift.common.gwt.client.widgets.ToggleableTextInputCell;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;

/**
 * Defines an editor widget for setting DKF {@link Nvpair name/value pairs}.
 * 
 * @author sharrison
 */
public class DkfNameValuePairEditor extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(DkfNameValuePairEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static DkfNameValuePairEditorUiBinder uiBinder = GWT.create(DkfNameValuePairEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface DkfNameValuePairEditorUiBinder extends UiBinder<Widget, DkfNameValuePairEditor> {

    }

    /** The css styles for this class */
    interface UiStyle extends CssResource {
        /**
         * Retrieve the css style for the name/value text input cells
         * 
         * @return the css style value from the ui.xml file.
         */
        public String textInputCell();
    }

    /** The Style that combines the ui.xml with this java class */
    @UiField
    UiStyle style;

    /** The table that contains the list of {@link Nvpair name/value pairs} */
    @UiField
    protected CellTable<Nvpair> rootTable;

    /** An optional command that will be executed whenever the underlying metadata is changed */
    private Command onChangeCommand = null;

    /** The widget that is displayed within the table when the widget is empty and enabled */
    private final Widget ENABLED_EMPTY_TABLE_WIDGET = new HTML("Click the add button to create a new entry");

    /** The widget that is displayed within the table when the widget is empty and disabled */
    private final Widget DISABLED_EMPTY_TABLE_WIDGET = new HTML("There are no entries for this table");

    /** The data provider that contains the list being edited and displays it on the cell table */
    private final ListDataProvider<Nvpair> dataModel = new ListDataProvider<>();

    /** Indicates whether or not the user should be able to interact with the widget */
    private boolean isEnabled = true;

    /** The default text for the name column header */
    private static final String DEFAULT_NAME_COL_HEADER_TXT = "Name";

    /** The default text for the value column header */
    private static final String DEFAULT_VALUE_COL_HEADER_TXT = "Value";

    /**
     * The object that is responsible for rendering the button inside the delete column
     */
    private final TextButtonCell deleteCell = new TextButtonCell() {
        @Override
        public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
            if (isEnabled) {
                Image image = new Image(value);
                SafeHtml htmlValue = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(htmlValue);
                image.setTitle("Remove this entry");
            }
        }
    };

    /**
     * The column that contains the button used to delete that row's entry
     */
    private final Column<Nvpair, String> deleteColumn = new Column<Nvpair, String>(deleteCell) {

        @Override
        public String getValue(Nvpair nvpair) {
            return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
        }
    };

    /**
     * The cell that is displayed within the header of the delete column. This is where we have our
     * 'add new' button.
     */
    private final TextButtonCell addButtonCell = new TextButtonCell() {

        @Override
        public void onBrowserEvent(Cell.Context context, Element parent, String value, NativeEvent event,
                ValueUpdater<String> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            if (BrowserEvents.CLICK.equals(event.getType())) {
                dataModel.getList().add(new Nvpair());
            }
        }

        @Override
        public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
            if (isEnabled) {
                Image image = new Image(value);
                image.setSize("18px", "18px");
                SafeHtml htmlValue = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(htmlValue);
                image.setTitle("Add new entry");
            }
        }
    };

    /** The cell that is displayed in the name/key column for each entry */
    private final ToggleableTextInputCell nameCell = new ToggleableTextInputCell();

    /** The column that contains each entry's name/key value */
    private final Column<Nvpair, String> nameColumn = new Column<Nvpair, String>(nameCell) {
        @Override
        public String getValue(Nvpair nvpair) {
            return nvpair.getName();
        }
    };

    /** The cell that is displayed in the name/key column for each entry */
    private final ToggleableTextInputCell valueCell = new ToggleableTextInputCell();

    /** The column that contains each entry's name/key value */
    private final Column<Nvpair, String> valueColumn = new Column<Nvpair, String>(valueCell) {
        @Override
        public String getValue(Nvpair nvpair) {
            return nvpair.getValue();
        }
    };

    /** The stringifier for the name value pair object. Will display the pair as "name: value". */
    private Stringifier<Nvpair> nvpairStringifier = new Stringifier<Nvpair>() {

        @Override
        public String stringify(Nvpair nvpair) {
            if (nvpair == null) {
                return "null";
            }

            String name = StringUtils.isBlank(nvpair.getName()) ? "<no name>" : "'" + nvpair.getName() + "'";
            String value = StringUtils.isBlank(nvpair.getName()) ? "<no value>" : "'" + nvpair.getValue() + "'";
            return name + ": " + value;
        }

    };

    /**
     * Constructs the editor with default header text values and initializes its UI state
     */
    public DkfNameValuePairEditor() {
        this(DEFAULT_NAME_COL_HEADER_TXT, DEFAULT_VALUE_COL_HEADER_TXT);
    }

    /**
     * Constructs the editor and initializes its UI state
     * 
     * @param nameColHeaderText the text for the name column header.
     * @param valueColHeaderText the text for the value column header.
     */
    public DkfNameValuePairEditor(String nameColHeaderText, String valueColHeaderText) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("DkfNameValuePairEditor()");
        }
        initWidget(uiBinder.createAndBindUi(this));

        initColumns(nameColHeaderText, valueColHeaderText);
        dataModel.addDataDisplay(rootTable);
        rootTable.setEmptyTableWidget(ENABLED_EMPTY_TABLE_WIDGET);
    }

    /**
     * Sets the list of name value pairs that the editor is responsible for changing
     * 
     * @param list the list that the editor should now modify, can't be null
     */
    public void setNameValueList(List<Nvpair> list) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("setNameValueList(");
            StringUtils.join(", ", list, nvpairStringifier, sb);
            logger.fine(sb.append(")").toString());
        }

        if (list == null) {
            throw new IllegalArgumentException("The parameter 'list' cannot be null.");
        }

        dataModel.setList(list);
        dataModel.refresh();
        cleanList();
    }

    /**
     * Specifies whether or not the user is allowed to interact with the widget
     * 
     * @param isEnabled the value that indicates whether or not user interaction should be enabled
     */
    public void setEnabled(boolean isEnabled) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setEnabled(" + isEnabled + ")");
        }

        this.isEnabled = isEnabled;
        addButtonCell.setEnabled(isEnabled);
        deleteCell.setEnabled(isEnabled);
        nameCell.setEditable(isEnabled);
        valueCell.setEditable(isEnabled);
        rootTable.setEmptyTableWidget(isEnabled ? ENABLED_EMPTY_TABLE_WIDGET : DISABLED_EMPTY_TABLE_WIDGET);
    }

    /**
     * Initializes the columns within the table
     * 
     * @param nameColHeaderText the text for the name column header.
     * @param valueColHeaderText the text for the value column header.
     */
    private void initColumns(String nameColHeaderText, String valueColHeaderText) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initColumns(" + nameColHeaderText + ", " + valueColHeaderText + ")");
        }

        deleteColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {
            @Override
            public void update(int index, Nvpair nvpair, String newValue) {
                dataModel.getList().remove(index);
                onChange();
            }
        });

        nameColumn.setCellStyleNames(style.textInputCell());
        nameColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {
            @Override
            public void update(int index, Nvpair nvpair, String newName) {
                nvpair.setName(StringUtils.isBlank(newName) ? "" : newName.trim());
                dataModel.refresh();
                onChange();
            }
        });

        valueColumn.setCellStyleNames(style.textInputCell());
        valueColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {
            @Override
            public void update(int index, Nvpair nvpair, String newValue) {
                nvpair.setValue(StringUtils.isBlank(newValue) ? "" : newValue.trim());
                dataModel.refresh();
                onChange();
            }
        });

        // set column names
        rootTable.addColumn(nameColumn,
                StringUtils.isBlank(nameColHeaderText) ? DEFAULT_NAME_COL_HEADER_TXT : nameColHeaderText);
        rootTable.addColumn(valueColumn,
                StringUtils.isBlank(valueColHeaderText) ? DEFAULT_VALUE_COL_HEADER_TXT : valueColHeaderText);

        rootTable.addColumn(deleteColumn, new Header<String>(addButtonCell) {
            @Override
            public String getValue() {
                return GatClientBundle.INSTANCE.add_image().getSafeUri().asString();
            }
        });
    }

    /**
     * Removes any entries from the current list being edited which do not contain a value for both
     * the name and the value
     */
    private void cleanList() {
        Iterator<Nvpair> nvpairIter = dataModel.getList().iterator();
        while (nvpairIter.hasNext()) {
            if (!isCompleteNameValuePair(nvpairIter.next())) {
                nvpairIter.remove();
            }
        }
    }

    /**
     * Determines whether a name value pair contains data for both its name and value fields
     * 
     * @param nvpair the name value pair to test
     * @return true if the name value pair has a populated name and value; false if one or both are
     *         missing.
     */
    private boolean isCompleteNameValuePair(Nvpair nvpair) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isCompleteNameValuePair(" + nvpairStringifier.stringify(nvpair) + ")");
        }

        return StringUtils.isNotBlank(nvpair.getName()) && StringUtils.isNotBlank(nvpair.getValue());
    }

    /**
     * Assigns the command to be executed whenever the underlying metadata is changed
     * 
     * @param command the command to be executed
     */
    public void setOnChangeCommand(Command command) {
        this.onChangeCommand = command;
    }

    /**
     * Executes whatever command is waiting for a change event, assuming such a command has been
     * assigned
     */
    private void onChange() {
        if (onChangeCommand != null && isEnabled) {
            onChangeCommand.execute();
        }
    }
    
    /**
     * Gets a copy of the underlying {@link List} of elements.
     * 
     * @return A copy of the list currently being edited. Changes made to the value returned by this
     *         method will not change the underlying list nor will they be reflected in the UI.
     */
    public List<Nvpair> getItems() {
        return new ArrayList<Nvpair>(dataModel.getList());
    }
}