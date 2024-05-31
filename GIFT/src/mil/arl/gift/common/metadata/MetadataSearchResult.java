/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;

/**
 * Contains the search results for a metadata search of a course folder.
 * 
 * @author mhoffman
 *
 */
public class MetadataSearchResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Mapping of adaptive courseflow phase (e.g. Rule) to the metadata search results
     * Can be empty if no request criteria was specified when making the request.
     */
    private Map<MerrillQuadrantEnum, QuadrantResultSet> phaseToResults = new HashMap<MerrillQuadrantEnum, QuadrantResultSet>();
        
    public MetadataSearchResult(){ }
    
    /**
     * Add the metadata search results for a specific adaptive courseflow phase (e.g. Rule).
     * 
     * @param quadrant the adaptive courseflow phase to add a search result for.  If a search result
     * already exist, the pre-existing result will be replaced by the provided result.  Can't be null.
     * @param resultSet contains the metadata search results.  Can't be null.
     */
    public void add(MerrillQuadrantEnum quadrant, QuadrantResultSet resultSet){
        
        if(quadrant == null){
            throw new IllegalArgumentException("The quadrant can't be null");
        }else if(resultSet == null){
            throw new IllegalArgumentException("The result set can't be null");
        }
        
        phaseToResults.put(quadrant, resultSet);
    }
    
    /**
     * Return the metadata search results for the specified adaptive courseflow phase (e.g. Rule)
     * 
     * @param quadrant the adaptive courseflow phase to get the search results for.  
     * @return the metadata search results for that phase.  If the phase wasn't
     * included in the search request this will return null.  If the phase is null, null will be returned.
     */
    public QuadrantResultSet getResultsForQuadrant(MerrillQuadrantEnum quadrant){
        
        if(quadrant == null){
            return null;
        }
        
        return phaseToResults.get(quadrant);
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[MetadataSearchResult: ");
        sb.append("results = {\n");
        for(MerrillQuadrantEnum quadrant : phaseToResults.keySet()){
            sb.append(quadrant).append(" : ").append(phaseToResults.get(quadrant)).append(",\n");
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * This inner class contains the metadata files that matched the adaptive courseflow phase (e.g. Rule)
     * search criteria.
     * 
     * @author mhoffman
     *
     */
    public static class QuadrantResultSet implements Serializable{
        
        private static final long serialVersionUID = 1L;
        
        /**
         * mapping of metadata file to the information
         * used on the client to display the search result to the user.  
         * key: workspace folder relative path to a course folder metadata file
         * value: An object containing information about the metadata contents
         */
        private Map<String, MetadataWrapper> metadataWorkspacePathToMetadata;
        
        /**
         * Required for GWT serialization. Don't use.
         */
        public QuadrantResultSet(){}
        
        /**
         * Set the map.
         * 
         * @param metadataWorkspacePathToMetadata - mapping of metadata file to the information
         * used on the client to display the search result to the user.  
         * key: workspace folder relative path to a course folder metadata file
         * value: An object containing information about the metadata contents
         */
        public QuadrantResultSet(Map<String, MetadataWrapper> metadataWorkspacePathToMetadata){
            
            this.metadataWorkspacePathToMetadata = metadataWorkspacePathToMetadata;
        }
        
        /**
         * mapping of metadata file to the information
         * used on the client to display the search result to the user.  
         * 
         * @return can be null or empty.
         * key: workspace folder relative path to a course folder metadata file
         * value: An object containing information about the metadata contents
         */
        public Map<String, MetadataWrapper> getMetadataRefs(){
            return metadataWorkspacePathToMetadata;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[QuadrantResultSet: ");
            sb.append("metadata = {\n");
            if(metadataWorkspacePathToMetadata != null){
                for(String path : metadataWorkspacePathToMetadata.keySet()){
                    MetadataWrapper wrapper = metadataWorkspacePathToMetadata.get(path);
                    sb.append(wrapper.getDisplayName()).append(",\n");
                }
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }

}
