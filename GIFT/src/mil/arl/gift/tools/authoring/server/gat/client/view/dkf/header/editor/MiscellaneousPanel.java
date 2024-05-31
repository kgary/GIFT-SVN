/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ObserverControls;
import generated.dkf.PreventManualStop;
import generated.dkf.Resources;
import generated.dkf.Scenario;
import generated.dkf.ScenarioControls;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableHTML;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;

/**
 * The Class MiscellaneousPanel.
 */
public class MiscellaneousPanel extends ScenarioValidationComposite {
    
    /** The extension for MP3 files */
    public static final String MP3_FILE_EXTENSION = ".mp3";

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(MiscellaneousPanel.class.getName());

    /** The ui binder. */
    private static MiscellaneousPanelUiBinder uiBinder = GWT.create(MiscellaneousPanelUiBinder.class);

    /**
     * The Interface MiscellaneousPanelUiBinder.
     */
    interface MiscellaneousPanelUiBinder extends UiBinder<Widget, MiscellaneousPanel> {
    }

    /** The text area for the scenario description. */
    @UiField
    protected EditableHTML descriptionTextArea;

    /** The checkbox for allowing the learner to manually end the scenario */
    @UiField
    protected CheckBox manualStopCheck;
    
    /** The header for the panel containing the fields for mission details */
    @UiField
    protected PanelHeader missionDetailsHeader;
    
    /** The collapsing container used to show/hide the mission details fields */
    @UiField
    protected Collapse missionDetailsCollapse;
    
    /** the mission details Source input */
    @UiField
    protected TextBox missionDetailsSourceTextbox;
    
    /** the mission details MET input */
    @UiField
    protected TextBox missionDetailsMETTextbox;
    
    /** the mission details Task input */
    @UiField
    protected TextBox missionDetailsTaskTextbox;
    
    /** the mission details Situation input */
    @UiField
    protected TextBox missionDetailsSituationTextbox;
    
    /** the mission details Goals input */
    @UiField
    protected TextBox missionDetailsGoalsTextbox;
    
    /** the mission details Condition input */
    @UiField
    protected TextBox missionDetailsConditionTextbox;
    
    /** the mission details ROE input */
    @UiField
    protected TextBox missionDetailsROETextbox;
    
    /** the mission details Threat Warning input */
    @UiField
    protected TextBox missionDetailsThreatWarningTextbox;
    
    /** the mission details Weapon Status input */
    @UiField
    protected TextBox missionDetailsWeaponStatusTextbox;
    
    /** the mission details Weapon Posture input */
    @UiField
    protected TextBox missionDetailsWeaponPostureTextbox;
    
    /** The header for the panel containing the fields controlling how the scenario is presented in the Game Master*/
    @UiField
    protected PanelHeader gameMasterHeader;
    
    /** The collapsing container used to show/hide the Game Master fields */
    @UiField
    protected Collapse gameMasterCollapse;
    
    /** The button used to pick an audio file to present to Game Master observers upon a good performance assessment */
    @UiField
    protected Button goodAudioButton;
    
    /** The tooltip for the good performance audio file button */
    @UiField
    protected Tooltip goodAudioTooltip;
    
    /** A button used to play the good performance audio file */
    @UiField
    protected Button playGoodAudioButton;
    
    /** A button used to pause the good performance audio file */
    @UiField
    protected Button pauseGoodAudioButton;
    
    /** The button used delete a good performance audio file other than the default */
    @UiField
    protected Button deleteGoodAudioButton;
    
    /** The button used to pick an audio file to present to Game Master observers upon a good performance assessment */
    @UiField
    protected Button poorAudioButton;
    
    /** The tooltip for the poor performance audio file button */
    @UiField
    protected Tooltip poorAudioTooltip;
    
    /** A button used to play the poor performance audio file */
    @UiField
    protected Button playPoorAudioButton;
    
    /** A button used to pause the poor performance audio file */
    @UiField
    protected Button pausePoorAudioButton;
    
    /** The button used delete a poor performance audio file other than the default */
    @UiField
    protected Button deletePoorAudioButton;
    
    /** The dialog for selecting MP3 audio files */
    private DefaultGatFileSelectionDialog mp3FileDialog = new DefaultGatFileSelectionDialog();
    
    /** Whether the user is currently selecting the audio file to represent good performance assessments */
    private boolean isSelectingGoodAudio = false;
    
    /** The element that is currently being used to play back good/poor performance audio */
    private AudioElement audioPlaybackElement = null;

