/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.course.Concepts;
import generated.proto.common.DomainOptionPermissionsProto;
import generated.proto.common.DomainOptionProto;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf DomainOption.
 * 
 * @author cpolynice
 *
 */
public class DomainOptionProtoCodec implements ProtoCodec<DomainOptionProto.DomainOption, DomainOption> {

    /* Codec that will be used to convert to/from a DomainOptionPermissions. */
    private static DomainOptionPermissionsProtoCodec permCodec = new DomainOptionPermissionsProtoCodec();

    /* Codec that will be used to convert to/from an Abstract Enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts the protobuf DomainOptionRecommendation to the common object
     * representation.
     * 
     * @param protoObject the protobuf DomainOptionRecommendation
     * @return the converted common object.
     */
    private static DomainOptionRecommendation convertRecommendation(
            DomainOptionProto.DomainOptionRecommendation protoObject) {
        if (protoObject == null) {
            return null;
        }

        DomainOptionRecommendation recommendation;

        if (protoObject.hasType()) {
            DomainOptionRecommendationEnum reason = (DomainOptionRecommendationEnum) enumCodec
                    .convert(protoObject.getType());
            recommendation = new DomainOptionRecommendation(reason);

            if (protoObject.hasReason()) {
                recommendation.setReason(protoObject.getReason().getValue());
            }

            if (protoObject.hasDetails()) {
                recommendation.setDetails(protoObject.getDetails().getValue());
            }
        } else {
            throw new MessageDecodeException(DomainOptionProtoCodec.class.getName(),
                    "Exception logged while converting ");
        }

        return recommendation;
    }

    /**
     * Maps the given common object DomainOptionRecommendation to the protobuf
     * builder.
     * 
     * @param commonObject the common object DomainOptionRecommendation
     * @return the protobuf builder containing the message.
     */
    private static DomainOptionProto.DomainOptionRecommendation mapRecommendation(
            DomainOptionRecommendation commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainOptionProto.DomainOptionRecommendation.Builder builder = DomainOptionProto.DomainOptionRecommendation
                .newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getDomainOptionRecommendationEnum()))
                .ifPresent(builder::setType);
        Optional.ofNullable(commonObject.getReason()).ifPresent(reason -> {
            builder.setReason(StringValue.of(reason));
        });
        Optional.ofNullable(commonObject.getDetails()).ifPresent(details -> {
            builder.setDetails(StringValue.of(details));
        });

        return builder.build();
    }

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<DomainOptionPermissions> convertList(
            List<DomainOptionPermissionsProto.DomainOptionPermissions> protoList) {
        if (protoList == null) {
            return null;
        }

        List<DomainOptionPermissions> commonList = new ArrayList<>();

        for (DomainOptionPermissionsProto.DomainOptionPermissions permission : protoList) {
            commonList.add(permCodec.convert(permission));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list.
     */
    private static List<DomainOptionPermissionsProto.DomainOptionPermissions> mapList(
            List<DomainOptionPermissions> commonList) {
        if (commonList == null) {
            return null;
        }

        List<DomainOptionPermissionsProto.DomainOptionPermissions> protoList = new ArrayList<>();

        for (DomainOptionPermissions permission : commonList) {
            protoList.add(permCodec.map(permission));
        }

        return protoList;
    }

    @Override
    public DomainOption convert(DomainOptionProto.DomainOption protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            String username = protoObject.hasUserName() ? protoObject.getUserName().getValue() : null;
            String domainName = protoObject.hasDomainName() ? protoObject.getDomainName().getValue() : null;
            String domainId = protoObject.hasDomainId() ? protoObject.getDomainId().getValue() : null;
            String sourceId = protoObject.hasSourceId() ? protoObject.getSourceId().getValue() : null;
            String description = protoObject.hasDescription() ? protoObject.getDescription().getValue() : null;

            DomainOption option = null;
            if (sourceId != null) {
                option = new DomainOption(domainName, domainId, sourceId, description, username);
            } else {
                option = new DomainOption(domainName, domainId, description, username);
            }

            if (protoObject.hasRecommendation()) {
                option.setDomainOptionRecommendation(convertRecommendation(protoObject.getRecommendation()));
            }

            if (CollectionUtils.isNotEmpty(protoObject.getPermissionsList())) {
                option.setDomainOptionPermissions(convertList(protoObject.getPermissionsList()));
            }

            if (protoObject.hasConcepts()) {
                UnmarshalledFile uFile = AbstractSchemaHandler.getFromXMLString(protoObject.getConcepts().getValue(),
                        generated.course.Concepts.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE, true);
                generated.course.Concepts concepts = (Concepts) uFile.getUnmarshalled();
                option.setConcepts(concepts);
            }

            return option;

        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

    }

    @Override
    public DomainOptionProto.DomainOption map(DomainOption commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainOptionProto.DomainOption.Builder builder = DomainOptionProto.DomainOption.newBuilder();
        
        Optional.ofNullable(mapRecommendation(commonObject.getDomainOptionRecommendation()))
                .ifPresent(builder::setRecommendation);
        Optional.ofNullable(mapList(commonObject.getDomainOptionPermissions())).ifPresent(builder::addAllPermissions);
        Optional.ofNullable(commonObject.getUsername()).ifPresent(username -> {
            builder.setUserName(StringValue.of(username));
        });
        Optional.ofNullable(commonObject.getDomainName()).ifPresent(name -> {
            builder.setDomainName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getDomainId()).ifPresent(domainId -> {
            builder.setDomainId(StringValue.of(domainId));
        });
        Optional.ofNullable(commonObject.getSourceId()).ifPresent(sourceId -> {
            builder.setSourceId(StringValue.of(sourceId));
        });
        Optional.ofNullable(commonObject.getDescription()).ifPresent(description -> {
            builder.setDescription(StringValue.of(description));
        });

        if (commonObject.getConcepts() != null) {
            try {
                builder.setConcepts(StringValue.of(AbstractSchemaHandler.getAsXMLString(commonObject.getConcepts(),
                        generated.course.Concepts.class, AbstractSchemaHandler.COURSE_SCHEMA_FILE)));
            } catch (Exception e) {
                throw new RuntimeException("There was a problem encoding the Domain Option object", e);
            }
        }

        return builder.build();
    }

}
