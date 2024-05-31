/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.dis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.jdis.pdu.CollisionPDU;
import org.jdis.pdu.DetonationPDU;
import org.jdis.pdu.EntityStatePDU;
import org.jdis.pdu.FirePDU;
import org.jdis.pdu.StartResumePDU;
import org.jdis.pdu.StopFreezePDU;
import org.jdis.pdu.record.ClockTime;
import org.jdis.pdu.record.EntityMarking;
import org.jdis.pdu.record.EnumerationRecord;
import org.jdis.pdu.record.EulerAngles;
import org.jdis.pdu.record.LinearVelocityVector;
import org.jdis.pdu.record.Vector;
import org.jdis.pdu.record.WorldCoordinates;
import org.jdis.util.UnsignedByte;

import mil.arl.gift.common.enums.ArticulationParameterTypeDesignatorEnum;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.ta.state.BurstDescriptor;
import mil.arl.gift.common.ta.state.Collision;
import mil.arl.gift.common.ta.state.DeadReckoningParameters;
import mil.arl.gift.common.ta.state.DeadReckoningParameters.DeadReckoningAlgorithmField;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityAppearance;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.ta.state.EventIdentifier;
import mil.arl.gift.common.ta.state.RemoveEntity;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.WeaponFire;

/**
 * This class contains logic to convert DIS related field values into their GIFT equivalents
 * Note: Eventually the converter could be configured using configuration files to accommodate different DIS versions
 *
 * @author mhoffman
 *
 */
public class DISToGIFTConverter {

    /** the size in bytes of the entity state entity appearance */
    private static final int ENTITY_APPEARANCE_LENGTH = 4;

    /** the starting index of the health appearance bits of the entity state entity appearance */
    private static final int HEALTH_APPEARANCE_START_INDEX = 3;

    /** the ending index of the health appearance bits of the entity state entity appearance */
    private static final int HEALTH_APPEARANCE_END_INDEX = 4;

    /** the starting index of the posture appearance bits of the entity state entity appearance */
    private static final int POSTURE_APPEARANCE_START_INDEX = 16;

    /** the ending index of the posture appearance bits of the entity state entity appearance */
    private static final int POSTURE_APPEARANCE_END_INDEX = 19;

    /** the healthy value for damage in the entity state entity appearance  
     * DIS: No Damage 
     */
    private static final int NO_DAMAGE_APPEARANCE_VALUE = 0;

    /** the slight damage value for damage in the entity state entity appearance  
     * DIS: Slight Damage 
     */
    private static final int SLIGHT_DAMAGE_APPEARANCE_VALUE = 1;

    /** the moderate damage value for damage in the entity state entity appearance  
     * DIS: Moderate Damage 
     */
    private static final int MODERATE_DAMAGE_APPEARANCE_VALUE = 2;

    /** the destroyed / died of wounds value for damage in the entity state entity appearance 
     * DIS: Destroyed 
     */
    private static final int DESTROYED_APPEARANCE_VALUE = 3;

    /** value representing an entity impact for a detonation */
    private static final int ENTITY_IMPACT_DET_RESULT = 1;

    /** value representing no impact for a detonation */
    private static final int NONE_DET_RESULT = 6;

    /** value representing some other impact for a detonation */
    private static final int OTHER_DET_RESULT = 0;

    /** the starting index of the entity appearance state field */
    private static final int STATE_FIELD_START_INDEX = 23;

    /** the ending index of the entity appearance state field */
    private static final int STATE_FIELD_END_INDEX = 23;

    /** the active value for entity appearance state field */
    private static final int STATE_FIELD_ACTIVE = 0;

    /** the inactive value for entity appearance state field */
    private static final int STATE_FIELD_INACTIVE = 1;

    /**
     * Represents a specific set of rules for translating between GIFT DIS
     * objects and their PDU counterparts.
     *
     * @author tflowers
     *
     */
    public static enum DISDialect {
        /** The dialect that is used when talking to ARES. */
        ARES
    }

    /**
     * Represents the generic conversion rules that apply to all DIS dialects
     * unless otherwise specified.
     *
     * @author tflowers
     *
     */
    private static class DISTranslator {

