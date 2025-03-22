package com.example.myapp7;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Environment;
import com.example.myapp7.R;  // Import the generated R class

public class DayActivity extends Activity {
    private static final int REQUEST_WRITE_STORAGE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        int month = getIntent().getIntExtra("month", 0);
        int day = getIntent().getIntExtra("day", 0);

        TextView dayHeader = findViewById(R.id.day_header);
        dayHeader.setText("Month " + month + " - Day " + day);

        EditText noteField = findViewById(R.id.note_field);
        Button saveButton = findViewById(R.id.button_save);

        // Check for external storage write permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        saveButton.setOnClickListener(v -> {
            String note = noteField.getText().toString().trim();
            if (note.isEmpty()) {
                Toast.makeText(DayActivity.this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // Format entry
            String entry = "Month " + month + " - Day " + day + "\n" + note + "\n---\n";
            try {
                // Get external storage directory
                File rootDir = Environment.getExternalStorageDirectory();
                // Use the app's label as the folder name (will change if you change the app name)
                String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
                File appFolder = new File(rootDir, appLabel);
                if (!appFolder.exists() && !appFolder.mkdirs()) {
                    Log.e("DayActivity", "Failed to create folder: " + appFolder.getAbsolutePath());
                    Toast.makeText(DayActivity.this, "Could not create folder in external storage", Toast.LENGTH_SHORT).show();
                    return;
                }
                File dayNotesFile = new File(appFolder, "day_notes.txt");
                FileOutputStream fos = new FileOutputStream(dayNotesFile, true);
                fos.write(entry.getBytes());
                fos.close();
                Toast.makeText(DayActivity.this,
                        "Saved to: " + dayNotesFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(DayActivity.this, "Error saving note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "External storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
