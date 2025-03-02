package com.example.loginpage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class logInActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(logInActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Prevent going back to login screen
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        checkUserCredentials(email, password);
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Invalid email format");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            return false;
        }
        return true;
    }

    private void checkUserCredentials(String email, String password) {
        CollectionReference usersRef = db.collection("users");

        usersRef.whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String storedPassword = document.getString("password");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                Toast.makeText(logInActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                navigateToHome(); // ✅ Now correctly placed
                            } else {
                                Toast.makeText(logInActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                    } else {
                        Toast.makeText(logInActivity.this, "No user found with this email!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(logInActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
