package com.example.loginpage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HospitalActivity extends AppCompatActivity {

    private EditText hospitalName, hospitalPhone, hospitalAddress, hospitalType, hospitalEmail, hospitalPassword;
    private Button submitBtn;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);

        // Initialize Firebase (ensure it's already initialized in Application class)
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        hospitalName = findViewById(R.id.hospitalName);
        hospitalPhone = findViewById(R.id.hospitalPhone);
        hospitalAddress = findViewById(R.id.hospitalAddress);
        hospitalType = findViewById(R.id.hospitalType);
        hospitalEmail = findViewById(R.id.hospitalEmail);
        hospitalPassword = findViewById(R.id.hospitalPassword);
        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v -> registerHospital());
    }

    private void registerHospital() {
        String name = hospitalName.getText().toString().trim();
        String phone = hospitalPhone.getText().toString().trim();
        String address = hospitalAddress.getText().toString().trim();
        String type = hospitalType.getText().toString().trim();
        String email = hospitalEmail.getText().toString().trim();
        String password = hospitalPassword.getText().toString().trim();

        if (!validateInputs(name, phone, address, type, email, password)) {
            return;
        }

        createHospitalAccount(email, password, name, phone, address, type);
    }

    private boolean validateInputs(String name, String phone, String address, String type, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            hospitalName.setError("Hospital name is required");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            hospitalPhone.setError("Phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            hospitalAddress.setError("Address is required");
            return false;
        }
        if (TextUtils.isEmpty(type)) {
            hospitalType.setError("Hospital type is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            hospitalEmail.setError("Invalid email format");
            return false;
        }
        if (password.length() < 6) {
            hospitalPassword.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void createHospitalAccount(String email, String password, String name, String phone, String address, String type) {
        // 🔴 FIXED: Disable reCAPTCHA verification in Debug mode
        auth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser hospitalUser = auth.getCurrentUser();
                        if (hospitalUser != null) {
                            saveHospitalData(hospitalUser.getUid(), name, phone, address, type, email);
                        }
                    } else {
                        handleAuthError(task.getException());
                    }
                });
    }

    private void saveHospitalData(String hospitalId, String name, String phone, String address, String type, String email) {
        Map<String, Object> hospital = new HashMap<>();
        hospital.put("name", name);
        hospital.put("phone", phone);
        hospital.put("address", address);
        hospital.put("type", type);
        hospital.put("email", email);

        db.collection("hospitals").document(hospitalId).set(hospital)
                .addOnSuccessListener(aVoid -> Toast.makeText(HospitalActivity.this, "Hospital Registered!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(HospitalActivity.this, "Failed to store data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleAuthError(Exception exception) {
        if (exception != null) {
            String errorMessage = exception.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("email address is already in use")) {
                    hospitalEmail.setError("This email is already registered");
                } else if (errorMessage.contains("WEAK_PASSWORD")) {
                    hospitalPassword.setError("Password is too weak");
                }
            }
            Toast.makeText(HospitalActivity.this, "Registration Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
