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

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveys;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveysResult;

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

/**
 * A dialog used to select a Survey Context Survey from the database
 * 
 * @author nroberts
 */
public class SelectSurveyContextSurveyDialog extends ModalDialogBox implements HasValue<SurveyContextSurvey>{

	private static SelectSurveyContextSurveyDialogUiBinder uiBinder = GWT
			.create(SelectSurveyContextSurveyDialogUiBinder.class);

	interface SelectSurveyContextSurveyDialogUiBinder extends
			UiBinder<Widget, SelectSurveyContextSurveyDialog> {
	}
	
	private Column<SurveyContextSurvey, String> nameColumn = new Column<SurveyContextSurvey,String>(new TextCell()){

		@Override
		public String getValue(SurveyContextSurvey object) {
			
			if(object == null){
				return "";
				
			} else {
			    // Display the name of the survey in the text box, but the value (hidden from the user) should be
			    // the survey context key.
				return object.getSurvey().getName();
			
			}
		}
		
	};
	
	@UiField(provided=true)
	protected CellTable<SurveyContextSurvey> surveyContextSurveyTable = new CellTable<SurveyContextSurvey>();
	
	private ListDataProvider<SurveyContextSurvey> tableDataProvider = new ListDataProvider<SurveyContextSurvey>();
	
	private SingleSelectionModel<SurveyContextSurvey> tableSelectionModel = new SingleSelectionModel<SurveyContextSurvey>();
	
	private SurveyContextSurvey value = null;
	
	private Button confirmButton = new Button("Set Survey");	
	private Button cancelButton = new Button("Cancel");
	
	private BigInteger surveyContextId = null;

	/**
	 * Creates a new dialog for selecting a survey context survey 
	 */
	public SelectSurveyContextSurveyDialog() {
		setWidget(uiBinder.createAndBindUi(this));
		
		setText("Select a Survey");
		setGlassEnabled(true);
		
		surveyContextSurveyTable.setPageSize(Integer.MAX_VALUE);
		surveyContextSurveyTable.addColumn(nameColumn);
		surveyContextSurveyTable.setSelectionModel(tableSelectionModel);
		surveyContextSurveyTable.setEmptyTableWidget(new HTML(""
				+ "<span style='font-size: 12pt;'>"
				+ 	"No surveys were found in the selected survey context; therefore, no surveys are available to choose from."
				+ "</span>"));
		
		tableDataProvider.addDataDisplay(surveyContextSurveyTable);
		
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
				
				ValueChangeEvent.fire(SelectSurveyContextSurveyDialog.this, value);
			}
		});
		
		setFooterWidget(footer);	
	}
	
	@Override
	public void center(){
		
		int id;
		
		if(surveyContextId == null){
			
			WarningDialog.error("Missing selection", "No survey context has been selected; therefore, no surveys are available to choose from.<br/><br/>"
					+ "Please select a survey context in this file's properties before attempting to select a survey.");
			return;
		}
		
		id = surveyContextId.intValue();
		
		BsLoadingDialogBox.display("Getting Surveys", "Please wait while we get the latest list of surveys.");
		
		AsyncCallback<FetchSurveyContextSurveysResult> callback = new AsyncCallback<FetchSurveyContextSurveysResult>(){

			@Override
			public void onFailure(Throwable t) {
				
				BsLoadingDialogBox.remove();
				
				if(t instanceof DetailedException){
					
					DetailedException e = (DetailedException) t;
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							e.getReason(), 
							e.getDetails(), 
							DetailedException.getFullStackTrace(t));
					
					dialog.center();
					
				} else {
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							"A problem occurred while getting the list of surveys.", 
							t.getMessage(), 
							DetailedException.getFullStackTrace(t));
					
					dialog.center();
				}
			}

			@Override
			public void onSuccess(FetchSurveyContextSurveysResult result) {
				
				BsLoadingDialogBox.remove();
				
				if(result != null){
					
					if(result.isSuccess()){
					
						tableDataProvider.getList().clear();
						
						SurveyContextSurvey matchedName = null;
						SurveyContextSurvey matchedId = null;
						
						for(SurveyContextSurvey survey : result.getSurveys()){
							
							tableDataProvider.getList().add(survey);
							
							if(value != null && (matchedName == null || matchedId == null)){
								
								if(value.getKey() != null && value.getKey().equals(survey.getKey())){									
									 matchedName = survey;
								}
								
								if(value.getSurvey() != null 
										&& value.getSurvey().getId() != 0 
										&& survey.getSurvey().getId() != 0
										&& value.getSurvey().getId() == survey.getSurvey().getId()){		
									
									 matchedId = survey;
								}
							}
						}
						
						tableDataProvider.refresh();
						
						if (matchedName != null) {
						    tableSelectionModel.setSelected(matchedName, true);
						} else if(matchedId != null){
						    
							tableSelectionModel.setSelected(matchedId, true);
							
						}else if(value != null && value.getKey() != null){
							
							OkayCancelDialog.show(
									"Previous Survey Not Found", 
									"The previously chosen survey was not found in the database and may have been deleted "
									+ "since the last time it was selected. If you change your survey selection now, you won't "
									+ "be able to choose the previous survey again unless it is recreated in the database."
									+ "<br/><br/>Are you sure you want to select a new survey and discard the old selection?", 
									"Yes, I want to select a new survey", 
									new OkayCancelCallback() {
										
										@Override
										public void okay() {
											SelectSurveyContextSurveyDialog.super.center();
										}
										
										@Override
										public void cancel() {
											SelectSurveyContextSurveyDialog.super.hide();
										}
									});
							
							return;
						}	
						
						SelectSurveyContextSurveyDialog.super.center();
						
					} else {
						
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(
								result.getErrorMsg() != null 
									? result.getErrorMsg() 
									: "A problem occurred while getting the list of surveys.", 
								result.getErrorDetails(), 
								result.getErrorStackTrace());
						
						dialog.center();
					}
					
				} else {
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							"A problem occurred while getting the list of surveys.", 
							"A response was received from the server, but no surveys were found in the response.", 
							null);
					
					dialog.center();
				}
			}		
		};
		
		FetchSurveyContextSurveys action = new FetchSurveyContextSurveys();
		action.setSurveyContextId(id);
		SharedResources.getInstance().getDispatchService().execute(action, callback);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<SurveyContextSurvey> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public SurveyContextSurvey getValue() {
		return value;
	}

	@Override
	public void setValue(SurveyContextSurvey value) {
		setValue(value, false);
	}

	@Override
	public void setValue(SurveyContextSurvey value, boolean fireEvents) {
		
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
}
