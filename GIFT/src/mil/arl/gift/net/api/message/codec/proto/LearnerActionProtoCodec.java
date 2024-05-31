/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.Serializable;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.dkf.AutoTutorSKO;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerAction.StrategyReference;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.TutorMeParams;
import generated.proto.common.LearnerActionProto;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a LearnerAction message.
 * 
 * @author cpolynice
 *
 */
public class LearnerActionProtoCodec implements ProtoCodec<LearnerActionProto.LearnerAction, LearnerAction> {

    @Override
    public LearnerAction convert(LearnerActionProto.LearnerAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = new LearnerAction();

        LearnerActionEnumType type = protoObject.hasType()
                ? LearnerActionEnumType.fromValue(protoObject.getType().getValue())
                : null;

        if (type == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The learner action type is null");
        }

        action.setType(type);

        if (protoObject.hasDisplayName()) {
            action.setDisplayName(protoObject.getDisplayName().getValue());
        }

        if (protoObject.hasDescription()) {
            action.setDescription(protoObject.getDescription().getValue());
        }

        if (protoObject.hasTutorMeParams()) {
            LearnerActionProto.TutorMeParams paramsProto = protoObject.getTutorMeParams();

            if (paramsProto.hasConversationFile()) {
                TutorMeParams params = new TutorMeParams();

                if (paramsProto.hasConversationFile()) {
                    params.setConfiguration(paramsProto.getConversationFile().getValue());
                } else if (paramsProto.hasRemoteSko()) {
                    AutoTutorSKO sko = new AutoTutorSKO();
                    generated.dkf.ATRemoteSKO remoteSKO = new generated.dkf.ATRemoteSKO();
                    generated.dkf.ATRemoteSKO.URL remoteSKOURL = new generated.dkf.ATRemoteSKO.URL();
                    remoteSKOURL.setAddress(paramsProto.getRemoteSko().getValue());
                    remoteSKO.setURL(remoteSKOURL);
                    sko.setScript(remoteSKO);
                    params.setConfiguration(sko);
                } else if (paramsProto.hasLocalSko()) {
                    AutoTutorSKO sko = new AutoTutorSKO();
                    generated.dkf.LocalSKO localSKO = new generated.dkf.LocalSKO();
                    localSKO.setFile(paramsProto.getLocalSko().getValue());
                    sko.setScript(localSKO);
                    params.setConfiguration(sko);
                }

                action.setLearnerActionParams(params);
            }
        } else if (protoObject.hasStrategyReference()) {
            String strategyName = protoObject.getStrategyReference().getStrategyName().getValue();
            StrategyReference strategyReference = new StrategyReference();
            strategyReference.setName(strategyName);

            action.setLearnerActionParams(strategyReference);
        }

        return action;
    }

    @Override
    public LearnerActionProto.LearnerAction map(LearnerAction commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        LearnerActionProto.LearnerAction.Builder builder = LearnerActionProto.LearnerAction.newBuilder();
        
        Optional.ofNullable(commonObject.getDisplayName()).ifPresent(name -> {
            builder.setDisplayName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getType()).ifPresent(type -> {
            builder.setType(StringValue.of(type.value()));
        });

        
        if (commonObject.getDescription() != null){
            builder.setDescription(StringValue.of(commonObject.getDescription()));
        }
        
        if (commonObject.getLearnerActionParams() != null) {
            Serializable actionParams = commonObject.getLearnerActionParams();
            
            if (actionParams instanceof TutorMeParams) {
                
                LearnerActionProto.TutorMeParams.Builder paramsBuilder = LearnerActionProto.TutorMeParams.newBuilder();
                TutorMeParams params = (TutorMeParams) actionParams;
                
                if (params.getConfiguration() instanceof String) {
                    paramsBuilder.setConversationFile(StringValue.of((String) params.getConfiguration()));
                } else if(params.getConfiguration() instanceof AutoTutorSKO){
                    
                    AutoTutorSKO sko = (AutoTutorSKO) params.getConfiguration();
                    Serializable script = sko.getScript();
                    if (script instanceof generated.dkf.ATRemoteSKO) {
                        paramsBuilder.setRemoteSko(StringValue.of(((generated.dkf.ATRemoteSKO) script).getURL().getAddress()));
                    } else if(script instanceof generated.dkf.LocalSKO) {
                        paramsBuilder.setRemoteSko(StringValue.of(((generated.dkf.LocalSKO)script).getFile()));
                    } else{
                        throw new MessageEncodeException(this.getClass().getName(), "Found unhandled AutoTutor SKO referenced object of " + script + ".");
                    }
                }
                
                builder.setTutorMeParams(paramsBuilder);

            } else if (actionParams instanceof StrategyReference) {
                StrategyReference strategyReference = (StrategyReference) actionParams;
                LearnerActionProto.StrategyReference.Builder stratBuilder = LearnerActionProto.StrategyReference
                        .newBuilder();
                stratBuilder.setStrategyName(StringValue.of(strategyReference.getName()));
                builder.setStrategyReference(stratBuilder);
            }
        }
            
        return builder.build();
    }

}