        /**
         * Converts a provided GIFT {@link EntityState} object into a
         * {@link EntityStatePDU} object.
         *
         * @param entityState The {@link EntityState} object to convert.
         * @return The {@link EntityStatePDU} that was created as a result of
         *         the conversion.
         */
        public EntityStatePDU createEntityStatePDU(EntityState entityState) {
            EntityStatePDU entityStatePDU = new EntityStatePDU();

            entityStatePDU.setEntityID(entityState.getEntityID().getSimulationAddress().getSiteID(),
                    entityState.getEntityID().getSimulationAddress().getApplicationID(),
                    entityState.getEntityID().getEntityID());

            entityStatePDU.setForceID(entityState.getForceID());

            EnumerationRecord entityAppearance = new EnumerationRecord(ENTITY_APPEARANCE_LENGTH);
            getDamageRecord(entityState.getAppearance().getDamage(), entityAppearance);
            getPostureRecord(entityState.getAppearance().getPosture(), entityAppearance);
            getStateField(entityState.getAppearance().isActive(), entityAppearance);
            entityStatePDU.setEntityAppearance(entityAppearance);

            org.jdis.pdu.record.EntityType entityType = convertEntityType(entityState.getEntityType());
            entityStatePDU.setEntityType(entityType);
            
            org.jdis.pdu.record.EntityType altEntityType;
            if(entityState.getAlternativeEntityType() != null){
                altEntityType = convertEntityType(entityState.getAlternativeEntityType());
            }else{
                altEntityType = entityType;
            }
            entityStatePDU.setAlternativeEntityType(altEntityType);

            LinearVelocityVector linearVelocityVector = new LinearVelocityVector(
                    (float) entityState.getLinearVelocity().x, (float) entityState.getLinearVelocity().y,
                    (float) entityState.getLinearVelocity().z);
            entityStatePDU.setEntityLinearVelocity(linearVelocityVector);

            WorldCoordinates entityLocation = new WorldCoordinates(entityState.getLocation().x,
                    entityState.getLocation().y, entityState.getLocation().z);
            entityStatePDU.setEntityLocation(entityLocation);

            EulerAngles entityOrientation = new EulerAngles((float) entityState.getOrientation().x,
                    (float) entityState.getOrientation().y, (float) entityState.getOrientation().z);
            entityStatePDU.setEntityOrientation(entityOrientation);

            EntityMarking entityMarking = new EntityMarking();
            entityMarking.setMarking(entityState.getEntityMarking().getEntityMarking());
            entityMarking.setCharacterSet(entityState.getEntityMarking().getEntityCharacterSetConstant());
            entityStatePDU.setEntityMarking(entityMarking);
            
            entityStatePDU.setArticulationParameters(convertGIFTArticulationParameters(entityState.getArticulationParameters()));
            
            if(entityState.getDeadReckoningParameters() != null){
                entityStatePDU.setDeadReckoningParameters(convertGIFTDeadReckoningParameters(entityState.getDeadReckoningParameters()));
            }

            return entityStatePDU;
        }

        /**
         * Converts a provided GIFT {@link EntityStatePDU} object into a
         * {@link EntityState} object.
         *
         * @param pdu The {@link EntityStatePDU} object to convert.
         * @return The {@link EntityState} that was created as a result of the
         *         conversion.
         */
        public EntityState createEntityState(EntityStatePDU pdu) {
            EntityIdentifier eId = convertEntityIdentifier(pdu.getEntityID());
            EntityAppearance entityAppearance = getEntityAppearance(pdu.getEntityAppearance());
            EntityType entityType = convertEntityType(pdu.getEntityType());
            EntityType altEntityType = convertEntityType(pdu.getAlternativeEntityType());
            Vector3d linearVel = convertLinearVelocity(pdu.getEntityLinearVelocity());
            Point3d location2 = convertLocation(pdu.getEntityLocation());
            Vector3d orientation = convertOrientation(pdu.getEntityOrientation());
            mil.arl.gift.common.ta.state.EntityMarking entityMarking = getEntityMarking(pdu.getEntityMarking());
            DeadReckoningParameters deadReckoningParameters = convertDeadReckoningParameters(pdu.getDeadReckoningParameters());
            @SuppressWarnings("unchecked")
            List<ArticulationParameter> articulationParameters = convertArticulationParameters(pdu.getArticulationParameters());

            EntityState entityState = new EntityState(eId, pdu.getForceID().intValue(), entityType, linearVel, location2,
                    orientation, articulationParameters, entityAppearance,
                    entityMarking);
            
            entityState.setAlternativeEntityType(altEntityType);
            entityState.setDeadReckoningParameters(deadReckoningParameters);

            return entityState;
        }
    }

    /**
     * Represents the conversion rules that apply to translating DIS objects for
     * ARES.
     *
     * @author tflowers
     *
     */
    private static class ARESDISTranslator extends DISTranslator {

        @Override
        public EntityStatePDU createEntityStatePDU(EntityState es) {

            final EntityStatePDU pdu = super.createEntityStatePDU(es);

            /* need to place echelon information in a place in the PDU that ARES
             * can pick up */
            final EchelonEnum echelon = es.getEntityType().getEchelon();
            int echelonValue = echelon != null ? echelon.getEchelonLevel() : 0;
            pdu.getAlternativeEntityType().setExtra(echelonValue);
            final String giftDisplayName = es.getEntityMarking().getGiftDisplayName();
            if (giftDisplayName != null) {
                pdu.getEntityMarking().setMarking(giftDisplayName);
            }
            return pdu;
        }
    }

    /**
     * A mapping of {@link DISDialect} values to the {@link DISTranslator}
     * objects that are used to translate for that {@link DISDialect}.
     */
    private static final Map<DISDialect, DISTranslator> dialectToTranslator = new HashMap<>();
    static {
        dialectToTranslator.put(null, new DISTranslator());
        dialectToTranslator.put(DISDialect.ARES, new ARESDISTranslator());
    }

    /**
     * Class constructor
     */
    private DISToGIFTConverter(){

    }

    /**
     * Return the detonation result for the given value.
     *
     * @param value - the DIS detonation result value to convert
     * @return DetonationResultEnum - the GIFT representation of that value
     */
    public static DetonationResultEnum getDetonationResult(int value){

        DetonationResultEnum result;

        switch(value){

        case ENTITY_IMPACT_DET_RESULT:
            result = DetonationResultEnum.ENTITY_IMPACT;
            break;
        case NONE_DET_RESULT:
            result = DetonationResultEnum.NONE;
            break;
        default:
            result = DetonationResultEnum.OTHER;
        }

        return result;
    }

