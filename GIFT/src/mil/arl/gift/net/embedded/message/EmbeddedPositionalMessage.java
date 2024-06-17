package mil.arl.gift.net.embedded.message;

public class EmbeddedPositionalMessage {

    private Position position;
    private Rotation rotation;
    private String name;
    private int parentIndex;


    public EmbeddedPositionalMessage(Position position, Rotation rotation, String name, int parentIndex){

        if(position == null || rotation == null || name == null){
            throw new IllegalArgumentException("None of the parameters can be null.");
        }

        this.position = position;
        this.rotation = rotation;
        this.name = name;
        this.parentIndex = parentIndex;
    }

    public Position getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public String getName() {
        return name;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EmbeddedPositionalMessage: ");
        sb.append("position = ").append(getPosition());
        sb.append(", rotation = ").append(getRotation());
        sb.append(", name = ").append(getName());
        sb.append(", parentIndex = ").append(getParentIndex());
        sb.append("]");
        return sb.toString();
    }
}
