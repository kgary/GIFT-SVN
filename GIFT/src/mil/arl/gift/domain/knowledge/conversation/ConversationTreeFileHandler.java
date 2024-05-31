/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import generated.conversation.Conversation;

import java.util.regex.Pattern;

import javax.xml.bind.UnmarshalException;

import org.xml.sax.SAXParseException;

import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;

/**
 * This class is responsible for parsing and validating a conversation file.
 * 
 * @author mhoffman
 *
 */
public class ConversationTreeFileHandler extends AbstractSchemaHandler {
    
    /** the conversation xml object parsed from the file */
    private generated.conversation.Conversation conversation;
    
    /**
     * syntax and pattern matching for conversation variables
     * e.g. "Hello my name is {{TUTOR_NAME}}." is changed to  "Hello my name is Steve."
     */
    public static final String VARIABLE_PREFIX = "{{";
    public static final String VARIABLE_SUFFIX = "}}";
    public static Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

    /**
     * Parse and validate the conversation file.
     * 
     * @param conversationFile contains the conversation elements to parse and validate.  Can't be null and must exist.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws DetailedException if there was a gift validation issue
     * @throws FileValidationException if there was an issue parsing the file or validating against the schema
     */
    public ConversationTreeFileHandler(FileProxy conversationFile, boolean failOnFirstSchemaError) throws DetailedException, FileValidationException{
        super(AbstractSchemaHandler.CONVERSATION_TREE_SCHEMA_FILE);
        
        try {
            UnmarshalledFile uFile = super.parseAndValidate(generated.conversation.Conversation.class, conversationFile.getInputStream(), failOnFirstSchemaError);
            conversation = (Conversation)uFile.getUnmarshalled();            
        }catch (DetailedException e){
            throw new FileValidationException( 
                    "Failed to parse and validate the conversation file '"+conversationFile.getFileId()+"' because "+e.getReason(), 
                    e.getDetails(),
                    conversationFile.getFileId(),
                    e);
            
        }catch(Throwable e){
            
            //
            // Attempt to translate some of the most common conversation tree schema violations into human readable error messages
            //
            if(e instanceof UnmarshalException){
                Throwable linkedException = ((UnmarshalException)e).getLinkedException();
                
                if(linkedException != null && linkedException instanceof SAXParseException){

                    SAXParseException saxException = (SAXParseException) linkedException;
                    if(saxException.getMessage() != null){
                        
                        if(saxException.getMessage().contains("The content of element 'choices' is not complete")){
                            
                            throw new FileValidationException( 
                                    "Failed to parse and validate the conversation file '"+conversationFile.getFileId()+"'.", 
                                    "There is at least one question node in the converstation that has less than two choices.  All question nodes must have two or more child choice nodes.",
                                    conversationFile.getFileId(),
                                    e);
                        }else if(saxException.getMessage().contains("The content of element 'message' is not complete")){
                         
                            throw new FileValidationException( 
                                    "Failed to parse and validate the conversation file '"+conversationFile.getFileId()+"'.", 
                                    "There is at least one message node in the converstation that has no child node.  All message nodes must have a child node (e.g. another message, question or end node).",
                                    conversationFile.getFileId(),
                                    e);
                        }
                    }
                }
            }
            
            //
            // default handling
            // 
            
            String details = "The most likely cause of this error is because the XML file content is not correctly formatted.  This could be anything from a missing XML tag"+
                    " needed to ensure the general XML structure was followed (i.e. all start and end tags are found), to a missing required field (e.g. course name is"+
                    " required) or the value for a field doesn't satisfy the schema requirements (i.e. the course name must be at least 1 character).\n\n"+
                    "Please take a look at the first part of stacktrace for a hint at the problem or ask for help on the GIFT <a href=\"https://gifttutoring.org/projects/gift/boards\" target=\"_blank\">forums</a>.\n\n"+
                    "<b>For Example: </b><div style=\"padding: 20px; border: 1px solid gray; background-color: #DDDDDD\">This example stacktrace snippet that indicates the course name ('#AnonType_nameCourse') value doesn't satisfy the minimum length requirement of 1 character:\n\n"+
                    "<i>javax.xml.bind.UnmarshalException - with linked exception: [org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 69; cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type '#AnonType_nameCourse'</i></div>";
            
            if(e.getMessage() != null){
                details += "\n\n<b>Your Validation error</b>: "+e.getMessage();
            }else if(e.getCause() != null && e.getCause().getMessage() != null){
                details += "\n\n<b>Your Validation error</b>: "+e.getCause().getMessage();
            }
            
            throw new FileValidationException(
                    "A problem occurred when parsing and validating the conversation file '"+conversationFile.getName()+"'.",
                    details, 
                    conversationFile.getFileId(), 
                    e);
        }
    }
    
    /**
     * Return the conversation root object.
     * 
     * @return will not be null
     */
    public generated.conversation.Conversation getConversation(){
        return conversation;
    }
    
    /**
     * Check the conversation against additional GIFT validation logic that goes beyond schema validation.
     * 
     * @throws IllegalArgumentException if there was a problem with the conversation argument provided
     * @throws DetailedException if there was a GIFT validation issue found
     */
    public GIFTValidationResults checkConversation(){
        return checkConversation(getConversation());
    }
    
    /**
     * Check the conversation against additional GIFT validation logic that goes beyond schema validation.
     * 
     * @param conversationToCheck the conversation element to check
     * @return validation results
     * @throws IllegalArgumentException if there was a problem with the conversation argument provided
     * @throws DetailedException if there was a GIFT validation issue found
     */
    public static GIFTValidationResults checkConversation(generated.conversation.Conversation conversationToCheck)
            throws IllegalArgumentException, DetailedException{
        
        if(conversationToCheck == null){
            throw new IllegalArgumentException("The conversation to check can't be null.");
        }

        //this will check for:
        // i. duplicate node ids
        // ii. at least 1 message or 1 question node
        // iii. every path from the start has an end
        return ConversationTreeModel.checkConversation(conversationToCheck, null);        

    }
}
