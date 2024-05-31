/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import java.util.HashMap;

/**
 * The Class HelpMap.
 */
public class HelpMap {
	
	/**
	 * The Enum FormFieldEnum.
	 */
	public enum FormFieldEnum {
		
		/** The blur. */
		BLUR,
		
		/** The course name. */
		COURSE_NAME,
		
		/** The course version. */
		COURSE_VERSION,
		
		/** The course description. */
		COURSE_DESCRIPTION,
		
		/** The course survey context. */
		COURSE_SURVEY_CONTEXT,
		
		/** The aar name. */
		AAR_NAME,
		
		/** The aar full screen. */
		AAR_FULL_SCREEN,
		
		/** The mbp name. */
		MBP_NAME,
		
		/** The survey full screen. */
		SURVEY_FULL_SCREEN,
		
		/** The survey use survey. */
		SURVEY_USE_SURVEY,
		
		/** The survey use dkf. */
		SURVEY_USE_DKF,
		
		/** The survey choose survey. */
		SURVEY_CHOOSE_SURVEY,
		
		/** The survey choose dkf. */
		SURVEY_CHOOSE_DKF,
		
		/** The guidance name. */
		GUIDANCE_NAME,
		
		/** The guidance display time. */
		GUIDANCE_DISPLAY_TIME,
		
		/** The guidance full screen. */
		GUIDANCE_FULL_SCREEN,
		
		/** The guidance use file. */
		GUIDANCE_USE_FILE,
		
		/** The guidance use url. */
		GUIDANCE_USE_URL,
		
		/** The guidance use message. */
		GUIDANCE_USE_MESSAGE,
		
		/** The guidance file. */
		GUIDANCE_FILE,
		
		/** The guidance message. */
		GUIDANCE_MESSAGE,
		
		/** The guidance url address. */
		GUIDANCE_URL_ADDRESS,
		
		/** The guidance url message. */
		GUIDANCE_URL_MESSAGE,
		
		/** The lesson material name. */
		LESSON_MATERIAL_NAME,
		
		/** The lesson material add. */
		LESSON_MATERIAL_ADD,
		
		/** The lesson material delete. */
		LESSON_MATERIAL_DELETE,
		
		/** The lesson material media name. */
		LESSON_MATERIAL_MEDIA_NAME,
		
		/** The lesson material media uri. */
		LESSON_MATERIAL_MEDIA_URI,
		
		/** The lesson material media type. */
		LESSON_MATERIAL_MEDIA_TYPE,
		
		/** The lesson material media width. */
		LESSON_MATERIAL_MEDIA_WIDTH,
		
		/** The lesson material media height. */
		LESSON_MATERIAL_MEDIA_HEIGHT,	
		
		/** The lesson material media fullscreen. */
		LESSON_MATERIAL_MEDIA_FULLSCREEN,
		
		/** The lesson material media autoplay. */
		LESSON_MATERIAL_MEDIA_AUTOPLAY,	
		
		/** The training app name. */
		TRAINING_APP_NAME,
		
		/** The training app finished when. */
		TRAINING_APP_FINISHED_WHEN,
		
		/** The training app dkf file. */
		TRAINING_APP_DKF_FILE,
		
		/** The training app full screen. */
		TRAINING_APP_FULL_SCREEN,
		
		/** The training app disable ii. */
		TRAINING_APP_DISABLE_II,
		
		/** The training app show avatar init. */
		TRAINING_APP_SHOW_AVATAR_INIT,
		
		/** The training app avatar type. */
		TRAINING_APP_AVATAR_TYPE,
		
		/** The training app avatar. */
		TRAINING_APP_AVATAR,
		
		/** The training app avatar browse. */
		TRAINING_APP_AVATAR_BROWSE,
		
		/** The training app add interop. */
		TRAINING_APP_ADD_INTEROP,
		
		/** The training app delete interop. */
		TRAINING_APP_DELETE_INTEROP,
		
		/** The training app interop impl. */
		TRAINING_APP_INTEROP_IMPL,
		
		/** The training app arg name. */
		TRAINING_APP_ARG_NAME,
		
		/** The training app arg value. */
		TRAINING_APP_ARG_VALUE,
		
		/** The dkf name. */
		DKF_NAME,
		
		/** The dkf version. */
		DKF_VERSION,
		
		/** The dkf description. */
		DKF_DESCRIPTION,
		
		/** The dkf survey context. */
		DKF_SURVEY_CONTEXT,
		
