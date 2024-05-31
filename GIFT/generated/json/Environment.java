
package generated.json;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * This is the overall playable map or real space that an exercise will occur in.  Will consist of three layers consisting of a real-world map, an exericse-based map, and an exercise-based parameters
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "exerciseMap",
    "exerciseEnvironment",
    "mapObjects"
})
public class Environment {

    /**
     * describes the real-world physical location a synthetic map is representing - used to filter and translate real-world sensor data to synthetic environment e.g., SiVT
     * (Required)
     * 
     */
    @JsonProperty("exerciseMap")
    @JsonPropertyDescription("describes the real-world physical location a synthetic map is representing - used to filter and translate real-world sensor data to synthetic environment e.g., SiVT")
    private Map exerciseMap;
    /**
     * the real or synthetic environmental parameters for the exercise map, based on the real geographic location, altitude, season and time of day.  Ideally will draw from historical data and randomized air-pressure systems.  These variables can be set dynamically by rendering engine during design but must be based on modeled data from other settings like location, season, sun or moon data, etc.  Can include other non-natural elements such as smoke and CBRN levels.  Can get much of this data from sites like http://www.worldweatheronline.com
     * (Required)
     * 
     */
    @JsonProperty("exerciseEnvironment")
    @JsonPropertyDescription("the real or synthetic environmental parameters for the exercise map, based on the real geographic location, altitude, season and time of day.  Ideally will draw from historical data and randomized air-pressure systems.  These variables can be set dynamically by rendering engine during design but must be based on modeled data from other settings like location, season, sun or moon data, etc.  Can include other non-natural elements such as smoke and CBRN levels.  Can get much of this data from sites like http://www.worldweatheronline.com")
    private Metoc exerciseEnvironment;
    /**
     * These are moving or fixed objects that will be positioned on the map to supplement xEvents or add realism.  Includes structures like trenches, forts, wrecks, walls, roadblocks and animals or insects, etc....  Also devices like c-wire, IEDs, boxes, fire-pits
     * 
     */
    @JsonProperty("mapObjects")
    @JsonPropertyDescription("These are moving or fixed objects that will be positioned on the map to supplement xEvents or add realism.  Includes structures like trenches, forts, wrecks, walls, roadblocks and animals or insects, etc....  Also devices like c-wire, IEDs, boxes, fire-pits")
    private List<MapObject> mapObjects = new ArrayList<MapObject>();

    /**
     * describes the real-world physical location a synthetic map is representing - used to filter and translate real-world sensor data to synthetic environment e.g., SiVT
     * (Required)
     * 
     */
    @JsonProperty("exerciseMap")
    public Map getExerciseMap() {
        return exerciseMap;
    }

    /**
     * describes the real-world physical location a synthetic map is representing - used to filter and translate real-world sensor data to synthetic environment e.g., SiVT
     * (Required)
     * 
     */
    @JsonProperty("exerciseMap")
    public void setExerciseMap(Map exerciseMap) {
        this.exerciseMap = exerciseMap;
    }

    /**
     * the real or synthetic environmental parameters for the exercise map, based on the real geographic location, altitude, season and time of day.  Ideally will draw from historical data and randomized air-pressure systems.  These variables can be set dynamically by rendering engine during design but must be based on modeled data from other settings like location, season, sun or moon data, etc.  Can include other non-natural elements such as smoke and CBRN levels.  Can get much of this data from sites like http://www.worldweatheronline.com
     * (Required)
     * 
     */
    @JsonProperty("exerciseEnvironment")
    public Metoc getExerciseEnvironment() {
        return exerciseEnvironment;
    }

    /**
     * the real or synthetic environmental parameters for the exercise map, based on the real geographic location, altitude, season and time of day.  Ideally will draw from historical data and randomized air-pressure systems.  These variables can be set dynamically by rendering engine during design but must be based on modeled data from other settings like location, season, sun or moon data, etc.  Can include other non-natural elements such as smoke and CBRN levels.  Can get much of this data from sites like http://www.worldweatheronline.com
     * (Required)
     * 
     */
    @JsonProperty("exerciseEnvironment")
    public void setExerciseEnvironment(Metoc exerciseEnvironment) {
        this.exerciseEnvironment = exerciseEnvironment;
    }

    /**
     * These are moving or fixed objects that will be positioned on the map to supplement xEvents or add realism.  Includes structures like trenches, forts, wrecks, walls, roadblocks and animals or insects, etc....  Also devices like c-wire, IEDs, boxes, fire-pits
     * 
     */
    @JsonProperty("mapObjects")
    public List<MapObject> getMapObjects() {
        return mapObjects;
    }

    /**
     * These are moving or fixed objects that will be positioned on the map to supplement xEvents or add realism.  Includes structures like trenches, forts, wrecks, walls, roadblocks and animals or insects, etc....  Also devices like c-wire, IEDs, boxes, fire-pits
     * 
     */
    @JsonProperty("mapObjects")
    public void setMapObjects(List<MapObject> mapObjects) {
        this.mapObjects = mapObjects;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("exerciseMap");
        sb.append('=');
        sb.append(((this.exerciseMap == null)?"<null>":this.exerciseMap));
        sb.append(',');
        sb.append("exerciseEnvironment");
        sb.append('=');
        sb.append(((this.exerciseEnvironment == null)?"<null>":this.exerciseEnvironment));
        sb.append(',');
        sb.append("mapObjects");
        sb.append('=');
        sb.append(((this.mapObjects == null)?"<null>":this.mapObjects));
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
        result = ((result* 31)+((this.exerciseEnvironment == null)? 0 :this.exerciseEnvironment.hashCode()));
        result = ((result* 31)+((this.exerciseMap == null)? 0 :this.exerciseMap.hashCode()));
        result = ((result* 31)+((this.mapObjects == null)? 0 :this.mapObjects.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Environment) == false) {
            return false;
        }
        Environment rhs = ((Environment) other);
        return ((((this.exerciseEnvironment == rhs.exerciseEnvironment)||((this.exerciseEnvironment!= null)&&this.exerciseEnvironment.equals(rhs.exerciseEnvironment)))&&((this.exerciseMap == rhs.exerciseMap)||((this.exerciseMap!= null)&&this.exerciseMap.equals(rhs.exerciseMap))))&&((this.mapObjects == rhs.mapObjects)||((this.mapObjects!= null)&&this.mapObjects.equals(rhs.mapObjects))));
    }

}
