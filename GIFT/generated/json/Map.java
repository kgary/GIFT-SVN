
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * describes the real-world physical location a synthetic map is representing - used to filter and translate real-world sensor data to synthetic environment e.g., SiVT
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mapId",
    "mapUuid",
    "mapName",
    "region",
    "country",
    "facility",
    "dwelling",
    "areaOfInterest",
    "coordSystemDatum",
    "utmGridZone",
    "utmGridSquare"
})
public class Map {

    /**
     * required: unique global id for a given real space
     * (Required)
     * 
     */
    @JsonProperty("mapId")
    @JsonPropertyDescription("required: unique global id for a given real space")
    private Object mapId;
    @JsonProperty("mapUuid")
    private String mapUuid;
    @JsonProperty("mapName")
    private String mapName;
    /**
     * required: name of a general region / continent / ocean / sea an area is within
     * (Required)
     * 
     */
    @JsonProperty("region")
    @JsonPropertyDescription("required: name of a general region / continent / ocean / sea an area is within")
    private String region;
    /**
     * required: name of a sovereign country / territory an area is within
     * (Required)
     * 
     */
    @JsonProperty("country")
    @JsonPropertyDescription("required: name of a sovereign country / territory an area is within")
    private String country;
    /**
     * optional: name of a facility area is within
     * 
     */
    @JsonProperty("facility")
    @JsonPropertyDescription("optional: name of a facility area is within")
    private String facility;
    /**
     * optional: name of a building an area is within
     * 
     */
    @JsonProperty("dwelling")
    @JsonPropertyDescription("optional: name of a building an area is within")
    private String dwelling;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("areaOfInterest")
    private Object areaOfInterest;
    /**
     * a single geodectic coordinate ref point gcc coordinates are calibrated to
     * (Required)
     * 
     */
    @JsonProperty("coordSystemDatum")
    @JsonPropertyDescription("a single geodectic coordinate ref point gcc coordinates are calibrated to")
    private Object coordSystemDatum;
    /**
     * required: a grid of rectangles around the earth, with a two capital letter designations.  First letter is representing an 8-12 degree latitude band, with degrees increasing with relative distance from equator.  Second letter represents 1 of 60, 6-degree longitudinal bands around the earth.
     * 
     */
    @JsonProperty("utmGridZone")
    @JsonPropertyDescription("required: a grid of rectangles around the earth, with a two capital letter designations.  First letter is representing an 8-12 degree latitude band, with degrees increasing with relative distance from equator.  Second letter represents 1 of 60, 6-degree longitudinal bands around the earth.")
    private String utmGridZone;
    /**
     * optional: a 100km grid Square are within each grid zone, given a two capital letter designations.  First letter is representing the latitude square, the second letter represents the longitudinal square.
     * 
     */
    @JsonProperty("utmGridSquare")
    @JsonPropertyDescription("optional: a 100km grid Square are within each grid zone, given a two capital letter designations.  First letter is representing the latitude square, the second letter represents the longitudinal square.")
    private String utmGridSquare;

    /**
     * required: unique global id for a given real space
     * (Required)
     * 
     */
    @JsonProperty("mapId")
    public Object getMapId() {
        return mapId;
    }

    /**
     * required: unique global id for a given real space
     * (Required)
     * 
     */
    @JsonProperty("mapId")
    public void setMapId(Object mapId) {
        this.mapId = mapId;
    }

    @JsonProperty("mapUuid")
    public String getMapUuid() {
        return mapUuid;
    }

    @JsonProperty("mapUuid")
    public void setMapUuid(String mapUuid) {
        this.mapUuid = mapUuid;
    }

    @JsonProperty("mapName")
    public String getMapName() {
        return mapName;
    }

    @JsonProperty("mapName")
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    /**
     * required: name of a general region / continent / ocean / sea an area is within
     * (Required)
     * 
     */
    @JsonProperty("region")
    public String getRegion() {
        return region;
    }

    /**
     * required: name of a general region / continent / ocean / sea an area is within
     * (Required)
     * 
     */
    @JsonProperty("region")
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * required: name of a sovereign country / territory an area is within
     * (Required)
     * 
     */
    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    /**
     * required: name of a sovereign country / territory an area is within
     * (Required)
     * 
     */
    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * optional: name of a facility area is within
     * 
     */
    @JsonProperty("facility")
    public String getFacility() {
        return facility;
    }

    /**
     * optional: name of a facility area is within
     * 
     */
    @JsonProperty("facility")
    public void setFacility(String facility) {
        this.facility = facility;
    }

    /**
     * optional: name of a building an area is within
     * 
     */
    @JsonProperty("dwelling")
    public String getDwelling() {
        return dwelling;
    }

