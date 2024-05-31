
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
 * references used as authoritative sources for exercise components... can document down to page and paragraph.
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "referenceId",
    "referenceUuid",
    "referenceTitle",
    "referenceNumber",
    "referenceAuthor",
    "publishDate",
    "fileType",
    "referencePublisher",
    "refChapPagePara"
})
public class Reference {

    /**
     * optional: referece unique id
     * (Required)
     * 
     */
    @JsonProperty("referenceId")
    @JsonPropertyDescription("optional: referece unique id")
    private Object referenceId;
    @JsonProperty("referenceUuid")
    private String referenceUuid;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("referenceTitle")
    private String referenceTitle;
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    /**
     * a person responsible for creating the tsp or xevent
     * 
     */
    @JsonProperty("referenceAuthor")
    @JsonPropertyDescription("a person responsible for creating the tsp or xevent")
    private Author referenceAuthor;
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
    private Reference.FileType fileType;
    @JsonProperty("referencePublisher")
    private String referencePublisher;
    @JsonProperty("refChapPagePara")
    private String refChapPagePara;

    /**
     * optional: referece unique id
     * (Required)
     * 
     */
    @JsonProperty("referenceId")
    public Object getReferenceId() {
        return referenceId;
    }

    /**
     * optional: referece unique id
     * (Required)
     * 
     */
    @JsonProperty("referenceId")
    public void setReferenceId(Object referenceId) {
        this.referenceId = referenceId;
    }

    @JsonProperty("referenceUuid")
    public String getReferenceUuid() {
        return referenceUuid;
    }

    @JsonProperty("referenceUuid")
    public void setReferenceUuid(String referenceUuid) {
        this.referenceUuid = referenceUuid;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("referenceTitle")
    public String getReferenceTitle() {
        return referenceTitle;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("referenceTitle")
    public void setReferenceTitle(String referenceTitle) {
        this.referenceTitle = referenceTitle;
    }

    @JsonProperty("referenceNumber")
    public String getReferenceNumber() {
        return referenceNumber;
    }

    @JsonProperty("referenceNumber")
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    /**
     * a person responsible for creating the tsp or xevent
     * 
     */
    @JsonProperty("referenceAuthor")
    public Author getReferenceAuthor() {
        return referenceAuthor;
    }

    /**
     * a person responsible for creating the tsp or xevent
     * 
     */
    @JsonProperty("referenceAuthor")
    public void setReferenceAuthor(Author referenceAuthor) {
        this.referenceAuthor = referenceAuthor;
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
    public Reference.FileType getFileType() {
        return fileType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public void setFileType(Reference.FileType fileType) {
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
        sb.append(Reference.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("referenceId");
        sb.append('=');
        sb.append(((this.referenceId == null)?"<null>":this.referenceId));
        sb.append(',');
        sb.append("referenceUuid");
        sb.append('=');
        sb.append(((this.referenceUuid == null)?"<null>":this.referenceUuid));
        sb.append(',');
        sb.append("referenceTitle");
        sb.append('=');
        sb.append(((this.referenceTitle == null)?"<null>":this.referenceTitle));
        sb.append(',');
        sb.append("referenceNumber");
        sb.append('=');
        sb.append(((this.referenceNumber == null)?"<null>":this.referenceNumber));
        sb.append(',');
        sb.append("referenceAuthor");
        sb.append('=');
        sb.append(((this.referenceAuthor == null)?"<null>":this.referenceAuthor));
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
        result = ((result* 31)+((this.referenceAuthor == null)? 0 :this.referenceAuthor.hashCode()));
        result = ((result* 31)+((this.referenceNumber == null)? 0 :this.referenceNumber.hashCode()));
        result = ((result* 31)+((this.referencePublisher == null)? 0 :this.referencePublisher.hashCode()));
        result = ((result* 31)+((this.publishDate == null)? 0 :this.publishDate.hashCode()));
        result = ((result* 31)+((this.referenceUuid == null)? 0 :this.referenceUuid.hashCode()));
        result = ((result* 31)+((this.referenceTitle == null)? 0 :this.referenceTitle.hashCode()));
        result = ((result* 31)+((this.referenceId == null)? 0 :this.referenceId.hashCode()));
        result = ((result* 31)+((this.fileType == null)? 0 :this.fileType.hashCode()));
        result = ((result* 31)+((this.refChapPagePara == null)? 0 :this.refChapPagePara.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Reference) == false) {
            return false;
        }
        Reference rhs = ((Reference) other);
        return ((((((((((this.referenceAuthor == rhs.referenceAuthor)||((this.referenceAuthor!= null)&&this.referenceAuthor.equals(rhs.referenceAuthor)))&&((this.referenceNumber == rhs.referenceNumber)||((this.referenceNumber!= null)&&this.referenceNumber.equals(rhs.referenceNumber))))&&((this.referencePublisher == rhs.referencePublisher)||((this.referencePublisher!= null)&&this.referencePublisher.equals(rhs.referencePublisher))))&&((this.publishDate == rhs.publishDate)||((this.publishDate!= null)&&this.publishDate.equals(rhs.publishDate))))&&((this.referenceUuid == rhs.referenceUuid)||((this.referenceUuid!= null)&&this.referenceUuid.equals(rhs.referenceUuid))))&&((this.referenceTitle == rhs.referenceTitle)||((this.referenceTitle!= null)&&this.referenceTitle.equals(rhs.referenceTitle))))&&((this.referenceId == rhs.referenceId)||((this.referenceId!= null)&&this.referenceId.equals(rhs.referenceId))))&&((this.fileType == rhs.fileType)||((this.fileType!= null)&&this.fileType.equals(rhs.fileType))))&&((this.refChapPagePara == rhs.refChapPagePara)||((this.refChapPagePara!= null)&&this.refChapPagePara.equals(rhs.refChapPagePara))));
    }

    public enum FileType {

        HTML(".html"),
        PDF(".pdf"),
        TXT(".txt"),
        DOC(".doc"),
        XML(".xml"),
        JSON(".json");
        private final String value;
        private final static Map<String, Reference.FileType> CONSTANTS = new HashMap<String, Reference.FileType>();

        static {
            for (Reference.FileType c: values()) {
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
        public static Reference.FileType fromValue(String value) {
            Reference.FileType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
