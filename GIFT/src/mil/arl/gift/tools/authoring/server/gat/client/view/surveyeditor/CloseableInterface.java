/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;


/**
 * The CloseableInterface provides an interface to notify when a survey panel is closed.
 * 
 * @author nblomberg
 *
 */
public interface CloseableInterface {

    /**
     * Handler for when the survey panel is closed.
     */
    void close();
}
