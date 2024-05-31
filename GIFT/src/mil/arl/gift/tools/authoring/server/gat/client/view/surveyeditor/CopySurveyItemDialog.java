/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.shared.event.TabShowHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.gwt.DataGrid;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyChangeEditMode;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySelectQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.TopLevelModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQuery;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQueryResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyHeader;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyReturnResult;

/**
 * A dialog used to select survey items from other surveys to copy into a survey being edited
 * 
 * @author nroberts
 */
public class CopySurveyItemDialog extends PopupPanel {
	
	private static Logger logger = Logger.getLogger(CopySurveyItemDialog.class.getName());
	
	private static final String STYLE_WRITING = "copySurveyItemDialogWriting";
	private static final String STYLE_SCORING = "copySurveyItemDialogScoring";
	
	/** The text shown to the user when no surveys are found in the database for surveys they own */
    private static final String NO_USER_SURVEYS_TEXT = "<span style='font-size: 12pt;'>"
            +   "You haven't authored any surveys yet.  Create a new survey course object to author a survey."
            + "</span>";
    
    /** The text shown to the user when no surveys are found in the database for public surveys */
    private static final String NO_PUBLIC_SURVEYS_TEXT = "<span style='font-size: 12pt;'>"
            +   "There are no public surveys.  Not sure what happened here because GIFT normally contains several already authored surveys."
            + "</span>";
    
	
	 /** A regular expression used to locate words and phrases in a search text expression */
	private static final String wordExpression = 
		"-?\"[^\"]*\"" +	//double quotes around phrases(s)
       "|-?[A-Za-z0-9']+"  //single word
	;
	
	/** A regular expression used to locate binary operators in a search text expression */
	private static final String binaryOperatorExpression = "(" + wordExpression + ")(\\s+(AND|OR)\\s+(" + wordExpression + "))+";
	
	/** The text shown to the user when no surveys match their entered search query */
	private static final String NO_SEARCH_MATCHES_TEXT = "<span style='font-size: 12pt;'>"
			+ 	"No surveys matching the given search query were found."
			+ "</span>";

	private static CopySurveyItemDialogUiBinder uiBinder = GWT.create(CopySurveyItemDialogUiBinder.class);
	
	/**
	 * A callback used to handle whenever the user finishes selecting they survey items they want to copy
	 * 
	 * @author nroberts
	 */
	public static interface CopySurveyItemCallback{
		
		/**
		 * Handles whenever the user finishes selecting they survey items they want to copy
		 * 
		 * @param items the survey items to copy
		 */
		public void onItemsSelected(List<QuestionContainerWidget> items);
	}

	interface CopySurveyItemDialogUiBinder extends UiBinder<Widget, CopySurveyItemDialog> {
	}

	@UiField(provided = true)
	protected DataGrid<SurveyHeader> userSurveyTable = new DataGrid<SurveyHeader>();
	
	@UiField(provided = true)
	protected DataGrid<SurveyHeader> publicSurveyTable = new DataGrid<SurveyHeader>();
	
	/**
	 * The survey name column of the table
	 */
	private Column<SurveyHeader, String> nameColumn = new Column<SurveyHeader,String>(new TextCell()){

		@Override
		public String getValue(SurveyHeader object) {
			
			if(object == null){
				return "";
				
			} else {
				return object.getName();
			
			}
		}
		
	};
	