    /**
     * optional: name of a building an area is within
     * 
     */
    @JsonProperty("dwelling")
    public void setDwelling(String dwelling) {
        this.dwelling = dwelling;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("areaOfInterest")
    public Object getAreaOfInterest() {
        return areaOfInterest;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("areaOfInterest")
    public void setAreaOfInterest(Object areaOfInterest) {
        this.areaOfInterest = areaOfInterest;
    }

    /**
     * a single geodectic coordinate ref point gcc coordinates are calibrated to
     * (Required)
     * 
     */
    @JsonProperty("coordSystemDatum")
    public Object getCoordSystemDatum() {
        return coordSystemDatum;
    }

    /**
     * a single geodectic coordinate ref point gcc coordinates are calibrated to
     * (Required)
     * 
     */
    @JsonProperty("coordSystemDatum")
    public void setCoordSystemDatum(Object coordSystemDatum) {
        this.coordSystemDatum = coordSystemDatum;
    }

    /**
     * required: a grid of rectangles around the earth, with a two capital letter designations.  First letter is representing an 8-12 degree latitude band, with degrees increasing with relative distance from equator.  Second letter represents 1 of 60, 6-degree longitudinal bands around the earth.
     * 
     */
    @JsonProperty("utmGridZone")
    public String getUtmGridZone() {
        return utmGridZone;
    }

    /**
     * required: a grid of rectangles around the earth, with a two capital letter designations.  First letter is representing an 8-12 degree latitude band, with degrees increasing with relative distance from equator.  Second letter represents 1 of 60, 6-degree longitudinal bands around the earth.
     * 
     */
    @JsonProperty("utmGridZone")
    public void setUtmGridZone(String utmGridZone) {
        this.utmGridZone = utmGridZone;
    }

    /**
     * optional: a 100km grid Square are within each grid zone, given a two capital letter designations.  First letter is representing the latitude square, the second letter represents the longitudinal square.
     * 
     */
    @JsonProperty("utmGridSquare")
    public String getUtmGridSquare() {
        return utmGridSquare;
    }

    /**
     * optional: a 100km grid Square are within each grid zone, given a two capital letter designations.  First letter is representing the latitude square, the second letter represents the longitudinal square.
     * 
     */
    @JsonProperty("utmGridSquare")
    public void setUtmGridSquare(String utmGridSquare) {
        this.utmGridSquare = utmGridSquare;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Map.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("mapId");
        sb.append('=');
        sb.append(((this.mapId == null)?"<null>":this.mapId));
        sb.append(',');
        sb.append("mapUuid");
        sb.append('=');
        sb.append(((this.mapUuid == null)?"<null>":this.mapUuid));
        sb.append(',');
        sb.append("mapName");
        sb.append('=');
        sb.append(((this.mapName == null)?"<null>":this.mapName));
        sb.append(',');
        sb.append("region");
        sb.append('=');
        sb.append(((this.region == null)?"<null>":this.region));
        sb.append(',');
        sb.append("country");
        sb.append('=');
        sb.append(((this.country == null)?"<null>":this.country));
        sb.append(',');
        sb.append("facility");
        sb.append('=');
        sb.append(((this.facility == null)?"<null>":this.facility));
        sb.append(',');
        sb.append("dwelling");
        sb.append('=');
        sb.append(((this.dwelling == null)?"<null>":this.dwelling));
        sb.append(',');
        sb.append("areaOfInterest");
        sb.append('=');
        sb.append(((this.areaOfInterest == null)?"<null>":this.areaOfInterest));
        sb.append(',');
        sb.append("coordSystemDatum");
        sb.append('=');
        sb.append(((this.coordSystemDatum == null)?"<null>":this.coordSystemDatum));
        sb.append(',');
        sb.append("utmGridZone");
        sb.append('=');
        sb.append(((this.utmGridZone == null)?"<null>":this.utmGridZone));
        sb.append(',');
        sb.append("utmGridSquare");
        sb.append('=');
        sb.append(((this.utmGridSquare == null)?"<null>":this.utmGridSquare));
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
        result = ((result* 31)+((this.country == null)? 0 :this.country.hashCode()));
        result = ((result* 31)+((this.utmGridZone == null)? 0 :this.utmGridZone.hashCode()));
        result = ((result* 31)+((this.coordSystemDatum == null)? 0 :this.coordSystemDatum.hashCode()));
        result = ((result* 31)+((this.utmGridSquare == null)? 0 :this.utmGridSquare.hashCode()));
        result = ((result* 31)+((this.areaOfInterest == null)? 0 :this.areaOfInterest.hashCode()));
        result = ((result* 31)+((this.mapUuid == null)? 0 :this.mapUuid.hashCode()));
        result = ((result* 31)+((this.dwelling == null)? 0 :this.dwelling.hashCode()));
        result = ((result* 31)+((this.mapId == null)? 0 :this.mapId.hashCode()));
        result = ((result* 31)+((this.mapName == null)? 0 :this.mapName.hashCode()));
        result = ((result* 31)+((this.region == null)? 0 :this.region.hashCode()));
        result = ((result* 31)+((this.facility == null)? 0 :this.facility.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Map) == false) {
            return false;
        }
        Map rhs = ((Map) other);
        return ((((((((((((this.country == rhs.country)||((this.country!= null)&&this.country.equals(rhs.country)))&&((this.utmGridZone == rhs.utmGridZone)||((this.utmGridZone!= null)&&this.utmGridZone.equals(rhs.utmGridZone))))&&((this.coordSystemDatum == rhs.coordSystemDatum)||((this.coordSystemDatum!= null)&&this.coordSystemDatum.equals(rhs.coordSystemDatum))))&&((this.utmGridSquare == rhs.utmGridSquare)||((this.utmGridSquare!= null)&&this.utmGridSquare.equals(rhs.utmGridSquare))))&&((this.areaOfInterest == rhs.areaOfInterest)||((this.areaOfInterest!= null)&&this.areaOfInterest.equals(rhs.areaOfInterest))))&&((this.mapUuid == rhs.mapUuid)||((this.mapUuid!= null)&&this.mapUuid.equals(rhs.mapUuid))))&&((this.dwelling == rhs.dwelling)||((this.dwelling!= null)&&this.dwelling.equals(rhs.dwelling))))&&((this.mapId == rhs.mapId)||((this.mapId!= null)&&this.mapId.equals(rhs.mapId))))&&((this.mapName == rhs.mapName)||((this.mapName!= null)&&this.mapName.equals(rhs.mapName))))&&((this.region == rhs.region)||((this.region!= null)&&this.region.equals(rhs.region))))&&((this.facility == rhs.facility)||((this.facility!= null)&&this.facility.equals(rhs.facility))));
    }

}
