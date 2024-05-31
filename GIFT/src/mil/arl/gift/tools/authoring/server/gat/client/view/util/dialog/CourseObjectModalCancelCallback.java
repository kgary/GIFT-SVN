/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

/**
 * Callback interface used to signal when the course object modal has 
 * @author nblomberg
 *
 */
public interface CourseObjectModalCancelCallback {
    
    /** 
     * Callback used when the modal is cancelled (either by clicking the button or by an error).
     * */
    void onCancelModal(boolean removeSelection);
}
