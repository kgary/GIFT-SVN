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
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.BranchPathHistoryProto;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a BranchPathHistory message.
 * 
 * @author cpolynice
 *
 */
public class BranchPathHistoryProtoCodec
        implements ProtoCodec<BranchPathHistoryProto.BranchPathHistory, BranchPathHistory> {

    @Override
    public BranchPathHistory convert(BranchPathHistoryProto.BranchPathHistory protoObject) {
        if (protoObject == null) {
            return null;
        }

        String courseId = protoObject.hasCourseId() ? protoObject.getCourseId().getValue() : null;
        String experimentId = protoObject.hasExperimentId() ? protoObject.getExperimentId().getValue() : null;
        int branchId = protoObject.hasBranchId() ? protoObject.getBranchId().getValue() : 0;
        int pathId = protoObject.hasPathId() ? protoObject.getPathId().getValue() : 0;
        int actualCnt = protoObject.hasActualCnt() ? protoObject.getActualCnt().getValue() : 0;
        int cnt = protoObject.hasCnt() ? protoObject.getCnt().getValue() : 0;

        BranchPathHistory branchPathHistory = null;
        if (protoObject.hasPathname()) {
            String pathName = protoObject.getPathname().getValue();
            boolean pathEnd = protoObject.getPathend().getValue();
            branchPathHistory = new BranchPathHistory(courseId, experimentId, branchId, pathId, actualCnt, cnt,
                    pathName, pathEnd);
        } else {
            branchPathHistory = new BranchPathHistory(courseId, experimentId, branchId, pathId, actualCnt, cnt);
        }

        Boolean increment = protoObject.hasIncrement() ? protoObject.getIncrement().getValue() : false;
        branchPathHistory.setIncrement(increment);

        return branchPathHistory;
    }

    @Override
    public BranchPathHistoryProto.BranchPathHistory map(BranchPathHistory commonObject) {
        if (commonObject == null) {
            return null;
        }

        BranchPathHistoryProto.BranchPathHistory.Builder builder = BranchPathHistoryProto.BranchPathHistory
                .newBuilder();

        builder.setBranchId(Int32Value.of(commonObject.getBranchId()));
        builder.setPathId(Int32Value.of(commonObject.getPathId()));
        builder.setActualCnt(Int32Value.of(commonObject.getActualCnt()));
        builder.setCnt(Int32Value.of(commonObject.getCnt()));
        builder.setPathend(BoolValue.of(commonObject.isPathEnding()));
        builder.setIncrement(BoolValue.of(commonObject.shouldIncrement()));
        Optional.ofNullable(commonObject.getCourseId()).ifPresent(cId -> {
            builder.setCourseId(StringValue.of(cId));
        });
        Optional.ofNullable(commonObject.getExperimentId()).ifPresent(eId -> {
            builder.setExperimentId(StringValue.of(eId));
        });
        Optional.ofNullable(commonObject.getPathName()).ifPresent(pName -> {
            builder.setPathname(StringValue.of(pName));
        });

        return builder.build();
    }
}
