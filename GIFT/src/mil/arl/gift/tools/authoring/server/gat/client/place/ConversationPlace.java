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
 * A place in which a conversation can be edited.
 */
public class ConversationPlace extends GenericParamPlace {
    
    /** Parameter to indicate to the conversation tree authoring tool that concept assessments are allowed/disabled */
    public static final String PARAM_ALLOW_ASSESSMENTS = "allowAssessments";

	/**
	 * Instantiates a new conversation.
	 *
	 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
	 */
	public ConversationPlace(HashMap<String, String> startParams) {
		super(startParams);
	}
    
    /**
     * The Class Tokenizer.
     *
     * @author iapostolos
     */
    public static class Tokenizer implements PlaceTokenizer<ConversationPlace> {

        /* (non-Javadoc)
         * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
         */
        @Override
        public String getToken(ConversationPlace place) {
            // Encode the mapping of start parameters into a token that will be placed on the url.  
            HashMap<String, String> startParams = place.getStartParams();
            
            String token = PlaceParamParser.encodeTokenParameters(startParams);
            
            return token;
        }

        /* (non-Javadoc)
         * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
         */
        @Override
        public ConversationPlace getPlace(String token) {
            
            return new ConversationPlace(PlaceParamParser.getParams(token)); 
        }
    }	
}
