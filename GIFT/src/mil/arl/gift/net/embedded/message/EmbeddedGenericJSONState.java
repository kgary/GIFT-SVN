/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message;

import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.WeaponFire;

/**
 * The purpose of this class is to create a generic, data driven state class
 * that can be used to store any JSON compatible data. Allows the user to send
 * state to GIFT components without the need to create a new state class
 * per state type
 */
public class EmbeddedGenericJSONState {
    /**
     * The unique Id generated specifically to this state.
     */
    private UUID mUUID;

    /**
     * The JSONObject that contains the data is being stored in the state
     */
    private JSONObject mData = new JSONObject();

    /**
     * Basic Constructor
     */
    public EmbeddedGenericJSONState() {
        this(UUID.randomUUID());
    }

    /**
     * Use this constructor when the UUID is already known.
     *
     * @param uuid  The unique Id generated specifically to this state.
     */
    public EmbeddedGenericJSONState(UUID uuid){
        setUUID(uuid);
    }

    /**
     * Gets a boolean value by id
     *
     * @param id the id of the value being retrieved.
     *
     * @return the boolean value that was retrieved
     */
    public boolean getBooleanById(String id) {
        boolean value = false;

    	if (mData.containsKey(id)) {
    	    value = (boolean) mData.get(id);
    	}

    	return value;
    }

    /**
     * Gets a string value by id
     *
     * @param id the id of the value being retrieved from the JSON Object
     *
     * @return string value representing the query, NULL if invalid
     */
    public String getStringById(String id) {
    	String value = null;

    	if (mData.containsKey((id))) {
    	    value = (String) mData.get(id);
    	}

    	return value;
    }

    /**
     * Gets a double value by id
     *
     * @param id the id of the value being retrieved from the JSON Object
     *
     * @return double value representing the query, initialized to 0
     */
    public double getDoubleById(String id) {
        double value = 0;

        if (mData.containsKey(id)) {
            value = (double) mData.get(id);
        }

        return value;
        }

    /**
     * Gets an int value by id
     *
     * @param id the id of the value being retrieved from the JSON Object
     *
     * @return int value representing the query, initialized to 0
     * @throws UnsupportedOperationException if the object found with the id can't be converted into an int
     */
    public int getIntById(String id) throws UnsupportedOperationException {
    	int value = 0;

    	if (mData.containsKey(id)) {

    	    Object number = mData.get(id);
    	    if(number instanceof Integer){
    	        value = (int) number;
    	    }else if(number instanceof Long){
    	        value = ((Long) number).intValue();
    	    }else{
    	        throw new UnsupportedOperationException("Unable to convert the number "+number+" into an integer because the logic for that object type hasn't been written.");
    	    }
    	}

    	return value;
    }

    /**
     * Gets long value by id
     *
     * @param id the id of the value being retrieved from the JSON Object
     *
     * @return long value representing the query result, initialized to 0
     */
    public long getLongById(String id) {
        long value = 0;

        if (mData.containsKey(id)) {
            value = (long) mData.get(id);
        }

        return value;
        }

    /**
     * Gets the JSONObject value by id
     *
     * @param id the id of the value being retrieved from the JSON Object
     *
     * @return JSONObject value representing the query result, initialized to
     * NULL
     */
    public JSONObject getObjectById(String id) {
    	JSONObject value = null;

    	if (mData.containsKey(id)) {
    	    value = (JSONObject) mData.get(id);
    	}

    	return value;
    }

    /**
     * Gets the JSONArray value by id
     *
     * @param id the id of the value being retrieved from the JSON Object
     *
     * @return JSONArray value representing the query result, initialized to
     * NULL
     */
    public JSONArray getArrayById(String id) {
    	JSONArray value = null;

    	if (mData.containsKey(id)) {
    	    value = (JSONArray) mData.get(id);
    	}

    	return value;
    }

    /**
     * Templated function that adds primitive values to the underlying JSON
     * Object
     *
     * @param id the id of the value being added
     *
     * @param value the value being added to the object
     * @param <T> the type of value being set
     * @return if value was successfully added to the JSONObject
     */
    @SuppressWarnings("unchecked")
    public <T> boolean setValueById(String id, T value) {
    	boolean returnValue = false;
    	mData.put(id, value);
    	returnValue = mData.containsKey(id);

    	return returnValue;
    }

    /**
     * Adds a JSONObject to the underlying JSON Object
     *
     * @param id the id of the value being added
     *
     * @param value the JSONObject being added to the underlying JSON Object
     *
     * @return if value was successfully added to the JSONObject
     */
    @SuppressWarnings("unchecked")
    public boolean addObjectById(String id, JSONObject value) {
    	boolean returnValue = false;

    	mData.put(id, value);
    	returnValue = mData.containsKey(id);

    	return returnValue;
    }

