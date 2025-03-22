package com.example.myapp4;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.IOException;

public class DayActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        int month = getIntent().getIntExtra("month", 0);
        int day = getIntent().getIntExtra("day", 0);

        // Update header to show month-day format.
        TextView dayHeader = findViewById(R.id.day_header);
        dayHeader.setText("Month " + month + " - Day " + day);

        EditText noteField = findViewById(R.id.note_field);
        Button saveButton = findViewById(R.id.button_save);

        saveButton.setOnClickListener(v -> {
            String note = noteField.getText().toString().trim();
            if (note.isEmpty()) {
                Toast.makeText(DayActivity.this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // Format: month-day then note then separator.
            String entry = "Month " + month + " - Day " + day + "\n" + note + "\n---\n";
            try {
                // Open file in append mode.
                FileOutputStream fos = openFileOutput("day_notes.txt", Context.MODE_APPEND);
                fos.write(entry.getBytes());
                fos.close();
                Toast.makeText(DayActivity.this, "Note saved!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(DayActivity.this, "Error saving note", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
