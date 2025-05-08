package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.TimerBatchProto;
import generated.proto.common.TimerBatchProto.TraineeLayer.Trainee;
import generated.proto.common.CommonEntitiesProto;
import mil.arl.gift.common.ta.state.TimerBatch;
import mil.arl.gift.common.ta.state.Timer;
import mil.arl.gift.common.ta.state.timer.CasualtyLayer;
import mil.arl.gift.common.ta.state.timer.TraineeLayer;
import mil.arl.gift.net.proto.ProtoCodec;
import org.json.simple.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class TimerBatchProtoCodec implements ProtoCodec<TimerBatchProto.TimerBatch, TimerBatch> {

    @Override
    public TimerBatch convert(TimerBatchProto.TimerBatch p) {
        if (p == null) return null;

        List<Timer> timers = new ArrayList<>();
        for (TimerBatchProto.Timer t : p.getMessagesList()) {
            CasualtyLayer cl = new CasualtyLayer();
            for (CommonEntitiesProto.Casualty c : t.getCasualtyLayer().getCasualtiesList()) {
                cl.addCasualty(c.getId(), c.getStatus());
            }

            TraineeLayer tl = new TraineeLayer();
            for (Trainee tr : t.getTraineeLayer().getTraineesList()) {
                JSONArray ooiWatched = new JSONArray();
                for (String s : tr.getOoiWatchedList()) ooiWatched.add(s);

                JSONArray roiWatched = new JSONArray();
                for (String s : tr.getRoiWatchedList()) roiWatched.add(s);

                JSONArray headRotation = new JSONArray();
                for (String s : tr.getHeadRotationList()) headRotation.add(s);

                JSONArray head = new JSONArray();
                for (String s : tr.getHeadList()) head.add(s);

                JSONArray leftHand = new JSONArray();
                for (String s : tr.getLeftHandList()) leftHand.add(s);

                JSONArray rightHand = new JSONArray();
                for (String s : tr.getRightHandList()) rightHand.add(s);

                JSONArray leftFoot = new JSONArray();
                for (String s : tr.getLeftFootList()) leftFoot.add(s);

                JSONArray rightFoot = new JSONArray();
                for (String s : tr.getRightFootList()) rightFoot.add(s);

                tl.addTrainee(
                    tr.getId(),
                    tr.getCommunication(),
                    ooiWatched,
                    roiWatched,
                    headRotation,
                    head,
                    leftHand,
                    rightHand,
                    leftFoot,
                    rightFoot
                );
            }

            timers.add(new Timer(
                t.getSessionID(),
                t.getScenarioEvent(),
                t.getTimestamp(),
                cl,
                tl
            ));
        }

        return new TimerBatch(p.getTimestamp(), p.getDataSize(), timers);
    }

    @Override
    public TimerBatchProto.TimerBatch map(TimerBatch b) {
        if (b == null) return null;

        TimerBatchProto.TimerBatch.Builder out =
            TimerBatchProto.TimerBatch.newBuilder()
                .setTimestamp(b.getTimestamp())
                .setDataSize(b.getDataSize());

        for (Timer t : b.getMessages()) {
            TimerBatchProto.CasualtyLayer.Builder clb =
                TimerBatchProto.CasualtyLayer.newBuilder();
            for (CasualtyLayer.Casualty c : t.getCasualtyLayer().getCasualties()) {
                clb.addCasualties(
                    CommonEntitiesProto.Casualty.newBuilder()
                        .setId(c.getId())
                        .setStatus(c.getStatus())
                        .build()
                );
            }

            TimerBatchProto.TraineeLayer.Builder tlb =
                TimerBatchProto.TraineeLayer.newBuilder();
            for (TraineeLayer.Trainee tr : t.getTraineeLayer().getTrainees()) {
                List<String> headRotations = new ArrayList<>();
                if (tr.getHeadRotation() != null) {
                    for (Object o : tr.getHeadRotation()) {
                        headRotations.add(o.toString());
                    }
                }

                List<String> heads = new ArrayList<>();
                if (tr.getHead() != null) {
                    for (Object o : tr.getHead()) {
                        heads.add(o.toString());
                    }
                }

                List<String> leftHands = new ArrayList<>();
                if (tr.getLeftHand() != null) {
                    for (Object o : tr.getLeftHand()) {
                        leftHands.add(o.toString());
                    }
                }

                List<String> rightHands = new ArrayList<>();
                if (tr.getRightHand() != null) {
                    for (Object o : tr.getRightHand()) {
                        rightHands.add(o.toString());
                    }
                }

                List<String> leftFeet = new ArrayList<>();
                if (tr.getLeftFoot() != null) {
                    for (Object o : tr.getLeftFoot()) {
                        leftFeet.add(o.toString());
                    }
                }

                List<String> rightFeet = new ArrayList<>();
                if (tr.getRightFoot() != null) {
                    for (Object o : tr.getRightFoot()) {
                        rightFeet.add(o.toString());
                    }
                }

                tlb.addTrainees(
                    TimerBatchProto.TraineeLayer.Trainee.newBuilder()
                        .setId(tr.getId())
                        .setCommunication(tr.isCommunication())
                        .addAllOoiWatched(tr.getOOIWatched() != null ? tr.getOOIWatched() : new ArrayList<String>())
                        .addAllRoiWatched(tr.getROIWatched() != null ? tr.getROIWatched() : new ArrayList<String>())
                        .addAllHeadRotation(headRotations)
                        .addAllHead(heads)
                        .addAllLeftHand(leftHands)
                        .addAllRightHand(rightHands)
                        .addAllLeftFoot(leftFeet)
                        .addAllRightFoot(rightFeet)
                        .build()
                );
            }

            out.addMessages(
                TimerBatchProto.Timer.newBuilder()
                    .setSessionID(t.getSessionID())
                    .setScenarioEvent(t.getScenarioEvent())
                    .setTimestamp(t.getTimestamp())
                    .setCasualtyLayer(clb)
                    .setTraineeLayer(tlb)
                    .build()
            );
        }

        return out.build();
    }
}
