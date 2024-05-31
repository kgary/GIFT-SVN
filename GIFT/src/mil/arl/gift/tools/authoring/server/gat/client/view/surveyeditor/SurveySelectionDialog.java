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
import org.gwtbootstrap3.client.shared.event.TabShownEvent;
import org.gwtbootstrap3.client.shared.event.TabShownHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.DataGrid;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.TopLevelModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQuery;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQueryResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyHeader;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyReturnResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurveyContextResult;

/**
 * Dialog created in order to select an existing survey out of the database. This is a 2 stage
 * dialog; the first stage lets the user choose between public surveys, or from survey they have created themselves.
 * The next stage will provide a paged list of the surveys based on their selection, displaying the type, number of questions,
 * and number of pages. 
 * 
 * @author wpearigen
 *
 */
public class SurveySelectionDialog extends ModalDialogBox {
	
	private static Logger logger = Logger.getLogger(SurveySelectionDialog.class.getName());

	private static SurveySelectionDialogUiBinder uiBinder = GWT
			.create(SurveySelectionDialogUiBinder.class);
	
	/** Tooltip text for the checkbox used to use the original version of an existing survey */
	private static final String USE_ORIGINAL_TOOLTIP = "Selecting this will use the original source of the survey. Changes to the survey will be reflected in your course.";
    
	/** Tooltip text for when the author can't use the original version of an existing survey*/
    private static final String CANT_USE_ORIGINAL_SOURCE_TOOLTIP = "Can not use original source of public surveys"; 
	
	interface SurveySelectionDialogUiBinder extends
	    UiBinder<Widget, SurveySelectionDialog> {
	}
	
	 /** A regular expression used to locate words and phrases in a search text expression */
	private static final String wordExpression = 
		"-?\"[^\"]*\"" +	//double quotes around phrases(s)
        "|-?[A-Za-z0-9']+"  //single word
	;
	
	/** A regular expression used to locate binary operators in a search text expression */
	private static final String binaryOperatorExpression = "(" + wordExpression + ")(\\s+(AND|OR)\\s+(" + wordExpression + "))+";
	
	/** The text shown to the user when no surveys are found in the database for surveys they own */
	private static final String NO_USER_SURVEYS_TEXT = "<span style='font-size: 12pt;'>"
			+ 	"You haven't authored any surveys yet.  Create a new survey course object to author a survey."
			+ "</span>";
	
	/** The text shown to the user when no surveys are found in the database for public surveys */
    private static final String NO_PUBLIC_SURVEYS_TEXT = "<span style='font-size: 12pt;'>"
            +   "There are no public surveys.  Not sure what happened here because GIFT normally contains several already authored surveys."
            + "</span>";
	
	/** The text shown to the user when no surveys match their entered search query */
	private static final String NO_SEARCH_MATCHES_TEXT = "<span style='font-size: 12pt;'>"
			+ 	"No surveys matching the given search query were found."
			+ "</span>";
	
	
	/** A panel used to show and hide advanced features*/
	private DisclosurePanel advancedPanel;
	
	/** A checkbox that allows the author to choose whether or not they want to select the original version of an existing survey */
	private CheckBox useOriginalCheckBox;
	
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
	
	@UiField(provided=true)
	protected HelpLink assessmentHelpLink;
	
	/** The table of user surveys */
	@UiField(provided = true)
	protected DataGrid<SurveyHeader> userSurveyTable = new DataGrid<SurveyHeader>();
	
	/** The table of public surveys*/
	@UiField(provided = true)
	protected DataGrid<SurveyHeader> publicSurveyTable = new DataGrid<SurveyHeader>();
	
	/** A column displaying the name of a survey */
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
	
	/** A column displaying the ID of a survey */
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
	
	/** A column displaying the number of questions in a survey */
	private Column<SurveyHeader, Number> numQuestionsColumn = new Column<SurveyHeader,Number>(new NumberCell()){

		@Override
		public Number getValue(SurveyHeader object) {
			return object.getQuestionCount();
		}
	};
	
	
	/** A column displaying the number of pages in a survey */
	private Column<SurveyHeader, Number> numPagesColumn = new Column<SurveyHeader,Number>(new NumberCell()){

		@Override
		public Number getValue(SurveyHeader object) {
			return object.getPageCount();
		}
	};
	
	/** RPC service that is used to retrieve the surveys from the database */
	private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
	
	/** Used to select only 1 survey at a time from the list */
	private SingleSelectionModel<SurveyHeader> tableSelectionModel = new SingleSelectionModel<SurveyHeader>();
	
