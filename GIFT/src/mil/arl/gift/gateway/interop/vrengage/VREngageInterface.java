/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.vrengage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dignitas.vrengage.protobuf.VrEngageCommon.Vector3d;
import com.dignitas.vrengage.protobuf.VrEngageCommon.VrEngageMessage;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.ActorSide;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.CloudState;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.CreateActor;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Fog;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Overcast;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Rain;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.RemoveActors;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.RunScript;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.Teleport;
import com.dignitas.vrengage.protobuf.VrEngageEnvironment.TimeOfDay;
import com.dignitas.vrengage.protobuf.VrEngageLOS.LosRequest;
import com.dignitas.vrengage.protobuf.VrEngageLOS.LosResponse;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.InvalidProtocolBufferException;

import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.ta.state.LoSResult;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.LoSResult.VisibilityResult;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.socket.SocketHandler;

/**
 * This is the VR-Engage interop interface responsible for communicating with VR-engage.
 *
 * @author nroberts
 */
public class VREngageInterface extends AbstractInteropInterface {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(VREngageInterface.class);

    private static final boolean RETRY_ON_FAIL = true;
    private static final int DEFAULT_ADAPTATION_DURATION = 5;
    private static final int DEFAULT_DUSK = 18;
    private static final int DEFAULT_DAWN = 8;
    private static final int DEFAULT_MIDDAY = 12;
    private static final int DEFAULT_MIDNIGHT = 0;

    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;
    static{
        supportedMsgTypes = new ArrayList<>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
        supportedMsgTypes.add(MessageTypeEnum.ENVIRONMENT_CONTROL);
        supportedMsgTypes.add(MessageTypeEnum.LOS_QUERY);
        supportedMsgTypes.add(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST);
    }

    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from
     * an external training applications (e.g. VR-Engage).
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.STOP_FREEZE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.START_RESUME);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.LOS_RESULT);
    }

    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.VR_ENGAGE);
    }

    /** the TCP socket connection used to send commands to VR-Engage */
    private ProtobufSocketHandler socketHandler = null;

    /** configuration parameters for the connection to the GIFT VR-Engage plugin */
    private generated.gateway.VREngage vrEngagePluginConfig = null;

    /**
     * Class constructor
     *
     * @param name - the display name of the plugin
     */
    public VREngageInterface(String name) {
        super(name, true);
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {

        if (config instanceof generated.gateway.VREngage) {

            this.vrEngagePluginConfig = (generated.gateway.VREngage) config;
            createSocketHandler();

            if(logger.isInfoEnabled()){
                logger.info("Plugin has been configured");
            }

        } else {
            throw new ConfigurationException("VR-Engage Plugin interface can't configure.",
                    "The VR-Engage Plugin interface only uses the interop config type of "+generated.gateway.VREngage.class+" and doesn't support using the interop config instance of " + config,
                    null);
        }

        return false;
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
        boolean replySent = false;

        if (message.getMessageType() == MessageTypeEnum.SIMAN) {

            Siman siman = (Siman) message.getPayload();

            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) {
                //TODO: Send appropriate command to VR-Engage. For now, assume VR-Engage is in the appropriate state.

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.START) {

                //TODO: Send appropriate command to VR-Engage. For now, assume VR-Engage is in the appropriate state.

                //Because VR-Engage doesn't send a START_RESUME DIS PDU, create one of our own
                StartResume startResume = new StartResume(0, 0, 0);

                DomainSessionMessage dsMsg = (DomainSessionMessage)message;
                GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), startResume, MessageTypeEnum.START_RESUME, this);

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.PAUSE) {

                //TODO: Send appropriate command to VR-Engage. For now, assume VR-Engage is in the appropriate state.

                //Because VR-Engage doesn't send a DIS STOP_FREEZE DIS PDU once the VR-Engage scenario is paused, create one of our own
                StopFreeze stopFreeze = new StopFreeze(0, StopFreeze.RECESS, 0, 0);

                DomainSessionMessage dsMsg = (DomainSessionMessage)message;
                GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), stopFreeze, MessageTypeEnum.STOP_FREEZE, this);

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESUME) {

                //TODO: Send appropriate command to VR-Engage. For now, assume VR-Engage is in the appropriate state.

                //Because VR-Engage doesn't send a START_RESUME DIS PDU, create one of our own
                StartResume startResume = new StartResume(0, 0, 0);

                DomainSessionMessage dsMsg = (DomainSessionMessage)message;
                GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), startResume, MessageTypeEnum.START_RESUME, this);

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.STOP) {

                //TODO: Send appropriate command to VR-Engage. For now, assume VR-Engage is in the appropriate state.

                //Because VR-Engage doesn't send a DIS STOP_FREEZE DIS PDU once the VR-Engage scenario is stopped, create one of our own
                StopFreeze stopFreeze = new StopFreeze(0, 0, 0, 0);

                DomainSessionMessage dsMsg = (DomainSessionMessage)message;
                GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), stopFreeze, MessageTypeEnum.STOP_FREEZE, this);

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESTART) {

                //nothing to do

            } else{
                errorMsg.append("VR-Engage plugin can't handle siman type of ").append(siman.getSimanTypeEnum());
                logger.error("Found unhandled Siman type of "+siman.getSimanTypeEnum());
            }

        } else if (message.getMessageType() == MessageTypeEnum.ENVIRONMENT_CONTROL) {

            EnvironmentControl control = (EnvironmentControl)message.getPayload();
            applyEnvironmentControl(control, message, errorMsg);

        }else if (message.getMessageType() == MessageTypeEnum.LOS_QUERY) {

            replySent = doLosQuery((DomainSessionMessage)message, errorMsg);

        }else if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST){

            //TODO: Handle command to display feedback

        }else {
            logger.error("Received unhandled GIFT message to send over to the VR-Engage plugin, " + message);

            errorMsg.append("VR-Engage plugin can't handle message of type ").append(message.getMessageType());
        }

        return replySent;
    }

    /**
     * Apply the scenario adaptation using VR-Engage's messaging protocol
     *
     * @param control contains parameters for the scenario adaptation (e.g. set
     *        fog to 0.9)
     * @param message the GIFT message to translate and communicate via this
     *        interop interface
     * @param errorMsg error message produced by the plugin handling this gift
     *        message
     */
    private void applyEnvironmentControl(EnvironmentControl control, Message message, StringBuilder errorMsg){

        EnvironmentAdaptation adaptation = control.getEnvironmentStatusType();
        Serializable type = adaptation.getType();

        if(type instanceof generated.dkf.EnvironmentAdaptation.Overcast) {

            generated.dkf.EnvironmentAdaptation.Overcast overcast = (generated.dkf.EnvironmentAdaptation.Overcast)type;

            double value = overcast.getValue().doubleValue();

            //derive enum intensity level from intensity value
            CloudState state = CloudState.CLEAR;
            switch((int) Math.round(value * 6)) {

                case 1:
                    state = CloudState.MOSTLY_CLEAR;
                    break;
                case 2:
                    state = CloudState.PARTLY_CLOUDY;
                    break;
                case 3:
                    state = CloudState.CLOUDY;
                    break;
                case 4:
                    state = CloudState.MOSTLY_CLOUDY;
                    break;
                case 5:
                    state = CloudState.OVERCAST;
                    break;
                case 6:
                    state = CloudState.THUNDERSTORM;
                    break;
                default:
                    state = CloudState.CLEAR;
                    break;
            }

            int duration = DEFAULT_ADAPTATION_DURATION;
            if(overcast.getScenarioAdaptationDuration() != null){
                duration = overcast.getScenarioAdaptationDuration().intValue();
            }

            Overcast.Builder overcastBuilder = Overcast.newBuilder();
            overcastBuilder.setState(state);
            overcastBuilder.setDuration(Duration.newBuilder().setSeconds(duration));
            
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VR-Engage plugin:\n"+ overcastBuilder.toString());
            }

            VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
            messageBuilder.setPayload(Any.pack(overcastBuilder.build()));
            messageBuilder.build();

            sendCommand(messageBuilder.build(), message, errorMsg);

        } else if(type instanceof generated.dkf.EnvironmentAdaptation.Rain) {

            generated.dkf.EnvironmentAdaptation.Rain rain = (generated.dkf.EnvironmentAdaptation.Rain)type;

            double value = rain.getValue().doubleValue();

            int duration = DEFAULT_ADAPTATION_DURATION;
            if(rain.getScenarioAdaptationDuration() != null){
                duration = rain.getScenarioAdaptationDuration().intValue();
            }

            Rain.Builder rainBuilder = Rain.newBuilder();
            rainBuilder.setIntensity(value);
            rainBuilder.setDuration(Duration.newBuilder().setSeconds(duration));
            
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VR-Engage plugin:\n" + rainBuilder.toString());
            }

            VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
            messageBuilder.setPayload(Any.pack(rainBuilder.build()));
            messageBuilder.build();

            sendCommand(messageBuilder.build(), message, errorMsg);

        } else if(type instanceof EnvironmentAdaptation.Fog) {

            generated.dkf.EnvironmentAdaptation.Fog fog = (generated.dkf.EnvironmentAdaptation.Fog) type;

            int duration = DEFAULT_ADAPTATION_DURATION;
            if(fog.getScenarioAdaptationDuration() != null){
                duration = fog.getScenarioAdaptationDuration().intValue();
            }

            double rColor = 0d;
            double gColor = 0d;
            double bColor = 0d;

            if(fog.getColor() != null){

                generated.dkf.EnvironmentAdaptation.Fog.Color customColor = fog.getColor();

                rColor = customColor.getRed() / 255d;
                gColor = customColor.getGreen() / 255d;
                bColor = customColor.getBlue() / 255d;
            }

            Vector3d.Builder colorBuilder = Vector3d.newBuilder();
            colorBuilder.setX(rColor);
            colorBuilder.setY(gColor);
            colorBuilder.setZ(bColor);

            Fog.Builder fogBuilder = Fog.newBuilder();
            fogBuilder.setDensity(fog.getDensity().doubleValue());
            fogBuilder.setDuration(Duration.newBuilder().setSeconds(duration));
            fogBuilder.setColorRgb(colorBuilder);
            
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VR-Engage plugin:\n"+ fogBuilder.toString());
            }

            VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
            messageBuilder.setPayload(Any.pack(fogBuilder.build()));
            messageBuilder.build();

            sendCommand(messageBuilder.build(), message, errorMsg);

        } else if(type instanceof EnvironmentAdaptation.TimeOfDay) {

            generated.dkf.EnvironmentAdaptation.TimeOfDay tod = (generated.dkf.EnvironmentAdaptation.TimeOfDay) type;

            long hourOfDay = DEFAULT_MIDNIGHT;
            if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Dawn){
                hourOfDay = DEFAULT_DAWN;
            }else if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Dusk){
                hourOfDay = DEFAULT_DUSK;
            }else if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Midday){
                hourOfDay = DEFAULT_MIDDAY;
            }else if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Midnight){
                hourOfDay = DEFAULT_MIDNIGHT;
            }

            long secondsOfDay = TimeUnit.HOURS.toSeconds(hourOfDay);

            TimeOfDay.Builder todBuilder = TimeOfDay.newBuilder();
            todBuilder.setTimePastMidnight(Duration.newBuilder().setSeconds(secondsOfDay));
            
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VR-Engage plugin:\n"+ todBuilder.toString());
            }

            VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
            messageBuilder.setPayload(Any.pack(todBuilder.build()));
            messageBuilder.build();

            sendCommand(messageBuilder.build(), message, errorMsg);

        } else if(type instanceof EnvironmentAdaptation.RemoveActors) {

            generated.dkf.EnvironmentAdaptation.RemoveActors removeActors = (generated.dkf.EnvironmentAdaptation.RemoveActors) type;

            RemoveActors.Builder raBuilder = RemoveActors.newBuilder();
            Serializable actorId = removeActors.getType();
            if(actorId instanceof generated.dkf.EnvironmentAdaptation.RemoveActors.Location){
                //currently not supported
                errorMsg.append("Received unhandled remove actor identification type of Location for VR Engage remove actors scenario adaptation request.  Please use actor name instead.");
                return;

            }else if(actorId instanceof String){
                raBuilder.addEntityMarking((String) actorId);
            }
            
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VR-Engage plugin:\n"+ raBuilder.toString());
            }

            VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
            messageBuilder.setPayload(Any.pack(raBuilder.build()));
            messageBuilder.build();

            sendCommand(messageBuilder.build(), message, errorMsg);

        } else if(type instanceof EnvironmentAdaptation.Teleport) {

            generated.dkf.EnvironmentAdaptation.Teleport teleport = (generated.dkf.EnvironmentAdaptation.Teleport) type;

            String entityMarking = null;
            if(teleport.getTeamMemberRef() != null && StringUtils.isNotBlank(teleport.getTeamMemberRef().getEntityMarking())){
                entityMarking = teleport.getTeamMemberRef().getEntityMarking();
            }

            generated.dkf.Coordinate coordinate = teleport.getCoordinate();
            if(coordinate.getType() instanceof generated.dkf.GCC){

                Teleport.Builder teleportBuilder = Teleport.newBuilder();

                generated.dkf.GCC gcc = (generated.dkf.GCC)coordinate.getType();

                Vector3d.Builder locationBuilder = Vector3d.newBuilder();
                locationBuilder.setX(gcc.getX().doubleValue());
                locationBuilder.setY(gcc.getY().doubleValue());
                locationBuilder.setZ(gcc.getZ().doubleValue());

                teleportBuilder.setEntityMarking(entityMarking);
                teleportBuilder.setLocation(locationBuilder);

                if(teleport.getHeading() != null){
                    teleportBuilder.setHeading(teleport.getHeading().getValue());
                }
                
                if(logger.isDebugEnabled()){
                    logger.debug("sending command to VR-Engage plugin:\n"+ teleportBuilder.toString());
                }

                VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
                messageBuilder.setPayload(Any.pack(teleportBuilder.build()));
                messageBuilder.build();

                sendCommand(messageBuilder.build(), message, errorMsg);

            }else{
                errorMsg.append("Received unhandled coordinate type of ").append(coordinate.getType()).append(" for VR-Engage teleport learner scenario adaptation request.");
                return;
            }

        } else if(type instanceof generated.dkf.EnvironmentAdaptation.CreateActors){

            generated.dkf.EnvironmentAdaptation.CreateActors createActors = (generated.dkf.EnvironmentAdaptation.CreateActors)type;

            generated.dkf.EnvironmentAdaptation.CreateActors.Side side = createActors.getSide();
            ActorSide actorSide = ActorSide.CIVILIAN;
            if(side.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor){
                actorSide = ActorSide.FRIENDLY;
            }else if(side.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor){
                actorSide = ActorSide.ENEMY;
            }

            String actorType = createActors.getType();

            generated.dkf.Coordinate coordinate = createActors.getCoordinate();
            if(coordinate.getType() instanceof generated.dkf.GCC){

                CreateActor.Builder createBuilder = CreateActor.newBuilder();

                generated.dkf.GCC gcc = (generated.dkf.GCC)coordinate.getType();

                Vector3d.Builder locationBuilder = Vector3d.newBuilder();
                locationBuilder.setX(gcc.getX().doubleValue());
                locationBuilder.setY(gcc.getY().doubleValue());
                locationBuilder.setZ(gcc.getZ().doubleValue());

                createBuilder.setLocation(locationBuilder);
                createBuilder.setSide(actorSide);
                createBuilder.setType(actorType);
                
                if(logger.isDebugEnabled()){
                    logger.debug("sending command to VR-Engage plugin:\n"+ createBuilder.toString());
                }

                VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
                messageBuilder.setPayload(Any.pack(createBuilder.build()));
                messageBuilder.build();

                sendCommand(messageBuilder.build(), message, errorMsg);
            }

        } else if(type instanceof generated.dkf.EnvironmentAdaptation.Script) {

            generated.dkf.EnvironmentAdaptation.Script script = (generated.dkf.EnvironmentAdaptation.Script)type;

            RunScript.Builder scriptBuilder = RunScript.newBuilder();
            scriptBuilder.setScriptText(script.getValue());
            
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VR-Engage plugin:\n"+ scriptBuilder.toString());
            }

            VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
            messageBuilder.setPayload(Any.pack(scriptBuilder.build()));
            messageBuilder.build();

            sendCommand(messageBuilder.build(), message, errorMsg);


        } else {
            //ERROR
            logger.error("Received unhandled environment control type " + control.getEnvironmentStatusType());
            errorMsg.append("VR-Engage plugin can't handle environment control type of ").append(control.getEnvironmentStatusType());
        }

    }

    /**
     * DEPRECATED 12.8.20 - the LoS request now uses a list of entity marking strings, not a single integer id.
     * The LoSRequest and VR Engage plugin needs to be updated accordingly.
     * 
     * Query VR-Engage via the GIFT VR-Engage Plugin DLL on behalf of GIFT for
     * LOS between entity and a list of points in the environment.
     *
     * @param losQueryMsg the original request message to respond to with the
     *        LOS results
     * @param errorMsg The {@link StringBuilder} which any error messages will
     *        be appended to.
     * @return boolean - whether this method sent a GIFT message in response to
     *         this query
     */
    @Deprecated
    private boolean doLosQuery(DomainSessionMessage losQueryMsg, StringBuilder errorMsg) {

        boolean handled = false;

        if(logger.isInfoEnabled()) {
            logger.info("\nVR-Engage plugin rec'd LOS_QUERY");
        }

        LoSQuery losQuery = (LoSQuery)losQueryMsg.getPayload();
        List<Point3d> points = losQuery.getLocations();
       
        Map<String, List<VisibilityResult>> entityLoSResults = new HashMap<>();
        
        for(String entityMarking : losQuery.getEntities()){
            
            List<VisibilityResult> results = new ArrayList<>();
            entityLoSResults.put(entityMarking, results);        
        
            for(int index = 0; index < points.size(); index++) {

                Point3d point = points.get(index);
    
                Vector3d.Builder locationBuilder = Vector3d.newBuilder();
                locationBuilder.setX(point.getX());
                locationBuilder.setY(point.getY());
                locationBuilder.setZ(point.getZ());
    
                LosRequest.Builder requestBuilder = LosRequest.newBuilder();
    //            requestBuilder.setEntityId(losQuery.getEntityId());
                requestBuilder.setLocation(locationBuilder.build());
    
                VrEngageMessage.Builder messageBuilder = VrEngageMessage.newBuilder();
                messageBuilder.setPayload(Any.pack(requestBuilder.build()));
                messageBuilder.build();
    
                VrEngageMessage command = messageBuilder.build();
    
                if(logger.isInfoEnabled()) {
                    logger.info("sending command: " + command);
                }
    
                VrEngageMessage result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
    
                if(result != null) {
    
                    if(logger.isInfoEnabled()) {    
                        logger.info("rec'd result: " + result);
                    }
    
                    double visibilityResult = 0.0;
    
                    try {
                        Any payload = result.getPayload();
    
                        if(payload.is(LosResponse.class)) {
    
                            LosResponse response = payload.unpack(LosResponse.class);
                            visibilityResult = response.getVisibility();
    
                        } else if(logger.isInfoEnabled()){
                            logger.info("result was not a line-of-sight response. reporting 0.0 as visibility result");
                        }
    
                    } catch (InvalidProtocolBufferException e) {
    
                        if(logger.isInfoEnabled()) {
                            logger.info("line-of-sight response was not formatted properly. reporting 0.0 as visibility result. Error: " + e);
                        }
                    }
    
                    results.add(new VisibilityResult(index, visibilityResult));
                }
                else {    
                    logger.error("Received null result from command: " + command);
                }
            }
        }
        if(!entityLoSResults.isEmpty()) {

            LoSResult losResult = new LoSResult(entityLoSResults, losQuery.getRequestId());

            GatewayModule.getInstance().sendMessageToGIFT(losQueryMsg.getUserSession(), losQueryMsg.getDomainSessionId(), losQueryMsg.getExperimentId(), losResult, MessageTypeEnum.LOS_RESULT, this);

            handled = true;
        }
        else {

          logger.error("Failed to generates any results for LOS query");
          errorMsg.append("VR-Engage plugin failed to generate any results for LOS query");

        }

        return handled;

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
        //TODO: Clean up VR-Engage as needed (i.e. remove from foreground, if needed)
    }

    @Override
    public boolean isAvailable(boolean testInterop) {

        if(super.isAvailable(testInterop)){

            if(testInterop){
                //test connection to GIFT VR-Engage plugin

                if(logger.isInfoEnabled()){
                    logger.info("Checking if the GIFT VR-Engage plugin is active, i.e. is the socket connection valid and available.");
                }

                // make sure there is a connection
                boolean madeConnection = false;
                if(socketHandler == null || !socketHandler.isConnected()){
                    try {
                        createSocketHandler();
                        socketHandler.connect();
                        madeConnection = true;
                    } catch (IllegalArgumentException | IOException e) {
                        socketHandler = null;  //because the socket failed to connect, start over by setting the handler to null
                        throw new ConfigurationException("Failed to connect the VR-Engage Plugin interface socket connection.",
                                "There was an exception while trying to connect:\n"+e.getMessage(), e);
                    }
                }

                //disconnect the connection that was made here
                if(madeConnection){

                    if(logger.isInfoEnabled()){
                        logger.info("Successfully tested the VR-Engage plugin interface because a connection was made to the GIFT VR-Engage plugin");
                    }

                    try {
                        socketHandler.disconnect();
                    } catch (@SuppressWarnings("unused") IOException e) {
                        //not sure if we care at this point
                    }
                }
            }
        }

        return true;
    }

    /**
     * Creates the socket connection if there are configuration parameters and the socket handler in this class is null.
     */
    private void createSocketHandler(){

        if(vrEngagePluginConfig != null){

            if(socketHandler == null){
                socketHandler = new ProtobufSocketHandler(vrEngagePluginConfig.getNetworkAddress(), vrEngagePluginConfig.getNetworkPort());

                if(logger.isInfoEnabled()){
                    logger.info("VR-Engage plugin interface socket handler created.");
                }
            }
        }
    }

    /**
     * Send a script command to VR-Engage GIFT Plugin and then replies to the GIFT
     * message that called the command.
     *
     * @param command The VR-Engage command to execute in the VR-Engage plugin DLL
     * @param replyTo The request message that caused the command to be sent.  Can be null.
     * @param errorMsg Buffer to write error messages too.  Can't be null.
     */
    private void sendCommand(VrEngageMessage command, Message replyTo, StringBuilder errorMsg) {

        VrEngageMessage result = sendCommand(command, errorMsg, RETRY_ON_FAIL);

        if (result != null) {

            if(logger.isDebugEnabled()){
                logger.debug("Received a response of "+result+" from sending the command "+command);
            }

        } else {

            logger.error("The attempt at sending the command "+command+" has failed because it returned a result of null");

            errorMsg.append("The VR-Engage plugin failed to respond.  Check the interop configuration and the Gateway module log for more details.");

        }
    }

    /**
     * Sends a script command to the VR-Engage GIFT Plugin
     *
     * @param command The command to execute in the plugin
     * @param errorMsg - used to append error content too, if an error occurs
     * @param retryOnFail - whether to keep trying to send the command again if it fails
     * @return The result of the command being executed.  Null if there was an issue.
     */
    public VrEngageMessage sendCommand(VrEngageMessage command, StringBuilder errorMsg, boolean retryOnFail) {

        try {
            return socketHandler.sendCommand(command, errorMsg, retryOnFail, true);

        } catch (IOException e) {

            //this is usually caused when VR-Engage is not running when the GW module
            //starts
            socketHandler = null;

            logger.error("IOException caught when connecting to the VR-Engage Plugin.  This is not a problem if VR-Engage isn't running yet and you plan to start it later.  Also, if you don't plan on running any GIFT courses"+
                    " that use VR-Engage, this can be ignored.  Otherwise, one solution might be to try a different port. Refer to the GIFT installation instructions on VR-Engage for more information.", e);
            errorMsg.append("Failed to send message to the VR-Engage Plugin.  Is VR-Engage running and does it have the GIFT VR-Engage plugin installed?  Refer to the GIFT installation instructions on VR-Engage for more information.");
        }

        return null;
    }

    @Override
    public void setEnabled(boolean value) throws ConfigurationException{

        if(isEnabled() && !value){
            //transition from enabled to not enabled

            if(logger.isInfoEnabled()){
                logger.info("Disabling VR-Engage plugin interface");
            }

            try {
                if(socketHandler != null){
                    socketHandler.disconnect();
                }
            } catch (@SuppressWarnings("unused") IOException e) {
                //not sure if we care at this point
            }
        }else if(!isEnabled() && value){
            //transition from not enabled to enabled

            if(logger.isInfoEnabled()){
                logger.info("Enabling VR-Engage plugin interface");
            }

            try {
                createSocketHandler();
                socketHandler.connect();
            } catch (IllegalArgumentException | IOException e) {

                throw new ConfigurationException("Failed to connect the VR-Engage Plugin interface socket connection.",
                        "There was an exception while trying to connect:\n"+e.getMessage(), e);
            }
        }

        super.setEnabled(value);
    }

    /**
     * An extension of {@link SocketHandler} capable of communicating with VR-Engage
     * using Protocol Buffer messages
     *
     * @author nroberts
     */
    private class ProtobufSocketHandler extends SocketHandler{

        public ProtobufSocketHandler(String address, int port) {
            super(address, port);
        }

        /**
         * Sends a command message to the VR-Engage GIFT Plugin
         *
         * @param command The command to send to the plugin
         * @param errorMsg - used to append error content too, if an error
         *        occurs
         * @param retryOnFail - if a response is needed (see response parameter)
         *        this flag is used to try to send the command again if it fails
         *        to receive a response the first time
         * @param readResponse - if true, the socket will wait for a response to
         *        the message (e.g. acknowledgement) and place the response in
         *        the string returned by this method. If false the message will
         *        be sent and a response will not be waited for.
         * @return The response received for the command sent if the response
         *         should be read based on the value of readResponse. Null if
         *         the response isn't read or there was an issue reading the
         *         response.
         * @throws IOException if there was a problem sending the command
         * @throws ConfigurationException if the socket has not been initialized
         *         or is not connected
         * @throws DetailedException if there was a problem reading the response
         *         to the command that was sent
         */
        public VrEngageMessage sendCommand(VrEngageMessage command, StringBuilder errorMsg, boolean retryOnFail, boolean readResponse) throws IOException, ConfigurationException, DetailedException {

            if(logger.isDebugEnabled()){
                logger.debug("Sending command of \n"+command);
            }

            if(socket == null) {
                throw new ConfigurationException("The socket hasn't been connected yet.", "Please call connect() before using the socket.", null);
            } else if(!socket.isConnected()){
                throw new ConfigurationException("The socket has been instantiated at some point but is no longer connected.", "There is most likely a logic error in the usage of the socket class which caused the socket to disconnect but not be set to null.  Currently there is no reconnect logic to handle this state.", null);
            }

            command.writeDelimitedTo(socket.getOutputStream());

            if(logger.isDebugEnabled()){
                logger.debug("Command sent successfully of \n"+command);
            }

            if(readResponse){

                try {
                    return VrEngageMessage.parseDelimitedFrom(socket.getInputStream());
                } catch (IOException e) {
                    if (retryOnFail) {
                        // try to send/read the command one more time

                        if (logger.isDebugEnabled()) {
                            logger.debug("Setting socket connection to null for connection to " + this
                                    + " because the following command could not be sent but retry on fail flag is set to tru:\n"
                                    + command);
                        }

                        /* reconnect first - this is an attempt at refreshing
                         * the connection to help resolve some socket issues
                         * that could be causing an issue here (hasn't been
                         * proven to help, yet) */
                        disconnect();
                        connect();

                        sendCommand(command, errorMsg, false, readResponse);
                    }

                    logger.error("IOException when waiting for a response from the VR-Engage Plugin", e);
                    throw new DetailedException("Failed to send a command to the external application.",
                            "The following command could not be sent to the address " + this + ":\n" + command, e);
                }
            }

            return null;
        }
    }
}
