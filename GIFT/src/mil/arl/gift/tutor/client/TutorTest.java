/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import generated.dkf.LearnerActionEnumType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.common.DisplayTextAction;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.enums.TextFeedbackDisplayEnum;
import mil.arl.gift.tutor.client.widgets.AvatarContainer;
import mil.arl.gift.tutor.shared.UserAction;
import mil.arl.gift.tutor.shared.UserActionIconEnum;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.AvatarContainerWidgetProperties;
import mil.arl.gift.tutor.shared.properties.FeedbackWidgetProperties;
import mil.arl.gift.tutor.shared.properties.UserActionWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * The entry point for the Tutor Test Page which, currently presents some buttons that when
 * selected will test the various forms of feedback delivery (i.e. text, audio, avatar)
 */
public class TutorTest implements EntryPoint {

    private FlowPanel feedbackPanel = new FlowPanel();
    
    private static final String DEFAULT_CHARACTER_PATH = "avatarResources/VirtualHuman/DefaultAvatar.html";
    
    private static final String AUDIO_TEXT = "You are part of a unit, you need to stay close to them";
    private static final String AUDIO_MP3 = "audio/01_you_are_part_of_a_unit_you_need_to_stay_close_to_them.mp3";
    private static final String AUDIO_OGG ="audio/01_you_are_part_of_a_unit_you_need_to_stay_close_to_them.ogg";

    /** Test actions */
    private static final String AUDIO_ONLY = "Play Audio";
    private static final String AUDIO_ONLY_DESC = "Plays an audio clip.";
    private static final String BEEP = "Display Text - Beep";
    private static final String BEEP_DESC = "Displays text accompanied by a beep sound";
    private static final String AVATAR_ONLY = "Display character";
    private static final String AVATAR_ONLY_DESC = "Displays the character (if not already displayed)";
    private static final String NO_EFFECTS  = "Display Text - No Effect";
    private static final String NO_EFFECTS_DESC  = "Displays text with no flash or beep effects.";
    private static final String FLASH_TEXT  = "Display Text - Flash Text";
    private static final String FLASH_TEXT_DESC  = "Displays flashing text.";
    private static final String ALL_EFFECTS = "Display Text - All Effects";
    private static final String ALL_EFFECTS_DESC = "Displays text with a flash and beep effect.";
    private static final String AUDIO_WITH_TEXT = "Play Audio - With Text";
    private static final String AUDIO_WITH_TEXT_DESC = "Plays an audio clip and displays text.";
    private static final String AVATAR_ACTION = "Display character with Action";
    private static final String AVATAR_ACTION_DESC = "Displays the character and runs a scripted action. The character will speak the text displayed. This will only work with the GIFT Default Media Semantics character, NOT Virtual Human Character.";
    private static final String AVATAR_AUDIO_TEXT = "Display character with Text to Speech";
    private static final String AVATAR_AUDIO_TEXT_DESC = "Displays the character as well as text that is spoken by the character.";
    
