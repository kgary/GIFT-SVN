/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.learneraction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import generated.dkf.AutoTutorSKO;
import generated.dkf.ConversationTreeFile;
import generated.dkf.TutorMeParams;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.LearnerActionsFiles;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeFileHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the learner actions for a domain
 *
 * @author jleonard
 */
public class LearnerActionsManager {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerActionsManager.class);    

    private generated.dkf.LearnerActionsList learnerActions = new generated.dkf.LearnerActionsList();       
    
    /** contains tutor me learner actions that are shown on the TUI feedback widget */
    private Map<String, generated.dkf.LearnerAction> tutorMeActions = new HashMap<>();
    
    /** the course folder that contains all course assets, won't be null */
    private AbstractFolderProxy courseFolder;
    
    /** whether to skip loading/checking files that this DKF references (e.g. learner actions xml, conversation tree xml) 
     *  This is useful when the DKF is used for Game Master playback and not during course execution. */
    private boolean skipExternalFileLoading;

    /**
     * Constructor
     * 
     * @param courseFolder the course folder that contains all course assets
     * @param skipExternalFileLoading whether to skip loading/checking files that this DKF references (e.g. learner actions xml, conversation tree xml) 
     *  This is useful when the DKF is used for Game Master playback and not during course execution.
     */
    public LearnerActionsManager(AbstractFolderProxy courseFolder, boolean skipExternalFileLoading) {

        if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null.");
        }
        
        this.courseFolder = courseFolder;
        this.skipExternalFileLoading = skipExternalFileLoading;
    }    
    
    /**
     * Remove all of the current references to learner actions
     */
    public void clear(){
        logger.info("Cleared all existing learner actions.");
        learnerActions.getLearnerAction().clear();
        tutorMeActions.clear();
    }
    
    /**
     * Check the learner actions for logic errors.
     * 
     * @param instructionalStrategyNames set of unique instructional strategy names to use when checking if an 
     * apply strategy learner action references an existing instructional strategy.
     * @throws DetailedException if there was a gift validation issue or problem retrieving a file referenced by a learner action
     * (e.g. conversation file)
     * @throws FileValidationException if there was an issue parsing the file or validating against the schema
     * @throws Exception if a learner action is missing required parameters
     */
    public void checkLearnerActions(Set<String> instructionalStrategyNames) throws DetailedException, FileValidationException, Exception{
        
        //check learner action tutor me conversation files
        for(generated.dkf.LearnerAction learnerAction : tutorMeActions.values()){
                
            Serializable actionParams = learnerAction.getLearnerActionParams();
            if(actionParams instanceof generated.dkf.TutorMeParams){
                generated.dkf.TutorMeParams tutorMeParams =  (generated.dkf.TutorMeParams)actionParams;
                
                Serializable configuration = tutorMeParams.getConfiguration();
                if(configuration instanceof AutoTutorSKO){
                    
                    if(skipExternalFileLoading){
                        continue; 
                     }
                    
                    DomainDKFHandler.checkAutoTutorReference((AutoTutorSKO)configuration, courseFolder);
                    
                }else if(configuration instanceof ConversationTreeFile){
                    
                    if(skipExternalFileLoading){
                       continue; 
                    }
                    
                    ConversationTreeFile conversationTreeFile = (ConversationTreeFile)configuration;
                    String conversationFileName = conversationTreeFile.getName();
                    FileProxy conversationFile;
                    try {
                        conversationFile = courseFolder.getRelativeFile(conversationFileName);
                        ConversationTreeFileHandler tree = new ConversationTreeFileHandler(conversationFile, true);
                        GIFTValidationResults treeValidationResults = tree.checkConversation();
                        if(treeValidationResults.hasCriticalIssue() || treeValidationResults.hasImportantIssues()){
                            throw treeValidationResults.getFirstError();
                        }
                    } catch (Throwable e) {
                        throw new DetailedException("Failed to retrieve the conversation file named '"+conversationFileName+"'.", 
                                "There was a severe error when trying to retrieve the file:\n"+e.getMessage(), e);
                    }
                }else{
                    //error
                    throw new DetailedException("Failed to validate a 'Tutor Me' learner action", 
                            "Found an unhandled parameter of "+configuration+" for the learner action named '"+learnerAction.getDisplayName()+"'.", null);
                }
            }else if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                if (StringUtils.isBlank(strategyRef.getName())) {
                    throw new Exception("The strategy reference is malformed for the learner action named '"+learnerAction.getDisplayName()+"'");
                }else if(!instructionalStrategyNames.contains(strategyRef.getName())){
                    throw new Exception("The strategy reference '"+strategyRef.getName()+"' in the learner action named '"+learnerAction.getDisplayName()+"' is not one of the "+instructionalStrategyNames.size()+" instructional strategies authored.");
                }
            }
        }

    }
    
    /**
     * Add an instance of learner actions list references to the current list of references.
     * 
     * @param learnerActionsList dkf list of learner actions to use
     */
    public void addLearnerActionsList(generated.dkf.LearnerActionsList learnerActionsList){
        
        if(learnerActionsList != null){
            for(generated.dkf.LearnerAction action : learnerActionsList.getLearnerAction()){
                learnerActions.getLearnerAction().add(action);
                
                if(action.getType() == LearnerActionEnumType.TUTOR_ME){
                    //keep track of tutor me actions
                    
                    generated.dkf.LearnerAction replacedLearnerAction = tutorMeActions.put(action.getDisplayName(), action);
                    
                    if(action.getDescription() == null && action.getLearnerActionParams() instanceof TutorMeParams){
                        // check if the action parameters can help with populating a description value that wasn't authored
                        
                        Serializable actionParams = action.getLearnerActionParams();
                        if(actionParams instanceof generated.dkf.TutorMeParams){
                            generated.dkf.TutorMeParams tutorMeParams =  (generated.dkf.TutorMeParams)actionParams;
                            if(tutorMeParams.getConfiguration() instanceof String){
                                
                                if(!skipExternalFileLoading){
                                    //retrieve conversation description value from conversation file                                
                                    String conversationFileName = (String) tutorMeParams.getConfiguration();
                                    FileProxy conversationFile;
                                    try {
                                        conversationFile = courseFolder.getRelativeFile(conversationFileName);
                                        ConversationTreeFileHandler handler = new ConversationTreeFileHandler(conversationFile, true);
                                        GIFTValidationResults treeValidationResults = handler.checkConversation();
                                        if(treeValidationResults.hasCriticalIssue() || treeValidationResults.hasImportantIssues()){
                                            throw treeValidationResults.getFirstError();
                                        }
                                        action.setDescription(handler.getConversation().getLearnersDescription());
                                    } catch (Throwable e) {
                                        throw new DetailedException("Failed to retrieve the conversation file named '"+conversationFileName+"'.", 
                                                "There was a severe error when trying to retrieve the file.  The error message reads:\n"+e.getMessage(), e);
                                    }
                                 }                                

                            }
                        }
                    }
                    
                    if(replacedLearnerAction != null){
                        throw new DetailedException("Unable to add the learner action named "+action.getDisplayName()+".", 
                                "There is already a 'Tutor Me' learner action with that name and 'Tutor Me' learner actions must be unique", null);
                    }
                }
            }
        }
    }
    
    /**
     * Add the learner actions list references from the learner actions file provided.
     * 
     * @param learnerActionsFile file containing learner actions to use.  Can't be null.
     * @throws FileNotFoundException  if the file was not found
     * @throws FileValidationException if there was a problem parsing the learner actions file
     */
    public void addLearnerActionsFile(FileProxy learnerActionsFile) throws FileNotFoundException, FileValidationException{

        LearnerActionsFileHandler fileHandler = new LearnerActionsFileHandler(learnerActionsFile);

        generated.dkf.LearnerActionsList lessonMaterialList = fileHandler.getLearnerActions();
        addLearnerActionsList(lessonMaterialList);
    }
    
    /**
     * Add a list of learner actions references from the files provided.
     * 
     * @param learnerActionsFiles collection of learner action files
     * @throws IOException if there was a problem with any of the learner action files
     * @throws FileValidationException  if there was a problem parsing the learner actions file
     */
    public void addLearnerActionsFiles(LearnerActionsFiles learnerActionsFiles) throws IOException, FileValidationException{
        
        if(learnerActionsFiles != null){
            
            for(String filename : learnerActionsFiles.getFile()){
                
                FileProxy learnerActionsFile = courseFolder.getRelativeFile(filename);
                addLearnerActionsFile(learnerActionsFile);
            }
        }
    }

    /**
     * Gets the learner actions for a domain
     *
     * @return LearnerActionsList The learner actions for a domain.  Can be empty but not null.
     */
    public generated.dkf.LearnerActionsList getLearnerActions() {
        return learnerActions;
    }
    
    /**
     * Return the tutor me type learner action referenced by the unique name.
     * 
     * @param name the unique name of a tutor me learner action
     * @return the learner action referenced by that name.  Can be null if not found.
     */
    public generated.dkf.LearnerAction getTutorMeActionByName(String name){
        return tutorMeActions.get(name);
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("[LearnerActionsManager: actions = {");
        
        if(getLearnerActions() != null && getLearnerActions().getLearnerAction() != null) {
            
            for (generated.dkf.LearnerAction action : getLearnerActions().getLearnerAction()) {
                sb.append("\n[").append(action.getDisplayName()).append(" type = ").append(action.getType().name());
                if(action.getLearnerActionParams() != null){
                    
                    if(action.getLearnerActionParams() != null){
                        Serializable actionParams = action.getLearnerActionParams();
                        if(actionParams instanceof generated.dkf.TutorMeParams){
                            generated.dkf.TutorMeParams tutorMeParams = (generated.dkf.TutorMeParams)actionParams;
                            
                            Serializable configuration = tutorMeParams.getConfiguration();
                            if(configuration instanceof generated.dkf.AutoTutorSKO){
                                generated.dkf.AutoTutorSKO autoTutorSKO = (generated.dkf.AutoTutorSKO)configuration;
                                Serializable scriptType = autoTutorSKO.getScript();
                                
                                if(scriptType instanceof generated.dkf.LocalSKO){
                                    generated.dkf.LocalSKO localSKO = (generated.dkf.LocalSKO)scriptType;
                                    sb.append(", AutoTutor Local SKO = ").append(localSKO.getFile());
                                }else if(scriptType instanceof generated.dkf.ATRemoteSKO){
                                    generated.dkf.ATRemoteSKO remoteSKO = (generated.dkf.ATRemoteSKO)scriptType;
                                    sb.append(", AutoTutor remote SKO = ").append(remoteSKO.getURL());
                                }
                                
        
                            }else if(configuration instanceof String){
                                sb.append(", conversation file = ").append(configuration);
                            }
                        }else if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                            generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                            sb.append(", strategyReference = ").append(strategyRef.getName());
                        }
                    }
                }
                
                if(action.getDescription() != null){
                    sb.append(", description = ").append(action.getDescription());
                }
                
                sb.append("],");
            }
            
        } else {
            sb.append("null");
        }
        
        sb.append("}");
        
        return sb.toString();
    }
}
