
package generated.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * optional: any/all tactical or exercise control overlays used to inform, prompt players or to help OC/T control exercise execution, prompts or guides.  overlays support one of four functions between TSS and TMT: mission description, exercise execution triggers (TSS features), assessment triggers and/or assessment criteria (TMT features)
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "overlayId",
    "overlayName",
    "overlayUse",
    "symbolCode",
    "shape",
    "anchor",
    "points",
    "radius",
    "width",
    "length",
    "height",
    "showLine",
    "lineColor",
    "lineAlpha",
    "lineStyle",
    "lineSize",
    "showFill",
    "fillColor",
    "fillAlpha",
    "textMessage",
    "textColor",
    "textFont"
})
public class Overlay {

    /**
     * required: exercise unique overlay id
     * (Required)
     * 
     */
    @JsonProperty("overlayId")
    @JsonPropertyDescription("required: exercise unique overlay id")
    private Object overlayId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overlayName")
    private String overlayName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overlayUse")
    private Overlay.OverlayUse overlayUse;
    @JsonProperty("symbolCode")
    private String symbolCode;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shape")
    private Overlay.Shape shape;
    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * 
     */
    @JsonProperty("anchor")
    @JsonPropertyDescription("Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format")
    private List<Object> anchor = new ArrayList<Object>();
    /**
     * used to represent all points in a shape or overlay
     * (Required)
     * 
     */
    @JsonProperty("points")
    @JsonPropertyDescription("used to represent all points in a shape or overlay")
    private List<List<Object>> points = new ArrayList<List<Object>>();
    /**
     * radius in meters used for circles
     * 
     */
    @JsonProperty("radius")
    @JsonPropertyDescription("radius in meters used for circles")
    private Integer radius;
    /**
     * width in meters used for squares or rectangle
     * 
     */
    @JsonProperty("width")
    @JsonPropertyDescription("width in meters used for squares or rectangle")
    private Integer width;
    /**
     * length in meters used for squares or rectangle
     * 
     */
    @JsonProperty("length")
    @JsonPropertyDescription("length in meters used for squares or rectangle")
    private Integer length;
    /**
     * height in meters for 3D components of squares or rectangle
     * 
     */
    @JsonProperty("height")
    @JsonPropertyDescription("height in meters for 3D components of squares or rectangle")
    private Integer height;
    @JsonProperty("showLine")
    private Boolean showLine = true;
    @JsonProperty("lineColor")
    private String lineColor = "#000000";
    @JsonProperty("lineAlpha")
    private String lineAlpha = "FF";
    @JsonProperty("lineStyle")
    private String lineStyle = "solid";
    @JsonProperty("lineSize")
    private String lineSize = "1px";
    @JsonProperty("showFill")
    private Boolean showFill = true;
    @JsonProperty("fillColor")
    private String fillColor = "#000000";
    @JsonProperty("fillAlpha")
    private String fillAlpha = "FF";
    @JsonProperty("textMessage")
    private String textMessage = "";
    @JsonProperty("textColor")
    private String textColor = "#000000";
    @JsonProperty("textFont")
    private String textFont = "NewTimesRoman";

    /**
     * required: exercise unique overlay id
     * (Required)
     * 
     */
    @JsonProperty("overlayId")
    public Object getOverlayId() {
        return overlayId;
    }

