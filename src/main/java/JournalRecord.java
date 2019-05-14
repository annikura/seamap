import java.util.Date;

public class JournalRecord {
    Date date;
    String ship;
    Double lat;
    Double lng;
    String mqk;
    String comment;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date.toString();
    }

    public String getMqk() {
        return mqk;
    }

    public String getShip() {
        return ship;
    }
}
