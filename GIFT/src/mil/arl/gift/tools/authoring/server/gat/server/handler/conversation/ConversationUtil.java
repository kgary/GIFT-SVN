/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.ConversationHelper;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.TreeNodeEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import generated.conversation.End;

/**
 * This class provides methods for converting the conversation tree JSON representation
 * that comes from the authoring tool to the schema representation (i.e. xml file) and
 * vice versa.
 * 
 * @author mhoffman
 *
 */
public class ConversationUtil {
    
    /**
     * Convert the conversation object into a JSON representation of that data.
     * 
     * @param conversation contains conversation attributes (e.g tree nodes) to create json objects
     * for.  Can't be null.
     * @return a new JSON object with the attributes from the conversation object
     */
    public static JSONObject toJSON(generated.conversation.Conversation conversation){
        
        if(conversation == null){
            throw new IllegalArgumentException("The conversation can't be null.");
        }
        
        JSONObject rootObj = new JSONObject();
        try{
            rootObj.put(ConversationHelper.CONVERSATION_NAME_KEY, conversation.getName());
            rootObj.put(ConversationHelper.AUTHORS_DESC_KEY, conversation.getAuthorsDescription());
            rootObj.put(ConversationHelper.LEARNERS_DESC_KEY, conversation.getLearnersDescription());
            rootObj.put(ConversationHelper.VERSION_KEY, conversation.getVersion());
            
            Map<Integer, Serializable> nodeMap = buildNodeMap(conversation);            
            int startNodeId = conversation.getStartNodeId().intValue();
        
            JSONObject treeObj = new JSONObject();
            JSONArray linksArray = new JSONArray();            
			List<Integer> encodedNodes = new ArrayList<>();

            jsonEncodeNode(startNodeId, treeObj, linksArray, nodeMap, encodedNodes);            
			treeObj.put(ConversationHelper.LINKS_KEY, linksArray);
            rootObj.put(ConversationHelper.TREE_KEY, treeObj);
            
        }catch(JSONException e){
            throw new DetailedException("Failed to convert the conversation '"+conversation.getName()+"' into a JSON object.", 
                    "A problem occurred while creating a JSON object from the conversation.  The message reads:\n"+e.getMessage(), e);
        }
        
        return rootObj;
    }
    
