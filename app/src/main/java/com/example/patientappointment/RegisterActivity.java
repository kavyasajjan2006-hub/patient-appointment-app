package com.example.patientappointment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // LOGIN BUTTON LOGIC
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    navigateToMain();
                } else {
                    // Extracting method here fixes the 'long surrounding method' warning
                    showToast(getAuthErrorMessage(task));
                }
            });
        });

        // REGISTER BUTTON LOGIC
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.length() < 6) {
                etPassword.setError("Minimum 6 characters");
                return;
            }

            setLoading(true);
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                setLoading(false);
                if (task.isSuccessful()) {
                    navigateToMain();
                } else {
                    showToast(getAuthErrorMessage(task));
                }
            });
        });
    }

    /**
     * Extracts error message logic to solve 'NullPointerException' and 'Redundancy' warnings.
     */
    @NonNull
    private String getAuthErrorMessage(@NonNull Task<AuthResult> task) {
        Exception exception = task.getException();
        if (exception == null) return "Authentication failed.";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "No account exists with this email.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Incorrect password or invalid email format.";
        } else {
            String message = exception.getMessage();
            return (message != null) ? message : "An unknown error occurred.";
        }
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        findViewById(R.id.btnLogin).setEnabled(!isLoading);
        findViewById(R.id.btnRegister).setEnabled(!isLoading);
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}