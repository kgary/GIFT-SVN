/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

/**
 * This is the common interface for proxy related content (files and folders).
 * 
 * @author mhoffman
 *
 */
public interface ContentProxyInterface {

    /**
     * Return the full name of the content (e.g. folder).  
     * For folder this will include any path information
     * For files this will be the absolute path.
     * 
     * @return the full name of the content (e.g. C:/GIFT_2015_1/Domain/ClearBldg, "My Workspaces/HemorrhageControl", "my.course.xml")
     */
    public String getName();
    
    /**
     * Return the file identifier.<br/>
     * local disk = absolute path of the file, "C:/work/GIFT/my.course.xml"<br/>
     * server (Nuxeo) = workspace path of the file, "my workspace/clearBldg/my.course.xml"
     * 
     * @return the file name
     */
    public String getFileId();
    
    /**
     * Whether the content is a directory or a file.
     * 
     * @return true if the content is a directory
     */
    public boolean isDirectory();
    
    /**
     * Whether the content is a java.io.File and not a document in Nuxeo or other content management system. 
     * @return true if the file behind the proxy is a java.io.File
     */
    public boolean isLocalFile();
}