    @SuppressWarnings("unchecked")
    public void addAll(JSONObject jsonObj){
        mData.putAll(jsonObj);
    }

    public JSONObject getJSONObject(){
        return mData;
    }

    /**
     * Adds a JSONArray to the underlying JSON Object
     *
     * @param id the id of the value being added
     *
     * @param value the JSONArray being added to the underlying JSONObject
     *
     * @return if value was successfully added to the underlying JSONObject
     */
    @SuppressWarnings("unchecked")
    public boolean addArraybyId(String id, JSONArray value) {
    	boolean returnValue = false;

    	mData.put(id, value);
    	returnValue = mData.containsKey(id);

    	return returnValue;

    }

    /**
     * Returns the state UUID
     *
     * @return the UUID of this state
     */
    public UUID getUUID() {
        return mUUID;
    }

    /**
     * Sets the UUID for this specific state
     *
     * @param mUUID the uuid to use for this state
     */
    private void setUUID(UUID mUUID) {
        this.mUUID = mUUID;
    }

    /**
     * Serializes the JSONObject to string and appends the UUID of the string to
     * the end NOTE: THIS STRING IS NOT JSON FORMATTED
     *
     * @return serialized string value
     */
    @Override
    public String toString() {

    	StringBuilder sb = new StringBuilder();
        sb.append("[EmbeddedGenericJSONState: ");
    	sb.append("uuid = ").append(mUUID);
    	sb.append(" jsonObject = ").append(mData);
    	sb.append("]");

    	return sb.toString();
    }

    /**
     * Convert an Entity State object into the Generic JSON State equivalent.
     *
     * @param entityState a populated entity state to convert to JSON information.  Can't be null.
     * @return GenericJSONState the new state object created with the attributes of the entity state
     */
    public static EmbeddedGenericJSONState fromEntityState(EntityState entityState){
        EmbeddedGenericJSONState entityStateMessage = new EntityStateJSON(entityState);
        return entityStateMessage;
    }

    /**
     * Convert a Detonation object into the Generic JSON State equivalent.
     *
     * @param detonation a populated detonation to convert to JSON information.  Can't be null.
     * @return GenericJSONState the new state object created with the attributes of the detonation
     */
    public static EmbeddedGenericJSONState fromDetonation(Detonation detonation){
        EmbeddedGenericJSONState detonationMessage = new DetonationJSON(detonation);
        return detonationMessage;
    }

    /**
     * Convert a WeaponFire object into the Generic JSON State equivalent.
     *
     * @param weaponFire a populated weapon fire to convert to JSON information.  Can't be null.
     * @return GenericJSONState the new state object created with the attributes of the weapon fire
     */
    public static EmbeddedGenericJSONState fromWeaponFire(WeaponFire weaponFire){
        EmbeddedGenericJSONState fireMessage = new WeaponFireJSON(weaponFire);
        return fireMessage;
    }

    /**
     * This inner class currently only contains the common keys for the JSON object used
     * by other state instances (e.g. Entity State).
     *
     * @author mhoffman
     *
     */
    public static class CommonStateJSON extends EmbeddedGenericJSONState{

        /**
         * TC3
         */
        public static final String TC3_SessionId_key = "sessionid";
        public static final String TC3_SimulationId_key = "simulationID";

        public static final String UPDATE_OBJ = "UpdateObject";
        public static final String SIM_CMD = "simulationCommand";
        public static final String DATA = "data";

        public static final String OBJECT_ID = "objectId";

        public static final String ENTITY_ID_ID = "entityID._entityID";
        public static final String ENTITY_ID_SIMADDR_SITEID = "entityID._simAddr._siteID";
        public static final String ENTITY_ID_SIMADDR_APPID = "entityID._simAddr._appID";
        public static final String FORCE_ID = "forceID";
    }

    /**
     * This inner class currently only contains the common weapon related keys for the
     * JSON object used by other state instances (e.g. Weapon fire).
     *
     * @author mhoffman
     *
     */
    public static class CommonWeaponJSON extends CommonStateJSON{

