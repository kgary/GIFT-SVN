/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import net.customware.gwt.dispatch.shared.Action;

/**
 * An action that gets the scenario adaptations for training applications.
 */
public class FetchTrainingAppScenarioAdaptations  implements Action<FetchTrainingAppScenarioAdaptationsResult>{

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchTrainingAppScenarioAdaptations: ");
        sb.append("]");

        return sb.toString();
    } 
}
