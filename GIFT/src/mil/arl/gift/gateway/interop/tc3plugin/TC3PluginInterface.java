/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.tc3plugin;

import java.beans.ExceptionListener;
import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.ta.state.LoSResult;
import mil.arl.gift.common.ta.state.LoSResult.VisibilityResult;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonStateJSON;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.GatewayModuleUtils;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the TC3 interop interface responsible for communicating with TC3 and GIFT
 * when the TC3 application is started.
 * 
 * @author asanchez
 * 
 */
public class TC3PluginInterface extends AbstractInteropInterface 
{
	/**
     * This class listens for exceptions thrown by the message builder thread.
     *
     * @author asanchez
     *
     */
	private class MessageBuilderExceptionListener implements ExceptionListener
	{
		/** GIFT message tied to this exception. */
		private Message _Message = null;
		
		/**
         * Ties a GIFT message to the next exception thrown.
         * 
		 * @param message GIFT message tied to this exception
         */
		public void tieCurrentGIFTMessageToException( Message message )
		{
			_Message = message;
		}
		
		/**
         * Receives exceptions thrown by the message builder thread.
         * 
         * @param e - Exception thrown.
         */
		@Override
		public void exceptionThrown( Exception e ) 
		{
			SendErrorMessageToGIFT( _Message, e );
		}
	}
	
    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger( TC3PluginInterface.class );