        public static final String FIRE_ENTITY_ID_ID = "firingEntityID._entityID";
        public static final String FIRE_ENTITY_ID_SIMADDR_SITEID = "firingEntityID._simAddr._siteID";
        public static final String FIRE_ENTITY_ID_SIMADDR_APPID = "firingEntityID._simAddr._appID";
        public static final String TARGET_ENTITY_ID_ID = "targetEntityID._entityID";
        public static final String TARGET_ENTITY_ID_SIMADDR_SITEID = "targetEntityID._simAddr._siteID";
        public static final String TARGET_ENTITY_ID_SIMADDR_APPID = "targetEntityID._simAddr._appID";
        public static final String MUNITION_ID_ID = "munitionID._entityID";
        public static final String MUNITION_ID_SIMADDR_SITEID = "munitionID._simAddr._siteID";
        public static final String MUNITION_ID_SIMADDR_APPID = "munitionID._simAddr._appID";
        public static final String EVENT_ID_ID = "eventID._eventID";
        public static final String EVENT_ID_SIMADDR_SITEID = "eventID._simAddr._siteID";
        public static final String EVENT_ID_SIMADDR_APPID = "eventID._simAddr._appID";
        public static final String VEL_X = "velocity._x";
        public static final String VEL_Y = "velocity._y";
        public static final String VEL_Z = "velocity._z";
        public static final String LOC_X = "location._x";
        public static final String LOC_Y = "location._y";
        public static final String LOC_Z = "location._z";
    }

    /**
     * This inner class contains the logic to convert a Weapon Fire object into a Weapon
     * Fire JSON object.
     *
     * @author mhoffman
     *
     */
    public static class WeaponFireJSON extends CommonWeaponJSON{

        public static final String BURST_MUNTYPE_ENTITYKIND = "burstDescriptor._munitionType._entityKind";
        public static final String BURST_MUNTYPE_DOMAIN = "burstDescriptor._munitionType._domain";
        public static final String BURST_MUNTYPE_COUNTRY = "burstDescriptor._munitionType._country";
        public static final String BURST_MUNTYPE_CATEGORY = "burstDescriptor._munitionType._category";
        public static final String BURST_MUNTYPE_SUBCAT = "burstDescriptor._munitionType._subcategory";
        public static final String BURST_MUNTYPE_SPECIFIC = "burstDescriptor._munitionType._specific";
        public static final String BURST_MUNTYPE_EXTRA = "burstDescriptor._munitionType._extra";
        public static final String BURST_WARHEAD = "burstDescriptor._warhead";
        public static final String BURST_FUSE = "burstDescriptor._fuse";
        public static final String BURST_QTY = "burstDescriptor._quantity";
        public static final String BURST_RATE = "burstDescriptor._rate";

        public WeaponFireJSON(WeaponFire weaponFire){
            init(weaponFire);
        }

