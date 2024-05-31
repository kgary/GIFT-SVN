/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.gwt.shared.ExperimentParameters;
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.ExperimentUrlManager;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.bootstrap.DashboardErrorWidget.ErrorMessage;
import mil.arl.gift.tools.dashboard.client.bootstrap.LoadCourseParameters;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PerformanceNodeDataDisplay;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.VolumeSetting;
import mil.arl.gift.tools.dashboard.client.websocket.ResumeLtiSessionHandler;
import mil.arl.gift.tools.dashboard.client.websocket.ResumeSessionHandler;
import mil.arl.gift.tools.dashboard.shared.PageLoadError;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExistingSessionResponse;

/**
 * The main entry point of the dashboard client. This class implements the GWT
 * EntryPoint onModuleLoad() function which is the 'main' starting point where
 * the client is loaded.
 *
 * @author nblomberg
 *
 */
public class Dashboard implements EntryPoint {

    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(Dashboard.class.getName());

    // Messaging for critical errors.
    private final String PAGELOAD_ERROR_TITLE = "Error Retrieving Server Properties";
    private final String PAGELOAD_THROWABLE_MSG = "<html>A throwable error occurred retrieving the server properties.  Unable to display the GIFT login page.<br>Please make sure you have a valid network connection and try reloading the page.</html>";

    /**
     * URL of the servlet handling files uploaded for importing. This is
     * configurable in src/mil/arl/gift/tools/dashboard/war/WEB-INF/web.xml
     */
    public static final String IMPORT_SERVLET_URL = "dashboard/import/";
    
    /**
     * URL of the 'nuxeoFileServlet' servlet handling files for bulk adding course sharing permissions. This is
     * configurable in src/mil/arl/gift/tools/dashboard/war/WEB-INF/web.xml
     */
    public static final String USER_PERMISSIONS_SERVLET_URL = "dashboard/cm/";

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service.
     */
    private final DashboardServiceAsync dashboardService = GWT.create(DashboardService.class);

    /** instance of the dashboard singleton object. */
    private static Dashboard instance = null;

    /**
     * The server properties are properties sent from the server to the client.
     */
    private ServerProperties serverProperties = null;
    
    /** Timeout flag for playing beep sound */
    private static boolean isBeepPlaying = false;
    
    /**
     * Enumeration of the types of sounds that could be played by the dashboard.
     * 
     * @author mhoffman
     *
     */
    public enum AssessmentSoundType{
        /** The 'poor' assessment sound type */
        POOR_ASSESSMENT,
        /** The 'good' assessment sound type */
        GOOD_ASSESSMENT;
        
        /**
         * Return whether the first sound type is a higher priority to play than the second sound type.
         * 
         * @param first the first sound type to check for priority 
         * @param second the second sound type to check for priority
         * @return true if:
         * 1. the first is poor assessment
         * 2. the first is good assessment and the second is null (not set)
         */
        public static boolean isHigherPriority(AssessmentSoundType first, AssessmentSoundType second){
            return first == POOR_ASSESSMENT || 
                    (first == GOOD_ASSESSMENT && second == null);
        }
    }
    
    /**
     * An enumeration of volume settings that are used throughout the Dashboard. These can be used
     * to turn off or modify the volume of certain sounds across the entire dashboard.
     * 
     * @author nroberts
     */
    public static enum VolumeSettings{
        
        /** Sets the volume of all Dashboard sounds at once*/
        ALL_SOUNDS(null),
        
        // Individual sounds
        
        /** Sets the volume of the sound that plays to indicate a poor assessment */
        POOR_ASSESSMENT_SOUND(ALL_SOUNDS),
        
        /** Sets the volume of the sound that plays to indicate a good assessment */
        GOOD_ASSESSMENT_SOUND(ALL_SOUNDS),
        
        /** Sets the volume of the sound that plays when the OC receives audio feedback */
        FEEDBACK_SOUND(ALL_SOUNDS),
        
        /** Sets the volume of any captured audio playing alongside a past session playbck */
        PAST_SESSION_AUDIO(ALL_SOUNDS),
        
        /** Sets the volume of any audio from captured videos playing alongside a past session playback */
        VIDEO_AUDIO(ALL_SOUNDS);
        
        /** The setting for this enum */
        private VolumeSetting setting;
        
        /**
         * Creates a new enumerated volume setting with the given parent. 
         * 
         * @param parent the parent of this volume setting. If non-null, then all volume checks for this
         * setting will be compared to the parent to see if the parent has a lower volume assigned.
         */
        private VolumeSettings(VolumeSettings parent) {
            this.setting = new VolumeSetting();
            
            if(parent != null) {
                parent.getSetting().addSubVolumeSetting(this.setting);
            }
        }
        
        /**
         * Gets the volume settings for this enumerated setting
         * 
         * @return the volume settings. Will not be null.
         */
        public VolumeSetting getSetting() {
            return setting;
        }
    }
    
    /**
     * Dashboard settings
     * 
     * @author cpadilla
     *
     */
    public static class Settings {
        
        /**
         * The names of settings that can be cached to local browser storage and 
         * are preserved between visits to the webpage
         * 
         * @author nroberts
         */
        private enum CachedSetting{
            AUTO_APPLY_STRATEGIES,
            AUTO_ADVANCE_SESSIONS,
            LEFT_DISPLAY_PANEL,
            RIGHT_DISPLAY_PANEL,
        	SHOW_OC_ONLY,
        	HIDE_POOR_ASSESSMENT_VISUAL,
        	HIDE_OC_ASSESSMENT_VISUAL,
        	PRIORITIZE_OC_ASSESSMENT,
        	SHOW_TEAM_ORG_NAME,
        	SHOW_MINIMAP,
        	SHOW_SCENARIO_SUPPORT,
        	HIDE_GOOD_ASSESSMENTS,
        	APPLY_CHANGES_AT_PLAYHEAD,
        	SHOW_ALL_TASKS,
        	MIL_SYMBOL_SCALE;
        }
        
