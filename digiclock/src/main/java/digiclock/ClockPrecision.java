package digiclock;

public enum ClockPrecision {
    
    DECISECOND(100), SECOND(1000), MINUTE(60_000);

    private int inMS;

    private ClockPrecision(int inMS) {
        this.inMS = inMS;
    }

    public int getInMilli() {
        return inMS;
    }
}
