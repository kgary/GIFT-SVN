/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;

/**
 * This is a wrapper around a file which abstracts where the file is actually
 * located (i.e. on local disk or on a server).
 *
 * @author mhoffman
 *
 */
public class FileProxy implements Comparable<FileProxy>, ContentProxyInterface {

    /**
     * the id of the file
     * local disk = absolute path of the file, "C:/work/GIFT/my.course.xml"
     * server (Nuxeo) = workspace path of the file, "my workspace/clearBldg/my.course.xml"
     */
    private String fileId;

    /**
     * the name of the file
     * local disk - "C:/work/GIFT/my.course.xml", filename = my.course.xml
     * server (Nuxeo) - "my workspace/clearBldg/my.course.xml", filename = my.course.xml
     */
    private String filename;

    /** content of the file (in memory) */
    private byte[] fileContent;

    /** the source of the file (e.g. {@link File}, {@link ExternalFileSystemInterface}) */
    private Object source;

    /** The list of permissions for each user granted access to this file */
    private FileProxyPermissions permissions;

    /**
     * Set the file as the source.
     *
     * @param file a local disk file.  File must exist.
     * @throws IllegalArgumentException if the file is null or is a directory
     * @throws FileNotFoundException if the file was not found
     */
    public FileProxy(File file) throws IllegalArgumentException, FileNotFoundException{

        if(file == null){
            throw new IllegalArgumentException("The file can't be null.");
        }else if(!file.exists()){
            throw new FileNotFoundException("Can't find the file named '"+file+"'.");
        }else if(file.isDirectory()){
            throw new IllegalArgumentException("The file is a directory.");
        }

        this.source = file;

        String filename = null;
        try{
            filename = file.getCanonicalPath();
        }catch(@SuppressWarnings("unused") IOException e){
            //unable to get canonical path, fall back on absolute path
            filename = file.getAbsolutePath();
        }
        setFileId(filename);

        setFileName(file.getName());

        //Since this constructor is only used in Desktop mode, all permissions are granted to all
        //users for this file.
        Map<String, SharedCoursePermissionsEnum> userToPermissions = new HashMap<>();
        if(file.canWrite()){
            userToPermissions.put(DomainOptionPermissions.ALL_USERS, SharedCoursePermissionsEnum.EDIT_COURSE);
        }else{
            userToPermissions.put(DomainOptionPermissions.ALL_USERS, SharedCoursePermissionsEnum.VIEW_COURSE);
        }
        FileProxyPermissions filePermissions = new FileProxyPermissions(userToPermissions);
        setFileProxyPermissions(filePermissions);
    }


    /**
     * Set the input stream as the source. Note: this doesn't actually read the
     * input stream into memory. To do that please call getInputStream (which
     * can be called any number of times).
     *
     * @param filename the name of the file Examples: local disk -
     *        "C:/work/GIFT/my.course.xml", filename = my.course.xml server
     *        (Nuxeo) - "my workspace/clearBldg/my.course.xml", filename =
     *        my.course.xml
     * @param fileId the id of the file Examples: local disk = absolute path of
     *        the file, "C:/work/GIFT/my.course.xml" server (Nuxeo) = workspace
     *        path of the file, "my workspace/clearBldg/my.course.xml"
     * @param externalFileSystemInterface used to retrieve the output stream for the file reference.  Can't be null.
     * @param inputStream the input stream used to get the contents of the file
     * @param permissions the permissions for each user that has access to this
     *        file proxy. Can't be null. local disk = if file is writable on
     *        local disk server (Nuxeo) = if the user accessing the file can
     *        write to the file (has write permissions).
     */
    public FileProxy(String filename, String fileId, ExternalFileSystemInterface externalFileSystemInterface, FileProxyPermissions permissions){

        if(externalFileSystemInterface == null){
            throw new NullPointerException("The inputStream can't be null.");
        }

        setFileId(fileId);
        setFileName(filename);

        this.source = externalFileSystemInterface;

        setFileProxyPermissions(permissions);
    }

    /**
     * Sets if the file proxy is readable.
     *
     * @param username the gift user to check for read permissions on this file
     * @return True if the file proxy can be read by this user. False otherwise.
     */
    public boolean canRead(String username) {

        if (username == null) {
            return false;
        }

        return permissions.hasReadPermissions(username);
    }

    /**
     * Sets if the file proxy is writable.
     *
     * @param username the gift user to check for write permissions on this file
     * @return True if the file proxy can be written to by this user. False
     *         otherwise.
     */
    public boolean canWrite(String username) {

        if(username == null){
            return false;
        }

        return permissions.hasWritePermissions(username);
    }

    /**
     * Sets the file proxy permissions.
     *
     * @param permissions the permissions for each user.  Can't be null.
     */
    private void setFileProxyPermissions(FileProxyPermissions permissions) {

        if(permissions == null){
            throw new IllegalArgumentException("The permissions can't be null.");
        }
        this.permissions = permissions;
    }

