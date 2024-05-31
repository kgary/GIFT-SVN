/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

/**
 * Contains help strings for the GAT.
 *
 * @author mhoffman
 *
 */
public final class CourseHelp {

    public static final String INFORMATION_AS_TEXT_COURSE_OBJECT_HELP =
            "<html><b>Information as text</b><br>Useful for displaying content to the learner from authored text.  The learner's actions are normally not assessed at this time.</html>";

    public static final String INFORMATION_FROM_FILE_COURSE_OBJECT_HELP =
            "<html><b>Information from local webpage</b><br>Useful for displaying content to the learner from a webpage in the course folder.  The learner's actions are normally not assessed at this time.</html>";

    public static final String INFORMATION_FROM_WEB_COURSE_OBJECT_HELP =
            "<html><b>Information from web</b><br>Useful for displaying content to the learner from a website.  The learner's actions are normally not assessed at this time.</html>";

    public static final String AUTHORED_BRANCH_COURSE_OBJECT_HELP =
            "<html><b>Authored Branch</b><br>Useful for authoring branching paths to the course flow. Learner's can be assigned different paths based on criteria.</html>";

    public static final String STRUCTURED_REVIEW_COURSE_OBJECT_HELP =
            "<html><b>Structured Review</b><br>Useful for displaying survey/text responses and overall real-time assessment scores to the learner.  The contents of the review are automatically collected from experiences in the course.</html>";

    public static final String SURVEY_COURSE_OBJECT_HELP =
            "<html><b>Survey/Test</b><br>Useful for collecting information about the learner such as an assessment on knowledge.  The results can be used by GIFT to alter the course experience.</html>";

    public static final String AUTOTUTOR_COURSE_OBJECT_HELP =
            "<html><b>AutoTutor Conversation</b><br>Useful for presenting an AutoTutor script to elicit and assess the learner's knowledge through a free response conversation.</html>";

    public static final String CONVERSATION_TREE_COURSE_OBJECT_HELP =
            "<html><b>Conversation Tree</b><br>Useful for presenting a conversation to elicit and assess the learner's knowledge through a choose your path style conversation.  The results can be used by GIFT to alter the course experience.</html>";

    public static final String QUESTION_BANK_COURSE_OBJECT_HELP =
            "<html><b>Question Bank</b><br>Useful for assessing the learner's knowledge on course concepts by dynamically selecting questions to present for each learner.  The results can be used by GIFT to alter the course experience.</html>";

    public static final String MEDIA_COLLECTION_COURSE_OBJECT_HELP =
            "<html><b>Media Collection</b><br>Provides a learner guided mechanism to deliver multimedia content.  Normally used to display lesson material for the course.</html>";

    public static final String IMAGE_COURSE_OBJECT_HELP =
            "<html><b>Image</b><br>Display an image to the learner.  Normally used to display lesson material for the course.</html>";

    public static final String PDF_COURSE_OBJECT_HELP =
            "<html><b>PDF</b><br>Display a PDF to the learner.  Normally used to display lesson material for the course.</html>";

    public static final String YOUTUBE_COURSE_OBJECT_HELP =
            "<html><b>YouTube</b><br>Display a YouTube video to the learner.  Normally used to display lesson material for the course.</html>";
    
    public static final String VIDEO_COURSE_OBJECT_HELP =
            "<html><b>Local Video</b><br>Display a local video to the learner.  Normally used to display lesson material for the course.</html>";

    public static final String SLIDE_SHOW_COURSE_OBJECT_HELP =
            "<html><b>Slide Show</b><br>"+
            "Converts a PowerPoint show file (.pps) into images that are presented in a webpage to the learner.  Normally used to display lesson material for the course.</html>";

