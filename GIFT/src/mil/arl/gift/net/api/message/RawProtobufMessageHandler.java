/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import java.util.concurrent.CompletableFuture;

import com.google.protobuf.AbstractMessage;

/**
 * Defines the method signature that {@link ProtobufMessageClient} uses to
 * handle incoming messages.
 *
 * @author tflowers
 *
 * @param <T> The type of {@link AbstractMessage} that is received and
 *        optionally sent.
 */
@FunctionalInterface
public interface RawProtobufMessageHandler<T extends AbstractMessage> {
    /**
     * Callback for when a message is received from the message broker.
     *
     * @param msg The message to be handled.
     * @return responder The object that is used to produce the response to the
     *         incoming message. Can be null or complete with null if no
     *         response is generated for the incoming message.
     */
    CompletableFuture<T> processMessage(T msg);
}
