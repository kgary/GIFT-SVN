/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

/**
 * Callback to indicate to the subscriber to show or hide.
 * 
 * @author sharrison
 */
public interface ShowHideCallback {
    /**
     * Show
     */
    public void show();

    /**
     * Hide
     */
    public void hide();
}
