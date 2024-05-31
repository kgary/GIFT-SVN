/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.util.StringUtils;

/**
 * A GWT compatible class that can be used by both client and server logic. It
 * contains the various fields that are used to control video media.
 * 
 * @author sharrison
 */
public class VideoMetadata implements Serializable {

    /** The version of this class used by the serialization logic. */
    private static final long serialVersionUID = 1L;

    /**
     * The fields found within the video metadata.
     * 
     * @author sharrison
     */
    public enum VideoMetadataField {
        /** The location field key */
        LOCATION("location"),
        /** The start time field key */
        START_TIME("start_time"),
        /** The offset field key */
        OFFSET("offset"),
        /** The title field key */
        TITLE("title"),
        /** The linked task/concept name field key */
        TASK_CONCEPT_NAME("task_concept_name"),
        /** The file path of a room dimension file */
        SPACE_METADATA_FILE("space_metadata_file"),
        /** video source field key */
        VIDEO_SOURCE("video_source");

        /** The tag for the video metadata field */
        private final String tag;

        /** The fields that belong to the LOM technical type */
        public static VideoMetadataField[] technicalTypeFields = new VideoMetadataField[] { LOCATION };

        /** The fields that belong to the LOM identifier type */
        public static VideoMetadataField[] identifierTypeFields = new VideoMetadataField[] { START_TIME, OFFSET, TITLE,
                TASK_CONCEPT_NAME, SPACE_METADATA_FILE, VIDEO_SOURCE };

        /**
         * Constructor
         * 
         * @param tag the tag for the video metadata field
         */
        private VideoMetadataField(String tag) {
            this.tag = tag;
        }

        /**
         * Get the tag for the video metadata field.
         * 
         * @return The metadata field tag.
         */
        public String getTag() {
            return tag;
        }

        /**
         * Get the metadata field by it's tag.
         * 
         * @param tag the tag to find.
         * @return the metadata field with the same tag. Can return null if the
         *         tag is not found.
         */
        public static VideoMetadataField getByTag(String tag) {
            if (StringUtils.isNotBlank(tag)) {
                for (VideoMetadataField metaField : VideoMetadataField.values()) {
                    if (StringUtils.equalsIgnoreCase(metaField.getTag(), tag)) {
                        return metaField;
                    }
                }
            }

            return null;
        }
    }

    /** The location of the video file (e.g. STEELR-DemoOption1_Novice Playback\KAdams_2020_10_28_11_15_43_415_converted.mp4) */
    private String location;

    /** The start time of the video */
    private Date startTime;

    /** The offset (in seconds) of the video */
    private Long offset;

    /** The custom title for the video */
    private String title;

    /** The name of the task/concept that is associated with this video */
    private String taskConceptName;
    
    /** The file path of the space metadata file that is associated with this video, 
     * relative to the video metadata file's parent folder 
     * A space metadata file contains information about the environment (e.g. room) the video
     * is recording (e.g. room dimensions)
     */
    private String spaceMetadataFile;
    
    /**
     * the file path to the metadata file that populated this class.
     * Can be null if this metadata file hasn't been created yet.
     * (e.g. STEELR-DemoOption1_Novice Playback\2021-03-07 18-31-34.vmeta.xml)
     */
    private String metadataFile;
    
    /**
     * the source of the video referenced by this metadata (e.g. VBS, USBCamera)
     */
    private String videoSource;

    /**
     * Default constructor. Needed for GWT serialization.
     */
    private VideoMetadata() {
    }
    
    /**
     * Constructor for when the metadata comes from new creation, not LOM xml file.
     * 
     * @param location the location of the video file. Can't be null. E.g. STEELR-DemoOption1_Novice Playback\KAdams_2020_10_28_11_15_43_415_converted.mp4
     * @param startTime the start time of the video. Can't be null.
     * @param offset the number of seconds to trim from the front of the video.
     *        (optional)
     * @param title the title of the video. (optional)
     * @param taskConceptName the associated task/concept for this metadata. (optional).
     */
    public VideoMetadata(String location, Date startTime, Long offset, String title, String taskConceptName) {
        this();
        if (StringUtils.isBlank(location)) {
            throw new IllegalArgumentException("The parameter 'location' cannot be null or empty.");
        } else if (startTime == null) {
            throw new IllegalArgumentException("The parameter 'startTime' cannot be null.");
        }

        this.location = location;
        this.startTime = startTime;
        this.offset = offset;
        this.title = title;
        this.taskConceptName = taskConceptName;
    }

