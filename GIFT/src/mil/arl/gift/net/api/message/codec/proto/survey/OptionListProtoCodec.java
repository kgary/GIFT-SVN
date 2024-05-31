/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.ListOptionProto;
import generated.proto.common.survey.OptionListProto;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Option List
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class OptionListProtoCodec implements ProtoCodec<OptionListProto.OptionList, OptionList> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(OptionListProto.OptionList.class);

    /** Codec used to convert between protobuf List Option class. */
    private static ListOptionProtoCodec codec = new ListOptionProtoCodec();

    @Override
    public OptionList convert(OptionListProto.OptionList protoObject) {
        if (protoObject == null) {
            return null;
        }

        int id;
        boolean isShared;
        String name;
        List<String> visibleToUserNames = new ArrayList<>();
        List<String> editableToUserNames = new ArrayList<>();
        List<ListOption> listOptions = new ArrayList<>();

        try {

            id = protoObject.hasId() ? protoObject.getId().getValue() : 0;
            isShared = protoObject.hasIsShared() ? protoObject.getIsShared().getValue() : false;
            name = protoObject.hasName() ? protoObject.getName().getValue() : null;

            if (CollectionUtils.isNotEmpty(protoObject.getVisibleToUserNamesList())) {
                visibleToUserNames.addAll(new ArrayList<>(protoObject.getVisibleToUserNamesList()));
            } else {
                visibleToUserNames.add(Constants.VISIBILITY_WILDCARD);
            }

            if (CollectionUtils.isNotEmpty(protoObject.getEditableToUserNamesList())) {
                editableToUserNames.addAll(new ArrayList<>(protoObject.getEditableToUserNamesList()));
            } else {
                editableToUserNames.add(Constants.EDITABLE_WILDCARD);
            }

            if (CollectionUtils.isNotEmpty(protoObject.getListOptionsList())) {
                for (ListOptionProto.ListOption options : protoObject.getListOptionsList()) {
                    listOptions.add(codec.convert(options));
                }
            }

        } catch (Exception e) {
            logger.error("Caught exception while creating a option list from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        return new OptionList(id, name, isShared, listOptions, visibleToUserNames, editableToUserNames);
    }

    @Override
    public OptionListProto.OptionList map(OptionList commonObject) {
        if (commonObject == null) {
            return null;
        }

        OptionListProto.OptionList.Builder builder = OptionListProto.OptionList.newBuilder();

        builder.setId(Int32Value.of(commonObject.getId()));
        
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });
        
        Optional.ofNullable(commonObject.getIsShared()).ifPresent(isShared -> {
            builder.setIsShared(BoolValue.of(isShared));
        });
        
        Optional.ofNullable(commonObject.getVisibleToUserNames()).ifPresent(builder::addAllVisibleToUserNames);
        Optional.ofNullable(commonObject.getEditableToUserNames()).ifPresent(builder::addAllEditableToUserNames);

        if (commonObject.getListOptions() != null) {
            for (ListOption listOptions : commonObject.getListOptions()) {
                Optional.ofNullable(codec.map(listOptions)).ifPresent(builder::addListOptions);
            }
        }

        return builder.build();
    }
}
