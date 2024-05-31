/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;

import java.util.ArrayList;
import java.util.List;

import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Creates the wizard pages as the user clicks through the wizard.
 * 
 * @author cdettmering
 */
public class InstallPageFactory implements PageFactory {
    
    private List<WizardPage> pages = new ArrayList<>();
    
    public void addPage(WizardPage page){
        pages.add(page);
    }
	
	public List<WizardPage> getPages() {
		return pages;
	}

	@Override
	public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
		if(path.size() < pages.size()) {
			return pages.get(path.size());
        } else if(path.size() == pages.size()) {
            return new SummaryPage(settings);
		} else {
			return null;
		}
	}

}
