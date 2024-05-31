
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlatformType {

    M_1_A_1_A_2_V_3_ABRAMS_TANK("M1A1/A2v3 Abrams Tank"),
    M_2_BRADLEY_AFV("M2 Bradley AFV"),
    M_1064_A_3_TRACK_MCV("M1064A3 Track MCV"),
    STRYKER_ICV("Stryker ICV"),
    STRYKER_MCV("Stryker MCV"),
    STRYKER_RECON("Stryker Recon"),
    STRYKER_CROWS_J("Stryker CROWS-J"),
    STRYKER_30_MM("Stryker 30mm"),
    STRYKER_MGS("Stryker MGS"),
    STRYKER_ATGM("Stryker ATGM"),
    M_1152_AX_HMMWV("M1152AX HMMWV"),
    SUPPORT_TRUCK("Support Truck"),
    SCOUT_VEHICLE("Scout Vehicle");
    private final String value;
    private final static Map<String, PlatformType> CONSTANTS = new HashMap<String, PlatformType>();

    static {
        for (PlatformType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    PlatformType(String value) {
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
    public static PlatformType fromValue(String value) {
        PlatformType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
