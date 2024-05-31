/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

import generated.proto.common.GenericStateProto.GenericState;
import generated.proto.common.GenericStateProto.GenericValue;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a GenericState.
 * 
 * @author cpolynice
 *
 */
public class GenericStateProtoCodec implements ProtoCodec<GenericState, GenericJSONState> {

    @Override
    public GenericJSONState convert(GenericState protoObject) {
        if (protoObject == null) {
            return null;
        }

        GenericJSONState genericState;

        if (protoObject.hasUniqueId()) {
            genericState = new GenericJSONState(UUID.fromString(protoObject.getUniqueId().getValue()));
        } else {
            genericState = new GenericJSONState();
        }

        if (CollectionUtils.isNotEmpty(protoObject.getObjectMap())) {
            for (Map.Entry<String, GenericValue> genericEntry : protoObject.getObjectMap().entrySet()) {
                if (genericEntry.getValue() != null) {
                    try {
                        JSONParser parser = new JSONParser();
                        GenericValue value = genericEntry.getValue();
                        
                        if (value.hasIntValue()) {
                            genericState.setValueById(genericEntry.getKey(), value.getIntValue().getValue());
                        } else if (value.hasDoubleValue()) {
                            genericState.setValueById(genericEntry.getKey(), value.getDoubleValue().getValue());
                        } else if (value.hasLongValue()) {
                            genericState.setValueById(genericEntry.getKey(), value.getLongValue().getValue());
                        } else if (value.hasBooleanValue()) {
                            genericState.setValueById(genericEntry.getKey(), value.getBooleanValue().getValue());
                        } else if (value.hasStringValue()) {
                            genericState.setValueById(genericEntry.getKey(), value.getStringValue().getValue());
                        } else if (value.hasJsonObject()) {
                            String jsonString = value.getJsonObject().getValue();
                            Object jsonObject = parser.parse(jsonString);
                            
                            if (jsonObject instanceof JSONArray) {
                                genericState.addArraybyId(genericEntry.getKey(), (JSONArray) jsonObject);
                            } else if (jsonObject instanceof JSONObject) {
                                genericState.addObjectById(genericEntry.getKey(), (JSONObject) jsonObject);
                            }
                        }
                    } catch (ParseException e) {
                        throw new MessageDecodeException(this.getClass().getName(),
                                "Exception logged while converting ", e);
                    }
                }
            }
        }

        return genericState;
    }

    @Override
    public GenericState map(GenericJSONState commonObject) {
        if (commonObject == null) {
            return null;
        }

        GenericState.Builder builder = GenericState.newBuilder();

        if (commonObject.getJSONObject() != null) {
            for (Object key : commonObject.getJSONObject().keySet()) {
                if (commonObject.getJSONObject().get(key) != null) {
                    Object valueJSON = commonObject.getJSONObject().get(key);
                    GenericValue.Builder value = GenericValue.newBuilder();

                    /* Check for the types that GenericJSONState supports. Start
                     * with primitive values first. */
                    if (valueJSON instanceof Integer) {
                        value.setIntValue(Int32Value.of((int) valueJSON));
                    } else if (valueJSON instanceof Double) {
                        value.setDoubleValue(DoubleValue.of((double) valueJSON));
                    } else if (valueJSON instanceof Long) {
                        value.setLongValue(Int64Value.of((long) valueJSON));
                    } else if (valueJSON instanceof Boolean) {
                        value.setBooleanValue(BoolValue.of((boolean) valueJSON));
                    } else if (valueJSON instanceof String) {
                        value.setStringValue(StringValue.of((String) valueJSON));
                    }
                    /* JSON object type. Encode inside protobuf as a JSONString
                     * so that it can be parsed out when converting. */
                    else if (valueJSON instanceof JSONArray) {
                        value.setJsonObject(StringValue.of(((JSONArray) valueJSON).toJSONString()));
                    } else {
                        value.setJsonObject(StringValue.of(((JSONObject) valueJSON).toJSONString()));
                    }

                    builder.putObject((String) key, value.build());
                }
            }
        }

        Optional.ofNullable(commonObject.getUUID()).ifPresent(uuid -> {
            builder.setUniqueId(StringValue.of(uuid.toString()));
        });

        return builder.build();
    }
}
