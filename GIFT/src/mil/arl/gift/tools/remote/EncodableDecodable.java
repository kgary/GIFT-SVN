/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import java.io.IOException;

/**
 * Interface that must be implemented by objects used by the remote launch
 * capability that will be sent over the network as messages.
 *
 * @author cragusa
 */
public interface EncodableDecodable {

    /**
     * Encodes the object's critical properties as text into the string buffer
     * provided using the delimiter provided. This method is public, but is
     * intended to only be called by the RemoteMessageUtil class.
     *
     * @param buffer the buffer into which the encoded object should be written.
     * @param delimeter character used to delimit the encoded fields.
     * @throws IOException if the encoding process fails.
     */
    void encode(StringBuffer buffer, char delimeter) throws IOException;

    /**
     * Decodes the object's critical properties from the provide string, using
     * the provided delimeter. This method is public, but is intended to only be
     * called by the RemoteMessageUtil class.
     *
     * @param string the string object containing properties of an encoded
     * object.
     * @param delimeter delimiter to use when decoding the object.
     * @throws IOException if the decoding process fails.
     */
    void decode(String string, char delimeter) throws IOException;

    /**
     * Gets the type of remote message this is
     *
     * @return RemoteMessageType The type of remote message this is
     */
    RemoteMessageType getMessageType();
}
