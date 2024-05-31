/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;
import mil.arl.gift.domain.DomainModuleProperties;

/**
 *  This class is responsible for managing AutoTutor conversation(s) for a single domain session.
 *  
 * @author mhoffman
 *
 */
public class AutoTutorManager implements ConversationManagerInterface{
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AutoTutorManager.class);
    
    /** 
     * mapping of unique conversation id (chat id) to the data model responsible for maintaining state 
     * about that conversation 
     */
    private Map<Integer, AutoTutorModel> conversationIdToModel = new HashMap<>();
    
    /**
     * Create the AutoTutor conversation data model for the AutoTutor SKO authored.  The data model is responsible
     * for maintaining state about the conversation.
     * 
     * Note: AutoTutor models should be created by the ConversationManager class, hence the lack of constructor modifier
     * 
     * @param chatId the unique conversation id for all users of this domain module instance
     * @param configuration contains either a course folder relative file name or a network resource URL reference to an AutoTutor SKO
     * @param courseFolder the course folder that would contains a course folder relative SKO file
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a problem creating the AutoTutor conversation model
     */
    void createAutoTutorModel(int chatId, generated.course.AutoTutorSKO configuration, 
            AbstractFolderProxy courseFolder, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        Serializable scriptType = configuration.getScript();
        AutoTutorModel model;
        
        if(scriptType instanceof generated.course.ATRemoteSKO){
            //SKO is NOT local to the ATWS but is already a network available resource
            generated.course.ATRemoteSKO remoteSKO = (generated.course.ATRemoteSKO)scriptType;
            
            model = createRemoteSKOAutoTutorModel(chatId, remoteSKO.getURL().getAddress(), courseFolder, conversationAssessmentHandler, domainKnowledgeActionInterface);
            
        }else if(scriptType instanceof generated.course.LocalSKO){
            //SKO is NOT local to the ATWS but is local to this GIFT instance (i.e. a file on this computer)
            generated.course.LocalSKO localSKO = (generated.course.LocalSKO)scriptType;
            
            model = createLocalSKOAutoTutorModel(chatId, localSKO.getFile(), courseFolder, conversationAssessmentHandler, domainKnowledgeActionInterface);            
            
        }else{
            throw new IllegalArgumentException("Received unhandled script type of "+scriptType);
        } 
        
        conversationIdToModel.put(chatId, model);
    }

    /**
     * Create the AutoTutor conversation data model for the AutoTutor SKO authored.  The data model is responsible
     * for maintaining state about the conversation.
     * 
     * Note: AutoTutor models should be created by the ConversationManager class, hence the lack of constructor modifier
     * 
     * @param chatId the unique conversation id for all users of this domain module instance
     * @param configuration contains either a course folder relative file name or a network resource URL reference to an AutoTutor SKO
     * @param courseFolder the course folder that would contains a course folder relative SKO file
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a problem creating the AutoTutor conversation model
     */
    void createAutoTutorModel(int chatId, generated.dkf.AutoTutorSKO configuration, 
            AbstractFolderProxy courseFolder, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        Serializable scriptType = configuration.getScript();
        AutoTutorModel model;
        
        if(scriptType instanceof generated.dkf.ATRemoteSKO){
            //SKO is NOT local to the ATWS but is already a network available resource
            generated.dkf.ATRemoteSKO remoteSKO = (generated.dkf.ATRemoteSKO)scriptType;
            
            model = createRemoteSKOAutoTutorModel(chatId, remoteSKO.getURL().getAddress(), courseFolder, conversationAssessmentHandler, domainKnowledgeActionInterface);
            
        }else if(scriptType instanceof generated.dkf.LocalSKO){
            //SKO is NOT local to the ATWS but is local to this GIFT instance (i.e. a file on this computer)
            generated.dkf.LocalSKO localSKO = (generated.dkf.LocalSKO)scriptType;
            
            model = createLocalSKOAutoTutorModel(chatId, localSKO.getFile(), courseFolder, conversationAssessmentHandler, domainKnowledgeActionInterface);            
            
        }else{
            throw new IllegalArgumentException("Received unhandled script type of "+scriptType);
        } 
        
        conversationIdToModel.put(chatId, model);
    }
    
    // Deprecated as of 8/18 - new AutoTutor ACE server no longer has an API to upload a sko from a GIFT course folder
    @Deprecated
    private AutoTutorModel createLocalSKOAutoTutorModel(int chatId, String scriptFileName, 
            AbstractFolderProxy courseFolder, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        try{
            FileProxy xmlProxy = courseFolder.getRelativeFile(scriptFileName);
            return new AutoTutorModel(chatId, xmlProxy, conversationAssessmentHandler, domainKnowledgeActionInterface);

        }catch(IOException e){
            throw new DetailedException("Failed to create the AutoTutor conversation model.", "There was a problem when trying to retrieve the AutoTutor SKO file from GIFT of '"+scriptFileName+"' in '"+courseFolder+"'.", e);
        }
    }
    
    private AutoTutorModel createRemoteSKOAutoTutorModel(int chatId, String scriptNameOrURL, 
            AbstractFolderProxy courseFolder, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        String scriptURL = scriptNameOrURL;
        
        //Check if the URL is a file on this computer
        try{
            if(courseFolder.fileExists(scriptNameOrURL)){
                
                //Add the network address of the hosted Domain module's domain folder
                String networkURL;
                try {
                    networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + "/"; 
                    
                } catch (Exception ex) {
                    logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
                    networkURL = DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
                }
                
                scriptURL = networkURL + scriptNameOrURL;
                
                if(logger.isDebugEnabled()){
                    logger.debug("The URL provided maps to the file of "+scriptNameOrURL+" on this computer, therefore attempting to provide the domain module hosted URL of "+
                            scriptURL+" to the ATWS.  This will only work if the ATWS has access to that file on your machine (i.e. appropriate network and permission configuration).");
                }
            }
        }catch(@SuppressWarnings("unused") Exception e){
            //don't care cause maybe this isn't a local file to begin with
        }
        
        return new AutoTutorModel(chatId, scriptURL, conversationAssessmentHandler, domainKnowledgeActionInterface);
    }

    @Override
    public void addUserResponse(ChatLog chatLog) {
        
        int chatId = chatLog.getChatId();
        
        if(chatId < 1){
            throw new IllegalArgumentException("Invalid chat id of "+chatId+".  Chat id must be a value greater than zero.");
        }
        
        AutoTutorModel model = conversationIdToModel.get(chatId);
        
        if(model == null){
            throw new IllegalArgumentException("Unable to find the conversation model based on chat id of "+chatId+".  Did you create the conversation model first?");
        }
        
        model.addUserResponse(chatLog);
        
        //display next conversation elements
        //TODO: do this after a pedagogical request to continue the conversation, maybe leverage course state msg?
        model.deliverNextActions();        
    }

    @Override
    public void startConversation(int chatId) {
               
        if(chatId < 1){
            throw new IllegalArgumentException("Invalid chat id of "+chatId+".  Chat id must be a value greater than zero.");
        }
        
        AutoTutorModel model = conversationIdToModel.get(chatId);
        
        if(model == null){
            throw new IllegalArgumentException("Unable to find the conversation model based on chat id of "+chatId+".  Did you create the conversation model first?");
        }   
        
        model.start();
    }

    @Override
    public void stopConversation(int chatId) {
        
        if(chatId < 1){
            throw new IllegalArgumentException("Invalid chat id of "+chatId+".  Chat id must be a value greater than zero.");
        }
        
        AutoTutorModel model = conversationIdToModel.get(chatId);
        
        if(model == null){
            throw new IllegalArgumentException("Unable to find the conversation model based on chat id of "+chatId+".  Did you create the conversation model first?");
        }   
        
        model.stop();
    }
    
    @Override
    public void stopAllConversations(){
        
        for(AutoTutorModel model : conversationIdToModel.values()){
            
            try{
                model.stop();
            }catch(Exception e){
                logger.error("Caught exception from misbehaving AutoTutor model when trying to forcefully stop it.", e);
            }
        }
    }

}
