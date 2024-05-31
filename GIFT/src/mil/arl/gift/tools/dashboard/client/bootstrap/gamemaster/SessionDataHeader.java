/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.IconStack;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider.RegisteredSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;

/**
 * A widget containing the data for the session header.
 * 
 * @author sharrison
 */
public class SessionDataHeader extends Composite
        implements ActiveSessionChangeHandler, RegisteredSessionChangeHandler {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionDataHeader.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionDataHeaderUiBinder uiBinder = GWT.create(SessionDataHeaderUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionDataHeaderUiBinder extends UiBinder<Widget, SessionDataHeader> {
    }

    /** The button for deleting the patch file (only for playback sessions) */
    @UiField
    protected IconStack removePatchButton;

    /** Below expectation assessment icon */
    @UiField(provided = true)
    protected AssessmentLevelIcon belowExpectationIcon;

    /** At expectation assessment icon */
    @UiField(provided = true)
    protected AssessmentLevelIcon atExpectationIcon;

    /** Above expectation assessment icon */
    @UiField(provided = true)
    protected AssessmentLevelIcon aboveExpectationIcon;
    
    /** The panel containing ALL the components used to author bookmarks. Used to show/hide said components. */
    @UiField(provided=true)
    protected BookmarkCreatorPanel bookmarkPanel;

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /** The registered session provider instance */
    private final RegisteredSessionProvider registeredSessionProvider = RegisteredSessionProvider.getInstance();
    
    /** The knowledge session used for the data in this header */
    private final AbstractKnowledgeSession knowledgeSession;
    
    /**
     * Constructor
     * 
     * @param knowledgeSession the knowledge session used for this header
     */
    public SessionDataHeader(final AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        this.knowledgeSession = knowledgeSession;
        
        bookmarkPanel = new BookmarkCreatorPanel(knowledgeSession);

        belowExpectationIcon = new AssessmentLevelIcon();
        belowExpectationIcon.setAssessmentLevel(AssessmentLevelEnum.BELOW_EXPECTATION, false);

        atExpectationIcon = new AssessmentLevelIcon();
        atExpectationIcon.setAssessmentLevel(AssessmentLevelEnum.AT_EXPECTATION, false);

        aboveExpectationIcon = new AssessmentLevelIcon();
        aboveExpectationIcon.setAssessmentLevel(AssessmentLevelEnum.ABOVE_EXPECTATION, false);

        initWidget(uiBinder.createAndBindUi(this));     

        if (knowledgeSession.inPastSessionMode()) {
            removePatchButton.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    UiManager.getInstance().displayConfirmDialog("Permanently Remove Changes?",
                            "This will permanently remove all changes made to this past session. This cannot be undone.",
                            "Permanently Remove Changes", "Cancel", new ConfirmationDialogCallback() {
                                @Override
                                public void onDecline() {
                                    /* Do nothing */
                                }

                                @Override
                                public void onAccept() {
                                    LoadingDialogProvider.getInstance().startLoading(LoadingType.PATCH_REMOVAL,
                                            "Removing Changes",
                                            "Removing changes and rebuilding playback experience...");
                                    UiManager.getInstance().getDashboardService().deleteSessionLogPatch(
                                            BrowserSession.getInstance().getBrowserSessionKey(),
                                            new AsyncCallback<GenericRpcResponse<Void>>() {
                                                @Override
                                                public void onSuccess(GenericRpcResponse<Void> result) {
                                                    registeredSessionProvider.updateLogPatchFile(null);
                                                    TimelineProvider.getInstance().reloadTimeline();
                                                    LoadingDialogProvider.getInstance().loadingComplete(LoadingType.PATCH_REMOVAL);
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    logger.warning("Failed to remove the changes because "
                                                            + caught.getMessage());
                                                    LoadingDialogProvider.getInstance().loadingComplete(LoadingType.PATCH_REMOVAL);
                                                }
                                            });
                                }
                            });
                }
            }, ClickEvent.getType());
            
        } else {
            removePatchButton.removeFromParent();
        }
        
        if (!knowledgeSession.inPastSessionMode()) {
            
            //show the global bookmark panel for non-past sessions
            bookmarkPanel.setVisible(true);
            
        } else {
            
            //hide the global bookmark panel for past sessions
            bookmarkPanel.setVisible(false);
        }

        /* Set default state of the remove patch button */
        updateRemovePatchBtnVisibility();

        /* Subscribe to the data providers */
        subscribe();
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to the active session changes */
        activeSessionProvider.addHandler(this);

        /* Note: this must be done after binding */
        registeredSessionProvider.addHandler(this);
    }

    /**
     * Unsubscribe from all providers. This should only be done before the panel
     * is destroyed.
     */
    private void unsubscribe() {
        /* Remove handlers */
        activeSessionProvider.removeHandler(this);
        registeredSessionProvider.removeHandler(this);
    }

    /**
     * Checks the prerequisites for displaying the {@link #removePatchButton}
     * and show or hides the button accordingly.
     */
    private void updateRemovePatchBtnVisibility() {
        boolean isRegistered = registeredSessionProvider
                .isRegistered(knowledgeSession.getHostSessionMember().getDomainSessionId());
        boolean doesPatchExist = registeredSessionProvider.hasLogMetadata()
                && StringUtils.isNotBlank(registeredSessionProvider.getLogMetadata().getLogPatchFile());

        removePatchButton.setVisible(isRegistered && doesPatchExist);
    }
    
    /**
     * Resets the components used to create global bookmarks to their default state
     */
    public void resetBookmarkButton() {
        bookmarkPanel.resetBookmarkButton();
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }

        
    }

    @Override
    public void sessionEnded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }

        /* If the session represented by this panel is no longer active, then
         * unsubscribe from all providers because this widget will be removed */
        if (!activeSessionProvider.isActiveSession(knowledgeSession.getHostSessionMember().getDomainSessionId())) {
            unsubscribe();
        }
        
        /* If an active session ends, prevent the user from adding bookmarks to it afterward */
        if (!knowledgeSession.inPastSessionMode()) {
            bookmarkPanel.setVisible(false);
        }
    }

    @Override
    public void registeredSessionChanged(AbstractKnowledgeSession newSession, AbstractKnowledgeSession oldSession) {
        
        /* Needed to update the remove patch button when a knowledge session's registered state changes */
        updateRemovePatchBtnVisibility();
    }

    @Override
    public void logPatchFileChanged(String logPatchFileName) {
        updateRemovePatchBtnVisibility();
    }
    
    /**
     * Gets the widget used to create bookmarks using this header
     * 
     * @return the bookmark creator. Will not be null.
     */
    public BookmarkCreatorPanel getBookmarkCreator() {
        return bookmarkPanel;
    }
}
