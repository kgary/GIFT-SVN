/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.IOException;
import java.net.URISyntaxException;

import mil.arl.gift.common.io.DesktopFolderProxy;

/**
 * Listens to selection and de-selections of the domain export panel.
 * 
 * @author cdettmering
 */
public interface DomainSelectionListener {
	
	/**
	 * Called when an export file has been selected.
	 * 
	 * @param file The file that is selected.
	 * @throws IOException if there was a problem retrieving related files based on the file selected
	 */
	public void exportFileSelected(DesktopFolderProxy file) throws IOException, URISyntaxException;
	
	/**
	 * Called when an export file has been unselected.
	 * 
	 * @param file The file that is unselected.
	 */
	public void exportFileUnselected(DesktopFolderProxy file);
}
