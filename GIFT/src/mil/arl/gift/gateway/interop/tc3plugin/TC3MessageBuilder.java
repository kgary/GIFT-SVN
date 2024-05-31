/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.tc3plugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonStateJSON;

import org.apache.commons.lang.mutable.MutableInt;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will listen to messages sent to the port in which TC3 will send
 * packets in. It also converts the string messages and converts them to classes
 * that GIFT and the TC3 interop plugin can understand.
 * 
 * @author asanchez
 * 
 */
public class TC3MessageBuilder {
    /**
     * Callback methods for when messages have been received from TC3.
     * 
     * @author asanchez
     * 
     */
    public interface TC3ReceivedMessagesCb {
	/**
	 * Receives the reply messages from TC3.
	 * 
	 * @param replyMessages
	 *            - An array of reply messages ready to be processed by
	 *            GIFT.
	 */
	public void ReceivedReplyMessage(TC3ReplyState[] replyMessages);

	/**
	 * Receives the game state messages from TC3.
	 * 
	 * @param gameStateMessages
	 *            - An array of TC3 game state messages ready to be sent to
	 *            GIFT.
	 */
	public void ReceivedSimMessage(GenericJSONState[] gameStateMessages);
    }

    /**
     * Helper class that holds the arrays of all the messages from TC3.
     * 
     * @author asanchez
     * 
     */
    private class MessagesHolder {
	/** Holds all the reply messages received. */
	public TC3ReplyState[] _ReplyMessages = null;

	/** Holds all the simulation messages received. */
	public GenericJSONState[] _StatesMessages = null;
    }

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory
	    .getLogger(TC3MessageBuilder.class);

    /** The owner of this listener. */
    TC3PluginInterface _TC3Interface = null;

    /**
     * Time for the listening thread to sleep after it has read something from
     * the input stream.
     */
    private static final Integer THREAD_SLEEP_TIME = 100;

    /** The starting unique ID for the messages. */
    private static final Integer ID_GENERATOR_START = 10000;

    /** Thread in which the listener will run. */
    private Thread _PacketListenerThread = null;

    /** Socket information. */
    private Socket _PluginSocket = null;

    /** True if listening to messages. */
    private Boolean _Listening;

    /**
     * The ID generator to give to the TC3 game states messages to be sent to
     * GIFT.
     */
    private static Integer _IDGenerator = -1;

    /**
     * Class constructor.
     * 
     * @param owner
     *            - The calling object of the message builder.
     * @param pluginSocket
     *            - Socket to listen to.
     */
    public TC3MessageBuilder(TC3PluginInterface owner, Socket pluginSocket) {
	logger.info("TC3MessageBuilder has been created");
	this._TC3Interface = owner;
	this._PluginSocket = pluginSocket;
	this._Listening = false;

	TC3MessageBuilder._IDGenerator = ID_GENERATOR_START;
    }

