package com.example.myapp7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.myapp7.R;

public class HomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        Button btnCalendar = findViewById(R.id.button_calendar);
        Button btnThings = findViewById(R.id.button_things);
        Button btnReport = findViewById(R.id.button_report);
        Button btnFields = findViewById(R.id.button_fields); // New button
        
        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        btnThings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ThingsActivity.class);
            startActivity(intent);
        });
        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            startActivity(intent);
        });
        btnFields.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FieldsActivity.class);
            startActivity(intent);
        });
    }
}
