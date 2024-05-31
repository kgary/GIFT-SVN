/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.StrategyStressCategory;
import generated.dkf.AGL;
import generated.dkf.Coordinate;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.Point;
import generated.dkf.EnvironmentAdaptation.CreateActors;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side.Civilian;
import generated.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor;
import generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs;
import generated.dkf.EnvironmentAdaptation.Endurance;
import generated.dkf.EnvironmentAdaptation.FatigueRecovery;
import generated.dkf.EnvironmentAdaptation.Fog;
import generated.dkf.EnvironmentAdaptation.Fog.Color;
import generated.dkf.EnvironmentAdaptation.HighlightObjects;
import generated.dkf.EnvironmentAdaptation.HighlightObjects.Offset;
import generated.dkf.EnvironmentAdaptation.Overcast;
import generated.dkf.EnvironmentAdaptation.Rain;
import generated.dkf.EnvironmentAdaptation.RemoveActors;
import generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs;
import generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects;
import generated.dkf.EnvironmentAdaptation.Script;
import generated.dkf.EnvironmentAdaptation.Teleport;
import generated.dkf.EnvironmentAdaptation.Teleport.Heading;
import generated.dkf.EnvironmentAdaptation.TimeOfDay;
import generated.dkf.EnvironmentAdaptation.TimeOfDay.Dawn;
import generated.dkf.EnvironmentAdaptation.TimeOfDay.Dusk;
import generated.dkf.EnvironmentAdaptation.TimeOfDay.Midday;
import generated.dkf.EnvironmentAdaptation.TimeOfDay.Midnight;
import generated.dkf.Path;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Segment;
import generated.dkf.Strategy;
import generated.dkf.TeamMember;
import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.ActorTypeCategoryEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.ColorBox;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An editor used to author scenario adaptations
 */