        /**
         * An abstract representation of a setting that has been cached to local browser storage
         * 
         * @author nroberts
         * @param <T> the type of valye that is associated with this setting. This generic argument
         * allows values other than strings to be saved to local storage.
         */
        private abstract class CachedSettingValue<T>{
            
            /** The name of the setting. Used when reading/writing to/from local storage  */
            private CachedSetting setting;
            
            /** A copy of the stored value in memory */
            private T value;
            
            /**
             * Creates a new cached setting value that sets the given setting name
             * to the given default value. If a value for this setting name already exists
             * in local storage, then the stored value will be loaded instead.
             * 
             * @param setting the setting name associated with the value.
             * @param defaultValue the default value. Can be null
             */
            public CachedSettingValue(CachedSetting setting, T defaultValue) {
                
                this.setting = setting;
                
                String valString = null;
                
                Storage storage = Storage.getLocalStorageIfSupported();
                if(storage != null) {
                    valString = storage.getItem(setting.name());
                }
                
                if(StringUtils.isBlank(valString)) {
                    
                    /* No value in local storage, so use the default*/
                    value = defaultValue;
                
                } else {
                    
                    /* A value exists in local storage, so load it */
                    try {
                        value = fromCacheString(valString);
                        
                    } catch(Exception e) {
                        logger.severe("Unable to load value '" + valString + "' for '" + setting + "' due to an error: " + e);
                        value = defaultValue;
                    }
                }
            }
            
            /**
             * Updates the setting to use the given value
             * 
             * @param value the new value to use. Can be null.
             */
            public void setValue(T value) {
                
                this.value = value;
                    
                try {
                    Storage storage = Storage.getLocalStorageIfSupported();
                    if(storage != null) {
                        String valString = toCacheString(value);
                        storage.setItem(setting.name(), valString);
                    }
                } catch(Exception e) {
                    logger.severe("Unable to save value '" + value + "' for '" + setting + "' due to an error: " + e);
                }
            }
            
            /**
             * Gets the current value associated with the setting
             * 
             * @return the current value. Can be null.
             */
            public T getValue() {
                return value;
            }
            
            /**
             * Encodes the given value to a string that is safe for local storage
             * 
             * @param value the value to encode. Can be null.
             * @return the same value encoded to a string safe for local storage. Can be null.
             */
            public abstract String toCacheString(T value);
            
            /**
             * Decodes the given storage string into an actual value object
             * 
             * @param valString a value encoded to a string safe for local storage. Can be null.
             * @return the original value that was encoded to a string. Can be null.
             */
            public abstract T fromCacheString(String valString);

            @Override
            public String toString() {
                return "CachedSettingValue [setting=" + setting + ", value=" + value + "]";
            }
        }
        
        /**
         * A type of setting value that accepts and caches strings.
         * 
         * @author nroberts
         */
        private class StringSettingValue extends CachedSettingValue<String>{
            
            /**
             * Creates a new string setting value
             * 
             * @param setting the setting name associated with the value.
             * @param defaultValue the default value. Can be null
             */
            public StringSettingValue(CachedSetting setting, String defaultValue) {
                super(setting, defaultValue);
            }

            @Override
            public String fromCacheString(String valString) {
                return valString;
            }
            
            @Override
            public String toCacheString(String value) {
                return value;
            }
        }
        
        /**
         * A type of setting value that accepts and caches booleans.
         * 
         * @author nroberts
         */
        private class BooleanSettingValue extends CachedSettingValue<Boolean>{

            /**
             * Creates a new boolean setting value
             * 
             * @param setting the setting name associated with the value.
             * @param defaultValue the default value. Can be null
             */
            public BooleanSettingValue(CachedSetting setting, Boolean defaultValue) {
                super(setting, defaultValue);
            }
            
            @Override
            public String toCacheString(Boolean value) {
                return value.toString();
            }
            
            @Override
            public Boolean fromCacheString(String valString) {
                return Boolean.valueOf(valString);
            }
        }
        
        /**
         * A type of setting value that accepts and caches doubles
         * 
         * @author jlouis
         */
        private class DoubleSettingValue extends CachedSettingValue<Double>{
        	
        	/**
             * Creates a new double setting value
             * 
             * @param setting the setting name associated with the value.
             * @param defaultValue the default value. Can be null
             */
        	public DoubleSettingValue(CachedSetting setting, Double defaultValue) {
        		super(setting, defaultValue);
        	}
        	
        	@Override
            public String toCacheString(Double value) {
                return value.toString();
            }
            
            @Override
            public Double fromCacheString(String valString) {
                return Double.valueOf(valString);
            }
        }
        
        /** 
         * A comparator used to sort concepts based on whether the user has enabled 
         * the setting to visually prioritize concepts 
         */
        public final Comparator<PerformanceNodeDataDisplay> PERF_NODE_SORT_COMPARATOR = new Comparator<PerformanceNodeDataDisplay>() {

            @Override
            public int compare(PerformanceNodeDataDisplay o1, PerformanceNodeDataDisplay o2) {
                
                if(!isPrioritizeOCAssessment()) {
                    return 0; //don't sort performance nodes if sorting is turned off in the settings
                }
                
                //sort performance nodes so that ones that require an observer controller assessment are ordered first
                return Boolean.compare(o1 == null || !o1.requiresObservedAssessment(), 
                        o2 == null || !o2.requiresObservedAssessment());
            }
        };
        