    /**
     * JSON encode the conversation tree object associated with the node id specified.
     * 
     * @param nodeId the unique conversation tree node to json encode its attributes and any descendants
     * @param nodeObj the json object to place attributes in
     * @param linksArray contains the JSON encoded conversation tree links that will contain node link references
     * for tree nodes that have already been defined as a child of another node
     * @param nodeMap contains all the conversation tree serializable objects to pull from when json encoding
     * @param encodedNodes list of conversation tree unique node ids that have already been encoded as a child of another node
     * @throws JSONException if there was a problem encoding this node or any descendant node
     */
    private static void jsonEncodeNode(int nodeId, JSONObject nodeObj, 
            JSONArray linksArray, Map<Integer, Serializable> nodeMap, List<Integer> encodedNodes) throws JSONException{
        
        Serializable node = nodeMap.get(nodeId);
        if(node == null){
            throw new DetailedException("Failed to encode a conversation node", 
                    "The conversation node with id "+nodeId+" could not be found among "+nodeMap+" known nodes", null);
        }
        
        if(node instanceof generated.conversation.Message){
            generated.conversation.Message messageNode = (generated.conversation.Message)node;
            
            nodeObj.put(ConversationHelper.NODE_NAME_KEY, messageNode.getText());
            nodeObj.put(ConversationHelper.NODE_ID_KEY, messageNode.getNodeId());
            nodeObj.put(ConversationHelper.NODE_TYPE_KEY, TreeNodeEnum.MESSAGE_NODE.getName());
            
            encodedNodes.add(messageNode.getNodeId().intValue());
            
            //
            // the descendants
            // [messages should have only 1 child]
            //
            if(messageNode.getChildNodeId() != null) {
            	
            	int childNodeId = messageNode.getChildNodeId().intValue();            
	            if(encodedNodes.contains(childNodeId)){
	                //create link for this node that is already encoded as a child 
	                //somewhere in the JSON encoding of the tree
	                try{
	                    addLinksObject(nodeId, childNodeId, linksArray);
	                }catch(JSONException e){
	                    throw new DetailedException("Failed to encode a conversation node.", 
	                            "There was a problem encoding a links object for the message node with id "+nodeId+" for its referenced child with id "+childNodeId+".  The message reads:\n"+e.getMessage(), e);
	                }
	                
	                return;
	            }else{                
	                encodedNodes.add(childNodeId);
	            }
	            
	            JSONArray children = new JSONArray();
	            JSONObject child = new JSONObject();
	            jsonEncodeNode(childNodeId, child, linksArray, nodeMap, encodedNodes);
	            
	            children.put(child);
	            nodeObj.put(ConversationHelper.CHILDREN_KEY, children);            
            
            } else {            	
            	generateEndNode(nodeObj, linksArray, nodeMap, encodedNodes);
            }
            
        }else if(node instanceof generated.conversation.Question){
            generated.conversation.Question questionNode = (generated.conversation.Question)node;
            
            nodeObj.put(ConversationHelper.NODE_NAME_KEY, questionNode.getText());
            nodeObj.put(ConversationHelper.NODE_ID_KEY, questionNode.getNodeId());
            nodeObj.put(ConversationHelper.NODE_TYPE_KEY, TreeNodeEnum.QUESTION_NODE.getName());
            
            //
            // the descendants
            //
            
            JSONArray children = new JSONArray();
            for(BigInteger choiceNodeId : questionNode.getChoices().getChoiceId()){                

                if(encodedNodes.contains(choiceNodeId.intValue())){
                    //create link for this node that is already encoded as a child 
                    //somewhere in the JSON encoding of the tree
                    try{
                        addLinksObject(nodeId, choiceNodeId.intValue(), linksArray);
                    }catch(JSONException e){
                        throw new DetailedException("Failed to encode a conversation node.", 
                                "There was a problem encoding a links object for the question node with id "+nodeId+" for its referenced choice child with id "+choiceNodeId+".  The message reads:\n"+e.getMessage(), e);
                    }
                    
                    continue;
                }else{                
                    encodedNodes.add(choiceNodeId.intValue());
                }
                
                JSONObject child = new JSONObject();
                jsonEncodeNode(choiceNodeId.intValue(), child, linksArray, nodeMap, encodedNodes);                
                children.put(child);
            }
            
            //insert an add node as the last child of this question
            int maxNodeID = 0;
    		
    		for(Integer id : nodeMap.keySet()){
    			
    			if(id > maxNodeID){
    				maxNodeID = id;
    			}
    		}
    		
    		int addNodeId = maxNodeID + 1;
    		
    		nodeMap.put(addNodeId, questionNode);
    		
    		encodedNodes.add(addNodeId);
    		
    		JSONObject addObj = new JSONObject();
            
            addObj.put(ConversationHelper.NODE_ID_KEY, addNodeId);
            addObj.put(ConversationHelper.NODE_TYPE_KEY, TreeNodeEnum.ADD_NODE.getName());
            children.put(addObj);
            
            nodeObj.put(ConversationHelper.CHILDREN_KEY, children); 
            
        }else if(node instanceof generated.conversation.Choice){
            generated.conversation.Choice choiceNode = (generated.conversation.Choice)node;
            
            nodeObj.put(ConversationHelper.NODE_NAME_KEY, choiceNode.getText());
            nodeObj.put(ConversationHelper.NODE_ID_KEY, choiceNode.getNodeId());
            nodeObj.put(ConversationHelper.NODE_TYPE_KEY, TreeNodeEnum.CHOICE_NODE.getName());
            
            //
            // the descendants
            // [choice should have only 1 child]
            //
            if(choiceNode.getChildNodeId() != null) {
            	
	            JSONArray children = new JSONArray();
	            int childNodeId = choiceNode.getChildNodeId().intValue();
	            
	            if(encodedNodes.contains(childNodeId)){
	                //create link for this node that is already encoded as a child 
	                //somewhere in the JSON encoding of the tree
	                try{
	                    addLinksObject(nodeId, childNodeId, linksArray);
	                }catch(JSONException e){
	                    throw new DetailedException("Failed to encode a conversation node.", 
	                            "There was a problem encoding a links object for the choice node with id "+nodeId+" for its referenced child with id "+childNodeId+".  The message reads:\n"+e.getMessage(), e);
	                }
	                
	                return;
	            }else{                
	                encodedNodes.add(childNodeId);
	            }
	            
	            JSONObject child = new JSONObject();
	            jsonEncodeNode(childNodeId, child, linksArray, nodeMap, encodedNodes);                
	            children.put(child);
	            
	            nodeObj.put(ConversationHelper.CHILDREN_KEY, children); 
            
            } else {
            	generateEndNode(nodeObj, linksArray, nodeMap, encodedNodes);
            }
            
            //
            // the assessment table
            //
            JSONArray assessmentsArray = new JSONArray();
            List<generated.conversation.Assessment> assessments = choiceNode.getAssessment();
            for(generated.conversation.Assessment assessment : assessments){
                
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(ConversationHelper.CONCEPT_KEY, assessment.getConcept());
                jsonObj.put(ConversationHelper.ASSESSMENT_KEY, assessment.getLevel());
                jsonObj.put(ConversationHelper.CONFIDENCE_KEY, assessment.getConfidence().toString());
                
                assessmentsArray.put(jsonObj);
            }
            
            nodeObj.put(ConversationHelper.ASSESSMENTS_KEY, assessmentsArray);
            
        }else if(node instanceof generated.conversation.End){
            generated.conversation.End endNode = (generated.conversation.End)node;
            
            //insert an add node before each end node
            int maxNodeID = 0;
    		
    		for(Integer id : nodeMap.keySet()){
    			
    			if(id > maxNodeID){
    				maxNodeID = id;
    			}
    		}
    		
    		int addNodeId = maxNodeID + 1;
    		
    		nodeMap.put(addNodeId, endNode);
    		
    		encodedNodes.add(addNodeId);
            
            nodeObj.put(ConversationHelper.NODE_ID_KEY, addNodeId);
            nodeObj.put(ConversationHelper.NODE_TYPE_KEY, TreeNodeEnum.ADD_NODE.getName());
            
            //encode the end node itself
            JSONObject endNodeObj = new JSONObject();
            
            endNodeObj.put(ConversationHelper.NODE_ID_KEY, endNode.getNodeId());
            endNodeObj.put(ConversationHelper.NODE_TYPE_KEY, TreeNodeEnum.END_NODE.getName());
            endNodeObj.put(ConversationHelper.NODE_NAME_KEY, "End");
            
            //
            // the assessment table
            //
            JSONArray assessmentsArray = new JSONArray();
            List<generated.conversation.Assessment> assessments = endNode.getAssessment();
            for(generated.conversation.Assessment assessment : assessments){
                
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(ConversationHelper.CONCEPT_KEY, assessment.getConcept());
                jsonObj.put(ConversationHelper.ASSESSMENT_KEY, assessment.getLevel());
                jsonObj.put(ConversationHelper.CONFIDENCE_KEY, assessment.getConfidence().toString());
                
                assessmentsArray.put(jsonObj);
            }
            
            endNodeObj.put(ConversationHelper.ASSESSMENTS_KEY, assessmentsArray);
            
            JSONArray childrenArray = new JSONArray();
            childrenArray.put(endNodeObj);
            
            nodeObj.put(ConversationHelper.CHILDREN_KEY, childrenArray);
            
        }else{
            //ERROR
            throw new IllegalArgumentException("Found unhandled node type "+node+" associated with node id "+nodeId+".");
        }
        
    }
    
