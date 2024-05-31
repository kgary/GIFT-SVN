/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.QuestionResponseElementMetadataProto;
import mil.arl.gift.common.survey.QuestionResponseElementMetadata;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a QuestionResponseElementMetadata 
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class QuestionResponseElementMetadataProtoCodec implements
        ProtoCodec<QuestionResponseElementMetadataProto.QuestionResponseElementMetadata, QuestionResponseElementMetadata> {

    @Override
    public QuestionResponseElementMetadata convert(
            QuestionResponseElementMetadataProto.QuestionResponseElementMetadata protoObject) {
        if (protoObject == null) {
            return null;
        }

        QuestionResponseElementMetadata responseElementMetadata = new QuestionResponseElementMetadata();

        if (protoObject.hasQuestionResponseId()) {
            responseElementMetadata.setQuestionResponseId(protoObject.getQuestionResponseId().getValue());
        }
        responseElementMetadata.setText(protoObject.hasText() ? protoObject.getText().getValue() : null);
        responseElementMetadata
                .setRowText(protoObject.hasRowText() ? protoObject.getRowText().getValue() : null);
        responseElementMetadata.setColumnIndex(protoObject.hasColumnIndex() ? protoObject.getColumnIndex().getValue() : null);
        responseElementMetadata.setRowIndex(protoObject.hasRowIndex() ? protoObject.getRowIndex().getValue() : null);

        return responseElementMetadata;
    }

    @Override
    public QuestionResponseElementMetadataProto.QuestionResponseElementMetadata map(
            QuestionResponseElementMetadata commonObject) {
        if (commonObject == null) {
            return null;
        }

        QuestionResponseElementMetadataProto.QuestionResponseElementMetadata.Builder builder = QuestionResponseElementMetadataProto.QuestionResponseElementMetadata
                .newBuilder();
        Optional.ofNullable(commonObject.getQuestionResponseId()).ifPresent(responseId -> {
            builder.setQuestionResponseId(Int32Value.of(responseId));
        });
        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });
        Optional.ofNullable(commonObject.getRowText()).ifPresent(rowText -> {
            builder.setRowText(StringValue.of(rowText));
        });
        Optional.ofNullable(commonObject.getColumnIndex()).ifPresent(columnIndex -> {
            builder.setColumnIndex(Int32Value.of(columnIndex));
        });
        Optional.ofNullable(commonObject.getRowIndex()).ifPresent(rowIndex -> {
            builder.setRowIndex(Int32Value.of(rowIndex));
        });
        
        return builder.build();
    }
}
