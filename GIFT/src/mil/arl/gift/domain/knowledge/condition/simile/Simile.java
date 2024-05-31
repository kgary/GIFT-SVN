/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition.simile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonStateJSON;
import mil.arl.gift.common.ta.state.GenericJSONState.CommonWeaponJSON;
import mil.arl.gift.domain.knowledge.condition.simile.SimileJNI.RuleInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Simile object GIFT implementation.
 * 
 * @author asanchez
 * 
 */
public class Simile {
    /**
     * Callback for the triggered rule information obtained from the Simile
     * library.
     * 
     * @param ruleInfo
     *            - Holds the triggered rule information.
     */
    private static void SimulationTriggeredRuleInfo(RuleInfo ruleInfo) {
        // System.out.println( "Triggered rule name: " + ruleInfo.GetRuleName()
        // );

        if (ruleInfo != null) {
            if (ruleInfo.GetRuleName() != null
                    && ruleInfo.GetRuleName().length() > 0) {
                // Send it to the corresponding condition.
                SimileResultsCb condCb = _TriggeredRuleCallbackMap.get(ruleInfo
                        .GetRuleName());
                if (condCb != null) {
                    condCb.SimulationResults(
                            ruleInfo.GetRuleName().startsWith(AT_EXPECTATION),
                            true, true);
                }
            }
        }
    }

    private interface SimileResultsCb {
        /**
         * Receives a processed event from Simile.
         * 
         * @param atExpectationAssessment
         *            - True if the triggered concept is an 'At Expectation'
         *            concept.
         * @param isSatisfied
         *            - True if the condition has been satisfied.
         * @param isConditionCompleted
         *            - True if the concept has been fulfilled.
         */
        public void SimulationResults(Boolean atExpectationAssessment,
                Boolean isSatisfied, Boolean isConditionCompleted);
    }

    /***************************************
     * Private Information Classes *
     ***************************************/
    
    /**
     * Set of all Objects that have been created for gift
     */
    private Set<String> _createdObjectIds = new HashSet<String>();

    /**
     * boolean denotes if a file has been loaded for Simile
     */
    private boolean _SimileLoaded = false;

    private boolean _UpdateTimer = true;
    /**
     * The callback for when Simile needs to pass results back to GIFT.
     * 
     * @author asanchez
     * 
     */

    /*****************************
     * Private Constants * /
     *****************************/

    ArrayList<String> _ValidMessageTypes;
    
    private static final String CONCEPT = "Concept";
    private static final String OBJECT_TYPE = "objectType";
    private static final String AT_EXPECTATION = "at_expectation";
    private static final String GIFT_SIM = "GiftSim";
    private static final String ENTITY_STATE = "EntityState";
    private static final String WEAPON_FIRE = "WeaponFire";
    private static final String TC3_OBJ_ID = "TC3ObjectId";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final String CREATE_OBJECT = "CreateObject";
    private static final String SIMULATION = "Simulation";
    private static final String TIME = "time";
    private static final String CUSTOM_EVENT = "CustomEvent";

    /**************************
     * Private Fields *
     **************************/

    /**
     * Simile object's current instance.
     */
    private final static Simile _Instance = new Simile();

    /**
     * The Simile library interface.
     */
    private SimileJNI _Simile = null;

    /**
     * Holds all the messages' unique Id already processed.
     */
    private List<UUID> _ProcessedMessages = null;

    /**
     * Flag that checks if Simile is running.
     */
    private Boolean _SimileRunning = false;

    private long lastTimeInSeconds;

    List<String> loadedScriptFiles;

    String lastLoadedScript;

    /**
     * Holds the concept names from the configuration file.
     */
    private static List<String> _ConceptsList = new ArrayList<String>();

    /**
     * Maps the GIFT condition keys to their callback.
     */
    private static Map<String, SimileResultsCb> _TriggeredRuleCallbackMap = null;

    /**
     * Instance of the logger.
     */
    private static Logger logger = LoggerFactory.getLogger(Simile.class);

    private List<Integer> disEntityIds;

    String scriptPath = null;

    /**
     * Default constructor.
     */
    private Simile() {
        loadedScriptFiles = new ArrayList<String>();
        disEntityIds = new ArrayList<Integer>();
        _Simile = SimileJNI.GetInstance();
        _ValidMessageTypes = new ArrayList<String>();
        _ValidMessageTypes.add("Start");
        _ValidMessageTypes.add("Stop");
        _ValidMessageTypes.add("CreateObject");
        _ValidMessageTypes.add("UpdateObject");
        _ValidMessageTypes.add("CustomEvent");
    }

