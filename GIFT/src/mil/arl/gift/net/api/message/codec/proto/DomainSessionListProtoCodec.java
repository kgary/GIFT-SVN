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

import generated.proto.common.DomainSessionListProto;
import generated.proto.common.DomainSessionProto;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.DomainSessionList;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainSessionList.
 * 
 * @author cpolynice
 *
 */
public class DomainSessionListProtoCodec
        implements ProtoCodec<DomainSessionListProto.DomainSessionList, DomainSessionList> {

    /* Codec that will be used to convert to/from a DomainSession. */
    private static DomainSessionProtoCodec codec = new DomainSessionProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<DomainSession> convertList(List<DomainSessionProto.DomainSession> protoList) {
        if (protoList == null) {
            return null;
        }

        List<DomainSession> commonList = new ArrayList<>();

        for (DomainSessionProto.DomainSession session : protoList) {
            commonList.add(codec.convert(session));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list.
     */
    private static List<DomainSessionProto.DomainSession> mapList(List<DomainSession> commonList) {
        if (commonList == null) {
            return null;
        }

        List<DomainSessionProto.DomainSession> protoList = new ArrayList<>();

        for (DomainSession session : commonList) {
            protoList.add(codec.map(session));
        }

        return protoList;
    }

    @Override
    public DomainSessionList convert(DomainSessionListProto.DomainSessionList protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<DomainSession> list = CollectionUtils.isNotEmpty(protoObject.getDomainSessionsList())
                ? convertList(protoObject.getDomainSessionsList())
                : new ArrayList<>();

        /* Check for required inputs */
        if (list == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The domain sessions list is null");
        }

        return new DomainSessionList(list);
    }

    @Override
    public DomainSessionListProto.DomainSessionList map(DomainSessionList commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainSessionListProto.DomainSessionList.Builder builder = DomainSessionListProto.DomainSessionList
                .newBuilder();

        Optional.ofNullable(mapList(commonObject.getDomainSessions())).ifPresent(builder::addAllDomainSessions);
        return builder.build();
    }
}
