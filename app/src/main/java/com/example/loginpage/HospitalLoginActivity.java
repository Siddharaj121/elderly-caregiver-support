package com.example.loginpage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HospitalLoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, locationEditText;
    private Spinner hospitalSpinner;
    private Button loginButton, searchButton;
    private ImageView hospitalLogo;
    private FirebaseFirestore db;
    private List<String> hospitalList = new ArrayList<>();
    private String selectedHospital = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        locationEditText = findViewById(R.id.locationEditText);
        hospitalSpinner = findViewById(R.id.hospitalSpinner);
        loginButton = findViewById(R.id.loginButton);
        searchButton = findViewById(R.id.searchButton);
        hospitalLogo = findViewById(R.id.hospitalLogo);

        // Search hospitals based on location
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = locationEditText.getText().toString().trim();
                if (!location.isEmpty()) {
                    fetchHospitalsByLocation(location);
                } else {
                    Toast.makeText(HospitalLoginActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle hospital selection from dropdown
        hospitalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHospital = hospitalList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHospital = "";
            }
        });

        // Handle Login Button Click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || selectedHospital.isEmpty()) {
                    Toast.makeText(HospitalLoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    authenticateHospital(email, password, selectedHospital);
                }
            }
        });
    }

    // Function to fetch hospitals by location
    private void fetchHospitalsByLocation(String location) {
        db.collection("hospitals")
                .whereEqualTo("location", location)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            hospitalList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                hospitalList.add(document.getString("hospitalName"));
                            }
                            updateHospitalSpinner();
                        } else {
                            Toast.makeText(HospitalLoginActivity.this, "No hospitals found in this location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Function to update the hospital dropdown
    private void updateHospitalSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hospitalList);
        hospitalSpinner.setAdapter(adapter);
    }

    // Function to authenticate hospital from Firestore
    private void authenticateHospital(String email, String password, String hospitalName) {
        db.collection("hospitals")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .whereEqualTo("hospitalName", hospitalName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            Toast.makeText(HospitalLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                           // Intent intent = new Intent(HospitalLoginActivity.this, DashboardActivity.class);
                            //startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(HospitalLoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