		/** The application completed condition ideal completion duration. */
		APPLICATION_COMPLETED_CONDITION_IDEAL_COMPLETION_DURATION,
		
		/** The auto tutor condition url. */
		AUTO_TUTOR_CONDITION_URL,
		
		/** The auto tutor condition file path. */
		AUTO_TUTOR_CONDITION_FILE_PATH,
		
		/** The avoid location condition distance. */
		AVOID_LOCATION_CONDITION_DISTANCE,
		
		/** The pace count condition distance. */
        PACE_COUNT_CONDITION_DISTANCE,
        
        /** The pace count condition threshold. */
        PACE_COUNT_CONDITION_THRESHOLD,
		
		/** The corridor boundary condition buffer width percentage.*/
		CORRIDOR_BOUNDARY_CONDITION_BUFFER_WIDTH_PERCENTAGE,
		
		/** The generic condition input name. */
		GENERIC_CONDITION_INPUT_NAME,
		
		/** The generic condition input value. */
		GENERIC_CONDITION_INPUT_VALUE,
		
		/** The marksmanship precision condition expected number of shots */
		MARKSMANSHIP_PRECISION_CONDITION_EXPECTED_NUMBER_OF_SHOTS,
		
		/** The marksmanship session complete condition expected number of shots */
		MARKSMANSHIP_SESSION_COMPLETE_CONDITION_EXPECTED_NUMBER_OF_SHOTS,
		
		/** The number of shots fired condition expected number of shots */
		NUMBER_OF_SHOTS_FIRED_CONDITION_EXPECTED_NUMBER_OF_SHOTS,
		
		/** The power point dwell condition default time. */
		POWER_POINT_DWELL_CONDITION_DEFAULT_TIME,
		
		/** The power point dwell condition index. */
		POWER_POINT_DWELL_CONDITION_INDEX,
		
		/** The power point dwell condition time. */
		POWER_POINT_DWELL_CONDITION_TIME,
		
		/** The simile condition input configuration file path. */
		SIMILE_CONDITION_INPUT_CONFIGURATION_FILE_PATH,
		
		/** The simile condition input condition key. */
		SIMILE_CONDITION_INPUT_CONDITION_KEY,
	}
	
	/** The help map. */
	private static HashMap<FormFieldEnum, String> helpMap = new HashMap<FormFieldEnum, String>();
	
	/**
	 * Gets the help.
	 *
	 * @param key the key
	 * @return the help
	 */
	public static String getHelp(FormFieldEnum key) {
		return helpMap.get(key);		
	}
	
