/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.survey;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import generated.course.PresentSurvey.ConceptSurvey;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.Category;
import mil.arl.gift.common.survey.Folder;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.survey.SurveyContextSurvey;
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

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("sas_rpc")
public interface SurveyRpcService extends RemoteService {

	/**
	 * Queries the server for what mode we're running in.
	 * 
	 * @return The deployment mode we're running in.
	 */
	DeploymentModeEnum getDeploymentMode();
	
    /**
     * Gets a question from the database
     *
     * @param id The ID of the question to get
     * @return GwtQuestion The question with ID with the specified ID, null if
     * there was an error
     */
    AbstractQuestion getQuestion(int id);

    /**
     * Queries the server for available questions
     *
     * @param query The query for a list of questions
     * @return ListQueryResponse<GwtQuestion> The response to the query
     */
    ListQueryResponse<AbstractQuestion> getQuestions(ListQuery<QuestionQueryData> query);

    /**
     * Gets a survey from the database
     *
     * @param id The ID of the survey to get
     * @return GwtSurvey The survey with the specified ID, null if there was an
     * error
     */
    SurveyReturnResult getSurvey(int id);
    
    /**
     * Gets a survey from the database
     *
     * @param id The ID of the survey to get
     * @return GwtSurvey The survey with the specified ID, null if there was an
     * error
     */
    SurveyReturnResult getSurveyWithResources(String username, int id, String targetWorkspaceFolder);

    /**
     * Queries the server for available surveys
     *
     * @param query The query for a list of surveys
     * @return ListQueryResponse<GwtSurvey> The response to the query
     */
    ListQueryResponse<SurveyHeader> getSurveys(ListQuery<SurveyQueryData> query);

    /**
     * Gets a option list from the database
     *
     * @param id The ID of the option list to get
     * @return GwtOptionList The option list with the specified ID, null if
     * there was an error
     */
    OptionList getOptionList(int id);

    /**
     * Queries the server for available option lists
     *
     * @param query The query for a list of option lists
     * @return ListQueryResponse<GwtOptionList> The response to the query
     */
    ListQueryResponse<OptionList> getOptionLists(ListQuery<OptionListQueryData> query);

    /**
     * Creates a new folder in the database
     *
     * @param folder The folder to create in the database
     * @return RpcResponse The response that indicates whether the operation was successful or not.
     */
    RpcResponse insertFolder(Folder folder);

    /**
     * Deletes a folder from the database
     *
     * @param folder The folder to delete from the database
     * @param userName used to check write permissions
     * @return Boolean If the operation was successful
     */
    Boolean deleteFolder(String folder, String userName);

    /**
     * Queries the server for available folders
     *
     * @param query The query for a list of folders
     * @return ListQueryResponse<GwtFolder> The response to the query
     */
    ListQueryResponse<Folder> getFolders(ListQuery<FolderQueryData> query);

    /**
     * Inserts a question into the database
     *
     * @param question The question to insert into the database
     * @return GwtQuestion The question in the database, null if the insert
     * failed
     */
    AbstractQuestion insertQuestion(AbstractQuestion question);

    /**
     * Updates a question in the database
     *
     * @param question The question to update in the database
     * @param username used to determine write permissions
     * @return boolean If the update was successful
     */
    Boolean updateQuestion(AbstractQuestion question, String username);

    /**
     * Updates a survey context survey in the database
     *
     * @param contextContextSurvey The survey context survey to update in the database
     * @return If the update was successful
     */
    Boolean updateSurveyContextSurvey(SurveyContextSurvey contextContextSurvey);

    /**
     * Inserts a survey into the database
     *
     * @param survey The survey to insert into the database
     * @return Boolean If the insert was successful
     */
    Survey insertSurvey(Survey survey);

    /**
     * Updates a survey in the database
     *
     * @param survey The survey to update in the database
     * @return boolean If the update was successful
     */
    Survey updateSurvey(Survey survey);
    
    /**
     * Exports a survey to the survey export folder, which can then be imported later.
     * 
     * @param surveyId The ID of the survey to export.
     * @return the response containing the export result.
     */
    ExportSurveysResponse exportSurvey(int surveyId);
    
    /**
     * Exports a survey to the survey export folder, which can then be imported later.
     * 
     * @param survey Survey object to export.
     * @return the response containing the export result.
     */
    ExportSurveysResponse exportSurvey(Survey survey);
    
    /**
     * Imports a survey that was exported using {@link #exportSurvey(int)}. Returns null if the import fails.
     * 
     * @param file The filename of the survey to import.
     * @param username The user that will have access to the survey. If the username is null, the default permissions will be used.
     * @return the response containing the import result.
     */
    ImportSurveysResponse importSurvey(String file, String username);
	    