    /**
     * Return the detonation PDU detonation result value for the enum provided.
     *
     * @param resultEnum the detonation result to convert to integer
     * @return an integer value representing the detonation result enum
     */
    public static int getDetonationResult(DetonationResultEnum resultEnum){

        if(resultEnum == DetonationResultEnum.ENTITY_IMPACT){
            return ENTITY_IMPACT_DET_RESULT;
        }else if(resultEnum == DetonationResultEnum.NONE){
            return NONE_DET_RESULT;
        }else{
            return OTHER_DET_RESULT;
        }
    }

    /**
     * Return the posture for the given entity appearance
     *
     * @param appearance - the DIS appearance value to convert the posture value from
     * @return PostureEnum - the GIFT representation of that posture value
     */
    public static PostureEnum getPosture(EnumerationRecord appearance){

        // Bits 16 -19 - Life-form State
        UnsignedByte uByte = appearance.getUnsignedByte(POSTURE_APPEARANCE_START_INDEX, POSTURE_APPEARANCE_END_INDEX);
        PostureEnum posture = PostureEnum.valueOf(uByte.intValue());

        return posture;
    }

    /**
     * Set the posture bits for the enumerated posture provided
     * @param postureEnum the enumerated posture to set the correct values for in the record
     * @param appearance the record where the posture bits will be set.
     */
    public static void getPostureRecord(PostureEnum postureEnum, EnumerationRecord appearance){

        int value = postureEnum.getValue();
        appearance.set(POSTURE_APPEARANCE_START_INDEX, POSTURE_APPEARANCE_END_INDEX, value);
    }

    /**
     * Set the state field bit for the active flag provided
     * @param active whether the entity is active (true) or deactivated (false)
     * @param appearance the recored where the state field bit will be set.
     */
    public static void getStateField(boolean active, EnumerationRecord appearance){

        int value = active ? STATE_FIELD_ACTIVE : STATE_FIELD_INACTIVE;
        appearance.set(STATE_FIELD_START_INDEX, STATE_FIELD_END_INDEX, value);
    }

    /**
     * Return the damage for the given entity appearance.
     *
     * @param appearance - the DIS appearance value to convert the damage value from
     * @return DamageEnum - the GIFT representation of that appearance value
     */
    public static DamageEnum getDamage(EnumerationRecord appearance){

        // Bits 3 & 4 - Health
        UnsignedByte uByte = appearance.getUnsignedByte(HEALTH_APPEARANCE_START_INDEX, HEALTH_APPEARANCE_END_INDEX);

        //map DIS damage values to GIFT enum
        DamageEnum damage = DamageEnum.HEALTHY;
        if(uByte.intValue() == NO_DAMAGE_APPEARANCE_VALUE){
            damage = DamageEnum.HEALTHY;
        }else if(uByte.intValue() == SLIGHT_DAMAGE_APPEARANCE_VALUE){
            damage = DamageEnum.SLIGHT_DAMAGE;
        }else if(uByte.intValue() == MODERATE_DAMAGE_APPEARANCE_VALUE){
            damage = DamageEnum.MODERATE_DAMAGE;
        }else if(uByte.intValue() == DESTROYED_APPEARANCE_VALUE){
            damage = DamageEnum.DESTROYED;
        }

        return damage;
    }

    /**
     * Set the damage health bits for the enumerated damage provided.
     * @param damageEnum the enumerated damage to set the correct values for in the record.
     * @param appearance the record where the damage bits will be set.
     */
    public static void getDamageRecord(DamageEnum damageEnum, EnumerationRecord appearance){

        if(damageEnum == DamageEnum.HEALTHY){
            appearance.set(HEALTH_APPEARANCE_START_INDEX, HEALTH_APPEARANCE_END_INDEX, NO_DAMAGE_APPEARANCE_VALUE);
        }else if(damageEnum == DamageEnum.SLIGHT_DAMAGE){
            appearance.set(HEALTH_APPEARANCE_START_INDEX, HEALTH_APPEARANCE_END_INDEX, SLIGHT_DAMAGE_APPEARANCE_VALUE);
        }else if(damageEnum == DamageEnum.MODERATE_DAMAGE){
            appearance.set(HEALTH_APPEARANCE_START_INDEX, HEALTH_APPEARANCE_END_INDEX, MODERATE_DAMAGE_APPEARANCE_VALUE);
        }else if(damageEnum == DamageEnum.DESTROYED){
            appearance.set(HEALTH_APPEARANCE_START_INDEX, HEALTH_APPEARANCE_END_INDEX, DESTROYED_APPEARANCE_VALUE);
        }
    }

    /**
     * Return the entity appearance for the provided entity appearance record
     *
     * @param appearance - DIS appearance value to convert
     * @return EntityAppearanceData - GIFT representation of appearance value
     */
    public static EntityAppearance getEntityAppearance(EnumerationRecord appearance){

        DamageEnum damage = getDamage(appearance);
        PostureEnum posture = getPosture(appearance);

        EntityAppearance entityAppearance = new EntityAppearance(damage, posture);

        UnsignedByte stateValue = appearance.getUnsignedByte(STATE_FIELD_START_INDEX, STATE_FIELD_END_INDEX);

        // bit 8 - state field
        entityAppearance.setActive(stateValue.intValue() == STATE_FIELD_ACTIVE ? true : false);

        return entityAppearance;
    }

