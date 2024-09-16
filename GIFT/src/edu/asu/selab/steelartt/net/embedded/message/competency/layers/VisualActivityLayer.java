package mil.arl.gift.net.embedded.message.competency.layers;
import mil.arl.gift.net.embedded.message.competency.components.AOI;
import mil.arl.gift.net.embedded.message.competency.components.LookingAt;

public class VisualActivityLayer {
    private AOI AOI;
    private LookingAt LookingAt;

    public AOI getAOI() {
        return AOI;
    }

    public void setAOI(AOI AOI) {
        this.AOI = AOI;
    }

    public LookingAt getLookingAt() {
        return LookingAt;
    }

    public void setLookingAt(LookingAt LookingAt) {
        this.LookingAt = LookingAt;
    }

    public VisualActivityLayer(AOI AOI, LookingAt LookingAt){
        this.AOI = AOI;
        this.LookingAt = LookingAt;
    }
}