	static {
		
		//Course
		helpMap.put(FormFieldEnum.BLUR, "");
		helpMap.put(FormFieldEnum.COURSE_NAME, "Enter a name for this course. The course name will appear on the Tutor in the list of courses.");
		helpMap.put(FormFieldEnum.COURSE_VERSION, "The version of this course/file.");
		helpMap.put(FormFieldEnum.COURSE_DESCRIPTION, "A descripion of the course."); 
		
		String surveyContextHelpMsg  = "Unique key in the Survey Authoring System database used to identify the survey context."; 
		       surveyContextHelpMsg += " A survey context includes the list of surveys for a course or lesson referenced by GIFT keys.";
			   surveyContextHelpMsg += " (Refer to the Survey Authoring System documentation for more information on survey context.)";
		
		helpMap.put(FormFieldEnum.COURSE_SURVEY_CONTEXT, surveyContextHelpMsg);
				
		//Structure Review
		helpMap.put(FormFieldEnum.AAR_NAME, "Enter a name for this Structured Review course object.");
		helpMap.put(FormFieldEnum.AAR_FULL_SCREEN, "Check if you want the Structured Review to be displayed in full screen mode, otherwise leave unchecked.");
		
		//MBP
		String mbpHelpMsg = "Name for this Merrill's Branch Point transition.<br>";
		mbpHelpMsg += "(Note: If you need a fully functional Merrill's Branch Point transition, please use the legacy Course Authoring Tool.)";		
		helpMap.put(FormFieldEnum.MBP_NAME, mbpHelpMsg);	
		
		//Present Survey
		helpMap.put(FormFieldEnum.SURVEY_FULL_SCREEN, "Check if you want the survey to be displayed in full screen mode, otherwise leave unchecked.");
		helpMap.put(FormFieldEnum.SURVEY_USE_SURVEY, "Select if you want to use a traditional survey for this transition.");
		helpMap.put(FormFieldEnum.SURVEY_USE_DKF, 	"Select if you want to use an AutoTutor session as your survey for this transition.");
		helpMap.put(FormFieldEnum.SURVEY_CHOOSE_SURVEY, "Choose the survey you'd like to use.<br>(If the list is blank it may mean that you haven't yet selected a Survey Context for the course.)");
		helpMap.put(FormFieldEnum.SURVEY_CHOOSE_DKF, 	"Choose a DKF that is configured for an AutoTutor session. <br>(All DKF's are shown. It's up to you to know which one(s) support an AutoTutor Session.)");
			
		//Guidance
		helpMap.put(FormFieldEnum.GUIDANCE_NAME, "The name of this Guidance element.");		
		String displayTimeHelpMsg = "Enter the amount of time (in seconds) to display the guidance. Leave blank to allow the user unlimited time.<br>(User can click a button to advance.)";		
		helpMap.put(FormFieldEnum.GUIDANCE_DISPLAY_TIME, displayTimeHelpMsg);		
		helpMap.put(FormFieldEnum.GUIDANCE_FULL_SCREEN, "Check if you want the guidance to be displayed in full screen mode.  Leave unchecked to display guidance in the horizontally compacted tutor.");		
		helpMap.put(FormFieldEnum.GUIDANCE_USE_FILE, "Select to use an HTML file from your Domain folder as guidance content.");		
		helpMap.put(FormFieldEnum.GUIDANCE_USE_URL, "Select to use an arbitrary URL as the source of guidance content.");		
		helpMap.put(FormFieldEnum.GUIDANCE_USE_MESSAGE, "Select to enter a customized message as guidance.");		
		helpMap.put(FormFieldEnum.GUIDANCE_FILE, "Select an HTML file from your Domain folder to use as the guidance content.");		
		helpMap.put(FormFieldEnum.GUIDANCE_MESSAGE, "Enter a guidance message to display in the tutor.");		
		helpMap.put(FormFieldEnum.GUIDANCE_URL_ADDRESS, "Enter a URL specifying a web page to use for the guidance.<br>(The address must contain the URL scheme prefix such as http://.)");		
		helpMap.put(FormFieldEnum.GUIDANCE_URL_MESSAGE, "Enter an informative message (e.g. instructions) about the guidance webpage.");

		//Lesson Material
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_NAME, "Enter a name for this Lesson Material transition.");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_ADD, "Click to add a lesson material item.");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_DELETE, "Click to delete selected lesson material item.");
		
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_NAME, "Enter a name for this lesson material item.");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_URI, "Enter a URI for this media item.");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_TYPE, "Select the media type of this media item.");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_WIDTH, "Enter the width for the display of the  media item (YouTube videos only).");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_HEIGHT, "Enter the height for the display of the media item (YouTube videos only).");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_FULLSCREEN, "Select whether or not the display of the media item should cover the entire screen (YouTube videos only).");
		helpMap.put(FormFieldEnum.LESSON_MATERIAL_MEDIA_AUTOPLAY, "Select whether or not media item should be played automatically (YouTube videos only).");
		
		//Training Application
		helpMap.put(FormFieldEnum.TRAINING_APP_NAME, "Enter a name for this Training Application transition.");
		helpMap.put(FormFieldEnum.TRAINING_APP_FINISHED_WHEN, "Specifies the training application state that will cause the Training Application transition to be finished, meaning when can the next transition (in the list of transitions) start.");
		helpMap.put(FormFieldEnum.TRAINING_APP_DKF_FILE, "The relative DKF name from the Domain directory as specified by the domain properties.");
		helpMap.put(FormFieldEnum.TRAINING_APP_FULL_SCREEN, "Check if you want the training application to be displayed in full screen mode, otherwise leave unchecked.");
		helpMap.put(FormFieldEnum.TRAINING_APP_DISABLE_II, "Whether or not to disable the implementation of instructional intervention strategies (e.g. Feedback) requested by the Pedagogical model.");
		helpMap.put(FormFieldEnum.TRAINING_APP_SHOW_AVATAR_INIT, "The initial agent to show when the training application transition is started.<br>If no agent information is provided here, the default tutor agent will be used.");
		helpMap.put(FormFieldEnum.TRAINING_APP_AVATAR_TYPE, "TRAINING_APP_AVATAR_TYPE");
		helpMap.put(FormFieldEnum.TRAINING_APP_AVATAR, "The character file created by a Media Semantics Character builder project.");
		helpMap.put(FormFieldEnum.TRAINING_APP_AVATAR_BROWSE, "Click to browse for Media Semantics character file.");
		
		helpMap.put(FormFieldEnum.TRAINING_APP_ADD_INTEROP, "Click to add an Interop Implementation.");
		helpMap.put(FormFieldEnum.TRAINING_APP_DELETE_INTEROP, "Click to delete a selected Interop Implementation.");
		helpMap.put(FormFieldEnum.TRAINING_APP_INTEROP_IMPL, "Select an Interop Implementation to use for this transition.");
		helpMap.put(FormFieldEnum.TRAINING_APP_ARG_NAME, "Argument name(s) for this selected Interop Implementation.<br>(This column is read only.)");
		helpMap.put(FormFieldEnum.TRAINING_APP_ARG_VALUE, "Enter a value for: ");		
		
		//DFK
		helpMap.put(FormFieldEnum.DKF_NAME, "Enter a name for this DKF.");
		helpMap.put(FormFieldEnum.DKF_VERSION, "The version of this DKF/file.");
		helpMap.put(FormFieldEnum.DKF_DESCRIPTION, "A descripion of the DKF."); 		
	
		helpMap.put(FormFieldEnum.COURSE_SURVEY_CONTEXT, surveyContextHelpMsg);
		
		//Condition Input Help Fields
		helpMap.put(FormFieldEnum.APPLICATION_COMPLETED_CONDITION_IDEAL_COMPLETION_DURATION, "The ideal amount of simulation time (hh:mm:ss) it takes the learner to complete the lesson in the training application. Note: because this parameter focuses on simulation time rather than wall clock time, a pause in the lesson/scenario will not count against the duration.");
		helpMap.put(FormFieldEnum.AUTO_TUTOR_CONDITION_URL, "The address must contain the URL scheme prefix such as http://.");
		helpMap.put(FormFieldEnum.AUTO_TUTOR_CONDITION_FILE_PATH, "The AT SKO file that contains the parameters needed to run an AutoTutor session in GIFT.");
		helpMap.put(FormFieldEnum.AVOID_LOCATION_CONDITION_DISTANCE, "The distance (meters) from the point to use to determine if the point has been reached.");
		helpMap.put(FormFieldEnum.CORRIDOR_BOUNDARY_CONDITION_BUFFER_WIDTH_PERCENTAGE, "A buffer value (usually around a corridor) as a percent of the width of the corridor.");
		helpMap.put(FormFieldEnum.GENERIC_CONDITION_INPUT_NAME, "");
		helpMap.put(FormFieldEnum.GENERIC_CONDITION_INPUT_VALUE, "");
		helpMap.put(FormFieldEnum.MARKSMANSHIP_PRECISION_CONDITION_EXPECTED_NUMBER_OF_SHOTS, "The expected number of shots per marksmanship training session.");
		helpMap.put(FormFieldEnum.MARKSMANSHIP_SESSION_COMPLETE_CONDITION_EXPECTED_NUMBER_OF_SHOTS, "The expected number of shots per marksmanship training session.");
		helpMap.put(FormFieldEnum.NUMBER_OF_SHOTS_FIRED_CONDITION_EXPECTED_NUMBER_OF_SHOTS, "The expected number of shots per marksmanship training session.");
		helpMap.put(FormFieldEnum.POWER_POINT_DWELL_CONDITION_DEFAULT_TIME, "The default condition configuration for those slides not uniquely identified in this condition. --- Amount of time in seconds.");
		helpMap.put(FormFieldEnum.POWER_POINT_DWELL_CONDITION_INDEX, "The index of a slide in a PowerPoint show.");
		helpMap.put(FormFieldEnum.POWER_POINT_DWELL_CONDITION_TIME, "Amount of time in seconds.");
		helpMap.put(FormFieldEnum.SIMILE_CONDITION_INPUT_CONFIGURATION_FILE_PATH, "The SIMILE configuration file (relative to the Domain directory) used to configure assessment logic for this condition.");
		helpMap.put(FormFieldEnum.SIMILE_CONDITION_INPUT_CONDITION_KEY, "A unique identifier used to link SIMILE condition(s) to this DKF condition in order to map SIMILE assessment results appropriately. The key should be in the configuration file specified in the configurationFile element.");
	}	
	
	/**
	 * Instantiates a new help map.
	 */
	private HelpMap() {}	
}
