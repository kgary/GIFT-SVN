/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import com.google.gwt.core.client.GWT; 
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.ert.ColumnProperties;
import mil.arl.gift.common.ert.EventColumnDisplay;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.TimeProperties;
import mil.arl.gift.common.gwt.client.widgets.SimpleProgressBar;
import mil.arl.gift.tools.ert.shared.EventSourceTreeNode;
import mil.arl.gift.tools.ert.shared.EventSourcesTreeModel;

/**
 * A widget for creating an event report
 *
 * @author jleonard
 */
public class EventReportCreatorWidget extends Composite {

    private final ErtRpcServiceAsync rpcService = GWT.create(ErtRpcService.class);

    private static EventReportCreatorWidgetUiBinder uiBinder = GWT.create(EventReportCreatorWidgetUiBinder.class);
    
    /** how often the check for report generation progress updates from the server */
    private static final int PROGRESS_CHECK_INTERVAL_MS = 100;

    @UiField
    FlowPanel eventSourceContainer;

    @UiField
    Button selectFileButton;

    @UiField
    Button createEventFileButton;

    @UiField
    FlowPanel eventsPanel;

    @UiField
    VerticalPanel selectFilePanel;

    @UiField
    VerticalPanel reportConfigurationPanel;
    
    @UiField
    Button loadSettingsButton;
    
    @UiField
    Button saveSettingsButton;

    @UiField
    Button cancelButton;

    @UiField
    Label reportNameLabel;

    @UiField
    TextBox emptyCellValueTextBox;

    @UiField
    ScrollPanel eventTypeColumnContainerPanel;

    @UiField
    FlowPanel eventTypeColumnPanel;

    @UiField
    FlowPanel eventFileColumnsPanel;

    @UiField
    Button closeEventTypeColumnPanelButton;
    
    @UiField
    ScrollPanel customizeDefaultColumnContainer;

    @UiField
    FlowPanel customizeDefaultColumnPanel;

    @UiField
    Button closeCustomizeDefaultColumnPanelButton;

    @UiField
    Styles style;
    
    @UiField
    ListBox mergeByColumnList;
    
    @UiField
    ListBox sortByColumnList;
    
    @UiField
    CheckBox excludeDatalessCheckBox;
    
    @UiField
    CheckBox relocateDuplicateColCheckBox;
    
    /** used to show all files under any already opened folders in the tree */
    @UiField
    Button showAllButton;
    
    /** the root of the file tree */
    private CellTree tree;
    
    final VerticalPanel loadingIndicator;

    private EventSourcesTreeModel eventSourcesTree;

    private ReportProperties reportProperties;

    /** 
     * the default columns not specific to an event (e.g. learner state) but to a type of source (e.g. sensor writer file)
     * For example this may contain the 'domain session time' column but not the 'acceleration_X' column
     */
    private List<EventReportColumn> allReportColumns;

    interface EventReportCreatorWidgetUiBinder extends
            UiBinder<Widget, EventReportCreatorWidget> {
    }

    interface Styles extends CssResource {

        String customizeAnchor();

        String smallColumnRightMargin();
    }

