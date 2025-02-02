package mil.arl.gift.net.embedded.message.triage;

import java.util.Map;

public class ActionsPerformed {
    private boolean exitWoundIdentified;
    private boolean airwayObstructionIdentified;
    private boolean shockIdentified;
    private boolean hypothermiaIdentified;
    private boolean bleedingIdentified;
    private boolean respiratoryDistressIdentified;
    private boolean severePainIdentified;
    private boolean woundAreaIdentified;

    public ActionsPerformed(boolean exitWoundIdentified, boolean airwayObstructionIdentified, boolean shockIdentified,
                            boolean hypothermiaIdentified, boolean bleedingIdentified, boolean respiratoryDistressIdentified,
                            boolean severePainIdentified, boolean woundAreaIdentified) {
        this.exitWoundIdentified = exitWoundIdentified;
        this.airwayObstructionIdentified = airwayObstructionIdentified;
        this.shockIdentified = shockIdentified;
        this.hypothermiaIdentified = hypothermiaIdentified;
        this.bleedingIdentified = bleedingIdentified;
        this.respiratoryDistressIdentified = respiratoryDistressIdentified;
        this.severePainIdentified = severePainIdentified;
        this.woundAreaIdentified = woundAreaIdentified;
    }

    public boolean isExitWoundIdentified() {
        return exitWoundIdentified;
    }

    public boolean isAirwayObstructionIdentified() {
        return airwayObstructionIdentified;
    }

    public boolean isShockIdentified() {
        return shockIdentified;
    }

    public boolean isHypothermiaIdentified() {
        return hypothermiaIdentified;
    }

    public boolean isBleedingIdentified() {
        return bleedingIdentified;
    }

    public boolean isRespiratoryDistressIdentified() {
        return respiratoryDistressIdentified;
    }

    public boolean isSeverePainIdentified() {
        return severePainIdentified;
    }

    public boolean isWoundAreaIdentified() {
        return woundAreaIdentified;
    }
}
