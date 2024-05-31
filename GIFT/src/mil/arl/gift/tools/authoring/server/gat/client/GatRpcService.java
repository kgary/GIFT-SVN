/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import generated.conversation.Conversation;
import generated.course.Media;
import generated.course.SlideShowProperties;
import generated.dkf.Concept;
import generated.dkf.Scenario;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.GiftScenarioProperties;
import mil.arl.gift.common.io.MapTileProperties;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.shared.AuthoritativeResourceRecord;
import mil.arl.gift.tools.authoring.server.gat.shared.ExternalScenarioImportResult;
import mil.arl.gift.tools.authoring.server.gat.shared.XTSPImporterResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.wrap.TrainingApplicationObject;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("rpc")
public interface GatRpcService extends RemoteService{
	
	/**
	 * Converts the given list of {@link Media} items to the {@link MediaHtml} items used to display them.
	 * 
	 * @param userName the user name of the user invoking this operation. Cannot be null or empty.
	 * @param mediaList the list of media items to convert. Cannot be null.
	 * @param courseFolderPath the path to the course folder containing the media. Cannot be null or empty.
	 * @return the list of converted items
	 */
	List<MediaHtml> getMediaHtmlList(List<Media> mediaList, String userName, String courseFolderPath);

	/**
	 * Gets a direct URL to a file within a users course folder via relative path  
	 * 
	 * @param userName The username of the owner of the course folder. Cannot be null or empty
	 * @param relativeFileName The relative path to the file, ie. courseFolder + "/" + relativePathFromRoot. Cannot be null or empty
	 * @return the URL to the content file
	 */
	FetchContentAddressResult getAssociatedCourseImage(String userName, String relativeFileName);
	
	 /**
     * Modifies the URL if necessary so that it can be presented in the GAT
     * 
     * @param userName The user name of the user invoking this operation. Cannot be null or empty.
     * @param contentUrl The URL to modify. Cannot be null or empty.
     * @param courseFolderPath The path to the course folder. Cannot be null or empty.
     * @param mediaProperties The media type properties (ie. PDFProperties, WebpageProperties, etc). Can be null.
	 * @return The result containing the URL to be presented
	 */
	FetchContentAddressResult getContentUrl(String userName, String contentUrl, String courseFolderPath, Serializable mediaProperties);

	/**
     * Gets the URLs needed to view the given slideshow's slides in a browser
     * 
     * @param slideShowProperties the properties of the slideshow that define its slides. Cannot be null.
     * @param courseFolderPath the path to the course folder containing the slide show. Cannot be null or empty.
     * @param userName the user name of the user invoking this operation. Cannot be null or empty.
     * @return the URLs needed to preview the slideshow
     */
	SlideShowProperties getSlideShowUrls(SlideShowProperties slideShowProperties, String courseFolderPath, String userName);
	
	/**
	 * Gets the JAXB course object from the file at the given location
	 * 
	 * @param userName the user name of the user invoking this operation. Cannot be null or empty.
	 * @param relativePath the path to the JAXB object relative to the user's workspace. Cannot be null or empty.
	 * @param useParentAsCourse whether to use parent folder in the relativePath as the course folder.  This is useful
	 * if you can guarantee that the parent folder is the course folder (e.g. gift wrap authored dkf.xml).  In the past
	 * the gift authoring tool would allow authors to place GIFT xml files in subfolders of the course folder.
	 * @return the JAXB course object
	 */
	FetchJAXBObjectResult getJAXBObject(String userName, String relativePath, boolean useParentAsCourse);
	
	/**
	 * Starts a preview of the given conversation
	 * 
	 * @param conversation the conversation to preview. Cannot be null.
	 * @return the updated chat ID and conversation state (i.e. what messages to display)
	 */
	UpdateConversationResult startConversation(Conversation conversation);
	
	/**
	 * Updates the conversation preview with the given chat ID based on the given course object and user response
	 * 
	 * @param chatId the ID of the chat session being used for the conversation preview.
	 * @param conversation the conversation course object used to guide the preview. Cannot be null.
	 * @param userText the text that the user last entered. Cannot be null.
	 * @return the updated conversation state (i.e. what messages to display)
	 */
	UpdateConversationResult updateConversation(int chatId, Conversation conversation, String userText);

    /**
     * Checks the new course name to make sure it does not conflict with existing courses.
     * 
     * @param username the name of the user invoking the operation. Cannot be null or empty.
     * @param courseFile the path of the original course file (i.e. "username/CourseName/CourseName.course.xml"). Cannot be null or empty.
     * @param newName the new name to check for conflicts. Cannot be null or empty.
     * @return a response that is successful if no conflicts exists
     */
    GenericRpcResponse<Boolean> checkCourseName(String username, String courseFile, String newName);
	
	/**
	 * Renames the course with the given course file to the provided new name
	 * 
	 * @param username the name of the user invoking the operation. Cannot be null or empty.
	 * @param courseFile the path of the course file to be renamed (i.e. "username/CourseName/CourseName.course.xml"). Cannot be null or empty.
	 * @param newName the new name to rename the course file to. Cannot be null or empty.
	 * @return a response that will contain the path to the renamed course file if the operation was successful
	 */
	GenericRpcResponse<String> renameCourse(String username, String courseFile, String newName);
	
