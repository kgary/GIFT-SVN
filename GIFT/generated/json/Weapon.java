
package generated.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Weapon {

    M_249_LMG("M249 LMG"),
    M_240_MMG("M240 MMG"),
    M_60_LMG("M60 LMG"),
    M_2_A_1_HMG("M2A1 HMG"),
    MK_19_GMG("MK19 GMG"),
    FMG_138_JAV("FMG-138 JAV"),
    AT_4_CS_ATW("AT4-CS ATW"),
    M_4_M_16_CARB_RIFLE("M4/M16 Carb/Rifle"),
    M_203_320_GL("M203/320 GL"),
    M_26_SHOTGUN("M26 Shotgun"),
    M_67_GRENADE("M67 Grenade");
    private final String value;
    private final static Map<String, Weapon> CONSTANTS = new HashMap<String, Weapon>();

    static {
        for (Weapon c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    Weapon(String value) {
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
    public static Weapon fromValue(String value) {
        Weapon constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