	   /** A column displaying the type of survey */
    private Column<SurveyHeader, SafeHtml> typeColumn = new Column<SurveyHeader, SafeHtml>(new SafeHtmlCell()){

        @Override
        public SafeHtml getValue(SurveyHeader object) {
            
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendHtmlConstant("<div style=''");

            if(object == null){
                builder.appendHtmlConstant(">");    // end the div starting element
                builder.appendEscaped("");          // show empty cell

            } else {
                
                Set<LearnerStateAttributeNameEnum> attrs = object.getLearnerStateAttributes();
                if(!attrs.isEmpty()) {
                    // build comma delimited listed of learner state attributes as the tooltip
                    
                    builder.appendHtmlConstant(" title='");
                    Iterator<LearnerStateAttributeNameEnum> itr = attrs.iterator();
                    while(itr.hasNext()) {
                        LearnerStateAttributeNameEnum name = itr.next();
                        builder.appendEscaped(name.getDisplayName());
                        
                        if(itr.hasNext()) {
                            builder.appendEscaped(", ");
                        }
                    }
                    builder.appendHtmlConstant("'");  // end the title attribute
                }
                builder.appendHtmlConstant(">");  // end the div starting element
                builder.appendEscaped(object.getSurveyType().getDisplayName()); // the value to put in the type column
            }
            
            builder.appendHtmlConstant("</div>");
            return builder.toSafeHtml();
        }
    };
	
	/**
	 * This is used as sort of a developer placeholder column now. The ID
	 * will not be shown to the user on release, so this column should be 
	 * replaced by a Survey type column, which is yet to be implemented. 
	 */
	private Column<SurveyHeader, String> idColumn = new Column<SurveyHeader,String>(new TextCell()){

		@Override
		public String getValue(SurveyHeader object) {
			
			if(object == null){
				return "";
				
			} else {
				return Integer.toString(object.getId());
			
			}
		}
		
	};
	
	/**
	 * the number of questions column of the table
	 */
	private Column<SurveyHeader, Number> numQuestionsColumn = new Column<SurveyHeader,Number>(new NumberCell()){

		@Override
		public Number getValue(SurveyHeader object) {
			return object.getQuestionCount();
		}

		
	};
	
	
	/**
	 * the number of pages column of the table
	 */
	private Column<SurveyHeader, Number> numPagesColumn = new Column<SurveyHeader,Number>(new NumberCell()){

		@Override
		public Number getValue(SurveyHeader object) {
			return object.getPageCount();
		}
		
	};
	
	/**
	 * RPC service that is used to retrieve the surveys from the database
	 */
	private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
	
	/**
	 * Used to select only 1 survey at a time from the list
	 */
	private SingleSelectionModel<SurveyHeader> tableSelectionModel = new SingleSelectionModel<SurveyHeader>();
	
	/**
	 * Provides the data to the survey list
	 */
	private ListDataProvider<SurveyHeader> userTableDataProvider = new ListDataProvider<SurveyHeader>();
	private ListDataProvider<SurveyHeader> publicTableDataProvider = new ListDataProvider<SurveyHeader>();
	
	/**
	 * The currently selected survey header, null if on first 
	 * widget of dialog or if none is selected
	 */
	private SurveyHeader selectedHeader = null;
	
	@UiField
	protected TabListItem userHeader;
	
	@UiField
	protected TabListItem publicHeader;
	
	@UiField
	protected TabPane userTabPane;
	
	@UiField
	protected TabPane publicTabPane;
	
	@UiField
	protected TextBox searchBox;
	
	@UiField
	protected Button searchButton;
	
	@UiField(provided=true)
	protected HelpLink helpLink;
	
	@UiField
	protected Button closeButton;
	
	@UiField
	protected Button enterButton;
	
	@UiField
	protected DeckPanel mainDeck;
	
	@UiField
	protected Widget selectSurveyPanel;
	
	@UiField
	protected Widget selectItemsPanel;
	
	@UiField
	protected Button cancelItemsButton;
	
	@UiField
	protected Button selectAllButton;
	
	@UiField
	protected Button selectNoneButton;
	
	@UiField
	protected Widget editorPanelContainer;
	
