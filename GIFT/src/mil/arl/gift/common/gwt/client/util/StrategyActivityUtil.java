/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.util;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;
import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.color;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Badge;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.ATRemoteSKO;
import generated.dkf.Audio;
import generated.dkf.AutoTutorSKO;
import generated.dkf.BooleanEnum;
import generated.dkf.Conversation;
import generated.dkf.ConversationTreeFile;
import generated.dkf.Coordinate;
import generated.dkf.DelayAfterStrategy;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.EnvironmentAdaptation.CreateActors;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side.Civilian;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor;
import generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs;
import generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo;
import generated.dkf.EnvironmentAdaptation.Endurance;
import generated.dkf.EnvironmentAdaptation.FatigueRecovery;
import generated.dkf.EnvironmentAdaptation.Fog;
import generated.dkf.EnvironmentAdaptation.Fog.Color;
import generated.dkf.EnvironmentAdaptation.HighlightObjects;
import generated.dkf.EnvironmentAdaptation.Overcast;
import generated.dkf.EnvironmentAdaptation.Rain;
import generated.dkf.EnvironmentAdaptation.RemoveActors;
import generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs;
import generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects;
import generated.dkf.EnvironmentAdaptation.Script;
import generated.dkf.EnvironmentAdaptation.Teleport;
import generated.dkf.EnvironmentAdaptation.TimeOfDay;
import generated.dkf.Feedback;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.ImageProperties;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Media;
import generated.dkf.MediaSemantics;
import generated.dkf.Message;
import generated.dkf.MidLessonMedia;
import generated.dkf.PDFProperties;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.SlideShowProperties;
import generated.dkf.Strategy;
import generated.dkf.TeamRef;
import generated.dkf.VideoProperties;
import generated.dkf.WebpageProperties;
import generated.dkf.YoutubeVideoProperties;
import mil.arl.gift.common.enums.MessageFeedbackDisplayModeEnum;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Utility class for strategy activities.
 * 
 * @author sharrison
 */
