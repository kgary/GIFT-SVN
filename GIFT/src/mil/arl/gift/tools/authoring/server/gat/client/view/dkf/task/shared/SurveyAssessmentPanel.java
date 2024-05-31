/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import generated.dkf.Question;
import generated.dkf.Reply;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.model.record.AssessmentRecord;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SelectSurveyContextSurveyButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.cell.ExtendedSelectionCell;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

/**
 * An editor used to author survey assessments
 * 
 * @author nroberts
 */
public class SurveyAssessmentPanel extends Composite {

	private static SurveyAssessmentPanelUiBinder uiBinder = GWT
			.create(SurveyAssessmentPanelUiBinder.class);

	interface SurveyAssessmentPanelUiBinder extends
			UiBinder<Widget, SurveyAssessmentPanel> {
	}
	
	/**
	 * The Interface GetValue.
	 *
	 * @param <C> the generic type
	 */
	private static interface GetValue<C> {
		
		/**
		 * Gets the value.
		 *
		 * @param record the record
		 * @return the value
		 */
		C getValue(AssessmentRecord record);
		
	}
	
	/** The no survey item. */
	public static String NO_SURVEY_ITEM = "Could not find any surveys. Make sure you have selected a survey context in the Dkf Header from which to get the list of surveys.";
	
	public static String NO_SURVEYS_IN_CONTEXT = "Could not find any surveys. Make sure the Survey Context you have selected has Surveys referencing it.";
	public static String NO_PERMISSION_TO_SURVEY = "(You do not have permissions to this survey) - ";
	
	public static String NO_QUESTION_PERMISSION = "(You do not have permissions to the dkf survey context, so you may not select the questions associated with it's survey)";
	/** The survey select input. */
	protected @UiField SelectSurveyContextSurveyButton surveySelectInput;

	/** The question assessment data display. */
	protected @UiField CellTable<AssessmentRecord> questionAssessmentDataDisplay;
	
	/** The add question button input. */
	@UiField(provided=true)
	protected  Image addQuestionButtonInput = new Image(GatClientBundle.INSTANCE.add_image());
	
	/** The delete question button input. */
	protected @UiField Button deleteQuestionButtonInput;
	
	@UiField
	protected HTML surveyWarning;

	public SurveyAssessmentPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		
		surveySelectInput.setValue(null);
		
		questionAssessmentDataDisplay.setPageSize(Integer.MAX_VALUE);
		
