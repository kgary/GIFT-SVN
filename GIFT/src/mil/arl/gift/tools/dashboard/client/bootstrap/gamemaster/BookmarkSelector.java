/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

/**
 * An interface used to allow widgets to select bookmarks
 * 
 * @author nroberts
 */
public interface BookmarkSelector{
    
    /**
     * Selects the bookmark with the given timestamp
     * 
     * @param timestamp the timestamp of the bookmark to select
     */
    public void selectBookmark(long timestamp);
}