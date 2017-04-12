package vincent.sprinkler;

class Station {
    private int id;
    private final int pin;
    private final String description;

    Station(int id, int pin, String description) {
        this.id = id;
        this.pin = pin;
        this.description = description;
    }

    int getId() {
        return id;
    }

    int getPin() {

        return pin;
    }

    String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Station{");
        sb.append("id=").append(id);
        sb.append(", pin=").append(pin);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
