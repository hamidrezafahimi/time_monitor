package com.example.myapp7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp7.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends Activity {
    // Top half: Calendar
    private int currentMonth = 1; // initial month
    private TableLayout calendarTable;
    private TextView monthLabel;
    private final int[] monthDays = {31,31,31,31,31,31,30,30,30,30,30,29};
    // Selected days (store day-of-year)
    private Integer selectedDate1 = null;
    private Integer selectedDate2 = null;
    
    // Bottom half: Things selection (via multi-choice dialog)
    private Button btnSelectThings;
    private TextView tvSelectedThings;
    private List<String> allThings; 
    private boolean[] selectedThingsFlags;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        
        // Top half views
        calendarTable = findViewById(R.id.report_calendar_table);
        monthLabel = findViewById(R.id.report_month_label);
        Button btnPrev = findViewById(R.id.report_button_prev);
        Button btnNext = findViewById(R.id.report_button_next);
        btnPrev.setOnClickListener(v -> {
            if (currentMonth > 1) { currentMonth--; drawCalendar(); }
        });
        btnNext.setOnClickListener(v -> {
            if (currentMonth < 12) { currentMonth++; drawCalendar(); }
        });
        drawCalendar();
        
        // Bottom half views
        btnSelectThings = findViewById(R.id.report_button_select_things);
        tvSelectedThings = findViewById(R.id.report_selected_things);
        Button btnSummarize = findViewById(R.id.report_button_summarize);
        
        loadAllThings();
        selectedThingsFlags = new boolean[allThings.size()];
        // Initially, no things are selected.
        updateSelectedThingsText();
        
        btnSelectThings.setOnClickListener(v -> {
            showThingsMultiChoiceDialog();
        });
        
        btnSummarize.setOnClickListener(v -> {
            if (selectedDate1 == null || selectedDate2 == null) {
                Toast.makeText(this, "Please select two days on the calendar", Toast.LENGTH_SHORT).show();
                return;
            }
            int startDay = Math.min(selectedDate1, selectedDate2);
            int endDay = Math.max(selectedDate1, selectedDate2);
            ArrayList<String> selectedThings = new ArrayList<>();
            for (int i = 0; i < allThings.size(); i++) {
                if (selectedThingsFlags[i]) {
                    selectedThings.add(allThings.get(i));
                }
            }
            if (selectedThings.isEmpty()) {
                Toast.makeText(this, "Please select at least one thing", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ReportActivity.this, SummaryActivity.class);
            intent.putExtra("startDay", startDay);
            intent.putExtra("endDay", endDay);
            intent.putStringArrayListExtra("selectedThings", selectedThings);
            startActivity(intent);
        });
    }
    
    private void drawCalendar() {
        monthLabel.setText("Month " + currentMonth);
        calendarTable.removeAllViews();
        TableRow header = new TableRow(this);
        String[] dayNames = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"};
        for (String d : dayNames) {
            TextView tv = new TextView(this);
            tv.setText(d);
            tv.setTextColor(Color.YELLOW);
            tv.setPadding(8,8,8,8);
            header.addView(tv);
        }
        calendarTable.addView(header);
        
        int offset = 6;
        for (int m = 1; m < currentMonth; m++) {
            offset = (offset + (monthDays[m-1] % 7)) % 7;
        }
        int days = monthDays[currentMonth - 1];
        int totalCells = offset + days;
        int rows = (int)Math.ceil(totalCells / 7.0);
        for (int i = 0; i < rows; i++) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < 7; j++) {
                int cellIndex = i*7 + j;
                TextView tv = new TextView(this);
                tv.setPadding(16,16,16,16);
                tv.setTextColor(Color.WHITE);
                if (cellIndex < offset || cellIndex >= offset + days) {
                    tv.setText("");
                } else {
                    int day = cellIndex - offset + 1;
                    tv.setText(String.valueOf(day));
                    final int thisDay = day;
                    tv.setOnClickListener(v -> {
                        onCalendarDayClicked(currentMonth, thisDay);
                    });
                    int doy = getDayOfYearForDate(currentMonth, day);
                    if ((selectedDate1 != null && doy == selectedDate1) ||
                        (selectedDate2 != null && doy == selectedDate2)) {
                        tv.setBackgroundColor(Color.RED);
                    } else if (selectedDate1 != null && selectedDate2 != null && doy > Math.min(selectedDate1, selectedDate2) && doy < Math.max(selectedDate1, selectedDate2)) {
                        tv.setBackgroundColor(Color.parseColor("#4444FF"));
                    }
                }
                row.addView(tv);
            }
            calendarTable.addView(row);
        }
    }
    
    private void onCalendarDayClicked(int month, int day) {
        int doy = getDayOfYearForDate(month, day);
        if (selectedDate1 == null) {
            selectedDate1 = doy;
            Toast.makeText(this, "First day selected", Toast.LENGTH_SHORT).show();
        } else if (selectedDate2 == null) {
            selectedDate2 = doy;
            Toast.makeText(this, "Second day selected", Toast.LENGTH_SHORT).show();
        } else {
            selectedDate1 = doy;
            selectedDate2 = null;
            Toast.makeText(this, "Selection reset. First day selected", Toast.LENGTH_SHORT).show();
        }
        drawCalendar();
    }
    
    private int getDayOfYearForDate(int month, int day) {
        int doy = 0;
        for (int m = 1; m < month; m++) {
            doy += monthDays[m-1];
        }
        doy += day;
        return doy;
    }
    
    private void loadAllThings() {
        allThings = new ArrayList<>();
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File thingsFile = new File(appFolder, "things.txt");
            if (thingsFile.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(thingsFile)));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        allThings.add(line.trim());
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showThingsMultiChoiceDialog() {
        if (allThings == null) loadAllThings();
        selectedThingsFlags = new boolean[allThings.size()];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Things");
        builder.setMultiChoiceItems(allThings.toArray(new CharSequence[0]), selectedThingsFlags, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedThingsFlags[which] = isChecked;
            }
        });
        builder.setPositiveButton("OK", (dialog, id) -> {
            updateSelectedThingsText();
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
    
    private void updateSelectedThingsText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allThings.size(); i++) {
            if (selectedThingsFlags[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(allThings.get(i));
            }
        }
        tvSelectedThings.setText(sb.toString());
    }
}