    /**
     * Generates a new end node and attaches it as a child of the given node object
     * 
	 * @param nodeObj the json object to generate a new end node for
     * @param linksArray contains the JSON encoded conversation tree links that will contain node link references
     * for tree nodes that have already been defined as a child of another node
     * @param nodeMap contains all the conversation tree serializable objects to pull from when json encoding
     * @param encodedNodes list of conversation tree unique node ids that have already been encoded as a child of another node
     * @throws JSONException if there was a problem encoding this node or any descendant node
	 */
	private static void generateEndNode(JSONObject nodeObj, JSONArray linksArray, Map<Integer, Serializable> nodeMap,
			List<Integer> encodedNodes) throws JSONException{
		
		//generate an end node as a child for this node, since it doesn't have any children
		int maxNodeID = 0;
		
		for(Integer id : nodeMap.keySet()){
			
			if(id > maxNodeID){
				maxNodeID = id;
			}
		}
		
		int childNodeId = maxNodeID + 1;
		
		End endNode = new End();
		endNode.setNodeId(BigInteger.valueOf(childNodeId));
		
		nodeMap.put(childNodeId, endNode);
		
		encodedNodes.add(childNodeId);
		           	
		JSONArray children = new JSONArray();
	    JSONObject child = new JSONObject();
	    jsonEncodeNode(childNodeId, child, linksArray, nodeMap, encodedNodes);
	     
	    children.put(child);
	    nodeObj.put(ConversationHelper.CHILDREN_KEY, children);
	}

