package com.example.myapp7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;
import com.example.myapp7.R;  // Ensure R is imported

public class CalendarActivity extends Activity {

    private TableLayout calendarTable;
    private TextView monthLabel;
    private Button previousButton, nextButton;

    // currentMonth is 0-indexed (0 = Month 1, 11 = Month 12)
    private int currentMonth = 0;
    // Month lengths: 6 months of 31 days, 5 months of 30 days, 1 month of 29 days.
    private final int[] monthDays = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};
    // Starting day offsets for each month.
    // Week order: Saturday=0, Sunday=1, Monday=2, Tuesday=3, Wednesday=4, Thursday=5, Friday=6.
    // The year starts on Friday (offset 6) so Month 1’s day 1 falls on Friday.
    private int[] monthStartOffsets = new int[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        monthLabel = findViewById(R.id.month_label);
        calendarTable = findViewById(R.id.calendar_table);
        previousButton = findViewById(R.id.button_previous);
        nextButton = findViewById(R.id.button_next);

        computeMonthStartOffsets();
        drawCalendar(currentMonth);

        previousButton.setOnClickListener(v -> {
            if (currentMonth > 0) {
                currentMonth--;
                drawCalendar(currentMonth);
            } else {
                Toast.makeText(this, "This is the first month.", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentMonth < 11) {
                currentMonth++;
                drawCalendar(currentMonth);
            } else {
                Toast.makeText(this, "This is the last month.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void computeMonthStartOffsets() {
        int offset = 6; // Month 1 starts on Friday (index 6)
        for (int i = 0; i < 12; i++) {
            monthStartOffsets[i] = offset;
            int days = monthDays[i];
            offset = (offset + (days % 7)) % 7;
        }
    }

    private void drawCalendar(int monthIndex) {
        monthLabel.setText("Month " + (monthIndex + 1));

        // Clear previous rows
        calendarTable.removeAllViews();

        // Set a fixed cell width (e.g., 48dp)
        int cellWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                48, getResources().getDisplayMetrics());

        // Header row with day names: Sat, Sun, Mon, Tue, Wed, Thu, Fri
        TableRow headerRow = new TableRow(this);
        String[] dayNames = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"};
        for (String dayName : dayNames) {
            TextView tv = new TextView(this);
            tv.setText(dayName);
            tv.setTextColor(Color.parseColor("#FFFF00"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setPadding(8, 8, 8, 8);
            tv.setLayoutParams(new TableRow.LayoutParams(cellWidth,
                    TableRow.LayoutParams.WRAP_CONTENT));
            headerRow.addView(tv);
        }
        calendarTable.addView(headerRow);

        int startOffset = monthStartOffsets[monthIndex];
        int daysInMonth = monthDays[monthIndex];
        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);

        int dayCounter = 1;
        for (int i = 0; i < rows; i++) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < 7; j++) {
                int cellIndex = i * 7 + j;
                if (cellIndex < startOffset || dayCounter > daysInMonth) {
                    // Empty cell
                    TextView emptyCell = new TextView(this);
                    emptyCell.setText("");
                    emptyCell.setPadding(8, 8, 8, 8);
                    emptyCell.setLayoutParams(new TableRow.LayoutParams(cellWidth,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    row.addView(emptyCell);
                } else {
                    // Day button
                    Button dayButton = new Button(this);
                    dayButton.setText(String.valueOf(dayCounter));
                    dayButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    dayButton.setBackgroundColor(Color.TRANSPARENT);
                    // Use red for Friday (column index 6)
                    if (j == 6) {
                        dayButton.setTextColor(Color.RED);
                    } else {
                        dayButton.setTextColor(Color.parseColor("#FFFF00"));
                    }
                    dayButton.setLayoutParams(new TableRow.LayoutParams(cellWidth,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    final int selectedDay = dayCounter;
                    dayButton.setOnClickListener(v -> {
                        Intent intent = new Intent(CalendarActivity.this, DayActivity.class);
                        intent.putExtra("month", monthIndex + 1);
                        intent.putExtra("day", selectedDay);
                        startActivity(intent);
                    });
                    row.addView(dayButton);
                    dayCounter++;
                }
            }
            calendarTable.addView(row);
        }
    }
}
