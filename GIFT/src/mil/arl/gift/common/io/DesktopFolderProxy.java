/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import mil.arl.gift.common.util.StringUtils;

/**
 * A representation of a GIFT folder (e.g. course folder) on local disk, meaning java.io.File operations
 * are acceptable.
 * 
 * @author mhoffman
 *
 */
public class DesktopFolderProxy extends AbstractFolderProxy {
    
    /** the folder representation and access logic */
    private File folder;
    
    /**
     * Set the folder
     * 
     * @param folder used to instantiate this proxy with a local concrete folder
     */
    public DesktopFolderProxy(File folder){
        
        if(folder == null || !folder.exists()){
            throw new IllegalArgumentException("The folder '"+folder+"' doesn't exist.");
        }
        
        this.folder = folder;
    }

    /**
     * Retrieves the folder associated with this proxy.
     * 
     * @return the folder file
     */
    public File getFolder(){
        return folder;
    }
    
    @Override
    public String getName(){
        return folder.getName();   
    }
    

    @Override
    public String getFileId() {
        
        try{
            return folder.getCanonicalPath();
        }catch(@SuppressWarnings("unused") IOException e){
            return folder.getAbsolutePath();
        }     
    }    

    @Override
    public void deleteFolder(String name) throws IOException {
        
        FileUtils.deleteDirectory(new File(getFileId() + File.separator + name));        
    }

    @Override
    public List<FileProxy> listFilesByName(Iterable<String> pathsToExclude, String filename) throws IOException {

        List<FileProxy> fileProxies = new ArrayList<>();
        getFilesByName(folder, fileProxies, pathsToExclude, filename);

        return fileProxies;
    }

    @Override
    public List<FileProxy> listFiles(Iterable<String> pathsToExclude, String... extensions) throws IOException {
        
        List<FileProxy> fileProxies = new ArrayList<>();
        getFilesByExtension(folder, fileProxies, pathsToExclude, extensions);
        
        return fileProxies;
    }

    /**
     * Find files with a certain name (including extension) from the starting directory down.
     * 
     * @param file where to start the search for files. If a file then no files are added to the
     *        collection.
     * @param files the list of files found
     * @param pathsToExclude an iterable collection of paths that should be excluded from the files
     *        returned, can be null if all paths should be considered. <em>Example:</em> If
     *        pathsToExclude contains 'foo/', then the files 'foo/bar.txt' and 'foo/baz/file.txt'
     *        should not be included in the returned results.
     * @param filename the filename to search for within the file folder. If null or empty, no files
     *        will be found to match.
     * @throws IOException if there was a problem retrieving the files
     */
    private static void getFilesByName(File file, List<FileProxy> files, Iterable<String> pathsToExclude,
            String filename) throws IOException {

        File[] children;
        FileFilter fileFilter = FileFinderUtil.getSVNFolderAndExtensionsFileFilter();

        children = file.listFiles(fileFilter);

        if (children == null) {
            return;
        }

        for (File child : children) {
            boolean shouldExclude = false;
            if (pathsToExclude != null) {
                for (String excludePath : pathsToExclude) {
                    if (child.getPath().startsWith(excludePath)) {
                        shouldExclude = true;
                        break;
                    }
                }
            }

            if (child.isFile() && !shouldExclude && StringUtils.equalsIgnoreCase(child.getName(), filename)) {
                files.add(new FileProxy(child));
            }
            getFilesByName(child, files, pathsToExclude, filename);
        }
    }
    
    /**
     * Find files of a certain extension (e.g. "mp3") from the starting directory down.
     * 
     * @param file - where to start the search for files.  If a file then no files are added to the collection.
     * @param files - list of files found
     * @param pathsToExclude an iterable collection of paths that should be excluded from the files returned, 
     * can be null if all paths should be considered. <em>Example:</em> If pathsToExclude contains 'foo/', 
     * then the files 'foo/bar.txt' and 'foo/baz/file.txt' should not be included in the returned results.
     * @param extensions - a list of file extensions to filter on (can be null or empty if no filter is needed)
     * @throws IOException if there was a problem retrieving the files
     */
    private static void getFilesByExtension(File file, List<FileProxy> files, Iterable<String> pathsToExclude, String... extensions) throws IOException{
        
        File[] children;
        FileFilter fileFilter = FileFinderUtil.getSVNFolderAndExtensionsFileFilter(extensions);
            
        children = file.listFiles(fileFilter);            
        
        if (children != null) {
            for (File child : children) {
                boolean shouldExclude = false;
                if(pathsToExclude != null) {
                    for(String excludePath : pathsToExclude) {
                        if(child.getPath().startsWith(excludePath)) {
                            shouldExclude = true;
                            break;
                        }
                    }
                }
                
                if(child.isFile() && !shouldExclude) {
                    files.add(new FileProxy(child));
                }
                getFilesByExtension(child, files, pathsToExclude, extensions);
            }
        }
    }
    
