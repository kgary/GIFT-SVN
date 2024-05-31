/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.survey;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.GIFTAutotutorSessionTypeEditingMode;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.RealTimeAssessmentPanel;

public class AutoTutorSessionChoicePanel extends Composite{
    
    private static AutoTutorSessionChoicePanelUiBinder uiBinder = GWT
            .create(AutoTutorSessionChoicePanelUiBinder.class);

    interface AutoTutorSessionChoicePanelUiBinder extends
            UiBinder<Widget, AutoTutorSessionChoicePanel> {
    }
    
    @UiField
    protected TextBox conversationUrlBox;
    
    @UiField
    protected RealTimeAssessmentPanel dkfSelectPanel;
    
    @UiField
    protected Widget skoPanel;
    
    @UiField
    protected Widget dkfPanel;
    
    @UiField(provided=true)
    protected Image createConversationButton = new Image(GatClientBundle.INSTANCE.add_image());
    
    public AutoTutorSessionChoicePanel(){
        initWidget(uiBinder.createAndBindUi(this));
        
        createConversationButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                GatClientUtility.openASATWindow();
            }
        });
    }
    
    /**
     * Sets the current autotutor session type editing mode
     * 
     * @param mode the editing mode to set
     */
    public void setAutotutorTypeEditingMode(GIFTAutotutorSessionTypeEditingMode mode){
        
        if(mode.equals(GIFTAutotutorSessionTypeEditingMode.SKO)){
            skoPanel.setVisible(true);
            dkfPanel.setVisible(false);
            
        } else if(mode.equals(GIFTAutotutorSessionTypeEditingMode.DKF)){
            skoPanel.setVisible(false);
            dkfPanel.setVisible(true);
        } 
    }
    
    /**
     * Gets the button used to select a DKF for the AutoTutor session
     * 
     * @return  the button used to select a DKF for the AutoTutor session
     */
    public HasClickHandlers getSelectDKFFileButton(){
        return dkfSelectPanel.getAddAssessmentButton();
    }
    
    /**
     * Sets the DKF file label
     * 
     * @param path The path to the DKF file
     */
    public void showDkfFileLabel(String path) {
    	dkfSelectPanel.setAssessment(path);
    }
    
    /**
     * Hides the dkf file label and displays the select file button.
     */
    public void hideDkfFileLabel() {
    	dkfSelectPanel.removeAssessment();
    }
    
    /**
     * Gets the remove dkf button
     * 
     * @return The remove dkf button
     */
    public HasClickHandlers getRemoveDkfButton() {
        return dkfSelectPanel.getDeleteButton();
    }
    
    /**
     * Gets the clickable Dkf file image
     *  
     * @return the Dkf edit image
     */
    public HasClickHandlers getEditDkfButton() {
        return dkfSelectPanel.getEditButton();
    }
    
    /**
     * Gets the input box used to enter an AutoTutor conversation URL
     * 
     * @return the AutoTutor conversation URL input box
     */
    public TextBox getConversationUrlBox(){
        return conversationUrlBox;
    }

}
