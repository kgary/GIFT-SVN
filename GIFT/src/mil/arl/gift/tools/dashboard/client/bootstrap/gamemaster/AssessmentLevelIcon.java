/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * An icon that visually indicates an assessment level
 * 
 * @author nroberts
 */
public class AssessmentLevelIcon extends Composite {
    
    /** The animation used to indicate to the user that the assessment level has changed */
    private static final Animation ICON_UPDATE_ANIMATION = Animation.FLIP;

    /** The UiBinder that combines the ui.xml with this java class */
    private static AssessmentLevelIconUiBinder uiBinder = GWT.create(AssessmentLevelIconUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AssessmentLevelIconUiBinder extends UiBinder<Widget, AssessmentLevelIcon> {
    }

    /** The inner star icon that should be the only star filled when the current level is BelowExpectation */
    @UiField
    protected Icon star1;
    
    /** The inner star icon that should be filled when the current level is AtExpectation */
    @UiField
    protected Icon star2;
    
    /** The inner star icon that should be filled when the current level is AboveExpectation */
    @UiField
    protected Icon star3;
    
    /** The current assessment level indicated by this icon */
    private AssessmentLevelEnum currentLevel = null;

    /**
     * Creates a new icon with no assessment level. This icon will be invisible until an assessment level other than Unknown is provided.
     */
    public AssessmentLevelIcon() {
        initWidget(uiBinder.createAndBindUi(this));
        
        setVisible(false);
    }
    
    /**
     * Sets the assessment level this icon represents and animates it if the previous assessment level was different
     * 
     * @param level the assessment level this icon should represent
     */
    public void setAssessmentLevel(AssessmentLevelEnum level) {
        setAssessmentLevel(level, true);
    }
    
    /**
     * Sets the assessment level this icon represents and optionally animates it if the previous assessment level was different
     * 
     * @param level the assessment level this icon should represent
     * @param animateOnChange whether or not to perform an animation if the previous assessment level was different
     */
    public void setAssessmentLevel(AssessmentLevelEnum level, boolean animateOnChange) {
        
        boolean assessmentUpdated = (currentLevel == null && level != null) || !currentLevel.equals(level);
        
        currentLevel = level;
        
        if(level == null || AssessmentLevelEnum.UNKNOWN.equals(level)) {
            setVisible(false);
            
        } else {
            
            setVisible(true);
            
            if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(level)) {
                star2.setColor("inherit"); //empty the 2nd star
                star3.setColor("inherit"); //empty the 3rd star
                
            } else if(AssessmentLevelEnum.AT_EXPECTATION.equals(level)) {
                star2.getElement().getStyle().clearColor(); //fill the 2nd star
                star3.setColor("inherit");                  //empty the 3rd star
                
            } else if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(level)) {
                star2.getElement().getStyle().clearColor(); //fill the 2nd star
                star3.getElement().getStyle().clearColor(); //fill the 3rd star
            }
        }
        
        if(assessmentUpdated && animateOnChange) {
            
            //perform an animation if the icon changes to draw the user's attention to it
            String iconAnimationName = Animate.animate(this, ICON_UPDATE_ANIMATION);
            Animate.removeAnimationOnEnd(this, iconAnimationName);
        }
    }

}
