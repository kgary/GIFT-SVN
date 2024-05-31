/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.sesandboxplugin;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Timestamp;

import cta.tmt.protobuf.EntityEvents;
import cta.tmt.protobuf.EntityFunctions;
import cta.tmt.protobuf.EntityServiceGrpc;
import cta.tmt.protobuf.EquipmentFunctions;
import cta.tmt.protobuf.EquipmentServiceGrpc;
import cta.tmt.protobuf.ScenarioFunctions;
import cta.tmt.protobuf.ScenarioServiceGrpc;
import cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse;
import cta.tmt.protobuf.EntityOuterClass;
import cta.tmt.protobuf.EntityOuterClass.CommunicationType;
import cta.tmt.protobuf.EntityOuterClass.Entity;
import cta.tmt.protobuf.EntityOuterClass.ReceiverType;
import cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse;
import cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse;
import cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse;
import cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse;
import cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VariableInfo;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.ta.state.BurstDescriptor;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityAppearance;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityMarking;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.ta.state.EventIdentifier;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.common.ta.state.VariablesState;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.WorldStateManager;
import mil.arl.gift.gateway.WorldStateManager.EntityTypeAndForceId;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.socket.ApplicationByteListener;
import mil.arl.gift.net.socket.SocketHandler;

/**
 * This is the Synthetic Environment Sandbox interop interface responsible for communicating with SE Sandbox
 * 
 * NOTE: Code references to this plugin and related values mostly use the name "HAVEN" instead of "SE Sandbox".
 * In September 2021, The name of this application was changed to "SE Sandbox" instead of "HAVEN". 
 * In October 2021, GIFT was updated so that its UI and branding would refer to SE Sandbox instead of
 * HAVEN in user-facing display text.
 * However, the older name of HAVEN is still generally used in places such as code and config values, 
 * where it is not being used to display information to users.
 * 
 * @author mcambata
 */
