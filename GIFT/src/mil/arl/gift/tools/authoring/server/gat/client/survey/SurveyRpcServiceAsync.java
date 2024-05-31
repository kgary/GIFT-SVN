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

import com.google.gwt.user.client.rpc.AsyncCallback;

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
 * The async counterpart of
 * <code>SurveyRpcService</code>.
 */
public interface SurveyRpcServiceAsync {
	
	/**
	 * Queries the server for what mode we're running in.
	 * 
	 * @param callback Callback for the deployment mode query.
	 */
	void getDeploymentMode(AsyncCallback<DeploymentModeEnum> callback);

    /**
     * Queries the server for available questions
     *
     * @param query The query for a list of questions
     * @param callback The callback for the response to the query
     */
    void getQuestions(ListQuery<QuestionQueryData> query, AsyncCallback<ListQueryResponse<AbstractQuestion>> callback);

    /**
     * Queries the server for available surveys
     *
     * @param query The query for a list of surveys
     * @param callback The callback for the response to the query
     */
    void getSurveys(ListQuery<SurveyQueryData> query, AsyncCallback<ListQueryResponse<SurveyHeader>> callback);

    /**
     * Inserts a question into the database
     *
     * @param question The question to insert into the database
     * @param callback Callback for the question in the database
     */
    void insertQuestion(AbstractQuestion question, AsyncCallback<AbstractQuestion> callback);

    /**
     * Updates a question in the database
     *
     * @param question The question to update in the database
     * @param username used for write permission check
     * @param callback Callback for if the update was successful
     */
    void updateQuestion(AbstractQuestion question, String username, AsyncCallback<Boolean> callback);

    /**
     * Updates a survey context survey in the database
     *
     * @param surveyContextSurvey The survey context survey to update in the database
     * @param callback Callback for if the update was successful
     */
    void updateSurveyContextSurvey(SurveyContextSurvey surveyContextSurvey, AsyncCallback<Boolean> callback);

    /**
     * Gets a question from the database
     *
     * @param id The ID of the question to get
     * @param callback Callback for the question with ID with the specified ID
     */
    void getQuestion(int id, AsyncCallback<AbstractQuestion> callback);

    /**
     * Queries the server for available option lists
     *
     * @param query The query for a list of option lists
     * @param callback The callback for the response to the query
     */
    void getOptionLists(ListQuery<OptionListQueryData> query, AsyncCallback<ListQueryResponse<OptionList>> callback);

    /**
     * Gets a option list from the database
     *
     * @param id The ID of the option list to get
     * @param callback Callback for the option list with the specified ID
     */
    void getOptionList(int id, AsyncCallback<OptionList> callback);

    /**
     * Gets a survey from the database
     *
     * @param id The ID of the survey to get
     * @param callback Callback for the survey with the specified ID
     */
    void getSurvey(int id, AsyncCallback<SurveyReturnResult> callback);
    
    /**
     * Gets a survey from the database
     *
     * @param id The ID of the survey to get
     * @param callback Callback for the survey with the specified ID
     */
    void getSurveyWithResources(String username, int id, String targetWorkspaceFolder, AsyncCallback<SurveyReturnResult> callback);

    /**
     * Creates a new folder in the database
     *
     * @param folder The folder to create in the database=
     * @param callback Callback for the folder in the database
     */
    void insertFolder(Folder folder, AsyncCallback<RpcResponse> callback);

    /**
     * Deletes a folder from the database
     *
     * @param folder The folder to delete from the database
     * @param userName used for write permission check
     * @param callback Callback for if the operation was successful
     */
    void deleteFolder(String folder, String userName, AsyncCallback<Boolean> callback);

    /**
     * Queries the server for available questions
     *
     * @param query The query for a list of questions
     * @param callback The callback for the response to the query
     */
    void getFolders(ListQuery<FolderQueryData> query, AsyncCallback<ListQueryResponse<Folder>> callback);

    /**
     * Updates a survey in the database
     *
     * @param survey The survey to update in the database
     * @param callback Callback for if the update was successful
     */
    void updateSurvey(Survey survey, AsyncCallback<Survey> callback);

