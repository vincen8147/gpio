package vincent.sprinkler;

public class WateringSchedule {

    private String description;
    private String startSchedule;
    private WateringDuration[] durations;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartSchedule() {
        return startSchedule;
    }

    public void setStartSchedule(String startSchedule) {
        this.startSchedule = startSchedule;
    }

    public WateringDuration[] getDurations() {
        return durations;
    }

    public void setDurations(WateringDuration[] durations) {
        this.durations = durations;
    }
}