        /** Option to hide the poor assessment visuals (e.g. red highlight task/concept panel) */
        private BooleanSettingValue hidePoorAssessmentVisual = new BooleanSettingValue(CachedSetting.HIDE_POOR_ASSESSMENT_VISUAL, false);
        
        /** Option to hide the observer controller assessment visuals (e.g. yellow highlight concept panel) */
        private BooleanSettingValue hideOCAssessmentVisual = new BooleanSettingValue(CachedSetting.HIDE_OC_ASSESSMENT_VISUAL, false);

        /** Option to show only the observer controller assessments */
        private BooleanSettingValue showOcOnly = new BooleanSettingValue(CachedSetting.SHOW_OC_ONLY, false);

        /** Option to sort so that controller assessment concepts are shown first */
        private BooleanSettingValue prioritizeOCAssessment = new BooleanSettingValue(CachedSetting.PRIORITIZE_OC_ASSESSMENT, false);
        
        /** Option to show the team org name on the map icons (i.e. the white labels to the lower left of icons) */
        private BooleanSettingValue showTeamOrgName = new BooleanSettingValue(CachedSetting.SHOW_TEAM_ORG_NAME, true);
        
        /** Option to show the mini map */
        private BooleanSettingValue showMiniMap = new BooleanSettingValue (CachedSetting.SHOW_MINIMAP, true);
        
        /** Option to show scenario support task/concepts in the timeline and assessment panels */
        private BooleanSettingValue showScenarioSupport = new BooleanSettingValue(CachedSetting.SHOW_SCENARIO_SUPPORT, false);
        
        /** Option to hide the automated assessments that have a good performance assessment */
        private BooleanSettingValue hideGoodAutoAssessments = new BooleanSettingValue(CachedSetting.HIDE_GOOD_ASSESSMENTS, false);

        /**
         * Option to apply playback changes starting at the playhead location
         */
        private BooleanSettingValue applyChangesAtPlayhead = new BooleanSettingValue(CachedSetting.APPLY_CHANGES_AT_PLAYHEAD, false);

        /** Option to show all tasks in the assessment panel */
        private BooleanSettingValue showAllTasks = new BooleanSettingValue(CachedSetting.SHOW_ALL_TASKS, false);
        
        /** The size at which military symbols should be scaled relative to their default size*/
        private DoubleSettingValue milSymbolScale = new DoubleSettingValue(CachedSetting.MIL_SYMBOL_SCALE, 1d);
        
        /** Whether to automatically apply strategies in Game Master */
        private BooleanSettingValue autoApplyStrategies = new BooleanSettingValue(
                CachedSetting.AUTO_APPLY_STRATEGIES, true);
        
        /** Whether to automatically advance to the next session in Game Master if a course has multiple DKF sessions */
        private BooleanSettingValue autoAdvanceSessions = new BooleanSettingValue(
                CachedSetting.AUTO_ADVANCE_SESSIONS, true);
        
        /** What panel to show in Game Master's left panel by default */
        private StringSettingValue leftDisplayPicker = new StringSettingValue(
                CachedSetting.LEFT_DISPLAY_PANEL, null);
        
        /** What panel to show in Game Master's right panel by default */
        private StringSettingValue rightDisplayPicker = new StringSettingValue(
                CachedSetting.RIGHT_DISPLAY_PANEL, null);
        
        /**
         * Constructor
         */
        public Settings() {
            
        }
        
        /**
         * Return whether all sound and visual related settings are muted/hidden.
         * 
         * @return true if all sounds and visual settings are muted/hidden.
         */
        public boolean isAllSoundsVisualsMuted(){
            return VolumeSettings.ALL_SOUNDS.getSetting().isMuted() && isHidePoorAssessmentVisual() && isHideOCAssessmentVisual();
        }
        
        /**
         * Return whether the poor assessment visuals are hidden (e.g. red highlight task/concept panel) 
         * @return true if suppose to be hidden.
         */
        public boolean isHidePoorAssessmentVisual(){
            return hidePoorAssessmentVisual.getValue();
        }
        
        /**
         * Return whether the scenario support task/concepts should be shown.
         * @return true if suppose to show the scenario support task/concepts.  Default is false.
         */
        public boolean isShowScenarioSupport() {
            return showScenarioSupport.getValue();
        }

        /**
         * Set whether the scenario support task/concepts should be shown.
         * @param showScenarioSupport true if suppose to show
         */
        public void setShowScenarioSupport(boolean showScenarioSupport) {
            this.showScenarioSupport.setValue(showScenarioSupport);
        }

        /**
         * Set whether the poor assessment visuals are hidden (e.g. red highlight task/concept panel) 
         * @param hidePoorAssessmentVisual true if suppose to be hidden
         */
        public void setHidePoorAssessmentVisual(boolean hidePoorAssessmentVisual){
            this.hidePoorAssessmentVisual.setValue(hidePoorAssessmentVisual);
        }
        
        /**
         * Return whether the observer controller assessment visuals are hidden (e.g. yellow highlight concept panel)
         * @return true if suppose to be hidden
         */
        public boolean isHideOCAssessmentVisual(){
            return hideOCAssessmentVisual.getValue();
        }

        /**
         * Set whether only the observer controller assessments should be shown.
         * 
         * @param showOcOnly true if only the observer controller assessments
         *        should be shown; false otherwise.
         */
        public void setShowOcOnly(boolean showOcOnly) {
            this.showOcOnly.setValue(showOcOnly);
        }