    /**
     * required: exercise unique overlay id
     * (Required)
     * 
     */
    @JsonProperty("overlayId")
    public void setOverlayId(Object overlayId) {
        this.overlayId = overlayId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overlayName")
    public String getOverlayName() {
        return overlayName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overlayName")
    public void setOverlayName(String overlayName) {
        this.overlayName = overlayName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overlayUse")
    public Overlay.OverlayUse getOverlayUse() {
        return overlayUse;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("overlayUse")
    public void setOverlayUse(Overlay.OverlayUse overlayUse) {
        this.overlayUse = overlayUse;
    }

    @JsonProperty("symbolCode")
    public String getSymbolCode() {
        return symbolCode;
    }

    @JsonProperty("symbolCode")
    public void setSymbolCode(String symbolCode) {
        this.symbolCode = symbolCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shape")
    public Overlay.Shape getShape() {
        return shape;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("shape")
    public void setShape(Overlay.Shape shape) {
        this.shape = shape;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * 
     */
    @JsonProperty("anchor")
    public List<Object> getAnchor() {
        return anchor;
    }

    /**
     * Geodectic coordinate used to support the placement of physical entities or overlays in a real-world physical place using a decimal-degrees format
     * 
     */
    @JsonProperty("anchor")
    public void setAnchor(List<Object> anchor) {
        this.anchor = anchor;
    }

    /**
     * used to represent all points in a shape or overlay
     * (Required)
     * 
     */
    @JsonProperty("points")
    public List<List<Object>> getPoints() {
        return points;
    }

    /**
     * used to represent all points in a shape or overlay
     * (Required)
     * 
     */
    @JsonProperty("points")
    public void setPoints(List<List<Object>> points) {
        this.points = points;
    }

    /**
     * radius in meters used for circles
     * 
     */
    @JsonProperty("radius")
    public Integer getRadius() {
        return radius;
    }

    /**
     * radius in meters used for circles
     * 
     */
    @JsonProperty("radius")
    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    /**
     * width in meters used for squares or rectangle
     * 
     */
    @JsonProperty("width")
    public Integer getWidth() {
        return width;
    }

    /**
     * width in meters used for squares or rectangle
     * 
     */
    @JsonProperty("width")
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * length in meters used for squares or rectangle
     * 
     */
    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    /**
     * length in meters used for squares or rectangle
     * 
     */
    @JsonProperty("length")
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * height in meters for 3D components of squares or rectangle
     * 
     */
    @JsonProperty("height")
    public Integer getHeight() {
        return height;
    }

    /**
     * height in meters for 3D components of squares or rectangle
     * 
     */
    @JsonProperty("height")
    public void setHeight(Integer height) {
        this.height = height;
    }

    @JsonProperty("showLine")
    public Boolean getShowLine() {
        return showLine;
    }

    @JsonProperty("showLine")
    public void setShowLine(Boolean showLine) {
        this.showLine = showLine;
    }

    @JsonProperty("lineColor")
    public String getLineColor() {
        return lineColor;
    }

    @JsonProperty("lineColor")
    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    @JsonProperty("lineAlpha")
    public String getLineAlpha() {
        return lineAlpha;
    }

    @JsonProperty("lineAlpha")
    public void setLineAlpha(String lineAlpha) {
        this.lineAlpha = lineAlpha;
    }

    @JsonProperty("lineStyle")
    public String getLineStyle() {
        return lineStyle;
    }

    @JsonProperty("lineStyle")
    public void setLineStyle(String lineStyle) {
        this.lineStyle = lineStyle;
    }

    @JsonProperty("lineSize")
    public String getLineSize() {
        return lineSize;
    }

    @JsonProperty("lineSize")
    public void setLineSize(String lineSize) {
        this.lineSize = lineSize;
    }

    @JsonProperty("showFill")
    public Boolean getShowFill() {
        return showFill;
    }

    @JsonProperty("showFill")
    public void setShowFill(Boolean showFill) {
        this.showFill = showFill;
    }

    @JsonProperty("fillColor")
    public String getFillColor() {
        return fillColor;
    }

    @JsonProperty("fillColor")
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    @JsonProperty("fillAlpha")
    public String getFillAlpha() {
        return fillAlpha;
    }

    @JsonProperty("fillAlpha")
    public void setFillAlpha(String fillAlpha) {
        this.fillAlpha = fillAlpha;
    }

    @JsonProperty("textMessage")
    public String getTextMessage() {
        return textMessage;
    }

    @JsonProperty("textMessage")
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    @JsonProperty("textColor")
    public String getTextColor() {
        return textColor;
    }

    @JsonProperty("textColor")
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    @JsonProperty("textFont")
    public String getTextFont() {
        return textFont;
    }

    @JsonProperty("textFont")
    public void setTextFont(String textFont) {
        this.textFont = textFont;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Overlay.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("overlayId");
        sb.append('=');
        sb.append(((this.overlayId == null)?"<null>":this.overlayId));
        sb.append(',');
        sb.append("overlayName");
        sb.append('=');
        sb.append(((this.overlayName == null)?"<null>":this.overlayName));
        sb.append(',');
        sb.append("overlayUse");
        sb.append('=');
        sb.append(((this.overlayUse == null)?"<null>":this.overlayUse));
        sb.append(',');
        sb.append("symbolCode");
        sb.append('=');
        sb.append(((this.symbolCode == null)?"<null>":this.symbolCode));
        sb.append(',');
        sb.append("shape");
        sb.append('=');
        sb.append(((this.shape == null)?"<null>":this.shape));
        sb.append(',');
        sb.append("anchor");
        sb.append('=');
        sb.append(((this.anchor == null)?"<null>":this.anchor));
        sb.append(',');
        sb.append("points");
        sb.append('=');
        sb.append(((this.points == null)?"<null>":this.points));
        sb.append(',');
        sb.append("radius");
        sb.append('=');
        sb.append(((this.radius == null)?"<null>":this.radius));
        sb.append(',');
        sb.append("width");
        sb.append('=');
        sb.append(((this.width == null)?"<null>":this.width));
        sb.append(',');
        sb.append("length");
        sb.append('=');
        sb.append(((this.length == null)?"<null>":this.length));
        sb.append(',');
        sb.append("height");
        sb.append('=');
        sb.append(((this.height == null)?"<null>":this.height));
        sb.append(',');
        sb.append("showLine");
        sb.append('=');
        sb.append(((this.showLine == null)?"<null>":this.showLine));
        sb.append(',');
        sb.append("lineColor");
        sb.append('=');
        sb.append(((this.lineColor == null)?"<null>":this.lineColor));
        sb.append(',');
        sb.append("lineAlpha");
        sb.append('=');
        sb.append(((this.lineAlpha == null)?"<null>":this.lineAlpha));
        sb.append(',');
        sb.append("lineStyle");
        sb.append('=');
        sb.append(((this.lineStyle == null)?"<null>":this.lineStyle));
        sb.append(',');
        sb.append("lineSize");
        sb.append('=');
        sb.append(((this.lineSize == null)?"<null>":this.lineSize));
        sb.append(',');
        sb.append("showFill");
        sb.append('=');
        sb.append(((this.showFill == null)?"<null>":this.showFill));
        sb.append(',');
        sb.append("fillColor");
        sb.append('=');
        sb.append(((this.fillColor == null)?"<null>":this.fillColor));
        sb.append(',');
        sb.append("fillAlpha");
        sb.append('=');
        sb.append(((this.fillAlpha == null)?"<null>":this.fillAlpha));
        sb.append(',');
        sb.append("textMessage");
        sb.append('=');
        sb.append(((this.textMessage == null)?"<null>":this.textMessage));
        sb.append(',');
        sb.append("textColor");
        sb.append('=');
        sb.append(((this.textColor == null)?"<null>":this.textColor));
        sb.append(',');
        sb.append("textFont");
        sb.append('=');
        sb.append(((this.textFont == null)?"<null>":this.textFont));
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
        result = ((result* 31)+((this.textMessage == null)? 0 :this.textMessage.hashCode()));
        result = ((result* 31)+((this.shape == null)? 0 :this.shape.hashCode()));
        result = ((result* 31)+((this.overlayName == null)? 0 :this.overlayName.hashCode()));
        result = ((result* 31)+((this.showFill == null)? 0 :this.showFill.hashCode()));
        result = ((result* 31)+((this.length == null)? 0 :this.length.hashCode()));
        result = ((result* 31)+((this.fillAlpha == null)? 0 :this.fillAlpha.hashCode()));
        result = ((result* 31)+((this.lineColor == null)? 0 :this.lineColor.hashCode()));
        result = ((result* 31)+((this.overlayUse == null)? 0 :this.overlayUse.hashCode()));
        result = ((result* 31)+((this.lineAlpha == null)? 0 :this.lineAlpha.hashCode()));
        result = ((result* 31)+((this.lineSize == null)? 0 :this.lineSize.hashCode()));
        result = ((result* 31)+((this.textColor == null)? 0 :this.textColor.hashCode()));
        result = ((result* 31)+((this.points == null)? 0 :this.points.hashCode()));
        result = ((result* 31)+((this.symbolCode == null)? 0 :this.symbolCode.hashCode()));
        result = ((result* 31)+((this.fillColor == null)? 0 :this.fillColor.hashCode()));
        result = ((result* 31)+((this.lineStyle == null)? 0 :this.lineStyle.hashCode()));
        result = ((result* 31)+((this.anchor == null)? 0 :this.anchor.hashCode()));
        result = ((result* 31)+((this.width == null)? 0 :this.width.hashCode()));
        result = ((result* 31)+((this.showLine == null)? 0 :this.showLine.hashCode()));
        result = ((result* 31)+((this.textFont == null)? 0 :this.textFont.hashCode()));
        result = ((result* 31)+((this.radius == null)? 0 :this.radius.hashCode()));
        result = ((result* 31)+((this.overlayId == null)? 0 :this.overlayId.hashCode()));
        result = ((result* 31)+((this.height == null)? 0 :this.height.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Overlay) == false) {
            return false;
        }
        Overlay rhs = ((Overlay) other);
        return (((((((((((((((((((((((this.textMessage == rhs.textMessage)||((this.textMessage!= null)&&this.textMessage.equals(rhs.textMessage)))&&((this.shape == rhs.shape)||((this.shape!= null)&&this.shape.equals(rhs.shape))))&&((this.overlayName == rhs.overlayName)||((this.overlayName!= null)&&this.overlayName.equals(rhs.overlayName))))&&((this.showFill == rhs.showFill)||((this.showFill!= null)&&this.showFill.equals(rhs.showFill))))&&((this.length == rhs.length)||((this.length!= null)&&this.length.equals(rhs.length))))&&((this.fillAlpha == rhs.fillAlpha)||((this.fillAlpha!= null)&&this.fillAlpha.equals(rhs.fillAlpha))))&&((this.lineColor == rhs.lineColor)||((this.lineColor!= null)&&this.lineColor.equals(rhs.lineColor))))&&((this.overlayUse == rhs.overlayUse)||((this.overlayUse!= null)&&this.overlayUse.equals(rhs.overlayUse))))&&((this.lineAlpha == rhs.lineAlpha)||((this.lineAlpha!= null)&&this.lineAlpha.equals(rhs.lineAlpha))))&&((this.lineSize == rhs.lineSize)||((this.lineSize!= null)&&this.lineSize.equals(rhs.lineSize))))&&((this.textColor == rhs.textColor)||((this.textColor!= null)&&this.textColor.equals(rhs.textColor))))&&((this.points == rhs.points)||((this.points!= null)&&this.points.equals(rhs.points))))&&((this.symbolCode == rhs.symbolCode)||((this.symbolCode!= null)&&this.symbolCode.equals(rhs.symbolCode))))&&((this.fillColor == rhs.fillColor)||((this.fillColor!= null)&&this.fillColor.equals(rhs.fillColor))))&&((this.lineStyle == rhs.lineStyle)||((this.lineStyle!= null)&&this.lineStyle.equals(rhs.lineStyle))))&&((this.anchor == rhs.anchor)||((this.anchor!= null)&&this.anchor.equals(rhs.anchor))))&&((this.width == rhs.width)||((this.width!= null)&&this.width.equals(rhs.width))))&&((this.showLine == rhs.showLine)||((this.showLine!= null)&&this.showLine.equals(rhs.showLine))))&&((this.textFont == rhs.textFont)||((this.textFont!= null)&&this.textFont.equals(rhs.textFont))))&&((this.radius == rhs.radius)||((this.radius!= null)&&this.radius.equals(rhs.radius))))&&((this.overlayId == rhs.overlayId)||((this.overlayId!= null)&&this.overlayId.equals(rhs.overlayId))))&&((this.height == rhs.height)||((this.height!= null)&&this.height.equals(rhs.height))));
    }

    public enum OverlayUse {

        TACTICAL("tactical"),
        TRIGGER("trigger"),
        ASSESSMENT("assessment"),
        SCRIPT("script"),
        ACTIVITY("activity");
        private final String value;
        private final static Map<String, Overlay.OverlayUse> CONSTANTS = new HashMap<String, Overlay.OverlayUse>();

        static {
            for (Overlay.OverlayUse c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        OverlayUse(String value) {
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
        public static Overlay.OverlayUse fromValue(String value) {
            Overlay.OverlayUse constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum Shape {

        POINT("Point"),
        POLYGON("Polygon"),
        LINE_STRING("LineString"),
        CIRCLE("Circle"),
        MULTI_LINE_STRING("MultiLineString"),
        MULTI_POLYGON("MultiPolygon");
        private final String value;
        private final static Map<String, Overlay.Shape> CONSTANTS = new HashMap<String, Overlay.Shape>();

        static {
            for (Overlay.Shape c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Shape(String value) {
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
        public static Overlay.Shape fromValue(String value) {
            Overlay.Shape constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
