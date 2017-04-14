package vincent.sprinkler;

public class WateringConfiguration {

    private int port;
    private Station common;
    private Station[] stations;
    private WateringSchedule[] schedules;
    private long startDelay;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

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

    public long getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
    }
}