    public static final String POWERPOINT_COURSE_OBJECT_HELP =
            "<html><b>PowerPoint</b><br>"+
            "Used to display a PowerPoint show file (.pps,.ppsx,.ppsm) in Microsoft PowerPoint.  This will require the learner to have PowerPoint installed on their comptuer.<br>"+
                    "<i>Suggestion:</i> Take a look at the Slide Show course object if you don't want to use Microsoft PowerPoint on the learner's computer.</html>";

    public static final String VBS_COURSE_OBJECT_HELP =
            "<html><b>Virtual Battle Space (VBS)</b><br>Assess the learner's skill in a VBS scenario. This will require the learner to have VBS installed on their comptuer.</html>";

    public static final String TC3_COURSE_OBJECT_HELP =
            "<html><b>TC3</b><br>Assess the learner's skill in a TC3 scenario. This will require the learner to have TC3 installed on their computer.</html>";

    public static final String ARES_COURSE_OBJECT_HELP =
            "<html><b>ARES</b><br>Assess the learner's skill in an ARES scenario.  This will require the learner to have ARES installed on their comptuer.</html>";

    public static final String UNITY_COURSE_OBJECT_HELP =
    		"<html><b>Unity WebGL</b><br>Assess the learner's skill while in a Unity scenario within a web browser. This will require the learner to use a web browser capable of rendering the Unity WebGL player such as FireFox, Chrome, or Microsoft Edge.</html>";

    public static final String HAVEN_COURSE_OBJECT_HELP =
            "<html><b>SE Sandbox</b><br>Assess the learner's skill in an SE Sandbox scenario. This will required the learner have SE Sandbox running on their computer.</html>";

    public static final String RIDE_COURSE_OBJECT_HELP =
            "<html><b>RIDE</b><br>Assess the learner's skill in a RIDE scenario. This will required the learner have RIDE running on their computer.</html>";
    
    public static final String MOBILE_APP_COURSE_OBJECT_HELP =
            "<html><b>Mobile Events</b><br>Assess the learner's skill within a real-life scenario monitored by a learner's mobile device. "
            + "This enables two-way commnunication between the mobile device and GIFT's webpages so that the mobile device can send GPS "
            + "tracking data and so that GIFT can tell the device when to vibrate or display notifications. This will require the learner "
            + "to run the course GIFT through the GIFT Mobile App.</html>";

    public static final String VR_ENGAGE_COURSE_OBJECT_HELP =
            "<html><b>VR-Engage</b><br>Assess the learner's skill in a VR-Engage scenario. This will require the learner to have VR-Engage installed on their comptuer.</html>";

    public static final String UNITY_DESKTOP_COURSE_OBJECT_HELP =
            "<html><b>Unity</b><br>Assess the learner's skill in a Unity scenario. This will require the learner to have a Unity application installed on their computer.</html>";

    public static final String DE_TESTBED_COURSE_OBJECT_HELP =
            "<html><b>DE Testbed</b><br>Assess the learner's skill in a DE Testbed scenario.  This will require the learner to have DE Testbed installed on their computer.</html>";

    public static final String EXAMPLE_APP_COURSE_OBJECT_HELP =
            "<html><b>Demo Application</b><br>Run's the Simple Example Demo Training Application meant to accompany the GIFT Software Development guide.</html>";

    public static final String ADAPTIVE_COURSE_OBJECT_HELP =
            "<html><b>Adaptive Courseflow</b><br>"+
            "Separates and manages knowledge comprehension and skill into a five part experience that delivers content based on learner state attributes and provides remediation.</br>"+
            "Authoring Rule phase content is now optional and remediation content can be separated from Rule/Example phase content if desired.</html>";

    public static final String LTI_COURSE_OBJECT_HELP = "<html><b>Learning Tools Interoperability (LTI)</b><br>Useful for integrating remote learning tools, such as applications, into your course."
            + "<br>The LTI component is based on the IMS Global Learning Tools Interoperability version 1.1.1 specifications which can be found at https://www.imsglobal.org/specs/ltiv1p1p1/implementation-guide.</html>";
}
