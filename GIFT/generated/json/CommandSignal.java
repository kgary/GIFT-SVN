
package generated.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "command",
    "control",
    "signal"
})
public class CommandSignal {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("command")
    private Object command;
    @JsonProperty("control")
    private Object control;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signal")
    private Object signal;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("command")
    public Object getCommand() {
        return command;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("command")
    public void setCommand(Object command) {
        this.command = command;
    }

    @JsonProperty("control")
    public Object getControl() {
        return control;
    }

    @JsonProperty("control")
    public void setControl(Object control) {
        this.control = control;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signal")
    public Object getSignal() {
        return signal;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signal")
    public void setSignal(Object signal) {
        this.signal = signal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CommandSignal.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("command");
        sb.append('=');
        sb.append(((this.command == null)?"<null>":this.command));
        sb.append(',');
        sb.append("control");
        sb.append('=');
        sb.append(((this.control == null)?"<null>":this.control));
        sb.append(',');
        sb.append("signal");
        sb.append('=');
        sb.append(((this.signal == null)?"<null>":this.signal));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.control == null)? 0 :this.control.hashCode()));
        result = ((result* 31)+((this.signal == null)? 0 :this.signal.hashCode()));
        result = ((result* 31)+((this.command == null)? 0 :this.command.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CommandSignal) == false) {
            return false;
        }
        CommandSignal rhs = ((CommandSignal) other);
        return ((((this.control == rhs.control)||((this.control!= null)&&this.control.equals(rhs.control)))&&((this.signal == rhs.signal)||((this.signal!= null)&&this.signal.equals(rhs.signal))))&&((this.command == rhs.command)||((this.command!= null)&&this.command.equals(rhs.command))));
    }

}
