/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.Strategy;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.RequestDoNothingTactic;
import mil.arl.gift.common.RequestInstructionalIntervention;
import mil.arl.gift.common.RequestMidLessonMedia;
import mil.arl.gift.common.RequestPerformanceAssessment;
import mil.arl.gift.common.RequestScenarioAdaptation;
import mil.arl.gift.common.course.AbstractActionKnowledge;
import mil.arl.gift.common.course.dkf.DomainActionKnowledge;
import mil.arl.gift.common.course.dkf.strategy.PerformanceAssessmentStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.DoNothingStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.ScenarioAdaptationStrategy;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerInterface;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerRequestInterface;

/**
 * This is the base class for Domain module pedagogical request handler.  It contains the common logic for dealing
 * with the pedagogical requests.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractPedagogicalRequestHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractPedagogicalRequestHandler.class);

    /** the current action knowledge */
    protected AbstractActionKnowledge actionKnowledge = null;

    /** used by strategy handlers to request that the core domain module classes (e.g. BaseDomainSession) do something on its behalf (e.g. display feedback on the TUI) */
    protected StrategyHandlerRequestInterface strategyHandlerRequestInterface;

    /** the course folder for the course using the DKF with pedagogical tactics to apply */
    protected DesktopFolderProxy courseFolder;

    /** the name of the learner being assessed.  Can't be null or empty. */
    protected final DomainSession domainSession;

    /**
     * Class constructor - set attributes
     *
     * @param actionKnowledge - the action knowledge used to handle pedagogical requests
     * @param strategyHandlerRequestInterface - the callback interface to handle implementation of pedagogical request via the domain module
     * @param courseFolder the course folder for the course using the DKF with pedagogical tactics to apply
     * @param domainSession - info about the domain session being assessed.  Can't be null or empty.
     */
    public AbstractPedagogicalRequestHandler(AbstractActionKnowledge actionKnowledge,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder, DomainSession domainSession){

        if(actionKnowledge == null){
            throw new IllegalArgumentException("The action knowledge can't be null");
        }
        this.actionKnowledge = actionKnowledge;

        if(strategyHandlerRequestInterface == null){
            throw new IllegalArgumentException("The strategy handler request interface can't be null");
        }
        this.strategyHandlerRequestInterface = strategyHandlerRequestInterface;

        if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null");
        }
        this.courseFolder = courseFolder;

        if(domainSession == null){
            throw new IllegalArgumentException("The domainSession can't be null");
        }
        this.domainSession = domainSession;
    }

    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes.
     */
    public void cleanup(){
        actionKnowledge = null;
        strategyHandlerRequestInterface = null;
        courseFolder = null;
    }
    
    /**
     * A pedagogical request was received and need to be handled by the domain
     * knowledge.  The handling of the pedagogical request is handled in a new thread 
     * and the calling thread to this method is released.
     *
     * @param pedRequest the incoming request to handle. If null or empty this
     *        method just returns.
     * @param domainActionKnowledge The {@link DomainActionKnowledge} that is
     *        used to convert each {@link AbstractPedagogicalRequest} to a
     *        {@link Strategy} that is used for the
     *        {@link AuthorizeStrategiesRequest}.
     */
    public void handlePedagogicalRequest(final PedagogicalRequest pedRequest, DomainActionKnowledge domainActionKnowledge) {

        if(pedRequest == null || pedRequest.getRequests().isEmpty()){
            return;
        }

        if(logger.isInfoEnabled()){
            logger.info("Received ped request of "+pedRequest);
        }

        Thread pedRequestHandlingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                
                List<Strategy> actionStrategies = domainActionKnowledge.getActions()
                        .getInstructionalStrategies().getStrategy();
                
                if(actionStrategies.isEmpty()) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Received a pedagogical request but found no available strategies to execute.\n"+pedRequest);
                    }
                    return;
                }

                /* The list of requests contain the individual activities from any number of
                 * strategies (multiple state transitions could have been triggered at the same
                 * time). We are executing the activities based on the strategy name, so execute the
                 * set of activities the first time we see a strategy name, but skip any other
                 * occurrence of the same name. 
                 * reasonToStrategies - is what will be populated and used outside of this method, it
                 * contains each unique reason string to the zero or more DKF strategies to apply.
                 * strategiesToApply - is used here to make sure the same DKF strategy isn't applied
                 * multiple times for this single pedagogical request message.
                 * (NOTE: not to be used for Branch Adaptations; those should always execute.) 
                 */
                Set<String> strategiesToApply = new HashSet<>();
                Map<String, List<StrategyToApply>> reasonToStrategies = new HashMap<>();
                for (Entry<String, List<AbstractPedagogicalRequest>> entry : pedRequest.getRequests().entrySet()) {

                    String reason = entry.getKey();
                    List<AbstractPedagogicalRequest> requestList = entry.getValue();
                    
                    if (!reasonToStrategies.containsKey(reason)) {
                        reasonToStrategies.put(reason, new ArrayList<>());
                    }

                    List<StrategyToApply> strategies = reasonToStrategies.get(reason);

                    // gather all the strategy names in the current entry...
                    for (AbstractPedagogicalRequest aRequest : requestList) {
                        
                        // do nothing, move on.
                        if (aRequest instanceof RequestDoNothingTactic) {
                            continue;
                        }
                        
                        String strategyName = null;

                        if (aRequest instanceof RequestInstructionalIntervention
                                || aRequest instanceof RequestMidLessonMedia
                                || aRequest instanceof RequestScenarioAdaptation
                                || aRequest instanceof RequestPerformanceAssessment) {

                            strategyName = aRequest.getStrategyName();
                            if (StringUtils.isBlank(strategyName)) {
                                if (logger.isWarnEnabled()) {
                                    logger.warn(
                                            "Received a pedagogical request that didn't contain a strategy. Moving on to the next request.");
                                }
                                continue;
                            }

                        } else if (aRequest instanceof RequestBranchAdaptation) {
                            strategyName = ((RequestBranchAdaptation) aRequest).getStrategy().getName();
                        }

                        if(strategyName == null) {
                            if (logger.isWarnEnabled()) {
                                logger.warn(
                                        "Received a pedagogical request that contained a strategy with no activities. Moving on to the next request.");
                            }
                            continue;
                        }else {
                            
                            // retrieve the authored strategy details by strategy name, adding the
                            // strategy details to the collection
                            for (Strategy strategy : actionStrategies) {
                                if (StringUtils.equals(strategyName, strategy.getName())) {
                                    // found the DKF strategy name being requested in the pedagogical request among the DKF strategies
                                    // (e.g. Request Instructional Intervention which contains a single feedback message and not many feedback messages that could be found in a single strategy set)
                                    // 
                                    if(!strategiesToApply.contains(strategy.getName())) {
                                        // found a DKF strategy that hasn't already been added to reasonToStrategies object for this pedagogical request
                                        // again - only want one reference to a DKF strategy per 'reasonToStrategies' object.
                                        StrategyToApply toApply = new StrategyToApply(strategy, reason, null);
                                        toApply.setTaskConceptsAppliedToo(aRequest.getTaskConceptsAppliedToo());
                                        strategies.add(toApply);
                                        strategiesToApply.add(strategy.getName());
                                        break;
                                    }
                                }
                            }
                        }
                    }//end for on AbstractPedagogicalRequest

                }//end for on Entry<String, List<AbstractPedagogicalRequest>>

                strategyHandlerRequestInterface.sendAuthorizeStrategiesRequest(reasonToStrategies, domainSession);
            }

        }, "Ped request handler -" + (domainSession.getUsername() != null ? domainSession.getUsername()
                : domainSession.getDomainSessionId()));

        pedRequestHandlingThread.start();
    }

    /**
     * Return whether or not the domain knowledge was configured to ignore instructional intervention Requests (e.g. Feedback)
     * (i.e. disable strategy implementation(s))
     *
     * @return boolean
     */
    protected abstract boolean shouldIgnoreInstructionalInterventions();
    
    /**
     * Return whether or not the course creator would like remediation to be delivered when not using
     * an adaptive courseflow course object.
     * 
     * @return true if the author wants remediation to be given in a training application course object
     */
    protected abstract boolean isRemediationEnabled();

    /**
     * Return a StrategyHandlerInterface instance for the handler class name provided.
     *
     * @param handlerClassName - class name for a handler interface implementation class (e.g. domain.knowledge.strategy.DefaultStrategyHandler)
     * @return StrategyHandlerInterface
     */
    public static StrategyHandlerInterface getHandler(String handlerClassName){

        try{
            Class<?> clazz = Class.forName(PackageUtil.getRoot() + "." + handlerClassName);
            StrategyHandlerInterface handler = StrategyHandlerInterface.Singleton.instance(clazz.asSubclass(StrategyHandlerInterface.class));
            return handler;
        }catch(Exception e){
            logger.error("Caught exception while instantiating the Strategy Handler Interface instance with class name "+handlerClassName, e);
        }

        return null;
    }

    /**
     * Determine the appropriate instructional intervention based on TBD
     *
     * @param instructionalInterv
     */
    protected void determineInstructionalIntervention(InstructionalInterventionStrategy instructionalInterv){

        StrategyHandlerInterface handler = getHandler(instructionalInterv.getHandlerInfo().getImpl());
        if(handler != null){
            handler.handleInstructionalIntervention(instructionalInterv, strategyHandlerRequestInterface, courseFolder);
        }
    }

    /**
     * Determine the appropriate mid-lesson media based on TBD
     *
     * @param media the strategy with which to retrieve media
     */
    protected void determineMidLessonMedia(MidLessonMediaStrategy media){

        StrategyHandlerInterface handler = getHandler(media.getHandlerInfo().getImpl());
        if(handler != null){
            handler.handleMidLessonMedia(media, strategyHandlerRequestInterface, courseFolder);
        }
    }

    /**
     * Determine the appropriate scenario adaptation based on TBD
     *
     * @param scenarioAdaptation the scenario adaptation to apply.  Can't be null.
     */
    protected void determineScenarioAdaptation(ScenarioAdaptationStrategy scenarioAdaptation){

        StrategyHandlerInterface handler = getHandler(scenarioAdaptation.getHandlerInfo().getImpl());
        if(handler != null){
            handler.handleScenarioAdaptation(scenarioAdaptation, strategyHandlerRequestInterface);
        }
    }

    /**
     * Determine the appropriate branch adaptation based on TBD
     *
     * @param branchAdaptation
     */
    protected void determineBranchAdaptation(BranchAdaptationStrategy branchAdaptation){

        StrategyHandlerInterface handler = getHandler(branchAdaptation.getHandlerInfo().getImpl());
        if(handler != null){
            handler.handleBranchAdaptation(branchAdaptation, strategyHandlerRequestInterface);
        }
    }

    /**
     * Determine the appropriate 'Do-Nothing' tactic
     *
     * @param doNothingStrategy
     */
    protected void determineDoNothingAction(DoNothingStrategy doNothingStrategy){

        StrategyHandlerInterface handler = getHandler(doNothingStrategy.getHandlerInfo().getImpl());
        if(handler != null){
            handler.handleDoNothing(doNothingStrategy);
        }
    }

    /**
     * Determine the appropriate performance assessment to give
     *
     * @param performanceAssessment the strategy to process
     */
    protected abstract void determinePerformanceAssessment(PerformanceAssessmentStrategy performanceAssessment);

}
