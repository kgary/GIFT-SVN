/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFilesResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Handles requests to get media files from within a course folder.
 * 
 * @author bzahid
 */
public class FetchMediaFilesHandler implements ActionHandler<FetchMediaFiles, FetchMediaFilesResult> {

    private static final Logger logger = LoggerFactory.getLogger(FetchMediaFilesHandler.class);

	@Override
	public FetchMediaFilesResult execute(FetchMediaFiles action, ExecutionContext context) throws DispatchException {
		
		long start = System.currentTimeMillis();
		FetchMediaFilesResult result = new FetchMediaFilesResult();
		
		try {
			
			if(action.getUserName() == null){
				throw new IllegalArgumentException("The username of the user retrieving media files cannot be null.");
			}
			
			if(action.getCourseFolderPath() == null){
				throw new IllegalArgumentException("The path to the course from which media files should be retrieved cannot be null.");
			}
			
			ArrayList<FileTreeModel> fileList = new ArrayList<FileTreeModel>();
			AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();	
			
			FileTreeModel mediaFilesModel = fileServices.getMediaFiles(action.getUserName(), action.getCourseFolderPath());
			
			addFilesToList(mediaFilesModel, fileList);

			Map<FileTreeModel, String> fileMap = getContentUrl(action, fileList);
			
			result.setFileMap(fileMap);
			result.setSuccess(true);
			
		} catch (DetailedException e) {
			
			result.setSuccess(false);
			result.setErrorMsg(e.getReason());
			result.setErrorDetails(e.getDetails());
			result.setErrorStackTrace(e.getErrorStackTrace());
			
		} catch (Exception e) {
			
			result.setSuccess(false);
			result.setErrorMsg("There was a problem retrieving media files for this course.");
			result.setErrorDetails(e.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("course.FetchMediaFilesResult", start);
		return result;
	}

	/**
	 * Adds the files from the fileTreeModel to the fileList
	 * 
	 * @param fileTreeModel fileTreeModel representing a directory
	 * @param fileList list of file in the fileTreeModel
	 */
	private void addFilesToList(FileTreeModel fileTreeModel, List<FileTreeModel> fileList) {
		if(fileTreeModel.isDirectory()) {
			for(FileTreeModel fileModel : fileTreeModel.getSubFilesAndDirectories()) {
				if(!fileModel.isDirectory()) {
					fileList.add(fileModel);					
				} else {
					addFilesToList(fileModel, fileList);
				}
			}
		} else {
			fileList.add(fileTreeModel);
		}
	}
	
	/**
	 * Gets the urls of a list of media files
	 * 
	 * @param action the {@link FetchMediaFiles} action
	 * @param fileList the list of media files
	 * @return a map of the files to their respective urls
	 */
	private HashMap<FileTreeModel, String> getContentUrl(FetchMediaFiles action, ArrayList<FileTreeModel> fileList) {

    	try {
    	    String contentURL = "";
    	    String userName = action.getUserName();
    	    String relativeFileName = "";
    	    
    	    HashMap<FileTreeModel, String> fileMap = new HashMap<FileTreeModel, String>();
    	    
    	    for (FileTreeModel file : fileList) {
    	        
                relativeFileName = action.getCourseFolderPath() + Constants.FORWARD_SLASH + file.getRelativePathFromRoot(true);

                // Ensure relativeFileName starts with forward slash in server mode
                if (DomainModuleProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.SERVER)){
                    if (!relativeFileName.startsWith(Constants.FORWARD_SLASH)) {
                        relativeFileName = Constants.FORWARD_SLASH + relativeFileName;
                    }
                }

                DesktopFolderProxy desktopWorkspace = new DesktopFolderProxy(new File(ServicesProperties.getInstance().getWorkspaceDirectory()));
                String fileId = desktopWorkspace.getFileId();
                
                contentURL = DomainCourseFileHandler.getAssociatedCourseImage(fileId, relativeFileName, userName);
                
                fileMap.put(file, contentURL);
            }
    	    
    	    return fileMap;

    	} catch (Exception e) {
    	    logger.error("An error occurred while fetching media file urls: " + e.getMessage());
    	    return null;
    	}

	}
	
	@Override
	public Class<FetchMediaFiles> getActionType() {
		return FetchMediaFiles.class;
	}

	@Override
	public void rollback(FetchMediaFiles action, FetchMediaFilesResult result,
			ExecutionContext context) throws DispatchException {
		// nothing to roll back
	}

}
