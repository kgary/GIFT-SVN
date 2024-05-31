/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.widgets;

import generated.ped.MetadataAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.gwt.client.widgets.DynamicSelectionCell;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * An editor that allows the user to add to, remove from, and modify a
 * collection of MetadataAttributes.
 * 
 * @author elafave
 *
 */
public class MetadataAttributesEditor extends Composite {
	
	/** The ui binder. */
    interface MetadataAttributesEditorUiBinder extends UiBinder<Widget, MetadataAttributesEditor> {} 
	private static MetadataAttributesEditorUiBinder uiBinder = GWT.create(MetadataAttributesEditorUiBinder.class);
	
	@UiField
	protected DataGrid<MetadataAttribute> dataGrid;
	
	private DynamicSelectionCell attributeSelectionCell;
	
	private Column<MetadataAttribute, String> attributeColumn;
	
	@UiField
	protected Button addButton;
	
	@UiField
	protected Button removeButton;
	
	/**
	 * List of MetadataAttributes representing the "model".
	 */
	private List<MetadataAttribute> metadataAttributes;
	
	private MerrillQuadrantEnum quadrant;
	
	/**
	 * List of MetadataAttributes representing the "view".
	 */
	private ListDataProvider<MetadataAttribute> dataProvider = new ListDataProvider<MetadataAttribute>();
	
	private SingleSelectionModel<MetadataAttribute> selectionModel = new SingleSelectionModel<MetadataAttribute>();
    
	private ListHandler<MetadataAttribute> sortHandler = new ListHandler<MetadataAttribute>(dataProvider.getList());
	
	private HashMap<MerrillQuadrantEnum, ArrayList<String>> quadrantToAcceptableAttributes = new HashMap<MerrillQuadrantEnum, ArrayList<String>>();
	
    public MetadataAttributesEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        //Define the values the user can pick from for each quadrant.
		ArrayList<String> acceptableAttributesForRule = new ArrayList<String>();
		ArrayList<String> acceptableAttributesForExample = new ArrayList<String>();
		ArrayList<String> acceptableAttributesForRecall = new ArrayList<String>();
        ArrayList<String> acceptableAttributesForPractice = new ArrayList<String>();
        
        List<MetadataAttributeEnum> metadataAttributeEnums = MetadataAttributeEnum.VALUES();
        for(MetadataAttributeEnum metadataAttributeEnum : metadataAttributeEnums) {
        	if(metadataAttributeEnum.isContentAttribute()) {
        		String displayName = metadataAttributeEnum.getDisplayName();
        		acceptableAttributesForRule.add(displayName);
        		acceptableAttributesForExample.add(displayName);
        		acceptableAttributesForRecall.add(displayName);
        	}
        	
        	if(metadataAttributeEnum.isPracticeAttribute()) {
        		String displayName = metadataAttributeEnum.getDisplayName();
        		acceptableAttributesForPractice.add(displayName);
        	}
        }
        Collections.sort(acceptableAttributesForRule);
        Collections.sort(acceptableAttributesForExample);
        Collections.sort(acceptableAttributesForRecall);
        Collections.sort(acceptableAttributesForPractice);
        
        quadrantToAcceptableAttributes.put(MerrillQuadrantEnum.RULE, acceptableAttributesForRule);
        quadrantToAcceptableAttributes.put(MerrillQuadrantEnum.EXAMPLE, acceptableAttributesForExample);
        quadrantToAcceptableAttributes.put(MerrillQuadrantEnum.RECALL, acceptableAttributesForRecall);
        quadrantToAcceptableAttributes.put(MerrillQuadrantEnum.PRACTICE, acceptableAttributesForPractice);
        
        //Create the column that provides those options to the user.
        quadrant = MerrillQuadrantEnum.RULE;
        attributeSelectionCell = new DynamicSelectionCell(acceptableAttributesForRule);
        attributeColumn = new Column<MetadataAttribute, String>(attributeSelectionCell) {
			@Override
			public String getValue(MetadataAttribute metadataAttribute) {
				String value = metadataAttribute.getValue();
				MetadataAttributeEnum metadataAttributeEnum = MetadataAttributeEnum.valueOf(value);
				return metadataAttributeEnum.getDisplayName();
			}
        };
        
        //Respond to user selections
        FieldUpdater<MetadataAttribute, String> fieldUpdater = new FieldUpdater<MetadataAttribute, String>(){
        	@Override
        	public void update(int index, MetadataAttribute metadataAttribute, String value) {
        		MetadataAttributeEnum metadataAttributeEnum = MetadataAttributeEnum.valueOf(value);
        		metadataAttribute.setValue(metadataAttributeEnum.getName());
        		
        		SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
        	}
        };
        
        attributeColumn.setFieldUpdater(fieldUpdater);
        attributeColumn.setSortable(true);
        
