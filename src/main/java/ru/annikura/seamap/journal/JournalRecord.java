package ru.annikura.seamap.journal;

import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.utils.ErrorOr;
import ru.annikura.seamap.data.CoordinateData;

import java.io.File;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JournalRecord {
    String date;
    String ship;
    Double lat;
    Double lng;
    String mqk;
    String comment;
    ArrayList<CoordinateData> square = null;

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
        return date;
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

        lat1 = lat1.strip();
        lat2 = lat2.strip();
        lng1 = lng1.strip();
        lng2 = lng2.strip();

        Double latd;
        Double lngd;

        if (lat1.isEmpty() && lat2.isEmpty()) {
            latd = null;
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

            latd = lat1d + lat2d / 60.0;
            if (latd > 90.0 || latd < 0.0) {
                return ErrorOr.createErr("Latitude is out of bounds 0..90.");
            }

            if (latDir.equals("S")) {
                latd *= -1;
            }
        }

        if (lng1.isEmpty() && lng2.isEmpty()) {
            lngd = null;
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

            lngd = lng1d + lng2d / 60.0;
            if (lngd > 180.0 || lngd < 0.0) {
                return ErrorOr.createErr("Longitude is out of bounds 0..180.");
            }

            if (lngDir.equals("W")) {
                lngd *= -1;
            }
        }
        return tryCreating(date, ship,
                latd == null ? "" : latd.toString(),
                lngd == null ? "" : lngd.toString(),
                mqk, comment);
    }

    public static ErrorOr<JournalRecord> tryCreating(
            @NotNull String date,
            @NotNull String ship,
            @NotNull String lat,
            @NotNull String lng,
            @NotNull String mqk,
            @NotNull String comment) {
        JournalRecord journalRecord = new JournalRecord();

        date = date.strip();
        ship = ship.strip();
        lat = lat.strip();
        lng = lng.strip();
        mqk = mqk.strip();

        if (!lat.isEmpty() || !lng.isEmpty()) {
            double latd;
            double lngd;


            lat = lat.isEmpty() ? "0.0" : lat;
            lng = lng.isEmpty() ? "0.0" : lng;

            try {
                latd = Double.parseDouble(lat);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("Latitude is expected to be a number.");
            }

            try {
                lngd = Double.parseDouble(lng);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("Longitude is expected to be a number.");
            }

            journalRecord.lat = latd;
            journalRecord.lng = lngd;
        }

        if (date.isEmpty()) {
            journalRecord.date = null;
        } else {
            String pattern = "dd.MM.yyyy HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            Date parsedDate = simpleDateFormat.parse(date, new ParsePosition(0));
            if (parsedDate == null) {
                return ErrorOr.createErr("Invalid date format. Date must match the following pattern: " + pattern);
            }
            journalRecord.date = simpleDateFormat.format(parsedDate);
        }
        journalRecord.ship = ship;

        journalRecord.mqk = mqk.isEmpty() ? null : mqk;

        if (journalRecord.mqk != null) {
            String square = mqk.substring(0, 2).toUpperCase();
            File squareFile;
            try {
                squareFile = new File(journalRecord.getClass().getResource(square).getPath());
            } catch (NullPointerException e) {
                return ErrorOr.createErr("Unknown Kriegsmarine Marinequadrat coordinate: " + square);
            }
            if (!squareFile.exists()) {
                return ErrorOr.createErr("Unknown Kriegsmarine Marinequadrat coordinate: " + square);
            }

            String subsquare = mqk.substring(2).strip();
            int subsquareNumber;
            try {
                subsquareNumber = subsquare.isEmpty() ? 0 : Integer.parseInt(subsquare);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("MQK subsquare coordinate is not a number (or absent).");
            }
            if (subsquareNumber < 0 || subsquareNumber > 10000) {
                return ErrorOr.createErr("MQK subsquare coordinate is out of range 0..9999.");
            }
            ErrorOr<List<List<String>>> squareCoordinates = DataLoader.downloadCSV(squareFile.getPath());
            if (squareCoordinates.isError()) {
                return ErrorOr.createErr(
                        "Error extracting mqk coordinate " + mqk + ": " + squareCoordinates.getError());
            }
            if (squareCoordinates.get().size() < subsquareNumber
                    || squareCoordinates.get().get(subsquareNumber).size() < 6) {
                return ErrorOr.createErr("Unknown mqk subsquare: " + subsquareNumber);
            }
            double[] mqkCoordinates = squareCoordinates.get().get(subsquareNumber)
                    .stream()
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            journalRecord.lat = journalRecord.lat == null ? mqkCoordinates[0] : journalRecord.lat;
            journalRecord.lng = journalRecord.lng == null ? mqkCoordinates[1] : journalRecord.lng;
            journalRecord.square = new ArrayList<>();

            for (int i = 1; i < mqkCoordinates.length / 2; i++) {
                journalRecord.square.add(new CoordinateData(mqkCoordinates[i * 2 + 1], mqkCoordinates[i * 2]));
            }
        }

        String[] lines = comment.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            StringBuilder builder = new StringBuilder();
            int symbolsPerLine = 32;
            for (int j = 0; j * symbolsPerLine < line.length(); j++) {
                builder.append(line, j * symbolsPerLine, Math.min((j + 1) * symbolsPerLine, line.length()));
                builder.append("\n");
            }
            lines[i] = builder.toString();
        }

        journalRecord.comment = String.join("", lines);

        return ErrorOr.createObj(journalRecord);
    }
}