        /**
         * Return whether only the observer controller assessments should be
         * shown.
         * 
         * @return true if only the observer controller assessments should be
         *         shown; false otherwise.
         */
        public boolean isShowOcOnly() {
            return showOcOnly.getValue();
        }

        /**
         * Set whether the observer controller assessment visuals are hidden (e.g. yellow highlight concept panel)
         * @param hideOCAssessmentVisual true if suppose to be hidden.
         */
        public void setHideOCAssessmentVisual(boolean hideOCAssessmentVisual){
            this.hideOCAssessmentVisual.setValue(hideOCAssessmentVisual);
        }
        
        /**
         * Return whether concepts that require an observer controller assessment should be visually sorted so they are shown first
         * @return true if observer controller assessment concepts are being prioritized
         */
        public boolean isPrioritizeOCAssessment(){
            return prioritizeOCAssessment.getValue();
        }
        
        /**
         * Set whether concepts that require an observer controller assessment should be visually sorted so they are shown first
         * @param prioritize whether observer controller assessment concepts should be prioritized
         */
        public void setPrioritizeOCAssessment(boolean prioritize){
            this.prioritizeOCAssessment.setValue(prioritize);
        }
        
        /**
         * Sorts the given list of widgets that display performance node data so that their 
         * ordering matches the current setting for prioritizing concepts that require observer
         * controller assessments.
         * <br/><br/>
         * If prioritizing OC assessment concepts is enabled, then the list will be sorted so those
         * concepts have lower indexes in the list. If the setting is disabled, then the list will
         * maintain its current ordering.
         * 
         * @param perfNodes the list of widgets to sort. Cannot be null.
         * @return if prioritizing concepts that require observer controller assessments is enabled, 
         * the last sorted widget that requires such an assessment. Otherwise, null.
         */
        public PerformanceNodeDataDisplay sortByPriority(List<? extends PerformanceNodeDataDisplay> perfNodes) {
            
            Collections.sort(perfNodes, PERF_NODE_SORT_COMPARATOR);
            
            if(prioritizeOCAssessment.getValue()) {
                
                /* find and return the last widget that requires an observer 
                 * controller assessment in the sorted list */
                PerformanceNodeDataDisplay lastOCNode = null;
                for(PerformanceNodeDataDisplay node : perfNodes) {
                    if(node.requiresObservedAssessment()) {
                        lastOCNode = node;
                        
                    } else {
                        if(lastOCNode != null) {
                            return lastOCNode;
                        }
                    }
                }
                
                return lastOCNode;
            }
            
            return null;
        }
        
        /**
         * Sets if all sounds and visual should be muted/hidden.
         * 
         * @param muteAll the muteAll to set
         */
        public void setMuteAllSoundsAndVisuals(boolean muteAll){
            VolumeSettings.ALL_SOUNDS.getSetting().setMuted(muteAll);
            setHidePoorAssessmentVisual(muteAll);
            setHideOCAssessmentVisual(muteAll);
        }

        /**
         * Return the value for the option to show the team org name on the map icons (i.e. the white labels to the lower left of icons)
         * @return true to indicate the name label should be shown when appropriate.
         */
        public boolean isShowTeamOrgName() {
            return showTeamOrgName.getValue();
        }

        /**
         * Set the option to show the team org name on the map icons (i.e. the white labels to the lower left of icons)
         * @param showTeamOrgName true for showing, false for hiding
         */
        public void setShowTeamOrgName(boolean showTeamOrgName) {
            this.showTeamOrgName.setValue(showTeamOrgName);
        }
        
        /**
         * Return the value for the option to show the mini map.
         * @return true to indicate the mini map should be shown.  Default is true.
         */
        public boolean isShowMiniMap() {
        	return showMiniMap.getValue();
        }
        
        /**
         * Set the option to show the mini map.
         * @param showMiniMap true for showing, false for hiding.
         */
        public void setShowMiniMap(boolean showMiniMap) {
        	this.showMiniMap.setValue(showMiniMap);
        }
        
        /**
         * Return the value of the option to show all tasks in the assessment panel, even if a task is selected
         * 
         * @return true to indicate if all tasks should be shown
         */
        public boolean isShowAllTasks() {
            return showAllTasks.getValue();
        }
        
        /**
         * Set the option to show all tasks in the assessment panel, even if a task is selected
         * 
         * @param showAll true to show all of the tasks. False to show only the currently selected task.
         */
        public void setShowAllTasks(boolean showAll) {
            this.showAllTasks.setValue(showAll);
        }

        /**
         * Return the value of the option to apply playback changes starting at
         * the playhead location
         * 
         * @return true to indicate the playback changes should happen at the
         *         playhead location; false to indicate that the entire
         *         assessment 'block' should be updated.
         */
        public boolean isApplyChangesAtPlayhead() {
            return applyChangesAtPlayhead.getValue();
        }

        /**
         * Set the option to apply playback changes starting at the playhead
         * location
         * 
         * @param applyChangesAtPlayhead true to indicate the playback changes
         *        should happen at the playhead location; false to indicate that
         *        the entire assessment 'block' should be updated.
         */
        public void setApplyChangesAtPlayhead(boolean applyChangesAtPlayhead) {
            this.applyChangesAtPlayhead.setValue(applyChangesAtPlayhead);
        }

        /**
         * Return whether concepts with automated assessments with good performance
         * should be hidden from the user. 
         * @return true if these automated assessments should be hidden.  Default is true.
         */
        public boolean isHideGoodAutoAssessments() {
            return hideGoodAutoAssessments.getValue();
        }

