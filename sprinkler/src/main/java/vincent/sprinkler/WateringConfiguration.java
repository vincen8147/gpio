package vincent.sprinkler;

public class WateringConfiguration {

    private Station common;
    private Station[] stations;
    private WateringSchedule[] schedules;

    public Station getCommon() {
        return common;
    }

    public void setCommon(Station common) {
        this.common = common;
    }

    public Station[] getStations() {
        return stations;
    }

    public void setStations(Station[] stations) {
        this.stations = stations;
    }

    public WateringSchedule[] getSchedules() {
        return schedules;
    }

    public void setSchedules(WateringSchedule[] schedules) {
        this.schedules = schedules;
    }
}
