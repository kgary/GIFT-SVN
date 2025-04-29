package mil.arl.gift.common.ta.state;

import java.util.List;

public class TimerBatch implements TrainingAppState{

    private String timestamp;
    private int dataSize;
    private List<Timer> messages;


    public TimerBatch(String timestamp, int dataSize, List<Timer> messages){
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

    public List<Timer> getMessages() {
        return messages;
    }

    public void setMessages(List<Timer> messages) {
        this.messages = messages;
    }
}