    private static final generated.dkf.LearnerAction AUDIO_ONLY_LA;
    private static final generated.dkf.LearnerAction BEEP_LA;
    private static final generated.dkf.LearnerAction AVATAR_ONLY_LA;
    private static final generated.dkf.LearnerAction NO_EFFECTS_LA;
    private static final generated.dkf.LearnerAction FLASH_TEXT_LA;
    private static final generated.dkf.LearnerAction ALL_EFFECTS_LA;
    private static final generated.dkf.LearnerAction AUDIO_WITH_TEXT_LA;
    private static final generated.dkf.LearnerAction AVATAR_ACTION_LA;
    private static final generated.dkf.LearnerAction AVATAR_AUDIO_TEXT_LA;
    static{
        AUDIO_ONLY_LA = new generated.dkf.LearnerAction();
        AUDIO_ONLY_LA.setDisplayName(AUDIO_ONLY);
        AUDIO_ONLY_LA.setDescription(AUDIO_ONLY_DESC);
        AUDIO_ONLY_LA.setType(LearnerActionEnumType.RADIO);
        
        BEEP_LA = new generated.dkf.LearnerAction();
        BEEP_LA.setDisplayName(BEEP);
        BEEP_LA.setDescription(BEEP_DESC);
        BEEP_LA.setType(LearnerActionEnumType.RADIO);
        
        AVATAR_ONLY_LA = new generated.dkf.LearnerAction();
        AVATAR_ONLY_LA.setDisplayName(AVATAR_ONLY);
        AVATAR_ONLY_LA.setDescription(AVATAR_ONLY_DESC);
        AVATAR_ONLY_LA.setType(LearnerActionEnumType.RADIO);
        
        NO_EFFECTS_LA = new generated.dkf.LearnerAction();
        NO_EFFECTS_LA.setDisplayName(NO_EFFECTS);
        NO_EFFECTS_LA.setDescription(NO_EFFECTS_DESC);
        NO_EFFECTS_LA.setType(LearnerActionEnumType.RADIO);
        
        FLASH_TEXT_LA = new generated.dkf.LearnerAction();
        FLASH_TEXT_LA.setDisplayName(FLASH_TEXT);
        FLASH_TEXT_LA.setDescription(FLASH_TEXT_DESC);
        FLASH_TEXT_LA.setType(LearnerActionEnumType.RADIO);
        
        ALL_EFFECTS_LA = new generated.dkf.LearnerAction();
        ALL_EFFECTS_LA.setDisplayName(ALL_EFFECTS);
        ALL_EFFECTS_LA.setDescription(ALL_EFFECTS_DESC);
        ALL_EFFECTS_LA.setType(LearnerActionEnumType.RADIO);
        
        AUDIO_WITH_TEXT_LA = new generated.dkf.LearnerAction();
        AUDIO_WITH_TEXT_LA.setDisplayName(AUDIO_WITH_TEXT);
        AUDIO_WITH_TEXT_LA.setDescription(AUDIO_WITH_TEXT_DESC);
        AUDIO_WITH_TEXT_LA.setType(LearnerActionEnumType.RADIO);
        
        AVATAR_ACTION_LA = new generated.dkf.LearnerAction();
        AVATAR_ACTION_LA.setDisplayName(AVATAR_ACTION);
        AVATAR_ACTION_LA.setDescription(AVATAR_ACTION_DESC);
        AVATAR_ACTION_LA.setType(LearnerActionEnumType.RADIO);
        
        AVATAR_AUDIO_TEXT_LA = new generated.dkf.LearnerAction();
        AVATAR_AUDIO_TEXT_LA.setDisplayName(AVATAR_AUDIO_TEXT);
        AVATAR_AUDIO_TEXT_LA.setDescription(AVATAR_AUDIO_TEXT_DESC);
        AVATAR_AUDIO_TEXT_LA.setType(LearnerActionEnumType.RADIO);
    }
    
    private static Logger logger = Logger.getLogger(TutorTest.class.getName());
    
    /** Instance of the TutorTest widget */
    private static TutorTest instance = null;
    
