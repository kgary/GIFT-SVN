/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.ExternalFileSystemInterface;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileProxyPermissions;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.nuxeo.NuxeoInterface;
import mil.arl.gift.net.nuxeo.NuxeoInterface.DocumentEntityType;
import mil.arl.gift.tools.services.ServicesManager;

/**
 * A representation of a GIFT folder (e.g. course folder) in Nuxeo, meaning java.io.File operations
 * are NOT acceptable and this class must use the Nuxeo Interface to interact with documents.
 *
 * @author mhoffman
 *
 */
public class ServerFolderProxy extends AbstractFolderProxy {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ServerFolderProxy.class);

    /** The document representing this folder */
    private final Document folderDocument;

    /** The nuxeo instance */
    private final NuxeoInterface nuxeoInterface;
    
    /** the user accessing the folder */
    private final String username;

    /**
     * Set attributes 
     * @param folderDocument the Nuxeo document that has a handle to the folder, can't be null
     * @param username the user accessing the folder.
     * @param nuxeoInterface used to access the folder, can't be null.
     */
    public ServerFolderProxy(Document folderDocument, String username, NuxeoInterface nuxeoInterface) {

        if(folderDocument == null){
            throw new NullPointerException("The folderDocument can't be null.");
        }else if(nuxeoInterface == null){
            throw new NullPointerException("The nuxeo interface can't be null.");
        }

        this.folderDocument = folderDocument;
        this.nuxeoInterface = nuxeoInterface;
        this.username = username;
    }

    /**
     * Retrieves the folder associated with this proxy.
     * 
     * @return the folder document
     */
    public Document getFolder(){
        return folderDocument;
    }

    @Override
    public void deleteFolder(String name) throws IOException {

        Document folder = nuxeoInterface.getFolderByName(getFileId(), name, username);
        ((ServerFileServices)ServicesManager.getInstance().getFileServices()).deleteFolder(this, folder, username);
    }

    @Override
    public FileProxy createFile(File source, String name) throws IOException{

        if(!source.exists()){
            throw new FileNotFoundException("The source '"+source+"' doesn't exist.");
        }else if(source.isDirectory()){
            throw new IllegalArgumentException("The source '"+source+"' can't be a directory.");
        }

        Document document = ((ServerFileServices)ServicesManager.getInstance().getFileServices()).createDocument(this, 
                source, 
                mil.arl.gift.common.util.StringUtils.isNotBlank(name) ? name : source.getName(), 
                username);
        ExternalFileSystemInterface documentConnection = nuxeoInterface.getDocumentConnection(document, username);
        FileProxyPermissions permissions = nuxeoInterface.getPermissions(document);
        FileProxy fileProxy = new FileProxy(document.getTitle(), document.getPath() + File.separator + document.getTitle(),
                documentConnection, permissions);
        return fileProxy;
    }

    @Override
    public void updateFileContents(FileProxy targetFile, UnmarshalledFile contentFile, boolean createBackup, boolean useAdminPrivilege) throws IOException, SAXException, JAXBException {
        FileType fileType = AbstractSchemaHandler.getFileType(targetFile.getName());
        String content = AbstractSchemaHandler.getAsXMLString(contentFile.getUnmarshalled(), AbstractSchemaHandler.getRootClass(fileType), AbstractSchemaHandler.getSchemaFile(fileType), true);
        
        ServerFileServices serverFileServices = (ServerFileServices) ServicesManager.getInstance().getFileServices();
        String relativePath = StringUtils.replaceOnce(targetFile.getFileId(), NuxeoInterface.DEFAULT_WORKSPACE_ROOT, "");
        serverFileServices.updateFileContents(username, relativePath, content, createBackup, useAdminPrivilege);
        targetFile.clearStoredFileContents();
    }
    
    @Override
    public void updateFileContents(FileProxy targetFile, File contentFile, boolean createBackup, boolean useAdminPrivilege) throws IOException{
        
        String content = new String(Files.readAllBytes(Paths.get(contentFile.getPath())));
        
        ServerFileServices serverFileServices = (ServerFileServices) ServicesManager.getInstance().getFileServices();
        String relativePath = StringUtils.replaceOnce(targetFile.getFileId(), NuxeoInterface.DEFAULT_WORKSPACE_ROOT, "");
        serverFileServices.updateFileContents(username, relativePath, content, createBackup, useAdminPrivilege);
        targetFile.clearStoredFileContents();
    }

    @Override
    public AbstractFolderProxy createFolder(String folderName) throws IOException{
        Document document = nuxeoInterface.createWorkspaceFolder(getFileId(), folderName, username, false);
        return new ServerFolderProxy(document, username, nuxeoInterface);
    }

    @Override
    public String getFileId() {
        return folderDocument.getPath();
    }

    @Override
    public String getName() {
        return folderDocument.getTitle();
    }

    @Override
    public List<FileProxy> listFilesByName(Iterable<String> pathsToExclude, String filename) throws IOException {

        List<FileProxy> fileProxies = new ArrayList<>();
        if (StringUtils.isNotBlank(filename)) {
            Documents documents = nuxeoInterface.getDocumentsByPath(folderDocument.getPath(), pathsToExclude, null,
                    username);

            for (Document document : documents) {
                if (StringUtils.equalsIgnoreCase(document.getTitle(), filename)) {
                    ExternalFileSystemInterface documentConnection = nuxeoInterface.getDocumentConnection(document, username);
                    
                    FileProxyPermissions permissions = nuxeoInterface.getPermissions(document);
                    FileProxy fileProxy = new FileProxy(document.getTitle(), document.getPath(),
                            documentConnection, permissions);
                    fileProxies.add(fileProxy);
                }
            }
        }

        return fileProxies;
    }

    @Override
    public List<FileProxy> listFiles(Iterable<String> pathsToExclude, String... extensions) throws IOException {

        List<FileProxy> fileProxies = new ArrayList<>();
        if(extensions == null || extensions.length == 0){
            //get all files
            Documents documents = nuxeoInterface.getDocumentsByPath(folderDocument.getPath(), pathsToExclude, null, username);

            for(Document document : documents){
                if(logger.isDebugEnabled()){
                    logger.debug("Getting file proxy for document: " + document);
                }
                ExternalFileSystemInterface documentConnection = nuxeoInterface.getDocumentConnection(document, username);

                FileProxyPermissions permissions = nuxeoInterface.getPermissions(document);
                FileProxy fileProxy = new FileProxy(document.getTitle(), document.getPath(), documentConnection, permissions);
                fileProxies.add(fileProxy);
            }
        }else{
            for(String extension : extensions){
                Documents documents = nuxeoInterface.getDocumentsByPath(folderDocument.getPath(), pathsToExclude, extension, username);

                for(Document document : documents){
                    if(logger.isDebugEnabled()){
                        logger.debug("Extension (" + extension + ") Getting file proxy for document: " + document);
                    }
                    ExternalFileSystemInterface documentConnection = nuxeoInterface.getDocumentConnection(document, username);

                    FileProxyPermissions permissions = nuxeoInterface.getPermissions(document);
                    FileProxy fileProxy = new FileProxy(document.getTitle(), document.getPath(), documentConnection, permissions);
                    fileProxies.add(fileProxy);
                }
            }
        }

        return fileProxies;
    }
    
    @Override
    public String getRelativeFileName(String workspaceFilePath) throws IOException{
        
        try{
            int index = workspaceFilePath.indexOf(folderDocument.getPath());
    
            String relativePath;
    
            if(index >= 0){
    
                //this folder is an ancestor of the given file, so return the relative path
                relativePath = workspaceFilePath.substring(index + folderDocument.getPath().length());
    
            } else {
    
                // this folder is NOT an ancestor of the given file, so return the full path
                relativePath = workspaceFilePath;
            }
    
            return FileTreeModel.correctFilePath(relativePath);
        }catch(Exception e){
            
            String filename = workspaceFilePath != null ? workspaceFilePath : null;
            if(mil.arl.gift.common.util.StringUtils.isNotBlank(filename)){
                throw new IOException("Unable to find the file '"+filename+"' in the folder '"+folderDocument.getPath()+"'.", e);
            }else{
                throw new IOException("Unable to perform a file search in the folder '"+folderDocument.getPath()+"' because the provided file to search is null.", e);
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
            // #4710 - changed to documentExists instead of getDocumentEntityByName cause getDocumentEntityByName was logging warnings when
            //        we just wanted to check if the file exists and it might not exist.
            return nuxeoInterface.documentExists(folderDocument.getPath(), filename, false, username);
        }catch(Exception e){
            throw new IOException("There was a problem while checking if the file '"+filename+"' exists in '"+folderDocument.getPath()+"'.", e);
        }
    }

    /**
     * Returns the relative file to this folder based on the file name specified.<br/>
     * WARNING: this will read the entire contents of the file into memory.
     * 
     * @param filename the file name to get the file for based on using this folder as the path to that file.
     * @return the proxy representation of that file
     * @throws IOException if there was a problem retrieving the file by the name
     */
    @Override
    public FileProxy getRelativeFile(String filename) throws IOException {

        try{
            DocumentEntityType documentEntityType = nuxeoInterface.getDocumentEntityByName(folderDocument.getPath(), filename, username);
    
            if(documentEntityType == null){
                throw new IOException("Unable to find the file named '"+filename+"' in the folder '"+folderDocument.getPath()+"'.");
            }
    
            Document document = nuxeoInterface.getDocumentByName(documentEntityType.getPath(), username);
            ExternalFileSystemInterface documentConnection = nuxeoInterface.getDocumentConnection(documentEntityType, username);
    
            FileProxyPermissions permissions = nuxeoInterface.getPermissions(document);
            FileProxy fileProxy = new FileProxy(documentEntityType.getTitle(), documentEntityType.getPath(), documentConnection, permissions);
            
            return fileProxy;
        }catch(Exception e){
            
            if(mil.arl.gift.common.util.StringUtils.isNotBlank(filename)){
                throw new IOException("Unable to find the file '"+filename+"' in the folder '"+folderDocument.getPath()+"'.", e);
            }else{
                throw new IOException("Unable to perform a file search in the folder '"+folderDocument.getPath()+"' because the provided file to search is null.", e);
            }
        }
    }

    @Override
    public AbstractFolderProxy getParentFolder(FileProxy fileProxy)
            throws IOException, URISyntaxException {

        if(fileProxy == null){
            return getParentFolder(new ServerFolderProxy(folderDocument, username, nuxeoInterface));
        }else{
            String workspaceFilename = fileProxy.getFileId();
            String parentFoldername = workspaceFilename.substring(0, workspaceFilename.lastIndexOf(Constants.FORWARD_SLASH));
            Document document = nuxeoInterface.getDocumentByName(parentFoldername, username);
            ServerFolderProxy folderProxy = new ServerFolderProxy(document, username, nuxeoInterface);
            return folderProxy;
        }
    }

    /**
     * Return the parent folder of the folder provided by decomposing the folders path in order to
     * retrieve it's parent folder.
     *
     * @param folderProxy the folder to get the parent folder of
     * @return the new folder proxy of the parent folder. Will be null if the folder doesn't exist
     *         or the workspaces directory was reached or the user doesn't have permission to the parent folder.
     * @throws IOException if there was a problem checking or retrieving the parent folder
     */
    public ServerFolderProxy getParentFolder(ServerFolderProxy folderProxy) throws IOException{

        String workspaceFolderName = folderProxy.getFileId();

        //this is the parent folder of the folder that was passed in, it contains /default-domain/workspaces/ prefix since its backed by a nuxeo document)
        String parentFoldername = workspaceFolderName.substring(0, workspaceFolderName.lastIndexOf(Constants.FORWARD_SLASH));
        parentFoldername = StringUtils.replaceOnce(parentFoldername, NuxeoInterface.DEFAULT_WORKSPACE_ROOT, "");
        if(parentFoldername.isEmpty()){
            return null;
        }else if(nuxeoInterface.documentExists(null, parentFoldername, true, username)){
            Document document = nuxeoInterface.getDocumentByName(parentFoldername, username);
            return new ServerFolderProxy(document, username, nuxeoInterface);
        }else{
            return null;
        }

    }

    @Override
    public float getSize() throws IOException {
        return nuxeoInterface.getFolderSizeById(getFileId(), username);
    }
    
    @Override
    public boolean isLocalFile() {
        return false;
    }

    @Override
    public String toString(){

        StringBuilder sb = new StringBuilder();
        sb.append("[ServerFolderProxy: ");
        sb.append("document = {");
        sb.append("id = ").append(folderDocument.getId());
        sb.append(", path = ").append(folderDocument.getPath());
        sb.append(", title = ").append(folderDocument.getTitle());
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }



}
