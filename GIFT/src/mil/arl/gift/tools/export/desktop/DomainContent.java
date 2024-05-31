/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import mil.arl.gift.common.io.ContentProxyInterface;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves all of the files under Domain that will be exported.
 * 
 * @author cdettmering
 */
public class DomainContent implements DomainSelectionListener {
	
	private static Logger logger = LoggerFactory.getLogger(DomainContent.class);

	/** List of files to be exported */
	private Vector<ContentProxyInterface> domainFiles;
	
	/**
	 * Creates a new empty DomainContent
	 */
	public DomainContent() {	    
	    domainFiles = new Vector<ContentProxyInterface>();
	}
	
	/**
	 * Gets all files that have been selected 
	 * 
	 * @return List of selected files
	 */
	public Vector<ContentProxyInterface> getExportFiles() {
		return domainFiles;
	}
	
	/**
	 * Add a collection of files to export.
	 * 
	 * @param files the collection to add
	 */
	public void addExportFiles(List<ContentProxyInterface> files){
	    
	    for(ContentProxyInterface file : files){
	        addExportFile(file);
	    }
	}
	
	/**
	 * Add a file to the files to export.
	 * 
	 * @param file the file to add
	 */
    public void addExportFile(ContentProxyInterface file){
        
        if(!contains(file.getFileId())){
            //add new file to collection
            
            logger.info("Adding file to export of "+file + ".");
            domainFiles.add(file);
        }
    }
	
	public boolean contains(String fileName) {
	    
	    File fileToCheck = new File(fileName);
	    
		for(ContentProxyInterface file : domainFiles) {
		    
		    if(file instanceof DesktopFolderProxy){
		        DesktopFolderProxy courseFolder = (DesktopFolderProxy)file;
		        
		        //is the course folder under this file
		        String relativePath = FileFinderUtil.getRelativePath(fileToCheck, courseFolder.getFolder());
		        if(relativePath.length() > 0 && relativePath.startsWith("..")){
		            //no it's not
		            continue;
		        }else{
		            return true;
		        }
		    }else if(file.getFileId().equals(fileName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Called when a file has been selected for export.
	 * 
	 * @param file The selected file
	 * @throws IOException if there was a problem retrieving related files based on the file selected
	 * @throws URISyntaxException 
	 */
	@Override
	public void exportFileSelected(DesktopFolderProxy file) throws IOException, URISyntaxException {
		logger.info(file.getName() + " selected");
		
		if(!contains(file.getFileId())){
		    //add new course file to collection
		    domainFiles.add(file);
		}
	}

	/**
	 * Called when a file has been unselected for export.
	 * 
	 * @param file The unselected course file
	 */
	@Override
	public void exportFileUnselected(DesktopFolderProxy file) {
		logger.info(file.getName() + " unselected");	
        
		Iterator<ContentProxyInterface> domainFilesItr = domainFiles.iterator();
        while(domainFilesItr.hasNext()) {
            
            ContentProxyInterface existingFile = domainFilesItr.next();
            
            if(file.getFileId().equals(existingFile.getFileId())) {
                
                //remove the course file
                domainFilesItr.remove();
            }
        }
	}
	
}
