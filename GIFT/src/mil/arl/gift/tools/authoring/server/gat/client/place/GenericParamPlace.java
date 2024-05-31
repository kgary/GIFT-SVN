/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.place;

import java.util.HashMap;

import com.google.gwt.place.shared.Place;

/**
 * The GenericParamPlace class holds a mapping of generic parameters (name/value pairs) that
 * can be used for any place.  The goal is that the url for the place would look like:
 *  /#DkfPlace:filePath=Public/COIN AutoTutor/simplest.dkf.xml|deployMode=Desktop
 *  or
 *  {@literal <placetag>:<param1>=<value1>|<param2>=<value2>|<param3>=<value3> }
 * 
 * Each place that extends this class can specify their own mapping of parameters.
 * 
 * IMPORTANT: 
 * 
 * Due to the gwt framework, the Tokenizer logic is not able to be put inside of this class.
 * Instead any class that extends this class, MUST still implement the Tokenizer class
 * to parse the parameters.  Look at the Tokenizer for {@link DkfPlace} as an example of how
 * to setup the Tokenizer.  The code there should be boilerplate code and should be easily 
 * copied into any new classes that extend this class.
 * 
 * 
 * @author nblomberg
 *
 */
public class GenericParamPlace extends Place{

    /** Parameter containing the path of the file to open. */
    public static final String PARAM_FILEPATH = "filePath";
    /** Parameter containing the deployment mode of the gift application (SERVER, DESKTOP, EXPERIMENT) */
    public static final String PARAM_DEPLOYMODE = "deployMode";
    /** Parameter to indicate to the authoring tool that a new file should be created. */
    public static final String PARAM_CREATENEW = "createNew";
    /** Parameter to indicate to the authoring tool that the file should be read-only */
    public static final String PARAM_READONLY = "readOnly";
    /** Parameter to indicate to the authoring tool that the file should be previewed in full screen mode */
    public static final String PARAM_FULLSCREEN = "fullScreen";
    
    /** Mapping of generic parameters that are used to customize the place */
	private HashMap<String, String> params = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param startParams A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
	 */
    public GenericParamPlace(HashMap<String, String> startParams) {
        super();
        
        this.params = startParams;
    }

    /**
     * Accessor to retrieve the start parameters for this place.
     * 
     * @return HashMap<String, String> A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
     */
    public HashMap<String, String> getStartParams() {
        return params;
    }

}