		initColumns();
	}
	
	/**
	 * Initializes the columns used in the question assessment DataGrid.
	 */
	private void initColumns() {
		
		TextCell questionTextCell = new TextCell(){
			
			@Override
			public void render(Context context, SafeHtml value, SafeHtmlBuilder sb){
				
				if(((AssessmentRecord)context.getKey()).getQuestionOrReply() instanceof Question){
					super.render(context, value, sb);
					
				} else {
					//render nothing for this row
				}
			}
		};
		
		TextCell replyTextCell = new TextCell(){
			
			@Override
			public void render(Context context, SafeHtml value, SafeHtmlBuilder sb){
				
				if(((AssessmentRecord)context.getKey()).getQuestionOrReply() instanceof Reply){
					super.render(context, value, sb);
					
				} else {
					//render nothing for this row
				}
			}
		};
		
		ExtendedSelectionCell assessmentSelectionCell = new ExtendedSelectionCell(){
			
			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb){
				
				if(((AssessmentRecord)context.getKey()).getQuestionOrReply() instanceof Reply){
					super.render(context, value, sb);
					
				} else {
					//render nothing for this row
				}	
			}
		};
		
		Column<AssessmentRecord, String> questionTextColumn = createColumn(
				
			questionTextCell,
			
			new GetValue<String>() {
				@Override
				public String getValue(AssessmentRecord record) {
					
					if(record.getQuestionOrReply() instanceof Question){
						return record.getDisplayText();
					}
					
					return "";
				}
			}, 

			null //values in this column cannot be edited
		);
		
		questionAssessmentDataDisplay.addColumn(questionTextColumn, "Question");
		questionAssessmentDataDisplay.setColumnWidth(questionTextColumn, "50%");
		questionAssessmentDataDisplay.setEmptyTableWidget(new HTML(""
				+ "<span style='font-size: 12pt;'>"
				+ 	"No questions have been added; therefore, no "
				+ 	"questions assessments will be performed."
				+ "</span>"));
		
		Column<AssessmentRecord, String> replyTextColumn = createColumn(
				
			replyTextCell,
			
			new GetValue<String>() {
				@Override
				public String getValue(AssessmentRecord record) {
					
					if(record.getQuestionOrReply() instanceof Reply){
						return record.getDisplayText();
					}
					
					return "";
				}
			}, 

			null //values in this column cannot be edited
		);
		
		questionAssessmentDataDisplay.addColumn(replyTextColumn, "Reply");
		questionAssessmentDataDisplay.setColumnWidth(replyTextColumn, "25%");
		
		List<String> assessmentChoices = new ArrayList<String>();
		
		for(AssessmentLevelEnum assessmentLevelEnum : AssessmentLevelEnum.VALUES()){			
			assessmentChoices.add(assessmentLevelEnum.getDisplayName());
		}
		
		assessmentSelectionCell.setDefaultOptions(assessmentChoices);
		
		Column<AssessmentRecord, String> assessmentSelectionColumn = createColumn(
				
				assessmentSelectionCell,
				
				new GetValue<String>() {
					@Override
					public String getValue(AssessmentRecord record) {
						
						if(record.getQuestionOrReply() instanceof Reply){
							return AssessmentLevelEnum.valueOf(((Reply) record.getQuestionOrReply()).getResult()).getDisplayName();
						}
						
						return "";
					}
				}, 

				new FieldUpdater<AssessmentRecord, String>() {
					@Override
					public void update(int index, AssessmentRecord record, String value) {	
						
						if(record.getQuestionOrReply() instanceof Reply){
							
							((Reply) record.getQuestionOrReply()).setResult(value);
							SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
						}
					}
				}
			);
			
			questionAssessmentDataDisplay.addColumn(assessmentSelectionColumn, "Assessment");
			questionAssessmentDataDisplay.setColumnWidth(assessmentSelectionColumn, "25%");
			
		Column<AssessmentRecord, SafeHtml> statusColumn = new Column<AssessmentRecord, SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(AssessmentRecord record) {
				
				return record.getStatusHtml();
			}
		};
		
		questionAssessmentDataDisplay.addColumn(statusColumn, "Status");
		
		questionAssessmentDataDisplay.redraw();
	}
	
	/**
	 * Creates the column.
	 *
	 * @param <C> the generic type
	 * @param cell the cell
	 * @param getter the getter
	 * @param fieldUpdater the field updater
	 * @return the column
	 */
	private <C> Column<AssessmentRecord, C> createColumn(Cell<C> cell,
			final GetValue<C> getter, FieldUpdater<AssessmentRecord, C> fieldUpdater) {
		
		Column<AssessmentRecord, C> column = new Column<AssessmentRecord, C>(cell) {
			@Override
			public C getValue(AssessmentRecord object) {
				return getter.getValue(object);
			}
		};
		
		if(fieldUpdater != null){
			column.setFieldUpdater(fieldUpdater);
		}	
		
		return column;
	}

	public HasValue<String> getSurveySelectInput() {
		return surveySelectInput;
	}

	public HasData<AssessmentRecord> getQuestionAssessmentDataDisplay() {
		return questionAssessmentDataDisplay;
	}

	public HasClickHandlers getAddQuestionButtonInput() {
		return addQuestionButtonInput;
	}

	public HasClickHandlers getDeleteQuestionButtonClickInput() {
		return deleteQuestionButtonInput;
	}

	public HasEnabled getDeleteQuestionButtonEnabledInput() {
		return deleteQuestionButtonInput;
	}
	
	public void redraw(){
		questionAssessmentDataDisplay.redraw();
	}
		
	public String getSelectedSurveyChoice(){
		return surveySelectInput.getValue();
	}
	
	public void setSelectedSurveyChoice(String choice) {
		surveySelectInput.setValue(choice, true);
	}
	
	public void setSelectedSurveyChoice(String choice, boolean fireEvents) {
		surveySelectInput.setValue(choice, fireEvents);
	}
	
	public void showSurveyWarning(String warningText){
		
		surveyWarning.setVisible(true);
		surveyWarning.setText(warningText);
	}
	
	public void hideSurveyWarning(){
		
		surveyWarning.setVisible(false);
		surveyWarning.setText("");
	}
	
	
	public void setSelectedSurveyContext(BigInteger surveyContextId) {
		surveySelectInput.setSurveyContextId(surveyContextId);
	}

	/**
	 * Sets the label in the SelectSurveyContextButton widget to display
	 * the survey name of the selected survey context id.
	 * @param choice
	 */
    public void setSelectedSurveyChoiceLabel(String surveyName) {
        surveySelectInput.setText(surveyName);
        
    }

}
