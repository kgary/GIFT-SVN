package mil.arl.gift.net.embedded.message;

import java.util.List;

public class EmbeddedTimerBatch {

    private String timestamp;
    private int dataSize;
    private List<EmbeddedTimer> messages;


    public EmbeddedTimerBatch(String timestamp, int dataSize, List<EmbeddedTimerBatch> messages){
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

    public List<EmbeddedTimer> getMessages() {
        return messages;
    }

    public void setMessages(List<EmbeddedTimer> messages) {
        this.messages = messages;
    }
}