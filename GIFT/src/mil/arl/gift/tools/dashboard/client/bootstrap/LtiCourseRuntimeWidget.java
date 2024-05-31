/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;



import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.UiManager.EndCourseReason;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The LtiCourseRuntimeWidget is a child of the BsCourseRuntimeWidget and uses the same skinning as that widget.
 * The differences are in how the screen transitions are handled.  Where the BsCourseRuntimeWidget expects there to
 * be a signed in user, the LtiCourseRuntimeWidget uses a non-logged in lti user where there is no sign in screen.
 * Instead an lti welcome page and lti ending page are used to provide context for the lti user experience.
 * 
 * @author nblomberg
 *
 */
public class LtiCourseRuntimeWidget extends BsCourseRuntimeWidget  {

   
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(LtiCourseRuntimeWidget.class.getName());
    
    /** The parameters used to load the course. */
    private LoadCourseParameters loadCourseParams;
    
    /**
     * Constructor
     * 
     * @param loadCourseParameters - contains parameters needed to load a GIFT course
     * @param offlineMode - true if the dashboard is in offline mode, false otherwise.
     */
    public LtiCourseRuntimeWidget(Object loadCourseParameters,
            boolean offlineMode, ServerProperties serverProperties) {
        super(loadCourseParameters, offlineMode, serverProperties.getTutorCourseStartTimeout());
        
        loadCourseParams = (LoadCourseParameters)loadCourseParameters;
        
        // Override the callback for errors to go back to the ending page.
        courseErrorCallback = new DialogCallback() {

            @Override
            public void onAccept() {
                UiManager.getInstance().displayScreen(ScreenEnum.LTI_CONSUMER_END_PAGE);
                
            }
            
        };
        
    }
    
    @Override
    public void handleCourseEndingTimerExpired(EndCourseReason reason) {
        courseEnded(reason);
    }


    @Override
    public void handleCourseEnded(EndCourseReason reason) {
        courseEnded(reason);
    }
    
    /**
     * Perform cleanup that is needed when the course has ended.  
     * 
     * @param reason - the reason that the course was ended.
     */
    private void courseEnded(final EndCourseReason reason) {
        
        logger.fine("Sending endcourse rpc for course: " + loadCourseParams.getCourseRuntimeId());
        dashboardService.endCourse(UiManager.getInstance().getSessionId(), loadCourseParams.getCourseRuntimeId(), new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("Failure while executing the endCourse rpc: " + t.getMessage());
                transitionToNextScreen(reason);
                
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (result.isSuccess()) {
                    logger.fine("endCourse rpc returned success.");
                } else {
                    logger.fine("endCourse rpc returned failure: " + result.getResponse());
                }
                
                transitionToNextScreen(reason);
            } 
        });
    }
    
    /**
     * Transition to the next screen based on the reason that the course was ended.
     * 
     * @param reason - the reason the course was ended.
     */
    private void transitionToNextScreen(EndCourseReason reason) {
        logger.info("transitioning to lti end page screen.");
        UiManager.getInstance().displayScreen(ScreenEnum.LTI_CONSUMER_END_PAGE);
    }
    
    
}