    /**
     * Instantiates a new dkf miscellaneous properties panel.
     */
    public MiscellaneousPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        // Initialize the widgets
        initWidget(uiBinder.createAndBindUi(this));

        /* No validation on this page so mark an inactive to short-circuit any attempts to
         * validate */
        setActive(false);
        
        // Description TextArea handler
        descriptionTextArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                ScenarioClientUtility.getScenario().setDescription(descriptionTextArea.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        descriptionTextArea.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                ScenarioClientUtility.getScenario().setDescription(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        //
        // Mission details handlers
        //
        
        missionDetailsSourceTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setSource(missionDetailsSourceTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsSourceTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setSource(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsTaskTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setTask(missionDetailsTaskTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsTaskTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setTask(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsMETTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setMET(missionDetailsMETTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsMETTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setMET(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsSituationTextbox.addKeyUpHandler(new KeyUpHandler() {	
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setSituation(missionDetailsSituationTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsSituationTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setSituation(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsGoalsTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setGoals(missionDetailsGoalsTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsGoalsTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setGoals(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsConditionTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setCondition(missionDetailsConditionTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsConditionTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setCondition(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsROETextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setROE(missionDetailsROETextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsROETextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setROE(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsThreatWarningTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setThreatWarning(missionDetailsThreatWarningTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsThreatWarningTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setThreatWarning(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsWeaponStatusTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setWeaponStatus(missionDetailsWeaponStatusTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsWeaponStatusTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setWeaponStatus(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        missionDetailsWeaponPostureTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getMission().setWeaponPosture(missionDetailsWeaponPostureTextbox.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        missionDetailsWeaponPostureTextbox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getMission().setWeaponPosture(event.getValue());
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        manualStopCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                Scenario scenario = getScenario();
                if (event.getValue()) {
                    // need to allow manually skipping the training app
                    if (scenario.getResources() != null && scenario.getResources().getScenarioControls() != null) {
                        scenario.getResources().setScenarioControls(null);
                    }
                } else {
                    // need to prevent manually skipping the training app
                    if (scenario.getResources() == null) {
                        scenario.setResources(new Resources());
                    }

                    if (scenario.getResources().getScenarioControls() == null) {
                        scenario.getResources().setScenarioControls(new ScenarioControls());
                    }

                    scenario.getResources().getScenarioControls().setPreventManualStop(new PreventManualStop());
                }

                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });
        
        mp3FileDialog.setIntroMessageHTML("Please select an MP3 file to play for this performance assessment notification.");
        mp3FileDialog.setAllowedFileExtensions(new String[]{MP3_FILE_EXTENSION});
        
        goodAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                isSelectingGoodAudio = true;
                mp3FileDialog.center();
                goodAudioTooltip.hide();
            }
        });
        
        playGoodAudioButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                pauseAudioPlayback();
                
                String file = null;
                
                Scenario scenario = getScenario();
                if(scenario != null 
                        && scenario.getResources() != null 
                        && scenario.getResources().getObserverControls() != null 
                        && scenario.getResources().getObserverControls().getAudio() != null
                        && scenario.getResources().getObserverControls().getAudio().getGoodPerformance() != null) {
                    
                    file = GatClientUtility.getBaseCourseFolderPath() 
                            + Constants.FORWARD_SLASH 
                            + scenario.getResources().getObserverControls().getAudio().getGoodPerformance();
                }
                
                if(file == null) {
                    audioPlaybackElement = JsniUtility.playAudio("audio/beep-good.mp3"); //play default good audio
                    
                } else {
                    
                    //play good audio within course folder
                    SharedResources.getInstance().getRpcService().getAssociatedCourseImage(
                            GatClientUtility.getUserName(), file, 
                            new AsyncCallback<FetchContentAddressResult>() {
    
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.severe("An error occured while getting preview link. " + caught.getClass().getName() + ": " + caught.getMessage());
                        }
    
                        @Override
                        public void onSuccess(FetchContentAddressResult result) {
                            if (result.isSuccess() && result.getContentURL() != null) {
                                audioPlaybackElement = JsniUtility.playAudio(result.getContentURL());
                                
                            } else {
                                logger.severe("Failed to get preview link. " + result.getErrorMsg());
                            }
                        }
                        
                    });
                }
            }
        });
        
        pauseGoodAudioButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                pauseAudioPlayback();
            }
        });
        
        deleteGoodAudioButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setGoodAudio(null);
            }
        });
        
        poorAudioButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                isSelectingGoodAudio = false;
                mp3FileDialog.center();
                poorAudioTooltip.hide();
            }
        });
        
        playPoorAudioButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                pauseAudioPlayback();
                
                String file = null;
                
                Scenario scenario = getScenario();
                if(scenario != null 
                        && scenario.getResources() != null 
                        && scenario.getResources().getObserverControls() != null 
                        && scenario.getResources().getObserverControls().getAudio() != null
                        && scenario.getResources().getObserverControls().getAudio().getPoorPerformance() != null) {
                    
                    file = GatClientUtility.getBaseCourseFolderPath() 
                            + Constants.FORWARD_SLASH 
                            + scenario.getResources().getObserverControls().getAudio().getPoorPerformance();
                }
                
                if(file == null) {
                    audioPlaybackElement = JsniUtility.playAudio("audio/beep-poor.mp3"); //play default poor audio
                    
                } else {
                    
                    //play poor audio within course folder
                    SharedResources.getInstance().getRpcService().getAssociatedCourseImage(
                            GatClientUtility.getUserName(), file, 
                            new AsyncCallback<FetchContentAddressResult>() {
    
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.severe("An error occured while getting preview link. " + caught.getClass().getName() + ": " + caught.getMessage());
                        }
    
                        @Override
                        public void onSuccess(FetchContentAddressResult result) {
                            if (result.isSuccess() && result.getContentURL() != null) {
                                audioPlaybackElement = JsniUtility.playAudio(result.getContentURL());
                                
                            } else {
                                logger.severe("Failed to get preview link. " + result.getErrorMsg());
                            }
                        }
                        
                    });
                }
            }
        });
        
        pausePoorAudioButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                pauseAudioPlayback();
            }
        });
        
        deletePoorAudioButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setPoorAudio(null);
            }
        });
        
        mp3FileDialog.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null){
                    if(isSelectingGoodAudio) {
                        setGoodAudio(event.getValue());
                        
                    } else {
                        setPoorAudio(event.getValue());
                    }
                }
            }
        });
        
        gameMasterHeader.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                gameMasterCollapse.toggle();
            }
        }, ClickEvent.getType());
        
        missionDetailsHeader.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                missionDetailsCollapse.toggle();
            }
        }, ClickEvent.getType());

        setReadonly(ScenarioClientUtility.isReadOnly());
    }
    
    /**
     * Return the Scenario's Mission in order to get/set it's values.
     * 
     * @return the Scenario's Mission object.  It will be created if it is null.
     */
    private generated.dkf.Scenario.Mission getMission(){
        generated.dkf.Scenario.Mission mission = ScenarioClientUtility.getScenario().getMission();
        if(mission == null){
            mission = new generated.dkf.Scenario.Mission();
            ScenarioClientUtility.getScenario().setMission(mission);
        }
        
        return mission;
    }
    
    /**
     * Sets the audio file used to represent good performance assessments to Game Master observers
     * 
     * @param selectedFile the file to use. Can be null to use the default.
     */
    private void setGoodAudio(String selectedFile) {
        
        if (StringUtils.isNotBlank(selectedFile)) {
            goodAudioButton.setText(selectedFile);
            deleteGoodAudioButton.setVisible(true);
            
        } else {
            goodAudioButton.setText("Default");
            deleteGoodAudioButton.setVisible(false);
        }
        
        Resources resources = ScenarioClientUtility.getScenario().getResources();
        
        if(selectedFile == null 
                && (resources.getObserverControls() == null 
                    || resources.getObserverControls().getAudio() == null
                    || resources.getObserverControls().getAudio().getPoorPerformance() == null)) {
            
            resources.setObserverControls(null);
            return;
        }
        
        if(resources.getObserverControls() == null) {
            resources.setObserverControls(new ObserverControls());
        }
        
        if(resources.getObserverControls().getAudio() == null) {
            resources.getObserverControls().setAudio(new ObserverControls.Audio());
        }
        
        resources.getObserverControls().getAudio().setGoodPerformance(selectedFile);
    }
    
    /**
     * Sets the audio file used to represent poor performance assessments to Game Master observers
     * 
     * @param selectedFile the file to use. Can be null to use the default.
     */
    private void setPoorAudio(String selectedFile) {
        
        if (StringUtils.isNotBlank(selectedFile)) {
            poorAudioButton.setText(selectedFile);
            deletePoorAudioButton.setVisible(true);
            
        } else {
            poorAudioButton.setText("Default");
            deletePoorAudioButton.setVisible(false);
        }
        
        Resources resources = ScenarioClientUtility.getScenario().getResources();
        
        if(selectedFile == null 
                && (resources.getObserverControls() == null 
                    || resources.getObserverControls().getAudio() == null
                    || resources.getObserverControls().getAudio().getGoodPerformance() == null)) {
            
            resources.setObserverControls(null);
            return;
        }
        
        if(resources.getObserverControls() == null) {
            resources.setObserverControls(new ObserverControls());
        }
        
        if(resources.getObserverControls().getAudio() == null) {
            resources.getObserverControls().setAudio(new ObserverControls.Audio());
        }
        
        resources.getObserverControls().getAudio().setPoorPerformance(selectedFile);
    }

    /**
     * Populates the panel using the data within the given {@link Scenario}.
     * 
     * @param scenario the data object that will be used to populate the panel.
     */
    public void edit(Scenario scenario) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + scenario + ")");
        }

        if (scenario == null) {
            throw new IllegalArgumentException("The 'scenario' parameter can't be null");
        }

        if (StringUtils.isNotBlank(scenario.getDescription())) {
            descriptionTextArea.setValue(scenario.getDescription());
        }
        
        if(scenario.getMission() != null){
            
            generated.dkf.Scenario.Mission mission = scenario.getMission();
            missionDetailsSourceTextbox.setValue(mission.getSource());
            missionDetailsMETTextbox.setValue(mission.getMET());
            missionDetailsConditionTextbox.setValue(mission.getCondition());
            missionDetailsGoalsTextbox.setValue(mission.getGoals());
            missionDetailsROETextbox.setValue(mission.getGoals());
            missionDetailsSituationTextbox.setValue(mission.getSituation());
            missionDetailsTaskTextbox.setValue(mission.getTask());
            missionDetailsThreatWarningTextbox.setValue(mission.getThreatWarning());
            missionDetailsWeaponPostureTextbox.setValue(mission.getWeaponPosture());
            missionDetailsWeaponStatusTextbox.setValue(mission.getWeaponStatus());
            
            // it may have values so show the panel by default
            missionDetailsCollapse.show();
        }

        if (scenario.getResources() != null && scenario.getResources().getScenarioControls() != null
                && scenario.getResources().getScenarioControls().getPreventManualStop() != null) {
            manualStopCheck.setValue(false);
        }
        
        String goodAudio = null;
        String poorAudio = null;
        
        if(scenario.getResources() != null 
                && scenario.getResources().getObserverControls() != null 
                && scenario.getResources().getObserverControls().getAudio() != null) {
            
            ObserverControls.Audio audio = scenario.getResources().getObserverControls().getAudio();
            
            goodAudio = audio.getGoodPerformance();
            poorAudio = audio.getPoorPerformance();
        }
        
        setGoodAudio(goodAudio);
        setPoorAudio(poorAudio);

        setReadonly(ScenarioClientUtility.isReadOnly());
    }

    /**
     * Retrieves the scenario instance.
     * 
     * @return the {@link Scenario}
     */
    private Scenario getScenario() {
        return ScenarioClientUtility.getScenario();
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        descriptionTextArea.setEditable(!isReadonly);
        manualStopCheck.setEnabled(!isReadonly);
        goodAudioButton.setEnabled(!isReadonly);
        poorAudioButton.setEnabled(!isReadonly);
        missionDetailsSourceTextbox.setEnabled(!isReadonly);
        missionDetailsMETTextbox.setEnabled(!isReadonly);
        missionDetailsGoalsTextbox.setEnabled(!isReadonly);
        missionDetailsROETextbox.setEnabled(!isReadonly);
        missionDetailsSituationTextbox.setEnabled(!isReadonly);
        missionDetailsTaskTextbox.setEnabled(!isReadonly);
        missionDetailsThreatWarningTextbox.setEnabled(!isReadonly);
        missionDetailsWeaponPostureTextbox.setEnabled(!isReadonly);
        missionDetailsWeaponStatusTextbox.setEnabled(!isReadonly);
        missionDetailsConditionTextbox.setEnabled(!isReadonly);
    }
    
    /**
     * Pauses any good/poor performance audio that is currently being played
     */
    private void pauseAudioPlayback() {
        
        if(audioPlaybackElement != null) {
            audioPlaybackElement.pause();
        }
    }
}