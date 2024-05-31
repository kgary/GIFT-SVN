/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.course.TrainingApplication;
import generated.course.TrainingApplication.Options.Remediation;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.OptionalGuidanceCreator;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

/**
 * The Class TrainingAppViewImpl.
 */
public class TrainingAppViewImpl extends Composite implements TrainingAppView, CourseReadOnlyHandler {
    
    /** logger instance */
    private static Logger logger = Logger.getLogger(TrainingAppViewImpl.class.getName());

	/** The ui binder. */
	private static TrainingAppViewImplUiBinder uiBinder = GWT
			.create(TrainingAppViewImplUiBinder.class);
	
	/**
	 * The Interface TrainingAppViewImplUiBinder.
	 */
	interface TrainingAppViewImplUiBinder extends
			UiBinder<Widget, TrainingAppViewImpl> {
	}
	
	@UiField
	protected CheckBox fullScreen;
	
	@UiField
	protected CheckBox disableTutoring;
	
	@UiField
	protected CheckBox showAvatarInitCbx;
	
	/** The disable checkbox. */
    @UiField
    protected CheckBox disabled;
	
    //////////////////
    // Avatar
    //////////////////
    
    @UiField
    protected Label avatarFileLabel;
    
    @UiField
    protected FocusPanel selectAvatarFilePanel;
    
    @UiField
    protected Widget avatarSelectedPanel;
    
    @UiField
    protected Button removeAvatarButton;
    
    @UiField
    protected Label selectAvatarFileLabel;	
    
    @UiField
    protected Widget fullScreenPanel;
	
	@UiField
	protected OptionalGuidanceCreator guidanceCreator;
	
	@UiField
	protected TrainingAppInteropEditor taInteropEditor;
	
	/** The editor used to modify training application remediation */
	@UiField
	protected RemediationEditor remediationEditor;
	
	/** The button used to add remediation to the training application being authored */
	@UiField
    protected Button addRemediationButton;
    
	/** The button used to add remediation from the training application being authored */
    @UiField
    protected Button deleteRemediationButton;
    
    /** The panel contains the ppt options */
    @UiField
    protected DisclosurePanel optionsPanel;
    
    /** The panel containing the button used to add remediation*/
    @UiField
    protected FlowPanel addRemediationPanel;
    
    /** The panel containing UI controls to edit the current remediation*/
    @UiField
    protected FlowPanel remediationButtonPanel;
    
    /** The widget containing all of the UIs used to author remediation */
    @UiField
    protected Widget remediationContainer;
    
    /** The deck used to switch between the UIs for when remediation is/isn't authored */
    @UiField
    protected DeckPanel remediationPanel;
    
    /** A button used to allow the author to change training app types when GIFT's lesson level is set to RTA */
    @UiField
    protected Button changeTypeButton;
	
	/**
	 * Instantiates a new training app view impl.
	 * 
	 * @param guidanceTransEditor the guidance transition editor injected into this view
	 */
	public TrainingAppViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		
		CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
		
