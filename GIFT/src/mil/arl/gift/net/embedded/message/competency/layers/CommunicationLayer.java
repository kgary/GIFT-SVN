package mil.arl.gift.net.embedded.message.competency.layers;
import mil.arl.gift.net.embedded.message.competency.components.Speaker;
import mil.arl.gift.net.embedded.message.competency.components.DirectedCommunication;

public class CommunicationLayer {
    private Speaker Speaker;
    private DirectedCommunication DirectedCommunication;

    public Speaker getSpeaker() {
        return Speaker;
    }

    public void setSpeaker(Speaker Speaker) {
        this.Speaker = Speaker;
    }

    public DirectedCommunication getDirectedCommunication() {
        return DirectedCommunication;
    }

    public void setDirectedCommunication(DirectedCommunication DirectedCommunication) {
        this.DirectedCommunication = DirectedCommunication;
    }

    public CommunicationLayer(Speaker Speaker,DirectedCommunication DirectedCommunication){
        this.Speaker = Speaker;
        this.DirectedCommunication = DirectedCommunication;
    }
}
