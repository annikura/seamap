import com.opencsv.CSVReader;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataLoader {
    public static ErrorOr<List<List<String>>> downloadCSV(@NotNull String filename) {
        List<List<String>> records = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(filename))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            return ErrorOr.createErr(e.getMessage());
        }
        return ErrorOr.createObj(records);
    }

    public static ErrorOr<List<JournalRecord>> downloadRecords(@NotNull String filename) {
        ErrorOr<List<List<String>>> csv = downloadCSV(filename);
        if (csv.isError()) {
            return ErrorOr.createErr(csv.getError());
        }
        final int NUMBER_OF_RECORDS = 10;
        List<List<String>> stringRecords = csv.get();
        List<JournalRecord> records = new ArrayList<>();
        for (int i = 0; i < stringRecords.size(); i++) {
            List<String> stringRecord = stringRecords.get(i);
            if (stringRecord.size() != NUMBER_OF_RECORDS) {
                return ErrorOr.createErr(
                        "Upload failure: expected " + NUMBER_OF_RECORDS
                                + " records in every line, found " + stringRecord.size() + " in line " + i);
            }
            ErrorOr<JournalRecord> newRecord = JournalRecord.tryCreating(
                    stringRecord.get(0),
                    stringRecord.get(1),
                    stringRecord.get(2),
                    stringRecord.get(3),
                    stringRecord.get(4),
                    stringRecord.get(5),
                    stringRecord.get(6),
                    stringRecord.get(7),
                    stringRecord.get(8),
                    stringRecord.get(9));
            if (newRecord.isError()) {
                return ErrorOr.createErr("Upload failure: validation failure in line " + i + ": " + newRecord.getError());
            }
            records.add(newRecord.get());
        }
        return ErrorOr.createObj(records);
    }

}
