package digitawayday.domain;

public class Fixed extends Activity{

    private int startTime;
    private int endTime;

    public Fixed(String name, int length, int startTime, int endTime){
        super(name, length);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}
