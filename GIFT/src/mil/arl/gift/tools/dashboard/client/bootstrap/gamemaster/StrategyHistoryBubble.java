/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;

/**
 * A display bubble that is shown to provide information on strategies that have
 * already been processed.
 *
 * @author sharrison
 */
public class StrategyHistoryBubble extends StrategyBubble {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyHistoryBubble.class.getName());

    /** The {@link StrategyHistoryItem} used to populate this widget */
    private final StrategyHistoryItem strategyHistoryItem;

    /** The history label to provide additional information to the user */
    private final HTML historyLabelHTML = new HTML();
    
    /** whether to display the time information for a strategy instance */
    private static final boolean displayTimeLabel = false;

    /**
     * Constructor.
     *
     * @param strategyHistoryItem the history item that contains the data from a
     *        processed strategy. Can't be null.
     * @param knowledgeSession the knowledge session used to populate this
     *        panel. Can't be null.
     */
    public StrategyHistoryBubble(final StrategyHistoryItem strategyHistoryItem,
            AbstractKnowledgeSession knowledgeSession) {
        super(strategyHistoryItem.getStrategy(), false, false, true, knowledgeSession, null);
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(".ctor(");
            List<Object> params = Arrays.<Object>asList(strategyHistoryItem, knowledgeSession.getNodeIdToNameMap(),
                    knowledgeSession.getTrainingAppType(), knowledgeSession.getSessionType());
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        this.strategyHistoryItem = strategyHistoryItem;

        /* Do not want the user to be able to select/deselect the checkboxes by
         * clicking the label */
        labelHandlerRegistration.removeHandler();

        refreshTimerWidgets();
        mainPanel.add(historyLabelHTML);

        updateCheckBoxValues(strategyHistoryItem.getApprovedActivities());
    }

    @Override
    public void buildMetricPanel(FlowPanel metricPanel) {
        // do nothing. We want no metrics for the strategy history
    }

    @Override
    public void refreshTimerWidgets() {
        historyLabelHTML.setHTML(buildHistoryLabel(strategyHistoryItem));
    }

    /**
     * Updates the UI checkboxes to show the approved activities as checked and
     * the declined activities as unchecked.
     *
     * @param approvedActivities the strategy activities that were approved.
     */
    private void updateCheckBoxValues(Collection<Serializable> approvedActivities) {
        if (approvedActivities.isEmpty()) {
            strategyCheckBox.setValue(false);
            return;
        }

        boolean foundSelectedActivity = false;
        boolean foundUnselectedActivity = false;
        for (Entry<Serializable, ActivityPanelManager> activityEntry : activityToBuilderMap.entrySet()) {
            if (approvedActivities.contains(activityEntry.getKey())) {
                foundSelectedActivity = true;
                activityEntry.getValue().getCheckBox().setValue(true);
            } else {
                foundUnselectedActivity = true;
            }
        }

        if (foundSelectedActivity && foundUnselectedActivity) {
            /* Indeterminate state */
            strategyCheckBox.setValue(null);
        } else {
            /* Set true if all activities were selected; set false if none were
             * selected */
            strategyCheckBox.setValue(foundSelectedActivity);
        }
    }

    /**
     * Builds the label that indicates approve/decline, the user name of the
     * person who performed the action, and how long ago that action was
     * performed.
     *
     * @param strategyHistoryItem the strategy history container.
     * @return the built label
     */
    private SafeHtml buildHistoryLabel(StrategyHistoryItem strategyHistoryItem) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();

        // only show declined/approved if done by a person and NOT when done by GIFT (automated)
        if (StringUtils.isNotBlank(strategyHistoryItem.getUserPerformed())) {
            sb.appendHtmlConstant(strategyHistoryItem.getApprovedActivities().isEmpty() ? "Declined" : "Approved");
            sb.appendHtmlConstant(" by '").appendHtmlConstant(strategyHistoryItem.getUserPerformed())
                    .appendHtmlConstant("'");
        }
        
        if(displayTimeLabel){

            long timeDiff;
            if (inPastSessionMode()) {
                /* Get timeline current play time */
                timeDiff = TimelineProvider.getInstance().getPlaybackTime() - strategyHistoryItem.getTimePerformed();
            } else {
                /* Get current time */
                timeDiff = new Date().getTime() - strategyHistoryItem.getTimePerformed();
            }
    
            int seconds = ((Long) (timeDiff / 1000)).intValue();
            
            //adjust the wording of the string based on whether the strategy was invoked in the past or future
            String relativeString = " ago";
            
            if(seconds < 0) {
                relativeString = " in the future";
            }
    
            String timeDisplay = FormattedTimeBox.getDisplayText(Math.abs(seconds), true);
            
            sb.appendHtmlConstant(" ").appendHtmlConstant(timeDisplay).appendHtmlConstant(relativeString);
        }

        return sb.toSafeHtml();
    }

    @Override
    public boolean setSendCommand(Command command) {
        throw new UnsupportedOperationException("The StrategyHistoryBubble cannot implement send functionality.");
    }

    @Override
    public void makeGameMasterDriven(Command sendCommand, String author) {
        throw new UnsupportedOperationException("The StrategyHistoryBubble cannot implement send functionality.");
    }
    
    /**
     * Gets the {@link StrategyHistoryItem} used to populate this widget
     * 
     * @return the history item. Cannot be null.
     */
    public StrategyHistoryItem getHistoryItem() {
        return strategyHistoryItem;
    }

    @Override
    protected boolean isActivityPresentFeedbackMessage(Serializable activity) {
        /* We never want to edit a message feedback so always return false */
        return false;
    }
}