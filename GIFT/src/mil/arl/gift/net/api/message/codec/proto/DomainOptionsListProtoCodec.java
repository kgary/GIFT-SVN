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

import generated.proto.common.DomainOptionProto;
import generated.proto.common.DomainOptionsListProto;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOptionsList;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DOmainOptionsList.
 * 
 * @author cpolynice
 *
 */
public class DomainOptionsListProtoCodec
        implements ProtoCodec<DomainOptionsListProto.DomainOptionsList, DomainOptionsList> {

    /* Codec that will be used to convert to/from a DomainOption. */
    private static DomainOptionProtoCodec codec = new DomainOptionProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<DomainOption> convertList(List<DomainOptionProto.DomainOption> protoList) {
        if (protoList == null) {
            return null;
        }

        List<DomainOption> commonList = new ArrayList<>();

        for (DomainOptionProto.DomainOption option : protoList) {
            commonList.add(codec.convert(option));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list.
     */
    private static List<DomainOptionProto.DomainOption> mapList(List<DomainOption> commonList) {
        if (commonList == null) {
            return null;
        }

        List<DomainOptionProto.DomainOption> protoList = new ArrayList<>();

        for (DomainOption option : commonList) {
            protoList.add(codec.map(option));
        }

        return protoList;
    }

    @Override
    public DomainOptionsList convert(DomainOptionsListProto.DomainOptionsList protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<DomainOption> list = null;

        if (CollectionUtils.isNotEmpty(protoObject.getDomainOptionsList())) {
            list = convertList(protoObject.getDomainOptionsList());
        }

        if (list == null) {
            list = new ArrayList<DomainOption>();
            throw new MessageDecodeException(this.getClass().getName(), "The domain options list is null");
        }

        return new DomainOptionsList(list);
    }

    @Override
    public DomainOptionsListProto.DomainOptionsList map(DomainOptionsList commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainOptionsListProto.DomainOptionsList.Builder builder = DomainOptionsListProto.DomainOptionsList
                .newBuilder();

        Optional.ofNullable(mapList(commonObject.getOptions())).ifPresent(builder::addAllDomainOptions);
        return builder.build();
    }
}
