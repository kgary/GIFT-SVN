/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.common.metadata.MetadataSearchResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A response to a {@link GetMerrillQuadrantFiles} action containing a mapping from each Metadata file to its associated content file
 * 
 * @author nroberts
 */
public class GetMerrillQuadrantFilesResult extends GatServiceResult {
    
    /** 
     * The metadata search results for a request that contains the mapping from each 
     * metadata file name to an object containing information about the metadata content
     */   
    private MetadataSearchResult searchResult;
		
	/**
	 * Class constructor
	 * For serialization only.
	 */
	public GetMerrillQuadrantFilesResult() {
    }
    
	/**
	 * Return the metadata search results for a request.
	 * 
	 * @return 
	 */
    public MetadataSearchResult getSearchResult() {
        return searchResult;
    }

    /**
     * Set the metadata search results for a request.
     * 
     * @param searchResult
     */
    public void setSearchResult(MetadataSearchResult searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[GetMerrillQuadrantFilesResult: ");
        builder.append(searchResult);
        builder.append("]");
        return builder.toString();
    }
}