	@UiField(provided=true)
	protected SurveyEditorPanel editorPanel = new SurveyEditorPanel(){
		
		@Override
		void onSurveyChangeEditModeEvent(SurveyChangeEditMode event) {
			super.onSurveyChangeEditModeEvent(event);
			
			logger.info("onSurveyChangeEditModeEvent: " + event);
	        
	        if (event.getEditMode() == SurveyEditMode.ScoringMode) {
	        	editorPanelContainer.removeStyleName(STYLE_WRITING);
	        	editorPanelContainer.addStyleName(STYLE_SCORING);
	        } else {
	        	editorPanelContainer.removeStyleName(STYLE_SCORING);
	        	editorPanelContainer.addStyleName(STYLE_WRITING);
	        }
		}
		
		@Override
		void onSurveySelectQuestionEvent(SurveySelectQuestionEvent event) {
			
			//allow users to multi-select without holding the Ctrl key
			event.setKeepPreviousSelection(true);
			
	        super.onSurveySelectQuestionEvent(event);
	        
	        checkSelectedSurveyItems();
	    }
	};
	
	/**
	 * A callback used to handle the selected survey items
	 */
	private CopySurveyItemCallback copyCallback = null;
	
	/**
	 * The editor that this widget is copying survey items for
	 */
	private SurveyEditorPanel copyTarget = null;
	
	/** The unfiltered list of user surveys requested from the server */
	private List<SurveyHeader> userSurveys = new ArrayList<SurveyHeader>();
	
	/** The unfiltered list of public surveys requested from the server */
	private List<SurveyHeader> publicSurveys = new ArrayList<SurveyHeader>();
	
	/** The widget shown when the user survey table is empty */
	private HTML userEmptyTableWidget = new HTML(""
			+ NO_USER_SURVEYS_TEXT);
	
	/** The widget shown when the public survey table is empty */
	private HTML publicEmptyTableWidget = new HTML(""
			+ NO_PUBLIC_SURVEYS_TEXT);
	
