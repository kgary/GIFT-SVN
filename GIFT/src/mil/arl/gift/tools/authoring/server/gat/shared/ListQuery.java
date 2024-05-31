/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A query for a list of data from the server
 *
 * @param <T> The query parameters class
 * @author jleonard
 */
public class ListQuery<T extends IsSerializable> implements IsSerializable {

    private T queryData;

    private int queryRecordReturnCount;

    private int queryRecordIndexStart;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public ListQuery() {
    }

    /**
     * Constructor
     *
     * Query for records from a certain index for a certain amount
     *
     * @param data The query parameters
     * @param queryRecordReturnCount The number of records to return (maximum),
     * 0 if there is no limit
     * @param queryRecordIndexStart The index to start the records from
     */
    public ListQuery(T data, int queryRecordReturnCount, int queryRecordIndexStart) {

        this.queryData = data;
        this.queryRecordReturnCount = queryRecordReturnCount;
        this.queryRecordIndexStart = queryRecordIndexStart;
    }

    /**
     * Gets the parameters of the query specific to what is being queried
     *
     * @return T The parameters of the query
     */
    public T getQueryData() {

        return this.queryData;
    }

    /**
     * Gets the index to start the records from
     *
     * @return int The index to start the records from
     */
    public int getQueryRecordIndexStart() {

        return this.queryRecordIndexStart;
    }

    /**
     * Gets the number of records to return (maximum)
     *
     * @return int The number of records to return
     */
    public int getQueryRecordReturnCount() {

        return this.queryRecordReturnCount;
    }

	public void setQueryData(T queryData) {
		this.queryData = queryData;
	}

	public void setQueryRecordReturnCount(int queryRecordReturnCount) {
		this.queryRecordReturnCount = queryRecordReturnCount;
	}

	public void setQueryRecordIndexStart(int queryRecordIndexStart) {
		this.queryRecordIndexStart = queryRecordIndexStart;
	}
}
