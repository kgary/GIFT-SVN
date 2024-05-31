/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.course.AbstractActionKnowledge;
import mil.arl.gift.common.course.dkf.strategy.PerformanceAssessmentStrategy;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.domain.AbstractPedagogicalRequestHandler;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerRequestInterface;

/**
 * Implementation of the domain module pedagogical request handler that provides logic for course level
 * handling of those requests.
 * 
 * @author mhoffman
 *
 */
public class CoursePedagogicalRequestHandler extends AbstractPedagogicalRequestHandler{
    
    /**
     * Class constructor - 
     * @param courseActionKnowledge - the action knowledge used to handle pedagogical requests
     * @param strategyHandlerRequestInterface - the callback interface to handle implementation of pedagogical request via the domain module
     * @param courseFolder the course folder for the course with pedagogical tactics to apply
     * @param domainSession - info about the domain session being assessed.  Can't be null or empty.
     */
    public CoursePedagogicalRequestHandler(AbstractActionKnowledge courseActionKnowledge,
            StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder, DomainSession domainSession) {
        super(courseActionKnowledge, strategyHandlerRequestInterface, courseFolder, domainSession);
        
    }

    @Override
    protected boolean shouldIgnoreInstructionalInterventions() {
        return false;
    }

    @Override
    protected void determinePerformanceAssessment(
            PerformanceAssessmentStrategy performanceAssessment) {

        //not supported        
    }

    @Override
    protected boolean isRemediationEnabled() {
        return false;
    }


}