	/** Provides the data to the list of user surveys */
	private ListDataProvider<SurveyHeader> userTableDataProvider = new ListDataProvider<SurveyHeader>();
	
	/** Provides the data to the list of public surveys */
	private ListDataProvider<SurveyHeader> publicTableDataProvider = new ListDataProvider<SurveyHeader>();
	
	/** The currently selected survey header, null if on first widget of dialog or if none is selected */
	private SurveyHeader selectedHeader = null;
	
	/**
	 * Button used to progress to the next part or confirm the survey choice.
	 * Will be disabled if a selection has not been made on both widgets
	 */
	private Button enterButton;
	
	/** A button used to delete surveys */
	private Button deleteSurveyButton;
	
	/** A callback that will be invoked when the selected survey changes*/
	private SurveySelectionCallback selectionCallback;
	
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

	/** The type of surveys that this dialog should allow users to pick from. If null, surveys of all types will be displayed. */
    private SurveyTypeEnum targetSurveyType;

    /** The survey resources of the base schema object (i.e. a course or scenario). Contains the survey context ID. */
    private AbstractSurveyResources surveyResources;
	
    /**
     * Creates a new survey selection dialog that interacts with the given survey resources
     * 
     * @param resources the survey resources containing the ID of the survey context that this dialog should modify
     */
	public SurveySelectionDialog(AbstractSurveyResources resources){
	    super(true);
	    
	    setSurveyResources(resources);
	    
	    TopLevelModal helpModal = new TopLevelModal();
	    
	    //bump up Z index of the help modal so that it appears above the selection dialog
	    helpModal.getElement().getStyle().setZIndex(1070);
	    
	    helpLink = new HelpLink(helpModal);
	    
	    TopLevelModal assessmentHelpModal = new TopLevelModal();
	    
	    //bump up Z index of the assessment help modal so that it appears above the selection dialog
        assessmentHelpModal.getElement().getStyle().setZIndex(1070);
	    
	    assessmentHelpLink = new HelpLink(assessmentHelpModal);
	    
	    setWidget(uiBinder.createAndBindUi(this));
		
		addStyleName("selectSurveyDialog");
		
		userTableDataProvider.addDataDisplay(userSurveyTable);
		publicTableDataProvider.addDataDisplay(publicSurveyTable);
		
		userHeader.setDataTargetWidget(userTabPane);
		
		publicHeader.setDataTargetWidget(publicTabPane);
		
		enterButton = new Button("Select");
		enterButton.setType(ButtonType.PRIMARY);
		enterButton.getElement().getStyle().setProperty("padding", "6px 20px");
		
		deleteSurveyButton = new Button("Delete");
		deleteSurveyButton.setType(ButtonType.DANGER);
		deleteSurveyButton.getElement().getStyle().setMarginRight(5, Unit.PX);
		deleteSurveyButton.getElement().getStyle().setProperty("padding", "6px 20px");
		deleteSurveyButton.setEnabled(false);
		
		advancedPanel = new DisclosurePanel("Advanced");
		useOriginalCheckBox = new CheckBox("Use Original");
		useOriginalCheckBox.setTitle(USE_ORIGINAL_TOOLTIP);
		advancedPanel.setContent(useOriginalCheckBox);
		advancedPanel.setAnimationEnabled(true);
		advancedPanel.addStyleName("surveyDisclosurePanel");
		
		setEnterButton(enterButton);
		setFooterWidget(enterButton);
		setFooterWidget(advancedPanel);
		setFooterWidget(deleteSurveyButton);
		
		setGlassEnabled(true);
		setText("Select Survey");
		setCloseable(true);
		setAnimationEnabled(true);
		
		getCloseButton().getElement().getStyle().setProperty("padding", "6px 20px");
		
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
				enterButton.setEnabled(true);
				
				if(selectedHeader != null){
				
					boolean hasEditPermission = GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck() 
						|| (selectedHeader.getEditableToUserNames() != null && selectedHeader.getEditableToUserNames().contains(GatClientUtility.getUserName()));
				
					if(hasEditPermission){
						deleteSurveyButton.setEnabled(true);
					
					} else {
						deleteSurveyButton.setEnabled(false);
					}
					
				} else {
					deleteSurveyButton.setEnabled(false);
				}
			}
        	
        });
        
        
        /*
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
						
						useOriginalCheckBox.setEnabled(true);
						useOriginalCheckBox.setTitle(USE_ORIGINAL_TOOLTIP);
						
						userTableDataProvider.getList().clear();
						userTableDataProvider.getList().addAll(applySearchFilter(userSurveys));
						userTableDataProvider.refresh();
					}
					
				});
			}
		});
		
		userHeader.addShownHandler(new TabShownHandler() {
			
			@Override
			public void onShown(TabShownEvent event) {
				useOriginalCheckBox.setValue(false);
				useOriginalCheckBox.setEnabled(false);
				useOriginalCheckBox.setTitle(CANT_USE_ORIGINAL_SOURCE_TOOLTIP);
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
				logger.info("Selected survey = " + selectedHeader.getName() + " with ID " + selectedHeader.getId());
				
				BsLoadingDialogBox.display("Loading Survey", "Please wait while the survey is retrieved.");
				SurveyEditorModal.getSurvey(selectedHeader.getId(), new AsyncCallback<SurveyReturnResult>() {

                    @Override
                    public void onFailure(Throwable t) {
                        
                        BsLoadingDialogBox.remove();
                        // Display a failure message dialog.
                        logger.severe("Throwable error encountered trying to get the survey result for survey id: " + selectedHeader.getId());
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to retrieve the surveys from the database", t.getMessage(), DetailedException.getFullStackTrace(t));
        				dialog.setTitle("Survey Database Error");
        				dialog.center();
                    }

                    @Override
                    public void onSuccess(SurveyReturnResult result) {
                    	
                        if (result != null && result.getSurvey() != null) {
                        	
                        	if(!useOriginalCheckBox.getValue()){
                        		
                        		try{
                        		
	                        		final Survey newSurvey = SurveyEditorModal.deepCopy(result.getSurvey(), true);
	                        		
	                        		logger.info("Created a new copy of survey: " + result.getSurvey().getName());
	                        		
	                        		SurveyEditorModal.saveSurveyAsync(
	                        		        newSurvey, 
	                        		        surveyResources != null ? surveyResources.getSurveyContextId() : null, 
	                        		        new AsyncCallback<Void>(){
	
	    								@Override
	    								public void onFailure(Throwable caught) {
	    								    
	    								    BsLoadingDialogBox.remove();
	    									logger.severe("Failed to insert " + newSurvey.getName() + " into the database");
	    									ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to retrieve the surveys from the database", caught.getMessage(), DetailedException.getFullStackTrace(caught));
	    									dialog.setTitle("Survey Database Error");
	    									dialog.center();
	    									
	    									// Close the dialog.
	    	                                hide();
	    								}
	
	    								@Override
	    								public void onSuccess(Void result) {
	    									
	    									pollForSaveProgress();   
	    								}
	                            		
	                            	});
                        		
                        		} catch (Exception e){
                        			
                        			BsLoadingDialogBox.remove();
                        			
                        			logger.severe("Throwable error encountered trying to copy a survey:\n" + e.toString());
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                    		"Failed to copy the selected survey due to an unexpected eror.", 
                                    		e.toString(), 
                                    		DetailedException.getFullStackTrace(e)
                                    );
                                    
                     				dialog.setTitle("Survey Copy Error");
                     				dialog.center();
                        		}
                        		
                        	} else {
                        	    
                        	    BsLoadingDialogBox.remove();
                        		logger.info("Using original source survey with ID " + result.getSurvey().getId());
                        		if (selectionCallback != null) {
	                                selectionCallback.onSelection(result.getSurvey(), true);
	                            }
                        		
                        		// Close the dialog.
                                hide();
                        	}
                            
                            
                        } else {
                            
                        	BsLoadingDialogBox.remove();
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                            		"Failed to retrieve the specified survey from the database. The most likely cause is that some of "
                            		+ "the survey's resources may have been deleted, making the survey invalid.", 
                            		new NullPointerException("The survey to select cannot be null.").toString(), 
                            		null
                            );
                            
             				dialog.setTitle("Select Survey Failed");
             				dialog.center();
                        }
                        
                    }
				    
				});
			}
			
		});
        
		
		//Not going to worry about error dialogs or detailed logs 
		//for this button since it will be removed soon
		deleteSurveyButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				
				if(selectedHeader != null){
					
					boolean hasEditPermission = GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck() 
						|| (selectedHeader.getEditableToUserNames() != null && selectedHeader.getEditableToUserNames().contains(GatClientUtility.getUserName()));
				
					if(hasEditPermission){
			    
					    String warningMessage = "Are you sure you want to delete the survey named '"+selectedHeader.getName()+"'?<br/>"+
					            "<b><i>NOTE:  This will PERMANENTLY remove the survey and any responses from the database.  In addition this survey will not be available in ANY other course(s) that have a shared reference to this survey.</b></i></br></br>"+
					            "<b><font color='red'>This action cannot be undone.</font></b>";
					    OkayCancelDialog.show("Are you sure? ", warningMessage, "Delete", new OkayCancelCallback() {
		
		                    @Override
		                    public void okay() {
		                        deleteSelectedSurvey(true);		                        
		                    }
		
		                    @Override
		                    public void cancel() {
		                        // nothing to do here.
		                        
		                    }
					        
					    });
					}
			    
				} else {
					WarningDialog.alert("Survey Deletion Failed", "The selected survey could not be deleted because you do "
							+ "not have permission to delete this survey.");
				}
			    
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
    	setPopupPosition(0, 20);
    	getElement().getStyle().setProperty("right", "20px");
    	getElement().getStyle().setProperty("bottom", "20px");
    	getElement().getStyle().setProperty("margin", "0px auto");
    	getElement().getStyle().setProperty("width", "100%");
    	getElement().getStyle().setProperty("maxWidth", "900px");   	
	}
	
	/**
	 * Polls the server for the status of the current save operation until that operation has completed, either successfully or
	 * unsuccessfully.
	 */
	private void pollForSaveProgress() {

		SurveyEditorModal.getSaveSurveyStatus(new AsyncCallback<LoadedProgressIndicator<Survey>>() {

					@Override
					public void onSuccess(LoadedProgressIndicator<Survey> result) {

						if (result.isComplete()) {
							
							BsLoadingDialogBox.remove();
							logger.info(
									"successfully inserted " + result.getPayload().getName() + " into the database");
							if (selectionCallback != null) {
								selectionCallback.onSelection(result.getPayload(), false);
							}

							// Close the dialog.
							hide();
							
						} else if (result.getException() != null) {
							
							BsLoadingDialogBox.remove();
							logger.severe("Failed to insert the survey into the database");
							ErrorDetailsDialog dialog = new ErrorDetailsDialog(
									"Failed to retrieve the surveys from the database", 
									result.getException().getDetails(),
									result.getException().getErrorStackTrace()
							);
							dialog.setTitle("Survey Database Error");
							dialog.center();

							// Close the dialog.
							hide();
							
						} else {
							
							//schedule another poll for progress 1 second from now
							Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
								
								@Override
								public boolean execute() {
									
									pollForSaveProgress();
									
									return false;
								}
								
							}, 1000);
						}
					}

					@Override
					public void onFailure(Throwable caught) {

						BsLoadingDialogBox.remove();
						logger.severe("Failed to insert the survey into the database");
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(
								"Failed to retrieve the surveys from the database", 
								caught.toString(),
								DetailedException.getFullStackTrace(caught)
						);
						dialog.setTitle("Survey Database Error");
						dialog.center();

						// Close the dialog.
						hide();
					}
				});
	}
	
	/**
	 * Calls the asynchronous method to delete the selected survey from the database.
	 * 
	 * @param deleteResponses whether to attempt to delete any survey responses for the survey being deleted.
	 */
	private void deleteSelectedSurvey(boolean deleteResponses) {
		
		if(selectedHeader != null){
			
			boolean hasEditPermission = GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck() 
				|| (selectedHeader.getEditableToUserNames() != null && selectedHeader.getEditableToUserNames().contains(GatClientUtility.getUserName()));
		
			if(hasEditPermission){
		
			    DeleteSurvey action = new DeleteSurvey(
                        GatClientUtility.getUserName(),
                        selectedHeader.getId()              
                );
			    action.setBypassPermissionCheck(GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck());
                action.setDeleteResponses(deleteResponses);                
                
                BsLoadingDialogBox.display("Deleting Survey", "Please wait while GIFT deletes this survey.");
                
                SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<DeleteSurveyContextResult>() {
    
                    @Override
                    public void onFailure(Throwable caught) {
                        
                        BsLoadingDialogBox.remove();
                        
                        ErrorDetailsDialog dialog = 
                                new ErrorDetailsDialog("Failed to delete the survey", 
                                        "The server threw an error while trying to delete that reads "+caught.getMessage(), 
                                        null);
                        dialog.setDialogTitle("Survey Delete Failed");
                        dialog.center();
                    }
    
                    @Override
                    public void onSuccess(DeleteSurveyContextResult result) {
                        
                        BsLoadingDialogBox.remove();
                        
                        if(result.isSuccess()){  
                            //the survey was deleted
                            
                            logger.info("Successfully deleted survey = " + result);                             
                            
                            if(userTabPane.isActive()){
                                
                                showPartOne(selectionCallback);
                                
                            } else if(publicTabPane.isActive()){
                                
                                refreshPublicSurveys();
                            }
                            
                        } else {
                            //the survey was not deleted
                            
                            String reason = result.getErrorMsg();
                            if(reason == null){
                                if(result.hadSurveyResponses()){
                                    reason = "There are course's that reference this survey for which you don't have permissions to remove the survey responses for.";
                                }else{
                                    reason = "An unknown problem prevented the deletion.";
                                }
                            }
                            
                            ErrorDetailsDialog dialog = 
                                    new ErrorDetailsDialog("Failed to delete the survey", 
                                            "The server was unable to delete the survey because "+reason, 
                                            null);
                            dialog.setDialogTitle("Survey Delete Failed");
                            dialog.center();
                        }       
                    }
                });

			}
	        
		} else {
			WarningDialog.alert("Survey Deletion Failed", "The selected survey could not be deleted because you do"
					+ "not have permission to delete this survey.");
		}
    }
	
	/**
	 * Refreshes the list of public surveys with the latest survey data from the server
	 */
	private void refreshPublicSurveys(){
		
		ListQuery<SurveyQueryData> query = new ListQuery<SurveyQueryData>();
		
		SurveyQueryData queryData = new SurveyQueryData(new ArrayList<String>(), "*", targetSurveyType);
		
		query.setQueryData(queryData);
		
		BsLoadingDialogBox.display("Gathering surveys", "Please wait while we get the latest list of public surveys.");
		
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
	 * Shows the first part of the selection dialog, allowing the user to choose
	 * between Their surveys or the Public surveys in the database. This method will
	 * also clear all the selections as it can be returned to from the next part.
	 */
	public void showPartOne(SurveySelectionCallback callback) {
		
		ListQuery<SurveyQueryData> query = new ListQuery<SurveyQueryData>();

        String username = GatClientUtility.getUserName();

        /* if GIFT Wrap desktop user, use wildcard as the user */
        if (StringUtils.equals(username, GatClientUtility.GIFT_WRAP_DESKTOP_USER)) {
            username = Constants.VISIBILITY_WILDCARD;
        }
		SurveyQueryData queryData = new SurveyQueryData(new ArrayList<String>(), username, targetSurveyType);
		
		query.setQueryData(queryData);
		
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
				
				userSurveys.clear();
				userSurveys.addAll(result.getList());
				
				userTableDataProvider.getList().clear();
				userTableDataProvider.getList().addAll(applySearchFilter(userSurveys));
				userTableDataProvider.refresh();
				
				BsLoadingDialogBox.remove();
				
				show();
			}
			
		});
		
		
	    selectionCallback = callback;
		tableSelectionModel.setSelected(tableSelectionModel.getSelectedObject(), false);
		advancedPanel.setOpen(false);
		useOriginalCheckBox.setValue(false);
		useOriginalCheckBox.setEnabled(true);
		deleteSurveyButton.setEnabled(true);
		
		/*
		 * Using scheduledDeferred here since selection change handler will
		 * reset enter button to enabled after returning to part One when
		 * deselecting its current selection
		 */
		Scheduler.get().scheduleDeferred(new ScheduledCommand(){

			@Override
			public void execute() {
				enterButton.setEnabled(false);
				userHeader.showTab();
			}
			
		});
		
		BsLoadingDialogBox.display("Gathering surveys", "Please wait while we get the latest list of your surveys.");
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
	 * Clear the search box text in order to reset the search filter.  
	 * This should normally only be done when the dialog is not visible in order to not
	 * change the search results from what the author has manually entered as a search filter.
	 */
	public void clearSearchFilter(){
	    searchBox.clear();
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
	
	/**
	 * Sets the type of surveys that this dialog should allow users to pick from. If set to null, this widget
	 * will retrieve and display surveys of any survey type.
	 * 
	 * @param type the type of survey to let authors pick from
	 */
	public void setTargetSurveyType(SurveyTypeEnum type) {
	    this.targetSurveyType = type;
	    
	    //show help text when limiting users to "Assess Learner Knowledge" surveys
	    assessmentHelpLink.setVisible(SurveyTypeEnum.ASSESSLEARNER_STATIC.equals(this.targetSurveyType));
	}
	
	/**
     * Sets the global survey resources (i.e. survey context, concepts, etc.) that should be used by this widget for authoring surveys
     * 
     * @param resources the survey resources
     */
    public void setSurveyResources(AbstractSurveyResources resources) {
        this.surveyResources = resources;
    }
}