    /**
     * Decode the DIS entity marking bits into a GIFT Entity marking object.
     *
     * @param marking the DIS entity marking to decode.  From DIS Standard: This record shall
     * be used to specify the character set used in the marking and the string of characters
     * to be interpreted for display. The character set shall be represented by an 8-bit enumeration.
     * The string of characters shall be represented by an 11 element character string.
     * @return the GIFT entity marking object
     */
    public static mil.arl.gift.common.ta.state.EntityMarking getEntityMarking(EntityMarking marking){
        String tag = "none";

        if(marking.getMarking()[0] != null ) {
            // This is a 11 byte string of characters

            ArrayList<Character> charList = new ArrayList<>();
            for(int i = 0; i< marking.getMarking().length; i++) {
                if(marking.getMarking()[i] != null) {
                    charList.add((char) marking.getMarking()[i].intValue());
                }
            }

            StringBuilder sb = new StringBuilder();
            for(Character ch: charList) {
                sb.append(ch);
            }
            tag = sb.toString();

        }

        return new mil.arl.gift.common.ta.state.EntityMarking(mil.arl.gift.common.ta.state.EntityMarking.ASCII_CHARACTER_SET, tag);

    }

    /**
     * Converts from an {@link org.jdis.pdu.record.ArticulationParameter} to a
     * {@link ArticulationParameter}.
     *
     * @param jdisArtParam The object to convert from. Can't be null.
     * @return The object that was converted to. Can't be null.
     */
    public static ArticulationParameter convertArticulationParameter(org.jdis.pdu.record.ArticulationParameter jdisArtParam) {
        return new ArticulationParameter(ArticulationParameterTypeDesignatorEnum.valueOf(jdisArtParam.getParameterTypeDesignator().intValue()),
                                         jdisArtParam.getChangeIndicator().intValue(),
                                         jdisArtParam.getArticulationAttachmentID().intValue(),
                                         jdisArtParam.getParameterType().intValue(),
                                         jdisArtParam.getParameterValue());
    }

    /**
     * Converts from a {@link List} of
     * {@link org.jdis.pdu.record.ArticulationParameter} to a {@link List} of
     * {@link ArticulationParameter}
     *
     * @param jdisArtParams The {@link List} to convert from. Can be null or empty.
     * @return The {@link List} to convert to. Won't be null but can be empty.
     */
    public static List<ArticulationParameter> convertArticulationParameters(List<org.jdis.pdu.record.ArticulationParameter> jdisArtParams) {
        List<ArticulationParameter> artParams = new LinkedList<>();
        if(jdisArtParams != null){
            for (org.jdis.pdu.record.ArticulationParameter jdisArtParam : jdisArtParams) {
                artParams.add(convertArticulationParameter(jdisArtParam));
            }
        }
        return artParams;
    }
    
    /**
     * Converts from a {@link ArticulationParameter} to a
     * {@link org.jdis.pdu.record.ArticulationParameter}.
     * 
     * @param artParam the object to convert from.  Can't be null.
     * @return The {@link org.jdis.pdu.record.ArticulationParameter} to conver to.  Wont' be null.
     */
    public static org.jdis.pdu.record.ArticulationParameter convertGIFTArticulationParameter(ArticulationParameter artParam){
        
        org.jdis.pdu.record.ArticulationParameter jdisArtParam = new org.jdis.pdu.record.ArticulationParameter();
        jdisArtParam.setParameterTypeDesignator(artParam.getParameterTypeDesignator().getValue());
        jdisArtParam.setChangeIndicator(artParam.getParameterChange());
        jdisArtParam.setArticulationAttachmentID(artParam.getPartAttachedToID());
        jdisArtParam.setParameterType(artParam.getParameterType());
        jdisArtParam.setParameterValue(artParam.getParameterValue());
        
        return jdisArtParam;
    }
    
    /**
     * Converts from a {@link List} of {@link ArticulationParameter} to a
     * {@link List} of {@link org.jdis.pdu.record.ArticulationParameter}.
     * 
     * @param articulationParameters the {@link List} to convert from.  Can be null or empty.
     * @return The {@link List} to convert too.  Won't be null.
     */
    public static List<org.jdis.pdu.record.ArticulationParameter> convertGIFTArticulationParameters(List<ArticulationParameter> articulationParameters){
        
        List<org.jdis.pdu.record.ArticulationParameter> disArtParams = new ArrayList<>();
        
        if (articulationParameters != null) {
            for(ArticulationParameter artParam : articulationParameters){
                disArtParams.add(convertGIFTArticulationParameter(artParam));
            }
        }
        
        return disArtParams;
    }

    /**
     * Convert the entity state PDU into a GIFT entity state message.
     *
     * @param es the Entity state PDU object
     * @return EntityState the gift representation of that entity state object
     */
    public static EntityState createEntityState(EntityStatePDU es) {
        return createEntityState(es, null);
    }

    /**
     * Convert the entity state PDU into a GIFT entity state message using the
     * rules of a specific dialect.
     *
     * @param es The {@link EntityStatePDU} object
     * @param dialect The {@link DISDialect} to use for the translation. A null
     *        value indicates the generic/vanilla dialect should be used.
     * @return EntityState the gift representation of that entity state object
     */
    public static EntityState createEntityState(EntityStatePDU es, DISDialect dialect) {
        return dialectToTranslator.get(dialect).createEntityState(es);
    }

