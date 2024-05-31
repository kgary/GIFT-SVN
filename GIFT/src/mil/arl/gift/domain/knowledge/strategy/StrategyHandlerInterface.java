/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.strategy;

import java.lang.reflect.Field;

import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.DoNothingStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.ScenarioAdaptationStrategy;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;

/**
 * This interface must be implemented by Strategy Handlers that wish to receive pedagogical instructional strategy request to handle.
 * 
 * @author mhoffman
 *
 */
public interface StrategyHandlerInterface {
    
    //TODO: eventually may create strategy handler manager to manage strategy class instances, for now force singleton instance structure
    /**
     * This class is responsible for returning a singleton instance of a class that implements this interface.
     *  
     * @author mhoffman
     *
     */
    class Singleton {
        
        /**
         * Return the singleton instance of the class that implements the StrategyHandlerInterface interface.
         * Note: the implementing class must have a member field called "instance" which contains the instantiated interface object. 
         * 
         * @param modelClass - the strategy handler interface implementation class
         * @return StrategyHandlerInterface - the singleton interface implementation class instance
         * @throws SecurityException if reflection request is denied
         * @throws NoSuchFieldException if the "instance" field with is not found. 
         * @throws IllegalAccessException  if this "instance" Field object is enforcing Java language access control and the underlying field is inaccessible.
         * @throws IllegalArgumentException if the specified object is not an instance of the class or interface declaring the underlying "instance" field (or a subclass or implementor thereof).
         */
        public static StrategyHandlerInterface instance(Class<? extends StrategyHandlerInterface> modelClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
            Field instanceField = modelClass.getDeclaredField("instance");
            instanceField.setAccessible(true);
            return (StrategyHandlerInterface)instanceField.get(null);
        }
    }

   /**
    * Notification that the pedagogical model request a scenario adaptation strategy be acted upon.  It is up to the handler
    * to select and execute the appropriate strategy instance.
    * 
    * @param scenarioAdaptation - information about the strategy selected
    * @param strategyHandlerRequestInterface - the interface used by this strategy handler to make request on its behalf (e.g. change the fog level to high, change time of day to midnight in the training application) 
    */
    public void handleScenarioAdaptation(ScenarioAdaptationStrategy scenarioAdaptation, StrategyHandlerRequestInterface strategyHandlerRequestInterface);
    
    /**
    * Notification that the pedagogical model request a branch adaptation strategy be acted upon.  It is up to the handler
    * to select and execute the appropriate strategy instance.
    * 
    * @param branchAdaptation - information about the strategy selected
    * @param strategyHandlerRequestInterface - the interface used by this strategy handler to make request on its behalf (e.g. select the branch to follow) 
    */
    public void handleBranchAdaptation(BranchAdaptationStrategy branchAdaptation, StrategyHandlerRequestInterface strategyHandlerRequestInterface);

   /**
    * Notification that the pedagogical model request an instructional intervention strategy be acted upon.  It is up to the handler
    * to select and execute the appropriate strategy instance.
    * 
    * @param instructionalInterv - information about the strategy selected
    * @param strategyHandlerRequestInterface - the interface used by this strategy handler to make request on its behalf (e.g. display feedback on the TUI)
    * @param courseFolder the course folder for the course using a DKF with pedagogical tactics to apply
    */
    public void handleInstructionalIntervention(InstructionalInterventionStrategy instructionalInterv, 
            StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder);
    
   /**
    * Notification that the pedagogical model request additional assessment on the node provided.  It is up to the handler
    * to figure out how to provided further assessment information.
    * 
    * @param node - a performance assessment node in the task/concept hierarchy
    * @param strategyHandlerRequestInterface - the interface used by this strategy handler to make request on its behalf (e.g. display survey on the TUI) 
    */
    public void handleRequestForPerformanceAssessment(AbstractPerformanceAssessmentNode node, StrategyHandlerRequestInterface strategyHandlerRequestInterface);
    
    /**
     * Notification that the pedagogical model request additional assessment using the conversation specified.  It is up to the handler
     * to figure out how to provided further assessment information.
     * 
     * @param conversation - information about a conversation to deliver to the learner
     * @param strategyHandlerRequestInterface - the interface used by this strategy handler to make request on its behalf (e.g. display survey on the TUI) 
     */
     public void handleRequestForPerformanceAssessment(generated.dkf.Conversation conversation, StrategyHandlerRequestInterface strategyHandlerRequestInterface);
    
    /**
     * Notification that the pedagogical module request a 'do-nothing' strategy be acted upon.   Most handlers will have an empty
     * implementation for this, other than maybe logging that it was requested.
     * 
     * @param doNothingStrategy - the container for the strategy information
     */
    public void handleDoNothing(DoNothingStrategy doNothingStrategy);

    /**
     * Notification that the pedagogical model request an mid-lesson media strategy be acted upon.  It is up to the handler
     * to select and execute the appropriate strategy instance.
     * 
     * @param media - information about the strategy selected
     * @param strategyHandlerRequestInterface - the interface used by this strategy handler to make request on its behalf (e.g. display feedback on the TUI)
     * @param courseFolder the course folder for the course using a DKF with pedagogical tactics to apply
     */
    void handleMidLessonMedia(MidLessonMediaStrategy media, StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder);
}
