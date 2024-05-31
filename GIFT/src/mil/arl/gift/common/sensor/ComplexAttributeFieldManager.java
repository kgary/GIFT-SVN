/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a centralized location to register sensor data attribute value classes and the fields 
 * associated with the data.  As each sensor attribute value class implementation class is loaded, it can register
 * its fields if it needs too.  Usually only complex data classes, like Tuple3dValue, which have multiple fields (x, y and z)
 * for a single instance will register with this class.
 * 
 * It is understood that if a sensor attribute value class is not registered with this manager, then the attribute
 * is deemed not complex (i.e. it has only one field) and its value should be accessible with the given methods layed out
 * in AbstractSensorAttributeValue.class.
 * 
 * @author mhoffman
 *
 */
public class ComplexAttributeFieldManager {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ComplexAttributeFieldManager.class);    
    
    /**
     * labels for the different fields of a 3d attribute
     */
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";
    
    /** 
     * map for sensor attribute value implementations to their collection of attributes 
     * e.g. The Tuple3dValue class would be a key to the list of field objects for X, Y and Z 
     */
    private static Map<Class<? extends AbstractSensorAttributeValue>, List<ComplexAttributeField>> classAttributeFields = new HashMap<>();
    
    /** 
     * Register the getter methods of complex sensor data objects in order to know how to retrieve the different values.
     * 
     * Note: the getter methods must return the types specified elsewhere in this class (ACCEPTABLE_RETURN_TYPES list)
     */
    static{
        
        try{
            
            // Tuple3dValue
            List<ComplexAttributeField> tuple3dAttributeFields = new ArrayList<>();
            tuple3dAttributeFields.add(new ComplexAttributeField(X, Tuple3dValue.class.getMethod("getX", (Class<?>[]) null)));
            tuple3dAttributeFields.add(new ComplexAttributeField(Y, Tuple3dValue.class.getMethod("getY", (Class<?>[]) null)));
            tuple3dAttributeFields.add(new ComplexAttributeField(Z, Tuple3dValue.class.getMethod("getZ", (Class<?>[]) null)));
            classAttributeFields.put(Tuple3dValue.class, tuple3dAttributeFields);
        
        }catch(Exception e){
            e.printStackTrace();
            logger.error("Caught exception while trying to register complex sensor data objects.", e);
        }
    }

    /** the singleton instance of this class */
    private static ComplexAttributeFieldManager instance = null;
    
    /**
     * Return the singleton instance of this class.
     * 
     * @return ComplexAttributeFieldManager the singleton instance
     */
    public static ComplexAttributeFieldManager getInstance(){
        
        if(instance == null){
            instance = new ComplexAttributeFieldManager();
        }
        
        return instance;
    }
    
    private ComplexAttributeFieldManager(){}
    
    /**
     * Add an attribute field (e.g. "X") for the specified sensor data attribute class (e.g. "Tuple3dValue").
     * 
     * @param complextAttributeClass an implementation of the sensor attribute value class to associate with the provided attribute field information
     * @param attributeField information about an field for the attribute value class implementation that needs to be registered for use later
     */
    public void registerFieldForAttributeClass(Class<? extends AbstractSensorAttributeValue> complextAttributeClass, ComplexAttributeField attributeField){
        
        List<ComplexAttributeField> attributeFields = classAttributeFields.get(complextAttributeClass);
        if(attributeFields == null){
            attributeFields = new ArrayList<>();
            classAttributeFields.put(complextAttributeClass, attributeFields);
        }
        
        attributeFields.add(attributeField);
    }
    
    /**
     * Return the collection of fields for a specific sensor attribute value class (e.g. Tuple3dValue).
     * 
     * @param complextAttributeClass the class to get its associated fields for
     * @return List<ComplexAttributeField> the collection of fields associated with the sensor attribute value class implementation
     */
    public List<ComplexAttributeField> getFieldsForAttributeClass(Class<? extends AbstractSensorAttributeValue> complextAttributeClass){
        return classAttributeFields.get(complextAttributeClass);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ComplexAttributeFieldManager: ");
        
        sb.append("\nclassAttributeFields = {");
        for(Class<? extends AbstractSensorAttributeValue> clazz : classAttributeFields.keySet()){
            
            sb.append("\n").append(clazz.getName()).append(" : {");
            for(ComplexAttributeField field : classAttributeFields.get(clazz)){
                sb.append(" ").append(field).append(",");
            }
            sb.append("\n},");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * This inner class contains the information for a single sensor data attribute field (e.g. Tuple3dValue's X field). 
     * It contains the label that should be applied to values for this field (e.g. "X") and the method that can be used to 
     * retrieve a value associated with that label from a sensor data attribute implementation class (e.g. Tuple3dValue).
     * 
     * For example: a complex sensor data object like a Tuple3d should contain 3 method entries, one for each attribute of a tuple 3d object:
     *  "getX", "getY", "getZ"
     
     * @author mhoffman
     *
     */
    public static class ComplexAttributeField{
        
        static final List<Class<?>> ACCEPTABLE_RETURN_TYPES = new ArrayList<>();{
            ACCEPTABLE_RETURN_TYPES.add(Boolean.class);
            ACCEPTABLE_RETURN_TYPES.add(boolean.class);
            ACCEPTABLE_RETURN_TYPES.add(Character.class);
            ACCEPTABLE_RETURN_TYPES.add(char.class);
            ACCEPTABLE_RETURN_TYPES.add(Byte.class);
            ACCEPTABLE_RETURN_TYPES.add(byte.class);
            ACCEPTABLE_RETURN_TYPES.add(Short.class);
            ACCEPTABLE_RETURN_TYPES.add(short.class);
            ACCEPTABLE_RETURN_TYPES.add(Integer.class);
            ACCEPTABLE_RETURN_TYPES.add(int.class);
            ACCEPTABLE_RETURN_TYPES.add(Long.class);
            ACCEPTABLE_RETURN_TYPES.add(long.class);
            ACCEPTABLE_RETURN_TYPES.add(Float.class);
            ACCEPTABLE_RETURN_TYPES.add(float.class);
            ACCEPTABLE_RETURN_TYPES.add(Double.class);
            ACCEPTABLE_RETURN_TYPES.add(double.class);
            ACCEPTABLE_RETURN_TYPES.add(String.class);
        }
        
        /** 
         * unique label for an attribute of a complex sensor data object 
         * (e.g. data object is Acceleration, some labels might be "X", "Y" and "Z" 
         */
        private String label;
        
        /**
         * The getter method for an attribute field with the given label which must return
         * one of the ACCEPTABLE_RETURN_TYPES data types.
         */
        private Method getterMethod;
        
        /**
         * Class constructor - set attributes
         * 
         * @param label unique label for an attribute of a complex sensor data object 
         * @param getterMethod The getter method for an attribute field with the given label which must return
         * one of the ACCEPTABLE_RETURN_TYPES data types.
         */
        public ComplexAttributeField(String label, Method getterMethod){
            
            if(label == null){
                throw new IllegalArgumentException("The label can't be null.");
            }
            
            this.label = label;
            
            setGetterMethod(getterMethod);
        }
        
        /**
         * Check and then set the getter method provided.
         * 
         * @param getterMethod the getter method for a field of a sensor data attribute
         */
        private void setGetterMethod(Method getterMethod){
            
            if(getterMethod == null){
                throw new IllegalArgumentException("The getter method can't be null.");
            }   

            Class<?> returnType;
            try{
                returnType = getterMethod.getReturnType();
            }catch(Exception e){
                logger.error("Caught exception while checking the attribute label of "+label+" for sensor attribute "+label, e);
                
                throw new IllegalArgumentException("Caught exception while checking the attribute label of "+label+" for sensor attribute "+label);
            }

            
            //Check the return type
            if(!(ACCEPTABLE_RETURN_TYPES.contains(returnType))){
                throw new IllegalArgumentException("The method of "+getterMethod.getName()+" for sensor attribute label of "+label+" returns a value of type "+returnType+" which is not a valid return type.");
            }
            
            this.getterMethod = getterMethod;
        }
        
        /**
         * Return the unique label for an attribute of a complex sensor data object 
         * 
         * @return String a sensor data attribute field label
         */
        public String getLabel(){
            return label;
        }
        
        /**
         * Return the getter method for an attribute field with the given label
         * 
         * @return Method the method that can be used to retrieve a value for a sensor data attribute field
         */
        public Method getMethod(){
            return getterMethod;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ComplexAttributeField: ");
            sb.append("label = ").append(getLabel());
            sb.append(", getterMethod = ").append(getMethod());
            sb.append("]");
            return sb.toString();
        }
    }
}
