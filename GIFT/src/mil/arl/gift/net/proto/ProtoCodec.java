/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.proto;

/**
 * This is the class that all classes that will be converting to/from protobuf
 * messages will implement.
 *
 * @author cpolynice
 */
public interface ProtoCodec<P, C> {

    /**
     * Converts data from protobuf message to common object class.
     * 
     * @param protoObject
     *            - the populated protobuf message
     * @return the common object that is created from the message. Will be
     *         determined at runtime.
     */
    C convert(P protoObject);

    /**
     * Maps data from common object to protobuf message.
     * 
     * @param commonObject
     *            - the populated class object
     * @return the protobuf message created from the class object. Specific type
     *         will be determined at runtime.
     */
    P map(C commonObject);
}
