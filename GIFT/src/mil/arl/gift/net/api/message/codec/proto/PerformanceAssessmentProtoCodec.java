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

import generated.proto.common.AbstractAssessmentProto;
import generated.proto.common.PerformanceAssessmentProto;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * PerformanceAssessment.
 * 
 * @author cpolynice
 *
 */
public class PerformanceAssessmentProtoCodec
        implements ProtoCodec<PerformanceAssessmentProto.PerformanceAssessment, PerformanceAssessment> {

    /* Codec that will be used to convert to/from a protobuf TaskAssessment. */
    private static final TaskAssessmentProtoCodec codec = new TaskAssessmentProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<TaskAssessment> convertTasks(List<AbstractAssessmentProto.TaskAssessment> protoList) {
        if (protoList == null) {
            return null;
        }

        List<TaskAssessment> commonList = new ArrayList<>();

        for (AbstractAssessmentProto.TaskAssessment task : protoList) {
            commonList.add(codec.convert(task));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<AbstractAssessmentProto.TaskAssessment> mapTasks(List<TaskAssessment> commonList) {
        if (commonList == null) {
            return null;
        }

        List<AbstractAssessmentProto.TaskAssessment> protoList = new ArrayList<>();

        for (TaskAssessment task : commonList) {
            protoList.add(codec.map(task));
        }

        return protoList;
    }

    @Override
    public PerformanceAssessment convert(PerformanceAssessmentProto.PerformanceAssessment protoObject) {
        if (protoObject == null) {
            return null;
        }

        String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
        String observerComment = protoObject.hasObserverComment() ? protoObject.getObserverComment().getValue() : null;
        String observerMedia = protoObject.hasObserverMedia() ? protoObject.getObserverMedia().getValue() : null;

        List<TaskAssessment> tasks = CollectionUtils.isNotEmpty(protoObject.getTasksList())
                ? convertTasks(protoObject.getTasksList())
                : new ArrayList<>();

        PerformanceAssessment newPerformanceAssessment = new PerformanceAssessment(tasks);
        newPerformanceAssessment.setEvaluator(evaluator);
        newPerformanceAssessment.setObserverComment(observerComment);
        newPerformanceAssessment.setObserverMedia(observerMedia);

        return newPerformanceAssessment;
    }

    @Override
    public PerformanceAssessmentProto.PerformanceAssessment map(PerformanceAssessment commonObject) {
        if (commonObject == null) {
            return null;
        }

        PerformanceAssessmentProto.PerformanceAssessment.Builder builder = PerformanceAssessmentProto.PerformanceAssessment
                .newBuilder();

        if (CollectionUtils.isNotEmpty(commonObject.getTasks())) {
            builder.addAllTasks(mapTasks(new ArrayList<>(commonObject.getTasks())));
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
