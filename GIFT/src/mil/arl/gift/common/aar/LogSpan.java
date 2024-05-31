/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import java.io.Serializable;

/**
 * Represents the bounds of a sequence of messages within a log.
 *
 * @author tflowers
 *
 */
public class LogSpan implements Serializable {

    /** The version of this class used by the serialization logic. */
    private static final long serialVersionUID = 1L;

    /** The index of the first message of the {@link LogSpan}. */
    private int start;

    /** The exclusive index of the last message of the {@link LogSpan}. */
    private int end;

    /** No argument constructor to make the type GWT serializable */
    private LogSpan() {
    }

    /**
     * Constructor that creates a new {@link LogSpan} with a provided start
     * index and end index.
     *
     * @param start The index of the first message of the {@link LogSpan}. Must
     *        be greater than or equal to zero.
     * @param end The exclusive index of the last message of the
     *        {@link LogSpan}. Must be greater than or equal to the start.
     */
    public LogSpan(int start, int end) {
        this();
        setStart(start);
        setEnd(end);
    }

    /**
     * Getter for the index of the first message to include in this
     * {@link LogSpan}.
     *
     * @return The value of {@link #start}. Must be greater than or equal to 0.
     */
    public int getStart() {
        return start;
    }

    /**
     * Setter for the index of the first message to include in this
     * {@link LogSpan}.
     *
     * @param start The new value of {@link #start}. Must be greater than or
     *        equal to 0.
     */
    private void setStart(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("The value of 'start' must be greater than or equal to 0.");
        }

        this.start = start;
    }

    /**
     * Getter for the exclusive index of the last message to be included in this
     * {@link LogSpan}. This means that this is the index of the first message
     * to be excluded from this {@link LogSpan}.
     *
     * @return The value of {@link #end}. Will be greater than or equal to the
     *         current value of {@link #getStart()}.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Setter for the exclusive index of the last message to be included in this
     * {@link LogSpan}. This means that this is the index of the first message
     * to be excluded from this {@link LogSpan}.
     *
     * @param end The new value of {@link #end}. This value must be greater than
     *        the current value of {@link #start}.
     */
    public void setEnd(int end) {
        if (end < start) {
            throw new IllegalArgumentException("The value of 'end' must be greater than the current value of start (" + start + ")");
        }

        this.end = end;
    }

    @Override
    public String toString() {
        return new StringBuilder("[LogSpan: ")
                .append("start = ").append(start)
                .append(", end = ").append(end)
                .append("]").toString();
    }
}
