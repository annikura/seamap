package ru.annikura.seamap.journal;

import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.utils.ErrorOr;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class WeatherRecord {
    final public static List<String> possibleDirections = Arrays.stream(new String[]{"N", "NNE", "NE", "ENE",
            "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW",
            "W", "WNW", "NW", "NNW"}).collect(Collectors.toList());

    String date;
    String source;
    String windDirection;
    Double windStrength;
    Double visibilityRange;

    public String getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public Double getWindStrength() {
        return windStrength;
    }

    public Double getVisibilityRange() {
        return visibilityRange;
    }

    public static ErrorOr<WeatherRecord> tryCreating(
            @NotNull String date,
            @NotNull String source,
            @NotNull String windStrength,
            @NotNull String windDirection,
            @NotNull String visibilityRange) {
        WeatherRecord weatherRecord = new WeatherRecord();

        // TODO: add stripping
        windDirection = windDirection.toUpperCase();

        if (date.isEmpty()) {
            weatherRecord.date = null;
        } else {
            String pattern = "dd.MM.yyyy HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            Date parsedDate = simpleDateFormat.parse(date, new ParsePosition(0));
            if (parsedDate == null) {
                return ErrorOr.createErr("Invalid date format. Date must match the following pattern: " + pattern);
            }
            weatherRecord.date = simpleDateFormat.format(parsedDate);
        }

        weatherRecord.source = source.isEmpty() ? null : source;

        if (!windDirection.isEmpty()) {
            boolean foundDirection = false;
            for (String direction : possibleDirections) {
                if (windDirection.equals(direction)) {
                    foundDirection = true;
                    break;
                }
            }
            if (!foundDirection) {
                return ErrorOr.createErr("Unknown wind direction: " + windDirection);
            }
            weatherRecord.windDirection = windDirection;
        } else {
            weatherRecord.windDirection = null;
        }

        if (!windStrength.isEmpty()) {
            double windStrengthd;
            try {
                windStrengthd = Double.parseDouble(windStrength);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("Wind windStrength is expected to be a float number.");
            }
            if (windStrengthd < 0) {
                return ErrorOr.createErr("Wind windStrength is expected to be a positive number.");
            }
            weatherRecord.windStrength = windStrengthd;
        } else {
            weatherRecord.windStrength = null;
        }

        if (!visibilityRange.isEmpty()) {
            double visibilityRanged;
            try {
                visibilityRanged = Double.parseDouble(visibilityRange);
            } catch (NumberFormatException e) {
                return ErrorOr.createErr("Visibility range is expected to be a float number.");
            }
            if (visibilityRanged < 0) {
                return ErrorOr.createErr("Visibility range is expected to be a positive number.");
            }
            weatherRecord.visibilityRange = visibilityRanged;
        } else {
            weatherRecord.visibilityRange = null;
        }

        return ErrorOr.createObj(weatherRecord);
    }
}
