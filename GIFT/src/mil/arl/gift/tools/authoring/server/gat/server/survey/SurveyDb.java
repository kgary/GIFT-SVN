/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.survey;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.hibernate.Session;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.Category;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.Folder;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyJSON;
import mil.arl.gift.tools.authoring.server.gat.shared.CategoryQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.CopySurveyContextResult;
import mil.arl.gift.tools.authoring.server.gat.shared.ExportSurveysResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.FolderQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.ImportSurveysResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQuery;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQueryResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.OptionListQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.QuestionQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyContextQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyDependencies;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyHeader;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyQueryData;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.ums.db.HibernateObjectReverter;
import mil.arl.gift.ums.db.SurveyContextUtil;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.Surveys.ExternalSurveyMapper;
import mil.arl.gift.ums.db.table.DbCategory;
import mil.arl.gift.ums.db.table.DbFolder;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestionResponse;
import mil.arl.gift.ums.db.table.DbSurvey;
import mil.arl.gift.ums.db.table.DbSurveyContext;
import mil.arl.gift.ums.db.table.DbSurveyContextSurvey;
import mil.arl.gift.ums.db.table.DbSurveyPage;
import mil.arl.gift.ums.db.table.DbSurveyResponse;

/**
 * Manages the SAS's connection to the database, providing methods for
 * inserting, updating, and delete the survey data objects
 *
 * @author jleonard
 */
