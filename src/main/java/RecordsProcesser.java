import org.jetbrains.annotations.NotNull;

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
        Map<String, WeatherRecord> weatherMap = joinWeatherRecordsByDate(
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

                if (weatherMap.containsKey(markerRecord.date)) {
                    WeatherRecord requiredRecord = weatherMap.get(markerRecord.date);
                    WeatherData weatherData = new WeatherData();

                    weatherData.source = requiredRecord.source;
                    weatherData.strength = requiredRecord.windStrength;
                    weatherData.windDirection = requiredRecord.windDirection;
                    weatherData.visibility = requiredRecord.visibilityRange;

                    markerData.weatherData = weatherData;
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

    private static Map<String, WeatherRecord> joinWeatherRecordsByDate(@NotNull List<WeatherRecord> records) {
        HashMap<String, ArrayList<Integer>> groupping = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            if (!groupping.containsKey(records.get(i).date)) {
                groupping.put(records.get(i).date, new ArrayList<>());
            }
            groupping.get(records.get(i).date).add(i);
        }

        HashMap<String, WeatherRecord> result = new HashMap<>();

        for (String key : groupping.keySet()) {
            WeatherRecord avgRecord = new WeatherRecord();
            avgRecord.date = key;
            avgRecord.visibilityRange = 0.0;
            avgRecord.windStrength = 0.0;
            ArrayList<String> windDirections = new ArrayList<>();
            ArrayList<String> sources = new ArrayList<>();

            int visibilityOptions = 0;
            int windStrengthOptions = 0;

            for (Integer recordId : groupping.get(key)) {
                WeatherRecord record = records.get(recordId);
                if (record.visibilityRange != null) {
                    visibilityOptions += 1;
                    avgRecord.visibilityRange += record.visibilityRange;
                }
                if (record.windStrength != null) {
                    windStrengthOptions += 1;
                    avgRecord.windStrength += record.windStrength;
                }
                if (record.windDirection != null) {
                    windDirections.add(record.windDirection);
                }
                if (record.source != null) {
                    sources.add(record.source);
                }
            }
            avgRecord.visibilityRange = visibilityOptions == 0 ? null : avgRecord.visibilityRange / visibilityOptions;
            avgRecord.windStrength = windStrengthOptions == 0 ? null : avgRecord.windStrength / windStrengthOptions;
            avgRecord.windDirection = String.join(", ", windDirections);
            avgRecord.source = String.join(", ", sources);

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