    /**
     * The permissions for each user that has access to the file proxy.
     *
     * @return the file proxy permissions.  Won't be null.
     */
    public FileProxyPermissions getFileProxyPermissions() {
        return permissions;
    }

    /**
     * Return whether this file exists or not by checking it's input stream.
     *
     * @return whether or not the file exists.
     */
    public boolean exists(){

        try(InputStream inputStream = getInputStream()){
            //TODO: does simply opening the stream suffice or do we need to try reading data?
            inputStream.markSupported();  //just doing some operation to prevent a build warning about unused inputStream variable
            return true;
        }catch(@SuppressWarnings("unused") Exception e){
            return false;
        }
    }

    private void setInput(File file) throws FileNotFoundException, IOException{

    	try(FileInputStream inputStream = new FileInputStream(file)){
    		fileContent = IOUtils.toByteArray(inputStream);
    	}
    }

    private void setInput(InputStream inputStream) throws IOException{
        fileContent = IOUtils.toByteArray(inputStream);
    }

    private void setFileId(String fileId){

        if(fileId == null){
            throw new NullPointerException("The file id can't be null.");
        }else if(fileId.isEmpty()){
            throw new IllegalArgumentException("The file id can't be null.");
        }

        this.fileId = fileId;
    }

    private void setFileName(String filename){

        if(filename == null){
            throw new NullPointerException("The filename can't be null.");
        }else if(filename.isEmpty()){
            throw new IllegalArgumentException("The filename can't be null.");
        }

        this.filename = filename;
    }

    @Override
    public String getFileId(){
        return fileId;
    }

    @Override
    public String getName() {
        return filename;
    }

    /**
     * Return a new input stream that contains the file contents. If the content
     * of the {@link FileProxy} is only read once, consider using
     * {@link #getSingleUseInputStream()} instead since it is more memory
     * efficient than this method.
     *
     * @return input stream that can be used to read the file contents
     * @throws IOException if there was a problem reading the file contents to
     *         build the input stream
     */
    public InputStream getInputStream() throws IOException{

        if(fileContent == null){
            //don't retrieve the contents of the source unless the input stream is requested

            if(source instanceof File){
                setInput((File)source);
            }else if (source instanceof ExternalFileSystemInterface) {
                InputStream iStream = ((ExternalFileSystemInterface)source).getInputStream();
                setInput(iStream);
                iStream.close(); //now that the data is in memory, close the stream
            }else {
                final String msg = "The source is of the unsupported type "
                        + (source != null ? source.getClass().getSimpleName() : "null");
                throw new IllegalStateException(msg);
            }
        }

        return new ByteArrayInputStream(fileContent);
    }

    /**
     * Returns the {@link InputStream} for the underlying file content. This is
     * more memory efficient than {@link #getInputStream()} but does not allow
     * the file content to be read more than once.
     *
     * <strong>IMPORTANT!</strong> If you require being able to read the file
     * again after calling this method, use {@link #getInputStream()}.
     *
     * @return The {@link InputStream} that produces the file's content.
     *         <strong>NOTE:</strong> The caller is responsible for closing this
     *         {@link InputStream}.
     * @throws IOException if there was a problem retrieving the
     *         {@link InputStream} of this {@link FileProxy}'s content.
     *
     */
    public InputStream getSingleUseInputStream() throws IOException {
        if (source instanceof File) {
            File file = (File) source;
            return new FileInputStream(file);
        } else if (source instanceof ExternalFileSystemInterface) {
            InputStream iStream = ((ExternalFileSystemInterface)source).getInputStream();
            return iStream;
        } else {
            final String msg = "The source is of the unsupported type "
                    + (source != null ? source.getClass().getSimpleName() : "null");
            throw new IllegalStateException(msg);
        }
    }

    /**
     * This should only be called if the file is modified after this proxy has been initialized. The
     * file content is read once and then stored. The stored content is returned each time
     * {@link #getInputStream()} is called. This will clear the stored content so that it will be
     * reinitialized the next time {@link #getInputStream()} is called.<br>
     * <br>
     * NOTE: if this proxy was initialized with an {@link InputStream}, then the content cannot be
     * refreshed because the stream was closed after the first reading.
     *
     * @throws IOException if there is a problem reading from the input stream
     */
    public void clearStoredFileContents() throws IOException {
        // if file, refresh input source
        if (source instanceof File) {
            /* the content will be populated again the next time getInputStream() is called */
            fileContent = null;
            return;
        }
    }

    @Override
    public boolean equals(Object otherFileProxy){
        return otherFileProxy != null && otherFileProxy instanceof FileProxy &&
                this.getFileId().equals(((FileProxy)otherFileProxy).getFileId());
    }

    @Override
    public int hashCode(){
        return this.getFileId().hashCode();
    }

    @Override
    public int compareTo(FileProxy otherFileProxy) {

        if(otherFileProxy == null){
            return 1;
        }

        return this.getFileId().compareTo(otherFileProxy.getFileId());
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isLocalFile() {
        return source instanceof java.io.File;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FileProxy: ");
        sb.append("id = ").append(getFileId());
        sb.append("]");
        return sb.toString();
    }

}