	/**
     * Create a new links JSON object using the provided node identifiers and add the new object to
     * the array provided.
     * 
     * @param sourceNodeId the source conversation tree node unique id to use as the source in the new links object
     * @param targetNodeId the target conversation tree node unique id to use as the target in the new links object
     * @param linksArray contains the JSON links objects for the conversation tree json encoding
     * @throws JSONException if there was a problem create the new json object
     */
    private static void addLinksObject(int sourceNodeId, int targetNodeId, JSONArray linksArray) throws JSONException{
        
        if(linksArray == null){
            throw new IllegalArgumentException("The links array can't be null.");
        }
        
        JSONObject obj = new JSONObject();
        obj.put(ConversationHelper.SOURCE_KEY, sourceNodeId);
        obj.put(ConversationHelper.TARGET_KEY, targetNodeId);
        
        linksArray.put(obj);
    }
    
    /**
     * Build a node map from the conversation elements.
     * 
     * @param conversation contains the conversation tree to build a node map from
     * @return a mapping of nodes by their unique node id in this conversation tree
     */
    private static Map<Integer, Serializable> buildNodeMap(generated.conversation.Conversation conversation){
        
        Map<Integer, Serializable> nodeMap = new HashMap<>();
             
        List<generated.conversation.Message> messages = conversation.getMessages().getMessage();
        for(generated.conversation.Message message : messages){
            nodeMap.put(message.getNodeId().intValue(), message);
        }
        
        List<generated.conversation.Choice> choices = conversation.getChoices().getChoice();
        for(generated.conversation.Choice choice : choices){
            nodeMap.put(choice.getNodeId().intValue(), choice);
        }
        
        List<generated.conversation.Question> questions = conversation.getQuestions().getQuestion();
        for(generated.conversation.Question question : questions){
            nodeMap.put(question.getNodeId().intValue(), question);
        }
        
        List<generated.conversation.End> ends = conversation.getEnds().getEnd();
        for(generated.conversation.End end : ends){
            nodeMap.put(end.getNodeId().intValue(), end);
        }
        
        return nodeMap;
    }
    
