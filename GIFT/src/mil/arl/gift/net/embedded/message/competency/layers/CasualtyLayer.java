package mil.arl.gift.net.embedded.message.competency.layers;
import mil.arl.gift.net.embedded.message.competency.components.Casualty;
import java.util.*;

public class CasualtyLayer {
    private List<Casualty> Casualty;

    public List<Casualty> getCasualty() {
        return Casualty;
    }

    public void setCasualty(List<Casualty> Casualty) {
        this.Casualty = Casualty;
    }

    public CasualtyLayer(List<Casualty> Casualty){
        this.Casualty=Casualty;
    }
}
