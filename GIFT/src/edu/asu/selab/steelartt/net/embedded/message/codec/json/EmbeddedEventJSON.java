package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import mil.arl.gift.net.embedded.message.EmbeddedEvent;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.net.api.message.MessageDecodeException;

public class EmbeddedEventJSON implements JSONCodec {
    @Override
    public Object decode(JSONObject jsonObj)  throws MessageDecodeException{
        return new EmbeddedEvent(
            (String) jsonObj.get("sessionID"),
            (String) jsonObj.get("scenarioEvent"),
            (String) jsonObj.get("timestamp"),
            (String) jsonObj.get("event"),
            (String) jsonObj.get("subtype"),
            (String) jsonObj.get("subtypeId")
        );
    }

    @Override
    public void encode(JSONObject jsonObj, Object payload)  throws MessageDecodeException{
        EmbeddedEvent event = (EmbeddedEvent) payload;
        jsonObj.put("sessionID", event.getSessionID());
        jsonObj.put("scenarioEvent", event.getScenarioEvent());
        jsonObj.put("timestamp", event.getTimestamp());
        jsonObj.put("event", event.getEvent());
        jsonObj.put("subtype", event.getSubtype());
        jsonObj.put("subtypeId", event.getSubtypeId());
    }
}
