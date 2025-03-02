package com.example.loginpage;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final int SMS_PERMISSION_REQUEST = 101;
    private LocationCallback locationCallback;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button btnSOS = findViewById(R.id.btnSOS);
        btnSOS.setOnClickListener(v -> showSOSConfirmationDialog());

        // Initialize location updates
        requestLocationUpdates();
    }

    private void showSOSConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm SOS Alert")
                .setMessage("Are you sure you want to send an emergency SOS alert?")
                .setPositiveButton("Yes", (dialog, which) -> sendSOSMessage())
                .setNegativeButton("No", null)
                .show();
    }

    private void sendSOSMessage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getLocationAndSendMessage();
        }
    }

    private void getLocationAndSendMessage() {
        if (latitude != 0 && longitude != 0) {
            String locationUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
            String message = "🚨 SOS Alert! A patient needs help. Location: " + locationUrl;

            sendSMS(message);
            sendWhatsAppMessage(message);
        } else {
            Toast.makeText(this, "Location not available. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS(String message) {
        String doctorNumber = "9307426214";  // Replace with Doctor's number
        String ambulanceNumber = "9307426214";  // Replace with Ambulance's number

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(doctorNumber, null, message, null, null);
                smsManager.sendTextMessage(ambulanceNumber, null, message, null, null);
                Toast.makeText(this, "SOS Sent via SMS!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "SMS failed to send!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendWhatsAppMessage(String message) {
        String doctorWhatsApp = "+919307426214";
        String ambulanceWhatsApp = "+919307426214";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + doctorWhatsApp + "?text=" + Uri.encode(message)));
            startActivity(intent);

            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(Uri.parse("https://wa.me/" + ambulanceWhatsApp + "?text=" + Uri.encode(message)));
            startActivity(intent2);

            Toast.makeText(this, "SOS Sent via WhatsApp!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        }
    }

    // Request real-time location updates
    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // Update every 10 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSendMessage();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted. Press SOS again!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
