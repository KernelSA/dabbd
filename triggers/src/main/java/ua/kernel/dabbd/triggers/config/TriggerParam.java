package ua.kernel.dabbd.triggers.config;

public enum TriggerParam {
    LOST_TRACKER_TIMEOUT_SECONDS("lost.tracker.timeout.seconds", 10 * 60),
    LOST_TRACKER_SPEED_THRESHOLD("lost.tracker.speed.threshold", 5),
    LOST_TRACKER_POWER_THRESHOLD("lost.tracker.power.threshold", 11),
    DATA_GAP_TIMEGAP_SECONDS("data.gap.timegap.seconds", 3 * 60),
    DATA_GAP_DISTANCE_METERS("data.gap.distance.meters", 500),
    DATA_GAP_SPEED_KMH("data.gap.speed.kmh", 10),
    FUEL_LEVEL_SPIKE("fuel.level.spike.percent", 10),
    FUEL_LEVEL_WIDOW_SIZE("fuel.level.widow.size", 6),
    POWER_LOST_WINDOW_SIZE("power.lost.window.size", 5),
    POWER_LOST_ZERO_POWER_COUNT("power.lost.zero.power.events.count", 2),
    POWER_LOST_SPEED_LIMIT("power.lost.speed.threshold", 2),

    PARKING_TIME_WINDOW_SECONDS("parking.time.window.seconds", 300),
    PARKING_SPEED_THRESHOLD_KMH("parking.speed.threshold.kmh", 2),
    PARKING_DISTANCE_THRESHOLD_METERS("parking.distance.threshold.meters", 10);

    public static final int LOST_SIGNAL_EVALUATION_PERIOD_SECONDS = 60;
    public static final int PARKING_TIMEOUT_EVALUATION_PERIOD_SECONDS = 60;

    TriggerParam(String key, int defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    private String key;
    private int defaultValue;

    public String key() {
        return key;
    }

    public int defaultValue() {
        return defaultValue;
    }
}