    /**
     * Convert the GIFT entity state into an entity state PDU.
     *
     * @param entityState the gift entity state to convert
     * @return the new entity state PDU
     */
    public static EntityStatePDU createEntityStatePDU(EntityState entityState){
        return createEntityStatePDU(entityState, null);
    }

    /**
     * Convert the GIFT {@link EntityState} into an {@link EntityStatePDU}.
     *
     * @param entityState the GIFT {@link EntityState} to convert.
     * @param dialect The {@link DISDialect} to use for the translation. A null
     *        value indicates the generic/vanilla dialect should be used.
     * @return the new {@link EntityStatePDU}.
     */
    public static EntityStatePDU createEntityStatePDU(EntityState entityState, DISDialect dialect) {
        return dialectToTranslator.get(dialect).createEntityStatePDU(entityState);

    }

    /**
     * Convert DIS euler angles into a vector 3d.
     *
     * @param eulerAngles the DIS euler angles to convert into a vector.
     * @return the vector containing the euler angles (Psi -> x, Theta -> y, Phi ->z)
     */
    private static Vector3d convertOrientation(EulerAngles eulerAngles){

        float x = eulerAngles.getPsi(),
                y = eulerAngles.getTheta(),
                z = eulerAngles.getPhi();

        if(eulerAngles.getPsi() == Float.NaN){
            x = 0.0f;
        }

        if(eulerAngles.getTheta() == Float.NaN){
            y = 0.0f;
        }

        if(eulerAngles.getPhi() == Float.NaN){
            z = 0.0f;
        }

        return new Vector3d(x,y,z);
    }

    /**
     * Convert DIS world coordinates into a point 3d.
     *
     * @param worldCoordinates the DIS world coordinates to convert in a point
     * @return a point containing the world coordinates (x->x, y->y, z->z)
     */
    private static Point3d convertLocation(WorldCoordinates worldCoordinates){

        double x = worldCoordinates.getX(),
                y = worldCoordinates.getY(),
                z = worldCoordinates.getZ();

        if(worldCoordinates.getX() == Double.NaN){
            x = 0.0;
        }

        if(worldCoordinates.getY() == Double.NaN){
            y = 0.0;
        }

        if(worldCoordinates.getZ() == Double.NaN){
            z = 0.0;
        }

        return new Point3d(x,y,z);
    }

    /**
     * Convert DIS linear velocity into a vector 3d.
     *
     * @param linearVelocityVector the DIS linear velocity to convert into a vector.
     * @return the vector containing the linear velocity (x->x, y->y, z->z)
     */
    private static Vector3d convertLinearVelocity(LinearVelocityVector linearVelocityVector){

        float x = linearVelocityVector.getX(),
                y = linearVelocityVector.getY(),
                z = linearVelocityVector.getZ();

        if(linearVelocityVector.getX() == Float.NaN){
            x = 0.0f;
        }

        if(linearVelocityVector.getY() == Float.NaN){
            y = 0.0f;
        }

        if(linearVelocityVector.getZ() == Float.NaN){
            z = 0.0f;
        }

        return new Vector3d(x,y,z);
    }
    
    /**
     * Convert a {@link org.jdis.pdu.record.DeadReckoningParameters} to a GIFT {@link DeadReckoningParameters}.
     * 
     * @param jdisDeadReckoningParameters Used to provide the parameters for dead reckoning the position and orientation of the entity. 
     * Dead Reckoning Algorithm in use, Entity Acceleration and Angular velocity shall be included as a part of the dead reckoning parameters.
     * @return the GIFT version of the dead reckoning parameters object.
     */
    private static DeadReckoningParameters convertDeadReckoningParameters(org.jdis.pdu.record.DeadReckoningParameters jdisDeadReckoningParameters){
        
        UnsignedByte drAlg = jdisDeadReckoningParameters.getDeadReckoningAlgorithm();
        DeadReckoningAlgorithmField deadReckoningAlgorithmField = DeadReckoningAlgorithmField.OTHER;
        if(drAlg.intValue() < DeadReckoningAlgorithmField.values().length){
            deadReckoningAlgorithmField = DeadReckoningAlgorithmField.values()[drAlg.intValue()];
        }
        
        DeadReckoningParameters deadReckoningParameters = new DeadReckoningParameters(deadReckoningAlgorithmField);
        return deadReckoningParameters;
    }
    
    /**
     * Convert a {@link DeadReckoningParameters) to a {@link org.jdis.pdu.record.DeadReckoningParameters}
     * @param deadReckoningParameters Used to provide the parameters for dead reckoning the position and orientation of the entity. 
     * Dead Reckoning Algorithm in use, Entity Acceleration and Angular velocity shall be included as a part of the dead reckoning parameters.
     * @return a new JDIS DeadReckoningParameters object
     */
    private static org.jdis.pdu.record.DeadReckoningParameters convertGIFTDeadReckoningParameters(DeadReckoningParameters deadReckoningParameters){
        
        org.jdis.pdu.record.DeadReckoningParameters jdisDeadReckoningParameters = new org.jdis.pdu.record.DeadReckoningParameters();
        jdisDeadReckoningParameters.setDeadReckoningAlgorithm(deadReckoningParameters.getDeadReckoningAlgorithmField().ordinal());
        
        return jdisDeadReckoningParameters;
    }

