package mil.arl.gift.net.embedded.message.codec.json.competency;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.competency.layers.CasualtyLayer;
import mil.arl.gift.net.embedded.message.competency.components.Casualty;

import java.util.ArrayList;
import java.util.List;

public class CasualtyLayerJSON {

    private static final String CASUALTY = "Casualty";

    public CasualtyLayer parse(JSONObject jsonObj) throws MessageDecodeException {
        try {
            JSONArray casualtiesJsonArray = (JSONArray) jsonObj.get(CASUALTY);
            if (casualtiesJsonArray == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Casualty field is missing.");
            }

            List<Casualty> casualties = new ArrayList<>();
            for (Object casualtyObj : casualtiesJsonArray) {
                JSONObject casualtyJson = (JSONObject) casualtyObj;

                Casualty casualty = new Casualty(
                    (String) casualtyJson.get("timestamp"),
                    (String) casualtyJson.get("id"),
                    (String) casualtyJson.get("triageStatus"),
                    (String) casualtyJson.get("stable"),
                    (String) casualtyJson.get("gettingTransported"),
                    (String) casualtyJson.get("gettingTreated")
                );

                casualties.add(casualty);
            }

            return new CasualtyLayer(casualties);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing CasualtyLayer.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, CasualtyLayer casualtyLayer) {
        JSONArray casualtiesJsonArray = new JSONArray();

        for (Casualty casualty : casualtyLayer.getCasualty()) {
            JSONObject casualtyJson = new JSONObject();
            casualtyJson.put("timestamp", casualty.getTimestamp());
            casualtyJson.put("id", casualty.getId());
            casualtyJson.put("triageStatus", casualty.getTriageStatus());
            casualtyJson.put("stable", casualty.getStable());
            casualtyJson.put("gettingTransported", casualty.getGettingTransported());
            casualtyJson.put("gettingTreated", casualty.getGettingTreated());
            casualtiesJsonArray.add(casualtyJson);
        }

        jsonObj.put(CASUALTY, casualtiesJsonArray);
    }
}
