/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingPriority;
import mil.arl.gift.tools.dashboard.client.gamemaster.LoadingDialogProvider.LoadingType;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;

/**
 * A widget that visually indicates an assessment level in a bar-like presentation
 * 
 * @author nroberts
 */
public class AssessmentLevelBar extends Composite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AssessmentLevelBar.class.getName());

    private static AssessmentLevelBarUiBinder uiBinder = GWT.create(AssessmentLevelBarUiBinder.class);

    interface AssessmentLevelBarUiBinder extends UiBinder<Widget, AssessmentLevelBar> {
    }
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {

        String fill();
        
        String vertical();
    }
    
    /** An accessor for this widget's CSS styling rules */
    @UiField
    protected Style style;
    
    /** The first star indicating the lowest assessment level */
    @UiField
    protected Icon star1;
    
    /** The second star indicating a middle assessment level */
    @UiField
    protected Icon star2;
    
    /** The third star indicating the highest assessment level */
    @UiField
    protected Icon star3;
    
    /** The button used to lock and unlock the performance state's assessment level */
    @UiField
    protected Icon lock;
    
    /**
     * The tooltip telling the user how to lock and unlock the performance state's assessment level
     */
    @UiField
    protected ManagedTooltip lockAssessmentTooltip;

    /** The provider of performance assessment information used to populate this widget */
    private PerformanceNodeDataProvider<?> dataProvider;

    /**
     * Creates a new bar displaying the assessment level of the performance node provided by the given
     * provider
     * 
     * @param dataProvider the provider of the performance node data. Cannot be null.
     */
    public AssessmentLevelBar(PerformanceNodeDataProvider<?> dataProvider) {
        
        if(dataProvider == null) {
            throw new IllegalArgumentException("The data provider for this assessment level bar cannot be null");
        }
        
        this.dataProvider = dataProvider;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        star1.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                setAssessmentLevel(AssessmentLevelEnum.BELOW_EXPECTATION);
            }
        });
        
        star2.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                setAssessmentLevel(AssessmentLevelEnum.AT_EXPECTATION);
            }
        });
        
        star3.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                setAssessmentLevel(AssessmentLevelEnum.ABOVE_EXPECTATION);
            }
        });
        
        lock.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                toggleLock();
            }
        });
    }
    
    /**
     * Redraws the bar so that the displayed assessment level matches the underlying performance node's state
     */
    public void redraw() {
        
        PerformanceStateAttribute perfState = dataProvider.getCurrentState().getState();
        AssessmentLevelEnum assessment = perfState.getShortTerm();

        /* If the state hasn't been activated yet, ignore it's current value */
        if (PerformanceNodeStateEnum.UNACTIVATED.equals(perfState.getNodeStateEnum())) {
            assessment = AssessmentLevelEnum.UNKNOWN;
        }

        boolean fill1 = true, fill2 = true, fill3 = true;
                
        if(!AssessmentLevelEnum.ABOVE_EXPECTATION.equals(assessment)) {
            fill3 = false;
            
            if(!AssessmentLevelEnum.AT_EXPECTATION.equals(assessment)) {
                fill2 = false;
                
                if(!AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessment)) {
                    fill1 = false;
                }
            }
        }
        
        String fillStyle = style.fill();
        
        star1.setStyleName(fillStyle, fill1);
        star2.setStyleName(fillStyle, fill2);
        star3.setStyleName(fillStyle, fill3);
        
        boolean locked = dataProvider.getCurrentState().getState().isAssessmentHold();
        lock.setType(locked ? IconType.LOCK : IconType.UNLOCK);
        
        lockAssessmentTooltip.setTitle(locked
                ? "This assessment has been locked so that it cannot be modified by the GIFT system automatically.<br/><br/>Click "
                        + "to unlock this assessment so that GIFT can change it."
                : "This assessment is unlocked and can be modified by the GIFT system automatically.<br/><br/>Click to lock this "
                        + "assessment so that GIFT cannot change it.");
    }
    
    /**
     * Initiates a server call to set the performance node's assessment level to the given level
     * 
     * @param assessment the assessment level that the performance node should be set to. Can be null.
     */
    private void setAssessmentLevel(AssessmentLevelEnum assessment) {
        
        boolean locked = dataProvider.getCurrentState().getState().isAssessmentHold();
        
        if(locked || dataProvider.getCurrentState() == null) {
            return;
        }
        
        final EvaluatorUpdateRequest updateRequest = new EvaluatorUpdateRequest(dataProvider.getCurrentState().getState().getName(),
                BsGameMasterPanel.getGameMasterUserName(), System.currentTimeMillis());
        
        updateRequest.setPerformanceMetric(assessment);
        updateRequest.setAssessmentHold(locked);
        
        applyUpdate(updateRequest);
    }
    
    /**
     * Initiates a server call to toggle whether or not the performance node's assessment level is currently locked
     */
    private void toggleLock() {
        
        if(dataProvider.getCurrentState() == null) {
            return;
        }
        
        boolean locked = dataProvider.getCurrentState().getState().isAssessmentHold();
        
        final EvaluatorUpdateRequest updateRequest = new EvaluatorUpdateRequest(dataProvider.getCurrentState().getState().getName(),
                BsGameMasterPanel.getGameMasterUserName(), System.currentTimeMillis());
        
        updateRequest.setPerformanceMetric(dataProvider.getCurrentState().getState().getShortTerm());
        updateRequest.setAssessmentHold(!locked);
        
        applyUpdate(updateRequest);
    }
    
    /**
     * Applies the changes that the game master user has made to the performance
     * node state through the UI by sending them to GIFT to be pushed to the
     * appropriate domain knowledge session.
     * 
     * @param updateRequest a request containing the changes that are being sent to GIFT. If null, no server call will be made.
     */
    private void applyUpdate(final EvaluatorUpdateRequest updateRequest) {
        
        if(updateRequest == null) {
            return;
        }
        
        if (dataProvider.getKnowledgeSession().inPastSessionMode()) {
            
            ConfirmationDialogCallback confirmCallback = new ConfirmationDialogCallback() {
                
                private void applyPatch(boolean applyToFutureStates) {
                    
                    LoadingDialogProvider.getInstance().startLoading(LoadingType.TIMELINE_REFRESH, LoadingPriority.HIGH, 
                            "Applying Assessments",
                            "Please wait while the updated assessments are rendered onto the timeline...");
                    
                    /* Create patch for changes and apply to timeline */
                    Long timestamp = TimelineProvider.getInstance().getPlaybackTime();
                    UiManager.getInstance().getDashboardService().createLogPatchForEvaluatorUpdate(
                            BrowserSession.getInstance().getBrowserSessionKey(),
                            BsGameMasterPanel.getGameMasterUserName(), timestamp,
                            !Dashboard.getInstance().getSettings().isApplyChangesAtPlayhead(),
                            updateRequest,
                            applyToFutureStates,
                            new AsyncCallback<GenericRpcResponse<String>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    logger.warning("Failed to create patch file because " + caught.getMessage());
                                    
                                    TimelineProvider.getInstance().reloadTimeline();
                                }

                                @Override
                                public void onSuccess(GenericRpcResponse<String> result) {
                                    
                                    if(result.getContent() != null) {
                                        if (logger.isLoggable(Level.INFO)) {
                                            logger.info("Successfully wrote patch file for evaluator update.");
                                        }
    
                                        /* Update log metadata patch file name */
                                        RegisteredSessionProvider.getInstance().updateLogPatchFile(result.getContent());
                                    }
                                    
                                    if(!result.getWasSuccessful()) {
                                        
                                        boolean isLrsError = result.getContent() != null;
                                        String title = isLrsError 
                                                ? "Unable to publish assessment to external system"
                                                : "Failed to save assessment";
                                        
                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                title,
                                                result.getException().getReason(), 
                                                result.getException().getDetails(),
                                                result.getException().getErrorStackTrace(),
                                                null);
                                    }
                                    
                                    TimelineProvider.getInstance().reloadTimeline();
                                }
                            });
                }
                
                @Override
                public void onDecline() {
                    applyPatch(false);
                }
                
                @Override
                public void onAccept() {
                    applyPatch(true);
                }
            };
            
            PerformanceNodeMetricsPanel.promptApplyIfNeeded(updateRequest, dataProvider.getCurrentState().getState(), confirmCallback);
            
        } else {
            /* Send live message */
            BrowserSession.getInstance()
                    .sendWebSocketMessage(new DashboardMessage(updateRequest, dataProvider.getKnowledgeSession()));
        }
    }
    
    /**
     * Sets whether this bar should display its content vertically
     * 
     * @param vertical whether content should be displayed vertically
     */
    public void setVertical(boolean vertical) {
        
        if(vertical) {
            addStyleName(style.vertical());
            
        } else {
            removeStyleName(style.vertical());
        }
    }
}
