/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.math.BigDecimal;
import java.util.Optional;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import generated.proto.common.EnvironmentControlProto;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Environment
 * Control.
 * 
 * @author cpolynice
 *
 */
public class EnvironmentControlProtoCodec
        implements ProtoCodec<EnvironmentControlProto.EnvironmentControl, EnvironmentControl> {

    /**
     * Convert the legacy name:value pairs for environment conditions of
     * scenario adaptation into new XML elements (i.e. generated classes)
     * 
     * @param value the old EnvironmentControlEnum.java enumeration name value.
     * @return a new environment adaptation object that contains the parameters
     *         previously used for that enumeration.
     */
    private generated.dkf.EnvironmentAdaptation buildFromLegacy(String value) {

        generated.dkf.EnvironmentAdaptation newEnvAdapt = new generated.dkf.EnvironmentAdaptation();

        if (value.equals("Overcast")) {
            generated.dkf.EnvironmentAdaptation.Overcast overcast = new generated.dkf.EnvironmentAdaptation.Overcast();
            overcast.setValue(new BigDecimal(
                    0.9)); /* value came from the old
                            * GIFT/config/gateway/externalApplications/VBS/vbs.
                            * control.properties file */
            newEnvAdapt.setType(overcast);

        } else if (value.equals("FogLevel1")) {

            generated.dkf.EnvironmentAdaptation.Fog fog = new generated.dkf.EnvironmentAdaptation.Fog();
            fog.setDensity(new BigDecimal(
                    0.1)); /* value came from the old
                            * GIFT/config/gateway/externalApplications/VBS/vbs.
                            * control.properties file */
            newEnvAdapt.setType(fog);

        } else if (value.equals("FogLevel2")) {

            generated.dkf.EnvironmentAdaptation.Fog fog = new generated.dkf.EnvironmentAdaptation.Fog();
            fog.setDensity(new BigDecimal(
                    0.4)); /* value came from the old
                            * GIFT/config/gateway/externalApplications/VBS/vbs.
                            * control.properties file */
            newEnvAdapt.setType(fog);

        } else if (value.equals("FogLevel3")) {

            generated.dkf.EnvironmentAdaptation.Fog fog = new generated.dkf.EnvironmentAdaptation.Fog();
            fog.setDensity(new BigDecimal(
                    0.6)); /* value came from the old
                            * GIFT/config/gateway/externalApplications/VBS/vbs.
                            * control.properties file */
            newEnvAdapt.setType(fog);

        } else if (value.equals("FogLevel4")) {

            generated.dkf.EnvironmentAdaptation.Fog fog = new generated.dkf.EnvironmentAdaptation.Fog();
            fog.setDensity(new BigDecimal(
                    0.9)); /* value came from the old
                            * GIFT/config/gateway/externalApplications/VBS/vbs.
                            * control.properties file */
            newEnvAdapt.setType(fog);

        } else if (value.equals("Rain")) {

            generated.dkf.EnvironmentAdaptation.Rain rain = new generated.dkf.EnvironmentAdaptation.Rain();
            rain.setValue(new BigDecimal(
                    0.5)); /* value came from the old
                            * GIFT/config/gateway/externalApplications/VBS/vbs.
                            * control.properties file */
            newEnvAdapt.setType(rain);

        } else if (value.equals("Clear")) {
            // no longer supported

        } else if (value.equals("TimeOfDayDusk")) {

            generated.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.dkf.EnvironmentAdaptation.TimeOfDay();
            tod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Dusk());
            newEnvAdapt.setType(tod);

        } else if (value.equals("TimeOfDayDawn")) {

            generated.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.dkf.EnvironmentAdaptation.TimeOfDay();
            tod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Dawn());
            newEnvAdapt.setType(tod);

        } else if (value.equals("TimeOfDayMidday")) {

            generated.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.dkf.EnvironmentAdaptation.TimeOfDay();
            tod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Midday());
            newEnvAdapt.setType(tod);

        } else if (value.equals("TimeOfDayMidnight")) {

            generated.dkf.EnvironmentAdaptation.TimeOfDay tod = new generated.dkf.EnvironmentAdaptation.TimeOfDay();
            tod.setType(new generated.dkf.EnvironmentAdaptation.TimeOfDay.Midnight());
            newEnvAdapt.setType(tod);

        }

        return newEnvAdapt;
    }

    @Override
    public EnvironmentControl convert(EnvironmentControlProto.EnvironmentControl protoObject) {
        if (protoObject == null) {
            return null;
        }

        generated.dkf.EnvironmentAdaptation environmentType;

        String envType = protoObject.hasEnvironmentType() ? protoObject.getEnvironmentType().getValue() : null;
        try {
            UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(envType,
                    generated.dkf.EnvironmentAdaptation.class, AbstractSchemaHandler.DKF_SCHEMA_FILE, false);
            environmentType = (generated.dkf.EnvironmentAdaptation) uFile.getUnmarshalled();
        } catch (@SuppressWarnings("unused") Exception e) {

            // this could be a legacy message
            try {
                environmentType = buildFromLegacy(envType);
            } catch (Exception e2) {
                throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e2);
            }
        }

        EnvironmentControl eControl = new EnvironmentControl(environmentType);
        if(protoObject.hasStress()) {
            eControl.setStress(protoObject.getStress().getValue());
        }
        
        return eControl;
    }

    @Override
    public EnvironmentControlProto.EnvironmentControl map(EnvironmentControl commonObject) {
        if (commonObject == null) {
            return null;
        }

        EnvironmentControlProto.EnvironmentControl.Builder builder = EnvironmentControlProto.EnvironmentControl
                .newBuilder();

        try {
            Optional.ofNullable(AbstractSchemaHandler.getAsXMLString(commonObject.getEnvironmentStatusType(),
                    generated.dkf.EnvironmentAdaptation.class, AbstractSchemaHandler.DKF_SCHEMA_FILE))
                    .ifPresent(type -> {
                        builder.setEnvironmentType(StringValue.of(type));
                    });
            
            Optional.ofNullable(commonObject.getStress()).ifPresent(stress -> {
                builder.setStress(DoubleValue.of(stress));
            });
            
            return builder.build();
        } catch (Exception e) {
            throw new MessageEncodeException(this.getClass().getName(),
                    "There was a problem converting the generated class to a string", e);
        }
    }

}
