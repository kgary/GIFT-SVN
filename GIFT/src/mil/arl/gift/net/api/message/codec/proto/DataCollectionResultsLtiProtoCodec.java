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

import generated.proto.common.DataCollectionResultsLtiProto;
import mil.arl.gift.common.experiment.DataCollectionResultsLti;
import mil.arl.gift.common.lti.LtiUserId;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a DataCollectionResultsLti
 * instance.
 * 
 * @author cpolynice
 *
 */
public class DataCollectionResultsLtiProtoCodec
        implements ProtoCodec<DataCollectionResultsLtiProto.DataCollectionResultsLti, DataCollectionResultsLti> {

    /* Codec that will be used to convert to/from a LtiUserId object. */
    private static LtiUserIdProtoCodec codec = new LtiUserIdProtoCodec();

    /* Format provided for the date. */
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* Format provided for the string time stamp. */
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
    // NOTE: SimpleDateFormat isn't thread-safe, received
    // ArrayIndexOutOfBoundsException exceptions under heavy work load, this
    // method has to be synchronized
    // TODO: should SimpleDateFormatter be instance variable, if so would need
    // one in AbstractMessageData as well for data classes to access
    public static synchronized Date parseDate(String dateStr) throws ParseException {
        return df.parse(dateStr);
    }

    /**
     * Returns the time stamp as a string provided by the format.
     * 
     * @param timeStamp - the timestamp to format
     * @return String - the formatted timestamp.
     */
    public static String getTimeStampAsString(Date timeStamp) {
        return fdf.format(timeStamp);
    }

    @Override
    public DataCollectionResultsLti convert(DataCollectionResultsLtiProto.DataCollectionResultsLti protoObject) {
        if (protoObject == null) {
            return null;
        }

        LtiUserId id = protoObject.hasId() ? codec.convert(protoObject.getId()) : null;
        String dataSetId = protoObject.hasDataSetId() ? protoObject.getDataSetId().getValue() : null;
        String messageLogFile = protoObject.hasMessageLogFile() ? protoObject.getMessageLogFile().getValue() : null;
        Date startTime = null;
        Date endTime = null;
        try {
            startTime = protoObject.hasStartTime() ? parseDate(protoObject.getStartTime().getValue()) : null;
            endTime = protoObject.hasEndTime() ? parseDate(protoObject.getEndTime().getValue()) : null;
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding experiment subject data", e);
        }
        
        return new DataCollectionResultsLti(id, dataSetId, messageLogFile, startTime,
                endTime);
    }

    @Override
    public DataCollectionResultsLtiProto.DataCollectionResultsLti map(DataCollectionResultsLti commonObject) {
        if (commonObject == null) {
            return null;
        }

        DataCollectionResultsLtiProto.DataCollectionResultsLti.Builder builder = DataCollectionResultsLtiProto.DataCollectionResultsLti
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getLtiUserId())).ifPresent(builder::setId);
        Optional.ofNullable(commonObject.getDataSetId()).ifPresent(dataSetId -> {
            builder.setDataSetId(StringValue.of(dataSetId));
        });
        Optional.ofNullable(commonObject.getStartTime()).ifPresent(startTime -> {
            builder.setStartTime(StringValue.of(getTimeStampAsString(startTime)));
        });
        Optional.ofNullable(commonObject.getEndTime()).ifPresent(endTime -> {
            builder.setEndTime(StringValue.of(getTimeStampAsString(endTime)));
        });
        Optional.ofNullable(commonObject.getMessageLogFilename()).ifPresent(file -> {
            builder.setMessageLogFile(StringValue.of(file));
        });

        return builder.build();
    }

}