    /**
     * Imports a survey that was exported.
     * 
     * @param file The filename of the survey to import.
     * @param username The user that will have access to the survey. If the username is null, the default permissions will be used.
     * @param filesToRename A map of files that should be renamed. Can be null.
     * @return the response containing the import result.
     */
    ImportSurveysResponse importSurvey(String file, String username, Map<String, String> filesToRename);
        
    
    /**
     * Gets a list of all exported surveys on the server.
     * 
     * @return List of exported survey file names, or null if no surveys exist.
     */
    List<String> getExportedSurveys();

    /**
     * Inserts an option list into the database
     *
     * @param userName The user creating the option list
     * @param optionList The option list to insert into the database
     * @return GwtOptionList The option list in the database, null if the insert
     * failed
     */
    OptionList insertOptionList(String userName, OptionList optionList);

    /**
     * Deletes an option list from the server
     *
     * @param optionListId The ID of the option list to delete from the server
     * @param userName used for write permission checks
     * @return Boolean If the action was successful
     */
    Boolean deleteOptionList(int optionListId, String userName);

    /**
     * Updates a option list in the database
     * 
     * @param userName The user updating the option list
     * @param optionList The option list to update in the database
     * @return boolean If the update was successful
     */
    Boolean updateOptionList(String userName, OptionList optionList);

    /**
     * Queries the server for available categories
     *
     * @param query The query for a list of categories
     * @return ListQueryResponse<GwtCategory> The response to the query
     */
    ListQueryResponse<Category> getCategories(ListQuery<CategoryQueryData> query);

    /**
     * Inserts a category into the database
     *
     * @param category The category to insert into the database
     * @return RpcResponse The response that indicates whether the operation was successful or not.
     */
    RpcResponse insertCategory(Category category);

    /**
     * Deletes a category from the database
     *
     * @param category The category to delete from the database
     * @param userName used for write permission checking
     * @return true If the operation was successful
     */
    Boolean deleteCategory(String category, String userName);

    /**
     * Gets the list of images the server has available to be displayed in
     * surveys
     *
     * @return List<String> The list of URIs to survey images
     */
    List<String> getSurveyImages();

    /**
     * Gets if the server can perform database restore and backup operations
     *
     * @return String The reason why database operations cannot be performed,
     * null if the server can
     */
    String canPerformAdminDatabaseOperations();
    
    /**
     * Gets if the server can connect to the survey database
     *
     * @return String The reason why database operations cannot be
     * performed, null if the server can
     */
    String canPerformDatabaseOperations();

    /**
     * Gets the list of database backups the server has, in chronological order
     *
     * @return List<String> The list of database backups, null if there was an
     * error
     */
    List<String> getDatabaseBackups();

    /**
     * Creates a backup of the database on the server
     *
     * @param backupUserData If user data should be backed up
     * @return Boolean If the operation was successful
     */
    Boolean backupDatabase(boolean backupUserData);

    /**
     * Restores the database with a backup on the server
     *
     * @param backupFileName The backup file to restore to
     * @return Boolean If the operation was successful
     */
    Boolean restoreDatabase(String backupFileName);

    /**
     * Renames a database backup file on the server
     *
     * @param oldBackupFileName The name of the database backup file on the
     * server
     * @param newBackupFileName The new name of the file
     * @return Boolean If the operation was successful
     */
    Boolean renameDatabaseBackupFile(String oldBackupFileName, String newBackupFileName);

    /**
     * Deletes a database backup file on the server
     *
     * @param backupFileName The name of the database backup file on the server
     * @return If the operation was successful
     */
    Boolean deleteDatabaseBackupFile(String backupFileName);

    /**
     * Gets the print out of dependencies for a survey, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param surveyId The ID of the survey
     * @return String The print out of dependencies for a survey
     */
    SurveyDependencies getSurveyDependencies(int surveyId);
    
    /**
     * Gets the print out of dependencies for a survey, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param surveyId The ID of the survey
     * @param ignoreSurveyContextId The id of a survey context to ignore (this typically will be the survey context that that the survey belongs to).
     * @return String The print out of dependencies for a survey
     */
    SurveyDependencies getSurveyDependencies(int surveyId, int ignoreSurveyContextId);

    /**
     * Gets the print out of dependencies for a survey context, returning "none"
     * if there are no dependencies or null if there was error
     *
     * @param surveyContextId The ID of the survey context
     * @return String The print out of dependencies for a survey context
     */
    String getSurveyContextDependencies(int surveyContextId);

