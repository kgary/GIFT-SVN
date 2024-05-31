/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.place;

import java.util.HashMap;

import com.google.gwt.place.shared.PlaceTokenizer;

import mil.arl.gift.tools.authoring.server.gat.client.util.PlaceParamParser;



/**
 * A place in which a DKF can be edited. Essentially, the location of the Web DAT for a particular DKF.
 */
public class DkfPlace extends GenericParamPlace{
    
    
    /**
     * Parameter to indicate the survey context id that is associated with the
     * course.
     */
    public final static String PARAM_SURVEYCONTEXTID="surveyContextId";
    
    /** Parameter to indicate that the dkf is being newly imported into the course for the first time. */
    public final static String PARAM_IMPORTEDDKF="importedDkf";

    /**
     * Parameter to indicate the training application for which the DKF is
     * authored
     */
    public final static String PARAM_TRAINING_APP = "trainingApp";

    /** Parameter to indicate if the DKF is being opened from GIFT Wrap */
    public final static String PARAM_GIFTWRAP = "giftWrap";

    /**
     * Parameter to indicate if the DKF is being used for a scenario log
     * playback
     */
    public final static String PARAM_PLAYBACK = "playback";
    
    /**
     * Parameter to indicate if the DKF is being used for interactive remediation content
     */
    public final static String PARAM_REMEDIATION = "remediation";

    /**
     * Constructor
     * 
     * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
     */
    public DkfPlace(HashMap<String, String> startParams) {
        super(startParams);
    }

    
    /**
     * The Class Tokenizer.
     */
    public static class Tokenizer implements PlaceTokenizer<DkfPlace> {

        /* (non-Javadoc)
         * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
         */
        @Override
        public String getToken(DkfPlace place) {
            
            // Encode the mapping of start parameters into a token that will be placed on the url.  
            HashMap<String, String> startParams = place.getStartParams();
            
            String token = PlaceParamParser.encodeTokenParameters(startParams);
            
            return token;
        }
        
        /* (non-Javadoc)
         * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
         */
        @Override
        public DkfPlace getPlace(String token) {
            return new DkfPlace(PlaceParamParser.getParams(token)); 
        }
    }
}