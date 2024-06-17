package mil.arl.gift.net.embedded.message.codec.json;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.embedded.message.EmbeddedPositionalMessage;
import mil.arl.gift.net.json.JSONCodec;

import mil.arl.gift.net.embedded.message.Position;
import mil.arl.gift.net.embedded.message.Rotation;

public class EmbeddedPositionalMessageJSON implements JSONCodec {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedPositionalMessageJSON.class);

    private static final String POSITION = "position";
    private static final String ROTATION = "rotation";
    private static final String NAME = "name";
    private static final String PARENT_INDEX = "parentIndex";

    @Override
    public Object decode(JSONObject jsonObj) throws MessageDecodeException {

        try{
            logger.info("Received JSONObject: ");
            logger.info(jsonObj.toJSONString());
            JSONObject positionJson = (JSONObject) jsonObj.get(POSITION);
            if (positionJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Position field is missing.");
            }

            Position position = new Position(
                (double) positionJson.get("x"),
                (double) positionJson.get("y"),
                (double) positionJson.get("z")
            );

            JSONObject rotationJson = (JSONObject) jsonObj.get(ROTATION);
            if (rotationJson == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Rotation field is missing.");
            }
            Rotation rotation = new Rotation(
                (double) rotationJson.get("x"),
                (double) rotationJson.get("y"),
                (double) rotationJson.get("z"),
                (double) rotationJson.get("w")
            );

            String name = (String) jsonObj.get(NAME);
            if (name == null) {
                throw new MessageDecodeException(this.getClass().getName(), "Name field is missing.");
            }

            Long parentIndexLong = (Long) jsonObj.get(PARENT_INDEX);
            if (parentIndexLong == null) {
                throw new MessageDecodeException(this.getClass().getName(), "ParentIndex field is missing.");
            }
            int parentIndex = parentIndexLong.intValue();

            return new EmbeddedPositionalMessage(position, rotation, name, parentIndex);

        }catch(Exception e){
            logger.error("caught exception while creating "+this.getClass().getName()+" from "+jsonObj, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(JSONObject jsonObj, Object payload) {

        EmbeddedPositionalMessage state = (EmbeddedPositionalMessage) payload;

        JSONObject positionJson = new JSONObject();
        Position position = state.getPosition();
        positionJson.put("x", position.getX());
        positionJson.put("y", position.getY());
        positionJson.put("z", position.getZ());

        JSONObject rotationJson = new JSONObject();
        Rotation rotation = state.getRotation();
        rotationJson.put("x", rotation.getX());
        rotationJson.put("y", rotation.getY());
        rotationJson.put("z", rotation.getZ());
        rotationJson.put("w", rotation.getW());

        jsonObj.put(POSITION, positionJson);
        jsonObj.put(ROTATION, rotationJson);
        jsonObj.put(NAME, state.getName());
        jsonObj.put(PARENT_INDEX, state.getParentIndex());
    }
}
