
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * references learning resources.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lrngRsrcId",
    "lrngRsrcUuid",
    "lrngRsrcTitle",
    "lrngRsrcNumber",
    "lrngRsrcAuthor",
    "publishDate",
    "fileType",
    "referencePublisher",
    "refChapPagePara"
})
public class LrngRsrc {

    /**
     * optional: learning resources unique id
     * (Required)
     * 
     */
    @JsonProperty("lrngRsrcId")
    @JsonPropertyDescription("optional: learning resources unique id")
    private Object lrngRsrcId;
    @JsonProperty("lrngRsrcUuid")
    private String lrngRsrcUuid;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lrngRsrcTitle")
    private String lrngRsrcTitle;
    @JsonProperty("lrngRsrcNumber")
    private String lrngRsrcNumber;
    /**
     * a person responsible for creating the tsp or xevent
     * 
     */
    @JsonProperty("lrngRsrcAuthor")
    @JsonPropertyDescription("a person responsible for creating the tsp or xevent")
    private Author lrngRsrcAuthor;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("publishDate")
    private String publishDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    private LrngRsrc.FileType fileType;
    @JsonProperty("referencePublisher")
    private String referencePublisher;
    @JsonProperty("refChapPagePara")
    private String refChapPagePara;

    /**
     * optional: learning resources unique id
     * (Required)
     * 
     */
    @JsonProperty("lrngRsrcId")
    public Object getLrngRsrcId() {
        return lrngRsrcId;
    }

    /**
     * optional: learning resources unique id
     * (Required)
     * 
     */
    @JsonProperty("lrngRsrcId")
    public void setLrngRsrcId(Object lrngRsrcId) {
        this.lrngRsrcId = lrngRsrcId;
    }

    @JsonProperty("lrngRsrcUuid")
    public String getLrngRsrcUuid() {
        return lrngRsrcUuid;
    }