    /**
     * Constructor
     */
    public EventReportCreatorWidget() {
    	    	
        initWidget(uiBinder.createAndBindUi(this));
        
        loadingIndicator = showLoading();
        
        selectFilePanel.setVisible(true);
        selectFileButton.setEnabled(false);
        reportConfigurationPanel.setVisible(false);
        
        eventSourceContainer.add(loadingIndicator); 
        
        showAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                if(tree != null){
                    tree.setDefaultNodeSize(Integer.MAX_VALUE);
                    
                    // show all children for any already open nodes under the root node
                    // i.e. if a root folder is open this will be the same as clicking 'show more' until there are
                    // no more files to show under that folder.
                    for(int childIndex = 0; childIndex < tree.getRootTreeNode().getChildCount(); childIndex++){
                        
                        if(tree.getRootTreeNode().isChildOpen(childIndex)){
                            tree.getRootTreeNode().setChildOpen(childIndex, false);
                            tree.getRootTreeNode().setChildOpen(childIndex, true);
                        }
                    }
                }
            }
        });
        
        selectFileButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                try {

                    Set<EventSourceTreeNode> selectedNodes = eventSourcesTree.getSelectedNodes();
                    List<Integer> selectedNodeIds = new ArrayList<Integer>(selectedNodes.size());
                    StringBuilder reportNameBuilder = new StringBuilder();

                    for (EventSourceTreeNode node : selectedNodes) {
                        
                        // currently don't support grabbing all the log files under a selected folder
                        // and the log files wanting to be parsed have to be selected specifically
                        if(node.isFolder()){
                            continue;
                        }
                        
                        selectedNodeIds.add(node.getNodeId());
                        reportNameBuilder.append(node.getName()).append(", ");
                    }
                    
                    if(selectedNodeIds.isEmpty()){
                        CommonResources.displayDialog("Please select one or more files", "You need to select one or more files to parse before continuing.  Folders should not be selected.");
                        return;
                    }

                    String reportNames = reportNameBuilder.substring(0, reportNameBuilder.length() - 2);

                    reportNameLabel.setText(reportNames);
                    selectFileButton.setEnabled(false);

                    rpcService.selectEventSource(selectedNodeIds, new AsyncCallback<ReportProperties>() {
                        @Override
                        public void onFailure(Throwable caught) {

                            CommonResources.displayErrorDialog("Parse Event Sources Error", "Parsing Event Sources", caught);

                            selectFileButton.setEnabled(true);
                        }

                        @Override
                        public void onSuccess(final ReportProperties result) {

                            if (result != null) {

                                populateReportProperties(result);

                            } else {

                                CommonResources.displayErrorDialog("Server Error", "Selecting a file", "Response was empty. See the server for details");
                            }

                            selectFileButton.setEnabled(true);
                        }
                    });

                } catch (RuntimeException e) {

                    CommonResources.displayErrorDialog("Client Error", "Selecting a file", e);
                }
            }
        });
        
        saveSettingsButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                CommonResources.displayTextInputDialog("Save Settings", "Save the settings for this report so it can be used on future reports. Enter a file name:", ReportProperties.DEFAUL_SETTINGS_FILENAME, new CommonResources.TextInputDialogCallback(){

                    @Override
                    public void onInput(final String input) {
                        
                        if(input != null) {
                            
                            updateReportProperties();
                            
                            rpcService.saveReportProperties(input + ReportProperties.DEFAUL_SETTINGS_FILENAME_EXT, reportProperties, new AsyncCallback<Boolean>(){

                                @Override
                                public void onFailure(Throwable caught) {
                                    
                                    CommonResources.displayErrorDialog("Save Settings Error", "Saving settings", caught);
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    
                                    if(result != null && result.booleanValue()) {
                                        
                                        CommonResources.displayDialog("Saving Settings Success", "The file was saved as " + input + ReportProperties.DEFAUL_SETTINGS_FILENAME_EXT);
                                        
                                    } else {
                                        
                                        CommonResources.displayErrorDialog("Saving Settings Error", "Saving settings", "See server for details.");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        
        loadSettingsButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                rpcService.getSettingsList(new AsyncCallback<List<String>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        
                        CommonResources.displayErrorDialog("Getting Settings List Error", "Getting settings list", caught);
                    }

                    @Override
                    public void onSuccess(List<String> result) {
                        
                        if(result != null) {
                            
                            final SettingsListDialog settingsList = new SettingsListDialog(result);
                            
                            settingsList.addCloseHandler(new CloseHandler<PopupPanel>() {

                                @Override
                                public void onClose(CloseEvent<PopupPanel> event) {
                                    
                                    final String selectedFile = settingsList.getSelectedFile();
                                    
                                    if(selectedFile != null) {
                                        
                                        rpcService.loadReportProperties(selectedFile, reportProperties, new AsyncCallback<ReportProperties>(){

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                
                                                CommonResources.displayErrorDialog("Loading Settings Error", "Loading settings", caught);
                                            }

                                            @Override
                                            public void onSuccess(ReportProperties result) {

                                                if (result != null) {

                                                    //removes all null entries from the list
                                                    reportProperties.getReportColumns().removeAll(Collections.singletonList(null));

                                                    //copy of current event sources default columns (enabled or not)
                                                    List<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>(allReportColumns);

                                                    //going to rebuild this by merging what is currently available for the event sources
                                                    //and the properties being loaded
                                                    reportProperties = null;
                                                    
                                                    //remove the current events of interest created when the event sources were parsed
                                                    eventsPanel.clear();

                                                    //removes all null entries from the list
                                                    result.getReportColumns().removeAll(Collections.singletonList(null));

                                                    //render the event column properties table from the settings file loaded
                                                    populateReportProperties(result);

                                                    //contains the default columns from the settings file
                                                    List<EventReportColumn> newReportColumns = new ArrayList<EventReportColumn>(allReportColumns);

                                                    //remove default columns that came from the settings file if they aren't available
                                                    //in the selected and parsed event sources, leaving a merged list of only default
                                                    //columns that are in the event sources and are in the settings file
                                                    newReportColumns.retainAll(reportColumns);

                                                    //create collection of default columns that aren't in the settings file but from event sources
                                                    List<EventReportColumn> oldReportColumns = new ArrayList<EventReportColumn>(reportColumns);
                                                    oldReportColumns.removeAll(newReportColumns);

                                                    allReportColumns.clear();

                                                    //merged default columns
                                                    allReportColumns.addAll(newReportColumns);

                                                    //default columns from event sources
                                                    allReportColumns.addAll(oldReportColumns);

                                                    reportProperties.getReportColumns().clear();

                                                    //add only the merged default columns
                                                    reportProperties.getReportColumns().addAll(newReportColumns);

                                                    //fill other positions with null to indicate not enabled for those default columns
                                                    //that are in the event sources but not in the settings
                                                    while (reportProperties.getReportColumns().size() < allReportColumns.size()) {

                                                        reportProperties.getReportColumns().add(null);
                                                    }

                                                    //render the table with default columns in the event sources, checked or not
                                                    //checked based on if there is a null value in report columns in the same index of allReportColumns
                                                    populateReportColumns();

                                                    CommonResources.displayDialog("Successfully Loaded Settings", "The settings file " + selectedFile + " was loaded successfully.");

                                                } else {

                                                    CommonResources.displayErrorDialog("Loading Settings Error", "Loading settings", "See server for details.");
                                                    
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                            
                            settingsList.center();
                            
                        } else {
                            
                            CommonResources.displayErrorDialog("Getting Settings List Error", "Getting settings list", "See server for details.");
                        }
                    }
                });
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                reportProperties = null;
                eventsPanel.clear();
                reportConfigurationPanel.setVisible(false);
                eventTypeColumnContainerPanel.setVisible(false);
                customizeDefaultColumnContainer.setVisible(false);
                selectFilePanel.setVisible(true);

            }
        });

        createEventFileButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (reportProperties != null) {
                    
                    updateReportProperties();

                    createEventFileButton.setEnabled(false);
                    for (EventType eventType : reportProperties.getEventTypeOptions()) {
                        
                        List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
                        java.util.Collections.sort(selectedColumns);
                        reportProperties.getReportColumns().addAll(selectedColumns);
                    }
                    
                    // Display a dialog box to inform the user of report creation progress
                    final DialogBox pleaseWaitDialog = new DialogBox();
                    pleaseWaitDialog.setText("Please Wait...");

                    pleaseWaitDialog.getElement().getStyle().setProperty("maxWidth", "200px");
                    pleaseWaitDialog.getElement().getStyle().setProperty("maxHeight", "50px");
                    VerticalPanel contents = new VerticalPanel();
                    contents.setSpacing(5);
                    contents.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
                    
                    final SimpleProgressBar progressbar = new SimpleProgressBar();
                    contents.add(progressbar);
                    
                    pleaseWaitDialog.add(contents);
                    pleaseWaitDialog.center();
                    pleaseWaitDialog.show();                  
                    
                    

                    rpcService.generateEventReport(reportProperties, new AsyncCallback<String>() {

                        @Override
                        public void onFailure(Throwable caught) {
                        	
                        	pleaseWaitDialog.hide();
                        	
                        	// Notify the server to remove the progress indicator for this client
            				rpcService.removeProgressIndicator(new AsyncCallback<Boolean>(){
            					@Override
                        		public void onFailure(Throwable caught){                    			
                        			// Nothing to do
                        		}
            					@Override
                        		public void onSuccess(Boolean result){                    			
                        			// Nothing to do
                        		}
            				});
                            
                            for (EventType eventType : reportProperties.getEventTypeOptions()) {
                                List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
                                reportProperties.getReportColumns().removeAll(selectedColumns);
                            }
                            createEventFileButton.setEnabled(true);
                            
                            //build the error message
                            StringBuilder sb = new StringBuilder();
                            sb.append("There was an error when trying to create the event report file.")
                                    .append("  Please refer to the ERT log followed by the GAS log in GIFT\\output\\logger\\tools\\.");                            
                            
                            if(caught.getMessage() != null){
                                
                                //create user friendly message about the exception
                                handleExceptionMessage(caught, reportProperties, sb);                                               	                                               
                            }
                            
                            sb.append("\n\nDetails: ").append(caught.toString());
                            
                            Window.alert(sb.toString());
                        }

                        @Override
                        public void onSuccess(String result) {
                            
                        	// Periodically call rpcService to get the current progress of report creation and display it to the dialog
                            final Timer timer = new Timer(){
                            	@Override
                            	public void run(){     
                            		
                            		rpcService.getProgressIndicator(new AsyncCallback<GenerateReportStatus>(){
                                		@Override
                                		public void onFailure(Throwable caught){             
                                			
                                			cancel();  
                            				pleaseWaitDialog.hide();
                            				
                            				// Notify the server to remove the progress indicator for this client
                            				rpcService.removeProgressIndicator(new AsyncCallback<Boolean>(){
                            					@Override
                                        		public void onFailure(Throwable caught){                    			
                                        			// Nothing to do
                                        		}
                            					@Override
                                        		public void onSuccess(Boolean result){                    			
                                        			// Nothing to do
                                        		}
                            				});
                            				
                            				for (EventType eventType : reportProperties.getEventTypeOptions()) {
                                                List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
                                                reportProperties.getReportColumns().removeAll(selectedColumns);
                                            }
                                            createEventFileButton.setEnabled(true);
                                            
                                            //build the error message
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("There was an error when trying to create the event report file.")
                                                    .append(" Failed to receive information from the server due to unhandled exception.");
                                			
                                			if(caught.getClass().getName().contains("com.google.")){
                                				sb.append("\n\nNote: the error might have been caused due to a client-server communication ")
                                						.append("issue. Make sure the GAS is currently running and check its console to see if it is ")
                                						.append("currently displaying any errors.");
                                			}
                                			
                                			sb.append(" \n\nDetails: ").append(caught.toString());
                                                    
                                            Window.alert(sb.toString());   
                                		}
                                		
                                		@Override
                                		public void onSuccess(GenerateReportStatus result){                                			
                                			
                                			if(result.getException() != null){
                                				
                                				cancel();  
                                				pleaseWaitDialog.hide();

                                				// Notify the server to remove the progress indicator for this client
                                				rpcService.removeProgressIndicator(new AsyncCallback<Boolean>(){
                                					@Override
                                            		public void onFailure(Throwable caught){                    			
                                            			// Nothing to do
                                            		}
                                					@Override
                                            		public void onSuccess(Boolean result){                    			
                                            			// Nothing to do
                                            		}
                                				});
                               				
                                				Throwable caught = result.getException();
                                				
                                				 for (EventType eventType : reportProperties.getEventTypeOptions()) {
                                                     List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
                                                     reportProperties.getReportColumns().removeAll(selectedColumns);
                                                 }
                                                 createEventFileButton.setEnabled(true);
                                                 
                                                 //build the error message
                                                 StringBuilder sb = new StringBuilder();
                                                 sb.append("There was an error when trying to create the event report file.")
                                                         .append("  Please refer to the ERT log followed by the GAS log in GIFT\\output\\logger\\tools\\.");                            
                                                 
                                                 if(caught.getMessage() != null){
                                                     //create user friendly message about the exception
                                                     handleExceptionMessage(caught, reportProperties, sb);	                                              
                                                 }
                                                 
                                                 sb.append("\n\nDetails: ").append(caught.toString());
                                                 
                                                 // Since the exception thrown is obfuscated by Javascript, get the stack trace from the progress indicator instead
                                                 if(result.getStackTraceMessage() != null){
                                                	 sb.append("\n").append(result.getStackTraceMessage());
                                                 }
                                                 
                                                 Window.alert(sb.toString());
                                                 
                                                 return;
                                                 
                                			}else if(result.isFinished()){
                                				
                                				cancel();  
                                				pleaseWaitDialog.hide();
                                				
                                				// Notify the server to remove the progress indicator for this service request
                                				rpcService.removeProgressIndicator(new AsyncCallback<Boolean>(){
                                					@Override
                                            		public void onFailure(Throwable caught){                    			
                                            			// Nothing to do
                                            		}
                                					@Override
                                            		public void onSuccess(Boolean result){                    			
                                            			// Nothing to do
                                            		}
                                				});
                                				
                                				//
                                				// Build success message to display to ERT client
                                				//
                                				
                                				String filename = reportProperties.getFileName();
                                				if(result.getReportResult() != null) {
                                				    filename = result.getReportResult().getLocationOnServer();
                                				}
                                				StringBuffer successMessage = new StringBuffer();
                                				successMessage.append("Successfully created event report file named ")
                                				              .append(filename)
                                				              .append("\n\n");
                                				
                                				if(result.getFinishedAdditionalDetails() != null && !result.getFinishedAdditionalDetails().isEmpty()){
                                				    successMessage.append("Additional Details:\n\n").append(result.getFinishedAdditionalDetails());
                                				}
                                				
                                				Window.alert(successMessage.toString());

                                				for (EventType eventType : reportProperties.getEventTypeOptions()) {
                                                     List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
                                                     reportProperties.getReportColumns().removeAll(selectedColumns);
                                                 }
                                				 
                                                 createEventFileButton.setEnabled(true);                                                                                                                     
                                                 return;                               				
                                			}
                                			
                                			progressbar.updateProgress(result.getProgress());
                                			
                                			schedule(PROGRESS_CHECK_INTERVAL_MS);
                                		}
                                	});             	                        	
                            	}
                            };
                        	
                            timer.schedule(PROGRESS_CHECK_INTERVAL_MS);
                        }
                    });
                    
                   
                }
            }
        });

        closeEventTypeColumnPanelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventTypeColumnContainerPanel.setVisible(false);
            }
        });
        
        closeCustomizeDefaultColumnPanelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                customizeDefaultColumnContainer.setVisible(false);
            }
        });

        rpcService.getEventSources(true, new AsyncCallback<EventSourcesTreeModel>() {
        	        	
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(EventSourcesTreeModel result) {
            	            	
                if (result != null) {
                    eventSourcesTree = result;
                    EventSourceTreeNode rootNode = new EventSourceTreeNode(0, "Event Sources", true);
                    
                    for (EventSourceTreeNode resultRootNode : result.getRootNodes()) {
                        rootNode.addChild(resultRootNode);
                    }
                    tree = new CellTree(result, rootNode);
                    tree.getRootTreeNode().setChildOpen(0, true);
                    
                    
                    eventSourcesTree.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

                        @Override
                        public void onSelectionChange(SelectionChangeEvent arg0) {
                            // TODO Auto-generated method stub
                            Set<EventSourceTreeNode> selectedNodes = eventSourcesTree.getSelectedNodes();                            
                            if (selectedNodes != null) {
                                for (EventSourceTreeNode node : selectedNodes) {
                                    for (EventSourceTreeNode child : node.getChildren()) {
                                        if (child != null) {
                                            eventSourcesTree.setSelectedNode(child);
                                        }
                                    }
                                }
                            }
                        }
                        
                    });
                                      
                    loadingIndicator.setVisible(false);
                    
                    eventSourceContainer.add(tree);
                    selectFilePanel.setVisible(true);
                    selectFileButton.setEnabled(true);
                }
            }
        });
        
        excludeDatalessCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                reportProperties.setExcludeDatalessColumns(excludeDatalessCheckBox.getValue());
            }
        });
        
        relocateDuplicateColCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                
                reportProperties.setRelocateDuplicateColumns(relocateDuplicateColCheckBox.getValue());
            }
        });
        
        mergeByColumnList.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (mergeByColumnList.getItemCount() > 0) {

                    if (mergeByColumnList.getSelectedIndex() > 0) {

                        reportProperties.setMergeByColumn(allReportColumns.get(mergeByColumnList.getSelectedIndex() - 1));

                    } else {

                        reportProperties.setMergeByColumn(null);
                    }
                }
            }
        });

        sortByColumnList.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (sortByColumnList.getItemCount() > 0) {

                    if (sortByColumnList.getSelectedIndex() > 0) {

                        reportProperties.setSortByColumn(allReportColumns.get(sortByColumnList.getSelectedIndex() - 1));

                    } else {

                        reportProperties.setSortByColumn(null);
                    }
                }
            }
        });
    }
    
    /**
     * Provide a more user friendly message about the provided exception.
     * 
     * @param caught the exception caught to decompose into a more user friendly description
     * @param reportProperties information about the report being conducted that caused the exception
     * @param userFriendlyMessage the buffer containing the user friendly message
     */
    private void handleExceptionMessage(Throwable caught, ReportProperties reportProperties, StringBuilder userFriendlyMessage){        
        
        if(caught.getMessage().contains("being used by another process")){
            //GWT doesn't support java.io.FileNotFoundException, therefore this is the way we determine the exception
            //is that type - by the message content generated by Java
            
            userFriendlyMessage.append("\n\nNote: is the output file of ").append(reportProperties.getFileName()).append(" currently opened?  If so,")
                           .append(" close it and then run the report again.");
            
        }else if(caught.getMessage().contains("is no content to write")){
            
            userFriendlyMessage.append("\n\nNote: did you select any 'events of interest' to include in your report?  ")
                   .append("Make sure at least one event type is checked before generating a report.");
            
        }else if(caught.getMessage().contains("heap space")){
            
            userFriendlyMessage.append("\n\nNote: if the error reported mentions out of memory or Java heap space, please consult\n")
                   .append("the GIFT Troubleshooting document for information on how to increase the memory limit for the ERT.");
      
        } else if(caught.getClass().getName().contains("com.google.")){
            
            userFriendlyMessage.append("\n\nNote: the error might have been caused due to a client-server communication ")
                   .append("issue. Make sure the GAS is currently running and check its console to see if it is ")
                   .append("currently displaying any errors.");
       }
    }

    private void populateReportColumns() {
        eventFileColumnsPanel.clear();
        HorizontalPanel columnsHeader = new HorizontalPanel();
        HTML checkBoxHeader = new HTML();
        columnsHeader.add(checkBoxHeader);
        columnsHeader.setCellWidth(checkBoxHeader, "20px");
        HTML columnNameHeader = new HTML("<b>Column Name</b>");
        columnsHeader.add(columnNameHeader);
        columnsHeader.setCellWidth(columnNameHeader, "175px");
        HTML columnHeaderHeader = new HTML("<b>Column Header</b>");
        columnsHeader.add(columnHeaderHeader);
        eventFileColumnsPanel.add(columnsHeader);
        final List<EventReportColumn> reportColumns = reportProperties.getReportColumns();
        for (final EventReportColumn reportColumn : allReportColumns) {
            HorizontalPanel columnPanel = new HorizontalPanel();
            columnPanel.setWidth("100%");
            columnPanel.setSpacing(1);
            final CheckBox displayColumnCheckBox = new CheckBox();
            displayColumnCheckBox.setValue(reportColumn.isEnabled());
            displayColumnCheckBox.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    reportColumn.setEnabled(displayColumnCheckBox.getValue());
                }
            });
            columnPanel.add(displayColumnCheckBox);
            columnPanel.setCellWidth(displayColumnCheckBox, "20px");
            Label columnName = new Label(reportColumn.getDisplayName());
            columnPanel.add(columnName);
            columnPanel.setCellWidth(columnName, "175px");

            Label columnHeader = new Label(reportColumn.getColumnName());
            columnPanel.add(columnHeader);
            columnPanel.setCellWidth(columnHeader, "150px");
            
            Image editImage = new Image("images/edit.png");
            editImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    populateCustomizeDefaultColumnPanel(reportColumn.getDisplayName(), reportColumn.getProperties());
                }
            });
            columnPanel.add(editImage);
            columnPanel.setCellWidth(editImage, "16px");
            columnPanel.setCellHorizontalAlignment(editImage, HasHorizontalAlignment.ALIGN_RIGHT);
            
            Image upImage = new Image("images/arrowup.png");
            upImage.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    int index = allReportColumns.indexOf(reportColumn);
                    if (index > 0) {
                        EventReportColumn tmp = reportColumns.get(index);
                        reportColumns.set(index, reportColumns.get(index - 1));
                        reportColumns.set(index - 1, tmp);

                        tmp = allReportColumns.get(index);
                        allReportColumns.set(index, allReportColumns.get(index - 1));
                        allReportColumns.set(index - 1, tmp);
                        populateReportColumns();
                    }
                }
            });
            columnPanel.add(upImage);
            columnPanel.setCellWidth(upImage, "20px");
            columnPanel.setCellHorizontalAlignment(upImage, HasHorizontalAlignment.ALIGN_RIGHT);
            
            Image downImage = new Image("images/arrowdown.png");
            downImage.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    int index = allReportColumns.indexOf(reportColumn);
                    if (index != -1 && (index + 1) < allReportColumns.size()) {
                        EventReportColumn tmp = reportColumns.get(index);
                        reportColumns.set(index, reportColumns.get(index + 1));
                        reportColumns.set(index + 1, tmp);

                        tmp = allReportColumns.get(index);
                        allReportColumns.set(index, allReportColumns.get(index + 1));
                        allReportColumns.set(index + 1, tmp);
                        populateReportColumns();
                    }
                }
            });
            columnPanel.add(downImage);
            columnPanel.setCellWidth(downImage, "20px");
            columnPanel.setCellHorizontalAlignment(downImage, HasHorizontalAlignment.ALIGN_RIGHT);
            eventFileColumnsPanel.add(columnPanel);
        }
    }
    
    private void populateCustomizeDefaultColumnPanel(String columnName, ColumnProperties properties) {
        
        customizeDefaultColumnPanel.clear();

        if (properties == null) {

            customizeDefaultColumnPanel.add(new InlineLabel("No properties to modify"));

        } else if (properties instanceof TimeProperties) {

            customizeDefaultColumnPanel.add(new TimePropertiesWidget(columnName, (TimeProperties) properties));

        } else {

            customizeDefaultColumnPanel.add(new InlineLabel("Could not construct a properties widget for unknown class: " + properties.getClass().getName()));
        }

        customizeDefaultColumnContainer.setVisible(true);
    }

    private void populateEventTypeColumns(final EventType eventType) {
        eventTypeColumnPanel.clear();
        FlexTable table = new FlexTable();
        table.getColumnFormatter().setWidth(0, "20px");
        HTML columnNameHeader = new HTML("<b>Column Name</b>");
        columnNameHeader.getElement().getStyle().setMarginRight(20, Style.Unit.PX);
        table.setWidget(0, 1, columnNameHeader);
        HTML columnHeaderHeader = new HTML("<b>Column Header</b>");
        table.setWidget(0, 2, columnHeaderHeader);

        for (final EventReportColumn reportColumn : eventType.getEventColumns()) {
            int rowNumber = table.getRowCount();
            final CheckBox displayColumnCheckBox = new CheckBox();
            
            displayColumnCheckBox.setValue(reportProperties.getEventTypeDisplay(eventType).isEnabled(reportColumn));
            
            displayColumnCheckBox.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    
                    EventColumnDisplay eventColumnDisplay = reportProperties.getEventTypeDisplay(eventType);
                    
                    if (eventColumnDisplay != null) {
                        
                        eventColumnDisplay.setEnabled(reportColumn, displayColumnCheckBox.getValue());
                    }
                }
            });
            
            if(!reportProperties.isSelected(eventType)) {
                
               displayColumnCheckBox.setEnabled(false);
            }
            
            table.setWidget(rowNumber, 0, displayColumnCheckBox);
            Label columnName = new Label(reportColumn.getDisplayName());
            columnName.getElement().getStyle().setMarginRight(20, Style.Unit.PX);
            table.setWidget(rowNumber, 1, columnName);
            Label columnHeader = new Label(reportColumn.getColumnName());
            table.setWidget(rowNumber, 2, columnHeader);
        }

        eventTypeColumnPanel.add(table);

        table.getColumnFormatter().getElement(1).getStyle().setProperty("marginRight", "50px");
    }

    private void populateReportProperties(final ReportProperties properties) {
        
        eventTypeColumnContainerPanel.setVisible(false);
        
        this.reportProperties = properties;
        allReportColumns = new ArrayList<EventReportColumn>(reportProperties.getReportColumns());
        selectFilePanel.setVisible(false);

        List<EventType> eventTypes = new ArrayList<EventType>(properties.getEventTypeOptions());
        java.util.Collections.sort(eventTypes);  //Alphabetize

        for (final EventType eventType : eventTypes) {
            
            final CheckBox eventTypeCheckBox = new CheckBox(eventType.getDisplayName());
            
            eventTypeCheckBox.setValue(properties.isSelected(eventType));
            
            eventTypeCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    
                    EventColumnDisplay eventColumnDisplay = reportProperties.getEventTypeDisplay(eventType);

                    if (eventTypeCheckBox.getValue()) {

                        properties.setSelected(eventType, true);

                        for (final EventReportColumn reportColumn : eventType.getEventColumns()) {
                            
                            if(eventColumnDisplay != null) {
                                
                                eventColumnDisplay.setEnabled(reportColumn, true);
                            }
                        }

                        populateEventTypeColumns(eventType);

                    } else {

                        properties.setSelected(eventType, false);

                        for (final EventReportColumn reportColumn : eventType.getEventColumns()) {

                            if(eventColumnDisplay != null) {
                                
                                eventColumnDisplay.setEnabled(reportColumn, false);
                            }
                        }
                        
                        populateEventTypeColumns(eventType);
                    }
                }
            });
            
            FlowPanel container = new FlowPanel();
            container.add(eventTypeCheckBox);

            //
            // Add a description component
            //
            if(eventType.getDescription() != null){
                
                final String description = eventType.getDescription();                
                
                final Image helpImage = new Image("images/help.png");
                helpImage.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        
                        PopupPanel popup = new PopupPanel(true);                        
                        popup.setWidget(new HTML(description + "<br><br>Click outside of this popup to close it."));
                        popup.setPopupPosition(helpImage.getAbsoluteLeft() + 20, helpImage.getAbsoluteTop() + 20);
                        popup.show();                
                    }

                });
                
                //add some space first
                container.add(new InlineHTML(" "));
                
                container.add(helpImage);

            }
            
            //
            // Add a customize component
            //
            Anchor customizeColumnsAnchor = new Anchor("Customize");
            customizeColumnsAnchor.getElement().addClassName(style.customizeAnchor());
            customizeColumnsAnchor.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    populateEventTypeColumns(eventType);
                    eventTypeColumnContainerPanel.setVisible(true);
                }
            });

            container.add(customizeColumnsAnchor);

            eventsPanel.add(container);

            java.util.Collections.sort(eventType.getEventColumns());  //Alphabetize
        }
        
        excludeDatalessCheckBox.setValue(reportProperties.shouldExcludeDatalessColumns());
        relocateDuplicateColCheckBox.setValue(reportProperties.shouldRelocateDuplicateColumns());

        mergeByColumnList.clear();

        mergeByColumnList.addItem("");

        for (EventReportColumn reportColumn : allReportColumns) {

            mergeByColumnList.addItem(reportColumn.getDisplayName());

            if (reportProperties.getMergeByColumn() != null && reportProperties.getMergeByColumn().equals(reportColumn)) {

                mergeByColumnList.setSelectedIndex(mergeByColumnList.getItemCount() - 1);
            }
        }

        sortByColumnList.clear();

        sortByColumnList.addItem("");

        for (EventReportColumn reportColumn : allReportColumns) {

            sortByColumnList.addItem(reportColumn.getDisplayName());

            if (reportProperties.getSortByColumn() != null && reportProperties.getSortByColumn().equals(reportColumn)) {

                sortByColumnList.setSelectedIndex(sortByColumnList.getItemCount() - 1);
            }
        }

        populateReportColumns();
        emptyCellValueTextBox.setText(properties.getEmptyCellValue());

        reportConfigurationPanel.setVisible(true);
    }
    
    private void updateReportProperties() {
        
        if (reportProperties != null) {
            
            reportProperties.setEmptyCellValue(emptyCellValueTextBox.getValue());
        }
    }
    
    /** 
     * Displays a loading indicator while the log files are retrieved.
     * @return VerticalPanel a vertical panel containing the loading indicator.
     */
    private VerticalPanel showLoading() {
    	
        VerticalPanel contents = new VerticalPanel();
        contents.setSpacing(5);
        contents.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        contents.add(new HTML("Retrieving event sources. Please wait... "));
        contents.add(new Image("images/tinyloading.gif"));
    	
    	return contents;
    }
}
