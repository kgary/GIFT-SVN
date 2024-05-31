/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

/**
 * An extension of {@link ProgressIndicator} that also provides an optional generic data payload. This payload can be used to
 * pass the results of an operation or to provide additional information about the status of the operation.
 * 
 * @author nroberts
 *
 * @param <T> the type of data payload to be included with this status. Payloads can be used to pass additional state information, such as
 * the results of a completed operation. If no payload is required, {@link Void} can be used instead of a payload data type.
 */
public class LoadedProgressIndicator<T> extends ProgressIndicator {
	
    private static final long serialVersionUID = 1L;
    
    /** A payload to be updated by the operation. This can either be the results of the operation or additional state information */
	private T payload;
	
	/**
	 * Creates a new empty status with no progress or completion
	 */
	public LoadedProgressIndicator(){
		super();
	}

	/**
	 * Gets the payload for this progress indicator. This can be used to get the results of an operation.
	 * 
	 * @return the payload
	 */
	public T getPayload() {
		return payload;
	}

	/**
	 * Sets the payload for this progress indicator. This can be used to pass the results of an operation.
	 * 
	 * @param payload the payload to set
	 */
	public void setPayload(T payload) {
		this.payload = payload;
	}
	
	@Override
	public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[LoadedProgressIndicator: ");
	    sb.append(super.toString());
	    sb.append(", payload = ").append(payload);
	    return sb.toString();
	}
}