        //Define how the column is sorted.
        Comparator<MetadataAttribute> attributeComparator = new Comparator<MetadataAttribute>() {
			@Override
			public int compare(MetadataAttribute attribute1, MetadataAttribute attribute2) {
				MetadataAttributeEnum metadataAttributeEnum1 = MetadataAttributeEnum.valueOf(attribute1.getValue());
				MetadataAttributeEnum metadataAttributeEnum2 = MetadataAttributeEnum.valueOf(attribute2.getValue());
				
				String value1 = metadataAttributeEnum1.getDisplayName();
				String value2 = metadataAttributeEnum2.getDisplayName();
				
				int result = value1.compareTo(value2);
				return result;
			}
		};
        sortHandler.setComparator(attributeColumn, attributeComparator);
        
        //Initialize the data grid.
        dataGrid.addColumn(attributeColumn, "Metadata Attributes");
        dataGrid.setSelectionModel(selectionModel);
        dataGrid.addColumnSortHandler(sortHandler);
        dataProvider.addDataDisplay(dataGrid);
        
        ClickHandler addHandler = new ClickHandler() {
        	@Override
			public void onClick(ClickEvent arg0) {
        		addMetadataAttribute();
			}
        };
        addButton.addClickHandler(addHandler);
        
        ClickHandler removeHandler = new ClickHandler() {
        	@Override
			public void onClick(ClickEvent arg0) {
        		if(metadataAttributes.size() == 1) {
        			WarningDialog.warning("Unable to remove", "The list must contain at least one metadata attribute.");
        			return;
        		}
        		
        		MetadataAttribute selectedAttribute = selectionModel.getSelectedObject();
        		if(selectedAttribute != null) {
	        		metadataAttributes.remove(selectedAttribute);
	        		dataProvider.getList().remove(selectedAttribute);
        		} else {
        			WarningDialog.warning("Missing selection", "Please select a metadata attribute before requesting it be removed.");
        		}
			}
        };
        removeButton.addClickHandler(removeHandler);
    }
    
    /**
     * Sets the list of MetadataAttributes to display. As the user makes
     * changes the supplied list will be updated accordingly.
     * @param quadrant Merrill Quadrant the metadata attributes belong to.
     * @param metadataAttributes Collection of MetadataAttributes to edit.
     */
    public void setAttributes(MerrillQuadrantEnum quadrant, List<MetadataAttribute> metadataAttributes) {
    	this.quadrant = quadrant;
    	this.metadataAttributes = metadataAttributes;
    	
    	//Remove MetadataAttributes that aren't appropriate for the quadrant.
    	ArrayList<String> options = quadrantToAcceptableAttributes.get(quadrant);
    	Iterator<MetadataAttribute> i = metadataAttributes.iterator();
    	while(i.hasNext()) {
    		String value = i.next().getValue();
    		MetadataAttributeEnum metadataAttributeEnum = MetadataAttributeEnum.valueOf(value);
			String displayName = metadataAttributeEnum.getDisplayName();
			
			if(!options.contains(displayName)) {
				i.remove();
			}
    	}
    	
    	//Replace the old MetadataAttributes
    	dataProvider.getList().clear();
    	attributeSelectionCell.setOptions(options);
    	dataProvider.getList().addAll(metadataAttributes);
    	
    	//The ColumnSortList keeps the entire history of columns being
		//sorted (programmatically or by the user). So if it is empty
		//it means we're loading the table for the first time and should
		//therefore sort it by attribute. If it isn't empty we still
    	//have to fire the event to make sure the new list is sorted otherwise
    	//the sort icon will be shown with an unsorted list.
    	ColumnSortList columnSortList = dataGrid.getColumnSortList();
		if(columnSortList.size() == 0)
		{
			columnSortList.push(attributeColumn);
		}
		ColumnSortEvent.fire(dataGrid, columnSortList);
		
		//Make sure we the list isn't empty.
		if(metadataAttributes.size() == 0) {
			addMetadataAttribute();
		}
    }

	public void redraw() {
		dataGrid.redraw();
	}
	
	private void addMetadataAttribute() {
		String displayName = quadrantToAcceptableAttributes.get(quadrant).get(0);
		MetadataAttributeEnum metadataAttributeEnum = MetadataAttributeEnum.valueOf(displayName);
		
		MetadataAttribute metadataAttribute = new MetadataAttribute();
		metadataAttribute.setValue(metadataAttributeEnum.getName());
		
		metadataAttributes.add(metadataAttribute);
		dataProvider.getList().add(metadataAttribute);
		
		ColumnSortList columnSortList = dataGrid.getColumnSortList();
		ColumnSortEvent.fire(dataGrid, columnSortList);
		
		selectionModel.setSelected(metadataAttribute, true);
	}
}
