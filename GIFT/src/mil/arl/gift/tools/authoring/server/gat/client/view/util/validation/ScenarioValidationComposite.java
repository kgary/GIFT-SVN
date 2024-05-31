/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.validation;

import java.io.Serializable;

import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * Validation composite for the scenario used to ensure proper dirty and validation
 * handing.
 * 
 * @author sharrison
 */
public abstract class ScenarioValidationComposite extends ValidationComposite {
    @Override
    protected void fireDirtyEvent(Serializable sourceObject) {
        ScenarioEventUtility.fireDirtyEditorEvent(sourceObject);
    }
}
