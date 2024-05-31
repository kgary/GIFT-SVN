/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets;

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

import generated.course.Nvpair;
import mil.arl.gift.common.gwt.client.widgets.ToggleableTextInputCell;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;

public class NameValuePairEditor extends Composite {
    
    private static Logger logger = Logger.getLogger(NameValuePairEditor.class.getName());
    
    private static NameValuePairEditorUiBinder uiBinder = GWT.create(NameValuePairEditorUiBinder.class);
    
    interface NameValuePairEditorUiBinder extends UiBinder<Widget, NameValuePairEditor> {
        
    }
    
    interface UiStyle extends CssResource {
        public String textInputCell();
    }
    
    @UiField
    UiStyle style;
    
    @UiField
    protected CellTable<Nvpair> rootTable;
    
    /** An optional command that will be executed whenever the underlying metadata is changed*/
    private Command onChangeCommand = null;
    
    /**
     * The widget that is displayed within the table when the widget is empty and enabled
     */
    private final Widget ENABLED_EMPTY_TABLE_WIDGET = new HTML("Click the add button to create a new entry");
    
    /**
     * The widget that is displayed within the table when the widget is empty and disabled
     */
    private final Widget DISABLED_EMPTY_TABLE_WIDGET = new HTML("There are no entries for this table");
    
    /**
     * The data provider that contains the list being edited and displays it on the cell table
     */
    private final ListDataProvider<Nvpair> dataModel = new ListDataProvider<>();
    
    /**
     * Indicates whether or not the user should be able to interact with the widget
     */
    private boolean isEnabled = true;
    
    /** The default text for the name column header */
    private static final String NAME_COL_HEADER_TXT = "Name";
    
    /** The default text for the value column header */
    private static final String VALUE_COL_HEADER_TXT = "Value";
    
    /**
     * The object that is responsible for rendering the button inside the 
     * delete column
     */
    private final TextButtonCell deleteCell = new TextButtonCell() {
        @Override
        public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
            if(isEnabled) {
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
        public String getValue(Nvpair nv) {
            return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
        }
    };
    
    /**
     * The cell that is displayed within the header of the delete column
     */
    private final TextButtonCell deleteHeaderCell = new TextButtonCell() {
        
        @Override
        public void onBrowserEvent(Cell.Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            if(BrowserEvents.CLICK.equals(event.getType())) {
                dataModel.getList().add(new Nvpair());
            }
        }
        
        @Override
        public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
            if(isEnabled) {
                Image image = new Image(value);
                image.setSize("18px", "18px");
                SafeHtml htmlValue = SafeHtmlUtils.fromTrustedString(image.toString());
                sb.append(htmlValue);
                image.setTitle("Add new entry");
            }
        }
    };
    
    /**
     * The cell that is displayed in the name/key column for each entry
     */
    private final ToggleableTextInputCell nameCell = new ToggleableTextInputCell() {
    	
    	@Override
		public void onBrowserEvent(Cell.Context context, Element elem, String value, NativeEvent event, ValueUpdater<String> updater) {
			if(event.getType().equals("blur")) {
    			updater.update(getInputElement(elem).getValue());
        	}
    	}
    };
    
    /**
     * The column that contains each entry's name/key value
     */
    private final Column<Nvpair, String> nameColumn = new Column<Nvpair, String>(nameCell) {
        
        @Override
        public String getValue(Nvpair nv) {
            return nv.getName();
        }
    };
    
    /**
     * The cell that is displayed in the name/key column for each entry
     */
    private final ToggleableTextInputCell valueCell = new ToggleableTextInputCell() {
    	
    	@Override
		public void onBrowserEvent(Cell.Context context, Element elem, String value, NativeEvent event, ValueUpdater<String> updater) {
			if(event.getType().equals("blur")) {
    			updater.update(getInputElement(elem).getValue());
        	}
    	}
    };
    
    /**
     * The column that contains each entry's name/key value
     */
    private final Column<Nvpair, String> valueColumn = new Column<Nvpair, String>(valueCell) {

        @Override
        public String getValue(Nvpair nv) {
            return nv.getValue();
        }
    };
    
    /**
     * Constructs the editor with default header text values and initializes its UI state
     */
    public NameValuePairEditor() {
        this(NAME_COL_HEADER_TXT, VALUE_COL_HEADER_TXT);
    }
    
    /**
     * Constructs the editor and initializes its UI state
     * 
     * @param nameColHeaderText the text for the name column header.
     * @param valueColHeaderText the text for the value column header.
     */
    public NameValuePairEditor(String nameColHeaderText, String valueColHeaderText) {
        initWidget(uiBinder.createAndBindUi(this));
        if(logger.isLoggable(Level.INFO)) {
            logger.info("ctor()");
        }
        
        initColumns(nameColHeaderText, valueColHeaderText);
        dataModel.addDataDisplay(rootTable);
        rootTable.setEmptyTableWidget(ENABLED_EMPTY_TABLE_WIDGET);
    }
    
