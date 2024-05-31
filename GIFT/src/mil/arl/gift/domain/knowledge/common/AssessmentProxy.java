/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.IntermediateConceptAssessment;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.TaskAssessment;

/**
 * The purpose of this class is to manage the accessing of assessments of accessible nodes 
 * (i.e. tasks, concepts, conditions) for a domain session (i.e. course).  
 * There can be several threads writing new assessments while other threads read (iterate) over those assessments
 * to build performance assessment messages or display strings.
 *  
 * @author mhoffman
 *
 */
public class AssessmentProxy {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AssessmentProxy.class);

    /** map of accessible node's (course level) unique id to it's current known assessment */
    private Map<UUID, AbstractAssessment> assessments = Collections.synchronizedMap(new HashMap<UUID, AbstractAssessment>());
    
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();
    
    /**
     * Default constructor
     */
    public AssessmentProxy(){
        
    }
    
    /**
     * Generate a snap-shot of the current assessment information known to this class for
     * the specified proxy performance nodes by creating a new Performance Assessment instance 
     * with new assessment objects.
     * 
     * Note: this method uses Read/Write logic to prevent a write from happening while a read is occurring 
     * and not blocking subsequent reads from happening.  Because of this care should be taken to prevent
     * generating perf. assessments to often.  This care is currently implemented by only sending perf. assessments
     * when a change happens.  Furthermore, only a single perf. assessment is generated for all the changes caused
     * by a single game state message w/in the direct handling of that message.
     * 
     * @param assessment contains the proxy information (i.e. UUIDs) for the root task nodes that need to be
     * included in the resulting performance assessment instance.
     * @return PerformanceAssessment new instance containing the performance assessment values for the task/concept hierarchy
     * defined by this proxy.
     */
    public PerformanceAssessment generatePerformanceAssessment(ProxyPerformanceAssessment assessment){
        
        try{
        
            //don't allow updates to assessments while reading
        	if(logger.isInfoEnabled()){
        	    logger.info("Generating Performance Assessment: Waiting for lock at "+System.currentTimeMillis()+".");
        	}
            rwlock.readLock().lock();
            if(logger.isInfoEnabled()){
                logger.info("Generating Performance Assessment: Lock acquired at "+System.currentTimeMillis()+".");
            }
            
            PerformanceAssessment newAssessment = new PerformanceAssessment();
                
            for(UUID taskUUID : assessment.getTasks()){
                
                AbstractAssessment taskAssessment = assessments.get(taskUUID);

                if(taskAssessment instanceof ProxyTaskAssessment){
                    
                    List<ConceptAssessment> conceptAssessments = new ArrayList<>();
                    for(UUID conceptUUID : ((ProxyTaskAssessment)taskAssessment).getConcepts()){
                        
                        AbstractAssessment conceptAssessment = assessments.get(conceptUUID);
                        ConceptAssessment newConceptAssessment = generateConceptAssessment(conceptAssessment);
                        conceptAssessments.add(newConceptAssessment);
                    }
                    
                    TaskAssessment newTaskAssessment = 
                            new TaskAssessment(taskAssessment.getName(), taskAssessment.getAssessmentLevel(), 
                                    taskAssessment.getTime(), conceptAssessments, 
                                    taskAssessment.getNodeId(), taskAssessment.getCourseNodeId());
                    newTaskAssessment.updateConfidence(taskAssessment.getConfidence(), true);
                    newTaskAssessment.updateCompetence(taskAssessment.getCompetence(), true);
                    newTaskAssessment.updatePriority(taskAssessment.getPriority(), true);
                    newTaskAssessment.updateTrend(taskAssessment.getTrend(), true);
                    
                    newTaskAssessment.setDifficulty(((ProxyTaskAssessment)taskAssessment).getDifficulty());
                    newTaskAssessment.setDifficultyReason(((ProxyTaskAssessment)taskAssessment).getDifficultyReason());
                    newTaskAssessment.setStress(((ProxyTaskAssessment)taskAssessment).getStress());
                    newTaskAssessment.setStressReason(((ProxyTaskAssessment)taskAssessment).getStressReason());

                    newTaskAssessment.setAssessmentHold(taskAssessment.isAssessmentHold());
                    newTaskAssessment.setConfidenceHold(taskAssessment.isConfidenceHold());
                    newTaskAssessment.setCompetenceHold(taskAssessment.isCompetenceHold());
                    newTaskAssessment.setPriorityHold(taskAssessment.isPriorityHold());
                    newTaskAssessment.setTrendHold(taskAssessment.isTrendHold());

                    newTaskAssessment.setEvaluator(taskAssessment.getEvaluator());
                    newTaskAssessment.setObserverComment(taskAssessment.getObserverComment());
                    newTaskAssessment.setObserverMedia(taskAssessment.getObserverMedia());
                    newTaskAssessment.setAuthoritativeResource(taskAssessment.getAuthoritativeResource());
                    newTaskAssessment.setNodeStateEnum(taskAssessment.getNodeStateEnum());
                    newTaskAssessment.setAssessmentExplanation(taskAssessment.getAssessmentExplanation());
                    newTaskAssessment.setScenarioSupportNode(taskAssessment.isScenarioSupportNode());
                    
                    newAssessment.updateTaskAssessment(newTaskAssessment);
                    
                }else{
                    newAssessment.updateTaskAssessment((TaskAssessment) taskAssessment);
                }
            }
            
            newAssessment.setEvaluator(assessment.getEvaluator());
            newAssessment.setObserverComment(assessment.getObserverComment());
            newAssessment.setObserverMedia(assessment.getObserverMedia());
            
            if(logger.isInfoEnabled()){
                logger.info("Generating Performance Assessment: finished at "+System.currentTimeMillis()+".");
            }
            return newAssessment;
        }finally{
            rwlock.readLock().unlock();
        }
        
    }
    
    /**
     * Generate a snap-shot of the current assessment information known to this class for
     * the specified concept assessment (proxy or not) by creating a new Concept Assessment instance 
     * with new assessment objects.
     *
     * 
     * @param conceptAssessment contains the concept assessment for a concept node that need to be
     * included in the resulting concept assessment instance.
     * @return ConceptAssessment new instance containing the concept assessment values for the concept 
     * defined by this proxy.
     */
    public ConceptAssessment generateConceptAssessment(AbstractAssessment conceptAssessment){
        
        if(conceptAssessment instanceof ProxyIntermediateConceptAssessment){
            //an intermediate concept that has subconcepts
            
            //build each subconcept's concept assessment
            List<ConceptAssessment> conceptAssessments = new ArrayList<>();
            for(UUID conceptUUID : ((ProxyIntermediateConceptAssessment)conceptAssessment).getConceptIds()){
            
                AbstractAssessment subConceptAssessment = assessments.get(conceptUUID);
                ConceptAssessment newAss = generateConceptAssessment(subConceptAssessment);
                conceptAssessments.add(newAss);
            }
            
            IntermediateConceptAssessment iConceptAssessment = 
                    new IntermediateConceptAssessment(conceptAssessment.getName(), 
                            conceptAssessment.getAssessmentLevel(), conceptAssessment.getTime(), 
                            conceptAssessment.getNodeId(), conceptAssessments, conceptAssessment.getCourseNodeId());
            iConceptAssessment.setNodeStateEnum(conceptAssessment.getNodeStateEnum());
            iConceptAssessment.setEvaluator(conceptAssessment.getEvaluator());
            iConceptAssessment.setScenarioSupportNode(conceptAssessment.isScenarioSupportNode());
            iConceptAssessment.setAuthoritativeResource(conceptAssessment.getAuthoritativeResource());
            
            return iConceptAssessment;
            
        }else if(conceptAssessment instanceof ConceptAssessment){
            return (ConceptAssessment) conceptAssessment;
        }else{
            //ERROR
            throw new IllegalArgumentException("Found unhandled concept assessment of "+conceptAssessment+".");
        }
    }

    /**
     * Update the assessment for the given identifier.  
     * 
     * @param assessmentUpdate a new assessment value
     */
    public void fireAssessmentUpdate(AbstractAssessment assessmentUpdate){
        
        UUID courseNodeId = assessmentUpdate.getCourseNodeId();
         
        if(logger.isDebugEnabled()){
            logger.debug("Updating assessment of course node id "+courseNodeId+" from "+assessments.get(courseNodeId)+" to "+assessmentUpdate);
        }
        
        try{
            //prevent reads while writing
            rwlock.writeLock().lock();
            
            synchronized(assessments){
                //prevent ???
                assessments.put(courseNodeId, assessmentUpdate);
            }
            
        }finally{
            rwlock.writeLock().unlock();
        }

    }
    
    /**
     * Return the assessment mapped to the given id.  
     * 
     * @param id - course level performance node id
     * @return AbstractAssessment - assessment for that node
     */
    public AbstractAssessment get(UUID id){        
                   
        AbstractAssessment ass = assessments.get(id);
        
        if(ass == null){
            throw new IllegalArgumentException("There is not an assessment for id "+id);
        }
        
        return ass;        
    }
    
    /**
     * Return the next node id that is available.  Node ids are unique within an activity
     * that can cause performance assessments (e.g. dkf, conversation tree, autotutor conversation).
     * If two performance assessment nodes have the same id it can result in performance assessment
     * messages missing information.
     * 
     * @param assessment the top level performance assessment to walk through and find the highest
     * node id within.
     * @return the highest node id plus 1.
     */
    public int getNextNodeId(ProxyPerformanceAssessment assessment){
        
        // this will contains the highest node id found
        int nextNodeId = 0;
        
        for(UUID taskUUID : assessment.getTasks()){
         
            AbstractAssessment taskAssessment = assessments.get(taskUUID);
            if(taskAssessment.getNodeId() > nextNodeId){
                nextNodeId = taskAssessment.getNodeId();
            }
            
            if(taskAssessment instanceof ProxyTaskAssessment){
                
                for(UUID conceptUUID : ((ProxyTaskAssessment)taskAssessment).getConcepts()){
                    AbstractAssessment conceptAssessment = assessments.get(conceptUUID);
                    if(conceptAssessment.getNodeId() > nextNodeId){
                        nextNodeId = taskAssessment.getNodeId();
                    }
                }
            }
        }
        
        // use the next integer value as this value is already being used
        return nextNodeId++;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AssessmentProxy : ");
        sb.append(" assessment-Map = {");
        for(UUID id : assessments.keySet()){
            sb.append("(").append(id).append(":").append(assessments.get(id)).append("), ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
