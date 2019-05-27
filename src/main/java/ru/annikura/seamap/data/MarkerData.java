package ru.annikura.seamap.data;

import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.journal.JournalRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MarkerData {
    public JournalRecord parent;
    public WeatherData weatherData;

    public CoordinateData coordinate;

    public String ship;
    public String date;
    public String originalCoordinates;
    public String comment;

    public List<CoordinateData> square;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public Date getDate() {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getDate(final @NotNull String format) {
        try {
            return new SimpleDateFormat(format).format(dateFormat.parse(date));
        } catch (ParseException e) {
            return null;
        }
    }
}
