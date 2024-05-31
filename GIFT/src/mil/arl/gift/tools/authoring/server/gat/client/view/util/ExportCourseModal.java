/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.util.ArrayList;
import java.util.Collections;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.SelectableCourseWidget;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchAllDomainOptions;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchAllDomainOptionsResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CancelExport;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteExportFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ExportCourses;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ExportCoursesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportSize;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetExportSizeResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;

/**
 * A modal dialog used to select course files to export
 * 
 * @author bzahid
 */
public class ExportCourseModal extends ModalDialogBox {
		
	/** the list containing the selected course files */
	private ArrayList<DomainOption> selectedFilesList;
	
	private VerticalPanel fileListPanel;
		
	private Button beginExportButton;
	
	private Button startExportButton;
	
	private ProgressBarListEntry exportSubtaskProgress;
	
	private ProgressBarListEntry exportOverallProgress;
	
	private BsLoadingIcon loadingIcon = new BsLoadingIcon();
	
	private HTML courseList;
		
	private HTML exportSummarySizeLabel;
	
	private boolean exportCancelled = false;
	
	private boolean pollForProgress = false;
	
	private final ModalDialogBox progressDialog = new ModalDialogBox();
	
	private final DeckPanel bodyDeckPanel = new DeckPanel();
	
	private final DeckPanel footerDeckPanel = new DeckPanel();
	
	private boolean cancelClicked;
	
	private static final int LOADING = 0;
	private static final int COURSES = 1;
	private static final int SUMMARY = 2;
	
	/** Used to make sure the next button is not enabled while courses are validating. */
	private int coursesPending = 0;
	private int coursesReturned = 0;
	
	/**
	 * Creates a file selection dialog that displays course files to select for exporting
	 */
	public ExportCourseModal(){
		super();		
		init();
		
		setGlassEnabled(true);
	}
	
