/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message.codec.json;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingAppRouteTypeEnum;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.embedded.message.EmbeddedSiman;
import mil.arl.gift.net.json.JSONCodec;

/**
 * This class is responsible for JSON encoding/decoding a simulation management (SIMAN) request.
 *
 * @author jleonard
 */
public class EmbeddedSimanJSON implements JSONCodec {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmbeddedSimanJSON.class);

    /** Message attribute names */
    private static final String SIMAN_TYPE = "Siman_Type";

    public static final String LOAD_ARGS = "LoadArgs";
    private static final String COURSE_FOLDER = "CourseFolder";
    private static final String FILE_SIZE = "FileSize";
    private static final String ROUTE_TYPE = "RouteType";

    //Legacy - pre 4.1
    private static final String SIMAN_DATA = "Siman_Data";

    @SuppressWarnings("unchecked")
    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try {

            SimanTypeEnum simanType = SimanTypeEnum.valueOf((String) jsonObj.get(SIMAN_TYPE));

            long fileSize = 0l;
            if(jsonObj.containsKey(FILE_SIZE)){
                fileSize = (Long)jsonObj.get(FILE_SIZE);
            }

            TrainingAppRouteTypeEnum routeType = null;
            if(jsonObj.containsKey(ROUTE_TYPE)) {
            	routeType = TrainingAppRouteTypeEnum.valueOf((String) jsonObj.get(ROUTE_TYPE));
            } else {
            	routeType = TrainingAppRouteTypeEnum.INTEROP;
            }

            JSONObject loadArgsObj = (JSONObject) jsonObj.get(LOAD_ARGS);

            if (loadArgsObj != null) {

                Map<String, Serializable> loadArgs = decodeLoadArgs(loadArgsObj, routeType == TrainingAppRouteTypeEnum.EMBEDDED);
                EmbeddedSiman simanLoad = EmbeddedSiman.CreateLoad(loadArgs);
                if(jsonObj.containsKey(COURSE_FOLDER)){
                    simanLoad.setRuntimeCourseFolderPath((String) jsonObj.get(COURSE_FOLDER));
                }

                simanLoad.setFileSize(fileSize);
                simanLoad.setRouteType(routeType);

                return simanLoad;

            }else if(simanType == SimanTypeEnum.LOAD && jsonObj.containsKey(SIMAN_DATA)){
                //Pre 4.1 load arguments in the form of Name:Value pairs - handling decoding logic
                //for applications such as the ERT.

                Map<String, String> simanData = new HashMap<>();
                JSONObject simanDataObj = (JSONObject) jsonObj.get(SIMAN_DATA);
                simanData.putAll(simanDataObj);

                //convert to InteropInputs
                generated.course.CustomInteropInputs inputs = new generated.course.CustomInteropInputs();
                generated.course.CustomInteropInputs.LoadArgs args = new generated.course.CustomInteropInputs.LoadArgs();

                for(String name : simanData.keySet()){
                    generated.course.Nvpair nvPair = new generated.course.Nvpair();
                    nvPair.setName(name);
                    nvPair.setValue(simanData.get(name));
                    args.getNvpair().add(nvPair);
                }

                inputs.setLoadArgs(args);

                Map<String, Serializable> loadArgs = new HashMap<>();
                generated.course.InteropInputs interopInputs = new generated.course.InteropInputs();
                interopInputs.setInteropInput(inputs);
                loadArgs.put(LOAD_ARGS, interopInputs);

                EmbeddedSiman simanLoad = EmbeddedSiman.CreateLoad(loadArgs);
                simanLoad.setFileSize(fileSize);
                simanLoad.setRouteType(routeType);

                return simanLoad;

            }else{

                EmbeddedSiman siman = EmbeddedSiman.Create(simanType);
                siman.setFileSize(fileSize);
                siman.setRouteType(routeType);
                return siman;
            }

        } catch (Exception e) {

            logger.error("caught exception while creating an SIMAN message from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

    }

    /**
     * Decode the load arguments.
     *
     * @param loadArgsObj the JSON encoding of the load args
     * @param isEmbedded specifies whether the JSON Object is an embeddedApp or an interop
     * @return Map<String, generated.course.InteropInputs> the decoded load args
     * @throws MessageDecodeException if there was a problem decoding the args
     */
    private Map<String, Serializable> decodeLoadArgs(JSONObject loadArgsObj, boolean isEmbedded) throws MessageDecodeException{

        Map<String, Serializable> loadArgsMap = new HashMap<>();
        for(Object key : loadArgsObj.keySet()){

            try{
                String value = (String) loadArgsObj.get(key);
                Serializable inputs = value != null ? EmbeddedSiman.getLoadArgFromXMLString(value, isEmbedded) : null;
                loadArgsMap.put((String) key, inputs);
            }catch(Exception e){
                logger.error("Caught exception while trying to decode the load args for interop implementation of "+key+" for SIMAN request.", e);
                throw new MessageEncodeException(this.getClass().getName(), "Exception logged while decoding");
            }
        }

        return loadArgsMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) throws MessageEncodeException {

        EmbeddedSiman siman = (EmbeddedSiman) payload;

        //write class attributes
        jsonObj.put(SIMAN_TYPE, siman.getSimanTypeEnum().toString());

        jsonObj.put(ROUTE_TYPE, siman.getRouteType().toString());

        encodeLoadArgs(jsonObj, siman);

        if(siman.getRuntimeCourseFolderPath() != null){
            jsonObj.put(COURSE_FOLDER, siman.getRuntimeCourseFolderPath());
        }

        jsonObj.put(FILE_SIZE, siman.getFileSize());
    }

    /**
     * Encode the load arguments of the SIMAN object.
     *
     * @param jsonObj - the object to place the encoded values in
     * @param siman - the object needing its load arguments encoded
     * @throws MessageEncodeException if there was a severe problem during encoding
     */
    @SuppressWarnings("unchecked")
    private void encodeLoadArgs(JSONObject jsonObj, EmbeddedSiman siman) throws MessageEncodeException {

        if (siman.getLoadArgs() != null) {

            JSONObject loadArgsJSON = new JSONObject();
            Map<String, Serializable> loadArgs = siman.getLoadArgs();
            for(String impl : loadArgs.keySet()){

                try{
                    String inputs = EmbeddedSiman.getLoadArgsAsXMLString(loadArgs.get(impl));
                    loadArgsJSON.put(impl, inputs);

                }catch(Exception e){
                    logger.error("Caught exception while trying to encode the load args for interop implementation of "+impl+" for SIMAN request.", e);
                    throw new MessageEncodeException(this.getClass().getName(), "Exception logged while encoding", e);
                }
            }

            jsonObj.put(LOAD_ARGS, loadArgsJSON);
        }
    }
}