    @Override
    public String getRelativeFileName(String workspaceFilePath) throws IOException{
        /* NOTE: this method has been changed to use the common
         * getRelativePath() in FileFinderUtil instead of the previous method of
         * converting the fileProxy to a URL because a problem was found using
         * the old approach. The issue was that if a filename had an encoded
         * character as part of the filename itself (e.g.
         * http%3Abiologyinmotion.comevolindex.html.metadata.xml) then the
         * resulting output name would be decoded (e.g.
         * http:biologyinmotion.comevolindex.html.metadata.xml). This caused the
         * file to not be found because the name changed. */
        
        try {
            /* Using common code to find the relative path from this folder to
             * the file proxy */
            String relativePath = FileFinderUtil.getRelativePath(folder.getAbsoluteFile(),
                    new File(workspaceFilePath));

            /* If the file proxy is not on this folder's path (not a
             * descendant), return the absolute path of the file proxy. */
            if (relativePath.startsWith("..")) {
                relativePath = new File(workspaceFilePath).getAbsolutePath();
            }

            /* The return expects a forward slashed path, replace all
             * backslashes with forward slashes */
            return relativePath.replaceAll("\\\\", Constants.FORWARD_SLASH);
        } catch (Exception e) {
            
            String filename = workspaceFilePath != null ? workspaceFilePath : null;
            if(mil.arl.gift.common.util.StringUtils.isNotBlank(filename)){
                throw new IOException("Unable to find the file '"+filename+"' in the folder '"+folder.getPath()+"'.", e);
            }else{
                throw new IOException("Unable to perform a file search in the folder '"+folder.getPath()+"' because the provided file to search is null.", e);
            }
        }
    }

    @Override
    public String getRelativeFileName(FileProxy fileProxy) throws IOException {
        return getRelativeFileName(fileProxy.getFileId());
    }
    
    @Override
    public boolean fileExists(String filename) throws IOException{
        try{
            File file = new File(folder + File.separator + filename);
            return file.exists();
        }catch(Exception e){
            throw new IOException("There was a problem while checking if the file '"+filename+"' exists in '"+folder.getName()+"'.", e);
        }
    }

    @Override
    public FileProxy getRelativeFile(String filename) throws IOException {
        
        try{
            File file = new File(folder + File.separator + filename);
            return new FileProxy(file);
        }catch(Exception e){
            
            if(mil.arl.gift.common.util.StringUtils.isNotBlank(filename)){
                throw new IOException("Unable to find the file '"+filename+"' in the folder '"+folder.getPath()+"'.", e);
            }else{
                throw new IOException("Unable to perform a file search in the folder '"+folder.getPath()+"' because the provided file to search is null.", e);
            }
        }
    }    

    @Override
    public AbstractFolderProxy getParentFolder(FileProxy fileProxy)
            throws IOException, URISyntaxException {
        
        if(fileProxy == null){
            File file = new File(getFileId());
            File parentFile = file.getParentFile();
            return parentFile == null ? null : new DesktopFolderProxy(parentFile);
        }else{
            File file = new File(fileProxy.getFileId());
            return new DesktopFolderProxy(file.getParentFile());
        }
    }

    @Override
    public void updateFileContents(FileProxy targetFile, UnmarshalledFile contentFile, boolean createBackup, boolean useAdminPrivilege) throws IOException, SAXException, JAXBException {
        if(createBackup) {
            createBackup(targetFile);
        }

        AbstractSchemaHandler.writeToFile(contentFile.getUnmarshalled(), new File(targetFile.getFileId()), true);
        targetFile.clearStoredFileContents();
    }
    
    @Override
    public void updateFileContents(FileProxy targetFile, File contentFile, boolean createBackup, boolean useAdminPrivilege) throws IOException{
        
        if(createBackup){
            createBackup(targetFile);
        }
        
        FileUtils.moveFile(contentFile, new File(targetFile.getFileId()));
        targetFile.clearStoredFileContents();
    }
    
    @Override
    public boolean isLocalFile() {
        return true;
    }

    /**
     * Creates a backup of the provided file and saves it into this folder.
     * 
     * @param sourceProxy the file to backup.
     * @return the backup file.
     * @throws IOException if there is a problem copying the source file.
     */
    private FileProxy createBackup(FileProxy sourceProxy) throws IOException {
        File source = new File(sourceProxy.getFileId());
        if (!source.exists()) {
            throw new FileNotFoundException("The source '" + source + "' doesn't exist.");
        } else if (source.isDirectory()) {
            throw new IllegalArgumentException("The source '" + source + "' can't be a directory.");
        }

        String newFilePath = getFileId() + File.separator + source.getName() + FileUtil.BACKUP_FILE_EXTENSION;
        File target = new File(newFilePath);

        /* Don't want to overwrite a previous bak file. Append numbers until we find an available
         * name. */
        int i = 1;
        while (target.exists()) {
            target = new File(newFilePath + i++);
        }

        FileUtils.copyFile(source, target);
        return new FileProxy(target);
    }

    @Override
    public FileProxy createFile(File source, String name) throws IOException {
        
        if(!source.exists()){
            throw new FileNotFoundException("The source '"+source+"' doesn't exist.");
        }else if(source.isDirectory()){
            throw new IllegalArgumentException("The source '"+source+"' can't be a directory.");
        }
        
        File target = new File(getFileId() + File.separator + 
                (StringUtils.isNotBlank(name) ? name : source.getName()));

        FileUtils.copyFile(source, target);
        return new FileProxy(target);
    }

    @Override
    public AbstractFolderProxy createFolder(String name) throws IOException {
        
        File newFolder = new File(getFileId() + File.separator + name);
        if(!newFolder.exists()){
            newFolder.mkdir();
        }
        return new DesktopFolderProxy(newFolder);
    }

    @Override
    public float getSize() {        
        long size = FileUtil.getSize(folder);       
        return FileUtil.byteToMb(size);
    }

    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[DesktopFolderProxy: ");
        sb.append("name = ").append(getName());
        sb.append(", id = ").append(getFileId());
        sb.append("]");
        return sb.toString();
    }
}
