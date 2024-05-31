/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.tools.ert.shared.EventSourcesTreeModel;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("rpc")
public interface ErtRpcService extends RemoteService {
    
    /**
     * Return the tree model containing the available event sources
     * 
     * @param refresh whether to refresh the tree of event sources by searching for
     * GIFT data files
     * @return EventSourcesTreeModel - contains nodes representing event sources
     * @throws IllegalArgumentException
     */
    EventSourcesTreeModel getEventSources(boolean refresh) throws IllegalArgumentException;
    
    /**
     * Gather information about the selected event sources.
     * 
     * @param eventSourceIds
     * @return ReportProperties
     * @throws Exception
     */
    ReportProperties selectEventSource(List<Integer> eventSourceIds) throws Exception;
    
    /**
     * Generate a report based on the report properties provided.
     * 
     * @param reportProperties
     * @return String - the name of the report file created
     * @throws Exception if there was a severe problem generating the report
     */
    String generateEventReport(ReportProperties reportProperties) throws Exception;

    /**
     * Save the report properties to the disk
     *
     * @param fileName The name of the file the report properties will be saved
     * in
     * @param properties The report properties to save to disk
     * @return boolean If the save was successful
     * @throws IllegalArgumentException
     */
    boolean saveReportProperties(String fileName, ReportProperties properties) throws IllegalArgumentException;
    
    /**
     * Load the report properties from the disk
     *
     * @param fileName The name of the file the report properties will be loaded
     * from
     * @param properties The report properties of the current report
     * @return ReportProperties The loaded report properties
     * @throws IllegalArgumentException
     */
    ReportProperties loadReportProperties(String fileName, ReportProperties properties) throws IllegalArgumentException;
    
    /**
     * Gets the list of settings saved on the server
     * 
     * @return List<String> The list of setting files on the server
     */
    List<String> getSettingsList();

    /**
     * Gets the progress indicator for a service initiated by a particular client, if applicable
     * 
     * @return ProgressIndicator An indicator of how much of the service has been completed
     */
    GenerateReportStatus getProgressIndicator();
    
    /**
     * Removes the progress indicator associated with a particular client
     * 
     * @return Boolean A true or false value indicating whether or not the progress indicator could be removed
     */
    Boolean removeProgressIndicator();
}
