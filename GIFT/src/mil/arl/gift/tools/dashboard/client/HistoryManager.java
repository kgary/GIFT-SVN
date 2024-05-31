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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;


/**
 * The HistoryManager singleton class provides an interface into the gwt history object.
 * Currently it provides accessors and wraps the gwt history object and encapsulates
 * all the url tokens that the client can use.
 * 
 * @author nblomberg
 *
 */
public class HistoryManager implements ValueChangeHandler<String>{
   
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(HistoryManager.class.getName());

    // Define the primary urls that the user can navigate to.
    public static final String LOGIN = "login";
    public static final String MYCOURSES = "takeacourse";
    public static final String MYSTATS = "learnerprofile";
    public static final String MYTOOLS = "coursecreator";
    public static final String MYEXPERIMENTS = "publishcourses";
    public static final String COURSE_RUNTIME = "course";
    public static final String GAME_MASTER = "gamemaster";
    public static final String WEB_MONITOR_STATUS = "status";
    public static final String WEB_MONITOR_MESSAGE = "message";
    public static final String LTICONSUMERSTART = "lticonsumerstart";
    public static final String LTICONSUMEREND = "lticonsumerend";
    public static final String EXPERIMENT_START = "experimentstart";

    /** Domain history tag */
    public final static String DOMAIN_SESSION_PAGE_TAG = "domain";
    /** User session history tag */
    public final static String USER_SESSION_ID_TAG = ".userSessionId=";
    /** End of course history tag */
    public final static String COURSE_END = "courseEnd";
    
    // Instance of the singleton object.
    private static HistoryManager instance = null;
    
    // Keeps track of the current/old token that the application is on.
    private String oldToken = "";

    /**
     * Constructor - private for singleton use..
     */
    private HistoryManager() {
        logger.fine("Creating HistoryManager class.");
        
        // Setup the value change handler.
        History.addValueChangeHandler(this);
    }
    
    /**
     * Gets an instance of the singleton object.  If the instance
     * is not yet created, it will be created.
     * 
     * @return HistoryManager - instance to the singleton object.  This should never return null.
     */
    public static HistoryManager getInstance() {
        if (instance == null) {
            
            instance = new HistoryManager();
        }
        
        return instance;
    }
    
    /**
     * Adds the history token to the url.
     * @param token - The string token to append to the url.
     */
    public void addHistory(String token) {
        // For now don't fire history events.
        History.newItem(token, false);
        
        // Save off the token.
        oldToken = token;
    }

    /**
     * Replaces the history token to the url.
     * 
     * @param token - The string token to append to the url.
     */
    public void replaceHistory(String token) {
        // For now don't fire history events.
        History.replaceItem(token, false);

        // Save off the token.
        oldToken = token;
    }

    /**
     * The onValueChange() function handles requested changes to the history
     * token.  The application can respond to the requested changes in history via this
     * handler.  
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String historyToken = event.getValue();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("History request to change from: " + oldToken + " to: " + historyToken);
        }
        
        // If we're on the course runtime panel and we're trying to back out, then we need to display a confirmation dialog.
        if (oldToken.equalsIgnoreCase(COURSE_RUNTIME)) {
            
            UiManager.getInstance().showStopCourseDialog();
            // Keep the history on 'courses' and let the dialog handle the confirm action which will update the history.  on decline
            // the user stays on 'courses' then.
            replaceHistory(oldToken);
            
        } else if (oldToken.equalsIgnoreCase(EXPERIMENT_START) || oldToken.equalsIgnoreCase(COURSE_END)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Back button was pressed before or after an experiment.");
            }

            /* Revert the history to its previous value */
            replaceHistory(oldToken);
            UiManager.getInstance().browserBackClickedInExperiment(false);

        } else if (oldToken.contains(USER_SESSION_ID_TAG)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Back button was pressed during an experiment.");
            }

            /* Revert the history to its previous value */
            replaceHistory(oldToken);

            /* Ignore the new token if it's identical to the previous token */
            if (!StringUtils.equalsIgnoreCase(oldToken, historyToken)) {
                UiManager.getInstance().browserBackClickedInExperiment(true);
            }

        } else if (UiManager.getInstance().isGatAuthoringActive()) {
            
            ScreenEnum screenId = convertTokenToScreenEnum(historyToken);
            UiManager.getInstance().showLeaveGatDialog(screenId);
            replaceHistory(oldToken);
            
        } else if (historyToken.equalsIgnoreCase(MYSTATS)) {
            UiManager.getInstance().displayScreen(ScreenEnum.LEARNER_PROFILE);
        } else if (historyToken.equalsIgnoreCase(MYTOOLS)) {
            UiManager.getInstance().displayScreen(ScreenEnum.COURSE_CREATOR);
        } else if (historyToken.equalsIgnoreCase(MYCOURSES)) {
            UiManager.getInstance().displayScreen(ScreenEnum.MYCOURSES);
        } else if (historyToken.equalsIgnoreCase(MYEXPERIMENTS)) {
            UiManager.getInstance().displayScreen(ScreenEnum.MY_RESEARCH);
        } else if (historyToken.equalsIgnoreCase(WEB_MONITOR_STATUS)) {
            UiManager.getInstance().displayScreen(ScreenEnum.WEB_MONITOR_STATUS);
        } else if (historyToken.equalsIgnoreCase(WEB_MONITOR_MESSAGE)) {
            UiManager.getInstance().displayScreen(ScreenEnum.WEB_MONITOR_MESSAGE);
        } else {
            // In these cases, we don't want the history to change (for example: going back to login page if we're already signed in is not allowed).
            // Keep the user on their current history token in these cases.
            logger.fine("Not updating history request.  Keeping history on token: " + oldToken);
            replaceHistory(oldToken);
        }
        
    }
    
    
    /**
     * Function to convert a history token to a corresponding screen id.  
     *
     * @param token - The history token to check against.
     * @return ScreenEnum - The corresponding screen that matches the history token.  Can return Invalid if there is no match.
     */
    public ScreenEnum convertTokenToScreenEnum(String token) {
        
        ScreenEnum screenId = ScreenEnum.INVALID;
        if (token.equalsIgnoreCase(COURSE_RUNTIME)) {
            screenId = ScreenEnum.COURSE_RUNTIME;
        } else if (token.equalsIgnoreCase(MYSTATS)) {
            screenId = ScreenEnum.LEARNER_PROFILE;
        } else if (token.equalsIgnoreCase(MYTOOLS)) {
            screenId = ScreenEnum.COURSE_CREATOR;
        } else if (token.equalsIgnoreCase(MYCOURSES)) {
            screenId = ScreenEnum.MYCOURSES;
        } else if (token.equalsIgnoreCase(MYEXPERIMENTS)) {
            screenId = ScreenEnum.MY_RESEARCH;
        } else if (token.equalsIgnoreCase(LOGIN)) {
            screenId = ScreenEnum.LOGIN;
        } else {
            logger.severe("Unable to translate token to screen id.  Token is: " + token);
        }
        
        
        return screenId;
    }

    /**
     * Builds the user session history tag with the provided user session key.
     * 
     * @param userSessionKey the key to use while building the use session
     *        history tag
     * @return the user session history tag
     */
    public static String buildUserSessionHistoryTag(String userSessionKey) {
        return DOMAIN_SESSION_PAGE_TAG + USER_SESSION_ID_TAG + userSessionKey;
    }
}
