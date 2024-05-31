/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export;

import java.util.List;
import java.util.Vector;

import mil.arl.gift.common.io.ContentProxyInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves all of the files under Domain that will be exported.
 * 
 * @author mhoffman
 */
public class DomainContent {
	
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
        
        if(!domainFiles.contains(file)){
            //add new file to collection
            
            logger.info("Adding file to export of "+file + ".");
            domainFiles.add(file);
        }
    }
	
	public boolean contains(String fileName) {
		for(ContentProxyInterface file : domainFiles) {
			if(file.getFileId().equals(fileName)) {
				return true;
			}
		}
		return false;
	}
	
}
