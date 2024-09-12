package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedCompetencyMessage;
import mil.arl.gift.net.json.JSONCodec;
import edu.asu.selab.steelartt.net.embedded.message.codec.json.competency.*;
import mil.arl.gift.net.embedded.message.competency.layers.*;

public class EmbeddedCompetencyMessageJSON implements JSONCodec {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedCompetencyMessageJSON.class);

    private static final String CASUALTY_LAYER = "CasualtyLayer";
    private static final String VISUAL_ACTIVITY_LAYER = "VisualActivityLayer";
    private static final String TRAINEE_LAYER = "TraineeLayer";
    private static final String ENVIRONMENT_LAYER = "EnvironmentLayer";
    private static final String COMMUNICATION_LAYER = "CommunicationLayer";

    private final CasualtyLayerJSON casualtyLayerJSON = new CasualtyLayerJSON();
    private final VisualActivityLayerJSON visualActivityLayerJSON = new VisualActivityLayerJSON();
    private final TraineeLayerJSON traineeLayerJSON = new TraineeLayerJSON();
    private final EnvironmentLayerJSON environmentLayerJSON = new EnvironmentLayerJSON();
    private final CommunicationLayerJSON communicationLayerJSON = new CommunicationLayerJSON();

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {
        try {
            logger.info("Received JSONObject: ");
            logger.info(jsonObj.toJSONString());

            CasualtyLayer casualtyLayer = casualtyLayerJSON.parse((JSONObject) jsonObj.get(CASUALTY_LAYER));
            VisualActivityLayer visualActivityLayer = visualActivityLayerJSON.parse((JSONObject) jsonObj.get(VISUAL_ACTIVITY_LAYER));
            TraineeLayer traineeLayer = traineeLayerJSON.parse((JSONObject) jsonObj.get(TRAINEE_LAYER));
            EnvironmentLayer environmentLayer = environmentLayerJSON.parse((JSONObject) jsonObj.get(ENVIRONMENT_LAYER));
            CommunicationLayer communicationLayer = communicationLayerJSON.parse((JSONObject) jsonObj.get(COMMUNICATION_LAYER));

            EmbeddedCompetencyMessage message = new EmbeddedCompetencyMessage();
            message.setCasualtyLayer(casualtyLayer);
            message.setVisualActivityLayer(visualActivityLayer);
            message.setTraineeLayer(traineeLayer);
            message.setEnvironmentLayer(environmentLayer);
            message.setCommunicationLayer(communicationLayer);

            return message;

        } catch (Exception e) {
            logger.error("Caught exception while creating " + this.getClass().getName() + " from " + jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {
        EmbeddedCompetencyMessage message = (EmbeddedCompetencyMessage) payload;

        JSONObject casualtyLayerJson = new JSONObject();
        casualtyLayerJSON.encode(casualtyLayerJson, message.getCasualtyLayer());
        jsonObj.put(CASUALTY_LAYER, casualtyLayerJson);

        JSONObject visualActivityLayerJson = new JSONObject();
        visualActivityLayerJSON.encode(visualActivityLayerJson, message.getVisualActivityLayer());
        jsonObj.put(VISUAL_ACTIVITY_LAYER, visualActivityLayerJson);

        JSONObject traineeLayerJson = new JSONObject();
        traineeLayerJSON.encode(traineeLayerJson, message.getTraineeLayer());
        jsonObj.put(TRAINEE_LAYER, traineeLayerJson);

        JSONObject environmentLayerJson = new JSONObject();
        environmentLayerJSON.encode(environmentLayerJson, message.getEnvironmentLayer());
        jsonObj.put(ENVIRONMENT_LAYER, environmentLayerJson);

        JSONObject communicationLayerJson = new JSONObject();
        communicationLayerJSON.encode(communicationLayerJson, message.getCommunicationLayer());
        jsonObj.put(COMMUNICATION_LAYER, communicationLayerJson);
    }
}