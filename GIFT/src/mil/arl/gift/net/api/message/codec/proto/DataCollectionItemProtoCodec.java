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

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.DataCollectionItemProto;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a DataCollectionItem
 * instance.
 * 
 * @author cpolynice
 *
 */
public class DataCollectionItemProtoCodec
        implements ProtoCodec<DataCollectionItemProto.DataCollectionItem, DataCollectionItem> {

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
    public DataCollectionItem convert(DataCollectionItemProto.DataCollectionItem protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            String id = protoObject.hasId() ? protoObject.getId().getValue() : null;
            String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
            String courseFolder = protoObject.hasCourseFolder() ? protoObject.getCourseFolder().getValue() : null;
            String authorUsername = protoObject.hasAuthorUsername() ? protoObject.getAuthorUsername().getValue() : null;
            String url = protoObject.hasUrl() ? protoObject.getUrl().getValue() : null;
            String description = protoObject.hasDescription() ? protoObject.getDescription().getValue() : null;
            String statusString = protoObject.hasStatus() ? protoObject.getStatus().getValue() : null;
            ExperimentStatus status = null;
            DataSetType dataSetType = null;
            Date latestSubjectAttemptDate = null;
            Date latestLtiResultAttemptDate = null;
            Date publishedDate = null;

            if (statusString != null) {
                status = ExperimentStatus.valueOf(statusString);
            }

            /* Optional to allow backwards compatibility. */
            if (protoObject.hasDatasetType()) {
                dataSetType = DataSetType.valueOf(protoObject.getDatasetType().getValue());
            }

            /* This can be null for backwards compatibility. */
            String sourceCourseId = protoObject.hasSourseCourseId() ? protoObject.getSourseCourseId().getValue() : null;
            int subjectSize = protoObject.hasSubjectSize() ? protoObject.getSubjectSize().getValue() : 0;
            int ltiResultSize = protoObject.hasLtiResultsSize() ? protoObject.getLtiResultsSize().getValue() : 0;

            if (protoObject.hasSubjectLastAttempt()) {
                latestSubjectAttemptDate = parseDate(protoObject.getSubjectLastAttempt().getValue());
            }

            if (protoObject.hasLtiSubjectLastAttempt()) {
                latestLtiResultAttemptDate = parseDate(protoObject.getLtiSubjectLastAttempt().getValue());
            }
            
            if(protoObject.hasPublishedDate()) {
                publishedDate = parseDate(protoObject.getPublishedDate().getValue());
            }

            return new DataCollectionItem(id, authorUsername, name, description, url, courseFolder, status, subjectSize,
                    latestSubjectAttemptDate, ltiResultSize, latestLtiResultAttemptDate, dataSetType, sourceCourseId, publishedDate);

        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding experiment subject data", e);
        }
    }

    @Override
    public DataCollectionItemProto.DataCollectionItem map(DataCollectionItem commonObject) {
        if (commonObject == null) {
            return null;
        }

        DataCollectionItemProto.DataCollectionItem.Builder builder = DataCollectionItemProto.DataCollectionItem
                .newBuilder();

        builder.setLtiResultsSize(Int32Value.of(Long.valueOf(commonObject.getLtiResultSize()).intValue()));
        builder.setSubjectSize(Int32Value.of(Long.valueOf(commonObject.getSubjectSize()).intValue()));
        Optional.ofNullable(commonObject.getId()).ifPresent(id -> {
            builder.setId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getDescription()).ifPresent(description -> {
            builder.setDescription(StringValue.of(description));
        });
        Optional.ofNullable(commonObject.getAuthorUsername()).ifPresent(username -> {
            builder.setAuthorUsername(StringValue.of(username));
        });
        Optional.ofNullable(commonObject.getCourseFolder()).ifPresent(folder -> {
            builder.setCourseFolder(StringValue.of(folder));
        });
        Optional.ofNullable(commonObject.getUrl()).ifPresent(url -> {
            builder.setUrl(StringValue.of(url));
        });
        Optional.ofNullable(commonObject.getStatus()).ifPresent(status -> {
            builder.setStatus(StringValue.of(status.name()));
        });
        Optional.ofNullable(commonObject.getDataSetType()).ifPresent(type -> {
            builder.setDatasetType(StringValue.of(type.name()));
        });
        Optional.ofNullable(commonObject.getSourceCourseId()).ifPresent(courseId -> {
            builder.setSourseCourseId(StringValue.of(courseId));
        });
        Optional.ofNullable(commonObject.getSubjectLastAttemptedDate()).ifPresent(lastAttempt -> {
            builder.setSubjectLastAttempt(StringValue.of(getTimeStampAsString(lastAttempt)));
        });
        
        Optional.ofNullable(commonObject.getLtiResultLastAttemptedDate()).ifPresent(ltiAttempt -> {
            builder.setLtiSubjectLastAttempt(StringValue.of(getTimeStampAsString(ltiAttempt)));
        });
        Optional.ofNullable(commonObject.getPublishedDate()).ifPresent(publishedDate -> {
            builder.setPublishedDate(StringValue.of(getTimeStampAsString(publishedDate)));
        });
        
        return builder.build();
    }

}
