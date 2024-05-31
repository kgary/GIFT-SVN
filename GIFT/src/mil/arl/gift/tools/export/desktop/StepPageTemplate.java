/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.pagetemplates.DefaultPageTemplate;
import org.ciscavate.cjwizard.pagetemplates.PageTemplate;

/**
 * Template that shows the number of steps, and which step is currently active.
 * 
 * @author cdettmering
 */
public class StepPageTemplate extends PageTemplate {
	
	/** Generated serial */
	private static final long serialVersionUID = 2654176514774718663L;
	
	private static final PageTemplate delegate = new DefaultPageTemplate();
	
	private JPanel stepPanel;
	
	private List<JLabel> steps;
	
	/** the panel containing each step (i.e. the wizard pages) as a label */
	private JScrollPane stepPanelView;
	
	/**
	 * Creates a new StepPageTemplate
	 */
	public StepPageTemplate() {
		super();
		setupUi();
		steps = new ArrayList<JLabel>();
		
	}
	
	/**
	 * Adds a page to the list of steps.
	 * 
	 * @param page The page to add.
	 */
	public void addStep(WizardPage page) {
		JLabel label = new JLabel(page.getDescription());
		label.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 20));
		stepPanel.add(label);
		steps.add(label);
	}

	/**
	 * Sets the currently active page
	 * 
	 * @param page The page to set
	 */
	@Override
	public void setPage(final WizardPage page) {
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				// Delegate the page turning logic
				delegate.setPage(page);				
				
				// Bold the current step.
				int index = indexOf(page.getDescription());
				if(index != -1) {
					for(JLabel step : steps) {
						step.setText(unBold(step.getText()));
						step.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 20));
					}
					
					JLabel label = steps.get(index);
					label.setText(makeBold(steps.get(index).getText()));
					label.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));
					
					//auto scroll to keep current label on screen
					Point labelTopLeft = label.getLocation();
					labelTopLeft.x = 0;
					stepPanelView.getViewport().setViewPosition(labelTopLeft);
				}
			}
			
		});
	}
	
	/**
	 * Bolds str tobe displayed in a JLabel
	 * 
	 * @param str String to bold
	 * @return Bolded version of str
	 */
	private String makeBold(String str) {
		return "<html><p><b>" + str + "</b></p></html>";
	}
	
	/**
	 * Removes bolding tags from str
	 * 
	 * @param str The string to unbold
	 * @return Unbolded version of str
	 */
	private String unBold(String str) {
		String strip = str.replaceAll("<html><p><b>", "");
		strip = strip.replaceAll("</b></p></html>", "");
		return strip;
	}
	
	/**
	 * Gets the index in the steps list of the page description
	 * 
	 * @param description String to find index of
	 * @return Index of description or -1 if it does not exist
	 */
	private int indexOf(String description) {
		int i = 0;
		for(JLabel step : steps) {
			if(step.getText().equals(description)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	/**
	 * Sets up the template UI
	 */
	private void setupUi() {
		stepPanel = new JPanel();
		stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.Y_AXIS));
		stepPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		stepPanelView = new JScrollPane(stepPanel);
		add(stepPanelView);
		
		delegate.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
		JScrollPane delegateView = new JScrollPane(delegate);
		add(delegateView);
	}

}
