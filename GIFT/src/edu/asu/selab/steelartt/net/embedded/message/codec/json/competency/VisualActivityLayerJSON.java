package mil.arl.gift.net.embedded.message.codec.json.competency;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.competency.layers.VisualActivityLayer;
import mil.arl.gift.net.embedded.message.competency.components.AOI;
import mil.arl.gift.net.embedded.message.competency.components.Gaze;
import mil.arl.gift.net.embedded.message.competency.components.LookingAt;
import mil.arl.gift.net.embedded.message.competency.components.LookingAt.InnerLookingAt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisualActivityLayerJSON {

    private static final String AOI = "AOI";
    private static final String LOOKING_AT = "LookingAt";

    public VisualActivityLayer parse(JSONObject jsonObj) throws MessageDecodeException {
        try {
            JSONObject aoiJson = (JSONObject) jsonObj.get(AOI);
            if (aoiJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "AOI field is missing.");
            }

            AOI aoi = parseAOI(aoiJson);

            JSONObject lookingAtJson = (JSONObject) jsonObj.get(LOOKING_AT);
            if (lookingAtJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "LookingAt field is missing.");
            }

            LookingAt lookingAt = parseLookingAt(lookingAtJson);

            return new VisualActivityLayer(aoi, lookingAt);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing VisualActivityLayer.", e);
        }
    }

    private AOI parseAOI(JSONObject jsonObj) throws MessageDecodeException {
        try {
            String id = (String) jsonObj.get("id");
            JSONObject gazeJson = (JSONObject) jsonObj.get("gaze");

            Gaze gaze = new Gaze(
                (List<String>) gazeJson.get("trainees"),
                (String) gazeJson.get("timestamp")
            );

            return new AOI(id, gaze);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing AOI.", e);
        }
    }

    private LookingAt parseLookingAt(JSONObject jsonObj) throws MessageDecodeException {
        try {
            String id = (String) jsonObj.get("id");
            JSONObject innerLookingAtJson = (JSONObject) jsonObj.get("LookingAt");

            InnerLookingAt innerLookingAt = new InnerLookingAt(
                (List<String>) innerLookingAtJson.get("objects"),
                (String) innerLookingAtJson.get("timestamp")
            );

            return new LookingAt(id, innerLookingAt);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing LookingAt.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, VisualActivityLayer visualActivityLayer) {
        //AOI
        JSONObject aoiJson = new JSONObject();
        AOI aoi = visualActivityLayer.getAOI();
        aoiJson.put("id", aoi.getId());

        //AOI-Gaze
        JSONObject gazeJson = new JSONObject();
        Gaze gaze = aoi.getGaze();
        gazeJson.put("trainees", gaze.getTrainees());
        gazeJson.put("timestamp", gaze.getTimestamp());
        aoiJson.put("gaze", gazeJson);
        jsonObj.put(AOI, aoiJson);

        //LookingAt
        JSONObject lookingAtJson = new JSONObject();
        LookingAt lookingAt = visualActivityLayer.getLookingAt();
        lookingAtJson.put("id", lookingAt.getId());

        //LookingAt-InnerLookingAt
        JSONObject innerLookingAtJson = new JSONObject();
        InnerLookingAt innerLookingAt = lookingAt.getInnerLookingAt();
        innerLookingAtJson.put("objects", innerLookingAt.getObjects());
        innerLookingAtJson.put("timestamp", innerLookingAt.getTimestamp());
        lookingAtJson.put("LookingAt", innerLookingAtJson);
        jsonObj.put(LOOKING_AT, lookingAtJson);
    }
}
