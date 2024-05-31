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
 * A place in which a Sensors Configuration can be edited. Essentially, the location of the Web SCAT for a particular sensors configuration.
 */
public class SensorsConfigurationPlace extends GenericParamPlace{

        /**
         * Constructor
         * 
         * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
         */
        public SensorsConfigurationPlace(HashMap<String, String> startParams) {
            super(startParams);
        }

        
        /**
         * The Class Tokenizer.
         */
        public static class Tokenizer implements PlaceTokenizer<SensorsConfigurationPlace> {

            /* (non-Javadoc)
             * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
             */
            @Override
            public String getToken(SensorsConfigurationPlace place) {
                
                // Encode the mapping of start parameters into a token that will be placed on the url.  
                HashMap<String, String> startParams = place.getStartParams();
                
                String token = PlaceParamParser.encodeTokenParameters(startParams);
                
                return token;
            }
            
            /* (non-Javadoc)
             * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
             */
            @Override
            public SensorsConfigurationPlace getPlace(String token) {

                return new SensorsConfigurationPlace(PlaceParamParser.getParams(token)); 
            }
     
        }
}
