package mil.arl.gift.net.embedded.message;

import java.util.List;

public class EmbeddedCompetencyMessageBatch {

    private String timestamp;
    private int dataSize;
    private List<EmbeddedCompetencyMessage> messages;


    public EmbeddedCompetencyMessageBatch(String timestamp, int dataSize, List<EmbeddedCompetencyMessage> messages){
        this.timestamp = timestamp;
        this.dataSize = dataSize;
        this.messages = messages;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public List<EmbeddedCompetencyMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<EmbeddedCompetencyMessage> messages) {
        this.messages = messages;
    }
}