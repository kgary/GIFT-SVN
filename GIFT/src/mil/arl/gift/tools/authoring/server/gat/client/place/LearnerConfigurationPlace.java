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
 * A place in which a Learner Configuration can be edited. Essentially, the location of the Web LCAT for a particular learner configuration.
 */
public class LearnerConfigurationPlace extends GenericParamPlace{

        /**
         * Constructor
         * 
         * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
         */
        public LearnerConfigurationPlace(HashMap<String, String> startParams) {
            super(startParams);
        }

        
        /**
         * The Class Tokenizer.
         */
        public static class Tokenizer implements PlaceTokenizer<LearnerConfigurationPlace> {

            /* (non-Javadoc)
             * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
             */
            @Override
            public String getToken(LearnerConfigurationPlace place) {
                
                // Encode the mapping of start parameters into a token that will be placed on the url.  
                HashMap<String, String> startParams = place.getStartParams();
                
                String token = PlaceParamParser.encodeTokenParameters(startParams);
                
                return token;
            }
            
            /* (non-Javadoc)
             * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
             */
            @Override
            public LearnerConfigurationPlace getPlace(String token) {

                return new LearnerConfigurationPlace(PlaceParamParser.getParams(token)); 
            }
     
        }
}