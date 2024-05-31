/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import generated.conversation.Choice;
import generated.conversation.End;
import generated.conversation.Message;
import generated.conversation.Question;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeAction;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeActions;

/**
 * This class is responsible for maintaining state about a single conversation tree for a single user
 * in a domain session.
 * 
 * @author mhoffman
 *
 */
public class ConversationTreeModel implements ConversationModelInterface{
    
    /** the text to show when the conversation reaches the 'end' node of the tree */
    private static final String DEFAULT_END_TEXT = "";
    
    /** the unique conversation id for all users of this domain module instance */
    private int chatId;

    /** contains the authored conversational elements */
    private generated.conversation.Conversation conversation;
    
    /** the current node in the tree */
    private int currentNodeId;
    
    /** used for updating performance assessments for performance nodes based on conversation assessments */
    private ConversationAssessmentHandlerInterface conversationAssessmentHandler;
    
    /** used for presenting conversation updates to the learner */
    private DomainKnowledgeActionInterface domainKnowledgeActionInterface;
    
    /**
     * used to handle retrieval of conversation variables that have 
     * been provided during course execution
     */
    private ConversationVarsHandler conversationVarHandler;
    
    /**
     * mapping of unique conversation node id (w/in a conversation tree file) to the 
     * node object containing the contents for that node (e.g. the question text to present)
     */
    //this could be replaced with a tree data structure
    private Map<Integer, Serializable> nodeMap;
    
    /** flag used to indicate whether the conversation has started or not */
    private boolean hasStarted = false;
    
    /**
     * Set attributes and validate conversation.
     * Note: this class should be created by the ConversationTreeManager class, hence the lack of constructor modifier
     * 
     * @param chatId the unique conversation id for all users of this domain module instance
     * @param conversation contains the authored conversational elements
     * @param conversationVarHandler used to handle retrieval of conversation variables that have 
     * been provided during course execution.
     * @param domainKnowledgeManager used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     * @throws DetailedException if there was a validation problem
     */
    ConversationTreeModel(int chatId, generated.conversation.Conversation conversation, ConversationVarsHandler conversationVarHandler,
            ConversationAssessmentHandlerInterface conversationAssessmentHandler,
            DomainKnowledgeActionInterface domainKnowledgeActionInterface) throws DetailedException{
        
        if(chatId < 1){
            throw new IllegalArgumentException("The chat id must be greater than 0");
        }
        
        this.chatId = chatId;
        
        if(conversation == null){
            throw new IllegalArgumentException("The conversation can't be null.");
        }
        
        this.conversation = conversation;
        
        if(conversationVarHandler == null){
            throw new IllegalArgumentException("The conversation variable handler can't be null.");
        }
        
        this.conversationVarHandler = conversationVarHandler;
        
        if(conversationAssessmentHandler == null){
            throw new IllegalArgumentException("The conversation assessment handler can't be null.");
        }
        
        this.conversationAssessmentHandler = conversationAssessmentHandler;
        
        if(domainKnowledgeActionInterface == null){
            throw new IllegalArgumentException("The domain knowledge action interface can't be null.");
        }
        
        this.domainKnowledgeActionInterface = domainKnowledgeActionInterface;
        
        currentNodeId = this.conversation.getStartNodeId().intValue();
        
        nodeMap = new HashMap<>();
        checkConversation(conversation, nodeMap);
    }
    
    /**
     * Return the conversation variable handler used to handle retrieval of conversation variables that have 
     * been provided during course execution.
     * 
     * @return the instance used by this conversation tree
     */
    public ConversationVarsHandler getConversationVarsHandler(){
        return conversationVarHandler;
    }
    