    /**
     * Gets the print out of dependencies for a question, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param questionId The ID of the question
     * @return String The print out of dependencies for a question
     */
    String getQuestionDependencies(int questionId);

    /**
     * Gets the print out of dependencies for a option list, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param optionListId The ID of the option list
     * @return String The print out of dependencies for a option list
     */
    String getOptionListDependencies(int optionListId);
    
    /**
     * Inserts a survey property key into the database
     * 
     * @param key The survey property key to insert into the database
     * @return boolean If the insert was successful
     */
    Boolean insertSurveyPropertyKey(SurveyPropertyKeyEnum key);
    
    /**
     * Gets a list of survey property key names stored in the database.
     * 
     * @return List<String> The list of survey property keys
     */
    List<String> getSurveyPropertyKeyNames();

    /**
     * Deletes all survey responses for the survey associated with the survey context.
     * 
     * @param surveyContextId The survey context that the survey belongs to.
     * @param survey The survey that the responses will be deleted from.
     * @return If the deletion was successful
     */
    Boolean deleteSurveyResponses(int surveyContextId, Survey survey, String userName);
    
    /**
	 * Attempts to lock a question.
	 * @param id ID of the Question that needs to be locked.
	 * @param acquire If true it indicates that we're trying to acquire the
	 * lock which will fail if the Question is already locked. If it is false
	 * it indicates that we're trying to renew the lock which is necessary to
	 * prevent the server from automatically releasing the lock.
	 * @return True if the Question's lock status was updated, false otherwise.
	 */
	Boolean lockQuestion(int id, boolean acquire);
	
	/**
	 * Releases the lock for the given Question.
	 * @param id ID for the Question whose lock should be released.
	 */
	boolean unlockQuestion(int id);
    
    /**
	 * Attempts to lock a survey.
	 * @param id ID of the survey that needs to be locked.
	 * @param acquire If true it indicates that we're trying to acquire the
	 * lock which will fail if the survey is already locked. If it is false
	 * it indicates that we're trying to renew the lock which is necessary to
	 * prevent the server from automatically releasing the lock.
	 * @return True if the survey's lock status was updated, false otherwise.
	 */
	Boolean lockSurvey(int id, boolean acquire);
	
	/**
	 * Releases the lock for the given survey.
	 * @param id ID for the survey whose lock should be released.
	 */
	boolean unlockSurvey(int id);
    
    /**
	 * Attempts to lock a survey context.
	 * @param id ID of the survey context that needs to be locked.
	 * @param acquire If true it indicates that we're trying to acquire the
	 * lock which will fail if the survey context is already locked. If it is false
	 * it indicates that we're trying to renew the lock which is necessary to
	 * prevent the server from automatically releasing the lock.
	 * @return True if the survey context's lock status was updated, false otherwise.
	 */
	Boolean lockSurveyContext(int id, boolean acquire);
	
	/**
	 * Releases the lock for the given survey context.
	 * @param id ID for the survey context whose lock should be released.
	 */
	boolean unlockSurveyContext(int id);

	/**
	 * Exports a survey context
	 * @param context - the survey context to export
	 * @return a response containing the export result.
	 */
	ExportSurveysResponse exportSurveyContext(SurveyContext context);

	/**
	 * Exports a survey context
	 * @param contextId - the id of the survey context to export
	 * @return a response containing the export result.
	 */
	ExportSurveysResponse exportSurveyContext(int contextId);

	/**
     * Gets a list of all exported survey contexts on the survey
     * @return List of exported survey context file names, or null if no survey contexts exist.
     */
	List<String> getExportedSurveyContexts();

    /**
     * Return the server properties that have been identified as important for the 
     * sas client.
     * 
     * @return ServerProperties the properties needed by the sas client
     */
	ServerProperties getServerProperties();

	/**
	 * Gets a list of images referenced by a given survey
	 * 
	 * @param surveyId - The id of the survey to check for images
	 * @return a list of the images the survey contains. The list will be empty if the survey has no images.
	 */
	List<String> getSurveyImages(int surveyId);

	/**
	 * Attempts to delete the specified file. This is useful if temporary files have been created during
	 * an import or export. If the file cannot be deleted, an error message is logged.
	 * 
	 * @param tempFileToDelete The file to delete.
	 * @return true if the file was deleted successfully, false otherwise.
	 */
	Boolean deleteTempFile(String tempFileToDelete);
	
