package com.example.myapp7;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp7.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldsReportActivity extends Activity {

    // Helper class to hold a field record.
    private static class FieldRecord {
        int month;
        int day;
        String value;
        FieldRecord(int month, int day, String value) {
            this.month = month;
            this.day = day;
            this.value = value;
        }
        // Returns true if this record is later than another.
        boolean isLaterThan(FieldRecord other) {
            if (this.month > other.month) return true;
            if (this.month == other.month && this.day > other.day) return true;
            return false;
        }
        // Returns the last updated date as a string in MM-DD format.
        String getDateString() {
            return String.format("%02d-%02d", month, day);
        }
    }

    private TableLayout reportTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fields_report);

        reportTable = findViewById(R.id.report_table);

        List<String> fieldNames = getFieldsList();
        if (fieldNames.isEmpty()) {
            Toast.makeText(this, "No fields found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get latest record for each field.
        Map<String, FieldRecord> latestRecords = getLatestFieldRecords();

        buildReportTable(fieldNames, latestRecords);
    }

    // Reads fields.txt and returns the list of field names.
    private List<String> getFieldsList() {
        List<String> list = new ArrayList<>();
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File fieldsFile = new File(appFolder, "fields.txt");
            if (!fieldsFile.exists()) return list;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fieldsFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    list.add(line.trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Reads fieldsData.txt and returns a map from field name to its latest FieldRecord.
    // Each block in fieldsData.txt has a header line "monthX-dayY", followed by lines "field:value", then a separator line "---".
    private Map<String, FieldRecord> getLatestFieldRecords() {
        Map<String, FieldRecord> map = new HashMap<>();
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File fieldsDataFile = new File(appFolder, "fieldsData.txt");
            if (!fieldsDataFile.exists()) return map;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fieldsDataFile)));
            String line;
            String currentHeader = null;
            int currentMonth = 0, currentDay = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equals("---")) {
                    currentHeader = null;
                    continue;
                }
                if (currentHeader == null) {
                    // This is the header.
                    currentHeader = line;
                    int[] md = parseHeader(currentHeader);
                    currentMonth = md[0];
                    currentDay = md[1];
                } else {
                    // Expect a line "field:value"
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        String field = parts[0].trim();
                        String value = parts[1].trim();
                        FieldRecord rec = new FieldRecord(currentMonth, currentDay, value);
                        if (!map.containsKey(field) || rec.isLaterThan(map.get(field))) {
                            map.put(field, rec);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    // Parses header of the form "monthX-dayY" into an array [month, day].
    private int[] parseHeader(String header) {
        header = header.toLowerCase().replace("month", "").replace("day", "").trim();
        String[] parts = header.split("-");
        int month = 0, day = 0;
        try {
            month = Integer.parseInt(parts[0].trim());
            day = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return new int[]{month, day};
    }

    // Builds the report table with a header row and a row per field.
    private void buildReportTable(List<String> fieldNames, Map<String, FieldRecord> records) {
        reportTable.removeAllViews();
        // Header row
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.DKGRAY);
        String[] headers = {"Field", "Last Value", "Last Updated"};
        for (String h : headers) {
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setTextColor(Color.YELLOW);
            tv.setPadding(16, 16, 16, 16);
            headerRow.addView(tv);
        }
        reportTable.addView(headerRow);
        
        // Data rows
        for (String field : fieldNames) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(Color.BLACK);
            TextView tvField = new TextView(this);
            tvField.setText(field);
            tvField.setTextColor(Color.WHITE);
            tvField.setPadding(16, 16, 16, 16);
            row.addView(tvField);
            
            FieldRecord rec = records.get(field);
            String lastValue = rec != null ? rec.value : "";
            String lastUpdated = rec != null ? rec.getDateString() : "";
            
            TextView tvValue = new TextView(this);
            tvValue.setText(lastValue);
            tvValue.setTextColor(Color.WHITE);
            tvValue.setPadding(16, 16, 16, 16);
            row.addView(tvValue);
            
            TextView tvDate = new TextView(this);
            tvDate.setText(lastUpdated);
            tvDate.setTextColor(Color.WHITE);
            tvDate.setPadding(16, 16, 16, 16);
            row.addView(tvDate);
            
            reportTable.addView(row);
        }
    }
}
