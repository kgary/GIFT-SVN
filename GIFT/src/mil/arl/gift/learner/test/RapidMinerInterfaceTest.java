/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.test;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonStateJSON;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface;
import mil.arl.gift.learner.common.rapidminer.TC3DataModel;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface.RapidMinerProcess;

/**
 * Tests the RapidMiner interface by running a process with made up input data.
 * 
 * @author mhoffman
 *
 */
public class RapidMinerInterfaceTest {

    // This key must match the rapidminer model.
    private static final String OUTPUT_KEY = "confidence(1)";
    
    /**
     * Runs a test of the RapidMiner API by creating input that is given to a
     * RapidMiner process.  That process outputs a result that is then displayed
     * through system.out.
     * 
     * @param args not used
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args){
        
        System.out.println("Running RapidMiner API test...\n");
        
        try{
            //
            // Build a data model - this is similar to receiving a series of TC3 game state
            //
            TC3DataModel dataModel = new TC3DataModel();
            GenericJSONState state1 = new GenericJSONState();
            JSONArray dataValuesState1 = new JSONArray();
            
            
            // The TC3 training application state messages received are a combination of JSONObjects that are 
            // inserted into a JSONArray with a 'data' tag.
            dataValuesState1.add(createJSONObject(TC3DataModel.BLOOD_VOLUME_LABEL, "20.0"));
            dataValuesState1.add(createJSONObject(TC3DataModel.HEART_RATE_LABEL, "70.0"));
            dataValuesState1.add(createJSONObject(TC3DataModel.ISSAFE_LABEL, "1"));
            
            dataValuesState1.add(createJSONObject(TC3DataModel.AVG_BLOODVOLUME_LABEL, "4700.0"));
            dataValuesState1.add(createJSONObject(TC3DataModel.MIN_HEARTRATE_LABEL, "70.0"));
            dataValuesState1.add(createJSONObject(TC3DataModel.ISSAFE_LABEL, "1"));
            
            dataValuesState1.add(createJSONObject(TC3DataModel.IS_UNDER_COVER_LABEL, "1"));  //TODO: does this come as 'true'/'false' strings?  Check other attributes as well...
            dataValuesState1.add(createJSONObject(TC3DataModel.WOUND_EXPOSED_LABEL, "1"));
            dataValuesState1.add(createJSONObject(TC3DataModel.BLEED_RATE_LABEL, "10.0"));
            
            state1.setValueById(CommonStateJSON.DATA, dataValuesState1);
            
            GenericJSONState state2 = new GenericJSONState();
            JSONArray dataValuesState2 = new JSONArray();
            dataValuesState2.add(createJSONObject(TC3DataModel.BLOOD_VOLUME_LABEL, "15.0"));
            dataValuesState2.add(createJSONObject(TC3DataModel.HEART_RATE_LABEL, "75.0"));
            dataValuesState2.add(createJSONObject(TC3DataModel.ISSAFE_LABEL, "0"));
            
            dataValuesState2.add(createJSONObject(TC3DataModel.AVG_BLOODVOLUME_LABEL, "20.0"));
            dataValuesState2.add(createJSONObject(TC3DataModel.MIN_HEARTRATE_LABEL, "70.0"));
            dataValuesState2.add(createJSONObject(TC3DataModel.ISSAFE_LABEL, "1"));
            
            dataValuesState2.add(createJSONObject(TC3DataModel.IS_UNDER_COVER_LABEL, "0"));  //TODO: does this come as 'true'/'false' strings?  Check other attributes as well...
            dataValuesState2.add(createJSONObject(TC3DataModel.WOUND_EXPOSED_LABEL, "0"));
            dataValuesState2.add(createJSONObject(TC3DataModel.BLEED_RATE_LABEL, "10.0"));  
            
            state2.setValueById(CommonStateJSON.DATA, dataValuesState2);
            
            dataModel.addState(state1);
            dataModel.addState(state2);
            
            RapidMinerProcess ANXIOUS_PROCESS = new RapidMinerProcess(RapidMinerInterface.ANXIOUS_PROCESS_FILE, OUTPUT_KEY);
                 
            //
            // Apply the RapidMiner process to the data
            //
            double anxiousConfidence = ANXIOUS_PROCESS.runProcess(dataModel.getIOContainer());
            if(anxiousConfidence > 0.5){
                System.out.println("ANXIOUS = HIGH, "+anxiousConfidence);
            }else{
                System.out.println("ANXIOUS = LOW, "+anxiousConfidence);
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("\nFinished.");
    }
    
    /**
     * Creates a JSONObject based on the key/value pair (as strings) that are passed in.
     * 
     * @param key - The key for the json object.  Cannot be null.
     * @param value - The value for the json object (as a string).  Cannot be null.
     * @return JSONObject - Return a json object representing the key/value pair that were created.  Cannot be null.
     */
    @SuppressWarnings("unchecked")
    private static JSONObject  createJSONObject(String key, String value) {
        JSONObject obj = new JSONObject();
        
        obj.put(key,  value);
        return obj;
    }
}
