package ru.annikura.seamap.journal;

import org.jetbrains.annotations.NotNull;
import ru.annikura.seamap.data.*;

import java.util.*;
import java.util.stream.Collectors;

public class RecordsProcesser {
    final private static List<String> colours = new ArrayList<>();
    static {
        colours.add("red");
        colours.add("blue");
        colours.add("orange");
        colours.add("green");
    }

    public static MapData processRecords(@NotNull List<JournalRecord> journalRecords,
                                         @NotNull List<WeatherRecord> weatherRecords) {
        Map<String, WeatherData> weatherMap = joinWeatherRecordsByDate(
                weatherRecords.stream()
                .filter(weatherRecord -> weatherRecord.date != null)
                .collect(Collectors.toList()));

        journalRecords = journalRecords.stream().filter(journalRecord -> journalRecord.ship != null && journalRecord.date != null &&
                (journalRecord.lat != null && journalRecord.lng != null || journalRecord.mqk != null))
                .collect(Collectors.toList());
        Map<String, ArrayList<Integer>> groupping = collectJournalRecordsByShip(journalRecords);

        double mapLatCenter = 0;
        double mapLngCenter = 0;
        double markersCounter = 0;

        MapData mapData = new MapData();
        List<ShipData> shipRecords = new ArrayList<>();

        int colourCounter = 0;

        for (String ship : groupping.keySet()) {
            ShipData shipRecord = new ShipData();
            shipRecord.shipName = ship;

            shipRecord.color = colours.get(colourCounter);
            colourCounter = colourCounter + 1 == colours.size() ? 0 : colourCounter + 1;

            List<MarkerData> markerRecords = new ArrayList<>();
            for (int id : groupping.get(ship)) {
                JournalRecord markerRecord = journalRecords.get(id);
                MarkerData markerData = new MarkerData();
                markerData.date = markerRecord.date;
                markerData.parent = markerRecord;

                if (weatherMap.containsKey(markerRecord.date)) {
                    markerData.weatherData = weatherMap.get(markerRecord.date);
                }

                markerData.ship = markerRecord.ship;
                markerData.coordinate = new CoordinateData(markerRecord.lat, markerRecord.lng);

                mapLatCenter += markerRecord.lat;
                mapLngCenter += markerRecord.lng;
                markersCounter += 1;

                markerData.originalCoordinates = markerRecord.mqk == null ? "" : markerRecord.mqk;
                markerData.square = markerRecord.square;
                markerData.comment = markerRecord.comment;
                markerRecords.add(markerData);
            }
            shipRecord.markers = markerRecords;
            shipRecords.add(shipRecord);
        }
        if (markersCounter == 0) {
            mapData.mapCenterLat = 0.0;
            mapData.mapCenterLng = 0.0;
        } else {
            mapData.mapCenterLat = mapLatCenter / markersCounter;
            mapData.mapCenterLng = mapLngCenter / markersCounter;
        }
        mapData.ships = shipRecords;
        return mapData;
    }

    private static Map<String, WeatherData> joinWeatherRecordsByDate(@NotNull List<WeatherRecord> records) {
        HashMap<String, ArrayList<WeatherData>> groupping = new HashMap<>();
        for (WeatherRecord record : records) {
            if (!groupping.containsKey(record.date)) {
                groupping.put(record.date, new ArrayList<>());
            }
            WeatherData newRecord = new WeatherData();
            newRecord.windDirection = record.windDirection;
            newRecord.windStrength = record.windStrength;
            newRecord.visibilityRange = record.visibilityRange;
            newRecord.source = record.source;

            groupping.get(record.date).add(newRecord);
        }

        HashMap<String, WeatherData> result = new HashMap<>();

        for (String key : groupping.keySet()) {
            WeatherData avgRecord = WeatherData.avg(groupping.get(key).toArray(new WeatherData[0]));
            result.put(key, avgRecord);
        }
        return result;
    }

    private static Map<String, ArrayList<Integer>> collectJournalRecordsByShip(@NotNull List<JournalRecord> records) {
        HashMap<String, ArrayList<Integer>> groupping = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            if (!groupping.containsKey(records.get(i).ship)) {
                groupping.put(records.get(i).ship, new ArrayList<>());
            }
            groupping.get(records.get(i).ship).add(i);
        }

        for (String key : groupping.keySet()) {
            groupping.get(key).sort(Comparator.comparing(o -> records.get(o).date));
        }
        return groupping;
    }
}