	/**
	 * Creates a new dialog to copy survey items to the specified survey
	 * 
	 * @param survey the survey to copy items to
	 */
	public CopySurveyItemDialog() {
		
		super();
		
		final TopLevelModal helpModal = new TopLevelModal();		
		helpModal.setClosable(false);
	    helpModal.getElement().getStyle().setZIndex(1070);
	    
	    helpLink = new HelpLink(helpModal);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		//inherit bootstrap style for modal content
		addStyleName(Styles.MODAL_CONTENT);
		
		addStyleName("selectSurveyDialog");
		
		setGlassEnabled(true);
		setAnimationEnabled(true);
    	
    	addCloseHandler(new CloseHandler<PopupPanel>() {
			
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {				
				editorPanel.setSurveyEventHandlingEnabled(false);
			}
		});
    	
    	mainDeck.showWidget(mainDeck.getWidgetIndex(selectSurveyPanel));
		
		closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		userHeader.setDataTargetWidget(userTabPane);
		
		publicHeader.setDataTargetWidget(publicTabPane);
		
		userTableDataProvider.addDataDisplay(userSurveyTable);
		publicTableDataProvider.addDataDisplay(publicSurveyTable);
		
        SafeHtml nameColumnHeader = SafeHtmlUtils.fromTrustedString(
                "Survey Name"
                + "<i class='fa fa-sort' style='margin-left: 5px;'/>"
        );
        SafeHtml typeColumnHeader = SafeHtmlUtils.fromTrustedString("<p style=\"text-align:center;\">Type</p>");
        SafeHtml numQuestionsColumnHeader = SafeHtmlUtils.fromTrustedString("<p style=\"text-align:center;\">Question Count</p>");
        SafeHtml numPagesColumnHeader = SafeHtmlUtils.fromTrustedString("<p style=\"text-align:center;\">Page Count</p>");
        SafeHtml idColumnHeader = SafeHtmlUtils.fromTrustedString(
                "<p style=\"text-align:center;\">"
                +   "ID"
                +   "<i class='fa fa-sort' style='margin-left: 5px;'/>"
                + "</p>"
        );
        
        userSurveyTable.addColumn(nameColumn, nameColumnHeader);
        userSurveyTable.addColumn(typeColumn, typeColumnHeader);
        userSurveyTable.addColumn(numQuestionsColumn, numQuestionsColumnHeader);
        userSurveyTable.addColumn(numPagesColumn, numPagesColumnHeader);
        userSurveyTable.addColumn(idColumn, idColumnHeader); 
        
        typeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);  
        idColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);   
        numQuestionsColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        numPagesColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        userSurveyTable.setColumnWidth(nameColumn, "100%");
        userSurveyTable.setColumnWidth(typeColumn, "60px");     
        userSurveyTable.setColumnWidth(idColumn, "60px");       
        userSurveyTable.setColumnWidth(numQuestionsColumn, "60px");     
        userSurveyTable.setColumnWidth(numPagesColumn, "60px");

		userSurveyTable.setPageSize(Integer.MAX_VALUE);
		
		userSurveyTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		
		userSurveyTable.setSelectionModel(tableSelectionModel);
		userSurveyTable.setEmptyTableWidget(userEmptyTableWidget);
		
        publicSurveyTable.addColumn(nameColumn, nameColumnHeader);
        publicSurveyTable.addColumn(typeColumn, typeColumnHeader);
        publicSurveyTable.addColumn(numQuestionsColumn, numQuestionsColumnHeader);
        publicSurveyTable.addColumn(numPagesColumn, numPagesColumnHeader);
        publicSurveyTable.addColumn(idColumn, idColumnHeader);
          
        publicSurveyTable.setColumnWidth(nameColumn, "100%");
        publicSurveyTable.setColumnWidth(idColumn, "60px");
        publicSurveyTable.setColumnWidth(numQuestionsColumn, "60px");       
        publicSurveyTable.setColumnWidth(numPagesColumn, "60px");       
        publicSurveyTable.setColumnWidth(typeColumn, "60px");

		publicSurveyTable.setPageSize(Integer.MAX_VALUE);
		
		publicSurveyTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		
		publicSurveyTable.setSelectionModel(tableSelectionModel);
		publicSurveyTable.setEmptyTableWidget(publicEmptyTableWidget);
		
		//setup column sorting logic
		Comparator<SurveyHeader> nameComparator = new Comparator<SurveyHeader>() {
			
			@Override
			public int compare(SurveyHeader o1, SurveyHeader o2) {			
				
				if(o1 == null && o2 == null){
					return 0;
					
				} else if(o1 == null){
					return -1;
					
				} else if(o2 == null){
					return 1;
				}
				
				return o1.getName().compareTo(o2.getName());
			}
		};
		
		Comparator<SurveyHeader> idComparator = new Comparator<SurveyHeader>() {
			
			@Override
			public int compare(SurveyHeader o1, SurveyHeader o2) {			
				
				if(o1 == null && o2 == null){
					return 0;
					
				} else if(o1 == null){
					return -1;
					
				} else if(o2 == null){
					return 1;
				}
				
				return Integer.compare(o1.getId(), o2.getId());
			}
		};
		
		ListHandler<SurveyHeader> userSurveySortHandler = new ListHandler<>(userTableDataProvider.getList());
		userSurveySortHandler.setComparator(nameColumn, nameComparator);
		userSurveySortHandler.setComparator(idColumn, idComparator);
		
		userSurveyTable.addColumnSortHandler(userSurveySortHandler);
		
		ListHandler<SurveyHeader> publicSurveySortHandler = new ListHandler<>(publicTableDataProvider.getList());
		publicSurveySortHandler.setComparator(nameColumn, nameComparator);
		publicSurveySortHandler.setComparator(idColumn, idComparator);
		
		publicSurveyTable.addColumnSortHandler(publicSurveySortHandler);
		
		nameColumn.setSortable(true);
		idColumn.setSortable(true);
        
        tableSelectionModel.addSelectionChangeHandler(new Handler(){

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				
				selectedHeader = tableSelectionModel.getSelectedObject();
				
				if(selectedHeader != null){				

					mainDeck.showWidget(mainDeck.getWidgetIndex(selectItemsPanel));
					enterButton.setEnabled(true);
					cancelItemsButton.setVisible(true);
					selectAllButton.setVisible(true);
					selectNoneButton.setVisible(true);
					
					BsLoadingDialogBox.display("Getting Survey Items", "Please wait while we get this survey's items.");
					
					rpcService.getSurveyWithResources(GatClientUtility.getUserName(), selectedHeader.getId(), 
			                GatClientUtility.getBaseCourseFolderPath(), new AsyncCallback<SurveyReturnResult>() {
						
						@Override
						public void onSuccess(SurveyReturnResult result) {
							
							BsLoadingDialogBox.remove();
							
							if(result != null && result.getSurvey() != null){
								
								//reset the enter button
								enterButton.setText("Copy Selected Items");
					        	enterButton.setEnabled(false);
								
								//reset the scrolling on the editor panel
								editorPanelContainer.getElement().setScrollTop(0);
																
								//load the survey into the editor panel
								Survey survey = result.getSurvey();
								
								editorPanelContainer.removeStyleName(STYLE_SCORING);
					        	editorPanelContainer.addStyleName(STYLE_WRITING);
								
					        	if(copyTarget != null){
					        		editorPanel.initializePanelForCopy(survey, copyTarget.getSurveyType());
					        		
					        	} else {
					        		editorPanel.initializePanelForCopy(survey, null);
					        	}
							}
						}
						
						@Override
						public void onFailure(Throwable arg0) {
							
							enterButton.setText("Copy Selected Items");
				        	enterButton.setEnabled(false);
							
							BsLoadingDialogBox.remove();
							
							WarningDialog.error("Failed to load", "An unexpected error occurred while loading the selected survey");
						}
					});

				} else {
					
					mainDeck.showWidget(mainDeck.getWidgetIndex(selectSurveyPanel));
					enterButton.setText("Copy Selected Items");
		        	enterButton.setEnabled(false);
					cancelItemsButton.setVisible(false);
					selectAllButton.setVisible(false);
					selectNoneButton.setVisible(false);
				}
			}
        	
        });       
        
        /**
         * Using scheduled deferred commands for the show and hide handlers in order to
         * prevent a bug where the handlers would call each other endlessly and no action
         * would ever occur. 
         */
        userHeader.addShowHandler(new TabShowHandler(){

			@Override
			public void onShow(TabShowEvent showEvent) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand(){

					@Override
					public void execute() {
						
						userTableDataProvider.getList().clear();
						userTableDataProvider.getList().addAll(applySearchFilter(userSurveys));
						userTableDataProvider.refresh();
					}
					
				});
			}
		});
		
		publicHeader.addShowHandler(new TabShowHandler(){

			@Override
			public void onShow(TabShowEvent showEvent) {
				
				//Only query for public surveys the first time the panel is opened
				if(publicSurveyTable.getRowCount() == 0){
					
					refreshPublicSurveys();
					
				} else {
					
					publicTableDataProvider.getList().clear();
					publicTableDataProvider.getList().addAll(applySearchFilter(publicSurveys));
					publicTableDataProvider.refresh();
				}
			
			}

		});
		
		enterButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event){
				
				List<QuestionContainerWidget> selectedElements = editorPanel.getSelectedQuestionWidgets();
				
				if(selectedElements != null && !selectedElements.isEmpty()){
					
					if(copyCallback != null){
						copyCallback.onItemsSelected(selectedElements);
					}
					
				} else {
					WarningDialog.error("Missing selection", "Please select one or more survey items to copy.");
				}
			}
			
		});
		
		cancelItemsButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				tableSelectionModel.setSelected(null, true);
			}
		});
		
		selectAllButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				editorPanel.selectAllSurveyQuestions();
				
				checkSelectedSurveyItems();
			}
		});
		
		selectNoneButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				editorPanel.deselectAllSurveyQuestions();
				
				checkSelectedSurveyItems();
			}
		});
		
		searchBox.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
					
				userTableDataProvider.getList().clear();
				userTableDataProvider.getList().addAll(applySearchFilter(userSurveys));
				userTableDataProvider.refresh();
				
				publicTableDataProvider.getList().clear();
				publicTableDataProvider.getList().addAll(applySearchFilter(publicSurveys));
				publicTableDataProvider.refresh();
				
				if(searchBox.getText() == null || searchBox.getText().trim().isEmpty()){
					
					userEmptyTableWidget.setHTML(NO_USER_SURVEYS_TEXT);
					publicEmptyTableWidget.setHTML(NO_PUBLIC_SURVEYS_TEXT);
					
				} else {
					
					userEmptyTableWidget.setHTML(NO_SEARCH_MATCHES_TEXT);
					publicEmptyTableWidget.setHTML(NO_SEARCH_MATCHES_TEXT);
				}
			}
		});
		
		searchButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				userTableDataProvider.getList().clear();
				userTableDataProvider.getList().addAll(applySearchFilter(userSurveys));
				userTableDataProvider.refresh();
				
				publicTableDataProvider.getList().clear();
				publicTableDataProvider.getList().addAll(applySearchFilter(publicSurveys));
				publicTableDataProvider.refresh();
				
				if(searchBox.getText() == null || searchBox.getText().trim().isEmpty()){
					
					userEmptyTableWidget.setHTML(NO_USER_SURVEYS_TEXT);
					publicEmptyTableWidget.setHTML(NO_PUBLIC_SURVEYS_TEXT);
					
				} else {
					
					userEmptyTableWidget.setHTML(NO_SEARCH_MATCHES_TEXT);
					publicEmptyTableWidget.setHTML(NO_SEARCH_MATCHES_TEXT);
				}
			}
		});
		
		addAttachHandler(new AttachEvent.Handler() {
			
			@Override
			public void onAttachOrDetach(final AttachEvent event) {
				
				// Nick: There seems to be a bug with GWT's ResizeLayoutPanel that prevents it from resizing
				// the widgets inside of it if it is invisible when it is attached to the page. Since the public
				// surveys panel isn't initially visible whenever this dialog is opened, this ends up affecting 
				// the public surveys table, since the table relies on a ResizeLayoutPanel for its resizing logic.
				// To deal with this, I've added the following logic in order to briefly show the public survey
				// panel's ResizeLayoutPanel whenever this dialog is attached to the page (i.e. whenever it is
				// shown) so that the resizing logic can work properly.
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						
						if(event.isAttached()){
							
							userTabPane.setActive(true);
							publicTabPane.setActive(false);
							
						} else {
							
							userTabPane.setActive(true);
							publicTabPane.setActive(true);
						}
					}
				});
				
			}
		});
		
		 //make the preview dialog take up the full screen size
    	setPopupPosition(20, 20);
    	getElement().getStyle().setProperty("right", "20px");
    	getElement().getStyle().setProperty("bottom", "20px");
	}
	
	/**
	 * Checks the number of selected survey items to see if the "Copy Selected Items" button should be enabled or disabled
	 */
	private void checkSelectedSurveyItems() {
		
		List<QuestionContainerWidget> selectedWidgets = editorPanel.getSelectedQuestionWidgets();
        
        if(selectedWidgets != null && !selectedWidgets.isEmpty()){
        	
        	int size = selectedWidgets.size();
        	
        	if(size > 1){
        		enterButton.setText("Copy " + size + " Selected Items");
        		
        	} else {
        		enterButton.setText("Copy " + size + " Selected Item");
        	}
        	
        	enterButton.setEnabled(true);
        	
        } else {
        	
        	enterButton.setText("Copy Selected Items");
        	enterButton.setEnabled(false);
        }
	}

	/**
	 * Shows an interface allowing the user to select survey items to copy using the given callback
	 * 
	 * @param copyTarget the editor invoking the copy operation
	 * @param copyCallback the callback used to handle the selected items so they can be copied
	 */
	public void showCopy(SurveyEditorPanel copyTarget, CopySurveyItemCallback copyCallback) {
		
		this.copyCallback = copyCallback;
		this.copyTarget = copyTarget;

		ListQuery<SurveyQueryData> query = new ListQuery<SurveyQueryData>();

		SurveyQueryData queryData = new SurveyQueryData(new ArrayList<String>(), GatClientUtility.getUserName());

		query.setQueryData(queryData);

		rpcService.getSurveys(query, new AsyncCallback<ListQueryResponse<SurveyHeader>>() {

			@Override
			public void onFailure(Throwable caught) {
				BsLoadingDialogBox.remove();
				ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to retrieve the surveys from the database",
						caught.getMessage(), DetailedException.getFullStackTrace(caught));
				dialog.setTitle("Survey Database Error");
				dialog.center();
			}

			@Override
			public void onSuccess(ListQueryResponse<SurveyHeader> result) {
				logger.info("Survey retrieval successful. Found " + result.getTotalQueryRecordsCount() + " records");
				
				userSurveys.clear();
				userSurveys.addAll(result.getList());
				
				userTableDataProvider.getList().clear();
				userTableDataProvider.getList().addAll(applySearchFilter(userSurveys));
				userTableDataProvider.refresh();
				
				BsLoadingDialogBox.remove();

				show();
			}

		});

		tableSelectionModel.setSelected(tableSelectionModel.getSelectedObject(), false);

		/**
		 * Using scheduledDeferred here since selection change handler will
		 * reset enter button to enabled after returning to part One when
		 * deselecting its current selection
		 */
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				enterButton.setEnabled(false);
			}

		});

		BsLoadingDialogBox.display("Getting User Surveys", "Please wait while we get the latest list of your surveys.");
	}
	
	/**
	 * Refreshes the list of public surveys with the latest survey data from the server
	 */
	private void refreshPublicSurveys(){
		
		ListQuery<SurveyQueryData> query = new ListQuery<SurveyQueryData>();
		
		SurveyQueryData queryData = new SurveyQueryData(new ArrayList<String>(), "*");
		
		query.setQueryData(queryData);
		
		BsLoadingDialogBox.display("Getting Public Surveys", "Please wait while we get the latest list of public surveys.");
		
		rpcService.getSurveys(query, new AsyncCallback<ListQueryResponse<SurveyHeader>>(){

			@Override
			public void onFailure(Throwable caught) {
				BsLoadingDialogBox.remove();
				ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to retrieve the surveys from the database", caught.getMessage(), DetailedException.getFullStackTrace(caught));
				dialog.setTitle("Survey Database Error");
				dialog.center();
			}

			@Override
			public void onSuccess(ListQueryResponse<SurveyHeader> result) {
				
				logger.info("Survey retrieval successful. Found " + result.getTotalQueryRecordsCount() + " records");
				
				publicSurveys.clear();
				publicSurveys.addAll(result.getList());
				
				publicTableDataProvider.getList().clear();
				publicTableDataProvider.getList().addAll(applySearchFilter(publicSurveys));
				publicTableDataProvider.refresh();
				
				BsLoadingDialogBox.remove();
			}
			
		});
	}
	
	/**
	 * Applies the search filter currently entered by the user to the given list of surveys and returns a copy of the result
	 * 
	 * @param surveyList the list that should be filtered
	 * @return a copy of the given list with the appropriate survey filtering applied to it
	 */
	private List<SurveyHeader> applySearchFilter(List<SurveyHeader> surveyList){
		return filterSurveysByText(searchBox.getValue(), surveyList);
	}
	
	/**
	 * Filters the given list of surveys using the given filter expression.
	 * 
	 * @param filterExpression an expression containing search terms to be filtered
	 * @param toFilter the list of surveys to filter
	 * @return a copy of the given list that has been filtered using the given expression
	 */
	private List<SurveyHeader> filterSurveysByText(String filterExpression, List<SurveyHeader> toFilter){
		
		List<SurveyHeader> result = new ArrayList<SurveyHeader>();
		
		if(filterExpression != null && !filterExpression.trim().isEmpty()){
			
			// Nick: The filtering logic here uses the same syntax defined by the SAS in 
			// AbstractHibernateDatabaseManager.selectRowsByText(Class<?>, Session, String, String)
			// and behaves similarly. The only real difference here is the classes used, since we
			// need to use JavaScript's regular expression syntax on the client end rather
			// then using the pattern-matching approach used by the SAS's server end.
			
			List<SurveyHeader> toFilterCopy = new ArrayList<SurveyHeader>(toFilter);
			
			RegExp searchTermExp = RegExp.compile(binaryOperatorExpression + "|" + wordExpression, "gim");
			
			//parse the filter expression to get the list of search terms
			ArrayList<String> searchTerms = new ArrayList<String>();
			
		    for (MatchResult matcher = searchTermExp.exec(filterExpression); matcher != null; matcher = searchTermExp.exec(filterExpression)){
		    	searchTerms.add(matcher.getGroup(0));
		    }
			
			for(String currentTerm : searchTerms){
	    		
				if(currentTerm != null){
					
		    		//if a term starts with a '-', then all rows captured by searching for the remainder of the term will be
		    		//removed from the result
		    		if(currentTerm.startsWith("-")){
		    			   			
		    			//if there are already surveys in the result, search for the remainder of the search term and remove all
		    			//surveys found in the search
		    			if(!result.isEmpty()){    				
		    				result.removeAll(filterSurveysByText(currentTerm.substring(1), toFilterCopy));
		    			
		    			//otherwise, add all the surveys in the table to the result, search for the remainder of the search term, 
		    			//and remove all surveys found in the search
		    			}else{    				
		    				result.addAll(toFilterCopy);
		        	        result.removeAll(filterSurveysByText(currentTerm.substring(1), toFilterCopy));
		    			}    			
		    		
		    		//if a term matches the regular expression for a binary operator chain, perform the binary operations
		    		//specified and add the resulting rows to the result
		    		}else if(currentTerm.matches(binaryOperatorExpression)){
		    			
		    			//parse the binary operator chain for its operands
		    			List<String> operands = Arrays.asList(currentTerm.split("\\s+AND\\s+|\\s+OR\\s+")); 
		    			
		    			//parse the binary operator chain for its operators
		    			for(String operand: operands){
		    				currentTerm = currentTerm.replaceAll(operand, "");
		    			}
		    			
		    			currentTerm = currentTerm.trim();
		    			
		    			List<String> operators = Arrays.asList(currentTerm.split("\\s+"));
		    			
		    			//for each operand, perform the next binary operation specified using result of the previous 
		    			//binary operation and the operand itself
		    			List<SurveyHeader> binaryOpResult = new ArrayList<SurveyHeader>();    			
		    			for(String operand : operands){
		    				
		    				int j = operands.indexOf(operand);
		    						
		    				if(operands.indexOf(operand) == 0){
		    					binaryOpResult.addAll(filterSurveysByText(operand, toFilterCopy));   	
		    					
		    				}else{
		    					if(operators.get(j-1).matches("AND")){
		    						binaryOpResult.retainAll(filterSurveysByText(operand, toFilterCopy));
		    						
		    					}else if(operators.get(j-1).matches("OR")){
		    						binaryOpResult.addAll(filterSurveysByText(operand, toFilterCopy));
		    					}
		    				}
		    			}
		    			
		    			//add what surveys remain to the result
		    			result.addAll(binaryOpResult);
		    		
		    		//otherwise, treat the term as a single phrase
		    		}else{
		    			
		    			//if the term begins and ends with quotes, remove the quotes before evaluating the term
		    			if(currentTerm.startsWith("\"") && currentTerm.endsWith("\"")){
		        			currentTerm = currentTerm.substring(1, currentTerm.length()-1);
		        		}
		    			
		    			//find all the surveys containing the term's text (ignoring case)
		    	        for(SurveyHeader header : toFilterCopy){
		    	        	
		    	        	if(header.getName() != null && header.getName().toLowerCase().contains(currentTerm.toLowerCase())){
		    	        		result.add(header);
		    	        	}
		    	        }
		    			
		    		}   	
				}
	    	}   
			
		} else {
			result.addAll(toFilter);
		}
		
		return result;
	}

}
