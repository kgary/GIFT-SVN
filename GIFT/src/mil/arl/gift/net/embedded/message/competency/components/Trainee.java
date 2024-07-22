package mil.arl.gift.net.embedded.message.competency.components;

public class Trainee {
    private String id;
    private String role;
    private String location;
    private String heading;
    private String timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Trainee(String id, String role, String location, String heading, String timestamp){
        this.id=id;
        this.role=role;
        this.location=location;
        this.heading=heading;
        this.timestamp=timestamp;
    }
}
