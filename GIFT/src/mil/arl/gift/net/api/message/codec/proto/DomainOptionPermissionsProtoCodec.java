/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.DomainOptionPermissionsProto;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainOptionPermissions instance.
 * 
 * @author cpolynice
 *
 */
public class DomainOptionPermissionsProtoCodec
        implements ProtoCodec<DomainOptionPermissionsProto.DomainOptionPermissions, DomainOptionPermissions> {

    /* Codec that will be used to convert to/from an Abstract Enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public DomainOptionPermissions convert(DomainOptionPermissionsProto.DomainOptionPermissions protoObject) {
        if (protoObject == null) {
            return null;
        }

        String username = protoObject.hasPermissionUser() ? protoObject.getPermissionUser().getValue() : null;
        SharedCoursePermissionsEnum permission = protoObject.hasPermissionType()
                ? (SharedCoursePermissionsEnum) enumCodec.convert(protoObject.getPermissionType())
                : null;
        boolean isOwner = protoObject.hasPermissionIsOwner() ? protoObject.getPermissionIsOwner().getValue() : false;

        return new DomainOptionPermissions(username, permission, isOwner);
    }

    @Override
    public DomainOptionPermissionsProto.DomainOptionPermissions map(DomainOptionPermissions commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainOptionPermissionsProto.DomainOptionPermissions.Builder builder = DomainOptionPermissionsProto.DomainOptionPermissions
                .newBuilder();

        builder.setPermissionIsOwner(BoolValue.of(commonObject.isOwner()));
        Optional.ofNullable(enumCodec.map(commonObject.getPermission())).ifPresent(builder::setPermissionType);
        Optional.ofNullable(commonObject.getUser()).ifPresent(user -> {
            builder.setPermissionUser(StringValue.of(user));
        });

        return builder.build();
    }
}