    /***************************
     * Private Helpers *
     ***************************/

    /**
     * Reads the Simile script file and gets the concepts from it.
     * 
     * @param configFile
     *            - The Simile script file.
     */
    private void ReadConceptsFromSimileScript(FileProxy configFile) {
        String lineStr = null;
        String[] tokens = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(configFile.getInputStream()));
            scriptPath = configFile.getFileId();
            while ((lineStr = reader.readLine()) != null) {
                tokens = lineStr.trim().split(" ");
                if (tokens[0].startsWith(CONCEPT)) {
                    tokens[3] = tokens[3].replace("\"", "");

                    if (!_ConceptsList.contains(tokens[3])) {
                        _ConceptsList.add(tokens[3]);
                    }
                }
            }

            reader.close();
        } catch (FileNotFoundException ex) {
            logger.error("The Simile configuration file was not found!", ex);
        } catch (IOException ex) {
            logger.error("Unable to read the Simile configuration file!", ex);
        }
    }

    /**************************
     * Public Methods *
     ***************************/

    /**
     * Queries the instance to the Simile object class.
     * 
     * @return Simile - An instance to the Simile object class.
     */
    public static Simile GetInstance() {
        return _Instance;
    }

    /*********************************
     * Public Simile Methods *
     *********************************/
    
    /**
     * Initialize SIMILE JNI
     * This method was created with logic moved from SetupSimileForGIFT method because of the way java handles
     * loading native libraries in a single JVM when multiple class loaders are used (see ticket #1745).  Basically
     * the following code should not be called by class loaders that merely wish to validate a SIMILE condition.  See
     * other comments in this class and SimileJNI.java related to #1745. 
     */
    public void initialize(){
        
        // Create the triggered rule listener.
        if (!_Simile
                .CreateTriggeredRuleListener(new SimileJNI.RuleInfoCB() {
                    @Override
                    public void TriggeredRuleInfo(RuleInfo ruleInfo) {
                        SimulationTriggeredRuleInfo(ruleInfo);
                    }
                })) {
            throw new RuntimeException("Failed to parse the Simile script.  Are you sure you installed the VC2013 Redistributable? (provided for you in GIFT\\external\\simile\\vcredist_x86.exe).  Please restart GIFT afterwards.");
        }
        
        if (!_Simile.Init()) {
            throw new RuntimeException("Error Initializing Simile!");
        }

    }

    /**
     * Setups Simile with the information it needs to run the scenario.
     * 
     * @param simileInterfaceCondition
     *            - The Simile interface condition class.
     * @param conditionInput
     *            - The condition input information from the DKF.
     * @param courseFolder contains the course assets including the file used to configure this simile instance.
     * @throws IOException if there was a problem retrieving the SIMILE config file from the course folder
     */
    public void SetupSimileForGIFT(
            final SIMILEInterfaceCondition simileInterfaceCondition,
            generated.dkf.SIMILEConditionInput conditionInput, AbstractFolderProxy courseFolder) throws IOException {
        if (conditionInput == null) {
            logger.error("The Simile condition input is null!");
            return;
        }

        String currentPath = courseFolder.getFileId() + File.separator + conditionInput.getConfigurationFile();
        lastLoadedScript = currentPath;
        // We only want to load each script file once
        if (!_SimileLoaded || !loadedScriptFiles.contains(currentPath)) {
            scriptPath = currentPath;
            loadedScriptFiles.add(currentPath);

            if (simileInterfaceCondition == null) {
                logger.error("The Simile interface condition class is null!");
                return;
            }

            if (conditionInput.getConditionKey() == null
                    || conditionInput.getConditionKey().isEmpty()) {
                logger.error("The Simile condition key is invalid!");
                return;
            }

            if (conditionInput.getConfigurationFile() == null
                    || conditionInput.getConfigurationFile().isEmpty()) {
                logger.error("The Simile configuration file is invalid!");
                return;
            }

            // Read the Simile script file and store the concept names for
            // checking against the dkf concepts.
            FileProxy configFile = courseFolder.getRelativeFile(conditionInput.getConfigurationFile());
            ReadConceptsFromSimileScript(configFile);

            // The condition Id to Simile callback map.
            _TriggeredRuleCallbackMap = new HashMap<String, SimileResultsCb>();

            // Holds the processed messages' Ids.
            _ProcessedMessages = new ArrayList<UUID>();

            //this must be set here so that the next caller (SIMILE condition class) doesn't also attempt
            //to setup SIMILE for GIFT (hence the original if !_SimileLoaded condition above.
            _SimileLoaded = true;
        }

        // Check that the current concept in the DKF is available in the Simile
        // script.
        if (!_ConceptsList.contains(conditionInput.getConditionKey())) {
            logger.error("\"" + conditionInput.getConditionKey()
                    + "\" is not available in "
                    + conditionInput.getConfigurationFile() + "!");
            return;
        }
        // Set up the result callback mapping.
        SimileResultsCb conditionCb = new SimileResultsCb() {
            @Override
            public void SimulationResults(Boolean atExpectationAssessment,
                    Boolean isSatisfied, Boolean isConditionCompleted) {
                simileInterfaceCondition.SimulationResults(
                        atExpectationAssessment, isSatisfied,
                        isConditionCompleted);
            }
        };

        try {
            // Set the current rule concept mapping to its below_expectation and
            // at_expectation results.
            _TriggeredRuleCallbackMap.put(conditionInput.getConditionKey(),
                    conditionCb);
            _TriggeredRuleCallbackMap.put(
                    "at_expectation_" + conditionInput.getConditionKey(),
                    conditionCb);
        } catch (Exception ex) {
            logger.error(
                    "Error while setting up Simile's condition keys during setup!",
                    ex);
        }

    }

    @SuppressWarnings("unchecked")
    public void addFactToSIMILE(final GenericJSONState stateMessage) {
        JSONArray dataArray = stateMessage.getArrayById(CommonStateJSON.DATA);
        if (dataArray != null) {
            String objectId = null;
            String objectType = null;
            Map<String, Object> simileData = new HashMap<String, Object>();
            for (int i = 0; i < dataArray.size(); ++i) {
                JSONObject current = (JSONObject) dataArray.get(i);
                if (current.containsKey(CommonStateJSON.OBJECT_ID)) {
                    objectId = (String) current.get(CommonStateJSON.OBJECT_ID);
                } else if (current.containsKey(OBJECT_TYPE)) {
                    objectType = (String) current.get(OBJECT_TYPE);
                    if (objectType.compareTo(GIFT_SIM) == 0) {
                        _UpdateTimer = false;
                    }
                } else {
                    if (current.containsKey(CommonWeaponJSON.FIRE_ENTITY_ID_ID)) {
                        objectType = WEAPON_FIRE;
                    } else if (current.containsKey(CommonStateJSON.ENTITY_ID_ID)) {
                        objectType = ENTITY_STATE;
                    }
                    Set<String> keys = current.keySet();
                    for (String key : keys) {
                        Object value = current.get(key);
                        simileData.put(key, value);
                    }
                }
            }
            if (objectId != null && objectType != null) {
                try {
                    _Simile.CreateFact(objectId, objectType, simileData);
                    _createdObjectIds.add(objectId);
                } catch (Exception ex) {
                    logger.error(
                            "Error while creating a fact in Simile for GIFT!",
                            ex);
                }

            }

        }
    }

    public void updateFactInSIMILE(GenericJSONState stateMessage) {
        JSONArray dataArray = stateMessage.getArrayById(CommonStateJSON.DATA);
        if (dataArray != null) {
            String objectId = null;

            Map<String, Object> simileData = new HashMap<String, Object>();
            for (int i = 0; i < dataArray.size(); ++i) {
                JSONObject current = (JSONObject) dataArray.get(i);
                if (current.containsKey(CommonStateJSON.OBJECT_ID)) {
                    objectId = (String) current.get(CommonStateJSON.OBJECT_ID);
                } else if (current.containsKey(CommonWeaponJSON.FIRE_ENTITY_ID_ID)) {
                    addFactToSIMILE(stateMessage);
                    return;
                } else {
                    if (current.containsKey(CommonStateJSON.ENTITY_ID_ID)) {
                        
                        Object entityId = current.get(CommonStateJSON.ENTITY_ID_ID); 
                        Integer oId;
                        if(entityId instanceof Integer){
                            oId = (Integer)entityId;
                        }else{
                            oId = ((Long)entityId).intValue();
                        }

                        if (!disEntityIds.contains(oId)) {
                            disEntityIds.add(oId);
                            addFactToSIMILE(stateMessage);
                            return;
                        }
                        
                    }else if (current.containsKey(TC3_OBJ_ID)) {
                        /*
                         * String id = (String)current.get("TC3ObjectId");
                         * if(id.compareTo("Player1") != 0 ||
                         * id.compareTo("Simulation") != 0) { return; } else
                         * if(!TC3ObjectIds.contains(id)) {
                         * TC3ObjectIds.add(id); current.remove("TC3ObjectId");
                         * addFactToSIMILE(stateMessage); return; }
                         */
                        current.remove(TC3_OBJ_ID);
                    }

                    @SuppressWarnings("unchecked")
                    Set<String> keys = current.keySet();
                    for (String key : keys) {
                        Object value = current.get(key);
                        simileData.put(key, value);
                    }
                }
            }

            if (objectId != null) {
                try {
                    _Simile.ModifyFact(objectId, simileData);
                } catch (Exception ex) {
                    logger.error(
                            "Error while creating a fact in Simile for GIFT!",
                            ex);
                }

            }
        }

    }

    /**
     * Processes the messages map sent to Simile by the Gateway module.
     * 
     * @param stateMessage
     *            - The state message to process.
     */
    public void ProcessMessage(final GenericJSONState stateMessage) {
        
        if (!_SimileLoaded) {
            if (_SimileRunning) {
                logger.error("Simile was not loaded while trying to process messages!");
            }

            return;
        }

        if (stateMessage == null) {
            logger.error("The current message was null while processing messages for Simile!");
            return;
        }

        // Do not process the same message more than once.
        if (_ProcessedMessages.contains(stateMessage.getUUID())) {
            return;
        }

        if (!_SimileRunning) {
            // for (int i = 0; i < loadedScriptFiles.size(); ++i) {
            // String currentPath = loadedScriptFiles.get(i);
            // _Simile.CompileScript(currentPath);
            // }
            _Simile.CompileScript(lastLoadedScript);
            _Simile.StartSimile();
            _SimileRunning = true;

        }

        _ProcessedMessages.add(stateMessage.getUUID());

        String simCommand = stateMessage.getStringById(CommonStateJSON.SIM_CMD);
        if (_ValidMessageTypes.contains(simCommand)) {
            if (simCommand.compareTo(START) == 0 && !_SimileRunning) {
                try {
                    _SimileRunning = true;
                    return;
                } catch (Exception ex) {
                    logger.error("Error while starting Simile for GIFT!", ex);
                }

            } else if (simCommand.compareTo(CREATE_OBJECT) == 0) {
                addFactToSIMILE(stateMessage);
            }

            else if (simCommand.compareTo(CommonStateJSON.UPDATE_OBJ) == 0) {

                JSONArray dataArray = stateMessage.getArrayById(CommonStateJSON.DATA);
                if (dataArray != null) {
                    String objectId = null;
                    for (int i = 0; i < dataArray.size(); ++i) {
                        JSONObject current = (JSONObject) dataArray.get(i);
                        if (current.containsKey(CommonStateJSON.OBJECT_ID)) {
                            objectId = (String) current.get(CommonStateJSON.OBJECT_ID);
                            if (objectId.compareTo(SIMULATION) == 0)
                                _UpdateTimer = false;
                        }
                    }
                }

                updateFactInSIMILE(stateMessage);
                if (_UpdateTimer) {
                    lastTimeInSeconds = (System.currentTimeMillis() - lastTimeInSeconds);
                    if (lastTimeInSeconds < 6000000) {
                        long value = lastTimeInSeconds / 1000;
                        Map<String, Object> simileData = new HashMap<String, Object>();
                        simileData.put(TIME, value);

                        _Simile.ModifyFact(SIMULATION, simileData);
                    }
                }
            } else if (simCommand.compareTo(CUSTOM_EVENT) == 0) {
                // The TC3 Simile implementation for GIFT does not use any
                // custom events.
            } else if (simCommand.compareTo(STOP) == 0) {
                StopSimile();
                _SimileRunning = false;
            }
        } else {
            // Unknown command sent from TC3.
            logger.warn("A command sent from TC3 is unknown while processing the messages for Simile!");
        }
    }

    /**
     * Manual call to stop Simile.
     */
    public void StopSimile() {
        if (_SimileRunning) {
            try {
                _Simile.StopSimile();
                _TriggeredRuleCallbackMap.clear();
                _ConceptsList.clear();
                _ProcessedMessages.clear();
                _SimileLoaded = false;
                _SimileRunning = false;
                disEntityIds.clear();
                loadedScriptFiles.clear();
            } catch (Exception ex) {
                logger.error("Error while stopping Simile for GIFT!", ex);
            }
        }
    }
}
