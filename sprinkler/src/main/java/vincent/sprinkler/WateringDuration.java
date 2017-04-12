package vincent.sprinkler;

/**
 * Value holder for the configured duration for a watering station.
 */
class WateringDuration {

    private int stationId;
    private int minutes;

    WateringDuration(int stationId, int minutes) {
        this.stationId = stationId;
        this.minutes = minutes;
    }

    int getStationId() {
        return stationId;
    }

    int getMinutes() {
        return minutes;
    }
}
