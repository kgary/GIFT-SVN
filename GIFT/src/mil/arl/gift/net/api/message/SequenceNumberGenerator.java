/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Sequence Number Generator is responsible for creating unique
 * sequence numbers for an instance of this class.
 * 
 * @author mhoffman
 *
 */
public class SequenceNumberGenerator {

    /** the current sequence number  */
    private static final AtomicInteger currSeqNumber = new AtomicInteger(0);

    /**
     * Return the next sequence number
     * 
     * @return int - the next sequence number
     */
    public static int nextSeqNumber(){
        return currSeqNumber.getAndIncrement();
    }
}
