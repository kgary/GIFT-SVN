/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.AbstractPerformanceState;

public 
/**
 * An inteface used to interact with objects that are capable of storing performance node data so that it
 * can be accessed from different views
 * 
 * @author nroberts
 */interface PerformanceNodeDataProvider<T extends AbstractPerformanceState> {

    /**
     * Gets the knowledge session that the performance node is a part of 
     * 
     * @return the knowledge session. Cannot be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession();
    
    /**
     * Gets performance node state data currently stored by this provider
     * 
     * @return the current performance node state data. Can be null.
     */
    public T getCurrentState();
}
