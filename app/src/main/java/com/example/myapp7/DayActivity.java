package com.example.myapp7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapp7.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DayActivity extends Activity {
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 222;
    private static final int REQUEST_WRITE_STORAGE = 111;

    private LinearLayout containerThings;
    private EditText noteField;
    private int currentMonth, currentDay;
    // Our custom month lengths (total 365 days)
    private final int[] monthDaysArray = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        // Expect currentMonth (1-indexed) and currentDay from intent.
        currentMonth = getIntent().getIntExtra("month", 0);
        currentDay = getIntent().getIntExtra("day", 0);

        TextView dayHeader = findViewById(R.id.day_header);
        dayHeader.setText("Month " + currentMonth + " - Day " + currentDay);

        containerThings = findViewById(R.id.container_things);
        noteField = findViewById(R.id.note_field);
        Button btnSaveNote = findViewById(R.id.button_save);
        Button btnFields = findViewById(R.id.button_fields); // New "Fields" button in day page

        // Check external storage permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please grant all files access permission", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }
        }

        // Load the list of things (for the Add/Off list) from things.txt.
        loadThingsList();

        // Save note button: writes to day_notes.txt.
        btnSaveNote.setOnClickListener(v -> {
            String note = noteField.getText().toString().trim();
            if (note.isEmpty()) {
                Toast.makeText(DayActivity.this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            String entry = "Month " + currentMonth + " - Day " + currentDay + "\n" + note + "\n---\n";
            try {
                File rootDir = Environment.getExternalStorageDirectory();
                String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
                File appFolder = new File(rootDir, appLabel);
                if (!appFolder.exists() && !appFolder.mkdirs()) {
                    Toast.makeText(DayActivity.this, "Could not create folder", Toast.LENGTH_SHORT).show();
                    return;
                }
                File dayNotesFile = new File(appFolder, "day_notes.txt");
                FileOutputStream fos = new FileOutputStream(dayNotesFile, true);
                fos.write(entry.getBytes());
                fos.close();
                Toast.makeText(DayActivity.this, "Note saved.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(DayActivity.this, "Error saving note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        // Fields button opens the Fields dialog.
        btnFields.setOnClickListener(v -> {
            showFieldsDialog();
        });
    }

    // Computes day-of-year (1 to 365) based on custom month lengths.
    private int getDayOfYear() {
        int doy = 0;
        for (int i = 0; i < currentMonth - 1; i++) {
            doy += monthDaysArray[i];
        }
        doy += currentDay;
        return doy;
    }

    // Loads things from things.txt and populates containerThings.
    private void loadThingsList() {
        containerThings.removeAllViews();
        List<String> things = getThingsList();
        for (String thing : things) {
            if (!thing.trim().isEmpty()) {
                addThingItem(thing);
            }
        }
    }

    // Reads things.txt and returns a list of things.
    private List<String> getThingsList() {
        List<String> things = new ArrayList<>();
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File thingsFile = new File(appFolder, "things.txt");
            if (!thingsFile.exists()) return things;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(thingsFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    things.add(line.trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return things;
    }

    // Adds one row in containerThings for a given thing with "Add" and toggle "Off/On" buttons.
    private void addThingItem(String thingText) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(8, 8, 8, 8);

        TextView thingView = new TextView(this);
        thingView.setText(thingText);
        thingView.setTextColor(Color.WHITE);
        thingView.setTextSize(18);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        thingView.setLayoutParams(params);

        Button btnAdd = new Button(this);
        btnAdd.setText("Add");
        btnAdd.setTextSize(14);

        Button btnOff = new Button(this);
        btnOff.setText("Off");
        btnOff.setTextSize(14);

        btnOff.setOnClickListener(v -> {
            if (btnOff.getText().toString().equals("Off")) {
                thingView.setAlpha(0.5f);
                btnAdd.setEnabled(false);
                btnOff.setText("On");
            } else {
                thingView.setAlpha(1.0f);
                btnAdd.setEnabled(true);
                btnOff.setText("Off");
            }
        });

        btnAdd.setOnClickListener(v -> {
            showStatisticsDialog(thingText);
        });

        itemLayout.addView(thingView);
        itemLayout.addView(btnAdd);
        itemLayout.addView(btnOff);
        containerThings.addView(itemLayout);
    }

    // Reads the statistics database from statistics.txt as a 2D array.
    // The table has 365 rows and (# of things * 3) columns.
    private String[][] readStatisticsDatabase() {
        List<String[]> rows = new ArrayList<>();
        int numCols = getThingsList().size() * 3;
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File statsFile = new File(appFolder, "statistics.txt");
            if (!statsFile.exists()) {
                String[][] table = new String[365][numCols];
                for (int i = 0; i < 365; i++) {
                    for (int j = 0; j < numCols; j++) {
                        table[i][j] = "";
                    }
                }
                return table;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(statsFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                rows.add(cols);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int totalCols = getThingsList().size() * 3;
        String[][] table = new String[365][totalCols];
        for (int i = 0; i < 365; i++) {
            if (i < rows.size()) {
                String[] row = rows.get(i);
                for (int j = 0; j < totalCols; j++) {
                    table[i][j] = j < row.length ? row[j] : "";
                }
            } else {
                for (int j = 0; j < totalCols; j++) {
                    table[i][j] = "";
                }
            }
        }
        return table;
    }

    // Writes the 2D statistics database back to statistics.txt.
    private void writeStatisticsDatabase(String[][] table) {
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            if (!appFolder.exists() && !appFolder.mkdirs()) return;
            File statsFile = new File(appFolder, "statistics.txt");
            FileOutputStream fos = new FileOutputStream(statsFile, false);
            for (int i = 0; i < 365; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < table[i].length; j++) {
                    sb.append(table[i][j]);
                    if (j < table[i].length - 1) sb.append(",");
                }
                sb.append("\n");
                fos.write(sb.toString().getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Parses a time string "H:mm" or "HH:mm" into minutes.
    private int parseTimeToMinutes(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length != 2) return 0;
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return h * 60 + m;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Retrieves the statistics for a given thing for the current day.
    // Returns an array of 3 Integers (in minutes) or null if no record.
    // Empty cells are considered not set (null).
    private Integer[] getStatisticsForThing(String thingText) {
        List<String> things = getThingsList();
        int thingIndex = things.indexOf(thingText);
        if (thingIndex < 0) return null;
        String[][] table = readStatisticsDatabase();
        int row = getDayOfYear() - 1;
        int colStart = thingIndex * 3;
        String startCell = table[row][colStart];
        String endCell = table[row][colStart + 1];
        String durCell = table[row][colStart + 2];
        Integer startTotal = startCell.isEmpty() ? null : parseTimeToMinutes(startCell);
        Integer endTotal = endCell.isEmpty() ? null : parseTimeToMinutes(endCell);
        Integer durationTotal = durCell.isEmpty() ? null : parseTimeToMinutes(durCell);
        return new Integer[]{startTotal, endTotal, durationTotal};
    }

    // Updates the statistics for the given thing on the current day.
    private void updateStatisticsForThing(String thingText, int startTotal, int endTotal, int durationTotal) {
        List<String> things = getThingsList();
        int thingIndex = things.indexOf(thingText);
        if (thingIndex < 0) return;
        String[][] table = readStatisticsDatabase();
        int row = getDayOfYear() - 1;
        String startStr = String.format("%02d:%02d", startTotal / 60, startTotal % 60);
        String endStr = String.format("%02d:%02d", endTotal / 60, endTotal % 60);
        String durStr = String.format("%d:%02d", durationTotal / 60, durationTotal % 60);
        int colStart = thingIndex * 3;
        table[row][colStart] = startStr;
        table[row][colStart + 1] = endStr;
        table[row][colStart + 2] = durStr;
        writeStatisticsDatabase(table);
    }

    // Displays the statistics dialog for a given thing.
    // If a record exists, spinners are pre-populated and disabled.
    // Tapping "Change" clears the fields and enables editing.
    private void showStatisticsDialog(String thingText) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_statistics, null);
        final Spinner spinnerStartHour = dialogView.findViewById(R.id.spinner_start_hour);
        final Spinner spinnerStartMinute = dialogView.findViewById(R.id.spinner_start_minute);
        final Spinner spinnerEndHour = dialogView.findViewById(R.id.spinner_end_hour);
        final Spinner spinnerEndMinute = dialogView.findViewById(R.id.spinner_end_minute);
        final Spinner spinnerDurationHour = dialogView.findViewById(R.id.spinner_duration_hour);
        final Spinner spinnerDurationMinute = dialogView.findViewById(R.id.spinner_duration_minute);
        Button buttonSaveStats = dialogView.findViewById(R.id.button_save_stats);
        Button buttonChangeStats = dialogView.findViewById(R.id.button_change_stats);

        // Build lists with an initial empty option.
        List<String> hoursList = new ArrayList<>();
        hoursList.add(""); // empty option
        for (int i = 0; i < 24; i++) {
            hoursList.add(String.valueOf(i));
        }
        List<String> minutesList = new ArrayList<>();
        minutesList.add(""); // empty option
        minutesList.add("0");
        minutesList.add("15");
        minutesList.add("30");
        minutesList.add("45");

        ArrayAdapter<String> adapterHours = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hoursList);
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterMinutes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, minutesList);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerStartHour.setAdapter(adapterHours);
        spinnerEndHour.setAdapter(adapterHours);
        spinnerDurationHour.setAdapter(adapterHours);
        spinnerStartMinute.setAdapter(adapterMinutes);
        spinnerEndMinute.setAdapter(adapterMinutes);
        spinnerDurationMinute.setAdapter(adapterMinutes);

        // Prepopulate spinners with saved record if exists.
        Integer[] stats = getStatisticsForThing(thingText);
        if (stats != null) {
            if (stats[0] != null) {
                int h = stats[0] / 60;
                int m = stats[0] % 60;
                spinnerStartHour.setSelection(h + 1);
                int sIdx = minutesList.indexOf(String.valueOf(m));
                spinnerStartMinute.setSelection(sIdx >= 0 ? sIdx : 0);
            } else {
                spinnerStartHour.setSelection(0);
                spinnerStartMinute.setSelection(0);
            }
            if (stats[1] != null) {
                int h = stats[1] / 60;
                int m = stats[1] % 60;
                spinnerEndHour.setSelection(h + 1);
                int eIdx = minutesList.indexOf(String.valueOf(m));
                spinnerEndMinute.setSelection(eIdx >= 0 ? eIdx : 0);
            } else {
                spinnerEndHour.setSelection(0);
                spinnerEndMinute.setSelection(0);
            }
            if (stats[2] != null) {
                int h = stats[2] / 60;
                int m = stats[2] % 60;
                spinnerDurationHour.setSelection(h + 1);
                int dIdx = minutesList.indexOf(String.valueOf(m));
                spinnerDurationMinute.setSelection(dIdx >= 0 ? dIdx : 0);
            } else {
                spinnerDurationHour.setSelection(0);
                spinnerDurationMinute.setSelection(0);
            }
            // Disable spinners initially.
            spinnerStartHour.setEnabled(false);
            spinnerStartMinute.setEnabled(false);
            spinnerEndHour.setEnabled(false);
            spinnerEndMinute.setEnabled(false);
            spinnerDurationHour.setEnabled(false);
            spinnerDurationMinute.setEnabled(false);
        } else {
            spinnerStartHour.setSelection(0);
            spinnerStartMinute.setSelection(0);
            spinnerEndHour.setSelection(0);
            spinnerEndMinute.setSelection(0);
            spinnerDurationHour.setSelection(0);
            spinnerDurationMinute.setSelection(0);
        }

        // Flags to track if a group is set.
        final boolean[] startSet = { !spinnerStartHour.getSelectedItem().toString().isEmpty() &&
                                       !spinnerStartMinute.getSelectedItem().toString().isEmpty() };
        final boolean[] endSet = { !spinnerEndHour.getSelectedItem().toString().isEmpty() &&
                                     !spinnerEndMinute.getSelectedItem().toString().isEmpty() };
        final boolean[] durationSet = { !spinnerDurationHour.getSelectedItem().toString().isEmpty() &&
                                        !spinnerDurationMinute.getSelectedItem().toString().isEmpty() };

        Runnable updateFlags = () -> {
            startSet[0] = !spinnerStartHour.getSelectedItem().toString().isEmpty()
                    && !spinnerStartMinute.getSelectedItem().toString().isEmpty();
            endSet[0] = !spinnerEndHour.getSelectedItem().toString().isEmpty()
                    && !spinnerEndMinute.getSelectedItem().toString().isEmpty();
            durationSet[0] = !spinnerDurationHour.getSelectedItem().toString().isEmpty()
                    && !spinnerDurationMinute.getSelectedItem().toString().isEmpty();
        };

        final Runnable updateComputed = new Runnable() {
            @Override
            public void run() {
                updateFlags.run();
                int count = (startSet[0] ? 1 : 0) + (endSet[0] ? 1 : 0) + (durationSet[0] ? 1 : 0);
                if (count != 2) return;
                if (startSet[0] && endSet[0] && !durationSet[0]) {
                    int startH = Integer.parseInt(spinnerStartHour.getSelectedItem().toString());
                    int startM = Integer.parseInt(spinnerStartMinute.getSelectedItem().toString());
                    int endH = Integer.parseInt(spinnerEndHour.getSelectedItem().toString());
                    int endM = Integer.parseInt(spinnerEndMinute.getSelectedItem().toString());
                    int computedDur = (endH * 60 + endM) - (startH * 60 + startM);
                    if (computedDur < 15) computedDur = 15;
                    int newDurH = computedDur / 60;
                    int newDurM = computedDur % 60;
                    spinnerDurationHour.setSelection(newDurH + 1);
                    int idx = minutesList.indexOf(String.valueOf(newDurM));
                    spinnerDurationMinute.setSelection(idx >= 0 ? idx : 0);
                } else if (startSet[0] && durationSet[0] && !endSet[0]) {
                    int startH = Integer.parseInt(spinnerStartHour.getSelectedItem().toString());
                    int startM = Integer.parseInt(spinnerStartMinute.getSelectedItem().toString());
                    int durH = Integer.parseInt(spinnerDurationHour.getSelectedItem().toString());
                    int durM = Integer.parseInt(spinnerDurationMinute.getSelectedItem().toString());
                    int computedEnd = (startH * 60 + startM) + (durH * 60 + durM);
                    if (computedEnd >= 24 * 60) computedEnd = 24 * 60 - 15;
                    int newEndH = computedEnd / 60;
                    int newEndM = computedEnd % 60;
                    spinnerEndHour.setSelection(newEndH + 1);
                    int idx = minutesList.indexOf(String.valueOf(newEndM));
                    spinnerEndMinute.setSelection(idx >= 0 ? idx : 0);
                } else if (endSet[0] && durationSet[0] && !startSet[0]) {
                    int endH = Integer.parseInt(spinnerEndHour.getSelectedItem().toString());
                    int endM = Integer.parseInt(spinnerEndMinute.getSelectedItem().toString());
                    int durH = Integer.parseInt(spinnerDurationHour.getSelectedItem().toString());
                    int durM = Integer.parseInt(spinnerDurationMinute.getSelectedItem().toString());
                    int computedStart = (endH * 60 + endM) - (durH * 60 + durM);
                    if (computedStart < 0) computedStart = 0;
                    int newStartH = computedStart / 60;
                    int newStartM = computedStart % 60;
                    spinnerStartHour.setSelection(newStartH + 1);
                    int idx = minutesList.indexOf(String.valueOf(newStartM));
                    spinnerStartMinute.setSelection(idx >= 0 ? idx : 0);
                }
            }
        };

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFlags.run();
                updateComputed.run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerStartHour.setOnItemSelectedListener(spinnerListener);
        spinnerStartMinute.setOnItemSelectedListener(spinnerListener);
        spinnerEndHour.setOnItemSelectedListener(spinnerListener);
        spinnerEndMinute.setOnItemSelectedListener(spinnerListener);
        spinnerDurationHour.setOnItemSelectedListener(spinnerListener);
        spinnerDurationMinute.setOnItemSelectedListener(spinnerListener);

        // "Change" button clears fields and enables editing.
        buttonChangeStats.setOnClickListener(v -> {
            spinnerStartHour.setSelection(0);
            spinnerStartMinute.setSelection(0);
            spinnerEndHour.setSelection(0);
            spinnerEndMinute.setSelection(0);
            spinnerDurationHour.setSelection(0);
            spinnerDurationMinute.setSelection(0);
            spinnerStartHour.setEnabled(true);
            spinnerStartMinute.setEnabled(true);
            spinnerEndHour.setEnabled(true);
            spinnerEndMinute.setEnabled(true);
            spinnerDurationHour.setEnabled(true);
            spinnerDurationMinute.setEnabled(true);
            updateFlags.run();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Enter Statistics for: " + thingText);
        final AlertDialog dialog = builder.create();
        dialog.show();

        buttonSaveStats.setOnClickListener(v -> {
            updateFlags.run();
            int count = (startSet[0] ? 1 : 0) + (endSet[0] ? 1 : 0) + (durationSet[0] ? 1 : 0);
            if (count < 2) {
                Toast.makeText(DayActivity.this, "Please set at least two fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (count == 2) {
                updateComputed.run();
                updateFlags.run();
            }
            if (!(startSet[0] && endSet[0] && durationSet[0])) {
                Toast.makeText(DayActivity.this, "Incomplete data; not saved.", Toast.LENGTH_SHORT).show();
                return;
            }
            int startH = Integer.parseInt(spinnerStartHour.getSelectedItem().toString());
            int startM = Integer.parseInt(spinnerStartMinute.getSelectedItem().toString());
            int endH = Integer.parseInt(spinnerEndHour.getSelectedItem().toString());
            int endM = Integer.parseInt(spinnerEndMinute.getSelectedItem().toString());
            int durH = Integer.parseInt(spinnerDurationHour.getSelectedItem().toString());
            int durM = Integer.parseInt(spinnerDurationMinute.getSelectedItem().toString());
            int startTotal = startH * 60 + startM;
            int endTotal = endH * 60 + endM;
            int durationTotal = durH * 60 + durM;
            updateStatisticsForThing(thingText, startTotal, endTotal, durationTotal);
            Toast.makeText(DayActivity.this, "Statistics saved.", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
    }

    private void showFieldsDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_fields, null);
        LinearLayout container = dialogView.findViewById(R.id.dialog_fields_container);
        Button btnSaveFields = dialogView.findViewById(R.id.btn_save_fields);
        
        // Load field names from fields.txt.
        List<String> fields = getFieldsForFields();
        // Attempt to load fields data for today.
        String record = getFieldsDataForToday();
        
        final List<FieldEntry> fieldEntries = new ArrayList<>();
        for (String field : fields) {
            String value = "";
            if (!record.isEmpty()) {
                String[] lines = record.split("\n");
                for (String line : lines) {
                    if (line.startsWith(field + ":")) {
                        value = line.substring((field + ":").length()).trim();
                        break;
                    }
                }
            }
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView tvField = new TextView(this);
            tvField.setText(field + ":");
            tvField.setTextColor(Color.WHITE);
            tvField.setTextSize(18);
            EditText etValue = new EditText(this);
            etValue.setHint("Enter value");
            etValue.setText(value);
            etValue.setTextColor(Color.YELLOW);
            etValue.setBackgroundColor(Color.DKGRAY);
            row.addView(tvField);
            row.addView(etValue);
            container.addView(row);
            fieldEntries.add(new FieldEntry(field, etValue));
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Enter Fields Data");
        AlertDialog dialog = builder.create();
        dialog.show();
        
        btnSaveFields.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            sb.append("month").append(currentMonth).append("-day").append(currentDay).append("\n");
            for (FieldEntry fe : fieldEntries) {
                String val = fe.editText.getText().toString().trim();
                sb.append(fe.fieldName).append(":").append(val).append("\n");
            }
            sb.append("---\n");
            try {
                File rootDir = Environment.getExternalStorageDirectory();
                String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
                File appFolder = new File(rootDir, appLabel);
                if (!appFolder.exists() && !appFolder.mkdirs()) {
                    Toast.makeText(DayActivity.this, "Could not create folder", Toast.LENGTH_SHORT).show();
                    return;
                }
                File fieldsDataFile = new File(appFolder, "fieldsData.txt");
                FileOutputStream fos = new FileOutputStream(fieldsDataFile, true);
                fos.write(sb.toString().getBytes());
                fos.close();
                Toast.makeText(DayActivity.this, "Fields data saved.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(DayActivity.this, "Error saving fields data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper class for fields dialog.
    private static class FieldEntry {
        String fieldName;
        EditText editText;
        FieldEntry(String fieldName, EditText editText) {
            this.fieldName = fieldName;
            this.editText = editText;
        }
    }

    // Reads fields.txt for use in the day page fields dialog.
    private List<String> getFieldsForFields() {
        List<String> list = new ArrayList<>();
        try {
            File rootDir = Environment.getExternalStorageDirectory();
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

    // Reads fieldsData.txt and returns the record for today (if any), based on header "monthX-dayY".
    private String getFieldsDataForToday() {
        String targetHeader = "month" + currentMonth + "-day" + currentDay;
        String record = "";
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File fieldsDataFile = new File(appFolder, "fieldsData.txt");
            if (!fieldsDataFile.exists()) return "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fieldsDataFile)));
            String line;
            boolean found = false;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(targetHeader)) {
                    found = true;
                    sb = new StringBuilder();
                    sb.append(line).append("\n");
                } else if (line.trim().equals("---") && found) {
                    sb.append(line);
                    record = sb.toString();
                    found = false;
                } else if (found) {
                    sb.append(line).append("\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return record;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "External storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
