/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.common.rapidminer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonStateJSON;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.Ontology;

/**
 * This class contains the RapidMiner data model for TC3 training application game
 * states.  It can be used to convert TC3 JSON encoded game state into RapidMiner
 * data model table.  That table can then be used as input to a RapidMiner process
 * and model.  Details on the table can be found here:  
 * http://rapid-i.com/wiki/index.php?title=Integrating_RapidMiner_into_your_application
 * 
 * @author mhoffman
 *
 */
public class TC3DataModel implements DataModel{
        
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TC3DataModel.class);
    
    /**
     * The TC3 game state attributes (unique names used by JSON mapping)
     * Note: ideally this would have been defined by TC3 developers somewhere else in GIFT or a config file.
     */
    public static final String BLOOD_VOLUME_LABEL          = "BloodVolume";
    public static final String HEART_RATE_LABEL            = "HeartRate";
    public static final String ISSAFE_LABEL                = "isSafe";
    public static final String BLEED_RATE_LABEL            = "BleedRate";
    public static final String LEFT_LUNG_EFF_LABEL         = "LeftLungEfficiency";
    public static final String SYSTOLIC_LABEL              = "Systolic";
    public static final String OUT_OF_COVER_TIME_LABEL     = "outOfCoverTime";
    public static final String IS_UNDER_COVER_LABEL        = "isUnderCover";
    public static final String WITH_UNIT_LABEL             = "withUnit";
    public static final String UNDER_FIRE_LABEL            = "underFire";
    public static final String UNDER_TACT_FIELD_CARE_LABEL = "underTacticalFieldCare";
    public static final String WOUND_EXPOSED_LABEL         = "WoundExposed";
    public static final String HAS_REQUESTED_HELP_LABEL    = "hasRequestedHelp";
    public static final String ROLLED_LABEL                = "Rolled";
    public static final String REQ_SECURITY_SWEEP_LABEL    = "RequestSecuritySweep";
    public static final String MOVE_TO_CPP_LABEL           = "MoveToCCP";
    public static final String TOURNIQUETS_APPLIED_LABEL   = "tourniquetsApplied";
    public static final String BLOOD_SWEEP_ACTION_LABEL    = "Bloodsweepaction";
    public static final String REQUEST_CASEVAC_LABEL       = "Requestcasevac";
    public static final String BREATHING_CHECKED_LABEL     = "Breathingchecked";
    public static final String HAS_COMMUNICATED_LABEL      = "hasCommunicated";
    public static final String VITALS_CHECK_LABEL          = "vitalsCheck";
    public static final String HAS_FIRED_WEAPON_LABEL      = "hasFiredWeapon";
    public static final String BANDAGE_APPLIED_LABEL        = "BandageApplied";
    
    //there are processed attributes - hopefully remove them in future 
    public static final String AVG_BLOODVOLUME_LABEL      = "Average of BloodVolume";
    public static final String MIN_HEARTRATE_LABEL        = "Min of HeartRate";
    public static final String SUM_ISSAFE_LABEL           = "Sum of isSafe";
    public static final String MS_SINCE_LAST_MOD_LABEL     = "mssincelast-mod";
    

    
    /**
     * RapidMiner attributes used a column headers in the data model table.
     */
    private static Attribute bloodVolAttr          = AttributeFactory.createAttribute(BLOOD_VOLUME_LABEL, Ontology.REAL);
    private static Attribute heartRateAttr         = AttributeFactory.createAttribute(HEART_RATE_LABEL, Ontology.REAL);
    private static Attribute isSafeAttr            = AttributeFactory.createAttribute(ISSAFE_LABEL, Ontology.REAL);
    private static Attribute bleedRateAttr         = AttributeFactory.createAttribute(BLEED_RATE_LABEL, Ontology.REAL);
    private static Attribute leftLungEffAttr       = AttributeFactory.createAttribute(LEFT_LUNG_EFF_LABEL, Ontology.REAL);
    private static Attribute systolicAttr         = AttributeFactory.createAttribute(SYSTOLIC_LABEL, Ontology.REAL);
    private static Attribute outOfCoverTimeAttr         = AttributeFactory.createAttribute(OUT_OF_COVER_TIME_LABEL, Ontology.REAL);
    private static Attribute isUnderCoverAttr         = AttributeFactory.createAttribute(IS_UNDER_COVER_LABEL, Ontology.REAL);
    private static Attribute withUnitAttr         = AttributeFactory.createAttribute(WITH_UNIT_LABEL, Ontology.REAL);
    private static Attribute underFireAttr         = AttributeFactory.createAttribute(UNDER_FIRE_LABEL, Ontology.REAL);
    private static Attribute underTactFieldCareAttr         = AttributeFactory.createAttribute(UNDER_TACT_FIELD_CARE_LABEL, Ontology.REAL);
    private static Attribute woundExposedAttr         = AttributeFactory.createAttribute(WOUND_EXPOSED_LABEL, Ontology.REAL);
    private static Attribute hasRequestedHelpAttr         = AttributeFactory.createAttribute(HAS_REQUESTED_HELP_LABEL, Ontology.REAL);
    private static Attribute rolledAttr         = AttributeFactory.createAttribute(ROLLED_LABEL, Ontology.REAL);
    private static Attribute requestSecuritySweepAttr         = AttributeFactory.createAttribute(REQ_SECURITY_SWEEP_LABEL, Ontology.REAL);
    private static Attribute moveToCPPAttr         = AttributeFactory.createAttribute(MOVE_TO_CPP_LABEL, Ontology.REAL);
    private static Attribute tourniquetsAppliedAttr         = AttributeFactory.createAttribute(TOURNIQUETS_APPLIED_LABEL, Ontology.REAL);
    private static Attribute bloodSweepActionAttr         = AttributeFactory.createAttribute(BLOOD_SWEEP_ACTION_LABEL, Ontology.REAL);
    private static Attribute requestCasevacAttr         = AttributeFactory.createAttribute(REQUEST_CASEVAC_LABEL, Ontology.REAL);
    private static Attribute breathingCheckedAttr         = AttributeFactory.createAttribute(BREATHING_CHECKED_LABEL, Ontology.REAL);
    private static Attribute hasCommunicatedAttr         = AttributeFactory.createAttribute(HAS_COMMUNICATED_LABEL, Ontology.REAL);
    private static Attribute vitalsCheckAttr         = AttributeFactory.createAttribute(VITALS_CHECK_LABEL, Ontology.REAL);
    private static Attribute hasFiredWeaponAttr         = AttributeFactory.createAttribute(HAS_FIRED_WEAPON_LABEL, Ontology.REAL);
    private static Attribute bandageAppliedAttr         = AttributeFactory.createAttribute(BANDAGE_APPLIED_LABEL, Ontology.REAL);
    
    //these are processed attributes - hopefully remove them in future
    private static Attribute mssincelastAttr       = AttributeFactory.createAttribute(MS_SINCE_LAST_MOD_LABEL, Ontology.REAL);
    private static Attribute avgBloodVolumeAttr       = AttributeFactory.createAttribute(AVG_BLOODVOLUME_LABEL, Ontology.REAL);
    private static Attribute minHeartRateAttr       = AttributeFactory.createAttribute(MIN_HEARTRATE_LABEL, Ontology.REAL);
    private static Attribute sumOfIsSafeAttr       = AttributeFactory.createAttribute(SUM_ISSAFE_LABEL, Ontology.REAL);
    
    /**
     * The exact ordering of the column labels in the data model table
     * Note: the order is important when populating the rows of the table because the value for
     * an attribute needs to appear under the correct column (i.e. index)
     */
    protected static List<Attribute> attributes = new LinkedList<Attribute>();
    
    
    
    static{
        attributes.add(bloodVolAttr);
        attributes.add(heartRateAttr);
        attributes.add(isSafeAttr);
        attributes.add(bleedRateAttr);
        attributes.add(leftLungEffAttr);
        attributes.add(systolicAttr);
        attributes.add(outOfCoverTimeAttr);
        attributes.add(isUnderCoverAttr);
        attributes.add(withUnitAttr);
        attributes.add(underFireAttr);
        attributes.add(underTactFieldCareAttr);
        attributes.add(woundExposedAttr);
        attributes.add(hasRequestedHelpAttr);
        attributes.add(rolledAttr);
        attributes.add(requestSecuritySweepAttr);
        attributes.add(moveToCPPAttr);
        attributes.add(tourniquetsAppliedAttr);
        attributes.add(bloodSweepActionAttr);
        attributes.add(requestCasevacAttr);
        attributes.add(breathingCheckedAttr);
        attributes.add(hasCommunicatedAttr);
        attributes.add(vitalsCheckAttr);
        attributes.add(hasFiredWeaponAttr);
        attributes.add(bandageAppliedAttr);
        
        attributes.add(mssincelastAttr);
        
        //hopefully these will go away once the RapidMiner process evolves
        attributes.add(avgBloodVolumeAttr);
        attributes.add(minHeartRateAttr);
        attributes.add(sumOfIsSafeAttr);
    }
    
    /**
     * mapping of unique TC3 game state attribute label to the RapidMiner attribute associated with its values
     * This is used when a TC3 game state is added and needs to be processed (i.e. values added to correct columns
     * of data model table)
     */
    protected static Map<String, Attribute> labelToAttribute = new HashMap<>();
    static{
        labelToAttribute.put(BLOOD_VOLUME_LABEL, bloodVolAttr);
        labelToAttribute.put(HEART_RATE_LABEL, heartRateAttr);
        labelToAttribute.put(ISSAFE_LABEL, isSafeAttr);
        labelToAttribute.put(BLEED_RATE_LABEL, bleedRateAttr);
        labelToAttribute.put(LEFT_LUNG_EFF_LABEL, leftLungEffAttr);
        labelToAttribute.put(SYSTOLIC_LABEL, systolicAttr);
        labelToAttribute.put(OUT_OF_COVER_TIME_LABEL, outOfCoverTimeAttr);
        labelToAttribute.put(IS_UNDER_COVER_LABEL, isUnderCoverAttr);
        labelToAttribute.put(WITH_UNIT_LABEL, withUnitAttr);
        labelToAttribute.put(UNDER_FIRE_LABEL, underFireAttr);
        labelToAttribute.put(UNDER_TACT_FIELD_CARE_LABEL, underTactFieldCareAttr);
        labelToAttribute.put(WOUND_EXPOSED_LABEL, woundExposedAttr);
        labelToAttribute.put(HAS_REQUESTED_HELP_LABEL, hasRequestedHelpAttr);
        labelToAttribute.put(ROLLED_LABEL, rolledAttr);
        labelToAttribute.put(REQ_SECURITY_SWEEP_LABEL, requestSecuritySweepAttr);
        labelToAttribute.put(MOVE_TO_CPP_LABEL, moveToCPPAttr);
        labelToAttribute.put(TOURNIQUETS_APPLIED_LABEL, tourniquetsAppliedAttr);
        labelToAttribute.put(BLOOD_SWEEP_ACTION_LABEL, bloodSweepActionAttr);
        labelToAttribute.put(REQUEST_CASEVAC_LABEL, requestCasevacAttr);
        labelToAttribute.put(BREATHING_CHECKED_LABEL, breathingCheckedAttr);
        labelToAttribute.put(HAS_COMMUNICATED_LABEL, hasCommunicatedAttr);
        labelToAttribute.put(VITALS_CHECK_LABEL, vitalsCheckAttr);
        labelToAttribute.put(HAS_FIRED_WEAPON_LABEL, hasFiredWeaponAttr);
        labelToAttribute.put(BANDAGE_APPLIED_LABEL, bandageAppliedAttr);
        
        labelToAttribute.put(MS_SINCE_LAST_MOD_LABEL, mssincelastAttr);
        
        //hopefully these will go away once the RapidMiner process evolves
        labelToAttribute.put(AVG_BLOODVOLUME_LABEL, avgBloodVolumeAttr);
        labelToAttribute.put(MIN_HEARTRATE_LABEL, minHeartRateAttr);
        labelToAttribute.put(SUM_ISSAFE_LABEL, sumOfIsSafeAttr);
    }
    
    /** 
     * contains the JSON states received by this data model, in the order received, no matter if it contained
     * information for the data model table or not
     */
    protected List<GenericJSONState> states = new ArrayList<>();
    
    /**
     * The RapidMiner data model table used as input to the Process in order to apply the model.
     */
    protected MemoryExampleTable table;
    
    /**
     * The exampleSet is a view of the data.  This is created during creation of the data model.
     */
    protected ExampleSet exampleSet;
    
    // $TODO$ nblomberg
    // This should NOT be the final implementation, however the current RapidMiner process is expecting a single
    // data row with all values on the same row.  What we're doing here, is simply overwriting the values
    // of the row as new trainingapp gamestate messages are received.  The row is never cleared or deleted,
    // only overwritten.  
    protected double[] data = new double[attributes.size()];
    
    public TC3DataModel(){        
        table = new MemoryExampleTable(attributes);
        
        /* The createExampleSet creates the view into the table based on the attributes.
         * Currently this means that the attributes/columns of the table are not expected
         * to change for this instance of the data model. 
        */ 
        exampleSet = table.createExampleSet();
    }
    
    /**
     * Add a new state to this data model.
     * 
     * @param state contains state information encoded in JSON format.
     * @throws Exception if there was a problem adding the state's attributes to the data model
     */
    @Override
    public void addState(Object state) throws Exception{
        
        if (state instanceof GenericJSONState) {
            
            GenericJSONState jsonState = (GenericJSONState) state;
            synchronized (states) {
                states.add(jsonState);
                addTableRow(jsonState);   
            }    
        } else {
            logger.error("Invalid state received for TC3DataModel.  Expected GenericJSONState, but received object of " + state);
        }
          
    }
    
    /**
     * Clear all the states known to this data model.
     */
    @Override
    public void clearStates(){
        
        synchronized (states) {
            states.clear();
            table.clear();
        }
    }
    
    /**
     * Adds the TC3 state content to the data model table being built by this class.
     * If the content has no values for the attributes represented by this data model than
     * no row will be added to the table.  Otherwise, the value for each attribute for this data
     * model found in the state object will be placed in the appropriate column of a new row.
     * 
     * @param state contains state information encoded in JSON format.
     * @throws Exception if there was a problem adding the state;s attributes to the data model.
     */
    protected void addTableRow(GenericJSONState state) throws Exception{
        
        
        //flag used to indicate if at least one column had data place in it (below)
        boolean updated = false;
            
        JSONObject obj = state.getJSONObject();
        logger.trace("Evaluating GenericJSONState JSONObject to determine if data should be added to our RapidMiner table: " + obj.toString());
        
        // $TODO$ nblomberg
        // For now we don't know what the contents of the genericjsonstate message are, so for each message,
        // we need to do an O(n^2) loop to find if the data has any matching attributes.  This may be an area
        // to optimize if needed.
        JSONArray msgData = (JSONArray)obj.get(CommonStateJSON.DATA);
        for (int x=0; x < msgData.size(); x++) {
        
            
            JSONObject msgObj = (JSONObject)msgData.get(x);
            
            // Iterate over each attribute we're interested in and get the value for it.
            for (String key : labelToAttribute.keySet()) {
                
                if (msgObj.containsKey(key)) {
      
                    Attribute attr = labelToAttribute.get(key);
                    
                    //get column index in table for this attribute
                    int index = attributes.indexOf(attr);
                    
                    if(index == -1){
                        throw new Exception("unable to find the attribute index for "+attr.getName()+".");
                    }
                    
                    double doubleValue;
                    Object value = msgObj.get(key);
                    if(value == null){
                        throw new Exception("The value for "+attr.getName()+" was null.");
                    }
                    
                    //process strings into doubles
                    if(value instanceof String){
                        String boolStr = (String)value;
                        // Convert booleans to 1 or 0s here.
                        if (boolStr.contentEquals(Boolean.TRUE.toString())) {
                            value = "1";
                        } else if (boolStr.contentEquals(Boolean.FALSE.toString())) {
                            value = "0";
                        }
                        
                        doubleValue = Double.parseDouble((String) value);
                    }else{
                        throw new Exception("Found unhandled value of "+value+" for attribute named "+attr.getName()+".");
                    }
                        
                     //place data in column at index
                    data[index] = doubleValue;
                    
                    updated = true;
                    logger.trace("Table updated with key: " + key + "  -- value is: " + value);
                }
                else {
                    // Uncomment to see ignored keys, however, leaving this commented out, because it really spams the logs. 
                    // logger.trace("Key with name of " + key + " is not found in the attribute list.  This key will not be added to the data.");                    
                }
            }
        }
            
            
           
        //only add a row to the table if at least 1 attribute had data
        if(updated){
            
            table.addDataRow(new DoubleArrayDataRow(data));
            logger.trace("RapidMiner MemoryExampleTable updated: " + table);
            logger.trace("Double Data: " + Arrays.toString(data));
            
        }

    }
    
    @Override
    public IOContainer getIOContainer(){
        
        synchronized (states) {
       
            
            // create a wrapper that implements the ExampleSet interface and
            // encapsulates your data
            // ...
            
            // Output the exampleset and exampletable (which gives the number of rows in the table).
            logger.trace("ExampleSet = " + exampleSet);
            logger.trace(" Example Table = " + exampleSet.getExampleTable());
            return new IOContainer(exampleSet);
        }
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TC3DataModel: ");
        sb.append("number-of-rows = ").append(table.size());
        sb.append("]");
        return sb.toString();
    }

}