    /**
     * Inserts a survey into the database
     *
     * @param survey The survey to insert into the database
     * @param callback Callback for if the insert was successful
     */
    void insertSurvey(Survey survey, AsyncCallback<Survey> callback);  
    
    /**
     * Exports a survey to the survey export folder, which can then be imported later.
     * 
     * @param surveyId The ID of the survey to export.
     * @param callback Callback to execute when survey export is done.
     */
    void exportSurvey(int surveyId, AsyncCallback<ExportSurveysResponse> callback);
    
    /**
     * Exports a survey to the survey export folder, which can then be imported later.
     * 
     * @param survey Survey object to export.
     * @param callback Callback to execute when survey export is done.
     */
    void exportSurvey(Survey survey, AsyncCallback<ExportSurveysResponse> callback);
    
    /**
     * Imports a survey that was exported.
     * 
     * @param file The filename of the survey to import.
     * @param username The user that will have access to the survey. If the username is null, the default permissions will be used.
     * @param callback Callback to execute when survey import is done.
     */
    void importSurvey(String file, String username, AsyncCallback<ImportSurveysResponse> callback);

    /**
     * Imports a survey that was exported.
     * 
     * @param file The filename of the survey to import.
     * @param username The user that will have access to the survey. If the username is null, the default permissions will be used.
     * @param filesToRename A map of files that should be renamed. Can be null.
     * @param callback Callback to execute when survey import is done.
     */
    void importSurvey(String file, String username, Map<String, String> filesToRename, AsyncCallback<ImportSurveysResponse> callback);
        
    /**
     * Gets a list of all exported surveys on the server.
     * 
     * @param callback Callback to execute when list retrieval is done.
     */
    void getExportedSurveys(AsyncCallback<List<String>> callback);

    /**
     * Inserts an option list into the database
     *
     * @param userName The user creating the option list
     * @param optionList The option list to insert into the database
     * @param callback Callback for the option list in the database, null if the
     * insert failed
     */
    void insertOptionList(String userName, OptionList optionList,
            AsyncCallback<OptionList> callback);

    /**
     * Deletes an option list from the server
     *
     * @param optionListId The ID of the option list to delete from the server
     * @param userName used for write permission checks
     * @param callback Callback for if the action was successful
     */
    void deleteOptionList(int optionListId, String userName,
            AsyncCallback<Boolean> callback);

    /**
     * Updates an option list in the database
     *
     * @param userName The user creating the option list
     * @param optionList The option list to update in the database
     * @param callback Callback for if the update was successful
     */
    void updateOptionList(String userName, OptionList optionList,
            AsyncCallback<Boolean> callback);

    /**
     * Queries the server for available categories
     *
     * @param query The query for a list of categories
     * @param callback The callback for the response to the query
     */
    void getCategories(ListQuery<CategoryQueryData> query, AsyncCallback<ListQueryResponse<Category>> callback);

    /**
     * Inserts a category into the database
     *
     * @param category The category to insert into the database
     * @param callback Callback for the category in the database
     */
    void insertCategory(Category category,
            AsyncCallback<RpcResponse> callback);

    /**
     * Deletes a category from the database
     *
     * @param category The category to delete from the database
     * @param userName used for write permission check
     * @param callback Callback for if the operation was successful
     */
    void deleteCategory(String category, String userName,
            AsyncCallback<Boolean> callback);

    /**
	 * Gets a list of images referenced by a given survey. 
	 * The list will be empty if the survey has no images.
	 * 
	 * @param surveyId - The id of the survey to check for images
	 * @param callback - the callback containing the list of survey images
	 */
	void getSurveyImages(int surveyId, AsyncCallback<List<String>> callback);
    
    /**
     * Gets the list of images the server has available to be displayed in
     * surveys
     *
     * @param callback The callback for the list of URIs to survey images
     */
    void getSurveyImages(AsyncCallback<List<String>> callback);

    /**
     * Gets if the server can perform database restore and backup operations
     *
     * @param callback Callback for the reason why database operations cannot be
     * performed, null if the server can
     */
    void canPerformAdminDatabaseOperations(AsyncCallback<String> callback);
    
