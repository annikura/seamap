import org.jetbrains.annotations.NotNull;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
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

    public static ErrorOr<JournalRecord> tryCreating(
            @NotNull String date,
            @NotNull String ship,
            @NotNull String lat1, @NotNull String lat2, @NotNull String latDir,
            @NotNull String lng1, @NotNull String lng2, @NotNull String lngDir,
            @NotNull String mqk,
            @NotNull String comment) {
        JournalRecord journalRecord = new JournalRecord();

        date = date.strip();
        ship = ship.strip();
        lat1 = lat1.strip();
        lat2 = lat2.strip();
        lng1 = lng1.strip();
        lng2 = lng2.strip();
        mqk = mqk.strip();

        String pattern = "dd.MM.yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date parsedDate = simpleDateFormat.parse(date, new ParsePosition(0));
        if (parsedDate == null) {
            return ErrorOr.createErr("Invalid date format. Date must match the following pattern: " + pattern);
        }
        journalRecord.date = parsedDate;
        journalRecord.ship = ship;

        if (lat1.isEmpty() && lat2.isEmpty()) {
            journalRecord.lat = null;
        } else {
            lat1 = lat1.isEmpty() ? "0.0" : lat1;
            lat2 = lat2.isEmpty() ? "0.0" : lat2;

            double lat1d, lat2d;
            try {
                lat1d = Double.parseDouble(lat1);
                lat2d = Double.parseDouble(lat2);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("Latitude is expected to be a number.");
            }

            if (!latDir.equals("N") && !latDir.equals("S")) {
                return ErrorOr.createErr("Latitude pole must be either 'N' or 'S', found " + latDir);
            }

            journalRecord.lat = lat1d + lat2d / 60.0;
            if (journalRecord.lat > 90.0 || journalRecord.lat < 0.0) {
                return ErrorOr.createErr("Latitude is out of bounds 0..90.");
            }

            if (latDir.equals("S")) {
                journalRecord.lat *= -1;
            }
        }

        if (lng1.isEmpty() && lng2.isEmpty()) {
            journalRecord.lng = null;
        } else {
            lng1 = lng1.isEmpty() ? "0.0" : lng1;
            lng2 = lng2.isEmpty() ? "0.0" : lng2;

            double lng1d, lng2d;
            try {
                lng1d = Double.parseDouble(lng1);
                lng2d = Double.parseDouble(lng2);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("Longitude is expected to be a number.");
            }

            if (!lngDir.equals("W") && !lngDir.equals("E")) {
                return ErrorOr.createErr("Longitude polarity must be either 'W' or 'E', found " + lngDir);
            }

            journalRecord.lng = lng1d + lng2d / 60.0;
            if (journalRecord.lng > 180.0 || journalRecord.lng < 0.0) {
                return ErrorOr.createErr("Longitude is out of bounds 0..180.");
            }

            if (lngDir.equals("W")) {
                journalRecord.lng *= -1;
            }
        }

        journalRecord.mqk = mqk.isEmpty() ? null : mqk;

        // TODO: valudate mqk
        if (journalRecord.mqk == null && journalRecord.lat == null && journalRecord.lng == null) {
            return ErrorOr.createErr("Either longitude/latitude or mqk must be specified.");
        }

        journalRecord.comment = comment;
        return ErrorOr.createObj(journalRecord);
    }
}