    /**
     * Convert the JSON representation of a conversation tree into the conversation object associated
     * with the XML schema.
     * 
     * @param jsonObj contains the conversation tree and metadata information (e.g. author's description).
     * @return a new conversation object
     */
    public static generated.conversation.Conversation fromJSON(JSONObject jsonObj){
        
        if(jsonObj == null){
            throw new IllegalArgumentException("The json object can't be null.");
        }
        
        generated.conversation.Conversation conversation = null;
        
        //
        // conversation header information
        //
        try{
            conversation = new generated.conversation.Conversation();
            conversation.setName(jsonObj.getString(ConversationHelper.CONVERSATION_NAME_KEY));
            if(jsonObj.has(ConversationHelper.AUTHORS_DESC_KEY)) {
            	conversation.setAuthorsDescription(jsonObj.getString(ConversationHelper.AUTHORS_DESC_KEY));
            }
            if(jsonObj.has(ConversationHelper.LEARNERS_DESC_KEY)) {
            	conversation.setLearnersDescription(jsonObj.getString(ConversationHelper.LEARNERS_DESC_KEY));
            }
            if(jsonObj.has(ConversationHelper.VERSION_KEY)) {
            	conversation.setVersion(jsonObj.getString(ConversationHelper.VERSION_KEY));
            }
            
            conversation.setChoices(new generated.conversation.Conversation.Choices());
            conversation.setEnds(new generated.conversation.Conversation.Ends());
            conversation.setMessages(new generated.conversation.Conversation.Messages());
            conversation.setQuestions(new generated.conversation.Conversation.Questions());
                        
        }catch(JSONException e){
            throw new DetailedException("Failed to convert the JSON representation of a conversation into a conversation object.", 
                    "A problem occurred with the conversation header information while creating a conversation object from a JSON object.  The message reads:\n"+e.getMessage(), e);
        }
        
        //
        // decode the tree and build the nodes
        // 
        try{
            JSONObject treeObj = jsonObj.getJSONObject(ConversationHelper.TREE_KEY);
            Map<Integer, List<Integer>> linksMap = new HashMap<>();            
            if(treeObj.has(ConversationHelper.LINKS_KEY)) {
            	JSONArray linksArray = treeObj.getJSONArray(ConversationHelper.LINKS_KEY);
	            for(int index = 0; index < linksArray.length(); index++){
	                
	                JSONObject link = (JSONObject) linksArray.get(index);
	                if(link.has(ConversationHelper.SOURCE_KEY)) {
		                int sourceNodeId = link.getInt(ConversationHelper.SOURCE_KEY);
		                int targetNodeId = link.getInt(ConversationHelper.TARGET_KEY);
		                
		                List<Integer> targetNodes = linksMap.get(sourceNodeId);
		                if(targetNodes == null){
		                    targetNodes = new ArrayList<>();
		                    linksMap.put(sourceNodeId, targetNodes);
		                }
		                
		                targetNodes.add(targetNodeId);
	                }
	            }
            }
            
            //remove any nodes from the tree that are only used for display purposes
            Map<Integer, List<Integer>> removedNodes = new HashMap<Integer, List<Integer>>();           
            removeDisplayNodes(treeObj, removedNodes);
            
            //update links to target nodes based on which nodes were removed
            List<Integer> sourceNodeIdsToRemove = new ArrayList<Integer>();           	
            
        	for(Integer sourceNodeId : linksMap.keySet()){
        		
        		List<Integer> targetNodeIds = linksMap.get(sourceNodeId);
        		
        		for(int i = targetNodeIds.size() - 1; i >= 0 ; i--){
        			
        			Integer targetNodeId = targetNodeIds.get(i);
        			
        			List<Integer> replacementNodes = removedNodes.get(targetNodeId);
        			
        			if(replacementNodes != null){
        				
        				targetNodeIds.remove(i);
        				
        				if(!replacementNodes.isEmpty()){
        					
        					for(int j = replacementNodes.size() - 1; j >= 0; j++){
        						
        						Integer replacementNode = replacementNodes.get(j);
        						
        						targetNodeIds.add(j, replacementNode);
        					}
        					
        				}
        			}
        		}
        		
        		if(removedNodes.containsKey(sourceNodeId)){
        			sourceNodeIdsToRemove.add(sourceNodeId);
        		}
        	}
        	
        	//update links to source nodes based on which nodes were removed
        	for(Integer sourceNodeId : sourceNodeIdsToRemove){
        		
        		List<Integer> targetNodeIds = linksMap.get(sourceNodeId);
        		
        		linksMap.remove(sourceNodeId);
        		
        		List<Integer> replacementNodes = removedNodes.get(sourceNodeId);
        		
        		if(replacementNodes != null){
        			
        			for(Integer replacementNode : replacementNodes){
        				linksMap.put(replacementNode, new ArrayList<Integer>(targetNodeIds));
        			}
        		}
        		
        	}
            
            Serializable rootNode = decodeNode(treeObj, linksMap, conversation);
            
            conversation.setStartNodeId(getNodeId(rootNode));
        }catch(JSONException e){
            throw new DetailedException("Failed to convert the JSON representation of a conversation into a conversation object.", 
                    "A problem occurred with the conversation tree information while creating a conversation object from a JSON object.  The message reads:\n"+e.getMessage(), e);        
        }        
        
        return conversation;
    }
    
