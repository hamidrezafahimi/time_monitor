package com.example.myapp7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapp7.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FieldsActivity extends Activity {
    private LinearLayout fieldsContainer;
    private EditText inputField;
    private Button btnAddField;
    private Button btnReport;
    private List<String> fieldsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fields);
        
        fieldsContainer = findViewById(R.id.fields_container);
        inputField = findViewById(R.id.input_field);
        btnAddField = findViewById(R.id.btn_add_field);
        btnReport = findViewById(R.id.btn_report_fields);
        
        loadFields();
        
        btnAddField.setOnClickListener(v -> {
            String newField = inputField.getText().toString().trim();
            if(newField.isEmpty()){
                Toast.makeText(this, "Enter a field", Toast.LENGTH_SHORT).show();
                return;
            }
            fieldsList.add(newField);
            saveFields();
            inputField.setText("");
            loadFields();
        });
        
        btnReport.setOnClickListener(v -> {
            String reportData = getLatestFieldsReport();
            if(reportData.isEmpty()){
                Toast.makeText(this, "No fields data found.", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Latest Fields Report")
                        .setMessage(reportData)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
    
    // Load fields from fields.txt and display them with remove buttons.
    private void loadFields() {
        fieldsContainer.removeAllViews();
        fieldsList = getFieldsList();
        for(String field : fieldsList){
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(this);
            tv.setText(field);
            tv.setTextColor(Color.YELLOW);
            tv.setTextSize(18);
            Button btnRemove = new Button(this);
            btnRemove.setText("Remove");
            btnRemove.setOnClickListener(v -> {
                fieldsList.remove(field);
                saveFields();
                loadFields();
            });
            row.addView(tv);
            row.addView(btnRemove);
            fieldsContainer.addView(row);
        }
    }
    
    // Reads fields.txt and returns a list of field names.
    private List<String> getFieldsList(){
        List<String> list = new ArrayList<>();
        try{
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File fieldsFile = new File(appFolder, "fields.txt");
            if(!fieldsFile.exists()) return list;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fieldsFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.trim().isEmpty()){
                    list.add(line.trim());
                }
            }
            reader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        return list;
    }
    
    // Saves the list of fields into fields.txt.
    private void saveFields(){
        try{
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            if(!appFolder.exists()){
                appFolder.mkdirs();
            }
            File fieldsFile = new File(appFolder, "fields.txt");
            FileOutputStream fos = new FileOutputStream(fieldsFile, false);
            for(String field : fieldsList){
                fos.write((field+"\n").getBytes());
            }
            fos.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    // Returns a string report table from fieldsData.txt.
    // The file is assumed to have blocks in the format:
    // monthX-dayY
    // field1:value1
    // field2:value2
    // ---
    // This method finds, for each field (from fields.txt), the record with the highest day-of-year.
    private String getLatestFieldsReport() {
        StringBuilder report = new StringBuilder();
        List<String> fieldNames = getFieldsList();
        // Map each field to its latest record.
        // We'll store: field -> (latestDay, value)
        class Record {
            int day;
            String value;
            Record(int day, String value) {
                this.day = day;
                this.value = value;
            }
        }
        java.util.Map<String, Record> map = new java.util.HashMap<>();
        try {
            File rootDir = android.os.Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File fieldsDataFile = new File(appFolder, "fieldsData.txt");
            if (!fieldsDataFile.exists()) return "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fieldsDataFile)));
            String line;
            String currentHeader = null;
            int currentDay = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if(line.equals("---")){
                    currentHeader = null;
                    currentDay = 0;
                    continue;
                }
                if(currentHeader == null){
                    currentHeader = line;
                    currentDay = parseHeaderToDayOfYear(currentHeader);
                } else {
                    if(line.contains(":")){
                        String[] parts = line.split(":", 2);
                        String field = parts[0].trim();
                        String value = parts[1].trim();
                        if(!map.containsKey(field) || currentDay > map.get(field).day){
                            map.put(field, new Record(currentDay, value));
                        }
                    }
                }
            }
            reader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        // Build table header.
        report.append("Field\tLast Set Value\tLast Day Updated\n");
        for(String field : fieldNames) {
            Record rec = map.get(field);
            String value = rec != null ? rec.value : "";
            String dayStr = rec != null ? "day " + rec.day : "";
            report.append(field).append("\t").append(value).append("\t").append(dayStr).append("\n");
        }
        return report.toString();
    }
    
    // Parses a header like "monthX-dayY" to a day-of-year.
    private int parseHeaderToDayOfYear(String header) {
        header = header.toLowerCase().replace("month", "").replace("day", "").trim();
        String[] parts = header.split("-");
        if(parts.length != 2) return 0;
        try {
            int month = Integer.parseInt(parts[0].trim());
            int day = Integer.parseInt(parts[1].trim());
            int doy = 0;
            // Assume months 1-6:31 days, 7-11:30 days, 12:29 days.
            for (int m = 1; m < month; m++) {
                if(m <= 6) {
                    doy += 31;
                } else if(m <= 11) {
                    doy += 30;
                } else {
                    doy += 29;
                }
            }
            doy += day;
            return doy;
        } catch(NumberFormatException e){
            return 0;
        }
    }
}