    /**
     * Sets the list of name value pairs that the editor is responsible for changing
     * @param list the list that the editor should now modify, can't be null
     */
    public void setNameValueList(List<Nvpair> list) {
        
        if (logger.isLoggable(Level.INFO)) {
            StringBuilder sb = new StringBuilder("setNameValueList(");
            if(list != null) {
                String delimiter = "";
                for(int i = 0; i < list.size(); i++) {
                    sb.append(delimiter).append(nameValueAsString(list.get(i)));
                    delimiter = ", ";
                }                
            } else {
                sb.append("null");
            }
            
            logger.info(sb.append(")").toString());
        }

        list = list == null ? new ArrayList<Nvpair>() : list;
        
        dataModel.setList(list);
        dataModel.refresh();
        cleanList();
    }
    
    /**
     * Specifies whether or not the user is allowed to interact with the widget
     * @param isEnabled the value that indicates whether or not user interaction should be enabled
     */
    public void setEnabled(boolean isEnabled) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("setEnabled(" + isEnabled + ")");
        }
        
        this.isEnabled = isEnabled;
        
        deleteHeaderCell.setEnabled(isEnabled);
        deleteCell.setEnabled(isEnabled);
        nameCell.setEditable(isEnabled);
        valueCell.setEditable(isEnabled);
        rootTable.setEmptyTableWidget(isEnabled ? ENABLED_EMPTY_TABLE_WIDGET : DISABLED_EMPTY_TABLE_WIDGET);
        cleanList();
        
        /** Redrawing table because widgets depend on isEnabled flag for visibility */
        rootTable.redraw();
    }
    
    /**
     * Initializes the columns within the table
     * 
     * @param nameColHeaderText the text for the name column header.
     * @param valueColHeaderText the text for the value column header.
     */
    private void initColumns(String nameColHeaderText, String valueColHeaderText) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("initColumns()");
        }

        deleteColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {
            
            @Override
            public void update(int index, Nvpair nv, String newValue) {
                dataModel.getList().remove(index);
                onChange();
            }
        });
        
        nameColumn.setCellStyleNames(style.textInputCell());
        nameColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {
            
            @Override
            public void update(int index, Nvpair nv, String newName) {
            	nv.setName(newName.trim());
                dataModel.refresh();
                onChange();
            }
        });
        
        valueColumn.setCellStyleNames(style.textInputCell());
        valueColumn.setFieldUpdater(new FieldUpdater<Nvpair, String>() {
            
            @Override
            public void update(int index, Nvpair nv, String newValue) {
            	nv.setValue(newValue.trim());
                dataModel.refresh();
                onChange();
            }
        });
        
        // set column names
        boolean isBlank = nameColHeaderText == null || nameColHeaderText.trim().isEmpty();
        rootTable.addColumn(nameColumn, isBlank ? NAME_COL_HEADER_TXT : nameColHeaderText);
        isBlank = valueColHeaderText == null || valueColHeaderText.trim().isEmpty();
        rootTable.addColumn(valueColumn, isBlank ? VALUE_COL_HEADER_TXT : valueColHeaderText);
        
        rootTable.addColumn(deleteColumn, new Header<String>(deleteHeaderCell) {
            
            @Override
            public String getValue() {
                return GatClientBundle.INSTANCE.add_image().getSafeUri().asString();
            }
        });
    }
    
    /**
     * Removes any entries from the current list being edited which do 
     * not contain a value for both the name and the value
     */
    private void cleanList() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("cleanList()");
        }
        
        Iterator<Nvpair> nvIter = dataModel.getList().iterator();
        while(nvIter.hasNext()) {
            if(!isCompleteNameValuePair(nvIter.next())) {
                nvIter.remove();
            }
        }
    }
    
    /**
     * Determines whether a name value pair contains data for both its name and value fields
     * @param nv the name value pair to test
     * @return
     */
    private boolean isCompleteNameValuePair(Nvpair nv) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("isCompleteNameValuePair(" + nameValueAsString(nv) + ")");
        }
        
        return nv.getName() != null 
                && !nv.getName().isEmpty() 
                && nv.getValue() != null 
                && !nv.getValue().isEmpty();
    }
    
    /**
     * Assigns the command to be executed whenever the underlying metadata is changed
     * 
     * @param command the command to be executed
     */
    public void setOnChangeCommand(Command command){
        this.onChangeCommand = command;
    }
    
    /**
     * Executes whatever command is waiting for a change event, assuming such a command has been assigned
     */
    private void onChange(){
        
        if(onChangeCommand != null && isEnabled){
            onChangeCommand.execute();
        }
    }
    
    /**
     * Creates a string representation of a name value pair
     * @param nv the name value pair to create a string representation for
     * @return the string representation of the name value pair
     */
    private String nameValueAsString(Nvpair nv) {
        if(nv == null) {
            return "null";
        }
        
        String name = nv.getName() != null ? "\"" + nv.getName() + "\"" : nv.getName();
        String value = nv.getValue() != null ? "\"" + nv.getValue() + "\"" : nv.getValue();
        return name + ": " + value;
    }
}