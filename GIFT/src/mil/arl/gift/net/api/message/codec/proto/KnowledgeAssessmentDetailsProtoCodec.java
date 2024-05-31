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

import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails;
import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails.KnowledgeSessionVariable;
import mil.arl.gift.net.proto.ProtoCodec;
import generated.proto.common.KnowledgeAssessmentDetailsProto;

/**
 * This class is responsible for converting to/from an
 * KnowledgeAssessmentDetails message.
 * 
 * @author nroberts
 */
public class KnowledgeAssessmentDetailsProtoCodec
        implements ProtoCodec<KnowledgeAssessmentDetailsProto.KnowledgeAssessmentDetails, KnowledgeAssessmentDetails> {

    @Override
    public KnowledgeAssessmentDetails convert(KnowledgeAssessmentDetailsProto.KnowledgeAssessmentDetails protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        List<KnowledgeSessionVariable> variables = new ArrayList<>();

        if(protoObject.getVariableList() != null) {
            for(KnowledgeAssessmentDetailsProto.KnowledgeSessionVariable variable : protoObject.getVariableList()) {
                String name = variable.hasName() ? variable.getName().getValue() : null;
                String value = variable.hasValue() ? variable.getValue().getValue() : null;
                String units = variable.hasUnits() ? variable.getUnits().getValue() : null;
                String actor = variable.hasActor() ? variable.getActor().getValue() : null;
                variables.add(new KnowledgeSessionVariable(name, value, units, actor));
            }
        }
        
        return new KnowledgeAssessmentDetails(variables);
    }

    @Override
    public KnowledgeAssessmentDetailsProto.KnowledgeAssessmentDetails map(KnowledgeAssessmentDetails commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        KnowledgeAssessmentDetailsProto.KnowledgeAssessmentDetails.Builder builder = KnowledgeAssessmentDetailsProto.KnowledgeAssessmentDetails
                .newBuilder();
        
        List<KnowledgeAssessmentDetailsProto.KnowledgeSessionVariable> variablesList = new ArrayList<>();
        commonObject.getVariables().forEach(variable -> {
            
            KnowledgeAssessmentDetailsProto.KnowledgeSessionVariable.Builder varBuilder = KnowledgeAssessmentDetailsProto.KnowledgeSessionVariable
                    .newBuilder();
            
            Optional.ofNullable(variable.getName()).ifPresent(name -> {
                varBuilder.setName(StringValue.of(name));
            });
            
            Optional.ofNullable(variable.getValue()).ifPresent(value -> {
                varBuilder.setValue(StringValue.of(value));
            });
            
            Optional.ofNullable(variable.getUnits()).ifPresent(units -> {
                varBuilder.setUnits(StringValue.of(units));
            });
            
            Optional.ofNullable(variable.getActor()).ifPresent(actor -> {
                varBuilder.setActor(StringValue.of(actor));
            });
            
            variablesList.add(varBuilder.build());
        });
        
        builder.addAllVariable(variablesList);

        return builder.build();
    }
}
