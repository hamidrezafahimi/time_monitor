package com.example.myapp7;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myapp7.R;  // Import the generated R class
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DayActivity extends Activity {
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 222;
    private static final int REQUEST_WRITE_STORAGE = 111;  // fallback for older devices

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

        // For Android 11+ (API 30+), request MANAGE_EXTERNAL_STORAGE if needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please grant all files access permission", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
                // Note: The user must manually grant this permission in system settings.
            }
        } else {
            // For older versions, request WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }
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
                // Write file to a public directory using the app label as folder name.
                File rootDir = Environment.getExternalStorageDirectory();
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
