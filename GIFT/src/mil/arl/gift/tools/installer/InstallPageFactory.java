/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;

import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Creates the wizard pages as the user clicks through the wizard.
 * 
 * @author cdettmering
 */
public class InstallPageFactory implements PageFactory {
    
    /**
     * the pages that will be available to the installer
     */
    private final List<WizardPage> pages;
    
    /**
     * singleton instance of this class
     */
    private static InstallPageFactory instance = null;
    
    /**
     * Get the singleton instance of the factory.
     * @return the singleton instance
     */
    public static synchronized InstallPageFactory getInstance(){
        
        if(instance == null){
            instance = new InstallPageFactory();
        }
        
        return instance;
    }
	
    /**
     * instantiate the appropriate pages for the installer
     */
	private InstallPageFactory(){
	    
	    pages = new ArrayList<>();
	    pages.add(new WelcomePage());
	    
	    // since the Tutor module (and TUI) is not used, there is no where to display a character agent
	    if(InstallerProperties.getInstance().getLessonLevel() != LessonLevelEnum.RTA){
	        pages.add(new AvatarPage());
	    }
	    
	    pages.add(new DatabasePage());
	    pages.add(new TrainingApplicationInstallPage());
	    pages.add(new MiscPage());
	}
	
	public List<WizardPage> getPages() {
		List<WizardPage> pageList = new ArrayList<WizardPage>(pages);
		pageList.add(new SummaryPage(null));
		return pageList;
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
