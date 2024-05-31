/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.metadata;

import java.util.Map;

import mil.arl.gift.common.io.FileProxy;

/**
 * Contains the result of a metadata file search for a single adaptive courseflow phase (e.g. Rule)
 * 
 * @author mhoffman
 *
 */
public class MetadataFileSearchResult {

    /**
     * mapping of file to the in-memory generated object representation of the metadata file
     */
    private Map<FileProxy, generated.metadata.Metadata> foundMetadataMap;
    
    /**
     * Set map
     * 
     * @param foundMetadataMap mapping of file to the in-memory generated object representation of the metadata file. Can't be null, can be empty.
     */
    public MetadataFileSearchResult(Map<FileProxy, generated.metadata.Metadata> foundMetadataMap){
        
        if(foundMetadataMap == null){
            throw new IllegalArgumentException("The map can't be null");
        }
        this.foundMetadataMap = foundMetadataMap;
    }
    
    /**
     * Return the mapping of file to the in-memory generated object representation of the metadata file
     * 
     * @return can't be null but can be empty
     */
    public Map<FileProxy, generated.metadata.Metadata> getMetadataFilesMap(){
        return foundMetadataMap;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[MetadataFileSearchResult: ");
        sb.append(" file = {\n");
        if(foundMetadataMap != null){
            
            for(FileProxy file : foundMetadataMap.keySet()){
                sb.append(file.getName()).append(",\n");
            }
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }
}
