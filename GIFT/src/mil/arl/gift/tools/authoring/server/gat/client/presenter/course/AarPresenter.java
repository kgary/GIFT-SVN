/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.course;

import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;

import generated.course.AAR;
import generated.course.BooleanEnum;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.aar.AarView;

/**
 * A presenter used to handle data management for an {@link AarView}.
 * 
 * @author nroberts
 */
public class AarPresenter extends AbstractGatPresenter implements AarView.Presenter{
        
    private static final Logger logger = Logger.getLogger(AarPresenter.class.getName());   
    
    /**
     * The view used to handle user input and display data back to the user
     */
    private AarView view;
    
    /**
     * The {@link AAR} currently being edited
     */
    private AAR currentAar; 
    
    /**
     * Creates a new presenter managing the given view
     * 
     * @param view the view to be managed
     */
    public AarPresenter(AarView view) {
        super();
        
        this.view = view;
        
        start();
        
        init();
    }   
    
    /**
     * Loads the given {@link AAR} into the view for editing
     * 
     * @param aar the AAR to edit
     */
    public void edit(AAR aar){
    	
    	this.currentAar = aar;
    	
    	populateView();
    }
    
    /**
     * Clears the view, setting all fields to their initial state.
     */
    private void clearView(){
    	
    	view.getFullScreenInput().setValue(null);
    	view.getDisabledInput().setValue(null);
    }
    
    @Override
    protected Logger getLogger() {
        return logger;
    }
    
    private void init() {

        HandlerRegistration registration = null;

        if(GatClientUtility.isReadOnly()){
            view.getFullScreenInputHasEnabled().setEnabled(false);
            view.getDisabledInputHasEnabled().setEnabled(false);
        }else{
            registration = view.getFullScreenInput().addValueChangeHandler(new ValueChangeHandler<Boolean> () {
    
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if(currentAar != null) {                    
                        BooleanEnum fullScreen = event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE;                    
                        currentAar.setFullScreen(fullScreen);
                        
                        eventBus.fireEvent(new EditorDirtyEvent());
                    }
                }
            });
            handlerRegistrations.add(registration);
            
            handlerRegistrations.add(view.getDisabledInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    
                    if(currentAar != null){        
                        
                        currentAar.setDisabled(event.getValue() != null && event.getValue()
                                ? BooleanEnum.TRUE
                                : BooleanEnum.FALSE
                        );
                                            
                        eventBus.fireEvent(new EditorDirtyEvent());
                                            
                        SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDisabledEvent(currentAar));
                    }
                }
                
            }));
        }
    }
    
    /**
     * Populates the view based on the current AAR.
     */
    public void populateView(){
    	
    	clearView();
    	
    	if(currentAar != null){
    		
    		view.getFullScreenInput().setValue(currentAar.getFullScreen() != null
                    ? currentAar.getFullScreen().equals(BooleanEnum.TRUE)
                    : true  //checked by default
            , true);
    		
    		view.getDisabledInput().setValue(currentAar.getDisabled() != null
                    ? currentAar.getDisabled().equals(BooleanEnum.TRUE)
                    : false  //not checked by default
            , true);
        }
    }    
    
    @Override
    public void start(){
    	super.start();
    }
    
    @Override
    public void stop(){
    	super.stop();
    }
}