    /**
     * This will check the conversation elements for logic errors.
     * 
     * @param conversation the conversation to check
     * @param nodeMap a map of conversation tree node unique id to the node object.  Can be null if caller doesn't need to know this information.
     * @return validation results
     * @throws DetailedException if there was a problem with the conversation elements 
     * (e.g. duplicate node id, no path from beginning to end in the tree)
     */
    public static GIFTValidationResults checkConversation(generated.conversation.Conversation conversation, Map<Integer, Serializable> nodeMap) throws DetailedException{
        
        if(conversation == null){
            throw new IllegalArgumentException("The conversation can't be null.");
        }
        
        if(nodeMap == null){
            //caller doesn't want the node map
            nodeMap = new HashMap<>();
        }
        
        GIFTValidationResults validationResults = new GIFTValidationResults();
        
        //
        // Build node map and check for duplicate node ids
        //
        
        if(conversation.getMessages() != null){
            
            for(Message messageNode : conversation.getMessages().getMessage()){
                
                Serializable existingNode = nodeMap.put(messageNode.getNodeId().intValue(), messageNode);
                if(existingNode != null){
                    validationResults.addImportantIssue(
                            new DetailedException("Found a duplicate conversation node id", "The message node id "+messageNode.getNodeId().intValue()+" is already assigned to another conversation node.  Every node must have a unique identifier.", null));
                }
            }
        }
        
        if(conversation.getQuestions() != null){
            
            for(Question questionNode : conversation.getQuestions().getQuestion()){
                
                Serializable existingNode = nodeMap.put(questionNode.getNodeId().intValue(), questionNode);
                if(existingNode != null){
                    validationResults.addImportantIssue(
                            new DetailedException("Found a duplicate conversation node id", "The question node id "+questionNode.getNodeId().intValue()+" is already assigned to another conversation node.  Every node must have a unique identifier.", null));
                }
            }
        }
        
        if(conversation.getChoices() != null){
            
            for(Choice choiceNode : conversation.getChoices().getChoice()){
                
                Serializable existingNode = nodeMap.put(choiceNode.getNodeId().intValue(), choiceNode);
                if(existingNode != null){
                    validationResults.addImportantIssue(
                            new DetailedException("Found a duplicate conversation node id", "The choice node id "+choiceNode.getNodeId().intValue()+" is already assigned to another conversation node.  Every node must have a unique identifier.", null));
                }
                
                //
                // check choice node concept assessments, if any
                //
                if(choiceNode.getAssessment() != null){
                    
                    try{
                        checkConceptAssessments(choiceNode.getAssessment());
                    }catch(Exception e){
                        validationResults.addImportantIssue(
                                new DetailedException("There was a problem validating the concept assessment rules for the choice node with id "+choiceNode.getNodeId(), 
                                "The validation problem for the choice node reads:\n"+e.getMessage(), e));
                    }
                }
            }
        }
        
        if(conversation.getEnds() != null){
            
            for(End endNode : conversation.getEnds().getEnd()){
                
                Serializable existingNode = nodeMap.put(endNode.getNodeId().intValue(), endNode);
                if(existingNode != null){
                    validationResults.addImportantIssue(
                            new DetailedException("Found a duplicate conversation node id", "The end node id "+endNode.getNodeId().intValue()+" is already assigned to another conversation node.  Every node must have a unique identifier.", null));
                }
                
                //
                // check end node concept assessments, if any
                //
                if(endNode.getAssessment() != null){
                    
                    try{
                        checkConceptAssessments(endNode.getAssessment());
                    }catch(Exception e){
                        validationResults.addImportantIssue(
                                new DetailedException("There was a problem validating the concept assessment rules for the end node with id "+endNode.getNodeId(), 
                                "The validation problem for the end node reads:\n"+e.getMessage(), e));
                    }
                }
            }
        }        
        
        //make sure there is at least 1 question or 1 message node
        generated.conversation.Conversation.Messages messages = conversation.getMessages();
        generated.conversation.Conversation.Questions questions = conversation.getQuestions();
        boolean hasMessage = messages != null && messages.getMessage() != null && !messages.getMessage().isEmpty();
        boolean hasQuestion = questions != null && questions.getQuestion() != null && !questions.getQuestion().isEmpty();
        if(!hasMessage && !hasQuestion){
            validationResults.addImportantIssue(
                    new DetailedException("The conversation must have at least one message or one question element.", "A conversation must have at least one message or one question element in order to present something to the learner.", null));
        } 
        
        //make sure there is a path from beginning to end in the tree
        try{
            Set<Integer> visitedNodes = new HashSet<Integer>();
            checkForPathEnds(conversation.getStartNodeId().intValue(), nodeMap, visitedNodes);
        }catch(RuntimeException e){
            validationResults.addImportantIssue(
                    new DetailedException("The conversation tree is incomplete.", "There is at least one path in the tree that doesn't terminate at an 'End' node.\n"+e.getMessage(), e));
        }
        
        return validationResults;
    }
    
    /**
     * Check the concept assessments for logic errors not checked for in the schema.
     * 
     * @param conceptAssessments the assessments to analyze
     * @throws Exception if a problem was found (e.g. duplicate concept names)
     */
    private static void checkConceptAssessments(List<generated.conversation.Assessment> conceptAssessments) throws Exception{
        
        Set<String> assessedConcepts = new HashSet<>();
        for(generated.conversation.Assessment assessment : conceptAssessments){
            
            if(assessedConcepts.contains(assessment.getConcept().toLowerCase())){
                throw new Exception("Found duplicate concept named '"+assessment.getConcept()+"' in the assessment rules.  Only one assessment rule per concept is allowed.");
            }
            
            assessedConcepts.add(assessment.getConcept().toLowerCase());
        }
    }
    
