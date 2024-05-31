/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.math.BigInteger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.OptionalGuidanceCreator;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateMetadataAttribute;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppInteropEditor;

/**
 * A dialog used to let users select application files to use in a Merrill's Branch Point and define their metadata attributes
 * 
 * @author nroberts
 */
public class AddApplicationDialog extends Composite {

	/** The UI binder used to populate this widget from its .ui.xml file */
	private static final Binder binder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, AddApplicationDialog> {
	}
	
	@UiField(provided=true)
	protected CellTable<CandidateConcept> conceptsTable = new CellTable<CandidateConcept>();
	
	@UiField(provided=true)
	protected CellTable<CandidateMetadataAttribute> attributesTable = new CellTable<CandidateMetadataAttribute>();
	
	
	//'Concepts:' table columns ----------------------------------------------------------------------------------------------------------
	
	/** The checkbox column for the 'Concepts:' table */
    private Column<CandidateConcept, Boolean> conceptSelectionColumn = new Column<CandidateConcept, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateConcept candidate) {
			
			return candidate.isChosen();
		}
    	
    };
    
    /** The name column for the 'Concepts:' table */
    private Column<CandidateConcept, String> conceptNameColumn = new Column<CandidateConcept, String>(new TextCell()){

		@Override
		public String getValue(CandidateConcept candidate) {

			if(candidate.getConceptName() != null ){
				return candidate.getConceptName();
			}
			
			return null;
		}
    	
    };
    
    
    //'Attributes:' table columns --------------------------------------------------------------------------------------------------------
	
    /** The checkbox column for the 'Concepts:' table */
    private Column<CandidateMetadataAttribute, Boolean> attributeSelectionColumn = new Column<CandidateMetadataAttribute, Boolean>(new CheckboxCell()){

		@Override
		public Boolean getValue(CandidateMetadataAttribute candidate) {
			
			return candidate.isChosen();
		}
    	
    };
    
    /** The name column for the 'Concepts:' table */
    private Column<CandidateMetadataAttribute, String> attributeNameColumn = new Column<CandidateMetadataAttribute, String>(new TextCell()){

		@Override
		public String getValue(CandidateMetadataAttribute candidate) {

			if(candidate.getAttribute() != null ){
				return candidate.getAttribute().getDisplayName();
			}
			
			return null;
		}
    	
    };
    
    @UiField
	protected Button uploadButton;
	
    @UiField
	protected Button cancelButton;
	
	@UiField
	protected OptionalGuidanceCreator practiceGuidanceCreator;
	
	@UiField
	protected HTML validationErrorText;
	
	@UiField
	protected TrainingAppInteropEditor taInteropEditor;

	@UiField
	protected Modal addApplicationModal;
	
	@UiField
	protected InlineHTML title;
	
	@UiField
	protected Button changeTypeButton;
	
	/**
	 * Creates a new 'Add Application' dialog
	 */
	public AddApplicationDialog() {
		
		initWidget(binder.createAndBindUi(this));
        
        // initialize 'Concepts:' table
        
        conceptsTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        conceptsTable.setPageSize(Integer.MAX_VALUE);
        
        conceptsTable.addColumn(conceptSelectionColumn);
        conceptsTable.addColumn(conceptNameColumn);
        conceptsTable.setColumnWidth(conceptSelectionColumn, "50px");
        
        VerticalPanel emptyConceptsWidget = new VerticalPanel();
        emptyConceptsWidget.setSize("100%", "100%");
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        Label emptyConceptsLabel = new Label("No course concepts were found. Please make sure you have added a list or hierarchy of concepts to the course summary.");
        emptyConceptsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyConceptsWidget.add(emptyConceptsLabel);
        
        emptyConceptsWidget.add(new HTML("<br>"));
        
        conceptsTable.setEmptyTableWidget(emptyConceptsWidget);
        
        
        // initialize 'Attributes:' table
        
        attributesTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        attributesTable.setPageSize(Integer.MAX_VALUE);
        
        attributesTable.addColumn(attributeSelectionColumn);
        attributesTable.addColumn(attributeNameColumn);
        attributesTable.setColumnWidth(attributeSelectionColumn, "50px");
        
        VerticalPanel emptyAttributesWidget = new VerticalPanel();
        emptyAttributesWidget.setSize("100%", "100%");
        
        emptyAttributesWidget.add(new HTML("<br>"));
        
        Label emptyAttributesLabel = new Label("Please select a checked concept to view the attributes assigned to it.");
        emptyAttributesLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyAttributesWidget.add(emptyAttributesLabel);
        
        emptyAttributesWidget.add(new HTML("<br>"));
        
        attributesTable.setEmptyTableWidget(emptyAttributesWidget);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addApplicationModal.hide();
			}
		});
		
		practiceGuidanceCreator.setTrainingAppEmbedded(true);
		practiceGuidanceCreator.hideMessageEditor(true);
		
	}
        
	/**
	 * Gets the table used to display this widget's selectable concepts
	 * 
	 * @return the table used to display concepts
	 */
	public HasData<CandidateConcept> getConceptsTable(){
		return conceptsTable;
	}
	
	/**
	 * Sets the field updater used to handle when the user interacts with a concept's check box
	 * 
	 * @param updater the field update to use
	 */
	public void setConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater){
    	conceptSelectionColumn.setFieldUpdater(updater);
    }
	
    public HasData<CandidateMetadataAttribute> getAttributesTable(){
        return attributesTable;
    }
	
	/**
	 * Sets the field updater used to handle when the user interacts with a metadata attribute's check box
	 * 
	 * @param updater
	 */
	public void setAttributeSelectionColumnFieldUpdater(FieldUpdater<CandidateMetadataAttribute, Boolean> updater){
    	attributeSelectionColumn.setFieldUpdater(updater);
    }
	
	/**
	 * Redraws the concepts table,re-rendering every row inside of it
	 */
	public void redrawConceptsTable(){
		conceptsTable.redraw();
	}
	
	/**
	 * Sets the dialog title
	 * 
	 * @param text The title of the dialog
	 */
	public void setText(String text) {
		title.setText(text);
	}
	
	/**
	 * Displays the dialog
	 */
	public void center(){
		
		addApplicationModal.show();

		conceptsTable.redraw();
		attributesTable.redraw();
	}
	
	/**
	 * Hides the dialog
	 */
	public void hide() {
		addApplicationModal.hide();
	}
	
	/**
	 * Gets the button used to finish adding the practice application
	 * 
	 * @return the button
	 */
	public HasClickHandlers getAddButton(){
		return uploadButton;
	}
	
	/**
	 * Gets the button used to finish adding the practice application
	 * 
	 * @return the button
	 */
	public HasEnabled getAddButtonEnabled(){
		return uploadButton;
	}
	
	/**
	 * Gets the element used to show validation error text in this widget
	 * 
	 * @return the validation error element
	 */
	public HasHTML getValidationErrorText(){
		return validationErrorText;
	}
	
	/**
	 * Gets the editor used to edit pratice applications
	 * 
	 * @return the practice application editor
	 */
	public TrainingAppInteropEditor getTAInteropEditor(){
		return taInteropEditor;
	}

	/**
     * Sets the survey context id that the course is using.
     * 
     * @param surveyContextId The surveyContextId of the current course.
     */
    public void setCourseSurveyContextId(BigInteger surveyContextId) {
        taInteropEditor.setCourseSurveyContextId(surveyContextId);
        
    }

    /**
     * Gets the editor used to author optional guidance messages to show while the practice application loads
     * 
     * @return the guidance editor
     */
	public OptionalGuidanceCreator getPracticeGuidanceCreator() {		
		return practiceGuidanceCreator;
	}
	
	/**
	 * Gets the button used to change the training application type
	 * 
	 * @return the button
	 */
	public Button getChangeApplicationButton(){
		return changeTypeButton;
	}
}