		init();
	}

	/**
	 * Initializes the UI elements that make up this view.
	 */
	private void init() {
	    
        // If LessonLevel is set to RTA, then the widgets should be hidden.
	    if(GatClientUtility.isRtaLessonLevel()){
	        remediationContainer.setVisible(false);
	        guidanceCreator.setVisible(false);
	        showAvatarInitCbx.setVisible(false);
            selectAvatarFileLabel.setVisible(false);
            removeAvatarButton.setVisible(false);
            fullScreen.setVisible(false);
            disableTutoring.setVisible(false);
            disabled.setVisible(false);
            optionsPanel.setVisible(false);
        }
	    
	    remediationPanel.showWidget(0);
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getFullScreenInput()
	 */
	@Override
	public HasValue<Boolean> getFullScreenInput() {
		return fullScreen;
	}
	
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getFullScreenHasEnabled()
     */
	@Override
	public HasEnabled getFullScreenHasEnabled(){
	    return fullScreen;
	}
	
    @Override
    public HasValue<Boolean> getDisabledInput() {
        return disabled;
    }

    @Override
    public HasEnabled getDisabledInputHasEnabled() {
        return disabled;
    }
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getDisableInstrInput()
	 */
	@Override
	public HasValue<Boolean> getDisableInstrInput() {
		return disableTutoring;
	}
	
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getDisableTutoringHasEnabled()
     */
    @Override
    public HasEnabled getDisableTutoringHasEnabled(){
        return disableTutoring;
    }

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getShowAvatarInput()
	 */
	@Override
	public CheckBox getShowAvatarInput() {
		return showAvatarInitCbx;
	}
	
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getShowAvatarHasEnabled()
     */
    @Override
    public HasEnabled getShowAvatarHasEnabled(){
        return showAvatarInitCbx;
    }
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getChooseAvatarButtonInputHasEnabledInterface()
	 */
	@Override
	public HasClickHandlers getChooseAvatarButtonInput(){
		return selectAvatarFilePanel;
	}
	
	/* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getRemoveAvatarButtonInput()
     */
    @Override
    public HasClickHandlers getRemoveAvatarButtonInput(){
        return removeAvatarButton;
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getRemoveAvatarHasEnabled()
     */
    @Override
    public HasEnabled getRemoveAvatarHasEnabled(){
        return removeAvatarButton;
    }

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getChooseAvatarButtonInputHasVisibility()
	 */
	@Override
	public HasVisibility getChooseAvatarButtonInputHasVisibility(){
		return selectAvatarFilePanel;
	}
	
	/* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.ta.TrainingAppView#getAvatarSelectedHasVisibility()
     */
    @Override
    public HasVisibility getAvatarSelectedHasVisibility(){
        return avatarSelectedPanel;
    }
	
	@Override
	public void showConfirmCreateGuidanceDialog(OkayCancelCallback callback){
		
		OkayCancelDialog.show("Create Guidance", "The current training application transition does not have any guidance associated with it.", "Create Guidance", callback);
	}
	
	@Override
	public HasText getAvatarFileLabel(){
		return avatarFileLabel;
	}
	
    @Override
    public HasText getSelectAvatarFileLabel(){
        return selectAvatarFileLabel;
    }
	
	@Override
	public void setInteropEditorTrainingApplication(TrainingApplication app){
		taInteropEditor.setTrainingApplication(app);
	}

	@Override
	public void setCourseFolderPath(String courseFolderPath) {
		taInteropEditor.setCourseFolderPath(courseFolderPath);
	}

    @Override
    public void setCourseSurveyContextId(BigInteger surveyContextId) {
        taInteropEditor.setCourseSurveyContextId(surveyContextId);
        
    }
    
    @Override
    public OptionalGuidanceCreator getGuidanceCreator(){
    	return guidanceCreator;
    }
    
    @Override
    public void setChoiceSelectionListener(Command command) {
		this.taInteropEditor.setChoiceSelectionListener(command);
	}
    
    @Override
    public void setAvatarFullScreenInputVisible(boolean visible){
    	fullScreenPanel.setVisible(visible);
    }

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		fullScreen.setEnabled(!isReadOnly);
		disabled.setEnabled(!isReadOnly);
		showAvatarInitCbx.setEnabled(!isReadOnly);
		disableTutoring.setEnabled(!isReadOnly);
		guidanceCreator.setEnabled(!isReadOnly);
		setRemediationEditingEnabled(!isReadOnly);
	}
	
	@Override
	public void setRemediation(Remediation remediation, List<String> courseConcepts, boolean showNoConceptsError) {
	    
	    if(remediation != null) {
	        
	        if(showNoConceptsError && (courseConcepts == null || courseConcepts.isEmpty())) {
	            
	            List<String> allConcepts = GatClientUtility.getBaseCourseConcepts();
	            
	            if(allConcepts == null || allConcepts.isEmpty()) {
	                
    	            WarningDialog.error("Missing course concepts", "Remediation requires course concepts to be defined "
    	                    + "and the course concepts could not be loaded. "
                            + "<br/><br/>Please check the course properties to verify that you have specified concepts "
                            + "to cover in this course.");
	            } else {
	                
	                StringBuilder sb = new StringBuilder("None of this course's concepts were included in the provided "
	                        + "real-time assessment. "
                            + "<br/><br/>Please add one or more of the your course's concepts to the real-time assessment and "
                            + "have overall assessment rules for at least one descendant condition.");
	            
    	            WarningDialog.error("No Real-Time Assessment Course Concepts", sb.toString());
	            }
	        }
	        
	        logger.info("Showing remediation panel for training app");
	        
	        remediationPanel.showWidget(remediationPanel.getWidgetIndex(remediationButtonPanel));
	        remediationEditor.setAvailableConcepts(courseConcepts, showNoConceptsError);
	        remediationEditor.editObject(remediation);
	        
	    } else {
	        remediationPanel.showWidget(remediationPanel.getWidgetIndex(addRemediationPanel));
	    }
	}
	
	@Override
	public HasClickHandlers getAddRemediationButton() {
	    return addRemediationButton;
	}
	
	@Override
    public HasClickHandlers getDeleteRemediationButton() {
        return deleteRemediationButton;
    }

    @Override
    public void setRemediationEditingEnabled(boolean enabled) {
        addRemediationButton.setEnabled(enabled);
        deleteRemediationButton.setEnabled(enabled);
        deleteRemediationButton.setVisible(enabled);
    }
    
    @Override
    public void setDkfChangedCommand(Command command) {
        taInteropEditor.setDkfChangedCommand(command);
    }
    
    /**
     * Gets the button used to change the training application type
     * 
     * @return the button
     */
    @Override
    public Button getChangeApplicationButton(){
        return changeTypeButton;
    }
}
