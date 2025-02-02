package mil.arl.gift.net.embedded.message;

import java.util.ArrayList;
import java.util.List;

public static class Scenario {
    private String id;
    private String title;
    private String description;
    private String timestamp;
    private List<Casualty> casualties;
    private List<Trainee> trainees;
    private List<ObjectOfInterest> objectsOfInterest;
    private List<RegionOfInterest> regionsOfInterest;

    public Scenario(String id, String title, String description, String timestamp, List<Casualty> casualties,
                    List<Trainee> trainees, List<ObjectOfInterest> objectsOfInterest, List<RegionOfInterest> regionsOfInterest) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.casualties = casualties;
        this.trainees = trainees;
        this.objectsOfInterest = objectsOfInterest;
        this.regionsOfInterest = regionsOfInterest;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<Casualty> getCasualties() {
        return casualties;
    }

    public List<Trainee> getTrainees() {
        return trainees;
    }

    public List<ObjectOfInterest> getObjectsOfInterest() {
        return objectsOfInterest;
    }

    public List<RegionOfInterest> getRegionsOfInterest() {
        return regionsOfInterest;
    }
}