    /**
     * Gets if the server can connect to the survey database
     *
     * @param callback Callback for the reason why database operations cannot be
     * performed, null if the server can
     */
    void canPerformDatabaseOperations(AsyncCallback<String> callback);

    /**
     * Gets the list of database backups the server has, in chronological order
     *
     * @param callback The callback for the list of database backups, null if
     * there was an error
     */
    void getDatabaseBackups(AsyncCallback<List<String>> callback);

    /**
     * Creates a backup of the database on the server
     *
     * @param backupUserData If user data should be backed up
     * @param callback Callback for if the operation was successful
     */
    void backupDatabase(boolean backupUserData,AsyncCallback<Boolean> callback);

    /**
     * Restores the database with a backup on the server
     *
     * @param backupFile The backup file to restore to
     * @param callback Callback for if the operation was successful
     */
    void restoreDatabase(String backupFile, AsyncCallback<Boolean> callback);

    /**
     * Renames a database backup file on the server
     *
     * @param oldBackupFileName The name of the database backup file on the
     * server
     * @param newBackupFileName The new name of the file
     * @param callback Callback for if the operation was successful
     */
    void renameDatabaseBackupFile(String oldBackupFileName, String newBackupFileName, AsyncCallback<Boolean> callback);

    /**
     * Deletes a database backup file on the server
     *
     * @param backupFileName The name of the database backup file on the server
     * @param callback Callback for if the operation was successful
     */
    void deleteDatabaseBackupFile(String backupFileName, AsyncCallback<Boolean> callback);

    /**
     * Gets the print out of dependencies for a survey context, returning "none"
     * if there are no dependencies or null if there was error
     *
     * @param surveyContextId The ID of the survey context
     * @param callback Callback for the print out of dependencies for a survey
     * context
     */
    void getSurveyContextDependencies(int surveyContextId, AsyncCallback<String> callback);

    /**
     * Gets the print out of dependencies for a survey, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param surveyId The ID of the survey
     * * @param ignoreSurveyContextId The id of a survey context to ignore (this typically will be the survey context that that the survey belongs to).
     * @param callback Callback for the print out of dependencies for a survey
     */
    void getSurveyDependencies(int surveyId, int ignoreSurveyContextId, AsyncCallback<SurveyDependencies> callback);
    
    /**
     * Gets the print out of dependencies for a survey, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param surveyId The ID of the survey
     * @param callback Callback for the print out of dependencies for a survey
     */
    void getSurveyDependencies(int surveyId, AsyncCallback<SurveyDependencies> callback);

    /**
     * Gets the print out of dependencies for a question, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param questionId The ID of the question
     * @param callback Callback for the print out of dependencies for a question
     */
    void getQuestionDependencies(int questionId, AsyncCallback<String> callback);

    /**
     * Gets the print out of dependencies for a option list, returning "none" if
     * there are no dependencies or null if there was error
     *
     * @param optionListId The ID of the option list
     * @param callback Callback for the print out of dependencies for a option
     * list
     */
    void getOptionListDependencies(int optionListId, AsyncCallback<String> callback);

    /**
     * Inserts a survey property key into the database
     * 
     * @param propertyKey The survey property key to insert into the database
     * @param callback Callback for the survey property key in the database
     */
	void insertSurveyPropertyKey(SurveyPropertyKeyEnum propertyKey, AsyncCallback<Boolean> callback);
	
	 /**
     * Gets a list of survey property key names stored in the database.
     * 
     * @param callback Callback for the survey property key names in the database
     */
    void getSurveyPropertyKeyNames(AsyncCallback<List<String>> callback);

	/**
     * Deletes all survey responses for the survey associated with the survey context.
     * 
     * @param surveyContextId The survey context that the survey belongs to.
     * @param survey The survey that the responses will be deleted from.
     * @return If the deletion was successful
     */
    void deleteSurveyResponses(int surveyContextId, Survey survey, String userName, AsyncCallback<Boolean> callback);
	
