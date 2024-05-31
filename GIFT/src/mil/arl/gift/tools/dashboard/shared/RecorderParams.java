/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.aar.VideoMetadata;
import mil.arl.gift.common.util.StringUtils;

/**
 * A set of parameters understood by the servlet GIFT uses to handle media recordings. These parameters
 * are used to determine where to save the recording file on the server.
 * 
 * @author nroberts
 */
public class RecorderParams {

    private static final String USER_ID = "userId";
    private static final String DOMAIN_SESSION_ID = "domainSessionId";
    private static final String EXPERIMENT_ID = "experimentId";
    
    /** param name for the delete operation that has been requested by the client */
    private static final String DELETE = "delete";
    
    /** param name for the metadata file associated with the {@link #videoMetadata} */
    private static final String METADATA_FILE = "metadataFile";
    
    /** The ID of the user that a recording is associated with */
    private Integer userId;
    
    /** The ID of the domain session that a recording is associated with */
    private Integer domainSessionId;
    
    /** The ID of the experiment that a recording is associated with */
    private String experimentId;

    /** The metadata for a video file */
    private VideoMetadata videoMetadata;
    
    /** whether the client is requesting the video be deleted on the server */
    private boolean performDelete = false;

    /**
     * Creates an empty set of recorder servlet parameters
     */
    public RecorderParams() {}
    
    /**
     * Gets the ID of the user that a recording is associated with
     * 
     * @return the user ID. Can be null.
     */
    public Integer getUserId() {
        return userId;
    }
    
    /**
     * Sets the ID of the user that a recording is associated with
     * 
     * @param userId the user ID. Can be null.
     * @return this set of parameters. Can be used to chain setter calls.
     */
    public RecorderParams setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the ID of the domain session that a recording is associated with
     * 
     * @return the domain session ID. Can be null.
     */
    public Integer getDomainSessionId() {
        return domainSessionId;
    }

    /**
     * Sets the ID of the domain session that a recording is associated with
     * 
     * @param domainSessionId the domain session ID. Can be null.
     * @return this set of parameters. Can be used to chain setter calls.
     */
    public RecorderParams setDomainSessionId(Integer domainSessionId) {
        this.domainSessionId = domainSessionId;
        return this;
    }

    /** 
     * Gets the ID of the experiment that a recording is associated with
     * 
     * @return the experiment ID. Can be null.
     */
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * Sets the ID of the experiment that a recording is associated with
     * 
     * @param experimentId the experiment ID. Can be null.
     * @return this set of parameters. Can be used to chain setter calls.
     */
    public RecorderParams setExperimentId(String experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    /**
     * Gets the metadata for a video file.
     * 
     * @return the video metadata. Can be null.
     */
    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    /**
     * Sets the metadata for a video file.
     * 
     * @param videoMetadata the video metadata. Can be null.
     * @return this set of parameters. Can be used to chain setter calls.
     */
    public RecorderParams setVideoMetadata(VideoMetadata videoMetadata) {
        this.videoMetadata = videoMetadata;
        return this;
    }

    /**
     * Encodes the given set of recorder servlet parameters to a URL query string
     * 
     * @param params the parameters to encode. If null, null will be returned.
     * @return a URL query string containing the encoded parameters. Can be null.
     */
    public static String encodeToQuery(RecorderParams params) {
        
        if(params == null) {
            return null;
        }
        
        Map<String, String> paramToValue = new HashMap<>();
        
        if(params.getUserId() != null) {
            paramToValue.put(USER_ID, Integer.toString(params.getUserId()));
        }
        
        if(params.getDomainSessionId() != null) {
            paramToValue.put(DOMAIN_SESSION_ID, Integer.toString(params.getDomainSessionId()));
        }
        
        if(params.getExperimentId() != null) {
            paramToValue.put(EXPERIMENT_ID, params.getExperimentId());
        }
        
        if(params.isPerformDelete()){
            paramToValue.put(DELETE, Boolean.valueOf(params.isPerformDelete()).toString());
        }

        if (params.getVideoMetadata() != null) {
            params.getVideoMetadata().getProperties(paramToValue);
            
            if(StringUtils.isNotBlank(params.getVideoMetadata().getMetadataFile())){
                paramToValue.put(METADATA_FILE, params.getVideoMetadata().getMetadataFile());
            }
        }

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> param : paramToValue.entrySet()) {
            
            if(first) {
                first = false;
                
            } else {
                sb.append("&");
            }
            
            sb.append(param.getKey()).append("=").append(param.getValue());
        }
        
        return sb.toString();
    }
    
    /**
     * Decodes the given URL query string to a set of recorder servlet parameters
     * 
     * @param query the URL query string to decode. If null, null will be returned.
     * @return a set of parameters decoded from the query string. Can be null.
     */
    public static RecorderParams decodeFromQuery(String query) {
        
        if(query == null) {
            return null;
        }
        
        RecorderParams params = new RecorderParams();
        
        String[] paramStrings = query.split("&");
        String metadataFile = null;
        
        final Map<String, String> unknownParams = new HashMap<String, String>();
        for(String paramString : paramStrings) {
            
            String[] pair = paramString.split("=");
            if(pair.length == 2) {
                
                switch(pair[0]) {
                
                    case USER_ID:
                        params.setUserId(Integer.valueOf(pair[1]));
                        break;
                    
                    case DOMAIN_SESSION_ID:
                        params.setDomainSessionId(Integer.valueOf(pair[1]));
                        break;
                        
                    case EXPERIMENT_ID:
                        params.setExperimentId(pair[1]);
                        break;
                        
                    case DELETE:
                        params.setPerformDelete(true);
                        break;
                        
                    case METADATA_FILE:
                        metadataFile = pair[1];
                        break;
                        
                    default:
                        unknownParams.put(pair[0],  pair[1]);
                        break;
                }
            }
        }

        VideoMetadata videoMetadata = VideoMetadata.generateMetadataFromProperties(unknownParams);
        if (videoMetadata != null) {
            
            if(StringUtils.isNotBlank(metadataFile)){
                videoMetadata.setMetadataFile(metadataFile);
            }
            params.setVideoMetadata(videoMetadata);
        }

        return params;
    }

    /**
     * Return whether the client is request that the server delete the metadata files 
     * @return true if the client is requesting a delete
     */
    public boolean isPerformDelete() {
        return performDelete;
    }

    /**
     * Set whether the client is request that the server delete the metadata files 
     * @param performDelete default is false
     */
    public void setPerformDelete(boolean performDelete) {
        this.performDelete = performDelete;
    }    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[RecorderParams: userId=");
        builder.append(userId);
        builder.append(", domainSessionId=");
        builder.append(domainSessionId);
        builder.append(", experimentId=");
        builder.append(experimentId);
        builder.append(", videoMetadata=");
        builder.append(videoMetadata);
        builder.append(", performDelete=");
        builder.append(performDelete);
        builder.append("]");
        return builder.toString();
    }
}
