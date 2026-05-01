package com.example.patientappointment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // If not logged in, force to Register/Login screen
        if (mAuth.getCurrentUser() == null) {
            goToLogin();
            return;
        }

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText(String.format("Welcome, %s", mAuth.getCurrentUser().getEmail()));

        findViewById(R.id.btnGoToBook).setOnClickListener(v ->
                startActivity(new Intent(this, BookingActivity.class)));

        findViewById(R.id.btnGoToView).setOnClickListener(v ->
                startActivity(new Intent(this, ViewAppointmentsActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            goToLogin();
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, RegisterActivity.class);
        // This clears the backstack so user can't "Go Back" into the app
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}