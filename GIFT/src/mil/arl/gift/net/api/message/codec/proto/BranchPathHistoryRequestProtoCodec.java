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

import generated.proto.common.BranchPathHistoryProto;
import generated.proto.common.BranchPathHistoryRequestProto.BranchPathHistoryRequest;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a BranchPathHistoryRequest
 * message.
 * 
 * @author cpolynice
 *
 */
public class BranchPathHistoryRequestProtoCodec
        implements ProtoCodec<BranchPathHistoryRequest, List<BranchPathHistory>> {

    /* Codec that will be used to convert to/from BranchPathHistory messages. */
    private static BranchPathHistoryProtoCodec codec = new BranchPathHistoryProtoCodec();

    @Override
    public List<BranchPathHistory> convert(BranchPathHistoryRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (CollectionUtils.isNotEmpty(protoObject.getPathsList())) {
            List<BranchPathHistory> pathHistories = new ArrayList<>();

            for (BranchPathHistoryProto.BranchPathHistory path : protoObject.getPathsList()) {
                pathHistories.add(codec.convert(path));
            }

            return pathHistories;
        } else {
            return null;
        }
    }

    @Override
    public BranchPathHistoryRequest map(List<BranchPathHistory> commonObject) {
        if (CollectionUtils.isEmpty(commonObject)) {
            return null;
        }

        BranchPathHistoryRequest.Builder builder = BranchPathHistoryRequest.newBuilder();

        for (BranchPathHistory path : commonObject) {
            builder.addPaths(codec.map(path));
        }

        return builder.build();
    }
}
