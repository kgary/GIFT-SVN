package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.TriageProto;
import mil.arl.gift.common.ta.state.Triage;
import mil.arl.gift.common.ta.state.triage.ActionsPerformed;
import mil.arl.gift.net.proto.ProtoCodec;

public class TriageProtoCodec implements ProtoCodec<TriageProto.Triage, Triage> {

  @Override
  public Triage convert(TriageProto.Triage p) {
    if (p == null) return null;

    ActionsPerformed ap = null;
    if (p.hasActionsPerformed()) {
      TriageProto.Triage.ActionsPerformed pa = p.getActionsPerformed();
      ap = new ActionsPerformed(
        pa.getExitWoundIdentified(),
        pa.getAirwayObstructionIdentified(),
        pa.getShockIdentified(),
        pa.getHypothermiaIdentified(),
        pa.getBleedingIdentified(),
        pa.getRespiratoryDistressIdentified(),
        pa.getSeverePainIdentified(),
        pa.getWoundAreaIdentified()
      );
    }

    return new Triage(
      p.getSessionID(),
      p.getScenarioEvent(),
      p.getTimestamp(),
      p.getTraineeId(),
      p.getCasualtyId(),
      p.getSubtypeId(),
      ap
    );
  }

  @Override
  public TriageProto.Triage map(Triage t) {
    if (t == null) return null;

    TriageProto.Triage.Builder b = TriageProto.Triage.newBuilder()
      .setSessionID(t.getSessionID())
      .setScenarioEvent(t.getScenarioEvent())
      .setTimestamp(t.getTimestamp())
      .setTraineeId(t.getTraineeId())
      .setCasualtyId(t.getCasualtyId())
      .setSubtypeId(t.getSubtypeId());

    ActionsPerformed ap = t.getActionsPerformed();
    if (ap != null) {
      TriageProto.Triage.ActionsPerformed.Builder apb =
        TriageProto.Triage.ActionsPerformed.newBuilder();
      apb.setExitWoundIdentified(ap.isExitWoundIdentified());
      apb.setAirwayObstructionIdentified(ap.isAirwayObstructionIdentified());
      apb.setShockIdentified(ap.isShockIdentified());
      apb.setHypothermiaIdentified(ap.isHypothermiaIdentified());
      apb.setBleedingIdentified(ap.isBleedingIdentified());
      apb.setRespiratoryDistressIdentified(ap.isRespiratoryDistressIdentified());
      apb.setSeverePainIdentified(ap.isSeverePainIdentified());
      apb.setWoundAreaIdentified(ap.isWoundAreaIdentified());
      b.setActionsPerformed(apb);
    }

    return b.build();
  }
}
