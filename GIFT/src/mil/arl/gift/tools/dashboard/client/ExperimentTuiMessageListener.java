/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window;

import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.EndCourseMessage;
import mil.arl.gift.common.util.StringUtils;

/**
 * The ExperimentTuiMessageListener class extends the TuiMessageListener. It
 * primarily overrides the handling of the tui messages so they can be handled
 * properly according to the experiment.
 * 
 * @author sharrison
 *
 */
public class ExperimentTuiMessageListener extends TuiMessageListener {

    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(ExperimentTuiMessageListener.class.getName());

    /**
     * Constructor
     */
    public ExperimentTuiMessageListener() {
        super();
    }

    @Override
    public boolean handleMessage(AbstractIFrameMessage msg) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("receiving message: " + msg.getMsgType());
        }

        boolean handled = false;

        if (msg instanceof EndCourseMessage) {
            EndCourseMessage endCourseMsg = (EndCourseMessage) msg;

            UiManager.getInstance().onExperimentEnded();

            final String experimentReturnUrl = endCourseMsg.getExperimentReturnUrl();
            if (StringUtils.isBlank(experimentReturnUrl)) {
                /* If the experiment does not have a return url, reset history
                 * to the start of the experiment so that if the user refreshes
                 * or clicks 'back' then they will be redirected to the 'start
                 * experiment' page */
                HistoryManager.getInstance().replaceHistory(HistoryManager.COURSE_END);
                
                if(endCourseMsg.getShouldReload()) {
                    
                    //reload if the TUI requests it, since this client may be out of sync with the server
                    Window.Location.reload();
                }
                
            } else {
                /* The experiment just ended and has a return url specified;
                 * redirect the window to the URL */
                Window.Location.replace(experimentReturnUrl);
            }

            handled = true;
        } else {
            handled = super.handleMessage(msg);
        }

        return handled;
    }
}