    /**
     * Converts a DIS {@link org.jdis.pdu.record.BurstDescriptor} into a GIFT
     * {@link BurstDescriptor}.
     *
     * @param burstDescriptor The {@link org.jdis.pdu.record.BurstDescriptor} to
     *        convert from. Can't be null.
     * @return The resulting {@link BurstDescriptor}. Can't be null.
     */
    private static BurstDescriptor convertBurstDescriptor(org.jdis.pdu.record.BurstDescriptor burstDescriptor) {
        final EntityType munitionType = convertEntityType(burstDescriptor.getMunitionType());
        final int warhead = burstDescriptor.getWarhead().intValue();
        final int fuse = burstDescriptor.getFuse().intValue();
        final int quantity = burstDescriptor.getQuantity().intValue();
        final int rate = burstDescriptor.getRate().intValue();
        return new BurstDescriptor(munitionType, warhead, fuse, quantity, rate);
    }

    /**
     * Converts a GIFT {@link BurstDescriptor} into a DIS
     * {@link org.jdis.pdu.record.BurstDescriptor}.
     *
     * @param burstDescriptor The {@link BurstDescriptor} to convert from. Can't
     *        be null.
     * @return The resulting {@link org.jdis.pdu.record.BurstDescriptor}. Can't
     *         be null.
     */
    private static org.jdis.pdu.record.BurstDescriptor convertBurstDescriptor(BurstDescriptor burstDescriptor) {
        org.jdis.pdu.record.BurstDescriptor toRet = new org.jdis.pdu.record.BurstDescriptor();

        toRet.setFuse(burstDescriptor.getFuse());
        toRet.setMunitionType(convertEntityType(burstDescriptor.getMunitionType()));
        toRet.setQuantity(burstDescriptor.getQuantity());
        toRet.setRate(burstDescriptor.getRate());
        toRet.setWarhead(burstDescriptor.getWarhead());

        return toRet;
    }

    /**
     * Converts a DIS {@link org.jdis.pdu.record.EntityIdentifier} to a GIFT
     * {@link EntityIdentifier}.
     *
     * @param entityId The {@link org.jdis.pdu.record.EntityIdentifier} to
     *        convert from. Can't be null.
     * @return The resulting {@link EntityIdentifier}. Can't be null.
     */
    private static EntityIdentifier convertEntityIdentifier(org.jdis.pdu.record.EntityIdentifier entityId) {
        final SimulationAddress simAddr = new SimulationAddress(
                entityId.getSimulationAddress().getSiteIdentification().intValue(),
                entityId.getSimulationAddress().getApplicationIdentification().intValue());

        return new EntityIdentifier(simAddr, entityId.getEntityIdentifier().intValue());
    }

    /**
     * Converts a GIFT {@link EntityIdentifier} into a DIS
     * {@link org.jdis.pdu.record.EntityIdentifier}.
     *
     * @param entityId The {@link EntityIdentifier} to convert from. Can't be
     *        null.
     * @return The resulting {@link org.jdis.pdu.record.EntityIdentifier}. Can't
     *         be null.
     */
    private static org.jdis.pdu.record.EntityIdentifier convertEntityIdentifier(EntityIdentifier entityId) {
        org.jdis.pdu.record.SimulationAddress simulationAddress = new org.jdis.pdu.record.SimulationAddress(
                entityId.getSimulationAddress().getSiteID(), entityId.getSimulationAddress().getApplicationID());
        return new org.jdis.pdu.record.EntityIdentifier(simulationAddress, entityId.getEntityID());
    }

    /**
     * Converts a DIS {@link org.jdis.pdu.record.EntityType} into a GIFT
     * {@link EntityType}.
     *
     * @param entityType The {@link org.jdis.pdu.record.EntityType} to convert
     *        from. Can't be null.
     * @return The resulting {@link EntityType}. Can't be null.
     */
    private static EntityType convertEntityType(org.jdis.pdu.record.EntityType entityType) {
        return new EntityType(entityType.getEntityKind().intValue(), entityType.getDomain().intValue(),
                entityType.getCountry().intValue(), entityType.getCategory().intValue(),
                entityType.getSubcategory().intValue(), entityType.getSpecific().intValue(),
                entityType.getExtra().intValue());
    }

    /**
     * Converts a GIFT {@link EntityType} into a DIS
     * {@link org.jdis.pdu.record.EntityIdentifier}.
     *
     * @param entityType The {@link EntityType} to convert from. Can't be null.
     * @return The resulting {@link org.jdis.pdu.record.EntityType}. Can't be
     *         null.
     */
    private static org.jdis.pdu.record.EntityType convertEntityType(EntityType entityType) {
        return new org.jdis.pdu.record.EntityType(entityType.getEntityKind(), entityType.getDomain(),
                entityType.getCountry(), entityType.getCategory(), entityType.getSubcategory(),
                entityType.getSpecific(), entityType.getExtra());
    }

