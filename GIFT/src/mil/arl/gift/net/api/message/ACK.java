/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

/**
 * This class represents an ACK (Acknowledged) message. An ACK is a positive
 * response to another network message and is usually caused by successfully receiving,
 * applying and/or overall completion of logic associated with the received message.
 * 
 * @author mhoffman
 *
 */
public class ACK{

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ACK: ");
        sb.append("]");

        return sb.toString();
    }
}
