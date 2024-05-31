/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.survey;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import generated.course.PresentSurvey.ConceptSurvey;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AsyncOperationManager;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Category;
import mil.arl.gift.common.survey.Folder;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.shared.CategoryQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.CopySurveyContextResult;
import mil.arl.gift.tools.authoring.server.gat.shared.ExportSurveysResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.FolderQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.ImportSurveysResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQuery;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQueryResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.OptionListQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.QuestionQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyDependencies;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyHeader;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyReturnResult;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;
import mil.arl.gift.ums.db.UMSHibernateUtil;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SurveyRpcServiceImpl extends RemoteServiceServlet implements SurveyRpcService {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SurveyRpcServiceImpl.class);

    /** capture important properties that the sas client may need */
    private static ServerProperties SERVER_PROPERTIES = new ServerProperties();
    
    /** A mapping from each user to the asynchronous save operation being executed for them, if applicable */
    private static final ConcurrentHashMap<String, LoadedProgressIndicator<Survey>> usernameToSurveySaveStatus = new ConcurrentHashMap<String, LoadedProgressIndicator<Survey>>();
    
    static{
        SERVER_PROPERTIES.addProperty(ServerProperties.DEPLOYMENT_MODE, DashboardProperties.getInstance().getDeploymentMode().getName());
    }
    
    /** The metrics sender is responsible for sending metrics of the dashboard rpcs to the metrics server */
    private MetricsSender metrics = new MetricsSender("gat.survey");
    
    /**
     * Constructor
     */
    public SurveyRpcServiceImpl() {
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Start sending metrics.
        metrics.startSending();
    }
    
    @Override
    public void destroy() {
        
        // Stop sending metrics.
        metrics.stopSending();
        super.destroy();
    }
    
    @Override
    public ServerProperties getServerProperties() {
        long start = System.currentTimeMillis();
        ServerProperties props = SERVER_PROPERTIES;
        metrics.endTrackingRpc("getServerProperties", start);
    	return props;
    }

    @Override
	public DeploymentModeEnum getDeploymentMode() {
        long start = System.currentTimeMillis();
        DeploymentModeEnum mode = DashboardProperties.getInstance().getDeploymentMode();
        metrics.endTrackingRpc("getDeploymentMode", start);
		return mode;
	}

    @Override
    public AbstractQuestion getQuestion(int id) {
        long start = System.currentTimeMillis();
        AbstractQuestion question = Surveys.getQuestion(id);
        metrics.endTrackingRpc("getQuestion", start);
        return question;
    }

    @Override
    public SurveyReturnResult getSurvey(int id) {
        long start = System.currentTimeMillis();
        Survey survey = Surveys.getSurvey(id);
        metrics.endTrackingRpc("getSurvey", start);
        return new SurveyReturnResult(survey);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public SurveyReturnResult getSurveyWithResources(String username, int id, String targetWorkspaceFolder) {
        long start = System.currentTimeMillis();
        Survey survey = Surveys.getSurvey(id);
        
        /* Check if the survey has elements that use resources in a workspace folder */
        if(targetWorkspaceFolder != null 
                && survey.getProperties().hasProperty(SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE)) {
            
            String sourceFolder = (String) survey.getProperties().getPropertyValue(SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE);
            
            AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
            
            /* Only copy resource files if the target folder is different than the source AND the source folder exists */
            if(!targetWorkspaceFolder.equals(sourceFolder)) {
                if(fileServices.fileExists(username, sourceFolder, true)) {
                
                    for(SurveyPage page : survey.getPages()) {
                        for(AbstractSurveyElement element : page.getElements()) {
                            
                            SurveyItemProperties props = null;
                            
                            if(element instanceof AbstractSurveyQuestion) {
                                
                                /* Question image properties */
                                props = ((AbstractSurveyQuestion<? extends AbstractQuestion>) element).getQuestion().getProperties();
                                
                            } else {
                                
                                /* Text element properties */
                                props = element.getProperties();
                            }
                            
                            /* Find any survey elements that reference a media file in a workspace folder */
                            if(props != null 
                                    && props.hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY)
                                    && props.hasProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY)) {
                                    
                                String mediaFileName = (String) props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
                                String destFile = targetWorkspaceFolder + Constants.FORWARD_SLASH + mediaFileName;
                                String sourceFile = sourceFolder + Constants.FORWARD_SLASH + mediaFileName;
                                
                                try {
                                    ServicesManager.getInstance().getFileServices().copyWorkspaceFile(
                                            username, 
                                            sourceFile, 
                                            targetWorkspaceFolder, 
                                            NameCollisionResolutionBehavior.FAIL_ON_COLLISION, 
                                            null);
                                
                                } catch(FileExistsException e) {
                                    logger.warn("Copying survey resource '" + sourceFile 
                                            + "' failed because a file already exists at '" + destFile + "'.", e);
                                    
                                } catch(Exception e) {
                                    
                                    logger.error("Copying survey resource '" + sourceFile 
                                            + "' failed because an error occured while writing to '" + destFile + "'.", e);
                                    
                                    SurveyReturnResult result = new SurveyReturnResult(survey);
                                    result.setIsSuccess(false);
                                    result.setResponse("Caught an unexpected exception while copying resource files from '" 
                                            + sourceFile + "': " + e.toString());
                                    
                                    metrics.endTrackingRpc("getSurvey", start);
                                    
                                    return result;
                                }
                            }
                        }
                    }
                    
                } else {
                    logger.info("Unable to copy survey resources from '" + sourceFolder + "' because that folder no longer exists");
                }
            }
        }
        
        metrics.endTrackingRpc("getSurvey", start);
        return new SurveyReturnResult(survey);
    }
    
    @Override
    public Survey getSurveyFromExportFile(String surveyKey, String surveyCourseFolderPath, String username) {
        long start = System.currentTimeMillis();

        AbstractFolderProxy surveyCourseFolderFile = ServicesManager.getInstance().getFileServices()
                .getFolder(surveyCourseFolderPath, username);
        Survey survey = SurveyExportFileUtil.findSurvey(surveyCourseFolderFile, surveyKey, username);

        metrics.endTrackingRpc("getSurveyFromExportFile", start);
        return survey;
    }

    @Override
    public Survey getSurveyFromContextKey(String surveyKey, int surveyContextId) {
        long start = System.currentTimeMillis();
        
        Survey survey = Surveys.getSurveyContextSurvey(surveyContextId, surveyKey);
        
        metrics.endTrackingRpc("getSurveyFromContextKey", start);
        return survey;
    }
    
    @Override
    public Survey getConceptSurvey(int surveyContextId, ConceptSurvey conceptSurvey) {
    	GetKnowledgeAssessmentSurveyRequest request = GetKnowledgeAssessmentSurveyRequest.createRequestFromConceptSurvey(
    			surveyContextId, conceptSurvey.getConceptQuestions());
    	
    	return Surveys.getConceptsSurvey(surveyContextId, request.getConcepts(), false).getSurvey();
    }

    @Override
    public OptionList getOptionList(int id) {
        long start = System.currentTimeMillis();
        OptionList list = Surveys.getOptionList(id);
        metrics.endTrackingRpc("getOptionList", start);
        return list;
        
    }

    @Override
    public ListQueryResponse<OptionList> getOptionLists(ListQuery<OptionListQueryData> query) {
        long start = System.currentTimeMillis();
        ListQueryResponse<OptionList> list = SurveyDb.getOptionLists(query);
        metrics.endTrackingRpc("getOptionListsWithListQueryResponse", start);
        return list;
    }

    @Override
    public AbstractQuestion insertQuestion(AbstractQuestion question) {
        long start = System.currentTimeMillis();
        
        AbstractQuestion result = null;
        try{
            result = Surveys.insertExternalQuestion(question, null, null);
        }catch(Exception e){
            logger.error("Failed to insert question of "+question, e);
        }
        metrics.endTrackingRpc("insertQuestion", start);
        return result;
    }

    @Override
    public Boolean updateQuestion(AbstractQuestion question, String username) {
        long start = System.currentTimeMillis();
        Boolean updated = Surveys.updateQuestion(question, username);
        metrics.endTrackingRpc("updateQuestion", start);
        return updated;
    }

    @Override
    public Boolean updateSurveyContextSurvey(SurveyContextSurvey surveyContextSurvey) {
        long start = System.currentTimeMillis();
        try{
            Surveys.insertSurveyContextSurvey(surveyContextSurvey, null);
            return true;
        }catch(Exception e){
            logger.error("Failed to update the survey context survey of "+surveyContextSurvey, e);
        }
        metrics.endTrackingRpc("updateSurveyContext", start);
        return false;
    }

    @Override
    public RpcResponse insertFolder(Folder folder) {
        long start = System.currentTimeMillis();
    	RpcResponse result = new RpcResponse();
    	
    	try {
    	    Surveys.insertFolder(folder);
    		result.setIsSuccess(true);
    	} catch (Exception e) {
    		
    		if (e instanceof DetailedException) {
    			result.setResponse(((DetailedException) e).getReason());
    			result.setAdditionalInformation(((DetailedException) e).getDetails());	
    		} else {
    			result.setResponse(e.getMessage());
    			result.setAdditionalInformation(e.getCause().toString());
    		}
    		
    		result.setIsSuccess(false);
    	}
    	
    	metrics.endTrackingRpc("insertFolder", start);
    	
        return result;
    }

    @Override
    public Boolean deleteTempFile(String tempFileToDelete) {
        long start = System.currentTimeMillis();
    	File tempFile = new File(tempFileToDelete);
    	boolean deleted = true;
    	
    	if(tempFile.exists()) {
    		try {
				FileUtils.forceDelete(tempFile);
			} catch (IOException e) {
				deleted = false;
				logger.warn("Could not delete " + tempFile.getPath() , e);
			}
    	}
    	
    	metrics.endTrackingRpc("deleteTempFile", start);
    	return Boolean.valueOf(deleted);
    }
    
    @Override
    public Boolean deleteFolder(String folder, String userName) {
        long start = System.currentTimeMillis();
        Boolean deleted = true;
        try{
            Surveys.deleteFolder(folder, userName);
        }catch(Exception e){
            logger.error("Failed to delete folder "+folder, e);
            deleted = false;
        }
        metrics.endTrackingRpc("deleteFolder", start);
        return deleted;
    }

    @Override
    public ListQueryResponse<Folder> getFolders(ListQuery<FolderQueryData> query) {
        long start = System.currentTimeMillis();
        
        ListQueryResponse<Folder> list = SurveyDb.getFolders(query);
        metrics.endTrackingRpc("getFolders", start);
        return list;
    }

    @Override
    public Survey updateSurvey(Survey survey) {
        long start = System.currentTimeMillis();
        Survey result = Surveys.updateSurvey(survey);
        metrics.endTrackingRpc("updateSurvey", start);
        return result;
    }

    @Override
    public Survey insertSurvey(Survey survey) {
        long start = System.currentTimeMillis();
        Survey result = Surveys.insertSurvey(survey, null);
        metrics.endTrackingRpc("insertSurvey", start);
        return result;
    }    

	@Override
	public ExportSurveysResponse exportSurvey(int surveyId) {
	    long start = System.currentTimeMillis();
	    ExportSurveysResponse result = SurveyDb.exportSurvey(surveyId);
		metrics.endTrackingRpc("exportSurveyById", start);
		return result;
	}

	@Override
	public ExportSurveysResponse exportSurvey(Survey survey) {
	    long start = System.currentTimeMillis();
	    ExportSurveysResponse result = SurveyDb.exportSurvey(survey);
		metrics.endTrackingRpc("exportSurveyBySurvey", start);
		return result;
	}
	
	@Override
	public ExportSurveysResponse exportSurveyContext(SurveyContext context) {
	    long start = System.currentTimeMillis();
	    ExportSurveysResponse result = SurveyDb.exportSurveyContext(context);
		metrics.endTrackingRpc("exportSurveyContextByContext", start);
		return result;
	}

	@Override
	public ExportSurveysResponse exportSurveyContext(int contextId) {
	    long start = System.currentTimeMillis();
	    ExportSurveysResponse result = SurveyDb.exportSurveyContext(contextId);
		metrics.endTrackingRpc("exportSurveyContextById", start);
		return result;
	}
	
	@Override
	public ImportSurveysResponse importSurvey(String file, String username) {
	    return importSurvey(file, username, null);
	}
	
	@Override
	public ImportSurveysResponse importSurvey(String file, String username, Map<String, String> filesToRename) {
		long start = System.currentTimeMillis();
	    ImportSurveysResponse result = SurveyDb.importSurvey(file, username, filesToRename);
		metrics.endTrackingRpc("importSurvey", start);
		
		return result;
	}
		
	@Override
	public List<String> getExportedSurveys() {
	    long start = System.currentTimeMillis();

	    List<String> result = null;
        if (DashboardProperties.getInstance().getSurveyExportPath() != null) {

            File exportSurveyDirectory = new File(DashboardProperties.getInstance().getSurveyExportPath());

            if (exportSurveyDirectory.isDirectory()) {
            	
            	File[] files = exportSurveyDirectory.listFiles(new FileFilter() {

					@Override
					public boolean accept(File file) {
						return file.exists() && file.isFile() && 
							(file.getName().endsWith(Surveys.SURVEY_EXPORT_EXTENSION) || 
							file.getName().endsWith(Surveys.SURVEY_EXPORT_EXTENSION + ".zip"));
					}
            		
            	});
            	
            	List<String> surveyExports = new ArrayList<String>();
            	for(File file : files) {
            		surveyExports.add(file.getName());
            	}
            	
                Collections.sort(surveyExports);
                Collections.reverse(surveyExports);
                result = surveyExports;

            } else {

                logger.error("Could not get the list of survey exports, '" + DashboardProperties.getInstance().getSurveyExportPath() + "' is not a directory");
            }

        } else {

            logger.error("Could not get list of survey exports, the survey exports directory property is undefined.");
        }

        metrics.endTrackingRpc("getExportedSurveys", start);
        return result;
	}
	
	@Override
	public List<String> getExportedSurveyContexts() {
	    long start = System.currentTimeMillis();
	    List<String> result = null;
        if (DashboardProperties.getInstance().getSurveyExportPath() != null) {

            File exportSurveyDirectory = new File(DashboardProperties.getInstance().getSurveyExportPath());

            if (exportSurveyDirectory.isDirectory()) {
            	
            	File[] files = exportSurveyDirectory.listFiles(new FileFilter() {

					@Override
					public boolean accept(File file) {
						return file.exists() && file.isFile() && 
							(file.getName().endsWith(FileUtil.SURVEY_REF_EXPORT_SUFFIX) ||
							file.getName().endsWith(FileUtil.SURVEY_REF_EXPORT_SUFFIX + ".zip"));
					}
            		
            	});
            	
            	List<String> surveyExports = new ArrayList<String>();
            	for(File file : files) {
            		surveyExports.add(file.getName());
            	}
            	
                Collections.sort(surveyExports);
                Collections.reverse(surveyExports);
                result = surveyExports;

            } else {

                logger.error("Could not get the list of survey exports, '" + DashboardProperties.getInstance().getSurveyExportPath() + "' is not a directory");
            }

        } else {

            logger.error("Could not get list of survey exports, the survey exports directory property is undefined.");
        }

        metrics.endTrackingRpc("getExportedSurveyContexts", start);
        return result;
	}	

    @Override
    public OptionList insertOptionList(String userName, OptionList optionList) {
        long start = System.currentTimeMillis();
        OptionList result = null;
        try{
            result = Surveys.insertOptionList(userName, optionList);
        }catch(Exception e){
            logger.error("Failed to insert option list: "+optionList, e);
        }
        metrics.endTrackingRpc("insertOptionList", start);
        return result;
    }

    @Override
    public Boolean deleteOptionList(int optionListId, String userName) {
        long start = System.currentTimeMillis();
        Boolean deleted = true;
        try{
            Surveys.deleteOptionList(optionListId, userName, null, null);
        }catch(Exception e){
            logger.error("Failed to delete option list with id "+optionListId, e);
            deleted = false;
        }
        metrics.endTrackingRpc("deleteOptionList", start);
        return deleted;
    }

    @Override
    public Boolean updateOptionList(String userName, OptionList optionList) {
        long start = System.currentTimeMillis();
        Boolean result = Surveys.updateOptionList(userName, optionList);
        metrics.endTrackingRpc("updateOptionList", start);
        return result;
    }

    @Override
    public ListQueryResponse<Category> getCategories(ListQuery<CategoryQueryData> query) {
        long start = System.currentTimeMillis();
        ListQueryResponse<Category> result = SurveyDb.getCategories(query);
        metrics.endTrackingRpc("getCategories", start);
        return result;
    }

    @Override
    public RpcResponse insertCategory(Category category) {
        long start = System.currentTimeMillis();
    	RpcResponse result = new RpcResponse();
    	
    	try {
    	    Surveys.insertCategory(category);
    		result.setIsSuccess(true);
    	} catch (Exception e) {
    		
    		if (e instanceof DetailedException) {
    			result.setResponse(((DetailedException) e).getReason());
    			result.setAdditionalInformation(((DetailedException) e).getDetails());	
    		} else {
    			result.setResponse(e.getMessage());
    			result.setAdditionalInformation(e.getCause().toString());
    		}
    		
    		result.setIsSuccess(false);
    	}
    	metrics.endTrackingRpc("insertCategory", start);
        return result;
    }

    @Override
    public Boolean deleteCategory(String category, String username) {
        long start = System.currentTimeMillis();
        Boolean deleted = true;
        try{
            Surveys.deleteCategory(category, username);
        }catch(Exception e){
            logger.error("Failed to delete category "+category, e);
            deleted = false;
        }
        metrics.endTrackingRpc("deleteCategory", start);
        return deleted;
    }

    @Override
    public List<String> getSurveyImages(int surveyId) {
        long start = System.currentTimeMillis();
    	
    	ArrayList<String> imageList = new ArrayList<String>();
    	
    	for(File file : SurveyDb.getSurveyImages(surveyId)) {
    		imageList.add(file.getName());
    	}
    	metrics.endTrackingRpc("getSurveyImagesById", start);
    	return imageList;
    }
    
    @Override
    public List<String> getSurveyImages() {
        long start = System.currentTimeMillis();

        List<String> surveyImages = new ArrayList<String>();

        File fileServerDirectory = new File(DashboardProperties.getInstance().getSurveyImageUploadDirectory());

        logger.debug("getSurveyImages() absolute path: " + fileServerDirectory.getAbsolutePath());
        for (File file : fileServerDirectory.listFiles()) {

            getSurveyImages(file, null, surveyImages, DashboardProperties.getInstance().getSurveyImagesDirectoryExcludes());
        }
        
        // Standardize the paths of the strings to have all forward slashes for the client.
        for (int i = 0; i < surveyImages.size(); i++) {
            String replacedString = surveyImages.get(i).replace(Constants.BACKWARD_SLASH, Constants.FORWARD_SLASH);
            surveyImages.set(i,replacedString);
        }

        metrics.endTrackingRpc("getSurveyImages", start);
        return surveyImages;
    }

    /**
     * Gets the list of images from a folder
     *
     * @param currentFile A file/folder containing survey images.  Can't be null and must exist.
     * @param currentDirectory The relative path to the current file/folder.  Usually null on the first call to indicate
     * the root directory.
     * @param surveyImages The current list of survey images
     * @param excludes The list of filters to use to exclude files
     */
    private void getSurveyImages(File currentFile, String currentDirectory, List<String> surveyImages, String[] excludes) {
       
        if (currentFile.isDirectory()) {
            for (File file : currentFile.listFiles()) {
                if (currentDirectory != null) {                    
                    getSurveyImages(file, currentDirectory + File.separator + currentFile.getName(), surveyImages, excludes);
                    }else{
                    getSurveyImages(file, currentFile.getName(), surveyImages, excludes);
                    }
                }
        } else {
            String fileName;
            if (currentDirectory != null) {
                fileName = currentDirectory + File.separator + currentFile.getName();
            } else {
                fileName = currentFile.getName();
            }

            boolean exclude = false;
            if (excludes != null) {
                for (String excludePattern : excludes) {
                    if (!exclude) {
                        exclude |= fileName.contains(excludePattern);
                    } else {
                        break;
                    }
                }
            }
            if (!exclude) {
                surveyImages.add(fileName);
            }
        }
    }

    @Override
    public String canPerformAdminDatabaseOperations() {
        long start = System.currentTimeMillis();
        String result = null;
        if (DashboardProperties.getInstance().getDatabaseBackupsDirectory() != null) {

            File databaseBackupsDirectory = new File(DashboardProperties.getInstance().getDatabaseBackupsDirectory());

            if (databaseBackupsDirectory.isDirectory()) {

                result = null;

            } else {

                result = "'" + DashboardProperties.getInstance().getDatabaseBackupsDirectory() + "' is not a directory";
            }

        } else {

            result = "The database backups directory property is undefined.";
        }
        
        metrics.endTrackingRpc("canPerformAdminDatabaseOperations", start);
        
        return result;
    }
    
    @Override
    public String canPerformDatabaseOperations(){
        long start = System.currentTimeMillis();
        String result = null;
        try{
            UMSHibernateUtil.getInstance();
        }catch(Exception e){
            logger.error("Caught exception while instantiating the UMS hibernate util.", e);
            result = e.getMessage();
        }
        
        //no issues to report
        metrics.endTrackingRpc("canPerformDatabaseOperations", start);
        return result;
    }

    @Override
    public List<String> getDatabaseBackups() {
        long start = System.currentTimeMillis();
        
        List<String> fileNames = null;
        List<File> files = UMSHibernateUtil.getInstance().getDatabaseBackupFileNames(DashboardProperties.getInstance().getDatabaseBackupsDirectory());
        if(files != null){
            //get the files names and sort them
            
            fileNames = new ArrayList<String>(files.size());
            for(File file : files){
                fileNames.add(file.getName());
            }
            
            Collections.sort(fileNames);
            Collections.reverse(fileNames);
        }
        metrics.endTrackingRpc("getDatabaseBackups", start);
        return fileNames;
    }

    @Override
    public Boolean backupDatabase(boolean backupUserData) {
        long start = System.currentTimeMillis();
        
        Boolean result = SurveyDb.backupDatabase(backupUserData);
        metrics.endTrackingRpc("backupDatabase", start);
        return result;
    }

    @Override
    public Boolean restoreDatabase(String backupFile) {
        long start = System.currentTimeMillis();

        Boolean result = SurveyDb.restoreDatabase(backupFile);
        metrics.endTrackingRpc("restoreDatabase", start);
        return result;
    }

    @Override
    public Boolean renameDatabaseBackupFile(String oldBackupFileName, String newBackupFileName) {
        long start = System.currentTimeMillis();
        Boolean result = false;
        if (DashboardProperties.getInstance().getDatabaseBackupsDirectory() != null) {

            File databaseBackupsDirectory = new File(DashboardProperties.getInstance().getDatabaseBackupsDirectory());

            if (databaseBackupsDirectory.isDirectory()) {
                boolean hasResult = false;
                for (File file : databaseBackupsDirectory.listFiles()) {

                    if (file.isFile() && file.getName().equals(oldBackupFileName)) {

                        hasResult = true;
                        result = file.renameTo(new File(file.getParentFile(), newBackupFileName));
                        break;
                    }
                }
                
                if (!hasResult) {
                    result = false;
                }

            } else {

                logger.error("Could not rename a database backup, '" + DashboardProperties.getInstance().getDatabaseBackupsDirectory() + "' is not a directory");
            }

        } else {

            logger.error("Could not rename a database backup, the database backups directory property is undefined.");
        }

        metrics.endTrackingRpc("renameDatabaseBackupFile", start);
        return result;
    }

    @Override
    public Boolean deleteDatabaseBackupFile(String backupFileName) {
        long start = System.currentTimeMillis();

        Boolean result = false;
        if (DashboardProperties.getInstance().getDatabaseBackupsDirectory() != null) {

            File databaseBackupsDirectory = new File(DashboardProperties.getInstance().getDatabaseBackupsDirectory());

            if (databaseBackupsDirectory.isDirectory()) {

                Boolean hasResult = false;
                for (File file : databaseBackupsDirectory.listFiles()) {

                    if (file.isFile() && file.getName().equals(backupFileName)) {

                        hasResult = true;
                        result = file.delete();
                        break;
                    }
                }

                if (!hasResult) {
                    result = false;
                }
                

            } else {

                logger.error("Could not delete a database backup, '" + DashboardProperties.getInstance().getDatabaseBackupsDirectory() + "' is not a directory");
            }

        } else {

            logger.error("Could not delete a database backup, the database backups directory property is undefined.");
        }

        metrics.endTrackingRpc("deleteDatabaseBackupFile", start);
        return result;
    }

    @Override
    public String getSurveyContextDependencies(int surveyContextId) {
        long start = System.currentTimeMillis();

        String result = Surveys.getSurveyContextDependencies(surveyContextId);
        metrics.endTrackingRpc("getSurveyContextDependencies", start);
        return result;
    }

    @Override
    public SurveyDependencies getSurveyDependencies(int surveyId) {
        long start = System.currentTimeMillis();

        SurveyDependencies result = SurveyDb.getSurveyDependencies(surveyId, 0);
        metrics.endTrackingRpc("getSurveyDependencies", start);
        return result;
    }
    
    @Override
    public SurveyDependencies getSurveyDependencies(int surveyId, int ignoreSurveyContextId) {
        long start = System.currentTimeMillis();

        SurveyDependencies result = SurveyDb.getSurveyDependencies(surveyId, ignoreSurveyContextId);
        metrics.endTrackingRpc("getSurveyDependenciesWithIgnore", start);
        return result;
    }

    @Override
    public String getQuestionDependencies(int questionId) {
        long start = System.currentTimeMillis();

        String result = Surveys.getQuestionDependencies(questionId);
        metrics.endTrackingRpc("getQuestionDependencies", start);
        return result;
    }

    @Override
    public String getOptionListDependencies(int optionListId) {
        long start = System.currentTimeMillis();

        String result = SurveyDb.getOptionListDependencies(optionListId);
        metrics.endTrackingRpc("getOptionListDependencies", start);
        return result;
    }

    @Override
    public ListQueryResponse<AbstractQuestion> getQuestions(ListQuery<QuestionQueryData> query) {
        long start = System.currentTimeMillis();

        ListQueryResponse<AbstractQuestion> result = SurveyDb.getQuestions(query);
        metrics.endTrackingRpc("getQuestions", start);
        return result;
    }

    @Override
    public ListQueryResponse<SurveyHeader> getSurveys(ListQuery<SurveyQueryData> query) {
        long start = System.currentTimeMillis();

        ListQueryResponse<SurveyHeader> result = SurveyDb.getSurveys(query);
        metrics.endTrackingRpc("getSurveys", start);
        return result;
    }
    
    @Override
    public Boolean insertSurveyPropertyKey(SurveyPropertyKeyEnum key){
        long start = System.currentTimeMillis();
        
        Boolean result = true;
        try{
            Surveys.insertSurveyPropertyKey(key);
        }catch(Exception e){
            result = false;
            
            logger.error("Failed to insert survey property key of '"+key+"' into survey database.", e);
        }
        metrics.endTrackingRpc("insertSurveyPropertyKey", start);
        return result;
    }
    
    @Override
    public List<String> getSurveyPropertyKeyNames(){
        long start = System.currentTimeMillis();
        List<String> result =  Surveys.getSurveyPropertyKeyNames();
        metrics.endTrackingRpc("getSurveyPropertyKeyNames", start);
        return result;
    }
	
	@Override
    public Boolean deleteSurveyResponses(int surveyContextId, Survey survey, String userName) {
        long start = System.currentTimeMillis();
        Boolean deleted = true;
        try{
            Surveys.deleteSurveyResponses(surveyContextId, survey.getId(), userName);
        }catch(Exception e){
            logger.error("Failed to delete survey context for survey context of "+surveyContextId, e);
            deleted = false;
        }
        metrics.endTrackingRpc("deleteSurveyResponsesFromSurvey", start);
        return deleted;
    }

	@Override
	public Boolean lockQuestion(int id, boolean acquire) {
	    long start = System.currentTimeMillis();
		boolean result = SasLockManager.getInstance().lockQuestion(id, acquire);
		metrics.endTrackingRpc("lockQuestion", start);
		return result;
	}

	@Override
	public boolean unlockQuestion(int id) {
	    long start = System.currentTimeMillis();
		SasLockManager.getInstance().unlockQuestion(id);
		metrics.endTrackingRpc("unlockQuestion", start);
		return true;
	}

	@Override
	public Boolean lockSurvey(int id, boolean acquire) {
	    long start = System.currentTimeMillis();
		boolean result = SasLockManager.getInstance().lockSurvey(id, acquire);
		metrics.endTrackingRpc("lockSurvey", start);
		return result;
	}

	@Override
	public boolean unlockSurvey(int id) {
	    long start = System.currentTimeMillis();
		SasLockManager.getInstance().unlockSurvey(id);
		metrics.endTrackingRpc("unlockSurvey", start);
		return true;
	}

	@Override
	public Boolean lockSurveyContext(int id, boolean acquire) {
	    long start = System.currentTimeMillis();
		boolean result = SasLockManager.getInstance().lockSurveyContext(id, acquire);
		metrics.endTrackingRpc("lockSurveyContext", start);
		return result;
	}

	@Override
	public boolean unlockSurveyContext(int id) {
	    long start = System.currentTimeMillis();
		SasLockManager.getInstance().unlockSurveyContext(id);
		metrics.endTrackingRpc("unlockSurveyContext", start);
		return true;
	}
	
	@Override
    public SurveyReturnResult surveyEditorSaveSurvey(Survey survey, SurveyContext surveyContext, String username) {
        long start = System.currentTimeMillis();
        SurveyReturnResult result = null;
        try{
            Survey savedSurvey = Surveys.surveyEditorSaveSurvey(survey, surveyContext.getId(), null, username);
            result = new SurveyReturnResult(savedSurvey);
            result.setIsSuccess(true);
        }catch(Exception e){
            logger.error("Failed to save survey", e);
            
            result = new SurveyReturnResult();
            result.setIsSuccess(false);
            result.setResponse("Failed to save survey.");
            result.setAdditionalInformation("The most common cause is that some required survey element wasn't authored.  Check the exception for further details.");
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }
        metrics.endTrackingRpc("surveyEditorSaveSurvey", start);
        return result;
    }

    @Override
    public void surveyEditorSaveSurveyAsync(final Survey survey, final Integer surveyContextId, final String username) {
        long start = System.currentTimeMillis();
        
        final LoadedProgressIndicator<Survey> saveStatus = new LoadedProgressIndicator<>();
        
        usernameToSurveySaveStatus.put(username, saveStatus);
        
        Runnable saveOperation = new Runnable() {

            @Override
            public void run(){
            	
                try{
                	
                    Survey savedSurvey = Surveys.surveyEditorSaveSurvey(survey, surveyContextId, null, username);
                    
                    saveStatus.setPayload(savedSurvey);
                    saveStatus.setComplete(true);
                    
                }catch(Exception e){
                	
                    logger.error("Failed to save survey", e);
                    
                    saveStatus.setException(new DetailedException(
                    		"Failed to save survey", 
                    		"The most common cause is that some required survey element wasn't authored.  Check the exception for further details.", 
                    		e
                    ));
                }
            }
        };
        
        AsyncOperationManager.getInstance().startAsyncOperation("saveSurveyThread-" + username, saveOperation);
        
        metrics.endTrackingRpc("surveyEditorSaveSurvey", start);
    }

    @Override
    public void surveyEditorExportSurveyContextSurvey(final SurveyContextSurvey surveyContextSurvey,
            String surveyCourseFolderPath, final String username) {
        AbstractFolderProxy surveyCourseFolderFile = ServicesManager.getInstance().getFileServices()
                .getFolder(surveyCourseFolderPath, username);
        try {
            SurveyExportFileUtil.saveSurveyToExportFile(surveyContextSurvey, surveyCourseFolderFile, username);
        } catch (Exception e) {
            throw new DetailedException("The survey context survey failed to export",
                    "The survey context survey failed to export because '" + e.getMessage() + "'.", e);
        }
    }

    @Override
    public LoadedProgressIndicator<Survey> getSaveSurveyStatus(String username){
    	
    	LoadedProgressIndicator<Survey> saveStatus = usernameToSurveySaveStatus.get(username);
    	
    	if(saveStatus != null){   		
    		return saveStatus;
    		
    	} else {
    		return new LoadedProgressIndicator<>();
    	}
    }

    @Override
    public CopySurveyContextResult copySurveyContext(int srcSurveyContextId, Set<String> srcSurveyContextGiftKey, int destSurveyContextId, String username) {
        
        long start = System.currentTimeMillis();
        CopySurveyContextResult surveyContextResult = null;
        try{
            surveyContextResult = SurveyDb.copySurveyContext(srcSurveyContextId,  srcSurveyContextGiftKey, destSurveyContextId,  username);
        }catch(Exception e){
            logger.error("Failed to copy survey context of context with id "+srcSurveyContextId, e);
        }

        metrics.endTrackingRpc("copySurveyContext", start);
        return surveyContextResult;
    }

    @Override
    public Boolean isSurveyEditable(int surveyId, String userName) {

        try{
            return Surveys.isSurveyEditable(surveyId, userName);
        }catch(Exception e){
            logger.error("Failed to check if the survey with id "+surveyId+" is editable to user "+userName, e);
        }
        return false;
    }

    @Override
    public Boolean isSurveyEditable(int surveyContextId, Survey survey, String username) {
        
        try{
            return Surveys.isSurveyEditable(surveyContextId, survey, username);
        }catch(Exception e){
            logger.error("Failed to check if the survey with id "+survey.getId()+" in survey context "+surveyContextId+" is editable to user "+username, e);
        }
        return false;
    }
	
}
