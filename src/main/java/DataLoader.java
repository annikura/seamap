import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ErrorOr<Void> uploadCSV(@NotNull String filename, @NotNull List<List<String>> data) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename));
            for (List<String> line : data) {
                writer.writeNext(line.toArray(new String[0]));
            }
            writer.close();
        } catch (IOException e) {
            return ErrorOr.createErr(e.getMessage());
        }
        return ErrorOr.createObj(null);
    }

    public static ErrorOr<Void> uploadJournalRecords(@NotNull String filename, @NotNull List<JournalRecord> data) {
        return uploadCSV(filename, data.stream().map(journalRecord -> {
            List<String> line = new ArrayList<>();
            line.add(journalRecord.date);
            line.add(journalRecord.ship);
            line.add(journalRecord.lat.toString());
            line.add(journalRecord.lng.toString());
            line.add(journalRecord.mqk);
            line.add(journalRecord.comment);
            return line;
        }).collect(Collectors.toList()));
    }


    public static ErrorOr<List<JournalRecord>> downloadRecords(@NotNull String filename) {
        ErrorOr<List<List<String>>> csv = downloadCSV(filename);
        if (csv.isError()) {
            return ErrorOr.createErr(csv.getError());
        }
        final int NUMBER_OF_RECORDS = 6;
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
                    stringRecord.get(5));
            if (newRecord.isError()) {
                return ErrorOr.createErr("Upload failure: validation failure in line " + i + ": " + newRecord.getError());
            }
            records.add(newRecord.get());
        }
        return ErrorOr.createObj(records);
    }

}