        /**
         * Set whether concepts with automated assessments with good performance
         * should be hidden from the user. 
         * @param hideGoodAutoAssessments the value to use, true to hide automated assessments with good performance value.
         */
        public void setHideGoodAutoAssessments(boolean hideGoodAutoAssessments) {
            this.hideGoodAutoAssessments.setValue(hideGoodAutoAssessments);
        }
        
        /**
         * Gets the scaling factor by which military symbols should be sized
         * 
         * @return the scaling factor. 1 by default.
         */
        public double getMilSymbolScale() {
            return milSymbolScale.getValue();
        }

        /**
         * Sets the scaling factor by which military symbols should be sized
         * 
         * @param milSymbolScale the scaling factor
         */
        public void setMilSymbolScale(double milSymbolScale) {
            this.milSymbolScale.setValue(milSymbolScale);
        }
        
        public boolean isAutoApplyStrategies() {
            return autoApplyStrategies.getValue();
        }

        public void setAutoApplyStrategies(boolean autoApplyStrategies) {
            this.autoApplyStrategies.setValue(autoApplyStrategies);
        }
        
        public boolean isAutoAdvanceSessions() {
            return autoAdvanceSessions.getValue();
        }

        public void setAutoAdvanceSessions(boolean autoAdvanceSessions) {
            this.autoAdvanceSessions.setValue(autoAdvanceSessions);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[Settings: ");
            builder.append("hidePoorAssessmentVisual = ").append(hidePoorAssessmentVisual);
            builder.append(", hideOCAssessmentVisual = ").append(hideOCAssessmentVisual);
            builder.append(", hideGoodAutoAssessments = ").append(hideGoodAutoAssessments);
            builder.append(", showOcOnly = ").append(showOcOnly);
            builder.append(", prioritizeOCAssessment = ").append(prioritizeOCAssessment);
            builder.append(", showMiniMap = ").append(showMiniMap);
            builder.append(", showTeamOrgName = ").append(showTeamOrgName);
            builder.append(", showScenarioSupport = ").append(showScenarioSupport);
            builder.append(", applyChangesAtPlayhead = ").append(applyChangesAtPlayhead);
            builder.append(", showAllTasks = ").append(showAllTasks);
            builder.append(", autoApplyStrategies = ").append(autoApplyStrategies);
            builder.append(", autoAdvanceSessions = ").append(autoAdvanceSessions);
            return builder.append("]").toString();
        }

        public String getLeftDisplayPicker() {
            return leftDisplayPicker.getValue();
        }

        public void setLeftDisplayPicker(String leftDisplayPicker) {
            this.leftDisplayPicker.setValue(leftDisplayPicker);
        }

        public String getRightDisplayPicker() {
            return rightDisplayPicker.getValue();
        }

        public void setRightDisplayPicker(String rightDisplayPicker) {
            this.rightDisplayPicker.setValue(rightDisplayPicker);
        }
    }
    
    /** The settings for this dashboard instance */
    private final Settings settings = new Settings();

    /**
     * Singleton constructor
     */
    private Dashboard() {

        logger.info("Creating dashboard interface.");

        // GWT constructs this class prior to calling onModuleLoad(). We set the
        // instance here as soon as it's created.
        instance = this;
    }

    /**
     * Accessor to the dashboard singleton object. If it doesn't exist yet it
     * will be created.
     *
     * @return Dashboard - instance to the dashboard singleton object.
     */
    public static Dashboard getInstance() {
        if (instance == null) {
            instance = new Dashboard();
        }

        return instance;

    }
    
    /**
     * Get the {@link Settings} for this instance of the dashboard
     * 
     * @return the {@link Settings} for this instance of the dashboard
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Accessor to get the server properties. The server properties are a list
     * of key/value pairs that contain settings that the server can pass to the
     * client. The settings are retrieved immediately onModuleLoad() and contain
     * required parameters that the client may need to run properly.
     *
     * @return ServerProperties - Properties from the server.
     */
    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        
        /* Load a JavaScript library to display JSON hierarchies for the web monitor */
        ScriptInjector.fromUrl("js/json-viewer.js")
            .setWindow(ScriptInjector.TOP_WINDOW)
            .inject();
        
        /* Load the CSS for the JSON viewerlibrary */
        injectCss("js/json-viewer.css");

        logger.info("On module load");

        // Create an instance of the UI manager class.
        UiManager.getInstance();

        // Fetch the server properties from the server. If this fails, all we
        // can do is display an error message.
        dashboardService.getServerProperties(new AsyncCallback<ServerProperties>() {
            @Override
            public void onFailure(Throwable t) {

                logger.severe("Error retrieving server properties: " + t.getMessage());

                // In this case we won't display a modal dialog, it's a critical
                // error and we will take the user to a 'page load error'
                // screen.
                PageLoadError errorDetails = new PageLoadError(PAGELOAD_ERROR_TITLE,
                        PAGELOAD_THROWABLE_MSG + "<br><br><i>Error details</i>: " + t.getMessage());
                UiManager.getInstance().displayScreen(ScreenEnum.PAGELOAD_ERROR, errorDetails);

            }

            @Override
            public void onSuccess(ServerProperties properties) {

                logger.fine("Retrieved server properties successfully: " + properties);

                // Update browser session properties.
                BrowserSession.setServerProperties(properties);
                // Initialize any class that may need the server properties.
                init(properties);
                JsniUtility.setTracker();
                // Continue with trying to load the proper page.
                resumeInitialPageLoad();
            }

        });

        stopLoadingProgress();
    }