	/**
	 * Gets the JAXB {@link Course} object used to preview the course at the given path
	 * 
	 * @param userName the name of the user invoking the operation. Cannot be null or empty.
	 * @param relativePath the path to the course, relative to the user's workspace. Cannot be null or empty.
	 * @return the {@link Course} object used to preview the course
	 */
	FetchJAXBObjectResult getCoursePreviewObject(String userName, String relativePath);

    /**
     * Gets the conditions that can be used by a given Training Application.
     * 
     * @param trainingApp The training application for which to fetch
     *        {@link ConditionInfo conditions}
     * @return The {@link ConditionInfo} objects that represent the conditions
     *         that the Training Application can use.
     */
    Set<ConditionInfo> getConditionsForTrainingApplication(TrainingApplicationEnum trainingApp);
    
    /**
     * Return the list of condition classes that call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * @return  won't be null.  The condition classes without the "mil.arl.gift" prefix.
     * that can call {@link #AbstractConditionconditionCompleted()}.
     */
    Set<String> getConditionsThatCanComplete();
    
    /**
     * Gets the conditions that are needed for the set of learner action types.
     * 
     * @return The {@link ConditionInfo} objects
     *        that represent the conditions for learner action types.
     */
    List<ConditionInfo> getConditionsForLearnerActions();
    
    /**
     * Identifies whether or not the Concept has data that conflicts with another Concept which references the same external source ID.
     * @param initialConcept The Concept to check
     * @param conceptList A List of all Concepts in the DKF
     * @return True if there is a conflict, false otherwise.
     */
    Boolean doesConceptHaveConflictingExternalResourceReferences(generated.dkf.Concept initialConcept, List<Concept> conceptList);
    
    /**
     * Identifies the Concepts which reference the same externalSourceId as alteredConcept, copies its values to theirs (aside from name and NodeID),
     * 		and returns a map of those Concepts indexed by NodeID.
     * @param alteredConcept The Concept that has been altered; its contents and data (aside from name and NodeID) will be copied to
     * 		any Concepts with the same externalSourceId before they are added to the returned map.
     * @param dkfScenario The DKF Scenario to read. Cannot be null.
     * @return a map of the Concepts that have the same externalSourceId as alteredConcept, after their values have been changed to copy its.
     * 			Indexed by their NodeIDs.
     */
    GenericRpcResponse<Map<java.math.BigInteger, Concept>> copyChangesToConceptsWithDuplicateExternalSourceIds(Concept alteredConcept, Scenario dkfScenario);
    
    /**
     * Gets the list of training application libraries
     * 
     * @param trainingApplicationType the training application type to retrieve. If null, all types
     *        will be retrieved.
     * @param username the user attempting to retrieve the training application objects.
     * @param progressIndicator (optional) used to communicate progress in retrieving the training app objects
     * @return a response containing the list of {@link TrainingApplicationObject}. Each
     *         {@link TrainingApplicationObject} contains the training application and its file
     *         path.
     */
    public GenericRpcResponse<List<TrainingApplicationObject>> getTrainingApplicationObjects(
            TrainingApplicationEnum trainingApplicationType, String username, ProgressIndicator progressIndicator);

    /**
     * Checks the provided training application path and name for conflicts. Will check the
     * workspace\Public\TrainingAppsLib user and Public folders' training applications for naming
     * conflicts.
     * 
     * @param trainingApplicationFolderPath the folder path to check for conflicts. Needs to contain
     *        the full path from the workspace subfolder. (e.g.
     *        Public/TrainingAppsLib/Public/folderName)
     * @param username the user attempting to retrieve the training application objects.
     * @return a response containing the training application object that is conflicted with the
     *         provided training application name. Will be null if there are no conflicts.
     */
    public GenericRpcResponse<TrainingApplicationObject> checkTrainingApplicationPath(
            FileTreeModel trainingApplicationFolderPath, String username);

    /**
     * Retrieves the user folder path from workspace\Public\TrainingAppsLib based on the provided
     * username. This does not guarantee that the folder exists.
     * 
     * @param username the user attempting to retrieve the TrainingAppsLib user folder.
     * @return a response containing the path of the TrainingAppsLib user folder.
     */
    public GenericRpcResponse<FileTreeModel> getTrainingAppsLibUserFolder(String username);

    /**
     * Deletes the given training application object and its associated folder
     * 
     * @param object the training application object to delete
     * @param username the user attempting the delete
     * @return a response containing the results of the operation
     */
    public GenericRpcResponse<Void> deleteTrainingApplicationObject(TrainingApplicationObject object, String username);

    /**
     * Creates a new folder within the training apps lib directory
     * 
     * @param folderName the name of the folder to create within the training apps lib directory
     * @param username the user attempting to create the folder
     * @return a response containing the results of the operations
     */
    public GenericRpcResponse<FileTreeModel> createTrainingAppsLibFolder(String folderName, String username);

