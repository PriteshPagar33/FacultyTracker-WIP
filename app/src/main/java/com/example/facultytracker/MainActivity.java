package com.example.facultytracker;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationRequest;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BaseActivity_main {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




            if (isNetworkAvailable()) {

                if (checkLocationPermission()) {
                    // Permissions already granted, proceed with location access
                    getLocation();
                } else {
                    // Request location permissions
                    requestLocationPermission();
                }

                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
                SharedPreferences preferenceAdmin = getSharedPreferences("MyPrefAdmin", MODE_PRIVATE);
                boolean isLoggedInAdmin = preferenceAdmin.getBoolean("isLoggedIn", false);


               if(isLoggedInAdmin)
                {
                    startActivity(new Intent(this, dashboard_admin.class));
                    finish(); // Close MainActivity to prevent going back
                }

                if (isLoggedIn) {
                    // User is already logged in, navigate to User_Home

                    startActivity(new Intent(this, User_Home.class));
                    finish(); // Close MainActivity to prevent going back
                }

                // Set background color for the entire screen
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser != null) {
                    // User is already logged in, navigate to User_Home
                    startActivity(new Intent(this, User_Home.class));
                    finish(); // Close MainActivity to prevent going back
                }
                setContentView(R.layout.activity_main);

                // Find the "Register" button by its ID
                Button registerButton = findViewById(R.id.buttonRegister);

                // Set a click listener for the "Register" button
                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Open the RegisterActivity when the button is clicked
                        Intent intent = new Intent(MainActivity.this, Register_page.class);
                        startActivity(intent);
                    }
                });

                // Find the "Login" button by its ID
                Button loginButton = findViewById(R.id.buttonLogin);

                // Set a click listener for the "Login" button
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Open the LoginActivity when the button is clicked
                        Intent intent = new Intent(MainActivity.this, Login_Page.class);
                        startActivity(intent);
                    }
                });

                // Find the "Admin Login" button by its ID
                Button adminLoginButton = findViewById(R.id.buttonAdminLogin);

                // Set a click listener for the "Admin Login" button
                adminLoginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Open the AdminLoginActivity when the button is clicked
                        Intent intent = new Intent(MainActivity.this, Admin_login.class);
                        startActivity(intent);
                    }
                });
            } else {
                // Internet is not available, show an AlertDialog with a retry button
                showNoInternetDialog();
            }

    }

    private void showAppNotAllowedError() {
        // Display an error message to the user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App Not Allowed");
        builder.setMessage("The app is not allowed to run at the current time.");
        builder.setPositiveButton("OK", (dialog, which) -> finish()); // Close the app
        builder.setCancelable(false);
        builder.show();
    }



    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location access
                getLocation();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }

    private void getLocation() {
        // TODO: Use the Fused Location Provider API to get the device's location
        // You can use Google Play services' LocationServices or Android's LocationManager
        // For simplicity, let's use a placeholder method
        // getDeviceLocation();
    }

    private void checkUserActivity() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // Reference to the user's activity document
            DocumentReference userActivityRef = db.collection("user_activity").document(userId);

            userActivityRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Timestamp lastActivityTimestamp = documentSnapshot.getTimestamp("lastActivity");

                    if (lastActivityTimestamp != null) {
                        long fifteenDaysInMillis = 15 * 24 * 60 * 60 * 1000; // 15 days in milliseconds
                        long currentTimeInMillis = System.currentTimeMillis();

                        if (currentTimeInMillis - lastActivityTimestamp.toDate().getTime() <= fifteenDaysInMillis) {
                            // User was active recently, navigate to the home page
                            startActivity(new Intent(this, User_Home.class));
                        } else {
                            // User was not active recently, show the login page
                            startActivity(new Intent(this, Login_Page.class));
                        }
                    } else {
                        // Handle the case where lastActivityTimestamp is null
                        // Show the login page
                        startActivity(new Intent(this, Login_Page.class));
                    }
                } else {
                    // Handle the case where the document doesn't exist
                    // Show the login page
                    startActivity(new Intent(this, Login_Page.class));
                }

                finish();  // finish the main activity to prevent going back
            }).addOnFailureListener(e -> {
                // Handle the failure to get the user's activity document
                // Show the login page
                startActivity(new Intent(this, Login_Page.class));
                finish();  // finish the main activity to prevent going back
            });
        } else {
            // User is not logged in, show the main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();  // finish the main activity to prevent going back
        }
    }








}