	/** 
	 * Initializes UI elements
	 */
	private void init() {
				
		// create components
		fileListPanel = new VerticalPanel();
		beginExportButton = new Button("Next");
		selectedFilesList = new ArrayList<DomainOption>();
		Button closeButton = new Button("Cancel");
		HorizontalPanel buttonPanel = new HorizontalPanel();
		FlowPanel loadingPanel = new FlowPanel();
		ScrollPanel fileScrollPanel = new ScrollPanel(fileListPanel);
		Label loadingLabel = new Label("Retrieving the list of courses, please wait...");
		
		beginExportButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				showWidget(SUMMARY);
			}
			
		});
				
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				cancelClicked = true;
				hide();
			}
			
		});
		
		// style the scroll panel
		fileScrollPanel.setHeight("200px");
		fileScrollPanel.getElement().getStyle().setBackgroundColor("aliceBlue");
		fileScrollPanel.getElement().getStyle().setProperty("border", "solid 1px lightGray");
		fileScrollPanel.setWidth("600px");
		fileListPanel.setWidth("100%");
		
		// style the buttons
		beginExportButton.setEnabled(false);
		beginExportButton.setType(ButtonType.PRIMARY);
		closeButton.setType(ButtonType.DANGER);
		
		buttonPanel.add(beginExportButton);
		buttonPanel.add(closeButton);
		beginExportButton.setMarginRight(10);
		
		// style the loading indicator
		loadingPanel.setWidth("600px");
		loadingPanel.setHeight("200px");
		loadingPanel.getElement().getStyle().setBackgroundColor("aliceBlue");
		loadingLabel.getElement().getStyle().setProperty("fontSize", "14px");
		loadingLabel.getElement().getStyle().setProperty("fontFamily", "Arial");
		loadingLabel.getElement().getStyle().setProperty("padding", "7% 0 20px");
		loadingPanel.getElement().getStyle().setProperty("textAlign", "center");
		loadingPanel.getElement().getStyle().setProperty("border", "solid 1px lightGray");
		loadingPanel.add(loadingLabel);
		loadingPanel.add(loadingIcon);	
		loadingIcon.setSize(IconSize.TIMES5);
		loadingIcon.setType(IconType.SPINNER);
		loadingIcon.startLoading();
		
		bodyDeckPanel.add(loadingPanel);
		bodyDeckPanel.add(fileScrollPanel);
		footerDeckPanel.add(buttonPanel);
		
		// create the export summary panel
		initSummaryPage();
		
		// create the progress dialog
		initProgressDialog();
		
		setWidget(bodyDeckPanel);
		setFooterWidget(footerDeckPanel);
		footerDeckPanel.getElement().getStyle().setProperty("float", "right");
	}
	
	/**
	 * Centers and displays the dialog while refreshing the course list
	 */
	@Override
	public void center() {
		super.center();
		refresh();
	}
	
	private void showWidget(int index){
				
		bodyDeckPanel.showWidget(index);
		
		if(index < 2) {
			setHtml("Select Course(s) to Export");
			setEnterButton(beginExportButton);
			footerDeckPanel.showWidget(0);
		} else {
			// show the export summary page
			showExportSummary();
			footerDeckPanel.showWidget(1);
		}
		
		super.center();
	}
	
	/**
	 * Reloads the list of course files available for selection and resets the UI.
	 */
	public void refresh() {
		
		// reset components
		cancelClicked = false;
		selectedFilesList.clear();
		beginExportButton.setEnabled(false);
		setHtml("Select Course(s) to Export");
				
		// show a loading icon while the course files list loads
		showWidget(LOADING);
		fileListPanel.clear();		
		
		// set up action to retrieve courses list
		FetchAllDomainOptions action = new FetchAllDomainOptions();
		action.setUserName(GatClientUtility.getUserName());

		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<FetchAllDomainOptionsResult>() {

			@Override
			public void onFailure(Throwable throwable) {
				showWidget(COURSES);
				WarningDialog.error("Failed to retrieve course files", "The server threw an exception while retrieving course files.\n" + throwable);
			}

			@Override
			public void onSuccess(FetchAllDomainOptionsResult result) {
				if (!cancelClicked){
					showWidget(COURSES);
				}
								
				if(result.isSuccess()) {
					
					Collections.sort(result.getDomainOptionsList());					
					for(DomainOption option : result.getDomainOptionsList()) {
						// add each domain option to the scroll panel as a course
						addCourseFile(option);
					}
				} else {
					WarningDialog.error("Failed to retrieve course files", "Unable to retrieve course files: " + result.getErrorMsg());
				}
			}

		});
	}
	
	
	/**
	 * Adds a domain option to the list of available courses for selection
	 * 
	 * @param option the domain option to add
	 */
	private void addCourseFile(final DomainOption option) {
		
		final SelectableCourseWidget courseWidget = new SelectableCourseWidget(option);
		
		courseWidget.setValidationCallback(new AsyncCallback<ValidateFileResult>() {
			@Override
			public void onFailure(Throwable t) {
				// re-enable the next button
				coursesReturned += 1;
				beginExportButton.setEnabled(!selectedFilesList.isEmpty() && coursesPending == coursesReturned);
				
				ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						"The course \"" + option.getDomainName() + "\" cannot be exported because it is invalid. "
								+ "Only valid courses may be exported in order to prevent problems sharing your "
								+ "course with other users.", 
						t.toString(), 
						DetailedException.getFullStackTrace(t));
				dialog.setText("Cannot Export Course");
				dialog.center();				
			}
			@Override
			public void onSuccess(ValidateFileResult result) {
				coursesReturned += 1;
				
				if(result.isSuccess()) {
					selectedFilesList.add(option);
				} else {
					FileValidationDialog dialog = new FileValidationDialog(
							result.getFileName(), 
							"The course \"" + option.getDomainName() + "\" cannot be exported because it is invalid. "
									+ "Only valid courses may be exported in order to prevent problems sharing your "
									+ "course with other users.<br/><br>Reason: " + result.getReason(),
							result.getDetails(),
							result.getStackTrace());
					dialog.setText("Cannot Export Course");
					dialog.center();
				}
				
				beginExportButton.setEnabled(!selectedFilesList.isEmpty() && coursesPending == coursesReturned);
			}
		});
		
		courseWidget.setCourseSelectionHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(selectedFilesList.contains(option)) {
					selectedFilesList.remove(option);
				} else {
					
					if(courseWidget.courseIsValid()) {
						selectedFilesList.add(option);
						beginExportButton.setEnabled(!selectedFilesList.isEmpty() && coursesPending == coursesReturned);
					} else {
						// disable the next button while a course is validating
						coursesPending += 1;
						beginExportButton.setEnabled(false);
					}
				}
				
				if(selectedFilesList.isEmpty()) {
					beginExportButton.setEnabled(false);
				}
			}
			
		});					
		
		fileListPanel.add(courseWidget);
	}
	
	/**
	 * Creates the export summary dialog UI elements
	 */
	private void initSummaryPage() {
				
		Button backButton = new Button("Back");
		HorizontalPanel buttonPanel = new HorizontalPanel();
		
		HTML exportSummaryText = new HTML("Courses intended for export should use locally referenced"
				+ " files that are in the<br/>course folder of the particular course, otherwise"
				+ " the file(s) will not be copied.<br/><br/>The export process will gather Survey"
				+ " Contexts needed by the courses being<br/>exported as well as any other "
				+ "resources referenced by those surveys<br/>(e.g. survey question images).<br/>");
		
		HTML exportSummaryLabel = new HTML("The following courses will be included in this export:");
		
		exportSummarySizeLabel = new HTML("<br/>Estimated Download Size: ");		
		
		courseList = new HTML();
		
		HTMLPanel panel = new HTMLPanel("");
		
		startExportButton = new Button("Start Export");	
		
		
		exportSummarySizeLabel.getElement().getStyle().setProperty("paddingLeft", "40px");
		startExportButton.setType(ButtonType.PRIMARY);
		backButton.setType(ButtonType.DANGER);
		
		startExportButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				startExport();
			}			
		});
		
		backButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				showWidget(COURSES);
			}			
		});
		
		courseList.addStyleName("exportCoursesDialogCourseList");
		
		panel.add(exportSummaryLabel);
		panel.add(courseList);
		panel.add(exportSummaryText);
		panel.add(exportSummarySizeLabel);
		panel.getElement().getStyle().setColor("#666666");
		
		buttonPanel.add(startExportButton);
		buttonPanel.add(backButton);
		startExportButton.setMarginRight(10);
		
		bodyDeckPanel.add(panel);
		footerDeckPanel.add(buttonPanel);
	}
	
	/**
	 * Displays the export summary page in the dialog
	 */
	private void showExportSummary() {
		
		setHtml("Export Summary");
		
		SafeHtmlBuilder cb = new SafeHtmlBuilder();
				
		// create exported courses list
		cb.appendHtmlConstant("<ul>");
		
		for(DomainOption option : selectedFilesList) {
			cb.appendHtmlConstant("<li>");
			cb.appendEscaped(option.getDomainName());
			cb.appendHtmlConstant("</li>");
		}
		
		cb.appendHtmlConstant("</ul>");
		
		courseList.setHTML(cb.toSafeHtml());
		
		// create summary text
		exportSummarySizeLabel.setHTML("<br/>Estimated Download Size:  Calculating...");
				
		// get the export size
		GetExportSize action = new GetExportSize();
		action.setUserName(GatClientUtility.getUserName());
		action.setSelectedDomainOptions(selectedFilesList);
		
		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetExportSizeResult>() {

			@Override
			public void onFailure(Throwable throwable) {
				exportSummarySizeLabel.setHTML("<br><i>An error occurred while calculating the download size.<i>");
			}

			@Override
			public void onSuccess(GetExportSizeResult result) {
				
				String value = "" + result.getExportSize();
				
				if(result.getExportSize() > 0.9) {
					value = value.substring(0, value.indexOf(".") + 2) + " MB";
				} else {
					value = "< 1 MB";
				}
				
				exportSummarySizeLabel.setHTML("<br/>Estimated Download Size:  " + value);
			}
			
		});
		
		setEnterButton(startExportButton);
	}
		
	/**
	 * Executes the action to export the selected courses
	 */
	private void startExport() {
	
		hide();
		
		// reset progress dialog
		pollForProgress = true;
		exportOverallProgress.updateProgress(new ProgressIndicator("loading"));
		exportSubtaskProgress.updateProgress(new ProgressIndicator("loading"));
		progressDialog.setHtml("Creating Export...");
		progressDialog.center();
		
		ExportCourses action = new ExportCourses();
		action.setUserName(GatClientUtility.getUserName());
		action.setSelectedDomainOptions(selectedFilesList);
		
		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<ExportCoursesResult>(){

			@Override
			public void onFailure(Throwable thrown) {
				
				pollForProgress = false;
				progressDialog.hide();
				
				if(!exportCancelled) {
					
					ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to Export the File.", 
							thrown.toString(), DetailedException.getFullStackTrace(thrown));
					dialog.setDialogTitle("Export Failed");
					dialog.center();
				} else {
					exportCancelled = true;
				}
			}

			@Override
			public void onSuccess(ExportCoursesResult result) {
				
				progressDialog.hide();
				pollForProgress = false;
				
				if(result.isSuccess() && !exportCancelled) {
					
					createSuccessDialog(result.getDownloadUrl(), result.getLocationOnServer());
				
				} else if(!exportCancelled) {
					ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), 
							result.getErrorDetails(), result.getErrorStackTrace());
					dialog.setDialogTitle("Export Failed");
					dialog.center();
				} else {
					exportCancelled = false;
				}
			}
		});
		
		pollForProgress();
		
	}
		
	/**
	 * Creates the progress dialog UI elements
	 */
	private void initProgressDialog() {
		
		final Button cancelExportButton = new Button("Cancel");
		FlowPanel wrapper = new FlowPanel();
				
		exportOverallProgress = new ProgressBarListEntry();
		exportSubtaskProgress = new ProgressBarListEntry();
		
		wrapper.add(exportSubtaskProgress);
		wrapper.add(exportOverallProgress);
				
		wrapper.getElement().getStyle().setProperty("minWidth", "400px");
		
		cancelExportButton.setType(ButtonType.DANGER);
		cancelExportButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				exportCancelled = true;
				pollForProgress = false;
				cancelExportButton.setEnabled(false);
				progressDialog.setHtml("Cancelling Export...");
				
				CancelExport action = new CancelExport();
				action.setUserName(GatClientUtility.getUserName());
				
				SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

					@Override
					public void onFailure(Throwable thrown) {
						
						WarningDialog.error("Failed to cancel", "An error occurred while cancelling the import: " + thrown.toString() +".");
					}

					@Override
					public void onSuccess(GatServiceResult result) {
						
						if(result.isSuccess()) {		
							// successfully cancelled. Hide the progress dialog
							
							cancelExportButton.setEnabled(true);
							pollForProgress = false;
							progressDialog.hide();
							
							// display the export selection dialog again
							showWidget(COURSES);
							
						} else {
							WarningDialog.error("Failed to cancel", "An error occurred while canceling the import: " + result.getErrorMsg());
							cancelExportButton.setEnabled(true);
							
						}
					}	
				});				
			}			
		});
		
		progressDialog.setWidget(wrapper);
		progressDialog.setHtml("Creating Export...");
		progressDialog.setFooterWidget(cancelExportButton);
		progressDialog.removeEnterButton();
		progressDialog.setGlassEnabled(true);
	}
	
	/**
	 * Updates the progress indicators if necessary
	 */
	private void pollForProgress() {
		if(pollForProgress) {
			
			GetExportProgress action = new GetExportProgress();
			action.setUserName(GatClientUtility.getUserName());
			
			SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetProgressResult>(){

				@Override
				public void onFailure(Throwable thrown) {
					pollForProgress();
				}

				@Override
				public void onSuccess(GetProgressResult result) {
					if(result.isSuccess()) {
						ProgressIndicator progress = result.getProgress();
						exportOverallProgress.updateProgress(progress);
						
						if(progress.getSubtaskProcessIndicator() != null) {
							exportSubtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());
						}
					}
					
					pollForProgress();
				}
			});
		}
	}
	
	/**
	 * Creates a dialog that indicates the export was successful and displays
	 * the download url. Attempts to automatically download the file.
	 * 
	 * @param downloadUrl
	 */
	private void createSuccessDialog(final String downloadUrl, final String locationOnServer) {
		
		final ModalDialogBox successDialog = new ModalDialogBox();
		Button closeButton = new Button("Close");
		HorizontalPanel panel = new HorizontalPanel();
		Icon infoIcon = new Icon(IconType.INFO_CIRCLE);
		HTML text = new HTML("Your export has been successfully created. <br/><br/>"
				+ "If your download doesn't start in a few seconds, please <a href="
				+ "'" + downloadUrl + "' target='_self'>click here</a> to start the download.");
		HTMLPanel textPanel = new HTMLPanel(text.getHTML());
		
		infoIcon.setSize(IconSize.TIMES3);
		closeButton.setType(ButtonType.PRIMARY);
		panel.getElement().getStyle().setColor("#666666");
		textPanel.getElement().getStyle().setProperty("fontSize", "14px");
		textPanel.getElement().getStyle().setProperty("paddingLeft", "15px");
		textPanel.getElement().getStyle().setProperty("fontFamily", "Arial");
		
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				DeleteExportFile action = new DeleteExportFile();
				
				action.setUserName(GatClientUtility.getUserName());
				action.setDownloadUrl(downloadUrl);
				action.setLocationOnServer(locationOnServer);
				
				SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						/* 
						 * Close the dialog. File deletion is handled silently, therefore errors 
						 * should be handled on the server.
						 */
						successDialog.hide();
					}

					@Override
					public void onSuccess(GatServiceResult arg0) {
						// Do Nothing. File deletion is handled silently, just close the dialog.
						successDialog.hide();
					}					
				});				
			}
			
		});
		
		panel.add(infoIcon);
		panel.add(textPanel);
		
		successDialog.setHtml("Export Successful");
		successDialog.setWidget(panel);
		successDialog.setFooterWidget(closeButton);
		successDialog.setEnterButton(closeButton);
		successDialog.setGlassEnabled(true);
		
		successDialog.center();
		
		Window.open(downloadUrl, "_self", "");
	}

}