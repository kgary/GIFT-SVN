package mil.arl.gift.net.embedded.message;

import java.util.List;

public class EmbeddedPositionalMessageBatch {

    private String timestamp;
    private int dataSize;
    private List<EmbeddedPositionalMessage> messages;


    public EmbeddedPositionalMessageBatch(String timestamp, int dataSize, List<EmbeddedPositionalMessage> messages){
        this.timestamp = timestamp;
        this.dataSize = dataSize;
        this.messages = messages;
    }

    // Getters and Setters

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

    public List<EmbeddedPositionalMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<EmbeddedPositionalMessage> messages) {
        this.messages = messages;
    }
}
