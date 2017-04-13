package vincent.sprinkler;

/**
 * Value holder for the configured duration for a watering station.
 */
class WateringDuration {

    private int stationId;
    private int minutes;

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WateringDuration{");
        sb.append("stationId=").append(stationId);
        sb.append(", minutes=").append(minutes);
        sb.append('}');
        return sb.toString();
    }
}
