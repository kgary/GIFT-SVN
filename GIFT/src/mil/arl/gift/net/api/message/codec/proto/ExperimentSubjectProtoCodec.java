/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

import com.google.protobuf.StringValue;

import generated.proto.common.ExperimentSubjectProto;
import mil.arl.gift.common.experiment.ExperimentSubject;
import mil.arl.gift.common.experiment.ExperimentSubjectId;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf ExperimentSubject
 * instance.
 * 
 * @author cpolynice
 *
 */
public class ExperimentSubjectProtoCodec
        implements ProtoCodec<ExperimentSubjectProto.ExperimentSubject, ExperimentSubject> {

    /* Codec that will be used to convert to/from an ExperimentSubjectId
     * instance. */
    private static ExperimentSubjectIdProtoCodec codec = new ExperimentSubjectIdProtoCodec();

    /* The format that will be used when instantiating a new Date. */
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* The format that will be used in returning the timestamp as a String. */
    private static FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(),
            Locale.getDefault());

    /**
     * Parse the date/time string using date format and return a new Date
     * instance.
     *
     * @param dateStr - the date/time string to parse
     * @return Date - a date instance created from the string
     * @throws ParseException if the beginning of the specified string cannot be
     *         parsed
     */
    /* NOTE: SimpleDateFormat isn't thread-safe, received
     * ArrayIndexOutOfBoundsException exceptions under heavy work load, this
     * method has to be synchronized TODO: should SimpleDateFormatter be
     * instance variable, if so would need one in AbstractMessageData as well
     * for data classes to access */
    public static synchronized Date parseDate(String dateStr) throws ParseException {
        return df.parse(dateStr);
    }

    /**
     * Returns the given timestamp as the string formatted using the
     * FastDateFormat.
     * 
     * @param timeStamp the date to format
     * @return the formatted timestamp as a String.
     */
    public static String getTimeStampAsString(Date timeStamp) {
        return fdf.format(timeStamp);
    }

    @Override
    public ExperimentSubject convert(ExperimentSubjectProto.ExperimentSubject protoObject) {
        if (protoObject == null) {
            return null;
        }

        ExperimentSubjectId id;
        Date startTime;
        Date endTime;
        String messageLogFile;

        try {
            id = protoObject.hasId() ? codec.convert(protoObject.getId()) : null;
            startTime = protoObject.hasStartTime() ? parseDate(protoObject.getStartTime().getValue()) : null;
            endTime = protoObject.hasEndTime() ? parseDate(protoObject.getEndTime().getValue()) : null;
            messageLogFile = protoObject.hasMessageLogFile() ? protoObject.getMessageLogFile().getValue() : null;
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding experiment subject data", e);
        }

        return new ExperimentSubject(id, startTime, endTime, messageLogFile);
    }

    @Override
    public ExperimentSubjectProto.ExperimentSubject map(ExperimentSubject commonObject) {
        if (commonObject == null) {
            return null;
        }

        ExperimentSubjectProto.ExperimentSubject.Builder builder = ExperimentSubjectProto.ExperimentSubject
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getExperimentSubjectId())).ifPresent(builder::setId);
        Optional.ofNullable(getTimeStampAsString(commonObject.getStartTime())).ifPresent(startTime -> {
            builder.setStartTime(StringValue.of(startTime));
        });
        Optional.ofNullable(getTimeStampAsString(commonObject.getEndTime())).ifPresent(endTime -> {
            builder.setEndTime(StringValue.of(endTime));
        });
        Optional.ofNullable(commonObject.getMessageLogFilename()).ifPresent(filename -> {
            builder.setMessageLogFile(StringValue.of(filename));
        });

        return builder.build();
    }

}
