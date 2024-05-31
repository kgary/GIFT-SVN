/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A response to a list query
 *
 * @author jleonard
 * @param <T> The type of response
 */
public class ListQueryResponse<T> implements IsSerializable {

    private List<T> listResponse = new ArrayList<T>();

    private int queryRecordIndexStart;

    private int totalQueryRecordsCount;

    private String errorMessage;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public ListQueryResponse() {
    }

    /**
     * Constructor
     *
     * Creates an error response
     *
     * @param errorMessage The error that occurred
     */
    public ListQueryResponse(String errorMessage) {

        this.errorMessage = errorMessage;
    }
    
    /**
     * Constructor
     *
     * @param listResponse The records in response to the query
     * @param queryRecordsTotal The number of records on the server for the
     * query
     */
    public ListQueryResponse(List<T> listResponse, int queryRecordsTotal) {
        this.listResponse.addAll(listResponse);
        this.totalQueryRecordsCount = queryRecordsTotal;
    }

    /**
     * Constructor
     *
     * @param listResponse The records in response to the query
     * @param queryRecordIndexStart The index the records start at in the server
     * @param queryRecordsTotal The number of records on the server for the
     * query
     */
    public ListQueryResponse(List<T> listResponse, int queryRecordIndexStart, int queryRecordsTotal) {
        this.listResponse.addAll(listResponse);
        this.queryRecordIndexStart = queryRecordIndexStart;
        this.totalQueryRecordsCount = queryRecordsTotal;
    }

    /**
     * Gets the records in response to the query
     *
     * @return List<T> The records in response to the query
     */
    public List<T> getList() {

        return this.listResponse;
    }

    /**
     * Gets the index the records start at in the server
     *
     * @return int The index the records start at in the server
     */
    public int getQueryRecordIndexStart() {

        return this.queryRecordIndexStart;
    }

    /**
     * Gets the number of records on the server for the query
     *
     * @return int The number of records on the server for the query
     */
    public int getTotalQueryRecordsCount() {

        return totalQueryRecordsCount;
    }

    /**
     * Gets the message of the error that occurred during the query
     *
     * Null if there was no error
     *
     * @return String The error message
     */
    public String getErrorMessage() {

        return errorMessage;
    }
}
