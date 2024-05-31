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
 * A place in which a course can be edited. Essentially, the location the Web CAT for a particular course.
 */
public class CoursePlace extends GenericParamPlace {


	/**
	 * Instantiates a new course.
	 */
	public CoursePlace() {
		this(null);
	}
    
	/**
	 * Instantiates a new course.
	 *
	 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
	 */
	public CoursePlace(HashMap<String, String> startParams) {
		super(startParams != null ? startParams : new HashMap<String, String>());
	}
    
    /**
     * The Class Tokenizer.
     *
     * @author iapostolos
     */
    public static class Tokenizer implements PlaceTokenizer<CoursePlace> {

        /* (non-Javadoc)
         * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
         */
        @Override
        public String getToken(CoursePlace place) {
            // Encode the mapping of start parameters into a token that will be placed on the url.  
            HashMap<String, String> startParams = place.getStartParams();
            
            String token = PlaceParamParser.encodeTokenParameters(startParams);
            
            return token;
        }

        /* (non-Javadoc)
         * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
         */
        @Override
        public CoursePlace getPlace(String token) {

            return new CoursePlace(PlaceParamParser.getParams(token)); 
        }
    }	
}
