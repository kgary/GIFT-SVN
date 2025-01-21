package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedCompetencyMessage;
import mil.arl.gift.net.json.JSONCodec;
import mil.arl.gift.net.embedded.message.codec.json.competency.*;
import mil.arl.gift.net.embedded.message.competency.layers.*;

public class EmbeddedCompetencyMessageJSON implements JSONCodec {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedCompetencyMessageJSON.class);

    private static final String CASUALTY_LAYER = "causalities";
    private static final String TRAINEE_LAYER = "trainees";

    private final CasualtyLayerJSON casualtyLayerJSON = new CasualtyLayerJSON();
    private final TraineeLayerJSON traineeLayerJSON = new TraineeLayerJSON();

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {
        try {
            logger.info("Received JSONObject: ");
            logger.info(jsonObj.toJSONString());

            CasualtyLayer casualtyLayer = casualtyLayerJSON.parse(jsonObj);
            TraineeLayer traineeLayer = traineeLayerJSON.parse(jsonObj);

            EmbeddedCompetencyMessage message = new EmbeddedCompetencyMessage();
            message.setCasualtyLayer(casualtyLayer);
            message.setTraineeLayer(traineeLayer);

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

        JSONObject traineeLayerJson = new JSONObject();
        traineeLayerJSON.encode(traineeLayerJson, message.getTraineeLayer());
        jsonObj.put(TRAINEE_LAYER, traineeLayerJson);
    }
}
