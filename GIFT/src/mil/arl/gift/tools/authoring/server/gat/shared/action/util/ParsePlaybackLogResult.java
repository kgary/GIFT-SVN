/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.List;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of the {@link ParsePlaybackLog} action.
 * 
 * @author sharrison
 */
public class ParsePlaybackLogResult extends GatServiceResult {

    /** The list of parsed log metadatas */
    private List<LogMetadata> parsedMetadatas;

    /**
     * Constructor for GWT serialization
     */
    private ParsePlaybackLogResult() {
        super();
    }

    /**
     * Instantiates a new successful playback log result.
     *
     * @param parsedMetadatas the parsed log metadatas.
     */
    public ParsePlaybackLogResult(List<LogMetadata> parsedMetadatas) {
        this();
        this.parsedMetadatas = parsedMetadatas;
    }

    /**
     * Instantiates a new failure playback log result.
     * 
     * @param errorMsg the reason for the failure.
     */
    public ParsePlaybackLogResult(String errorMsg) {
        this();
        setSuccess(false);
        setErrorMsg(errorMsg);
    }

    /**
     * Gets the parsed log metadatas.
     *
     * @return the parsed log metadatas.
     */
    public List<LogMetadata> getParsedMetadatas() {
        return parsedMetadatas;
    }
}
