package com.example.facultytracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class User_Home extends AppCompatActivity {

    private boolean isLoggedIn = true;
    public static final String CHANNEL_ID = "YourChannelId";
    public static final String CHANNEL_NAME = "YourChannelName";
    public static final String CHANNEL_DESCRIPTION = "YourChannelDescription";

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 123;

    private static final String TAG = "GeofenceActivity";
    private static final int GEOFENCE_RADIUS = 100; // in meters




    @Override
    public void onBackPressed() {
        if (isLoggedIn) {
            // If the user is logged in, show a logout confirmation or handle logout logic
            showbackAlert(); // You can create a method to show a dialog or perform logout logic
        } else {
            // If the user is not logged in, allow normal back button behavior
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Button logoutButton = findViewById(R.id.logOutB);
        ImageButton backButton = findViewById(R.id.backB);

        if (!hasNotificationPermission()) {
            // If not, request the permission
            requestNotificationPermission();
        }


        CardView profile = findViewById(R.id.profileCard);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(User_Home.this, User_Profile.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Leave = findViewById(R.id.HolidayCard);
        Leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(User_Home.this, User_Leave.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView College = findViewById(R.id.collegeCard);
        College.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(User_Home.this, User_College.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Settings = findViewById(R.id.settingsCard);
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(User_Home.this, User_Settings.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Help = findViewById(R.id.helpCard);
        Help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(User_Home.this, User_Help.class);

                // Start the new activity
                startActivity(intent);
            }
        });

        CardView Aboutus = findViewById(R.id.aboutusCard);
        Aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the ProfileActivity (replace ProfileActivity.class with your actual activity class)
                Intent intent = new Intent(User_Home.this, About_Us.class);

                // Start the new activity
                startActivity(intent);
            }
        });




        ImageView imageView = findViewById(R.id.image_view1); // Replace "yourImageView" with the actual ID of your ImageView
        Drawable drawable = imageView.getDrawable();

// Set the color filter to black
        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

// Apply the modified drawable to the ImageView
        imageView.setImageDrawable(drawable);


        fetchUserDetails();


        startBackgroundServiceIfLoggedIn();










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

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

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
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        isLoggedIn = false;

        Intent serviceIntent = new Intent(User_Home.this, BackgroundService.class);
        stopService(serviceIntent);

        // Navigate to the login page, you can replace Login_Page.class with your actual login activity
        Intent intent = new Intent(User_Home.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();


        Log.d("Logout", "performLogout: Logging out and navigating to MainActivity");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(User_Home.this, BackgroundService.class);
        stopService(serviceIntent);

    }

    private void startBackgroundServiceIfLoggedIn() {
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            startService(serviceIntent);
    }






    private void fetchUserDetails() {
        // Retrieve the stored username from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String selectedDepartment = preferences.getString("selectedDepartment", "");

        Log.d(TAG, "fetchUserDetails: Username = " + username + ", Department = " + selectedDepartment);




        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection(selectedDepartment);

        // Query the user with the stored username
        usersRef.whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);

                        // Fetch user details and update UI
                        String firstName = userDocument.getString("firstName");
                        String lastName = userDocument.getString("lastName");
                       String designation =userDocument.getString("designation");

                        Log.d("Fetch Details designation", "fetchUserDetails: User found - First Name: " + firstName + ", Last Name: " + lastName+"Designation "+designation);


                        updateUI(firstName, lastName,designation);
                    } else {
                        // Handle the case where the user is not found
                        Log.d(TAG, "fetchUserDetails: User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to fetch user details
                    Log.e(TAG, "fetchUserDetails: Error fetching user details", e);
                });
    }

    private void updateUI(String firstName, String lastName,String designation) {
        TextView name = findViewById(R.id.textView2);
        name.setText(firstName + " " + lastName);
        TextView Designation= findViewById(R.id.textView3);
       Designation.setText(designation);

        // You can similarly update other UI components with other user details
    }

    private boolean hasNotificationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel if using Android Oreo or higher
            createNotificationChannel();
        }

        // Request notification permission
        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Notification permission denied
                showPermissionDeniedDialog();
            }
        }
    }
    private void createNotificationChannel() {
        // Check if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied")
                .setMessage("To receive notifications, the app needs the notification permission. Please grant the permission in the app settings.")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle cancel action or dismiss the dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}