public class SurveyDb {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SurveyDb.class);

    private static UMSDatabaseManager dbMgr;

    private static HibernateObjectReverter hibernateToGiftSurvey = new HibernateObjectReverter(UMSDatabaseManager.getInstance());

    static {
        dbMgr = UMSDatabaseManager.getInstance();
    }

    /**
     * Constructor
     */
    private SurveyDb() { }


    /**
     * Queries the server for available questions
     *
     * @param query The query for a list of questions
     * @return ListQueryResponse<GwtQuestion> The response to the query
     */
    public static ListQueryResponse<AbstractQuestion> getQuestions(ListQuery<QuestionQueryData> query) {
        // Until we figure out how to do all the filtering on the database side,
        // a hybrid approach is being used to filter as much on the DB side as 
        // possible, followed by code based filtering.
        try {
            // grab query items
            List<QuestionTypeEnum> questionTypes = query.getQueryData().getQueryQuestionTypes();
            List<String> categories = query.getQueryData().getQueryCategories();
            String userName = query.getQueryData().getUserName();
            List<String> querySearchText = query.getQueryData().getQuerySearchText();
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("from DbQuestion as q");
            boolean needsWhere = true;
            
            // filter user name in query
            if (userName != null && !userName.isEmpty()) {
                queryBuilder.append(" where ('*' in elements(q.visibleToUserNames) or '") 
                            .append(userName).append("' in elements(q.visibleToUserNames))");
                needsWhere = false;
            }
            
            int startIndex = -1;
            int maxResults = -1;
            int numRows = -1;
            int typeID = -1;
            boolean calculateIndices = true;
            if (categories.isEmpty() && questionTypes.isEmpty() && (querySearchText == null || querySearchText.isEmpty() || 
                    (querySearchText.size() == 1 && querySearchText.get(0).isEmpty()))) {
                
                // no filter criteria so we can do a small and fast query with calculated start index and max results
                int numResults  = query.getQueryRecordReturnCount();
                numRows = (int)dbMgr.getRowCountByQuery("select count(*) " + queryBuilder.toString(), null);
                maxResults = query.getQueryRecordReturnCount() == 0 ? numRows
                        : Math.min(numRows, numResults);
                
                startIndex = (query.getQueryRecordIndexStart() +  maxResults >= numRows ? 
            		numRows - maxResults : query.getQueryRecordIndexStart());
                if(startIndex < 0){
                    startIndex = 0;
                }
                calculateIndices = false;
            } else {
                // TODO still need to figure out the query to make this possible
                // Filter category
                if (!categories.isEmpty()) {
    //                if (needsWhere) {
    //                    queryBuilder.append(" where ");
    //                    needsWhere = false;
    //                } else {
    //                    queryBuilder.append(" and ");
    //                }
    //                queryBuilder.append("(");
    //                for (int i=0; i < categories.size(); i++) {
    //                    queryBuilder.append(" '").append(categories.get(i)).append("' in elements(q.categories.name)");
    //                    if (i != categories.size()-1) {
    //                        queryBuilder.append(" or ");
    //                    }
    //                }
    //                queryBuilder.append(")");
                    }
                
                // Filter question type
                if (!questionTypes.isEmpty()) {
                    if (needsWhere) {
                        queryBuilder.append(" where ");
                    } else {
                        queryBuilder.append(" and ");
                    }
                    queryBuilder.append("(");
                    for (int i=0; i < questionTypes.size(); i++) {
                        // Note that we have to add one to the search value because the QuestionTypeEnum is zero based
                        // whereas the DbQuestionType id is one based indexed
                        typeID = questionTypes.get(i).getValue()+1;
                        queryBuilder.append(" q.questionType.id = ").append(typeID);
                        if (i != questionTypes.size()-1) {
                            queryBuilder.append(" or ");
                        }
                    }
                    queryBuilder.append(")");
                }
            }
            
            // Execute query
            queryBuilder.append(" ORDER BY q.questionId");
            if (logger.isInfoEnabled()) {
                logger.info("QUERY: " + queryBuilder.toString());
            }
            List<DbQuestion> questions = dbMgr.selectRowsByQuery(DbQuestion.class,
                    queryBuilder.toString(),
                    startIndex, maxResults);
            
            // convert the questions
            Set<AbstractQuestion> queryQuestions = new LinkedHashSet<AbstractQuestion>(hibernateToGiftSurvey.convertQuestions(questions));
            
            // Filter category
            if (!categories.isEmpty()) {
                filterOutQuestionsByCategory(queryQuestions, query.getQueryData().getQueryCategories());
            }
            
            // filter search criteria
            if(querySearchText != null && !querySearchText.isEmpty()) {
            	filterOutQuestionsByText(queryQuestions, querySearchText);
            }

            List<AbstractQuestion> queryQuestionsList = new ArrayList<AbstractQuestion>(queryQuestions);
            int endIndex = queryQuestionsList.size(); // default to the size of the list
            
            // if we are not doing a fast query, i.e. all filtering done on the DB
            // side then we still need to return a subset of the results and must
            // calculate the indices for the sublist
            if (calculateIndices) {
                //used to find the starting point of the questions to load
                //if the query index start + record count is greater than or equal to the list size, use list size - record count as start index. If that value is negative, use zero.
                startIndex = (query.getQueryRecordIndexStart() + query.getQueryRecordReturnCount() >= queryQuestionsList.size() ? 
                            queryQuestionsList.size() - query.getQueryRecordReturnCount() : query.getQueryRecordIndexStart());
                if(startIndex < 0){
                    startIndex = 0;
                }
                endIndex = query.getQueryRecordReturnCount() == 0 ? queryQuestionsList.size()
                        : clampListIndex(queryQuestionsList, query.getQueryRecordIndexStart() + query.getQueryRecordReturnCount());
            } else {
                startIndex = 0;
            }
            List<AbstractQuestion> subList = new ArrayList<AbstractQuestion>(queryQuestionsList.subList(startIndex, endIndex));
            
            
            for(AbstractQuestion question : subList) {
            	int questionId = question.getQuestionId();
            	boolean locked = SasLockManager.getInstance().isQuestionLocked(questionId);
            	question.setLocked(locked);
            }
            
            return new ListQueryResponse<AbstractQuestion>(subList, query.getQueryRecordIndexStart(), Math.max(queryQuestionsList.size(), numRows));

        } catch (Exception e) {

            logger.error("Caught an exception while getting questions from the database", e);
            return new ListQueryResponse<AbstractQuestion>("Caught an exception while getting questions from the database: " + e.getClass().getName());
        }
    }
    
    /**
     * Queries the server for available survey contexts.
     * Note: this query can be expensive so use caution!!!
     *
     * @param query The query for a list of survey contexts
     * @return ListQueryResponse<GwtSurveyContext> The response to the query
     */
    public static ListQueryResponse<SurveyContext> getSurveyContexts(ListQuery<SurveyContextQueryData> query) {

        try {
            String userName = query.getQueryData().getUserName();
            
            final String queryString = "from DbSurveyContext as SurveyContext where '*' in elements(SurveyContext.visibleToUserNames) or '" + 
                            userName + "' in elements(SurveyContext.visibleToUserNames)";
            int start = query.getQueryRecordIndexStart();
            int numRows = (int)dbMgr.getRowCountByQuery("select count(*) " + queryString, null);
            int maxResults = query.getQueryRecordReturnCount() == 0 ? numRows
                    : Math.min(numRows, query.getQueryRecordReturnCount());
            
            // This needs to be wrapped into a single transaction since the surveycontext is lazy fetch by default.
            Session session = dbMgr.getCurrentSession();
            session.beginTransaction();
            List<DbSurveyContext> dbSurveyContexts = dbMgr.selectRowsByQuery(DbSurveyContext.class,
                    queryString, start, maxResults, session);
            
            for (DbSurveyContext sc : dbSurveyContexts) {
                SurveyContextUtil.getSurveyContextWithoutGeneratedSurveys(sc, dbMgr, session);
            }
            
            session.close();

            Collection<SurveyContext> allSurveyContexts = hibernateToGiftSurvey.convertSurveyContexts(dbSurveyContexts);

            List<SurveyContext> querySurveyContextsList = new ArrayList<SurveyContext>(allSurveyContexts);
            
            return new ListQueryResponse<SurveyContext>(querySurveyContextsList, start, numRows);

        } catch (Exception e) {
            logger.error("Caught an exception while getting survey contexts from the database", e);
            return new ListQueryResponse<SurveyContext>("Caught an exception while getting survey contexts from  the database: " + e.getClass().getName());
        }
    }

    
    /**
	 * Gets a list of images referenced by a given survey
	 * 
	 * @param surveyId - The id of the survey to check for images
	 * @return a list of images the survey contains. Can be empty.
	 */
    public static List<File> getSurveyImages(int surveyId) {
    	
    	ArrayList<File> surveyImages = new ArrayList<File>();
    	
    	Surveys.getSurveyImageReferences(Surveys.getSurvey(surveyId), surveyImages);
    	
    	return surveyImages;
    }
    
    /**
     * Exports a survey to the survey export folder, which can then be imported later.
     * 
     * @param surveyId The ID of the survey to export.
     * @return a response containing the export result.
     */
    public static ExportSurveysResponse exportSurvey(int surveyId) {
        Survey survey = Surveys.getSurvey(surveyId);
    	return exportSurvey(survey);
    }
    
    /**
     * Creates a survey export file for download.
     * 
     * @param survey Survey object to export.
     * @return a response containing the export result.
     */
    public static ExportSurveysResponse exportSurvey(Survey survey) {
    	// Convert to JSON
    	SurveyJSON json = new SurveyJSON();
    	JSONObject obj = new JSONObject();
    	ExportSurveysResponse result = new ExportSurveysResponse();
    	File exportDir = null;
    	File exportZip = null;
    	
    	try {
    		FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss", TimeZone.getDefault(), Locale.getDefault());
    		json.encode(obj, survey);
    		
    		// Strip out special characters
    		String surveyName = survey.getName();
    		surveyName = surveyName.replace("/", "-");
    		surveyName = surveyName.replace("\\", "-");
    		surveyName = surveyName.replace("*", "-");
    		surveyName = surveyName.replace(".", "-");
    		surveyName = surveyName + "_" + fdf.format(new Date()) + ".survey";
    		
    		exportDir = new File(DashboardProperties.getInstance().getDomainDirectory() + File.separator + "temp" + File.separator + surveyName);
    		exportZip = new File(exportDir.getPath() + ".zip");	
    		exportDir.mkdir();
    		exportZip.deleteOnExit();
    		FileUtil.registerFileToDeleteOnShutdown(exportDir);
    		
    		// Write survey to file
    		PrintWriter writer = new PrintWriter(exportDir.getPath() + File.separator + surveyName);
    		writer.println(obj.toJSONString());
    		writer.close();
    		    	
    		// Check for images
    		ArrayList<File> surveyImages = new ArrayList<File>();
    		Surveys.getSurveyImageReferences(survey, surveyImages);

    		if(!surveyImages.isEmpty()) {
    			// copy the images and preserve the file structure
    			File uploadDir = new File(DashboardProperties.getInstance().getSurveyImageUploadDirectory());
    			File imagesDir = new File(exportDir.getPath() + File.separator + uploadDir.getPath());
    			imagesDir.mkdir();

    			for(File image : surveyImages) {
    				String copyPath = image.getPath().replace(uploadDir.getPath(), imagesDir.getPath());
    				File imageCopy = new File(copyPath);
    				FileUtils.copyFile(image, imageCopy);
    			}
    		}

    		ZipUtils.zipFolder(exportDir, exportZip);
    		FileUtils.forceDelete(exportDir);
    		
    		result.setIsSuccess(true);
    		result.setExportDownloadUrl(getExportUrl(exportZip));
    		result.setTempFileToDelete(exportZip.getPath());
    		    		
    	} catch(MessageEncodeException e) {
    		logger.error("Caught exception when trying to export survey", e);
    		result.setResponse("An error occurred while preparing the survey for export. Please make sure the survey is valid.");
    		result.setAdditionalInformation(e.toString());
    		result.setIsSuccess(false);
    	} catch(Exception e) {
    		logger.error("Caught exception when trying to export survey", e);
    		result.setResponse("An error occurred while trying to export the survey.");
    		result.setAdditionalInformation(e.toString());
    		result.setIsSuccess(false);
    	}
    	
    	if(!result.isSuccess()) {
    		FileUtils.deleteQuietly(exportZip);
    		FileUtils.deleteQuietly(exportDir);
    	}
    	
    	return result;
    }
	
	/** Creates a download url to the specified file.
	 * @param exportFile the file to create a download url for.
	 * @return a String containing the download url.
	 */
    private static String getExportUrl(File exportFile) {
    	
    	String networkURL;
    	String filePath = FileFinderUtil.getRelativePath(new File(DashboardProperties.getInstance().getDomainDirectory()), exportFile);
        
        try {
            networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + "/";

        } catch (Exception ex) {

            logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
            networkURL = DashboardProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
        }        
        
        networkURL += filePath;
        
      	return UriUtil.makeURICompliant(networkURL);
    }
    
    /**
     * Imports a survey that was exported using {@link #exportSurvey(int)}. Returns null if the import fails.
     * 
     * @param file The filename of the survey to import.
     * @param username The user that will have access to the survey. If the username is null, the default permissions will be used.
     * @param filesToRename a map of original to new filenames used to resolve filename conflicts with imported image files. Can be null.
     * @return a response containing the import result.
     */
    public static ImportSurveysResponse importSurvey(String file, String username, Map<String, String> filesToRename) {
    	
    	ImportSurveysResponse result;
    	String filePath = DashboardProperties.getInstance().getDomainDirectory() + File.separator + "temp" + File.separator + file;
        File tempFileToDelete = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        tempFileToDelete.deleteOnExit();
        
        try {

        	result = checkImportForConflicts(file, username, filesToRename);
        	if(!result.isSuccess()) {
        		return result;
        	}

        	String surveyFile = filePath;
        	if(surveyFile.endsWith(".zip")) {
        		surveyFile = filePath + File.separator + file.substring(file.lastIndexOf("/"));
        		surveyFile = surveyFile.replace(".zip", "");
        	}
        	String importFileContents = FileUtils.readFileToString(new File(surveyFile));

        	if(filesToRename != null) {
				// replace image name references in the survey 
				Set<String> set = filesToRename.keySet();
				for(String originalName : set) {
					importFileContents = importFileContents.replace(originalName, filesToRename.get(originalName));
				}
			}
			
			if(result.getConflictsList() != null && !result.getConflictsList().isEmpty()) {
				// replace generated folder name references in the survey
				Set<String> set = result.getConflictsList().keySet();
				for(String originalName : set) {
					importFileContents = importFileContents.replace(originalName, result.getConflictsList().get(originalName));
				}
			}
        	
        	JSONObject obj = (JSONObject) JSONValue.parse(importFileContents);
        	SurveyJSON json = new SurveyJSON();
        	Survey survey = (Survey) json.decode(obj);

        	logger.info("Replacing permissions for survey " + survey.getName() + " with " +"<" + username + ">."); 

        	survey.getEditableToUserNames().clear();
        	survey.getVisibleToUserNames().clear();

        	survey.getEditableToUserNames().add(username);
        	survey.getVisibleToUserNames().add(username);

        	Session session = dbMgr.createNewSession();
        	session.beginTransaction();

        	try{
        		DbSurvey newSurvey = Surveys.insertExternalSurvey(survey, null, session);
        		if(newSurvey == null){
        			throw new Exception("The survey was not imported.  Rolling back database transaction.");
        		}

        		session.getTransaction().commit();

        		result.setSurveyId(newSurvey.getSurveyId());
        		result.setSurveyName(newSurvey.getName());
        		result.setIsSuccess(true);

        	}catch(Exception e){
        		logger.error("Caught exception while inserting new survey with original id "+survey.getId()+" and name of "+survey.getName()+".", e);

        		result.setResponse("An error occurred while copying the survey to the database.");
        		result.setAdditionalInformation(e.toString());
        		result.setIsSuccess(false);

        		session.getTransaction().rollback();

        	}finally{
        		session.close();
        		FileUtils.forceDelete(tempFileToDelete);
        	}

        } catch(Exception e) {
        	logger.error("Caught exception when trying to import survey", e);
        	result = new ImportSurveysResponse();
        	result.setResponse("An error occurred while trying to import the survey.");
        	result.setAdditionalInformation(e.toString());
        	result.setIsSuccess(false);
        }
        
        return result;
    }
    
    /**
     * Checks the import file images for potential filename conflicts. If no conflicts are found, the images
     * are copied to the survey images directory.
     * 
     * @param file The path to the import file.
     * @param username used to check for user images (currently not used)
     * @param filesToRename A map of original to new filenames used to resolve filename conflicts. Can be null.
     * @return a result containing information about the import.
     * @throws Exception if there was an error checking the files.
     */
    private static ImportSurveysResponse checkImportForConflicts(String file, String username, Map<String, String> filesToRename) throws Exception {
    	
    	ImportSurveysResponse result = new ImportSurveysResponse();
    	String filePath = DashboardProperties.getInstance().getDomainDirectory() + File.separator + "temp" + File.separator + file;
        File importFile = new File(filePath);
        File tempFileToDelete = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        tempFileToDelete.deleteOnExit();
        
        if(importFile.exists()) {            	
        	File surveyExport = null;
        	if(file.endsWith(".zip")) {

        		// unzip the archive
        		surveyExport = new File(importFile.getPath().replace(".zip", ""));
        		ZipUtils.unzipArchive(importFile, surveyExport, null);
        		result.setTempFileToDelete(tempFileToDelete.getPath());
        		
        		// check for images
        		File imagesDir = new File(surveyExport.getPath() + File.separator + DashboardProperties.getInstance().getSurveyImageUploadDirectory());
        		if(imagesDir.exists()) {

        		    File imageUploadDir = new File(DashboardProperties.getInstance().getSurveyImageUploadDirectory());

                    // check for filename conflicts before importing any images
                    HashMap<String, String> imageConflicts = new HashMap<String, String>();
                    result.setConflictsList(imageConflicts);
                    
                    for(File img : imagesDir.listFiles()) {

                        if(img.isDirectory()) {
                            // handle generated folders silently
                            continue;
                        }
                        
                        String filename = img.getName();
                        if(filesToRename != null && filesToRename.keySet().contains(filename)) {
                            // if a new name was provided for a file, use the new name instead.
                            filename = filesToRename.get(filename);
                        }

                        File conflict = new File(imageUploadDir.getPath() + File.separator + filename);
                        if(!conflict.getParentFile().equals(imageUploadDir) && conflict.exists()) {
                            //a conflict can only happen with images in sub-folders which are generated during image upload, 
                            //not the root upload images folder
                            imageConflicts.put(img.getName(), filename);
                        }
                    }

                    if(!imageConflicts.isEmpty()) {
                        // if there are file conflicts, notify the user.
                        result.setIsSuccess(false);
                        result.setHasFilenameConflicts(true);
                        return result;
                    }

                    // if no conflicts exist, continue with the import
                    for(File img : imagesDir.listFiles()) {
                        
                        if(img.isDirectory()) {
                            File generatedFolder = new File(DashboardProperties.getInstance().getSurveyImageUploadDirectory() + File.separator + img.getName());
                            if(generatedFolder.exists()) {
                                // if there is a folder name conflict, give the new folder a new uuid 
                                File rename = new File(img.getParent() + File.separator + UUID.randomUUID().toString());
                                if(img.renameTo(rename)){
                                    FileUtils.copyDirectoryToDirectory(rename, imageUploadDir);
                                    imageConflicts.put(img.getName(), rename.getName());
                                } else {
                                    logger.error("Couldn't rename file \"" + img.getPath() + "\" to \"" + rename.getPath() + "\"");
                                }
                            } else {
                                FileUtils.copyDirectoryToDirectory(img, imageUploadDir);
                            }
                            
                        } else if(filesToRename != null && filesToRename.keySet().contains(img.getName())) {
                            // if this image name is in the list of files to rename, rename it before copying
                            File rename = new File(img.getParent() + File.separator + filesToRename.get(img.getName()));
                            if(img.renameTo(rename)){
                                FileUtils.copyFileToDirectory(rename, imageUploadDir);
                            } else {
                                logger.error("Couldn't rename file \"" + img.getPath() + "\" to \"" + rename.getPath() + "\"");
                            }
                        } else {
                            FileUtils.copyFileToDirectory(img, imageUploadDir);
                        }
                    }//end for

                }
            }
        	
        	result.setIsSuccess(true);
        	
        } else {
        	result.setIsSuccess(false);
        	result.setResponse("The import file does not exist on the server. Please upload the file and try again.");
        }
        
        return result;
    }   
    
    /**
     * Queries the server for available surveys
     *
     * @param query The query for a list of surveys (NOTE: The userName MUST be set as a filter either by an explicit
     *         username, OR by using the '*' method to filter on Public surveys.  The other parameters for the SurveyQueryData
     *         such as setting the QueryRecordIndexStart, QueryRecordReturnCount, or the QueryFolders is NOT supported for this method.
     * @return ListQueryResponse<SurveyHeader> The response to the query
     */
    public static ListQueryResponse<SurveyHeader> getSurveys(ListQuery<SurveyQueryData> query) {

        final int UNUSED_INDEX = -1;
        try {
            
            // Validate the request filter parameters.  At this time, only the userName is expected to be set (and it must be set).
            // The other parameters are not supported, so an error is returned to let the caller know that they should not be used for this 
            // method.
            if (query.getQueryRecordIndexStart() > 0) {
                logger.error("SurveyDb::getSurveys - Setting the QueryRecordIndexStart is not supported for this method.");
                return new ListQueryResponse<SurveyHeader>("Setting the QueryRecordIndexStart is not supported for this method.");
            }
            
            if (query.getQueryRecordReturnCount() > 0) {
                logger.error("SurveyDb::getSurveys - Setting the QueryRecordReturnCount is not supported for this method.");
                return new ListQueryResponse<SurveyHeader>("Setting the QueryRecordReturnCount is not supported for this method.");
            }
            
            if (query.getQueryData().getQueryFolders() != null && !query.getQueryData().getQueryFolders().isEmpty()) {
                logger.error("SurveyDb::getSurveys - Setting the QueryFolders is not supported for this method.");
                return new ListQueryResponse<SurveyHeader>("Setting the QueryFolders is not supported for this method.");
            }
            
        	String userName = query.getQueryData().getUserName();
        	
        	if (userName == null || userName.isEmpty()) {
        	    logger.error("SurveyDb::getSurveys - Username must be set in the query for a specific user, or use '*' to filter on Public surveys.");
        	    return new ListQueryResponse<SurveyHeader>("Username must be set in the query for a specific user, or use '*' to filter on Public surveys.");
        	}

        	final String queryString = "from DbSurvey as Survey where '" + userName + "' in elements(Survey.visibleToUserNames)";

           
        	if(logger.isDebugEnabled()) {
        	    logger.debug("QueryString = " + queryString);
        	}
        	List<DbSurvey> dbSurveys = dbMgr.selectRowsByQuery(DbSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX);
        	if(logger.isDebugEnabled()) {
        	    logger.debug("Result size = " + dbSurveys.size());
        	}
            Iterator<DbSurvey> dbSurveyIterator = dbSurveys.iterator();
            Map<DbSurvey, SurveyProperties> surveyToProperties = new HashMap<>();
            while(dbSurveyIterator.hasNext()){
            	
            	DbSurvey dbSurvey = dbSurveyIterator.next();
            	if(logger.isDebugEnabled()) {
            	    logger.debug("DbSurvey found survey: " + dbSurvey.getName());
            	}
            	Set<String> visibleToUserNames = dbSurvey.getVisibleToUserNames();
        		if(!visibleToUserNames.contains(userName)) { 
        			dbSurveyIterator.remove();
        			dbSurveys.remove(dbSurvey);
        			continue;
        		}
            	
            	SurveyProperties surveyProperties = hibernateToGiftSurvey.convertSurveyProperties(dbSurvey.getProperties());
            	
            	if(surveyProperties.hasProperty(SurveyPropertyKeyEnum.UNPRESENTABLE)
            			&& surveyProperties.getBooleanPropertyValue(SurveyPropertyKeyEnum.UNPRESENTABLE)){
            		
            		//unpresentable surveys should not be sent back to the client
            		dbSurveyIterator.remove();
            		dbSurveys.remove(dbSurvey);
            		continue;
            	}
            	
            	if(query.getQueryData().getSurveyType() != null 
            	        && !query.getQueryData().getSurveyType().equals(surveyProperties.getSurveyType())) {
            	    
            	    //surveys that are not the right type should not be sent back to the client
                    dbSurveyIterator.remove();
                    dbSurveys.remove(dbSurvey);
                    continue;
            	}
            	
            	surveyToProperties.put(dbSurvey, surveyProperties);
            	    
            }

            
            int totalRecordCount = dbSurveys.size();
            List<SurveyHeader> headers = new ArrayList<SurveyHeader>();
            
            for(DbSurvey dbSurvey : surveyToProperties.keySet()) {
                
                SurveyProperties surveyProperties = surveyToProperties.get(dbSurvey);
                
                String folder = dbSurvey.getFolder() == null ? null : dbSurvey.getFolder().getName();
                
                int pageCount = dbSurvey.getSurveyPages().size();
                
                int elementCount = 0, questionCount = 0;
                
                for(DbSurveyPage surveyPage : dbSurvey.getSurveyPages()) {
                    
                    elementCount += surveyPage.getSurveyElements().size();
                    
                    // only count questions, not text elements
                    for(mil.arl.gift.ums.db.table.DbSurveyElement sElement : surveyPage.getSurveyElements()) {
                        SurveyElementTypeEnum surveyElementType = SurveyElementTypeEnum.valueOf(sElement.getSurveyElementType().getKey());

                        if(SurveyElementTypeEnum.QUESTION_ELEMENT.equals(surveyElementType)) {
                            questionCount++;
                        }
                    }
                }
                
                SurveyTypeEnum surveyType = surveyProperties.getSurveyTypePropertyOrSetIt(dbSurvey.getName());
                Set<LearnerStateAttributeNameEnum> learnerStateAttrs = surveyProperties.getScoredAttributes();
               
                SurveyHeader header = new SurveyHeader(dbSurvey.getSurveyId(), folder, dbSurvey.getName(), 
                        pageCount, elementCount, questionCount, surveyType, learnerStateAttrs, dbSurvey.getVisibleToUserNames(), dbSurvey.getEditableToUserNames());
                
                // Set the lock flag.
                int id = header.getId();
                boolean locked = SasLockManager.getInstance().isSurveyLocked(id);
                header.setLocked(locked);
                
            	headers.add(header);
            }

            return new ListQueryResponse<SurveyHeader>(headers, totalRecordCount);

        } catch (Exception e) {
            logger.error("Caught an exception while getting surveys from the database", e);
            return new ListQueryResponse<SurveyHeader>("Caught an exception while getting surveys from  the database: " + e.getClass().getName());
        }
    }


    /**
     * Queries the server for available option lists
     *
     * @param query The query for a list of option lists
     * @return ListQueryResponse<GwtOptionList> The response to the query
     */
    public static ListQueryResponse<OptionList> getOptionLists(ListQuery<OptionListQueryData> query) {

        try {

            Set<OptionList> queryOptionLists = new LinkedHashSet<OptionList>();

            if (query.getQueryData().isShowSharedOnly()) {                
                
                // if we only want shared option lists, pull from the database where shared = true and where the visibleToUserNames contains the 'everyone' wildcard.
                String queryString = "from DbOptionList as optionList where optionList.isShared = " + query.getQueryData().isShowSharedOnly()
                        + " and '" + Constants.VISIBILITY_WILDCARD + "' in elements(optionList.visibleToUserNames)";

                //get the list of existing option lists so that they can be checked to find one identical to optionListValue
                List<DbOptionList> dbOptionLists = dbMgr.selectRowsByQuery(DbOptionList.class, queryString, Surveys.UNUSED_INDEX, Surveys.UNUSED_INDEX);
                
                Collection<OptionList> allOptionLists = hibernateToGiftSurvey.convertOptionLists(dbOptionLists);

                queryOptionLists.addAll(allOptionLists);

                /* note: we do not need to use the username filter for shared option lists because
                 * these grant visible access to everyone. */
            } else {

                // Shared and unshared option lists have to be queried individually in order to get the set of all option lists

                DbOptionList exampleOptionList = new DbOptionList();
                exampleOptionList.setIsShared(true);

                List<DbOptionList> dbOptionLists = dbMgr.selectRowsByExample(exampleOptionList, DbOptionList.class);
                Collection<OptionList> allOptionLists = hibernateToGiftSurvey.convertOptionLists(dbOptionLists);

                exampleOptionList.setIsShared(false);

                dbOptionLists = dbMgr.selectRowsByExample(exampleOptionList, DbOptionList.class);
                allOptionLists.addAll(hibernateToGiftSurvey.convertOptionLists(dbOptionLists));

                queryOptionLists.addAll(allOptionLists);

                // filter out the option lists that don't have visible access for the username.
                String userName = query.getQueryData().getUserName();
                if(userName != null && !userName.isEmpty()) {
                    filterOutOptionListsByUserName(queryOptionLists, userName);
                }
            }

            List<OptionList> queryOptionListList = new ArrayList<OptionList>(queryOptionLists);

            List<OptionList> subList = new ArrayList<OptionList>(queryOptionListList.subList(
                    query.getQueryRecordIndexStart(),
                    query.getQueryRecordReturnCount() == 0
                    ? queryOptionListList.size()
                    : clampListIndex(queryOptionListList, query.getQueryRecordIndexStart() + query.getQueryRecordReturnCount())));

            return new ListQueryResponse<OptionList>(subList, query.getQueryRecordIndexStart(), queryOptionListList.size());

        } catch (Exception e) {

            logger.error("Caught an exception while getting option lists from the database", e);
            return new ListQueryResponse<OptionList>("Caught an exception while getting answer sets from  the database: " + e.getClass().getName());
        }
    }


    /**
     * Queries the server for available folders
     *
     * @param query The query for a list of folders
     * @return ListQueryResponse<GwtFolder> The response to the query
     */
    public static ListQueryResponse<Folder> getFolders(ListQuery<FolderQueryData> query) {
        try {

            Set<Folder> queryFolders = new LinkedHashSet<Folder>();

            List<DbFolder> dbFolders = dbMgr.selectAllRows(DbFolder.class);
            
            String userName = query.getQueryData().getUserName();
            Iterator<DbFolder> iterator = dbFolders.iterator();
            while(iterator.hasNext()) {
            	Set<String> userNames = iterator.next().getVisibleToUserNames();
            	if(!userNames.contains(userName) && !userNames.contains("*")) {
            		iterator.remove();
            	}
            }
            
            queryFolders.addAll(hibernateToGiftSurvey.convertFolders(dbFolders));

            List<Folder> queryFoldersList = new ArrayList<Folder>(queryFolders);

            List<Folder> subList = new ArrayList<Folder>(queryFoldersList.subList(
                    query.getQueryRecordIndexStart(),
                    query.getQueryRecordReturnCount() == 0
                    ? queryFoldersList.size()
                    : clampListIndex(queryFoldersList, query.getQueryRecordIndexStart() + query.getQueryRecordReturnCount())));

            return new ListQueryResponse<Folder>(subList, query.getQueryRecordIndexStart(), queryFoldersList.size());

        } catch (Exception e) {

            logger.error("Caught an exception while getting folders from the database", e);
            return new ListQueryResponse<Folder>("Caught an exception while getting folders from the database: " + e.getClass().getName());
        }
    }

    /**
     * Queries the server for available categories
     *
     * @param query The query for a list of categories
     * @return ListQueryResponse<Category> The response to the query
     */
    public static ListQueryResponse<Category> getCategories(ListQuery<CategoryQueryData> query) {

        try {

            Set<Category> queryCategories = new LinkedHashSet<Category>();

            List<DbCategory> dbCategories = dbMgr.selectAllRows(DbCategory.class);
            
            String userName = query.getQueryData().getUserName();
            Iterator<DbCategory> iterator = dbCategories.iterator();
            while(iterator.hasNext()) {
            	Set<String> userNames = iterator.next().getVisibleToUserNames();
            	if(!userNames.contains(userName) && !userNames.contains("*")) {
            		iterator.remove();
            	}
            }
            
            queryCategories.addAll(hibernateToGiftSurvey.convertCategories(dbCategories));

            List<Category> queryCategoriesList = new ArrayList<Category>(queryCategories);

            List<Category> subList = new ArrayList<Category>(queryCategoriesList.subList(
                    query.getQueryRecordIndexStart(),
                    query.getQueryRecordReturnCount() == 0
                    ? queryCategoriesList.size()
                    : clampListIndex(queryCategoriesList, query.getQueryRecordIndexStart() + query.getQueryRecordReturnCount())));

            return new ListQueryResponse<Category>(subList, query.getQueryRecordIndexStart(), queryCategoriesList.size());

        } catch (Exception e) {

            logger.error("Caught an exception while getting categories from the database", e);
            return new ListQueryResponse<Category>("Caught an exception while getting categories from the database: " + e.getClass().getName());
        }
    }


    /**
     * Gets the print out of dependencies for a survey, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param surveyId The ID of the survey
     * @param ignoreSurveyContextId The id of a survey context to ignore (this typically will be the survey context that that the survey belongs to).
     *        This is optional and can be set to 0.
     * @return String The print out of dependencies for a survey, "none" if there
     * are no dependencies, "response", if the survey already has responses or null
     * for an error.
     */
    public static SurveyDependencies getSurveyDependencies(int surveyId, int ignoreSurveyContextId) {
        try {
            final int UNUSED_INDEX = -1;

            String queryString = "from DbSurveyContextSurvey as context where context.survey.id = " + surveyId;
            logger.debug("queryString = " + queryString);
            List<DbSurveyContextSurvey> dbSurveyContextSurveys = dbMgr.selectRowsByQuery(DbSurveyContextSurvey.class, queryString, UNUSED_INDEX, UNUSED_INDEX);

            Set<DbSurveyContext> dbSurveyContexts = new HashSet<DbSurveyContext>();
            for (DbSurveyContextSurvey dbSurveyContextSurvey : dbSurveyContextSurveys) {
                
                if (ignoreSurveyContextId != 0 && dbSurveyContextSurvey.getSurveyContext().getSurveyContextId() == ignoreSurveyContextId) {
                    // Ignore the survey context id from being added to the list of dependencies.
                    continue;
                }
                
                if (dbSurveyContextSurvey.getSurvey().getSurveyId() == surveyId) {
                    dbSurveyContexts.add(dbSurveyContextSurvey.getSurveyContext());
                }
            }
            int surveyResponseCount = 0;
            
            queryString = "from DbSurveyResponse as response where response.survey.id = " + surveyId;
            logger.debug("queryString = " + queryString);
            List<DbSurveyResponse> dbSurveyResponses = dbMgr.selectRowsByQuery(DbSurveyResponse.class, queryString, UNUSED_INDEX, UNUSED_INDEX);
            for (DbSurveyResponse dbSurveyResponse : dbSurveyResponses) {
                if (dbSurveyResponse.getSurvey().getSurveyId() == surveyId) {
                    surveyResponseCount += 1;
                }
            }
            
            // Survey already has responses
            if(surveyResponseCount > 0) {
            	return new SurveyDependencies(true, null);
            }
            
            if (!dbSurveyContexts.isEmpty() || surveyResponseCount > 0) {
                StringBuilder dependenciesString = new StringBuilder();
                if (!dbSurveyContexts.isEmpty()) {
                    dependenciesString.append(dbSurveyContexts.size());
                    if (dbSurveyContexts.size() > 1) {
                        dependenciesString.append(" Survey Contexts reference");
                    } else {
                        dependenciesString.append(" Survey Context references");
                    }
                    dependenciesString.append(" this survey.");
                    dependenciesString.append("<ul>");
                    for (DbSurveyContext dbSurveyContext : dbSurveyContexts) {
                        dependenciesString.append("<li>").append(dbSurveyContext.getName()).append("</li>");
                    }
                    dependenciesString.append("</ul>");
                    dependenciesString.append("Editing this survey will affect future presentations of ");
                    if (dbSurveyContexts.size() > 1) {
                        dependenciesString.append("those survey contexts!");
                    } else {
                        dependenciesString.append("that survey context!");
                    }
                    dependenciesString.append("<br/>");
                }
                if (surveyResponseCount > 0) {
                    dependenciesString.append(surveyResponseCount);
                    if (surveyResponseCount > 1) {
                        dependenciesString.append(" Survey Responses reference");
                    } else {
                        dependenciesString.append(" Survey Response references");
                    }
                    dependenciesString.append(" this survey. Editing this survey will cause future survey responses to be different!<br/>");
                }
                return new SurveyDependencies(false, dependenciesString.toString());
            } else {
                return new SurveyDependencies();
            }
            
            
        } catch (Exception e) {
            logger.error("Caught an exception while getting the dependencies for a survey", e);
        }
        return null;
    }

    /**
     * Gets the print out of dependencies for a option list, returning "none" if
     * there are no dependencies or null if there was error.  A dependency for an option list includes questions
     * and question responses that reference the option list.
     *
     * @param optionListId The ID of the option list
     * @return String The print out of dependencies for a option list.  Will return null if there was an error.  Will return "none"
     * if no dependencies where found.
     */
    public static String getOptionListDependencies(int optionListId) {

        try {            
            // Retrieve the number of question properties that reference the option list
            // A question property value id is unique to a question, therefore it provides the count of questions using the option list
            String questionPropValueQueryString = "from DbQuestionPropertyValue as qPropValue where qPropValue.optionListValue.optionListId = "+optionListId;
            List<DbQuestionPropertyValue> props = dbMgr.selectRowsByQuery(DbQuestionPropertyValue.class, questionPropValueQueryString, -1, -1);
            int questionCount = props.size();

            // Retrieve the number of responses that reference the option list in either the row text option list or text option list column
            String responseOptionListQueryString = "from DbQuestionResponse as response where response.textOptionList.optionListId = "+optionListId+" or response.rowTextOptionList.optionListId = "+optionListId;
            List<DbQuestionResponse> responses = dbMgr.selectRowsByQuery(DbQuestionResponse.class, responseOptionListQueryString, -1, -1);
            int responseCount = responses.size();            
            
            if (questionCount > 0 || responseCount > 0) {
                StringBuilder dependenciesString = new StringBuilder();
                if (questionCount > 0) {
                    dependenciesString.append(questionCount);
                    if (questionCount > 1) {
                        dependenciesString.append(" Questions reference");
                    } else {
                        dependenciesString.append(" Question references");
                    }
                    dependenciesString.append(" this answer set. ");
                    dependenciesString.append("Editing this answer set will affect future presentations of ");
                    if (questionCount > 1) {
                        dependenciesString.append("questions ");
                    } else {
                        dependenciesString.append("this question ");
                    }
                    dependenciesString.append("with this answer set and the responses to ");
                    if (questionCount > 1) {
                        dependenciesString.append("them ");
                    } else {
                        dependenciesString.append("it ");
                    }
                    dependenciesString.append("will be different!<br/>");
                }
                if (responseCount > 0) {
                    dependenciesString.append(responseCount);
                    if (responseCount > 1) {
                        dependenciesString.append(" Responses reference");
                    } else {
                        dependenciesString.append(" Response references");
                    }
                    dependenciesString.append(" this option list.<br/> Editing this answer set will cause future responses to be different!<br/>");
                }
                return dependenciesString.toString();
            } else {
                return "none";
            }
        } catch (Exception e) {
            logger.error("Caught an exception while getting the dependencies for a option list", e);
        }
        return null;
    }

    /**
     * Creates a backup of the database on the server
     *
     * @param backupUserData If user data should be backed up
     * @return Boolean If the operation was successful
     */
    public static boolean backupDatabase(boolean backupUserData) {

        if (DashboardProperties.getInstance().getDatabaseBackupsDirectory() != null) {

            FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss", TimeZone.getDefault(), Locale.getDefault());

            // optional list of specific tables to backup
            List<Class<? extends Object>> tableClasses = null;
            String backupFileName = null;
            if (backupUserData) {
                //backup entire database
                backupFileName = DashboardProperties.getInstance().getDatabaseBackupsDirectory() + "/umsDataPlusUserBackup-" + fdf.format(new Date());
            }else{
                //backup the survey content only
                tableClasses = UMSDatabaseManager.getSurveyDataTableClasses();
                backupFileName = DashboardProperties.getInstance().getDatabaseBackupsDirectory() + "/umsDataBackup-" + fdf.format(new Date());
            }
            
            try {
                
                UMSDatabaseManager.getInstance().backupDatabase(backupFileName, tableClasses, UMSDatabaseManager.getPermissionsTableNames());
                return true;

            } catch (Exception e) {
                logger.error("Could not backup the database data because an exception was thrown.", e);
            }

        } else {
            logger.error("Could not backup the database, the database backups directory property is undefined.");
        }

        return false;
    }

    /**
     * Restores the database with a backup on the server
     *
     * @param backupFileName The backup file to restore to
     * @return Boolean If the operation was successful
     */
    public static boolean restoreDatabase(String backupFileName) {
        
        if (DashboardProperties.getInstance().getDatabaseBackupsDirectory() != null) {
                            
            File databaseBackupsDirectory = new File(DashboardProperties.getInstance().getDatabaseBackupsDirectory());
            if (databaseBackupsDirectory.isDirectory()) {
                
                for (File file : databaseBackupsDirectory.listFiles()) {
                    
                    if (file.isFile() && file.getName().equals(backupFileName)) {

                        String modifiedBackupFileName = DashboardProperties.getInstance().getDatabaseBackupsDirectory() + File.separator + backupFileName;
                        try {
                            
                            UMSDatabaseManager.getInstance().restoreDatabase(
                            		new File(modifiedBackupFileName), 
                            		UMSDatabaseManager.getTableNames(UMSDatabaseManager.getSurveyDataTableClasses()), 
                            		UMSDatabaseManager.getTableNames(UMSDatabaseManager.getSurveyResponseTableClasses()),
                            		UMSDatabaseManager.getPermissionsTableNames());
                            
                            return true;
                            
                        } catch (Exception e) {
                            logger.error("Could not restore the database because an exception was thrown.", e);
                        }
                    }
                }
                return false;
            } else {
                logger.error("Could not restore the database backup, '" + DashboardProperties.getInstance().getDatabaseBackupsDirectory() + "' is not a directory");
            }

        } else {
            logger.error("Could not restore the database, the database backups directory property is undefined.");
        }
        return false;
    }

    /**
     * Clamps an index to be within the bounds of a list
     *
     * @param <T> The type of list
     * @param list The list for the index to be clamped within
     * @param value The index
     * @return int The clamped index
     */
    private static <T> int clampListIndex(List<T> list, int value) {

        if (value < 0) {

            return 0;

        } else {

            return value > list.size() ? list.size() : value;
        }
    }
    
    private static void filterOutOptionListsByUserName(Collection<OptionList> sourceList, String userName) {
    	Set<OptionList> toKeep = new HashSet<OptionList>();
    	
    	for(OptionList item : sourceList) {
    		HashSet<String> visibleToUserNames = item.getVisibleToUserNames();
    		if(visibleToUserNames.contains(userName) || visibleToUserNames.contains("*")) { 
    			toKeep.add(item);
    		}
    	}
    	
    	sourceList.retainAll(toKeep);
    }
    
    @SuppressWarnings("unused")
    private static void filterOutQuestionsByUserName(Collection<AbstractQuestion> sourceList, String userName) {
    	Set<AbstractQuestion> toKeep = new HashSet<AbstractQuestion>();
    	
    	for(AbstractQuestion item : sourceList) {
    		HashSet<String> visibleToUserNames = item.getVisibleToUserNames();
    		if(visibleToUserNames.contains(userName) || visibleToUserNames.contains("*")) { 
    			toKeep.add(item);
    		}
    	}
    	
    	sourceList.retainAll(toKeep);
    }

    private static void filterOutQuestionsByCategory(Collection<AbstractQuestion> sourceList, List<String> categoriesToKeep) {

        Set<AbstractQuestion> toKeep = new HashSet<AbstractQuestion>();

        for (AbstractQuestion question : sourceList) {

            for (String questionCategory : question.getCategories()) {

                if (categoriesToKeep.contains(questionCategory)) {

                    toKeep.add(question);
                }
            }
        }

        sourceList.retainAll(toKeep);
    }

    @SuppressWarnings("unused")
    private static void filterOutQuestionsByQuestionType(Collection<AbstractQuestion> sourceList, List<QuestionTypeEnum> questionTypesToKeep) {

        Set<AbstractQuestion> toRemove = new HashSet<AbstractQuestion>();

        for (AbstractQuestion question : sourceList) {

            QuestionTypeEnum questionType = QuestionTypeEnum.valueOf(question);

            if (!questionTypesToKeep.contains(questionType)) {

                toRemove.add(question);
            }
        }

        sourceList.removeAll(toRemove);
    }
    
    /**
     * Filters out a collection of questions based on whether or not their text matches the specified search text
     * 
     * @param sourceList The collection of questions to be filtered
     * @param searchText The list of search text strings used to filter the collection
     */
    private static void filterOutQuestionsByText(Collection<AbstractQuestion> sourceList, List<String> searchText) {
    	
        Set<AbstractQuestion> toKeep = new HashSet<AbstractQuestion>();     

    	Session session = dbMgr.createNewSession();
    	try{        	
        	session.beginTransaction();
        	
        	for(String s : searchText){
        		if(s.isEmpty()){
        			//If the search text is empty, skip filtering, since all questions have text containing the empty string
        			session.close();
        			return;
        		}
        		toKeep.addAll(hibernateToGiftSurvey.convertQuestions(dbMgr.selectRowsByText(DbQuestion.class, session, "text", s)));
        	}
    	}finally{  
    	    
    	    if(session != null && session.isOpen()){
    	        session.close();
    	    }
    	}
    	
    	//Since the questions to keep were obtained from the database, they are separate instances of the questions in the source list.
    	//Because of this, sourceList.retainAll cannot be used here, as it only compares references for non-primitives.
    	for(Iterator<AbstractQuestion> it = sourceList.iterator(); it.hasNext();){
    		
    		AbstractQuestion question = it.next();
    		
    		boolean shouldKeep = false;
    		
    		for(AbstractQuestion keepQuestion : toKeep){
    			if(question.getQuestionId() == keepQuestion.getQuestionId()){
    				shouldKeep = true;
    				break;
    			}
    		}
    		
    		if(!shouldKeep){
    			it.remove();
    		}
    	}
    }
    
    @SuppressWarnings("unused")
    private static <T> List<T> cullList(List<T> sourceList, ListQuery<? extends IsSerializable> query) {

        List<T> subList = new ArrayList<T>(sourceList.subList(
                query.getQueryRecordIndexStart(),
                query.getQueryRecordReturnCount() == 0
                ? sourceList.size()
                : clampListIndex(sourceList, query.getQueryRecordIndexStart() + query.getQueryRecordReturnCount())));
        
        return subList;
    }

    /**
     * Queries a list of surveys for those that are in a folder
     *
     * @param sourceList The list of surveys to check/filter
     * @param folders The folders to determine which surveys are within
     * @return List<DbSurvey> The filtered list of surveys that are in at least one of the folders.
     */
    @SuppressWarnings("unused")
    private static List<DbSurvey> querySurveyList(Collection<DbSurvey> sourceList, List<String> folders) {
        
        if(folders == null) {            
            throw new IllegalArgumentException("The list of folders cannot be null");
        }

        List<DbSurvey> queryResults = new ArrayList<DbSurvey>();

        for (DbSurvey sourceRecord : sourceList) {

            if (folders.isEmpty() && sourceRecord.getFolder() == null) {  
                //no folders provided and the survey is not associated with a folder
               queryResults.add(sourceRecord);
                
            } else {
                
                DbFolder dbFolder = sourceRecord.getFolder();
                
                //search all provided folders to determine if the survey is in at least one of them
                for(String folder : folders) {
                    
                    if(dbFolder != null && folder != null && folder.equals(dbFolder.getName())) {                      
                        //found matching folder name
                        
                        queryResults.add(sourceRecord);                        
                        break;
                        
                    } else if(dbFolder == null && folder == null){
                    	//found the survey to be in the 'no folder' folder which was provided as a folder to match
                        
                    	queryResults.add(sourceRecord);                        
                        break;                   	
                    }
                }                
            }
        }

        return queryResults;
    }

	/**
	 * Imports a survey context from a file. Returns null if the import fails.
	 * @param file - the survey context file
	 * @param username - the user importing the survey context
	 * @param filesToRename a map of original to new filenames used to resolve filename conflicts with imported image files. Can be null.
	 * @return a response containing the import result.
	 */
	public static ImportSurveysResponse importSurveyContext(String file, String username, Map<String, String> filesToRename) {

		ImportSurveysResponse result;
		// Retrieve the file
		String filePath = DashboardProperties.getInstance().getDomainDirectory() + File.separator + "temp" + File.separator + file;
		File tempFileToDelete = new File(filePath.substring(0, filePath.lastIndexOf("/")));
		tempFileToDelete.deleteOnExit();

		try {
			
			result = checkImportForConflicts(file, username, filesToRename);
			if(!result.isSuccess()) {
				return result;
			}
			
			String surveyFile = filePath;
        	if(surveyFile.endsWith(".zip")) {
        		surveyFile = filePath + File.separator + file.substring(file.lastIndexOf("/"));
        		surveyFile = surveyFile.replace(".zip", "");
        	}
        	String importFileContents = FileUtils.readFileToString(new File(surveyFile));
        	
			if(filesToRename != null) {
				// replace image name references in the survey 
				Set<String> set = filesToRename.keySet();
				for(String originalName : set) {
					importFileContents = importFileContents.replace(originalName, filesToRename.get(originalName));
				}
			}
			
			if(result.getConflictsList() != null && !result.getConflictsList().isEmpty()) {
				// replace generated folder name references in the survey
				Set<String> set = result.getConflictsList().keySet();
				for(String originalName : set) {
					importFileContents = importFileContents.replace(originalName, result.getConflictsList().get(originalName));
				}
			}

			// Convert the file to a JSONObject
			JSONObject obj = (JSONObject) JSONValue.parse(importFileContents);
			SurveyContextJSON json = new SurveyContextJSON();
			// Decode the SurveyContext
			SurveyContext context = (SurveyContext) json.decode(obj);

			logger.info("Replacing permissions for survey context " + context.getName() + " with " +"<" + username + ">."); 

			context.getEditableToUserNames().clear();
			context.getVisibleToUserNames().clear();

			context.getEditableToUserNames().add(username);
			context.getVisibleToUserNames().add(username);

			// Insert the SurveyContext into the database
			Session session = dbMgr.createNewSession();
			session.beginTransaction();
			try{
				SurveyContext newSurvey = Surveys.insertExternalSurveyContext(context, new ExternalSurveyMapper(), null, session);

				if(newSurvey == null){
					throw new Exception("The survey was not imported.  Rolling back database transaction.");
				}

				session.getTransaction().commit();
				result.setSurveyId(newSurvey.getId());
				result.setSurveyName(newSurvey.getName());
				result.setIsSuccess(true);

			}catch(Exception e){
				// Rollback the database if there was an error
				logger.error("Caught exception while inserting new survey with original id "+context.getId()+" and name of "+context.getName()+".", e);

				result.setResponse("An error occurred while copying the survey context to the database.");
				result.setAdditionalInformation(e.toString());
				result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
				result.setIsSuccess(false);

                if (session.isOpen()) {
                    session.getTransaction().rollback();
                }

			}finally{
                if (session.isOpen()) {
                    session.close();
                }
				FileUtils.forceDelete(tempFileToDelete);
			}

		} catch(Exception e) {
			logger.error("Caught exception when trying to import survey", e);
			result = new ImportSurveysResponse();
			result.setResponse("An error occurred while trying to import the survey context.");
			result.setAdditionalInformation(e.toString());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
			result.setIsSuccess(false);
		}

		return result;
	}

	/**
	 * Exports a survey context
	 * @param context - the survey context to export
	 * @return a response containing the status of the export.
	 */
	public static ExportSurveysResponse exportSurveyContext(SurveyContext context){
		// Convert to JSON
    	SurveyContextJSON json = new SurveyContextJSON();
    	JSONObject obj = new JSONObject();
    	ExportSurveysResponse result = new ExportSurveysResponse();
    	File exportDir = null;
    	File exportZip = null;
    	
    	try {
    		FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss", TimeZone.getDefault(), Locale.getDefault());
    		json.encode(obj, context);
    		
    		// Strip out special characters
    		String surveyContextName = context.getName();
    		surveyContextName = surveyContextName.replace("/", "-");
    		surveyContextName = surveyContextName.replace("\\", "-");
    		surveyContextName = surveyContextName.replace("*", "-");
    		surveyContextName = surveyContextName.replace(".", "-");
    		surveyContextName = surveyContextName + "_" + fdf.format(new Date()) + FileUtil.SURVEY_REF_EXPORT_SUFFIX;
    		
    		exportDir = new File(DashboardProperties.getInstance().getDomainDirectory() + File.separator + "temp" + File.separator + surveyContextName);
    		exportZip = new File(exportDir.getPath() + ".zip");
    		exportDir.mkdir();
    		FileUtil.registerFileToDeleteOnShutdown(exportDir);
    		exportZip.deleteOnExit();
    		
    		// check the surveys for images
    		ArrayList<File> images = new ArrayList<File>();
    		for (SurveyContextSurvey scs : context.getContextSurveys()) {
    			images.addAll(getSurveyImages(scs.getSurvey().getId()));    			
    		}
    		
    		// add the images to the export
    		if(!images.isEmpty()) {
    			File uploadDir = new File(DashboardProperties.getInstance().getSurveyImageUploadDirectory());
    			File imagesDir = new File(exportDir.getPath() + File.separator + uploadDir.getPath());
    			imagesDir.mkdir();
    			
    			for(File image : images) {
    				String copyPath = image.getPath().replace(uploadDir.getPath(), imagesDir.getPath());
    				File imageCopy = new File(copyPath);
    				FileUtils.copyFile(image, imageCopy);
    			}
    		}
    		
    		// Write to file
    		String surveyContextExportFile = exportDir.getPath() + File.separator + surveyContextName;
    		PrintWriter writer = new PrintWriter(surveyContextExportFile);
    		writer.println(obj.toJSONString());
    		writer.close();
    		
    		ZipUtils.zipFolder(exportDir, exportZip);
    		FileUtils.forceDelete(exportDir);
    		
    		result.setIsSuccess(true);
    		result.setExportDownloadUrl(getExportUrl(exportZip));
    		result.setTempFileToDelete(exportZip.getPath());
    		
    	} catch(MessageEncodeException e) {
    		logger.error("Caught exception when trying to export survey context", e);
    		result.setResponse("An error occurred while preparing the survey context for export. Please make sure the survey context surveys are valid.");
    		result.setAdditionalInformation(e.toString());
    		result.setIsSuccess(false);
    	} catch(Exception e) {
    		logger.error("Caught exception when trying to export survey context", e);
    		result.setResponse("An error occurred while trying to export the survey context.");
    		result.setAdditionalInformation(e.toString());
    		result.setIsSuccess(false);
    	}
    	
    	if(!result.isSuccess()) {
    		FileUtils.deleteQuietly(exportZip);
    		FileUtils.deleteQuietly(exportDir);
    	}
    	
    	return result;
	}

	/**
	 * Exports a survey context
	 * @param contextId - the id of the survey context to export
	 * @return a response containing the status of the export.
	 */
	public static ExportSurveysResponse exportSurveyContext(int contextId){
    	return exportSurveyContext(Surveys.getSurveyContext(contextId));
	}
    
    /**
     * Copies the surveys from a source survey context and adds the new surveys to the destination survey context.
     * If the user doesn't have permission to copy the source survey then that survey will be skipped.
     * 
     * @param srcSurveyContextId - The source survey context where the surveys will be copied from (should be a valid non null survey context).
     * @param srcSurveyContextGiftKey - collection of unique gift keys found in the source survey context that map to surveys to copy into
     * the destination survey context.  If null or empty this method return null.
     * @param destSurveyContextId - The destination survey context where the new surveys will be added to (should be valid non null survey context).
     * @param username - The username that will have permissions for the surveys that are created.
     * @return CopySurveyContextResult - Contains the updated survey context along with a mapping of original gift survey context keys to the new values
     *                                   in the destination survey context.  
     *                                   The result will be non-null if successful, otherwise null is returned if the survey(s) couldn't be copied.
     */
    public static CopySurveyContextResult copySurveyContext(int srcSurveyContextId, Set<String> srcSurveyContextGiftKey, int destSurveyContextId, String username) throws Exception {
        
        final String DUPLICATE_SUFFIX = "_Copy";
        CopySurveyContextResult surveyContextResult = null;
        ArrayList<String> keyNames = new ArrayList<String>();
        HashMap<String, String> surveyIdMapping = new HashMap<String, String>();
        
        //nothing to copy
        if(srcSurveyContextGiftKey == null || srcSurveyContextGiftKey.isEmpty()){
            return null;
        }
        
        //user doesn't have permissions to add surveys to this survey context
        if(!Surveys.isSurveyContextEditable(destSurveyContextId, username)){
            return null;
        }

        //mapping of source survey context gift key to the survey 
        Map<String, Survey> surveysToCopy = new HashMap<String, Survey>();
        for(String giftKey : srcSurveyContextGiftKey){
            
            Survey survey = Surveys.getSurveyContextSurvey(srcSurveyContextId, giftKey);
            if(Surveys.isSurveyVisible(srcSurveyContextId, survey, username)){
                surveysToCopy.put(giftKey, survey);
            }
        }
        
        //nothing to copy
        if(surveysToCopy.isEmpty()){
            return null;
        }

        // get the survey context where the surveys will be copied to (minus generated question bank surveys)
        SurveyContext destSurveyContext = Surveys.getSurveyContext(destSurveyContextId, false);
        
        if (destSurveyContext != null) {
            // Get all the survey key names in use in the destination survey context.
            for (SurveyContextSurvey contextSurvey : destSurveyContext.getContextSurveys()) {
                keyNames.add(contextSurvey.getKey());
            }            
            
            // Iterate over each survey make a copy.
            // Each new copy gets added to the destination survey context.
            for (String surveyToCopyGiftKey : surveysToCopy.keySet()) {

                //create copy with ids of zero
                final Survey newSurvey = Survey.deepCopy(surveysToCopy.get(surveyToCopyGiftKey), username, true);

                try{
                    //save the survey to the db
                    Survey surveyResult = Surveys.surveyEditorSaveSurvey(newSurvey, destSurveyContextId, null, username);
                    
                    //create a unique gift key in the destination survey context
                    String uniqueKeyName = newSurvey.getName();
                    while(keyNames.contains(uniqueKeyName)){
                        uniqueKeyName += DUPLICATE_SUFFIX;
                    }
                
                    keyNames.add(uniqueKeyName);
                                        
                    //save the survey context survey to the db
                    SurveyContextSurvey surveyContextSurvey = new SurveyContextSurvey(destSurveyContextId, uniqueKeyName, surveyResult);
                    surveyContextSurvey = Surveys.insertSurveyContextSurvey(surveyContextSurvey, null);
                    
                    destSurveyContext.getContextSurveys().add(surveyContextSurvey);
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("destSurveyContext has size of: " + destSurveyContext.getContextSurveys().size());
                    }
                    
                    // Keep a mapping of old survey ids to new survey ids (Hashmap<Old, New>)
                    surveyIdMapping.put(surveyToCopyGiftKey, uniqueKeyName);
                }catch(Exception e){
                    throw new Exception("Failed to save the survey the new survey named '"+newSurvey.getName()+"'.", e);
                }

            }          
                        
            surveyContextResult = new CopySurveyContextResult(destSurveyContext, surveyIdMapping);
            if(logger.isDebugEnabled()){
                logger.debug("Successfully updated the survey context: " + destSurveyContext.getName());
            }

        } else {
            logger.error("Unable to get the survey contexts for source survey context id: " + srcSurveyContextId + ", dest survey context id: " + destSurveyContextId);
        }
        
        return surveyContextResult;
    }

}
