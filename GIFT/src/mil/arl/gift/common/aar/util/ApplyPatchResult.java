/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import java.util.HashSet;
import java.util.Set;

/**
 * The results from applying a PerformanceStateAttribute patch into the log
 * messages
 * 
 * @author sharrison
 */
public class ApplyPatchResult {
    /** The message located at the patch injection timestamp */
    MessageManager currentMessage;

    /** The collection of all affected messages from the patch */
    final Set<MessageManager> affectedMessages = new HashSet<>();

    /** Empty constructor */
    public ApplyPatchResult() {
    }

    /**
     * Set the message located at the patch injection timestamp.
     * 
     * @param currentMessage the message at the patch timestamp.
     */
    public void setCurrentMessage(MessageManager currentMessage) {
        this.currentMessage = currentMessage;
    }

    /**
     * Retrieve the message located at the patch injection timestamp.
     * 
     * @return the message at the patch timestamp. Can be null.
     */
    public MessageManager getCurrentMessage() {
        return currentMessage;
    }

    /**
     * Retrieve the list of all affected messages from the patch.
     * 
     * @return the list of affected messages. Will never be null.
     */
    public Set<MessageManager> getAffectedMessages() {
        return affectedMessages;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ApplyPatchResult currentMessage = ");
        builder.append(currentMessage);
        builder.append(", affectedMessages = ");
        builder.append(affectedMessages);
        builder.append("]");
        return builder.toString();
    }
    
    
}
