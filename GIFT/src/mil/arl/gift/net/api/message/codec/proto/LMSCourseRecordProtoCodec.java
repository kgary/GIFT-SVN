/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.LMSCourseRecordProto;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LMSCourseRecord.
 * 
 * @author cpolynice
 *
 */
public class LMSCourseRecordProtoCodec implements ProtoCodec<LMSCourseRecordProto.LMSCourseRecord, LMSCourseRecord> {

    /* Date format for retrieving the date as a String. */
    private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a Z");

    /* Date format for parsing the date given, in legacy format. */
    private static final DateFormat legacyDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    /* Codec that will be used to convert to/from a GradedScoreNode. */
    private static GradedScoreNodeProtoCodec gradedCodec = new GradedScoreNodeProtoCodec();

    /* Codec that will be used to convert to/from a LMSConnectionInfo. */
    private static LMSConnectionInfoProtoCodec lmsCodec = new LMSConnectionInfoProtoCodec();
    
    /* codec that will be used to convert to/from CourseRecordRef */
    private static CourseRecordRefProtoCodec courseRecordRefCodec = new CourseRecordRefProtoCodec();

    private String getDateAsString(Date date) {
        return dateFormat.format(date);
    }

    private Date parseCourseDate(String dateStr) throws ParseException {

        Date date;
        dateFormat.setLenient(false);
        legacyDateFormat.setLenient(false);

        try {
            date = dateFormat.parse(dateStr);
        } catch (@SuppressWarnings("unused") ParseException e) {
            date = legacyDateFormat.parse(dateStr);
        }

        return date;
    }

    @Override
    public LMSCourseRecord convert(LMSCourseRecordProto.LMSCourseRecord protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            CourseRecordRef courseRecordRef = courseRecordRefCodec.convert(protoObject.getCourseRecordRef());
            GradedScoreNode root = protoObject.hasRoot() ? gradedCodec.convert(protoObject.getRoot()) : null;

            /* Optional (for now) + legacy messaging parsing support when this
             * value didn't exist (pre v5.0). */
            String domain = protoObject.hasDomainName() ? protoObject.getDomainName().getValue() : null;
            String strDate = protoObject.hasDate() ? protoObject.getDate().getValue() : null;
            Date date = parseCourseDate(strDate);

            LMSCourseRecord record = new LMSCourseRecord(courseRecordRef, domain, root, date);

            /* Optional */
            if (protoObject.hasConnection()) {
                record.setLMSConnectionInfo(lmsCodec.convert(protoObject.getConnection()));
            }

            return record;

        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }
    }

    @Override
    public LMSCourseRecordProto.LMSCourseRecord map(LMSCourseRecord commonObject) {
        if (commonObject == null) {
            return null;
        }

        LMSCourseRecordProto.LMSCourseRecord.Builder builder = LMSCourseRecordProto.LMSCourseRecord.newBuilder();

        Optional.ofNullable(courseRecordRefCodec.map(commonObject.getCourseRecordRef())).ifPresent(builder::setCourseRecordRef);
        Optional.ofNullable(gradedCodec.map(commonObject.getRoot())).ifPresent(builder::setRoot);
        Optional.ofNullable(lmsCodec.map(commonObject.getLMSConnectionInfo())).ifPresent(builder::setConnection);
        Optional.ofNullable(commonObject.getDomainName()).ifPresent(name -> {
            builder.setDomainName(StringValue.of(name));
        });
        Optional.ofNullable(getDateAsString(commonObject.getDate())).ifPresent(date -> {
            builder.setDate(StringValue.of(date));
        });

        return builder.build();
    }

}