    /**
     * Listens for messages sent from TC3.
     * 
     * @param receivedMessageCb
     *            - Callback interface for when a message or messages have been
     *            received from TC3.
     * @throws RuntimeException
     *             - Error when reading the messages sent from TC3.
     */
    public void listenForMessages(final TC3ReceivedMessagesCb receivedMessageCb)
	    throws RuntimeException {
	// Catches exceptions from the message builder thread and notifies the
	// main thread about the exception thrown.
	UncaughtExceptionHandler exhandler = new UncaughtExceptionHandler() {
	    /**
	     * Handles uncaught exceptions from the child thread.
	     * 
	     * @param thread
	     *            - The thread the exception occurred on.
	     * @param e
	     *            - The exception object.
	     */
	    @Override
	    public void uncaughtException(Thread thread, Throwable e) {
		// Ready to start listening so notify the main thread so it can
		// continue.
		synchronized (_TC3Interface) {
		    if (e instanceof Exception) {
			_TC3Interface.SendExceptionToPlugin((Exception) e);
		    }

		    _TC3Interface.notifyAll();
		}
	    }
	};

	_PacketListenerThread = new Thread(
		"TC3 Message Builder Packet Listener") {
	    @Override
	    public void run() {
		logger.info("Packet listener has started running.");

		if (_PluginSocket == null) {
		    throw new RuntimeException(
			    "Invalid plugin socket in the TC3MessageBuilder class.");
		}

		if (_PluginSocket.isClosed()) {
		    throw new RuntimeException(
			    "The plugin socket is closed in the TC3MessageBuilder class.");
		}

		ByteBuffer response = ByteBuffer.allocate(8192);
		byte[] bytesFromTC3 = new byte[response.capacity()];

		MutableInt totalBytes = new MutableInt(0);
		BufferedInputStream socketInput = null;

		try {
		    socketInput = new BufferedInputStream(
			    _PluginSocket.getInputStream());
		} catch (IOException e) {
		    logger.error(
			    "IOException when creating the buffered reader.", e);
		    throw new RuntimeException(e);
		}

		_Listening = true;

		// Ready to start listening so notify the main thread so it can
		// continue.
		synchronized (_TC3Interface) {
		    _TC3Interface.notifyAll();
		}

		while (true) {
		    // If this switch turns to false, then it must mean we are
		    // trying to exit this thread.
		    if (!_Listening) {
			break;
		    }

		    try {

			// Check if there's something in the input stream.
			if (socketInput.available() > 0) {
			    // This method hangs until it has received
			    // something.
			    int bytesRead = socketInput.read(bytesFromTC3);

			    // Maybe TC3 has disconnected from GIFT.
			    if (bytesRead == -1) {
				break;
			    }

			    try {
				response.put(bytesFromTC3);
			    } catch (BufferOverflowException e) {
				logger.error(
					"The byte buffer has been overflown. Let's try increasing the buffer size.",
					e);
				throw e;
			    }

			    // Tackle on the bytes that have just been read to
			    // any other remaining bytes from previous reads.
			    totalBytes.add(bytesRead);

			    try {
				// Categorize and organize the messages
				// received.
				String[] messageData = getMessageDataFromBuffer(
					response, totalBytes);

				// Gets all the reply messages and simulation
				// messages. We will need to handle this
				// accordingly based
				// on the type of message.
				MessagesHolder messages = BuildTC3Messages(messageData);

				// Send the reply messages back to the tc3
				// interop plugin. The plugin should handle the
				// reply messages
				// as it sees fit.
				receivedMessageCb
					.ReceivedReplyMessage(messages._ReplyMessages);

				// Send the state messages to the gateway plugin
				// which will then send the messages to GIFT.
				receivedMessageCb
					.ReceivedSimMessage(messages._StatesMessages);
			    } catch (RuntimeException e) {
				logger.error(
					"Error when parsing and/or converting the messages sent from TC3.",
					e);
				throw e;
			    }
			}
		    } catch (IOException e) {
			// If the read method threw this exception and the
			// plugin has been
			// closed, then we can just exit this loop gracefully.
			if (_PluginSocket == null || _PluginSocket.isClosed()) {
			    break;
			}

			logger.error(
				"IOException when receiving a message from TC3.",
				e);
			break;
		    }

		    try {
			Thread.sleep(THREAD_SLEEP_TIME);
		    } catch (InterruptedException e) {
			logger.error(
				"Packet listener thread has been interrupted.",
				e);
			break;
		    }
		}

		logger.info("Packet listener thread is stopping.");

		try {
		    socketInput.close();
		    socketInput = null;
		} catch (IOException e) {
		    logger.error("IOException when closing the input socket.",
			    e);
		    throw new RuntimeException(e);
		}
	    }
	};

	_PacketListenerThread.setUncaughtExceptionHandler(exhandler);
	_PacketListenerThread.start();
    }

    /**
     * Stops listening for messages being sent from TC3.
     */
    public void stopListeningForMessages() {
	_Listening = false;

	logger.info("Stopping packet listener thread.");

	try {
	    if (_PacketListenerThread != null
		    && _PacketListenerThread.isAlive()) {
		_PacketListenerThread.join();
		_PacketListenerThread = null;
	    }
	} catch (InterruptedException e) {
	    logger.error(
		    "InterruptedException caught when trying to stop the TC3 packet listener from listening.",
		    e);
	}
    }

    /**
     * Is the listener currently listening?
     * 
     * @return Boolean - Whether this listener is currently listening.
     */
    public Boolean isListeningForMessages() {
	return _Listening;
    }

    /**
     * Clean up any resources.
     */
    public void cleanup() {
	stopListeningForMessages();
    }

