/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.iframe.messages;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;

/**
 * The history item message is used by the tutor to signal to the dashboard to
 * change its history to the provided tag.
 * 
 * @author sharrison
 *
 */
public class HistoryItemMessage extends AbstractIFrameMessage {

    /** Message history item attribute name */
    private final String HISTORY_ITEM = "HISTORY_ITEM";

    /** History tag */
    private String historyItem = "";

    /**
     * Constructor
     */
    public HistoryItemMessage() {
        setMsgType(IFrameMessageType.ADD_HISTORY_ITEM);
    }

    /**
     * Constructor. Sets the message parameters as well as the type of message
     * for this class.
     * 
     * @param historyItem the history tag to signal the dashboard to add. Can't
     *        be null.
     */
    public HistoryItemMessage(String historyItem) {
        this();

        if (historyItem == null) {
            throw new IllegalArgumentException("The parameter 'historyItem' cannot be null.");
        }

        this.historyItem = historyItem;
    }

    /**
     * Retrieve the history tag
     * 
     * @return the history tag. Can't be null.
     */
    public String getHistoryItem() {
        return historyItem;
    }

    @Override
    public void encode(JSONObject obj) {

        obj.put(HISTORY_ITEM, new JSONString(getHistoryItem()));
    }

    @Override
    public void decode(JSONObject obj) {

        JSONValue valHistoryItem = obj.get(HISTORY_ITEM);

        if (valHistoryItem != null && valHistoryItem.isString() != null) {
            historyItem = valHistoryItem.isString().stringValue();
        } else {
            historyItem = "";
        }
    }
}
