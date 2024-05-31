/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

/**
 * Represents a GIFT folder (e.g. course folder) and provides methods to interact
 * with the folder in an abstract manner.  This way the folder could reside on disk or on a server
 * somewhere.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractFolderProxy implements ContentProxyInterface {
    
    @Override
    public boolean isDirectory(){
        return true;
    }
    
    /**
     * Updates the target file with the provided contents.
     * 
     * @param targetFile the file to update
     * @param contentFile the content to replace the target file's content with.
     * @param createBackup true to create a backup of the file before updating it with the provided content.
     * @param useAdminPrivilege Whether or not the operation should be performed with admin privileges
     * @throws IOException if there was a problem writing to the file
     * @throws SAXException if there was a problem marhsalling the file contents
     * @throws JAXBException if there was a problem marshalling the file contents against the schema
     */
    public abstract void updateFileContents(FileProxy targetFile, UnmarshalledFile contentFile, boolean createBackup, boolean useAdminPrivilege) throws IOException, SAXException, JAXBException;

    /**
     * Updates the target file with the provided contents.
     * 
     * @param targetFile the file to update
     * @param contentFile the content to replace the target file's content with
     * @param createBackup true to create a backup of the file before updating it with the provided content.
     * @param useAdminPrivilege Whether or not the operation should be performed with admin privileges
     * @throws IOException if there was a problem writing to the file
     */
    public abstract void updateFileContents(FileProxy targetFile, File contentFile, boolean createBackup, boolean useAdminPrivilege) throws IOException;

    /**
     * Create a new file in this folder with the contents of the specified file.
     * 
     * @param source the file whose content's will create a new file in this folder.  The file can't be a folder.
     * @param name the optional name to give the file being created.  If null/empty  the source file name is used.
     * @return the proxy to the created file
     * @throws IOException if there was a problem creating the file
     */
    public abstract FileProxy createFile(File source, String name) throws IOException;

    /**
     * Create a new folder in this folder.
     * 
     * @param name the name of the subfolder to create
     * @return the proxy to the created folder
     * @throws IOException if there was a problem creating the folder
     */
    public abstract AbstractFolderProxy createFolder(String name) throws IOException;
    
    /**
     * Delete the child folder with the given name.
     * 
     * @param name the name of the subfolder to delete.
     * @throws IOException if there was a problem deleting the folder
     */
    public abstract void deleteFolder(String name) throws IOException;

    /**
     * Returns a collection of files found with a certain name (e.g. "filename.txt") from the
     * starting directory down. If no filename is provided an empty list will be returned.
     * Directories are not returned.
     * 
     * @param pathsToExclude an iterable collection of paths that should be excluded from the files
     *        returned, can be null if all paths should be considered. <em>Example:</em> If
     *        pathsToExclude contains 'foo/', then the files 'foo/bar.txt' and 'foo/baz/file.txt'
     *        should not be included in the returned results.
     * @param filename the filename to search for
     * @return collection of files found, in no particular order.
     * @throws IOException if there was a problem retrieving the files
     */
    public abstract List<FileProxy> listFilesByName(Iterable<String> pathsToExclude, String filename) throws IOException;

    /**
     * Returns a collection of files found with certain extensions (e.g. "mp3") from the starting directory down.
     * If no extensions are found, all files will be in the collection.  Directories are not returned.
     * 
     * @param pathsToExclude an iterable collection of paths that should be excluded from the files returned, 
     * can be null if all paths should be considered. <em>Example:</em> If pathsToExclude contains 'foo/', 
     * then the files 'foo/bar.txt' and 'foo/baz/file.txt' should not be included in the returned results.
     * @param extensions file extensions to filter on (can be null if no filtering is needed)
     * @return collection of files found, in no particular order.
     * @throws IOException if there was a problem retrieving the files
     */
    public abstract List<FileProxy> listFiles(Iterable<String> pathsToExclude, String... extensions) throws IOException;
    
    /**
     * Returns the relative file name of the file specified to this folder.<br/>
     * Example:<br/>
     * Folder: A/B, File: A/B/C/my.txt, Result = C/my.txt<br/>
     * Folder: A/D, File: A/B/C/my.txt, Result = A/B/C/my.txt (not a common hierarchy)<br/>
     * 
     * @param fileProxy the file to get the relative file name of
     * @return the non-common part of the two paths
     * @throws IOException if there was a problem retrieving the file information
     */
    public abstract String getRelativeFileName(FileProxy fileProxy) throws IOException;
    
    /**
     * Returns the relative file name of the file specified to this folder.
     * Example:
     * Folder: A/B, File: A/B/C/my.txt, Result = C/my.txt
     * Folder: A/D, File: A/B/C/my.txt, Result = A/B/C/my.txt (not a common hierarchy)
     * 
     * @param workspaceFilePath the file to get the relative file name of,<br/>
     * local disk = absolute path of the file, "C:/work/GIFT/my.course.xml"server (Nuxeo) = workspace path of the file, "my workspace/clearBldg/my.course.xml"
     * @return the non-common part of the two paths
     * @throws IOException if there was a problem retrieving the file information
     */
    public abstract String getRelativeFileName(String workspaceFilePath) throws IOException;
    
    /**
     * Return the parent folder to the file.
     * 
     * @param fileProxy the file to get the parent folder of.  Use null to return the parent folder of this folder.
     * @return the parent folder. Can be null if there is no parent or the user doesn't have permissions to access that folder.
     * @throws IOException if there was a problem accessing the file or parent folder
     * @throws URISyntaxException if there was a problem constructing URIs to do the retrieval
     */
    public abstract AbstractFolderProxy getParentFolder(FileProxy fileProxy) throws IOException, URISyntaxException;
    
    /**
     * Return whether a file that is a descendant of this file exists.
     * This doesn't read in the file contents into memory and is preferred over FileProxy.fileExists when in
     * server mode.
     * 
     * @param filename the file name to get the file for based on using this folder as the path to that file.
     * @return true if a file exists with the file name (and path) somewhere under this folder, false otherwise.
     * @throws IOException if there was a problem (e.g. permission issue) checking if the file exists
     */
    public abstract boolean fileExists(String filename) throws IOException;
    
    /**
     * Returns the relative file to this folder based on the file name specified.<br/>
     * WARNING: in SERVER MODE this will read the entire contents of the file into memory.
     * 
     * @param filename the file name to get the file for based on using this folder as the path to that file.
     * @return the proxy representation of that file
     * @throws IOException if there was a problem retrieving the file by the name
     */
    public abstract FileProxy getRelativeFile(String filename)  throws IOException;
    
    /**
     * Return the size of this folder (and all descendant files) in MB.
     * 
     * @return the size of this folder in MB
     * @throws IOException if there was a problem determining the folder size
     */
    public abstract float getSize() throws IOException;
}