    /***************************
     * STATIC METHODS *
     ***************************/

    /**
     * Builds a a TC3ReplyState message object ready to be handled by the TC3
     * interop plugin.
     * 
     * @param replyCmd
     *            - The command that was originally sent to TC3.
     * @param replyData
     *            - The data map to be sent along with the message.
     * @return TC3GameState - The TC3 reply message ready to be sent to the
     *         plugin..
     */
    public static TC3ReplyState ConstructTC3ReplyMessage(String replyCmd,
	    Map<String, String> replyData) {
	if (replyCmd != null && replyCmd.length() > 0) {
	    TC3ReplyState rs = new TC3ReplyState(_IDGenerator++, replyCmd);
	    if (replyData != null) {
		rs._Results.putAll(replyData);
	    }

	    // Lots of messages over a long period of time from TC3 to ever get
	    // this far.
	    if (_IDGenerator == Integer.MAX_VALUE) {
		_IDGenerator = ID_GENERATOR_START;
	    }

	    return rs;
	}

	return null;
    }

    /**
     * Builds a a TC3GameState message object ready to be sent to GIFT.
     * 
     * @param simCmd
     *            - The command that will need to be sent to SIMILE.
     * @param simId
     *            - The current simulation ID that SIMILE needs to identify
     *            which domain it is running in.
     * @param sessionId
     *            - The current GIFT session ID.
     * @param tc3Data
     *            - The data map to be sent along with the message.
     *            Can be empty but not null.
     * @return TC3GameState - The TC3 simulation message ready to be sent to
     *         GIFT.
     */
    @SuppressWarnings("unchecked")
    public static GenericJSONState ConstructTC3SimMessage(String simCmd,
	    String simId, Integer sessionId, Map<String, Object> tc3Data) {
        
        GenericJSONState tc3State = new GenericJSONState();
        
        try{
            if (simId != null && simId.length() > 0 && sessionId != null
                    && sessionId > 0) {
                
                tc3State.setValueById(CommonStateJSON.SIM_CMD, simCmd);
                tc3State.setValueById(CommonStateJSON.TC3_SimulationId_key, simId);
                tc3State.setValueById(CommonStateJSON.TC3_SessionId_key, sessionId);
                JSONArray tc3DataArray = new JSONArray();
                for (Map.Entry<String, Object> entry : tc3Data.entrySet()) {
                    JSONObject newObject = new JSONObject();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if(key != null && value != null) {
                        newObject.put(entry.getKey(), entry.getValue());
                        tc3DataArray.add(newObject);
                    }else {
                        logger.error("Null key or value was found in ConstructTC3SimMessage with simCommand: " + simCmd);
                    }
                }
                tc3State.addArraybyId("data", tc3DataArray);
            }
        }catch(NullPointerException e){ 
            logger.error("Caught an exception while trying to decode a TC3 message with simCommand = '" + simCmd+"'.", e); 
        } 

        return tc3State;
    }

    /***************************
     * PRIVATE METHODS *
     ***************************/

