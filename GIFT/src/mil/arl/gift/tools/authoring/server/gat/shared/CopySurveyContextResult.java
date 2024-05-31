/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.survey.SurveyContext;

/**
 * Result that is returned from the copySurveyContext rpc.
 * The result contains the new SurveyContext that has the surveys copied into it.
 * It also contains a mapping of old GIFT Survey Context key values that are mapped to the
 * new GIFT Survey Context key values.  This mapping can be used to go through the dkf file
 * and update old references to the new key references.
 * 
 * @author nblomberg
 *
 */
public class CopySurveyContextResult implements IsSerializable {

    private SurveyContext surveyContext;

    private HashMap<String, String> surveyIdMap = new HashMap<String, String>();

    /**
     * Default Constructor
     *
     * Required to exist and be public for IsSerializable
     */
    public CopySurveyContextResult() {
    }

    /**
     * Constructor
     * 
     * @param context - The survey context that has all the new surveys copied into it.
     * @param surveyIdMap - Mapping of Old to New GIFT Survey Keys, where Old is the original gift survey context key and the 
     *                          New is the survey context key that should be used in the dkf instead.
     */
    public CopySurveyContextResult(SurveyContext context, HashMap<String, String> surveyIdMap) {
        this.surveyContext = context;
        this.surveyIdMap.putAll(surveyIdMap);
    }

   
    /**
     * Accessor to get the survey context data from the result.
     * 
     * @return SurveyContext The survey context that contains all the new surveys copied into it.
     */
    public SurveyContext getSurveyContext() {

        return surveyContext;
    }

    /**
     * Accessor to get the survey id map which is a mapping of Old to New GIFT Survey Keys, 
     * where Old is the original gift survey context key and the New is the survey context key that 
     * should be used in the dkf instead.
     * 
     * @return HashMap<String, String> Mapping of Old to New GIFT Survey Keys, where Old is the original gift survey context key and the 
     *         New is the survey context key that should be used in the dkf instead.
     */
    public HashMap<String, String> getSurveyIdMapping() {

        return surveyIdMap;
    }
}
