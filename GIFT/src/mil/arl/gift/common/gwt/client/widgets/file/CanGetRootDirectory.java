/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

/**
 * An object capable of getting some root directory from the server to use in displaying a hierarchy of files. The root directory will act as 
 * the top of the hierarchy, above which files cannot be accessed.
 * 
 * @author nroberts
 */
public interface CanGetRootDirectory {

	/**
	 * Gets the root directory from the server. In most GWT applications, this will involve some sort of RPC or dispatch call. The callback given 
	 * should be invoked after the result of the call is received by the client.
	 * 
	 * @param callback the callback invoked after the client receives the result of the call
	 */
	public void getRootDirectory(GetRootDirectoryCallback callback);
}
