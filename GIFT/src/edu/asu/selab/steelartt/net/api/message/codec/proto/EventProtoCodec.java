package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.EventProto;
import mil.arl.gift.common.ta.state.Event;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * Conversion/Map between the generated EventProto.Event class and mil.arl.gift.common.ta.state.Event
 */
public class EventProtoCodec implements ProtoCodec<EventProto.Event, Event> {

  @Override
  public Event convert(EventProto.Event protoObject) {
    if (protoObject == null) {
      return null;
    }
    return new Event(
      protoObject.getSessionID(),
      protoObject.getScenarioEvent(),
      protoObject.getTimestamp(),
      protoObject.getEvent(),
      protoObject.getSubtype(),
      protoObject.getSubtypeId()
    );
  }

  @Override
  public EventProto.Event map(Event commonObject) {
    if (commonObject == null) {
      return null;
    }
    return EventProto.Event.newBuilder()
      .setSessionID(commonObject.getSessionID())
      .setScenarioEvent(commonObject.getScenarioEvent())
      .setTimestamp(commonObject.getTimestamp())
      .setEvent(commonObject.getEvent())
      .setSubtype(commonObject.getSubtype())
      .setSubtypeId(commonObject.getSubtypeId())
      .build();
  }
}
