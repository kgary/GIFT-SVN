/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.EnvironmentAdaptation;
import generated.dkf.ScenarioAdaptation;
import mil.arl.gift.common.course.dkf.strategy.AbstractDKFStrategy;

/**
 * This class contains information about a scenario adaptation strategy.
 *
 * @author mhoffman
 *
 */
public class ScenarioAdaptationStrategy extends AbstractDKFStrategy {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ScenarioAdaptationStrategy.class);

    /** The scenario adaptation strategy activity */
    private ScenarioAdaptation scenarioAdaptation;

    /**
     * Class constructor - set attributes
     *
     * @param name - the display name of this strategy
     * @param scenarioAdaptation - dkf.xsd generated class instance
     */
    public ScenarioAdaptationStrategy(String name, ScenarioAdaptation scenarioAdaptation){
        super(name, scenarioAdaptation.getStrategyHandler());
        this.scenarioAdaptation = scenarioAdaptation;

        if (scenarioAdaptation.getDelayAfterStrategy() != null
                && scenarioAdaptation.getDelayAfterStrategy().getDuration() != null) {
            this.setDelayAfterStrategy(scenarioAdaptation.getDelayAfterStrategy().getDuration().floatValue());
        }
    }

    /**
     * Return the environment adaptation type from this activity
     *
     * @return the {@link EnvironmentAdaptation} from this activity 
     */
    public EnvironmentAdaptation getType() {
        if (logger.isInfoEnabled()) {
            logger.info("getType()");
        }

        return scenarioAdaptation.getEnvironmentAdaptation();
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ScenarioAdaptationStrategy: ");
        sb.append(super.toString());
        sb.append(", type = ").append(getType());
        sb.append("]");

        return sb.toString();
    }
}