public class ScenarioAdaptationEditor extends ItemEditor<ScenarioAdaptation> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioAdaptationEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ScenarioAdaptationEditorUiBinder uiBinder = GWT.create(ScenarioAdaptationEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ScenarioAdaptationEditorUiBinder extends UiBinder<Widget, ScenarioAdaptationEditor> {
    }

    /**
     * The bootstrap style name to be applied to the button when it has been
     * toggled on
     */
    private static final String TOGGLE_ON_STYLE = "active";
    
    /**
     * the Select widget option for the placeholder highlight name reference choice
     */
    private static final String HIGHLIGHT_NAME_REF_PLACEHOLDER = "Select a highlight to remove";
    
    /**
     * text to use for the script help editor's help dialog.  HTML syntax supported.
     */
    private static final String SCRIPT_TEXT_HELP = "Scripts are used to execute custom operations in a training application.  Normally "+
                                "these commands are not already supported in the GIFT authoring tool. This script "+
                                "will be delivered to and applied in the training application environment when appropriate. Each "+
                                "training application has its own syntax for scripts.";

    /** The style that is defined in the ui.xml */
    protected interface UiStyle extends CssResource {
        /**
         * The style name for an environment adaptation tile within the
         * {@link ScenarioAdaptationEditor#adaptationTypeRibbon}.
         *
         * @return The name of the style for environment adaptations
         */
        String environmentAdaptation();

        /**
         * The style name for an actor adaptation tile within the
         * {@link ScenarioAdaptationEditor#adaptationTypeRibbon}.
         *
         * @return The name of the style for actor adaptations
         */
        String actorsAdaptation();

        /**
         * The style name for an player settings adaptation tile within the
         * {@link ScenarioAdaptationEditor#adaptationTypeRibbon}.
         *
         * @return The name of the style for player settings adaptations
         */
        String playerSettingsAdaptation();
    }

    /** The instance of the style that is defined within the ui.xml */
    @UiField
    protected UiStyle style;

    /** The panel that contains all other widgets */
    @UiField
    DeckPanel rootPanel;

    /** The ribbon used to choose the type of adaptation */
    @UiField
    Ribbon adaptationTypeRibbon;
    
    /** the ribbon for choosing the type of object to highlight */
    @UiField
    Ribbon highlightTypeRibbon;

    /** The panel that contains the subeditor icon and subeditor controls */
    @UiField
    FlowPanel subEditorPanel;

    /** The tooltip for the {@link #subEditorTypeIcon} */
    @UiField
    Tooltip subEditorTypeIconTooltip;

    /** The icon representing the type of subeditor */
    @UiField
    Icon subEditorTypeIcon;

    /** The panel that contains each of the individual subeditors */
    @UiField
    DeckPanel subEditorControlsPanel;
    
    @UiField
    HTML scriptHelp;

    /** The panel used to author {@link FatigueRecovery} */
    @UiField
    FlowPanel fatiguePanel;

    /** The spinner used to author {@link FatigueRecovery#getRate()} */
    @UiField(provided = true)
    DecimalNumberSpinner fatigueRateSpinner = new DecimalNumberSpinner(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.valueOf(1000), BigDecimal.valueOf(0.1));

    /** A picker used to pick the learner whose fatigue should be recovered  */
    @UiField(provided = true)
    protected TeamMemberPicker fatigueMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication(), true);
    
    /** The panel used to author {@link Fog} */
    @UiField
    FlowPanel fogPanel;

    /** The slider used to author {@link Fog#getDensity()} */
    @UiField
    Slider fogDensitySlider;    
    
    /** The button opting into the optional {@link Fog#getColor()} */
    @UiField
    Button fogColorToggle;

    /** The control used to author {@link Fog#getColor()} */
    @UiField
    ColorBox fogColorBox;

    /** The box used to author {@link Fog#getScenarioAdaptationDuration()} */
    @UiField
    FormattedTimeBox fogTransitionTimeBox;
    
    /** the box used to display the stress category for fog adaptation */
    @UiField
    Label fogStressCategoryLabel;

    /** The panel used to author {@link Rain} */
    @UiField
    FlowPanel rainPanel;

    /** The slider used to author {@link Rain#getValue()} */
    @UiField
    Slider rainSlider;

    /** The box used to author {@link Rain#getScenarioAdaptationDuration()} */
    @UiField
    FormattedTimeBox rainTransitionTimeBox;
    
    /** the box used to display the stress category for rain adaptation */
    @UiField
    Label rainStressCategoryLabel;

    /** The panel used to author {@link Overcast} */
    @UiField
    FlowPanel overcastPanel;

    /** The slider used to author {@link Overcast#getValue()} */
    @UiField
    Slider overcastSlider;

    /** The box used to author {@link Overcast#getScenarioAdaptationDuration()} */
    @UiField
    FormattedTimeBox overcastTransitionTimeBox;
    
    /** the box used to display the stress category for overcast adaptation */
    @UiField
    Label overcastStressCategoryLabel;

    /** The panel used to author {@link TeleportLearner} */
    @UiField
    FlowPanel teleportPanel;

    /** The editor used to author {@link TeleportLearner#getCoordinate()} */
    @UiField
    ScenarioCoordinateEditor teleportCoordinateEditor;

    /** The button opting into the optional {@link TeleportLearner#getHeading()} */
    @UiField
    Button teleportHeadingToggle;

    /** The slider authoring the value of {@link TeleportLearner#getHeading()} */
    @UiField
    Slider teleportHeadingSlider;
    
    /** A picker used to pick the learner that should be teleported */
    @UiField(provided = true)
    protected TeamMemberPicker teleportMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication(), true);

    /** The panel used to author {@link CreateActors} */
    @UiField
    FlowPanel createActorsPanel;

    /** The editor used to author {@link CreateActor#getCoordinate()} */
    @UiField
    ScenarioCoordinateEditor createActorCoordinateEditor;
    
    /** The button opting into the optional {@link CreateActor#getHeading()} */
    @UiField
    Button createActorHeadingToggle;

    /** The slider authoring the value of {@link CreateActor#getHeading()} */
    @UiField
    Slider createActorHeadingSlider;

    /** The textbox used to author {@link CreateActors#getType()} */
    @UiField
    TextBox createActorTypeBox;
    
    /** the textbox used to author {@link CreateActors#getActorName()} */
    @UiField
    TextBox createActorNameBox;
    
    /** used to select the actor type category {@link CreateActors#getTypeCategory()} */
    @UiField
    Select createActorTypeCategoryBox;

    /**
     * A button used to author a {@link Opfor} for
     * {@link CreateActors#getSide()}
     */
    @UiField
    Button createActorEnemyButton;

    /**
     * A button used to author a {@link Blufor} for
     * {@link CreateActors#getSide()}
     */
    @UiField
    Button createActorFriendlyButton;

    /**
     * A button used to author a {@link Civilian} for
     * {@link CreateActors#getSide()}
     */
    @UiField
    Button createActorCivilianButton;

    /** The panel used to author {@link RemoveActors} */
    @UiField
    FlowPanel removeActorsPanel;
    
    /** the textbox used to author {@link RemoveActors#getActorName()} */
    @UiField
    TextBox removeActorNameBox;
    
    /** used to select the actor type category {@link RemoveActors#getTypeCategory()} */
    @UiField
    Select removeActorTypeCategoryBox;

    /** The panel used to author {@link Script} */
    @UiField
    FlowPanel scriptPanel;

    /** The text area used to author {@link Script#getValue()} */
    @UiField
    TextArea scriptTextArea;

    /** The panel used to author {@link TimeOfDay} */
    @UiField
    FlowPanel timePanel;

    /** A button used to author {@link TimeOfDay#getType()} */
    @UiField
    Button dawnTimeButton;

    /** A button used to author {@link TimeOfDay#getType()} */
    @UiField
    Button middayTimeButton;

    /** A button used to author {@link TimeOfDay#getType()} */
    @UiField
    Button duskTimeButton;

    /** A button used to author {@link TimeOfDay#getType()} */
    @UiField
    Button midnightTimeButton;
    
    /** the box used to display the stress category for time of day adaptation */
    @UiField
    Label todStressCategoryLabel;

    /** The panel used to author {@link Endurance} */
    @UiField
    FlowPanel endurancePanel;
    
    /** The panel used to author {@link HighlightObjects} */
    @UiField
    FlowPanel addHighlightObjectPanel;
    
    /** the panel used to author {@link RemoveHighlightOnObjects} */
    @UiField
    FlowPanel removeHighlightObjectPanel;

    /** The slider used to author {@link Endurance#getValue()} */
    @UiField
    Slider enduranceSlider;
    
    /** the name of the highlight for highlighting object */
    @UiField
    TextBox highlightNameTextbox;
    
    /** the red color button for highlight object */
    @UiField
    Button highlightRedButton;
    
    /** the green color button for highlight object */
    @UiField
    Button highlightGreenButton;
    
    /** the blue color button for highlight object */
    @UiField
    Button highlightBlueButton;
    
    /** for providing offset to the right/left in meters for highlight location */
    @UiField
    TextBox highlightOffsetRightTextbox;
    
    /** for providing offset to the up/down in meters for highlight location */
    @UiField
    TextBox highlightOffsetUpTextbox;
    
    /** for providing offset to the front/back in meters for highlight location */
    @UiField
    TextBox highlightOffsetFrontTextbox;
    
    /** contains the highlight object type ribbon and deck panel of editors */
    @UiField
    DeckPanel highlightTypeRootPanel;
    
    /** contains the highlight object type editors */
    @UiField
    DeckPanel highlightSubEditorDeckPanel;
    
    /** contains the editor for highlight object */
    @UiField
    FlowPanel highlightSubEditorPanel;
    
    /**
     * The panel surrounding the select component {@link #removeHighlightNameBox}. This is
     * required for validation because the select's getElement() is returning the actual
     * select component which is hidden by bootstrap. The validation's addStyle() gets automatically
     * applied to the surrounding div, but removeStyle() does not propogate up. Therefore, we are
     * just using this panel to show the validation styling for this multiple select component.
     */
    @UiField
    protected HTMLPanel removeHighlightNamePanel;
    
    /** containing the names of available highlight object names */
    @UiField
    protected Select removeHighlightNameBox;
    
    /** A picker used to pick the team member to highlight */
    @UiField(provided = true)
    protected TeamMemberPicker highlightMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication(), true);
    
    /** define the point to highlight */   
    @UiField(provided = true)
    protected PlaceOfInterestPicker highlightLocationPicker = new PlaceOfInterestPicker(Point.class);
    
    /** A picker used to pick the learner whose endurance should be changed */
    @UiField(provided = true)
    protected TeamMemberPicker enduranceMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication(), true);

    /** The button to show/hide the {@link #descriptionFlowPanel} */
    @UiField
    protected Button descriptionButton;

    /**
     * The panel containing the description label and
     * {@link #descriptionTextBox}
     */
    @UiField
    protected FlowPanel descriptionFlowPanel;

    /** The text box contianing the description of the adaptation */
    @UiField
    protected TextBox descriptionTextBox;
    
    /** panel containing the picker for the stress category */
    @UiField
    protected HorizontalPanel stressCategoryPickerPanel;
    
    /** the button for selecting the environmental type stress category */
    @UiField
    protected Button environmentalStressCategoryButton;
    
    /** the button for selecting the cognitive type stress category */
    @UiField
    protected Button cognitiveStressCategoryButton;
    
    /** the button for selecting the physiological type stress category */
    @UiField
    protected Button physiologicalStressCategoryButton;
    
    /** The panel containing the duration field for fog adaptation */
    @UiField
    protected Widget fogDurationPanel;
    
    /** The panel containing the duration field for rain adaptation */
    @UiField
    protected Widget rainDurationPanel;
    
    /** The panel containing the duration field for overcast adaptation */
    @UiField
    protected Widget overcastDurationPanel;
    
    /** The panel containing VR-Engage-specific help for time of day adaptation */
    @UiField
    protected Widget todVrEngageHelp;
    
    /** the panel to author creating bread crumbs */
    @UiField
    protected FlowPanel createBreadcrumbsPanel;
    
    /** the panel to author removing bread crumbs */
    @UiField
    protected FlowPanel removeBreadcrumbsPanel;
    
    /** the picker for the point or path place of interest to create the bread crumb(s) at */
    @UiField(provided = true)
    protected PlaceOfInterestPicker breadcrumbLocationPicker = new PlaceOfInterestPicker(Point.class, Path.class);
    
    /** who will see the created bread crumb(s) */
    @UiField(provided = true)
    protected EditableTeamPicker createBreadcrumbsTeamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());
    
    /** who needs bread crumbs removed from their view */
    @UiField(provided = true)
    protected EditableTeamPicker removeBreadcrumbsTeamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /** The icon type for the {@link Fatigue} subeditor */
    public static final IconType FATIGUE_ICON_TYPE = StrategyActivityUtil.SA_FATIGUE_ICON_TYPE;

    /** The icon type for the {@link Fog} subeditor */
    public static final IconType FOG_ICON_TYPE = StrategyActivityUtil.SA_FOG_ICON_TYPE;

    /** The icon type for the {@link Rain} subeditor */
    public static final IconType RAIN_ICON_TYPE = StrategyActivityUtil.SA_RAIN_ICON_TYPE;

    /** The icon type for the {@link Overcast} subeditor */
    public static final IconType OVERCAST_ICON_TYPE = StrategyActivityUtil.SA_OVERCAST_ICON_TYPE;

    /** The icon type for the {@link TeleportLearner} subeditor */
    public static final IconType TELEPORT_LEARNER_ICON_TYPE = StrategyActivityUtil.SA_TELEPORT_LEARNER_ICON_TYPE;

    /** The icon type for the {@link CreateActors} subeditor */
    public static final IconType CREATE_ACTORS_ICON_TYPE = StrategyActivityUtil.SA_CREATE_ACTORS_ICON_TYPE;

    /** The icon type for the {@link RemoveActors} subeditor */
    public static final IconType REMOVE_ACTORS_ICON_TYPE = StrategyActivityUtil.SA_REMOVE_ACTORS_ICON_TYPE;

    /** The icon type for the {@link Script} subeditor */
    public static final IconType SCRIPT_ICON_TYPE = StrategyActivityUtil.SA_SCRIPT_ICON_TYPE;

    /** The icon type for the {@link TimeOfDay} subeditor */
    public static final IconType TIME_OF_DAY_ICON_TYPE = StrategyActivityUtil.SA_TIME_OF_DAY_ICON_TYPE;

    /** The icon type for the {@link Endurance} subeditor */
    public static final IconType ENDURANCE_ICON_TYPE = StrategyActivityUtil.SA_ENDURANCE_ICON_TYPE;
    
    /** The icon type for the {@link HighlightObjects} subeditor */
    public static final IconType HIGHLIGHT_ICON_TYPE = StrategyActivityUtil.SA_HIGHLIGHT_ICON_TYPE;
    
    /** The icon type for the {@link RemoveHighlightOnObjects} subeditor */
    public static final IconType REMOVE_HIGHLIGHT_ICON_TYPE = StrategyActivityUtil.SA_REMOVE_HIGHLIGHT_ICON_TYPE;
    
    /** The icon type for the {@link CreateBreadcrumbs} subeditor */
    public static final IconType CREATE_BREADCRUMBS_ICON_TYPE = StrategyActivityUtil.SA_CREATE_BREADCRUMBS_ICON_TYPE;
    
    /** The icon type for the {@link RemoveBreadcrumbs} subeditor */
    public static final IconType REMOVE_BREADCRUMBS_ICON_TYPE = StrategyActivityUtil.SA_REMOVE_BREADCRUMBS_ICON_TYPE;
    
    /** The icon type for the {@link HighlightObjects.TeamMemberRef} subeditor */
    public static final IconType HIGHLIGHT_TEAM_MEMBER_ICON_TYPE = StrategyActivityUtil.SA_HIGHLIGHT_TEAM_MEMBER_ICON_TYPE;
    
    /** The icon type for the {@link Coordinate} subeditor for highlight objects */
    public static final IconType HIGHLIGHT_LOCATION_ICON_TYPE = StrategyActivityUtil.SA_HIGHLIGHT_LOCATION_ICON_TYPE;

    /** The icon tooltip for the {@link Fatigue} subeditor */
    public static final String FATIGUE_ICON_TOOLTIP = StrategyActivityUtil.SA_FATIGUE_ICON_TOOLTIP;

    /** The icon tooltip for the {@link Fog} subeditor */
    public static final String FOG_ICON_TOOLTIP = StrategyActivityUtil.SA_FOG_ICON_TOOLTIP;

    /** The icon tooltip for the {@link Rain} subeditor */
    public static final String RAIN_ICON_TOOLTIP = StrategyActivityUtil.SA_RAIN_ICON_TOOLTIP;

    /** The icon tooltip for the {@link Overcast} subeditor */
    public static final String OVERCAST_ICON_TOOLTIP = StrategyActivityUtil.SA_OVERCAST_ICON_TOOLTIP;

    /** The icon tooltip for the {@link TeleportLearner} subeditor */
    public static final String TELEPORT_LEARNER_ICON_TOOLTIP = StrategyActivityUtil.SA_TELEPORT_LEARNER_ICON_TOOLTIP;

    /** The icon tooltip for the {@link CreateActors} subeditor */
    public static final String CREATE_ACTORS_ICON_TOOLTIP = StrategyActivityUtil.SA_CREATE_ACTORS_ICON_TOOLTIP;

    /** The icon tooltip for the {@link RemoveActors} subeditor */
    public static final String REMOVE_ACTORS_ICON_TOOLTIP = StrategyActivityUtil.SA_REMOVE_ACTORS_ICON_TOOLTIP;

    /** The icon tooltip for the {@link Script} subeditor */
    public static final String SCRIPT_ICON_TOOLTIP = StrategyActivityUtil.SA_SCRIPT_ICON_TOOLTIP;

    /** The icon tooltip for the {@link TimeOfDay} subeditor */
    public static final String TIME_OF_DAY_ICON_TOOLTIP = StrategyActivityUtil.SA_TIME_OF_DAY_ICON_TOOLTIP;

    /** The icon tooltip for the {@link Endurance} subeditor */
    public static final String ENDURANCE_ICON_TOOLTIP = StrategyActivityUtil.SA_ENDURANCE_ICON_TOOLTIP;

    /** The icon tooltip for the {@link HighlightObjects} subeditor */
    public static final String HIGHLIGHT_ICON_TOOLTIP = StrategyActivityUtil.SA_HIGHLIGHT_ICON_TOOLTIP;
    
    /** The icon tooltip for the {@link RemoveHighlightOnObjects} subeditor */
    public static final String REMOVE_HIGHLIGHT_ICON_TOOLTIP = StrategyActivityUtil.SA_REMOVE_HIGHLIGHT_ICON_TOOLTIP;
    
    /**  The icon tooltip for the {@link HighlightObjects.TeamMemberRef} subeditor*/
    public static final String HIGHLIGHT_TEAM_MEMBER_ICON_TOOLTIP = StrategyActivityUtil.SA_HIGHLIGHT_TEAM_MEMBER_ICON_TOOLTIP;
    
    /**  The icon tooltip for the {@link Coordinate} subeditor for highlight objects */
    public static final String HIGHLIGHT_LOCATION_ICON_TOOLTIP = StrategyActivityUtil.SA_HIGHLIGHT_LOCATION_ICON_TOOLTIP;
    
    /**  The icon tooltip for the {@link CreateBreadcrumbs} subeditor for highlight objects */
    public static final String CREATE_BREADCRUMBS_ICON_TOOLTIP = StrategyActivityUtil.SA_CREATE_BREADCRUMBS_ICON_TOOLTIP;
    
    /**  The icon tooltip for the {@link RemoveBreadcrumbs} subeditor for highlight objects */
    public static final String REMOVE_BREADCRUMBS_ICON_TOOLTIP = StrategyActivityUtil.SA_REMOVE_BREADCRUMBS_ICON_TOOLTIP;

    /** The validation for the type of actor being created */
    private final WidgetValidationStatus createActorsTypeValidation;
    
    /** The validation for the name of actor being created */
    private final WidgetValidationStatus createActorsNameValidation;
    
    /** The validation for the name of actor being removed */
    private final WidgetValidationStatus removeActorsNameValidation;

    /** The validation for the content of a script */
    private final WidgetValidationStatus scriptValueValidation;
    
    /** The validation for the time of day adaptation panel */
    private final WidgetValidationStatus timeOfDayValidation;
    
    /** the validation for the highlight name widget */
    private final WidgetValidationStatus highlightNameValidation;
    
    /** validate that a highlight type has been chosen */
    private final WidgetValidationStatus highlightTypeChosenValidation;
    
    /** the validation for the highlight offset front widget */
    private final WidgetValidationStatus highlightOffsetFrontNumberValidation;
    
    /** the validation for the highlight offset up widget */
    private final WidgetValidationStatus highlightOffsetUpNumberValidation;
    
    /** the validation for the highlight offset right widget */
    private final WidgetValidationStatus highlightOffsetRightNumberValidation;
    
    /** the validation for the remove highlight widget */
    private final WidgetValidationStatus removeHighlightNameValidation;
    
    /** validate that the highlight location uses AGL coordinates */
    private final WidgetValidationStatus highlightAGLLocationValidation;
    
    /** validate that the bread crumb location uses AGL coordinates */
    private final WidgetValidationStatus breadcrumbAGLLocationValidation;
    
    /** the current data model for what this widget is building */
    private ScenarioAdaptation scenarioAdaptation;

    /**
     * Instantiates a new scenario adaptation editor.
     */
    public ScenarioAdaptationEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ScenarioAdaptationEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        // Static stress categories
        fogStressCategoryLabel.setText(EnvironmentAdaptation.Fog.STRESS_CATEGORY.name());
        rainStressCategoryLabel.setText(EnvironmentAdaptation.Rain.STRESS_CATEGORY.name());
        overcastStressCategoryLabel.setText(EnvironmentAdaptation.Overcast.STRESS_CATEGORY.name());
        todStressCategoryLabel.setText(EnvironmentAdaptation.TimeOfDay.STRESS_CATEGORY.name());
        
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
        htmlBuilder.appendEscaped(SCRIPT_TEXT_HELP);
        if(StringUtils.isNotBlank(GatClientUtility.getServerProperties().getVersionName())){
            TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
            if(taType == TrainingApplicationEnum.VBS){
                htmlBuilder.appendHtmlConstant("<br/><br/>");
                htmlBuilder.appendEscaped("Refer to ");
                htmlBuilder.appendHtmlConstant("<a href=\"https://gifttutoring.org/projects/gift/wiki/GIFT_VBS_Plugin_"+
                        GatClientUtility.getServerProperties().getVersionName()+"#Scripting\" target=\"_blank\">VBS Scripting</a>");
                htmlBuilder.appendEscaped(" for more information");
            }else if(taType == TrainingApplicationEnum.HAVEN){
                htmlBuilder.appendHtmlConstant("<br/><br/>");
                htmlBuilder.appendEscaped("Refer to ");
                htmlBuilder.appendHtmlConstant("<a href=\"https://gifttutoring.org/projects/gift/wiki/GIFT_HAVEN_Plugin_"+
                        GatClientUtility.getServerProperties().getVersionName()+"#Scripting\" target=\"_blank\">SE Sandbox Scripting</a>");
                htmlBuilder.appendEscaped(" for more information");
            } else if(taType == TrainingApplicationEnum.RIDE){
                htmlBuilder.appendHtmlConstant("<br/><br/>");
                htmlBuilder.appendEscaped("Refer to ");
                htmlBuilder.appendHtmlConstant("<a href=\"https://gifttutoring.org/projects/gift/wiki/GIFT_RIDE_Plugin_"+
                        GatClientUtility.getServerProperties().getVersionName()+"#Scripting\" target=\"_blank\">RIDE Scripting</a>");
                htmlBuilder.appendEscaped(" for more information");
            }
        }
        scriptHelp.setHTML(htmlBuilder.toSafeHtml());

        initSATypeRibbon();
        initHighlightTypeRibbon();
        showSATypeRibbon();

        teleportCoordinateEditor.setCoordinate(new Coordinate());
        createActorCoordinateEditor.setCoordinate(new Coordinate());
        
        for(ActorTypeCategoryEnum catEnum : ActorTypeCategoryEnum.values()) {            
            Option createActor = new Option();
            createActor.setText(catEnum.name());
            createActor.setValue(catEnum.name());
            createActorTypeCategoryBox.add(createActor);
            
            Option removeActor = new Option();
            removeActor.setText(catEnum.name());
            removeActor.setValue(catEnum.name());
            removeActorTypeCategoryBox.add(removeActor);
        }
        
        // highlight name must only contain letters
        highlightNameTextbox.addDomHandler(new InputHandler() {
            
            //the last valid value entered into the text box
            String lastValue = null;

            @Override
            public void onInput(InputEvent event) {
                
                final String value = highlightNameTextbox.getText();
                
                if (value != null && !value.isEmpty()) {
                    if(StringUtils.isAlphaNumeric(value)){
                        // good value (so far)
                        lastValue = value;
                    }else{
                        // bad value, revert to previous value
                        highlightNameTextbox.setText(lastValue);
                    }
                }else{
                    // for when the value is cleared out
                    lastValue = value;
                }                
                
            }
            
        }, InputEvent.getType());
        
        highlightNameTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                // send change event so remove highlight scenario adaptations will be updated appropriately
                
                if(scenarioAdaptation != null){
                    EnvironmentAdaptation envAdapt = scenarioAdaptation.getEnvironmentAdaptation();
                    
                    if(envAdapt.getType() instanceof HighlightObjects){
                        

                        HighlightObjects highlight = (HighlightObjects)envAdapt.getType();
                        String oldRef = highlight.getName();
                        ScenarioEventUtility.fireRenameEvent(highlight, oldRef, event.getValue());                    
                    }

                }
                
                requestValidation(highlightNameValidation);
            }
        });
        
        highlightOffsetFrontTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(highlightOffsetFrontNumberValidation);
            }
        });
        
        highlightOffsetUpTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(highlightOffsetUpNumberValidation);
            }
        });
        
        highlightOffsetRightTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(highlightOffsetRightNumberValidation);
            }
        });
        
        breadcrumbLocationPicker.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                requestValidation(breadcrumbAGLLocationValidation);
            }
            
        });
        
        highlightLocationPicker.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                requestValidation(highlightAGLLocationValidation);
            }
            
        });
        
        removeHighlightNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                requestValidation(removeHighlightNameValidation);
            }
        });
        
        /* Default the description panel to hidden */
        descriptionFlowPanel.getElement().getStyle().setVisibility(Visibility.HIDDEN);

        teleportMemberPicker.setActive(false);
        fatigueMemberPicker.setActive(false);
        enduranceMemberPicker.setActive(false);
        highlightMemberPicker.setActive(false);
        highlightLocationPicker.setActive(false);
        createBreadcrumbsTeamPicker.setActive(false);
        removeBreadcrumbsTeamPicker.setActive(false);
        breadcrumbLocationPicker.setActive(false);
        
        //only show duration fields if the training app supports duration
        boolean isAdaptationDurationSupported = !TrainingApplicationEnum.VR_ENGAGE.equals(ScenarioClientUtility.getTrainingAppType());
        fogDurationPanel.setVisible(isAdaptationDurationSupported);
        rainDurationPanel.setVisible(isAdaptationDurationSupported);
        overcastDurationPanel.setVisible(isAdaptationDurationSupported);
        
        //only show VR-Engage specific help when authoring for VR-Engage
        todVrEngageHelp.setVisible(TrainingApplicationEnum.VR_ENGAGE.equals(ScenarioClientUtility.getTrainingAppType()));

        createActorsTypeValidation = new WidgetValidationStatus(createActorTypeBox, "The type for an actor can't be blank");
        createActorsNameValidation = new WidgetValidationStatus(createActorNameBox, "The name of an actor can only have Alphanumeric values");
        removeActorsNameValidation = new WidgetValidationStatus(removeActorNameBox, "The name of an actor must be provided and can only have Alphanumeric values");
        scriptValueValidation = new WidgetValidationStatus(scriptTextArea, "The content of a script can't be blank");
        timeOfDayValidation = new WidgetValidationStatus(timePanel, "The time of day must be selected");
        highlightNameValidation = new WidgetValidationStatus(highlightNameTextbox, "The highlight name can't be blank and must contain letters only.");
        highlightTypeChosenValidation = new WidgetValidationStatus(highlightTypeRibbon, "Choose the type of object to highlight");
        highlightOffsetFrontNumberValidation = new WidgetValidationStatus(highlightOffsetFrontTextbox, "The highlight offset 'Front' must be a number.");
        highlightOffsetUpNumberValidation = new WidgetValidationStatus(highlightOffsetUpTextbox, "The highlight offset 'Up' must be a number.");
        highlightOffsetRightNumberValidation = new WidgetValidationStatus(highlightOffsetRightTextbox, "The highlight offset 'Right' must be a number.");
        removeHighlightNameValidation = new WidgetValidationStatus(removeHighlightNamePanel, "A valid highlight name must be specified for the remove highlight action.");
        highlightAGLLocationValidation = new WidgetValidationStatus(highlightLocationPicker, "Must use the AGL coordinate type for this training application.  Either use a different place of interest or change the coordinate type.");
        breadcrumbAGLLocationValidation = new WidgetValidationStatus(breadcrumbLocationPicker, "Must use the AGL coordinate type for this training application.  Either use a different place of interest or change the coordinate type.");
    }
    
    /**
     * Populate the highlight type ribbon with choices.
     */
    private void initHighlightTypeRibbon(){
        String tooltip = null;
        
        Widget teamMember = highlightTypeRibbon.addRibbonItem(HIGHLIGHT_TEAM_MEMBER_ICON_TYPE, HIGHLIGHT_TEAM_MEMBER_ICON_TOOLTIP, tooltip, new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {

                // show the highlight type editor panel
                highlightTypeRootPanel.showWidget(highlightTypeRootPanel.getWidgetIndex(highlightSubEditorPanel));
                
                // show the editor for highlight team member
                highlightSubEditorDeckPanel.showWidget(highlightSubEditorDeckPanel.getWidgetIndex(highlightMemberPicker));
                
                // show a validation error if a team member has not been picked yet
                highlightMemberPicker.setActive(true);
                if(scenarioAdaptation != null){
                    highlightMemberPicker.validateAllAndFireDirtyEvent(scenarioAdaptation);                   
                }else{                    
                    highlightMemberPicker.validateAll();
                }
                
                // remove the validation error of having not picked a highlight type
                requestValidation(highlightTypeChosenValidation);
            }
        });
        teamMember.addStyleName(style.actorsAdaptation());
        
        Widget location = highlightTypeRibbon.addRibbonItem(HIGHLIGHT_LOCATION_ICON_TYPE, HIGHLIGHT_LOCATION_ICON_TOOLTIP, tooltip, new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                // show the highlight type editor panel
                highlightTypeRootPanel.showWidget(highlightTypeRootPanel.getWidgetIndex(highlightSubEditorPanel));
                
                // show the editor for highlight location
                highlightSubEditorDeckPanel.showWidget(highlightSubEditorDeckPanel.getWidgetIndex(highlightLocationPicker));
                
                // remove the validation error of having not picked a highlight type
                // add validation error to make sure a place of interest with AGL coordinate is selected
                requestValidation(highlightTypeChosenValidation, highlightAGLLocationValidation);
            }
        });
        location.addStyleName(style.actorsAdaptation());
    }

    /**
     * Initializes each of the choices within the {@link #adaptationTypeRibbon}.
     */
    private void initSATypeRibbon() {
        final TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
        if(taType == null) {
            return;
        }
        
        ScenarioClientUtility.getTrainingAppScenarioAdaptations(new AsyncCallback<Map<TrainingApplicationEnum,Set<String>>>() {
            
            @Override
            public void onSuccess(Map<TrainingApplicationEnum, Set<String>> result) {
                
                if(result == null) {
                    return;
                }

                Set<String> adaptations = result.get(taType);
                if(CollectionUtils.isEmpty(adaptations)) {
                    return;
                }
                
        String tooltip = null;
        //
        // create/remove objects from the environment
        //
                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.CreateActors.class.getCanonicalName())) {
            Widget createActors = adaptationTypeRibbon.addRibbonItem(CREATE_ACTORS_ICON_TYPE, CREATE_ACTORS_ICON_TOOLTIP, tooltip, makeWidgetShower(new CreateActors()));
            createActors.addStyleName(style.actorsAdaptation());
        }

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.RemoveActors.class.getCanonicalName())) {
            Widget removeActors = adaptationTypeRibbon.addRibbonItem(REMOVE_ACTORS_ICON_TYPE, REMOVE_ACTORS_ICON_TOOLTIP, tooltip, makeWidgetShower(new RemoveActors()));
            removeActors.addStyleName(style.actorsAdaptation());
        }

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.Teleport.class.getCanonicalName())) {
            Widget teleport = adaptationTypeRibbon.addRibbonItem(TELEPORT_LEARNER_ICON_TYPE, TELEPORT_LEARNER_ICON_TOOLTIP, tooltip, makeWidgetShower(new Teleport()));
            teleport.addStyleName(style.actorsAdaptation());
        }
        
                if(adaptations.contains(generated.dkf.EnvironmentAdaptation.HighlightObjects.class.getCanonicalName())){
            Widget highlight = adaptationTypeRibbon.addRibbonItem(HIGHLIGHT_ICON_TYPE, HIGHLIGHT_ICON_TOOLTIP, tooltip, makeWidgetShower(new HighlightObjects()));
            highlight.addStyleName(style.actorsAdaptation());
        }
        
                if(adaptations.contains(generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects.class.getCanonicalName())){
            Widget removeHighlight = adaptationTypeRibbon.addRibbonItem(REMOVE_HIGHLIGHT_ICON_TYPE, REMOVE_HIGHLIGHT_ICON_TOOLTIP, tooltip, makeWidgetShower(new RemoveHighlightOnObjects()));
            removeHighlight.addStyleName(style.actorsAdaptation());
        }
        
                if(adaptations.contains(generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.class.getCanonicalName())){
            Widget createBreadcrumbs = adaptationTypeRibbon.addRibbonItem(CREATE_BREADCRUMBS_ICON_TYPE, CREATE_BREADCRUMBS_ICON_TOOLTIP, tooltip, makeWidgetShower(new CreateBreadcrumbs()));
            createBreadcrumbs.addStyleName(style.actorsAdaptation());
        }
        
                if(adaptations.contains(generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.class.getCanonicalName())){
            Widget removeBreadcrumbs = adaptationTypeRibbon.addRibbonItem(REMOVE_BREADCRUMBS_ICON_TYPE, REMOVE_BREADCRUMBS_ICON_TOOLTIP, tooltip, makeWidgetShower(new RemoveBreadcrumbs()));
            removeBreadcrumbs.addStyleName(style.actorsAdaptation());
        }
        
        //
        // Environment changes (e.g. weather)
        //

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.Fog.class.getCanonicalName())) {
            Widget fog = adaptationTypeRibbon.addRibbonItem(FOG_ICON_TYPE, FOG_ICON_TOOLTIP, tooltip, makeWidgetShower(new Fog()));
            fog.addStyleName(style.environmentAdaptation());
        }

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.Overcast.class.getCanonicalName())) {
            Widget overcast = adaptationTypeRibbon.addRibbonItem(OVERCAST_ICON_TYPE, OVERCAST_ICON_TOOLTIP, tooltip, makeWidgetShower(new Overcast()));
            overcast.addStyleName(style.environmentAdaptation());
        }

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.Rain.class.getCanonicalName())) {
            Widget rain = adaptationTypeRibbon.addRibbonItem(RAIN_ICON_TYPE, RAIN_ICON_TOOLTIP, tooltip, makeWidgetShower(new Rain()));
            rain.addStyleName(style.environmentAdaptation());
        }
                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.TimeOfDay.class.getCanonicalName())) {
            Widget timeOfDay = adaptationTypeRibbon.addRibbonItem(TIME_OF_DAY_ICON_TYPE, TIME_OF_DAY_ICON_TOOLTIP, tooltip, makeWidgetShower(new TimeOfDay()));
            timeOfDay.addStyleName(style.environmentAdaptation());
        }
        
        //
        // Changes to a characters behavior
        //

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.Endurance.class.getCanonicalName())) {
            Widget endurance = adaptationTypeRibbon.addRibbonItem(ENDURANCE_ICON_TYPE, ENDURANCE_ICON_TOOLTIP, tooltip, makeWidgetShower(new Endurance()));
            endurance.addStyleName(style.playerSettingsAdaptation());
        }

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.FatigueRecovery.class.getCanonicalName())) {
            Widget fatigue = adaptationTypeRibbon.addRibbonItem(FATIGUE_ICON_TYPE, FATIGUE_ICON_TOOLTIP, tooltip, makeWidgetShower(new FatigueRecovery()));
            fatigue.addStyleName(style.playerSettingsAdaptation());
        }
        
        //
        // Misc.
        //

                if (adaptations.contains(generated.dkf.EnvironmentAdaptation.Script.class.getCanonicalName())) {
            adaptationTypeRibbon.addRibbonItem(SCRIPT_ICON_TYPE, SCRIPT_ICON_TOOLTIP, tooltip, makeWidgetShower(new Script()));
        }
            }

            @Override
            public void onFailure(Throwable thrown) {
                logger.log(Level.SEVERE,
                        "The server failed to retrieve the training application scenario adaptations.",
                        thrown);
    }
        });

    }

    /**
     * Handles enabling/disabling the {@link #teleportHeadingSlider} to author a
     * {@link Heading#getValue()}
     *
     * @param event The event containing information about the click.
     */
    @UiHandler("teleportHeadingToggle")
    protected void onTeleportHeaderButtonToggled(ClickEvent event) {
        teleportHeadingSlider.setVisible(!teleportHeadingSlider.isVisible());
    }
    
    /**
     * Handles enabling/disabling the {@link #createActorHeadingSlider} to author a
     * {@link Heading#getValue()}
     *
     * @param event The event containing information about the click.
     */
    @UiHandler("createActorHeadingToggle")
    protected void onCreateActorHeaderButtonToggled(ClickEvent event) {
        createActorHeadingSlider.setVisible(!createActorHeadingSlider.isVisible());
    }
    
    /**
     * Handles enabling/disabling the {@link #fogColorBox} to author a
     * {@link Fog#getColor()}
     *
     * @param event The event containing information about the click.
     */
    @UiHandler("fogColorToggle")
    protected void onFogColorButtonToggled(ClickEvent event) {
        fogColorBox.setVisible(!fogColorBox.isVisible());
    }

    /**
     * Handles validating the contents of the {@link #createActorTypeBox} when
     * its value changes.
     *
     * @param event The event containing details about the new value of
     *        {@link #createActorTypeBox}.
     */
    @UiHandler("createActorTypeBox")
    protected void onCreateActorTypeChanged(ValueChangeEvent<String> event) {
        requestValidation(createActorsTypeValidation);
    }
    
    /**
     * Handles validating the contents of the {@link #createActorNameBox} when
     * its value changes.
     *
     * @param event The event containing details about the new value of
     *        {@link #createActorNameBox}.
     */
    @UiHandler("createActorNameBox")
    protected void onCreateActorNameChanged(ValueChangeEvent<String> event) {
        requestValidation(createActorsNameValidation);
    }
    
    /**
     * Handles validating the contents of the {@link #removeeActorNameBox} when
     * its value changes.
     *
     * @param event The event containing details about the new value of
     *        {@link #removeActorNameBox}.
     */
    @UiHandler("removeActorNameBox")
    protected void onRemoveActorNameChanged(ValueChangeEvent<String> event) {
        requestValidation(removeActorsNameValidation);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.CreateActors#getSide()} to {@link Opfor}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("createActorEnemyButton")
    protected void onCreateActorsEnemyToggled(ClickEvent event) {
        createActorEnemyButton.setActive(true);
        createActorFriendlyButton.setActive(false);
        createActorCivilianButton.setActive(false);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.CreateActors#getSide()} to {@link Blufor}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("createActorFriendlyButton")
    protected void onCreateActorsFriendlyToggled(ClickEvent event) {
        createActorEnemyButton.setActive(false);
        createActorFriendlyButton.setActive(true);
        createActorCivilianButton.setActive(false);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.CreateActors#getSide()} to {@link Civilian}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("createActorCivilianButton")
    protected void onCreateActorsCivilianToggled(ClickEvent event) {
        createActorEnemyButton.setActive(false);
        createActorFriendlyButton.setActive(false);
        createActorCivilianButton.setActive(true);
    }
    
    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.HighlightObjects#getColor()} to {@link EnvironmentAdaptation.HighlightObjects.Color.Red}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("highlightRedButton")
    protected void onHighlightRedButtonToggled(ClickEvent event) {
        highlightRedButton.setActive(true);
        highlightGreenButton.setActive(false);
        highlightBlueButton.setActive(false);
    }
    
    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.HighlightObjects#getColor()} to {@link EnvironmentAdaptation.HighlightObjects.Color.Green}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("highlightGreenButton")
    protected void onHighlightGreenButtonToggled(ClickEvent event) {
        highlightRedButton.setActive(false);
        highlightGreenButton.setActive(true);
        highlightBlueButton.setActive(false);
    }
    
    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.HighlightObjects#getColor()} to {@link EnvironmentAdaptation.HighlightObjects.Color.Blue}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("highlightBlueButton")
    protected void onHighlightBlueButtonToggled(ClickEvent event) {
        highlightRedButton.setActive(false);
        highlightGreenButton.setActive(false);
        highlightBlueButton.setActive(true);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.TimeOfDay#getType()} to {@link Dawn}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("dawnTimeButton")
    protected void onTimeOfDayDawnToggled(ClickEvent event) {
        dawnTimeButton.setActive(true);
        middayTimeButton.setActive(false);
        duskTimeButton.setActive(false);
        midnightTimeButton.setActive(false);
        
        requestValidation(timeOfDayValidation);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.TimeOfDay#getType()} to {@link Midday}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("middayTimeButton")
    protected void onTimeOfDayMiddayToggled(ClickEvent event) {
        dawnTimeButton.setActive(false);
        middayTimeButton.setActive(true);
        duskTimeButton.setActive(false);
        midnightTimeButton.setActive(false);
        
        requestValidation(timeOfDayValidation);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.TimeOfDay#getType()} to {@link Dusk}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("duskTimeButton")
    protected void onTimeOfDayDuskToggled(ClickEvent event) {
        dawnTimeButton.setActive(false);
        middayTimeButton.setActive(false);
        duskTimeButton.setActive(true);
        midnightTimeButton.setActive(false);
        
        requestValidation(timeOfDayValidation);
    }

    /**
     * Sets the currently authored
     * {@link EnvironmentAdaptation.TimeOfDay#getType()} to {@link Midnight}.
     *
     * @param event The event containing information about the click
     */
    @UiHandler("midnightTimeButton")
    protected void onTimeOfDayMidnightToggled(ClickEvent event) {
        dawnTimeButton.setActive(false);
        middayTimeButton.setActive(false);
        duskTimeButton.setActive(false);
        midnightTimeButton.setActive(true);
        
        requestValidation(timeOfDayValidation);
    }
    
    /**
     * Handles the environmentalStressCategoryButton being clicked.
     * 
     * @param event the event contain information about the click
     */
    @UiHandler("environmentalStressCategoryButton")
    protected void onEnvironmentalStressCategoryButtonToggled(ClickEvent event) {
        
        // toggle this one
        environmentalStressCategoryButton.setActive(!environmentalStressCategoryButton.isActive());
        environmentalStressCategoryButton.setFocus(environmentalStressCategoryButton.isActive());
        
        cognitiveStressCategoryButton.setActive(false);
        physiologicalStressCategoryButton.setActive(false);
    }
    
    /**
     * Handles the cognitiveStressCategoryButton being clicked.
     * 
     * @param event the event contain information about the click
     */
    @UiHandler("cognitiveStressCategoryButton")
    protected void onCognitiveStressCategoryButtonToggled(ClickEvent event) {
        environmentalStressCategoryButton.setActive(false);
        
        // toggle this one
        cognitiveStressCategoryButton.setActive(!cognitiveStressCategoryButton.isActive());
        cognitiveStressCategoryButton.setFocus(cognitiveStressCategoryButton.isActive());
        
        physiologicalStressCategoryButton.setActive(false);
    }
    
    /**
     * Handles the physiologicalStressCategoryButton being clicked.
     * 
     * @param event the event contain information about the click
     */
    @UiHandler("physiologicalStressCategoryButton")
    protected void onPhysiologicalStressCategoryButtonToggled(ClickEvent event) {
        environmentalStressCategoryButton.setActive(false);
        cognitiveStressCategoryButton.setActive(false);
        
        // toggle this one
        physiologicalStressCategoryButton.setActive(!physiologicalStressCategoryButton.isActive());
        physiologicalStressCategoryButton.setFocus(physiologicalStressCategoryButton.isActive());
    }
    
    
    /**
     * Handles validating the contents of the {@link #scriptTextArea} when its
     * value changes.
     *
     * @param event The event containing details about the new value of
     *        {@link #scriptTextArea}.
     */
    @UiHandler("scriptTextArea")
    protected void onScriptValueChanged(ValueChangeEvent<String> event) {
        requestValidation(scriptValueValidation);
    }

    /**
     * The event handler that captures when the description button is toggled.
     *
     * @param event The event that specifies whether the button has been toggled
     *        on or off.
     */
    @UiHandler("descriptionButton")
    protected void onDescriptionButtonToggled(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onDescriptionButtonToggled()");
        }

        /* If visible, then hide; and vice-versa */
        toggleDescriptionPanel(!isDescriptionPanelVisible());
    }

    /**
     * Checks if the {@link #descriptionFlowPanel} is visible or hidden.
     * 
     * @return true if the panel is visible; false if it is hidden
     */
    private boolean isDescriptionPanelVisible() {
        final Style descriptionStyle = descriptionFlowPanel.getElement().getStyle();
        return StringUtils.equals(descriptionStyle.getVisibility(), Visibility.VISIBLE.getCssName());
    }

    /**
     * Shows or hides the description panel.
     * @param show true to show the panel; false to hide it.
     */
    private void toggleDescriptionPanel(boolean show) {
        final Style descriptionStyle = descriptionFlowPanel.getElement().getStyle();
        if (show) {
            descriptionStyle.setVisibility(Visibility.VISIBLE);
            descriptionButton.addStyleName(TOGGLE_ON_STYLE);
        } else {
            descriptionStyle.setVisibility(Visibility.HIDDEN);
            descriptionButton.removeStyleName(TOGGLE_ON_STYLE);
        }
    }

    /**
     * Creates a {@link ClickHandler} that makes the {@link #rootPanel} show the
     * given {@link Widget}.
     *
     * @param type The input with which the widget should be initialized with
     *        when it is shown.
     * @return A {@link ClickHandler} that when invoked, shows the given
     *         {@link Widget} within the {@link #rootPanel}.
     */
    private ClickHandler makeWidgetShower(final Serializable type) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                populateFromType(type);
            }
        };
    }

    /**
     * Shows the ribbon used to select the type of the
     * {@link ScenarioAdaptation}.
     */
    public void resetEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("resetEditor()");
        }

        showSATypeRibbon();

        /* Default the description panel to empty and hidden */
        descriptionTextBox.clear();
        toggleDescriptionPanel(false);
        
        stressCategoryPickerPanel.setVisible(false);
    }

    @Override
    public void populateEditor(ScenarioAdaptation scenarioAdaptation) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + scenarioAdaptation + ")");
        }
        
        this.scenarioAdaptation = scenarioAdaptation;

        showSATypeRibbon();

        EnvironmentAdaptation environmentAdaptation = scenarioAdaptation.getEnvironmentAdaptation();
        if (environmentAdaptation == null) {
            return;
        }

        /* Populate the editor based on the type of EnvironmentAdaptation */
        populateFromType(environmentAdaptation.getType());

        /* Populate the scenario adaptation description */
        descriptionTextBox.setText(scenarioAdaptation.getDescription());
        toggleDescriptionPanel(StringUtils.isNotBlank(scenarioAdaptation.getDescription()));
    }

    /**
     * Chooses and populates the subeditor based on the provided type.
     *
     * @param type The type to populate the editor with. Can't be null.
     */
    private void populateFromType(Serializable type) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateFromType(" + type + ")");
        }
        
        teleportMemberPicker.setActive(false);
        teleportMemberPicker.clearValidations();
        fatigueMemberPicker.setActive(false);
        fatigueMemberPicker.clearValidations();
        enduranceMemberPicker.setActive(false);
        enduranceMemberPicker.clearValidations();
        highlightMemberPicker.setActive(false);
        highlightMemberPicker.clearValidations();
        highlightLocationPicker.setActive(false);
        highlightLocationPicker.clearValidations();
        createBreadcrumbsTeamPicker.setActive(false);
        createBreadcrumbsTeamPicker.clearValidations();
        removeBreadcrumbsTeamPicker.setActive(false);
        removeBreadcrumbsTeamPicker.clearValidations();
        breadcrumbLocationPicker.setActive(false);
        breadcrumbLocationPicker.clearValidations();
        
        // make the type of adaptation determine if the stress category panel should be shown
        stressCategoryPickerPanel.setVisible(false);

        if (type instanceof FatigueRecovery) {
            FatigueRecovery fatigue = (FatigueRecovery) type;
            BigDecimal rate = fatigue.getRate();

            fatigueRateSpinner.setValue(rate != null ? rate : BigDecimal.valueOf(1.0));
            
            fatigueMemberPicker.setActive(true);
            fatigueMemberPicker.setValue(fatigue.getTeamMemberRef() != null 
                    ? fatigue.getTeamMemberRef().getValue() 
                    : null);
            
            StrategyStressCategory category = fatigue.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(fatiguePanel, FATIGUE_ICON_TYPE, FATIGUE_ICON_TOOLTIP);
        } else if (type instanceof Fog) {
            Fog fog = (Fog) type;
            BigDecimal density = fog.getDensity();
            Color color = fog.getColor();
            fogColorToggle.setActive(color != null);
            fogColorBox.setVisible(color != null);
            if(color != null){
                fogColorBox.setValue(color.getRed(), color.getGreen(), color.getBlue());
            }else{
                fogColorBox.setValue(255, 255, 255);
            }
            
            fogDensitySlider.setValue(density != null ? density.doubleValue() : 0.0);
            BigInteger transitionTime = fog.getScenarioAdaptationDuration();
            fogTransitionTimeBox.setValue(transitionTime != null ? transitionTime.intValue() : 0);
            showSubEditor(fogPanel, FOG_ICON_TYPE, FOG_ICON_TOOLTIP);
        } else if (type instanceof Rain) {
            Rain rain = (Rain) type;
            BigDecimal value = rain.getValue();
            BigInteger transitionTime = rain.getScenarioAdaptationDuration();

            rainSlider.setValue(value != null ? value.doubleValue() : 0.0);
            rainTransitionTimeBox.setValue(transitionTime != null ? transitionTime.intValue() : 0);
            showSubEditor(rainPanel, RAIN_ICON_TYPE, RAIN_ICON_TOOLTIP);
        } else if (type instanceof Overcast) {
            Overcast overcast = (Overcast) type;
            BigDecimal value = overcast.getValue();
            BigInteger transitionTime = overcast.getScenarioAdaptationDuration();

            overcastSlider.setValue(value != null ? value.doubleValue() : 0.0);
            overcastTransitionTimeBox.setValue(transitionTime != null ? transitionTime.intValue() : 0);
            showSubEditor(overcastPanel, OVERCAST_ICON_TYPE, OVERCAST_ICON_TOOLTIP);
        } else if (type instanceof Teleport) {
            Teleport teleport = (Teleport) type;
            Coordinate destination = teleport.getCoordinate();
            Heading heading = teleport.getHeading();

            if(destination == null){
                
                if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
                    //VBS allows GCC and AGL but VBS scripting for teleporting learner only supports AGL
                    teleportCoordinateEditor.setDisallowedTypes(CoordinateType.GCC);
                }
                
                teleportCoordinateEditor.setCoordinate(new Coordinate());
            }else{
                teleportCoordinateEditor.setCoordinate(destination);
            }
            teleportHeadingSlider.setValue((double) (heading != null ? heading.getValue() : 0));
            teleportHeadingSlider.setVisible(heading != null);
            teleportHeadingToggle.setActive(heading != null);
            
            teleportMemberPicker.setActive(true);
            teleportMemberPicker.setValue(teleport.getTeamMemberRef() != null 
                    ? teleport.getTeamMemberRef().getValue() 
                    : null);
            
            StrategyStressCategory category = teleport.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(teleportPanel, TELEPORT_LEARNER_ICON_TYPE, TELEPORT_LEARNER_ICON_TOOLTIP);
        } else if (type instanceof CreateActors) {
            CreateActors createActors = (CreateActors) type;
            Coordinate location = createActors.getCoordinate();
            String actorType = createActors.getType();
            Side side = createActors.getSide(); 
            String actorName = createActors.getActorName();
            generated.dkf.EnvironmentAdaptation.CreateActors.Heading heading = createActors.getHeading();
            
            if(location == null){
                
                if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
                    //VBS allows GCC and AGL but VBS scripting for creating actors only supports AGL
                    createActorCoordinateEditor.setDisallowedTypes(CoordinateType.GCC);
                }
                
                createActorCoordinateEditor.setCoordinate(new Coordinate());
            }else{
                createActorCoordinateEditor.setCoordinate(location);
            }

            createActorHeadingSlider.setValue((double) (heading != null ? heading.getValue() : 0));
            createActorHeadingSlider.setVisible(heading != null);
            createActorHeadingToggle.setActive(heading != null);

            createActorTypeBox.setValue(actorType);
            createActorNameBox.setValue(actorName);
            
            if(createActors.getTypeCategory() == null) {
                // default to person
                createActors.setTypeCategory(ActorTypeCategoryEnum.PERSON);
            }
            createActorTypeCategoryBox.setValue(createActors.getTypeCategory().name());            
            createActorTypeCategoryBox.render();
            createActorTypeCategoryBox.refresh();
            
            createActorEnemyButton.setActive(side == null || side.getType() == null || side.getType() instanceof Opfor);
            createActorFriendlyButton.setActive(side != null && side.getType() instanceof Blufor);
            createActorCivilianButton.setActive(side != null && side.getType() instanceof Civilian);
            
            StrategyStressCategory category = createActors.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);

            showSubEditor(createActorsPanel, CREATE_ACTORS_ICON_TYPE, CREATE_ACTORS_ICON_TOOLTIP);
        } else if (type instanceof RemoveActors) {
            RemoveActors removeActors = (RemoveActors) type;
            Serializable actorId = removeActors.getType();
            String actorName = null;
            if(actorId instanceof String) {
                actorName = (String)actorId;
            }
            
            removeActorNameBox.setValue(actorName);
            
            if(removeActors.getTypeCategory() == null) {
                // default to person
                removeActors.setTypeCategory(ActorTypeCategoryEnum.PERSON);
            }
            removeActorTypeCategoryBox.setValue(removeActors.getTypeCategory().name());            
            removeActorTypeCategoryBox.render();
            removeActorTypeCategoryBox.refresh();
            
            StrategyStressCategory category = removeActors.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);

            showSubEditor(removeActorsPanel, REMOVE_ACTORS_ICON_TYPE, REMOVE_ACTORS_ICON_TOOLTIP);
        } else if (type instanceof Script) {
            Script script = (Script) type;
            String textValue = script.getValue();

            scriptTextArea.setValue(textValue);
            
            StrategyStressCategory category = script.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(scriptPanel, SCRIPT_ICON_TYPE, SCRIPT_ICON_TOOLTIP);
        } else if (type instanceof TimeOfDay) {
            TimeOfDay timeOfDay = (TimeOfDay) type;
            Serializable timeOfDayType = timeOfDay.getType();

            dawnTimeButton.setActive(timeOfDayType instanceof Dawn);
            middayTimeButton.setActive(timeOfDayType instanceof Midday);
            duskTimeButton.setActive(timeOfDayType instanceof Dusk);
            midnightTimeButton.setActive(timeOfDayType instanceof Midnight);

            showSubEditor(timePanel, TIME_OF_DAY_ICON_TYPE, TIME_OF_DAY_ICON_TOOLTIP);
        } else if (type instanceof Endurance) {
            Endurance endurance = (Endurance) type;
            BigDecimal value = endurance.getValue();

            enduranceSlider.setValue(value != null ? value.doubleValue() : 0.0);
            
            enduranceMemberPicker.setActive(true);
            enduranceMemberPicker.setValue(endurance.getTeamMemberRef() != null 
                    ? endurance.getTeamMemberRef().getValue() 
                    : null);
            
            StrategyStressCategory category = endurance.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(endurancePanel, ENDURANCE_ICON_TYPE, ENDURANCE_ICON_TOOLTIP);
        } else if (type instanceof HighlightObjects){
            
            HighlightObjects highlightObjects = (HighlightObjects)type;
            
            // set color widget -
            // use red as default
            HighlightObjects.Color color = highlightObjects.getColor();
            highlightRedButton.setActive(color == null || color.getType() == null || color.getType() instanceof EnvironmentAdaptation.HighlightObjects.Color.Red);
            highlightGreenButton.setActive(color != null && color.getType() instanceof EnvironmentAdaptation.HighlightObjects.Color.Green);
            highlightBlueButton.setActive(color != null && color.getType() instanceof EnvironmentAdaptation.HighlightObjects.Color.Blue);
            
            // set the name of the object showing highlighting widget (this is not the object being highlighted)
            String name = highlightObjects.getName();
            highlightNameTextbox.setValue(name);

            // set the offset widgets
            Offset offset = highlightObjects.getOffset();
            highlightOffsetRightTextbox.setValue(offset != null && offset.getRight() != null ? offset.getRight().toPlainString() : "0");
            highlightOffsetUpTextbox.setValue(offset != null && offset.getUp() != null ? offset.getUp().toPlainString() : "0");
            highlightOffsetFrontTextbox.setValue(offset != null && offset.getFront() != null ? offset.getFront().toPlainString() : "0");
            
            // set the highlighted object type widget
            Serializable objectType = highlightObjects.getType();
            if(objectType == null){
                //show highlight type choice ribbon
                
                highlightTypeRootPanel.showWidget(highlightTypeRootPanel.getWidgetIndex(highlightTypeRibbon));
                
            }else if(objectType instanceof HighlightObjects.TeamMemberRef){
                
                // show the highlight type editor panel
                highlightTypeRootPanel.showWidget(highlightTypeRootPanel.getWidgetIndex(highlightSubEditorPanel));
                
                // show the editor for highlight team member
                highlightSubEditorDeckPanel.showWidget(highlightSubEditorDeckPanel.getWidgetIndex(highlightMemberPicker));
                
                HighlightObjects.TeamMemberRef teamMemberRef = (HighlightObjects.TeamMemberRef)objectType;
                highlightMemberPicker.setActive(true);
                highlightMemberPicker.setValue(teamMemberRef != null 
                        ? teamMemberRef.getValue() 
                        : null);
                
            }else if(objectType instanceof HighlightObjects.LocationInfo){
                
                // show the highlight type editor panel
                highlightTypeRootPanel.showWidget(highlightTypeRootPanel.getWidgetIndex(highlightSubEditorPanel));
                
                // show the editor for highlight location
                highlightSubEditorDeckPanel.showWidget(highlightSubEditorDeckPanel.getWidgetIndex(highlightLocationPicker));
                
                highlightLocationPicker.setActive(true);

                HighlightObjects.LocationInfo locationInfo = (HighlightObjects.LocationInfo)objectType;
                if(locationInfo != null){
                    highlightLocationPicker.setValue(locationInfo.getPlaceOfInterestRef());
                }
              
            }              
            
            // Do this no matter the type of highlight object
            if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
                //VBS allows GCC and AGL but VBS scripting for highlighting learner only supports AGL
                highlightLocationPicker.setDisallowedTypes(CoordinateType.GCC);
            }
            
            StrategyStressCategory category = highlightObjects.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(addHighlightObjectPanel, HIGHLIGHT_ICON_TYPE, HIGHLIGHT_ICON_TOOLTIP);
            
        } else if(type instanceof RemoveHighlightOnObjects){
            
            RemoveHighlightOnObjects remove = (RemoveHighlightOnObjects)type;
            
            // refresh highlight names
            updateHighlightNamesList();
            
            removeHighlightNameBox.setValue(remove.getHighlightName());
            
            removeHighlightNameBox.render();
            removeHighlightNameBox.refresh();
            
            StrategyStressCategory category = remove.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(removeHighlightObjectPanel, REMOVE_HIGHLIGHT_ICON_TYPE, REMOVE_HIGHLIGHT_ICON_TOOLTIP);
            
        } else if(type instanceof CreateBreadcrumbs){
            
            CreateBreadcrumbs createBreadcrumbs = (CreateBreadcrumbs)type;
            
            List<String> members = new ArrayList<>();
            for(generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef memberRef : createBreadcrumbs.getTeamMemberRef()){
                members.add(memberRef.getValue());
            }
            
            createBreadcrumbsTeamPicker.setActive(true);
            createBreadcrumbsTeamPicker.setValue(members);
                        
            breadcrumbLocationPicker.setActive(true);

            CreateBreadcrumbs.LocationInfo locationInfo = createBreadcrumbs.getLocationInfo();
            if(locationInfo != null){
                breadcrumbLocationPicker.setValue(locationInfo.getPlaceOfInterestRef());
            }
            
            if(ScenarioClientUtility.getTrainingAppType() == TrainingApplicationEnum.VBS){
                //VBS allows GCC and AGL but VBS scripting for bread crumbs only supports AGL
                breadcrumbLocationPicker.setDisallowedTypes(CoordinateType.GCC);
            }
            
            StrategyStressCategory category = createBreadcrumbs.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);

            showSubEditor(createBreadcrumbsPanel, CREATE_BREADCRUMBS_ICON_TYPE, CREATE_BREADCRUMBS_ICON_TOOLTIP);
            
        } else if(type instanceof RemoveBreadcrumbs){
            
            RemoveBreadcrumbs removeBreadcrumbs = (RemoveBreadcrumbs)type;
            
            List<String> members = new ArrayList<>();
            for(generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef memberRef : removeBreadcrumbs.getTeamMemberRef()){
                members.add(memberRef.getValue());
            }
            
            removeBreadcrumbsTeamPicker.setActive(true);
            removeBreadcrumbsTeamPicker.setValue(members);
            
            StrategyStressCategory category = removeBreadcrumbs.getStressCategory();                    
            stressCategoryPickerPanel.setVisible(true);
            environmentalStressCategoryButton.setActive(category == StrategyStressCategory.ENVIRONMENTAL);
            cognitiveStressCategoryButton.setActive(category == StrategyStressCategory.COGNITIVE);
            physiologicalStressCategoryButton.setActive(category == StrategyStressCategory.PHYSIOLOGICAL);
            
            showSubEditor(removeBreadcrumbsPanel, REMOVE_BREADCRUMBS_ICON_TYPE, REMOVE_BREADCRUMBS_ICON_TOOLTIP);
            
        } else {
            throw new UnsupportedOperationException("Unable to load EnvironmentAdaptation of type "
                    + type.getClass().getName());
        }
    }
    
    /**
     * Gets the list of available highlight names from scenario adaptations in the current data model.
     *
     * @return the collection of highlight names that are available. Cannot be null.
     */
    private List<String> getAvailableHighlightNames() {

        // not using a set in order to maintain the same ordered as authored
        List<String> availableHighlightNames = new ArrayList<>();

        InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();

        if (strategies != null) {
            for (Strategy strategy : strategies.getStrategy()) {
                
                for(Serializable activity : strategy.getStrategyActivities()){
                    
                    if(activity instanceof ScenarioAdaptation){
                        
                        ScenarioAdaptation scenarioAdapt = (ScenarioAdaptation)activity;
                        EnvironmentAdaptation environmentAdapt = scenarioAdapt.getEnvironmentAdaptation();
                        Serializable environmentAdaptType = environmentAdapt.getType();
                        if(environmentAdaptType instanceof HighlightObjects){
                            HighlightObjects highlight = (HighlightObjects)environmentAdaptType;
                            if(StringUtils.isNotBlank(highlight.getName()) && !availableHighlightNames.contains(highlight.getName())){
                                availableHighlightNames.add(highlight.getName());
                            }
                        }
                    }
                }

            }

        }

        return availableHighlightNames;
    }
    
    /**
     * Updates the available highlight names that the author can select from
     */
    public void updateHighlightNamesList() {
        updateHighlightNamesList(null, null);
    }    

    /**
     * Updates the available highlight names that the author can select from and, if necessary, replaces the old
     * name of a renamed highlight.
     *
     * @param oldName the old name to update.  Can be null if the list just needs to be updated.
     * @param newName the new value to update the name with. Can be null if the list just needs to be updated
     */
    public void updateHighlightNamesList(String oldName, String newName) {

        String selectedName = removeHighlightNameBox.getValue();

        List<String> highlightNames = getAvailableHighlightNames();        

        //
        // update the UI with the most current list of strategy names
        //
        removeHighlightNameBox.clear();
        Option placeholderOption = new Option();
        placeholderOption.setText(HIGHLIGHT_NAME_REF_PLACEHOLDER);
        placeholderOption.setValue(HIGHLIGHT_NAME_REF_PLACEHOLDER);
        placeholderOption.setSelected(true);
        placeholderOption.setEnabled(false);
        placeholderOption.setHidden(true);
        removeHighlightNameBox.add(placeholderOption);
        
        for (String highilightName : highlightNames) {
            Option option = new Option();
            option.setText(highilightName);
            option.setValue(highilightName);
            removeHighlightNameBox.add(option);
        }

        if(!highlightNames.isEmpty()) {         
            
            if (StringUtils.isNotBlank(oldName)) {
                
                if(oldName != null && oldName.equals(selectedName)) {
                    // the current selected name is being changed to a new name
                    selectedName = newName;
                }
                
                //if a rename is occurring, make sure to replace the old name with the new one in the list
                // (for each occurrence of the old name)
                ListIterator<String> itr = highlightNames.listIterator();
                while (itr.hasNext()) {
                    String highilightName = itr.next();
                    if (StringUtils.equals(oldName, highilightName)) {
                        itr.set(newName);
                    }
                }
            }

            if(selectedName != null && highlightNames.contains(selectedName)) {
                //if a name was already selected and still exists in the updated list, reselect it
                removeHighlightNameBox.setValue(selectedName);
                
                if(scenarioAdaptation != null){
                    EnvironmentAdaptation envAdapt = scenarioAdaptation.getEnvironmentAdaptation();
                    
                    if(envAdapt.getType() instanceof RemoveHighlightOnObjects){
                        RemoveHighlightOnObjects removeHighlight = (RemoveHighlightOnObjects)envAdapt.getType();
                        removeHighlight.setHighlightName(selectedName);
                    }                    
                }
            }else{
                removeHighlightNameBox.setValue(HIGHLIGHT_NAME_REF_PLACEHOLDER);
            }
        }
                
        removeHighlightNameBox.render();
        removeHighlightNameBox.refresh();

        requestValidation(removeHighlightNameValidation);
    }


    @Override
    public void applyEdits(ScenarioAdaptation scenarioAdaptation) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + scenarioAdaptation + ")");
        }
        
        StrategyStressCategory stressCategory = null;
        if(environmentalStressCategoryButton.isActive()) {
            stressCategory = StrategyStressCategory.ENVIRONMENTAL;
        }else if(physiologicalStressCategoryButton.isActive()) {
            stressCategory = StrategyStressCategory.PHYSIOLOGICAL;
        }else if(cognitiveStressCategoryButton.isActive()) {
            stressCategory = StrategyStressCategory.COGNITIVE;
        }

        Serializable type;
        if (isSubEditorShown(fatiguePanel)) {
            BigDecimal rate = fatigueRateSpinner.getValue();
            
            FatigueRecovery.TeamMemberRef memberRef = null;
            
            if(StringUtils.isNotBlank(fatigueMemberPicker.getValue())) {
                
                memberRef = new FatigueRecovery.TeamMemberRef();
                memberRef.setValue(fatigueMemberPicker.getValue());
                
                TeamMember member = ScenarioClientUtility.getTeamMemberWithName(memberRef.getValue());
                
                if(member != null && member.getLearnerId() != null && member.getLearnerId().getType() instanceof String) {
                    memberRef.setEntityMarking((String) member.getLearnerId().getType());
                }
            }

            FatigueRecovery fatigue = new FatigueRecovery();
            fatigue.setRate(rate);
            fatigue.setTeamMemberRef(memberRef);
            fatigue.setStressCategory(stressCategory);
            type = fatigue;
        } else if (isSubEditorShown(fogPanel)) {
            BigDecimal density = BigDecimal.valueOf(fogDensitySlider.getValue());
            BigInteger transition = BigInteger.valueOf(fogTransitionTimeBox.getValue());

            Fog fog = new Fog();
            fog.setDensity(density);
            
            Color color = null;
            if(fogColorToggle.isActive()){
                color = new Color();
                color.setRed(fogColorBox.getRedInt());
                color.setGreen(fogColorBox.getGreenInt());
                color.setBlue(fogColorBox.getBlueInt());
            }
            fog.setColor(color);
            fog.setScenarioAdaptationDuration(transition);
            type = fog;
        } else if (isSubEditorShown(rainPanel)) {
            BigDecimal value = BigDecimal.valueOf(rainSlider.getValue());
            BigInteger transition = BigInteger.valueOf(rainTransitionTimeBox.getValue());

            Rain rain = new Rain();
            rain.setValue(value);
            rain.setScenarioAdaptationDuration(transition);
            type = rain;
        } else if (isSubEditorShown(overcastPanel)) {
            BigDecimal value = BigDecimal.valueOf(overcastSlider.getValue());
            BigInteger transition = BigInteger.valueOf(overcastTransitionTimeBox.getValue());

            Overcast overcast = new Overcast();
            overcast.setValue(value);
            overcast.setScenarioAdaptationDuration(transition);
            type = overcast;
        } else if (isSubEditorShown(teleportPanel)) {
            teleportCoordinateEditor.updateCoordinate();
            Coordinate location = teleportCoordinateEditor.getCoordinateCopy();
            Heading heading = null;
            if (teleportHeadingToggle.isActive()) {
                Double headingValue = teleportHeadingSlider.getValue();
                heading = new Heading();
                heading.setValue(headingValue != null ? headingValue.intValue() : 0);
            }
            
            Teleport.TeamMemberRef memberRef = null;
            
            if(StringUtils.isNotBlank(teleportMemberPicker.getValue())) {
                
                memberRef = new Teleport.TeamMemberRef();
                memberRef.setValue(teleportMemberPicker.getValue());
                
                TeamMember member = ScenarioClientUtility.getTeamMemberWithName(memberRef.getValue());
                
                if(member != null && member.getLearnerId() != null && member.getLearnerId().getType() instanceof String) {
                    memberRef.setEntityMarking((String) member.getLearnerId().getType());
                }
            }

            Teleport teleport = new Teleport();
            teleport.setCoordinate(location);
            teleport.setHeading(heading);
            teleport.setTeamMemberRef(memberRef);
            teleport.setStressCategory(stressCategory);
            type = teleport;
        } else if (isSubEditorShown(createActorsPanel)) {
            createActorCoordinateEditor.updateCoordinate();
            Coordinate location = createActorCoordinateEditor.getCoordinateCopy();
            
            generated.dkf.EnvironmentAdaptation.CreateActors.Heading heading = null;
            if (createActorHeadingToggle.isActive()) {
                Double headingValue = createActorHeadingSlider.getValue();
                heading = new generated.dkf.EnvironmentAdaptation.CreateActors.Heading();
                heading.setValue(headingValue != null ? headingValue.intValue() : 0);
            }
            
            Serializable sideType;
            if (createActorCivilianButton.isActive()) {
                sideType = new Civilian();
            } else if (createActorEnemyButton.isActive()) {
                sideType = new Opfor();
            } else if (createActorFriendlyButton.isActive()) {
                sideType = new Blufor();
            } else {
                throw new UnsupportedOperationException("No CreateActors Side type was selected");
            }

            CreateActors createActors = new CreateActors();
            createActors.setCoordinate(location);
            createActors.setHeading(heading);
            createActors.setSide(new Side());
            createActors.getSide().setType(sideType);
            createActors.setType(createActorTypeBox.getValue());
            createActors.setActorName(createActorNameBox.getValue());
            createActors.setStressCategory(stressCategory);
            
            ActorTypeCategoryEnum catEnum = null;
            try {
                catEnum = ActorTypeCategoryEnum.valueOf(createActorTypeCategoryBox.getValue());
            }catch(Exception e) {
                logger.log(Level.WARNING, 
                        "Unable to map selected actor type category of "+createActorTypeCategoryBox.getValue()+".  Using default of "+ActorTypeCategoryEnum.PERSON, e);
                // default to person
                catEnum = ActorTypeCategoryEnum.PERSON;
            }
            createActors.setTypeCategory(catEnum);
            type = createActors;
        } else if (isSubEditorShown(removeActorsPanel)) {
            RemoveActors removeActors = new RemoveActors();
            removeActors.setType(removeActorNameBox.getValue());
            
            ActorTypeCategoryEnum catEnum = null;
            try {
                catEnum = ActorTypeCategoryEnum.valueOf(removeActorTypeCategoryBox.getValue());
            }catch(Exception e) {
                logger.log(Level.WARNING, 
                        "Unable to map selected actor type category of "+removeActorTypeCategoryBox.getValue()+".  Using default of "+ActorTypeCategoryEnum.PERSON, e);
                // default to person
                catEnum = ActorTypeCategoryEnum.PERSON;
            }
            removeActors.setTypeCategory(catEnum);

            removeActors.setStressCategory(stressCategory);

            type = removeActors;
        } else if (isSubEditorShown(scriptPanel)) {
            String value = scriptTextArea.getValue();

            Script script = new Script();
            script.setValue(value);
            script.setStressCategory(stressCategory);
            type = script;
        } else if (isSubEditorShown(timePanel)) {
            Serializable timeType;
            if (dawnTimeButton.isActive()) {
                timeType = new Dawn();
            } else if (middayTimeButton.isActive()) {
                timeType = new Midday();
            } else if (duskTimeButton.isActive()) {
                timeType = new Dusk();
            } else if (midnightTimeButton.isActive()) {
                timeType = new Midnight();
            } else {
                throw new UnsupportedOperationException("No TimeofDay type was selected");
            }

            TimeOfDay timeOfDay = new TimeOfDay();
            timeOfDay.setType(timeType);
            type = timeOfDay;
        } else if (isSubEditorShown(endurancePanel)) {
            BigDecimal value = BigDecimal.valueOf(enduranceSlider.getValue());
            
            Endurance.TeamMemberRef memberRef = null;
            
            if(StringUtils.isNotBlank(enduranceMemberPicker.getValue())) {
                
                memberRef = new Endurance.TeamMemberRef();
                memberRef.setValue(enduranceMemberPicker.getValue());
                
                TeamMember member = ScenarioClientUtility.getTeamMemberWithName(memberRef.getValue());
                
                if(member != null && member.getLearnerId() != null && member.getLearnerId().getType() instanceof String) {
                    memberRef.setEntityMarking((String) member.getLearnerId().getType());
                }
            }

            Endurance endurance = new Endurance();
            endurance.setValue(value);
            endurance.setTeamMemberRef(memberRef);
            endurance.setStressCategory(stressCategory);
            type = endurance;
            
        } else if(isSubEditorShown(addHighlightObjectPanel)){
            
            HighlightObjects.Color color = new HighlightObjects.Color();
            if(highlightRedButton.isActive()){
                color.setType(new HighlightObjects.Color.Red());
            }else if(highlightGreenButton.isActive()){
                color.setType(new HighlightObjects.Color.Green());
            }else if(highlightBlueButton.isActive()){
                color.setType(new HighlightObjects.Color.Blue());
            }

            String name = highlightNameTextbox.getValue();

            // set the offset widgets
            Offset offset = new Offset();
            offset.setFront(new BigDecimal(highlightOffsetFrontTextbox.getValue()));
            offset.setRight(new BigDecimal(highlightOffsetRightTextbox.getValue()));
            offset.setUp(new BigDecimal(highlightOffsetUpTextbox.getValue()));
            
            // set the highlighted object type widget
            Serializable highlightType = null;
            if(highlightSubEditorDeckPanel.getVisibleWidget() == highlightSubEditorDeckPanel.getWidgetIndex(highlightMemberPicker)){
                               
                HighlightObjects.TeamMemberRef teamMemberRef = null;
                
                if(StringUtils.isNotBlank(highlightMemberPicker.getValue())) {
                    
                    teamMemberRef = new HighlightObjects.TeamMemberRef();
                    teamMemberRef.setValue(highlightMemberPicker.getValue());
                    
                    TeamMember member = ScenarioClientUtility.getTeamMemberWithName(teamMemberRef.getValue());
                    
                    if(member != null && member.getLearnerId() != null && member.getLearnerId().getType() instanceof String) {
                        teamMemberRef.setEntityMarking((String) member.getLearnerId().getType());
                    }
                }
                
                highlightType = teamMemberRef;
                
            }else{
                
                generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                        new generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo();
                String poiName = highlightLocationPicker.getValue();
                locationInfo.setPlaceOfInterestRef(poiName);
                highlightType = locationInfo;
            }          
            
            HighlightObjects highlightObjects = new HighlightObjects();
            highlightObjects.setName(name);
            highlightObjects.setColor(color);
            highlightObjects.setOffset(offset);
            highlightObjects.setType(highlightType);
            highlightObjects.setStressCategory(stressCategory);
            type = highlightObjects;
            
        }else if(isSubEditorShown(removeHighlightObjectPanel)){
            
            RemoveHighlightOnObjects removeHighlight = new RemoveHighlightOnObjects();
            removeHighlight.setHighlightName(removeHighlightNameBox.getValue());
            removeHighlight.setStressCategory(stressCategory);
            type = removeHighlight;
            
        }else if(isSubEditorShown(createBreadcrumbsPanel)){
            
            CreateBreadcrumbs breadcrumbs = new CreateBreadcrumbs();
            
            List<String> members = createBreadcrumbsTeamPicker.getValue();
            for(String member : members){
                generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef teamMemberRef = 
                        new generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef();
                teamMemberRef.setValue(member);
                
                TeamMember teamMember = ScenarioClientUtility.getTeamMemberWithName(teamMemberRef.getValue());
                
                if(teamMember != null && teamMember.getLearnerId() != null && teamMember.getLearnerId().getType() instanceof String) {
                    teamMemberRef.setEntityMarking((String) teamMember.getLearnerId().getType());
                }
                
                breadcrumbs.getTeamMemberRef().add(teamMemberRef);
            }
            
            generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = 
                    new generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo();
            String poiName = breadcrumbLocationPicker.getValue();
            locationInfo.setPlaceOfInterestRef(poiName);
            breadcrumbs.setLocationInfo(locationInfo);
            breadcrumbs.setStressCategory(stressCategory);
            
            type = breadcrumbs;
            
        }else if(isSubEditorShown(removeBreadcrumbsPanel)){
            
            RemoveBreadcrumbs removeCrumbs = new RemoveBreadcrumbs();
            
            List<String> members = removeBreadcrumbsTeamPicker.getValue();
            for(String member : members){
                generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef teamMemberRef = 
                        new generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef();
                teamMemberRef.setValue(member);
                
                TeamMember teamMember = ScenarioClientUtility.getTeamMemberWithName(teamMemberRef.getValue());
                
                if(teamMember != null && teamMember.getLearnerId() != null && teamMember.getLearnerId().getType() instanceof String) {
                    teamMemberRef.setEntityMarking((String) teamMember.getLearnerId().getType());
                }
                
                removeCrumbs.getTeamMemberRef().add(teamMemberRef);
            }      
            
            removeCrumbs.setStressCategory(stressCategory);

            type = removeCrumbs;
            
        } else {
            throw new UnsupportedOperationException("It is unknown how to apply the changes from the currently visible editor");
        }

        EnvironmentAdaptation environmentAdaptation = new EnvironmentAdaptation();
        environmentAdaptation.setType(type);
        scenarioAdaptation.setEnvironmentAdaptation(environmentAdaptation);

        String descriptionText = isDescriptionPanelVisible() ? descriptionTextBox.getText() : null;
        scenarioAdaptation.setDescription(descriptionText);
        
        this.scenarioAdaptation = scenarioAdaptation;
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teleportMemberPicker);
        childValidationComposites.add(fatigueMemberPicker);
        childValidationComposites.add(enduranceMemberPicker);
        childValidationComposites.add(highlightMemberPicker);
        childValidationComposites.add(highlightLocationPicker);
        childValidationComposites.add(breadcrumbLocationPicker);
        childValidationComposites.add(createBreadcrumbsTeamPicker);
        childValidationComposites.add(removeBreadcrumbsTeamPicker);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(createActorsTypeValidation);
        validationStatuses.add(createActorsNameValidation);
        validationStatuses.add(removeActorsNameValidation);
        validationStatuses.add(scriptValueValidation);
        validationStatuses.add(timeOfDayValidation);
        validationStatuses.add(highlightNameValidation);
        validationStatuses.add(highlightTypeChosenValidation);
        validationStatuses.add(highlightOffsetFrontNumberValidation);
        validationStatuses.add(highlightOffsetRightNumberValidation);
        validationStatuses.add(highlightOffsetUpNumberValidation);
        validationStatuses.add(removeHighlightNameValidation);
        validationStatuses.add(highlightAGLLocationValidation);
        validationStatuses.add(breadcrumbAGLLocationValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (createActorsTypeValidation.equals(validationStatus)) {
            boolean createActorsTypeIsSelected = isSubEditorShown(createActorsPanel);
            boolean actorTypeIsBlank = StringUtils.isBlank(createActorTypeBox.getValue());
            validationStatus.setValidity(!createActorsTypeIsSelected || !actorTypeIsBlank);
        }else if(createActorsNameValidation.equals(validationStatus)) {
            boolean createActorsTypeIsSelected = isSubEditorShown(createActorsPanel);
            // can be empty/null or alphanumeric
            boolean actorNameIsGood = StringUtils.isBlank(createActorNameBox.getValue()) || createActorNameBox.getValue().matches("^[a-zA-Z0-9]*$");
            validationStatus.setValidity(!createActorsTypeIsSelected || actorNameIsGood);
        }else if(removeActorsNameValidation.equals(validationStatus)) {
            boolean removeActorsTypeIsSelected = isSubEditorShown(removeActorsPanel);
            // can be empty/null or alphanumeric
            boolean actorNameIsGood = StringUtils.isNotBlank(removeActorNameBox.getValue()) && removeActorNameBox.getValue().matches("^[a-zA-Z0-9]*$");
            validationStatus.setValidity(!removeActorsTypeIsSelected || actorNameIsGood);
        } else if (scriptValueValidation.equals(validationStatus)) {
            boolean scriptTypeIsSelected = isSubEditorShown(scriptPanel);
            boolean scriptTextIsBlank = StringUtils.isBlank(scriptTextArea.getValue());
            validationStatus.setValidity(!scriptTypeIsSelected || !scriptTextIsBlank);
        } else if(timeOfDayValidation.equals(validationStatus)){     
            boolean timeOfDayIsSelected = isSubEditorShown(timePanel);
            validationStatus.setValidity(!timeOfDayIsSelected || dawnTimeButton.isActive()
                    || middayTimeButton.isActive() || duskTimeButton.isActive() || midnightTimeButton.isActive());
        } else if(highlightNameValidation.equals(validationStatus)){
            boolean addHighlightTypeIsSelected = isSubEditorShown(addHighlightObjectPanel);
            boolean highlightNameIsAlphaNumeric = StringUtils.isAlphaNumeric(highlightNameTextbox.getValue());
            validationStatus.setValidity(!addHighlightTypeIsSelected || highlightNameIsAlphaNumeric);
        } else if(highlightOffsetFrontNumberValidation.equals(validationStatus)){
            boolean addHighlightTypeIsSelected = isSubEditorShown(addHighlightObjectPanel);
            boolean notANumber = true;
            try{
                Double.parseDouble(highlightOffsetFrontTextbox.getValue());
                notANumber = false;
            }catch(@SuppressWarnings("unused") Exception e){
                // don't care
            }
            validationStatus.setValidity(!addHighlightTypeIsSelected || !notANumber);
        }else if(highlightOffsetUpNumberValidation.equals(validationStatus)){
            boolean addHighlightTypeIsSelected = isSubEditorShown(addHighlightObjectPanel);
            boolean notANumber = true;
            try{
                Double.parseDouble(highlightOffsetUpTextbox.getValue());
                notANumber = false;
            }catch(@SuppressWarnings("unused") Exception e){
                // don't care
            }
            validationStatus.setValidity(!addHighlightTypeIsSelected || !notANumber);
        }else if(highlightOffsetRightNumberValidation.equals(validationStatus)){
            boolean addHighlightTypeIsSelected = isSubEditorShown(addHighlightObjectPanel);
            boolean notANumber = true;
            try{
                Double.parseDouble(highlightOffsetRightTextbox.getValue());
                notANumber = false;
            }catch(@SuppressWarnings("unused") Exception e){
                // don't care
            }
            validationStatus.setValidity(!addHighlightTypeIsSelected || !notANumber);
        }else if(removeHighlightNameValidation.equals(validationStatus)){
            boolean removeHighlightTypeIsSelected = isSubEditorShown(removeHighlightObjectPanel);
            List<String> currHighlightNames = getAvailableHighlightNames();
            boolean notAHighlight = !currHighlightNames.contains(removeHighlightNameBox.getValue());
            
            validationStatus.setValidity(!removeHighlightTypeIsSelected || !notAHighlight);
        }else if(highlightTypeChosenValidation.equals(validationStatus)){
            // check that a highlight type choice has been made
            boolean addHighlightTypeIsSelected = isSubEditorShown(addHighlightObjectPanel);
            boolean highlightTypeRibbonShown = highlightTypeRootPanel.getVisibleWidget() ==
                    highlightTypeRootPanel.getWidgetIndex(highlightTypeRibbon);

            validationStatus.setValidity(!addHighlightTypeIsSelected || !highlightTypeRibbonShown);
            
        }else if(highlightAGLLocationValidation.equals(validationStatus)){
            
            boolean addHighlightTypeIsSelected = isSubEditorShown(addHighlightObjectPanel);
            boolean addLocationHighlightIsSelection = highlightSubEditorDeckPanel.getVisibleWidget() ==
                    highlightSubEditorDeckPanel.getWidgetIndex(highlightLocationPicker);
            TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
            boolean isVbs = taType == TrainingApplicationEnum.VBS;
            String poiName = highlightLocationPicker.getValue();
            Serializable poi = ScenarioClientUtility.getPlaceOfInterestWithName(poiName);
            boolean validPOIAGL = false;
            if(poi != null){
                if(poi instanceof Point){
                    // highlight only works with points, not areas or paths
                    Point point = (Point)poi;
                    validPOIAGL = point.getCoordinate() != null && point.getCoordinate().getType() instanceof generated.dkf.AGL;
                }
            }
            
            // if not editing create highlight or not editing create highlight location type or is not VBS training app
            // or is a valid place of interest using AGL
            validationStatus.setValidity(!addHighlightTypeIsSelected || !addLocationHighlightIsSelection || !isVbs || validPOIAGL);
            
        }else if(breadcrumbAGLLocationValidation.equals(validationStatus)){
            
            boolean addBreadcrumbTypeIsSelected = isSubEditorShown(createBreadcrumbsPanel);
            TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
            boolean isVbs = taType == TrainingApplicationEnum.VBS;
            String poiName = breadcrumbLocationPicker.getValue();
            Serializable poi = ScenarioClientUtility.getPlaceOfInterestWithName(poiName);
            boolean validPOIAGL = false;
            if(poi != null){
                if(poi instanceof Point){
                    Point point = (Point)poi;
                    validPOIAGL = point.getCoordinate() != null && point.getCoordinate().getType() instanceof generated.dkf.AGL;
                }else if(poi instanceof Path){
                    Path path = (Path)poi;
                    List<Segment> segments = path.getSegment();
                    if(!segments.isEmpty()){
                        // just check the first segments coordinate type
                        Segment firstSegment = segments.get(0);
                        if(firstSegment.getStart() != null){
                            validPOIAGL = firstSegment.getStart().getCoordinate() != null &&
                                    firstSegment.getStart().getCoordinate().getType() instanceof AGL;
                        }
                    }
                }
            }
            
            validationStatus.setValidity(!addBreadcrumbTypeIsSelected || !isVbs || validPOIAGL);
        }
    }

    @Override
    protected boolean validate(ScenarioAdaptation adaptation) {
        String errorMsg = ScenarioValidatorUtility.validateScenarioAdaptation(adaptation);
        return StringUtils.isBlank(errorMsg);
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    @Override
    public void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        /* Fatigue widgets */
        fatigueRateSpinner.setEnabled(!isReadonly);
        fatigueMemberPicker.setReadonly(isReadonly);

        /* Fog widgets */
        fogDensitySlider.setEnabled(!isReadonly);
        fogTransitionTimeBox.setEnabled(!isReadonly);
        fogColorToggle.setEnabled(!isReadonly);
        fogColorBox.setEnabled(!isReadonly);

        /* Rain widgets */
        rainSlider.setEnabled(!isReadonly);
        rainTransitionTimeBox.setEnabled(!isReadonly);

        /* Overcast widgets */
        overcastSlider.setEnabled(!isReadonly);
        overcastTransitionTimeBox.setEnabled(!isReadonly);

        /* Teleport Learner widgets */
        teleportCoordinateEditor.setReadOnly(isReadonly);
        teleportHeadingToggle.setEnabled(!isReadonly);
        teleportHeadingSlider.setEnabled(!isReadonly);
        teleportMemberPicker.setReadonly(isReadonly);

        /* Create Actors widgets */
        createActorCoordinateEditor.setReadOnly(isReadonly);
        createActorHeadingToggle.setEnabled(!isReadonly);
        createActorHeadingSlider.setEnabled(!isReadonly);
        createActorTypeBox.setEnabled(!isReadonly);
        createActorNameBox.setEnabled(!isReadonly);
        createActorEnemyButton.setEnabled(!isReadonly);
        createActorFriendlyButton.setEnabled(!isReadonly);
        createActorCivilianButton.setEnabled(!isReadonly);
        createActorTypeCategoryBox.setEnabled(!isReadonly);

        /* Remove Actors widgets */
        removeActorNameBox.setEnabled(!isReadonly);
        removeActorTypeCategoryBox.setEnabled(!isReadonly);

        /* Script widgets */
        scriptTextArea.setEnabled(!isReadonly);

        /* Time of Day widgets */
        dawnTimeButton.setEnabled(!isReadonly);
        middayTimeButton.setEnabled(!isReadonly);
        duskTimeButton.setEnabled(!isReadonly);
        midnightTimeButton.setEnabled(!isReadonly);

        /* Endurance widgets */
        enduranceSlider.setEnabled(!isReadonly);
        enduranceMemberPicker.setReadonly(isReadonly);
        
        /* Highlight objects widgets */
        highlightRedButton.setEnabled(!isReadonly);
        highlightGreenButton.setEnabled(!isReadonly);
        highlightBlueButton.setEnabled(!isReadonly);
        highlightLocationPicker.setReadonly(isReadonly);
        highlightMemberPicker.setReadonly(isReadonly);
        highlightNameTextbox.setEnabled(!isReadonly);
        highlightOffsetFrontTextbox.setEnabled(!isReadonly);
        highlightOffsetRightTextbox.setEnabled(!isReadonly);
        highlightOffsetUpTextbox.setEnabled(!isReadonly);
        
        /* Remove highlight widgets */
        removeHighlightNameBox.setEnabled(!isReadonly);
        
        /* create bread crumbs widgets */
        breadcrumbLocationPicker.setReadonly(isReadonly);
        createBreadcrumbsTeamPicker.setReadonly(isReadonly);
        
        /* remove bread crumbs widgets */
        removeBreadcrumbsTeamPicker.setReadonly(isReadonly);
        
        descriptionTextBox.setEnabled(!isReadonly);
        descriptionButton.setVisible(!isReadonly);
        
        environmentalStressCategoryButton.setEnabled(!isReadonly);
        cognitiveStressCategoryButton.setEnabled(!isReadonly);
        physiologicalStressCategoryButton.setEnabled(!isReadonly);
    }

    /**
     * Shows the given {@link Widget} within the {@link #subEditorControlsPanel}
     *
     * @param widget The {@link Widget} to show within the
     *        {@link #subEditorControlsPanel}. Can't be null and must be a
     *        direct child of {@link #subEditorControlsPanel}.
     * @param iconType The icon type to show in the {@link #subEditorTypeIcon}.
     *        Can't be null.
     * @param iconToolTip The tool tip to show when the user hovers over the
     *        {@link #subEditorTypeIcon}.
     */
    private void showSubEditor(Widget widget, IconType iconType, String iconToolTip) {
        if (iconType == null) {
            throw new IllegalArgumentException("The parameter 'iconType' cannot be null.");
        }

        if (iconToolTip == null) {
            throw new IllegalArgumentException("The parameter 'iconToolTip' cannot be null.");
        }

        rootPanel.showWidget(rootPanel.getWidgetIndex(subEditorPanel));
        subEditorControlsPanel.showWidget(subEditorControlsPanel.getWidgetIndex(widget));
        subEditorTypeIcon.setType(iconType);
        subEditorTypeIconTooltip.setTitle(iconToolTip);

        validateAll();
    }

    /**
     * Shows the {@link #adaptationTypeRibbon} within the {@link #rootPanel}.
     */
    private void showSATypeRibbon() {
        
        rootPanel.showWidget(rootPanel.getWidgetIndex(adaptationTypeRibbon));
    }

    /**
     * Determines whether or not a given {@link Widget} is currently visible
     * within the {@link #subEditorControlsPanel}.
     *
     * @param widget The {@link Widget} whose visibility to check. Can't be
     *        null.
     * @return True if the given {@link Widget} is visible within
     *         {@link #subEditorControlsPanel}, false otherwise.
     */
    private boolean isSubEditorShown(Widget widget) {
        if (widget == null) {
            throw new IllegalArgumentException("The parameter 'widget' cannot be null.");
        }

        // the subeditor (e.g. remove actor editor) is shown if the rootpanel is not showing the 
        // adaptation type ribbon (i.e. choose type of scenario adaptation)
        return rootPanel.getVisibleWidget() != rootPanel.getWidgetIndex(adaptationTypeRibbon) &&
                subEditorControlsPanel.getVisibleWidget() == subEditorControlsPanel.getWidgetIndex(widget);
    }
}
