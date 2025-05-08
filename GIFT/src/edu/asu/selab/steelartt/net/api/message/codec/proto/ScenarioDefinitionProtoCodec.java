package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.ScenarioDefinitionProto;
import generated.proto.common.CommonEntitiesProto;

import mil.arl.gift.common.ta.state.ScenarioDefinition;
import mil.arl.gift.common.ta.state.scenarioDefinition.Scenario;
import mil.arl.gift.common.ta.state.scenarioDefinition.AreaOfInterest;
import mil.arl.gift.common.ta.state.scenarioDefinition.Casualty;
import mil.arl.gift.common.ta.state.scenarioDefinition.Trainee;
import mil.arl.gift.common.ta.state.scenarioDefinition.ObjectOfInterest;
import mil.arl.gift.common.ta.state.scenarioDefinition.RegionOfInterest;
import mil.arl.gift.common.ta.state.scenarioDefinition.NPC;

import mil.arl.gift.net.proto.ProtoCodec;

import java.util.ArrayList;
import java.util.List;

public class ScenarioDefinitionProtoCodec
    implements ProtoCodec<ScenarioDefinitionProto.ScenarioDefinition, ScenarioDefinition> {

  @Override
  public ScenarioDefinition convert(ScenarioDefinitionProto.ScenarioDefinition p) {
    if (p == null) return null;

    ScenarioDefinitionProto.Scenario protoScen = p.getScenario();

    // casualties
    List<Casualty> cs = new ArrayList<>();
    for (CommonEntitiesProto.Casualty c : protoScen.getCasualtiesList()) {
      cs.add(new Casualty(
        c.getId(),
        c.getStatus(),
        new ArrayList<Double>(),
        new ArrayList<AreaOfInterest>()
      ));
    }

    // trainees
    List<Trainee> ts = new ArrayList<>();
    for (ScenarioDefinitionProto.Trainee t : protoScen.getTraineesList()) {
      List<AreaOfInterest> ta = new ArrayList<>();
      for (ScenarioDefinitionProto.AreaOfInterest a : t.getAreasOfInterestList()) {
        ta.add(new AreaOfInterest(
          a.getLabel(),
          a.getAreaType(),
          a.getInterestType(),
          a.getDescription()
        ));
      }
      ts.add(new Trainee(
        t.getId(),
        t.getDescription(),
        t.getRole(),
        t.getLocationList(),
        ta
      ));
    }

    // objects of interest
    List<ObjectOfInterest> os = new ArrayList<>();
    for (ScenarioDefinitionProto.ObjectOfInterest o : protoScen.getObjectsOfInterestList()) {
      List<AreaOfInterest> oa = new ArrayList<>();
      for (ScenarioDefinitionProto.AreaOfInterest a : o.getAreasOfInterestList()) {
        oa.add(new AreaOfInterest(a.getLabel(), a.getAreaType(), a.getInterestType(), a.getDescription()));
      }
      os.add(new ObjectOfInterest(
        o.getId(),
        o.getDescription(),
        o.getType(),
        o.getLocationList(),
        oa
      ));
    }

    // regions of interest
    List<RegionOfInterest> rs = new ArrayList<>();
    for (ScenarioDefinitionProto.RegionOfInterest r : protoScen.getRegionsOfInterestList()) {
      List<AreaOfInterest> ra = new ArrayList<>();
      for (ScenarioDefinitionProto.AreaOfInterest a : r.getAreasOfInterestList()) {
        ra.add(new AreaOfInterest(a.getLabel(), a.getAreaType(), a.getInterestType(), a.getDescription()));
      }
      rs.add(new RegionOfInterest(
        r.getId(),
        r.getDescription(),
        r.getType(),
        r.getLocationList(),
        ra
      ));
    }

    // NPCs
    List<NPC> ns = new ArrayList<>();
    for (ScenarioDefinitionProto.NPC n : protoScen.getNpcsList()) {
      List<AreaOfInterest> na = new ArrayList<>();
      for (ScenarioDefinitionProto.AreaOfInterest a : n.getAreasOfInterestList()) {
        na.add(new AreaOfInterest(a.getLabel(), a.getAreaType(), a.getInterestType(), a.getDescription()));
      }
      ns.add(new NPC(
        n.getId(),
        n.getDescription(),
        n.getRole(),
        n.getLocationList(),
        na
      ));
    }

    Scenario scenario = new Scenario(
      protoScen.getId(),
      protoScen.getTitle(),
      protoScen.getDescription(),
      protoScen.getTimestamp(),
      cs, ts, os, rs, ns
    );

    return new ScenarioDefinition(p.getScenarioEvent(), scenario);
  }

  @Override
  public ScenarioDefinitionProto.ScenarioDefinition map(ScenarioDefinition def) {
    if (def == null) return null;

    ScenarioDefinitionProto.ScenarioDefinition.Builder out =
      ScenarioDefinitionProto.ScenarioDefinition.newBuilder()
        .setScenarioEvent(def.getScenarioEvent());

    Scenario scen = def.getScenario();
    ScenarioDefinitionProto.Scenario.Builder sb =
      ScenarioDefinitionProto.Scenario.newBuilder()
        .setId(scen.getId())
        .setTitle(scen.getTitle())
        .setDescription(scen.getDescription())
        .setTimestamp(scen.getTimestamp());

    // map casualties
    for (Casualty c : scen.getCasualties()) {
      sb.addCasualties(
        CommonEntitiesProto.Casualty.newBuilder()
          .setId(c.getId())
          .setStatus(c.getDescription())
          .build()
      );
    }

    // map trainees
    for (Trainee t : scen.getTrainees()) {
      ScenarioDefinitionProto.Trainee.Builder tb =
        ScenarioDefinitionProto.Trainee.newBuilder()
          .setId(t.getId())
          .setDescription(t.getDescription())
          .setRole(t.getRole())
          .addAllLocation(t.getLocation());
      for (AreaOfInterest a : t.getAreasOfInterest()) {
        tb.addAreasOfInterest(
          ScenarioDefinitionProto.AreaOfInterest.newBuilder()
            .setLabel(a.getLabel())
            .setAreaType(a.getAreaType())
            .setInterestType(a.getInterestType())
            .setDescription(a.getDescription())
            .build()
        );
      }
      sb.addTrainees(tb.build());
    }

    // map objects of interest
    for (ObjectOfInterest o : scen.getObjectsOfInterest()) {
      ScenarioDefinitionProto.ObjectOfInterest.Builder ob =
        ScenarioDefinitionProto.ObjectOfInterest.newBuilder()
          .setId(o.getId())
          .setDescription(o.getDescription())
          .setType(o.getType())
          .addAllLocation(o.getLocation());
      for (AreaOfInterest a : o.getAreasOfInterest()) {
        ob.addAreasOfInterest(
          ScenarioDefinitionProto.AreaOfInterest.newBuilder()
            .setLabel(a.getLabel())
            .setAreaType(a.getAreaType())
            .setInterestType(a.getInterestType())
            .setDescription(a.getDescription())
            .build()
        );
      }
      sb.addObjectsOfInterest(ob.build());
    }

    // map regions of interest
    for (RegionOfInterest r : scen.getRegionsOfInterest()) {
      ScenarioDefinitionProto.RegionOfInterest.Builder rb =
        ScenarioDefinitionProto.RegionOfInterest.newBuilder()
          .setId(r.getId())
          .setDescription(r.getDescription())
          .setType(r.getType())
          .addAllLocation(r.getLocation());
      for (AreaOfInterest a : r.getAreasOfInterest()) {
        rb.addAreasOfInterest(
          ScenarioDefinitionProto.AreaOfInterest.newBuilder()
            .setLabel(a.getLabel())
            .setAreaType(a.getAreaType())
            .setInterestType(a.getInterestType())
            .setDescription(a.getDescription())
            .build()
        );
      }
      sb.addRegionsOfInterest(rb.build());
    }

    // map NPCs
    for (NPC n : scen.getNPCs()) {
      ScenarioDefinitionProto.NPC.Builder nb =
        ScenarioDefinitionProto.NPC.newBuilder()
          .setId(n.getId())
          .setDescription(n.getDescription())
          .addAllLocation(n.getLocation());

        if (n.getRole() != null) {
            nb.setRole(n.getRole());
        } else {
            nb.setRole("");
        }      
        
        for (AreaOfInterest a : n.getAreasOfInterest()) {
        nb.addAreasOfInterest(
          ScenarioDefinitionProto.AreaOfInterest.newBuilder()
            .setLabel(a.getLabel())
            .setAreaType(a.getAreaType())
            .setInterestType(a.getInterestType())
            .setDescription(a.getDescription())
            .build()
        );
      }
      sb.addNpcs(nb.build());
    }

    out.setScenario(sb.build());
    return out.build();
  }
}
