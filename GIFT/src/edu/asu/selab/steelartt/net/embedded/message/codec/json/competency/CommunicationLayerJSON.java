package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.competency.layers.CommunicationLayer;
import mil.arl.gift.net.embedded.message.competency.components.Speaker;
import mil.arl.gift.net.embedded.message.competency.components.DirectedCommunication;

import java.util.List;

public class CommunicationLayerJSON {

    private static final String SPEAKER = "Speaker";
    private static final String DIRECTED_COMMUNICATION = "DirectedCommunication";

    public CommunicationLayer parse(JSONObject jsonObj) throws MessageDecodeException {
        try {
            JSONObject speakerJson = (JSONObject) jsonObj.get(SPEAKER);
            if (speakerJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Speaker field is missing.");
            }

            Speaker speaker = parseSpeaker(speakerJson);

            JSONObject directedCommunicationJson = (JSONObject) jsonObj.get(DIRECTED_COMMUNICATION);
            if (directedCommunicationJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "DirectedCommunication field is missing.");
            }

            DirectedCommunication directedCommunication = parseDirectedCommunication(directedCommunicationJson);

            return new CommunicationLayer(speaker, directedCommunication);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing CommunicationLayer.", e);
        }
    }

    private Speaker parseSpeaker(JSONObject jsonObj) throws MessageDecodeException {
        try {
            List<Integer> values = (List<Integer>) jsonObj.get("values");
            List<String> timestamps = (List<String>) jsonObj.get("timestamps");

            return new Speaker(values, timestamps);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing Speaker.", e);
        }
    }

    private DirectedCommunication parseDirectedCommunication(JSONObject jsonObj) throws MessageDecodeException {
        try {
            List<Integer> values = (List<Integer>) jsonObj.get("values");
            List<String> timestamps = (List<String>) jsonObj.get("timestamps");

            return new DirectedCommunication(values, timestamps);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Error parsing DirectedCommunication.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void encode(JSONObject jsonObj, CommunicationLayer communicationLayer) {
        JSONObject speakerJson = new JSONObject();
        Speaker speaker = communicationLayer.getSpeaker();

        speakerJson.put("values", speaker.getValues());
        speakerJson.put("timestamps", speaker.getTimestamps());

        jsonObj.put(SPEAKER, speakerJson);

        JSONObject directedCommunicationJson = new JSONObject();
        DirectedCommunication directedCommunication = communicationLayer.getDirectedCommunication();

        directedCommunicationJson.put("values", directedCommunication.getValues());
        directedCommunicationJson.put("timestamps", directedCommunication.getTimestamps());

        jsonObj.put(DIRECTED_COMMUNICATION, directedCommunicationJson);
    }
}
