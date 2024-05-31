/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition.autotutor;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.net.rest.RESTClient;

/**
 * This class is responsible for interfacing with the auto tutor web service ("AT WS").  Each instance of this class
 * will have its own session id with the web service.
 * 
 * @author mhoffman
 *
 */
public class AutoTutorWebServiceInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AutoTutorWebServiceInterface.class);
    
    private static final String DEFAULT_USERNAME = "gift-user";
    
    private static final String ID_PARAM_KEY = "id";
    private static final String USER_PARAM_KEY = "User";
    private static final String LANGUAGE_PARAM_KEY = "language";
    private static final String ENGLISH_LANGUAGE = "English";  //eventually this will come from the client browser based on the language GIFT UI is translated into
    private static final String TEXT_PARAM_KEY = "TEXT";
    private static final String INPUT_PARAM_KEY = "input";
    private static final String SCRIPT_URL_PARAM_KEY = "ScriptURL";
    private static final String ACE_ACTIONS = "ACEActions";   
    public static final String EXPECTATION = "Expectation";    
    public static final String HINT ="Hint";
    public static final String PROMPT = "Prompt";
    
    /** response keys */
    private static final String SYSTEM = "System";
    private static final String SPEAK = "Speak";
    private static final String DISPLAY = "Display";
    private static final String END = "End";
    private static final String WAIT = "WaitForInput"; 
    private static final String AGENT = "Agent";
    private static final String ACT = "Act";
    private static final String COLON = ":";
    private static final String SEMI_COLON = ";";
    private static final String DATA = "Data";     
    private static final String EQUALS = "=";
    private static final String REGEX = "regex";
    private static final String LSA = "lsa";    
      
    private static final String ExpectationMatch = "ExpectationMatch";
    private static final String HintMatch = "HintMatch";
    private static final String PromptMatch = "PromptMatch";
    private static final String SetStatus = "SetStatus";
    
    
    /** this is the wait response message when calling getAssessmentResult method of the AT WS */
    private static final String WAIT_RESPONSE = "#..WAIT..#";
    
    /** maximum amount of time to wait on AT WS to stop sending a wait response and provide an answer to the query */
    private static final long MAX_WAIT_MS = 5000;
    
    private static final String EMPTY = "";
    
    public static enum Script_Reference_Type{
        ATWS,
        URL,
        FILE
    }
    
    /** 
     * values used for assessing a user's response
     * The range for a coverage value is [0,1.0].
     * These "assessment" value boundaries were chosen by the University of Memphis for use by GIFT.
     */
    private static final double GOOD_COVERAGE = 0.7;
    private static final double POOR_COVERAGE = 0.3;
    
    private RESTClient service = null;
    
    
    /** whether to keep trying to query AT WS upon receiving an exception */
    private boolean repeatedlyTry = true;
    private static final int REPEAT_ATTEMPTS = 2;
    
    /** response to an init script method call - contains session id */
    private InitScriptResponse initScriptResponse;
    
    /** the URL of the AutoTutor ACE server */
    private URL ACEREST_URL;
    
    /** unique id for this conversation execution */
    private String guid = UUID.randomUUID().toString();    

    /** parameters sent to the AutoTutor ACE engine in REST calls */
    private Map<String,String> parameters = new HashMap<String,String>();
    
    /**
     * Class constructor - establish connection
     */
    public AutoTutorWebServiceInterface(){
        init();
    }
    
    /**
     * Establish a connection to the web service.
     * Should be called by the constructor.
     */
    private void init(){    
        
        try {
            ACEREST_URL = new URL(DomainModuleProperties.getInstance().getAutoTutorACEURL());
            
            //ping URL
            UriUtil.isURLReachable(DomainModuleProperties.getInstance().getAutoTutorACEURL(), null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to the AutoTutor ACE server at the URL '"+DomainModuleProperties.getInstance().getAutoTutorACEURL()+"' because\n"+e.toString(), e);
        }
        
        parameters.put(ID_PARAM_KEY,guid);
   	 	parameters.put(USER_PARAM_KEY,DEFAULT_USERNAME);
   	 	parameters.put(LANGUAGE_PARAM_KEY, ENGLISH_LANGUAGE);
        service = new RESTClient();
    }
    
    /**
     * This method is called when the conversation is finished. 
     * This will help us to remove the expired object from the cache. 
     */
    public void endScript(){
        
        int attemptCnt = 0;
        do{            
            
            try{            	
            	service.delete(ACEREST_URL, parameters);
                break;
            }catch(Exception e){
                logger.error("Caught exception while trying to end the autotutor conversation", e);            	
            }
            
            attemptCnt++;
            
        }while(repeatedlyTry && attemptCnt < REPEAT_ATTEMPTS);
    }
    
    /**
     * Return the information on an assessment method call to the AT WS.
     * 
     * @param input - the user's input
     * @return AssessmentResponse
     */
    public AssessmentResponse getAssessments(String input){
        
        if(input == null){
            throw new IllegalArgumentException("The input can't be null.");
        }
        
        AssessmentResponse assResponse = null;
        
        int attemptCnt = 0;
        do{
            try{
            	
            	parameters.put(TEXT_PARAM_KEY,input);
            	
            	byte[] output = service.put(ACEREST_URL,parameters);
            	
            	String rawAssessment = new String(output);            	
            	
                Object actionobj = new JSONParser().parse(rawAssessment);
                JSONArray msg = (JSONArray) ((JSONObject) actionobj).get(ACE_ACTIONS);
                rawAssessment = msg.toJSONString();

                long startTime = System.currentTimeMillis(), nowTime;
                
                while (rawAssessment.equals(WAIT_RESPONSE)){
                    nowTime = System.currentTimeMillis();
                    if((nowTime - startTime) > MAX_WAIT_MS){
                        logger.error("Waited more than "+MAX_WAIT_MS/1000.0+" seconds to get an assessment response and AutoTutor Webservice keeps responding with wait.");
                        break;
                    }
                    
                    //the AutoTutor ACE server is requesting more time to compute, wait before querying it again
                	Thread.sleep(100);
                    
                	rawAssessment = service.put(ACEREST_URL, parameters).toString();
                }
                assResponse = new AssessmentResponse(rawAssessment);
                if(logger.isInfoEnabled()){
                    logger.info("Received assessment response of "+assResponse);
                }
                break;                
    
            }catch(Exception e){
                logger.error("Caught exception while trying to retrieve the AutoTutor assessments for the learner's input of:\n"+input, e);
            }
            
            attemptCnt++;
            
        }while(repeatedlyTry && attemptCnt < REPEAT_ATTEMPTS);
        
        return assResponse;
    }
    
    /**
     * Calculate and return a GIFT assessment value for the assessment response information received
     * from the AT WS.
     * 
     * @param response - information returned from a get assessment call to the AT WS.
     * @return List<String>: Matching for all the values
     */
    public AssessmentLevelEnum calculateAssessment(AssessmentResponse response, String Assessmenttype){
        
        AssessmentLevelEnum level = AssessmentLevelEnum.AT_EXPECTATION;
        if(response != null) {
            double coverage = 0.0;
        	if (Assessmenttype == EXPECTATION) {
        		coverage =  response.getExpectationCoverage();
        	}else if (Assessmenttype == HINT){
        		coverage =  response.getHintCoverage();
        	}else if (Assessmenttype == PROMPT) {
        		coverage =  response.getPromptCoverage();
        	}

            if(coverage <= POOR_COVERAGE){
                level = AssessmentLevelEnum.BELOW_EXPECTATION;
            }else if(coverage <= GOOD_COVERAGE){
                level = AssessmentLevelEnum.AT_EXPECTATION;
            }else if(coverage > GOOD_COVERAGE){
                level = AssessmentLevelEnum.ABOVE_EXPECTATION;
            }
        }
        
        return level;
    }
    
    /**
     * Initialize the specified local script (i.e. on the web service machine) by initiating an instance of AutoTutor
     * Conversation class (ATC).
     *  
     * @param scriptName - the name of a local (to the web service) script
     */
    public void initScript(String scriptName){
        initScript(scriptName, DEFAULT_USERNAME);       
    }
    
    /**
     * Initialize the specified local script (i.e. on the web service machine) by initiating an instance of AutoTutor
     * Conversation class (ATC).
     *  
     * @param scriptName - the name of a local (to the web service) script
     * @param userName - the user's name
     */
    public void initScript(String scriptName, String userName){
        
        if(scriptName == null){
            throw new IllegalArgumentException("The script name can't be null.");
        }else if(userName == null){
            throw new IllegalArgumentException("The user name can't be null.");
        }
        
        int attemptCnt = 0;
        do{
            try{
            	parameters.put(INPUT_PARAM_KEY,Constants.EMPTY);

            	byte[] output = service.post(ACEREST_URL,parameters);
            	String response = new String (output);
                initScriptResponse = new InitScriptResponse(response,guid);
        
            }catch(Exception e){
                logger.error("Caught exception while trying to initialize the AutoTutor ACE server for the script "+scriptName, e);
            }
            
            attemptCnt++;
        
        }while(repeatedlyTry && attemptCnt < REPEAT_ATTEMPTS);
    }
    
    /**
     * Initialize the specified remote script (i.e. not on the web service machine) by initiating an instance of AutoTutor
     * Conversation class (ATC).
     * 
     * @param scriptURL - URL of a script remote to the web service machine but still accessible by it via a connection
     */
    public void initScriptByURL(String scriptURL){
        initScriptByURL(scriptURL, DEFAULT_USERNAME);
    }
    
    /**
     * Initialize the specified remote script (i.e. not on the web service machine) by initiating an instance of AutoTutor
     * Conversation class (ATC).
     * 
     * @param scriptURL - URL of a script remote to the web service machine but still accessible by it via a connection
     * @param userName - the user's name
     */
    public void initScriptByURL(String scriptURL, String userName){
        
        if(scriptURL == null){
            throw new IllegalArgumentException("The script URL can't be null.");
        }else if(userName == null){
            throw new IllegalArgumentException("The user name can't be null.");
        }
        
        int attemptCnt = 0;
        do{
            try{
            	parameters.put(TEXT_PARAM_KEY, Constants.EMPTY);
           	 	parameters.put(SCRIPT_URL_PARAM_KEY,scriptURL);
            	byte[] output = service.post(ACEREST_URL,parameters);
            	String response = new String (output);
                initScriptResponse = new InitScriptResponse(response,guid);                
            }catch(Exception e){
                logger.error("Caught exception while trying to initialize the AutoTutor ACE server for the script URL "+scriptURL, e);
            }
            
            attemptCnt++;
        
        }while(repeatedlyTry && attemptCnt < REPEAT_ATTEMPTS);
    }
    
    /**
     * Initialize the specified local script (i.e. a file on this machine) by initiating an instance of AutoTutor
     * Conversation class (ATC).
     * Deprecated - the new RESTFul API for AutoTutor ACE engine doesn't support
     * uploading a file.  Use {@link #initScriptByURL(String)} instead.
     * 
     * @param file - a local script file to upload to the web service machine
     */
    @Deprecated
    public void initScriptByXML(FileProxy file){
//        initScriptByXML(file, DEFAULT_USERNAME);
    }
    
    /**
     * Initialize the specified local script (i.e. a file on this machine) by initiating an instance of AutoTutor
     * Conversation class (ATC).
     * Deprecated - the new RESTFul API for AutoTutor ACE engine doesn't support
     * uploading a file.  Use {@link #initScriptByURL(String)} instead.
     * 
     * @param file - a local script file to upload to the web service machine
     * @param userName - the user's name
     */
    @Deprecated
    public void initScriptByXML(FileProxy file, String userName){
        
//        if(file == null || !file.exists()){
//            throw new IllegalArgumentException("The file '"+file+"' doesn't exist.");
//        }else if(userName == null){
//            throw new IllegalArgumentException("The user name can't be null.");
//        }
//        
//        int attemptCnt = 0;
//        do{
//            try{
//            	URL ACERESTurl = new URL(ACEREST);
//            	parameters.put("Text","");
//            	// parameters.put("ScriptURL",file.getFileId());
//            	// ACE only accepts scripts in the form of URL, not local file.
//            	byte[] output =service.post(ACERESTurl,parameters);
//            	String response=new String (output);
//            	
//            	initScriptResponse = new InitScriptResponse(response,guid);
//            	
//            	       
//            }catch(WebServiceException wse){
//                logger.error("Caught exception while trying to call init Script by XML method", wse);
//            } catch (IOException e) {
//                logger.error("Caught exception while reading file of "+file+" into a string to send to the init script by XML method.", e);
//                break;  //give up and don't try again because the problem has nothing to do with AutoTutorWebService
//            }
//            
//            attemptCnt++;
//        
//        }while(repeatedlyTry && attemptCnt < REPEAT_ATTEMPTS);
    }
    
    /**
     * Return the response of an init script call.
     * 
     * @return InitScriptResponse
     */
    public InitScriptResponse getInitScriptResponse(){
        return initScriptResponse;
    }
    
    /**
     * Get the actions for the user's text.
     * 
     * @param input - the user's text
     * @return ActionsResponse - information about the actions received.  Will be null if an error occurred.
     */
    public ActionsResponse getActions(String input){
        
        if(input == null){
            throw new IllegalArgumentException("The input can't be null.");
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Retrieving actions for user input of '"+input+"'.");
        }
        
        ActionsResponse response = null;
        
        int attemptCnt = 0;
        do{
            try{
            	parameters.put(TEXT_PARAM_KEY, input);
            	byte[] output = service.put(ACEREST_URL,parameters);
            	String rawActions = new String(output);
            	
                Object Actionobj = new JSONParser().parse(rawActions);
                JSONArray msg = (JSONArray) ((JSONObject) Actionobj).get(ACE_ACTIONS);
                rawActions = msg.toJSONString();

                response = new ActionsResponse(rawActions);
                
                if(response.getWaitForInput() == null){
                    //get more actions...
                	String rawActions2 = service.put(ACEREST_URL,parameters).toString();  
                    response.addRawResponse(rawActions2);
                }
                
                if(logger.isInfoEnabled()){
                    logger.info("Received actions response of "+response);
                }
                break;
        
            }catch(Exception e){
                logger.error("Caught exception while trying to retrieve the next AutoTutor conversation action from the learner's input of:\n"+input, e);
            }
            
            attemptCnt++;
        
        }while(repeatedlyTry && attemptCnt < REPEAT_ATTEMPTS);
        
        return response;
    }
    
    /**
     * Get the initial set of actions from the script.
     * 
     * @return ActionsResponse - information about the actions received
     * Note: the conversation id will not be set.
     */
    public ActionsResponse getInitialActions(){        
        return getActions(EMPTY);
    }
    
    /**
     * This method provides detailed data about the conversation. There is no input argument for this method. 
     * The output of the method is a string with all logged data. Any time the method is called, the stored 
     * data will be returned and cleared in the memory. The client program is responsible for saving the data to the desired place.
     * 
     * @return String
     */
    public String getLog(){        
        return guid;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder("[AutoTutorWebServiceInterface: ");
        sb.append("guid = ").append(guid);
        sb.append(", connected = ").append(service != null);
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * This nested class organizes the information provided in a response to an init script method call.
     * 
     * @author mhoffman
     *
     */
    public static class InitScriptResponse{
        
        /** unique session id for this connection to the AT WS */
        private String sessionId;
        
        /** the raw response string from an init script method call */
        private String response;
        
        /**
         * Class constructor - parse the response.
         * 
         * @param response - raw response from an init script method call.  Can't be null.
         */
        public InitScriptResponse(String response,String  guid){
            parseResponse(response,guid);
        }
        
        /**
         * Parse the raw response string and retrieve the important information.
         * 
         * @param response
         */
        private void parseResponse(String response, String guid){            
            
            if(response == null){
                throw new IllegalArgumentException("The response can't be null");
            }
            
            this.response = response;            
            sessionId = guid;
        }
        
        /**
         * Return the session id parameter returned from the response.
         * 
         * @return String
         */
        public String getSessionId(){
            return sessionId;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[InitScriptResponse: ");
            sb.append("sessionId = ").append(getSessionId());
            sb.append(", rawResponse = ").append(response);
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This subclass organizes the information provided in a response to a get actions method call.
     * 
     * @author mhoffman
     *
     */
    public static class ActionsResponse implements DomainAssessmentContent{
        
        /** collection of strings to speak via an avatar */
        private List<String> speak = new ArrayList<>();
        
        /** collection of strings to display as text */
        private List<String> display = new ArrayList<>();
        
        /** whether the session has ended */
        private boolean ended = false;
        
        private Double waitForInputSeconds = 30.00;
        
        /** the raw response string to the method call */
        private String response;

        private List<String> expectationMatchScore = new ArrayList<>(); 
        private List<String> hintMatchScore = new ArrayList<>(); 
        private List<String> promptMatchScore = new ArrayList<>();

        private double expectationCoverage = 0.0;
        private double hintCoverage = 0.0;
        private double promptCoverage = 0.0;
        
        /** unique chat id of the chat associated with this response */
        private int chatId;
        
        /**
         * Class constructor - parse the response.
         * 
         * @param response - response to the get actions method call.  Can't be null or empty.
         */
        public ActionsResponse(String response){
            parseResponse(response);
        }      
        
        /**
         * Add a response to the get actions method call to the current response already parsed.
         * 
         * @param response - response to the get actions method call.  Can't be null or empty.
         */
        public void addRawResponse(String response){
            this.response += SEMI_COLON + response;
            parseResponse(response);
        }
        
        /**
         * Parse the response string and retrieve the important information.
         * 
         * @param response response to the get actions method call.  Can't be null or empty.
         */
        private void parseResponse(String response){            
            
            if(response == null){
                throw new IllegalArgumentException("The response can't be null");
            }else if(response.isEmpty()){
                throw new IllegalArgumentException("The response can't be empty");
            }
            
            this.response = response;
            
            try {
				Object Actionobj = new JSONParser().parse(response);
				
	            JSONArray msg = (JSONArray) Actionobj;
	            for (Object o : msg) {
	        		JSONObject ca = (JSONObject) o;
	        		String key = (String) ca.get(AGENT);
	        		String type = (String) ca.get(ACT);
	        		String data = (String)ca.get(DATA);                    
                    if(key.equals(SYSTEM)){                                         
                        if(type.equals(DISPLAY)){
                        	String [] displayMsg = data.split(COLON);
                        	
                        	if(displayMsg.length > 0){
                        	    
                        	    // get the last item in the list.  This is normally the message to display.  If there is more than
                        	    // one item, the first item is normally the tutor's name, i.e. 'Tutor'.
                        	    // Add a new line at the end to handle multiple lines to display, i.e. when the for loop enters here multiple times
                        	    String adisplay = displayMsg[displayMsg.length-1] + Constants.HTML_NEWLINE;
                            	if (!display.contains(adisplay)) {
                            	    //don't add the same message more than once.  This was seen in 8/18 and is most likely a bug in AutoTutor ACE server
                            	    //that was recently deployed for GIFT.
                            		display.add(adisplay);
                            	}
                        	}
                        }else if(type.equals(END) || (type.equals(SetStatus) && data.equals(END))){
                            ended = true;
                        }else if(type.equals(WAIT)){
                            //deprecated
                        }else if (type.equals(ExpectationMatch)) {
                        	expectationMatchScore.add(data);
                        }else if (type.equals(HintMatch)) {
                        	hintMatchScore.add(data);
                        }else if (type.equals(PromptMatch)) {
                        	promptMatchScore.add(data);
                        }
                        
                    }else{
                        if(type.equals(SPEAK)){
                        	if (!speak.contains(data)) {
                        	    //don't add the same message more than once.  This was seen in 8/18 and is most likely a bug in AutoTutor ACE server
                                //that was recently deployed for GIFT.
                                speak.add(data);  
                        	}
                        }
	                }
	        	} //end for
            
	            expectationCoverage = composeScore(expectationMatchScore);  
	            hintCoverage = composeScore(hintMatchScore); 
	            promptCoverage = composeScore(promptMatchScore);  
	            
            } catch (ParseException e) {
				logger.error("Had trouble parsing a response from the AutoTutor ACE server:\n"+response, e);
			}
        
        }
        
        /**
         * Calculate a score for the match scores provided.  
         * 
         * @param matchScore one or more match scores (e.g. hint match score) that comes from the AutoTutor ACE
         * response to learner input.          
         * @return 0.0 by default, otherwise the value comes from the LSA or REGEX value in the match score
         * parameters.
         */
        private double composeScore(List<String> matchScore) {

            double defaultValue = 0.0;
            if (matchScore.isEmpty()) {
            	 return defaultValue;
            }
            for (String dataStr : matchScore) {
                
                String[] params = dataStr.split(SEMI_COLON);
                for (String param : params){
                   
                    String[] tokens = param.split(EQUALS);
                    if(tokens.length >= 2){
                        String aname = tokens[0];
                        String avalue = tokens[1];
                        if ((aname.trim().equals(LSA))||(aname.trim().equals(REGEX))) {
                            double newvalue = Float.parseFloat(avalue.trim());
                            if (newvalue >= defaultValue) {
                                defaultValue = newvalue;
                            }
                        }
                    }
                }
            }
            
            return defaultValue;
        }
        
        /**
         * Return the coverage score for Expectation.
         * 
         * @return default is 0.0
         */
        public double getExpectationCoverage() {
            return expectationCoverage;
        }

        /**
         * Return the coverage score for Hint.
         * 
         * @return default is 0.0
         */
        public double getHintCoverage() {
            return hintCoverage;
        }

        /**
         * Return the coverage score for Prompt.
         * 
         * @return default is 0.0
         */
        public double getPromptCoverage() {
            return promptCoverage;
        }
        
        /**
         * Whether or not the session has ended.
         * 
         * @return boolean
         */
        public boolean hasEnded(){
            return ended;
        }
        
        /**
         * Return the amount of time in seconds the system should wait for the user to respond. 
         * It is up to the client program to set the timer and detect the user's idleness. 
         * The client program is supposed to return whatever input the user has entered if the user idled for n seconds.
         * 
         * @return Double - can be null if no user input is needed
         */
        public Double getWaitForInput(){
           return waitForInputSeconds;
        }
        
        /**
         * Return the list of strings to speak.
         * 
         * @return List<String>
         */
        public List<String> getSpeak(){
            return speak;
        }
        
        /**
         * Return the list of strings to speak as a single string with spaces separating
         * each string.
         * 
         * @return String
         */
        public String getSpeakAsString(){
            
            StringBuffer sb = new StringBuffer();
            
            for(String str : speak){
                sb.append(str).append(Constants.SPACE);
            }
            
            return sb.toString();
        }
        
        /**
         * Return the list of strings to display as text.
         * 
         * @return List<String>
         */
        public List<String> getDisplayText(){
            return display;
        }
        
        /**
         * Return the unique chat id that this Actions Response applies too.
         * 
         * @return int
         */
        public int getChatId(){
            return chatId;
        }
        
        /**
         * Set the unique chat id that can be used used to match/sync updates and chat history 
         * 
         * @param chatId - unique id for a given chat session
         */
        public void setChatId(int chatId){
            
            if(chatId <= 0){
                throw new IllegalArgumentException("The chat id of "+chatId+" must be greater than zero to be considered a valid id.");
            }
            
            this.chatId = chatId;
        }
        
        /**
         * Return the list of strings to display as text as a single string with newlines separating
         * each string.
         * 
         * @return String
         */
        public String getDisplayTextAsString(){
            
            StringBuffer sb = new StringBuffer();
            
            for(String str : display){
                sb.append(str).append("\n");
            }
            
            return sb.toString();
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ActionsResponse: ");
            sb.append("chatId = ").append(getChatId());
            sb.append(", ended = ").append(hasEnded());
            sb.append(", expectation coverage = ").append(getExpectationCoverage());
            sb.append(", hint coverage = ").append(getHintCoverage());
            sb.append(", prompt coverage = ").append(getPromptCoverage());
            sb.append(", speak = ").append(getSpeakAsString());
            sb.append(", display = ").append(getDisplayTextAsString());
            sb.append(", rawResponse = ").append(response);
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This nested class organizes the information provided in a response to a get assessments method call.
     * 
     * @author mhoffman
     *
     */
    public static class AssessmentResponse{
        
        /** collection of strings to speak via an avatar */
        private List<String> speak = new ArrayList<>();
        
        /** collection of strings to display as text */
        private List<String> display = new ArrayList<>();
        
        /** whether the session has ended */
        private boolean ended = false;     
        
        /** the raw response string to the method call */
        private String response;
        
        private List<String> expectationMatchScore = new ArrayList<>(); 
        private List<String> hintMatchScore = new ArrayList<>(); 
        private List<String> promptMatchScore = new ArrayList<>(); 
        
        private double expectationCoverage = 0.0;
        private double hintCoverage = 0.0;
        private double promptCoverage = 0.0;
        
        /** unique chat id of the chat associated with this response */
        private int chatId;
        
        /**
         * Class constructor - parse the response.
         * 
         * @param response - response to the get actions method call.  Can't be null or empty.
         */
        
        /** the raw response string to the method call */
   
        public AssessmentResponse(String response){
            parseResponse(response);
        }

        /**
         * Parse the response string and retrieve the important information.
         * 
         * @param response
         */
        private void parseResponse(String response){            
            
            if(response == null){
                throw new IllegalArgumentException("The response can't be null");
            }else if(response.isEmpty()){
                throw new IllegalArgumentException("The response can't be empty");
            }            
            
            this.response = response;
            
            try {
				Object actionobj = new JSONParser().parse(response);
				
	            JSONArray msg = (JSONArray) actionobj;
	        	for (Object o : msg) {
	        		JSONObject ca = (JSONObject) o;
	        		String key = (String) ca.get(AGENT);
	        		String type = (String) ca.get(ACT);
	        		String data = (String)ca.get(DATA);                    
                    if(key.equals(SYSTEM)){                                         
                        if(type.equals(DISPLAY)){
                        	String [] displayMsg = data.split(COLON);

                            if(displayMsg.length > 0){
                                
                                // get the last item in the list.  This is normally the message to display.  If there is more than
                                // one item, the first item is normally the tutor's name, i.e. 'Tutor'.
                                // Add a new line at the end to handle multiple lines to display, i.e. when the for loop enters here multiple times
                                String adisplay = displayMsg[displayMsg.length-1] + Constants.HTML_NEWLINE;
                                if (!display.contains(adisplay)) {
                                    //don't add the same message more than once.  This was seen in 8/18 and is most likely a bug in AutoTutor ACE server
                                    //that was recently deployed for GIFT.
                                    display.add(adisplay);
                                }
                            }
                        }else if(type.equals(END)){
                            ended = true;
                        }else if(type.equals(WAIT)){
                            //deprecated
                        }else if (type.equals(ExpectationMatch)) {
                        	expectationMatchScore.add(data);
                        }else if (type.equals(HintMatch)) {
                        	hintMatchScore.add(data);
                        }else if (type.equals(PromptMatch)) {
                        	promptMatchScore.add(data);
                        }
                        
                    }else{
                        if(type.equals(SPEAK)){
                        	if (!speak.contains(data)) {
                        	    //don't add the same message more than once.  This was seen in 8/18 and is most likely a bug in AutoTutor ACE server
                                //that was recently deployed for GIFT.
                                speak.add(data);  
                        	}
                        }
	                }
	        	} //end for
	        	
	        	//calculate coverage for each
	            expectationCoverage = composeScore(expectationMatchScore);  
	            hintCoverage = composeScore(hintMatchScore); 
	            promptCoverage = composeScore(promptMatchScore);  
	            
            } catch (ParseException e) {
                logger.error("Had trouble parsing a response from the AutoTutor ACE server:\n"+response, e);
			}
        
        }
        
        /**
         * Calculate a score for the match scores provided.  
         * 
         * @param matchScore one or more match scores (e.g. hint match score) that comes from the AutoTutor ACE
         * response to learner input.          
         * @return 0.0 by default, otherwise the value comes from the LSA or REGEX value in the match score
         * parameters.
         */
        private double composeScore(List<String> matchScore) {

            double defaultValue = 0.0;
            if (matchScore.isEmpty()) {
           	 return defaultValue;
           }
        	for (String dataStr : matchScore) {
        	    
                String[] params = dataStr.split(SEMI_COLON);
                for (String param : params){
                   
                    String[] tokens = param.split(EQUALS);
                    if(tokens.length >= 2){
                        String aname = tokens[0];
                        String avalue = tokens[1];
                        if ((aname.trim().equals(LSA))||(aname.trim().equals(REGEX))) {
                            double newvalue = Float.parseFloat(avalue.trim());
                            if (newvalue >= defaultValue) {
                                defaultValue = newvalue;
                            }
                        }
                    }
                }
        	}
        	
			return defaultValue;
		}

        /**
         * Return the coverage score for Expectation.
         * 
         * @return default is 0.0
         */
        public double getExpectationCoverage() {
            return expectationCoverage;
        }

        /**
         * Return the coverage score for Hint.
         * 
         * @return default is 0.0
         */
        public double getHintCoverage() {
            return hintCoverage;
        }

        /**
         * Return the coverage score for Prompt.
         * 
         * @return default is 0.0
         */
        public double getPromptCoverage() {
            return promptCoverage;
        }

        public String getSpeakAsString(){
            
            StringBuffer sb = new StringBuffer();
            
            for(String str : speak){
                sb.append(str).append(Constants.SPACE);
            }
            
            return sb.toString();
        }
        
        /**
         * Return the list of strings to display as text.
         * 
         * @return List<String>
         */
        public List<String> getDisplayText(){
            return display;
        }
        
        /**
         * Return the unique chat id that this Actions Response applies too.
         * 
         * @return int
         */
        public int getChatId(){
            return chatId;
        }
        
        /**
         * Set the unique chat id that can be used used to match/sync updates and chat history 
         * 
         * @param chatId - unique id for a given chat session
         */
        public void setChatId(int chatId){
            
            if(chatId <= 0){
                throw new IllegalArgumentException("The chat id of "+chatId+" must be greater than zero to be considered a valid id.");
            }
            
            this.chatId = chatId;
        }
        
        /**
         * Return the list of strings to display as text as a single string with newlines separating
         * each string.
         * 
         * @return String
         */
        public String getDisplayTextAsString(){
            
            StringBuffer sb = new StringBuffer();
            
            for(String str : display){
                sb.append(str).append("\n");
            }
            
            return sb.toString();
        }

       public boolean hasEnded(){
           return ended;
       }
        
        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append("[ActionsResponse: ");
            sb.append("chatId = ").append(getChatId());
            sb.append(", ended = ").append(hasEnded());
            sb.append(", expectation coverage = ").append(getExpectationCoverage());
            sb.append(", hint coverage = ").append(getHintCoverage());
            sb.append(", prompt coverage = ").append(getPromptCoverage());
            sb.append(", speak = ").append(getSpeakAsString());
            sb.append(", display = ").append(getDisplayTextAsString());
            sb.append(", rawResponse = ").append(response);
            sb.append("]");
            return sb.toString();
        }
    }
}