public class SESandboxPluginInterface extends AbstractInteropInterface {
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SESandboxPluginInterface.class);
    
    /** The number of bytes used in HAVEN's "message start" signature */
    public static final int MESSAGE_START_BYTE_COUNT = 4;
    
    /** 
     * The characters used in HAVEN's "message start" signature 
     * The value is defined by HAVEN, but set here so it can be searched for and read. */
    public static final Byte[] MESSAGE_START_BYTE_SIGNATURE = new Byte[]{77, 65, 68, 65};
    
    /** The number of bytes used to determine the event type of an incoming HAVEN proto message. */
    public static final int EVENT_TYPE_BYTE_COUNT = 2;
    
    /** The number of bytes used to determine the size of an incoming HAVEN proto message. */
    public static final int MESSAGE_SIZE_VALUE_BYTE_COUNT = 4;
    
    /** The maximum size that an incoming HAVEN message should have. */
    public static final int MAX_HAVEN_MESSAGE_SIZE = 1024;
    
    /** default seconds the feedback will stay visible in the Haven UI */
    private static final float DEFAULT_FEEDBACK_DURATION = (float) 2.0;
    
    /** incoming Haven health value upper threshold used to set GIFT damage value to {@link #DamageEnum.SLIGHT_DAMAGE} */
    private static final float SLIGHT_DAMAGE_THRESHOLD = (float) 0.75;
    
    /** incoming Haven health value upper threshold used to set GIFT damage value to {@link #DamageEnum.MODERATE_DAMAGE} */
    private static final float MODERATE_DAMAGE_THRESHOLD = (float) 0.5;
    
    /** incoming Haven health value upper threshold used to set GIFT damage value to {@link #DamageEnum.DESTROYED_DAMAGE} */
    private static final float DESTROYED_DAMAGE_THRESHOLD = (float) 0.01;
    
    /** The script command string to set whether or not a weapon is disabled. Used by the handleScript method. */
    private static final String DISABLE_WEAPON_CONTROL_SCRIPT_NAME = "disableWeaponControl";
    
    /** 
     * The number of bytes that can be held in the byteInputQueue at once.
     * NOTE: This is also the max size of a proto message from HAVEN that can be processed.
     */
    private static final int BYTE_INPUT_QUEUE_SIZE = 4096;
    
    /** Default hour of dusk used for sending time of day to HAVEN */
    private static final int DEFAULT_DUSK = 18;
    /** Default hour of dawn used for sending time of day to HAVEN */
    private static final int DEFAULT_DAWN = 8;
    /** Default hour of midday used for sending time of day to HAVEN */
    private static final int DEFAULT_MIDDAY = 12;
    /** Default hour of midnight used for sending time of day to HAVEN */
    private static final int DEFAULT_MIDNIGHT = 0;
    /** Number of hours in a day, used for calculating time difference */
    private static final int HOURS_IN_DAY = 24;
    /** Number of milliseconds in an hour, used for calculating time difference */
    private static final int MILLISECONDS_IN_HOUR = 3600000;
    /** Number of milliseconds in a second, used for calculating time difference */
    private static final int MILLISECONDS_IN_SECOND = 1000;
    
    /** The current domain session ID */
    private Integer domainSessionId;
            
    /** Used for connecting to HAVEN with GRPC */
    private ManagedChannel grpcChannel;
    
    /** 
     * Async stub for GRPC communication regarding HAVEN entities.
     * For example: can be used to get an entity type and force ID from HAVEN entities, or can set the location of a HAVEN entity.
     **/
    private EntityServiceGrpc.EntityServiceStub entityServiceStub;
    
    /**
     * Async stub for GRPC communication regarding HAVEN scenario.
     * For example: can be used to send feedback messages to HAVEN application from GIFT.
     */
    private ScenarioServiceGrpc.ScenarioServiceStub scenarioServiceStub;
    
    /**
     * Async stub for GRPC communication regarding equipment in HAVEN.
     * For example: can be used to cause a weapon malfunction or to repair the functionality of a weapon in HAVEN.
     */
    private EquipmentServiceGrpc.EquipmentServiceStub equipmentServiceStub;
    
    /** The byte input from the HAVEN application */
    BlockingQueue<Byte> byteInputQueue = new ArrayBlockingQueue<Byte>(BYTE_INPUT_QUEUE_SIZE);
    
    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;    
    static{
        supportedMsgTypes = new ArrayList<>();
        
        // Even though this class doesn't handle SIMAN GIFT messages yet, 
        // this is needed in order to run without a 'There are currently no enabled Gateway 
        // module interop plugins that can handle the message type Siman' course ending error
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);  
        
        supportedMsgTypes.add(MessageTypeEnum.ENVIRONMENT_CONTROL);
        supportedMsgTypes.add(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST);
        supportedMsgTypes.add(MessageTypeEnum.LESSON_STARTED);
        supportedMsgTypes.add(MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS);
        supportedMsgTypes.add(MessageTypeEnum.INIT_INTEROP_CONNECTIONS);
        supportedMsgTypes.add(MessageTypeEnum.VARIABLE_STATE_REQUEST);
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. HAVEN). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.ENTITY_STATE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.WEAPON_FIRE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.DETONATION);
    }
    
    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.HAVEN); 
    }
    
    /** the socket connection */
    private SocketHandler socketHandler = null;
    
    /** configuration parameters for the connection to the GIFT HAVEN plugin */
    private generated.gateway.SESandbox havenPluginConfig = null;    
    
    /**
     * Class constructor
     * 
     * @param name - the display name of the plugin
     */
    public SESandboxPluginInterface(String name) {
        super(name, true);
        
        domainSessionId = null;    // A placeholder value. This will be replaced by an actual domainSessionId when a message is received.
        
        entityServiceStub = null;
        scenarioServiceStub = null;
        equipmentServiceStub = null;
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        
        if (config instanceof generated.gateway.SESandbox) {
                        
            this.havenPluginConfig = (generated.gateway.SESandbox) config;
            createSocketHandler();
            
            connectGrpcChannel();
            
            entityServiceStub = EntityServiceGrpc.newStub(grpcChannel);
            scenarioServiceStub = ScenarioServiceGrpc.newStub(grpcChannel);
            equipmentServiceStub = EquipmentServiceGrpc.newStub(grpcChannel);
            
            if(logger.isInfoEnabled()){
                logger.info("Plugin has been configured");
            }

        } else {
            throw new ConfigurationException("SE Sandbox Plugin interface can't configure.",
                    "The SE Sandbox Plugin interface only uses the interop config type of "+generated.gateway.SESandbox.class+" and doesn't support using the interop config instance of " + config,
                    null);
        }
        
        return false;
    }
    
    /**
     * Determines if this session has been given a domain session ID value corresponding to a currently running HAVEN user session.
     * 
     * @return whether or not this session has been given a domain session ID value corresponding to a currently running HAVEN user session..
     */
    private boolean getIsWorldStateManagerTrackingThisSession() {
        return domainSessionId != null;
    }
    
    /**
     * Creates the socket connection if there are configuration parameters and the socket handler in this class is null.
     */
    private void createSocketHandler(){
        
        if(havenPluginConfig != null){
            
            if(socketHandler == null || !socketHandler.isConnected()){
                
                ApplicationByteListener socketListener = new ApplicationByteListener() {
                    
                    @Override
                    public void handleApplicationByteInput(byte[] byteInput) {
                        onSocketReceivedData(byteInput);
                    }
                };
                
                socketHandler = new SocketHandler(havenPluginConfig.getNetworkAddress(), havenPluginConfig.getNetworkPort(), socketListener, MAX_HAVEN_MESSAGE_SIZE);
                
                if(logger.isInfoEnabled()){
                    logger.info("SE Sandbox plugin interface socket handler created.");
                }
            }
        }
    }
    
    /**
     * Connects the channel used for GRPC communication with HAVEN
     */
    private void connectGrpcChannel() {
        
        if (havenPluginConfig != null) {
            
            String target = havenPluginConfig.getNetworkAddress().toString() + ":" + havenPluginConfig.getGrpcNetworkPort();
            grpcChannel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
        }
    }
    
    /**
     * Shuts down the channel used for GRPC connection with HAVEN
     */
    private void disconnectGrpcChannel() {
        grpcChannel.shutdownNow();
    }
    
    /**
     * Called when byte data is sent to the HAVEN plugin interface. Adds the data to a queue so it can later be read and processed.
     * 
     * @param byteInput Incoming data from HAVEN, starting in the form of a byte array. This will be converted to a readable format.
     */
    protected synchronized void onSocketReceivedData(byte[] byteInput) {
       int queueSize;
        try {
            
            byte[] truncatedByteArray = clearTrailingZeros(byteInput);
                        
            for (byte byteToAdd : truncatedByteArray) {
                byteInputQueue.offer(byteToAdd, 1, TimeUnit.SECONDS);
            }
            
            /* After the new input is added to the byte queue, remove anything before the next message start signature. */
            
            int indexOfNextMessageStart = getIndexOfNextMessageStartInByteQueue();
            
            if (indexOfNextMessageStart == -1) {
                /* 
                 * -1 is a special case meaning no "message start" signature can be found.
                 * As long as the buffer is not full, just return and wait for more data.
                 * If the buffer is full, then remove part of the buffer and return.
                 */
                
                if (byteInputQueue.size() >= BYTE_INPUT_QUEUE_SIZE) {
                    for (int i=0; i < (BYTE_INPUT_QUEUE_SIZE / 2); i++) {
                        byteInputQueue.poll();
                    }
                }
                
                return;
            } else if (indexOfNextMessageStart > 0) {
                
                /*
                 * If indexOfNextMessageStart > 0, then remove all bytes before the beginning of the next message.
                 * This should only happen if there is unusable data, so it should be removed.
                 */                
                for (int i=0; i < indexOfNextMessageStart; i++) {
                    byteInputQueue.poll();
                }
            }
            queueSize = byteInputQueue.size();
            while (queueSize > 0 && !isByteQueueLessThanMessageSize()) {
                parseProtoMessageFromByteQueue();
            }
            
            int sizeValue = getHavenMessageSize();
            
            if (sizeValue > MAX_HAVEN_MESSAGE_SIZE) {
                logger.error("SE Sandbox protobuf message has an invalid size of " + sizeValue + ". This likely indicates an error in data transfer. Message will be skipped.");
                
                /* Remove the current "begin message" signature, so that the next time a message is parsed, it will seek to the next message. */
                for (int i=0; i < MESSAGE_START_BYTE_COUNT; i++) {
                    byteInputQueue.poll();
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing data received from SE Sandbox", e);
        }
    }
    
    /**
     * Finds the beginning of the next incoming message from HAVEN
     * 
     * @return the index in the byte queue of the incoming message's first byte (starting with the "start of message" signature).
     *          If no message start signature is found in the byte input queue, then -1 is returned.
     */
    private synchronized int getIndexOfNextMessageStartInByteQueue() {
        try {
            
            /* Find the first instance of the "message start signature" in the byte queue */
            List<Byte> byteInputSearchList = new ArrayList<>(Arrays.asList(byteInputQueue.toArray(new Byte[0])));
            
            boolean isSignatureFound = false;
            
            /* As the search list is modified, this tracks its offset from the actual byte input queue. */
            int indexToCheckOffset = 0;
            
            while (!isSignatureFound) {
                int indexToCheck = byteInputSearchList.indexOf(MESSAGE_START_BYTE_SIGNATURE[0]);
                if (indexToCheck == -1) {
                    return -1;
                } else if (byteInputSearchList.size() < indexToCheck + (MESSAGE_START_BYTE_SIGNATURE.length - 1)) {
                    return -1;
                } else {
                    
                    /* Check whether all of the bytes of the "message start signature" follow the indexToCheck. */
                    isSignatureFound = true;
                    for (int i=0; i < MESSAGE_START_BYTE_SIGNATURE.length; i++) {
                        if (byteInputSearchList.get(indexToCheck + i) != MESSAGE_START_BYTE_SIGNATURE[i]) {
                            isSignatureFound = false;
                            break;
                        }
                    }
                }
                
                if (isSignatureFound) {
                    return indexToCheck + indexToCheckOffset;
                } else {
                    /* Remove bytes up to the indexToCheck from the search list, so that the same section doesn't get searched over and over. */
                    for (int i=0; i <= indexToCheck; i++) {
                        byteInputSearchList.remove(0);
                        indexToCheckOffset++;
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("An exception occurred while searching for the start of a message from SE Sandbox", e);
        }
        
        /* If no value is returned by this point, return -1. */
        return -1;
    }
    
    /**
     * Reads from the byte queue to find the next protobuf message, parses that message, and performs the appropriate behavior.
     */
    private synchronized void parseProtoMessageFromByteQueue() {
        try {
            
            if (!isEnabled() || byteInputQueue == null) {
                return;
            }
            
            for (int i=0; i < MESSAGE_START_BYTE_COUNT; i++) {
                byteInputQueue.poll();
            }
                
            byte[] entityTypeBytes = new byte[EVENT_TYPE_BYTE_COUNT];
            
            for (int i=0; i < entityTypeBytes.length; i++) {
                entityTypeBytes[i] = byteInputQueue.remove();
            }
            
            EntityEvents.EventType stateEventType = getEventTypeFromBytes(entityTypeBytes);
            
            byte[] protobufSizeBytes = new byte[MESSAGE_SIZE_VALUE_BYTE_COUNT];
            
            for (int i=0; i < protobufSizeBytes.length; i++) {
                protobufSizeBytes[i] = byteInputQueue.remove();
            }
            
            int sizeValue = getProtobufSizeFromBytes(protobufSizeBytes);
            
            if (sizeValue > MAX_HAVEN_MESSAGE_SIZE) {
                throw new Exception("SE Sandbox protobuf message has an invalid size of " + sizeValue + ". This likely indicates an error in data transfer. Message will be skipped.");
            }
            
            byte[] protoMessageBytes = new byte[sizeValue];
            
            for (int i=0; i < protoMessageBytes.length; i++) {
                protoMessageBytes[i] = byteInputQueue.remove();
            }
                        
            try {
                switch (stateEventType)
                {
                    case EnterArea:
                        EntityEvents.EnterAreaMessage enterAreaMsg = EntityEvents.EnterAreaMessage.parseFrom(protoMessageBytes);
                        handleEnterAreaProtoMessage(enterAreaMsg);
                        break;
                    case SimulationStateUpdate:
                        EntityEvents.SimulationStateUpdateMessage simulationStateUpdateMsg = EntityEvents.SimulationStateUpdateMessage.parseFrom(protoMessageBytes);
                        handleSimulationStateUpdateProtoMessage(simulationStateUpdateMsg);
                        break;
                    case EntityUpdate:
                        EntityEvents.EntityUpdateMessage entityUpdateMsg = EntityEvents.EntityUpdateMessage.parseFrom(protoMessageBytes);
                        handleEntityUpdateProtoMessage(entityUpdateMsg);
                        break;
                    case EntityUpdates:
                        EntityEvents.EntityUpdatesMessage entityUpdatesMsg = EntityEvents.EntityUpdatesMessage.parseFrom(protoMessageBytes);
                        handleEntityUpdatesProtoMessage(entityUpdatesMsg);
                        break;
                    case EntityFireWeapon:
                        EntityEvents.EntityFireWeaponMessage entityFireWeaponMsg = EntityEvents.EntityFireWeaponMessage.parseFrom(protoMessageBytes);
                        handleEntityFireWeaponProtoMessage(entityFireWeaponMsg);
                        break;
                    default:
                        logger.info("received unhandled SE Sandbox state event type of "+stateEventType+" of size "+sizeValue);
                        break;
                }
            } catch (Exception e) {
                logger.error("Error parsing a SE Sandbox protobuf message of type "+stateEventType+".", e);
            }
                   
        } catch (Exception e) {
            logger.error("Error parsing a SE Sandbox protobuf message", e);
        }
    }
    
    /**
     * Removes trailing zeros at the end of a byte array.
     * This is performed because, when reading incoming bytes from HAVEN, there may be 0-bytes after some messages.
     * If extra 0-bytes were read as part of an incoming protobuf message, it could cause errors, so they are checked for and removed.
     * 
     * @param byteInput the byte array
     * @return the modified array
     */
    private byte[] clearTrailingZeros(byte[] byteInput) {
        
        for (int i=(byteInput.length - 1); i >= 0; i--) {
            if (byteInput[i] != 0) {
                
                byte[] arrayToReturn = Arrays.copyOfRange(byteInput, 0, i + 1);
                return arrayToReturn;
            }
        }
        
        return byteInput;
    }
    
    /**
     * Reads the byte queue to determine whether or not at least one whole message is present in it.
     * This can be used to avoid attempting to read a partial message.
     * 
     * @return true if the byte queue is less than the message size, and false otherwise.
     */
    private synchronized boolean isByteQueueLessThanMessageSize() {
                
        if (byteInputQueue.size() < (EVENT_TYPE_BYTE_COUNT + MESSAGE_SIZE_VALUE_BYTE_COUNT + MESSAGE_START_BYTE_COUNT)) {
            return true;
        }
        
        // Get the bytes that make up the protobuf message's size.
        int messageSize = getHavenMessageSize();
        
        if (byteInputQueue.size() < (messageSize + EVENT_TYPE_BYTE_COUNT + MESSAGE_SIZE_VALUE_BYTE_COUNT)) {
            return true;
        } else
        {
            return false;
        }
    }
    
    /**
     * Get the size of the current HAVEN message's protobuf bytes. 
     * Requires that the byte input queue start at the beginning of a HAVEN message to return correct information.
     * If this is not the case, then returns -1.
     * 
     * @return the size of the current HAVEN message's protobuf bytes. Or -1 if the input queue is not at the beginning of a HAVEN message.
     */
    private synchronized int getHavenMessageSize() {
        try {
            if (getIndexOfNextMessageStartInByteQueue() != 0 || 
                    byteInputQueue.size() < (MESSAGE_SIZE_VALUE_BYTE_COUNT + EVENT_TYPE_BYTE_COUNT + MESSAGE_START_BYTE_COUNT)) {
                return -1;
            } else {
            
                Byte[] queueArray = byteInputQueue.toArray(new Byte[0]);
                
                byte[] messageSizeArray = new byte[MESSAGE_SIZE_VALUE_BYTE_COUNT];
                
                int messageSizeArrayOffset = EVENT_TYPE_BYTE_COUNT + MESSAGE_START_BYTE_COUNT;
                for (int i=messageSizeArrayOffset; i < MESSAGE_SIZE_VALUE_BYTE_COUNT + messageSizeArrayOffset; i++) {
                    messageSizeArray[i - messageSizeArrayOffset] = queueArray[i];
                }
                
                int messageSize = getProtobufSizeFromBytes(messageSizeArray);
                
                return messageSize;
            }
        } catch (Exception e) {
            logger.error("Error getting size of an incoming SE Sandbox message", e);
            
            return -1;
        }
    }
    
    /**
     * Requests an entity type and force ID from HAVEN via GRPC.
     * If a response is received, that data is sent to the world state manager.
     * If a user session ID has not been defined for the SESandboxPluginInterface yet, this does nothing.
     * 
     * The response is received and then processed asynchronously. 
     * This does not prevent the current thread from continuing
     * while it waits for a response from HAVEN.
     * 
     * @param simId The int simId used to identify the entity whose data is being requested from Haven.
     */
    private void requestEntityType(int simId) {
        
        if (getIsWorldStateManagerTrackingThisSession()) {
            try {
                EntityFunctions.GetDisTypeRequest request = EntityFunctions.GetDisTypeRequest.newBuilder()
                        .setEntity(simId)
                        .build();
                
                entityServiceStub.getDisType(request, new StreamObserver<EntityFunctions.GetDisTypeResponse>() {
                    
                    @Override
                    public void onNext(GetDisTypeResponse response) {
                        EntityType newEntityType = new EntityType(response.getDis().getKind(), 
                                response.getDis().getDomain(), 
                                response.getDis().getCountry(), 
                                response.getDis().getCategory(), 
                                response.getDis().getSubcategory(), 
                                response.getDis().getSpecific(), 
                                response.getDis().getExtra());
                        int newForceId = response.getForceValue();
                                                
                        try {
                            WorldStateManager.getInstance().setEntityTypeAndForceId(domainSessionId, simId, newEntityType, newForceId);
                        } catch (Exception e) {
                            logger.error("Error setting DIS type and force ID for entity ID: " + simId + ".", e);
                        }
                    }
                    
                    @Override
                    public void onError(Throwable e) {
                        logger.error("Error getting DIS type from SE Sandbox for entity ID " + simId + ": " + e.toString(), e);
                        
                    }
                    
                    @Override
                    public void onCompleted() {
                        if(logger.isInfoEnabled()){
                            logger.info("Completed getting DIS type from SE Sandbox.");
                        }
                    }
                });
                                      
            } catch (Throwable e) {
                logger.error("Failed to get a DIS type from SE Sandbox for entity ID " + simId, e);
                
            }
        }
    }
    
    /**
     * Requests weapon safety data from SE Sandbox via gRPC.
     * First requests the primary weapon from the specified entity, then requests the weapon safety from that weapon.
     * If a response with weapon safety data is received, that data is sent as a VARIABLE_STATE_RESULT message for use by conditions.
     * 
     * The response is received and then processed asynchronously. 
     * This does not prevent the current thread from continuing
     * while it waits for a response from SE Sandbox.
     * 
     * @param entityMarking The int simId used to identify the entity whose data is being requested from SE Sandbox. Cannot be empty or null.
     * @param variablesStateRequestMsg the original request message to respond to with the
     *        variables state results. Cannot be null.
     */
    private void requestWeaponSafetyState(String entityMarking, DomainSessionMessage variablesStateRequestMsg) {
        try {
            if (StringUtils.isBlank(entityMarking)) {
                throw new IllegalArgumentException("The parameter 'entityMarking' cannot be blank.");
            }
            if (variablesStateRequestMsg == null) {
                throw new IllegalArgumentException("The parameter 'variablesStateRequestMsg' cannot be blank.");
            }
            
            int simId = Integer.parseInt(entityMarking);
                        
            EquipmentFunctions.GetPrimaryWeaponRequest weaponRequest = EquipmentFunctions.GetPrimaryWeaponRequest.newBuilder()
                    .setOwner(simId)
                    .build();
            
            SESandboxPluginInterface seSandboxPlugin = this;
                    
            equipmentServiceStub.getPrimaryWeapon(weaponRequest, new StreamObserver<EquipmentFunctions.GetPrimaryWeaponResponse>() {
                
                @Override
                public void onNext(GetPrimaryWeaponResponse weaponResponse) {
                    if(logger.isInfoEnabled()){
                        logger.info("Received weapon ID " + weaponResponse.getWeapon() + " for entity ID: " + simId);
                    }
                    
                    try {
                        int weaponId = weaponResponse.getWeapon();
                        EquipmentFunctions.IsWeaponSaftetyOnRequest request = EquipmentFunctions.IsWeaponSaftetyOnRequest.newBuilder()
                                .setWeapon(weaponId)
                                .build();
                        
                        equipmentServiceStub.isWeaponSafetyOn(request, new StreamObserver<EquipmentFunctions.IsWeaponSafetyOnResponse>() {
                            
                            @Override
                            public void onNext(IsWeaponSafetyOnResponse weaponSafetyResponse) {
                                WeaponState newWeaponState = new WeaponState(entityMarking);
                                newWeaponState.setHasWeapon(true);
                                newWeaponState.setWeaponAim(null);
                                newWeaponState.setWeaponSafetyStatus(weaponSafetyResponse.getIsOn());
                                
                                VariablesState variablesState = new VariablesState();
                                VariablesStateRequest request = (VariablesStateRequest) variablesStateRequestMsg.getPayload();
                                
                                VariablesStateResult result = new VariablesStateResult(request.getRequestId(), variablesState);
                                
                                Map<String, VariableState> wStateVarMap = variablesState.getVariableMapForType(VARIABLE_TYPE.WEAPON_STATE);
                                wStateVarMap.put(entityMarking, newWeaponState);

                                GatewayModule.getInstance().sendMessageToGIFT(variablesStateRequestMsg.getUserSession(), variablesStateRequestMsg.getDomainSessionId(), 
                                        variablesStateRequestMsg.getExperimentId(), result, MessageTypeEnum.VARIABLE_STATE_RESULT, seSandboxPlugin);
                            }
                            
                            @Override
                            public void onError(Throwable e) {
                                logger.error("Error getting weapon safety data from SE Sandbox for weapon ID " + weaponId + ": " + e.toString(), e);
                                
                            }
                            
                            @Override
                            public void onCompleted() {
                                if(logger.isInfoEnabled()){
                                    logger.info("Completed getting weapon safety data from SE Sandbox.");
                                }
                                
                            }
                        });                                  
                    } catch (Throwable e) {
                        logger.error("Failed to get weapon safety data from SE Sandbox for weapon ID " + weaponResponse.getWeapon(), e);
                        
                    }
                }
                
                @Override
                public void onError(Throwable t) {
                    logger.error("Error getting a primary weapon when requesting weapon safety data from SE Sandbox", t);
                    
                }
                
                @Override
                public void onCompleted() {
                    if(logger.isInfoEnabled()){
                        logger.info("Getting primary weapon completed for entity ID: " + simId);
                    }
                    
                }
            });
        } catch (Exception e) {
            logger.error("Failed to get a primary weapon ID when requesting weapon safety data from SE Sandbox", e);
        }
        
    }
    
    /**
     * Creates an EntityState based on data gathered from a HAVEN protobuf message.
     * 
     * HAVEN does not sent entity updates with the same data as the EntityState message used by GIFT, so
     * this method collects the entity's EntityTransform and Health values, and uses those to determine
     * the correct data to create an EntityState message.
     * 
     * NOTE: If an entity's EntityType and force ID are not yet tracked in the WorldStateManager, then
     * this method will asynchronously request that data from HAVEN, and send the EntityState message
     * with an unknown EntityType and force ID. Once the data is received, the WorldStateManager will
     * associate it with the entity's simulation ID from HAVEN, and subsequent EntityState messages
     * will contain the correct values for that data.
     * 
     * @param transform the EntityTransform
     * @param health the Health object
     * @return The resulting EntityState.
     */
    private EntityState getEntityStateFromHavenEntity(EntityOuterClass.EntityTransform transform, EntityOuterClass.Health health) {

        /* Entity transform variables */
        EntityOuterClass.Entity entity = transform.getEntity();
        EntityOuterClass.Vector3 position = transform.getPosition();
        EntityOuterClass.Vector3 rotation = transform.getRotation();
        
        /* Entity variables */
        int simId = entity.getSimID();
        
        /* Health variables */
        int currentHealth = health.getCurrent();
        int maxHealth = health.getMax();
        float healthPercentage = (float)currentHealth / (float)maxHealth;
        DamageEnum damageEnum = DamageEnum.HEALTHY;
        
        if (healthPercentage < DESTROYED_DAMAGE_THRESHOLD) {
            damageEnum = DamageEnum.DESTROYED;
        } else if (healthPercentage < MODERATE_DAMAGE_THRESHOLD) {
            damageEnum = DamageEnum.MODERATE_DAMAGE;
        } else if (healthPercentage < SLIGHT_DAMAGE_THRESHOLD) {
            damageEnum = DamageEnum.SLIGHT_DAMAGE;
        } 
        
        /* 
         * Request the entity type for this entity ID (it may not show in the map until the next time this entity is updated) 
         * Don't request the entity type if one has already been determined for the entity with this simId.
         * */
        if (getIsWorldStateManagerTrackingThisSession()) {
            if (!WorldStateManager.getInstance().entityTypeIsDefinedForId(domainSessionId, simId)) {
                requestEntityType(simId);
            }
        }
                
        /* Variables needed to set up the EntityState message */
        EntityIdentifier entityId = new EntityIdentifier(new SimulationAddress(0, 0), simId);
        EntityType entityType = new EntityType(0, 0, 0, 0, 0, 0, 0);
        int forceId = 0;
        
        if (getIsWorldStateManagerTrackingThisSession()) {
            if (WorldStateManager.getInstance().entityTypeIsDefinedForId(domainSessionId, simId)) {
                EntityTypeAndForceId entityMapValue = WorldStateManager.getInstance().getEntityTypeAndForceIdFor(domainSessionId, simId);
                if (entityMapValue != null) {
                    entityType = entityMapValue.getType();
                    forceId = entityMapValue.getForceId();
                }
            }
        }
                
        List<ArticulationParameter> artParams = new ArrayList<>(0);
        EntityAppearance appearance = new EntityAppearance(damageEnum, PostureEnum.UNUSED);
        EntityMarking entityMarking = new EntityMarking(EntityMarking.ASCII_CHARACTER_SET, String.valueOf(simId));
        
        EntityState entityState = new EntityState(
                entityId,
                forceId,
                entityType,
                new Vector3d(0,0,0),
                new Point3d(position.getX(), position.getY(), position.getZ()),
                new Vector3d(rotation.getX(), rotation.getY(), rotation.getZ()),
                artParams,
                appearance,
                entityMarking);
        
        // NOTE: Alternative Entity Type should be optional, but it being set to null is causing a NullPointerException. Should a null check be added, or its comment changed to remove the note about being optional?
        entityState.setAlternativeEntityType(entityType);
        
        return entityState;
    }

    /**
     * Processes an Enter Area protobuf message.
     * 
     * @param msg the EnterAreaMessage
     */
    private void handleEnterAreaProtoMessage(EntityEvents.EnterAreaMessage msg) {
        
        // TODO: This message can currently be received and parsed, but does not yet do anything with the received data.
//        EntityOuterClass.EntityTransform transform = msg.getEntity();
//        EntityOuterClass.EntityTransform areaTransform = msg.getArea();
//        Timestamp time = msg.getWhen();
        
    }
    
    /**
     * Sends a feedback message to HAVEN to be displayed in the application
     * 
     * The response is received and then processed asynchronously. 
     * This does not prevent the current thread from continuing
     * while it waits for a response from HAVEN.        
     * 
     * @param message The string to be displayed. Must not be null.
     * @param duration The number of seconds to display the message. Must be greater than 0.
     */
    private void sendDisplayFeedBackMessage(String message, float duration) {
        
        if (message == null) {
            logger.error("Tried to send a Display Feedback message to SE Sandbox with a null message. Message must be a non-null string value.");
            return;
        }
        
        if (duration <= 0.0) {
            logger.error("Tried to send a Display Feedback message to SE Sandbox with a duration of " + duration + ". Duration must be a float greater than 0.");
            return;
        }
        
        try {
            ScenarioFunctions.ProvideFeedbackRequest request = ScenarioFunctions.ProvideFeedbackRequest.newBuilder()
                    .setDuration(duration)
                    .setMessage(message)
                    .setFeedbackType(CommunicationType.Text)
                    .setReceiverType(ReceiverType.All_Trainees)
                    .build();
            
            scenarioServiceStub.provideFeedback(request, new StreamObserver<ScenarioFunctions.ProvideFeedbackResponse>() {
                
                @Override
                public void onNext(ProvideFeedbackResponse response) {
                    if(logger.isInfoEnabled()){
                        logger.info("Feedback received by SE Sandbox: " + message);
                    }
                    
                }
                
                @Override
                public void onError(Throwable t) {
                    logger.error("Error sending feedback to SE Sandbox", t);
                    
                }
                
                @Override
                public void onCompleted() {
                    if(logger.isInfoEnabled()){
                        logger.info("Sending feedback completed: " + message);
                    }
                    
                }
            });
        } catch (Exception e) {
            logger.error("Failed to send a DisplayFeedBackMessage to SE Sandbox", e);
        }
    }
    
    /**
     * Sends a message to HAVEN to disable the weapon of the specified entity.
     * 
     * The response is received and then processed asynchronously. 
     * This does not prevent the current thread from continuing
     * while it waits for a response from HAVEN.
     * 
     * @param simId The simulation ID of the entity whose weapon malfunctions
     */
    private void sendWeaponMalfunction(int simId) {
        try {
            EquipmentFunctions.GetPrimaryWeaponRequest weaponRequest = EquipmentFunctions.GetPrimaryWeaponRequest.newBuilder()
                    .setOwner(simId)
                    .build();
                    
            
                    
            equipmentServiceStub.getPrimaryWeapon(weaponRequest, new StreamObserver<EquipmentFunctions.GetPrimaryWeaponResponse>() {
                
                @Override
                public void onNext(GetPrimaryWeaponResponse weaponResponse) {
                    if(logger.isInfoEnabled()){
                        logger.info("Received weapon ID " + weaponResponse.getWeapon() + " for entity ID: " + simId);
                    }
                    
                    EquipmentFunctions.MalfunctionRequest malfunctionRequest = EquipmentFunctions.MalfunctionRequest.newBuilder()
                            .setItem(weaponResponse.getWeapon())
                            .build();
                    
                    equipmentServiceStub.malfunction(malfunctionRequest, new StreamObserver<EquipmentFunctions.MalfunctionResponse>() {
                        
                        @Override
                        public void onNext(MalfunctionResponse malfunctionResponse) {
                            if(logger.isInfoEnabled()){
                                logger.info("Malfunction inject received by SE Sandbox for entity ID: " + simId);
                            }
                            
                        }
                        
                        @Override
                        public void onError(Throwable t) {
                            logger.error("Error sending malfunction to SE Sandbox", t);
                            
                        }
                        
                        @Override
                        public void onCompleted() {
                            if(logger.isInfoEnabled()){
                                logger.info("Sending malfunction completed for entity ID: " + simId);
                            }
                            
                        }
                    });
                }
                
                @Override
                public void onError(Throwable t) {
                    logger.error("Error getting a primary weapon from SE Sandbox", t);
                    
                }
                
                @Override
                public void onCompleted() {
                    if(logger.isInfoEnabled()){
                        logger.info("Getting primary weapon completed for entity ID: " + simId);
                    }
                    
                }
            });
        } catch (Exception e) {
            logger.error("Failed to send a Weapon Malfunction inject to SE Sandbox", e);
        }
        
    }
    
    /**
     * Sends a message to HAVEN to repair the weapon of the specified entity.
     * 
     * @param simId The simulation ID of the entity whose weapon is repaired
     */
    private void sendEntityRepairObjectMessage(int simId) {        
        try {
            EquipmentFunctions.GetPrimaryWeaponRequest weaponRequest = EquipmentFunctions.GetPrimaryWeaponRequest.newBuilder()
                    .setOwner(simId)
                    .build();
                    
            
                    
            equipmentServiceStub.getPrimaryWeapon(weaponRequest, new StreamObserver<EquipmentFunctions.GetPrimaryWeaponResponse>() {
                
                @Override
                public void onNext(GetPrimaryWeaponResponse weaponResponse) {
                    if(logger.isInfoEnabled()){
                        logger.info("Received weapon ID " + weaponResponse.getWeapon() + " for entity ID: " + simId);
                    }
                    
                    EquipmentFunctions.RepairRequest repairRequest = EquipmentFunctions.RepairRequest.newBuilder()
                            .setItem(weaponResponse.getWeapon())
                            .build();
                    
                    equipmentServiceStub.repair(repairRequest, new StreamObserver<EquipmentFunctions.RepairResponse>() {
                        
                        @Override
                        public void onNext(EquipmentFunctions.RepairResponse repairResponse) {
                            if(logger.isInfoEnabled()){
                                logger.info("Repair inject received by SE Sandbox for entity ID: " + simId);
                            }
                            
                        }
                        
                        @Override
                        public void onError(Throwable t) {
                            logger.error("Error sending repair to SE Sandbox", t);
                            
                        }
                        
                        @Override
                        public void onCompleted() {
                            if(logger.isInfoEnabled()){
                                logger.info("Sending repair completed for entity ID: " + simId);
                            }
                            
                        }
                    });
                }
                
                @Override
                public void onError(Throwable t) {
                    logger.error("Error getting a primary weapon from SE Sandbox", t);
                    
                }
                
                @Override
                public void onCompleted() {
                    if(logger.isInfoEnabled()){
                        logger.info("Getting primary weapon completed for entity ID: " + simId);
                    }
                    
                }
            });
        } catch (Exception e) {
            logger.error("Failed to send a Repair inject to SE Sandbox", e);
        }
    }
    
    /**
     * Sends a target time, as a protobuf Timestamp, rounded down to the nearest second, to HAVEN.
     * HAVEN should set the time in the scenario to this time.
     * 
     * @param timeOfDayStamp Target time of day, rounded down to the nearest second.
     * @throws IllegalArgumentException if the timeOfDayStamp is null.
     */
    private void sendTimeOfDay(Timestamp timeOfDayStamp) throws IllegalArgumentException {
        try {
            if (timeOfDayStamp == null) {
                throw new IllegalArgumentException("The parameter 'targetTimeOfDay' cannot be null.");
            }
            
            ScenarioFunctions.SetTimeOfDayRequest timeOfDayRequest = ScenarioFunctions.SetTimeOfDayRequest.newBuilder()
                    .setTimeOfDay(timeOfDayStamp)
                    .build();
            
            scenarioServiceStub.setTimeOfDay(timeOfDayRequest, new StreamObserver<ScenarioFunctions.SetTimeOfDayResponse>() {
                
                @Override
                public void onNext(SetTimeOfDayResponse arg0) {
                    if(logger.isInfoEnabled()){
                        logger.info("'Set Time of Day' inject received by SE Sandbox for time stamp: " + timeOfDayStamp);
                    }
                }
                
                @Override
                public void onError(Throwable t) {
                    logger.error("Error sending 'Set Time of Day' inject to SE Sandbox: " + timeOfDayStamp, t);
                    
                }
                
                @Override
                public void onCompleted() {
                    if(logger.isInfoEnabled()){
                        logger.info("'Set Time of Day' inject to SE Sandbox was completed for time stamp: " + timeOfDayStamp);
                    }
                }
            });
             
        } catch (Exception e) {
            logger.error("Failed to send a 'Set Time of Day' inject to SE Sandbox", e);
        }
    }
    
    /**
     * Takes a target TimeOfDay enumerator, and the current time. Skips forward until the hour matches the target hour (without changing the minutes),
     * then produces a Timestamp object that can be sent to HAVEN.
     * 
     * The TimeOfDay enumerators are mapped to hours of the day.
     * 
     * The Timestamp is rounded down to the nearest second.
     * 
     * @param targetTimeOfDay A TimeOfDay enumerator. Will be mapped to a numerical hour.
     * @param currentTimeMilliseconds The current time in milliseconds.
     * @return A protobuf TimeOfDay object which can be sent to HAVEN
     * @throws IllegalArgumentException if targetTimeOfDay is null or currentTimeMilliseconds is negative.
     */
    private Timestamp convertTimeOfDayToTimeStamp(generated.dkf.EnvironmentAdaptation.TimeOfDay targetTimeOfDay, long currentTimeMilliseconds) throws IllegalArgumentException {
        
        if (targetTimeOfDay == null) {
            throw new IllegalArgumentException("The parameter 'targetTimeOfDay' cannot be null.");
        } else if (currentTimeMilliseconds < 0) {
            throw new IllegalArgumentException("The parameter 'currentTimeMilliseconds' cannot be negative.");
        }
        
        int targetHourOfDay = DEFAULT_MIDNIGHT;
        if(targetTimeOfDay.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Dawn){
            targetHourOfDay = DEFAULT_DAWN;
        }else if(targetTimeOfDay.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Dusk){
            targetHourOfDay = DEFAULT_DUSK;
        }else if(targetTimeOfDay.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Midday){
            targetHourOfDay = DEFAULT_MIDDAY;
        }else if(targetTimeOfDay.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Midnight){
            targetHourOfDay = DEFAULT_MIDNIGHT;
        }
        
        try {

            // retrieve the hour of day in the time specified by currentTimeMilliseconds
            Date havenDate = new Date(currentTimeMilliseconds);
            Calendar havenCalendar = GregorianCalendar.getInstance();
            havenCalendar.setTime(havenDate);
            int currentHourOfDay = havenCalendar.get(Calendar.HOUR_OF_DAY);

            //calculate the amount of time to skip forward in HAVEN
            long skipTime = 0;
            int hourDiff = targetHourOfDay - currentHourOfDay;

            if(hourDiff > 0){
                //desired hour is in the future (of the current day)
                skipTime = hourDiff;

            }else if(hourDiff < 0){
                //desired time is in the past (of the current day)
                //to avoid going back in time, skip to that time on the next day
                skipTime = hourDiff + HOURS_IN_DAY;
            }

            skipTime *= MILLISECONDS_IN_HOUR;
            
            long resultTime = currentTimeMilliseconds + skipTime;
            
            long resultTimeSeconds = resultTime / MILLISECONDS_IN_SECOND;
            
            Timestamp resultTimestamp = Timestamp.newBuilder().setSeconds(resultTimeSeconds).build();
            
            return resultTimestamp;

        } catch (Exception e) {
            logger.error("Caught an exception while changing the time of day. Defaulting to current simulated SE Sandbox time. ", e);
            
            long currentTimeSeconds = currentTimeMilliseconds / MILLISECONDS_IN_SECOND;
            
            Timestamp currentHavenTimestamp = Timestamp.newBuilder().setSeconds(currentTimeSeconds).build();
            
            return currentHavenTimestamp;
        }
    }

    /**
     * Processes an EntityUpdate protobuf message.
     * 
     * @param msg the EntityUpdateMessage
     */
    private void handleEntityUpdateProtoMessage(EntityEvents.EntityUpdateMessage msg) {
        
        EntityOuterClass.EntityTransform transform = msg.getEntity();
        EntityOuterClass.Health health = msg.getHealth();
                
        EntityState entityState = getEntityStateFromHavenEntity(transform, health);
                        
        GatewayModule.getInstance().sendMessageToGIFT(entityState, MessageTypeEnum.ENTITY_STATE, this);
    }

    /**
     * Processes an EntityUpdates protobuf message.
     * 
     * @param msg the EntityUpdatesMessage
     */
    private void handleEntityUpdatesProtoMessage(EntityEvents.EntityUpdatesMessage msg) {
        
        List<EntityEvents.EntityUpdateMessage> updateList = msg.getUpdatesList();
        for (EntityEvents.EntityUpdateMessage entityUpdate : updateList) {
            handleEntityUpdateProtoMessage(entityUpdate);
        }
    }
    
    /**
     * Processes a SimulationStateUpdate protobuf message.
     * 
     * @param msg the SimulationStateUpdateMessage
     */
    private void handleSimulationStateUpdateProtoMessage(EntityEvents.SimulationStateUpdateMessage msg) {
        
     // TODO: This message can currently be received and parsed, but does not yet do anything with the received data.
//        EntityOuterClass.SimulationStateType state = msg.getState();
        
    }
    
    /**
     * Processes an EntityFireWeapon protobuf message.
     * 
     * As of now, this method displays fire lines and detonation events, but uses default and unknown values
     * for related data, due to the fact that HAVEN produces more limited information than is used by the
     * Detonation and WeaponFire messages. As incoming data from HAVEN is improved, this can be adjusted to record more precise data.
     * 
     * @param msg the EntityFireWeaponMessage
     */
    private void handleEntityFireWeaponProtoMessage(EntityEvents.EntityFireWeaponMessage msg) {
        
        EntityOuterClass.EntityTransform firingEntityTransform = msg.getFiringEntity();
        EntityOuterClass.EntityTransform targetEntityTransform = msg.getTarget();
        EntityOuterClass.Vector3 weaponImpactLocation = msg.getImpactLocation();
        int firingEventId = msg.getId();
        boolean isHit = msg.getIsHit();
        
        /* 
         * Default/unknown values are used in cases where data from HAVEN's EntityFireWeaponMessage does not
         * contain information used by GIFT's Detonation or WeaponFire messages.
         */
        EntityIdentifier feIdentifier = new EntityIdentifier(new SimulationAddress(0, 0), firingEntityTransform.getEntity().getSimID());
        EntityIdentifier teIdentifier = new EntityIdentifier(new SimulationAddress(0, 0), targetEntityTransform.getEntity().getSimID());
        EntityIdentifier blankIdentifier = new EntityIdentifier(new SimulationAddress(0, 0), 0);
        EventIdentifier eventId = new EventIdentifier(new SimulationAddress(0, 0), firingEventId);
        EntityType entityType = new EntityType(0, 0, 0, 0, 0, 0, 0);
        BurstDescriptor burstDescriptor = new BurstDescriptor(entityType, 0, 0, 0, 0);
        DetonationResultEnum resultEnum = DetonationResultEnum.NONE;
        
        /* If the weapon fire hits an entity, then record it as an ENTITY_IMPACT. */
        if (isHit && targetEntityTransform != null && targetEntityTransform.getEntity() != null) {
            Entity targetEntity = targetEntityTransform.getEntity();

            if (targetEntity.getSimID() != 0 && targetEntity.getEngineID() != 0) {
                resultEnum = DetonationResultEnum.ENTITY_IMPACT;
            }
        }
        
        if (isHit) {
        	
        	WeaponFire weaponFire = new WeaponFire(
                    feIdentifier, 
                    teIdentifier, 
                    blankIdentifier, 
                    eventId, 
                    new Vector3d(0.0, 0.0, 0.0),
                    new Vector3d(firingEntityTransform.getPosition().getX(),
                            firingEntityTransform.getPosition().getY(), 
                            firingEntityTransform.getPosition().getZ()), 
                    burstDescriptor);
            
            GatewayModule.getInstance().sendMessageToGIFT(weaponFire, MessageTypeEnum.WEAPON_FIRE, this);
        	
            Detonation detonation = new Detonation(
                    feIdentifier, 
                    teIdentifier, 
                    blankIdentifier, 
                    eventId, 
                    new Vector3d(0.0, 0.0, 0.0), 
                    new Vector3d(weaponImpactLocation.getX(), weaponImpactLocation.getY(), weaponImpactLocation.getZ()),
                    burstDescriptor, 
                    resultEnum);
            
            GatewayModule.getInstance().sendMessageToGIFT(detonation, MessageTypeEnum.DETONATION, this);
        }
    }
    
    /**
     * Determines the entity type of a HAVEN protobuf message from a byte array.
     * 
     * @param stateByteArray the byte array
     * @return the EventType of the HAVEN protobuf message.
     */
    private EntityEvents.EventType getEventTypeFromBytes(byte[] stateByteArray) {
        
        ByteBuffer buffer = ByteBuffer.wrap(stateByteArray);
        
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        short stateInteger = buffer.getShort();
                
        EntityEvents.EventType eventTypeToReturn = EntityEvents.EventType.forNumber(stateInteger);
        
        return eventTypeToReturn;
    }
    
    /**
     * Determines the size (in bytes) of a HAVEN protobuf message from a byte array.
     * 
     * @param sizeByteArray the byte array
     * @return an integer containing the size (in bytes) of the upcoming protobuf message.
     */
    private int getProtobufSizeFromBytes(byte[] sizeByteArray) {
        
        ByteBuffer buffer = ByteBuffer.wrap(sizeByteArray);
        
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int sizeValue = buffer.getInt();
                        
        return sizeValue;
    }

    @Override
    public void setEnabled(boolean value) throws ConfigurationException{
        
        if(isEnabled() && !value){
            //transition from enabled to not enabled
            
            if(logger.isInfoEnabled()){
                logger.info("Disabling SE Sandbox plugin interface");
            }
            
            try {
                if(socketHandler != null){
                    socketHandler.disconnect();
                    disconnectGrpcChannel();
                }
            } catch (IOException e) {
                if(logger.isDebugEnabled()){
                    logger.debug("Exception occurred while disabling SE Sandbox plugin. This should not cause an error due to when it occurs.", e);
                }
            }
            
            // When this plugin is disabled, clear the world state manager session, so that the data does not persist unnecessarily
            clearWorldStateManagerSession();
            
        }else if(!isEnabled() && value){
            //transition from not enabled to enabled
            
            if(logger.isInfoEnabled()){
                logger.info("Enabling SE Sandbox plugin interface");
            }
            
            try {
                createSocketHandler();
                socketHandler.connect();
                connectGrpcChannel();
            } catch (IllegalArgumentException | IOException e) {
                throw new ConfigurationException("Failed to connect the SE Sandbox Plugin interface socket connection.", 
                        "There was an exception while trying to connect:\n"+e.getMessage(), e);
            }
        }
        
        super.setEnabled(value);
    }
    
    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes() {
        return PRODUCED_MSG_TYPES;
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) throws ConfigurationException {
                
        if (message.getMessageType() == MessageTypeEnum.ENVIRONMENT_CONTROL) {

            EnvironmentControl control = (EnvironmentControl)message.getPayload();
            applyEnvironmentControl(control, errorMsg);
        } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST){
            
            String text = (String)message.getPayload();
            sendDisplayFeedBackMessage(text, DEFAULT_FEEDBACK_DURATION);
            
        } else if(message.getMessageType() == MessageTypeEnum.SIMAN){
            // no-op
            // Even though this class doesn't handle SIMAN GIFT messages yet, 
            // this is needed in order to run without a 'There are currently no enabled Gateway 
            // module interop plugins that can handle the message type Siman' course ending error
            
        } else if (message.getMessageType() == MessageTypeEnum.LESSON_STARTED || 
                message.getMessageType() == MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS ||
                message.getMessageType() == MessageTypeEnum.INIT_INTEROP_CONNECTIONS) {
            
            /*
             *  These message types are all domain session messages that occur near the beginning of a course.
             *  Whichever of these is received first should trigger the plugin to start tracking the world state.
             *  
             *  If startTrackingWorldState is called a second time with a message from the same domain session,
             *  it will ignore any past the first, unless the WorldState has been cleared.
             */
            startTrackingWorldState((DomainSessionMessage)message);
        } else if(message.getMessageType() == MessageTypeEnum.VARIABLE_STATE_REQUEST){
                
            sendVariablesStateQuery((DomainSessionMessage)message);
                
        } else {
            logger.error("Received unhandled GIFT message to send over to the SE Sandbox plugin, " + message);            
            errorMsg.append("SE Sandbox plugin can't handle message of type ").append(message.getMessageType());
        }
        
        return false;
    }
    
    /**
     * Query SE Sandbox via the GIFT SE Sandbox Plugin on behalf of GIFT for variable state info on one or more
     * players in the environment.
     * 
     * @param variablesStateRequestMsg the original request message to respond to with the
     *        variables state results. Cannot be null.
     */
    private void sendVariablesStateQuery(DomainSessionMessage variablesStateRequestMsg) {
        
        if (variablesStateRequestMsg == null) {
            throw new IllegalArgumentException("The parameter 'variablesStateRequestMsg' cannot be blank.");
        }
        
        if(logger.isInfoEnabled()) {            
            logger.info("\nSE Sandbox plugin rec'd Variables State Request Query");            
        }
        
        VariablesStateRequest request = (VariablesStateRequest) variablesStateRequestMsg.getPayload();
        for(VARIABLE_TYPE varType : request.getTypeToVarInfoMap().keySet()){
        
            VariableInfo varInfo = request.getTypeToVarInfoMap().get(varType);
            Set<String> entityIds = varInfo.getEntityIds();
            switch(varType){
            case WEAPON_STATE:
                if(entityIds == null){
                    continue;
                }
                    
                for(String entityId : entityIds){
                    requestWeaponSafetyState(entityId, variablesStateRequestMsg);
                }
                
               break;
            }
        }
    }
    
    /**
     * When a domain session message is received, IF the world state manager is not already tracking this session, 
     * sets up information from the user session so that this plugin
     * can use it with the WorldStateManager.
     * 
     * @param message the domain session message
     */
    private void startTrackingWorldState(DomainSessionMessage domainMessage) {
        
        if (!getIsWorldStateManagerTrackingThisSession()) {
        
            domainSessionId = domainMessage.getDomainSessionId();
            WorldStateManager.getInstance().getWorldState(domainSessionId);            
        }
    }
    
    /**
     * Clears out the current user session's WorldState from the WorldStateManager
     * and indicates that the world state manager is not currently tracking this session
     */
    private void clearWorldStateManagerSession() {
        
        WorldStateManager.getInstance().clearWorldState(domainSessionId);
        domainSessionId = null;
    }

    /**
     * Apply the scenario adaptation using HAVEN scripting command.
     * 
     * @param control contains parameters for the scenario adaptation (e.g. set
     *        fog to 0.9)
     * @param errorMsg error message produced by the plugin handling this gift
     *        message
     */
    private void applyEnvironmentControl(EnvironmentControl control, StringBuilder errorMsg) {

        EnvironmentAdaptation adaptation = control.getEnvironmentStatusType();
        Serializable type = adaptation.getType();
        
        if(type instanceof generated.dkf.EnvironmentAdaptation.Script){

            generated.dkf.EnvironmentAdaptation.Script script = (generated.dkf.EnvironmentAdaptation.Script)type;

            if(logger.isDebugEnabled()){
                logger.debug("sending command to SE Sandbox plugin:\n"+script.getValue());
            }

            handleScript(script.getValue(), errorMsg);
        } else if (type instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay) {
            
            try {
                generated.dkf.EnvironmentAdaptation.TimeOfDay targetTimeOfDay = (generated.dkf.EnvironmentAdaptation.TimeOfDay)type;
                
                /* 
                 * Get the current time in milliseconds. In the future, if HAVEN offers a request for current time, we can modify
                 * this to start from HAVEN's current time instead.
                 */
                Date currentDate = new Date();
                
                long currentTimeMilliseconds = currentDate.getTime();
                
                Timestamp timeOfDayStamp = convertTimeOfDayToTimeStamp(targetTimeOfDay, currentTimeMilliseconds);
            
                sendTimeOfDay(timeOfDayStamp);
            } catch (Exception e) {
                logger.error("Error sending a Time of Day to SE Sandbox.", e);
            }
        }
        
    }

    /**
     * Parse a script command to HAVEN GIFT Plugin, convert it to a protobuf message,
     * and send the resulting method to the external HAVEN application.
     * <br/>
     * Script follows the pattern of [entity ID] [command name] [comma-delimited arguments].
     * <br/>
     * For example: "10 disableWeaponControl true" disables the weapon of the entity with simId 10.
     * 
     * @param value The HAVEN command to parse in the HAVEN plugin and then send to the external HAVEN application. Can't be null.
     * @param errorMsg Buffer to write error messages to.  Can't be null.
     */
    private void handleScript(String value, StringBuilder errorMsg) {
        
        if (value == null) {
            throw new IllegalArgumentException("The parameter 'value' cannot be null.");
        }
        else if (errorMsg == null) {
            throw new IllegalArgumentException("The parameter 'errorMsg' cannot be null.");
        }
        
        String[] scriptCommandSignature = value.split(" ");
        if (scriptCommandSignature.length > 2) {
            
            String entityId = scriptCommandSignature[0];
            
            String commandName = scriptCommandSignature[1];
            
            String[] scriptArgsSplit = scriptCommandSignature[2].split(",");
            
            if (commandName.equalsIgnoreCase(DISABLE_WEAPON_CONTROL_SCRIPT_NAME)) {
                if (scriptArgsSplit.length >= 1) {
                    String fireArg = scriptArgsSplit[0];
                    
                    int simId = 0;
                    
                    try {
                        simId = Integer.parseInt(entityId);
                    } catch (Exception e) {
                        logger.error("ID of user in disableWeaponControl script is not useable. Must be an integer value.", e);
                        errorMsg.append("ID of user in disableWeaponControl script is not useable. Must be an integer value.");
                        
                        return;
                    }
                    
                    sendDisableWeaponControlCommand(Boolean.valueOf(fireArg).booleanValue(), simId);
                    
                } else {
                    logger.error("Command to SE Sandbox does not have enough arguments for disableWeaponControl.");
                    errorMsg.append("Command to SE Sandbox does not have enough arguments for disableWeaponControl. ").append(scriptArgsSplit.length).append(" arguments defined. 5 are required.");
                }
            }
            else {
                logger.error("script command to SE Sandbox of \"" + commandName + "\" was not recognized as a valid command.");
                errorMsg.append("script command to SE Sandbox of \"").append(commandName).append("\" was not recognized as a valid command.");
            }
        } else {
            logger.error("Command to SE Sandbox improperly formatted and could not be sent.");
            errorMsg.append("Command to SE Sandbox improperly formatted and could not be sent.");

        }
    }
    
    /**
     * Sends a command to HAVEN to either cause a weapon to malfunction or to repair the weapon of a given entity.
     * 
     * @param fireArg if true, the entity's weapon will malfunction. If false, any malfunction will be removed and the weapon will be repaired.
     * @param simId the simulation ID of the target entity
     */
    private void sendDisableWeaponControlCommand(boolean fireArg, int simId) {
        if (fireArg) {
            sendWeaponMalfunction(simId);
        } else {
            sendEntityRepairObjectMessage(simId);
        }
    }

    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations() {
        return REQ_TRAINING_APPS;
    }

    @Override
    public Serializable getScenarios() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() throws DetailedException {
        return null;
    }

    @Override
    public void selectObject(Serializable objectIdentifier) throws DetailedException {
        
    }

    @Override
    public void loadScenario(String scenarioIdentifier) throws DetailedException {
        
    }

    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }

    @Override
    public void cleanup() {
        
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SESandboxPluginInterface: ");
        sb.append(super.toString());
        sb.append(", socket = ").append(socketHandler);
        
        sb.append(", messageTypes = {");
        for(MessageTypeEnum mType : supportedMsgTypes){
            sb.append(mType).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }

}
