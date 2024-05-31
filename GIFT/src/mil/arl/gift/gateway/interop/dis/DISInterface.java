/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.dis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import org.jdis.PDUReader;
import org.jdis.PDUWriter;
import org.jdis.event.PDUListener;
import org.jdis.pdu.CollisionPDU;
import org.jdis.pdu.DetonationPDU;
import org.jdis.pdu.EntityStatePDU;
import org.jdis.pdu.FirePDU;
import org.jdis.pdu.PDU;
import org.jdis.pdu.StartResumePDU;
import org.jdis.pdu.StopFreezePDU;
import org.jdis.util.UnsignedByte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.Collision;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.AbstractInteropInterface.HandlesDisDialect;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.dis.DISTime;
import mil.arl.gift.net.dis.DISToGIFTConverter;
import mil.arl.gift.net.dis.DISToGIFTConverter.DISDialect;
import mil.arl.gift.net.util.Util;

/**
 * This is a DIS interop interface class responsible for reading and writing DIS PDUs.
 *
 * @author mhoffman
 *
 */
public class DISInterface extends AbstractInteropInterface implements HandlesDisDialect {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DISInterface.class);

    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;
    static{
        supportedMsgTypes = new ArrayList<>();
        supportedMsgTypes.add(MessageTypeEnum.DETONATION);
        supportedMsgTypes.add(MessageTypeEnum.ENTITY_STATE);
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
        supportedMsgTypes.add(MessageTypeEnum.WEAPON_FIRE);
    }

    /**
     * contains the training applications that this interop plugin was built for and should connect to
     * In this case DIS is a standard and not tied to a specific training application, therefore the list is
     * empty.  Currently applications likes VBS use this interop plugin but VBS is not required to use this plugin.
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<>();
    }

    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from
     * an external training applications (e.g. VBS).
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.STOP_FREEZE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.ENTITY_STATE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.WEAPON_FIRE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.DETONATION);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.START_RESUME);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.COLLISION);
    }

    /** the exercise id to filter DIS PDUs on */
    private UnsignedByte exerciseID;

    /** the site id used to create the DIS PDU simulation address record */
    private int siteID;

    /** the application id used to create the DIS PDU simulation address record */
    private int applicationID;

    private generated.gateway.DIS disConfig = null;

    /** responsible for reading pdu's from the network */
    private PDUReader pduReader;

    /** responsible for writing pdu's to the network */
    private PDUWriter pduWriter;

    /**
     * whether any PDUs sent by this interop plugin should also be received and handled
     * by this interop plugin.  This is useful for domain session log playback during
     * a course when you want external systems (e.g. ARES)
     * and GIFT logic (e.g. domain module assessments, game master UI) to use the messages as well.
     */
    private boolean loopBackPDUs = false;

    /**
     * This PDU Reader instance is used to simply filter out DIS PDUs that are
     * sent by this DIS interop plugin.  The JDIS library doesn't do this on its own.
     * A PDU is deemed sent by this DIS interop plugin if the packet received has the
     * following values:
     *    1) packet is from the machine running the JVM instance with this DIS interop (IP address)
     *    2) packet is received on the same port this DIS interop is sending on (send port is randomly selected
     *       from open ports at the time the PDU writer is initialized)
     *
     * @author mhoffman
     *
     */
    private class CustomPDUReader extends PDUReader{

		public CustomPDUReader(int port) throws IOException {
			super(new MulticastSocket(port));
		}
		
		@Override
		public void datagramPacketReceived(DatagramPacket packet){

			//filter packets sent by GIFT
			if(isLocalPacket(packet)){
				return;
			}

			super.datagramPacketReceived(packet);
		}
    }

    /**
     * Returns a boolean indicating if the specified DatagramPacket is from
     * the local node.
     *
     * @param datagramPacket the DatagramPacket to examine
     * @return  a boolean indicating if the specified DatagramPacket is from
     * the local node
     */
    protected boolean isLocalPacket(DatagramPacket datagramPacket) {

        return Util.isLocalAddress(datagramPacket.getAddress())
            && datagramPacket.getPort() == pduWriter.getDatagramSocket().getLocalPort();
    }

    /** the DIS PDU listener for this DIS plugin that will receive incoming PDU messages from a DIS network*/
    private PDUListener pduListener = new PDUListener(){

        @Override
        public void pduReceived(PDU pdu){
            notifiedPDUReceived(pdu);
        }
    };

    /** The dialect to use when translating between GIFT game states and the DIS standard */
    private DISDialect dialect = null;

    /**
     * Class constructor
     *
     * @param name - display name for this plugin
     */
    public DISInterface(String name){
        super(name, false);

    }

    /**
     * Class constructor
     *
     * @param name - display name for this plugin
     * @param requiresUsersDisplay if this interop plugin requires interacting or controlling
     * a window or application on the user's computer
     */
     protected DISInterface(String name, boolean requiresUsersDisplay){
        super(name, requiresUsersDisplay);

    }

    /**
     * Return the DIS site identifier value
     *
     * @return int
     */
    public int getSiteID() {
        return siteID;
    }

    /**
     * Return the DIS application identifier value
     *
     * @return int
     */
    public int getApplicationID() {
        return applicationID;
    }

    @Override
    public void setEnabled(boolean value) throws ConfigurationException{

        boolean isEnabledAlready = isEnabled();
        super.setEnabled(value);

        if(!isEnabledAlready && value && !isPlayback()){
            //prevent adding the listener more than once, as well as re-recreating the PDU reader

            if(pduReader == null || !pduReader.getDatagramSocket().isBound()){
                try {
                    if(logger.isInfoEnabled()){
                        logger.info("Re-connecting DIS interface on port "+disConfig.getReceivePort()+".");
                    }
                    pduReader = new CustomPDUReader(disConfig.getReceivePort());
                } catch (Exception e) {
                    logger.error("Caught exception while trying to establish the DIS connection to port "+disConfig.getReceivePort()+".", e);
                    throw new ConfigurationException("Unable to connect to the DIS port of "+disConfig.getReceivePort()+".",
                            e.getMessage(),
                            e);
                }
            }

            //listen for PDUs
            pduReader.addPDUListener(pduListener);

            if(logger.isInfoEnabled()){
                logger.info("Started listening for incoming DIS PDUs.");
            }

        }else if(!value){

            if(pduReader != null){
                //stop listening for PDUs
                pduReader.removePDUListener(pduListener);

                if(logger.isInfoEnabled()){
                    logger.info("Stopped listening for incoming DIS PDUs.");
                }

                //disconnect - only want to connect when this interface is enabled,
                //otherwise allow the port to be binded to another GIFT interface
                pduReader.stop();
                pduReader = null;
            }
        }

    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException{

        if(config instanceof generated.gateway.DIS){

            disConfig = (generated.gateway.DIS)config;

            try{
                //make sure the port is currently available
                pduReader = new CustomPDUReader(disConfig.getReceivePort());

                //only want to connect when this interface is enabled, otherwise allow the port to be binded to another GIFT interface
                //disconnect didn't relinquish the port
                pduReader.stop();
                pduReader = null;
                
            }catch(Exception e){
                throw new ConfigurationException("DIS interface can't configure PDU reader on port "+disConfig.getReceivePort()+".",
                        e.getMessage(),
                        e);
            }

            try{
//                pduWriter = new PDUWriter(new MulticastSocket(disConfig.getSendPort()), InetAddress.getByName(disConfig.getNetworkAddress()), disConfig.getSendPort());
                pduWriter = new PDUWriter(disConfig.getNetworkAddress(), String.valueOf(disConfig.getSendPort()));
            }catch(Exception e){
                throw new ConfigurationException("DIS interface can't configure PDU writer on port "+disConfig.getNetworkAddress()+":"+disConfig.getSendPort()+".",
                        e.getMessage(),
                        e);
            }

                siteID = disConfig.getSiteID().intValue();
                applicationID = disConfig.getApplicationID().intValue();
                exerciseID = new UnsignedByte(disConfig.getExerciseID());

                if(logger.isInfoEnabled()){
                    logger.info("DIS interface configured to receive on "+disConfig.getNetworkAddress()+":"+disConfig.getReceivePort()+" and send on "+disConfig.getNetworkAddress()+":"+disConfig.getSendPort());
                }

        }else{
            throw new ConfigurationException("DIS Plugin interface can't configure.",
                    "DIS plugin interface uses "+generated.gateway.DIS.class+" interop input and doesn't support using the interop config instance of " + config,
                    null);
        }


        return false;
    }

    /**
     * A PDU was received by this interop plugin.  Check to see if this interop plugin
     * should be handling incoming PDUs (the interop plugin is enabled) and the exercise ID
     * matches the exercise ID this interop plugin is configured to filter on.  Then handle
     * the PDU contents appropriate, e.g. send GIFT message to a GIFT module for that PDU.
     *
     * @param pdu the incoming PDU.  If null this method does nothing.
     */
    private void notifiedPDUReceived(PDU pdu){

        //make sure this plugin is enabled before attempting to deal with an incoming PDU
        if(!isEnabled()){
            return;
        }

        if(pdu == null){
            return;
        }

        try{

            //filter on exercise id
            if(pdu.getPDUHeader().getExerciseIdentifier().compareTo(exerciseID) != 0){
                return;
            }

            if(logger.isInfoEnabled()){
                logger.info("Received DIS pdu: "+pdu);
            }

            //DEBUG
            //Date rcv = new Date();

            handlePDU(pdu);

            //DEBUG
            //System.out.println("time = "+ (new Date().getTime() - rcv.getTime()));

        }catch(Exception e){
            logger.error("Caught exception while handling PDU of "+pdu+".", e);
        }
    }

    /**
     * Handle the PDU received from the network by translating it into a GIFT message and sending that new
     * message over the network.
     *
     * @param pdu the incoming DIS pdu to handle
     */
     protected void handlePDU(PDU pdu){

        if(pdu instanceof EntityStatePDU){
            handleEntityStatePDU((EntityStatePDU)pdu);
        }else if(pdu instanceof DetonationPDU){
            handleDetonationPDU((DetonationPDU)pdu);
        }else if(pdu instanceof FirePDU){
            handleWeaponFirePDU((FirePDU)pdu);
        }else if(pdu instanceof StopFreezePDU){
            handleStopFreezePDU((StopFreezePDU)pdu);
        }else if(pdu instanceof StartResumePDU){
            handleStartResumePDU((StartResumePDU)pdu);
        }else if(pdu instanceof CollisionPDU){
            handleCollisionPDU((CollisionPDU)pdu);
        }else{
            logger.error("received unhandled PDU of "+pdu);
        }
    }

     /**
      * Convert the Collision PDU into a GIFT Collision game state object and send a message over the GIFT network
      * with that new object.
      *
      * @param collisionPDU the incoming collision pdu to handle
      */
     protected void handleCollisionPDU(CollisionPDU collisionPDU){

         Collision collision = DISToGIFTConverter.createCollision(collisionPDU);
         GatewayModule.getInstance().sendMessageToGIFT(collision, MessageTypeEnum.COLLISION, this);
     }

    /**
     * Convert the Stop Freeze PDU into a GIFT Stop Freeze message and send the new message over the GIFT network.
     *
     * @param stopFreezePDU the incoming stopfreeze pdu to handle
     */
     protected void handleStopFreezePDU(StopFreezePDU stopFreezePDU){

         if(logger.isInfoEnabled()){
             logger.info("Received Stop Freeze PDU.");
         }

         long realWorldTime = 0;  //TODO: translate this time to epoch?
         int reason = stopFreezePDU.getReason().intValue();
         int frozenBehavior = stopFreezePDU.getFrozenBehavior().intValue();
         long requestID = stopFreezePDU.getRequestID().longValue();

         StopFreeze stopFreeze = new StopFreeze(realWorldTime, reason, frozenBehavior, requestID);
         GatewayModule.getInstance().sendMessageToGIFT(stopFreeze, MessageTypeEnum.STOP_FREEZE, this);
     }

     /**
      * Convert the Start Resume PDU into a GIFT Start Resume message and send the new message over the GIFT network.
      *
      * @param startResumePDU the incoming startresume pdu to handle
      */
      protected void handleStartResumePDU(StartResumePDU startResumePDU){

          if(logger.isInfoEnabled()){
              logger.info("Received Start Resume PDU.");
          }

          long realWorldTime = 0;  //TODO: translate this time to epoch?
          long simulationTime = 0; //TODO: translate this time to epoch?
          long requestID = startResumePDU.getRequestID().longValue();

          StartResume startResume = new StartResume(realWorldTime, simulationTime, requestID);
          GatewayModule.getInstance().sendMessageToGIFT(startResume, MessageTypeEnum.START_RESUME, this);
      }

    /**
     * Convert the Fire PDU into a GIFT weapon fire message and send the new message over the GIFT network.
     *
     * @param fire the incoming fire pdu to handle
     */
    protected void handleWeaponFirePDU(FirePDU fire){

        WeaponFire weaponFire = DISToGIFTConverter.createWeaponFire(fire);
        GatewayModule.getInstance().sendMessageToGIFT(weaponFire, MessageTypeEnum.WEAPON_FIRE, this);
    }

    /**
     * Convert the detonation PDU into a GIFT detonation message and send the new message over the GIFT network.
     *
     * @param det the incoming detonation pdu to handle
     */
    protected void handleDetonationPDU(DetonationPDU det){

        Detonation detonation = DISToGIFTConverter.createDetonation(det);
        GatewayModule.getInstance().sendMessageToGIFT(detonation, MessageTypeEnum.DETONATION, this);
    }

    /**
     * Convert the entity state PDU into a GIFT entity state message and send the new message over the GIFT network.
     *
     * @param es the incoming entity state pdu to handle
     */
    protected void handleEntityStatePDU(EntityStatePDU es){

        /* Route the entity state back onto GIFT's message bus */
        EntityState entityState = DISToGIFTConverter.createEntityState(es, dialect);
        GatewayModule.getInstance().sendMessageToGIFT(entityState, MessageTypeEnum.ENTITY_STATE, this);
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg){

        boolean repliedToByGIFTMsg = false;
        PDU pduToProcess = null;

        if(message.getMessageType() == MessageTypeEnum.START_RESUME){

            StartResume startResume = (StartResume)message.getPayload();
            StartResumePDU startResumePDU = DISToGIFTConverter.createStartResumePDU(startResume);
            pduToProcess = startResumePDU;

        }else if(message.getMessageType() == MessageTypeEnum.STOP_FREEZE){

            StopFreeze stopFreeze = (StopFreeze)message.getPayload();
            StopFreezePDU stopFreezePDU = DISToGIFTConverter.createStopFreezePDU(stopFreeze);
            pduToProcess = stopFreezePDU;

        }else if (message.getMessageType() == MessageTypeEnum.SIMAN) {

            Siman siman = (Siman) message.getPayload();

            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) {
                
                // Determine if GIFT is playing back a domain session log file during a course
                loopBackPDUs = siman.isPlaybackLoadArgs();

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.START) {

                StartResumePDU sr = new StartResumePDU();
                pduToProcess = sr;

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.PAUSE) {

                StopFreezePDU stopFreeze = new StopFreezePDU();
                stopFreeze.setReason(StopFreeze.RECESS);
                pduToProcess = stopFreeze;

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESUME) {

                StartResumePDU startResume = new StartResumePDU();
                pduToProcess = startResume;

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.STOP) {

                if (logger.isInfoEnabled()) {
                    logger.info("Sending stop freeze PDU over DIS network.");
                }

                StopFreezePDU stopFreeze = new StopFreezePDU();
                stopFreeze.setReason(StopFreeze.TERMINATION);
                pduToProcess = stopFreeze;

            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESTART) {

                // nothing to do

            } else {
                errorMsg.append("DIS plugin can't handle siman type of ").append(siman.getSimanTypeEnum());
                logger.error("Found unhandled Siman type of " + siman.getSimanTypeEnum());
            }

        }else if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            
            EntityState entityState = (EntityState)message.getPayload();
            EntityStatePDU entityStatePDU = DISToGIFTConverter.createEntityStatePDU(entityState, dialect);
            pduToProcess = entityStatePDU;

        }else if(message.getMessageType() == MessageTypeEnum.WEAPON_FIRE){

            WeaponFire weaponFire = (WeaponFire)message.getPayload();
            FirePDU firePDU = DISToGIFTConverter.createFirePDU(weaponFire);
            pduToProcess = firePDU;

        }else if(message.getMessageType() == MessageTypeEnum.DETONATION){

            Detonation detonation = (Detonation)message.getPayload();
            DetonationPDU detonationPDU = DISToGIFTConverter.createDetonationPDU(detonation);
            pduToProcess = detonationPDU;

        }else{
            logger.error("received unhandled GIFT message to send over the DIS network, "+message);
            errorMsg.append("DIS plugin can't handle message of type ").append(message.getMessageType());
        }
        
        if(pduToProcess != null) {
            
            if(loopBackPDUs && ModuleTypeEnum.DOMAIN_MODULE.equals(message.getSenderModuleType())){
                
                //if the domain module is playing back a game state message, act as if the PDU was received from an application
                handlePDU(pduToProcess);
                
            } else {
                sendPDU(pduToProcess); //otherwise, send the PDU out to any connected applications
            }
        }

        return repliedToByGIFTMsg;
    }

    /**
     * Send the PDU over the network
     *
     * @param pdu - the DIS message to send
     */
    public void sendPDU(PDU pdu){

        try{
            pdu.getPDUHeader().setExerciseIdentifier(exerciseID);
            pdu.getPDUHeader().setTimestamp(DISTime.convertUTCTimeToDISTimestamp(System.currentTimeMillis()));
            pduWriter.send(pdu);

        }catch(Exception e){
            logger.error("caught exception while trying to send pdu: "+pdu, e);
        }
    }

    @Override
    public void cleanup(){

        if(pduReader != null){
            pduReader.stop();
        }

        pduWriter.stop();
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }

    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations(){
        return REQ_TRAINING_APPS;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes(){
        return PRODUCED_MSG_TYPES;
    }

    @Override
    public Serializable getScenarios() {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() {
        return null;
    }

    @Override
    public void loadScenario(String scenarioIdentifier)
            throws DetailedException {
        //not supported
    }

    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }

    @Override
    public void selectObject(Serializable objectIdentifier)
            throws DetailedException {
        // not supported
    }

    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }
    
    @Override
    public void setDisDialect(DISDialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DISInterface: ");
        sb.append(super.toString());

        if(disConfig != null){
            sb.append(", receive port = ").append(disConfig.getReceivePort());
            sb.append(", send port = ").append(disConfig.getSendPort());
        }

        sb.append(", site = ").append(getSiteID());
        sb.append(", application = ").append(getApplicationID());
        sb.append(", exercise = ").append(exerciseID);

        sb.append(", messageTypes = {");
        for(MessageTypeEnum mType : supportedMsgTypes){
            sb.append(mType ).append(", ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }

}
