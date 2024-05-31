/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;

/**
 * Contains all images needed by the GAT.
 *
 * @author cragusa
 */
public interface GatClientBundle extends ClientBundle {

    /**  The one and only GatResourceBundle. */
    public static final GatClientBundle INSTANCE = GWT.create(GatClientBundle.class);
    public static final String IMAGES_PATH = "mil/arl/gift/common/images/";
    //TODO: To avoid confusion resources,
    //should be moved from beneath the war folder

    // CSS
    /**
     * Css.
     *
     * @return the css resource
     */
    @NotStrict
    @Source("../war/GiftAuthoringTool.css")
    public CssResource css();

    // Images
    //Course and DKF File Status
    /**
     * Check.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "check.png")
    ImageResource check();

    /**
     * Warn.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "warning.png")
    ImageResource warn();

    /**
     * Invalid.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "invalid.png")
    ImageResource invalid();

    //Dialog Boxes
    /**
     * Info_image.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "say.png")
    ImageResource info_image();

    /**
     * Alert_image.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "notify.png")
    ImageResource alert_image();

    /**
     * Warn_image.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "warn-32.png")
    ImageResource warn_image();

    /**
     * Error_image.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "stop.png")
    ImageResource error_image();

    //Transition Icons
    /**
     * Aar_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/aar-2.png")
    ImageResource aar_icon();

    /**
     * Mbp_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/mbp.png")
    ImageResource mbp_icon();

    /**
     * Guidance_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/guidance.png")
    ImageResource guidance_icon();

    /**
     * Training_app_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/ta.png")
    ImageResource training_app_icon();

    /**
     * Survey_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/survey.png")
    ImageResource survey_icon();

    /**
     * Lesson_material_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/lm.png")
    ImageResource lesson_material_icon();

    //Course Transition Node Status
    /**
     * Node_valid_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/check.png")
    ImageResource node_valid_icon();

    /**
     * Node_invalid_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "transitions/no.png")
    ImageResource node_invalid_icon();

    /**
     * Task_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "dkf/task.png")
    ImageResource task_icon();

    /**
     * Concept_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "dkf/concept.png")
    ImageResource concept_icon();

    /**
     * Condition_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "dkf/condition.png")
    ImageResource condition_icon();

    /**
     * Add_concept_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "dkf/add_concept.png")
    ImageResource add_concept_icon();

    /**
     * Add_condition_icon.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "dkf/add_condition.png")
    ImageResource add_condition_icon();

    /**
     * Loading_animation.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "loading.gif")
    ImageResource loading_animation();

    /**
     * Add_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "add.png")
    ImageResource add_image();

    /**
     * Edit_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "edit.png")
    ImageResource edit_image();

    /**
     * Add_child_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "add_child.png")
    ImageResource add_child_image();

    /**
     * Course_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/course_editor.png")
    ImageResource course_editor_icon();

    /**
     * DKF_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/dkf_editor.png")
    ImageResource dkf_editor_icon();

    /**
     * Conversation_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/conversation_editor.png")
    ImageResource conversation_editor_icon();

    /**
     * Sensor_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/sensor_editor.png")
    ImageResource sensor_editor_icon();

    /**
     * Learner_state_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/learner_state_editor.png")
    ImageResource learner_state_editor_icon();

    /**
     * Metadata_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/metadata_editor.png")
    ImageResource metadata_editor_icon();

    /**
     * Pedagogy_editor_image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "icons/pedagogy_editor.png")
    ImageResource pedagogy_editor_icon();

    /**
     * Cancel_image.
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "cancel.png")
    ImageResource cancel_image();

    /**
     * Course_default_books image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "course_tiles/course_default_books.jpg")
    ImageResource course_default();


    /**
     * Image_not_found image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "course_tiles/image_not_found.png")
    ImageResource image_not_found();

    /**
     * Save image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "save.png")
    ImageResource save_enabled();

    /**
     * Saved_disabled image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "save_disabled.png")
    ImageResource save_disabled();

    /**
     * Clean image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "clean.png")
    ImageResource clean();

    /**
     * Clean_disabled image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "clean_disabled.png")
    ImageResource clean_disabled();

    /**
     * trashcan image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "trashcan.png")
    ImageResource trashcan();

    /**
     * trashcan_disabled image
     *
     * @return the image resource
     */
    @Source(IMAGES_PATH + "trashcan_disabled.png")
    ImageResource trashcan_disabled();

	/**
	 * download image
	 *
	 * @return the image resource
	 */
	@Source(IMAGES_PATH + "download.png")
	ImageResource download();
}
