/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a domain module's status.
 * 
 * @author mhoffman
 *
 */
public class DomainModuleStatus extends ModuleStatus {	
    
    /** 
     * The topics that the Domain module is currently using to play back messages to itself
     * using domain session log files
     */
    private Set<String> logPlaybackTopics;

    /**
     * Creates a new domain module status from a base module status with the given log playback topics
     * 
     * @param status the base status from which to create the Domain module status. Cannot be null.
     * @param logPlaybackTopics the topics that the Domain module is currently using for 
     * log playback. Can be null.
     */
    public DomainModuleStatus(ModuleStatus status, Set<String> logPlaybackTopics){
        super(status.getModuleName(), status.getQueueName(), status.getModuleType());

        this.logPlaybackTopics = Collections.synchronizedSet(logPlaybackTopics != null ? logPlaybackTopics : new HashSet<>());
    }
    
    /**
     * Creates a new domain module status from a base module status
     * 
     * @param status the base status from which to create the Domain module status. Cannot be null.
     */
    public DomainModuleStatus(ModuleStatus status){
        this(status, null);
    }

    /**
     * Gets the topics that the Domain module is currently using to play back messages to itself
     * using a domain session log
     * 
     * @return the topics being used for log playback. Will not be null.
     */
    public Set<String> getLogPlaybackTopics() {
        return logPlaybackTopics;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DomainModuleStatus: ");
        sb.append(" logPlaybackTopics = ").append(getLogPlaybackTopics());
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