        @SuppressWarnings("unchecked")
        private void init(WeaponFire weaponFire){

            if(weaponFire == null){
                throw new IllegalArgumentException("The weapon fire can't be null.");
            }

            setValueById(SIM_CMD, UPDATE_OBJ);
            JSONArray wfDataArray = new JSONArray();
            JSONObject tmp = new JSONObject();
            tmp.put(FIRE_ENTITY_ID_ID, weaponFire.getFiringEntityID().getEntityID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(FIRE_ENTITY_ID_SIMADDR_SITEID, weaponFire.getFiringEntityID().getSimulationAddress().getSiteID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(FIRE_ENTITY_ID_SIMADDR_APPID, weaponFire.getFiringEntityID().getSimulationAddress().getApplicationID());
            tmp = new JSONObject();
            tmp.put(OBJECT_ID, FIRE_ENTITY_ID_ID + weaponFire.getFiringEntityID().getEntityID());
            wfDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(TARGET_ENTITY_ID_ID, weaponFire.getTargetEntityID().getEntityID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(TARGET_ENTITY_ID_SIMADDR_SITEID, weaponFire.getTargetEntityID().getSimulationAddress().getSiteID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(TARGET_ENTITY_ID_SIMADDR_APPID, weaponFire.getTargetEntityID().getSimulationAddress().getApplicationID());
            wfDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(MUNITION_ID_ID, weaponFire.getMunitionID().getEntityID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(MUNITION_ID_SIMADDR_SITEID, weaponFire.getMunitionID().getSimulationAddress().getSiteID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(MUNITION_ID_SIMADDR_APPID, weaponFire.getMunitionID().getSimulationAddress().getApplicationID());
            wfDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(EVENT_ID_ID, weaponFire.getEventID().getEventID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(EVENT_ID_SIMADDR_SITEID, weaponFire.getEventID().getSimulationAddress().getSiteID());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(EVENT_ID_SIMADDR_APPID, weaponFire.getEventID().getSimulationAddress().getApplicationID());
            wfDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(VEL_X, weaponFire.getVelocity().x);
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(VEL_Y, weaponFire.getVelocity().y);
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(VEL_Z, weaponFire.getVelocity().z);
            wfDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(LOC_X, weaponFire.getLocation().x);
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOC_Y, weaponFire.getLocation().y);
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOC_Z, weaponFire.getLocation().z);
            wfDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_ENTITYKIND, weaponFire.getBurstDescriptor().getMunitionType().getEntityKind());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_DOMAIN, weaponFire.getBurstDescriptor().getMunitionType().getDomain());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_COUNTRY, weaponFire.getBurstDescriptor().getMunitionType().getCountry());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_CATEGORY, weaponFire.getBurstDescriptor().getMunitionType().getCategory());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_SUBCAT, weaponFire.getBurstDescriptor().getMunitionType().getSubcategory());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_SPECIFIC, weaponFire.getBurstDescriptor().getMunitionType().getSpecific());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_MUNTYPE_EXTRA, weaponFire.getBurstDescriptor().getMunitionType().getExtra());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_WARHEAD, weaponFire.getBurstDescriptor().getWarhead());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_FUSE, weaponFire.getBurstDescriptor().getFuse());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_QTY, weaponFire.getBurstDescriptor().getQuantity());
            wfDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(BURST_RATE, weaponFire.getBurstDescriptor().getRate());
            wfDataArray.add(tmp);
            addArraybyId(DATA, wfDataArray);
        }
    }

    /**
     * This inner class contains the logic to convert a Detonation object into a Detonation
     * JSON object.
     *
     * @author mhoffman
     *
     */
    public static class DetonationJSON extends CommonWeaponJSON{

        public static final String DET_RESULT_VALUE = "detonationResult._value";
        public static final String DET_RESULT_NAME = "detonationResult._name";
        public static final String DET_RESULT_DISPLAY = "detonationResult._displayName";

        public DetonationJSON(Detonation detonation){
            init(detonation);
        }

        @SuppressWarnings("unchecked")
        private void init(Detonation detonation){

            if(detonation == null){
                throw new IllegalArgumentException("The detonation can't be null.");
            }

            setValueById(SIM_CMD, UPDATE_OBJ);

            JSONArray dtDataArray = new JSONArray();
            JSONObject tmp = new JSONObject();
            tmp.put(FIRE_ENTITY_ID_ID, detonation.getFiringEntityID().getEntityID());
            dtDataArray.add(tmp);
            tmp.clear();
            tmp.put(FIRE_ENTITY_ID_SIMADDR_SITEID, detonation.getFiringEntityID().getSimulationAddress().getSiteID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(FIRE_ENTITY_ID_SIMADDR_APPID, detonation.getFiringEntityID().getSimulationAddress().getApplicationID());
            dtDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(TARGET_ENTITY_ID_ID, detonation.getTargetEntityID().getEntityID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(TARGET_ENTITY_ID_SIMADDR_SITEID, detonation.getTargetEntityID().getSimulationAddress().getSiteID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(TARGET_ENTITY_ID_SIMADDR_APPID, detonation.getTargetEntityID().getSimulationAddress().getApplicationID());
            dtDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(MUNITION_ID_ID, detonation.getMunitionID().getEntityID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(MUNITION_ID_SIMADDR_SITEID, detonation.getMunitionID().getSimulationAddress().getSiteID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(MUNITION_ID_SIMADDR_APPID, detonation.getMunitionID().getSimulationAddress().getApplicationID());
            dtDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(EVENT_ID_ID, detonation.getEventID().getEventID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(EVENT_ID_SIMADDR_SITEID, detonation.getEventID().getSimulationAddress().getSiteID());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(EVENT_ID_SIMADDR_APPID, detonation.getEventID().getSimulationAddress().getApplicationID());
            dtDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(VEL_X, detonation.getVelocity().x);
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(VEL_Y, detonation.getVelocity().y);
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(VEL_Z, detonation.getVelocity().z);
            dtDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(LOC_X, detonation.getLocation().x);
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOC_Y, detonation.getLocation().y);
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOC_Z, detonation.getLocation().z);
            dtDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(DET_RESULT_VALUE, detonation.getDetonationResult().getValue());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(DET_RESULT_NAME, detonation.getDetonationResult().getName());
            dtDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(DET_RESULT_DISPLAY, detonation.getDetonationResult().getDisplayName());
            dtDataArray.add(tmp);

            addArraybyId(DATA, dtDataArray);
        }
    }

    /**
     * This inner class contains the logic to convert a Entity State object into a Entity
     * State JSON object.
     *
     * @author mhoffman
     *
     */
    public static class EntityStateJSON extends CommonStateJSON{

        public static final String ENTITY_TYPE_ENTITYKIND= "entityType._entityKind";
        public static final String ENTITY_TYPE_DOMAIN = "entityType._domain";
        public static final String ENTITY_TYPE_COUNTRY = "entityType._country";
        public static final String ENTITY_TYPE_CATEGORY = "entityType._category";
        public static final String ENTITY_TYPE_SUBCAT = "entityType._subcategory";
        public static final String ENTITY_TYPE_SPEC = "entityType._specific";
        public static final String ENTITY_TYPE_EXTRA = "entityType._extra";

        public static final String LINEAR_VEL_X = "linearVel._x";
        public static final String LINEAR_VEL_Y = "linearVel._y";
        public static final String LINEAR_VEL_Z = "linearVel._z";
        public static final String LOCATION_X = "location._x";
        public static final String LOCATION_Y = "location._y";
        public static final String LOCATION_Z = "location._z";
        public static final String ORIENT_X = "orientation._x";
        public static final String ORIENT_Y = "orientation._y";
        public static final String ORIENT_Z = "orientation._z";
        public static final String APPEAR_POSTURE_VALUE = "appearance._posture._value";
        public static final String APPEAR_POSTURE_NAME = "appearance._posture._name";
        public static final String APPEAR_POSTURE_DISPLAY = "appearance._posture._displayName";
        public static final String APPEAR_DAMAGE_VALUE = "appearance._damage._value";
        public static final String APPEAR_DAMAGE_NAME = "appearance._damage._name";
        public static final String APPEAR_DAMAGE_DISPLAY = "appearance._damage._displayName";

        public EntityStateJSON(EntityState entityState){
            init(entityState);
        }

        @SuppressWarnings("unchecked")
        private void init(EntityState entityState){

            if(entityState == null){
                throw new IllegalArgumentException("The entity state can't be null.");
            }

            setValueById(SIM_CMD, UPDATE_OBJ);
            // set up the data array that simile uses to update the object
            JSONArray esDataArray = new JSONArray();
            JSONObject tmp = new JSONObject();
            tmp.put(ENTITY_ID_ID, entityState.getEntityID().getEntityID());
            esDataArray.add(tmp);
            tmp = new JSONObject();

            tmp.put(ENTITY_ID_SIMADDR_SITEID, entityState.getEntityID().getSimulationAddress().getSiteID());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_ID_SIMADDR_APPID, entityState.getEntityID().getSimulationAddress().getApplicationID());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(FORCE_ID, entityState.getForceID());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_ENTITYKIND, entityState.getEntityType().getEntityKind());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_DOMAIN, entityState.getEntityType().getDomain());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_COUNTRY, entityState.getEntityType().getCountry());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_CATEGORY, entityState.getEntityType().getCategory());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_SUBCAT, entityState.getEntityType().getSubcategory());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_SPEC, entityState.getEntityType().getSpecific());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ENTITY_TYPE_EXTRA, entityState.getEntityType().getExtra());
            tmp = new JSONObject();
            tmp.put(OBJECT_ID, "Entity" + entityState.getEntityID().getEntityID());

            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LINEAR_VEL_X, entityState.getLinearVelocity().x);
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LINEAR_VEL_Y, entityState.getLinearVelocity().y);
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LINEAR_VEL_Z, entityState.getLinearVelocity().z);
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOCATION_X, entityState.getLocation().x);
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOCATION_Y, entityState.getLocation().y);
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(LOCATION_Z, entityState.getLocation().z);
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ORIENT_X, entityState.getOrientation().getX());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ORIENT_Y, entityState.getOrientation().getY());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(ORIENT_Z, entityState.getOrientation().getZ());
            esDataArray.add(tmp);

            tmp = new JSONObject();
            tmp.put(APPEAR_POSTURE_VALUE, entityState.getAppearance().getPosture().getValue());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(APPEAR_POSTURE_NAME, entityState.getAppearance().getPosture().getName());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(APPEAR_POSTURE_DISPLAY, entityState.getAppearance().getPosture().getDisplayName());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(APPEAR_DAMAGE_VALUE, entityState.getAppearance().getDamage().getValue());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(APPEAR_DAMAGE_NAME, entityState.getAppearance().getDamage().getName());
            esDataArray.add(tmp);
            tmp = new JSONObject();
            tmp.put(APPEAR_DAMAGE_DISPLAY, entityState.getAppearance().getDamage().getDisplayName());
            esDataArray.add(tmp);

            // add the array of data to the message
            addArraybyId(DATA, esDataArray);
        }
    }

}
