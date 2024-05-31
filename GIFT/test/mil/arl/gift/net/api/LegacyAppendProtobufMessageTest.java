/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import generated.proto.common.ProtobufLogMessageProto;
import generated.proto.common.ProtobufLogMessageProto.ProtobufLogMessage;
import generated.proto.common.ProtobufMessageProto.ProtobufMessage;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.common.logger.ProtobufMessageLogReader;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.codec.json.MessageJSONCodec;
import mil.arl.gift.net.api.message.codec.proto.ProtobufMessageProtoCodec;

/**
 * This class is responsible for retrieving messages from selected log, 
 * writing and keeping track of messages in the log file, 
 * and adding any messages to the protobuf binary file
 * 
 * @author kquiroga
 *
 */
@Ignore
public class LegacyAppendProtobufMessageTest {
    
    private static final Scanner input = new Scanner(System.in);
    
    private static final String LEGACY_DOMAIN_LOG_FILE = "data" + File.separator + "tests" + File.separator + 
            "Legacy_Domain_Session_Log" + ProtobufMessageLogReader.PROTOBUF_LOG_FILE_EXTENSION;
    private static final String LEGACY_SYSTEM_LOG_FILE = "data" + File.separator + "tests" + File.separator + 
            "Legacy_System_Message_Log" + ProtobufMessageLogReader.PROTOBUF_LOG_FILE_EXTENSION;
    
    private static final MessageLogReader legacyDomainReader = new ProtobufMessageLogReader();
    private static final MessageLogReader legacySystemReader = new ProtobufMessageLogReader();
    
    private static final MessageLogReader appendLogReader = new ProtobufMessageLogReader();
    
    private static final ProtobufMessageProtoCodec codec = new ProtobufMessageProtoCodec();
    
    private static Set<MessageTypeEnum> legacyMsgTypes = new HashSet<MessageTypeEnum>();
    private static Set<MessageTypeEnum> appendMsgTypes = new HashSet<MessageTypeEnum>();
    private static List<MessageTypeEnum> definedMessages = new ArrayList<MessageTypeEnum>();
    
    private static List<Message> domainMessageList = new ArrayList<>(); 
    private static List<Message> systemMessageList = new ArrayList<>();
    
    
    private static int domainOrSystemLog;
    
    private static Map<Integer, MessageTypeEnum> choiceToMessage = new HashMap<>();
    
    /**
     * This method displays the currently supported message types by GIFT.
     */
    public void displayMessageTypes() {
        System.out.println("List of currently supported message types by GIFT:");
        for (int i = 0; i < definedMessages.size(); i++) {            
            System.out.println((i + 1) + " - " + definedMessages.get(i).getDisplayName());
        }
    }

    /**
     * This method gets user decision to retrieve messages from domain or system log and writes messages to log file
     * @param message
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public void addMessage(Message message) throws IllegalArgumentException, Exception {
        if (message == null) {
            return;
        }
        /* Asks user which log they wish to add to */
        System.out.println("Which file do you wish to output to:");
        System.out.println("0 - Domain Log");
        System.out.println("1 - System Message Log");
        System.out.print("Select a file(0 or 1): ");
        domainOrSystemLog = input.nextInt();
        OutputStream os;
        
        /* Gets messages from log file selected */
        if(domainOrSystemLog == 1) {
            os = new FileOutputStream(new File(LEGACY_SYSTEM_LOG_FILE));
         } else {
            os = new FileOutputStream(new File(LEGACY_DOMAIN_LOG_FILE));
         }
          
        ProtobufLogMessageProto.ProtobufLogMessage.Builder protobufBuilder = ProtobufLogMessageProto.ProtobufLogMessage.newBuilder();
        
        /* writes messages to log file selected */
        for(Message data : (domainOrSystemLog == 1) ? systemMessageList : domainMessageList) {
            ProtobufMessage protoMsg = codec.map(data);
            protobufBuilder.setMessage(protoMsg);
            ProtobufLogMessage logMessageFinal = protobufBuilder.build();
            logMessageFinal.writeDelimitedTo(os);
        }
        
        ProtobufLogMessage.Builder appendMessage = ProtobufLogMessage.newBuilder();  
        appendMessage.setMessage(codec.map(message));
        appendMessage.build().writeDelimitedTo(os);
        os.close();
        