    /**
     * Recursively check that all paths in the conversation tree terminate with an 'end' node.
     * 
     * @param nodeId the current node id to check it's descendant nodes for an 'end' node.
     * @param nodeMap map of unique conversation tree node id to the node element (e.g. message node)
     * @param visitedNodes collection of nodes already visited during this recursive check for path end nodes.  This prevents
     * endless recursion due to a looped authored in the tree
     * @throws RuntimeException if a path is found to not end correctly
     */
    private static void checkForPathEnds(int nodeId, Map<Integer, Serializable> nodeMap, Set<Integer> visitedNodes) throws RuntimeException{
        
        if(visitedNodes.contains(nodeId)){
            return;
        }
        
        visitedNodes.add(nodeId);
        Serializable nodeObj = nodeMap.get(nodeId);

        if(nodeObj instanceof End){
            return;
        }else if(nodeObj instanceof Message){
            checkForPathEnds(((Message)nodeObj).getChildNodeId().intValue(), nodeMap, visitedNodes);
            return;
        }else if(nodeObj instanceof Question){
            
            for(BigInteger choiceId : ((Question)nodeObj).getChoices().getChoiceId()){
                checkForPathEnds(choiceId.intValue(), nodeMap, visitedNodes);
            }
            
            //all choices to this question resulted in an end nodes
            return;
            
        }else if(nodeObj instanceof Choice){
            checkForPathEnds(((Choice)nodeObj).getChildNodeId().intValue(), nodeMap, visitedNodes);
            return;
        }else if(nodeObj == null){
            throw new RuntimeException("Unable to determine if all paths in the tree end with an end node because the node with id "+nodeId+" could not be found.");
        }
        
        throw new RuntimeException("Unable to determine if all paths in the tree end with an end node because an unhandled node in the tree was found (node id = "+nodeId+").");
        
    }
    
