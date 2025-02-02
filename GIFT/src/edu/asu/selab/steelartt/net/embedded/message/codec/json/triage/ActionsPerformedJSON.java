package mil.arl.gift.net.embedded.message.codec.json.triage;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.triage.ActionsPerformed;

public class ActionsPerformedJSON {

    public ActionsPerformed parse(JSONObject jsonObj) {
        return new ActionsPerformed(
            (boolean) jsonObj.get("exitWoundIdentified"),
            (boolean) jsonObj.get("airwayObstructionIdentified"),
            (boolean) jsonObj.get("shockIdentified"),
            (boolean) jsonObj.get("hypothermiaIdentified"),
            (boolean) jsonObj.get("bleedingIdentified"),
            (boolean) jsonObj.get("respiratoryDistressIdentified"),
            (boolean) jsonObj.get("severePainIdentified"),
            (boolean) jsonObj.get("woundAreaIdentified")
        );
    }

    public void encode(JSONObject jsonObj, ActionsPerformed actionsPerformed) {
        jsonObj.put("exitWoundIdentified", actionsPerformed.isExitWoundIdentified());
        jsonObj.put("airwayObstructionIdentified", actionsPerformed.isAirwayObstructionIdentified());
        jsonObj.put("shockIdentified", actionsPerformed.isShockIdentified());
        jsonObj.put("hypothermiaIdentified", actionsPerformed.isHypothermiaIdentified());
        jsonObj.put("bleedingIdentified", actionsPerformed.isBleedingIdentified());
        jsonObj.put("respiratoryDistressIdentified", actionsPerformed.isRespiratoryDistressIdentified());
        jsonObj.put("severePainIdentified", actionsPerformed.isSeverePainIdentified());
        jsonObj.put("woundAreaIdentified", actionsPerformed.isWoundAreaIdentified());
    }
}
