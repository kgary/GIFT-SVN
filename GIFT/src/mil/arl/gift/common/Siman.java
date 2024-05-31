/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.Map;

import generated.course.EmbeddedAppInputs;
import generated.course.InteropInputs;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingAppRouteTypeEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;

/**
 * This class contains a simulation management request.
 * 
 * @author jleonard
 */
public class Siman{

    /** The type of SIMAN command being requested */
    private SimanTypeEnum simanType = null;
    
    /** 
     * Specifies the destination of the SIMAN message, i.e. whether it should go to a desktop training application or an embedded
     * web application. This is used during the decoding process to determine whether {@link #getLoadArgFromXMLString(String, boolean) 
     * should return {@link InteropInputs} or {@link EmbeddedAppInputs}.
     */
    private TrainingAppRouteTypeEnum routeType = null;

    /** 
     * specific information for a SIMAN Load request 
     * the load arguments map where the key is a gateway module interop plugin class and the
     * value is a generated object that may contain additional parameters that interop plugin needs
     * during SIMAN load operations.  Will be null if not a SIMAN load type.
     */
    private Map<String, Serializable> loadArgs;
    
    /** (optional) runtime course folder path relative to Domain folder */
    private String runtimeCourseFolderPath = null;
    
    //MH: ideally this would be embedded in the interop inputs load args some where.  Until we gather more args like this
    //    that shouldn't exist in the course.xsd because they aren't part of authoring but for runtime, this location is fine.
    /** (optional) size (bytes) of a training application file to load on the client side */
    private long fileSize = 0l;

    /**
     * Class constructor
     * Note: use the static Create* methods to create instances of this class.
     *
     * @param simanType - The type of SIMAN command being requested
     */
    private Siman(SimanTypeEnum simanType) {
        this.simanType = simanType;
    }
    
    /**
     * Set the runtime course folder path relative to Domain folder
     * 
     * @param runtimeCourseFolderPath the value of runtimeCourseFolderPath
     */
    public void setRuntimeCourseFolderPath(String runtimeCourseFolderPath){
        this.runtimeCourseFolderPath = runtimeCourseFolderPath;
    }
    
    /**
     * Return the runtime course folder path relative to Domain folder.  This
     * is used to build the URL for domain hosted files (e.g. PowerPoint show) that 
     * could be referenced in the Siman LOAD args.
     * 
     * @return the domain folder runtime course folder relative path.  Can be null.
     */
    public String getRuntimeCourseFolderPath(){
        return runtimeCourseFolderPath;
    }
    
    /**
     * Create a Siman instance based on the siman enumerated type provided.
     * Note: Load must use a different constructor.
     * 
     * @param simanType the enumerated type to create a SIMAN instance for.
     * @return Siman the instance created
     */
    public static Siman Create(SimanTypeEnum simanType){
        
        if(simanType == SimanTypeEnum.PAUSE){
            return CreatePause();
        }else if(simanType == SimanTypeEnum.RESTART){
            return CreateReset();
        }else if(simanType == SimanTypeEnum.RESUME){
            return CreateResume();
        }else if(simanType == SimanTypeEnum.START){
            return CreateStart();
        }else if(simanType == SimanTypeEnum.STOP){
            return CreateStop();
        }else{
            throw new IllegalArgumentException("Unable to create a SIMAN instance for enumerated value of "+simanType+".");
        }
    }
    
    /**
     * Return a Siman instance for the enumerated Pause.
     * 
     * @return Siman the instance created
     */
    public static Siman CreatePause(){
        return new Siman(SimanTypeEnum.PAUSE);
    }
    
    /**
     * Return a Siman instance for the enumerated Restart.
     * 
     * @return Siman the instance created
     */
    public static Siman CreateReset(){
        return new Siman(SimanTypeEnum.RESTART);
    }
    
    /**
     * Return a Siman instance for the enumerated Resume.
     * 
     * @return Siman the instance created
     */
    public static Siman CreateResume(){
        return new Siman(SimanTypeEnum.RESUME);
    }
    
    /**
     * Return a Siman instance for the enumerated Start.
     * 
     * @return Siman the instance created
     */
    public static Siman CreateStart(){
        return new Siman(SimanTypeEnum.START);
    }
    
    /**
     * Return a Siman instance for the enumerated Stop.
     * 
     * @return Siman the instance created
     */
    public static Siman CreateStop(){
        return new Siman(SimanTypeEnum.STOP);
    }
    
    /**
     * Return a Siman instance for the enumerated Load.
     * 
     * @param loadArgs arguments for the load operation.  Can be null.
     * @return Siman the instance created
     */
    public static Siman CreateLoad(Map<String, Serializable> loadArgs){
        Siman loadSiman = new Siman(SimanTypeEnum.LOAD);
        loadSiman.loadArgs = loadArgs;
        return loadSiman;
    }

    /**
     * Return the SIMAN type being requested.
     *  
     * @return SimanTypeEnum
     */
    public SimanTypeEnum getSimanTypeEnum() {
        return simanType;
    }
    