	/**
	 * Attempts to lock a question.
	 * @param id ID of the Question that needs to be locked.
	 * @param acquire If true it indicates that we're trying to acquire the
	 * lock which will fail if the Question is already locked. If it is false
	 * it indicates that we're trying to renew the lock which is necessary to
	 * prevent the server from automatically releasing the lock.
	 * @param callback Callback reports whether or not the lock status for the
	 * question was successfully updated.
	 */
	void lockQuestion(int id, boolean acquire, AsyncCallback<Boolean> callback);
	
	/**
	 * Releases the lock for the given Question.
	 * @param id ID for the Question whose lock should be released.
	 * @param callback This always returns true.
	 */
	void unlockQuestion(int id, AsyncCallback<Boolean> callback);
	
	/**
	 * Attempts to lock a survey.
	 * @param id ID of the survey that needs to be locked.
	 * @param acquire If true it indicates that we're trying to acquire the
	 * lock which will fail if the survey is already locked. If it is false
	 * it indicates that we're trying to renew the lock which is necessary to
	 * prevent the server from automatically releasing the lock.
	 * @param callback Callback reports whether or not the lock status for the
	 * survey was successfully updated.
	 */
	void lockSurvey(int id, boolean acquire, AsyncCallback<Boolean> callback);
	
	/**
	 * Releases the lock for the given survey.
	 * @param id ID for the survey whose lock should be released.
	 * @param callback This always returns true.
	 */
	void unlockSurvey(int id, AsyncCallback<Boolean> callback);
	
	/**
	 * Attempts to lock a survey context.
	 * @param id ID of the survey context that needs to be locked.
	 * @param acquire If true it indicates that we're trying to acquire the
	 * lock which will fail if the survey context is already locked. If it is false
	 * it indicates that we're trying to renew the lock which is necessary to
	 * prevent the server from automatically releasing the lock.
	 * @param callback Callback reports whether or not the lock status for the
	 * survey context was successfully updated.
	 */
	void lockSurveyContext(int id, boolean acquire, AsyncCallback<Boolean> callback);
	
	/**
	 * Releases the lock for the given survey context.
	 * @param id ID for the survey context whose lock should be released.
	 * @param callback This always returns true.
	 */
	void unlockSurveyContext(int id, AsyncCallback<Boolean> callback);
	
	/**
	 * Exports a survey context
	 * @param context - the survey context to export
	 * @param callback - the callback to report whether or not the lock status for the
	 * survey context was successfully exported.
	 */
	void exportSurveyContext(SurveyContext context, AsyncCallback<ExportSurveysResponse> callback);

	/**
	 * Exports a survey context
	 * @param contextId - the id of the survey context to export
	 * @param callback - the callback to report whether or not the lock status for the
	 * survey context was successfully exported.
	 */
	void exportSurveyContext(int contextId, AsyncCallback<ExportSurveysResponse> callback);

	/**
     * Gets a list of all exported survey contexts on the survey
     *
     * @param callback - the callback to execute when list retrieval is done.
     */
	void getExportedSurveyContexts(AsyncCallback<List<String>> callback);

    /**
     * Return the server properties that have been identified as important for the 
     * sas client.
     * @param callback - the callback to execute when the properties are retrieved
     * 
     * @return ServerProperties the properties needed by the sas client
     */
	void getServerProperties(AsyncCallback<ServerProperties> callback);
	
	/**
	 * Attempts to delete the specified file. This is useful if temporary files have been created during
	 * an import or export. If the file cannot be deleted, an error message is logged.
	 * 
	 * @param tempFileToDelete The file to delete.
	 * @param callback the callback to execute when the delete operation is complete.
	 */
	void deleteTempFile(String tempFileToDelete, AsyncCallback<Boolean> callback);
	
	/**
     * Called from the GAT Survey Editor to save a survey to the database.
     * 
     * Inserts a complete survey (including new surveyelements) into the database.
     *
     * @param survey The survey to save to the database.
     * @param surveyContext The survey context that the survey belongs to.
     * @param username used for write permission check
     * @param callback Callback for if the insert was successful
     */
    void surveyEditorSaveSurvey(Survey survey, SurveyContext surveyContext, String username, AsyncCallback<SurveyReturnResult> callback);
	
