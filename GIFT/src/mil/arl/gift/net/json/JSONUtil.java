/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.net.api.message.MessageDecodeException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A class of utility methods for creating and parsing JSON messages
 *
 * @author jleonard
 */
public class JSONUtil {

    /**
     * Decode the provided JSON object's encoding of a list back into a map with
     * the appropriate value type checked objects.
     *
     * @param <T> The type of object the codec will decode
     * @param array A JSONArray that has the
     * @param codec Used to decode the object from it's JSON representation
     * @return List<T> The decoded list
     * @throws MessageDecodeException When there is an issue decoding the array
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> decodeList(JSONArray array, JSONCodec codec) throws MessageDecodeException {

        List<T> results = null;

        if (array != null) {

            results = new ArrayList<>(array.size());

            for (Object obj : array) {

                Object result = codec.decode((JSONObject) obj);
                results.add((T) result);
            }
        }

        return results;
    }

    /**
     * Decodes a JSON-encoded map in to it's Java representation
     *
     * @param <T> The type of object used as the key in the map
     * @param <U> The type of object used as the value in the map
     * @param jsonObj The JSON object that contains the map.
     *          Note:  the map key's must be JSON simple types (e.g. Strings, Long) and the values can be objects or list of
     *          objects decoded by the codec
     * @param codec The codec used to convert the JSON representation of the map's values into Java objects
     * @return Map<T, U> The decoded map
     */
    @SuppressWarnings("unchecked")
    public static <T, U> Map<T, U> decodeMap(JSONObject jsonObj, JSONCodec codec) {

        Map<T, U> map = new HashMap<>();

        for (Object key : jsonObj.keySet()) {

            Object object = jsonObj.get(key);
            if(object instanceof JSONArray){
                
                U items = (U) decodeList((JSONArray) object, codec);
                map.put((T) key, items);
                
            }else{
                U attr = (U) codec.decode((JSONObject) jsonObj.get(key));
                map.put((T) key, attr);
            }
        }

        return map;
    }
    
