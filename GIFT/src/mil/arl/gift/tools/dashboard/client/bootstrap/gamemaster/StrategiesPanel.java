/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.HiddenEvent;
import org.gwtbootstrap3.client.shared.event.HiddenHandler;
import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.shared.event.ShownHandler;
import org.gwtbootstrap3.client.ui.Badge;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyPlacement;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Audio;
import generated.dkf.Feedback;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Message;
import generated.dkf.Message.Delivery;
import generated.dkf.Strategy;
import generated.dkf.StrategyHandler;
import generated.dkf.Team;
import generated.dkf.ToObserverController;
import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil;
import mil.arl.gift.common.gwt.client.util.TeamsUtil;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil.ActivityType;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil.StrategyActivityIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PriorityPanel.WidgetBuilder;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.RunState;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.StrategyProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.Component;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.PermissionUpdateHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider.SessionStateUpdateHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.StrategyProvider.StrategyReceivedHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedStrategyCache;

/**
 * The panel that contains the different strategy options. This includes all
 * current and past {@link Strategy} requests and preset strategies.
 *
 * @author tflowers
 */
public class StrategiesPanel extends Composite implements ActiveSessionChangeHandler, PermissionUpdateHandler, SessionStateUpdateHandler, StrategyReceivedHandler{

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategiesPanel.class.getName());

    /** The binder that combines this java class with the ui.xml */
    private static StrategiesPanelUiBinder uiBinder = GWT.create(StrategiesPanelUiBinder.class);

    /** The binder that combines the java class with the ui.xml */
    interface StrategiesPanelUiBinder extends UiBinder<Widget, StrategiesPanel> {
    }

    /**
     * The default class for the strategy handler. Nick: I don't like
     * hard-coding this, but I just can't find a good way to get the default
     * strategy handler class on the client side
     */
    private final static String DEFAULT_STRATEGY_HANDLER_CLASS = "domain.knowledge.strategy.DefaultStrategyHandler";
    
    /** 30 second delay for the {@link #refreshTimer} */
    private final static int TIMER_DELAY = 30000;

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service.
     */
    private final DashboardServiceAsync dashboardService = UiManager.getInstance().getDashboardService();

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /** The panel containing current strategy suggestions */
    @UiField(provided = true)
    protected PriorityPanel<Strategy, StrategyBubble> notificationActionPanel = new PriorityPanel<>(
            new WidgetBuilder<Strategy, StrategyBubble>() {
                @Override
                public StrategyBubble buildWidget(final Strategy strategy) {
                    final List<Serializable> EMPTY_COLLECTION = new ArrayList<>();

                    final StrategyBubble strategyBubble = new StrategyBubble(strategy, true, true,
                            knowledgeSession, team) {
                        @Override
                        public void buildMetricPanel(FlowPanel metricPanel) {
                            StrategyMetrics strategyMetrics = strategyMetricsMap.get(strategy);
                            if (strategyMetrics == null) {
                                return;
                            }

                            final int approvedCount = strategyMetrics.getApprovedCount();
                            final int recommendedCount = strategyMetrics.getRecommendedCount();
                            final String lastApprover = strategyMetrics.getLastApprover();
                            final Date lastApprovedTime = strategyMetrics.getLastApprovedTime();

                            SafeHtmlBuilder sb = new SafeHtmlBuilder();
                            sb.appendHtmlConstant("Approved ");
                            sb.append(bold(String.valueOf(approvedCount))).append(bold(" of ")).append(bold(String.valueOf(recommendedCount)));
                            sb.appendHtmlConstant(recommendedCount == 1 ? " time" : " times");
                            metricPanel.add(new HTML(sb.toSafeHtml()));

                            /* Check that the strategy has been approved at
                             * least once */
                            if (lastApprovedTime != null) {
                                sb = new SafeHtmlBuilder();

                                sb.appendHtmlConstant("Last approved");
                                if (StringUtils.isNotBlank(lastApprover)) {
                                    sb.appendHtmlConstant(" by ").append(bold(lastApprover));
                                }

                                long timeDiff;
                                if (knowledgeSession.inPastSessionMode()) {
                                    /* Get timeline current play time */
                                    timeDiff = TimelineProvider.getInstance().getPlaybackTime()
                                            - lastApprovedTime.getTime();
                                } else {
                                    /* Get current time */
                                    timeDiff = new Date().getTime() - lastApprovedTime.getTime();
                                }

                                int seconds = ((Long) (timeDiff / 1000)).intValue();
                                String timeDisplay = FormattedTimeBox.getDisplayText(seconds, true);

                                sb.appendHtmlConstant(" ").append(bold(timeDisplay)).appendHtmlConstant(" ago.");
                                metricPanel.add(new HTML(sb.toSafeHtml()));
                            }
                        }
                    };

                    strategyBubble.setSendCommand(new Command() {
                        @Override
                        public void execute() {
                            sendSingleBubble(strategyBubble);

                            /* Remove the bubble from the UI and from the
                             * collection */
                            notificationActionPanel.remove(strategyBubble.getStrategy());

                            /* Create the history item for the strategy */
                            StrategyHistoryItem historyItem = new StrategyHistoryItem(strategyBubble.getStrategy(),
                                    strategyBubble.getSelectedActivities(), UiManager.getInstance().getUserName());
                            addToHistoryPanel(historyItem);
                        }
                    });

                    strategyBubble.addCloseButtonHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            StrategyHistoryItem historyItem = new StrategyHistoryItem(strategy, EMPTY_COLLECTION,
                                    UiManager.getInstance().getUserName());

                            notificationActionPanel.remove(strategyBubble.getStrategy());
                            addToHistoryPanel(historyItem);
                            fireNotificationCountEvent();
                        }
                    });

                    strategyBubble.addChangeCallback(new ChangeCallback<Void>() {
                        @Override
                        public void onChange(Void newValue, Void oldValue) {
                            boolean hasSelectedActivity = !strategyBubble.getSelectedActivities().isEmpty();
                            boolean approveButtonVisible = approveStrategyRequestsButton.isVisible();

                            /* Update button visibility if an activity is
                             * selected and the approve button is hidden or if
                             * no activity is selected and the approve button is
                             * visible */
                            if (hasSelectedActivity != approveButtonVisible) {
                                updateSuggestionButtonsVisibility();
                            }
                        }
                    });

                    /* If the parent panel is open and the strategy request
                     * panel is being shown, then the strategy bubble is already
                     * visible and being shown to the user */
                    strategyBubble.setHasBeenSeen(isParentPanelVisible && strategyRequestCollapse.isShown());
                    return strategyBubble;
                }
            }, new Comparator<Strategy>() {

                @Override
                public int compare(Strategy o1, Strategy o2) {
                    /* No sort, insert at top */
                    return 0;
                }
            });

    /** The panel containing current strategy suggestions */
    @UiField(provided = true)
    protected PriorityPanel<Strategy, StrategyBubble> presetStrategyPanel = new PriorityPanel<>(
            new WidgetBuilder<Strategy, StrategyBubble>() {
                @Override
                public StrategyBubble buildWidget(final Strategy strategy) {
                    final StrategyBubble strategyBubble = new StrategyBubble(strategy, false, false,
                            knowledgeSession, team) {
                        @Override
                        public void buildMetricPanel(FlowPanel metricPanel) {
                            StrategyMetrics strategyMetrics = strategyMetricsMap.get(strategy);
                            if (strategyMetrics == null) {
                                return;
                            }

                            final int approvedCount = strategyMetrics.getApprovedCount();

                            if (approvedCount > 0) {
                                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                                sb.appendHtmlConstant("This scenario inject has been manually initiated ");
                                sb.append(bold(String.valueOf(approvedCount))).append(bold((approvedCount == 1 ? " time" : " times")));
                                sb.appendHtmlConstant(".");
                                metricPanel.add(new HTML(sb.toSafeHtml()));
                            }
                        }
                    };

                    strategyBubble.setSendCommand(new Command() {
                        @Override
                        public void execute() {
                            sendSingleBubble(strategyBubble);

                            /* Create the history item for the strategy */
                            StrategyHistoryItem historyItem = new StrategyHistoryItem(strategyBubble.getStrategy(),
                                    strategyBubble.getSelectedActivities(), UiManager.getInstance().getUserName());
                            addToHistoryPanel(historyItem);
                        }
                    });

                    strategyBubble.addChangeCallback(new ChangeCallback<Void>() {
                        @Override
                        public void onChange(Void newValue, Void oldValue) {
                            updatePresetStrategiesButtonVisibility();
                        }
                    });

                    return strategyBubble;
                }
            }, new Comparator<Strategy>() {

                @Override
                public int compare(Strategy o1, Strategy o2) {
                    /* No sort, insert at top */
                    return 0;
                }
            });

    /** The panel containing past strategy suggestions */
    @UiField(provided = true)
    protected PriorityPanel<StrategyHistoryItem, StrategyHistoryBubble> notificationHistoryPanel = new PriorityPanel<>(
            new WidgetBuilder<StrategyHistoryItem, StrategyHistoryBubble>() {

                @Override
                public StrategyHistoryBubble buildWidget(final StrategyHistoryItem element) {
                    
                    final StrategyHistoryBubble strategyBubble = new StrategyHistoryBubble(element, knowledgeSession);
                    
                    if(timelineNavigator != null) {
                        
                        /* If navigating through the session timeline is possible, then allow the user to jump
                         * to the time when a strategy was applied whenever they click said strategy to expand it */
                        strategyBubble.addDomHandler(new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                if(strategyBubble.isBubbleExpanded()) { 
                                    
                                    setSelectedHistoryItem(strategyBubble);
                                    
                                    if(timelineNavigator != null) {
                                        timelineNavigator.seekTo(element.getTimePerformed());
                                    }
                                }
                            }
                            
                        }, ClickEvent.getType());
                    }
                    
                    return strategyBubble;
                }
            }, new Comparator<StrategyHistoryItem>() {

                @Override
                public int compare(StrategyHistoryItem left, StrategyHistoryItem right) {
                    
                    if(knowledgeSession.inPastSessionMode()) {
                        
                        /* Show the items in the order they they arrive from the history */
                        return Long.compare(left.getTimePerformed(), right.getTimePerformed());
                        
                    } else {
                    
                        /* Reverse the comparison to ensure the newest history is
                         * shown first. */
                        return Long.compare(right.getTimePerformed(), left.getTimePerformed());
                    }
                }
            });

    /** The header for the current strategy requests */
    @UiField
    protected PanelHeader strategyRequestHeader;

    /** The collapse containing the current strategy requests */
    @UiField
    protected Collapse strategyRequestCollapse;

    /** The button used to approve all checked strategy requests */
    @UiField
    protected Button approveStrategyRequestsButton;

    /** The button used to dismiss all current strategy requests */
    @UiField
    protected Button dismissAllStrategyRequestsButton;

    /** The button used to select all strategy requests */
    @UiField
    protected Button checkAllStrategyRequestsButton;

    /** The button used to deselect all strategy requests */
    @UiField
    protected Button uncheckAllStrategyRequestsButton;

    /** The header for the preset strategies */
    @UiField
    protected PanelHeader presetHeader;

    /** The collapse for the preset strategies */
    @UiField
    protected Collapse presetCollapse;

    /**
     * Icon when clicked will create a new preset strategy with 1 feedback
     * activity
     */
    @UiField
    protected Icon createNewPresetButton;

    /**
     * The button used to send the selected strategy activities to be executed
     */
    @UiField
    protected Button sendPresetStrategiesButton;

    /** The button used to reset the strategy activities to be unselected */
    @UiField
    protected Button resetPresetStrategiesButton;

    /** The header for the history of applied and dismissed strategies */
    @UiField
    protected PanelHeader historyHeader;

    /** The collapse for the history of applied and dismissed strategies */
    @UiField
    protected Collapse historyCollapse;
    
    /** the panel for the strategy requests */
    @UiField
    protected Panel strategyRequestsPanel;
    
    /** the panel for the strategy presets */
    @UiField
    protected Panel strategyPresetsPanel;
    
    /** Displays how many notifications are awaiting user action */
    @UiField
    protected Badge notificationBadge;
    
    /** The checkbox that indicates if the user is in 'auto' mode or not */
    @UiField
    protected CheckBoxButton autoCheckBox;

    /** The current knowledge session */
    private final AbstractKnowledgeSession knowledgeSession;

    /** The structure of the team for the {@link #knowledgeSession} */
    private final Team team;

    /** Maps the strategies to their respective metrics */
    private final Map<Strategy, StrategyMetrics> strategyMetricsMap = new HashMap<>();

    /**
     * Flag indicating if this panel's parent panel (and any ancestor panels)
     * are visible. E.g. all panels that could be preventing this panel from
     * being seen are open.
     */
    private boolean isParentPanelVisible = false;

    /** The navigator that can be used to jump to a specific point in the session's timeline */
    private TimelineNavigator timelineNavigator;
    
    /** The strategy history item that is currently selected (i.e. expanded) */
    private StrategyHistoryBubble selectedHistoryItem = null;
    
    /** Maps a strategy name to its last active notification. */
    private Map<String, Notify> strategyNameToNotification = new HashMap<>();
    
    /** Timer to refresh certain display widgets after a set period of time */
    private final Timer refreshTimer = new Timer() {
        @Override
        public void run() {
            /* Notify strategy suggestions panel to refresh timer widgets */
            refreshTimerWidgets();
        }
    };
    
    /**
     * Constructs a {@link StrategiesPanel} with no suggestions.
     * 
     * @param knowledgeSession the knowledge session used to populate this data
     *        panel.
     */
    public StrategiesPanel(final AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("StrategiesPanel(" + knowledgeSession.getNameOfSession() + ")");
        }

        this.knowledgeSession = knowledgeSession;
        this.team = TeamsUtil.convertToDkfTeam(knowledgeSession.getTeamStructure());

        initWidget(uiBinder.createAndBindUi(this));

        strategyRequestHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCollapse(strategyRequestCollapse.isShown() ? null : strategyRequestCollapse);
            }
        }, ClickEvent.getType());

        presetHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCollapse(presetCollapse.isShown() ? null : presetCollapse);
            }
        }, ClickEvent.getType());

        historyHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCollapse(historyCollapse.isShown() ? null : historyCollapse);
            }
        }, ClickEvent.getType());
        
        historyCollapse.addHiddenHandler(new HiddenHandler() {
            
            @Override
            public void onHidden(HiddenEvent event) {
                
                //deselect the currently selected strategy when the strategy history is hidden
                setSelectedHistoryItem(null);
            }
        });

        createNewPresetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();

                final StrategyHandler strategyHandler = new StrategyHandler();
                strategyHandler.setImpl(DEFAULT_STRATEGY_HANDLER_CLASS);

                final Feedback feedback = new Feedback();
                feedback.setFeedbackPresentation(new Message());

                final InstructionalIntervention ii = new InstructionalIntervention();
                ii.setStrategyHandler(strategyHandler);
                ii.setFeedback(feedback);

                final Strategy strategy = new Strategy();
                strategy.setName("Custom Feedback");
                strategy.getStrategyActivities().add(ii);

                addPresetStrategies(null, Arrays.asList(strategy), true);
                final StrategyBubble createdBubble = presetStrategyPanel.getWidget(strategy);
                createdBubble.editFeedback(ii, true, new Command() {
                    @Override
                    public void execute() {
                        presetStrategyPanel.remove(strategy);
                    }
                });
            }
        });

        sendPresetStrategiesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                List<StrategyToApply> strategiesToSend = new ArrayList<>();
                final String approverUsername = UiManager.getInstance().getUserName();

                for (StrategyBubble bubble : presetStrategyPanel.getWidgetItems()) {
                    Strategy newStrategy = createStrategyFromSelectedActivities(bubble, true);
                    if (newStrategy == null) {
                        continue;
                    }

                    strategiesToSend.add(new StrategyToApply(newStrategy, Constants.GAMEMASTER_SOURCE, approverUsername));
                    strategyMetricsMap.get(bubble.getStrategy()).incrementApprovedCount(approverUsername);

                    /* Create the history item for the strategy */
                    StrategyHistoryItem historyItem = new StrategyHistoryItem(newStrategy,
                            newStrategy.getStrategyActivities(), approverUsername);
                    addToHistoryPanel(historyItem, approverUsername);
                }

                if (strategiesToSend.isEmpty()) {
                    return;
                }

                final ApplyStrategies payload = new ApplyStrategies(strategiesToSend, Constants.GAMEMASTER_SOURCE, approverUsername);
                BrowserSession.getInstance().sendWebSocketMessage(new DashboardMessage(payload, knowledgeSession));
                revertToDefaultValues();
                displayToast(payload);
            }
        });

        resetPresetStrategiesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                revertToDefaultValues();
            }
        });
        
        if(Dashboard.getInstance().getServerProperties().getLessonLevel() == LessonLevelEnum.RTA){
            // game master is not in charge of approving strategies when in RTA lesson level, the external
            // system connected via the GW module is.
            // uncheck and hide the checkbox
            autoCheckBox.setValue(false);
            autoCheckBox.setVisible(false);
            
            Dashboard.getInstance().getSettings().setAutoApplyStrategies(false);
            
        }else{

            autoCheckBox.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation(); //don't toggle the header when the auto checkbox is clicked
                }
            });
            autoCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    /* send new auto mode state to the server */
                    boolean isAuto = Boolean.TRUE.equals(autoCheckBox.getValue());
                    Dashboard.getInstance().getSettings().setAutoApplyStrategies(isAuto);
                    sendAutoMode(isAuto);
                    
                    if(!isAuto) {
						/* Automatically show strategy requests if auto-applying is disabled */
                        strategyRequestCollapse.show();
                    }
                }
            });
        }
        
        autoCheckBox.setValue(Dashboard.getInstance().getSettings().isAutoApplyStrategies());
        
        if(!Dashboard.getInstance().getSettings().isAutoApplyStrategies()) {
            
            /* If auto-applying strategies is turned off, open the request approval panel automatically */
            strategyRequestCollapse.show();
        }
        
        /* Restart timer */
        refreshTimer.scheduleRepeating(TIMER_DELAY);

        openStrategyHistoryPanel();

        /* Subscribe to the data providers */
        subscribe();
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Note: this must be done after binding */

        /* Subscribe to the active session changes */
        activeSessionProvider.addHandler(this);

        /* Subscribe to permission changes */
        PermissionsProvider.getInstance().addHandler(this);
        
        /* Subscribe to the knowledge session state updates */
        SessionStateProvider.getInstance().addHandler(this);

        /* Subscribe to incoming strategy messages */
        StrategyProvider.getInstance().addHandler(this);
    }

    /**
     * Unsubscribe from all providers. This should only be done before the panel
     * is destroyed.
     */
    private void unsubscribe() {
        /* Remove handlers */
        activeSessionProvider.removeHandler(this);
        PermissionsProvider.getInstance().removeHandler(this);
        SessionStateProvider.getInstance().removeHandler(this);
        StrategyProvider.getInstance().removeHandler(this);
    }

    /**
     * Update the game master UI based on the components that should be allowed/disallowed from
     * being used.
     * 
     * @param disallowedComponents - collection of enumerated components that should be hidden.  Won't
     * be null but can be empty.
     */
    private void setDisallowedComponents(Set<Component> disallowedComponents){
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Setting disallowedComponents to " + disallowedComponents);
        }

        //update UI components
        strategyRequestsPanel.setVisible(!disallowedComponents.contains(Component.STRATEGY_REQUESTS));
        strategyPresetsPanel.setVisible(!disallowedComponents.contains(Component.STRATEGY_PRESETS));
    }

    /**
     * Reverts the {@link StrategyBubble strategy bubbles} in this widget to
     * their default state.
     */
    private void revertToDefaultValues() {
        for (StrategyBubble bubble : presetStrategyPanel.getWidgetItems()) {
            bubble.revertToDefaultValue();
        }
    }

    /**
     * Opens the provided collapse and closes all others.
     *
     * @param collapse the collapse to open.
     */
    private void openCollapse(Collapse collapse) {
        boolean hideOthers = false;

        /* Need to explicitly set the visibility of header buttons instead of
         * just calling updateSuggestionButtonsVisibility() or
         * updatePresetStrategiesButtonVisibility() because the collapse might
         * not have finished its animation of opening/closing yet and this could
         * lead to an incorrect 'open' state if we try checking too fast. */

        if (strategyRequestCollapse.equals(collapse)) {
            setNotificationBubblesSeen();
            strategyRequestCollapse.show();
            setStrategyRequestButtonsVisibility(true);
            hideOthers = true;
        } else {
            strategyRequestCollapse.hide();
            setStrategyRequestButtonsVisibility(false);
        }

        if (!hideOthers && presetCollapse.equals(collapse)) {
            presetCollapse.show();
            setPresetStrategiesButtonVisibility(true);
            hideOthers = true;
        } else {
            presetCollapse.hide();
            setPresetStrategiesButtonVisibility(false);
        }

        if (!hideOthers && historyCollapse.equals(collapse)) {
            historyCollapse.show();
            hideOthers = true;
        } else {
            historyCollapse.hide();
        }
    }

    /**
     * Handler invoked when the user has requested that all currently selected
     * strategies and activities be applied.
     *
     * @param event The event object containing details about the click. Can't
     *        be null.
     */
    @UiHandler("approveStrategyRequestsButton")
    protected void onApplyClicked(ClickEvent event) {
        List<StrategyToApply> strategiesToApply = new ArrayList<>();
        final String approverUsername = UiManager.getInstance().getUserName();
        
        /* default to a fixed reason for this strategy if a more appropriate reason can't be derived from the strategies themselves */
        String trigger = Constants.MANUALLY_APPROVED;

        /* Collect each of the strategies that have been marked for
         * application. */
        Iterator<StrategyBubble> iter = notificationActionPanel.getWidgetItems().iterator();
        while (iter.hasNext()) {
            /* Get the current strategy bubble */
            final StrategyBubble strategyBubble = iter.next();
            final Strategy strategyRequested = strategyBubble.getStrategy();

            /* If there are any selected activities, create a strategy to apply
             * for them; skip any strategies that have nothing selected. */
            Collection<Serializable> activities = strategyBubble.getSelectedActivities();
            if (activities.isEmpty()) {
                continue;
            }

            Strategy strategy = new Strategy();
            strategy.setName(strategyRequested.getName());
            strategy.getStrategyActivities().addAll(activities);
            strategiesToApply.add(new StrategyToApply(strategy, trigger, approverUsername));
            if (strategyMetricsMap.containsKey(strategyRequested)) {
                strategyMetricsMap.get(strategyRequested).incrementApprovedCount(approverUsername);
            }

            /* Remove the bubble from the UI and from the collection */
            notificationActionPanel.remove(strategyRequested);

            /* Create the history item for the strategy */
            StrategyHistoryItem historyItem = new StrategyHistoryItem(strategyRequested, activities, approverUsername);
            addToHistoryPanel(historyItem);
        }

        /* If no strategies were found to apply, do nothing else. */
        if (strategiesToApply.isEmpty()) {
            return;
        }else if(strategiesToApply.size() == 1){
            // use the single strategy's name as the reason for this strategy
            trigger = strategiesToApply.get(0).getStrategy().getName();
        }

        fireNotificationCountEvent();

        /* Send the apply strategies message to the server */
        ApplyStrategies applyStrategies = new ApplyStrategies(strategiesToApply, trigger, approverUsername);
        BrowserSession.getInstance().sendWebSocketMessage(new DashboardMessage(applyStrategies, knowledgeSession));

        event.stopPropagation();
    }

    /**
     * Handler invoked when the user has requested that all strategies and
     * activities be ignored.
     *
     * @param event The event object containing details about the click. Can't
     *        be null.
     */
    @UiHandler("dismissAllStrategyRequestsButton")
    protected void onDismissClicked(ClickEvent event) {
        Date timePerformed = new Date();
        String userName = UiManager.getInstance().getUserName();
        ArrayList<Serializable> approvedActivities = new ArrayList<>();

        /* Convert each StrategyBubble into a StrategyHistoryItem */
        List<Strategy> notificationStrategies = notificationActionPanel.getItems();
        Collection<StrategyHistoryItem> strategyHistories = new ArrayList<>(notificationStrategies.size());
        for (Strategy strategy : notificationStrategies) {
            StrategyHistoryItem strategyHistoryItem = new StrategyHistoryItem(strategy, approvedActivities,
                    timePerformed.getTime(), userName);

            strategyHistories.add(strategyHistoryItem);
        }

        notificationActionPanel.clear();
        addStrategyHistoryToHistoryPanel(strategyHistories, null, timePerformed.getTime());

        fireNotificationCountEvent();

        event.stopPropagation();
    }

    /**
     * Handler for the {@link #checkAllStrategyRequestsButton}. Marks all
     * currently suggested strategies as selected.
     *
     * @param event The {@link ClickEvent} that is being handled. Can't be null.
     */
    @UiHandler("checkAllStrategyRequestsButton")
    protected void onCheckAllSuggestions(ClickEvent event) {
        event.stopPropagation();
        for (StrategyBubble stratBubble : notificationActionPanel.getWidgetItems()) {
            stratBubble.setValue(true);
        }

        updateSuggestionButtonsVisibility();
    }

    /**
     * Handler for the {@link #uncheckAllStrategyRequestsButton}. Marks all
     * currently suggested strategies as deselected.
     *
     * @param event The {@link ClickEvent} that is being handled. Can't be null.
     */
    @UiHandler("uncheckAllStrategyRequestsButton")
    protected void onUncheckAllSuggestions(ClickEvent event) {
        event.stopPropagation();
        for (StrategyBubble stratBubble : notificationActionPanel.getWidgetItems()) {
            stratBubble.setValue(false);
        }

        approveStrategyRequestsButton.setVisible(false);
    }

    /**
     * Adds a {@link Collection} of {@link Strategy} as suggestions to the
     * {@link #notificationActionPanel}.
     *
     * @param strategies The {@link Collection} of {@link Strategy} to suggest.
     *        Can't be null. An empty collection performs no operation.
     * @param evaluator The username of the person who made this request
     */
    public void addStrategyRequests(Collection<Strategy> strategies, String evaluator) {
        /* If there are no strategies, return early since no additional work
         * will be done. */
        if (strategies.isEmpty()) {
            return;
        }

        createMetricsForNewStrategies(strategies, notificationActionPanel);

        /* increment the recommendation count */
        for (Strategy strategy : strategies) {
            strategyMetricsMap.get(strategy).incrementRecommendedCount();
        }
        
        // Ignore messages sent from this controller
        if (UiManager.getInstance().getUserName().equals(evaluator)) {
            return;
        }

        notificationActionPanel.addAll(strategies);
        fireNotificationCountEvent();
    }

    /**
     * Creates new {@link StrategyMetrics} for each provided {@link Strategy
     * strategies} if it does not already have one.
     *
     * @param strategies the {@link Strategy strategies} to create metrics for.
     * @param priorityPanel the {@link PriorityPanel} that will contain these
     *        strategies.
     */
    private void createMetricsForNewStrategies(Collection<Strategy> strategies,
            final PriorityPanel<Strategy, StrategyBubble> priorityPanel) {
        for (final Strategy newStrategy : strategies) {
            /* Metric already exists for this strategy */
            if (strategyMetricsMap.containsKey(newStrategy)) {
                continue;
            }

            strategyMetricsMap.put(newStrategy, new StrategyMetrics(newStrategy.getName(), new ChangeCallback<Void>() {
                @Override
                public void onChange(Void newValue, Void oldValue) {
                    /* Refresh metric panel on change for each bubble that uses
                     * this metric */
                    for (StrategyBubble bubble : priorityPanel.getWidgetItems()) {
                        if (StringUtils.equalsIgnoreCase(newStrategy.getName(), bubble.getStrategy().getName())) {
                            bubble.refreshMetricPanel();
                        }
                    }
                }
            }));
        }
    }

    /**
     * Clears both the {@link #notificationActionPanel} and the
     * {@link #notificationHistoryPanel}. Should be called when switching domain
     * sessions.
     */
    public void reset() {
        strategyMetricsMap.clear();

        notificationActionPanel.clear();
        notificationHistoryPanel.clear();
        fireNotificationCountEvent();

        presetStrategyPanel.clear();

        updateSuggestionButtonsVisibility();
        updatePresetStrategiesButtonVisibility();
    }

    /**
     * Updates the visibility of the {@link #checkAllStrategyRequestsButton},
     * the {@link #uncheckAllStrategyRequestsButton}, the
     * {@link #approveStrategyRequestsButton}, and the
     * {@link #dismissAllStrategyRequestsButton} based on the content of
     * {@link #sessionEnded} and if any activities are selected.
     */
    private void updateSuggestionButtonsVisibility() {
        setStrategyRequestButtonsVisibility(strategyRequestCollapse.isShown());
    }

    /**
     * Updates the visibility of the {@link #checkAllStrategyRequestsButton},
     * the {@link #uncheckAllStrategyRequestsButton}, the
     * {@link #approveStrategyRequestsButton}, and the
     * {@link #dismissAllStrategyRequestsButton} based on the content of
     * {@link #sessionEnded} and if any activities are selected.
     *
     * @param visible true to attempt to set the buttons to visible; false hide
     *        them.
     */
    private void setStrategyRequestButtonsVisibility(boolean visible) {
        if (visible) {
            final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
            boolean enabled = RunState.RUNNING.equals(activeSessionProvider.getRunState(dsId))
                    && !notificationActionPanel.isEmpty();

            /* Check/uncheck all buttons are visible if any notifications are in
             * the panel */
            uncheckAllStrategyRequestsButton.setVisible(enabled);
            checkAllStrategyRequestsButton.setVisible(enabled);

            if (enabled) {
                /* See if any activities are selected */
                boolean foundSelected = false;
                for (StrategyBubble bubble : notificationActionPanel.getWidgetItems()) {
                    if (!bubble.getSelectedActivities().isEmpty()) {
                        foundSelected = true;
                        break;
                    }
                }
                enabled = foundSelected;
            }

            /* Approve button is visible iff there is at least one selected
             * activity */
            approveStrategyRequestsButton.setVisible(enabled);

            /* Dismiss button is visible if any notifications are in the panel.
             * Ignores the allowOutgoingMessages flag. */
            dismissAllStrategyRequestsButton.setVisible(!notificationActionPanel.isEmpty());
        } else {
            approveStrategyRequestsButton.setVisible(false);
            dismissAllStrategyRequestsButton.setVisible(false);
            checkAllStrategyRequestsButton.setVisible(false);
            uncheckAllStrategyRequestsButton.setVisible(false);
        }
    }

    /**
     * Safely executes each handler within the
     * {@link #notificationCountChangeHandlers}.
     */
    private void fireNotificationCountEvent() {
        int unseenCount = 0;
        final List<StrategyBubble> notificationBubbles = notificationActionPanel.getWidgetItems();
        for (StrategyBubble bubble : notificationBubbles) {
            if (!bubble.isHasBeenSeen()) {
                unseenCount++;
            }
        }

        onNotificationCountChanged(notificationBubbles.size(), unseenCount);

        updateSuggestionButtonsVisibility();
    }
    
    /**
     * Converts the provided {@link Strategy strategies} into
     * {@link StrategyHistoryItem strategy history items}.
     *
     * @param strategies the strategies to convert.
     * @param msgTimestamp the timestamp of the message that provided the
     *        strategies.
     * @return the collection of strategy history items.
     */
    private static Collection<StrategyHistoryItem> convertToStrategyHistoryItems(Collection<Strategy> strategies, long msgTimestamp) {
        List<StrategyHistoryItem> historyItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(strategies)) {
            for (Strategy strategy : strategies) {
                List<Serializable> activities = strategy.getStrategyActivities();
                historyItems.add(new StrategyHistoryItem(strategy, activities, msgTimestamp, null));
            }
        }

        return historyItems;
    }

    /**
     * A method that adds a {@link StrategyHistoryItem processed strategy} to
     * the {@link #notificationHistoryPanel} and caches it for future use.
     *
     * @param strategyHistoryItem The {@link StrategyHistoryItem processed
     *        strategy} to add. If null, no action is taken.
     */
    public void addToHistoryPanel(StrategyHistoryItem strategyHistoryItem) {

        addToHistoryPanel(strategyHistoryItem, null);
    }

    /**
     * A method that adds a {@link StrategyHistoryItem processed strategy} to
     * the {@link #notificationHistoryPanel} and caches it for future use.
     *
     * @param strategyHistoryItem The {@link StrategyHistoryItem processed
     *        strategy} to add. If null, no action is taken.
     * @param evaluator the user that requested the strategy. Can be null if automatically
     *        applied by GIFT.
     */
    public void addToHistoryPanel(StrategyHistoryItem strategyHistoryItem, String evaluator) {
        if (strategyHistoryItem == null) {
            return;
        }

        addStrategyHistoryToHistoryPanel(Arrays.asList(strategyHistoryItem), evaluator, strategyHistoryItem.getTimePerformed());
    }
    
    /**
     * A method that adds {@link StrategyHistoryItem processed strategies} to
     * the {@link #notificationHistoryPanel} and caches them for future use.
     *
     * @param strategies The strategies to add. If null or empty, this method will, at most, remove strategies from the history
     *        panel that happened after the provided timestamp.
     * @param evaluator the user that requested the strategy. Can be null if automatically
     *        applied by GIFT.
     * @param earliestStrategyTime the epoch time of the earliest strategy being added here. If strategyHistoryItems is empty
     *        than this can be the current play head time in order to clear out all strategies.
     */
    public void addStrategiesToHistoryPanel(Collection<Strategy> strategies, String evaluator, long earliestStrategyTime) {
        Collection<StrategyHistoryItem> strategyHistoryItems = convertToStrategyHistoryItems(strategies, earliestStrategyTime);
        addStrategyHistoryToHistoryPanel(strategyHistoryItems, evaluator, earliestStrategyTime);
    }

    /**
     * A method that adds {@link StrategyHistoryItem processed strategies} to
     * the {@link #notificationHistoryPanel} and caches them for future use.
     *
     * @param strategyHistoryItems The {@link StrategyHistoryItem processed
     *        strategies} to add. If null or empty, this method will, at most, remove strategies from the history
     *        panel that happened after the provided timestamp.
     * @param evaluator the user that requested the strategy. Can be null if automatically
     *        applied by GIFT.
     * @param earliestStrategyTime the epoch time of the earliest strategy being added here. If strategyHistoryItems is empty
     *        than this can be the current play head time in order to clear out all strategies.
     */
    public void addStrategyHistoryToHistoryPanel(Collection<StrategyHistoryItem> strategyHistoryItems, String evaluator, long earliestStrategyTime) {
        
        // perform this check after potentially removing any strategies that happen in the future.
        // This was moved in 10/2020 to fix an issue where seeking to before the first strategy would
        // not remove strategies from the history panel.
        if (CollectionUtils.isEmpty(strategyHistoryItems)) {
            return;
        }

        /* Add to history panel */
        notificationHistoryPanel.addAll(strategyHistoryItems);

        /* Cache the history items */
        final List<ProcessedStrategyCache> cacheCollection = new ArrayList<>();
        for (StrategyHistoryItem historyItem : strategyHistoryItems) {
            ProcessedStrategyCache cache = new ProcessedStrategyCache(historyItem.getStrategy(),
                    historyItem.getApprovedActivities(), knowledgeSession, historyItem.getTimePerformed(),
                    historyItem.getUserPerformed());
            cacheCollection.add(cache);
        }

        /* Send to cache */
        dashboardService.cacheProcessedStrategy(BrowserSession.getInstance().getBrowserSessionKey(), cacheCollection,
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> response) {
                        // do nothing
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        throw new DetailedException("Caching the processed scenario injects failed.",
                                "Failed to cache the scenario injects " + cacheCollection + " because " + t.getMessage(), t);
                    }
                });
    }

    /**
     * A method that adds a {@link Collection} of {@link ProcessedStrategyCache
     * cached processed strategies} to the {@link #notificationHistoryPanel}.
     *
     * @param cachedStrategies The {@link Collection} of
     *        {@link ProcessedStrategyCache cached processed strategies} to add.
     *        If null or empty, no action is taken.
     */
    public void loadHistoryCache(Collection<ProcessedStrategyCache> cachedStrategies) {
        if (CollectionUtils.isEmpty(cachedStrategies)) {
            return;
        }

        notificationHistoryPanel.clear();

        /* Add to history panel */
        for (ProcessedStrategyCache cachedItem : cachedStrategies) {
            StrategyHistoryItem historyItem = new StrategyHistoryItem(cachedItem.getStrategy(),
                    cachedItem.getApprovedActivities(), cachedItem.getTimePerformed(), cachedItem.getUserPerformed());
            notificationHistoryPanel.add(historyItem);
        }
    }

    /**
     * The notifications bubbles are visible and have been seen. Update their
     * property value to reflect this change.
     */
    private void setNotificationBubblesSeen() {
        for (StrategyBubble bubble : notificationActionPanel.getWidgetItems()) {
            bubble.setHasBeenSeen(true);
        }
    }

    /**
     * Refreshes any widget that has a timestamp or something else that needs
     * frequent updating.
     */
    public void refreshTimerWidgets() {
        for (StrategyBubble bubble : notificationActionPanel.getWidgetItems()) {
            bubble.refreshTimerWidgets();
        }

        for (StrategyHistoryBubble bubble : notificationHistoryPanel.getWidgetItems()) {
            bubble.refreshTimerWidgets();
        }
    }

    /**
     * Adds the {@link Collection} of {@link Strategy strategies} to the
     * {@link #presetStrategyPanel}.
     *
     * @param associatedStrategies The {@link Collection} of {@link Strategy
     *        strategies} to add to the panel that have an association with at
     *        least one {@link StateTransition}. Can be null iff there are
     *        unassociated strategies.
     * @param unassociatedStrategies The {@link Collection} of {@link Strategy
     *        strategies} to add to the panel that do not have an association
     *        with at least one {@link StateTransition}. Can be null iff there
     *        are associated strategies.
     * @param userCreated true if the user created these strategies manually
     *        (clicked the create new button); false if the strategies came from
     *        the system.
     */
    public void addPresetStrategies(Collection<Strategy> associatedStrategies,
            Collection<Strategy> unassociatedStrategies, boolean userCreated) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("setPresetStrategies(");
            List<Object> params = Arrays.<Object>asList(associatedStrategies, unassociatedStrategies);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (associatedStrategies == null && unassociatedStrategies == null) {
            throw new IllegalArgumentException(
                    "The parameters 'associatedStrategies' and 'unassociatedStrategies' cannot both be null.");
        } else if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        /* If the preset strategies are from the system, replace the existing
         * ones with the new ones. TODO: Note: this will also clear the
         * user-made strategies. Is this a problem? */
        if (!userCreated) {
            presetStrategyPanel.clear();
        }

        /* The priority panel used for preset strategies inserts at the top so
         * add them in reverse order since we want to preserve the ordering
         * here. The reason we are inserting at the top is so if the user
         * creates a new preset strategy later, it will be automatically added
         * to the top. */

        /* Add associated strategies first (will be under the unassociated
         * strategies) */
        if (associatedStrategies != null) {
            createMetricsForNewStrategies(associatedStrategies, presetStrategyPanel);
            Object[] strategyArray = associatedStrategies.toArray();
            /* Insert in reverse order */
            for (int i = strategyArray.length - 1; i >= 0; i--) {
                Strategy strategy = (Strategy) strategyArray[i];
                if (strategy.getStrategyActivities().isEmpty()) {
                    logger.warning("Received strategy '" + strategy.getName()
                            + "' with no activities. This serves no purpose in the SendCustomStrategyPanel so it has been skipped.");
                    continue;
                }
                
                // does the strategy only contain OC feedback?  If so, don't add it to the strategy presets
                // panel because it doesn't make sense for the OC to manually send feedback to himself
                boolean ocFeedbackOnly = false;
                for(Serializable activity : strategy.getStrategyActivities()){
                    
                    if(activity instanceof InstructionalIntervention){
                        InstructionalIntervention instInterv = (InstructionalIntervention)activity;
                        Feedback feedback = instInterv.getFeedback();
                        if(CollectionUtils.isNotEmpty(feedback.getTeamRef())){
                            // sending to a team member
                            break;
                        }else if(feedback.getFeedbackPresentation() instanceof Message){
                            Message message = (Message)feedback.getFeedbackPresentation();
                            Delivery delivery = message.getDelivery();
                            ToObserverController toObserverController = delivery != null ? delivery.getToObserverController() : null;
                            ocFeedbackOnly = toObserverController != null && 
                                    StringUtils.isNotBlank(toObserverController.getValue());

                            if(!ocFeedbackOnly){
                                break;
                            }
                        }
                    }else{
                        break;
                    }
                }

                if(!ocFeedbackOnly){
                    presetStrategyPanel.add(strategy);
                }
            }
        }

        /* Add unassociated strategies second (will be at the top) */
        if (unassociatedStrategies != null) {
            createMetricsForNewStrategies(unassociatedStrategies, presetStrategyPanel);
            Object[] strategyArray = unassociatedStrategies.toArray();
            /* Insert in reverse order */
            for (int i = strategyArray.length - 1; i >= 0; i--) {
                Strategy strategy = (Strategy) strategyArray[i];
                if (strategy.getStrategyActivities().isEmpty()) {
                    logger.warning("Received strategy '" + strategy.getName()
                            + "' with no activities. This serves no purpose in the SendCustomStrategyPanel so it has been skipped.");
                    continue;
                }
                
                // does the strategy only contain OC feedback?  If so, don't add it to the strategy presets
                // panel because it doesn't make sense for the OC to manually send feedback to himself
                boolean ocFeedbackOnly = false;
                for(Serializable activity : strategy.getStrategyActivities()){
                    
                    if(activity instanceof InstructionalIntervention){
                        InstructionalIntervention instInterv = (InstructionalIntervention)activity;
                        Feedback feedback = instInterv.getFeedback();
                        if(CollectionUtils.isNotEmpty(feedback.getTeamRef())){
                            // sending to a team member
                            break;
                        }else if(feedback.getFeedbackPresentation() instanceof Message){
                            Message message = (Message)feedback.getFeedbackPresentation();
                            Delivery delivery = message.getDelivery();
                            ToObserverController toObserverController = delivery != null ? delivery.getToObserverController() : null;
                            ocFeedbackOnly = toObserverController != null && 
                                    StringUtils.isNotBlank(toObserverController.getValue());

                            if(!ocFeedbackOnly){
                                break;
                            }
                        }
                    }else{
                        break;
                    }
                }

                if(!ocFeedbackOnly){
                    presetStrategyPanel.add(strategy);

                    final StrategyBubble createdBubble = presetStrategyPanel.getWidget(strategy);
                    createdBubble.makeGameMasterDriven(new Command() {
                        @Override
                        public void execute() {
                            sendSingleBubble(createdBubble);
    
                            /* Create the history item for the strategy */
                            StrategyHistoryItem historyItem = new StrategyHistoryItem(createdBubble.getStrategy(),
                                    createdBubble.getSelectedActivities(), UiManager.getInstance().getUserName());
                            addToHistoryPanel(historyItem, UiManager.getInstance().getUserName());
                        }
                    }, userCreated ? UiManager.getInstance().getUserName() : null);
                }
            }
        }

        updatePresetStrategiesButtonVisibility();
    }

    /**
     * Sends the provided {@link StrategyBubble} and displays a toast to the
     * Game Master
     * 
     * @param bubbleToSend the strategy bubble being sent
     */
    private void sendSingleBubble(StrategyBubble bubbleToSend) {
        Strategy newStrategy = createStrategyFromSelectedActivities(bubbleToSend, false);
        if (newStrategy != null) {
            final String approverUsername = UiManager.getInstance().getUserName();
            strategyMetricsMap.get(bubbleToSend.getStrategy()).incrementApprovedCount(approverUsername);
            final ApplyStrategies payload = new ApplyStrategies(
                    Arrays.asList(new StrategyToApply(newStrategy, Constants.GAMEMASTER_SOURCE, approverUsername)), 
                    Constants.GAMEMASTER_SOURCE, approverUsername);
            BrowserSession.getInstance().sendWebSocketMessage(new DashboardMessage(payload, knowledgeSession));
            displayToast(payload);
        }
    }

    /**
     * Creates a strategy from the selected activities within the provided
     * {@link StrategyBubble}.
     *
     * @param bubble the {@link StrategyBubble} that contains the selected
     *        activities.
     * @param ignoreUnassociatedStrategies true to ignore strategies that are
     *        unassociated with any state transition (e.g. they can only be
     *        triggered manually by the game master); false to include them.
     *        NOTE: unassociated strategies will be included regardless of this
     *        value if they are not in 'simple send' mode; in this case, they
     *        are treated like any other strategy.
     * @return the strategy containing the selected activities. If all
     *         activities are selected, then the original strategy is returned.
     *         Can return null if no activities were selected or if the strategy
     *         was skipped.
     */
    private Strategy createStrategyFromSelectedActivities(StrategyBubble bubble, boolean ignoreUnassociatedStrategies) {
        if (bubble == null) {
            throw new IllegalArgumentException("The parameter 'bubble' cannot be null.");
        }

        /* Check if we should skip bubbles that are in simple send mode */
        if (ignoreUnassociatedStrategies && bubble.isInSimpleSendMode()) {
            return null;
        }

        /* Collect the selected strategy activities within the bubble */
        Collection<Serializable> selectedActivities = bubble.getSelectedActivities();
        if (selectedActivities.isEmpty()) {
            return null;
        }

        final Strategy selectedStrategy = bubble.getStrategy();
        final String selectedStrategyName = selectedStrategy.getName();
        boolean customSelection = selectedStrategy.getStrategyActivities().size() != selectedActivities.size();

        Strategy strategyToSend;
        if (customSelection) {
            /* Prepend 'custom_' to signify that the strategy has been
             * altered */
            strategyToSend = new Strategy();
            strategyToSend.setName("custom_" + selectedStrategyName);
            strategyToSend.getStrategyActivities().addAll(selectedActivities);
        } else {
            strategyToSend = selectedStrategy;
        }

        return strategyToSend;
    }

    /**
     * Display useful information to the user about the sent strategy request.
     * This is used as a visual indicator that a custom strategy request was
     * sent.
     *
     * @param strategies the strategies being sent.
     */
    private void displayToast(ApplyStrategies strategies) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("displayToast(" + strategies + ")");
        }

        /* If there aren't any actual strategies to apply, there is nothing else
         * to do. */
        List<StrategyToApply> strategiesToApply = strategies.getStrategies();
        if (strategiesToApply.isEmpty()) {
            return;
        }

        String strategyName;
        if (strategiesToApply.size() == 1) {
            strategyName = "Scenario Injects \"" + strategiesToApply.get(0).getStrategy().getName() + "\"";
        } else {
            strategyName = strategiesToApply.size() + " Scenario Injects";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div data-notify=\"container\" class=\"col-xs-11 col-sm-4 alert alert-info\" role=\"alert\">");
        sb.append(
                "<button type=\"button\" aria-hidden=\"true\" class=\"close\" data-notify=\"dismiss\">&times;</button>");
        sb.append("<b><span data-notify=\"message\">{2}</span></b></div>");

        NotifySettings notifySettings = StrategiesPanel.buildNotifySettings();
        notifySettings.setTemplate(sb.toString());
        Notify.notify("Sent " + strategyName + " to the Scenario", notifySettings);
    }

    /**
     * Updates the visibility of the preset strategy header buttons based on the
     * content of {@link #sessionEnded} and if there are any selected
     * activities.
     */
    private void updatePresetStrategiesButtonVisibility() {
        setPresetStrategiesButtonVisibility(presetCollapse.isShown());
    }

    /**
     * Updates the visibility of the preset strategy header buttons based on the
     * content of {@link #sessionEnded} and if there are any selected
     * activities.
     *
     * @param visible true to attempt to set the buttons to visible; false hide
     *        them.
     */
    private void setPresetStrategiesButtonVisibility(boolean visible) {
        final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        visible &= RunState.RUNNING.equals(activeSessionProvider.getRunState(dsId));
        createNewPresetButton.setVisible(visible);

        /* See if any activities are selected */
        boolean foundSelected = false;
        for (StrategyBubble bubble : presetStrategyPanel.getWidgetItems()) {
            if (bubble.isInSimpleSendMode()) {
                continue;
            } else if (!bubble.getSelectedActivities().isEmpty()) {
                foundSelected = true;
                break;
            }
        }

        /* The send and reset buttons require more critieria to become
         * visible */
        visible &= foundSelected;

        sendPresetStrategiesButton.setVisible(visible);
        resetPresetStrategiesButton.setVisible(visible);
    }

    /**
     * Opens the strategy history collapse panel and closes the others. This is
     * the default behavior for the initial setup of this panel.
     */
    public void openStrategyHistoryPanel() {
        openStrategyHistoryPanel(null, null);
    }
    
    /**
     * Opens the strategy history panel so that it is visible in the strategy panel
     * 
     * @param name the name of the strategy to select when the strategy panel is shown.
     * If no strategy with a matching name is found no strategy will be selected.
     * @param timestamp the timestamp of the strategy to select when the strategy panel is shown.
     * If no strategy with a matching timestamp is found no strategy will be selected.
     */
    public void openStrategyHistoryPanel(final String name, final Long timestamp) {
        
        if(name != null && timestamp != null) {
            if(historyCollapse.isShown()) {
                
                /* If we need to expand and scroll to a strategy and the strategy history panel
                 * is already expanded, then just expand the strategy immediately */
                expandStrategyHistoryBubble(name, timestamp);
                
            } else {
                
                /* If we need to expand and scroll to a strategy and the strategy history panel
                 * is closed, then we need to wait until it is fully open before we expand the
                 * strategy, otherwise scrolling to it will not work properly because the strategy
                 * is still being moved. */
                final HandlerRegistration[] registration = {};
                registration[0] = historyCollapse.addShownHandler(new ShownHandler() {
                    
                    @Override
                    public void onShown(ShownEvent event) {
                        
                        expandStrategyHistoryBubble(name, timestamp);
                        registration[0].removeHandler();
                    }
                });
            }
        }
        
        openCollapse(historyCollapse);
    }
    
    /**
     * Expands the history bubble of the strategy with the given name and timetamp
     * and scrolls it into view
     * 
     * @param name the name of the strategy to expand. Can be null.
     * @param timestamp the timestamp of the strategy to expand. Can be null.
     */
    private void expandStrategyHistoryBubble(final String name, final Long timestamp) {
        
        /* Defer expanding the bubble until after the event loop has cleared. This helps
         * avoid interacting with the bubble while the surrounding panels are still being
         * initialized. 
         * 
         * If this is not done, then clicking on a strategy in the timeline during a past 
         * session when that session has not yet started and the assessments panel is closed
         * will NOT expand the strategy bubble because it is not yet ready.
         */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
        
                for (final StrategyHistoryBubble bubble : notificationHistoryPanel.getWidgetItems()) {
                    
                    if(bubble.getHistoryItem().getTimePerformed() == timestamp 
                            && Objects.equals(bubble.getStrategy().getName(), name)) {
                        
                        //if a strategy with a matching name and timestamp is found, expand it
                        bubble.expandBubble();
                        setSelectedHistoryItem(bubble);
                        
                        bubble.getElement().scrollIntoView();
                    }
                }
            }
        });
    }

    @Override
    public void permissionUpdate(Set<Component> disallowedComponents) {
        setDisallowedComponents(disallowedComponents);
        
        /* Update UI components */
        autoCheckBox.setVisible(!disallowedComponents.contains(Component.AUTO_APPLY_STRATEGIES));
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }
        
        /* Restart timer */
        refreshTimer.scheduleRepeating(TIMER_DELAY);

        updateSuggestionButtonsVisibility();
        updatePresetStrategiesButtonVisibility();
        
        /* Update UI components */
        autoCheckBox.setEnabled(true);
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
        
        /* Stop refreshing the display */
        refreshTimer.cancel();

        /* Update UI components */
        autoCheckBox.setEnabled(false);

        updateSuggestionButtonsVisibility();
        updatePresetStrategiesButtonVisibility();
    }

    /**
     * Sets a navigator that can be used to set the current session time to a particular
     * point in a knowledge session's timeline.
     * 
     * @param navigator the timeline navigator. Can be null, if navigating through a 
     * timeline should not be possible, such as for active sessions.
     */
    public void setTimelineNavigator(TimelineNavigator navigator) {
        this.timelineNavigator = navigator;
    }
    
    /**
     * Sets the history item that should be selected. If another item is currently selected,
     * then it will be de-selected and collapsed.
     * 
     * @param strategyBubble the history item to select. Can be null, if no item should be selected.
     */
    private void setSelectedHistoryItem(StrategyHistoryBubble strategyBubble) {
        if(selectedHistoryItem != null && !selectedHistoryItem.equals(strategyBubble)) {
            selectedHistoryItem.collapseBubble();
        }
        
        selectedHistoryItem = strategyBubble;
    }

    @Override
    public void sessionStateUpdate(KnowledgeSessionState state, int domainSessionId) {
        
        if (knowledgeSession.getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        }

        /* Null assessment means that entity received a "visual only" state. Do
         * not process further. This was implemented as a quick hack for ARES
         * visualization. */
        if (state.getLearnerState() != null) {
            PerformanceState performance = state.getLearnerState().getPerformance();
            for (TaskPerformanceState perfState : performance.getTasks().values()) {
                for (ConceptPerformanceState c : perfState.getConcepts()) {
                    if (c.getState().getAssessedTeamOrgEntities().containsValue(null)) {
                        return;
                    }
                }
            }
        }
        
        if (!state.getCachedProcessedStrategies().isEmpty()) {
            loadHistoryCache(state.getCachedProcessedStrategies());
        }
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        // Nothing to do, since this panel does not care about task data
    }

    @Override
    public void setPresetStrategies(Collection<Strategy> associatedStrategies,
            Collection<Strategy> unassociatedStrategies, int domainSessionId) {
        
        /* Process the strategies if the specified session is equal to this
         * panel's session */
        if (knowledgeSession.getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        }

        addPresetStrategies(associatedStrategies, unassociatedStrategies, false);
    }

    @Override
    public void addSuggestedStrategy(Collection<Strategy> strategies, int domainSessionId, String evaluator,
            long msgTimestamp) {
        
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("addSuggestedStrategy(");
            List<Object> params = Arrays.<Object>asList(strategies, domainSessionId, evaluator, msgTimestamp);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        }

        /* Handle the suggested strategies iff the specified session is equal to
         * this panel's session */
        if (knowledgeSession.getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        } else if (UiManager.getInstance().getUserName().equals(evaluator)) {
            /* Ignore messages that were sent from this evaluator */
            return;
        }

        /* Show the toast for each of the suggested strategies when in active session mode */
        if (!knowledgeSession.inPastSessionMode()) {
            for (Strategy strategy : strategies) {
                displayToast(strategy);
            }
        }

        /* If in auto mode, the suggested strategies are automatically applied
         * (already happened on server-side). Directly add them to the history
         * panel; otherwise wait for the user to manually approve/deny */
        if (isAutoMode()) {
            addStrategiesToHistoryPanel(strategies, evaluator, msgTimestamp);
        } else {
            /* The non-mandatory strategies will be added to the requests */
            addStrategyRequests(strategies, evaluator);
        }
    }

    @Override
    public void addAppliedStrategy(Collection<Strategy> strategies, int domainSessionId, String evaluator,
            long msgTimestamp) {
        
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("addAppliedStrategy(");
            List<Object> params = Arrays.<Object>asList(strategies, domainSessionId, evaluator, msgTimestamp);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        }

        /* Handle the applied strategies iff the specified session is equal to
         * this panel's session */
        if (knowledgeSession.getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        }

        /* Show the toast for each of the applied strategies if not PAST
         * playback */
        if (!knowledgeSession.inPastSessionMode()) {
            for (Strategy strategy : strategies) {
                displayToast(strategy);
            }
        }

        addStrategiesToHistoryPanel(strategies, evaluator, msgTimestamp);
    }

    @Override
    public void executeOcStrategy(ExecuteOCStrategy executeOcStrategy, int domainSessionId, long msgTimestamp) {
        
        if (executeOcStrategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        final Strategy strategy = executeOcStrategy.getStrategy();
            for (Serializable activity : strategy.getStrategyActivities()) {
                // display feedback message if it's intended for the controller
                if (activity instanceof InstructionalIntervention) {
                    InstructionalIntervention ii = (InstructionalIntervention) activity;
                    
                    Serializable presentation = ii.getFeedback().getFeedbackPresentation();
                    if (presentation instanceof Message) {
                        Message message = (Message) presentation;
                    /* For now we only check if the property is not null
                     * eventually we'll use the string value to send to specific
                     * controllers */
                        if (message.getDelivery().getToObserverController() != null) {
                                displayToast(message);

                                if (message.getDelivery().getInTutor() != null) {
                                    String textEnhancement = message.getDelivery().getInTutor().getTextEnhancement();
                            if (TextFeedbackDisplayEnum.BEEP_ONLY.toString().equals(textEnhancement)
                                    || TextFeedbackDisplayEnum.BEEP_AND_FLASH.toString().equals(textEnhancement)) {
                                        if (!Dashboard.VolumeSettings.FEEDBACK_SOUND.getSetting().isMuted()) {
                                            logger.info("play beep for feedback message (session data panel)");
                                            BsGameMasterPanel.playGoodPerformanceBeep(knowledgeSession);
                                        }
                                    }
                                }
                            }
                    } else if (presentation instanceof Audio) {
                        Audio audio = (Audio) presentation;
                        if (audio.getToObserverController() != null) {
                                if(!Dashboard.VolumeSettings.FEEDBACK_SOUND.getSetting().isMuted()){
                                    logger.info("play beep for feedback audio (session data panel)");
                                    Dashboard.playAudio(
                                            audio.getMP3File(), 
                                            audio.getOGGFile(), 
                                            Dashboard.VolumeSettings.FEEDBACK_SOUND.getSetting().getVolume());
                                }
                            }
                        }
                    }
                }

        /* Originated as a task trigger, this did not go through the normal
         * approve/deny process so it must be added to the history panel
         * separately */
        if (executeOcStrategy.isScenarioControl()) {
            strategy.setName(strategy.getName());
            addStrategiesToHistoryPanel(Arrays.asList(strategy),
                    executeOcStrategy.getEvaluator(), msgTimestamp);
        }
    }
    
    /**
     * Display a feedback message intended for the controller
     *
     * @param message the feedback being presented.
     */
    private void displayToast(Message message) {
        
        /* Do not show the toast if in past session mode, show in active session */
        if (knowledgeSession.inPastSessionMode()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div data-notify=\"container\" class=\"col-xs-11 col-sm-4 alert alert-info\" role=\"alert\">");
        sb.append(
                "<button type=\"button\" aria-hidden=\"true\" class=\"close\" data-notify=\"dismiss\">&times;</button>");
        sb.append("<b><span data-notify=\"message\">{2}</span></b>");
        sb.append("<br/>");
        StrategyActivityIcon icon = StrategyActivityUtil.getIconFromActivityType(ActivityType.MESSAGE_FEEDBACK);
        icon.applyToastStyle();
        sb.append(icon.toString());
        sb.append(message.getContent());
        sb.append("</div>");

        NotifySettings notifySettings = StrategiesPanel.buildNotifySettings();
        notifySettings.setTemplate(sb.toString());

        Notify.notify("Message Received", notifySettings);
    }
    
    /**
     * Display useful information to the user about the incoming strategy request. This is used as a
     * visual indicator that a new strategy request was received.
     *
     * @param strategy the strategy being requested.
     */
    private void displayToast(Strategy strategy) {
        /* Do not show the toast if PAST session mode */
        if (knowledgeSession.inPastSessionMode()) {
            return;
        }
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("showing toast - "+knowledgeSession.getSessionType()+" : "+strategy.getName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div data-notify=\"container\" class=\"col-xs-11 col-sm-4 alert alert-info\" role=\"alert\">");
        sb.append(
                "<button type=\"button\" aria-hidden=\"true\" class=\"close\" data-notify=\"dismiss\">&times;</button>");
        sb.append("<b><span data-notify=\"message\">{2}</span></b>");
        sb.append("<br/>");
        for (Serializable activity : strategy.getStrategyActivities()) {
            
            StrategyActivityIcon icon = StrategyActivityUtil.getIconFromActivity(activity);
            icon.applyToastStyle();
            sb.append(icon.toString());
        }
        sb.append("</div>");

        /* Don't show toast if the notification panel is already visible or if
         * in 'auto mode' */
        if (isAttached() || isAutoMode()) {
            return;
        }

        NotifySettings notifySettings = buildNotifySettings();
        notifySettings.setTemplate(sb.toString());
        Notify oldNotify = strategyNameToNotification.get(strategy.getName());
        if (oldNotify != null) {
            oldNotify.hide();
        }

        Notify notify = Notify.notify("Scenario Injects \"" + strategy.getName() + "\" Received", notifySettings);
        strategyNameToNotification.put(strategy.getName(), notify);
    }
    
    /**
     * Build the notify settings used for game master.
     *
     * @return the settings used for the Notify toast
     */
    public static NotifySettings buildNotifySettings() {
        NotifySettings notifySettings = NotifySettings.newSettings();
        notifySettings.setPlacement(NotifyPlacement.TOP_RIGHT);
        notifySettings.setPauseOnMouseOver(true);
        notifySettings.setType(NotifyType.INFO);
        notifySettings.setZIndex(999999);
        notifySettings.setOffset(20, 110);
        notifySettings.setAnimation(Animation.FADE_IN_RIGHT, Animation.FADE_OUT_RIGHT);
        notifySettings.setNewestOnTop(true);
        return notifySettings;
    }
    
    /**
     * Return the value of the {@link #autoCheckBox}.
     * 
     * @return true if the checkbox is checked; false otherwise.
     */
    public boolean isAutoMode() {
        return Boolean.TRUE.equals(autoCheckBox.getValue());
    }

    /**
     * Set the value for the {@link #autoCheckBox}. This will also fire an event
     * to update the server-side 'auto' value.
     * 
     * @param isAuto true if the checkbox should be checked; false if unchecked.
     */
    public void setAutoMode(boolean isAuto) {
        ValueChangeEvent.fire(autoCheckBox, isAuto);
    }
    
    /**
     * Executes some logic whenever the number of notifications changes
     *
     * @param notificationCount the new total notification count
     * @param unseenNotificationCount the number of notifications that have
     *        never been seen by the user
     */
    public void onNotificationCountChanged(int notificationCount, int unseenNotificationCount) {
        if (unseenNotificationCount > 0) {
            notificationBadge.setText(Integer.toString(unseenNotificationCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }
    
    /**
     * Send the auto mode state to the server.
     * 
     * @param isAuto true if in auto mode; false if in manual mode.
     */
    public static void sendAutoMode(final boolean isAuto) {
        UiManager.getInstance().getDashboardService().updateGameMasterAutoState(
                BrowserSession.getInstance().getBrowserSessionKey(), isAuto,
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> response) {
                        // do nothing
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        throw new DetailedException("Updating the auto mode state failed.",
                                "The auto mode was unable to be switched to '" + (isAuto ? "true" : "false")
                                        + "' because " + t.getMessage(),
                                t);
                    }
                });
    }
}