    /**
     * Checks the byte buffer sent from TC3 and breaks it up into strings that
     * need to be converted to game or reply states.
     * 
     * @param inResponse
     *            - Message buffer from TC3 that needs parsing.
     * @param inTotalBytesRead
     *            - The total bytes read from TC3.
     * @return String[] - Messages array ready to be broken up into TC3GameState
     *         messages.
     * @throws RuntimeException
     *             - Invalid byte buffer and/or bytes to read. Throwing the
     *             exception because this could potentially mess with the state
     *             information being sent from the gateway to the domain module.
     */
    private String[] getMessageDataFromBuffer(ByteBuffer inResponse,
	    MutableInt inTotalBytesRead) throws RuntimeException {
	if (inResponse == null) {
	    logger.error("Incoming message in the parseIncomingMessage method is null.");
	    throw new NullPointerException("The byte buffer received is null.");
	}

	// Set the position of the buffer at the beginning.
	inResponse.flip();

	if (inTotalBytesRead == null || inTotalBytesRead.intValue() <= 0) {
	    logger.error("The number of bytes read object is null or is less than 0 in the parseIncomingMessage method.");
	    throw new IllegalArgumentException(
		    "The bytes to read must be a valid integer and greater than 0.");
	}

	if (inResponse.limit() <= 0) {
	    if (inTotalBytesRead.intValue() <= 0) {
		// Nothing to parse.
		return null;
	    } else {
		logger.error("Incoming message in the parseIncomingMessage method is empty.");
		throw new IllegalStateException(
			"Either the byte buffer received is empty or the state of the internal markers is bad.");
	    }
	}

	List<String> messageList = new ArrayList<String>();

	// Check that we have all the complete packets.
	while (inTotalBytesRead.intValue() > 0) {
	    // If less than 4 bytes remaining, then just return what we have
	    // until next time.
	    if (inTotalBytesRead.intValue() < 4) {
		break;
	    }

	    Integer messageSize = null;

	    // Extract the header from the buffer.
	    byte[] headerData = new byte[4];

	    try {
		// Mark the beginning of the 4-byte header in case we need to go
		// back to it if the message size is larger than the total
		// amount of bytes we have remaining.
		inResponse.mark();
		inResponse.get(headerData, 0, 4);
	    } catch (@SuppressWarnings("unused") BufferUnderflowException e) {
		// Not enough bytes left to read the size of the next header, so
		// let's just return what we have.
		break;
	    }

	    try {
		// System uses big endian as default.
		messageSize = ByteBuffer.wrap(headerData).asIntBuffer().get();
	    } catch (BufferUnderflowException e) {
		logger.error("Error converting the bytes array to an integer.",
			e);
		return null;
	    }

	    // Invalid message header.
	    if (messageSize == null || messageSize == -1) {
		// Restore this message's 4-byte header to the buffer.
		inResponse.reset();

		logger.error("Error converting the bytes array to an integer.");
		throw new IllegalStateException(
			"The message size parsed from the message is invalid.");
	    }

	    // We have an incomplete or partial packet.
	    if (messageSize > (inTotalBytesRead.intValue() - 4)) {
		// Restore this message's 4-byte header to the buffer.
		inResponse.reset();
		break;
	    }

	    try {
		// Get the data string and append to the list.
		byte[] buf = new byte[messageSize];
		inResponse.get(buf, 0, messageSize);

		messageList.add(new String(buf));
	    } catch (IndexOutOfBoundsException e) {
		logger.error(
			"Error obtaining the message data from the byte array.",
			e);
		return null;
	    }

	    // Remove the bytes from this message.
	    inTotalBytesRead.subtract(messageSize + 4);
	}

	// Don't need the data that has already been checked.
	if (inTotalBytesRead.intValue() <= 0) {
	    inResponse.clear();
	} else {
	    // Restore this message's 4-byte header to the buffer.
	    inResponse.reset();

	    // Move the data remaining to the beginning of the buffer.
	    inResponse.compact();
	}

	// All the message data strings ready to be parsed.
	return (!messageList.isEmpty() ? messageList
		.toArray(new String[messageList.size()]) : null);
    }

