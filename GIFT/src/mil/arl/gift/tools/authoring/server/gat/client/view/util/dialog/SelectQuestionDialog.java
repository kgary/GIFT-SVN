/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.math.BigInteger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyQuestions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyQuestionsResult;

/**
 * A dialog used to select a Question from the database
 * 
 * @author nroberts
 */
public class SelectQuestionDialog extends ModalDialogBox implements HasValue<AbstractSurveyQuestion<?>>{

    /** The UiBinder that combines the ui.xml with this java class */
	private static SelectQuestionDialogUiBinder uiBinder = GWT.create(SelectQuestionDialogUiBinder.class);

	/** Defines the UiBinder that combines the ui.xml with a java class */
	interface SelectQuestionDialogUiBinder extends UiBinder<Widget, SelectQuestionDialog> {
	}
	
	private Column<AbstractSurveyQuestion<?>, String> nameColumn = new Column<AbstractSurveyQuestion<?>,String>(new TextCell()){

		@Override
		public String getValue(AbstractSurveyQuestion<?> object) {
			
			if(object == null || object.getQuestion() == null){
				return "";
				
			} else {
				
				StringBuilder sb = new StringBuilder();
				sb.append(object.getQuestion().getText());
				
				if(object.getQuestion().getQuestionId() != 0){
					sb.append(" (ID: ").append(object.getQuestion().getQuestionId()).append(")");
				}
				
				return sb.toString();
			
			}
		}
		
	};
	
	@UiField(provided=true)
	protected CellTable<AbstractSurveyQuestion<?>> questionTable = new CellTable<AbstractSurveyQuestion<?>>();
	
	private ListDataProvider<AbstractSurveyQuestion<?>> tableDataProvider = new ListDataProvider<AbstractSurveyQuestion<?>>();
	
	private SingleSelectionModel<AbstractSurveyQuestion<?>> tableSelectionModel = new SingleSelectionModel<AbstractSurveyQuestion<?>>();
	
	private AbstractSurveyQuestion<?> value = null;
	
	private Button confirmButton = new Button("Add Question");	
	private Button cancelButton = new Button("Cancel");
	
	private BigInteger surveyContextId = null;
	
	private String surveyGIFTKey = null;

