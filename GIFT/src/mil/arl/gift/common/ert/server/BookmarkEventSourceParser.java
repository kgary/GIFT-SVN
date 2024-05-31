/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.logger.BookmarkEntry;
import mil.arl.gift.common.logger.BookmarkReader;
import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.DomainSessionEvent;

/**
 * This class is responsible for converting bookmark event log entries into events for the ERT.
 * 
 * @author mhoffman
 *
 */
public class BookmarkEventSourceParser extends AbstractEventSourceParser {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(BookmarkEventSourceParser.class);
    
    /**
     * the columns for a bookmark event
     */
    private static List<EventReportColumn> columns;
    
    static{        
        columns = new ArrayList<EventReportColumn>();
        columns.add(EventReportColumn.TIME_COLUMN);
        columns.add(EventReportColumn.DS_TIME_COLUMN);
        columns.add(EventReportColumn.USER_ID_COLUMN);
        columns.add(EventReportColumn.DS_ID_COLUMN);
        columns.add(EventReportColumn.EVENT_TYPE_COLUMN);
        columns.add(EventReportColumn.CONTENT_COLUMN); 
    }
    
    private static final String BOOKMARK_EVENT_NAME = "Bookmark";
    private static final String BOOKMARK_EVENT_DESCRIPTION = "An instructor/experimentor created bookmark (i.e. note) about an observed event of interest.";
    private static EventType bookmarkEventType = new EventType(BOOKMARK_EVENT_NAME, BOOKMARK_EVENT_NAME, BOOKMARK_EVENT_DESCRIPTION);
    private static List<EventType> typesOfEvents;
    
    static{
        typesOfEvents = new ArrayList<EventType>(1);
        typesOfEvents.add(bookmarkEventType);
    }    
    
    /** the bookmark event source file */
    private File file;
    
    /** instance of the reader that parses bookmark event files */
    private BookmarkReader reader;
    
    /**
     * Class constructor - parses the message log file.
     * 
     * @param file the bookmark event source file
     */
    public BookmarkEventSourceParser(File file){
        this.file = file;
        
        init();
    }
    
    /**
     * Parse the bookmark file
     */
    private void init(){
        
        reader = new BookmarkReader();
        reader.parse(file.getAbsolutePath());
        
    }
    
    @Override
    public List<EventType> getTypesOfEvents() {
        return typesOfEvents;
    }

    @Override
    public List<AbstractEvent> getEventsByType(EventType eventType) {
        
        List<AbstractEvent> events = null;
        
        if(eventType.equals(bookmarkEventType)){
            
            List<BookmarkEntry> bookmarks = reader.getBookmarks();
            events = new ArrayList<AbstractEvent>(bookmarks.size());
            
            boolean isDSEvent = false;
            if(reader.getUserId() != null && reader.getDomainSessionId() != null){
                isDSEvent = true;
            }
            
            for(BookmarkEntry entry : bookmarks){                
                
                //support legacy bookmarks that don't have domain session times in the entries
                Double dsTime = entry.getDomainSessionTime() != null ? entry.getDomainSessionTime() : -1;
                
                if(isDSEvent){
                    events.add(new DomainSessionEvent(BOOKMARK_EVENT_NAME, entry.getTime(), 
                            dsTime, 
                            reader.getUserId(), 
                            reader.getDomainSessionId(), 
                            entry.getAnnotation()));
                }else{
                    events.add(new DomainSessionEvent(BOOKMARK_EVENT_NAME, entry.getTime(), dsTime, entry.getAnnotation()));
                }
            }            
            
            logger.info("Found "+events.size()+" bookmark events");
        }

        return events;
    }

    @Override
    public List<EventReportColumn> getDefaultColumns() {
        return columns;
    }
    
    /**
     * Return whether or not the file provided is a bookmark file.
     * 
     * @param file the bookmark event source file
     * @return boolean
     */
    public static boolean isBookmarkLog(File file){
        
        //TODO: create logic here
        boolean checkPassed = true;
        
        logger.info("Check of file named = "+file.getName()+" to be a bookmark event source file resulted in "+checkPassed);

        return checkPassed;
    }

    @Override
    public Integer getParticipantId() {
        return null;
    }

}
