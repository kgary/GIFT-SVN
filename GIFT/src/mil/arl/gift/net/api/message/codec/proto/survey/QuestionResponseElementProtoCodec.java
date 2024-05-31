/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.QuestionResponseElementProto;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a QuestionResponseElement 
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class QuestionResponseElementProtoCodec
        implements ProtoCodec<QuestionResponseElementProto.QuestionResponseElement, QuestionResponseElement> {

    /** Logger instance for the class. */
    private static Logger logger = LoggerFactory.getLogger(QuestionResponseElementProto.QuestionResponseElement.class);

    /** Codecs that the class will use to convert to/from protobuf messages. */
    private static ListOptionProtoCodec listOptionCodec = new ListOptionProtoCodec();
    private static OptionListProtoCodec optionListCodec = new OptionListProtoCodec();

    @Override
    public QuestionResponseElement convert(QuestionResponseElementProto.QuestionResponseElement protoObject) {
        if (protoObject == null) {
            return null;
        }

        int responseId;
        String text;
        OptionList textOptionList = null;
        String rowText = null;
        OptionList rowTextOptionList = null;
        Date answerTime = null;
        ListOption choiceListOption = null, rowChoiceListOption = null;
        Integer colChoiceListOptionIndex = null, rowChoiceListOptionIndex = null;
        QuestionResponseElement questionResponseElement;

        try {
            responseId = protoObject.hasResponseId() ? protoObject.getResponseId().getValue() : 0;

            text = protoObject.hasText() ? protoObject.getText().getValue() : null;

            if (text == null) {
                // LEGACY late 2018 - could be null if not answered, now null is
                // not supported
                text = Constants.EMPTY;
            }

            if (protoObject.hasRowText()) {
                rowText = protoObject.getRowText().getValue();
            }

            if (protoObject.hasTextOptionList()) {
                textOptionList = optionListCodec.convert(protoObject.getTextOptionList());
            }

            if (protoObject.hasRowTextOptionList()) {
                rowTextOptionList = optionListCodec.convert(protoObject.getRowTextOptionList());
            }

            // LEGACY late 2018 - could be null if not answered, now null is
            // not supported
            answerTime = protoObject.hasAnswerTime() ? new Date(ProtobufConversionUtil.convertTimestampToMillis(protoObject.getAnswerTime())) : new Date();

            if (protoObject.hasChoiceListOption()) {
                choiceListOption = listOptionCodec.convert(protoObject.getChoiceListOption());
            }

            if (protoObject.hasRowChoiceListOption()) {
                rowChoiceListOption = listOptionCodec.convert(protoObject.getRowChoiceListOption());
            }

            if (protoObject.hasColumnChoiceListOptionIndex()) {
                colChoiceListOptionIndex = protoObject.getColumnChoiceListOptionIndex().getValue();
            } else {
                /* LEGACY mid 2019 - could be null if answered before changes
                 * made for #4131 */
                if (textOptionList != null && textOptionList.getListOptions() != null && choiceListOption != null) {
                    for (ListOption option : textOptionList.getListOptions()) {
                        if (option != null && Objects.equals(option.getText(), choiceListOption.getText())) {

                            /* if the list of options contains an option whose
                             * text matches the choice, use the index of that
                             * option as the index of the choice */
                            colChoiceListOptionIndex = textOptionList.getListOptions().indexOf(option);
                            break;
                        }
                    }
                }
            }

            if (protoObject.hasRowChoiceListOptionIndex()) {
                rowChoiceListOptionIndex = protoObject.getRowChoiceListOptionIndex().getValue();
            } else {
                /* LEGACY mid 2019 - could be null if answered before changes
                 * made for #4131 */
                if (rowTextOptionList != null && rowTextOptionList.getListOptions() != null
                        && rowChoiceListOption != null) {
                    for (ListOption option : rowTextOptionList.getListOptions()) {
                        if (option != null && Objects.equals(option.getText(), rowChoiceListOption.getText())) {

                            /* if the list of row options contains an option
                             * whose text matches the row text, use the index of
                             * that option as the index of the row choice */
                            rowChoiceListOptionIndex = rowTextOptionList.getListOptions().indexOf(option);
                            break;
                        }
                    }
                }
            }

            if (choiceListOption != null) {
                // either a multiple choice, matrix of choices or rating scale
                // question type

                if (rowChoiceListOption != null) {
                    // a matrix of choices question type
                    questionResponseElement = new QuestionResponseElement(colChoiceListOptionIndex, choiceListOption,
                            textOptionList, rowChoiceListOptionIndex, rowChoiceListOption, rowTextOptionList,
                            answerTime);
                } else {
                    // multiple choice or rating scale question type
                    questionResponseElement = new QuestionResponseElement(colChoiceListOptionIndex, choiceListOption,
                            textOptionList, answerTime);
                }
            } else if (textOptionList != null) {
                // a legacy gift message
                // ALSO reading from the survey database
                questionResponseElement = new QuestionResponseElement(text, textOptionList, rowText, rowTextOptionList,
                        answerTime);

            } else {
                // other question types that don't have choices for the survey
                // taker (e.g. free response)
                questionResponseElement = new QuestionResponseElement(text, answerTime);
            }

            if (responseId > 0) {
                questionResponseElement.setQuestionResponseId(responseId);
            }

            return questionResponseElement;

        } catch (Exception e) {
            logger.error("Caught exception while creating question response element data from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }


    }

    @Override
    public QuestionResponseElementProto.QuestionResponseElement map(QuestionResponseElement commonObject) {
        if (commonObject == null) {
            return null;
        }

        QuestionResponseElementProto.QuestionResponseElement.Builder builder = QuestionResponseElementProto.QuestionResponseElement
                .newBuilder();

        builder.setResponseId(Int32Value.of(commonObject.getQuestionResponseId()));
        
        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });
        
        Optional.ofNullable(commonObject.getColumnIndex()).ifPresent(index -> {
            builder.setColumnChoiceListOptionIndex(Int32Value.of(index));
        });
        Optional.ofNullable(commonObject.getRowIndex()).ifPresent(index -> {
            builder.setRowChoiceListOptionIndex(Int32Value.of(index));
        });

        Optional.ofNullable(listOptionCodec.map(commonObject.getChoice())).ifPresent(builder::setChoiceListOption);
        Optional.ofNullable(optionListCodec.map(commonObject.getChoices())).ifPresent(builder::setTextOptionList);
        Optional.ofNullable(listOptionCodec.map(commonObject.getRowChoice()))
                .ifPresent(builder::setRowChoiceListOption);
        Optional.ofNullable(optionListCodec.map(commonObject.getRowChoices())).ifPresent(builder::setRowTextOptionList);
        Optional.ofNullable(ProtobufConversionUtil.convertDateToTimestamp(commonObject.getAnswerTime())).ifPresent(builder::setAnswerTime);

        if (commonObject.getRowChoice() != null && StringUtils.isNotBlank(commonObject.getRowChoice().getText())) {
            builder.setRowText(StringValue.of(commonObject.getRowChoice().getText()));
        }

        return builder.build();
    }
}
