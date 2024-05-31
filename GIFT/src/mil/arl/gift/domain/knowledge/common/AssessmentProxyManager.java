/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.HashMap;
import java.util.Map;


/**
 * This class is used to manage the Assessment Proxy instances across all domain sessions for
 * the domain module.  Each Assessment proxy is used to manage the assessment values for the
 * performance assessment nodes in a domain session.
 * 
 * @author mhoffman
 *
 */
public class AssessmentProxyManager {
    
    /** 
     * map of assessment node to the proxy that manages it 
     * - this allows for multiple users/course simultaneously in the same domain module 
     */
    private Map<AbstractPerformanceAssessmentNode, AssessmentProxy> nodeToProxy = new HashMap<>();

    /** singleton instance of this class */
    private static AssessmentProxyManager instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return AssessmentProxyManager
     */
    public static AssessmentProxyManager getInstance(){
        
        if(instance == null){
            instance = new AssessmentProxyManager();
        }
        
        return instance;
    }
    
    private AssessmentProxyManager() {}
    
    /**
     * Return the Assessment Proxy registered for the specified Performance Assessment 
     * Node (i.e. task, intermediate concept or concept).
     * 
     * @param node the performance assessment node to get the registered proxy for
     * @return AssessmentProxy contains assessment values for all assessment nodes in the same domain session
     * as the node was created for.  Can be null if {@link #registerNode(AbstractPerformanceAssessmentNode, AssessmentProxy)}
     * has not been called OR {@link #unregisterNode(AbstractPerformanceAssessmentNode)} was called.
     */
    public AssessmentProxy getAssessmentProxy(AbstractPerformanceAssessmentNode node){
        return nodeToProxy.get(node);
    }
    
    /**
     * Register the performance assessment node (i.e. task, intermediate concept or concept) with the specified
     * Assessment Proxy instance.  This way the node can request the proxy for the it's domain session.
     * 
     * @param node the performance assessment node to register the proxy with
     * @param proxy contains assessment values for all assessment nodes in the same domain session
     * as the node was created for.
     * @throws Exception if the node is already registered.
     */
    public void registerNode(AbstractPerformanceAssessmentNode node, AssessmentProxy proxy) throws Exception{
        
        if(!nodeToProxy.containsKey(node)){
            nodeToProxy.put(node, proxy);
        }else{
            throw new Exception("The node of "+node+" is already registered with the proxy of "+proxy);
        }
    }
    
    /**
     * Unregister the performance assessment node with the proxy it was associated with.
     * This is useful for cleaning up when a node is no longer of importance.
     * 
     * @param node the performance assessment node to unregister any proxy with
     * @return AssessmentProxy the proxy the node is disassociating with.  Can be null.
     */
    public AssessmentProxy unregisterNode(AbstractPerformanceAssessmentNode node){
        return nodeToProxy.remove(node);
    }
}
