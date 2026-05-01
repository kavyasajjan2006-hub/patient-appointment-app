package com.example.patientappointment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

// CRITICAL IMPORTS FOR LISTS
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private EditText etDate, etTime;
    private Spinner spDoctor;
    private DatabaseReference db;
    private String currentlySelectedDoctor = "";

    private final String[] allDoctors = {
            "Dr. Ram(Cardiology)",
            "Dr. Sitha (Dermatology)",
            "Dr. Radha (Pediatrics)",
            "Dr. Krishna (Orthopedics)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Find views by ID
        spDoctor = findViewById(R.id.spDoctor);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        Button btnConfirm = findViewById(R.id.btnConfirmBooking);
        ImageButton btnBack = findViewById(R.id.btnBackTopLeft);

        db = FirebaseDatabase.getInstance().getReference("Appointments");

        btnBack.setOnClickListener(v -> finish());

        refreshDoctorList(new ArrayList<String>());

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", d, (m + 1), y);
                etDate.setText(selectedDate);
                checkAvailability();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            Calendar maxDate = Calendar.getInstance();
            maxDate.add(Calendar.MONTH, 3);
            dpd.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            dpd.show();
        });

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, min) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, min);
                etTime.setText(selectedTime);
                checkAvailability();
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });

        btnConfirm.setOnClickListener(v -> {
            String dateStr = etDate.getText().toString();
            String timeStr = etTime.getText().toString();

            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(this, "Select date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            String selection = spDoctor.getSelectedItem().toString();
            if (selection.contains("(Busy)")) {
                Toast.makeText(this, "Doctor is busy. Book with a 20-min gap!", Toast.LENGTH_LONG).show();
            } else {
                saveBooking(selection);
            }
        });
    }

    private int timeToMinutes(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            return (Integer.parseInt(parts[0]) * 60) + Integer.parseInt(parts[1]);
        } catch (Exception e) { return -1; }
    }

    private void checkAvailability() {
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        if (date.isEmpty() || time.isEmpty()) return;

        int selectedTimeMins = timeToMinutes(time);

        if (spDoctor.getSelectedItem() != null) {
            currentlySelectedDoctor = spDoctor.getSelectedItem().toString().split(" \\(")[0];
        }

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> displayList = new ArrayList<>();
                for (String docInfo : allDoctors) {
                    String docNameOnly = docInfo.split(" \\(")[0];
                    boolean busy = false;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Appointment a = ds.getValue(Appointment.class);
                        if (a != null && a.getDoctor() != null && a.getDoctor().equals(docNameOnly) && a.getDate().equals(date)) {
                            int existingTime = timeToMinutes(a.getTime());
                            if (Math.abs(selectedTimeMins - existingTime) < 20) {
                                busy = true;
                                break;
                            }
                        }
                    }
                    displayList.add(busy ? docInfo + " (Busy)" : docInfo);
                }
                refreshDoctorList(displayList);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void refreshDoctorList(List<String> list) {
        if (list.isEmpty()) Collections.addAll(list, allDoctors);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spDoctor.setAdapter(adapter);

        if (!currentlySelectedDoctor.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).startsWith(currentlySelectedDoctor)) {
                    spDoctor.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveBooking(String fullInfo) {
        String id = db.push().getKey();
        String uid = FirebaseAuth.getInstance().getUid();
        if (id == null || uid == null) return;

        String doctorName = fullInfo.split(" \\(")[0];
        String spec = fullInfo.substring(fullInfo.indexOf("(") + 1, fullInfo.indexOf(")"));

        Appointment appt = new Appointment(id, uid, doctorName, spec, etDate.getText().toString(), etTime.getText().toString());
        db.child(id).setValue(appt).addOnSuccessListener(aVoid -> finish());
    }
}