	/**
     * Called from the GAT Survey Editor to save a survey to the database.
     * 
     * Inserts a complete survey (including new surveyelements) into the database.
     *
     * @param survey The survey to save to the database.
     * @param surveyContext The survey context that the survey belongs to.
     * @param username used to determine write permissions
     */
	SurveyReturnResult surveyEditorSaveSurvey(Survey survey, SurveyContext surveyContext, String username);
	
	/**
     * Called from the GAT Survey Editor to asynchronously save a survey to the database.
     * 
     * Inserts a complete survey (including new surveyelements) into the database.
     *
     * @param survey The survey to save to the database.
     * @param surveyContextId The survey context id that the survey belongs to.
     * @param username used to determine write permissions
     */
	void surveyEditorSaveSurveyAsync(Survey survey, Integer surveyContextId, String username);    

    /**
     * Export the provided survey context survey to the survey course folder.
     * 
     * @param surveyContextSurvey the survey context survey to export
     * @param surveyCourseFolderPath the folder to export the survey context survey to. This folder
     *        path should be relative to the workspace directory (e.g.
     *        Public/TrainingAppsLib/userFolder).
     * @param username used to determine write permissions
     */
	void surveyEditorExportSurveyContextSurvey(SurveyContextSurvey surveyContextSurvey, String surveyCourseFolderPath,
            String username);

    /**
     * Gets the survey with the provided GIFT survey key from the survey export file found in the
     * survey course folder.
     * 
     * @param surveyKey the survey context survey GIFT key. Used to find the appropriate survey in
     *        the survey export file.
     * @param surveyCourseFolderPath the path of the survey course folder that contains the survey
     *        export file. This folder path should be relative to the workspace directory (e.g.
     *        Public/TrainingAppsLib/userFolder).
     * @param username used to determine write permissions
     * @return The survey found in the survey context of the survey export file found in the course
     *         folder that was mapped with the surveyKey in that survey context. Null if the survey
     *         could not be found in that survey context.
     */
    Survey getSurveyFromExportFile(String surveyKey, String surveyCourseFolderPath, String username);

	/**
	 * Gets a survey from a survey context in the database
	 * 
	 * @param surveyKey the GIFT key identifying the survey to retrieve
	 * @param surveyContextId the ID of the survey context containing the survey to get
	 * @return The survey with the given key in the given survey context, or null if no such survey could be found
	 */
	Survey getSurveyFromContextKey(String surveyKey, int surveyContextId);
	
	/**
     * Creates a new survey containing questions associated with the given concept survey.
     * 
     * @param surveyContextId The current survey context id
     * @param conceptSurvey The concept survey
     * @return A new survey containing questions associated with the given concept survey.
     */
	Survey getConceptSurvey(int surveyContextId, ConceptSurvey conceptSurvey);
	
	/**
	 * Copies the surveys from a source survey context and adds the new surveys to the destination survey context.
	 * 
	 * @param srcSurveyContextId - The source survey context where the surveys will be copied from (should be a valid non null survey context).
	 * @param destSurveyContextId - The destination survey context where the new surveys will be added to (should be valid non null survey context).
	 * @param username - The username that will have permissions for the surveys that are created.
	 * @return CopySurveyContextResult - Contains the updated survey context along with a mapping of gift survey context keys that have
	 *                                   changed.  The result will be non-null if successful, otherwise null is returned if the rpc fails.
	 */
	CopySurveyContextResult copySurveyContext(int srcSurveyContextId, Set<String> srcSurveyContextGiftKey, int destSurveyContextId, String username);

	/**
	 * Gets the status of the survey save operation being executed for the given user, if such an operation exists
	 * 
	 * @param username the name of the user to get the save operation status for
	 * @return the status of the survey save operation
	 */
	LoadedProgressIndicator<Survey> getSaveSurveyStatus(String username);
	
    /**
     * Checks the user permissions for the survey only.
     * This is an internal method and should not be called externally since both the survey context and
     * survey permissions need to be checked at the same time.
     * 
     * @param surveyId The survey to check the permissions on.
     * @param userName the user to check permissions for                 
     * @return True if the survey is editable based on user permissions.  If the user does not have permissions for the
     *         survey, then false is returned.
     */
     Boolean isSurveyEditable(int surveyId, String userName);

     /**
      * Return whether the survey in the survey context is editable to the user.
      * 
      * @param surveyContextId the id of the survey context containing the reference to the survey
      * @param survey the survey to check within the survey context
      * @param username used for write permissions checks
      * @return true if the survey context and survey is editable to the user
      */
     Boolean isSurveyEditable(int surveyContextId, Survey survey, String username);
}
