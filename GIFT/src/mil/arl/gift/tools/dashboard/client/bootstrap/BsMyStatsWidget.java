/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.rpcs.LmsCourseRecordsResponse;

/**
 * The My Stats Widget contains the course history details for a user.  It is somewhat a replacement/reskin of the
 * original ReviewLmsDataWidget.java from the TUI, but with a layout specific for the dashboard.
 * 
 * Eventually more subpanels/functionality may be added, but for now, this panel is responsible for displaying
 * the lms course history for a user.
 * 
 * @author nblomberg
 *
 */
public class BsMyStatsWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(BsMyStatsWidget.class.getName());
    
    private static BsMyStatsWidgetUiBinder uiBinder = GWT.create(BsMyStatsWidgetUiBinder.class);
    
    @UiField
    FlowPanel mainPanel;
    
    @UiField
    Container ctrlCourseHistoryContainer;
    
    @UiField
    Heading ctrlNoHistoryMessage;
    
    @UiField
    Paragraph ctrlLoadPanel;
    
    @UiField
    BsLoadingIcon ctrlLoadIcon;
    
    @UiField
    BsLoadingIcon ctrlLoadMoreIcon;
    
    @UiField
    protected TextBox searchBox;
    
    @UiField
    protected Widget noSearchText;
    
    @UiField
    FlowPanel courseFilterList;
    
    private static final String NO_COURSE_HISTORY_MESSAGE = "You don't have any course history yet.  Please complete some courses to view your course history.";
    private static final String RPC_ERROR_TITLE = "Rpc Error";
    private static final String RPC_ERROR_MESSAGE =  "Error getting the lms data for the user. Please check the logs for further details.";
    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);


    interface BsMyStatsWidgetUiBinder extends UiBinder<Widget, BsMyStatsWidget> {
    }
    
    /**
     * Constructor
     */
    public BsMyStatsWidget() {
        
        
        initWidget(uiBinder.createAndBindUi(this));
        
        UiManager.getInstance().fillToBottomOfViewport(mainPanel);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Loading the myStatsWidget for session id: " + UiManager.getInstance().getSessionId());
        }
        
        ctrlLoadMoreIcon.setVisible(false);
        
        searchBox.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						
						String searchText = searchBox.getText();
						
						boolean searchFound = false;
						
						//search for the given text in all of the available history widgets
						for(int i = 0; i < courseFilterList.getWidgetCount(); i++){
							
							Widget widget = courseFilterList.getWidget(i);
							
							if(widget != null && widget instanceof BsCourseHistoryFilterWidget){
								
								BsCourseHistoryFilterWidget filterWidget = (BsCourseHistoryFilterWidget) widget;
								
								LMSCourseRecord rootRecord = filterWidget.getRootRecord();
								
								if(searchText != null 
										&& rootRecord != null 
										&& rootRecord.getRoot() != null 
										&& rootRecord.getRoot().getName() != null
										&& !(rootRecord.getRoot().getName().toLowerCase().contains(searchText.toLowerCase()))){
									
									widget.setVisible(false);
									
								} else {
									
									widget.setVisible(true);
									
									searchFound = true;
								}
							}
						}
						
						noSearchText.setVisible(!searchFound);
					}
				});
			}
		});
        
        // Fetch the initial set of lms data (if any).  Also kick off and display any initial loading page.
        fetchInitialRecords();
    }
    
    
    /**
     * Fetch the initial set of lms records from the database starting at page index of 0 and
     * getting a maximum batch of COURSE_RECORD_PAGE_SIZE records.  This call should only be 
     * made at the initial load of the page.  Since we don't know if the user has any records at all,
     * we display a loading page while the records are being fetched.
     */
    private void fetchInitialRecords() {     
        
        ctrlNoHistoryMessage.setVisible(false);
        ctrlLoadPanel.setVisible(true);
        ctrlLoadIcon.startLoading();
        ctrlCourseHistoryContainer.setVisible(false);
        
        logger.fine("Fetching initial course history results.");
        dashboardService.getLatestRootLMSDataPerDomain(UiManager.getInstance().getSessionId(), new AsyncCallback<LmsCourseRecordsResponse>() {

            @Override
            public void onFailure(Throwable t) {
                UiManager.getInstance().displayErrorDialog(RPC_ERROR_TITLE, RPC_ERROR_MESSAGE, null);
                
                logger.severe("Throwable error occurred retrieving the lms data for the user: " + t.getMessage());
                
                ctrlLoadPanel.setVisible(false);
                ctrlLoadIcon.stopLoading();
                
                ctrlNoHistoryMessage.setText(RPC_ERROR_MESSAGE);
                ctrlNoHistoryMessage.setVisible(true);
            }

            @Override
            public void onSuccess(LmsCourseRecordsResponse result) {
                
                
                if (result != null && result.isSuccess()) {
                    
                    if (result.getCourseRecords() != null) {
                        
                        if (!result.getCourseRecords().getRecords().isEmpty()) {
                            logger.info("Received "+result.getCourseRecords().getRecords().size()+" records.");
                            ctrlCourseHistoryContainer.setVisible(true);
                            appendCourseList((ArrayList<LMSCourseRecord>) result.getCourseRecords().getRecords());
                            
                            ctrlLoadPanel.setVisible(false);
                            ctrlLoadIcon.stopLoading();
                            ctrlNoHistoryMessage.setVisible(false);
                        } else {
                            logger.fine("getLMSData() returned no records.");
                            
                            ctrlLoadPanel.setVisible(false);
                            ctrlLoadIcon.stopLoading();
                            ctrlNoHistoryMessage.setText(NO_COURSE_HISTORY_MESSAGE);
                            ctrlNoHistoryMessage.setVisible(true);
                        }

                    } else {
                        logger.fine("Lms data returned null for course records.");
                        ctrlLoadPanel.setVisible(false);
                        ctrlLoadIcon.stopLoading();
                        ctrlNoHistoryMessage.setText(NO_COURSE_HISTORY_MESSAGE);
                        ctrlNoHistoryMessage.setVisible(true);
                    }
                    
                } else {
                    
                    UiManager.getInstance().displayErrorDialog(RPC_ERROR_TITLE, RPC_ERROR_MESSAGE, null);
                    logger.severe("Error retrieving lms data for the user: " + result);
                    ctrlLoadPanel.setVisible(false);
                    ctrlLoadIcon.stopLoading();
                    
                    ctrlNoHistoryMessage.setText(RPC_ERROR_MESSAGE);
                    ctrlNoHistoryMessage.setVisible(true);
                }
                
            }
            
        });
    }
    
    /**
     * Populates the course history list based on a set of records.  Note that this will
     * append to the existing list (unsorted).  Once the new set of records is applied, then
     * the list is filtered by any current filter that may exist.
     * 
     * @param records - The batch of records to append the course list with.
     */
    private void appendCourseList(ArrayList<LMSCourseRecord> records) {
        
    	 if(logger.isLoggable(Level.FINE)){        
             logger.fine("Populating course list with (" + records.size() + ") new course records.");
         }
    	 
    	 //sort the records alphabetically
    	 Collections.sort(records, new Comparator<LMSCourseRecord>() {

			@Override
			public int compare(LMSCourseRecord o1, LMSCourseRecord o2) {
				
				if(o1.getRoot().getName() != null && o2.getRoot().getName() != null){
					
					int result = o1.getRoot().getName().compareToIgnoreCase(o2.getRoot().getName());
					
					if(result == 0 && o1.getDate() != null && o2.getDate() != null){
						return o2.getDate().compareTo(o1.getDate());
						
					} else {
						return result;
					}
				}
				
				return 0;
			}
		});

         for(LMSCourseRecord record : records){
                
             try{
                 BsCourseHistoryFilterWidget itemObj = new BsCourseHistoryFilterWidget(record); 
                 courseFilterList.add(itemObj);
             }catch(Exception e){
                 logger.log(Level.SEVERE, "Failed to create the course history widget for '"+record.getDomainName()+"'.", e);
             }
         }
    }
}
