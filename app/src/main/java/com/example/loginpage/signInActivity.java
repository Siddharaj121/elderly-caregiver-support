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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signInActivity extends AppCompatActivity {

    private EditText editTextName, editTextSurname, editTextEmail, editTextPassword, editTextPhone, editTextAddress;
    private Button buttonSignIn;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public void LogIn(View v){
        Intent intent = new Intent(signInActivity.this, logInActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonSignIn = findViewById(R.id.buttonLogIn);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String surname = editTextSurname.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (!validateInputs(name, surname, email, password, phone, address)) {
            return;
        }

        checkIfUserExists(email, name, surname, password, phone, address);
    }

    private boolean validateInputs(String name, String surname, String email, String password, String phone, String address) {
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(surname)) {
            editTextSurname.setError("Surname is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email format");
            return false;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            editTextAddress.setError("Address is required");
            return false;
        }
        return true;
    }

    private void checkIfUserExists(String email, String name, String surname, String password, String phone, String address) {
        CollectionReference usersRef = db.collection("users");

        usersRef.whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(signInActivity.this, "Email already registered!", Toast.LENGTH_SHORT).show();
                    } else {
                        registerNewUser(name, surname, email, password, phone, address);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(signInActivity.this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void registerNewUser(String name, String surname, String email, String password, String phone, String address) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("surname", surname);
        user.put("email", email);
        user.put("password", password);
        user.put("phone", phone);
        user.put("address", address);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> Toast.makeText(signInActivity.this, "User Registered!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(signInActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
