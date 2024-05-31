/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.vecmath.Point3d;

import com.google.protobuf.StringValue;

import generated.proto.common.LoSQueryProto;
import generated.proto.common.Point3DProto.Point3D;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LoSQuery.
 * 
 * @author cpolynice
 *
 */
public class LoSQueryProtoCodec implements ProtoCodec<LoSQueryProto.LoSQuery, LoSQuery> {

    /** value used for request id when one is not in the decoded object */
    private static final String UNKNOWN_ID = "UNKNOWN";

    /* Codec that will be used to convert to/from a protobuf Point3D. */
    private static Point3DProtoCodec codec = new Point3DProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<Point3d> convertLocations(List<Point3D> protoList) {
        if (protoList == null) {
            return null;
        }

        List<Point3d> commonList = new ArrayList<>();

        for (Point3D location : protoList) {
            commonList.add(codec.convert(location));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<Point3D> mapLocations(List<Point3d> commonList) {
        if (commonList == null) {
            return null;
        }

        List<Point3D> protoList = new ArrayList<>();

        for (Point3d location : commonList) {
            protoList.add(codec.map(location));
        }

        return protoList;
    }

    @Override
    public LoSQuery convert(LoSQueryProto.LoSQuery protoObject) {
        if (protoObject == null) {
            return null;
        }

        String requestId = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : UNKNOWN_ID;
        List<Point3d> locations = CollectionUtils.isNotEmpty(protoObject.getLocationsList())
                ? convertLocations(protoObject.getLocationsList())
                : null;
        Set<String> entities = CollectionUtils.isNotEmpty(protoObject.getEntitiesList())
                ? new HashSet<>(protoObject.getEntitiesList())
                : null;

        if (entities != null) {
            return new LoSQuery(locations, requestId, entities);
        } else {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Could not find any of the handled entity references.");
        }
    }

    @Override
    public LoSQueryProto.LoSQuery map(LoSQuery commonObject) {
        if (commonObject == null) {
            return null;
        }

        LoSQueryProto.LoSQuery.Builder builder = LoSQueryProto.LoSQuery.newBuilder();

        Optional.ofNullable(commonObject.getEntities()).ifPresent(builder::addAllEntities);
        Optional.ofNullable(mapLocations(commonObject.getLocations())).ifPresent(builder::addAllLocations);
        Optional.ofNullable(commonObject.getRequestId()).ifPresent(id -> {
            builder.setRequestId(StringValue.of(id));
        });

        return builder.build();
    }

}