	/**
	 * Creates a new dialog for selecting a survey context survey 
	 */
	public SelectQuestionDialog() {
		setWidget(uiBinder.createAndBindUi(this));
		
		setText("Select a Survey Question");
		setGlassEnabled(true);
		
		questionTable.setPageSize(Integer.MAX_VALUE);
		questionTable.addColumn(nameColumn);
		questionTable.setSelectionModel(tableSelectionModel);
		questionTable.setEmptyTableWidget(new HTML(""
				+ "<span style='font-size: 12pt;'>"
				+ 	"No Multiple Choice, Rating Scale, Free Response, or Matrix of Choices questions were found in the selected survey; therefore, no "
				+ 	"questions are available to choose from."
				+ "</span>"));
		
		tableDataProvider.addDataDisplay(questionTable);
		
		tableSelectionModel.addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				value = tableSelectionModel.getSelectedObject();
			}
		});
		
		setEnterButton(confirmButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		confirmButton.setType(ButtonType.PRIMARY);
		cancelButton.setType(ButtonType.DANGER);
		
		FlowPanel footer = new FlowPanel();
		footer.add(confirmButton);
		footer.add(cancelButton);
		
		confirmButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				
				if(value == null){
					WarningDialog.error("Missing selection", "No survey context selected.");
					return;
				}
				
				ValueChangeEvent.fire(SelectQuestionDialog.this, value);
			}
		});
		
		setFooterWidget(footer);	
	}
	
	@Override
	public void center(){
		
		int id;
		
		String key;
		
		if(surveyContextId == null){
			
			WarningDialog.error("Missing selection", "No survey context has been selected; therefore, no questions are available to choose from.<br/><br/>"
					+ "Please select a survey context and survey to use before attempting to select a question.");
			return;
		}
		
		if(surveyGIFTKey == null || surveyGIFTKey.isEmpty()){
			
			WarningDialog.error("Missing selection", "No survey has been selected; therefore, no questions are available to choose from.<br/><br/>"
					+ "Please select a survey to use before attempting to select a question.");
			return;
		}
		
		id = surveyContextId.intValue();
		key = surveyGIFTKey;
		
		BsLoadingDialogBox.display("Getting Questions", "Please wait while we get the latest list of questions.");
		
		AsyncCallback<FetchSurveyQuestionsResult> callback = new AsyncCallback<FetchSurveyQuestionsResult>(){

			@Override
			public void onFailure(Throwable t) {
				
				BsLoadingDialogBox.remove();
				
				if(t instanceof DetailedException){
					
					DetailedException e = (DetailedException) t;
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							e.getReason() 
								+ "<br/><br/><b>Note:</b> If you have recently selected a new survey context or modified the survey context that was "
								+ "selected, this problem might have occurred because the survey used to get this list "
								+ "does not exist in the new survey context. If this is the case, you will need to select a new survey "
								+ "before you can select any questions.", 
							e.getDetails(), 
							DetailedException.getFullStackTrace(t));
					
					dialog.center();
					
				} else {
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							"A problem occurred while getting the list of surveys." 
									+ "<br/><br/><b>Note:</b> If you have recently selected a new survey context or modified the survey context that was "
									+ "selected, this problem might have occurred because the survey used to get this list "
									+ "does not exist in the new survey context. If this is the case, you will need to select a new survey "
									+ "before you can select any questions.", 
							t.getMessage(), 
							DetailedException.getFullStackTrace(t));
					
					dialog.center();
				}
			}

			@Override
			public void onSuccess(FetchSurveyQuestionsResult result) {
				
				BsLoadingDialogBox.remove();
				
				if(result != null){
					
					if(result.isSuccess()){
					
						tableDataProvider.getList().clear();
						
						AbstractSurveyQuestion<?> matchedName = null;
						AbstractSurveyQuestion<?> matchedId = null;
						
						for(AbstractSurveyQuestion<?> question : result.getSurveyQuestions()){
							
							if(question.getQuestion() != null){
								
								tableDataProvider.getList().add(question);
								
								if(value != null && (matchedName == null || matchedId == null)){
									
									if(value.getQuestion() != null 
											&& value.getQuestion().getText() != null 
											&& question.getQuestion() != null 
											&& value.getQuestion().getText().equals(question.getQuestion().getText())){									
										 matchedName = question;
									}
									
									if(value.getId() != 0 
											&& value.getId() == question.getId()){		
										
										 matchedId = question;
									}
								}
							}
						}
						
						
						if(matchedId != null){
							tableSelectionModel.setSelected(matchedId, true);
							
						} else if(matchedName != null){
							tableSelectionModel.setSelected(matchedName, true);
						}
						
						tableDataProvider.refresh();
						
						SelectQuestionDialog.super.center();
						
					} else {
						
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(
								result.getErrorMsg() != null 
										? (result.getErrorMsg() 
												+ "<br/><br/><b>Note:</b> If you have recently selected a new survey context or modified the survey context that was "
												+ "selected, this problem might have occurred because the survey used to get this list "
												+ "does not exist in the new survey context. If this is the case, you will need to select a new survey "
												+ "before you can select any questions.")
										: ("A problem occurred while getting the list of questions."
												+ "<br/><br/><b>Note:</b> If you have recently selected a new survey context or modified the survey context that was "
												+ "selected, this problem might have occurred because the survey used to get this list "
												+ "does not exist in the new survey context. If this is the case, you will need to select a new survey "
												+ "before you can select any questions."), 
								result.getErrorDetails(), 
								result.getErrorStackTrace());
						
						dialog.center();
					}
					
				} else {
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							"A problem occurred while getting the list of questions.<br/><br/>"
									+ "<b>Note:</b> If you have recently selected a new survey context or modified the survey context that was "
									+ "selected, this problem might have occurred because the survey used to get this list "
									+ "does not exist in the new survey context. If this is the case, you will need to select a new survey "
									+ "before you can select any questions.", 
							"A response was received from the server, but no questions were found in the response.", 
							null);
					
					dialog.center();
				}
			}		
		};
		
		FetchSurveyQuestions action = new FetchSurveyQuestions();
		action.setSurveyContextId(id);
		action.setGiftKey(key);
		SharedResources.getInstance().getDispatchService().execute(action, callback);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<AbstractSurveyQuestion<?>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public AbstractSurveyQuestion<?> getValue() {
		return value;
	}

	@Override
	public void setValue(AbstractSurveyQuestion<?> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(AbstractSurveyQuestion<?> value, boolean fireEvents) {
		
		this.value = value;
		
		if(fireEvents){
			ValueChangeEvent.fire(this, value);
		}
	}
	
	/**
	 * Sets the survey context ID to use to get the list of surveys. If no survey context ID is set, then no surveys will be loaded.
	 * 
	 * @param id the ID to use
	 */
	public void setSurveyContextId(BigInteger id){
		surveyContextId = id;
	}
	
	/**
	 * Sets the survey GIFT key to use to get the list of surveys. If no survey GIFT key is set, then no surveys will be loaded.
	 * 
	 * @param key the key to use
	 */
	public void setSurveyGIFTKey(String key){
		surveyGIFTKey = key;
	}
}