    /**
     * Constructor for when the metadata comes from a LOM xml file, not a new metadata file.
     * 
     * @param metadataFile the location of the metadata file. Can't be null or empty.
     * @param location the location of the video file. Can't be null.
     * @param startTime the start time of the video. Can't be null.
     * @param offset the number of seconds to trim from the front of the video.
     *        (optional)
     * @param title the title of the video. (optional)
     * @param taskConceptName the associated task/concept for this metadata. (optional).
     */
    public VideoMetadata(String metadataFile, String location, Date startTime, Long offset, String title, String taskConceptName) {
        this(location, startTime, offset, title, taskConceptName);
        setMetadataFile(metadataFile);
    }    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metadataFile == null) ? 0 : metadataFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VideoMetadata)) {
            return false;
        }
        VideoMetadata other = (VideoMetadata) obj;
        if (metadataFile == null) {
            if (other.metadataFile != null) {
                return false;
            }
        } else if (!metadataFile.equals(other.metadataFile)) {
            return false;
        }
        return true;
    }

    /**
     * Get the location of the video file.
     * 
     * @return the location of the video file. Includes the unique domain
     *         session log folder name. Can't be null. E.g. 'Room Clearing
     *         Option 1 playback_domainSession268_uId4\first room_2020-10-28
     *         11-04-48_converted.mp4'
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * the file path to the metadata file that populated this class.
     * Can be null if this metadata file hasn't been created yet.
     * @return can be null (e.g. STEELR-DemoOption1_Novice Playback\2021-03-07 18-31-34.vmeta.xml)
     */
    public String getMetadataFile(){
        return metadataFile;
    }
    
    /**
     * the file path to the metadata file that populated this class.
     * @param metadataFile can't be null or empty. (e.g. STEELR-DemoOption1_Novice Playback\2021-03-07 18-31-34.vmeta.xml)
     */
    public void setMetadataFile(String metadataFile){
        if (StringUtils.isBlank(metadataFile)) {
            throw new IllegalArgumentException("The parameter 'metadataFile' cannot be null or empty.");
        }

        this.metadataFile = metadataFile;
    }

    /**
     * Get the file name from the video file location. Attempts to find the
     * filename by removing parent folders.
     * 
     * @return the file name of the video file without the parent folder. Can't
     *         be null. E.g. "first room_2020-10-28 11-04-48_converted.mp4"
     */
    public String getFileName() {
        return getFileFromPath(location);
    }
    
    /**
     * Get the file name for the metadata file.  Attempts to find the filename by removing
     * parent folders.
     * @return the file name of the metadata file without the parent folder. Can't
     *         be null. E.g. "first room_2020-10-28 11-04-48_converted.vmeta.xml"
     */
    public String getMetadataFileName(){
        return getFileFromPath(metadataFile);
    }
    
    /**
     * Get the file name for the space metadata file.  Attempts to find the filename by removing
     * parent folders.
     * @return the file name of the space metadata file without the parent folder. Can
     *         be null or empty. 
     */
    public String getSpaceMetadataFileName(){
        return getFileFromPath(spaceMetadataFile);
    }
    
    /**
     * Util method for removing parent folders from the file path provided
     * @param filePath should be a file and may or may not include ancestor folders.  If null or empty then
     * the filePath provided is returned.
     * @return the file name without the ancestor folders included.
     */
    private String getFileFromPath(String filePath){
        if(StringUtils.isBlank(filePath)){
            return filePath;
        }else if (filePath.contains("\\")) {
            return filePath.substring(filePath.lastIndexOf("\\") + 1);
        } else if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        } else {
            return filePath;
        }
    }

    /**
     * Get the start time of the video.
     * 
     * @return the start time of the video. Can't be null.
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Get the offset (in seconds) of the video.
     * 
     * @return the offset (in seconds) of the video. Will return 0 if null.
     */
    public long getOffset() {
        return offset == null ? 0 : offset;
    }

    /**
     * Gets the start time (in milliseconds) including the {@link #offset}.
     * 
     * @return the start time (in milliseconds) of the video with the offset.
     */
    public long getStartTimeWithOffset() {
        return startTime.getTime() + (getOffset() * 1000);
    }

    /**
     * Get the custom title of the video.
     * 
     * @return the title of the video. Can be null if never set.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the associated task/concept for this video.
     * 
     * @return the name of the task/concept associated with this video. Can be
     *         null if never set.
     */
    public String getTaskConceptName() {
        return taskConceptName;
    }
    
    /**
     * Get the file location of the space metadata file associated with this video.
     * A space metadata file contains information about the environment (e.g. room) the video
     * is recording (e.g. room dimensions)
     * 
     * @return the location of the space metadata file, relative to the parent folder
     *         of the video metadata file. If the space metadata file is stored in the  
     *         same location as the video metadata file, this will be only the filename.  Can be null.
     */
    public String getSpaceMetadataFile() {
        return spaceMetadataFile;
    }
    
    /**
     * Set the file location of the space metadata file associated with this video.
     * A space metadata file contains information about the environment (e.g. room) the video
     * is recording (e.g. room dimensions)
     * 
     * @param fileLocation the location of the space metadata file, relative to the parent folder
     *        of the video metadata file. If the space metadata file is stored in the same location 
     *        as the video metadata file, this will be only the filename.  Can be null.
     */
    public void setSpaceMetadataFile(String fileLocation) {
        spaceMetadataFile = fileLocation;
    }    
    
    /**
     * Get the video source of the video referenced by this metadata. (e.g. VBS, USBCamera)
     * 
     * @return the video source value.  Can be null.
     */
    public String getVideoSource() {
        return videoSource;
    }

    /**
     * Set the video source of the video referenced by this metadata.
     * 
     * @param videoSource the video source value.  Can be null. (e.g. VBS, USBCamera)
     */
    public void setVideoSource(String videoSource) {
        this.videoSource = videoSource;
    }

    /**
     * Populate the property map with the values from this video metadata.
     * 
     * @param propertyMap the map to populate with this metadata values.
     */
    public void getProperties(Map<String, String> propertyMap) {
        for (VideoMetadataField metaField : VideoMetadataField.values()) {
            switch (metaField) {
            case LOCATION:
                propertyMap.put(metaField.getTag(), getLocation());
                break;
            case START_TIME:
                propertyMap.put(metaField.getTag(), Long.toString(getStartTime().getTime()));
                break;
            case OFFSET:
                propertyMap.put(metaField.getTag(), Long.toString(getOffset()));
                break;
            case TITLE:
                if (StringUtils.isNotBlank(getTitle())) {
                    propertyMap.put(metaField.getTag(), getTitle());
                }
                break;
            case TASK_CONCEPT_NAME:
                if (StringUtils.isNotBlank(getTaskConceptName())) {
                    propertyMap.put(metaField.getTag(), getTaskConceptName());
                }
                break;
            case SPACE_METADATA_FILE:
                if (StringUtils.isNotBlank(getSpaceMetadataFile())) {
                    propertyMap.put(metaField.getTag(), getSpaceMetadataFile());
                }
                break;
            case VIDEO_SOURCE:
                if(StringUtils.isNotBlank(getVideoSource())){
                    propertyMap.put(metaField.getTag(), getVideoSource());
                }
                break;
             default:
                throw new IllegalArgumentException("Unhandled enum type '" + metaField + "'.");
            }
        }
    }

    /**
     * Builds the video metadata using the values in a property map.
     * 
     * @param propertyMap the property map of key/value pairs.
     * @return the video metadata if the property map contains values for at
     *         least {@link VideoMetadataField#LOCATION} and
     *         {@link VideoMetadataField#START_TIME}. Returns null otherwise.
     */
    public static VideoMetadata generateMetadataFromProperties(Map<String, String> propertyMap) {
        if (propertyMap == null) {
            return null;
        }

        String location = null, title = null, taskConceptName = null, spaceMetadataFile = null, videoSource = null;
        Date startTime = null;
        Long offset = null;

        for (Entry<String, String> entry : propertyMap.entrySet()) {
            final VideoMetadataField metaField = VideoMetadataField.getByTag(entry.getKey());
            final String value = entry.getValue();
            if (metaField == null || value == null) {
                continue;
            }

            switch (metaField) {
            case LOCATION:
                location = value;
                break;
            case START_TIME:
                try {
                    startTime = new Date(Long.parseLong(value));
                } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
                    /* Can't parse */
                }
                break;
            case OFFSET:
                try {
                    offset = Long.parseLong(value);
                } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
                    /* Can't parse */
                }
                break;
            case TITLE:
                title = value;
                break;
            case TASK_CONCEPT_NAME:
                taskConceptName = value;
                break;
            case SPACE_METADATA_FILE:
                spaceMetadataFile = value;
                break;
            case VIDEO_SOURCE:
                videoSource = value;
                break;
            default:
                throw new IllegalArgumentException("Unhandled enum type '" + metaField + "'.");
            }
        }

        if (location == null || startTime == null) {
            return null;
        }

        VideoMetadata newVideoMetadata = new VideoMetadata(location, startTime, offset, title, taskConceptName);
        newVideoMetadata.setSpaceMetadataFile(spaceMetadataFile);
        newVideoMetadata.setVideoSource(videoSource);
        return newVideoMetadata;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[VideoMetadata: ");
        sb.append("location = ").append(location);
        sb.append(", metadata = ").append(metadataFile);
        sb.append(", startTime = ").append(startTime);
        sb.append(", offset = ").append(offset);
        sb.append(", title = ").append(title);
        sb.append(", taskConceptName = ").append(taskConceptName);
        sb.append(", spaceMetadataFile = ").append(spaceMetadataFile);
        sb.append(", videoSource = ").append(videoSource);
        sb.append("]");
        return sb.toString();
    }
}
