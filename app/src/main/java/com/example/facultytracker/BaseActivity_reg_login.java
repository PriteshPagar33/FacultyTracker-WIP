package com.example.facultytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class BaseActivity_reg_login extends AppCompatActivity {

    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the connectivity receiver
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);

        if (isAppAllowed()) {

        } else {
            // Show a message or navigate to a different activity
            // For example, you can show a message and then finish the activity
            Toast.makeText(this, "App is not allowed at this time.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the connectivity receiver
        unregisterReceiver(connectivityReceiver);
    }

    // Method to check if the device has an active internet connection
    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    // Method to show an AlertDialog when there is no internet connection
    protected void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your network settings and try again.")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retry button clicked, check internet connection again
                        if (isNetworkAvailable()) {
                            // Internet is available, continue with your activity logic here



                        } else {
                            // Internet is still not available, show the dialog again or handle it accordingly
                            showNoInternetDialog();
                        }
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit button clicked, close the app or handle it accordingly
                        finish();
                    }
                })
                .setCancelable(false)  // Make the dialog not dismissable by touching outside of it
                .show();
    }

    // Connectivity receiver to listen for changes in network connectivity
    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the device has an active internet connection
            if (!isNetworkAvailable()) {
                // Internet is not available, show an AlertDialog with a retry button
                showNoInternetDialog();
            }
        }
    }

    protected boolean isAppAllowed() {
        // Define the allowed days and time
        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Check if it's Monday to Friday (days 2 to 6 in Calendar)
       if (currentDayOfWeek >= Calendar.MONDAY && currentDayOfWeek <= Calendar.SATURDAY) {
            return currentHour >= 7 && currentHour <= 23; // 9 AM to 6 PM
        }
        // Check if it's an odd Saturday (day 7 in Calendar)
        else if (currentDayOfWeek == Calendar.SATURDAY && currentDayOfWeek % 2 == 0) {
            return currentHour >= 7 && currentHour <= 23; // 9 AM to 6 PM
        }

        // For all other days or times, return false
        return false;
    }

}