    /**
     * Gets the destination of the SIMAN message, i.e. whether it should go to a desktop training application or an embedded
     * web application. This is used during the decoding process to determine whether {@link #getLoadArgFromXMLString(String, boolean) 
     * should return {@link InteropInputs} or {@link EmbeddedAppInputs}.
     * 
     * @return a route type identifying the destination of the SIMAN message
     */
    public TrainingAppRouteTypeEnum getRouteType() {
    	return routeType;
    }
    
    /**
     * Sets the destination of the SIMAN message, i.e. whether it should go to a desktop training application or an embedded
     * web application. This will be used during the decoding process to determine whether {@link #getLoadArgFromXMLString(String, boolean) 
     * should return {@link InteropInputs} or {@link EmbeddedAppInputs}.
     * 
     * @param newRouteType a route type identifying the new destination of the SIMAN message
     */
    public void setRouteType(TrainingAppRouteTypeEnum newRoutType) {
    	if(newRoutType == null) {
    		throw new IllegalArgumentException("The new value for the SIMAN message's route type must not be null");
    	}
    	
    	routeType = newRoutType;
    }

    /**
     * Return the data associated with a siman load request.
     * 
     * @return the load arguments map where the key is a gateway module interop plugin class and the
     * value is a generated object that may contain additional parameters that interop plugin needs
     * during SIMAN load operations.  Will be null if not a SIMAN load type.
     */
    public Map<String, Serializable> getLoadArgs() {
        return loadArgs;
    }
    
    /**
     * Convert the generated class load argument object into a string representation of the XML contents.
     * 
     * @param loadArg the object to convert to an XML string
     * @return String the XML string created from the object.  Can be null if the object is null.
     * @throws Exception if there was a problem marshalling the object.
     */
    public static String getLoadArgsAsXMLString(Serializable loadArg) throws Exception{
        
        if(loadArg == null){
            return null;
        }
        
        String xmlString = 
                AbstractSchemaHandler.getAsXMLString(loadArg, loadArg instanceof InteropInputs ? InteropInputs.class : EmbeddedAppInputs.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE);
        return xmlString;
    }
    
    /**
     * Convert the XML string load arguments into a generated class instance.
     * 
     * @param xmlString the XML string to convert into a generated class object.
     * @param isEmbedded whether or not the load arguments should be returned as embedded load arguments. If true, the XML string will be
     * validated and returned as an {@link EmbeddedAppInputs} object. Otherwise the XML string will be validated and returned as an 
     * {@link InteropInputs} object.
     * @return generated.course.InteropInputs the new instance object created from the XML string.
     * @throws Exception if there was a problem unmarshalling the XML string
     */
    public static Serializable getLoadArgFromXMLString(String xmlString, boolean isEmbedded) throws Exception{
        
        UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(xmlString, isEmbedded ? EmbeddedAppInputs.class : InteropInputs.class, null, true);
        return isEmbedded ? (EmbeddedAppInputs) uFile.getUnmarshalled() : (InteropInputs) uFile.getUnmarshalled();
    }
    
    /**
     * Returns whether this is a SIMAN load that contains log playback references.
     * 
     * @return true if this is a SIMAN load that is intended to execute playback logic instead of load
     * an external system with a scenario.
     */
    public boolean isPlaybackLoadArgs(){
        
        if(getSimanTypeEnum() != SimanTypeEnum.LOAD){
            return false;
        }
        
        Map<String, Serializable> loadArgs = getLoadArgs();
        if(loadArgs != null){
            for(Serializable loadInputObj : loadArgs.values()){
                
                if(loadInputObj instanceof generated.course.DISInteropInputs){
                    generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs)loadInputObj;
                    if (disInteropInputs != null && disInteropInputs.getLogFile() != null && disInteropInputs.getLogFile().getDomainSessionLog() != null) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Return the size (bytes) of a training application file to load on the client side
     * 
     * @return will be zero if not set
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Set the size (bytes) of a training application file to load on the client side
     * 
     * @param fileSize can't be negative
     */
    public void setFileSize(long fileSize) {
        
        if(fileSize < 0){
            throw new IllegalArgumentException("The file size must be a non-negative number.");
        }
        
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Siman: ");
        sb.append(" type = ").append(getSimanTypeEnum());
        
        if(loadArgs != null){
            sb.append(", loadArgs = {");
        
            for(String impl : loadArgs.keySet()){
                
                Serializable inputs = loadArgs.get(impl);
                sb.append(" {").append(impl).append(":");
                
                try{
                    sb.append(getLoadArgsAsXMLString(inputs));
                }catch(@SuppressWarnings("unused") Exception e){
                    sb.append("<ERROR - caught exception during 'toString' of these load args>");
                }
                sb.append("},");
            }
            
            sb.append("}");
        }
        
        if(runtimeCourseFolderPath != null){
            sb.append(", runtimeCourseFolderPath = ").append(getRuntimeCourseFolderPath());
        }
        
        sb.append(", filesize = ").append(getFileSize());
        
        sb.append("]");

        return sb.toString();
    }

}
