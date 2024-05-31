/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.tools.ert.shared.EventSourcesTreeModel;

/**
 * The async counterpart of
 * <code>ErtRpcService</code>.
 */
public interface ErtRpcServiceAsync {

    /**
     * Return the tree model containing the available event sources
     * 
     * @param refresh whether to refresh the tree of event sources by searching for
     * GIFT data files
     * @return EventSourcesTreeModel - contains nodes representing event sources
     * @throws IllegalArgumentException
     */
    void getEventSources(boolean refresh, AsyncCallback<EventSourcesTreeModel> callback);

    void selectEventSource(List<Integer> eventSourceIds, AsyncCallback<ReportProperties> callback);
    
    void generateEventReport(ReportProperties reportProperties, AsyncCallback<String> callback);
    
    void saveReportProperties(String fileName, ReportProperties properties, AsyncCallback<Boolean> callback);
    
    void loadReportProperties(String fileName, ReportProperties properties, AsyncCallback<ReportProperties> callback);
    
    void getSettingsList(AsyncCallback<List<String>> callback);
    
    void getProgressIndicator(AsyncCallback<GenerateReportStatus> callback);
    
    void removeProgressIndicator(AsyncCallback<Boolean> callback);
}