    /**
     * Populate the list with the next nodes in the conversation tree.
     * The list will contain every node from the current node to a question or end node.
     * 
     * @param nextNodes collection of nodes that are next in the tree and need to be displayed
     * to the learner.  Can't be null.
     * This list will contain more than 1 item if the current next node in the conversation
     * is a message node.
     */
    private void getNextNodes(List<Serializable> nextNodes){
        
        if(nextNodes == null){
            throw new IllegalArgumentException("The next nodes list can't be null.");
        }
                
        Serializable currentNodeObj = nodeMap.get(currentNodeId);
        if(currentNodeObj instanceof Question){
            //reached the end of the line as the next nodes are choices
            nextNodes.add(currentNodeObj);
            
        }else if(currentNodeObj instanceof Message){
            //get next node after message node
            nextNodes.add(currentNodeObj);
            
            currentNodeId = ((Message)currentNodeObj).getChildNodeId().intValue();
            getNextNodes(nextNodes);
        }else{
            //must be an end node
            nextNodes.add(currentNodeObj);
        }
    }
    
    
    /**
     * Replace conversation variables in the text provided with the variable values.  If a variable value
     * is not found the variable syntax will remain in the text.
     *  
     * @param text the text to replace conversation variables in
     * @return a new string with conversation variables replaced with variable values where available.
     */
    private String replaceVariables(String text){   
        
        if(text == null || text.isEmpty()){
            throw new IllegalArgumentException("The text can't be null or empty.");
        }

        Matcher matcher = ConversationTreeFileHandler.VARIABLE_PATTERN.matcher(text);
        
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String replacement = conversationVarHandler.getValue(matcher.group(1));
            builder.append(text.substring(i, matcher.start()));
            if (replacement == null){
                builder.append(matcher.group(0));
            }else{
                builder.append(replacement);
            }
            
            i = matcher.end();
        }
        builder.append(text.substring(i, text.length()));
        return builder.toString();
    }
    
    /**
     * Return the Choice conversation tree nodes referenced by the question node specified
     * by the id.
     * 
     * @param questionNodeId identifies a particular question in the conversation tree
     * @return list of choice nodes for the question
     * @throws DetailedException if there was a problem finding a choice node for the question referenced
     * in the conversation tree
     */
    private List<Choice> getQuestionChoices(Integer questionNodeId) throws DetailedException{
        
        if(questionNodeId == null || questionNodeId < 1){
            throw new IllegalArgumentException("The question node id of "+questionNodeId+" is not valid.  The value must be a number greater than zero.");
        }
        
        Serializable questionNodeObj = nodeMap.get(questionNodeId);
        if(questionNodeObj instanceof Question){
            
            List<Choice> choices = new ArrayList<>();
            for(BigInteger choiceNodeId : ((Question)questionNodeObj).getChoices().getChoiceId()){
                
                Serializable choiceNodeObj = nodeMap.get(choiceNodeId.intValue());
                if(choiceNodeObj == null){
                    throw new DetailedException("Failed to find a choice node for the question", 
                            "The question (node id "+questionNodeId+") references a choice node id "+choiceNodeId+" which doesn't exist.", null);
                }else if(choiceNodeObj instanceof Choice){
                    choices.add((Choice) choiceNodeObj);
                }else{
                    throw new DetailedException("Failed to find a choice node for the question", 
                            "The question (node id "+questionNodeId+") references a choice node id "+choiceNodeId+" which exist but is not a choice node.", null);
                }
            }
            
            return choices;
        }else{
            throw new IllegalArgumentException("The node for "+questionNodeId+" is not a question node in the conversation.");
        }
    }
    
    /**
     * Use the learner's choice text to find the choice node in the conversation tree under the current
     * question node.  Use that choice node to select the next node in the conversation.
     * 
     * Note: the method doesn't have a modifier because it should be called by the ConversationTreeMananger class.
     * 
     * @param currentQuestionChoice the text of the question's choice chosen by the learner.  Can't be null or empty.
     * @throws DetailedException if there was a problem finding the current question node being asked or the
     * question's choice node based on the text provided.
     */
    void selectedQuestionChoice(String currentQuestionChoice) throws DetailedException{
        
        if(currentQuestionChoice == null || currentQuestionChoice.isEmpty()){
            throw new IllegalArgumentException("The question choice text can't be null or empty.");
        }
        
        Serializable currentNodeObj = nodeMap.get(currentNodeId);
        if(!(currentNodeObj instanceof Question)){
            throw new DetailedException("Unable to use the learner's chat input to select the corresponding conversation tree question choice.", 
                    "The current conversation node is not a question node therefore it is not an appropriate time to try and select a choice node based on the learner's chat input:\n"+currentQuestionChoice, null);
        }
        
        //determine which choice node id corresponds to the choice text argument
        boolean found = false;
        List<Choice> choices = getQuestionChoices(currentNodeId);
        for(Choice choice : choices){
            
            if(choice.getText().equals(currentQuestionChoice)){
                //found matching choice node, set next node
                
                currentNodeId = choice.getChildNodeId().intValue();
                found = true;
                
                //update assessment
                if(choice.getAssessment() != null){
                    List<ConversationAssessment> assessments = ConversationAssessment.createListInstance(choice.getAssessment());
                    
                    if(!assessments.isEmpty()){
                        conversationAssessmentHandler.assessPerformanceFromConversation(assessments);
                    }
                }
                
                break;
            }
        }
        
        if(!found){
            throw new DetailedException("Unable to use the learner's chat input to select the corresponding conversation tree question choice.", 
                    "The current question conversation node doesn't have a choice node that matches the learner's chat input:\n"+currentQuestionChoice, null);
        }        
    }
    
    @Override
    public void deliverNextActions(){
        
        List<Serializable> nextNodes = new ArrayList<>(1);
        getNextNodes(nextNodes);
        
        ConversationTreeActions actions = buildConversationActions(nextNodes);
        domainKnowledgeActionInterface.handleDomainActionWithLearner(actions);
    }
    
    /**
     * Create conversational tree actions that can be used to display information to the learner based
     * on the next nodes in the conversation tree.
     * 
     * @param nextNodes contains the next nodes in the conversation tree to present (e.g. Message, Question)
     * @return the conversational tree actions to be displayed to the learner
     */
    private ConversationTreeActions buildConversationActions(List<Serializable> nextNodes){
        
        List<ConversationTreeAction> actionsList = new ArrayList<>();
        for(Serializable nextNode : nextNodes){
            
            if(nextNode instanceof generated.conversation.Message){
                generated.conversation.Message message = (generated.conversation.Message)nextNode;  
                String text = replaceVariables(message.getText());
                ConversationTreeAction action = new ConversationTreeAction(text, null);
                actionsList.add(action);
                
            }else if(nextNode instanceof Question){
                
                Question question = (Question)nextNode;
                
                //get question choices
                List<String> choices = new ArrayList<>();
                List<Choice> choiceNodes = getQuestionChoices(question.getNodeId().intValue());
                for(Choice choice : choiceNodes){
                    String choiceText = replaceVariables(choice.getText());
                    choices.add(choiceText);
                }
                
                String text = replaceVariables(question.getText());
                ConversationTreeAction action = new ConversationTreeAction(text, choices);
                actionsList.add(action);
                
            }else{
                //an end node
                
                ConversationTreeAction action = new ConversationTreeAction(DEFAULT_END_TEXT, null);
                action.setConversationEnd(true);
                actionsList.add(action);
            }
        }//end for
        
        ConversationTreeActions actions = new ConversationTreeActions(chatId, actionsList);
        return actions;
    }

    @Override
    public void start() {

        if(!hasStarted){
            hasStarted = true;
            
            deliverNextActions();
        }
    }

    @Override
    public void stop() {
        //nothing to do right now
    }
}
