/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.util.StringUtils;

/**
 * A class that handles asynchronous communication with a {@link Socket}.
 *
 * @author tflowers
 *
 */
public class AsyncSocketHandler implements Closeable {

    /**
     * The functional interface that represents a handler for a message received
     * from the socket.
     *
     * @author tflowers
     *
     */
    @FunctionalInterface
    public interface IncomingDataHandler {
        /**
         * The method that is invoked when {@link String} data is received from
         * the socket.
         *
         * @param line The {@link String} that was received from the socket.
         */
        void onLineReceived(String line);
    }

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(AsyncSocketHandler.class);

    /** The {@link Socket} on which communication occurs. */
    private final Socket socket = new Socket();

    /** The object used to write data to the {@link #socket}. */
    private BufferedWriter socketWriter;

    /** The network address to which to connect. */
    private final String address;

    /** The network port to which to connect. */
    private final int port;

    /** The object that is used to process incoming data */
    private final IncomingDataHandler incomingHandler;

    /** The future that is responsible for polling the {@link #socket}. */
    private CompletableFuture<Void> pollingFuture = null;

    /**
     * The amount of time in milliseconds for which the polling future should
     * block while attempting to read from the socket.
     */
    private static final int POLLING_TIMEOUT = 5000;

    /**
     * Constructs a {@link AsyncSocketHandler} that connects to a specified
     * location and uses a provided {@link IncomingDataHandler} for received
     * messages.
     *
     * @param address The name of the host to which to connect. Can't be null or
     *        empty.
     * @param port The port to which to connect.
     * @param incomingHandler The {@link IncomingDataHandler} used to process
     *        data received from the {@link Socket}.
     */
    public AsyncSocketHandler(String address, int port, IncomingDataHandler incomingHandler) {
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("The parameter 'address' cannot be null.");
        } else if (incomingHandler == null) {
            throw new IllegalArgumentException("The parameter 'incomingHandler' cannot be null.");
        }

        this.address = address;
        this.port = port;
        this.incomingHandler = incomingHandler;
    }

    /**
     * Establishes a connection via a {@link Socket} based on the currently
     * configured network information.
     *
     * @throws IOException if there was a problem establishing a {@link Socket}
     *         connection.
     */
    public void connect() throws IOException {
        /* Connect the socket to the specified endpoint. */
        final InetSocketAddress socketAddr = new InetSocketAddress(address, port);
        socket.connect(socketAddr);

        /* Set the timeout for the socket. */
        try {
            socket.setSoTimeout(POLLING_TIMEOUT);
        } catch (SocketException sockEx) {
            logger.error("There was a problem setting the timeout for the socket at " + socketAddr + ".", sockEx);
            throw sockEx;
        }

        /* Create the writer around the socket. */
        try {
            final OutputStream outputStream = socket.getOutputStream();
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            socketWriter = new BufferedWriter(outputStreamWriter);
        } catch (IOException ioEx) {
            socket.close();
            throw ioEx;
        }

        /* Start the thread which will listen for messages on the socket. */
        pollingFuture = CompletableFuture.runAsync(() -> {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                while (socket.isConnected()) {
                    try {
                        final String line = reader.readLine();
                        incomingHandler.onLineReceived(line);
                    } catch (@SuppressWarnings("unused") SocketTimeoutException sockTimeEx) {
                        Thread.yield();
                    }
                }
            } catch (IOException ioEx) {
                logger.error("There was a problem reading from the socket connected at " + socketAddr + ".", ioEx);
            }
        });
    }

    /**
     * Determines whether or not the socket is connected to its remote endpoint.
     *
     * @return true if the socket is connected to the the remote endpoint, false
     *         otherwise.
     */
    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * Sends data through the established {@link Socket} connection.
     *
     * @param message The data to send through the {@link Socket}.
     * @throws IOException if there was a problem sending the message through
     *         the socket.
     */
    public void sendMessage(String message) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("sendMessage('" + message + "')");
        }

        if (socketWriter == null) {
            throw new IllegalStateException(
                    "Unable to send a message if the 'connect' method has not yet been called.");
        }

        socketWriter.write(message + "\n");
        socketWriter.flush();
    }

    /**
     * Severs the connection from the remote endpoint.
     * Any thread currently blocked in an I/O operation upon this socket will throw a SocketException. 
     * Once a socket has been closed, it is not available for further networking use (i.e. can't be reconnected or rebound). 
     * A new socket needs to be created. 
     *
     * @throws IOException if there was a problem while attempting to sever the
     *         connection.
     */
    public void disconnect() throws IOException {
        if (socket.isConnected()) {
            socket.close();
        }
    }

    @Override
    public void close() throws IOException {
        disconnect();
        if (pollingFuture != null) {
            pollingFuture.cancel(false);
        }
    }
}
