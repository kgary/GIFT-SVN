/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.InstructionalIntervention;
import generated.dkf.Message;
import generated.dkf.MidLessonMedia;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;
import generated.dkf.Team;
import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.SaveCancelCallback;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil.StrategyActivityIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ThreeStateCheckbox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.RunState;

/**
 * A display bubble to allow the user to select the strategy and its activities.
 *
 * @author sharrison
 */
public abstract class StrategyBubble extends Composite implements ActiveSessionChangeHandler {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyBubble.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StrategyBubbleUiBinder uiBinder = GWT.create(StrategyBubbleUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StrategyBubbleUiBinder extends UiBinder<Widget, StrategyBubble> {
    }

    /** Tooltip for the send button while in simple mode */
    private static final String SEND_WHOLE_STRATEGY_TOOLTIP_TEXT = "Apply this strategy now";

    /** Tooltip for the send button while in custom mode */
    private static final String SEND_CUSTOM_STRATEGY_TOOLTIP_TEXT = "Apply the selected activities in this strategy now";

    /** Tooltip for the mode button while in simple mode */
    private static final String CHANGE_TO_CUSTOM_MODE_TOOLTIP_TEXT = "Allow portions of this strategy to be applied";

    /** Tooltip for the mode button while in custom mode */
    private static final String CHANGE_TO_SIMPLE_MODE_TOOLTIP_TEXT = "Only the complete strategy can be applied";
    
    /**
     * Number format used to show the strategy stress in 1 decimal precision
     */
    private static NumberFormat STRESS_FORMAT = NumberFormat.getFormat("0.0");
    
    /**
     * Number format used to show the strategy difficulty in 1 decimal precision
     */
    private static NumberFormat DIFFICULTY_FORMAT = NumberFormat.getFormat("0.0");

    /** The active session provider instance */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();
    
    /** An interface used to access this class' CSS styles */
    protected interface Style extends CssResource {
    
        public String selected();
        
        public String borderRight();
    }

    /** A set of styles associated with this widget */
    @UiField
    protected Style style;

    /** The main flow panel that contains the contents of the bubble */
    @UiField
    protected FlowPanel mainPanel;

    /** The icon used to close the bubble */
    @UiField
    protected Icon closeButton;

    /** The panel containing the header data for the bubble */
    @UiField
    protected FlowPanel bubbleHeader;

    /**
     * Toggles between the {@link #strategyCheckBox} and
     * {@link #sendButtonGroup}
     */
    @UiField
    protected DeckPanel headerDeckPanel;

    /** The global checkbox for this strategy */
    @UiField
    protected ThreeStateCheckbox strategyCheckBox;

    /**
     * The group of buttons to display if allowing the user to send only this
     * strategy set
     */
    @UiField
    protected ButtonGroup sendButtonGroup;

    /** The tooltip for {@link #sendStrategyButton} */
    @UiField
    protected ManagedTooltip sendStrategyButtonTooltip;

    /** Sends the strategy represented by this bubble */
    @UiField
    protected Button sendStrategyButton;

    /** The tooltip for {@link #changeModeButton} */
    @UiField
    protected ManagedTooltip changeModeButtonTooltip;

    /** Changes the mode of the bubble between simple and custom */
    @UiField
    protected Button changeModeButton;

    /** The html label for the strategy name */
    @UiField
    protected EditableInlineLabel strategyNameLabel;

    /**
     * The glass panel that prevents the author from interacting with the
     * strategy bubble while a feedback is being edited
     */
    @UiField
    protected FlowPanel glassPanel;

    /** The list of icons that are displayed when the bubble is collapsed */
    private final List<FlowPanel> collapsedStrategyIcons = new ArrayList<>();
    
    /** the list of icons that are displayed no matter if the strategy bubble is expanded or collapsed (e.g. stress icon) */
    private final List<FlowPanel> fixedStrategyIcons = new ArrayList<>();

    /** The panel that is displayed when the bubble is expanded */
    private final FlowPanel expandedPanel = new FlowPanel();

    /** The panel containing the strategy metrics data. */
    private FlowPanel metricPanel = new FlowPanel();

    /** The strategy for this bubble */
    private final Strategy strategy;

    /**
     * The map of activities to the panel builder that contains the activity's
     * associated panel components
     */
    protected Map<Serializable, ActivityPanelManager> activityToBuilderMap = new HashMap<>();
    
    /**
     * The list of icons for each ActivityType
     */
    protected List<StrategyActivityIcon> consecutiveActivityIcon = new ArrayList<StrategyActivityIcon>();

    /**
     * The default selection value for the {@link #strategyCheckBox} and the
     * activity checkboxes
     */
    private final boolean defaultSelectionChoice;

    /** The flag indicating if the checkboxes are clickable */
    private final boolean readOnly;

    /**
     * The list of callbacks to be executed whenever the activity selection is
     * changed
     */
    private Collection<ChangeCallback<Void>> activitySelectionChangeCallbacks = new HashSet<>();

    /** Flag to indicate if the user has seen this bubble */
    private boolean hasBeenSeen = false;

    /** Click handler registration for the strategy name label */
    protected HandlerRegistration labelHandlerRegistration;

    /** Flag indicating if this bubble is Game Master driven */
    private boolean isGameMasterDriven = false;

    /** The command to execute on sending the bubble */
    private Command sendCommand;

    /**
     * The user that manually created the {@link #strategy} this bubble
     * represents. Will be null if the {@link #strategy} is from the system.
     */
    private String author;

    /**
     * The name of the user that last edited this strategy. Will be null if it
     * was never edited.
     */
    private String lastEditedBy;

    /**
     * Reusable click handler that stops the click event from propagating
     */
    private final ClickHandler stopPropagationHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
        }
    };

    /** The knowledge session */
    private final AbstractKnowledgeSession knowledgeSession;

    /** The structure of the team for the session */
    private final Team team;

    /**
     * Constructor.
     *
     * @param strategy the strategy with which to populate this bubble. Can't be
     *        null.
     * @param defaultSelectionChoice true to have the strategy and strategy
     *        activities selected by default; false to have them unselected.
     * @param closeable true if the bubble should be closeable; false if it
     *        should not.
     * @param knowledgeSession the knowledge session used to populate this
     *        panel. Can't be null.
     * @param team the structure of the team for the session.
     */
    public StrategyBubble(Strategy strategy, boolean defaultSelectionChoice, boolean closeable, AbstractKnowledgeSession knowledgeSession,
            Team team) {
        this(strategy, defaultSelectionChoice, closeable, false, knowledgeSession, team);
    }

    /**
     * Constructor.
     *
     * @param strategy the strategy with which to populate this bubble. Can't be
     *        null.
     * @param defaultSelectionChoice true to have the strategy and strategy
     *        activities selected by default; false to have them unselected.
     * @param closeable true if the bubble should be closeable; false if it
     *        should not.
     * @param readOnly true to make the panel readonly; false otherwise.
     * @param knowledgeSession the knowledge session used to populate this
     *        panel. Can't be null.
     * @param team the structure of the team for the session.
     */
    protected StrategyBubble(final Strategy strategy, boolean defaultSelectionChoice, boolean closeable,
            final boolean readOnly, AbstractKnowledgeSession knowledgeSession,
            Team team) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        } else if (strategy.getStrategyActivities().isEmpty()) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot have an empty activity list.");
        } else if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        } else if (knowledgeSession.getTrainingAppType() == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot have a null training app type.");
        }

        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("StrategyBubble(");
            List<Object> params = Arrays.<Object>asList(strategy, defaultSelectionChoice, closeable, readOnly,
                    knowledgeSession.getNodeIdToNameMap(), knowledgeSession.getTrainingAppType());
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.strategy = strategy;
        this.defaultSelectionChoice = defaultSelectionChoice;
        this.readOnly = readOnly;
        this.knowledgeSession = knowledgeSession;
        this.team = team;

        /* If the bubble is clicked, perform collapse/expand */
        this.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* Prevent action if glass panel is attached. Cannot just stop
                 * propagation on the glass panel because you could mouse down
                 * on the edit panel and mouse up on the glass panel, which is a
                 * click for the bubble */
                if (glassPanel.isAttached()) {
                    return;
                }

                if (isBubbleExpanded()) {
                    collapseBubble();
                } else {
                    expandBubble();
                }
            }
        }, ClickEvent.getType());

        /* init - close button */
        if (closeable) {
            closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();

                    /* Removal of the bubble must be handled externally (through
                     * addCloseButtonHandler) because it's too dangerous to
                     * assume we can just remove from parent here. */
                }
            });
        } else {
            /* Remove the close button */
            closeButton.removeFromParent();
        }

        /* init - bubble header */
        bubbleHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!readOnly
                        && headerDeckPanel.getVisibleWidget() != headerDeckPanel.getWidgetIndex(sendButtonGroup)) {
                    event.stopPropagation();
                }
            }
        }, ClickEvent.getType());

        /* init - strategy check box */
        strategyCheckBox.setEnabled(!readOnly);
        strategyCheckBox.setValue(defaultSelectionChoice);
        strategyCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* If the strategy checkbox or label is clicked, perform that
                 * action on all the children checkboxes */
                boolean selected = isChecked(strategyCheckBox);
                for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
                    activityPanelManager.getCheckBox().setValue(selected);
                }
                fireActivitySelectionChangeCallbacks();
            }
        });

        /* init - send button group */
        initSendButtonGroup();

        /* Default to showing the strategy check box */
        headerDeckPanel.showWidget(headerDeckPanel.getWidgetIndex(strategyCheckBox));
        headerDeckPanel.setVisible(!readOnly);  // hide the checkbox so it doesn't take up visual space, do this AFTER showWidget call
        
        /* init - strategy name label */
        strategyNameLabel.setValue(strategy.getName());
        strategyNameLabel.setEditingEnabled(false);
        strategyNameLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String newName = event.getValue();
                if (StringUtils.isBlank(newName)) {
                    strategyNameLabel.setValue(strategy.getName());
                } else {
                    strategy.setName(newName);
                }
            }
        });
        labelHandlerRegistration = strategyNameLabel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                strategyCheckBox.setValue(!isChecked(strategyCheckBox));
                boolean selected = isChecked(strategyCheckBox);
                for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
                    activityPanelManager.getCheckBox().setValue(selected);
                }
                fireActivitySelectionChangeCallbacks();
            }
        }, ClickEvent.getType());
        
        // add the difficulty attribute elements to the collapsed view of the strategy
        addStrategyDifficultyIcon();
        
        // add the stress attribute elements to the collapsed view of the strategy
        addStrategyStressIcon();
        
        countActiviesAndCreateIcons(consecutiveActivityIcon, strategy);

        /* Build the expanded panel for later use */
        buildExpandedStrategyBubble();

        /* init - glass panel */
        glassPanel.addDomHandler(stopPropagationHandler, ClickEvent.getType());
        glassPanel.removeFromParent();

        /* Subscribe to the data providers */
        subscribe();
    }
    
    /**
     * Adds UI elements to the {@link #collapsedStrategyIcons} that display information about this
     * strategy's stress attribute value, if provided.  E.g. Arrow up icon and stress number in a label.
     */
    private void addStrategyStressIcon() {
        
        if(strategy == null) {
            return;
        }
        
        Double stress = null;
        if(strategy.getStress() != null) {
            stress = strategy.getStress().doubleValue();
        }
        
        Icon arrowIcon = new Icon();
        String stressAction = "";
        if(stress != null) {
            if(stress > 0) {
                // arrow up
                arrowIcon.setType(IconType.ARROW_UP);
                stressAction = "Increases stress";
            }else if(stress < 0) {
                // arrow down
                arrowIcon.setType(IconType.ARROW_DOWN);
                stressAction = "Decreases stress";
            }else {
                // currently not showing a no stress value label
                return;
            }
        }else {
            return;
        }
                
        // make the icon and label on the same line
        HorizontalPanel stressHorizPanel = new HorizontalPanel();
        Image imgIcon = new Image();
        imgIcon.setUrl("images/icons/stressed.png");
        imgIcon.setSize("20px", "20px");
        Label stressLabel = new Label(STRESS_FORMAT.format(stress));
        stressHorizPanel.add(imgIcon);
        stressHorizPanel.add(stressLabel);
        stressHorizPanel.add(arrowIcon);
        ManagedTooltip tooltip = new ManagedTooltip(stressHorizPanel, stressAction);

        FlowPanel stressPanel = new FlowPanel();
        stressPanel.getElement().getStyle().setPaddingRight(5, Unit.PX);
        stressPanel.getElement().getStyle().setPaddingLeft(3, Unit.PX);
        stressPanel.addStyleName(style.borderRight());
        stressPanel.add(tooltip);

        fixedStrategyIcons.add(stressPanel);
        mainPanel.add(stressPanel);
    }

    /**
     * Adds UI elements to the {@link #collapsedStrategyIcons} that display information about this
     * strategy's difficulty attribute value, if provided.  E.g. Arrow up icon and difficulty number in a label.
     */
    private void addStrategyDifficultyIcon() {
        
        if(strategy == null) {
            return;
        }
        
        Double difficulty = null;
        if(strategy.getDifficulty() != null) {
            difficulty = strategy.getDifficulty().doubleValue();
        }
        
        Icon arrowIcon = new Icon();
        String difficultyAction = "";
        if(difficulty != null) {
            if(difficulty > 0) {
                // arrow up
                arrowIcon.setType(IconType.ARROW_UP);
                difficultyAction = "Increases difficulty";
            }else if(difficulty < 0) {
                // arrow down
                arrowIcon.setType(IconType.ARROW_DOWN);
                difficultyAction = "Decreases difficulty";
            }else {
                // currently not showing a no difficulty value label
                return;
            }
        }else {
            return;
        }
                
        // make the icon and label on the same line
        HorizontalPanel difficultyHorizPanel = new HorizontalPanel();
        Image imgIcon = new Image();
        imgIcon.setUrl("images/icons/effort.png");
        imgIcon.setSize("20px", "20px");
        Label difficultyLabel = new Label(DIFFICULTY_FORMAT.format(difficulty));
        difficultyHorizPanel.add(imgIcon);
        difficultyHorizPanel.add(difficultyLabel);
        difficultyHorizPanel.add(arrowIcon);
        ManagedTooltip tooltip = new ManagedTooltip(difficultyHorizPanel, difficultyAction);

        FlowPanel difficultyPanel = new FlowPanel();
        difficultyPanel.getElement().getStyle().setPaddingRight(5, Unit.PX);
        difficultyPanel.getElement().getStyle().setPaddingLeft(3, Unit.PX);
        difficultyPanel.addStyleName(style.borderRight());
        difficultyPanel.add(tooltip);

        fixedStrategyIcons.add(difficultyPanel);
        mainPanel.add(difficultyPanel);
    }
    
    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to the active session changes */
        activeSessionProvider.addHandler(this);
    }

    /**
     * Unsubscribe from all providers. This should only be done before the panel
     * is destroyed.
     */
    private void unsubscribe() {
        /* Remove handlers */
        activeSessionProvider.removeHandler(this);
    }

    /**
     * Initialize the send button group for the bubble. The send button group
     * will remain invisible until
     * {@link #makeGameMasterDriven(Command, String)} is called.
     */
    private void initSendButtonGroup() {
        sendButtonGroup.addDomHandler(stopPropagationHandler, ClickEvent.getType());

        /* Configure send strategy button */
        sendStrategyButtonTooltip.setTitle(SEND_WHOLE_STRATEGY_TOOLTIP_TEXT);
        sendStrategyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (sendCommand != null) {
                    sendCommand.execute();
                }
            }
        });

        /* Configure change mode button */
        changeModeButtonTooltip.setTitle(CHANGE_TO_CUSTOM_MODE_TOOLTIP_TEXT);
        changeModeButton.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* If showing checkboxes, mark them as selected; otherwise
                 * deselect them */
                final boolean isInSimpleMode = isInSimpleSendMode();
                for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
                    final CheckBox checkBox = activityPanelManager.getCheckBox();
                    checkBox.setValue(true);
                    checkBox.getElement().getStyle().setDisplay(isInSimpleMode ? Display.INLINE : Display.NONE);
                }

                /* Update tooltip and expand or collapse bubble appropriately */
                if (isInSimpleMode) {
                    sendStrategyButtonTooltip.setTitle(SEND_CUSTOM_STRATEGY_TOOLTIP_TEXT);
                    changeModeButtonTooltip.setTitle(CHANGE_TO_SIMPLE_MODE_TOOLTIP_TEXT);
                    changeModeButton.setIcon(IconType.GEARS);
                    expandBubble();
                } else {
                    sendStrategyButtonTooltip.setTitle(SEND_WHOLE_STRATEGY_TOOLTIP_TEXT);
                    changeModeButtonTooltip.setTitle(CHANGE_TO_CUSTOM_MODE_TOOLTIP_TEXT);
                    changeModeButton.setIcon(IconType.GEAR);
                }

                /* Set the send button to visible because we know by this point
                 * it'll either be in simple send mode or will have an activity
                 * selected */
                updateSendStrategyButtonVisible();

                fireActivitySelectionChangeCallbacks();
            }
        }, ClickEvent.getType());
    }

    /**
     * Count the icons for the activities in the strategy and add them to
     * the consecutiveActivityIcon list
     * 
     * @param consecutiveActivityIcon the list of icons containing consecutive activity icons with
     * badges containing the number of each strategy activity
     * @param strategy the strategy for which to create the activity icons
     */
    private void countActiviesAndCreateIcons(List<StrategyActivityIcon> consecutiveActivityIcon, Strategy strategy) {

        List<FlowPanel> iconContainer = StrategyActivityUtil.countActivitiesAndCreateIcons(consecutiveActivityIcon, strategy);
        for (FlowPanel icon : iconContainer) {
            collapsedStrategyIcons.add(icon);
            mainPanel.add(icon);
        }

    }

    /**
     * Builds the panel for the strategy bubble while it is expanded.
     */
    private void buildExpandedStrategyBubble() {
        /* Create row for each activity */
        for (final Serializable activity : strategy.getStrategyActivities()) {
            ActivityPanelManager activityPanelManager = new ActivityPanelManager(activity);
            expandedPanel.add(activityPanelManager.getActivityPanel());
        }

        refreshMetricPanel();
        expandedPanel.add(metricPanel);

        expandedPanel.getElement().getStyle().setPaddingLeft(20, Unit.PX);
    }

    /**
     * Builds the strategy metrics panel
     *
     * @param metricPanel the panel to populate with the desired strategy
     *        metrics data.
     */
    protected abstract void buildMetricPanel(FlowPanel metricPanel);

    /**
     * Refreshes the metric panel contents
     */
    public void refreshMetricPanel() {
        /* Clear existing contents and rebuild */
        metricPanel.clear();
        buildMetricPanel(metricPanel);

        /* Add a top padding if the panel contains a widget */
        metricPanel.getElement().getStyle().setPaddingTop(metricPanel.getWidgetCount() == 0 ? 0 : 5, Unit.PX);
    }

    /**
     * Expand the bubble if it is in the collapsed state
     */
    public void expandBubble() {
        /* Already expanded */
        if (isBubbleExpanded()) {
            return;
        }

        /* Remove the icons from the panel */
        for (FlowPanel iconContainer : collapsedStrategyIcons) {
            mainPanel.remove(iconContainer);
        }

        /* Insert expanded panel after the header panel 
         * Note: not included the fixedStrategyIcons size will make those icons appear after the strategy activities 
         */
        int insertAtLocation = mainPanel.getWidgetIndex(bubbleHeader) + fixedStrategyIcons.size() + 1;
        if (insertAtLocation < mainPanel.getWidgetCount()) {
            mainPanel.insert(expandedPanel, insertAtLocation);
        } else {
            mainPanel.add(expandedPanel);
        }
        
        addStyleName(style.selected());
    }

    /**
     * Collapse the bubble if it is in the expanded state
     */
    public void collapseBubble() {
        /* Already collapsed */
        if (!isBubbleExpanded()) {
            return;
        }

        /* Remove the expanded panel from this panel */
        mainPanel.remove(expandedPanel);

        /* Insert icons after the header panel */
        int insertAtLocation = mainPanel.getWidgetIndex(bubbleHeader) + 1;
        if (insertAtLocation < mainPanel.getWidgetCount()) {
            
            // first insert the fixed icons that should always be shown, if any
            for(FlowPanel iconContainer : fixedStrategyIcons) {
                mainPanel.insert(iconContainer, insertAtLocation++);
            }
            for (FlowPanel iconContainer : collapsedStrategyIcons) {
                mainPanel.insert(iconContainer, insertAtLocation++);
            }
        } else {
            // first add the fixed icons that should always be shown, if any
            for(FlowPanel iconContainer : fixedStrategyIcons) {
                mainPanel.add(iconContainer);
            }
            for (FlowPanel iconContainer : collapsedStrategyIcons) {
                mainPanel.add(iconContainer);
            }
        }
        
        removeStyleName(style.selected());
    }

    /**
     * Checks if the bubble is expanded or not
     *
     * @return true if the bubble is expanded; false otherwise.
     */
    public boolean isBubbleExpanded() {
        return mainPanel.getWidgetIndex(expandedPanel) >= 0;
    }

    /**
     * Refreshes any widget that has a timestamp or something else that needs
     * frequent updating.
     */
    public void refreshTimerWidgets() {
        refreshMetricPanel();
    }

    /**
     * Getter for the {@link Strategy} that this {@link StrategyBubble}
     * represents.
     *
     * @return The {@link Strategy} being represented. Can't be null.
     */
    public Strategy getStrategy() {
        return strategy;
    }

    /**
     * Getter for the {@link Collection} of strategy activities that are
     * currently selected by the {@link StrategyBubble} widget.
     *
     * @return The {@link Collection} of strategy activities. Can't be null. Can
     *         be empty.
     */
    public Collection<Serializable> getSelectedActivities() {
        /* Simple send mode always returns all activities */
        if (isInSimpleSendMode()) {
            return activityToBuilderMap.keySet();
        }

        Collection<Serializable> toRet = new ArrayList<>();
        for (Entry<Serializable, ActivityPanelManager> entry : activityToBuilderMap.entrySet()) {
            if (entry.getValue().getCheckBox().getValue() != Boolean.TRUE) {
                continue;
            }

            toRet.add(entry.getKey());
        }

        return toRet;
    }

    /**
     * Checks if the strategy bubble can only be driven by manual actions of the
     * game master (e.g. unassociated strategy).
     *
     * @return true if it is; false if it isn't.
     */
    public boolean isGameMasterDriven() {
        return isGameMasterDriven;
    }

    /**
     * Checks if the strategy bubble is in simple send mode.
     *
     * @return true if it is; false if it isn't
     */
    public boolean isInSimpleSendMode() {
        /* Must be in Game Master state and have at least 1 activity to be in
         * simple send mode */
        if (activityToBuilderMap.isEmpty() || !isGameMasterDriven()) {
            return false;
        }

        /* Check the first checkbox to see if it is visible or not. If it is not
         * visible then it is in simple mode. */
        boolean isCheckBoxVisible = activityToBuilderMap.values().iterator().next().getCheckBox().isVisible();
        return !isCheckBoxVisible;
    }
    
    /**
     * Return whether the session shown in the game master is in past session mode.
     * @return true if in past session mode, false if in active session mode
     */
    protected boolean inPastSessionMode() {
        return knowledgeSession.inPastSessionMode();
    }

    /**
     * Determines if the activity presents a feedback message to the user.
     * 
     * @param activity the activity to check.
     * @return true if the message presents a feedback message; false otherwise.
     */
    protected boolean isActivityPresentFeedbackMessage(Serializable activity) {
        if (activity instanceof InstructionalIntervention) {
            InstructionalIntervention instrInter = (InstructionalIntervention) activity;

            if (instrInter.getFeedback() != null) {
                if (instrInter.getFeedback().getFeedbackPresentation() instanceof Message) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Examines the children checkboxes and sees if they are all the same. If
     * so, the strategy checkbox needs to be updated to represent that value.
     * (E.g. if all child checkboxes are unchecked, the strategy checkbox should
     * be unchecked as well. Alternatively, if at least one child checkbox is
     * checked, the strategy checkbox should also be checked.)
     */
    private void updateStrategyCheckBoxIfNeeded() {
        /* The strategy checkbox should be selected, indeterminate, or
         * deselected if all, a subset, or none of its children are selected,
         * respectively */

        /* Determine if the strategy checkbox should be checked or
         * indeterminate */
        Boolean newValue = null;
        for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
            boolean childIsChecked = isChecked(activityPanelManager.getCheckBox());

            if (newValue == null) {
                newValue = childIsChecked;
            } else if (newValue.booleanValue() != childIsChecked) {
                /* Indeterminate state reached. No reason to examine the rest of
                 * the children. */
                newValue = null;
                break;
            }
        }

        strategyCheckBox.setValue(newValue);

        /* Show/Hide the send button iff the button group is visible */
        if (isGameMasterDriven()) {
            /* Show button if is in simple mode OR the new value is true */
            updateSendStrategyButtonVisible();
        }
    }

    /**
     * Executes the callbacks' onChange() method.
     */
    private void fireActivitySelectionChangeCallbacks() {
        for (ChangeCallback<Void> callback : activitySelectionChangeCallbacks) {
            try {
                callback.onChange(null, null);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "There was an exception thrown within a ChangeCallback", t);
            }
        }
    }

    /**
     * Subscribes a {@link ChangeCallback} to this {@link StrategyBubble}.
     *
     * @param callback the callback to be executed whenever the selected
     *        strategy activities change.
     * @return true if the callback was subscribed successfully; false
     *         otherwise.
     */
    public boolean addChangeCallback(ChangeCallback<Void> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        return activitySelectionChangeCallbacks.add(callback);
    }

    /**
     * Helper method to turn a Boolean into a boolean
     *
     * @param checkBox the checkbox to compare
     * @return true if the checkbox is checked; false otherwise
     */
    private boolean isChecked(CheckBox checkBox) {
        return Boolean.TRUE.equals(checkBox.getValue());
    }

    /**
     * Set whether this bubble has been seen by the user
     *
     * @param seen true if the bubble has been seen by the user; false
     *        otherwise.
     */
    public void setHasBeenSeen(boolean seen) {
        hasBeenSeen = seen;
    }

    /**
     * Returns if this bubble has been seen by the user.
     *
     * @return true if this bubble has been seen; false otherwise.
     */
    public boolean isHasBeenSeen() {
        return hasBeenSeen;
    }

    /**
     * The user that created the {@link #strategy} that this bubble represents.
     * 
     * @return the author of the {@link #strategy}; will be null if it came from
     *         the system.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * The user that last edited an activity within this bubble.
     * 
     * @return the user that last edited the bubble or null if no edit has been
     *         performed.
     */
    public String getLastEditedBy() {
        return lastEditedBy;
    }

    /**
     * Show or hide the send strategies button. If the session is ended, then
     * the button will always be hidden no matter the provided value.
     */
    private void updateSendStrategyButtonVisible() {
        final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        boolean running = RunState.RUNNING.equals(activeSessionProvider.getRunState(dsId));
        
        boolean eligibleToShow = isInSimpleSendMode() || Boolean.TRUE.equals(strategyCheckBox.getValue());
                
        sendStrategyButton.setVisible(running && eligibleToShow);
    }

    /**
     * Updates the strategy checkbox (and children) with the default selection
     * value.
     */
    public void revertToDefaultValue() {
        setValue(defaultSelectionChoice);
        fireActivitySelectionChangeCallbacks();
    }

    /**
     * Updates the strategy checkbox (and children) with the provided value.
     *
     * @param selected true to check the checkboxes within this bubble; false to
     *        uncheck them.
     */
    public void setValue(boolean selected) {
        for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
            activityPanelManager.getCheckBox().setValue(selected);
        }

        updateStrategyCheckBoxIfNeeded();
    }

    /**
     * Sets the command for the bubble to execute on send. Will do nothing if
     * this bubble is Game Master driven as the send command cannot be
     * overwritten in that case.
     * 
     * @param command the command to execute on send.
     * @return true if the command was set; false if it was not.
     */
    public boolean setSendCommand(Command command) {
        /* Can't overwrite the game master driven send command */
        if (isGameMasterDriven()) {
            return false;
        }

        sendCommand = command;
        return true;
    }

    /**
     * Marks this bubble as Game Master driven. This will change the bubbles
     * appearance and add an 'instant send' button. Once set, it cannot be
     * undone.
     * 
     * @param sendCommand the command to execute on send. Can't be null.
     * @param author the user that created the {@link #strategy} this bubble
     *        represents. Should be null if the strategy is from the system.
     */
    public void makeGameMasterDriven(Command sendCommand, String author) {
        /* Already game master driven */
        if (isGameMasterDriven()) {
            return;
        }

        if (sendCommand == null) {
            throw new IllegalArgumentException("The parameter 'sendCommand' cannot be null.");
        }

        isGameMasterDriven = true;
        this.sendCommand = sendCommand;
        this.author = author;
        if (StringUtils.isNotBlank(author)) {
            strategyNameLabel.addClickHandler(stopPropagationHandler);
            strategyNameLabel.setEditingEnabled(true);
        }

        /* Style the bubble color */
        this.getElement().getStyle().setBackgroundColor("rgb(255,255,150)");

        /* Remove the strategy checkbox and replace it with the send button
         * group */
        headerDeckPanel.showWidget(headerDeckPanel.getWidgetIndex(sendButtonGroup));

        /* Do not want the user to be able to select/deselect the checkboxes by
         * clicking the label */
        labelHandlerRegistration.removeHandler();

        /* Hide activity checkboxes by default */
        for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
            CheckBox activityCheckBox = activityPanelManager.getCheckBox();
            activityCheckBox.setValue(true);
            activityCheckBox.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

    /**
     * Puts the activity into an 'edit' mode so the Game Master can edit the
     * feedback
     * 
     * @param feedbackActivity the activity to edit
     * @param editStrategyName determines if the edit panel should include a
     *        field for the user to edit the strategy name.
     * @param cancelCommand an optional command to be executed if the edit is
     *        cancelled.
     */
    public void editFeedback(final InstructionalIntervention feedbackActivity, boolean editStrategyName,
            Command cancelCommand) {
        if (activityToBuilderMap.containsKey(feedbackActivity) && !isEditing()) {
            expandBubble();
            activityToBuilderMap.get(feedbackActivity).editFeedback(editStrategyName, cancelCommand);
        }
    }

    /**
     * Checks if this bubble is currently being edited
     * 
     * @return true if this bubble is being edited; false otherwise.
     */
    public boolean isEditing() {
        for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
            if (activityPanelManager.isEditing()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a {@link ClickHandler} that is executed when the user clicks the
     * close button.
     *
     * @param handler The {@link ClickHandler} to execute. Can't be null.
     * @return The {@link HandlerRegistration} that can be used to deregister
     *         the click handler later.
     */
    public HandlerRegistration addCloseButtonHandler(ClickHandler handler) {
        return closeButton.addClickHandler(handler);
    }

    /**
     * Container to hold the associated components for a single strategy
     * activity.
     * 
     * @author sharrison
     */
    protected class ActivityPanelManager {
        /** The strategy activity used to build the panel */
        private final Serializable strategyActivity;

        /** The summary panel of the activity */
        private final FlowPanel summaryPanel = new FlowPanel();

        /** The parent panel of the activity */
        private final SimplePanel activityPanel = new SimplePanel(summaryPanel);

        /** The checkbox representing if the activity is selected */
        private final CheckBox activityCheckBox = new CheckBox();

        /** Flag indicating if activity panel is currently editing */
        private boolean isEditing = false;

        /** The command to execute if cancel is clicked */
        private Command cancelCommand;

        /**
         * Constructor. Builds the activity panel based on the provided
         * activity. Holds a reference to the widgets used in the panel.
         * 
         * @param activity the strategy activity to use to build the panel.
         */
        public ActivityPanelManager(final Serializable activity) {
            this.strategyActivity = activity;

            /* initialize the activity checkbox */
            activityCheckBox.setEnabled(!readOnly);
            activityCheckBox.setValue(defaultSelectionChoice);
            activityCheckBox.getElement().getStyle().setDisplay(Display.INLINE);
            activityCheckBox.getElement().getStyle().setTop(5, Unit.PX);
            activityCheckBox.getElement().getStyle().setMargin(0, Unit.PX);
            activityCheckBox.addClickHandler(stopPropagationHandler);
            activityCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    updateStrategyCheckBoxIfNeeded();
                    fireActivitySelectionChangeCallbacks();
                }
            });

            /* Build the summary panel */
            rebuildSummary();

            activityPanel.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (readOnly || isInSimpleSendMode()) {
                        return;
                    }

                    event.stopPropagation();

                    boolean isChecked = isChecked(activityCheckBox);
                    /* Flip the value of the checkbox */
                    activityCheckBox.setValue(!isChecked);
                    updateStrategyCheckBoxIfNeeded();
                    fireActivitySelectionChangeCallbacks();
                }
            }, ClickEvent.getType());

            /* Add this component to the global map */
            activityToBuilderMap.put(activity, this);
        }

        /**
         * Builds the summary panel
         */
        public void rebuildSummary() {
            summaryPanel.clear();

            final int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
            boolean running = RunState.RUNNING.equals(activeSessionProvider.getRunState(dsId));
            if (running && isActivityPresentFeedbackMessage(strategyActivity)) {
                Icon editIcon = new Icon(IconType.PENCIL);
                ManagedTooltip editIconTooltip = new ManagedTooltip(editIcon, "Edit this activity");
                editIcon.setSize(IconSize.LARGE);
                editIcon.setMarginLeft(-15);
                editIcon.setPaddingRight(4);
                editIcon.getElement().getStyle().setCursor(Cursor.POINTER);
                editIcon.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation();
                        editFeedback(false, null);
                    }
                });

                summaryPanel.add(editIconTooltip);
            }

            StrategyActivityIcon icon = StrategyActivityUtil.getIconFromActivity(strategyActivity);
            icon.applySummaryStyle();

            summaryPanel.add(activityCheckBox);
            summaryPanel.add(icon);

            if (strategyActivity instanceof InstructionalIntervention) {
                StrategyActivityUtil.summarize((InstructionalIntervention) strategyActivity, knowledgeSession.getTrainingAppType(),
                        summaryPanel);
            } else if (strategyActivity instanceof MidLessonMedia) {
                StrategyActivityUtil.summarize((MidLessonMedia) strategyActivity, summaryPanel);
            } else if (strategyActivity instanceof PerformanceAssessment) {
                StrategyActivityUtil.summarize((PerformanceAssessment) strategyActivity, knowledgeSession.getNodeIdToNameMap(), summaryPanel);
            } else if (strategyActivity instanceof ScenarioAdaptation) {
                StrategyActivityUtil.summarize((ScenarioAdaptation) strategyActivity, summaryPanel);
            } else {
                String msg = "The wrapper is unexpectedly wrapping type " + strategyActivity.getClass().getSimpleName();
                throw new UnsupportedOperationException(msg);
            }
        }

        /**
         * Retrieve the activity panel from the builder
         * 
         * @return the activity panel
         */
        public SimplePanel getActivityPanel() {
            return activityPanel;
        }

        /**
         * The check box representing if this strategy activity is selected.
         * 
         * @return the activity check box
         */
        public CheckBox getCheckBox() {
            return activityCheckBox;
        }

        /**
         * Checks if this activity panel is currently being edited
         * 
         * @return true if this panel is being edited; false otherwise.
         */
        public boolean isEditing() {
            return isEditing;
        }

        /**
         * Puts the activity into an 'edit' mode so the Game Master can edit the
         * feedback
         * 
         * @param editStrategyName determines if the edit panel should include a
         *        field for the user to edit the strategy name.
         * @param cancelCommand an optional command to be executed if the edit
         *        is cancelled.
         */
        public void editFeedback(boolean editStrategyName, final Command cancelCommand) {
            if (isEditing()) {
                return;
            } else if (!(strategyActivity instanceof InstructionalIntervention)) {
                throw new UnsupportedOperationException("Editing is only supported by InstructionalIntervention");
            }

            this.cancelCommand = cancelCommand;

            final InstructionalIntervention iiActivity = (InstructionalIntervention) strategyActivity;
            if (iiActivity.getFeedback() == null
                    || !(iiActivity.getFeedback().getFeedbackPresentation() instanceof Message)) {
                throw new UnsupportedOperationException(
                        "Editing is only supported by InstructionalIntervention that contains a Message feedback");
            }

            StrategyBubbleEditPanel editFeedbackPanel = new StrategyBubbleEditPanel(strategy, iiActivity,
                    knowledgeSession.getTrainingAppType(), team, editStrategyName, new SaveCancelCallback() {
                        @Override
                        public void save() {
                            isEditing = false;

                            strategyNameLabel.setValue(strategy.getName());

                            /* Set lastEditedBy to the current user */
                            lastEditedBy = UiManager.getInstance().getUserName();

                            /* Rebuild the summary panel because the data has
                             * changed */
                            rebuildSummary();

                            /* Show summary */
                            activityPanel.setWidget(summaryPanel);

                            /* Remove the glass panel */
                            glassPanel.removeFromParent();
                        }

                        @Override
                        public void cancel() {
                            cancelEditing();
                        }
                    });

            /* Only show a 'short-circuit' save and apply button if it is game
             * master driven AND is the only activity within the strategy */
            if (isGameMasterDriven() && strategy.getStrategyActivities().size() == 1) {
                editFeedbackPanel.addSaveAndApplyButton("Save and Apply", true, new Command() {
                    @Override
                    public void execute() {
                        if (sendCommand != null) {
                            /* Make activity selected */
                            for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
                                activityPanelManager.getCheckBox().setValue(true);
                            }

                            sendCommand.execute();
                        }
                    }
                });
            }

            activityPanel.setWidget(editFeedbackPanel);
            mainPanel.add(glassPanel);
            isEditing = true;
        }

        /**
         * Cancel editing the activity. Will do nothing if not currently
         * editing.
         */
        public void cancelEditing() {
            if (!isEditing()) {
                return;
            }

            isEditing = false;

            /* Show summary */
            activityPanel.setWidget(summaryPanel);

            /* Remove the glass panel */
            glassPanel.removeFromParent();

            /* If the caller provided a cancel command, execute it */
            if (cancelCommand != null) {
                cancelCommand.execute();
            }
        }
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        /* Only process the knowledge session for this data panel */
        if (!this.knowledgeSession.equals(knowledgeSession)) {
            return;
        }

        updateSendStrategyButtonVisible();
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

        updateSendStrategyButtonVisible();

        for (ActivityPanelManager activityPanelManager : activityToBuilderMap.values()) {
            activityPanelManager.cancelEditing();
            activityPanelManager.rebuildSummary();
        }
    }
}