package com.example.myapp4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;

public class CalendarActivity extends Activity {

    private TableLayout calendarTable;
    private TextView monthLabel;
    private Button previousButton, nextButton;

    // currentMonth is 0-indexed (0 = Month 1, 11 = Month 12)
    private int currentMonth = 0;
    // Month lengths: 6 months of 31 days, 5 months of 30 days, 1 month of 29 days.
    private final int[] monthDays = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};
    // Starting day offsets for each month. 0=Sat, 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri
    // The year starts on Friday => offset=6 for Month 1
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
        int offset = 6; // first month starts on Friday
        for (int i = 0; i < 12; i++) {
            monthStartOffsets[i] = offset;
            int days = monthDays[i];
            // move offset by the number of days in the current month, mod 7
            offset = (offset + (days % 7)) % 7;
        }
    }

    private void drawCalendar(int monthIndex) {
        // Update label
        monthLabel.setText("Month " + (monthIndex + 1));

        // Clear previous rows
        calendarTable.removeAllViews();

        // 1) Header row with day names: Sat..Fri
        TableRow headerRow = new TableRow(this);
        String[] dayNames = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"};
        for (String dayName : dayNames) {
            TextView tv = new TextView(this);
            tv.setText(dayName);
            tv.setTextColor(Color.parseColor("#FFFF00"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setPadding(16, 16, 16, 16);
            headerRow.addView(tv);
        }
        calendarTable.addView(headerRow);

        // 2) Days grid
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
                    emptyCell.setPadding(16, 16, 16, 16);
                    row.addView(emptyCell);
                } else {
                    // This is a valid day
                    Button dayButton = new Button(this);
                    dayButton.setText(String.valueOf(dayCounter));
                    dayButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    dayButton.setBackgroundColor(Color.TRANSPARENT);

                    // Friday is index 6
                    if (j == 6) {
                        dayButton.setTextColor(Color.RED);
                    } else {
                        dayButton.setTextColor(Color.parseColor("#FFFF00"));
                    }

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