    /**
     * Decodes a JSON-encoded map in to it's Java representation
     *
     * @param <T> The type of object used as the key in the map
     * @param <U> The type of object used as the value in the map
     * @param jsonObj The JSON object that contains the map.
     *          Note:  The values can be objects or list of objects decoded by the codec
     * @param keyCodec the codec used to convert the JSON representation of the map's keys into Java objects
     * @param valueCodec The codec used to convert the JSON representation of the map's values into Java objects
     * @return Map<T, U> The decoded map
     * @throws ParseException if there was a problem parsing the map's key values which are strings that are JSON encoded.
     */
    @SuppressWarnings("unchecked")
    public static <T, U> Map<T, U> decodeMap(JSONObject jsonObj, JSONCodec keyCodec, JSONCodec valueCodec) throws ParseException {

        Map<T, U> map = new HashMap<>();

        JSONParser jParser = new JSONParser();
        for (Object keyObject : jsonObj.keySet()) {
            
            //
            // Handle key
            //
            JSONObject keyJObject = (JSONObject) jParser.parse((String) keyObject);
            T key = (T) keyCodec.decode(keyJObject);

            //
            // Handle value
            //
            Object valueObject = jsonObj.get(keyObject);
            if(valueObject instanceof JSONArray){
                
                U items = (U) decodeList((JSONArray) valueObject, valueCodec);
                map.put(key, items);
                
            }else{
                U value = (U) valueCodec.decode((JSONObject) jsonObj.get(keyObject));
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * Decodes a JSON-encoded map in to it's Java representation with an
     * enumeration as it's key
     *
     * @param <T> The type of Abstract Enum used as the key in the map
     * @param <U> The type of object used as the value in the map
     * @param jsonObj The JSON object that contains the map
     * @param enumClass The enumeration class
     * @param codec The codec used to convert the JSON representation of the
     * value into a Java object
     * @return Map<T, U> The decoded map
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEnum, U> Map<T, U> decodeEnumKeyMap(JSONObject jsonObj, Class<T> enumClass, JSONCodec codec) {

        Map<T, U> map = new HashMap<>();

        for (Object key : jsonObj.keySet()) {

            U attr = (U) codec.decode((JSONObject) jsonObj.get(key));
            
            if(enumClass.equals(SurveyPropertyKeyEnum.class) && AbstractEnum.valueOf(SurveyPropertyKeyEnum.class, (String) key) == null){
            	
            	//if a survey property key was loaded from the UMS database, it needs to be added to the other modules' copies of SurveyPropertyKeyEnum 
            	map.put((T) SurveyPropertyKeyEnum.createEnumeration(key.toString()), attr);
            	break;
            }
            
            map.put(AbstractEnum.valueOf(enumClass, (String) key), attr);
        }

        return map;
    }

    /**
     * Decodes a list of Integers (encoded as Longs in JSON) in JSON to their Java representation
     *
     * @param array The JSON encoded list of Longs
     * @return List<Integer> The decoded list of Integers
     */
    public static List<Integer> decodeIntegerList(JSONArray array) {

        List<Integer> results = null;

        if (array != null) {

            results = new ArrayList<>(array.size());

            for (Object obj : array) {
                results.add(((Long) obj).intValue());
            }
        }

        return results;
            }

    /**
     * Decodes a list of numbers (not Integers which are encoded as Long) in JSON to their Java representation
     * Note: the reason this method is not for Integers is that JSON encoding changes Integers to Longs, therefore
     * use the decodeIntegerNumberList method.
     *
     * @param array The JSON encoded list of numbers
     * @param <T> the type of objects in the returned list
     * @return The decoded list of numbers
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> List<T> decodeNonIntegerNumberList(JSONArray array) {

        List<T> results = null;

        if (array != null) {

            results = new ArrayList<>(array.size());

            for (Object obj : array) {                
                results.add((T) obj);
        }
        }

        return results;
    }

    /**
     * Decodes a list of strings in JSON to their Java representation
     *
     * @param array The JSON encoded list of strings
     * @return The decoded list of strings.  Can be null or empty.
     */
    public static List<String> decodeStringList(JSONArray array) {

        List<String> results = null;

        if (array != null) {

            results = new ArrayList<>(array.size());

            for (Object obj : array) {

                results.add((String) obj);
            }
        }

        return results;
    }

    /**
     * Decode the provided JSON object's encoding of a list back into a map with
     * the appropriate value type checked objects.
     *
     * @param <T> The type of object the codec will encode
     * @param objectList The list of objects to encode
     * @param codec Used to encode the object into it's JSON representation
     * @return JSONArray The JSON array of the encoded list
     */
    @SuppressWarnings("unchecked")
    public static <T> JSONArray encodeList(Collection<T> objectList, JSONCodec codec) {

        JSONArray array = new JSONArray();

        for (T object : objectList) {

            JSONObject recordObj = new JSONObject();

            codec.encode(recordObj, object);
            array.add(recordObj);
        }

        return array;
    }

    /**
     * Encodes a map in to it's JSON representation.
     * Note: the key <T> for the map must be JSON simple types (e.g. String, Longs).  The values can be objects or list of
     *          objects decoded by the codec.
     *
     * @param <T> The type of object used as the key in the map
     * @param <U> The type of object used as the value in the map
     * @param mapObj The JSON object to put the map into
     * @param map The map to encode
     * @param codec The codec used to convert the value into its JSON
     * representation
     */
    @SuppressWarnings("unchecked")
    public static <T, U> void encodeMap(JSONObject mapObj, Map<T, U> map, JSONCodec codec) {

        for (T t : map.keySet()) {

            JSONObject attrObj = new JSONObject();
            
            if(map.get(t) instanceof List){
                
                JSONArray array =  encodeList((List<?>)map.get(t), codec);
                mapObj.put(t, array);
                
            }else{
                codec.encode(attrObj, map.get(t));
                mapObj.put(t, attrObj);
            }
            
        }
    }
    
    /**
     * Encodes a map in to it's JSON representation.
     * Note: The values can be objects or list of objects decoded by the valueCodec.
     *
     * @param <T> The type of object used as the key in the map
     * @param <U> The type of object used as the value in the map
     * @param mapObj The JSON object to put the map into
     * @param map The map to encode
     * @param keyCodec The codec used to convert the key into its JSON representation
     * @param valueCodec The codec used to convert the value into its JSON representation
     */
    @SuppressWarnings("unchecked")
    public static <T, U> void encodeMap(JSONObject mapObj, Map<T, U> map, JSONCodec keyCodec, JSONCodec valueCodec) {

        for (T t : map.keySet()) {
            
            //
            // Handle key
            //
            JSONObject keyObj = new JSONObject();
            keyCodec.encode(keyObj, t);
            
            //
            // Handle value and add to encoded map
            //
            if(map.get(t) instanceof List){
                
                JSONArray array =  encodeList((List<?>)map.get(t), valueCodec);
                mapObj.put(keyObj, array);
                
            }else{
                JSONObject valueObj = new JSONObject();
                valueCodec.encode(valueObj, map.get(t));
                mapObj.put(keyObj, valueObj);
            }
            
        }
    }

    /**
     * Encodes a map in to it's JSON representation with an enumeration as the
     * key
     *
     * @param <T> The type of Abstract Enum used as the key in the map
     * @param <U> The type of object used as the value in the map
     * @param mapObj The JSON object to put the map into
     * @param map The map to encode
     * @param codec The codec used to convert the value into its JSON
     * representation
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEnum, U> void encodeEnumKeyMap(JSONObject mapObj, Map<T, U> map, JSONCodec codec) {

        for (T t : map.keySet()) {

            JSONObject attrObj = new JSONObject();
            codec.encode(attrObj, map.get(t));
            mapObj.put(t.getName(), attrObj);
        }
    }

    /**
     * Encodes a list of numbers in to a JSON array
     *
     * @param list The list of numbers
     * @return JSONArray The JSON encoded list of numbers
     */
    @SuppressWarnings("unchecked")
    public static JSONArray encodeNumberList(List<? extends Number> list) {

        JSONArray array = null;

        if (list != null) {

            array = new JSONArray();

            for (Number value : list) {

                array.add(value);
            }
        }

        return array;
    }

    /**
     * Encodes a list of strings in to a JSON array
     *
     * @param list The list of strings
     * @return JSONArray The JSON encoded list of strings
     */
    @SuppressWarnings("unchecked")
    public static JSONArray encodeStringList(Collection<? extends String> list) {

        JSONArray array = null;

        if (list != null) {

            array = new JSONArray();

            for (String value : list) {

                array.add(value);
            }
        }

        return array;
    }
}
