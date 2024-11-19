package com.example.facultytracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

public class dashboard_admin extends AppCompatActivity {


    private boolean isLoggedIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);
        Button logoutButton = findViewById(R.id.logOutB);
        ImageButton backButton = findViewById(R.id.backB);
        fetchUserDetails();



        CardView Faculty_info = findViewById(R.id.profileCard);
        Faculty_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(dashboard_admin.this, Admin_Faculty_Info.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView LeaveManagement = findViewById(R.id.HolidayCard);
        LeaveManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(dashboard_admin.this, Admin_Leave_Management.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Map = findViewById(R.id.collegeCard);
        Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(dashboard_admin.this, Admin_Map.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Settings = findViewById(R.id.settingsCard);
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(dashboard_admin.this, User_Settings.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Help = findViewById(R.id.helpCard);
        Help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(dashboard_admin.this, User_Help.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Aboutus = findViewById(R.id.aboutusCard);
        Aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(dashboard_admin.this, About_Us.class);
                Log.d("About Us", "performLogout: Logging out and navigating to MainActivity");
                // Start the new activity
                startActivity(intent);
            }
        });


       backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showbackAlert();
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

    }



    private void fetchUserDetails() {
        // Retrieve the stored username from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefAdmin", MODE_PRIVATE);
        String username = preferences.getString("username", "");


        Log.d("Admin Dashboard", "fetchUserDetails: Username = " + username );




        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Admin_Login");

        // Query the user with the stored username
        usersRef.whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);

                        // Fetch user details and update UI
                        String Branch = userDocument.getString("Branch");


                        Log.d("Fetch Details designation", "fetchUserDetails: User found - First Name: " + Branch);


                        updateUI(Branch);
                    } else {
                        // Handle the case where the user is not found
                        Log.d("Admin Dashboard", "fetchUserDetails: User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to fetch user details
                    Log.e("Admin Dashboard", "fetchUserDetails: Error fetching user details", e);
                });
    }

    private void updateUI(String Branch) {
        TextView designationTextView = findViewById(R.id.textView3);
        designationTextView.setText(Branch);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Branch", Branch);
        editor.apply();
        Log.d("SharedPreferences", "Branch: " + Branch);


        // You can similarly update other UI components with other user details
    }

    private void showbackAlert() {
        // Show an alert with "OK" and "Cancel" buttons
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to close the app?.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Log out the user or take any necessary actions here
                    // For now, simply close the app
                    finishAffinity();


                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog and do nothing
                    dialog.dismiss();
                })
                .setCancelable(false) // To prevent dismiss by tapping outside the dialog
                .show();

    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, perform logout
                        performLogout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked No, do nothing (dismiss the dialog)
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void performLogout() {
        // Implement your logic to perform logout actions
        // For example, you can clear the user session, navigate to the login page, etc.
        // After performing logout, set isLoggedIn to false
        SharedPreferences preferences = getSharedPreferences("MyPrefAdmin", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        isLoggedIn = false;



        // Navigate to the login page, you can replace Login_Page.class with your actual login activity
        Intent intent = new Intent(dashboard_admin.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();


        Log.d("Logout", "performLogout: Logging out and navigating to MainActivity");


    }

}