    @Override
    public void onModuleLoad() {

        instance = this;
        
        //
        // Build the panel
        //
        RootLayoutPanel root = RootLayoutPanel.get();
        final FlowPanel rootPanel = new FlowPanel();
        rootPanel.add(feedbackPanel);
        
        WidgetInstance widgetInstance = new WidgetInstance(WidgetTypeEnum.AVATAR_CONTAINER_WIDGET, new WidgetProperties());
        AvatarContainerWidgetProperties.setWidgetType(widgetInstance.getWidgetProperties(), WidgetTypeEnum.USER_ACTION_WIDGET);
        widgetInstance.getWidgetProperties().setIsTutorTest(true);
        
        ArrayList<UserAction> actions = new ArrayList<UserAction>();
        actions.add(new UserAction(NO_EFFECTS_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(FLASH_TEXT_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(BEEP_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(ALL_EFFECTS_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(AUDIO_ONLY_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(AUDIO_WITH_TEXT_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(AVATAR_ONLY_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(AVATAR_AUDIO_TEXT_LA, UserActionIconEnum.TUTOR_ME_ICON));
        actions.add(new UserAction(AVATAR_ACTION_LA, UserActionIconEnum.TUTOR_ME_ICON));
        
        UserActionWidgetProperties.setUserActions(widgetInstance.getWidgetProperties(), actions);
        
        final AvatarContainer feedbackWidget = new AvatarContainer(widgetInstance);
        
        feedbackWidget.getElement().getStyle().setProperty("position", "static");
        feedbackPanel.add(feedbackWidget);

        root.add(rootPanel);
        root.addStyleName("documentPanel");
        exposeNativeFunctions();
                
        // In GIFT 2019-1 if a character is going to be used in the feedback widget then the character is
        // pre-loaded prior to any action the character might need to take.  Therefore making sure 
        // the character loads now
        handleUserAction(new UserAction(AVATAR_ONLY_LA, UserActionIconEnum.TUTOR_ME_ICON));
    }
    
    public static TutorTest getInstance() {
        return instance;
    }
    
    public void handleUserAction(UserAction action) {

        logger.info("Received action - "+action);

        WidgetProperties properties = new WidgetProperties();

        List<TutorUserInterfaceFeedback> feedback = new ArrayList<TutorUserInterfaceFeedback>();
        
        // the feedback to display
        DisplayTextAction textAction = null;
        generated.dkf.InTutor deliverySettings = null;
        TutorUserInterfaceFeedback tuiFeedback = null;
        
        if(action.getDisplayString().startsWith("Display Text")) {
            
            deliverySettings = new generated.dkf.InTutor();
            
            switch(action.getDisplayString()) {
                case NO_EFFECTS:
                    textAction = new DisplayTextAction("This is some feedback that has no display effects.");
                    break;
                    
                case FLASH_TEXT:
                    textAction = new DisplayTextAction("This is some feedback that should flash.");
                    deliverySettings.setTextEnhancement(TextFeedbackDisplayEnum.FLASH_ONLY.getName());
                    break;
                    
                case BEEP:
                    textAction = new DisplayTextAction("This is some feedback that should be accompanied with a beep sound.");
                    deliverySettings.setTextEnhancement(TextFeedbackDisplayEnum.BEEP_ONLY.getName());
                    break;
                    
                case ALL_EFFECTS:
                    textAction = new DisplayTextAction("This is some feedback that should flash and be accompanied with a beep sound.");
                    deliverySettings.setTextEnhancement(TextFeedbackDisplayEnum.BEEP_AND_FLASH.getName());
                    break;
                    
                default: 
                    break;
            }
        }
        
        if(textAction != null) {
            // the text presentation to use
            logger.info("Found text action of "+textAction);
            textAction.setDeliverySettings(deliverySettings);
            tuiFeedback = new TutorUserInterfaceFeedback(textAction, null, null, null, null);
            
        } else {
            switch(action.getDisplayString()) {
                case AUDIO_ONLY:
                    tuiFeedback = new TutorUserInterfaceFeedback(null, new PlayAudioAction(AUDIO_MP3, AUDIO_OGG), null, null, null);
                    break;
                    
                case AUDIO_WITH_TEXT:
                    textAction = new DisplayTextAction(AUDIO_TEXT);
                    deliverySettings = new generated.dkf.InTutor();
                    deliverySettings.setTextEnhancement(TextFeedbackDisplayEnum.FLASH_ONLY.getName());
                    textAction.setDeliverySettings(deliverySettings);
                    tuiFeedback = new TutorUserInterfaceFeedback(textAction, new PlayAudioAction(AUDIO_MP3, AUDIO_OGG), null, null, null);
                    break;
                    
                case AVATAR_ONLY:
                    tuiFeedback = new TutorUserInterfaceFeedback(null, null, new DisplayAvatarAction(new AvatarData(DEFAULT_CHARACTER_PATH, 200, 250)), null, null);
                    break;
                    
                case AVATAR_AUDIO_TEXT:
                    String feedbackText = "This is using a remote call to Character Server. If you can hear this and see the flashing, this test is working as intended";
                    textAction = new DisplayTextAction("This is using a remote call to Character Server. If you can hear this and see the flashing, this test is working as intended");
                    deliverySettings = new generated.dkf.InTutor();
                    deliverySettings.setTextEnhancement(TextFeedbackDisplayEnum.FLASH_ONLY.getName());
                    textAction.setDeliverySettings(deliverySettings);
                    tuiFeedback = new TutorUserInterfaceFeedback(textAction, null, new DisplayTextToSpeechAvatarAction(new AvatarData(DEFAULT_CHARACTER_PATH, 200, 250), feedbackText), null, null);
                    break;
                    
                case AVATAR_ACTION:
                    textAction = new DisplayTextAction("This is a scripted action. If you can hear this (and see the flashing), this test is working as intended. This will only work with the GIFT Default Media Semantics character, NOT Virtual Human Character.");
                    deliverySettings = new generated.dkf.InTutor();
                    deliverySettings.setTextEnhancement(TextFeedbackDisplayEnum.FLASH_ONLY.getName());
                    textAction.setDeliverySettings(deliverySettings);
                    tuiFeedback = new TutorUserInterfaceFeedback(textAction, null, new DisplayScriptedAvatarAction(new AvatarData(DEFAULT_CHARACTER_PATH, 200, 250), "TestAction"), null, null);
                    break;
                    
                default:
                    break;
            }
        }
        feedback.add(tuiFeedback);
        FeedbackWidgetProperties.setFeedback(properties, feedback);
        FeedbackWidgetProperties.setHasNewFeedback(properties, true);

        displayFeedback(properties);
    }

    private void displayFeedback(WidgetProperties properties) {
        logger.info("Display feedback widget request with properties - "+properties);
        WidgetInstance instance = new WidgetInstance(WidgetTypeEnum.AVATAR_CONTAINER_WIDGET, properties);
        AvatarContainerWidgetProperties.setWidgetType(properties, WidgetTypeEnum.FEEDBACK_WIDGET);
        instance.getWidgetProperties().setIsTutorTest(true);
        AvatarContainer.getInstance().update(instance);
    }
    
    /**
     * Exposes a javascript function to be used by the GIFT Media Semantics Avatar html
     * to notify GIFT that the avatar is idle.
     */
    public native void exposeNativeFunctions()/*-{
    
       var that = this;

       $wnd.notifyGIFT = $entry(function(){
            // Since the tutor test doesn't leverage the idle logic, this method just needs to exist so the avatar script doesn't fail
        });
    }-*/;
}
