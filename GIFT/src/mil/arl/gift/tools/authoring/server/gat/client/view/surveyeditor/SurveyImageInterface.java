/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.List;


/**
 * Defines an interface class to retrieve a list of images that are driven by the server.
 * 
 * @author nblomberg
 *
 */
public interface SurveyImageInterface {

    /**
     * Handler for when the survey panel is closed.
     */
    List<String> getSurveyImageList();
}
