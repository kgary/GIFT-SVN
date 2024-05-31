/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Parse an initialize domain session request message.
 * 
 * @author mhoffman
 *
 */
public class InitializeDomainSessionRequestEvent extends DomainSessionEvent {
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Parse the event.
     * @param time the epoch time at which the event occurred
     * @param domainSessionMessageEntry contains domain session information
     * @param initDSRequest the request event to parse for additional attributes
     */
    public InitializeDomainSessionRequestEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, InitializeDomainSessionRequest initDSRequest) {
        super(MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);
        
        CommonEventObjectsUtil.createCellsForMobileAppProperties(
                initDSRequest.getClientInfo().getMobileAppProperties(), cells);
        
        columns.add(EventReportColumn.PLATFORM_COLUMN);
        columns.add(EventReportColumn.VERSION_COLUMN);
        columns.add(EventReportColumn.SCREEN_WIDTH_COLUMN);
        columns.add(EventReportColumn.SCREEN_HEIGHT_COLUMN);
    }

    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[InitializeDomainSessionRequestEvent: ");
        sb.append(super.toString());
        
        sb.append(", columns = {");
        for(EventReportColumn column : columns){
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
