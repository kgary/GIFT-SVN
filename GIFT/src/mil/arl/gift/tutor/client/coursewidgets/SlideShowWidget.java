/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.coursewidgets;

import java.io.Serializable;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for displaying a slide show to the user
 * 
 * @author jleonard
 */
public class SlideShowWidget extends Composite {

	private static Logger logger = Logger.getLogger(SlideShowWidget.class.getName());
	
	private static SlideShowWidgetUiBinder uiBinder = GWT.create(SlideShowWidgetUiBinder.class);

    @UiField
    protected Image image;
    
    private Serializable properties;
    
    private int currentSlide = 0;
        
    private CourseHeaderWidget headerWidget;
    
    interface SlideShowWidgetUiBinder extends UiBinder<Widget, SlideShowWidget> {
    }

    /**
     * Constructor
     *
     * @param instance The abstract instance of the widget
     */
    public SlideShowWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        headerWidget = CourseHeaderWidget.getInstance();
    }
    
    public void setProperties(Serializable slideShowProperties) {
        
    	if(slideShowProperties instanceof generated.course.SlideShowProperties 
    	        || slideShowProperties instanceof generated.dkf.SlideShowProperties) {
    	    properties = slideShowProperties;
            init();
    		
    	} else {
    	    logger.warning("The properties provided to the SlideShowWidget are not SlideShowProperties");
    	}
    	
    }
    
    private void init() {
        
        if(properties instanceof generated.course.SlideShowProperties) {
            
            final generated.course.SlideShowProperties slideProperties = (generated.course.SlideShowProperties) properties;
            
            if(slideProperties != null && slideProperties.getSlideRelativePath() != null) {
                
                String url = slideProperties.getSlideRelativePath().get(0);
                image.setUrl(url);
                
                if(slideProperties.getSlideRelativePath().size() > 1) {
                
                    headerWidget.showNextButton(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            
                            currentSlide += 1;
                            String url = slideProperties.getSlideRelativePath().get(currentSlide);
                            image.setUrl(url);
        
                            if(currentSlide + 1 >= slideProperties.getSlideRelativePath().size()) {
                                headerWidget.setNextButtonEnabled(false);
                                headerWidget.setContinueButtonEnabled(true);
                            }
        
                            headerWidget.setPreviousButtonEnabled(true);
                        }               
                    }, "next slide");

                    if(slideProperties.getDisplayPreviousSlideButton() == generated.course.BooleanEnum.TRUE) {
                        headerWidget.showPreviousButton(new ClickHandler() {
        
                            @Override
                            public void onClick(ClickEvent event) {
                                if(currentSlide == 1) {
                                    headerWidget.setPreviousButtonEnabled(false);
                                }
                                
                                currentSlide -= 1;
                                String url = slideProperties.getSlideRelativePath().get(currentSlide);
                                image.setUrl(url);
                                
                                if(currentSlide < slideProperties.getSlideRelativePath().size()) {
                                    headerWidget.setNextButtonEnabled(true);
                                }
                            }
                        }, "previous slide");
                        headerWidget.setPreviousButtonEnabled(false);
                    }
                    
                    if(slideProperties.getKeepContinueButton() == generated.course.BooleanEnum.FALSE) {
                        headerWidget.setContinueButtonEnabled(false);
                    }
                }
            }
            
        } else if(properties instanceof generated.dkf.SlideShowProperties) {
                
            final generated.dkf.SlideShowProperties slideProperties = (generated.dkf.SlideShowProperties) properties;
            
            if(slideProperties != null && slideProperties.getSlideRelativePath() != null) {
                
                String url = slideProperties.getSlideRelativePath().get(0);
                image.setUrl(url);
                
                if(slideProperties.getSlideRelativePath().size() > 1) {
                
                    headerWidget.showNextButton(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            
                            currentSlide += 1;
                            String url = slideProperties.getSlideRelativePath().get(currentSlide);
                            image.setUrl(url);
        
                            if(currentSlide + 1 >= slideProperties.getSlideRelativePath().size()) {
                                headerWidget.setNextButtonEnabled(false);
                                headerWidget.setContinueButtonEnabled(true);
                            }
        
                            headerWidget.setPreviousButtonEnabled(true);
                        }               
                    }, "next slide");

                    if(slideProperties.getDisplayPreviousSlideButton() == generated.dkf.BooleanEnum.TRUE) {
                        headerWidget.showPreviousButton(new ClickHandler() {
        
                            @Override
                            public void onClick(ClickEvent event) {
                                if(currentSlide == 1) {
                                    headerWidget.setPreviousButtonEnabled(false);
                                }
                                
                                currentSlide -= 1;
                                String url = slideProperties.getSlideRelativePath().get(currentSlide);
                                image.setUrl(url);
                                
                                if(currentSlide < slideProperties.getSlideRelativePath().size()) {
                                    headerWidget.setNextButtonEnabled(true);
                                }
                            }
                        }, "previous slide");
                        headerWidget.setPreviousButtonEnabled(false);
                    }
                    
                    if(slideProperties.getKeepContinueButton() == generated.dkf.BooleanEnum.FALSE) {
                        headerWidget.setContinueButtonEnabled(false);
                    }
                }
            }
        }
    }
    
    /**
     * Gets whether or not the continue button should be enabled based on the provided media properties (if applicable, since guidance
     * media may not provide properties).
     * 
     * @return true if the continue button should be enabled false otherwise 
     */
    public boolean getContinueButtonEnabled() {
        
    	if(properties != null) {
    	
        	if(properties instanceof generated.course.SlideShowProperties) {
        	    
        	    generated.course.SlideShowProperties slideProperties = (generated.course.SlideShowProperties) properties;
        	    
        	    return (slideProperties.getKeepContinueButton() == generated.course.BooleanEnum.TRUE) || (slideProperties.getSlideRelativePath().size() == 1);
        	
        	} else if(properties instanceof generated.dkf.SlideShowProperties) {
                    
                generated.dkf.SlideShowProperties slideProperties = (generated.dkf.SlideShowProperties) properties;
                
                return (slideProperties.getKeepContinueButton() == generated.dkf.BooleanEnum.TRUE) || (slideProperties.getSlideRelativePath().size() == 1);
        	
        	} else {
        	    return false;
        	}
    	
    	} else {
    	    
    	    /*
    	     * Guidance objects (namely "Information from File" and "Information from Web") won't have media properties, so the 
    	     * continue button should always be enabled for them
    	     */
    	    return true;
    	}
    }
}