    @JsonProperty("lrngRsrcUuid")
    public void setLrngRsrcUuid(String lrngRsrcUuid) {
        this.lrngRsrcUuid = lrngRsrcUuid;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lrngRsrcTitle")
    public String getLrngRsrcTitle() {
        return lrngRsrcTitle;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lrngRsrcTitle")
    public void setLrngRsrcTitle(String lrngRsrcTitle) {
        this.lrngRsrcTitle = lrngRsrcTitle;
    }

    @JsonProperty("lrngRsrcNumber")
    public String getLrngRsrcNumber() {
        return lrngRsrcNumber;
    }

    @JsonProperty("lrngRsrcNumber")
    public void setLrngRsrcNumber(String lrngRsrcNumber) {
        this.lrngRsrcNumber = lrngRsrcNumber;
    }

    /**
     * a person responsible for creating the tsp or xevent
     * 
     */
    @JsonProperty("lrngRsrcAuthor")
    public Author getLrngRsrcAuthor() {
        return lrngRsrcAuthor;
    }

    /**
     * a person responsible for creating the tsp or xevent
     * 
     */
    @JsonProperty("lrngRsrcAuthor")
    public void setLrngRsrcAuthor(Author lrngRsrcAuthor) {
        this.lrngRsrcAuthor = lrngRsrcAuthor;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("publishDate")
    public String getPublishDate() {
        return publishDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("publishDate")
    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public LrngRsrc.FileType getFileType() {
        return fileType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public void setFileType(LrngRsrc.FileType fileType) {
        this.fileType = fileType;
    }

    @JsonProperty("referencePublisher")
    public String getReferencePublisher() {
        return referencePublisher;
    }

    @JsonProperty("referencePublisher")
    public void setReferencePublisher(String referencePublisher) {
        this.referencePublisher = referencePublisher;
    }

    @JsonProperty("refChapPagePara")
    public String getRefChapPagePara() {
        return refChapPagePara;
    }

    @JsonProperty("refChapPagePara")
    public void setRefChapPagePara(String refChapPagePara) {
        this.refChapPagePara = refChapPagePara;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(LrngRsrc.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("lrngRsrcId");
        sb.append('=');
        sb.append(((this.lrngRsrcId == null)?"<null>":this.lrngRsrcId));
        sb.append(',');
        sb.append("lrngRsrcUuid");
        sb.append('=');
        sb.append(((this.lrngRsrcUuid == null)?"<null>":this.lrngRsrcUuid));
        sb.append(',');
        sb.append("lrngRsrcTitle");
        sb.append('=');
        sb.append(((this.lrngRsrcTitle == null)?"<null>":this.lrngRsrcTitle));
        sb.append(',');
        sb.append("lrngRsrcNumber");
        sb.append('=');
        sb.append(((this.lrngRsrcNumber == null)?"<null>":this.lrngRsrcNumber));
        sb.append(',');
        sb.append("lrngRsrcAuthor");
        sb.append('=');
        sb.append(((this.lrngRsrcAuthor == null)?"<null>":this.lrngRsrcAuthor));
        sb.append(',');
        sb.append("publishDate");
        sb.append('=');
        sb.append(((this.publishDate == null)?"<null>":this.publishDate));
        sb.append(',');
        sb.append("fileType");
        sb.append('=');
        sb.append(((this.fileType == null)?"<null>":this.fileType));
        sb.append(',');
        sb.append("referencePublisher");
        sb.append('=');
        sb.append(((this.referencePublisher == null)?"<null>":this.referencePublisher));
        sb.append(',');
        sb.append("refChapPagePara");
        sb.append('=');
        sb.append(((this.refChapPagePara == null)?"<null>":this.refChapPagePara));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.lrngRsrcId == null)? 0 :this.lrngRsrcId.hashCode()));
        result = ((result* 31)+((this.referencePublisher == null)? 0 :this.referencePublisher.hashCode()));
        result = ((result* 31)+((this.publishDate == null)? 0 :this.publishDate.hashCode()));
        result = ((result* 31)+((this.lrngRsrcNumber == null)? 0 :this.lrngRsrcNumber.hashCode()));
        result = ((result* 31)+((this.lrngRsrcTitle == null)? 0 :this.lrngRsrcTitle.hashCode()));
        result = ((result* 31)+((this.lrngRsrcAuthor == null)? 0 :this.lrngRsrcAuthor.hashCode()));
        result = ((result* 31)+((this.lrngRsrcUuid == null)? 0 :this.lrngRsrcUuid.hashCode()));
        result = ((result* 31)+((this.fileType == null)? 0 :this.fileType.hashCode()));
        result = ((result* 31)+((this.refChapPagePara == null)? 0 :this.refChapPagePara.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LrngRsrc) == false) {
            return false;
        }
        LrngRsrc rhs = ((LrngRsrc) other);
        return ((((((((((this.lrngRsrcId == rhs.lrngRsrcId)||((this.lrngRsrcId!= null)&&this.lrngRsrcId.equals(rhs.lrngRsrcId)))&&((this.referencePublisher == rhs.referencePublisher)||((this.referencePublisher!= null)&&this.referencePublisher.equals(rhs.referencePublisher))))&&((this.publishDate == rhs.publishDate)||((this.publishDate!= null)&&this.publishDate.equals(rhs.publishDate))))&&((this.lrngRsrcNumber == rhs.lrngRsrcNumber)||((this.lrngRsrcNumber!= null)&&this.lrngRsrcNumber.equals(rhs.lrngRsrcNumber))))&&((this.lrngRsrcTitle == rhs.lrngRsrcTitle)||((this.lrngRsrcTitle!= null)&&this.lrngRsrcTitle.equals(rhs.lrngRsrcTitle))))&&((this.lrngRsrcAuthor == rhs.lrngRsrcAuthor)||((this.lrngRsrcAuthor!= null)&&this.lrngRsrcAuthor.equals(rhs.lrngRsrcAuthor))))&&((this.lrngRsrcUuid == rhs.lrngRsrcUuid)||((this.lrngRsrcUuid!= null)&&this.lrngRsrcUuid.equals(rhs.lrngRsrcUuid))))&&((this.fileType == rhs.fileType)||((this.fileType!= null)&&this.fileType.equals(rhs.fileType))))&&((this.refChapPagePara == rhs.refChapPagePara)||((this.refChapPagePara!= null)&&this.refChapPagePara.equals(rhs.refChapPagePara))));
    }

    public enum FileType {

        HTML(".html"),
        PDF(".pdf"),
        TXT(".txt"),
        DOC(".doc"),
        XML(".xml"),
        JSON(".json");
        private final String value;
        private final static Map<String, LrngRsrc.FileType> CONSTANTS = new HashMap<String, LrngRsrc.FileType>();

        static {
            for (LrngRsrc.FileType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FileType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static LrngRsrc.FileType fromValue(String value) {
            LrngRsrc.FileType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
