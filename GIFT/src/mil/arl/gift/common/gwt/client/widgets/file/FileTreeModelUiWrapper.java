/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import mil.arl.gift.common.io.FileTreeModel;

/**
 * @author nroberts
 *
 */
public class FileTreeModelUiWrapper {
		
	private boolean showAsHidden;
	
	private boolean showAsOpenableIfDirectory;
	
	private boolean showAsReadOnly;
	
	private FileTreeModel file;
	
	public FileTreeModelUiWrapper(FileTreeModel file){
		
		this.file = file;
		
		showAsHidden = false;
		showAsOpenableIfDirectory = true;
		showAsReadOnly = false;
	}

	public boolean getShowAsHidden() {
		return showAsHidden;
	}

	public void setShowAsHidden(boolean hidden) {
		this.showAsHidden = hidden;
	}

	public boolean getShowAsOpenableIfDirectory() {
		return showAsOpenableIfDirectory;
	}

	public void setShowAsOpenableIfDirectory(boolean openable) {
		this.showAsOpenableIfDirectory = openable;
	}

	public boolean getShownsReadOnly() {
		return showAsReadOnly;
	}

	public void setShowAsReadOnly(boolean readOnly) {
		this.showAsReadOnly = readOnly;
	}

	public FileTreeModel getFile() {
		return file;
	}
}
