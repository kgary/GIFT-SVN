package mil.arl.gift.net.embedded.message.codec.json.timer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.timer.CasualtyLayer;
import java.util.List;

public class CasualtyLayerJSON {

    public CasualtyLayer parse(JSONObject jsonObj) {
        JSONArray casualtiesArray = (JSONArray) jsonObj.get("casualties");
        CasualtyLayer casualtyLayer = new CasualtyLayer();
        for (Object obj : casualtiesArray) {
            JSONObject casualtyObj = (JSONObject) obj;
            casualtyLayer.addCasualty((String) casualtyObj.get("id"), (String) casualtyObj.get("status"));
        }
        return casualtyLayer;
    }

    public void encode(JSONObject jsonObj, CasualtyLayer casualtyLayer) {
        JSONArray casualtiesArray = new JSONArray();
        List<CasualtyLayer.Casualty> casualties = casualtyLayer.getCasualties();
        for (CasualtyLayer.Casualty casualty : casualties) {
            JSONObject casualtyObj = new JSONObject();
            casualtyObj.put("id", casualty.getId());
            casualtyObj.put("status", casualty.getStatus());
            casualtiesArray.add(casualtyObj);
        }
        jsonObj.put("casualties", casualtiesArray);
    }
}
