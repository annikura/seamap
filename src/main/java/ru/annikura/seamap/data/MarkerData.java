package ru.annikura.seamap.data;

import java.util.List;

public class MarkerData {
    public WeatherData weatherData;

    public CoordinateData coordinate;

    public String ship;
    public String date;
    public String originalCoordinates;
    public String comment;

    public List<CoordinateData> square;
}