    /**
     * Builds the TC3 reply and simulation states that GIFT uses.
     * 
     * @param inMessages
     *            - Messages array to parse into either reply messages or
     *            simulation messages.
     * @return MessagesHolder - A message holder holding all the reply messages
     *         and the simulation messages ready to be handle by GIFT.
     * @throws IllegalArgumentException
     *             - Messages array must not be null.
     * @throws RuntimeException
     *             - Error occurred while constructing the TC3 state messages.
     */
    private MessagesHolder BuildTC3Messages(String[] inMessages) throws IllegalArgumentException, RuntimeException {
        
        if (inMessages == null) {
            throw new IllegalArgumentException("The messages array must not be null.");
        }

        // Temporary holders for the data to send to GIFT.
        List<TC3ReplyState> replyMessages = new ArrayList<TC3ReplyState>();
        List<GenericJSONState> gameStates = new ArrayList<GenericJSONState>();

        // String parsing variables.
        String[] delimitedMessage = null;
        Integer curIdx = 0;

        // Reply messages variables.
        String curReplyCmd = null;
        Boolean replyMsg = false;
        Map<String, String> replyData = new HashMap<String, String>();

        // Game state variables.
        String simId = null;
        String simCmd = null;
        Integer sessionId = -1;
        Map<String, Object> simData = new HashMap<String, Object>();

        for (String message : inMessages) {
            delimitedMessage = message.split(",");
            curIdx = 0;

            curReplyCmd = null;
            replyMsg = false;
            replyData.clear();

            simId = null;
            sessionId = -1;
            simData.clear();

            if (delimitedMessage != null && delimitedMessage.length > 0) {
                
                while (curIdx < delimitedMessage.length) {
                    
                    if (delimitedMessage[curIdx].startsWith("GIFTREPLY")) {
                        replyMsg = true;
                    } else if (delimitedMessage[curIdx].startsWith("GIFTMSG")) {
                        // Simulation messages.
                        simCmd = delimitedMessage[curIdx].substring(8);
                    } else {
                        // Reply and simulation message data parsing.
                        if (replyMsg) {
                            
                            if (delimitedMessage[curIdx].equals("event")) {
                                curReplyCmd = delimitedMessage[++curIdx];
                            } else {
                                replyData.put(delimitedMessage[curIdx], delimitedMessage[++curIdx]);
                            }
                        } else {
                            if (simCmd != null) {
                                
                                if (delimitedMessage[curIdx].equals("simulationId")) {
                                        simId = delimitedMessage[++curIdx];
                                } else if (delimitedMessage[curIdx].equals("sessionId")) {
                                    try {
                                        sessionId = Integer.parseInt(delimitedMessage[++curIdx]);
                                    } catch (NumberFormatException e) {
                                        logger.error("Converting the GIFT session ID to the Integer format failed.",e);
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    simData.put(delimitedMessage[curIdx],delimitedMessage[++curIdx]);
                                }
                            }
                        }
                    }

                    curIdx++;
                }

                if (replyMsg) {
                    // Check for a valid state after parsing all the reply state
                    // data in the string and construct a TC3ReplyState object
                    // to
                    // be handled by the TC3 gateway plugin.
                    if (curReplyCmd != null && curReplyCmd.length() > 0) {
                        TC3ReplyState curReplyState = ConstructTC3ReplyMessage(curReplyCmd, replyData);
                        if (curReplyState != null) {
                            replyMessages.add(curReplyState);
                        }
                        
                    } else {
                        logger.error("Reply state is missing information. Please check below for the reply state debug information.");
                        logger.error("Message: " + message);
                        logger.error("Reply Command: "+ ((curReplyCmd != null) ? curReplyCmd : ""));

                        for (Entry<String, String> _curEntry : replyData.entrySet()) {
                            logger.error("Reply Data: " + _curEntry.getKey()+ " " + _curEntry.getValue());
                        }

                        throw new RuntimeException(
                                "A replay state has missing or invalid information so we could not construct the TC3ReplyState."
                                    + "Check the log for more information about the message.");
                    }
                } else {
                    // Check for a valid state after parsing all the game state
                    // data in the string and construct a TC3GameState object to
                    // send
                    // to GIFT.
                    if (simCmd != null && simId != null && simId.length() > 0
                        && sessionId != null && sessionId > 0) {
                        
                        GenericJSONState curSimState = ConstructTC3SimMessage(
                            simCmd, simId, sessionId, simData);
                        gameStates.add(curSimState);
                    } else {
                        logger.error("Game state is missing information. Please check below for the game state debug information.");
                        logger.error("Message: " + message);
                        logger.error("TC3 Command: "+ ((simCmd != null) ? simCmd : ""));
                        logger.error("Simulation Id: " + ((simId != null) ? simId : ""));
                        logger.error("Session Id:" + " " + ((sessionId != null) ? sessionId.toString() : ""));

                        for (Entry<String, Object> _curEntry : simData.entrySet()) {
                            logger.error("Game Data: " + _curEntry.getKey()+ " " + _curEntry.getValue().toString());
                        }

                        throw new RuntimeException(
                            "A simulation state has missing or invalid information so we could not construct the TC3GameState."
                                + "Check the log for more information about the message.");
                    }
                }
            }
        }

        // Reply messages and game states to send to GIFT.
        MessagesHolder messages = new MessagesHolder();
        messages._ReplyMessages = ((!replyMessages.isEmpty()) ? replyMessages.toArray(new TC3ReplyState[replyMessages.size()]) : null);
        messages._StatesMessages = ((!gameStates.isEmpty()) ? gameStates.toArray(new GenericJSONState[gameStates.size()]) : null);

        return messages;
    }
}
