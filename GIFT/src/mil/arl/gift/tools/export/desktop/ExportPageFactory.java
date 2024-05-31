/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Creates the wizard pages as the user clicks through the wizard.
 * 
 * @author cdettmering
 */
public class ExportPageFactory implements PageFactory {
	
	/** Create all pages upfront */
	private static final WizardPage[] pages = {
		new WelcomePage(),
		new DomainSelectionPage(),
	    new OptionsPage(),
		new ExportPage()
	};
	
	public List<WizardPage> getPages() {
		List<WizardPage> pageList = new ArrayList<WizardPage>(Arrays.asList(pages));
		pageList.add(new SummaryPage(null));
		return pageList;
	}

	@Override
	public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
		if(path.size() < pages.length) {
			return pages[path.size()];
		} else if(path.size() == pages.length) {
			return new SummaryPage(settings);
		} else {
			return null;
		}
	}

}