    /**
     * Saves the given data to its associated training application course object
     * 
     * @param objectData the data object to save
     * @param oldCourseFolderPath the parent path of the training application that was changed. The
     *        new object data contains the new path so it cannot be trusted to find the location of
     *        the old data. The root of the path must be a workspace sub-folder (e.g. Public,
     *        &lt;username&gt;).
     * @param username the user attempting to perform the save
     * @return a response containing the result of the operation
     */
    public GenericRpcResponse<Void> saveTACourseObject(TrainingApplicationObject objectData,
            FileTreeModel oldCourseFolderPath, String username);

    /**
     * Retrieves all the training applications scenario properties.
     * 
     * @param username information used to authenticate the request
     * @return a response containing the list of training application scenario properties that were
     *         found.
     */
    public GenericRpcResponse<List<GiftScenarioProperties>> getTrainingApplicationScenarioProperties(String username);

    /**
     * Retrieves the training application scenario properties.
     * 
     * @param folderPath the path of the folder to search for the GIFT scenario property file. The
     *        root must be a sub-folder of the Training.Apps/maps directory.
     * @param username information used to authenticate the request
     * @return a response containing the training application scenario properties that were found
     *         withing the provided search folder.
     */
    public GenericRpcResponse<GiftScenarioProperties> getTrainingApplicationScenarioProperty(String folderPath,
            String username);

    /**
     * Retrieves the map tile property files from the provided folder location.
     * 
     * @param folderPath the path of the folder to search for the map tile property files. The root
     *        must be a sub-folder of the Training.Apps/maps directory.
     * @param username information used to authenticate the request
     * @return a response containing the list of map tile properties that were found within the
     *         provided search folder.
     */
    public GenericRpcResponse<List<MapTileProperties>> getScenarioMapTileProperties(String folderPath, String username);
    
    
    /**
     * Gets the resources with the given IDs from an authoritative resource system
     * 
     * @param ids the list of IDs uniquely identifying the resources being requested
     * 
     * @return a response containing the list of requested records
     */
    public GenericRpcResponse<List<AuthoritativeResourceRecord>> getAuthoritativeResources(List<String> ids);
    	
    
    /**
     * Parses the given file to generate places of interest using the data inside it
     * 
     * @param username the name of the user making the request
     * @param domainFilePath the path of the file to parse in the Domain folder, relative to the 
     *        Domain folder
     * @param ignoreElevation whether elevation values in the file should be ignored. If null, no places of interest will
     * be created, and the uploaded file will simply be cleaned up
     * @return the list of places of interest that were gathered from within the provided file
     */
    GenericRpcResponse<List<Serializable>> getPlacesOfInterestFromFile(String username, String domainFilePath, Boolean ignoreElevation);

    /**
     * Parses the given xtsp file, generates changes to the DKF at the given path, and produces a list of Course Concepts
     * which are detected in the xTSP file.
     * 
     * @param username the name of the user making the request. Cannot be null or empty.
     * @param dkfPath the path of the DKF to be changed. Cannot be null or empty.
     * e.g. "mcambata/XTSP Import Test/TrainingApp_1.dkf.xml"
     * @param xtspFilePath the path of the XTSP file to be parsed. Cannot be null or empty.
     * e.g. "mcambata/XTSP Import Test/xtspFile.json"'
     * @param trainingAppType The type of training application for which the DKF is being created. Cannot be null.
     * @return a callback which returns the list of Course Concepts that were detected in the xTSP file.
     */
    GenericRpcResponse<XTSPImporterResult> importXtspIntoScenarioFile(String username, String dkfPath, String xtspFilePath, TrainingApplicationEnum trainingAppType);
    
    /**
     * Gets which course concepts from the provided list have been implemented by the given scenario file
     * 
     * @param username the username of the user making the request. Cannot be null.
     * @param scenarioFilePath the workspace-relative path to the scenario file. Cannot be null.
     * @param courseConcepts the course concepts to look for in the scenario file. Can be null.
     * @return a response containing the list of concepts that were implemented by the scenario file
     */
    GenericRpcResponse<Set<String>> getScenarioConcepts(String username, String scenarioFilePath, Set<String> courseConcepts);

    /**
     * Gets the resource with the given ID from an authoritative resource system
     * 
     * @param id the ID uniquely identifying the resource being requested
     * @return a response containing the requested record
     */
    GenericRpcResponse<AuthoritativeResourceRecord> getAuthoritiativeResource(String id);
    
    /**
     * Queries for multiple authoritative resources
     * 
     * @param type the type of target being queried. Can be null.
     * @param name the name of the target. Can be null. 
     * @param start the starting index of the query. Can be null. 
     * @param size the size of the query result. Can be null. 
     * @return a response containing the multiple records requested
     */
    GenericRpcResponse<List<AuthoritativeResourceRecord>> queryAuthoritativeResources(String type, String name, Integer start, Integer size);

    /**
     * Creates a course for a scenario with the given ID from an external host
     * 
     * @param scenarioId the ID of the scenario to create a course for. Cannot be null.
     * @return the result containing the created course. Can be null.
     */
    GenericRpcResponse<ExternalScenarioImportResult> createCourseForScenario(String scenarioId);
}