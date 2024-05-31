/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import generated.course.Guidance;
import generated.course.LessonMaterial;
import generated.course.Media;
import mil.arl.gift.common.DisplayMediaCollectionRequest;
import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.util.StringUtils;

/**
 * Custom parser for message related to display media.
 * 
 * @author mhoffman
 *
 */
public class DisplayMediaCollectionEvent extends DomainSessionEvent {

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    private static final String LESSON_MATERIAL = "LessonMaterial";
    
    /**
     * Parse the request to display a media collection.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param displayMediaCollection - contains references to the media collection
     */
    public DisplayMediaCollectionEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, DisplayMediaCollectionRequest displayMediaCollection) {
        super(MessageTypeEnum.DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);

        parseEvent(displayMediaCollection);
    }
    
    /**
     * Parse the request to display a single media.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param displayMessageTutorRequest - contains reference to a single media item
     */
    public DisplayMediaCollectionEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, DisplayMessageTutorRequest displayMessageTutorRequest){
        super(MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(displayMessageTutorRequest);
    }
    
    /**
     * Parse the request to display some media item
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param displayMediaTutorRequest - contains reference to some media item, could also be Guidance course object structure
     */
    public DisplayMediaCollectionEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, DisplayMediaTutorRequest displayMediaTutorRequest){
        super(MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(displayMediaTutorRequest);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    /**
     * Place an identifier to the single media item in a column
     * 
     * @param displayMessageTutorRequest - contains reference to a single media item
     */
    private void parseEvent(DisplayMediaTutorRequest displayMediaTutorRequest){
        
        StringBuilder materialNames = new StringBuilder();
        
        // Handle the guidance item
        Guidance guidance = displayMediaTutorRequest.getGuidance();
        parseGuidance(guidance, materialNames);
        
        // Handle the media item
        Media media = displayMediaTutorRequest.getMedia();
        if(media != null){
            if(materialNames.length() > 0){
                materialNames.append(Constants.COMMA);
            }
            
            materialNames.append(media.getName());
        } // end handle Media item
        
        // Handle the media list
        List<Media> mediaList = displayMediaTutorRequest.getMediaList();
        parseMediaList(mediaList, materialNames);
        
        EventReportColumn materialNamesCol = new EventReportColumn(LESSON_MATERIAL, LESSON_MATERIAL);
        columns.add(materialNamesCol);
        cells.add(new Cell(materialNames.toString(), materialNamesCol));
    }
    
    /**
     * Parse the guidance object by extracting useful info and place that it in the string builder.
     * 
     * @param guidance contains guidance information that was displayed to the learner.  If null this
     * method does nothing.
     * @param materialNames where to place extracted info for display in a cell
     */
    private void parseGuidance(Guidance guidance, StringBuilder materialNames){
        
        if(guidance == null){
            return;
        }
        
        Serializable guidanceChoice = guidance.getGuidanceChoice();
        if(guidanceChoice instanceof Guidance.Message){
            Guidance.Message message = (Guidance.Message)guidanceChoice;
            String content = message.getContent();
            if(StringUtils.isNotBlank(content)){
                
                if(content.length() > 20){
                    materialNames.append(content.substring(0, 20)).append("...");
                }else{
                    materialNames.append(content);
                }
            }
        }else if(guidanceChoice instanceof Guidance.File){
            Guidance.File file = (Guidance.File)guidanceChoice;
            String html = file.getHTML();
            if(StringUtils.isNotBlank(html)){
                
                if(materialNames.length() > 0){
                    materialNames.append(Constants.COMMA);
                }
                
                materialNames.append(html);
            }
        }else if(guidanceChoice instanceof Guidance.URL){
            Guidance.URL url = (Guidance.URL)guidanceChoice;
            String address = url.getAddress();
            if(StringUtils.isNotBlank(address)){
                
                if(materialNames.length() > 0){
                    materialNames.append(Constants.COMMA);
                }
                
                materialNames.append(address);
            }
        }
    }
    
    /**
     * Place an identifier to the single media item in a column
     * 
     * @param displayMessageTutorRequest - contains reference to a single media item
     */
    private void parseEvent(DisplayMessageTutorRequest displayMessageTutorRequest){
        
        String contentName = displayMessageTutorRequest.getTitle();
        if(contentName == null){
            //grab the content reference to display in the cell
            
            if(displayMessageTutorRequest.getMessage() != null){
                contentName = displayMessageTutorRequest.getMessage();
            }
            
            StringBuilder buffer = new StringBuilder();
            parseGuidance(displayMessageTutorRequest.getGuidance(), buffer);
            contentName = buffer.toString();

        }
        
        EventReportColumn materialNamesCol = new EventReportColumn(LESSON_MATERIAL, LESSON_MATERIAL);
        columns.add(materialNamesCol);
        cells.add(new Cell(contentName, materialNamesCol));
    }
    
    /**
     * Parse the media list provided and add important info to the string builder.
     * 
     * @param mediaList if null this method does nothing.  Contains zero or more media items to extract
     * relevant info to place in the string builder for a cell.
     * @param materialNames where to place extracted info from the media items for a cell.
     */
    private void parseMediaList(List<Media> mediaList, StringBuilder materialNames){
        
        if(mediaList == null){
            return;
        }
        
        for(Media media : mediaList){
            
            if(materialNames.length() > 0){
                materialNames.append(Constants.COMMA);
            }
            
            materialNames.append(media.getName());
        }
    }
    
    /**
     * Place all identifiers to the media in a comma delimited list in a single column.
     * 
     * @param displayMediaCollection - contains references to the media collection
     */
    private void parseEvent(DisplayMediaCollectionRequest displayMediaCollection){
        
        LessonMaterial lessonMaterial = displayMediaCollection.getLessonMaterial();
        StringBuilder materialNames = new StringBuilder();
        if(lessonMaterial != null && lessonMaterial.getLessonMaterialList() != null && lessonMaterial.getLessonMaterialList().getMedia() != null){
            
            parseMediaList(lessonMaterial.getLessonMaterialList().getMedia(), materialNames);
        }
        
        EventReportColumn materialNamesCol = new EventReportColumn(LESSON_MATERIAL, LESSON_MATERIAL);
        columns.add(materialNamesCol);
        cells.add(new Cell(materialNames.toString(), materialNamesCol));
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CourseStateEvent: ");
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
