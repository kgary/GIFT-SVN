/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor.LearnerStateOutline;

import java.awt.Color;
import javax.swing.Icon;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 * This class is used to render a Learner State Outline with custom style
 * 
 * @author mzellars
 *
 */
public class RenderData implements RenderDataProvider {
	
	@Override
	public Color getBackground(Object node) {
		
		return null;
	}

	@Override
	public String getDisplayName(Object node) {
		
		@SuppressWarnings("unchecked")
		LearnerStateNode<String> n = (LearnerStateNode<String>)node;
		
		return n.getDisplayName();
	}

	@Override
	public Color getForeground(Object node) {

		return null;
	}

	@Override
	public Icon getIcon(Object node) {
		
		@SuppressWarnings("unchecked")
		LearnerStateNode<String> n = (LearnerStateNode<String>)node;
		
		return n.getIcon();
	}

	@Override
	public String getTooltipText(Object node) {
		
		return null;
	}

	@Override
	public boolean isHtmlDisplayName(Object arg0) {

		return false;
	}
}