public class StrategyActivityUtil {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyActivityUtil.class.getName());
    
    /** The type of the activity used to map the icon and tooltip text */
    public enum ActivityType {
        MESSAGE_FEEDBACK,
        AUDIO_FEEDBACK,
        AVATAR_FEEDBACK,
        FILE_FEEDBACK,
        OTHER_FEEDBACK,
        MIDLESSON_MEDIA,
        CONVERSATION_TREE,
        AUTOTUTOR_CONVERSATION,
        SURVEY_ACTIVITY,
        UNKNOWN,
        UNKNOWN_PA,
        UNKNOWN_EA,
        UNKNOWN_SA,
        UNSPECIFIED,
        FATIGUE_RECOVERY,
        FOG,
        RAIN,
        OVERCAST,
        TELEPORT,
        CREATE_ACTORS,
        REMOVE_ACTORS,
        SCRIPT,
        TIME_OF_DAY,
        ENDURANCE,
        HIGHLIGHT,
        REMOVE_HIGHLIGHT,
        CREATE_BREADCRUMBS,
        REMOVE_BREADCRUMBS
    }

    /** A ManagedTooltip for strategy activity icons specifying the type of the activity */
    public static class StrategyActivityIcon extends ManagedTooltip {

        /** The type of the strategy activity */
        final private ActivityType type;
        
        /** The number of consecutive icons of this strategy activity */
        private int badgeCount = 1;

        /** Interface to allow CSS file access */
        public interface Bundle extends ClientBundle {

            /** The instance of the bundle */
            public static final Bundle INSTANCE = GWT.create(Bundle.class);
            
            /**
             * The specific css resource
             * 
             * @return the css resource
             */
            @Source("StrategyActivityIconStyles.css")
            public MyResources css();
        }

        /** Interface to allow CSS style name access */
        interface MyResources extends CssResource {

            /**
             * Styles the icon with the default style
             * 
             * @return the style name
             */
            String iconStyle();

            /**
             * Styles the icon with the summary style
             * 
             * @return the style name
             */
            String summaryStyle();

            /**
             * Styles the icon with the toast style
             * 
             * @return the style name
             */
            String toastStyle();

            /**
             * Styles the icon with the GAT style
             * 
             * @return the style name
             */
            String gatStyle();

            /**
             * Styles the icon with the state transition panel style
             * 
             * @return the style name
             */
            String stateTransitionPanelStyle();
        }

        /** The CSS resource */
        protected static final MyResources style = Bundle.INSTANCE.css();

        static {
            /* Make sure the css style names are accessible */
            Bundle.INSTANCE.css().ensureInjected();
        }
        
        /** The list of styles applied to the Strategy Activity Icon */
        private String styleApplied;

        /**
         * Creates a tooltip for strategy activity type icons 
         * 
         * @param type the type of the strategy activity
         * @param icon icon widget for the tooltip
         * @param title title for the tooltip
         */
        public StrategyActivityIcon(ActivityType type, Icon icon, String title) {
            super(icon, title);
            
            if (type == null) {
                throw new IllegalArgumentException("ActivityType cannot be null for StrategyActivityIcon.");
            }
            
            this.type = type;

            applyIconStyle();
        }

        /**
         * Applies the supplied icon style to the icon.
         * 
         * @param icon the icon with which to apply the style. Can't be null.
         * @param style the name of the style to apply to the icon. Can't be
         *        blank.
         */
        private void applyIconStyle(Icon icon, String style) {
            if (icon == null) {
                throw new IllegalArgumentException("The parameter 'icon' cannot be null.");
            } else if (StringUtils.isBlank(style)) {
                throw new IllegalArgumentException("The parameter 'style' cannot be blank.");
            }

            /* Remove the existing applied style if it exists */
            if (StringUtils.isNotBlank(styleApplied)) {
                icon.removeStyleName(styleApplied);
            }

            /* Set the applied style to the icon */
            icon.addStyleName(style);
            styleApplied = style;
        }

        /** Applies styling to the icon used by the Strategy Bubble  */
        public void applyIconStyle() {
            Icon icon = (Icon) getWidget();
            icon.setSize(IconSize.LARGE);
            applyIconStyle(icon, style.iconStyle());
        }

        /** Applies styling to the icon used in the summary panel */
        public void applySummaryStyle() {
            Icon icon = (Icon) getWidget();
            icon.setSize(IconSize.TIMES2);
            applyIconStyle(icon, style.summaryStyle());
        }
        
        /** Applies styling to the icon used in a toast alert */
        public void applyToastStyle() {
            Icon icon = (Icon) getWidget();
            icon.setSize(IconSize.TIMES2);
            applyIconStyle(icon, style.toastStyle());
        }
        
        /** Applies styling to the icon used in the GAT */
        public void applyGATStyle() {
            Icon icon = (Icon) getWidget();
            icon.setSize(IconSize.TIMES2);
            applyIconStyle(icon, style.gatStyle());
        }
        
        /** Applies styling to the icon used in the state transition panel */
        public void applyStateTransitionPanelStyle() {
            Icon icon = (Icon) getWidget();
            icon.setSize(IconSize.TIMES2);
            applyIconStyle(icon, style.stateTransitionPanelStyle());
        }
        
        /** getter for the type
         * 
         * @return the type of {@link ActivityType}
         */
        public ActivityType getType() {
            return this.type;
        }
        
        /** getter for the badge count
         * 
         * @return the badge count
         */
        public int getCount() {
            return badgeCount;
        }
        
        /** Increments the badge count number */
        public void incrementCount() {
            badgeCount++;
        }

    }


    /** The icon type for the {@link Fatigue} subeditor */
    public static final IconType SA_FATIGUE_ICON_TYPE = IconType.BATTERY_QUARTER;

    /** The icon type for the {@link Fog} subeditor */
    public static final IconType SA_FOG_ICON_TYPE = IconType.LOW_VISION;

    /** The icon type for the {@link Rain} subeditor */
    public static final IconType SA_RAIN_ICON_TYPE = IconType.TINT;

    /** The icon type for the {@link Overcast} subeditor */
    public static final IconType SA_OVERCAST_ICON_TYPE = IconType.CLOUD;

    /** The icon type for the {@link TeleportLearner} subeditor */
    public static final IconType SA_TELEPORT_LEARNER_ICON_TYPE = IconType.STREET_VIEW;

    /** The icon type for the {@link CreateActors} subeditor */
    public static final IconType SA_CREATE_ACTORS_ICON_TYPE = IconType.USER_PLUS;

    /** The icon type for the {@link RemoveActors} subeditor */
    public static final IconType SA_REMOVE_ACTORS_ICON_TYPE = IconType.USER_TIMES;

    /** The icon type for the {@link Script} subeditor */
    public static final IconType SA_SCRIPT_ICON_TYPE = IconType.TERMINAL;

    /** The icon type for the {@link TimeOfDay} subeditor */
    public static final IconType SA_TIME_OF_DAY_ICON_TYPE = IconType.SUN_O;

    /** The icon type for the {@link Endurance} subeditor */
    public static final IconType SA_ENDURANCE_ICON_TYPE = IconType.BATTERY_THREE_QUARTERS;
    
    /** The icon type for the {@link HighlightObjects} subeditor */
    public static final IconType SA_HIGHLIGHT_ICON_TYPE = IconType.MOUSE_POINTER;
    
    /** The icon type for the {@link RemoveHighlightOnObjects} subeditor */
    public static final IconType SA_REMOVE_HIGHLIGHT_ICON_TYPE = IconType.ERASER;
    
    /** The icon type for the {@link CreateBreadcrumbs} subeditor */
    public static final IconType SA_CREATE_BREADCRUMBS_ICON_TYPE = IconType.ROAD;
    
    /** The icon type for the {@link RemoveBreadcrumbs} subeditor */
    public static final IconType SA_REMOVE_BREADCRUMBS_ICON_TYPE = IconType.ERASER;
    
    /** The icon type for the {@link HighlightObjects.TeamMemberRef} subeditor */
    public static final IconType SA_HIGHLIGHT_TEAM_MEMBER_ICON_TYPE = IconType.USER;
    
    /** The icon type for the {@link Coordinate} subeditor for highlight objects */
    public static final IconType SA_HIGHLIGHT_LOCATION_ICON_TYPE = IconType.MAP_MARKER;

    /** The icon tooltip for the {@link Fatigue} subeditor */
    public static final String SA_FATIGUE_ICON_TOOLTIP = "Fatigue Recovery";

    /** The icon tooltip for the {@link Fog} subeditor */
    public static final String SA_FOG_ICON_TOOLTIP = "Fog";

    /** The icon tooltip for the {@link Rain} subeditor */
    public static final String SA_RAIN_ICON_TOOLTIP = "Rain";

    /** The icon tooltip for the {@link Overcast} subeditor */
    public static final String SA_OVERCAST_ICON_TOOLTIP = "Overcast";

    /** The icon tooltip for the {@link TeleportLearner} subeditor */
    public static final String SA_TELEPORT_LEARNER_ICON_TOOLTIP = "Teleport";

    /** The icon tooltip for the {@link CreateActors} subeditor */
    public static final String SA_CREATE_ACTORS_ICON_TOOLTIP = "Create Actors";

    /** The icon tooltip for the {@link RemoveActors} subeditor */
    public static final String SA_REMOVE_ACTORS_ICON_TOOLTIP = "Remove Actors";

    /** The icon tooltip for the {@link Script} subeditor */
    public static final String SA_SCRIPT_ICON_TOOLTIP = "Script";

    /** The icon tooltip for the {@link TimeOfDay} subeditor */
    public static final String SA_TIME_OF_DAY_ICON_TOOLTIP = "Time of Day";

    /** The icon tooltip for the {@link Endurance} subeditor */
    public static final String SA_ENDURANCE_ICON_TOOLTIP = "Endurance";

    /** The icon tooltip for an unknown scenario adaptation */
    private static final String SA_UNKNOWN_TOOLTIP = "Unknown Scenario Adaptation";

    /** The icon tooltip for an unknown source */
    private static final String UNKNOWN_TOOLTIP = "Unknown";

    /** The icon tooltip for an unspecified source */
    private static final String UNSPECIFIED_TOOLTIP = "Unspecified";

    /** The icon tooltip for an unknown performance assessment */
    private static final String UNKNOWN_PERFORMANCE_ASSESSMENT_TOOLTIP = "Unknown Performance Assessment";

    /** The icon tooltip for an unknown environment adaptation */
    private static final String UNKNOWN_ENVIRONMENT_ADAPTATION_TOOLTIP = "Unknown Environment Adaptation";
    
    /** The icon tooltip for the {@link HighlightObjects} subeditor */
    public static final String SA_HIGHLIGHT_ICON_TOOLTIP = "Highlight Object";
    
    /** The icon tooltip for the {@link RemoveHighlightOnObjects} subeditor */
    public static final String SA_REMOVE_HIGHLIGHT_ICON_TOOLTIP = "Remove Highlight";
    
    /** The icon tooltip for the {@link CreateBreadcrumbs} subeditor */
    public static final String SA_CREATE_BREADCRUMBS_ICON_TOOLTIP = "Breadcrumbs";
    
    /** The icon tooltip for the {@link RemoveBreadcrumbs} subeditor */
    public static final String SA_REMOVE_BREADCRUMBS_ICON_TOOLTIP = "Remove Breadcrumbs";
    
    /** The icon tooltip for the {@link HighlightObjects.TeamMemberRef} subeditor */
    public static final String SA_HIGHLIGHT_TEAM_MEMBER_ICON_TOOLTIP = "Highlight Team Member";
    
    /** The icon tooltip for the {@link Coordinate} subeditor for highlighting object */
    public static final String SA_HIGHLIGHT_LOCATION_ICON_TOOLTIP = "Highlight Location";

    /** Tooltip text for audio feedback type activities */
    private static final String AUDIO_FEEDBACK_ACTIVITY_TOOLTIP_TEXT = "Present audio feedback to the learner";

    /** Tooltip text for scripted avatar feedback type activities */
    private static final String AVATAR_FEEDBACK_ACTIVITY_TOOLTIP_TEXT = "Deliver a scripted avatar message to the learner";

    /** Tooltip text for message feedback type activities */
    private static final String MESSAGE_FEEDBACK_ACTIVITY_TOOLTIP_TEXT = "Deliver a message to the learner";

    /** Tooltip text for feedback file type activities */
    private static final String FILE_FEEDBACK_ACTIVITY_TOOLTIP_TEXT = "Feedback File";

    /** Tooltip text for unknown feedback type activities */
    private static final String OTHER_FEEDBACK_ACTIVITY_TOOLTIP_TEXT = "Instructional Intervention";

    /** Tooltip text for mid-lesson media type activities */
    private static final String MIDLESSON_MEDIA_ACTIVITY_TOOLTIP_TEXT = "Delivers a media resource to the learner";

    /** Tooltip text for conversation tree type activities */
    private static final String CONVERSATION_TREE_ACTIVITY_TOOLTIP_TEXT = "Starts a Conversation Tree conversation with the learner";

    /** Tooltip text for auto tutor conversation type activities */
    private static final String AUTOTUTOR_CONVERSATION_ACTIVITY_TOOLTIP_TEXT = "Starts an AutoTutor conversation with the learner";

    /** Tooltip text for survey type activities */
    private static final String SURVEY_ACTIVITY_TOOLTIP_TEXT = "Assesses the learner with a survey";

    /**
     * Retrieves the icon used for the provided activity type. Wraps the icon in a tooltip.
     * 
     * @param activity the activity used to find the correct icon.
     * @return a {@link StrategyActivityIcon} wrapping an {@link Icon} which represents
     * the provided activity type.
     */
    public static StrategyActivityIcon getIconFromActivity(Serializable activity) {
        ActivityType activityType = getActivityTypeFromActivity(activity);
        return getIconFromActivityType(activityType);
    }

    /**
     * Retrieves the {@link ActivityType} from the activity. Used to determine icon and tooltip value
     * 
     * @param activity the {@link Serializable} activity used to find the type.
     * @return the {@link ActivityType} which represents the type of the {@link Serializable} activity.
     */
    public static ActivityType getActivityTypeFromActivity(Serializable activity) {
        final ActivityType activityType;
        if (activity instanceof InstructionalIntervention) {
            InstructionalIntervention iIntervention = (InstructionalIntervention) activity;
            Serializable feedbackPresentation = iIntervention.getFeedback().getFeedbackPresentation();
            if (feedbackPresentation instanceof Message) {
                activityType = ActivityType.MESSAGE_FEEDBACK;
            } else if (feedbackPresentation instanceof Audio) {
                activityType = ActivityType.AUDIO_FEEDBACK;
            } else if (feedbackPresentation instanceof MediaSemantics) {
                activityType = ActivityType.AVATAR_FEEDBACK;
            } else if (feedbackPresentation instanceof Feedback.File) {
                activityType = ActivityType.FILE_FEEDBACK;
            } else {
                activityType = ActivityType.OTHER_FEEDBACK;
            }
        } else if (activity instanceof MidLessonMedia) {
            activityType = ActivityType.MIDLESSON_MEDIA;
        } else if (activity instanceof PerformanceAssessment) {
            PerformanceAssessment perfAssess = (PerformanceAssessment) activity;
            final Serializable assessmentType = perfAssess.getAssessmentType();
            if (assessmentType instanceof Conversation) {
                if (((Conversation) assessmentType).getType() instanceof ConversationTreeFile) {
                    activityType = ActivityType.CONVERSATION_TREE;
                } else {
                    activityType = ActivityType.AUTOTUTOR_CONVERSATION;
                }
            } else if (assessmentType instanceof PerformanceAssessment.PerformanceNode) {
                activityType = ActivityType.SURVEY_ACTIVITY;
            } else {

                activityType = activity == null ? ActivityType.UNKNOWN_PA : ActivityType.UNKNOWN;
                String msg = new StringBuilder("The performance assessment type '")
                        .append(activity == null ? "null" : activity.getClass().getSimpleName())
                        .append("' was unrecognized").toString();
                logger.warning(msg);
            }
        } else if (activity instanceof ScenarioAdaptation) {
            ScenarioAdaptation scenarioAdaptation = (ScenarioAdaptation) activity;
            EnvironmentAdaptation environmentAdaptation = scenarioAdaptation.getEnvironmentAdaptation();
            if (environmentAdaptation != null) {
                Serializable adaptationType = environmentAdaptation.getType();
                if (adaptationType instanceof FatigueRecovery) {
                    activityType = ActivityType.FATIGUE_RECOVERY;
                } else if (adaptationType instanceof Fog) {
                    activityType = ActivityType.FOG;
                } else if (adaptationType instanceof Rain) {
                    activityType = ActivityType.RAIN;
                } else if (adaptationType instanceof Overcast) {
                    activityType = ActivityType.OVERCAST;
                } else if (adaptationType instanceof Teleport) {
                    activityType = ActivityType.TELEPORT;
                } else if (adaptationType instanceof CreateActors) {
                    activityType = ActivityType.CREATE_ACTORS;
                } else if (adaptationType instanceof RemoveActors) {
                    activityType = ActivityType.REMOVE_ACTORS;
                } else if (adaptationType instanceof Script) {
                    activityType = ActivityType.SCRIPT;
                } else if (adaptationType instanceof TimeOfDay) {
                    activityType = ActivityType.TIME_OF_DAY;
                } else if (adaptationType instanceof Endurance) {
                    activityType = ActivityType.ENDURANCE;
                } else if(adaptationType instanceof HighlightObjects){
                    activityType = ActivityType.HIGHLIGHT;
                } else if(adaptationType instanceof RemoveHighlightOnObjects){
                    activityType = ActivityType.REMOVE_HIGHLIGHT;
                } else if(adaptationType instanceof CreateBreadcrumbs){
                    activityType = ActivityType.CREATE_BREADCRUMBS;
                }else if(adaptationType instanceof RemoveBreadcrumbs){
                    activityType = ActivityType.REMOVE_BREADCRUMBS;
                } else {
                    activityType = ActivityType.UNKNOWN_EA;
                }
            } else {
                activityType = ActivityType.UNKNOWN_SA;
            }
        } else {
            /* Unrecognized/Unspecified activity type. */
            activityType = activity == null ? ActivityType.UNSPECIFIED : ActivityType.UNKNOWN;
        }

        return activityType;
    }

    /**
     * Retrieves the {@link StrategyActivityIcon} used for the provided activity type.
     * The tooltip widget is always an icon.
     * 
     * @param activityType the activity type used to find the correct icon.
     * @return a {@link StrategyActivityIcon} wrapping an {@link Icon} which represents the
     * provided activity type.
     */
    public static StrategyActivityIcon getIconFromActivityType(ActivityType activityType) {
        IconType iconType = IconType.QUESTION;
        String tooltipText = UNKNOWN_TOOLTIP;
        switch (activityType) {
            case MESSAGE_FEEDBACK:
                iconType = IconType.COMMENT;
                tooltipText = MESSAGE_FEEDBACK_ACTIVITY_TOOLTIP_TEXT;
                break;
            case AUDIO_FEEDBACK:
                iconType = IconType.MUSIC;
                tooltipText = AUDIO_FEEDBACK_ACTIVITY_TOOLTIP_TEXT;
                break;
            case AVATAR_FEEDBACK:
                iconType = IconType.USER;
                tooltipText = AVATAR_FEEDBACK_ACTIVITY_TOOLTIP_TEXT;
                break;
            case FILE_FEEDBACK:
                iconType = IconType.FILE;
                tooltipText = FILE_FEEDBACK_ACTIVITY_TOOLTIP_TEXT;
                break;
            case OTHER_FEEDBACK:
                iconType = IconType.QUESTION;
                tooltipText = OTHER_FEEDBACK_ACTIVITY_TOOLTIP_TEXT;
                break;
            case MIDLESSON_MEDIA:
                iconType = IconType.FILE;
                tooltipText = MIDLESSON_MEDIA_ACTIVITY_TOOLTIP_TEXT;
                break;
            case CONVERSATION_TREE:
                iconType = IconType.COMMENTS;
                tooltipText = CONVERSATION_TREE_ACTIVITY_TOOLTIP_TEXT;
                break;
            case AUTOTUTOR_CONVERSATION:
                iconType = IconType.COMMENTS;
                tooltipText = AUTOTUTOR_CONVERSATION_ACTIVITY_TOOLTIP_TEXT;
                break;
            case SURVEY_ACTIVITY:
                iconType = IconType.PENCIL_SQUARE;
                tooltipText = SURVEY_ACTIVITY_TOOLTIP_TEXT;
                break;
            case UNKNOWN:
                iconType = IconType.QUESTION;
                tooltipText = UNKNOWN_TOOLTIP;
                break;
            case UNKNOWN_PA:
                iconType = IconType.QUESTION;
                tooltipText = UNKNOWN_PERFORMANCE_ASSESSMENT_TOOLTIP;
                break;
            case UNKNOWN_EA:
                iconType = IconType.QUESTION;
                tooltipText = UNKNOWN_ENVIRONMENT_ADAPTATION_TOOLTIP;
                break;
            case UNKNOWN_SA:
                iconType = IconType.QUESTION;
                tooltipText = SA_UNKNOWN_TOOLTIP;
                break;
            case UNSPECIFIED:
                iconType = IconType.QUESTION;
                tooltipText = UNSPECIFIED_TOOLTIP;
                break;
            case FATIGUE_RECOVERY:
                iconType = SA_FATIGUE_ICON_TYPE;
                tooltipText = SA_FATIGUE_ICON_TOOLTIP;
                break;
            case FOG:
                iconType = SA_FOG_ICON_TYPE;
                tooltipText = SA_FOG_ICON_TOOLTIP;
                break;
            case RAIN:
                iconType = SA_RAIN_ICON_TYPE;
                tooltipText = SA_RAIN_ICON_TOOLTIP;
                break;
            case OVERCAST:
                iconType = SA_OVERCAST_ICON_TYPE;
                tooltipText = SA_OVERCAST_ICON_TOOLTIP;
                break;
            case TELEPORT:
                iconType = SA_TELEPORT_LEARNER_ICON_TYPE;
                tooltipText = SA_TELEPORT_LEARNER_ICON_TOOLTIP;
                break;
            case CREATE_ACTORS:
                iconType = SA_CREATE_ACTORS_ICON_TYPE;
                tooltipText = SA_CREATE_ACTORS_ICON_TOOLTIP;
                break;
            case REMOVE_ACTORS:
                iconType = SA_REMOVE_ACTORS_ICON_TYPE;
                tooltipText = SA_REMOVE_ACTORS_ICON_TOOLTIP;
                break;
            case SCRIPT:
                iconType = SA_SCRIPT_ICON_TYPE;
                tooltipText = SA_SCRIPT_ICON_TOOLTIP;
                break;
            case TIME_OF_DAY:
                iconType = SA_TIME_OF_DAY_ICON_TYPE;
                tooltipText = SA_TIME_OF_DAY_ICON_TOOLTIP;
                break;
            case ENDURANCE:
                iconType = SA_ENDURANCE_ICON_TYPE;
                tooltipText = SA_ENDURANCE_ICON_TOOLTIP;
                break;
            case HIGHLIGHT:
                iconType = SA_HIGHLIGHT_ICON_TYPE;
                tooltipText = SA_HIGHLIGHT_ICON_TOOLTIP;
                break;
            case REMOVE_HIGHLIGHT:
                iconType = SA_REMOVE_HIGHLIGHT_ICON_TYPE;
                tooltipText = SA_REMOVE_HIGHLIGHT_ICON_TOOLTIP;
                break;
            case CREATE_BREADCRUMBS:
                iconType = SA_CREATE_BREADCRUMBS_ICON_TYPE;
                tooltipText = SA_CREATE_BREADCRUMBS_ICON_TOOLTIP;
                break;
            case REMOVE_BREADCRUMBS:
                iconType = SA_REMOVE_BREADCRUMBS_ICON_TYPE;
                tooltipText = SA_REMOVE_BREADCRUMBS_ICON_TOOLTIP;
                break;
        }

        return new StrategyActivityIcon(activityType, new Icon(iconType), tooltipText);
    }

    /**
     * Count the icons for the activities in the strategy and add them to
     * the panel
     * 
     * @param consecutiveActivityIcon the modifiable list of {@link StrategyActivityIcon} to add
     * the strategy activities from the strategy to
     * @param strategy the strategy for which to create the activity icons
     * @return the list of panels containing the icons and badges
     */
    public static List<FlowPanel> countActivitiesAndCreateIcons(List<StrategyActivityIcon> consecutiveActivityIcon, Strategy strategy) {

        // count the number of each activity
        for (Serializable activity : strategy.getStrategyActivities()) {
            countConsecutiveStrategyActivityIcons(consecutiveActivityIcon, activity);
        }
        
        List<FlowPanel> iconContainer = new ArrayList<FlowPanel>();

        // add the icons with badges
        for (int i=0; i<consecutiveActivityIcon.size(); i++) {
            StrategyActivityIcon icon = consecutiveActivityIcon.get(i);
            FlowPanel container = new FlowPanel();
            container.add(icon);
            container.getElement().getStyle().setPosition(Position.RELATIVE);
            container.getElement().getStyle().setMarginRight(-4, Unit.PX);
            Integer count = icon.getCount();
            if (count > 1) {
                Badge badge = new Badge();
                badge.getElement().getStyle().setFontSize(10, Unit.PX);
                badge.getElement().getStyle().setPosition(Position.ABSOLUTE);
                badge.getElement().getStyle().setBottom(-6, Unit.PX);
                badge.getElement().getStyle().setRight(-6, Unit.PX);
                badge.setText(count.toString());
                container.add(badge);
                container.getElement().getStyle().setMarginRight(4, Unit.PX);
            }
            iconContainer.add(container);
        }
        
        return iconContainer;
    }

    /**
     * Checks the type of a strategy activity and add it to the count for that icon badge
     * 
     * @param consecutiveActivityIcon the modifiable list of {@link StrategyActivityIcon} to add
     * the strategy activities from the strategy to
     * @param activity the strategy activity.
     */
    private static void countConsecutiveStrategyActivityIcons(List<StrategyActivityIcon> consecutiveActivityIcon, Serializable activity) {
        ActivityType activityType = StrategyActivityUtil.getActivityTypeFromActivity(activity);
        // if the last activity type is the same as the current one, increment the badge count
        int lastIndex = consecutiveActivityIcon.size() - 1;
        int iconSize = consecutiveActivityIcon.size();
        if (iconSize > 0 && consecutiveActivityIcon.get(lastIndex).getType() == activityType) {
            consecutiveActivityIcon.get(lastIndex).incrementCount();
        }
        // if the last activity type is different than the current one, create a new icon
        else {
            consecutiveActivityIcon.add(StrategyActivityUtil.getIconFromActivityType(activityType));
        }
    }

    /**
     * Creates a summary widget describing a provided {@link DelayAfterStrategy} and appends it to a
     * {@link FlowPanel}.
     *
     * @param delay The {@link DelayAfterStrategy} to describe with the summary widget. If delay is
     *        null, no action is taken.
     * @param flowPanel The {@link FlowPanel} to which to append the summary widget.
     */
    private static void summarize(DelayAfterStrategy delay, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(delay, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        /* If a delay has not been defined, there is nothing to append to the flowPanel */
        if (delay == null) {
            return;
        }

        int delayInSeconds = delay.getDuration().intValue();

        Icon icon = new Icon(IconType.CLOCK_O);
        icon.setSize(IconSize.LARGE);
        icon.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        icon.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        Tooltip iconTooltip = new ManagedTooltip(icon,
                "Waits " + FormattedTimeBox.getDisplayText(delayInSeconds) + " before moving on to the next strategy");

        flowPanel.add(iconTooltip);
    }

    /**
     * Creates a summary for an {@link InstructionalIntervention}.
     *
     * @param instructionalIntervention The {@link InstructionalIntervention} to summarize.
     * @param trainingApplicationType the type of training application that contains this
     *        instructional intervention.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    public static void summarize(InstructionalIntervention instructionalIntervention,
            TrainingApplicationEnum trainingApplicationType, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(instructionalIntervention, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        Feedback feedback = instructionalIntervention.getFeedback();
        if (feedback == null) {
            return;
        }

        Serializable presentation = feedback.getFeedbackPresentation();
        if (presentation instanceof Message) {
            summarize((Message) presentation, feedback.getTeamRef(), trainingApplicationType, flowPanel);
        } else if (presentation instanceof Audio) {
            summarize((Audio) presentation, feedback.getTeamRef(), flowPanel);
        } else if (presentation instanceof MediaSemantics) {
            summarize((MediaSemantics) presentation, trainingApplicationType, flowPanel);
        } else if (presentation instanceof Feedback.File) {
            summarize((Feedback.File) presentation, flowPanel);
        }

        summarize(instructionalIntervention.getDelayAfterStrategy(), flowPanel);

        if (!feedback.getTeamRef().isEmpty()) {

            int count = feedback.getTeamRef().size();

            Badge badge = new Badge("" + count);

            Icon icon = new Icon(IconType.USERS);
            icon.getElement().getStyle().setFontSize(16, Unit.PX);
            icon.setPaddingLeft(5);

            badge.add(icon);

            Tooltip tooltip = new ManagedTooltip(badge);
            
            if(count == 1) {
                tooltip.setTitle("Only display this feedback to " + feedback.getTeamRef().get(0).getValue());
            
            } else {
                tooltip.setTitle("Only display this feedback to " + count + " specific team members");
            }

            flowPanel.add(badge);
        }
    }

    /**
     * Creates a summary for a {@link Message}.
     *
     * @param message The {@link Message} to summarize.
     * @param teamRefs The list of {@link TeamRef} to send the message to
     * @param trainingApplicationType the type of training application that contains this message.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    private static void summarize(Message message, List<TeamRef> teamRefs, TrainingApplicationEnum trainingApplicationType,
            FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(message, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        /* Show the message that will be shown if one has been defined. */
        String content = message.getContent();
        if (StringUtils.isNotBlank(content)) {
            InlineHTML summaryTextHtml = new InlineHTML("\"" + content + "\"");
            summaryTextHtml.getElement().getStyle().setMarginRight(8, Unit.PX);
            flowPanel.add(summaryTextHtml);
        }

        /* Add icons representing how the message will be displayed */
        for (Widget widget : getMessagePresentationDisplay(message, teamRefs, trainingApplicationType)) {
            widget.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
            widget.getElement().getStyle().setMarginRight(4, Unit.PX);
            flowPanel.add(widget);
        }
    }

    /**
     * Gets a list of widgets that represent how the given message's presentation settings are
     * configured
     *
     * @param message the message whose presentation settings are being displayed
     * @param teamRefs The list of {@link TeamRef} to send the message to
     * @param trainingApplicationType the type of training application that contains this message.
     * @return a list of widgets representing the given message's presentation configuration
     */
    public static List<Widget> getMessagePresentationDisplay(Message message, List<TeamRef> teamRefs,
            TrainingApplicationEnum trainingApplicationType) {

        List<Widget> presentationWidgets = new ArrayList<>();

        if (message != null && message.getDelivery() != null) {

            if (message.getDelivery().getInTrainingApplication() != null) {

                if (message.getDelivery().getInTrainingApplication().getEnabled() != null
                        && message.getDelivery().getInTrainingApplication().getEnabled().equals(BooleanEnum.TRUE)) {
                    Image image = new Image(TrainingApplicationEnum.getTrainingAppTypeIcon(trainingApplicationType));
                    image.setSize("1.34em", "1.34em");
                    image.getElement().getStyle().setProperty("margin", "0px 3px");

                    Tooltip tooltip = new ManagedTooltip(image);
                    tooltip.setTitle("Allow the training application to show the feedback message (if supported)");

                    presentationWidgets.add(image);

                    if (message.getDelivery().getInTrainingApplication().getMobileOption() != null) {
                        if (message.getDelivery().getInTrainingApplication().getMobileOption().isVibrate()) {

                            Image vimage = new Image("images/vibrate-phone.png");
                            vimage.setSize("1.34em", "1.34em");
                            vimage.getElement().getStyle().setProperty("margin", "0px 3px");

                            Tooltip vtooltip = new ManagedTooltip(vimage);
                            vtooltip.setTitle("Have the mobile device vibrate when displaying the feedback message");

                            presentationWidgets.add(vimage);
                        }
                    }
                }

            }

            if (message.getDelivery().getInTutor() != null) {

                String messagePresentation = message.getDelivery().getInTutor().getMessagePresentation();
                String textEnhancement = message.getDelivery().getInTutor().getTextEnhancement();

                boolean showText = false;
                boolean showAvatar = false;

                if (MessageFeedbackDisplayModeEnum.TEXT_ONLY.getName().equals(messagePresentation)) {
                    showText = true;

                } else if (MessageFeedbackDisplayModeEnum.AVATAR_ONLY.getName().equals(messagePresentation)) {
                    showAvatar = true;

                } else if (MessageFeedbackDisplayModeEnum.AVATAR_AND_TEXT.getName().equals(messagePresentation)) {
                    showText = true;
                    showAvatar = true;
                }

                if (showText) {
                    addShowTextWidgets(presentationWidgets);
                }

                if (showAvatar) {
                    addCharacterSayFeedbackWidgets(presentationWidgets);
                }

                boolean playBeep = false;
                boolean playFlash = false;

                if (TextFeedbackDisplayEnum.BEEP_ONLY.toString().equals(textEnhancement)) {
                    playBeep = true;

                } else if (TextFeedbackDisplayEnum.FLASH_ONLY.toString().equals(textEnhancement)) {
                    playFlash = true;

                } else if (TextFeedbackDisplayEnum.BEEP_AND_FLASH.toString().equals(textEnhancement)) {
                    playBeep = true;
                    playFlash = true;
                }

                if (playBeep) {
                    Icon icon = new Icon(IconType.BELL);
                    icon.setSize(IconSize.LARGE);
                    icon.getElement().getStyle().setProperty("margin", "0px 3px");

                    Tooltip tooltip = new ManagedTooltip(icon);
                    tooltip.setTitle("Play a beep sound through the browser when this feedback is shown");

                    presentationWidgets.add(icon);
                }

                if (playFlash) {
                    Icon icon = new Icon(IconType.SUN_O);
                    icon.setSize(IconSize.LARGE);
                    icon.getElement().getStyle().setProperty("margin", "0px 3px");

                    Tooltip tooltip = new ManagedTooltip(icon);
                    tooltip.setTitle("Flash a yellow background behind the feedback text when this feedback is shown");

                    presentationWidgets.add(icon);
                }
            }

            if (message.getDelivery().getToObserverController() != null) {
                Icon icon = new Icon(IconType.ID_CARD_O);
                icon.setSize(IconSize.LARGE);
                icon.getElement().getStyle().setProperty("margin", "0px 3px");

                Tooltip tooltip = new ManagedTooltip(icon);
                if (CollectionUtils.isNotEmpty(teamRefs)) {
                    tooltip.setTitle("Also send this feedback to the observer controller");
                } else {
                    tooltip.setTitle("Send this feedback to the observer controller only");
                }

                presentationWidgets.add(icon);
            }
            
        }

        if (presentationWidgets.isEmpty()) {
            // no message presentation is the same as having in tutor as text and have the character
            // say the feedback
            // during course execution. Therefore show those icons by default.

            // Show text
            addShowTextWidgets(presentationWidgets);

            // character say feedback
            addCharacterSayFeedbackWidgets(presentationWidgets);
        }

        return presentationWidgets;
    }

    /**
     * Add the widgets that indicate the character will speak the feedback.
     * 
     * @param presentationWidgets the list where to add any necessary widgets
     */
    private static void addCharacterSayFeedbackWidgets(List<Widget> presentationWidgets) {

        Icon icon = new Icon(IconType.USER_O);
        icon.setSize(IconSize.LARGE);
        icon.getElement().getStyle().setProperty("margin", "0px 3px");

        Tooltip tooltip = new ManagedTooltip(icon);
        tooltip.setTitle("Have the character speak the feedback message");

        presentationWidgets.add(icon);
    }

    /**
     * Add the widgets that indicate the text will appear in the tutor.
     * 
     * @param presentationWidgets the list where to add any necessary widgets
     */
    private static void addShowTextWidgets(List<Widget> presentationWidgets) {

        Icon icon = new Icon(IconType.FILE_TEXT);
        icon.setSize(IconSize.LARGE);
        icon.getElement().getStyle().setProperty("margin", "0px 3px");

        Tooltip tooltip = new ManagedTooltip(icon);
        tooltip.setTitle("Display the feedback as text in the tutor");

        presentationWidgets.add(icon);

    }

    /**
     * Creates a summary for an {@link Audio}.
     *
     * @param audio The {@link Audio} to summarize.
     * @param teamRefs The list of {@link TeamRef} to send the message to
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    private static void summarize(Audio audio, List<TeamRef> teamRefs, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(audio, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();

        /* Print summary of mp3 file */
        String mp3 = audio.getMP3File();
        boolean hasMp3 = StringUtils.isNotBlank(mp3);
        if (hasMp3) {
            SafeHtml mp3Html = bold(mp3);
            htmlBuilder.append(mp3Html);
        }

        /* Print summary of ogg file */
        String ogg = audio.getOGGFile();
        boolean hasOgg = StringUtils.isNotBlank(ogg);
        if (hasOgg) {
            if (hasMp3) {
                htmlBuilder.appendEscapedLines(" / ");
            }

            SafeHtml oggHtml = bold(ogg);
            htmlBuilder.append(oggHtml);
        }

        InlineHTML fileHtml = new InlineHTML(htmlBuilder.toSafeHtml());
        fileHtml.getElement().getStyle().setMarginRight(4, Unit.PX);
        flowPanel.add(fileHtml);
        
        if (audio.getToObserverController() != null) {
            Icon icon = new Icon(IconType.ID_CARD_O);
            icon.setSize(IconSize.LARGE);
            icon.getElement().getStyle().setProperty("margin", "0px 3px");

            Tooltip tooltip = new ManagedTooltip(icon);
            if (CollectionUtils.isNotEmpty(teamRefs)) {
                tooltip.setTitle("Also send this feedback to the observer controller");
            } else {
                tooltip.setTitle("Send this feedback to the observer controller only");
            }

            flowPanel.add(icon);
        }
    }

    /**
     * Creates a summary for a {@link MediaSemantics}.
     *
     * @param mediaSemantics The {@link MediaSemantics} to summarize.
     * @param trainingApplicationType the type of training application that contains this media
     *        semantics.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    private static void summarize(MediaSemantics mediaSemantics, TrainingApplicationEnum trainingApplicationType,
            FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(mediaSemantics, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        summarize(mediaSemantics.getMessage(), null, trainingApplicationType, flowPanel);
    }

    /**
     * Creates a summary for a {@link Feedback.File}.
     *
     * @param feedbackFile The {@link Feedback.File} to summarize.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    private static void summarize(Feedback.File feedbackFile, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(feedbackFile, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        flowPanel.add(new InlineHTML(feedbackFile.getHTML()));
    }

    /**
     * Creates a summary for a {@link MidLessonMedia}.
     *
     * @param midLessonMedia The {@link MidLessonMedia} to summarize.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    public static void summarize(MidLessonMedia midLessonMedia, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(midLessonMedia, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        if (midLessonMedia.getLessonMaterialList() != null) {
            for (Media media : midLessonMedia.getLessonMaterialList().getMedia()) {
                Widget typeWidget = null;
                String tooltipTextPrefix = null;

                Serializable mediaType = media.getMediaTypeProperties();
                if (mediaType instanceof PDFProperties) {
                    typeWidget = new Icon(IconType.FILE_PDF_O);
                    tooltipTextPrefix = "PDF File";
                } else if (mediaType instanceof WebpageProperties) {
                    if (isWebAddress(media)) {
                        typeWidget = new Icon(IconType.GLOBE);
                        tooltipTextPrefix = "Web Address";
                    } else {
                        typeWidget = new Icon(IconType.FILE);
                        tooltipTextPrefix = "Local Webpage";
                    }
                } else if (mediaType instanceof YoutubeVideoProperties) {
                    typeWidget = new Icon(IconType.YOUTUBE_PLAY);
                } else if (mediaType instanceof VideoProperties) {
                    typeWidget = new Icon(IconType.FILE_VIDEO_O);
                    tooltipTextPrefix = "Local Video";
                } else if (mediaType instanceof ImageProperties) {
                    typeWidget = new Icon(IconType.IMAGE);
                    tooltipTextPrefix = "Image";
                } else if (mediaType instanceof SlideShowProperties) {
                    typeWidget = new Image("images/slideshow_icon.png");
                    tooltipTextPrefix = "Slide Show";
                } else {
                    typeWidget = new Icon(IconType.QUESTION);
                    tooltipTextPrefix = "Unknown";
                }

                if (typeWidget instanceof Icon) {
                    Icon icon = (Icon) typeWidget;
                    icon.setSize(IconSize.LARGE);
                }

                typeWidget.getElement().getStyle().setDisplay(Display.INLINE);
                typeWidget.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
                typeWidget.getElement().getStyle().setMarginRight(4, Unit.PX);

                /* If the media has a name, include it in the tooltip */
                StringBuilder tooltipText = new StringBuilder(tooltipTextPrefix);
                if (StringUtils.isNotBlank(media.getName())) {
                    tooltipText.append(": ").append(media.getName());
                }

                Tooltip tooltip = new ManagedTooltip(typeWidget, tooltipText.toString());
                flowPanel.add(tooltip);
            }
        }
    }

    /**
     * Checks whether the given media object is a web address
     * 
     * @param media the media object to check
     * @return whether the given media object is a web address
     */
    private static boolean isWebAddress(Media media) {
        return (media.getUri() != null && (media.getUri().contains("://") || media.getUri().contains("www.")));
    }

    /**
     * Creates a summary for a {@link PerformanceAssessment}.
     *
     * @param perfAssess The {@link PerformanceAssessment} to summarize.
     * @param nodeIdToNameMap The complete map of task and concept node ids to their respective
     *        names.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    public static void summarize(PerformanceAssessment perfAssess, Map<BigInteger, String> nodeIdToNameMap,
            FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(perfAssess, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        if (perfAssess.getAssessmentType() instanceof Conversation) {
            Conversation conversation = (Conversation) perfAssess.getAssessmentType();
            summarize(conversation, flowPanel);
        } else if (perfAssess.getAssessmentType() instanceof PerformanceAssessment.PerformanceNode) {
            PerformanceAssessment.PerformanceNode perfNode = (PerformanceAssessment.PerformanceNode) perfAssess
                    .getAssessmentType();
            summarize(perfNode, nodeIdToNameMap, flowPanel);
        }
    }

    /**
     * Creates a summary for a {@link Conversation}.
     *
     * @param conversation The {@link Conversation} to summarize.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    private static void summarize(Conversation conversation, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(conversation, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        Serializable type = conversation.getType();
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
        if (type instanceof ConversationTreeFile) {
            ConversationTreeFile conversationTree = (ConversationTreeFile) type;
            if (StringUtils.isNotBlank(conversationTree.getName())) {
                htmlBuilder.appendEscaped("[Conversation Tree] ").append(bold(conversationTree.getName()));
            } else {
                htmlBuilder.append(bold(color("Undefined Conversation Tree", "red")));
            }
        } else if (type instanceof AutoTutorSKO) {
            AutoTutorSKO autoTutorSko = (AutoTutorSKO) type;
            Serializable script = autoTutorSko.getScript();
            if (script instanceof ATRemoteSKO) {
                ATRemoteSKO atRemoteSko = (ATRemoteSKO) script;

                /* Get the location of the AutoTutor conversation as HTML */
                SafeHtml urlHtml;
                if (atRemoteSko.getURL() != null && atRemoteSko.getURL().getAddress() != null) {
                    urlHtml = bold(atRemoteSko.getURL().getAddress());
                } else {
                    urlHtml = color(bold("Unspecified Location"), "red");
                }
                htmlBuilder.appendEscaped("[AutoTutor] ").append(urlHtml);
            } else {
                htmlBuilder.appendEscaped("Start an AutoTutor conversation");
            }
        } else {
            htmlBuilder.appendHtmlConstant("Start an ").append(bold(color("Unknown", "red")))
                    .appendEscaped(" type of conversation");
        }

        flowPanel.add(new InlineHTML(htmlBuilder.toSafeHtml()));
    }

    /**
     * Creates a summary for a {@link PerformanceAssessment.PerformanceNode}.
     *
     * @param perfNode The {@link PerformanceAssessment.PerformanceNode} to summarize.
     * @param nodeIdToNameMap The complete map of task and concept node ids to their respective
     *        names.
     * @param flowPanel The {@link FlowPanel} to which to append the summary.
     */
    private static void summarize(PerformanceAssessment.PerformanceNode perfNode,
            Map<BigInteger, String> nodeIdToNameMap, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(perfNode, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        /* Append the name or warning text */
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder().appendEscaped("Survey for ");
        if (nodeIdToNameMap.containsKey(perfNode.getNodeId())) {
            htmlBuilder.append(bold(nodeIdToNameMap.get(perfNode.getNodeId())));
        } else {
            htmlBuilder.append(bold(color("Unspecified Task or Concept", "red")));
        }

        flowPanel.add(new InlineHTML(htmlBuilder.toSafeHtml()));
    }

    /**
     * Creates a summary for a {@link ScenarioAdaptation}.
     *
     * @param scenarioAdaptation The {@link ScenarioAdaptation} to summarize.
     * @param flowPanel The {@link StringBuilder} to which to append the summary.
     */
    public static void summarize(ScenarioAdaptation scenarioAdaptation, FlowPanel flowPanel) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(scenarioAdaptation, flowPanel);
            logger.fine("summarize(" + StringUtils.join(", ", params) + ")");
        }

        final String unspecifiedText = "No adaptation specified";
        final SafeHtml paddedDivStart = SafeHtmlUtils
                .fromTrustedString("<div style='margin: 0px 4px; display: inline;'>");
        final SafeHtml paddedDivEnd = SafeHtmlUtils.fromTrustedString("</div>");

        /* If no environment adaptation was authored, indicate that in the summary and return */
        EnvironmentAdaptation envAdapt = scenarioAdaptation.getEnvironmentAdaptation();
        if (envAdapt == null) {
            flowPanel.add(new HTML(unspecifiedText));
            return;
        }

        /* If no type-value pair was provided, indicate that in the summary and return */
        Serializable envAdaptType = envAdapt.getType();
        if (envAdaptType == null) {
            flowPanel.add(new HTML(unspecifiedText));
            return;
        }
        
        InlineHTML inlineHtml = new InlineHTML();
        inlineHtml.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        inlineHtml.getElement().getStyle().setMarginRight(8, Unit.PX);

        flowPanel.add(inlineHtml);

        final SafeHtml unspecifiedHtml = color(bold("Unspecified"), "red");
        Icon transitionIcon = null;
        String transitionIconTooltip = "Apply this change over ";
        SafeHtmlBuilder html = new SafeHtmlBuilder();

        if(StringUtils.isNotBlank(scenarioAdaptation.getDescription())){
            html.appendEscaped(scenarioAdaptation.getDescription());
        }else{
            if (envAdaptType instanceof FatigueRecovery) {
                FatigueRecovery fatigue = (FatigueRecovery) envAdaptType;
    
                html.appendEscaped("Fatigue Recovery Rate: ");
                if (fatigue.getRate() != null) {
                    html.append(fatigue.getRate().doubleValue());
                } else {
                    html.append(unspecifiedHtml);
                }
                
                if (fatigue.getTeamMemberRef() != null && StringUtils.isNotBlank(fatigue.getTeamMemberRef().getValue())) {
    
                    Badge badge = new Badge("1");
    
                    Icon icon = new Icon(IconType.USER);
                    icon.getElement().getStyle().setFontSize(16, Unit.PX);
                    icon.setPaddingLeft(5);
    
                    badge.add(icon);
    
                    Tooltip tooltip = new ManagedTooltip(badge);
                    tooltip.setTitle("Adjust fatigue recovery for " + fatigue.getTeamMemberRef().getValue());
    
                    flowPanel.add(badge);
                }
                
            } else if (envAdaptType instanceof Fog) {
                Fog fog = (Fog) envAdaptType;
                Color color = fog.getColor();
    
                html.appendEscaped("Fog: ");
                if (fog.getDensity() != null) {
                    html.append(fog.getDensity().doubleValue() * 100).appendEscaped("%");
                } else {
                    html.append(unspecifiedHtml);
                }
    
                if (color != null) {
                    FlowPanel colorSwatch = new FlowPanel();
                    Style style = colorSwatch.getElement().getStyle();
                    style.setBackgroundColor(
                            "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")");
                    style.setDisplay(Display.INLINE_BLOCK);
                    style.setPropertyPx("borderRadius", 8);
                    style.setMarginLeft(8, Unit.PX);
                    style.setWidth(20, Unit.PX);
                    style.setHeight(20, Unit.PX);
                    style.setVerticalAlign(VerticalAlign.MIDDLE);
                    style.setBorderStyle(BorderStyle.SOLID);
                    style.setBorderWidth(1, Unit.PX);
                    style.setBorderColor("#000000");
                    html.appendHtmlConstant(colorSwatch.toString());
                }
    
                BigInteger fogTransitionTime = fog.getScenarioAdaptationDuration();
                if (fogTransitionTime != null && fogTransitionTime.intValue() > 0) {
                    transitionIcon = new Icon(IconType.CLOCK_O);
                    transitionIconTooltip += FormattedTimeBox.getDisplayText(fogTransitionTime.intValue());
                }
            } else if (envAdaptType instanceof Rain) {
                Rain rain = (Rain) envAdaptType;
    
                html.appendEscaped("Rain: ");
                if (rain.getValue() != null) {
                    html.append(rain.getValue().doubleValue() * 100).appendEscaped("%");
                } else {
                    html.append(unspecifiedHtml);
                }
    
                BigInteger rainTransitionTime = rain.getScenarioAdaptationDuration();
                if (rainTransitionTime != null && rainTransitionTime.intValue() > 0) {
                    transitionIcon = new Icon(IconType.CLOCK_O);
                    transitionIconTooltip += FormattedTimeBox.getDisplayText(rainTransitionTime.intValue());
                }
            } else if (envAdaptType instanceof Overcast) {
                Overcast overcast = (Overcast) envAdaptType;
    
                html.appendEscaped("Overcast: ");
                if (overcast.getValue() != null) {
                    html.append(overcast.getValue().doubleValue() * 100).appendEscaped("%");
                } else {
                    html.append(unspecifiedHtml);
                }
    
                BigInteger overcastTransitionTime = overcast.getScenarioAdaptationDuration();
                if (overcastTransitionTime != null && overcastTransitionTime.intValue() > 0) {
                    transitionIcon = new Icon(IconType.CLOCK_O);
                    transitionIconTooltip += FormattedTimeBox.getDisplayText(overcastTransitionTime.intValue());
                }
            } else if (envAdaptType instanceof Teleport) {
                Teleport teleport = (Teleport) envAdaptType;
    
                if (teleport.getCoordinate() != null) {
                    summarize(teleport.getCoordinate(), html);
                } else {
                    html.appendEscaped("Coordinate: ").append(unspecifiedHtml);
                }
    
                if (teleport.getHeading() != null) {
                    html.append(paddedDivStart).appendEscaped("Heading: ").append(teleport.getHeading().getValue())
                            .appendEscaped(" degrees").append(paddedDivEnd);
                }
                
                if (teleport.getTeamMemberRef() != null && StringUtils.isNotBlank(teleport.getTeamMemberRef().getValue())) {
    
                    Badge badge = new Badge("1");
    
                    Icon icon = new Icon(IconType.USER);
                    icon.getElement().getStyle().setFontSize(16, Unit.PX);
                    icon.setPaddingLeft(5);
    
                    badge.add(icon);
    
                    Tooltip tooltip = new ManagedTooltip(badge);
                    tooltip.setTitle("Teleport " + teleport.getTeamMemberRef().getValue() + " to this location");
    
                    flowPanel.add(badge);
                }
                
            } else if (envAdaptType instanceof CreateActors) {
                CreateActors createActors = (CreateActors) envAdaptType;
    
                if (createActors.getSide() != null) {
                    html.append(paddedDivStart);
    
                    String color;
                    String borderColor;
                    String text;
    
                    if (createActors.getSide().getType() instanceof Opfor) {
                        color = "#c9302c";
                        borderColor = "#ac2925";
                        text = "Enemy";
                    } else if (createActors.getSide().getType() instanceof Blufor) {
                        color = "#204d74";
                        borderColor = "#122b40";
                        text = "Friendly";
                    } else if (createActors.getSide().getType() instanceof Civilian) {
                        color = "#449d44";
                        borderColor = "398439";
                        text = "Civilian";
                    } else {
                        color = "#000000";
                        borderColor = "#000000";
                        text = "Unaffiliated";
                    }
    
                    SafeHtml factionText = SafeHtmlUtils
                            .fromSafeConstant("<span style='backgroundColor: " + color + "; borderColor: " + borderColor
                                    + "; padding: 4px; borderRadius: 4px; color: white;'>" + text + "</span>");
    
                    html.append(factionText);
                    html.append(paddedDivEnd);
                }
    
                html.append(paddedDivStart);
                if(StringUtils.isNotBlank(createActors.getActorName())){
                    // show optional actor name instead of actor type because actor type might not be the most human friendly display string
                    // e.g. gft_soldiers_inf_team_leader_m4aim
                    html.appendEscaped(createActors.getActorName());
                }else {
                    if (createActors.getType() != null) {
                        html.appendEscaped(createActors.getType());
                    } else {
                        html.appendEscaped("Actors: ").append(unspecifiedHtml);
                    }
                }
                html.append(paddedDivEnd);
    
                html.append(paddedDivStart);
                if (createActors.getCoordinate() != null) {
                    summarize(createActors.getCoordinate(), html);
                } else {
                    html.appendEscaped("Coordinate: ").append(unspecifiedHtml);
                }
                
                if (createActors.getHeading() != null) {
                    html.append(paddedDivStart).appendEscaped("Heading: ").append(createActors.getHeading().getValue())
                            .appendEscaped(" degrees").append(paddedDivEnd);
                }
                
                html.append(paddedDivEnd);
            } else if (envAdaptType instanceof RemoveActors) {
                RemoveActors removeActors = (RemoveActors) envAdaptType;
    
                if (removeActors.getType() instanceof String) {
                    String actorName = (String) removeActors.getType();
                    html.appendEscaped("Remove ").appendEscaped(actorName);
                } else {
                    html.append(unspecifiedHtml);
                }
            } else if (envAdaptType instanceof Script) {
                Script script = (Script) envAdaptType;
    
                if (script.getValue() != null) {
                    html.appendEscaped("Custom Script");
                } else {
                    html.append(unspecifiedHtml);
                }
            } else if (envAdaptType instanceof TimeOfDay) {
                TimeOfDay timeOfDay = (TimeOfDay) envAdaptType;
    
                html.appendEscaped("Time of Day: ");
                if (timeOfDay.getType() instanceof TimeOfDay.Dawn) {
                    html.appendEscaped("Dawn");
                } else if (timeOfDay.getType() instanceof TimeOfDay.Midday) {
                    html.appendEscaped("Midday");
                } else if (timeOfDay.getType() instanceof TimeOfDay.Dusk) {
                    html.appendEscaped("Dusk");
                } else if (timeOfDay.getType() instanceof TimeOfDay.Midnight) {
                    html.appendEscaped("Midnight");
                } else {
                    html.append(unspecifiedHtml);
                }
            } else if (envAdaptType instanceof Endurance) {
                Endurance endurance = (Endurance) envAdaptType;
    
                html.appendEscaped("Endurance: ");
                if (endurance.getValue() != null) {
                    html.append(endurance.getValue().doubleValue() * 100).appendEscaped("%");
                } else {
                    html.append(unspecifiedHtml);
                }
                
                if (endurance.getTeamMemberRef() != null && StringUtils.isNotBlank(endurance.getTeamMemberRef().getValue())) {
    
                    Badge badge = new Badge("1");
    
                    Icon icon = new Icon(IconType.USER);
                    icon.getElement().getStyle().setFontSize(16, Unit.PX);
                    icon.setPaddingLeft(5);
    
                    badge.add(icon);
    
                    Tooltip tooltip = new ManagedTooltip(badge);
                    tooltip.setTitle("Adjust endurance for " + endurance.getTeamMemberRef().getValue());
    
                    flowPanel.add(badge);
                }
            } else if(envAdaptType instanceof CreateBreadcrumbs){
                CreateBreadcrumbs createBreadcrumbs = (CreateBreadcrumbs)envAdaptType;
                
                if (createBreadcrumbs.getTeamMemberRef() != null) {
                    
                    Badge badge = new Badge(String.valueOf(createBreadcrumbs.getTeamMemberRef().size()));
    
                    Icon icon = new Icon(IconType.USER);
                    icon.getElement().getStyle().setFontSize(16, Unit.PX);
                    icon.setPaddingLeft(5);
    
                    badge.add(icon);
    
                    Tooltip tooltip = new ManagedTooltip(badge);
                    tooltip.setTitle("Show bread crumbs to " + createBreadcrumbs.getTeamMemberRef().size() + " team organization members");
    
                    flowPanel.add(badge);
                }
                
                LocationInfo locationInfo = createBreadcrumbs.getLocationInfo();
                if(locationInfo != null && StringUtils.isNotBlank(locationInfo.getPlaceOfInterestRef())){
                    html.appendEscaped(" at ").appendEscaped(locationInfo.getPlaceOfInterestRef());
                }else{
                    html.appendEscaped(" at ").append(unspecifiedHtml);
                }
                
            } else if(envAdaptType instanceof RemoveBreadcrumbs){
                RemoveBreadcrumbs removeBreadcrumbs = (RemoveBreadcrumbs)envAdaptType;
                
                html.appendEscaped("Remove bread crumbs");
                
                if (removeBreadcrumbs.getTeamMemberRef() != null) {
                    
                    Badge badge = new Badge(String.valueOf(removeBreadcrumbs.getTeamMemberRef().size()));
    
                    Icon icon = new Icon(IconType.USER);
                    icon.getElement().getStyle().setFontSize(16, Unit.PX);
                    icon.setPaddingLeft(5);
    
                    badge.add(icon);
    
                    Tooltip tooltip = new ManagedTooltip(badge);
                    tooltip.setTitle("Remove bread crumbs for " + removeBreadcrumbs.getTeamMemberRef().size() + " team organization members");
    
                    flowPanel.add(badge);
                }
                
            } else if(envAdaptType instanceof HighlightObjects){
                HighlightObjects highlight = (HighlightObjects)envAdaptType;
                
                String name = highlight.getName();
                if(StringUtils.isNotBlank(name)){
                    html.appendEscaped("'").appendEscaped(name).appendEscaped("'");
                }
                
                Serializable type = highlight.getType();
                if(type instanceof HighlightObjects.TeamMemberRef){
                    HighlightObjects.TeamMemberRef member = (HighlightObjects.TeamMemberRef)type;
                    if(StringUtils.isNotBlank(member.getValue())){
                        html.appendEscaped(" on ").appendEscaped(member.getValue());
                    }else if(StringUtils.isNotBlank(member.getEntityMarking())){
                        html.appendEscaped(" at ").appendEscaped(member.getEntityMarking());
                    }
                }else if(type instanceof HighlightObjects.LocationInfo){
                    HighlightObjects.LocationInfo locationInfo = (HighlightObjects.LocationInfo)type;
                    html.appendEscaped(" at ");
                    if(StringUtils.isNotBlank(locationInfo.getPlaceOfInterestRef())){
                        html.appendEscaped("'").appendEscaped(locationInfo.getPlaceOfInterestRef()).appendEscaped("'");
                    }else if(locationInfo.getCoordinate() != null){
                        summarize(locationInfo.getCoordinate(), html);
                    }else{
                        html.appendEscaped(unspecifiedText);
                    }
                }
                
                HighlightObjects.Offset offset = highlight.getOffset();
                SafeHtmlBuilder offsetBuilder = null;
                if(offset.getFront() != null && offset.getFront().doubleValue() != 0){
                    offsetBuilder = new SafeHtmlBuilder();
                    offsetBuilder.appendEscaped(" Front: ").appendEscaped(offset.getFront().toPlainString());
                }
                
                if(offset.getRight() != null && offset.getRight().doubleValue() != 0){
                    if(offsetBuilder == null){
                        offsetBuilder = new SafeHtmlBuilder();
                    }
                    offsetBuilder.appendEscaped(" Right: ").appendEscaped(offset.getRight().toPlainString());
                }
                
                if(offset.getUp() != null && offset.getUp().doubleValue() != 0){
                    if(offsetBuilder == null){
                        offsetBuilder = new SafeHtmlBuilder();
                    }
                    offsetBuilder.appendEscaped(" Up: ").appendEscaped(offset.getUp().toPlainString());
                }   
                
                if(offsetBuilder != null){
                    
                    Icon offsetIcon = new Icon();
                    offsetIcon.setType(IconType.ARROWS);
                    offsetIcon.setSize(IconSize.LARGE);
                    offsetIcon.setMarginRight(4);
                    offsetIcon.setMarginLeft(4);
                    
                    Tooltip tooltip = new ManagedTooltip(offsetIcon);
                    tooltip.setTitle("Higlight Object Offset");
                    
                    html.append(SafeHtmlUtils.fromTrustedString(tooltip.toString())).append(offsetBuilder.toSafeHtml());
                }
                
                HighlightObjects.Color color = highlight.getColor();
                if (color != null) {
                    FlowPanel colorSwatch = new FlowPanel();
                    Style style = colorSwatch.getElement().getStyle();
                    String colorStr = "red";
                    if(color.getType() instanceof HighlightObjects.Color.Red){
                        colorStr = "red";
                    }else if(color.getType() instanceof HighlightObjects.Color.Blue){
                        colorStr = "blue";
                    }else if(color.getType() instanceof HighlightObjects.Color.Green){
                        colorStr = "green";
                    }
                    style.setBackgroundColor(colorStr);
                    style.setDisplay(Display.INLINE_BLOCK);
                    style.setPropertyPx("borderRadius", 8);
                    style.setMarginLeft(8, Unit.PX);
                    style.setWidth(20, Unit.PX);
                    style.setHeight(20, Unit.PX);
                    style.setVerticalAlign(VerticalAlign.MIDDLE);
                    style.setBorderStyle(BorderStyle.SOLID);
                    style.setBorderWidth(1, Unit.PX);
                    style.setBorderColor("#000000");
                    html.appendHtmlConstant(colorSwatch.toString());
                }
                
                
            }else if(envAdaptType instanceof RemoveHighlightOnObjects){
                RemoveHighlightOnObjects removeHighlight = (RemoveHighlightOnObjects)envAdaptType;
                
                String highlightName = removeHighlight.getHighlightName();
                if(StringUtils.isBlank(highlightName)){
                    html.append(unspecifiedHtml);
                }else{
                    html.appendEscaped("Remove '").appendEscaped(highlightName).appendEscaped("'");
                }
            }
        }
        
        inlineHtml.setHTML(html.toSafeHtml());
        
        if (transitionIcon != null) {
            transitionIcon.setSize(IconSize.LARGE);
            transitionIcon.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
            flowPanel.add(new ManagedTooltip(transitionIcon, transitionIconTooltip));
        }
    }

    /**
     * Summarizes a given {@link Coordinate} and appends it to a {@link SafeHtmlBuilder}.
     *
     * @param coordinate The {@link Coordinate} which to summarize. Can't be null.
     * @param html The {@link SafeHtmlBuilder} to which to append the summary. Can't be null.
     */
    private static void summarize(Coordinate coordinate, SafeHtmlBuilder html) {
        if (coordinate == null) {
            throw new IllegalArgumentException("The parameter 'coordinate' cannot be null.");
        }

        if (html == null) {
            throw new IllegalArgumentException("The parameter 'html' cannot be null.");
        }

        String coordinateTypeName;
        BigDecimal firstValue, secondValue, thirdValue;

        Serializable coordinateType = coordinate.getType();
        if (coordinateType instanceof GCC) {
            GCC gcc = (GCC) coordinateType;
            coordinateTypeName = "GCC";
            firstValue = gcc.getX();
            secondValue = gcc.getY();
            thirdValue = gcc.getZ();
        } else if (coordinateType instanceof GDC) {
            GDC gdc = (GDC) coordinateType;
            coordinateTypeName = "GDC";
            firstValue = gdc.getLongitude();
            secondValue = gdc.getLatitude();
            thirdValue = gdc.getElevation();
        } else if (coordinateType instanceof AGL) {
            AGL agl = (AGL) coordinateType;
            coordinateTypeName = "VBS AGL";
            firstValue = agl.getX();
            secondValue = agl.getY();
            thirdValue = agl.getElevation();
        } else {
            String coordinateClassName = coordinateType != null ? coordinateType.getClass().getName() : "null";
            throw new UnsupportedOperationException(
                    "The coordinate type '" + coordinateClassName + "' cannot be summarized.");
        }

        CoordinateType coordinateTypeEnum = CoordinateType.getCoordinateTypeFromCoordinate(coordinateType);

        Icon coordinateIcon = new Icon();
        coordinateIcon.setType(coordinateTypeEnum.getIconType());
        coordinateIcon.setSize(IconSize.LARGE);
        coordinateIcon.setMarginRight(4);

        SafeHtml firstHtml = coordinateTypeEnum.buildXLabel(Float.valueOf(firstValue.floatValue()));
        SafeHtml secondHtml = coordinateTypeEnum.buildYLabel(Float.valueOf(secondValue.floatValue()));
        SafeHtml thirdHtml = coordinateTypeEnum.buildZLabel(Float.valueOf(thirdValue.floatValue()));
        BubbleLabel firstLabel = new BubbleLabel(firstHtml);
        BubbleLabel secondLabel = new BubbleLabel(secondHtml);
        BubbleLabel thirdLabel = new BubbleLabel(thirdHtml);

        html.append(SafeHtmlUtils.fromSafeConstant(coordinateIcon.toString()));
        html.appendEscaped(coordinateTypeName);
        html.append(SafeHtmlUtils.fromSafeConstant(firstLabel.toString()));
        html.append(SafeHtmlUtils.fromSafeConstant(secondLabel.toString()));
        html.append(SafeHtmlUtils.fromSafeConstant(thirdLabel.toString()));
    }
}