	/**
     * Called from the GAT Survey Editor to asynchronously save a survey to the database.
     * 
     * Inserts a complete survey (including new surveyelements) into the database.
     *
     * @param survey The survey to save to the database.
     * @param surveyContextId The survey context id that the survey belongs to.
     * @param username used for write permission check
     * @param callback Callback for if the insert was successful
     */
    void surveyEditorSaveSurveyAsync(Survey survey, Integer surveyContextId, String username, AsyncCallback<Void> callback);

    /**
     * Export the provided survey context survey to the survey course folder.
     * 
     * @param surveyContextSurvey the survey context survey to export
     * @param surveyCourseFolderPath the folder to export the survey context survey to. This folder
     *        path should be relative to the workspace directory (e.g.
     *        Public/TrainingAppsLib/userFolder).
     * @param username used to determine write permissions
     * @param callback Callback for if the export was successful
     */
    void surveyEditorExportSurveyContextSurvey(SurveyContextSurvey surveyContextSurvey, String surveyCourseFolderPath,
            String username, AsyncCallback<Void> callback);

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
     * @param callback returns the survey found in the survey context of the survey export file
     *        found in the course folder that was mapped with the surveyKey in that survey context.
     *        Can be null if the survey could not be found in that survey context.
     */
    void getSurveyFromExportFile(String surveyKey, String surveyCourseFolderPath, String username, AsyncCallback<Survey> callback);
    
    /**
	 * Gets a survey from a survey context in the database
	 * 
	 * @param surveyKey the GIFT key identifying the survey to retrieve
	 * @param surveyContextId the ID of the survey context containing the survey to get
	 * @param callback a callback containing the survey with the given key 
	 * in the given survey context, or null if no such survey could be found
	 */
	void getSurveyFromContextKey(String surveyKey, int surveyContextId, AsyncCallback<Survey> callback);
	
	/**
	 * Creates a new survey containing questions associated with the given concept survey.
	 * 
	 * @param surveyContextId The current survey context id
	 * @param conceptSurvey The concept survey
	 * @param callback A callback containing the new survey
	 */
    void getConceptSurvey(int surveyContextId, ConceptSurvey conceptSurvey, AsyncCallback<Survey> callback);
	
	/**
     * Copies the surveys from a source survey context and adds the new surveys to the destination survey context.
     * 
     * @param srcSurveyContextId - The source survey context where the surveys will be copied from (should be a valid non null survey context).
     * @param destSurveyContextId - The destination survey context where the new surveys will be added to (should be valid non null survey context).
     * @param username - The username that will have permissions for the surveys that are created.
     * @param callback - Callback containing the result.  The result contains the updated survey context along with a mapping of gift survey context keys that have
     *                   changed.  The result will be non-null if successful, otherwise null is returned if the rpc fails.
     */
	void copySurveyContext(int srcSurveyContextId, Set<String> srcSurveyContextGiftKey, int destSurveyContextId, String username, AsyncCallback<CopySurveyContextResult> callback);

	/**
	 * Gets the status of the survey save operation being executed for the given user, if such an operation exists
	 * 
	 * @param username the name of the user to get the save operation status for
	 * @param callback a callback returning the status of the survey save operation
	 */
	void getSaveSurveyStatus(String username, AsyncCallback<LoadedProgressIndicator<Survey>> callback);
	
    /**
     * Checks the user permissions for the survey only.
     * This is an internal method and should not be called externally since both the survey context and
     * survey permissions need to be checked at the same time.
     * 
     * @param surveyId The survey to check the permissions on.
     * @param userName the user to check permissions for                 
     * @param callback True if the survey is editable based on user permissions.  If the user does not have permissions for the
     *         survey, then false is returned.
     */
     void isSurveyEditable(int surveyId, String userName, AsyncCallback<Boolean> callback);
     
     /**
      * Return whether the survey in the survey context is editable to the user.
      * 
      * @param surveyContextId the id of the survey context containing the reference to the survey
      * @param survey the survey to check within the survey context
      * @param username used for write permissions checks
      * @param callback true if the survey context and survey is editable to the user
      */
     void isSurveyEditable(int surveyContextId, Survey survey, String username, AsyncCallback<Boolean> callback);
}
