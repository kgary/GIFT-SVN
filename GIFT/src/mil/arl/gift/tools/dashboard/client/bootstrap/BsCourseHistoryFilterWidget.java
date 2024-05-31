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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.constants.IconType;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.rpcs.LmsCourseRecordsResponse;

/**
 * The BsCourseHistoryFilterWidget represents a 'filter widget' in the My Stats course filter list.
 * The widget displays the course image/name, but may hold additional details in the future.
 * 
 * @author nblomberg
 *
 */
public class BsCourseHistoryFilterWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(BsCourseHistoryFilterWidget.class.getName());
    
    private static BsCourseHistoryFilterWidgetUiBinder uiBinder = GWT.create(BsCourseHistoryFilterWidgetUiBinder.class);
    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    private static final String RPC_ERROR_TITLE = "Rpc Error";
    private static final String NO_MORE_HISTORY_TITLE = "No More History";
    private static final String NO_MORE_HISTORY_MESSAGE = "There is no more course history available.";
    
    // used to format the timestamp of the record
    private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm a z";    
    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat(DATE_FORMAT);

    
    @UiField
    protected Image ctrlCourseImage;
    
    @UiField
    protected Heading ctrlCourseName;
    
    @UiField
    protected Collapse resultsCollapse;
    
    @UiField
    protected Widget headerPanel;
    
    @UiField
    protected Widget mainPanel;
    
    @UiField
    protected HasText lastTakenLabel;
    
    @UiField
    protected HasText courseIdLabel;
    
    @UiField
    protected FlowPanel resultsPanel;
    
    @UiField
    protected Button ctrlMoreHistory;
    
    @UiField
    protected Button showButton;
    
    @UiField
    protected BlockerPanel loadBlocker;
    
    // The number of records to retrieve from the server at a time.
    private static final int COURSE_RECORD_PAGE_SIZE = 10;
    
    // The current page index that the client has retrieved from the server.
    private int pageIndex = 0;

	private LMSCourseRecord rootRecord;
    
    
    // The name of the course that this widget represents.
    
    interface BsCourseHistoryFilterWidgetUiBinder extends UiBinder<Widget, BsCourseHistoryFilterWidget> {
    }
    
    
    /**
     * Default Constructor - needed for gwt UiBinder
     */
    private BsCourseHistoryFilterWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        
        logger.fine("Loading the BsCourseHistoryWidget for session id: " + UiManager.getInstance().getSessionId());

        
        // Fill in a default image here, but eventually we will want to pull this from the domain option data.
        ctrlCourseImage.setUrl(DomainOption.COURSE_DEFAULT_IMAGE);
        
        headerPanel.addDomHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				setResultsVisible(!resultsCollapse.isShown());
			}
			
		}, ClickEvent.getType());
        
        ctrlMoreHistory.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	
              // Retrieve more records.
              fetchMoreRecords();
            }
                
        });
    }
    
    /**
     * Constructor
     */
    public BsCourseHistoryFilterWidget(LMSCourseRecord rootRecord) {
        
        this();
        
        logger.fine("Creating course history widget for '"+rootRecord.getDomainName()+"'.");
        
        this.rootRecord = rootRecord;
        
        ctrlCourseName.setText(rootRecord.getRoot().getName());
        lastTakenLabel.setText(dateFormat.format(rootRecord.getDate()));
        
        if(rootRecord.getDomainName() != null){
            
            //try to get the first folder of the course.xml path, this could be the workspace or experiment id
            String rootPath = rootRecord.getDomainName();
            if(rootPath.contains(Constants.FORWARD_SLASH)){
                rootPath = rootPath.substring(0, rootPath.indexOf(Constants.FORWARD_SLASH));
            }
            courseIdLabel.setText(rootPath);
            
        }else{
            courseIdLabel.setText("unknown");
        }
    }
    
    /**
     * Sets whether or not the LMS data results for the course represented by this widget should be shown. If the results are currently
     * hidden and need to be shown, this method will also kick off the RPC logic needed to request the LMS data.
     * 
     * @param visible whether or not the results should be visible
     */
    public void setResultsVisible(boolean visible){
    	
    	if(visible){
    		
    		fetchMoreRecords();
    		
    	} else {
    		
    		resultsCollapse.hide();
    		
    		mainPanel.removeStyleName("courseFilterListEntrySelected");
    		headerPanel.removeStyleName("courseFilterListEntrySelected");
    		
    		showButton.setText("Show Results");
    		showButton.setIcon(IconType.PLUS_CIRCLE);
    		showButton.setActive(false);
    		
    		pageIndex = 0;
    		resultsPanel.clear();
    	}
    }
    
    /**
     * Populates the course history list based on a set of records.  Note that this will
     * append to the existing list (unsorted).  Once the new set of records is applied, then
     * the list is filtered by any current filter that may exist.
     * 
     * @param records - The batch of records to append the course list with.
     */
    private void appendRecordList(ArrayList<LMSCourseRecord> records) {
        
        if(logger.isLoggable(Level.FINE)){        
            logger.fine("Populating course list with (" + records.size() + ") new course records.");
        }
        
        //
        // Build map of records grouped by gift event id (e.g. domain session id)
        //
        Map<Integer, List<LMSCourseRecord>> recordsMap = new HashMap<Integer, List<LMSCourseRecord>>();
        for (LMSCourseRecord record : records) {  
            
            List<LMSCourseRecord> recordsListByEvent = recordsMap.get(record.getGIFTEventId());
            if(recordsListByEvent == null){
                recordsListByEvent = new ArrayList<LMSCourseRecord>();
                recordsMap.put(record.getGIFTEventId(), recordsListByEvent);
            }
            
            recordsListByEvent.add(record);
        }
        
        List<List<LMSCourseRecord>> recordLists = new ArrayList<List<LMSCourseRecord>>(recordsMap.values());
        
        //sort the domain session groups so that the most recent are at the top
        Collections.sort(recordLists, new Comparator<List<LMSCourseRecord>>() {

			@Override
			public int compare(List<LMSCourseRecord> o1, List<LMSCourseRecord> o2) {
				
				if(o1.get(0).getDate() != null && o2.get(0).getDate() != null){
					return o2.get(0).getDate().compareTo(o1.get(0).getDate());
				}
				
				return 0;
			}
		});
        
        //build widget for each group
        for(List<LMSCourseRecord> recordsListByEvent : recordLists){
            
            BsCourseHistoryWidget historyItem = new BsCourseHistoryWidget(recordsListByEvent);
            
            resultsPanel.add(historyItem);
        }
        
        pageIndex = records.size();
        
        //disable button to get more history if no more history is available
        ctrlMoreHistory.setEnabled(pageIndex >= COURSE_RECORD_PAGE_SIZE);
        
        showButton.setIconSpin(false);
		showButton.setText("Hide Results");
		showButton.setIcon(IconType.MINUS_CIRCLE);
        
        if(!resultsCollapse.isShown()){
        	
        	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				
				@Override
				public void execute() {
					
					resultsCollapse.show();
					
					mainPanel.addStyleName("courseFilterListEntrySelected");
					headerPanel.addStyleName("courseFilterListEntrySelected");
				}
			});
        }
    }
    
    /**
     * Fetch more records from the lms starting from the current page index and getting
     * records in batches of COURSE_RECORD_PAGE_SIZE.  While the records are being fetched
     * a spinner icon is displayed to indicate that there are more records being retrieved.
     */
    private void fetchMoreRecords() {
    	
    	loadBlocker.block();
    	
    	showButton.setText("Loading Results...");
    	showButton.setActive(true);
		showButton.setIcon(IconType.REFRESH);
		showButton.setIconSpin(true);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Fetching course history for index: " + pageIndex + " with a max of " + COURSE_RECORD_PAGE_SIZE + " results.");
        }
		
        dashboardService.getLmsData(
        		UiManager.getInstance().getSessionId(), 
        		rootRecord.getDomainName(),
        		null,
        		pageIndex, 
        		COURSE_RECORD_PAGE_SIZE,
        		new AsyncCallback<LmsCourseRecordsResponse>() {

            @Override
            public void onFailure(Throwable t) {
                UiManager.getInstance().displayErrorDialog(RPC_ERROR_TITLE, "Error getting the lms data for the user: " + t.getMessage(), null);
                
                logger.severe("Throwable error occurred retrieving the lms data for the user: " + t.getMessage());
              
                showButton.setIconSpin(false);
        		
                if(resultsCollapse.isShown()){
                	showButton.setIcon(IconType.PLUS_CIRCLE);
                	showButton.setText("Show Results");
                	
                } else {
                	showButton.setIcon(IconType.MINUS_CIRCLE);
                	showButton.setText("Hide Results");
                }
                
                loadBlocker.unblock();
            }

            @Override
            public void onSuccess(LmsCourseRecordsResponse result) {
            	
                if (result != null && result.isSuccess()) {
                    
                    if (result.getCourseRecords() != null) {
                        
                        
                        if (!result.getCourseRecords().getRecords().isEmpty()) {
                            appendRecordList((ArrayList<LMSCourseRecord>) result.getCourseRecords().getRecords());
                        } else {
                        	
                            UiManager.getInstance().displayInfoDialog(NO_MORE_HISTORY_TITLE, NO_MORE_HISTORY_MESSAGE);
                            
                            ctrlMoreHistory.setEnabled(false);
                        }

                    } else {
                        logger.fine("Lms data returned null for course records.");
                    }
                    
                } else {
                    logger.severe("Error retrieving lms data for the user: " + result);
                }
                
                showButton.setIconSpin(false);
                
                if(resultsCollapse.isShown()){
                	showButton.setIcon(IconType.PLUS_CIRCLE);
                	showButton.setText("Show Results");
                	
                } else {
                	showButton.setIcon(IconType.MINUS_CIRCLE);
                	showButton.setText("Hide Results");
                }
                
                loadBlocker.unblock(); 
            }
            
        });
    }
    
    /**
     * Gets the root LMS record this widget represents
     * 
     * @return the root LMS record this widget represents
     */
    public LMSCourseRecord getRootRecord(){
    	return rootRecord;
    }

}
