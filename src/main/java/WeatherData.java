import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

public class WeatherData {
    String source;
    Double windStrength;
    String windDirection;
    Double visibilityRange;

    public CoordinateData getWindDirectionVector() {
        int position = WeatherRecord.possibleDirections.indexOf(windDirection);
        if (position < 0 || position > WeatherRecord.possibleDirections.size()) {
            return null;
        }
        return new CoordinateData(1, 0).turn(30 * position);
    }

    public static WeatherData avg(final @NotNull WeatherData... weatherData) {
        WeatherData avgRecord = new WeatherData();
        avgRecord.visibilityRange = 0.0;
        avgRecord.windStrength = 0.0;
        HashSet<String> windDirections = new HashSet<>();
        ArrayList<String> sources = new ArrayList<>();

        int visibilityOptions = 0;
        int windStrengthOptions = 0;

        for (WeatherData data : weatherData) {
            if (data.visibilityRange != null) {
                visibilityOptions += 1;
                avgRecord.visibilityRange += data.visibilityRange;
            }
            if (data.windStrength != null) {
                windStrengthOptions += 1;
                avgRecord.windStrength += data.windStrength;
            }
            if (data.windDirection != null) {
                windDirections.add(data.windDirection);
            }
            if (data.source != null) {
                sources.add(data.source);
            }
        }
        avgRecord.visibilityRange = visibilityOptions == 0 ? null : avgRecord.visibilityRange / visibilityOptions;
        avgRecord.windStrength = windStrengthOptions == 0 ? null : avgRecord.windStrength / windStrengthOptions;
        avgRecord.windDirection = String.join(", ", windDirections);
        avgRecord.source = String.join(", ", sources);

        return avgRecord;
    }
}
