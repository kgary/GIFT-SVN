/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition.simile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimileJNI {
    
    private static Logger logger = LoggerFactory.getLogger(SimileJNI.class);	
	/**
	 * Maps the GIFT condition keys to their callback.
	 */
	private static Map<String, SimileResultsCb> _TriggeredRuleCallbackMap = null;
	static boolean loadedMisLibraries = false, loadedSimileInterfaceLibrary = false;
	public class RuleInfo {
		private String _RuleName;

		public String GetRuleName() {
			return _RuleName;
		}
	}
	
	public abstract static interface RuleInfoCB {		
		public void TriggeredRuleInfo( SimileJNI.RuleInfo ruleInfo );

	}
	
	/**
     * The callback for when Simile needs to pass results back to GIFT.
     * 
     * @author asanchez
     *
     */
    private interface SimileResultsCb  {
    	/**
         * Receives a processed event from Simile.
         * 
         * @param atExpectationAssessment - True if the triggered concept is an 'At Expectation' concept.
         * @param isSatisfied - True if the condition has been satisfied.
         * @param isConditionCompleted - True if the concept has been fulfilled.
         */
        public void SimulationResults( Boolean atExpectationAssessment, Boolean isSatisfied, Boolean isConditionCompleted );
    }
    
    /**
	 * Callback for the triggered rule information obtained from the Simile library.
	 * 
	 * @param ruleInfo - Holds the triggered rule information.
	 */
	private static void SimulationTriggeredRuleInfo( RuleInfo ruleInfo ){
		//System.out.println( "Triggered rule name: " + ruleInfo.GetRuleName() );
		
        if ( ruleInfo != null ) {
            
        	if( ruleInfo.GetRuleName() != null && ruleInfo.GetRuleName().length() > 0 )	{
	            // Send it to the corresponding condition.
	            SimileResultsCb condCb = _TriggeredRuleCallbackMap.get( ruleInfo.GetRuleName() );
	            if ( condCb != null ) {
	                condCb.SimulationResults( ruleInfo.GetRuleName().startsWith( "at_expectation" ), true, true );
	            }
        	}
        }
	}
	
	static {
	    
        try {
            //Changed how the native libraries are loaded in order to support Single Process Launcher (SPL) in
            //where a single JVM contains multiple class loaders.  That causes this static block to be called at least
            //twice (once from the GIFT Dashboard application and once from the Domain module), however the libraries
            //only need to be loaded once and not throw an exception the subsequent times.  In addition the loadedMisLibraries
            //needs to be set to true. For more info see ticket #1745.
          
            logger.info("Loading SIMILE DLLs");
              
            String clipsName = "CLIPS", boostName = "boost_system-vc120-mt-1_55", dataTypeName = "ECSCL_Data_Type_JSON",
                    clipsRouterName = "SIMILE_CLIPS_Router", clipsWrapperName = "SIMILE_CLIPS_Wrapper", simileCore = "SimileCore",
                    simileParser = "SimileParser_FlexBison";
            
            System.loadLibrary(clipsName);
            System.loadLibrary(boostName);
            System.loadLibrary(dataTypeName);
            System.loadLibrary(clipsRouterName);
            System.loadLibrary(clipsWrapperName);
            System.loadLibrary(simileCore);
            System.loadLibrary(simileParser);
            
            logger.info("Finished loading SIMILE dlls");

      }catch(Throwable e){
          logger.error("Failed to load the SIMILE Libraries. Attached is the callstack. Are you sure you installed the VC2013 Redistributable? (provided for you in GIFT\\external\\simile\\vcredist_x86.exe)  Please restart GIFT afterwards.", e);
          logger.error("Failed to load from libraries from:\n"+System.getProperty("java.library.path"));          
      }
		
	}
	
	/**
	 * Loads the SIMILE Interface native library (dll).
	 * The reason this is separated from the other native libraries being loaded is due to the fact
	 * that this library needs to be loaded by the classloader that will be actually using the SIMILE native
	 * methods.  If this is called by the Dashboard classloader during SIMILE condition validation an unsatisfiedlinkerror
	 * exception will be thrown.
	 */
	private static void loadInterfaceDll(){
	    
        try {
            String simileInterface = "Simile_Interface";
            System.loadLibrary(simileInterface);
            loadedSimileInterfaceLibrary = true;

        } catch(Throwable e) {
            logger.error("Failed to load the SIMILE Libraries. Attached is the callstack. Are you sure you installed the VC2013 Redistributable? (provided for you in GIFT\\external\\simile\\vcredist_x86.exe)  Please restart GIFT afterwards.", e);
            logger.error("Failed to load from libraries from:\n"+System.getProperty("java.library.path"));            
        }
	}
	
	private SimileJNI(){
		
	}
	
	private static SimileJNI _instance= new SimileJNI();
	//native methods
	public native boolean Init();
	private native boolean parsefile(String path);
	public native boolean StartSimile();
	public native boolean StopSimile();
	private native boolean modifyFact(String name, Object[] attributes);
	private native boolean createFact(String name, String type, Object[] attributes);
	private native boolean SetRuleCallBack(RuleInfoCB ruleInfoCB);
	
	public boolean ModifyFact(String name, Map<String,Object> attributes){
		int numattrs=attributes.size();
		Object attrs []= new Object[numattrs*2];
		int i;
		Iterator<Entry<String,Object>> it= attributes.entrySet().iterator();
		for (i=0; i<numattrs;i++ ){
			Map.Entry<String,Object> pair=it.next();
			attrs[2*i]=pair.getKey();
			attrs[2*i+1]=pair.getValue().toString();
		}
		return modifyFact( name,  attrs);
	}
	
	public boolean CreateFact(String name, String type, Map<String,Object> attributes){
		int numattrs=attributes.size();
		Object attrs []= new Object[numattrs*2];
		int i;
		Iterator<Entry<String,Object>> it= attributes.entrySet().iterator();
		for (i=0; i<numattrs;i++ )
		{
			Map.Entry<String,Object> pair=it.next();
			attrs[2*i]=pair.getKey();
			attrs[2*i+1]=pair.getValue().toString();
		}
		return createFact( name,  type,  attrs);
	}
	
	public boolean CompileScript(String filepath){
		if(!filepath.endsWith(".ixs")){
			return false;
		}
		return parsefile( filepath);
	}
	
	/**
     * provides the equivalent of the Simile UnitTests in the java environment
	 * @param args not used
     */
	public static void main(String[] args){
		String name ="p1";
		String type= "EntityState";
		Map <String,Object> attributes= new HashMap<String,Object>();
		attributes.put("entityID._entityID",1 );
		attributes.put( "entityID._simAddr._siteID",43068);
		attributes.put("entityID._simAddr._appID",13553 );
		attributes.put("forceID",1 );
		attributes.put( "entityType._entityKind",3);
		attributes.put( "entityType._domain",1);
		attributes.put( "entityType._country",225);
		attributes.put( "entityType._category",1);
		attributes.put( "entityType._subcategory",1);
		attributes.put( "entityType._specific",3);
		attributes.put( "entityType._extra",1);
		attributes.put( "linearVel._x",0);
		attributes.put("linearVel._y",0 );
		attributes.put("linearVel._z",0 );
		attributes.put("location._x",3767068.5 );
		attributes.put( "location._y",-3163452.25);
		attributes.put("location._z",4046284.5 );
		attributes.put("orientation._x",0 );
		attributes.put( "orientation._y",0);
		attributes.put("orientation._z",0 );
		attributes.put("progressTimeFail",false );
		attributes.put( "progressTime",0);
		attributes.put( "appearance._posture._value",1);
		attributes.put("appearance._posture._name","Uprightstandingstill" );
		attributes.put("appearance._posture._displayName","standingstill" );
		attributes.put( "appearance._damage._value",0);
		attributes.put("appearance._damage._name","NoDamage" );
		attributes.put("appearance._damage._displayName","NoDamage" );
		attributes.put("checkpointProgress","UNKNOWN" );
		attributes.put("correctBoundaryLocation",true );
		attributes.put("boundaryViolationTimeFail",false );
		attributes.put("boundaryViolationTime",0 );
		attributes.put("boundaryViolationCountFail",false );
		attributes.put("boundaryViolationCount",0 );
		attributes.put("correctPosture",true );
		attributes.put("postureViolationTimeFail",false );
		attributes.put( "postureViolationTime",0);
		attributes.put( "postureViolationCount",0);
		attributes.put( "clearedPaceCheckpoint",false);
		attributes.put("clearedProgCheckpoint",false );
		attributes.put("isPaceGood",true );
		attributes.put( "clearedCorridor",false);
		attributes.put( "currentCheckpointProgress","EARLY");
			
		SimileJNI SIMILE= SimileJNI.GetInstance();
		
        if(SIMILE.parsefile("C:\\Development\\simile_2014\\Scripts\\vbsConversionNew.ixs"))
        {
        	System.out.println("FileParsed");
        }
		// Create the triggered rule listener.
		SIMILE.CreateTriggeredRuleListener( new SimileJNI.RuleInfoCB(){
		    
			@Override
			public void TriggeredRuleInfo( SimileJNI.RuleInfo ruleInfo ){
				SimulationTriggeredRuleInfo( ruleInfo );
			}
		} );
		
		_TriggeredRuleCallbackMap = new HashMap<String, SimileResultsCb>();
		
		if(SIMILE.StartSimile()){
			System.out.println("SimileStarted");
		}
		
		if(SIMILE.CreateFact(name, type, attributes)){
			System.out.println("CreatedFact");
		}
		
		attributes.clear();
		attributes.put("correctBoundaryLocation", false );
		attributes.put( "boundaryViolationTimeFail", false);
		attributes.put("boundaryViolationTime",200 );
		attributes.put("boundaryViolationCountFail",false );
		attributes.put("boundaryViolationCount", 4 );
		attributes.put("correctPosture",false );
		attributes.put( "postureViolationTime", 200);
		attributes.put( "postureViolationCount", 1);
		attributes.put("clearedProgCheckpoint",true );
		attributes.put("currentCheckpointProgress", "LATE" );
		attributes.put("clearedCorridor", true );
		attributes.put("progressTime",600 );
		attributes.put("clearedPaceCheckpoint",true );
		attributes.put("isPaceGood",true );
				
		if(SIMILE.ModifyFact(name, attributes))	{
			System.out.println("FactModified");
		}
		
		SIMILE.StopSimile();
		
	}
	
	public static SimileJNI GetInstance() {
		return SimileJNI._instance;
	}
	
	public boolean CreateTriggeredRuleListener( RuleInfoCB ruleInfoCB ) {
	    
	    //make sure the SIMILE interface is loaded (but only by the classloader that needs to call the native methods)
        loadInterfaceDll();
        
	    if(loadedMisLibraries && loadedSimileInterfaceLibrary) {
	        //TODO check input for errors
	        return SetRuleCallBack(ruleInfoCB);
	    }else{
	        logger.error("SIMILE Libraries haven't been loaded");
	    }
	    
	    return false;
	}
}
