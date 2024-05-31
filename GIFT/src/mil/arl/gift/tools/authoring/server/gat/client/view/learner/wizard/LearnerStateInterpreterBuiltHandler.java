/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard;

import generated.learner.Input;

/**
 * Handler that is called when a Learner State Interpreter (generated.learner.Input)
 * is constructed using the BuildLearnerStateInterpreterDialog.
 * @author elafave
 *
 */
public interface LearnerStateInterpreterBuiltHandler {

	public void onInterpreterBuilt(Input learnerStateInterpreter);
}