    /**
     * Convert the DIS detonation PDU to a GIFT detonation object.
     *
     * @param pdu a DIS detonation PDU to convert to a GIFT detonation object.
     * @return a new gift detonation object created from the DIS detonation PDU
     */
    public static Detonation createDetonation(DetonationPDU pdu) {

        EntityIdentifier feIdentifier = convertEntityIdentifier(pdu.getFiringEntityID());
        EntityIdentifier teIdentifier = convertEntityIdentifier(pdu.getTargetEntityID());
        EntityIdentifier mIdentifier = convertEntityIdentifier(pdu.getMunitionID());

        SimulationAddress eSimAddr = new SimulationAddress(
                pdu.getEventID().getSimulationAddress().getSiteIdentification().intValue(),
                pdu.getEventID().getSimulationAddress().getApplicationIdentification().intValue());
        EventIdentifier eIdentifier = new EventIdentifier(eSimAddr, pdu.getEventID().getEventIdentifier().intValue());

        final Vector3d velocity = convertVector(pdu.getVelocity());
        final WorldCoordinates pduLocation = pdu.getLocationInWorldCoordinates();
        final Vector3d location = new Vector3d(pduLocation.getX(), pduLocation.getY(), pduLocation.getZ());
        final DetonationResultEnum detonationResult = getDetonationResult(pdu.getDetonationResult().intValue());
        final BurstDescriptor burstDescriptor = convertBurstDescriptor(pdu.getBurstDescriptor());
        Detonation detonation = new Detonation(feIdentifier, teIdentifier, mIdentifier, eIdentifier, velocity, location,
                burstDescriptor, detonationResult);

        return detonation;
    }

    /**
     * Convert the gift detonation object to a DIS detonation PDU.
     *
     * @param detonation a GIFT detonation to convert to a DIS detonation PDU
     * @return a new DIS detonation PDU created from the gift detonation object.
     */
    public static DetonationPDU createDetonationPDU(Detonation detonation){

        DetonationPDU detonationPDU = new DetonationPDU();

        org.jdis.pdu.record.EntityIdentifier firingEntityID = convertEntityIdentifier(detonation.getFiringEntityID());
        detonationPDU.setFiringEntityID(firingEntityID);

        org.jdis.pdu.record.EntityIdentifier targetEntityID = convertEntityIdentifier(detonation.getTargetEntityID());
        detonationPDU.setTargetEntityID(targetEntityID);

        org.jdis.pdu.record.EntityIdentifier munitionID = convertEntityIdentifier(detonation.getMunitionID());
        detonationPDU.setMunitionID(munitionID);

        org.jdis.pdu.record.EventIdentifier eventID = new org.jdis.pdu.record.EventIdentifier(
                detonation.getEventID().getSimulationAddress().getApplicationID(),
                detonation.getEventID().getSimulationAddress().getSiteID(),
                detonation.getEventID().getEventID());
        detonationPDU.setEventID(eventID);

        detonationPDU.setDetonationResult(getDetonationResult(detonation.getDetonationResult()));

        final Vector3d location = detonation.getLocation();
        detonationPDU.setLocationInWorldCoordinates(location.x, location.y, location.z);

        org.jdis.pdu.record.BurstDescriptor burstDescriptor = convertBurstDescriptor(detonation.getBurstDescriptor());
        detonationPDU.setBurstDescriptor(burstDescriptor);

        final Vector3d velocity = detonation.getVelocity();
        detonationPDU.setVelocity((float)velocity.x, (float)velocity.y, (float)velocity.z);

        return detonationPDU;
    }

    /**
     * Convert a DIS fire PDU to a GIFT weapon fire object.
     *
     * @param firePDU a DIS fire PDU to convert to a gift weapon fire object.
     * @return a new gift weapon fire object created from the DIS fire PDU.
     */
    public static WeaponFire createWeaponFire(FirePDU firePDU){

        EntityIdentifier feIdentifier = convertEntityIdentifier(firePDU.getFiringEntityID());
        EntityIdentifier teIdentifier = convertEntityIdentifier(firePDU.getTargetEntityID());
        EntityIdentifier mIdentifier = convertEntityIdentifier(firePDU.getMunitionID());

        SimulationAddress eSimAddr = new SimulationAddress(firePDU.getEventID().getSimulationAddress().getSiteIdentification().intValue(), firePDU.getEventID().getSimulationAddress().getApplicationIdentification().intValue());
        EventIdentifier eIdentifier = new EventIdentifier(eSimAddr, firePDU.getEventID().getEventIdentifier().intValue());

        BurstDescriptor burstDescriptor = convertBurstDescriptor(firePDU.getBurstDescriptor());

        WeaponFire weaponFire = new WeaponFire(feIdentifier, teIdentifier, mIdentifier, eIdentifier, DISToGIFTConverter.convertVector(firePDU.getVelocity()),
                new Vector3d(firePDU.getLocationInWorldCoordinates().getX(), firePDU.getLocationInWorldCoordinates().getY(), firePDU.getLocationInWorldCoordinates().getZ()), burstDescriptor);

        return weaponFire;
    }