        /* Used to update the message count after adding the message. Note that this line doesn't actually add 
         * the message to the domain or system log but just maintains the list of messages currently written in 
         * the session. 
         */
        if (domainOrSystemLog == 1) {
            systemMessageList.add(message);
        } else {
            domainMessageList.add(message);
        }
    } 
   
    /**
     * This method parses list of messages and keeps track of all parsed messages
     * 
     */
    @Before
    public void init() {
        try {
            /* Parses log and adds them to the list*/
            legacyDomainReader.parseLog(new FileProxy(new File(LEGACY_DOMAIN_LOG_FILE))); 
            legacyMsgTypes.addAll(legacyDomainReader.getTypesOfMessages());
            domainMessageList.addAll(legacyDomainReader.getMessages());
            legacySystemReader.parseLog(new FileProxy(new File(LEGACY_SYSTEM_LOG_FILE))); 
            legacyMsgTypes.addAll(legacySystemReader.getTypesOfMessages());
            systemMessageList.addAll(legacySystemReader.getMessages());
            
            definedMessages.addAll(MessageTypeEnum.VALUES());
            
            for (int i = 0; i < definedMessages.size(); i++) {
                choiceToMessage.put(i + 1, definedMessages.get(i));
            }
         } catch (Exception e) {
             System.err.println(e.getMessage());
         }
    }
 
    /**
     * This method reads inputed protobuf binary file and adds a selected message type to the legacy protobuf binary log.
     * @throws IllegalArgumentException
     * @throws Exception
     */
    @Test
    public void testLegacyAppendProtobufMessage() throws IllegalArgumentException, Exception {    
        /* Reads in the path to the protobuf binary log file from the user. */
        System.out.print("Please provide an absolute path to the protobuf binary file: ");
        String path = input.nextLine();
        
        /* Parses protobuf binary file and retrieves the GIFT message objects */
        try {
           appendLogReader.parseLog(new FileProxy(new File(path))); 
           appendMsgTypes.addAll(appendLogReader.getTypesOfMessages());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        /* Outputs the current, supported message types by GIFT */
        displayMessageTypes();
        
        /* Until the user exits by typing '0', the user may choose a message type to add to the legacy log file */
        while (true) {
            System.out.print("\nChoose message type to add to legacy protobuf binary log (0 to exit): ");
            int choice = input.nextInt();
            
            if (choice == 0) {
                break;
            }
            
            /* Check whether message type selected has been found within log file or not */
            if(CollectionUtils.isNotEmpty(appendLogReader.getMessagesByType(choiceToMessage.get(choice)))) {
                
                /* Print all options of that selected message type that may be added */
                for(int i = 0; i < appendLogReader.getMessagesByType(choiceToMessage.get(choice)).size(); i++) {
                    JSONObject jsonMsg = new JSONObject();
                    MessageJSONCodec.encode(jsonMsg, appendLogReader.getMessagesByType(choiceToMessage.get(choice)).get(i));
                    System.out.println((i+1) + " - " + jsonMsg);
                }
                
                /* Get users input for which message type to add to legacy log */
                System.out.print("\nChoose message type to add to legacy protobuf binary log: ");
                int selection = input.nextInt();
                
                /* Assure selection is valid and user wants to add this message */
                if(selection > 0 && selection < appendLogReader.getMessagesByType(choiceToMessage.get(choice)).size()) {
                    System.out.print("Are you sure you want to add this message type? (Y or N): ");
                    char option = input.next().charAt(0);
                    
                    /* If user is sure add message to legacy protobuf binary log and print completed statement */
                    if(option == 'Y' || option == 'y') {
                        addMessage(appendLogReader.getMessagesByType(choiceToMessage.get(choice)).get(selection));
                        if(domainOrSystemLog == 0) {
                            System.out.print("Finished writing " + appendLogReader.getMessagesByType(choiceToMessage.get(choice)).get(selection) + " to Domain protobuf log.");
                            System.out.println(" There are now " + domainMessageList.size() + " messages inside the log.");
                        }
                        else {
                            System.out.print("Finished writing " + appendLogReader.getMessagesByType(choiceToMessage.get(choice)).get(selection) + " to System protobuf log.");
                            System.out.println(" There are now " + systemMessageList.size() + " messages inside the log.");
                        }
                    }
                    else {
                        System.out.println("Message was not added. You may now select a new message to add.");
                    }
                }
            }
            else
                System.out.println("Type not found. Select another message type.");
            
        }
    }
}
