/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.pedagogy.wizard;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import generated.ped.Attribute;

/**
 * Handler that is called when an Attribute (generated.ped.Attribute) is
 * constructed using the BuildAttributeDialog.
 * @author elafave
 *
 */
public interface AttributeBuiltHandler {

	public void onAttributeBuilt(MerrillQuadrantEnum quadrant, Attribute attribute);
}
