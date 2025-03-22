package com.example.myapp7;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.myapp7.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends Activity {
    private int startDay, endDay;
    private ArrayList<String> selectedThings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        
        startDay = getIntent().getIntExtra("startDay", 0);
        endDay = getIntent().getIntExtra("endDay", 0);
        selectedThings = getIntent().getStringArrayListExtra("selectedThings");
        
        if (startDay == 0 || endDay == 0 || selectedThings == null || selectedThings.isEmpty()) {
            Toast.makeText(this, "Invalid report parameters.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LinearLayout container = findViewById(R.id.summary_container);
        container.removeAllViews();
        
        List<String> allThings = getThingsList();
        String[][] table = readStatisticsDatabase();
        for (String thing : selectedThings) {
            int thingIndex = allThings.indexOf(thing);
            if (thingIndex < 0) continue;
            int colIndex = thingIndex * 3 + 2; // duration column
            int sumDuration = 0;
            for (int i = startDay - 1; i < endDay; i++) {
                String durStr = table[i][colIndex];
                if (!durStr.isEmpty()) {
                    sumDuration += parseTimeToMinutes(durStr);
                }
            }
            int sumH = sumDuration / 60;
            int sumM = sumDuration % 60;
            String sumFormatted = String.format("%d:%02d", sumH, sumM);
            TextView tv = new TextView(this);
            tv.setText(thing + " - Total Duration: " + sumFormatted);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(18);
            container.addView(tv);
        }
    }
    
    // Reads the statistics database from statistics.txt as a 2D array.
    private String[][] readStatisticsDatabase() {
        List<String[]> rows = new ArrayList<>();
        List<String> allThingsList = getThingsList();
        int numCols = allThingsList.size() * 3;
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
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
        List<String> allThings = getThingsList();
        numCols = allThings.size() * 3; // Remove duplicate declaration by reusing variable name
        String[][] table = new String[365][numCols];
        for (int i = 0; i < 365; i++) {
            if (i < rows.size()) {
                String[] row = rows.get(i);
                for (int j = 0; j < numCols; j++) {
                    table[i][j] = j < row.length ? row[j] : "";
                }
            } else {
                for (int j = 0; j < numCols; j++) {
                    table[i][j] = "";
                }
            }
        }
        return table;
    }
    
    // Reads things.txt.
    private List<String> getThingsList() {
        List<String> things = new ArrayList<>();
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return things;
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
}