    /**
     * Convert a gift weapon fire object to a DIS fire PDU.
     *
     * @param weaponFire a gift weapon fire object to convert to a DIS fire PDU
     * @return a new DIS fire PDU created from the gift weapon fire object.
     */
    public static FirePDU createFirePDU(WeaponFire weaponFire){

        FirePDU firePDU = new FirePDU();

        org.jdis.pdu.record.EntityIdentifier firingEntityID = convertEntityIdentifier(weaponFire.getFiringEntityID());
        firePDU.setFiringEntityID(firingEntityID);

        org.jdis.pdu.record.EntityIdentifier targetEntityID = convertEntityIdentifier(weaponFire.getTargetEntityID());
        firePDU.setTargetEntityID(targetEntityID);

        org.jdis.pdu.record.EntityIdentifier munitionID = convertEntityIdentifier(weaponFire.getMunitionID());
        firePDU.setMunitionID(munitionID);

        org.jdis.pdu.record.EventIdentifier eventID = new org.jdis.pdu.record.EventIdentifier(
                weaponFire.getEventID().getSimulationAddress().getApplicationID(),
                weaponFire.getEventID().getSimulationAddress().getSiteID(),
                weaponFire.getEventID().getEventID());
        firePDU.setEventID(eventID);

        org.jdis.pdu.record.BurstDescriptor burstDescriptor = convertBurstDescriptor(weaponFire.getBurstDescriptor());
        firePDU.setBurstDescriptor(burstDescriptor);

        firePDU.setLocationInWorldCoordinates(weaponFire.getLocation().x, weaponFire.getLocation().y, weaponFire.getLocation().z);

        firePDU.setVelocity((float)weaponFire.getVelocity().x, (float)weaponFire.getVelocity().y, (float)weaponFire.getVelocity().z);

        return firePDU;
    }

    /**
     * Create a new gift collection from the collision PDU.
     *
     * @param collisionPDU a DIS collision PDU used to create a gift collision.
     * @return a new gift collision object created from the collision PDU.
     */
    public static Collision createCollision(CollisionPDU collisionPDU){

        EntityIdentifier collidingIdentifier = convertEntityIdentifier(collisionPDU.getCollidingEntityID());

        EntityIdentifier issuingIdentifier = convertEntityIdentifier(collisionPDU.getIssuingEntityID());

        int collisionType = collisionPDU.getCollisionType().intValue();

        Collision collision = new Collision(issuingIdentifier, collidingIdentifier, collisionType);

        return collision;
    }

    /**
     * Create a new Remove Entity PDU from the GIFT remove entity.
     *
     * @param removeEntity the gift remove entity object
     * @return a new remove entity PDU created from the gift remove entity.
     */
    public static edu.nps.moves.dis7.RemoveEntityPdu createRemoveEntityPDU(RemoveEntity removeEntity){

        edu.nps.moves.dis7.RemoveEntityPdu removeEntityPDU = new edu.nps.moves.dis7.RemoveEntityPdu();

        edu.nps.moves.dis7.EntityID originatingEntityID = new edu.nps.moves.dis7.EntityID();
        originatingEntityID.setApplicationID(removeEntity.getOriginatingId().getSimulationAddress().getApplicationID());
        originatingEntityID.setEntityID(removeEntity.getOriginatingId().getEntityID());
        originatingEntityID.setSiteID(removeEntity.getOriginatingId().getSimulationAddress().getSiteID());
        removeEntityPDU.setOriginatingEntityID(originatingEntityID);

        edu.nps.moves.dis7.EntityID receivingEntityID = new edu.nps.moves.dis7.EntityID();
        originatingEntityID.setApplicationID(removeEntity.getReceivingId().getSimulationAddress().getApplicationID());
        originatingEntityID.setEntityID(removeEntity.getReceivingId().getEntityID());
        originatingEntityID.setSiteID(removeEntity.getReceivingId().getSimulationAddress().getSiteID());
        removeEntityPDU.setReceivingEntityID(receivingEntityID);

        removeEntityPDU.setRequestID(removeEntity.getRequestId());

        return removeEntityPDU;
    }

    /**
     * Create a new start resume PDU from the GIFT start resume.
     *
     * @param startResume the gift start resume object
     * @return a new start resume PDU created from the gift start resume.
     */
    public static StartResumePDU createStartResumePDU(StartResume startResume){

        StartResumePDU sr = new StartResumePDU();
        sr.setRealWorldTime(new ClockTime(DISTime.getHoursSinceEpochFromUTC(startResume.getRealWorldTime()), DISTime.convertUTCTimeToDISTimestamp(startResume.getRealWorldTime())));
        sr.setRequestID(startResume.getRequestID());
        sr.setSimulationTime(new ClockTime(0, startResume.getSimulationTime()));

        return sr;
    }

    /**
     * Create a new stop freeze PDU from the GIFT stop freeze.
     *
     * @param stopFreeze the gift stop freeze object
     * @return a new stop freeze PDU created from the gift stop freeze.
     */
    public static StopFreezePDU createStopFreezePDU(StopFreeze stopFreeze){

        StopFreezePDU sf = new StopFreezePDU();
        sf.setFrozenBehavior(stopFreeze.getFrozenBehavior());
        sf.setRealWorldTime(new ClockTime(DISTime.getHoursSinceEpochFromUTC(stopFreeze.getRealWorldTime()), DISTime.convertUTCTimeToDISTimestamp(stopFreeze.getRealWorldTime())));
        sf.setReason(stopFreeze.getReason());
        sf.setRequestID(stopFreeze.getRequestID());
        return sf;
    }

    /**
     * Return a java Vector3d for the DIS vector instance
     *
     * @param vector - DIS vector class to convert
     * @return Vector3d - GIFT representation of vector
     */
    public static Vector3d convertVector(Vector vector){

        return new Vector3d(vector.getX(), vector.getY(), vector.getZ());
    }

}
