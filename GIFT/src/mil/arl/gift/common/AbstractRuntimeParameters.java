/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;
import java.io.Serializable;

/**
 * The AbstractRuntimeParameters class allows for any runtime parameters which can be used to configure
 * the domain session when it is started. Any future runtime parameters that are created should extend from this class.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractRuntimeParameters implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public AbstractRuntimeParameters() {
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("");
        return sb.toString();
    }
}