    /** This is the AHK class name of the TC3Sim Application used to manipulate the window. */
    private static final String AHK_CLASS = "TC3Sim Trainer";

    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;    
    static
    {
        supportedMsgTypes = new ArrayList<MessageTypeEnum>();
        supportedMsgTypes.add( MessageTypeEnum.SIMAN );
        supportedMsgTypes.add( MessageTypeEnum.LOS_QUERY );
        supportedMsgTypes.add( MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST );
    }    
    
    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<TrainingApplicationEnum>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.TC3); 
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. VBS). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<MessageTypeEnum>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.STOP_FREEZE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.START_RESUME);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.LOS_RESULT);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.GENERIC_JSON_STATE);
    }

    /** GIFT session Id. */
    private Integer _GIFTSessionId = 0;
    
    /** Socket and socket address information of the GIFT TC3 interface. */
    private SocketAddress _SocketAddress = null;
    private ServerSocket _ServerSocket = null;
    private Socket _PluginSocket = null;
        
    /** max amount of time to wait for a response on the opened connection */
    private static final int RESPONSE_TIMEOUT = 5000;
    
    /** Message listener and builder that will run in its own thread as long as there's a connection with TC3. */
    private TC3MessageBuilder _TC3MessageBuilder = null;
    
    /** The message builder exception handler that receives exceptions when thrown from the listener thread. */
    private MessageBuilderExceptionListener _MessageBuilderExListener = null;
    
    /** Indicates if the plugin has successfully started the TC3 scenario. */
    private Boolean _TC3Started = false;
    
    /** Whether to try to reconnect to TC3 if an exception has occurred the first time it tried. */
    private Boolean _TryToReconnectToTC3 = true;
    
    /** Error string received from TC3. */
    private String _ErrorMessageFromTC3 = null;
    
    /**
     * MH: this is a hack due to the fact that we don't have the TC3 source.  Basically this is used
     * for LoS reply messages from TC3
     */
    private String lastKnownExperimentId = null;
    
    /** the configuration parameters for communicating with TC3 application */
    private generated.gateway.TC3 tc3PluginConfig;
    
    /**
     * Class constructor.
     * @param name - Name of the plugin.
     */
    public TC3PluginInterface( String name ) 
    {
        super( name, true );
    }

    @Override
    public boolean configure( Serializable config ) throws ConfigurationException
    {
        if ( config instanceof generated.gateway.TC3 ) 
        {
            tc3PluginConfig = (generated.gateway.TC3)config;
            _SocketAddress = new InetSocketAddress( tc3PluginConfig.getNetworkAddress(), tc3PluginConfig.getNetworkPort() );
            
            _GIFTSessionId = 1;
            
            if ( _SocketAddress == null )
            {
                logger.error( "Invalid socket address object when connecting to the TC3 Plugin." );
            }
            else
            {
                if ( _ServerSocket == null ) 
                {
                    try{
                        _ServerSocket = new ServerSocket( tc3PluginConfig.getNetworkPort() );
                        _ServerSocket.setSoTimeout(RESPONSE_TIMEOUT);
                    }catch(IOException e){
                        throw new ConfigurationException( "TC3 interface can't configure.",
                                "The TC3 interface was unable to connect to TC3 via the network port of "+tc3PluginConfig.getNetworkPort()+".",
                                e);
                    }
                }
            }
            
            _MessageBuilderExListener = new MessageBuilderExceptionListener();
          
            logger.info( "TC3 plugin has been configured." );
        } 
        else 
        {
            throw new ConfigurationException( "TC3 interface can't configure.",
                    "The TC3 interface uses "+generated.gateway.TC3.class+" interop input types and doesn't support using the interop config instance of " + config + ".",
                    null);
        }        
        
        return false;
    }
    
    /**
     * Handles messages that need to be sent to TC3.
     * 
     * @param message - Message to send to TC3.
     * @param errorMsg - Buffer to write error messages.
     * @return boolean - Whether this method sent a GIFT message in response to this query.
     */
    @Override
    public boolean handleGIFTMessage( Message message, StringBuilder errorMsg ) 
    {
        if ( message.getMessageType() == MessageTypeEnum.SIMAN ) 
        {
            DomainSessionMessage dsMsg = (DomainSessionMessage)message;
            _GIFTSessionId = dsMsg.getDomainSessionId();
            
            Siman siman = (Siman)message.getPayload();
        	
            if ( siman.getSimanTypeEnum() == SimanTypeEnum.LOAD ) 
            {  
                //
                // get appropriate configuration info for loading
                //
                generated.course.InteropInputs interopInputs = getLoadArgsByInteropImpl(this.getClass().getName(), siman.getLoadArgs());                
                generated.course.TC3InteropInputs inputs = (generated.course.TC3InteropInputs) interopInputs.getInteropInput();

                String scenarioName = inputs.getLoadArgs().getScenarioName();

                if ( scenarioName != null ) 
                {	
                    sendCommandToTC3( message, "GIFTMSG_HostMission \"ScenarioName\" \"" + scenarioName + "\"", errorMsg );
                    
                    if ( HoldThreadForReplyFromTC3( message, true ) )
                    {
                    	// Since there were no errors in launching the scenario, send a start message as sign of success.
                        StartResume sr = new StartResume( new Date().getTime(), 0, 0 );
                        GatewayModule.getInstance().sendMessageToGIFT( sr, MessageTypeEnum.START_RESUME, this );
                    }
                } 
                else
                {
                    GatewayModule.getInstance().sendReplyMessageToGIFT(
                        message,
                        new NACK( ErrorEnum.MALFORMED_DATA_ERROR, "The TC3 Interop connection could not load the scenario, no scenario name was defined." ),
                        MessageTypeEnum.PROCESSED_NACK,
                        this );
                }             
            } 
            else if ( siman.getSimanTypeEnum() == SimanTypeEnum.START ) 
            {
                sendCommandToTC3( message, "GIFTMSG_Start", errorMsg );
                
                _TC3Started = true;

                try{
                    GatewayModuleUtils.setAlwaysOnTop( AHK_CLASS, true );            
                    GatewayModuleUtils.giveFocus( AHK_CLASS );
                }catch(ConfigurationException e){
                    logger.error("Failed to bring the TC3 window to the foreground", e);
                }
            } 
            else if ( siman.getSimanTypeEnum() == SimanTypeEnum.PAUSE ) 
            {
                sendCommandToTC3( message, "GIFTMSG_Pause \"PauseMessage\" \"The Intelligent Tutoring System has paused this simulation. Please stand by...\" \"Timer\" \"-1\"", 
                        errorMsg );
            } 
            else if ( siman.getSimanTypeEnum() == SimanTypeEnum.RESUME ) 
            {
                sendCommandToTC3( message, "GIFTMSG_Resume", errorMsg );

                try{
                    GatewayModuleUtils.setAlwaysOnTop( AHK_CLASS, true );
                    GatewayModuleUtils.giveFocus( AHK_CLASS );
                }catch(ConfigurationException e){
                    logger.error("Failed to bring the TC3 window to the foreground", e);
                }
            } 
            else if ( siman.getSimanTypeEnum() == SimanTypeEnum.STOP ) 
            {
            	if ( _TC3Started )
            	{
            		sendCommandToTC3( message, "GIFTMSG_Stop", errorMsg );
            		_TC3Started = false;
            	}

            	try{
                    GatewayModuleUtils.setAlwaysOnTop( AHK_CLASS, false );
                    GatewayModuleUtils.minimizeWindow( AHK_CLASS );
            	}catch(ConfigurationException e){
                    logger.error("Failed to bring the TC3 window to NOT the foreground", e);
                }
            } 
            else if ( siman.getSimanTypeEnum() == SimanTypeEnum.RESTART ) 
            {
                logger.error( "Received unhandled GIFT restart message to send over to the TC3 plugin, " + message + "." );
            } 
            else
            {
                errorMsg.append( "TC3 plugin can't handle siman type of ").append(siman.getSimanTypeEnum()).append("." );
                logger.error( "Found unhandled Siman type of " + siman.getSimanTypeEnum() + "." );
            }
        }
        else if ( message.getMessageType() == MessageTypeEnum.LOS_QUERY ) 
        {
            return sendLosQuery( message, errorMsg );
        }
        else if ( message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST )
        {    
            String text = (String)message.getPayload();
            if ( text != null && !text.equals( "" ) )
            {
            	sendCommandToTC3( message, "GIFTMSG_Speak \"SoundName\" \"" + text + "\"", errorMsg );
            	sendCommandToTC3( message, "GIFTMSG_Text \"ChatText\" \"" + text + "\"", errorMsg);
            	
            	//if method returns false, then a NACK was sent by the method, otherwise no message was sent by the method
            	return !HoldThreadForReplyFromTC3( message, false );
            }
            else
            {
            	errorMsg.append( "Invalid sound Id of ").append(text).append(" received." );
                logger.error( "Invalid sound Id of " + text + " received." );
            }
        }
        else 
        {
            errorMsg.append( "TC3 plugin can't handle message of type ").append(message.getMessageType()).append("." );
            logger.error( "Received unhandled GIFT message to send over to the TC3 plugin, " + message + "." );
        }

        // Return false so that GIFT acknowledges the request.
        return false;
    }
    
    /**
     * Sends an exception thrown from a child thread to the exception handler.
     * 
     * @param e : The exception thrown.
     */
    public void SendExceptionToPlugin( Exception e )
    {
    	if ( _MessageBuilderExListener != null )
    	{
    		_MessageBuilderExListener.exceptionThrown( e );
    	}
    }
    
    /**
     * Gets a list of supported message types by this plugin.
     * 
     * @return List<MessageTypeEnum> : List containing the message types that this plugin can handle.
     */
    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() 
    {
        return supportedMsgTypes;
    }

    /**
     * Clean up any resources.
     */
    @Override
    public void cleanup() 
    {
    	_TryToReconnectToTC3 = true;
    	
    	if ( _TC3MessageBuilder != null )
        {
        	_TC3MessageBuilder.cleanup();
        	_TC3MessageBuilder = null;
        }
    	
        try
        {
            if ( _PluginSocket != null )
            {
                _PluginSocket.close();
                _PluginSocket = null;
            }
            
            if ( _ServerSocket != null )
            {
                _ServerSocket.close();
                _ServerSocket = null;
            }
        }
        catch ( IOException e )
        {
            logger.error( "IOException caught when trying to close the sockets.", e );
        }  
    }

    /**
     * Get information about this plugin.
     * 
     * @return String - Plugin information string.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "[TC3PluginInterface: " );
        sb.append( super.toString() );
        sb.append( ", address = ").append(_SocketAddress );
        
        sb.append( ", messageTypes = {" );
        for ( MessageTypeEnum mType : supportedMsgTypes )
        {
            sb.append( mType).append(", " );
        }
        sb.append( "}" );
        
        sb.append( "]" );
        
        return sb.toString();
    }
    
    /***************************
     *     PRIVATE METHODS     *
     ***************************/
    
    /**
     * Handles the sending of a script command to TC3.
     * 
     * @param message - Message received from GIFT to send to TC3.
     * @param command - The command to execute in TC3.
     * @param errorMsg - Buffer to write error messages.
     * @throws ConfigurationException - Unable to connect to TC3.
     */
    private void sendCommandToTC3( Message message, String command, StringBuilder errorMsg ) throws ConfigurationException
    {
    	// Any exception thrown by the message builder will be tied to this message.
    	_MessageBuilderExListener.tieCurrentGIFTMessageToException( message );
    	
    	try {
    	
    		sendCommand( command, errorMsg );
    	
    	} catch ( ConfigurationException e ) {
    		
    		SendErrorMessageToGIFT( message, e );
    		
    		// TC3 was unable to connect. Throw the exception to close the Gateway Module.
    		throw e;
    		
    	} catch (Exception e ) {
    		
    		SendErrorMessageToGIFT( message, e );
    	}
    }
    
    /**
     * Internal handles the sending of a command to TC3.
     * 
     * @param command - The command to execute in TC3.
     * @param errorMsg - Buffer to write error messages.
     * @throws IOException - Error when sending command to TC3.
     * @throws NullPointerException - Invalid command being sent to TC3.
     * @throws RuntimeException - Message listener error.
     * @throws ConfigurationException - Unable to connect to TC3.
     */
    private void sendCommand( String command, StringBuilder errorMsg ) throws RuntimeException, IOException, NullPointerException, ConfigurationException
    {
        try 
        {  
        	if ( command == null || command.length() <= 0 )
        	{
        		throw new NullPointerException( "A command sent to TC3 must not be null or empty." );
        	}
        	
            logger.debug( "Sending command: " + command + "." );
            
            if ( _ServerSocket == null )
            {
                throw new RuntimeException( "Invalid server socket when trying to send command: " + command + "." );
            }

            if ( _PluginSocket != null && !_PluginSocket.isConnected() ) 
            {
            	_TC3MessageBuilder.cleanup();
            	_TC3MessageBuilder = null;
                
                _PluginSocket.close();
                _PluginSocket = null;
            }

            if ( _PluginSocket == null ) 
            {
                // This methods hangs until a connection has been established.
                _PluginSocket = _ServerSocket.accept();
                
                // Start listening for incoming messages from TC3 to be sent to the domain module and then to the assessment engine.
                _TC3MessageBuilder = new TC3MessageBuilder( this, _PluginSocket );
                
                _TC3MessageBuilder.listenForMessages( new TC3MessageBuilder.TC3ReceivedMessagesCb()
                {
                    @Override
                    public void ReceivedReplyMessage( TC3ReplyState[] replyMessages )
                    {
                        // Receives the reply messages sent from TC3.
                    	handleReplyMessages( replyMessages );
                    }

                    @Override
                    public void ReceivedSimMessage( GenericJSONState[] gameStateMessages )
                    {
                        // Game state messages with the information ready to be sent to GIFT.
                    	handleGamestateMessage( gameStateMessages );
                    }
                } );

                try
                {
                	// Wait for the listener to check if all its initialization completed successfully.
                    synchronized( this )
                    {
                    	wait();
                    }
                } 
                catch ( InterruptedException e ) 
                {
               	 	throw new RuntimeException( e );
                }
                
                sendCommand( "GIFTMSG_ConnectionAccepted \"sessionid\" \"" + _GIFTSessionId + "\"", errorMsg );
            }          
            
            // Send the command to TC3.
        	DataOutputStream socketOutput = new DataOutputStream( _PluginSocket.getOutputStream() );
        	socketOutput.writeInt( command.length() );
        	socketOutput.write( command.getBytes() );

            logger.debug( "Command sent successfully." );
        } 
        catch ( IOException e ) 
        {    
        	if ( _TryToReconnectToTC3 )
        	{
        		_TryToReconnectToTC3 = false;
        		
        		if ( _PluginSocket != null ) 
                {
        			try
        			{
        				_PluginSocket.close();
        				_PluginSocket = null;
        			}
        			catch ( IOException e2 )
        			{
        				logger.error( "IOException caught when trying to close the plugin socket in the TC3 Plugin.", e2 );
        			}
                }
        		
        		if ( _TC3MessageBuilder != null )
    			{
    				_TC3MessageBuilder.cleanup();
    				_TC3MessageBuilder = null;
    			}
        		
        		// Try to reconnect to TC3.
        		sendCommand( command, errorMsg );
        	}
        	else
        	{
        		logger.error( "IOException caught when connecting to the TC3 Plugin.", e );
        		
        		throw new ConfigurationException( "The Gateway Module was unable to connect to TC3. "
        				+ "\nPlease make sure TC3 is at the main menu and then restart the course.\n\nIf TC3 is at the main menu you maybe having a firewall issue."
        		        + "TC3 is communicating with the GIFT Gateway module on "+tc3PluginConfig.getNetworkAddress()+":"+tc3PluginConfig.getNetworkPort()+".  Is that port open?",
                        "IOException caught when connecting to the TC3 Plugin: " + e.getMessage(),
                        e);
        	}
        }
    }
    
    /**
     * Sends error message to GIFT.
     * 
     * @param message - Message tied to the exception thrown.
     * @param e - The exception object that was thrown.
     */
    private void SendErrorMessageToGIFT( Message message, Exception e )
    {
    	if( _TC3MessageBuilder != null )
		{
			_TC3MessageBuilder.cleanup();
			_TC3MessageBuilder = null;
		}
    	
    	if(e != null && e instanceof DetailedException){
    	    
    	    DetailedException detailedException = (DetailedException) e;
    	    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, detailedException.getReason());
    	    nack.setErrorHelp(detailedException.getDetails());
            GatewayModule.getInstance().sendReplyMessageToGIFT(
                    message,
                    nack,
                    MessageTypeEnum.PROCESSED_NACK,
                    this );
    	}else{
    	
        	GatewayModule.getInstance().sendReplyMessageToGIFT(
                    message,
                    new NACK( ErrorEnum.OPERATION_FAILED, e != null ? e.getMessage() : ( "Unknown exception when sending " + message.toString() + "." ) ),
                    MessageTypeEnum.PROCESSED_NACK,
                    this );
    	}
    }
    
    /**
     * Sets the main plugin thread to wait until received reply message from TC3.
     * 
     * @param message - The message sent to TC3 from GIFT.
     * @param criticalMessage - Indicates whether this message is critical to GIFT to start the scenario.
     * @return Boolean - True if no errors were received from TC3.
     */
    private Boolean HoldThreadForReplyFromTC3( Message message, Boolean criticalMessage )
    {
    	if ( message != null )
    	{
        	 try
             {
             	// Wait for TC3 to send a reply back to GIFT.
                 synchronized( this )
                 {
                     wait();
                 }
             } 
             catch ( InterruptedException e ) 
             {
            	 _ErrorMessageFromTC3 = "Main thread was interrupted while waiting from a response from TC3.";
                 logger.error( _ErrorMessageFromTC3, e );
             }
             
             // Report the error received from TC3.
             if ( _ErrorMessageFromTC3 != null )
             {
            	 if ( criticalMessage )
            	 {
	                 GatewayModule.getInstance().sendReplyMessageToGIFT(
	                     message,
	                     new NACK( ErrorEnum.OPERATION_FAILED, _ErrorMessageFromTC3 ),
	                     MessageTypeEnum.PROCESSED_NACK,
	                     this );
            	 }
                 
                 _ErrorMessageFromTC3 = null;
                 
                 return false;
             }
    	}
    	
    	// Everything is O.K.
    	return true;
    }
    
    /**
     * Releases the plugin thread after received reply message from TC3.
     * 
     * @param replyMessage - Reply message to check errors from.
     */
    private void ReleaseThreadAfterReplyFromTC3( TC3ReplyState replyMessage )
    {
    	if ( replyMessage != null )
    	{
    		// Check for error messages inside the reply state.
			_ErrorMessageFromTC3 = replyMessage._Results.get( "errorMessage" );
			if ( _ErrorMessageFromTC3 != null )
			{
				logger.error( "Error message from TC3: " + _ErrorMessageFromTC3 );
				_ErrorMessageFromTC3 = "Error message from TC3: " + _ErrorMessageFromTC3;
			}
    	}
    	
		// If the main thread was set to wait for a response from TC3, then set it back to run
		// after received the reply from TC3.
	    synchronized( this )
	    {
	        notifyAll();
	    }
    }

    /**
     * Query TC3 via the GIFT TC3 interop interface on behalf of GIFT for LOS between player and a list of points in the environment.
     * 
     * @param message - Message holding the points to query from TC3.
     * @param String - Error message.
     * @return Boolean - True if no errors were received from TC3.
     */
    private Boolean sendLosQuery( Message message, StringBuilder errorMsg ) 
    {   
    	if ( message instanceof DomainSessionMessage  )
    	{	
	    	DomainSessionMessage losQueryMsg = (DomainSessionMessage)message;
	    	lastKnownExperimentId = losQueryMsg.getExperimentId();
	    	
	        if ( logger.isInfoEnabled() ) 
	        {            
	            logger.info( "TC3 plugin received LOS_QUERY." );          
	        }        
	                
	        LoSQuery losQuery = (LoSQuery)losQueryMsg.getPayload();    
	        List<Point3d> points = losQuery.getLocations();

	        for ( Point3d point : points ) 
	        {
	        	if ( logger.isInfoEnabled() ) 
	            {
	                logger.info( "Sending command GIFTMSG_QueryLOS on point " + point.toString() + "." );
	            }
	        	
	            // Once GIFT supports sending strings in the LosQuery message, change the QueryType to 'Entity'.
	            sendCommandToTC3( message, 
	            	"GIFTMSG_QueryLOS \"QueryType\" \"Points\" " +
	            	"\"PointX\" \"" 		+ point.getX() + 
	                "\" \"PointY\" \"" 		+ point.getY() + 
	                "\" \"PointZ\" \"" 		+ point.getZ() +
	                "\" \"UserId\" \"" 		+ losQueryMsg.getUserSession().getUserId() +
	                "\" \"DomainId\" \""	+ losQueryMsg.getDomainSessionId() + "\"",
	                errorMsg );
	            
	            //if method returns false, then a NACK was sent by the method, otherwise no message was sent by the method
	            return HoldThreadForReplyFromTC3( message, false );
	        }
	    }
    	else
    	{
    		logger.error( "Invalid message passed to the sendLosQuery method in the TC3 interop plugin." );
            errorMsg.append( "Invalid message passed to the sendLosQuery method in the TC3 interop plugin." );
    	}
    	
    	return false;
    }
    
    /**
     * Handles the LOS query results sent from TC3.
     * 
     * @param results - Map holding all the results for the LOS query returned from TC3.
     * @return Boolean - True if the LOS query message was handled.
     */
    private Boolean setLosQueryResults( Map<String, String> results )
    {
    	Boolean handled = false;
        if ( results != null ) 
        {
        	if ( logger.isInfoEnabled() ) 
	        {            
	            logger.info( "TC3 plugin received LOS_QUERY results from TC3." );          
	        } 
        	
        	Point3d pt = null;
        	String errMessage = null;
        	Boolean isVisible = false;
        	Double visibilityPercent = 0.0;

        	Map<String, List<VisibilityResult>> entityLoSResults = new HashMap<>();
        	
        	try
        	{
        		pt = new Point3d( Double.parseDouble( results.get( "PointX" ) ),
        				Double.parseDouble( results.get( "PointY" ) ),
        				Double.parseDouble( results.get( "PointZ" ) ) );
        	}
        	catch ( NumberFormatException e )
        	{
        		logger.error( "Error converting the LOS point from a string to a double.", e );
        		return handled;
        	}
            
        	errMessage = results.get( "errorMessage" );
        	if ( errMessage == null )
        	{
    			isVisible = Boolean.parseBoolean( results.get( "visible" ) );
        		if ( isVisible )
        		{
        			try
        			{
        				visibilityPercent = (double)Integer.parseInt( results.get( "visibilityResult" ) );
        			}
        			catch ( NumberFormatException e )
        			{
        				logger.error( "Error converting the LOS visibility result from a string to an integer.", e );
                		return handled;
        			}
        			
        			VisibilityResult visibilityResult = new VisibilityResult(0, visibilityPercent);
        			List<VisibilityResult> visibilityResults = new ArrayList<>();
        			visibilityResults.add(visibilityResult);
        			entityLoSResults.put( "NULL", visibilityResults );
        		}
        	}
        	else
        	{
        		logger.info( errMessage + "on point " + pt.toString() + "." );
        	}
 
            if ( !entityLoSResults.isEmpty() ) 
    	    {   
                Integer userId = -1;
                Integer domainId = -1;
                
                try
                {
                    userId = Integer.parseInt( results.get( "UserId" ) );
                    domainId = Integer.parseInt( results.get( "DomainId" ) );
                }
                catch ( NumberFormatException e )
                {
                    logger.error( "Error converting the user and domain IDs to integers when setting the LOS query results.", e );
                    return handled;
                }
            	
    	        LoSResult losResult = new LoSResult( entityLoSResults,  "UNABLE_TO_MAP_REQUEST_ID_TO_RESULT");
    	        GatewayModule.getInstance().sendMessageToGIFT( new UserSession(userId), domainId, lastKnownExperimentId, losResult, MessageTypeEnum.LOS_RESULT, this );
    	        
    	        handled = true;
    	    }
    	    else 
    	    {
    	        logger.error( "Failed to generate any results for LOS query." );
    	    } 
        }
        else 
        {
            logger.error( "Received null result from the GIFTMSG_QueryLOS command." );                    
        }

    	return handled;
    }
    
    /**
     * Callback for when the TC3 plugin has received a reply message from TC3.
     * 
     * @param replyMessages - Reply messages sent from TC3.
     */
    private void handleReplyMessages( TC3ReplyState[] replyMessages )
    {
    	if ( replyMessages != null && replyMessages.length > 0 )
    	{
    		for ( TC3ReplyState rs : replyMessages )
    		{
    			logger.debug( "Received a response from sending the command " + rs._CmdName + "." );

    			switch ( rs._CmdName )
    			{
    				case "GIFTMSG_ConnectionAccepted":
    					_TryToReconnectToTC3 = true;
    					break;
    					
    				case "GIFTMSG_HostMission":
    				case "GIFTMSG_Speak":
    					ReleaseThreadAfterReplyFromTC3( rs );
    					break;
    					
    				case "GIFTMSG_Stop":
    					_TryToReconnectToTC3 = true;
    					
    					// Send a stop message to SIMILE because GIFT sent a stop message to TC3.
    					String simId = rs._Results.get( "simulationId" );
    					Integer sessionId = -1;
    					try
    					{
    						sessionId = Integer.parseInt( rs._Results.get( "sessionId" ) );
    					}
    					catch ( NumberFormatException e )
    					{
    						logger.error( "Error when trying to convert the session Id to an integer.", e );
    					}
    					
    					if ( simId != null && simId.length() > 0 && sessionId != null && sessionId > 0 )
    					{
    						// Only send it to GIFT if the simulation is running.
    						if ( !simId.equals( "InvalidSimId" ) )
    						{
    							// Only send it to the domain module if we have a valid simulation ID.
    						    	GenericJSONState stopState = TC3MessageBuilder.ConstructTC3SimMessage( "Stop", simId, sessionId, null );
    							GatewayModule.getInstance().sendMessageToGIFT( stopState, MessageTypeEnum.GENERIC_JSON_STATE, this );
    						}
    					}
    					else
    					{
    						logger.error( "Invalid data when trying to construct the STOP command from the reply sent from TC3." );
    					}
    					
    					// This returns to the course selection screen in the GIFT browser.
    					StopFreeze sf = new StopFreeze( new Date().getTime(), 0, 0, 10000 );
    			        GatewayModule.getInstance().sendMessageToGIFT( sf, MessageTypeEnum.STOP_FREEZE, this );
    					break;
    			
    				case "GIFTMSG_QueryLOS":
    					setLosQueryResults( rs._Results );
    					ReleaseThreadAfterReplyFromTC3( rs );
    					break;
    					
    				default:
    	    			_ErrorMessageFromTC3 = rs._Results.get( "errorMessage" );
    					if ( _ErrorMessageFromTC3 != null )
    					{
    						logger.error( "Error message from TC3: " + _ErrorMessageFromTC3 );
    					}
    					break;
    			}
    		}
    	}
    }
    
    /**
     * Callback for when the TC3 plugin has received a simulation message from TC3 that has already been parsed and ready to send to GIFT.
     * 
     * @param gameStateMessage - Game state messages ready to be sent to GIFT.
     */
    private void handleGamestateMessage( GenericJSONState[] gameStateMessage )
    {

        if ( gameStateMessage != null && gameStateMessage.length > 0 )
        {
            for ( GenericJSONState gs : gameStateMessage )
            {
                // Send the newly constructed info from TC3 to GIFT.
                GatewayModule.getInstance().sendMessageToGIFT( gs, MessageTypeEnum.GENERIC_JSON_STATE, this );                
                if (gs.getStringById(CommonStateJSON.SIM_CMD).compareTo("Stop") == 0 )
            	{
            		// This returns to the course selection screen in the GIFT browser.
					StopFreeze sf = new StopFreeze( new Date().getTime(), 2, 0, 10000 );
			        GatewayModule.getInstance().sendMessageToGIFT( sf, MessageTypeEnum.STOP_FREEZE, this );
            	}
            }
        }
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
        
    }
    
    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }
}
