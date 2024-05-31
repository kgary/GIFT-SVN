/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.ListOptionProto;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a List Option 
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class ListOptionProtoCodec implements ProtoCodec<ListOptionProto.ListOption, ListOption> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(ListOptionProto.ListOption.class);

    @Override
    public ListOption convert(ListOptionProto.ListOption protoObject) {

        if (protoObject == null) {
            return null;
        }

        int id, optionListId;
        String text;
        
        try {
            id = protoObject.hasId() ? protoObject.getId().getValue() : 0;
            optionListId = protoObject.hasOptionListId() ? protoObject.getOptionListId().getValue() : 0;
            text = protoObject.hasText() ? protoObject.getText().getValue() : null;

            // Check for required inputs
            if (text == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The option text is null");
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating a list option from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
        
        
        return new ListOption(id, text, optionListId);
    }

    @Override
    public ListOptionProto.ListOption map(ListOption commonObject) {
        if (commonObject == null) {
            return null;
        }

        ListOptionProto.ListOption.Builder builder = ListOptionProto.ListOption.newBuilder();
        
        builder.setId(Int32Value.of(commonObject.getId()));
        builder.setOptionListId(Int32Value.of(commonObject.getOptionListId()));
        
        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });

        return builder.build();
    }
}
