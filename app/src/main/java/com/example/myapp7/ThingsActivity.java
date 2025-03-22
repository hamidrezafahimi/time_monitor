package com.example.myapp7;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myapp7.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThingsActivity extends Activity {

    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 222;
    private static final int REQUEST_WRITE_STORAGE = 111;
    private LinearLayout containerSavedThings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_things);

        EditText thingInput = findViewById(R.id.edittext_thing);
        Button saveThingButton = findViewById(R.id.button_save_thing);
        containerSavedThings = findViewById(R.id.container_saved_things);

        // Check external storage permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please grant all files access permission", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }
        }

        saveThingButton.setOnClickListener(v -> {
            String thing = thingInput.getText().toString().trim();
            if (thing.isEmpty()) {
                Toast.makeText(ThingsActivity.this, "Please enter a thing", Toast.LENGTH_SHORT).show();
                return;
            }
            // Append the thing as a new line to things.txt.
            String entry = thing + "\n";
            try {
                File rootDir = Environment.getExternalStorageDirectory();
                String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
                File appFolder = new File(rootDir, appLabel);
                if (!appFolder.exists() && !appFolder.mkdirs()) {
                    Toast.makeText(ThingsActivity.this, "Could not create folder in external storage", Toast.LENGTH_SHORT).show();
                    return;
                }
                File thingsFile = new File(appFolder, "things.txt");
                FileOutputStream fos = new FileOutputStream(thingsFile, true);
                fos.write(entry.getBytes());
                fos.close();
                Toast.makeText(ThingsActivity.this, "Thing saved to: " + thingsFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                thingInput.setText("");  // Clear input.
                loadSavedThings(); // Refresh the list.
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ThingsActivity.this, "Error saving thing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load saved things on start.
        loadSavedThings();
    }

    private void loadSavedThings() {
        containerSavedThings.removeAllViews();
        List<String> thingsList = readThingsFile();
        for (String thing : thingsList) {
            if (!thing.trim().isEmpty()) {
                // Create a horizontal layout.
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                // Create a red cross button.
                Button crossButton = new Button(this);
                crossButton.setText("✖");
                crossButton.setTextColor(Color.RED);
                crossButton.setTextSize(18);
                // When tapped, remove the thing.
                crossButton.setOnClickListener(v -> {
                    removeThing(thing);
                });
                // Create a TextView for the thing.
                TextView thingText = new TextView(this);
                thingText.setText(thing);
                thingText.setTextColor(Color.WHITE);
                thingText.setTextSize(18);
                thingText.setPadding(16, 0, 0, 0);
                itemLayout.addView(crossButton);
                itemLayout.addView(thingText);
                containerSavedThings.addView(itemLayout);
            }
        }
    }

    private List<String> readThingsFile() {
        List<String> list = new ArrayList<>();
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File appFolder = new File(rootDir, appLabel);
            File thingsFile = new File(appFolder, "things.txt");
            if (!thingsFile.exists()) return list;
            FileInputStream fis = new FileInputStream(thingsFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    list.add(line);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void removeThing(String thingToRemove) {
        // Read all things, remove the matching one, and rewrite the file.
        List<String> thingsList = readThingsFile();
        boolean removed = thingsList.remove(thingToRemove);
        if (removed) {
            try {
                File rootDir = Environment.getExternalStorageDirectory();
                String appLabel = getApplicationInfo().loadLabel(getPackageManager()).toString();
                File appFolder = new File(rootDir, appLabel);
                File thingsFile = new File(appFolder, "things.txt");
                FileOutputStream fos = new FileOutputStream(thingsFile, false);
                for (String t : thingsList) {
                    fos.write((t + "\n").getBytes());
                }
                fos.close();
                Toast.makeText(this, "Thing removed", Toast.LENGTH_SHORT).show();
                loadSavedThings();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error removing thing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
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
