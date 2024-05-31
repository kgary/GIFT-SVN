/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractPerformanceStateProto;
import generated.proto.common.PerformanceStateProto;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf PerformanceState.
 * 
 * @author cpolynice
 *
 */
public class PerformanceStateProtoCodec
        implements ProtoCodec<PerformanceStateProto.PerformanceState, PerformanceState> {

    /* Codec that will be used to convert to/from a protobuf
     * TaskPerformanceState. */
    private static TaskPerformanceStateProtoCodec codec = new TaskPerformanceStateProtoCodec();

    @Override
    public PerformanceState convert(PerformanceStateProto.PerformanceState protoObject) {
        if (protoObject == null) {
            return null;
        }

        String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
        String observerComment = protoObject.hasObserverComment() ? protoObject.getObserverComment().getValue() : null;
        String observerMedia = protoObject.hasObserverMedia() ? protoObject.getObserverMedia().getValue() : null;

        Map<Integer, TaskPerformanceState> tasks = new HashMap<>();

        if (CollectionUtils.isNotEmpty(protoObject.getTasksMap())) {
            for (Map.Entry<Integer, AbstractPerformanceStateProto.TaskPerformanceState> task : protoObject.getTasksMap()
                    .entrySet()) {
                int key = task.getKey();
                TaskPerformanceState value = codec.convert(task.getValue());

                if (value != null) {
                    tasks.put(key, value);
                }
            }
        }

        PerformanceState newPerformanceState = new PerformanceState(tasks);
        newPerformanceState.setEvaluator(evaluator);
        newPerformanceState.setObserverComment(observerComment);
        newPerformanceState.setObserverMedia(observerMedia);

        return newPerformanceState;
    }

    @Override
    public PerformanceStateProto.PerformanceState map(PerformanceState commonObject) {
        if (commonObject == null) {
            return null;
        }

        PerformanceStateProto.PerformanceState.Builder builder = PerformanceStateProto.PerformanceState.newBuilder();

        if (CollectionUtils.isNotEmpty(commonObject.getTasks())) {
            for (Map.Entry<Integer, TaskPerformanceState> task : commonObject.getTasks().entrySet()) {
                int key = task.getKey();
                AbstractPerformanceStateProto.TaskPerformanceState value = codec.map(task.getValue());

                if (value != null) {
                    builder.putTasks(key, value);
                }
            }
        } else {
            builder.putAllTasks(new HashMap<>());
        }

        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
            builder.setEvaluator(StringValue.of(evaluator));
        });

        Optional.ofNullable(commonObject.getObserverComment()).ifPresent(oc -> {
            builder.setObserverComment(StringValue.of(oc));
        });

        Optional.ofNullable(commonObject.getObserverMedia()).ifPresent(oMedia -> {
            builder.setObserverMedia(StringValue.of(oMedia));
        });

        return builder.build();
    }

}