    /**
     * Removes any nodes descendant from the given node that are only used for display purposes, such as add nodes. This method
     * should ideally be called before a conversation tree is decoded to avoid trying to decode nodes that aren't actually part 
     * of the conversation schema
     * 
   	 * @param treeObj the node from which descendant display nodes should be deleted
   	 * @param removed nodes a map tracking which nodes have been removed and, if applicable, what nodes have replaced them
   	 */
   	private static void removeDisplayNodes(JSONObject nodeJsonObj, Map<Integer, List<Integer>> removedNodes) {
   		
   		if(removedNodes == null){
            throw new IllegalArgumentException("The map of removed nodes can't be null.");
        }
   		
        String nodeTypeStr = nodeJsonObj.getString(ConversationHelper.NODE_TYPE_KEY);
           
        TreeNodeEnum nodeType = TreeNodeEnum.fromName(nodeTypeStr);
           
        if(!TreeNodeEnum.ADD_NODE.equals(nodeType)){
        	
        	if(nodeJsonObj.has(ConversationHelper.CHILDREN_KEY)) {
        		
        		JSONArray childrenArray = (JSONArray) nodeJsonObj.get(ConversationHelper.CHILDREN_KEY);
        		
        		//iterate over the node's children backwards to find and remove display nodes
        		for(int i = childrenArray.length() - 1; i >= 0 ; i--){
                    
                    JSONObject childNode = (JSONObject) childrenArray.get(i);
                    
                    removeDisplayNodes(childNode, removedNodes);
                    
                    TreeNodeEnum childType = TreeNodeEnum.fromName(childNode.getString(ConversationHelper.NODE_TYPE_KEY));
                    
                    if(TreeNodeEnum.ADD_NODE.equals(childType)){
                    	
                    	//remove this display node
                    	int childId = childNode.getInt(ConversationHelper.NODE_ID_KEY);                  	
                    	
                    	List<Integer> replacementNodeIds = new ArrayList<Integer>();
                    	
                    	if(childNode.has(ConversationHelper.CHILDREN_KEY)) {
                    		
                    		//if a display node has children, replace it with its children (for add nodes, there should be only one child)
                    		JSONArray grandchildrenArray = (JSONArray) childNode.get(ConversationHelper.CHILDREN_KEY);
                    			
                			JSONObject grandchildNode = (JSONObject) grandchildrenArray.get(0);
                			
                			if(grandchildNode != null){
                				
	                			childrenArray.put(i, grandchildNode);
	                			
	                			int grandchildId = grandchildNode.getInt(ConversationHelper.NODE_ID_KEY);
	                			
	                			replacementNodeIds.add(grandchildId);
	                			
                			} else {
                				childrenArray.remove(i);
                			}
                			
                    	} else {
                    		
                    		childrenArray.remove(i);
                    	}
                    	
                    	removedNodes.put(childId, replacementNodeIds);
                    
                    }
                }
        	}
        }
   	}