    /**
     * Initialize the dashboard with the properties retrieved from the server.
     *
     * @param properties
     *            - The properties from the server.
     */
    private void init(ServerProperties properties) {

        logger.fine("Dashboard init() called with properties: " + properties);

        serverProperties = properties;
        JsniUtility.setServerProperties( properties );
        // initialize any class that requires the server properties.
        UiManager.getInstance().init(serverProperties);
    }

    /**
     * Resumes the initial page load (after the server properties have been
     * retrieved)
     */
    private void resumeInitialPageLoad() {

        final String experimentId = Window.Location.getParameter(ExperimentUrlManager.EXPERIMENT_ID_URL_PARAMETER);
        final String courseCollectionId = Window.Location.getParameter(ExperimentUrlManager.COURSE_COLLECTION_ID_URL_PARAMETER);
        final String consumerKey = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_KEY);

        /* Check if any redirects are necessary due to refreshing after the
         * experiment ended */
        if (handleRedirectRules()) {
            /* Applied a redirect, no need to progress further. */
            return;
        }

        if (StringUtils.isNotBlank(experimentId)) {
            /* EXPERIMENT MODE - If experiment id parameter is not null, then
             * load the experiment welcome page. */
            loadExperimentLandingPage(experimentId);
        } else if (StringUtils.isNotBlank(courseCollectionId)) {
            /* COLLECTION MODE - Load the course collection page. */
            UiManager.getInstance().displayScreen(ScreenEnum.COURSE_COLLECTION, courseCollectionId);
        } else if (StringUtils.isNotBlank(consumerKey)) {
            /* LTI MODE - If launching an experiment with an LTI consumer key,
             * then load the LTI landing page. */
            createLtiUserSession();
        } else {
            /* NORMAL MODE - Load the normal/default dashboard page. */
            loadDefaultDashboardPage();
        }
    }

    /**
     * Checks if any redirect rules apply and performs the redirect if one does.
     * 
     * @return true if a redirect rule was applied; false otherwise.
     */
    private boolean handleRedirectRules() {
        final String historyToken = History.getToken();

        /* Handle COURSE_END redirect rules */
        if (StringUtils.equalsIgnoreCase(historyToken, HistoryManager.COURSE_END)) {
            /* Return URL redirect rule: If a return URL is provided, go to that
             * URL on course end. */
            final String returnUrlParam = Window.Location.getParameter(ExperimentUrlManager.RETURN_URL_PARAMETER);
            if (StringUtils.isNotBlank(returnUrlParam)) {
                Window.Location.replace(returnUrlParam);
                return true;
            }

            /* end of COURSE_END rules */
            return false;
        }

        /* Handle EXPERIMENT_START redirect rules */
        if (StringUtils.equalsIgnoreCase(historyToken, HistoryManager.EXPERIMENT_START)) {
            /* Return URL redirect rule: If a return URL is provided, go to that
             * URL on course end. */
            final String returnUrlParam = Window.Location.getParameter(ExperimentUrlManager.RETURN_URL_PARAMETER);
            if (StringUtils.isNotBlank(returnUrlParam)) {
                Window.Location.replace(returnUrlParam);
                return true;
            }

            /* end of EXPERIMENT_START rules */
            return false;
        }

        return false;
    }

    /**
     * Loads the welcome page for the provided experiment.
     * 
     * @param experimentId the id that references the experiment
     */
    private void loadExperimentLandingPage(final String experimentId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("loadExperimentLandingPage(" + experimentId + ")");
        }

        if (serverProperties.hasRestrictedAccess()) {
            logger.warning("Unable to load Experiment landing page because this GIFT has restricted access.");
            ErrorMessage params = new ErrorMessage("Unable to start course.",
                    "This instance of GIFT is currently under restricted access.<br><br>"
                            + "Please contact your GIFT administrator for help or try again later when the restriction has been lifted.",
                    null);
            UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, params);
            return;
        }

        String historyToken = History.getToken();

        final String returnUrl = Window.Location.getParameter(ExperimentUrlManager.RETURN_URL_PARAMETER);
        ExperimentParameters experimentParameters = new ExperimentParameters(experimentId, returnUrl);

        /* Check if we should try to 'continue' an experiment */
        if (StringUtils.isNotBlank(historyToken) && historyToken.contains(HistoryManager.USER_SESSION_ID_TAG)) {
            String userSessionId = historyToken.substring(historyToken.indexOf(HistoryManager.USER_SESSION_ID_TAG));
            userSessionId = userSessionId.substring(userSessionId.indexOf("=") + 1);
            experimentParameters.setUserSessionId(userSessionId);

            LoadCourseParameters loadCourseParameters = new LoadCourseParameters();
            loadCourseParameters.setExperimentParameters(experimentParameters);
            UiManager.getInstance().displayScreen(ScreenEnum.EXPERIMENT_RUNTIME, loadCourseParameters);
            return;
        }

        UiManager.getInstance().displayScreen(ScreenEnum.EXPERIMENT_WELCOME_PAGE, experimentParameters);
    }

    /**
     * Retrieves the LTI user session based on the consumer key and consumer id
     * that are part of the LTI launch request. This method will ask the server
     * to create an LTI user session if it doesn't already exist. It is intended
     * to be called early during the processing of the LTI launch request so
     * that a user session can be established for future requests to the server.
     */
    private void createLtiUserSession() {
        logger.info("createLtiUserSession()");
        // Get the required parameters.
        final String consumerKey = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_KEY);
        final String consumerId = Window.Location.getParameter(DocumentUtil.LTI_CONSUMER_ID);
        final String courseId = Window.Location.getParameter(DocumentUtil.LTI_COURSE_ID);
        final String dataSetId = Window.Location.getParameter(DocumentUtil.LTI_DATA_SET_ID);
        final String lisOutcomeServiceUrl = Window.Location.getParameter(DocumentUtil.LTI_OUTCOME_SERVICE_URL);
        final String lisSourcedid = Window.Location.getParameter(DocumentUtil.LTI_OUTCOME_SERVICE_SOURCEDID);

        dashboardService.createLtiUserSession(consumerKey, consumerId, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable t) {

                logger.severe("createLtiUserSession() returned failure: " + t.getMessage());

                UiManager.getInstance().setSessionId(UiManager.INVALID_SESSION);
                loadLtiLandingPage(consumerKey, consumerId, courseId, dataSetId, lisOutcomeServiceUrl, lisSourcedid, null);
            }

            @Override
            public void onSuccess(RpcResponse result) {

                if (result != null && result.isSuccess()) {

                    // This will set the user session id before loading the LTI
                    // landing screen.
                    UiManager.getInstance().setSessionId(result.getBrowserSessionId());

                    loadLtiLandingPage(consumerKey, consumerId, courseId, dataSetId, lisOutcomeServiceUrl, lisSourcedid, result.getUserSessionId());
                } else {
                    logger.severe("createLtiUserSession() returned result isSuccess = false.");
                    UiManager.getInstance().setSessionId(UiManager.INVALID_SESSION);
                    loadLtiLandingPage(consumerKey, consumerId, courseId, dataSetId, lisOutcomeServiceUrl, lisSourcedid, null);
                }
            }
        });
    }

    /**
     * Loads the LTI landing / start screen. This page should be used as the
     * initial page load for incoming LTI launch requests. Unlike the normal
     * dashboard interface the LTI landing page is used for anonymous LTI users
     * (don't login to the dashboard) and the course is auto started for the LTI
     * user.
     *
     * @param consumerKey The consumer key for the LTI user.
     * @param consumerId
     * @param dataSetId (optional) The published course id for the LTI launch.
     *        If used, the course data will be tracked by the course author. The
     *        course id of the course that will be launched.
     */
    private void loadLtiLandingPage(String consumerKey, String consumerId, String courseId, String dataSetId,
           String lisServiceUrl, String lisSourcedid, String userSessionKey) {
        logger.info("Displaying LTI landing page.");

        if(serverProperties.hasRestrictedAccess()){
            logger.warning("Unable to load LTI landing page because this GIFT has restricted access.");
            ErrorMessage params = new ErrorMessage("Unable to start course.", "This instance of GIFT is currently under restricted access.<br><br>"+
                    "Please contact your GIFT administrator for help or try again later when the restriction has been lifted.", null);
            UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, params);
        }else{
            
            LtiParameters params = new LtiParameters(consumerKey, consumerId, courseId, dataSetId, lisServiceUrl, lisSourcedid);
            if (UiManager.getInstance().getSessionId() != UiManager.INVALID_SESSION) {
                // Only open a WebSocket if the session is valid.
                ResumeLtiSessionHandler resumeHandler = new ResumeLtiSessionHandler(params);
                BrowserSession.createBrowserSession(userSessionKey, UiManager.getInstance().getSessionId(),  resumeHandler);

            } else {
                // session id is invalid, so this will display an error.
                ErrorMessage errorData = new ErrorMessage("Unable to start course.",
                        "The Lti Session was not set properly when loading the screen.", null);
                UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, errorData);
            }
            
            
        }
    }

    /**
     * Loads the default (normal) dashboard page. This page typically consists
     * of a login screen for users to login to the gift dashboard.
     */
    private void loadDefaultDashboardPage() {
        // See if the user is already logged in.
        // dashboardSession id is the browser session.
        String sessionId = SessionStorage.getItem(SessionStorage.DASHBOARD_SESSIONID_TOKEN);

        /* 
         * If there is a session ID in the session storage AND the page was reloaded, then login using the
         * existing server browser session. 
         * 
         * Explicitly checking for a reload here is necessary, since duplicating a browser tab also copies 
         * its session data just like a reload. Continuing the server browser session with a duplicated tab
         * can cause problems if the old tab is still in use, so doing this check allows us to resume after a 
         * reload but still require users to sign back in when they duplicate a tab.
         */
        if (sessionId != null && JsniUtility.wasPageReloaded()) {

            dashboardService.loginFromExistingSession(sessionId, new AsyncCallback<ExistingSessionResponse>() {

                @Override
                public void onFailure(Throwable caught) {
                    logger.info("failure trying to contact dashboard server.");
                    UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                }

                @Override
                public void onSuccess(ExistingSessionResponse result) {
                    if (result.isSuccess()) {
                        
                        // Make sure the user hasn't attempted to change the debug parameter
                        final String debugFlag = Window.Location.getParameter(DocumentUtil.DEBUG_PARAMETER);
                        final boolean newDebugFlag = debugFlag != null && debugFlag.equalsIgnoreCase(DocumentUtil.DEBUG_ENABLED_VALUE);
                        if(Boolean.compare(UiManager.getInstance().isDebugMode(), newDebugFlag) != 0){
                            logger.info("The dashboard client is requesting to change debug mode from "+UiManager.getInstance().isDebugMode()+" to "+newDebugFlag+" from outside the login page.  Redirecting to login page.");
                            UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                            return;
                        }

                        final String userName = result.getUserName();
                        final ScreenEnum state = result.getScreenState();
                        final String userSessionId = result.getUserSessionId();
                        final String password = result.getUserPass();
                        final boolean offline = result.isOffline();
                        final String browserSessionId = result.getBrowserSessionId();

                        final Command resumeSessionCommand = new Command() {

                            @Override
                            public void execute() {

                                // Use the cookie session value to set the default
                                // session information.
                                UiManager.getInstance().setSessionId(browserSessionId);
                                UiManager.getInstance().setUserName(userName);
                                UiManager.getInstance().setOfflineMode(offline);



                                if(password != null) {

                                    //password may be null in offline mode
                                    UiManager.getInstance().setUserPassword(password);
                                }

                                logger.info("cookie found, and existing session found on server, resuming session for state: "
                                        + state);


                                // If the user was on the course runtime, we don't allow
                                // resuming to the same URL. Instead we return them to
                                // the
                                // my course screen.
                                ScreenEnum screenToLoad = state;
                                if (ScreenEnum.COURSE_RUNTIME.compareTo(screenToLoad) == 0) {
                                    logger.info(
                                            "We found an existing session in course runtime state.  Returning user to my courses page.");
                                    screenToLoad = ScreenEnum.MYCOURSES;
                                }

                                // Before transitioning to the screen, we now want to establish a WebSocket connection with the server as well
                                // by creating a browser session on the client that contains a WebSocket.
                                ResumeSessionHandler resumeHandler = new ResumeSessionHandler(screenToLoad);
                                BrowserSession.createBrowserSession(userSessionId, browserSessionId,  resumeHandler);

                            }
                        };

                        if(!offline) {

                            //if GIFT is online, we're through signing in and can resume the session
                            resumeSessionCommand.execute();

                        } else {

                            //if GIFT is offline, we need to fetch our user ID before we resume the session
                            dashboardService.getUserId(userName, new AsyncCallback<RpcResponse>() {

                                @Override
                                public void onFailure(Throwable throwable) {
                                    logger.info("cookie found, but the username provided is not registered, proceeding to login page.");
                                    // Display the login page.
                                    UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                                }

                                @Override
                                public void onSuccess(RpcResponse result) {
                                    // Needed to log the user into the TUI

                                    UiManager.getInstance().setUserId(Integer.parseInt(result.getResponse()));

                                    resumeSessionCommand.execute();
                                }
                            });

                        }



                    } else {
                        logger.info("cookie found, but no existing session on server, proceeding to login page.");
                        // Display the login page.
                        UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                    }
                }

            });

        } else {
            logger.info("no cookie found -- proceeding to login.");
            UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
        }
    }

    /**
     * JSNI function used to hide the 'page loading' icon.
     */
    public static native void stopLoadingProgress()/*-{
        var loadContainer = $wnd.document.getElementById("dashboardLoadContainer");
        if (loadContainer != null) {
            loadContainer.style.display="none";
        } else {
            console.log("SEVERE: Unable to hide the loading icon, the load container was null.");
        }
    }-*/; 
    
    /**
     * Plays a beep sound using the audio file with the given URL
     * 
     * @param url the URL of the audio file to play for the beep. Cannot be null.
     */
    public static void playBeep(String url) {
        playBeep(url, null);
    }

    /**
     * Plays a beep sound using the audio file with the given URL
     * 
     * @param url the URL of the audio file to play for the beep. Cannot be null.
     * @param volume the volume to play the audio at. If null, the audio will
     * be played at the default volume.
     */
    public static void playBeep(String url, Double volume) {
        if (!isBeepPlaying && url != null) {
            isBeepPlaying = true;
            JsniUtility.playAudio(url, volume);
            Timer timer = new Timer() {
                
                @Override
                public void run() {
                    isBeepPlaying = false;
                }
            };

            timer.schedule(JsniUtility.getServerProperties().getAssessmentNotificationDelayMs());
        }
    }
    
    /**
     * Plays a beep sound indicating good performance
     * 
     * @param volume the volume to play the audio at. If null, the audio will
     * be played at the default volume.
     */
    public static void playGoodBeep(Double volume) {
        playBeep("audio/beep-good.mp3", volume);
    }
    
    /**
     * Plays a beep sound indicating poor performance
     * 
     * @param volume the volume to play the audio at. If null, the audio will
     * be played at the default volume.
     */
    public static void playPoorBeep(Double volume) {
        playBeep("audio/beep-poor.mp3", volume);
    }

    /**
     * Plays an audio file in the browser
     * 
     * @param mp3File the URL of an MP3 audio file. Can be null.
     * @param oggFile the URL of an OGG audio file. Can be null.
     * @param volume the volume to play the audio at. If null, the audio will
     * be played at the default volume.
     */
    public static native void playAudio(String mp3File, String oggFile, Double volume) /*-{ 
        var new_audio = document.createElement("audio");
        var source = document.createElement('source');
        source.type = 'audio/ogg';
        source.src = oggFile;
        new_audio.appendChild(source);
        source = document.createElement('source');
        source.type = 'audio/mpeg';
        source.src = mp3File;
        if(volume != null){
            new_audio.volume = volume;
        }
        new_audio.appendChild(source);
        document.append
        new_audio.load();
        new_audio.play();
    }-*/;
    
    /**
     * Injects a link in the document's header to load a CSS stylesheet at the given URL
     * 
     * @param url the URL of the CSS stylesheet to inject. Cannot be null.
     */
    public static native void injectCss(String url)/*-{
        var fileref=document.createElement("link");
        fileref.setAttribute("rel","stylesheet");
        fileref.setAttribute("type","text/css");
        fileref.setAttribute("href",url);
        $doc.getElementsByTagName("head")[0].appendChild(fileref);
    }-*/;
}
