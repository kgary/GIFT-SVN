/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Map;

import generated.proto.common.ExternalMonitorConfigProto;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ExternalMonitorConfig.
 * 
 * @author cpolynice
 *
 */
public class ExternalMonitorConfigProtoCodec
        implements ProtoCodec<ExternalMonitorConfigProto.ExternalMonitorConfig, ExternalMonitorConfig> {

    @Override
    public ExternalMonitorConfig convert(ExternalMonitorConfigProto.ExternalMonitorConfig protoObject) {
        if (protoObject == null) {
            return null;
        }

        ExternalMonitorConfig config = new ExternalMonitorConfig();
        Map<String, Boolean> protoMap = CollectionUtils.isNotEmpty(protoObject.getSettingMapMap())
                ? protoObject.getSettingMapMap()
                : null;

        if (protoMap != null) {
            for (ExternalMonitorConfig.Setting setting : ExternalMonitorConfig.Setting.values()) {
                Boolean value = protoMap.get(setting.name());

                if (value != null) {
                    config.set(setting, value);
                }
            }

            return config;
        } else {
            return null;
        }

    }

    @Override
    public ExternalMonitorConfigProto.ExternalMonitorConfig map(ExternalMonitorConfig commonObject) {
        if (commonObject == null) {
            return null;
        }

        ExternalMonitorConfigProto.ExternalMonitorConfig.Builder builder = ExternalMonitorConfigProto.ExternalMonitorConfig
                .newBuilder();

        Map<ExternalMonitorConfig.Setting, Boolean> settingMap = CollectionUtils.isNotEmpty(commonObject.getSettings())
                ? commonObject.getSettings()
                : null;

        if (settingMap != null) {
            for (ExternalMonitorConfig.Setting setting : settingMap.keySet()) {
                builder.putSettingMap(setting.name(), settingMap.get(setting));
            }
        }

        return builder.build();
    }
}