	/**
     * Decode a JSON representation of a conversation tree node into a Conversation tree object.
     * 
     * @param nodeJsonObj contains a conversation tree node to decode from a json representation
     * @param linksMap contains a mapping of source conversation tree node ids to list of target conversation tree node ids.  This
     * represents the JSON encoding of the links attribute which are lines in the graph to nodes that are already defined in
     * the JSON encoded and shouldn't be encoded more than once.
     * @param conversation where decoded nodes will be added too (e.g. message nodes are added to the conversation tree's list
     * of messages) 
     * @return a new conversation tree object associated with the XML schema.
     */
    private static Serializable decodeNode(JSONObject nodeJsonObj, Map<Integer, List<Integer>> linksMap, generated.conversation.Conversation conversation){
        
        if(linksMap == null){
            throw new IllegalArgumentException("The links map can't be null.");
        }
        
        Serializable newNode = null;
        
        try{
            int nodeId = nodeJsonObj.getInt(ConversationHelper.NODE_ID_KEY);
            String nodeTypeStr = nodeJsonObj.getString(ConversationHelper.NODE_TYPE_KEY);
            
            TreeNodeEnum nodeType = TreeNodeEnum.fromName(nodeTypeStr);
            switch(nodeType){
            
            case MESSAGE_NODE:
                
                String messageText = (String) nodeJsonObj.get(ConversationHelper.NODE_NAME_KEY);
                generated.conversation.Message messageNode = new generated.conversation.Message();
                messageNode.setText(messageText);
                messageNode.setNodeId(BigInteger.valueOf(nodeId));
                
                //
                // build the descendants
                // [messages should only have 1 child]
                //
                boolean encodedMessageChild = false;
                if(nodeJsonObj.has(ConversationHelper.CHILDREN_KEY)) {
                    
                    JSONArray messageChildrenArray = (JSONArray) nodeJsonObj.get(ConversationHelper.CHILDREN_KEY);
                    
                    if(messageChildrenArray.length() == 1){
                        JSONObject messageChildObj = messageChildrenArray.getJSONObject(0);
                        Serializable messageChildNode = decodeNode(messageChildObj, linksMap, conversation);
                        
                        //link the child of this message node
                        messageNode.setChildNodeId(getNodeId(messageChildNode));
                        
                        encodedMessageChild = true;
                    }else if(messageChildrenArray.length() > 1){
                        //ERROR
                        throw new DetailedException("Failed to convert the JSON encoded convesation tree node with id "+nodeId+"." , 
                                "The message conversation tree node with id "+nodeId+" has "+messageChildrenArray.length()+" children when a max of 1 is allowed.", null);
                    }
                }
                
                //attempt to get the child from the defined links
                if(!encodedMessageChild && linksMap.containsKey(nodeId)){
                    
                    List<Integer> targetNodeIds = linksMap.get(nodeId);
                    if(targetNodeIds.size() == 1){
                        messageNode.setChildNodeId(BigInteger.valueOf(targetNodeIds.get(0)));
                    }else if(targetNodeIds.size() > 1){
                        //ERROR
                        throw new DetailedException("Failed to convert the JSON encoded convesation tree node with id "+nodeId+"." , 
                                "The message conversation tree node with id "+nodeId+" has "+targetNodeIds.size()+" children (defined in links JSON object) when a max of 1 is allowed.", null);
                    }
                }
                
                //add the node to the conversations message nodes
                conversation.getMessages().getMessage().add(messageNode);
                
                newNode = messageNode;
                break;
            case CHOICE_NODE:
                
                String choiceText = (String) nodeJsonObj.get(ConversationHelper.NODE_NAME_KEY);
                generated.conversation.Choice choiceNode = new generated.conversation.Choice();
                choiceNode.setText(choiceText);
                choiceNode.setNodeId(BigInteger.valueOf(nodeId));
                
                //
                // assessment table (optional)
                //
                if(nodeJsonObj.has(ConversationHelper.ASSESSMENTS_KEY)){
                    JSONArray choiceAssessmentsObj = (JSONArray) nodeJsonObj.get(ConversationHelper.ASSESSMENTS_KEY);
                    for(int index = 0; index < choiceAssessmentsObj.length(); index++){
                        
                        JSONObject assessmentObj = choiceAssessmentsObj.getJSONObject(index);
                        
                        generated.conversation.Assessment assessment = new generated.conversation.Assessment();
                        assessment.setConcept(assessmentObj.getString(ConversationHelper.CONCEPT_KEY));
                        assessment.setConfidence(BigDecimal.valueOf((Double.parseDouble((String)assessmentObj.get(ConversationHelper.CONFIDENCE_KEY)))));
                        assessment.setLevel((String) assessmentObj.get(ConversationHelper.ASSESSMENT_KEY));
                        
                        choiceNode.getAssessment().add(assessment);
                    }
                }
                
                //
                // build the descendants
                // [choices should only have 1 child]
                //
                boolean encodedChoiceChild = false;
                if(nodeJsonObj.has(ConversationHelper.CHILDREN_KEY)) {
                    JSONArray choiceChildrenArray = (JSONArray) nodeJsonObj.get(ConversationHelper.CHILDREN_KEY);
                    
                    if(choiceChildrenArray.length() == 1){
                        JSONObject choiceChildObj = choiceChildrenArray.getJSONObject(0);
                        Serializable choiceChildNode = decodeNode(choiceChildObj, linksMap, conversation);
                        
                        //link the child of this choice node
                        choiceNode.setChildNodeId(getNodeId(choiceChildNode));  
                        
                        encodedChoiceChild = true;
                    }else if(choiceChildrenArray.length() > 1){
                        //ERROR
                        throw new DetailedException("Failed to convert the JSON encoded convesation tree node with id "+nodeId+"." , 
                                "The choice conversation tree node with id "+nodeId+" has "+choiceChildrenArray.length()+" children when a max of 1 is allowed.", null);
                    }
                }
                
                //attempt to get the child from the defined links
                if(!encodedChoiceChild && linksMap.containsKey(nodeId)){
                    
                    List<Integer> targetNodeIds = linksMap.get(nodeId);
                    if(targetNodeIds.size() == 1){
                        choiceNode.setChildNodeId(BigInteger.valueOf(targetNodeIds.get(0)));
                    }else if(targetNodeIds.size() > 1){
                        //ERROR
                        throw new DetailedException("Failed to convert the JSON encoded convesation tree node with id "+nodeId+"." , 
                                "The choice conversation tree node with id "+nodeId+" has "+targetNodeIds.size()+" children (defined in links JSON object) when a max of 1 is allowed.", null);
                    }
                }
                
                //add the node to the conversations choice nodes
                conversation.getChoices().getChoice().add(choiceNode);
                
                newNode = choiceNode;
                break;
            case END_NODE:
                generated.conversation.End endNode = new generated.conversation.End();
                endNode.setNodeId(BigInteger.valueOf(nodeId));
                
                //
                // assessment table (optional)
                //
                if(nodeJsonObj.has(ConversationHelper.ASSESSMENTS_KEY)){
                    JSONArray endAssessmentsObj = (JSONArray) nodeJsonObj.get(ConversationHelper.ASSESSMENTS_KEY);
                    for(int index = 0; index < endAssessmentsObj.length(); index++){
                        
                        JSONObject assessmentObj = endAssessmentsObj.getJSONObject(index);
                        
                        generated.conversation.Assessment assessment = new generated.conversation.Assessment();
                        assessment.setConcept(assessmentObj.getString(ConversationHelper.CONCEPT_KEY));
                        assessment.setConfidence(BigDecimal.valueOf((Double.parseDouble((String)assessmentObj.get(ConversationHelper.CONFIDENCE_KEY)))));
                        assessment.setLevel((String) assessmentObj.get(ConversationHelper.ASSESSMENT_KEY));
                        
                        endNode.getAssessment().add(assessment);
                    }
                }
                
                //add the node to the conversations end nodes
                conversation.getEnds().getEnd().add(endNode);
                
                newNode = endNode;
                break;
            case QUESTION_NODE:
                
                String questionText = (String) nodeJsonObj.get(ConversationHelper.NODE_NAME_KEY);
                generated.conversation.Question questionNode = new generated.conversation.Question();
                questionNode.setText(questionText);
                questionNode.setNodeId(BigInteger.valueOf(nodeId));
                
                questionNode.setChoices(new generated.conversation.Question.Choices());
                
                //
                // build the descendants
                //
                if(nodeJsonObj.has(ConversationHelper.CHILDREN_KEY)) {
                    
                    JSONArray questionChildrenArray = (JSONArray) nodeJsonObj.get(ConversationHelper.CHILDREN_KEY);
                    for(int index = 0; index < questionChildrenArray.length(); index++){
                        
                        JSONObject questionChildObj = questionChildrenArray.getJSONObject(index);
                        Serializable questionChildNode = decodeNode(questionChildObj, linksMap, conversation);
                        
                        questionNode.getChoices().getChoiceId().add(getNodeId(questionChildNode));
                    }
                }
                
                //attempt to get additional children from the defined links
                if(linksMap.containsKey(nodeId)){
                    
                    List<Integer> targetNodeIds = linksMap.get(nodeId);
                    for(Integer targetNodeId : targetNodeIds){
                        questionNode.getChoices().getChoiceId().add(BigInteger.valueOf(targetNodeId));
                    }

                }
                
                //add the node to the conversations question nodes
                conversation.getQuestions().getQuestion().add(questionNode);
                
                newNode = questionNode;
                break;
            default:
                //ERROR
                break;
            }
            
        }catch(JSONException e){
            throw new DetailedException("Failed to decode a JSON representation of a conversation node into a conversation node object.", 
                    "A problem occurred while creating a conversation node object from a JSON node object.  The message reads:\n"+e.getMessage(), e);
        }
        
        return newNode;
    }
    
    /**
     * Return the unique conversation node id for the conversation tree object (e.g. Message) provided.
     * 
     * @param node the node to retrieve the node id from
     * @return the node id.
     */
    private static BigInteger getNodeId(Serializable node){
        
        if(node instanceof generated.conversation.Message){
            return ((generated.conversation.Message)node).getNodeId();
        }else if(node instanceof generated.conversation.Question){
            return ((generated.conversation.Question)node).getNodeId();
        }else if(node instanceof generated.conversation.Choice){
            return ((generated.conversation.Choice)node).getNodeId();
        }else if(node instanceof generated.conversation.End){
            return ((generated.conversation.End)node).getNodeId();
        }else{
            return null;
        }
    }
    
}
